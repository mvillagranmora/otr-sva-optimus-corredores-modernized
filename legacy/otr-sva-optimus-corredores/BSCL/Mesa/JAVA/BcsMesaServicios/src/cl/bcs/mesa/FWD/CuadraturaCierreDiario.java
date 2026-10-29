package cl.bcs.mesa.FWD;

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
	
	private final Log log = new Log("FWD.CuadraturaCierreDiario");
	
	private final String SQL_SELECT_CUADRATURADIARIAOPERACIONES		 		= loadResourceToString("cl/bcs/mesa/FWD/sql/CuadraturaCierreDiario_SelectCuadraturaDiariaOperaciones.sql");
	private final String SQL_SELECT_OPERACIONES_PENDIENTES 					= loadResourceToString("cl/bcs/mesa/FWD/sql/CuadraturaCierreDiario_SelectOperacionesPendientes.sql");
	private final String SQL_SELECT_OPERACIONES_MOD_COMPENSADO 				= loadResourceToString("cl/bcs/mesa/FWD/sql/CuadraturaCierreDiario_SelectOperacionesModCompensado.sql");
	private final String SQL_SELECT_OPERACIONES_MOD_ENTREGA_FISICA 			= loadResourceToString("cl/bcs/mesa/FWD/sql/CuadraturaCierreDiario_SelectOperacionesModEntregaFisica.sql");
	private final String SQL_SELECT_OPERACIONES_MOD_ENTREGA_FISICA_SIN_SPOT	= loadResourceToString("cl/bcs/mesa/FWD/sql/CuadraturaCierreDiario_SelectOperacionesModEntregaFisicaLiqSinSpot.sql");
	private final String SQL_SELECT_VERIFICAR_VALORIZACION_RAZONABLE		= loadResourceToString("cl/bcs/mesa/FWD/sql/CuadraturaCierreDiario_VerificarValorizacionRazonable.sql");
	private final String SQL_DELETE_CUADRATURADIARIAOPERACIONES 			= loadResourceToString("cl/bcs/mesa/FWD/sql/CuadraturaCierreDiario_DeleteCuadraturaDiariaOperaciones.sql");
	private final String SQL_INSERT_CUADRATURADIARIAOPERACIONES 			= loadResourceToString("cl/bcs/mesa/FWD/sql/CuadraturaCierreDiario_InsertCuadraturaDiariaOperaciones.sql");
	private final String SQL_SELECT_DETALLE_CUADRATURADIARIAOPERACIONES 	= loadResourceToString("cl/bcs/mesa/FWD/sql/CuadraturaCierreDiario_SelectJSONCuadraturaDiariaOperaciones.sql");
	private final String SQL_SELECT_INFORME_IX 								= loadResourceToString("cl/bcs/mesa/FWD/sql/CuadraturaCierreDiario_SelectInformeIX.sql");
	private final String SQL_SELECT_OPERECIONES_CUENTA_INVERSION 			= loadResourceToString("cl/bcs/mesa/FWD/sql/CuadraturaCierreDiario_SelectOperacionesCuentaInversion.sql");
	private final String SQL_SELECT_OPERACIONES_TESORERIA 					= loadResourceToString("cl/bcs/mesa/FWD/sql/CuadraturaCierreDiario_SelectOperacionesTesoreria.sql");
	private final String SQL_SELECT_DATOS_CENTRALIZACION					= loadResourceToString("cl/bcs/mesa/FWD/sql/CuadraturaCierreDiario_SelectDatosCentralizacion.sql");
	
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
		public Long FolioCartera;     
		public String RazonSocial;     
		public BigDecimal MontoMonedaPrincipal;
		public BigDecimal MontoMonedaSecundaria;
		public String IdMonedaPrincipal;
		public String IdMonedaSecundaria;
		public BigDecimal RazonableActivo;
		public BigDecimal RazonablePasivo;
		public String IdMesa;
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
	public static class Ejecucion{
		public Long Estado; 
		
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
				for (CuadraturaDiariaObj objProc: in.p_IdProceso){
					Long ejecucionProceso = -1L;
					certificacionesPendientes.clear();
					ope.clear();
					HashMap<String, Object> params = new HashMap<String, Object>();
					params.put("Fecha", in.p_FechaInicio);
					switch (objProc.IdCuadraturaDiariaOperaciones) {
						case "CUAMOVPEN": {
							params.put("IdEmpresa", in.p_IdEmpresa);
							ope = _sql.query(conn, SQL_SELECT_OPERACIONES_PENDIENTES, params, Operaciones.class);
							certificacionesPendientes = buscarDescuadresCertificacion(ope);
							break;
						}
						case "CUAMOVMODCON": {
							ope = _sql.query(conn, SQL_SELECT_OPERACIONES_MOD_COMPENSADO, params, Operaciones.class);
							certificacionesPendientes = buscarDescuadresModComp(ope);
							break;
						}
						case "CUAMOVMODFIS": {
							ope = _sql.query(conn, SQL_SELECT_OPERACIONES_MOD_ENTREGA_FISICA, params, Operaciones.class);
							certificacionesPendientes = buscarDescuadresEF(ope);
							break;
						}
						case "CUAMOVMODFISSPT": {
							ope = _sql.query(conn, SQL_SELECT_OPERACIONES_MOD_ENTREGA_FISICA_SIN_SPOT, params, Operaciones.class);
							certificacionesPendientes = buscarDescuadresEFSPT(ope);
							break;
						}
						case "CUAVERVALRAZ": {
							params.put("IdEmpresa", in.p_IdEmpresa);
							List<Ejecucion> ejecucion = _sql.query(conn, SQL_SELECT_VERIFICAR_VALORIZACION_RAZONABLE, params, Ejecucion.class);
							ejecucionProceso = ejecucion.get(0).Estado;
							break;
						}
						case "CUAINFIX": {
							params.put("IdEmpresa", in.p_IdEmpresa);
							List<Ejecucion> ejecucion = _sql.query(conn, SQL_SELECT_INFORME_IX, params, Ejecucion.class);
							ejecucionProceso = ejecucion.get(0).Estado;
							break;
						}
						case "CUAENVTESCTA": {
							ope = _sql.query(conn, SQL_SELECT_OPERECIONES_CUENTA_INVERSION, params, Operaciones.class);
							certificacionesPendientes = buscarDescuadresCTAInversion(ope);
							break;
						}
						case "CUAENVTESSINCTA": {
							ope = _sql.query(conn, SQL_SELECT_OPERACIONES_TESORERIA, params, Operaciones.class);
							certificacionesPendientes = buscarDescuadresCTAInversion(ope);
							break;
						}
						case "CUADATOSCEN": {
							params.clear();
							params.put("IdEmpresa", in.p_IdEmpresa);
							ope = _sql.query(conn, SQL_SELECT_DATOS_CENTRALIZACION, params, Operaciones.class);
							certificacionesPendientes = buscarDescuadresDatosCentralizacion(ope);
							break;
						}
						default:
							break;
					}
					String vResultado = new String();
					
					if(!"CUAVERVALRAZ".equalsIgnoreCase(objProc.IdCuadraturaDiariaOperaciones) && !"CUAINFIX".equals(objProc.IdCuadraturaDiariaOperaciones))
					{
						if (ope.size()==0){
							vResultado = "UI_SIN_OPERACIONES";
						}
						else if(certificacionesPendientes.size() == 0){
							vResultado = "UI_CUADRADO";
						}else {
							vResultado = "UI_DESCUADRADO";
						}
					}
					else
					{
						if(ejecucionProceso > 0)
							vResultado = "UI_PROCESO_EJECUTADO";
						else
							vResultado = "UI_PROCESO_SIN_EJECUTAR";
					}
												
					for (Resumen recoResumen: rsResumen){
						if (recoResumen.IdCuadraturaDiariaOperaciones.equals(objProc.IdCuadraturaDiariaOperaciones)){
							recoResumen.FechaEjecucion = in.p_FechaInicio;
							recoResumen.Resultado = vResultado;
							//Ejecucion proceso sirve solo para el proceso CUAVERVALRAZ
							
							recoResumen.TotalOperaciones = ejecucionProceso > 0 ? 1 :(long)ope.size(); 
							recoResumen.OperacionesConProblemas = ejecucionProceso == 0 ? 1 :(long)certificacionesPendientes.size();
							recoResumen.DetalleCuadraturaJSON = gson.toJson(certificacionesPendientes);
							recoResumen.IdUsuario = in.p_IdUsuario;
							
						}
					}
					
					objCuadraturaDiariaOperaciones oDelete = new objCuadraturaDiariaOperaciones();
					oDelete.IdCuadraturaDiariaOperaciones = objProc.IdCuadraturaDiariaOperaciones;
					
					parametrosparaBorrar.add(oDelete);
					
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
			if("PEN".equalsIgnoreCase(operacion.IdEstado)){
				operacion.Descripcion = "UI_PENDIENTE";
				descuadres.add(operacion);
			}
		}
		return descuadres;
	}
	
	private List<Operaciones> buscarDescuadresModComp(List<Operaciones> ope){
		List<Operaciones> descuadres = new ArrayList<Operaciones>();
		for(Operaciones operacion : ope){
			if("VIG".equalsIgnoreCase(operacion.IdEstado) || "ANT".equalsIgnoreCase(operacion.IdEstado) || "VCD".equalsIgnoreCase(operacion.IdEstado)){
				operacion.Descripcion = "UI_PENDIENTE";
				descuadres.add(operacion);
			}
		}
		return descuadres;
	}
	
	private List<Operaciones> buscarDescuadresEF(List<Operaciones> ope){
		List<Operaciones> descuadres = new ArrayList<Operaciones>();
		for(Operaciones operacion : ope){
			if("VIG".equalsIgnoreCase(operacion.IdEstado) || "ANT".equalsIgnoreCase(operacion.IdEstado)){
				operacion.Descripcion = "UI_PENDIENTE";
				descuadres.add(operacion);
			}
		}
		return descuadres;
	}
	
	private List<Operaciones> buscarDescuadresEFSPT(List<Operaciones> ope){
		List<Operaciones> descuadres = new ArrayList<Operaciones>();
		for(Operaciones operacion : ope){
			operacion.Descripcion = "UI_PENDIENTE";
			descuadres.add(operacion);
		}
		return descuadres;
	}
	
	private List<Operaciones> buscarDescuadresCTAInversion(List<Operaciones> ope) {
		List<Operaciones> descuadres = new ArrayList<Operaciones>();
		for(Operaciones operacion : ope){
			if(!"CRT".equalsIgnoreCase(operacion.IdEstado)){
				operacion.Descripcion = "UI_PENDIENTE";
				descuadres.add(operacion);
			}
		}
		return descuadres;
	}
	
	private List<Operaciones> buscarDescuadresDatosCentralizacion(List<Operaciones> ope) {
		List<Operaciones> descuadres = new ArrayList<Operaciones>();
		for(Operaciones operacion : ope){
			if(operacion.MontoMonedaPrincipal.compareTo(BigDecimal.ZERO) == 0 || operacion.MontoMonedaSecundaria.compareTo(BigDecimal.ZERO) == 0 || operacion.RazonableActivo.compareTo(BigDecimal.ZERO) == 0 || operacion.RazonablePasivo.compareTo(BigDecimal.ZERO) == 0){
				operacion.Descripcion = "UI_DESCUADRADO";
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
			log.IdModulo = "MFW";
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
			//String json = "{\"p_IdUsuario\":7,\"p_IdEmpresa\":1,\"p_IdModulo\":\"MES\",\"p_FechaInicio\":\"2019-05-13\",\"p_IdProceso\":[{\"IdCuadraturaDiariaOperaciones\":\"CUAENVTESSINCTA\"}],\"p_TipoLlamado\":\"Proceso\"}";
			String json ="{\"p_IdUsuario\":74252,\"p_IdEmpresa\":1,\"p_IdModulo\":\"MES\",\"p_FechaInicio\":\"2023-09-15\",\"p_IdProceso\":[{\"IdCuadraturaDiariaOperaciones\":\"CUADATOSCEN\"}],\"p_TipoLlamado\":\"Proceso\"}";
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
