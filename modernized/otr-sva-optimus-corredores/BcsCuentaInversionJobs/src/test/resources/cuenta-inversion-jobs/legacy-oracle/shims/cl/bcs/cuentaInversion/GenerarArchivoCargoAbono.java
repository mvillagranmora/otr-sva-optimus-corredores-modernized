package cl.bcs.cuentaInversion;

import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;

/**
 * ORACLE SHIM of legacy cl.bcs.cuentaInversion.GenerarArchivoCargoAbono: the Entrada / Salida classes are VERBATIM copies of the legacy nested classes (field names, types and
 * constructor defaults), the service itself is a FAKE: it records {servicio, entrada} (Hooks.registrar), may throw what the case scripts
 * ("colaboradores.GenerarArchivoCargoAbono.lanza"), may run scripted SQL on the job connection ("colaboradores.GenerarArchivoCargoAbono.sql": the side effects the real
 * service would leave, so commit / rollback are observable in data) and answers "colaboradores.GenerarArchivoCargoAbono.salida" decoded into the legacy
 * Salida class (an absent field takes the legacy constructor default).
 */
public class GenerarArchivoCargoAbono implements JavaCall {

    public static class GenerarArchivoCargoAbono_Entrada
    	{
    		public Long 	p_IdEmpresa;
    		public Long 	p_IdUsuario;
    		public String 	p_IdModulo;	
        	public String	p_FechaDesde;
        	public String	p_FechaHasta;		
    		public Long		p_IdEjecutivo;
        	public String	p_IdMoneda;
        	public String	p_IdTipoMovimiento;
        	public String	p_IdConcepto;
        	public String	p_IdEstado;
    
        			
    		public String toString() {
    			return Utils.printJson(this, false);
    		}
    	}

    public static class GenerarArchivoCargoAbono_Salida {
    
    		public List<MensajeSalida>			P_OUT_MENSAJES;
    		public Long							P_OUT_ERRORESCONTADOR;
    		public String						P_OUT_RUTAARCHIVO;
    		
    		
    		public Long							P_OUT_CANTIDADREGISTROS;
    		
    		
    		public GenerarArchivoCargoAbono_Salida()
    		{
    			this.P_OUT_MENSAJES 			 = new ArrayList<MensajeSalida>();
    			
    			this.P_OUT_CANTIDADREGISTROS     = 0L;
    		}
    		
    		public String toString() {
    			return Utils.printJson(this, false);
    		}
    	}

    public GenerarArchivoCargoAbono_Salida logica(Connection con, GenerarArchivoCargoAbono_Entrada entrada) throws Exception {
        return cl.bcs.oracle.Hooks.servicio("GenerarArchivoCargoAbono", entrada, GenerarArchivoCargoAbono_Salida.class, con);
    }

    @Override
    public JsonObject call(Connection conn, JsonObject in) throws Exception {
        return null;
    }
}
