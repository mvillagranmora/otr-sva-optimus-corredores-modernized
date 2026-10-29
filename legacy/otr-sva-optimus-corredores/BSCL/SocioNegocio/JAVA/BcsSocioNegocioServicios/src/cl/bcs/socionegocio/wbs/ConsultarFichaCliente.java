package cl.bcs.socionegocio.wbs;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarFichaCliente implements JavaCall {
    static final Logger logger = LogManager.getLogger(ConsultarFichaCliente.class);
   
    private final String SQL_SELECT_IDPERSONA_BLOQUEO_PERSONA = loadResourceToString("cl/bcs/socionegocio/wbs/sql/ConsultarFichaCliente_SelectIdPersonaBloqueoPersona.sql");
    private final String SQL_SELECT_IDPERSONA_BLOQUEO_CLIENTE = loadResourceToString("cl/bcs/socionegocio/wbs/sql/ConsultarFichaCliente_SelectIdPersonaBloqueoCliente.sql");
    private final String SQL_SELECT_IDPERSONA_DOCUMENTO_IDENTIDAD = loadResourceToString("cl/bcs/socionegocio/wbs/sql/ConsultarFichaCliente_SelectIdPersonaDocumentoIdentidad.sql");
    private final String SQL_SELECT_DOCUMENTO_IDENTIDAD = loadResourceToString("cl/bcs/socionegocio/wbs/sql/ConsultarFichaCliente_SelectDocumentoIdentidad.sql");
    private final String SQL_SELECT_DATOS_PERSONA = loadResourceToString("cl/bcs/socionegocio/wbs/sql/ConsultarFichaCliente_SelectPersonaPorID.sql");
    private final String SQL_SELECT_DATOSCLIENTE = loadResourceToString("cl/bcs/socionegocio/wbs/sql/ConsultarFichaCliente_SelectClientePorIdPersona.sql");
    private final String SQL_SELECT_DIRECCIONFISICA = loadResourceToString("cl/bcs/socionegocio/wbs/sql/ConsultarFichaCliente_SelectDireccionFisica.sql");
    private final String SQL_SELECT_TELEFONO = loadResourceToString("cl/bcs/socionegocio/wbs/sql/ConsultarFichaCliente_SelectTelefono.sql");
    private final String SQL_SELECT_DIRECCIONELECTRONICA = loadResourceToString("cl/bcs/socionegocio/wbs/sql/ConsultarFichaCliente_SelectDireccionElectronica.sql");
    private final String SQL_SELECT_INDICADORCLIENTE = loadResourceToString("cl/bcs/socionegocio/wbs/sql/ConsultarFichaCliente_SelectVistaIndicadorCliente.sql");
    private final String SQL_SELECT_INDICADORPERSONA = loadResourceToString("cl/bcs/socionegocio/wbs/sql/ConsultarFichaCliente_SelectVistaIndicadorPersona.sql");
    private final String SQL_SELECT_CUENTABANCARIA = loadResourceToString("cl/bcs/socionegocio/wbs/sql/ConsultarFichaCliente_SelectCuentaBancaria.sql");
    private final String SQL_SELECT_TARJETABANCARIA = loadResourceToString("cl/bcs/socionegocio/wbs/sql/ConsultarFichaCliente_SelectTarjetaBancaria.sql");
    private final String SQL_SELECT_VISTADOF = loadResourceToString("cl/bcs/socionegocio/wbs/sql/ConsultarFichaCliente_SelectVistaDOF.sql");
    private final String SQL_SELECT_VISTADETALLEDOF = loadResourceToString("cl/bcs/socionegocio/wbs/sql/ConsultarFichaCliente_SelectVistaDetalleDOF.sql");
    private final String SQL_SELECT_NOMBREFANTASIA = loadResourceToString("cl/bcs/socionegocio/wbs/sql/ConsultarFichaCliente_SelectNombreFantasia.sql");
    private final String SQL_SELECT_RELACION = loadResourceToString("cl/bcs/socionegocio/wbs/sql/ConsultarFichaCliente_SelectRelacion.sql");
    private final String SQL_SELECT_PLAN_APV = loadResourceToString("cl/bcs/socionegocio/wbs/sql/ConsultarFichaCliente_SelectPlanAPV.sql");
    private final String SQL_SELECT_FATCA = loadResourceToString("cl/bcs/socionegocio/wbs/sql/ConsultarFichaCliente_SelectFatca.sql");
    private final String SQL_SELECT_PAIS_RESIDENCIA = loadResourceToString("cl/bcs/socionegocio/wbs/sql/ConsultarFichaCliente_SelectPaisResidencia.sql");
    private final String SQL_SELECT_PORTAFOLIO_CLIENTE = loadResourceToString("cl/bcs/socionegocio/wbs/sql/ConsultarFichaCliente_SelectPortafolioCliente.sql");
    
    public static class EntradaConsultarFichaCliente extends BcsPojo {
    	public Long p_IdEmpresa;
    	public Long p_IdUsuario;
    	public String p_ClaveUsuario;
    	public List<Filtro> p_Filtros;
    }

	private static class Filtro extends BcsPojo {
		public Long IdInterno;
		public String Filtro;
		public String Valor;
	}
	
    public static class Persona extends BcsPojo {
        public Long IdPersona;
    }

	public static class SalidaConsultarFichaCliente extends BcsPojo {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR = 0L;
		public List<SocioNegocio> P_OUT_RESULTADOPROCESO;

		public SalidaConsultarFichaCliente() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_RESULTADOPROCESO = new ArrayList<SocioNegocio>();
		}
	}

	public static class SocioNegocio extends BcsPojo {
		public Long IdPersona = 0L;
		public Long IdCliente = 0L;
		public String TipoEntidad = "";
		public String TipoDocumentoIdentidad = "";
		public String DocumentoIdentidad = "";
		public String IdTipoPersona = "";
		public String TipoPersona = "";		
		public String Nombres = "";
		public String ApellidoPaterno = "";
		public String ApellidoMaterno = "";
		public String RazonSocial = "";
		public String IdGenero = "";
		public String Genero = "";
		public String IdEstado = "";
		public String Estado = "";
		public Long IdPaisNacimientoConstitucion = 0L; 
		public String PaisNacimientoConstitucion = "";
		public String IdTransparenciaFiscal = "";
		public String ResidenciaTributariaExtranjera = "";
		public Long IdPaisResidenciaCasaMatriz = 0L;
		public String PaisResidenciaCasaMatriz = "";
		public Long IdPaisNacionalidad = 0L;
		public String PaisNacionalidad = "";
		public String FechaNacimientoConstitucion = "";
		public String FechaDefuncion = "";
		public String FechaInicioActividad = "";
		public String GlobalLegalEntity = "";
		public String IdEstadoCivil = "";
		public String EstadoCivil = "";
		public String IdRegimenConyugal = "";
		public String RegimenConyugal = "";
		public Long IdGiro = 0L;
		public String Giro = "";
		public Long IdProfesion = 0L;
		public String Profesion = "";
		public String IdCargo = "";
		public String Cargo = "";
		public Long IdRangoPatrimonio = 0L;
		public String RangoPatrimonio = "";
		public String IdTipoBloqueo = "";
		public String NombreTipoBloqueo = "";
		public String IdTipoCanalIngresoCorredor = "";
		public List<TiposDocumento> TiposDocumento = new ArrayList<TiposDocumento>();
		public List<CuentaBancaria> CuentaBancaria = new ArrayList<CuentaBancaria>();
		public List<TarjetaBancaria> TarjetaBancaria = new ArrayList<TarjetaBancaria>();
		public List<Telefono> Telefono = new ArrayList<Telefono>();
		public List<DireccionFisica> DireccionFisica = new ArrayList<DireccionFisica>();
		public List<DireccionElectronica> DireccionElectronica = new ArrayList<DireccionElectronica>();
		public List<IndicadorPersona> IndicadorPersona = new ArrayList<IndicadorPersona>();
		public List<Relacion> Relaciones = new ArrayList<Relacion>();
		public List<NombreFantasia> NombreFantasia = new ArrayList<NombreFantasia>();
		public FatcaCrs FatcaCrs;
		public DatosCliente DatosCliente;
		public String IdSituacionLaboral;
		public List<DatosPortafolio> DatosPortafolios = new ArrayList<>();
		public String CodigoRangoRenta = "";
		public String RangoRenta = "";
		public String CodigoRangoNivelVenta = "";
		public String RangoNivelVenta = "";
		public String CodigoPropositoTransaccion = "";
		public String PropositoTransaccion = "";
	}

	public static class DatosCliente extends BcsPojo {
		public Long IdCliente = 0L;
		public Long IdPersona = 0L;
		public String IdCategoriaInversionista = "";
		public String CategoriaInversionista = "";
		public Long CodigoEjecutivo = 0L;
		public String Ejecutivo = "";
		public String IdTipoCliente = "";
		public String TipoCliente = "";
		public String IdTipoBloqueo = "";
		public String NombreTipoBloqueo = "";
		public Long IdSegmentoCliente = 0L;
		public String SegmentoCliente = "";
		public String TipoEnrolamiento = "";
		public String IdEstado = "";
		public String Estado = "";
		public String UltimaModificacion = "";
		public List<IndicadorCliente> IndicadorCliente = new ArrayList<>();
		public List<DeclaracionOrigenFondo> DeclaracionOrigenFondo = new ArrayList<>();
		public List<PlanAPV> PlanAPV = new ArrayList<PlanAPV>();
	}

	public static class TiposDocumento extends BcsPojo {
		public Long IdPersona = 0L;
		public Long IdDocumentoIdentidad = 0L;
		public String TipoDocumento = "";
		public String NumeroDocumento = "";
		public String NumeroSerie = "";
		public String FechaEmision = "";
		public String FechaVencimiento = "";
	}

	public static class DireccionFisica extends BcsPojo {
		public Long IdPersona = 0L;
		public Long IdDireccionFisica = 0L;
		public String IdProposito = "";
		public String Proposito = "";
		public String Sector = "";
		public String CodigoPostal = "";
		public Long IdComuna = 0L;
		public String Comuna = "";
		public String Calle = "";
		public String Numero = "";
		public String Departamento = "";
		public String Complemento = "";
		public Long IdCiudad = 0L;
		public String Ciudad = "";
	}

	public static class Telefono extends BcsPojo {
		public Long IdPersona = 0L;
		public Long IdTelefono = 0L;
		public String IdProposito = "";
		public String Proposito = "";
		public String Numero = "";
		public Long IdPais = 0L;
		public String Pais = "";
		public String Tipo = "";
		public String Anexo = "";
	}

	public static class DireccionElectronica extends BcsPojo {
		public Long IdPersona = 0L;
		public Long IdDireccionElectronica = 0L;
		public String Tipo = "";
		public String IdProposito = "";
		public String Proposito = "";
		public String Direccion = "";
		public String IdEstado = "";
		public String Estado = "";
	}

	public static class IndicadorCliente extends BcsPojo {
		public Long IdCliente = 0L;
		public String ValorIndicador = "";
		public String CodigoTipoIndicador = "";
		public String GrupoIndicador = "";
	}

	public static class IndicadorPersona extends BcsPojo {
		public Long IdPersona = 0L;
		public String ValorIndicador = "";
		public String CodigoTipoIndicador = "";
		public String GrupoIndicador = "";
	}
	
	public static class DatosPortafolio extends BcsPojo{
		public Long IdPortafolio;
		public String CodigoPortafolio;
		public String IdFormaCobro;
	    public String IdPerfilComercial;
	    public String IdPerfilCobros;
	    public String IdPerfilProductos;
	    public Long IdCliente;
	}

	public static class CuentaBancaria extends BcsPojo {
		public Long IdPersona;
		public Long IdCuentaBancaria = 0L;
		public String NumeroCuentaBancaria = "";
		public String IdEstado = "";
		public String IdMoneda = "";
		public String CodigoTipoCuentaBancaria = "";
		public String TipoCuentaBancaria = "";
		public Long IdBanco = 0L;
		public String Banco = "";
                public Long IdPlaza = 0L;
                public String NombrePlaza = "";
	}

	public static class Relacion extends BcsPojo {
		public Long IdRelacion = 0L;
		public Long IdPersona = 0L;
		public Long IdPersonaRelacionada = 0L;
		public String IdTipoRelacion = "";
		public String NombreTipoRelacion = "";
		public String TipoDocumentoIdentidad = "";
		public String DocumentoIdentidad = "";
		public String NombreCompleto = "";

	}

	public static class TarjetaBancaria extends BcsPojo {
		public Long IdPersona = 0L;
		public Long IdTarjetaBancaria = 0L;
		public Long IdBanco = 0L;
		public String Banco = "";
		public String IdTipo = "";
		public String Tipo = "";
		public String NumeroTarjeta = "";
		public Integer CodigoVerificacion = 0;
		public String FechaVencimiento = "";
	}

	public static class DeclaracionOrigenFondo extends BcsPojo {
		public Long IdPersona;
		public Long IdCliente;
		public Long IdDeclaracionOrigenFondos = 0L;
		public String IdTipoOperacionOrigenFondos = "";
		public String FechaDeclaracion = "";
		public String Comentario = "";
		public String OperacionInscripcion = "";
		public String IdEstado = "";
		public List<DetalleOrigenFondo> DetalleOrigenFondo;
	}

	public static class SelectIdsPersona extends BcsPojo {
		public Long IdPersona = 0L;
		public Long IdCliente = 0L;
	}

	public static class DetalleOrigenFondo extends BcsPojo {
		public Long IdDeclaracionOrigenFondos = 0L;
		public String IdTipoOrigenFondo = "";
		public String TipoOrigenFondo = "";
		public String Valor = "0.0";
		public String Otros = "";
	}

	public static class NombreFantasia extends BcsPojo {
		public Long IdPersona = 0L;
		public Long IdNombreFantasia = 0L;
		public String Nombre = "";
	}

	public static class PlanAPV extends BcsPojo {
		public Long IdCliente = 0L;
		public Integer IdPlanTipoInversion = 0;
		public String NombrePlanTipoInversion = "";
		public String IdEstado = "";
	}
	
	public static class FatcaCrs extends BcsPojo {
		public Long IdPersona = 0L;
		public String IdClasificacionFatca = "";
		public String NombreClasificacionFatca = "";
		public String IdClasificacionEmpresaFatca = "";
		public String NombreClasificacionEmpresaFatca = "";
		public String NumeroGIIN = "";
		public String FechaFirmaFormularioString = "";
		public String AutorizacionInformarOrganismo = "";
		public String NACEEUU = "";
		public String NACEEUUExplicacion = "";
		public String ObligacionesFiscales = "";
		public String ObligacionesFiscalesExplicacion = "";
		public String ExcepcionResidente = "";
		public String ExcepcionNacimiento = "";
		public String ExcepcionDomicilio = "";
		public String ExcepcionTelefono = "";
		public String ExcepcionOrdenPermanente = "";
		public String ExcepcionFirma = "";
		public String ParticipacionSociedadesUSA = "";
		public String OrdenPermanente = "";
		public List<PaisResidencia> PaisResidencia = new ArrayList<PaisResidencia>();
	}
	
	public static class PaisResidencia extends BcsPojo {
		public Long IdPersona = 0L;
		public Long IdPais = 0L;
		public String Pais = "";
		public String Tin = "";
		public String NoTin = "";
		public String OtraCausa = "";
	}

	public SalidaConsultarFichaCliente logica(Connection conn, EntradaConsultarFichaCliente in) throws Exception {
		logger.info("INICIO::ConsultarFichaCliente");
		long inicio = System.currentTimeMillis();
		
		SalidaConsultarFichaCliente salida = new SalidaConsultarFichaCliente();

		List<String> documentosIdentidad = in.p_Filtros.parallelStream()
				.filter(Filtro -> "DocumentoIdentidad".equals(Filtro.Filtro))
				.map(Filtro -> Filtro.Valor)
				.collect(Collectors.toList());
		List<String> tiposBloqueoCliente = in.p_Filtros.parallelStream()
				.filter(Filtro -> "TipoBloqueoCliente".equals(Filtro.Filtro))
				.map(Filtro -> Filtro.Valor)
				.collect(Collectors.toList());
		List<String> tiposBloqueoPersona = in.p_Filtros.parallelStream()
				.filter(Filtro -> "TipoBloqueoPersona".equals(Filtro.Filtro))
				.map(Filtro -> Filtro.Valor)
				.collect(Collectors.toList());

		List<Long> idPersonaDocumentoIdentidad = obtenerIdPersonas(conn, documentosIdentidad, SQL_SELECT_IDPERSONA_DOCUMENTO_IDENTIDAD);
		List<Long> idtiposBloqueoCliente = obtenerIdPersonas(conn, tiposBloqueoCliente, SQL_SELECT_IDPERSONA_BLOQUEO_CLIENTE);
		List<Long> idtiposBloqueoPersona = obtenerIdPersonas(conn, tiposBloqueoPersona, SQL_SELECT_IDPERSONA_BLOQUEO_PERSONA);

		List<Long> idsPersonas = Stream.of(idPersonaDocumentoIdentidad, idtiposBloqueoCliente, idtiposBloqueoPersona)
				.distinct()
				.flatMap(Collection::stream)
				.collect(Collectors.toList());
		
		List<SocioNegocio> datosSocioNegocio = obtenerSociosNegocio(conn,idsPersonas,in.p_IdEmpresa);
		List<Long> idClientes = datosSocioNegocio.parallelStream()
			.map(socioNegocio -> socioNegocio.IdCliente)
			.collect(Collectors.toList());
        
				Map<Long, List<TiposDocumento>> documentosPorPersona = obtenerDocumentosIdentidad(conn, idsPersonas);
                Map<Long, List<CuentaBancaria>> cuentasBancariasPorPersona = obtenerCuentasBancarias(conn, idsPersonas,in.p_IdEmpresa);
                Map<Long, List<TarjetaBancaria>> tarjetasBancariasPorPersona = obtenerTarjetasBancarias(conn, idsPersonas,in.p_IdEmpresa);
                Map<Long, List<Telefono>> telefonosPorPersona = obtenerTelefonos(conn, idsPersonas);
                Map<Long, List<DireccionElectronica>> direccionElectronicaPorPersona = obtenerDireccionesElectronicas(conn, idsPersonas);
                Map<Long, List<DireccionFisica>> direccionFisicaPorPersona = obtenerDireccionesFisicas(conn,idsPersonas);
                Map<Long, List<IndicadorPersona>> indicadoresPorPersona = obtenerIndicadoresPersona(conn, idsPersonas,in.p_IdEmpresa);
                Map<Long, List<IndicadorCliente>> indicadoresPorCliente = obtenerIndicadoresCliente(conn, idClientes,in.p_IdEmpresa);
                Map<Long, List<Relacion>> relacionesPorPersona = obtenerRelacionesPersona(conn, idsPersonas,in.p_IdEmpresa);
                Map<Long, List<NombreFantasia>> nombresFantasiaPorCliente = obtenerNombresFantasia(conn, idsPersonas);
                Map<Long, List<DetalleOrigenFondo>> dofDetallePorCliente = obtenerDetalleDofPorId(conn,idClientes,in.p_IdEmpresa);
                Map<Long, List<DeclaracionOrigenFondo>> dofPorCliente = obtenerDofPorCliente(conn, idClientes,in.p_IdEmpresa,dofDetallePorCliente);
                Map<Long, List<PlanAPV>> planesApvPorCliente = obtenerApvPorCliente(conn, idClientes);
                Map<Long, DatosCliente> datosClientes = obtenerDatosCliente(conn, idsPersonas,indicadoresPorCliente,dofPorCliente,planesApvPorCliente);
                Map<Long, List<PaisResidencia>> paisResidenciaPorPersona = obtenerPaisResidenciaFatca(conn, idsPersonas);
                Map<Long, FatcaCrs> fatcaPorPersona = obtenerFatca(conn,idsPersonas,paisResidenciaPorPersona);
                Map<Long,List<DatosPortafolio>> portafoliosPorIdCliente = obtenerDatosPortafolio(conn, idClientes, in.p_IdEmpresa);
        
        salida.P_OUT_RESULTADOPROCESO = datosSocioNegocio
                .parallelStream()
                .map(socioNegocio -> {
                	SocioNegocio socio = socioNegocio;
                	socio.TiposDocumento = documentosPorPersona.getOrDefault(socioNegocio.IdPersona,new ArrayList<>());
                	socio.CuentaBancaria = cuentasBancariasPorPersona.getOrDefault(socioNegocio.IdPersona,new ArrayList<>());
                	socio.TarjetaBancaria = tarjetasBancariasPorPersona.getOrDefault(socio.IdPersona,new ArrayList<>());
                	socio.Telefono = telefonosPorPersona.getOrDefault(socio.IdPersona,new ArrayList<>());
                	socio.DireccionElectronica = direccionElectronicaPorPersona.getOrDefault(socio.IdPersona,new ArrayList<>());
                	socio.DireccionFisica = direccionFisicaPorPersona.getOrDefault(socio.IdPersona,new ArrayList<>());
                	socio.IndicadorPersona = indicadoresPorPersona.getOrDefault(socio.IdPersona,new ArrayList<>());
                	socio.NombreFantasia = nombresFantasiaPorCliente.getOrDefault(socio.IdPersona,new ArrayList<>());
                	socio.Relaciones = relacionesPorPersona.getOrDefault(socio.IdPersona,new ArrayList<>());
                	socio.DatosCliente = datosClientes.get(socio.IdPersona);
                	socio.FatcaCrs = fatcaPorPersona.get(socio.IdPersona); 
                	socio.DatosPortafolios = portafoliosPorIdCliente.getOrDefault(socio.IdCliente, Collections.emptyList());
                	return socio;
                })
                .collect(Collectors.toList());       
        
		long fin = System.currentTimeMillis();
		logger.info("FIN::ConsultarFichaCliente", fin - inicio);
		return salida;
	}
	
	private Map<Long,FatcaCrs> obtenerFatca(Connection conn, List<Long> idPersonas, Map<Long, List<PaisResidencia>> paisesResidencia) throws SQLException {
        logger.debug("INICIO::ConsultarFichaCliente.obtenerFatca");
        long inicio = System.currentTimeMillis();

        SQL sql = new SQL();
        HashMap<String, Object> parametros = new HashMap<>(2);
        parametros.put("p_IdPersonas", idPersonas);
     
         Map<Long, FatcaCrs> fatcaCrs = sql.query(conn, SQL_SELECT_FATCA, parametros, FatcaCrs.class)
                        .parallelStream()
                        .collect(Collectors.toMap(fatca -> fatca.IdPersona, fatca -> {
                        	FatcaCrs f = new FatcaCrs();
                        	f = fatca;
                        	f.PaisResidencia = paisesResidencia.getOrDefault(fatca.IdPersona,new ArrayList<>());
                        	return f;
                        }));
        long fin = System.currentTimeMillis();
        logger.debug("FIN::ConsultarFichaCliente.obtenerFatca", fin - inicio);

        return fatcaCrs;
	}

	private Map<Long, List<PaisResidencia>> obtenerPaisResidenciaFatca(Connection conn, List<Long> idPersonas) throws SQLException {
        logger.debug("INICIO::ConsultarFichaCliente.obtenerPaisResidenciaFatca");
        long inicio = System.currentTimeMillis();

        SQL sql = new SQL();
        HashMap<String, Object> parametros = new HashMap<>(2);
        parametros.put("p_IdPersonas", idPersonas);
 
        Map<Long,List<PaisResidencia>> paisesResidencia = sql.query(conn, SQL_SELECT_PAIS_RESIDENCIA, parametros, PaisResidencia.class)
        		.parallelStream()
                .collect(Collectors.groupingBy(paisResidencia -> paisResidencia.IdPersona, Collectors.mapping(paisResidencia -> paisResidencia, Collectors.toList())));;
        long fin = System.currentTimeMillis();
        logger.debug("FIN::ConsultarFichaCliente.obtenerPaisResidenciaFatca", fin - inicio);

        return paisesResidencia;
	}

	private Map<Long, List<PlanAPV>> obtenerApvPorCliente(Connection conn, List<Long> idClientes) throws SQLException {
        logger.debug("INICIO::ConsultarFichaCliente.obtenerApvPorCliente");
        long inicio = System.currentTimeMillis();

        SQL sql = new SQL();
        HashMap<String, Object> parametros = new HashMap<>(2);
        parametros.put("p_IdClientes", idClientes);
 
        Map<Long,List<PlanAPV>> planesApv = sql.query(conn, SQL_SELECT_PLAN_APV, parametros, PlanAPV.class)
        		.parallelStream()
                .collect(Collectors.groupingBy(apv -> apv.IdCliente, Collectors.mapping(apv -> apv, Collectors.toList())));;
        long fin = System.currentTimeMillis();
        logger.debug("FIN::ConsultarFichaCliente.obtenerApvPorCliente", fin - inicio);

        return planesApv;
	}

	private Map<Long, List<Relacion>> obtenerRelacionesPersona(Connection conn, List<Long> idsPersonas,Long idEmpresa) throws SQLException {
        logger.debug("INICIO::ConsultarFichaCliente.obtenerRelacionesPersona");
        long inicio = System.currentTimeMillis();

        SQL sql = new SQL();
        HashMap<String, Object> parametros = new HashMap<>(3);
        parametros.put("p_IdPersonas", idsPersonas);
        parametros.put("p_IdEmpresa", idEmpresa);
 
        Map<Long, List<Relacion>> relaciones = sql.query(conn, SQL_SELECT_RELACION, parametros, Relacion.class)
                .parallelStream()
                .collect(Collectors.groupingBy(relacion -> relacion.IdPersona, Collectors.mapping(relacion -> relacion, Collectors.toList())));
        
        long fin = System.currentTimeMillis();
        logger.debug("FIN::ConsultarFichaCliente.obtenerRelacionesPersona", fin - inicio);

        return relaciones;
	}

	private Map<Long, List<DireccionFisica>> obtenerDireccionesFisicas(Connection conn, List<Long> idsPersonas) throws SQLException {
        logger.debug("INICIO::ConsultarFichaCliente.obtenerDireccionesFisicas");
        long inicio = System.currentTimeMillis();

        SQL sql = new SQL();
        HashMap<String, Object> parametros = new HashMap<>(2);
        parametros.put("p_IdPersonas", idsPersonas);
 
        Map<Long, List<DireccionFisica>> direccionesFisicas = sql.query(conn, SQL_SELECT_DIRECCIONFISICA, parametros, DireccionFisica.class)
                .parallelStream()
                .collect(Collectors.groupingBy(direccion -> direccion.IdPersona, Collectors.mapping(direccion -> direccion, Collectors.toList())));
        
        long fin = System.currentTimeMillis();
        logger.debug("FIN::ConsultarFichaCliente.obtenerDireccionesFisicas", fin - inicio);

        return direccionesFisicas;
	}

	private Map<Long,List<DetalleOrigenFondo>> obtenerDetalleDofPorId(Connection conn, List<Long> idClientes, Long idEmpresa) throws SQLException{
        logger.debug("INICIO::ConsultarFichaCliente.obtenerDetalleDofPorCliente");
        long inicio = System.currentTimeMillis();

        SQL sql = new SQL();
        HashMap<String, Object> parametros = new HashMap<>(4);
        parametros.put("p_IdClientes", idClientes);
        parametros.put("p_IdEmpresa", idEmpresa);
 
        Map<Long,List<DetalleOrigenFondo>> detallesOrigenFondo = sql.query(conn, SQL_SELECT_VISTADETALLEDOF, parametros, DetalleOrigenFondo.class)
        		.parallelStream()
                .collect(Collectors.groupingBy(detalleDof -> detalleDof.IdDeclaracionOrigenFondos, Collectors.mapping(detalleDof -> detalleDof, Collectors.toList())));;
        long fin = System.currentTimeMillis();
        logger.debug("FIN::ConsultarFichaCliente.obtenerDetalleDofPorCliente", fin - inicio);

        return detallesOrigenFondo;
	}
	
    private Map<Long, List<DeclaracionOrigenFondo>> obtenerDofPorCliente(Connection conn, List<Long> idClientes,Long idEmpresa, Map<Long,List<DetalleOrigenFondo>> detalleDof) throws SQLException {
        logger.debug("INICIO::ConsultarFichaCliente.obtenerDofPorCliente");
        long inicio = System.currentTimeMillis();

        SQL sql = new SQL();
        HashMap<String, Object> parametros = new HashMap<>(3);
        parametros.put("p_IdClientes", idClientes);
        parametros.put("p_IdEmpresa", idEmpresa);
 
        Map<Long, List<DeclaracionOrigenFondo>> dofPorCliente = sql.query(conn, SQL_SELECT_VISTADOF, parametros, DeclaracionOrigenFondo.class)
                .parallelStream()
                .collect(Collectors.groupingBy(declaracion -> declaracion.IdCliente, Collectors.mapping(declaracion -> {
                	DeclaracionOrigenFondo declaracionOrigenFondo = new DeclaracionOrigenFondo();
                	declaracionOrigenFondo = declaracion;
                	declaracionOrigenFondo.DetalleOrigenFondo = detalleDof.getOrDefault(declaracion.IdDeclaracionOrigenFondos,new ArrayList<>());
                	return declaracionOrigenFondo;
                }, Collectors.toList())));
        
        long fin = System.currentTimeMillis();
        logger.debug("FIN::ConsultarFichaCliente.obtenerDofPorCliente", fin - inicio);

        return dofPorCliente;
	}

	private Map<Long, DatosCliente> obtenerDatosCliente(Connection conn, List<Long> idsPersonas, Map<Long, List<IndicadorCliente>> indicadoresPorCliente, Map<Long, List<DeclaracionOrigenFondo>> dofPorCliente, Map<Long, List<PlanAPV>> planesApvPorCliente) throws SQLException {
        logger.debug("INICIO::ConsultarFichaCliente.obtenerDatosCliente");
        long inicio = System.currentTimeMillis();

        SQL sql = new SQL();
        HashMap<String, Object> parametros = new HashMap<>(2);
        parametros.put("p_IdPersonas", idsPersonas);
 
        Map<Long, DatosCliente> datosClientes = sql.query(conn, SQL_SELECT_DATOSCLIENTE, parametros, DatosCliente.class)
                .parallelStream()
                .collect(Collectors.toMap(datosCliente -> datosCliente.IdPersona, datosCliente -> {
                	DatosCliente datos = new DatosCliente();
                	datos = datosCliente;
                	datos.IndicadorCliente = indicadoresPorCliente.getOrDefault(datosCliente.IdCliente,new ArrayList<>());
                	datos.DeclaracionOrigenFondo = dofPorCliente.getOrDefault(datosCliente.IdCliente,new ArrayList<>());
                	datos.PlanAPV = planesApvPorCliente.getOrDefault(datosCliente.IdCliente,new ArrayList<>());
                	return datos;
                }));
        
        long fin = System.currentTimeMillis();
        logger.debug("FIN::ConsultarFichaCliente.obtenerDatosCliente", fin - inicio);

        return datosClientes;
	}

	private Map<Long, List<NombreFantasia>> obtenerNombresFantasia(Connection conn, List<Long> idsPersonas) throws SQLException {
        logger.debug("INICIO::ConsultarFichaCliente.obtenerNombresFantasia");
        long inicio = System.currentTimeMillis();

        SQL sql = new SQL();
        HashMap<String, Object> parametros = new HashMap<>(2);
        parametros.put("p_IdPersonas", idsPersonas);
 
        Map<Long, List<NombreFantasia>> nombresFantasia = sql.query(conn, SQL_SELECT_NOMBREFANTASIA, parametros, NombreFantasia.class)
                .parallelStream()
                .collect(Collectors.groupingBy(nombreFantasia -> nombreFantasia.IdPersona, Collectors.mapping(nombreFantasia -> nombreFantasia, Collectors.toList())));
        
        long fin = System.currentTimeMillis();
        logger.debug("FIN::ConsultarFichaCliente.obtenerNombresFantasia", fin - inicio);

        return nombresFantasia;
	}

	private Map<Long, List<IndicadorCliente>> obtenerIndicadoresCliente(Connection conn, List<Long> idClientes,Long idEmpresa) throws SQLException {
        logger.debug("INICIO::ConsultarFichaCliente.obtenerIndicadoresCliente");
        long inicio = System.currentTimeMillis();

        SQL sql = new SQL();
        HashMap<String, Object> parametros = new HashMap<>(3);
        parametros.put("p_IdClientes", idClientes);
        parametros.put("p_IdEmpresa", idEmpresa);
 
        Map<Long, List<IndicadorCliente>> indicadoresCliente = sql.query(conn, SQL_SELECT_INDICADORCLIENTE, parametros, IndicadorCliente.class)
                .parallelStream()
                .collect(Collectors.groupingBy(indicador -> indicador.IdCliente, Collectors.mapping(indicador -> indicador, Collectors.toList())));
        
        long fin = System.currentTimeMillis();
        logger.debug("FIN::ConsultarFichaCliente.obtenerIndicadoresCliente", fin - inicio);

        return indicadoresCliente;
	}
	
    private Map<Long, List<IndicadorPersona>> obtenerIndicadoresPersona(Connection conn, List<Long> idsPersonas,Long idEmpresa) throws SQLException {
        logger.debug("INICIO::ConsultarFichaCliente.obtenerIndicadoresPersona");
        long inicio = System.currentTimeMillis();

        SQL sql = new SQL();
        HashMap<String, Object> parametros = new HashMap<>(3);
        parametros.put("p_IdPersonas", idsPersonas);
        parametros.put("p_IdEmpresa", idEmpresa);
        Map<Long, List<IndicadorPersona>> indicadoresPersona = sql.query(conn, SQL_SELECT_INDICADORPERSONA, parametros, IndicadorPersona.class)
                .parallelStream()
                .collect(Collectors.groupingBy(indicador -> indicador.IdPersona, Collectors.mapping(indicador -> indicador, Collectors.toList())));
        
        long fin = System.currentTimeMillis();
        logger.debug("FIN::ConsultarFichaCliente.obtenerIndicadoresPersona", fin - inicio);

        return indicadoresPersona;
	}
    
    private  Map<Long,List<DatosPortafolio>> obtenerDatosPortafolio(Connection conn, List<Long> idClientes,Long idEmpresa) throws SQLException {
        logger.debug("INICIO::ConsultarFichaCliente.obtenerDatosPortafolio");
        long inicio = System.currentTimeMillis();

        SQL sql = new SQL();
        HashMap<String, Object> parametros = new HashMap<>(2);
        parametros.put("IdCliente", idClientes);
        Map<Long, List<DatosPortafolio>> portafoliosPorIdCliente = sql.query(conn, SQL_SELECT_PORTAFOLIO_CLIENTE, parametros, DatosPortafolio.class)
        		.stream()
        		.collect(Collectors.groupingBy(portafolio -> portafolio.IdCliente));		
        
        long fin = System.currentTimeMillis();
        logger.debug("FIN::ConsultarFichaCliente.obtenerDatosPortafolio {}", fin - inicio);

        return  portafoliosPorIdCliente;
	}

	private Map<Long, List<DireccionElectronica>> obtenerDireccionesElectronicas(Connection conn, List<Long> idsPersonas) throws SQLException {
        logger.debug("INICIO::ConsultarFichaCliente.obtenerDireccionesElectronicas");
        long inicio = System.currentTimeMillis();

        SQL sql = new SQL();
        HashMap<String, Object> parametros = new HashMap<>(2);
        parametros.put("p_IdPersonas", idsPersonas);
 
        Map<Long, List<DireccionElectronica>> direccionesElectronicas = sql.query(conn, SQL_SELECT_DIRECCIONELECTRONICA, parametros, DireccionElectronica.class)
                .parallelStream()
                .collect(Collectors.groupingBy(direccionElectronica -> direccionElectronica.IdPersona, Collectors.mapping(direccionElectronica -> direccionElectronica, Collectors.toList())));
        
        long fin = System.currentTimeMillis();
        logger.debug("FIN::ConsultarFichaCliente.obtenerDireccionesElectronicas", fin - inicio);

        return direccionesElectronicas;
	}

	private Map<Long, List<Telefono>> obtenerTelefonos(Connection conn, List<Long> idsPersonas) throws SQLException {
        logger.debug("INICIO::ConsultarFichaCliente.obtenerTelefonos");
        long inicio = System.currentTimeMillis();

        SQL sql = new SQL();
        HashMap<String, Object> parametros = new HashMap<>(2);
        parametros.put("p_IdPersonas", idsPersonas);
 
        Map<Long, List<Telefono>> telefonos = sql.query(conn, SQL_SELECT_TELEFONO, parametros, Telefono.class)
                .parallelStream()
                .collect(Collectors.groupingBy(telefono -> telefono.IdPersona, Collectors.mapping(telefono -> telefono, Collectors.toList())));
        
        long fin = System.currentTimeMillis();
        logger.debug("FIN::ConsultarFichaCliente.obtenerTelefonos", fin - inicio);

        return telefonos;
	}

	private Map<Long, List<TarjetaBancaria>> obtenerTarjetasBancarias(Connection conn, List<Long> idsPersonas, Long idEmpresa) throws SQLException {
        logger.debug("INICIO::ConsultarFichaCliente.obtenerTarjetasBancarias");
        long inicio = System.currentTimeMillis();

        SQL sql = new SQL();
        HashMap<String, Object> parametros = new HashMap<>(3);
        parametros.put("p_IdPersonas", idsPersonas);
        parametros.put("p_IdEmpresa", idEmpresa);
 
        Map<Long, List<TarjetaBancaria>> tarjetasBancarias = sql.query(conn, SQL_SELECT_TARJETABANCARIA, parametros, TarjetaBancaria.class)
                .parallelStream()
                .collect(Collectors.groupingBy(tarjetaBancaria -> tarjetaBancaria.IdPersona, Collectors.mapping(tarjetaBancaria -> tarjetaBancaria, Collectors.toList())));
        
        long fin = System.currentTimeMillis();
        logger.debug("FIN::ConsultarFichaCliente.obtenerTarjetasBancarias", fin - inicio);

        return tarjetasBancarias;
	}

	private Map<Long, List<CuentaBancaria>> obtenerCuentasBancarias(Connection conn, List<Long> idsPersonas, Long idEmpresa)throws SQLException {
        logger.debug("INICIO::ConsultarFichaCliente.obtenerCuentasBancarias");
        long inicio = System.currentTimeMillis();

        SQL sql = new SQL();
        HashMap<String, Object> parametros = new HashMap<>(3);
        parametros.put("p_IdPersonas", idsPersonas);
        parametros.put("p_IdEmpresa", idEmpresa);
 
        Map<Long, List<CuentaBancaria>> cuentasBancarias = sql.query(conn, SQL_SELECT_CUENTABANCARIA, parametros, CuentaBancaria.class)
                .parallelStream()
                .collect(Collectors.groupingBy(cuentaBancaria -> cuentaBancaria.IdPersona, Collectors.mapping(cuentaBancaria -> cuentaBancaria, Collectors.toList())));
        
        long fin = System.currentTimeMillis();
        logger.debug("FIN::ConsultarFichaCliente.obtenerCuentasBancarias", fin - inicio);

        return cuentasBancarias;
	}

	private List<SocioNegocio> obtenerSociosNegocio(Connection conn, List<Long> idsPersona, Long idEmpresa) throws SQLException {
        logger.debug("INICIO::ConsultarFichaCliente.obtenerSociosNegocio");
        long inicio = System.currentTimeMillis();

        SQL sql = new SQL();
        HashMap<String, Object> parametros = new HashMap<>(3);
        parametros.put("p_IdEmpresa", idEmpresa);
        parametros.put("p_IdPersonas", idsPersona);

        List<SocioNegocio> sociosNegocio = sql.query(conn, SQL_SELECT_DATOS_PERSONA, parametros, SocioNegocio.class);
        
        long fin = System.currentTimeMillis();
        logger.debug("FIN::ConsultarFichaCliente.obtenerSociosNegocio", fin - inicio);

        return sociosNegocio;
    }
	
    private Map<Long,List<TiposDocumento>> obtenerDocumentosIdentidad(Connection conn, List<Long> idsPersona) throws SQLException {
        logger.debug("INICIO::ConsultarFichaCliente.obtenerDocumentosIdentidad");
        long inicio = System.currentTimeMillis();

        SQL sql = new SQL();
        HashMap<String, Object> parametros = new HashMap<>(2);
        parametros.put("p_IdPersonas", idsPersona);
 
        Map<Long, List<TiposDocumento>> documentosIdentidad = sql.query(conn, SQL_SELECT_DOCUMENTO_IDENTIDAD, parametros, TiposDocumento.class)
                .parallelStream()
                .collect(Collectors.groupingBy(documentoIdentidad -> documentoIdentidad.IdPersona, Collectors.mapping(documentoIdentidad -> documentoIdentidad, Collectors.toList())));
        
        long fin = System.currentTimeMillis();
        logger.debug("FIN::ConsultarFichaCliente.obtenerDocumentosIdentidad", fin - inicio);

        return documentosIdentidad;
    }
    
	private List<Long> obtenerIdPersonas(Connection conn,List<String> entidades, String query) throws SQLException {
        logger.debug("INICIO::ConsultarFichaCliente.obtenerIdPersonas");
        long inicio = System.currentTimeMillis();
        
        SQL sql = new SQL();
        HashMap<String, Object> parametros = new HashMap<>(2);
        parametros.put("IdEntidades", entidades);

        List<Persona> resultadoPersonas = sql.query(conn, query, parametros, Persona.class);
        List<Long> idPersonas = resultadoPersonas.parallelStream().map(Persona -> Persona.IdPersona).collect(Collectors.toList());    
        
        long fin = System.currentTimeMillis();
        logger.debug("FIN::ConsultarFichaCliente.obtenerIdPersonas", fin - inicio);
		return idPersonas;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaConsultarFichaCliente in = gson.fromJson(jin.toString(), EntradaConsultarFichaCliente.class);
		SalidaConsultarFichaCliente out = logica(conn, in);

		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public static void main(String[] args) {
		System.out.println("== INICIO PRUEBA LOCAL ==");

		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd")
					.serializeNulls().create();

			String jsonCL = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1,\"p_ClaveUsuario\":\"\",\"p_Filtros\":[{\"IdInterno\":0,\"Filtro\":\"DocumentoIdentidad\",\"Valor\":\"19170266-2\"}]}";

			EntradaConsultarFichaCliente in = gson.fromJson(jsonCL, EntradaConsultarFichaCliente.class);

			ConsultarFichaCliente demo = new ConsultarFichaCliente();
			System.out.println("ENTRADA");
			System.out.println(in);

			try {
				SalidaConsultarFichaCliente out = demo.logica(con, in);
				con.rollback();
				System.out.println("SALIDA");
				System.out.println(out);
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
			con.rollback();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		System.out.println("== FIN PRUEBA LOCAL ==");
	}
}