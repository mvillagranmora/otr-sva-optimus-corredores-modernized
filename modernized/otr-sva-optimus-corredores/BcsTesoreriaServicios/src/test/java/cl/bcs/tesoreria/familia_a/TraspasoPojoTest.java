package cl.bcs.tesoreria.familia_a;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import cl.bcs.tesoreria.familia_a.support.Canon;
import cl.bcs.tesoreria.familia_a.support.Caso;
import cl.bcs.tesoreria.familia_a.support.ZonaUtc;

/**
 * Characterization of the data holder {@code cl.bcs.tesoreria.pojo.Traspaso} (state of a freshly built instance of each nested class).
 * The modern home is {@code cl.bcs.tesoreria.javacall.pojo.Traspaso}, with the same nested classes and the same default values.
 */
@ExtendWith(ZonaUtc.class)
class TraspasoPojoTest {

    static final List<String> CASOS = List.of(            "TES-A-830_pojo-traspaso-valores-por-defecto-de-las-clases-de-datos");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("pojo.Traspaso");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    @Test
    @DisplayName("TES-A-830 pojo traspaso valores por defecto de las clases de datos")
    void tesA830_pojoTraspasoValoresPorDefectoDeLasClasesDeDatos() throws Exception {
        Caso caso = Caso.cargar(CASOS.get(0));
        Gson gson = new GsonBuilder().serializeNulls().create();
        JsonObject actual = new JsonObject();
        for (var n : caso.entrada().getAsJsonArray("clases")) {
            String intended = n.getAsString().replace("cl.bcs.tesoreria.pojo.Traspaso", "cl.bcs.tesoreria.javacall.pojo.Traspaso");
            var ctor = Class.forName(intended).getDeclaredConstructor();
            ctor.setAccessible(true);
            actual.add(n.getAsString(), gson.toJsonTree(ctor.newInstance()));
        }
        EJECUTADOS.ejecutado(caso.nombre());
        assertThat(Canon.texto(actual)).as(caso.nombre()).isEqualTo(Canon.texto(caso.esperado().get("salida")));
    }
}
