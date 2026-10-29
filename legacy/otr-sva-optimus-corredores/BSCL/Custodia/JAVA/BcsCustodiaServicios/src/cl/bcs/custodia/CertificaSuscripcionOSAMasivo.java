package cl.bcs.custodia;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.EnviarMovimientoFacturarSuscripcionOSA_Masivo.EnviarMovimientoFacturarSuscripcionOSA_Entrada;
import cl.bcs.custodia.EnviarMovimientoFacturarSuscripcionOSA_Masivo.EnviarMovimientoFacturarSuscripcionOSA_Salida;
import cl.bcs.custodia.EnviarMovimientoFacturarSuscripcionOSA_Masivo.EventoCapitalClienteFac;
import cl.bcs.custodia.EnviarMovimientoFacturarSuscripcionOSA_Masivo.EventoCapitalClienteFormaPago;
import cl.bcs.custodia.EnviarMovimientoFacturarSuscripcionOSA_Masivo.UpdateEventoCapitalCliente;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CertificaSuscripcionOSAMasivo implements JavaCall {
	private static final Logger logger = LogManager.getLogger(EnviarMovimientoFacturarSuscripcionOSA_Masivo.class);
	
	private final String SQL_SELECT_COUNT_EVENTO_CAPITAL_CLIENTE_MASIVO = loadResourceToString("cl/bcs/custodia/sql/CertificaSuscripcionOSA_SelectCountEventoCapitalClientesMasivo.sql");
	private final String SQL_UPDATE_EVENTO_CAPITAL_CLIENTE = loadResourceToString("cl/bcs/custodia/sql/CertificaSuscripcionOSA_UpdateEventoCapitalClientes.sql");

	private final String estadoVigente = "VIG";
	
	public class CertificaSuscripcionOSAMasivo_Entrada{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public List<Long> p_IdEventoCapitalCliente;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public class CertificaSuscripcionOSAMasivo_Salida{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida>	P_OUT_MENSAJES;
		
		public CertificaSuscripcionOSAMasivo_Salida(){
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class EventoCapitalClienteCert{
		public Long IdEventoCapitalCliente;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public Long getFaltantesEventoCapitalCliente(List<Long> entrada, List<EventoCapitalClienteCert> resultadoConsulta) {
		Boolean existe = false;
		Integer faltantes = 0;
		for (Long id : entrada) {
			existe = false;
			for(EventoCapitalClienteCert evento : resultadoConsulta) {
				if(evento.IdEventoCapitalCliente.equals(id)) {
					existe = true;
					break;
				}
			}
			if(!existe) {
				faltantes++;
			}				
		}
		
		return (long)faltantes;
	}
	
	public CertificaSuscripcionOSAMasivo_Salida logica(Connection con, CertificaSuscripcionOSAMasivo_Entrada entrada) throws Exception{
		CertificaSuscripcionOSAMasivo_Salida out = new CertificaSuscripcionOSAMasivo_Salida();
		
		logger.debug("INICIO::CertificaSuscripcionOSAMasivo");
		SQL sql=new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>();
		
				
		params.put("ListaIdEventoCapitalCliente", entrada.p_IdEventoCapitalCliente);
		params.put("IdEstado", estadoVigente);
		List<EventoCapitalClienteCert> eventoCapitalClientes = sql.query(con, SQL_SELECT_COUNT_EVENTO_CAPITAL_CLIENTE_MASIVO, params, EventoCapitalClienteCert.class);
		
		Long faltantes = getFaltantesEventoCapitalCliente(entrada.p_IdEventoCapitalCliente, eventoCapitalClientes);
		if (!faltantes.equals(0L)) {
			out.P_OUT_ERRORESCONTADOR = (long)faltantes;
			MensajeSalida msj = new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_EXISTE_EVENTO_CAPITAL_CLIENTE",new HashMap<String,String>(),con,entrada.p_IdEmpresa);
			out.P_OUT_MENSAJES.add(msj);
			return out;
		}
		
		List<UpdateEventoCapitalCliente> upds = new ArrayList<UpdateEventoCapitalCliente>();
		UpdateEventoCapitalCliente upd = new UpdateEventoCapitalCliente();
		for (Long id : entrada.p_IdEventoCapitalCliente) {
			upd = new UpdateEventoCapitalCliente();
			upd.IdEventoCapitalCliente = id;
			upds.add(upd);
		}
		
		sql.batch(con, SQL_UPDATE_EVENTO_CAPITAL_CLIENTE, upds, UpdateEventoCapitalCliente.class);
		

		logger.debug("FIN::CertificaSuscripcionOSAMasivo");
		return out;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = new Gson();	
		CertificaSuscripcionOSAMasivo_Entrada in=gson.fromJson(jin.toString() ,CertificaSuscripcionOSAMasivo_Entrada.class);
		CertificaSuscripcionOSAMasivo_Salida out= logica(conn,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"CUS\",\"p_IdEventoCapitalCliente\": [97476]}";
			CertificaSuscripcionOSAMasivo_Entrada Objin = gson.fromJson(json, CertificaSuscripcionOSAMasivo_Entrada.class);
			
			CertificaSuscripcionOSAMasivo demo=new CertificaSuscripcionOSAMasivo();				
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			try {
 				CertificaSuscripcionOSAMasivo_Salida ObjOut= demo.logica(con, Objin);
 				//con.commit();
 				con.rollback();
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}
 			//con.commit();
 			
	    	}
		 	
		System.out.println("== END ==");
	}
}
