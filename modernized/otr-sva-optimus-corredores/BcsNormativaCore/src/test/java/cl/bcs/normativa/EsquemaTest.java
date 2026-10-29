package cl.bcs.normativa;

import cl.bcs.normativa.domain.Campo;
import cl.bcs.normativa.domain.Esquema;
import cl.bcs.normativa.domain.EsquemaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Characterization tests for Esquema domain logic.
 *
 * Oracle: legacy cl.bcs.normativa.esquema.Esquema
 *
 * Business rules pinned:
 *   B-NOR-ESQ-01: buscarCampoPorNombre lanza EsquemaException si no existe
 *   B-NOR-ESQ-02: buscarCampoPorCodigoSII lanza EsquemaException si no existe
 *   B-NOR-ESQ-03: getCamposClaves retorna solo campos con clave=true
 *   B-NOR-ESQ-04: getCamposComunes retorna solo campos con comun=true
 *   B-NOR-ESQ-05: getNombreCampoFecha retorna nombre del campo con tipoDato=DATE_FILTER
 *   B-NOR-ESQ-06: getNombreCampoRutCliente retorna nombre del campo con tipoDato=RUT_CLIENTE
 *   B-NOR-ESQ-07: buscarCampoCargaExterna busca por ordenCargaExterna
 *   B-NOR-ESQ-08: getNombreTablaOrigen/Destino formatea correctamente
 */
@DisplayName("Esquema — domain logic characterization")
class EsquemaTest {

    private Esquema esquema;
    private Campo campoPeriodo;
    private Campo campoRut;
    private Campo campoMonto;

    @BeforeEach
    void setUp() {
        esquema = new Esquema();
        esquema.setNombre("DJ1937");
        esquema.setVersion("v1");
        esquema.setModulo("TRI");

        campoPeriodo = new Campo();
        campoPeriodo.setNombre("Periodo");
        campoPeriodo.setTipoDato("DATE_FILTER");
        campoPeriodo.setComun(true);
        campoPeriodo.setOrdenCargaExterna(1);

        campoRut = new Campo();
        campoRut.setNombre("RutCliente");
        campoRut.setTipoDato("RUT_CLIENTE");
        campoRut.setClave(true);
        campoRut.setOrdenCargaExterna(2);

        campoMonto = new Campo();
        campoMonto.setNombre("Monto");
        campoMonto.setTipoDato("AMOUNT");
        campoMonto.setOrdenCargaExterna(3);

        esquema.setCampos(List.of(campoPeriodo, campoRut, campoMonto));
    }

    // =========================================================================
    // B-NOR-ESQ-01
    // =========================================================================
    @Test
    @DisplayName("B-NOR-ESQ-01: buscarCampoPorNombre encuentra campo existente")
    void buscarCampoPorNombre_existente_retornaCampo() {
        Campo found = esquema.buscarCampoPorNombre("Monto");
        assertThat(found.getNombre()).isEqualTo("Monto");
    }

    @Test
    @DisplayName("B-NOR-ESQ-01: buscarCampoPorNombre lanza EsquemaException si no existe")
    void buscarCampoPorNombre_inexistente_lanzaException() {
        assertThatThrownBy(() -> esquema.buscarCampoPorNombre("Inexistente"))
                .isInstanceOf(EsquemaException.class)
                .hasMessageContaining("Inexistente");
    }

    // =========================================================================
    // B-NOR-ESQ-02
    // =========================================================================
    @Test
    @DisplayName("B-NOR-ESQ-02: buscarCampoPorCodigoSII lanza EsquemaException si no existe")
    void buscarCampoPorCodigoSII_inexistente_lanzaException() {
        assertThatThrownBy(() -> esquema.buscarCampoPorCodigoSII("CODIGO_X"))
                .isInstanceOf(EsquemaException.class);
    }

    // =========================================================================
    // B-NOR-ESQ-03
    // =========================================================================
    @Test
    @DisplayName("B-NOR-ESQ-03: getCamposClaves retorna solo clave=true")
    void getCamposClaves_retornaSoloCamposClave() {
        var claves = esquema.getCamposClaves();
        assertThat(claves).hasSize(1);
        assertThat(claves.get(0).getNombre()).isEqualTo("RutCliente");
    }

    // =========================================================================
    // B-NOR-ESQ-04
    // =========================================================================
    @Test
    @DisplayName("B-NOR-ESQ-04: getCamposComunes retorna solo comun=true")
    void getCamposComunes_retornaSoloCamposComunes() {
        var comunes = esquema.getCamposComunes();
        assertThat(comunes).hasSize(1);
        assertThat(comunes.get(0).getNombre()).isEqualTo("Periodo");
    }

    // =========================================================================
    // B-NOR-ESQ-05
    // =========================================================================
    @Test
    @DisplayName("B-NOR-ESQ-05: getNombreCampoFecha retorna campo DATE_FILTER")
    void getNombreCampoFecha_retornaCampoPeriodo() {
        assertThat(esquema.getNombreCampoFecha()).isEqualTo("Periodo");
    }

    // =========================================================================
    // B-NOR-ESQ-06
    // =========================================================================
    @Test
    @DisplayName("B-NOR-ESQ-06: getNombreCampoRutCliente retorna campo RUT_CLIENTE")
    void getNombreCampoRutCliente_retornaCampoRut() {
        assertThat(esquema.getNombreCampoRutCliente()).isEqualTo("RutCliente");
    }

    // =========================================================================
    // B-NOR-ESQ-07
    // =========================================================================
    @Test
    @DisplayName("B-NOR-ESQ-07: buscarCampoCargaExterna por posicion")
    void buscarCampoCargaExterna_posicion2_retornaRut() {
        Campo found = esquema.buscarCampoCargaExterna(2);
        assertThat(found).isNotNull();
        assertThat(found.getNombre()).isEqualTo("RutCliente");
    }

    @Test
    @DisplayName("B-NOR-ESQ-07: buscarCampoCargaExterna posicion inexistente retorna null")
    void buscarCampoCargaExterna_posicionInexistente_retornaNull() {
        assertThat(esquema.buscarCampoCargaExterna(99)).isNull();
    }

    // =========================================================================
    // B-NOR-ESQ-08
    // =========================================================================
    @Test
    @DisplayName("B-NOR-ESQ-08: getNombreTablaOrigen con formato correcto")
    void getNombreTablaOrigen_formatoCorrecto() {
        String origen = esquema.getNombreTablaOrigen();
        assertThat(origen).isEqualTo("BSCL.Normativa.BD.Vistas::TRI.DJ1937.Origen");
    }

    @Test
    @DisplayName("B-NOR-ESQ-08: getNombreTablaDestino con formato correcto")
    void getNombreTablaDestino_formatoCorrecto() {
        String destino = esquema.getNombreTablaDestino();
        assertThat(destino).isEqualTo("BSCL.Normativa.BD::TRI.DatosDJ1937");
    }

    // =========================================================================
    // getNombresCamposClave
    // =========================================================================
    @Test
    @DisplayName("getNombresCamposClave retorna lista de nombres")
    void getNombresCamposClave_retornaListaNombres() {
        List<String> nombres = esquema.getNombresCamposClave();
        assertThat(nombres).containsExactly("RutCliente");
    }
}
