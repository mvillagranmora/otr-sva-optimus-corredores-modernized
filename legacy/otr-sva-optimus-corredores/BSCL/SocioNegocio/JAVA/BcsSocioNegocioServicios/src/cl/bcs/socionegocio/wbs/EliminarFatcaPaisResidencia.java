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

import cl.bcs.socionegocio.wbs.pojo.FatcaPaisResidencia;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;

import cl.bcs.socionegocio.wbs.pojo.EntradaAdministrarWS;
import cl.bcs.socionegocio.wbs.WebServiceUtils.EntidadManipulada;
import cl.bcs.socionegocio.wbs.WebServiceUtils.SalidaWBS;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;

public class EliminarFatcaPaisResidencia implements JavaCall {
	static final Logger logger = LogManager.getLogger(EliminarFatcaPaisResidencia.class);
	
	public static class EntradaEliminarFatcaPaisResidencia extends EntradaAdministrarWS{
		public Long p_IdPersona;
		public List<FatcaPaisResidencia> p_FatcasPaisesResidencias;
		
		public EntradaEliminarFatcaPaisResidencia() {
			super();
			this.p_FatcasPaisesResidencias = new ArrayList<FatcaPaisResidencia>();
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaEliminarFatcaPaisResidencia jin = gson.fromJson(in.toString(), EntradaEliminarFatcaPaisResidencia.class);
		SalidaWBS out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public SalidaWBS logica(Connection conn, EntradaEliminarFatcaPaisResidencia entrada) throws Exception {
		logger.info("INICIO::EliminarFatcaPaisResidencia");
		long inicioServicio = System.currentTimeMillis();
		SalidaWBS out = new SalidaWBS();
		
		for(FatcaPaisResidencia paisResidencia : entrada.p_FatcasPaisesResidencias) {
			EntidadManipulada entidadEliminada = new EntidadManipulada();
			entidadEliminada.IdEntidad = paisResidencia.IdPersona;
			entidadEliminada.TipoEntidad = "FATCA_PAIS_RESIDENCIA";
			
			List<cl.bcs.socionegocio.per.EliminarFatcaPaisResidencia.FatcaPaisResidencia> listaDeUno =  new ArrayList<>();
			cl.bcs.socionegocio.per.EliminarFatcaPaisResidencia.FatcaPaisResidencia entidadPaisResidencia = new cl.bcs.socionegocio.per.EliminarFatcaPaisResidencia.FatcaPaisResidencia();
		    entidadPaisResidencia.IdPaisResidencia = paisResidencia.IdPaisResidencia;
		    entidadPaisResidencia.IdPersona = entrada.p_IdPersona;
		    listaDeUno.add(entidadPaisResidencia);
		    
		    cl.bcs.socionegocio.per.EliminarFatcaPaisResidencia eliminarFatcaPaisResidencia = new cl.bcs.socionegocio.per.EliminarFatcaPaisResidencia();
		    cl.bcs.socionegocio.per.EliminarFatcaPaisResidencia.EntradaEliminarFatcaPaisResidencia entradaEliminarFatcaPaisResidencia = new cl.bcs.socionegocio.per.EliminarFatcaPaisResidencia.EntradaEliminarFatcaPaisResidencia();
		    entradaEliminarFatcaPaisResidencia.p_IdEmpresa = entrada.p_IdEmpresa;
		    entradaEliminarFatcaPaisResidencia.p_FatcaPaisResidencia = listaDeUno;
		    
		    cl.bcs.socionegocio.per.EliminarFatcaPaisResidencia.SalidaEliminarFatcaPaisResidencia salidaEliminarFatcaPaisResidencia= eliminarFatcaPaisResidencia.logica(conn,entradaEliminarFatcaPaisResidencia);
		    
			if( !salidaEliminarFatcaPaisResidencia.P_OUT_MENSAJES.isEmpty() ) {
				entidadEliminada.Mensajes.addAll(salidaEliminarFatcaPaisResidencia.P_OUT_MENSAJES);
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
		logger.info("FIN::EliminarFatcaPaisResidencia", finServicio - inicioServicio);
		return out;
	}
	
	public static void main(String args[]) throws SQLException {
		System.out.println("== TEST ==");		                  
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();			
			
			String jsonEntrada = "\r\n"
					+ "{\r\n"
					+ "    \"p_FatcasPaisesResidencias\": [{\r\n"
					+ "        \"IdPaisResidencia\": 1937763\r\n"
					+ "    },\r\n"
					+ "    {\r\n"
					+ "        \"IdPaisResidencia\": 1937764\r\n"
					+ "    }],\r\n"
					+ "    \"p_IdPersona\": 1790863,\r\n"
					+ "    \"p_IdEmpresa\": 1,\r\n"
					+ "    \"p_IdUsuario\": 9\r\n"
					+ "}";
			EntradaEliminarFatcaPaisResidencia entradaEliminarFatcaPaisResidencia = gson.fromJson(jsonEntrada, EntradaEliminarFatcaPaisResidencia.class);
			EliminarFatcaPaisResidencia eliminarFatcaPaisResidencia = new EliminarFatcaPaisResidencia();				
			try {
				SalidaWBS salidaWBS= eliminarFatcaPaisResidencia.logica(con, entradaEliminarFatcaPaisResidencia);
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
