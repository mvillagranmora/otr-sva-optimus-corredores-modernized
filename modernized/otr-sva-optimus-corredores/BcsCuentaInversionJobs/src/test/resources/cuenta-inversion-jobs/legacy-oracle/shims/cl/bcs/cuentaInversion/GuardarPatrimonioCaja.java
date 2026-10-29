package cl.bcs.cuentaInversion;

import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;

/**
 * ORACLE SHIM of legacy cl.bcs.cuentaInversion.GuardarPatrimonioCaja: the Entrada / Salida classes are VERBATIM copies of the legacy nested classes (field names, types and
 * constructor defaults), the service itself is a FAKE: it records {servicio, entrada} (Hooks.registrar), may throw what the case scripts
 * ("colaboradores.GuardarPatrimonioCaja.lanza"), may run scripted SQL on the job connection ("colaboradores.GuardarPatrimonioCaja.sql": the side effects the real
 * service would leave, so commit / rollback are observable in data) and answers "colaboradores.GuardarPatrimonioCaja.salida" decoded into the legacy
 * Salida class (an absent field takes the legacy constructor default).
 */
public class GuardarPatrimonioCaja implements JavaCall {

    public static class EntradaGuardarPatrimonioCaja{
    		public Long p_IdEmpresa;
    		public Long p_IdUsuario;
    		public String p_IdModulo;
    		public String p_Fecha;
    		
    		public String toString() {
    			return Utils.printJson(this, false);
    		}
    	}

    public static class SalidaGuardarPatrimonioCaja{
    		public Long P_OUT_ERRORESCONTADOR;
    		public List <MensajeSalida> P_OUT_MENSAJES;
    		
    		public SalidaGuardarPatrimonioCaja() {
    			this.P_OUT_ERRORESCONTADOR=0L;
    			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
    		}
    		
    		public String toString() {
    			return Utils.printJson(this, false);
    		}
    	}

    public SalidaGuardarPatrimonioCaja logica(Connection con, EntradaGuardarPatrimonioCaja entrada) throws Exception {
        return cl.bcs.oracle.Hooks.servicio("GuardarPatrimonioCaja", entrada, SalidaGuardarPatrimonioCaja.class, con);
    }

    @Override
    public JsonObject call(Connection conn, JsonObject in) throws Exception {
        return null;
    }
}
