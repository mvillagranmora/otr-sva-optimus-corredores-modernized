package cl.bcs.contabilidad;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.contabilidad.CentralizaMultiSeleccion.EntradaCentralizaMultiSeleccion;
import cl.bcs.contabilidad.CentralizaMultiSeleccion.SalidaCentralizaMultiSeleccion;
import cl.bcs.contabilidad.pojo.ResultadoCentralizacion;
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
	private final Log log = new Log("CON.CuadraturaCierreDiario");
	
	private final String SQL_SELECT_CUADRATURADIARIAOPERACIONES		 	= loadResourceToString("cl/bcs/contabilidad/sql/CuadraturaCierreDiario_SelectCuadraturaDiariaOperaciones.sql");
	private final String SQL_SELECT_MOVIMIENTOS 						= loadResourceToString("cl/bcs/contabilidad/sql/CuadraturaCierreDiario_SelectContabilidad.sql");
	private final String SQL_SELECT_COMPROBANTES						= loadResourceToString("cl/bcs/contabilidad/sql/CuadraturaCierreDiario_SelectComprobantes.sql");
	private final String SQL_DELETE_CUADRATURADIARIAOPERACIONES 		= loadResourceToString("cl/bcs/contabilidad/sql/CuadraturaCierreDiario_DeleteCuadraturaDiariaOperaciones.sql");
	private final String SQL_INSERT_CUADRATURADIARIAOPERACIONES 		= loadResourceToString("cl/bcs/contabilidad/sql/CuadraturaCierreDiario_InsertCuadraturaDiariaOperaciones.sql");
	private final String SQL_SELECT_DETALLE_CUADRATURADIARIAOPERACIONES = loadResourceToString("cl/bcs/contabilidad/sql/CuadraturaCierreDiario_SelectJSONCuadraturaDiariaOperaciones.sql");
	
	
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
		public String p_FechaInicio;
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
		public Long IdTipoCentralizacion;
		public Long IdUsuario;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Movimientos{
		public Long IdTipoCentralizacion; 
		public Long IdMovimiento;
		public String IdTipoMovimiento; 
		public Long IdEmpresa;
		public Long Numero;     
		public Long Linea;     
		public String Fecha;
		public String DocumentoIdentidad;
		public Long IdPortafolio;
		public String Ejercicio;
		public String Periodo;
		public BigDecimal Monto;
		public String IdMoneda;
		public String Estado;
		public String Descripcion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Comprobantes{
		 public Long IdPreComprobante;
		 public String NombreEmpresa;
		 public Long Ejercicio;
		 public String FechaContable;
	     public String FechaProceso;
		 public String HoraProceso;
		 public Long FolioContabilidad;
		 public Long CantidadAsientos;
	     public String Estado;
		 public String NombreEstado;
		 public String Glosa;
		 public Long IdReversa;
		 public String Tipo;
		 public String CodigoUsuario;
		 public Long IdTipoContabilidad;
		 public String NombreTipoContabilidad;
		 public Long IdEmpresa;
		 public BigDecimal Debe;
		 public BigDecimal Haber;
		 public String IdMoneda;
		 public String IdEstadoPrograma;
		 public String Descripcion;
	}
	
	public static class objCuadraturaDiariaOperaciones{
		public Long IdEmpresa; 
		public String IdCuadraturaDiariaOperaciones;
		public String FechaEjecucion;
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
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
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
				
				List<CentralizaMultiSeleccion.InTiposCentralizacion> inTiposCentralizacion = new ArrayList<CentralizaMultiSeleccion.InTiposCentralizacion>();
				List<Long> centralizaciones = new ArrayList<Long>();
				for(Resumen resumen : rsResumen){
					if(resumen.IdTipoCentralizacion.longValue() != 0){
						centralizaciones.add(resumen.IdTipoCentralizacion);
						CentralizaMultiSeleccion.InTiposCentralizacion centralizacion = new CentralizaMultiSeleccion.InTiposCentralizacion();
						centralizacion.IdTipoCentralizacion  = resumen.IdTipoCentralizacion;
						inTiposCentralizacion.add(centralizacion);
					}
				}
				
				List<Movimientos> mov = new ArrayList<Movimientos>();
				List<objCuadraturaDiariaOperaciones> parametrosparaBorrar = new ArrayList<>();

				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
				
				HashMap<String, Object> params = new HashMap<>();
				List<ResultadoCentralizacion> resultadoCentralizacion = new ArrayList<ResultadoCentralizacion>();
				if(centralizaciones.size() > 0){
					log.write("Se buscan los movimientos");
					params.put("IdEmpresa", in.p_IdEmpresa);
					params.put("FechaCentralizacion", in.p_FechaInicio);
					params.put("IdCentralizaciones", centralizaciones);
					mov = _sql.query(conn, SQL_SELECT_MOVIMIENTOS, params, Movimientos.class);
					log.write("Se ejecuta sp contabilidad");
					CentralizaMultiSeleccion centralizaMultiSeleccion = new CentralizaMultiSeleccion();
					EntradaCentralizaMultiSeleccion entradaCentralizaMultiSeleccion = new EntradaCentralizaMultiSeleccion();
					
					entradaCentralizaMultiSeleccion.p_IdEmpresa = in.p_IdEmpresa;
					entradaCentralizaMultiSeleccion.p_IdUsuario = in.p_IdUsuario;
					entradaCentralizaMultiSeleccion.p_IdModulo = in.p_IdModulo;
					entradaCentralizaMultiSeleccion.p_SimulaOperacion = "S";
					entradaCentralizaMultiSeleccion.p_FechaContable = in.p_FechaInicio;
					entradaCentralizaMultiSeleccion.p_Ejercicio = Long.valueOf(in.p_FechaInicio.split("-")[2]);
					entradaCentralizaMultiSeleccion.p_in_TiposCentralizacion = inTiposCentralizacion;
					SalidaCentralizaMultiSeleccion salidaCentralizaMultiSeleccion = centralizaMultiSeleccion.logica(conn, entradaCentralizaMultiSeleccion);
					
					resultadoCentralizacion = salidaCentralizaMultiSeleccion.P_OUT_RESULTADO;
				}
				params.clear();
				
				for (Resumen objProc: rsResumen){
					if(objProc.IdTipoCentralizacion.longValue() == 0){
						switch (objProc.IdCuadraturaDiariaOperaciones) {
							case "CUACOMCON":
								
								String vResultado = new String();
								List<Comprobantes> descuadres = new ArrayList<Comprobantes>();
								params.put("FechaCentralizacion", in.p_FechaInicio);
								params.put("IdEmpresa", in.p_IdEmpresa);
								List<Comprobantes> comprobantes = _sql.query(conn, SQL_SELECT_COMPROBANTES, params, Comprobantes.class);
								descuadres = buscarDescuadresComprobantesContabilizados(comprobantes);
								
								if (comprobantes.size()==0 && descuadres.size()==0){
									vResultado = "UI_SIN_OPERACIONES";
								}
								else if(descuadres.size() == 0){
									vResultado = "UI_CUADRADO";
								}else {
									vResultado = "UI_DESCUADRADO";
								}

								objProc.TotalOperaciones = (long)comprobantes.size(); 
								objProc.OperacionesConProblemas = (long)descuadres.size();
								objProc.Resultado = vResultado;
								objProc.DetalleCuadraturaJSON = gson.toJson(descuadres);
								
								break;
							default:
								break;
						}
					}else{
						String vResultado = new String();
						List<Movimientos> movCentralizacion = buscarMovimientos(objProc.IdTipoCentralizacion,mov);
						List<Movimientos> descuadres = new ArrayList<Movimientos>();
						List<Movimientos> descuadresCentralizacion = new ArrayList<Movimientos>();
						List<Movimientos> descuadresCentralizacionActual = new ArrayList<Movimientos>();
						descuadresCentralizacion = buscarDescuadresCentralizacion(objProc.IdTipoCentralizacion,movCentralizacion,resultadoCentralizacion);
						descuadresCentralizacionActual = buscarDescuadresCentralizacionActual(objProc.IdTipoCentralizacion,movCentralizacion,resultadoCentralizacion);
						descuadres.addAll(descuadresCentralizacion);
						descuadres.addAll(descuadresCentralizacionActual);
						if (movCentralizacion.size()==0 && descuadres.size()==0){
							vResultado = "UI_SIN_OPERACIONES";
						}
						else if(descuadres.size() == 0){
							vResultado = "UI_CUADRADO";
						}else {
							vResultado = "UI_DESCUADRADO";
						}
						
						objProc.TotalOperaciones = (long)movCentralizacion.size(); 
						objProc.OperacionesConProblemas = (long)descuadres.size();
						objProc.Resultado = vResultado;
						objProc.DetalleCuadraturaJSON = gson.toJson(descuadres);
						
					}
					
					objProc.FechaEjecucion = new Date(format.parse(in.p_FechaInicio).getTime());
					objCuadraturaDiariaOperaciones oDelete = new objCuadraturaDiariaOperaciones();
					oDelete.IdCuadraturaDiariaOperaciones = objProc.IdCuadraturaDiariaOperaciones;
					
					parametrosparaBorrar.add(oDelete);
					objProc.IdUsuario = in.p_IdUsuario;
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
			HashMap<String,String> mapMsg = new HashMap<>();
			out.P_OUT_MENSAJES.add(new MensajeSalida(1L,MensajeSalida.MENSAJE_ERROR,"UI_PROCESO_TERMINADO_CON_ERRORES",mapMsg,conn,in.p_IdEmpresa));
			log.write(e);
			throw e;
		}
		
		return out;
	}
	
	private List<Comprobantes> buscarDescuadresComprobantesContabilizados(List<Comprobantes> mov){
		List<Comprobantes> descuadres = new ArrayList<Comprobantes>();
		for(Comprobantes comprobante : mov){
			if("V".equalsIgnoreCase(comprobante.Estado)){
				comprobante.Descripcion = "No Contabilizado";//"UI_NO_CONTABILIZADO";
				descuadres.add(comprobante);
			}
		}
		return descuadres;
	}
	
	private List<Movimientos> buscarMovimientos(Long idTipoCentralizacion,List<Movimientos> mov){
		List<Movimientos> movCentralizacion = new ArrayList<Movimientos>();
		for(Movimientos mv : mov){
			if(mv.IdTipoCentralizacion.longValue() == idTipoCentralizacion.longValue()){
				movCentralizacion.add(mv);
			}
		}
		return movCentralizacion;
	}
	
	//Busca los que se encuentran en la centralizacion simulada y no se encuentra en lo centralizado actualemente
	private List<Movimientos> buscarDescuadresCentralizacion(Long idTipoCentralizacion,List<Movimientos> mov,List<ResultadoCentralizacion> centralizados){
		List<Movimientos> descuadres = new ArrayList<Movimientos>();
		for(ResultadoCentralizacion obj : centralizados){
			if(obj.IdTipoCentralizacion.longValue() == idTipoCentralizacion.longValue()){
				boolean existe = false;
				for(Movimientos mv : mov){
					if(mv.IdTipoCentralizacion.longValue() == obj.IdTipoCentralizacion.longValue() && mv.Numero.longValue() == obj.Numero.longValue()){
						existe = true;
					}
				}
				if(!existe){
					Movimientos mv = new Movimientos();
					mv.IdTipoCentralizacion = obj.IdTipoCentralizacion;
					mv.IdMovimiento = obj.IdMovimiento;
					mv.IdTipoMovimiento = obj.IdTipoMovimiento;
					mv.IdEmpresa = obj.IdEmpresa;
					mv.Numero = obj.Numero;    
					mv.Linea = obj.Linea;     
					mv.Fecha = obj.Fecha;
					mv.DocumentoIdentidad = obj.DocumentoIdentidad;
					mv.IdPortafolio = obj.IdPortafolio;
					mv.Ejercicio = obj.Ejercicio;
					mv.Periodo = obj.Periodo;
					mv.Monto = obj.Monto;
					mv.IdMoneda = obj.IdMoneda;
					mv.Estado = obj.Estado;
					mv.Descripcion = "Movimiento no Centralizado";//"UI_MOVIMIENTO_NO_CENTRALIZADO";
					descuadres.add(mv);
				}
			}
		}
		return descuadres;
	}
	
	//Busca los que se encuentran en la centralizacion actual y no se encuentra en lo centralizado simulada
	private List<Movimientos> buscarDescuadresCentralizacionActual(Long idTipoCentralizacion,List<Movimientos> mov,List<ResultadoCentralizacion> centralizados){
		List<Movimientos> descuadres = new ArrayList<Movimientos>();
		for(Movimientos mv : mov){
			boolean existe = false;
			for(ResultadoCentralizacion obj : centralizados){
				if(obj.IdTipoCentralizacion.longValue() == idTipoCentralizacion.longValue() && mv.Numero.longValue() == obj.Numero.longValue()){
					existe = true;
				}
			}
			if(!existe){
				mv.Descripcion = "Movimiento ya no es Contabilizable";//"UI_MOVIMIENTO_YA_NO_ES_CONTABILIZABLE";
				descuadres.add(mv);
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
			log.IdModulo = "CON";
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
			//String json = "{\"p_IdUsuario\":21,\"p_IdEmpresa\":1,\"p_IdModulo\":\"CON\",\"p_FechaInicio\":\"2018-08-21\",\"p_IdProceso\":[{\"IdCuadraturaDiariaOperaciones\":\"CUACENFACTCP\"}],\"p_TipoLlamado\":\"Proceso\"}";
			String json = "{\"p_IdUsuario\":74252,\"p_IdEmpresa\":1,\"p_IdModulo\":\"CON\",\"p_FechaInicio\":\"2023-09-15\",\"p_IdProceso\":[],\"p_TipoLlamado\":\"Proceso\"}";
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
