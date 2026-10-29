package cl.bcs.fondomutuo.normal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import cl.bcs.fondomutuo.adapter.out.jdbc.ConexionUnidadDeTrabajo;
import cl.bcs.fondomutuo.kit.AccesoDatosException;
import cl.bcs.fondomutuo.normal.BloqueoSolicitudPort.Bloqueo;

/**
 * H1 (architecture review): when the INSERT or the CALL of the lock fails AFTER {@code CREATE LOCAL TEMPORARY TABLE}, the session-local table must not stay on
 * the pooled connection (every later lock on that connection would fail with "duplicate table name"). ONE connection for every request (a pool of size 1):
 * the second request must work. H2 only; the AUTONOMOUS procedures run on a second H2 connection (SesionHana).
 */
class BloqueoSolicitudFalloTest {

    private Connection raw;
    private SesionHana sesion;
    private volatile boolean fallaElCall;
    private BloqueoSolicitudJdbc bloqueo;

    @BeforeEach
    void preparar() throws Exception {
        String url = "jdbc:h2:mem:blq" + UUID.randomUUID().toString().replace("-", "") + ";MODE=LEGACY;DB_CLOSE_DELAY=-1";
        raw = DriverManager.getConnection(url, "sa", "");
        for (String f : new String[] { "schema-types.sql", "schema-fmm.sql", "schema-keys.sql", "schema-session.sql" }) {
            CasoNormal.script(raw, Files.readString(CasoNormal.recursos().getParent().resolve("fondomutuo-3a").resolve(f), StandardCharsets.UTF_8));
        }
        raw.commit();
        raw.setAutoCommit(false);
        sesion = new SesionHana(raw, url, CasoNormal.RELOJ);
        Connection con = sesion.conexion();
        Connection conFallo = (Connection) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { Connection.class }, (p, m, a) -> {
            if (fallaElCall && m.getName().equals("prepareCall") && String.valueOf(a[0]).contains("CrearBloqueo")) {
                throw new SQLException("forced failure of the CALL");
            }
            try {
                return m.invoke(con, a);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        });
        bloqueo = new BloqueoSolicitudJdbc(new ConexionUnidadDeTrabajo(new SingleConnectionDataSource(conFallo, true)));
    }

    @AfterEach
    void cerrar() throws Exception {
        cl.bcs.fondomutuo.BdEfimeraH2.cerrar(raw);
    }

    private static Bloqueo fila(String id) {
        Bloqueo b = new Bloqueo();
        b.IdBloqueoSolicitudAporteRescate = id;
        b.IdPortafolio = 1L;
        b.IdSerieFondo = "S1";
        b.IdRegimenTributario = "";
        b.TipoSolicitud = "A";
        b.Monto = new BigDecimal("1000");
        b.NumeroCuotas = new BigDecimal("0");
        b.FechaSistema = "2026-03-02";
        return b;
    }

    private void segundaPeticionFunciona() {
        bloqueo.crear(List.of(fila("segunda")));
        assertThat(sesion.tablasLocalesSinDrop()).containsExactly("#p_BloqueoSolicitudAporteRescate");
        assertThat(sesion.commitsAutonomos()).anyMatch(s -> s.startsWith("INSERT 1"));
        bloqueo.llamarEliminar(List.of(fila("segunda")));
        bloqueo.soltarTabla();
        assertThat(sesion.tablasLocalesSinDrop()).isEmpty();
    }

    @Test
    void fallaElCall_laTablaLocalSeSuelta_yLaSegundaPeticionEnLaMismaConexionFunciona() {
        fallaElCall = true;
        assertThatThrownBy(() -> bloqueo.crear(List.of(fila("primera")))).isInstanceOf(AccesoDatosException.class).hasMessageContaining("SQL failure").hasRootCauseMessage("forced failure of the CALL");   // M3 (F3 W1 review): the message no longer carries the text of the database exception
        assertThat(sesion.tablasLocalesSinDrop()).as("no session-local table left on the connection").isEmpty();
        fallaElCall = false;
        segundaPeticionFunciona();
    }

    @Test
    void fallaElInsert_laTablaLocalSeSuelta_yLaSegundaPeticionEnLaMismaConexionFunciona() {
        String demasiadoLargo = "x".repeat(200);   // the column is NVARCHAR(120): the INSERT of the batch fails
        assertThatThrownBy(() -> bloqueo.crear(List.of(fila(demasiadoLargo)))).isInstanceOf(AccesoDatosException.class);
        assertThat(sesion.tablasLocalesSinDrop()).isEmpty();
        segundaPeticionFunciona();
    }

    @Test
    void sinEl_fix_elSegundoCreateFallaPorTablaDuplicada_esElDefectoQueSeCorrigio() throws Exception {
        // documents the symptom the fix prevents: a leaked table makes the next CREATE fail on the same session
        Connection con = sesion.conexion();
        try (var st = con.createStatement()) {
            st.execute("CREATE LOCAL TEMPORARY TABLE #p_BloqueoSolicitudAporteRescate (X INT)");
            assertThatThrownBy(() -> st.execute("CREATE LOCAL TEMPORARY TABLE #p_BloqueoSolicitudAporteRescate (X INT)")).hasMessageContaining("duplicate table name");
        }
    }
}
