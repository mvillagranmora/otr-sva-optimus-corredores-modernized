package cl.bcs.mesa.SPT;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;

public class CuadraturaCierreDiario implements JavaCall{
	
	private final Log log = new Log("SPT.CuadraturaCierreDiario");
	
	private final String SQL_SELECT_CUADRATURADIARIAOPERACIONES		 	= loadResourceToString("cl/bcs/mesa/SPT/sql/CuadraturaCierreDiario_SelectCuadraturaDiariaOperaciones.sql");
	private final String SQL_SELECT_OPERACIONES_PENDIENTES 				= loadResourceToString("cl/bcs/mesa/SPT/sql/CuadraturaCierreDiario_SelectOperacionesPendientes.sql");
	private final String SQL_SELECT_OPERACIONES_PENDIENTES_CONFIRMAR 	= loadResourceToString("cl/bcs/mesa/SPT/sql/CuadraturaCierreDiario_SelectOperacionesPendientesConfirmar.sql");
	private final String SQL_SELECT_OPERACIONES_PENDIENTES_FACTURAR 	= loadResourceToString("cl/bcs/mesa/SPT/sql/CuadraturaCierreDiario_SelectOperacionesPendientesFacturacion.sql");
	private final String SQL_SELECT_OPERACIONES_PENDIENTES_LIQUIDAR 	= loadResourceToString("cl/bcs/mesa/SPT/sql/CuadraturaCierreDiario_SelectOperacionesPendientesLiquidacion.sql");
	private final String SQL_SELECT_OPERACIONES_DISPONIBLES_CONTABILIDAD= loadResourceToString("cl/bcs/mesa/SPT/sql/CuadraturaCierreDiario_SelectOperacionesDisponiblesContabilidad.sql");
	private final String SQL_SELECT_INTERFAZ_OMA						= loadResourceToString("cl/bcs/mesa/SPT/sql/CuadraturaCierreDiario_SelectInterfazOMA.sql");
	private final String SQL_DELETE_CUADRATURADIARIAOPERACIONES 		= loadResourceToString("cl/bcs/mesa/SPT/sql/CuadraturaCierreDiario_DeleteCuadraturaDiariaOperaciones.sql");
	private final String SQL_INSERT_CUADRATURADIARIAOPERACIONES 		= loadResourceToString("cl/bcs/mesa/SPT/sql/CuadraturaCierreDiario_InsertCuadraturaDiariaOperaciones.sql");
	private final String SQL_SELECT_DETALLE_CUADRATURADIARIAOPERACIONES = loadResourceToString("cl/bcs/mesa/SPT/sql/CuadraturaCierreDiario_SelectJSONCuadraturaDiariaOperaciones.sql");
	
	
	public static class CuadraturaDiariaObj{
		public String IdCuadraturaDiariaOperaciones;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CuadraturaCierreDiarioEntrada {
		public Long p_IdUsuario;
		public Long p_IdEmpresa;
		public String p_IdModulo;
		public Date p_FechaInicio;
		public List<CuadraturaDiariaObj> p_IdProceso;
		public String p_TipoLlamado;
		
		public CuadraturaCierreDiarioEntrada() {
			this.p_IdProceso = new ArrayList<CuadraturaDiariaObj>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class DetalleResumenJSON{
		public String IdCuadraturaDiariaOperaciones;
		public String DetalleCuadraturaJSON;
		
		public String toString() {
			return Utils.printJson(this, false);
		}		
	}
	
	public static class Resumen{
		public String IdCuadraturaDiariaOperaciones;
		public Long IdEmpresa;
		public String IdClasificacionTipoInstrumento;
		public String OperacionCuadraturaDiariaOperaciones;
		public Date FechaEjecucion;
		public Long TotalOperaciones;
		public Long OperacionesConProblemas;
		public String TipoOperacion;
		public String Resultado;
		public Long Orden;
		public String DetalleCuadraturaJSON;
		public Long IdUsuario;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Operaciones{
		public Long IdOperacion; 
		public Integer FolioOperacion;     
		public String RazonSocial;     
		public BigDecimal MontoOperacion;
		public BigDecimal MontoOperacionMoneda;
		public String IdMoneda;
		public String IdMonedaLiquidacion;
		public Long IdMesaOrigen;
		public String IdEstado;
		public String Descripcion;
		public String IdEstadoFacturacion;
		//CUAMOVDISPCONT
		public BigDecimal MontoMonedaPrincipalEquivalente;
		public BigDecimal MontoEquivalente;
		
		public Operaciones() {
			this.MontoMonedaPrincipalEquivalente = BigDecimal.ZERO;
			this.MontoEquivalente = BigDecimal.ZERO;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class objCuadraturaDiariaOperaciones{
		public Long IdEmpresa; 
		public String IdCuadraturaDiariaOperaciones;
		public Date FechaEjecucion;
		public Long TotalOperaciones; 
		public Long OperacionesConProblemas;
		public String Resultado;
		public String DetalleCuadraturaJson;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CuadraturaCierreDiarioSalida {
		public List<MensajeSalida>			P_OUT_MENSAJES;
		public Long							P_OUT_ERRORESCONTADOR;
		public String						P_OUT_DETALLE_CUADRATURA;
		
		public CuadraturaCierreDiarioSalida() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	/*
	 * Objeto de transporte
	 */
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		CuadraturaCierreDiarioEntrada in = gson.fromJson(jin.toString(), CuadraturaCierreDiarioEntrada.class);
		CuadraturaCierreDiarioSalida out = logica(con, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public CuadraturaCierreDiarioSalida logica(Connection conn, CuadraturaCierreDiarioEntrada in) throws Exception {
		SQL _sql = new SQL();
		CuadraturaCierreDiarioSalida out = new CuadraturaCierreDiarioSalida();
		out.P_OUT_ERRORESCONTADOR = 0L;
		log.write("INICIO");
		
		try {
			//Modo Proceso
			if (in.p_TipoLlamado.equals("Proceso")){
				Long Id = 0L;
				HashMap<String,String> mapMsg = new HashMap<>();
				List<String> idOper = new ArrayList<>();
				for (CuadraturaDiariaObj obj: in.p_IdProceso){
					idOper.add(obj.IdCuadraturaDiariaOperaciones);
				}
				HashMap<String,Object> regResumen = new HashMap<>();
				regResumen.put("IdEmpresa", in.p_IdEmpresa);
				regResumen.put("IdCuadraturaDiariaOperaciones", idOper);
				List<Resumen> rsResumen = _sql.query(conn, SQL_SELECT_CUADRATURADIARIAOPERACIONES, regResumen, Resumen.class);
				
				List<Operaciones> certificacionesPendientes = new ArrayList<Operaciones>();
				List<Operaciones> ope = new ArrayList<Operaciones>();
				List<objCuadraturaDiariaOperaciones> parametrosparaBorrar = new ArrayList<>();

				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
				
				HashMap<String, Object> params = new HashMap<String, Object>();
				String vResultado = new String();
				objCuadraturaDiariaOperaciones oDelete = new objCuadraturaDiariaOperaciones();
				for (CuadraturaDiariaObj objProc: in.p_IdProceso){
					switch (objProc.IdCuadraturaDiariaOperaciones) {
						case "CUAMOVPEN":
						case "CUAMOVPENCON":
						case "CUAMOVCOMFAC":
						case "CUAMOVPENLIQ":
							params.clear();
							params.put("IdEmpresa", in.p_IdEmpresa);
							params.put("Fecha", in.p_FechaInicio);
							if("CUAMOVPEN".equalsIgnoreCase(objProc.IdCuadraturaDiariaOperaciones)) ope = _sql.query(conn, SQL_SELECT_OPERACIONES_PENDIENTES, params, Operaciones.class);
							if("CUAMOVPENCON".equalsIgnoreCase(objProc.IdCuadraturaDiariaOperaciones)) ope = _sql.query(conn, SQL_SELECT_OPERACIONES_PENDIENTES_CONFIRMAR, params, Operaciones.class);
							if("CUAMOVCOMFAC".equalsIgnoreCase(objProc.IdCuadraturaDiariaOperaciones)) ope = _sql.query(conn, SQL_SELECT_OPERACIONES_PENDIENTES_FACTURAR, params, Operaciones.class);
							if("CUAMOVPENLIQ".equalsIgnoreCase(objProc.IdCuadraturaDiariaOperaciones)) ope = _sql.query(conn, SQL_SELECT_OPERACIONES_PENDIENTES_LIQUIDAR, params, Operaciones.class);
							vResultado = new String();
							
							if("CUAMOVPENLIQ".equalsIgnoreCase(objProc.IdCuadraturaDiariaOperaciones)) {
								certificacionesPendientes = buscarDescuadresTesoreria(ope);
							}
							else {
								certificacionesPendientes = buscarDescuadresCertificacion(ope);
							}
							
							if (ope.size()==0){
								vResultado = "UI_SIN_OPERACIONES";
							}
							else if(certificacionesPendientes.size() == 0){
								vResultado = "UI_CUADRADO";
							}else {
								vResultado = "UI_DESCUADRADO";
							}
														
							for (Resumen recoResumen: rsResumen){
								if (recoResumen.IdCuadraturaDiariaOperaciones.equals(objProc.IdCuadraturaDiariaOperaciones)){
									recoResumen.FechaEjecucion = in.p_FechaInicio;
									recoResumen.TotalOperaciones = (long)ope.size(); 
									recoResumen.OperacionesConProblemas = (long)certificacionesPendientes.size();
									recoResumen.Resultado = vResultado;
									recoResumen.DetalleCuadraturaJSON = gson.toJson(certificacionesPendientes);
									recoResumen.IdUsuario = in.p_IdUsuario;
								}
							}
							
							oDelete = new objCuadraturaDiariaOperaciones();
							oDelete.IdCuadraturaDiariaOperaciones = objProc.IdCuadraturaDiariaOperaciones;
							
							parametrosparaBorrar.add(oDelete);
							
							break;
						case "CUAMOVDISPCONT":
							params.clear();
							params.put("IdEmpresa", in.p_IdEmpresa);
							params.put("Fecha", in.p_FechaInicio);
							ope = _sql.query(conn, SQL_SELECT_OPERACIONES_DISPONIBLES_CONTABILIDAD, params, Operaciones.class);
							vResultado = new String();
							
							certificacionesPendientes = buscarDescuadresDisponiblesContabilidad(ope);
							
							if (ope.size()==0){
								vResultado = "UI_SIN_OPERACIONES";
							}
							else if(certificacionesPendientes.size() == 0){
								vResultado = "UI_CUADRADO";
							}else {
								vResultado = "UI_DESCUADRADO";
							}
														
							for (Resumen recoResumen: rsResumen){
								if (recoResumen.IdCuadraturaDiariaOperaciones.equals(objProc.IdCuadraturaDiariaOperaciones)){
									recoResumen.FechaEjecucion = in.p_FechaInicio;
									recoResumen.TotalOperaciones = (long)ope.size(); 
									recoResumen.OperacionesConProblemas = (long)certificacionesPendientes.size();
									recoResumen.Resultado = vResultado;
									recoResumen.DetalleCuadraturaJSON = gson.toJson(certificacionesPendientes);
									recoResumen.IdUsuario = in.p_IdUsuario;
								}
							}
							
							oDelete = new objCuadraturaDiariaOperaciones();
							oDelete.IdCuadraturaDiariaOperaciones = objProc.IdCuadraturaDiariaOperaciones;
							
							parametrosparaBorrar.add(oDelete);
							break;
						case "CUAINTOMA":
							params.clear();
							params.put("IdEmpresa", in.p_IdEmpresa);
							params.put("Fecha", in.p_FechaInicio);
							ope = _sql.query(conn, SQL_SELECT_INTERFAZ_OMA, params, Operaciones.class);
							vResultado = new String();
							
							if (ope.size()==0){
								vResultado = "UI_PROCESO_NO_EJECUTADO";
							}
							else if(ope.size() > 0){
								vResultado = "UI_PROCESO_EJECUTADO";
							}else {
								vResultado = "UI_PROCESO_NO_EJECUTADO";
							}
														
							for (Resumen recoResumen: rsResumen){
								if (recoResumen.IdCuadraturaDiariaOperaciones.equals(objProc.IdCuadraturaDiariaOperaciones)){
									recoResumen.FechaEjecucion = in.p_FechaInicio;
									recoResumen.TotalOperaciones = (long)ope.size() > 0 ? 1L : 0L; 
									recoResumen.OperacionesConProblemas = 0L;
									recoResumen.Resultado = vResultado;
									recoResumen.DetalleCuadraturaJSON = "";
									recoResumen.IdUsuario = in.p_IdUsuario;
								}
							}
							
							oDelete = new objCuadraturaDiariaOperaciones();
							oDelete.IdCuadraturaDiariaOperaciones = objProc.IdCuadraturaDiariaOperaciones;
							
							parametrosparaBorrar.add(oDelete);
						default:
							break;
					}
				}
				if (parametrosparaBorrar.size() > 0){ 
					_sql.batch(conn, SQL_DELETE_CUADRATURADIARIAOPERACIONES, parametrosparaBorrar, objCuadraturaDiariaOperaciones.class);
					_sql.batch(conn, SQL_INSERT_CUADRATURADIARIAOPERACIONES, rsResumen, Resumen.class);
				}
				
				out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_OK,"UI_PROCESO_EJECUTADO_EXITO",mapMsg,conn,in.p_IdEmpresa));
				AgregarLog(conn, in);
				
			}else if (in.p_TipoLlamado.equals("Consulta")){
				//Modo consulta
				List<String> idOper = new ArrayList<>();
				for (CuadraturaDiariaObj obj: in.p_IdProceso){
					idOper.add(obj.IdCuadraturaDiariaOperaciones);
				}
				
				HashMap<String, Object> params = new HashMap<>();
				params.put("IdEmpresa", in.p_IdEmpresa);
				params.put("IdCuadraturaDiariaOperaciones", idOper);
				List<DetalleResumenJSON> rsJson = _sql.query(conn, SQL_SELECT_DETALLE_CUADRATURADIARIAOPERACIONES, params, DetalleResumenJSON.class);
				
				if (rsJson.size() != 0) {
					out.P_OUT_DETALLE_CUADRATURA = (rsJson).get(0).DetalleCuadraturaJSON;					
				}
			}
		}catch(Exception e){
			out.P_OUT_ERRORESCONTADOR = 1L;
			log.write(e);
			throw e;
		}
		
		return out;
	}
	
	private List<Operaciones> buscarDescuadresCertificacion(List<Operaciones> ope){
		List<Operaciones> descuadres = new ArrayList<Operaciones>();
		for(Operaciones operacion : ope){
			if("PEN".equalsIgnoreCase(operacion.IdEstado) || "PAU".equalsIgnoreCase(operacion.IdEstado) || "ING".equalsIgnoreCase(operacion.IdEstadoFacturacion) || ("ING".equalsIgnoreCase(operacion.IdEstado) && "FAC".equalsIgnoreCase(operacion.IdEstadoFacturacion))){
				operacion.Descripcion = "UI_PENDIENTE";
				descuadres.add(operacion);
			}
		}
		return descuadres;
	}
	
	private List<Operaciones> buscarDescuadresTesoreria(List<Operaciones> ope){
		List<Operaciones> descuadres = new ArrayList<Operaciones>();
		for(Operaciones operacion : ope){
			if("ING".equalsIgnoreCase(operacion.IdEstado) && "FAC".equalsIgnoreCase(operacion.IdEstadoFacturacion)){
				operacion.Descripcion = "UI_PENDIENTE";
				descuadres.add(operacion);
			}
		}
		return descuadres;
	}
	
	private List<Operaciones> buscarDescuadresDisponiblesContabilidad(List<Operaciones> ope){
		List<Operaciones> descuadres = new ArrayList<Operaciones>();
		for(Operaciones operacion : ope){
			if(operacion.MontoOperacion.compareTo(BigDecimal.ZERO) == 0 
					|| operacion.MontoOperacionMoneda.compareTo(BigDecimal.ZERO) == 0
					|| operacion.MontoMonedaPrincipalEquivalente.compareTo(BigDecimal.ZERO) == 0
					|| operacion.MontoEquivalente.compareTo(BigDecimal.ZERO) == 0){
				operacion.Descripcion = "UI_PENDIENTE";
				descuadres.add(operacion);
			}
		}
		return descuadres;
	}
	
	public void AgregarLog(Connection con, CuadraturaCierreDiarioEntrada in) {
		try {
			String FechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
			Long IdCabeceraLog =  in.p_IdUsuario;		
			//CABECERA
			AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
			AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
				
			log.IdCabeceraLog = IdCabeceraLog;
			log.IdModulo = "MSP";
			log.IdUsuario = in.p_IdUsuario;
			log.IdEmpresa = in.p_IdEmpresa;
			log.FechaSistemaLog = FechaSistema;
			log.IdUsuarioAtendido = 0L;
			log.IdCliente = 0L;
			log.EstadoCliente = "";
			log.NombreCliente = "";
			log.IdTipoDocumentoIdentidad = "";
			log.IdDocumentoIdentidad = "";
			log.IdPortafolio = 0L;
			log.EstadoPortafolio = "";
			log.IdTipoRegistro = "CUADRATURACIERREDIARIO";
			log.EstadoRegistro = "";
			log.IdTipoOperacion = "";
			log.Folio = 0L;
			log.Cantidad = BigDecimal.ZERO;
			log.PrecioTasa = BigDecimal.ZERO;
			log.Monto = BigDecimal.ZERO;
			log.IdMonedaOrigen = "";
			log.IdMonedaLiquidacion = "";
			log.TipoCambio = "";
			log.Nemo = "";
			log.IdProceso = 0L;
			log.EstadoProceso = "ING";
			log.IdPersona = 0L;
			log.IdTipoLog = "INFORMATIVO";
			log.NombreProceso = "";
			//AÑADIR AL LOG
			entradaAgregarLog.p_Log.add(log);
			//REGISTRO
			AgregarLog agregarLogService = new AgregarLog();
			agregarLogService.logica(con, entradaAgregarLog);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			//String json = "{\"p_IdUsuario\":21,\"p_IdEmpresa\":1,\"p_IdModulo\":\"ORD\",\"p_FechaInicio\":\"2018-03-08\",\"p_IdProceso\":[{\"IdCuadraturaDiariaOperaciones\":\"CUAMOVCERT\"}],\"p_TipoLlamado\":\"Proceso\"}";
			String json = "{\"p_IdUsuario\":74252,\"p_IdEmpresa\":1,\"p_IdModulo\":\"MES\",\"p_FechaInicio\":\"2023-09-15\",\"p_IdProceso\":[{\"IdCuadraturaDiariaOperaciones\":\"CUAMOVDISPCONT\"}],\"p_TipoLlamado\":\"Proceso\"}";
			CuadraturaCierreDiarioEntrada Objin = gson.fromJson(json, CuadraturaCierreDiarioEntrada.class);
			
			CuadraturaCierreDiario demo=new CuadraturaCierreDiario();				
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			try {
 				CuadraturaCierreDiarioSalida ObjOut= demo.logica(con, Objin);
 				con.commit();
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				//er.printStackTrace();	
				con.rollback();
			}
 			//con.commit();
 			
	    	}
		 	
		System.out.println("== END ==");
	}
}
