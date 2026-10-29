package cl.bcs.tesoreria;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarComprobantes implements JavaCall {

	private final String SQL_SELECT_COMPROBANTES = loadResourceToString("cl/bcs/tesoreria/sql/ConsultarComprobantes_SelectComprobantes.sql");
    static final Logger logger = LogManager.getLogger(ConsultarComprobantes.class);

    public static class  EntradaConsultarComprobantes extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public List<IdComprobantes> p_IdComprobantes;
	
		public EntradaConsultarComprobantes() {
		    this.p_IdEmpresa = 0L;
		    this.p_IdUsuario = 0L;
		    this.p_IdModulo = "TES";
	    }
    }
    
    public static class SalidaConsultarComprobantes extends BcsPojo {

		public Long 								P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> 					P_OUT_MENSAJES;
		public List<Comprobante_Proceso>			P_OUT_COMPROBANTES;		//COMPROBANTES REFACTURABLES		
		
		public SalidaConsultarComprobantes() {
			this.P_OUT_COMPROBANTES = new ArrayList<>();
			this.P_OUT_MENSAJES = new ArrayList<>();
			this.P_OUT_ERRORESCONTADOR = 0L;
		}
		
	}
    
    public static class  IdComprobantes extends BcsPojo {
    	
		public Long IdComprobante;
		
	}
    
    public static class Comprobante_Proceso extends BcsPojo {
    	
    	public Long IdComprobante;
    	public String CuentaDCVOrigen;
    	public String CuentaDCVDestino;
    	public String IdMoneda;
    	public BigDecimal Monto;
    	public java.sql.Date FechaLiquidacion;
    	public String NombrePersona;
    	public String DocumentoIdentidad;
    	public String NombreConcepto;
    	public String NombreEstado;
    	public String Nemotecnico;
	}

    @Override
    public JsonObject call(Connection conn, JsonObject jin) throws Exception {
	Gson gson = Utils.getGsonBuilder();
	EntradaConsultarComprobantes in = gson.fromJson(jin.toString(), EntradaConsultarComprobantes.class);
	SalidaConsultarComprobantes out = logica(conn, in);
	JsonParser parser = new JsonParser();
	JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
	return dataout;
    }

    private SalidaConsultarComprobantes logica(Connection conn, EntradaConsultarComprobantes entrada) throws Exception {
    	
		logger.debug("INICIO::ConsultarComprobantes");
		
		Long init 							= System.currentTimeMillis();
		SalidaConsultarComprobantes salida 	= new SalidaConsultarComprobantes();
		SQL sql 							= new SQL();
		
		HashMap<String,Object> ParamIdComprobantes = new HashMap<>(1);
		List<Long> listadoIdsComprobantes = new ArrayList<Long>(entrada.p_IdComprobantes.size());
		
		for( IdComprobantes IdComprobante: entrada.p_IdComprobantes ) {
			listadoIdsComprobantes.add(IdComprobante.IdComprobante);
		}
		
		ParamIdComprobantes.put("IdsComprobantes", listadoIdsComprobantes);
		List<Comprobante_Proceso> ListadoComprobantes = sql.query(conn, SQL_SELECT_COMPROBANTES, ParamIdComprobantes, Comprobante_Proceso.class);
		salida.P_OUT_COMPROBANTES = ListadoComprobantes;
		
		logger.info("FIN::ConsultarComprobantes, " + (System.currentTimeMillis() - init));

		return salida;
	
    }

    public static void main(String[] args) throws SQLException {
    	
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
		    
			String json = "{\"p_IdModulo\":\"TES\",\"p_IdEmpresa\":1,\"p_IdUsuario\":1646,\"p_IdComprobantes\":[{\"IdComprobante\":3494},{\"IdComprobante\":3493}]}";
		    EntradaConsultarComprobantes entrada = Utils.getGsonBuilder().fromJson(json, EntradaConsultarComprobantes.class);
	
		    System.out.println("== ENTRADA ==");
		    System.out.println(entrada);
	
		    try {
		    	SalidaConsultarComprobantes salida = new ConsultarComprobantes().logica(con, entrada);
				System.out.println("== SALIDA ==");
				System.out.println(salida);
		    } catch (Exception er) {
		    	er.printStackTrace();
		    } finally {
		    	con.rollback();
		    }
	
		}
		System.out.println("== END ==");
    }

}
