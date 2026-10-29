package cl.bcs.apv.jobs.job;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import cl.bcs.apv.jobs.support.Canon;
import cl.bcs.apv.jobs.support.CasoJob;
import cl.bcs.apv.jobs.support.EjecucionJob.Corrida;
import cl.bcs.apv.jobs.support.VerificaJob;
import cl.bcs.apv.jobs.support.ZonaUtc;

/**
 * Proves the HARNESS, with no job involved: the comparator accepts the golden itself and rejects a wrong outcome, a wrong table, a wrong collaborator trace and
 * a wrong file; no golden holds the day or the second it was recorded; no golden or case carries a credential.
 */
@ExtendWith(ZonaUtc.class)
class HarnessSelfCheckTest {

    static final String CASO = "APVJ-CMP-001_dos-mensajes-escribe-log-y-ruta-salida";

    private static Corrida delGolden(CasoJob caso, boolean marca) {
        JsonObject e = caso.esperado();
        List<JsonObject> traza = new ArrayList<>();
        e.getAsJsonArray("colaboradores").forEach(x -> traza.add(x.getAsJsonObject().deepCopy()));
        return new Corrida(e.getAsJsonObject("resultado").deepCopy(), e.getAsJsonObject("tablas").deepCopy(), traza, e.getAsJsonObject("archivos").deepCopy(), marca);
    }

    private static Corrida delGolden(CasoJob caso) {
        return delGolden(caso, true);
    }

    @Test
    @DisplayName("harness: the golden compared with itself is equal")
    void elGoldenSeIgualaASiMismo() {
        CasoJob caso = CasoJob.cargar(CASO);
        VerificaJob.comparar(caso, delGolden(caso));
    }

    @Test
    @DisplayName("harness: a wrong outcome, table, collaborator trace or file is rejected")
    void rechazaCadaDiferencia() {
        CasoJob caso = CasoJob.cargar(CASO);
        Corrida c = delGolden(caso);
        c.resultado().addProperty("desenlace", "ROLLBACK");
        assertThatThrownBy(() -> VerificaJob.comparar(caso, c)).hasMessageContaining("resultado");
        Corrida t = delGolden(caso);
        t.tablas().getAsJsonArray("BSCL.Plataforma.BD::BAT.Programa").get(0).getAsJsonObject().addProperty("RutaSalida", "otra");
        assertThatThrownBy(() -> VerificaJob.comparar(caso, t)).hasMessageContaining("tablas");
        Corrida k = delGolden(caso);
        k.colaboradores().get(0).getAsJsonObject("entrada").addProperty("p_IdUsuario", 99);
        assertThatThrownBy(() -> VerificaJob.comparar(caso, k)).hasMessageContaining("colaboradores");
        Corrida a = delGolden(caso);
        a.archivos().addProperty(a.archivos().keySet().iterator().next(), "otro contenido");
        assertThatThrownBy(() -> VerificaJob.comparar(caso, a)).hasMessageContaining("archivos");
        assertThatThrownBy(() -> VerificaJob.comparar(caso, delGolden(caso, false))).hasMessageContaining("stamp");
    }

    @Test
    @DisplayName("harness: an extra collaborator call is rejected")
    void unaLlamadaDeMas() {
        CasoJob caso = CasoJob.cargar(CASO);
        Corrida c = delGolden(caso);
        c.colaboradores().add(c.colaboradores().get(0).deepCopy());
        assertThatThrownBy(() -> VerificaJob.comparar(caso, c)).hasMessageContaining("colaboradores");
    }

    @Test
    @DisplayName("harness: no golden holds a 14-digit run stamp, and no case or golden carries a credential")
    void sinFechaDeGrabacionNiSecretos() throws Exception {
        Pattern marca = Pattern.compile("(?<!\\d)20\\d{12}(?!\\d)");
        Pattern secreto = Pattern.compile("(?i)(password|passwd|pwd|secret|token)\"?\\s*[:=]\\s*\"[^\"<]+\"");
        for (String n : CasoJob.todos()) {
            Path d = CasoJob.recurso(CasoJob.RAIZ + "/cases/" + n);
            for (String f : List.of("case.json", "expected.json")) {
                String t = Files.readString(d.resolve(f));
                assertThat(marca.matcher(t).find()).as(n + "/" + f + " holds a run stamp").isFalse();
                assertThat(secreto.matcher(t).find()).as(n + "/" + f + " holds a credential").isFalse();
            }
        }
    }

    @Test
    @DisplayName("harness: arrays keep their order, nulls are dropped, numbers compare by value")
    void canon() {
        JsonArray a = new JsonArray();
        a.add(1);
        a.add(2);
        JsonArray b = new JsonArray();
        b.add(2);
        b.add(1);
        assertThat(Canon.texto(a)).isNotEqualTo(Canon.texto(b));
        JsonObject o1 = new JsonObject();
        o1.addProperty("x", new java.math.BigDecimal("1.50"));
        JsonObject o2 = new JsonObject();
        o2.addProperty("x", new java.math.BigDecimal("1.5"));
        o2.add("y", com.google.gson.JsonNull.INSTANCE);
        assertThat(Canon.texto(o1)).isEqualTo(Canon.texto(o2));
    }
}
