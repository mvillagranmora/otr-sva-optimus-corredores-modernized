package cl.bcs.plataforma.pau;
import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo;
import cl.bcs.plataforma.pla.AgregarLog;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;


public class AgregarControlProceso implements JavaCall {	
	static final Logger logger = LogManager.getLogger(AgregarControlProceso.class);

	//QUERIES
	private final String SQL_INSERTCONTROLPROCESO = loadResourceToString("cl/bcs/plataforma/pau/sql/InsertControlProceso.sql");
	private final String SQL_SELECT_USUARIO = loadResourceToString("cl/bcs/plataforma/pau/sql/SelectUsuarioSistema.sql");
	private final String SQL_SELECT_REQUISITO_OPERATIVO = loadResourceToString("cl/bcs/plataforma/pau/sql/AgregarControlProceso_SelectRequisitoOperativo.sql");
	private final String SQL_SELECT_EVENTO_AUTORIZACION = loadResourceToString("cl/bcs/plataforma/pau/sql/AgregarControlProceso_SelectEventoAutorizacion.sql");
	private final String ID_CONCEPTO = "MODFAC";

	/*******************
	 * clases in/out
	 *******************/
	
	public static class InsertControlProceso{
		public Long IdEmpresa;
		public String CodigoModulo;
		public String CodigoProceso;
		public String FechaSistema;
		public String CodigoUsuario;
		public Long Secuencia;
		public String ResultadoProceso;
	}
	
	public static class ControlProcesoEntrada {
		public Long IdEmpresa;
		public String CodigoModulo;
		public String CodigoProceso;
		public String FechaSistema;
		public String CodigoUsuario;
		public String ResultadoProceso;
		public String p_UsaReglasNegocio;
		
		public Long errores;
		public Long advertencias;
		public Long autorizaciones = 0L;
		public Long ok = 0L;
		public List<MensajeSalida>	MensajesProceso;
		public String IdTipoCanalIngreso;
		
		public ControlProcesoEntrada(){
			this.errores = 0L;
			this.advertencias = 0L;
			this.autorizaciones = 0L;
			this.ok = 0L;
			this.MensajesProceso = new ArrayList<>();
			this.IdTipoCanalIngreso = "0";
			this.p_UsaReglasNegocio = "N";
		}
	}
	
	public static class ControlProcesoSalida extends BcsPojo{
		public Long P_OUT_ERRORESCONTADOR;
		public String P_OUT_MENSAJE;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public ControlProcesoSalida() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJE = "";
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
	}
	
	public static class RequisitosOperativo extends BcsPojo{
		public String IdTipoEtapa;
		public Long IdEmpresa;
		public String IdTipoCanalIngreso;
		public String AP_CuadraturaFacturacion;
	}
	
	public static class Usuario {
		public Long IdUsuario;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Evento{
		public Long IdEvento;
		
		public Evento(){
			this.IdEvento = 0L;
		}
	}

	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = getGsonBuilder();		
		ControlProcesoEntrada in=gson.fromJson(jin.toString() ,ControlProcesoEntrada.class);
		ControlProcesoSalida out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	
	public void AgregarLog(Connection con, ControlProcesoEntrada in) {
		try {
			String FechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");

			Long idUsuario = this.obtenerIdUsuario(con, in.CodigoUsuario);
			Long IdCabeceraLog = idUsuario;	
			//CABECERA
			AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
			AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
			//SOLO DEJARA REGISTRO EN EL LOG AQUELLOS QUE CONTENGAN EN EL CODIGO PROCESO CUADRATURA_ BOTON DE APROBACION PARA LAS CUADRATURA DE CIERRE
			if(in.CodigoProceso.contains("CUADRATURA_")) {
				entradaAgregarLog.p_IdEmpresa = in.IdEmpresa;
				entradaAgregarLog.p_IdModulo= in.CodigoModulo;
				entradaAgregarLog.p_IdUsuario = 0L;
				log.IdCabeceraLog = IdCabeceraLog;
				log.IdModulo = in.CodigoModulo;
				log.IdUsuario = idUsuario;
				log.IdEmpresa = in.IdEmpresa;
				log.FechaSistemaLog = FechaSistema;
				log.IdUsuarioAtendido = idUsuario;
				log.IdCliente = 0L;
				log.EstadoCliente = "";
				log.NombreCliente = "";
				log.IdTipoDocumentoIdentidad = "";
				log.IdDocumentoIdentidad = "";
				log.IdPortafolio = 0L;
				log.EstadoPortafolio = "";
				log.IdTipoRegistro = "APROBACION CUADRATURA";
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
				entradaAgregarLog.p_Log.add(log);
				AgregarLog agregarLogService = new AgregarLog();
				agregarLogService.logica(con, entradaAgregarLog);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private Long obtenerIdUsuario(Connection con, String codigoUsuario) throws Exception {
		logger.debug("INICIO::AgregarControlProceso.obtenerIdUsuario");
		long inicio = System.currentTimeMillis();
		
		Long idUsuario = 0L;
    	SQL _sql = new SQL();
    	HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("CodigoUsuario", codigoUsuario);
		List<Usuario> usuarioSistema = _sql.query(con, SQL_SELECT_USUARIO, params, Usuario.class);        
		idUsuario = !usuarioSistema.isEmpty()?usuarioSistema.get(0).IdUsuario:0L;
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::AgregarControlProceso.obtenerIdUsuario", fin - inicio);
		return idUsuario;
	}
	
	public ControlProcesoSalida logica(Connection con, ControlProcesoEntrada entrada) throws Exception {
	    logger.debug("INICIO::AgregarControlProceso");
	    long inicio = System.currentTimeMillis();

	    ControlProcesoSalida out = new ControlProcesoSalida();
	    HashMap<String, RequisitosOperativo> requisitos = obtenerRequisitosOperativos(con, entrada.IdEmpresa);

	    if ("S".equals(entrada.p_UsaReglasNegocio)) {
	        ProcesaRequisito(con, entrada.IdEmpresa, "FAC".equals(entrada.CodigoModulo), "CUADRATURA_FACTURACION", "AP_CuadraturaFacturacion", entrada, requisitos, out.P_OUT_MENSAJES);
	        String requiereAutorizacion = Utils.getRequiereEventoAutorizacion(con, entrada.IdEmpresa, ID_CONCEPTO);
	        
	        if(puedeCrearAutorizacion(requiereAutorizacion, entrada.CodigoModulo, entrada.autorizaciones)){
	        	Evento evento = this.obtenerEventoAutorizacion(con, entrada);
	        	if(evento.IdEvento == 0L) {
		            this.crearEventoAutorizacion(con, entrada, out);
	        	}else {
	        		HashMap<String,String> valores = new HashMap<>();
	        		valores.put("IdEvento", evento.IdEvento.toString());
	    			out.P_OUT_ERRORESCONTADOR++;
	    			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ERROR_YA_EXISTE_EVENTO", valores,con,entrada.IdEmpresa));
	    			return out;
	        	}
	        }else if(entrada.errores == 0) {
		        this.generarControlProceso(con, entrada, out);
	        }
	    }else{
			this.generarControlProceso(con, entrada, out);
		}

	    long fin = System.currentTimeMillis();
	    logger.debug("FIN::AgregarControlProceso", fin - inicio);
	    return out;
	}
	
	public Boolean puedeCrearAutorizacion(String requiereAutorizacion, String codigoModulo, Long autorizaciones) {
	    return "S".equals(requiereAutorizacion) && "FAC".equals(codigoModulo) && autorizaciones > 0;
	}

	private ControlProcesoSalida generarControlProceso(Connection con, ControlProcesoEntrada entrada, ControlProcesoSalida out) throws Exception {
		logger.debug("INICIO::AgregarControlProceso.generarControlProceso");
		long inicio = System.currentTimeMillis();
		try{
			out.P_OUT_ERRORESCONTADOR = new Long(0);
			SQL sql=new SQL();
			List<Long> secuencia = Utils.getSecuencia(con, "BSCL.Plataforma.BD.Secuencias::IdControlProceso", 1L);
			List<InsertControlProceso> prog = new ArrayList<InsertControlProceso>();
			InsertControlProceso iteminsert = new InsertControlProceso();
			iteminsert.CodigoModulo = entrada.CodigoModulo;
			iteminsert.CodigoProceso = entrada.CodigoProceso;
			iteminsert.CodigoUsuario = entrada.CodigoUsuario;
			iteminsert.FechaSistema = entrada.FechaSistema;
			iteminsert.IdEmpresa = entrada.IdEmpresa;
			iteminsert.ResultadoProceso = entrada.ResultadoProceso;
			iteminsert.Secuencia = secuencia.get(0);
			System.out.println("IdControlProceso->"+Utils.printJson(iteminsert, false));
			prog.add(iteminsert);
			sql.batch(con,SQL_INSERTCONTROLPROCESO, prog, InsertControlProceso.class);
			AgregarLog(con, entrada);
			
		} catch (Exception e){
			out.P_OUT_ERRORESCONTADOR++;
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ERROR_APROBAR_CUADRATURA", new HashMap<String, String>(),con,entrada.IdEmpresa));
		}
		long fin = System.currentTimeMillis();
		logger.debug("FIN::AgregarControlProceso.generarControlProceso", fin - inicio);
		return out;
	}
	
	private Evento obtenerEventoAutorizacion(Connection con, ControlProcesoEntrada entrada) throws Exception {

		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");

		SQL sql = new SQL();
		HashMap<String,Object> params = new HashMap<>();
		params.put("FechaSistema", fechaSistema);
		params.put("IdTipoEvento", ID_CONCEPTO);

		Evento evento = sql.query(con, SQL_SELECT_EVENTO_AUTORIZACION, params, Evento.class).stream().findFirst().orElse(new Evento());
		return evento;
	}
	
	private ControlProcesoSalida crearEventoAutorizacion(Connection con,  ControlProcesoEntrada entrada, ControlProcesoSalida out) throws Exception {
		logger.debug("INICIO::AgregarControlProceso.agregarControlProceso");
		long inicio = System.currentTimeMillis();

		CrearEventoAutorizacionMasivo crearEventoAutorizacionMasivo = new CrearEventoAutorizacionMasivo();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		Long idUsuario = this.obtenerIdUsuario(con, entrada.CodigoUsuario);
		
		CrearEventoAutorizacionMasivo.ServiceEntrada entradaCrearEventoAutorizacionMasivo = new CrearEventoAutorizacionMasivo.ServiceEntrada();
		entradaCrearEventoAutorizacionMasivo.P_IDEMPRESA = entrada.IdEmpresa;
		entradaCrearEventoAutorizacionMasivo.P_IDMODULO = entrada.CodigoModulo;
		entradaCrearEventoAutorizacionMasivo.P_IDUSUARIO = idUsuario;
		entradaCrearEventoAutorizacionMasivo.P_SIMULAROPERACION = "N";
		entradaCrearEventoAutorizacionMasivo.P_USAREGLASNEGOCIO = "N";
		
		CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS evento = new CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS();
		evento.IdUnion = 1L;
		evento.IdTipoEvento = ID_CONCEPTO;
		evento.IdModuloSolicita = entrada.CodigoModulo;
		evento.IdUsuarioSolicita = idUsuario;
		evento.Fecha = fechaSistema;
		evento.IdConcepto = ID_CONCEPTO;
		evento.FechaOperacionConcepto = fechaSistema;
		evento.Glosa = "DESCUADRATURA CIERRE DIARIO FACTURACION";

		CrearEventoAutorizacionMasivo.ObjTABLADETALLE detalleEvento = new CrearEventoAutorizacionMasivo.ObjTABLADETALLE();
		detalleEvento.IdUnion = evento.IdUnion;
		detalleEvento.Clasificacion = "1";
		detalleEvento.NombreCampo = "ResultadoProceso";
		detalleEvento.TipoDato = "STRING";
		detalleEvento.Valor = entrada.ResultadoProceso;
		
		entradaCrearEventoAutorizacionMasivo.P_TABLAEVENTOS = Arrays.asList(evento);
		entradaCrearEventoAutorizacionMasivo.P_DETALLE = Arrays.asList(detalleEvento);
		
		CrearEventoAutorizacionMasivo.ServiceSalida salidaCrearEventoAutorizacionMasivo = crearEventoAutorizacionMasivo.logica(con, entradaCrearEventoAutorizacionMasivo);
		out.P_OUT_ERRORESCONTADOR += salidaCrearEventoAutorizacionMasivo.P_OUT_ERRORESCONTADOR;

		List<MensajeSalida> mensajesAdvertencia = salidaCrearEventoAutorizacionMasivo.P_OUT_ADVERTENCIASDETALLE.parallelStream().map(advertencia -> new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA, advertencia.Codigo, advertencia.Descripcion)).collect(Collectors.toList());
		List<MensajeSalida> mensajesErrores = salidaCrearEventoAutorizacionMasivo.P_OUT_ERRORESDETALLE.parallelStream().map(error -> new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, error.Codigo, error.Descripcion)).collect(Collectors.toList());

		out.P_OUT_MENSAJES.addAll(mensajesAdvertencia);
		out.P_OUT_MENSAJES.addAll(mensajesErrores);

		
		if (out.P_OUT_ERRORESCONTADOR.equals(0L)) {
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_AUTORIZACION, "REQUIERE_AUTORIZACION", new HashMap<String, String>(), con, entrada.IdEmpresa));
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::AgregarControlProceso.crearEventoAutorizacion", fin - inicio);
		return out;
	}
	
	private HashMap<String,RequisitosOperativo> obtenerRequisitosOperativos(Connection con, Long idEmpresa) throws Exception {
		HashMap<String, RequisitosOperativo> requisitos = new HashMap<>();

		SQL sql = new SQL();
		HashMap<String,Object> params = new HashMap<>();
		params.put("IdEmpresa", idEmpresa);
		List<RequisitosOperativo> requisitosOperativos = sql.query(con, SQL_SELECT_REQUISITO_OPERATIVO, params, RequisitosOperativo.class);
		HashMap<Object,Object> __requisitos = Utils.List2Hash(requisitosOperativos, "IdTipoCanalIngreso");

		for(Object key: __requisitos.keySet()) {
			requisitos.put((String)key,(RequisitosOperativo) __requisitos.get(key));
		}
		return requisitos;
	}
	
	
	private void ProcesaRequisito(Connection con,Long idEmpresa,boolean esError,String codMsg,String CodRequisito,ControlProcesoEntrada cp,HashMap<String, RequisitosOperativo> requisitos,List<MensajeSalida> P_OUT_MENSAJES) throws Exception {
		MensajeSalida objMensaje = new MensajeSalida();
		if (esError) {
			HashMap<String,String> valores = new HashMap<String,String>();

			String msg=Diccionario.getMensaje(con,idEmpresa,codMsg)
				  .msg();
			if (CodRequisito.equals(MensajeSalida.MENSAJE_ERROR)) {
				objMensaje = new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,codMsg,msg);
				P_OUT_MENSAJES.add(objMensaje);
				cp.MensajesProceso.add(objMensaje);
				cp.errores++;						
			}else if (CodRequisito.equals(MensajeSalida.MENSAJE_AUTORIZACION)) {
				objMensaje = new MensajeSalida(0L,MensajeSalida.MENSAJE_AUTORIZACION,codMsg,msg);
				P_OUT_MENSAJES.add(objMensaje);
				cp.MensajesProceso.add(objMensaje);
				cp.autorizaciones++;						
			}
			else if (CodRequisito.equals(MensajeSalida.MENSAJE_ADVERTENCIA)) {
				objMensaje = new MensajeSalida(0L,MensajeSalida.MENSAJE_ADVERTENCIA,codMsg,msg);
				P_OUT_MENSAJES.add(objMensaje);
				cp.MensajesProceso.add(objMensaje);
				cp.advertencias++;
			}else {
				String requisito = obtenerValorRequisitoOperativo(requisitos,cp.IdTipoCanalIngreso, CodRequisito);
				if("AP_CuadraturaFacturacion".equals(CodRequisito)) {
				if (requisito.equals(MensajeSalida.MENSAJE_ERROR) || requisito.equals(MensajeSalida.MENSAJE_RECHAZO)) {
					objMensaje = new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"CUADRATURA_FACTURACION_ERROR",valores,con,idEmpresa);
					P_OUT_MENSAJES.add(objMensaje);
					cp.MensajesProceso.add(objMensaje);
					cp.errores++;
				}
				else if (requisito.equals(MensajeSalida.MENSAJE_AUTORIZACION)) {
					objMensaje = new MensajeSalida(0L,requisito,codMsg,msg);
					cp.MensajesProceso.add(objMensaje);
					cp.autorizaciones++;
				}
				else if (requisito.equals(MensajeSalida.MENSAJE_ADVERTENCIA)) {
					objMensaje = new MensajeSalida(0L,requisito,"CUADRATURA_FACTURACION_ADVERTENCIA",valores,con,idEmpresa);
					P_OUT_MENSAJES.add(objMensaje);
					cp.MensajesProceso.add(objMensaje);
					cp.advertencias++;
				}
				else {
					cp.ok++;
				}
				}
			if(cp.errores>0) {
				return;
				}
			}
		}else {
			cp.ok++;
		}		
	}
	
	public String obtenerValorRequisitoOperativo(HashMap<String, RequisitosOperativo> requisitos,String IdTipoCanalIngreso,String requisitoOperativo) throws IllegalArgumentException, IllegalAccessException {
		if (requisitos.get(IdTipoCanalIngreso) == null &&  requisitos.get("0") == null) return "OK";
		RequisitosOperativo reqCanal = requisitos.get(IdTipoCanalIngreso);
		RequisitosOperativo reqTodos = requisitos.get("0");
		Field[] fields = reqCanal.getClass().getDeclaredFields();
		for (Field f: fields) {
			if (f.getName().equalsIgnoreCase(requisitoOperativo)) {
				String requisitoCanal = (String) f.get(reqCanal);
				String requisitoTodos = null;
				if (f.get(reqTodos)!= null) {
					requisitoTodos = (String) f.get(reqTodos);
				} else {
					requisitoTodos = null;
				}
				if (requisitoCanal != null){
					return requisitoCanal;
				}
				if (requisitoTodos != null){
					return requisitoTodos;
				}
				else {
					return "OK";
				}
			}
		}
		return "";
	}
	
	public static void main(String[] args) {

		try (Connection con =  PlataformaUtils.getPropertiesConnection("qa02","connections.properties")){
			Gson gson = Utils.getGsonBuilder();					
			String json = "{\"IdEmpresa\":1,\"CodigoModulo\":\"FAC\",\"CodigoProceso\":\"CUADRATURA_FACTURACION\",\"FechaSistema\":\"2024-03-25\",\"CodigoUsuario\":\"SISTEMA\",\"ResultadoProceso\":\"UI_DESCUADRE\",\"p_UsaReglasNegocio\":\"S\"}";
			ControlProcesoEntrada Objin = gson.fromJson(json, ControlProcesoEntrada.class);			
			AgregarControlProceso crearComp = new AgregarControlProceso();
			JsonParser parser 		= new JsonParser();
	        JsonObject dataJsonIn	= parser.parse(json).getAsJsonObject();
			JsonObject salida = crearComp.call(con, dataJsonIn);
			System.out.println(salida);
	    	con.commit();
	    	con.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
        System.out.println("== END ==");
	}
}