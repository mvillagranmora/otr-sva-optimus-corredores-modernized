package cl.bcs.contabilidad.infrastructure.batch;

import cl.bcs.contabilidad.domain.port.out.InterfazContadorSource;
import cl.bcs.contabilidad.domain.port.out.InterfazFileTransfer;
import cl.bcs.contabilidad.domain.port.out.InterfazSapSource;
import cl.bcs.contabilidad.domain.port.out.PlataformaPort;
import cl.bcs.contabilidad.infrastructure.interfaz.InterfazGenerators;
import cl.bcs.contabilidad.infrastructure.persistence.InterfazContadorJdbcSource;
import cl.bcs.contabilidad.infrastructure.sap.SapB1Jdbc;
import cl.bcs.contabilidad.infrastructure.sap.SapB1InterfazJdbcSource;
import cl.bcs.contabilidad.infrastructure.sftp.JschInterfazFileTransfer;
import cl.bcs.contabilidad.infrastructure.sftp.SftpSettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.time.Clock;

/**
 * Wiring of the daily interface job (legacy {@code CreaInterfazDiaria}, BcsContabilidadJobs).
 *
 * <p>The contador runs on the application's HANA datasource. SAP B1 and SFTP settings are read from the
 * environment on first use, so the application starts without them and a missing one only fails the affected step
 * (logged by the job). The clock is the JVM default zone, as the legacy {@code Calendar.getInstance()} was.
 * The SAP B1 datasource is deliberately not a bean, so Spring Boot keeps auto-configuring the HANA one.
 */
@Configuration(proxyBeanMethods = false)
public class InterfazDiariaConfiguration {

    @Bean
    InterfazContadorSource interfazContadorSource(NamedParameterJdbcTemplate jdbc) {
        return new InterfazContadorJdbcSource(jdbc);
    }

    @Bean
    InterfazSapSource interfazSapSource(SapB1Jdbc sapB1Jdbc) {
        return new SapB1InterfazJdbcSource(sapB1Jdbc);
    }

    @Bean
    InterfazFileTransfer interfazFileTransfer() {
        return new JschInterfazFileTransfer(SftpSettings::fromEnvironment);
    }

    @Bean
    CreaInterfazDiariaJob creaInterfazDiariaJob(PlataformaPort plataforma,
                                                InterfazContadorSource contador,
                                                InterfazSapSource sap,
                                                InterfazFileTransfer transferencia) {
        Clock clock = Clock.systemDefaultZone();
        return new CreaInterfazDiariaJob(plataforma, contador, InterfazGenerators.enOrdenLegacy(sap, clock),
                transferencia, clock);
    }
}
