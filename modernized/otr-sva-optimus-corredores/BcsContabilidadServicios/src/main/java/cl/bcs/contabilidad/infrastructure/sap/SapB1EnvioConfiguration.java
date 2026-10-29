package cl.bcs.contabilidad.infrastructure.sap;

import cl.bcs.contabilidad.domain.port.out.PlanCuentasSapPort;
import cl.bcs.contabilidad.domain.port.out.SapGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Wiring of the SAP B1 side of "envío a SAP" (WP-B).
 *
 * <p>Service Layer: {@code sap.b1.url / user / password / company} (env {@code SAP_B1_URL}, {@code SAP_B1_USER},
 * {@code SAP_B1_PASSWORD}, {@code SAP_B1_COMPANY}), plus {@code sap.b1.maximo-intentos} and
 * {@code sap.b1.espera-reintento-login}. Chart of accounts: the SAP B1 database from {@code SAP_B1_DB_URL / USER /
 * PASSWORD} ({@link SapB1DatabaseSettings}, pooled by {@link SapB1Jdbc}) and the same company schema. Every value is read lazily, so the
 * application starts without them; the SAP B1 datasource is deliberately not a bean, so Spring Boot keeps
 * auto-configuring the HANA one.
 */
@Configuration(proxyBeanMethods = false)
public class SapB1EnvioConfiguration {

    @Bean
    SapB1Config sapB1Config(@Value("${sap.b1.url:${SAP_B1_URL:}}") String url,
                            @Value("${sap.b1.user:${SAP_B1_USER:}}") String usuario,
                            @Value("${sap.b1.password:${SAP_B1_PASSWORD:}}") String clave,
                            @Value("${sap.b1.company:${SAP_B1_COMPANY:}}") String compania,
                            @Value("${sap.b1.maximo-intentos:" + SapB1Config.MAXIMO_INTENTOS_DEFAULT + "}") int maximoIntentos,
                            @Value("${sap.b1.espera-reintento-login:PT2S}") Duration espera) {
        return new SapB1Config(url, usuario, clave, compania, maximoIntentos, espera);
    }

    @Bean
    SapGateway sapGateway(SapB1Config config) {
        return new SapB1Gateway(config);
    }

    /** The one SAP B1 database pool of the module (lazy; not a DataSource bean). */
    @Bean
    SapB1Jdbc sapB1Jdbc() {
        return new SapB1Jdbc(System::getenv);
    }

    @Bean
    PlanCuentasSapPort planCuentasSap(SapB1Config config, SapB1Jdbc sapB1Jdbc) {
        return new SapB1PlanCuentasJdbcAdapter(sapB1Jdbc, config.companyDb());
    }
}
