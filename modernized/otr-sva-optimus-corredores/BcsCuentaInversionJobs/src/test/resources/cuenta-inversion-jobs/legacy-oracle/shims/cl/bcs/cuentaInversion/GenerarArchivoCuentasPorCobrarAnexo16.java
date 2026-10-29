package cl.bcs.cuentaInversion;

import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;

/**
 * ORACLE SHIM of legacy cl.bcs.cuentaInversion.GenerarArchivoCuentasPorCobrarAnexo16: the Entrada / Salida classes are VERBATIM copies of the legacy nested classes (field names, types and
 * constructor defaults), the service itself is a FAKE: it records {servicio, entrada} (Hooks.registrar), may throw what the case scripts
 * ("colaboradores.GenerarArchivoCuentasPorCobrarAnexo16.lanza"), may run scripted SQL on the job connection ("colaboradores.GenerarArchivoCuentasPorCobrarAnexo16.sql": the side effects the real
 * service would leave, so commit / rollback are observable in data) and answers "colaboradores.GenerarArchivoCuentasPorCobrarAnexo16.salida" decoded into the legacy
 * Salida class (an absent field takes the legacy constructor default).
 */
public class GenerarArchivoCuentasPorCobrarAnexo16 implements JavaCall {

    public static class GenerarArchivoCuentasPorCobrarAnexo16_Entrada extends BcsPojo {
            public Long p_IdEmpresa;
            public Long p_IdUsuario;
            public String p_FechaHasta;
        }

    public static class GenerarArchivoCuentasPorCobrarAnexo16_Salida extends BcsPojo {
            public List <MensajeSalida> P_OUT_MENSAJES;
            public Long P_OUT_ERRORESCONTADOR;
            public String P_OUT_RUTAARCHIVO;
    
            public GenerarArchivoCuentasPorCobrarAnexo16_Salida() {
            	this.P_OUT_ERRORESCONTADOR = 0L;
                this.P_OUT_MENSAJES		   = new ArrayList<MensajeSalida>();
            }
        }

    public GenerarArchivoCuentasPorCobrarAnexo16_Salida logica(Connection con, GenerarArchivoCuentasPorCobrarAnexo16_Entrada entrada) throws Exception {
        return cl.bcs.oracle.Hooks.servicio("GenerarArchivoCuentasPorCobrarAnexo16", entrada, GenerarArchivoCuentasPorCobrarAnexo16_Salida.class, con);
    }

    @Override
    public JsonObject call(Connection conn, JsonObject in) throws Exception {
        return null;
    }
}
