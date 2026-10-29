package cl.bcs.cuentaInversion;

import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;

/**
 * ORACLE SHIM of legacy cl.bcs.cuentaInversion.GenerarReporteAcreedores: the Entrada / Salida classes are VERBATIM copies of the legacy nested classes (field names, types and
 * constructor defaults), the service itself is a FAKE: it records {servicio, entrada} (Hooks.registrar), may throw what the case scripts
 * ("colaboradores.GenerarReporteAcreedores.lanza"), may run scripted SQL on the job connection ("colaboradores.GenerarReporteAcreedores.sql": the side effects the real
 * service would leave, so commit / rollback are observable in data) and answers "colaboradores.GenerarReporteAcreedores.salida" decoded into the legacy
 * Salida class (an absent field takes the legacy constructor default).
 */
public class GenerarReporteAcreedores implements JavaCall {

    public static class EntradaGenerarReporteAcreedores extends BcsPojo{
    		public Long 	p_IdEmpresa;
    		public Long 	p_IdUsuario;
    		public String 	p_IdModulo;					
        	public String	p_FechaSolicitud;    	
        	public String 	p_IdMoneda;
        	public String	p_Consolidar;
        	public String 	p_NombreInternoArchivo;
    	}

    public static class SalidaGenerarReporteAcreedores extends BcsPojo{
    		public List<MensajeSalida>			P_OUT_MENSAJES;
    		public Long							P_OUT_ERRORESCONTADOR;
    		public String 						P_OUT_RUTAARCHIVO;
    		public String 						P_OUT_CODIGOCORREDOR;
    		
    		public SalidaGenerarReporteAcreedores() {
    			this.P_OUT_MENSAJES  		= new ArrayList<MensajeSalida>();
    			this.P_OUT_ERRORESCONTADOR 	= 0L;	
    			this.P_OUT_RUTAARCHIVO 		= "";
    			this.P_OUT_CODIGOCORREDOR	= "";
    		}
    	}

    public SalidaGenerarReporteAcreedores logica(Connection con, EntradaGenerarReporteAcreedores entrada) throws Exception {
        return cl.bcs.oracle.Hooks.servicio("GenerarReporteAcreedores", entrada, SalidaGenerarReporteAcreedores.class, con);
    }

    @Override
    public JsonObject call(Connection conn, JsonObject in) throws Exception {
        return null;
    }
}
