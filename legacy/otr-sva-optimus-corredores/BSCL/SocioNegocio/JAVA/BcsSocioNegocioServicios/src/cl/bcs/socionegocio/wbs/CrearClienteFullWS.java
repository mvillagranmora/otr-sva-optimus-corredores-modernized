package cl.bcs.socionegocio.wbs;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.acc.CrearAutorizadoOrden;
import cl.bcs.plataforma.acc.CrearAutorizadoOrden.CrearAutorizadoOrden_Entrada;
import cl.bcs.plataforma.acc.CrearAutorizadoOrden.CrearAutorizadoOrden_Salida;
import cl.bcs.plataforma.frm.ValidarCamposObligatorios;
import cl.bcs.plataforma.frm.ValidarContenidoCampos;
import cl.bcs.plataforma.frm.ValidarCamposObligatorios.SalidaValidarCamposObligatorios;
import cl.bcs.plataforma.frm.pojo.FormularioCanal;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.enr.ConsultarEntidadEnrolamiento;
import cl.bcs.socionegocio.enr.ConsultarEntidadEnrolamiento.EntradaConsultarEntidadEnrolamiento;
import cl.bcs.socionegocio.enr.ConsultarEntidadEnrolamiento.SalidaConsultarEntidadEnrolamiento;
import cl.bcs.socionegocio.enr.CrearBorradorCuentaBancaria;
import cl.bcs.socionegocio.enr.CrearBorradorCuentaBancaria.EntradaCrearBorradorCuentaBancaria;
import cl.bcs.socionegocio.enr.CrearBorradorCuentaCustodioPortafolio;
import cl.bcs.socionegocio.enr.CrearBorradorCuentaCustodioPortafolio.EntradaCrearBorradorCuentaCustodioPortafolio;
import cl.bcs.socionegocio.enr.CrearBorradorDeclaracionOrigenFondos;
import cl.bcs.socionegocio.enr.CrearBorradorDeclaracionOrigenFondos.EntradaCrearBorradorDeclaracionOrigenFondos;
import cl.bcs.socionegocio.enr.CrearBorradorDireccionElectronica;
import cl.bcs.socionegocio.enr.CrearBorradorDireccionElectronica.EntradaCrearBorradorDireccionElectronica;
import cl.bcs.socionegocio.enr.CrearBorradorDireccionFisica;
import cl.bcs.socionegocio.enr.CrearBorradorDireccionFisica.EntradaCrearBorradorDireccionFisica;
import cl.bcs.socionegocio.enr.CrearBorradorDocumentoIdentidad;
import cl.bcs.socionegocio.enr.CrearBorradorDocumentoIdentidad.EntradaCrearBorradorDocumentoIdentidad;
import cl.bcs.socionegocio.enr.CrearBorradorEnrolamiento;
import cl.bcs.socionegocio.enr.CrearBorradorEnrolamiento.BorradorEnrolamiento;
import cl.bcs.socionegocio.enr.CrearBorradorEnrolamiento.EntradaCrearBorradorEnrolamiento;
import cl.bcs.socionegocio.enr.CrearBorradorEnrolamiento.SalidaCrearBorradorEnrolamiento;
import cl.bcs.socionegocio.enr.CrearBorradorFatcaPaisResidencia;
import cl.bcs.socionegocio.enr.CrearBorradorFatcaPaisResidencia.EntradaCrearBorradorFatcaPaisResidencia;
import cl.bcs.socionegocio.enr.CrearBorradorNombreFantasia;
import cl.bcs.socionegocio.enr.CrearBorradorRelacionPersona;
import cl.bcs.socionegocio.enr.CrearBorradorRelacionPersona.EntradaCrearBorradorRelacionPersona;
import cl.bcs.socionegocio.enr.CrearBorradorTarjetaBancaria;
import cl.bcs.socionegocio.enr.CrearBorradorTarjetaBancaria.EntradaCrearBorradorTarjetaBancaria;
import cl.bcs.socionegocio.enr.CrearBorradorTelefono;
import cl.bcs.socionegocio.enr.CrearBorradorTelefono.EntradaCrearBorradorTelefono;
import cl.bcs.socionegocio.enr.CrearClienteFull;
import cl.bcs.socionegocio.enr.CrearClienteFull.SalidaCrearClienteFull;
import cl.bcs.socionegocio.enr.ModificarBorradorClienteDatosGenerales;
import cl.bcs.socionegocio.enr.ModificarBorradorClienteDatosGenerales.EntradaModificarBorradorClienteDatosGenerales;
import cl.bcs.socionegocio.enr.ModificarBorradorDocumentoIdentidad;
import cl.bcs.socionegocio.enr.ModificarBorradorDocumentoIdentidad.EntradaModificarBorradorDocumentoIdentidad;
import cl.bcs.socionegocio.enr.ModificarBorradorFatca;
import cl.bcs.socionegocio.enr.ModificarBorradorFatca.EntradaModificarBorradorFatca;
import cl.bcs.socionegocio.enr.ModificarBorradorIndicador;
import cl.bcs.socionegocio.enr.ModificarBorradorIndicador.EntradaModificarBorradorIndicador;
import cl.bcs.socionegocio.enr.ModificarBorradorPersonaDatosGenerales;
import cl.bcs.socionegocio.enr.ModificarBorradorPersonaDatosGenerales.EntradaModificarBorradorPersonaDatosGenerales;
import cl.bcs.socionegocio.enr.CrearBorradorNombreFantasia.EntradaCrearBorradorNombreFantasia;
import cl.bcs.socionegocio.enr.pojo.ResultadoAdministrarBorradorEntidad;
import cl.bcs.socionegocio.enr.pojo.SalidaAdministrarBorrador;
import cl.bcs.socionegocio.per.CrearDireccionElectronicaMasivo;
import cl.bcs.socionegocio.per.CrearDireccionElectronicaMasivo.EntradaCrearDireccionElectronicaMasivo;
import cl.bcs.socionegocio.per.CrearDireccionElectronicaMasivo.SalidaCrearDireccionElectronicaMasivo;
import cl.bcs.socionegocio.per.CrearDireccionFisicaMasivo;
import cl.bcs.socionegocio.per.CrearDireccionFisicaMasivo.EntradaCrearDireccionFisicaMasivo;
import cl.bcs.socionegocio.per.CrearDireccionFisicaMasivo.SalidaCrearDireccionFisicaMasivo;
import cl.bcs.socionegocio.per.CrearFatca;
import cl.bcs.socionegocio.per.CrearPersona;
import cl.bcs.socionegocio.per.CrearTelefonoMasivo;
import cl.bcs.socionegocio.per.CrearTelefonoMasivo.EntradaCrearTelefonoMasivo;
import cl.bcs.socionegocio.per.CrearTelefonoMasivo.SalidaCrearTelefonoMasivo;
import cl.bcs.socionegocio.per.EliminarPersonaDireccionElectronicaMasivo;
import cl.bcs.socionegocio.per.EliminarPersonaDireccionFisicaMasivo;
import cl.bcs.socionegocio.per.EliminarPersonaTelefonoMasivo;
import cl.bcs.socionegocio.per.CrearFatca.EntradaCrearFatca;
import cl.bcs.socionegocio.per.CrearPersona.EntradaServicioCrearPersona;
import cl.bcs.socionegocio.per.CrearPersona.SalidaServicioCrearPersona;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.EntidadPersonaEliminar;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.EntradaEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.SalidaEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.pojo.RegistroCambioMasivo;
import cl.bcs.socionegocio.per.ModificarPersonaDatosGeneralesMasivo;
import cl.bcs.socionegocio.pojo.AdvertenciaDetalle;
import cl.bcs.socionegocio.pojo.Enrolamiento;
import cl.bcs.socionegocio.pojo.ErrorDetalle;
import cl.bcs.socionegocio.wbs.CrearClienteExpress.ConsultaClientePortafolio;
import cl.bcs.socionegocio.wbs.CrearClienteExpress.ErroresDetalle;
import cl.bcs.socionegocio.wbs.CrearClienteExpress.Mensaje;
import cl.bcs.socionegocio.wbs.CrearClienteExpress.MensajeAdvertencia;
import cl.bcs.socionegocio.wbs.WebServiceUtils.EntidadManipulada;
import cl.bcs.socionegocio.wbs.WebServiceUtils.SalidaWBS;
import cl.bcs.socionegocio.wbs.pojo.EntradaAdministrarWS;
import cl.bcs.socionegocio.wbs.pojo.SalidaAdministrarWS;
import cl.bcs.socionegocio.wbs.pojo.Indicador;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.utils.Utils.DatosPais;
import cl.bcs.socionegocio.wbs.pojo.Empleador;
import cl.bcs.plataforma.bat.InscribirProcesoBatch;
import cl.bcs.plataforma.bat.InscribirProcesoBatch.InTipoPrograma;
import cl.bcs.plataforma.bat.InscribirProcesoBatch.InscribirProcesoBatchEntrada;
import cl.bcs.plataforma.bat.InscribirProcesoBatch.Parametro;


public class CrearClienteFullWS implements JavaCall {
	static final Logger logger = LogManager.getLogger(CrearClienteFullWS.class);
	
	private final String SQL_SELECT_PAIS_TRANSPARENCIA_FISCAL = loadResourceToString("cl/bcs/socionegocio/wbs/sql/CrearClienteFullWS_SelectPaisTransparenciaFiscal.sql");
	private final String SQL_SELECT_IDPERSONA = loadResourceToString("cl/bcs/socionegocio/wbs/sql/CrearClienteFullWS_SelectPersonaIdPersona.sql");
	private final String SQL_SELECT_INDICADORES = loadResourceToString("cl/bcs/socionegocio/wbs/sql/CrearClienteFullWS_SelectIndicadoresEnrolamiento.sql");
	private final String SQL_SELECT_DOCUMENTO_IDENTIDAD = loadResourceToString("cl/bcs/socionegocio/wbs/sql/CrearClienteFullWS_SelectDocumentoIdentidad.sql"); 
	private final String SQL_SELECT_USA_DIVISION_PAIS = loadResourceToString("cl/bcs/socionegocio/wbs/sql/CrearClienteFullWS_SelectUsaDivisionPais.sql"); 
	private final String SQL_SELECT_COMUNA = loadResourceToString("cl/bcs/socionegocio/wbs/sql/CrearClienteFullWS_SelectComuna.sql"); 
	private final String SQL_SELECT_PERSONA_DATOS_EMPLEADOR = loadResourceToString("cl/bcs/socionegocio/wbs/sql/ModificarDatosGenerales_SelectPersonaDatosEmpleador.sql");	
	private final String SQL_SELECT_DATOS_PERSONA = loadResourceToString("cl/bcs/socionegocio/wbs/sql/CrearClienteFullWS_SelectDatosPersonaPorIdPersona.sql");
	private final String SQL_SELECT_IDPERSONA_DOCUMENTO_IDENTIDAD = loadResourceToString("cl/bcs/socionegocio/wbs/sql/CrearClienteFullWS_SelectIdPersonaPorDocumentoIdentidad.sql");
	private final String SQL_SELECT_PERSONA_DOCUMENTO_IDENTIDAD = loadResourceToString("cl/bcs/socionegocio/wbs/sql/CrearClienteFullWS_SelectDocumentoIdentidadPorIdPersona.sql");
	private final String SQL_SELECT_PERSONA_DIRECCION_FISICA = loadResourceToString("cl/bcs/socionegocio/wbs/sql/CrearClienteFullWS_SelectDireccionFisicaPorIdPersona.sql");
	private final String SQL_SELECT_PERSONA_DIRECCION_ELECTRONICA = loadResourceToString("cl/bcs/socionegocio/wbs/sql/CrearClienteFullWS_SelectDireccionElectronicaPorIdPersona.sql");
	private final String SQL_SELECT_PERSONA_TELEFONOS = loadResourceToString("cl/bcs/socionegocio/wbs/sql/CrearClienteFullWS_SelectTelefonoPorIdPersona.sql");
	private final String SQL_SELECT_PERSONA_RELACION_PERSONA = loadResourceToString("cl/bcs/socionegocio/wbs/sql/CrearClienteFullWS_SelectRelacionPersonaPorIdPersona.sql");
	private final String SQL_SELECT_PERSONA_FACTA = loadResourceToString("cl/bcs/socionegocio/wbs/sql/CrearClienteFullWS_SelectFactaPorIdPersona.sql");
	private final String SQL_SELECT_PERSONA_FACTA_PAIS_RESIDENCIA = loadResourceToString("cl/bcs/socionegocio/wbs/sql/CrearClienteFullWS_SelectFactaPaisResidenciaPorIdPersona.sql");
	private final String SQL_SELECT_PERSONA_CUENTA_BANCARIA = loadResourceToString("cl/bcs/socionegocio/wbs/sql/CrearClienteFullWS_SelectCuentaBancariaPorIdPersona.sql");
	private final String SQL_SELECT_PERSONA_TARJETA_BANCARIA = loadResourceToString("cl/bcs/socionegocio/wbs/sql/CrearClienteFullWS_SelectTarjetaBancariaPorIdPersona.sql");
	private final String SQL_SELECT_PERSONA_INDICADOR = loadResourceToString("cl/bcs/socionegocio/wbs/sql/CrearClienteFullWS_SelectIndicadoresPersonaPorIdPersona.sql");
	private final String SQL_SELECT_CONSULTAR_CLIENTE_PORTAFOLIO = loadResourceToString("cl/bcs/socionegocio/wbs/sql/CrearClienteExpress_SelectConsultaClientePortafolio.sql");
	private final String SQL_SELECT_RELACIONADA_POR_DOCUMENTO_IDENTIDAD = loadResourceToString("cl/bcs/socionegocio/wbs/sql/CrearClienteFull_SelectConsultaPersonaRelacionadaPorDocumentoIdentidad.sql");
	
	private final String SQL_SELECT_TELEFONOS_ID_PERSONA = loadResourceToString("cl/bcs/socionegocio/per/sql/EliminarPersonaTelefonoMasivo_SelectTelefonosPorIdPersona.sql");
	private final String SQL_SELECT_DIRECCIONES_ELECTRONICAS_ID_PERSONA = loadResourceToString("cl/bcs/socionegocio/per/sql/EliminarPersonaDireccionElectronicaMasivo_SelectDireccionElectronicaPorIdPersona.sql");
	private final String SQL_SELECT_DIRECCIONES_FISICAS_ID_PERSONA = loadResourceToString("cl/bcs/socionegocio/per/sql/EliminarPersonaDireccionFisicaMasivo_SelectDireccionFisicaPorIdPersona.sql");
	private final String SQL_PREDETERMINAR_PERSONA_DIRECCION_ELECTRONICA = loadResourceToString("cl/bcs/socionegocio/wbs/sql/CrearClienteExpress_Call_PredeterminarDireccionElectronica.sql");
	private final String SQL_PREDETERMINAR_PERSONA_DIRECCION_FISICA = loadResourceToString("cl/bcs/socionegocio/wbs/sql/CrearClienteExpress_Call_PredeterminarDireccionFisica.sql");
	private final String SQL_PREDETERMINAR_PERSONA_TELEFONO = loadResourceToString("cl/bcs/socionegocio/wbs/sql/CrearClienteExpress_Call_PredeterminarTelefono.sql");
	private final String SQL_SELECT_IDPERSONA_CLIENTE_CREADO = loadResourceToString("cl/bcs/socionegocio/wbs/sql/CrearClienteFullWS_SelectPersonaIdPersonaNuevoClienteCreado.sql");
	
	private static final String ID_TIPO_CANAL = "WBS";
	private static final String USA_REGLAS_NEGOCIO = "S";
	private static final String ID_MODULO = "SOC";
	private static final String ID_TIPO_ENTIDAD = "CLIENTE";
	private static final String TIPO_ENROLAMIENTO = "FULL";
	private static final Pattern PATTERN = Pattern.compile("^[0-9]+-[0-9kK]{1}$");

	public static class EntradaCrearClienteFullWS extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_ClaveUsuario;
		public List<SocioNegocio> p_SocioNegocio;
	}

	public static class SocioNegocio extends BcsPojo {
		public Long IdInterno;
		public Long IdPersona;
		public String IdTipoPersona;
		public String Nombres;
		public String ApellidoPaterno;
		public String ApellidoMaterno;
		@ValidarContenidoCampos(idCampo = "IdTipoGenero", tipoDato = String.class, idCampoMensaje = "IdGenero", prefijo = "")
		public String IdGenero;
		public String RazonSocial;
		public Long IdPaisNacimientoConstitucion;
		public String FechaNacimientoConstitucion;
		public String IdEstadoCivil;
		public String IdRegimenConyugal;
		public transient Long IdPersonaConyuge; //para pasarlo al enrolamiento
		public Long IdPaisResidenciaCasaMatriz;
		public String ResidenciaTriburariaFueraChile;
		public Long IdPaisNacionalidadPrincipal;
		public Long IdGiro;
		public Long IdProfesionActividad;
		public String Cargo;
		public String DetalleCargo;			
		public Long RangoPatrimonio;
		public String InicioActividad;
		public String GlobalLegal;
		public List<DireccionFisica> DireccionesFisicas; 
		public List<Telefono> Telefonos;
		public List<DireccionElectronica> DireccionesElectronicas;
		public List<TipoDocumento> TiposDocumento;
		public List<NombreFantasia> NombreFantasia;
		public List<RelacionPersona> RelacionesPersona;
		public List<CuentaBancaria> CuentasBancarias;
		public List<TarjetasBancaria> TarjetasBancarias;
		public List<Indicador> IndicadoresPersona;
		public FatcaCrs FatcaCrs;
		public DatosCliente DatosCliente;
		public Empleador Empleador;
		public Long IdPersonaEmpleador;
		public String IdSituacionLaboral;	
		public String IdTipoCanalIngresoCorredor;	
		@ValidarContenidoCampos(idCampo = "CodigoRangoSeleccion", tipoDato = String.class, idCampoMensaje = "CodigoRangoRenta", prefijo = "RANGO_RENTA")
		public String CodigoRangoRenta;
		@ValidarContenidoCampos(idCampo = "CodigoRangoSeleccion", tipoDato = String.class, idCampoMensaje = "CodigoRangoNivelVenta", prefijo = "RANGO_VENTA")
		public String CodigoRangoNivelVenta;
		public String CodigoPropositoTransaccion;
		
		public SocioNegocio() {
			super();
			this.IdInterno = 0L;
			this.IdPersona = 0L;
			this.IdTipoPersona = "";
			this.Nombres = "";
			this.ApellidoPaterno = "";
			this.ApellidoMaterno = "";
			this.IdGenero = "";
			this.RazonSocial = "";
			this.IdPaisNacimientoConstitucion = 0L;
			this.FechaNacimientoConstitucion = "0001-01-01";
			this.IdEstadoCivil = "";
			this.IdRegimenConyugal = "";
			this.IdPaisResidenciaCasaMatriz = 0L;
			this.ResidenciaTriburariaFueraChile = "";
			this.IdPaisNacionalidadPrincipal = 0L;
			this.IdGiro = 0L;
			this.IdProfesionActividad = 0L;
			this.Cargo = "";
			this.DetalleCargo = "";			
			this.RangoPatrimonio = 0L;
			this.InicioActividad = "0001-01-01";
			this.GlobalLegal = "";
			this.DireccionesFisicas = new ArrayList<>();
			this.TiposDocumento = new ArrayList<>();
			this.RelacionesPersona = new ArrayList<>();
			this.CuentasBancarias = new ArrayList<>();
			this.TarjetasBancarias = new ArrayList<>();
			this.IndicadoresPersona = new ArrayList<>();
			this.FatcaCrs = new FatcaCrs();
			this.DatosCliente = new DatosCliente();
			this.Telefonos = new ArrayList<>();
			this.DireccionesElectronicas = new ArrayList<>();
			this.Empleador = new Empleador();
			this.IdPersonaEmpleador = 0L;
			this.IdSituacionLaboral = "";
			this.IdTipoCanalIngresoCorredor = "";
			this.NombreFantasia = new ArrayList<>();
			this.CodigoRangoRenta = "";
			this.CodigoRangoNivelVenta = "";
			this.CodigoPropositoTransaccion = "";
		}
	}
	
	public static class FatcaCrs extends BcsPojo {
		public String IdClasificacionFatca;
		public String IdClasificacionEmpresaFatca;
		public String NumeroGIIN;
		public String FechaFirmaFormulario;
		public String AutorizacionInformarOrganismo;
		public String NACEEUU;
		public String NACEEUUExplicacion;
		public String ObligacionesFiscales;
		public String ObligacionesFiscalesExplicacion;
		public String ExcepcionResidente;
		public String ExcepcionNacimiento;
		public String ExcepcionDomicilio;
		public String ExcepcionTelefono;
		public String ExcepcionOrdenPermanente;
		public String ExcepcionFirma;
		public String ParticipacionSociedadesUSA;
		public String OrdenPermanente;
		public List<PaisResidencia> PaisesResidencia;
		
		public FatcaCrs() {
			super();
			this.IdClasificacionFatca = "PENDIENTE";
			this.NumeroGIIN = "";
			this.AutorizacionInformarOrganismo = "N";
			this.ExcepcionDomicilio = "N";
			this.ExcepcionTelefono = "N";
			this.ExcepcionOrdenPermanente = "N";
			this.ExcepcionFirma = "N";
			this.ParticipacionSociedadesUSA = "N";
			this.OrdenPermanente = "N";
			this.NACEEUU = "";
			this.NACEEUUExplicacion = "";
			this.ObligacionesFiscales = "";
			this.ObligacionesFiscalesExplicacion = "";
			this.FechaFirmaFormulario = "0001-01-01 00:00:00";
			this.IdClasificacionEmpresaFatca = "";
			this.ExcepcionResidente = "N";
			this.ExcepcionNacimiento = "N";
			PaisesResidencia = new ArrayList<>();
		}
	}
	
	public static class PaisResidencia extends BcsPojo {
		public Long IdPais ;
		public String Tin;
		public String NoTin;
		public String OtraCausa;
		
		public PaisResidencia() {
			this.IdPais = 0L;
			this.Tin = "";
			this.NoTin = "";
			this.OtraCausa = "";
		}
	}
	
	public static class RelacionPersona extends BcsPojo {
		public Long IdPersonaRelacionada;
		public String IdTipoRelacion;
		public String TipoDocumentoIdentidad;
		public String DocumentoIdentidad; 
		public String Nombres;
		public String ApellidoPaterno;
		public String ApellidoMaterno;
		public transient String EstaInsertada;
		
		public RelacionPersona() {
			this.IdPersonaRelacionada = 0L;
			this.IdTipoRelacion = "";
			this.TipoDocumentoIdentidad = "";
			this.DocumentoIdentidad = "";
			this.Nombres = "";
			this.ApellidoPaterno = "";
			this.ApellidoMaterno = "";
			this.EstaInsertada = "N";
		}
	}
	
	public static class DatosCliente extends BcsPojo {
		public String IdCategoriaInversionista;
		public String IdPerfilComercial;
		public String IdTipoCliente;
		public Long IdSegmentoCliente;
		public Long CodigoEjecutivo;
		public String CodigoEjecutivoExterno;
		public Long TipoClienteCMF;
		public String AreaCliente;
		public Long Sucursal;
		public String ClienteFinal;
		public List<Indicador> IndicadoresCliente;
		public DatosPortafolio DatosPortafolio;
		public List<DeclaracionOrigenFondo>	DeclaracionOrigenFondo;
		public String FirmaAutomaticaDocumento;

		public DatosCliente() {
			super();
			IdCategoriaInversionista = "";
			IdPerfilComercial = "";
			IdTipoCliente = "";
			IdSegmentoCliente = 0L;
			CodigoEjecutivo = 0L;
			CodigoEjecutivoExterno = "";
			TipoClienteCMF = 0L;
			AreaCliente = "";
			Sucursal = 0L;
			ClienteFinal = "";
			IndicadoresCliente = new ArrayList<>();
			DatosPortafolio = new DatosPortafolio();
			DeclaracionOrigenFondo = new ArrayList<>();
			FirmaAutomaticaDocumento = "N";
		}
	}
	
	public static class DeclaracionOrigenFondo extends BcsPojo{
	    public String IdTipoOperacionOrigenFondos;
	    public String FechaDeclaracion;
	    public String Comentario;
	    public String OperacionInscripcion;
	    public List<DetalleOrigenFondo>	DetalleOrigenFondo;
	    
		public DeclaracionOrigenFondo(){
	        IdTipoOperacionOrigenFondos	= "";
	        FechaDeclaracion			= "";
	        Comentario					= "";
	        OperacionInscripcion		= "";
			DetalleOrigenFondo = new ArrayList<DetalleOrigenFondo>();
		}
	}

	public static class DetalleOrigenFondo extends BcsPojo{
	    public String	IdTipoOrigenFondo;
	    public Long		Valor;
	    public String	Otros;

	    public DetalleOrigenFondo(){
	        IdTipoOrigenFondo= "";
	        Valor = 0L;
	        Otros = "";
	    }
	}
	
	public static class DatosPortafolio extends BcsPojo{
		public Long IdEjecutivoComercial;
		public List<CuentasCustodio> CuentasCustodio;
		public List<Indicador> PreferenciasNegocio;

		public DatosPortafolio() {
			IdEjecutivoComercial= 0L;
			CuentasCustodio = new ArrayList<>();
			PreferenciasNegocio = new ArrayList<>();
		}
	}
	
	public static class CuentasCustodio extends BcsPojo{
		public String IdBoveda;
		public String Codigo;
		public String LiquidaDiv;
		public String LiquidaVen;
		public String LiquidaCup;
		public String LiquidaPre;
		public String IdTipoCuentaCustodio;
		public CuentasCustodio() {
			super();
			IdBoveda = "";
			Codigo = "";
			IdTipoCuentaCustodio = "";
			LiquidaDiv = "N";
			LiquidaVen = "N";
			LiquidaCup = "N";
			LiquidaPre = "N";
		}		
	}

	public static class TipoDocumento extends BcsPojo {
		public String TipoDocumento;
		public String NumeroDocumento;
		public String NumeroSerie;
		public String FechaEmision;
		public String FechaVencimiento;
		
		public TipoDocumento() {
			this.TipoDocumento = "";
			this.NumeroDocumento = "";
			this.NumeroSerie = "";
			this.FechaEmision = "0001-01-01";
			this.FechaVencimiento = "0001-01-01";
		}
	}
	
	public static class DireccionFisica extends BcsPojo{
		public String IdProposito;
		public String Sector;
		public String CodigoPostal;
		public Long IdComuna;
		public String Calle;
		public String Numero;
		public String Departamento;
		public String Comentario;
		public Long IdCiudad;
		public String CodigoUnicoTerritorial;
		public String Predeterminado;
		public Long IdEnrolamiento;
		public Long IdDireccionFisica;
		public Long IdPersona;
		
		public DireccionFisica() {
			super();
			this.IdProposito = "";
			this.Sector = "";
			this.CodigoPostal = "";
			this.IdComuna = 0L;
			this.Calle = "";
			this.Numero = "";
			this.Departamento = "";
			this.Comentario = "";
			this.IdCiudad = 0L;
			this.CodigoUnicoTerritorial = "";
			this.Predeterminado = "N";
			this.IdEnrolamiento=0L;
			this.IdDireccionFisica = 0L;
			this.IdPersona = 0L;
		}
	}
	
	public static class DivisionPais extends BcsPojo{
	    public Long IdCiudad;
	    public String UsaDivisionPais;
	}
	
	public static class Comuna extends BcsPojo{
	    public Long IdComuna;
	    public Long IdCiudad;
	}
	
	public static class Telefono extends BcsPojo {
		public String IdProposito;
		public String Numero;
		public Long IdPais;
		public String Tipo;
		public String Anexo;
		public transient String Comentario; //para que el enr no se caiga
		public String Predeterminado;//para que el enr no se caiga
		public Long IdEnrolamiento;
		public Long IdTelefono;
		public Long IdPersona;
	
		public Telefono() {
			this.IdProposito = "";
			this.Numero = "";
			this.IdPais = 0L;
			this.Tipo = "";
			this.Anexo = "";
			this.Comentario = "";
			this.Predeterminado = "N";
			this.IdEnrolamiento=0L;
			this.IdTelefono=0L;
			this.IdPersona = 0L;
		}
	}
	
	public static class DireccionElectronica extends BcsPojo {
		public String Comentario;
		public String IdProposito;
		public String Direccion;
		public String Predeterminado;//para que el enr no se caiga
		public Long IdEnrolamiento;
		public Long IdDireccionElectronica;
		public Long IdPersona;
		
		public DireccionElectronica() {
			this.Comentario = "";
			this.IdProposito = "";
			this.Direccion = "";
			this.Predeterminado = "N";
			this.IdEnrolamiento=0L;
			this.IdDireccionElectronica=0L;
			this.IdPersona = 0L;
		}
	}

	public static class CuentaBancaria extends BcsPojo {
		public Long IdCuentaBancaria;
		public Long IdBanco;
		public String CodigoTipoCuentaBancaria;
		public String NumeroCuentaBancaria;
		public String IdMoneda;
		public Long CodigoPlaza;
		public String PAC;
		public String Predeterminado;
		public String ActualizaMedioPago;
		
		public CuentaBancaria() {
			this.IdCuentaBancaria = 0L;
			this.IdBanco = 0L;
			this.CodigoTipoCuentaBancaria = "";
			this.NumeroCuentaBancaria = "";
			this.IdMoneda = "";
			this.CodigoPlaza = 0L;
			this.PAC = "";
			this.Predeterminado= "";
			this.ActualizaMedioPago = "";
		}
	}
	
	public static class TarjetasBancaria {
		public Long IdBanco;
		public String IdTipo;
		public String NumeroTarjeta;
		public Long CodigoVerificacion;
		public String FechaVencimiento;
		
		public TarjetasBancaria() {
			this.IdBanco = 0L;
			this.IdTipo = "";
			this.NumeroTarjeta = "";
			this.CodigoVerificacion = 0L;
			this.FechaVencimiento = "";
		}
	}

	public static class IdPersonaConsulta {
		public Long IdPersona;
		
		public IdPersonaConsulta() {
			this.IdPersona = 0L;
		}
	}
	
	public static class SalidaCrearClienteFullWS extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<EntidadCreada> P_OUT_ENTIDADCREADA;

		public SalidaCrearClienteFullWS() {
			super();
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_ENTIDADCREADA = new ArrayList<>();
		}
	}
	
	public static class EntidadCreada extends BcsPojo {
		public String TipoEntidad;
		public Long IdEntidad;
		public Long IdInterno;
		public List<Mensaje> Mensajes = new ArrayList<>();
	}
	
	public static class Pais extends BcsPojo {
		public Long IdPais;
		public String NombrePais;
		public String CodigoPais;
		public String UsaDivisionPais;
		public String NombreDivisionPais;
		public String EsOcde;
		public String EsPaisUs;
	}
	
	public static class Id extends BcsPojo {
	    public Long Id;
	    public Long IdInterno;
	    public String CodigoTipoIndicador;
	}

	public static class Persona extends BcsPojo {
		public String IdTipoPersona;
		public String Nombres;
		public String ApellidoPaterno;
		public String ApellidoMaterno;
		public String IdGenero;
		public String RazonSocial;
		public Long IdPaisNacimientoConstitucion;
		public String FechaNacimientoConstitucion;
		public String IdEstadoCivil;
		public String IdRegimenConyugal;
		public transient Long IdPersonaConyuge; //para pasarlo al enrolamiento
		public transient Long IdPersonaEmpleador;
		public Long IdPaisResidenciaCasaMatriz;
		public String ResidenciaTriburariaFueraChile;
		public Long IdPaisNacionalidadPrincipal;
		public Long IdGiro;
		public Long IdProfesionActividad;
		public String Cargo;
		public String DetalleCargo;		
		public Long RangoPatrimonio;
		public String InicioActividad;
		public String GlobalLegal;
		public String IdSituacionLaboral;
		public String IdTipoCanalIngresoCorredor;
		
		public Persona() {			
		}
	}
	
	public static class NombreFantasia extends BcsPojo{
		public String NombreFantasia;
		
		public NombreFantasia() {
			this.NombreFantasia = "";
		}
	}
	
	public static class EntidadesCreadasEnrolamiento extends BcsPojo {
		public Long IdDireccionFisicaPrincipal;
		public Long IdDireccionElectronicaPrincipal;
		public Long IdTelefonoPrincipal;
		
		public EntidadesCreadasEnrolamiento () {
			this.IdDireccionFisicaPrincipal = 0L;
			this.IdDireccionElectronicaPrincipal = 0L;
			this.IdTelefonoPrincipal = 0L;
		}
	}
	
	private SalidaCrearClienteFullWS logica(Connection conn, EntradaCrearClienteFullWS entrada) throws Exception {
		logger.info("INICIO::CrearClienteFullWS");
		
		String localizacionSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.LocalizacionSistema");
		SalidaCrearClienteFullWS salidaWS = new SalidaCrearClienteFullWS();
		String agregaAdvertencia = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "SOCIODENEGOCIO", "SOC.MensajeAdvertenciaRelacionadoExiste");
		List<Mensaje> mensajesAdvertencia = new ArrayList<>();
		 
		SalidaValidarCamposObligatorios validacion = validarEntrada(conn, entrada);
		
		if(validacion.P_OUT_ERRORESCONTADOR > 0) {
			salidaWS.P_OUT_ERRORESCONTADOR = validacion.P_OUT_ERRORESCONTADOR;
			EntidadCreada entidadCreada = new EntidadCreada();
			entidadCreada.IdEntidad = 0L;
			entidadCreada.IdInterno = 0L;
			entidadCreada.TipoEntidad = ID_TIPO_ENTIDAD;
			validacion.P_OUT_MENSAJES.forEach(mensaje -> {
				Mensaje msg = new Mensaje();
				msg.IdInterno = 0L;
				msg.TipoMensaje = PlataformaUtils.MensajeSalida.MENSAJE_ERROR;
				msg.Codigo = mensaje.codigo;
				msg.Mensaje = mensaje.descripcion;
				entidadCreada.Mensajes.add(msg);
			});
			salidaWS.P_OUT_ENTIDADCREADA.add(entidadCreada);
			return salidaWS;
		}

		String EditarTelefonoYDirecciones = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "SOCIODENEGOCIO", "PER.EditarTelefonoYDirecciones");
		
		for(SocioNegocio socioNegocio : entrada.p_SocioNegocio) {
			EntidadCreada entidadCreada = new EntidadCreada();
			entidadCreada.IdEntidad = 0L;
			entidadCreada.IdInterno = socioNegocio.IdInterno;
			entidadCreada.TipoEntidad = ID_TIPO_ENTIDAD;
			
			if("CL".equalsIgnoreCase(localizacionSistema)) {
				if(!this.ValidarDocumentoIdentidad(socioNegocio.Empleador.TipoDocumentoIdentidad, socioNegocio.Empleador.DocumentoIdentidad)) {
					entidadCreada = agregarError("DOCUMENTO_IDENTIDAD_EMPLEADOR_INVALIDO",String.format("DOCUMENTO IDENTIDAD %s NO ES VALIDO", socioNegocio.Empleador.DocumentoIdentidad),entidadCreada);
					salidaWS.P_OUT_ENTIDADCREADA.add(entidadCreada);
					continue;
				}
				
				for(TipoDocumento tipoDocumento : socioNegocio.TiposDocumento) {
					if(!this.ValidarDocumentoIdentidad(tipoDocumento.TipoDocumento, tipoDocumento.NumeroDocumento)) {
						entidadCreada = agregarError("DOCUMENTO_IDENTIDAD_INVALIDO",String.format("DOCUMENTO IDENTIDAD %s NO ES VALIDO", tipoDocumento.NumeroDocumento),entidadCreada);
						continue;
					}
				}
				for(RelacionPersona relacionPersona : socioNegocio.RelacionesPersona) {
					if(!this.ValidarDocumentoIdentidad(relacionPersona.TipoDocumentoIdentidad, relacionPersona.DocumentoIdentidad)) {
						entidadCreada = agregarError("DOCUMENTO_IDENTIDAD_RELACION_INVALIDO",String.format("DOCUMENTO IDENTIDAD %s NO ES VALIDO", relacionPersona.DocumentoIdentidad),entidadCreada);
						continue;
					}
				}
			}
			
			if (socioNegocio.TiposDocumento.isEmpty()){
			    entidadCreada = agregarError("DOCUMENTOS_VACIO","DOCUMENTO NO PUEDE SER VACIO",entidadCreada);
			    salidaWS.P_OUT_ENTIDADCREADA.add(entidadCreada);
				continue;
			}
			
			List<String> documentosIdentidad = socioNegocio.TiposDocumento.parallelStream().map(documento -> {
					String documentoIdentidad = documento.NumeroDocumento;
					return documentoIdentidad;
					}).collect(Collectors.toList());
			
			List<Long> idPersona = obtenerIdPersonaSocioNegocio(conn, documentosIdentidad);
			socioNegocio.IdPersona = idPersona.parallelStream().findFirst().orElse(0L);
			
			if(idPersona.size() == 1 && socioNegocio.IdPersona > 0){
				cargarDatosSocioNegocioExistente(conn, socioNegocio, entrada);
			}else if(idPersona.size() > 1){
				entidadCreada = agregarError("DOCUMENTOS_INVALIDOS","MAS DE UN DOCUMENTO IDENTIDAD YA ESTA INGRESADO EN EL SISTEMA",entidadCreada);
			    salidaWS.P_OUT_ENTIDADCREADA.add(entidadCreada);
				continue;
			}
			
			SalidaConsultarEntidadEnrolamiento salidaConsultarEntidadEnrolamiento = consultarEntidadEnrolamiento(conn,socioNegocio,entrada);
			if(existeCliente(salidaConsultarEntidadEnrolamiento)) {
				entidadCreada = crearSalidaClienteExiste(socioNegocio,entidadCreada);
				salidaWS.P_OUT_ENTIDADCREADA.add(entidadCreada);
				continue;
			}
			
			socioNegocio = calcularCiudadDivisionPais(conn, socioNegocio);
			
			SalidaCrearBorradorEnrolamiento salidaCrearBorradorEnrolamiento = crearBorradorEnrolamiento(conn, socioNegocio, entrada);
			if(salidaCrearBorradorEnrolamiento.P_OUT_ERRORESCONTADOR > 0) {
				entidadCreada = crearSalidaMensajesErrores(socioNegocio,salidaCrearBorradorEnrolamiento,entidadCreada);
				salidaWS.P_OUT_ENTIDADCREADA.add(entidadCreada);
				salidaWS.P_OUT_ERRORESCONTADOR = contarErrores(salidaWS);
				continue;
			}
			
			SalidaAdministrarBorrador salidaAdministrarBorrador = new SalidaAdministrarBorrador();
		
			List<DireccionFisica> direccionesFisicasBorrador = new ArrayList<DireccionFisica>();
			if(socioNegocio.IdPersona > 0) {
				direccionesFisicasBorrador = socioNegocio.DireccionesFisicas.stream().filter(dirF -> dirF.IdDireccionFisica.equals(0L) ).collect(Collectors.toList());
			}else {
				direccionesFisicasBorrador = socioNegocio.DireccionesFisicas.parallelStream().filter(dirF -> ("N").equals(dirF.Predeterminado)).collect(Collectors.toList());
			}

			salidaAdministrarBorrador = crearBorradorDireccionFisica(conn, socioNegocio, direccionesFisicasBorrador, entrada, salidaCrearBorradorEnrolamiento);
			if(salidaAdministrarBorrador.P_OUT_ERRORESCONTADOR > 0) {
				entidadCreada = crearSalidaMensajeAdministrarBorrador(socioNegocio, salidaAdministrarBorrador, entidadCreada);
			}	
			
			List<Telefono> telefonosBorrador = new ArrayList<Telefono>();
			if(socioNegocio.IdPersona > 0) {
				telefonosBorrador = socioNegocio.Telefonos.stream().filter(tel -> tel.IdTelefono.equals(0L)).collect(Collectors.toList());
			}else {
				telefonosBorrador = socioNegocio.Telefonos.parallelStream().filter(tel -> ("N").equals(tel.Predeterminado)).collect(Collectors.toList());
			}
			
			salidaAdministrarBorrador = crearBorradorTelefono(conn, socioNegocio, telefonosBorrador, entrada, salidaCrearBorradorEnrolamiento);
			if(salidaAdministrarBorrador.P_OUT_ERRORESCONTADOR > 0) {
				entidadCreada = crearSalidaMensajeAdministrarBorrador(socioNegocio, salidaAdministrarBorrador, entidadCreada);
			}
			
			
			if (socioNegocio.DireccionesElectronicas.size() > 0) {
				for (DireccionElectronica direccionElectronica : socioNegocio.DireccionesElectronicas) {
					if (direccionElectronica.IdProposito.isEmpty() || "".equals(direccionElectronica.IdProposito)) {
						entidadCreada = agregarError("PROPOSITO_VACIO","EL PROPOSITO NO PUEDE ESTAR VACIO",entidadCreada);
						continue;
					}
					
					if (direccionElectronica.Direccion.isEmpty() || "".equals(direccionElectronica.Direccion)) {
						entidadCreada = agregarError("DIRECCION_VACIO","EL DIRECCION NO PUEDE ESTAR VACIO",entidadCreada);
						continue;
					}
					
				}
			}
			
			
			List<DireccionElectronica> direccionesElectronicasBorrador = new ArrayList<DireccionElectronica>();
			if(socioNegocio.IdPersona > 0) {
				direccionesElectronicasBorrador = socioNegocio.DireccionesElectronicas.stream().filter(dirE -> dirE.IdDireccionElectronica.equals(0L) ).collect(Collectors.toList());;
			}else {
				direccionesElectronicasBorrador = socioNegocio.DireccionesElectronicas.parallelStream().filter(dirE -> ("N").equals(dirE.Predeterminado)).collect(Collectors.toList());
			}
			
			salidaAdministrarBorrador = crearBorradorDireccionElectronica(conn, socioNegocio, direccionesElectronicasBorrador, entrada, salidaCrearBorradorEnrolamiento);
			if(salidaAdministrarBorrador.P_OUT_ERRORESCONTADOR > 0) {
				entidadCreada = crearSalidaMensajeAdministrarBorrador(socioNegocio, salidaAdministrarBorrador, entidadCreada);
			}

			salidaAdministrarBorrador = crearBorradorCuentaBancaria(conn, socioNegocio, entrada, salidaCrearBorradorEnrolamiento);
			if(salidaAdministrarBorrador.P_OUT_ERRORESCONTADOR > 0) {
				entidadCreada = crearSalidaMensajeAdministrarBorrador(socioNegocio, salidaAdministrarBorrador, entidadCreada);
			}
				
			salidaAdministrarBorrador = crearBorradorTarjetaBancaria(conn, socioNegocio, entrada, salidaCrearBorradorEnrolamiento);
			if(salidaAdministrarBorrador.P_OUT_ERRORESCONTADOR > 0) {
				entidadCreada = crearSalidaMensajeAdministrarBorrador(socioNegocio, salidaAdministrarBorrador, entidadCreada);
			}
				
			TipoDocumento primerDocumento = socioNegocio.TiposDocumento.remove(0);
			salidaAdministrarBorrador = modificarBorradorTipoDocumento(conn, socioNegocio,primerDocumento, entrada, salidaCrearBorradorEnrolamiento);
			if(salidaAdministrarBorrador.P_OUT_ERRORESCONTADOR > 0) {
				entidadCreada = crearSalidaMensajeAdministrarBorrador(socioNegocio, salidaAdministrarBorrador, entidadCreada);
			}
				
			salidaAdministrarBorrador = crearBorradorTipoDocumento(conn, socioNegocio, entrada, salidaCrearBorradorEnrolamiento);
			if(salidaAdministrarBorrador.P_OUT_ERRORESCONTADOR > 0) {
				entidadCreada = crearSalidaMensajeAdministrarBorrador(socioNegocio, salidaAdministrarBorrador, entidadCreada);
			}

		    salidaAdministrarBorrador = crearBorradorRelacionPersona(conn, socioNegocio, entrada, salidaCrearBorradorEnrolamiento, agregaAdvertencia);
		    if (salidaAdministrarBorrador.P_OUT_ERRORESCONTADOR > 0) {
		    	entidadCreada = crearSalidaMensajeAdministrarBorrador(socioNegocio, salidaAdministrarBorrador, entidadCreada);
		    }
		    
			if( salidaAdministrarBorrador.P_OUT_MENSAJES.stream().filter(msj -> PlataformaUtils.MensajeSalida.MENSAJE_ADVERTENCIA.equals(msj.tipo) ).count() > 0 ) {
				mensajesAdvertencia.addAll( agregarMensajesAdvertencias(conn, salidaAdministrarBorrador, entrada));
				socioNegocio.IdPersonaConyuge = salidaAdministrarBorrador.P_OUT_MENSAJES.stream().findFirst().orElse(new MensajeSalida()).id;
			}
			
			salidaAdministrarBorrador = modificarBorradorClienteDatosGenerales(conn, socioNegocio, entrada, salidaCrearBorradorEnrolamiento);
			if(salidaAdministrarBorrador.P_OUT_ERRORESCONTADOR > 0) {
				entidadCreada = crearSalidaMensajeAdministrarBorrador(socioNegocio, salidaAdministrarBorrador, entidadCreada);
			}
			
			salidaAdministrarBorrador = crearBorradorCuentaCustodioPortafolio(conn, socioNegocio, entrada, salidaCrearBorradorEnrolamiento);
			if(salidaAdministrarBorrador.P_OUT_ERRORESCONTADOR > 0) {
				entidadCreada = crearSalidaMensajeAdministrarBorrador(socioNegocio, salidaAdministrarBorrador, entidadCreada);
			}
			
			salidaAdministrarBorrador = modificarBorradorIndicador(conn, socioNegocio, entrada, salidaCrearBorradorEnrolamiento,socioNegocio.IndicadoresPersona, "INDICADORES_PERSONA", "PERSONA");
			if(salidaAdministrarBorrador.P_OUT_ERRORESCONTADOR > 0) {
				entidadCreada = crearSalidaMensajeAdministrarBorrador(socioNegocio, salidaAdministrarBorrador, entidadCreada);
			}
			
			salidaAdministrarBorrador = modificarBorradorIndicador(conn, socioNegocio, entrada, salidaCrearBorradorEnrolamiento, socioNegocio.DatosCliente.IndicadoresCliente,"INDICADORES_CLIENTE", "CLIENTE");
			if(salidaAdministrarBorrador.P_OUT_ERRORESCONTADOR > 0) {
				entidadCreada = crearSalidaMensajeAdministrarBorrador(socioNegocio, salidaAdministrarBorrador, entidadCreada);
			}
			
			List<Indicador> preferenciasNegocioSinCobroCustodia = socioNegocio.DatosCliente.DatosPortafolio.PreferenciasNegocio.stream().filter(ind-> !ind.CodigoTipoIndicador.equals("COBROCUST")).collect(Collectors.toList());
			salidaAdministrarBorrador = modificarBorradorIndicador(conn, socioNegocio, entrada, salidaCrearBorradorEnrolamiento,preferenciasNegocioSinCobroCustodia,"PORTAFOLIO_CLIENTE_INDICADORES", "PORTAFOLIO");
			if(salidaAdministrarBorrador.P_OUT_ERRORESCONTADOR > 0) {
				entidadCreada = crearSalidaMensajeAdministrarBorrador(socioNegocio, salidaAdministrarBorrador, entidadCreada);
			}
			
			salidaAdministrarBorrador = modificarBorradorFatca(conn, socioNegocio, entrada, salidaCrearBorradorEnrolamiento);
			if(salidaAdministrarBorrador.P_OUT_ERRORESCONTADOR > 0) {
				entidadCreada = crearSalidaMensajeAdministrarBorrador(socioNegocio, salidaAdministrarBorrador, entidadCreada);
			}

			salidaAdministrarBorrador = crearBorradorFatcaPaisResidencia(conn, socioNegocio, entrada, salidaCrearBorradorEnrolamiento);
			if(salidaAdministrarBorrador.P_OUT_ERRORESCONTADOR > 0) {
				entidadCreada = crearSalidaMensajeAdministrarBorrador(socioNegocio, salidaAdministrarBorrador, entidadCreada);
			}
			
		    if(debeCrearEmpleador(socioNegocio.Empleador) ){
			    salidaAdministrarBorrador = crearEmpleador(conn, socioNegocio, entrada, salidaCrearBorradorEnrolamiento, agregaAdvertencia);
			   	if(salidaAdministrarBorrador.P_OUT_ERRORESCONTADOR > 0 ) {
			   		entidadCreada = crearSalidaMensajeAdministrarBorrador(socioNegocio, salidaAdministrarBorrador, entidadCreada);
			    }
			   	else {
			   		if(agregaAdvertencia.equalsIgnoreCase("S") && salidaAdministrarBorrador.P_OUT_MENSAJES.stream().filter(msj -> PlataformaUtils.MensajeSalida.MENSAJE_ADVERTENCIA.equals(msj.tipo) ).count() > 0) {
			   			mensajesAdvertencia.addAll( agregarMensajesAdvertencias(conn, salidaAdministrarBorrador, entrada));
			   		}
			   		socioNegocio.Empleador.IdEmpleador = salidaAdministrarBorrador.P_OUT_REFID.stream().findFirst().orElse(new ResultadoAdministrarBorradorEntidad()).IdEntidad;
			   		socioNegocio.IdPersonaEmpleador = socioNegocio.Empleador.IdEmpleador;
			   	}
		    	
		    }		  
		    
		    salidaAdministrarBorrador = modificarBorradorPersonaDatosGenerales(conn, socioNegocio, entrada, salidaCrearBorradorEnrolamiento);
			if(salidaAdministrarBorrador.P_OUT_ERRORESCONTADOR > 0) {
				entidadCreada = crearSalidaMensajeAdministrarBorrador(socioNegocio, salidaAdministrarBorrador, entidadCreada);
			}
			
			salidaAdministrarBorrador = crearBorradorDeclaracionOrigenFondos(conn, socioNegocio, entrada, salidaCrearBorradorEnrolamiento);
			if(salidaAdministrarBorrador.P_OUT_ERRORESCONTADOR > 0) {
				entidadCreada = crearSalidaMensajeAdministrarBorrador(socioNegocio, salidaAdministrarBorrador, entidadCreada);
			}
			
			if (esJuridico(socioNegocio)) {
				salidaAdministrarBorrador = crearBorradorNombreFantasia(conn, socioNegocio, entrada, salidaCrearBorradorEnrolamiento);
				if (salidaAdministrarBorrador.P_OUT_ERRORESCONTADOR > 0) {
					entidadCreada = crearSalidaMensajeAdministrarBorrador(socioNegocio, salidaAdministrarBorrador,
							entidadCreada);
				}
			}
			
			if (entidadCreada.Mensajes.isEmpty()) {
			    
				SalidaCrearClienteFull salidaCrearClienteFull = crearClienteFull(conn, socioNegocio, entrada, salidaCrearBorradorEnrolamiento);
				if (salidaCrearClienteFull.P_OUT_ERRORESCONTADOR > 0) {
					entidadCreada = crearSalidaMensajesErroresCrearClienteFull(socioNegocio, salidaCrearClienteFull,entidadCreada);
					salidaWS.P_OUT_ERRORESCONTADOR = salidaCrearClienteFull.P_OUT_ERRORESCONTADOR + salidaWS.P_OUT_ERRORESCONTADOR;
				} else {

					entidadCreada = crearSalidaMensajesCrearClienteFull(socioNegocio, salidaCrearClienteFull,entidadCreada);
					if(entidadCreada.Mensajes.isEmpty() && salidaCrearClienteFull.P_OUT_ERRORESCONTADOR == 0) {
						Mensaje mensaje = new Mensaje();
						mensaje.IdInterno = 0L;
						mensaje.TipoMensaje = PlataformaUtils.MensajeSalida.MENSAJE_OK;
						mensaje.Codigo = "UI_CLIENTE_CREADO";
						mensaje.Mensaje = "CLIENTE CREADO";
						entidadCreada.Mensajes.add(mensaje);
					}
					
					CrearAutorizadoOrden_Salida salidaCrearAutorizadoOrden = this.crearAutorizadoOrden(conn, entrada, documentosIdentidad);
					for (MensajeSalida mensajes : salidaCrearAutorizadoOrden.P_OUT_MENSAJES) {
				    	
				    	Mensaje mensajeCrearAutorizadoOrden 	= new Mensaje();
				    	mensajeCrearAutorizadoOrden.IdInterno 	= mensajes.id;
				    	mensajeCrearAutorizadoOrden.TipoMensaje = mensajes.tipo;
				    	mensajeCrearAutorizadoOrden.Codigo 		= mensajes.codigo;
				    	mensajeCrearAutorizadoOrden.Mensaje 	= mensajes.descripcion;
				    	entidadCreada.Mensajes.add(mensajeCrearAutorizadoOrden);
						
				    }
					if(mensajesAdvertencia.size() > 0) {
						entidadCreada.Mensajes.addAll(mensajesAdvertencia);
					}
						
				}
				
				if(salidaWS.P_OUT_ERRORESCONTADOR.equals(0L) && ("S").equals(EditarTelefonoYDirecciones)) {
					
					Boolean EdicionTelefonos = false, EdicionDireccionesFisicas = false, EdicionCorreos = false;
					
					if(socioNegocio.DireccionesFisicas.size() > 0) {
						EdicionDireccionesFisicas = true;
					}
					
					if(socioNegocio.DireccionesElectronicas.size() > 0) {
						EdicionCorreos = true;
					}
					
					if(socioNegocio.Telefonos.size() > 0) {
						EdicionTelefonos = true;
					}
					
					if(EdicionDireccionesFisicas || EdicionCorreos || EdicionTelefonos) {
						
						socioNegocio.IdPersona = socioNegocio.IdPersona.equals(0L) ? consultarIdPersonaNuevoClienteCreado(conn, salidaCrearClienteFull.P_OUT_REFID) : socioNegocio.IdPersona;
						
						List<MensajeSalida> mensajesPredeterminar = PredeterminarDireccionesYTelefonos(conn, entrada, socioNegocio, EdicionDireccionesFisicas, EdicionCorreos, EdicionTelefonos);
						
						if(mensajesPredeterminar.size() > 0) {
							List<Mensaje> mensajes = mensajesPredeterminar.stream().map(msj -> {
								Mensaje mensaje = new Mensaje();
								mensaje.Codigo = msj.codigo;
								mensaje.IdInterno = msj.id;
								mensaje.Mensaje = msj.descripcion;
								mensaje.TipoMensaje = msj.tipo;
								return mensaje;
							}).collect(Collectors.toList());
							
							entidadCreada.Mensajes.addAll(mensajes);
						}
					}
				}
			}
							
			salidaWS.P_OUT_ENTIDADCREADA.add(entidadCreada);
			
		}
		salidaWS.P_OUT_ERRORESCONTADOR = contarErrores(salidaWS);
		logger.debug("FIN::CrearClienteFullWS.modificarBorradorIndicador");
		return salidaWS;
	}
	
	private SalidaValidarCamposObligatorios validarEntrada(Connection con, EntradaCrearClienteFullWS entrada) throws Exception {
		ValidarCamposObligatorios validarCamposObligatorios = new ValidarCamposObligatorios();
		SalidaValidarCamposObligatorios resultado = validarCamposObligatorios.validarContenidoCampos(con, entrada.p_SocioNegocio, ID_TIPO_CANAL,  entrada.p_IdEmpresa);
		System.out.println(resultado);
		return resultado;
	}

	private Long consultarIdPersonaNuevoClienteCreado(Connection conn, List<ResultadoAdministrarBorradorEntidad> p_OUT_REFID) throws Exception {
		
		SQL sql = new SQL();
		
		Long idCliente = p_OUT_REFID.stream().findFirst().orElse(new ResultadoAdministrarBorradorEntidad()).IdEntidad;
		
		HashMap<String, Object> parametros = new HashMap<>(1);
		parametros.put("IdCliente", idCliente);
		IdPersonaConsulta idPersona = sql.query(conn, SQL_SELECT_IDPERSONA_CLIENTE_CREADO, parametros, IdPersonaConsulta.class).parallelStream().findFirst().orElse(new IdPersonaConsulta());
		return idPersona.IdPersona;
	}

	private SocioNegocio calcularCiudadDivisionPais(Connection conn, SocioNegocio socioNegocio) throws SQLException {
		logger.debug("INICIO::CrearClienteFullWS.calcularCiudadDivisionPais");
		Long inicio = System.currentTimeMillis();

		List<Long> idComunas = socioNegocio.DireccionesFisicas.parallelStream()
				.filter(direccion -> direccion.IdComuna > 0L).map(direccion -> direccion.IdComuna)
				.collect(Collectors.toList());

		SQL sql = new SQL();
		HashMap<String, Object> parametros = new HashMap<>();
		parametros.put("IdComunas", idComunas);

		Map<Long, Long> idCiudades = sql.query(conn, SQL_SELECT_COMUNA, parametros, Comuna.class).parallelStream()
				.collect(Collectors.toMap(c -> c.IdComuna, c -> c.IdCiudad));

		socioNegocio.DireccionesFisicas.parallelStream().forEach(direccion -> {
			direccion.IdCiudad = direccion.IdCiudad.equals(0L) ? idCiudades.get(direccion.IdComuna): direccion.IdCiudad;
		});

		Long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearClienteFullWS.calcularCiudadDivisionPais", fin - inicio);
		return socioNegocio;
	}

	private List<Mensaje> agregarMensajesAdvertencias(Connection conn, SalidaAdministrarBorrador salidaAdministrarBorrador, EntradaCrearClienteFullWS entrada) throws Exception {
		logger.debug("INICIO::CrearClienteFullWS.agregarMensajesAdvertencias");
		Long inicio = System.currentTimeMillis();
		HashMap<String, String> parametrosMensaje = new HashMap<>(2);
		PlataformaUtils.MensajeSalida ms = new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ADVERTENCIA, "UI_ADVERTENCIA_PERSONA_RELACIONADA_EXISTE", parametrosMensaje, conn, entrada.p_IdEmpresa);
		
		List<Mensaje> mensajes = salidaAdministrarBorrador.P_OUT_MENSAJES.stream().filter(msj -> PlataformaUtils.MensajeSalida.MENSAJE_ADVERTENCIA.equals(msj.tipo)).map(mensaje -> {
			Mensaje mensajeWS = new Mensaje();
			mensajeWS.Codigo = ms.codigo;
			mensajeWS.TipoMensaje = ms.tipo;
			mensajeWS.Mensaje = ms.descripcion;
			mensajeWS.IdInterno = mensaje.id;
			return mensajeWS;
		}).collect(Collectors.toList());

		Long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearClienteFullWS.agregarMensajesAdvertencias", fin - inicio);
		return mensajes;
	}

	private SalidaAdministrarBorrador crearBorradorNombreFantasia(Connection con, SocioNegocio socioNegocio,EntradaCrearClienteFullWS entrada, SalidaCrearBorradorEnrolamiento borrador) throws Exception {
		logger.debug("INICIO::CrearClienteFull.crearBorradorNombreFantasia");
		Long inicio = System.currentTimeMillis();
		
		CrearBorradorNombreFantasia crearBorradorNombreFantasia = new CrearBorradorNombreFantasia();
		EntradaCrearBorradorNombreFantasia entradaCrearBorradorNombreFantasia = new EntradaCrearBorradorNombreFantasia();
		AtomicLong nombeFantasiaIdInterno = new AtomicLong();
		List<cl.bcs.socionegocio.enr.pojo.NombreFantasia> nombresFantasia = socioNegocio.NombreFantasia.stream().map(nombre -> {
			cl.bcs.socionegocio.enr.pojo.NombreFantasia nombreFantasiaEnrolamiento = new cl.bcs.socionegocio.enr.pojo.NombreFantasia();
			nombreFantasiaEnrolamiento.IdInterno = nombeFantasiaIdInterno.getAndIncrement();
			nombreFantasiaEnrolamiento.IdEnrolamiento = borrador.P_OUT_REFID;
			nombreFantasiaEnrolamiento.NombreFantasia = nombre.NombreFantasia;
			return nombreFantasiaEnrolamiento;
		}).collect(Collectors.toList());
		
		entradaCrearBorradorNombreFantasia.p_CodigoFormulario = obtenerCodigoFormulario("NOMBRES_FANTASIA_PERSONA",borrador);
		entradaCrearBorradorNombreFantasia.p_NombresFantasia.addAll(nombresFantasia);
		entradaCrearBorradorNombreFantasia.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCrearBorradorNombreFantasia.p_IdModulo = ID_MODULO;
		entradaCrearBorradorNombreFantasia.p_IdTipoCanal = ID_TIPO_CANAL;
		entradaCrearBorradorNombreFantasia.p_IdUsuario = entrada.p_IdUsuario;
		entradaCrearBorradorNombreFantasia.p_UsaReglasNegocio = USA_REGLAS_NEGOCIO;
		
		SalidaAdministrarBorrador salidaAdministrarBorrador = new SalidaAdministrarBorrador();
		
		salidaAdministrarBorrador = crearBorradorNombreFantasia.logica(con, entradaCrearBorradorNombreFantasia);
		
		Long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearClienteFull.crearBorradorNombreFantasia", fin - inicio);
		
		return salidaAdministrarBorrador;
	}

	private SalidaAdministrarBorrador crearBorradorDeclaracionOrigenFondos(Connection con, SocioNegocio socioNegocio,EntradaCrearClienteFullWS entrada, SalidaCrearBorradorEnrolamiento borrador) throws Exception {
		logger.debug("INICIO::CrearClienteFull.crearBorradorDeclaracionOrigenFondos");
		Long inicio = System.currentTimeMillis();
		
		CrearBorradorDeclaracionOrigenFondos crearBorradorDeclaracionOrigenFondos = new CrearBorradorDeclaracionOrigenFondos();
		EntradaCrearBorradorDeclaracionOrigenFondos entradaCrearBorradorDeclaracionOrigenFondos = new EntradaCrearBorradorDeclaracionOrigenFondos();
		AtomicLong declaracionIdInterno = new AtomicLong();
		List<cl.bcs.socionegocio.enr.pojo.DeclaracionOrigenFondos> declaraciones = socioNegocio.DatosCliente.DeclaracionOrigenFondo.stream().map(declaracion -> {
			cl.bcs.socionegocio.enr.pojo.DeclaracionOrigenFondos declaracionOrigenFondosEnrolamiento = new cl.bcs.socionegocio.enr.pojo.DeclaracionOrigenFondos();
			declaracionOrigenFondosEnrolamiento.IdInterno = declaracionIdInterno.getAndIncrement();
			declaracionOrigenFondosEnrolamiento.IdEnrolamiento = borrador.P_OUT_REFID;
			declaracionOrigenFondosEnrolamiento.IdTipoOperacionOrigenFondos = declaracion.IdTipoOperacionOrigenFondos;
			declaracionOrigenFondosEnrolamiento.FechaDeclaracion = Fecha.toDate(declaracion.FechaDeclaracion);
			declaracionOrigenFondosEnrolamiento.Comentario = declaracion.Comentario;
			declaracionOrigenFondosEnrolamiento.OperacionInscripcion = declaracion.OperacionInscripcion;
			declaracionOrigenFondosEnrolamiento.DetalleOrigenFondo = declaracion.DetalleOrigenFondo.stream().map(detalle -> {
				cl.bcs.socionegocio.enr.pojo.DetalleOrigenFondo detalledeclaracionOrigenFondosEnrolamiento = new cl.bcs.socionegocio.enr.pojo.DetalleOrigenFondo();
				detalledeclaracionOrigenFondosEnrolamiento.IdTipoOrigenFondo = detalle.IdTipoOrigenFondo;
				detalledeclaracionOrigenFondosEnrolamiento.Valor = BigDecimal.valueOf(detalle.Valor);
				detalledeclaracionOrigenFondosEnrolamiento.Otros = detalle.Otros;
				return detalledeclaracionOrigenFondosEnrolamiento;
			}).collect(Collectors.toList());
			return declaracionOrigenFondosEnrolamiento;
		}).collect(Collectors.toList());
		
		entradaCrearBorradorDeclaracionOrigenFondos.p_CodigoFormulario = obtenerCodigoFormulario("DECLARACION_ORIGEN_FONDOS",borrador);
		entradaCrearBorradorDeclaracionOrigenFondos.p_DeclaracionOrigenFondos.addAll(declaraciones);
		entradaCrearBorradorDeclaracionOrigenFondos.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCrearBorradorDeclaracionOrigenFondos.p_IdModulo = ID_MODULO;
		entradaCrearBorradorDeclaracionOrigenFondos.p_IdTipoCanal = socioNegocio.IdTipoCanalIngresoCorredor.trim().isEmpty() ? ID_TIPO_CANAL : socioNegocio.IdTipoCanalIngresoCorredor;
		entradaCrearBorradorDeclaracionOrigenFondos.p_IdUsuario = entrada.p_IdUsuario;
		entradaCrearBorradorDeclaracionOrigenFondos.p_UsaReglasNegocio = USA_REGLAS_NEGOCIO;
		
		SalidaAdministrarBorrador salidaAdministrarBorrador = new SalidaAdministrarBorrador();
		
		salidaAdministrarBorrador = crearBorradorDeclaracionOrigenFondos.logica(con, entradaCrearBorradorDeclaracionOrigenFondos);
		
		Long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearClienteFull.crearBorradorDeclaracionOrigenFondos", fin - inicio);
		
		return salidaAdministrarBorrador;
	}

	private boolean debeCrearEmpleador(Empleador empleador) {
		if( empleador.IdEmpleador >0 || empleador.IdTipoPersona.equals("")) {
			return false;
		}
		if( empleador.IdEmpleador == 0 && empleador.IdTipoPersona.equals("N") && (empleador.Nombres.equals("") || empleador.ApellidoPaterno.equals("") || empleador.ApellidoMaterno.equals("") || empleador.DocumentoIdentidad.equals("") || empleador.TipoDocumentoIdentidad.equals(""))) {
			return false;
		}
		
		if( empleador.IdEmpleador == 0 && empleador.IdTipoPersona.equals("J") && ( empleador.RazonSocial.equals("") || empleador.DocumentoIdentidad.equals("") || empleador.TipoDocumentoIdentidad.equals(""))) {
			return false;
		}
		
		return true;
	}
	
	private boolean esJuridico(SocioNegocio socioNegocio) {
		return "J".equals(socioNegocio.IdTipoPersona);
	}

	private void cargarDatosSocioNegocioExistente(Connection conn, SocioNegocio socioNegocio,EntradaCrearClienteFullWS in) throws Exception {
		logger.debug("INICIO::CrearClienteFullWS.cargarDatosSocioNegocioExistente");
	    Long inicio = System.currentTimeMillis();

		HashMap<String, Object> params = new HashMap<String, Object>(2);
		params.put("IdPersona", socioNegocio.IdPersona);
		
		socioNegocio.DireccionesFisicas.addAll(obtenerDireccionFisica(conn, params));
		socioNegocio.DireccionesFisicas.parallelStream().forEach( dir-> {
			dir.IdPersona = socioNegocio.IdPersona;
		});
		socioNegocio.Telefonos.addAll(obtenerTelefonos(conn, params));
		socioNegocio.Telefonos.parallelStream().forEach( tel-> {
			tel.IdPersona = socioNegocio.IdPersona;
		});
		socioNegocio.DireccionesElectronicas.addAll(obtenerDireccionElectronica(conn, params));
		socioNegocio.DireccionesElectronicas.parallelStream().forEach( dire-> {
			dire.IdPersona = socioNegocio.IdPersona;
		});
		socioNegocio.TiposDocumento = (obtenerTipoDocumento(conn, params));
		socioNegocio.RelacionesPersona.addAll(obtenerRelacionPersona(conn, params));
		socioNegocio.CuentasBancarias.addAll(obtenerCuentasBancaria(conn, params));
		socioNegocio.TarjetasBancarias.addAll(obtenerTarjetasBancaria(conn, params));
		socioNegocio.FatcaCrs = obtenerFatca(conn,  params, socioNegocio, in);
		socioNegocio.IndicadoresPersona = obtenerIndicadoresPersona(conn, params);
		
		Long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearClienteFullWS.cargarDatosSocioNegocioExistente {}", fin - inicio); 
	}

	private List<Indicador> obtenerIndicadoresPersona(Connection conn, HashMap<String, Object> params) throws Exception {
		logger.debug("FIN::CrearClienteFullWS.obtenerIndicadoresPersona"); 
		Long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		List<Indicador> indicadores = sql.query(conn, SQL_SELECT_PERSONA_INDICADOR ,params, Indicador.class);
		
		Long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearClienteFullWS.obtenerIndicadoresPersona {}", fin - inicio); 
		
		return indicadores;
	}

	private List<Long> obtenerIdPersonaSocioNegocio(Connection conn, List<String> documentosIdentidad) throws Exception {
		
		logger.debug("INICIO::CrearClienteFullWS.obtenerIdPersonaSocioNegocio");
	    Long inicio = System.currentTimeMillis();
		
		HashMap<String, Object> params = new HashMap<String, Object>(2);
		params.put("DocumentosIdentidad", documentosIdentidad);		
		
		SQL sql = new SQL();
		List<Long> idPersona = sql.query(conn, SQL_SELECT_IDPERSONA_DOCUMENTO_IDENTIDAD ,params, IdPersonaConsulta.class).parallelStream().map(ids -> {
			return ids.IdPersona;
		}).collect(Collectors.toList());

		Long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearClienteFullWS.obtenerIdPersonaSocioNegocio {}", fin - inicio); 
		
		return idPersona;
	}
	
	private Persona obtenerDatosPersona(Connection conn, HashMap<String, Object> params) throws Exception {
		logger.debug("FIN::CrearClienteFullWS.obtenerDatosPersona"); 
		Long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		Persona persona = sql.query(conn, SQL_SELECT_DATOS_PERSONA, params, Persona.class).parallelStream().findFirst().orElse(new Persona()); 
		
		Long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearClienteFullWS.obtenerDatosPersona {}", fin - inicio); 
		
		return persona;
	}

	private List<DireccionFisica> obtenerDireccionFisica(Connection conn, HashMap<String, Object> params) throws Exception  {
		logger.debug("FIN::CrearClienteFullWS.obtenerDireccionFisica"); 
		Long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		List<DireccionFisica> direccionFisicas = sql.query(conn, SQL_SELECT_PERSONA_DIRECCION_FISICA ,params, DireccionFisica.class);
		
		Long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearClienteFullWS.obtenerDireccionFisica {}", fin - inicio); 
		
		return direccionFisicas;
	}
	
	private List<Telefono> obtenerTelefonos(Connection conn, HashMap<String, Object> params) throws Exception {
		logger.debug("FIN::CrearClienteFullWS.obtenerTelefonos"); 
		Long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		List<Telefono> telefonos = sql.query(conn, SQL_SELECT_PERSONA_TELEFONOS ,params, Telefono.class);
		
		Long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearClienteFullWS.obtenerTelefonos {}", fin - inicio); 
		
		return telefonos;
	}

	private List<DireccionElectronica> obtenerDireccionElectronica(Connection conn, HashMap<String, Object> params) throws Exception {	
		logger.debug("FIN::CrearClienteFullWS.obtenerDireccionElectronica"); 
		Long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		List<DireccionElectronica> direccionElectronicas = sql.query(conn, SQL_SELECT_PERSONA_DIRECCION_ELECTRONICA ,params, DireccionElectronica.class);
		
		Long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearClienteFullWS.obtenerDireccionElectronica {}", fin - inicio); 
		
		return direccionElectronicas;
	}

	private List<TipoDocumento> obtenerTipoDocumento(Connection conn, HashMap<String, Object> params) throws Exception {
		logger.debug("FIN::CrearClienteFullWS.obtenerTipoDocumento"); 
		Long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		List<TipoDocumento> tipoDocumentos = sql.query(conn, SQL_SELECT_PERSONA_DOCUMENTO_IDENTIDAD ,params, TipoDocumento.class);
		
		Long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearClienteFullWS.obtenerTipoDocumento {}", fin - inicio);
		
		return tipoDocumentos;
	}
		
	private List<RelacionPersona> obtenerRelacionPersona(Connection conn, HashMap<String, Object> params) throws Exception {
		logger.debug("FIN::CrearClienteFullWS.obtenerRelacionPersona"); 
		Long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		List<RelacionPersona> relacionPersonas = sql.query(conn, SQL_SELECT_PERSONA_RELACION_PERSONA ,params, RelacionPersona.class);
		
		Long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearClienteFullWS.obtenerRelacionPersona {}", fin - inicio);
		return relacionPersonas;
	}

	private List<CuentaBancaria> obtenerCuentasBancaria(Connection conn, HashMap<String, Object> params) throws Exception {
		
		logger.debug("FIN::CrearClienteFullWS.obtenerCuentasBancarias"); 
		Long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		List<CuentaBancaria> cuentaBancarias = sql.query(conn, SQL_SELECT_PERSONA_CUENTA_BANCARIA ,params, CuentaBancaria.class);
		
		Long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearClienteFullWS.obtenerCuentasBancarias {}", fin - inicio);
		
		return cuentaBancarias;
	}
	
	private List<TarjetasBancaria> obtenerTarjetasBancaria(Connection conn, HashMap<String, Object> params) throws Exception {
		
		logger.debug("FIN::CrearClienteFullWS.obtenerTarjetasBancaria"); 
		Long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		List<TarjetasBancaria> tarjetasBancarias = sql.query(conn, SQL_SELECT_PERSONA_TARJETA_BANCARIA ,params, TarjetasBancaria.class);
		
		Long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearClienteFullWS.obtenerTarjetasBancaria {}", fin - inicio);
		
		return tarjetasBancarias;
	}
	
	private FatcaCrs obtenerFatca(Connection conn, HashMap<String, Object> params, SocioNegocio socioNegocio, EntradaCrearClienteFullWS in) throws Exception {
		
		logger.debug("FIN::CrearClienteFullWS.obtenerFatca"); 
		Long inicio = System.currentTimeMillis();
		FatcaCrs fatca = new FatcaCrs();
		
		List<PaisResidencia> paisResidencias = obtenerPaisResidencia(conn, params);
		
		SQL sql = new SQL();
		List<FatcaCrs> fatcasCrs = sql.query(conn, SQL_SELECT_PERSONA_FACTA ,params, FatcaCrs.class);
		
		if(fatcasCrs.size() == 0) {
			crearFatca(conn, params.get("IdPersona"), socioNegocio, in);
		}else {
			fatca = fatcasCrs.parallelStream().findFirst().orElse(new FatcaCrs());
		}
		
		fatca.PaisesResidencia = paisResidencias;
		
		Long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearClienteFullWS.obtenerFatca {}", fin - inicio);
		
		return fatca;
	}
	
	private void crearFatca(Connection conn, Object idPersona, SocioNegocio socioNegocio, EntradaCrearClienteFullWS in) throws Exception {
		logger.debug("FIN::CrearClienteFullWS.crearFatca"); 
		Long inicio = System.currentTimeMillis();
		
		DatosPais paisUsa = Utils.getDatosPais(conn, "US");
		
		cl.bcs.socionegocio.per.pojo.Fatca fatca = new cl.bcs.socionegocio.per.pojo.Fatca();
		fatca = fatca.FatcaPorDefecto();
		fatca.IdPersona = Long.parseLong(idPersona.toString());
		fatca.IdClasificacionEmpresaFatca = "J".equals(socioNegocio.IdTipoPersona) ? "PENDIENTE" : "";
		fatca.ExcepcionResidente = paisUsa.IdPais.equals(socioNegocio.IdPaisResidenciaCasaMatriz) ? "S" : "N";
		fatca.ExcepcionNacimiento = paisUsa.IdPais.equals(socioNegocio.IdPaisNacimientoConstitucion) ? "S" : "N";
		
		CrearFatca crearFatca = new CrearFatca();
		EntradaCrearFatca entradaCrearFatca = new EntradaCrearFatca();
		entradaCrearFatca.p_IdEmpresa = in.p_IdEmpresa;
		entradaCrearFatca.p_IdUsuario = in.p_IdUsuario;
		entradaCrearFatca.p_Fatcas.add(fatca);
		
		crearFatca.logica(conn, entradaCrearFatca);
		
		Long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearClienteFullWS.crearFatca {}", fin - inicio);
	}

	private List<PaisResidencia> obtenerPaisResidencia(Connection conn, HashMap<String, Object> params) throws Exception {
		logger.debug("FIN::CrearClienteFullWS.obtenerPaisResidencia"); 
		Long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		List<PaisResidencia> paisResidencias = sql.query(conn, SQL_SELECT_PERSONA_FACTA_PAIS_RESIDENCIA ,params, PaisResidencia.class);
		
		Long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearClienteFullWS.obtenerPaisResidencia {}", fin - inicio);
		
		return paisResidencias;
	}

	private SalidaAdministrarBorrador crearEmpleador(Connection conn, SocioNegocio socioNegocio,
			EntradaCrearClienteFullWS in, SalidaCrearBorradorEnrolamiento salidaCrearBorradorEnrolamiento, String agregaAdvertencia) throws Exception {
			
		    logger.debug("INICIO::CrearClienteFullWS.crearEmpleador");
		    Long inicio = System.currentTimeMillis();
		
		    EntradaAdministrarWS entradaWs = new EntradaAdministrarWS();
		    entradaWs.p_IdEmpresa = in.p_IdEmpresa;
		    entradaWs.p_IdModulo = ID_MODULO;
		    entradaWs.p_IdUsuario = in.p_IdUsuario;
		    SalidaAdministrarWS salidaWS = new SalidaAdministrarWS();
		    SalidaWBS salida = new SalidaWBS();
		    EntidadManipulada entidadAValidar = new EntidadManipulada();
		    
			CrearPersona crearPersona = new CrearPersona();
			CrearPersona.EntradaServicioCrearPersona entradaServicioCrearPersona = new EntradaServicioCrearPersona(); 
			entradaServicioCrearPersona.p_IdEmpresa = in.p_IdEmpresa;
			entradaServicioCrearPersona.p_IdModulo = ID_MODULO;
			entradaServicioCrearPersona.p_IdUsuario = in.p_IdUsuario;
			Enrolamiento enrolamiento = new Enrolamiento();
			
			Empleador datosEmpleadorDesdeBD = new Empleador();
			datosEmpleadorDesdeBD = consultarDatosEmpleadorBD(conn, socioNegocio.Empleador.DocumentoIdentidad) ;

			SalidaAdministrarBorrador salidaAdministrarBorrador = new SalidaAdministrarBorrador();
			
			SQL sql = new SQL();
			HashMap<String, Object> params = new HashMap<String, Object>(2);
			params.put("DocumentosIdentidad", socioNegocio.Empleador.DocumentoIdentidad);
			Long idPersonaExiste = sql.query(conn, SQL_SELECT_IDPERSONA_DOCUMENTO_IDENTIDAD, params, IdPersonaConsulta.class).stream().findFirst().orElse(new IdPersonaConsulta()).IdPersona;
			
			if(idPersonaExiste > 0 && agregaAdvertencia.equalsIgnoreCase("S")) {
				HashMap<String, String> parametrosMensaje = new HashMap<>(2);
				salidaAdministrarBorrador.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(idPersonaExiste, PlataformaUtils.MensajeSalida.MENSAJE_ADVERTENCIA, "UI_ADVERTENCIA_PERSONA_RELACIONADA_EXISTE", parametrosMensaje, conn, in.p_IdEmpresa) );
				
				ResultadoAdministrarBorradorEntidad administrarBorradorEntidad = new ResultadoAdministrarBorradorEntidad();
				administrarBorradorEntidad.IdEntidad = idPersonaExiste;
				administrarBorradorEntidad.IdInterno = socioNegocio.IdInterno;
				salidaAdministrarBorrador.P_OUT_REFID = new ArrayList<ResultadoAdministrarBorradorEntidad>(Collections.singletonList(administrarBorradorEntidad));
				
				return salidaAdministrarBorrador;
			}
			
	    	if(entidadEmpleadorEsVacia(datosEmpleadorDesdeBD) || agregaAdvertencia.equalsIgnoreCase("N")) {	    	
				
				enrolamiento.ApellidoMaterno = socioNegocio.Empleador.ApellidoMaterno;
				enrolamiento.ApellidoPaterno = socioNegocio.Empleador.ApellidoPaterno;
				enrolamiento.DocumentoIdentidad = socioNegocio.Empleador.DocumentoIdentidad;
				enrolamiento.IdTipoDocumentoIdentidad = socioNegocio.Empleador.TipoDocumentoIdentidad;
				enrolamiento.Nombres = socioNegocio.Empleador.Nombres;
				enrolamiento.RazonSocial = socioNegocio.Empleador.RazonSocial;
				enrolamiento.Tipo = socioNegocio.Empleador.IdTipoPersona;
				enrolamiento.IdTipoEntidad = "PERSONA";
				enrolamiento.IdEmpresa = in.p_IdEmpresa;
				enrolamiento.IdTipoDocumentoIdentidadAsociado = "";
				enrolamiento.IdTipoCanalIngresoCorredor = socioNegocio.IdTipoCanalIngresoCorredor;
				
	    	}else{
	    		
	    		enrolamiento.ApellidoMaterno = datosEmpleadorDesdeBD.ApellidoMaterno;
				enrolamiento.ApellidoPaterno = datosEmpleadorDesdeBD.ApellidoPaterno;
				enrolamiento.DocumentoIdentidad = datosEmpleadorDesdeBD.DocumentoIdentidad;
				enrolamiento.IdTipoDocumentoIdentidad = datosEmpleadorDesdeBD.TipoDocumentoIdentidad;
				enrolamiento.Nombres = datosEmpleadorDesdeBD.Nombres;
				enrolamiento.RazonSocial = datosEmpleadorDesdeBD.RazonSocial;
				enrolamiento.Tipo = datosEmpleadorDesdeBD.IdTipoPersona;
				enrolamiento.IdTipoEntidad = "PERSONA";
				enrolamiento.IdEmpresa = in.p_IdEmpresa;
				enrolamiento.IdTipoDocumentoIdentidadAsociado = "";
				enrolamiento.IdTipoCanalIngresoCorredor = datosEmpleadorDesdeBD.IdTipoCanalIngresoCorredor;
	    	}
			
			entradaServicioCrearPersona.p_ConsultarEnrolamiento = new ArrayList<Enrolamiento>(Collections.singletonList(enrolamiento)) ;
			
			List<Enrolamiento> datosAValidar = new ArrayList<Enrolamiento>();
			datosAValidar.add(enrolamiento);
			
			FormularioCanal formuCanal = new FormularioCanal();
			formuCanal.IdEmpresa = in.p_IdEmpresa;
			formuCanal.CodigoFormulario = WebServiceUtils.obtenerCodigoFormulario(conn,socioNegocio.IdTipoPersona , TIPO_ENROLAMIENTO, "DATOS_GENERALES_PERSONA_DATOS_EMPLEADOR");
			formuCanal.IdTipoCanal = socioNegocio.IdTipoCanalIngresoCorredor.trim().isEmpty() ? ID_TIPO_CANAL : socioNegocio.IdTipoCanalIngresoCorredor;
			
			salidaWS = entradaWs.validarCamposObligatoriosEntrada(conn,datosAValidar, Enrolamiento.class, formuCanal, salidaWS);
			
		
			if(salidaWS.P_OUT_ERRORESCONTADOR > 0) {
				
				entidadAValidar = WebServiceUtils.agregarMensajesCamposRequeridos(entidadAValidar, salidaWS);
				entidadAValidar.IdEntidad = 0L;
				entidadAValidar.TipoEntidad = "EMPLEADOR";
				salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadAValidar);
				salida.P_OUT_ERRORESCONTADOR += salidaWS.P_OUT_ERRORESCONTADOR;
				
				salidaAdministrarBorrador.P_OUT_MENSAJES = salidaWS.P_OUT_MENSAJES.parallelStream().map(err -> {
					PlataformaUtils.MensajeSalida mensajeSalida = new PlataformaUtils.MensajeSalida();
	                mensajeSalida.codigo  = err.codigo;
	                mensajeSalida.descripcion = err.descripcion;
	                mensajeSalida.id = socioNegocio.IdInterno;
	                mensajeSalida.tipo = PlataformaUtils.MensajeSalida.MENSAJE_ERROR;
					return mensajeSalida;
				}).collect(Collectors.toList());
				salidaAdministrarBorrador.P_OUT_ERRORESCONTADOR += salidaWS.P_OUT_ERRORESCONTADOR;
				return salidaAdministrarBorrador;
			}
					
			if(!entidadEmpleadorEsVacia(datosEmpleadorDesdeBD) && agregaAdvertencia.equalsIgnoreCase("S")) {
				PlataformaUtils.MensajeSalida mensajeSalida = new PlataformaUtils.MensajeSalida();
                mensajeSalida.codigo  = "";
                mensajeSalida.descripcion = "EMPLEADOR SELECCIONADO";
                mensajeSalida.id = datosEmpleadorDesdeBD.IdEmpleador;
                mensajeSalida.tipo = PlataformaUtils.MensajeSalida.MENSAJE_OK;
				salidaAdministrarBorrador.P_OUT_MENSAJES.add(mensajeSalida);
				ResultadoAdministrarBorradorEntidad administrarBorradorEntidad = new ResultadoAdministrarBorradorEntidad();
				administrarBorradorEntidad.IdEntidad = datosEmpleadorDesdeBD.IdEmpleador;
				administrarBorradorEntidad.IdInterno = socioNegocio.IdInterno;
				salidaAdministrarBorrador.P_OUT_REFID = new ArrayList<ResultadoAdministrarBorradorEntidad>(Collections.singletonList(administrarBorradorEntidad));
				
			} else {
				if(idPersonaExiste > 0) {
					
					List<RegistroCambioMasivo> cambios = new ArrayList<RegistroCambioMasivo>();
					if(datosEmpleadorDesdeBD.IdTipoPersona.equals("J") ) {
						if(!enrolamiento.RazonSocial.equals(datosEmpleadorDesdeBD.RazonSocial) && !enrolamiento.RazonSocial.isEmpty()) {
							RegistroCambioMasivo cambioRazonSocial = new RegistroCambioMasivo();
							cambioRazonSocial.CampoModificado = "RazonSocial";
							cambioRazonSocial.IdEntidad = idPersonaExiste;
							cambioRazonSocial.ValorNuevo = enrolamiento.RazonSocial;
							cambios.add(cambioRazonSocial);
						}
						
					} else {
						if(!enrolamiento.Nombres.equals(datosEmpleadorDesdeBD.Nombres) && !enrolamiento.Nombres.isEmpty()) {
							RegistroCambioMasivo cambioNombres = new RegistroCambioMasivo();
							cambioNombres.CampoModificado = "Nombres";
							cambioNombres.IdEntidad = idPersonaExiste;
							cambioNombres.ValorNuevo = enrolamiento.Nombres;
							cambios.add(cambioNombres);
						}
						if(!enrolamiento.ApellidoPaterno.equals(datosEmpleadorDesdeBD.ApellidoPaterno) && !enrolamiento.ApellidoPaterno.isEmpty()) {
							RegistroCambioMasivo cambioPaterno = new RegistroCambioMasivo();
							cambioPaterno.CampoModificado = "ApellidoPaterno";
							cambioPaterno.IdEntidad = idPersonaExiste;
							cambioPaterno.ValorNuevo = enrolamiento.ApellidoPaterno;
							cambios.add(cambioPaterno);
						}
						if(!enrolamiento.ApellidoMaterno.equals(datosEmpleadorDesdeBD.ApellidoMaterno) && !enrolamiento.ApellidoMaterno.isEmpty()) {
							RegistroCambioMasivo cambioMaterno = new RegistroCambioMasivo();
							cambioMaterno.CampoModificado = "ApellidoPaterno";
							cambioMaterno.IdEntidad = idPersonaExiste;
							cambioMaterno.ValorNuevo = enrolamiento.ApellidoPaterno;
							cambios.add(cambioMaterno);
						}
					}
					
					if(!cambios.isEmpty()) {
						EntradaEjecutarCambiosPersona entradaEjecutarCambiosPersona = new EntradaEjecutarCambiosPersona();
						entradaEjecutarCambiosPersona.p_IdEmpresa = in.p_IdEmpresa;
						entradaEjecutarCambiosPersona.p_IdModulo = ID_MODULO;
						entradaEjecutarCambiosPersona.p_IdUsuario = in.p_IdUsuario;
						entradaEjecutarCambiosPersona.p_UsaReglasNegocio = "N";
						entradaEjecutarCambiosPersona.p_IdTipoCanal = "WBS";
						
						entradaEjecutarCambiosPersona.p_RegistroCambio.addAll(cambios);
						
						ModificarPersonaDatosGeneralesMasivo modificarPersonaDatosGeneralesMasivo =new ModificarPersonaDatosGeneralesMasivo();
						
						SalidaEjecutarCambiosPersona salidaEjecutarCambiosPersona = modificarPersonaDatosGeneralesMasivo.logica(conn, entradaEjecutarCambiosPersona);
						
						salidaAdministrarBorrador.P_OUT_ERRORESCONTADOR = salidaEjecutarCambiosPersona.P_OUT_ERRORESCONTADOR + salidaEjecutarCambiosPersona.P_OUT_ERRORESCONTADOR;
						salidaAdministrarBorrador.P_OUT_MENSAJES = salidaEjecutarCambiosPersona.P_OUT_MENSAJES.stream().map(mensaje -> {
							PlataformaUtils.MensajeSalida mensajeSalida = new PlataformaUtils.MensajeSalida();
							mensajeSalida.codigo = mensaje.codigo;
							mensajeSalida.descripcion = mensaje.descripcion;
							mensajeSalida.tipo = mensaje.tipo;
							return mensajeSalida;
						}).collect(Collectors.toList());
						
					}
					
					ResultadoAdministrarBorradorEntidad administrarBorradorEntidad = new ResultadoAdministrarBorradorEntidad();
					administrarBorradorEntidad.IdEntidad = idPersonaExiste;
					administrarBorradorEntidad.IdInterno = socioNegocio.IdInterno;
					salidaAdministrarBorrador.P_OUT_REFID = new ArrayList<ResultadoAdministrarBorradorEntidad>(Collections.singletonList(administrarBorradorEntidad));
					
				}else {
					SalidaServicioCrearPersona salidaServicioCrearPersona = crearPersona.logica(conn, entradaServicioCrearPersona);		
					
					salidaAdministrarBorrador.P_OUT_MENSAJES = salidaServicioCrearPersona.P_OUT_ERRORESDETALLE.stream().map(err -> {
						PlataformaUtils.MensajeSalida mensajeSalida = new PlataformaUtils.MensajeSalida();
						mensajeSalida.codigo  = err.Codigo;
						mensajeSalida.descripcion = err.Descripcion;
						mensajeSalida.id = socioNegocio.IdInterno;
						mensajeSalida.tipo = agregaAdvertencia.equalsIgnoreCase("S") && err.Codigo.equalsIgnoreCase("PERSONA_EXISTE") ? PlataformaUtils.MensajeSalida.MENSAJE_ADVERTENCIA : PlataformaUtils.MensajeSalida.MENSAJE_ERROR ;//"PERSONA";
						return mensajeSalida;
					}).collect(Collectors.toList());
					
					salidaAdministrarBorrador.P_OUT_ERRORESCONTADOR = salidaAdministrarBorrador.P_OUT_MENSAJES.stream().filter(msj -> PlataformaUtils.MensajeSalida.MENSAJE_ERROR.equals(msj.tipo) ).count();
					
					
					ResultadoAdministrarBorradorEntidad administrarBorradorEntidad = new ResultadoAdministrarBorradorEntidad();
					administrarBorradorEntidad.IdEntidad = salidaServicioCrearPersona.P_OUT_REFID;
					administrarBorradorEntidad.IdInterno = socioNegocio.IdInterno;
					salidaAdministrarBorrador.P_OUT_REFID = new ArrayList<ResultadoAdministrarBorradorEntidad>(Collections.singletonList(administrarBorradorEntidad));
				}
			}
					
			Long fin = System.currentTimeMillis();
			logger.debug("FIN::CrearClienteFullWS.crearEmpleador {}", fin - inicio);
			
		return salidaAdministrarBorrador;
	}

	private Empleador consultarDatosEmpleadorBD(Connection conn, String documentoIdentidad) throws Exception {
		logger.debug("INICIO::CrearClienteFullWS.consultarDatosEmpleadorBD");
	    Long inicio = System.currentTimeMillis();
	    
		HashMap<String, Object> params = new HashMap<String, Object>(2);
		params.put("DocumentoIdentidad", documentoIdentidad );
				
		SQL sql = new SQL();
		Empleador empleador = (sql.query(conn, SQL_SELECT_PERSONA_DATOS_EMPLEADOR , params, Empleador.class)).parallelStream().findFirst().orElse(new Empleador());
		
	    long fin = System.currentTimeMillis();
		logger.info("FIN::CrearClienteFullWS.consultarDatosEmpleadorBD", fin - inicio);
		return empleador;
		
	}
	
	private boolean entidadEmpleadorEsVacia(Empleador datosEmpleador) throws Exception{
		logger.debug("INICIO::CrearClienteFullWS.entidadEmpleadorEsVacia");
	    Long inicio = System.currentTimeMillis();
	    
	    if(!datosEmpleador.DocumentoIdentidad.equals("") || !datosEmpleador.TipoDocumentoIdentidad.equals("") || !datosEmpleador.ApellidoMaterno.equals("") || !datosEmpleador.ApellidoPaterno.equals("")
	    		|| !datosEmpleador.Nombres.equals("") || !datosEmpleador.RazonSocial.equals("") || !datosEmpleador.IdTipoPersona.equals("")) {
	    	return false;
	    }
	    
	    long fin = System.currentTimeMillis();
		logger.info("FIN::CrearClienteFullWS.entidadEmpleadorEsVacia", fin - inicio);
		return true;
	}

	private EntidadCreada crearSalidaMensajesCrearClienteFull(SocioNegocio socioNegocio,SalidaCrearClienteFull salida, EntidadCreada entidadCreada) {
		AtomicLong contadorMensajes = new AtomicLong();
		List<Mensaje> mensajes = salida.P_OUT_MENSAJES.stream().map(mensaje -> {
			Mensaje mensajeWS = new Mensaje();
			mensajeWS.Codigo = mensaje.codigo;
			mensajeWS.TipoMensaje = mensaje.tipo;
			mensajeWS.Mensaje = mensaje.descripcion;
			mensajeWS.IdInterno = contadorMensajes.getAndIncrement();
			return mensajeWS;
		}).collect(Collectors.toList());
		
		entidadCreada.Mensajes.addAll(mensajes);
		return entidadCreada;
	}

	private EntidadCreada agregarError(String codigo, String descripcion, EntidadCreada entidadCreada) {
	    Mensaje mensaje = new Mensaje();
	    mensaje.Codigo = codigo;
	    mensaje.IdInterno = 0L;
	    mensaje.Mensaje = descripcion;
	    mensaje.TipoMensaje = PlataformaUtils.MensajeSalida.MENSAJE_ERROR;
	    entidadCreada.Mensajes.add(mensaje);
	    return entidadCreada;
	}

	private SalidaAdministrarBorrador modificarBorradorIndicador(Connection conn, SocioNegocio socioNegocio, EntradaCrearClienteFullWS entrada, SalidaCrearBorradorEnrolamiento borrador, List<Indicador> indicadoresSocio, String codigoFormulario, String idTipoEntidad) throws Exception {
	   
		logger.debug("INICIO::CrearClienteFullWS.modificarBorradorIndicador");
		Long inicio = System.currentTimeMillis();
		ModificarBorradorIndicador modificarBorradorIndicador = new ModificarBorradorIndicador();
		SalidaAdministrarBorrador salidaAdministrarBorrador = new SalidaAdministrarBorrador();
		AtomicLong indicadorIdInterno = new AtomicLong();
		
		List<String> codigosIndicadores = indicadoresSocio.stream()
				.map(indicador -> indicador.CodigoTipoIndicador).collect(Collectors.toList());
		HashMap<String, Object> parametros = new HashMap<String, Object>(5);
		parametros.put("CodigoTipoIndicador", codigosIndicadores);
		parametros.put("IdEmpresa", entrada.p_IdEmpresa);
		parametros.put("IdEnrolamiento", borrador.P_OUT_REFID);
		parametros.put("IdTipoEntidad",idTipoEntidad);
		SQL sql = new SQL();
		List<Id> idsIndicadores = sql.query(conn, SQL_SELECT_INDICADORES, parametros, Id.class);
		Map<String, Id> indicadorPorCodigoTipoIndicador = idsIndicadores.stream().collect(Collectors.toMap(u -> u.CodigoTipoIndicador, u -> u));
		if (idsIndicadores.size() == codigosIndicadores.size()) {
			List<cl.bcs.socionegocio.enr.pojo.Indicador> indicadores = indicadoresSocio
					.stream()
					.filter(indicador -> indicadorPorCodigoTipoIndicador.containsKey(indicador.CodigoTipoIndicador))
					.map(indicador -> {
						Id idIndicador = indicadorPorCodigoTipoIndicador.get(indicador.CodigoTipoIndicador);
						idIndicador.IdInterno = indicadorIdInterno.getAndIncrement();
						cl.bcs.socionegocio.enr.pojo.Indicador indicadorBorrador = new cl.bcs.socionegocio.enr.pojo.Indicador();
						indicadorBorrador.IdIndicador = idIndicador.Id;
						indicadorBorrador.IdInterno = idIndicador.IdInterno;
						indicadorBorrador.IdEnrolamiento = borrador.P_OUT_REFID;
						indicadorBorrador.ValorIndicador = indicador.ValorIndicador;
						indicadorBorrador.CodigoTipoIndicador = indicador.CodigoTipoIndicador;
						return indicadorBorrador;
					})
					.collect(Collectors.toList());
			
			EntradaModificarBorradorIndicador entradaModificarIndicador = new EntradaModificarBorradorIndicador();
			entradaModificarIndicador.p_CodigoFormulario = obtenerCodigoFormulario(codigoFormulario, borrador);
			entradaModificarIndicador.p_Indicadores.addAll(indicadores);
			entradaModificarIndicador.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaModificarIndicador.p_IdModulo = ID_MODULO;
			entradaModificarIndicador.p_IdTipoCanal = socioNegocio.IdTipoCanalIngresoCorredor.trim().isEmpty() ? ID_TIPO_CANAL : socioNegocio.IdTipoCanalIngresoCorredor;
			entradaModificarIndicador.p_IdUsuario = entrada.p_IdUsuario;
			entradaModificarIndicador.p_UsaReglasNegocio = USA_REGLAS_NEGOCIO;
			
			salidaAdministrarBorrador = modificarBorradorIndicador.logica(conn, entradaModificarIndicador);
			
			if(salidaAdministrarBorrador.P_OUT_ERRORESCONTADOR > 0) {
				ResultadoAdministrarBorradorEntidad resultado = new ResultadoAdministrarBorradorEntidad();
				resultado.IdEntidad = borrador.P_OUT_REFID;
				resultado.IdInterno = 0L;
				salidaAdministrarBorrador.P_OUT_REFID.add(resultado);				
			}
		} else {
			MensajeSalida mensaje = new MensajeSalida();
			mensaje.tipo = PlataformaUtils.MensajeSalida.MENSAJE_ERROR;
			mensaje.codigo = "INDICADOR_NO_EXISTE";
			mensaje.descripcion = "INDICADOR_NO_EXISTE";
			mensaje.id = 0L;
			salidaAdministrarBorrador.P_OUT_MENSAJES.add(mensaje);
			salidaAdministrarBorrador.P_OUT_ERRORESCONTADOR++;
		}
		
		Long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearClienteFullWS.modificarBorradorIndicador", fin -inicio);
		return salidaAdministrarBorrador;
	}

	private Long contarErrores(SalidaCrearClienteFullWS salida) {
	    logger.debug("INICIO::CrearClienteFull.contarErrores");
	    long inicio = System.currentTimeMillis();
	    
	    Long errores = salida.P_OUT_ENTIDADCREADA.parallelStream().reduce(0L,
			    (sum, ent) -> sum += ent.Mensajes.stream().filter(m -> PlataformaUtils.MensajeSalida.MENSAJE_ERROR.equals(m.TipoMensaje)).count(), 
			    (sum1, sum2) -> sum1 + sum2);
	    long fin = System.currentTimeMillis();
	    logger.debug("FIN::CrearClienteFull.contarErrores", fin - inicio);
	    return errores;
	}
	
	private SalidaAdministrarBorrador crearBorradorRelacionPersona(Connection con, SocioNegocio socioNegocio, EntradaCrearClienteFullWS entrada, SalidaCrearBorradorEnrolamiento borrador, String agregaAdvertencia) throws Exception {
		logger.debug("INICIO::CrearClienteFull.crearBorradorRelacionPersona");
	    long inicio = System.currentTimeMillis();
		CrearBorradorRelacionPersona crearBorradorRelacionPersona = new CrearBorradorRelacionPersona();
		EntradaCrearBorradorRelacionPersona entradaCrearBorradorRelacionPersona = new EntradaCrearBorradorRelacionPersona();
		entradaCrearBorradorRelacionPersona.p_CodigoFormulario = obtenerCodigoFormulario("RELACIONES",borrador);
		entradaCrearBorradorRelacionPersona.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCrearBorradorRelacionPersona.p_IdModulo = ID_MODULO;
		entradaCrearBorradorRelacionPersona.p_IdTipoCanal = socioNegocio.IdTipoCanalIngresoCorredor.trim().isEmpty() ? ID_TIPO_CANAL : socioNegocio.IdTipoCanalIngresoCorredor;
		entradaCrearBorradorRelacionPersona.p_IdUsuario = entrada.p_IdUsuario;
		entradaCrearBorradorRelacionPersona.p_UsaReglasNegocio ="S";
		SalidaAdministrarBorrador salidaAdministrarBorrador = new SalidaAdministrarBorrador();
		if ( validarExistenPersonasRelacionadas(con, socioNegocio)) { // si existen los idsPersona
			
			SalidaServicioCrearPersona salidaServicioCrearPersona = crearPersonasRelacionadas(con, socioNegocio, entrada, agregaAdvertencia);
			if (salidaServicioCrearPersona.P_OUT_ERRORESCONTADOR == 0) {
			    entradaCrearBorradorRelacionPersona.p_RelacionesPersona = socioNegocio.RelacionesPersona.stream().map(relacion -> {
				cl.bcs.socionegocio.enr.pojo.RelacionPersona relacionBorrador = new cl.bcs.socionegocio.enr.pojo.RelacionPersona();
				relacionBorrador.IdEnrolamientoOrigen = borrador.P_OUT_REFID;
				relacionBorrador.IdPersonaDestino = relacion.IdPersonaRelacionada;
				relacionBorrador.IdTipoRelacion = relacion.IdTipoRelacion;
				relacionBorrador.DocumentoIdentidadRelacion = relacion.DocumentoIdentidad;
				relacionBorrador.TipoDocumentoIdentidadRelacion = relacion.TipoDocumentoIdentidad;
				relacionBorrador.NombresPersonaRelacion = relacion.Nombres;
				relacionBorrador.ApellidoPaternoPersonaRelacion = relacion.ApellidoPaterno;
				relacionBorrador.ApellidoMaternoPersonaRelacion = relacion.ApellidoMaterno;
				relacionBorrador.EstaInsertada = relacion.EstaInsertada;
				relacionBorrador.IdRelacionPersona = 0L;
				relacionBorrador.FechaInicioRelacion = "";
				relacionBorrador.FechaFinRelacion = "";
				relacionBorrador.PorcentajeParticipacion = new BigDecimal(0);
				relacionBorrador.RetiraDocumentos = "";
				relacionBorrador.FinalidadRelacion = "";
				relacionBorrador.CondicionesRelacion = "";
				relacionBorrador.IdGradoVinculacionParental = 0L;
				relacionBorrador.Ciudad = "";
				relacionBorrador.Calle = "";
				relacionBorrador.Numero = "";
				relacionBorrador.Telefono = "";
				relacionBorrador.Email = "";
				relacionBorrador.Complemento = "";
				
				if("CY".equals(relacion.IdTipoRelacion)) {
				    socioNegocio.IdPersonaConyuge = relacion.IdPersonaRelacionada;
				}
				return relacionBorrador;
			    }).filter(c -> !c.IdTipoRelacion.equals("") && !c.DocumentoIdentidadRelacion.equals("")).collect(Collectors.toList()); 
			    
			    if (entradaCrearBorradorRelacionPersona.p_RelacionesPersona.size() > 0)
			    {
			    	salidaAdministrarBorrador = crearBorradorRelacionPersona.logica(con, entradaCrearBorradorRelacionPersona);
			    }
			} else {
				AtomicLong mensajeId = new AtomicLong();
				
				if(agregaAdvertencia.equalsIgnoreCase("S")) {
					salidaAdministrarBorrador.P_OUT_MENSAJES
					.addAll(salidaServicioCrearPersona.P_OUT_ERRORESDETALLE.stream().map(errorDetalle -> {
						MensajeSalida mensaje = new MensajeSalida();
						mensaje.tipo = PlataformaUtils.MensajeSalida.MENSAJE_ADVERTENCIA;
						mensaje.codigo = "UI_ADVERTENCIA_PERSONA_RELACIONADA_EXISTE";
						mensaje.descripcion = "UI_ADVERTENCIA_PERSONA_RELACIONADA_EXISTE";
						mensaje.id = errorDetalle.IdEntidad;
						return mensaje;
					}).collect(Collectors.toList()));
				}else {
					salidaAdministrarBorrador.P_OUT_MENSAJES
					.addAll(salidaServicioCrearPersona.P_OUT_ERRORESDETALLE.stream().map(errorDetalle -> {
						MensajeSalida mensaje = new MensajeSalida();
						mensaje.tipo = PlataformaUtils.MensajeSalida.MENSAJE_ERROR;
						mensaje.codigo = errorDetalle.Codigo;
						mensaje.descripcion = errorDetalle.Descripcion;
						mensaje.id = mensajeId.getAndIncrement();
						return mensaje;
					}).collect(Collectors.toList()));
				}
			}
		} else {
			MensajeSalida mensaje = new MensajeSalida();
			mensaje.tipo = PlataformaUtils.MensajeSalida.MENSAJE_ERROR;
			mensaje.codigo = "PERSONA_RELACIONADA_NO_EXISTE";
			mensaje.descripcion = "PERSONA_RELACIONADA_NO_EXISTE";
			mensaje.id = 0L;
			salidaAdministrarBorrador.P_OUT_MENSAJES.add(mensaje);
		}
		salidaAdministrarBorrador.P_OUT_ERRORESCONTADOR = salidaAdministrarBorrador.P_OUT_MENSAJES.parallelStream()
			.filter( mensaje->PlataformaUtils.MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo)).count();
		Long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearClienteFull.crearBorradorRelacionPersona", fin - inicio);
		return salidaAdministrarBorrador;
}

	private Boolean validarExistenPersonasRelacionadas(Connection conn, SocioNegocio socioNegocio) throws SQLException {
    
		logger.debug("INICIO::CrearClienteFull.validarExistenPersonasRelacionadas");
		Long inicio = System.currentTimeMillis();
		
		List<Long> idPersonas = socioNegocio.RelacionesPersona.stream()
				.filter(relacion -> relacion.IdPersonaRelacionada > 0).map(relacion -> relacion.IdPersonaRelacionada)
				.collect(Collectors.toList());
		HashMap<String, Object> parametros = new HashMap<String, Object>(2);
		parametros.put("IdPersonas", idPersonas);
		SQL sql = new SQL();
		List<Id> idsPersonasValidas = sql.query(conn, SQL_SELECT_IDPERSONA, parametros, Id.class);
		Boolean valida = idPersonas.size() == idsPersonasValidas.size() ? true : false;
		
		Long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearClienteFull.crearBorradorRelacionPersona", fin - inicio);
		
		return valida;
	}

	private SalidaServicioCrearPersona crearPersonasRelacionadas(Connection conn, SocioNegocio socioNegocio,EntradaCrearClienteFullWS entrada, String agregaAdvertencia) throws Exception {
		logger.debug("INICIO::CrearClienteFull.crearPersonasRelacionadas");
		Long inicio = System.currentTimeMillis();
		
		SalidaServicioCrearPersona salidaCrearPersonasRelacionadas = new SalidaServicioCrearPersona();
		CrearPersona crearPersona = new CrearPersona();
		List<RelacionPersona> relaciones = socioNegocio.RelacionesPersona.parallelStream().filter(relacion->relacion.IdPersonaRelacionada.equals(0L) && !relacion.IdTipoRelacion.equals("") && !relacion.DocumentoIdentidad.equals("")).collect(Collectors.toList());
		for (RelacionPersona relacion : relaciones) {
			SQL sql = new SQL();
			HashMap<String, Object> documentos = new HashMap<String, Object>(2);
			documentos.put("DocumentosIdentidad", relacion.DocumentoIdentidad);
	
			Long idPersonaExiste = sql.query(conn, SQL_SELECT_IDPERSONA_DOCUMENTO_IDENTIDAD, documentos, IdPersonaConsulta.class).stream().findFirst().orElse(new IdPersonaConsulta()).IdPersona;
			
			if(idPersonaExiste > 0 && agregaAdvertencia.equals("N")) {
				salidaCrearPersonasRelacionadas = modificarPersonaRelacionada(conn, entrada, relacion, idPersonaExiste);
				relacion.IdPersonaRelacionada = salidaCrearPersonasRelacionadas.P_OUT_REFID;
				relacion.EstaInsertada = "S";
			} else {
				List<cl.bcs.socionegocio.pojo.Enrolamiento> enrolamiento = new ArrayList<>();
				cl.bcs.socionegocio.pojo.Enrolamiento personaCrear = new cl.bcs.socionegocio.pojo.Enrolamiento();
				personaCrear.IdTipoDocumentoIdentidad = relacion.TipoDocumentoIdentidad;
				personaCrear.DocumentoIdentidad = relacion.DocumentoIdentidad;
				personaCrear.Nombres = relacion.Nombres;
				personaCrear.ApellidoPaterno = relacion.ApellidoPaterno;
				personaCrear.ApellidoMaterno = relacion.ApellidoMaterno;
				personaCrear.IdTipoEntidad = "PERSONA";
				personaCrear.IdPaisNacimiento = 0L;
				personaCrear.Tipo = "N";
				personaCrear.IdEmpresa= entrada.p_IdEmpresa;
				personaCrear.IdTipoDocumentoIdentidadAsociado = "";
				enrolamiento.add(personaCrear);
		
				EntradaServicioCrearPersona entradaServicioCrearPersona = new EntradaServicioCrearPersona();
				entradaServicioCrearPersona.p_IdEmpresa = entrada.p_IdEmpresa;
				entradaServicioCrearPersona.p_IdUsuario = entrada.p_IdUsuario;
				entradaServicioCrearPersona.p_IdModulo = ID_MODULO;
				entradaServicioCrearPersona.p_ConsultarEnrolamiento = enrolamiento;
		
				SalidaServicioCrearPersona salidaServicioCrearPersona = crearPersona.logica(conn, entradaServicioCrearPersona);
				if (salidaServicioCrearPersona.P_OUT_ERRORESCONTADOR == 0) {
					relacion.IdPersonaRelacionada = salidaServicioCrearPersona.P_OUT_REFID;
					relacion.EstaInsertada = "S";
				} else {
					salidaCrearPersonasRelacionadas.P_OUT_ERRORESDETALLE
							.addAll(salidaServicioCrearPersona.P_OUT_ERRORESDETALLE);
					salidaCrearPersonasRelacionadas.P_OUT_ERRORESCONTADOR += salidaServicioCrearPersona.P_OUT_ERRORESCONTADOR;
		
					salidaCrearPersonasRelacionadas.P_OUT_ERRORESDETALLE.stream().forEach(err -> {
						err.IdEntidad = err.Codigo.equals("PERSONA_EXISTE") ? idPersonaExiste : 0L;
					});
				}
			}
		}
		
		Long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearClienteFull.crearPersonasRelacionadas", fin - inicio);
		
		return salidaCrearPersonasRelacionadas;
	}
	
	private SalidaServicioCrearPersona modificarPersonaRelacionada(Connection conn, EntradaCrearClienteFullWS in, RelacionPersona relacion, Long idPersonaExiste) throws Exception {
		logger.debug("INICIO::CrearClienteFull.modificarPersonaRelacionada");
		Long inicio = System.currentTimeMillis();
		
		SalidaServicioCrearPersona salidaModificarPersonasRelacionadas = new SalidaServicioCrearPersona();
		SQL sql = new SQL();
		HashMap<String, Object> documentos = new HashMap<String, Object>(2);
		documentos.put("DocumentosIdentidad", relacion.DocumentoIdentidad);
		
		RelacionPersona relacionadaBD = sql.query(conn, SQL_SELECT_RELACIONADA_POR_DOCUMENTO_IDENTIDAD, documentos, RelacionPersona.class).get(0);
		
		List<RegistroCambioMasivo> cambios = new ArrayList<RegistroCambioMasivo>();
		
		if(!relacionadaBD.Nombres.equals(relacion.Nombres) && !relacion.Nombres.isEmpty()) {
			RegistroCambioMasivo cambioNombres = new RegistroCambioMasivo();
			cambioNombres.CampoModificado = "Nombres";
			cambioNombres.IdEntidad = idPersonaExiste;
			cambioNombres.ValorNuevo = relacion.Nombres;
			cambios.add(cambioNombres);
		}
		if(!relacionadaBD.ApellidoPaterno.equals(relacion.ApellidoPaterno) && !relacion.ApellidoPaterno.isEmpty()) {
			RegistroCambioMasivo cambioPaterno = new RegistroCambioMasivo();
			cambioPaterno.CampoModificado = "ApellidoPaterno";
			cambioPaterno.IdEntidad = idPersonaExiste;
			cambioPaterno.ValorNuevo = relacion.ApellidoPaterno;
			cambios.add(cambioPaterno);
		}
		if(!relacionadaBD.ApellidoMaterno.equals(relacion.ApellidoMaterno) && !relacion.ApellidoMaterno.isEmpty()) {
			RegistroCambioMasivo cambioMaterno = new RegistroCambioMasivo();
			cambioMaterno.CampoModificado = "ApellidoMaterno";
			cambioMaterno.IdEntidad = idPersonaExiste;
			cambioMaterno.ValorNuevo = relacion.ApellidoMaterno;
			cambios.add(cambioMaterno);
		}
		
		if(!cambios.isEmpty()) {
			EntradaEjecutarCambiosPersona entradaEjecutarCambiosPersona = new EntradaEjecutarCambiosPersona();
			entradaEjecutarCambiosPersona.p_IdEmpresa = in.p_IdEmpresa;
			entradaEjecutarCambiosPersona.p_IdModulo = ID_MODULO;
			entradaEjecutarCambiosPersona.p_IdUsuario = in.p_IdUsuario;
			entradaEjecutarCambiosPersona.p_UsaReglasNegocio = "N";
			entradaEjecutarCambiosPersona.p_IdTipoCanal = "WBS";
			
			entradaEjecutarCambiosPersona.p_RegistroCambio.addAll(cambios);
			
			ModificarPersonaDatosGeneralesMasivo modificarPersonaDatosGeneralesMasivo =new ModificarPersonaDatosGeneralesMasivo();
			
			SalidaEjecutarCambiosPersona salidaEjecutarCambiosPersona = modificarPersonaDatosGeneralesMasivo.logica(conn, entradaEjecutarCambiosPersona);
			
			salidaModificarPersonasRelacionadas.P_OUT_ADVERTENCIASDETALLE = salidaEjecutarCambiosPersona.P_OUT_MENSAJES.stream().filter(msj -> msj.tipo.equals("ADVERTENCIA")).map(mensaje -> {
				AdvertenciaDetalle advertencia = new AdvertenciaDetalle();
				advertencia.Codigo = mensaje.codigo;
				advertencia.Descripcion = mensaje.descripcion;
				advertencia.RequiereAutorizacion = "S";
				return advertencia;
			}).collect(Collectors.toList());
			
			salidaModificarPersonasRelacionadas.P_OUT_ERRORESDETALLE = salidaEjecutarCambiosPersona.P_OUT_MENSAJES.stream().filter(msj -> msj.tipo.equals("ERROR")).map(mensaje -> {
				ErrorDetalle detalle = new ErrorDetalle();
				detalle.Codigo = mensaje.codigo;
				detalle.Descripcion = mensaje.descripcion;
				detalle.IdEntidad = idPersonaExiste;
				return detalle;
			}).collect(Collectors.toList());
			
			salidaModificarPersonasRelacionadas.P_OUT_ADVERTENCIASCONTADOR = salidaModificarPersonasRelacionadas.P_OUT_ADVERTENCIASDETALLE.size();
			salidaModificarPersonasRelacionadas.P_OUT_ERRORESCONTADOR = salidaModificarPersonasRelacionadas.P_OUT_ERRORESDETALLE.size();
		}
		salidaModificarPersonasRelacionadas.P_OUT_REFID = idPersonaExiste;
		
		Long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearClienteFull.modificarPersonaRelacionada", fin - inicio);
		return salidaModificarPersonasRelacionadas;
	}

	private SalidaAdministrarBorrador crearBorradorCuentaCustodioPortafolio(Connection con, SocioNegocio socioNegocio,EntradaCrearClienteFullWS entrada, SalidaCrearBorradorEnrolamiento borrador) throws Exception {
		logger.debug("INICIO::CrearClienteFull.crearBorradorCuentaCustodioPortafolio");
		Long inicio = System.currentTimeMillis();
		
		CrearBorradorCuentaCustodioPortafolio crearBorradorCuentaCustodioPortafolio = new CrearBorradorCuentaCustodioPortafolio();
		EntradaCrearBorradorCuentaCustodioPortafolio entradaCrearBorradorCuentaCustodioPortafolio = new EntradaCrearBorradorCuentaCustodioPortafolio();		
		
		AtomicLong cuentaCustodioIdInterno = new AtomicLong();
		List<cl.bcs.socionegocio.enr.pojo.CuentaCustodioPortafolio> cuentasCustodio = socioNegocio.DatosCliente.DatosPortafolio.CuentasCustodio.stream().map(cuentaCustodio -> {
			cl.bcs.socionegocio.enr.pojo.CuentaCustodioPortafolio cuentaCustodioEnrolamiento = new cl.bcs.socionegocio.enr.pojo.CuentaCustodioPortafolio();
			cuentaCustodioEnrolamiento.IdInterno = cuentaCustodioIdInterno.getAndIncrement();
			cuentaCustodioEnrolamiento.IdEnrolamiento = borrador.P_OUT_REFID;
			cuentaCustodioEnrolamiento.Codigo = cuentaCustodio.Codigo;
			cuentaCustodioEnrolamiento.IdBoveda = cuentaCustodio.IdBoveda;
			cuentaCustodioEnrolamiento.IdTipoCuentaCustodia = cuentaCustodio.IdTipoCuentaCustodio;
			cuentaCustodioEnrolamiento.LiquidaVencimientos = obtenerValorCheckbox(cuentaCustodio.LiquidaVen) ;
			cuentaCustodioEnrolamiento.LiquidaCupones = obtenerValorCheckbox(cuentaCustodio.LiquidaCup);
			cuentaCustodioEnrolamiento.LiquidaPrepagos = obtenerValorCheckbox(cuentaCustodio.LiquidaPre);
			cuentaCustodioEnrolamiento.LiquidaDividendos = obtenerValorCheckbox(cuentaCustodio.LiquidaDiv);
			return cuentaCustodioEnrolamiento;
		}).collect(Collectors.toList());
		
		entradaCrearBorradorCuentaCustodioPortafolio.p_CodigoFormulario = obtenerCodigoFormulario("CUENTA_CUSTODIO",borrador);
		entradaCrearBorradorCuentaCustodioPortafolio.p_CuentasCustodioPortafolio.addAll(cuentasCustodio);
		entradaCrearBorradorCuentaCustodioPortafolio.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCrearBorradorCuentaCustodioPortafolio.p_IdModulo = ID_MODULO;
		entradaCrearBorradorCuentaCustodioPortafolio.p_IdTipoCanal = socioNegocio.IdTipoCanalIngresoCorredor.trim().isEmpty() ? ID_TIPO_CANAL : socioNegocio.IdTipoCanalIngresoCorredor;
		entradaCrearBorradorCuentaCustodioPortafolio.p_IdUsuario = entrada.p_IdUsuario;
		entradaCrearBorradorCuentaCustodioPortafolio.p_UsaReglasNegocio = USA_REGLAS_NEGOCIO;
		
		SalidaAdministrarBorrador salidaAdministrarBorrador = new SalidaAdministrarBorrador();
		
		salidaAdministrarBorrador = crearBorradorCuentaCustodioPortafolio.logica(con, entradaCrearBorradorCuentaCustodioPortafolio);
		
		Long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearClienteFull.crearBorradorCuentaCustodioPortafolio", fin - inicio);
		
		return salidaAdministrarBorrador;
	}

	private SalidaAdministrarBorrador modificarBorradorClienteDatosGenerales(Connection con, SocioNegocio socioNegocio,EntradaCrearClienteFullWS entrada, SalidaCrearBorradorEnrolamiento borrador) throws Exception {
		
		logger.debug("INICIO::CrearClienteFull.modificarBorradorClienteDatosGenerales");
		Long inicio = System.currentTimeMillis();
		
		ModificarBorradorClienteDatosGenerales modificarBorradorClienteDatosGenerales = new ModificarBorradorClienteDatosGenerales();
		EntradaModificarBorradorClienteDatosGenerales entradaModificarBorradorClienteDatosGenerales = new EntradaModificarBorradorClienteDatosGenerales();
		AtomicLong idInternoCliente = new AtomicLong();
		
		cl.bcs.socionegocio.enr.pojo.ClienteDatosGenerales datosClienteEnrolamiento = new cl.bcs.socionegocio.enr.pojo.ClienteDatosGenerales();
		DatosCliente datosCliente = socioNegocio.DatosCliente;
		
		datosClienteEnrolamiento.IdInterno = idInternoCliente.getAndIncrement();
		datosClienteEnrolamiento.IdEnrolamiento = borrador.P_OUT_REFID;
		datosClienteEnrolamiento.IdTipoCliente = datosCliente.IdTipoCliente;
		datosClienteEnrolamiento.IdCategoriaInversionista = datosCliente.IdCategoriaInversionista;
		datosClienteEnrolamiento.IdSegmentoCliente = datosCliente.IdSegmentoCliente;
		datosClienteEnrolamiento.IdEmpleadoEjecutivo = datosCliente.CodigoEjecutivo;
		datosClienteEnrolamiento.CodigoEjecutivoExterno = datosCliente.CodigoEjecutivoExterno;
		datosClienteEnrolamiento.IdSucursalPreferencia = datosCliente.Sucursal;
		datosClienteEnrolamiento.EsClienteFinal = obtenerValorCheckbox(datosCliente.ClienteFinal);
		datosClienteEnrolamiento.CodigoArea = datosCliente.AreaCliente;
		
		entradaModificarBorradorClienteDatosGenerales.p_CodigoFormulario = obtenerCodigoFormulario("DATOS_GENERALES_CLIENTE", borrador);
		entradaModificarBorradorClienteDatosGenerales.p_ClientesDatosGenerales.add(datosClienteEnrolamiento);
		entradaModificarBorradorClienteDatosGenerales.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaModificarBorradorClienteDatosGenerales.p_IdModulo = ID_MODULO;
		entradaModificarBorradorClienteDatosGenerales.p_IdTipoCanal = socioNegocio.IdTipoCanalIngresoCorredor.trim().isEmpty() ? ID_TIPO_CANAL : socioNegocio.IdTipoCanalIngresoCorredor;
		entradaModificarBorradorClienteDatosGenerales.p_IdUsuario = entrada.p_IdUsuario;
		entradaModificarBorradorClienteDatosGenerales.p_UsaReglasNegocio = USA_REGLAS_NEGOCIO;
		
		SalidaAdministrarBorrador salidaAdministrarBorrador = new SalidaAdministrarBorrador();
		
		salidaAdministrarBorrador = modificarBorradorClienteDatosGenerales.logica(con, entradaModificarBorradorClienteDatosGenerales);
		
		Long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearClienteFull.modificarBorradorClienteDatosGenerales", fin - inicio);
		
		return salidaAdministrarBorrador;
	}

	private SalidaAdministrarBorrador crearBorradorFatcaPaisResidencia(Connection con, SocioNegocio socioNegocio,EntradaCrearClienteFullWS entrada, SalidaCrearBorradorEnrolamiento borrador) throws Exception {
		
		logger.debug("INICIO::CrearClienteFull.crearBorradorFatcaPaisResidencia");
		Long inicio = System.currentTimeMillis();
		
		CrearBorradorFatcaPaisResidencia crearBorradorFatcaPaisResidencia = new CrearBorradorFatcaPaisResidencia();
		EntradaCrearBorradorFatcaPaisResidencia entradaCrearBorradorFatcaPaisResidencia = new EntradaCrearBorradorFatcaPaisResidencia();

		List<cl.bcs.socionegocio.enr.pojo.FatcaPaisResidencia> fatcas = socioNegocio.FatcaCrs.PaisesResidencia
				.stream().map(fatca -> {
					cl.bcs.socionegocio.enr.pojo.FatcaPaisResidencia fatcaEnrolamiento = new cl.bcs.socionegocio.enr.pojo.FatcaPaisResidencia();
					fatcaEnrolamiento.IdEnrolamiento = borrador.P_OUT_REFID;
					fatcaEnrolamiento.IdPaisResidencia = fatca.IdPais;
					fatcaEnrolamiento.TIN = fatca.Tin;
					fatcaEnrolamiento.IdCausaNoTin = fatca.NoTin;
					fatcaEnrolamiento.Observacion = fatca.OtraCausa;
					fatcaEnrolamiento.PaisResidenciaPrincipal = "";
					fatcaEnrolamiento.TieneTin = obtenerTieneTin(fatca.Tin);
					return fatcaEnrolamiento;
				}).collect(Collectors.toList());
		
		entradaCrearBorradorFatcaPaisResidencia.p_FatcaPaisResidencia.addAll(fatcas);
		entradaCrearBorradorFatcaPaisResidencia.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCrearBorradorFatcaPaisResidencia.p_CodigoFormulario = obtenerCodigoFormulario("FATCA_PAIS_RESIDENCIA",borrador);
		entradaCrearBorradorFatcaPaisResidencia.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCrearBorradorFatcaPaisResidencia.p_IdModulo = ID_MODULO;
		entradaCrearBorradorFatcaPaisResidencia.p_IdTipoCanal = socioNegocio.IdTipoCanalIngresoCorredor.trim().isEmpty() ? ID_TIPO_CANAL : socioNegocio.IdTipoCanalIngresoCorredor;
		entradaCrearBorradorFatcaPaisResidencia.p_IdUsuario = entrada.p_IdUsuario;
		entradaCrearBorradorFatcaPaisResidencia.p_UsaReglasNegocio = USA_REGLAS_NEGOCIO;
		SalidaAdministrarBorrador salidaAdministrarBorrador = new SalidaAdministrarBorrador();
		
		salidaAdministrarBorrador = crearBorradorFatcaPaisResidencia.logica(con, entradaCrearBorradorFatcaPaisResidencia);
		
		Long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearClienteFull.crearBorradorFatcaPaisResidencia", fin - inicio);
		
		return salidaAdministrarBorrador;
	}

	private String obtenerTieneTin(String tin) {
	    if(!"".equals(tin) && tin!=null ) {
		return "S";		
	    }
	    return "N";
	}

	private SalidaAdministrarBorrador modificarBorradorFatca(Connection con, SocioNegocio socioNegocio, EntradaCrearClienteFullWS entrada, SalidaCrearBorradorEnrolamiento borrador) throws Exception {
		
		logger.debug("INICIO::CrearClienteFull.modificarBorradorFatca");
		Long inicio = System.currentTimeMillis();
		
		ModificarBorradorFatca modificarBorradorFatca = new ModificarBorradorFatca();
		EntradaModificarBorradorFatca entradaModificarBorradorFatca = new EntradaModificarBorradorFatca();
		AtomicLong fatcaIdInterno = new AtomicLong();
		cl.bcs.socionegocio.enr.pojo.Fatca fatcaEnrolamiento = new cl.bcs.socionegocio.enr.pojo.Fatca();

		FatcaCrs fatca = socioNegocio.FatcaCrs;
		fatcaEnrolamiento.IdInterno = fatcaIdInterno.getAndIncrement();
		fatcaEnrolamiento.IdEnrolamiento = borrador.P_OUT_REFID;
		fatcaEnrolamiento.IdClasificacionFatca = fatca.IdClasificacionFatca;
		fatcaEnrolamiento.IdClasificacionEmpresaFatca = fatca.IdClasificacionEmpresaFatca;
		fatcaEnrolamiento.NumeroGIIN = fatca.NumeroGIIN;
		fatcaEnrolamiento.FechaFirmaFormulario = fatca.FechaFirmaFormulario.isEmpty()? Fecha.toDateTime("0001-01-01") : Fecha.toDateTime(fatca.FechaFirmaFormulario);
		fatcaEnrolamiento.AutorizacionInformarOrganismo =obtenerValorCheckbox(fatca.AutorizacionInformarOrganismo);
		fatcaEnrolamiento.ExcepcionResidente = obtenerValorCheckbox(fatca.ExcepcionResidente);
		fatcaEnrolamiento.ExcepcionNacimiento = obtenerValorCheckbox(fatca.ExcepcionNacimiento);
		fatcaEnrolamiento.ExcepcionDomicilio = obtenerValorCheckbox(fatca.ExcepcionDomicilio);
		fatcaEnrolamiento.ExcepcionTelefono = obtenerValorCheckbox(fatca.ExcepcionTelefono);
		fatcaEnrolamiento.ExcepcionOrdenPermanente = obtenerValorCheckbox(fatca.ExcepcionOrdenPermanente);
		fatcaEnrolamiento.ExcepcionFirma = obtenerValorCheckbox(fatca.ExcepcionFirma);
		fatcaEnrolamiento.ParticipacionSociedadesUSA = obtenerValorCheckbox(fatca.ParticipacionSociedadesUSA);
		fatcaEnrolamiento.OrdenPermanente = obtenerValorCheckbox(fatca.OrdenPermanente);
		fatcaEnrolamiento.NACEEUU =  fatca.NACEEUU;
		fatcaEnrolamiento.NACEEUUExplicacion = fatca.NACEEUUExplicacion;
		fatcaEnrolamiento.ObligacionesFiscales = fatca.ObligacionesFiscales;
		fatcaEnrolamiento.ObligacionesFiscalesExplicacion = fatca.ObligacionesFiscalesExplicacion;
	
		entradaModificarBorradorFatca.p_CodigoFormulario = obtenerCodigoFormulario("FATCA",borrador);
		entradaModificarBorradorFatca.p_Fatcas.add(fatcaEnrolamiento);
		entradaModificarBorradorFatca.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaModificarBorradorFatca.p_IdModulo = ID_MODULO;
		entradaModificarBorradorFatca.p_IdTipoCanal = socioNegocio.IdTipoCanalIngresoCorredor.trim().isEmpty() ? ID_TIPO_CANAL : socioNegocio.IdTipoCanalIngresoCorredor;
		entradaModificarBorradorFatca.p_IdUsuario = entrada.p_IdUsuario;
		entradaModificarBorradorFatca.p_UsaReglasNegocio = USA_REGLAS_NEGOCIO;
		
		SalidaAdministrarBorrador salidaAdministrarBorrador = new SalidaAdministrarBorrador();
		
		salidaAdministrarBorrador = modificarBorradorFatca.logica(con, entradaModificarBorradorFatca);
		
		Long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearClienteFull.modificarBorradorFatca", fin - inicio);
		
		return salidaAdministrarBorrador;
		
	}

	private SalidaAdministrarBorrador modificarBorradorPersonaDatosGenerales(Connection con, SocioNegocio socioNegocio,EntradaCrearClienteFullWS entrada, SalidaCrearBorradorEnrolamiento borrador) throws Exception {
		
		logger.debug("INICIO::CrearClienteFull.modificarBorradorPersonaDatosGenerales");
		Long inicio = System.currentTimeMillis();
		
		ModificarBorradorPersonaDatosGenerales modificarBorradorPersonaDatosGenerales = new ModificarBorradorPersonaDatosGenerales();
		EntradaModificarBorradorPersonaDatosGenerales entradaModificarBorradorPersonaDatosGenerales = new EntradaModificarBorradorPersonaDatosGenerales();
		AtomicLong borradorPersonaIdInterno = new AtomicLong();
		List<cl.bcs.socionegocio.enr.pojo.PersonaDatosGenerales> personas = new ArrayList<>();
		cl.bcs.socionegocio.enr.pojo.PersonaDatosGenerales personaEnrolamiento = new cl.bcs.socionegocio.enr.pojo.PersonaDatosGenerales();
		personaEnrolamiento.IdInterno = borradorPersonaIdInterno.getAndIncrement();
		personaEnrolamiento.IdEnrolamiento = borrador.P_OUT_REFID;
		personaEnrolamiento.Nombres = socioNegocio.Nombres;
		personaEnrolamiento.ApellidoPaterno = socioNegocio.ApellidoPaterno;
		personaEnrolamiento.ApellidoMaterno = socioNegocio.ApellidoMaterno;
		personaEnrolamiento.RazonSocial = socioNegocio.RazonSocial;
		personaEnrolamiento.IdGiroPrincipal = socioNegocio.IdGiro;
		personaEnrolamiento.IdPaisNacimiento = socioNegocio.IdPaisNacimientoConstitucion;
		personaEnrolamiento.IdPaisResidencia = socioNegocio.IdPaisResidenciaCasaMatriz;
		personaEnrolamiento.FechaNacimiento = socioNegocio.FechaNacimientoConstitucion.isEmpty() ? Fecha.toDate("0001-01-01") : Fecha.toDate(socioNegocio.FechaNacimientoConstitucion);
		personaEnrolamiento.Sexo = socioNegocio.IdGenero;
		personaEnrolamiento.EstadoCivil = socioNegocio.IdEstadoCivil;
		personaEnrolamiento.IdTransparenciaFiscal = obtenerTransparenciaFiscal(con, socioNegocio.IdPaisResidenciaCasaMatriz);
		personaEnrolamiento.RegimenConyugal = socioNegocio.IdRegimenConyugal;
		personaEnrolamiento.IdProfesion = socioNegocio.IdProfesionActividad;
		personaEnrolamiento.FechaInicioActividad = socioNegocio.InicioActividad.isEmpty()? Fecha.toDateTime("0001-01-01") : Fecha.toDateTime(socioNegocio.InicioActividad);
		personaEnrolamiento.IdPaisNacionalidadPrincipal = socioNegocio.IdPaisNacionalidadPrincipal;
		personaEnrolamiento.GlobalLegalEntity = socioNegocio.GlobalLegal;
		personaEnrolamiento.IdPersonaEmpleador = socioNegocio.Empleador.IdEmpleador;
		personaEnrolamiento.CodigoTipoCargoEmpleado = socioNegocio.Cargo;
		personaEnrolamiento.DetalleCargo = socioNegocio.DetalleCargo;
		personaEnrolamiento.IdRangoPatrimonio = socioNegocio.RangoPatrimonio;
		personaEnrolamiento.CodigoRangoRenta = "N".equalsIgnoreCase(socioNegocio.IdTipoPersona) ? socioNegocio.CodigoRangoRenta : "";
		personaEnrolamiento.CodigoRangoNivelVenta = "J".equalsIgnoreCase(socioNegocio.IdTipoPersona) ? socioNegocio.CodigoRangoNivelVenta : "";
		personaEnrolamiento.IdPersonaConyuge = socioNegocio.IdPersonaConyuge;
		personaEnrolamiento.ResidenciaTributariaExtranjera = obtenerValorCheckbox(socioNegocio.ResidenciaTriburariaFueraChile);
		personaEnrolamiento.IdSituacionLaboral = socioNegocio.IdSituacionLaboral;
		personaEnrolamiento.IdTipoCanalIngresoCorredor = socioNegocio.IdTipoCanalIngresoCorredor;
		personaEnrolamiento.CodigoRangoRenta = "N".equalsIgnoreCase(socioNegocio.IdTipoPersona) ? socioNegocio.CodigoRangoRenta : "";
		personaEnrolamiento.CodigoRangoNivelVenta = "J".equalsIgnoreCase(socioNegocio.IdTipoPersona) ? socioNegocio.CodigoRangoNivelVenta : "";
		personas.add(personaEnrolamiento);
		
		entradaModificarBorradorPersonaDatosGenerales.p_CodigoFormulario = obtenerCodigoFormulario("DATOS_GENERALES_PERSONA",borrador);
		entradaModificarBorradorPersonaDatosGenerales.p_PersonaDatosGenerales.addAll(personas);
		entradaModificarBorradorPersonaDatosGenerales.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaModificarBorradorPersonaDatosGenerales.p_IdModulo = ID_MODULO;
		entradaModificarBorradorPersonaDatosGenerales.p_IdTipoCanal = socioNegocio.IdTipoCanalIngresoCorredor.trim().isEmpty() ? ID_TIPO_CANAL : socioNegocio.IdTipoCanalIngresoCorredor;
		entradaModificarBorradorPersonaDatosGenerales.p_IdUsuario = entrada.p_IdUsuario;
		entradaModificarBorradorPersonaDatosGenerales.p_UsaReglasNegocio = USA_REGLAS_NEGOCIO;
		
		SalidaAdministrarBorrador salidaAdministrarBorrador = new SalidaAdministrarBorrador();
		
		salidaAdministrarBorrador = modificarBorradorPersonaDatosGenerales.logica(con, entradaModificarBorradorPersonaDatosGenerales);
		
		Long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearClienteFull.modificarBorradorPersonaDatosGenerales", fin - inicio);
		
		return salidaAdministrarBorrador;
	}

	private String obtenerValorCheckbox(String campo) {
	    if(campo != null) {
		 campo=campo.toUpperCase();
	    }
	   
	    if(campo == null || "".equals(campo) || "N".equals(campo) || campo.startsWith("N")) 
		return "N";
	    return "S";
	}

	private EntidadCreada crearSalidaMensajesErroresCrearClienteFull(SocioNegocio socioNegocio,SalidaCrearClienteFull salida, EntidadCreada entidadCreada) {
		
		logger.debug("INICIO::CrearClienteFull.crearSalidaMensajesErroresCrearClienteFull");
		Long inicio = System.currentTimeMillis();
		
		AtomicLong contadorMensajes = new AtomicLong();
		List<Mensaje> mensajes = salida.P_OUT_MENSAJES.stream().map(mensaje -> {
			Mensaje mensajeWS = new Mensaje();
			mensajeWS.Codigo = mensaje.codigo;
			mensajeWS.TipoMensaje = mensaje.tipo;
			mensajeWS.Mensaje = mensaje.descripcion;
			mensajeWS.IdInterno = contadorMensajes.getAndIncrement();
			return mensajeWS;
		}).collect(Collectors.toList());
		
		entidadCreada.Mensajes.addAll(mensajes);
		
		mensajes = salida.P_OUT_REQUISITOS_ENR_FULL.stream().map(mensaje -> {
			Mensaje mensajeWS = new Mensaje();
			mensajeWS.Codigo = "CAMPO_REQUERIDO";
			mensajeWS.Mensaje = mensaje.Descripcion;
			mensajeWS.IdInterno = contadorMensajes.incrementAndGet();
			mensajeWS.TipoMensaje = PlataformaUtils.MensajeSalida.MENSAJE_ERROR;
			return mensajeWS;
		}).collect(Collectors.toList());
		
		entidadCreada.Mensajes.addAll(mensajes);
		
		Long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearClienteFull.crearSalidaMensajesErroresCrearClienteFull", fin - inicio);
		
		return entidadCreada;
	}

	private SalidaAdministrarBorrador crearBorradorTarjetaBancaria(Connection con, SocioNegocio socioNegocio,EntradaCrearClienteFullWS entrada, SalidaCrearBorradorEnrolamiento borrador) throws Exception {
		
		logger.debug("INICIO::CrearClienteFull.crearBorradorTarjetaBancaria");
		Long inicio = System.currentTimeMillis();
		
		CrearBorradorTarjetaBancaria crearBorradorTarjetaBancaria = new CrearBorradorTarjetaBancaria();
		EntradaCrearBorradorTarjetaBancaria entradaCrearBorradorTarjetaBancaria = new EntradaCrearBorradorTarjetaBancaria();
		AtomicLong tarjetaIdInterno = new AtomicLong();
		List<cl.bcs.socionegocio.enr.pojo.TarjetaBancaria> tarjetas = socioNegocio.TarjetasBancarias.stream().map(tarjeta -> {
			cl.bcs.socionegocio.enr.pojo.TarjetaBancaria tarjetaEnrolamiento = new cl.bcs.socionegocio.enr.pojo.TarjetaBancaria();
			tarjetaEnrolamiento.IdInterno = tarjetaIdInterno.getAndIncrement();
			tarjetaEnrolamiento.IdEnrolamiento = borrador.P_OUT_REFID;
			tarjetaEnrolamiento.IdBanco = tarjeta.IdBanco;
			tarjetaEnrolamiento.IdTipoTarjetaBancaria = tarjeta.IdTipo;
			tarjetaEnrolamiento.NumeroTarjetaBancaria = tarjeta.NumeroTarjeta;
			tarjetaEnrolamiento.CodigoVerificacion = tarjeta.CodigoVerificacion;
			tarjetaEnrolamiento.FechaVencimiento = tarjeta.FechaVencimiento;
			return tarjetaEnrolamiento;
		}).collect(Collectors.toList());
		
		entradaCrearBorradorTarjetaBancaria.p_CodigoFormulario = obtenerCodigoFormulario("TARJETA_BANCARIA",borrador);
		entradaCrearBorradorTarjetaBancaria.p_TarjetasBancarias.addAll(tarjetas);
		entradaCrearBorradorTarjetaBancaria.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCrearBorradorTarjetaBancaria.p_IdModulo = ID_MODULO;
		entradaCrearBorradorTarjetaBancaria.p_IdTipoCanal = socioNegocio.IdTipoCanalIngresoCorredor.trim().isEmpty() ? ID_TIPO_CANAL : socioNegocio.IdTipoCanalIngresoCorredor;
		entradaCrearBorradorTarjetaBancaria.p_IdUsuario = entrada.p_IdUsuario;
		entradaCrearBorradorTarjetaBancaria.p_UsaReglasNegocio = USA_REGLAS_NEGOCIO;
		
		SalidaAdministrarBorrador salidaAdministrarBorrador = new SalidaAdministrarBorrador();
		
		salidaAdministrarBorrador = crearBorradorTarjetaBancaria.logica(con, entradaCrearBorradorTarjetaBancaria);
		
		Long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearClienteFull.crearBorradorTarjetaBancaria", fin - inicio);
		
		return salidaAdministrarBorrador;
	}

	private SalidaAdministrarBorrador crearBorradorTelefono(Connection con, SocioNegocio socioNegocio, List<Telefono> telefonosNoPredeterminados, EntradaCrearClienteFullWS entrada, SalidaCrearBorradorEnrolamiento borrador) throws Exception {
		
		logger.debug("INICIO::CrearClienteFull.crearBorradorTelefono");
		Long inicio = System.currentTimeMillis();
		
		CrearBorradorTelefono crearBorradorTelefono = new CrearBorradorTelefono();
		EntradaCrearBorradorTelefono entradaCrearBorradorTelefono = new EntradaCrearBorradorTelefono();
		AtomicLong telefonoIdInterno = new AtomicLong();
		List<cl.bcs.socionegocio.enr.pojo.Telefono> telefonos = telefonosNoPredeterminados.stream().map(telefono -> {
			cl.bcs.socionegocio.enr.pojo.Telefono telefonoEnrolamiento = new cl.bcs.socionegocio.enr.pojo.Telefono();
			telefonoEnrolamiento.IdInterno = telefonoIdInterno.getAndIncrement();
			telefonoEnrolamiento.IdEnrolamiento = borrador.P_OUT_REFID;
			telefonoEnrolamiento.IdPais = telefono.IdPais;
			telefonoEnrolamiento.IdTipoTelefono = telefono.Tipo;
			telefonoEnrolamiento.Numero = telefono.Numero;
			telefonoEnrolamiento.Anexo = telefono.Anexo;
			telefonoEnrolamiento.CodigoProposito = telefono.IdProposito;
			telefonoEnrolamiento.Comentario = telefono.Comentario;
			telefonoEnrolamiento.Predeterminado = telefono.Predeterminado ;
			return telefonoEnrolamiento;
		}).collect(Collectors.toList());
		
		entradaCrearBorradorTelefono.p_CodigoFormulario = obtenerCodigoFormulario("TELEFONO",borrador);
		entradaCrearBorradorTelefono.p_Telefonos.addAll(telefonos);
		entradaCrearBorradorTelefono.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCrearBorradorTelefono.p_IdModulo = ID_MODULO;
		entradaCrearBorradorTelefono.p_IdTipoCanal = socioNegocio.IdTipoCanalIngresoCorredor.trim().isEmpty() ? ID_TIPO_CANAL : socioNegocio.IdTipoCanalIngresoCorredor;
		entradaCrearBorradorTelefono.p_IdUsuario = entrada.p_IdUsuario;
		entradaCrearBorradorTelefono.p_UsaReglasNegocio = USA_REGLAS_NEGOCIO;
		
		SalidaAdministrarBorrador salidaAdministrarBorrador = new SalidaAdministrarBorrador();
		
		salidaAdministrarBorrador = crearBorradorTelefono.logica(con, entradaCrearBorradorTelefono);
		
		Long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearClienteFull.crearBorradorTelefono", fin - inicio);
		
		return salidaAdministrarBorrador;
	}

	private SalidaAdministrarBorrador crearBorradorTipoDocumento(Connection conn, SocioNegocio socioNegocio,EntradaCrearClienteFullWS entrada, SalidaCrearBorradorEnrolamiento borrador) throws Exception {
		
		logger.debug("INICIO::CrearClienteFull.crearBorradorTipoDocumento");
		Long inicio = System.currentTimeMillis();
		
		CrearBorradorDocumentoIdentidad crearBorradorDocumentoIdentidad = new CrearBorradorDocumentoIdentidad();
		EntradaCrearBorradorDocumentoIdentidad entradaCrearBorradorDocumentoIdentidad = new EntradaCrearBorradorDocumentoIdentidad();
		AtomicLong documentoIdentidadIdInterno = new AtomicLong();
		List<cl.bcs.socionegocio.enr.pojo.DocumentoIdentidad> documentos = socioNegocio.TiposDocumento.stream().map(documento -> {
			cl.bcs.socionegocio.enr.pojo.DocumentoIdentidad documentoIdentidadEnrolamiento = new cl.bcs.socionegocio.enr.pojo.DocumentoIdentidad();
			documentoIdentidadEnrolamiento.IdInterno = documentoIdentidadIdInterno.getAndIncrement();
			documentoIdentidadEnrolamiento.IdEnrolamiento = borrador.P_OUT_REFID;
			documentoIdentidadEnrolamiento.IdTipoDocumentoIdentidad = documento.TipoDocumento;
			documentoIdentidadEnrolamiento.DocumentoIdentidad = documento.NumeroDocumento;
			documentoIdentidadEnrolamiento.NumeroSerie = documento.NumeroSerie;
			documentoIdentidadEnrolamiento.FechaEmision = documento.FechaEmision;
			documentoIdentidadEnrolamiento.FechaVencimiento = documento.FechaVencimiento;
			return documentoIdentidadEnrolamiento;
		}).collect(Collectors.toList());
		
		entradaCrearBorradorDocumentoIdentidad.p_CodigoFormulario = obtenerCodigoFormulario("DOCUMENTO_IDENTIDAD",borrador);
		entradaCrearBorradorDocumentoIdentidad.p_DocumentosIdentidades.addAll(documentos);
		entradaCrearBorradorDocumentoIdentidad.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCrearBorradorDocumentoIdentidad.p_IdModulo = ID_MODULO;
		entradaCrearBorradorDocumentoIdentidad.p_IdTipoCanal = socioNegocio.IdTipoCanalIngresoCorredor.trim().isEmpty() ? ID_TIPO_CANAL : socioNegocio.IdTipoCanalIngresoCorredor;
		entradaCrearBorradorDocumentoIdentidad.p_IdUsuario = entrada.p_IdUsuario;
		entradaCrearBorradorDocumentoIdentidad.p_UsaReglasNegocio = USA_REGLAS_NEGOCIO;
		
		SalidaAdministrarBorrador salidaAdministrarBorrador = new SalidaAdministrarBorrador();
		
		salidaAdministrarBorrador = crearBorradorDocumentoIdentidad.logica(conn, entradaCrearBorradorDocumentoIdentidad);
		
		Long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearClienteFull.crearBorradorTipoDocumento", fin - inicio);
		
		return salidaAdministrarBorrador;
	}
	
	private SalidaAdministrarBorrador modificarBorradorTipoDocumento(Connection conn, SocioNegocio socioNegocio, TipoDocumento documento,EntradaCrearClienteFullWS entrada, SalidaCrearBorradorEnrolamiento borrador) throws Exception {
		
		logger.debug("INICIO::CrearClienteFull.modificarBorradorTipoDocumento");
		Long inicio = System.currentTimeMillis();
		
		ModificarBorradorDocumentoIdentidad modificarBorradorDocumentoIdentidad = new ModificarBorradorDocumentoIdentidad();
		EntradaModificarBorradorDocumentoIdentidad entradaModificarBorradorDocumentoIdentidad = new EntradaModificarBorradorDocumentoIdentidad();
		cl.bcs.socionegocio.enr.pojo.DocumentoIdentidad documentoIdentidadEnrolamiento = new cl.bcs.socionegocio.enr.pojo.DocumentoIdentidad();;
		// buscar documento identidad
		HashMap<String, Object> parametros = new HashMap<String, Object>(2);
		parametros.put("NumeroDocumento", documento.NumeroDocumento);
		SQL sql = new SQL();
		List<Id> idsDoc = sql.query(conn, SQL_SELECT_DOCUMENTO_IDENTIDAD, parametros, Id.class);
	
		if(!idsDoc.isEmpty()) {
		    documentoIdentidadEnrolamiento.IdDocumentoIdentidad = idsDoc.get(0).Id;
		    documentoIdentidadEnrolamiento.IdInterno = 0L;
		    documentoIdentidadEnrolamiento.IdEnrolamiento = borrador.P_OUT_REFID;
		    documentoIdentidadEnrolamiento.IdTipoDocumentoIdentidad = documento.TipoDocumento;
		    documentoIdentidadEnrolamiento.DocumentoIdentidad = documento.NumeroDocumento;
		    documentoIdentidadEnrolamiento.NumeroSerie = documento.NumeroSerie;
		    documentoIdentidadEnrolamiento.FechaEmision = documento.FechaEmision;
		    documentoIdentidadEnrolamiento.FechaVencimiento = documento.FechaVencimiento;
			
		    entradaModificarBorradorDocumentoIdentidad.p_CodigoFormulario = obtenerCodigoFormulario("DOCUMENTO_IDENTIDAD",borrador);
		    entradaModificarBorradorDocumentoIdentidad.p_DocumentosIdentidades.add(documentoIdentidadEnrolamiento);
		    entradaModificarBorradorDocumentoIdentidad.p_IdEmpresa = entrada.p_IdEmpresa;
		    entradaModificarBorradorDocumentoIdentidad.p_IdModulo = ID_MODULO;
		    entradaModificarBorradorDocumentoIdentidad.p_IdTipoCanal = socioNegocio.IdTipoCanalIngresoCorredor.trim().isEmpty() ? ID_TIPO_CANAL : socioNegocio.IdTipoCanalIngresoCorredor;
		    entradaModificarBorradorDocumentoIdentidad.p_IdUsuario = entrada.p_IdUsuario;
		    entradaModificarBorradorDocumentoIdentidad.p_UsaReglasNegocio = USA_REGLAS_NEGOCIO;
		}

		SalidaAdministrarBorrador salidaAdministrarBorrador = new SalidaAdministrarBorrador();
		salidaAdministrarBorrador = modificarBorradorDocumentoIdentidad.logica(conn, entradaModificarBorradorDocumentoIdentidad);
		
		Long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearClienteFull.modificarBorradorTipoDocumento", fin - inicio);
		
		return salidaAdministrarBorrador;
	}

	private EntidadCreada crearSalidaMensajeAdministrarBorrador(SocioNegocio socioNegocio,SalidaAdministrarBorrador salidaAdministrarBorrador, EntidadCreada entidadCreada) {
		AtomicLong contadorMensajes = new AtomicLong();
		List<Mensaje> mensajes = salidaAdministrarBorrador.P_OUT_MENSAJES.stream().map(mensaje -> {
			Mensaje mensajeWS = new Mensaje();
			mensajeWS.Codigo = mensaje.codigo;
			mensajeWS.TipoMensaje = mensaje.tipo;
			mensajeWS.Mensaje = mensaje.descripcion;
			mensajeWS.IdInterno = contadorMensajes.getAndIncrement();
			return mensajeWS;
		}).collect(Collectors.toList());
		entidadCreada.Mensajes.addAll(mensajes);
		entidadCreada.IdEntidad = salidaAdministrarBorrador.P_OUT_REFID.parallelStream().findFirst().orElse(null) != null ? salidaAdministrarBorrador.P_OUT_REFID.parallelStream().findFirst().get().IdEntidad : 0;
		return entidadCreada;
	}

	private SalidaAdministrarBorrador crearBorradorCuentaBancaria(Connection conn, SocioNegocio socioNegocio, EntradaCrearClienteFullWS entrada, SalidaCrearBorradorEnrolamiento borrador) throws Exception {
		
		logger.debug("INICIO::CrearClienteFull.crearBorradorCuentaBancaria");
		Long inicio = System.currentTimeMillis();
		
		SalidaAdministrarBorrador salidaAdministrarBorrador = new SalidaAdministrarBorrador();
		CrearBorradorCuentaBancaria crearBorradorCuentaBancaria = new CrearBorradorCuentaBancaria();
		EntradaCrearBorradorCuentaBancaria entradaCrearBorradorCuentaBancaria = new EntradaCrearBorradorCuentaBancaria();
		AtomicLong cuentaIdInterno = new AtomicLong();
		
		long cantidadPredeterminadas = socioNegocio.CuentasBancarias.stream().filter(c -> "S".equals(c.Predeterminado)).count();
		
		if (cantidadPredeterminadas > 1) {
			HashMap<String, String> parametrosMensaje = new HashMap<>();
			List<String> numerosCuentasDuplicadas = socioNegocio.CuentasBancarias.stream().filter(c -> "S".equals(c.Predeterminado)).map(c -> c.NumeroCuentaBancaria).collect(Collectors.toList());
			salidaAdministrarBorrador.P_OUT_ERRORESCONTADOR += 1 ;
			for (String CtaBancaria : numerosCuentasDuplicadas) {
				parametrosMensaje.put("Cuenta Duplicada n:" , CtaBancaria);
			}
			
			salidaAdministrarBorrador.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ERROR, "ERROR_MULTIPLES_CUENTAS_BANCARIAS_PREDETERMINADAS", parametrosMensaje, conn, entrada.p_IdEmpresa) );
			return salidaAdministrarBorrador;
		}
			
		List<cl.bcs.socionegocio.enr.pojo.CuentaBancaria> cuentasBancarias = socioNegocio.CuentasBancarias.stream().map(cuentaBancaria -> {
			cl.bcs.socionegocio.enr.pojo.CuentaBancaria cuentaBancariaEnrolamiento = new cl.bcs.socionegocio.enr.pojo.CuentaBancaria();
			cuentaBancariaEnrolamiento.IdInterno = cuentaIdInterno.getAndIncrement();
			cuentaBancariaEnrolamiento.IdEnrolamiento = borrador.P_OUT_REFID;
			cuentaBancariaEnrolamiento.IdCuentaBancaria = cuentaBancaria.IdCuentaBancaria;
			cuentaBancariaEnrolamiento.IdCuentaBancariaPersona = cuentaBancaria.IdCuentaBancaria;
			cuentaBancariaEnrolamiento.IdBanco = cuentaBancaria.IdBanco;
			cuentaBancariaEnrolamiento.IdTipoCuentaBancaria = cuentaBancaria.CodigoTipoCuentaBancaria;
			cuentaBancariaEnrolamiento.NumeroCuentaBancaria = cuentaBancaria.NumeroCuentaBancaria;
			cuentaBancariaEnrolamiento.IdMoneda = cuentaBancaria.IdMoneda;
			cuentaBancariaEnrolamiento.IdPlaza = cuentaBancaria.CodigoPlaza;
			cuentaBancariaEnrolamiento.EsPac = obtenerValorCheckbox(cuentaBancaria.PAC);
			cuentaBancariaEnrolamiento.Predeterminado = cuentaBancaria.Predeterminado;
			cuentaBancariaEnrolamiento.CrearPreferenciaPago = obtenerValorCheckbox(cuentaBancaria.ActualizaMedioPago);
			cuentaBancariaEnrolamiento.CodigoCuentaInterbancario = "";
			cuentaBancariaEnrolamiento.CodigoAba = "";
			cuentaBancariaEnrolamiento.OtroCodigoBancoExtranjero = "";
			cuentaBancariaEnrolamiento.OtroCodigoCuentaExtranjero = "";
			cuentaBancariaEnrolamiento.IdPersonaRelacionada = 0L;
			return cuentaBancariaEnrolamiento;
		}).collect(Collectors.toList());

	
		entradaCrearBorradorCuentaBancaria.p_CodigoFormulario = obtenerCodigoFormulario("CUENTA_BANCARIA",borrador);
		entradaCrearBorradorCuentaBancaria.p_CuentasBancarias.addAll(cuentasBancarias);
		entradaCrearBorradorCuentaBancaria.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCrearBorradorCuentaBancaria.p_IdModulo = ID_MODULO;
		entradaCrearBorradorCuentaBancaria.p_IdTipoCanal = socioNegocio.IdTipoCanalIngresoCorredor.trim().isEmpty() ? ID_TIPO_CANAL : socioNegocio.IdTipoCanalIngresoCorredor;
		entradaCrearBorradorCuentaBancaria.p_IdUsuario = entrada.p_IdUsuario;
		entradaCrearBorradorCuentaBancaria.p_UsaReglasNegocio = USA_REGLAS_NEGOCIO;
		
		
		salidaAdministrarBorrador = crearBorradorCuentaBancaria.logica(conn, entradaCrearBorradorCuentaBancaria);
		
		Long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearClienteFull.crearBorradorCuentaBancaria", fin - inicio);
		
		return salidaAdministrarBorrador;
	}

	private EntidadCreada crearSalidaMensajesErrores(SocioNegocio socioNegocio, SalidaCrearBorradorEnrolamiento salidaCrearBorradorEnrolamiento, EntidadCreada entidadCreada) {		
		
		logger.debug("INICIO::CrearClienteFull.crearSalidaMensajesErrores");
		Long inicio = System.currentTimeMillis();
		
		AtomicLong contadorMensajes = new AtomicLong();
		List<Mensaje> mensajes = salidaCrearBorradorEnrolamiento.P_OUT_MENSAJES.stream().map(mensaje -> {
			Mensaje mensajeWS = new Mensaje();
			mensajeWS.Codigo = mensaje.codigo;
			mensajeWS.IdInterno = contadorMensajes.getAndIncrement();
			mensajeWS.Mensaje = mensaje.descripcion;
			mensajeWS.TipoMensaje = mensaje.tipo;
			return mensajeWS;
		}).collect(Collectors.toList());
		
		entidadCreada.TipoEntidad = ID_TIPO_ENTIDAD;
		entidadCreada.IdInterno = socioNegocio.IdInterno;
		entidadCreada.IdEntidad = 0L;
		entidadCreada.Mensajes.addAll(mensajes);
		
		Long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearClienteFull.crearSalidaMensajesErrores", fin - inicio);
		
		return entidadCreada;
	}

	private SalidaCrearBorradorEnrolamiento crearBorradorEnrolamiento(Connection con, SocioNegocio socioNegocio, EntradaCrearClienteFullWS entrada) throws Exception {
				
		logger.debug("INICIO::CrearClienteFull.crearBorradorEnrolamiento");
		Long inicio = System.currentTimeMillis();
		
		CrearBorradorEnrolamiento crearBorradorEnrolamiento = new CrearBorradorEnrolamiento();
		EntradaCrearBorradorEnrolamiento entradaCrearBorrador = new EntradaCrearBorradorEnrolamiento();
		SalidaCrearBorradorEnrolamiento salidaCrearBorradorEnrolamiento = new SalidaCrearBorradorEnrolamiento();
		
		entradaCrearBorrador.p_IdUsuario = entrada.p_IdUsuario;
		entradaCrearBorrador.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCrearBorrador.p_IdModulo = ID_MODULO;
		entradaCrearBorrador.p_IdTipoCanal = socioNegocio.IdTipoCanalIngresoCorredor.trim().isEmpty() ? ID_TIPO_CANAL : socioNegocio.IdTipoCanalIngresoCorredor;
		entradaCrearBorrador.p_UsaReglasNegocio = USA_REGLAS_NEGOCIO;
		entradaCrearBorrador.p_CodigoFormulario = obtenerCodigoFormulario(socioNegocio.IdTipoPersona);
		entradaCrearBorrador.p_ConsultarEnrolamiento = new ArrayList<>();
		
		BorradorEnrolamiento borradorEnrolamiento = new BorradorEnrolamiento();

		borradorEnrolamiento.IdEnrolamiento = 0L;
		borradorEnrolamiento.IdEmpresa = entrada.p_IdEmpresa;
		borradorEnrolamiento.IdUsuario = entrada.p_IdUsuario;
		borradorEnrolamiento.IdTipoEntidad = ID_TIPO_ENTIDAD;
		borradorEnrolamiento.Tipo = socioNegocio.IdTipoPersona;
		borradorEnrolamiento.TipoEnrolamiento = TIPO_ENROLAMIENTO;
		borradorEnrolamiento.Nombres = socioNegocio.Nombres;
		borradorEnrolamiento.ApellidoPaterno = socioNegocio.ApellidoPaterno;
		borradorEnrolamiento.ApellidoMaterno = socioNegocio.ApellidoMaterno;
		borradorEnrolamiento.RazonSocial = socioNegocio.RazonSocial;
		borradorEnrolamiento.Sexo = socioNegocio.IdGenero;
		borradorEnrolamiento.EstadoCivil = socioNegocio.IdEstadoCivil;
		borradorEnrolamiento.RegimenConyugal = socioNegocio.IdRegimenConyugal;
		borradorEnrolamiento.IdPersonaConyuge = socioNegocio.IdPersonaConyuge;
		borradorEnrolamiento.FechaNacimiento = socioNegocio.FechaNacimientoConstitucion.isEmpty()? Fecha.toDate("0001-01-01") : Fecha.toDate(socioNegocio.FechaNacimientoConstitucion);
		borradorEnrolamiento.IdPaisResidencia = socioNegocio.IdPaisResidenciaCasaMatriz;

		borradorEnrolamiento.IdPaisNacimiento = socioNegocio.IdPaisNacimientoConstitucion;
		borradorEnrolamiento.DocumentoIdentidad = socioNegocio.TiposDocumento.get(0).NumeroDocumento;
		borradorEnrolamiento.IdTipoDocumentoIdentidad = socioNegocio.TiposDocumento.get(0).TipoDocumento;
		borradorEnrolamiento.POR_IdPerfilComercial = socioNegocio.DatosCliente.IdPerfilComercial;
		borradorEnrolamiento.CLI_IdCategoriaInversionista = socioNegocio.DatosCliente.IdCategoriaInversionista;
		borradorEnrolamiento.CLI_AreaCliente = socioNegocio.DatosCliente.AreaCliente;
		borradorEnrolamiento.CLI_IdTipoClienteCMF = socioNegocio.DatosCliente.TipoClienteCMF;
		borradorEnrolamiento.CLI_IdEmpleadoEjecutivo = socioNegocio.DatosCliente.CodigoEjecutivo;
		borradorEnrolamiento.CLI_IdTipoCliente = socioNegocio.DatosCliente.IdTipoCliente;
		borradorEnrolamiento.IdTipoCanalIngresoCorredor = socioNegocio.IdTipoCanalIngresoCorredor;
		
		borradorEnrolamiento.IdEntidadEnrolada = 0L;
		borradorEnrolamiento.DocumentoIdentidadAdicional = "";
		borradorEnrolamiento.IdTipoDocumentoIdentidadAdicional = "";
		borradorEnrolamiento.IdTipoDocumentoIdentidadAsociado = "";
		borradorEnrolamiento.EMP_FechaInicioCargo = "0001-01-01";
		borradorEnrolamiento.EMP_FechaTerminoCargo = "0001-01-01"; 
		if(socioNegocio.IdPersona > 0) {
			borradorEnrolamiento.DireccionCalle = !socioNegocio.DireccionesFisicas.isEmpty() && socioNegocio.DireccionesFisicas.parallelStream().filter(dir -> dir.Predeterminado.equals("S") && !dir.IdDireccionFisica.equals(0L)).count() >= 1  ? socioNegocio.DireccionesFisicas.stream().filter(dir -> dir.Predeterminado.equals("S") && !dir.IdDireccionFisica.equals(0L)).findFirst().get().Calle : "";
			borradorEnrolamiento.DireccionNumero = !socioNegocio.DireccionesFisicas.isEmpty() && socioNegocio.DireccionesFisicas.parallelStream().filter(dir -> dir.Predeterminado.equals("S") && !dir.IdDireccionFisica.equals(0L)).count() >= 1  ? socioNegocio.DireccionesFisicas.stream().filter(dir -> dir.Predeterminado.equals("S") && !dir.IdDireccionFisica.equals(0L)).findFirst().get().Numero : "";
			borradorEnrolamiento.DireccionDepartamento = !socioNegocio.DireccionesFisicas.isEmpty() && socioNegocio.DireccionesFisicas.parallelStream().filter(dir -> dir.Predeterminado.equals("S") && !dir.IdDireccionFisica.equals(0L)).count() >= 1  ? socioNegocio.DireccionesFisicas.stream().filter(dir -> dir.Predeterminado.equals("S") && !dir.IdDireccionFisica.equals(0L)).findFirst().get().Departamento : "";
			borradorEnrolamiento.IdCiudad = !socioNegocio.DireccionesFisicas.isEmpty() && socioNegocio.DireccionesFisicas.parallelStream().filter(dir -> dir.Predeterminado.equals("S") && !dir.IdDireccionFisica.equals(0L)).count() >= 1  ? socioNegocio.DireccionesFisicas.stream().filter(dir -> dir.Predeterminado.equals("S") && !dir.IdDireccionFisica.equals(0L)).findFirst().get().IdCiudad : 0L;
			borradorEnrolamiento.IdDivisionPais = !socioNegocio.DireccionesFisicas.isEmpty() && socioNegocio.DireccionesFisicas.parallelStream().filter(dir -> dir.Predeterminado.equals("S") && !dir.IdDireccionFisica.equals(0L)).count() >= 1  ? socioNegocio.DireccionesFisicas.stream().filter(dir -> dir.Predeterminado.equals("S") && !dir.IdDireccionFisica.equals(0L)).findFirst().get().IdComuna : 0L;
			borradorEnrolamiento.Email = !socioNegocio.DireccionesElectronicas.isEmpty() && socioNegocio.DireccionesElectronicas.parallelStream().filter(dir -> dir.Predeterminado.equals("S") && !dir.IdDireccionElectronica.equals(0L)).count() >= 1  ? socioNegocio.DireccionesElectronicas.stream().filter(dir -> dir.Predeterminado.equals("S") && !dir.IdDireccionElectronica.equals(0L)).findFirst().get().Direccion :   "";
			borradorEnrolamiento.IdTipoTelefono = !socioNegocio.Telefonos.isEmpty() && socioNegocio.Telefonos.parallelStream().filter(tel -> tel.Predeterminado.equals("S") && !tel.IdTelefono.equals(0L)).count() >= 1  ? socioNegocio.Telefonos.stream().filter(tel -> tel.Predeterminado.equals("S") && !tel.IdTelefono.equals(0L)).findFirst().get().Tipo : "";
			borradorEnrolamiento.Telefono = !socioNegocio.Telefonos.isEmpty() && socioNegocio.Telefonos.parallelStream().filter(tel -> tel.Predeterminado.equals("S") && !tel.IdTelefono.equals(0L)).count() >= 1 ? socioNegocio.Telefonos.stream().filter(tel -> tel.Predeterminado.equals("S") && !tel.IdTelefono.equals(0L)).findFirst().get().Numero : "";
		}else {
			borradorEnrolamiento.DireccionCalle = !socioNegocio.DireccionesFisicas.isEmpty() && socioNegocio.DireccionesFisicas.parallelStream().filter(dir -> dir.Predeterminado.equals("S")).count() >= 1  ? socioNegocio.DireccionesFisicas.stream().filter(dir -> dir.Predeterminado.equals("S")).findFirst().get().Calle : "";
			borradorEnrolamiento.DireccionNumero = !socioNegocio.DireccionesFisicas.isEmpty() && socioNegocio.DireccionesFisicas.parallelStream().filter(dir -> dir.Predeterminado.equals("S")).count() >= 1  ? socioNegocio.DireccionesFisicas.stream().filter(dir -> dir.Predeterminado.equals("S")).findFirst().get().Numero : "";
			borradorEnrolamiento.DireccionDepartamento = !socioNegocio.DireccionesFisicas.isEmpty() && socioNegocio.DireccionesFisicas.parallelStream().filter(dir -> dir.Predeterminado.equals("S")).count() >= 1  ? socioNegocio.DireccionesFisicas.stream().filter(dir -> dir.Predeterminado.equals("S")).findFirst().get().Departamento : "";
			borradorEnrolamiento.IdCiudad = !socioNegocio.DireccionesFisicas.isEmpty() && socioNegocio.DireccionesFisicas.parallelStream().filter(dir -> dir.Predeterminado.equals("S")).count() >= 1  ? socioNegocio.DireccionesFisicas.stream().filter(dir -> dir.Predeterminado.equals("S")).findFirst().get().IdCiudad : 0L;
			borradorEnrolamiento.IdDivisionPais = !socioNegocio.DireccionesFisicas.isEmpty() && socioNegocio.DireccionesFisicas.parallelStream().filter(dir -> dir.Predeterminado.equals("S")).count() >= 1  ? socioNegocio.DireccionesFisicas.stream().filter(dir -> dir.Predeterminado.equals("S")).findFirst().get().IdComuna : 0L;
			borradorEnrolamiento.Email = !socioNegocio.DireccionesElectronicas.isEmpty() && socioNegocio.DireccionesElectronicas.parallelStream().filter(dir -> dir.Predeterminado.equals("S")).count() >= 1  ? socioNegocio.DireccionesElectronicas.stream().filter(dir -> dir.Predeterminado.equals("S")).findFirst().get().Direccion :   "";
			borradorEnrolamiento.IdTipoTelefono = !socioNegocio.Telefonos.isEmpty() && socioNegocio.Telefonos.parallelStream().filter(tel -> tel.Predeterminado.equals("S")).count() >= 1  ? socioNegocio.Telefonos.stream().filter(tel -> tel.Predeterminado.equals("S")).findFirst().get().Tipo : "";
			borradorEnrolamiento.Telefono = !socioNegocio.Telefonos.isEmpty() && socioNegocio.Telefonos.parallelStream().filter(tel -> tel.Predeterminado.equals("S")).count() >= 1 ? socioNegocio.Telefonos.stream().filter(tel -> tel.Predeterminado.equals("S")).findFirst().get().Numero : "";
		}
		borradorEnrolamiento.CLI_CodigoAdicionalHolding = "";
		borradorEnrolamiento.CLI_IdClaseCliente = "";
		borradorEnrolamiento.CNT_Banco = "";
		borradorEnrolamiento.CNT_Bolsa = "";
		borradorEnrolamiento.CNT_Boveda = "";
		borradorEnrolamiento.CNT_CamaraDeCompensacion = "";
		borradorEnrolamiento.CNT_CodigoInscripcion = "";
		borradorEnrolamiento.CNT_Corredora = "";
		borradorEnrolamiento.CNT_Depositos = "";
		borradorEnrolamiento.CNT_Emisor = "";
		borradorEnrolamiento.CNT_Otros = "";
		borradorEnrolamiento.EMP_CodigoEmpleado = "";
		borradorEnrolamiento.EMP_CodigoTipoCargoEmpleado = "";
		borradorEnrolamiento.EMP_IdEmpleadoSupervisor = 0L;
		borradorEnrolamiento.PRO_IdAreaProveedor = 0L;
		borradorEnrolamiento.EMP_CargoIndefinido = "";
		borradorEnrolamiento.CodigoRangoRenta = "N".equalsIgnoreCase(socioNegocio.IdTipoPersona) ? socioNegocio.CodigoRangoRenta : "";
		borradorEnrolamiento.CodigoRangoNivelVenta = "J".equalsIgnoreCase(socioNegocio.IdTipoPersona) ? socioNegocio.CodigoRangoNivelVenta : "";
		borradorEnrolamiento.CodigoPropositoTransaccion = "N".equals(borradorEnrolamiento.Tipo) ? socioNegocio.CodigoPropositoTransaccion : "";
		borradorEnrolamiento.POR_IdEmpleadoEjecutivoComercial= socioNegocio.DatosCliente.DatosPortafolio.IdEjecutivoComercial != 0L ||socioNegocio.DatosCliente.DatosPortafolio.IdEjecutivoComercial != null ? socioNegocio.DatosCliente.DatosPortafolio.IdEjecutivoComercial: socioNegocio.DatosCliente.CodigoEjecutivo != null? socioNegocio.DatosCliente.CodigoEjecutivo:0L ;
		
		entradaCrearBorrador.p_ConsultarEnrolamiento.add(borradorEnrolamiento);
		
		salidaCrearBorradorEnrolamiento = crearBorradorEnrolamiento.logica(con, entradaCrearBorrador);
		
		Long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearClienteFull.crearBorradorEnrolamiento", fin - inicio);

		return salidaCrearBorradorEnrolamiento;

	}
	
	private SalidaAdministrarBorrador crearBorradorDireccionElectronica(Connection con, SocioNegocio socioNegocio, List<DireccionElectronica> direccionesElectronicas, EntradaCrearClienteFullWS entrada,SalidaCrearBorradorEnrolamiento borrador) throws Exception {
		
		logger.debug("INICIO::CrearClienteFull.crearBorradorDireccionElectronica");
		Long inicio = System.currentTimeMillis();
		
		CrearBorradorDireccionElectronica crearBorradorDireccionElectronica = new CrearBorradorDireccionElectronica();
		EntradaCrearBorradorDireccionElectronica entradaCrearBorradorDireccionElectronica = new EntradaCrearBorradorDireccionElectronica();
		AtomicLong direccionIdInterno = new AtomicLong();
		List<cl.bcs.socionegocio.enr.pojo.DireccionElectronica> direcciones = direccionesElectronicas.stream().map(direccion -> {
			cl.bcs.socionegocio.enr.pojo.DireccionElectronica direccionEnrolamiento = new cl.bcs.socionegocio.enr.pojo.DireccionElectronica();
			direccionEnrolamiento.IdInterno = direccionIdInterno.getAndIncrement();
			direccionEnrolamiento.IdEnrolamiento = borrador.P_OUT_REFID;
			direccionEnrolamiento.Direccion = direccion.Direccion;
			direccionEnrolamiento.CodigoProposito = direccion.IdProposito;
			direccionEnrolamiento.Comentario = direccion.Comentario;
			direccionEnrolamiento.Predeterminado = obtenerValorCheckbox(direccion.Predeterminado);
			return direccionEnrolamiento;
		}).collect(Collectors.toList());
	
		entradaCrearBorradorDireccionElectronica.p_CodigoFormulario = obtenerCodigoFormulario("DIRECCION_ELECTRONICA", borrador);
		entradaCrearBorradorDireccionElectronica.p_DireccionesElectronicas.addAll(direcciones);
		entradaCrearBorradorDireccionElectronica.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCrearBorradorDireccionElectronica.p_IdModulo = ID_MODULO;
		entradaCrearBorradorDireccionElectronica.p_IdTipoCanal = socioNegocio.IdTipoCanalIngresoCorredor.trim().isEmpty() ? ID_TIPO_CANAL : socioNegocio.IdTipoCanalIngresoCorredor;
		entradaCrearBorradorDireccionElectronica.p_IdUsuario = entrada.p_IdUsuario;
		entradaCrearBorradorDireccionElectronica.p_UsaReglasNegocio = USA_REGLAS_NEGOCIO;
		
		SalidaAdministrarBorrador salidaAdministrarBorrador = new SalidaAdministrarBorrador();
		
		salidaAdministrarBorrador = crearBorradorDireccionElectronica.logica(con, entradaCrearBorradorDireccionElectronica);
		
		Long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearClienteFull.crearBorradorDireccionElectronica", fin - inicio);
		
		return salidaAdministrarBorrador;
	}

	private SalidaAdministrarBorrador crearBorradorDireccionFisica(Connection con, SocioNegocio socioNegocio, List<DireccionFisica> direccionesFisicas, EntradaCrearClienteFullWS entrada,SalidaCrearBorradorEnrolamiento borrador) throws Exception {
		
		logger.debug("INICIO::CrearClienteFull.crearBorradorDireccionFisica");
		Long inicio = System.currentTimeMillis();
		
		CrearBorradorDireccionFisica crearBorradorDireccionFisica = new CrearBorradorDireccionFisica();
		EntradaCrearBorradorDireccionFisica entradaCrearBorradorDireccionFisica = new EntradaCrearBorradorDireccionFisica();
		AtomicLong direccionIdInterno = new AtomicLong();
		Map<Long,String> usaDivisionPaisPorCiudad = obtenerUsaDivisionPorCiudad(con,direccionesFisicas);
		List<cl.bcs.socionegocio.enr.pojo.DireccionFisica> direcciones = direccionesFisicas.stream().map(direccion -> {
			cl.bcs.socionegocio.enr.pojo.DireccionFisica direccionEnrolamiento = new cl.bcs.socionegocio.enr.pojo.DireccionFisica();
			direccionEnrolamiento.IdInterno = direccionIdInterno.getAndIncrement();
			direccionEnrolamiento.IdEnrolamiento = borrador.P_OUT_REFID;
			direccionEnrolamiento.CodigoProposito = direccion.IdProposito;
			direccionEnrolamiento.IdDireccionFisica = 0L;
			direccionEnrolamiento.Sector = direccion.Sector;
			direccionEnrolamiento.CodigoPostal = direccion.CodigoPostal;
			direccionEnrolamiento.IdDivisionPais = direccion.IdComuna;
			direccionEnrolamiento.IdCiudad = direccion.IdCiudad;
			direccionEnrolamiento.Calle = direccion.Calle;
			direccionEnrolamiento.Numero = direccion.Numero;
			direccionEnrolamiento.Departamento = direccion.Departamento;
			direccionEnrolamiento.Comentario = direccion.Comentario;
			direccionEnrolamiento.Predeterminado = obtenerValorCheckbox(direccion.Predeterminado);
			direccionEnrolamiento.CodigoDivisionPais = direccion.CodigoUnicoTerritorial;
			direccionEnrolamiento.CodigoCompuesto = direccion.CodigoUnicoTerritorial;
			direccionEnrolamiento.UsaDivisionPais = usaDivisionPaisPorCiudad.get(direccion.IdCiudad);
			direccionEnrolamiento.IdDireccionFisicaPersona = direccion.IdDireccionFisica;
			return direccionEnrolamiento;
		}).collect(Collectors.toList());
	
		entradaCrearBorradorDireccionFisica.p_CodigoFormulario = obtenerCodigoFormulario("DIRECCION_FISICA", borrador);
		entradaCrearBorradorDireccionFisica.p_DireccionesFisicas.addAll(direcciones);
		entradaCrearBorradorDireccionFisica.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCrearBorradorDireccionFisica.p_IdModulo = ID_MODULO;
		entradaCrearBorradorDireccionFisica.p_IdTipoCanal = socioNegocio.IdTipoCanalIngresoCorredor.trim().isEmpty() ? ID_TIPO_CANAL : socioNegocio.IdTipoCanalIngresoCorredor;
		entradaCrearBorradorDireccionFisica.p_IdUsuario = entrada.p_IdUsuario;
		entradaCrearBorradorDireccionFisica.p_UsaReglasNegocio = USA_REGLAS_NEGOCIO;
		
		SalidaAdministrarBorrador salidaAdministrarBorrador = new SalidaAdministrarBorrador();
		
		salidaAdministrarBorrador = crearBorradorDireccionFisica.logica(con, entradaCrearBorradorDireccionFisica);
		
		Long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearClienteFull.crearBorradorDireccionFisica", fin - inicio);
		
		return salidaAdministrarBorrador;
	}

	private Map<Long, String> obtenerUsaDivisionPorCiudad(Connection con, List<DireccionFisica> direccionesFisicas) throws SQLException {
	    logger.debug("INICIO::CrearClienteFullWS.obtenerUsaDivisionPorCiudad");
		Long inicio = System.currentTimeMillis();
		List<Long> idsCiudades = direccionesFisicas.parallelStream().filter(dir-> dir.IdCiudad>0L).map(dir-> dir.IdCiudad).distinct().collect(Collectors.toList());

		SQL sql = new SQL();
		HashMap<String, Object> parametros = new HashMap<>();
		parametros.put("IdCiudad", idsCiudades);
		Map<Long, String> usaDivisionPaisPorCiudad = new HashMap<Long, String>();
		
		if(!idsCiudades.isEmpty()) {
		    List<DivisionPais> direcciones = sql.query(con, SQL_SELECT_USA_DIVISION_PAIS, parametros, DivisionPais.class);		
		    usaDivisionPaisPorCiudad= direcciones.parallelStream().collect(Collectors.toMap(div->div.IdCiudad,div->div.UsaDivisionPais));
		}else {
		    usaDivisionPaisPorCiudad.put(0L, "N");
		}
		
		
		Long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearClienteFullWS.obtenerUsaDivisionPorCiudad", fin - inicio);
		
	    return usaDivisionPaisPorCiudad;
	}

	private SalidaCrearClienteFull crearClienteFull(Connection con, SocioNegocio socioNegocio, EntradaCrearClienteFullWS entrada, SalidaCrearBorradorEnrolamiento borrador) throws Exception {
		
		logger.debug("INICIO::CrearClienteFullWS.crearClienteFull");
		Long inicio = System.currentTimeMillis();
		
		CrearClienteFull crearClienteFull = new CrearClienteFull();
		CrearClienteFull.EntradaCrearClienteFull entradaFull = new CrearClienteFull.EntradaCrearClienteFull();
		
		List<CrearClienteFull.BorradorEnrolamiento> enrolamientos = new ArrayList<>();
		CrearClienteFull.BorradorEnrolamiento borradorEnrolamiento = new CrearClienteFull.BorradorEnrolamiento();
		borradorEnrolamiento.IdInterno = socioNegocio.IdInterno;
		borradorEnrolamiento.IdEnrolamiento = borrador.P_OUT_REFID;
		enrolamientos.add(borradorEnrolamiento);
		
		entradaFull.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaFull.p_IdUsuario = entrada.p_IdUsuario;
		entradaFull.p_IdModulo = ID_MODULO;
		entradaFull.p_IdTipoCanalIngreso = socioNegocio.IdTipoCanalIngresoCorredor.trim().isEmpty() ? ID_TIPO_CANAL : socioNegocio.IdTipoCanalIngresoCorredor;
		entradaFull.p_UsaReglasNegocio = USA_REGLAS_NEGOCIO;
		entradaFull.p_Enrolamientos = enrolamientos;
		if(socioNegocio.DatosCliente.FirmaAutomaticaDocumento.equalsIgnoreCase("S") && socioNegocio.IdTipoPersona.equalsIgnoreCase("N")) {
			entradaFull.p_FirmaAutomaticaDocumento = "S";
		}else {
			entradaFull.p_FirmaAutomaticaDocumento = "N";
		}
		
		SalidaCrearClienteFull salidaCrearClienteFull = new SalidaCrearClienteFull();
		salidaCrearClienteFull = crearClienteFull.logica(con, entradaFull);
		
		Long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearClienteFullWS.crearClienteFull", fin - inicio);
		
		return salidaCrearClienteFull;
	}
	
	private String obtenerCodigoFormulario(String string, SalidaCrearBorradorEnrolamiento borrador) {
		return borrador.P_FORMULARIOS_SOCIO_NEGOCIO.parallelStream()
					.filter(form -> string.equals(form.Formulario)).findFirst().get().CodigoFormulario;
	}

	private EntidadCreada crearSalidaClienteExiste(SocioNegocio socioNegocio, EntidadCreada entidadCreada) {		
		entidadCreada.TipoEntidad = ID_TIPO_ENTIDAD;
		entidadCreada.IdInterno = socioNegocio.IdInterno;
		entidadCreada.IdEntidad = 0L;
		Mensaje mensaje = new Mensaje();
		mensaje.IdInterno = Long.valueOf(entidadCreada.Mensajes.size());
		mensaje.TipoMensaje = PlataformaUtils.MensajeSalida.MENSAJE_ERROR;
		mensaje.Codigo = "CLIENTE_EXISTE";
		mensaje.Mensaje = "CLIENTE_EXISTE";
		entidadCreada.Mensajes.add(mensaje);
		
		return entidadCreada;
	}

	private Boolean existeCliente(SalidaConsultarEntidadEnrolamiento salidaConsultarEntidadEnrolamiento) {
		return "S".equals(salidaConsultarEntidadEnrolamiento.P_OUT_ENTIDADEXISTE) && "S".equals(salidaConsultarEntidadEnrolamiento.P_OUT_PERSONAEXISTE);
	}

	private String obtenerCodigoFormulario(String tipoPersona) {
		return "J".equals(tipoPersona) ? "INGRESAR_CLIENTE_FULL_JURIDICA" : "INGRESAR_CLIENTE_FULL_NATURAL";
	}
	
	private SalidaConsultarEntidadEnrolamiento consultarEntidadEnrolamiento(Connection conn,SocioNegocio socioNegocio, EntradaCrearClienteFullWS entrada) throws Exception{
		logger.debug("INICIO::CrearClienteFullWS.consultarEntidadEnrolamiento");
		Long inicio = System.currentTimeMillis();
		
		SalidaConsultarEntidadEnrolamiento salidaConsultarEntidadEnrolamiento = new SalidaConsultarEntidadEnrolamiento();
		ConsultarEntidadEnrolamiento consultarEntidadEnrolamiento = new ConsultarEntidadEnrolamiento();
		EntradaConsultarEntidadEnrolamiento entradaConsultarEntidadEnrolamiento = new EntradaConsultarEntidadEnrolamiento();
		entradaConsultarEntidadEnrolamiento.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaConsultarEntidadEnrolamiento.p_IdUsuario = entrada.p_IdUsuario;
		entradaConsultarEntidadEnrolamiento.p_IdModulo = ID_MODULO;
		entradaConsultarEntidadEnrolamiento.p_UsaReglasNegocio = USA_REGLAS_NEGOCIO;
		entradaConsultarEntidadEnrolamiento.p_IdTipoEntidad = ID_TIPO_ENTIDAD;
		entradaConsultarEntidadEnrolamiento.p_IdTipoDocumentoIdentidad = socioNegocio.TiposDocumento.get(0).TipoDocumento;
		entradaConsultarEntidadEnrolamiento.p_DocumentoIdentidad = socioNegocio.TiposDocumento.get(0).NumeroDocumento;
		entradaConsultarEntidadEnrolamiento.p_DocumentoIdentidadAdicional = "";
		entradaConsultarEntidadEnrolamiento.p_IdTipoDocumentoIdentidadAdicional = "";
		entradaConsultarEntidadEnrolamiento.p_IdTipoPersona = socioNegocio.IdTipoPersona;
		entradaConsultarEntidadEnrolamiento.p_esRutInterno = "N";
		salidaConsultarEntidadEnrolamiento = consultarEntidadEnrolamiento.logica(conn, entradaConsultarEntidadEnrolamiento);
			
		Long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearClienteFullWS.consultarEntidadEnrolamiento", fin - inicio);
		return salidaConsultarEntidadEnrolamiento;
	}
	
	private String obtenerTransparenciaFiscal(Connection con, Long idPaisResidencia) throws SQLException {
		logger.debug("INICIO::CrearClienteFullWS.obtenerTransparenciaFiscal");
		Long inicio = System.currentTimeMillis();
		
		String transparencia = "";
		SQL sql = new SQL();
		HashMap<String, Object> parametros = new HashMap<>();
		parametros.put("IdPais", idPaisResidencia);
		List<Pais> paises = sql.query(con, SQL_SELECT_PAIS_TRANSPARENCIA_FISCAL, parametros, Pais.class);

		if(!paises.isEmpty()) {		
			Pais paisResidencia;
			paisResidencia = paises.get(0);
			String[] transparenciaFiscal = { "CONNACIONAL", "US-PERSON-FACTA", "OCDE-PERSON-CRS", "OTRO" };
			if ("S".equals(paisResidencia.EsPaisUs)) {
				transparencia =  transparenciaFiscal[1];
			}
			else if ("S".equals(paisResidencia.EsOcde) && !"CL".equals(paisResidencia.CodigoPais)) {
				transparencia =  transparenciaFiscal[2];
			}
			else if ("CL".equals(paisResidencia.CodigoPais)) {
				transparencia =  transparenciaFiscal[0];
			}
			else{
			    transparencia =  transparenciaFiscal[3];
			}
		}
				
		Long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearClienteFullWS.obtenerTransparenciaFiscal", fin - inicio);
		
		return transparencia;
	}
	
	private CrearAutorizadoOrden_Salida crearAutorizadoOrden(Connection conn, EntradaCrearClienteFullWS in, List<String> documentosIdentidad) throws Exception {
		
		CrearAutorizadoOrden crearAutorizadoOrden = new CrearAutorizadoOrden();
	    CrearAutorizadoOrden_Entrada entradaCrearAutorizadoOrden = new CrearAutorizadoOrden_Entrada();
		String OperarIClientes  = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "SOCIODENEGOCIO", "CLI.OperarIClientes");		
	    
		SQL sql = new SQL();
		HashMap<String,Object> parametros 	= new HashMap<>();
		parametros.put("DocumentoIdentidad", documentosIdentidad.get(0));
		List<ConsultaClientePortafolio> consultarPortafolioPropositoApv = sql.query(conn, SQL_SELECT_CONSULTAR_CLIENTE_PORTAFOLIO, parametros, ConsultaClientePortafolio.class);
		
		entradaCrearAutorizadoOrden.p_IdEmpresa 				= in.p_IdEmpresa;
		entradaCrearAutorizadoOrden.p_IdUsuario 				= in.p_IdUsuario;
		entradaCrearAutorizadoOrden.p_IdModulo 					= "PLA";
		entradaCrearAutorizadoOrden.p_IdEstado 					= "ACT";
		entradaCrearAutorizadoOrden.p_UsaReglasNegocio 			= "S";
		entradaCrearAutorizadoOrden.p_IdPersona 				= consultarPortafolioPropositoApv.get(0).IdPersona;
		entradaCrearAutorizadoOrden.p_IdPortafolio 				= consultarPortafolioPropositoApv.get(0).IdPortafolio;
		entradaCrearAutorizadoOrden.p_IdTipoAutorizado 			= "S".equalsIgnoreCase(OperarIClientes) ? "ICLIENTE-OPERAR" : "ICLIENTE-CONSULTAR";
		entradaCrearAutorizadoOrden.p_FirmaOptimusCliente 		= "S".equalsIgnoreCase(OperarIClientes) ? "S" : "N";
		CrearAutorizadoOrden_Salida salidaCrearAutorizadoOrden 	= crearAutorizadoOrden.logica(conn, entradaCrearAutorizadoOrden);
		
		return salidaCrearAutorizadoOrden;
		
	}
	
	private boolean ValidarDocumentoIdentidad(String TipoDocumentoIdentidad, String DocumentoIdentidad) {
		if("R".equals(TipoDocumentoIdentidad)) {
			Matcher matcher = PATTERN.matcher(DocumentoIdentidad);
			if ( matcher.matches() == false ) return false;
			String[] stringRut = DocumentoIdentidad.split("-");
			return stringRut[1].toLowerCase().equals(this.ObtenerDV(stringRut[0]));
		}
		return true;
	}
	
	private String ObtenerDV(String rut) {
		Integer M=0,S=1,T=Integer.parseInt(rut);
		for (;T!=0;T=(int) Math.floor(T/=10))
			S=(S+T%10*(9-M++%6))%11;
		return ( S > 0 ) ? String.valueOf(S-1) : "k";
	}
	
	
	public List<EntidadPersonaEliminar> obtenerDireccionesFisicas(Connection con, Long idPersona) throws Exception {
		logger.debug("INICIO::EliminarPersonaDireccionFisicaMasivo.obtenerDireccionesFisicas");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		HashMap<String, Object> params = new HashMap<String, Object>();

		List<Long> idEntidades = new ArrayList<Long>();
		idEntidades.add(idPersona);
		params.put("IdEntidad", idEntidades);
		List<EntidadPersonaEliminar> lstEntidad = sql.query(con, SQL_SELECT_DIRECCIONES_FISICAS_ID_PERSONA, params,
				EntidadPersonaEliminar.class);

		long finServicio = System.currentTimeMillis();
		logger.info("FIN::EliminarPersonaDireccionFisicaMasivo.obtenerDireccionesFisicas", finServicio - inicio);
		return lstEntidad;
	}
	
	public List<EntidadPersonaEliminar> obtenerDireccionesElectronicas(Connection con, Long idPersona)
			throws Exception {
		logger.debug("INICIO::EliminarPersonaDireccionElectronicaMasivo.obtenerDireccionesElectronicas");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		HashMap<String, Object> params = new HashMap<String, Object>();

		List<Long> idEntidades = new ArrayList<Long>();
		idEntidades.add(idPersona);
		params.put("IdEntidad", idEntidades);
		List<EntidadPersonaEliminar> lstEntidad = sql.query(con, SQL_SELECT_DIRECCIONES_ELECTRONICAS_ID_PERSONA, params, EntidadPersonaEliminar.class);

		long finServicio = System.currentTimeMillis();
		logger.info("FIN::EliminarPersonaDireccionElectronicaMasivo.obtenerDireccionesElectronicas",
				finServicio - inicio);
		return lstEntidad;
	}
	
	public List<EntidadPersonaEliminar> obtenerTelefonos(Connection con, Long idPersona) throws Exception {
		logger.debug("INICIO::EliminarPersonaTelefonoMasivo.obtenerTelefonos");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		HashMap<String, Object> params = new HashMap<String, Object>();

		List<Long> idEntidades = new ArrayList<Long>();
		idEntidades.add(idPersona);
		params.put("IdEntidad", idEntidades);
		List<EntidadPersonaEliminar> lstEntidad = sql.query(con, SQL_SELECT_TELEFONOS_ID_PERSONA, params,
				EntidadPersonaEliminar.class);

		long finServicio = System.currentTimeMillis();
		logger.info("FIN::EliminarPersonaTelefonoMasivo.obtenerTelefonos", finServicio - inicio);
		return lstEntidad;
	}
	
	private HashMap<String, Object> predeterminarTelefono(Connection conn, Long idTelefono, EntradaCrearClienteFullWS entrada) throws Exception {
		logger.debug("INICIO::Persona.predeterminarTelefono");
		long inicio = System.currentTimeMillis();

		SQL actualizar_telefono = new SQL();

		HashMap<String, Object> paramIN = new HashMap<>(6);
		paramIN.put("IdEmpresa", entrada.p_IdEmpresa);
		paramIN.put("IdUsuario", entrada.p_IdUsuario);
		paramIN.put("IdModulo", ID_MODULO);
		paramIN.put("UsaReglasNegocio", USA_REGLAS_NEGOCIO);
		paramIN.put("IdTelefono", idTelefono);

		HashMap<String, Class> paramOUT = new HashMap<>(6);
		paramOUT.put("p_out_RefId", Integer.class);
		paramOUT.put("p_out_AdvertenciasContador", Integer.class);
		paramOUT.put("p_out_AdvertenciasDetalle", MensajeAdvertencia.class);
		paramOUT.put("p_out_ErroresContador", Integer.class);
		paramOUT.put("p_out_ErroresDetalle", ErroresDetalle.class);
		HashMap<String, Object> resultadoPT = actualizar_telefono.call(conn, SQL_PREDETERMINAR_PERSONA_TELEFONO, paramIN, paramOUT);

		long finServicio = System.currentTimeMillis();
		logger.info("FIN::Persona.predeterminarTelefono", finServicio - inicio);
		return resultadoPT;
	}

	private HashMap<String, Object> predeterminarDireccionFisica(Connection conn, Long idDireccionFisica, EntradaCrearClienteFullWS entrada) throws Exception {
		logger.debug("INICIO::Persona.predeterminarDireccionFisica");
		long inicio = System.currentTimeMillis();

		SQL actualizar_direccion = new SQL();

		HashMap<String, Object> paramIN = new HashMap<>(6);
		paramIN.put("IdEmpresa", entrada.p_IdEmpresa);
		paramIN.put("IdUsuario", entrada.p_IdUsuario);
		paramIN.put("IdModulo", ID_MODULO);
		paramIN.put("UsaReglasNegocio", USA_REGLAS_NEGOCIO);
		paramIN.put("IdDireccionFisica", idDireccionFisica);

		HashMap<String, Class> paramOUT = new HashMap<>(6);
		paramOUT.put("p_out_RefId", Integer.class);
		paramOUT.put("p_out_AdvertenciasContador", Integer.class);
		paramOUT.put("p_out_AdvertenciasDetalle", MensajeAdvertencia.class);
		paramOUT.put("p_out_ErroresContador", Integer.class);
		paramOUT.put("p_out_ErroresDetalle", ErroresDetalle.class);
		HashMap<String, Object> resultadoPF = actualizar_direccion.call(conn, SQL_PREDETERMINAR_PERSONA_DIRECCION_FISICA, paramIN, paramOUT);

		long finServicio = System.currentTimeMillis();
		logger.info("FIN::Persona.predeterminarDireccionFisica", finServicio - inicio);
		return resultadoPF;
	}

	private HashMap<String, Object> predeterminarDireccionElectronica(Connection conn, Long idDireccionElectronica, EntradaCrearClienteFullWS entrada) throws Exception {
		logger.debug("INICIO::Persona.predeterminarDireccionElectronica");
		long inicio = System.currentTimeMillis();

		SQL actualizar_correo = new SQL();

		HashMap<String, Object> paramIN = new HashMap<>(6);
		paramIN.put("IdEmpresa", entrada.p_IdEmpresa);
		paramIN.put("IdUsuario", entrada.p_IdUsuario);
		paramIN.put("IdModulo", ID_MODULO);
		paramIN.put("UsaReglasNegocio", USA_REGLAS_NEGOCIO);
		paramIN.put("IdDireccionElectronica", idDireccionElectronica);

		HashMap<String, Class> paramOUT = new HashMap<>(6);
		paramOUT.put("p_out_RefId", Integer.class);
		paramOUT.put("p_out_AdvertenciasContador", Integer.class);
		paramOUT.put("p_out_AdvertenciasDetalle", MensajeAdvertencia.class);
		paramOUT.put("p_out_ErroresContador", Integer.class);
		paramOUT.put("p_out_ErroresDetalle", ErroresDetalle.class);
		HashMap<String, Object> resultadoPC = actualizar_correo.call(conn, SQL_PREDETERMINAR_PERSONA_DIRECCION_ELECTRONICA, paramIN, paramOUT);

		long finServicio = System.currentTimeMillis();
		logger.info("FIN::Persona.predeterminarDireccionElectronica", finServicio - inicio);
		return resultadoPC;
	}
	
	public List<RegistroCambioMasivo> obtenerRegistrosCambioEliminarDireccionesFisicas(List<DireccionFisica> direccionesFisicas) {
		logger.trace("INICIO::CrearClienteFullWS.obtenerRegistrosCambioEliminarDireccionFisica");
		long inicio = System.currentTimeMillis();

		List<RegistroCambioMasivo> registrosCambio = new ArrayList<>();
		for (DireccionFisica direccionFisica : direccionesFisicas) {
			registrosCambio.add(WebServiceUtils.crearRegistroCambio(direccionFisica.IdDireccionFisica, "Borrado", "S"));
		}

		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearClienteFullWS.obtenerRegistrosCambioEliminarDireccionFisica", fin - inicio);
		return registrosCambio;
	}
	
	public List<RegistroCambioMasivo> obtenerRegistrosCambioEliminarDireccionesElectronicas(List<DireccionElectronica> direccionesElectronicas) {
		logger.trace("INICIO::CrearClienteFullWS.obtenerRegistrosCambioEliminarDireccionElectronica");
		long inicio = System.currentTimeMillis();

		List<RegistroCambioMasivo> registrosCambio = new ArrayList<>();
		for (DireccionElectronica direccionElectronica : direccionesElectronicas) {
			registrosCambio.add(WebServiceUtils.crearRegistroCambio(direccionElectronica.IdDireccionElectronica, "Borrado", "S"));
		}

		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearClienteFullWS.obtenerRegistrosCambioEliminarDireccionElectronica", fin - inicio);
		return registrosCambio;
	}
	
	public List<RegistroCambioMasivo> obtenerRegistrosCambioEliminarTelefonos(List<Telefono> telefonos) {
		logger.trace("INICIO::CrearClienteFullWS.obtenerRegistrosCambioEliminarTelefono");
		long inicio = System.currentTimeMillis();

		List<RegistroCambioMasivo> registrosCambio = new ArrayList<>();
		for (Telefono telefono : telefonos) {
			registrosCambio.add(WebServiceUtils.crearRegistroCambio(telefono.IdTelefono, "Borrado", "S"));
		}

		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearClienteFullWS.obtenerRegistrosCambioEliminarTelefono", fin - inicio);
		return registrosCambio;
	}
	
	private List<MensajeSalida> PredeterminarDireccionesYTelefonos(Connection conn, EntradaCrearClienteFullWS entrada, SocioNegocio socioNegocio, Boolean EdicionDireccionesFisicas, Boolean EdicionCorreos, Boolean EdicionTelefonos) throws Exception {
		logger.trace("INICIO::CrearClienteFullWS.PredeterminarDireccionesYTelefonos");
		long inicio = System.currentTimeMillis();
		
		List<MensajeSalida> mensajes = new ArrayList<MensajeSalida>();
		
		Long idPersona = socioNegocio.IdPersona;

		if (EdicionDireccionesFisicas) {
			
			List<MensajeSalida> mensajesEdicionDireccionesFisicas = EdicionDireccionesFisicas(conn, entrada, idPersona, socioNegocio.IdTipoCanalIngresoCorredor, socioNegocio.DireccionesFisicas);
			
			if(!mensajesEdicionDireccionesFisicas.isEmpty()) {
				mensajes.addAll(mensajesEdicionDireccionesFisicas);
			}
			
			if(mensajes.stream().filter(mensaje -> PlataformaUtils.MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo)).count() > 0) {
				return mensajes;
			}
		}

		if (EdicionCorreos) {

			List<MensajeSalida> mensajesEdicionDireccionesElectronicas = EdicionDireccionesElectronicas(conn, entrada, idPersona, socioNegocio.IdTipoCanalIngresoCorredor, socioNegocio.DireccionesElectronicas);
			
			if(!mensajesEdicionDireccionesElectronicas.isEmpty()) {
				mensajes.addAll(mensajesEdicionDireccionesElectronicas);
			}
			
			if(mensajes.stream().filter(mensaje -> PlataformaUtils.MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo)).count() > 0) {
				return mensajes;
			}
			
		}

		if (EdicionTelefonos) {
			
			List<MensajeSalida> mensajesEdicionTelefonos = EdicionTelefonos(conn, entrada, idPersona, socioNegocio.IdTipoCanalIngresoCorredor, socioNegocio.Telefonos);
			
			if(!mensajesEdicionTelefonos.isEmpty()) {
				mensajes.addAll(mensajesEdicionTelefonos);
			}
			
			if(mensajes.stream().filter(mensaje -> PlataformaUtils.MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo)).count() > 0) {
				return mensajes;
			}
			
		}
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearClienteFullWS.PredeterminarDireccionesYTelefonos", fin - inicio);
		return mensajes;
	}
	
	private List<MensajeSalida> EdicionTelefonos(Connection conn, EntradaCrearClienteFullWS entrada, Long idPersona, String idTipoCanalIngresoCorredor, List<Telefono> telefonos) throws Exception {
		logger.trace("INICIO::CrearClienteFullWS.EdicionTelefonos");
		long inicio = System.currentTimeMillis();
		
		List<MensajeSalida> mensajes = new ArrayList<MensajeSalida>();
		
		List<EntidadPersonaEliminar> p_TelefonosPersona = new ArrayList<>();
		List<Telefono> p_TelefonosEliminar = new ArrayList<>();
		List<Telefono> p_TelefonosAgregados = new ArrayList<>();
		
		p_TelefonosPersona  = obtenerTelefonos(conn, idPersona);
		p_TelefonosAgregados = telefonos.stream().filter(tel -> tel.IdTelefono.equals(0L)).collect(Collectors.toList());
		
		Long idTelefono = 0L;
		
		CrearTelefonoMasivo  crearTelefonoMasivo = new CrearTelefonoMasivo();
		EntradaCrearTelefonoMasivo  entradaCrearTelefonoMasivo = new EntradaCrearTelefonoMasivo();
		entradaCrearTelefonoMasivo.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCrearTelefonoMasivo.p_IdModulo = ID_MODULO;
		entradaCrearTelefonoMasivo.p_IdUsuario = entrada.p_IdUsuario;
		entradaCrearTelefonoMasivo.p_UsaReglasNegocio = USA_REGLAS_NEGOCIO;
		
		for (Telefono telefono : p_TelefonosAgregados) {
			AtomicLong index = new AtomicLong(0);
			CrearTelefonoMasivo.Telefono inTelefono = new CrearTelefonoMasivo.Telefono();
			inTelefono.IdInterno = index.getAndIncrement();
			
			inTelefono.IdPais = telefono.IdPais;
			inTelefono.IdTipoTelefono = telefono.Tipo;
			inTelefono.Numero = telefono.Numero;
			inTelefono.Anexo = telefono.Anexo;
			inTelefono.CodigoProposito = telefono.IdProposito;
			inTelefono.Comentario = telefono.Comentario;
			inTelefono.IdEstado = "ACT";
			inTelefono.IdPersona = idPersona;
			inTelefono.Predeterminado = telefono.Predeterminado;
			entradaCrearTelefonoMasivo.p_Telefonos.add(inTelefono);
		}
		
		if(!entradaCrearTelefonoMasivo.p_Telefonos.isEmpty()) {
			SalidaCrearTelefonoMasivo salidaCrearTelefonoMasivo = crearTelefonoMasivo.logica(conn, entradaCrearTelefonoMasivo);
			idTelefono = salidaCrearTelefonoMasivo.P_OUT_REFID.get(0).IdEntidad;				
		}
		
		p_TelefonosEliminar = p_TelefonosPersona.stream().map(tel -> {
			Telefono telefono = new Telefono();
			telefono.IdTelefono = tel.IdEntidadPersonaEliminar;
			telefono.IdPersona = tel.IdPersona;
			return telefono;
		}).collect(Collectors.toList());

		if(!p_TelefonosEliminar.isEmpty()) {
			EliminarPersonaTelefonoMasivo eliminarPersonaTelefonoMasivo = new EliminarPersonaTelefonoMasivo();
			EntradaEjecutarCambiosPersona entradaEjecutarCambiosPersona = new EntradaEjecutarCambiosPersona();
			entradaEjecutarCambiosPersona.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaEjecutarCambiosPersona.p_IdPersona = idPersona;
			entradaEjecutarCambiosPersona.p_IdModulo = ID_MODULO;
			entradaEjecutarCambiosPersona.p_IdUsuario = entrada.p_IdUsuario;
			entradaEjecutarCambiosPersona.p_UsaReglasNegocio = USA_REGLAS_NEGOCIO;
			entradaEjecutarCambiosPersona.p_IdTipoCanal = idTipoCanalIngresoCorredor;
			entradaEjecutarCambiosPersona.p_EsCrearCliente = "S";
			entradaEjecutarCambiosPersona.p_RegistroCambio = obtenerRegistrosCambioEliminarTelefonos(p_TelefonosEliminar);
			
			SalidaEjecutarCambiosPersona salidaEliminarPersonaTelefonoMasivo = eliminarPersonaTelefonoMasivo.logica(conn, entradaEjecutarCambiosPersona);
			
			if (!salidaEliminarPersonaTelefonoMasivo.P_OUT_MENSAJES.isEmpty()) {
				mensajes.addAll(salidaEliminarPersonaTelefonoMasivo.P_OUT_MENSAJES);
			}
			
			if(salidaEliminarPersonaTelefonoMasivo.P_OUT_ERRORESCONTADOR > 0L) {
				return mensajes;
			}
		}
		
		HashMap<String, Object> salidaTelefono = predeterminarTelefono(conn, idTelefono, entrada);
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearClienteFullWS.EdicionTelefonos", fin - inicio);
		return mensajes;
	}

	private List<MensajeSalida> EdicionDireccionesElectronicas(Connection conn, EntradaCrearClienteFullWS entrada, Long idPersona, String idTipoCanalIngresoCorredor, List<DireccionElectronica> direccionesElectronicas) throws Exception {
		logger.trace("INICIO::CrearClienteFullWS.EdicionDireccionesElectronicas");
		long inicio = System.currentTimeMillis();
		
		List<MensajeSalida> mensajes = new ArrayList<MensajeSalida>();
		
		List<EntidadPersonaEliminar> p_DireccionesPersona = new ArrayList<>();
		List<DireccionElectronica> p_DireccionesElectronicasEliminar = new ArrayList<>();
		List<DireccionElectronica> p_DireccionesAgregadas = new ArrayList<>();
		
		p_DireccionesPersona  = obtenerDireccionesElectronicas(conn, idPersona);
		p_DireccionesAgregadas = direccionesElectronicas.stream().filter(dirE -> dirE.IdDireccionElectronica.equals(0L)).collect(Collectors.toList());
		
		Long idDireccionElectronica = 0L;
		
		CrearDireccionElectronicaMasivo  crearDireccionElectronica = new CrearDireccionElectronicaMasivo ();
		EntradaCrearDireccionElectronicaMasivo  entradaCrearDireccionElectronicaMasivo = new EntradaCrearDireccionElectronicaMasivo();
		entradaCrearDireccionElectronicaMasivo.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCrearDireccionElectronicaMasivo.p_IdModulo = ID_MODULO;
		entradaCrearDireccionElectronicaMasivo.p_IdUsuario = entrada.p_IdUsuario;
		entradaCrearDireccionElectronicaMasivo.p_UsaReglasNegocio = USA_REGLAS_NEGOCIO;
		
		for (DireccionElectronica direccionElectronica : p_DireccionesAgregadas) {
			AtomicLong index = new AtomicLong(0);
			CrearDireccionElectronicaMasivo.DireccionElectronica inDireccionElectronica = new CrearDireccionElectronicaMasivo.DireccionElectronica();
			inDireccionElectronica.IdInterno = index.getAndIncrement();
			inDireccionElectronica.Direccion = direccionElectronica.Direccion;
			inDireccionElectronica.CodigoProposito = direccionElectronica.IdProposito;
			inDireccionElectronica.Comentario = direccionElectronica.Comentario;
			inDireccionElectronica.IdEstado = "ACT";
			inDireccionElectronica.IdPersona = idPersona;
			inDireccionElectronica.Predeterminado = direccionElectronica.Predeterminado;
			entradaCrearDireccionElectronicaMasivo.p_DireccionesElectronica.add(inDireccionElectronica);
		}
		
		if(!entradaCrearDireccionElectronicaMasivo.p_DireccionesElectronica.isEmpty()) {
			SalidaCrearDireccionElectronicaMasivo salidaCrearDireccionElectronicaMasivo = crearDireccionElectronica.logica(conn, entradaCrearDireccionElectronicaMasivo);
			idDireccionElectronica = salidaCrearDireccionElectronicaMasivo.P_OUT_REFID.get(0).IdEntidad;				
		}
		
		p_DireccionesElectronicasEliminar = p_DireccionesPersona.stream().map(dirP -> {
			DireccionElectronica direccion = new DireccionElectronica();
			direccion.IdDireccionElectronica = dirP.IdEntidadPersonaEliminar;
			direccion.IdPersona = dirP.IdPersona;
			return direccion;
		}).collect(Collectors.toList());
		
		if(!p_DireccionesElectronicasEliminar.isEmpty()) {
			EliminarPersonaDireccionElectronicaMasivo eliminarPersonaDireccionElectronicaMasivo = new EliminarPersonaDireccionElectronicaMasivo();
			EntradaEjecutarCambiosPersona entradaEjecutarCambiosPersona = new EntradaEjecutarCambiosPersona();
			entradaEjecutarCambiosPersona.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaEjecutarCambiosPersona.p_IdPersona = idPersona;
			entradaEjecutarCambiosPersona.p_IdModulo = ID_MODULO;
			entradaEjecutarCambiosPersona.p_IdUsuario = entrada.p_IdUsuario;
			entradaEjecutarCambiosPersona.p_UsaReglasNegocio = USA_REGLAS_NEGOCIO;
			entradaEjecutarCambiosPersona.p_IdTipoCanal = idTipoCanalIngresoCorredor;
			entradaEjecutarCambiosPersona.p_EsCrearCliente = "S";
			entradaEjecutarCambiosPersona.p_RegistroCambio = obtenerRegistrosCambioEliminarDireccionesElectronicas(p_DireccionesElectronicasEliminar);
			
			SalidaEjecutarCambiosPersona salidaEliminarPersonaDireccionElectronicaMasivo = eliminarPersonaDireccionElectronicaMasivo.logica(conn, entradaEjecutarCambiosPersona);
			
			if (!salidaEliminarPersonaDireccionElectronicaMasivo.P_OUT_MENSAJES.isEmpty()) {
				mensajes.addAll(salidaEliminarPersonaDireccionElectronicaMasivo.P_OUT_MENSAJES);
			}
			
			if(salidaEliminarPersonaDireccionElectronicaMasivo.P_OUT_ERRORESCONTADOR > 0L) {
				return mensajes;
			}
		}
		
		HashMap<String, Object> salidaDireccionElectronica = predeterminarDireccionElectronica(conn, idDireccionElectronica, entrada);
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearClienteFullWS.EdicionDireccionesElectronicas", fin - inicio);
		return mensajes;
	}

	private List<MensajeSalida> EdicionDireccionesFisicas(Connection conn, EntradaCrearClienteFullWS entrada, Long idPersona, String idTipoCanalIngresoCorredor, List<DireccionFisica> direccionesFisicas) throws Exception {
		logger.trace("INICIO::CrearClienteFullWS.EdicionDireccionesFisicas");
		long inicio = System.currentTimeMillis();
		
		List<MensajeSalida> mensajes = new ArrayList<MensajeSalida>();
		
		List<EntidadPersonaEliminar> p_DireccionesPersona = new ArrayList<>();
		List<DireccionFisica> p_DireccionesFisicasEliminar = new ArrayList<>();
		List<DireccionFisica> p_DireccionesAgregadas = new ArrayList<>();
		
		p_DireccionesPersona  = obtenerDireccionesFisicas(conn, idPersona);
		p_DireccionesAgregadas = direccionesFisicas.stream().filter(dirF -> dirF.IdDireccionFisica.equals(0L)).collect(Collectors.toList());
		
		Long idDireccionFisica = 0L;
		
		CrearDireccionFisicaMasivo crearDireccionFisica = new CrearDireccionFisicaMasivo();
		EntradaCrearDireccionFisicaMasivo entradaCrearDireccionFisicaMasivo = new EntradaCrearDireccionFisicaMasivo();
		entradaCrearDireccionFisicaMasivo.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCrearDireccionFisicaMasivo.p_IdModulo = ID_MODULO;
		entradaCrearDireccionFisicaMasivo.p_IdUsuario = entrada.p_IdUsuario;
		entradaCrearDireccionFisicaMasivo.p_UsaReglasNegocio = USA_REGLAS_NEGOCIO;
		
		for (DireccionFisica direccionFisica : p_DireccionesAgregadas) {
			AtomicLong index = new AtomicLong(0);
			CrearDireccionFisicaMasivo.DireccionFisica inDireccionFisica = new CrearDireccionFisicaMasivo.DireccionFisica();
			inDireccionFisica.IdInterno = index.getAndIncrement();
			inDireccionFisica.Calle = direccionFisica.Calle;
			inDireccionFisica.Numero = direccionFisica.Numero;
			inDireccionFisica.Departamento = direccionFisica.Departamento;
			inDireccionFisica.Sector = direccionFisica.Sector;
			inDireccionFisica.CodigoPostal = direccionFisica.CodigoPostal;
			inDireccionFisica.IdCiudad = direccionFisica.IdCiudad;
			inDireccionFisica.IdDivisionPais = direccionFisica.IdComuna;
			inDireccionFisica.CodigoProposito = direccionFisica.IdProposito;
			inDireccionFisica.Comentario = direccionFisica.Comentario;
			inDireccionFisica.IdEstado = "ACT";
			inDireccionFisica.IdPersona = idPersona;
			inDireccionFisica.Predeterminado = direccionFisica.Predeterminado;
			entradaCrearDireccionFisicaMasivo.p_DireccionesFisica.add(inDireccionFisica);
		}
		
		if(!entradaCrearDireccionFisicaMasivo.p_DireccionesFisica.isEmpty()) {
			SalidaCrearDireccionFisicaMasivo salidaCrearDireccionFisicaMasivo = crearDireccionFisica.logica(conn, entradaCrearDireccionFisicaMasivo);
			idDireccionFisica = salidaCrearDireccionFisicaMasivo.P_OUT_REFID.get(0).IdEntidad;				
		}
		
		p_DireccionesFisicasEliminar = p_DireccionesPersona.stream().map(dirP -> {
			DireccionFisica direccion = new DireccionFisica();
			direccion.IdDireccionFisica = dirP.IdEntidadPersonaEliminar;
			direccion.IdPersona = dirP.IdPersona;
			return direccion;
		}).collect(Collectors.toList());

		if(!p_DireccionesFisicasEliminar.isEmpty()) {
			EliminarPersonaDireccionFisicaMasivo eliminarPersonaDireccionFisicaMasivo = new EliminarPersonaDireccionFisicaMasivo();
			EntradaEjecutarCambiosPersona entradaEjecutarCambiosPersona = new EntradaEjecutarCambiosPersona();
			entradaEjecutarCambiosPersona.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaEjecutarCambiosPersona.p_IdPersona = idPersona;
			entradaEjecutarCambiosPersona.p_IdModulo = ID_MODULO;
			entradaEjecutarCambiosPersona.p_IdUsuario = entrada.p_IdUsuario;
			entradaEjecutarCambiosPersona.p_UsaReglasNegocio = USA_REGLAS_NEGOCIO;
			entradaEjecutarCambiosPersona.p_IdTipoCanal = idTipoCanalIngresoCorredor;
			entradaEjecutarCambiosPersona.p_EsCrearCliente = "S";
			entradaEjecutarCambiosPersona.p_RegistroCambio = obtenerRegistrosCambioEliminarDireccionesFisicas(p_DireccionesFisicasEliminar);
			
			SalidaEjecutarCambiosPersona salidaEliminarPersonaDireccionFisicaMasivo = eliminarPersonaDireccionFisicaMasivo.logica(conn, entradaEjecutarCambiosPersona);
			
			if (!salidaEliminarPersonaDireccionFisicaMasivo.P_OUT_MENSAJES.isEmpty()) {
				mensajes.addAll(salidaEliminarPersonaDireccionFisicaMasivo.P_OUT_MENSAJES);
			}
			
			if(salidaEliminarPersonaDireccionFisicaMasivo.P_OUT_ERRORESCONTADOR > 0L) {
				return mensajes;
			}
		}
		
		HashMap<String, Object> salidaDireccionFisica = predeterminarDireccionFisica(conn, idDireccionFisica, entrada);
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearClienteFullWS.EdicionDireccionesFisicas", fin - inicio);
		return mensajes;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaCrearClienteFullWS jin = gson.fromJson(in.toString(), EntradaCrearClienteFullWS.class);
		SalidaCrearClienteFullWS out = logica(conn,jin);

		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public static void main(String[] args) {
		System.out.println("== INICIO PRUEBA LOCAL CREAR CLIENTE FULL 16.19 ==");

		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
					.serializeNulls().create();
					

			/* BUENO */ String jsonCL ="{ \r\n"
					+ "   \"p_IdEmpresa\": 1,\r\n"
					+ "   \"p_IdUsuario\": 97,\r\n"
					+ "   \"p_ClaveUsuario\":\"1\",\r\n"
					+ "   \"p_SocioNegocio\": [\r\n"
					+ "        {\r\n"
					+ "           \"IdInterno\": 0,\r\n"
					+ "           \"IdTipoPersona\":\"N\",\r\n"
					+ "           \"Nombres\":\"Olobos\",\r\n"
					+ "           \"ApellidoPaterno\":\"666\",\r\n"
					+ "           \"ApellidoMaterno\":\"666\",\r\n"
					+ "           \"IdGenero\":\"M\",\r\n"
					+ "           \"RazonSocial\":\"\",\r\n"
					+ "           \"IdPaisNacimientoConstitucion\": 44,\r\n"
					+ "           \"FechaNacimientoConstitucion\":\"1980-03-10\",\r\n"
					+ "           \"IdEstadoCivil\":\"SO\",\r\n"
					+ "           \"IdRegimenConyugal\":\"PG\",\r\n"
					+ "           \"IdPaisResidenciaCasaMatriz\": 44,\r\n"
					+ "           \"ResidenciaTriburariaFueraChile\":\"N\",\r\n"
					+ "           \"IdPaisNacionalidadPrincipal\": 44,\r\n"
					+ "           \"IdGiro\": 634,\r\n"
					+ "           \"IdProfesionActividad\": 452,\r\n"
					+ "           \"Cargo\":\"CA0005\",\r\n"
					+ "           \"DetalleCargo\":\"Detiene a los tipos malos\",\r\n"
					+ "           \"IdSituacionLaboral\":\"INDE\",\r\n"
					+ "           \"IdTipoCanalIngresoCorredor\":\"AGE\",\r\n"
					+ "           \"RangoPatrimonio\": 2,\r\n"
					+ "           \"CodigoRangoRenta\":\"RANGO_RENTA_5000\",\r\n"
					+ "           \"CodigoRangoNivelVenta\":\"RANGO_VENTA_25000\",\r\n"
					+ "           \"InicioActividad\":\"2021-12-30\",\r\n"
					+ "           \"GlobalLegal\":\"Legal Entity\",\r\n"
					+ "           \"DireccionesFisicas\": [\r\n"
					+ "                {\r\n"
					+ "                   \"IdProposito\":\"PART\",\r\n"
					+ "                   \"Sector\":\"VITACURA\",\r\n"
					+ "                   \"CodigoPostal\":\"8150000\",\r\n"
					+ "                   \"IdComuna\": 75882,\r\n"
					+ "                   \"Calle\":\"Av. Brasil\",\r\n"
					+ "                   \"Numero\":\"180\",\r\n"
					+ "                   \"Departamento\":\"505\",\r\n"
					+ "                   \"Comentario\":\"casa\",\r\n"
					+ "                   \"IdCiudad\": 80,\r\n"
					+ "                   \"CodigoUnicoTerritorial\":\"03302\",\r\n"
					+ "                   \"Predeterminado\":\"S\"\r\n"
					+ "                }\r\n"
					+ "            ],\r\n"
					+ "           \"Telefonos\": [\r\n"
					+ "                {\r\n"
					+ "                   \"IdProposito\":\"PART\",\r\n"
					+ "                   \"Numero\":\"+56977073648\",\r\n"
					+ "                   \"IdPais\": 44,\r\n"
					+ "                   \"Tipo\":\"MOVIL\",\r\n"
					+ "                   \"Anexo\":\"\",\r\n"
					+ "                   \"Predeterminado\":\"S\"\r\n"
					+ "                }\r\n"
					+ "            ],\r\n"
					+ "           \"DireccionesElectronicas\": [\r\n"
					+ "                {\r\n"
					+ "                   \"Comentario\":\"Mail Principal\",\r\n"
					+ "                   \"IdProposito\":\"PART\",\r\n"
					+ "                   \"Direccion\":\"zedx@gmail.com\",\r\n"
					+ "                   \"Predeterminado\":\"S\"\r\n"
					+ "                }\r\n"
					+ "            ],\r\n"
					+ "           \"TiposDocumento\": [\r\n"
					+ "                {\r\n"
					+ "                   \"TipoDocumento\":\"R\",\r\n"
					+ "                   \"NumeroDocumento\":\"10896747-1\",\r\n"
					+ "                   \"NumeroSerie\":\"\",\r\n"
					+ "                   \"FechaEmision\":\"2021-12-20\",\r\n"
					+ "                   \"FechaVencimiento\":\"2030-12-20\"\r\n"
					+ "                }\r\n"
					+ "            ],\r\n"
					+ "           \"CuentasBancarias\": [\r\n"
					+ "                {\r\n"
					+ "                   \"IdBanco\": 4,\r\n"
					+ "                   \"CodigoTipoCuentaBancaria\":\"CUCO\",\r\n"
					+ "                   \"NumeroCuentaBancaria\":\"470470470\",\r\n"
					+ "                   \"IdMoneda\":\"CLP\",\r\n"
					+ "                   \"CodigoPlaza\": 761,\r\n"
					+ "                   \"PAC\":\"S\",\r\n"
					+ "                   \"ActualizaMedioPago\":\"S\",\r\n"
					+ "                   \"Predeterminado\":\"S\"\r\n"
					+ "                }\r\n"
					+ "            ],\"TarjetasBancarias\": [\r\n"
					+ "             {\r\n"
					+ "             \"IdBanco\": 0,\r\n"
					+ "             \"IdTipo\": \"\",\r\n"
					+ "             \"NumeroTarjeta\": \"\",\r\n"
					+ "             \"CodigoVerificacion\": 0,\r\n"
					+ "             \"FechaVencimiento\": \"\"\r\n"
					+ "            }\r\n"
					+ "           ],\r\n"
					+ "           \"IndicadoresPersona\": [\r\n"
					+ "                {\r\n"
					+ "                   \"CodigoTipoIndicador\":\"NCG380PER\",\r\n"
					+ "                   \"ValorIndicador\":\"3\"\r\n"
					+ "                }\r\n"
					+ "            ],\r\n"
					+ "           \"DatosCliente\": {\r\n"
					+ "               \"IdCategoriaInversionista\":\"ARRIESGADO\",\r\n"
					+ "               \"IdTipoCliente\":\"RETAIL\",\r\n"
					+ "               \"IdSegmentoCliente\": 2,\r\n"
					+ "               \"CodigoEjecutivo\": 760,\r\n"
					+ "               \"TipoClienteCMF\": 36,\r\n"
					+ "               \"AreaCliente\":\"REDMIN\",\r\n"
					+ "               \"Sucursal\": 1,\r\n"
					+ "               \"ClienteFinal\":\"S\",\r\n"
					+ "               \"FirmaAutomaticaDocumento\":\"S\",\r\n"
					+ "               \"DatosPortafolio\": {\r\n"
					+ "                   \"CuentasCustodio\": [\r\n"
					+ "                        {\r\n"
					+ "                           \"IdBoveda\":\"DCV\",\r\n"
					+ "                           \"Codigo\":\"\",\r\n"
					+ "                           \"IdTipoCuentaCustodio\":\"DEPOSITANTE\",\r\n"
					+ "                           \"LiquidaDiv\":\"S\",\r\n"
					+ "                           \"LiquidaVen\":\"S\",\r\n"
					+ "                           \"LiquidaCup\":\"S\",\r\n"
					+ "                           \"LiquidaPre\":\"S\"\r\n"
					+ "                        }\r\n"
					+ "                    ]\r\n"
					+ "                }\r\n"
					+ "            }\r\n"
					+ "        }\r\n"
					+ "    ]\r\n"
					+ "}";

			SQL sql = new SQL();
			//sql.batch(con, "UPDATE \"BSCL.Plataforma.BD::FRM.CampoFormulario\" SET \"Requerido\" = 'N' WHERE \"IdTipoCanal\" = 'WBS'", new ArrayList<>(), Object.class);
			/*sql.batch(con, "UPDATE \"BSCL.Plataforma.BD::FRM.CampoFormulario\" SET \"Requerido\" = 'S' WHERE \"IdTipoCanal\" = 'WBS' AND \"CodigoFormulario\" IN " 
			+ "('INGRESAR_CLIENTE_FULL_NATURAL','DATOS_GENERALES_PERSONA_FULL_NATURAL','DATOS_GENERALES_CLIENTE_FULL_NATURAL',"
			+ "'TELEFONO_FULL_NATURAL','TARJETA_BANCARIA_FULL_NATURAL','RELACIONES_FULL_NATURAL',"
			+ "'FATCA_FULL_NATURAL','FATCA_PAIS_RESIDENCIA_FULL_NATURAL','DOCUMENTO_IDENTIDAD_FULL_NATURAL','DIRECCION_FISICA_FULL_NATURAL')", new ArrayList<>(), Object.class);
			*/

			CrearClienteFullWS demo = new CrearClienteFullWS();
			EntradaCrearClienteFullWS Objin = gson.fromJson(jsonCL, EntradaCrearClienteFullWS.class);			
			System.out.println("ENTRADA");
			System.out.println(Objin.toString());

			try {
				SalidaCrearClienteFullWS ObjOut = demo.logica(con, Objin);
				if(ObjOut.P_OUT_ERRORESCONTADOR > 0) {
					con.rollback();
				}
				con.rollback();
			 	//con.commit();

				System.out.println("SALIDA");
				System.out.println(ObjOut);
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