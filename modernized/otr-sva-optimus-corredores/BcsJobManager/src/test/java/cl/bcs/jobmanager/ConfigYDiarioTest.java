package cl.bcs.jobmanager;

import cl.bcs.jobmanager.config.JobManagerProperties;
import cl.bcs.jobmanager.domain.Programa;
import cl.bcs.jobmanager.service.DestinoResolver;
import cl.bcs.jobmanager.service.DiaryPollingService;
import cl.bcs.jobmanager.port.out.AgendaPort;
import cl.bcs.jobmanager.port.out.NodoPort;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.core.io.ClassPathResource;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** Defaults (inert, diary off), property binding of the activation settings and the destination map. */
class ConfigYDiarioTest {

    private static JobManagerProperties enlazar(Map<String, String> valores) {
        return new Binder(new MapConfigurationPropertySource(valores))
                .bind("jobmanager", JobManagerProperties.class).orElseGet(JobManagerProperties::new);
    }

    @Test
    void porDefectoEsInerteYElDiarioEstaApagado() {
        JobManagerProperties p = new JobManagerProperties();
        assertThat(p.inerte()).isTrue();
        assertThat(p.getAgenda().isHabilitada()).isFalse();
        assertThat(p.getReadTimeoutMs()).isGreaterThanOrEqualTo(600_000);
        assertThat(p.getConnectTimeoutMs()).isEqualTo(2_000);
    }

    @Test
    void elYmlDeProduccionNoActivaNada() throws Exception {
        String yml = new String(new ClassPathResource("application.yml").getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertThat(yml).contains("tipos-habilitados: ${JM_TIPOS_HABILITADOS:}").contains("habilitada: false");
    }

    @Test
    void enlazaTiposDestinosYClavesConPunto() {
        JobManagerProperties p = enlazar(Map.of(
                "jobmanager.tipos-habilitados", "A, B ,,A",
                "jobmanager.destinos.[cl.bcs.tesoreria]", "http://tes:8101/",
                "jobmanager.destinos.TIPO_X", "http://x:1"));
        assertThat(p.tiposActivos()).containsExactly("A", "B");
        assertThat(p.inerte()).isFalse();
        Programa clase = new Programa();
        clase.setIdTipoPrograma("OTRO");
        clase.setNombreClasePrograma("cl.bcs.tesoreria.jobs.UnJob");
        assertThat(new DestinoResolver(p.getDestinos()).resolver(clase)).contains("http://tes:8101");
        clase.setIdTipoPrograma("TIPO_X");
        assertThat(new DestinoResolver(p.getDestinos()).resolver(clase)).contains("http://x:1");
    }

    @Test
    void resolucionPorPrefijoDePaqueteMasLargoYSinCoincidenciasParciales() {
        DestinoResolver r = new DestinoResolver(Map.of("cl.bcs", "http://general", "cl.bcs.apv", "http://apv"));
        Programa p = new Programa();
        p.setIdTipoPrograma("Z");
        p.setNombreClasePrograma("cl.bcs.apv.jobs.Job");
        assertThat(r.resolver(p)).contains("http://apv");
        p.setNombreClasePrograma("cl.bcs.apvx.Job");
        assertThat(r.resolver(p)).contains("http://general");
        p.setNombreClasePrograma("otro.paquete.Job");
        assertThat(r.resolver(p)).isEmpty();
    }

    @Test
    void diarioApagadoPorDefectoNoHaceNada() {
        AgendaPort agenda = Mockito.mock(AgendaPort.class);
        NodoPort nodos = Mockito.mock(NodoPort.class);
        JobManagerProperties p = new JobManagerProperties();
        new DiaryPollingService(agenda, nodos, p).pollDiary();
        p.setMode("AGENDA"); // mode alone is not enough
        new DiaryPollingService(agenda, nodos, p).pollDiary();
        p.setMode("PROGRAMA");
        p.getAgenda().setHabilitada(true); // flag alone is not enough
        new DiaryPollingService(agenda, nodos, p).pollDiary();
        Mockito.verifyNoInteractions(agenda, nodos);
    }

    @Test
    void diarioEncendidoConsultaLaAgenda() {
        AgendaPort agenda = Mockito.mock(AgendaPort.class);
        NodoPort nodos = Mockito.mock(NodoPort.class);
        JobManagerProperties p = new JobManagerProperties();
        p.setMode("AGENDA");
        p.getAgenda().setHabilitada(true);
        new DiaryPollingService(agenda, nodos, p).pollDiary();
        Mockito.verify(agenda).readEnabledAgendas();
    }
}
