package cl.bcs.fondomutuo.kit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;

import org.junit.jupiter.api.Test;

/** FM-KIT-110..119: gates G0..G4 and G-CUS as configuration, FAIL CLOSED (skeleton; steps 3-4 call them before the first write). */
class CompuertasTest {

    private static Compuertas cerradas(String listas) {
        return new Compuertas(listas, false, false, false, false, false);
    }

    private static void cierra(Runnable r, String compuerta) {
        assertThatThrownBy(r::run).isInstanceOf(ServicioNoServidoException.class).extracting(e -> ((ServicioNoServidoException) e).compuerta()).isEqualTo(compuerta);
    }

    @Test
    void fmKit110_porDefecto_todaCompuertaInternaEstaCerrada() {
        Compuertas c = cerradas("NORMAL");
        cierra(c::g1MedioDistintoDeCin, "G1");
        cierra(c::g2Spot, "G2");
        cierra(c::g3SaldoFlujoCaja, "G3");
        cierra(c::g4Owp, "G4");
        cierra(c::gCustodia, "G-CUS");
    }

    @Test
    void fmKit111_abrirUnaPropiedad_abreSoloEsaCompuerta() {
        Compuertas c = new Compuertas("NORMAL", true, false, false, false, false);
        assertThatCode(c::g1MedioDistintoDeCin).doesNotThrowAnyException();
        cierra(c::g2Spot, "G2");
        assertThatCode(new Compuertas("NORMAL", false, true, true, true, true)::g2Spot).doesNotThrowAnyException();
    }

    @Test
    void fmKit112_g0_unaListaConFilasNoServida_503() {
        Compuertas c = cerradas("NORMAL");
        assertThatCode(() -> c.g0ListasConFilas(Set.of("NORMAL"))).doesNotThrowAnyException();
        assertThatCode(() -> c.g0ListasConFilas(Set.of())).doesNotThrowAnyException();
        cierra(() -> c.g0ListasConFilas(Set.of("PROGRAMADO")), "G0");
        cierra(() -> c.g0ListasConFilas(Set.of("NORMAL", "RESCATE_REINVERSION")), "G0");   // mixed request: 503 as a whole
    }

    @Test
    void fmKit113_listasServidas_sinValorOVacio_nadaSeSirve_fallaCerrado() {
        assertThat(cerradas("").listasServidas()).isEmpty();
        cierra(() -> cerradas("").g0ListasConFilas(Set.of("NORMAL")), "G0");
        cierra(() -> cerradas(" , ,").g0ListasConFilas(Set.of("NORMAL")), "G0");
    }

    @Test
    void fmKit114_listasServidas_seNormaliza_mayusculasYEspacios_variasLista() {
        Compuertas c = cerradas(" normal , Rescate_Reinversion ");
        assertThat(c.listasServidas()).containsExactlyInAnyOrder("NORMAL", "RESCATE_REINVERSION");
        assertThatCode(() -> c.g0ListasConFilas(Set.of("NORMAL", "RESCATE_REINVERSION"))).doesNotThrowAnyException();
    }

    @Test
    void fmKit115_laExcepcionNoTrasportaTextoDeNegocio_soloLaCompuertaYElMotivo() {
        ServicioNoServidoException e = new ServicioNoServidoException("G2", "Spot operation needs Mesa CrearOperacionSpot");
        assertThat(e.getMessage()).startsWith("G2: ");
        assertThat(e.compuerta()).isEqualTo("G2");
    }
}
