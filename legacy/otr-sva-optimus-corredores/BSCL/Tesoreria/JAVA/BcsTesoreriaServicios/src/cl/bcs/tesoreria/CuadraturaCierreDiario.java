package cl.bcs.tesoreria;

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
	
	private final Log log = new Log("TES.GeneracionArchivoAGF");
	
	private final String SQL_SELECT_CUADRATURADIARIAOPERACIONES		 	= loadResourceToString("cl/bcs/tesoreria/sql/CuadraturaCierreDiario_SelectCuadraturaDiariaOperaciones.sql");
	private final String SQL_SELECT_FORMADEPAGO							= loadResourceToString("cl/bcs/tesoreria/sql/CuadraturaCierreDiario_SelectFormasDePago.sql");
	private final String SQL_SELECT_INGRESOEGRESO						= loadResourceToString("cl/bcs/tesoreria/sql/CuadraturaCierreDiario_SelectIngresoEgreso.sql");
	private final String SQL_DELETE_CUADRATURADIARIAOPERACIONES 		= loadResourceToString("cl/bcs/tesoreria/sql/CuadraturaCierreDiario_DeleteCuadraturaDiariaOperaciones.sql");
	private final String SQL_INSERT_CUADRATURADIARIAOPERACIONES 		= loadResourceToString("cl/bcs/tesoreria/sql/CuadraturaCierreDiario_InsertCuadraturaDiariaOperaciones.sql");
	private final String SQL_SELECT_DETALLE_CUADRATURADIARIAOPERACIONES = loadResourceToString("cl/bcs/tesoreria/sql/CuadraturaCierreDiario_SelectJSONCuadraturaDiariaOperaciones.sql");
	
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
	
	public static class FormaDePago{
		public Long   IdComprobante;
		public Long   IdComprobanteFormaDePago; 
		public Date   FechaLiquidacion;
		public String NombreConcepto;     
		public String NombreMedioDePago;     
		public String NombreTipoMovimiento;
		public String IdMoneda;
		public BigDecimal Monto;
		public String IdEstado;
		public String Descripcion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class IngresoEgreso{
		public Long   IdIngresoEgreso;
		public Date   FechaLiquidacion;
		public String NombreConcepto;     
		public String NombreMedioDePago;     
		public String NombreTipoMovimiento;
		public String IdMoneda;
		public BigDecimal Monto;
		public String IdEstado;
		public String Descripcion;
		
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
				
				
				//1. Busca el Registro de Cuadratura 
				HashMap<String,String> mapMsg = new HashMap<>();
				List<String> idOper = new ArrayList<>();
				for (CuadraturaDiariaObj obj: in.p_IdProceso){
					idOper.add(obj.IdCuadraturaDiariaOperaciones);
				}
				HashMap<String,Object> regResumen = new HashMap<>();
				regResumen.put("IdEmpresa", in.p_IdEmpresa);
				regResumen.put("IdCuadraturaDiariaOperaciones", idOper);
				List<Resumen> rsResumen = _sql.query(conn, SQL_SELECT_CUADRATURADIARIAOPERACIONES, regResumen, Resumen.class);
				
				List<FormaDePago> certificacionesPendientes = new ArrayList<FormaDePago>();
				List<FormaDePago> mov = new ArrayList<FormaDePago>();
				List<IngresoEgreso> comprobantesPendientes = new ArrayList<IngresoEgreso>();
				List<IngresoEgreso> movIE = new ArrayList<IngresoEgreso>();
				List<objCuadraturaDiariaOperaciones> parametrosparaBorrar = new ArrayList<>();

				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
				objCuadraturaDiariaOperaciones CeDelete = new objCuadraturaDiariaOperaciones();
				objCuadraturaDiariaOperaciones CoDelete = new objCuadraturaDiariaOperaciones();
				
				
				//Recorre y procesa cada proceso
				for (CuadraturaDiariaObj objProc: in.p_IdProceso){
					switch (objProc.IdCuadraturaDiariaOperaciones) {
						case "CUAMOVCERT":
							
							// Obtiene formas de pago a liquidarse hoy  
							HashMap<String, Object> params = new HashMap<>();
							params.put("FechaLiquidacion", in.p_FechaInicio);
							mov = _sql.query(conn, SQL_SELECT_FORMADEPAGO, params, FormaDePago.class);
							String vResultado = new String();
							
							// Revisa las formas de pago a liquidarse hoy y que no lo están
							certificacionesPendientes = buscarDescuadresCertificacion(mov);
							
							if (mov.size()==0){
								vResultado = "SIN OPERACIONES";
							}
							else if(certificacionesPendientes.size() == 0){
								vResultado = "CUADRADO";
							}else {
								vResultado = "DESCUADRADO";
							}
														
							for (Resumen recoResumen: rsResumen){
								if (recoResumen.IdCuadraturaDiariaOperaciones.equals(objProc.IdCuadraturaDiariaOperaciones)){
									recoResumen.FechaEjecucion = in.p_FechaInicio;
									recoResumen.TotalOperaciones = (long)mov.size(); 
									recoResumen.OperacionesConProblemas = (long)certificacionesPendientes.size();
									recoResumen.Resultado = vResultado;
									recoResumen.DetalleCuadraturaJSON = gson.toJson(certificacionesPendientes); // Detalle de Formas de Pago pendientes de certificar 
									recoResumen.IdUsuario = in.p_IdUsuario;
								}
							}
							
							CeDelete.IdCuadraturaDiariaOperaciones = objProc.IdCuadraturaDiariaOperaciones;
							parametrosparaBorrar.add(CeDelete);
							
							break;
						case "CUAMOVSINCOMP":
							
							// Obtiene Ingresos y Egresos a liquidarse hoy  
							HashMap<String, Object> paramsIE = new HashMap<>();
							paramsIE.put("FechaLiquidacion", in.p_FechaInicio);
							movIE = _sql.query(conn, SQL_SELECT_INGRESOEGRESO, paramsIE, IngresoEgreso.class);
							String vResultadoIE = new String();
							
							// Revisa las Ingresos y Egresos a liquidarse hoy y que están sin Comprobante
							comprobantesPendientes = buscarDescuadresNoEmitidos(movIE);
							
							if (movIE.size()==0){
								vResultadoIE = "SIN OPERACIONES";
							}
							else if(comprobantesPendientes.size() == 0){
								vResultadoIE = "CUADRADO";
							}else {
								vResultadoIE = "DESCUADRADO";
							}
							
							for (Resumen recoResumen: rsResumen){
								if (recoResumen.IdCuadraturaDiariaOperaciones.equals(objProc.IdCuadraturaDiariaOperaciones)){
									recoResumen.FechaEjecucion = in.p_FechaInicio;
									recoResumen.TotalOperaciones = (long)movIE.size(); 
									recoResumen.OperacionesConProblemas = (long)comprobantesPendientes.size();
									recoResumen.Resultado = vResultadoIE;
									recoResumen.DetalleCuadraturaJSON = gson.toJson(comprobantesPendientes); // Detalle de Ingreso y Egresos pendientes sin comprobante 
									recoResumen.IdUsuario = in.p_IdUsuario;
								}
							}
							
							CoDelete.IdCuadraturaDiariaOperaciones = objProc.IdCuadraturaDiariaOperaciones;
							parametrosparaBorrar.add(CoDelete);
							
							break;
						default:
							break;
					}
				}
				
				//Borra e inserta registro de Cuadratura diaria de operaciones
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
	
	private List<FormaDePago> buscarDescuadresCertificacion(List<FormaDePago> mov){
		List<FormaDePago> descuadres = new ArrayList<FormaDePago>();
		for(FormaDePago movimientos : mov){
			if("ING".equalsIgnoreCase(movimientos.IdEstado)){
				movimientos.Descripcion = "NO CERTIFICADO";
				descuadres.add(movimientos);
			}
		}
		return descuadres;
	}
	
	private List<IngresoEgreso> buscarDescuadresNoEmitidos(List<IngresoEgreso> movIE){
		List<IngresoEgreso> descuadres = new ArrayList<IngresoEgreso>();
		for(IngresoEgreso movimientos : movIE){
			if("ING".equalsIgnoreCase(movimientos.IdEstado)){
				movimientos.Descripcion = "SIN COMPROBANTE";
				descuadres.add(movimientos);
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
			log.IdModulo = "TES";
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
			//String json = "{\"p_IdUsuario\":6408,\"p_IdEmpresa\":1,\"p_IdModulo\":\"FMM\",\"p_FechaInicio\":\"2023-02-13\",\"p_IdProceso\":[{\"IdCuadraturaDiariaOperaciones\":\"CUAMOVSINCOMP\"},{\"IdCuadraturaDiariaOperaciones\":\"CUAMOVCERT\"}],\"p_TipoLlamado\":\"Proceso\"}";
			String json = "{\"p_IdUsuario\":74252,\"p_IdEmpresa\":1,\"p_IdModulo\":\"FMM\",\"p_FechaInicio\":\"2023-09-15\",\"p_IdProceso\":[{\"IdCuadraturaDiariaOperaciones\":\"CUAMOVSINCOMP\"}],\"p_TipoLlamado\":\"Proceso\"}";
			
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

