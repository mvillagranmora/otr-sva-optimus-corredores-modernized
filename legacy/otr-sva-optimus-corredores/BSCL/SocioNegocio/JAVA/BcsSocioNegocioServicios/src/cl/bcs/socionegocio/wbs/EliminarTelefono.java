package cl.bcs.socionegocio.wbs;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.socionegocio.per.EjecutarCambiosPersona.EntradaEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.SalidaEjecutarCambiosPersona;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.per.EliminarPersonaTelefonoMasivo;
import cl.bcs.socionegocio.per.pojo.RegistroCambioMasivo;
import cl.bcs.socionegocio.wbs.WebServiceUtils.EntidadManipulada;
import cl.bcs.socionegocio.wbs.WebServiceUtils.SalidaWBS;
import cl.bcs.socionegocio.wbs.pojo.EntradaAdministrarWS;
import cl.bcs.socionegocio.wbs.pojo.Telefono;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;

public class EliminarTelefono implements JavaCall {
	static final Logger logger = LogManager.getLogger(EliminarTelefono.class);
	private final String ID_TIPO_CANAL = "WBS";
	
	public static class EntradaEliminarTelefono extends EntradaAdministrarWS{
		public List<Telefono> p_Telefonos;
		
		public EntradaEliminarTelefono() {
			super();
			this.p_Telefonos = new ArrayList<>();
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaEliminarTelefono jin = gson.fromJson(in.toString(), EntradaEliminarTelefono.class);
		SalidaWBS out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public SalidaWBS logica(Connection conn, EntradaEliminarTelefono entrada) throws Exception {
		logger.info("INICIO::EliminarTelefono");
		long inicioServicio = System.currentTimeMillis();
		SalidaWBS out = new SalidaWBS();
		
		for(Telefono telefono : entrada.p_Telefonos) {
			EntidadManipulada entidadEliminada = new EntidadManipulada();
			entidadEliminada.IdEntidad = telefono.IdTelefono;
			entidadEliminada.TipoEntidad = "TELEFONO";
			
			List<Telefono> listaDeUno =  new ArrayList<>();
		    listaDeUno.add(telefono);
		    
		    EliminarPersonaTelefonoMasivo eliminarPersonaTelefonoMasivo = new EliminarPersonaTelefonoMasivo();
			EntradaEjecutarCambiosPersona entradaEjecutarCambiosPersona = new EntradaEjecutarCambiosPersona();
			entradaEjecutarCambiosPersona.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaEjecutarCambiosPersona.p_IdModulo = entrada.p_IdModulo;
			entradaEjecutarCambiosPersona.p_IdUsuario = entrada.p_IdUsuario;
			entradaEjecutarCambiosPersona.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
			entradaEjecutarCambiosPersona.p_IdTipoCanal = ID_TIPO_CANAL;
			entradaEjecutarCambiosPersona.p_RegistroCambio = obtenerRegistrosCambioEliminarTelefonos(entrada.p_Telefonos);
			
			SalidaEjecutarCambiosPersona salidaEjecutarCambiosPersona = eliminarPersonaTelefonoMasivo.logica(conn, entradaEjecutarCambiosPersona);

			if( !salidaEjecutarCambiosPersona.P_OUT_MENSAJES.isEmpty() ) {
				entidadEliminada.Mensajes.addAll(salidaEjecutarCambiosPersona.P_OUT_MENSAJES);
				out.P_OUT_ENTIDADES_MANIPULADAS.add(entidadEliminada);
				continue;
		    }
		    
		    if(entidadEliminada.Mensajes.isEmpty()) {
				entidadEliminada = WebServiceUtils.agregarMensajeOk(entidadEliminada,"ELIMINADA");
				out.P_OUT_ENTIDADES_MANIPULADAS.add(entidadEliminada);
		    }
		}
		out.P_OUT_ERRORESCONTADOR = out.P_OUT_ENTIDADES_MANIPULADAS.parallelStream().reduce(0L,
				(sum, ent) -> sum += ent.Mensajes.parallelStream().filter(mensaje ->PlataformaUtils.MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo)).count(),
				(sum1, sum2) -> sum1 + sum2);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::EliminarTelefono", finServicio - inicioServicio);
		return out;
	}
	
	private List<RegistroCambioMasivo> obtenerRegistrosCambioEliminarTelefonos(List<Telefono> cuentas){
		logger.trace("INICIO::EliminarTelefono.obtenerRegistrosCambioEliminarTelefono");
		long inicio = System.currentTimeMillis();
		
		List<RegistroCambioMasivo> registrosCambio = new ArrayList<>();
		for(Telefono Telefono : cuentas) {
			registrosCambio.add(WebServiceUtils.crearRegistroCambio(Telefono.IdTelefono, "Borrado", "S"));
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::EliminarTelefono.obtenerRegistrosCambioEliminarTelefono", fin - inicio);
		return registrosCambio;
	}
	
	public static void main(String args[]) throws SQLException {
		System.out.println("== TEST ==");		                  
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();			
			
			String jsonEntrada = "\r\n"
					+ "{\r\n"
					+ "    \"p_Telefonos\": [{\r\n"
					+ "        \"IdTelefono\": 2578952\r\n"
					+ "    },\r\n"
					+ "    {\r\n"
					+ "        \"IdTelefono\": 2578959\r\n"
					+ "    }],\r\n"
					+ "    \"p_IdEmpresa\": 1,\r\n"
					+ "    \"p_IdUsuario\": 9\r\n"
					+ "}";
			EntradaEliminarTelefono entradaEliminarTelefono = gson.fromJson(jsonEntrada, EntradaEliminarTelefono.class);
			EliminarTelefono eliminarTelefono = new EliminarTelefono();				
			try {
				SalidaWBS salidaWBS= eliminarTelefono.logica(con, entradaEliminarTelefono);
				//con.commit();
				con.rollback();
				con.close();
				System.out.println(salidaWBS.toString());
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
