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
import cl.bcs.socionegocio.per.CrearPersonaNombreFantasiaMasivo;
import cl.bcs.socionegocio.per.CrearPersonaNombreFantasiaMasivo.EntradaCrearPersonaNombreFantasiaMasivo;
import cl.bcs.socionegocio.per.CrearPersonaNombreFantasiaMasivo.SalidaCrearPersonaNombreFantasiaMasivo;
import cl.bcs.socionegocio.wbs.WebServiceUtils.EntidadManipulada;
import cl.bcs.socionegocio.wbs.WebServiceUtils.SalidaWBS;
import cl.bcs.socionegocio.wbs.pojo.EntradaAdministrarWS;
import cl.bcs.socionegocio.wbs.pojo.NombreFantasia;
import cl.bcs.socionegocio.wbs.pojo.SalidaAdministrarWS;
import cl.bcs.socionegocio.wbs.pojo.TipoPersona;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearNombreFantasia implements JavaCall {
	static final Logger logger = LogManager.getLogger(CrearNombreFantasia.class);
	private String SQL_SELECT_PERSONA = loadResourceToString("cl/bcs/socionegocio/wbs/sql/ConsultarPersona_SelectPersona.sql");

	public static class EntradaCrearNombreFantasia extends EntradaAdministrarWS {
		public List<NombreFantasia> p_NombresFantasia;
		
		public EntradaCrearNombreFantasia() {
			super();
			this.p_NombresFantasia = new ArrayList<>();
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaCrearNombreFantasia jin = gson.fromJson(in.toString(), EntradaCrearNombreFantasia.class);
		SalidaWBS out = logica(conn, jin);

		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public SalidaWBS logica(Connection conn, EntradaCrearNombreFantasia entrada) throws Exception {
		logger.info("INICIO::CrearNombreFantasia");
		long inicioServicio = System.currentTimeMillis();
		SalidaWBS salida =  new SalidaWBS();
		
		entrada.p_NombresFantasia = asignarTipoPersona(conn,entrada);
		
		for (NombreFantasia nombreFantasia : entrada.p_NombresFantasia) {
		    SalidaAdministrarWS out = new SalidaAdministrarWS();
		    EntidadManipulada entidadCreada = new EntidadManipulada();
		    entidadCreada.IdEntidad = nombreFantasia.IdNombreFantasia;
		    entidadCreada.TipoEntidad = "NOMBRE FANTASIA";
		    nombreFantasia.EsBorrador = "N";
		    
		    if(nombreFantasia.TipoEnrolamiento == null || nombreFantasia.TipoEnrolamiento.isEmpty()) {
				entidadCreada = WebServiceUtils.noEsCliente(entidadCreada);
				salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadCreada);
				continue;
		    }
		    
		    FormularioCanal formulario = new FormularioCanal();
		    formulario.IdEmpresa = entrada.p_IdEmpresa;
		    formulario.CodigoFormulario = nombreFantasia.CodigoFormulario;
		    formulario.IdTipoCanal = entrada.p_IdTipoCanal;
		    
		    List<NombreFantasia> listaDeUno = new ArrayList<>();
		    listaDeUno.add(nombreFantasia);

		    out = entrada.validarCamposObligatoriosEntrada(conn, listaDeUno, NombreFantasia.class, formulario, out);
		    if(out.P_OUT_ERRORESCONTADOR > 0) {
				entidadCreada.Mensajes.addAll(out.P_OUT_MENSAJES);
				salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadCreada);
				continue;
		    }
		    
		    CrearPersonaNombreFantasiaMasivo crearPersonaNombreFantasiaMasivo = new CrearPersonaNombreFantasiaMasivo();
		    EntradaCrearPersonaNombreFantasiaMasivo entradaCrearPersonaNombreFantasiaMasivo = new EntradaCrearPersonaNombreFantasiaMasivo();
		    entradaCrearPersonaNombreFantasiaMasivo.p_IdEmpresa = entrada.p_IdEmpresa;
		    entradaCrearPersonaNombreFantasiaMasivo.p_IdModulo = entrada.p_IdModulo;
		    entradaCrearPersonaNombreFantasiaMasivo.p_IdUsuario = entrada.p_IdUsuario;
		    entradaCrearPersonaNombreFantasiaMasivo.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
		    entradaCrearPersonaNombreFantasiaMasivo.p_NombresFantasia = obtenerNombresFantasia(listaDeUno);
		    
		    SalidaCrearPersonaNombreFantasiaMasivo salidaCrearPersonaNombreFantasiaMasivo = new SalidaCrearPersonaNombreFantasiaMasivo();
		    salidaCrearPersonaNombreFantasiaMasivo = crearPersonaNombreFantasiaMasivo.logica(conn, entradaCrearPersonaNombreFantasiaMasivo);
		    
		    if(!salidaCrearPersonaNombreFantasiaMasivo.P_OUT_MENSAJES.isEmpty()) {
				entidadCreada.Mensajes.addAll(salidaCrearPersonaNombreFantasiaMasivo.P_OUT_MENSAJES);
				salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadCreada);
				continue;
		    }
		    
		    if(entidadCreada.Mensajes.isEmpty()) {
				entidadCreada = WebServiceUtils.agregarMensajeOk(entidadCreada, "CREADA");
				salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadCreada);
		    }
		}
		
		salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ENTIDADES_MANIPULADAS.parallelStream().reduce(0L,
			(sum, ent) -> sum += ent.Mensajes.parallelStream().filter(mensaje ->PlataformaUtils.MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo)).count(),
			(sum1, sum2) -> sum1 + sum2);
		
		if(salida.P_OUT_ERRORESCONTADOR > 0 ) {
		    conn.rollback();
		}
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearNombreFantasia", finServicio - inicioServicio);
		return salida;
	}

	private List<TipoPersona> obtenerTiposPersona(Connection con, EntradaCrearNombreFantasia entrada) throws SQLException {
		logger.debug("INICIO::CrearNombreFantasia.obtenerTiposPersona");
		long inicio = System.currentTimeMillis();
		
		List<Long> idsPersonas = entrada.p_NombresFantasia.stream()
				  .map(persona -> persona.IdPersona)
				  .distinct()
				  .collect(Collectors.toList());
		
		HashMap<String, Object> params = new HashMap<String, Object>(2);
		params.put("IdPersonas", idsPersonas);
		
		SQL sql = new SQL();
		List<TipoPersona> tiposPersona = sql.query(con, SQL_SELECT_PERSONA, params, TipoPersona.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearNombreFantasia.obtenerTiposPersona", fin - inicio);
		return tiposPersona;
	}

	private List<cl.bcs.socionegocio.per.pojo.NombreFantasia> obtenerNombresFantasia(List<NombreFantasia> nombresFantasia) {
	    List<cl.bcs.socionegocio.per.pojo.NombreFantasia> nombresFantasiaCreacion = nombresFantasia.parallelStream().map(entidad -> {
	    	cl.bcs.socionegocio.per.pojo.NombreFantasia entidadCreacion = new cl.bcs.socionegocio.per.pojo.NombreFantasia();
	    	entidadCreacion.IdInterno = entidad.IdInterno;
	    	entidadCreacion.IdPersona = entidad.IdPersona;
		    entidadCreacion.DescripcionNombreFantasia = entidad.NombreFantasia;
			
			return entidadCreacion;
	    }).collect(Collectors.toList());

	    return nombresFantasiaCreacion;
	}
	
	private List<NombreFantasia> asignarTipoPersona(Connection con, EntradaCrearNombreFantasia entrada) throws SQLException{
		logger.debug("INICIO::CrearNombreFantasia.asignarTipoPersona");
		long inicio = System.currentTimeMillis();
		
		List<TipoPersona> tipoPersonas = obtenerTiposPersona(con, entrada);
		
		entrada.p_NombresFantasia.parallelStream().forEach(entidad -> {
			TipoPersona tipo = tipoPersonas.parallelStream().filter(tipoPersona -> tipoPersona.IdPersona.equals(entidad.IdPersona)).findFirst().orElse(new TipoPersona());
			entidad.CodigoFormulario = WebServiceUtils.obtenerCodigoFormulario(con, tipo.Tipo, tipo.TipoEnrolamiento, "NOMBRES_FANTASIA_PERSONA");
			entidad.TipoEnrolamiento = tipo.TipoEnrolamiento;
			entidad.IdCliente = tipo.IdCliente;
		});
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearNombreFantasia.asignarTipoPersona", fin - inicio);
		return entrada.p_NombresFantasia;
	}
	
	public static void main(String args[]) throws SQLException {
		System.out.println("== TEST ==");		                  
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();			
			
			String jsonEntrada = "{\r\n"
				+ "  \"p_NombresFantasia\": [\r\n"
				+ "    {\r\n"
				+ "        \"IdInterno\": 0,\r\n"
				+ "        \"IdPersona\": 1732307\r\n"
				//+ "        \"NombreFantasia\": \"PRUEBABABABABABA111111\"\r\n"
				+ "    }\r\n"
//				+ "    {\r\n"
//				+ "        \"IdInterno\": 1,\r\n"
//				+ "        \"IdPersona\": 1759102,\r\n"
//				+ "        \"NombreFantasia\": \"NOMBRE FANTASIA PRUEBA\"\r\n"
//				+ "    }"
				+ "	 ],\r\n"
				+ "  \"p_IdEmpresa\": 1,\r\n"
				+ "  \"p_IdUsuario\": 9\r\n"
				+ "}";
			
			
			EntradaCrearNombreFantasia entradaCrearNombreFantasia = gson.fromJson(jsonEntrada, EntradaCrearNombreFantasia.class);
			CrearNombreFantasia crearNombreFantasia = new CrearNombreFantasia();		
			System.out.println("ENTRADA:");
			System.out.println(crearNombreFantasia.toString());
			try {
			    SalidaWBS salida= crearNombreFantasia.logica(con, entradaCrearNombreFantasia);
				//con.commit();
				con.rollback();
				con.close();
				System.out.println("SALIDA:");
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
