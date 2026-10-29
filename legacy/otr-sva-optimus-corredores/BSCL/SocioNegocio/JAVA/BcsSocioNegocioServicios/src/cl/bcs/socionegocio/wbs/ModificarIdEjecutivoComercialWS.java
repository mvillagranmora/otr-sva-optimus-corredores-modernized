package cl.bcs.socionegocio.wbs;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.por.ModificarIdEjecutivoComercial;
import cl.bcs.socionegocio.por.ModificarIdEjecutivoComercial.EntradaModificarIdEjecutivoComercial;
import cl.bcs.socionegocio.por.ModificarIdEjecutivoComercial.SalidaEjecutarCambioEjecutivo;
import cl.bcs.socionegocio.wbs.ModificarClienteWS.EntradaModificarClienteWS;
import cl.bcs.socionegocio.wbs.WebServiceUtils.EntidadManipulada;
import cl.bcs.socionegocio.wbs.WebServiceUtils.SalidaWBS;
import cl.bcs.socionegocio.wbs.pojo.EntradaAdministrarWS;
import cl.bcs.socionegocio.wbs.pojo.SalidaAdministrarWS;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarIdEjecutivoComercialWS implements JavaCall {
	static final Logger logger = LogManager.getLogger(ModificarIdEjecutivoComercialWS.class);
	
	public static class EntradaModificarIdEjecutivoComercialWS extends EntradaAdministrarWS {
		public Long p_IdPortafolio;
		public Long p_IdEjecutivoComercial;
		public Long p_IdPersona;
		
		public EntradaModificarIdEjecutivoComercialWS() {
			this.p_IdPortafolio = 0L;
			this.p_IdEjecutivoComercial =0L;
			this.p_IdPersona =0L;
		}
	}
	
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaModificarIdEjecutivoComercialWS jin = gson.fromJson(in.toString(), EntradaModificarIdEjecutivoComercialWS.class);
		SalidaWBS out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	
	public SalidaWBS logica(Connection conn, EntradaModificarIdEjecutivoComercialWS entrada)  throws Exception {
		logger.info("INICIO::ModificarIdEjecutivoComercial");
		long inicioServicio = System.currentTimeMillis();

		EntidadManipulada entidadModificada = new EntidadManipulada();
		entidadModificada.IdEntidad = entrada.p_IdEjecutivoComercial;
		entidadModificada.TipoEntidad = "ID EJECUTIVO COMERCIAL";
		SalidaWBS salida =  new SalidaWBS();
		
		
		EntradaModificarIdEjecutivoComercial EntrdaEjecutivoComercialPortafolio = new EntradaModificarIdEjecutivoComercial();
		ModificarIdEjecutivoComercial EjecutivoComercialPortafolio = new ModificarIdEjecutivoComercial();
		EntrdaEjecutivoComercialPortafolio.p_IdUsuario = entrada.p_IdUsuario;
		EntrdaEjecutivoComercialPortafolio.p_IdEmpresa = entrada.p_IdEmpresa;
		EntrdaEjecutivoComercialPortafolio.p_IdPersona = entrada.p_IdPersona;
		EntrdaEjecutivoComercialPortafolio.p_IdPortafolio= entrada.p_IdPortafolio;
		EntrdaEjecutivoComercialPortafolio.p_IdEjecutivoComercial= entrada.p_IdEjecutivoComercial;
		
		
		SalidaEjecutarCambioEjecutivo SalidaEjecutivo= EjecutivoComercialPortafolio.logica(conn, EntrdaEjecutivoComercialPortafolio);
				
		
		
	    if( !SalidaEjecutivo.P_OUT_MENSAJES.isEmpty() ) {
				entidadModificada.Mensajes.addAll(SalidaEjecutivo.P_OUT_MENSAJES);
				salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadModificada);
		    }
		    
		    if(entidadModificada.Mensajes.isEmpty()) {
				entidadModificada = WebServiceUtils.agregarMensajeOk(entidadModificada,"MODIFICADA");
				salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadModificada);
		    }
		
	
		salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ENTIDADES_MANIPULADAS.parallelStream().reduce(0L,
				(sum, ent) -> sum += ent.Mensajes.parallelStream().filter(mensaje ->PlataformaUtils.MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo)).count(),
				(sum1, sum2) -> sum1 + sum2);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarIdEjecutivoComercial", finServicio - inicioServicio);
		
		return salida;
		
	}
	
	public static void main(String args[]) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
						.serializeNulls().create();

			String json= "{\"p_IdPortafolio\":159540000,\"p_IdEjecutivoComercial\":80,\"p_IdEmpresa\":1,\"p_IdPersona\":145428,\"p_IdUsuario\":9582}";
			System.out.println("ENTRADA=");
			
			EntradaModificarIdEjecutivoComercialWS entradaModificarIdEjecutivoComercialWS = gson.fromJson(json, EntradaModificarIdEjecutivoComercialWS.class);
			ModificarIdEjecutivoComercialWS ModificarIdEjecutivoComercialWS= new ModificarIdEjecutivoComercialWS();
			try {
				SalidaWBS salida2 = ModificarIdEjecutivoComercialWS.logica(con, entradaModificarIdEjecutivoComercialWS);

				//con.commit();
				con.rollback();
				con.close();
				System.out.println("SALIDA=");
				System.out.println(salida2.toString());
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
				con.close();
			}
		}
		System.out.println("== END ==");
	}

}