package cl.bcs.plataforma.login.qa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import cl.bcs.plataforma.adapter.in.ConfiguracionController;
import cl.bcs.plataforma.service.ConfiguracionCacheService;

/**
 * QA only, READ ONLY: the former disabled test {@code intTest_configuracion_withHana} (the 11-schema UNION ALL of {@code /Configuraciones}) run against HANA
 * QA through the real controller and cache service. The connection is flagged read-only; the query is a SELECT. Skipped without the QA_HANA_* variables.
 */
class ConfiguracionesQaTest {

    @Test
    void configuraciones_con_hana_qa_solo_lectura() throws Exception {
        String url = System.getenv("QA_HANA_URL");
        String usuario = System.getenv("QA_HANA_USER");
        String clave = System.getenv("QA_HANA_PASSWORD");
        assumeTrue(url != null && usuario != null && clave != null, "HANA QA variables not set");

        try (Connection real = DriverManager.getConnection(url, usuario, clave)) {
            real.setReadOnly(true);
            DataSource ds = (DataSource) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { DataSource.class }, (p, m, a) -> {
                if (m.getName().equals("getConnection")) {
                    return Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { Connection.class }, (cp, cm, ca) -> {
                        if (cm.getName().equals("close")) return null;
                        try {
                            return cm.invoke(real, ca);
                        } catch (InvocationTargetException e) {
                            throw e.getCause();
                        }
                    });
                }
                throw new UnsupportedOperationException(m.getName());
            });
            MockMvc mvc = MockMvcBuilders.standaloneSetup(new ConfiguracionController(new ConfiguracionCacheService(new NamedParameterJdbcTemplate(ds)))).build();
            String cuerpo = mvc.perform(get("/Configuraciones").contentType("application/json")
                    .content("[{\"idEmpresa\":1,\"modulo\":\"PLATAFORMA\",\"codigoParametro\":\"PLA.Version\"},"
                            + "{\"idEmpresa\":1,\"modulo\":\"PLATAFORMA\",\"codigoParametro\":\"PLA.Ambiente\"}]"))
                    .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
            assertThat(cuerpo).contains("\"PLA.Version\"").contains("\"PLA.Ambiente\"").contains("\"QA\"");
        }
    }
}
