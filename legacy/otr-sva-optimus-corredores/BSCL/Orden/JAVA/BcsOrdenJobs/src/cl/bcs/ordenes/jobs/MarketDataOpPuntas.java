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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import cl.bcs.ordenes.pojo.ControlMercado;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;

public class MarketDataOpPuntas extends BcsJob implements JavaCall {
	
	String IdTipoPrograma = "MARKETDATAOP_PUNTAS";
	private static final Logger LOGGER = LogManager.getLogger(MarketDataRvPuntas.class);
	
	private final String SQL_OBTENER_MENSAJES_SIN_PROCESAR = loadResourceToString("cl/bcs/ordenes/jobs/sql/MarketDataOpPuntas_ObtenerMensajesSinProcesar.sql");
	private final String SQL_ACTUALIZA_PUNTA_COMPRA = loadResourceToString("cl/bcs/ordenes/jobs/sql/MarketDataOpPuntas_ActualizaPuntaCompra.sql");
	private final String SQL_ACTUALIZA_PUNTA_VENTA = loadResourceToString("cl/bcs/ordenes/jobs/sql/MarketDataOpPuntas_ActualizaPuntaVenta.sql");
	private final String SQL_ACTUALIZA_CONTROL_MERCADO = loadResourceToString("cl/bcs/ordenes/jobs/sql/Globales_ActualizaControlMercado.sql");

	public boolean transaction(Connection conn) throws InterruptedException, Throwable {
		return logica(conn,false);
		
	}
	
	public boolean logica(Connection conn, boolean debug) throws SQLException {
		LOGGER.info("Inicio Market Data Puntas OP");
		SQL sql = new SQL();
		
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		SimpleDateFormat dfFechaHora = new SimpleDateFormat("yyyy-MM-dd");
		String fecha = dfFechaHora.format(cal.getTime());
		
		//SELECT
		HashMap<String,Object> params1 = new HashMap<String,Object>();
		List<RegistroPunta> registros = sql.query(conn, SQL_OBTENER_MENSAJES_SIN_PROCESAR,params1,RegistroPunta.class);
		
		//LOOP
		ArrayList<BolsaInstrumentoPunta> puntasCompra = new ArrayList<BolsaInstrumentoPunta>();
		ArrayList<BolsaInstrumentoPunta> puntasVenta = new ArrayList<BolsaInstrumentoPunta>();
		ArrayList<ControlMercado> registrosControlMercado =  new ArrayList<>();
		
		try {
			
		
			for(RegistroPunta punta: registros) {
				
				ControlMercado ControlMercado = new ControlMercado();
				ControlMercado.IdMensaje1 = punta.IdMensaje1;
				ControlMercado.IdMensaje2 = punta.IdMensaje2;
				
				if (punta.IdInstrumento == null) {
					ControlMercado.Procesado = "E";
					ControlMercado.Log = "Mensaje Punta:" + punta.IdMensaje2 + " no existe";
					registrosControlMercado.add(ControlMercado);
					
					LOGGER.debug("ERROR " + ControlMercado.toString());
				}
				else {
					
					BolsaInstrumentoPunta pre = new BolsaInstrumentoPunta();
					pre.IdInstrumento = punta.IdInstrumento;
					pre.IdBolsa = "BCS";
					pre.Fecha = fecha;
					
					pre.IdCondicionDeLiquidacion = punta.settlement == null ? "T0" : punta.settlement;
					
					if (punta.settlement == null || punta.settlement.equalsIgnoreCase("PH")) {
						pre.IdCondicionDeLiquidacion = "T0";
					}
					else if (punta.settlement.equalsIgnoreCase("CN")) {
						pre.IdCondicionDeLiquidacion = "T2";
					}
					else if (punta.settlement.equalsIgnoreCase("CN")) {
						pre.IdCondicionDeLiquidacion = "T1";
					}
					else {
						pre.IdCondicionDeLiquidacion = "T2";
					}
					
					pre.Nemotecnico = punta.symbol;
					if (punta.price != null) {
						pre.PuntaCompra = punta.price;
						pre.CantidadPuntaCompra = punta.amount;
						puntasCompra.add(pre);
						LOGGER.debug("PUNTA COMPRA " + punta.symbol + " " + punta.price);
					}
					if (punta.priceSale != null) {
						pre.PuntaVenta = punta.priceSale;
						pre.CantidadPuntaVenta = punta.amountSale;
						puntasVenta.add(pre);
						LOGGER.debug("PUNTA VENTA " + punta.symbol + " " + punta.priceSale);
					}
						
					ControlMercado.Procesado = "S";
					ControlMercado.Log = "OK";
					registrosControlMercado.add(ControlMercado);
					
				}
				
				
			}
			
			if (puntasCompra.size() > 0)
				sql.batch(conn,SQL_ACTUALIZA_PUNTA_COMPRA, puntasCompra,BolsaInstrumentoPunta.class);
			if (puntasVenta.size() > 0)
				sql.batch(conn,SQL_ACTUALIZA_PUNTA_VENTA, puntasVenta,BolsaInstrumentoPunta.class);
			if (registrosControlMercado.size() > 0)
				sql.batch(conn,SQL_ACTUALIZA_CONTROL_MERCADO, registrosControlMercado,ControlMercado.class);
			LOGGER.info("Fin Market Data Puntas " + (puntasCompra.size() + puntasVenta.size() + " puntas procesadas"));
		}
		catch(Exception e) {
			LOGGER.error("ERROR",e.getMessage());
			throw e;
		}
		
		return true;
	}
	
	public static class RegistroPunta {
		public String IdMensaje1;
		public String IdMensaje2;
		public Long IdInstrumento;
		public String IdTipoInstrumento;
		
		public String codTicker;
		public String timeStamp;
		public String action;
		public String symbol;
		public BigDecimal amount;
		public BigDecimal amountSale;
		public BigDecimal price;
		public BigDecimal priceSale;
		public String settlement;
		public String buyExecutingFirm;
		public String buyEnteringFirm;
		public String buyEnteringTrader;
		public String sellExecutingFirm;
		public String sellEnteringFirm;
		public String sellEnteringTrader;
		public BigDecimal entryPositionNo;
		public String bookingRefID;

		/*public String Timestamp;
		public String Instrumento;
		public String CondicionLiquidacion;
		public BigDecimal CantidadCompra;
		public BigDecimal CantidadVenta;
		public BigDecimal PrecioCompra;
		public BigDecimal PrecioVenta;
		public String CorredorCompra;
		public String OperadorDirectoCompra;
		public String CorredorVenta;
		public String OperadorDirectoVenta;*/
		
		
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
        LOGGER.error(IdTipoPrograma + " ****** ERROR  *******");  
        t.printStackTrace();
    }

	@Override
	public void pause() {

	}
	
	public static void main(String[] args){
	
		
		while(true) {
			MarketDataOpPuntas p = new MarketDataOpPuntas();
		try {
			Connection asdf = PlataformaUtils.getDESAConnection();
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
