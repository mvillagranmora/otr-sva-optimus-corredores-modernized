package cl.bcs.filters;

import cl.bcs.filters.filter.ServiceAccessFilter;
import cl.bcs.filters.port.out.ServicioRepository;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ServiceAccessFilter} — no Spring Boot context needed.
 *
 * RULE-005: verifies that any exception thrown during access-control evaluation
 * results in a 401 response, not a pass-through (fail-closed behaviour).
 */
class ServiceAccessFilterUnitTest {

    private ServicioRepository servicioRepository;
    private ServiceAccessFilter filter;

    @BeforeEach
    void setUp() {
        servicioRepository = mock(ServicioRepository.class);
        filter = new ServiceAccessFilter(servicioRepository);
    }

    // ------------------------------------------------------------------
    // RULE-005 — fail-closed on exception
    // ------------------------------------------------------------------

    @Test
    @DisplayName("RULE-005: DB exception during getValidaAcceso → 401 (fail-closed), chain NOT called")
    void rule005_exception_in_access_check_returns_401() throws Exception {
        when(servicioRepository.getConfiguracion("PLA.TipoValidaServicio"))
                .thenReturn(Optional.of("TODOS"));
        when(servicioRepository.getCodigoUsuario(anyString()))
                .thenReturn("usuario_act");
        when(servicioRepository.getValidaAcceso("200"))
                .thenThrow(new RuntimeException("DB connection lost"));

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRequestURI("/api/status");
        req.setQueryString("t=200");
        req.addHeader("X-SESSION-TOKEN", "any-valid-token");

        MockHttpServletResponse resp = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(req, resp, chain);

        // RULE-005 FIX: must return 401, not 200
        assertThat(resp.getStatus()).isEqualTo(401);
        // Chain must NOT have been called (fail-closed)
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    @DisplayName("RULE-005: DB exception during getCodigoUsuario → 401 (fail-closed)")
    void rule005_exception_in_getCodigoUsuario_returns_401() throws Exception {
        when(servicioRepository.getConfiguracion("PLA.TipoValidaServicio"))
                .thenReturn(Optional.of("LISTA"));
        when(servicioRepository.getConfiguracion("PLA.ListaValidaServicio"))
                .thenReturn(Optional.of("usuario_act"));
        when(servicioRepository.getCodigoUsuario(anyString()))
                .thenThrow(new RuntimeException("Timeout"));

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRequestURI("/api/status");
        req.setQueryString("t=200");
        req.addHeader("X-SESSION-TOKEN", "some-token");

        MockHttpServletResponse resp = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(req, resp, chain);

        assertThat(resp.getStatus()).isEqualTo(401);
        verify(chain, never()).doFilter(any(), any());
    }

    // ------------------------------------------------------------------
    // RULE-004 — channel bypass logic
    // ------------------------------------------------------------------

    @Test
    @DisplayName("RULE-004: WS channel is whitelisted → chain called, no access check")
    void rule004_ws_channel_passes_without_check() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRequestURI("/api/status");
        req.setQueryString("t=200&ch=WS");
        req.addHeader("X-SESSION-TOKEN", "any-token");

        MockHttpServletResponse resp = new MockHttpServletResponse();
        FilterChain chain = new MockFilterChain();

        filter.doFilter(req, resp, chain);

        // Chain was called → no access check performed for whitelisted channel
        verifyNoInteractions(servicioRepository);
    }

    @Test
    @DisplayName("RULE-004: FOO channel is NOT whitelisted → access check performed")
    void rule004_unknown_channel_triggers_access_check() throws Exception {
        when(servicioRepository.getConfiguracion("PLA.TipoValidaServicio"))
                .thenReturn(Optional.of("TODOS"));
        when(servicioRepository.getCodigoUsuario(anyString()))
                .thenReturn("u1");
        when(servicioRepository.getValidaAcceso("200"))
                .thenReturn(Optional.of("S"));
        when(servicioRepository.countAccesoServicio(anyString(), eq("200")))
                .thenReturn(1);

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRequestURI("/api/status");
        req.setQueryString("t=200&ch=FOO");
        req.addHeader("X-SESSION-TOKEN", "some-token");

        MockHttpServletResponse resp = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(req, resp, chain);

        // Access check WAS performed (servicioRepository was consulted)
        verify(servicioRepository).getValidaAcceso("200");
    }
}
