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
import cl.bcs.socionegocio.per.EliminarPersonaTarjetaBancariaMasivo;
import cl.bcs.socionegocio.per.pojo.RegistroCambioMasivo;
import cl.bcs.socionegocio.wbs.WebServiceUtils.EntidadManipulada;
import cl.bcs.socionegocio.wbs.WebServiceUtils.SalidaWBS;
import cl.bcs.socionegocio.wbs.pojo.EntradaAdministrarWS;
import cl.bcs.socionegocio.wbs.pojo.TarjetaBancaria;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;

public class EliminarTarjetaBancaria implements JavaCall {
	static final Logger logger = LogManager.getLogger(EliminarTarjetaBancaria.class);
	private final String ID_TIPO_CANAL = "WBS";
	
	public static class EntradaEliminarTarjetaBancaria extends EntradaAdministrarWS{
		public List<TarjetaBancaria> p_TarjetasBancarias;
		
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaEliminarTarjetaBancaria jin = gson.fromJson(in.toString(), EntradaEliminarTarjetaBancaria.class);
		SalidaWBS out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public SalidaWBS logica(Connection conn, EntradaEliminarTarjetaBancaria entrada) throws Exception {
		logger.info("INICIO::EliminarTarjetaBancaria");
		long inicioServicio = System.currentTimeMillis();

		SalidaWBS out = new SalidaWBS();
				
		for(TarjetaBancaria direccionFisica : entrada.p_TarjetasBancarias) {
		    EntidadManipulada entidadEliminada = new EntidadManipulada();
		    entidadEliminada.IdEntidad = direccionFisica.IdTarjetaBancaria;
		    entidadEliminada.TipoEntidad = "TARJETA_BANCARIA";
		    
		    List<TarjetaBancaria> listaDeUno =  new ArrayList<>();
		    listaDeUno.add(direccionFisica);
		    
		    EliminarPersonaTarjetaBancariaMasivo eliminarPersonaTarjetaBancariaMasivo = new EliminarPersonaTarjetaBancariaMasivo();
			EntradaEjecutarCambiosPersona entradaEjecutarCambiosPersona = new EntradaEjecutarCambiosPersona();
			entradaEjecutarCambiosPersona.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaEjecutarCambiosPersona.p_IdModulo = entrada.p_IdModulo;
			entradaEjecutarCambiosPersona.p_IdUsuario = entrada.p_IdUsuario;
			entradaEjecutarCambiosPersona.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
			entradaEjecutarCambiosPersona.p_IdTipoCanal = ID_TIPO_CANAL;
			entradaEjecutarCambiosPersona.p_RegistroCambio = obtenerRegistrosCambioEliminarTarjetasBancarias(listaDeUno);
			
			SalidaEjecutarCambiosPersona salidaEjecutarCambiosPersona = eliminarPersonaTarjetaBancariaMasivo.logica(conn, entradaEjecutarCambiosPersona);

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
		logger.info("FIN::EliminarTarjetaBancaria", finServicio - inicioServicio);
		return out;
	}
	
	private List<RegistroCambioMasivo> obtenerRegistrosCambioEliminarTarjetasBancarias(List<TarjetaBancaria> tarjetas){
		logger.trace("INICIO::EliminarTarjetaBancaria.obtenerRegistrosCambioEliminarTarjetaBancaria");
		long inicio = System.currentTimeMillis();
		
		List<RegistroCambioMasivo> registrosCambio = new ArrayList<>();
		for(TarjetaBancaria tarjetaBancaria : tarjetas) {
			registrosCambio.add(WebServiceUtils.crearRegistroCambio(tarjetaBancaria.IdTarjetaBancaria, "Borrado", "S"));
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::EliminarTarjetaBancaria.obtenerRegistrosCambioEliminarTarjetaBancaria", fin - inicio);
		return registrosCambio;
	}
	
	public static void main(String args[]) throws SQLException {
		System.out.println("== TEST ==");		                  
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();			
			
			String jsonEntrada = "\r\n"
					+ "{\r\n"
					+ "    \"p_TarjetasBancarias\": [{\r\n"
					+ "        \"IdPersona\": 1335561,\r\n"
					+ "        \"IdTarjetaBancaria\": 10027\r\n"
					+ "    },\r\n"
					+ "    {\r\n"
					+ "        \"IdPersona\": 20004,\r\n"
					+ "        \"IdTarjetaBancaria\": 10007\r\n"
					+ "    }],\r\n"
					+ "    \"p_IdEmpresa\": 1,\r\n"
					+ "    \"p_IdUsuario\": 9\r\n"
					+ "}";
			EntradaEliminarTarjetaBancaria entradaEliminarTarjetaBancaria = gson.fromJson(jsonEntrada, EntradaEliminarTarjetaBancaria.class);
			EliminarTarjetaBancaria eliminarTarjetaBancaria = new EliminarTarjetaBancaria();				
			try {
				SalidaWBS salidaWBS= eliminarTarjetaBancaria.logica(con, entradaEliminarTarjetaBancaria);
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
