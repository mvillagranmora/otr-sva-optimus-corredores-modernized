package cl.bcs.cuentaInversion;

import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;

/**
 * ORACLE SHIM of legacy cl.bcs.cuentaInversion.ActualizarSaldoCargoAbono: the Entrada / Salida classes are VERBATIM copies of the legacy nested classes (field names, types and
 * constructor defaults), the service itself is a FAKE: it records {servicio, entrada} (Hooks.registrar), may throw what the case scripts
 * ("colaboradores.ActualizarSaldoCargoAbono.lanza"), may run scripted SQL on the job connection ("colaboradores.ActualizarSaldoCargoAbono.sql": the side effects the real
 * service would leave, so commit / rollback are observable in data) and answers "colaboradores.ActualizarSaldoCargoAbono.salida" decoded into the legacy
 * Salida class (an absent field takes the legacy constructor default).
 */
public class ActualizarSaldoCargoAbono implements JavaCall {

    public static class ActualizarSaldoCargoAbonoEntrada {
    		public Long p_IdEmpresa;
    	    public Long p_IdUsuario;
    		public String p_FechaMovimiento;
    		public String p_ActualizarTodosLosSaldos;
    		public List<CuentaInversion> p_CuentaInversion;
    		
    		public ActualizarSaldoCargoAbonoEntrada() {
    			p_ActualizarTodosLosSaldos = "N";
    			p_CuentaInversion = new ArrayList<CuentaInversion>();
    		};
    		
    		public String toString() {
    			return Utils.printJson(this, false);
    		};
    		
    	}

    public static class ActualizarSaldoCargoAbonoSalida {
    		public List<MensajeSalida> P_OUT_MENSAJES;
    		public Long P_OUT_ERRORESCONTADOR;
    		
    		public ActualizarSaldoCargoAbonoSalida() {
    			P_OUT_MENSAJES = new ArrayList<>();
    			P_OUT_ERRORESCONTADOR = 0L;
    		}
    		
    		public String toString() {
    			return Utils.printJson(this, false);
    		}
    	}

    public static class CuentaInversion{
    		public Long IdCuentaInversion;	
    		
    		public String toString() {
    			return Utils.printJson(this, false);
    		}		
    	}

    public ActualizarSaldoCargoAbonoSalida logica(Connection con, ActualizarSaldoCargoAbonoEntrada entrada) throws Exception {
        return cl.bcs.oracle.Hooks.servicio("ActualizarSaldoCargoAbono", entrada, ActualizarSaldoCargoAbonoSalida.class, con);
    }

    @Override
    public JsonObject call(Connection conn, JsonObject in) throws Exception {
        return null;
    }
}
