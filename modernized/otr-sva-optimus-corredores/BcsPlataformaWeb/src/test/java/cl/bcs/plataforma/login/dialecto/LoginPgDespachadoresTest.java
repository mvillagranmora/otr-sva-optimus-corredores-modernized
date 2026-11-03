package cl.bcs.plataforma.login.dialecto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;

import cl.bcs.plataforma.service.ProcedureDispatcherService;
import cl.bcs.plataforma.service.QueryDispatcherService;

/**
 * T-30 / T-33 guard (architecture review H1): the two generic dispatchers name HANA objects ({@code CALL BSCL."..."}, views of the registry). In a
 * Postgres deployment they must answer 503 BEFORE anything reaches the connection, so HANA text is never sent to Postgres; in HANA mode they are untouched.
 */
class LoginPgDespachadoresTest {

    private static SqlDialecto postgres() {
        return new PostgresDialecto(CatalogoEquivalencias.desdeRecursos());
    }

    private static SqlDialecto hana() {
        return new HanaDialecto(CatalogoEquivalencias.desdeRecursos());
    }

    /** a connection / data source that records every call and would break the test if used */
    private static final class Espia {
        final List<String> llamadas = new ArrayList<>();

        Connection conexion() {
            return (Connection) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { Connection.class }, (p, m, a) -> {
                llamadas.add(m.getName());
                throw new AssertionError("the connection was used: " + m.getName());
            });
        }

        DataSource fuente() {
            return (DataSource) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { DataSource.class }, (p, m, a) -> {
                llamadas.add(m.getName());
                throw new AssertionError("the data source was used: " + m.getName());
            });
        }
    }

    @Test
    @DisplayName("postgres: a stored procedure call answers 503 and nothing reaches the connection (no CALL BSCL. is ever sent)")
    void procedimiento_en_postgres_no_envia_nada() {
        Espia espia = new Espia();
        ProcedureDispatcherService svc = new ProcedureDispatcherService(espia.fuente(), null, postgres());
        assertThatThrownBy(() -> svc.llamar(espia.conexion(), "BSCL.Plataforma.BD.Servicios::ACC.ConsultarUsuarioAdmin", new JsonObject()))
                .isInstanceOf(UnsupportedOperationException.class).hasMessageContaining("hana mode");
        assertThat(espia.llamadas).as("calls on the connection / data source").isEmpty();
    }

    @Test
    @DisplayName("postgres: dispatch() of a procedure also answers 503 without asking the data source for a connection")
    void dispatch_de_procedimiento_en_postgres() {
        Espia espia = new Espia();
        ProcedureDispatcherService svc = new ProcedureDispatcherService(espia.fuente(), null, postgres());
        assertThatThrownBy(() -> svc.dispatch("BSCL.Plataforma.BD.Servicios::ConsultarFormatos", new JsonObject(), 1L, 1L))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThat(espia.llamadas).isEmpty();
    }

    @Test
    @DisplayName("postgres: an OData query over a HANA view answers 503 without asking the data source for a connection")
    void consulta_odata_en_postgres() {
        Espia espia = new Espia();
        QueryDispatcherService svc = new QueryDispatcherService(espia.fuente(), postgres());
        assertThatThrownBy(() -> svc.dispatch("BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa", null, null, null, 0, 0, true))
                .isInstanceOf(UnsupportedOperationException.class).hasMessageContaining("hana mode");
        assertThat(espia.llamadas).isEmpty();
    }

    @Test
    @DisplayName("hana: the guard lets the call through (the connection is reached as before)")
    void en_hana_la_guardia_deja_pasar() {
        Espia espia = new Espia();
        QueryDispatcherService svc = new QueryDispatcherService(espia.fuente(), hana());
        assertThatThrownBy(() -> svc.dispatch("BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa", null, null, null, 0, 0, true))
                .isInstanceOf(AssertionError.class).hasMessageContaining("the data source was used");
        assertThat(espia.llamadas).isNotEmpty();
    }

    @Test
    @DisplayName("two statements never share the same HANA text (de(text) keys on it), and the pre-flight rejects a statement with no text in the dialect")
    void sentencias_unicas_y_preflight_real() {
        SentenciasSql pg = new SentenciasSql(postgres());
        assertThat(pg.ids()).hasSize(55);
        assertThatThrownBy(() -> pg.exigirDisponibles(List.of("BootstrapServicio.SQL_EMPRESAS"))).isInstanceOf(UnsupportedOperationException.class);
        pg.exigirDisponibles(List.of("CrearSesionServicio.QUERY_USUARIOS", "FiltroSesion.SQL_VALIDA_TOKEN"));
        new SentenciasSql(hana()).exigirDisponibles(pg.ids());
    }
}
