package cl.bcs.socionegocio.wbs;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import cl.bcs.plataforma.frm.ValidarCamposObligatorios.SalidaValidarCamposObligatorios;
import cl.bcs.plataforma.frm.ValidarContenidoCampos;
import cl.bcs.plataforma.frm.pojo.FormularioCanal;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.cli.CrearCliente;
import cl.bcs.socionegocio.cli.CrearCliente.EntradaServicioCrearCliente;
import cl.bcs.socionegocio.cli.CrearCliente.SalidaServicioCrearCliente;
import cl.bcs.socionegocio.per.CrearPersona;
import cl.bcs.socionegocio.per.ModificarPersonaDatosGeneralesMasivo;
import cl.bcs.socionegocio.per.CrearPersona.EntradaServicioCrearPersona;
import cl.bcs.socionegocio.per.CrearPersona.SalidaServicioCrearPersona;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.EntradaEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.SalidaEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.pojo.RegistroCambioMasivo;
import cl.bcs.socionegocio.pojo.Enrolamiento;
import cl.bcs.socionegocio.soc.SocioNegocioUtils;
import cl.bcs.socionegocio.wbs.WebServiceUtils.Id;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

import cl.bcs.socionegocio.per.CrearTelefonoMasivo;
import cl.bcs.socionegocio.per.EliminarPersonaDireccionElectronicaMasivo;
import cl.bcs.socionegocio.per.EliminarPersonaDireccionFisicaMasivo;
import cl.bcs.socionegocio.per.EliminarPersonaTelefonoMasivo;
import cl.bcs.socionegocio.per.CrearTelefonoMasivo.EntradaCrearTelefonoMasivo;
import cl.bcs.socionegocio.per.CrearTelefonoMasivo.SalidaCrearTelefonoMasivo;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.EntidadPersonaEliminar;
import cl.bcs.socionegocio.wbs.WebServiceUtils.SalidaWBS;
import cl.bcs.socionegocio.per.CrearDireccionElectronicaMasivo.DireccionElectronica;
import cl.bcs.socionegocio.per.CrearDireccionElectronicaMasivo.EntradaCrearDireccionElectronicaMasivo;
import cl.bcs.socionegocio.per.CrearDireccionElectronicaMasivo.SalidaCrearDireccionElectronicaMasivo;
import cl.bcs.socionegocio.per.CrearDireccionFisicaMasivo.DireccionFisica;
import cl.bcs.socionegocio.per.CrearDireccionFisicaMasivo.EntradaCrearDireccionFisicaMasivo;
import cl.bcs.socionegocio.per.CrearDireccionFisicaMasivo.SalidaCrearDireccionFisicaMasivo;
import cl.bcs.socionegocio.per.CrearDireccionElectronicaMasivo;
import cl.bcs.socionegocio.per.CrearDireccionFisicaMasivo;
import cl.bcs.socionegocio.wbs.pojo.Telefono;
import cl.bcs.socionegocio.wbs.CrearClienteExpress.Mensaje;
import cl.bcs.socionegocio.wbs.CrearClienteFullWS.EntidadCreada;
import cl.bcs.socionegocio.wbs.CrearClienteFullWS.EntradaCrearClienteFullWS;
import cl.bcs.socionegocio.wbs.WebServiceUtils.EntidadManipulada;

public class CrearClienteExpress implements JavaCall {
	static final Logger logger = LogManager.getLogger(CrearClienteExpress.class);

	private final String SQL_SELECT_IDPERSONA = loadResourceToString("cl/bcs/socionegocio/wbs/sql/CrearClienteFullWS_SelectPersonaIdPersona.sql");
	private final String SQL_SELECT_IDPERSONA_POR_DOCUMENTO_IDENTIDAD = loadResourceToString("cl/bcs/socionegocio/wbs/sql/CrearClienteExpress_SelectPersonaIdPersonaPorDocumentoIdentidad.sql");
	private final String SQL_SELECT_DATOS_PERSONA_POR_DOCUMENTO_IDENTIDAD = loadResourceToString("cl/bcs/socionegocio/wbs/sql/CrearClienteExpress_SelectDatosPersonaPorDocumentoIdentidad.sql");
	private final String SQL_SELECT_CONSULTAR_CLIENTE_PORTAFOLIO = loadResourceToString("cl/bcs/socionegocio/wbs/sql/CrearClienteExpress_SelectConsultaClientePortafolio.sql");
	private final String SQL_SELECT_CONYUGE_POR_ID_PERSONA = loadResourceToString("cl/bcs/socionegocio/wbs/sql/CrearClienteExpress_SelectConsultaPersonaConyugePorIdPersona.sql");
	private final String SQL_SELECT_TELEFONOS_ID_PERSONA = loadResourceToString("cl/bcs/socionegocio/per/sql/EliminarPersonaTelefonoMasivo_SelectTelefonosPorIdPersona.sql");
	private final String SQL_SELECT_DIRECCIONES_ELECTRONICAS_ID_PERSONA = loadResourceToString("cl/bcs/socionegocio/per/sql/EliminarPersonaDireccionElectronicaMasivo_SelectDireccionElectronicaPorIdPersona.sql");
	private final String SQL_SELECT_DIRECCIONES_FISICAS_ID_PERSONA = loadResourceToString("cl/bcs/socionegocio/per/sql/EliminarPersonaDireccionFisicaMasivo_SelectDireccionFisicaPorIdPersona.sql");
	private final String SQL_PREDETERMINAR_PERSONA_DIRECCION_ELECTRONICA = loadResourceToString("cl/bcs/socionegocio/wbs/sql/CrearClienteExpress_Call_PredeterminarDireccionElectronica.sql");
	private final String SQL_PREDETERMINAR_PERSONA_DIRECCION_FISICA = loadResourceToString("cl/bcs/socionegocio/wbs/sql/CrearClienteExpress_Call_PredeterminarDireccionFisica.sql");
	private final String SQL_PREDETERMINAR_PERSONA_TELEFONO = loadResourceToString("cl/bcs/socionegocio/wbs/sql/CrearClienteExpress_Call_PredeterminarTelefono.sql");
	private final String SQL_CONSULTAR_DIVISION_PAIS = loadResourceToString("cl/bcs/socionegocio/wbs/sql/CrearClienteExpress_ConsultarDivisionPais.sql");
	private final String SQL_UPDATE_ID_PERSONA_CONYUGE_DE_CONYUGE = loadResourceToString("cl/bcs/socionegocio/wbs/sql/CrearClienteExpress_UpdateIdPersonaConyugeDeConyuge.sql");
	private static final Pattern PATTERN = Pattern.compile("^[0-9]+-[0-9kK]{1}$");
	
	private static final String ID_MODULO = "SOC";
	private static final String TIPO_ENTIDAD_CLIENTE = "CLIENTE";
	private static final String ID_TIPO_CANAL = "WBS";
	private static final String USA_REGLAS_NEGOCIO = "S";

	public static class EntradaCrearClienteExpress extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_ClaveUsuario;
		public List<SocioNegocio> p_SocioNegocio;
	}

	public static class SocioNegocio extends BcsPojo {
		public Long IdEntidad;
		public String IdTipoPersona;
		public Long IdPersona;
		public String TipoDocumento;
		public String NumeroDocumento;
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
		public Long IdPaisResidenciaCasaMatriz;
		public Long IdDireccionFisica;
		public Long IdComuna;
		public String Calle;
		public String Numero;
		public String Complemento;
		public String TipoTelefono;
		public String Telefono;
		public Long IdTelefono;
		public String Email;
		public Long IdEmail;
		public String CategoriaInversionista;
		public String PerfilComercial;
		public String TipoCliente;
		public Long TipoClienteCMF;
		public String AreaCliente;
		public Long Ejecutivo;
		public Long IdCiudad;
		public Long IdPersonaConyuge;
		public String ConyugeTipoDocumentoIdentidad;
		public String ConyugeDocumentoIdentidad;
		public String ConyugeNombres;
		public String ConyugeApellidoPaterno;
		public String ConyugeApellidoMaterno;
		public transient String EnrolarConyuge;
		public String IdSituacionLaboral;
		public String IdTipoCanalIngresoCorredor;
		@ValidarContenidoCampos(idCampo = "CodigoRangoSeleccion", tipoDato = String.class, idCampoMensaje = "CodigoPropositoTransaccion", prefijo = "PROPOSITO")
		public String CodigoPropositoTransaccion;

		public SocioNegocio() {
			IdEntidad = 0L;
			IdTipoPersona = "N";
			IdPersona = 0L;
			TipoDocumento = "";
			NumeroDocumento = "";
			Nombres = "";
			ApellidoPaterno = "";
			ApellidoMaterno = "";
			IdGenero = "";
			RazonSocial = "";
			IdPaisNacimientoConstitucion = 0L;
			FechaNacimientoConstitucion = "0001-01-01";
			IdEstadoCivil = "";
			IdRegimenConyugal = "";
			IdPaisResidenciaCasaMatriz = 0L;
			IdDireccionFisica = 0L;
			IdComuna = 0L;
			Calle = "";
			Numero = "";
			Complemento = "";
			IdTelefono = 0L;
			TipoTelefono = "";
			Telefono = "";
			IdEmail = 0L;
			Email = "";
			CategoriaInversionista = "";
			PerfilComercial = "";
			TipoCliente = "";
			TipoClienteCMF = 0L;
			AreaCliente = "";
			Ejecutivo = 0L;
			IdCiudad = 0L;
			IdPersonaConyuge = 0L;
			ConyugeTipoDocumentoIdentidad = "";
			ConyugeDocumentoIdentidad = "";
			ConyugeNombres = "";
			ConyugeApellidoPaterno = "";
			ConyugeApellidoMaterno = "";
			EnrolarConyuge = "N";
			IdSituacionLaboral = "";
			CodigoPropositoTransaccion = "";
		}

	}

	public static class RelacionadoConyuge extends BcsPojo {
		public Long IdPersona;
		public String Nombres;
		public String ApellidoPaterno;
		public String ApellidoMaterno;

		public RelacionadoConyuge() {
			this.IdPersona = 0L;
			this.Nombres = "";
			this.ApellidoPaterno = "";
			this.ApellidoMaterno = "";
		}
	}
	
	public static class DivisionPais extends BcsPojo {
	    public Long IdComuna;
	    public Long IdCiudad;

	    public DivisionPais() {
	        this.IdComuna = 0L;
	        this.IdCiudad = 0L;
	    }
	}

	public static class SalidaCrearClienteExpress extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public Long P_OUT_ADVERTENCIASCONTADOR;
		public List<EntidadCreada> P_OUT_ENTIDADCREADA;

		public SalidaCrearClienteExpress() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_ADVERTENCIASCONTADOR = 0L;
			this.P_OUT_ENTIDADCREADA = new ArrayList<>();
		}
	}

	public static class EntidadCreada extends BcsPojo {
		public String TipoEntidad;
		public Long IdEntidad;
		public List<Mensaje> Mensajes;

		public EntidadCreada() {
			TipoEntidad = "";
			IdEntidad = 0L;
			Mensajes = new ArrayList<>();
		}
	}

	public static class ConsultaClientePortafolio extends BcsPojo {

		public Long IdPortafolio;
		public Long IdPersona;

	}

	public static class Mensaje extends BcsPojo implements Comparable<Mensaje> {
		public Long IdInterno;
		public String TipoMensaje;
		public String Codigo;
		public String Mensaje;

		@Override
		public int compareTo(Mensaje o) {
			return IdInterno.compareTo(o.IdInterno);
		}
	}

	public static class MensajeAdvertencia extends BcsPojo {
		public String Codigo;
		public String RequiereAutorizacion;
		public String Descripcion;
	}

	public static class ErroresDetalle extends BcsPojo {
		public String Codigo;
		public String Descripcion;
	}
	
	private boolean validarDocumentoIdentidad(String TipoDocumentoIdentidad, String DocumentoIdentidad) {
		if("R".equals(TipoDocumentoIdentidad)) {
			Matcher matcher = PATTERN.matcher(DocumentoIdentidad);
			if ( matcher.matches() == false ) return false;
			String[] stringRut = DocumentoIdentidad.split("-");
			return stringRut[1].toLowerCase().equals(this.obtenerDV(stringRut[0]));
		}
		return false;
	}
	
	private String obtenerDV(String rut) {
		Integer M=0,S=1,T=Integer.parseInt(rut);
		for (;T!=0;T=(int) Math.floor(T/=10))
			S=(S+T%10*(9-M++%6))%11;
		return ( S > 0 ) ? String.valueOf(S-1) : "k";
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaCrearClienteExpress in = gson.fromJson(jin.toString(), EntradaCrearClienteExpress.class);
		SalidaCrearClienteExpress out = logica(conn, in);

		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	private SalidaCrearClienteExpress logica(Connection conn, EntradaCrearClienteExpress in) throws Exception {
		logger.info("INICIO::CrearClienteExpress");
		SalidaCrearClienteExpress salida = new SalidaCrearClienteExpress();

		String EditarTelefonoYDirecciones = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "SOCIODENEGOCIO", "PER.EditarTelefonoYDirecciones");
		Boolean ExistenEdiciones = false, ExistenEdicionesTelefonos = false, ExistenEdicionesDireccionesFisicas = false,
				ExistenEdicionesCorreos = false;
		
		String agregaAdvertencia = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "SOCIODENEGOCIO", "SOC.MensajeAdvertenciaRelacionadoExiste");
		List<Mensaje> mensajesAdvertencia = new ArrayList<>();

		if (in.p_SocioNegocio.isEmpty()) {
			salida = agregarError("NO_HAY_DATOS", "No hay datos para enrolar cliente", salida);
			return salida;
		}
		
		SalidaValidarCamposObligatorios validacion = validarEntrada(conn, in);

		if (validacion.P_OUT_ERRORESCONTADOR > 0) {
			salida.P_OUT_ERRORESCONTADOR = validacion.P_OUT_ERRORESCONTADOR;
			EntidadCreada entidadCreada = new EntidadCreada();
			entidadCreada.IdEntidad = 0L;
			entidadCreada.TipoEntidad = "CLIENTE";
			validacion.P_OUT_MENSAJES.forEach(mensaje -> {
				Mensaje msg = new Mensaje();
				msg.IdInterno = 0L;
				msg.TipoMensaje = PlataformaUtils.MensajeSalida.MENSAJE_ERROR;
				msg.Codigo = mensaje.codigo;
				msg.Mensaje = mensaje.descripcion;
				entidadCreada.Mensajes.add(msg);
			});
			salida.P_OUT_ENTIDADCREADA.add(entidadCreada);
			return salida;
		}
		
		//Validacion Maximo Codigo Portafolio
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
					salida = agregarError(codigoError, Diccionario.getMensaje(conn,in.p_IdEmpresa, codigoError).msg(), salida);
					return salida;
				}
			}
		}

		List<DireccionFisica> direccionesFisicas = new ArrayList<DireccionFisica>();
		List<CrearTelefonoMasivo.Telefono> telefonos = new ArrayList<CrearTelefonoMasivo.Telefono>();
		List<DireccionElectronica> direccionesElectronicas = new ArrayList<DireccionElectronica>();
		
		in.p_SocioNegocio = calcularCiudadDivisionPais(conn, in.p_SocioNegocio);
		
		CrearCliente crearCliente = new CrearCliente();
		for (SocioNegocio socio : in.p_SocioNegocio) {
			EntidadCreada entidad = new EntidadCreada();
			entidad.IdEntidad = 0L;
			entidad.TipoEntidad = TIPO_ENTIDAD_CLIENTE;

			SocioNegocio socioDesdeBD = obtenerSocioDesdeBD(conn, socio.NumeroDocumento);
			Long idPersonaConyuge = 0L;
			if (socioDesdeBD != null) {
				socioDesdeBD.Nombres = ("").equals(socioDesdeBD.Nombres) && ("N").equals(socioDesdeBD.IdTipoPersona) ? socio.Nombres : socioDesdeBD.Nombres;
				socioDesdeBD.ApellidoPaterno = ("").equals(socioDesdeBD.ApellidoPaterno) && ("N").equals(socioDesdeBD.IdTipoPersona) ? socio.ApellidoPaterno : socioDesdeBD.ApellidoPaterno;
				socioDesdeBD.ApellidoMaterno = ("").equals(socioDesdeBD.ApellidoMaterno) && ("N").equals(socioDesdeBD.IdTipoPersona) ? socio.ApellidoMaterno : socioDesdeBD.ApellidoMaterno;
				socioDesdeBD.RazonSocial = ("").equals(socioDesdeBD.RazonSocial) && ("J").equals(socioDesdeBD.IdTipoPersona) ? socio.RazonSocial : socioDesdeBD.RazonSocial;
				socioDesdeBD.IdGenero = ("").equals(socioDesdeBD.IdGenero) && ("N").equals(socioDesdeBD.IdTipoPersona) ? socio.IdGenero : socioDesdeBD.IdGenero;
				socioDesdeBD.IdPaisNacimientoConstitucion = socioDesdeBD.IdPaisNacimientoConstitucion.equals(0L) ? socio.IdPaisNacimientoConstitucion : socioDesdeBD.IdPaisNacimientoConstitucion;
				socioDesdeBD.IdPaisResidenciaCasaMatriz = socioDesdeBD.IdPaisResidenciaCasaMatriz.equals(0L) ? socio.IdPaisResidenciaCasaMatriz : socioDesdeBD.IdPaisResidenciaCasaMatriz;
				socioDesdeBD.FechaNacimientoConstitucion = ("0001-01-01").equals(socioDesdeBD.FechaNacimientoConstitucion) ? socio.FechaNacimientoConstitucion : socioDesdeBD.FechaNacimientoConstitucion;
				socioDesdeBD.CategoriaInversionista = socio.CategoriaInversionista != null ? socio.CategoriaInversionista : "";
				socioDesdeBD.PerfilComercial = socio.PerfilComercial != null ? socio.PerfilComercial : "";
				socioDesdeBD.TipoCliente = socio.TipoCliente != null ? socio.TipoCliente : "";
				socioDesdeBD.TipoClienteCMF = socio.TipoClienteCMF != null ? socio.TipoClienteCMF : 0L;
				socioDesdeBD.AreaCliente = socio.AreaCliente != null ? socio.AreaCliente : "";
				socioDesdeBD.Ejecutivo = socio.Ejecutivo != null ? socio.Ejecutivo : 0L;
				socioDesdeBD.IdTipoCanalIngresoCorredor = socio.IdTipoCanalIngresoCorredor != null || socio.IdTipoCanalIngresoCorredor != "" ? socio.IdTipoCanalIngresoCorredor : "";
				socioDesdeBD.IdEstadoCivil = !("").equals(socio.IdEstadoCivil) && ("N").equals(socioDesdeBD.IdTipoPersona) ? socio.IdEstadoCivil : socioDesdeBD.IdEstadoCivil;
				socioDesdeBD.IdRegimenConyugal = ("CA").equals(socioDesdeBD.IdEstadoCivil) && ("N").equals(socioDesdeBD.IdTipoPersona) ? socio.IdRegimenConyugal : "";
				socioDesdeBD.IdSituacionLaboral = ("N").equals(socioDesdeBD.IdTipoPersona) && !("").equals(socio.IdSituacionLaboral) ? socio.IdSituacionLaboral : socioDesdeBD.IdSituacionLaboral;
				if(!("").equals(socio.ConyugeDocumentoIdentidad) ){
					idPersonaConyuge = buscarPersonaConyuge(conn, socio);
					
					socioDesdeBD.ConyugeApellidoMaterno = ("N").equals(socioDesdeBD.IdTipoPersona) && !("").equals(socio.ConyugeApellidoMaterno) ? socio.ConyugeApellidoMaterno : socioDesdeBD.ConyugeApellidoMaterno ;
					socioDesdeBD.ConyugeApellidoPaterno = ("N").equals(socioDesdeBD.IdTipoPersona) && !("").equals(socio.ConyugeApellidoPaterno) ? socio.ConyugeApellidoPaterno : socioDesdeBD.ConyugeApellidoPaterno ;
					socioDesdeBD.ConyugeNombres = ("N").equals(socioDesdeBD.IdTipoPersona) && !("").equals(socio.ConyugeNombres) ? socio.ConyugeNombres : socioDesdeBD.ConyugeNombres ;
					socioDesdeBD.ConyugeDocumentoIdentidad = ("N").equals(socioDesdeBD.IdTipoPersona) && !("").equals(socio.ConyugeDocumentoIdentidad) ? socio.ConyugeDocumentoIdentidad : socioDesdeBD.ConyugeDocumentoIdentidad ;
					socioDesdeBD.ConyugeTipoDocumentoIdentidad = ("N").equals(socioDesdeBD.IdTipoPersona) && !("").equals(socio.ConyugeTipoDocumentoIdentidad) ? socio.ConyugeTipoDocumentoIdentidad : socioDesdeBD.ConyugeTipoDocumentoIdentidad ;
				}
				
				if (("S").equals(EditarTelefonoYDirecciones)) {

					if (!socio.IdCiudad.equals(0L) || !socio.Calle.isEmpty() || !socio.Numero.isEmpty() || !socio.Complemento.isEmpty()) {
						ExistenEdiciones = true;
						ExistenEdicionesDireccionesFisicas = true;
						
						socioDesdeBD.IdCiudad = !socio.IdCiudad.equals(0L) && socio.IdCiudad > 0L && !socio.IdCiudad.equals(socioDesdeBD.IdCiudad) ? socio.IdCiudad : socioDesdeBD.IdCiudad;
						socioDesdeBD.Calle = !socio.Calle.isEmpty() && !socio.Calle.equals(socioDesdeBD.Calle) ? socio.Calle : socioDesdeBD.Calle;
						socioDesdeBD.Numero = !socio.Numero.isEmpty() && !socio.Numero.equals(socioDesdeBD.Numero) ? socio.Numero : socioDesdeBD.Numero;
						socioDesdeBD.Complemento = !socio.Complemento.isEmpty() && !socio.Complemento.equals(socioDesdeBD.Complemento) ? socio.Complemento : socioDesdeBD.Complemento;					
						socioDesdeBD.IdComuna = !socio.IdComuna.equals(0L) && socio.IdComuna > 0L && !socio.IdComuna.equals(socioDesdeBD.IdComuna) ? socio.IdComuna : socioDesdeBD.IdComuna;

					} else {
						socioDesdeBD.IdCiudad = !socioDesdeBD.IdCiudad.equals(0L) ? socio.IdCiudad : socioDesdeBD.IdCiudad;
						socioDesdeBD.Calle = !socioDesdeBD.Calle.isEmpty() ? socio.Calle : socioDesdeBD.Calle;
						socioDesdeBD.Numero = !socioDesdeBD.Numero.isEmpty() ? socio.Numero : socioDesdeBD.Numero;
						socioDesdeBD.Complemento = !socioDesdeBD.Complemento.isEmpty() ? socio.Complemento : socioDesdeBD.Complemento;
						socioDesdeBD.IdComuna = !socioDesdeBD.IdComuna.equals(0L) ? socio.IdComuna : socioDesdeBD.IdComuna;
					}

					if (!socio.TipoTelefono.isEmpty() || !socio.Telefono.isEmpty()) {
						ExistenEdiciones = true;
						ExistenEdicionesTelefonos = true;

						socioDesdeBD.TipoTelefono = !socio.TipoTelefono.isEmpty() && !socio.TipoTelefono.equals(socioDesdeBD.TipoTelefono) ? socio.TipoTelefono : socioDesdeBD.TipoTelefono;
						socioDesdeBD.Telefono = !socio.Telefono.isEmpty() && !socio.Telefono.equals(socioDesdeBD.Telefono) ? socio.Telefono : socioDesdeBD.Telefono;
					} else {
						socioDesdeBD.TipoTelefono = !socioDesdeBD.TipoTelefono.isEmpty() ? socio.TipoTelefono : socioDesdeBD.TipoTelefono;
						socioDesdeBD.Telefono = !socioDesdeBD.Telefono.isEmpty() ? socio.Telefono : socioDesdeBD.Telefono;
					}

					if (!socio.Email.isEmpty()) {
						ExistenEdiciones = true;
						ExistenEdicionesCorreos = true;

						socioDesdeBD.Email = !socio.Email.isEmpty() && !socio.Email.equals(socioDesdeBD.Email) ? socio.Email : socioDesdeBD.Email;
					} else {
						socioDesdeBD.Email = !socioDesdeBD.Email.isEmpty() ? socio.Email : socioDesdeBD.Email;
					}
				} else {
					socioDesdeBD.IdCiudad = socioDesdeBD.IdCiudad.equals(0L) ? socio.IdCiudad : socioDesdeBD.IdCiudad;
					socioDesdeBD.Calle = socioDesdeBD.Calle.isEmpty() ? socio.Calle : socioDesdeBD.Calle;
					socioDesdeBD.Numero = socioDesdeBD.Numero.isEmpty() ? socio.Numero : socioDesdeBD.Numero;
					socioDesdeBD.Complemento = socioDesdeBD.Complemento.isEmpty() ? socio.Complemento : socioDesdeBD.Complemento;
					socioDesdeBD.IdComuna = socioDesdeBD.IdComuna.equals(0L) ? socio.IdComuna : socioDesdeBD.IdComuna;
					
					if( ( !socioDesdeBD.IdComuna.equals(socio.IdComuna) && !socio.IdComuna.equals(0L) ) || ( !socioDesdeBD.IdCiudad.equals(socio.IdCiudad) && !socio.IdCiudad.equals(0L) )
							|| ( !socioDesdeBD.Calle.equals(socio.Calle) && !socio.Calle.isEmpty()) || ( !socioDesdeBD.Complemento.equals(socio.Complemento) && !socio.Complemento.isEmpty())) {
						//agregar direccion fisica nueva
						DireccionFisica dirFis = new DireccionFisica();
						dirFis.Calle = socio.Calle;
						dirFis.Numero = socio.Numero;
						dirFis.Departamento = socio.Complemento;
						dirFis.Sector = "";
						dirFis.CodigoPostal = "";
						dirFis.IdCiudad = socio.IdCiudad;
						dirFis.IdDivisionPais = socio.IdComuna;
						dirFis.CodigoProposito = "";
						dirFis.Comentario = "";
						dirFis.IdEstado = "ACT";
						dirFis.IdPersona = socioDesdeBD.IdPersona;
						dirFis.Predeterminado = "S";
						dirFis.IdUsuarioCreador = in.p_IdUsuario;
						direccionesFisicas.add(dirFis);
					}
					socioDesdeBD.TipoTelefono = socioDesdeBD.TipoTelefono.isEmpty() ? socio.TipoTelefono : socioDesdeBD.TipoTelefono;
					socioDesdeBD.Telefono = socioDesdeBD.Telefono.isEmpty() ? socio.Telefono : socioDesdeBD.Telefono;
					if( ( !socioDesdeBD.TipoTelefono.equals(socio.TipoTelefono) && !socio.TipoTelefono.isEmpty()) || ( !socioDesdeBD.Telefono.equals(socio.Telefono) && !socio.Telefono.isEmpty())) {
						//agregar telefono nuevo
						CrearTelefonoMasivo.Telefono telefono = new CrearTelefonoMasivo.Telefono();
						telefono.Numero = socio.Telefono;
						telefono.IdTipoTelefono = socio.TipoTelefono;
						telefono.CodigoProposito = "PART";
						telefono.IdEstado = "ACT";
						telefono.IdPais = socioDesdeBD.IdPaisNacimientoConstitucion;
						telefono.Predeterminado = "S";
						telefono.IdPersona = socioDesdeBD.IdPersona;
						telefono.Anexo = "";
						telefono.CodigoProposito = "";
						telefono.Comentario = "";
						telefono.IdUsuarioCreador = in.p_IdUsuario;
						telefonos.add(telefono);
					}
					socioDesdeBD.Email = socioDesdeBD.Email.isEmpty() ? socio.Email : socioDesdeBD.Email;
					if( ( !socioDesdeBD.Email.equals(socio.Email) && !socio.Email.isEmpty()) ) {
						//agregar email nuevo
						DireccionElectronica mail = new DireccionElectronica();
						mail.Direccion = socio.Email;
						mail.IdEstado = "ACT";
						mail.CodigoProposito = "";
						mail.Comentario = "";
						mail.IdPersona = socioDesdeBD.IdPersona;
						mail.Predeterminado = "S";
						mail.IdUsuarioCreador = in.p_IdUsuario;
						direccionesElectronicas.add(mail);
					}
				}
				socioDesdeBD.CodigoPropositoTransaccion = socioDesdeBD.CodigoPropositoTransaccion == null ? socio.CodigoPropositoTransaccion : "";
				socio = socioDesdeBD;
			}
			
			if(!validarDocumentoIdentidad(socio.TipoDocumento,socio.NumeroDocumento)) {
				entidad = agregarError("RUT_INVALIDO", "RUT INVALIDO", entidad);
				logger.debug("Ingresa rut invalido");
				salida.P_OUT_ENTIDADCREADA.add(entidad);
				continue;
			}
			
			if (socio.IdPersonaConyuge != 0 && !idPersonaExiste(conn, socio.IdPersonaConyuge)) {
				entidad = agregarError("ID_PERSONA_CONYUGE_NO_EXISTE", "ID PERSONA CONYUGE NO EXISTE", entidad);
				salida.P_OUT_ENTIDADCREADA.add(entidad);
				continue;
			}
			if (socioNecesitaCrearPersonaConyuge(socio)) {
				socio.EnrolarConyuge = "S";
				
				String DocIdent = socio.NumeroDocumento;
				in.p_SocioNegocio.stream().filter( soc -> DocIdent.equals(soc.NumeroDocumento) ).findFirst().get().IdPersonaConyuge = idPersonaConyuge;
				SalidaValidarCamposObligatorios salidaValidarConyuge = validarFormularioConyuge(conn, in);
				if (salidaValidarConyuge.P_OUT_ERRORESCONTADOR > 0) {
					entidad = obtenerMensajes(salidaValidarConyuge, entidad);
					salida.P_OUT_ENTIDADCREADA.add(entidad);
					continue;
				}
				if (idPersonaConyuge == 0L) {
					CrearPersona crearPersona = new CrearPersona();
					EntradaServicioCrearPersona entradaServicioCrearPersona = obtenerEntradaCrearPersonaConyuge(conn, socio, in);
					SalidaServicioCrearPersona salidaServicioCrearPersona = crearPersona.logica(conn, entradaServicioCrearPersona);

					if (salidaServicioCrearPersona.P_OUT_ERRORESCONTADOR > 0) {
						entidad = obtenerMensajes(salidaServicioCrearPersona, entidad);
						salida.P_OUT_ENTIDADCREADA.add(entidad);
						continue;
					}
					socio.IdPersonaConyuge = salidaServicioCrearPersona.P_OUT_REFID;
				} else {
					socio.IdPersonaConyuge = idPersonaConyuge;
					
					if (agregaAdvertencia.equalsIgnoreCase("S")) {
						mensajesAdvertencia.addAll(agregarMensajesAdvertencias(conn, in, socio.IdPersonaConyuge));
					} else {
						SalidaEjecutarCambiosPersona salidaEjecutarCambiosPersona = modificarPersonaConyuge(conn, in, socio, idPersonaConyuge);
						if (salidaEjecutarCambiosPersona.P_OUT_ERRORESCONTADOR > 0) {
							entidad = obtenerMensajesModificacionConyuge(salidaEjecutarCambiosPersona, entidad);
							salida.P_OUT_ENTIDADCREADA.add(entidad);
							continue;
						}
					}
				}
			}

			EntradaServicioCrearCliente entradaCrearCliente = new EntradaServicioCrearCliente();

			entradaCrearCliente = obtenerEntradaCrearCliente(socio, in);

			SalidaServicioCrearCliente clienteCreado = crearCliente.logica(conn, entradaCrearCliente);

			entidad = obtenerClienteCreado(clienteCreado, entidad);

			if (entidad.Mensajes.isEmpty()) {
				Mensaje mensaje = new Mensaje();
				mensaje.IdInterno = 0L;
				mensaje.TipoMensaje = PlataformaUtils.MensajeSalida.MENSAJE_OK;
				mensaje.Codigo = "ENTIDAD_CREADA";
				mensaje.Mensaje = "ENTIDAD CREADA";
				entidad.Mensajes.add(mensaje);

				CrearAutorizadoOrden_Salida salidaCrearAutorizadoOrden = this.crearAutorizadoOrden(conn, in, socio);
				for (MensajeSalida mensajes : salidaCrearAutorizadoOrden.P_OUT_MENSAJES) {

					Mensaje mensajeCrearAutorizadoOrden = new Mensaje();
					mensajeCrearAutorizadoOrden.IdInterno = mensajes.id;
					mensajeCrearAutorizadoOrden.TipoMensaje = mensajes.tipo;
					mensajeCrearAutorizadoOrden.Codigo = mensajes.codigo;
					mensajeCrearAutorizadoOrden.Mensaje = mensajes.descripcion;
					entidad.Mensajes.add(mensajeCrearAutorizadoOrden);

				}
				
				if(socio.IdPersonaConyuge > 0L) {
					actualizarIdPersonaConyugeDeConyuge(conn, socio);
				}
			}

			entidad.Mensajes.addAll(mensajesAdvertencia);

			salida.P_OUT_ENTIDADCREADA.add(entidad);

			logger.info("INICIO::Modificar Direccion Fisica, Electronica y Telefono de Persona");

			if (("S").equals(EditarTelefonoYDirecciones)) {
				if (ExistenEdiciones) {
					List<EntidadCreada> entidadesEdiciones = new ArrayList<EntidadCreada>();
					
					if (ExistenEdicionesDireccionesFisicas) {
						SalidaWBS salidaEdicionDireccionesFisicas = EdicionDireccionesFisicas(conn, in, socioDesdeBD);
						
						if(salidaEdicionDireccionesFisicas.P_OUT_ERRORESCONTADOR > 0L) {
							entidadesEdiciones.addAll(obtenerEntidadesEdicion(salidaEdicionDireccionesFisicas.P_OUT_ENTIDADES_MANIPULADAS));
						}
					}

					if (ExistenEdicionesTelefonos) {
						SalidaWBS salidaEdicionTelefonos = EdicionTelefonos(conn,in, socio);
						
						if(salidaEdicionTelefonos.P_OUT_ERRORESCONTADOR > 0) {
							entidadesEdiciones.addAll(obtenerEntidadesEdicion(salidaEdicionTelefonos.P_OUT_ENTIDADES_MANIPULADAS));
						}
					}

					if (ExistenEdicionesCorreos) {
						SalidaWBS salidaEdicionDireccionesElectronicas = EdicionDireccionesElectronicas(conn,in, socio);
						
						if(salidaEdicionDireccionesElectronicas.P_OUT_ERRORESCONTADOR > 0) {
							entidadesEdiciones.addAll(obtenerEntidadesEdicion(salidaEdicionDireccionesElectronicas.P_OUT_ENTIDADES_MANIPULADAS));
						}
					}
					salida.P_OUT_ENTIDADCREADA.addAll(entidadesEdiciones);
				}
			} else {
				if(!direccionesFisicas.isEmpty()) {
					SalidaCrearDireccionFisicaMasivo salidaCrearDireccionFisicaMasivo = crearDireccionesFisicas(conn, direccionesFisicas, in.p_IdEmpresa, in.p_IdUsuario);
					if(salidaCrearDireccionFisicaMasivo.P_OUT_ERRORESCONTADOR > 0L){
						EntidadCreada entidadDirFis = new EntidadCreada();
						entidadDirFis.IdEntidad = salidaCrearDireccionFisicaMasivo.P_OUT_REFID.get(0).IdEntidad;
						entidadDirFis.TipoEntidad = "DIRECCION FISICA";
						
						if(!salidaCrearDireccionFisicaMasivo.P_OUT_MENSAJES.isEmpty()) {
							entidadDirFis.Mensajes.addAll(salidaCrearDireccionFisicaMasivo.P_OUT_MENSAJES.stream().map(mensaje -> {
								Mensaje msjSalida = new Mensaje();
								msjSalida.IdInterno = mensaje.id;
								msjSalida.Codigo = mensaje.codigo;
								msjSalida.Mensaje = mensaje.descripcion;
								msjSalida.TipoMensaje = mensaje.tipo;
								
								return msjSalida;
							}).collect(Collectors.toList()));
							salida.P_OUT_ENTIDADCREADA.add(entidadDirFis);
							salida.P_OUT_ERRORESCONTADOR += entidadDirFis.Mensajes.stream().filter(mensaje -> PlataformaUtils.MensajeSalida.MENSAJE_ERROR.equals(mensaje.TipoMensaje)).count();
						}	
					}
				}
				if(!direccionesElectronicas.isEmpty()) {
					SalidaCrearDireccionElectronicaMasivo salidaCrearDireccionElectronicaMasivo = crearDireccionesElectronicas(conn, direccionesElectronicas, in.p_IdEmpresa, in.p_IdUsuario);
					if(salidaCrearDireccionElectronicaMasivo.P_OUT_ERRORESCONTADOR > 0L){
						EntidadCreada entidadDirFis = new EntidadCreada();
						entidadDirFis.IdEntidad = salidaCrearDireccionElectronicaMasivo.P_OUT_REFID.get(0).IdEntidad;
						entidadDirFis.TipoEntidad = "DIRECCION FISICA";
						
						if(!salidaCrearDireccionElectronicaMasivo.P_OUT_MENSAJES.isEmpty()) {
							entidadDirFis.Mensajes.addAll(salidaCrearDireccionElectronicaMasivo.P_OUT_MENSAJES.stream().map(mensaje -> {
								Mensaje msjSalida = new Mensaje();
								msjSalida.IdInterno = mensaje.id;
								msjSalida.Codigo = mensaje.codigo;
								msjSalida.Mensaje = mensaje.descripcion;
								msjSalida.TipoMensaje = mensaje.tipo;
								
								return msjSalida;
							}).collect(Collectors.toList()));
							salida.P_OUT_ENTIDADCREADA.add(entidadDirFis);
							salida.P_OUT_ERRORESCONTADOR += entidadDirFis.Mensajes.stream().filter(mensaje -> PlataformaUtils.MensajeSalida.MENSAJE_ERROR.equals(mensaje.TipoMensaje)).count();
						}
					}
				}
				if(!telefonos.isEmpty()) {
					SalidaCrearTelefonoMasivo salidaCrearTelefonoMasivo = crearTelefonos(conn, telefonos, in.p_IdEmpresa, in.p_IdUsuario);
					if(salidaCrearTelefonoMasivo.P_OUT_ERRORESCONTADOR > 0L){
						EntidadCreada entidadDirFis = new EntidadCreada();
						entidadDirFis.IdEntidad = salidaCrearTelefonoMasivo.P_OUT_REFID.get(0).IdEntidad;
						entidadDirFis.TipoEntidad = "DIRECCION FISICA";
						
						if(!salidaCrearTelefonoMasivo.P_OUT_MENSAJES.isEmpty()) {
							entidadDirFis.Mensajes.addAll(salidaCrearTelefonoMasivo.P_OUT_MENSAJES.stream().map(mensaje -> {
								Mensaje msjSalida = new Mensaje();
								msjSalida.IdInterno = mensaje.id;
								msjSalida.Codigo = mensaje.codigo;
								msjSalida.Mensaje = mensaje.descripcion;
								msjSalida.TipoMensaje = mensaje.tipo;
								
								return msjSalida;
							}).collect(Collectors.toList()));
							salida.P_OUT_ENTIDADCREADA.add(entidadDirFis);
							salida.P_OUT_ERRORESCONTADOR += entidadDirFis.Mensajes.stream().filter(mensaje -> PlataformaUtils.MensajeSalida.MENSAJE_ERROR.equals(mensaje.TipoMensaje)).count();
						}
					}
				}
			}

			logger.info("FIN::Modificar Direccion Fisica, Electronica y Telefono de Persona");
		}

		salida.P_OUT_ADVERTENCIASCONTADOR = contarAdvertencias(salida);
		salida.P_OUT_ERRORESCONTADOR = contarErrores(salida);
		logger.info("FIN::CrearClienteExpress");
		return salida;
	}

	private SalidaValidarCamposObligatorios validarEntrada(Connection con, EntradaCrearClienteExpress entrada) throws Exception {
		ValidarCamposObligatorios validarCamposObligatorios = new ValidarCamposObligatorios();
		SalidaValidarCamposObligatorios resultado = validarCamposObligatorios.validarContenidoCampos(con, entrada.p_SocioNegocio, ID_TIPO_CANAL,  entrada.p_IdEmpresa);
		return resultado;
	}
	
	private List<EntidadCreada> obtenerEntidadesEdicion(List<EntidadManipulada> entidadesManipuladas) {
		logger.debug("INICIO::CrearClienteExpress.obtenerEntidadesEdicion");
		long inicio = System.currentTimeMillis();
		
		List<EntidadCreada> entidades = entidadesManipuladas.stream().map(ent -> {
			EntidadCreada entidad = new EntidadCreada();
			entidad.TipoEntidad = ent.TipoEntidad;
			entidad.IdEntidad = ent.IdEntidad;
			entidad.Mensajes = ent.Mensajes.stream().map(mensj-> {
				Mensaje mensaje = new Mensaje();
				mensaje.Codigo = mensj.codigo;
				mensaje.Mensaje = mensj.descripcion;
				mensaje.TipoMensaje = mensj.tipo;
				mensaje.IdInterno = mensj.id;
				return mensaje;
			}).collect(Collectors.toList());
			return entidad;
		}).collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearClienteExpress.EdicionTelefonos", fin - inicio);
		return entidades;
	}

	private SalidaCrearTelefonoMasivo crearTelefonos(Connection conn, List<CrearTelefonoMasivo.Telefono> telefonos, Long idEmpresa, Long idUsuario) throws Exception {
		logger.debug("INICIO::CrearClienteExpress.crearTelefonos");
		long inicio = System.currentTimeMillis();
		
		EntradaCrearTelefonoMasivo entradaCrearTelefonoMasivo = new EntradaCrearTelefonoMasivo();
		entradaCrearTelefonoMasivo.p_IdEmpresa = idEmpresa;
		entradaCrearTelefonoMasivo.p_IdModulo = ID_MODULO;
		entradaCrearTelefonoMasivo.p_IdUsuario = idUsuario;
		entradaCrearTelefonoMasivo.p_UsaReglasNegocio = USA_REGLAS_NEGOCIO;
		
		CrearTelefonoMasivo crearTelefonoMasivo = new CrearTelefonoMasivo();

		entradaCrearTelefonoMasivo.p_Telefonos.addAll(telefonos);

		SalidaCrearTelefonoMasivo salidaCrearTelefonoMasivo = crearTelefonoMasivo.logica(conn, entradaCrearTelefonoMasivo);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearClienteExpress.crearTelefonos", fin - inicio);
		return salidaCrearTelefonoMasivo;
	}
	
	private SalidaWBS EdicionTelefonos(Connection conn, EntradaCrearClienteExpress in, SocioNegocio socio) throws Exception {
		logger.debug("INICIO::CrearClienteExpress.EdicionTelefonos");
		long inicio = System.currentTimeMillis();
		
		SalidaWBS salidaTelefonos = new SalidaWBS();
		
		List<CrearTelefonoMasivo.Telefono> p_inTelefonos= new ArrayList<CrearTelefonoMasivo.Telefono>();
		CrearTelefonoMasivo.Telefono entradaTelefono = new CrearTelefonoMasivo.Telefono();
		entradaTelefono.IdPais = socio.IdPaisNacimientoConstitucion;
		entradaTelefono.IdTipoTelefono = socio.TipoTelefono;
		entradaTelefono.Numero = socio.Telefono;
		entradaTelefono.Anexo = "";
		entradaTelefono.CodigoProposito = "";
		entradaTelefono.Comentario = "";
		entradaTelefono.IdEstado = "ACT";
		entradaTelefono.IdPersona = socio.IdPersona;
		entradaTelefono.Predeterminado = "S";
		entradaTelefono.IdUsuarioCreador = in.p_IdUsuario;
		
		p_inTelefonos.add(entradaTelefono);

		SalidaCrearTelefonoMasivo salidaCrearTelefonoMasivo = crearTelefonos(conn, p_inTelefonos, in.p_IdEmpresa, in.p_IdUsuario);
		
		Long idTelefono = salidaCrearTelefonoMasivo.P_OUT_REFID.get(0).IdEntidad.longValue();
		
		EntidadManipulada entidadTelefono = new EntidadManipulada();
		entidadTelefono.IdEntidad = idTelefono;
		entidadTelefono.TipoEntidad = "TELEFONO";
		
		if(!salidaCrearTelefonoMasivo.P_OUT_MENSAJES.isEmpty()) {
			entidadTelefono.Mensajes.addAll(salidaCrearTelefonoMasivo.P_OUT_MENSAJES);
			salidaTelefonos.P_OUT_ENTIDADES_MANIPULADAS.add(entidadTelefono);
			salidaTelefonos.P_OUT_ERRORESCONTADOR += entidadTelefono.Mensajes.stream().filter(mensaje -> PlataformaUtils.MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo)).count();
			
			return salidaTelefonos;
		}
		
		if (entidadTelefono.Mensajes.isEmpty()) {
			entidadTelefono = WebServiceUtils.agregarMensajeOk(entidadTelefono, "CREADA");
			salidaTelefonos.P_OUT_ENTIDADES_MANIPULADAS.add(entidadTelefono);
		}
		
		salidaTelefonos.P_OUT_ERRORESCONTADOR += entidadTelefono.Mensajes.stream().filter(mensaje -> PlataformaUtils.MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo)).count();
		
		// Actualizacion de Direccion Fisica Principal

		HashMap<String, Object> salidaPredeterminarTelefono = predeterminarTelefono(conn, idTelefono, in);
		
		// Obtencion y eliminacion de las direcciones fisicas a borrar

		List<EntidadPersonaEliminar> registros = obtenerTelefonos(conn, socio.IdPersona);
		SalidaWBS salidaEliminarTelefono = new SalidaWBS();
		
		List<Telefono> p_Telefonos = new ArrayList<>();
		p_Telefonos = registros.stream().filter( tel -> !tel.IdEntidadPersonaEliminar.equals(idTelefono)).map(tel ->{
			Telefono telefono = new Telefono();
			telefono.IdTelefono = tel.IdEntidadPersonaEliminar;
			telefono.IdPersona = socio.IdPersona;
			return telefono;
		}).collect(Collectors.toList());
		
		if(!p_Telefonos.isEmpty()) {
			EntradaEjecutarCambiosPersona entradaEjecutarCambiosPersona = new EntradaEjecutarCambiosPersona();
			entradaEjecutarCambiosPersona.p_IdEmpresa = in.p_IdEmpresa;
			entradaEjecutarCambiosPersona.p_IdPersona = socio.IdPersona;
			entradaEjecutarCambiosPersona.p_IdModulo = ID_MODULO;
			entradaEjecutarCambiosPersona.p_IdUsuario = in.p_IdUsuario;
			entradaEjecutarCambiosPersona.p_UsaReglasNegocio = USA_REGLAS_NEGOCIO;
			entradaEjecutarCambiosPersona.p_IdTipoCanal = ID_TIPO_CANAL;
			entradaEjecutarCambiosPersona.p_EsCrearCliente = "S";
			entradaEjecutarCambiosPersona.p_RegistroCambio = obtenerRegistrosCambioEliminarTelefonos(p_Telefonos);
			
			EliminarPersonaTelefonoMasivo eliminarPersonaTelefonoMasivo = new EliminarPersonaTelefonoMasivo();
			SalidaEjecutarCambiosPersona salidaEjecutarCambiosPersona = eliminarPersonaTelefonoMasivo.logica(conn, entradaEjecutarCambiosPersona);
			
			if (!salidaEjecutarCambiosPersona.P_OUT_MENSAJES.isEmpty()) {
				entidadTelefono.Mensajes.addAll( salidaEjecutarCambiosPersona.P_OUT_MENSAJES);
				salidaEliminarTelefono.P_OUT_ENTIDADES_MANIPULADAS.add(entidadTelefono);
			}
		}
		
		salidaEliminarTelefono.P_OUT_ERRORESCONTADOR = salidaEliminarTelefono.P_OUT_ENTIDADES_MANIPULADAS
				.parallelStream().reduce(0L, (sum, ent) -> sum += ent.Mensajes.parallelStream().filter(mensaje -> PlataformaUtils.MensajeSalida.MENSAJE_ERROR .equals(mensaje.tipo)).count(), (sum1, sum2) -> sum1 + sum2);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearClienteExpress.EdicionTelefonos", fin - inicio);		
		return salidaTelefonos;
	}

	private SalidaCrearDireccionElectronicaMasivo crearDireccionesElectronicas(Connection conn, List<DireccionElectronica> direccionesElectronicas, Long idEmpresa, Long idUsuario) throws Exception {
		logger.debug("INICIO::CrearClienteExpress.crearDireccionesElectronicas");
		long inicio = System.currentTimeMillis();
		
		EntradaCrearDireccionElectronicaMasivo entradaCrearDireccionElectronicaMasivo = new EntradaCrearDireccionElectronicaMasivo();
		entradaCrearDireccionElectronicaMasivo.p_IdEmpresa = idEmpresa;
		entradaCrearDireccionElectronicaMasivo.p_IdModulo = ID_MODULO;
		entradaCrearDireccionElectronicaMasivo.p_IdUsuario = idUsuario;
		entradaCrearDireccionElectronicaMasivo.p_UsaReglasNegocio = USA_REGLAS_NEGOCIO;
		
		CrearDireccionElectronicaMasivo crearDireccionElectronicaMasivo = new CrearDireccionElectronicaMasivo();

		entradaCrearDireccionElectronicaMasivo.p_DireccionesElectronica.addAll(direccionesElectronicas);

		SalidaCrearDireccionElectronicaMasivo salidaCrearDireccionElectronicaMasivo = crearDireccionElectronicaMasivo.logica(conn, entradaCrearDireccionElectronicaMasivo);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearClienteExpress.crearDireccionesElectronicas", fin - inicio);
		return salidaCrearDireccionElectronicaMasivo;
	}
	
	private SalidaWBS EdicionDireccionesElectronicas(Connection conn, EntradaCrearClienteExpress in, SocioNegocio socio) throws Exception {
		logger.debug("INICIO::CrearClienteExpress.EdicionDireccionesElectronicas");
		long inicio = System.currentTimeMillis();
		
		SalidaWBS salidaDireccionElectronica = new SalidaWBS();
		
		List<DireccionElectronica> p_inDireccionesElectronicas = new ArrayList<DireccionElectronica>();
		DireccionElectronica direccionElectronica = new DireccionElectronica();
		direccionElectronica.IdDireccionElectronica = socio.IdEmail;
		direccionElectronica.Direccion = socio.Email;
		direccionElectronica.CodigoProposito = "";
		direccionElectronica.Comentario = "";
		direccionElectronica.IdEstado = "ACT";
		direccionElectronica.IdPersona = socio.IdPersona;
		direccionElectronica.Predeterminado = "S";
		direccionElectronica.IdUsuarioCreador = in.p_IdUsuario;
		
		p_inDireccionesElectronicas.add(direccionElectronica);
		
		SalidaCrearDireccionElectronicaMasivo salidaCrearDireccionElectronicaMasivo = crearDireccionesElectronicas(conn, p_inDireccionesElectronicas,in.p_IdEmpresa, in.p_IdUsuario);//new SalidaCrearDireccionElectronicaMasivo();
		
		Long idDireccionElectronica = salidaCrearDireccionElectronicaMasivo.P_OUT_REFID.get(0).IdEntidad.longValue();
		
		EntidadManipulada entidadDireccion = new EntidadManipulada();
		entidadDireccion.IdEntidad = idDireccionElectronica;
		entidadDireccion.TipoEntidad = "DIRECCION_ELECTRONICA";
		
		if(!salidaCrearDireccionElectronicaMasivo.P_OUT_MENSAJES.isEmpty()) {
			entidadDireccion.Mensajes.addAll(salidaCrearDireccionElectronicaMasivo.P_OUT_MENSAJES);
			salidaDireccionElectronica.P_OUT_ENTIDADES_MANIPULADAS.add(entidadDireccion);
			salidaDireccionElectronica.P_OUT_ERRORESCONTADOR += entidadDireccion.Mensajes.stream().filter(mensaje -> PlataformaUtils.MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo)).count();
			
			return salidaDireccionElectronica;
		}
		
		if (entidadDireccion.Mensajes.isEmpty()) {
			entidadDireccion = WebServiceUtils.agregarMensajeOk(entidadDireccion, "CREADA");
			salidaDireccionElectronica.P_OUT_ENTIDADES_MANIPULADAS.add(entidadDireccion);
		}
		
		salidaDireccionElectronica.P_OUT_ERRORESCONTADOR += entidadDireccion.Mensajes.stream().filter(mensaje -> PlataformaUtils.MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo)).count();
		
		// Actualizacion de Direccion Fisica Principal

		HashMap<String, Object> salidaPredeterminarDireccionElectronica = predeterminarDireccionElectronica(conn, idDireccionElectronica, in);
		
		// Obtencion y eliminacion de las direcciones fisicas a borrar

		List<EntidadPersonaEliminar> registros = obtenerDireccionesElectronicas(conn, socio.IdPersona);
		SalidaWBS salidaEliminarDireccionElectronica = new SalidaWBS();
		
		List<DireccionElectronica> p_DireccionesElectronicas = new ArrayList<>();
		p_DireccionesElectronicas = registros.stream().filter( dirE -> !dirE.IdEntidadPersonaEliminar.equals(idDireccionElectronica)).map(dirE ->{
			DireccionElectronica direccion = new DireccionElectronica();
			direccion.IdDireccionElectronica = dirE.IdEntidadPersonaEliminar;
			direccion.IdPersona = dirE.IdPersona;
			return direccion;
		}).collect(Collectors.toList());
		
		if(!p_DireccionesElectronicas.isEmpty()) {
			EntradaEjecutarCambiosPersona entradaEjecutarCambiosPersona = new EntradaEjecutarCambiosPersona();
			entradaEjecutarCambiosPersona.p_IdEmpresa = in.p_IdEmpresa;
			entradaEjecutarCambiosPersona.p_IdPersona = socio.IdPersona;
			entradaEjecutarCambiosPersona.p_IdModulo = ID_MODULO;
			entradaEjecutarCambiosPersona.p_IdUsuario = in.p_IdUsuario;
			entradaEjecutarCambiosPersona.p_UsaReglasNegocio = USA_REGLAS_NEGOCIO;
			entradaEjecutarCambiosPersona.p_IdTipoCanal = ID_TIPO_CANAL;
			entradaEjecutarCambiosPersona.p_EsCrearCliente = "S";
			entradaEjecutarCambiosPersona.p_RegistroCambio = obtenerRegistrosCambioEliminarDireccionesElectronicas(p_DireccionesElectronicas);
			
			EliminarPersonaDireccionElectronicaMasivo eliminarPersonaDireccionElectronicaMasivo = new EliminarPersonaDireccionElectronicaMasivo();
			SalidaEjecutarCambiosPersona salidaEjecutarCambiosPersona = eliminarPersonaDireccionElectronicaMasivo.logica(conn, entradaEjecutarCambiosPersona);
			
			if (!salidaEjecutarCambiosPersona.P_OUT_MENSAJES.isEmpty()) {
				entidadDireccion.Mensajes.addAll( salidaEjecutarCambiosPersona.P_OUT_MENSAJES);
				salidaEliminarDireccionElectronica.P_OUT_ENTIDADES_MANIPULADAS.add(entidadDireccion);
			}
		}
		salidaDireccionElectronica.P_OUT_ERRORESCONTADOR = salidaDireccionElectronica.P_OUT_ENTIDADES_MANIPULADAS
				.parallelStream().reduce(0L, (sum, ent) -> sum += ent.Mensajes.parallelStream().filter(mensaje -> PlataformaUtils.MensajeSalida.MENSAJE_ERROR .equals(mensaje.tipo)).count(), (sum1, sum2) -> sum1 + sum2);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearClienteExpress.EdicionDireccionesElectronicas", fin - inicio);
		return salidaDireccionElectronica;
	}

	private void actualizarIdPersonaConyugeDeConyuge(Connection conn, SocioNegocio socio) throws Exception {
		logger.debug("INICIO::CrearClienteExpress.actualizarIdPersonaConyugeDeConyuge");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		
		sql.batch(conn, SQL_UPDATE_ID_PERSONA_CONYUGE_DE_CONYUGE, Arrays.asList(socio), SocioNegocio.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearClienteExpress.actualizarIdPersonaConyugeDeConyuge", fin - inicio);
	}
	
	private SalidaCrearDireccionFisicaMasivo crearDireccionesFisicas(Connection conn, List<DireccionFisica> direccionesFisicas, Long idEmpresa, Long idUsuario) throws Exception {
		logger.debug("INICIO::CrearClienteExpress.crearDireccionesFisicas");
		long inicio = System.currentTimeMillis();
		
		EntradaCrearDireccionFisicaMasivo entradaCrearDireccionFisicaMasivo = new EntradaCrearDireccionFisicaMasivo();
		entradaCrearDireccionFisicaMasivo.p_IdEmpresa = idEmpresa;
		entradaCrearDireccionFisicaMasivo.p_IdModulo = ID_MODULO;
		entradaCrearDireccionFisicaMasivo.p_IdUsuario = idUsuario;
		entradaCrearDireccionFisicaMasivo.p_UsaReglasNegocio = USA_REGLAS_NEGOCIO;
		
		CrearDireccionFisicaMasivo crearDireccionFisicaMasivo = new CrearDireccionFisicaMasivo();

		entradaCrearDireccionFisicaMasivo.p_DireccionesFisica.addAll(direccionesFisicas);

		SalidaCrearDireccionFisicaMasivo salidaCrearDireccionFisicaMasivo = crearDireccionFisicaMasivo.logica(conn, entradaCrearDireccionFisicaMasivo);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearClienteExpress.crearDireccionesFisicas", fin - inicio);
		return salidaCrearDireccionFisicaMasivo;
	}
	
	private SalidaWBS EdicionDireccionesFisicas(Connection conn, EntradaCrearClienteExpress in, SocioNegocio socioDesdeBD) throws Exception {
		logger.debug("INICIO::CrearClienteExpress.EdicionDireccionesFisicas");
		long inicio = System.currentTimeMillis();
		
		SalidaWBS salidaDireccionFisica = new SalidaWBS();
		
		List<DireccionFisica> p_inDireccionesFisicas = new ArrayList<DireccionFisica>();
		DireccionFisica direccionFisica = new DireccionFisica();
		direccionFisica.Calle = socioDesdeBD.Calle;
		direccionFisica.Numero = socioDesdeBD.Numero;
		direccionFisica.Departamento = socioDesdeBD.Complemento;
		direccionFisica.Sector = "";
		direccionFisica.CodigoPostal = "";
		direccionFisica.IdCiudad = socioDesdeBD.IdCiudad;
		direccionFisica.IdDivisionPais = socioDesdeBD.IdComuna;
		direccionFisica.CodigoProposito = "PART";
		direccionFisica.Comentario = "";
		direccionFisica.IdEstado = "ACT";
		direccionFisica.IdPersona = socioDesdeBD.IdPersona;
		direccionFisica.Predeterminado = "S";
		direccionFisica.IdUsuarioCreador = in.p_IdUsuario;
		p_inDireccionesFisicas.add(direccionFisica);

		SalidaCrearDireccionFisicaMasivo salidaCrearDireccionFisicaMasivo = crearDireccionesFisicas(conn,p_inDireccionesFisicas, in.p_IdEmpresa, in.p_IdUsuario);// crearDireccionFisicaMasivo.logica(conn, entradaCrearDireccionFisicaMasivo);

		Long idDireccionFisica = salidaCrearDireccionFisicaMasivo.P_OUT_REFID.get(0).IdEntidad.longValue();
		
		EntidadManipulada entidadDireccion = new EntidadManipulada();
		entidadDireccion.IdEntidad = idDireccionFisica;
		entidadDireccion.TipoEntidad = "DIRECCION_FISICA";

		if (!salidaCrearDireccionFisicaMasivo.P_OUT_MENSAJES.isEmpty()) {
			entidadDireccion.Mensajes.addAll(salidaCrearDireccionFisicaMasivo.P_OUT_MENSAJES);
			salidaDireccionFisica.P_OUT_ENTIDADES_MANIPULADAS.add(entidadDireccion);
			salidaDireccionFisica.P_OUT_ERRORESCONTADOR += entidadDireccion.Mensajes.stream().filter(mensaje -> PlataformaUtils.MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo)).count();
			
			return salidaDireccionFisica;
		}

		if (entidadDireccion.Mensajes.isEmpty()) {
			entidadDireccion = WebServiceUtils.agregarMensajeOk(entidadDireccion, "CREADA");
			salidaDireccionFisica.P_OUT_ENTIDADES_MANIPULADAS.add(entidadDireccion);
		}

		salidaDireccionFisica.P_OUT_ERRORESCONTADOR += entidadDireccion.Mensajes.stream().filter(mensaje -> PlataformaUtils.MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo)).count();
		
		// Actualizacion de Direccion Fisica Principal

		HashMap<String, Object> salidaPredeterminarDireccionFisica = predeterminarDireccionFisica(conn, idDireccionFisica, in);
		
		// Obtencion y eliminacion de las direcciones fisicas a borrar

		List<EntidadPersonaEliminar> registros = obtenerDireccionesFisicas(conn, socioDesdeBD.IdPersona);
		SalidaWBS salidaEliminarDireccionFisica = new SalidaWBS();

		List<DireccionFisica> p_DireccionesFisicas = new ArrayList<>();
		p_DireccionesFisicas = registros.stream().filter( dirP -> !dirP.IdEntidadPersonaEliminar.equals(idDireccionFisica)).map(dirP ->{
			DireccionFisica direccion = new DireccionFisica();
			direccion.IdDireccionFisica = dirP.IdEntidadPersonaEliminar;
			direccion.IdPersona = dirP.IdPersona;
			return direccion;
		}).collect(Collectors.toList());
		
		if(!p_DireccionesFisicas.isEmpty()) {
			EntradaEjecutarCambiosPersona entradaEjecutarCambiosPersona = new EntradaEjecutarCambiosPersona();
			entradaEjecutarCambiosPersona.p_IdEmpresa = in.p_IdEmpresa;
			entradaEjecutarCambiosPersona.p_IdPersona = socioDesdeBD.IdPersona;
			entradaEjecutarCambiosPersona.p_IdModulo = ID_MODULO;
			entradaEjecutarCambiosPersona.p_IdUsuario = in.p_IdUsuario;
			entradaEjecutarCambiosPersona.p_UsaReglasNegocio = USA_REGLAS_NEGOCIO;
			entradaEjecutarCambiosPersona.p_IdTipoCanal = ID_TIPO_CANAL;
			entradaEjecutarCambiosPersona.p_EsCrearCliente = "S";
			entradaEjecutarCambiosPersona.p_RegistroCambio = obtenerRegistrosCambioEliminarDireccionesFisicas(p_DireccionesFisicas);
			
			EliminarPersonaDireccionFisicaMasivo eliminarPersonaDireccionFisicaMasivo = new EliminarPersonaDireccionFisicaMasivo();
			SalidaEjecutarCambiosPersona salidaEjecutarCambiosPersona = eliminarPersonaDireccionFisicaMasivo.logica(conn, entradaEjecutarCambiosPersona);
			
			if (!salidaEjecutarCambiosPersona.P_OUT_MENSAJES.isEmpty()) {
				entidadDireccion.Mensajes.addAll( salidaEjecutarCambiosPersona.P_OUT_MENSAJES);
				salidaEliminarDireccionFisica.P_OUT_ENTIDADES_MANIPULADAS.add(entidadDireccion);
			}
		}
		
		salidaDireccionFisica.P_OUT_ERRORESCONTADOR = salidaDireccionFisica.P_OUT_ENTIDADES_MANIPULADAS
				.parallelStream().reduce(0L, (sum, ent) -> sum += ent.Mensajes.parallelStream().filter(mensaje -> PlataformaUtils.MensajeSalida.MENSAJE_ERROR .equals(mensaje.tipo)).count(), (sum1, sum2) -> sum1 + sum2);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearClienteExpress.EdicionDireccionesFisicas", fin - inicio);
		return salidaDireccionFisica;
	}

	private EntidadCreada obtenerMensajesModificacionConyuge(SalidaEjecutarCambiosPersona salidaEjecutarCambiosPersona, EntidadCreada entidadCreada) {
		logger.debug("INICIO::CrearClienteExpress.obtenerMensajesModificacionConyuge");
		long inicio = System.currentTimeMillis();

		AtomicLong contadorAdvertencias = new AtomicLong();
		List<Mensaje> mensajesAdvertencia = salidaEjecutarCambiosPersona.P_OUT_MENSAJES.stream()
				.filter(msj -> (PlataformaUtils.MensajeSalida.MENSAJE_ADVERTENCIA).equals(msj.tipo)).map(advert -> {
					Mensaje mensaje = new Mensaje();
					mensaje.IdInterno = contadorAdvertencias.incrementAndGet();
					mensaje.TipoMensaje = PlataformaUtils.MensajeSalida.MENSAJE_ADVERTENCIA;
					mensaje.Codigo = advert.codigo;
					mensaje.Mensaje = advert.descripcion;
					return mensaje;
				}).collect(Collectors.toList());

		AtomicLong contadorErrores = new AtomicLong();
		List<Mensaje> mensajesError = salidaEjecutarCambiosPersona.P_OUT_MENSAJES.stream()
				.filter(msj ->(PlataformaUtils.MensajeSalida.MENSAJE_ERROR).equals(msj.tipo)).map(error -> {
					Mensaje mensaje = new Mensaje();
					mensaje.IdInterno = contadorErrores.incrementAndGet();
					mensaje.TipoMensaje = PlataformaUtils.MensajeSalida.MENSAJE_ERROR;
					mensaje.Codigo = error.codigo;
					mensaje.Mensaje = error.descripcion;
					return mensaje;
				}).collect(Collectors.toList());

		Collections.sort(mensajesAdvertencia);
		Collections.sort(mensajesError);

		entidadCreada.Mensajes.addAll(mensajesAdvertencia);
		entidadCreada.Mensajes.addAll(mensajesError);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearClienteExpress.obtenerMensajesModificacionConyuge", fin - inicio);
		return entidadCreada;
	}

	private SalidaEjecutarCambiosPersona modificarPersonaConyuge(Connection conn, EntradaCrearClienteExpress in, SocioNegocio socio, Long idPersonaConyuge) throws Exception {
		logger.debug("INICIO::CrearClienteExpress.modificarPersonaConyuge");
		long inicio = System.currentTimeMillis();

		SalidaEjecutarCambiosPersona salidaEjecutarCambiosPersona = new SalidaEjecutarCambiosPersona();

		RelacionadoConyuge conyugeEntrada = obtenerDatosConyugeEntrada(socio);
		RelacionadoConyuge conyugeBD = obtenerDatosConyugeBD(conn, idPersonaConyuge);

		List<RegistroCambioMasivo> cambios = obtenerRegistroCambio(conyugeEntrada, conyugeBD, idPersonaConyuge);

		if (!cambios.isEmpty()) {
			EntradaEjecutarCambiosPersona entradaEjecutarCambiosPersona = new EntradaEjecutarCambiosPersona();
			entradaEjecutarCambiosPersona.p_IdEmpresa = in.p_IdEmpresa;
			entradaEjecutarCambiosPersona.p_IdModulo = ID_MODULO;
			entradaEjecutarCambiosPersona.p_IdUsuario = in.p_IdUsuario;
			entradaEjecutarCambiosPersona.p_UsaReglasNegocio = USA_REGLAS_NEGOCIO;
			entradaEjecutarCambiosPersona.p_IdTipoCanal = ID_TIPO_CANAL;

			entradaEjecutarCambiosPersona.p_RegistroCambio.addAll(cambios);

			ModificarPersonaDatosGeneralesMasivo modificarPersonaDatosGeneralesMasivo = new ModificarPersonaDatosGeneralesMasivo();

			salidaEjecutarCambiosPersona = modificarPersonaDatosGeneralesMasivo.logica(conn, entradaEjecutarCambiosPersona);

		}
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearClienteExpress.modificarPersonaConyuge", fin - inicio);
		return salidaEjecutarCambiosPersona;
	}

	private RelacionadoConyuge obtenerDatosConyugeBD(Connection conn, Long idPersonaConyuge) throws Exception {
		logger.debug("INICIO::CrearClienteExpress.obtenerDatosConyugeBD");
		long inicio = System.currentTimeMillis();

		SQL sql = new SQL();
		HashMap<String, Object> param = new HashMap<String, Object>(2);
		param.put("IdPersona", idPersonaConyuge);

		RelacionadoConyuge conyuge = sql.query(conn, SQL_SELECT_CONYUGE_POR_ID_PERSONA, param, RelacionadoConyuge.class).get(0);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearClienteExpress.obtenerDatosConyugeBD", fin - inicio);
		return conyuge;
	}

	private RelacionadoConyuge obtenerDatosConyugeEntrada(SocioNegocio socio) {
		logger.debug("INICIO::CrearClienteExpress.obtenerDatosConyugeEntrada");
		long inicio = System.currentTimeMillis();

		RelacionadoConyuge conyuge = new RelacionadoConyuge();
		conyuge.Nombres = socio.ConyugeNombres;
		conyuge.ApellidoPaterno = socio.ConyugeApellidoPaterno;
		conyuge.ApellidoMaterno = socio.ConyugeApellidoMaterno;

		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearClienteExpress.obtenerDatosConyugeEntrada", fin - inicio);
		return conyuge;
	}

	private List<RegistroCambioMasivo> obtenerRegistroCambio(RelacionadoConyuge conyugeEntrada, RelacionadoConyuge conyugeBD, Long idPersonaConyuge) {
		logger.debug("INICIO::CrearClienteExpress.obtenerRegistroCambio");
		long inicio = System.currentTimeMillis();

		List<RegistroCambioMasivo> cambios = new ArrayList<RegistroCambioMasivo>();

		if (!conyugeEntrada.Nombres.equals(conyugeBD.Nombres) && !conyugeEntrada.Nombres.isEmpty()) {
			RegistroCambioMasivo cambioNombres = new RegistroCambioMasivo();
			cambioNombres.CampoModificado = "Nombres";
			cambioNombres.IdEntidad = idPersonaConyuge;
			cambioNombres.ValorNuevo = conyugeEntrada.Nombres;
			cambios.add(cambioNombres);
		}
		if(!conyugeEntrada.ApellidoPaterno.equals(conyugeBD.ApellidoPaterno) && !conyugeEntrada.ApellidoPaterno.isEmpty()) {
			RegistroCambioMasivo cambioPaterno = new RegistroCambioMasivo();
			cambioPaterno.CampoModificado = "ApellidoPaterno";
			cambioPaterno.IdEntidad = idPersonaConyuge;
			cambioPaterno.ValorNuevo = conyugeEntrada.ApellidoPaterno;
			cambios.add(cambioPaterno);
		}
		if(!conyugeEntrada.ApellidoMaterno.equals(conyugeBD.ApellidoMaterno) && !conyugeEntrada.ApellidoMaterno.isEmpty()) {
			RegistroCambioMasivo cambioMaterno = new RegistroCambioMasivo();
			cambioMaterno.CampoModificado = "ApellidoMaterno";
			cambioMaterno.IdEntidad = idPersonaConyuge;
			cambioMaterno.ValorNuevo = conyugeEntrada.ApellidoMaterno;
			cambios.add(cambioMaterno);
		}

		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearClienteExpress.obtenerRegistroCambio", fin - inicio);
		return cambios;
	}

	private List<Mensaje> agregarMensajesAdvertencias(Connection conn, EntradaCrearClienteExpress in, Long idPersonaConyuge) throws Exception {
		logger.debug("INICIO::CrearClienteExpress.agregarMensajesAdvertencias");
	    long inicio = System.currentTimeMillis();
	    
	    HashMap<String, String> parametrosMensaje = new HashMap<>(2);
		PlataformaUtils.MensajeSalida ms = new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ADVERTENCIA, "UI_ADVERTENCIA_PERSONA_RELACIONADA_EXISTE", parametrosMensaje, conn, in.p_IdEmpresa);
		
		Mensaje mensajeWS = new Mensaje();
		mensajeWS.Codigo = ms.codigo;
		mensajeWS.TipoMensaje = ms.tipo;
		mensajeWS.Mensaje = ms.descripcion;
		mensajeWS.IdInterno = idPersonaConyuge;
		List<Mensaje> mensajes = new ArrayList<Mensaje>();
		mensajes.add(mensajeWS);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearClienteExpress.agregarMensajesAdvertencias", fin - inicio);
		return mensajes;
	}

	private SocioNegocio obtenerSocioDesdeBD(Connection conn, String numeroDocumento) throws Exception {
		logger.debug("INICIO::CrearClienteExpress.obtenerSocioDesdeBD");
		long inicio = System.currentTimeMillis();

		HashMap<String, Object> parametros = new HashMap<String, Object>();
		parametros.put("DocumentoIdentidad", numeroDocumento);

		SQL sql = new SQL();
		SocioNegocio socio = sql .query(conn, SQL_SELECT_DATOS_PERSONA_POR_DOCUMENTO_IDENTIDAD, parametros, SocioNegocio.class)
				.parallelStream().findFirst().orElse(null);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearClienteExpress.obtenerSocioDesdeBD", fin - inicio);
		return socio;
	}

	private Long buscarPersonaConyuge(Connection conn, SocioNegocio socio) throws SQLException {
		logger.debug("INICIO::CrearClienteExpress.buscarPersonaConyuge");
		long inicio = System.currentTimeMillis();

		Long idPersonaConyuge = 0L;
		HashMap<String, Object> parametros = new HashMap<String, Object>(2);
		parametros.put("IdTipoDocumentoIdentidad", socio.ConyugeTipoDocumentoIdentidad);
		parametros.put("DocumentoIdentidad", socio.ConyugeDocumentoIdentidad);

		SQL sql = new SQL();
		List<Id> selectIdPersonaConyuge = sql.query(conn, SQL_SELECT_IDPERSONA_POR_DOCUMENTO_IDENTIDAD, parametros, Id.class);
		if (selectIdPersonaConyuge.size() > 0) {
			idPersonaConyuge = selectIdPersonaConyuge.get(0).Id;
		}

		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearClienteExpress.buscarPersonaConyuge", fin - inicio);
		return idPersonaConyuge;
	}

	private EntidadCreada obtenerMensajes(SalidaValidarCamposObligatorios salidaValidarConyuge, EntidadCreada entidad) {
		logger.debug("INICIO::CrearClienteExpress.obtenerMensajes");
		long inicio = System.currentTimeMillis();

		AtomicLong idInterno = new AtomicLong();
		List<Mensaje> mensajes = salidaValidarConyuge.P_OUT_MENSAJES.parallelStream().map(m -> {
			Mensaje mensaje = new Mensaje();
			mensaje.Codigo = m.codigo;
			mensaje.TipoMensaje = m.tipo;
			mensaje.Mensaje = m.descripcion;
			mensaje.IdInterno = idInterno.getAndIncrement();
			return mensaje;
		}).collect(Collectors.toList());
		Collections.sort(mensajes);
		entidad.Mensajes.addAll(mensajes);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearClienteExpress.obtenerMensajes", fin - inicio);
		return entidad;
	}

	private SalidaValidarCamposObligatorios validarFormularioConyuge(Connection conn, EntradaCrearClienteExpress in) throws Exception {
	    logger.debug("INICIO::CrearClienteExpress.validarFormularioConyuge");
	    long inicio = System.currentTimeMillis();
	    
	    FormularioCanal formularioCanal = new FormularioCanal();
	    formularioCanal.CodigoFormulario = "INGRESAR_CLIENTE_EXPRESS_NATURAL";
	    formularioCanal.IdEmpresa = in.p_IdEmpresa;
	    formularioCanal.IdTipoCanal = ID_TIPO_CANAL;
	    ValidarCamposObligatorios validarCampos = new ValidarCamposObligatorios();
	    List<Enrolamiento> enrolamientos = obtenerEnrolamientos(in.p_SocioNegocio); 
	    SalidaValidarCamposObligatorios salida = validarCampos.validarObjeto(conn, formularioCanal, enrolamientos, Enrolamiento.class);
	    
	    long fin = System.currentTimeMillis();
	    logger.debug("FIN::CrearClienteExpress.validarFormularioConyuge", fin - inicio);
	    return salida;
	}

	private List<Enrolamiento> obtenerEnrolamientos(List<SocioNegocio> p_SocioNegocio) {
		List<Enrolamiento> enrolamientos = p_SocioNegocio.parallelStream().map(socioNegocio -> {

		    Enrolamiento enrolamiento = new Enrolamiento();
		    enrolamiento.IdEnrolamiento = 0L;
		    enrolamiento.TipoEnrolamiento = "EXPRESS";
		    enrolamiento.IdTipoEntidad = TIPO_ENTIDAD_CLIENTE;
		    enrolamiento.IdEntidadEnrolada = 0L;
		    enrolamiento.IdTipoDocumentoIdentidad = socioNegocio.TipoDocumento.trim();
		    enrolamiento.DocumentoIdentidad = socioNegocio.NumeroDocumento.trim().replace(".", "");
		    enrolamiento.Tipo = socioNegocio.IdTipoPersona;
		    enrolamiento.Nombres = socioNegocio.Nombres;
		    enrolamiento.ApellidoPaterno = socioNegocio.ApellidoPaterno;
		    enrolamiento.ApellidoMaterno = socioNegocio.ApellidoMaterno;
		    enrolamiento.RazonSocial = socioNegocio.RazonSocial;
		    enrolamiento.Sexo = socioNegocio.IdGenero.trim();
		    enrolamiento.IdPaisNacimiento = socioNegocio.IdPaisNacimientoConstitucion;
		    enrolamiento.FechaNacimiento = socioNegocio.FechaNacimientoConstitucion.trim().isEmpty() ? Fecha.toDate("0001-01-01") : Fecha.toDate(socioNegocio.FechaNacimientoConstitucion);
		    enrolamiento.EstadoCivil = socioNegocio.IdEstadoCivil.trim();
		    enrolamiento.RegimenConyugal = socioNegocio.IdRegimenConyugal;
		    enrolamiento.IdPaisResidencia = socioNegocio.IdPaisResidenciaCasaMatriz;
		    enrolamiento.IdCiudad = socioNegocio.IdCiudad;
			enrolamiento.IdDivisionPais = socioNegocio.IdComuna;
		    enrolamiento.DireccionCalle = socioNegocio.Calle;
		    enrolamiento.DireccionNumero = socioNegocio.Numero;
		    enrolamiento.DireccionDepartamento = socioNegocio.Complemento;
		    enrolamiento.IdTipoTelefono = socioNegocio.TipoTelefono;
		    enrolamiento.Telefono = socioNegocio.Telefono;
		    enrolamiento.Email = socioNegocio.Email;
		    enrolamiento.CLI_IdCategoriaInversionista = socioNegocio.CategoriaInversionista;
		    enrolamiento.POR_IdPerfilComercial = socioNegocio.PerfilComercial;
		    enrolamiento.CLI_IdTipoCliente = socioNegocio.TipoCliente;
		    enrolamiento.CLI_IdTipoClienteCMF = socioNegocio.TipoClienteCMF;
		    enrolamiento.CLI_AreaCliente = socioNegocio.AreaCliente;
		    enrolamiento.CLI_IdEmpleadoEjecutivo = socioNegocio.Ejecutivo;
		    enrolamiento.IdPersonaConyuge = socioNegocio.IdPersonaConyuge;
		    enrolamiento.IdTipoDocumentoIdentidadAsociado = "";
		    enrolamiento.ConyugeTipoDocumentoIdentidad = socioNegocio.ConyugeTipoDocumentoIdentidad;
		    enrolamiento.ConyugeDocumentoIdentidad = socioNegocio.ConyugeDocumentoIdentidad;
		    enrolamiento.ConyugeNombres = socioNegocio.ConyugeNombres;
		    enrolamiento.ConyugeApellidoPaterno = socioNegocio.ConyugeApellidoPaterno;
		    enrolamiento.ConyugeApellidoMaterno = socioNegocio.ConyugeApellidoMaterno;
		    enrolamiento.EnrolarConyuge = socioNegocio.EnrolarConyuge;
		return enrolamiento;
	    }).collect(Collectors.toList());
	    return enrolamientos;
	}

	private EntidadCreada agregarError(String codigo, String descripcion, EntidadCreada entidad) {
		logger.debug("INICIO::CrearClienteExpress.agregarError");
		long inicio = System.currentTimeMillis();

		Mensaje m = new Mensaje();
		m.Codigo = codigo;
		m.IdInterno = 0L;
		m.Mensaje = descripcion;
		m.TipoMensaje = PlataformaUtils.MensajeSalida.MENSAJE_ERROR;
		entidad.Mensajes.add(m);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearClienteExpress.agregarError", fin - inicio);
		return entidad;
	}

	private boolean idPersonaExiste(Connection conn, Long idPersonaConyuge) throws SQLException {
		logger.debug("INICIO::CrearClienteExpress.idPersonaExiste");
		long inicio = System.currentTimeMillis();

		HashMap<String, Object> parametros = new HashMap<String, Object>(2);
		parametros.put("IdPersonas", idPersonaConyuge);
		SQL sql = new SQL();
		List<Id> idsPersonasValidas = sql.query(conn, SQL_SELECT_IDPERSONA, parametros, Id.class);
		Boolean personaExiste = idsPersonasValidas.size() == 1;

		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearClienteExpress.idPersonaExiste", fin - inicio);
		return personaExiste;

	}

	private Long contarErrores(SalidaCrearClienteExpress salida) {
	    logger.debug("INICIO::CrearClienteExpress.contarErrores");
	    long inicio = System.currentTimeMillis();
	    
	    Long errores = salida.P_OUT_ENTIDADCREADA.parallelStream().reduce(0L,
			    (sum, ent) -> sum += ent.Mensajes.stream().filter(m -> PlataformaUtils.MensajeSalida.MENSAJE_ERROR.equals(m.TipoMensaje)).count(), 
			    (sum1, sum2) -> sum1 + sum2);
	    long fin = System.currentTimeMillis();
	    logger.debug("FIN::CrearClienteExpress.contarErrores", fin - inicio);
	    return errores;
	}

	private Long contarAdvertencias(SalidaCrearClienteExpress salida) {
	    logger.debug("INICIO::CrearClienteExpress.contarAdvertencias");
	    long inicio = System.currentTimeMillis();
	    
	    Long advertencias = salida.P_OUT_ENTIDADCREADA.parallelStream().reduce(0L,
			    (sum, ent) -> sum += ent.Mensajes.stream().filter(m -> PlataformaUtils.MensajeSalida.MENSAJE_ADVERTENCIA.equals(m.TipoMensaje)).count(), 
			    (sum1, sum2) -> sum1 + sum2);
	    long fin = System.currentTimeMillis();
	    logger.debug("FIN::CrearClienteExpress.contarAdvertencias", fin - inicio);
	    return advertencias;
	}
	
	private EntidadCreada obtenerMensajes(SalidaServicioCrearPersona salidaServicioCrearPersona, EntidadCreada entidadCreada) {
	    logger.debug("INICIO::CrearClienteExpress.obtenerMensajes");
	    long inicio = System.currentTimeMillis();
	    AtomicLong contadorAdvertencias = new AtomicLong();
	    List<Mensaje> mensajesAdvertencia = salidaServicioCrearPersona.P_OUT_ADVERTENCIASDETALLE.parallelStream().collect(Collectors.mapping(advertencias -> {
			Mensaje mensaje = new Mensaje();
			mensaje.IdInterno = contadorAdvertencias.incrementAndGet();
			mensaje.TipoMensaje = PlataformaUtils.MensajeSalida.MENSAJE_ADVERTENCIA;
			mensaje.Codigo = advertencias.Codigo;
			mensaje.Mensaje = advertencias.Descripcion;
			return mensaje;
	    }, Collectors.toList()));

	    AtomicLong contadorErrores = new AtomicLong();
	    List<Mensaje> mensajesError = salidaServicioCrearPersona.P_OUT_ERRORESDETALLE.parallelStream().collect(Collectors.mapping(error -> {
		Mensaje mensaje = new Mensaje();
			mensaje.IdInterno = contadorErrores.incrementAndGet();
			mensaje.TipoMensaje = PlataformaUtils.MensajeSalida.MENSAJE_ERROR;
			mensaje.Codigo = error.Codigo;
			mensaje.Mensaje = error.Descripcion;
			return mensaje;
	    }, Collectors.toList()));

		Collections.sort(mensajesAdvertencia);
		Collections.sort(mensajesError);

		entidadCreada.Mensajes.addAll(mensajesAdvertencia);
		entidadCreada.Mensajes.addAll(mensajesError);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearClienteExpress.obtenerMensajes", fin - inicio);
		return entidadCreada;
	}

	private EntradaServicioCrearPersona obtenerEntradaCrearPersonaConyuge(Connection conn, SocioNegocio socio, EntradaCrearClienteExpress entrada) throws SQLException {
		logger.debug("INICIO::CrearClienteExpress.obtenerEntradaCrearPersonaConyuge");
		long inicio = System.currentTimeMillis();

		List<cl.bcs.socionegocio.pojo.Enrolamiento> enrolamiento = new ArrayList<>();
		cl.bcs.socionegocio.pojo.Enrolamiento personaCrear = new cl.bcs.socionegocio.pojo.Enrolamiento();
		personaCrear.IdTipoDocumentoIdentidad = socio.ConyugeTipoDocumentoIdentidad;
		personaCrear.DocumentoIdentidad = socio.ConyugeDocumentoIdentidad;
		personaCrear.Nombres = socio.ConyugeNombres;
		personaCrear.ApellidoPaterno = socio.ConyugeApellidoPaterno;
		personaCrear.ApellidoMaterno = socio.ConyugeApellidoMaterno;
		personaCrear.IdTipoEntidad = "PERSONA";
		personaCrear.IdPaisNacimiento = 0L;
		personaCrear.Tipo = "N";
		personaCrear.IdEmpresa = entrada.p_IdEmpresa;
		personaCrear.IdTipoDocumentoIdentidadAsociado = "";
		personaCrear.EstadoCivil = socio.IdEstadoCivil;
		personaCrear.RegimenConyugal = socio.IdRegimenConyugal;
		enrolamiento.add(personaCrear);

		EntradaServicioCrearPersona entradaServicioCrearPersona = new EntradaServicioCrearPersona();
		entradaServicioCrearPersona.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaServicioCrearPersona.p_IdUsuario = entrada.p_IdUsuario;
		entradaServicioCrearPersona.p_IdModulo = ID_MODULO;
		entradaServicioCrearPersona.p_ConsultarEnrolamiento = enrolamiento;

		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearClienteExpress.obtenerEntradaCrearPersonaConyuge", fin - inicio);
		return entradaServicioCrearPersona;
	}

	private boolean socioNecesitaCrearPersonaConyuge(SocioNegocio socio) {
	    return "N".equals(socio.IdTipoPersona) && socio.IdPersonaConyuge == 0L && ("CA".equals(socio.IdEstadoCivil) || "CC".equals(socio.IdEstadoCivil));
	}

	private EntradaServicioCrearCliente obtenerEntradaCrearCliente(SocioNegocio socioNegocio, EntradaCrearClienteExpress entrada) {
		logger.debug("INICIO::CrearClienteExpress.obtenerEntradaCrearCliente");
		long inicio = System.currentTimeMillis();

	    EntradaServicioCrearCliente entradaCrearCliente = new EntradaServicioCrearCliente();
	    entradaCrearCliente.p_IdUsuario = entrada.p_IdUsuario;
	    entradaCrearCliente.p_IdEmpresa = entrada.p_IdEmpresa;
	    entradaCrearCliente.p_IdModulo = ID_MODULO;
	    entradaCrearCliente.p_IdTipoCanal = socioNegocio.IdTipoCanalIngresoCorredor.trim().isEmpty() ? ID_TIPO_CANAL : socioNegocio.IdTipoCanalIngresoCorredor; 
	    entradaCrearCliente.p_UsaReglasNegocio = USA_REGLAS_NEGOCIO;
	    entradaCrearCliente.p_CodigoFormulario = obtenerCodigoFormulario(socioNegocio.IdTipoPersona);
	    entradaCrearCliente.p_ConsultarEnrolamiento = new ArrayList<>();
	    Enrolamiento enrolamiento = new Enrolamiento();
	    enrolamiento.IdEnrolamiento = 0L;
	    enrolamiento.IdEmpresa = entrada.p_IdEmpresa;
	    enrolamiento.IdUsuario = entrada.p_IdUsuario;
	    enrolamiento.TipoEnrolamiento = "EXPRESS";
	    enrolamiento.IdTipoEntidad = TIPO_ENTIDAD_CLIENTE;
	    enrolamiento.IdEntidadEnrolada = 0L;
	    enrolamiento.IdTipoDocumentoIdentidad = socioNegocio.TipoDocumento.trim();
	    enrolamiento.DocumentoIdentidad = socioNegocio.NumeroDocumento.trim().replace(".", "");
	    enrolamiento.Tipo = socioNegocio.IdTipoPersona;
	    enrolamiento.Nombres = socioNegocio.Nombres;
	    enrolamiento.ApellidoPaterno = socioNegocio.ApellidoPaterno;
	    enrolamiento.ApellidoMaterno = socioNegocio.ApellidoMaterno;
	    enrolamiento.RazonSocial = socioNegocio.RazonSocial;
	    enrolamiento.Sexo = socioNegocio.IdGenero.trim();
	    enrolamiento.IdPaisNacimiento = socioNegocio.IdPaisNacimientoConstitucion;
	    enrolamiento.FechaNacimiento = socioNegocio.FechaNacimientoConstitucion.trim().isEmpty() ? Fecha.toDate("0001-01-01") : Fecha.toDate(socioNegocio.FechaNacimientoConstitucion);
	    enrolamiento.EstadoCivil = socioNegocio.IdEstadoCivil.trim();
	    enrolamiento.RegimenConyugal = socioNegocio.IdRegimenConyugal;
	    enrolamiento.IdPaisResidencia = socioNegocio.IdPaisResidenciaCasaMatriz;
		enrolamiento.IdPaisNacionalidadPrincipal = socioNegocio.IdPaisNacimientoConstitucion;
	    enrolamiento.IdCiudad = socioNegocio.IdCiudad;
		enrolamiento.IdDivisionPais = socioNegocio.IdComuna;
	    enrolamiento.DireccionCalle = socioNegocio.Calle;
	    enrolamiento.DireccionNumero = socioNegocio.Numero;
	    enrolamiento.DireccionDepartamento = socioNegocio.Complemento;
	    enrolamiento.IdTipoTelefono = socioNegocio.TipoTelefono;
	    enrolamiento.Telefono = socioNegocio.Telefono;
	    enrolamiento.Email = socioNegocio.Email;
	    enrolamiento.CLI_IdCategoriaInversionista = socioNegocio.CategoriaInversionista;
	    enrolamiento.POR_IdPerfilComercial = socioNegocio.PerfilComercial;
	    enrolamiento.CLI_IdTipoCliente = socioNegocio.TipoCliente;
	    enrolamiento.CLI_IdTipoClienteCMF = socioNegocio.TipoClienteCMF;
	    enrolamiento.CLI_AreaCliente = socioNegocio.AreaCliente;
	    enrolamiento.CLI_IdEmpleadoEjecutivo = socioNegocio.Ejecutivo;
	    enrolamiento.IdPersonaConyuge = socioNegocio.IdPersonaConyuge;
	    enrolamiento.IdTipoDocumentoIdentidadAsociado = "";
	    enrolamiento.ConyugeTipoDocumentoIdentidad = socioNegocio.ConyugeTipoDocumentoIdentidad;
	    enrolamiento.ConyugeDocumentoIdentidad = socioNegocio.ConyugeDocumentoIdentidad;
	    enrolamiento.ConyugeNombres = socioNegocio.ConyugeNombres;
	    enrolamiento.ConyugeApellidoPaterno = socioNegocio.ConyugeApellidoPaterno;
	    enrolamiento.ConyugeApellidoMaterno = socioNegocio.ConyugeApellidoMaterno;
	    enrolamiento.EnrolarConyuge = socioNegocio.EnrolarConyuge;
	    enrolamiento.IdSituacionLaboral = socioNegocio.IdSituacionLaboral;
	    enrolamiento.CodigoPropositoTransaccion = socioNegocio.CodigoPropositoTransaccion;
	    entradaCrearCliente.p_ConsultarEnrolamiento.add(enrolamiento);
	    long fin = System.currentTimeMillis();
	    logger.debug("FIN::CrearClienteExpress.obtenerEntradaCrearCliente", fin - inicio);
	return entradaCrearCliente;
	}
	
	private SalidaCrearClienteExpress agregarError(String codigo, String descripcion, SalidaCrearClienteExpress salida) {
	    logger.debug("INICIO::CrearClienteExpress.agregarError");
	    long inicio = System.currentTimeMillis();
	    EntidadCreada entidad = new EntidadCreada();
	    agregarError(codigo, descripcion, entidad);
	    salida.P_OUT_ENTIDADCREADA.add(entidad);
	    salida.P_OUT_ERRORESCONTADOR = 1L;
	    Long fin = System.currentTimeMillis();
	    
	    logger.debug("FIN::CrearClienteExpress.agregarError", fin - inicio);
	    return salida;
	}

	private EntidadCreada obtenerClienteCreado(SalidaServicioCrearCliente servicioCrearCliente, EntidadCreada entidadCreada) {
	    logger.debug("INICIO::CrearClienteExpress.obtenerEntidadesCreadas");
	    long inicio = System.currentTimeMillis();

	    entidadCreada.IdEntidad = servicioCrearCliente.P_OUT_REFIDCLIENTE;
	    entidadCreada.TipoEntidad = "CLIENTE";
	    AtomicLong contadorAdvertencias = new AtomicLong();
	    List<Mensaje> mensajesAdvertencia = servicioCrearCliente.P_OUT_ADVERTENCIASDETALLE.parallelStream().collect(Collectors.mapping(advertencias -> {
			Mensaje mensaje = new Mensaje();
			mensaje.IdInterno = contadorAdvertencias.incrementAndGet();
			mensaje.TipoMensaje = PlataformaUtils.MensajeSalida.MENSAJE_ADVERTENCIA;
			mensaje.Codigo = advertencias.Codigo;
			mensaje.Mensaje = advertencias.Descripcion;
			return mensaje;
	    }, Collectors.toList()));

	    AtomicLong contadorErrores = new AtomicLong();
	    List<Mensaje> mensajesError = servicioCrearCliente.P_OUT_ERRORESDETALLE.parallelStream().collect(Collectors.mapping(error -> {
			Mensaje mensaje = new Mensaje();
			mensaje.IdInterno = contadorErrores.incrementAndGet();
			mensaje.TipoMensaje = PlataformaUtils.MensajeSalida.MENSAJE_ERROR;
			mensaje.Codigo = error.Codigo;
			mensaje.Mensaje = error.Descripcion;
			return mensaje;
	    }, Collectors.toList()));

		Collections.sort(mensajesAdvertencia);
		Collections.sort(mensajesError);

		entidadCreada.Mensajes.addAll(mensajesAdvertencia);
		entidadCreada.Mensajes.addAll(mensajesError);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearClienteExpress.obtenerEntidadesCreadas", fin - inicio);
		return entidadCreada;
	}

	private String obtenerCodigoFormulario(String tipoPersona) {
		return "J".equals(tipoPersona) ? "INGRESAR_CLIENTE_EXPRESS_JURIDICA" : "INGRESAR_CLIENTE_EXPRESS_NATURAL";
	}
	
	private CrearAutorizadoOrden_Salida crearAutorizadoOrden(Connection conn, EntradaCrearClienteExpress in, SocioNegocio socio) throws Exception {
		
		CrearAutorizadoOrden crearAutorizadoOrden = new CrearAutorizadoOrden();
	    CrearAutorizadoOrden_Entrada entradaCrearAutorizadoOrden = new CrearAutorizadoOrden_Entrada();
		String OperarIClientes  = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "SOCIODENEGOCIO", "CLI.OperarIClientes");		
	    
		SQL sql = new SQL();
		HashMap<String,Object> parametros 	= new HashMap<>();
		parametros.put("DocumentoIdentidad", socio.NumeroDocumento);
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

	private HashMap<String, Object> predeterminarTelefono(Connection conn, Long idTelefono,
			EntradaCrearClienteExpress entrada) throws Exception {
		logger.debug("INICIO::predeterminarTelefono");
		long inicio = System.currentTimeMillis();

		SQL actualizar_telefono = new SQL();

		HashMap<String, Object> paramIN = new HashMap<>(5);
		paramIN.put("IdEmpresa", entrada.p_IdEmpresa);
		paramIN.put("IdUsuario", entrada.p_IdUsuario);
		paramIN.put("IdModulo", ID_MODULO);
		paramIN.put("UsaReglasNegocio", USA_REGLAS_NEGOCIO);
		paramIN.put("IdTelefono", idTelefono);

		HashMap<String, Class> paramOUT = new HashMap<>(5);
		paramOUT.put("p_out_RefId", Integer.class);
		paramOUT.put("p_out_AdvertenciasContador", Integer.class);
		paramOUT.put("p_out_AdvertenciasDetalle", MensajeAdvertencia.class);
		paramOUT.put("p_out_ErroresContador", Integer.class);
		paramOUT.put("p_out_ErroresDetalle", ErroresDetalle.class);
		HashMap<String, Object> resultadoPT = actualizar_telefono.call(conn, SQL_PREDETERMINAR_PERSONA_TELEFONO, paramIN, paramOUT);

		long finServicio = System.currentTimeMillis();
		logger.info("FIN::predeterminarTelefono", finServicio - inicio);
		return resultadoPT;
	}

	private HashMap<String, Object> predeterminarDireccionFisica(Connection conn, Long idDireccionFisica,
			EntradaCrearClienteExpress entrada) throws Exception {
		logger.debug("INICIO::predeterminarDireccionFisica");
		long inicio = System.currentTimeMillis();

		SQL actualizar_direccion = new SQL();

		HashMap<String, Object> paramIN = new HashMap<>();
		paramIN.put("IdEmpresa", entrada.p_IdEmpresa);
		paramIN.put("IdUsuario", entrada.p_IdUsuario);
		paramIN.put("IdModulo", ID_MODULO);
		paramIN.put("UsaReglasNegocio", USA_REGLAS_NEGOCIO);
		paramIN.put("IdDireccionFisica", idDireccionFisica);

		HashMap<String, Class> paramOUT = new HashMap<>();
		paramOUT.put("p_out_RefId", Integer.class);
		paramOUT.put("p_out_AdvertenciasContador", Integer.class);
		paramOUT.put("p_out_AdvertenciasDetalle", MensajeAdvertencia.class);
		paramOUT.put("p_out_ErroresContador", Integer.class);
		paramOUT.put("p_out_ErroresDetalle", ErroresDetalle.class);
		HashMap<String, Object> resultadoPF = actualizar_direccion.call(conn, SQL_PREDETERMINAR_PERSONA_DIRECCION_FISICA, paramIN, paramOUT);

		long finServicio = System.currentTimeMillis();
		logger.info("FIN::predeterminarDireccionFisica", finServicio - inicio);
		return resultadoPF;
	}

	private HashMap<String, Object> predeterminarDireccionElectronica(Connection conn, Long idDireccionElectronica,
			EntradaCrearClienteExpress entrada) throws Exception {
		logger.debug("INICIO::predeterminarDireccionElectronica");
		long inicio = System.currentTimeMillis();

		SQL actualizar_correo = new SQL();

		HashMap<String, Object> paramIN = new HashMap<>();
		paramIN.put("IdEmpresa", entrada.p_IdEmpresa);
		paramIN.put("IdUsuario", entrada.p_IdUsuario);
		paramIN.put("IdModulo", ID_MODULO);
		paramIN.put("UsaReglasNegocio", USA_REGLAS_NEGOCIO);
		paramIN.put("IdDireccionElectronica", idDireccionElectronica);

		HashMap<String, Class> paramOUT = new HashMap<>();
		paramOUT.put("p_out_RefId", Integer.class);
		paramOUT.put("p_out_AdvertenciasContador", Integer.class);
		paramOUT.put("p_out_AdvertenciasDetalle", MensajeAdvertencia.class);
		paramOUT.put("p_out_ErroresContador", Integer.class);
		paramOUT.put("p_out_ErroresDetalle", ErroresDetalle.class);
		HashMap<String, Object> resultadoPC = actualizar_correo.call(conn, SQL_PREDETERMINAR_PERSONA_DIRECCION_ELECTRONICA, paramIN, paramOUT);

		long finServicio = System.currentTimeMillis();
		logger.info("FIN::predeterminarDireccionElectronica", finServicio - inicio);
		return resultadoPC;
	}

	private List<EntidadPersonaEliminar> obtenerDireccionesFisicas(Connection con, Long idPersona) throws Exception {
		logger.debug("INICIO::EliminarPersonaDireccionFisicaMasivo.obtenerDireccionesFisicas");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		HashMap<String, Object> params = new HashMap<String, Object>();

		List<Long> idEntidades = new ArrayList<Long>();
		idEntidades.add(idPersona);
		params.put("IdEntidad", idEntidades);
		List<EntidadPersonaEliminar> lstEntidad = sql.query(con, SQL_SELECT_DIRECCIONES_FISICAS_ID_PERSONA, params, EntidadPersonaEliminar.class);

		long finServicio = System.currentTimeMillis();
		logger.info("FIN::EliminarPersonaDireccionFisicaMasivo.obtenerDireccionesFisicas", finServicio - inicio);
		return lstEntidad;
	}

	private List<RegistroCambioMasivo> obtenerRegistrosCambioEliminarDireccionesFisicas(List<DireccionFisica> cuentas) {
		logger.trace("INICIO::EliminarDireccionFisica.obtenerRegistrosCambioEliminarDireccionFisica");
		long inicio = System.currentTimeMillis();

		List<RegistroCambioMasivo> registrosCambio = new ArrayList<>();
		for (DireccionFisica DireccionFisica : cuentas) {
			registrosCambio.add(WebServiceUtils.crearRegistroCambio(DireccionFisica.IdDireccionFisica, "Borrado", "S"));
		}

		long fin = System.currentTimeMillis();
		logger.trace("FIN::EliminarDireccionFisica.obtenerRegistrosCambioEliminarDireccionFisica", fin - inicio);
		return registrosCambio;
	}

	private List<EntidadPersonaEliminar> obtenerTelefonos(Connection con, Long idPersona) throws Exception {
		logger.debug("INICIO::EliminarPersonaTelefonoMasivo.obtenerTelefonos");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		HashMap<String, Object> params = new HashMap<String, Object>();

		List<Long> idEntidades = new ArrayList<Long>();
		idEntidades.add(idPersona);
		params.put("IdEntidad", idEntidades);
		List<EntidadPersonaEliminar> lstEntidad = sql.query(con, SQL_SELECT_TELEFONOS_ID_PERSONA, params, EntidadPersonaEliminar.class);

		long finServicio = System.currentTimeMillis();
		logger.info("FIN::EliminarPersonaTelefonoMasivo.obtenerTelefonos", finServicio - inicio);
		return lstEntidad;
	}

	private List<RegistroCambioMasivo> obtenerRegistrosCambioEliminarTelefonos(List<Telefono> cuentas) {
		logger.trace("INICIO::EliminarTelefono.obtenerRegistrosCambioEliminarTelefono");
		long inicio = System.currentTimeMillis();

		List<RegistroCambioMasivo> registrosCambio = new ArrayList<>();
		for (Telefono Telefono : cuentas) {
			registrosCambio.add(WebServiceUtils.crearRegistroCambio(Telefono.IdTelefono, "Borrado", "S"));
		}

		long fin = System.currentTimeMillis();
		logger.trace("FIN::EliminarTelefono.obtenerRegistrosCambioEliminarTelefono", fin - inicio);
		return registrosCambio;
	}

	private List<EntidadPersonaEliminar> obtenerDireccionesElectronicas(Connection con, Long idPersona)
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

	private List<RegistroCambioMasivo> obtenerRegistrosCambioEliminarDireccionesElectronicas(
			List<DireccionElectronica> cuentas) {
		logger.trace("INICIO::EliminarDireccionElectronica.obtenerRegistrosCambioEliminarDireccionElectronica");
		long inicio = System.currentTimeMillis();

		List<RegistroCambioMasivo> registrosCambio = new ArrayList<>();
		for (DireccionElectronica DireccionElectronica : cuentas) {
			registrosCambio.add( WebServiceUtils.crearRegistroCambio(DireccionElectronica.IdDireccionElectronica, "Borrado", "S"));
		}

		long fin = System.currentTimeMillis();
		logger.trace("FIN::EliminarDireccionElectronica.obtenerRegistrosCambioEliminarDireccionElectronica",
				fin - inicio);
		return registrosCambio;
	}
	
    private List<SocioNegocio> calcularCiudadDivisionPais(Connection conn, List<SocioNegocio> socioNegocios) throws SQLException {
		logger.debug("INICIO::CrearClienteExpress.calcularCiudadDivisionPais");
		Long inicio = System.currentTimeMillis();

		List<Long> idComunas = socioNegocios.parallelStream()
				.filter(socioNegocio -> socioNegocio.IdComuna > 0L).map(socioNegocio -> socioNegocio.IdComuna)
				.collect(Collectors.toList());

		SQL sql = new SQL();
		HashMap<String, Object> parametros = new HashMap<>();
		parametros.put("IdComunas", idComunas);

		Map<Long, Long> idCiudades = sql.query(conn, SQL_CONSULTAR_DIVISION_PAIS, parametros, DivisionPais.class).parallelStream()
				.collect(Collectors.toMap(c -> c.IdComuna, c -> c.IdCiudad));

		socioNegocios.parallelStream().forEach(direccion -> {
			direccion.IdCiudad = direccion.IdCiudad.equals(0L) ? idCiudades.get(direccion.IdComuna): direccion.IdCiudad;
		});

		Long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearClienteExpress.calcularCiudadDivisionPais", fin - inicio);
		return socioNegocios;
	}

	public static void main(String[] args) {
		System.out.println("== INICIO PRUEBA LOCAL ==");

		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
					.serializeNulls().create();

			String jsonCL = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1,\"p_ClaveUsuario\":\"\",\"p_SocioNegocio\":[{\"IdEntidad\":0,\"IdTipoPersona\":\"N\",\"TipoDocumento\":\"R\",\"NumeroDocumento\":\"7000464-K\",\"Nombres\":\"CLIENTE_CAMBIADO_FINAL\",\"ApellidoPaterno\":\"PRUEBA\",\"ApellidoMaterno\":\"DESARROLLO\",\"IdGenero\":\"M\",\"IdEstadoCivil\":\"SO\",\"IdRegimenConyugal\":\"\",\"RazonSocial\":\"\",\"IdPaisResidenciaCasaMatriz\":0,\"IdPaisNacimientoConstitucion\":0,\"FechaNacimientoConstitucion\":\"\",\"IdCiudad\":2618,\"Calle\":\"CALLE_CLIENTE_FINAL\",\"Numero\":\"3405\",\"Complemento\":\"COMPLEMENTO_CLIENTE_FINAL\",\"TipoTelefono\":\"MOVIL\",\"Telefono\":\"+56911111112\",\"Email\":\"CLIENTE_FINAL@GMAIL.COM\",\"CategoriaInversionista\":\"ARRIESGADO\",\"PerfilComercial\":\"PROJO\",\"TipoCliente\":\"RETAIL\",\"TipoClienteCMF\":0,\"AreaCliente\":\"\",\"Ejecutivo\":0,\"IdPersonaConyuge\":0,\"ConyugeTipoDocumentoIdentidad\":\"R\",\"ConyugeDocumentoIdentidad\":\"\",\"ConyugeNombres\":\"\",\"ConyugeApellidoPaterno\":\"\",\"ConyugeApellidoMaterno\":\"\",\"IdTipoCanalIngresoCorredor\":\"APP\",\"IdSituacionLaboral\":\"\"}]}";

			EntradaCrearClienteExpress Objin = gson.fromJson(jsonCL, EntradaCrearClienteExpress.class);
			SocioNegocio p_SocioNegocio = new SocioNegocio();
			CrearClienteExpress demo = new CrearClienteExpress();
			List<SocioNegocio> listaSoc = new ArrayList<SocioNegocio>();
			
			p_SocioNegocio.IdTipoPersona= "N";
			p_SocioNegocio.TipoDocumento= "R";
			p_SocioNegocio.NumeroDocumento= "7090927-8";
			p_SocioNegocio.Nombres= "BOB";
			p_SocioNegocio.ApellidoPaterno= "PATINO";
			p_SocioNegocio.ApellidoMaterno= "ROSALES";
			p_SocioNegocio.RazonSocial= "";
			p_SocioNegocio.IdGenero= "M";
			p_SocioNegocio.Calle= "asdasd";
			p_SocioNegocio.Numero= "312";
			p_SocioNegocio.Complemento= "asdasd";
			p_SocioNegocio.TipoTelefono= "FIJO";
			p_SocioNegocio.Telefono= "+56321645789";
			p_SocioNegocio.Email= "asdasd@asdasd.casd";
			p_SocioNegocio.IdCiudad= 122L;
			p_SocioNegocio.IdPaisNacimientoConstitucion= 44L;
			p_SocioNegocio.FechaNacimientoConstitucion= "1995-01-01";
			p_SocioNegocio.IdPaisResidenciaCasaMatriz= 44L;
			p_SocioNegocio.IdEstadoCivil= "CA";
			p_SocioNegocio.IdRegimenConyugal= "SC";
			p_SocioNegocio.IdPersonaConyuge= 0L;
			p_SocioNegocio.ConyugeTipoDocumentoIdentidad= "R";
			p_SocioNegocio.ConyugeDocumentoIdentidad= "7399415-2";
			p_SocioNegocio.ConyugeNombres= "CONYUGE EXPRESS";
			p_SocioNegocio.ConyugeApellidoPaterno= "AAPP";
			p_SocioNegocio.ConyugeApellidoMaterno= "AAMM";
			p_SocioNegocio.IdSituacionLaboral= "OTRO";
			p_SocioNegocio.CategoriaInversionista= "ARRIESGADO";
			p_SocioNegocio.PerfilComercial= "MODELO_PROJO";
			p_SocioNegocio.TipoCliente= "SOLOSPOT";
			p_SocioNegocio.TipoClienteCMF= 36L;
			p_SocioNegocio.AreaCliente= "POR DEFINIR";
			p_SocioNegocio.Ejecutivo= 395L;
			p_SocioNegocio.IdTipoCanalIngresoCorredor= "WBS";
			
			listaSoc.add(p_SocioNegocio);
			
			Objin.p_IdEmpresa=1L;
			Objin.p_IdUsuario=21L;
			Objin.p_ClaveUsuario="";
			Objin.p_SocioNegocio= new ArrayList<>();
			Objin.p_SocioNegocio.addAll(listaSoc);
			

			try {
				SalidaCrearClienteExpress ObjOut = demo.logica(con, Objin);
				System.out.println(ObjOut);

				if (ObjOut.P_OUT_ERRORESCONTADOR > 0) {
					con.rollback();
				} else {
					con.commit();
//					con.rollback();
				}

			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		System.out.println("== FIN PRUEBA LOCAL ==");
	}
}