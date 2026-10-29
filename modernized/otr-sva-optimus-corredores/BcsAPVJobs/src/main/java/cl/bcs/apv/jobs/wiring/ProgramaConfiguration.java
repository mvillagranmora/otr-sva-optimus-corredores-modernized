package cl.bcs.apv.jobs.wiring;

import cl.bcs.apv.jobs.guarda.GuardaTomee;
import cl.bcs.apv.jobs.programa.EjecutorPrograma;
import cl.bcs.apv.jobs.programa.ProgramaModo;
import cl.bcs.apv.jobs.programa.ProgramaRepositorio;
import cl.bcs.apv.jobs.programa.ProgramaRepositorioJdbc;
import cl.bcs.apv.jobs.programa.ProgramaStatusListener;
import cl.bcs.apv.jobs.runtime.JobRunner;
import cl.bcs.apv.jobs.runtime.JobStatusListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Wiring of job pattern H4 (parameters and claim from {@code BAT.Programa}, final state written back). The mode is the property
 * {@code apv.jobs.programa.modo}: {@code programa} (default, production) or {@code parametros-del-llamador} (tests and local runs:
 * the request carries class and parameters and {@code BAT} is not touched). An unknown value stops the start.
 */
@Configuration(proxyBeanMethods = false)
public class ProgramaConfiguration {

    private static final Logger log = LoggerFactory.getLogger(ProgramaConfiguration.class);

    @Bean
    ProgramaModo programaModo(@Value("${apv.jobs.programa.modo:programa}") String modo) {
        ProgramaModo m = ProgramaModo.desde(modo);
        log.info("Job pattern H4: apv.jobs.programa.modo={}", m.valor());
        return m;
    }

    /** Its own connections from the application data source: it commits by itself, apart from the job transaction. */
    @Bean
    ProgramaRepositorio programaRepositorio(DataSource dataSource) {
        return new ProgramaRepositorioJdbc(dataSource);
    }

    /**
     * Mode {@code programa}: the real listener, which writes TER / ERR to {@code BAT.Programa}. Caller mode: no row exists, the status is only
     * logged (the caller of {@code /internal/job-call} also gets the outcome in the response).
     */
    @Bean
    JobStatusListener jobStatusListener(ProgramaModo modo, ProgramaRepositorio repositorio) {
        if (modo == ProgramaModo.PROGRAMA) {
            return new ProgramaStatusListener(repositorio);
        }
        return (idPrograma, error, mensaje) -> {
            if (error) {
                log.warn("Job #{} status: error, message: {}", idPrograma, mensaje);
            } else {
                log.info("Job #{} status: ok", idPrograma);
            }
        };
    }

    @Bean
    EjecutorPrograma ejecutorPrograma(JobRunner runner, ProgramaRepositorio repositorio, ProgramaModo modo,
                                      ObjectProvider<GuardaTomee> guarda) {
        return new EjecutorPrograma(runner, repositorio, modo, guarda.getIfAvailable()); // step 6: absent when apv.jobs.guarda-tomee=false
    }
}
