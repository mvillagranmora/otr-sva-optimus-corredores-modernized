package cl.bcs.cuentaInversion;

import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;

/**
 * ORACLE SHIM of legacy cl.bcs.cuentaInversion.GenerarInformePatrimonioDiario: the Entrada / Salida classes are VERBATIM copies of the legacy nested classes (field names, types and
 * constructor defaults), the service itself is a FAKE: it records {servicio, entrada} (Hooks.registrar), may throw what the case scripts
 * ("colaboradores.GenerarInformePatrimonioDiario.lanza"), may run scripted SQL on the job connection ("colaboradores.GenerarInformePatrimonioDiario.sql": the side effects the real
 * service would leave, so commit / rollback are observable in data) and answers "colaboradores.GenerarInformePatrimonioDiario.salida" decoded into the legacy
 * Salida class (an absent field takes the legacy constructor default).
 */
public class GenerarInformePatrimonioDiario implements JavaCall {

    public static class EntradaGenerarInformePatrimonioDiario extends BcsPojo{
            public Long     p_IdEmpresa;
            public Long     p_IdUsuario;
            public String   p_IdModulo;
            public String   p_Ruta;
            public Long     p_IdCliente;
            public Long     p_IdPortafolio;
            public String   p_IdEstado;
            public Long     p_IdProductoAgrupador;
            public String   p_FechaDesde;
            public String   p_FechaHasta;
            public Long     p_Periodo;
            
            
        }

    public static class SalidaGenerarInformePatrimonioDiario extends BcsPojo{
            public String       p_out_NombreArchivoLog;
            public String       p_out_RutaArchivoLog;
            public String       p_out_NombreArchivo;
            public String       p_out_RutaArchivo;
            public Long         p_out_ErroresContador;
            public String       p_out_Comentario;
            public List<String> p_out_ArchivoSecundario;
    
        }

    public SalidaGenerarInformePatrimonioDiario logica(Connection con, EntradaGenerarInformePatrimonioDiario entrada) throws Exception {
        return cl.bcs.oracle.Hooks.servicio("GenerarInformePatrimonioDiario", entrada, SalidaGenerarInformePatrimonioDiario.class, con);
    }

    @Override
    public JsonObject call(Connection conn, JsonObject in) throws Exception {
        return null;
    }
}
