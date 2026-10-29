package cl.bcs.ordenes.jobs;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.JsonObject;

import cl.bcs.ordenes.CargaArchivoAsignacionRF;
import cl.bcs.ordenes.CargaArchivoAsignacionRV;
import cl.bcs.ordenes.CargaArchivoCuadraturaCavali;
import cl.bcs.plataforma.ins.CargaArchivoPresenciaBursatil;
import cl.bcs.ordenes.CargaArchivoIGK;
import cl.bcs.ordenes.CargaArchivoOrdenesRF;
import cl.bcs.ordenes.CargaArchivoOrdenesRV;
import cl.bcs.ordenes.CargaArchivoTransaccionBVL;
import cl.bcs.ordenes.CargaArchivoTransaccionROIX;
import cl.bcs.ordenes.CargaArchivoTransaccionTOMS;
import cl.bcs.ordenes.CargaArchivoTransaccionTOMS.Instrumentos;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CargarArchivoOrdenesTransacciones extends BcsJob implements JavaCall{
	private final Log log = new Log("ORD.CargarArchivoOrdenesTransacciones");
	private final String SQL_UPDATE_ID_PROGRAMA = loadResourceToString("cl/bcs/ordenes/jobs/sql/CargaArchivosOrdenesTransacciones_update.sql");
	private final String SQL_SELECT_TOLERANCIA_ERRORES = loadResourceToString("cl/bcs/ordenes/jobs/sql/CargarArchivoOrdenesTransacciones_SelectToleranciaErrores.sql");
	
	public static class ActualizarPrograma{
		public String 	RutaLog;
		public String 	RutaArchivo;
		public Long 	IdPrograma;
		
		public ActualizarPrograma(String RutaArchivo,String RutaLog,Long IdPrograma){
			this.RutaArchivo = RutaArchivo;
			this.RutaLog = RutaLog;
			this.IdPrograma = IdPrograma;
		}
	}
	
	public static class Configuracion{
		public String Valor;
	}
	
	@Override
    public boolean transaction(Connection con) throws InterruptedException, Throwable {
		return logica(con,false);
	}
	
    public boolean logica(Connection con,boolean debug) throws InterruptedException, Throwable {
    	try {
    		Long idEmpresa = 1L;
            Long idUsuario = 3L;
            String modulo = "ORD";
            String tipoArchivo = "";
            String contenidoArchivo = "";
            String nombreArchivo = "";
            String rutaLog = "";
            SQL sql=new SQL();
            if(debug) {
            	idEmpresa = 1L;
                idUsuario = 102L;
                modulo = "ORD";
                tipoArchivo = "CUADRATURA_CAVALI";
                nombreArchivo = "OPE99Prueba.txt";
            	contenidoArchivo = "eyJwX2NvbnRlbmlkbyI6W3siQ0NDQVZfRkVDSEFPUEVSQUNJT04iOiIyMDE4MDQxMCIsIkNDQ0FWX01PREFMSURBRCI6IjAxIiwiQ0NDQVZfTlVNRVJPT1BFUkFDSU9OIjoiMDAyMDAwIiwiQ0NDQVZfVElQT01PTkVEQSI6IjAxIiwiQ0NDQVZfVElQT09QRVJBQ0lPTiI6IjAxIiwiQ0NDQVZfVElQT0lOU1RSVU1FTlRPIjoiMDIiLCJDQ0NBVl9FU1RBRE9PUEVSQUNJT04iOiIwMCIsIkNDQ0FWX0NPRElHT1ZBTE9SIjoiUEVQMjE0MDAxMDA1IiwiQ0NDQVZfQ09ESUdPVkFMT1IyIjoiQUxJQ09SQzEgICIsIkNDQ0FWX1BSRUNJT09QRVJBQ0lPTiI6Ni43NzcyLCJDQ0NBVl9DQU5USURBRE9QRVJBQ0lPTiI6MTAwLCJDQ0NBVl9BR0VOVEVDT01QUkFET1IiOiIwMDAxIiwiQ0NDQVZfQUdFTlRFVkVOREVET1IiOiIwMDAxIiwiQ0NDQVZfTU9OVE9FRkVDVElWTyI6Njc3LjcyLCJDQ0NBVl9WQUxPUk5PTUlOQUwiOjAuMDAwMDAxLCJDQ0NBVl9UQVNBT1BFUkFDSU9OIjowLCJDQ0NBVl9GRUNIQUxJUUNPTlRBRE8iOiIyMDE4MDQxMCIsIkNDQ0FWX0ZFQ0hBTElRUExBWk8iOiIwMDAwMDAwMCIsIkNDQ0FWX0NBTlRJREFERElBU09QRSI6IjAwMDAiLCJDQ0NBVl9USVBPQVZBTCI6IjAwIiwiQ0NDQVZfQ09ESUdPQVZBTCI6IjAwMDAiLCJDQ0NBVl9USVBPTElRVUlEQUNJT04iOiIwMiIsIkNDQ0FWX0lORElDQURPUkZPTkRPUyI6IjAzIiwiQ0NDQVZfVElQT0NBTUJJTyI6My4yMTcsIkNDQ0FWX0RFU0NSSVBDSU9OTkVHT0NJTyI6IkFMSUNPUlAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICIsIkNDQ0FWX0hPUkFPUEVSQUNJT04iOiIxMjM2NDgiLCJDQ0NBVl9JTlRFUkVTIjowLCJDQ0NBVl9OVU1FUk9TVUJBU1RBIjoiMDAiLCJDQ0NBVl9BR0VOVEVDT01QRVNQRUNJQUwiOiIwMCIsIkNDQ0FWX0FHRU5URVZFTkRFU1BFQ0lBTCI6IjAwIiwiQ0NDQVZfRkxBR09GRVJUQVBVQkxJQ0EiOiIwMCIsIkNDQ0FWX0NPUlJFTEFUSVZPQlZMIjoiMDAwMDAwMjAwMCIsIk1FTlNBSkVWQUxJREFDSU9OIjoiT0sifSx7IkNDQ0FWX0ZFQ0hBT1BFUkFDSU9OIjoiMjAxODA0MTAiLCJDQ0NBVl9NT0RBTElEQUQiOiIwMSIsIkNDQ0FWX05VTUVST09QRVJBQ0lPTiI6IjAwMjAwMSIsIkNDQ0FWX1RJUE9NT05FREEiOiIwMSIsIkNDQ0FWX1RJUE9PUEVSQUNJT04iOiIwMSIsIkNDQ0FWX1RJUE9JTlNUUlVNRU5UTyI6IjAyIiwiQ0NDQVZfRVNUQURPT1BFUkFDSU9OIjoiMDAiLCJDQ0NBVl9DT0RJR09WQUxPUiI6IlBFUDIxNDAwMTAwNSIsIkNDQ0FWX0NPRElHT1ZBTE9SMiI6IkFMSUNPUkMxICAiLCJDQ0NBVl9QUkVDSU9PUEVSQUNJT04iOjYuNzc3MiwiQ0NDQVZfQ0FOVElEQURPUEVSQUNJT04iOjE1MCwiQ0NDQVZfQUdFTlRFQ09NUFJBRE9SIjoiMDAwMSIsIkNDQ0FWX0FHRU5URVZFTkRFRE9SIjoiMDAwMSIsIkNDQ0FWX01PTlRPRUZFQ1RJVk8iOjEwMTYuNTgsIkNDQ0FWX1ZBTE9STk9NSU5BTCI6MC4wMDAwMDEsIkNDQ0FWX1RBU0FPUEVSQUNJT04iOjAsIkNDQ0FWX0ZFQ0hBTElRQ09OVEFETyI6IjIwMTgwNDEwIiwiQ0NDQVZfRkVDSEFMSVFQTEFaTyI6IjAwMDAwMDAwIiwiQ0NDQVZfQ0FOVElEQURESUFTT1BFIjoiMDAwMCIsIkNDQ0FWX1RJUE9BVkFMIjoiMDAiLCJDQ0NBVl9DT0RJR09BVkFMIjoiMDAwMCIsIkNDQ0FWX1RJUE9MSVFVSURBQ0lPTiI6IjAyIiwiQ0NDQVZfSU5ESUNBRE9SRk9ORE9TIjoiMDMiLCJDQ0NBVl9USVBPQ0FNQklPIjozLjIxNywiQ0NDQVZfREVTQ1JJUENJT05ORUdPQ0lPIjoiQUxJQ09SUCAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIiwiQ0NDQVZfSE9SQU9QRVJBQ0lPTiI6IjEyMzY0OCIsIkNDQ0FWX0lOVEVSRVMiOjAsIkNDQ0FWX05VTUVST1NVQkFTVEEiOiIwMCIsIkNDQ0FWX0FHRU5URUNPTVBFU1BFQ0lBTCI6IjAwIiwiQ0NDQVZfQUdFTlRFVkVOREVTUEVDSUFMIjoiMDAiLCJDQ0NBVl9GTEFHT0ZFUlRBUFVCTElDQSI6IjAwIiwiQ0NDQVZfQ09SUkVMQVRJVk9CVkwiOiIwMDAwMDAyMDAxIiwiTUVOU0FKRVZBTElEQUNJT04iOiJPSyJ9LHsiQ0NDQVZfRkVDSEFPUEVSQUNJT04iOiIyMDE4MDQxMCIsIkNDQ0FWX01PREFMSURBRCI6IjAxIiwiQ0NDQVZfTlVNRVJPT1BFUkFDSU9OIjoiMDAyMDAyIiwiQ0NDQVZfVElQT01PTkVEQSI6IjAxIiwiQ0NDQVZfVElQT09QRVJBQ0lPTiI6IjAxIiwiQ0NDQVZfVElQT0lOU1RSVU1FTlRPIjoiMDIiLCJDQ0NBVl9FU1RBRE9PUEVSQUNJT04iOiIwMCIsIkNDQ0FWX0NPRElHT1ZBTE9SIjoiUEVQMjE0MDAxMDA1IiwiQ0NDQVZfQ09ESUdPVkFMT1IyIjoiQUxJQ09SQzEgICIsIkNDQ0FWX1BSRUNJT09QRVJBQ0lPTiI6Ni43NzcyLCJDQ0NBVl9DQU5USURBRE9QRVJBQ0lPTiI6MjAwLCJDQ0NBVl9BR0VOVEVDT01QUkFET1IiOiIwMDAxIiwiQ0NDQVZfQUdFTlRFVkVOREVET1IiOiIwMDAxIiwiQ0NDQVZfTU9OVE9FRkVDVElWTyI6MTM1NS40NCwiQ0NDQVZfVkFMT1JOT01JTkFMIjowLjAwMDAwMSwiQ0NDQVZfVEFTQU9QRVJBQ0lPTiI6MCwiQ0NDQVZfRkVDSEFMSVFDT05UQURPIjoiMjAxODA0MTAiLCJDQ0NBVl9GRUNIQUxJUVBMQVpPIjoiMDAwMDAwMDAiLCJDQ0NBVl9DQU5USURBRERJQVNPUEUiOiIwMDAwIiwiQ0NDQVZfVElQT0FWQUwiOiIwMCIsIkNDQ0FWX0NPRElHT0FWQUwiOiIwMDAwIiwiQ0NDQVZfVElQT0xJUVVJREFDSU9OIjoiMDIiLCJDQ0NBVl9JTkRJQ0FET1JGT05ET1MiOiIwMyIsIkNDQ0FWX1RJUE9DQU1CSU8iOjMuMjE3LCJDQ0NBVl9ERVNDUklQQ0lPTk5FR09DSU8iOiJBTElDT1JQICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAiLCJDQ0NBVl9IT1JBT1BFUkFDSU9OIjoiMTIzNjQ4IiwiQ0NDQVZfSU5URVJFUyI6MCwiQ0NDQVZfTlVNRVJPU1VCQVNUQSI6IjAwIiwiQ0NDQVZfQUdFTlRFQ09NUEVTUEVDSUFMIjoiMDAiLCJDQ0NBVl9BR0VOVEVWRU5ERVNQRUNJQUwiOiIwMCIsIkNDQ0FWX0ZMQUdPRkVSVEFQVUJMSUNBIjoiMDAiLCJDQ0NBVl9DT1JSRUxBVElWT0JWTCI6IjAwMDAwMDIwMDIiLCJNRU5TQUpFVkFMSURBQ0lPTiI6Ik9LIn1dfQ==";
            }else {
            	idEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
                idUsuario = Long.parseLong(this.getParam("IdUsuario"));
                modulo = this.getParam("IdModulo");
                tipoArchivo = this.getParam("TipoArchivo");
                nombreArchivo = this.getParam("NombreArchivo");
                contenidoArchivo = this.getParam("ContenidoArchivo");
                System.out.print(this.getParam("ContenidoArchivo"));
            }
            
            HashMap<String,Object> params = new HashMap<>();
			params.put("IdEmpresa", idEmpresa);
			Long errores = 0L;
			Long tolerancia = 0L;
			List<Configuracion> configuracion = sql.query(con, SQL_SELECT_TOLERANCIA_ERRORES, params, Configuracion.class);
			if(configuracion.size() > 0 && configuracion.get(0).Valor != null){
				try{
					tolerancia = Long.parseLong(configuracion.get(0).Valor);
				}catch(Exception e){
					tolerancia = 0L;
				}
			}
            if (tipoArchivo.equalsIgnoreCase("CARGA_TRANSACCION_ROIX")){
            	CargaArchivoTransaccionROIX cargaArchivoTransaccionROIX = new CargaArchivoTransaccionROIX();
            	CargaArchivoTransaccionROIX.InCargaArchivoTransaccionROIX inCargaArchivoTransaccionROIX = new CargaArchivoTransaccionROIX.InCargaArchivoTransaccionROIX();
            	inCargaArchivoTransaccionROIX.p_IdEmpresa = idEmpresa;
            	inCargaArchivoTransaccionROIX.p_IdModulo = modulo;
            	inCargaArchivoTransaccionROIX.p_IdUsuario = idUsuario;
            	inCargaArchivoTransaccionROIX.p_Archivo = contenidoArchivo;
            	inCargaArchivoTransaccionROIX.p_NombreArchivo = nombreArchivo;
            	inCargaArchivoTransaccionROIX.p_Ruta = Utils.getConfiguracion(con, Utils.getSchema(), idEmpresa, "PLA.RutaBaseArchivos");
            	CargaArchivoTransaccionROIX.OutCargaArchivoTransaccionROIX out = cargaArchivoTransaccionROIX.logica(con, inCargaArchivoTransaccionROIX);
            	rutaLog = out.p_out_NombreArchivoLog;
            	errores = out.p_out_ErroresContador;
            } else if (tipoArchivo.equalsIgnoreCase("CARGA_TRANSACCIONES_TOMS")){
            	CargaArchivoTransaccionTOMS cargaArchivoTransaccionTOMS = new CargaArchivoTransaccionTOMS();
            	CargaArchivoTransaccionTOMS.InCargaArchivoTransaccionTOMS inCargaArchivoTransaccionTOMS = new CargaArchivoTransaccionTOMS.InCargaArchivoTransaccionTOMS();
            	inCargaArchivoTransaccionTOMS.p_IdEmpresa = idEmpresa;
            	inCargaArchivoTransaccionTOMS.p_IdModulo = modulo;
            	inCargaArchivoTransaccionTOMS.p_IdUsuario = idUsuario;
            	inCargaArchivoTransaccionTOMS.p_Archivo = contenidoArchivo;
            	
            	inCargaArchivoTransaccionTOMS.p_Ruta = Utils.getConfiguracion(con, Utils.getSchema(), idEmpresa, "PLA.RutaBaseArchivos");
            	CargaArchivoTransaccionTOMS.OutCargaArchivoTransaccionTOMS out = cargaArchivoTransaccionTOMS.logica(con, inCargaArchivoTransaccionTOMS);
            	rutaLog = out.p_out_NombreArchivoLog;
            	errores = out.p_out_ErroresContador;
            } else if (tipoArchivo.equalsIgnoreCase("CARGA_ORDEN_RV")){
            	CargaArchivoOrdenesRV cargaArchivoOrdenesRV = new CargaArchivoOrdenesRV();
            	CargaArchivoOrdenesRV.InCargarArchivoOrdenesRV inCargaArchivoOrdenesRV = new CargaArchivoOrdenesRV.InCargarArchivoOrdenesRV();
            	inCargaArchivoOrdenesRV.p_IdEmpresa = idEmpresa;
            	inCargaArchivoOrdenesRV.p_IdModulo = modulo;
            	inCargaArchivoOrdenesRV.p_IdUsuario = idUsuario;
            	inCargaArchivoOrdenesRV.p_Archivo = contenidoArchivo;
            	
            	inCargaArchivoOrdenesRV.p_Ruta = Utils.getConfiguracion(con, Utils.getSchema(), idEmpresa, "PLA.RutaBaseArchivos");
            	CargaArchivoOrdenesRV.OutCargarArchivoOrdenesRV out = cargaArchivoOrdenesRV.logica(con, inCargaArchivoOrdenesRV);
            	rutaLog = out.p_out_NombreArchivoLog;
            	errores = out.p_out_ErroresContador;
            } else if (tipoArchivo.equalsIgnoreCase("CARGA_ORDEN_RF")){
            	CargaArchivoOrdenesRF cargaArchivoOrdenesRF = new CargaArchivoOrdenesRF();
            	CargaArchivoOrdenesRF.InCargarArchivoOrdenesRF inCargaArchivoOrdenesRF = new CargaArchivoOrdenesRF.InCargarArchivoOrdenesRF();
            	
            	inCargaArchivoOrdenesRF.p_IdEmpresa = idEmpresa;
            	inCargaArchivoOrdenesRF.p_IdModulo = modulo;
            	inCargaArchivoOrdenesRF.p_IdUsuario = idUsuario;
            	inCargaArchivoOrdenesRF.p_Archivo = contenidoArchivo;
            	
            	inCargaArchivoOrdenesRF.p_Ruta = Utils.getConfiguracion(con, Utils.getSchema(), idEmpresa, "PLA.RutaBaseArchivos");
            	CargaArchivoOrdenesRF.OutCargarArchivoOrdenesRF out = cargaArchivoOrdenesRF.logica(con, inCargaArchivoOrdenesRF);
            	rutaLog = out.p_out_NombreArchivoLog;
            	errores = out.p_out_ErroresContador;
            } else if (tipoArchivo.equalsIgnoreCase("CARGA_ASIGNACIONES_RV")){
            	CargaArchivoAsignacionRV cargaArchivoAsignacionRV = new CargaArchivoAsignacionRV();
            	CargaArchivoAsignacionRV.InCargaArchivoAsignacionRV inCargaArchivoAsignacionRV = new CargaArchivoAsignacionRV.InCargaArchivoAsignacionRV();
            	
            	inCargaArchivoAsignacionRV.p_IdEmpresa = idEmpresa;
            	inCargaArchivoAsignacionRV.p_IdModulo = modulo;
            	inCargaArchivoAsignacionRV.p_IdUsuario = idUsuario;
            	inCargaArchivoAsignacionRV.p_Archivo = contenidoArchivo;
            	
            	inCargaArchivoAsignacionRV.p_Ruta = Utils.getConfiguracion(con, Utils.getSchema(), idEmpresa, "PLA.RutaBaseArchivos");
            	CargaArchivoAsignacionRV.OutCargaArchivoAsignacionRV out = cargaArchivoAsignacionRV.logica(con, inCargaArchivoAsignacionRV);
            	rutaLog = out.p_out_NombreArchivoLog;
            	errores = out.p_out_ErroresContador;
            } else if (tipoArchivo.equalsIgnoreCase("CARGA_ASIGNACIONES_RF")){
            	CargaArchivoAsignacionRF cargaArchivoAsignacionRF = new CargaArchivoAsignacionRF();
            	CargaArchivoAsignacionRF.InCargaArchivoAsignacionRF inCargaArchivoAsignacionRF = new CargaArchivoAsignacionRF.InCargaArchivoAsignacionRF();
            	
            	inCargaArchivoAsignacionRF.p_IdEmpresa = idEmpresa;
            	inCargaArchivoAsignacionRF.p_IdModulo = modulo;
            	inCargaArchivoAsignacionRF.p_IdUsuario = idUsuario;
            	inCargaArchivoAsignacionRF.p_Archivo = contenidoArchivo;
            	
            	inCargaArchivoAsignacionRF.p_Ruta = Utils.getConfiguracion(con, Utils.getSchema(), idEmpresa, "PLA.RutaBaseArchivos");
            	CargaArchivoAsignacionRF.OutCargaArchivoAsignacionRF out = cargaArchivoAsignacionRF.logica(con, inCargaArchivoAsignacionRF);
            	rutaLog = out.p_out_NombreArchivoLog;
            	errores = out.p_out_ErroresContador;
            } else if (tipoArchivo.equalsIgnoreCase("CARGA_TRANSACCION_BVL")){
            	CargaArchivoTransaccionBVL cargaArchivoTransaccionBVL = new CargaArchivoTransaccionBVL();
            	CargaArchivoTransaccionBVL.InCargaArchivoTransaccionBVL inCargaArchivoTransaccionBVL = new CargaArchivoTransaccionBVL.InCargaArchivoTransaccionBVL();
            	
            	inCargaArchivoTransaccionBVL.p_IdEmpresa = idEmpresa;
            	inCargaArchivoTransaccionBVL.p_IdModulo = modulo;
            	inCargaArchivoTransaccionBVL.p_IdUsuario = idUsuario;
            	inCargaArchivoTransaccionBVL.p_Archivo = contenidoArchivo;
            	
            	inCargaArchivoTransaccionBVL.p_Ruta = Utils.getConfiguracion(con, Utils.getSchema(), idEmpresa, "PLA.RutaBaseArchivos");
            	CargaArchivoTransaccionBVL.OutCargaArchivoTransaccionBVL out = cargaArchivoTransaccionBVL.logica(con, inCargaArchivoTransaccionBVL);
            	rutaLog = out.p_out_NombreArchivoLog;
            	errores = out.p_out_ErroresContador;
            } else if (tipoArchivo.equalsIgnoreCase("CARGA_TESORERIA_IGK")){
            	CargaArchivoIGK cargaArchivoIGK = new CargaArchivoIGK();
            	CargaArchivoIGK.InCargaArchivoIGK inCargaArchivoIGK = new CargaArchivoIGK.InCargaArchivoIGK();
            	
            	inCargaArchivoIGK.p_IdEmpresa = idEmpresa;
            	inCargaArchivoIGK.p_IdModulo = modulo;
            	inCargaArchivoIGK.p_IdUsuario = idUsuario;
            	inCargaArchivoIGK.p_Archivo = contenidoArchivo;
            	
            	inCargaArchivoIGK.p_Ruta = Utils.getConfiguracion(con, Utils.getSchema(), idEmpresa, "PLA.RutaBaseArchivos");
            	CargaArchivoIGK.OutCargaArchivoIGK out = cargaArchivoIGK.logica(con, inCargaArchivoIGK);
            	rutaLog = out.p_out_NombreArchivoLog;
            	errores = out.p_out_ErroresContador;
            } else if (tipoArchivo.equalsIgnoreCase("CUADRATURA_CAVALI")){
            	CargaArchivoCuadraturaCavali cargaArchivoCuadraturaCavali = new CargaArchivoCuadraturaCavali();
            	CargaArchivoCuadraturaCavali.InCargaArchivoCuadraturaCavali inCargaArchivoCuadraturaCavali = new CargaArchivoCuadraturaCavali.InCargaArchivoCuadraturaCavali();
            	
            	inCargaArchivoCuadraturaCavali.p_IdEmpresa = idEmpresa;
            	inCargaArchivoCuadraturaCavali.p_IdModulo = modulo;
            	inCargaArchivoCuadraturaCavali.p_IdUsuario = idUsuario;
            	inCargaArchivoCuadraturaCavali.p_Archivo = contenidoArchivo;
            	inCargaArchivoCuadraturaCavali.p_Ruta = Utils.getConfiguracion(con, Utils.getSchema(), idEmpresa, "PLA.RutaBaseArchivos");
            	
            	CargaArchivoCuadraturaCavali.OutCargaArchivoCuadraturaCavali out = cargaArchivoCuadraturaCavali.logica(con, inCargaArchivoCuadraturaCavali);
            	rutaLog = out.p_out_NombreArchivoLog;
            	errores = out.p_out_ErroresContador;
            } else if (tipoArchivo.equalsIgnoreCase("CARGA_INSTRUMENTO_PRE-BURSATIL")){
            	CargaArchivoPresenciaBursatil cargaArchivoPresenciaBursatil = new CargaArchivoPresenciaBursatil();
            	CargaArchivoPresenciaBursatil.InCargaArchivoPresenciaBursatil inCargaArchivoPresenciaBursatil = new CargaArchivoPresenciaBursatil.InCargaArchivoPresenciaBursatil();
            	
            	inCargaArchivoPresenciaBursatil.p_IdEmpresa = idEmpresa;
            	inCargaArchivoPresenciaBursatil.p_IdModulo = modulo;
            	inCargaArchivoPresenciaBursatil.p_IdUsuario = idUsuario;
            	inCargaArchivoPresenciaBursatil.p_Archivo = contenidoArchivo;
            	inCargaArchivoPresenciaBursatil.p_Ruta = Utils.getConfiguracion(con, Utils.getSchema(), idEmpresa, "PLA.RutaBaseArchivos");
            	
            	CargaArchivoPresenciaBursatil.OutCargaArchivoPresenciaBursatil out = cargaArchivoPresenciaBursatil.logica(con, inCargaArchivoPresenciaBursatil);
            	rutaLog = out.p_out_NombreArchivoLog;
            	errores = out.p_out_ErroresContador;
            } else {
            	log.write("El formato " + tipoArchivo + " no se encuentra configurado");
            	throw new Throwable(new Exception( "*CargarArchivoOrdenesTransacciones* Error al cargar archivos de ordenes y transacciones, formato " + tipoArchivo + " no se encuentra configurado"));
            }
            
			if(errores.longValue() > tolerancia.longValue()){
				con.rollback();
			}
			
			List<ActualizarPrograma> actualizar = new ArrayList<ActualizarPrograma>();
			actualizar.add(new ActualizarPrograma("",rutaLog,this.getJobId()));
			sql.batch(con,SQL_UPDATE_ID_PROGRAMA , actualizar,ActualizarPrograma.class);
			con.commit();
            return true;
		} catch (Exception e) {
			log.write(e);
			con.rollback();
			throw new Throwable(new Exception( "*CargarArchivoOrdenesTransacciones* Error al cargar archivos de ordenes y transacciones"));
		}
    }

    @Override
    public void onError(Throwable t) {
    	log.write(t.getMessage());
        log.write("****** ERROR TAREA  *******");
        t.printStackTrace();
    }

	@Override
	public void pause() {
	}
	
	public static void main(String[] args) {
        try {
            Connection con = PlataformaUtils.getQAPEConnection();
            try {
            	CargarArchivoOrdenesTransacciones demo=new CargarArchivoOrdenesTransacciones();
            	demo.logica(con,true);
                con.rollback();
            } catch (Throwable er) {
                er.printStackTrace();
                con.rollback();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("== END ==");       
    }

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		return null;
	}
        

    	
	
}