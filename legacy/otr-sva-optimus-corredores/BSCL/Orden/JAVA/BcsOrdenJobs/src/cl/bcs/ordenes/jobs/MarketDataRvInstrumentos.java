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

public class MarketDataRvInstrumentos extends BcsJob implements JavaCall {
	
	AtomicBoolean running=new AtomicBoolean(false);
	private static final Logger logger = LogManager.getLogger(MarketDataRvInstrumentos.class);
	
	private final String SQL_OBTENER_MENSAJES_SIN_PROCESAR = loadResourceToString("cl/bcs/ordenes/jobs/sql/MarketDataRvInstrumentos_ObtenerMensajesSinProcesar.sql");
	private final String SQL_ACTUALIZA_PRECIOS = loadResourceToString("cl/bcs/ordenes/jobs/sql/MarketDataRvInstrumentos_ActualizaPrecios.sql");
	
	public boolean transaction(Connection conn) throws InterruptedException, Throwable {
		return go(conn);
	}
	
	
	public static class RegistroPrecio {
		public Long IdInstrumento;
		public String IdTipoInstrumento;
		public String codTicker;
		public String timeStamp;
		public String action;
		public String instruments;
		public String codIsin;
		public String market;
		public BigDecimal minPrice;
		public BigDecimal openPrice;
		public BigDecimal highPrice;
		public BigDecimal imbalance;
		public BigDecimal tradeVolume;
		public BigDecimal avgPrice;
		public BigDecimal tradeAmount;
		public BigDecimal closingPrice;
		public String trend;
		public String dcv;
		public String instrumentType;
		public String timeStampLastTrade;
		public BigDecimal lastPrice;
		public BigDecimal quantityLastTrade;
		
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
		logger.info("Inicio Captura de Precios");
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
			
			
			if (precio.instruments.startsWith("T-")) {
				//nemo test se ignora
			}else if (precio.IdInstrumento == null) {
				logger.warn("Error, instrumento " + precio.instruments + " no existe");
			}
			else {
				BolsaInstrumentoPrecio pre = new BolsaInstrumentoPrecio();
				pre.IdInstrumento = precio.IdInstrumento;
				pre.IdBolsa = precio.codTicker.contains("XLIM") ? "BVL" : "BCS";
				pre.Fecha = fecha;
				pre.Nemotecnico = precio.instruments;
				pre.PrecioMayor = precio.highPrice;
				pre.PrecioMenor = precio.minPrice;
				pre.PrecioMedio = precio.avgPrice;
				pre.PrecioApertura = precio.openPrice;
				pre.PrecioCierre = precio.closingPrice;
				pre.UnidadesTransadas = precio.tradeVolume;
				pre.MontoTransado = precio.tradeAmount;
				pre.Variacion = precio.imbalance;
				
				precios.add(pre);
				
			}
			
		}
		
		logger.debug("Instrumentos Actualizados:" + precios.size());
		
		sql.batch(conn,SQL_ACTUALIZA_PRECIOS, precios,BolsaInstrumentoPrecio.class);
		logger.debug("Instrumentos Actualizados Fin:" + precios.size());
		conn.commit();
		logger.info("FIN ---------------------------- ");
		
		running.set(false);
		return true;
	}
	
	@Override
    public void onError(Throwable t) {
        logger.error(" ****** ERROR  *******");  
        t.printStackTrace();
    }

	@Override
	public void pause() {

	}
	
	public static void main(String[] args) throws InterruptedException, SQLException, Throwable{
	
		MarketDataRvInstrumentos p = new MarketDataRvInstrumentos();
		Connection conn = PlataformaUtils.getMBIPREConnection();
		p.go(conn);
		conn.rollback();

	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		return null;
	}

	
}
