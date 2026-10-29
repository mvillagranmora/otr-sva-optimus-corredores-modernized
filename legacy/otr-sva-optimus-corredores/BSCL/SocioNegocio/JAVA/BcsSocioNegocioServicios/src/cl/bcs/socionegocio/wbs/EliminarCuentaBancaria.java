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
import cl.bcs.socionegocio.per.EliminarPersonaCuentaBancariaMasivo;
import cl.bcs.socionegocio.per.pojo.RegistroCambioMasivo;

import cl.bcs.socionegocio.wbs.pojo.EntradaAdministrarWS;
import cl.bcs.socionegocio.wbs.WebServiceUtils.EntidadManipulada;
import cl.bcs.socionegocio.wbs.WebServiceUtils.SalidaWBS;
import cl.bcs.socionegocio.wbs.pojo.CuentaBancaria;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;

public class EliminarCuentaBancaria implements JavaCall {
	static final Logger logger = LogManager.getLogger(EliminarCuentaBancaria.class);
	private final String ID_TIPO_CANAL = "WBS";
	
	public static class EntradaEliminarCuentaBancaria extends EntradaAdministrarWS{
		public List<CuentaBancaria> p_CuentasBancarias;
		
		public EntradaEliminarCuentaBancaria() {
			super();
			this.p_CuentasBancarias = new ArrayList<>();
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaEliminarCuentaBancaria jin = gson.fromJson(in.toString(), EntradaEliminarCuentaBancaria.class);
		SalidaWBS out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public SalidaWBS logica(Connection conn, EntradaEliminarCuentaBancaria entrada) throws Exception {
		logger.info("INICIO::EliminarCuentaBancaria");
		long inicioServicio = System.currentTimeMillis();
		SalidaWBS out = new SalidaWBS();
		
		for(CuentaBancaria cuentaBancaria : entrada.p_CuentasBancarias) {
			EntidadManipulada entidadEliminada = new EntidadManipulada();
			entidadEliminada.IdEntidad = cuentaBancaria.IdCuentaBancaria;
			entidadEliminada.TipoEntidad = "CUENTA_BANCARIA";
			
			List<CuentaBancaria> listaDeUno =  new ArrayList<>();
		    listaDeUno.add(cuentaBancaria);
		    
			EliminarPersonaCuentaBancariaMasivo eliminarPersonaCuentaBancariaMasivo = new EliminarPersonaCuentaBancariaMasivo();
			EntradaEjecutarCambiosPersona entradaEjecutarCambiosPersona = new EntradaEjecutarCambiosPersona();
			entradaEjecutarCambiosPersona.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaEjecutarCambiosPersona.p_IdModulo = entrada.p_IdModulo;
			entradaEjecutarCambiosPersona.p_IdUsuario = entrada.p_IdUsuario;
			entradaEjecutarCambiosPersona.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
			entradaEjecutarCambiosPersona.p_IdTipoCanal = ID_TIPO_CANAL;
			entradaEjecutarCambiosPersona.p_RegistroCambio = obtenerRegistrosCambioEliminarCuentasBancarias(listaDeUno);
				
			SalidaEjecutarCambiosPersona salidaEjecutarCambiosPersona = eliminarPersonaCuentaBancariaMasivo.logica(conn, entradaEjecutarCambiosPersona);

			
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
		logger.info("FIN::EliminarCuentaBancaria", finServicio - inicioServicio);
		return out;
	}
	
	private List<RegistroCambioMasivo> obtenerRegistrosCambioEliminarCuentasBancarias(List<CuentaBancaria> cuentas){
		logger.trace("INICIO::EliminarCuentaBancaria.obtenerRegistrosCambioEliminarCuentaBancaria");
		long inicio = System.currentTimeMillis();
		
		List<RegistroCambioMasivo> registrosCambio = new ArrayList<>();
		for(CuentaBancaria CuentaBancaria : cuentas) {
			registrosCambio.add(WebServiceUtils.crearRegistroCambio(CuentaBancaria.IdCuentaBancaria, "Borrado", "S"));
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::EliminarCuentaBancaria.obtenerRegistrosCambioEliminarCuentaBancaria", fin - inicio);
		return registrosCambio;
	}
	
	public static void main(String args[]) throws SQLException {
		System.out.println("== TEST ==");		                  
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();			
			
			String jsonEntrada = "\r\n"
					+ "{\r\n"
					+ "    \"p_CuentasBancarias\": [{\r\n"
					+ "        \"IdCuentaBancaria\": 107\r\n"
					+ "    },\r\n"
					+ "    {\r\n"
					+ "        \"IdCuentaBancaria\": 108\r\n"
					+ "    }],\r\n"
					+ "    \"p_IdEmpresa\": 1,\r\n"
					+ "    \"p_IdUsuario\": 9\r\n"
					+ "}";
			EntradaEliminarCuentaBancaria entradaEliminarCuentaBancaria = gson.fromJson(jsonEntrada, EntradaEliminarCuentaBancaria.class);
			EliminarCuentaBancaria eliminarCuentaBancaria = new EliminarCuentaBancaria();				
			try {
				SalidaWBS salidaWBS= eliminarCuentaBancaria.logica(con, entradaEliminarCuentaBancaria);
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
