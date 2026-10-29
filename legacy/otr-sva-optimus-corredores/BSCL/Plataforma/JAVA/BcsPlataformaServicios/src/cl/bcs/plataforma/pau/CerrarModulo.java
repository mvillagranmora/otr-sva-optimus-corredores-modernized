package cl.bcs.plataforma.pau;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog.Log;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CerrarModulo implements JavaCall {
    private static final Logger logger = LogManager.getLogger(CerrarModulo.class);
    
    private final String SQL_CALL_SP_DEPURACION = loadResourceToString("cl/bcs/plataforma/pau/sql/CerrarModulo_CallServicioDepuracion.sql");
    private final String SQL_SELECT_ESTADO_MODULO_SISTEMA = loadResourceToString("cl/bcs/plataforma/pau/sql/CerrarModulo_SelectSistemaEntregado.sql");
    private final String SQL_SELECT_MODULO_ESTADO = loadResourceToString("cl/bcs/plataforma/pau/sql/CerrarModulo_SelectModuloEstado.sql");
    private final String SQL_SELECT_MESAS_ABIERTAS = loadResourceToString("cl/bcs/plataforma/pau/sql/CerrarModulo_SelectMesasAbiertas.sql");
    private final String SQL_SELECT_EJECUCION_PROCESOS = loadResourceToString("cl/bcs/plataforma/pau/sql/CerrarModulo_SelectProcesosEjecutados.sql");
    private final String SQL_SELECT_MODULOS_DEPENDIENTES = loadResourceToString("cl/bcs/plataforma/pau/sql/CerrarModulo_SelectModulosDependientes.sql");
    private final String SQL_UPDATE_CIERRE_MODULO = loadResourceToString("cl/bcs/plataforma/pau/sql/CerrarModulo_UpdateCierreModulo.sql");
    private final String SQL_SELECT_NOMBRE_MODULO = loadResourceToString("cl/bcs/plataforma/pau/sql/CerrarModulo_SelectNombreModulo.sql");
    private final String SQL_CALL_SP_ACTUALIZAR_TABLA_TBBASIPA = loadResourceToString("cl/bcs/plataforma/pau/sql/CerrarModulo_CallServicioActualizarTBBASIPA.sql");
    private final String SQL_INSERT_MENSAJE_WEBSOCKET = loadResourceToString("cl/bcs/plataforma/pau/sql/CerrarModulo_InsertMensajeWebsocket.sql");
    private final String SQL_SELECT_REQUISITO_OPERATIVO = loadResourceToString("cl/bcs/plataforma/pau/sql/CerrarModulo_SelectRequisitoOperativo.sql");
    private final String SQL_SELECT_EVENTO_AUTORIZACION = loadResourceToString("cl/bcs/plataforma/pau/sql/CerrarModulo_SelectEventoAutorizacion.sql");
	
    private final String ID_CONCEPTO = "MODFAC";
    
    public static class Modulo extends BcsPojo {
        public String CodigoModulo;
        public String Estado;
        public String CodigoUsuario;
        public String NombreModulo;
        
        public Modulo() {
        	this.CodigoModulo = "";
        	this.CodigoUsuario = "";
        	this.Estado = "CER";
        	this.NombreModulo = "";
        }
    }

    public static class EntradaCerrarModulo extends BcsPojo {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_UsaReglasNegocio;
        public List<Modulo> p_Modulo;
        
        public EntradaCerrarModulo () {
        	this.p_IdEmpresa = 0L;
        	this.p_IdUsuario = 0L;
        	this.p_IdModulo = "";
        	this.p_UsaReglasNegocio = "N";
        	this.p_Modulo = new ArrayList<Modulo>();
        }
    }

    public static class SalidaCerrarModulo extends BcsPojo {
        public List<MensajeSalida> P_OUT_MENSAJES;
        public Long P_OUT_ERRORESCONTADOR;
        public List<MensajeSalida> P_OUT_ADVERTENCIAS;
        public Long P_OUT_ADVERTENCIASCONTADOR;
        public List<ResultadoModulo> P_OUT_RESULTADO;
        
        public SalidaCerrarModulo () {
        	this.P_OUT_RESULTADO = new ArrayList<ResultadoModulo>();
        	this.P_OUT_ERRORESCONTADOR = 0L;
        	this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
        	this.P_OUT_ADVERTENCIASCONTADOR = 0L;
        	this.P_OUT_ADVERTENCIAS = new ArrayList<MensajeSalida>();
        }
    }
    
    public static class ResultadoModulo extends BcsPojo {
    	public String CodigoModulo;
    	public String IdEstado;
    	public String NombreModulo;
    	
    	public ResultadoModulo() {
    		this.CodigoModulo = "";
    		this.IdEstado = "";
    		this.NombreModulo = "";
    	}
    	
    	public ResultadoModulo (String codigoModulo, String idEstado, String nombreModulo) {
    		this.CodigoModulo = codigoModulo;
    		this.IdEstado = idEstado;
    		this.NombreModulo = nombreModulo;
    	}
    }
    
    public static class Mesa extends BcsPojo {
    	public Long IdMesa;
    	public String IdEstado;
    	public String CodigoModulo;
    	public String NombreModulo;
    	
    	public Mesa () {
    		this.IdMesa = 0L;
    		this.IdEstado = "";
    		this.CodigoModulo = "";
    		this.NombreModulo = "";
    	}
    }
    
    public static class Proceso extends BcsPojo {
    	public String CodigoModulo;
    	public String CodigoProceso;
    	public String DescripcionProceso;
    	public Long IdEmpresa;
    	public String NombreModulo;
    	public String Resultado;
    	
    	public Proceso () {
    		this.CodigoModulo = "";
    		this.CodigoProceso = "";
    		this.DescripcionProceso = "";
    		this.IdEmpresa = 0L;
    		this.NombreModulo = "";
    		this.Resultado = "";
    	}
    	
    }
    
    public static class ModuloDependiente extends Modulo {
    	public Long IdEmpresa;
    	public String Resultado;
    	 
    	public ModuloDependiente () {
    		this.IdEmpresa = 0L;
    		this.Resultado = "";
    	}
    }

    public static class ObjetoWebSocket extends ResultadoModulo {
    	public Long IdEmpresa;
    	public String CodigoUsuario;
    	
    	public ObjetoWebSocket () {
    		this.IdEmpresa = 0L;
    		this.CodigoUsuario = "";
    	}
    	
    }
    
    public static class RequisitoOperativo extends BcsPojo {
    	public String IdTipoEtapa;
		public String AP_CuadraturaFacturacion;
		public String NombreCondicionOperativa;
		
		public RequisitoOperativo () {
			this.IdTipoEtapa = "";
			this.AP_CuadraturaFacturacion = "";
			this.NombreCondicionOperativa = "";
		}
    }

	public static class Evento extends BcsPojo{
		public Long IdEvento;
		public String IdEstado;
		
		public Evento(){
			this.IdEvento = 0L;
			this.IdEstado = "";
		}
	}
	
    @SuppressWarnings("rawtypes")
    public SalidaCerrarModulo logica(Connection con, EntradaCerrarModulo entrada) throws Exception {
        logger.info("INICIO::CerrarModulo");
        long inicio = System.currentTimeMillis();
        SalidaCerrarModulo salida = new SalidaCerrarModulo();
        
        String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
        
        SQL sql = new SQL();
        HashMap<String,Object> parmsIn =new HashMap<String,Object>(5);
		parmsIn.put("p_IdEmpresa", entrada.p_IdEmpresa);
		parmsIn.put("p_IdUsuario", entrada.p_IdUsuario);
		parmsIn.put("p_Servicio", "CERRAR MODULO APERTURA CIERRE");
		parmsIn.put("p_Bandera", "INICIO");
		parmsIn.put("p_Detalle", "INICIO");
		
        sql.call(con, SQL_CALL_SP_DEPURACION, parmsIn, new HashMap<String,Class>(0));
        List<MensajeSalida> mensajesValidaciones = validarRequisitosCierreModulo(con, entrada, fechaSistema);
        
        salida.P_OUT_MENSAJES.addAll(mensajesValidaciones);
        
        if(!salida.P_OUT_MENSAJES.isEmpty()) {
        	salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
        	long fin = System.currentTimeMillis();
    		logger.info("FIN::CerrarModulo", fin - inicio);
    		return salida;
        }
        
    	sql.batch(con, SQL_UPDATE_CIERRE_MODULO, entrada.p_Modulo, Modulo.class);
    	
    	parmsIn.clear();
    	parmsIn.put("p_IdEmpresa", entrada.p_IdEmpresa);
		parmsIn.put("p_IdsModulo", entrada.p_Modulo.stream().map(mod -> mod.CodigoModulo).collect(Collectors.toList()));
    	List<ResultadoModulo> resultados = sql.query(con, SQL_SELECT_NOMBRE_MODULO, parmsIn, ResultadoModulo.class);
    	salida.P_OUT_RESULTADO.addAll(resultados);
    	
    	if(entrada.p_Modulo.stream().anyMatch(mod -> "MFW".equals(mod.CodigoModulo))) {
    		parmsIn.clear();
            parmsIn.put("p_IdEmpresa", entrada.p_IdEmpresa);
    		parmsIn.put("p_IdUsuario", entrada.p_IdUsuario);
    		parmsIn.put("p_FechaSistema", fechaSistema);
    		sql.call(con, SQL_CALL_SP_ACTUALIZAR_TABLA_TBBASIPA, parmsIn, new HashMap<String,Class>(0));
    	}
    	
    	List<ObjetoWebSocket> objetos = resultados.stream().map( res ->{
    		ObjetoWebSocket obj = new ObjetoWebSocket();
    		Modulo modulo = entrada.p_Modulo.stream().filter(mod -> res.CodigoModulo.equals(mod.CodigoModulo)).findFirst().orElse(new Modulo());
    		obj.IdEmpresa = entrada.p_IdEmpresa;
    		obj.NombreModulo = res.NombreModulo;
    		obj.CodigoUsuario = modulo.CodigoUsuario;
    		obj.CodigoModulo = res.CodigoModulo;
    		obj.IdEstado = res.IdEstado;
    		return obj;
    	}).collect(Collectors.toList());
    	sql.batch(con, SQL_INSERT_MENSAJE_WEBSOCKET, objetos, ObjetoWebSocket.class);
        
        parmsIn.clear();
        parmsIn.put("p_IdEmpresa", entrada.p_IdEmpresa);
		parmsIn.put("p_IdUsuario", entrada.p_IdUsuario);
		parmsIn.put("p_Servicio", "CERRAR MODULO APERTURA CIERRE");
		parmsIn.put("p_Bandera", "FIN");
		parmsIn.put("p_Detalle", "FIN");
		sql.call(con, SQL_CALL_SP_DEPURACION, parmsIn, new HashMap<String,Class>(0));
        
		agregarLog(con, objetos, entrada, fechaSistema);
		
        long fin = System.currentTimeMillis();
		logger.info("FIN::CerrarModulo", fin - inicio);
        return salida;
    }

    private void agregarLog(Connection con, List<ObjetoWebSocket> objetos, EntradaCerrarModulo entrada, String fechaSistema) throws Exception {
    	logger.trace("INICIO::CerrarModulo.agregarLog");
		long inicio = System.currentTimeMillis();
		
		AgregarLog agregarLogService = new AgregarLog();
		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
		
		entradaAgregarLog.p_IdTrabajo = 1L;
		List<Log> logs = objetos.stream().map(obj -> {
			Log log = new Log();
			log.IdTrabajo = entradaAgregarLog.p_IdTrabajo;
			log.IdCabeceraLog = 1L;
			log.IdTipoLog = "CERRAR";
			log.IdModulo = "PLA";
			log.IdUsuario = entrada.p_IdUsuario;
			log.IdEmpresa = entrada.p_IdEmpresa;
			log.FechaSistemaLog = fechaSistema;
			log.IdUsuarioAtendido = entrada.p_IdUsuario;
			log.IdPersona = 0L;
			log.IdCliente = 0L;
			log.EstadoCliente = "";
			log.NombreCliente = "";
			log.IdTipoDocumentoIdentidad = "";
			log.IdDocumentoIdentidad = "";
			log.IdPortafolio = 0L;
			log.EstadoPortafolio = "";
			log.IdTipoRegistro = "CERRAR_MODULO";
			log.IdRegistro = "0";
			log.EstadoRegistro = obj.IdEstado;
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
			log.EstadoProceso = "";
			log.NombreProceso = "";
			return log;
		}).collect(Collectors.toList());

		entradaAgregarLog.p_Log.addAll(logs);
		
		agregarLogService.logica(con, entradaAgregarLog);
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CerrarModulo.agregarLog", fin - inicio);
	}

	private List<MensajeSalida> validarRequisitosCierreModulo(Connection con, EntradaCerrarModulo entrada, String fechaSistema) throws Exception {
    	logger.info("INICIO::CerrarModulo.validarRequisitosCierreModulo");
        long inicio = System.currentTimeMillis();

        HashMap<String,String> parmsMsj =new HashMap<String,String>(4);
        
        List<MensajeSalida> mensajes = new ArrayList<MensajeSalida>();
        
        RevisarRequisitosBasicos revisarRequisitos = new RevisarRequisitosBasicos();
        RevisarRequisitosBasicosSalida salidaRequisitos = new RevisarRequisitosBasicosSalida();
        salidaRequisitos = revisarRequisitos.logica(con, entrada.p_IdEmpresa, entrada.p_IdUsuario ,entrada.p_IdModulo);
        
        if(!salidaRequisitos.p_out_Mensajes.isEmpty()){
        	mensajes.addAll(salidaRequisitos.p_out_Mensajes);
        	long fin = System.currentTimeMillis();
    		logger.info("FIN::CerrarModulo.validarRequisitosCierreModulo", fin - inicio);
    		return mensajes;
        }
        
        if(entrada.p_Modulo.isEmpty()) {
        	mensajes.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_NO_SE_INGRESARON_MODULOS", new HashMap<String, String>(0), con, entrada.p_IdEmpresa));
        	long fin = System.currentTimeMillis();
        	logger.info("FIN::CerrarModulo.validarRequisitosCierreModulo", fin - inicio);
    		return mensajes;
        }
        
        if(entrada.p_Modulo.stream().anyMatch(modulo -> "SIS".equals(modulo.CodigoModulo))) {
        	mensajes.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "MODULO_SISTEMA_NO_SE_PUEDE_CERRAR_DEBE_ENTREGAR", new HashMap<String, String>(0), con, entrada.p_IdEmpresa));
        	long fin = System.currentTimeMillis();
        	logger.info("FIN::CerrarModulo.validarRequisitosCierreModulo", fin - inicio);
    		return mensajes;
        }
        
        if("ENT".equals(consultarEstadoModuloSistema(con,entrada))) {
        	mensajes.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_NO_SE_PUEDE_CERRAR_SISTEMA_ENTREGADO", new HashMap<String, String>(0), con, entrada.p_IdEmpresa));
        	long fin = System.currentTimeMillis();
        	logger.info("FIN::CerrarModulo.validarRequisitosCierreModulo", fin - inicio);
    		return mensajes;
        }
        
        List<Modulo> modulos = consultarModulos(con, entrada);
        for(Modulo modulo : modulos) {
        	if("CER".equals(modulo.Estado)) {
        		parmsMsj.put("NombreModulo",modulo.NombreModulo);
        		mensajes.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_MODULO_YA_CERRADO", parmsMsj, con, entrada.p_IdEmpresa));
        		parmsMsj.clear();
        	}
        }
        
        if(!mensajes.isEmpty()) {
        	long fin = System.currentTimeMillis();
    		logger.info("FIN::CerrarModulo.validarRequisitosCierreModulo", fin - inicio);
    		return mensajes;
    	}
    	
    	if(entrada.p_Modulo.stream().anyMatch(mod -> Arrays.asList("MSP", "MRF", "MFW").contains(mod.CodigoModulo))) {
    		List<Mesa> mesasAbiertas = consultarMesasAbiertas(con, entrada.p_IdEmpresa, entrada.p_Modulo.stream().filter(mod->Arrays.asList("MSP", "MRF", "MFW").contains(mod.CodigoModulo)).collect(Collectors.toList()));
    		for(Mesa mesaAbierta : mesasAbiertas) {
    			parmsMsj.put("NombreModulo",mesaAbierta.NombreModulo);
    			mensajes.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_MODULO_CON_MESA_ABIERTA", parmsMsj, con, entrada.p_IdEmpresa));
    			parmsMsj.clear();
    		}
    		
    		if(!mensajes.isEmpty()) {
	        	long fin = System.currentTimeMillis();
	    		logger.info("FIN::CerrarModulo.validarRequisitosCierreModulo", fin - inicio);
	    		return mensajes;
	    	}
    	}
    	
    	List<Proceso> procesos = consultarEjecucionProcesos(con, entrada, fechaSistema);
    	
    	for(Proceso proceso : procesos) {
    		if(!"ERR".equals(proceso.Resultado)) {
    			continue;
    		}
    		
    		parmsMsj.put("Modulo",proceso.NombreModulo);
			parmsMsj.put("Proceso", proceso.DescripcionProceso);
			
			if("FAC".equals(proceso.CodigoModulo) ) {
				String requiereAutorizacion = Utils.getRequiereEventoAutorizacion(con, entrada.p_IdEmpresa, ID_CONCEPTO);
				List<RequisitoOperativo> requisitos = obtenerRequisitosOperativos(con, entrada.p_IdEmpresa);
				if("S".equals(requiereAutorizacion) && !requisitos.isEmpty() && MensajeSalida.MENSAJE_AUTORIZACION.equals(requisitos.get(0).AP_CuadraturaFacturacion)) {
					Evento evento = obtenerEventoAutorizacion(con,fechaSistema);
					if(evento.IdEvento.compareTo(0L) > 0) {
						parmsMsj.put("NombreEventoAutorizacion", requisitos.get(0).NombreCondicionOperativa);
						parmsMsj.put("IdEvento", evento.IdEvento.toString());
						if("VIG".equals(evento.IdEstado)) {
    						mensajes.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_MODULO_CON_PROCESOS_SIN_AUTORIZAR", parmsMsj, con, entrada.p_IdEmpresa)); 
						}else if("RZD".equals(evento.IdEstado)) {
    						mensajes.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_MODULO_CON_PROCESOS_RECHAZADOS", parmsMsj, con, entrada.p_IdEmpresa)); 
						} 
					}else {
						mensajes.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "MODULO_CON_PROCESOS_NO_EJECUTADOS", parmsMsj, con, entrada.p_IdEmpresa)); 
					}
				}else if(!requisitos.isEmpty() && MensajeSalida.MENSAJE_RECHAZO.equals(requisitos.get(0).AP_CuadraturaFacturacion)) {
    				mensajes.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_MODULO_FACTURACION_CON_PROCESOS_NO_EJECUTADOS_RECHAZADOS", parmsMsj, con, entrada.p_IdEmpresa));
				}
			}else {
				mensajes.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "MODULO_CON_PROCESOS_NO_EJECUTADOS", parmsMsj, con, entrada.p_IdEmpresa));
			}
			parmsMsj.clear();   
    	}

        if(!mensajes.isEmpty()) {
        	long fin = System.currentTimeMillis();
    		logger.info("FIN::CerrarModulo.validarRequisitosCierreModulo", fin - inicio);
    		return mensajes;
    	}
        
    	List<ModuloDependiente> modulosDependientesAbiertos = consultaModulosDependientesAbiertos(con, entrada);
    	
    	for(ModuloDependiente modulo : modulosDependientesAbiertos) {
    		if("ERR".equals(modulo.Resultado)) {
    			parmsMsj.put("NombreModulo",modulo.NombreModulo);
    			mensajes.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_MODULOS_DEPENDIENTES_AUN_ABIERTOS", parmsMsj, con, entrada.p_IdEmpresa));
    			parmsMsj.clear();
    		}
    	}
    	
        long fin = System.currentTimeMillis();
		logger.info("FIN::CerrarModulo.validarRequisitosCierreModulo", fin - inicio);
		return mensajes;
	}

	private Evento obtenerEventoAutorizacion(Connection con, String fechaSistema) throws Exception {
		logger.info("INICIO::CerrarModulo.obtenerEventoAutorizacion");
        long inicio = System.currentTimeMillis();
        
		SQL sql = new SQL();
		HashMap<String,Object> params = new HashMap<>(2);
		params.put("FechaSistema", fechaSistema);
		params.put("IdTipoEvento", ID_CONCEPTO);
		
		Evento evento = sql.query(con, SQL_SELECT_EVENTO_AUTORIZACION, params, Evento.class).stream().findFirst().orElse(new Evento());
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::CerrarModulo.obtenerEventoAutorizacion", fin - inicio);
		return evento;
	}

	private List<RequisitoOperativo> obtenerRequisitosOperativos(Connection con, Long p_IdEmpresa) throws Exception {
		logger.info("INICIO::CerrarModulo.obtenerRequisitosOperativos");
        long inicio = System.currentTimeMillis();
        
		SQL sql = new SQL();
		HashMap<String,Object> params = new HashMap<>(1);
		params.put("IdEmpresa", p_IdEmpresa);
		
		List<RequisitoOperativo> requisitos = sql.query(con, SQL_SELECT_REQUISITO_OPERATIVO, params, RequisitoOperativo.class);
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::CerrarModulo.obtenerRequisitosOperativos", fin - inicio);
		return requisitos;
	}

	private List<ModuloDependiente> consultaModulosDependientesAbiertos(Connection con, EntradaCerrarModulo entrada) throws Exception {
    	logger.info("INICIO::CerrarModulo.consultaModulosDependientesAbiertos");
        long inicio = System.currentTimeMillis();
        
        SQL sql = new SQL();
        HashMap<String,Object> params = new HashMap<String,Object>(2);
    	params.put("p_IdEmpresa", entrada.p_IdEmpresa);
    	params.put("p_IdsModulo", entrada.p_Modulo.stream().map( mod -> mod.CodigoModulo ).collect(Collectors.toList()));
    	
    	List<ModuloDependiente> modulosDependientesAbiertos = sql.query(con, SQL_SELECT_MODULOS_DEPENDIENTES, params, ModuloDependiente.class);
    	
    	long fin = System.currentTimeMillis();
		logger.info("FIN::CerrarModulo.consultaModulosDependientesAbiertos", fin - inicio);
		return modulosDependientesAbiertos;
	}

	private List<Proceso> consultarEjecucionProcesos(Connection con, EntradaCerrarModulo entrada, String fechaSistema) throws Exception {
    	logger.info("INICIO::CerrarModulo.consultarEjecucionProcesos");
        long inicio = System.currentTimeMillis();
        
        SQL sql = new SQL();
        HashMap<String,Object> params = new HashMap<String,Object>(3);
    	params.put("p_IdEmpresa", entrada.p_IdEmpresa);
    	params.put("p_IdsModulo", entrada.p_Modulo.stream().map( mod -> mod.CodigoModulo ).collect(Collectors.toList()));
    	params.put("p_FechaSistema", fechaSistema);
        
    	List<Proceso> procesos = sql.query(con, SQL_SELECT_EJECUCION_PROCESOS, params, Proceso.class);
    	
    	long fin = System.currentTimeMillis();
		logger.info("FIN::CerrarModulo.consultarEjecucionProcesos", fin - inicio);
		return procesos;
	}

	private List<Mesa> consultarMesasAbiertas(Connection con, Long p_idEmpresa, List<Modulo> modulos) throws Exception {
    	logger.info("INICIO::CerrarModulo.consultarMesasAbiertas");
        long inicio = System.currentTimeMillis();
        
        SQL sql = new SQL();
    	HashMap<String,Object> params = new HashMap<String,Object>(2);
    	params.put("p_IdEmpresa", p_idEmpresa);
    	params.put("p_IdsModulo", modulos.stream().map( mod -> mod.CodigoModulo ).collect(Collectors.toList()));
        
    	List<Mesa> mesasAbiertas = sql.query(con, SQL_SELECT_MESAS_ABIERTAS, params, Mesa.class);
    	
        long fin = System.currentTimeMillis();
		logger.info("FIN::CerrarModulo.consultarMesasAbiertas", fin - inicio);
		return mesasAbiertas;
	}

	private String consultarEstadoModuloSistema(Connection con, EntradaCerrarModulo entrada) throws Exception {
    	logger.info("INICIO::CerrarModulo.consultarEstadoModuloSistema");
        long inicio = System.currentTimeMillis();
        
    	SQL sql = new SQL();
    	HashMap<String,Object> params = new HashMap<String,Object>(1);
    	params.put("p_IdEmpresa", entrada.p_IdEmpresa);
    	Modulo moduloSistema = sql.query(con, SQL_SELECT_ESTADO_MODULO_SISTEMA, params, Modulo.class).stream().findFirst().orElse(new Modulo());
    	
    	long fin = System.currentTimeMillis();
		logger.info("FIN::CerrarModulo.consultarEstadoModuloSistema", fin - inicio);
		return moduloSistema.Estado;
	}
    
    private List<Modulo> consultarModulos(Connection con, EntradaCerrarModulo entrada) throws Exception {
    	logger.info("INICIO::CerrarModulo.consultarModulos");
        long inicio = System.currentTimeMillis();
        
    	SQL sql = new SQL();
    	HashMap<String,Object> params = new HashMap<String,Object>(2);
    	params.put("p_IdEmpresa", entrada.p_IdEmpresa);
    	params.put("p_IdsModulo",entrada.p_Modulo.stream().map(mod -> mod.CodigoModulo).collect(Collectors.toList()));
    	List<Modulo> modulos = sql.query(con, SQL_SELECT_MODULO_ESTADO, params, Modulo.class);
    	
    	long fin = System.currentTimeMillis();
		logger.info("FIN::CerrarModulo.consultarModulos", fin - inicio);
		return modulos;
	}

	@Override
    public JsonObject call(Connection conn, JsonObject in) throws Exception {
        Gson gson = new Gson();
        EntradaCerrarModulo entrada = gson.fromJson(in.toString(), EntradaCerrarModulo.class);
        SalidaCerrarModulo salida = logica(conn, entrada);

        JsonParser parser = new JsonParser();
        return parser.parse(gson.toJson(salida)).getAsJsonObject();
    }

    public static void main(String[] args) throws Exception {
        try (Connection con = PlataformaUtils.getPropertiesConnection("desa02")) {
            Gson gson = new Gson();
            String json = "{\r\n"
            		+ "    \"p_IdEmpresa\": 1,\r\n"
            		+ "    \"p_IdUsuario\": 97,\r\n"
            		+ "    \"p_IdModulo\": \"PLA\",\r\n"
            		+ "    \"p_simularOperacion\": \"N\",\r\n"
            		+ "    \"p_UsaReglasNegocio\": \"S\",\r\n"
            		+ "    \"p_Modulo\": [\r\n"
            		+ "        {\r\n"
            		+ "            \"CodigoModulo\": \"FAC\",\r\n"
            		+ "            \"Estado\": \"CER\",\r\n"
            		+ "            \"CodigoUsuario\": \"SISTEMA\"\r\n"
            		+ "        }\r\n"
            		+ "    ]\r\n"
            		+ "}";

            EntradaCerrarModulo entrada = gson.fromJson(json, EntradaCerrarModulo.class);
            CerrarModulo servicio = new CerrarModulo();

            try {
            	SalidaCerrarModulo salida = servicio.logica(con, entrada);
                System.out.println(salida);
                con.rollback();
            } catch (Exception e) {
                e.printStackTrace();
                con.rollback();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("== END ==");
    }
}
