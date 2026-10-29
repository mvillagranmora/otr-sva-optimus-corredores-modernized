package cl.bcs.becb;

import cl.bcs.becb.adapter.out.BecbRepositoryAdapter;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class BecbIntegrationTest {

    @Autowired
    BecbRepositoryAdapter adapter;

    // --- obtenerIdCliente (H2) ---

    @Test
    void obtenerIdCliente_devuelveIdDesdeH2() {
        Long id = adapter.obtenerIdCliente();
        assertThat(id).isEqualTo(42L);
    }

    // --- obtenerValorUF (H2) ---

    @Test
    void obtenerValorUF_fechaConDatos_devuelveValor() {
        BigDecimal uf = adapter.obtenerValorUF("2024-01-15");
        assertThat(uf).isEqualByComparingTo(new BigDecimal("37000.123456"));
    }

    @Test
    void obtenerValorUF_fechaDistinta_devuelveOtroValor() {
        BigDecimal uf = adapter.obtenerValorUF("2024-01-14");
        assertThat(uf).isEqualByComparingTo(new BigDecimal("36998.654321"));
    }

    @Test
    void obtenerValorUF_fechaSinDatos_devuelveNull() {
        BigDecimal uf = adapter.obtenerValorUF("2099-12-31");
        assertThat(uf).isNull();
    }

    // --- Queries HANA-específicas (@Disabled) ---

    @Disabled("requires HANA: ConsultarInformeRiesgoFinanciero.sql — query con lógica HANA-específica (joins a vistas calculadas)")
    @Test
    void consultarRiesgoFinanciero_hana() { }

    @Disabled("requires HANA: ConsultarDetalleRiesgoFinanciero.sql — query con lógica HANA-específica (joins a vistas calculadas)")
    @Test
    void consultarDetalleRiesgoFinanciero_hana() { }
}
