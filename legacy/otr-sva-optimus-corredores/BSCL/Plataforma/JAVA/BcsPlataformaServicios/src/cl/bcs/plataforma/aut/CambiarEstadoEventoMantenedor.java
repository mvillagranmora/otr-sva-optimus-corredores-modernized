package cl.bcs.plataforma.aut;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cl.bcs.plataforma.aut.ModificarEstadoEventoAutorizacionMasivo.ServicioSalidaAdvertencia;
import cl.bcs.plataforma.aut.ModificarEstadoEventoAutorizacionMasivo.ServicioSalidaError;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CambiarEstadoEventoMantenedor implements JavaCall {


	private static final Logger logger = LogManager.getLogger(ModificarEstadoEventoAutorizacionMasivo.class);
	
    private final String SQL_ModificaEstadoTipoEvento = loadResourceToString("cl/bcs/plataforma/aut/sql/ModificaEstadoTipoEvento.sql");
    
    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdTipoEvento;
        public String p_IdEstado;

        public String toString() {
            return Utils.printJson(this, false);
        }
    }

    public static class Evento {
        public Long IdEmpresa;
        public String IdTipoEvento;
        public String Nombre;
        public String IdEstado;
        public String toString() {
            return Utils.printJson(this, false);
        }
    }

    public static class Salida{
        public Long P_OUT_ERRORESCONTADOR;
        public Long P_OUT_ADVERTENCIASCONTADOR;
        public List<ServicioSalidaAdvertencia> P_OUT_ADVERTENCIADETALLE;
        public List<ServicioSalidaError> P_OUT_ERRORESDETALLE;

        public String toString() {
            return Utils.printJson(this, false);
        }
    }
    
    @Override
    public JsonObject call(Connection con, JsonObject jin) throws Exception {
        Gson gson = getGsonBuilder();
        Entrada in = gson.fromJson(jin.toString(), Entrada.class);
        Salida out = logica(con, in);
        JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
        return dataout;
    }

    
    public Salida logica(Connection con, Entrada in) throws Exception {
        logger.info("Inicio ");
        SQL sql = new SQL();
        Salida out = new Salida();
        out.P_OUT_ERRORESCONTADOR = new Long(0);
        out.P_OUT_ADVERTENCIASCONTADOR = new Long(0);
        out.P_OUT_ADVERTENCIADETALLE = new ArrayList<ServicioSalidaAdvertencia>();
        out.P_OUT_ERRORESDETALLE = new ArrayList<ServicioSalidaError>();
        
        List<Evento> params = new ArrayList<Evento>();
        Evento evt = new Evento();
        evt.IdTipoEvento = in.p_IdTipoEvento;
        evt.IdEmpresa = in.p_IdEmpresa;
        
        if(in.p_IdEstado.equals("VIG")) {
        	 evt.IdEstado = "INA";	
		}else {
			evt.IdEstado = "VIG";
		}
        params.add(evt);
        sql.batch(con, SQL_ModificaEstadoTipoEvento, params, Evento.class);
        
       return out; 
    }
        

    public static void main(String[] args) {
        System.out.println("== START ==");
        try (Connection con = PlataformaUtils.getDESAConnection()){
            con.setAutoCommit(false);
           
                JsonParser parser = new JsonParser();
                String lejson = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdTipoEvento\":\"MODNOT\",\"p_IdEstado\":\"VIG\"}";
                JsonObject dataJsonIn = parser.parse(lejson).getAsJsonObject();
                CambiarEstadoEventoMantenedor demo = new CambiarEstadoEventoMantenedor();
                System.out.println(demo.call(con, dataJsonIn).toString());
                con.rollback();
                //con.commit();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("== END ==");
    }

}
