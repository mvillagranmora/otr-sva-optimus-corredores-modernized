package cl.bcs.socionegocio.enr;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.enr.CrearBorradorCuentaBancaria.EntradaCrearBorradorCuentaBancaria;
import cl.bcs.socionegocio.enr.CrearBorradorDireccionElectronica.EntradaCrearBorradorDireccionElectronica;
import cl.bcs.socionegocio.enr.CrearBorradorDireccionFisica.EntradaCrearBorradorDireccionFisica;
import cl.bcs.socionegocio.enr.CrearBorradorDocumentoIdentidad.EntradaCrearBorradorDocumentoIdentidad;
import cl.bcs.socionegocio.enr.CrearBorradorFatca.EntradaCrearBorradorFatca;
import cl.bcs.socionegocio.enr.CrearBorradorGrupoDireccion.EntradaCrearBorradorGrupoDireccion;
import cl.bcs.socionegocio.enr.CrearBorradorGrupoDireccionDetalle.EntradaCrearBorradorGrupoDireccionDetalle;
import cl.bcs.socionegocio.enr.CrearBorradorIndicador.EntradaCrearBorradorIndicador;
import cl.bcs.socionegocio.enr.CrearBorradorRelacionPersona.EntradaCrearBorradorRelacionPersona;
import cl.bcs.socionegocio.enr.CrearBorradorTarjetaBancaria.EntradaCrearBorradorTarjetaBancaria;
import cl.bcs.socionegocio.enr.CrearBorradorTelefono.EntradaCrearBorradorTelefono;
import cl.bcs.socionegocio.enr.pojo.CuentaBancaria;
import cl.bcs.socionegocio.enr.pojo.DireccionElectronica;
import cl.bcs.socionegocio.enr.pojo.DireccionFisica;
import cl.bcs.socionegocio.enr.pojo.DocumentoIdentidad;
import cl.bcs.socionegocio.enr.pojo.Enrolamiento;
import cl.bcs.socionegocio.enr.pojo.Fatca;
import cl.bcs.socionegocio.enr.pojo.GrupoDireccion;
import cl.bcs.socionegocio.enr.pojo.GrupoDireccionDetalle;
import cl.bcs.socionegocio.enr.pojo.Indicador;
import cl.bcs.socionegocio.enr.pojo.RelacionPersona;
import cl.bcs.socionegocio.enr.pojo.ResultadoAdministrarBorradorEntidad;
import cl.bcs.socionegocio.enr.pojo.SalidaAdministrarBorrador;
import cl.bcs.socionegocio.enr.pojo.TarjetaBancaria;
import cl.bcs.socionegocio.enr.pojo.Telefono;
import cl.bcs.socionegocio.soc.SocioNegocioUtils.TipoSegmento;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearBorradorEnrolamientoDatosPersona implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(CrearBorradorEnrolamientoDatosPersona.class);
	
	private final String SQL_SELECT_PERSONA = loadResourceToString("cl/bcs/socionegocio/enr/sql/CrearBorradorEnrolamientoDatosPersona_SelectDatosPersona.sql");
	private final String SQL_SELECT_DOCUMENTO_IDENTIDAD = loadResourceToString("cl/bcs/socionegocio/enr/sql/CrearBorradorEnrolamientoDatosPersona_SelectDocumentoIdentidad.sql");
	private final String SQL_SELECT_DIRECCION_FISICA = loadResourceToString("cl/bcs/socionegocio/enr/sql/CrearBorradorEnrolamientoDatosPersona_SelectDireccionFisicas.sql");
	private final String SQL_SELECT_DIRECCION_ELECTRONICA = loadResourceToString("cl/bcs/socionegocio/enr/sql/CrearBorradorEnrolamientoDatosPersona_SelectDireccionElectronica.sql");
	private final String SQL_SELECT_TELEFONO = loadResourceToString("cl/bcs/socionegocio/enr/sql/CrearBorradorEnrolamientoDatosPersona_SelectTelefono.sql");
	private final String SQL_SELECT_TARJETA_BANCARIA = loadResourceToString("cl/bcs/socionegocio/enr/sql/CrearBorradorEnrolamientoDatosPersona_SelectTarjetaBancaria.sql");
	private final String SQL_SELECT_CUENTA_BANCARIA = loadResourceToString("cl/bcs/socionegocio/enr/sql/CrearBorradorEnrolamientoDatosPersona_SelectCuentaBancaria.sql");
	private final String SQL_SELECT_RELACION_PERSONA = loadResourceToString("cl/bcs/socionegocio/enr/sql/CrearBorradorEnrolamientoDatosPersona_SelectRelacionPersona.sql");
	private final String SQL_SELECT_GRUPO_DIRECCION = loadResourceToString("cl/bcs/socionegocio/enr/sql/CrearBorradorEnrolamientoDatosPersona_SelectGrupoDireccion.sql");
	private final String SQL_SELECT_GRUPO_DIRECCION_DETALLE = loadResourceToString("cl/bcs/socionegocio/enr/sql/CrearBorradorEnrolamientoDatosPersona_SelectGrupoDireccionDetalle.sql");
	private final String SQL_SELECT_INDICADOR = loadResourceToString("cl/bcs/socionegocio/enr/sql/CrearBorradorEnrolamientoDatosPersona_SelectIndicador.sql");
	private final String SQL_SELECT_FATCA = loadResourceToString("cl/bcs/socionegocio/per/sql/ModificarPersonaFatca_SelectFatca.sql");
	private final String SQL_INSERT_ENROLAMIENTO_BORRADOR = loadResourceToString("cl/bcs/socionegocio/enr/sql/CrearBorradorEnrolamientoDatosPersona_InsertEnrolamientoBorrador.sql");
	private final String SQL_CONSULTARSEGMENTOS	= loadResourceToString("cl/bcs/socionegocio/cli/sql/CrearCliente_SelectSegmentos.sql");
	
	public static class EntradaCrearBorradorEnrolamientoDatosPersona extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public List<Persona> p_Personas;
		
		public EntradaCrearBorradorEnrolamientoDatosPersona() {
			this.p_Personas = new ArrayList<>();
		}
	}
	
	public static class Persona extends BcsPojo {
		public Long IdInterno;
		public Long IdPersona;
		public String IdTipoEntidadDestino;
		public Long IdEntidadEnrolada;
	}
	
	public static class DatoPersona extends BcsPojo {
		public Long IdPersona;
		public String Tipo;
		public String Nombres;
		public String ApellidoPaterno;
		public String ApellidoMaterno;
		public String RazonSocial;
		public Long IdPaisNacimiento;
		public java.sql.Date FechaNacimiento;
		public String Sexo;
		public String EstadoCivil;
		public Long IdPersonaConyuge;
		public String RegimenConyugal;
		public Long IdDocumentoIdentidad;
		public Long IdDocumentoIdentidadAdicional;
		public Long IdDireccionFisicaPrincipal;
		public Long IdDireccionElectronicaPrincipal;
		public Long IdTelefonoPrincipal;
		public Long IdGradoInstruccion;
		public Long IdRangoPatrimonio;
		public String IdTransparenciaFiscal;
		public Long IdGiroCIIU;
		public BigDecimal IngresoPromedioMensual;
		public Integer AgnosServicio;
		public java.sql.Timestamp FechaInicioActividad;
		public String GlobalLegalEntity;
		public String CodigoTipoCargoEmpleado;
		public String DetalleCargo;
		public Long IdPersonaEmpleador;
		public Long IdPersonaRepresentante;
		public Long IdProfesion;
		public Long IdPaisNacionalidadSecundaria;
		public Long IdPaisNacionalidadPrincipal;
		public Long IdPaisResidenciaSecundaria;
		public Long IdPaisResidencia;
		public Long IdGiroPrincipal;
		public String EsExtranjero;
		public String ResidenciaTributariaExtranjera;
		public String IdTipoCanalIngresoCorredor;
	}
	
	public static class DatoDocumentoIdentidad extends BcsPojo {
		public Long IdDocumentoIdentidad;
		public Long IdPersona;
		public String DocumentoIdentidadAsociado;
		public String IdTipoDocumentoIdentidad;
		public String DocumentoIdentidad;
		public String NumeroSerie;
		public java.sql.Date FechaEmision;
		public java.sql.Date FechaVencimiento;
		public String IdTipoDocumentoIdentidadAsociado;
		public String TipoPersona;
		public String Predeterminado;
	}
	
	public static class DatoDireccionFisica extends BcsPojo {
		public Long IdDireccionFisica;
		public Long IdPersona;
		public String Calle;
		public String Numero;
		public String Departamento;
		public String Sector;
		public String CodigoPostal;
		public Long IdCiudad;
		public Long IdDivisionPais;
		public String CodigoProposito;
		public String Comentario;
		public String CodigoCompuesto;
		public String Predeterminado;
	}
	
	public static class DatoDireccionElectronica extends BcsPojo {
		public Long IdDireccionElectronica;	
		public Long IdPersona;
		public String Direccion;
		public String CodigoProposito;
		public String Comentario;
		public String Predeterminado;
	}
	
	public static class DatoTelefono extends BcsPojo {
		public Long IdTelefono;
		public Long IdPersona;
		public Long IdPais;
		public String IdTipoTelefono;
		public String Numero;
		public String Anexo;
		public String CodigoProposito;
		public String Comentario;
		public String Predeterminado;
	}
	
	public static class DatoTarjetaBancaria extends BcsPojo {
		public Long IdTarjetaBancaria;
		public Long IdPersona;
		public Long IdBanco;
		public String IdTipoTarjetaBancaria;
		public String NumeroTarjetaBancaria;
		public java.sql.Date FechaVencimiento;
		public Integer CodigoVerificacion;
	}
	
	public static class DatoCuentaBancaria extends BcsPojo {
		public Long IdCuentaBancaria;
	    public Long IdBanco;
	    public String IdTipoCuentaBancaria;
	    public String IdMoneda;
	    public String NumeroCuentaBancaria;
	    public Long IdPersona;
	    public Long IdPlaza;
	    public String IdTipoIdentificadorCuenta;
	    public String IdentificadorCuenta;
	    public Long IdPersonaRelacionada;
	    public String CodigoCuentaInterbancario;
	    public String CodigoAba;
	    public String OtroCodigoBancoExtranjero;
	    public String CodigoIban;
	    public String OtroCodigoCuentaExtranjero;
	    public String EsPac;
	    public String Predeterminado;
	}
	
	public static class DatoRelacionPersona extends BcsPojo {
		public Long IdRelacionPersona;
		public Long IdPersonaOrigen;
		public String IdTipoRelacion;
		public Long IdPersonaDestino;
		public java.sql.Timestamp FechaInicioRelacion;
		public java.sql.Timestamp FechaFinRelacion;
		public BigDecimal PorcentajeParticipacion;
		public String RetiraDocumentos;
		public String FinalidadRelacion;
		public String CondicionesRelacion;
		public Long IdGradoVinculacionParental;
		public Long IdCiudad;
		public String Calle;
		public String Numero;
		public String Email;
		public String Telefono;
		public String Complemento;
		public String Ciudad;
	}
	
	public static class DatoGrupoDireccion extends BcsPojo {
		public Long   IdPersona;
	    public Long   IdGrupoDireccion;
	    public String Grupo;
	    public String Comentario;
	}
	
	public static class DatoGrupoDireccionDetalle extends BcsPojo {
		public Long   IdGrupoDireccionDetalle;
	    public Long   IdDireccionElectronica;
	    public String IdTipoDestinatario;
	    public Long   IdGrupoDireccion;
	}
	
	public static class DatoIndicador extends BcsPojo {
		public Long IdIndicador;
		public Long IdPersona;
		public String IdTipoEntidad;
		public Long IdTipoIndicador;
		public String ModeloCliente;
		public Long IdEmpresa;
		public String ValorIndicador;
	}

	public static class DatoFatca extends BcsPojo {
		public Long IdPersona;
		public Long IdEnrolamiento;
		public String IdClasificacionFatca;
		public String IdClasificacionEmpresaFatca;
		public String NumeroGIIN;
		public java.sql.Timestamp FechaFirmaFormulario;
		public String AutorizacionInformarOrganismo;
		public String ExcepcionResidente;
		public String ExcepcionNacimiento;
		public String ExcepcionDomicilio;
		public String ExcepcionTelefono;
		public String ExcepcionOrdenPermanente;
		public String ExcepcionFirma;
		public String ParticipacionSociedadesUSA;
		public String OrdenPermanente;
		public String NACEEUU;
		public String NACEEUUExplicacion;
		public String EntidadFinanciera;
		public String TipoInstitucionFinanciera;
		public String EntidadGestionada;
		public String IngresosPasivos;
		public String GenerarIngresosPasivos;
		public String CategoriaEntidad;
		public String GIINEntidad;
		public String RazonSocial;
		public java.sql.Date FechaLiquidacion;
		public String NombreMercado;
		public java.sql.Date FechaConstitucion;
		public String ObligacionesFiscales;
		public String ObligacionesFiscalesExplicacion;
		public String IndividuoControl;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaCrearBorradorEnrolamientoDatosPersona jin = gson.fromJson(in.toString(), EntradaCrearBorradorEnrolamientoDatosPersona.class);
		SalidaAdministrarBorrador out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaAdministrarBorrador logica(Connection con, EntradaCrearBorradorEnrolamientoDatosPersona entrada) throws Exception {
		logger.info("INICIO::CrearBorradorEnrolamientoDatosPersona");
		long inicioServicio = System.currentTimeMillis();
		SalidaAdministrarBorrador out = new SalidaAdministrarBorrador();
		
		List<Long> idsPersonas = obtenerIdsPersonas(entrada.p_Personas);
		
		List<DatoPersona> datosPersonas = obtenerDatosPersonas(con, idsPersonas);
		List<DatoDocumentoIdentidad> datosDocumentosIdentidades = obtenerDatosDocumentosIdentidades(con, idsPersonas);
		List<DatoDireccionFisica> datosDireccionesFisicas = obtenerDatosDireccionesFisicas(con, idsPersonas);
		List<DatoDireccionElectronica> datosDireccionesElectronicas = obtenerDatosDireccionesElectronicas(con, idsPersonas);
		List<DatoTelefono> datosTelefono = obtenerDatosTelefonos(con, idsPersonas);
		List<DatoTarjetaBancaria> datosTarjetasBancarias = obtenerDatosTarjetasBancarias(con, idsPersonas);
		List<DatoCuentaBancaria> datosCuentasBancarias = obtenerDatosCuentasBancarias(con, idsPersonas);
		List<DatoRelacionPersona> datosRelacionesPersonas = obtenerDatosRelacionesPersonas(con, idsPersonas);
		List<DatoGrupoDireccion> datosGruposDirecciones = obtenerDatosGruposDirecciones(con, idsPersonas);
		List<DatoIndicador> datosIndicadores = obtenerDatosIndicadores(con, idsPersonas);
		List<DatoFatca> datosFatcas = obtenerDatosFatca(con, idsPersonas);
		List<Long> idsGruposDirecciones = obtenerIdsGruposDirecciones(datosGruposDirecciones);
		List<DatoGrupoDireccionDetalle> datosGruposDireccionesDetalles = obtenerDatosGruposDireccionesDetalles(con, idsGruposDirecciones);
		
		List<Enrolamiento> enrolamientos = obtenerEntidadesEnrolamiento(con, datosPersonas, entrada);
		List<DocumentoIdentidad> documentosIdentidades = obtenerEntidadesDocumentosIdentidades(enrolamientos, datosDocumentosIdentidades);
		List<DireccionFisica> direccionesFisicas = obtenerEntidadesDireccionesFisicas(enrolamientos, datosDireccionesFisicas);
		List<DireccionElectronica> direccionesElectronicas = obtenerEntidadesDireccionesElectronicas(enrolamientos, datosDireccionesElectronicas);
		List<Telefono> telefonos = obtenerEntidadesTelefonos(enrolamientos, datosTelefono);
		List<TarjetaBancaria> tarjetasBancarias = obtenerEntidadesTarjetasBancarias(enrolamientos, datosTarjetasBancarias);
		List<CuentaBancaria> cuentasBancarias = obtenerEntidadesCuentasBancarias(enrolamientos, datosCuentasBancarias);
		List<RelacionPersona> relacionesPersona = obtenerEntidadesRelacionesPersonas(enrolamientos, datosRelacionesPersonas);
		List<GrupoDireccion> gruposDirecciones = obtenerEntidadesGruposDirecciones(enrolamientos, datosGruposDirecciones);
		List<Indicador> indicadores = obtenerEntidadesIndicadores(enrolamientos, datosIndicadores);
		List<Fatca> fatca = obtenerEntidadesFatcas(enrolamientos, datosFatcas);
		
		SalidaAdministrarBorrador salidaCrearDocumentoIdentidad = crearDocumentoIdentidad(con, entrada, documentosIdentidades);
		SalidaAdministrarBorrador salidaCrearDireccionFisica = crearDireccionFisica(con, entrada, direccionesFisicas);
		SalidaAdministrarBorrador salidaCrearDireccionElectronica = crearDireccionElectronica(con, entrada, direccionesElectronicas);
		SalidaAdministrarBorrador salidaCrearTelefono = crearTelefono(con, entrada, telefonos);
		SalidaAdministrarBorrador salidaCrearTarjetaBancaria = crearTarjetaBancaria(con, entrada, tarjetasBancarias);
		SalidaAdministrarBorrador salidaCrearCuentaBancaria = crearCuentaBancaria(con, entrada, cuentasBancarias);
		SalidaAdministrarBorrador salidaCrearRelacionPersona = crearRelacionPersona(con, entrada, relacionesPersona);
		SalidaAdministrarBorrador salidaCrearIndicador = crearIndicador(con, entrada, indicadores);
		SalidaAdministrarBorrador salidaCrearFatca = crearFatca(con, entrada, fatca);
		SalidaAdministrarBorrador salidaCrearGrupoDireccion = crearGrupoDireccion(con, entrada, gruposDirecciones);
		
		List<GrupoDireccionDetalle> gruposDireccionesDetalles = obtenerEntidadesGrupoIndicadoresDetalle(datosGruposDireccionesDetalles, salidaCrearDireccionElectronica.P_OUT_REFID, salidaCrearGrupoDireccion.P_OUT_REFID);
		SalidaAdministrarBorrador salidaCrearGrupoDireccionDetalle = crearGrupoDireccionDetalle(con, entrada, gruposDireccionesDetalles);
		
		enrolamientos = actualizarIdDocumentoIdentidad(enrolamientos, salidaCrearDocumentoIdentidad.P_OUT_REFID, datosPersonas);
		enrolamientos = actualizarIdDocumentoIdentidadAdicional(enrolamientos, salidaCrearDocumentoIdentidad.P_OUT_REFID, datosPersonas);
		enrolamientos = actualizarIdDireccionFisicaPrincipal(enrolamientos, salidaCrearDireccionFisica.P_OUT_REFID, datosPersonas);
		enrolamientos = actualizarIdDireccionElectronicaPrincipal(enrolamientos, salidaCrearDireccionElectronica.P_OUT_REFID, datosPersonas);
		enrolamientos = actualizarIdTelefonoPrincipal(enrolamientos, salidaCrearTelefono.P_OUT_REFID, datosPersonas);
		
		out.agregarErroresMensajesSalida(salidaCrearDocumentoIdentidad);		
		out.agregarErroresMensajesSalida(salidaCrearDireccionFisica);
		out.agregarErroresMensajesSalida(salidaCrearDireccionElectronica);
		out.agregarErroresMensajesSalida(salidaCrearTelefono);
		out.agregarErroresMensajesSalida(salidaCrearTarjetaBancaria);
		out.agregarErroresMensajesSalida(salidaCrearCuentaBancaria);
		out.agregarErroresMensajesSalida(salidaCrearRelacionPersona);
		out.agregarErroresMensajesSalida(salidaCrearGrupoDireccion);
		out.agregarErroresMensajesSalida(salidaCrearIndicador);
		out.agregarErroresMensajesSalida(salidaCrearGrupoDireccionDetalle);
		out.agregarErroresMensajesSalida(salidaCrearFatca);
		
		insertarEnrolamiento(con, enrolamientos);
		
		out.P_OUT_REFID = obtenerIdsInsertados(enrolamientos);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearBorradorEnrolamientoDatosPersona", finServicio - inicioServicio);
		return out;
	}
	
	private List<Long> obtenerIdsPersonas(List<Persona> personas){
		logger.trace("INICIO::CrearBorradorEnrolamientoDatosPersona.obtenerIdsPersonas");
		long inicio = System.currentTimeMillis();
		List<Long> idsPersona = personas.parallelStream().map(persona->persona.IdPersona).collect(Collectors.toList());
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearBorradorEnrolamientoDatosPersona.obtenerIdsPersonas", fin - inicio);
		return idsPersona;
	}
	
	private List<DatoPersona> obtenerDatosPersonas(Connection con, List<Long> idsPersonas) throws Exception {
		logger.debug("INICIO::CrearBorradorEnrolamientoDatosPersona.obtenerDatosPersonas");
		long inicio = System.currentTimeMillis();
		
		HashMap<String, Object> parametros = new HashMap<String, Object>(1);
		parametros.put("IdPersona", idsPersonas);
		
		SQL sql = new SQL();
		List<DatoPersona> datosPersonas = sql.query(con, SQL_SELECT_PERSONA, parametros, DatoPersona.class);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearBorradorEnrolamientoDatosPersona.obtenerDatosPersonas", fin - inicio);
		return datosPersonas;
	}
	
	private List<DatoDocumentoIdentidad> obtenerDatosDocumentosIdentidades(Connection con, List<Long> idsPersonas) throws Exception {
		logger.debug("INICIO::CrearBorradorEnrolamientoDatosPersona.obtenerDatosDocumentosIdentidades");
		long inicio = System.currentTimeMillis();
		
		HashMap<String, Object> parametros = new HashMap<String, Object>(1);
		parametros.put("IdPersona", idsPersonas);
		
		SQL sql = new SQL();
		List<DatoDocumentoIdentidad> datosDocumentosIdentidades = sql.query(con, SQL_SELECT_DOCUMENTO_IDENTIDAD, parametros, DatoDocumentoIdentidad.class);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearBorradorEnrolamientoDatosPersona.obtenerDatosDocumentosIdentidades", fin - inicio);
		return datosDocumentosIdentidades;
	}
	
	private List<DatoDireccionFisica> obtenerDatosDireccionesFisicas(Connection con, List<Long> idsPersonas) throws Exception {
		logger.debug("INICIO::CrearBorradorEnrolamientoDatosPersona.obtenerDatosDireccionesFisicas");
		long inicio = System.currentTimeMillis();
		
		HashMap<String, Object> parametros = new HashMap<String, Object>(1);
		parametros.put("IdPersona", idsPersonas);
		
		SQL sql = new SQL();
		List<DatoDireccionFisica> datosDireccionesFisicas = sql.query(con, SQL_SELECT_DIRECCION_FISICA, parametros, DatoDireccionFisica.class);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearBorradorEnrolamientoDatosPersona.obtenerDatosDireccionesFisicas", fin - inicio);
		return datosDireccionesFisicas;
	}
	
	private List<DatoDireccionElectronica> obtenerDatosDireccionesElectronicas(Connection con, List<Long> idsPersonas) throws Exception {
		logger.debug("INICIO::CrearBorradorEnrolamientoDatosPersona.obtenerDatosDireccionesElectronicas");
		long inicio = System.currentTimeMillis();
		
		HashMap<String, Object> parametros = new HashMap<String, Object>(1);
		parametros.put("IdPersona", idsPersonas);
		
		SQL sql = new SQL();
		List<DatoDireccionElectronica> datosDireccionesElectronicas = sql.query(con, SQL_SELECT_DIRECCION_ELECTRONICA, parametros, DatoDireccionElectronica.class);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearBorradorEnrolamientoDatosPersona.obtenerDatosDireccionesElectronicas", fin - inicio);
		return datosDireccionesElectronicas;
	}
	
	private List<DatoTelefono> obtenerDatosTelefonos(Connection con, List<Long> idsPersonas) throws Exception {
		logger.debug("INICIO::CrearBorradorEnrolamientoDatosPersona.obtenerDatosTelefonos");
		long inicio = System.currentTimeMillis();
		
		HashMap<String, Object> parametros = new HashMap<String, Object>(1);
		parametros.put("IdPersona", idsPersonas);
		
		SQL sql = new SQL();
		List<DatoTelefono> datosTelefonos = sql.query(con, SQL_SELECT_TELEFONO, parametros, DatoTelefono.class);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearBorradorEnrolamientoDatosPersona.obtenerDatosTelefonos", fin - inicio);
		return datosTelefonos;
	}
	
	private List<DatoTarjetaBancaria> obtenerDatosTarjetasBancarias(Connection con, List<Long> idsPersonas) throws Exception {
		logger.debug("INICIO::CrearBorradorEnrolamientoDatosPersona.obtenerDatosTarjetasBancarias");
		long inicio = System.currentTimeMillis();
		
		HashMap<String, Object> parametros = new HashMap<String, Object>(1);
		parametros.put("IdPersona", idsPersonas);
		
		SQL sql = new SQL();
		List<DatoTarjetaBancaria> datosTarjetasBancarias = sql.query(con, SQL_SELECT_TARJETA_BANCARIA, parametros, DatoTarjetaBancaria.class);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearBorradorEnrolamientoDatosPersona.obtenerDatosTarjetasBancarias", fin - inicio);
		return datosTarjetasBancarias;
	}
	
	private List<DatoCuentaBancaria> obtenerDatosCuentasBancarias(Connection con, List<Long> idsPersonas) throws Exception {
		logger.debug("INICIO::CrearBorradorEnrolamientoDatosPersona.obtenerDatosCuentasBancarias");
		long inicio = System.currentTimeMillis();
		
		HashMap<String, Object> parametros = new HashMap<String, Object>(1);
		parametros.put("IdPersona", idsPersonas);
		
		SQL sql = new SQL();
		List<DatoCuentaBancaria> datosCuentasBancarias = sql.query(con, SQL_SELECT_CUENTA_BANCARIA, parametros, DatoCuentaBancaria.class);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearBorradorEnrolamientoDatosPersona.obtenerDatosCuentasBancarias", fin - inicio);
		return datosCuentasBancarias;
	}
	
	private List<DatoRelacionPersona> obtenerDatosRelacionesPersonas(Connection con, List<Long> idsPersonas) throws Exception {
		logger.debug("INICIO::CrearBorradorEnrolamientoDatosPersona.obtenerDatosRelacionesPersonas");
		long inicio = System.currentTimeMillis();
		
		HashMap<String, Object> parametros = new HashMap<String, Object>(1);
		parametros.put("IdPersona", idsPersonas);
		
		SQL sql = new SQL();
		List<DatoRelacionPersona> datosRelacionesPersonas = sql.query(con, SQL_SELECT_RELACION_PERSONA, parametros, DatoRelacionPersona.class);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearBorradorEnrolamientoDatosPersona.obtenerDatosRelacionesPersonas", fin - inicio);
		return datosRelacionesPersonas;
	}
	
	private List<DatoGrupoDireccion> obtenerDatosGruposDirecciones(Connection con, List<Long> idsPersonas) throws Exception {
		logger.debug("INICIO::CrearBorradorEnrolamientoDatosPersona.obtenerDatosGruposDirecciones");
		long inicio = System.currentTimeMillis();
		
		HashMap<String, Object> parametros = new HashMap<String, Object>(1);
		parametros.put("IdPersona", idsPersonas);
		
		SQL sql = new SQL();
		List<DatoGrupoDireccion> datosGruposDirecciones = sql.query(con, SQL_SELECT_GRUPO_DIRECCION, parametros, DatoGrupoDireccion.class);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearBorradorEnrolamientoDatosPersona.obtenerDatosGruposDirecciones", fin - inicio);
		return datosGruposDirecciones;
	}
	
	private List<DatoIndicador> obtenerDatosIndicadores(Connection con, List<Long> idsPersonas) throws Exception {
		logger.debug("INICIO::CrearBorradorEnrolamientoDatosPersona.obtenerDatosIndicadores");
		long inicio = System.currentTimeMillis();
		
		HashMap<String, Object> parametros = new HashMap<String, Object>(1);
		parametros.put("IdPersona", idsPersonas);
		
		SQL sql = new SQL();
		List<DatoIndicador> datosIndicadores = sql.query(con, SQL_SELECT_INDICADOR, parametros, DatoIndicador.class);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearBorradorEnrolamientoDatosPersona.obtenerDatosIndicadores", fin - inicio);
		return datosIndicadores;
	}
	
	private List<Long> obtenerIdsGruposDirecciones(List<DatoGrupoDireccion> gruposDirecciones){
		logger.debug("INICIO::CrearBorradorEnrolamientoDatosPersona.obtenerIdsGruposDirecciones");
		long inicio = System.currentTimeMillis();
		List<Long> idsGruposDirecciones = gruposDirecciones.parallelStream().map(grupoDireccion->grupoDireccion.IdGrupoDireccion).collect(Collectors.toList());
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearBorradorEnrolamientoDatosPersona.obtenerIdsGruposDirecciones", fin - inicio);
		return idsGruposDirecciones;
	}
	
	private List<DatoGrupoDireccionDetalle> obtenerDatosGruposDireccionesDetalles(Connection con, List<Long> idsGruposDirecciones) throws Exception {
		logger.debug("INICIO::CrearBorradorEnrolamientoDatosPersona.obtenerDatosGruposDireccionesDetalles");
		long inicio = System.currentTimeMillis();
		
		HashMap<String, Object> parametros = new HashMap<String, Object>(1);
		parametros.put("IdGrupoDireccion", idsGruposDirecciones);
		
		SQL sql = new SQL();
		List<DatoGrupoDireccionDetalle> datosGruposDireccionesDetalles = sql.query(con, SQL_SELECT_GRUPO_DIRECCION_DETALLE, parametros, DatoGrupoDireccionDetalle.class);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearBorradorEnrolamientoDatosPersona.obtenerDatosGruposDireccionesDetalles", fin - inicio);
		return datosGruposDireccionesDetalles;
	}
	
	private List<DatoFatca> obtenerDatosFatca(Connection con, List<Long> idsPersonas) throws Exception {
		logger.debug("INICIO::CrearBorradorEnrolamientoDatosPersona.obtenerDatosFatca");
		long inicio = System.currentTimeMillis();
		
		HashMap<String, Object> parametros = new HashMap<String, Object>(1);
		parametros.put("IdPersona", idsPersonas);
		
		SQL sql = new SQL();
		List<DatoFatca> datosFatca = sql.query(con, SQL_SELECT_FATCA, parametros, DatoFatca.class);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearBorradorEnrolamientoDatosPersona.obtenerDatosFatca", fin - inicio);
		return datosFatca;
	}
	
	private List<Enrolamiento> obtenerEntidadesEnrolamiento(Connection con, List<DatoPersona> datosPersona, EntradaCrearBorradorEnrolamientoDatosPersona entrada) throws Exception {
		logger.debug("INICIO::CrearBorradorEnrolamientoDatosPersona.obtenerEntidadesEnrolamiento");
		long inicio = System.currentTimeMillis();
		
		List<Long> secuencias = Utils.getSecuencia(con, "BSCL.SocioNegocio.BD.Secuencias::BorradorEnrolamiento", Long.valueOf(datosPersona.size()));
		
		AtomicInteger index = new AtomicInteger();
		
		Map<Long, Persona> personasPorIdPersona = entrada.p_Personas.parallelStream().collect(Collectors.toMap(persona->persona.IdPersona, persona->persona));
		
		List<Enrolamiento> enrolamientos = datosPersona.parallelStream().map(datoPersona->{
			Persona persona = personasPorIdPersona.get(datoPersona.IdPersona);
			Enrolamiento enrolamiento = new Enrolamiento();
			enrolamiento.IdInterno = persona.IdInterno;
			enrolamiento.IdEnrolamiento = secuencias.get(index.getAndIncrement());
			enrolamiento.IdEmpresa = entrada.p_IdEmpresa;
			enrolamiento.IdUsuario = entrada.p_IdUsuario;
			enrolamiento.IdTipoEntidad = persona.IdTipoEntidadDestino;
			enrolamiento.IdEntidadEnrolada = persona.IdEntidadEnrolada;
			enrolamiento.IdPersona = datoPersona.IdPersona;
			enrolamiento.Tipo = datoPersona.Tipo;
			enrolamiento.Nombres = datoPersona.Nombres;
			enrolamiento.ApellidoPaterno = datoPersona.ApellidoPaterno;
			enrolamiento.ApellidoMaterno = datoPersona.ApellidoMaterno;
			enrolamiento.RazonSocial = datoPersona.RazonSocial;
			enrolamiento.IdPaisNacimiento = datoPersona.IdPaisNacimiento;
			enrolamiento.FechaNacimiento = datoPersona.FechaNacimiento;
			enrolamiento.Sexo = datoPersona.Sexo;
			enrolamiento.EstadoCivil = datoPersona.EstadoCivil;
			enrolamiento.IdPersonaConyuge = datoPersona.IdPersonaConyuge;
			enrolamiento.RegimenConyugal = datoPersona.RegimenConyugal;
			enrolamiento.IdGradoInstruccion = datoPersona.IdGradoInstruccion;
			enrolamiento.IdRangoPatrimonio = datoPersona.IdRangoPatrimonio;
			enrolamiento.IdGiroCIIU = datoPersona.IdGiroCIIU;
			enrolamiento.IngresoPromedioMensual = datoPersona.IngresoPromedioMensual;
			enrolamiento.AgnosServicio = datoPersona.AgnosServicio;
			enrolamiento.FechaInicioActividad = datoPersona.FechaInicioActividad;
			enrolamiento.GlobalLegalEntity = datoPersona.GlobalLegalEntity;
			enrolamiento.DetalleCargo = datoPersona.DetalleCargo;
			enrolamiento.IdPersonaEmpleador = datoPersona.IdPersonaEmpleador;
			enrolamiento.IdPersonaRepresentante = datoPersona.IdPersonaRepresentante;
			enrolamiento.IdProfesion = datoPersona.IdProfesion;
			enrolamiento.IdPaisNacionalidadSecundaria = datoPersona.IdPaisNacionalidadSecundaria;
			enrolamiento.IdPaisNacionalidadPrincipal = datoPersona.IdPaisNacionalidadPrincipal;
			enrolamiento.IdPaisResidenciaSecundaria = datoPersona.IdPaisResidenciaSecundaria;
			enrolamiento.IdPaisResidencia = datoPersona.IdPaisResidencia;
			enrolamiento.IdGiroPrincipal = datoPersona.IdGiroPrincipal;
			enrolamiento.CodigoTipoCargoEmpleado = datoPersona.CodigoTipoCargoEmpleado;
			enrolamiento.IdTransparenciaFiscal = datoPersona.IdTransparenciaFiscal;
			enrolamiento.ResidenciaTributariaExtranjera = datoPersona.ResidenciaTributariaExtranjera;
			enrolamiento.EsExtranjero = datoPersona.EsExtranjero;
			enrolamiento.IdTipoCanalIngresoCorredor = datoPersona.IdTipoCanalIngresoCorredor;
			
			try {
				enrolamiento.CLI_IdSegmentoCliente = obtenerSegmentoPorDefecto(con);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return enrolamiento;
		}).collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearBorradorEnrolamientoDatosPersona.obtenerEntidadesEnrolamiento", fin - inicio);
		return enrolamientos;
	}
	
	private Boolean esListaValida(List<?> lista) {
		logger.trace("INICIO::CrearBorradorEnrolamientoDatosPersona.esListaValida");
		long inicio = System.currentTimeMillis();
		Boolean esListaValida = lista != null && lista.size() > 0;
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearBorradorEnrolamientoDatosPersona.esListaValida", fin - inicio);
		return esListaValida;
	}
	
	private Long obtenerSegmentoPorDefecto(Connection conn) throws SQLException {
		logger.trace("INICIO::CrearBorradorEnrolamientoDatosPersona.obtenerSegmentoPorDefecto");
		long inicio = System.currentTimeMillis();

		SQL sql = new SQL();
		List<TipoSegmento> listadoSegmento = new ArrayList<TipoSegmento>();
		HashMap<String, Object> paramsSegmento = new HashMap<String, Object>();
		try {
			listadoSegmento = sql.query(conn, SQL_CONSULTARSEGMENTOS, paramsSegmento, TipoSegmento.class);
		}catch(Exception e) {
			e.printStackTrace();
		}
		List<Long> idSegmentos = listadoSegmento.parallelStream().map(sol -> {
			return sol.IdSegmentoCliente;
		}).collect(Collectors.toList());
		
		Long idSegmento = idSegmentos.get(0);
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearBorradorEnrolamientoDatosPersona.obtenerSegmentoPorDefecto", fin - inicio);

		return idSegmento;
	}

	
	private List<DocumentoIdentidad> obtenerEntidadesDocumentosIdentidades(List<Enrolamiento> enrolamientos, List<DatoDocumentoIdentidad> datosDocumentoIdentidad){
		logger.trace("INICIO::CrearBorradorEnrolamientoDatosPersona.obtenerEntidadesDocumentosIdentidades");
		long inicio = System.currentTimeMillis();
		
		List<DocumentoIdentidad> documentosIdentidades = new ArrayList<>();
		
		Map<Long, List<DatoDocumentoIdentidad>> datosDocumentosIdentidadesPorIdPersona = datosDocumentoIdentidad.parallelStream().collect(Collectors.groupingBy(datoDocumentoIdentidad->datoDocumentoIdentidad.IdPersona));
		for(Enrolamiento enrolamiento : enrolamientos) {
			List<DatoDocumentoIdentidad> datosDocumentoIdentidadPersona = datosDocumentosIdentidadesPorIdPersona.get(enrolamiento.IdPersona);
			if(esListaValida(datosDocumentoIdentidadPersona)) {
				List<DocumentoIdentidad> documentosIdentidadesPersona = datosDocumentoIdentidadPersona.parallelStream().map(datoDocumentoIdentidad->{
					DocumentoIdentidad documentoIdentidad = new DocumentoIdentidad();
					documentoIdentidad.IdDocumentoIdentidad = 0L;
					documentoIdentidad.IdInterno = datoDocumentoIdentidad.IdDocumentoIdentidad;
					documentoIdentidad.IdEnrolamiento = enrolamiento.IdEnrolamiento;
					documentoIdentidad.IdTipoDocumentoIdentidad = datoDocumentoIdentidad.IdTipoDocumentoIdentidad;
					documentoIdentidad.DocumentoIdentidad = datoDocumentoIdentidad.DocumentoIdentidad;
					documentoIdentidad.NumeroSerie = datoDocumentoIdentidad.NumeroSerie;
					documentoIdentidad.FechaEmision = Fecha.toStringDate(datoDocumentoIdentidad.FechaEmision);
					documentoIdentidad.FechaVencimiento = Fecha.toStringDate(datoDocumentoIdentidad.FechaVencimiento);
					documentoIdentidad.IdTipoDocumentoIdentidadAsociado  = datoDocumentoIdentidad.IdTipoDocumentoIdentidadAsociado;
					documentoIdentidad.TipoPersona = datoDocumentoIdentidad.TipoPersona;
					documentoIdentidad.Predeterminado = datoDocumentoIdentidad.Predeterminado;
					documentoIdentidad.IdDocumentoIdentidadPersona = datoDocumentoIdentidad.IdDocumentoIdentidad;
					return documentoIdentidad;
				}).collect(Collectors.toList());
				documentosIdentidades.addAll(documentosIdentidadesPersona);
			}
		}
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearBorradorEnrolamientoDatosPersona.obtenerEntidadesDocumentosIdentidades", fin - inicio);
		return documentosIdentidades;
	}
	
	private List<DireccionFisica> obtenerEntidadesDireccionesFisicas(List<Enrolamiento> enrolamientos, List<DatoDireccionFisica> datosDireccionesFisicas){
		logger.trace("INICIO::CrearBorradorEnrolamientoDatosPersona.obtenerEntidadesDireccionesFisicas");
		long inicio = System.currentTimeMillis();
		
		List<DireccionFisica> direccionesFisicas = new ArrayList<>();
		
		Map<Long, List<DatoDireccionFisica>> datosDireccionesFisicasPorIdPersona = datosDireccionesFisicas.parallelStream().collect(Collectors.groupingBy(datoDireccionFisica->datoDireccionFisica.IdPersona));
		for(Enrolamiento enrolamiento : enrolamientos) {
			List<DatoDireccionFisica> datosDireccionFisicaPersona = datosDireccionesFisicasPorIdPersona.get(enrolamiento.IdPersona);
			if(esListaValida(datosDireccionFisicaPersona)) {
				List<DireccionFisica> direccionesFisicasPersona = datosDireccionFisicaPersona.parallelStream().map(datoDireccionFisica->{
					DireccionFisica direccionFisica = new DireccionFisica();
					direccionFisica.IdDireccionFisica = 0L;
					direccionFisica.IdInterno = datoDireccionFisica.IdDireccionFisica;
					direccionFisica.IdEnrolamiento = enrolamiento.IdEnrolamiento;
					direccionFisica.Calle = datoDireccionFisica.Calle;
					direccionFisica.Numero = datoDireccionFisica.Numero;
					direccionFisica.Departamento = datoDireccionFisica.Departamento;
					direccionFisica.Sector = datoDireccionFisica.Sector;
					direccionFisica.CodigoPostal = datoDireccionFisica.CodigoPostal;
					direccionFisica.IdCiudad = datoDireccionFisica.IdCiudad;
					direccionFisica.IdDivisionPais = datoDireccionFisica.IdDivisionPais;
					direccionFisica.CodigoProposito = datoDireccionFisica.CodigoProposito;
					direccionFisica.Comentario = datoDireccionFisica.Comentario;
					direccionFisica.CodigoCompuesto = datoDireccionFisica.CodigoCompuesto;
					direccionFisica.Predeterminado = datoDireccionFisica.Predeterminado;
					direccionFisica.IdDireccionFisicaPersona = datoDireccionFisica.IdDireccionFisica;
					return direccionFisica;
				}).collect(Collectors.toList());
				direccionesFisicas.addAll(direccionesFisicasPersona);
			}
		}
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearBorradorEnrolamientoDatosPersona.obtenerEntidadesDireccionesFisicas", fin - inicio);
		return direccionesFisicas;
	}
	
	private List<DireccionElectronica> obtenerEntidadesDireccionesElectronicas(List<Enrolamiento> enrolamientos, List<DatoDireccionElectronica> datosDireccionesElectronicas){
		logger.trace("INICIO::CrearBorradorEnrolamientoDatosPersona.obtenerEntidadesDireccionesElectronicas");
		long inicio = System.currentTimeMillis();
		
		List<DireccionElectronica> direccionesElectronicas = new ArrayList<>();
		
		Map<Long, List<DatoDireccionElectronica>> datosDireccionesFisicasPorIdPersona = datosDireccionesElectronicas.parallelStream().collect(Collectors.groupingBy(datoDireccionElectronica->datoDireccionElectronica.IdPersona));
		for(Enrolamiento enrolamiento : enrolamientos) {
			List<DatoDireccionElectronica> datosDireccionElectronicaPersona = datosDireccionesFisicasPorIdPersona.get(enrolamiento.IdPersona);
			if(esListaValida(datosDireccionElectronicaPersona)) {
				List<DireccionElectronica> direccionesElectronicasPersona = datosDireccionElectronicaPersona.parallelStream().map(datoDireccionElectronica->{
					DireccionElectronica direccionElectronica = new DireccionElectronica();
					direccionElectronica.IdDireccionElectronica = 0L;
					direccionElectronica.IdInterno = datoDireccionElectronica.IdDireccionElectronica;
					direccionElectronica.IdEnrolamiento = enrolamiento.IdEnrolamiento;
					direccionElectronica.Direccion = datoDireccionElectronica.Direccion;
					direccionElectronica.CodigoProposito = datoDireccionElectronica.CodigoProposito;
					direccionElectronica.Comentario = datoDireccionElectronica.Comentario;
					direccionElectronica.Predeterminado = datoDireccionElectronica.Predeterminado;
					direccionElectronica.IdDireccionElectronicaPersona = datoDireccionElectronica.IdDireccionElectronica;
					return direccionElectronica;
				}).collect(Collectors.toList());
				direccionesElectronicas.addAll(direccionesElectronicasPersona);
			}
		}
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearBorradorEnrolamientoDatosPersona.obtenerEntidadesDireccionesElectronicas", fin - inicio);
		return direccionesElectronicas;
	}
	
	private List<Telefono> obtenerEntidadesTelefonos(List<Enrolamiento> enrolamientos, List<DatoTelefono> datosTelefonos){
		logger.trace("INICIO::CrearBorradorEnrolamientoDatosPersona.obtenerEntidadesTelefonos");
		long inicio = System.currentTimeMillis();
		
		List<Telefono> telefonos = new ArrayList<>();
		
		Map<Long, List<DatoTelefono>> datosTelefonosPorIdPersona = datosTelefonos.parallelStream().collect(Collectors.groupingBy(datoTelefono->datoTelefono.IdPersona));
		for(Enrolamiento enrolamiento : enrolamientos) {
			List<DatoTelefono> datosTelefonosPersona = datosTelefonosPorIdPersona.get(enrolamiento.IdPersona);
			if(esListaValida(datosTelefonosPersona)) {
				List<Telefono> telefonosPersona = datosTelefonosPersona.parallelStream().map(datoTelefono->{
					Telefono telefono = new Telefono();
					telefono.IdTelefono = 0L;
					telefono.IdInterno = datoTelefono.IdTelefono;
					telefono.IdEnrolamiento = enrolamiento.IdEnrolamiento;
					telefono.IdPais = datoTelefono.IdPais;
					telefono.IdTipoTelefono = datoTelefono.IdTipoTelefono;
					telefono.Numero = datoTelefono.Numero;
					telefono.Anexo = datoTelefono.Anexo;
					telefono.CodigoProposito = datoTelefono.CodigoProposito;
					telefono.Comentario = datoTelefono.Comentario;
					telefono.Predeterminado = datoTelefono.Predeterminado;
					telefono.IdTelefonoPersona = datoTelefono.IdTelefono;
					return telefono;
				}).collect(Collectors.toList());
				telefonos.addAll(telefonosPersona);
			}
		}
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearBorradorEnrolamientoDatosPersona.obtenerEntidadesTelefonos", fin - inicio);
		return telefonos;
	}
	
	private List<TarjetaBancaria> obtenerEntidadesTarjetasBancarias(List<Enrolamiento> enrolamientos, List<DatoTarjetaBancaria> datosTarjetasBancarias){
		logger.trace("INICIO::CrearBorradorEnrolamientoDatosPersona.obtenerEntidadesTarjetaBancaria");
		long inicio = System.currentTimeMillis();
		
		List<TarjetaBancaria> tarjetasBancarias = new ArrayList<>();
		
		Map<Long, List<DatoTarjetaBancaria>> datosTarjetasBancariasPorIdPersona = datosTarjetasBancarias.parallelStream().collect(Collectors.groupingBy(datoTarjetaBancaria->datoTarjetaBancaria.IdPersona));
		for(Enrolamiento enrolamiento : enrolamientos) {
			List<DatoTarjetaBancaria> datosTarjetasBancariasPersona = datosTarjetasBancariasPorIdPersona.get(enrolamiento.IdPersona);
			if(esListaValida(datosTarjetasBancariasPersona)) {
				List<TarjetaBancaria> tarjetasBancariasPersona = datosTarjetasBancariasPersona.parallelStream().map(datoTarjetaBancaria->{
					TarjetaBancaria tarjetaBancaria = new TarjetaBancaria();
					tarjetaBancaria.IdTarjetaBancaria = 0L;
					tarjetaBancaria.IdInterno = datoTarjetaBancaria.IdTarjetaBancaria;
					tarjetaBancaria.IdEnrolamiento = enrolamiento.IdEnrolamiento;
					tarjetaBancaria.IdBanco = datoTarjetaBancaria.IdBanco;
					tarjetaBancaria.IdTipoTarjetaBancaria = datoTarjetaBancaria.IdTipoTarjetaBancaria;
					tarjetaBancaria.NumeroTarjetaBancaria = datoTarjetaBancaria.NumeroTarjetaBancaria;
					tarjetaBancaria.FechaVencimiento = esObjetoValido(datoTarjetaBancaria.FechaVencimiento) ? Fecha.stringToSqlDate(datoTarjetaBancaria.FechaVencimiento) : "0001-01-01";
					tarjetaBancaria.CodigoVerificacion = esObjetoValido(datoTarjetaBancaria.CodigoVerificacion) ? Long.valueOf(datoTarjetaBancaria.CodigoVerificacion) : 0L;
					tarjetaBancaria.IdTarjetaBancariaPersona = datoTarjetaBancaria.IdTarjetaBancaria;
					return tarjetaBancaria;
				}).collect(Collectors.toList());
				tarjetasBancarias.addAll(tarjetasBancariasPersona);
			}
		}
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearBorradorEnrolamientoDatosPersona.obtenerEntidadesTarjetaBancaria", fin - inicio);
		return tarjetasBancarias;
	}
	
	private List<CuentaBancaria> obtenerEntidadesCuentasBancarias(List<Enrolamiento> enrolamientos, List<DatoCuentaBancaria> datosCuentasBancarias){
		logger.trace("INICIO::CrearBorradorEnrolamientoDatosPersona.obtenerEntidadesCuentasBancarias");
		long inicio = System.currentTimeMillis();
		
		List<CuentaBancaria> cuentasBancarias = new ArrayList<>();
		
		Map<Long, List<DatoCuentaBancaria>> datosCuentasBancariasPorIdPersona = datosCuentasBancarias.parallelStream().collect(Collectors.groupingBy(datoCuentaBancaria->datoCuentaBancaria.IdPersona));
		for(Enrolamiento enrolamiento : enrolamientos) {
			List<DatoCuentaBancaria> datosCuentasBancariasPersona = datosCuentasBancariasPorIdPersona.get(enrolamiento.IdPersona);
			if(esListaValida(datosCuentasBancariasPersona)) {
				List<CuentaBancaria> cuentasBancariasPersonas = datosCuentasBancariasPersona.parallelStream().map(datoCuentaBancaria->{
					CuentaBancaria cuentaBancaria = new CuentaBancaria();
					cuentaBancaria.IdInterno = datoCuentaBancaria.IdCuentaBancaria;
					cuentaBancaria.IdCuentaBancaria = 0L;
					cuentaBancaria.IdBanco = datoCuentaBancaria.IdBanco;
					cuentaBancaria.IdTipoCuentaBancaria = datoCuentaBancaria.IdTipoCuentaBancaria;
					cuentaBancaria.IdMoneda = datoCuentaBancaria.IdMoneda;
					cuentaBancaria.NumeroCuentaBancaria = datoCuentaBancaria.NumeroCuentaBancaria;
					cuentaBancaria.IdEnrolamiento = enrolamiento.IdEnrolamiento;
					cuentaBancaria.IdPlaza = datoCuentaBancaria.IdPlaza;
					cuentaBancaria.IdTipoIdentificadorCuenta = datoCuentaBancaria.IdTipoIdentificadorCuenta;
					cuentaBancaria.IdentificadorCuenta = datoCuentaBancaria.IdentificadorCuenta;
					cuentaBancaria.IdPersonaRelacionada = datoCuentaBancaria.IdPersonaRelacionada;
					cuentaBancaria.CodigoCuentaInterbancario = datoCuentaBancaria.CodigoCuentaInterbancario;
					cuentaBancaria.CodigoAba = datoCuentaBancaria.CodigoAba;
					cuentaBancaria.OtroCodigoBancoExtranjero = datoCuentaBancaria.OtroCodigoBancoExtranjero;
					cuentaBancaria.CodigoIban = datoCuentaBancaria.OtroCodigoBancoExtranjero;
					cuentaBancaria.OtroCodigoCuentaExtranjero = datoCuentaBancaria.OtroCodigoCuentaExtranjero;
					cuentaBancaria.EsPac = datoCuentaBancaria.EsPac;
					cuentaBancaria.IdCuentaBancariaPersona = datoCuentaBancaria.IdCuentaBancaria;
					cuentaBancaria.Predeterminado = datoCuentaBancaria.Predeterminado;
					return cuentaBancaria;
				}).collect(Collectors.toList());
				cuentasBancarias.addAll(cuentasBancariasPersonas);
			}
		}
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearBorradorEnrolamientoDatosPersona.obtenerEntidadesCuentasBancarias", fin - inicio);
		return cuentasBancarias;
	}
	
	private List<RelacionPersona> obtenerEntidadesRelacionesPersonas(List<Enrolamiento> enrolamientos, List<DatoRelacionPersona> datosRelacionesPersonas){
		logger.trace("INICIO::CrearBorradorEnrolamientoDatosPersona.obtenerEntidadesRelacionesPersonas");
		long inicio = System.currentTimeMillis();
		
		List<RelacionPersona> relacionesPersona = new ArrayList<>();
		
		Map<Long, List<DatoRelacionPersona>> datosRelacionesPersonasPorIdPersona = datosRelacionesPersonas.parallelStream().collect(Collectors.groupingBy(datoRelacionPersona->datoRelacionPersona.IdPersonaOrigen));
		for(Enrolamiento enrolamiento : enrolamientos) {
			List<DatoRelacionPersona> datosRelacionesPersonaPersona = datosRelacionesPersonasPorIdPersona.get(enrolamiento.IdPersona);
			if(esListaValida(datosRelacionesPersonaPersona)) {
				List<RelacionPersona> relacionesPersonaPersona = datosRelacionesPersonaPersona.parallelStream().map(datoRelacionPersona->{
					RelacionPersona relacionPersona = new RelacionPersona();
					relacionPersona.IdRelacionPersona = 0L;
					relacionPersona.IdInterno = datoRelacionPersona.IdRelacionPersona;
					relacionPersona.IdEnrolamientoOrigen = enrolamiento.IdEnrolamiento;
					relacionPersona.IdTipoRelacion = datoRelacionPersona.IdTipoRelacion;
					relacionPersona.IdPersonaDestino = datoRelacionPersona.IdPersonaDestino;
					relacionPersona.FechaInicioRelacion = esObjetoValido(datoRelacionPersona.FechaInicioRelacion) ? Fecha.stringToSqlDate(datoRelacionPersona.FechaInicioRelacion) : "0001-01-01";
					relacionPersona.FechaFinRelacion = esObjetoValido(datoRelacionPersona.FechaFinRelacion) ? Fecha.stringToSqlDate(datoRelacionPersona.FechaFinRelacion) : "0001-01-01";
					relacionPersona.PorcentajeParticipacion = datoRelacionPersona.PorcentajeParticipacion;
					relacionPersona.RetiraDocumentos = datoRelacionPersona.RetiraDocumentos;
					relacionPersona.FinalidadRelacion = datoRelacionPersona.FinalidadRelacion;
					relacionPersona.CondicionesRelacion = datoRelacionPersona.CondicionesRelacion;
					relacionPersona.IdGradoVinculacionParental = datoRelacionPersona.IdGradoVinculacionParental;
					relacionPersona.IdCiudad = datoRelacionPersona.IdCiudad;
					relacionPersona.Calle = datoRelacionPersona.Calle;
					relacionPersona.Numero = datoRelacionPersona.Numero;
					relacionPersona.Email = datoRelacionPersona.Email;
					relacionPersona.Telefono = datoRelacionPersona.Telefono;
					relacionPersona.Complemento = datoRelacionPersona.Complemento;
					relacionPersona.Ciudad = datoRelacionPersona.Ciudad;
					relacionPersona.IdRelacionPersonaPersona = datoRelacionPersona.IdRelacionPersona;
					return relacionPersona;
				}).collect(Collectors.toList());
				relacionesPersona.addAll(relacionesPersonaPersona);
			}
		}
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearBorradorEnrolamientoDatosPersona.obtenerEntidadesRelacionesPersonas", fin - inicio);
		return relacionesPersona;
	}
	
	private List<GrupoDireccion> obtenerEntidadesGruposDirecciones(List<Enrolamiento> enrolamientos, List<DatoGrupoDireccion> datosGruposDirecciones){
		logger.trace("INICIO::CrearBorradorEnrolamientoDatosPersona.obtenerEntidadesGruposDirecciones");
		long inicio = System.currentTimeMillis();
		
		List<GrupoDireccion> gruposDirecciones = new ArrayList<>();
		
		Map<Long, List<DatoGrupoDireccion>> datosGruposDireccionesPorIdPersona = datosGruposDirecciones.parallelStream().collect(Collectors.groupingBy(datoGrupoDireccion->datoGrupoDireccion.IdPersona));
		for(Enrolamiento enrolamiento : enrolamientos) {
			List<DatoGrupoDireccion> datosGrupoDireccionPersona = datosGruposDireccionesPorIdPersona.get(enrolamiento.IdPersona);
			if(esListaValida(datosGrupoDireccionPersona)) {
				List<GrupoDireccion> gruposDireccionesPersona = datosGrupoDireccionPersona.parallelStream().map(datoGrupoDireccion->{
					GrupoDireccion grupoDireccion = new GrupoDireccion();
			        grupoDireccion.IdGrupoDireccion = 0L;
			        grupoDireccion.IdEnrolamiento = enrolamiento.IdEnrolamiento;
			        grupoDireccion.IdInterno = datoGrupoDireccion.IdGrupoDireccion;
			        grupoDireccion.Grupo = datoGrupoDireccion.Grupo;
			        grupoDireccion.Comentario = datoGrupoDireccion.Comentario;
			        grupoDireccion.IdGrupoDireccionPersona = datoGrupoDireccion.IdGrupoDireccion;
					return grupoDireccion;
				}).collect(Collectors.toList());
				gruposDirecciones.addAll(gruposDireccionesPersona);
			}
		}
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearBorradorEnrolamientoDatosPersona.obtenerEntidadesGruposDirecciones", fin - inicio);
		return gruposDirecciones;
	}
	
	private List<Indicador> obtenerEntidadesIndicadores(List<Enrolamiento> enrolamientos, List<DatoIndicador> datosIndicadores){
		logger.trace("INICIO::CrearBorradorEnrolamientoDatosPersona.obtenerEntidadesIndicadores");
		long inicio = System.currentTimeMillis();
		
		List<Indicador> indicadores = new ArrayList<>();
		
		Map<Long, List<DatoIndicador>> datosIndicadoresPorIdPersona = datosIndicadores.parallelStream().collect(Collectors.groupingBy(datoIndicador->datoIndicador.IdPersona));
		for(Enrolamiento enrolamiento : enrolamientos) {
			List<DatoIndicador> datosIndicadorPersona = datosIndicadoresPorIdPersona.get(enrolamiento.IdPersona);
			if(esListaValida(datosIndicadorPersona)) {
				List<Indicador> indicadoresPersona = datosIndicadorPersona.parallelStream().map(datoIndicador->{
					Indicador indicador = new Indicador();
					indicador.IdIndicador = 0L;
					indicador.IdInterno = datoIndicador.IdIndicador;
					indicador.IdEnrolamiento = enrolamiento.IdEnrolamiento;
					indicador.IdTipoEntidad = datoIndicador.IdTipoEntidad;
					indicador.IdTipoIndicador = datoIndicador.IdTipoIndicador;
					indicador.ModeloCliente = datoIndicador.ModeloCliente;
					indicador.IdEmpresa = datoIndicador.IdEmpresa;
					indicador.ValorIndicador = datoIndicador.ValorIndicador;
					indicador.IdIndicadorPersona = datoIndicador.IdIndicador;
					return indicador;
				}).collect(Collectors.toList());
				indicadores.addAll(indicadoresPersona);
			}
		}
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearBorradorEnrolamientoDatosPersona.obtenerEntidadesIndicadores", fin - inicio);
		return indicadores;
	}
	
	private List<Fatca> obtenerEntidadesFatcas(List<Enrolamiento> enrolamientos, List<DatoFatca> datosFatcas){
		logger.trace("INICIO::CrearBorradorEnrolamientoDatosPersona.obtenerEntidadesFatcas");
		long inicio = System.currentTimeMillis();
		
		List<Fatca> fatcas = new ArrayList<>();
		
		Map<Long, List<DatoFatca>> datosFatcasPorIdPersona = datosFatcas.parallelStream().collect(Collectors.groupingBy(datoFatca->datoFatca.IdPersona));
		for(Enrolamiento enrolamiento : enrolamientos) {
			List<DatoFatca> datosFatcaPersona = datosFatcasPorIdPersona.get(enrolamiento.IdPersona);
			if(esListaValida(datosFatcaPersona)) {
				List<Fatca> fatcasPersona = datosFatcaPersona.parallelStream().map(datoFatca->{
					Fatca fatca = new Fatca();
					fatca.IdInterno = datoFatca.IdPersona;
					fatca.IdEnrolamiento = enrolamiento.IdEnrolamiento;
					fatca.IdClasificacionFatca= datoFatca.IdClasificacionFatca;
					fatca.IdClasificacionEmpresaFatca = datoFatca.IdClasificacionEmpresaFatca;
					fatca.NumeroGIIN = datoFatca.NumeroGIIN;
					fatca.FechaFirmaFormulario = datoFatca.FechaFirmaFormulario;
					fatca.AutorizacionInformarOrganismo = datoFatca.AutorizacionInformarOrganismo;
					fatca.ExcepcionResidente = datoFatca.ExcepcionResidente;
					fatca.ExcepcionNacimiento = datoFatca.ExcepcionNacimiento;
					fatca.ExcepcionDomicilio = datoFatca.ExcepcionDomicilio;
					fatca.ExcepcionTelefono = datoFatca.ExcepcionTelefono;
					fatca.ExcepcionOrdenPermanente = datoFatca.ExcepcionOrdenPermanente;
					fatca.ExcepcionFirma = datoFatca.ExcepcionFirma;
					fatca.ParticipacionSociedadesUSA = datoFatca.ParticipacionSociedadesUSA;
					fatca.OrdenPermanente = datoFatca.OrdenPermanente;
					fatca.NACEEUU = datoFatca.NACEEUU;
					fatca.NACEEUUExplicacion = datoFatca.NACEEUUExplicacion;
					fatca.EntidadFinanciera = datoFatca.EntidadFinanciera;
					fatca.TipoInstitucionFinanciera = datoFatca.TipoInstitucionFinanciera;
					fatca.EntidadGestionada = datoFatca.EntidadGestionada;
					fatca.IngresosPasivos = datoFatca.IngresosPasivos;
					fatca.GenerarIngresosPasivos = datoFatca.GenerarIngresosPasivos;
					fatca.CategoriaEntidad = datoFatca.CategoriaEntidad;
					fatca.GIINEntidad = datoFatca.GIINEntidad;
					fatca.RazonSocial = datoFatca.RazonSocial;
					fatca.FechaLiquidacion = datoFatca.FechaLiquidacion !=null ? datoFatca.FechaLiquidacion : fatca.FechaLiquidacion;
					fatca.NombreMercado = datoFatca.NombreMercado;
					fatca.FechaConstitucion = datoFatca.FechaConstitucion !=null ? datoFatca.FechaLiquidacion : fatca.FechaLiquidacion;
					fatca.ObligacionesFiscales = datoFatca.ObligacionesFiscales;
					fatca.ObligacionesFiscalesExplicacion = datoFatca.ObligacionesFiscalesExplicacion;
					fatca.IndividuoControl = datoFatca.IndividuoControl;
					return fatca;
				}).collect(Collectors.toList());
				fatcas.addAll(fatcasPersona);
			}
		}
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearBorradorEnrolamientoDatosPersona.obtenerEntidadesFatcas", fin - inicio);
		return fatcas;
	}
	
	private SalidaAdministrarBorrador crearDocumentoIdentidad(Connection con, EntradaCrearBorradorEnrolamientoDatosPersona entrada, List<DocumentoIdentidad> documentosIdentidades) throws Exception {
		logger.debug("INICIO::CrearBorradorEnrolamientoDatosPersona.crearDocumentoIdentidad");
		long inicio = System.currentTimeMillis();
		
		SalidaAdministrarBorrador salidaAdministrarBorrador = new SalidaAdministrarBorrador();
		
		EntradaCrearBorradorDocumentoIdentidad entradaCrearBorradorDocumentoIdentidad = new EntradaCrearBorradorDocumentoIdentidad();
		entradaCrearBorradorDocumentoIdentidad.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCrearBorradorDocumentoIdentidad.p_IdUsuario = entrada.p_IdUsuario;
		entradaCrearBorradorDocumentoIdentidad.p_IdModulo = entrada.p_IdModulo;
		entradaCrearBorradorDocumentoIdentidad.p_UsaReglasNegocio = "N";
		entradaCrearBorradorDocumentoIdentidad.p_CodigoFormulario = "";
		entradaCrearBorradorDocumentoIdentidad.p_IdTipoCanal = "";
		entradaCrearBorradorDocumentoIdentidad.p_DocumentosIdentidades = documentosIdentidades;
		
		CrearBorradorDocumentoIdentidad crearDocumentoIdentidad = new CrearBorradorDocumentoIdentidad();
		salidaAdministrarBorrador = crearDocumentoIdentidad.logica(con, entradaCrearBorradorDocumentoIdentidad);
		
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearBorradorEnrolamientoDatosPersona.crearDocumentoIdentidad", fin - inicio);
		return salidaAdministrarBorrador;
	}
	
	private SalidaAdministrarBorrador crearDireccionFisica(Connection con, EntradaCrearBorradorEnrolamientoDatosPersona entrada, List<DireccionFisica> direccionesFisicas) throws Exception {
		logger.debug("INICIO::CrearBorradorEnrolamientoDatosPersona.crearDireccionFisica");
		long inicio = System.currentTimeMillis();
		
		SalidaAdministrarBorrador salidaAdministrarBorrador = new SalidaAdministrarBorrador();
		
		EntradaCrearBorradorDireccionFisica entradaCrearBorradorDireccionFisica = new EntradaCrearBorradorDireccionFisica();
		entradaCrearBorradorDireccionFisica.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCrearBorradorDireccionFisica.p_IdUsuario = entrada.p_IdUsuario;
		entradaCrearBorradorDireccionFisica.p_IdModulo = entrada.p_IdModulo;
		entradaCrearBorradorDireccionFisica.p_UsaReglasNegocio = "N";
		entradaCrearBorradorDireccionFisica.p_CodigoFormulario = "";
		entradaCrearBorradorDireccionFisica.p_IdTipoCanal = "";
		entradaCrearBorradorDireccionFisica.p_DireccionesFisicas = direccionesFisicas;
		
		CrearBorradorDireccionFisica crearBorradorDireccionFisica = new CrearBorradorDireccionFisica();
		salidaAdministrarBorrador = crearBorradorDireccionFisica.logica(con, entradaCrearBorradorDireccionFisica);
		
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearBorradorEnrolamientoDatosPersona.crearDireccionFisica", fin - inicio);
		return salidaAdministrarBorrador;
	}
	
	private SalidaAdministrarBorrador crearDireccionElectronica(Connection con, EntradaCrearBorradorEnrolamientoDatosPersona entrada, List<DireccionElectronica> direccionesElectronicas) throws Exception {
		logger.debug("INICIO::CrearBorradorEnrolamientoDatosPersona.crearDireccionElectronica");
		long inicio = System.currentTimeMillis();
		
		SalidaAdministrarBorrador salidaAdministrarBorrador = new SalidaAdministrarBorrador();
		
		EntradaCrearBorradorDireccionElectronica entradaCrearBorradorDireccionElectronica = new EntradaCrearBorradorDireccionElectronica();
		entradaCrearBorradorDireccionElectronica.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCrearBorradorDireccionElectronica.p_IdUsuario = entrada.p_IdUsuario;
		entradaCrearBorradorDireccionElectronica.p_IdModulo = entrada.p_IdModulo;
		entradaCrearBorradorDireccionElectronica.p_UsaReglasNegocio = "N";
		entradaCrearBorradorDireccionElectronica.p_CodigoFormulario = "";
		entradaCrearBorradorDireccionElectronica.p_IdTipoCanal = "";
		entradaCrearBorradorDireccionElectronica.p_DireccionesElectronicas = direccionesElectronicas;
		
		CrearBorradorDireccionElectronica crearBorradorDireccionElectronica = new CrearBorradorDireccionElectronica();
		salidaAdministrarBorrador = crearBorradorDireccionElectronica.logica(con, entradaCrearBorradorDireccionElectronica);
		
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearBorradorEnrolamientoDatosPersona.crearDireccionElectronica", fin - inicio);
		return salidaAdministrarBorrador;
	}
	
	private SalidaAdministrarBorrador crearTelefono(Connection con, EntradaCrearBorradorEnrolamientoDatosPersona entrada, List<Telefono> telefonos) throws Exception {
		logger.debug("INICIO::CrearBorradorEnrolamientoDatosPersona.crearTelefono");
		long inicio = System.currentTimeMillis();
		
		SalidaAdministrarBorrador salidaAdministrarBorrador = new SalidaAdministrarBorrador();
		
		EntradaCrearBorradorTelefono entradaCrearBorradorTelefono = new EntradaCrearBorradorTelefono();
		entradaCrearBorradorTelefono.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCrearBorradorTelefono.p_IdUsuario = entrada.p_IdUsuario;
		entradaCrearBorradorTelefono.p_IdModulo = entrada.p_IdModulo;
		entradaCrearBorradorTelefono.p_UsaReglasNegocio = "N";
		entradaCrearBorradorTelefono.p_CodigoFormulario = "";
		entradaCrearBorradorTelefono.p_IdTipoCanal = "";
		entradaCrearBorradorTelefono.p_Telefonos = telefonos;
		
		CrearBorradorTelefono crearBorradorTelefono = new CrearBorradorTelefono();
		salidaAdministrarBorrador = crearBorradorTelefono.logica(con, entradaCrearBorradorTelefono);
		
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearBorradorEnrolamientoDatosPersona.crearTelefono", fin - inicio);
		return salidaAdministrarBorrador;
	}
	
	private SalidaAdministrarBorrador crearTarjetaBancaria(Connection con, EntradaCrearBorradorEnrolamientoDatosPersona entrada, List<TarjetaBancaria> tarjetasBancarias) throws Exception {
		logger.debug("INICIO::CrearBorradorEnrolamientoDatosPersona.crearTarjetaBancaria");
		long inicio = System.currentTimeMillis();
		
		SalidaAdministrarBorrador salidaAdministrarBorrador = new SalidaAdministrarBorrador();
		
		EntradaCrearBorradorTarjetaBancaria entradaCrearBorradorTarjetaBancaria = new EntradaCrearBorradorTarjetaBancaria();
		entradaCrearBorradorTarjetaBancaria.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCrearBorradorTarjetaBancaria.p_IdUsuario = entrada.p_IdUsuario;
		entradaCrearBorradorTarjetaBancaria.p_IdModulo = entrada.p_IdModulo;
		entradaCrearBorradorTarjetaBancaria.p_UsaReglasNegocio = "N";
		entradaCrearBorradorTarjetaBancaria.p_CodigoFormulario = "";
		entradaCrearBorradorTarjetaBancaria.p_IdTipoCanal = "";
		entradaCrearBorradorTarjetaBancaria.p_TarjetasBancarias = tarjetasBancarias;
		
		CrearBorradorTarjetaBancaria crearBorradorTarjetaBancaria = new CrearBorradorTarjetaBancaria();
		salidaAdministrarBorrador = crearBorradorTarjetaBancaria.logica(con, entradaCrearBorradorTarjetaBancaria);
		
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearBorradorEnrolamientoDatosPersona.crearTarjetaBancaria", fin - inicio);
		return salidaAdministrarBorrador;
	}
	
	private SalidaAdministrarBorrador crearCuentaBancaria(Connection con, EntradaCrearBorradorEnrolamientoDatosPersona entrada, List<CuentaBancaria> cuentasBancarias) throws Exception {
		logger.debug("INICIO::CrearBorradorEnrolamientoDatosPersona.crearCuentaBancaria");
		long inicio = System.currentTimeMillis();
		
		SalidaAdministrarBorrador salidaAdministrarBorrador = new SalidaAdministrarBorrador();
		
		EntradaCrearBorradorCuentaBancaria entradaCrearBorradorCuentaBancaria = new EntradaCrearBorradorCuentaBancaria();
		entradaCrearBorradorCuentaBancaria.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCrearBorradorCuentaBancaria.p_IdUsuario = entrada.p_IdUsuario;
		entradaCrearBorradorCuentaBancaria.p_IdModulo = entrada.p_IdModulo;
		entradaCrearBorradorCuentaBancaria.p_UsaReglasNegocio = "N";
		entradaCrearBorradorCuentaBancaria.p_CodigoFormulario = "";
		entradaCrearBorradorCuentaBancaria.p_IdTipoCanal = "";
		entradaCrearBorradorCuentaBancaria.p_CuentasBancarias = cuentasBancarias;
		
		CrearBorradorCuentaBancaria crearBorradorCuentaBancaria = new CrearBorradorCuentaBancaria();
		salidaAdministrarBorrador = crearBorradorCuentaBancaria.logica(con, entradaCrearBorradorCuentaBancaria);
		
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearBorradorEnrolamientoDatosPersona.crearCuentaBancaria", fin - inicio);
		return salidaAdministrarBorrador;
	}
	
	private SalidaAdministrarBorrador crearRelacionPersona(Connection con, EntradaCrearBorradorEnrolamientoDatosPersona entrada, List<RelacionPersona> relacionesPersona) throws Exception {
		logger.debug("INICIO::CrearBorradorEnrolamientoDatosPersona.crearRelacionPersona");
		long inicio = System.currentTimeMillis();
		
		SalidaAdministrarBorrador salidaAdministrarBorrador = new SalidaAdministrarBorrador();
		
		EntradaCrearBorradorRelacionPersona entradaCrearBorradorRelacionPersona = new EntradaCrearBorradorRelacionPersona();
		entradaCrearBorradorRelacionPersona.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCrearBorradorRelacionPersona.p_IdUsuario = entrada.p_IdUsuario;
		entradaCrearBorradorRelacionPersona.p_IdModulo = entrada.p_IdModulo;
		entradaCrearBorradorRelacionPersona.p_UsaReglasNegocio = "N";
		entradaCrearBorradorRelacionPersona.p_CodigoFormulario = "";
		entradaCrearBorradorRelacionPersona.p_IdTipoCanal = "";
		entradaCrearBorradorRelacionPersona.p_RelacionesPersona = relacionesPersona;
		
		CrearBorradorRelacionPersona crearBorradorRelacionPersona = new CrearBorradorRelacionPersona();
		salidaAdministrarBorrador = crearBorradorRelacionPersona.logica(con, entradaCrearBorradorRelacionPersona);
		
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearBorradorEnrolamientoDatosPersona.crearRelacionPersona", fin - inicio);
		return salidaAdministrarBorrador;
	}
	
	private SalidaAdministrarBorrador crearIndicador(Connection con, EntradaCrearBorradorEnrolamientoDatosPersona entrada, List<Indicador> indicadores) throws Exception {
		logger.debug("INICIO::CrearBorradorEnrolamientoDatosPersona.crearIndicador");
		long inicio = System.currentTimeMillis();
		
		SalidaAdministrarBorrador salidaAdministrarBorrador = new SalidaAdministrarBorrador();
		
		EntradaCrearBorradorIndicador entradaCrearBorradorIndicador = new EntradaCrearBorradorIndicador();
		entradaCrearBorradorIndicador.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCrearBorradorIndicador.p_IdUsuario = entrada.p_IdUsuario;
		entradaCrearBorradorIndicador.p_IdModulo = entrada.p_IdModulo;
		entradaCrearBorradorIndicador.p_UsaReglasNegocio = "N";
		entradaCrearBorradorIndicador.p_CodigoFormulario = "";
		entradaCrearBorradorIndicador.p_IdTipoCanal = "";
		entradaCrearBorradorIndicador.p_Indicadores = indicadores;
		
		CrearBorradorIndicador crearBorradorIndicador = new CrearBorradorIndicador();
		salidaAdministrarBorrador = crearBorradorIndicador.logica(con, entradaCrearBorradorIndicador);
		
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearBorradorEnrolamientoDatosPersona.crearIndicador", fin - inicio);
		return salidaAdministrarBorrador;
	}
	
	private SalidaAdministrarBorrador crearGrupoDireccion(Connection con, EntradaCrearBorradorEnrolamientoDatosPersona entrada, List<GrupoDireccion> gruposDirecciones) throws Exception {
		logger.debug("INICIO::CrearBorradorEnrolamientoDatosPersona.crearGrupoDireccion");
		long inicio = System.currentTimeMillis();
		
		SalidaAdministrarBorrador salidaAdministrarBorrador = new SalidaAdministrarBorrador();
		
		EntradaCrearBorradorGrupoDireccion entradaCrearBorradorGrupoIndicador = new EntradaCrearBorradorGrupoDireccion();
		entradaCrearBorradorGrupoIndicador.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCrearBorradorGrupoIndicador.p_IdUsuario = entrada.p_IdUsuario;
		entradaCrearBorradorGrupoIndicador.p_IdModulo = entrada.p_IdModulo;
		entradaCrearBorradorGrupoIndicador.p_UsaReglasNegocio = "N";
		entradaCrearBorradorGrupoIndicador.p_CodigoFormulario = "";
		entradaCrearBorradorGrupoIndicador.p_IdTipoCanal = "";
		entradaCrearBorradorGrupoIndicador.p_GrupoDirecciones = gruposDirecciones;
		
		CrearBorradorGrupoDireccion crearBorradorGrupoDireccion = new CrearBorradorGrupoDireccion();
		salidaAdministrarBorrador = crearBorradorGrupoDireccion.logica(con, entradaCrearBorradorGrupoIndicador);
		
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearBorradorEnrolamientoDatosPersona.crearGrupoDireccion", fin - inicio);
		return salidaAdministrarBorrador;
	}
	
	private List<GrupoDireccionDetalle> obtenerEntidadesGrupoIndicadoresDetalle(List<DatoGrupoDireccionDetalle> datosGruposDireccionesDetalles, List<ResultadoAdministrarBorradorEntidad> direccionesElectronicos, List<ResultadoAdministrarBorradorEntidad> gruposDirecciones){
		logger.trace("INICIO::CrearBorradorEnrolamientoDatosPersona.obtenerEntidadesIndicadores");
		long inicio = System.currentTimeMillis();
		
		List<GrupoDireccionDetalle> grupoDireccionesDetalles = new ArrayList<>();
		
		Map<Long, ResultadoAdministrarBorradorEntidad> idsGruposDireccionesPorIdInterno = gruposDirecciones.parallelStream().collect(Collectors.toMap(grupoDireccion->grupoDireccion.IdInterno, grupoDireccion->grupoDireccion));
		Map<Long, ResultadoAdministrarBorradorEntidad> idsDireccionesElectronicosPorIdInterno = direccionesElectronicos.parallelStream().collect(Collectors.toMap(direccionElectronica->direccionElectronica.IdInterno, direccionElectronica->direccionElectronica));
		for(DatoGrupoDireccionDetalle datoGrupoDireccionDetalle : datosGruposDireccionesDetalles) {
			ResultadoAdministrarBorradorEntidad idGrupoDireccion = idsGruposDireccionesPorIdInterno.get(datoGrupoDireccionDetalle.IdGrupoDireccion);
			ResultadoAdministrarBorradorEntidad idDireccionElectronica = idsDireccionesElectronicosPorIdInterno.get(datoGrupoDireccionDetalle.IdDireccionElectronica);
			if(idGrupoDireccion != null && idDireccionElectronica != null) {
				GrupoDireccionDetalle detalle = new GrupoDireccionDetalle();
		        detalle.IdGrupoDireccionDetalle = 0L;
		        detalle.IdGrupoDireccion        = idGrupoDireccion.IdEntidad;
		        detalle.IdDireccionElectronica  = idDireccionElectronica.IdEntidad;
		        detalle.IdTipoDestinatario      = datoGrupoDireccionDetalle.IdTipoDestinatario;
		        detalle.IdGrupoDireccionPersona = datoGrupoDireccionDetalle.IdGrupoDireccion;
		        detalle.IdGrupoDireccionDetallePersona = datoGrupoDireccionDetalle.IdGrupoDireccionDetalle;
		        grupoDireccionesDetalles.add(detalle);
			}
		}
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearBorradorEnrolamientoDatosPersona.obtenerEntidadesIndicadores", fin - inicio);
		return grupoDireccionesDetalles;
	}
	
	private SalidaAdministrarBorrador crearGrupoDireccionDetalle(Connection con, EntradaCrearBorradorEnrolamientoDatosPersona entrada, List<GrupoDireccionDetalle> gruposDireccionesDetalle) throws Exception {
		logger.debug("INICIO::CrearBorradorEnrolamientoDatosPersona.crearGrupoDireccionDetalle");
		long inicio = System.currentTimeMillis();
		
		SalidaAdministrarBorrador salidaAdministrarBorrador = new SalidaAdministrarBorrador();
		
		EntradaCrearBorradorGrupoDireccionDetalle entradaCrearBorradorGrupoDireccionDetalle = new EntradaCrearBorradorGrupoDireccionDetalle();
		entradaCrearBorradorGrupoDireccionDetalle.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCrearBorradorGrupoDireccionDetalle.p_IdUsuario = entrada.p_IdUsuario;
		entradaCrearBorradorGrupoDireccionDetalle.p_IdModulo = entrada.p_IdModulo;
		entradaCrearBorradorGrupoDireccionDetalle.p_UsaReglasNegocio = "N";
		entradaCrearBorradorGrupoDireccionDetalle.p_CodigoFormulario = "";
		entradaCrearBorradorGrupoDireccionDetalle.p_IdTipoCanal = "";
		entradaCrearBorradorGrupoDireccionDetalle.p_GrupoDireccionDetalles = gruposDireccionesDetalle;
		
		CrearBorradorGrupoDireccionDetalle crearBorradorGrupoDireccionDetalle = new CrearBorradorGrupoDireccionDetalle();
		salidaAdministrarBorrador = crearBorradorGrupoDireccionDetalle.logica(con, entradaCrearBorradorGrupoDireccionDetalle);
		
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearBorradorEnrolamientoDatosPersona.crearGrupoDireccionDetalle", fin - inicio);
		return salidaAdministrarBorrador;
	}
	
	private SalidaAdministrarBorrador crearFatca(Connection con, EntradaCrearBorradorEnrolamientoDatosPersona entrada, List<Fatca> fatca) throws Exception {
		logger.debug("INICIO::CrearBorradorEnrolamientoDatosPersona.crearFatca");
		long inicio = System.currentTimeMillis();
		
		SalidaAdministrarBorrador salidaAdministrarBorrador = new SalidaAdministrarBorrador();
		
		EntradaCrearBorradorFatca entradaCrearBorradorFatca = new EntradaCrearBorradorFatca();
		entradaCrearBorradorFatca.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCrearBorradorFatca.p_IdUsuario = entrada.p_IdUsuario;
		entradaCrearBorradorFatca.p_IdModulo = entrada.p_IdModulo;
		entradaCrearBorradorFatca.p_UsaReglasNegocio = "N";
		entradaCrearBorradorFatca.p_CodigoFormulario = "";
		entradaCrearBorradorFatca.p_IdTipoCanal = "";
		entradaCrearBorradorFatca.p_Fatcas = fatca;
		
		CrearBorradorFatca crearBorradorFatca = new CrearBorradorFatca();
		salidaAdministrarBorrador = crearBorradorFatca.logica(con, entradaCrearBorradorFatca);
		
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearBorradorEnrolamientoDatosPersona.crearFatca", fin - inicio);
		return salidaAdministrarBorrador;
	}
	
	private Map<Long, DatoPersona> obtenerDatosPersonasPorIdPersona(List<DatoPersona> datosPersona){
		logger.trace("INICIO::CrearBorradorEnrolamientoDatosPersona.obtenerDatosPersonasPorIdPersona");
		long inicio = System.currentTimeMillis();
		
		Map<Long, DatoPersona> datosPersonaPorIdPersona = datosPersona.parallelStream().collect(Collectors.toMap(datoPersona->datoPersona.IdPersona, datoPersona->datoPersona));
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearBorradorEnrolamientoDatosPersona.obtenerDatosPersonasPorIdPersona", inicio - fin);
		return datosPersonaPorIdPersona;
	}
	
	private Map<Long, ResultadoAdministrarBorradorEntidad> obtenerEntidadesPorIdInterno(List<ResultadoAdministrarBorradorEntidad> entidades){
		logger.trace("INICIO::CrearBorradorEnrolamientoDatosPersona.obtenerEntidadesPorIdInterno");
		long inicio = System.currentTimeMillis();
		
		Map<Long, ResultadoAdministrarBorradorEntidad> entidadesPorIdInterno = entidades.parallelStream().collect(Collectors.toMap(entidad->entidad.IdInterno, entidad->entidad));
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearBorradorEnrolamientoDatosPersona.obtenerEntidadesPorIdInterno", inicio - fin);
		return entidadesPorIdInterno;
	}
	
	private Boolean esObjetoValido(Object objeto) {
		logger.trace("INICIO::CrearBorradorEnrolamientoDatosPersona.obtenerEntidadesPorIdInterno");
		long inicio = System.currentTimeMillis();
		Boolean esObjetoValido = objeto != null;
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearBorradorEnrolamientoDatosPersona.obtenerEntidadesPorIdInterno", inicio - fin);
		return esObjetoValido;
	}
	
	private List<Enrolamiento> actualizarIdDocumentoIdentidad(List<Enrolamiento> enrolamientos, List<ResultadoAdministrarBorradorEntidad> documentosIdentidades, List<DatoPersona> datosPersonas) {
		logger.trace("INICIO::CrearBorradorEnrolamientoDatosPersona.actualizarIdDocumentoIdentidad");
		long inicio = System.currentTimeMillis();
		
		Map<Long, ResultadoAdministrarBorradorEntidad> documentosIdentidadesPorIdDocumentoIdentidadPersona = obtenerEntidadesPorIdInterno(documentosIdentidades);
		Map<Long, DatoPersona> datosPersonasPorIdPersona = obtenerDatosPersonasPorIdPersona(datosPersonas);
		
		enrolamientos.forEach(enrolamiento->{
			DatoPersona datoPersona = datosPersonasPorIdPersona.get(enrolamiento.IdPersona);
			if(esObjetoValido(datoPersona)) {
				ResultadoAdministrarBorradorEntidad documentoIdentidad = documentosIdentidadesPorIdDocumentoIdentidadPersona.get(datoPersona.IdDocumentoIdentidad);
				enrolamiento.IdDocumentoIdentidad = esObjetoValido(documentoIdentidad) ? documentoIdentidad.IdEntidad : 0L;
			}
		});
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearBorradorEnrolamientoDatosPersona.actualizarIdDocumentoIdentidad", fin - inicio);
		return enrolamientos;
	}
	
	private List<Enrolamiento> actualizarIdDocumentoIdentidadAdicional(List<Enrolamiento> enrolamientos, List<ResultadoAdministrarBorradorEntidad> documentosIdentidades, List<DatoPersona> datosPersonas) {
		logger.trace("INICIO::CrearBorradorEnrolamientoDatosPersona.actualizarIdDocumentoIdentidadAdicional");
		long inicio = System.currentTimeMillis();
		
		Map<Long, ResultadoAdministrarBorradorEntidad> documentosIdentidadesPorIdDocumentoIdentidadPersona = obtenerEntidadesPorIdInterno(documentosIdentidades);
		Map<Long, DatoPersona> datosPersonasPorIdPersona = obtenerDatosPersonasPorIdPersona(datosPersonas);
		
		enrolamientos.forEach(enrolamiento->{
			DatoPersona datoPersona = datosPersonasPorIdPersona.get(enrolamiento.IdPersona);
			if(esObjetoValido(datoPersona)) {
				ResultadoAdministrarBorradorEntidad documentoIdentidad = documentosIdentidadesPorIdDocumentoIdentidadPersona.get(datoPersona.IdDocumentoIdentidadAdicional);
				enrolamiento.IdDocumentoIdentidadAdicional = esObjetoValido(documentoIdentidad) ? documentoIdentidad.IdEntidad : 0L;
			}
		});
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearBorradorEnrolamientoDatosPersona.actualizarIdDocumentoIdentidadAdicional", fin - inicio);
		return enrolamientos;
	}
	
	private List<Enrolamiento> actualizarIdDireccionFisicaPrincipal(List<Enrolamiento> enrolamientos, List<ResultadoAdministrarBorradorEntidad> direccionesFisicas, List<DatoPersona> datosPersonas) {
		logger.trace("INICIO::CrearBorradorEnrolamientoDatosPersona.actualizarIdDireccionFisicaPrincipal");
		long inicio = System.currentTimeMillis();
		
		Map<Long, ResultadoAdministrarBorradorEntidad> direccionesFisicasPorIdDireccionFisicaPersona = obtenerEntidadesPorIdInterno(direccionesFisicas);
		Map<Long, DatoPersona> datosPersonasPorIdPersona = obtenerDatosPersonasPorIdPersona(datosPersonas);
		
		enrolamientos.forEach(enrolamiento->{
			DatoPersona datoPersona = datosPersonasPorIdPersona.get(enrolamiento.IdPersona);
			if(esObjetoValido(datoPersona)) {
				ResultadoAdministrarBorradorEntidad direccionFisica = direccionesFisicasPorIdDireccionFisicaPersona.get(datoPersona.IdDireccionFisicaPrincipal);
				enrolamiento.IdDireccionFisicaPrincipal = esObjetoValido(direccionFisica) ? direccionFisica.IdEntidad : 0L;
			}
		});
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearBorradorEnrolamientoDatosPersona.actualizarIdDireccionFisicaPrincipal", fin - inicio);
		return enrolamientos;
	}
	
	private List<Enrolamiento> actualizarIdDireccionElectronicaPrincipal(List<Enrolamiento> enrolamientos, List<ResultadoAdministrarBorradorEntidad> direccionesElectronicas, List<DatoPersona> datosPersonas) {
		logger.trace("INICIO::CrearBorradorEnrolamientoDatosPersona.actualizarIdDireccionElectronicaPrincipal");
		long inicio = System.currentTimeMillis();
		
		Map<Long, ResultadoAdministrarBorradorEntidad> direccionesElectronicasPorIdDireccionElectronicaPersona = obtenerEntidadesPorIdInterno(direccionesElectronicas);
		Map<Long, DatoPersona> datosPersonasPorIdPersona = obtenerDatosPersonasPorIdPersona(datosPersonas);
		
		enrolamientos.forEach(enrolamiento->{
			DatoPersona datoPersona = datosPersonasPorIdPersona.get(enrolamiento.IdPersona);
			if(esObjetoValido(datoPersona)) {
				ResultadoAdministrarBorradorEntidad direccionElectronica = direccionesElectronicasPorIdDireccionElectronicaPersona.get(datoPersona.IdDireccionElectronicaPrincipal);
				enrolamiento.IdDireccionElectronicaPrincipal = esObjetoValido(direccionElectronica) ? direccionElectronica.IdEntidad : 0L;
			}
		});
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearBorradorEnrolamientoDatosPersona.actualizarIdDireccionElectronicaPrincipal", fin - inicio);
		return enrolamientos;
	}
	
	private List<Enrolamiento> actualizarIdTelefonoPrincipal(List<Enrolamiento> enrolamientos, List<ResultadoAdministrarBorradorEntidad> telefonos, List<DatoPersona> datosPersonas) {
		logger.trace("INICIO::CrearBorradorEnrolamientoDatosPersona.actualizarIdTelefonoPrincipal");
		long inicio = System.currentTimeMillis();
		
		Map<Long, ResultadoAdministrarBorradorEntidad> telefonosPorIdTelefonoPersona = obtenerEntidadesPorIdInterno(telefonos);
		Map<Long, DatoPersona> datosPersonasPorIdPersona = obtenerDatosPersonasPorIdPersona(datosPersonas);
		
		enrolamientos.forEach(enrolamiento->{
			DatoPersona datoPersona = datosPersonasPorIdPersona.get(enrolamiento.IdPersona);
			if(esObjetoValido(datoPersona)) {
				ResultadoAdministrarBorradorEntidad telefono = telefonosPorIdTelefonoPersona.get(datoPersona.IdTelefonoPrincipal);
				enrolamiento.IdTelefonoPrincipal = esObjetoValido(telefono) ? telefono.IdEntidad : 0L;
			}
		});
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearBorradorEnrolamientoDatosPersona.actualizarIdTelefonoPrincipal", fin - inicio);
		return enrolamientos;
	}
	
	private void insertarEnrolamiento(Connection con, List<Enrolamiento> enrolamientos) throws Exception {
		logger.debug("INICIO::CrearBorradorEnrolamientoDatosPersona.insertarEnrolamiento");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		sql.batch(con, SQL_INSERT_ENROLAMIENTO_BORRADOR, enrolamientos, Enrolamiento.class);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearBorradorEnrolamientoDatosPersona.insertarEnrolamiento", fin - inicio);
	}
	
	private List<ResultadoAdministrarBorradorEntidad> obtenerIdsInsertados(List<Enrolamiento> enrolamientos) {
        logger.trace("INICIO::CrearBorradorEnrolamientoDatosPersona.obtenerIdsInsertados");
        long inicio = System.currentTimeMillis();

        List<ResultadoAdministrarBorradorEntidad> idsInsertados = enrolamientos
                .parallelStream()
                .map(detalle -> {
                    ResultadoAdministrarBorradorEntidad resultado = new ResultadoAdministrarBorradorEntidad();
                    resultado.IdEntidad = detalle.IdEnrolamiento;
                    resultado.IdInterno = detalle.IdInterno;

                    return resultado;
                })
                .collect(Collectors.toList());

        long fin = System.currentTimeMillis();
        logger.trace("FIN::CrearBorradorEnrolamientoDatosPersona.obtenerIdsInsertados", fin - inicio);

        return idsInsertados;
    }
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");		                  
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			EntradaCrearBorradorEnrolamientoDatosPersona entradaCrearBorradorEnrolamientoDatosPersona = new EntradaCrearBorradorEnrolamientoDatosPersona();
			entradaCrearBorradorEnrolamientoDatosPersona.p_IdEmpresa = 1L;
			entradaCrearBorradorEnrolamientoDatosPersona.p_IdUsuario = 21L;
			entradaCrearBorradorEnrolamientoDatosPersona.p_IdModulo = "SOC";
			entradaCrearBorradorEnrolamientoDatosPersona.p_UsaReglasNegocio = "S";
			Persona persona = new Persona();
			persona.IdInterno = 1L;
			persona.IdPersona = 1790842L;
			persona.IdTipoEntidadDestino = "CLIENTE";
			persona.IdEntidadEnrolada = 0L;
			entradaCrearBorradorEnrolamientoDatosPersona.p_Personas = Arrays.asList(persona);
			CrearBorradorEnrolamientoDatosPersona crearBorradorEnrolamientoDatosPersona = new CrearBorradorEnrolamientoDatosPersona();
			try {
				SalidaAdministrarBorrador salidaAdministrarBorrador = crearBorradorEnrolamientoDatosPersona.logica(con, entradaCrearBorradorEnrolamientoDatosPersona);
				//con.commit();
				System.out.println(salidaAdministrarBorrador.toString());
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
						
    	}
		System.out.println("== END ==");
	}
	
	
}
