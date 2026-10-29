package cl.bcs.apv.infrastructure.web;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/** Architecture review H3 (allow-list): /apv/** requires the shared service-to-service token and fails closed without one. */
class TokenInternoFilterTest {

    @Test
    void correctTokenPassesToTheEndpoint() throws Exception {
        var f = new TokenInternoFilter("fake-shared-secret");
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/apv/contratos");
        req.setRequestURI("/apv/contratos");
        req.addHeader(TokenInternoFilter.CABECERA, "fake-shared-secret");
        MockFilterChain chain = new MockFilterChain();
        f.doFilter(req, new MockHttpServletResponse(), chain);
        assertThat(chain.getRequest()).isNotNull();
    }

    @Test
    void missingOrWrongTokenIs401() throws Exception {
        var f = new TokenInternoFilter("fake-shared-secret");
        for (String t : new String[] {null, "", "wrong", "fake-shared-secret "}) {
            MockHttpServletRequest req = new MockHttpServletRequest("POST", "/apv/contratos");
            req.setRequestURI("/apv/contratos");
            if (t != null) {
                req.addHeader(TokenInternoFilter.CABECERA, t);
            }
            MockHttpServletResponse res = new MockHttpServletResponse();
            MockFilterChain chain = new MockFilterChain();
            f.doFilter(req, res, chain);
            assertThat(res.getStatus()).as("token=" + t).isEqualTo(401);
            assertThat(chain.getRequest()).as("token=" + t).isNull();
        }
    }

    @Test
    void failsClosedWhenNoSecretIsConfigured() throws Exception {
        for (String cfg : new String[] {"", null}) {
            var f = new TokenInternoFilter(cfg);
            MockHttpServletRequest req = new MockHttpServletRequest("POST", "/apv/contratos");
            req.setRequestURI("/apv/contratos");
            req.addHeader(TokenInternoFilter.CABECERA, "");
            MockHttpServletResponse res = new MockHttpServletResponse();
            MockFilterChain chain = new MockFilterChain();
            f.doFilter(req, res, chain);
            assertThat(res.getStatus()).isEqualTo(401);
            assertThat(chain.getRequest()).isNull();
        }
    }

    @Test
    void pathVariantsOfInternalStillNeedTheToken() throws Exception {
        var f = new TokenInternoFilter("fake-shared-secret");
        for (String uri : new String[] {"/apv/contratos;x=1", "//apv/contratos", "/apv/;a=b/contratos", "/./apv/contratos", "/apv", "/other", "/x;a=b/", "//x", "/actuator/healthz", "/actuator/env"}) {
            MockHttpServletRequest req = new MockHttpServletRequest("POST", uri);
            req.setRequestURI(uri);
            MockHttpServletResponse res = new MockHttpServletResponse();
            MockFilterChain chain = new MockFilterChain();
            f.doFilter(req, res, chain);
            assertThat(res.getStatus()).as(uri).isEqualTo(401);
            assertThat(chain.getRequest()).as(uri).isNull();
        }
    }

    @Test
    void onlyTheHealthProbesAreNotFiltered() throws Exception {
        var f = new TokenInternoFilter("fake-shared-secret");
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/actuator/health");
        req.setRequestURI("/actuator/health");
        MockFilterChain chain = new MockFilterChain();
        f.doFilter(req, new MockHttpServletResponse(), chain);
        assertThat(chain.getRequest()).isNotNull();
    }
}
