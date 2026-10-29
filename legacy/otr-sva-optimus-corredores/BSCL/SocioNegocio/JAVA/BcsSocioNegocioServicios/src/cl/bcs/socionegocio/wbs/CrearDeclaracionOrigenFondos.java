package cl.bcs.socionegocio.wbs;

import java.math.BigDecimal;
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
import cl.bcs.socionegocio.cli.CrearDeclaracionOrigenFondos.SalidaCrearDeclaracionOrigenFondos;
import cl.bcs.socionegocio.wbs.WebServiceUtils.EntidadManipulada;
import cl.bcs.socionegocio.wbs.WebServiceUtils.SalidaWBS;
import cl.bcs.socionegocio.wbs.pojo.DeclaracionOrigenFondo;
import cl.bcs.socionegocio.wbs.pojo.EntradaAdministrarWS;
import cl.bcs.socionegocio.wbs.pojo.SalidaAdministrarWS;
import cl.bcs.socionegocio.wbs.pojo.TipoPersona;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearDeclaracionOrigenFondos implements JavaCall {
	static final Logger logger = LogManager.getLogger(CrearDeclaracionOrigenFondos.class);
	
	private String SQL_SELECT_PERSONA = loadResourceToString("cl/bcs/socionegocio/wbs/sql/ConsultarPersona_SelectPersona.sql");


	public static class EntradaCrearDeclaracionOrigenFondos extends EntradaAdministrarWS {
		public List<DeclaracionOrigenFondo> p_DeclaracionOrigenFondos;
		
		public EntradaCrearDeclaracionOrigenFondos(){
			this.p_DeclaracionOrigenFondos = new ArrayList<DeclaracionOrigenFondo>();
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaCrearDeclaracionOrigenFondos jin = gson.fromJson(in.toString(), EntradaCrearDeclaracionOrigenFondos.class);
		SalidaWBS out = logica(conn, jin);

		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public SalidaWBS logica(Connection conn, EntradaCrearDeclaracionOrigenFondos entrada) throws Exception {
		logger.info("INICIO::CrearDeclaracionOrigenFondos");
		long inicioServicio = System.currentTimeMillis();
		SalidaWBS salida = new SalidaWBS();
		
		entrada.p_DeclaracionOrigenFondos = asignarTipoCliente(conn,entrada);
		
		for(DeclaracionOrigenFondo declaracionOrigenFondo : entrada.p_DeclaracionOrigenFondos) {
			SalidaAdministrarWS out = new SalidaAdministrarWS();
		    EntidadManipulada entidadCreada = new EntidadManipulada();
		    entidadCreada.IdEntidad = declaracionOrigenFondo.IdDeclaracionOrigenFondos;
		    entidadCreada.TipoEntidad = "DECLARACION ORIGEN FONDOS";
		    declaracionOrigenFondo.EsBorrador = "N";
		    
		    if(declaracionOrigenFondo.TipoEnrolamiento == null) {
				entidadCreada = WebServiceUtils.noEsCliente(entidadCreada);
				salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadCreada);
				continue;
		    }
		    
		    FormularioCanal formulario = new FormularioCanal();
		    formulario.IdEmpresa = entrada.p_IdEmpresa;
		    formulario.CodigoFormulario = declaracionOrigenFondo.CodigoFormulario;
		    formulario.IdTipoCanal = entrada.p_IdTipoCanal;
		    
		    List<DeclaracionOrigenFondo> listaDeUno = new ArrayList<>();
		    listaDeUno.add(declaracionOrigenFondo);
		    out = entrada.validarCamposObligatoriosEntrada(conn, listaDeUno, DeclaracionOrigenFondo.class, formulario, out);
		    if(out.P_OUT_ERRORESCONTADOR > 0) {
				entidadCreada.Mensajes.addAll(out.P_OUT_MENSAJES);
				salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadCreada);
				continue;
		    }
		    
		    cl.bcs.socionegocio.cli.CrearDeclaracionOrigenFondos crearDeclaracionOrigenFondos = new cl.bcs.socionegocio.cli.CrearDeclaracionOrigenFondos();
		    cl.bcs.socionegocio.cli.CrearDeclaracionOrigenFondos.EntradaCrearDeclaracionOrigenFondos entradaCrearDeclaracionOrigenFondos = new cl.bcs.socionegocio.cli.CrearDeclaracionOrigenFondos.EntradaCrearDeclaracionOrigenFondos();
			entradaCrearDeclaracionOrigenFondos.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaCrearDeclaracionOrigenFondos.p_IdModulo = entrada.p_IdModulo;
		    entradaCrearDeclaracionOrigenFondos.p_IdUsuario = entrada.p_IdUsuario;
			entradaCrearDeclaracionOrigenFondos.p_IdTipoOperacionOrigenFondos = declaracionOrigenFondo.IdTipoOperacionOrigenFondos;
			entradaCrearDeclaracionOrigenFondos.p_FechaDeclaracion = String.valueOf(declaracionOrigenFondo.FechaDeclaracion);
			entradaCrearDeclaracionOrigenFondos.p_Comentario = declaracionOrigenFondo.Comentario;
			entradaCrearDeclaracionOrigenFondos.p_IdEstado = "ACT";
			entradaCrearDeclaracionOrigenFondos.p_OperacionInscripcion = declaracionOrigenFondo.OperacionInscripcion;
			entradaCrearDeclaracionOrigenFondos.p_IdCliente = declaracionOrigenFondo.IdCliente;

			for (cl.bcs.socionegocio.wbs.pojo.DetalleOrigenFondo origenFondo : declaracionOrigenFondo.DetalleOrigenFondo) {
				cl.bcs.socionegocio.cli.CrearDeclaracionOrigenFondos.Detalle detalle = new cl.bcs.socionegocio.cli.CrearDeclaracionOrigenFondos.Detalle();
				detalle.IdTipoOrigenFondo = origenFondo.IdTipoOrigenFondo;
				detalle.Valor = BigDecimal.valueOf(origenFondo.Valor);
				detalle.Otros = origenFondo.Otros;
				entradaCrearDeclaracionOrigenFondos.p_Detalle.add(detalle);
			}

			SalidaCrearDeclaracionOrigenFondos salidaCrearDeclaracionOrigenFondos = crearDeclaracionOrigenFondos.logica(conn, entradaCrearDeclaracionOrigenFondos);
			
			if (!salidaCrearDeclaracionOrigenFondos.P_OUT_MENSAJES.isEmpty()) {
				entidadCreada.Mensajes.addAll(salidaCrearDeclaracionOrigenFondos.P_OUT_MENSAJES);
				salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadCreada);
				continue;
			}

			if (entidadCreada.Mensajes.isEmpty()) {
				entidadCreada = WebServiceUtils.agregarMensajeOk(entidadCreada, "CREADA");
				salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadCreada);
			}
			
		}
		
		salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ENTIDADES_MANIPULADAS.parallelStream().reduce(0L,(sum, ent) -> sum += ent.Mensajes.parallelStream()
				.filter(mensaje -> PlataformaUtils.MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo)).count(),
				(sum1, sum2) -> sum1 + sum2);

		if (salida.P_OUT_ERRORESCONTADOR > 0) {
			conn.rollback();
		}
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearDeclaracionOrigenFondos", finServicio - inicioServicio);
		return salida;
	}
	
	private List<TipoPersona> obtenerTiposPersona(Connection con, EntradaCrearDeclaracionOrigenFondos entrada) throws SQLException {
		logger.debug("INICIO::CrearDeclaracionOrigenFondos.obtenerTiposPersona");
		long inicio = System.currentTimeMillis();
		
		List<Long> idsPersonas = entrada.p_DeclaracionOrigenFondos.stream()
				  .map(persona -> persona.IdPersona)
				  .distinct()
				  .collect(Collectors.toList());
		
		HashMap<String, Object> params = new HashMap<String, Object>(2);
		params.put("IdPersonas", idsPersonas);
		
		SQL sql = new SQL();
		List<TipoPersona> tiposPersona = sql.query(con, SQL_SELECT_PERSONA, params, TipoPersona.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearDeclaracionOrigenFondos.obtenerTiposPersona", fin - inicio);
		return tiposPersona;
	}

	private List<DeclaracionOrigenFondo> asignarTipoCliente(Connection con, EntradaCrearDeclaracionOrigenFondos entrada) throws Exception {
		logger.debug("INICIO::CrearDeclaracionOrigenFondos.asignarTipoPersona");
		long inicio = System.currentTimeMillis();
		
		List<TipoPersona> tipoPersonas = obtenerTiposPersona(con, entrada);
		
		entrada.p_DeclaracionOrigenFondos.parallelStream().forEach(entidad -> {
			TipoPersona tipo = tipoPersonas.parallelStream().filter(tipoPersona -> tipoPersona.IdPersona.equals(entidad.IdPersona)).findFirst().orElse(new TipoPersona());
			entidad.CodigoFormulario = WebServiceUtils.obtenerCodigoFormulario(con, tipo.Tipo, tipo.TipoEnrolamiento, "DECLARACION_ORIGEN_FONDOS");
			entidad.TipoEnrolamiento = tipo.TipoEnrolamiento;
			entidad.IdCliente = tipo.IdCliente;
		});
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearDeclaracionOrigenFondos.asignarTipoPersona", fin - inicio);
		return entrada.p_DeclaracionOrigenFondos;
	}

}
