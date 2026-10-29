package cl.bcs.socionegocio.enr;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.enr.CrearClienteFull.BorradorEnrolamiento;
import cl.bcs.socionegocio.enr.pojo.DatosClienteDatosGenerales;
import cl.bcs.socionegocio.enr.pojo.DeclaracionOrigenFondos;
import cl.bcs.socionegocio.enr.pojo.DetalleOrigenFondo;
import cl.bcs.socionegocio.enr.pojo.CuentaBancaria;
import cl.bcs.socionegocio.enr.pojo.CuentaCustodioPortafolio;
import cl.bcs.socionegocio.enr.pojo.DireccionElectronicaEnrolamiento;
import cl.bcs.socionegocio.enr.pojo.DireccionFisica;
import cl.bcs.socionegocio.enr.pojo.DocumentoIdentidad;
import cl.bcs.socionegocio.enr.pojo.Enrolamiento;
import cl.bcs.socionegocio.enr.pojo.Fatca;
import cl.bcs.socionegocio.enr.pojo.FatcaPaisResidencia;
import cl.bcs.socionegocio.enr.pojo.GrupoDireccionDetalleEnrolamiento;
import cl.bcs.socionegocio.enr.pojo.GrupoDireccionEnrolamiento;
import cl.bcs.socionegocio.enr.pojo.Indicador;
import cl.bcs.socionegocio.enr.pojo.NombreFantasia;
import cl.bcs.socionegocio.enr.pojo.PreferenciaPagoPortafolio;
import cl.bcs.socionegocio.enr.pojo.RelacionPersona;
import cl.bcs.socionegocio.enr.pojo.TarjetaBancaria;
import cl.bcs.socionegocio.enr.pojo.Telefono;
import cl.bcs.socionegocio.enr.pojo.Validacion;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarBorradorCliente implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(ConsultarBorradorCliente.class);
	
	private final String SQL_SELECT_ENROLAMIENTO = loadResourceToString("cl/bcs/socionegocio/enr/sql/ConsultarBorradorCliente_SelectEnrolamiento.sql");
	private final String SQL_SELECT_DOCUMENTO_IDENTIDAD = loadResourceToString("cl/bcs/socionegocio/enr/sql/ConsultarBorradorCliente_SelectDocumentoIdentidad.sql");
	private final String SQL_SELECT_CUENTA_BANCARIA = loadResourceToString("cl/bcs/socionegocio/enr/sql/ConsultarBorradorCliente_SelectCuentaBancaria.sql");
	private final String SQL_SELECT_TARJETA_BANCARIA = loadResourceToString("cl/bcs/socionegocio/enr/sql/ConsultarBorradorCliente_SelectTarjetaBancaria.sql");
	private final String SQL_SELECT_DIRECCION_FISICA = loadResourceToString("cl/bcs/socionegocio/enr/sql/ConsultarBorradorCliente_SelectDireccionFisica.sql");
	private final String SQL_SELECT_DIRECCION_ELECTRONICA = loadResourceToString("cl/bcs/socionegocio/enr/sql/ConsultarBorradorCliente_SelectDireccionElectronica.sql");
	private final String SQL_SELECT_TELEFONO = loadResourceToString("cl/bcs/socionegocio/enr/sql/ConsultarBorradorCliente_SelectTelefono.sql");
	private final String SQL_SELECT_GRUPO_DIRECCION = loadResourceToString("cl/bcs/socionegocio/enr/sql/ConsultarBorradorCliente_SelectGrupoDireccion.sql");
	private final String SQL_SELECT_GRUPO_DIRECCION_DETALLE = loadResourceToString("cl/bcs/socionegocio/enr/sql/ConsultarBorradorCliente_SelectGrupoDireccionDetalle.sql");
	private final String SQL_SELECT_INDICADOR = loadResourceToString("cl/bcs/socionegocio/enr/sql/ConsultarBorradorCliente_SelectIndicador.sql");
	private final String SQL_SELECT_RELACION = loadResourceToString("cl/bcs/socionegocio/enr/sql/ConsultarBorradorCliente_SelectRelacion.sql");
	private final String SQL_SELECT_FATCA = loadResourceToString("cl/bcs/socionegocio/enr/sql/ConsultarBorradorCliente_SelectFATCA.sql");
	private final String SQL_SELECT_FATCA_PAIS_RESIDENCIA = loadResourceToString("cl/bcs/socionegocio/enr/sql/ConsultarBorradorCliente_SelectFatcaPaisResidencia.sql");
	private final String SQL_SELECT_CUENTA_CUSTODIO = loadResourceToString("cl/bcs/socionegocio/enr/sql/ConsultarBorradorCliente_SelectCuentaCustodio.sql");
	private final String SQL_SELECT_PREFERENCIA_PAGO = loadResourceToString("cl/bcs/socionegocio/enr/sql/ConsultarBorradorCliente_SelectPreferenciaPago.sql");
	private final String SQL_SELECT_DECLARACION_ORIGEN_FONDOS = loadResourceToString("cl/bcs/socionegocio/enr/sql/ConsultarBorradorCliente_SelectDeclaracionOrigenFondos.sql");
	private final String SQL_SELECT_DETALLE_DECLARACION_ORIGEN_FONDOS = loadResourceToString("cl/bcs/socionegocio/enr/sql/ConsultarBorradorCliente_SelectDetalleDeclaracionOrigenFondos.sql");
	private final String SQL_SELECT_NOMBRE_FANTASIA = loadResourceToString("cl/bcs/socionegocio/enr/sql/ConsultarBorradorCliente_SelectNombreFantasia.sql");
	
	public static class EntradaConsultarBorradorCliente extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public List<BorradorEnrolamiento> p_Enrolamientos;
		
		public EntradaConsultarBorradorCliente() {
			this.p_Enrolamientos = new ArrayList<>();
		}
	}
	
	public static class SalidaConsultarBorradorCliente extends BcsPojo{
		public Long	P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public List<DatosClienteDatosGenerales> P_OUT_BORRADORES_CLIENTE;
		
		public SalidaConsultarBorradorCliente() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<>();
			this.P_OUT_BORRADORES_CLIENTE = new ArrayList<>();
		}
		
		public void agregarValidacionesSalida(List<Validacion> validaciones) {
			for(Validacion validacion : validaciones) {
				agregarValidacionSalida(validacion);
			}
		}
		
		public void agregarValidacionSalida(Validacion validacion) {
			if(validacion.EsError) {
				this.P_OUT_ERRORESCONTADOR += 1L;
				this.P_OUT_MENSAJES.add(validacion.MensajeError);
			}
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaConsultarBorradorCliente jin = gson.fromJson(in.toString(), EntradaConsultarBorradorCliente.class);
		SalidaConsultarBorradorCliente out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaConsultarBorradorCliente logica(Connection con, EntradaConsultarBorradorCliente entrada) throws Exception {
		logger.info("INICIO::ConsultarBorradorCliente");
		long inicioServicio = System.currentTimeMillis();
		
		SalidaConsultarBorradorCliente out = new SalidaConsultarBorradorCliente();
		List<DatosClienteDatosGenerales> borradoresCliente = obtenerBorradoresCliente(con, entrada.p_Enrolamientos);
		List<Validacion> validacionesBorradorCliente = validarBorradoresCliente(con, entrada.p_IdEmpresa, borradoresCliente);
		out.agregarValidacionesSalida(validacionesBorradorCliente);
		
		if(out.P_OUT_ERRORESCONTADOR.equals(0L)) {
			out.P_OUT_BORRADORES_CLIENTE = borradoresCliente;
		}
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ConsultarBorradorCliente", finServicio - inicioServicio);
		return out;
	}
	
	private List<DatosClienteDatosGenerales> obtenerBorradoresCliente(Connection con, List<BorradorEnrolamiento> borradoresEnrolamiento) throws Exception{
		logger.trace("INICIO::ConsultarBorradorCliente.obtenerBorradoresCliente");
		long inicio = System.currentTimeMillis();
		
		HashMap<String, Object> parametrosBusquedadBorradores = obtenerParametrosBusquedaBorradores(borradoresEnrolamiento);
		List<Enrolamiento> enrolamientos = obtenerEnrolamientos(con, parametrosBusquedadBorradores);
		List<DocumentoIdentidad> documentosIdentidad = obtenerDocumentosIdentidad(con, parametrosBusquedadBorradores);
		List<CuentaBancaria> cuentasBancaria = obtenerCuentasBancaria(con, parametrosBusquedadBorradores);
		List<TarjetaBancaria> tarjetasBancaria = obtenerTarjetasBancaria(con, parametrosBusquedadBorradores);
		List<DireccionFisica> direccionesFisica = obtenerDireccionesFisica(con, parametrosBusquedadBorradores);
		List<DireccionElectronicaEnrolamiento> direccionesElectronica = obtenerDireccionesElectronica(con, parametrosBusquedadBorradores);
		List<Telefono> telefonos = obtenerTelefonos(con, parametrosBusquedadBorradores);
		List<GrupoDireccionEnrolamiento> gruposDireccion = obtenerGruposDireccion(con, parametrosBusquedadBorradores);
		List<GrupoDireccionDetalleEnrolamiento> gruposDireccionDetalle = obtenerGruposDireccionDetalle(con, parametrosBusquedadBorradores);
		List<Indicador> indicadores = obtenerIndicadores(con, parametrosBusquedadBorradores);
		List<RelacionPersona> relaciones = obtenerRelaciones(con, parametrosBusquedadBorradores);
		List<Fatca> fatcas = obtenerFatcas(con, parametrosBusquedadBorradores);
		List<FatcaPaisResidencia> fatcaPaisesResidencia = obtenerFatcaPaisesResidencia(con, parametrosBusquedadBorradores);
		List<CuentaCustodioPortafolio> cuentasCustodio = obtenerCuentasCustodio(con, parametrosBusquedadBorradores);
		List<NombreFantasia> nombresFantasia = obtenerNombresFantasia(con, parametrosBusquedadBorradores);
		List<PreferenciaPagoPortafolio> preferenciasPago = obtenerPreferenciasPago(con, parametrosBusquedadBorradores);
		Map<Long, DeclaracionOrigenFondos> declaracionOrigenFondosPorIdDof = obtenerDeclaracionOrigenFondos(con, parametrosBusquedadBorradores);
		Map<Long, List<DetalleOrigenFondo>> detallePorIdDof = obtenerDetalleDeclaracionOrigenFondos(con, new ArrayList<>(declaracionOrigenFondosPorIdDof.keySet()));
		List<DeclaracionOrigenFondos> declaracionOrigenFondos = new ArrayList<>(declaracionOrigenFondosPorIdDof.values());
		
		List<DatosClienteDatosGenerales> borradoresCliente = new ArrayList<>();
		
		for(BorradorEnrolamiento borradorEnrolamiento : borradoresEnrolamiento) {
			DatosClienteDatosGenerales borradorCliente = new DatosClienteDatosGenerales();
			borradorCliente.IdInterno = borradorEnrolamiento.IdInterno;
			borradorCliente.Enrolamiento = enrolamientos
					.stream()
					.filter(enrolamiento->enrolamiento.IdEnrolamiento.equals(borradorEnrolamiento.IdEnrolamiento))
					.findFirst()
					.orElse(new Enrolamiento());
			
			borradorCliente.DocumentosIdentidades = documentosIdentidad
					.stream()
					.filter(borrador->borrador.IdEnrolamiento.equals(borradorEnrolamiento.IdEnrolamiento))
					.collect(Collectors.toList());
			
			borradorCliente.CuentasBancaria = cuentasBancaria
					.stream()
					.filter(borrador->borrador.IdEnrolamiento.equals(borradorEnrolamiento.IdEnrolamiento))
					.collect(Collectors.toList());
			
			borradorCliente.TarjetasBancaria = tarjetasBancaria
					.stream()
					.filter(borrador->borrador.IdEnrolamiento.equals(borradorEnrolamiento.IdEnrolamiento))
					.collect(Collectors.toList());
			
			borradorCliente.DireccionesFisica = direccionesFisica
					.stream()
					.filter(borrador->borrador.IdEnrolamiento.equals(borradorEnrolamiento.IdEnrolamiento))
					.collect(Collectors.toList());
			
			borradorCliente.DireccionesElectronica = direccionesElectronica
					.stream()
					.filter(borrador->borrador.IdEnrolamiento.equals(borradorEnrolamiento.IdEnrolamiento))
					.collect(Collectors.toList());
			
			borradorCliente.Telefonos = telefonos
					.stream()
					.filter(borrador->borrador.IdEnrolamiento.equals(borradorEnrolamiento.IdEnrolamiento))
					.collect(Collectors.toList());
			
			borradorCliente.GruposDireccion = gruposDireccion
					.stream()
					.filter(borrador->borrador.IdEnrolamiento.equals(borradorEnrolamiento.IdEnrolamiento))
					.collect(Collectors.toList());
			
			borradorCliente.GruposDireccionDetalle = gruposDireccionDetalle
					.stream()
					.filter(borrador->borrador.IdEnrolamiento.equals(borradorEnrolamiento.IdEnrolamiento))
					.collect(Collectors.toList());
			
			borradorCliente.Indicadores = indicadores
					.stream()
					.filter(borrador->borrador.IdEnrolamiento.equals(borradorEnrolamiento.IdEnrolamiento))
					.collect(Collectors.toList());
			
			borradorCliente.Relaciones = relaciones
					.stream()
					.filter(borrador->borrador.IdEnrolamientoOrigen.equals(borradorEnrolamiento.IdEnrolamiento))
					.collect(Collectors.toList());
			
			borradorCliente.FatcaPaisesResidencia = fatcaPaisesResidencia
					.stream()
					.filter(borrador->borrador.IdEnrolamiento.equals(borradorEnrolamiento.IdEnrolamiento))
					.collect(Collectors.toList());
			
			borradorCliente.CuentasCustodio = cuentasCustodio
					.stream()
					.filter(borrador->borrador.IdEnrolamiento.equals(borradorEnrolamiento.IdEnrolamiento))
					.collect(Collectors.toList());
			
			borradorCliente.PreferenciasPago = preferenciasPago
					.stream()
					.filter(borrador->borrador.IdEnrolamiento.equals(borradorEnrolamiento.IdEnrolamiento))
					.collect(Collectors.toList());
			
			borradorCliente.Fatca = fatcas
					.stream()
					.filter(fatca->fatca.IdEnrolamiento.equals(borradorEnrolamiento.IdEnrolamiento))
					.findFirst()
					.orElse(new Fatca());
			
			borradorCliente.DeclaracionOrigenFondos = declaracionOrigenFondos
													.stream()
													.filter(dof->dof.IdEnrolamiento.equals(borradorEnrolamiento.IdEnrolamiento))
													.peek(dof -> dof.DetalleOrigenFondo = detallePorIdDof.getOrDefault(dof.IdDeclaracionOrigenFondos, new ArrayList<>()))
													.collect(Collectors.toList());
			borradorCliente.NombresFantasia = nombresFantasia
					.stream()
					.filter(borrador->borrador.IdEnrolamiento.equals(borradorEnrolamiento.IdEnrolamiento))
					.collect(Collectors.toList());
			
			borradorCliente.EsPersonaNueva = borradorCliente.Enrolamiento.IdPersona > 0 ? "N" : "S";
			borradorCliente.IdPersona = borradorCliente.Enrolamiento.IdPersona;
			borradorCliente.IdSocioNegocio = borradorCliente.Enrolamiento.IdSocioDeNegocio;
			borradoresCliente.add(borradorCliente);
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConsultarBorradorCliente.obtenerBorradoresCliente", fin - inicio);
		return borradoresCliente;
	}
	
	private List<NombreFantasia> obtenerNombresFantasia(Connection con, HashMap<String, Object> parametros) throws Exception {
		logger.debug("INICIO::ConsultarBorradorCliente.obtenerNombresFantasia");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		
		List<NombreFantasia> nombresFantasia = sql.query(con, SQL_SELECT_NOMBRE_FANTASIA, parametros, NombreFantasia.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarBorradorCliente.obtenerNombresFantasia", fin - inicio);
		return nombresFantasia;
	}

	private HashMap<String, Object> obtenerParametrosBusquedaBorradores(List<BorradorEnrolamiento> borradoresEnrolamiento){
		logger.trace("INICIO::ConsultarBorradorCliente.obtenerParametrosBusquedaBorradores");
		long inicio = System.currentTimeMillis();
		
		List<Long> idsEnrolamientos = borradoresEnrolamiento
				.parallelStream()
				.map(borradorEnrolamiento->borradorEnrolamiento.IdEnrolamiento)
				.distinct()
				.collect(Collectors.toList());
		
		HashMap<String, Object> parametros = new HashMap<String, Object>(2);
		parametros.put("IdEnrolamiento", idsEnrolamientos);
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConsultarBorradorCliente.obtenerParametrosBusquedaBorradores", fin - inicio);
		return parametros;
	}
	
	
	private List<Enrolamiento> obtenerEnrolamientos(Connection con, HashMap<String, Object> parametros) throws Exception{
		logger.debug("INICIO::ConsultarBorradorCliente.obtenerEnrolamientos");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		
		List<Enrolamiento> enrolamientos = sql.query(con, SQL_SELECT_ENROLAMIENTO, parametros, Enrolamiento.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarBorradorCliente.obtenerEnrolamientos", fin - inicio);
		return enrolamientos;
	}
	
	private List<DocumentoIdentidad> obtenerDocumentosIdentidad(Connection con, HashMap<String, Object> parametros) throws Exception{
		logger.debug("INICIO::ConsultarBorradorCliente.obtenerDocumentosIdentidad");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		
		List<DocumentoIdentidad> documentosIdentidad = sql.query(con, SQL_SELECT_DOCUMENTO_IDENTIDAD, parametros, DocumentoIdentidad.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarBorradorCliente.obtenerDocumentosIdentidad", fin - inicio);
		return documentosIdentidad;
	}
	
	private List<CuentaBancaria> obtenerCuentasBancaria(Connection con, HashMap<String, Object> parametros) throws Exception{
		logger.debug("INICIO::ConsultarBorradorCliente.obtenerCuentasBancaria");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		
		List<CuentaBancaria> cuentasBancaria = sql.query(con, SQL_SELECT_CUENTA_BANCARIA, parametros, CuentaBancaria.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarBorradorCliente.obtenerCuentasBancaria", fin - inicio);
		return cuentasBancaria;
	}
	
	private List<TarjetaBancaria> obtenerTarjetasBancaria(Connection con, HashMap<String, Object> parametros) throws Exception{
		logger.debug("INICIO::ConsultarBorradorCliente.obtenerTarjetasBancaria");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		
		List<TarjetaBancaria> tarjetasBancaria = sql.query(con, SQL_SELECT_TARJETA_BANCARIA, parametros, TarjetaBancaria.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearClienteFull.obtenerTarjetasBancaria", fin - inicio);
		return tarjetasBancaria;
	}
	
	private List<DireccionFisica> obtenerDireccionesFisica(Connection con, HashMap<String, Object> parametros) throws Exception{
		logger.debug("INICIO::ConsultarBorradorCliente.obtenerDireccionesFisica");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		
		List<DireccionFisica> direccionesFisica = sql.query(con, SQL_SELECT_DIRECCION_FISICA, parametros, DireccionFisica.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarBorradorCliente.obtenerDireccionesFisica", fin - inicio);
		return direccionesFisica;
	}
	
	private List<DireccionElectronicaEnrolamiento> obtenerDireccionesElectronica(Connection con, HashMap<String, Object> parametros) throws Exception{
		logger.debug("INICIO::ConsultarBorradorCliente.obtenerDireccionesElectronicas");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		
		List<DireccionElectronicaEnrolamiento> direccionesElectronica = sql.query(con, SQL_SELECT_DIRECCION_ELECTRONICA, parametros, DireccionElectronicaEnrolamiento.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearClienteFull.obtenerDireccionesElectronicas", fin - inicio);
		return direccionesElectronica;
	}
	
	private List<Telefono> obtenerTelefonos(Connection con, HashMap<String, Object> parametros) throws Exception{
		logger.debug("INICIO::ConsultarBorradorCliente.obtenerTelefonos");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		
		List<Telefono> telefonos = sql.query(con, SQL_SELECT_TELEFONO, parametros, Telefono.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarBorradorCliente.obtenerTelefonos", fin - inicio);
		return telefonos;
	}
	
	private List<GrupoDireccionEnrolamiento> obtenerGruposDireccion(Connection con, HashMap<String, Object> parametros) throws Exception{
		logger.debug("INICIO::ConsultarBorradorCliente.obtenerGruposDireccion");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		
		List<GrupoDireccionEnrolamiento> gruposDireccion = sql.query(con, SQL_SELECT_GRUPO_DIRECCION, parametros, GrupoDireccionEnrolamiento.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearClienteFull.obtenerGruposDireccion", fin - inicio);
		return gruposDireccion;
	}
	
	private List<GrupoDireccionDetalleEnrolamiento> obtenerGruposDireccionDetalle(Connection con, HashMap<String, Object> parametros) throws Exception{
		logger.debug("INICIO::ConsultarBorradorCliente.obtenerGruposDireccionDetalle");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		
		List<GrupoDireccionDetalleEnrolamiento> gruposDireccionDetalle = sql.query(con, SQL_SELECT_GRUPO_DIRECCION_DETALLE, parametros, GrupoDireccionDetalleEnrolamiento.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarBorradorCliente.obtenerGruposDireccionDetalle", fin - inicio);
		return gruposDireccionDetalle;
	}
	
	private List<Indicador> obtenerIndicadores(Connection con, HashMap<String, Object> parametros) throws Exception{
		logger.debug("INICIO::ConsultarBorradorCliente.obtenerIndicadores");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		
		List<Indicador> indicadores = sql.query(con, SQL_SELECT_INDICADOR, parametros, Indicador.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarBorradorCliente.obtenerIndicadores", fin - inicio);
		return indicadores;
	}
	
	private List<RelacionPersona> obtenerRelaciones(Connection con, HashMap<String, Object> parametros) throws Exception{
		logger.debug("INICIO::ConsultarBorradorCliente.obtenerRelaciones");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		
		List<RelacionPersona> relaciones = sql.query(con, SQL_SELECT_RELACION, parametros, RelacionPersona.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarBorradorCliente.obtenerRelaciones", fin - inicio);
		return relaciones;
	}
	
	private List<Fatca> obtenerFatcas(Connection con, HashMap<String, Object> parametros) throws Exception{
		logger.debug("INICIO::ConsultarBorradorCliente.obtenerFatcas");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		
		List<Fatca> fatcas = sql.query(con, SQL_SELECT_FATCA, parametros, Fatca.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarBorradorCliente.obtenerFatcas", fin - inicio);
		return fatcas;
	}
	
	private List<FatcaPaisResidencia> obtenerFatcaPaisesResidencia(Connection con, HashMap<String, Object> parametros) throws Exception{
		logger.debug("INICIO::ConsultarBorradorCliente.obtenerFatcaPaisesResidencia");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		
		List<FatcaPaisResidencia> fatcaPaisesResidencia = sql.query(con, SQL_SELECT_FATCA_PAIS_RESIDENCIA, parametros, FatcaPaisResidencia.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarBorradorCliente.obtenerFatcaPaisesResidencia", fin - inicio);
		return fatcaPaisesResidencia;
	}
	
	private List<CuentaCustodioPortafolio> obtenerCuentasCustodio(Connection con, HashMap<String, Object> parametros) throws Exception{
		logger.debug("INICIO::ConsultarBorradorCliente.obtenerCuentasCustodio");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		
		List<CuentaCustodioPortafolio> cuentasCustodio = sql.query(con, SQL_SELECT_CUENTA_CUSTODIO, parametros, CuentaCustodioPortafolio.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarBorradorCliente.obtenerCuentasCustodio", fin - inicio);
		return cuentasCustodio;
	}
	
	private List<PreferenciaPagoPortafolio> obtenerPreferenciasPago(Connection con, HashMap<String, Object> parametros) throws Exception{
		logger.debug("INICIO::ConsultarBorradorCliente.obtenerPreferenciasPago");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		
		List<PreferenciaPagoPortafolio> preferenciasPago = sql.query(con, SQL_SELECT_PREFERENCIA_PAGO, parametros, PreferenciaPagoPortafolio.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarBorradorCliente.obtenerPreferenciasPago", fin - inicio);
		return preferenciasPago;
	}
	
	private Map<Long, DeclaracionOrigenFondos> obtenerDeclaracionOrigenFondos(Connection con, HashMap<String, Object> parametros) throws Exception{
		logger.debug("INICIO::ConsultarBorradorCliente.obtenerDeclaracionOrigenFondos");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		
		Map<Long, DeclaracionOrigenFondos> declaracionOrigenFondos = sql.query(con, SQL_SELECT_DECLARACION_ORIGEN_FONDOS, parametros, DeclaracionOrigenFondos.class)
																.stream()
																.collect(Collectors.toMap(dof -> dof.IdDeclaracionOrigenFondos, dof -> dof));
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarBorradorCliente.obtenerDeclaracionOrigenFondos", fin - inicio);
		return declaracionOrigenFondos;
	}
	
	private Map<Long, List<DetalleOrigenFondo>> obtenerDetalleDeclaracionOrigenFondos(Connection con, List<Long> idsDeclaracionOrigenFondos) throws Exception{
		logger.debug("INICIO::ConsultarBorradorCliente.obtenerDetalleDeclaracionOrigenFondos");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		
		HashMap<String, Object> parametros = new HashMap<>(2);
		parametros.put("IdsDeclaracionOrigenFondos", idsDeclaracionOrigenFondos);
		
		Map<Long, List<DetalleOrigenFondo>> detalleDeclaracionOrigenFondos = sql.query(con, SQL_SELECT_DETALLE_DECLARACION_ORIGEN_FONDOS, parametros, DetalleOrigenFondo.class)
																			.stream()
																			.collect(Collectors.groupingBy(ddof -> ddof.IdDeclaracionOrigenFondos));
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarBorradorCliente.obtenerDetalleDeclaracionOrigenFondos", fin - inicio);
		return detalleDeclaracionOrigenFondos;
	}
	
	private List<Validacion> validarBorradoresCliente(Connection con, Long idEmpresa, List<DatosClienteDatosGenerales> borradoresCliente) throws Exception{
		logger.trace("INICIO::CrearClienteFull.validarEnrolamientos");
		long inicio = System.currentTimeMillis();
		
		List<Validacion> validaciones = new ArrayList<>();
		for(DatosClienteDatosGenerales borradorCliente : borradoresCliente) {
			if(borradorCliente.Enrolamiento.IdEnrolamiento == null) {
				Validacion validacion = new Validacion();
				validacion.EsError = true;
				validacion.MensajeError = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_BORRADOR_ENROLAMIENTO_NO_EXISTE", new HashMap<String,String>(1), con, idEmpresa);
				validaciones.add(validacion);
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearClienteFull.validarEnrolamientos", fin - inicio);
		return validaciones;
	}
}
