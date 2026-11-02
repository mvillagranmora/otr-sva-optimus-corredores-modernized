import java.sql.*;
import java.util.Locale;
import java.util.Properties;

/**
 * Read-only HANA JDBC check, run with the java source launcher (java -cp lib/ngdbc.jar HanaSmoke.java [SELECT ...]).
 * Reads qa01.saphana.* from the environment, opens the connection read-only and runs one SELECT.
 * Never prints the user, the password or the URL host.
 */
public class HanaSmoke {
    public static void main(String[] args) throws Exception {
        String url = need("qa01.saphana.dbURL");
        String drv = System.getenv().getOrDefault("qa01.saphana.dbdrv", "");
        if (drv.isBlank()) drv = "com.sap.db.jdbc.Driver";
        Properties p = new Properties();
        p.setProperty("user", need("qa01.saphana.dbuser"));
        p.setProperty("password", need("qa01.saphana.dbpass"));
        String extra = System.getenv().getOrDefault("qa01.saphana.dbprop", "");
        for (String kv : extra.split("[;&]")) {
            int i = kv.indexOf('=');
            if (i > 0) p.setProperty(kv.substring(0, i).trim(), kv.substring(i + 1).trim());
        }
        String sql = args.length > 0 ? String.join(" ", args) : "SELECT CURRENT_USER AS U, CURRENT_SCHEMA AS S FROM DUMMY";
        if (!sql.stripLeading().toUpperCase(Locale.ROOT).startsWith("SELECT")) {
            System.err.println("Only SELECT statements are allowed.");
            System.exit(2);
        }
        Class.forName(drv);
        try (Connection c = DriverManager.getConnection(url, p)) {
            c.setReadOnly(true);
            System.out.println("connected: driver " + c.getMetaData().getDriverVersion()
                    + ", database " + c.getMetaData().getDatabaseProductVersion());
            try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery(sql)) {
                ResultSetMetaData m = rs.getMetaData();
                int rows = 0;
                while (rs.next() && rows++ < 50) {
                    StringBuilder b = new StringBuilder();
                    for (int i = 1; i <= m.getColumnCount(); i++) b.append(i > 1 ? " | " : "").append(rs.getString(i));
                    System.out.println(b);
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL error " + e.getErrorCode() + " (state " + e.getSQLState() + ")");
            System.exit(1);
        }
    }

    private static String need(String key) {
        String v = System.getenv(key);
        if (v == null || v.isBlank()) {
            System.err.println("Missing " + key + " (fill it in .env)");
            System.exit(2);
        }
        return v;
    }
}
