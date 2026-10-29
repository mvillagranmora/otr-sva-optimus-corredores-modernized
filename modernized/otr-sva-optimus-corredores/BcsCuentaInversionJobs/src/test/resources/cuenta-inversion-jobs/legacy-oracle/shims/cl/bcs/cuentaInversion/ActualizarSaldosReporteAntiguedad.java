package cl.bcs.cuentaInversion;

import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;

/**
 * ORACLE SHIM of legacy cl.bcs.cuentaInversion.ActualizarSaldosReporteAntiguedad: the Entrada / Salida classes are VERBATIM copies of the legacy nested classes (field names, types and
 * constructor defaults), the service itself is a FAKE: it records {servicio, entrada} (Hooks.registrar), may throw what the case scripts
 * ("colaboradores.ActualizarSaldosReporteAntiguedad.lanza"), may run scripted SQL on the job connection ("colaboradores.ActualizarSaldosReporteAntiguedad.sql": the side effects the real
 * service would leave, so commit / rollback are observable in data) and answers "colaboradores.ActualizarSaldosReporteAntiguedad.salida" decoded into the legacy
 * Salida class (an absent field takes the legacy constructor default).
 */
public class ActualizarSaldosReporteAntiguedad implements JavaCall {

    public static class EntradaActualizarSaldosReporteAntiguedad extends BcsPojo{
    		public Long p_IdEmpresa;
    	    public Long p_IdUsuario;
    	    public String p_Rut;
    	    public String p_CodigoPortafolio;
    	    public String p_Moneda;
    		public String p_FechaMovimiento;
    	}

    public static class SalidaActualizarSaldosReporteAntiguedad extends BcsPojo{
    		public List<MensajeSalida> P_OUT_MENSAJES;
    		public Long P_OUT_ERRORESCONTADOR;
    		
    		public SalidaActualizarSaldosReporteAntiguedad() {
    			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
    			this.P_OUT_ERRORESCONTADOR = 0L;
    		}
    	}

    public SalidaActualizarSaldosReporteAntiguedad logica(Connection con, EntradaActualizarSaldosReporteAntiguedad entrada) throws Exception {
        return cl.bcs.oracle.Hooks.servicio("ActualizarSaldosReporteAntiguedad", entrada, SalidaActualizarSaldosReporteAntiguedad.class, con);
    }

    @Override
    public JsonObject call(Connection conn, JsonObject in) throws Exception {
        return null;
    }
}
