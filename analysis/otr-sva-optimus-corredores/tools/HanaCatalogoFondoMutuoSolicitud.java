import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;
import java.util.*;

/**
 * READ-ONLY catalog (metadata only, no business data) of the objects of RULES_FONDOMUTUO_SOLICITUD.md section 6. A fixed whitelist of SELECT statements over SYS.* catalog views;
 * the connection is read-only and rolled back; no INSERT/UPDATE/DELETE/DDL exists in this program.
 * Credentials come from the local .env keys qa01.saphana.* and are never printed or written.
 * usage: java -cp ngdbc.jar HanaCatalogoFondoMutuoSolicitud.java <envFile> <outDir>
 */
public class HanaCatalogoFondoMutuoSolicitud {
    static final String[] NOMBRES = {
        "FMM.SolicitudAporteRescate", "FMM.SerieDeFondo", "FMM.Fondo", "FMM.TipoFondo", "FMM.TipoMovimientoFondoMutuo",
        "FMM.RegimenFondoMutuo", "FMM.RegimenSerieDeFondo", "FMM.ConfiguracionPropositoPortafolio", "FMM.ConfiguracionFechasFondoMutuo",
        "FMM.ConfiguracionFondoMutuo", "FMM.ValorCuota", "FMM.ValorTipoCambio", "FMM.Comision", "FMM.InterfazAgf",
        "FMM.CrearSolicitudAporteRescateNormal_BloqueoSolicitudAporteRescate", "FMM.ConsultarSolicitudAporteRescate",
        "FMM.ConsultarTramoComision", "FMM.ConsultarComisionMaxima", "IND.Indicador", "IND.TipoIndicador", "PLA.Configuracion",
        "REG.DiaFeriado", "ACC.Usuario", "ACC.ConsultarRolesPerfilesUsuario", "MER.Emisor", "PAG.MedioDePagoYModalidadDePago",
        "REC.RegimenTributario", "REC.ConsultarRegimenTributario", "TRA.Traspaso", "TRA.OrdenTraspaso", "ConsultarPortafolio",
        "CLI.ConsultarClienteEstado", "PER.ConsultarPersonaNombreCompleto", "EMP.Empleado", "GBL.ValidaPatrimonioEmisor_Proceso_Paso"
    };
    static final String[] SECUENCIAS = {"SolicitudAporteRescate", "FolioContrato", "InterfazAgf", "SecuenciaMovimiento"};
    static final String[] PROCEDIMIENTOS = {"CrearBloqueoSolicitudAporteRescate", "EliminarBloqueoSolicitudAporteRescate"};
    static final LinkedHashMap<String, String> QUERIES = new LinkedHashMap<>();

    static String lista(String[] v) {
        StringBuilder b = new StringBuilder();
        for (String s : v) b.append(b.length() > 0 ? "," : "").append("'").append(s.replace("'", "")).append("'");
        return b.toString();
    }

    static {
        String in = lista(NOMBRES);
        QUERIES.put("fmm_columnas_tablas", "SELECT TABLE_NAME, COLUMN_NAME, POSITION, DATA_TYPE_NAME, LENGTH, SCALE, IS_NULLABLE FROM SYS.TABLE_COLUMNS WHERE SCHEMA_NAME = 'BSCL' AND SUBSTR_AFTER(TABLE_NAME, '::') IN (" + in + ") ORDER BY TABLE_NAME, POSITION");
        QUERIES.put("fmm_columnas_vistas", "SELECT VIEW_NAME, COLUMN_NAME, POSITION, DATA_TYPE_NAME, LENGTH, SCALE, IS_NULLABLE FROM SYS.VIEW_COLUMNS WHERE SCHEMA_NAME = 'BSCL' AND SUBSTR_AFTER(VIEW_NAME, '::') IN (" + in + ") ORDER BY VIEW_NAME, POSITION");
        QUERIES.put("fmm_tablas_tipo", "SELECT TABLE_NAME, TABLE_TYPE, IS_TEMPORARY, TEMPORARY_TABLE_TYPE FROM SYS.TABLES WHERE SCHEMA_NAME = 'BSCL' AND SUBSTR_AFTER(TABLE_NAME, '::') IN (" + in + ") ORDER BY TABLE_NAME");
        QUERIES.put("fmm_vistas", "SELECT VIEW_NAME, VIEW_TYPE FROM SYS.VIEWS WHERE SCHEMA_NAME = 'BSCL' AND SUBSTR_AFTER(VIEW_NAME, '::') IN (" + in + ") ORDER BY VIEW_NAME");
        QUERIES.put("fmm_claves", "SELECT TABLE_NAME, COLUMN_NAME, POSITION, IS_PRIMARY_KEY, IS_UNIQUE_KEY FROM SYS.CONSTRAINTS WHERE SCHEMA_NAME = 'BSCL' AND SUBSTR_AFTER(TABLE_NAME, '::') IN (" + in + ") ORDER BY TABLE_NAME, CONSTRAINT_NAME, POSITION");
        QUERIES.put("fmm_secuencias", "SELECT SEQUENCE_NAME, START_NUMBER, MIN_VALUE, MAX_VALUE, INCREMENT_BY, IS_CYCLED FROM SYS.SEQUENCES WHERE SCHEMA_NAME = 'BSCL' AND SUBSTR_AFTER(SEQUENCE_NAME, '::') IN (" + lista(SECUENCIAS) + ") ORDER BY SEQUENCE_NAME");
        QUERIES.put("fmm_procedimientos", "SELECT PROCEDURE_NAME, CASE WHEN DEFINITION LIKE '%AUTONOMOUS TRANSACTION%' THEN 'TRUE' ELSE 'FALSE' END AS AUTONOMOUS_TRANSACTION, READ_ONLY FROM SYS.PROCEDURES WHERE SCHEMA_NAME = 'BSCL' AND SUBSTR_AFTER(PROCEDURE_NAME, '::') IN (" + lista(PROCEDIMIENTOS) + ") ORDER BY PROCEDURE_NAME");
        QUERIES.put("fmm_procedimientos_param", "SELECT PROCEDURE_NAME, PARAMETER_NAME, POSITION, DATA_TYPE_NAME, PARAMETER_TYPE FROM SYS.PROCEDURE_PARAMETERS WHERE SCHEMA_NAME = 'BSCL' AND SUBSTR_AFTER(PROCEDURE_NAME, '::') IN (" + lista(PROCEDIMIENTOS) + ") ORDER BY PROCEDURE_NAME, POSITION");
    }

    public static void main(String[] a) throws Exception {
        Map<String, String> env = new HashMap<>();
        for (String l : Files.readAllLines(Paths.get(a[0]), StandardCharsets.UTF_8)) {
            int i = l.indexOf('=');
            if (i > 0 && !l.startsWith("#")) env.put(l.substring(0, i).trim(), l.substring(i + 1).trim());
        }
        Properties p = new Properties();
        p.setProperty("user", env.get("qa01.saphana.dbuser"));
        p.setProperty("password", env.get("qa01.saphana.dbpass"));
        for (String kv : env.getOrDefault("qa01.saphana.dbprop", "").split(";")) {
            int i = kv.indexOf('=');
            if (i > 0) p.setProperty(kv.substring(0, i).trim(), kv.substring(i + 1).trim());
        }
        Class.forName(env.getOrDefault("qa01.saphana.dbdrv", "com.sap.db.jdbc.Driver"));
        Path out = Paths.get(a[1]);
        Files.createDirectories(out);
        try (Connection cn = DriverManager.getConnection(env.get("qa01.saphana.dbURL"), p)) {
            cn.setReadOnly(true);
            cn.setAutoCommit(false);
            for (Map.Entry<String, String> q : QUERIES.entrySet()) {
                if (!q.getValue().startsWith("SELECT")) throw new IllegalStateException("only SELECT is allowed");
                try (Statement st = cn.createStatement(); ResultSet rs = st.executeQuery(q.getValue());
                     BufferedWriter w = Files.newBufferedWriter(out.resolve(q.getKey() + ".csv"), StandardCharsets.UTF_8)) {
                    ResultSetMetaData md = rs.getMetaData();
                    int n = md.getColumnCount();
                    StringBuilder h = new StringBuilder();
                    for (int c = 1; c <= n; c++) h.append(c > 1 ? "," : "").append(md.getColumnLabel(c));
                    w.write(h + "\n");
                    int rows = 0;
                    while (rs.next()) {
                        StringBuilder r = new StringBuilder();
                        for (int c = 1; c <= n; c++) {
                            String v = rs.getString(c);
                            if (v == null) v = "";
                            v = v.replace("\r", " ").replace("\n", " ");
                            if (v.contains(",") || v.contains("\"")) v = "\"" + v.replace("\"", "\"\"") + "\"";
                            r.append(c > 1 ? "," : "").append(v);
                        }
                        w.write(r + "\n");
                        rows++;
                    }
                    System.out.println(q.getKey() + ": " + rows + " rows");
                } catch (SQLException e) {
                    System.out.println(q.getKey() + ": ERROR " + e.getErrorCode() + " " + String.valueOf(e.getMessage()).split("\n")[0]);
                }
            }
            cn.rollback();
        }
    }
}
