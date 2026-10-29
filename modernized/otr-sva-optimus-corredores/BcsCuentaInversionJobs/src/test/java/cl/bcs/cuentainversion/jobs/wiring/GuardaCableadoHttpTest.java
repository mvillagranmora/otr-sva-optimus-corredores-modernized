package cl.bcs.cuentainversion.jobs.wiring;

import cl.bcs.cuentainversion.jobs.adapter.ColaboradoresJobs;
import cl.bcs.cuentainversion.jobs.guarda.ColaboradorTomEEGuardado;
import cl.bcs.cuentainversion.jobs.guarda.GuardaTomee;
import cl.bcs.cuentainversion.jobs.job.pauta.PautaGenerarReporteAcreedores;
import cl.bcs.cuentainversion.jobs.programa.EjecutorPrograma;
import cl.bcs.cuentainversion.port.out.Colaboradores;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

/** Step 6 wiring with the guard on: the guard is the primary port around {@code ColaboradoresJobs}, the executor holds the guard. */
@SpringBootTest(properties = {"services.javacall.url=http://localhost:9", "cuentainversion.jobs.sesion-hana=false",
        "cuentainversion.jobs.guarda-tomee=true"})
@ActiveProfiles("test")
class GuardaCableadoHttpTest {

    @Autowired
    ApplicationContext contexto;
    @Autowired
    Colaboradores colaboradores;
    @Autowired
    EjecutorPrograma ejecutor;

    @Test
    @DisplayName("G6-W01: guard on: the primary port is the guard; ColaboradoresJobs is its only delegate; the executor has the guard")
    void laGuardaEsElPuertoPrimario() {
        assertThat(colaboradores).isInstanceOf(ColaboradorTomEEGuardado.class);
        assertThat(contexto.getBeansOfType(ColaboradoresJobs.class)).hasSize(1);
        assertThat(ReflectionTestUtils.getField(colaboradores, "delegado")).isSameAs(contexto.getBean(ColaboradoresJobs.class));
        assertThat(contexto.getBeansOfType(GuardaTomee.class)).hasSize(1);
        assertThat(ReflectionTestUtils.getField(ejecutor, "guarda")).isSameAs(contexto.getBean(GuardaTomee.class));
    }

    @Test
    @DisplayName("G6-W02: the services (through the reporting adapter) and the PAUTA steps receive the guarded port")
    void losConsumidoresRecibenElPuertoGuardado() {
        assertThat(ReflectionTestUtils.getField(contexto.getBean(PautaGenerarReporteAcreedores.class), "colaboradores")).isSameAs(colaboradores);
        Object adaptador = contexto.getBean(cl.bcs.cuentainversion.infrastructure.adapter.ColaboradoresReportesAdapter.class);
        assertThat(ReflectionTestUtils.getField(adaptador, "colaboradores")).isSameAs(colaboradores);
        Object auditoria = contexto.getBean(cl.bcs.cuentainversion.infrastructure.adapter.AuditoriaReportesAdapter.class);
        assertThat(ReflectionTestUtils.getField(auditoria, "colaboradores")).isSameAs(colaboradores);
    }
}
