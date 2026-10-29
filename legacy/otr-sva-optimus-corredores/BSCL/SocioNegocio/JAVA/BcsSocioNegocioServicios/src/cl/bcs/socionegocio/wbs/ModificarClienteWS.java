package cl.bcs.socionegocio.wbs;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.frm.ValidarCamposObligatorios;
import cl.bcs.plataforma.frm.ValidarCamposObligatorios.SalidaValidarCamposObligatorios;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.wbs.ModificarRelacion.EntradaModificarRelacion;
import cl.bcs.socionegocio.wbs.ModificarIndicadores.EntradaModificarIndicador;
import cl.bcs.socionegocio.wbs.ModificarNombreFantasia.EntradaModificarNombreFantasia;
import cl.bcs.socionegocio.wbs.CrearFatcaPaisResidencia.EntradaCrearFatcaPaisResidencia;
import cl.bcs.socionegocio.wbs.CrearNombreFantasia.EntradaCrearNombreFantasia;
import cl.bcs.socionegocio.utils.RegenerarDocumentosCliente;
import cl.bcs.socionegocio.utils.RegenerarDocumentosCliente.EntradaRegenerarDocumentosCliente;
import cl.bcs.socionegocio.utils.RegenerarDocumentosCliente.SalidaRegenerarDocumentosCliente;
import cl.bcs.socionegocio.per.ModificarConyuge;
import cl.bcs.socionegocio.per.ModificarConyuge.Conyuge;
import cl.bcs.socionegocio.per.ModificarConyuge.EntradaConyuge;
import cl.bcs.socionegocio.per.ModificarConyuge.EntradaModificarConyuge;
import cl.bcs.socionegocio.wbs.CrearClienteExpress.Mensaje;
import cl.bcs.socionegocio.wbs.CrearClienteFullWS.EntidadCreada;
import cl.bcs.socionegocio.wbs.CrearClienteFullWS.EntradaCrearClienteFullWS;
import cl.bcs.socionegocio.wbs.CrearClienteFullWS.IdPersonaConsulta;
import cl.bcs.socionegocio.wbs.CrearCuentaBancaria.EntradaCrearCuentaBancaria;
import cl.bcs.socionegocio.wbs.CrearDireccionFisica.EntradaCrearDireccionFisica;
import cl.bcs.socionegocio.wbs.CrearDireccionElectronica.EntradaCrearDireccionElectronica;
import cl.bcs.socionegocio.wbs.CrearDocumentoIdentidad.EntradaCrearDocumentoIdentidad;
import cl.bcs.socionegocio.wbs.CrearRelacion.EntradaCrearRelaciones;
import cl.bcs.socionegocio.wbs.CrearTarjetaBancaria.EntradaCrearTarjetaBancaria;
import cl.bcs.socionegocio.wbs.CrearTelefono.EntradaCrearTelefono;
import cl.bcs.socionegocio.wbs.EliminarCuentaBancaria.EntradaEliminarCuentaBancaria;
import cl.bcs.socionegocio.wbs.EliminarDireccionElectronica.EntradaEliminarDireccionElectronica;
import cl.bcs.socionegocio.wbs.EliminarDireccionFisica.EntradaEliminarDireccionFisica;
import cl.bcs.socionegocio.wbs.EliminarFatcaPaisResidencia.EntradaEliminarFatcaPaisResidencia;
import cl.bcs.socionegocio.wbs.EliminarTarjetaBancaria.EntradaEliminarTarjetaBancaria;
import cl.bcs.socionegocio.wbs.EliminarTelefono.EntradaEliminarTelefono;
import cl.bcs.socionegocio.wbs.ModificarCuentaBancaria.EntradaModificarCuentaBancaria;
import cl.bcs.socionegocio.wbs.ModificarDatosGenerales.EntradaModificarDatosGenerales;
import cl.bcs.socionegocio.wbs.ModificarDatosGeneralesCliente.EntradaModificarDatosGeneralesCliente;
import cl.bcs.socionegocio.wbs.ModificarDeclaracionOrigenFondos.EntradaModificarDeclaracionOrigenFondos;
import cl.bcs.socionegocio.wbs.ModificarDireccionElectronica.EntradaModificarDireccionElectronica;
import cl.bcs.socionegocio.wbs.ModificarDireccionFisica.EntradaModificarDireccionFisica;
import cl.bcs.socionegocio.wbs.ModificarDocumentoIdentidad.EntradaModificarDocumentoIdentidad;
import cl.bcs.socionegocio.wbs.ModificarFatca.EntradaModificarFatca;
import cl.bcs.socionegocio.wbs.ModificarFatcaPaisResidencia.EntradaModificarFatcaPaisResidencia;
import cl.bcs.socionegocio.wbs.ModificarIdEjecutivoComercialWS.EntradaModificarIdEjecutivoComercialWS;
import cl.bcs.socionegocio.wbs.ModificarTarjetaBancaria.EntradaModificarTarjetaBancaria;
import cl.bcs.socionegocio.wbs.ModificarTelefono.EntradaModificarTelefono;
import cl.bcs.socionegocio.wbs.CrearDeclaracionOrigenFondos.EntradaCrearDeclaracionOrigenFondos;
import cl.bcs.socionegocio.wbs.WebServiceUtils.EntidadManipulada;
import cl.bcs.socionegocio.wbs.WebServiceUtils.SalidaWBS;
import cl.bcs.socionegocio.wbs.pojo.CuentaBancaria;
import cl.bcs.socionegocio.wbs.pojo.DatosPersonaDatosGenerales;
import cl.bcs.socionegocio.wbs.pojo.DireccionElectronica;
import cl.bcs.socionegocio.wbs.pojo.DireccionFisica;
import cl.bcs.socionegocio.wbs.pojo.Fatca;
import cl.bcs.socionegocio.wbs.pojo.FatcaPaisResidencia;
import cl.bcs.socionegocio.wbs.pojo.Indicador;
import cl.bcs.socionegocio.wbs.pojo.NombreFantasia;
import cl.bcs.socionegocio.wbs.pojo.Relacion;
import cl.bcs.socionegocio.wbs.pojo.TarjetaBancaria;
import cl.bcs.socionegocio.wbs.pojo.Telefono;
import cl.bcs.socionegocio.wbs.pojo.TipoDocumento;
import cl.bcs.socionegocio.wbs.pojo.DatosClienteDatosGenerales;
import cl.bcs.socionegocio.wbs.pojo.DeclaracionOrigenFondo;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarClienteWS implements JavaCall {
	static final Logger logger = LogManager.getLogger(ModificarClienteWS.class);

	private final String SQL_SELECT_ID_PERSONA 	= loadResourceToString("cl/bcs/socionegocio/wbs/sql/ModificarClienteWS_SelectIdPersona.sql");
	private final String SQL_SELECT_ID_CLIENTE 	= loadResourceToString("cl/bcs/socionegocio/wbs/sql/ModificarClienteWS_SelectIdCliente.sql");
	private final String SQL_SELECT_CONYUGE 	= loadResourceToString("cl/bcs/socionegocio/per/sql/ModificarConyuge_SelectValidaConyuge.sql");
	private final String SQL_SELECT_IDPERSONA_DOCUMENTO_IDENTIDAD 	= loadResourceToString("cl/bcs/socionegocio/wbs/sql/CrearClienteFullWS_SelectIdPersonaPorDocumentoIdentidad.sql");
	private final String SQL_SELECT_CONSULTAR_PORTAFOLIO 			= loadResourceToString("cl/bcs/socionegocio/wbs/sql/ModificarClienteWS_SelectPortafolio.sql");

	private final String SQL_SELECT_VALIDA_INTEGRIDAD_BASE	= loadResourceToString("cl/bcs/socionegocio/wbs/sql/ModificarClienteWS_SelectIdEntidades.sql");

	public static class EntradaModificarClienteWS extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_ClaveUsuario;
		public List<SocioNegocio> p_SocioNegocio;
		public String p_ActualizarIndicadorFormaCobro;

		public EntradaModificarClienteWS() {
			this.p_ActualizarIndicadorFormaCobro = "N";
		}
	}
	public static class ValidacionEntidad extends BcsPojo{
		public SocioNegocio socioNegocio;
		public List<Object> entidadModificar;
		public Long ERRORESCONTADOR;
		public List<EntidadManipulada> ENTIDADES_MANIPULADAS;
		public ValidacionEntidad() {
			this.socioNegocio = new SocioNegocio();
			this.ERRORESCONTADOR = 0L;
			this.ENTIDADES_MANIPULADAS = new ArrayList<EntidadManipulada>(0);
			this.entidadModificar =  new ArrayList<>(0);
		}
	}

	public static class SocioNegocio extends BcsPojo {
		public Long IdInterno;
		public String TipoDocumentoIdentidad; 
		public String DocumentoIdentidad; 
		public List<DatosPersonaDatosGenerales> DatosPersonaDatosGenerales;
		public List<DatosClienteDatosGenerales> DatosClienteDatosGenerales; 
		public List<TipoDocumento> TiposDocumento;
		public List<CuentaBancaria> CuentasBancaria; 
		public List<TarjetaBancaria> TarjetasBancaria; 
		public List<Telefono> Telefonos;
		public List<DireccionFisica> DireccionesFisica;
		public List<DireccionElectronica> DireccionesElectronica;
		public List<Indicador> IndicadoresPersona;
		public List<Indicador> IndicadoresCliente;
		public List<Fatca> FatcaCrs;
		public List<FatcaPaisResidencia> FatcaCrsPaisResidencia;
		public List<Relacion> Relaciones;
		public List<NombreFantasia> NombresFantasia;
		public List<DeclaracionOrigenFondo> DeclaracionOrigenFondo;
		public List<DatosPortafolio> DatosPortafolio;
		


		public SocioNegocio() {
			this.IdInterno = 0L;
			this.TipoDocumentoIdentidad = "";
			this.DocumentoIdentidad = "";
			this.DatosPersonaDatosGenerales = new ArrayList<>();
			this.DatosClienteDatosGenerales = new ArrayList<>();
			this.TiposDocumento = new ArrayList<>();
			this.CuentasBancaria = new ArrayList<>();
			this.TarjetasBancaria = new ArrayList<>();
			this.Telefonos = new ArrayList<>();
			this.DireccionesFisica = new ArrayList<>();
			this.DireccionesElectronica = new ArrayList<>();
			this.IndicadoresPersona = new ArrayList<>();
			this.IndicadoresCliente = new ArrayList<>();
			this.FatcaCrs = new ArrayList<>();
			this.FatcaCrsPaisResidencia = new ArrayList<>();
			this.Relaciones = new ArrayList<>();
			this.NombresFantasia = new ArrayList<>();
			this.DeclaracionOrigenFondo = new ArrayList<>();
			this.DatosPortafolio = new ArrayList<>();
		}

	}

	public static class Id extends BcsPojo {
		public Long Id;
		public String IdTipoCanalIngresoCorredor;
	}
	public static class Entidad extends BcsPojo {
		public Long IdEntidad;
		public Long IdPersona;
		public Long IdCliente;

	}
	
	public static class Portafolio extends BcsPojo {
		
		public Long IdPortafolio;
		public String CodigoPortafolio;
		public String Proposito;
		public Long IdCliente;

	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaModificarClienteWS jin = gson.fromJson(in.toString(), EntradaModificarClienteWS.class);
		SalidaWBS out = logica(conn, jin);

		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public SalidaWBS logica(Connection con, EntradaModificarClienteWS entrada) throws Exception {
		logger.info("INICIO::ModificarDatosGenerales");
		long inicioServicio = System.currentTimeMillis();
		
		String agregaAdvertencia = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "SOCIODENEGOCIO", "SOC.MensajeAdvertenciaRelacionadoExiste");
		String procesoActualizacionContratos = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "SOCIODENEGOCIO", "SOC.ValidarRegenerarDocumentosCliente");
		
		SalidaWBS salida = new SalidaWBS();
		
		SalidaValidarCamposObligatorios validacion = validarEntrada(con, entrada);
		
		if(validacion.P_OUT_ERRORESCONTADOR > 0) {
			salida.P_OUT_ERRORESCONTADOR = validacion.P_OUT_ERRORESCONTADOR;
			EntidadManipulada entidadManipulada = new EntidadManipulada();
			entidadManipulada.IdEntidad = 0L;
			entidadManipulada.TipoEntidad = "CLIENTE";
			entidadManipulada.Mensajes = validacion.P_OUT_MENSAJES;
			salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadManipulada);
			return salida;
		}
		
		for (SocioNegocio socioNegocio : entrada.p_SocioNegocio) {
			Long idPersona = obtenerIdPersonaPorDocumentoIdentidad(con, socioNegocio);
			Long idCliente = obtenerIdClientePorIdPersona(con, idPersona);

			if (!socioNegocio.Relaciones.isEmpty()) {
				List<Relacion> relacionesCrear = socioNegocio.Relaciones.parallelStream()
						.filter(relacion -> "AGREGAR".equals(relacion.Accion) && !"CY".equals(relacion.IdTipoRelacion)).collect(Collectors.toList());
				List<Relacion> relacionesModificar = socioNegocio.Relaciones.parallelStream()
						.filter(relacion -> "MODIFICAR".equals(relacion.Accion) && !"CY".equals(relacion.IdTipoRelacion)).collect(Collectors.toList());
				List<Relacion> modificarConyuge = socioNegocio.Relaciones.parallelStream()
						.filter(relacion -> "CY".equals(relacion.IdTipoRelacion) ).collect(Collectors.toList());
				if (!relacionesCrear.isEmpty()) {
					SalidaWBS salidaCrear = crearRelaciones(con, entrada, idPersona, relacionesCrear);
					salida.P_OUT_ENTIDADES_MANIPULADAS.addAll(salidaCrear.P_OUT_ENTIDADES_MANIPULADAS);
					salida.P_OUT_ERRORESCONTADOR += salidaCrear.P_OUT_ERRORESCONTADOR;
				}
				
				if (!relacionesModificar.isEmpty()) {
					SalidaWBS salidaModificar = modificarRelaciones(con, entrada, idPersona, relacionesModificar);
					salida.P_OUT_ENTIDADES_MANIPULADAS.addAll(salidaModificar.P_OUT_ENTIDADES_MANIPULADAS);
					salida.P_OUT_ERRORESCONTADOR += salidaModificar.P_OUT_ERRORESCONTADOR;
				}
				
				if (!modificarConyuge.isEmpty()) {
					
					
					SQL sql = new SQL();
					HashMap<String, Object> params = new HashMap<String, Object>(2);
					params.put("DocumentosIdentidad", modificarConyuge.get(0).DocumentoIdentidad);
					Long idPersonaExiste = sql.query(con, SQL_SELECT_IDPERSONA_DOCUMENTO_IDENTIDAD, params, IdPersonaConsulta.class).stream().findFirst().orElse(new IdPersonaConsulta()).IdPersona;
					SalidaWBS salidaModificarConyuge = modificarConyuge(con, entrada, modificarConyuge.get(0), idPersona, socioNegocio.DatosPersonaDatosGenerales.get(0),agregaAdvertencia);
					if(idPersonaExiste > 0 && agregaAdvertencia.equalsIgnoreCase("S")) {
						EntidadManipulada entidadManipulada = new EntidadManipulada();
						HashMap<String, String> parametrosMensaje = new HashMap<>(2);
						entidadManipulada.Mensajes.add(new PlataformaUtils.MensajeSalida(idPersonaExiste, PlataformaUtils.MensajeSalida.MENSAJE_ADVERTENCIA, "UI_ADVERTENCIA_PERSONA_RELACIONADA_EXISTE", parametrosMensaje, con, entrada.p_IdEmpresa));
						entidadManipulada.TipoEntidad = "RELACION";
						entidadManipulada.IdEntidad = 0L;
						socioNegocio.DatosPersonaDatosGenerales.get(0).IdPersonaConyuge = idPersonaExiste;
						salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadManipulada);
					}else {
						salida.P_OUT_ENTIDADES_MANIPULADAS.addAll(salidaModificarConyuge.P_OUT_ENTIDADES_MANIPULADAS);
						salida.P_OUT_ERRORESCONTADOR += salidaModificarConyuge.P_OUT_ERRORESCONTADOR;
					}
				}
			}
			
			if (!socioNegocio.DatosPersonaDatosGenerales.isEmpty()) {
				socioNegocio.DatosPersonaDatosGenerales.forEach(DatosPersonaDatosGenerales -> {if ("SO".equals(DatosPersonaDatosGenerales.EstadoCivil)) {
					DatosPersonaDatosGenerales.IdPersonaConyuge = 0L;
					DatosPersonaDatosGenerales.RegimenConyugal = "";}});
				SalidaWBS salidaModificar = modificarDatosGeneralesPersona(con, entrada, idPersona, socioNegocio.DatosPersonaDatosGenerales);
				salida.P_OUT_ENTIDADES_MANIPULADAS.addAll(salidaModificar.P_OUT_ENTIDADES_MANIPULADAS);
				salida.P_OUT_ERRORESCONTADOR += salidaModificar.P_OUT_ERRORESCONTADOR;
				
			}
			
			if (!socioNegocio.DatosClienteDatosGenerales.isEmpty()) {
				SalidaWBS salidaModificar = modificarDatosGeneralesCliente(con, entrada, idPersona, socioNegocio.DatosClienteDatosGenerales);
				salida.P_OUT_ENTIDADES_MANIPULADAS.addAll(salidaModificar.P_OUT_ENTIDADES_MANIPULADAS);
				salida.P_OUT_ERRORESCONTADOR += salidaModificar.P_OUT_ERRORESCONTADOR;
			}
			
			if (!socioNegocio.TiposDocumento.isEmpty()) {
				List<TipoDocumento> tiposDocumentoCrear = socioNegocio.TiposDocumento.parallelStream()
						.filter(tipoDocumento -> "AGREGAR".equals(tipoDocumento.Accion)).collect(Collectors.toList());
				List<TipoDocumento> tiposDocumentoModificar = socioNegocio.TiposDocumento.parallelStream()
						.filter(tipoDocumento -> "MODIFICAR".equals(tipoDocumento.Accion)).collect(Collectors.toList());

				SalidaWBS salidaCrear = crearTiposDocumento(con, entrada, idPersona, tiposDocumentoCrear);
				salida.P_OUT_ENTIDADES_MANIPULADAS.addAll(salidaCrear.P_OUT_ENTIDADES_MANIPULADAS);
				salida.P_OUT_ERRORESCONTADOR += salidaCrear.P_OUT_ERRORESCONTADOR;

				SalidaWBS salidaModificar = modificarTiposDocumento(con, entrada, idPersona, tiposDocumentoModificar);
				salida.P_OUT_ENTIDADES_MANIPULADAS.addAll(salidaModificar.P_OUT_ENTIDADES_MANIPULADAS);
				salida.P_OUT_ERRORESCONTADOR += salidaModificar.P_OUT_ERRORESCONTADOR;
			}

			if (!socioNegocio.CuentasBancaria.isEmpty()) {
				List<CuentaBancaria> cuentasBancariaCrear = socioNegocio.CuentasBancaria.parallelStream()
						.filter(cuenta -> "AGREGAR".equals(cuenta.Accion)).collect(Collectors.toList());
				List<CuentaBancaria> cuentasBancariaModificar = socioNegocio.CuentasBancaria.parallelStream()
						.filter(cuenta -> "MODIFICAR".equals(cuenta.Accion)).collect(Collectors.toList());
				List<CuentaBancaria> cuentasBancariaEliminar = socioNegocio.CuentasBancaria.parallelStream()
						.filter(cuenta -> "ELIMINAR".equals(cuenta.Accion)).collect(Collectors.toList());
				
				if (!cuentasBancariaCrear.isEmpty()) {
					SalidaWBS salidaCrear = crearCuentasBancarias(con, entrada, idPersona, cuentasBancariaCrear);
					salida.P_OUT_ENTIDADES_MANIPULADAS.addAll(salidaCrear.P_OUT_ENTIDADES_MANIPULADAS);
					salida.P_OUT_ERRORESCONTADOR += salidaCrear.P_OUT_ERRORESCONTADOR;
				}

				if (!cuentasBancariaModificar.isEmpty()) {
					SalidaWBS salidaModificar = modificarCuentasBancarias(con, entrada, idPersona, cuentasBancariaModificar);
					salida.P_OUT_ENTIDADES_MANIPULADAS.addAll(salidaModificar.P_OUT_ENTIDADES_MANIPULADAS);
					salida.P_OUT_ERRORESCONTADOR += salidaModificar.P_OUT_ERRORESCONTADOR;
				}
				
				if (!cuentasBancariaEliminar.isEmpty()) {
					SalidaWBS salidaEliminar = eliminarCuentasBancarias(con, entrada, idPersona, cuentasBancariaEliminar);
					salida.P_OUT_ENTIDADES_MANIPULADAS.addAll(salidaEliminar.P_OUT_ENTIDADES_MANIPULADAS);
					salida.P_OUT_ERRORESCONTADOR += salidaEliminar.P_OUT_ERRORESCONTADOR;
				}
			}

			if (!socioNegocio.TarjetasBancaria.isEmpty()) {
				List<TarjetaBancaria> tarjetaBancariaCrear = socioNegocio.TarjetasBancaria.parallelStream()
						.filter(tarjeta -> "AGREGAR".equals(tarjeta.Accion)).collect(Collectors.toList());
				List<TarjetaBancaria> tarjetaBancariaModificar = socioNegocio.TarjetasBancaria.parallelStream()
						.filter(tarjeta -> "MODIFICAR".equals(tarjeta.Accion)).collect(Collectors.toList());
				List<TarjetaBancaria> tarjetaBancariaEliminar = socioNegocio.TarjetasBancaria.parallelStream()
						.filter(tarjeta -> "ELIMINAR".equals(tarjeta.Accion)).collect(Collectors.toList());
				
				if (!tarjetaBancariaCrear.isEmpty()) {
				SalidaWBS salidaCrear = crearTarjetasBancarias(con, entrada, idPersona, tarjetaBancariaCrear);
				salida.P_OUT_ENTIDADES_MANIPULADAS.addAll(salidaCrear.P_OUT_ENTIDADES_MANIPULADAS);
				salida.P_OUT_ERRORESCONTADOR += salidaCrear.P_OUT_ERRORESCONTADOR;
				}
				
				if (!tarjetaBancariaModificar.isEmpty()) {
					SalidaWBS salidaModificar = modificarTarjetasBancarias(con, entrada, idPersona, tarjetaBancariaModificar);
					salida.P_OUT_ENTIDADES_MANIPULADAS.addAll(salidaModificar.P_OUT_ENTIDADES_MANIPULADAS);
					salida.P_OUT_ERRORESCONTADOR += salidaModificar.P_OUT_ERRORESCONTADOR;
				}
				
				if (!tarjetaBancariaEliminar.isEmpty()) {
					SalidaWBS salidaEliminar = eliminarTarjetasBancarias(con, entrada, idPersona, tarjetaBancariaEliminar);
					salida.P_OUT_ENTIDADES_MANIPULADAS.addAll(salidaEliminar.P_OUT_ENTIDADES_MANIPULADAS);
					salida.P_OUT_ERRORESCONTADOR += salidaEliminar.P_OUT_ERRORESCONTADOR;
				}
			}

			if (!socioNegocio.Telefonos.isEmpty()) {
				List<Telefono> telefonosCrear = socioNegocio.Telefonos.parallelStream()
						.filter(telefono -> "AGREGAR".equals(telefono.Accion)).collect(Collectors.toList());
				List<Telefono> telefonosModificar = socioNegocio.Telefonos.parallelStream()
						.filter(telefono -> "MODIFICAR".equals(telefono.Accion)).collect(Collectors.toList());
				List<Telefono> telefonosEliminar = socioNegocio.Telefonos.parallelStream()
						.filter(telefono -> "ELIMINAR".equals(telefono.Accion)).collect(Collectors.toList());

				if (!telefonosCrear.isEmpty()) {
					SalidaWBS salidaCrear = crearTelefonos(con, entrada, idPersona, telefonosCrear);
					salida.P_OUT_ENTIDADES_MANIPULADAS.addAll(salidaCrear.P_OUT_ENTIDADES_MANIPULADAS);
					salida.P_OUT_ERRORESCONTADOR += salidaCrear.P_OUT_ERRORESCONTADOR;
				}

				if (!telefonosModificar.isEmpty()) {
					SalidaWBS salidaModificar = modificarTelefonos(con, entrada, idPersona, telefonosModificar);
					salida.P_OUT_ENTIDADES_MANIPULADAS.addAll(salidaModificar.P_OUT_ENTIDADES_MANIPULADAS);
					salida.P_OUT_ERRORESCONTADOR += salidaModificar.P_OUT_ERRORESCONTADOR;
				}

				if (!telefonosEliminar.isEmpty()) {
					SalidaWBS salidaEliminar = eliminarTelefonos(con, entrada, idPersona, telefonosEliminar);
					salida.P_OUT_ENTIDADES_MANIPULADAS.addAll(salidaEliminar.P_OUT_ENTIDADES_MANIPULADAS);
					salida.P_OUT_ERRORESCONTADOR += salidaEliminar.P_OUT_ERRORESCONTADOR;
				}
			}
			
			if (!socioNegocio.DireccionesFisica.isEmpty()) {
				List<DireccionFisica> direccionesFisicaCrear = socioNegocio.DireccionesFisica.parallelStream()
						.filter(direccion -> "AGREGAR".equals(direccion.Accion)).collect(Collectors.toList());
				List<DireccionFisica> direccionesFisicaModificar = socioNegocio.DireccionesFisica.parallelStream()
						.filter(direccion -> "MODIFICAR".equals(direccion.Accion)).collect(Collectors.toList());
				List<DireccionFisica> direccionesFisicaEliminar = socioNegocio.DireccionesFisica.parallelStream()
						.filter(direccion -> "ELIMINAR".equals(direccion.Accion)).collect(Collectors.toList());
				
				if (!direccionesFisicaCrear.isEmpty()) {
					SalidaWBS salidaCrear = crearDireccionesFisica(con, entrada, idPersona, direccionesFisicaCrear);
					salida.P_OUT_ENTIDADES_MANIPULADAS.addAll(salidaCrear.P_OUT_ENTIDADES_MANIPULADAS);
					salida.P_OUT_ERRORESCONTADOR += salidaCrear.P_OUT_ERRORESCONTADOR;
				}

				if (!direccionesFisicaModificar.isEmpty()) {
					SalidaWBS salidaModificar = modificarDireccionesFisica(con, entrada, idPersona, direccionesFisicaModificar);
					salida.P_OUT_ENTIDADES_MANIPULADAS.addAll(salidaModificar.P_OUT_ENTIDADES_MANIPULADAS);
					salida.P_OUT_ERRORESCONTADOR += salidaModificar.P_OUT_ERRORESCONTADOR;
				}

				if (!direccionesFisicaEliminar.isEmpty()) {
					SalidaWBS salidaEliminar = eliminarDireccionesFisica(con, entrada, idPersona, direccionesFisicaEliminar);
					salida.P_OUT_ENTIDADES_MANIPULADAS.addAll(salidaEliminar.P_OUT_ENTIDADES_MANIPULADAS);
					salida.P_OUT_ERRORESCONTADOR += salidaEliminar.P_OUT_ERRORESCONTADOR;
				}
			}
			
			if (!socioNegocio.DireccionesElectronica.isEmpty()) {
				List<DireccionElectronica> direccionesElectronicaCrear = socioNegocio.DireccionesElectronica.parallelStream()
						.filter(direccion -> "AGREGAR".equals(direccion.Accion)).collect(Collectors.toList());
				List<DireccionElectronica> direccionesElectronicaModificar = socioNegocio.DireccionesElectronica.parallelStream()
						.filter(direccion -> "MODIFICAR".equals(direccion.Accion)).collect(Collectors.toList());
				List<DireccionElectronica> direccionesElectronicaEliminar = socioNegocio.DireccionesElectronica.parallelStream()
						.filter(direccion -> "ELIMINAR".equals(direccion.Accion)).collect(Collectors.toList());
				
				if (!direccionesElectronicaCrear.isEmpty()) {
					SalidaWBS salidaCrear = crearDireccionesElectronica(con, entrada, idPersona, direccionesElectronicaCrear);
					salida.P_OUT_ENTIDADES_MANIPULADAS.addAll(salidaCrear.P_OUT_ENTIDADES_MANIPULADAS);
					salida.P_OUT_ERRORESCONTADOR += salidaCrear.P_OUT_ERRORESCONTADOR;
				}
				
				if (!direccionesElectronicaModificar.isEmpty()) {
					SalidaWBS salidaModificar = modificarDireccionesElectronica(con, entrada, idPersona, direccionesElectronicaModificar);
					salida.P_OUT_ENTIDADES_MANIPULADAS.addAll(salidaModificar.P_OUT_ENTIDADES_MANIPULADAS);
					salida.P_OUT_ERRORESCONTADOR += salidaModificar.P_OUT_ERRORESCONTADOR;
				}

				if (!direccionesElectronicaEliminar.isEmpty()) {
					SalidaWBS salidaEliminar = eliminarDireccionesElectronica(con, entrada, idPersona, direccionesElectronicaEliminar);
					salida.P_OUT_ENTIDADES_MANIPULADAS.addAll(salidaEliminar.P_OUT_ENTIDADES_MANIPULADAS);
					salida.P_OUT_ERRORESCONTADOR += salidaEliminar.P_OUT_ERRORESCONTADOR;
				}
			}
			
			if (!socioNegocio.IndicadoresPersona.isEmpty()) {
				SalidaWBS salidaModificar = modificarIndicadores(con, entrada, idPersona,socioNegocio.IndicadoresPersona,"INDICADORES_PERSONA");
				salida.P_OUT_ENTIDADES_MANIPULADAS.addAll(salidaModificar.P_OUT_ENTIDADES_MANIPULADAS);
				salida.P_OUT_ERRORESCONTADOR += salidaModificar.P_OUT_ERRORESCONTADOR;
			}
			
			if (!socioNegocio.IndicadoresCliente.isEmpty()) {
				SalidaWBS salidaModificar = modificarIndicadores(con, entrada, idPersona,socioNegocio.IndicadoresCliente, "INDICADORES_CLIENTE");
				salida.P_OUT_ENTIDADES_MANIPULADAS.addAll(salidaModificar.P_OUT_ENTIDADES_MANIPULADAS);
				salida.P_OUT_ERRORESCONTADOR += salidaModificar.P_OUT_ERRORESCONTADOR;
			}
			
			if (!socioNegocio.FatcaCrs.isEmpty()) {
				SalidaWBS salidaModificar = modificarFatca(con, entrada, idPersona,socioNegocio.FatcaCrs);
				salida.P_OUT_ENTIDADES_MANIPULADAS.addAll(salidaModificar.P_OUT_ENTIDADES_MANIPULADAS);
				salida.P_OUT_ERRORESCONTADOR += salidaModificar.P_OUT_ERRORESCONTADOR;
			}
			
			if (!socioNegocio.FatcaCrsPaisResidencia.isEmpty()) {
				List<FatcaPaisResidencia> fatcasPaisResidenciaCrear = socioNegocio.FatcaCrsPaisResidencia.parallelStream()
						.filter(fatca -> "AGREGAR".equals(fatca.Accion)).collect(Collectors.toList());
				List<FatcaPaisResidencia> fatcasPaisResidenciaModificar = socioNegocio.FatcaCrsPaisResidencia.parallelStream()
						.filter(fatca -> "MODIFICAR".equals(fatca.Accion)).collect(Collectors.toList());
				List<FatcaPaisResidencia> fatcasPaisResidenciaEliminar = socioNegocio.FatcaCrsPaisResidencia.parallelStream()
						.filter(fatca -> "ELIMINAR".equals(fatca.Accion)).collect(Collectors.toList());
				
				if (!fatcasPaisResidenciaCrear.isEmpty()) {
					SalidaWBS salidaCrear = crearFatcasPaisResidencia(con, entrada, idPersona, fatcasPaisResidenciaCrear);
					salida.P_OUT_ENTIDADES_MANIPULADAS.addAll(salidaCrear.P_OUT_ENTIDADES_MANIPULADAS);
					salida.P_OUT_ERRORESCONTADOR += salidaCrear.P_OUT_ERRORESCONTADOR;
				}
				
				if (!fatcasPaisResidenciaModificar.isEmpty()) {
					SalidaWBS salidaModificar = modificarFatcasPaisResidencia(con, entrada, idPersona, fatcasPaisResidenciaModificar);
					salida.P_OUT_ENTIDADES_MANIPULADAS.addAll(salidaModificar.P_OUT_ENTIDADES_MANIPULADAS);
					salida.P_OUT_ERRORESCONTADOR += salidaModificar.P_OUT_ERRORESCONTADOR;
				}

				if (!fatcasPaisResidenciaEliminar.isEmpty()) {
					SalidaWBS salidaEliminar = eliminarFatcasPaisResidencia(con, entrada, idPersona, fatcasPaisResidenciaEliminar);
					salida.P_OUT_ENTIDADES_MANIPULADAS.addAll(salidaEliminar.P_OUT_ENTIDADES_MANIPULADAS);
					salida.P_OUT_ERRORESCONTADOR += salidaEliminar.P_OUT_ERRORESCONTADOR;
				}
			}
			
			if (!socioNegocio.NombresFantasia.isEmpty()) {
				List<NombreFantasia> nombresFantasiaCrear = socioNegocio.NombresFantasia.parallelStream()
						.filter(relacion -> "AGREGAR".equals(relacion.Accion)).collect(Collectors.toList());
				List<NombreFantasia> nombresFantasiaModificar = socioNegocio.NombresFantasia.parallelStream()
						.filter(relacion -> "MODIFICAR".equals(relacion.Accion)).collect(Collectors.toList());
				
				if (!nombresFantasiaCrear.isEmpty()) {
					SalidaWBS salidaCrear = crearNombresFantasia(con, entrada, idPersona, nombresFantasiaCrear);
					salida.P_OUT_ENTIDADES_MANIPULADAS.addAll(salidaCrear.P_OUT_ENTIDADES_MANIPULADAS);
					salida.P_OUT_ERRORESCONTADOR += salidaCrear.P_OUT_ERRORESCONTADOR;
				}
				
				if (!nombresFantasiaModificar.isEmpty()) {
					SalidaWBS salidaModificar = modificarNombresFantasia(con, entrada, idPersona, nombresFantasiaModificar);
					salida.P_OUT_ENTIDADES_MANIPULADAS.addAll(salidaModificar.P_OUT_ENTIDADES_MANIPULADAS);
					salida.P_OUT_ERRORESCONTADOR += salidaModificar.P_OUT_ERRORESCONTADOR;
				}
			}
			
			if (!socioNegocio.DeclaracionOrigenFondo.isEmpty()) {
				List<DeclaracionOrigenFondo> dofCrear = socioNegocio.DeclaracionOrigenFondo.parallelStream()
						.filter(dof -> "AGREGAR".equals(dof.Accion)).collect(Collectors.toList());
				List<DeclaracionOrigenFondo> dofModificar = socioNegocio.DeclaracionOrigenFondo.parallelStream()
						.filter(dof -> "MODIFICAR".equals(dof.Accion)).collect(Collectors.toList());
				
				if (!dofCrear.isEmpty()) {
					SalidaWBS salidaCrear = crearDeclaracionOrigenFondo(con, entrada, idPersona, idCliente, dofCrear);
					salida.P_OUT_ENTIDADES_MANIPULADAS.addAll(salidaCrear.P_OUT_ENTIDADES_MANIPULADAS);
					salida.P_OUT_ERRORESCONTADOR += salidaCrear.P_OUT_ERRORESCONTADOR;
				}
				
				if (!dofModificar.isEmpty()) {
					SalidaWBS salidaModificar = modificarDeclaracionOrigenFondo(con, entrada, idPersona, idCliente, dofModificar);
					salida.P_OUT_ENTIDADES_MANIPULADAS.addAll(salidaModificar.P_OUT_ENTIDADES_MANIPULADAS);
					salida.P_OUT_ERRORESCONTADOR += salidaModificar.P_OUT_ERRORESCONTADOR;
				}
			}
			if (!socioNegocio.DatosPortafolio.isEmpty() ) {
				for(DatosPortafolio datosPortafolio : socioNegocio.DatosPortafolio) {
					if(!datosPortafolio.Indicadores.isEmpty() && Objects.nonNull(idCliente) && Objects.nonNull(idPersona)) {
						datosPortafolio.Indicadores.forEach(ind -> ind.IdPortafolio = datosPortafolio.IdPortafolio);
						SalidaWBS salidaModificar = modificarIndicadores(con, entrada, idPersona,datosPortafolio.Indicadores, "INDICADORES_PORTAFOLIO");
						salida.P_OUT_ENTIDADES_MANIPULADAS.addAll(salidaModificar.P_OUT_ENTIDADES_MANIPULADAS);
						salida.P_OUT_ERRORESCONTADOR += salidaModificar.P_OUT_ERRORESCONTADOR;
					}
					
					if(datosPortafolio.IdEjecutivoComercial!= null && datosPortafolio.IdEjecutivoComercial > 0L ){
						SalidaWBS salidaModificar = modificarIdEjecutivoComercial(con, entrada, idPersona,datosPortafolio);			
						salida.P_OUT_ENTIDADES_MANIPULADAS.addAll(salidaModificar.P_OUT_ENTIDADES_MANIPULADAS);
						salida.P_OUT_ERRORESCONTADOR += salidaModificar.P_OUT_ERRORESCONTADOR;
					}
				}		
			}
		}

		if (salida.P_OUT_ERRORESCONTADOR == 0 &&  "S".equalsIgnoreCase(procesoActualizacionContratos)) {
			for (SocioNegocio socioNegocio : entrada.p_SocioNegocio) {
				Long idPersona = obtenerIdPersonaPorDocumentoIdentidad(con, socioNegocio);
				Long idCliente = obtenerIdClientePorIdPersona(con, idPersona);
				String idTipoCanalIngresoCorredor = obtenerIdTipoCanalIngresoCorredorPorDocumentoIdentidad(con, socioNegocio);
				
				EntradaRegenerarDocumentosCliente entradaRegenerarDocumentosCliente= new EntradaRegenerarDocumentosCliente();
				entradaRegenerarDocumentosCliente.p_IdEmpresa = entrada.p_IdEmpresa;
				entradaRegenerarDocumentosCliente.p_IdModulo = "SOC";
				entradaRegenerarDocumentosCliente.p_IdUsuario = entrada.p_IdUsuario;
				entradaRegenerarDocumentosCliente.p_IdCliente = idCliente;
				entradaRegenerarDocumentosCliente.p_IdTipoCanalIngresoCorredor = idTipoCanalIngresoCorredor;
				
				RegenerarDocumentosCliente regenerarDocumentosCliente = new RegenerarDocumentosCliente();
				SalidaRegenerarDocumentosCliente salidaRegenerarDocumentosCliente = regenerarDocumentosCliente.logica(con, entradaRegenerarDocumentosCliente);
				
			}	
		}
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarDatosGenerales", finServicio - inicioServicio);
		return salida;
	}

	private SalidaValidarCamposObligatorios validarEntrada(Connection con, EntradaModificarClienteWS entrada) throws Exception {
		ValidarCamposObligatorios validarCamposObligatorios = new ValidarCamposObligatorios();
		
		List<DatosPersonaDatosGenerales> datosValidar = entrada.p_SocioNegocio.stream().flatMap(data -> data.DatosPersonaDatosGenerales.stream()).collect(Collectors.toList());
		
		SalidaValidarCamposObligatorios resultado = validarCamposObligatorios.validarContenidoCampos(con, datosValidar, "WBS",  entrada.p_IdEmpresa);
		System.out.println(resultado);
		return resultado;
	}
	
	private Long contarAdvertencias(List<EntidadManipulada> entidadesManipuladas) {
		logger.info("INICIO::ModificarClienteWS.contarAdvertencias");
		long inicioServicio = System.currentTimeMillis();
		
		 Long advetencias = entidadesManipuladas.parallelStream().reduce(0L,
				    (sum, ent) -> sum += ent.Mensajes.stream().filter(m -> PlataformaUtils.MensajeSalida.MENSAJE_ADVERTENCIA.equals(m.tipo)).count(), 
				    (sum1, sum2) -> sum1 + sum2);
		 
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarClienteWS.contarAdvertencias", finServicio - inicioServicio);
		return advetencias;
	}

	private Long obtenerIdClientePorIdPersona(Connection con, Long idPersona) throws Exception {
		logger.debug("INICIO::ModificarClienteWS.obtenerIdClientePorIdPersona");
		long inicio = System.currentTimeMillis();

		HashMap<String, Object> parametros = new HashMap<String, Object>(4);
		parametros.put("IdPersona", idPersona);

		SQL sql = new SQL();
		List<Id> idsCliente = sql.query(con, SQL_SELECT_ID_CLIENTE, parametros, Id.class);

		Long idCliente = idsCliente.parallelStream().findFirst().orElse(new Id()).Id;

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarEntidadEnrolamiento.obtenerIdClientePorIdPersona", fin - inicio);
		return idCliente;
	}

	private SalidaWBS modificarDeclaracionOrigenFondo(Connection con, EntradaModificarClienteWS entrada, Long idPersona, Long idCliente, List<DeclaracionOrigenFondo> declaracionOrigenFondos) throws Exception {
		logger.debug("INICIO::ModificarClienteWS.modificarDeclaracionOrigenFondo");
		long inicio = System.currentTimeMillis();
		SalidaWBS salida = new SalidaWBS();
		ValidacionEntidad salidaValidaIntegridad = new ValidacionEntidad();
		SocioNegocio entradaValidaIntegridad = new SocioNegocio();
		declaracionOrigenFondos.forEach(dof -> {
			dof.IdCliente = idCliente;
			dof.IdPersona = idPersona;
		});
		entradaValidaIntegridad.DeclaracionOrigenFondo.addAll(declaracionOrigenFondos);
		salidaValidaIntegridad = selectEntidad(con, entradaValidaIntegridad, "DeclaracionOrigenFondo");
		
		
		ModificarDeclaracionOrigenFondos modificarDeclaracionOrigenFondos = new ModificarDeclaracionOrigenFondos();
		EntradaModificarDeclaracionOrigenFondos entradaCrearDeclaracionOrigenFondos = new EntradaModificarDeclaracionOrigenFondos();
		entradaCrearDeclaracionOrigenFondos.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCrearDeclaracionOrigenFondos.p_IdUsuario = entrada.p_IdUsuario;
		entradaCrearDeclaracionOrigenFondos.p_DeclaracionOrigenFondos = salidaValidaIntegridad.socioNegocio.DeclaracionOrigenFondo;
		salida = modificarDeclaracionOrigenFondos.logica(con, entradaCrearDeclaracionOrigenFondos);
		salida.P_OUT_ENTIDADES_MANIPULADAS.addAll(salidaValidaIntegridad.ENTIDADES_MANIPULADAS);
		salida.P_OUT_ERRORESCONTADOR+= (salidaValidaIntegridad.ERRORESCONTADOR);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarClienteWS.modificarDeclaracionOrigenFondo", fin - inicio);
		return salida;
	}

	private SalidaWBS crearDeclaracionOrigenFondo(Connection con, EntradaModificarClienteWS entrada, Long idPersona, Long idCliente, List<DeclaracionOrigenFondo> declaracionOrigenFondos) throws Exception {
		logger.debug("INICIO::ModificarClienteWS.crearDeclaracionOrigenFondo");
		long inicio = System.currentTimeMillis();
		SalidaWBS salida = new SalidaWBS();
		
		declaracionOrigenFondos.forEach(dof -> {
			dof.IdCliente = idCliente;
			dof.IdPersona = idPersona;
		});
		
		CrearDeclaracionOrigenFondos crearDeclaracionOrigenFondos = new CrearDeclaracionOrigenFondos();
		EntradaCrearDeclaracionOrigenFondos entradaCrearDeclaracionOrigenFondos = new EntradaCrearDeclaracionOrigenFondos();
		entradaCrearDeclaracionOrigenFondos.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCrearDeclaracionOrigenFondos.p_IdUsuario = entrada.p_IdUsuario;
		entradaCrearDeclaracionOrigenFondos.p_DeclaracionOrigenFondos = declaracionOrigenFondos;
		salida = crearDeclaracionOrigenFondos.logica(con, entradaCrearDeclaracionOrigenFondos);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarClienteWS.crearDeclaracionOrigenFondo", fin - inicio);
		return salida;
	}

	private SalidaWBS modificarNombresFantasia(Connection con, EntradaModificarClienteWS entrada, Long idPersona, List<NombreFantasia> nombresFantasia) throws Exception {
		logger.debug("INICIO::ModificarClienteWS.modificarNombresFantasia");
		long inicio = System.currentTimeMillis();
		SalidaWBS salida = new SalidaWBS();
		ValidacionEntidad salidaValidaIntegridad = new ValidacionEntidad();
		SocioNegocio entradaValidaIntegridad = new SocioNegocio();
		nombresFantasia.forEach(nombre -> nombre.IdPersona = idPersona);
		entradaValidaIntegridad.NombresFantasia.addAll(nombresFantasia);
		salidaValidaIntegridad = selectEntidad(con, entradaValidaIntegridad, "NombresFantasia");
		ModificarNombreFantasia modificarNombreFantasia = new ModificarNombreFantasia();
		EntradaModificarNombreFantasia entradaModificarNombreFantasia  = new EntradaModificarNombreFantasia();
		entradaModificarNombreFantasia.p_IdUsuario = entrada.p_IdUsuario;
		entradaModificarNombreFantasia.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaModificarNombreFantasia.p_NombresFantasia = salidaValidaIntegridad.socioNegocio.NombresFantasia;
		salida = modificarNombreFantasia.logica(con, entradaModificarNombreFantasia);
		salida.P_OUT_ENTIDADES_MANIPULADAS.addAll(salidaValidaIntegridad.ENTIDADES_MANIPULADAS);
		salida.P_OUT_ERRORESCONTADOR+= (salidaValidaIntegridad.ERRORESCONTADOR);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarClienteWS.modificarNombresFantasia", fin - inicio);
		return salida;
	}

	private SalidaWBS crearNombresFantasia(Connection con, EntradaModificarClienteWS entrada, Long idPersona, List<NombreFantasia> nombresFantasia) throws Exception {
		logger.debug("INICIO::ModificarClienteWS.crearNombresFantasia");
		long inicio = System.currentTimeMillis();
		SalidaWBS salida = new SalidaWBS();

		nombresFantasia.forEach(nombre -> nombre.IdPersona = idPersona);

		CrearNombreFantasia crearNombreFantasia = new CrearNombreFantasia();
		EntradaCrearNombreFantasia entradaCrearNombreFantasia  = new EntradaCrearNombreFantasia();
		entradaCrearNombreFantasia.p_IdUsuario = entrada.p_IdUsuario;
		entradaCrearNombreFantasia.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCrearNombreFantasia.p_NombresFantasia = nombresFantasia;
		salida = crearNombreFantasia.logica(con, entradaCrearNombreFantasia);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarClienteWS.crearNombresFantasia", fin - inicio);
		return salida;
	}

	private SalidaWBS modificarRelaciones(Connection con, EntradaModificarClienteWS entrada, Long idPersona, List<Relacion> relaciones) throws Exception {
		logger.debug("INICIO::ModificarClienteWS.modificarRelaciones");
		long inicio = System.currentTimeMillis();
		SalidaWBS salida = new SalidaWBS();
		ValidacionEntidad salidaValidaIntegridad = new ValidacionEntidad();
		SocioNegocio entradaValidaIntegridad = new SocioNegocio();
		relaciones.forEach(relacion -> relacion.IdPersona = idPersona);
		entradaValidaIntegridad.Relaciones.addAll(relaciones);
		salidaValidaIntegridad = selectEntidad(con, entradaValidaIntegridad, "Relaciones");
		ModificarRelacion modificarRelacion = new ModificarRelacion();
		EntradaModificarRelacion entradaModificarRelacion  = new EntradaModificarRelacion();
		entradaModificarRelacion.p_IdUsuario = entrada.p_IdUsuario;
		entradaModificarRelacion.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaModificarRelacion.p_Relaciones = salidaValidaIntegridad.socioNegocio.Relaciones;
		salida = modificarRelacion.logica(con, entradaModificarRelacion);
		salida.P_OUT_ENTIDADES_MANIPULADAS.addAll(salidaValidaIntegridad.ENTIDADES_MANIPULADAS);
		salida.P_OUT_ERRORESCONTADOR+= (salidaValidaIntegridad.ERRORESCONTADOR);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarClienteWS.modificarRelaciones", fin - inicio);
		return salida;
	}
	
	private SalidaWBS modificarConyuge(Connection con, EntradaModificarClienteWS entrada,Relacion relacionesConyuge, Long idPersona, DatosPersonaDatosGenerales datosGenerales, String agregaAdvertencia) throws Exception {
	
		logger.debug("INICIO::ModificarClienteWS.modificarConyuge");
		long inicio = System.currentTimeMillis();
		
		SalidaWBS salidaServicioModificarConyuge =new SalidaWBS();
		
		SQL sql = new SQL();
		HashMap<String, Object> params = new HashMap<String, Object>(2);
		params.put("DocumentosIdentidad", relacionesConyuge.DocumentoIdentidad);
		Long idPersonaExiste = sql.query(con, SQL_SELECT_IDPERSONA_DOCUMENTO_IDENTIDAD, params, IdPersonaConsulta.class).stream().findFirst().orElse(new IdPersonaConsulta()).IdPersona;
		if(idPersonaExiste > 0 && agregaAdvertencia.equalsIgnoreCase("S")) {
			EntidadManipulada entidadManipulada = new EntidadManipulada();
			HashMap<String, String> parametrosMensaje = new HashMap<>(2);
			entidadManipulada.Mensajes.add(new PlataformaUtils.MensajeSalida(idPersonaExiste, PlataformaUtils.MensajeSalida.MENSAJE_ADVERTENCIA, "UI_ADVERTENCIA_PERSONA_RELACIONADA_EXISTE", parametrosMensaje, con, entrada.p_IdEmpresa));
			datosGenerales.IdPersonaConyuge = idPersonaExiste;
			entidadManipulada.TipoEntidad = "RELACION";
			entidadManipulada.IdEntidad = 0L;
			
			salidaServicioModificarConyuge.P_OUT_ENTIDADES_MANIPULADAS.add(entidadManipulada);
			
			return salidaServicioModificarConyuge;
		}
		
		EntradaConyuge entradaConyuge			= new EntradaConyuge();
		entradaConyuge.Accion 					= relacionesConyuge.Accion;
		entradaConyuge.IdPersona 				= idPersona;
		entradaConyuge.IdPersonaConyuge 		= relacionesConyuge.IdPersonaRelacionada;
		entradaConyuge.EstadoCivil 				= datosGenerales.EstadoCivil;
		entradaConyuge.TipoDocumentoIdentidad 	= relacionesConyuge.TipoDocumentoIdentidad;
		entradaConyuge.DocumentoIdentidad		= relacionesConyuge.DocumentoIdentidad;
		entradaConyuge.Nombres					= relacionesConyuge.Nombre;
		entradaConyuge.ApellidoPaterno			= relacionesConyuge.ApellidoPaterno;
		entradaConyuge.ApellidoMaterno			= relacionesConyuge.ApellidoMaterno;
		entradaConyuge.IdEstado					= relacionesConyuge.IdEstado;
		
		EntradaModificarConyuge entradaModificarConyuge = new EntradaModificarConyuge();
		entradaModificarConyuge.p_IdEmpresa 			= entrada.p_IdEmpresa;
		entradaModificarConyuge.p_IdUsuario				= entrada.p_IdUsuario;
		entradaModificarConyuge.p_Conyuges				= entradaConyuge;
		
		ModificarConyuge servicioModificarConyuge = new ModificarConyuge();
		salidaServicioModificarConyuge = servicioModificarConyuge.logica(con, entradaModificarConyuge);
		
		if( salidaServicioModificarConyuge.P_OUT_ERRORESCONTADOR == 0 )
			datosGenerales.IdPersonaConyuge = this.consultarConyuge( con, idPersona );
			
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarClienteWS.modificarConyuge", fin - inicio);
		return salidaServicioModificarConyuge;
	}

	private Long consultarConyuge ( Connection con, Long idPersona ) throws Exception {
		
		logger.debug("INICIO::ModificarClienteWS.consultarConyuge");
		long inicio = System.currentTimeMillis();
		SQL sql 	= new SQL();
		
		HashMap<String, Object> params 	= new HashMap<String, Object>(2);
		
		params.put("IdPersona", idPersona);
		List<Conyuge> conyuge = sql.query(con, SQL_SELECT_CONYUGE, params, Conyuge.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarClienteWS.consultarConyuge", fin - inicio);
		return conyuge.get(0).IdPersonaConyuge;
	}
	
	private SalidaWBS crearRelaciones(Connection con, EntradaModificarClienteWS entrada, Long idPersona, List<Relacion> relaciones) throws Exception {
		logger.debug("INICIO::ModificarClienteWS.crearRelaciones");
		long inicio = System.currentTimeMillis();
		SalidaWBS salida = new SalidaWBS();

		relaciones.forEach(relacion -> relacion.IdPersona = idPersona);

		CrearRelacion crearRelacion = new CrearRelacion();
		EntradaCrearRelaciones entradaCrearRelacionee  = new EntradaCrearRelaciones();
		entradaCrearRelacionee.p_IdUsuario = entrada.p_IdUsuario;
		entradaCrearRelacionee.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCrearRelacionee.p_Relaciones = relaciones;	
		salida = crearRelacion.logica(con, entradaCrearRelacionee);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarClienteWS.crearRelaciones", fin - inicio);
		return salida;
	}

	private SalidaWBS eliminarFatcasPaisResidencia(Connection con, EntradaModificarClienteWS entrada, Long idPersona, List<FatcaPaisResidencia> fatcas) throws Exception {
		logger.debug("INICIO::ModificarClienteWS.eliminarFatcasPaisResidencia");
		long inicio = System.currentTimeMillis();
		SalidaWBS salida = new SalidaWBS();

		fatcas.forEach(fatca -> fatca.IdPersona = idPersona);

		EliminarFatcaPaisResidencia eliminarFatcaPaisResidencia = new EliminarFatcaPaisResidencia();
		EntradaEliminarFatcaPaisResidencia entradaEliminarFatcaPaisResidencia  = new EntradaEliminarFatcaPaisResidencia();
		entradaEliminarFatcaPaisResidencia.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaEliminarFatcaPaisResidencia.p_FatcasPaisesResidencias = fatcas;
		entradaEliminarFatcaPaisResidencia.p_IdPersona = idPersona;
		salida = eliminarFatcaPaisResidencia.logica(con, entradaEliminarFatcaPaisResidencia);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarClienteWS.eliminarFatcasPaisResidencia", fin - inicio);
		return salida;
	}

	private SalidaWBS modificarFatcasPaisResidencia(Connection con, EntradaModificarClienteWS entrada, Long idPersona, List<FatcaPaisResidencia> fatcas) throws Exception {
		logger.debug("INICIO::ModificarClienteWS.modificarFatcasPaisResidencia");
		long inicio = System.currentTimeMillis();
		SalidaWBS salida = new SalidaWBS();

		fatcas.forEach(fatca -> fatca.IdPersona = idPersona);

		ModificarFatcaPaisResidencia modificarFatcaPaisResidencia = new ModificarFatcaPaisResidencia();
		EntradaModificarFatcaPaisResidencia entradaModificarFatcaPaisResidencia  = new EntradaModificarFatcaPaisResidencia();
		entradaModificarFatcaPaisResidencia.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaModificarFatcaPaisResidencia.p_FatcasPaisesResidencias = fatcas;
		entradaModificarFatcaPaisResidencia.p_IdPersona = idPersona;
		salida = modificarFatcaPaisResidencia.logica(con, entradaModificarFatcaPaisResidencia);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarClienteWS.modificarFatcasPaisResidencia", fin - inicio);
		return salida;
	}

	private SalidaWBS crearFatcasPaisResidencia(Connection con, EntradaModificarClienteWS entrada, Long idPersona, List<FatcaPaisResidencia> fatcas) throws Exception {
		logger.debug("INICIO::ModificarClienteWS.crearFatcasPaisResidencia");
		long inicio = System.currentTimeMillis();
		SalidaWBS salida = new SalidaWBS();

		fatcas.forEach(fatca -> fatca.IdPersona = idPersona);

		CrearFatcaPaisResidencia crearFatcaPaisResidencia = new CrearFatcaPaisResidencia();
		EntradaCrearFatcaPaisResidencia entradaCrearFatcaPaisResidencia  = new EntradaCrearFatcaPaisResidencia();
		entradaCrearFatcaPaisResidencia.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCrearFatcaPaisResidencia.p_FatcasPaisesResidencias = fatcas;
		entradaCrearFatcaPaisResidencia.p_IdPersona = idPersona;
		salida = crearFatcaPaisResidencia.logica(con, entradaCrearFatcaPaisResidencia);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarClienteWS.crearFatcasPaisResidencia", fin - inicio);
		return salida;
	}

	private SalidaWBS modificarFatca(Connection con, EntradaModificarClienteWS entrada, Long idPersona,List<Fatca> fatcas) throws Exception {
		logger.debug("INICIO::ModificarClienteWS.modificarFatca");
		long inicio = System.currentTimeMillis();
		SalidaWBS salida = new SalidaWBS();

		fatcas.forEach(fatca -> fatca.IdPersona = idPersona);

		ModificarFatca modificarFatca = new ModificarFatca();
		EntradaModificarFatca entradaModificarFatca  = new EntradaModificarFatca();
		entradaModificarFatca.p_IdUsuario = entrada.p_IdUsuario;
		entradaModificarFatca.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaModificarFatca.p_Fatcas = fatcas;
		salida = modificarFatca.logica(con, entradaModificarFatca);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarClienteWS.modificarFatca", fin - inicio);
		return salida;
	}

	private SalidaWBS modificarIndicadores(Connection con, EntradaModificarClienteWS entrada, Long idPersona, List<Indicador> indicadores, String formulario) throws Exception {
		logger.debug("INICIO::ModificarClienteWS.modificarIndicadores");
		long inicio = System.currentTimeMillis();
		SalidaWBS salida = new SalidaWBS();

		indicadores.forEach(indicador -> indicador.IdPersona = idPersona);
		ModificarIndicadores modificarIndicadores = new ModificarIndicadores();
		EntradaModificarIndicador entradaModificarIndicador  = new EntradaModificarIndicador();
		entradaModificarIndicador.p_IdUsuario = entrada.p_IdUsuario;
		entradaModificarIndicador.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaModificarIndicador.p_Formulario = formulario;
		if(formulario.equals("INDICADORES_PORTAFOLIO")) {
			ValidacionEntidad salidaValidaIntegridad = new ValidacionEntidad();
			SocioNegocio entradaValidaIntegridad = new SocioNegocio();
			if( "N".equals(entrada.p_ActualizarIndicadorFormaCobro) ) {
				indicadores = this.validarIndicadorFormaCobroPortafolioApv(con, indicadores);
			}
			entradaValidaIntegridad.IndicadoresCliente.addAll(indicadores);
			salidaValidaIntegridad = selectEntidad(con, entradaValidaIntegridad, "DatosPortafolio");
			entradaModificarIndicador.p_Indicadores = salidaValidaIntegridad.socioNegocio.IndicadoresCliente;
			salida = modificarIndicadores.logica(con, entradaModificarIndicador);
			salida.P_OUT_ENTIDADES_MANIPULADAS.addAll(salidaValidaIntegridad.ENTIDADES_MANIPULADAS);
			salida.P_OUT_ERRORESCONTADOR+= (salidaValidaIntegridad.ERRORESCONTADOR);
		}else {
			entradaModificarIndicador.p_Indicadores = indicadores;
			salida = modificarIndicadores.logica(con, entradaModificarIndicador);
		}

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarClienteWS.modificarIndicadores", fin - inicio);
		return salida;
	}
	
	private List<Indicador> validarIndicadorFormaCobroPortafolioApv(Connection con, List<Indicador> indicadores) throws Exception {
		
		logger.debug("INICIO::ModificarClienteWS.validarIndicadorFormaCobroPortafolioApv");
		long inicio 							= System.currentTimeMillis();
		List<Indicador> indicadoresAEliminar	= new ArrayList<>();
		SQL sql 								= new SQL();

		List<Long> idsPortafolios = indicadores
										.stream()
							            .collect(Collectors.toSet())
							            .stream()
										.map(item -> item.IdPortafolio)
										.collect(Collectors.toList());
		
		HashMap<String, Object> parametros = new HashMap<String, Object>(4);
		parametros.put("IdPortafolio", idsPortafolios);

		List<Portafolio> portafolios = sql.query(con, SQL_SELECT_CONSULTAR_PORTAFOLIO, parametros, Portafolio.class);
		
		for(Portafolio portafolio: portafolios ) {
			
			indicadoresAEliminar = indicadores
									.stream()
									.filter( a -> "COBROCUST".equals(a.CodigoTipoIndicador) &&
											a.IdPortafolio.compareTo(portafolio.IdPortafolio) == 0
											)
									.collect(Collectors.toList());	
			
		}
			
		for( Indicador indicadorAEliminar:  indicadoresAEliminar) {
			if( indicadores.contains(indicadorAEliminar) )
				indicadores.remove(indicadorAEliminar);
		}
		
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarClienteWS.validarIndicadorFormaCobroPortafolioApv", fin - inicio);
		return indicadores;
		
	}

	private SalidaWBS eliminarDireccionesElectronica(Connection con, EntradaModificarClienteWS entrada, Long idPersona,List<DireccionElectronica> direccionesElectronica) throws Exception {
		logger.debug("INICIO::ModificarClienteWS.eliminarDireccionesElectronica");
		long inicio = System.currentTimeMillis();
		SalidaWBS salida = new SalidaWBS();
		ValidacionEntidad salidaValidaIntegridad = new ValidacionEntidad();
		SocioNegocio entradaValidaIntegridad = new SocioNegocio();
		direccionesElectronica.forEach(direccion -> direccion.IdPersona = idPersona);
		entradaValidaIntegridad.DireccionesElectronica.addAll(direccionesElectronica);
		salidaValidaIntegridad = selectEntidad(con, entradaValidaIntegridad, "DireccionElectronica");
		EliminarDireccionElectronica eliminarDireccionElectronica = new EliminarDireccionElectronica();
		EntradaEliminarDireccionElectronica entradaEliminarDireccionElectronica  = new EntradaEliminarDireccionElectronica();
		entradaEliminarDireccionElectronica.p_IdUsuario = entrada.p_IdUsuario;
		entradaEliminarDireccionElectronica.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaEliminarDireccionElectronica.p_DireccionesElectronicas = salidaValidaIntegridad.socioNegocio.DireccionesElectronica;
		salida = eliminarDireccionElectronica.logica(con, entradaEliminarDireccionElectronica);
		salida.P_OUT_ENTIDADES_MANIPULADAS.addAll(salidaValidaIntegridad.ENTIDADES_MANIPULADAS);
		salida.P_OUT_ERRORESCONTADOR+= (salidaValidaIntegridad.ERRORESCONTADOR);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarClienteWS.eliminarDireccionesElectronica", fin - inicio);
		return salida;
	}

	private SalidaWBS modificarDireccionesElectronica(Connection con, EntradaModificarClienteWS entrada, Long idPersona,List<DireccionElectronica> direccionesElectronica) throws Exception {
		logger.debug("INICIO::ModificarClienteWS.modificarDireccionesElectronica");
		long inicio = System.currentTimeMillis();
		SalidaWBS salida = new SalidaWBS();
		ValidacionEntidad salidaValidaIntegridad = new ValidacionEntidad();
		SocioNegocio entradaValidaIntegridad = new SocioNegocio();
		direccionesElectronica.forEach(direccion -> direccion.IdPersona = idPersona);
		entradaValidaIntegridad.DireccionesElectronica.addAll(direccionesElectronica);
		salidaValidaIntegridad = selectEntidad(con, entradaValidaIntegridad, "DireccionElectronica");	
		ModificarDireccionElectronica modificarDireccionElectronica = new ModificarDireccionElectronica();
		EntradaModificarDireccionElectronica entradaModificarDireccionElectronica = new EntradaModificarDireccionElectronica();
		entradaModificarDireccionElectronica.p_IdUsuario = entrada.p_IdUsuario;
		entradaModificarDireccionElectronica.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaModificarDireccionElectronica.p_DireccionesElectronicas = salidaValidaIntegridad.socioNegocio.DireccionesElectronica;
		salida = modificarDireccionElectronica.logica(con, entradaModificarDireccionElectronica);
		salida.P_OUT_ENTIDADES_MANIPULADAS.addAll(salidaValidaIntegridad.ENTIDADES_MANIPULADAS);
		salida.P_OUT_ERRORESCONTADOR+= (salidaValidaIntegridad.ERRORESCONTADOR);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarClienteWS.modificarDireccionesElectronica", fin - inicio);


		return salida;
	}
	

	private SalidaWBS crearDireccionesElectronica(Connection con, EntradaModificarClienteWS entrada, Long idPersona,List<DireccionElectronica> direccionesElectronica) throws Exception {
		logger.debug("INICIO::ModificarClienteWS.crearDireccionesElectronica");
		long inicio = System.currentTimeMillis();
		SalidaWBS salida = new SalidaWBS();

		direccionesElectronica.forEach(direccion -> direccion.IdPersona = idPersona);

		CrearDireccionElectronica crearDireccionElectronica= new CrearDireccionElectronica();
		EntradaCrearDireccionElectronica entradaCrearDireccionElectronica = new EntradaCrearDireccionElectronica();
		entradaCrearDireccionElectronica.p_IdUsuario = entrada.p_IdUsuario;
		entradaCrearDireccionElectronica.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCrearDireccionElectronica.p_DireccionesElectronicas = direccionesElectronica;
		salida = crearDireccionElectronica.logica(con, entradaCrearDireccionElectronica);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarClienteWS.crearDireccionesElectronica", fin - inicio);
		return salida;
	}

	private SalidaWBS eliminarDireccionesFisica(Connection con, EntradaModificarClienteWS entrada, Long idPersona, List<DireccionFisica> direccionesFisica) throws Exception {
		logger.debug("INICIO::ModificarClienteWS.eliminarDireccionesFisica");
		long inicio = System.currentTimeMillis();
		SalidaWBS salida = new SalidaWBS();
		ValidacionEntidad salidaValidaIntegridad = new ValidacionEntidad();
		SocioNegocio entradaValidaIntegridad = new SocioNegocio();
		direccionesFisica.forEach(direccion -> direccion.IdPersona = idPersona);
		entradaValidaIntegridad.DireccionesFisica.addAll(direccionesFisica);
		salidaValidaIntegridad = selectEntidad(con, entradaValidaIntegridad, "DireccionFisica");
		EliminarDireccionFisica eliminarDireccionFisica = new EliminarDireccionFisica();
		EntradaEliminarDireccionFisica entradaEliminarDireccionFisica  = new EntradaEliminarDireccionFisica();
		entradaEliminarDireccionFisica.p_IdUsuario = entrada.p_IdUsuario;
		entradaEliminarDireccionFisica.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaEliminarDireccionFisica.p_DireccionesFisicas = salidaValidaIntegridad.socioNegocio.DireccionesFisica;;
		salida = eliminarDireccionFisica.logica(con, entradaEliminarDireccionFisica);
		salida.P_OUT_ENTIDADES_MANIPULADAS.addAll(salidaValidaIntegridad.ENTIDADES_MANIPULADAS);
		salida.P_OUT_ERRORESCONTADOR+= (salidaValidaIntegridad.ERRORESCONTADOR);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarClienteWS.eliminarDireccionesFisica", fin - inicio);
		return salida;
	}

	private SalidaWBS modificarDireccionesFisica(Connection con, EntradaModificarClienteWS entrada, Long idPersona,List<DireccionFisica> direccionesFisica) throws Exception {
		logger.debug("INICIO::ModificarClienteWS.crearDireccionesFisica");
		long inicio = System.currentTimeMillis();
		SalidaWBS salida = new SalidaWBS();
		ValidacionEntidad salidaValidaIntegridad = new ValidacionEntidad();
		SocioNegocio entradaValidaIntegridad = new SocioNegocio();
		direccionesFisica.forEach(direccion -> direccion.IdPersona = idPersona);
		entradaValidaIntegridad.DireccionesFisica.addAll(direccionesFisica);
		salidaValidaIntegridad = selectEntidad(con, entradaValidaIntegridad, "DireccionFisica");
		ModificarDireccionFisica modificarDireccionFisica = new ModificarDireccionFisica();
		EntradaModificarDireccionFisica entradaModificarDireccionFisica = new EntradaModificarDireccionFisica();
		entradaModificarDireccionFisica.p_IdUsuario = entrada.p_IdUsuario;
		entradaModificarDireccionFisica.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaModificarDireccionFisica.p_DireccionesFisicas = salidaValidaIntegridad.socioNegocio.DireccionesFisica;
		salida = modificarDireccionFisica.logica(con, entradaModificarDireccionFisica);
		salida.P_OUT_ENTIDADES_MANIPULADAS.addAll(salidaValidaIntegridad.ENTIDADES_MANIPULADAS);
		salida.P_OUT_ERRORESCONTADOR+= (salidaValidaIntegridad.ERRORESCONTADOR);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarClienteWS.crearDireccionesFisica", fin - inicio);
		return salida;
	}

	private SalidaWBS crearDireccionesFisica(Connection con, EntradaModificarClienteWS entrada, Long idPersona,List<DireccionFisica> direccionesFisica) throws Exception {
		logger.debug("INICIO::ModificarClienteWS.crearDireccionesFisica");
		long inicio = System.currentTimeMillis();
		SalidaWBS salida = new SalidaWBS();

		direccionesFisica.forEach(direccion -> direccion.IdPersona = idPersona);

		CrearDireccionFisica crearDireccionFisica = new CrearDireccionFisica();
		EntradaCrearDireccionFisica entradaCrearDireccionFisica = new EntradaCrearDireccionFisica();
		entradaCrearDireccionFisica.p_IdUsuario = entrada.p_IdUsuario;
		entradaCrearDireccionFisica.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCrearDireccionFisica.p_DireccionesFisicas = direccionesFisica;
		salida = crearDireccionFisica.logica(con, entradaCrearDireccionFisica);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarClienteWS.crearDireccionesFisica", fin - inicio);
		return salida;
	}

	private SalidaWBS eliminarTelefonos(Connection con, EntradaModificarClienteWS entrada, Long idPersona, List<Telefono> telefonos) throws Exception {
		logger.debug("INICIO::ModificarClienteWS.eliminarTelefonos");
		long inicio = System.currentTimeMillis();
		SalidaWBS salida = new SalidaWBS();
		ValidacionEntidad salidaValidaIntegridad = new ValidacionEntidad();
		SocioNegocio entradaValidaIntegridad = new SocioNegocio();
		telefonos.forEach(telefono -> telefono.IdPersona = idPersona);
		entradaValidaIntegridad.Telefonos.addAll(telefonos);	
		salidaValidaIntegridad = selectEntidad(con, entradaValidaIntegridad, "Telefono");
		EliminarTelefono eliminarTelefono = new EliminarTelefono();
		EntradaEliminarTelefono entradaEliminarTelefono = new EntradaEliminarTelefono();
		entradaEliminarTelefono.p_IdUsuario = entrada.p_IdUsuario;
		entradaEliminarTelefono.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaEliminarTelefono.p_Telefonos = salidaValidaIntegridad.socioNegocio.Telefonos;
		salida = eliminarTelefono.logica(con, entradaEliminarTelefono);
		salida.P_OUT_ENTIDADES_MANIPULADAS.addAll(salidaValidaIntegridad.ENTIDADES_MANIPULADAS);
		salida.P_OUT_ERRORESCONTADOR+= (salidaValidaIntegridad.ERRORESCONTADOR);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarClienteWS.eliminarTelefonos", fin - inicio);
		return salida;
	}

	private SalidaWBS modificarTelefonos(Connection con, EntradaModificarClienteWS entrada, Long idPersona,List<Telefono> telefonos) throws Exception {
		logger.debug("INICIO::ModificarClienteWS.modificarTelefonos");
		long inicio = System.currentTimeMillis();
		SalidaWBS salida = new SalidaWBS();
		ValidacionEntidad salidaValidaIntegridad = new ValidacionEntidad();
		SocioNegocio entradaValidaIntegridad = new SocioNegocio();

		telefonos.forEach(telefono -> telefono.IdPersona = idPersona);
		entradaValidaIntegridad.Telefonos.addAll(telefonos);	
		salidaValidaIntegridad = selectEntidad(con, entradaValidaIntegridad, "Telefono");


		ModificarTelefono modificarTelefono = new ModificarTelefono();
		EntradaModificarTelefono entradaModificarTelefono = new EntradaModificarTelefono();
		entradaModificarTelefono.p_IdUsuario = entrada.p_IdUsuario;
		entradaModificarTelefono.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaModificarTelefono.p_Telefonos =  salidaValidaIntegridad.socioNegocio.Telefonos;
		salida = modificarTelefono.logica(con, entradaModificarTelefono);
		salida.P_OUT_ENTIDADES_MANIPULADAS.addAll(salidaValidaIntegridad.ENTIDADES_MANIPULADAS);
		salida.P_OUT_ERRORESCONTADOR+= (salidaValidaIntegridad.ERRORESCONTADOR);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarClienteWS.modificarTelefonos", fin - inicio);
		return salida;
	}

	private SalidaWBS crearTelefonos(Connection con, EntradaModificarClienteWS entrada, Long idPersona,List<Telefono> telefonos) throws Exception {
		logger.debug("INICIO::ModificarClienteWS.crearTelefonos");
		long inicio = System.currentTimeMillis();
		SalidaWBS salida = new SalidaWBS();

		telefonos.forEach(telefono -> telefono.IdPersona = idPersona);

		CrearTelefono crearTelefono = new CrearTelefono();
		EntradaCrearTelefono entradaCrearTelefono = new EntradaCrearTelefono();
		entradaCrearTelefono.p_IdUsuario = entrada.p_IdUsuario;
		entradaCrearTelefono.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCrearTelefono.p_Telefonos = telefonos;
		salida = crearTelefono.logica(con, entradaCrearTelefono);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarClienteWS.crearTelefonos", fin - inicio);
		return salida;
	}

	private SalidaWBS eliminarTarjetasBancarias(Connection con, EntradaModificarClienteWS entrada, Long idPersona, List<TarjetaBancaria> tarjetaBancarias) throws Exception {
		logger.debug("INICIO::ModificarClienteWS.eliminarTarjetasBancarias");
		long inicio = System.currentTimeMillis();
		SalidaWBS salida = new SalidaWBS();
		ValidacionEntidad salidaValidaIntegridad = new ValidacionEntidad();
		SocioNegocio entradaValidaIntegridad = new SocioNegocio();
		tarjetaBancarias.forEach(tarjetas -> tarjetas.IdPersona = idPersona);
		entradaValidaIntegridad.TarjetasBancaria.addAll(tarjetaBancarias);
		salidaValidaIntegridad = selectEntidad(con, entradaValidaIntegridad, "TarjetasBancaria");
		EliminarTarjetaBancaria eliminarTarjetasBancarias = new EliminarTarjetaBancaria();
		EntradaEliminarTarjetaBancaria entradaEliminarTarjetaBancaria = new EntradaEliminarTarjetaBancaria();
		entradaEliminarTarjetaBancaria.p_IdUsuario = entrada.p_IdUsuario;
		entradaEliminarTarjetaBancaria.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaEliminarTarjetaBancaria.p_TarjetasBancarias = salidaValidaIntegridad.socioNegocio.TarjetasBancaria;
		salida = eliminarTarjetasBancarias.logica(con, entradaEliminarTarjetaBancaria);
		salida.P_OUT_ENTIDADES_MANIPULADAS.addAll(salidaValidaIntegridad.ENTIDADES_MANIPULADAS);
		salida.P_OUT_ERRORESCONTADOR+= (salidaValidaIntegridad.ERRORESCONTADOR);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarClienteWS.eliminarTarjetasBancarias", fin - inicio);
		return salida;
	}

	private SalidaWBS modificarTarjetasBancarias(Connection con, EntradaModificarClienteWS entrada, Long idPersona,List<TarjetaBancaria> tarjetasBancarias) throws Exception {
		logger.debug("INICIO::ModificarClienteWS.modificarTarjetasBancarias");
		long inicio = System.currentTimeMillis();
		SalidaWBS salida = new SalidaWBS();
		ValidacionEntidad salidaValidaIntegridad = new ValidacionEntidad();
		SocioNegocio entradaValidaIntegridad = new SocioNegocio();
		tarjetasBancarias.forEach(tarjetas -> tarjetas.IdPersona = idPersona);
		entradaValidaIntegridad.TarjetasBancaria.addAll(tarjetasBancarias);
		salidaValidaIntegridad = selectEntidad(con, entradaValidaIntegridad, "TarjetasBancaria");
		ModificarTarjetaBancaria modificarTarjetaBancaria = new ModificarTarjetaBancaria();
		EntradaModificarTarjetaBancaria entradaModificarTarjetaBancaria = new EntradaModificarTarjetaBancaria();
		entradaModificarTarjetaBancaria.p_IdUsuario = entrada.p_IdUsuario;
		entradaModificarTarjetaBancaria.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaModificarTarjetaBancaria.p_TarjetasBancarias = salidaValidaIntegridad.socioNegocio.TarjetasBancaria;
		salida = modificarTarjetaBancaria.logica(con, entradaModificarTarjetaBancaria);
		salida.P_OUT_ENTIDADES_MANIPULADAS.addAll(salidaValidaIntegridad.ENTIDADES_MANIPULADAS);
		salida.P_OUT_ERRORESCONTADOR+= (salidaValidaIntegridad.ERRORESCONTADOR);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarClienteWS.modificarTarjetasBancarias", fin - inicio);
		return salida;
	}

	private SalidaWBS crearTarjetasBancarias(Connection con, EntradaModificarClienteWS entrada, Long idPersona,List<TarjetaBancaria> tarjetasBancarias) throws Exception {
		logger.debug("INICIO::ModificarClienteWS.crearTarjetasBancarias");
		long inicio = System.currentTimeMillis();
		SalidaWBS salida = new SalidaWBS();

		tarjetasBancarias.forEach(tarjetas -> tarjetas.IdPersona = idPersona);

		CrearTarjetaBancaria crearTarjetaBancaria = new CrearTarjetaBancaria();
		EntradaCrearTarjetaBancaria entradaCrearTarjetaBancaria = new EntradaCrearTarjetaBancaria();
		entradaCrearTarjetaBancaria.p_IdUsuario = entrada.p_IdUsuario;
		entradaCrearTarjetaBancaria.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCrearTarjetaBancaria.p_TarjetasBancarias = tarjetasBancarias;
		salida = crearTarjetaBancaria.logica(con, entradaCrearTarjetaBancaria);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarClienteWS.crearTarjetasBancarias", fin - inicio);
		return salida;
	}

	private SalidaWBS eliminarCuentasBancarias(Connection con, EntradaModificarClienteWS entrada, Long idPersona,List<CuentaBancaria> cuentasBancarias) throws Exception {
		logger.debug("INICIO::ModificarClienteWS.eliminarCuentasBancarias");
		long inicio = System.currentTimeMillis();
		SalidaWBS salida = new SalidaWBS();
		ValidacionEntidad salidaValidaIntegridad = new ValidacionEntidad();
		SocioNegocio entradaValidaIntegridad = new SocioNegocio();
		cuentasBancarias.forEach(cuentas -> cuentas.IdPersona = idPersona);
		entradaValidaIntegridad.CuentasBancaria.addAll(cuentasBancarias);
		salidaValidaIntegridad = selectEntidad(con, entradaValidaIntegridad, "CuentasBancaria");
		EliminarCuentaBancaria eliminarCuentaBancaria = new EliminarCuentaBancaria();
		EntradaEliminarCuentaBancaria entradaEliminarCuentaBancaria = new EntradaEliminarCuentaBancaria();
		entradaEliminarCuentaBancaria.p_IdUsuario = entrada.p_IdUsuario;
		entradaEliminarCuentaBancaria.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaEliminarCuentaBancaria.p_CuentasBancarias = salidaValidaIntegridad.socioNegocio.CuentasBancaria;
		salida = eliminarCuentaBancaria.logica(con, entradaEliminarCuentaBancaria);
		salida.P_OUT_ENTIDADES_MANIPULADAS.addAll(salidaValidaIntegridad.ENTIDADES_MANIPULADAS);
		salida.P_OUT_ERRORESCONTADOR+= (salidaValidaIntegridad.ERRORESCONTADOR);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarClienteWS.eliminarCuentasBancarias", fin - inicio);
		return salida;
	}

	private SalidaWBS modificarCuentasBancarias(Connection con, EntradaModificarClienteWS entrada, Long idPersona,List<CuentaBancaria> cuentasBancarias) throws Exception {
		logger.debug("INICIO::ModificarClienteWS.modificarCuentasBancarias");
		long inicio = System.currentTimeMillis();
		SalidaWBS salida = new SalidaWBS();
		ValidacionEntidad salidaValidaIntegridad = new ValidacionEntidad();
		SocioNegocio entradaValidaIntegridad = new SocioNegocio();
		cuentasBancarias.forEach(cuentas -> cuentas.IdPersona = idPersona);
		entradaValidaIntegridad.CuentasBancaria.addAll(cuentasBancarias);
		salidaValidaIntegridad = selectEntidad(con, entradaValidaIntegridad, "CuentasBancaria");
		ModificarCuentaBancaria modificarCuentaBancaria = new ModificarCuentaBancaria();
		EntradaModificarCuentaBancaria entradaModificarCuentaBancaria = new EntradaModificarCuentaBancaria();
		entradaModificarCuentaBancaria.p_IdUsuario = entrada.p_IdUsuario;
		entradaModificarCuentaBancaria.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaModificarCuentaBancaria.p_CuentasBancarias = salidaValidaIntegridad.socioNegocio.CuentasBancaria;
		salida = modificarCuentaBancaria.logica(con, entradaModificarCuentaBancaria);
		salida.P_OUT_ENTIDADES_MANIPULADAS.addAll(salidaValidaIntegridad.ENTIDADES_MANIPULADAS);
		salida.P_OUT_ERRORESCONTADOR+= (salidaValidaIntegridad.ERRORESCONTADOR);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarClienteWS.modificarCuentasBancarias", fin - inicio);
		return salida;
	}

	private SalidaWBS crearCuentasBancarias(Connection con, EntradaModificarClienteWS entrada, Long idPersona, List<CuentaBancaria> cuentasBancarias) throws Exception {
		logger.debug("INICIO::ModificarClienteWS.crearCuentasBancarias");
		long inicio = System.currentTimeMillis();
		SalidaWBS salida = new SalidaWBS();

		cuentasBancarias.forEach(cuentas -> cuentas.IdPersona = idPersona);
		
		CrearCuentaBancaria crearCuentaBancaria = new CrearCuentaBancaria();
		EntradaCrearCuentaBancaria entradaCrearCuentaBancaria = new EntradaCrearCuentaBancaria();
		entradaCrearCuentaBancaria.p_IdUsuario = entrada.p_IdUsuario;
		entradaCrearCuentaBancaria.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCrearCuentaBancaria.p_CuentasBancarias = cuentasBancarias;

		salida = crearCuentaBancaria.logica(con, entradaCrearCuentaBancaria);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarClienteWS.crearCuentasBancarias", fin - inicio);
		return salida;
	}

	private SalidaWBS modificarTiposDocumento(Connection con, EntradaModificarClienteWS entrada, Long idPersona, List<TipoDocumento> tiposDodumento) throws Exception {
		logger.debug("INICIO::ModificarClienteWS.modificarTiposDocumento");
		long inicio = System.currentTimeMillis();
		SalidaWBS salida = new SalidaWBS();
		ValidacionEntidad salidaValidaIntegridad = new ValidacionEntidad();
		SocioNegocio entradaValidaIntegridad = new SocioNegocio();
		tiposDodumento.forEach(tipos -> tipos.IdPersona = idPersona);
		entradaValidaIntegridad.TiposDocumento.addAll(tiposDodumento);
		salidaValidaIntegridad = selectEntidad(con, entradaValidaIntegridad, "TiposDocumento");
		ModificarDocumentoIdentidad modificarDocumentoIdentidad = new ModificarDocumentoIdentidad();
		EntradaModificarDocumentoIdentidad entradaModificarDocumentoIdentidad = new EntradaModificarDocumentoIdentidad();
		entradaModificarDocumentoIdentidad.p_IdUsuario = entrada.p_IdUsuario;
		entradaModificarDocumentoIdentidad.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaModificarDocumentoIdentidad.p_DocumentosIdentidad = salidaValidaIntegridad.socioNegocio.TiposDocumento;
		salida = modificarDocumentoIdentidad.logica(con, entradaModificarDocumentoIdentidad);
		salida.P_OUT_ENTIDADES_MANIPULADAS.addAll(salidaValidaIntegridad.ENTIDADES_MANIPULADAS);
		salida.P_OUT_ERRORESCONTADOR+= (salidaValidaIntegridad.ERRORESCONTADOR);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarClienteWS.modificarTiposDocumento", fin - inicio);
		return salida;
	}

	private SalidaWBS crearTiposDocumento(Connection con, EntradaModificarClienteWS entrada, Long idPersona, List<TipoDocumento> tiposDocumento) throws Exception {
		logger.debug("INICIO::ModificarClienteWS.crearTiposDocumento");
		long inicio = System.currentTimeMillis();
		SalidaWBS salida = new SalidaWBS();

		tiposDocumento.forEach(tipos -> tipos.IdPersona = idPersona);

		CrearDocumentoIdentidad crearDocumentoIdentidad = new CrearDocumentoIdentidad();
		EntradaCrearDocumentoIdentidad entradaCrearDocumentoIdentidad = new EntradaCrearDocumentoIdentidad();
		entradaCrearDocumentoIdentidad.p_IdUsuario = entrada.p_IdUsuario;
		entradaCrearDocumentoIdentidad.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCrearDocumentoIdentidad.p_TiposDocumento = tiposDocumento;
		salida = crearDocumentoIdentidad.logica(con, entradaCrearDocumentoIdentidad);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarClienteWS.crearTiposDocumento", fin - inicio);
		return salida;
	}

	private Long obtenerIdPersonaPorDocumentoIdentidad(Connection con, SocioNegocio socioNegocio) throws Exception {
		logger.debug("INICIO::ModificarClienteWS.obtenerIdPersonaPorDocumentoIdentidad");
		long inicio = System.currentTimeMillis();

		HashMap<String, Object> parametros = new HashMap<String, Object>(4);
		parametros.put("DocumentoIdentidad", socioNegocio.DocumentoIdentidad.trim());
		parametros.put("IdTipoDocumentoIdentidad", socioNegocio.TipoDocumentoIdentidad);

		SQL sql = new SQL();
		List<Id> idsPersona = sql.query(con, SQL_SELECT_ID_PERSONA, parametros, Id.class);

		Long idPersona = idsPersona.parallelStream().findFirst().orElse(new Id()).Id;

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarEntidadEnrolamiento.obtenerIdPersonaPorDocumentoIdentidad", fin - inicio);
		return idPersona;
	}
	
	private String obtenerIdTipoCanalIngresoCorredorPorDocumentoIdentidad(Connection con, SocioNegocio socioNegocio) throws Exception {
		logger.debug("INICIO::ModificarClienteWS.obtenerIdTipoCanalIngresoCorredorPorDocumentoIdentidad");
		long inicio = System.currentTimeMillis();

		HashMap<String, Object> parametros = new HashMap<String, Object>(4);
		parametros.put("DocumentoIdentidad", socioNegocio.DocumentoIdentidad);
		parametros.put("IdTipoDocumentoIdentidad", socioNegocio.TipoDocumentoIdentidad);

		SQL sql = new SQL();
		List<Id> idsTipoCanalIngresoCorredor = sql.query(con, SQL_SELECT_ID_PERSONA, parametros, Id.class);

		String idTipoCanalIngresoCorredor = idsTipoCanalIngresoCorredor.parallelStream().findFirst().orElse(new Id()).IdTipoCanalIngresoCorredor;

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarEntidadEnrolamiento.obtenerIdTipoCanalIngresoCorredorPorDocumentoIdentidad", fin - inicio);
		return idTipoCanalIngresoCorredor;
	}	
	
	private SalidaWBS modificarDatosGeneralesCliente(Connection con, EntradaModificarClienteWS entrada, Long idPersona, List<DatosClienteDatosGenerales> datosGenerales) throws Exception {
		logger.debug("INICIO::ModificarClienteWS.modificarDatosGeneralesCliente");
		long inicio = System.currentTimeMillis();
		SalidaWBS salida = new SalidaWBS();

		datosGenerales.forEach(datos -> datos.IdPersona = idPersona);

		ModificarDatosGeneralesCliente modificarDatosGeneralesCliente = new ModificarDatosGeneralesCliente();
		EntradaModificarDatosGeneralesCliente entradaModificarDatosGeneralesCliente = new EntradaModificarDatosGeneralesCliente();
		entradaModificarDatosGeneralesCliente.p_IdUsuario = entrada.p_IdUsuario;
		entradaModificarDatosGeneralesCliente.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaModificarDatosGeneralesCliente.p_ClienteDatosGenerales = datosGenerales;
		salida = modificarDatosGeneralesCliente.logica(con, entradaModificarDatosGeneralesCliente);

		if (salida.P_OUT_ERRORESCONTADOR > 0 ) {			
			for (EntidadManipulada Entidad : salida.P_OUT_ENTIDADES_MANIPULADAS) {
				if (Entidad.TipoEntidad.equalsIgnoreCase("DATOS GENERALES CLIENTE") ) {
					for (MensajeSalida Mensaje : Entidad.Mensajes) {
						if (Mensaje.codigo.equalsIgnoreCase("SOLICITUD_NO_EXISTE")) {
							Mensaje.tipo = MensajeSalida.MENSAJE_ADVERTENCIA;
							Mensaje.codigo = "ADVERTENCIA_DATOS_CLIENTE_GENERALES_SIN_MODIFICACIONES";
							salida.P_OUT_ERRORESCONTADOR -= 1;
							salida.P_OUT_ADVERTENCIASCONTADOR +=1 ;
						}
					}
				}
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarClienteWS.modificarDatosGeneralesCliente", fin - inicio);
		return salida;
	}

	private SalidaWBS modificarDatosGeneralesPersona(Connection con, EntradaModificarClienteWS entrada, Long idPersona, List<DatosPersonaDatosGenerales> datosGenerales) throws Exception {
		logger.debug("INICIO::ModificarClienteWS.modificarDatosGeneralesPersona");
		long inicio = System.currentTimeMillis();
		SalidaWBS salida = new SalidaWBS();
		datosGenerales.forEach(datos -> datos.IdPersona = idPersona);
		
		ModificarDatosGenerales modificarDatosGenerales = new ModificarDatosGenerales();
		EntradaModificarDatosGenerales entradaModificarDatosGenerales = new EntradaModificarDatosGenerales();
		entradaModificarDatosGenerales.p_IdUsuario = entrada.p_IdUsuario;
		entradaModificarDatosGenerales.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaModificarDatosGenerales.p_PersonaDatosGenerales = datosGenerales;
		salida = modificarDatosGenerales.logica(con, entradaModificarDatosGenerales);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarClienteWS.modificarDatosGeneralesPersona", fin - inicio);
		return salida;
	}
	
	private SalidaWBS modificarIdEjecutivoComercial(Connection con, EntradaModificarClienteWS entrada, Long idPersona,DatosPortafolio datosPortafolio) throws Exception {
		logger.debug("INICIO::ModificarClienteWS.modificarIdEjecutivoComercial");
		long inicio = System.currentTimeMillis();
		SalidaWBS salida = new SalidaWBS();
		SocioNegocio entradaValidaIntegridad = new SocioNegocio();
		ModificarIdEjecutivoComercialWS modificarIdEjecutivoComercialWS = new ModificarIdEjecutivoComercialWS();
		EntradaModificarIdEjecutivoComercialWS entradaModificarIdEjecutivoComercialWS = new EntradaModificarIdEjecutivoComercialWS();
		entradaModificarIdEjecutivoComercialWS.p_IdUsuario = entrada.p_IdUsuario;
		entradaModificarIdEjecutivoComercialWS.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaModificarIdEjecutivoComercialWS.p_IdPersona= idPersona;
		entradaModificarIdEjecutivoComercialWS.p_IdPortafolio= datosPortafolio.IdPortafolio;
		entradaModificarIdEjecutivoComercialWS.p_IdEjecutivoComercial = datosPortafolio.IdEjecutivoComercial;

     	salida = modificarIdEjecutivoComercialWS.logica(con, entradaModificarIdEjecutivoComercialWS);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarClienteWS.modificarIdEjecutivoComercial", fin - inicio);


		return salida;
	}
	
	private ValidacionEntidad selectEntidad (Connection con, SocioNegocio entradaValidaIntegridad, String nombreClase) throws Exception {
		
		logger.debug("INICIO::ModificarClienteWS.selectEntidad");
		long inicio = System.currentTimeMillis();
		SQL sql 	= new SQL();
		ValidacionEntidad salidaValidacion = new ValidacionEntidad();
		HashMap<String, Object> params 	= new HashMap<String, Object>(1);
		String queryBase = SQL_SELECT_VALIDA_INTEGRIDAD_BASE;
		String queryDIR= "";
		List<Long> idsEntidad = null;
		List<Entidad> entidad = null;
		switch (nombreClase) {
			case "TiposDocumento":
				idsEntidad = entradaValidaIntegridad.TiposDocumento
	            .stream()
	            .map(item -> item.IdDocumentoIdentidad)
	            .collect(Collectors.toList());
				queryDIR = queryBase.replaceAll("<<SELECT_CLAUSE>>","ENT.\"IdDocumentoIdentidad\"");
				queryDIR = queryDIR.replace("<<LEFT_CLIENT>>","");
				queryDIR = queryDIR.replace("<<SELECT_CLIENTE>>","");
				queryDIR = queryDIR.replace("<<IDENTIDAD>>","ENT");
				queryDIR = queryDIR.replace("<<FROM_CLAUSE>>","\"BSCL.SocioNegocio.BD::PER.DocumentoIdentidad\"");
				params.put("idEntidades", idsEntidad);
				entidad = sql.query(con, queryDIR, params, Entidad.class);
				salidaValidacion = this.validaIntegredidadPersonaEntidad(entidad, entradaValidaIntegridad.TiposDocumento, "IdDocumentoIdentidad", "DOCUMENTO IDENTIDAD");
				for(Object objetoModificar : salidaValidacion.entidadModificar) {
					if (objetoModificar instanceof ArrayList) {
					    ArrayList<TipoDocumento> listaModificar = (ArrayList<TipoDocumento>) objetoModificar;
					    salidaValidacion.socioNegocio.TiposDocumento.addAll(listaModificar);
					}
				}
				break;
			case "DireccionElectronica":
				idsEntidad = entradaValidaIntegridad.DireccionesElectronica
	            .stream()
	            .map(item -> item.IdDireccionElectronica)
	            .collect(Collectors.toList());
				queryDIR = queryBase.replaceAll("<<SELECT_CLAUSE>>","\"IdDireccionElectronica\"");
				queryDIR = queryDIR.replace("<<LEFT_CLIENT>>","");
				queryDIR = queryDIR.replace("<<SELECT_CLIENTE>>","");
				queryDIR = queryDIR.replace("<<IDENTIDAD>>","ENT");
				queryDIR = queryDIR.replace("<<FROM_CLAUSE>>","\"BSCL.SocioNegocio.BD::PER.DireccionElectronica\"");
				params.put("idEntidades", idsEntidad);
				entidad = sql.query(con, queryDIR, params, Entidad.class);
				salidaValidacion = this.validaIntegredidadPersonaEntidad(entidad, entradaValidaIntegridad.DireccionesElectronica, "IdDireccionElectronica", "DIRECCION ELECTRONICA");
				for(Object objetoModificar : salidaValidacion.entidadModificar) {
					if (objetoModificar instanceof ArrayList) {
					    ArrayList<DireccionElectronica> listaModificar = (ArrayList<DireccionElectronica>) objetoModificar;
					    salidaValidacion.socioNegocio.DireccionesElectronica.addAll(listaModificar);
					}
				}
				break;
			case "Telefono":
				idsEntidad = entradaValidaIntegridad.Telefonos
	            .stream()
	            .map(item -> item.IdTelefono)
	            .collect(Collectors.toList());
				queryDIR = queryBase.replaceAll("<<SELECT_CLAUSE>>","\"IdTelefono\"");
				queryDIR = queryDIR.replace("<<LEFT_CLIENT>>","");
				queryDIR = queryDIR.replace("<<SELECT_CLIENTE>>","");
				queryDIR = queryDIR.replace("<<IDENTIDAD>>","ENT");
				queryDIR = queryDIR.replace("<<FROM_CLAUSE>>","\"BSCL.SocioNegocio.BD::PER.Telefono\"");
				params.put("idEntidades", idsEntidad);
				entidad = sql.query(con, queryDIR, params, Entidad.class);
				salidaValidacion = this.validaIntegredidadPersonaEntidad(entidad, entradaValidaIntegridad.Telefonos, "IdTelefono", "TELEFONO");
				for(Object objetoModificar : salidaValidacion.entidadModificar) {
					if (objetoModificar instanceof ArrayList) {
					    ArrayList<Telefono> listaModificar = (ArrayList<Telefono>) objetoModificar;
					    salidaValidacion.socioNegocio.Telefonos.addAll(listaModificar);
					}
				}
				break;
			case "DireccionFisica":
				idsEntidad = entradaValidaIntegridad.DireccionesFisica
	            .stream()
	            .map(item -> item.IdDireccionFisica)
	            .collect(Collectors.toList());
				queryDIR = queryBase.replaceAll("<<SELECT_CLAUSE>>","\"IdDireccionFisica\"");
				queryDIR = queryDIR.replace("<<LEFT_CLIENT>>","");
				queryDIR = queryDIR.replace("<<SELECT_CLIENTE>>","");
				queryDIR = queryDIR.replace("<<IDENTIDAD>>","ENT");
				queryDIR = queryDIR.replace("<<FROM_CLAUSE>>","\"BSCL.SocioNegocio.BD::PER.DireccionFisica\"");
				params.put("idEntidades", idsEntidad);
				entidad = sql.query(con, queryDIR, params, Entidad.class);
				salidaValidacion = this.validaIntegredidadPersonaEntidad(entidad, entradaValidaIntegridad.DireccionesFisica, "IdDireccionFisica", "DIRECCION FISICA");
				for(Object objetoModificar : salidaValidacion.entidadModificar) {
					if (objetoModificar instanceof ArrayList) {
					    ArrayList<DireccionFisica> listaModificar = (ArrayList<DireccionFisica>) objetoModificar;
					    salidaValidacion.socioNegocio.DireccionesFisica.addAll(listaModificar);
					}
				}
				break;
			case "TarjetasBancaria":
				idsEntidad = entradaValidaIntegridad.TarjetasBancaria
	            .stream()
	            .map(item -> item.IdTarjetaBancaria)
	            .collect(Collectors.toList());
				queryDIR = queryBase.replaceAll("<<SELECT_CLAUSE>>","\"IdTarjetaBancaria\"");
				queryDIR = queryDIR.replace("<<LEFT_CLIENT>>","");
				queryDIR = queryDIR.replace("<<SELECT_CLIENTE>>","");
				queryDIR = queryDIR.replace("<<IDENTIDAD>>","ENT");
				queryDIR = queryDIR.replace("<<FROM_CLAUSE>>","\"BSCL.SocioNegocio.BD::PER.TarjetaBancaria\"");
				params.put("idEntidades", idsEntidad);
				entidad = sql.query(con, queryDIR, params, Entidad.class);
				salidaValidacion = this.validaIntegredidadPersonaEntidad(entidad, entradaValidaIntegridad.TarjetasBancaria, "IdTarjetaBancaria", "TARJETA BANCARIA");
				for(Object objetoModificar : salidaValidacion.entidadModificar) {
					if (objetoModificar instanceof ArrayList) {
					    ArrayList<TarjetaBancaria> listaModificar = (ArrayList<TarjetaBancaria>) objetoModificar;
					    salidaValidacion.socioNegocio.TarjetasBancaria.addAll(listaModificar);
					}
				}
				break;
			case "CuentasBancaria":
				idsEntidad = entradaValidaIntegridad.CuentasBancaria
	            .stream()
	            .map(item -> item.IdCuentaBancaria)
	            .collect(Collectors.toList());
				queryDIR = queryBase.replaceAll("<<SELECT_CLAUSE>>","\"IdCuentaBancaria\"");
				queryDIR = queryDIR.replace("<<LEFT_CLIENT>>","");
				queryDIR = queryDIR.replace("<<SELECT_CLIENTE>>","");
				queryDIR = queryDIR.replace("<<IDENTIDAD>>","ENT");
				queryDIR = queryDIR.replace("<<FROM_CLAUSE>>","\"BSCL.SocioNegocio.BD::PER.CuentaBancaria\"");
				params.put("idEntidades", idsEntidad);
				entidad = sql.query(con, queryDIR, params, Entidad.class);
				salidaValidacion = this.validaIntegredidadPersonaEntidad(entidad, entradaValidaIntegridad.CuentasBancaria, "IdCuentaBancaria", "CUENTA BANCARIA");
				for(Object objetoModificar : salidaValidacion.entidadModificar) {
					if (objetoModificar instanceof ArrayList) {
					    ArrayList<CuentaBancaria> listaModificar = (ArrayList<CuentaBancaria>) objetoModificar;
					    salidaValidacion.socioNegocio.CuentasBancaria.addAll(listaModificar);
					}
				}
				break;
				case "NombresFantasia":
					idsEntidad = entradaValidaIntegridad.NombresFantasia
		            .stream()
		            .map(item -> item.IdNombreFantasia)
		            .collect(Collectors.toList());
					queryDIR = queryBase.replaceAll("<<SELECT_CLAUSE>>","\"IdNombreFantasia\"");
					queryDIR = queryDIR.replace("<<LEFT_CLIENT>>","");
					queryDIR = queryDIR.replace("<<SELECT_CLIENTE>>","");
					queryDIR = queryDIR.replace("<<IDENTIDAD>>","ENT");
					queryDIR = queryDIR.replace("<<FROM_CLAUSE>>","\"BSCL.SocioNegocio.BD::PER.NombreFantasia\"");
					params.put("idEntidades", idsEntidad);
					entidad = sql.query(con, queryDIR, params, Entidad.class);
					salidaValidacion = this.validaIntegredidadPersonaEntidad(entidad, entradaValidaIntegridad.NombresFantasia, "IdNombreFantasia", "NOMBRE FANTASIA");
					for(Object objetoModificar : salidaValidacion.entidadModificar) {
						if (objetoModificar instanceof ArrayList) {
						    ArrayList<NombreFantasia> listaModificar = (ArrayList<NombreFantasia>) objetoModificar;
						    salidaValidacion.socioNegocio.NombresFantasia.addAll(listaModificar);
						}
					}
					break;
				case "Relaciones":
					idsEntidad = entradaValidaIntegridad.Relaciones
		            .stream()
		            .map(item -> item.IdRelacion)
		            .collect(Collectors.toList());
					queryDIR = queryBase.replaceAll("<<SELECT_CLAUSE>>","\"IdRelacionPersona\"");
					queryDIR = queryDIR.replace("<<LEFT_CLIENT>>","");
					queryDIR = queryDIR.replace("<<SELECT_CLIENTE>>","");
					queryDIR = queryDIR.replace("<<IDENTIDAD>>","ENT");
					queryDIR = queryDIR.replace("ENT.\"IdPersona\"","ENT.\"IdPersonaOrigen\"");
					queryDIR = queryDIR.replace("<<FROM_CLAUSE>>","\"BSCL.SocioNegocio.BD::REL.RelacionPersona\"");
					params.put("idEntidades", idsEntidad);
					entidad = sql.query(con, queryDIR, params, Entidad.class);
					salidaValidacion = this.validaIntegredidadPersonaEntidad(entidad, entradaValidaIntegridad.Relaciones, "IdRelacion", "RELACION");
					for(Object objetoModificar : salidaValidacion.entidadModificar) {
						if (objetoModificar instanceof ArrayList) {
						    ArrayList<Relacion> listaModificar = (ArrayList<Relacion>) objetoModificar;
						    salidaValidacion.socioNegocio.Relaciones.addAll(listaModificar);
						}
					}
					break;
				case "DeclaracionOrigenFondo":
					idsEntidad = entradaValidaIntegridad.DeclaracionOrigenFondo
		            .stream()
		            .map(item -> item.IdDeclaracionOrigenFondos)
		            .collect(Collectors.toList());
					queryDIR = queryBase.replaceAll("<<SELECT_CLAUSE>>","\"IdDeclaracionOrigenFondos\"");
					queryDIR = queryDIR.replace("<<LEFT_CLIENT>>","LEFT JOIN BSCL.\"BSCL.SocioNegocio.BD::CLI.Cliente\" AS CLI ON CLI.\"IdCliente\" = ENT.\"IdCliente\"");
					queryDIR = queryDIR.replace("<<IDENTIDAD>>","CLI");
					queryDIR = queryDIR.replace("<<SELECT_CLIENTE>>",",CLI.\"IdCliente\" AS \"IdCliente\"");
					queryDIR = queryDIR.replace("<<FROM_CLAUSE>>","\"BSCL.SocioNegocio.BD::CLI.DeclaracionOrigenFondos\"");
					params.put("idEntidades", idsEntidad);
					entidad = sql.query(con, queryDIR, params, Entidad.class);
					salidaValidacion = this.validaIntegredidadPersonaEntidad(entidad, entradaValidaIntegridad.DeclaracionOrigenFondo, "IdDeclaracionOrigenFondos", "DECLARACION ORIGEN FONDOS");
					for(Object objetoModificar : salidaValidacion.entidadModificar) {
						if (objetoModificar instanceof ArrayList) {
						    ArrayList<DeclaracionOrigenFondo> listaModificar = (ArrayList<DeclaracionOrigenFondo>) objetoModificar;
						    salidaValidacion.socioNegocio.DeclaracionOrigenFondo.addAll(listaModificar);
						}
					}
					break;		
				case "DatosPortafolio":
					idsEntidad = entradaValidaIntegridad.IndicadoresCliente
		            .stream()
		            .map(item -> item.IdPortafolio)
		            .collect(Collectors.toList());
					queryDIR = queryBase.replaceAll("<<SELECT_CLAUSE>>","\"IdPortafolio\"");
					queryDIR = queryDIR.replace("<<SELECT_CLIENTE>>",",CLI.\"IdCliente\" AS \"IdCliente\"");
					queryDIR = queryDIR.replace("<<FROM_CLAUSE>>","\"BSCL.Portafolio.BD::POR.Portafolio\"");
					queryDIR = queryDIR.replace("<<LEFT_CLIENT>>","LEFT JOIN BSCL.\"BSCL.SocioNegocio.BD::CLI.Cliente\" AS CLI ON CLI.\"IdCliente\" = ENT.\"IdCliente\"");
					queryDIR = queryDIR.replace("<<IDENTIDAD>>","CLI");
					params.put("idEntidades", idsEntidad);
					entidad = sql.query(con, queryDIR, params, Entidad.class);
					salidaValidacion = this.validaIntegredidadPersonaEntidad(entidad, entradaValidaIntegridad.IndicadoresCliente, "IdPortafolio", "PORTAFOLIO");
					for(Object objetoModificar : salidaValidacion.entidadModificar) {
						if (objetoModificar instanceof ArrayList) {
						    ArrayList<Indicador> listaModificar = (ArrayList<Indicador>) objetoModificar;
						    salidaValidacion.socioNegocio.IndicadoresCliente.addAll(listaModificar);
						}
					}
					break;
	}

	

	long fin = System.currentTimeMillis();
	logger.debug("FIN::ModificarClienteWS.selectEntidad", fin - inicio);
	return salidaValidacion;
	}
	
	private ValidacionEntidad validaIntegredidadPersonaEntidad (List<Entidad> entidad, List<?> entradaValidaIntegridad, String nombreColumna, String tipoEntidad) throws Exception {
		ValidacionEntidad salidaValidacion = new ValidacionEntidad();
	     List<Object> salidaModificar = new ArrayList<>();
	     String nombreClase= "";
		 for (Object entradaComparar : entradaValidaIntegridad) {
			 	nombreClase = entradaComparar.getClass().getName();
	            Class<?> clazz = entradaComparar.getClass();
	            Field campoIdInteres = clazz.getDeclaredField(nombreColumna);
	            campoIdInteres.setAccessible(true);
	            Field campoIdPersona = clazz.getDeclaredField("IdPersona");
	            Field campoIdCliente = clazz.getDeclaredField("IdCliente");
	            campoIdPersona.setAccessible(true);
	            Object valorCampoIdInteres = campoIdInteres.get(entradaComparar);
	            Object valorCampoIdPersona = campoIdPersona.get(entradaComparar);
	            Object valorCampoIdCliente = campoIdCliente.get(entradaComparar);
				Optional<Entidad> entradaQUERY = entidad.stream()
		        .filter(entidadQuery -> entidadQuery.IdEntidad.equals(valorCampoIdInteres))
		        .findFirst();
				if (entradaQUERY.isPresent()) {
				    Entidad entradaEncontrada = entradaQUERY.get();
				    if(entradaEncontrada.IdPersona.equals(valorCampoIdPersona)) {
				    	if(valorCampoIdCliente!=null && entradaEncontrada.IdCliente!=null) {
				    		if(entradaEncontrada.IdCliente.equals(valorCampoIdCliente)){
				    			salidaModificar.add(entradaComparar);
						    	salidaValidacion.entidadModificar.add(salidaModificar);
				    		}else {
				    			EntidadManipulada entidadManipulada = new EntidadManipulada();   	
					        	entidadManipulada.IdEntidad = (Long) valorCampoIdInteres;
					        	entidadManipulada.TipoEntidad = tipoEntidad;
					        	//entradaComparar.EsBorrador = "N";
					        	entidadManipulada = WebServiceUtils.noSeRelaciona(entidadManipulada);
					        	salidaValidacion.ENTIDADES_MANIPULADAS.add(entidadManipulada);
					        	salidaValidacion.ERRORESCONTADOR++;
				    		}
				    	}else {
					    	salidaModificar.add(entradaComparar);
					    	salidaValidacion.entidadModificar.add(salidaModificar);
				    	}
				    }else {
		        	EntidadManipulada entidadManipulada = new EntidadManipulada();   	
		        	entidadManipulada.IdEntidad = (Long) valorCampoIdInteres;
		        	entidadManipulada.TipoEntidad = tipoEntidad;
		        	//entradaComparar.EsBorrador = "N";
		        	entidadManipulada = WebServiceUtils.noSeRelaciona(entidadManipulada);
		        	salidaValidacion.ENTIDADES_MANIPULADAS.add(entidadManipulada);
		        	salidaValidacion.ERRORESCONTADOR++;
				    }
				} else {
					EntidadManipulada entidadManipulada = new EntidadManipulada();
		        	entidadManipulada.IdEntidad = (Long) valorCampoIdInteres;
		        	entidadManipulada.TipoEntidad = tipoEntidad;
		        	//direccion.EsBorrador = "N";
		        	entidadManipulada = WebServiceUtils.noExisteIdEntidad(entidadManipulada);
		        	salidaValidacion.ENTIDADES_MANIPULADAS.add(entidadManipulada);
		        	salidaValidacion.ERRORESCONTADOR++;
				}
	        }
       
	return salidaValidacion;
	}

	public static void main(String args[]) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			Gson gson = Utils.getGsonBuilder();

			String desa = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1,\"p_ClaveUsuario\":\"6b86b273ff34fce19d6b804eff5a3f5747ada4eaa22f1d49c01e52ddb7875b4b\",\"p_SocioNegocio\":[{\"IdInterno\":0,\"TipoDocumentoIdentidad\":\"R\",\"DocumentoIdentidad\":\"40162369-8\",\"DireccionesElectronica\":[{\"Accion\":\"MODIFICAR\",\"IdDireccionElectronica\":875699,\"CodigoProposito\":\"PART\",\"Direccion\":\"PRUEBAWS@SURA.CL\",\"Comentario\":\"TEST WEBSERVICE\",\"Predeterminado\":\"S\",\"IdEstado\":\"ACT\"}]}]}";
			String jsonAgregar = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1,\"p_ClaveUsuario\":\"6b86b273ff34fce19d6b804eff5a3f5747ada4eaa22f1d49c01e52ddb7875b4b\",\"p_SocioNegocio\":[{\"IdInterno\":0,\"TipoDocumentoIdentidad\":\"R\",\"DocumentoIdentidad\":\"16657034-4\",\"DireccionesElectronica\":[{\"Accion\":\"AGREGAR\",\"CodigoProposito\":\"PART\",\"Direccion\":\"PRUEBAWS1@SURA.CL\",\"Comentario\":\"TEST WEBSERVICE\",\"Predeterminado\":\"S\",\"IdEstado\":\"ACT\"}]}]}";
			String pruebaModificar = "{\r\n"
					+ "    \"p_IdEmpresa\": 1,\r\n"
					+ "    \"p_IdUsuario\": 6,\r\n"
					+ "	   \"p_ActualizarIndicadorFormaCobro\": \"N\", \r\n"
					+ "    \"p_SocioNegocio\": [\r\n"
					+ "        {\r\n"
					+ "            \"IdInterno\": 0,\r\n"
					+ "            \"TipoDocumentoIdentidad\": \"R\",\r\n"
					+ "            \"DocumentoIdentidad\": \"10000562-K\",\r\n"
					+ "			   \"DatosClienteDatosGenerales\": [\r\n"
					+ "                {\r\n"
					+ " 				\"CodigoEjecutivo\": \"335\","
	                + " 				\"IdCategoriaInversionista\": \"MODERADO\" \r\n"
					+ "                }\r\n"
					+ "            ],\r\n"
					+ "			   \"DatosPortafolio\": [\r\n"
					+ "				{\r\n"
					+ "					\"IdPortafolio\": 481064,\r\n"
					+ "					\"Indicadores\": [\r\n"
					+ "						{\r\n"
					+ "							\"ValorIndicador\": \"OPCION 4\",\r\n"
					+ "							\"CodigoTipoIndicador\": \"COBROCUST\"\r\n"
					+ "						}\r\n"
					+ "					]\r\n"
					+ "				}\r\n"
					+ "			]"
					+ "        }\r\n"
					+ "    ]\r\n"
					+ "}";
//						
			
			String pruebaEmpleador = "{\r\n"
					+ "    \"p_IdEmpresa\": 1,\r\n"
					+ "    \"p_IdUsuario\": 4,\r\n"
					+ "    \"p_SocioNegocio\": [\r\n"
					+ "        {\r\n"
					+ "            \"IdInterno\": 0,\r\n"
					+ "            \"TipoDocumentoIdentidad\": \"R\",\r\n"
					+ "            \"DocumentoIdentidad\": \"35925132-7\",\r\n"
					+ "            \"DatosPersonaDatosGenerales\": [\r\n"
					+ "                {\r\n"
					+ " 				\"Cargo\": \"DIR\","
	                + " 				 \"IdSituacionLaboral\": \"EMPL\","
					+ "                   \"Empleador\":"
					+ "						{ \r\n "
					+ "            			\"IdTipoPersona\": \"N\",\r\n"
					+ "            			\"TipoDocumentoIdentidad\": \"R\",\r\n"
					+ "            			\"DocumentoIdentidad\": \"38459434-4\",\r\n"
					+ "            			\"Nombres\": \"ESTEBAN\",\r\n"
					+ "            			\"ApellidoPaterno\": \"FUENTES\",\r\n"
					+ "            			\"ApellidoMaterno\": \"ALCARRAZ\",\r\n"
					+ "            			\"RazonSocial\": \"PRUEBA_1\" "
					+ "				       }\r\n "
					+ "                }\r\n"
					+ "            ]\r\n"
					+ "        }\r\n"
					+ "    ]\r\n"
					+ "}";
			
			
			
			String jsion = "{\r\n"
					+ "    \"p_IdEmpresa\": 1,\r\n"
					+ "    \"p_IdUsuario\": 4,\r\n"
					+ "    \"p_SocioNegocio\": [\r\n"
					+ "        {\r\n"
					+ "            \"IdInterno\": 0,\r\n"
					+ "            \"TipoDocumentoIdentidad\": \"R\",\r\n"
					+ "            \"DocumentoIdentidad\": \"24511698-7\",\r\n"
					+ "            \"DeclaracionOrigenFondo\": [\r\n"
					+ "                {\r\n"
					+ "                    \"Accion\": \"MODIFICAR\",\r\n"
					+ "                    \"IdDeclaracionOrigenFondos\": 10147,\r\n"
					+ "                    \"IdTipoOperacionOrigenFondos\": \"APORFM\",\r\n"
					+ "                    \"FechaDeclaracion\": \"2027-01-01\",\r\n"
					+ "                    \"Comentario\": \"asdasdasd\",\r\n"
					+ "                    \"OperacionInscripcion\": \"FICHA\"\r\n"
					+ "            ]\r\n"
					+ "        }\r\n"
					+ "    ]\r\n"
					+ "}";
			System.out.println("ENTRADA=");
			System.out.println(pruebaModificar);
			
			EntradaModificarClienteWS entradaModificarClienteWS = gson.fromJson(desa, EntradaModificarClienteWS.class);
			ModificarClienteWS modificarClienteWS = new ModificarClienteWS();
			try {
				SalidaWBS salida = modificarClienteWS.logica(con, entradaModificarClienteWS);

				//con.commit();
				con.rollback();
				con.close();
				System.out.println("SALIDA=");
				System.out.println(salida.toString());
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
				con.close();
			}
		}
		System.out.println("== END ==");
	}

}