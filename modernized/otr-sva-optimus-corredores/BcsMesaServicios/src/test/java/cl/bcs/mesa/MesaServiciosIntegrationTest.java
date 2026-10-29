package cl.bcs.mesa;

import cl.bcs.mesa.adapter.out.MesaRepositoryAdapter;
import cl.bcs.mesa.domain.PosicionInicial;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class MesaServiciosIntegrationTest {

    @Autowired
    MesaRepositoryAdapter adapter;

    @Test
    void posiciones_fechaConDatos_devuelveListaCorrecta() {
        List<PosicionInicial> result = adapter.obtenerPosicionesIniciales("2024-01-15");
        assertThat(result).hasSize(2);
        assertThat(result).extracting(p -> p.IdMoneda)
            .containsExactlyInAnyOrder("USD", "EUR");
    }

    @Test
    void posiciones_USD_valoresCorrecto() {
        List<PosicionInicial> result = adapter.obtenerPosicionesIniciales("2024-01-15");
        PosicionInicial usd = result.stream()
            .filter(p -> "USD".equals(p.IdMoneda)).findFirst().orElseThrow();
        assertThat(usd.PosicionInicial).isEqualByComparingTo(new BigDecimal("1000000.0000"));
        assertThat(usd.TipoCambioInicial).isEqualByComparingTo(new BigDecimal("930.5000"));
        assertThat(usd.CierreIngresado).isEqualByComparingTo(new BigDecimal("931.2000"));
    }

    @Test
    void posiciones_EUR_valoresCorrecto() {
        List<PosicionInicial> result = adapter.obtenerPosicionesIniciales("2024-01-15");
        PosicionInicial eur = result.stream()
            .filter(p -> "EUR".equals(p.IdMoneda)).findFirst().orElseThrow();
        assertThat(eur.PosicionInicial).isEqualByComparingTo(new BigDecimal("500000.0000"));
        assertThat(eur.TipoCambioInicial).isEqualByComparingTo(new BigDecimal("1010.2500"));
    }

    @Test
    void posiciones_fechaSinDatos_devuelveListaVacia() {
        List<PosicionInicial> result = adapter.obtenerPosicionesIniciales("2099-12-31");
        assertThat(result).isEmpty();
    }

    @Test
    void posiciones_fechaDistinta_devuelveUnaFila() {
        List<PosicionInicial> result = adapter.obtenerPosicionesIniciales("2024-01-14");
        assertThat(result).hasSize(1);
        assertThat(result.get(0).IdMoneda).isEqualTo("USD");
        assertThat(result.get(0).PosicionInicial).isEqualByComparingTo(new BigDecimal("800000.0000"));
    }

    @Disabled("requires HANA SP: SP_OBTENER_MOVIMIENTOS_SPT — stored procedure sin equivalente en H2")
    @Test
    void movimientosSpot_hana_sp() { }
}
