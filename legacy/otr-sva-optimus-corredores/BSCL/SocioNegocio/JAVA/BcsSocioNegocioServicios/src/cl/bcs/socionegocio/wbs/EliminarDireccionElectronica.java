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
import cl.bcs.socionegocio.per.EliminarPersonaDireccionElectronicaMasivo;
import cl.bcs.socionegocio.per.pojo.RegistroCambioMasivo;

import cl.bcs.socionegocio.wbs.pojo.EntradaAdministrarWS;
import cl.bcs.socionegocio.wbs.WebServiceUtils.EntidadManipulada;
import cl.bcs.socionegocio.wbs.WebServiceUtils.SalidaWBS;
import cl.bcs.socionegocio.wbs.pojo.DireccionElectronica;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;

public class EliminarDireccionElectronica implements JavaCall {
	static final Logger logger = LogManager.getLogger(EliminarDireccionElectronica.class);
	private final String ID_TIPO_CANAL = "WBS";
	
	public static class EntradaEliminarDireccionElectronica extends EntradaAdministrarWS{
		public List<DireccionElectronica> p_DireccionesElectronicas;
		
		public EntradaEliminarDireccionElectronica() {
			super();
			this.p_DireccionesElectronicas = new ArrayList<>();
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaEliminarDireccionElectronica jin = gson.fromJson(in.toString(), EntradaEliminarDireccionElectronica.class);
		SalidaWBS out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public SalidaWBS logica(Connection conn, EntradaEliminarDireccionElectronica entrada) throws Exception {
		logger.info("INICIO::EliminarDireccionElectronica");
		long inicioServicio = System.currentTimeMillis();
		SalidaWBS out = new SalidaWBS();
		
		for(DireccionElectronica direccion : entrada.p_DireccionesElectronicas) {
			EntidadManipulada entidadEliminada = new EntidadManipulada();
			entidadEliminada.IdEntidad = direccion.IdDireccionElectronica;
			entidadEliminada.TipoEntidad = "DIRECCION_ELECTRONICA";
			
			List<DireccionElectronica> listaDeUno =  new ArrayList<>();
		    listaDeUno.add(direccion);
		    
		    EliminarPersonaDireccionElectronicaMasivo eliminarPersonaDireccionElectronicaMasivo = new EliminarPersonaDireccionElectronicaMasivo();
			EntradaEjecutarCambiosPersona entradaEjecutarCambiosPersona = new EntradaEjecutarCambiosPersona();
			entradaEjecutarCambiosPersona.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaEjecutarCambiosPersona.p_IdModulo = entrada.p_IdModulo;
			entradaEjecutarCambiosPersona.p_IdUsuario = entrada.p_IdUsuario;
			entradaEjecutarCambiosPersona.p_UsaReglasNegocio = "N";
			entradaEjecutarCambiosPersona.p_IdTipoCanal = ID_TIPO_CANAL;
			entradaEjecutarCambiosPersona.p_RegistroCambio = obtenerRegistrosCambioEliminarDireccionesElectronicas(listaDeUno);
					
			SalidaEjecutarCambiosPersona salidaEjecutarCambiosPersona = eliminarPersonaDireccionElectronicaMasivo.logica(conn, entradaEjecutarCambiosPersona);
			
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
		logger.info("FIN::EliminarDireccionElectronica", finServicio - inicioServicio);
		return out;
	}
	
	private List<RegistroCambioMasivo> obtenerRegistrosCambioEliminarDireccionesElectronicas(List<DireccionElectronica> cuentas){
		logger.trace("INICIO::EliminarDireccionElectronica.obtenerRegistrosCambioEliminarDireccionElectronica");
		long inicio = System.currentTimeMillis();
		
		List<RegistroCambioMasivo> registrosCambio = new ArrayList<>();
		for(DireccionElectronica DireccionElectronica : cuentas) {
			registrosCambio.add(WebServiceUtils.crearRegistroCambio(DireccionElectronica.IdDireccionElectronica, "Borrado", "S"));
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::EliminarDireccionElectronica.obtenerRegistrosCambioEliminarDireccionElectronica", fin - inicio);
		return registrosCambio;
	}
	
	public static void main(String args[]) throws SQLException {
		System.out.println("== TEST ==");		                  
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();			
			
			String jsonEntrada = "\r\n"
					+ "{\r\n"
					+ "    \"p_DireccionesElectronicas\": [{\r\n"
					+ "        \"IdDireccionElectronica\": 1944715\r\n"
					+ "    }\r\n"
					+ "    ],\r\n"
					+ "    \"p_IdEmpresa\": 1,\r\n"
					+ "    \"p_IdUsuario\": 9\r\n"
					+ "}";
			EntradaEliminarDireccionElectronica entradaEliminarDireccionElectronica = gson.fromJson(jsonEntrada, EntradaEliminarDireccionElectronica.class);
			EliminarDireccionElectronica eliminarDireccionElectronica = new EliminarDireccionElectronica();				
			try {
				SalidaWBS salidaWBS= eliminarDireccionElectronica.logica(con, entradaEliminarDireccionElectronica);
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
