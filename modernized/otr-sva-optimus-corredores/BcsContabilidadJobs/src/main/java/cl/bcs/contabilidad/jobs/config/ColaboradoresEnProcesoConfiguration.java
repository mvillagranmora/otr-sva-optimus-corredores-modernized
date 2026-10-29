package cl.bcs.contabilidad.jobs.config;

import cl.bcs.contabilidad.domain.port.out.PlataformaPort;
import cl.bcs.contabilidad.infrastructure.plataforma.TomEePlataformaClient;
import cl.bcs.contabilidad.jobs.adapter.DespachadorEnProceso;
import cl.bcs.contabilidad.jobs.adapter.EntradaEnProceso;
import cl.bcs.contabilidad.jobs.adapter.PlataformaEnProceso;
import cl.bcs.tesoreria.javacall.JavaCallDispatcher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

/**
 * Step 5 (JOBS_PATTERN.md H-1): the collaborators that are ALREADY migrated as libraries run in process, in the transaction of the job. Active only
 * with {@code contabilidad.jobs.colaboradores-en-proceso=true} (application.yml); without it (or {@code false}) the wiring is exactly the pre-step-5
 * one: {@code TomEePlataformaClient} is the only {@code PlataformaPort} and the clearing goes over HTTP.
 *
 * <p>With it, {@link PlataformaEnProceso} is the {@code @Primary} {@code PlataformaPort}: it answers Tesoreria's {@code ConsultarClearingFinanciero}
 * in process and delegates everything else to {@code TomEePlataformaClient} (a plain bean then). The Contabilidad services are not edited: they
 * inject the port by type and get the primary one. No library depends on the other at compile time: the link between them is this class.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = ColaboradoresEnProcesoConfiguration.PROPIEDAD, havingValue = "true")
@Import(ServiciosTesoreriaEnProcesoConfiguration.class)
public class ColaboradoresEnProcesoConfiguration {

    public static final String PROPIEDAD = "contabilidad.jobs.colaboradores-en-proceso";

    /**
     * Tesoreria's in-process entry point. Only the clearing is claimed: the dispatcher knows 50 names, but only the clearing service is a bean in this
     * application ({@link ServiciosTesoreriaEnProcesoConfiguration}); any other Tesoreria class would go over HTTP.
     */
    @Bean
    EntradaEnProceso entradaTesoreria(JavaCallDispatcher dispatcher) {
        return EntradaEnProceso.de("tesoreria",
                c -> PlataformaEnProceso.CLEARING_FINANCIERO.equals(c) && dispatcher.maneja(c), dispatcher::despachar);
    }

    @Bean
    DespachadorEnProceso despachadorEnProceso(List<EntradaEnProceso> propietarios) {
        return new DespachadorEnProceso(propietarios);
    }

    @Bean
    @Primary
    PlataformaPort plataformaEnProceso(TomEePlataformaClient http, DespachadorEnProceso despachador,
                                       PlatformTransactionManager transacciones) {
        return new PlataformaEnProceso(http, despachador, transacciones);
    }
}
