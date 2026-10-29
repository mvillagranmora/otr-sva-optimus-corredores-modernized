package cl.bcs.socionegocio.cli;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.sql.Date;
import cl.bcs.plataforma.acc.CrearUsuario;
import cl.bcs.plataforma.acc.CrearUsuario.EntradaCrearUsuario;
import cl.bcs.plataforma.acc.CrearUsuario.SalidaCrearUsuario;
import cl.bcs.plataforma.acc.CrearUsuario.Usuario;
import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo;
import cl.bcs.plataforma.evt.AgregarEvento;
import cl.bcs.plataforma.evt.AgregarEvento.ObjEVENTOS;
import cl.bcs.plataforma.evt.AgregarEvento.ObjIDDIRECCIONES;
import cl.bcs.plataforma.frm.AsignarCamposFormulario;
import cl.bcs.plataforma.frm.ValidarCamposObligatorios;
import cl.bcs.plataforma.frm.ValidarCamposObligatorios.SalidaValidarCamposObligatorios;
import cl.bcs.plataforma.frm.pojo.FormularioCanal;
import cl.bcs.plataforma.ind.ConsultarIndicadores;
import cl.bcs.plataforma.ind.ConsultarIndicadores.Indicador;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarImagen;
import cl.bcs.plataforma.pla.utils.ConsultarImagen.ConsultarImagenEntrada;
import cl.bcs.plataforma.pla.utils.ConsultarImagen.ConsultarImagenSalida;
import cl.bcs.plataforma.pla.utils.ConsultarValoresPorDefecto;
import cl.bcs.plataforma.pla.utils.ConsultarValoresPorDefecto.EntradaConsultarValoresPorDefecto;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.portafolio.CrearPortafolio;
import cl.bcs.portafolio.RevisaDatosPortafolio;
import cl.bcs.portafolio.RevisaDatosPortafolio.EntradaRevisaDatosPortafolio;
import cl.bcs.portafolio.RevisaDatosPortafolio.SalidaRevisaDatosPortafolio;
import cl.bcs.socionegocio.por.CrearCuentaCustodioMasivo;
import cl.bcs.socionegocio.por.CrearCuentaCustodioMasivo.EntradaCrearCuentaCustodioMasivo;
import cl.bcs.socionegocio.por.CrearCuentaCustodioMasivo.SalidaCrearCuentaCustodioMasivo;
import cl.bcs.socionegocio.por.pojo.CuentaCustodio;
import cl.bcs.socionegocio.enr.EnrolamientoUtils;
import cl.bcs.socionegocio.enr.AdministrarEnrolamientoDireccionFisica.DireccionFisicaPredeterminada;
import cl.bcs.socionegocio.enr.AdministrarEnrolamientoTelefono.TelefonoPredeterminado;
import cl.bcs.socionegocio.enr.pojo.PerfilComercialBase;
import cl.bcs.socionegocio.enr.pojo.SalidaAdministrarBorrador;
import cl.bcs.socionegocio.enr.pojo.TipoCliente;
import cl.bcs.socionegocio.per.CrearDocumentoIdentidad;
import cl.bcs.socionegocio.per.CrearDocumentoIdentidad.EntradaCrearDocumentoIdentidad;
import cl.bcs.socionegocio.per.CrearDocumentoIdentidad.SalidaCrearDocumentoIdentidad;
import cl.bcs.socionegocio.per.CrearPersona;
import cl.bcs.socionegocio.per.CrearPersona.EntradaServicioCrearPersona;
import cl.bcs.socionegocio.per.CrearPersona.SalidaServicioCrearPersona;
import cl.bcs.socionegocio.per.ModificarPersonaDatosGenerales;
import cl.bcs.socionegocio.per.ModificarPersonaDatosGenerales.EntradaModificarPersonaDatosGenerales;
import cl.bcs.socionegocio.per.ModificarPersonaDatosGenerales.SalidaModificarPersonaDatosGenerales;
import cl.bcs.socionegocio.per.pojo.RegistroCambio;
import cl.bcs.socionegocio.pojo.AdvertenciaDetalle;
import cl.bcs.socionegocio.pojo.Enrolamiento;
import cl.bcs.socionegocio.pojo.ErrorDetalle;
import cl.bcs.socionegocio.pojo.SalidaEstandar;
import cl.bcs.socionegocio.soc.SocioNegocioUtils;
import cl.bcs.socionegocio.soc.SocioNegocioUtils.TipoSegmento;
import cl.bcs.socionegocio.soc.ValidarDocumentoIdentidad;
import cl.bcs.socionegocio.soc.ValidarDocumentoIdentidad.SalidaValidarDocumentoIdentidad;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.StringUtils;
import cl.bcs.utils.Utils;

public class CrearCliente implements JavaCall {
	static final Logger logger = LogManager.getLogger(CrearCliente.class);

	public String SQL_ConsultarIdPersonaUsuario 								= "SELECT EMP.\"IdPersona\" as \"Id\" FROM \"BSCL.SocioNegocio.BD::EMP.Empleado\" EMP WHERE EMP.\"IdEmpleado\" = :IdEmpleadoEjecutivo";	
	private final String SQL_CREAR_CLIENTE 										= loadResourceToString("cl/bcs/socionegocio/cli/sql/CrearCliente_Insert.sql");
	private final String SQL_CREAR_CLIENTE_SOCIONEGOCIO_UPDATE 					= loadResourceToString("cl/bcs/socionegocio/cli/sql/CrearCliente_SocioNegocio_Update.sql");
	private final String SQL_CREAR_CLIENTE_DOCUMENTOIDENTIDADADICIONAL_UPDATE 	= loadResourceToString("cl/bcs/socionegocio/cli/sql/CrearCliente_DocumentoIdentidadAdicional_Update.sql");
	private final String SQL_CONSULTAR_INDICADOR_OTC 							= loadResourceToString("cl/bcs/socionegocio/cli/sql/CrearCliente_Select_IndicadorOTC.sql");
	private final String SQL_INSERTAR_INDICADOR_CLIENTE	 						= loadResourceToString("cl/bcs/socionegocio/cli/sql/CrearCliente_Insert_IndicadorCliente.sql");
	private final String SQL_CONSULTAR_INDICADOR 								= loadResourceToString("cl/bcs/socionegocio/cli/sql/CrearCliente_Select_Indicador.sql");
	public String SQL_INSERTAR_INDICADOR_HERENCIA_CLIENTE 						= loadResourceToString("cl/bcs/socionegocio/cli/sql/CrearCliente_IndicadorClienteHerencia_Insert.sql");
	public String SQL_SELECT_NOTIFICACION 										= loadResourceToString("cl/bcs/socionegocio/cli/sql/CrearCliente_SelectNotificacion.sql");
	public final String SQL_CONSULTA_EXISTENCIA_PERSONA							= loadResourceToString("cl/bcs/socionegocio/per/sql/ConsultaExistenciaPersona.sql");
	public final String SQL_CONSULTAR_CANAL                                     = loadResourceToString("cl/bcs/socionegocio/cli/sql/CrearCliente_SelectCanalIngreso.sql");
	public final String SQL_CONSULTAR_EJECUTIVO									= loadResourceToString("cl/bcs/socionegocio/cli/sql/CrearCliente_SelectEjecutivo.sql");
	public final String SQL_CONSULTAR_TIPOCLIENTECMF							= loadResourceToString("cl/bcs/socionegocio/cli/sql/CrearCliente_SelectTipoClienteCMF.sql");
	public final String SQL_CONSULTAR_AREA										= loadResourceToString("cl/bcs/socionegocio/cli/sql/CrearCliente_SelectArea.sql");
	public final String SQL_CONSULTARPERSONA									= loadResourceToString("cl/bcs/socionegocio/cli/sql/CrearCliente_SelectPersona.sql");
	public final String SQL_CONSULTARCLIENTE									= loadResourceToString("cl/bcs/socionegocio/cli/sql/CrearCliente_SelectCliente.sql");
	public final String SQL_CONSULTARSEGMENTOS									= loadResourceToString("cl/bcs/socionegocio/cli/sql/CrearCliente_SelectSegmentos.sql");
	public final String SQL_CREATE_TABLA_PARAMETROS 							= "CREATE LOCAL TEMPORARY TABLE  #tabla_Parametros (\"NombreParametro\" NVARCHAR(100), \"Valor\" NVARCHAR(100))";
	public final String SQL_INSERT_TABLA_PARAMETROS								= loadResourceToString("cl/bcs/socionegocio/cli/sql/CrearCliente_ParametrosInsert.sql");
	public final String SQL_CALL_AGREGAR_PROGRAMA 								= loadResourceToString("cl/bcs/socionegocio/cli/sql/CrearCliente_CallAgregaPrograma.sql");
	public final String SQL_DROP_TABLA_PARAMETROS								= "DROP TABLE #tabla_Parametros";
	public final String SQL_SELECT_DIRECCION_FISICA_PREDETERMINADA              = loadResourceToString("cl/bcs/socionegocio/cli/sql/CrearCliente_SelectDireccionFisicaPredeterminada.sql");
	public final String SQL_SELECT_TELEFONO_PREDETERMINADO                      = loadResourceToString("cl/bcs/socionegocio/cli/sql/CrearCliente_SelectTelefonoPredeterminado.sql");
	public final String SQL_UPDATE_DIRECCION_FISICA_Y_TELEFONO_PORTAFOLIO       = loadResourceToString("cl/bcs/socionegocio/cli/sql/CrearCliente_UpdateDireccionFisicaYTelefonoPortafolio.sql");
	private final String SQL_CONSULTA_EXISTE_USUARIO							= loadResourceToString("cl/bcs/plataforma/acc/sql/Consulta_Existe_Usuario.sql");

	private final String CODIGO_OPERA_MERCADO_OTC = "OM_OTC";

	public static class EntradaServicioCrearCliente {
		public long p_IdEmpresa;
		public long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_CodigoFormulario;
		public String p_IdTipoCanal;
		public List<Enrolamiento> p_ConsultarEnrolamiento;

		public EntradaServicioCrearCliente() {
			this.p_UsaReglasNegocio = "N";
			this.p_CodigoFormulario = "";
			this.p_IdTipoCanal = "";
		}

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class SalidaServicioCrearCliente extends SalidaEstandar {
		public SalidaServicioCrearCliente() {
			this.P_OUT_REFIDCLIENTE = 0;
			this.P_OUT_REFIDPORTAFOLIO = 0;
			this.P_OUT_DOCUMENTOIDENTIDAD = "";
			this.P_OUT_ADVERTENCIASCONTADOR = 0;
			this.P_OUT_ERRORESCONTADOR = 0;
			this.P_OUT_ADVERTENCIASDETALLE = new ArrayList<AdvertenciaDetalle>();
			this.P_OUT_ERRORESDETALLE = new ArrayList<ErrorDetalle>();
			this.P_OUT_PORTAFOLIO_IDS = new ArrayList<>();
		}

		public long P_OUT_REFIDCLIENTE;
		public long P_OUT_REFIDPORTAFOLIO;	// Campo inutil, reemplazado por P_OUT_PORTAFOLIO_IDS, ahora se crean hasta N portafolios.
		public String P_OUT_DOCUMENTOIDENTIDAD;

		public List<Long> P_OUT_PORTAFOLIO_IDS;

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class Contador {
		public long Count;

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class EntidadCliente extends Enrolamiento {
		public EntidadCliente() {
			super();
		}

		public long IdCliente;
		public long IdPersona;
		public String IdEstado;
		public Long IdSegmentoCliente;
		public String EsEnrolado;
		public String IdPlantillaLimiteExtra;
		public long IdPersonaUsuario;

		public void setFromEnrolamiento(Enrolamiento enr) {
			this.IdEnrolamiento = enr.IdEnrolamiento;
			this.IdEmpresa = enr.IdEmpresa;
			this.IdUsuario = enr.IdUsuario;
			this.IdTipoEntidad = enr.IdTipoEntidad;
			this.IdEntidadEnrolada = enr.IdEntidadEnrolada;
			this.IdTipoDocumentoIdentidad = enr.IdTipoDocumentoIdentidad;
			this.IdTipoDocumentoIdentidadAsociado = enr.IdTipoDocumentoIdentidadAsociado;
			this.DocumentoIdentidad = enr.DocumentoIdentidad;
			this.IdTipoDocumentoIdentidadAdicional = enr.IdTipoDocumentoIdentidadAdicional;
			this.DocumentoIdentidadAdicional = enr.DocumentoIdentidadAdicional;
			this.Tipo = enr.Tipo;
			this.Nombres = enr.Nombres;
			this.ApellidoPaterno = enr.ApellidoPaterno;
			this.ApellidoMaterno = enr.ApellidoMaterno;
			this.RazonSocial = enr.RazonSocial;
			this.IdPaisNacimiento = enr.IdPaisNacimiento;
			this.FechaNacimiento = enr.FechaNacimiento;
			this.Sexo = enr.Sexo;
			this.EstadoCivil = enr.EstadoCivil;
			this.IdCiudad = enr.IdCiudad;
			this.IdDivisionPais = enr.IdDivisionPais;
			this.IdTipoTelefono = enr.IdTipoTelefono;
			this.Telefono = enr.Telefono;
			this.Email = enr.Email;
			this.DireccionCalle = enr.DireccionCalle;
			this.DireccionNumero = enr.DireccionNumero;
			this.DireccionDepartamento = enr.DireccionDepartamento;
			this.FechaCreacion = enr.FechaCreacion;
			this.FechaCreacionStr = enr.FechaCreacionStr;
			this.FechaModificacion = enr.FechaModificacion;
			this.FechaModificacionStr = enr.FechaModificacionStr;
			this.IdPersonaConyuge = enr.IdPersonaConyuge;
			this.RegimenConyugal = enr.RegimenConyugal;
			this.CLI_IdCategoriaInversionista = enr.CLI_IdCategoriaInversionista;
			this.CLI_IdTipoCliente = enr.CLI_IdTipoCliente;
			this.CLI_IdTipoClienteCMF = enr.CLI_IdTipoClienteCMF;
			this.CLI_IdEmpleadoEjecutivo = enr.CLI_IdEmpleadoEjecutivo;
			this.CLI_CodigoAdicionalHolding = enr.CLI_CodigoAdicionalHolding;
			this.CLI_IdClaseCliente = enr.CLI_IdClaseCliente;
			this.CLI_ClienteFinal = enr.CLI_ClienteFinal;
			this.POR_IdPerfilComercial = enr.POR_IdPerfilComercial;
			this.POR_IdPersonaBrokerExtranjero = enr.POR_IdPersonaBrokerExtranjero;
			this.EMP_CodigoEmpleado = enr.EMP_CodigoEmpleado;
			this.EMP_CodigoTipoCargoEmpleado = enr.EMP_CodigoTipoCargoEmpleado;
			this.EMP_IdEmpleadoSupervisor = enr.EMP_IdEmpleadoSupervisor;
			this.EMP_IdSucursal = enr.EMP_IdSucursal;
			this.EMP_FechaInicioCargo = enr.EMP_FechaInicioCargo;
			this.EMP_FechaTerminoCargo = enr.EMP_FechaTerminoCargo;
			this.CNT_CodigoInscripcion = enr.CNT_CodigoInscripcion;
			this.CNT_Emisor = enr.CNT_Emisor;
			this.CNT_Depositos = enr.CNT_Depositos;
			this.CNT_Bolsa = enr.CNT_Bolsa;
			this.CNT_Corredora = enr.CNT_Corredora;
			this.CNT_CamaraDeCompensacion = enr.CNT_CamaraDeCompensacion;
			this.CNT_Banco = enr.CNT_Banco;
			this.CNT_Boveda = enr.CNT_Boveda;
			this.PRO_IdAreaProveedor = enr.PRO_IdAreaProveedor;
			this.EMP_CargoIndefinido = enr.EMP_CargoIndefinido;
			this.EsRutInterno = enr.EsRutInterno;
			this.TipoEnrolamiento = enr.TipoEnrolamiento;
			this.CLI_AreaCliente = enr.CLI_AreaCliente;
		}
	}
	
	public static class DatosPersona{
		public Long IdPersona;
		public String Tipo;
		public String Nombres;
		public String ApellidoPaterno;
		public String ApellidoMaterno;
		public String DocumentoIdentidad;
		public String IdTipoDocumentoIdentidad;
		public String RazonSocial;
		public Date FechaNacimiento;
		public String Sexo;
		public String RegimenConyugal;
		public String EstadoCivil;
		public Long IdProfesion;
		public Long IdPersonaConyuge;
		public Long IdPaisNacimiento;
		public Long IdPaisResidencia;
		public Long IdPaisNacionalidadPrincipal;
		public String IdSituacionLaboral;
	}
	
	public static class DatosCliente extends DatosPersona{
		public Long IdCliente;
		public String IdEstado;
	}

	public static class EntidadCrearClienteUpdate {
		public long p_IdDocumentoIdentidadAdicional;
		public long p_IdPersona;
	}

	public static class Ids {
		public long Id;
	}
	
	public static class Canal {
		public String IdTipoCanalIngreso;
		public String CreaUsuarioCliente;
	}

	public static class ErrorDetalleCall {
		public String Codigo;
		public String Descripcion;
	}

	public static class AdvertenciaDetalleCall {
		public String Codigo;
		public String RequiereAutorizacion;
		public String Descripcion;
	}

	public static class EntidadCrearClienteUpdateSocioNegocio {
		public long IdSocioDeNegocio;
		public long IdCliente;
	}

	public static class EntidadCrearClienteUpdateEnrolamiento {
		public long IdEnrolamiento;
		public long IdCliente;
	}

	public static class IndicadorOTC {
		public long IdTipoIndicador;
	}

	public static class IndicadorCMF {
		public long IdIndicador;
		public long IdTipoIndicador;

	}

	public static class IndicadorHerencia {
		public long IdEmpresa;
		public long IdPersona;
	}

	public static class IndicadorCliente {
		public long IdIndicador;
		public long IdCliente;
		public String IdTipoEntidad;
		public long IdTipoIndicador;
		public long IdEmpresa;
		public String ValorIndicador;
	}

	public static class NotificacionCliente {
		public Long IdSecuencia;
		public String NombreCompleto;
		public Long IdDireccionElectronicaPrincipal;
		public Long IdEmpleadoEjecutivo;
		public String IdTipoCliente;
		public String CorreoCliente;
		public Long IdPersona;
		public String TelefonoCliente;
		public String TipoDocumentoIdentidad;
		public String RutCavali;
		public String DocumentoIdentidad;
		public Long IdPersonaAgente;
		public Long IdDireccionAgente;
		public String Agente;
		public String Corredora;
		public String DireccionCorredora;
		public String RutCorredora;
		public String AtencionCliente;

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}


    
    
	public static class Parametros extends BcsPojo{
		public String NombreParametro;
		public String Valor;
		Parametros(){
			this.NombreParametro ="";
			this.Valor ="";
		}
		Parametros(String NombreParametro,String Valor){
			this.NombreParametro =NombreParametro;
			this.Valor =Valor;
		}
	}
	
	
	public static class SelectPersona
	{
		public Long		IdPersona			= 0L;		
		public Long		IdSocioDeNegocio	= 0L;
		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	public static class Ejecutivo{
		public Long IdEmpleado;
		public String NombreCompleto;
	}
	
	public static class TipoClienteCMF{
		public Long IdDetalleIndicador;
		public String DetalleIndicador;
	}
	
	public static class Area{
		public String Codigo;
		public String Descripcion;
	}
	
	public static class EntradaCrearModificarPersona{		
		public Long	IdEmpresa = 0L;
		public Long IdUsuario = 0L;
		public String IdModulo = "";
		public String FechaSistema = "";
		public String UsaReglasNegocio = "N";
		public String IdTipoCanal = "";
	}
	
	public static class SalidaCrearModificarPersona{
		public Boolean PERSONA_CREADA = false;
		public Long PERSONA_ERRORESCONTADOR = 0L;
		public Long PERSONA_REFID = 0L;
		public Long PERSONA_IDSOCIONEGOCIO = 0L;
	}
	
	public static class PortafolioUpdate extends BcsPojo{
		public Long IdPortafolio;
		public Long IdDireccionEnvioCorrespondencia;
		public Long IdTelefonoContacto;
		
		public PortafolioUpdate() {
			this.IdDireccionEnvioCorrespondencia = 0L;
			this.IdTelefonoContacto = 0L;
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaServicioCrearCliente in = gson.fromJson(jin.toString(), EntradaServicioCrearCliente.class);
		SalidaServicioCrearCliente out = logica(conn, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public SalidaServicioCrearCliente logica(Connection conn, EntradaServicioCrearCliente in) throws Exception {
		logger.info("INICIO: CrearCliente");
		String fechaSistema = Utils.getFechaSistema(conn, Utils.getSchema(), in.p_IdEmpresa);		
		String bloqueoPorDefecto;
		String bloqueoPorDefectoEnrFull;
		String bloqueoPorDefectoEnrExp;
		
		if(in.p_IdTipoCanal.isEmpty() || in.p_IdTipoCanal.contains("APP") || in.p_IdTipoCanal.contains("WBS") || in.p_IdTipoCanal.contains("NUEVA WEB SURA")){
			bloqueoPorDefecto = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "SOCIODENEGOCIO", "CLI.BloqueoPorDefectoWS");
			bloqueoPorDefectoEnrFull = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa,"SOCIODENEGOCIO", "CLI.BloqueoPorDefectoWS");
			bloqueoPorDefectoEnrExp = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa,"SOCIODENEGOCIO", "CLI.BloqueoPorDefectoWS");
			
		}else {
			bloqueoPorDefecto = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa,"SOCIODENEGOCIO", "CLI.BloqueoPorDefecto");
			bloqueoPorDefectoEnrFull = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa,"SOCIODENEGOCIO", "CLI.BloqueoPorDefectoEnrolamientoFull");
		    bloqueoPorDefectoEnrExp = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa,"SOCIODENEGOCIO", "CLI.BloqueoPorDefectoEnrolamientoExpress");
			
		}
		String codigoPais = Optional
				.ofNullable(Utils.getConfiguracion(conn, Utils.getSchema(), in.p_IdEmpresa, "PLA.LocalizacionSistema"))
				.orElse("CL");
		String crearUsuarioAutomaticamente = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa,"SOCIODENEGOCIO", "CLI.CrearUsuarioAutomatico");

		SalidaServicioCrearCliente salida = new SalidaServicioCrearCliente();
		SQL sql = new SQL();
		if (!in.p_ConsultarEnrolamiento.isEmpty()) {

			in.p_ConsultarEnrolamiento = asignarCamposPorDefecto(conn, in);

			SalidaAdministrarBorrador salidaAsignarPerfilComercialPorDefecto = new SalidaAdministrarBorrador();
			asignarPerfilComercialPorDefecto(conn, in.p_ConsultarEnrolamiento, in.p_IdEmpresa,salidaAsignarPerfilComercialPorDefecto);
			salida.P_OUT_ERRORESCONTADOR += salidaAsignarPerfilComercialPorDefecto.P_OUT_ERRORESCONTADOR;
			List<ErrorDetalle> mensajesError = obtenerMensajesError(salidaAsignarPerfilComercialPorDefecto.P_OUT_MENSAJES);
			salida.P_OUT_ERRORESDETALLE.addAll(mensajesError);
			if (salida.P_OUT_ERRORESCONTADOR > 0) {
				return salida;
			}

			SalidaAdministrarBorrador salidaAsignarTipoClientePorDefecto = new SalidaAdministrarBorrador();
			asignarTipoClientePorDefecto(conn, in.p_ConsultarEnrolamiento, in.p_IdEmpresa,salidaAsignarTipoClientePorDefecto);
			salida.P_OUT_ERRORESCONTADOR += salidaAsignarTipoClientePorDefecto.P_OUT_ERRORESCONTADOR;
			mensajesError = obtenerMensajesError(salidaAsignarTipoClientePorDefecto.P_OUT_MENSAJES);
			salida.P_OUT_ERRORESDETALLE.addAll(mensajesError);
			if (salida.P_OUT_ERRORESCONTADOR > 0) {
				return salida;
			}

			SalidaValidarCamposObligatorios salidaValidarCamposObligatorios = validarCamposObligatoriosEntrada(conn,in);
			salida.P_OUT_ERRORESCONTADOR = salidaValidarCamposObligatorios.P_OUT_ERRORESCONTADOR;
			mensajesError = obtenerMensajesError(salidaValidarCamposObligatorios.P_OUT_MENSAJES);
			salida.P_OUT_ERRORESDETALLE.addAll(mensajesError);

			if (salida.P_OUT_ERRORESCONTADOR > 0) {
				return salida;
			}

			List<SocioNegocioUtils.ConfiguracionPortafolioDefecto> configuracionPortafolios = SocioNegocioUtils.obtenerConfiguracionPortafolioDefecto(conn);
			
			if (configuracionPortafolios.size() > 0) {
				String maximoCodigoPortafolio = "";
				
				try {
					maximoCodigoPortafolio = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "SOCIODENEGOCIO", "POR.MaximoCodigoPortafolio");
				} catch (Exception e) {
					logger.warn("No se pudo obtener el parametro POR.MaximoCodigoPortafolio. Usando valor por defecto vacio.", e);
				}
				
				if (!maximoCodigoPortafolio.isEmpty()) {
					Pattern regexCodigoPortafolio = Pattern.compile(maximoCodigoPortafolio);
					List<SocioNegocioUtils.ConfiguracionPortafolioDefecto> portafoliosInvalidos = configuracionPortafolios
							.stream()
							.filter(port -> !port.Proposito.equalsIgnoreCase("PROPOSITOAPV") &&
									!regexCodigoPortafolio.matcher(port.CodigoPortafolio).matches())
							.collect(Collectors.toList());
					
					if (portafoliosInvalidos.size() > 0) {
						logger.debug("CODIGO PORTAFOLIO NO CUMPLE CON PARAMETRO POR.MaximoCodigoPortafolio");
						String codigoError = "CODIGO_PORTAFOLIO_NO_VALIDO";
						salida.addError(codigoError, Diccionario.getMensaje(conn,in.p_IdEmpresa, codigoError).msg());
						return salida;
					}
				}
			}
			
			HashMap <String, Object> paramsAQuery = new HashMap <String, Object> ();
			List<Long> IdsEjecutivos = new ArrayList<Long>();
			List<Long> IdsTipoClienteCMF = new ArrayList<Long>();
			List<String> IdsArea = new ArrayList<String>();
			
			for(Enrolamiento clien : in.p_ConsultarEnrolamiento) {
				if(clien.CLI_IdEmpleadoEjecutivo!=null && !(clien.CLI_IdEmpleadoEjecutivo.compareTo(0L) == 0) && !IdsEjecutivos.contains(clien.CLI_IdEmpleadoEjecutivo))IdsEjecutivos.add(clien.CLI_IdEmpleadoEjecutivo);
				if(clien.CLI_IdTipoClienteCMF!=null && !(clien.CLI_IdTipoClienteCMF.compareTo(0L) == 0) && !IdsTipoClienteCMF.contains(clien.CLI_IdTipoClienteCMF))IdsTipoClienteCMF.add(clien.CLI_IdTipoClienteCMF);
				if(clien.CLI_AreaCliente!=null && !(clien.CLI_AreaCliente.compareToIgnoreCase("") == 0) && !IdsArea.contains(clien.CLI_AreaCliente))IdsArea.add(clien.CLI_AreaCliente);
			}
			
			paramsAQuery.put("IdEjecutivo",IdsEjecutivos);
			HashMap<String, Object> hEjecutivo = obtenerhashListadoEjecutivo(conn, sql, paramsAQuery, IdsEjecutivos);
			
			paramsAQuery.put("IdDetalleIndicador",IdsTipoClienteCMF);
			HashMap<String, Object> hTiploClienteCMF = obtenerhashListadoClienteCMF(conn, sql, paramsAQuery, IdsTipoClienteCMF);
			
			paramsAQuery.put("Codigo", IdsArea);
			HashMap<String, Object> hArea = obtenerhashListadoAreas(conn, sql, paramsAQuery, IdsArea);

			for (Enrolamiento cliente : in.p_ConsultarEnrolamiento) {
				// RUTINA PARA COMPROBAR VIABILIDAD DE LA CREACION DEL SOCIO
				ValidarDocumentoIdentidad validarDocumentoIdentidad1 = new ValidarDocumentoIdentidad();
				SalidaValidarDocumentoIdentidad documentoValido = validarDocumentoIdentidad1.logica(conn,in.p_IdEmpresa, cliente.IdTipoEntidad, cliente.IdTipoDocumentoIdentidad, cliente.DocumentoIdentidad, cliente.Tipo);
				if (documentoValido.Valido.equals("S")) {
					logger.info("INICIO: Revision Portafolio");
					long idCliente = Utils.getSecuencia(conn, "BSCL.SocioNegocio.BD.Secuencias::Cliente", (long) 1).get(0);
					
					EntradaRevisaDatosPortafolio entradaRevisaDatosPortafolio = new EntradaRevisaDatosPortafolio();
					entradaRevisaDatosPortafolio.p_IdEmpresa = in.p_IdEmpresa;
					entradaRevisaDatosPortafolio.p_IdUsuario = in.p_IdUsuario;
					entradaRevisaDatosPortafolio.p_IdModulo = in.p_IdModulo;
					entradaRevisaDatosPortafolio.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
					entradaRevisaDatosPortafolio.p_IdCliente = idCliente;
					entradaRevisaDatosPortafolio.p_IdPerfilComercialBase = cliente.POR_IdPerfilComercial;
					RevisaDatosPortafolio revisaDatosPortafolio = new RevisaDatosPortafolio();
					SalidaRevisaDatosPortafolio salidaRevisaDatosPortafolio = revisaDatosPortafolio.logica(conn,entradaRevisaDatosPortafolio);
	
					if (salidaRevisaDatosPortafolio.P_OUT_ERRORESCONTADOR > 0) {
						for (MensajeSalida mensaje : salidaRevisaDatosPortafolio.P_OUT_MENSAJES) {
							if (MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo)) {
								salida.addError(mensaje.codigo, mensaje.descripcion);
							}
	
							if (MensajeSalida.MENSAJE_ADVERTENCIA.equals(mensaje.tipo)) {
								salida.addAdvertencia(mensaje.codigo, mensaje.descripcion);
							}
						}
						break;
					}
					logger.info("TERMINO: Revision Portafolio");
	
					salida.P_OUT_ERRORESCONTADOR = 0;
	
					if (cliente.IdTipoEntidad != null && cliente.IdTipoEntidad.equals("CLIENTE")) {
						ValidarDocumentoIdentidad validarDocumentoIdentidad = new ValidarDocumentoIdentidad();
	
						// evaluar si el documento de identidad adicional ya existe
						Optional<SalidaValidarDocumentoIdentidad> documentoAdicionalValido = null;
						if (codigoPais.equals("PE")) {
							validarDocumentoIdentidad = new ValidarDocumentoIdentidad();
							documentoAdicionalValido = Optional.ofNullable(validarDocumentoIdentidad.logica(conn,
									in.p_IdEmpresa, cliente.IdTipoEntidad, cliente.IdTipoDocumentoIdentidadAdicional,
									cliente.DocumentoIdentidadAdicional, cliente.Tipo));
							if (documentoAdicionalValido.isPresent() && documentoAdicionalValido.get().Valido.equals("N")) {
								salida.addError("UI_EXISTE_DOCUMENTO_ADICIONAL", Diccionario.getMensaje(conn,
										in.p_IdEmpresa, "UI_EXISTE_DOCUMENTO_ADICIONAL", new HashMap<>()));
								return salida;
							}
						}
	
						//Crear o Modificar Persona
						EntradaCrearModificarPersona entradaCrearModificarPersona = new EntradaCrearModificarPersona();
						entradaCrearModificarPersona.IdEmpresa = in.p_IdEmpresa;
						entradaCrearModificarPersona.IdUsuario = in.p_IdUsuario;
						entradaCrearModificarPersona.IdModulo = in.p_IdModulo;
						entradaCrearModificarPersona.UsaReglasNegocio = in.p_UsaReglasNegocio;
						entradaCrearModificarPersona.FechaSistema = fechaSistema;
						entradaCrearModificarPersona.IdTipoCanal = in.p_IdTipoCanal;
						SalidaCrearModificarPersona salidaCrearModificarPersona = SalidaCrearModificarPersona (conn, entradaCrearModificarPersona, cliente, salida);
	
						// evaluar si se creo la nueva persona errores = 0, o evaluar si la persona ya existia REFID != 0
						if (salidaCrearModificarPersona.PERSONA_ERRORESCONTADOR == 0 || salidaCrearModificarPersona.PERSONA_REFID != 0) {
							// ingresar cliente
							long idPersona = salidaCrearModificarPersona.PERSONA_REFID;
							long idSocioNegocio = salidaCrearModificarPersona.PERSONA_IDSOCIONEGOCIO;
	
							String requiereAutorizacion = Utils.getRequiereEventoAutorizacion(conn, in.p_IdEmpresa,
									EnrolamientoUtils.obtenerTipoEnrolamiento(cliente.TipoEnrolamiento));
							HashMap<String, Object> params = new HashMap<String, Object>();
							params.put("IdEmpleadoEjecutivo", cliente.CLI_IdEmpleadoEjecutivo);
							
							logger.debug("INICIO::SQL_ConsultarIdPersonaUsuario");
							List<Ids> idPersonaUsuario = sql.query(conn, SQL_ConsultarIdPersonaUsuario, params, Ids.class);
							logger.debug("FIN::SQL_ConsultarIdPersonaUsuario");
							
							EntidadCliente entidadCliente = new EntidadCliente();
							entidadCliente.setFromEnrolamiento(cliente);
							entidadCliente.IdCliente = idCliente;
							entidadCliente.IdPersona = idPersona;
	
							String bloquearPorDefecto = bloqueoPorDefecto;
	
							if ("FULL".equals(cliente.TipoEnrolamiento)) {
								bloquearPorDefecto = bloqueoPorDefectoEnrFull;
							}
	
							if ("EXPRESS".equals(cliente.TipoEnrolamiento)) {
								bloquearPorDefecto = bloqueoPorDefectoEnrExp;
							}
	
							if (cliente.CLI_ClienteBloqueado == null || cliente.CLI_ClienteBloqueado.equals("")) {
								entidadCliente.IdEstado = (bloquearPorDefecto.equals("S")) ? "BLO" : "ACT";
							} else {
								entidadCliente.IdEstado = (cliente.CLI_ClienteBloqueado.equals("S")) ? "BLO" : "ACT";
							}
							
							Long idSegmento = obtenerSegmentoPorDefecto(conn);
							if(idSegmento == -1L) {
								salida.addError("ERROR_ID_SEGMENTO","No existe Id Segmento");
								return salida;
							}
							entidadCliente.IdSegmentoCliente = cliente.CLI_IdSegmentoCliente > 0 ? cliente.CLI_IdSegmentoCliente  : idSegmento;
							
							entidadCliente.EsEnrolado = (requiereAutorizacion.equals("S")) ? "P" : "S";
							entidadCliente.IdPlantillaLimiteExtra = "CLI_" + idCliente;
							entidadCliente.IdPersonaUsuario = (idPersonaUsuario != null && idPersonaUsuario.size() > 0)	? idPersonaUsuario.get(0).Id : 0;
							logger.debug("INICIO::SQL_CREAR_CLIENTE");
							int[] resCliente = sql.batch(conn, SQL_CREAR_CLIENTE, Arrays.asList(entidadCliente),
									EntidadCliente.class);
							logger.debug("FIN::SQL_CREAR_CLIENTE");
							
							//Preparandonos para Log
							AgregarLog.InAgregarLog entradaAgregarLog;
							
							if (resCliente != null && resCliente.length > 0 && resCliente[0] == 1) {
								salida.P_OUT_REFIDCLIENTE = idCliente;
								salida.P_OUT_DOCUMENTOIDENTIDAD = entidadCliente.DocumentoIdentidad;
								logger.info("RES IDCLI: " + idCliente);
								
								entradaAgregarLog = prepararLog(conn, sql, entidadCliente, in, fechaSistema, hEjecutivo, hTiploClienteCMF, hArea);
								
							} else {
								salida.addError("CLIENTE_NO_PUEDE_SER_INGRESADO", "CLIENTE_NO_PUEDE_SER_INGRESADO");
								return salida;
							}
							
							List<Long> IdsPortafolios = new ArrayList<Long>();
						
							List<PortafolioUpdate> listaPortafolioUpdate = new ArrayList<>();
							List<DireccionFisicaPredeterminada> idDireccionFisicaPredeterminada = new ArrayList<>();
							List<TelefonoPredeterminado> IdTelefonoContactoPredeterminado = new ArrayList<>();
							
							if(idPersona > 0) {
								HashMap<String, Object> parametros = new HashMap<String, Object>();
								parametros.put("IdPersona", idPersona);
								
								idDireccionFisicaPredeterminada = sql.query(conn, SQL_SELECT_DIRECCION_FISICA_PREDETERMINADA, parametros,DireccionFisicaPredeterminada.class);
							
								IdTelefonoContactoPredeterminado = sql.query(conn, SQL_SELECT_TELEFONO_PREDETERMINADO, parametros,TelefonoPredeterminado.class);
							}
							
							for (SocioNegocioUtils.ConfiguracionPortafolioDefecto portafolioDefecto : configuracionPortafolios) {
								logger.debug("INICIO::CrearCliente.crearPortafolio");
								long inicioCrearPortafolio = System.currentTimeMillis();
	
								CrearPortafolio.EntradaCrearPortafolio entradaCrearPortafolio = new CrearPortafolio.EntradaCrearPortafolio();
								entradaCrearPortafolio.p_IdEmpresa = in.p_IdEmpresa;
								entradaCrearPortafolio.p_IdUsuario = in.p_IdUsuario;
								entradaCrearPortafolio.p_IdModulo = in.p_IdModulo;
								entradaCrearPortafolio.p_UsaReglasNegocio = "N";
								entradaCrearPortafolio.p_IdCliente = idCliente;
								entradaCrearPortafolio.p_IdPerfilComercialBase = cliente.POR_IdPerfilComercial;
								entradaCrearPortafolio.p_IdPersonaBrokerExtranjero = cliente.POR_IdPersonaBrokerExtranjero;
								entradaCrearPortafolio.p_AreaCliente = cliente.CLI_AreaCliente;
								entradaCrearPortafolio.p_Estado = "ACT";
								entradaCrearPortafolio.p_CodigoPortafolio = portafolioDefecto.CodigoPortafolio;
								entradaCrearPortafolio.p_PropositoPortafolio = portafolioDefecto.Proposito;
								entradaCrearPortafolio.p_DesdeEnrolamiento = "S";
	
								CrearPortafolio.SalidaCrearPortafolio salidaCrearPortafolio = new CrearPortafolio().logica(conn, entradaCrearPortafolio);
								if (salidaCrearPortafolio.P_OUT_ERRORESCONTADOR > 0) {
									salidaCrearPortafolio.P_OUT_ERRORESDETALLE.forEach(error -> salida.addError(error.Codigo, error.Descripcion));
									salidaCrearPortafolio.P_OUT_ADVERTENCIASDETALLE.forEach(adv -> salida.addError(adv.Codigo, adv.Descripcion));
								} else {
									salida.P_OUT_REFIDPORTAFOLIO = salidaCrearPortafolio.p_out_RefId;
									salida.P_OUT_PORTAFOLIO_IDS.add(salidaCrearPortafolio.p_out_RefId);
									IdsPortafolios = salida.P_OUT_PORTAFOLIO_IDS; 
									
									PortafolioUpdate portafolioUpdate = new PortafolioUpdate();
									if(salidaCrearPortafolio.p_out_RefId != null && salidaCrearPortafolio.p_out_RefId > 0 && idPersona > 0) { 
										
										portafolioUpdate.IdPortafolio = salidaCrearPortafolio.p_out_RefId;										
					
										if (idDireccionFisicaPredeterminada != null && idDireccionFisicaPredeterminada.size() > 0) {
											portafolioUpdate.IdDireccionEnvioCorrespondencia = idDireccionFisicaPredeterminada.get(0).IdDireccionFisica;
										}
											
										if (IdTelefonoContactoPredeterminado != null && IdTelefonoContactoPredeterminado.size() > 0) {
											portafolioUpdate.IdTelefonoContacto = IdTelefonoContactoPredeterminado.get(0).IdTelefonoContacto;
										}
										
										listaPortafolioUpdate.add(portafolioUpdate);
									}
								}
								
								long finCrearPortafolio = System.currentTimeMillis();
								logger.debug("INICIO::CrearCliente.crearPortafolio {}", finCrearPortafolio - inicioCrearPortafolio);
							}

							if (listaPortafolioUpdate != null && !listaPortafolioUpdate.isEmpty()) {
								int[] resUpdatePortafolio = sql.batch(conn, SQL_UPDATE_DIRECCION_FISICA_Y_TELEFONO_PORTAFOLIO, listaPortafolioUpdate, PortafolioUpdate.class);
							}
							
							registrarLog(conn, entradaAgregarLog, IdsPortafolios);
	
							if (salida.P_OUT_ERRORESCONTADOR == 0) {
								HashMap<String, Object> paramsIn = new HashMap<>();
								HashMap<String, Class> paramsOut = new HashMap<>();
								HashMap<String, Object> resultSP = new HashMap<>();
	
								// Actualizar socio de negocio con IdCliente
								EntidadCrearClienteUpdateSocioNegocio updateSocioNegocio = new EntidadCrearClienteUpdateSocioNegocio();
								updateSocioNegocio.IdSocioDeNegocio = idSocioNegocio;
								updateSocioNegocio.IdCliente = idCliente;
								logger.debug("INICIO::SQL_CREAR_CLIENTE_SOCIONEGOCIO_UPDATE");
								int[] resUpdateSocioNegocio = sql.batch(conn, SQL_CREAR_CLIENTE_SOCIONEGOCIO_UPDATE,
										Arrays.asList(updateSocioNegocio), EntidadCrearClienteUpdateSocioNegocio.class);
								logger.debug("FIN::SQL_CREAR_CLIENTE_SOCIONEGOCIO_UPDATE");
								if (resUpdateSocioNegocio == null
										|| (resUpdateSocioNegocio.length > 0 && resUpdateSocioNegocio[0] == 0)) {
									salida.addError("UI_ERROR_ACTUALIZAR_SOCIONEGOCIO", "UI_ERROR_ACTUALIZAR_SOCIONEGOCIO");
								}
	
								// Se heredan indicadores desde persona hacia cliente
								IndicadorHerencia inserHerenciaIndicadores = new IndicadorHerencia();
								inserHerenciaIndicadores.IdEmpresa = in.p_IdEmpresa;
								inserHerenciaIndicadores.IdPersona = idPersona;
								logger.debug("INICIO::SQL_INSERTAR_INDICADOR_HERENCIA_CLIENTE");
								int[] resIndicadorHerenciaCliente = sql.batch(conn, SQL_INSERTAR_INDICADOR_HERENCIA_CLIENTE,
										Arrays.asList(inserHerenciaIndicadores), IndicadorHerencia.class);
								logger.debug("FIN::SQL_INSERTAR_INDICADOR_HERENCIA_CLIENTE");
								if (resIndicadorHerenciaCliente != null && resIndicadorHerenciaCliente.length > 0
										&& resIndicadorHerenciaCliente[0] == 1) {
									logger.info("Sin indicadores heredados.");
								}
	
								// Ingresar el Indicador Tipo Cliente CMF
								if (cliente.CLI_IdTipoClienteCMF != 0) {
	
									String CodigoTipoIndicador = (cliente.Tipo.equals("N")) ? "TCLICMF" : "TCLICMFJ";
	
									paramsIn = new HashMap<String, Object>();
									paramsIn.put("CodigoTipoIndicador", CodigoTipoIndicador);
									paramsIn.put("IdEmpresa", in.p_IdEmpresa);
									logger.debug("INICIO::SQL_CONSULTAR_INDICADOR");
									List<IndicadorCMF> resultado = sql.query(conn, SQL_CONSULTAR_INDICADOR, paramsIn,IndicadorCMF.class);
									logger.debug("FIN::SQL_CONSULTAR_INDICADOR");
									if (!resultado.isEmpty()) {
										long idIndicador = Utils
												.getSecuencia(conn, "BSCL.Plataforma.BD.Secuencias::Indicador", (long) 1)
												.get(0);
										long idTipoIndicador = resultado.get(0).IdTipoIndicador;
										IndicadorCliente indicadorCliente = new IndicadorCliente();
										indicadorCliente.IdIndicador = idIndicador;
										indicadorCliente.IdCliente = idCliente;
										indicadorCliente.IdTipoEntidad = cliente.IdTipoEntidad;
										indicadorCliente.IdTipoIndicador = idTipoIndicador;
										indicadorCliente.IdEmpresa = in.p_IdEmpresa;
										indicadorCliente.ValorIndicador = cliente.CLI_IdTipoClienteCMF.toString();
										logger.debug("INICIO::SQL_INSERTAR_INDICADOR_CLIENTE");
										int[] resIndicadorCliente = sql.batch(conn, SQL_INSERTAR_INDICADOR_CLIENTE,Arrays.asList(indicadorCliente), IndicadorCliente.class);
										logger.debug("FIN::SQL_INSERTAR_INDICADOR_CLIENTE");
										if (resIndicadorCliente != null && resIndicadorCliente.length > 0
												&& resIndicadorCliente[0] == 1) {
											logger.info("Indicador creado");
										}
									}
								}
	
								// si el modelo de cliente tiene alguna preferencia de orden "operacuenta rv o
								// rf/if" activada, crear cuenta de custodia
								if (codigoPais.equals("PE") && entidadCliente.CLI_IdTipoCliente != null) {
									// obtener preferencias de negocio del portafolio
									ConsultarIndicadores consultarIndicadores = new ConsultarIndicadores();
									ConsultarIndicadores.EntradaServicioIndicadores entradaConsultarIndicadores = new ConsultarIndicadores.EntradaServicioIndicadores();
									entradaConsultarIndicadores.p_IdEmpresa = in.p_IdEmpresa;
									entradaConsultarIndicadores.p_ModeloCliente = cliente.CLI_IdTipoCliente;
									entradaConsultarIndicadores.p_IdTipoEntidad = "PORTAFOLIO";
									ConsultarIndicadores.SalidaServicioIndicadores salidaConsultarIndicadores = consultarIndicadores
											.logica(conn, entradaConsultarIndicadores);
									HashMap<String, Indicador> indicadores = new HashMap<>();
									for (Indicador indicador : salidaConsultarIndicadores.p_out_Indicadores) {
										indicadores.put(indicador.CodigoTipoIndicador, indicador);
									}
	
									boolean existeAlgunIndicador = false;
									String tipoCustodia = "";
									String tipoCuenta = "";
									// obtener valores por defecto
									ConsultarValoresPorDefecto consultarValoresDefecto = new ConsultarValoresPorDefecto();
									EntradaConsultarValoresPorDefecto entradaValoresDefecto = new EntradaConsultarValoresPorDefecto();
									entradaValoresDefecto.p_IdEmpresa = in.p_IdEmpresa;
									entradaValoresDefecto.p_TipoEntidad = "ENROLAMIENTO";
									entradaValoresDefecto.p_IdTipoCanalIngreso = "SIS";
									HashMap<String, String> valoresDefecto = consultarValoresDefecto.logica(conn, entradaValoresDefecto);
	
									// obtener indicadores ORDOCUSTRV y ORDOCUSTRF
									if ((indicadores.get("ORDOCUSTRV") != null
											&& indicadores.get("ORDOCUSTRV").ValorIndicador.equals("S"))
											|| (indicadores.get("ORDOCUSTRF") != null
													&& indicadores.get("ORDOCUSTRF").ValorIndicador.equals("S"))) {
										// obtener las llaves de los valores por defecto
										tipoCustodia = "IdTipoCustodia";
										tipoCuenta = "IdTipoCuentaCustodia";
										existeAlgunIndicador = true;
									}
	
									// obtener indicadores ORDCUENTAPROPIA y ORDCUENTABANCO
									if ((indicadores.get("ORDCUENTAPROPIA") != null
											&& indicadores.get("ORDCUENTAPROPIA").ValorIndicador.equals("S"))
											|| (indicadores.get("ORDCUENTABANCO") != null
													&& indicadores.get("ORDCUENTABANCO").ValorIndicador.equals("S"))) {
	
										tipoCustodia = (indicadores.get("ORDCUENTAPROPIA").ValorIndicador.equals("S")) ? "IdTipoCustodiaPropia" : "IdTipoCustodiaBanco";
										tipoCuenta = (indicadores.get("ORDCUENTAPROPIA").ValorIndicador.equals("S")) ? "IdTipoCustodiaMandante" : "IdTipoCustodiaDepositante";
										existeAlgunIndicador = true;
									}
	
									// Flujo para habilitar requisito al enrolar cliente con Rut Interno
									if ("S".equals(cliente.EsRutInterno)) {
										paramsIn = new HashMap<String, Object>();
										paramsIn.put("IdEmpresa", in.p_IdEmpresa);
										paramsIn.put("CodigoTipoIndicador", CODIGO_OPERA_MERCADO_OTC);
										logger.debug("INICIO::SQL_CONSULTAR_INDICADOR_OTC");
										List<IndicadorOTC> resultado = sql.query(conn, SQL_CONSULTAR_INDICADOR_OTC,	paramsIn, IndicadorOTC.class);
										logger.debug("FIN::SQL_CONSULTAR_INDICADOR_OTC");
										if (!resultado.isEmpty()) {
											long idIndicador = Utils.getSecuencia(conn,"BSCL.Plataforma.BD.Secuencias::Indicador", (long) 1).get(0);
											long idTipoIndicador = resultado.get(0).IdTipoIndicador;
											IndicadorCliente indicadorCliente = new IndicadorCliente();
											indicadorCliente.IdIndicador = idIndicador;
											indicadorCliente.IdCliente = idCliente;
											indicadorCliente.IdTipoEntidad = cliente.IdTipoEntidad;
											indicadorCliente.IdTipoIndicador = idTipoIndicador;
											indicadorCliente.IdEmpresa = in.p_IdEmpresa;
											indicadorCliente.ValorIndicador = cliente.EsRutInterno;
											logger.debug("INICIO::SQL_INSERTAR_INDICADOR_CLIENTE");
											int[] resIndicadorCliente = sql.batch(conn, SQL_INSERTAR_INDICADOR_CLIENTE,Arrays.asList(indicadorCliente), IndicadorCliente.class);
											logger.debug("FIN::SQL_INSERTAR_INDICADOR_CLIENTE");
											if (resIndicadorCliente != null && resIndicadorCliente.length > 0
													&& resIndicadorCliente[0] == 1) {
												logger.info("Requisito Opera Mercado OTC habilitado.");
											}
										}
									}
	
									if (existeAlgunIndicador) {
										// crear cuenta custodia
										logger.debug("INICIO::CrearCuentaCustodioMasivo");
										CrearCuentaCustodioMasivo crearCuentaCustodioMasivo = new CrearCuentaCustodioMasivo();
										EntradaCrearCuentaCustodioMasivo entradaCuentaCustodio = new EntradaCrearCuentaCustodioMasivo();
										
										entradaCuentaCustodio.p_IdEmpresa = in.p_IdEmpresa;
										entradaCuentaCustodio.p_IdUsuario = in.p_IdUsuario;
										entradaCuentaCustodio.p_IdModulo = in.p_IdModulo;
										entradaCuentaCustodio.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
										
										CuentaCustodio cuentaCustodio = new CuentaCustodio();
										cuentaCustodio.IdPortafolio = salida.P_OUT_REFIDPORTAFOLIO;
										cuentaCustodio.IdBoveda = valoresDefecto.get("IdBovedaCuentaCustodia");
										cuentaCustodio.Codigo = cliente.DocumentoIdentidadAdicional;
										cuentaCustodio.IdTipoCuentaCustodia = valoresDefecto.get(tipoCuenta);
										cuentaCustodio.IdTipoCustodia = valoresDefecto.get(tipoCustodia);
										cuentaCustodio.IdModeloCustodia = valoresDefecto.get("IdModeloCustodia");
										
										entradaCuentaCustodio.p_CuentasCustodio.add(cuentaCustodio);
										
										SalidaCrearCuentaCustodioMasivo salidaCuentaCustodio = crearCuentaCustodioMasivo.logica(conn, entradaCuentaCustodio);
										logger.debug("FIN::CrearCuentaCustodioMasivo");
										
										// Procesar mensajes de salida
										for (MensajeSalida mensaje : salidaCuentaCustodio.P_OUT_MENSAJES) {
											if (mensaje.tipo.equals(MensajeSalida.MENSAJE_ERROR)) {
												salida.addError(mensaje.codigo, mensaje.descripcion);
											} else if (mensaje.tipo.equals(MensajeSalida.MENSAJE_AUTORIZACION)) {
												salida.addAdvertencia("REQUIERE_AUTORIZACION_CUENTA_CUSTODIO",
													Diccionario.getMensaje(conn, in.p_IdEmpresa,"REQUIERE_AUTORIZACION_CUENTA_CUSTODIO", new HashMap<>()));
											} else if (mensaje.tipo.equals(MensajeSalida.MENSAJE_ADVERTENCIA)) {
												salida.addAdvertencia(mensaje.codigo, mensaje.descripcion);
											}
										}
									} else {
										salida.addError("UI_TIPO_CLIENTE_ERROR",
												"El Tipo de Cliente SAB, se encuentra mal configurado. Debe operar con tipo cuenta custodio, propio o banco.");
									}
	
								}
	
								// identificar si existe un documento de identidad adicional (PERU)
								Long idDocumentoIdentidadAdicional = null;
								if (codigoPais.equals("PE")) {
									if (documentoAdicionalValido.isPresent()
											&& documentoAdicionalValido.get().Valido.equals("S")) {
										// ingresar documento identidad adicional.
										idDocumentoIdentidadAdicional = Utils.getSecuencia(conn,"BSCL.SocioNegocio.BD.Secuencias::DocumentoIdentidad", (long) 1).get(0);
										CrearDocumentoIdentidad crearDocumentoIdentidad = new CrearDocumentoIdentidad();
										EntradaCrearDocumentoIdentidad entradaDocumentoIdentidad = new EntradaCrearDocumentoIdentidad();
										entradaDocumentoIdentidad.p_IdUsuario = in.p_IdUsuario;
										entradaDocumentoIdentidad.p_IdEmpresa = in.p_IdEmpresa;
										entradaDocumentoIdentidad.p_IdModulo = in.p_IdModulo;
										entradaDocumentoIdentidad.p_UsaReglasNegocio = "N";
	
										entradaDocumentoIdentidad.p_IdPersona = idPersona;
										entradaDocumentoIdentidad.p_IdTipoDocumentoIdentidad = cliente.IdTipoDocumentoIdentidadAdicional;
										entradaDocumentoIdentidad.p_IdTipoDocumentoIdentidadAsociado = cliente.IdTipoDocumentoIdentidadAdicional;
										entradaDocumentoIdentidad.p_DocumentoIdentidad = cliente.DocumentoIdentidadAdicional;
										entradaDocumentoIdentidad.p_TipoPersona = cliente.Tipo;
										SalidaCrearDocumentoIdentidad salidaDocumentoIdentidad = crearDocumentoIdentidad
												.logica(conn, entradaDocumentoIdentidad);
										if (salidaDocumentoIdentidad.P_OUT_ERRORESCONTADOR == 0) {
											idDocumentoIdentidadAdicional = salidaDocumentoIdentidad.p_out_RefId;
											logger.info("RES DOC ADI: " + idDocumentoIdentidadAdicional);
	
											// actualizar los ids de la persona con cada entidad que fue ingresada
											EntidadCrearClienteUpdate updatePersona = new EntidadCrearClienteUpdate();
											updatePersona.p_IdDocumentoIdentidadAdicional = (idDocumentoIdentidadAdicional != null) ? idDocumentoIdentidadAdicional : 0;
											updatePersona.p_IdPersona = idPersona;
											logger.debug("INICIO::SQL_CREAR_CLIENTE_DOCUMENTOIDENTIDADADICIONAL_UPDATE");
											sql.batch(conn, SQL_CREAR_CLIENTE_DOCUMENTOIDENTIDADADICIONAL_UPDATE,
													Arrays.asList(updatePersona), EntidadCrearClienteUpdate.class);
											logger.debug("FIN::SQL_CREAR_CLIENTE_DOCUMENTOIDENTIDADADICIONAL_UPDATE");
	
										} else {
											for (ErrorDetalle error : salidaDocumentoIdentidad.P_OUT_ERRORESDETALLE) {
												salida.addError(error.Codigo, error.Descripcion);
											}
											return salida;
										}
	
									} else {
										salida.addError("UI_EXISTE_DOCUMENTO_ADICIONAL", Diccionario.getMensaje(conn,in.p_IdEmpresa, "UI_EXISTE_DOCUMENTO_ADICIONAL", new HashMap<>()));
										return salida;
									}
								}
								// Generar Evento de autorizacion
								if (requiereAutorizacion.equals("S")) {
									CrearEventoAutorizacionMasivo evtAutorizacion = new CrearEventoAutorizacionMasivo();
	
									CrearEventoAutorizacionMasivo.ServiceEntrada entradaEvtAutorizacion = new CrearEventoAutorizacionMasivo.ServiceEntrada();
									entradaEvtAutorizacion.P_IDEMPRESA = in.p_IdEmpresa;
									entradaEvtAutorizacion.P_IDMODULO = in.p_IdModulo;
									entradaEvtAutorizacion.P_IDUSUARIO = in.p_IdUsuario;
									entradaEvtAutorizacion.P_SIMULAROPERACION = "N";
									entradaEvtAutorizacion.P_USAREGLASNEGOCIO = "N";
	
									CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS evento = new CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS();
									Enrolamiento enrolamientoAux = in.p_ConsultarEnrolamiento.parallelStream().findFirst().orElse(null);
									String tipoEvento = EnrolamientoUtils.obtenerTipoEnrolamiento(enrolamientoAux.TipoEnrolamiento);
									evento.IdTipoEvento = tipoEvento;
									evento.IdModuloSolicita = in.p_IdModulo;
									evento.IdUsuarioSolicita = in.p_IdUsuario;
									evento.Fecha = fechaSistema;
									evento.IdConcepto = tipoEvento;
									evento.NumeroOperacionConcepto = String.valueOf(idCliente);
									evento.FechaOperacionConcepto = fechaSistema;
									evento.IdCliente = idCliente;
									evento.Glosa = (cliente.Tipo.equals("J"))
											? cliente.DocumentoIdentidad + " / " + cliente.RazonSocial
											: cliente.DocumentoIdentidad + " / " + cliente.Nombres + " "
													+ cliente.ApellidoPaterno + " " + cliente.ApellidoMaterno;
	
									entradaEvtAutorizacion.P_TABLAEVENTOS = Arrays.asList(evento);
	
									CrearEventoAutorizacionMasivo.ServiceSalida salidaAut = evtAutorizacion.logica(conn,
											entradaEvtAutorizacion);
									salida.addAdvertencia("REQUIERE_AUTORIZACION_CLIENTE", Diccionario.getMensaje(conn,
											in.p_IdEmpresa, "REQUIERE_AUTORIZACION_CLIENTE", new HashMap<>()));
	
									for (CrearEventoAutorizacionMasivo.ObjADVERTENCIASDETALLE advertencia : salidaAut.P_OUT_ADVERTENCIASDETALLE) {
										salida.addAdvertencia(advertencia.Codigo, advertencia.Descripcion);
									}
									for (CrearEventoAutorizacionMasivo.ObjERRORESDETALLE error : salidaAut.P_OUT_ERRORESDETALLE) {
										salida.addError(error.Codigo, error.Descripcion);
									}
								} else if (salida.P_OUT_ERRORESCONTADOR == 0) {
									// si NO requiere autorizacion
									paramsIn = new HashMap<>();
									paramsIn.put("IdCliente", salida.P_OUT_REFIDCLIENTE);
									paramsIn.put("IdEmpresa", in.p_IdEmpresa);
									logger.debug("INICIO::SQL_SELECT_NOTIFICACION");
									List<NotificacionCliente> notificaciones = sql.query(conn, SQL_SELECT_NOTIFICACION,paramsIn, NotificacionCliente.class);
									logger.debug("FIN::SQL_SELECT_NOTIFICACION");
									if (notificaciones != null && !notificaciones.isEmpty()) {
										NotificacionCliente notificacion = notificaciones.get(0);
										if (notificacion.IdPersonaAgente != null) {
											String logoCorredorRutaPublica = "";
											ConsultarImagen consultarImagen = new ConsultarImagen();
											ConsultarImagenEntrada consultarImagenIn = new ConsultarImagenEntrada();
											consultarImagenIn.p_IdEmpresa = in.p_IdEmpresa;
											consultarImagenIn.p_imagenNombres.add("LOGO_CORREDOR");
											ConsultarImagenSalida consultarImagenOut = consultarImagen.logica(conn, consultarImagenIn);
											if (consultarImagenOut.OUT_IMAGENES.size() > 0 ) {
												logoCorredorRutaPublica = consultarImagenOut.OUT_IMAGENES.get(0).RutaPublica;
											}
											// Agrega notificacion
											AgregarEvento.ServiceEntrada notifIn = new AgregarEvento.ServiceEntrada();
											notifIn.P_IDEMPRESA = in.p_IdEmpresa;
											notifIn.P_IDUSUARIO = in.p_IdUsuario;
											notifIn.P_IDMODULO = in.p_IdModulo;
	
											List<ObjEVENTOS> eventosNotificacion = new ArrayList<>();
											List<ObjIDDIRECCIONES> direccionesNotificacion = new ArrayList<>();
	
											ObjEVENTOS evento = new ObjEVENTOS();
											evento.CodigoTipoEvento = "ALTACLIENTE";
											evento.FechaCreacionSistema = fechaSistema;
											evento.IdPersonaDestinatario = notificacion.IdPersona;
											evento.IdPersonaRemitente = notificacion.IdPersonaAgente;
											evento.IdPortafolio = 0L;
											evento.IdSecuencia = notificacion.IdSecuencia;
											eventosNotificacion.add(evento);
	
											// Valores generales
											notifIn.addValor(notificacion.IdSecuencia, "IDINTERNO",
													"" + notificacion.IdSecuencia)
													.addValor(notificacion.IdSecuencia, "CLIENTE",
															notificacion.NombreCompleto)
													.addValor(notificacion.IdSecuencia, "DOCUMENTOIDENTIDAD",
															notificacion.DocumentoIdentidad)
													.addValor(notificacion.IdSecuencia, "TIPODOCUMENTOIDENTIDAD",
															notificacion.TipoDocumentoIdentidad)
													.addValor(notificacion.IdSecuencia, "IDTIPOCLIENTE",
															notificacion.IdTipoCliente)
													.addValor(notificacion.IdSecuencia, "TELEFONO",
															notificacion.TelefonoCliente)
													.addValor(notificacion.IdSecuencia, "CORREDORA", notificacion.Corredora)
													.addValor(notificacion.IdSecuencia, "DIRECCIONCORREDORA",
															notificacion.DireccionCorredora)
													.addValor(notificacion.IdSecuencia, "RUTCORREDORA",
															notificacion.RutCorredora)
													.addValor(notificacion.IdSecuencia, "ATENCIONCLIENTE",
															notificacion.AtencionCliente)
													.addValor(notificacion.IdSecuencia, "AGENTE", notificacion.Agente)
													.addValor(notificacion.IdSecuencia, "LOGO_CORREDORA", logoCorredorRutaPublica);
	
											if (codigoPais.equals("PE")) {
												notifIn.addValor(notificacion.IdSecuencia, "RUTCAVALI",
														notificacion.RutCavali);
											}
	
											if (notificacion.IdDireccionAgente != null) {
												ObjIDDIRECCIONES dir1 = new ObjIDDIRECCIONES();
												dir1.IdDireccion = notificacion.IdDireccionAgente;
												dir1.IdSecuencia = notificacion.IdSecuencia;
												dir1.TipoDestinatario = "CC";
												direccionesNotificacion.add(dir1);
											}
	
											ObjIDDIRECCIONES dir2 = new ObjIDDIRECCIONES();
											dir2.IdDireccion = notificacion.IdDireccionElectronicaPrincipal;
											dir2.IdSecuencia = notificacion.IdSecuencia;
											dir2.TipoDestinatario = "TO";
	
											direccionesNotificacion.add(dir2);
	
											notifIn.P_EVENTOS = eventosNotificacion;
											notifIn.P_IDDIRECCIONES = direccionesNotificacion;
	
											if (notifIn.P_EVENTOS.size() > 0) {
												AgregarEvento notif = new AgregarEvento();
												notif.logica(conn, notifIn);
											}
										}
	
									}
	
								}
								
								String tipoCanal = in.p_IdTipoCanal.trim().isEmpty()?"WBS" : in.p_IdTipoCanal;
								String crearUsuarioAutomatico = "S";							
								
								HashMap<String,Object> paramscanal = new HashMap<>();
								paramscanal.put("IdTipoCanalIngreso", tipoCanal);
								List<Canal> canalingreso = sql.query(conn, SQL_CONSULTAR_CANAL, paramscanal, Canal.class);
								if (canalingreso.size() == 0) {
									crearUsuarioAutomatico = "S";
								}
								else								{
									crearUsuarioAutomatico = canalingreso.get(0).CreaUsuarioCliente;
								}
								
								HashMap<String, Object> params_ = new HashMap<>();
								
								params_.put("IdEmpresa", in.p_IdEmpresa);
								params_.put("CodigoUsuario",cliente.DocumentoIdentidad);
								List<Usuario> existe_usuario = sql.query(conn, SQL_CONSULTA_EXISTE_USUARIO, params_, Usuario.class);

								// crear usuario cliente automaticamente.
								if ("S".equals(crearUsuarioAutomatico) && salidaCrearModificarPersona.PERSONA_REFID != 0  && existe_usuario.isEmpty()) {								
									CrearUsuario crearUsuario = new CrearUsuario();
									EntradaCrearUsuario entradaCrearUsuario = new EntradaCrearUsuario();
									entradaCrearUsuario.p_IdEmpresa = in.p_IdEmpresa;
									entradaCrearUsuario.p_IdUsuario = in.p_IdUsuario;
									entradaCrearUsuario.p_IdModulo = in.p_IdModulo;
									entradaCrearUsuario.p_UsaReglasNegocio = "S";
									entradaCrearUsuario.p_CodigoUsuario = cliente.DocumentoIdentidad;
									String claveRandom = String.valueOf(new Random().nextInt(100000));
									entradaCrearUsuario.p_Clave = Hashing.sha256().newHasher().putString(claveRandom, Charsets.UTF_8).hash().toString();
									entradaCrearUsuario.p_FechaIngreso = fechaSistema;
									entradaCrearUsuario.p_FechaVigencia = "0001-01-01";
									entradaCrearUsuario.p_FechaUltimaConexion = "";
									entradaCrearUsuario.p_Conectado = "N";
									entradaCrearUsuario.p_Hora = "00:00:00";
									entradaCrearUsuario.p_Terminal = "";
									entradaCrearUsuario.p_IdEstado = "ACT";
									entradaCrearUsuario.p_IntentosConexion = 0;
									entradaCrearUsuario.p_FechaBloqueoTemporal = "";
									entradaCrearUsuario.p_TipoEntidad = "CLI";
									entradaCrearUsuario.p_IdEntidad = salida.P_OUT_REFIDCLIENTE;
									entradaCrearUsuario.p_HeredarRolesUsuario = -1L;
									entradaCrearUsuario.p_UsuarioClientes = "S";
									SalidaCrearUsuario salidaCrearUsuario = crearUsuario.logica(conn, entradaCrearUsuario);
									if (salidaCrearUsuario.P_OUT_ERRORESCONTADOR == 0L) {
										logger.info("Creado correctamente usuario cliente");
									} else {
										salida.P_OUT_ERRORESCONTADOR+=salidaCrearUsuario.P_OUT_ERRORESCONTADOR;
										salidaCrearUsuario.P_OUT_MENSAJES.forEach(mensaje->salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle(mensaje.codigo,mensaje.descripcion)));
									}
								}
								//llama al job que crea todos los documentos que debe firmar el cliente
								List<Parametros> ParametrosX = new ArrayList<>() ;
								ParametrosX.add(new Parametros("p_IdEmpresa",String.valueOf(in.p_IdEmpresa))) ;
								ParametrosX.add(new Parametros("p_IdCliente",String.valueOf(idCliente))) ;
								ParametrosX.add(new Parametros("p_IdTipoCanal",String.valueOf(in.p_IdTipoCanal))) ;
								EnviaGenerarDocumentosAutomaticosJob(conn,in.p_IdEmpresa,in.p_IdUsuario ,"GENERAR_DOCUMENTOS_AUTOMATICOS",ParametrosX,salida);
	
								// agrega log de categoria de cliente
								parametrosLog ParametrosL = new parametrosLog();
								ParametrosL.SubRegistro = "INGRESO";
								ParametrosL.CampoRegistro = "CategoriaInversionista";
								ParametrosL.ValorAntiguo = cliente.CLI_IdCategoriaInversionista;
								ParametrosL.ValorNuevo = cliente.CLI_IdCategoriaInversionista;
								RegistraLogCategoriaCliente(conn, in, fechaSistema, ParametrosL,idCliente);
								
							} else {
								salida.addError("CLIENTE_NO_PUEDE_SER_ENROLADO", "CLIENTE_NO_PUEDE_SER_ENROLADO");
							}
						}
					} else {
						salida.addError("CLIENTE_NO_PUEDE_SER_ENROLADO", "CLIENTE_NO_PUEDE_SER_ENROLADO");
					}
				} else {
					logger.info("Cliente no valido");
					salida.P_OUT_ERRORESCONTADOR++;
					salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle(cliente.IdTipoEntidad + "_EXISTE", cliente.IdTipoEntidad + "_EXISTE"));
					logger.debug(salida.P_OUT_ERRORESDETALLE.get(salida.P_OUT_ERRORESDETALLE.size() - 1));
				}
			}
		} else {
			salida.addError("CLIENTE_NO_PUEDE_SER_ENROLADO", "CLIENTE_NO_PUEDE_SER_ENROLADO");
		}
		
		logger.info("TERMINO: CrearCliente");
		
		
		return salida;
	}
	
    public void EnviaGenerarDocumentosAutomaticosJob(Connection con,Long IdEmpresa,Long IdUsuario,String IdTipoTarea,List<Parametros>TablaParametros, SalidaServicioCrearCliente salida) {
    	logger.debug("INICIO::CrearCliente.EnviaGenerarDocumentosAutomaticosJob");
		long inicio = System.currentTimeMillis();
		try {
			
			SQL sql = new SQL();
			SQL.exec(con, SQL_CREATE_TABLA_PARAMETROS);	
			sql.batch(con,SQL_INSERT_TABLA_PARAMETROS ,TablaParametros, Parametros.class);		
			
			HashMap<String,Object>paramIN = new HashMap<>();
			paramIN.put("IdEmpresa",IdEmpresa);
			paramIN.put("IdUsuario", IdUsuario);
			paramIN.put("IdTipoTarea", IdTipoTarea);
			
			HashMap<String,Class> paramOUT = new HashMap<>();
			
			HashMap<String, Object> resultadoSP = sql.call(con, SQL_CALL_AGREGAR_PROGRAMA, paramIN, paramOUT);
			
			SQL.exec(con, SQL_DROP_TABLA_PARAMETROS);	
			
			} catch (SQLException e) {
				salida.P_OUT_ERRORESCONTADOR++;
				salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("ERROR_GENERACION_AUTOMATICA_DOCUMENTOS", "ERROR_GENERACION_AUTOMATICA_DOCUMENTOS_DESCRIPCION"));
				System.out.println(e);
			}	
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearCliente.EnviaGenerarDocumentosAutomaticosJob", fin - inicio);
    }	
	
	private List<Enrolamiento> asignarCamposPorDefecto(Connection con, EntradaServicioCrearCliente entrada) throws Exception {
		logger.trace("INICIO::CrearCliente.asignarCamposPorDefecto");
		long inicio = System.currentTimeMillis();
		FormularioCanal formularioCanal = new FormularioCanal();
		formularioCanal.IdEmpresa = entrada.p_IdEmpresa;
		formularioCanal.CodigoFormulario = entrada.p_CodigoFormulario;
		formularioCanal.IdTipoCanal = entrada.p_IdTipoCanal;

		AsignarCamposFormulario asignarCamposFormulario = new AsignarCamposFormulario();
		entrada.p_ConsultarEnrolamiento = asignarCamposFormulario.asignarValoresDefecto(con, formularioCanal,
				entrada.p_ConsultarEnrolamiento, Enrolamiento.class);

		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearCliente.asignarCamposPorDefecto", fin - inicio);
		return entrada.p_ConsultarEnrolamiento;
	}

	private void asignarPerfilComercialPorDefecto(Connection con, List<Enrolamiento> enrolamientos, Long idEmpresa,
			SalidaAdministrarBorrador salida) throws Exception {
		logger.trace("INICIO::CrearCliente.asignarPerfilComercialPorDefecto");
		long inicio = System.currentTimeMillis();

		List<MensajeSalida> mensajes = new ArrayList<>();
		List<PerfilComercialBase> perfilesComercialesPorDefecto = EnrolamientoUtils
				.obtenerPerfilesComercialesPorDefecto(con, idEmpresa);

		for (Enrolamiento enrolamiento : enrolamientos) {
			if (StringUtils.isNullOrBlank(enrolamiento.POR_IdPerfilComercial)) {
				PerfilComercialBase perfilComercialBase = obtenerPerfilComercialPorDefecto(con, enrolamiento,
						perfilesComercialesPorDefecto);

				if (perfilComercialBase == null) {
					HashMap<String, String> datosMensajes = new HashMap<>(2);
					datosMensajes.put("TipoEnrolamiento", enrolamiento.TipoEnrolamiento);
					datosMensajes.put("IdCategoriaInversionista", enrolamiento.CLI_IdCategoriaInversionista);

					mensajes.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,
							"UI_ERROR_PERFIL_COMERCIAL_BASE_POR_DEFECTO_NO_EXISTE", datosMensajes, con, idEmpresa));
				} else {
					enrolamiento.POR_IdPerfilComercial = perfilComercialBase.IdPerfilComercialBase;
				}

			}
		}

		salida.P_OUT_ERRORESCONTADOR += mensajes.size();
		salida.P_OUT_MENSAJES.addAll(mensajes);

		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearCliente.asignarPerfilComercialPorDefecto", fin - inicio);
	}

	private PerfilComercialBase obtenerPerfilComercialPorDefecto(Connection con, Enrolamiento enrolamiento,
			List<PerfilComercialBase> perfilesComercialesPorDefecto) throws SQLException {
		logger.trace("INICIO::CrearCliente.obtenerIdPerfilComercialPorDefecto");
		long inicio = System.currentTimeMillis();

		PerfilComercialBase perfilComercialBase = perfilesComercialesPorDefecto.stream().filter(
				perfilComercialBasePorDefecto -> esPerfilComercialBaseParaBorrador(perfilComercialBasePorDefecto,
						enrolamiento)).findFirst().orElse(null);

		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearCliente.obtenerIdPerfilComercialPorDefecto", fin - inicio);

		return perfilComercialBase;
	}
	
	private Long obtenerSegmentoPorDefecto(Connection conn) throws SQLException {
		logger.trace("INICIO::CrearCliente.obtenerSegmentoPorDefecto");
		long inicio = System.currentTimeMillis();

		SQL sql = new SQL();
		List<TipoSegmento> listadoSegmento = new ArrayList<TipoSegmento>();
		HashMap<String, Object> paramsSegmento = new HashMap<String, Object>();
		try {
			listadoSegmento = sql.query(conn, SQL_CONSULTARSEGMENTOS, paramsSegmento, TipoSegmento.class);
		}catch(Exception e) {
			TipoSegmento tSegmento = new TipoSegmento();
			tSegmento.IdSegmentoCliente = -1L;
			listadoSegmento.add(tSegmento);
			e.printStackTrace();
		}
		List<Long> idSegmentos = listadoSegmento.parallelStream().map(sol -> {
			return sol.IdSegmentoCliente;
		}).collect(Collectors.toList());
		
		Long idSegmento = idSegmentos.get(0);
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearCliente.obtenerSegmentoPorDefecto", fin - inicio);

		return idSegmento;
	}

	private Boolean esPerfilComercialBaseParaBorrador(PerfilComercialBase perfilComercialBase,
			Enrolamiento enrolamiento) {
		logger.trace("INICIO::CrearCliente.esPerfilComercialBaseParaBorrador");
		long inicio = System.currentTimeMillis();
		Boolean esPerfilComercialBaseParaBorrador = perfilComercialBase.IdCategoriaInversionista
				.equals(enrolamiento.CLI_IdCategoriaInversionista)
				&& perfilComercialBase.TipoEnrolamiento.equals(enrolamiento.TipoEnrolamiento);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearCliente.esPerfilComercialBaseParaBorrador", fin - inicio);
		return esPerfilComercialBaseParaBorrador;
	}

	private void asignarTipoClientePorDefecto(Connection con, List<Enrolamiento> enrolamientos, Long idEmpresa,
			SalidaAdministrarBorrador salida) throws Exception {
		logger.trace("INICIO::CrearCliente.asignarTipoClientePorDefecto");
		long inicio = System.currentTimeMillis();

		List<MensajeSalida> mensajes = new ArrayList<>();
		List<TipoCliente> tiposClientePorDefecto = EnrolamientoUtils.obtenerTiposClientePorDefecto(con, idEmpresa);

		for (Enrolamiento enrolamiento : enrolamientos) {
			if (StringUtils.isNullOrBlank(enrolamiento.CLI_IdTipoCliente)) {
				TipoCliente tipoCliente = obtenerTipoClientePorDefecto(con, enrolamiento, tiposClientePorDefecto);

				if (tipoCliente == null) {
					HashMap<String, String> datosMensajes = new HashMap<>(1);
					datosMensajes.put("TipoEnrolamiento", enrolamiento.TipoEnrolamiento);

					mensajes.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,
							"UI_ERROR_TIPO_CLIENTE_POR_DEFECTO_NO_EXISTE", datosMensajes, con, idEmpresa));
				} else {
					enrolamiento.CLI_IdTipoCliente = tipoCliente.IdTipoCliente;
				}
			}
		}

		salida.P_OUT_ERRORESCONTADOR += mensajes.size();
		salida.P_OUT_MENSAJES.addAll(mensajes);

		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearCliente.asignarTipoClientePorDefecto", fin - inicio);
	}

	private TipoCliente obtenerTipoClientePorDefecto(Connection con, Enrolamiento enrolamiento,
			List<TipoCliente> tiposClientesPorDefecto) throws SQLException {
		logger.trace("INICIO::CrearCliente.obtenerTipoClientePorDefecto");
		long inicio = System.currentTimeMillis();

		TipoCliente tipoCliente = tiposClientesPorDefecto.stream().filter(
				tipoClientePorDefecto -> tipoClientePorDefecto.TipoEnrolamiento.equals(enrolamiento.TipoEnrolamiento))
				.findFirst().orElse(null);

		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearCliente.obtenerTipoClientePorDefecto", fin - inicio);

		return tipoCliente;
	}

	private SalidaValidarCamposObligatorios validarCamposObligatoriosEntrada(Connection con,
			EntradaServicioCrearCliente entrada) throws Exception {
		logger.trace("INICIO::CrearCliente.validarCamposObligatoriosEntrada");
		long inicio = System.currentTimeMillis();
		SalidaValidarCamposObligatorios salidaValidarCamposObligatorios = new SalidaValidarCamposObligatorios();
		if ("S".equals(entrada.p_UsaReglasNegocio)) {
			FormularioCanal formularioCanal = new FormularioCanal();
			formularioCanal.IdEmpresa = entrada.p_IdEmpresa;
			formularioCanal.CodigoFormulario = entrada.p_CodigoFormulario;
			formularioCanal.IdTipoCanal = entrada.p_IdTipoCanal;

			ValidarCamposObligatorios validarCamposObligatorios = new ValidarCamposObligatorios();
			salidaValidarCamposObligatorios = validarCamposObligatorios.validarObjeto(con, formularioCanal,entrada.p_ConsultarEnrolamiento, Enrolamiento.class);
		}
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearCliente.validarCamposObligatoriosEntrada", fin - inicio);
		return salidaValidarCamposObligatorios;
	}

	private List<ErrorDetalle> obtenerMensajesError(List<MensajeSalida> mensajes) {
		logger.trace("INICIO::CrearCliente.obtenerMensajesError");
		long inicio = System.currentTimeMillis();
		List<ErrorDetalle> mensajesError = mensajes.parallelStream()
				.filter(mensaje -> MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo)).map(mensaje -> {
					ErrorDetalle errorDetalle = new ErrorDetalle();
					errorDetalle.Codigo = mensaje.codigo;
					errorDetalle.Descripcion = mensaje.descripcion;
					return errorDetalle;
				}).collect(Collectors.toList());
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearCliente.obtenerMensajesError", fin - inicio);
		return mensajesError;
	}

	private SalidaCrearModificarPersona SalidaCrearModificarPersona(Connection conn, EntradaCrearModificarPersona entradaCrearModificarPersona, 
			Enrolamiento cliente, SalidaServicioCrearCliente salida) throws Exception {
		logger.trace("INICIO::CrearCliente.SalidaCrearModificarPersona");
		long inicio = System.currentTimeMillis();	
		SQL sql = new SQL();
		SalidaCrearModificarPersona salidaCrearModificarPersona = new SalidaCrearModificarPersona();
				
		HashMap<String,Object> parametroEntrada = new HashMap<String,Object>();
		parametroEntrada.put("DocumentoIdentidad", cliente.DocumentoIdentidad.trim());
		parametroEntrada.put("TipoDocumentoIdentidad", cliente.IdTipoDocumentoIdentidad);
		List<SelectPersona> SelectPersona = sql.query(conn, SQL_CONSULTA_EXISTENCIA_PERSONA, parametroEntrada, SelectPersona.class);
		
		if (SelectPersona.isEmpty()) {
			CrearPersona crearPersona = new CrearPersona();
			EntradaServicioCrearPersona entradaCrearPersona = new EntradaServicioCrearPersona();
			entradaCrearPersona.p_IdEmpresa = entradaCrearModificarPersona.IdEmpresa;
			entradaCrearPersona.p_IdModulo = entradaCrearModificarPersona.IdModulo;
			entradaCrearPersona.p_IdUsuario = entradaCrearModificarPersona.IdUsuario;
			entradaCrearPersona.p_UsaReglasNegocio = entradaCrearModificarPersona.UsaReglasNegocio;
			entradaCrearPersona.p_ConsultarEnrolamiento = Arrays.asList(cliente);
			entradaCrearPersona.p_IdTipoCanal = entradaCrearModificarPersona.IdTipoCanal;
			SalidaServicioCrearPersona salidaCrearPersona = crearPersona.logica(conn, entradaCrearPersona);
			for (AdvertenciaDetalle advertencia : salidaCrearPersona.P_OUT_ADVERTENCIASDETALLE) {
				salida.addAdvertencia(advertencia.Codigo, advertencia.Descripcion);
			}
			salidaCrearModificarPersona.PERSONA_REFID = salidaCrearPersona.P_OUT_REFID; //IdPersona
			salidaCrearModificarPersona.PERSONA_IDSOCIONEGOCIO = salidaCrearPersona.P_OUT_IDSOCIONEGOCIO;
			salidaCrearModificarPersona.PERSONA_ERRORESCONTADOR = salidaCrearPersona.P_OUT_ERRORESCONTADOR;									
			salidaCrearModificarPersona.PERSONA_CREADA = true;

			for (ErrorDetalle errorPersona : salidaCrearPersona.P_OUT_ERRORESDETALLE) {
				salida.addError(errorPersona.Codigo, errorPersona.Descripcion);
			}

		} else {
			ModificarPersonaDatosGenerales modificarPersonaDatosGenerales = new ModificarPersonaDatosGenerales();
			EntradaModificarPersonaDatosGenerales entradaCambiosPersona = new EntradaModificarPersonaDatosGenerales();
			entradaCambiosPersona.p_IdEmpresa = entradaCrearModificarPersona.IdEmpresa;
			entradaCambiosPersona.p_IdUsuario = entradaCrearModificarPersona.IdUsuario;
			entradaCambiosPersona.p_IdModulo = entradaCrearModificarPersona.IdModulo;			
			entradaCambiosPersona.p_UsaReglasNegocio = "N";
			entradaCambiosPersona.p_IdPersona = SelectPersona.get(0).IdPersona;
			
			List<RegistroCambio> registrosCambios = obtenerCambioModificarPersona(conn, cliente, entradaCrearModificarPersona.FechaSistema);	
			entradaCambiosPersona.p_RegistroCambio.addAll(registrosCambios);
			SalidaModificarPersonaDatosGenerales salidaEjecutarCambiosPersona = modificarPersonaDatosGenerales.logica(conn, entradaCambiosPersona);
			
			salidaCrearModificarPersona.PERSONA_REFID = entradaCambiosPersona.p_IdPersona;
			salidaCrearModificarPersona.PERSONA_IDSOCIONEGOCIO = SelectPersona.get(0).IdSocioDeNegocio;
			salidaCrearModificarPersona.PERSONA_ERRORESCONTADOR = salidaEjecutarCambiosPersona.P_OUT_ERRORESCONTADOR;			
			salidaCrearModificarPersona.PERSONA_CREADA = false;
			
			for (MensajeSalida errorPersona : salidaEjecutarCambiosPersona.P_OUT_MENSAJES) {
				if(salidaEjecutarCambiosPersona.P_OUT_ERRORESCONTADOR>0) {
					salida.addError(errorPersona.codigo, errorPersona.descripcion);
				} else {
				 	salida.addAdvertencia(errorPersona.codigo, errorPersona.descripcion);
				}
			}
		}	
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearCliente.SalidaCrearModificarPersona", fin - inicio);
		return salidaCrearModificarPersona;
	}
	
	private List<RegistroCambio> obtenerCambioModificarPersona(Connection conn, Enrolamiento enrolamiento, String fechaSistema){
		logger.trace("INICIO::CrearCliente.obtenerCambioModificarPersona");
		long inicio = System.currentTimeMillis();	
		
		SQL sql = new SQL();
		List<RegistroCambio> registrosCambio = new ArrayList<>();
		List<DatosPersona> listadoPersona = new ArrayList<DatosPersona>();
		HashMap<String,Object> hPersonaPorId = new HashMap<String, Object>();
		HashMap<String,Object> hPersonaPorDocumentoIdentidad = new HashMap<String, Object>();
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("DocumentoIdentidad", enrolamiento.DocumentoIdentidad);
		params.put("IdPersona", 0L);
		if(enrolamiento.IdPersonaConyuge!=null && !(enrolamiento.IdPersonaConyuge.compareTo(0L) == 0)) params.replace("IdPersona", enrolamiento.IdPersonaConyuge);
		
		try {
			listadoPersona = obtenerListadoPersona(conn, sql , params);
			if(!(listadoPersona.isEmpty())) {
				hPersonaPorId = Utils.List2Hash(listadoPersona, "IdPersona");
				hPersonaPorDocumentoIdentidad = Utils.List2Hash(listadoPersona, "DocumentoIdentidad");
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		if(!(hPersonaPorDocumentoIdentidad.isEmpty())) {
			try {
				if(hPersonaPorDocumentoIdentidad.containsKey(enrolamiento.DocumentoIdentidad)) {
					DatosPersona persona = (DatosPersona) hPersonaPorDocumentoIdentidad.get(enrolamiento.DocumentoIdentidad);
					if( (persona.Tipo==null && (StringUtils.isNullOrBlank(enrolamiento.Tipo) == false) )  || ( persona.Tipo!=null && (StringUtils.isNullOrBlank(enrolamiento.Tipo) == false) && !(enrolamiento.Tipo.compareToIgnoreCase(persona.Tipo) == 0)) ) registrosCambio.add(EnrolamientoUtils.crearRegistroCambio("Tipo", enrolamiento.Tipo));
					if( (persona.Nombres==null && (StringUtils.isNullOrBlank(enrolamiento.Nombres) == false) ) || (persona.Nombres!=null && (StringUtils.isNullOrBlank(enrolamiento.Nombres) == false) && !(enrolamiento.Nombres.compareToIgnoreCase(persona.Nombres) == 0))  ) registrosCambio.add(EnrolamientoUtils.crearRegistroCambio("Nombres", enrolamiento.Nombres));
					if( (persona.ApellidoPaterno==null && (StringUtils.isNullOrBlank(enrolamiento.ApellidoPaterno) == false)) || (persona.ApellidoPaterno!=null && (StringUtils.isNullOrBlank(enrolamiento.ApellidoPaterno) == false) && !(enrolamiento.ApellidoMaterno.compareToIgnoreCase(persona.ApellidoMaterno) == 0) ) ) registrosCambio.add(EnrolamientoUtils.crearRegistroCambio("ApellidoPaterno", enrolamiento.ApellidoPaterno));
					if( (persona.ApellidoMaterno==null && (StringUtils.isNullOrBlank(enrolamiento.ApellidoPaterno) == false)) || (persona.ApellidoMaterno!=null && (StringUtils.isNullOrBlank(enrolamiento.ApellidoMaterno) == false) &&  !(enrolamiento.ApellidoMaterno.compareToIgnoreCase(persona.ApellidoMaterno) == 0) )  ) registrosCambio.add(EnrolamientoUtils.crearRegistroCambio("ApellidoMaterno", enrolamiento.ApellidoMaterno));
					if( (persona.RazonSocial==null && (StringUtils.isNullOrBlank(enrolamiento.RazonSocial) == false) ) || (persona.RazonSocial!=null && (StringUtils.isNullOrBlank(enrolamiento.RazonSocial) == false) && !(enrolamiento.RazonSocial.compareToIgnoreCase(persona.RazonSocial) == 0) )  ) registrosCambio.add(EnrolamientoUtils.crearRegistroCambio("RazonSocial", enrolamiento.RazonSocial));
					if( (persona.FechaNacimiento==null && enrolamiento.FechaNacimiento!=null && enrolamiento.FechaNacimiento != java.sql.Date.valueOf("0001-01-01") ) 
						|| (persona.FechaNacimiento!=null && enrolamiento.FechaNacimiento!=null && enrolamiento.FechaNacimiento != java.sql.Date.valueOf("0001-01-01") && !(persona.FechaNacimiento.toString().compareToIgnoreCase(enrolamiento.FechaNacimiento.toString()) == 0) ) ) {
						registrosCambio.add(EnrolamientoUtils.crearRegistroCambio("FechaNacimiento", enrolamiento.FechaNacimiento));
					}
					if( (persona.Sexo==null && (StringUtils.isNullOrBlank(enrolamiento.Sexo) == false) ) || (persona.Sexo!=null && (StringUtils.isNullOrBlank(enrolamiento.Sexo) == false) && !(enrolamiento.Sexo.compareToIgnoreCase(persona.Sexo) == 0) ) )registrosCambio.add(EnrolamientoUtils.crearRegistroCambio("Sexo", enrolamiento.Sexo));
					if( (persona.IdProfesion==null && enrolamiento.IdProfesion!=null && !(enrolamiento.IdProfesion.compareTo(0L)== 0) ) || (persona.IdProfesion!=null && enrolamiento.IdProfesion!=null && !(enrolamiento.IdProfesion.compareTo(0L)== 0) && !(enrolamiento.IdProfesion.compareTo(persona.IdProfesion) == 0) ) ) registrosCambio.add(EnrolamientoUtils.crearRegistroCambio("IdProfesion", enrolamiento.IdProfesion));
					if( (persona.IdPaisNacimiento==null && enrolamiento.IdPaisNacimiento!=null && !(enrolamiento.IdPaisNacimiento.compareTo(0L) == 0) ) || (persona.IdPaisNacimiento!=null && enrolamiento.IdPaisNacimiento!=null && !(enrolamiento.IdPaisNacimiento.compareTo(0L) == 0) && !(enrolamiento.IdPaisNacimiento.compareTo(persona.IdPaisNacimiento) == 0) ) ) registrosCambio.add(EnrolamientoUtils.crearRegistroCambio("IdPaisNacimiento", enrolamiento.IdPaisNacimiento));
					if( (persona.IdPaisResidencia==null && enrolamiento.IdPaisResidencia!=null && !(enrolamiento.IdPaisResidencia.compareTo(0L) == 0) ) || (persona.IdPaisResidencia!=null && enrolamiento.IdPaisResidencia!=null && !(enrolamiento.IdPaisResidencia.compareTo(0L) == 0) && !(enrolamiento.IdPaisResidencia.compareTo(persona.IdPaisResidencia) == 0) ) ) registrosCambio.add(EnrolamientoUtils.crearRegistroCambio("IdPaisResidencia", enrolamiento.IdPaisResidencia));
					if( (persona.IdPaisNacionalidadPrincipal==null && enrolamiento.IdPaisNacionalidadPrincipal!=null && !(enrolamiento.IdPaisNacionalidadPrincipal.compareTo(0L) == 0) ) || (persona.IdPaisNacionalidadPrincipal!=null && enrolamiento.IdPaisNacionalidadPrincipal!=null && !(enrolamiento.IdPaisNacionalidadPrincipal.compareTo(0L) == 0) && !(enrolamiento.IdPaisNacionalidadPrincipal.compareTo(persona.IdPaisNacionalidadPrincipal) == 0) ) ) registrosCambio.add(EnrolamientoUtils.crearRegistroCambio("IdPaisNacionalidadPrincipal", enrolamiento.IdPaisNacionalidadPrincipal));
					if( (persona.EstadoCivil==null && (StringUtils.isNullOrBlank(enrolamiento.EstadoCivil) == false) ) || (persona.EstadoCivil!=null && enrolamiento.EstadoCivil!=null  && !(enrolamiento.EstadoCivil.compareTo(persona.EstadoCivil) == 0) ) ) registrosCambio.add(EnrolamientoUtils.crearRegistroCambio("EstadoCivil", enrolamiento.EstadoCivil));
					if( ((persona.IdSituacionLaboral==null || StringUtils.isNullOrBlank(persona.IdSituacionLaboral)) && (StringUtils.isNullOrBlank(enrolamiento.IdSituacionLaboral) == false) ) || (persona.IdSituacionLaboral!=null && enrolamiento.IdSituacionLaboral!=null  && !(enrolamiento.IdSituacionLaboral.compareTo(persona.IdSituacionLaboral) == 0) ) ) registrosCambio.add(EnrolamientoUtils.crearRegistroCambio("IdSituacionLaboral", enrolamiento.IdSituacionLaboral));
					if(enrolamiento.IdPersonaConyuge!=null && !(enrolamiento.IdPersonaConyuge.compareTo(0L) == 0)) {
						try {
							if(hPersonaPorId.containsKey(enrolamiento.IdPersonaConyuge)) {
								DatosPersona personaConyuge = (DatosPersona) hPersonaPorId.get(enrolamiento.IdPersonaConyuge);
								if( ( persona.IdPersonaConyuge==null && !(enrolamiento.IdPersonaConyuge.compareTo(0L)==0) ) 
									|| (persona.IdPersonaConyuge!=null && !(enrolamiento.IdPersonaConyuge.compareTo(0L)==0) && !(enrolamiento.IdPersonaConyuge.compareTo(personaConyuge.IdPersona)== 0) ) 
									|| (persona.IdPersonaConyuge.compareTo(0L)==0 && enrolamiento.IdPersonaConyuge > 0L) ) {
									registrosCambio.add(EnrolamientoUtils.crearRegistroCambio("IdPersonaConyuge", enrolamiento.IdPersonaConyuge)); 
								}
								if( (persona.RegimenConyugal==null && (StringUtils.isNullOrBlank(enrolamiento.RegimenConyugal) == false) )
									|| (persona.RegimenConyugal!=null && (StringUtils.isNullOrBlank(enrolamiento.RegimenConyugal) == false) && !(persona.RegimenConyugal.compareToIgnoreCase(enrolamiento.RegimenConyugal)==0) ) ) {
									registrosCambio.add(EnrolamientoUtils.crearRegistroCambio("RegimenConyugal",enrolamiento.RegimenConyugal));
								}
							}
						}catch(Exception e) {
							e.printStackTrace();
						}
					}
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearCliente.obtenerCambioModificarPersona", fin - inicio);
		return registrosCambio;
	}

	private HashMap<String, Object> obtenerhashListadoEjecutivo(Connection conn, SQL sql, HashMap<String, Object> params, List<Long> listadoIds) {
		logger.debug("INICIO::CrearCliente.ObtenerhashListadoEjecutivo");
		long inicio = System.currentTimeMillis();
		
		HashMap<String, Object>  listadoHash = new HashMap<String, Object>();
		try {
			if(!(listadoIds.isEmpty())) {
				List<Ejecutivo> listado = sql.query(conn, SQL_CONSULTAR_EJECUTIVO , params, Ejecutivo.class);
				listadoHash = Utils.List2Hash(listado, "IdEmpleado");
				params.clear();
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		params.clear();
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearCliente.ObtenerhashListadoEjecutivo", fin - inicio);
		
		return listadoHash;
	}
	
	private HashMap<String, Object> obtenerhashListadoClienteCMF(Connection conn, SQL sql, HashMap<String, Object> params, List<Long> listadoIds) {
		logger.debug("INICIO::CrearCliente.obtenerhashListadoClienteCMF");
		long inicio = System.currentTimeMillis();
		
		HashMap<String, Object>  listadoHash = new HashMap<String, Object>();
		try {
			if(!(listadoIds.isEmpty())) {
				List<TipoClienteCMF> listado = sql.query(conn, SQL_CONSULTAR_TIPOCLIENTECMF , params, TipoClienteCMF.class);
				listadoHash = Utils.List2Hash(listado, "IdDetalleIndicador");
				params.clear();
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		params.clear();
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearCliente.obtenerhashListadoClienteCMF", fin - inicio);
		
		return listadoHash;
	}
	
	private HashMap<String, Object> obtenerhashListadoAreas(Connection conn, SQL sql, HashMap<String, Object> params, List<String> listadoIds) {
		logger.debug("INICIO::CrearCliente.ObtenerhashListadoAreas");
		long inicio = System.currentTimeMillis();
		
		HashMap<String, Object>  listadoHash = new HashMap<String, Object>();
		try {
			if(!(listadoIds.isEmpty())) {
				List<Area> listado = sql.query(conn, SQL_CONSULTAR_AREA , params, Area.class);
				listadoHash = Utils.List2Hash(listado, "Codigo");
				params.clear();
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		params.clear();
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearCliente.ObtenerhashListadoAreas", fin - inicio);
		
		return listadoHash;
	}
	
	private List<DatosPersona> obtenerListadoPersona(Connection conn, SQL sql, HashMap<String, Object> params){
		logger.debug("INICIO::CrearCliente.ObtenerListadoPersona");
		long inicio = System.currentTimeMillis();
		
		List<DatosPersona> listadoPersona = new ArrayList<DatosPersona>();
		try {
			listadoPersona = sql.query(conn, SQL_CONSULTARPERSONA, params, DatosPersona.class);
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearCliente.ObtenerListadoPersona", fin - inicio);
		
		return listadoPersona;
	}
	
	private List<DatosCliente> obtenerListadoCliente(Connection conn, SQL sql, HashMap<String, Object> params){
		logger.debug("INICIO::CrearCliente.ObtenerListadoCliente");
		long inicio = System.currentTimeMillis();
		
		List<DatosCliente> listadoCliente = new ArrayList<DatosCliente>();
		try {
			listadoCliente = sql.query(conn, SQL_CONSULTARCLIENTE, params, DatosCliente.class);
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearCliente.ObtenerListadoCliente", fin - inicio);
		
		return listadoCliente;
	}
	
	
	private AgregarLog.InAgregarLog prepararLog(Connection conn, SQL sql, EntidadCliente cliente, EntradaServicioCrearCliente in, String fechaSistema,
												HashMap<String,Object> hCMF,HashMap<String,Object> hArea,HashMap<String,Object> hEjecuivo) {
		
		logger.debug("INICIO::CrearCliente.PrepararLog");
		long inicio = System.currentTimeMillis();

		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
		Long IdTrabajo = (long)(Math.random()*10000);
		HashMap<String, Object> params = new HashMap<String, Object>();
		HashMap<String, Object> hPersonaCliente = new HashMap<String, Object>();
		params.put("IdCliente", cliente.IdCliente);
		List<DatosCliente> listadoCliente = obtenerListadoCliente(conn, sql, params);
		try {
			hPersonaCliente = Utils.List2Hash(listadoCliente, "IdCliente");
		}catch(Exception e) {
			e.printStackTrace();
		}
		//CABECERA LOG
		AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
		entradaAgregarLog.p_IdTrabajo = IdTrabajo;
		log.IdTrabajo = entradaAgregarLog.p_IdTrabajo;
		log.IdCabeceraLog = cliente.IdCliente;
		log.IdTipoLog = "INGRESO";
		log.IdModulo = in.p_IdModulo;
		log.IdTipoRegistro = "CLIENTE";
		log.IdUsuario = in.p_IdUsuario;
		log.IdEmpresa = in.p_IdEmpresa;
		log.FechaSistemaLog = fechaSistema;
		log.IdUsuarioAtendido = in.p_IdUsuario;
		log.IdCliente = cliente.IdCliente;
		if(!(hPersonaCliente.isEmpty())) {
			DatosCliente datosCliente = (DatosCliente) hPersonaCliente.get(cliente.IdCliente);
			if(datosCliente != null) {
				String nombreCompleto = (datosCliente.Nombres!=null && datosCliente.Nombres!="")? datosCliente.Nombres+" " : "" ;
				nombreCompleto += (datosCliente.ApellidoPaterno!=null && datosCliente.ApellidoPaterno!="")?datosCliente.ApellidoPaterno+" " : "";
				nombreCompleto += (datosCliente.ApellidoMaterno!=null && datosCliente.ApellidoMaterno!="")?datosCliente.ApellidoMaterno+" " : "";
				log.NombreCliente = nombreCompleto;
				if(datosCliente.DocumentoIdentidad!=null && datosCliente.DocumentoIdentidad!="") log.IdDocumentoIdentidad = datosCliente.DocumentoIdentidad;
				if(datosCliente.IdTipoDocumentoIdentidad!=null && datosCliente.IdTipoDocumentoIdentidad!="") log.IdTipoDocumentoIdentidad = datosCliente.IdTipoDocumentoIdentidad;
				if(datosCliente.IdEstado!=null && datosCliente.IdEstado!="") log.EstadoCliente = datosCliente.IdEstado;
			}
		}
		log.Folio = cliente.IdCliente;
		log.NombreProceso = "Ingreso Cliente";
		log.EstadoProceso = "Ing";
		entradaAgregarLog.p_Log.add(log);
		//DETALLE LOG
		if(cliente.CLI_IdCategoriaInversionista!=null && cliente.CLI_IdCategoriaInversionista.toString().trim()!="") agregarLogDetalle("Ingreso Cliente", log, "Categoria Inversionista", cliente.CLI_IdCategoriaInversionista, entradaAgregarLog.p_LogDetalle);
		if(cliente.POR_IdPerfilComercial!=null && cliente.POR_IdPerfilComercial.toString().trim()!="") agregarLogDetalle("Ingreso Cliente", log, "Pefil Comercial", cliente.POR_IdPerfilComercial, entradaAgregarLog.p_LogDetalle);
		if(cliente.CLI_IdTipoCliente!=null && cliente.CLI_IdTipoCliente.toString().trim()!="") agregarLogDetalle("Ingreso Cliente", log, "Tipo Cliente", cliente.CLI_IdTipoCliente, entradaAgregarLog.p_LogDetalle);
		
		if(!(hCMF.isEmpty())){
			if(cliente.CLI_IdTipoClienteCMF!=null && !(cliente.CLI_IdTipoClienteCMF.compareTo(0L)==0)){
				if(hCMF.containsKey(cliente.CLI_IdTipoClienteCMF)) {
					TipoClienteCMF tipClienteCMF = (TipoClienteCMF) hCMF.get(cliente.CLI_IdTipoClienteCMF);
					if(tipClienteCMF.DetalleIndicador!=null && tipClienteCMF.DetalleIndicador.toString().trim()!="") agregarLogDetalle("Ingreso Cliente", log, "Tipo Cliente CMF", tipClienteCMF.DetalleIndicador, entradaAgregarLog.p_LogDetalle);
				}
			}
		}
		if(!(hArea.isEmpty())){
			if(cliente.CLI_AreaCliente!=null && cliente.CLI_AreaCliente.toString().trim()!="") {
				if(hArea.containsKey(cliente.CLI_AreaCliente)) {
					Area area = (Area) hArea.get(cliente.CLI_AreaCliente);
					if(area.Descripcion!=null && area.Descripcion.toString().trim()!="") agregarLogDetalle("Ingreso Cliente", log, "Area Cliente", area.Descripcion, entradaAgregarLog.p_LogDetalle);
				}
			}
		}
		if(!(hEjecuivo.isEmpty())){
			if(cliente.CLI_IdEmpleadoEjecutivo!=null && cliente.CLI_IdEmpleadoEjecutivo.toString().trim()!="") {
				if(hEjecuivo.containsKey(cliente.CLI_IdEmpleadoEjecutivo)) {
					Ejecutivo ejecutivo = (Ejecutivo) hEjecuivo.get(cliente.CLI_IdEmpleadoEjecutivo);
					if(ejecutivo.NombreCompleto!=null && ejecutivo.NombreCompleto.toString().trim()!="") agregarLogDetalle("Ingreso Cliente", log, "Ejecutivo", ejecutivo.NombreCompleto, entradaAgregarLog.p_LogDetalle);
				}
			}
		}

		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearCliente.PrepararLog", fin - inicio);

		return entradaAgregarLog;
	}
	
	private void agregarLogDetalle(String Movimiento, AgregarLog.InAgregarLog.Log log, String nombreCampo, String campo, List<AgregarLog.InAgregarLog.DetalleLog> ListaDetalleLog) {
		AgregarLog.InAgregarLog.DetalleLog detalleLog = new AgregarLog.InAgregarLog.DetalleLog();
		detalleLog.IdTrabajo = log.IdTrabajo;
		detalleLog.IdCabeceraLog = log.IdCabeceraLog;
		detalleLog.SubRegistro = Movimiento.trim();
		if(campo != "" && campo.length() > 1) {
			detalleLog.CampoRegistro = nombreCampo.trim();
			detalleLog.ValorAntiguo = "";
			detalleLog.ValorNuevo = campo.trim();
			ListaDetalleLog.add(detalleLog);
		}
	}
	
	private void registrarLog(Connection conn, AgregarLog.InAgregarLog entradaAgregarLog, List<Long> idsPortafolios) {
		logger.debug("INICIO::CrearCliente.RegistrarLog");
		long inicio = System.currentTimeMillis();
		
		AgregarLog agregarLogService = new AgregarLog();
		
		if(entradaAgregarLog != null) {
			if(!(idsPortafolios.isEmpty())) {
				Long idPortafolio = idsPortafolios.get(0);
				AgregarLog.InAgregarLog.Log log = entradaAgregarLog.p_Log.get(0);
				log.IdPortafolio = idPortafolio;
				log.EstadoPortafolio = "ACT";
				//limpiamos lista, para pasar nuevo objeto modificado
				entradaAgregarLog.p_Log.clear();
				entradaAgregarLog.p_Log.add(log);
			}
			try {
				agregarLogService.logica(conn, entradaAgregarLog);
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearCliente.RegistrarLog", fin - inicio);
	}


	public void RegistraLogCategoriaCliente(Connection con, EntradaServicioCrearCliente in, String FechaSistema,
			parametrosLog TablaParametrosLog,Long IdclienteLog) {
		logger.trace("INICIO::CrearCliente.RegistraLogCategoriaCliente");
		try {
			
			ModificarClienteDatosGenerales AgregaCambioLog = new ModificarClienteDatosGenerales();
			ModificarClienteDatosGenerales.EntradaModificarClienteDatosGenerales entrada = new ModificarClienteDatosGenerales.EntradaModificarClienteDatosGenerales();
			ModificarClienteDatosGenerales.RegistroCambio registroCambio = new ModificarClienteDatosGenerales.RegistroCambio();
			
			registroCambio.CampoModificado ="IdCategoriaInversionista";
			registroCambio.ValorNuevo =TablaParametrosLog.ValorNuevo;
			
			entrada.p_IdEmpresa = in.p_IdEmpresa;
			entrada.p_IdUsuario = in.p_IdUsuario;
			entrada.p_IdModulo =  in.p_IdTipoCanal;
			entrada.p_UsaReglasNegocio="N";
			entrada.p_IdCliente = IdclienteLog;
			entrada.p_IdCanal =  in.p_IdTipoCanal;
			entrada.p_DesdeEnrolamiento = true;
			//entrada.p_DesdeEnrolamientoCliente = true;
			entrada.p_RegistroCambio =  Arrays.asList(registroCambio); 			
			AgregaCambioLog.logica(con, entrada);
			System.out.println("solicitud " + entrada);
		} catch ( Exception e) {
			// TODO Auto-generated catch block
			System.out.println(e);
		}
		logger.trace("FIN::CrearCliente.RegistraLogCategoriaCliente");
	}
	
	public class EntidadAgregarLog  {	
		String  p_IdTipoLog;
		String  p_IdModulo;  
		Long  p_IdUsuario; 
		Long  p_IdEmpresa ;
		String  p_FechaSistemaLog; 
		Long  p_IdUsuarioAtendido; 
		String  p_IdTipoRegistro ;
		List <parametrosLog>  p_DetalleLog ;
		
		public	EntidadAgregarLog(){
			this.p_IdTipoLog = "";
			this.p_IdModulo = "";  
			this.p_IdUsuario = 0l; 
			this.p_IdEmpresa  = 0l;
			this.p_FechaSistemaLog = "";
			this.p_IdUsuarioAtendido = 0l; 
			this.p_IdTipoRegistro  = "";
			this.p_DetalleLog  = new ArrayList<>();
		}
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public class parametrosLog {
		String SubRegistro;
		String CampoRegistro;
		String ValorAntiguo;
		String ValorNuevo;
		public	parametrosLog() {
			this.SubRegistro = "";
			this.CampoRegistro = "";
			this.ValorAntiguo = "";
			this.ValorNuevo = "";
		}

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static void main(String[] args) {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			Gson gson = Utils.getGsonBuilder();
			
			String json = "{\"p_ConsultarEnrolamiento\":[{\"IdEnrolamiento\":0,\"IdEmpresa\":1,\"IdUsuario\":76770,\"IdTipoEntidad\":\"CLIENTE\",\"IdEntidadEnrolada\":0,\"IdTipoDocumentoIdentidad\":\"R\",\"IdTipoDocumentoIdentidadAsociado\":\"R\",\"DocumentoIdentidad\":\"71465605-8\",\"IdTipoDocumentoIdentidadAdicional\":\"R\",\"DocumentoIdentidadAdicional\":\"\",\"Tipo\":\"N\",\"Nombres\":\"ASD\",\"ApellidoPaterno\":\"PA\",\"ApellidoMaterno\":\"LU\",\"RazonSocial\":\"\",\"IdPaisResidencia\":44,\"IdPaisNacimiento\":2,\"FechaNacimiento\":\"2026-04-28\",\"Sexo\":\"O\",\"EstadoCivil\":\"SO\",\"IdCiudad\":3490,\"IdDivisionPais\":345,\"IdTipoTelefono\":\"MOVIL\",\"Telefono\":\"+56912482711\",\"Email\":\"A@M.COM\",\"DireccionCalle\":\"ADW\",\"DireccionNumero\":\"12\",\"DireccionDepartamento\":\"\",\"IdPersonaConyuge\":0,\"RegimenConyugal\":\"\",\"CLI_IdCategoriaInversionista\":\"ARRIESGADO\",\"CLI_IdTipoCliente\":\"EXPRESS\",\"CLI_IdTipoClienteCMF\":41,\"CLI_IdClaseCliente\":\"\",\"CLI_IdEmpleadoEjecutivo\":767,\"CLI_CodigoAdicionalHolding\":\"\",\"CLI_ClienteFinal\":\"N\",\"POR_IdPerfilComercial\":\"TEST01\",\"POR_IdPersonaBrokerExtranjero\":0,\"EMP_CodigoEmpleado\":\"\",\"EMP_CodigoTipoCargoEmpleado\":\"\",\"EMP_IdEmpleadoSupervisor\":0,\"EMP_IdSucursal\":0,\"EMP_FechaInicioCargo\":\"0001-01-01\",\"EMP_FechaTerminoCargo\":\"0001-01-01\",\"CNT_CodigoInscripcion\":\"\",\"CNT_Emisor\":\"\",\"CNT_Depositos\":\"\",\"CNT_Bolsa\":\"\",\"CNT_Corredora\":\"\",\"CNT_CamaraDeCompensacion\":\"\",\"CNT_Banco\":\"\",\"CNT_Boveda\":\"\",\"CNT_Otros\":\"\",\"PRO_IdAreaProveedor\":0,\"EMP_CargoIndefinido\":\"\",\"EsRutInterno\":\"N\",\"TipoEnrolamiento\":\"EXPRESS\",\"CLI_AreaCliente\":\"CLIMAY\",\"UsaDivisionPais\":\"S\",\"IdTipoCanalIngresoCorredor\":\"OPT\",\"CodigoPropositoTransaccion\":\"RANGO_RENTA_1000\"}],\"p_IdEmpresa\":1,\"p_IdUsuario\":76770,\"p_IdModulo\":\"SOC\",\"p_CodigoFormulario\":\"INGRESAR_CLIENTE_EXPRESS_NATURAL\",\"p_IdTipoCanal\":\"OPT\",\"p_UsaReglasNegocio\":\"S\"}";
			EntradaServicioCrearCliente Objin = gson.fromJson(json, EntradaServicioCrearCliente.class);

			CrearCliente demo = new CrearCliente();
			System.out.println("ENTRADA");
			System.out.println(Objin.toString());

			try {
				SalidaServicioCrearCliente ObjOut = demo.logica(con, Objin);
				con.rollback();
				System.out.println(ObjOut.toString());
				System.out.println("SALIDA");
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
			con.rollback();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}

}
