package cl.bcs.plataforma.login.qa;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import cl.bcs.plataforma.login.clave.GestionClaveServicio;
import cl.bcs.plataforma.service.ProcedureDispatcherService;

/**
 * QA only, READ ONLY dry run of {@code RecuperacionClave} for a real user of HANA QA. The recovery sends an e-mail and stores a provisional password, so
 * nothing of that is executed: the connection is flagged read-only, the UPDATE is swallowed (and listed), the events procedure is NOT called (its input is
 * recorded, with the provisional password masked) and only the read-only date procedure runs. What this proves is the reading part (user, person, address,
 * company, token length, logo) against the real tables and the exact input the events procedure would receive. Skipped without the QA_HANA_* variables;
 * QA_RECUPERACION_USUARIO is the user code.
 */
class RecuperacionQaTest {

    private final List<String> escrituraOmitidas = new ArrayList<>();

    @Test
    void recuperacion_en_seco_solo_lectura() throws Exception {
        String url = System.getenv("QA_HANA_URL");
        String usuario = System.getenv("QA_HANA_USER");
        String clave = System.getenv("QA_HANA_PASSWORD");
        String codigo = System.getenv("QA_RECUPERACION_USUARIO");
        assumeTrue(url != null && usuario != null && clave != null && codigo != null, "HANA QA variables not set");
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        try (Connection real = DriverManager.getConnection(url, usuario, clave)) {
            real.setReadOnly(true);
            real.setAutoCommit(false);
            Connection con = soloLectura(real);
            DataSource ds = (DataSource) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { DataSource.class }, (p, m, a) -> {
                if (m.getName().equals("getConnection")) return con;
                throw new UnsupportedOperationException(m.getName());
            });
            ProcedureDispatcherService sp = new ProcedureDispatcherService(ds, new NamedParameterJdbcTemplate(ds));
            JsonObject[] eventoRecibido = new JsonObject[1];
            GestionClaveServicio servicio = new GestionClaveServicio((c, ruta, in) -> {
                if (ruta.endsWith("EVT.AgregarEvento")) {
                    eventoRecibido[0] = in.deepCopy();
                    return new JsonObject();
                }
                return sp.llamar(c, ruta, in);
            });
            JsonObject cuerpo = new JsonObject();
            cuerpo.addProperty("p_CodigoUsuario", codigo);
            cuerpo.addProperty("p_CanalSistema", "OPTIMUS");
            GestionClaveServicio.Respuesta r = servicio.recuperacionClave(con, cuerpo.toString());
            real.rollback();

            StringBuilder sb = new StringBuilder();
            sb.append("estado ").append(r.estado()).append('\n').append("cuerpo ").append(r.cuerpo()).append('\n');
            sb.append("escrituras omitidas: ").append(escrituraOmitidas.size()).append('\n');
            for (String e : escrituraOmitidas) sb.append("  ").append(e).append('\n');
            if (eventoRecibido[0] != null) {
                String json = new GsonBuilder().setPrettyPrinting().serializeNulls().create().toJson(eventoRecibido[0]);
                // the provisional password is a secret: mask its value
                json = json.replaceAll("(\"Llave\": \"ClaveProvisoria\",\\s*\"Valor\": \")[^\"]*", "$1***");
                sb.append("entrada de EVT.AgregarEvento:\n").append(json).append('\n');
            } else {
                sb.append("EVT.AgregarEvento no fue invocado\n");
            }
            Path dir = Paths.get(System.getProperty("qa.salida", "target/qa"));
            Files.createDirectories(dir);
            Files.writeString(dir.resolve("nuevo-recuperacion.txt"), sb.toString(), StandardCharsets.UTF_8);
        }
    }

    private Connection soloLectura(Connection real) {
        return (Connection) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { Connection.class }, (p, m, a) -> {
            String n = m.getName();
            if (n.equals("close") || n.equals("commit")) return null;
            if (n.equals("prepareStatement") && a[0] instanceof String sql) {
                String t = sql.strip().toUpperCase();
                if (t.startsWith("SELECT") || t.startsWith("WITH")) return invocar(m, real, a);
                escrituraOmitidas.add(sql.strip().replaceAll("\\s+", " ").substring(0, Math.min(110, sql.strip().length())));
                return statementSimulado();
            }
            if (n.equals("prepareCall") && a[0] instanceof String sql) {
                if (!sql.contains("ObtenerFechaHoraSistema")) throw new IllegalStateException("procedure not allowed in the dry run: " + sql);
                return invocar(m, real, a);
            }
            return invocar(m, real, a);
        });
    }

    private static PreparedStatement statementSimulado() {
        return (PreparedStatement) Proxy.newProxyInstance(RecuperacionQaTest.class.getClassLoader(), new Class<?>[] { PreparedStatement.class }, (p, m, a) -> {
            String n = m.getName();
            if (n.equals("executeUpdate") || n.equals("getUpdateCount")) return 1;
            if (n.equals("execute")) return false;
            if (n.startsWith("set") || n.equals("close") || n.equals("clearParameters")) return null;
            if (n.equals("isClosed")) return false;
            throw new UnsupportedOperationException("simulated statement: " + n);
        });
    }

    private static Object invocar(java.lang.reflect.Method m, Object o, Object[] a) throws Throwable {
        try {
            return m.invoke(o, a);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }
}
