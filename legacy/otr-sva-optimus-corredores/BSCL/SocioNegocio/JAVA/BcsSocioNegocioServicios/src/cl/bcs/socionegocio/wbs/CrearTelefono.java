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
import cl.bcs.socionegocio.per.CrearTelefonoMasivo;
import cl.bcs.socionegocio.per.CrearTelefonoMasivo.EntradaCrearTelefonoMasivo;
import cl.bcs.socionegocio.per.CrearTelefonoMasivo.SalidaCrearTelefonoMasivo;
import cl.bcs.socionegocio.wbs.WebServiceUtils.EntidadManipulada;
import cl.bcs.socionegocio.wbs.WebServiceUtils.SalidaWBS;
import cl.bcs.socionegocio.wbs.pojo.EntradaAdministrarWS; 
import cl.bcs.socionegocio.wbs.pojo.SalidaAdministrarWS;
import cl.bcs.socionegocio.wbs.pojo.Telefono;
import cl.bcs.socionegocio.wbs.pojo.TipoPersona;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearTelefono implements JavaCall {
	static final Logger logger = LogManager.getLogger(CrearTelefono.class);
	private String SQL_SELECT_PERSONA = loadResourceToString("cl/bcs/socionegocio/wbs/sql/ConsultarPersona_SelectPersona.sql");
	
	public static class EntradaCrearTelefono extends EntradaAdministrarWS {
		public List<Telefono> p_Telefonos;
		
		public EntradaCrearTelefono() {
			super();
			this.p_Telefonos = new ArrayList<>();
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaCrearTelefono jin = gson.fromJson(in.toString(), EntradaCrearTelefono.class);
		SalidaWBS out = logica(conn, jin);

		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaWBS logica(Connection conn, EntradaCrearTelefono entrada) throws Exception {
		logger.info("INICIO::CrearTelefono");
		long inicioServicio = System.currentTimeMillis();
		SalidaWBS salida = new SalidaWBS();

		entrada.p_Telefonos = asignarTipoPersona(conn, entrada);

		for (Telefono telefono : entrada.p_Telefonos) {
			SalidaAdministrarWS out = new SalidaAdministrarWS();
			EntidadManipulada entidadCreada = new EntidadManipulada();
			entidadCreada.IdEntidad = telefono.IdTelefono;
			entidadCreada.TipoEntidad = "TELEFONO";
			telefono.EsBorrador = "N";
			
			if (telefono.TipoEnrolamiento == null) {
				entidadCreada = WebServiceUtils.noEsCliente(entidadCreada);
				salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadCreada);
				continue;
			}

			FormularioCanal formulario = new FormularioCanal();
			formulario.IdEmpresa = entrada.p_IdEmpresa;
			formulario.CodigoFormulario = telefono.CodigoFormulario;
			formulario.IdTipoCanal = entrada.p_IdTipoCanal;

			List<Telefono> listaDeUno = new ArrayList<>();
			listaDeUno.add(telefono);

			out = entrada.validarCamposObligatoriosEntrada(conn, listaDeUno, Telefono.class, formulario, out);
			if (out.P_OUT_ERRORESCONTADOR > 0) {
				entidadCreada.Mensajes.addAll(out.P_OUT_MENSAJES);
				salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadCreada);
				continue;
			}

			CrearTelefonoMasivo crearTelefonoMasivo = new CrearTelefonoMasivo();
			EntradaCrearTelefonoMasivo entradaCrearTelefonoMasivo = new EntradaCrearTelefonoMasivo();
			entradaCrearTelefonoMasivo.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaCrearTelefonoMasivo.p_IdModulo = entrada.p_IdModulo;
			entradaCrearTelefonoMasivo.p_IdUsuario = entrada.p_IdUsuario;
			entradaCrearTelefonoMasivo.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
			entradaCrearTelefonoMasivo.p_Telefonos = obtenerTelefonos(listaDeUno);

			SalidaCrearTelefonoMasivo salidaCrearTelefonoMasivo = new SalidaCrearTelefonoMasivo();
			salidaCrearTelefonoMasivo = crearTelefonoMasivo.logica(conn,
					entradaCrearTelefonoMasivo);
			if (!salidaCrearTelefonoMasivo.P_OUT_MENSAJES.isEmpty()) {
				entidadCreada.Mensajes.addAll(salidaCrearTelefonoMasivo.P_OUT_MENSAJES);
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
		logger.info("FIN::CrearTelefono", finServicio - inicioServicio);
		return salida;
	}
	
	private List<cl.bcs.socionegocio.per.CrearTelefonoMasivo.Telefono> obtenerTelefonos(List<Telefono> telefonos) {
	    List<cl.bcs.socionegocio.per.CrearTelefonoMasivo.Telefono> tarjetasCreacion =  telefonos.parallelStream().map(telefono -> {
			cl.bcs.socionegocio.per.CrearTelefonoMasivo.Telefono tel = new cl.bcs.socionegocio.per.CrearTelefonoMasivo.Telefono();
			tel.IdInterno = telefono.IdInterno;
			tel.IdPersona = telefono.IdPersona;
			tel.IdInterno = telefono.IdInterno;
			tel.IdTelefono = telefono.IdTelefono;
			tel.IdPais = telefono.IdPais;
			tel.IdTipoTelefono = telefono.IdTipoTelefono;
			tel.Numero = telefono.Numero;
			tel.Anexo = telefono.Anexo;
			tel.CodigoProposito = telefono.CodigoProposito;
			tel.Comentario = telefono.Comentario;
			tel.IdEstado = telefono.IdEstado;
			tel.IdPersona = telefono.IdPersona;
			tel.Predeterminado = telefono.Predeterminado;
			tel.IdUsuarioCreador = telefono.IdUsuarioCreador;
			tel.IdEstadoAutorizacion = telefono.IdEstadoAutorizacion;
			return tel;
		}).collect(Collectors.toList());
	    
	    return tarjetasCreacion;
	}

	private List<TipoPersona> obtenerTiposPersona(Connection con, EntradaCrearTelefono entrada) throws SQLException {
		logger.debug("INICIO::CrearTelefono.obtenerTiposPersona");
		long inicio = System.currentTimeMillis();
		
		List<Long> idsPersonas = entrada.p_Telefonos.stream()
				  .map(persona -> persona.IdPersona)
				  .distinct()
				  .collect(Collectors.toList());
		
		HashMap<String, Object> params = new HashMap<String, Object>(2);
		params.put("IdPersonas", idsPersonas);
		
		SQL sql = new SQL();
		List<TipoPersona> tiposPersona = sql.query(con, SQL_SELECT_PERSONA, params, TipoPersona.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearTelefono.obtenerTiposPersona", fin - inicio);
		return tiposPersona;
	}
	
	private List<Telefono> asignarTipoPersona(Connection con, EntradaCrearTelefono entrada) throws SQLException{
		logger.debug("INICIO::CrearTarjetaBancaria.asignarTipoPersona");
		long inicio = System.currentTimeMillis();
		
		List<TipoPersona> tipoPersonas = obtenerTiposPersona(con, entrada);
		
		entrada.p_Telefonos.parallelStream().forEach(telefono -> {
			TipoPersona tipo = tipoPersonas.parallelStream().filter(tipoPersona -> tipoPersona.IdPersona.equals(telefono.IdPersona)).findFirst().orElse(new TipoPersona());
			telefono.CodigoFormulario = WebServiceUtils.obtenerCodigoFormulario(con, tipo.Tipo, tipo.TipoEnrolamiento, "TELEFONO");
			telefono.TipoEnrolamiento = tipo.TipoEnrolamiento;
			telefono.IdCliente = tipo.IdCliente;
		});
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearTarjetaBancaria.asignarTipoPersona", fin - inicio);
		return entrada.p_Telefonos;
	}
	
	public static void main(String args[]) throws SQLException {
		System.out.println("== TEST ==");		                  
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();			
			
			String jsonEntrada = "{\r\n"
					+ "  \"p_Telefonos\": [\r\n"
					+ "    {\r\n"
					+ "      \"IdInterno\": 0,\r\n"
					+ "      \"IdPersona\": 1732307,\r\n"
					+ "      \"Anexo\": \"\",\r\n"
					+ "      \"Comentario\": \"\",\r\n"
					+ "      \"IdEstado\": \"\",\r\n"
					+ "      \"IdTipoTelefono\": \"\",\r\n"
					+ "      \"Numero\": \"\",\r\n"
					+ "      \"IdPais\": 0,\r\n"
					+ "      \"CodigoProposito\": \"\",\r\n"
					+ "      \"Predeterminado\": \"S\"\r\n"
					+ "    }\r\n"
					+ "  ],\r\n"
					+ "  \"p_IdEmpresa\": 1,\r\n"
					+ "  \"p_IdUsuario\": 9\r\n"
					+ "}\r\n"
					+ "";
			EntradaCrearTelefono entradaCrearTelefono = gson.fromJson(jsonEntrada, EntradaCrearTelefono.class);
			CrearTelefono crearTarjetaBancaria = new CrearTelefono();				
			try {
			    SalidaWBS salida= crearTarjetaBancaria.logica(con, entradaCrearTelefono);
				//con.commit();
				con.rollback();
				con.close();
				System.out.println(salida.toString());
			}
			catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
				con.close();
			}			
    	}
		System.out.println("== END ==");
	}
}
