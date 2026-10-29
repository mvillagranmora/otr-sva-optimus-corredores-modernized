package cl.bcs.tesoreria.familia_b;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import cl.bcs.tesoreria.familia_b.support.Caso;
import cl.bcs.tesoreria.familia_b.support.Equivalencia;
import cl.bcs.tesoreria.familia_b.support.ZonaUtc;
import cl.bcs.tesoreria.javacall.CrearCargaMasivaPacService;

/**
 * Characterization of legacy {@code cl.bcs.tesoreria.CrearCargaMasivaPac} (entry point {@code call(conn, json)}) as {@code CrearCargaMasivaPacService#execute(JsonObject)}:
 * loads the PAC "universe" file: deactivates the previous universe and every active mandate, inserts the new universe and reactivates the mandates it lists
 * (state ING with an active SAAPV/CSC contract). Any exception is swallowed into an 'Error' message.
 * 
 * <p>Goldens: {@code src/test/resources/tesoreria-b/cases/TES-B-214..220} (output, CAR.CargaMasivaDetallePAC, PAC.MandatoPAC states).
 */
@ExtendWith(ZonaUtc.class)
class CrearCargaMasivaPacServiceTest {

    static final List<String> CASOS = List.of(
            "TES-B-214_carga-universo-reemplaza-universo-y-reactiva-mandatos-validos",
            "TES-B-215_carga-de-otro-tipo-no-hace-nada-y-responde-exito",
            "TES-B-216_sin-cargas-responde-exito-sin-tocar-datos",
            "TES-B-217_universo-vacio-desactiva-todo-y-no-reactiva",
            "TES-B-218_detalle-nulo-responde-error-pero-conserva-lo-ya-confirmado",
            "TES-B-219_identificador-nulo-responde-error-del-sistema",
            "TES-B-220_tipo-de-carga-nulo-responde-error-del-sistema");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();

    private static void verificar(String nombre) {
        Equivalencia.verificar(EJECUTADOS, nombre, caso -> new CrearCargaMasivaPacService(caso.jdbc()).execute(caso.entrada().deepCopy()));
    }

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("CrearCargaMasivaPac");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    // CrearCargaMasivaPac.java:146-199 + sql/CrearCargaMasivaPac_UpdateUniversoPac.sql: universe load: old UNI rows INA, ids taken from the sequence, spaces removed from IdentServicio, valid mandates reactivated
    @Test
    void tesB214_cargaUniversoReemplazaUniversoYReactivaMandatosValidos() {
        verificar(CASOS.get(0));
    }

    // CrearCargaMasivaPac.java:146: other load type: success message and nothing changes
    @Test
    void tesB215_cargaDeOtroTipoNoHaceNadaYRespondeExito() {
        verificar(CASOS.get(1));
    }

    // CrearCargaMasivaPac.java:141-199: no load entries: success message
    @Test
    void tesB216_sinCargasRespondeExitoSinTocarDatos() {
        verificar(CASOS.get(2));
    }

    // CrearCargaMasivaPac.java:154-157,175-190: empty universe: old rows INA and every active mandate INA, nothing reactivated
    @Test
    void tesB217_universoVacioDesactivaTodoYNoReactiva() {
        verificar(CASOS.get(3));
    }

    // CrearCargaMasivaPac.java:157-159,202-206: null detail list: error message (counter 1) but the deactivations before the explicit commit survive
    @Test
    void tesB218_detalleNuloRespondeErrorPeroConservaLoYaConfirmado() {
        verificar(CASOS.get(4));
    }

    // CrearCargaMasivaPac.java:168,202-206: a null IdentServicio: NullPointerException turned into the 'Error' message
    @Test
    void tesB219_identificadorNuloRespondeErrorDelSistema() {
        verificar(CASOS.get(5));
    }

    // CrearCargaMasivaPac.java:146,202-206: a null load type: same error message
    @Test
    void tesB220_tipoDeCargaNuloRespondeErrorDelSistema() {
        verificar(CASOS.get(6));
    }

}
