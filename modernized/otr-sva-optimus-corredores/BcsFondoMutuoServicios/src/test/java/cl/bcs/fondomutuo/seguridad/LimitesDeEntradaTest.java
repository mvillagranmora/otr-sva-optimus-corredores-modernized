package cl.bcs.fondomutuo.seguridad;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import cl.bcs.fondomutuo.adapter.in.javacall.JavaCallLimitesFilter;

/** SEC-004: oversized body (413), deep nesting and huge lists (400) are rejected BEFORE the chain (unit of work, database); normal requests pass unchanged. */
class LimitesDeEntradaTest {

    final JavaCallLimitesFilter f = new JavaCallLimitesFilter(1000, 10, 50);
    int llamadasAlControlador;
    String cuerpoVisto;

    private MockHttpServletResponse enviar(String uri, String cuerpo, boolean sinContentLength) throws Exception {
        byte[] bytes = cuerpo.getBytes(StandardCharsets.UTF_8);
        MockHttpServletRequest rq = new MockHttpServletRequest("POST", uri) {
            @Override
            public long getContentLengthLong() {
                return sinContentLength ? -1 : super.getContentLengthLong();
            }
        };
        rq.setRequestURI(uri);
        rq.setContent(bytes);
        rq.setCharacterEncoding("UTF-8");
        MockHttpServletResponse rs = new MockHttpServletResponse();
        llamadasAlControlador = 0;
        f.doFilter(rq, rs, (a, b) -> {
            llamadasAlControlador++;
            cuerpoVisto = new String(a.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        });
        return rs;
    }

    private static String lista(int n) {
        StringBuilder sb = new StringBuilder("{\"class\":\"X\",\"params\":{\"p_Lista\":[");
        for (int i = 0; i < n; i++) sb.append(i == 0 ? "" : ",").append("{\"a\":").append(i).append("}");
        return sb.append("]}}").toString();
    }

    @Test
    void cuerpoGrande_413_porContentLength_sinLlegarAlControlador() throws Exception {
        MockHttpServletResponse rs = enviar("/internal/java-call", "{\"class\":\"" + "x".repeat(2000) + "\"}", false);
        assertThat(rs.getStatus()).isEqualTo(413);
        assertThat(rs.getContentAsString()).isEqualTo("{\"error\":\"request body too large\"}");
        assertThat(llamadasAlControlador).isZero();
    }

    @Test
    void cuerpoGrande_413_sinContentLength_chunked() throws Exception {
        MockHttpServletResponse rs = enviar("/internal/java-call", "{\"class\":\"" + "x".repeat(2000) + "\"}", true);
        assertThat(rs.getStatus()).isEqualTo(413);
        assertThat(llamadasAlControlador).isZero();
    }

    @Test
    void anidamientoProfundo_400() throws Exception {
        MockHttpServletResponse rs = enviar("/internal/java-call", "{\"params\":" + "[".repeat(11) + "]".repeat(11) + "}", false);
        assertThat(rs.getStatus()).isEqualTo(400);
        assertThat(rs.getContentAsString()).isEqualTo("{\"error\":\"request structure exceeds the allowed limits\"}");
        assertThat(llamadasAlControlador).isZero();
    }

    @Test
    void listaEnorme_400_yEnElLimite_pasa() throws Exception {
        MockHttpServletResponse rs = enviar("/internal/java-call", lista(51), true);
        assertThat(rs.getStatus()).isEqualTo(400);
        assertThat(llamadasAlControlador).isZero();
        enviar("/internal/java-call", lista(50), false);
        assertThat(llamadasAlControlador).isEqualTo(1);
    }

    @Test
    void listaDeEscalares_cuentaIgual() throws Exception {
        String json = "{\"params\":{\"l\":[" + "1,".repeat(60) + "1]}}";
        assertThat(json.length()).isLessThan(1000);
        assertThat(enviar("/internal/java-call", json, false).getStatus()).isEqualTo(400);
    }

    @Test
    void casoNormal_pasaYElControladorVeElMismoCuerpo() throws Exception {
        String normal = "{\"class\":\"cl.bcs.fondomutuo.ManejadorSolicitudAporteRescate\",\"params\":{\"p_IdEmpresa\":1,\"l\":[{\"a\":[1,2]},{\"a\":[3]}],\"t\":\"ñ\"}}";
        MockHttpServletResponse rs = enviar("/internal/java-call", normal, false);
        assertThat(rs.getStatus()).isEqualTo(200);
        assertThat(llamadasAlControlador).isEqualTo(1);
        assertThat(cuerpoVisto).isEqualTo(normal);
    }

    @Test
    void jsonInvalido_noSeJuzgaAqui_loResponderaElControlador() throws Exception {
        enviar("/internal/java-call", "{no es json", false);
        assertThat(llamadasAlControlador).isEqualTo(1);
    }

    @Test
    void rutasFueraDeInternal_noSeFiltran() throws Exception {
        enviar("/actuator/health", "x".repeat(5000), true);
        assertThat(llamadasAlControlador).isEqualTo(1);
    }

    @Test
    void valoresPorDefecto() {
        assertThat(JavaCallLimitesFilter.MAX_BODY_POR_DEFECTO).isEqualTo(2 * 1024 * 1024);
        assertThat(JavaCallLimitesFilter.MAX_PROFUNDIDAD_POR_DEFECTO).isEqualTo(64);
        assertThat(JavaCallLimitesFilter.MAX_LISTA_POR_DEFECTO).isEqualTo(5000);
    }
}
