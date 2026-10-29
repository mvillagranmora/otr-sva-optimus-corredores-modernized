package cl.bcs.cuentaInversion;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.cuentaInversion.CrearCobroComisionExclusionConcepto.Concepto;
import cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearCobroComisionExclusionConcepto implements JavaCall {
	private static final Logger logger = LogManager.getLogger(CrearCobroComisionExclusionConcepto.class);
			
	private final String SQL_CREATE_TABLA_PASO = loadResourceToString("cl/bcs/cuentaInversion/sql/CobroComisionExclusionConcepto_CreateTablaPaso.sql");
	private final String SQL_INSERT_TABLA_PASO = loadResourceToString("cl/bcs/cuentaInversion/sql/CobroComisionExclusionConcepto_InsertTablaPaso.sql");
	private final String SQL_SELECT_CONCEPTOS_VALIDOS = loadResourceToString("cl/bcs/cuentaInversion/sql/CobroComisionExclusionConcepto_SelectConceptosCuentaInversionValidos.sql");
	private final String SQL_INSERT_CONCEPTOS_EXCLUIDOS = loadResourceToString("cl/bcs/cuentaInversion/sql/CobroComisionExclusionConcepto_InsertConceptosCuentaInversion.sql");
	private final String SQL_DROP_TABLA_PASO = loadResourceToString("cl/bcs/cuentaInversion/sql/CobroComisionExclusionConcepto_DropTablaPaso.sql");
	private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();
	
	public static class EntradaCrearCobroComisionExclusionConcepto extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public List<Concepto> p_Conceptos;
		public Date p_FechaVigenciaDesde;
		
		public EntradaCrearCobroComisionExclusionConcepto () {
			this.p_IdEmpresa = 1L;
			this.p_IdUsuario = 0L;
			this.p_IdModulo = "CTA";
			this.p_UsaReglasNegocio = "N";
			this.p_Conceptos = new ArrayList<Concepto>();
			this.p_FechaVigenciaDesde =Fecha.toDate("0001-01-01");
		}
	}

	public static class SalidaCrearCobroComisionExclusionConcepto extends BcsPojo {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		public Long P_OUT_ADVERTENCIASCONTADOR;
		
		public SalidaCrearCobroComisionExclusionConcepto () {
			this.P_OUT_MENSAJES = new ArrayList<>();
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_ADVERTENCIASCONTADOR = 0L;
		}
	}
	
	public static class Concepto extends BcsPojo {
		public Long IdExclusionConcepto;
		public String IdConcepto;
		public String NombreConcepto;
		public Date FechaVigenciaDesde;
		public String Existe;
		public String Excluido;
		
		public Concepto () {
			this.IdExclusionConcepto = 0L;
			this.IdConcepto = "";
			this.NombreConcepto ="";
			this.FechaVigenciaDesde =Fecha.toDate("0001-01-01");
			this.Existe = "N";
			this.Excluido = "N";
		}
	}
	
	public static class ConceptoTablaPaso extends BcsPojo {
		public Long IdTrabajo;
		public String IdConcepto;
		
		public ConceptoTablaPaso () {
			this.IdTrabajo = 0L;
			this.IdConcepto = "";
		}
		
		public ConceptoTablaPaso (Long idTrabajo, String idConcepto) {
			this.IdTrabajo = 0L;
			this.IdConcepto = idConcepto;
		}
		
		
	}
	
	public SalidaCrearCobroComisionExclusionConcepto logica(Connection con, EntradaCrearCobroComisionExclusionConcepto entrada) throws Exception {
		logger.info("INICIO::CrearCobroComisionExclusionConcepto");
		long inicio = System.currentTimeMillis();
		
		SalidaCrearCobroComisionExclusionConcepto salida = new SalidaCrearCobroComisionExclusionConcepto();
		
		if(entrada.p_Conceptos.isEmpty()) {
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_ENTRADA_SIN_CONCEPTOS",new HashMap<String, String>(),con,entrada.p_IdEmpresa));
			salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
			return salida;
		}
		
		String fechaSistema	= ConfiguracionSistema.getParametroConfiguracionString(con, 1L,"PLATAFORMA", "PLA.FechaSistema");
		if(entrada.p_FechaVigenciaDesde.before(Fecha.toDate(fechaSistema))) {
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_FECHA_VIGENCIA_DESDE_MENOR_FECHA_SISTEMA",new HashMap<String, String>(),con,entrada.p_IdEmpresa));
			salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
			return salida;
		}
		
		List<Concepto> conceptosValidacion = validarExistenciaConceptos(con, entrada.p_Conceptos);
		List<Concepto> conceptosExistentes = new ArrayList<Concepto>();
		
		for(Concepto concepto : conceptosValidacion) {
			HashMap<String, String> paramMensaje = new HashMap<String, String>(1);
			paramMensaje.put("IdConcepto", concepto.IdConcepto);
			if(_c.NO.equals(concepto.Existe)) {
				logger.info("INFO::CrearCobroComisionExclusionConcepto :: Concepto "+ concepto.IdConcepto +" no Existe");
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ADVERTENCIA,"UI_ADVERTENCIA_CONCEPTO_NO_EXISTE",paramMensaje,con,entrada.p_IdEmpresa));
				salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
				salida.P_OUT_ADVERTENCIASCONTADOR++;
			} else if(_c.NO.equals(concepto.Excluido)) {
				conceptosExistentes.add(concepto);
			} else {
				logger.info("INFO::CrearCobroComisionExclusionConcepto :: Concepto "+ concepto.IdConcepto +" ya se encuentra Excluido");
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ADVERTENCIA,"UI_ADVERTENCIA_CONCEPTO_YA_EXCLUIDO",paramMensaje,con,entrada.p_IdEmpresa));
				salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
				salida.P_OUT_ADVERTENCIASCONTADOR++;
			}
		}
		
		if(!conceptosExistentes.isEmpty()) {
			conceptosExistentes = obtenerConceptosAgregar(con, conceptosExistentes, entrada.p_FechaVigenciaDesde.toString());
			ingresarExclusionConceptos(con, conceptosExistentes);
			
			for(Concepto concepto : conceptosExistentes) {
				HashMap<String, String> paramMensaje = new HashMap<String, String>(1);
				paramMensaje.put("IdConcepto", concepto.IdConcepto);
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"UI_CONCEPTO_EXCLUIDO",paramMensaje,con,entrada.p_IdEmpresa));
				
			}
		}
		
		//agregar log
		agregarLog(con, conceptosExistentes, entrada.p_IdEmpresa, entrada.p_IdUsuario);
		long fin = System.currentTimeMillis();
		logger.info("FIN::CrearCobroComisionExclusionConcepto", fin - inicio);
		
		return salida;
	}
	
	private void agregarLog(Connection con, List<Concepto> conceptos, Long idEmpresa, Long idUsuario) throws Exception {
		logger.info("INICIO::EliminarCobroComisionExclusionConcepto.agregarLog");
		long inicio = System.currentTimeMillis();
		
		String fechaSistema	= ConfiguracionSistema.getParametroConfiguracionString(con, 1L,"PLATAFORMA", "PLA.FechaSistema");
		
		AgregarLog agregarLogService = new AgregarLog();
		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
		entradaAgregarLog.p_IdTrabajo = 1L;
		
		for (Concepto concepto : conceptos) {
			AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();

			log.IdTrabajo = entradaAgregarLog.p_IdTrabajo;
			log.IdCabeceraLog = concepto.IdExclusionConcepto;
			log.IdTipoLog = "INGRESO";
			log.IdModulo = "CTA";
			log.IdUsuario = idUsuario;
			log.IdEmpresa = idEmpresa;
			log.FechaSistemaLog = fechaSistema;
			log.IdUsuarioAtendido = idUsuario;
			log.IdCliente = 0L;
			log.EstadoCliente = "";
			log.NombreCliente = "";
			log.IdTipoDocumentoIdentidad = "";
			log.IdDocumentoIdentidad = "";
			log.IdPortafolio = 0L;
			log.EstadoPortafolio = "";
			log.IdTipoRegistro = "EXCLUSION_CONCEPTO_COBRO_COMISION";
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
			log.EstadoProceso = "";
			log.NombreProceso = "Ingreso de Concepto "+ concepto.IdConcepto +" a Exclusion de Concepto Cobro Comision Patrimonio";						
			entradaAgregarLog.p_Log.add(log);
		}
		
		agregarLogService.logica(con, entradaAgregarLog);
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::EliminarCobroComisionExclusionConcepto.agregarLog", fin - inicio);
	}

	private void ingresarExclusionConceptos(Connection con, List<Concepto> conceptosValidos) throws Exception {
		logger.info("INICIO::CrearCobroComisionExclusionConcepto.ingresarExclusionConceptos");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		
		sql.batch(con, SQL_INSERT_CONCEPTOS_EXCLUIDOS, conceptosValidos, Concepto.class);
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::CrearCobroComisionExclusionConcepto.ingresarExclusionConceptos", fin - inicio);
	}


	private List<Concepto> obtenerConceptosAgregar(Connection con, List<Concepto> conceptosValidos, String fechaSistema) throws Exception {
		logger.info("INICIO::CrearCobroComisionExclusionConcepto.obtenerConceptosAgregar");
		
		List<Long> secuenciaConceptos = Utils.getSecuencia(con, "BSCL.CuentaInversion.BD.Secuencias::CobroComisionExclusionConcepto", Long.valueOf(conceptosValidos.size()));
		AtomicInteger index = new AtomicInteger(0);
		
		List<Concepto> conceptosAgregar = conceptosValidos.stream().map(concepto -> {
			concepto.IdExclusionConcepto = secuenciaConceptos.get(index.getAndIncrement());
			concepto.FechaVigenciaDesde = Fecha.toDate(fechaSistema);
			return concepto;
		}).collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::CrearCobroComisionExclusionConcepto.obtenerConceptosAgregar");
		return conceptosAgregar;
	}


	public List<Concepto> validarExistenciaConceptos(Connection con, List<Concepto> conceptosEntrada) throws Exception {
		logger.info("INICIO::CrearCobroComisionExclusionConcepto.validarExistenciaConceptos");
		long inicio = System.currentTimeMillis();
		
		SQL sql=new SQL();
		List<Concepto> conceptosValidados = new ArrayList<Concepto>();
		try {
			SQL.exec(con, SQL_CREATE_TABLA_PASO);
			Long idTrabajo = new Double(Math.random()*10000000000L).longValue();
			List<ConceptoTablaPaso> conceptos = conceptosEntrada.stream().map(c -> new ConceptoTablaPaso(idTrabajo, c.IdConcepto)).collect(Collectors.toList());
			
			sql.batch(con, SQL_INSERT_TABLA_PASO,conceptos,ConceptoTablaPaso.class);
			
			conceptosValidados = sql.query(con,SQL_SELECT_CONCEPTOS_VALIDOS,new HashMap<String,Object>(), Concepto.class);
			
		} catch (SQLException e) {
			logger.info("CrearCobroComisionExclusionConcepto.validarExistenciaConceptos:: ERROR ELIMINANDO VALIDANDO CONCEPTOS: ", e);
		} finally {
			try {
				SQL.exec(con, SQL_DROP_TABLA_PASO);		
			} catch (Exception er) {
				logger.info("CrearCobroComisionExclusionConcepto.validarExistenciaConceptos:: ERROR ELIMINANDO TABLA TEMPORAL: ", er);	
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::CrearCobroComisionExclusionConcepto.validarExistenciaConceptos", fin - inicio);
		
		return conceptosValidados;
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject in) throws Exception {
		Gson gson = getGsonBuilder();
		EntradaCrearCobroComisionExclusionConcepto jin=gson.fromJson(in.toString() , EntradaCrearCobroComisionExclusionConcepto.class);
		SalidaCrearCobroComisionExclusionConcepto out = logica(con,jin);
		JsonParser parser = new JsonParser();
		JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}

	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa01")) {
			Gson gson = Utils.getGsonBuilder();			
			String json = "{\r\n"
					+ "  \"p_IdEmpresa\": 1,\r\n"
					+ "  \"p_IdUsuario\": 33,\r\n"
					+ "  \"p_IdModulo\": \"CTA\",\r\n"
					+ "  \"p_UsaReglasNegocio\": \"N\",\r\n"
					+ "  \"p_FechaVigenciaDesde\": \"2024-04-24\",\r\n"
					+ "  \"p_Conceptos\": [\r\n"
					+ "    {\r\n"
					+ "      \"IdConcepto\": \"ABCCLP\"\r\n"
					+ "    }\r\n"
					+ "  ]\r\n"
					+ "}";
			EntradaCrearCobroComisionExclusionConcepto Objin = gson.fromJson(json, EntradaCrearCobroComisionExclusionConcepto.class);
			CrearCobroComisionExclusionConcepto demo = new CrearCobroComisionExclusionConcepto();
			try {
				SalidaCrearCobroComisionExclusionConcepto salida = demo.logica(con, Objin);
				System.out.println(salida.toString());
				con.commit();
//				con.rollback();
			}
			catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}
		}
		System.out.println("== END ==");
	}
}