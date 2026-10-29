package cl.bcs.plataforma.internal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Full Spring context WITH the token filter in the chain: the existing routes need no token, /internal needs it. */
@SpringBootTest(properties = "services.internal-token=secreto-ctx")
@ActiveProfiles("test")
class RutasExistentesNoCambianTest {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private InternalTokenFilter filtro;

    private MockMvc mvc;

    @BeforeEach
    void montar() {
        mvc = MockMvcBuilders.webAppContextSetup(wac).addFilters(filtro).build();
    }

    @Test
    void call_sinToken_seguiaDando503_noPide401() throws Exception {
        mvc.perform(post("/call").contentType("application/json").content("{\"t\":1}")).andExpect(status().isServiceUnavailable());
        mvc.perform(get("/call").param("t", "1")).andExpect(status().isServiceUnavailable());
    }

    @Test
    void otrasRutas_sinToken() throws Exception {
        mvc.perform(get("/Ping")).andExpect(status().isOk());
        mvc.perform(post("/bootstrap/home")).andExpect(status().isUnauthorized()); // the JSON bootstrap answers 401 without a token (the session filter, under /PLA, answers 400)
    }

    @Test
    void internal_sinToken_401_yConTokenLlegaAlControlador() throws Exception {
        mvc.perform(post("/internal/java-call").contentType("application/json").content("{}")).andExpect(status().isUnauthorized());
        mvc.perform(post("/internal//java-call").contentType("application/json").content("{}")).andExpect(status().isUnauthorized());
        mvc.perform(post("/internal/java-call").header("X-Internal-Token", "secreto-ctx").contentType("application/json").content("{}"))
                .andExpect(status().isBadRequest()); // authenticated, then rejected for the missing 'class'
    }
}
