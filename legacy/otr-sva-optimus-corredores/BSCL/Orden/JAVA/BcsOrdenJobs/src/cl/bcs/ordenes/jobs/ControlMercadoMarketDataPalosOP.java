package cl.bcs.ordenes.jobs;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.ordenes.CrearTransaccionesMarketData;
import cl.bcs.ordenes.CrearTransaccionesMarketData.CrearTransaccionesMarketData_Entrada;
import cl.bcs.ordenes.CrearTransaccionesMarketData.CrearTransaccionesMarketData_Salida;
import cl.bcs.ordenes.CrearTransaccionesMarketData.Resultado_Salida;
import cl.bcs.ordenes.CrearTransaccionesMarketData.TransaccionOPMarketData_Entrada;
import cl.bcs.ordenes.pojo.ControlMercado;
import cl.bcs.ordenes.pojo.TransaccionOPMarketData;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ControlMercadoMarketDataPalosOP extends BcsJob {

	private static final Logger LOGGER = LogManager.getLogger(ControlMercadoMarketDataPalosOP.class);
	
	private final String SQL_SELECT_USUARIO_SISTEMA = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/Globales_SelectUsuarioSistema.sql");
	private final String SQL_SELECT_TRANSACCIONES = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/ControlMercadoMarketDataPalosOP_SelectTransacciones.sql");
	private final String SQL_ACTUALIZA_CONTROL_MERCADO = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/Globales_ActualizaControlMercado.sql");
	private final String SQL_INSERT_HIS_TRANSACCIONES = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/ControlMercadoMarketDataPalosOP_Insert_His_Transacciones.sql");
	
	public static class TransaccionOP_Proceso extends TransaccionOPMarketData {
		public Long IdInterno;
		public String _TipoMensaje;
		public String _IdMensaje1;
		public String _IdMensaje2;
		public ControlMercado registroControlMercado = new ControlMercado();
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
		return logica (con,false);
	}

	@Override
	public void onError(Throwable t) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}
	
	public static class Usuario {
		public Long IdUsuario;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public boolean logica(Connection conn,boolean debug) {
		try {
			LOGGER.info("Inicio Ciclo MarketData OP");
			SQL _sql = new SQL();
			List<TransaccionOP_Proceso> transacciones = _sql.query(conn, SQL_SELECT_TRANSACCIONES, new HashMap<String,Object>(), TransaccionOP_Proceso.class);
			LOGGER.debug(transacciones.size() + " nuevos mensajes.");
			if (transacciones.size() == 0){
				return true;
			}
			
			Long idEmpresa = 0L;
	        
	        if (debug) {
	        	idEmpresa = 1L;
	        } else{
	        	 idEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
	        }
			
	        HashMap<String,Object> params = new HashMap<String,Object>();
			params.put("IdEmpresa", idEmpresa);
			List<Usuario> usuarioSistema = _sql.query(conn, SQL_SELECT_USUARIO_SISTEMA, params, Usuario.class);        
			Long idUsuario = usuarioSistema.get(0).IdUsuario;
			
			for (TransaccionOP_Proceso tran : transacciones) {
				
				tran.registroControlMercado.Log = "";
				tran.registroControlMercado.Procesado = "N";
				tran.registroControlMercado.IdMensaje1 = tran._IdMensaje1;
				tran.registroControlMercado.IdMensaje2 = tran._IdMensaje2;
				
				if (tran.codTicker.equalsIgnoreCase("")) {
					tran.registroControlMercado.Procesado = "E";
					tran.registroControlMercado.Log = "ERROR, NO SE ENCONTRO TRANSACCION " + tran._IdMensaje1 + " - " + tran._IdMensaje2;
					LOGGER.debug(tran.registroControlMercado.Log);
				}
				
			}
			
			CrearTransaccionesMarketData_Entrada entradaCrearTransaccionesMarketData = new CrearTransaccionesMarketData_Entrada();
			entradaCrearTransaccionesMarketData.p_IdEmpresa = idEmpresa;
			entradaCrearTransaccionesMarketData.p_IdUsuario = idUsuario;
			for (TransaccionOP_Proceso tran : transacciones) {
				
				if (!tran.registroControlMercado.Procesado.equalsIgnoreCase("E")) {
					TransaccionOPMarketData_Entrada tx = new TransaccionOPMarketData_Entrada();
					tx.IdInterno			= tran.IdInterno;
					tx.codTicker			= tran.codTicker;			
					tx.timeStamp			= tran.timeStamp;
					tx.action				= tran.action;
					tx.tradeTime			= tran.tradeTime;
					tx.tradeID				= tran.tradeID;
					tx.instrument			= tran.instrument;
					tx.market				= tran.market;
					tx.settlementCondition	= tran.settlementCondition;
					tx.orderType			= tran.orderType!=null ? tran.orderType.trim() :" ";
					tx.indivisible			= tran.indivisible;
					tx.shortSell			= tran.shortSell;
					tx.quantity				= tran.quantity;
					tx.price				= tran.price;
					tx.amount				= tran.amount;
					tx.closingPrice			= tran.closingPrice;
					tx.buyerBroker			= tran.buyerBroker;
					tx.buyEnteringFirm		= tran.buyEnteringFirm;
					tx.buyEnteringTrader	= tran.buyEnteringTrader;
					tx.sellerBroker			= tran.sellerBroker;
					tx.sellEnteringFirm		= tran.sellEnteringFirm;
					tx.sellEnteringTrader	= tran.sellEnteringTrader;
					tx.buyFund				= tran.buyFund;	
					tx.sellFund				= tran.sellFund;	
					tx.currency				= tran.currency;	
					tx.tradeCondition		= tran.tradeCondition;
					tx.indDcv				= tran.indDcv;			
					
					entradaCrearTransaccionesMarketData.transaccionesOP.add(tx);
				}
				
			}
			
			CrearTransaccionesMarketData serviceCrearTransaccionesMarketData = new CrearTransaccionesMarketData();
			CrearTransaccionesMarketData_Salida salidaCrearTransaccionesMarketData = serviceCrearTransaccionesMarketData.logica(conn, entradaCrearTransaccionesMarketData);
			
			for (TransaccionOP_Proceso tran : transacciones) {
				
				for (Resultado_Salida resp : salidaCrearTransaccionesMarketData.P_OUT_RESULTADO) {
				
					if (tran.IdInterno.equals(resp.IdInterno)) {
					
						tran.registroControlMercado.Procesado = "S";
						LOGGER.info("Registro " + tran.registroControlMercado.IdMensaje1 + " - " + tran.registroControlMercado.IdMensaje2 + " procesado OK");
					}
				}
				
				if (!tran.registroControlMercado.Procesado.equalsIgnoreCase("S")) {
					tran.registroControlMercado.Procesado = "E";
					LOGGER.info("Registro " + tran.registroControlMercado.IdMensaje1 + " - " + tran.registroControlMercado.IdMensaje2 + " procesado ERROR");
				}
				
				for (MensajeSalida msj : salidaCrearTransaccionesMarketData.P_OUT_MENSAJES) {
					if (tran.IdInterno.equals(msj.id)) {
						tran.registroControlMercado.Log+=msj.descripcion+"\n";
						LOGGER.info(msj.descripcion);
					}
					
				}
	    	}
			
			List<ControlMercado> updateControlMercado = new ArrayList<>();
			for (TransaccionOP_Proceso tran : transacciones) {
				updateControlMercado.add(tran.registroControlMercado);
			}
			
			//ACTUALIZAR CONTROL MERCADO
			if (updateControlMercado.size() > 0) {
				_sql.batch(conn, SQL_ACTUALIZA_CONTROL_MERCADO, updateControlMercado, ControlMercado.class);
			}
			//INSERTAR HISTORICO DE TRANSACCIONES
			_sql.batch(conn, SQL_INSERT_HIS_TRANSACCIONES, transacciones, TransaccionOP_Proceso.class);
				conn.commit();
			
//			conn.rollback();
			
			LOGGER.info("Fin Ciclo MarketData OP");
		}
		catch (Exception e) {
			LOGGER.error("ERROR AL CREAR TRANSACCION");
			LOGGER.error(e);
		}
		
		return true;
	}
	
	public static void main(String[] args) {
        //System.out.println("== START ==");
        try {
            Connection con = PlataformaUtils.getBCOESTADOConnection();
            ControlMercadoMarketDataPalosOP demo=new ControlMercadoMarketDataPalosOP();
        	
            // ---------------------------------
            try {
            	demo.logica(con,true);
                //con.rollback();
            } catch (Throwable er) {
                er.printStackTrace();
                con.rollback();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("== END ==");       
    }
	
}
