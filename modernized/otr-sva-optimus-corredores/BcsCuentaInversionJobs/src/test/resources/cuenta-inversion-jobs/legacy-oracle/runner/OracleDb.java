import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import cl.bcs.cuentainversion.jobs.support.H2Hana;

/**
 * The H2 database of one case and the connection the legacy BcsJob runs on: autocommit off (BcsJob.run), every SQL text translated
 * by H2Hana, every commit / rollback recorded in order, the HANA stored procedures answered by {@link ScriptedProc}. The job
 * connection and the dump connection are different connections: a dump sees only what the job COMMITTED.
 */
final class OracleDb {

    private static int contador = 0;

    final Connection setup;
    final Connection conexionJob;
    final List<String> eventos = new ArrayList<>();

    OracleDb(Path recursos, String semilla) throws Exception {
        String url = "jdbc:h2:mem:oraclejobs" + (++contador) + ";MODE=LEGACY;DB_CLOSE_DELAY=-1";
        setup = DriverManager.getConnection(url, "sa", "");
        script(setup, Files.readString(recursos.resolve("schema-jobs.sql"), StandardCharsets.UTF_8));
        script(setup, Files.readString(recursos.resolve("seed/base.sql"), StandardCharsets.UTF_8));
        if (!semilla.isBlank()) script(setup, semilla);
        Connection raw = DriverManager.getConnection(url, "sa", "");
        raw.setAutoCommit(false);
        Connection traducida = H2Hana.wrap(raw);
        Connection consultas = DriverManager.getConnection(url, "sa", "");
        conexionJob = (Connection) Proxy.newProxyInstance(OracleDb.class.getClassLoader(), new Class<?>[]{Connection.class}, (p, m, a) -> {
            try {
                String n = m.getName();
                if (n.equals("commit") || n.equals("rollback")) eventos.add(n);
                if (n.equals("prepareCall")) return ScriptedProc.crear((String) a[0], consultas);
                return m.invoke(traducida, a);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        });
    }

    static void script(Connection c, String sql) throws Exception {
        StringBuilder clean = new StringBuilder();
        for (String line : sql.split("\n")) {
            if (!line.strip().startsWith("--")) clean.append(line).append('\n');
        }
        try (Statement st = c.createStatement()) {
            for (String stmt : clean.toString().split(";\\s*\n")) {
                if (!stmt.isBlank()) st.execute(stmt);
            }
        }
    }
}
