package cl.bcs.apv.jobs.wiring;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.ClassUtils;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.context.ActiveProfiles;

import cl.bcs.apv.jobs.adapter.ColaboradoresJobs;
import cl.bcs.apv.jobs.runtime.JobRunner;
import cl.bcs.apv.jobs.web.JobCallController;
import cl.bcs.apv.jobs.web.TokenInternoFilter;

/**
 * Architecture review M-9. The component scan of {@link ServiciosApvConfiguration} pulls {@code cl.bcs.apv.infrastructure} of the service: this test
 * pins what ends up in the job context. Beans of the job module itself (runner, controller, filter, collaborators) are expected by type; there is no
 * {@code @Scheduled} method and no controller outside {@code cl.bcs.apv.jobs.web}. {@code TransaccionesLegacyConfig} (service) is intended to be
 * here: {@code globalRollbackOnParticipationFailure=false} applies to the one transaction manager, hence also to the {@code JobRunner} run
 * transactions (a participating collaborator that throws no longer marks the job transaction rollback-only; the runner's own rollback decision stands).
 */
@SpringBootTest(properties = {"services.javacall.url=http://localhost:9", "apv.jobs.sesion-hana=false"})
@ActiveProfiles("test")
class ContextoDeJobsTest {

    @Autowired
    ApplicationContext contexto;

    @Test
    void beansEsperadosDelModuloDeJobsPorTipo() {
        for (Class<?> tipo : List.of(JobRunner.class, JobCallController.class, TokenInternoFilter.class, ColaboradoresJobs.class)) {
            assertThat(contexto.getBeanNamesForType(tipo)).as(tipo.getSimpleName()).hasSize(1);
        }
        assertThat(contexto.getBeanNamesForType(PlatformTransactionManager.class)).hasSize(1);
    }

    @Test
    void ningunBeanTieneMetodosScheduled() {
        List<String> con = new ArrayList<>();
        for (String nombre : contexto.getBeanDefinitionNames()) {
            Class<?> tipo = contexto.getType(nombre);
            if (tipo == null) {
                continue;
            }
            Class<?> real = ClassUtils.getUserClass(tipo);
            for (var metodo : real.getDeclaredMethods()) {
                if (AnnotatedElementUtils.hasAnnotation(metodo, Scheduled.class)) {
                    con.add(nombre + "#" + metodo.getName());
                }
            }
        }
        assertThat(con).isEmpty();
    }

    @Test
    void ningunControladorFueraDelPaqueteWebDelModulo() {
        List<String> fuera = new ArrayList<>();
        for (String nombre : contexto.getBeanDefinitionNames()) {
            Class<?> tipo = contexto.getType(nombre);
            if (tipo == null) {
                continue;
            }
            Class<?> real = ClassUtils.getUserClass(tipo);
            boolean controlador = AnnotatedElementUtils.hasAnnotation(real, Controller.class); // @RestController is meta-annotated with it
            if (controlador && !real.getName().startsWith("cl.bcs.apv.jobs.web.")
                    && !real.getName().startsWith("org.springframework.boot.")) {
                fuera.add(real.getName());
            }
        }
        assertThat(fuera).isEmpty();
    }

    @Test
    void laTransaccionDelRunnerLlevaLaSemanticaLegacyDelServicio() {
        PlatformTransactionManager tm = contexto.getBean(PlatformTransactionManager.class);
        assertThat(tm).isInstanceOf(DataSourceTransactionManager.class);
        assertThat(((DataSourceTransactionManager) tm).isGlobalRollbackOnParticipationFailure()).isFalse();
        assertThat(ReflectionTestUtils.getField(contexto.getBean(JobRunner.class), "transacciones")).isSameAs(tm);
    }
}
