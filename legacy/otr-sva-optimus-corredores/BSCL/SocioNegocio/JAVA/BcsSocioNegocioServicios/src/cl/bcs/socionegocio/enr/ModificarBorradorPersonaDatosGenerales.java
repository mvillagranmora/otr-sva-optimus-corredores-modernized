package cl.bcs.socionegocio.enr;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
//import java.util.concurrent.atomic.AtomicInteger;
import java.util.Objects;
import java.util.stream.Collectors;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.socionegocio.enr.CrearBorradorEnrolamiento.ExcepcionNacimiento;
import cl.bcs.socionegocio.enr.CrearBorradorEnrolamiento.ExcepcionResidencia;
import cl.bcs.socionegocio.enr.CrearBorradorFatcaPaisResidencia.EntradaCrearBorradorFatcaPaisResidencia;
import cl.bcs.socionegocio.enr.ModificarBorradorFatcaPaisResidencia.EntradaModificarBorradorFatcaPaisResidencia;
//import cl.bcs.socionegocio.enr.pojo.DireccionFisica;
import cl.bcs.socionegocio.enr.pojo.EntradaAdministrarBorrador;
import cl.bcs.socionegocio.enr.pojo.FatcaPaisResidencia;
import cl.bcs.socionegocio.enr.pojo.PersonaDatosGenerales;
import cl.bcs.socionegocio.enr.pojo.ResultadoAdministrarBorradorEntidad;
import cl.bcs.socionegocio.enr.pojo.SalidaAdministrarBorrador;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.utils.Utils.DatosPais;

public class ModificarBorradorPersonaDatosGenerales implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(ModificarBorradorPersonaDatosGenerales.class);
	
	private String SQL_UPDATE_PERSONA_DATOS_GENERALES = loadResourceToString("cl/bcs/socionegocio/enr/sql/ModificarBorradorPersonaDatosGenerales_UpdatePersonaDatosGenerales.sql");
	private final String SQL_SELECT_FATCA_PAIS_RESIDENCIA = loadResourceToString("cl/bcs/socionegocio/enr/sql/ComunesBorradorFatcaPaisResidencia_SelectFatcaPaisResidencia.sql");
	private final String SQL_UPDATE_FATCA_EXCEPCION_NACIMIENTO = loadResourceToString("cl/bcs/socionegocio/enr/sql/ComunesBorradorFatca_UpdateFatcaExcepcionNacimiento.sql");
	private final String SQL_UPDATE_FATCA_EXCEPCION_RESIDENCIA = loadResourceToString("cl/bcs/socionegocio/enr/sql/ComunesBorradorFatca_UpdateFatcaExcepcionResidencia.sql");
	private String SQL_SELECT_EXISTENCIA_CARGO = "SELECT CARGO.\"IdTipoCargoPersona\" AS \"Existencia_Cargo\" FROM \"BSCL.SocioNegocio.BD::PER.TipoCargoPersona\" AS CARGO WHERE CARGO.\"CodigoTipoCargoPersona\"=:p_CodigoTipoCargoPersona";
	private String SQL_SELECT_EXISTENCIA_REGIMEN_CONYUGAL = "SELECT REGIMENCONYUGAL.\"IdTipoRegimenConyugal\" AS \"ExisteTipoRegimenConyugal\" FROM \"BSCL.Plataforma.BD::REG.TipoRegimenConyugal\" AS REGIMENCONYUGAL WHERE REGIMENCONYUGAL.\"IdTipoRegimenConyugal\"=:p_IdTipoRegimenConyugal";

	
	public static class EntradaModificarBorradorPersonaDatosGenerales extends EntradaAdministrarBorrador{
		public List<PersonaDatosGenerales> p_PersonaDatosGenerales;
		
		public EntradaModificarBorradorPersonaDatosGenerales(){
			this.p_PersonaDatosGenerales = new ArrayList<PersonaDatosGenerales>();
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaModificarBorradorPersonaDatosGenerales jin = gson.fromJson(in.toString(), EntradaModificarBorradorPersonaDatosGenerales.class);
		SalidaAdministrarBorrador out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaAdministrarBorrador logica(Connection con, EntradaModificarBorradorPersonaDatosGenerales entrada) throws Exception {
		logger.info("INICIO::ModificarBorradorPersonaDatosGenerales");
		long inicioServicio = System.currentTimeMillis();
		SalidaAdministrarBorrador out = new SalidaAdministrarBorrador();
		
		out = entrada.validarCamposObligatoriosEntrada(con, entrada.p_PersonaDatosGenerales, PersonaDatosGenerales.class, out);
		out =validarExistenciaCargo(con, entrada, out);
		out =validarRegimenConyugal(con, entrada, out);
		if(out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}
		
		List<PersonaDatosGenerales> datosGenerales = obtenerPersonaDatosGenerales(con, entrada.p_PersonaDatosGenerales);
		
		modificarDatosGenerales(con, datosGenerales);
		
		PersonaDatosGenerales datosGeneralesEnrolamiento = datosGenerales.get(0); 

		Long idPaisSistema = ConfiguracionSistema.getParametroConfiguracionLong(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.PaisSistema");

		boolean esPaisResidenciaDistintitoASistema = !Objects.equals(datosGeneralesEnrolamiento.IdPaisResidencia, idPaisSistema);
		if(esPaisResidenciaDistintitoASistema) {
			SalidaAdministrarBorrador salidaCrearBorradorFatcaPaisResidencia = administrarBorradorFatcaPaisResidencia(con, entrada.p_IdEmpresa, datosGeneralesEnrolamiento);
			if(out.P_OUT_ERRORESCONTADOR > 0) {
				out.agregarErroresMensajesSalida(salidaCrearBorradorFatcaPaisResidencia);
			}
		}

		actualizarExcepcionResidenciaFatca(con, datosGeneralesEnrolamiento);
		actualizarExcepcionNacimientoFatca(con, datosGeneralesEnrolamiento);
		
		out.P_OUT_REFID = obtenerIdsInsertados(datosGenerales);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarBorradorPersonaDatosGenerales", finServicio - inicioServicio);
		return out;
		
	}

	private List<PersonaDatosGenerales> obtenerPersonaDatosGenerales(Connection con, List<PersonaDatosGenerales> borradoresDatosGenerales) throws Exception{
		logger.trace("INICIO::ModificarBorradorPersonaDatosGenerales.obtenerPersonaDatosGenerales");
		long inicio = System.currentTimeMillis();
		
		List<PersonaDatosGenerales> datosGenerales = borradoresDatosGenerales.parallelStream().map(borradorDireccionFisica->{
			PersonaDatosGenerales datosGeneral = obtenerEntidadDatosGenerales(borradorDireccionFisica);
			return datosGeneral;
		}).collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ModificarBorradorPersonaDatosGenerales.obtenerDireccionesFisicas", fin - inicio);
		return datosGenerales;
	}
	
	private PersonaDatosGenerales obtenerEntidadDatosGenerales(PersonaDatosGenerales borradorDatosGenerales) {
		logger.trace("INICIO::ModificarBorradorPersonaDatosGenerales.obtenerEntidadDatosGenerales");
		long inicio = System.currentTimeMillis();
		
		PersonaDatosGenerales datoGeneral = new PersonaDatosGenerales();
		datoGeneral.IdInterno = borradorDatosGenerales.IdInterno;
		datoGeneral.IdEnrolamiento = borradorDatosGenerales.IdEnrolamiento;
		datoGeneral.Nombres = borradorDatosGenerales.Nombres;
		datoGeneral.ApellidoPaterno = borradorDatosGenerales.ApellidoPaterno;
		datoGeneral.ApellidoMaterno = borradorDatosGenerales.ApellidoMaterno;
		datoGeneral.RazonSocial = borradorDatosGenerales.RazonSocial;
		datoGeneral.IdGiroPrincipal = borradorDatosGenerales.IdGiroPrincipal;
		datoGeneral.IdPaisNacimiento = borradorDatosGenerales.IdPaisNacimiento;
		datoGeneral.IdPaisResidencia = borradorDatosGenerales.IdPaisResidencia;
		datoGeneral.FechaNacimiento = borradorDatosGenerales.FechaNacimiento;
		datoGeneral.Sexo = borradorDatosGenerales.Sexo;
		datoGeneral.EstadoCivil = borradorDatosGenerales.EstadoCivil;
		datoGeneral.IdTransparenciaFiscal = borradorDatosGenerales.IdTransparenciaFiscal;
		datoGeneral.RegimenConyugal = borradorDatosGenerales.RegimenConyugal;
		datoGeneral.IdProfesion = borradorDatosGenerales.IdProfesion;
		datoGeneral.FechaInicioActividad = borradorDatosGenerales.FechaInicioActividad;
		datoGeneral.IdPersonaConyuge = borradorDatosGenerales.IdPersonaConyuge;
		datoGeneral.IdPersonaRepresentante = borradorDatosGenerales.IdPersonaRepresentante;
		datoGeneral.IdPaisNacionalidadPrincipal = borradorDatosGenerales.IdPaisNacionalidadPrincipal;
		datoGeneral.GlobalLegalEntity = borradorDatosGenerales.GlobalLegalEntity;
		datoGeneral.IdPersonaEmpleador = borradorDatosGenerales.IdPersonaEmpleador;
		datoGeneral.CodigoTipoCargoEmpleado = borradorDatosGenerales.CodigoTipoCargoEmpleado;
		datoGeneral.DetalleCargo = borradorDatosGenerales.DetalleCargo;
		datoGeneral.IdRangoPatrimonio = borradorDatosGenerales.IdRangoPatrimonio;
		datoGeneral.IdGradoInstruccion = borradorDatosGenerales.IdGradoInstruccion;
		datoGeneral.AgnosServicio = borradorDatosGenerales.AgnosServicio;
		datoGeneral.IngresoPromedioMensual = borradorDatosGenerales.IngresoPromedioMensual;
		datoGeneral.IdGiroCIIU = borradorDatosGenerales.IdGiroCIIU;
		datoGeneral.IdPaisResidenciaSecundaria = borradorDatosGenerales.IdPaisResidenciaSecundaria;
		datoGeneral.IdPaisNacionalidadSecundaria = borradorDatosGenerales.IdPaisNacionalidadSecundaria;
		datoGeneral.EsExtranjero = borradorDatosGenerales.EsExtranjero;
		datoGeneral.IdProfesion = borradorDatosGenerales.IdProfesion;
		datoGeneral.ResidenciaTributariaExtranjera = borradorDatosGenerales.ResidenciaTributariaExtranjera;
		datoGeneral.IdSituacionLaboral = borradorDatosGenerales.IdSituacionLaboral;
		datoGeneral.IdTipoCanalIngresoCorredor = borradorDatosGenerales.IdTipoCanalIngresoCorredor;
		datoGeneral.NombreFantasia = borradorDatosGenerales.NombreFantasia;
		datoGeneral.CodigoRangoRenta = borradorDatosGenerales.CodigoRangoRenta;
		datoGeneral.CodigoRangoNivelVenta = borradorDatosGenerales.CodigoRangoNivelVenta;
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ModificarBorradorPersonaDatosGenerales.obtenerEntidadDatosGenerales", fin - inicio);
		return datoGeneral;
	}
	
	
	private SalidaAdministrarBorrador validarExistenciaCargo(Connection con, EntradaModificarBorradorPersonaDatosGenerales entrada , SalidaAdministrarBorrador salidaCrearBorrador) throws Exception {
		for(PersonaDatosGenerales persona:entrada.p_PersonaDatosGenerales) {
			if (persona.IdSituacionLaboral.equalsIgnoreCase("EMPL")) {
				SQL sql = new SQL();
				HashMap<String,Object> parametros = new HashMap<String,Object>(2);
				parametros.put("p_CodigoTipoCargoPersona",persona.CodigoTipoCargoEmpleado);
				HashMap<String, String> params = new HashMap<>(2);
				params.put("CodigoTipoCargoEmpleado",persona.CodigoTipoCargoEmpleado);
				Long existeCargo = sql.countQuery(con, SQL_SELECT_EXISTENCIA_CARGO, parametros);
				if(existeCargo.equals((long)0)) {
					salidaCrearBorrador.P_OUT_ERRORESCONTADOR ++;
					salidaCrearBorrador.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ERROR, "UI_NO_EXISTE_CARGO", params, con, entrada.p_IdEmpresa));				
				}	
			}
		}
		return salidaCrearBorrador;		
	}
		
	private Boolean validarExistenciaRegimenConyugal(Connection con, String regimenConyugal) throws Exception {
		SQL sql = new SQL();
		HashMap<String,Object> parametros = new HashMap<String,Object>(2);
		parametros.put("p_IdTipoRegimenConyugal",regimenConyugal);
		Long existeRegimenConyugal = sql.countQuery(con, SQL_SELECT_EXISTENCIA_REGIMEN_CONYUGAL, parametros);
		Boolean existeRegimen = existeRegimenConyugal == 0 ? false : true;
		return existeRegimen;
	}
	
	private SalidaAdministrarBorrador validarRegimenConyugal(Connection con, EntradaModificarBorradorPersonaDatosGenerales entrada , SalidaAdministrarBorrador salidaCrearBorrador) throws Exception {		
		for(PersonaDatosGenerales persona:entrada.p_PersonaDatosGenerales) {
			if(persona.EstadoCivil.equalsIgnoreCase("CA")) {
				HashMap<String, String> params = new HashMap<>(2);
				List<MensajeSalida> mensaje= salidaCrearBorrador.P_OUT_MENSAJES.stream().filter(s -> s.descripcion.startsWith("Campo REGIMEN CONYUGAL es obligatorio")).collect(Collectors.toList());		
				params.put("IdTipoRegimenConyugal",persona.RegimenConyugal);
				params.put("NombreCampo","REGIMEN CONYUGAL");
				if(persona.RegimenConyugal.equals("") && mensaje.size()==0) {
					salidaCrearBorrador.P_OUT_ERRORESCONTADOR ++;
					salidaCrearBorrador.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ERROR, "UI_ERROR_CAMPO_ES_OBLIGATORIO", params, con, entrada.p_IdEmpresa));								
				}
				if(!validarExistenciaRegimenConyugal(con,persona.RegimenConyugal)) {
					salidaCrearBorrador.P_OUT_ERRORESCONTADOR ++;
					salidaCrearBorrador.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ERROR, "UI_NO_EXISTE_REGIMEN_CONYUGAL", params, con, entrada.p_IdEmpresa));							
				}
			}
		}
		return salidaCrearBorrador;		
	}
	
	private SalidaAdministrarBorrador administrarBorradorFatcaPaisResidencia(Connection con, Long idEmpresa, PersonaDatosGenerales datosGeneralesEnrolamiento) throws Exception {
		logger.debug("INICIO::ModificarBorradorPersonaDatosGenerales.administrarBorradorFatcaPaisResidencia");
		long inicio = System.currentTimeMillis();
		SalidaAdministrarBorrador salidaAdministrarBorradorFatcaPaisResidencia = new SalidaAdministrarBorrador();
		SQL sql = new SQL();
		HashMap<String,Object> paramsSelect = new HashMap<String,Object>();
		paramsSelect.put("IdEnrolamiento", datosGeneralesEnrolamiento.IdEnrolamiento);	
	
		List<FatcaPaisResidencia> fatcasPaisResidenciaPrincipal = sql.query(con, SQL_SELECT_FATCA_PAIS_RESIDENCIA, paramsSelect, FatcaPaisResidencia.class);		
		FatcaPaisResidencia fatcaPaisResidenciaPrincipal = fatcasPaisResidenciaPrincipal.parallelStream().findFirst().orElse(null);
		
		List<FatcaPaisResidencia> fatcasPaisResidencia = new ArrayList<FatcaPaisResidencia>();	
		FatcaPaisResidencia fatcaPaisResidencia = new FatcaPaisResidencia();
		fatcaPaisResidencia.IdEnrolamiento = datosGeneralesEnrolamiento.IdEnrolamiento;
		fatcaPaisResidencia.IdPaisResidencia = datosGeneralesEnrolamiento.IdPaisResidencia;
		fatcaPaisResidencia.TIN = "";
		fatcaPaisResidencia.IdCausaNoTin = "OTRA";
		fatcaPaisResidencia.Observacion = "Pendiente de ingreso";
		fatcaPaisResidencia.PaisResidenciaPrincipal = "S";
		fatcasPaisResidencia.add(fatcaPaisResidencia);
		
		if(fatcasPaisResidenciaPrincipal.isEmpty()) {						
			salidaAdministrarBorradorFatcaPaisResidencia = crearBorradorFatcaPaisResidencia(con, idEmpresa, fatcasPaisResidencia);			
		}else if(fatcaPaisResidenciaPrincipal.IdPaisResidencia != datosGeneralesEnrolamiento.IdPaisResidencia){		
			salidaAdministrarBorradorFatcaPaisResidencia = modificarBorradorFatcaPaisResidencia(con, idEmpresa, fatcasPaisResidencia);
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarBorradorPersonaDatosGenerales.administrarBorradorFatcaPaisResidencia", fin - inicio);
		return salidaAdministrarBorradorFatcaPaisResidencia;	
	}
	
	private SalidaAdministrarBorrador crearBorradorFatcaPaisResidencia(Connection con, Long idEmpresa, List<FatcaPaisResidencia> fatcasPaisResidencia) throws Exception {
		logger.debug("INICIO::ModificarBorradorPersonaDatosGenerales.crearBorradorFatcaPaisResidencia");
		long inicio = System.currentTimeMillis();
		SalidaAdministrarBorrador salidaCrearBorradorFatcaPaisResidencia = new SalidaAdministrarBorrador();
		CrearBorradorFatcaPaisResidencia crearBorradorFatcaPaisResidencia = new CrearBorradorFatcaPaisResidencia();
		EntradaCrearBorradorFatcaPaisResidencia entradaCrearBorradorFatcaPaisResidencia = new EntradaCrearBorradorFatcaPaisResidencia();
		
		entradaCrearBorradorFatcaPaisResidencia.p_IdEmpresa = idEmpresa;
		entradaCrearBorradorFatcaPaisResidencia.p_FatcaPaisResidencia = fatcasPaisResidencia;

		salidaCrearBorradorFatcaPaisResidencia = crearBorradorFatcaPaisResidencia.logica(con, entradaCrearBorradorFatcaPaisResidencia);		
				
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarBorradorPersonaDatosGenerales.crearBorradorFatcaPaisResidencia", fin - inicio);
		return salidaCrearBorradorFatcaPaisResidencia;
	}
	
	private SalidaAdministrarBorrador modificarBorradorFatcaPaisResidencia(Connection con, Long idEmpresa, List<FatcaPaisResidencia> fatcasPaisResidencia) throws Exception {
		logger.debug("INICIO::ModificarBorradorPersonaDatosGenerales.modificarBorradorFatcaPaisResidencia");
		long inicio = System.currentTimeMillis();
		SalidaAdministrarBorrador salidaModificarBorradorFatcaPaisResidencia = new SalidaAdministrarBorrador();
		ModificarBorradorFatcaPaisResidencia modificarBorradorFatcaPaisResidencia = new ModificarBorradorFatcaPaisResidencia();
		EntradaModificarBorradorFatcaPaisResidencia entradaModificarBorradorFatcaPaisResidencia = new EntradaModificarBorradorFatcaPaisResidencia();
		
		entradaModificarBorradorFatcaPaisResidencia.p_IdEmpresa = idEmpresa;	
		entradaModificarBorradorFatcaPaisResidencia.p_FatcaPaisResidencia = fatcasPaisResidencia;
		
		salidaModificarBorradorFatcaPaisResidencia = modificarBorradorFatcaPaisResidencia.logica(con, entradaModificarBorradorFatcaPaisResidencia);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarBorradorPersonaDatosGenerales.modificarBorradorFatcaPaisResidencia", fin - inicio);
		return salidaModificarBorradorFatcaPaisResidencia;
	}
	
	private void actualizarExcepcionNacimientoFatca(Connection con, PersonaDatosGenerales datosGeneralesEnrolamiento) throws SQLException {
		logger.debug("INICIO::EntradaModificarBorradorPersonaDatosGenerales.actualizarExcepcionNacimientoFatca");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		DatosPais paisUsa = Utils.getDatosPais(con, "US");
		ExcepcionNacimiento excepcionNacimiento = new ExcepcionNacimiento();
		excepcionNacimiento.IdEnrolamiento = datosGeneralesEnrolamiento.IdEnrolamiento; 
		excepcionNacimiento.ExcepcionNacimiento = datosGeneralesEnrolamiento.IdPaisNacimiento.compareTo(paisUsa.IdPais) == 0 ? "S" : "N";
		List<ExcepcionNacimiento> excepciones = new ArrayList<ExcepcionNacimiento>();
		excepciones.add(excepcionNacimiento);
		sql.batch(con, SQL_UPDATE_FATCA_EXCEPCION_NACIMIENTO, excepciones, ExcepcionNacimiento.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::EntradaModificarBorradorPersonaDatosGenerales.actualizarExcepcionNacimientoFatca", fin - inicio);
	}

	private void actualizarExcepcionResidenciaFatca(Connection con, PersonaDatosGenerales datosGeneralesEnrolamiento) throws SQLException {
		logger.debug("INICIO::EntradaModificarBorradorPersonaDatosGenerales.actualizarExcepcionResidenciaFatca");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		DatosPais paisUsa = Utils.getDatosPais(con, "US");
		ExcepcionResidencia excepcionResidencia = new ExcepcionResidencia();
		excepcionResidencia.IdEnrolamiento = datosGeneralesEnrolamiento.IdEnrolamiento; 
		excepcionResidencia.ExcepcionResidencia = datosGeneralesEnrolamiento.IdPaisResidencia.compareTo(paisUsa.IdPais) == 0 ? "S" : "N";
		List<ExcepcionResidencia> excepciones = new ArrayList<ExcepcionResidencia>();
		excepciones.add(excepcionResidencia);
		sql.batch(con, SQL_UPDATE_FATCA_EXCEPCION_RESIDENCIA, excepciones, ExcepcionResidencia.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::EntradaModificarBorradorPersonaDatosGenerales.actualizarExcepcionResidenciaFatca", fin - inicio);
	}
	
	private void modificarDatosGenerales(Connection con, List<PersonaDatosGenerales> datosGenerales) throws Exception {
		logger.debug("INICIO::ModificarBorradorPersonaDatosGenerales.modificarDatosGenerales");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		sql.batch(con, SQL_UPDATE_PERSONA_DATOS_GENERALES, datosGenerales, PersonaDatosGenerales.class);
		long fin = System.currentTimeMillis();

		logger.debug("FIN::ModificarBorradorPersonaDatosGenerales.modificarDatosGenerales", fin - inicio);
	}
	
	private List<ResultadoAdministrarBorradorEntidad> obtenerIdsInsertados(List<PersonaDatosGenerales> datosGenerales){
		logger.trace("INICIO::ModificarBorradorPersonaDatosGenerales.obtenerIdsInsertados");
		long inicio = System.currentTimeMillis();
		
		List<ResultadoAdministrarBorradorEntidad> idsInsertados = datosGenerales.parallelStream().map(datoGeneral->{
			ResultadoAdministrarBorradorEntidad resultadoCreacionBorradorEntidad = new ResultadoAdministrarBorradorEntidad();
			resultadoCreacionBorradorEntidad.IdInterno = datoGeneral.IdInterno;
			resultadoCreacionBorradorEntidad.IdEntidad = datoGeneral.IdEnrolamiento;
			return resultadoCreacionBorradorEntidad;
		}).collect(Collectors.toList());
		long fin = System.currentTimeMillis();
		
		logger.trace("FIN::ModificarBorradorPersonaDatosGenerales.obtenerIdsInsertados", fin - inicio);
		return idsInsertados;
	}

	public static void main(String[] args) {
		System.out.println("== INICIO TEST ==");

		try (Connection conn = PlataformaUtils.getDESAConnection()) {
			try {
				String jsonRequest = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"S\",\"p_CodigoFormulario\":\"DATOS_GENERALES_PERSONA_FULL_NATURAL\",\"p_IdTipoCanal\":\"OPT\",\"p_PersonaDatosGenerales\":[{\"IdInterno\":0,\"IdEnrolamiento\":424,\"Nombres\":\"HFKJ\",\"ApellidoPaterno\":\"ASDA\",\"ApellidoMaterno\":\"ASDSA\",\"IdPaisNacimiento\":44,\"IdPaisResidencia\":44,\"FechaNacimiento\":\"2021-04-01\",\"Sexo\":\"F\",\"EstadoCivil\":\"SO\",\"IdPersonaConyuge\":0,\"RegimenConyugal\":\"\",\"Cargo\":\"\",\"Empleador\":\"\",\"CodigoTipoCargoEmpleado\":\"AGE\",\"DetalleCargo\":\"\",\"IdRangoPatrimonio\":4,\"IngresoPromedioMensual\":0,\"IdGiroPrincipal\":135,\"IdGradoInstruccion\":0,\"IdGiroCIIU\":0,\"IdPaisResidenciaSecundaria\":0,\"IdPaisNacionalidadSecundaria\":0,\"RazonSocial\":\"\",\"FechaInicioActividad\":\"0001-01-01\",\"IdPersonaRepresentante\":0,\"IdPersonaEmpleador\":2326990,\"GlobalLegalEntity\":\"\",\"IdNombreFantasiaPrincipal\":0,\"AgnosServicio\":0,\"Alias\":\"\",\"EsExtranjero\":\"N\",\"IdTransparenciaFiscal\":\"CONNACIONAL\",\"IdProfesion\":2,\"IdPaisNacionalidadPrincipal\":1,\"ResidenciaTributariaExtranjera\":\"N\"}]}\n";
				EntradaModificarBorradorPersonaDatosGenerales entradaModificarBorradorPersonaDatosGenerales = Utils.getGsonBuilder().fromJson(jsonRequest, EntradaModificarBorradorPersonaDatosGenerales.class);
				System.out.println("Entrada");
				System.out.println(entradaModificarBorradorPersonaDatosGenerales);
				SalidaAdministrarBorrador salidaAdministrarBorrador = new ModificarBorradorPersonaDatosGenerales().logica(conn, entradaModificarBorradorPersonaDatosGenerales);
				System.out.println("Salida");
				System.out.println(salidaAdministrarBorrador);
				if (salidaAdministrarBorrador.P_OUT_ERRORESCONTADOR > 0) {
					conn.rollback();
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				conn.commit();
			}
		} catch (SQLException throwables) {
			throwables.printStackTrace();
		}

		System.out.println("== FIN TEST");
	}
}
