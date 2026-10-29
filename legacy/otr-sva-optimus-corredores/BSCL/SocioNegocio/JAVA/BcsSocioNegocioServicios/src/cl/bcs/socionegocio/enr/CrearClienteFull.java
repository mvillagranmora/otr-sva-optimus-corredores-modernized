package cl.bcs.socionegocio.enr;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.socionegocio.enr.pojo.Indicador;
import cl.bcs.socionegocio.pojo.RequisitoEnrolamientoFull;
import cl.bcs.socionegocio.soc.pojo.Cliente.DatosGenerales;
import cl.bcs.utils.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.enr.AdministrarEnrolamientoCliente.SocioNegocio;
import cl.bcs.socionegocio.enr.ConsultarBorradorCliente.EntradaConsultarBorradorCliente;
import cl.bcs.socionegocio.enr.ConsultarBorradorCliente.SalidaConsultarBorradorCliente;
import cl.bcs.socionegocio.enr.pojo.DatosClienteDatosGenerales;
import cl.bcs.socionegocio.enr.pojo.ResultadoAdministrarBorradorEntidad;
import cl.bcs.socionegocio.enr.pojo.Validacion;
import cl.bcs.socionegocio.soc.SocioNegocioUtils;
import cl.bcs.socionegocio.soc.ValidarRequisitosEnrolamientoFull;
import cl.bcs.socionegocio.soc.ValidarRequisitosEnrolamientoFull.EntradaValidarRequisitosEnrolamientoFull;
import cl.bcs.socionegocio.soc.ValidarRequisitosEnrolamientoFull.SalidaValidarRequisitosEnrolamientoFull;
import cl.bcs.socionegocio.soc.pojo.Cliente;
import cl.bcs.socionegocio.soc.pojo.Portafolio;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearClienteFull implements JavaCall {

	static final Logger logger = LogManager.getLogger(CrearClienteFull.class);
	
	private final String SQL_UPDATE_LOG_GENERAR_CONTRATOS  = loadResourceToString("cl/bcs/socionegocio/enr/sql/CrearClienteFull_UpdateTablaLogGenerarContratos.sql");

	public static class EntradaCrearClienteFull extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_IdTipoCanalIngreso;
		public List<BorradorEnrolamiento> p_Enrolamientos;
		public String p_FirmaAutomaticaDocumento;

		public EntradaCrearClienteFull() {
			this.p_Enrolamientos = new ArrayList<>();
			this.p_FirmaAutomaticaDocumento = "N";
		}
	}

	public static class BorradorEnrolamiento extends BcsPojo{
		public Long IdInterno;
		public Long IdEnrolamiento;
	}

	public static class SalidaCrearClienteFull extends BcsPojo{
		public Long	P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public List<ResultadoAdministrarBorradorEntidad> P_OUT_REFID;
		public List<RequisitoEnrolamientoFull> P_OUT_REQUISITOS_ENR_FULL;

		public SalidaCrearClienteFull() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<>();
			this.P_OUT_REFID = new ArrayList<>();
			this.P_OUT_REQUISITOS_ENR_FULL = new ArrayList<>();
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
		EntradaCrearClienteFull jin = gson.fromJson(in.toString(), EntradaCrearClienteFull.class);
		SalidaCrearClienteFull out = logica(conn,jin);

		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public SalidaCrearClienteFull logica(Connection con, EntradaCrearClienteFull entrada) throws Exception {
		logger.info("INICIO::CrearClienteFull");
		long inicioServicio = System.currentTimeMillis();

		SalidaCrearClienteFull out = new SalidaCrearClienteFull();
		
		//Validacion Maximo Codigo Portafolio
		List<SocioNegocioUtils.ConfiguracionPortafolioDefecto> configuracionPortafolios = SocioNegocioUtils.obtenerConfiguracionPortafolioDefecto(con);
		
		if (configuracionPortafolios.size() > 0) {
			String maximoCodigoPortafolio = "";
			
			try {
				maximoCodigoPortafolio = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "SOCIODENEGOCIO", "POR.MaximoCodigoPortafolio");
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
					MensajeSalida errorValidacion = new MensajeSalida(0L, "ERROR", codigoError, Diccionario.getMensaje(con,entrada.p_IdEmpresa, codigoError).msg());
					out.P_OUT_ERRORESCONTADOR++;
					out.P_OUT_MENSAJES.add(errorValidacion);
					return out;
				}
			}
		}

		SalidaConsultarBorradorCliente salidaConsultarBorradorCliente = obtenerBorradoresCliente(con, entrada);
		List<Validacion> validacionesCamposInmutablesPersona = validarCamposInmutablesPersona(con, entrada.p_IdEmpresa, salidaConsultarBorradorCliente.P_OUT_BORRADORES_CLIENTE);
		out.agregarValidacionesSalida(validacionesCamposInmutablesPersona);

		out.P_OUT_ERRORESCONTADOR += salidaConsultarBorradorCliente.P_OUT_ERRORESCONTADOR;
		out.P_OUT_MENSAJES.addAll(salidaConsultarBorradorCliente.P_OUT_MENSAJES);

		if(out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}

		List<DatosClienteDatosGenerales> borradoresCliente = salidaConsultarBorradorCliente.P_OUT_BORRADORES_CLIENTE;


		ValidarRequisitosEnrolamientoFull validarRequisitosEnrolamientoFull = new ValidarRequisitosEnrolamientoFull();
		EntradaValidarRequisitosEnrolamientoFull entradaValidarFormularioSocioNegocio = new EntradaValidarRequisitosEnrolamientoFull();
		entradaValidarFormularioSocioNegocio.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaValidarFormularioSocioNegocio.p_IdUsuario = entrada.p_IdUsuario;
		entradaValidarFormularioSocioNegocio.p_IdModulo = entrada.p_IdModulo;
		entradaValidarFormularioSocioNegocio.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
		entradaValidarFormularioSocioNegocio.p_IdTipoCanalIngreso = entrada.p_IdTipoCanalIngreso;
		entradaValidarFormularioSocioNegocio.clientes = obtenerClientesValidacion(borradoresCliente);

		SalidaValidarRequisitosEnrolamientoFull salidaValidarRequisitosEnrolamientoFull = validarRequisitosEnrolamientoFull.logica(con, entradaValidarFormularioSocioNegocio);
		out.P_OUT_ERRORESCONTADOR += salidaValidarRequisitosEnrolamientoFull.P_OUT_ERRORESCONTADOR;

		if (out.P_OUT_ERRORESCONTADOR > 0) {
		    out.P_OUT_REQUISITOS_ENR_FULL.addAll(salidaValidarRequisitosEnrolamientoFull.P_OUT_REQUISITOS_ENR_FULL);
		    out.agregarValidacionesSalida(salidaValidarRequisitosEnrolamientoFull.P_OUT_VALIDACIONES.stream().map(validacion -> {
		    	Validacion mapped = new Validacion();
		    	mapped.MensajeError = validacion.MensajeError;
		    	mapped.EsError = validacion.EsError;

		    	return mapped;
			}).collect(Collectors.toList()));

		    return out;
		}

		AdministrarEnrolamientoPersona administrarEnrolamientoPersona = new AdministrarEnrolamientoPersona();
		borradoresCliente = administrarEnrolamientoPersona.enrolarPersona(con, entrada, borradoresCliente);

		AdministrarEnrolamientoDocumentoIdentidad administrarEnrolamientoDocumentoIdentidad = new AdministrarEnrolamientoDocumentoIdentidad();
		borradoresCliente = administrarEnrolamientoDocumentoIdentidad.enrolarDocumentoIdentidad(con, entrada, borradoresCliente);


		AdministrarEnrolamientoCuentaBancaria administrarEnrolamientoCuentaBancaria = new AdministrarEnrolamientoCuentaBancaria();
		borradoresCliente = administrarEnrolamientoCuentaBancaria.enrolarCuentaBancaria(con, entrada, borradoresCliente);
		borradoresCliente = administrarEnrolamientoCuentaBancaria.actualizarCuentaBancariaPrincipalPredeterminado(con, entrada, borradoresCliente);

		AdministrarEnrolamientTarjetaBancaria administrarEnrolamientTarjetaBancaria = new AdministrarEnrolamientTarjetaBancaria();
		borradoresCliente = administrarEnrolamientTarjetaBancaria.enrolarTarjetaBancaria(con, entrada, borradoresCliente);

		AdministrarEnrolamientoRelacionPersona administrarEnrolamientoRelacionPersona = new AdministrarEnrolamientoRelacionPersona();
		borradoresCliente = administrarEnrolamientoRelacionPersona.enrolarRelacionPersona(con, entrada, borradoresCliente);

		AdministrarEnrolamientoGrupoDireccionElectronica administrarEnrolamientoGrupoDireccionElectronica = new AdministrarEnrolamientoGrupoDireccionElectronica();
		borradoresCliente = administrarEnrolamientoGrupoDireccionElectronica.enrolarGrupoDireccion(con, entrada, borradoresCliente);

		AdministrarEnrolamientoIndicador administrarEnrolamientoIndicador = new AdministrarEnrolamientoIndicador();
		borradoresCliente = administrarEnrolamientoIndicador.modificarIndicadoresPersona(con, entrada, borradoresCliente);

		AdministrarEnrolamientoFatca administrarEnrolamientoFatca = new AdministrarEnrolamientoFatca();
		borradoresCliente = administrarEnrolamientoFatca.modificarFatcas(con, entrada, borradoresCliente);
		borradoresCliente = administrarEnrolamientoFatca.crearPaisResidenciaFatca(con, entrada, borradoresCliente);

		AdministrarEnrolamientoCliente administrarEnrolamientoCliente = new AdministrarEnrolamientoCliente();
		borradoresCliente = administrarEnrolamientoCliente.crearCliente(con, entrada, borradoresCliente);
		borradoresCliente = administrarEnrolamientoIndicador.modificarIndicadoresCliente(con, entrada, borradoresCliente);

		AdministrarEnrolamientoPortafolio administrarEnrolamientoPortafolio = new AdministrarEnrolamientoPortafolio();
		borradoresCliente = administrarEnrolamientoPortafolio.crearPortafolio(con, entrada, borradoresCliente);
		List<MensajeSalida> mensajes = new ArrayList<MensajeSalida>();
		
		
		// El enrolamiento de direcion fisica, electronica y telefono DEBE IR DESPUES de Enrolamiento cliente ya que necesita el IdCliente para predeterminar estos item
		AdministrarEnrolamientoDireccionFisica administrarEnrolamientoDireccionFisica = new AdministrarEnrolamientoDireccionFisica();
		borradoresCliente = administrarEnrolamientoDireccionFisica.enrolarDireccionFisica(con, entrada, borradoresCliente);
			
		AdministrarEnrolamientoDireccionElectronica administrarEnrolamientoDireccionElectronica = new AdministrarEnrolamientoDireccionElectronica();
		borradoresCliente = administrarEnrolamientoDireccionElectronica.enrolarDireccionElectronica(con, entrada, borradoresCliente);
			
		AdministrarEnrolamientoTelefono administrarEnrolamientoTelefono = new AdministrarEnrolamientoTelefono();
		borradoresCliente = administrarEnrolamientoTelefono.enrolarTelefono(con, entrada, borradoresCliente);
		
		borradoresCliente = administrarEnrolamientoDireccionFisica.updateDireccionFisicaPredeterminada(con, entrada, borradoresCliente);
		borradoresCliente = administrarEnrolamientoTelefono.updateTerlefonoPredeterminado(con, entrada, borradoresCliente);
		
		
		borradoresCliente.stream().forEach(borrador -> { 
			mensajes.addAll(
					borrador.MensajesSalida.stream().filter(msj -> msj.tipo.equals("ERROR")).collect(Collectors.toList())
			);
		});
		
		if(mensajes.size() != 0) {
			out = obtenerSalida(borradoresCliente);
			return out;
		}
		
		borradoresCliente = administrarEnrolamientoIndicador.modificarIndicadoresPortafolio(con, entrada, borradoresCliente);


		AdministrarEnrolamientoCuentaCustodio administrarEnrolamientoCuentaCustodio = new AdministrarEnrolamientoCuentaCustodio();
		borradoresCliente = administrarEnrolamientoCuentaCustodio.crearCuentaCustodio(con, entrada, borradoresCliente);

		AdministrarEnrolamientoPreferenciaPago administrarEnrolamientoPreferenciaPago = new AdministrarEnrolamientoPreferenciaPago();
		borradoresCliente = administrarEnrolamientoPreferenciaPago.crearPreferenciaPago(con, entrada, borradoresCliente);

		AdministrarEnrolamientoDeclaracionOrigenFondos  administrarEnrolamientoDeclaracionOrigenFondos = new AdministrarEnrolamientoDeclaracionOrigenFondos();
		borradoresCliente = administrarEnrolamientoDeclaracionOrigenFondos.crearDeclaracionOrigenFondos(con, entrada, borradoresCliente);
		
		AdministrarEnrolamientoNombreFantasia  administrarEnrolamientoNombreFantasia = new AdministrarEnrolamientoNombreFantasia();
		borradoresCliente = administrarEnrolamientoNombreFantasia.crearNombreFantasia(con, entrada, borradoresCliente);
		
		actualizarTablaLogGenerarContratos(con, borradoresCliente);
		
		out = obtenerSalida(borradoresCliente);

		long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearClienteFull", finServicio - inicioServicio);

		return out;
	}
	private void actualizarTablaLogGenerarContratos(Connection con, List<DatosClienteDatosGenerales> borradoresCliente) throws SQLException {
		SQL sql = new SQL();
		sql.batch(con, SQL_UPDATE_LOG_GENERAR_CONTRATOS, borradoresCliente, DatosClienteDatosGenerales.class);
	}

	private List<Validacion> validarCamposInmutablesPersona(Connection con, Long idEmpresa, List<DatosClienteDatosGenerales> borradores) {
		logger.debug("INICIO::CrearClienteFull.validarCamposInmutablesPersona");
		long inicio = System.currentTimeMillis();

		Predicate<DatosClienteDatosGenerales> modificaTipoPersona = borrador -> "N".equals(borrador.EsPersonaNueva) && (Objects.equals(borrador.Enrolamiento.TipoPersonaEnrolado, borrador.Enrolamiento.TipoPersonaBorrador) == false);
		List<Validacion> validaciones = borradores.stream()
				.filter(modificaTipoPersona)
				.map(borrador -> {
					Validacion validacion = new Validacion();
					validacion.EsError = true;

					MensajeSalida mensajeSalida = new MensajeSalida();
					mensajeSalida.id = borrador.IdInterno;
					mensajeSalida.tipo = MensajeSalida.MENSAJE_ERROR;
					mensajeSalida.codigo = "TIPO_PERSONA_INMUTABLE";
					try {
						mensajeSalida.descripcion = Diccionario.getMensaje(con, idEmpresa, mensajeSalida.codigo, new HashMap<>(0));
					} catch (Exception e) {
						mensajeSalida.descripcion = mensajeSalida.codigo + " sin traduccion";
					}
					validacion.MensajeError = mensajeSalida;

					return validacion;
				})
				.collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearClienteFull.validarCamposInmutablesPersona {}", fin - inicio);
		
		return validaciones;
	}

	private List<Cliente> obtenerClientesValidacion(List<DatosClienteDatosGenerales> borradoresCliente) {
	    return borradoresCliente.stream()
		.map(borrador -> {
		    DatosGenerales datosGenerales = new DatosGenerales();
			datosGenerales.IdCliente = null;
			datosGenerales.IdPersona = null;
			datosGenerales.TipoEnrolamiento = borrador.Enrolamiento.TipoEnrolamiento;
			datosGenerales.TipoPersona = borrador.Enrolamiento.Tipo;

		    //datos generales persona
		    datosGenerales.Nombres = borrador.Enrolamiento.Nombres;
		    datosGenerales.ApellidoPaterno = borrador.Enrolamiento.ApellidoPaterno;
		    datosGenerales.ApellidoMaterno = borrador.Enrolamiento.ApellidoMaterno;
		    datosGenerales.IdTransparenciaFiscal = borrador.Enrolamiento.IdTransparenciaFiscal;
		    datosGenerales.Sexo = borrador.Enrolamiento.Sexo;
		    datosGenerales.FechaNacimiento = borrador.Enrolamiento.FechaNacimiento;
		    datosGenerales.FechaDefuncion = null;
		    datosGenerales.EstadoCivil = borrador.Enrolamiento.EstadoCivil;
		    datosGenerales.RegimenConyugal = borrador.Enrolamiento.RegimenConyugal;
		    datosGenerales.Cargo = borrador.Enrolamiento.CodigoTipoCargoEmpleado;
		    datosGenerales.CodigoTipoCargoEmpleado = borrador.Enrolamiento.CodigoTipoCargoEmpleado;
		    datosGenerales.DetalleCargo = borrador.Enrolamiento.DetalleCargo;
		    datosGenerales.RazonSocial = borrador.Enrolamiento.RazonSocial;
		    datosGenerales.ResidenciaTributariaExtranjera = borrador.Enrolamiento.ResidenciaTributariaExtranjera;
		    datosGenerales.IngresoPromedioMensual = borrador.Enrolamiento.IngresoPromedioMensual;
		    datosGenerales.GlobalLegalEntity = borrador.Enrolamiento.GlobalLegalEntity;
		    datosGenerales.FechaInicioActividad = borrador.Enrolamiento.FechaInicioActividad;
		    datosGenerales.IdPaisNacimiento = borrador.Enrolamiento.IdPaisNacimiento;
		    datosGenerales.IdPaisResidencia = borrador.Enrolamiento.IdPaisResidencia;
		    datosGenerales.IdPersonaConyuge = borrador.Enrolamiento.IdPersonaConyuge;
		    datosGenerales.IdPersonaEmpleador = borrador.Enrolamiento.IdPersonaEmpleador;
		    datosGenerales.IdProfesion = borrador.Enrolamiento.IdProfesion;
		    datosGenerales.IdPaisNacionalidadPrincipal = borrador.Enrolamiento.IdPaisNacionalidadPrincipal;
		    datosGenerales.IdRangoPatrimonio = borrador.Enrolamiento.IdRangoPatrimonio;
		    datosGenerales.IdGiroPrincipal = borrador.Enrolamiento.IdGiroPrincipal;
		    datosGenerales.IdPersonaRepresentante = borrador.Enrolamiento.IdPersonaRepresentante;
		    datosGenerales.ResidenciaTributariaExtranjera = borrador.Enrolamiento.ResidenciaTributariaExtranjera;
		    datosGenerales.IdNombreFantasiaPrincipal = null;
		    datosGenerales.IdTipoCanalIngresoCorredor = borrador.Enrolamiento.IdTipoCanalIngresoCorredor;
			datosGenerales.IdSituacionLaboral = borrador.Enrolamiento.IdSituacionLaboral;
			datosGenerales.CodigoRangoRenta=borrador.Enrolamiento.CodigoRangoRenta;
		    datosGenerales.CodigoRangoNivelVenta=borrador.Enrolamiento.CodigoRangoNivelVenta;
		    //datosgenerales cliente
			datosGenerales.IdTipoCliente = borrador.Enrolamiento.CLI_IdTipoCliente;
			datosGenerales.CodigoAdicionalHolding = borrador.Enrolamiento.CLI_CodigoAdicionalHolding;
			datosGenerales.CodigoEjecutivoExterno = borrador.Enrolamiento.CLI_CodigoEjecutivoExterno;
			datosGenerales.IdCategoriaInversionista = borrador.Enrolamiento.CLI_IdCategoriaInversionista;
			datosGenerales.IdClaseCliente = borrador.Enrolamiento.CLI_IdClaseCliente;
			datosGenerales.IdTipoBloqueo = null;
			datosGenerales.IdEmpleadoEjecutivo = borrador.Enrolamiento.CLI_IdEmpleadoEjecutivo;
			datosGenerales.IdSegmentoCliente = borrador.Enrolamiento.CLI_IdSegmentoCliente;
			datosGenerales.IdSucursalPreferencia = borrador.Enrolamiento.CLI_IdSucursalPreferencia;
			datosGenerales.CLI_AreaCliente = borrador.Enrolamiento.CLI_AreaCliente;
			datosGenerales.CLI_IdTipoCliente = borrador.Enrolamiento.CLI_IdTipoCliente;
			datosGenerales.CLI_CodigoAdicionalHolding = borrador.Enrolamiento.CLI_CodigoAdicionalHolding;
			datosGenerales.CLI_CodigoEjecutivoExterno = borrador.Enrolamiento.CLI_CodigoEjecutivoExterno;
			datosGenerales.CLI_IdCategoriaInversionista = datosGenerales.IdCategoriaInversionista;
			datosGenerales.CLI_IdClaseCliente = borrador.Enrolamiento.CLI_IdClaseCliente;
			datosGenerales.CLI_IdPersonaEjecutivo = null;
			datosGenerales.CLI_IdEmpleadoEjecutivo = borrador.Enrolamiento.CLI_IdEmpleadoEjecutivo;
			datosGenerales.CLI_IdSegmentoCliente = borrador.Enrolamiento.CLI_IdSegmentoCliente;
			datosGenerales.CLI_IdSucursalPreferencia = borrador.Enrolamiento.CLI_IdSucursalPreferencia;
			datosGenerales.FechaBloqueo = null;
			// excepciones
			datosGenerales.EsBorrador = "S";

			Cliente.Fatca fatca = new Cliente.Fatca();
			fatca.FechaFirmaFormulario = borrador.Fatca.FechaFirmaFormulario;
			fatca.IdClasificacionFatca = borrador.Fatca.IdClasificacionFatca;
			fatca.IdClasificacionEmpresaFatca = borrador.Fatca.IdClasificacionEmpresaFatca;

			Map<String, cl.bcs.socionegocio.soc.pojo.Indicador> indicadoresClientePorCodigoTipoIndicador = new HashMap<>(borrador.Indicadores.size());
			Map<String, cl.bcs.socionegocio.soc.pojo.Indicador> indicadoresPersonaPorCodigoTipoIndicador = new HashMap<>(borrador.Indicadores.size());
			Map<String, cl.bcs.socionegocio.soc.pojo.Indicador> indicadoresPortafolioPorCodigoTipoIndicador = new HashMap<>(borrador.Indicadores.size());

			for (Indicador indicador : borrador.Indicadores) {
				cl.bcs.socionegocio.soc.pojo.Indicador socIndicador = new cl.bcs.socionegocio.soc.pojo.Indicador();
				socIndicador.CodigoTipoIndicador = indicador.CodigoTipoIndicador;
				socIndicador.ValorIndicador = indicador.ValorIndicador;
				socIndicador.IdTipoEntidad = indicador.IdTipoEntidad;
				socIndicador.IdEntidad = indicador.IdEnrolamiento;
				socIndicador.IdTipoIndicador = indicador.IdTipoIndicador;
				socIndicador.IdIndicador = indicador.IdIndicador;
				socIndicador.TieneValorAsignado = socIndicador.IdIndicador == null ? "N" : "S";
				socIndicador.IdTipoPersona = indicador.IdTipoPersona;
				socIndicador.ClaseDato = indicador.ClaseDato;
				socIndicador.IdEstado = indicador.IdEstado;

				if ("PERSONA".equals(indicador.IdTipoEntidad)) {
					indicadoresPersonaPorCodigoTipoIndicador.put(indicador.CodigoTipoIndicador, socIndicador);
					continue;
				}
				if ("PORTAFOLIO".equals(indicador.IdTipoEntidad)) {
					indicadoresPortafolioPorCodigoTipoIndicador.put(indicador.CodigoTipoIndicador, socIndicador);
					continue;
				}
				if ("CLIENTE".equals(indicador.IdTipoEntidad)) {
					indicadoresClientePorCodigoTipoIndicador.put(indicador.CodigoTipoIndicador, socIndicador);
				}
			}
			List<Portafolio> portafolio = new ArrayList<Portafolio>();
			for (cl.bcs.socionegocio.enr.pojo.Portafolio portafolioBorrador : borrador.Portafolio) {
				Portafolio p = new Portafolio();
			    p.IdPortafolio = portafolioBorrador.IdPortafolio;
			    p.DatosGenerales = new Portafolio.DatosGenerales();
			    p.CantidadCuentaCustodio = (long) borrador.CuentasCustodio.size();
			    p.CantidadPreferenciasPagoPaga = borrador.PreferenciasPago.stream().filter(u -> "P".equals(u.RecibePagaPortafolio)).count();
			    p.CantidadPreferenciasPagoRecibe = borrador.PreferenciasPago.stream().filter(u -> "R".equals(u.RecibePagaPortafolio)).count();
			    p.IndicadoresPorCodigoTipoIndicador = indicadoresPortafolioPorCodigoTipoIndicador;
			    p.DatosGenerales.IdEmpleadoEjecutivoComercial=borrador.Enrolamiento.POR_IdEmpleadoEjecutivoComercial;
			    portafolio.add(p);
			}
			if (borrador.Portafolio.isEmpty()) {
				Portafolio p = new Portafolio();
				p.DatosGenerales = new Portafolio.DatosGenerales();
				p.DatosGenerales.IdEmpleadoEjecutivoComercial=borrador.Enrolamiento.POR_IdEmpleadoEjecutivoComercial;
				portafolio.add(p);  
			}

		    Cliente cliente = new Cliente();
		    cliente.CantidadCuentasBancarias = Long.valueOf(borrador.CuentasBancaria.size());
			cliente.CantidadTarjetasBancarias = Long.valueOf(borrador.TarjetasBancaria.size());
			cliente.CantidadTelefonos = Long.valueOf(borrador.Telefonos.size());
			cliente.CantidadDireccionesFisicas = Long.valueOf(borrador.DireccionesFisica.size());
			cliente.CantidadDireccionesElectronicas = Long.valueOf(borrador.DireccionesElectronica.size());
			cliente.CantidadRelaciones = Long.valueOf(borrador.Relaciones.size());
			cliente.CantidadNombresFantasia = Long.valueOf(borrador.NombresFantasia.size());
		    cliente.IdEnrolamiento = borrador.Enrolamiento.IdEnrolamiento;
		    cliente.CantidadDocumentosIdentidad = Long.valueOf(borrador.DocumentosIdentidades.size());
		    cliente.IdEnrolamiento = borrador.Enrolamiento.IdEnrolamiento;
		    cliente.DatosGenerales = datosGenerales;
		    cliente.Fatca = fatca;
		    cliente.Portafolios.addAll(portafolio);
		    cliente.IndicadoresClientePorCodigoTipoIndicador = indicadoresClientePorCodigoTipoIndicador;
		    cliente.IndicadoresPersonaPorCodigoTipoIndicador = indicadoresPersonaPorCodigoTipoIndicador;
		    cliente.TieneResidenciaTributariaExtranjera = StringUtils.isNullOrBlank(borrador.Enrolamiento.ResidenciaTributariaExtranjera) ? "N" : borrador.Enrolamiento.ResidenciaTributariaExtranjera;

		    return cliente;
		}).collect(Collectors.toList());
	}

	private SalidaConsultarBorradorCliente obtenerBorradoresCliente(Connection con, EntradaCrearClienteFull entrada) throws Exception{
		logger.trace("INICIO::CrearClienteFull.obtenerBorradoresCliente");
		long inicio = System.currentTimeMillis();

		EntradaConsultarBorradorCliente entradaConsultarBorradorCliente = new EntradaConsultarBorradorCliente();
		entradaConsultarBorradorCliente.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaConsultarBorradorCliente.p_IdUsuario = entrada.p_IdUsuario;
		entradaConsultarBorradorCliente.p_IdModulo = entrada.p_IdModulo;
		entradaConsultarBorradorCliente.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
		entradaConsultarBorradorCliente.p_Enrolamientos = entrada.p_Enrolamientos;

		ConsultarBorradorCliente consultarBorradorCliente = new ConsultarBorradorCliente();
		SalidaConsultarBorradorCliente salidaConsultarBorradorCliente = consultarBorradorCliente.logica(con, entradaConsultarBorradorCliente);

		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearClienteFull.obtenerBorradoresCliente", fin - inicio);
		return salidaConsultarBorradorCliente;
	}

	private SalidaCrearClienteFull obtenerSalida(List<DatosClienteDatosGenerales> borradoresCliente) {
		logger.trace("INICIO::CrearClienteFull.obtenerSalida");
		long inicio = System.currentTimeMillis();
		SalidaCrearClienteFull salida = new SalidaCrearClienteFull();

		for(DatosClienteDatosGenerales borradorCliente : borradoresCliente) {
			salida.P_OUT_MENSAJES.addAll(borradorCliente.MensajesSalida);
			ResultadoAdministrarBorradorEntidad cliente = new ResultadoAdministrarBorradorEntidad();
			cliente.IdInterno = borradorCliente.IdInterno;
			cliente.IdEntidad = borradorCliente.Cliente.IdCliente;
			salida.P_OUT_REFID.add(cliente);
		}

		salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);

		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearClienteFull.obtenerSalida", fin - inicio);
		return salida;
	}

	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"N\",\"p_CodigoFormulario\":\"\",\"p_IdTipoCanalIngreso\":\"OPT\",\"p_Enrolamientos\":[{\"IdInterno\":0,\"IdEnrolamiento\":1633}]}\n";
			EntradaCrearClienteFull entrada = Utils.getGsonBuilder().fromJson(json, EntradaCrearClienteFull.class);
			try {
				SalidaCrearClienteFull salidaCrearClienteFull = new CrearClienteFull().logica(con, entrada);
				if (salidaCrearClienteFull.P_OUT_ERRORESCONTADOR > 0) {
					con.rollback();
				}
				System.out.println(salidaCrearClienteFull);
			} catch (Exception er) {
				er.printStackTrace();
			} finally {
				con.rollback();
			}

    	}
		System.out.println("== END ==");
	}

}
