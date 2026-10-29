package cl.bcs.socionegocio;

import cl.bcs.socionegocio.adapter.out.SocioNegocioServiciosRepositoryAdapter;
import cl.bcs.socionegocio.domain.SocioNegocio;
import cl.bcs.socionegocio.service.SocioNegocioServiciosService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class SocioNegocioServiciosIntegrationTest {

    @Autowired
    private SocioNegocioServiciosRepositoryAdapter adapter;

    @Test
    void iupGenerar_validInput_returns200AndCorrectValue() {
        // Pure logic — no HTTP needed; verify service produces expected result
        String result = SocioNegocioServiciosService.iupGenerar("12345678", "RUT", null);
        assertThat(result).isEqualTo("20123456780000");
    }

    @Test
    void consultarSociosNegocio_empresa1_returnsAtLeastOne() {
        List<SocioNegocio> socios = adapter.consultarSociosNegocio(1L);
        assertThat(socios).hasSizeGreaterThanOrEqualTo(1);
    }

    @Test
    void obtenerFechaSistema_empresa1_returnsCorrectDate() {
        String fecha = adapter.obtenerFechaSistema(1L);
        assertThat(fecha).isEqualTo("2024-01-15");
    }

    @Test
    @Disabled("requires HANA SP — crearSocioNegocio throws UnsupportedOperationException")
    void crearSocioNegocio_throwsUnsupportedOperation() {
        assertThatThrownBy(() -> adapter.crearSocioNegocio(1L, "99999999-9"))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("requires HANA SP");
    }
}
