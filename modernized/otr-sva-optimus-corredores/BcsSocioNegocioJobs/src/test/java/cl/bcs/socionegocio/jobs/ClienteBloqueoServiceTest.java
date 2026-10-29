package cl.bcs.socionegocio.jobs;

import cl.bcs.socionegocio.jobs.domain.ClienteABloquear;
import cl.bcs.socionegocio.jobs.domain.DocumentoCliente;
import cl.bcs.socionegocio.jobs.service.ClienteBloqueoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pure-logic unit tests for ClienteBloqueoService.
 * No Spring context required — all methods are public static.
 *
 * Canary: calcularFechaVencimiento(2024-01-01, 30) == 2024-01-31
 * Executed cases: 13
 */
class ClienteBloqueoServiceTest {

    // -----------------------------------------------------------------------
    // B-BLQ-01: calcularFechaVencimiento
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("B-BLQ-01a: calcularFechaVencimiento adds 30 days correctly")
    void calcularFechaVencimiento_30dias() {
        LocalDate result = ClienteBloqueoService.calcularFechaVencimiento(
                LocalDate.of(2024, 1, 1), 30);
        assertThat(result).isEqualTo(LocalDate.of(2024, 1, 31));
    }

    @Test
    @DisplayName("B-BLQ-01b: calcularFechaVencimiento with 0 days returns same date")
    void calcularFechaVencimiento_0dias() {
        LocalDate base = LocalDate.of(2024, 1, 1);
        assertThat(ClienteBloqueoService.calcularFechaVencimiento(base, 0)).isEqualTo(base);
    }

    // -----------------------------------------------------------------------
    // B-BLQ-02: esPautaAnteriorTerminada
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("B-BLQ-02a: esPautaAnteriorTerminada returns true for TER")
    void esPautaAnteriorTerminada_TER() {
        assertThat(ClienteBloqueoService.esPautaAnteriorTerminada("TER")).isTrue();
    }

    @Test
    @DisplayName("B-BLQ-02b: esPautaAnteriorTerminada returns false for PEN")
    void esPautaAnteriorTerminada_PEN() {
        assertThat(ClienteBloqueoService.esPautaAnteriorTerminada("PEN")).isFalse();
    }

    @Test
    @DisplayName("B-BLQ-02c: esPautaAnteriorTerminada returns false for null (NPE safe)")
    void esPautaAnteriorTerminada_null() {
        assertThat(ClienteBloqueoService.esPautaAnteriorTerminada(null)).isFalse();
    }

    // -----------------------------------------------------------------------
    // B-BLQ-03: clienteVenceHoy
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("B-BLQ-03a: clienteVenceHoy returns true when dates match")
    void clienteVenceHoy_match() {
        LocalDate fecha = LocalDate.of(2024, 1, 15);
        assertThat(ClienteBloqueoService.clienteVenceHoy(fecha, fecha)).isTrue();
    }

    @Test
    @DisplayName("B-BLQ-03b: clienteVenceHoy returns false when dates differ")
    void clienteVenceHoy_noMatch() {
        assertThat(ClienteBloqueoService.clienteVenceHoy(
                LocalDate.of(2024, 1, 16), LocalDate.of(2024, 1, 15))).isFalse();
    }

    // -----------------------------------------------------------------------
    // B-BLQ-04: debeBloquearAlVencer
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("B-BLQ-04a: debeBloquearAlVencer returns true for S")
    void debeBloquearAlVencer_S() {
        assertThat(ClienteBloqueoService.debeBloquearAlVencer("S")).isTrue();
    }

    @Test
    @DisplayName("B-BLQ-04b: debeBloquearAlVencer returns false for N")
    void debeBloquearAlVencer_N() {
        assertThat(ClienteBloqueoService.debeBloquearAlVencer("N")).isFalse();
    }

    // -----------------------------------------------------------------------
    // B-BLQ-05: buildClienteABloquear
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("B-BLQ-05: buildClienteABloquear uses hard-coded BAUTOM and vencimiento comment")
    void buildClienteABloquear_hardCoded() {
        ClienteABloquear c = ClienteBloqueoService.buildClienteABloquear(100L);
        assertThat(c.idCliente()).isEqualTo(100L);
        assertThat(c.idTipoBloqueo()).isEqualTo("BAUTOM");
        assertThat(c.comentarioBloqueo()).contains("vencimiento");
    }

    // -----------------------------------------------------------------------
    // B-BLQ-06: calcularFechasVencimientoLote
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("B-BLQ-06a: calcularFechasVencimientoLote skips documents with estado=PEN")
    void calcularFechasVencimientoLote_skipsPEN() {
        DocumentoCliente doc = new DocumentoCliente(
                1L, 1L, "PEN", "S",
                LocalDate.of(2024, 1, 15), LocalDate.of(2023, 12, 16));
        List<DocumentoCliente> result =
                ClienteBloqueoService.calcularFechasVencimientoLote(List.of(doc), 30);
        // fechaVigencia must remain unchanged
        assertThat(result.getFirst().fechaVigencia()).isEqualTo(LocalDate.of(2024, 1, 15));
    }

    @Test
    @DisplayName("B-BLQ-06b: calcularFechasVencimientoLote skips documents with null fechaModificacion")
    void calcularFechasVencimientoLote_skipsNullFechaModificacion() {
        DocumentoCliente doc = new DocumentoCliente(
                2L, 2L, "ACT", "S",
                LocalDate.of(2024, 1, 15), null);
        List<DocumentoCliente> result =
                ClienteBloqueoService.calcularFechasVencimientoLote(List.of(doc), 30);
        assertThat(result.getFirst().fechaVigencia()).isEqualTo(LocalDate.of(2024, 1, 15));
    }

    // -----------------------------------------------------------------------
    // B-BLQ-07: filtrarClientesABloquear
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("B-BLQ-07: filtrarClientesABloquear returns only docs where venceHoy AND BloquearAlVencer=S")
    void filtrarClientesABloquear_combinedFilter() {
        LocalDate hoy = LocalDate.of(2024, 1, 15);

        DocumentoCliente bloqueaHoy = new DocumentoCliente(100L, 1L, "ACT", "S", hoy, LocalDate.of(2023, 12, 16));
        DocumentoCliente noBloquea  = new DocumentoCliente(101L, 2L, "ACT", "N", hoy, LocalDate.of(2023, 12, 16));
        DocumentoCliente futuro     = new DocumentoCliente(103L, 4L, "ACT", "S", hoy.plusDays(5), LocalDate.of(2023, 12, 21));

        List<ClienteABloquear> resultado = ClienteBloqueoService.filtrarClientesABloquear(
                List.of(bloqueaHoy, noBloquea, futuro), hoy);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().idCliente()).isEqualTo(100L);
    }
}
