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
import cl.bcs.socionegocio.per.pojo.RegistroCambioMasivo;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.EntradaEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.SalidaEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.ModificarRelacionPersonaMasivo;
import cl.bcs.socionegocio.per.ModificarRelacionPersonaMasivo.RelacionPersona;
import cl.bcs.socionegocio.wbs.WebServiceUtils.EntidadManipulada;
import cl.bcs.socionegocio.wbs.WebServiceUtils.SalidaWBS;
import cl.bcs.socionegocio.wbs.pojo.EntradaAdministrarWS;
import cl.bcs.socionegocio.wbs.pojo.Relacion;
import cl.bcs.socionegocio.wbs.pojo.SalidaAdministrarWS;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarRelacion implements JavaCall {
	static final Logger logger = LogManager.getLogger(ModificarRelacion.class);
	
	private String SQL_SELECT_RELACION = loadResourceToString("cl/bcs/socionegocio/wbs/sql/ModificarRelacion_SelectRelacion.sql");
	
	public static class EntradaModificarRelacion extends EntradaAdministrarWS {
		public List<Relacion> p_Relaciones;
		
		public EntradaModificarRelacion(){
			this.p_Relaciones = new ArrayList<Relacion>();
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaModificarRelacion jin = gson.fromJson(in.toString(), EntradaModificarRelacion.class);
		SalidaWBS out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public SalidaWBS logica(Connection conn, EntradaModificarRelacion entrada) throws Exception {
		logger.info("INICIO::ModificarRelacion");
		long inicioServicio = System.currentTimeMillis();
		SalidaWBS salida =  new SalidaWBS();
		List<Relacion> relacionesPersona = obtenerRelaciones(conn,entrada);

		for(Relacion relacion : relacionesPersona) {
		    SalidaAdministrarWS out = new SalidaAdministrarWS();
		    EntidadManipulada entidadModificada = new EntidadManipulada();
		    entidadModificada.IdEntidad = relacion.IdRelacion;
		    entidadModificada.TipoEntidad = "RELACION";
		    relacion.EsBorrador = "N";
		    
		    if(relacion.TipoEnrolamiento == null) {
		    	entidadModificada = WebServiceUtils.noEsCliente(entidadModificada);
				salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadModificada);
				continue;
		    }
		    
		    List<Relacion> mergeCambios = new ArrayList<>();
		    Relacion nuevosCampos = entrada.p_Relaciones.parallelStream()
		    		.filter(relacionMod -> relacionMod.IdRelacion.equals(relacion.IdRelacion))
		    		.findFirst().orElse(new Relacion());
		    WebServiceUtils.merge(relacion, nuevosCampos);
		    mergeCambios.add(relacion);

		    FormularioCanal formulario = new FormularioCanal();
		    formulario.IdEmpresa = entrada.p_IdEmpresa;
		    formulario.CodigoFormulario = WebServiceUtils.obtenerCodigoFormulario(conn, relacion.Tipo, relacion.TipoEnrolamiento, "RELACIONES");
		    formulario.IdTipoCanal = entrada.p_IdTipoCanal;

		    out = entrada.validarCamposObligatoriosEntrada(conn, obtenerRelacionTipoCreacion(mergeCambios), RelacionPersona.class, formulario, out);
			  
		    if(out.P_OUT_ERRORESCONTADOR > 0) {
				entidadModificada = WebServiceUtils.agregarMensajesCamposRequeridos(entidadModificada, out);
				salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadModificada);
				continue;
		    }
		    
		    List<Relacion> listaDeUno =  new ArrayList<>();
		    listaDeUno.add(nuevosCampos);
		    
		    ModificarRelacionPersonaMasivo modificarRelacionMasivo = new ModificarRelacionPersonaMasivo();
		    EntradaEjecutarCambiosPersona entradaEjecutarCambiosPersona = new EntradaEjecutarCambiosPersona();
		    entradaEjecutarCambiosPersona.p_IdEmpresa = entrada.p_IdEmpresa;
		    entradaEjecutarCambiosPersona.p_IdModulo = entrada.p_IdModulo;
		    entradaEjecutarCambiosPersona.p_IdUsuario = entrada.p_IdUsuario;
		    entradaEjecutarCambiosPersona.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
		    entradaEjecutarCambiosPersona.p_RegistroCambio = crearRegistroCambio(listaDeUno);
		    
		    SalidaEjecutarCambiosPersona salidaEjecutarCambiosPersona = modificarRelacionMasivo.logica(conn, entradaEjecutarCambiosPersona);
		    
		    if( !salidaEjecutarCambiosPersona.P_OUT_MENSAJES.isEmpty() ) {
			entidadModificada.Mensajes.addAll(salidaEjecutarCambiosPersona.P_OUT_MENSAJES);
		    }
		    
		    if(entidadModificada.Mensajes.isEmpty()) {
				entidadModificada = WebServiceUtils.agregarMensajeOk(entidadModificada,"MODIFICADA");
				salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadModificada);
		    }
		    
		}
		salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ENTIDADES_MANIPULADAS.parallelStream().reduce(0L,
				(sum, ent) -> sum += ent.Mensajes.parallelStream().filter(mensaje ->PlataformaUtils.MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo)).count(),
				(sum1, sum2) -> sum1 + sum2);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarRelacion", finServicio - inicioServicio);
		return salida;
	}

	private List<RelacionPersona> obtenerRelacionTipoCreacion(List<Relacion> relaciones) {
	    logger.trace("INICIO::ModificarRelacion.obtenerRelacionTipoCreacion");
		long inicio = System.currentTimeMillis();
		long fin = System.currentTimeMillis();
		
		List<RelacionPersona> relacionesModificar = relaciones.parallelStream().map(relacion->{
		    RelacionPersona relacionMod = new RelacionPersona();
		    relacionMod.IdRelacionPersona = relacion.IdRelacion;
		    relacionMod.IdPersonaDestino = relacion.IdPersonaRelacionada;
		    relacionMod.IdEstado = relacion.IdEstado;
		    relacionMod.IdTipoRelacion = relacion.IdTipoRelacion;
		    
		    return relacionMod;
		}).collect(Collectors.toList());
		logger.trace("FIN::ModificarRelacion.obtenerRelacionTipoCreacion", fin - inicio);
		return relacionesModificar;
	}

	private List<RegistroCambioMasivo> crearRegistroCambio(List<Relacion> relaciones) {
		logger.trace("INICIO::ModificarRelacion.crearRegistroCambio");
		long inicio = System.currentTimeMillis();
		
		List<RegistroCambioMasivo> registrosCambio = new ArrayList<>();
		for(Relacion relacion : relaciones) {
			registrosCambio.add(WebServiceUtils.crearRegistroCambio(relacion.IdRelacion, "IdEstado", relacion.IdEstado));

		}
		
		registrosCambio = registrosCambio.stream().filter(registro ->  registro.ValorNuevo !=null && !registro.ValorNuevo.equals("")).collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ModificarRelacion.crearRegistroCambio", fin - inicio);
		return registrosCambio;
	}

	private List<Relacion> obtenerRelaciones(Connection conn, EntradaModificarRelacion entrada) throws Exception {
		logger.debug("INICIO::ModificarRelacion.obtenerRelaciones");
		long inicio = System.currentTimeMillis();
		
		List<Long> IdsRelaciones = entrada.p_Relaciones.stream()
				  .map(relacion -> relacion.IdRelacion)
				  .distinct()
				  .collect(Collectors.toList());
		
		HashMap<String, Object> params = new HashMap<String, Object>(2);
		params.put("IdRelacionPersona", IdsRelaciones);
		
		SQL sql = new SQL();
		List<Relacion> datosRelaciones = sql.query(conn, SQL_SELECT_RELACION, params, Relacion.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarRelacion.obtenerRelaciones", fin - inicio);
		return datosRelaciones;
	}
	
	public static void main(String args[]) throws SQLException {
		System.out.println("== TEST ==");		                  
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();			
			
			String jsonEntrada = "{\r\n"
					+ "  \"p_Relaciones\": [\r\n"
					+ "    {\r\n"
					+ "      \"IdRelacion\": 927,\r\n"
					+ "      \"IdEstado\": \"INA\"\r\n"
					+ "    },\r\n"
					+ "    {\r\n"
					+ "      \"IdRelacion\": 928,\r\n"
					+ "      \"IdEstado\": \"INA\"\r\n"
					+ "    }\r\n"
					+ "  ],\r\n"
					+ "  \"p_IdEmpresa\": 1,\r\n"
					+ "  \"p_IdUsuario\": 9\r\n"
					+ "}";
			
			String jsonReq = "{\r\n"
					+ "  \"p_Relaciones\": [\r\n"
					+ "    {\r\n"
					+ "      \"IdRelacion\": 549,\r\n"
					+ "      \"IdEstado\": \"INA\"\r\n"
					+ "    }\r\n"
					+ "  ],\r\n"
					+ "  \"p_IdEmpresa\": 1,\r\n"
					+ "  \"p_IdUsuario\": 9\r\n"
					+ "}";
				
			EntradaModificarRelacion entradaModificarDocumentoIdentidad = gson.fromJson(jsonReq, EntradaModificarRelacion.class);
			ModificarRelacion ModificarRelacion = new ModificarRelacion();				
			try {
				SalidaWBS salida= ModificarRelacion.logica(con, entradaModificarDocumentoIdentidad);
//				con.commit();
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
