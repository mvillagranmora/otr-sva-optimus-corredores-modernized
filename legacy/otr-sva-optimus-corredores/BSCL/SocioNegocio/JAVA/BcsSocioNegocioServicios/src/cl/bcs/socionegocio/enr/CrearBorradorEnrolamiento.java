package cl.bcs.socionegocio.enr;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.enr.ConsultarFormularioSocioNegocio.FormularioSocioNegocio;
import cl.bcs.socionegocio.enr.CrearBorradorDireccionElectronica.EntradaCrearBorradorDireccionElectronica;
import cl.bcs.socionegocio.enr.CrearBorradorDireccionFisica.EntradaCrearBorradorDireccionFisica;
import cl.bcs.socionegocio.enr.CrearBorradorDocumentoIdentidad.EntradaCrearBorradorDocumentoIdentidad;
import cl.bcs.socionegocio.enr.CrearBorradorEnrolamientoDatosPersona.EntradaCrearBorradorEnrolamientoDatosPersona;
import cl.bcs.socionegocio.enr.CrearBorradorIndicador.EntradaCrearBorradorIndicador;
import cl.bcs.socionegocio.enr.CrearBorradorTelefono.EntradaCrearBorradorTelefono;
import cl.bcs.socionegocio.enr.CrearBorradorFatca.EntradaCrearBorradorFatca;
import cl.bcs.socionegocio.enr.CrearBorradorFatcaPaisResidencia.EntradaCrearBorradorFatcaPaisResidencia;
import cl.bcs.socionegocio.enr.ModificarBorradorFatcaPaisResidencia.EntradaModificarBorradorFatcaPaisResidencia;
import cl.bcs.socionegocio.enr.EliminarBorradorIndicador.EntradaEliminarBorradorIndicador;
import cl.bcs.socionegocio.enr.ModificarBorradorDireccionElectronica.EntradaModificarBorradorDireccionElectronica;
import cl.bcs.socionegocio.enr.ModificarBorradorDireccionFisica.EntradaModificarBorradorDireccionFisica;
import cl.bcs.socionegocio.enr.ModificarBorradorTelefono.EntradaModificarBorradorTelefono;
import cl.bcs.socionegocio.enr.pojo.DireccionElectronica;
import cl.bcs.socionegocio.enr.pojo.DireccionFisica;
import cl.bcs.socionegocio.enr.pojo.DocumentoIdentidad;
import cl.bcs.socionegocio.enr.pojo.Enrolamiento;
import cl.bcs.socionegocio.enr.pojo.EntradaAdministrarBorrador;
import cl.bcs.socionegocio.enr.pojo.Fatca;
import cl.bcs.socionegocio.enr.pojo.FatcaPaisResidencia;
import cl.bcs.socionegocio.enr.pojo.Indicador;
import cl.bcs.socionegocio.enr.pojo.PerfilComercialBase;
import cl.bcs.socionegocio.enr.pojo.TipoCliente;
import cl.bcs.socionegocio.soc.SocioNegocioUtils.TipoSegmento;
import cl.bcs.socionegocio.enr.pojo.ResultadoAdministrarBorradorEntidad;
import cl.bcs.socionegocio.enr.pojo.SalidaAdministrarBorrador;
import cl.bcs.socionegocio.enr.pojo.Telefono;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.StringUtils;
import cl.bcs.utils.Utils;
import cl.bcs.utils.Utils.DatosPais;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CrearBorradorEnrolamiento implements JavaCall {

	static final Logger logger = LogManager.getLogger(CrearBorradorEnrolamiento.class);

	private final String SQL_SELECT_ENROLAMIENTO_BORRADOR = loadResourceToString("cl/bcs/socionegocio/enr/sql/CrearBorradorEnrolamiento_SelectEnrolamientoBorrador.sql");
	private final String SQL_INSERT_ENROLAMIENTO_BORRADOR = loadResourceToString("cl/bcs/socionegocio/enr/sql/CrearBorradorEnrolamiento_InsertEnrolamientoBorrador.sql");
	private final String SQL_UPDATE_ENROLAMIENTO_BORRADOR = loadResourceToString("cl/bcs/socionegocio/enr/sql/CrearBorradorEnrolamiento_UpdateEnrolamientoBorrador.sql");
	private final String SQL_SELECT_INDICADORES_CREACION = loadResourceToString("cl/bcs/socionegocio/enr/sql/CrearBorradorEnrolamiento_SelectIndicadoresCreacion.sql");
	private final String SQL_SELECT_INDICADORES_ELIMINACION = loadResourceToString("cl/bcs/socionegocio/enr/sql/CrearBorradorEnrolamiento_SelectIndicadoresEliminacion.sql");
	private final String SQL_SELECT_PAIS = loadResourceToString("cl/bcs/socionegocio/enr/sql/CrearBorradorEnrolamiento_SelectPais.sql");
	private final String SQL_SELECT_FATCA_PAIS_RESIDENCIA = loadResourceToString("cl/bcs/socionegocio/enr/sql/ComunesBorradorFatcaPaisResidencia_SelectFatcaPaisResidencia.sql");
	private final String SQL_UPDATE_FATCA_EXCEPCION_NACIMIENTO = loadResourceToString("cl/bcs/socionegocio/enr/sql/ComunesBorradorFatca_UpdateFatcaExcepcionNacimiento.sql");
	private final String SQL_UPDATE_FATCA_EXCEPCION_RESIDENCIA = loadResourceToString("cl/bcs/socionegocio/enr/sql/ComunesBorradorFatca_UpdateFatcaExcepcionResidencia.sql");
	private final String SQL_SELECT_PERSONA_EXISTENTE_SIN_BORRADOR_ENROLAMIENTO = loadResourceToString("cl/bcs/socionegocio/enr/sql/CrearBorradorEnrolamiento_SelectPersonaExistenteSinBorradorEnrolamiento.sql");
	private final String SQL_CONSULTARSEGMENTOS	= loadResourceToString("cl/bcs/socionegocio/cli/sql/CrearCliente_SelectSegmentos.sql");
	
	public static class EntradaCrearBorradorEnrolamiento extends EntradaAdministrarBorrador {
		public List<BorradorEnrolamiento> p_ConsultarEnrolamiento;

		public EntradaCrearBorradorEnrolamiento() {
			this.p_ConsultarEnrolamiento = new ArrayList<BorradorEnrolamiento>();
			this.p_CodigoFormulario = "";
			this.p_IdTipoCanal = "";
		}
	}
	
	public static class BorradorEnrolamiento extends BcsPojo{
		public Long IdEnrolamiento;
		public Long IdEmpresa;
		public Long IdUsuario;
		public String IdTipoEntidad;
		public Long IdEntidadEnrolada;
		public String IdTipoDocumentoIdentidad;
		public String IdTipoDocumentoIdentidadAsociado;
		public String DocumentoIdentidad;
		public String IdTipoDocumentoIdentidadAdicional;
		public String DocumentoIdentidadAdicional;
		public String Tipo;
		public String Nombres;
		public String ApellidoPaterno;
		public String ApellidoMaterno;
		public String RazonSocial;
		public Long IdPaisNacimiento;
		public Long IdPaisResidencia;
		public java.sql.Date FechaNacimiento;
		public String Sexo;
		public String EstadoCivil;
		public Long IdCiudad;
		public Long IdDivisionPais;
		public String IdTipoTelefono;
		public String Telefono;
		public String Email;
		public String DireccionCalle;
		public String DireccionNumero;
		public String DireccionDepartamento;
		public Long IdPersonaConyuge;
		public String RegimenConyugal;
		public String CLI_IdCategoriaInversionista;
		public String CLI_IdTipoCliente;
		public Long CLI_IdTipoClienteCMF;
		public Long CLI_IdEmpleadoEjecutivo;
		public String CLI_CodigoAdicionalHolding;
		public String CLI_IdClaseCliente;
		public String POR_IdPerfilComercial;
		public String EMP_CodigoEmpleado;
		public String EMP_CodigoTipoCargoEmpleado;
		public Long EMP_IdEmpleadoSupervisor;
		public Long EMP_IdSucursal;
		public String EMP_FechaInicioCargo;
		public String EMP_FechaTerminoCargo;
		public String CNT_CodigoInscripcion;
		public String CNT_Emisor;
		public String CNT_Depositos;
		public String CNT_Bolsa;
		public String CNT_Corredora;
		public String CNT_CamaraDeCompensacion;
		public String CNT_Banco;
		public String CNT_Boveda;
		public String CNT_Otros;
		public Long PRO_IdAreaProveedor;
		public String EMP_CargoIndefinido;
		public String TipoEnrolamiento;
		public String CLI_AreaCliente;
		public String UsaDivisionPais;
		public String IdTipoCanalIngresoCorredor;
		public String CodigoPropositoTransaccion;
		public String CodigoRangoRenta;
		public String CodigoRangoNivelVenta;
		public Long POR_IdEmpleadoEjecutivoComercial;
		
		public BorradorEnrolamiento() {
			this.TipoEnrolamiento = "";
			this.IdPersonaConyuge = 0L;
			this.CLI_AreaCliente = "";
			this.IdTipoCanalIngresoCorredor = "";
		}
	}
	
	public static class SalidaCrearBorradorEnrolamiento extends BcsPojo{
		public Long	P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_REFID;
		public List<FormularioSocioNegocio> P_FORMULARIOS_SOCIO_NEGOCIO;
		public SalidaCrearBorradorEnrolamiento() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_REFID = 0L;
			this.P_FORMULARIOS_SOCIO_NEGOCIO = new ArrayList<FormularioSocioNegocio>(); 
		}
		
		public void agregarErroresMensajesSalida(SalidaAdministrarBorrador salidaAdministrarBorrador) {
			this.P_OUT_ERRORESCONTADOR += salidaAdministrarBorrador.P_OUT_ERRORESCONTADOR;
			this.P_OUT_MENSAJES.addAll(salidaAdministrarBorrador.P_OUT_MENSAJES);
		}
	}
	
	public static class EnrolamientoExistente extends BcsPojo{
		public Long IdEnrolamiento;
		public Long IdDireccionFisicaPrincipal;
		public Long IdDireccionElectronicaPrincipal;
		public Long IdTelefonoPrincipal;
		public Long IdDocumentoIdentidad;
		public Long IdDocumentoIdentidadAdicional;
		public String IdTipoCliente;
		public String TieneFatca;
	}
	
	public static class Pais extends BcsPojo{
		public Long IdPais;
		
		public Pais() {
			this.IdPais = 0L;
		}
	}
	
	public static class TipoIndicador extends BcsPojo{
		public String IdTipoEntidad;
		public String ValorIndicadorDefecto;
		public Long IdTipoIndicador;
		public String CodigoTipoIndicador;
	}
	
	public static class Persona extends BcsPojo{
		public Long IdPersona;
	}
	
	public static class ExcepcionResidencia extends BcsPojo{
		public Long IdEnrolamiento;
		public String ExcepcionResidencia;
	}
	
	public static class ExcepcionNacimiento extends BcsPojo{
		public Long IdEnrolamiento;
		public String ExcepcionNacimiento;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaCrearBorradorEnrolamiento jin = gson.fromJson(in.toString(), EntradaCrearBorradorEnrolamiento.class);
		SalidaCrearBorradorEnrolamiento out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaCrearBorradorEnrolamiento logica(Connection con, EntradaCrearBorradorEnrolamiento entrada) throws Exception {
		logger.info("INICIO::CrearCampoFormulario");
		long inicioServicio = System.currentTimeMillis();
		SalidaCrearBorradorEnrolamiento out = new SalidaCrearBorradorEnrolamiento();

		SalidaAdministrarBorrador salidaAdministrarBorrador = new SalidaAdministrarBorrador();
		
		entrada.p_ConsultarEnrolamiento = entrada.asignarCamposPorDefecto(con, entrada.p_ConsultarEnrolamiento, BorradorEnrolamiento.class);
		
		salidaAdministrarBorrador = entrada.validarCamposObligatoriosEntrada(con, entrada.p_ConsultarEnrolamiento, BorradorEnrolamiento.class, salidaAdministrarBorrador);
		out.P_OUT_ERRORESCONTADOR += salidaAdministrarBorrador.P_OUT_ERRORESCONTADOR;
		out.P_OUT_MENSAJES.addAll(salidaAdministrarBorrador.P_OUT_MENSAJES);
		if (out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}
		
		SalidaAdministrarBorrador salidaAsignarPerfilComercialPorDefecto = new SalidaAdministrarBorrador();
		asignarPerfilComercialPorDefecto(con, entrada.p_ConsultarEnrolamiento, entrada.p_IdEmpresa, entrada.p_ConsultarEnrolamiento.get(0).IdTipoCanalIngresoCorredor, salidaAsignarPerfilComercialPorDefecto);
		out.P_OUT_ERRORESCONTADOR += salidaAsignarPerfilComercialPorDefecto.P_OUT_ERRORESCONTADOR;
		out.P_OUT_MENSAJES.addAll(salidaAsignarPerfilComercialPorDefecto.P_OUT_MENSAJES);
		if (out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}
		
		SalidaAdministrarBorrador salidaAsignarTipoClientePorDefecto = new SalidaAdministrarBorrador();
		asignarTipoClientePorDefecto(con, entrada.p_ConsultarEnrolamiento, entrada.p_IdEmpresa, salidaAsignarTipoClientePorDefecto);
		out.P_OUT_ERRORESCONTADOR += salidaAsignarTipoClientePorDefecto.P_OUT_ERRORESCONTADOR;
		out.P_OUT_MENSAJES.addAll(salidaAsignarTipoClientePorDefecto.P_OUT_MENSAJES);
		if (out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}
		
		Long idEnrolamientoConDatosPersona = crearEnrolamientoPorDatosPersona(con, entrada);

		Enrolamiento enrolamiento = crearEntidadEnrolamiento(con, entrada.p_ConsultarEnrolamiento.get(0), idEnrolamientoConDatosPersona);

		SalidaAdministrarBorrador salidaCrearDocumentoIdentidad = crearDocumentoIdentidad(con, entrada, enrolamiento);
		enrolamiento = actualizarIdDocumentoIdentidad(salidaCrearDocumentoIdentidad.P_OUT_REFID, enrolamiento);
		enrolamiento = actualizarIdDocumentoIdentidadAdicional(salidaCrearDocumentoIdentidad.P_OUT_REFID, enrolamiento);

		SalidaAdministrarBorrador salidaModificarDireccionFisica = modificarDireccionFisica(con, entrada, enrolamiento);
		SalidaAdministrarBorrador salidaModificarDireccionElectronica = modificarDireccionElectronica(con, entrada, enrolamiento);
		SalidaAdministrarBorrador salidaModificarTelefono = modificarTelefono(con, entrada, enrolamiento);
		
		SalidaAdministrarBorrador salidaCrearDireccionFisica = crearDireccionFisica(con, entrada, enrolamiento);
		enrolamiento = actualizarIdDireccionFisicaPrincipal(salidaCrearDireccionFisica.P_OUT_REFID, enrolamiento);
		
		SalidaAdministrarBorrador salidaCrearDireccionElectronica = crearDireccionElectronica(con, entrada, enrolamiento);
		enrolamiento = actualizarIdDireccionElectronicaPrincipal(salidaCrearDireccionElectronica.P_OUT_REFID, enrolamiento);
		
		SalidaAdministrarBorrador salidaCrearTelefono = crearTelefono(con, entrada, enrolamiento);
		enrolamiento = actualizarIdTelefonoPrincipal(salidaCrearTelefono.P_OUT_REFID, enrolamiento);
		
		SalidaAdministrarBorrador salidaEliminarIndicador = eliminarIndicadoresInactivos(con, entrada, enrolamiento.IdEnrolamiento, enrolamiento.EsCambioTipoCliente);
		SalidaAdministrarBorrador salidaCrearIndicador = crearIndicadores(con, entrada, enrolamiento.IdEnrolamiento);
		
		if("N".equals(enrolamiento.TieneFatca)) {
			SalidaAdministrarBorrador salidaCrearBorradorFatcaDefecto = crearBorradorFatcaDefecto(con, entrada.p_IdEmpresa, enrolamiento);
			if(out.P_OUT_ERRORESCONTADOR > 0) {
				out.agregarErroresMensajesSalida(salidaCrearBorradorFatcaDefecto);
			}
		}
		
		Long idPaisSistema = ConfiguracionSistema.getParametroConfiguracionLong(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.PaisSistema");
		
		if(enrolamiento.IdPaisResidencia != idPaisSistema) {
			SalidaAdministrarBorrador salidaAdministrarBorradorFatcaPaisResidencia = administrarBorradorFatcaPaisResidencia(con, entrada.p_IdEmpresa, enrolamiento);
			if(out.P_OUT_ERRORESCONTADOR > 0) {
				out.agregarErroresMensajesSalida(salidaAdministrarBorradorFatcaPaisResidencia);
			}
		}
		
		if("S".equals(enrolamiento.NuevoRegistro)) {
			insertarEnrolamiento(con, enrolamiento);
		}else {
			actualizarEnrolamiento(con, enrolamiento);
		}
		
		actualizarExcepcionResidenciaFatca(con, enrolamiento);
		actualizarExcepcionNacimientoFatca(con, enrolamiento);
		
		out.agregarErroresMensajesSalida(salidaCrearDocumentoIdentidad);
		
		out.agregarErroresMensajesSalida(salidaModificarDireccionFisica);
		out.agregarErroresMensajesSalida(salidaModificarDireccionElectronica);
		out.agregarErroresMensajesSalida(salidaModificarTelefono);
		
		out.agregarErroresMensajesSalida(salidaCrearDireccionFisica);
		out.agregarErroresMensajesSalida(salidaCrearDireccionElectronica);
		out.agregarErroresMensajesSalida(salidaCrearTelefono);
		out.agregarErroresMensajesSalida(salidaEliminarIndicador);
		out.agregarErroresMensajesSalida(salidaCrearIndicador);

		out.P_OUT_REFID = enrolamiento.IdEnrolamiento;
		out.P_FORMULARIOS_SOCIO_NEGOCIO = ConsultarFormularioSocioNegocio.ObtenerFormulariosSocioNegocio(enrolamiento.Tipo, enrolamiento.TipoEnrolamiento);

		long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearCampoFormulario", finServicio - inicioServicio);
		return out;
	}

	private PerfilComercialBase obtenerPerfilComercialPorDefecto(Connection con, BorradorEnrolamiento borradorEnrolamiento, List<PerfilComercialBase> perfilesComercialesPorDefecto) throws SQLException {
		logger.trace("INICIO::CrearBorradorEnrolamiento.obtenerIdPerfilComercialPorDefecto");
		long inicio = System.currentTimeMillis();
		
		PerfilComercialBase perfilComercialBase = perfilesComercialesPorDefecto
				.stream()
				.filter(perfilComercialBasePorDefecto->esPerfilComercialBaseParaBorrador(perfilComercialBasePorDefecto, borradorEnrolamiento))
				.findFirst()
				.orElse(null);

		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearBorradorEnrolamiento.obtenerIdPerfilComercialPorDefecto", fin - inicio);

		return perfilComercialBase;
	}
	
	private Boolean esPerfilComercialBaseParaBorrador(PerfilComercialBase perfilComercialBase, BorradorEnrolamiento borradorEnrolamiento) {
		logger.trace("INICIO::CrearBorradorEnrolamiento.esPerfilComercialBaseParaBorrador");
		long inicio = System.currentTimeMillis();
		Boolean esPerfilComercialBaseParaBorrador = perfilComercialBase.IdCategoriaInversionista.equals(borradorEnrolamiento.CLI_IdCategoriaInversionista) && perfilComercialBase.TipoEnrolamiento.equals(borradorEnrolamiento.TipoEnrolamiento);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearBorradorEnrolamiento.esPerfilComercialBaseParaBorrador", fin - inicio);
		return esPerfilComercialBaseParaBorrador;
	}
	
	private TipoCliente obtenerTipoClientePorDefecto(Connection con, BorradorEnrolamiento borradorEnrolamiento, List<TipoCliente> tiposClientesPorDefecto) throws SQLException {
		logger.trace("INICIO::CrearBorradorEnrolamiento.obtenerTipoClientePorDefecto");
		long inicio = System.currentTimeMillis();

		TipoCliente tipoCliente = tiposClientesPorDefecto
				.stream()
				.filter(tipoClientePorDefecto->tipoClientePorDefecto.TipoEnrolamiento.equals(borradorEnrolamiento.TipoEnrolamiento))
				.findFirst()
				.orElse(null);

		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearBorradorEnrolamiento.obtenerTipoClientePorDefecto", fin - inicio);

		return tipoCliente;
	}
	
	private List<BorradorEnrolamiento> obtenerBorradoresEnrolamientoClientes(List<BorradorEnrolamiento> borradores){
		logger.trace("INICIO::CrearBorradorEnrolamiento.asignarPerfilComercialPorDefecto");
		long inicio = System.currentTimeMillis();
		List<BorradorEnrolamiento> borradoresEnrolamientoClientes = borradores.parallelStream().filter(borrador->"CLIENTE".equals(borrador.IdTipoEntidad)).collect(Collectors.toList());
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearBorradorEnrolamiento.asignarPerfilComercialPorDefecto", fin - inicio);
		return borradoresEnrolamientoClientes;
	}
	
	private void asignarPerfilComercialPorDefecto(Connection con, List<BorradorEnrolamiento> borradores, Long idEmpresa, String idCanal,  SalidaAdministrarBorrador salida ) throws Exception {
		logger.trace("INICIO::CrearBorradorEnrolamiento.asignarPerfilComercialPorDefecto");
		long inicio = System.currentTimeMillis();

		List<MensajeSalida> mensajes = new ArrayList<>();
		List<BorradorEnrolamiento> borradoresEnrolamientoClientes = obtenerBorradoresEnrolamientoClientes(borradores);
		List<PerfilComercialBase> perfilesComercialesPorDefecto = EnrolamientoUtils.obtenerPerfilesComercialesPorDefecto(con, idEmpresa);
		
		for (BorradorEnrolamiento borradorEnrolamiento : borradoresEnrolamientoClientes) {
			if (StringUtils.isNullOrBlank(borradorEnrolamiento.POR_IdPerfilComercial)) {
				PerfilComercialBase perfilComercialBase = obtenerPerfilComercialPorDefecto(con, borradorEnrolamiento, perfilesComercialesPorDefecto);

				if (perfilComercialBase == null) {
					HashMap<String, String> datosMensajes = new HashMap<>(2);
					datosMensajes.put("TipoEnrolamiento", borradorEnrolamiento.TipoEnrolamiento);
					datosMensajes.put("IdCategoriaInversionista", borradorEnrolamiento.CLI_IdCategoriaInversionista);

					if (idCanal.equals("AGE") & borradorEnrolamiento.TipoEnrolamiento.equals("FULL")) {
						mensajes.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_ENROLAMIENTO_CATEGORIA_INVERSIONISTA", datosMensajes, con, idEmpresa));
					}
					else
					{	
						mensajes.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_PERFIL_COMERCIAL_BASE_POR_DEFECTO_NO_EXISTE", datosMensajes, con, idEmpresa));
					}
				} else {
					borradorEnrolamiento.POR_IdPerfilComercial = perfilComercialBase.IdPerfilComercialBase;
				}

			}
		}

		salida.P_OUT_ERRORESCONTADOR += mensajes.size();
		salida.P_OUT_MENSAJES.addAll(mensajes);

		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearBorradorEnrolamiento.asignarPerfilComercialPorDefecto", fin - inicio);
	}
	
	private void asignarTipoClientePorDefecto(Connection con, List<BorradorEnrolamiento> borradores, Long idEmpresa, SalidaAdministrarBorrador salida) throws Exception {
		logger.trace("INICIO::CrearBorradorEnrolamiento.asignarTipoClientePorDefecto");
		long inicio = System.currentTimeMillis();

		List<MensajeSalida> mensajes = new ArrayList<>();
		List<BorradorEnrolamiento> borradoresEnrolamientoClientes = obtenerBorradoresEnrolamientoClientes(borradores);
		List<TipoCliente> tiposClientePorDefecto = EnrolamientoUtils.obtenerTiposClientePorDefecto(con, idEmpresa);
		
		for (BorradorEnrolamiento borradorEnrolamiento : borradoresEnrolamientoClientes) {
			if (StringUtils.isNullOrBlank(borradorEnrolamiento.CLI_IdTipoCliente)) {
				TipoCliente tipoCliente = obtenerTipoClientePorDefecto(con, borradorEnrolamiento, tiposClientePorDefecto);

				if (tipoCliente == null) {
					HashMap<String, String> datosMensajes = new HashMap<>(1);
					datosMensajes.put("TipoEnrolamiento", borradorEnrolamiento.TipoEnrolamiento);

					mensajes.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_TIPO_CLIENTE_POR_DEFECTO_NO_EXISTE", datosMensajes, con, idEmpresa));
				} else {
					borradorEnrolamiento.CLI_IdTipoCliente = tipoCliente.IdTipoCliente;
				}
			}
		}

		salida.P_OUT_ERRORESCONTADOR += mensajes.size();
		salida.P_OUT_MENSAJES.addAll(mensajes);

		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearBorradorEnrolamiento.asignarTipoClientePorDefecto", fin - inicio);
	}
	
	private Long crearEnrolamientoPorDatosPersona(Connection con, EntradaCrearBorradorEnrolamiento entrada) throws Exception {
		logger.trace("INICIO::CrearBorradorEnrolamiento.crearEntidadEnrolamiento");
		long inicio = System.currentTimeMillis();
		
		BorradorEnrolamiento borradorEnrolamiento = entrada.p_ConsultarEnrolamiento.get(0);
		
		HashMap<String, Object> parametros = new HashMap<String, Object>();
		parametros.put("IdTipoDocumentoIdentidad", borradorEnrolamiento.IdTipoDocumentoIdentidad);
		parametros.put("DocumentoIdentidad", borradorEnrolamiento.DocumentoIdentidad);
		parametros.put("IdTipoEntidad", borradorEnrolamiento.IdTipoEntidad);
		
		SQL sql = new SQL();
		List<Persona> personas = sql.query(con, SQL_SELECT_PERSONA_EXISTENTE_SIN_BORRADOR_ENROLAMIENTO, parametros, Persona.class);
		
		if(personas.size() > 0) {
			EntradaCrearBorradorEnrolamientoDatosPersona entradaCrearBorradorEnrolamientoDatosPersona = new EntradaCrearBorradorEnrolamientoDatosPersona();
			entradaCrearBorradorEnrolamientoDatosPersona.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaCrearBorradorEnrolamientoDatosPersona.p_IdUsuario = entrada.p_IdUsuario;
			entradaCrearBorradorEnrolamientoDatosPersona.p_IdModulo = entrada.p_IdModulo;
			entradaCrearBorradorEnrolamientoDatosPersona.p_UsaReglasNegocio = "N";
			entradaCrearBorradorEnrolamientoDatosPersona.p_Personas = personas.parallelStream().map(persona->{
				CrearBorradorEnrolamientoDatosPersona.Persona personaCreacion = new CrearBorradorEnrolamientoDatosPersona.Persona();
				personaCreacion.IdInterno = 1L;
				personaCreacion.IdPersona = persona.IdPersona;
				personaCreacion.IdTipoEntidadDestino = borradorEnrolamiento.IdTipoEntidad;
				personaCreacion.IdEntidadEnrolada = borradorEnrolamiento.IdEntidadEnrolada;
				return personaCreacion;
			}).collect(Collectors.toList());
			
			CrearBorradorEnrolamientoDatosPersona crearBorradorEnrolamientoDatosPersona = new CrearBorradorEnrolamientoDatosPersona();
			SalidaAdministrarBorrador salidaCrearBorradorEnrolamientoDatosPersona = crearBorradorEnrolamientoDatosPersona.logica(con, entradaCrearBorradorEnrolamientoDatosPersona);
			ResultadoAdministrarBorradorEntidad enrolamiento = salidaCrearBorradorEnrolamientoDatosPersona.P_OUT_REFID.parallelStream().findFirst().orElse(null);
			return enrolamiento.IdEntidad;
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearBorradorEnrolamiento.crearEntidadEnrolamiento", fin - inicio);
		return 0L;
	}
	
	private Enrolamiento crearEntidadEnrolamiento(Connection con, BorradorEnrolamiento borradorEnrolamiento, Long idEnrolamientoDatosPersona) throws Exception {
		logger.trace("INICIO::CrearBorradorEnrolamiento.crearEntidadEnrolamiento");
		long inicio = System.currentTimeMillis();

		EnrolamientoExistente enrolamientoExistente = obtenerEnrolamientoExistente(con, borradorEnrolamiento.IdTipoEntidad, borradorEnrolamiento.IdTipoDocumentoIdentidad, borradorEnrolamiento.DocumentoIdentidad);
		Enrolamiento enrolamiento = new Enrolamiento();
		String fechaStr = borradorEnrolamiento.EMP_FechaTerminoCargo;

		if (enrolamientoExistente == null) {
			List<Long> secuencias = Utils.getSecuencia(con, "BSCL.SocioNegocio.BD.Secuencias::BorradorEnrolamiento", 1L);
			Long idEnrolamiento = secuencias.get(0);
			enrolamiento.IdEnrolamiento = idEnrolamiento;
		} else {
			enrolamiento.IdEnrolamiento = enrolamientoExistente.IdEnrolamiento;
			enrolamiento.IdDocumentoIdentidad = enrolamientoExistente.IdDocumentoIdentidad;
			enrolamiento.IdDocumentoIdentidadAdicional = enrolamientoExistente.IdDocumentoIdentidadAdicional;
			enrolamiento.IdDireccionFisicaPrincipal = enrolamientoExistente.IdDireccionFisicaPrincipal;
			enrolamiento.IdDireccionElectronicaPrincipal = enrolamientoExistente.IdDireccionElectronicaPrincipal;
			enrolamiento.IdTelefonoPrincipal = enrolamientoExistente.IdTelefonoPrincipal;
			enrolamiento.EsCambioTipoCliente = esCambioTipoCliente(enrolamientoExistente.IdTipoCliente, borradorEnrolamiento.CLI_IdTipoCliente, idEnrolamientoDatosPersona, enrolamiento.IdEnrolamiento) ? "N" : "S";
			enrolamiento.TieneFatca = enrolamientoExistente.TieneFatca;
			enrolamiento.NuevoRegistro = "N";
		}
		
		enrolamiento.IdEmpresa = borradorEnrolamiento.IdEmpresa;
		enrolamiento.IdUsuario = borradorEnrolamiento.IdUsuario;
		enrolamiento.IdTipoEntidad = borradorEnrolamiento.IdTipoEntidad;
		enrolamiento.IdEntidadEnrolada = borradorEnrolamiento.IdEntidadEnrolada;
		enrolamiento.Tipo = borradorEnrolamiento.Tipo;
		enrolamiento.Nombres = borradorEnrolamiento.Nombres;
		enrolamiento.ApellidoPaterno = borradorEnrolamiento.ApellidoPaterno;
		enrolamiento.ApellidoMaterno = borradorEnrolamiento.ApellidoMaterno;
		enrolamiento.RazonSocial = borradorEnrolamiento.RazonSocial;
		enrolamiento.IdPaisNacimiento = borradorEnrolamiento.IdPaisNacimiento;
		enrolamiento.IdPaisResidencia = borradorEnrolamiento.IdPaisResidencia;
		enrolamiento.FechaNacimiento = borradorEnrolamiento.FechaNacimiento;
		enrolamiento.Sexo = borradorEnrolamiento.Sexo;
		enrolamiento.EstadoCivil = borradorEnrolamiento.EstadoCivil;
		enrolamiento.IdPersonaConyuge = borradorEnrolamiento.IdPersonaConyuge;
		enrolamiento.RegimenConyugal = borradorEnrolamiento.RegimenConyugal;
		enrolamiento.CLI_IdCategoriaInversionista = borradorEnrolamiento.CLI_IdCategoriaInversionista;
		enrolamiento.CLI_IdTipoCliente = borradorEnrolamiento.CLI_IdTipoCliente;
		enrolamiento.CLI_IdTipoClienteCMF = borradorEnrolamiento.CLI_IdTipoClienteCMF;
		enrolamiento.CLI_IdEmpleadoEjecutivo = borradorEnrolamiento.CLI_IdEmpleadoEjecutivo;
		enrolamiento.CLI_CodigoAdicionalHolding = borradorEnrolamiento.CLI_CodigoAdicionalHolding;
		enrolamiento.CLI_IdClaseCliente = borradorEnrolamiento.CLI_IdClaseCliente;
		enrolamiento.POR_IdPerfilComercial = borradorEnrolamiento.POR_IdPerfilComercial;
		enrolamiento.EMP_CodigoEmpleado = borradorEnrolamiento.EMP_CodigoEmpleado;
		enrolamiento.EMP_CodigoTipoCargoEmpleado = borradorEnrolamiento.EMP_CodigoTipoCargoEmpleado;
		enrolamiento.EMP_IdEmpleadoSupervisor = borradorEnrolamiento.EMP_IdEmpleadoSupervisor;
		enrolamiento.EMP_IdSucursal = borradorEnrolamiento.EMP_IdSucursal;
		enrolamiento.EMP_FechaInicioCargo = Fecha.toDate(borradorEnrolamiento.EMP_FechaInicioCargo);
		enrolamiento.EMP_FechaTerminoCargo = (fechaStr != null && !fechaStr.trim().isEmpty()) ? Fecha.toDate(fechaStr) : null;
		enrolamiento.CNT_CodigoInscripcion = borradorEnrolamiento.CNT_CodigoInscripcion;
		enrolamiento.CNT_Emisor = borradorEnrolamiento.CNT_Emisor;
		enrolamiento.CNT_Depositos = borradorEnrolamiento.CNT_Depositos;
		enrolamiento.CNT_Bolsa = borradorEnrolamiento.CNT_Bolsa;
		enrolamiento.CNT_Corredora = borradorEnrolamiento.CNT_Corredora;
		enrolamiento.CNT_CamaraDeCompensacion = borradorEnrolamiento.CNT_CamaraDeCompensacion;
		enrolamiento.CNT_Banco = borradorEnrolamiento.CNT_Banco;
		enrolamiento.CNT_Boveda = borradorEnrolamiento.CNT_Boveda;
		enrolamiento.CNT_Otros = borradorEnrolamiento.CNT_Otros;
		enrolamiento.PRO_IdAreaProveedor = borradorEnrolamiento.PRO_IdAreaProveedor;
		enrolamiento.EMP_CargoIndefinido = borradorEnrolamiento.EMP_CargoIndefinido;
		enrolamiento.TipoEnrolamiento = borradorEnrolamiento.TipoEnrolamiento;
		enrolamiento.CLI_AreaCliente = borradorEnrolamiento.CLI_AreaCliente;
		enrolamiento.IdTipoCanalIngresoCorredor = borradorEnrolamiento.IdTipoCanalIngresoCorredor;
		enrolamiento.CodigoPropositoTransaccion = ("N".equals(borradorEnrolamiento.Tipo) && borradorEnrolamiento.CodigoPropositoTransaccion != null) ? borradorEnrolamiento.CodigoPropositoTransaccion : "";
		enrolamiento.CodigoRangoRenta="N".equals(borradorEnrolamiento.Tipo) ? borradorEnrolamiento.CodigoRangoRenta : "";
		enrolamiento.CodigoRangoNivelVenta="J".equals(borradorEnrolamiento.Tipo) ? borradorEnrolamiento.CodigoRangoNivelVenta : "";
		enrolamiento.POR_IdEmpleadoEjecutivoComercial = borradorEnrolamiento.POR_IdEmpleadoEjecutivoComercial;
		
		
		try {
			enrolamiento.CLI_IdSegmentoCliente = obtenerSegmentoPorDefecto(con);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearBorradorEnrolamiento.crearEntidadEnrolamiento", fin - inicio);
		return enrolamiento;
	}
	
	private Long obtenerSegmentoPorDefecto(Connection conn) throws SQLException {
		logger.trace("INICIO::CrearBorradorEnrolamiento.obtenerSegmentoPorDefecto");
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
		logger.trace("FIN::CrearBorradorEnrolamiento.obtenerSegmentoPorDefecto", fin - inicio);

		return idSegmento;
	}

	
	
	private EnrolamientoExistente obtenerEnrolamientoExistente(Connection con, String idTipoEntidad, String idTipoDocumentoIdentidad, String documentoIdentidad) throws Exception {
		logger.debug("INICIO::CrearBorradorEnrolamiento.obtenerEnrolamientoExistente");
		long inicio = System.currentTimeMillis();
		
		HashMap<String, Object> parametros = new HashMap<String, Object>(3);
		parametros.put("IdTipoEntidad", idTipoEntidad);
		parametros.put("IdTipoDocumentoIdentidad", idTipoDocumentoIdentidad);
		parametros.put("DocumentoIdentidad", documentoIdentidad);
		
		SQL sql = new SQL();
		List<EnrolamientoExistente> enrolamientosExistentes = sql.query(con, SQL_SELECT_ENROLAMIENTO_BORRADOR, parametros, EnrolamientoExistente.class);
		
		EnrolamientoExistente enrolamientoExistente = enrolamientosExistentes.parallelStream().findFirst().orElse(null);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearBorradorEnrolamiento.obtenerEnrolamientoExistente", fin - inicio);
		return enrolamientoExistente;
	}
	
	private Boolean esCambioTipoCliente(String tipoCliente, String tipoClienteEntrada, Long idEnrolamientoDatosPersona, Long idEnrolamientoExistente) {
		logger.trace("INICIO::CrearBorradorEnrolamiento.esCambioTipoCliente");
		long inicio = System.currentTimeMillis();
		Boolean esCambioTipoCliente = tipoCliente.equals(tipoClienteEntrada) || idEnrolamientoDatosPersona.equals(idEnrolamientoExistente);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearBorradorEnrolamiento.esCambioTipoCliente", fin - inicio);
		return esCambioTipoCliente;
	}
	
	private SalidaAdministrarBorrador crearDocumentoIdentidad(Connection con, EntradaCrearBorradorEnrolamiento entrada, Enrolamiento enrolamiento) throws Exception {
		logger.debug("INICIO::CrearBorradorEnrolamiento.crearDocumentoIdentidad");
		long inicio = System.currentTimeMillis();
		
		SalidaAdministrarBorrador salidaAdministrarBorrador = new SalidaAdministrarBorrador();
		if("S".equals(enrolamiento.NuevoRegistro)) {
			BorradorEnrolamiento borradorEnrolamiento = entrada.p_ConsultarEnrolamiento.get(0);
			
			DocumentoIdentidad documentoIdentidad = new DocumentoIdentidad();
			documentoIdentidad.IdInterno = 1L;
			documentoIdentidad.IdEnrolamiento = enrolamiento.IdEnrolamiento;
			documentoIdentidad.IdTipoDocumentoIdentidad = borradorEnrolamiento.IdTipoDocumentoIdentidad;
			documentoIdentidad.DocumentoIdentidad = borradorEnrolamiento.DocumentoIdentidad;
			documentoIdentidad.IdTipoDocumentoIdentidadAsociado = borradorEnrolamiento.IdTipoDocumentoIdentidadAsociado;
			documentoIdentidad.TipoPersona = enrolamiento.Tipo;
			documentoIdentidad.Predeterminado = "S";
			documentoIdentidad.NumeroSerie = "";
			documentoIdentidad.FechaEmision = "0001-01-01";
			documentoIdentidad.FechaVencimiento = "0001-01-01";
			
			EntradaCrearBorradorDocumentoIdentidad entradaCrearBorradorDocumentoIdentidad = new EntradaCrearBorradorDocumentoIdentidad();
			entradaCrearBorradorDocumentoIdentidad.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaCrearBorradorDocumentoIdentidad.p_IdUsuario = entrada.p_IdUsuario;
			entradaCrearBorradorDocumentoIdentidad.p_IdModulo = entrada.p_IdModulo;
			entradaCrearBorradorDocumentoIdentidad.p_UsaReglasNegocio = "N";
			entradaCrearBorradorDocumentoIdentidad.p_CodigoFormulario = entrada.p_CodigoFormulario;
			entradaCrearBorradorDocumentoIdentidad.p_IdTipoCanal = entrada.p_IdTipoCanal;
			entradaCrearBorradorDocumentoIdentidad.p_DocumentosIdentidades = Arrays.asList(documentoIdentidad);
			
			if(esDocumentoIdentidadAdicionalValido(borradorEnrolamiento.DocumentoIdentidadAdicional)) {
				DocumentoIdentidad documentoIdentidadAdicional = new DocumentoIdentidad();
				documentoIdentidadAdicional.IdInterno = 2L;
				documentoIdentidadAdicional.IdEnrolamiento = enrolamiento.IdEnrolamiento;
				documentoIdentidadAdicional.IdTipoDocumentoIdentidad = borradorEnrolamiento.IdTipoDocumentoIdentidadAdicional;
				documentoIdentidadAdicional.DocumentoIdentidad = borradorEnrolamiento.DocumentoIdentidadAdicional;
				documentoIdentidadAdicional.IdTipoDocumentoIdentidadAsociado = "";
				documentoIdentidadAdicional.TipoPersona = enrolamiento.Tipo;
				documentoIdentidadAdicional.Predeterminado = "N";
				documentoIdentidad.NumeroSerie = "";
				documentoIdentidad.FechaEmision = "0001-01-01";
				documentoIdentidad.FechaVencimiento = "0001-01-01";
				entradaCrearBorradorDocumentoIdentidad.p_DocumentosIdentidades.add(documentoIdentidadAdicional);
			}
			
			CrearBorradorDocumentoIdentidad crearDocumentoIdentidad = new CrearBorradorDocumentoIdentidad();
			salidaAdministrarBorrador = crearDocumentoIdentidad.logica(con, entradaCrearBorradorDocumentoIdentidad);
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearBorradorEnrolamiento.crearDocumentoIdentidad", fin - inicio);
		return salidaAdministrarBorrador;
	}
	
	private Boolean esDocumentoIdentidadAdicionalValido(String documentoIdentidadAdicional) {
		logger.trace("INICIO::CrearBorradorEnrolamiento.esDocumentoIdentidadAdicionalValido");
		long inicio = System.currentTimeMillis();
		Boolean esDocumentoIdentidadAdicionalValido = !documentoIdentidadAdicional.isEmpty();
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearBorradorEnrolamiento.esDocumentoIdentidadAdicionalValido", fin - inicio);
		return esDocumentoIdentidadAdicionalValido;
	}
	
	private Enrolamiento actualizarIdDocumentoIdentidad(List<ResultadoAdministrarBorradorEntidad> resultadosAdministrarBorradorEntidad, Enrolamiento enrolamiento) {
		logger.trace("INICIO::CrearBorradorEnrolamiento.actualizarIdDocumentoIdentidad");
		long inicio = System.currentTimeMillis();
		
		Map<Long, ResultadoAdministrarBorradorEntidad> resultadosAdministrarBorradorEntidadPorIdInterno = obtenerResultadoAdministrarBorradorEntidadPorIdDocumento(resultadosAdministrarBorradorEntidad);
		ResultadoAdministrarBorradorEntidad resultadoAdministrarBorradorEntidad = resultadosAdministrarBorradorEntidadPorIdInterno.get(1L);
		
		if(esResultadoAdministrarBorradorEntidadValido(resultadoAdministrarBorradorEntidad)) {
			enrolamiento.IdDocumentoIdentidad = resultadoAdministrarBorradorEntidad.IdEntidad;
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearBorradorEnrolamiento.actualizarIdDocumentoIdentidad", fin - inicio);
		return enrolamiento;
	}
	
	private Enrolamiento actualizarIdDocumentoIdentidadAdicional(List<ResultadoAdministrarBorradorEntidad> resultadosAdministrarBorradorEntidad, Enrolamiento enrolamiento) {
		logger.trace("INICIO::CrearBorradorEnrolamiento.actualizarIdDocumentoIdentidadAdicional");
		long inicio = System.currentTimeMillis();
		
		Map<Long, ResultadoAdministrarBorradorEntidad> resultadosAdministrarBorradorEntidadPorIdInterno = obtenerResultadoAdministrarBorradorEntidadPorIdDocumento(resultadosAdministrarBorradorEntidad);
		ResultadoAdministrarBorradorEntidad resultadoAdministrarBorradorEntidad = resultadosAdministrarBorradorEntidadPorIdInterno.get(2L);
		
		if(esResultadoAdministrarBorradorEntidadValido(resultadoAdministrarBorradorEntidad)) {
			enrolamiento.IdDocumentoIdentidadAdicional = resultadoAdministrarBorradorEntidad.IdEntidad;
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearBorradorEnrolamiento.actualizarIdDocumentoIdentidadAdicional", fin - inicio);
		return enrolamiento;
	}
	
	private SalidaAdministrarBorrador modificarDireccionFisica(Connection con, EntradaCrearBorradorEnrolamiento entrada, Enrolamiento enrolamiento) throws Exception {
		logger.debug("INICIO::CrearBorradorEnrolamiento.modificarDireccionFisica");
		long inicio = System.currentTimeMillis();
		
		SalidaAdministrarBorrador salidaAdministrarBorrador = new SalidaAdministrarBorrador();
		if(esIdEntidadExistente(enrolamiento.IdDireccionFisicaPrincipal)) {
			BorradorEnrolamiento borradorEnrolamiento = entrada.p_ConsultarEnrolamiento.get(0);
			
			DireccionFisica direccionFisica = new DireccionFisica();
			direccionFisica.IdInterno = 1L;
			direccionFisica.IdDireccionFisica = enrolamiento.IdDireccionFisicaPrincipal;
			direccionFisica.IdEnrolamiento = enrolamiento.IdEnrolamiento;
			direccionFisica.Calle = borradorEnrolamiento.DireccionCalle;
			direccionFisica.Numero = borradorEnrolamiento.DireccionNumero;
			direccionFisica.Departamento = borradorEnrolamiento.DireccionDepartamento;
			direccionFisica.IdCiudad = borradorEnrolamiento.IdCiudad;
			direccionFisica.IdDivisionPais = borradorEnrolamiento.IdDivisionPais;
			direccionFisica.IdPais = borradorEnrolamiento.IdPaisResidencia;
			direccionFisica.CodigoProposito = "J".equals(enrolamiento.Tipo) ? "COME" : "PART";
			direccionFisica.Predeterminado = "S";
			direccionFisica.Sector = "";
			direccionFisica.CodigoPostal = "";
			direccionFisica.Comentario = "";
			direccionFisica.CodigoCompuesto = "";
			
			ModificarBorradorDireccionFisica modificarBorradorDireccionFisica = new ModificarBorradorDireccionFisica();
			EntradaModificarBorradorDireccionFisica entradaModificarBorradorDireccionFisica = new EntradaModificarBorradorDireccionFisica();
			entradaModificarBorradorDireccionFisica.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaModificarBorradorDireccionFisica.p_IdUsuario = entrada.p_IdUsuario;
			entradaModificarBorradorDireccionFisica.p_IdModulo = entrada.p_IdModulo;
			entradaModificarBorradorDireccionFisica.p_UsaReglasNegocio = "N";
			entradaModificarBorradorDireccionFisica.p_CodigoFormulario = entrada.p_CodigoFormulario;
			entradaModificarBorradorDireccionFisica.p_IdTipoCanal = entrada.p_IdTipoCanal;
			entradaModificarBorradorDireccionFisica.p_DireccionesFisicas = Arrays.asList(direccionFisica);
			
			salidaAdministrarBorrador = modificarBorradorDireccionFisica.logica(con, entradaModificarBorradorDireccionFisica);
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearBorradorEnrolamiento.modificarDireccionFisica", fin - inicio);
		return salidaAdministrarBorrador;
	}
	
	private SalidaAdministrarBorrador modificarDireccionElectronica(Connection con, EntradaCrearBorradorEnrolamiento entrada, Enrolamiento enrolamiento) throws Exception {
		logger.debug("INICIO::CrearBorradorEnrolamiento.modificarDireccionElectronica");
		long inicio = System.currentTimeMillis();
		
		SalidaAdministrarBorrador salidaAdministrarBorrador = new SalidaAdministrarBorrador();
		if(esIdEntidadExistente(enrolamiento.IdDireccionElectronicaPrincipal)) {
			BorradorEnrolamiento borradorEnrolamiento = entrada.p_ConsultarEnrolamiento.get(0);
			
			DireccionElectronica direccionElectronica = new DireccionElectronica();
			direccionElectronica.IdInterno = 1L;
			direccionElectronica.IdDireccionElectronica = enrolamiento.IdDireccionElectronicaPrincipal;
			direccionElectronica.IdEnrolamiento = enrolamiento.IdEnrolamiento;
			direccionElectronica.Direccion = borradorEnrolamiento.Email;
			direccionElectronica.CodigoProposito = "J".equals(enrolamiento.Tipo) ? "COME" : "PART";
			direccionElectronica.Predeterminado = "S";
			direccionElectronica.Comentario = "";
			
			ModificarBorradorDireccionElectronica modificarBorradorDireccionElectronica = new ModificarBorradorDireccionElectronica();
			EntradaModificarBorradorDireccionElectronica entradaModificarBorradorDireccionElectronica = new EntradaModificarBorradorDireccionElectronica();
			entradaModificarBorradorDireccionElectronica.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaModificarBorradorDireccionElectronica.p_IdUsuario = entrada.p_IdUsuario;
			entradaModificarBorradorDireccionElectronica.p_IdModulo = entrada.p_IdModulo;
			entradaModificarBorradorDireccionElectronica.p_UsaReglasNegocio = "N";
			entradaModificarBorradorDireccionElectronica.p_CodigoFormulario = entrada.p_CodigoFormulario;
			entradaModificarBorradorDireccionElectronica.p_IdTipoCanal = entrada.p_IdTipoCanal;
			entradaModificarBorradorDireccionElectronica.p_DireccionesElectronicas = Arrays.asList(direccionElectronica);
			
			salidaAdministrarBorrador = modificarBorradorDireccionElectronica.logica(con, entradaModificarBorradorDireccionElectronica);
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearBorradorEnrolamiento.modificarDireccionElectronica", fin - inicio);
		return salidaAdministrarBorrador;
	}
	
	private SalidaAdministrarBorrador modificarTelefono(Connection con, EntradaCrearBorradorEnrolamiento entrada, Enrolamiento enrolamiento) throws Exception {
		logger.debug("INICIO::CrearBorradorEnrolamiento.modificarTelefono");
		long inicio = System.currentTimeMillis();
		
		SalidaAdministrarBorrador salidaAdministrarBorrador = new SalidaAdministrarBorrador();
		if(esIdEntidadExistente(enrolamiento.IdTelefonoPrincipal)) {
			BorradorEnrolamiento borradorEnrolamiento = entrada.p_ConsultarEnrolamiento.get(0);
			
			Telefono telefono = new Telefono();
			telefono.IdInterno = 1L;
			telefono.IdTelefono = enrolamiento.IdTelefonoPrincipal;
			telefono.IdEnrolamiento = enrolamiento.IdEnrolamiento;
			telefono.IdPais = obtenerIdPaisTelefono(con, borradorEnrolamiento.IdCiudad);
			telefono.IdTipoTelefono = borradorEnrolamiento.IdTipoTelefono;
			telefono.Numero = borradorEnrolamiento.Telefono;
			telefono.CodigoProposito = "J".equals(enrolamiento.Tipo) ? "COME" : "PART";
			telefono.Predeterminado = "S";
			telefono.Anexo = "";
			telefono.Comentario = "";
			
			ModificarBorradorTelefono modificarBorradorTelefono = new ModificarBorradorTelefono();
			EntradaModificarBorradorTelefono entradaModificarBorradorTelefono = new EntradaModificarBorradorTelefono();
			entradaModificarBorradorTelefono.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaModificarBorradorTelefono.p_IdUsuario = entrada.p_IdUsuario;
			entradaModificarBorradorTelefono.p_IdModulo = entrada.p_IdModulo;
			entradaModificarBorradorTelefono.p_UsaReglasNegocio = "N";
			entradaModificarBorradorTelefono.p_CodigoFormulario = entrada.p_CodigoFormulario;
			entradaModificarBorradorTelefono.p_IdTipoCanal = entrada.p_IdTipoCanal;
			entradaModificarBorradorTelefono.p_Telefonos = Arrays.asList(telefono);
			
			salidaAdministrarBorrador = modificarBorradorTelefono.logica(con, entradaModificarBorradorTelefono);
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearBorradorEnrolamiento.modificarTelefono", fin - inicio);
		return salidaAdministrarBorrador;
	}
	
	private SalidaAdministrarBorrador crearDireccionFisica(Connection con, EntradaCrearBorradorEnrolamiento entrada, Enrolamiento enrolamiento) throws Exception {
		logger.debug("INICIO::CrearBorradorEnrolamiento.crearDireccionFisica");
		long inicio = System.currentTimeMillis();
		
		SalidaAdministrarBorrador salidaAdministrarBorrador = new SalidaAdministrarBorrador();
		BorradorEnrolamiento borradorEnrolamiento = entrada.p_ConsultarEnrolamiento.get(0);
		
		if(esDireccionFisicaNueva(enrolamiento.IdDireccionFisicaPrincipal, borradorEnrolamiento)) {
			DireccionFisica direccionFisica = new DireccionFisica();
			direccionFisica.IdInterno = 1L;
			direccionFisica.IdDireccionFisica = 0L;
			direccionFisica.IdEnrolamiento = enrolamiento.IdEnrolamiento;
			direccionFisica.Calle = borradorEnrolamiento.DireccionCalle;
			direccionFisica.Numero = borradorEnrolamiento.DireccionNumero;
			direccionFisica.Departamento = borradorEnrolamiento.DireccionDepartamento;
			direccionFisica.IdCiudad = borradorEnrolamiento.IdCiudad;
			direccionFisica.IdDivisionPais = borradorEnrolamiento.IdDivisionPais;
			direccionFisica.CodigoProposito = "J".equals(enrolamiento.Tipo) ? "COME" : "PART";
			direccionFisica.Predeterminado = "S";
			direccionFisica.Sector = "";
			direccionFisica.CodigoPostal = "";
			direccionFisica.Comentario = "";
			direccionFisica.CodigoCompuesto = "";
			
			CrearBorradorDireccionFisica crearBorradorDireccionFisica = new CrearBorradorDireccionFisica();
			EntradaCrearBorradorDireccionFisica entradaCrearBorradorDireccionFisica = new EntradaCrearBorradorDireccionFisica();
			entradaCrearBorradorDireccionFisica.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaCrearBorradorDireccionFisica.p_IdUsuario = entrada.p_IdUsuario;
			entradaCrearBorradorDireccionFisica.p_IdModulo = entrada.p_IdModulo;
			entradaCrearBorradorDireccionFisica.p_UsaReglasNegocio = "N";
			entradaCrearBorradorDireccionFisica.p_CodigoFormulario = entrada.p_CodigoFormulario;
			entradaCrearBorradorDireccionFisica.p_IdTipoCanal = entrada.p_IdTipoCanal;
			entradaCrearBorradorDireccionFisica.p_DireccionesFisicas = Arrays.asList(direccionFisica);
			
			salidaAdministrarBorrador = crearBorradorDireccionFisica.logica(con, entradaCrearBorradorDireccionFisica);
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearBorradorEnrolamiento.crearDireccionFisica", fin - inicio);
		return salidaAdministrarBorrador;
	}
	
	private Boolean esDireccionFisicaNueva(Long idDireccionFisicaPrincipal, BorradorEnrolamiento borradorEnrolamiento) {
		logger.trace("INICIO::CrearBorradorEnrolamiento.esDireccionFisicaNueva");
		long inicio = System.currentTimeMillis();
		Boolean esDireccionFisicaNueva = Long.valueOf(0L).equals(idDireccionFisicaPrincipal) && esDireccionFisicaConValores(borradorEnrolamiento);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearBorradorEnrolamiento.esDireccionFisicaNueva", fin - inicio);
		return esDireccionFisicaNueva;
	}
	
	private Boolean esDireccionFisicaConValores(BorradorEnrolamiento borradorEnrolamiento) {
		logger.trace("INICIO::CrearBorradorEnrolamiento.esDireccionFisicaConValores");
		long inicio = System.currentTimeMillis();
		Boolean esDireccionFisicaConValores = !borradorEnrolamiento.DireccionCalle.isEmpty() || !borradorEnrolamiento.DireccionNumero.isEmpty() || !borradorEnrolamiento.DireccionDepartamento.isEmpty() || !Long.valueOf(0L).equals(borradorEnrolamiento.IdCiudad) || !Long.valueOf(0L).equals(borradorEnrolamiento.IdDivisionPais);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearBorradorEnrolamiento.esDireccionFisicaConValores", fin - inicio);
		return esDireccionFisicaConValores;
	}
	
	private Enrolamiento actualizarIdDireccionFisicaPrincipal(List<ResultadoAdministrarBorradorEntidad> resultadosAdministrarBorradorEntidad, Enrolamiento enrolamiento) {
		logger.trace("INICIO::CrearBorradorEnrolamiento.actualizarIdDireccionFisicaPrincipal");
		long inicio = System.currentTimeMillis();
		
		Map<Long, ResultadoAdministrarBorradorEntidad> resultadosAdministrarBorradorEntidadPorIdInterno = obtenerResultadoAdministrarBorradorEntidadPorIdDocumento(resultadosAdministrarBorradorEntidad);
		ResultadoAdministrarBorradorEntidad resultadoAdministrarBorradorEntidad = resultadosAdministrarBorradorEntidadPorIdInterno.get(1L);
		
		if(esResultadoAdministrarBorradorEntidadValido(resultadoAdministrarBorradorEntidad)) {
			enrolamiento.IdDireccionFisicaPrincipal = resultadoAdministrarBorradorEntidad.IdEntidad;
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearBorradorEnrolamiento.actualizarIdDireccionFisicaPrincipal", fin - inicio);
		return enrolamiento;
	}
	
	private SalidaAdministrarBorrador crearDireccionElectronica(Connection con, EntradaCrearBorradorEnrolamiento entrada, Enrolamiento enrolamiento) throws Exception {
		logger.debug("INICIO::CrearBorradorEnrolamiento.crearDireccionElectronica");
		long inicio = System.currentTimeMillis();
		
		SalidaAdministrarBorrador salidaAdministrarBorrador = new SalidaAdministrarBorrador();
		BorradorEnrolamiento borradorEnrolamiento = entrada.p_ConsultarEnrolamiento.get(0);
		if(esDireccionElectronicaNueva(enrolamiento.IdDireccionElectronicaPrincipal, borradorEnrolamiento.Email)) {
			
			DireccionElectronica direccionElectronica = new DireccionElectronica();
			direccionElectronica.IdInterno = 1L;
			direccionElectronica.IdDireccionElectronica = 0L;
			direccionElectronica.IdEnrolamiento = enrolamiento.IdEnrolamiento;
			direccionElectronica.Direccion = borradorEnrolamiento.Email;
			direccionElectronica.CodigoProposito = "J".equals(enrolamiento.Tipo) ? "COME" : "PART";
			direccionElectronica.Predeterminado = "S";
			direccionElectronica.Comentario = "";
			
			CrearBorradorDireccionElectronica crearBorradorDireccionElectronica = new CrearBorradorDireccionElectronica();
			EntradaCrearBorradorDireccionElectronica entradaCrearBorradorDireccionElectronica = new EntradaCrearBorradorDireccionElectronica();
			entradaCrearBorradorDireccionElectronica.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaCrearBorradorDireccionElectronica.p_IdUsuario = entrada.p_IdUsuario;
			entradaCrearBorradorDireccionElectronica.p_IdModulo = entrada.p_IdModulo;
			entradaCrearBorradorDireccionElectronica.p_UsaReglasNegocio = "N";
			entradaCrearBorradorDireccionElectronica.p_CodigoFormulario = entrada.p_CodigoFormulario;
			entradaCrearBorradorDireccionElectronica.p_IdTipoCanal = entrada.p_IdTipoCanal;
			entradaCrearBorradorDireccionElectronica.p_DireccionesElectronicas = Arrays.asList(direccionElectronica);
			
			salidaAdministrarBorrador = crearBorradorDireccionElectronica.logica(con, entradaCrearBorradorDireccionElectronica);
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearBorradorEnrolamiento.crearDireccionElectronica", fin - inicio);
		return salidaAdministrarBorrador;
	}
	
	private Boolean esDireccionElectronicaNueva(Long idDireccionElectronicaPrincipal, String email) {
		logger.trace("INICIO::CrearBorradorEnrolamiento.esDireccionElectronicaNueva");
		long inicio = System.currentTimeMillis();
		Boolean esDireccionElectronicaNueva = Long.valueOf(0L).equals(idDireccionElectronicaPrincipal) && !email.isEmpty();
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearBorradorEnrolamiento.esDireccionElectronicaNueva", fin - inicio);
		return esDireccionElectronicaNueva;
	}
	
	private Enrolamiento actualizarIdDireccionElectronicaPrincipal(List<ResultadoAdministrarBorradorEntidad> resultadosAdministrarBorradorEntidad, Enrolamiento enrolamiento) {
		logger.trace("INICIO::CrearBorradorEnrolamiento.actualizarIdDireccionElectronicaPrincipal");
		long inicio = System.currentTimeMillis();
		
		Map<Long, ResultadoAdministrarBorradorEntidad> resultadosAdministrarBorradorEntidadPorIdInterno = obtenerResultadoAdministrarBorradorEntidadPorIdDocumento(resultadosAdministrarBorradorEntidad);
		ResultadoAdministrarBorradorEntidad resultadoAdministrarBorradorEntidad = resultadosAdministrarBorradorEntidadPorIdInterno.get(1L);
		
		if(esResultadoAdministrarBorradorEntidadValido(resultadoAdministrarBorradorEntidad)) {
			enrolamiento.IdDireccionElectronicaPrincipal = resultadoAdministrarBorradorEntidad.IdEntidad;
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearBorradorEnrolamiento.actualizarIdDireccionElectronicaPrincipal", fin - inicio);
		return enrolamiento;
	}
	
	private SalidaAdministrarBorrador crearTelefono(Connection con, EntradaCrearBorradorEnrolamiento entrada, Enrolamiento enrolamiento) throws Exception {
		logger.debug("INICIO::CrearBorradorEnrolamiento.crearTelefono");
		long inicio = System.currentTimeMillis();
		
		SalidaAdministrarBorrador salidaAdministrarBorrador = new SalidaAdministrarBorrador();
		BorradorEnrolamiento borradorEnrolamiento = entrada.p_ConsultarEnrolamiento.get(0);
		
		if(esTelefonoNuevo(enrolamiento.IdTelefonoPrincipal, borradorEnrolamiento.IdTipoTelefono, borradorEnrolamiento.Telefono)) {
			
			Telefono telefono = new Telefono();
			telefono.IdInterno = 1L;
			telefono.IdTelefono = enrolamiento.IdTelefonoPrincipal;
			telefono.IdEnrolamiento = enrolamiento.IdEnrolamiento;
			telefono.IdPais = obtenerIdPaisTelefono(con, borradorEnrolamiento.IdCiudad);
			telefono.IdTipoTelefono = borradorEnrolamiento.IdTipoTelefono;
			telefono.Numero = borradorEnrolamiento.Telefono;
			telefono.CodigoProposito = "J".equals(enrolamiento.Tipo) ? "COME" : "PART";
			telefono.Predeterminado = "S";
			telefono.Anexo = "";
			telefono.Comentario = "";
			
			CrearBorradorTelefono crearBorradorTelefono = new CrearBorradorTelefono();
			EntradaCrearBorradorTelefono entradaCrearBorradorTelefono = new EntradaCrearBorradorTelefono();
			entradaCrearBorradorTelefono.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaCrearBorradorTelefono.p_IdUsuario = entrada.p_IdUsuario;
			entradaCrearBorradorTelefono.p_IdModulo = entrada.p_IdModulo;
			entradaCrearBorradorTelefono.p_UsaReglasNegocio = "N";
			entradaCrearBorradorTelefono.p_CodigoFormulario = entrada.p_CodigoFormulario;
			entradaCrearBorradorTelefono.p_IdTipoCanal = entrada.p_IdTipoCanal;
			entradaCrearBorradorTelefono.p_Telefonos = Arrays.asList(telefono);
			
			salidaAdministrarBorrador = crearBorradorTelefono.logica(con, entradaCrearBorradorTelefono);
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearBorradorEnrolamiento.crearTelefono", fin - inicio);
		return salidaAdministrarBorrador;
	}

	private Boolean esTelefonoNuevo(Long idTelefonoPrincipal, String idTipoTelefono, String telefono) {
		logger.trace("INICIO::CrearBorradorEnrolamiento.esTelefonoNuevo");
		long inicio = System.currentTimeMillis();
		Boolean esTelefonoNuevo = Long.valueOf(0L).equals(idTelefonoPrincipal) && !idTipoTelefono.isEmpty() && !telefono.isEmpty();
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearBorradorEnrolamiento.esTelefonoNuevo", fin - inicio);
		return esTelefonoNuevo;
	}
	
	private Enrolamiento actualizarIdTelefonoPrincipal(List<ResultadoAdministrarBorradorEntidad> resultadosAdministrarBorradorEntidad, Enrolamiento enrolamiento) {
		logger.trace("INICIO::CrearBorradorEnrolamiento.actualizarIdTelefonoPrincipal");
		long inicio = System.currentTimeMillis();
		
		Map<Long, ResultadoAdministrarBorradorEntidad> resultadosAdministrarBorradorEntidadPorIdInterno = obtenerResultadoAdministrarBorradorEntidadPorIdDocumento(resultadosAdministrarBorradorEntidad);
		ResultadoAdministrarBorradorEntidad resultadoAdministrarBorradorEntidad = resultadosAdministrarBorradorEntidadPorIdInterno.get(1L);
		
		if(esResultadoAdministrarBorradorEntidadValido(resultadoAdministrarBorradorEntidad)) {
			enrolamiento.IdTelefonoPrincipal = resultadoAdministrarBorradorEntidad.IdEntidad;
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearBorradorEnrolamiento.actualizarIdTelefonoPrincipal", fin - inicio);
		return enrolamiento;
	}
	
	private SalidaAdministrarBorrador eliminarIndicadoresInactivos(Connection con, EntradaCrearBorradorEnrolamiento entrada, Long idEnrolamiento, String esCambioTipoCliente) throws Exception {
		logger.debug("INICIO::CrearBorradorEnrolamiento.eliminarIndicadores");
		long inicio = System.currentTimeMillis();
		
		SalidaAdministrarBorrador salidaAdministrarBorrador = new SalidaAdministrarBorrador();
		List<Indicador> indicadoresInactivos = obtenerIndicadoresInactivos(con, idEnrolamiento, entrada.p_ConsultarEnrolamiento.get(0).CLI_IdTipoCliente, esCambioTipoCliente);
		
		if(indicadoresInactivos.size() > 0) {
			EliminarBorradorIndicador eliminarBorradorIndicador = new EliminarBorradorIndicador();
			EntradaEliminarBorradorIndicador entradaEliminarBorradorIndicador = new EntradaEliminarBorradorIndicador();
			entradaEliminarBorradorIndicador.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaEliminarBorradorIndicador.p_IdUsuario = entrada.p_IdUsuario;
			entradaEliminarBorradorIndicador.p_IdModulo = entrada.p_IdModulo;
			entradaEliminarBorradorIndicador.p_UsaReglasNegocio = "N";
			entradaEliminarBorradorIndicador.p_CodigoFormulario = entrada.p_CodigoFormulario;
			entradaEliminarBorradorIndicador.p_IdTipoCanal = entrada.p_IdTipoCanal;
			entradaEliminarBorradorIndicador.p_Indicadores = indicadoresInactivos;
			salidaAdministrarBorrador = eliminarBorradorIndicador.logica(con, entradaEliminarBorradorIndicador);
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearBorradorEnrolamiento.eliminarIndicadores", fin - inicio);
		return salidaAdministrarBorrador;
	}
	
	private List<Indicador> obtenerIndicadoresInactivos(Connection con, Long idEnrolamiento, String idTipoCliente, String esCambioTipoCliente) throws Exception {
		logger.debug("INICIO::CrearBorradorEnrolamiento.obtenerIndicadoresInactivos");
		long inicio = System.currentTimeMillis();
		
		HashMap<String, Object> parametros = new HashMap<String, Object>(3);
		parametros.put("IdEnrolamiento", idEnrolamiento);
		parametros.put("IdTipoCliente", idTipoCliente);
		parametros.put("EsCambioTipoCliente", esCambioTipoCliente);
		
		SQL sql = new SQL();
		List<Indicador> indicadoresInactivos = sql.query(con, SQL_SELECT_INDICADORES_ELIMINACION, parametros, Indicador.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearBorradorEnrolamiento.obtenerIndicadoresInactivos", fin - inicio);
		return indicadoresInactivos;
	}
	
	private SalidaAdministrarBorrador crearIndicadores(Connection con, EntradaCrearBorradorEnrolamiento entrada, Long idEnrolamiento) throws Exception{
		logger.debug("INICIO::CrearBorradorEnrolamiento.crearIndicadores");
		long inicio = System.currentTimeMillis();
		
		SalidaAdministrarBorrador salidaAdministrarBorrador = new SalidaAdministrarBorrador();
		List<Indicador> indicadoresCrear = obtenerIndicadoresCrear(con, idEnrolamiento, entrada.p_ConsultarEnrolamiento.get(0).CLI_IdTipoCliente, entrada.p_IdEmpresa, entrada.p_ConsultarEnrolamiento.get(0).CLI_IdTipoClienteCMF);
		
		if(indicadoresCrear.size() > 0) {
			CrearBorradorIndicador crearBorradorIndicador = new CrearBorradorIndicador();
			EntradaCrearBorradorIndicador entradaCrearBorradorIndicador = new EntradaCrearBorradorIndicador();
			entradaCrearBorradorIndicador.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaCrearBorradorIndicador.p_IdUsuario = entrada.p_IdUsuario;
			entradaCrearBorradorIndicador.p_IdModulo = entrada.p_IdModulo;
			entradaCrearBorradorIndicador.p_UsaReglasNegocio = "N";
			entradaCrearBorradorIndicador.p_CodigoFormulario = entrada.p_CodigoFormulario;
			entradaCrearBorradorIndicador.p_IdTipoCanal = entrada.p_IdTipoCanal;
			entradaCrearBorradorIndicador.p_Indicadores = indicadoresCrear;
			salidaAdministrarBorrador = crearBorradorIndicador.logica(con, entradaCrearBorradorIndicador);
			
			
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearBorradorEnrolamiento.crearIndicadores", fin - inicio);
		return salidaAdministrarBorrador;
	}
	
	private List<Indicador> obtenerIndicadoresCrear(Connection con, Long idEnrolamiento, String idTipoCliente, Long idEmpresa, Long IdTipoClienteCMF) throws Exception {
		logger.debug("INICIO::CrearBorradorEnrolamiento.obtenerIndicadoresCrear");
		long inicio = System.currentTimeMillis();
		
		HashMap<String, Object> parametros = new HashMap<String, Object>(2);
		parametros.put("IdEnrolamiento", idEnrolamiento);
		parametros.put("IdTipoCliente", idTipoCliente);
		
		SQL sql = new SQL();
		List<TipoIndicador> tipoIndicadoresCrear = sql.query(con, SQL_SELECT_INDICADORES_CREACION, parametros, TipoIndicador.class);
		
		List<Indicador> indicadoresCrear = tipoIndicadoresCrear.parallelStream().map(tipoIndicador->{
			Indicador indicador = new Indicador();
			indicador.IdInterno = 0L;
			indicador.IdIndicador = 0L;
			indicador.IdEnrolamiento = idEnrolamiento;
			indicador.IdTipoEntidad = tipoIndicador.IdTipoEntidad;
			indicador.IdTipoIndicador = tipoIndicador.IdTipoIndicador;
			indicador.ModeloCliente = "";
			indicador.IdEmpresa = idEmpresa;
			indicador.CodigoTipoIndicador=tipoIndicador.CodigoTipoIndicador;
			if (tipoIndicador.CodigoTipoIndicador.equalsIgnoreCase("TCLICMF")  || tipoIndicador.CodigoTipoIndicador.equalsIgnoreCase("TCLICMFJ") )
			{
				indicador.ValorIndicador = IdTipoClienteCMF.toString();
			}else{
				indicador.ValorIndicador = tipoIndicador.ValorIndicadorDefecto;
				
				};
			return indicador;
		}).collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearBorradorEnrolamiento.obtenerIndicadoresCrear", fin - inicio);
		return indicadoresCrear;
	}
	
	private SalidaAdministrarBorrador crearBorradorFatcaDefecto(Connection con, Long idEmpresa, Enrolamiento enrolamiento) throws Exception{
		logger.debug("INICIO::CrearBorradorEnrolamiento.crearBorradorFatcaDefecto");
		long inicio = System.currentTimeMillis();
		
		SalidaAdministrarBorrador salidaAdministrarBorrador = new SalidaAdministrarBorrador();
		
		DatosPais paisUsa = Utils.getDatosPais(con, "US");
		
		List<Fatca> fatcas = new ArrayList<Fatca>();
		Fatca fatca = new Fatca();
		fatca.IdEnrolamiento = enrolamiento.IdEnrolamiento;
		fatca.IdClasificacionEmpresaFatca = "J".equals(enrolamiento.TipoEnrolamiento) ? "PENDIENTE" : "";
		fatca.ExcepcionResidente = enrolamiento.IdPaisResidencia.compareTo(paisUsa.IdPais) == 0 ? "S" : "N";
		fatca.ExcepcionNacimiento = enrolamiento.IdPaisNacimiento.compareTo(paisUsa.IdPais) == 0 ? "S" : "N";
		
		fatcas.add(fatca);
		
		CrearBorradorFatca crearBorradorFatca = new CrearBorradorFatca();
		EntradaCrearBorradorFatca entradaCrearBorradorFatca = new EntradaCrearBorradorFatca();
		entradaCrearBorradorFatca.p_Fatcas = fatcas;
		
		salidaAdministrarBorrador = crearBorradorFatca.logica(con, entradaCrearBorradorFatca);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearBorradorEnrolamiento.crearBorradorFatcaDefecto", fin - inicio);
		return salidaAdministrarBorrador;
	}

	private SalidaAdministrarBorrador administrarBorradorFatcaPaisResidencia(Connection con, Long idEmpresa, Enrolamiento enrolamiento) throws Exception {
		logger.debug("INICIO::CrearBorradorEnrolamiento.administrarBorradorFatcaPaisResidencia");
		long inicio = System.currentTimeMillis();
		SalidaAdministrarBorrador salidaAdministrarBorradorFatcaPaisResidencia = new SalidaAdministrarBorrador();
		SQL sql = new SQL();
		HashMap<String,Object> paramsSelect = new HashMap<String,Object>();
		paramsSelect.put("IdEnrolamiento", enrolamiento.IdEnrolamiento);	
	
		List<FatcaPaisResidencia> fatcasPaisResidenciaPrincipal = sql.query(con, SQL_SELECT_FATCA_PAIS_RESIDENCIA, paramsSelect, FatcaPaisResidencia.class);		
		FatcaPaisResidencia fatcaPaisResidenciaPrincipal = fatcasPaisResidenciaPrincipal.parallelStream().findFirst().orElse(null);
		
		List<FatcaPaisResidencia> fatcasPaisResidencia = new ArrayList<FatcaPaisResidencia>();	
		FatcaPaisResidencia fatcaPaisResidencia = new FatcaPaisResidencia();
		fatcaPaisResidencia.IdEnrolamiento = enrolamiento.IdEnrolamiento;
		fatcaPaisResidencia.IdPaisResidencia = enrolamiento.IdPaisResidencia;
		fatcaPaisResidencia.TIN = "";
		fatcaPaisResidencia.IdCausaNoTin = "OTRA";
		fatcaPaisResidencia.Observacion = "Pendiente de ingreso";
		fatcaPaisResidencia.PaisResidenciaPrincipal = "S";
		fatcasPaisResidencia.add(fatcaPaisResidencia);
		
		if(fatcasPaisResidenciaPrincipal.isEmpty()) {						
			salidaAdministrarBorradorFatcaPaisResidencia = crearBorradorFatcaPaisResidencia(con, idEmpresa, fatcasPaisResidencia);			
		}else if(fatcaPaisResidenciaPrincipal.IdPaisResidencia != enrolamiento.IdPaisResidencia){		
			salidaAdministrarBorradorFatcaPaisResidencia = modificarBorradorFatcaPaisResidencia(con, idEmpresa, fatcasPaisResidencia);
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearBorradorEnrolamiento.administrarBorradorFatcaPaisResidencia", fin - inicio);
		return salidaAdministrarBorradorFatcaPaisResidencia;
	}
	
	private SalidaAdministrarBorrador crearBorradorFatcaPaisResidencia(Connection con, Long idEmpresa, List<FatcaPaisResidencia> fatcasPaisResidencia) throws Exception {
		logger.debug("INICIO::CrearBorradorEnrolamiento.crearBorradorFatcaPaisResidencia");
		long inicio = System.currentTimeMillis();
		SalidaAdministrarBorrador salidaCrearBorradorFatcaPaisResidencia = new SalidaAdministrarBorrador();
		CrearBorradorFatcaPaisResidencia crearBorradorFatcaPaisResidencia = new CrearBorradorFatcaPaisResidencia();
		EntradaCrearBorradorFatcaPaisResidencia entradaCrearBorradorFatcaPaisResidencia = new EntradaCrearBorradorFatcaPaisResidencia();
		
		entradaCrearBorradorFatcaPaisResidencia.p_IdEmpresa = idEmpresa;
		entradaCrearBorradorFatcaPaisResidencia.p_FatcaPaisResidencia = fatcasPaisResidencia;

		salidaCrearBorradorFatcaPaisResidencia = crearBorradorFatcaPaisResidencia.logica(con, entradaCrearBorradorFatcaPaisResidencia);		
				
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearBorradorEnrolamiento.crearBorradorFatcaPaisResidencia", fin - inicio);
		return salidaCrearBorradorFatcaPaisResidencia;
	}
	
	private SalidaAdministrarBorrador modificarBorradorFatcaPaisResidencia(Connection con, Long idEmpresa, List<FatcaPaisResidencia> fatcasPaisResidencia) throws Exception {
		logger.debug("INICIO::CrearBorradorEnrolamiento.modificarBorradorFatcaPaisResidencia");
		long inicio = System.currentTimeMillis();
		SalidaAdministrarBorrador salidaModificarBorradorFatcaPaisResidencia = new SalidaAdministrarBorrador();
		ModificarBorradorFatcaPaisResidencia modificarBorradorFatcaPaisResidencia = new ModificarBorradorFatcaPaisResidencia();
		EntradaModificarBorradorFatcaPaisResidencia entradaModificarBorradorFatcaPaisResidencia = new EntradaModificarBorradorFatcaPaisResidencia();
		
		entradaModificarBorradorFatcaPaisResidencia.p_IdEmpresa = idEmpresa;	
		entradaModificarBorradorFatcaPaisResidencia.p_FatcaPaisResidencia = fatcasPaisResidencia;
		
		salidaModificarBorradorFatcaPaisResidencia = modificarBorradorFatcaPaisResidencia.logica(con, entradaModificarBorradorFatcaPaisResidencia);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearBorradorEnrolamiento.modificarBorradorFatcaPaisResidencia", fin - inicio);
		return salidaModificarBorradorFatcaPaisResidencia;
	}
		
	private void insertarEnrolamiento(Connection con, Enrolamiento enrolamiento) throws Exception {
		logger.debug("INICIO::CrearBorradorEnrolamiento.insertarEnrolamiento");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		List<Enrolamiento> enrolamientos = Arrays.asList(enrolamiento);
		sql.batch(con, SQL_INSERT_ENROLAMIENTO_BORRADOR, enrolamientos, Enrolamiento.class);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearBorradorEnrolamiento.insertarEnrolamiento", fin - inicio);
	}
	
	private void actualizarEnrolamiento(Connection con, Enrolamiento enrolamiento) throws Exception {
		logger.debug("INICIO::CrearBorradorEnrolamiento.actualizarEnrolamiento");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		List<Enrolamiento> enrolamientos = Arrays.asList(enrolamiento);
		sql.batch(con, SQL_UPDATE_ENROLAMIENTO_BORRADOR, enrolamientos, Enrolamiento.class);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearBorradorEnrolamiento.actualizarEnrolamiento", fin - inicio);
	}
	
	private Map<Long, ResultadoAdministrarBorradorEntidad> obtenerResultadoAdministrarBorradorEntidadPorIdDocumento(List<ResultadoAdministrarBorradorEntidad> resultadosAdministrarBorradorEntidad){
		logger.trace("INICIO::CrearBorradorEnrolamiento.crearTelefono");
		long inicio = System.currentTimeMillis();
		Map<Long, ResultadoAdministrarBorradorEntidad> resultadosAdministrarBorradorEntidadPorIdInterno = resultadosAdministrarBorradorEntidad.parallelStream().collect(Collectors.toMap(resultado->resultado.IdInterno, resultado->resultado));
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearBorradorEnrolamiento.crearTelefono", fin - inicio);
		return resultadosAdministrarBorradorEntidadPorIdInterno;
	}
	
	private Boolean esResultadoAdministrarBorradorEntidadValido(ResultadoAdministrarBorradorEntidad resultadoAdministrarBorradorEntidad) {
		logger.trace("INICIO::CrearBorradorEnrolamiento.esResultadoAdministrarBorradorEntidadValido");
		long inicio = System.currentTimeMillis();
		Boolean esResultadoAdministrarBorradorEntidadValido = resultadoAdministrarBorradorEntidad != null;
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearBorradorEnrolamiento.esResultadoAdministrarBorradorEntidadValido", fin - inicio);
		return esResultadoAdministrarBorradorEntidadValido;
	}
	
	private Boolean esIdEntidadExistente(Long idEntidad) {
		logger.trace("INICIO::CrearBorradorEnrolamiento.esIdEntidadExistente");
		long inicio = System.currentTimeMillis();
		Boolean esIdEntidadExistente = !idEntidad.equals(0L);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearBorradorEnrolamiento.esIdEntidadExistente", fin - inicio);
		return esIdEntidadExistente;
	}
	
	private Long obtenerIdPaisTelefono(Connection con, Long idCiudad) throws Exception {
		logger.trace("INICIO::CrearBorradorEnrolamiento.obtenerIdPaisTelefono");
		long inicio = System.currentTimeMillis();
		
		HashMap<String, Object> parametros = new HashMap<String, Object>();
		parametros.put("IdCiudad", idCiudad);
		
		SQL sql = new SQL();
		List<Pais> paises = sql.query(con, SQL_SELECT_PAIS, parametros, Pais.class);
		
		Pais pais = paises.parallelStream().findFirst().orElse(new Pais());
		
		Long idPais = pais.IdPais;
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearBorradorEnrolamiento.obtenerIdPaisTelefono", fin - inicio);
		return idPais;
	}
	
	private void actualizarExcepcionNacimientoFatca(Connection con, Enrolamiento enrolamiento) throws SQLException {
		logger.debug("INICIO::CrearBorradorEnrolamiento.actualizarExcepcionNacimientoFatca");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		DatosPais paisUsa = Utils.getDatosPais(con, "US");
		ExcepcionNacimiento excepcionNacimiento = new ExcepcionNacimiento();
		excepcionNacimiento.IdEnrolamiento = enrolamiento.IdEnrolamiento; 
		excepcionNacimiento.ExcepcionNacimiento = enrolamiento.IdPaisNacimiento.compareTo(paisUsa.IdPais) == 0 ? "S" : "N";
		List<ExcepcionNacimiento> excepciones = new ArrayList<ExcepcionNacimiento>();
		excepciones.add(excepcionNacimiento);
		sql.batch(con, SQL_UPDATE_FATCA_EXCEPCION_NACIMIENTO, excepciones, ExcepcionNacimiento.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearBorradorEnrolamiento.actualizarExcepcionNacimientoFatca", fin - inicio);
	}

	private void actualizarExcepcionResidenciaFatca(Connection con, Enrolamiento enrolamiento) throws SQLException {
		logger.debug("INICIO::CrearBorradorEnrolamiento.actualizarExcepcionResidenciaFatca");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		DatosPais paisUsa = Utils.getDatosPais(con, "US");
		ExcepcionResidencia excepcionResidencia = new ExcepcionResidencia();
		excepcionResidencia.IdEnrolamiento = enrolamiento.IdEnrolamiento; 
		excepcionResidencia.ExcepcionResidencia = enrolamiento.IdPaisResidencia.compareTo(paisUsa.IdPais) == 0 ? "S" : "N";
		List<ExcepcionResidencia> excepciones = new ArrayList<ExcepcionResidencia>();
		excepciones.add(excepcionResidencia);
		sql.batch(con, SQL_UPDATE_FATCA_EXCEPCION_RESIDENCIA, excepciones, ExcepcionResidencia.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearBorradorEnrolamiento.actualizarExcepcionResidenciaFatca", fin - inicio);
	}


	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");		                  
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();
			
			String jsonEntrada = "{\"p_ConsultarEnrolamiento\":[{\"IdEnrolamiento\":0,\"IdEmpresa\":1,\"IdUsuario\":4,\"IdTipoEntidad\":\"CLIENTE\",\"IdEntidadEnrolada\":0,\"IdTipoDocumentoIdentidad\":\"R\",\"IdTipoDocumentoIdentidadAsociado\":\"R\",\"DocumentoIdentidad\":\"13006360-8\",\"IdTipoDocumentoIdentidadAdicional\":\"R\",\"DocumentoIdentidadAdicional\":\"\",\"Tipo\":\"N\",\"Nombres\":\"HFKJ\",\"ApellidoPaterno\":\"ASDA\",\"ApellidoMaterno\":\"ASDSA\",\"RazonSocial\":\"\",\"IdPaisNacimiento\":44,\"IdPaisResidencia\":44,\"FechaNacimiento\":\"2021-04-01\",\"Sexo\":\"F\",\"EstadoCivil\":\"SO\",\"IdCiudad\":173,\"IdDivisionPais\":343,\"IdTipoTelefono\":\"MOVIL\",\"Telefono\":\"+56 1 2345 6879\",\"Email\":\"ASD@AD.COM\",\"DireccionCalle\":\"DSFDS\",\"DireccionNumero\":\"123\",\"DireccionDepartamento\":\"ASD\",\"IdPersonaConyuge\":0,\"RegimenConyugal\":\"\",\"CLI_IdCategoriaInversionista\":\"BALANCEADO\",\"CLI_IdTipoCliente\":\"RETAIL\",\"CLI_IdTipoClienteCMF\":805,\"CLI_IdClaseCliente\":\"\",\"CLI_IdEmpleadoEjecutivo\":11579,\"CLI_CodigoAdicionalHolding\":\"\",\"CLI_AreaCliente\":\"POR DEFINIR\",\"POR_IdPerfilComercial\":\"DEF BALANCEADO F\",\"EMP_CodigoEmpleado\":\"\",\"EMP_CodigoTipoCargoEmpleado\":\"\",\"EMP_IdEmpleadoSupervisor\":0,\"EMP_IdSucursal\":0,\"EMP_FechaInicioCargo\":\"0001-01-01\",\"EMP_FechaTerminoCargo\":\"0001-01-01\",\"CNT_CodigoInscripcion\":\"\",\"CNT_Emisor\":\"\",\"CNT_Depositos\":\"\",\"CNT_Bolsa\":\"\",\"CNT_Corredora\":\"\",\"CNT_CamaraDeCompensacion\":\"\",\"CNT_Banco\":\"\",\"CNT_Boveda\":\"\",\"CNT_Otros\":\"\",\"PRO_IdAreaProveedor\":0,\"EMP_CargoIndefinido\":\"\",\"TipoEnrolamiento\":\"FULL\",\"UsaDivisionPais\":\"S\"}],\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"S\",\"p_CodigoFormulario\":\"INGRESAR_CLIENTE_FULL_NATURAL\",\"p_IdTipoCanal\":\"OPT\"}\n";
			EntradaCrearBorradorEnrolamiento entradaCrearBorradorEnrolamiento = gson.fromJson(jsonEntrada, EntradaCrearBorradorEnrolamiento.class);
			CrearBorradorEnrolamiento crearBorradorEnrolamiento = new CrearBorradorEnrolamiento();
			try {
				SalidaCrearBorradorEnrolamiento salidaCrearSolicitudAporteRescateNormal = crearBorradorEnrolamiento.logica(con, entradaCrearBorradorEnrolamiento);
				con.rollback();
				System.out.println(salidaCrearSolicitudAporteRescateNormal.toString());
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
						
    	}
		System.out.println("== END ==");
	}
}
