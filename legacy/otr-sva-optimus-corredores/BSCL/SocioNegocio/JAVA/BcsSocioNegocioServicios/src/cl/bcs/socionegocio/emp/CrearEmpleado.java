package cl.bcs.socionegocio.emp;

import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.aut.CrearEventoAutorizacion;
import cl.bcs.plataforma.aut.CrearEventoAutorizacion.ServiceEntrada;
import cl.bcs.plataforma.aut.CrearEventoAutorizacion.ServiceSalida;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.socionegocio.cli.pojo.ConsultarEnrolamiento;
import cl.bcs.socionegocio.soc.ValidarDocumentoIdentidad;
import cl.bcs.socionegocio.soc.ValidarDocumentoIdentidad.SalidaValidarDocumentoIdentidad;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

/**
 * Migracion de BSCL.SocioNegocio.BD.Servicios::EMP.CrearEmpleado
 */
public class CrearEmpleado implements JavaCall {

	static final Logger logger = LogManager.getLogger(CrearEmpleado.class);

	private final String SQL_COUNT_CODIGO_EMPLEADO_EXISTE = loadResourceToString("cl/bcs/socionegocio/emp/sql/CrearEmpleado_CountCodigoEmpleadoExiste.sql");
	private final String SQL_SELECT_MAXIMO_CODIGO_EMPLEADO = loadResourceToString("cl/bcs/socionegocio/emp/sql/CrearEmpleado_SelectMaximoCodigoEmpleado.sql");
	private final String SQL_COUNT_ENROLAMIENTO = loadResourceToString("cl/bcs/socionegocio/emp/sql/CrearEmpleado_CountEnrolamiento.sql");
	private final String SQL_SELECT_ENROLAMIENTO = loadResourceToString("cl/bcs/socionegocio/emp/sql/CrearEmpleado_SelectEnrolamiento.sql");
	private final String SQL_INSERT_ENROLAMIENTO = loadResourceToString("cl/bcs/socionegocio/emp/sql/CrearEmpleado_InsertEnrolamiento.sql");
	private final String SQL_SELECT_ID_PAIS_CIUDAD = loadResourceToString("cl/bcs/socionegocio/emp/sql/CrearEmpleado_SelectIdPaisCiudad.sql");
	private final String SQL_COUNT_PERSONA_EXISTE = loadResourceToString("cl/bcs/socionegocio/emp/sql/CrearEmpleado_CountPersonaExiste.sql");
	private final String SQL_COUNT_PERSONA_DOCIDENTIDAD_ADICIONAL_EXISTE = loadResourceToString("cl/bcs/socionegocio/emp/sql/CrearEmpleado_CountPersonaDocumentoIdentidadAdicionalExiste.sql");
	private final String SQL_INSERT_PERSONA = loadResourceToString("cl/bcs/socionegocio/emp/sql/CrearEmpleado_InsertPersona.sql");
	private final String SQL_INSERT_SOCIODENEGOCIO = loadResourceToString("cl/bcs/socionegocio/emp/sql/CrearEmpleado_InsertSocioDeNegocio.sql");
	private final String SQL_INSERT_FATCA = loadResourceToString("cl/bcs/socionegocio/emp/sql/CrearEmpleado_InsertFatca.sql");
	private final String SQL_INSERT_DOCUMENTO_IDENTIDAD = loadResourceToString("cl/bcs/socionegocio/emp/sql/CrearEmpleado_InsertDocumentoIdentidad.sql");
	private final String SQL_INSERT_DOCUMENTO_IDENTIDAD_ADICIONAL = loadResourceToString("cl/bcs/socionegocio/emp/sql/CrearEmpleado_InsertDocumentoIdentidadAdicional.sql");
	private final String SQL_INSERT_DIRECCION_FISICA = loadResourceToString("cl/bcs/socionegocio/emp/sql/CrearEmpleado_InsertDireccionFisica.sql");
	private final String SQL_INSERT_TELEFONO = loadResourceToString("cl/bcs/socionegocio/emp/sql/CrearEmpleado_InsertTelefono.sql");
	private final String SQL_INSERT_DIRECCION_ELECTRONICA = loadResourceToString("cl/bcs/socionegocio/emp/sql/CrearEmpleado_InsertDireccionElectronica.sql");
	private final String SQL_UPDATE_PERSONA_PRINCIPALES = loadResourceToString("cl/bcs/socionegocio/emp/sql/CrearEmpleado_UpdatePersonaPrincipales.sql");
	private final String SQL_SELECT_SOCIONEGOCIO_PERSONA_EXISTENTE = loadResourceToString("cl/bcs/socionegocio/emp/sql/CrearEmpleado_SelectSocioNegocioPersonaExistente.sql");
	private final String SQL_COUNT_PERSONA_EMPLEADOR = loadResourceToString("cl/bcs/socionegocio/emp/sql/CrearEmpleado_CountPersonaEmpleador.sql");
	private final String SQL_INSERT_EMPLEADO = loadResourceToString("cl/bcs/socionegocio/emp/sql/CrearEmpleado_InsertEmpleado.sql");
	private final String SQL_UPDATE_SOCIODENEGOCIO_IDEMPLEADO = loadResourceToString("cl/bcs/socionegocio/emp/sql/CrearEmpleado_UpdateSocioDeNegocioIdEmpleado.sql");
	private final String SQL_UPDATE_ENROLAMIENTO_IDENTIDADENROLADA = loadResourceToString("cl/bcs/socionegocio/emp/sql/CrearEmpleado_UpdateEnrolamientoIdEntidadEnrolada.sql");

	public static class Entrada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public List<ConsultarEnrolamiento> p_ConsultarEnrolamiento;

		public Entrada() {
			this.p_ConsultarEnrolamiento = new ArrayList<>();
		}
	}

	public static class Salida {
		public Long P_OUT_REFID;
		public int P_OUT_ADVERTENCIASCONTADOR;
		public List<AdvertenciaDetalle> P_OUT_ADVERTENCIASDETALLE;
		public int P_OUT_ERRORESCONTADOR;
		public List<ErrorDetalle> P_OUT_ERRORESDETALLE;

		public Salida() {
			this.P_OUT_REFID = 0L;
			this.P_OUT_ADVERTENCIASCONTADOR = 0;
			this.P_OUT_ADVERTENCIASDETALLE = new ArrayList<>();
			this.P_OUT_ERRORESCONTADOR = 0;
			this.P_OUT_ERRORESDETALLE = new ArrayList<>();
		}

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class ErrorDetalle {
		public String Codigo;
		public String Descripcion;

		public ErrorDetalle(String codigo, String descripcion) {
			this.Codigo = codigo;
			this.Descripcion = descripcion;
		}
	}

	public static class AdvertenciaDetalle {
		public String Codigo;
		public String RequiereAutorizacion;
		public String Descripcion;

		public AdvertenciaDetalle(String codigo, String requiereAutorizacion, String descripcion) {
			this.Codigo = codigo;
			this.RequiereAutorizacion = requiereAutorizacion;
			this.Descripcion = descripcion;
		}
	}

	// ---- Filas de lectura ----
	public static class Contador {
		public Long Contador;
	}

	public static class ValorConfiguracion {
		public String Valor;
	}

	public static class IdEnrolamientoRow {
		public Long IdEnrolamiento;
	}

	public static class IdPaisRow {
		public Long IdPais;
	}

	public static class SocioPersonaExistenteRow {
		public Long IdSocioDeNegocio;
		public Long IdPersona;
		public Long IdDireccionElectronicaPrincipal;
	}

	// ---- Filas de escritura (bind por reflexion via SQL.batch) ----
	public static class EnrolamientoInsertRow {
		public Long IdEnrolamiento;
		public Long IdEmpresa;
		public Long IdUsuario;
		public String IdTipoEntidad;
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
		public Date FechaNacimiento;
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
		public String CLI_IdCategoriaInversionista;
		public String POR_IdPerfilComercial;
		public String CLI_IdTipoCliente;
		public String CLI_IdClaseCliente;
		public String EMP_CargoIndefinido;
	}

	public static class PersonaInsertRow {
		public Long IdPersona;
		public String Tipo;
		public String Nombres;
		public String ApellidoPaterno;
		public String ApellidoMaterno;
		public String RazonSocial;
		public Long IdPaisNacimiento;
		public Long IdPaisResidencia;
		public Date FechaNacimiento;
		public String Sexo;
		public String EstadoCivil;
		public String EsEnrolado;
		public Long IdUsuarioCreador;
	}

	public static class SocioDeNegocioInsertRow {
		public Long IdSocioDeNegocio;
		public Long IdPersona;
		public Long IdEmpresa;
	}

	public static class FatcaInsertRow {
		public Long IdPersona;
		public String IdClasificacionEmpresaFatca;
		public String FechaIngreso;
		public Long IdUsuarioCreador;
	}

	public static class DocumentoIdentidadInsertRow {
		public Long IdDocumentoIdentidad;
		public Long IdPersona;
		public String IdTipoDocumentoIdentidad;
		public String IdTipoDocumentoIdentidadAsociado;
		public String DocumentoIdentidad;
		public Long IdUsuarioCreador;
		public String TipoPersona;
	}

	public static class DocumentoIdentidadAdicionalInsertRow {
		public Long IdDocumentoIdentidad;
		public Long IdPersona;
		public String IdTipoDocumentoIdentidad;
		public String DocumentoIdentidad;
		public Long IdUsuarioCreador;
		public String TipoPersona;
	}

	public static class DireccionFisicaInsertRow {
		public Long IdDireccionFisica;
		public String Calle;
		public String Numero;
		public String Departamento;
		public Long IdCiudad;
		public Long IdDivisionPais;
		public String CodigoProposito;
		public Long IdPersona;
		public Long IdUsuarioCreador;
	}

	public static class TelefonoInsertRow {
		public Long IdTelefono;
		public Long IdPais;
		public String IdTipoTelefono;
		public String Numero;
		public String CodigoProposito;
		public Long IdPersona;
		public Long IdUsuarioCreador;
	}

	public static class DireccionElectronicaInsertRow {
		public Long IdDireccionElectronica;
		public String Direccion;
		public String CodigoProposito;
		public Long IdPersona;
		public Long IdUsuarioCreador;
	}

	public static class PersonaUpdateRow {
		public Long IdPersona;
		public Long IdDocumentoIdentidad;
		public Long IdDocumentoIdentidadAdicional;
		public Long IdDireccionElectronicaPrincipal;
		public Long IdDireccionFisicaPrincipal;
		public Long IdTelefonoPrincipal;
	}

	public static class EmpleadoInsertRow {
		public Long IdEmpleado;
		public Long IdPersona;
		public Long IdEmpresa;
		public String CodigoEmpleado;
		public String CodigoTipoCargoEmpleado;
		public Long IdEmpleadoSupervisor;
		public Long IdSucursal;
		public Date FechaInicioCargo;
		public Date FechaTerminoCargo;
		public String EsEnrolado;
		public Long IdUsuarioCreador;
		public String CargoIndefinido;
		public Long IdDireccionElectronica;
	}

	public static class SocioDeNegocioUpdateRow {
		public Long IdSocioDeNegocio;
		public Long IdEmpleado;
	}

	public static class EnrolamientoUpdateRow {
		public Long IdEnrolamiento;
		public Long IdEntidadEnrolada;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		Entrada in = gson.fromJson(jin.toString(), Entrada.class);
		Salida out = logica(conn, in);
		JsonParser parser = new JsonParser();
		return parser.parse(gson.toJson(out)).getAsJsonObject();
	}

	public Salida logica(Connection con, Entrada in) throws Exception {
		logger.info("INIT CrearEmpleado");
		Salida salida = new Salida();
		SQL sql = new SQL();

		// ------------------------------------------------------------------
		// Validaciones
		// ------------------------------------------------------------------
		if (in.p_ConsultarEnrolamiento == null || in.p_ConsultarEnrolamiento.size() != 1) {
			agregarError(salida, con, in.p_IdEmpresa, "EMPLEADO_NO_PUEDE_SER_ENROLADO");
			return salida;
		}

		ConsultarEnrolamiento enr = in.p_ConsultarEnrolamiento.get(0);

		if (!"EMPLEADO".equals(enr.IdTipoEntidad)) {
			agregarError(salida, con, in.p_IdEmpresa, "EMPLEADO_NO_PUEDE_SER_ENROLADO");
		}

		SalidaValidarDocumentoIdentidad validacionDocumento = new ValidarDocumentoIdentidad().logica(con, in.p_IdEmpresa,
				"EMPLEADO", enr.IdTipoDocumentoIdentidad, enr.DocumentoIdentidad, enr.Tipo);
		if ("N".equals(validacionDocumento.Valido)) {
			agregarError(salida, con, in.p_IdEmpresa, "EMPLEADO_EXISTE");
		}

		HashMap<String, Object> paramsCodigo = new HashMap<>();
		paramsCodigo.put("CodigoEmpleado", enr.EMP_CodigoEmpleado);
		paramsCodigo.put("IdEmpresa", in.p_IdEmpresa);
		long countCodigoEmpleadoExiste = contador(sql.query(con, SQL_COUNT_CODIGO_EMPLEADO_EXISTE, paramsCodigo, Contador.class));
		if (countCodigoEmpleadoExiste > 0) {
			agregarError(salida, con, in.p_IdEmpresa, "CODIGO_EMPLEADO_EXISTE");
		}

		HashMap<String, Object> paramsMaximo = new HashMap<>();
		paramsMaximo.put("IdEmpresa", in.p_IdEmpresa);
		List<ValorConfiguracion> maximos = sql.query(con, SQL_SELECT_MAXIMO_CODIGO_EMPLEADO, paramsMaximo, ValorConfiguracion.class);
		int maximoCodigoEmpleado = 40;
		if (!maximos.isEmpty() && maximos.get(0).Valor != null && !maximos.get(0).Valor.isEmpty()) {
			maximoCodigoEmpleado = Integer.parseInt(maximos.get(0).Valor.trim());
		}
		if (enr.EMP_CodigoEmpleado != null && enr.EMP_CodigoEmpleado.length() > maximoCodigoEmpleado) {
			HashMap<String, String> valores = new HashMap<>();
			valores.put("Numero", String.valueOf(maximoCodigoEmpleado));
			String descripcion = Diccionario.getMensaje(con, in.p_IdEmpresa, "EMPLEADO_CODIGO_MAXIMO_LONGITUD_EXCEDE", valores);
			salida.P_OUT_ERRORESDETALLE.add(0, new ErrorDetalle("EMPLEADO_CODIGO_MAXIMO_LONGITUD_EXCEDE", descripcion));
			salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESDETALLE.size();
		}

		if (salida.P_OUT_ERRORESCONTADOR > 0) {
			return salida;
		}

		String fechaHoraSistema = FechaDB.ObtenerFechaHoraSistemaUTC(con, in.p_IdEmpresa);
		String fechaSoloFecha = fechaHoraSistema.substring(0, 10);

		// ------------------------------------------------------------------
		// Enrolamiento (se crea si no existe un borrador previo)
		// ------------------------------------------------------------------
		Long idEnrolamiento = obtenerOCrearEnrolamiento(con, sql, in, enr);

		// ------------------------------------------------------------------
		// Autorizacion requerida
		// ------------------------------------------------------------------
		String requiereAutorizacion = Utils.getRequiereEventoAutorizacion(con, in.p_IdEmpresa, "ENROLE");
		String esEnrolado = "S".equals(requiereAutorizacion) ? "P" : "S";

		// ------------------------------------------------------------------
		// Persona (se crea solo si no existe)
		// ------------------------------------------------------------------
		Long idSocioNegocio;
		Long idPersona;
		Long idDireccionElectronicaPrincipal;

		HashMap<String, Object> paramsExistePersona = new HashMap<>();
		paramsExistePersona.put("DocumentoIdentidad", enr.DocumentoIdentidad);
		paramsExistePersona.put("IdTipoDocumentoIdentidad", enr.IdTipoDocumentoIdentidad);
		long countExistePersona = contador(sql.query(con, SQL_COUNT_PERSONA_EXISTE, paramsExistePersona, Contador.class));

		if (countExistePersona == 0) {
			idSocioNegocio = nextVal(con, "SocioDeNegocio");
			idPersona = nextVal(con, "Persona");
			Long idDocIdentidad = nextVal(con, "DocumentoIdentidad");
			Long idDireccionFisicaPrincipal = nextVal(con, "DireccionFisica");
			idDireccionElectronicaPrincipal = nextVal(con, "DireccionElectronica");
			Long idTelefonoPrincipal = nextVal(con, "Telefono");

			HashMap<String, Object> paramsCiudad = new HashMap<>();
			paramsCiudad.put("IdCiudad", enr.IdCiudad);
			List<IdPaisRow> paisesCiudad = sql.query(con, SQL_SELECT_ID_PAIS_CIUDAD, paramsCiudad, IdPaisRow.class);
			Long idPaisCiudad = paisesCiudad.isEmpty() ? null : paisesCiudad.get(0).IdPais;

			HashMap<String, Object> paramsAdicional = new HashMap<>();
			paramsAdicional.put("DocumentoIdentidadAdicional", enr.DocumentoIdentidadAdicional);
			paramsAdicional.put("IdTipoDocumentoIdentidadAdicional", enr.IdTipoDocumentoIdentidadAdicional);
			paramsAdicional.put("IdEmpresa", in.p_IdEmpresa);
			long countExisteAdicional = (enr.DocumentoIdentidadAdicional == null || enr.DocumentoIdentidadAdicional.isEmpty())
					? 0
					: contador(sql.query(con, SQL_COUNT_PERSONA_DOCIDENTIDAD_ADICIONAL_EXISTE, paramsAdicional, Contador.class));

			Long idDocIdentidadAdicional = null;
			boolean tieneDocumentoAdicional = idPaisCiudad != null && idPaisCiudad != 44L
					&& enr.DocumentoIdentidadAdicional != null && !enr.DocumentoIdentidadAdicional.isEmpty()
					&& countExisteAdicional == 0;
			if (tieneDocumentoAdicional) {
				idDocIdentidadAdicional = nextVal(con, "DocumentoIdentidad");
			}

			insertarPersona(con, sql, enr, idPersona, idPaisCiudad, esEnrolado, in.p_IdUsuario);

			SocioDeNegocioInsertRow socioNegocioRow = new SocioDeNegocioInsertRow();
			socioNegocioRow.IdSocioDeNegocio = idSocioNegocio;
			socioNegocioRow.IdPersona = idPersona;
			socioNegocioRow.IdEmpresa = in.p_IdEmpresa;
			sql.batch(con, SQL_INSERT_SOCIODENEGOCIO, Arrays.asList(socioNegocioRow), SocioDeNegocioInsertRow.class);

			FatcaInsertRow fatcaRow = new FatcaInsertRow();
			fatcaRow.IdPersona = idPersona;
			fatcaRow.IdClasificacionEmpresaFatca = "J".equals(enr.Tipo) ? "PENDIENTE" : "";
			fatcaRow.FechaIngreso = fechaHoraSistema;
			fatcaRow.IdUsuarioCreador = in.p_IdUsuario;
			sql.batch(con, SQL_INSERT_FATCA, Arrays.asList(fatcaRow), FatcaInsertRow.class);

			DocumentoIdentidadInsertRow docRow = new DocumentoIdentidadInsertRow();
			docRow.IdDocumentoIdentidad = idDocIdentidad;
			docRow.IdPersona = idPersona;
			docRow.IdTipoDocumentoIdentidad = enr.IdTipoDocumentoIdentidad;
			docRow.IdTipoDocumentoIdentidadAsociado = enr.IdTipoDocumentoIdentidadAsociado;
			docRow.DocumentoIdentidad = enr.DocumentoIdentidad;
			docRow.IdUsuarioCreador = in.p_IdUsuario;
			docRow.TipoPersona = enr.Tipo;
			sql.batch(con, SQL_INSERT_DOCUMENTO_IDENTIDAD, Arrays.asList(docRow), DocumentoIdentidadInsertRow.class);

			if (idDocIdentidadAdicional != null) {
				DocumentoIdentidadAdicionalInsertRow docAdicionalRow = new DocumentoIdentidadAdicionalInsertRow();
				docAdicionalRow.IdDocumentoIdentidad = idDocIdentidadAdicional;
				docAdicionalRow.IdPersona = idPersona;
				docAdicionalRow.IdTipoDocumentoIdentidad = enr.IdTipoDocumentoIdentidadAdicional;
				docAdicionalRow.DocumentoIdentidad = enr.DocumentoIdentidadAdicional;
				docAdicionalRow.IdUsuarioCreador = in.p_IdUsuario;
				docAdicionalRow.TipoPersona = enr.Tipo;
				sql.batch(con, SQL_INSERT_DOCUMENTO_IDENTIDAD_ADICIONAL, Arrays.asList(docAdicionalRow), DocumentoIdentidadAdicionalInsertRow.class);
			}

			DireccionFisicaInsertRow direccionFisicaRow = new DireccionFisicaInsertRow();
			direccionFisicaRow.IdDireccionFisica = idDireccionFisicaPrincipal;
			direccionFisicaRow.Calle = enr.DireccionCalle;
			direccionFisicaRow.Numero = enr.DireccionNumero;
			direccionFisicaRow.Departamento = enr.DireccionDepartamento;
			direccionFisicaRow.IdCiudad = enr.IdCiudad;
			direccionFisicaRow.IdDivisionPais = enr.IdDivisionPais;
			direccionFisicaRow.CodigoProposito = codigoProposito(enr.Tipo);
			direccionFisicaRow.IdPersona = idPersona;
			direccionFisicaRow.IdUsuarioCreador = in.p_IdUsuario;
			sql.batch(con, SQL_INSERT_DIRECCION_FISICA, Arrays.asList(direccionFisicaRow), DireccionFisicaInsertRow.class);

			TelefonoInsertRow telefonoRow = new TelefonoInsertRow();
			telefonoRow.IdTelefono = idTelefonoPrincipal;
			telefonoRow.IdPais = idPaisCiudad;
			telefonoRow.IdTipoTelefono = enr.IdTipoTelefono;
			telefonoRow.Numero = enr.Telefono;
			telefonoRow.CodigoProposito = codigoProposito(enr.Tipo);
			telefonoRow.IdPersona = idPersona;
			telefonoRow.IdUsuarioCreador = in.p_IdUsuario;
			sql.batch(con, SQL_INSERT_TELEFONO, Arrays.asList(telefonoRow), TelefonoInsertRow.class);

			DireccionElectronicaInsertRow direccionElectronicaRow = new DireccionElectronicaInsertRow();
			direccionElectronicaRow.IdDireccionElectronica = idDireccionElectronicaPrincipal;
			direccionElectronicaRow.Direccion = enr.Email;
			direccionElectronicaRow.CodigoProposito = codigoProposito(enr.Tipo);
			direccionElectronicaRow.IdPersona = idPersona;
			direccionElectronicaRow.IdUsuarioCreador = in.p_IdUsuario;
			sql.batch(con, SQL_INSERT_DIRECCION_ELECTRONICA, Arrays.asList(direccionElectronicaRow), DireccionElectronicaInsertRow.class);

			PersonaUpdateRow personaUpdateRow = new PersonaUpdateRow();
			personaUpdateRow.IdPersona = idPersona;
			personaUpdateRow.IdDocumentoIdentidad = idDocIdentidad;
			personaUpdateRow.IdDocumentoIdentidadAdicional = idDocIdentidadAdicional != null ? idDocIdentidadAdicional : 0L;
			personaUpdateRow.IdDireccionElectronicaPrincipal = idDireccionElectronicaPrincipal;
			personaUpdateRow.IdDireccionFisicaPrincipal = idDireccionFisicaPrincipal;
			personaUpdateRow.IdTelefonoPrincipal = idTelefonoPrincipal;
			sql.batch(con, SQL_UPDATE_PERSONA_PRINCIPALES, Arrays.asList(personaUpdateRow), PersonaUpdateRow.class);

		} else {
			HashMap<String, Object> paramsSocioExistente = new HashMap<>();
			paramsSocioExistente.put("DocumentoIdentidad", enr.DocumentoIdentidad);
			paramsSocioExistente.put("IdTipoDocumentoIdentidad", enr.IdTipoDocumentoIdentidad);
			paramsSocioExistente.put("IdEmpresa", in.p_IdEmpresa);
			List<SocioPersonaExistenteRow> existentes = sql.query(con, SQL_SELECT_SOCIONEGOCIO_PERSONA_EXISTENTE, paramsSocioExistente, SocioPersonaExistenteRow.class);
			SocioPersonaExistenteRow existente = existentes.isEmpty() ? new SocioPersonaExistenteRow() : existentes.get(0);
			idSocioNegocio = existente.IdSocioDeNegocio;
			idPersona = existente.IdPersona;
			idDireccionElectronicaPrincipal = existente.IdDireccionElectronicaPrincipal;

			HashMap<String, Object> paramsEmpleador = new HashMap<>();
			paramsEmpleador.put("IdPersona", idPersona);
			long countPersonaEmpleador = contador(sql.query(con, SQL_COUNT_PERSONA_EMPLEADOR, paramsEmpleador, Contador.class));
			if (countPersonaEmpleador > 0) {
				salida.P_OUT_ADVERTENCIASCONTADOR += 1;
				String descripcion = Diccionario.getMensaje(con, in.p_IdEmpresa, "TIENE_EMPLEADOR_RELACIONADO", new HashMap<>());
				salida.P_OUT_ADVERTENCIASDETALLE.add(0, new AdvertenciaDetalle("TIENE_EMPLEADOR_RELACIONADO", "", descripcion));
			}
		}

		// ------------------------------------------------------------------
		// Empleado
		// ------------------------------------------------------------------
		Long idEmpleado = nextVal(con, "Empleado");

		EmpleadoInsertRow empleadoRow = new EmpleadoInsertRow();
		empleadoRow.IdEmpleado = idEmpleado;
		empleadoRow.IdPersona = idPersona;
		empleadoRow.IdEmpresa = in.p_IdEmpresa;
		empleadoRow.CodigoEmpleado = enr.EMP_CodigoEmpleado;
		empleadoRow.CodigoTipoCargoEmpleado = enr.EMP_CodigoTipoCargoEmpleado;
		empleadoRow.IdEmpleadoSupervisor = enr.EMP_IdEmpleadoSupervisor;
		empleadoRow.IdSucursal = enr.EMP_IdSucursal;
		empleadoRow.FechaInicioCargo = enr.EMP_FechaInicioCargo;
		empleadoRow.FechaTerminoCargo = enr.EMP_FechaTerminoCargo;
		empleadoRow.EsEnrolado = esEnrolado;
		empleadoRow.IdUsuarioCreador = in.p_IdUsuario;
		empleadoRow.CargoIndefinido = enr.EMP_CargoIndefinido;
		empleadoRow.IdDireccionElectronica = idDireccionElectronicaPrincipal;
		sql.batch(con, SQL_INSERT_EMPLEADO, Arrays.asList(empleadoRow), EmpleadoInsertRow.class);

		SocioDeNegocioUpdateRow socioUpdateRow = new SocioDeNegocioUpdateRow();
		socioUpdateRow.IdSocioDeNegocio = idSocioNegocio;
		socioUpdateRow.IdEmpleado = idEmpleado;
		sql.batch(con, SQL_UPDATE_SOCIODENEGOCIO_IDEMPLEADO, Arrays.asList(socioUpdateRow), SocioDeNegocioUpdateRow.class);

		EnrolamientoUpdateRow enrolamientoUpdateRow = new EnrolamientoUpdateRow();
		enrolamientoUpdateRow.IdEnrolamiento = idEnrolamiento;
		enrolamientoUpdateRow.IdEntidadEnrolada = idEmpleado;
		sql.batch(con, SQL_UPDATE_ENROLAMIENTO_IDENTIDADENROLADA, Arrays.asList(enrolamientoUpdateRow), EnrolamientoUpdateRow.class);

		salida.P_OUT_REFID = idEmpleado;

		// ------------------------------------------------------------------
		// Evento de autorizacion
		// ------------------------------------------------------------------
		if ("S".equals(requiereAutorizacion)) {
			String glosa = "J".equals(enr.Tipo)
					? enr.DocumentoIdentidad + " / " + enr.RazonSocial
					: enr.DocumentoIdentidad + " / " + enr.Nombres + " " + enr.ApellidoPaterno + " " + enr.ApellidoMaterno;

			ServiceEntrada entradaEvento = new ServiceEntrada();
			entradaEvento.p_IdEmpresa = in.p_IdEmpresa;
			entradaEvento.p_IdUsuario = in.p_IdUsuario;
			entradaEvento.p_IdModulo = in.p_IdModulo;
			entradaEvento.p_UsaReglasNegocio = "N";
			entradaEvento.p_IdTipoEvento = "ENROLE";
			entradaEvento.p_IdModuloSolicita = in.p_IdModulo;
			entradaEvento.p_IdUsuarioSolicita = in.p_IdUsuario;
			entradaEvento.p_Fecha = fechaSoloFecha;
			entradaEvento.p_IdConcepto = "ENROLE";
			entradaEvento.p_NumeroOperacionConcepto = String.valueOf(idEmpleado);
			entradaEvento.p_FechaOperacionConcepto = fechaSoloFecha;
			entradaEvento.p_IdFuncionario = idEmpleado;
			entradaEvento.p_IdCliente = idEmpleado;
			entradaEvento.p_Glosa = glosa;

			ServiceSalida salidaEvento = new CrearEventoAutorizacion().logica(con, entradaEvento);

			for (int i = salidaEvento.p_out_AdvertenciasDetalle.size() - 1; i >= 0; i--) {
				salida.P_OUT_ADVERTENCIASDETALLE.add(0, new AdvertenciaDetalle(
						salidaEvento.p_out_AdvertenciasDetalle.get(i).Codigo,
						salidaEvento.p_out_AdvertenciasDetalle.get(i).RequiereAutorizacion,
						salidaEvento.p_out_AdvertenciasDetalle.get(i).Descripcion));
			}
			for (int i = salidaEvento.p_out_ErroresDetalle.size() - 1; i >= 0; i--) {
				salida.P_OUT_ERRORESDETALLE.add(0, new ErrorDetalle(
						salidaEvento.p_out_ErroresDetalle.get(i).Codigo,
						salidaEvento.p_out_ErroresDetalle.get(i).Descripcion));
			}
			salida.P_OUT_ADVERTENCIASCONTADOR = salida.P_OUT_ADVERTENCIASDETALLE.size();
			salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESDETALLE.size();

			if (salida.P_OUT_ERRORESCONTADOR > 0) {
				return salida;
			}

			String descripcionRequiere = Diccionario.getMensaje(con, in.p_IdEmpresa, "REQUIERE_AUTORIZACION", new HashMap<>());
			salida.P_OUT_ADVERTENCIASDETALLE.add(0, new AdvertenciaDetalle("REQUIERE_AUTORIZACION", "S", descripcionRequiere));
			salida.P_OUT_ADVERTENCIASCONTADOR = salida.P_OUT_ADVERTENCIASDETALLE.size();
		}

		logger.info("END CrearEmpleado");
		return salida;
	}

	private Long obtenerOCrearEnrolamiento(Connection con, SQL sql, Entrada in, ConsultarEnrolamiento enr) throws Exception {
		HashMap<String, Object> paramsCount = new HashMap<>();
		paramsCount.put("IdTipoEntidad", enr.IdTipoEntidad);
		paramsCount.put("IdTipoDocumentoIdentidad", enr.IdTipoDocumentoIdentidad);
		paramsCount.put("DocumentoIdentidad", enr.DocumentoIdentidad);
		paramsCount.put("Tipo", enr.Tipo);
		long countEnrolamiento = contador(sql.query(con, SQL_COUNT_ENROLAMIENTO, paramsCount, Contador.class));

		if (countEnrolamiento == 0) {
			Long idEnrolamiento = nextVal(con, "Enrolamiento");

			EnrolamientoInsertRow row = new EnrolamientoInsertRow();
			row.IdEnrolamiento = idEnrolamiento;
			row.IdEmpresa = in.p_IdEmpresa;
			row.IdUsuario = in.p_IdUsuario;
			row.IdTipoEntidad = enr.IdTipoEntidad;
			row.IdTipoDocumentoIdentidad = enr.IdTipoDocumentoIdentidad;
			row.IdTipoDocumentoIdentidadAsociado = enr.IdTipoDocumentoIdentidadAsociado;
			row.DocumentoIdentidad = enr.DocumentoIdentidad;
			row.IdTipoDocumentoIdentidadAdicional = enr.IdTipoDocumentoIdentidadAdicional;
			row.DocumentoIdentidadAdicional = enr.DocumentoIdentidadAdicional;
			row.Tipo = enr.Tipo;
			row.Nombres = enr.Nombres;
			row.ApellidoPaterno = enr.ApellidoPaterno;
			row.ApellidoMaterno = enr.ApellidoMaterno;
			row.RazonSocial = enr.RazonSocial;
			row.IdPaisNacimiento = enr.IdPaisNacimiento;
			row.FechaNacimiento = enr.FechaNacimiento;
			row.Sexo = enr.Sexo;
			row.EstadoCivil = enr.EstadoCivil;
			row.IdCiudad = enr.IdCiudad;
			row.IdDivisionPais = enr.IdDivisionPais;
			row.IdTipoTelefono = enr.IdTipoTelefono;
			row.Telefono = enr.Telefono;
			row.Email = enr.Email;
			row.DireccionCalle = enr.DireccionCalle;
			row.DireccionNumero = enr.DireccionNumero;
			row.DireccionDepartamento = enr.DireccionDepartamento;
			row.CLI_IdCategoriaInversionista = enr.CLI_IdCategoriaInversionista;
			row.POR_IdPerfilComercial = enr.POR_IdPerfilComercial;
			row.CLI_IdTipoCliente = enr.CLI_IdTipoCliente;
			row.CLI_IdClaseCliente = enr.CLI_IdClaseCliente;
			row.EMP_CargoIndefinido = enr.EMP_CargoIndefinido;
			sql.batch(con, SQL_INSERT_ENROLAMIENTO, Arrays.asList(row), EnrolamientoInsertRow.class);

			return idEnrolamiento;
		}

		HashMap<String, Object> paramsSelect = new HashMap<>();
		paramsSelect.put("IdTipoEntidad", enr.IdTipoEntidad);
		paramsSelect.put("IdTipoDocumentoIdentidad", enr.IdTipoDocumentoIdentidad);
		paramsSelect.put("DocumentoIdentidad", enr.DocumentoIdentidad);
		List<IdEnrolamientoRow> existentes = sql.query(con, SQL_SELECT_ENROLAMIENTO, paramsSelect, IdEnrolamientoRow.class);
		return existentes.isEmpty() ? null : existentes.get(0).IdEnrolamiento;
	}

	private void insertarPersona(Connection con, SQL sql, ConsultarEnrolamiento enr, Long idPersona, Long idPaisCiudad,
			String esEnrolado, Long idUsuarioCreador) throws Exception {
		PersonaInsertRow row = new PersonaInsertRow();
		row.IdPersona = idPersona;
		row.Tipo = enr.Tipo;
		row.Nombres = enr.Nombres;
		row.ApellidoPaterno = enr.ApellidoPaterno;
		row.ApellidoMaterno = enr.ApellidoMaterno;
		row.RazonSocial = enr.RazonSocial;
		row.IdPaisNacimiento = (enr.IdPaisNacimiento == null || enr.IdPaisNacimiento == 0L) ? null : enr.IdPaisNacimiento;
		row.IdPaisResidencia = (idPaisCiudad == null || idPaisCiudad == 0L) ? null : idPaisCiudad;
		row.FechaNacimiento = enr.FechaNacimiento;
		row.Sexo = enr.Sexo;
		row.EstadoCivil = (enr.EstadoCivil == null || enr.EstadoCivil.isEmpty()) ? null : enr.EstadoCivil;
		row.EsEnrolado = esEnrolado;
		row.IdUsuarioCreador = idUsuarioCreador;
		sql.batch(con, SQL_INSERT_PERSONA, Arrays.asList(row), PersonaInsertRow.class);
	}

	private String codigoProposito(String tipo) {
		return "J".equals(tipo) ? "COME" : "PART";
	}

	private long contador(List<Contador> resultado) {
		return (resultado != null && !resultado.isEmpty() && resultado.get(0).Contador != null) ? resultado.get(0).Contador : 0L;
	}

	private Long nextVal(Connection con, String nombreSecuencia) throws Exception {
		return Utils.getSecuencia(con, "BSCL.SocioNegocio.BD.Secuencias::" + nombreSecuencia, 1L).get(0);
	}

	private void agregarError(Salida salida, Connection con, Long idEmpresa, String codigo) throws Exception {
		String descripcion = Diccionario.getMensaje(con, idEmpresa, codigo, new HashMap<>());
		salida.P_OUT_ERRORESDETALLE.add(0, new ErrorDetalle(codigo, descripcion));
		salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESDETALLE.size();
	}

	public static void main(String[] args) throws Exception {
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			System.out.println("== BEGIN ==");
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_ConsultarEnrolamiento\":[{\"IdEnrolamiento\":0,\"IdEmpresa\":1,\"IdUsuario\":82908,\"IdTipoEntidad\":\"EMPLEADO\",\"IdEntidadEnrolada\":0,\"IdTipoDocumentoIdentidad\":\"R\",\"IdTipoDocumentoIdentidadAsociado\":\"R\",\"DocumentoIdentidad\":\"26199390-2\",\"IdTipoDocumentoIdentidadAdicional\":\"\",\"DocumentoIdentidadAdicional\":\"\",\"Tipo\":\"N\",\"Nombres\":\"TEST\",\"ApellidoPaterno\":\"TESTER\",\"ApellidoMaterno\":\"TESTEADOR\",\"RazonSocial\":\"\",\"IdPaisNacimiento\":44,\"FechaNacimiento\":\"1999-06-01\",\"Sexo\":\"M\",\"EstadoCivil\":\"SO\",\"IdCiudad\":3490,\"IdDivisionPais\":345,\"IdTipoTelefono\":\"OTRO\",\"Telefono\":\"+56982648122\",\"Email\":\"M@M.COM\",\"DireccionCalle\":\"JULIO\",\"DireccionNumero\":\"142\",\"DireccionDepartamento\":\"\",\"FechaCreacion\":\"2026-07-27\",\"FechaModificacion\":\"2026-07-27\",\"IdPersonaConyuge\":0,\"RegimenConyugal\":\"\",\"CLI_IdCategoriaInversionista\":\"\",\"CLI_IdTipoCliente\":\"\",\"CLI_IdClaseCliente\":\"\",\"CLI_IdTipoClienteCMF\":0,\"CLI_IdEmpleadoEjecutivo\":0,\"CLI_CodigoAdicionalHolding\":\"\",\"POR_IdPerfilComercial\":\"\",\"EMP_CodigoEmpleado\":\"TST2\",\"EMP_CodigoTipoCargoEmpleado\":\"ASEFIN\",\"EMP_IdEmpleadoSupervisor\":896,\"EMP_IdSucursal\":20,\"EMP_FechaInicioCargo\":\"2026-07-27\",\"EMP_FechaTerminoCargo\":\"\",\"CNT_CodigoInscripcion\":\"\",\"CNT_Emisor\":\"\",\"CNT_Depositos\":\"\",\"CNT_Bolsa\":\"\",\"CNT_Corredora\":\"\",\"CNT_CamaraDeCompensacion\":\"\",\"CNT_Banco\":\"\",\"CNT_Boveda\":\"\",\"CNT_Otros\":\"\",\"PRO_IdAreaProveedor\":0,\"EMP_CargoIndefinido\":\"S\"}],\"p_IdEmpresa\":1,\"p_IdUsuario\":82908,\"p_IdModulo\":\"SOC\"}";
			Entrada entrada = gson.fromJson(json, Entrada.class);

			CrearEmpleado demo = new CrearEmpleado();
			try {
				Salida salida = demo.logica(conn, entrada);
				System.out.println(salida.toString());
			} finally {
				conn.rollback();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}
}
