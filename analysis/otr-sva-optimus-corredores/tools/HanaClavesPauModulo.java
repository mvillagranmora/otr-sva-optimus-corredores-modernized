import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;
import java.util.*;

/**
 * READ-ONLY catalog (metadata only, no business data): keys of PAU.Modulo (Q-ESM-1 of RULES_PLATAFORMA_ESM.md). A fixed whitelist of SELECT statements over SYS.* catalog views;
 * the connection is read-only and rolled back; no INSERT/UPDATE/DELETE/DDL exists in this program.
 * Credentials come from the local .env keys qa01.saphana.* and are never printed or written.
 * usage: java -cp ngdbc.jar HanaClavesPauModulo.java <envFile> <outDir>
 */
public class HanaClavesPauModulo {
    static final String[] NOMBRES = {
        "PAU.Modulo", "MER.DefinicionParametroFinanciero", "MER.ParametroFinanciero", "MER.UsoParidadParametroFinanciero",
        "MER.UsoParidadYTipoCambio", "PLA.ConsultarConfiguracionFlujoCaja", "POR.Portafolio", "CLI.Cliente", "PER.Persona",
        "PER.DocumentoIdentidad", "COM.ConsultarCategoriaInversionista", "PLA.Moneda", "CTA.CuentaInversion",
        "CTA.CargoAbonoMovimiento", "CUS.Cartera", "CUS.ConsultarCarteraFlujoCaja", "CUS.ConsultarEventoCapitalClienteFlujoCaja",
        "FAC.MovimientoComprobante", "INS.Bolsa", "INS.TipoOperacion", "INS.TipoProducto", "INS.ClasificacionTipoInstrumento",
        "INS.TipoInstrumento", "INS.TipoOperacionTipoInstrumento", "PLA.TipoCanalIngreso", "COM.PerfilComercialBase",
        "COM.PerfilProductos", "COM.DetallePerfilProductos", "COM.PerfilCobros", "COM.DetallePerfilCobros", "COM.Cobro",
        "COM.CobroClasificacion", "COM.ValorCobro", "LIM.Limite", "IND.ConsultarIndicadoresPortafolio",
        "PLA.ConsultarFlujoCaja_Portafolio_Proceso", "GBL.SaldoFlujoCaja_Proceso_Paso"
    };
    static final String[] TEMPORALES = {"PLA.ConsultarFlujoCaja_Portafolio_Proceso", "GBL.SaldoFlujoCaja_Proceso_Paso"};
    static final LinkedHashMap<String, String> QUERIES = new LinkedHashMap<>();

    static String lista(String[] v) {
        StringBuilder b = new StringBuilder();
        for (String s : v) b.append(b.length() > 0 ? "," : "").append("'").append(s.replace("'", "")).append("'");
        return b.toString();
    }

    static {
        QUERIES.put("cat_claves_pau_modulo", "SELECT TABLE_NAME, COLUMN_NAME, POSITION, IS_PRIMARY_KEY, IS_UNIQUE_KEY FROM SYS.CONSTRAINTS WHERE SCHEMA_NAME = 'BSCL' AND SUBSTR_AFTER(TABLE_NAME, '::') = 'PAU.Modulo' ORDER BY CONSTRAINT_NAME, POSITION");
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
