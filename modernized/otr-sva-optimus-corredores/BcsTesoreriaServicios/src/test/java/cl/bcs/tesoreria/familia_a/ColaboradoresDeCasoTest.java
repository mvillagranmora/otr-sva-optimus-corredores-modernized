package cl.bcs.tesoreria.familia_a;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;

import cl.bcs.tesoreria.familia_a.support.Caso;
import cl.bcs.tesoreria.familia_a.support.ColaboradoresDeCaso;

/**
 * Pins the contract of the {@code Colaboradores} test double that the family A tests rely on: it traces {servicio, entrada} of every call, answers the
 * scripted collaborators like the legacy oracle fakes, and an unscripted collaborator answers an empty object (the legacy empty Salida).
 */
class ColaboradoresDeCasoTest {

    private static Caso caso() {
        return Caso.cargar("TES-A-20_cie-ingreso-valido-inserta-movimiento-y-forma-de-pago");
    }

    @Test
    void elDobleRegistraServicioYEntradaDeCadaLlamada() {
        ColaboradoresDeCaso c = new ColaboradoresDeCaso(caso());
        JsonObject in = new JsonObject();
        in.addProperty("p_IdEmpresa", 1);
        c.invocar("CrearCargoAbono", in);
        c.invocar("AgregarLog", new JsonObject());
        assertThat(c.traza()).hasSize(2);
        assertThat(c.traza().get(0).get("servicio").getAsString()).isEqualTo("CrearCargoAbono");
        assertThat(c.traza().get(0).getAsJsonObject("entrada").get("p_IdEmpresa").getAsInt()).isEqualTo(1);
    }

    @Test
    void unColaboradorSinGuionContestaVacio() {
        ColaboradoresDeCaso c = new ColaboradoresDeCaso(caso());
        assertThat(c.invocar("ConsultarFlujoCaja", new JsonObject()).entrySet()).isEmpty();
    }

    @Test
    void agregarLogContestaContadorCero() {
        ColaboradoresDeCaso c = new ColaboradoresDeCaso(caso());
        assertThat(c.invocar("AgregarLog", new JsonObject()).get("P_OUT_ERRORESCONTADOR").getAsLong()).isZero();
    }
}
