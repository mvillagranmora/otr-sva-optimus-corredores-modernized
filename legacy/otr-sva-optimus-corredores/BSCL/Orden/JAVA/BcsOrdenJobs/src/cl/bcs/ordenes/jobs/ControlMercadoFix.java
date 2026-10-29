package cl.bcs.ordenes.jobs;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.ordenes.ProcesarMensajeFix;
import cl.bcs.ordenes.ProcesarMensajeFix.ProcesarMensajeFix_Entrada;
import cl.bcs.ordenes.ProcesarMensajeFix.ProcesarMensajeFix_Salida;
import cl.bcs.ordenes.ProcesarMensajeFix.RespuestaFix_Entrada;
import cl.bcs.ordenes.ProcesarMensajeFix.Resultado_Salida;
import cl.bcs.ordenes.jobs.ControlMercadoTransaccionesBolsaElectronica.Usuario;
import cl.bcs.ordenes.pojo.ControlMercado;
import cl.bcs.ordenes.pojo.RespuestaFix;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ControlMercadoFix extends BcsJob {

	private Logger logger = null;
	
	private final String SQL_ACTUALIZA_CONTROL_MERCADO = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/Globales_ActualizaControlMercado.sql");
	private final String SQL_OBTENER_MENSAJES_SIN_PROCESAR = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/ControlMercadoFix_ObtenerMensajesSinProcesar.sql");
	private final String SQL_SELECT_USUARIO_SISTEMA = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/Globales_SelectUsuarioSistema.sql");

	public static class RespuestaFix_Proceso extends RespuestaFix {
		public Long IdInterno;
		public String _TipoMensaje;
		public String _IdMensaje1;
		public String _IdMensaje2;
		public ControlMercado registroControlMercado = new ControlMercado();
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Usuario {
		public Long IdUsuario;
		
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
	
	class ConfiguracionFix {
		String nombreTabla;
		String tipoMensaje;
	}
	
	
	
	public boolean logica(Connection conn,boolean debug) throws Exception {
		try {
			
			HashMap<String,ConfiguracionFix> clientesFix = new HashMap<>();
			
			ConfiguracionFix retail = new ConfiguracionFix();
			retail.nombreTabla = "BSCL.Orden.BD::DMA.TBRESPUESTAS_2";
			retail.tipoMensaje = "RESPUESTA_DMA_RETAIL";
			
			ConfiguracionFix agente = new ConfiguracionFix();
			agente.nombreTabla = "BSCL.Orden.BD::DMA.TBRESPUESTAS";
			agente.tipoMensaje = "RESPUESTA_DMA_AGENTE";
			
			
			clientesFix.put("RETAIL", retail);
			clientesFix.put("AGENTE", agente);
			
			Long idEmpresa = 0L;
	        String clienteFix = "";
	        
	        if (debug) {
	        	idEmpresa = 1L;
	        	clienteFix = "RETAIL";	//O RETAIL
	        } else{
	        	 idEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
	             clienteFix = this.getParam("clienteFix");
	        }
	        
	        Long idUsuario = 0L;
	        {
	        	SQL _sql = new SQL();
	        	HashMap<String,Object> params = new HashMap<String,Object>();
				params.put("IdEmpresa", idEmpresa);
				List<Usuario> usuarioSistema = _sql.query(conn, SQL_SELECT_USUARIO_SISTEMA, params, Usuario.class);        
				idUsuario = usuarioSistema.get(0).IdUsuario;
			}
	        ConfiguracionFix configuracionActual = clientesFix.get(clienteFix);
	        
	        logger = LogManager.getLogger(ControlMercadoFix.class + "-" + clienteFix);
	        
	        if (configuracionActual == null) {
	        	logger.debug("No se encontro configuracion");
	        	return false;
	        }
	        
	        logger.debug("Inicio Ciclo FIX " + clienteFix);
			SQL _sql = new SQL();
			
			String queryMensajesSinProcesar = SQL_OBTENER_MENSAJES_SIN_PROCESAR.replace("<NOMBRE_TABLA>", configuracionActual.nombreTabla);
			HashMap<String,Object> params = new HashMap<String,Object>();
			params.put("TipoMensaje", configuracionActual.tipoMensaje);
			
			List<RespuestaFix_Proceso> mensajesFix = _sql.query(conn, queryMensajesSinProcesar, params, RespuestaFix_Proceso.class);
			logger.debug(mensajesFix.size() + " nuevos mensajes.");
			for (RespuestaFix_Proceso mensaje : mensajesFix) {
				procesarMensajeFix(mensaje,idEmpresa,idUsuario,conn,configuracionActual);
			}
			
			logger.info("Fin Ciclo FIX " + clienteFix);
		}
		catch (Exception e) {
			logger.error("Error En Cliente Fix");
			logger.error(e);
			throw e;
		}
		
		return true;
	}
	
	private void procesarMensajeFix(RespuestaFix_Proceso msjFix, Long idEmpresa, Long idUsuario, Connection conn, ConfiguracionFix configuracionActual) {
		
		try {
			
			logger.info("Procesando mensaje:"+msjFix.toString());
			
			msjFix.registroControlMercado.Log = "";
			msjFix.registroControlMercado.Procesado = "N";
			msjFix.registroControlMercado.IdMensaje1 = msjFix._IdMensaje1;
			msjFix.registroControlMercado.IdMensaje2 = msjFix._IdMensaje2;
				
			if (msjFix.NRO_ENTRADA.equals(new BigDecimal(-1))) {
				msjFix.registroControlMercado.Procesado = "E";
				msjFix.registroControlMercado.Log = "ERROR, NO SE ENCONTRO MENSAJE FIX " + msjFix._IdMensaje1 + " - " + msjFix._IdMensaje2;
				logger.debug(msjFix.registroControlMercado.Log);
			}
				
			ProcesarMensajeFix_Entrada entradaProcesarMensajeFix = new ProcesarMensajeFix_Entrada();
			entradaProcesarMensajeFix.p_IdEmpresa = idEmpresa;
			entradaProcesarMensajeFix.p_IdUsuario = idUsuario;
			entradaProcesarMensajeFix.p_Tabla = configuracionActual.nombreTabla;
			entradaProcesarMensajeFix.p_TipoMensaje = configuracionActual.tipoMensaje;
			
			if (!msjFix.registroControlMercado.Procesado.equalsIgnoreCase("E")) {
				RespuestaFix_Entrada msjFixEntrada = new RespuestaFix_Entrada();
				Utils.copyPojo(msjFix, msjFixEntrada);
				entradaProcesarMensajeFix.p_MensajesFix.add(msjFixEntrada);
				
				ProcesarMensajeFix serviceProcesarMensajeFix = new ProcesarMensajeFix();
				ProcesarMensajeFix_Salida salidaProcesarMensajeFix = serviceProcesarMensajeFix.logica(conn, entradaProcesarMensajeFix);
				
				for (Resultado_Salida resp : salidaProcesarMensajeFix.P_OUT_RESULTADO) {
				
					if (msjFix.IdInterno.equals(resp.IdInterno)) {
						
						if (resp.Tipo.equalsIgnoreCase(MensajeSalida.MENSAJE_OK)) {
							msjFix.registroControlMercado.Procesado = "S";
							logger.debug("Registro " + msjFix.registroControlMercado.IdMensaje1 + " - " + msjFix.registroControlMercado.IdMensaje2 + " procesado OK - " + resp.MensajeReferencia + " " + resp.IdReferencia);
						}
						else if (resp.Tipo.equalsIgnoreCase(MensajeSalida.MENSAJE_ADVERTENCIA)) {
							msjFix.registroControlMercado.Procesado = "R";
							logger.debug("Registro " + msjFix.registroControlMercado.IdMensaje1 + " - " + msjFix.registroControlMercado.IdMensaje2 + " procesado ADVERTENCIA - " + resp.MensajeReferencia);
						}
					}
				}
				
				for (MensajeSalida msj : salidaProcesarMensajeFix.P_OUT_MENSAJES) {
					msjFix.registroControlMercado.Log+=msj.descripcion+"\n";
					logger.debug(msj.descripcion);
				}
			}
			
			if (!msjFix.registroControlMercado.Procesado.equalsIgnoreCase("S") && !msjFix.registroControlMercado.Procesado.equalsIgnoreCase("R")) {
				msjFix.registroControlMercado.Procesado = "E";
				logger.debug("Registro " + msjFix.registroControlMercado.IdMensaje1 + " - " + msjFix.registroControlMercado.IdMensaje2 + msjFix.registroControlMercado.Log);
			}
			
			
			if (msjFix.registroControlMercado.Procesado.equalsIgnoreCase("E")) {
				conn.rollback();
			}
			
			List<ControlMercado> updateControlMercado = new ArrayList<>();
			updateControlMercado.add(msjFix.registroControlMercado);
			SQL _sql = new SQL();
			_sql.batch(conn, SQL_ACTUALIZA_CONTROL_MERCADO, updateControlMercado, ControlMercado.class);
			conn.commit();				
			
		}
		catch (Exception e) {
			try {
				logger.error("Error en ControlMercadoFix",e);
				conn.rollback();
				List<ControlMercado> updateControlMercado = new ArrayList<>();
				updateControlMercado.add(msjFix.registroControlMercado);
				msjFix.registroControlMercado.Log = e.toString();
				SQL _sql = new SQL();
				_sql.batch(conn, SQL_ACTUALIZA_CONTROL_MERCADO, updateControlMercado, ControlMercado.class);
				conn.commit();	
			} catch (SQLException e1) {
				logger.error(e1);
			}
		}
		
	}

	public static void main(String[] args) {
        //System.out.println("== START ==");
        try {
            Connection con = PlataformaUtils.getPropertiesConnection("mbipre", "connections.properties");
            // ---------------------------------
            try {
            	ControlMercadoFix demo=new ControlMercadoFix();
            	SQL.exec(con, "UPDATE \"BSCL.Orden.BD::ORD.ControlMercado\" SET \"Procesado\" = 'N' "
						+ "WHERE \"IdMensaje1\" = '-406' AND \"IdMensaje2\" = '2025-06-092025-06-09:11:21:45.102';");
            	while (true) {
            		demo.logica(con,true);
            		Thread.sleep(5000);
            	}
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
