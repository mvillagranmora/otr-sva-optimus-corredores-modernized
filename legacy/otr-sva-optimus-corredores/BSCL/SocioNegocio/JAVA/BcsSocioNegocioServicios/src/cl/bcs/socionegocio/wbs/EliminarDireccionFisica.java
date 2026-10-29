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
import cl.bcs.socionegocio.per.EliminarPersonaDireccionFisicaMasivo;
import cl.bcs.socionegocio.per.pojo.RegistroCambioMasivo;

import cl.bcs.socionegocio.wbs.pojo.EntradaAdministrarWS;
import cl.bcs.socionegocio.wbs.WebServiceUtils.EntidadManipulada;
import cl.bcs.socionegocio.wbs.WebServiceUtils.SalidaWBS;
import cl.bcs.socionegocio.wbs.pojo.DireccionFisica;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;

public class EliminarDireccionFisica implements JavaCall {
	static final Logger logger = LogManager.getLogger(EliminarDireccionFisica.class);
	private final String ID_TIPO_CANAL = "WBS";
	
	public static class EntradaEliminarDireccionFisica extends EntradaAdministrarWS{
		public List<DireccionFisica> p_DireccionesFisicas;
		
		public EntradaEliminarDireccionFisica() {
			super();
			this.p_DireccionesFisicas = new ArrayList<>();
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaEliminarDireccionFisica jin = gson.fromJson(in.toString(), EntradaEliminarDireccionFisica.class);
		SalidaWBS out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public SalidaWBS logica(Connection conn, EntradaEliminarDireccionFisica entrada) throws Exception {
		logger.info("INICIO::EliminarDireccionFisica");
		long inicioServicio = System.currentTimeMillis();
		SalidaWBS out = new SalidaWBS();
		
		for(DireccionFisica direccionFisica : entrada.p_DireccionesFisicas) {
			EntidadManipulada entidadEliminada = new EntidadManipulada();
			entidadEliminada.IdEntidad = direccionFisica.IdDireccionFisica;
			entidadEliminada.TipoEntidad = "DIRECCION_FISICA";
			
			List<DireccionFisica> listaDeUno =  new ArrayList<>();
		    listaDeUno.add(direccionFisica);
		    
			EliminarPersonaDireccionFisicaMasivo eliminarPersonaDireccionFisicaMasivo = new EliminarPersonaDireccionFisicaMasivo();
			EntradaEjecutarCambiosPersona entradaEjecutarCambiosPersona = new EntradaEjecutarCambiosPersona();
			entradaEjecutarCambiosPersona.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaEjecutarCambiosPersona.p_IdModulo = entrada.p_IdModulo;
			entradaEjecutarCambiosPersona.p_IdUsuario = entrada.p_IdUsuario;
			entradaEjecutarCambiosPersona.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
			entradaEjecutarCambiosPersona.p_IdTipoCanal = ID_TIPO_CANAL;
			entradaEjecutarCambiosPersona.p_RegistroCambio = obtenerRegistrosCambioEliminarDireccionesFisicas(listaDeUno);
			
			SalidaEjecutarCambiosPersona salidaEjecutarCambiosPersona = eliminarPersonaDireccionFisicaMasivo.logica(conn, entradaEjecutarCambiosPersona);
			
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
		logger.info("FIN::EliminarDireccionFisica", finServicio - inicioServicio);
		return out;
	}
	
	private List<RegistroCambioMasivo> obtenerRegistrosCambioEliminarDireccionesFisicas(List<DireccionFisica> cuentas){
		logger.trace("INICIO::EliminarDireccionFisica.obtenerRegistrosCambioEliminarDireccionFisica");
		long inicio = System.currentTimeMillis();
		
		List<RegistroCambioMasivo> registrosCambio = new ArrayList<>();
		for(DireccionFisica DireccionFisica : cuentas) {
			registrosCambio.add(WebServiceUtils.crearRegistroCambio(DireccionFisica.IdDireccionFisica, "Borrado", "S"));
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::EliminarDireccionFisica.obtenerRegistrosCambioEliminarDireccionFisica", fin - inicio);
		return registrosCambio;
	}
	
	public static void main(String args[]) throws SQLException {
		System.out.println("== TEST ==");		                  
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();			
			
			String jsonEntrada = "\r\n"
					+ "{\r\n"
					+ "    \"p_DireccionesFisicas\": [{\r\n"
					+ "        \"IdDireccionFisica\": 159004\r\n"
					+ "    },\r\n"
					+ "    {\r\n"
					+ "        \"IdDireccionFisica\": 159005\r\n"
					+ "    }],\r\n"
					+ "    \"p_IdEmpresa\": 1,\r\n"
					+ "    \"p_IdUsuario\": 9\r\n"
					+ "}";
			EntradaEliminarDireccionFisica entradaEliminarDireccionFisica = gson.fromJson(jsonEntrada, EntradaEliminarDireccionFisica.class);
			EliminarDireccionFisica eliminarDireccionFisica = new EliminarDireccionFisica();				
			try {
				SalidaWBS salidaWBS= eliminarDireccionFisica.logica(con, entradaEliminarDireccionFisica);
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
