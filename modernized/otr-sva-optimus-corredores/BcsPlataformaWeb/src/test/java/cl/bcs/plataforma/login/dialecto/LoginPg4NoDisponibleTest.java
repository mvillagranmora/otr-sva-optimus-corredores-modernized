package cl.bcs.plataforma.login.dialecto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import cl.bcs.plataforma.config.GlobalExceptionHandler;
import cl.bcs.plataforma.config.NoDisponibleException;
import cl.bcs.plataforma.login.ProcedimientoAlmacenado;
import cl.bcs.plataforma.login.clave.GestionClaveServicio;

/**
 * Architecture review H1 and H2: only {@link NoDisponibleException} means "not available" (503). Any other {@code UnsupportedOperationException} (Gson on a JSON
 * null, {@code List.of}) is a real error and must not look like a missing feature; and the password flows must not swallow the 503 into a 401.
 */
class LoginPg4NoDisponibleTest {

    @org.springframework.web.bind.annotation.RestController
    static class Lanza {
        @org.springframework.web.bind.annotation.GetMapping("/disponible")
        String a() {
            throw new NoDisponibleException("not here");
        }

        @org.springframework.web.bind.annotation.GetMapping("/gson")
        String b() {
            return com.google.gson.JsonNull.INSTANCE.getAsString();
        }
    }

    private static MockMvc mvc() {
        return MockMvcBuilders.standaloneSetup(new Lanza()).setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    @DisplayName("H1 NoDisponibleException is a 503 and is an UnsupportedOperationException")
    void no_disponible_es_503() throws Exception {
        assertThat(new NoDisponibleException("x")).isInstanceOf(UnsupportedOperationException.class);
        mvc().perform(get("/disponible")).andExpect(status().isServiceUnavailable());
    }

    @Test
    @DisplayName("H1 the UnsupportedOperationException that Gson throws on a JSON null is NOT a 503")
    void el_de_gson_no_es_503() throws Exception {
        int estado = mvc().perform(get("/gson")).andReturn().getResponse().getStatus();
        assertThat(estado).isNotEqualTo(503).isGreaterThanOrEqualTo(500);
    }

    @Test
    @DisplayName("H2 the password flows relaunch NoDisponibleException instead of answering an error code")
    void los_flujos_de_clave_relanzan_el_503() {
        ProcedimientoAlmacenado noDisponible = (con, ruta, in) -> {
            throw new NoDisponibleException("no procedure");
        };
        GestionClaveServicio svc = new GestionClaveServicio(noDisponible);
        String body = "{\"p_IdEmpresa\":\"1\",\"p_Username\":\"jperez\",\"p_Password\":\"x\",\"p_Token\":\"t\"}";
        assertThatThrownBy(() -> svc.solicitudRecuperacionClave(null, body)).isInstanceOf(NoDisponibleException.class);
        assertThatThrownBy(() -> svc.confirmarCambioClave(null, body)).isInstanceOf(NoDisponibleException.class);
    }

    @Test
    @DisplayName("H2 any other failure of the procedure still answers the error code (legacy behavior unchanged)")
    void otro_fallo_sigue_siendo_codigo_de_error() {
        ProcedimientoAlmacenado falla = (con, ruta, in) -> {
            throw new IllegalStateException("boom");
        };
        GestionClaveServicio svc = new GestionClaveServicio(falla);
        String body = "{\"p_IdEmpresa\":\"1\",\"p_Username\":\"jperez\",\"p_Password\":\"x\",\"p_Token\":\"t\"}";
        assertThat(svc.solicitudRecuperacionClave(null, body).estado()).isNotEqualTo(503);
        assertThat(svc.confirmarCambioClave(null, body).estado()).isNotEqualTo(503);
    }

    @Test
    @DisplayName("H2 after any other failure the three password flows roll the connection back before answering the error")
    void otro_fallo_deshace_la_transaccion() {
        ProcedimientoAlmacenado falla = (con, ruta, in) -> {
            throw new IllegalStateException("boom");
        };
        GestionClaveServicio svc = new GestionClaveServicio(falla);
        String body = "{\"p_IdEmpresa\":\"1\",\"p_Username\":\"jperez\",\"p_Password\":\"x\",\"p_Token\":\"t\"}";
        java.util.List<String> llamadas = new java.util.ArrayList<>();
        java.sql.Connection con = (java.sql.Connection) java.lang.reflect.Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[] { java.sql.Connection.class }, (p, m, a) -> {
                    llamadas.add(m.getName());
                    return null;
                });
        svc.solicitudRecuperacionClave(con, body);
        svc.confirmarCambioClave(con, body);
        assertThat(llamadas).containsExactly("rollback", "rollback");
    }

    @Test
    @DisplayName("H2 the 503 is not rolled back by the flow (the unit of work does it when the exception leaves)")
    void el_503_no_toca_la_conexion() {
        ProcedimientoAlmacenado noDisponible = (con, ruta, in) -> {
            throw new NoDisponibleException("no procedure");
        };
        java.util.List<String> llamadas = new java.util.ArrayList<>();
        java.sql.Connection con = (java.sql.Connection) java.lang.reflect.Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[] { java.sql.Connection.class }, (p, m, a) -> {
                    llamadas.add(m.getName());
                    return null;
                });
        assertThatThrownBy(() -> new GestionClaveServicio(noDisponible).solicitudRecuperacionClave(con, "{\"p_IdEmpresa\":\"1\",\"p_Username\":\"j\"}"))
                .isInstanceOf(NoDisponibleException.class);
        assertThat(llamadas).isEmpty();
    }
}
