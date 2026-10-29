package cl.bcs.ordenes.jobs;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;

public class MarketDataOpPrecioInstrumentos extends BcsJob implements JavaCall {
	
	AtomicBoolean running=new AtomicBoolean(false);
	private static final Logger LOGGER = LogManager.getLogger(MarketDataOpPrecioInstrumentos.class);
	
	private final String SQL_OBTENER_MENSAJES_SIN_PROCESAR = loadResourceToString("cl/bcs/ordenes/jobs/sql/MarketDataOpPrecioInstrumentos_ObtenerMensajesSinProcesar.sql");
	private final String SQL_ACTUALIZA_PRECIOS = loadResourceToString("cl/bcs/ordenes/jobs/sql/MarketDataOpPrecioInstrumentos_ActualizaPrecios.sql");
	
	public boolean transaction(Connection conn) throws InterruptedException, Throwable {
		return go(conn);
	}
	
	
	public static class RegistroPrecio {
		
		public Long IdInstrumento;
		public String IdTipoInstrumento;
		public String codTicker;
		public String timeTicker;
		public String codIsin;
		public String action;
		public String instruments;
		public BigDecimal imbalance;
		public BigDecimal tradeVolume;
		public BigDecimal avgPrice;
		public BigDecimal tradeAmount;
		public String trend;
		public String cfiCode;
		public String maturityDate;
		public String issuer;
		public BigDecimal eventType;
		public String settlement;
		public String exercise;
		public String openContract;
		public BigDecimal StrikePrice;
		public BigDecimal minPrice;
		public BigDecimal highPrice;
		
		public String toString() {
			return cl.bcs.utils.Utils.printJson(this,true);		
		}
	}
	
	public static class BolsaInstrumentoPrecio {
		public Long IdInstrumento;
		public String IdBolsa;
		public String Fecha;
		public String Nemotecnico;
		//public String FechaActualizacionMD; AUTOMATICO, CURRENT_TIMESTAMP
		public BigDecimal PrecioMayor;
		public BigDecimal PrecioMenor;
		public BigDecimal PrecioApertura;
		public BigDecimal PrecioCierre;
		public BigDecimal PrecioMedio;
		public BigDecimal UnidadesTransadas;
		public BigDecimal MontoTransado;
		public BigDecimal Variacion;
		
		public String toString() {
			return cl.bcs.utils.Utils.printJson(this,true);		
		}
	}
	
	public boolean go(Connection conn) throws Throwable {
		LOGGER.info("Inicio Captura de Precios");
		SQL sql = new SQL();
		running.set(true);
		
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		String fecha = sdf.format(calendar.getTime());
		
		//SELECT
		HashMap<String,Object> params1 = new HashMap<String,Object>();
		List<RegistroPrecio> registros = sql.query(conn, SQL_OBTENER_MENSAJES_SIN_PROCESAR,params1,RegistroPrecio.class);
		
		//LOOP
		ArrayList<BolsaInstrumentoPrecio> precios = new ArrayList<BolsaInstrumentoPrecio>();
		
		for(RegistroPrecio precio: registros) {
			
			
			if (precio.IdInstrumento == null) {
				LOGGER.error("Error, instrumento " + precio.instruments + " no existe");
			}
			else if (precio.instruments.startsWith("T-")) {
				
				//nemo test se ignora
			}
			else {
				BolsaInstrumentoPrecio pre = new BolsaInstrumentoPrecio();
				pre.IdInstrumento = precio.IdInstrumento;
				pre.IdBolsa = "BCS";
				pre.Fecha = fecha;
				pre.Nemotecnico = precio.instruments;
				pre.PrecioMayor = precio.highPrice;
				pre.PrecioMenor = precio.minPrice;
				pre.PrecioMedio = precio.avgPrice;
				pre.PrecioApertura = precio.StrikePrice;
				pre.PrecioCierre = precio.StrikePrice;
				pre.UnidadesTransadas = precio.tradeVolume;
				pre.MontoTransado = precio.tradeAmount;
				pre.Variacion = precio.imbalance;
				
				precios.add(pre);
				
			}
			
		}
		
		LOGGER.debug("Instrumentos Opciones Actualizados:" + precios.size());
		
		sql.batch(conn,SQL_ACTUALIZA_PRECIOS, precios,BolsaInstrumentoPrecio.class);
		conn.commit();
		LOGGER.info("FIN ---------------------------- ");
		
		running.set(false);
		return true;
	}
	
	@Override
    public void onError(Throwable t) {
        LOGGER.error(" ****** ERROR  *******");  
        t.printStackTrace();
    }

	@Override
	public void pause() {

	}
	
	public static void main(String[] args) throws InterruptedException, SQLException, Throwable{
	
		MarketDataOpPrecioInstrumentos p = new MarketDataOpPrecioInstrumentos();
		Connection conn = PlataformaUtils.getDESAConnection();
		p.go(conn);
		conn.commit();

	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		return null;
	}

	
}
