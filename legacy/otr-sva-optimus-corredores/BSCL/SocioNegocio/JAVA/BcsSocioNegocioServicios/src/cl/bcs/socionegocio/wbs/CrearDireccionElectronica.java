package cl.bcs.socionegocio.wbs;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.frm.pojo.FormularioCanal;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.per.CrearDireccionElectronicaMasivo;
import cl.bcs.socionegocio.per.CrearDireccionElectronicaMasivo.EntradaCrearDireccionElectronicaMasivo;
import cl.bcs.socionegocio.per.CrearDireccionElectronicaMasivo.SalidaCrearDireccionElectronicaMasivo;
import cl.bcs.socionegocio.wbs.WebServiceUtils.EntidadManipulada;
import cl.bcs.socionegocio.wbs.WebServiceUtils.SalidaWBS;
import cl.bcs.socionegocio.wbs.pojo.EntradaAdministrarWS;
import cl.bcs.socionegocio.wbs.pojo.SalidaAdministrarWS;
import cl.bcs.socionegocio.wbs.pojo.DireccionElectronica;
import cl.bcs.socionegocio.wbs.pojo.TipoPersona;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearDireccionElectronica implements JavaCall {
	static final Logger logger = LogManager.getLogger(CrearDireccionElectronica.class);
	private String SQL_SELECT_PERSONA = loadResourceToString("cl/bcs/socionegocio/wbs/sql/ConsultarPersona_SelectPersona.sql");

	public static class EntradaCrearDireccionElectronica extends EntradaAdministrarWS {
		public List<DireccionElectronica> p_DireccionesElectronicas;

		public EntradaCrearDireccionElectronica() {
			super();
			this.p_DireccionesElectronicas = new ArrayList<>();
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaCrearDireccionElectronica jin = gson.fromJson(in.toString(), EntradaCrearDireccionElectronica.class);
		SalidaWBS out = logica(conn, jin);

		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public SalidaWBS logica(Connection conn, EntradaCrearDireccionElectronica entrada) throws Exception {
		logger.info("INICIO::CrearDireccionElectronica");
		long inicioServicio = System.currentTimeMillis();
		SalidaWBS salida = new SalidaWBS();

		entrada.p_DireccionesElectronicas = asignarTipoPersona(conn, entrada);

		for (DireccionElectronica direccionElectronica : entrada.p_DireccionesElectronicas) {
			SalidaAdministrarWS out = new SalidaAdministrarWS();
			EntidadManipulada entidadCreada = new EntidadManipulada();
			entidadCreada.IdEntidad = direccionElectronica.IdCliente;
			entidadCreada.TipoEntidad = "DIRECCION ELECTRONICA";
			direccionElectronica.EsBorrador = "N";
			
			if (direccionElectronica.TipoEnrolamiento == null) {
				entidadCreada = WebServiceUtils.noEsCliente(entidadCreada);
				salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadCreada);
				continue;
			}

			FormularioCanal formulario = new FormularioCanal();
			formulario.IdEmpresa = entrada.p_IdEmpresa;
			formulario.CodigoFormulario = direccionElectronica.CodigoFormulario;
			formulario.IdTipoCanal = entrada.p_IdTipoCanal;

			List<DireccionElectronica> listaDeUno = new ArrayList<>();
			listaDeUno.add(direccionElectronica);

			out = entrada.validarCamposObligatoriosEntrada(conn, listaDeUno, DireccionElectronica.class, formulario,out);
			if (out.P_OUT_ERRORESCONTADOR > 0) {
				entidadCreada.Mensajes.addAll(out.P_OUT_MENSAJES);
				salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadCreada);
				continue;
			}

			CrearDireccionElectronicaMasivo crearDireccionElectronicaMasivo = new CrearDireccionElectronicaMasivo();
			EntradaCrearDireccionElectronicaMasivo entradaCrearDireccionElectronicaMasivo = new EntradaCrearDireccionElectronicaMasivo();
			entradaCrearDireccionElectronicaMasivo.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaCrearDireccionElectronicaMasivo.p_IdModulo = entrada.p_IdModulo;
			entradaCrearDireccionElectronicaMasivo.p_IdUsuario = entrada.p_IdUsuario;
			entradaCrearDireccionElectronicaMasivo.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
			entradaCrearDireccionElectronicaMasivo.p_DireccionesElectronica = obtenerDirecciones(listaDeUno);

			SalidaCrearDireccionElectronicaMasivo salidaCrearDireccionElectronicaMasivo = new SalidaCrearDireccionElectronicaMasivo();
			salidaCrearDireccionElectronicaMasivo = crearDireccionElectronicaMasivo.logica(conn,
					entradaCrearDireccionElectronicaMasivo);
			if (!salidaCrearDireccionElectronicaMasivo.P_OUT_MENSAJES.isEmpty()) {
				entidadCreada.Mensajes.addAll(salidaCrearDireccionElectronicaMasivo.P_OUT_MENSAJES);
				salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadCreada);
				continue;
			}

			if (entidadCreada.Mensajes.isEmpty()) {
				entidadCreada = WebServiceUtils.agregarMensajeOk(entidadCreada, "CREADA");
				salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadCreada);
			}
		}

		salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ENTIDADES_MANIPULADAS.parallelStream().reduce(0L,
				(sum, ent) -> sum += ent.Mensajes.parallelStream()
						.filter(mensaje -> PlataformaUtils.MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo)).count(),
				(sum1, sum2) -> sum1 + sum2);
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearDireccionElectronica", finServicio - inicioServicio);
		return salida;
	}

	private List<TipoPersona> obtenerTiposPersona(Connection con, EntradaCrearDireccionElectronica entrada)
			throws SQLException {
		logger.debug("INICIO::CrearDireccionElectronica.obtenerTiposPersona");
		long inicio = System.currentTimeMillis();

		List<Long> idsPersonas = entrada.p_DireccionesElectronicas.stream().map(persona -> persona.IdPersona).distinct()
				.collect(Collectors.toList());

		HashMap<String, Object> params = new HashMap<String, Object>(2);
		params.put("IdPersonas", idsPersonas);

		SQL sql = new SQL();
		List<TipoPersona> tiposPersona = sql.query(con, SQL_SELECT_PERSONA, params, TipoPersona.class);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearDireccionElectronica.obtenerTiposPersona", fin - inicio);
		return tiposPersona;
	}

	private List<cl.bcs.socionegocio.per.CrearDireccionElectronicaMasivo.DireccionElectronica> obtenerDirecciones(
			List<DireccionElectronica> direcciones) {
		List<cl.bcs.socionegocio.per.CrearDireccionElectronicaMasivo.DireccionElectronica> direccionesCreacion = direcciones
				.parallelStream().map(direccion -> {
					cl.bcs.socionegocio.per.CrearDireccionElectronicaMasivo.DireccionElectronica direccionElectronica = new cl.bcs.socionegocio.per.CrearDireccionElectronicaMasivo.DireccionElectronica();
					direccionElectronica.IdInterno = direccion.IdInterno;
					direccionElectronica.IdDireccionElectronica = direccion.IdDireccionElectronica;
					direccionElectronica.Direccion = direccion.Direccion;
					direccionElectronica.CodigoProposito = direccion.CodigoProposito;
					direccionElectronica.Comentario = direccion.Comentario;
					direccionElectronica.IdEstado = direccion.IdEstado;
					direccionElectronica.IdPersona = direccion.IdPersona;
					direccionElectronica.Predeterminado = direccion.Predeterminado;
					direccionElectronica.IdUsuarioCreador = direccion.IdUsuarioCreador;
					direccionElectronica.IdEstadoAutorizacion = direccion.IdEstadoAutorizacion;
					return direccionElectronica;
				}).collect(Collectors.toList());

		return direccionesCreacion;
	}

	private List<DireccionElectronica> asignarTipoPersona(Connection con, EntradaCrearDireccionElectronica entrada)
			throws SQLException {
		logger.debug("INICIO::CrearDireccionElectronica.asignarTipoPersona");
		long inicio = System.currentTimeMillis();

		List<TipoPersona> tipoPersonas = obtenerTiposPersona(con, entrada);

		entrada.p_DireccionesElectronicas.parallelStream().forEach(tarjeta -> {
			TipoPersona tipo = tipoPersonas.parallelStream()
					.filter(tipoPersona -> tipoPersona.IdPersona.equals(tarjeta.IdPersona)).findFirst()
					.orElse(new TipoPersona());
			tarjeta.CodigoFormulario = WebServiceUtils.obtenerCodigoFormulario(con, tipo.Tipo, tipo.TipoEnrolamiento,"DIRECCION_ELECTRONICA");
			tarjeta.TipoEnrolamiento = tipo.TipoEnrolamiento;
			tarjeta.IdCliente = tipo.IdCliente;
		});

		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearDireccionElectronica.asignarTipoPersona", fin - inicio);
		return entrada.p_DireccionesElectronicas;
	}

	public static void main(String args[]) throws SQLException {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();

			String jsonEntrada = "{\r\n"
					+ "  \"p_DireccionesElectronicas\": [\r\n"
					+ "    {\r\n"
					+ "      \"IdInterno\": 0,\r\n"
					+ "      \"IdPersona\": 370978,\r\n"
					+ "      \"Direccion\": \"\",\r\n"
					+ "      \"CodigoProposito\": \"PART\",\r\n"
					+ "      \"Comentario\": \"ASDASDASDASD\",\r\n"
					+ "      \"IdEstado\": \"ACT\",\r\n"
					+ "      \"Predeterminado\": \"N\"\r\n"
					+ "    },\r\n"
					+ "    {\r\n"
					+ "      \"IdInterno\": 0,\r\n"
					+ "      \"IdPersona\": 370978,\r\n"
					+ "      \"Direccion\": \"CORREO@MAIL.COM\",\r\n"
					+ "      \"CodigoProposito\": \"\",\r\n"
					+ "      \"Comentario\": \"ASDASDASDASD\",\r\n"
					+ "      \"IdEstado\": \"ACT\",\r\n"
					+ "      \"Predeterminado\": \"N\"\r\n"
					+ "    },\r\n"
					+ "    {\r\n"
					+ "      \"IdInterno\": 0,\r\n"
					+ "      \"IdPersona\": 370978,\r\n"
					+ "      \"Direccion\": \"CORREO@MAIL.COM\",\r\n"
					+ "      \"CodigoProposito\": \"PART\",\r\n"
					+ "      \"Comentario\": \"\",\r\n"
					+ "      \"IdEstado\": \"ACT\",\r\n"
					+ "      \"Predeterminado\": \"N\"\r\n"
					+ "    }\r\n"
					+ "  ],\r\n"
					+ "  \"p_IdEmpresa\": 1,\r\n"
					+ "  \"p_IdUsuario\": 9\r\n"
					+ "}\r\n"
					+ "";
			
			String jsonEntradaFullJur = "{\r\n"
					+ "  \"p_DireccionesElectronicas\": [\r\n"
					+ "    {\r\n"
					+ "      \"IdPersona\": 1732307,\r\n"
					+ "      \"Direccion\": \"\",\r\n"
					+ "      \"Comentario\": \"\",\r\n"
					+ "      \"CodigoProposito\": \"\",\r\n"
					+ "      \"IdEstado\": \"ACT\",\r\n"
					+ " 	 \"Predeterminado\": \"N\"\r\n"
					+ "    }\r\n"
					+ "  ],\r\n"
					+ "  \"p_IdEmpresa\": 1,\r\n"
					+ "  \"p_IdUsuario\": 4\r\n"
					+ "}";
			EntradaCrearDireccionElectronica entradaCrearDireccionElectronica = gson.fromJson(jsonEntradaFullJur,
					EntradaCrearDireccionElectronica.class);
			CrearDireccionElectronica crearDireccionElectronica = new CrearDireccionElectronica();
			try {
				SalidaWBS salida = crearDireccionElectronica.logica(con, entradaCrearDireccionElectronica);
				 con.commit();
				//con.rollback();
				con.close();
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
