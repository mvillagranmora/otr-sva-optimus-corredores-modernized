package cl.bcs.ordenes;

import cl.bcs.ordenes.adapter.out.OrdenRepositoryAdapter;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class OrdenServiciosIntegrationTest {

    @Autowired
    OrdenRepositoryAdapter adapter;

    @Test
    void consultarEstado_moduloActivo_devuelveActivo() {
        String estado = adapter.consultarEstadoModulo("ORDENES");
        assertThat(estado).isEqualTo("ACTIVO");
    }

    @Test
    void consultarEstado_moduloInactivo_devuelveInactivo() {
        String estado = adapter.consultarEstadoModulo("BLOQUEO");
        assertThat(estado).isEqualTo("INACTIVO");
    }

    @Test
    void consultarEstado_otroModulo_devuelveActivo() {
        String estado = adapter.consultarEstadoModulo("CIERRE");
        assertThat(estado).isEqualTo("ACTIVO");
    }

    @Test
    void consultarEstado_moduloInexistente_devuelveNull() {
        String estado = adapter.consultarEstadoModulo("NO_EXISTE");
        assertThat(estado).isNull();
    }

    @Disabled("requires HANA SP: SP_CREAR_ORDEN — stored procedure sin equivalente en H2")
    @Test
    void registrarOrden_hana_sp() { }
}
