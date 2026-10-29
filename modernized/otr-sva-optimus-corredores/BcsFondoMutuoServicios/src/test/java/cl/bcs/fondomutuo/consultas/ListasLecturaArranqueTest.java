package cl.bcs.fondomutuo.consultas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import cl.bcs.fondomutuo.consultas.custodia.ConsultarCustodiaPortafolioCustodiaCerrada;
import cl.bcs.fondomutuo.consultas.custodia.ConsultarCustodiaPortafolioCustodiaPort;

/** Architecture review F3 W1: M1 (CPF open with the custody closed stops the start-up), M5 (INFO with the open reads) and M2 (WARN when the zone is not UTC; never a failure). */
class ListasLecturaArranqueTest {

    private ListAppender<ILoggingEvent> log;
    private Logger logger;

    @BeforeEach
    void capturar() {
        logger = (Logger) LoggerFactory.getLogger(ListasLecturaArranque.class);
        log = new ListAppender<>();
        log.start();
        logger.addAppender(log);
        logger.setLevel(Level.INFO);
    }

    @AfterEach
    void soltar() {
        logger.detachAppender(log);
    }

    private List<String> mensajes(Level nivel) {
        return log.list.stream().filter(e -> e.getLevel() == nivel).map(ILoggingEvent::getFormattedMessage).toList();
    }

    private static final ConsultarCustodiaPortafolioCustodiaPort REAL = (JsonObject in) -> new JsonObject();

    @Test
    void m1_cpfAbiertaConLaCustodiaCerrada_detieneElArranque() {
        assertThatThrownBy(() -> ListasLecturaArranque.iniciar(Set.of("NORMAL", "CONSULTAR_CUSTODIA_PORTAFOLIO"), new ConsultarCustodiaPortafolioCustodiaCerrada(),
                TimeZone.getTimeZone("UTC"))).isInstanceOf(IllegalStateException.class).hasMessageContaining("CONSULTAR_CUSTODIA_PORTAFOLIO").hasMessageContaining("custody");
    }

    @Test
    void m1_cpfConUnAdaptadorRealOCerradaSinCpf_noFalla() {
        assertThatCode(() -> ListasLecturaArranque.iniciar(Set.of("CONSULTAR_CUSTODIA_PORTAFOLIO"), REAL, TimeZone.getTimeZone("UTC"))).doesNotThrowAnyException();
        assertThatCode(() -> ListasLecturaArranque.iniciar(Set.of("NORMAL", "CONSULTAR_FECHA_SOLICITUD"), new ConsultarCustodiaPortafolioCustodiaCerrada(),
                TimeZone.getTimeZone("UTC"))).doesNotThrowAnyException();
    }

    @Test
    void m5_registraSoloLosNombresDeLasLecturasAbiertas() {
        ListasLecturaArranque.iniciar(Set.of("NORMAL", "CONSULTAR_VALOR_DOLAR", "CONSULTAR_FECHA_SOLICITUD"), new ConsultarCustodiaPortafolioCustodiaCerrada(),
                TimeZone.getTimeZone("UTC"));
        assertThat(mensajes(Level.INFO)).hasSize(1);
        assertThat(mensajes(Level.INFO).get(0)).contains("[CONSULTAR_FECHA_SOLICITUD, CONSULTAR_VALOR_DOLAR]").doesNotContain("NORMAL");
        assertThat(mensajes(Level.WARN)).isEmpty();
    }

    @Test
    void m2_zonaNoUtcConLecturasAbiertas_avisaYNoFalla() {
        assertThatCode(() -> ListasLecturaArranque.iniciar(Set.of("CONSULTAR_FECHA_SOLICITUD"), new ConsultarCustodiaPortafolioCustodiaCerrada(),
                TimeZone.getTimeZone("America/Santiago"))).doesNotThrowAnyException();
        assertThat(mensajes(Level.WARN)).hasSize(1);
        assertThat(mensajes(Level.WARN).get(0)).contains("America/Santiago").contains("UTC");
    }

    @Test
    void m2_sinAvisoSiLaZonaEsUtcOSiNoHayLecturasAbiertas() {
        ListasLecturaArranque.iniciar(Set.of("CONSULTAR_FECHA_SOLICITUD"), new ConsultarCustodiaPortafolioCustodiaCerrada(), TimeZone.getTimeZone("UTC"));
        ListasLecturaArranque.iniciar(Set.of("NORMAL"), new ConsultarCustodiaPortafolioCustodiaCerrada(), TimeZone.getTimeZone("America/Santiago"));
        assertThat(mensajes(Level.WARN)).isEmpty();
    }

    @Test
    void m4_laListaDeEscrituraAbierta_seRegistraComoUiWrite_conAvisoDeZonaNoUtc_yNoFalla() {
        assertThatCode(() -> ListasLecturaArranque.iniciar(Set.of("NORMAL", "CARGAR_VALOR_CUOTA"), new ConsultarCustodiaPortafolioCustodiaCerrada(),
                TimeZone.getTimeZone("America/Santiago"))).doesNotThrowAnyException();
        assertThat(mensajes(Level.INFO)).contains("UI WRITE open: CARGAR_VALOR_CUOTA");
        assertThat(mensajes(Level.WARN)).hasSize(1);
        assertThat(mensajes(Level.WARN).get(0)).contains("America/Santiago").contains("UTC");
    }

    @Test
    void m4_conLaListaDeEscrituraEnUtc_noHayAviso_ySinLaLista_noHayLineaUiWrite() {
        ListasLecturaArranque.iniciar(Set.of("CARGAR_VALOR_CUOTA"), new ConsultarCustodiaPortafolioCustodiaCerrada(), TimeZone.getTimeZone("UTC"));
        assertThat(mensajes(Level.WARN)).isEmpty();
        assertThat(mensajes(Level.INFO)).contains("UI WRITE open: CARGAR_VALOR_CUOTA");
        log.list.clear();
        ListasLecturaArranque.iniciar(Set.of("NORMAL", "CONSULTAR_VALOR_DOLAR"), new ConsultarCustodiaPortafolioCustodiaCerrada(), TimeZone.getTimeZone("UTC"));
        assertThat(mensajes(Level.INFO)).noneMatch(m -> m.contains("UI WRITE"));
    }
}
