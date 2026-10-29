package cl.bcs.cuentaInversion;

import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;

/**
 * ORACLE SHIM of legacy cl.bcs.cuentaInversion.GenerarArchivoSaldoCuentas: the Entrada / Salida classes are VERBATIM copies of the legacy nested classes (field names, types and
 * constructor defaults), the service itself is a FAKE: it records {servicio, entrada} (Hooks.registrar), may throw what the case scripts
 * ("colaboradores.GenerarArchivoSaldoCuentas.lanza"), may run scripted SQL on the job connection ("colaboradores.GenerarArchivoSaldoCuentas.sql": the side effects the real
 * service would leave, so commit / rollback are observable in data) and answers "colaboradores.GenerarArchivoSaldoCuentas.salida" decoded into the legacy
 * Salida class (an absent field takes the legacy constructor default).
 */
public class GenerarArchivoSaldoCuentas implements JavaCall {

    public static class GenerarArchivoSaldoCuentas_Entrada
    	{
    		public Long 	p_IdEmpresa;
    		public Long 	p_IdUsuario;
    		public String 	p_IdModulo;					
        	public String	p_FechaDesde;
        	public String	p_FechaHasta;    	
        	public String	p_SoloClientesSobregirados; // S : N;
        	public String	p_SoloClientesConSaldoDisponible; // S : N;	
        	public String   p_SoloClientesConSaldoDisponibleClientesSobregirados; // S : N;	
        	public String	p_IncluyeProductoApv; // S : N;   
        	public String	p_IncluyeOtrosSaldos; // S : N;  
    		public Long		p_IdEjecutivo;
        	public String	p_IdMoneda;
        	public List<String> p_Encabezados = new ArrayList<>();
        			
    		public String toString() {
    			return Utils.printJson(this, false);
    		}
    	}

    public static class GenerarArchivoSaldoCuentas_Salida {
    
    		public List<MensajeSalida>			P_OUT_MENSAJES;
    		public Long							P_OUT_ERRORESCONTADOR;
    		public String						P_OUT_RUTAARCHIVO;
    		
    		public GenerarArchivoSaldoCuentas_Salida()
    		{	
    			this.P_OUT_MENSAJES 			 = new ArrayList<MensajeSalida>();
    		}
    		
    		public String toString() {
    			return Utils.printJson(this, false);
    		}
    	}

    public GenerarArchivoSaldoCuentas_Salida logica(Connection con, GenerarArchivoSaldoCuentas_Entrada entrada) throws Exception {
        return cl.bcs.oracle.Hooks.servicio("GenerarArchivoSaldoCuentas", entrada, GenerarArchivoSaldoCuentas_Salida.class, con);
    }

    @Override
    public JsonObject call(Connection conn, JsonObject in) throws Exception {
        return null;
    }
}
