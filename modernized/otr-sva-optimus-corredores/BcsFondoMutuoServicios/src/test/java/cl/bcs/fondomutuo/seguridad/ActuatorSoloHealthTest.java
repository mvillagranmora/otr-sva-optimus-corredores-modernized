package cl.bcs.fondomutuo.seguridad;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;

/** SEC-007 / SEC-004: the shipped application.yml exposes only health (no details) and sets the Tomcat limits. */
class ActuatorSoloHealthTest {

    @Test
    void applicationYml_soloHealth_sinDetalles_yConLimitesTomcat() {
        YamlPropertiesFactoryBean y = new YamlPropertiesFactoryBean();
        y.setResources(new ClassPathResource("application.yml"));
        Properties p = y.getObject();
        assertThat(p.getProperty("management.endpoints.web.exposure.include")).isEqualTo("health");
        assertThat(p.getProperty("management.endpoint.health.show-details")).isEqualTo("never");
        assertThat(p.getProperty("server.tomcat.max-swallow-size")).isNotBlank();
        assertThat(p.getProperty("server.tomcat.max-http-form-post-size")).isNotBlank();
        assertThat(p.getProperty("server.tomcat.connection-timeout")).isNotBlank();
        assertThat(p.getProperty("server.tomcat.keep-alive-timeout")).isNotBlank();
    }
}
