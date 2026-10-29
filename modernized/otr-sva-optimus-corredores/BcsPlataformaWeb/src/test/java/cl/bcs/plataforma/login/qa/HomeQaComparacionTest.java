package cl.bcs.plataforma.login.qa;

import static org.assertj.core.api.Assertions.assertThat;
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
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.google.gson.GsonBuilder;

import cl.bcs.plataforma.login.bootstrap.BootstrapServicio;
import cl.bcs.plataforma.login.sesion.SesionDatos;
import cl.bcs.plataforma.service.ProcedureDispatcherService;

/**
 * QA only, READ ONLY. Runs the home bootstrap of the modernized service for a real user of HANA QA and writes the JSON next to the legacy capture, to
 * compare them. Nothing is written to HANA: the JDBC connection is flagged read-only, every statement that is not a SELECT is swallowed by a dry-run
 * wrapper (and listed in the output), and only stored procedures whose definition was checked as read-only may be called. Skipped without
 * QA_HANA_URL / QA_HANA_USER / QA_HANA_PASSWORD; QA_BOOTSTRAP_IDUSUARIO is the user whose latest valid session is used (its token never leaves memory).
 */
class HomeQaComparacionTest {

    private static final List<String> SP_PERMITIDOS = List.of("ObtenerFechas", "ObtenerFechaHoraSistema", "ObtenerConfiguracion", "ConsultarFormatos",
            "ACC.ConsultarUsuarioAdmin");

    private final List<String> escrituraOmitidas = new ArrayList<>();

    @Test
    void home_de_un_usuario_real_de_qa_en_solo_lectura() throws Exception {
        String url = System.getenv("QA_HANA_URL");
        String usuario = System.getenv("QA_HANA_USER");
        String clave = System.getenv("QA_HANA_PASSWORD");
        String idUsuario = System.getenv("QA_BOOTSTRAP_IDUSUARIO");
        assumeTrue(url != null && usuario != null && clave != null && idUsuario != null, "HANA QA variables not set");
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        try (Connection real = DriverManager.getConnection(url, usuario, clave)) {
            real.setReadOnly(true);
            real.setAutoCommit(false);
            Connection con = soloLectura(real);
            String token = null;
            try (PreparedStatement ps = con.prepareStatement("SELECT \"IdTokenSesion\" FROM \"BSCL.Plataforma.BD::ACC.Sesion\" WHERE \"IdUsuario\" = ? "
                    + "AND \"TokenValido\" = 'S' ORDER BY \"FechaIngreso\" DESC LIMIT 1")) {
                ps.setLong(1, Long.parseLong(idUsuario));
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) token = rs.getString(1);
                }
            }
            assumeTrue(token != null, "the user has no valid session in QA");

            DataSource ds = (DataSource) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { DataSource.class }, (p, m, a) -> {
                if (m.getName().equals("getConnection")) return con;
                throw new UnsupportedOperationException(m.getName());
            });
            ProcedureDispatcherService sp = new ProcedureDispatcherService(ds, new NamedParameterJdbcTemplate(ds));
            // HANA refuses to call a procedure declared READ_ONLY=FALSE inside a read-only transaction, and ACC.ConsultarUsuarioAdmin is one (its body is
            // only SELECTs): its logic (read its definition from SYS.PROCEDURES) is run here as SELECTs, on the same connection.
            cl.bcs.plataforma.login.ProcedimientoAlmacenado soloSelect = (c, ruta, in) -> {
                if (!ruta.endsWith("ACC.ConsultarUsuarioAdmin")) return sp.llamar(c, ruta, in);
                com.google.gson.JsonObject cfg = new com.google.gson.JsonObject();
                cfg.addProperty("p_IdEmpresa", in.get("p_IdEmpresa").getAsLong());
                cfg.addProperty("p_CodigoParametro", "ACC.IdRolAdmin");
                String rolAdmin = sp.llamar(c, "BSCL.Plataforma.BD.Utilitarios::ObtenerConfiguracion", cfg).get("P_OUT_VALOR").getAsString();
                long cuantos;
                try (PreparedStatement q = c.prepareStatement("SELECT COUNT(\"IdRol\") FROM \"BSCL.Plataforma.BD.Vistas::ACC.ConsultarRolesUsuario\" "
                        + "WHERE \"EsAutorizado\" = 'S' AND \"IdEmpresa\" = ? AND \"IdUsuario\" = ? AND \"IdRol\" = TO_BIGINT(?)")) {
                    q.setLong(1, in.get("p_IdEmpresa").getAsLong());
                    q.setLong(2, in.get("p_IdUsuario").getAsLong());
                    q.setString(3, rolAdmin);
                    try (ResultSet rs = q.executeQuery()) {
                        rs.next();
                        cuantos = rs.getLong(1);
                    }
                }
                com.google.gson.JsonObject out = new com.google.gson.JsonObject();
                out.addProperty("P_OUT_ESADMIN", cuantos > 0 ? "S" : "N");
                return out;
            };
            BootstrapServicio.Home home = new BootstrapServicio(new SesionDatos(), soloSelect).home(con, token);
            real.rollback();

            Path dir = Paths.get(System.getProperty("qa.salida", "target/qa"));
            Files.createDirectories(dir);
            Files.writeString(dir.resolve("nuevo-home.json"), new GsonBuilder().setPrettyPrinting().serializeNulls().create().toJson(home.datos()),
                    StandardCharsets.UTF_8);
            Files.writeString(dir.resolve("nuevo-home.escrituras-omitidas.txt"), String.join("\n", escrituraOmitidas) + "\n", StandardCharsets.UTF_8);
            assertThat(home.valida()).as("session accepted: %s", home.datos().toString().length() > 300 ? "" : home.datos().toString()).isTrue();
        }
    }

    /** Connection wrapper: SELECT passes, a call passes only for the checked read-only procedures, everything else is a recorded no-op. */
    private Connection soloLectura(Connection real) {
        return (Connection) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { Connection.class }, (p, m, a) -> {
            String n = m.getName();
            if (n.equals("close") || n.equals("commit")) return null;
            if (n.equals("prepareStatement") && a[0] instanceof String sql) {
                String t = sql.strip().toUpperCase();
                if (t.startsWith("SELECT") || t.startsWith("WITH")) return invocar(m, real, a);
                escrituraOmitidas.add(sql.strip().replaceAll("\\s+", " ").substring(0, Math.min(120, sql.strip().length())));
                return statementSimulado();
            }
            if (n.equals("prepareCall") && a[0] instanceof String sql) {
                if (SP_PERMITIDOS.stream().noneMatch(sql::contains)) throw new IllegalStateException("procedure not allowed in the read-only run: " + sql);
                return invocar(m, real, a);
            }
            return invocar(m, real, a);
        });
    }

    private static PreparedStatement statementSimulado() {
        return (PreparedStatement) Proxy.newProxyInstance(HomeQaComparacionTest.class.getClassLoader(), new Class<?>[] { PreparedStatement.class }, (p, m, a) -> {
            String n = m.getName();
            if (n.equals("executeUpdate") || n.equals("getUpdateCount")) return 1;
            if (n.equals("executeLargeUpdate")) return 1L;
            if (n.equals("executeBatch")) return new int[0];
            if (n.equals("execute")) return false;
            if (n.startsWith("set") || n.equals("close") || n.equals("addBatch") || n.equals("clearParameters")) return null;
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
