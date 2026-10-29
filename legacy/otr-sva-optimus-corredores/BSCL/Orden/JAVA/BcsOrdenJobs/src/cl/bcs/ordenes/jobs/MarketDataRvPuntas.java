package cl.bcs.ordenes.jobs;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import cl.bcs.ordenes.pojo.ControlMercado;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;

public class MarketDataRvPuntas extends BcsJob implements JavaCall {
	
	String IdTipoPrograma = "MARKETDATARV_PUNTAS";
	private static final Logger logger = LogManager.getLogger(MarketDataRvPuntas.class);
	
	private final String SQL_OBTENER_MENSAJES_SIN_PROCESAR = loadResourceToString("cl/bcs/ordenes/jobs/sql/MarketDataRvPuntas_ObtenerMensajesSinProcesar.sql");
	private final String SQL_ACTUALIZA_PUNTA_COMPRA = loadResourceToString("cl/bcs/ordenes/jobs/sql/MarketDataRvPuntas_ActualizaPuntaCompra.sql");
	private final String SQL_ACTUALIZA_PUNTA_VENTA = loadResourceToString("cl/bcs/ordenes/jobs/sql/MarketDataRvPuntas_ActualizaPuntaVenta.sql");

	public boolean transaction(Connection conn) throws InterruptedException, Throwable {
		return logica(conn,false);
		
	}
	
	public boolean logica(Connection conn, boolean debug) throws SQLException {
		logger.info("Inicio Market Data Puntas RV");
		SQL sql = new SQL();
		
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		SimpleDateFormat dfFechaHora = new SimpleDateFormat("yyyy-MM-dd");
		
		//SELECT
		HashMap<String,Object> params1 = new HashMap<String,Object>();
		List<RegistroPunta> registros = sql.query(conn, SQL_OBTENER_MENSAJES_SIN_PROCESAR,params1,RegistroPunta.class);
		
		//LOOP
		ArrayList<BolsaInstrumentoPunta> puntasCompra = new ArrayList<BolsaInstrumentoPunta>();
		ArrayList<BolsaInstrumentoPunta> puntasVenta = new ArrayList<BolsaInstrumentoPunta>();
		
		ZoneId z = ZoneId.of("America/Santiago"); 
		DateTimeFormatter f = DateTimeFormatter.ISO_LOCAL_DATE;
		
		try {
			
		
			for(RegistroPunta punta: registros) {
					
				LocalDateTime ldt = LocalDateTime.parse( punta.Timestamp.replace(" ", "T")) ;
				OffsetDateTime odt = ldt.atOffset( ZoneOffset.UTC );
				ZonedDateTime zdt = odt.atZoneSameInstant( z );
				
				BolsaInstrumentoPunta pre = new BolsaInstrumentoPunta();
				pre.IdInstrumento = punta.IdInstrumento;
				pre.IdBolsa = "BCS";
				pre.Fecha = f.format(zdt);
				
				pre.IdCondicionDeLiquidacion = punta.CondicionLiquidacion == null ? "T0" : punta.CondicionLiquidacion;
				
				if (punta.CondicionLiquidacion == null || punta.CondicionLiquidacion.equalsIgnoreCase("PH")) {
					pre.IdCondicionDeLiquidacion = "T0";
				}
				else if (punta.CondicionLiquidacion.equalsIgnoreCase("CN")) {
					pre.IdCondicionDeLiquidacion = "T2";
				}
				else if (punta.CondicionLiquidacion.equalsIgnoreCase("PM")) {
					pre.IdCondicionDeLiquidacion = "T1";
				}
				else if (punta.CondicionLiquidacion.equalsIgnoreCase("T+3")) {
					pre.IdCondicionDeLiquidacion = "T3";
				}
				else {
					continue;
				}
				
				pre.Nemotecnico = punta.Instrumento;
				pre.PuntaCompra = punta.PrecioCompra == null?BigDecimal.ZERO:punta.PrecioCompra;
				pre.CantidadPuntaCompra = punta.CantidadCompra == null?BigDecimal.ZERO:punta.CantidadCompra;
				puntasCompra.add(pre);
				logger.debug("PUNTA COMPRA " + punta.Instrumento + " " + punta.PrecioCompra);
				
				pre.PuntaVenta = punta.PrecioVenta == null?BigDecimal.ZERO:punta.PrecioVenta;
				pre.CantidadPuntaVenta = punta.CantidadVenta== null?BigDecimal.ZERO:punta.CantidadVenta;
				puntasVenta.add(pre);
				logger.debug("PUNTA VENTA " + punta.Instrumento + " " + punta.PrecioVenta);
					
			}
			
			if (puntasCompra.size() > 0)
				sql.batch(conn,SQL_ACTUALIZA_PUNTA_COMPRA, puntasCompra,BolsaInstrumentoPunta.class);
			if (puntasVenta.size() > 0)
				sql.batch(conn,SQL_ACTUALIZA_PUNTA_VENTA, puntasVenta,BolsaInstrumentoPunta.class);
			logger.info("Fin Market Data Puntas " + (puntasCompra.size() + puntasVenta.size() + " puntas procesadas"));
		}
		catch(Exception e) {
			logger.error("ERROR",e.getMessage());
			throw e;
		}
		
		return true;
	}
	
	public static class RegistroPunta {
		
		public Long IdInstrumento;
		public String IdTipoInstrumento;
		public String Timestamp;
		public String Instrumento;
		public String CondicionLiquidacion;
		public BigDecimal CantidadCompra;
		public BigDecimal CantidadVenta;
		public BigDecimal PrecioCompra;
		public BigDecimal PrecioVenta;
		public String CorredorCompra;
		public String OperadorDirectoCompra;
		public String CorredorVenta;
		public String OperadorDirectoVenta;
		
		
		public String toString() {
			return cl.bcs.utils.Utils.printJson(this,true);		
		}
	}
	
	public static class BolsaInstrumentoPunta {
		public Long IdInstrumento;
		public String IdBolsa;
		public String IdCondicionDeLiquidacion;
		public String Fecha;
		public String Nemotecnico;
		//public String FechaActualizacion; AUTOMATICO, CURRENT_TIMESTAMP
		public BigDecimal PuntaCompra;
		public BigDecimal PuntaVenta;
		public BigDecimal CantidadPuntaCompra;
		public BigDecimal CantidadPuntaVenta;
		
		public String toString() {
			return cl.bcs.utils.Utils.printJson(this,true);		
		}
	}
	
	@Override
    public void onError(Throwable t) {
        logger.error(IdTipoPrograma + " ****** ERROR  *******");  
        t.printStackTrace();
    }

	@Override
	public void pause() {

	}
	
	public static void main(String[] args){
	
		
		while(true) {
			MarketDataRvPuntas p = new MarketDataRvPuntas();
		try {
			Connection asdf = PlataformaUtils.getMBIConnection();
			p.logica(asdf,false);
			asdf.commit();
			Thread.sleep(5);
			
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
	
	}




	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	
}
