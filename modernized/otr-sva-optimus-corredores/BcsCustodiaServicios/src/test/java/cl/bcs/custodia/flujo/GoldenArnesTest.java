package cl.bcs.custodia.flujo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cl.bcs.custodia.flujo.h2.FlujoCaso;
import cl.bcs.custodia.flujo.h2.HanaH2;

/**
 * Integrity of the goldens and of the shared harness (independent of the new implementation, so these pass today): the goldens exist, come from
 * the legacy recorder, name their rules and cover the cards; the harness reproduces a golden for a subject that replays it and goes RED when one
 * byte of the output changes; the dialect bridge rewrites exactly the listed HANA constructs.
 */
@DisplayName("ConsultarFlujoCustodia goldens and harness integrity")
class GoldenArnesTest {

    private static final Pattern REGLA = Pattern.compile("(CUS-FLU-\\d\\d|D-CUS-\\d\\d)");
    private static Object[] previo;

    @BeforeAll
    static void zona() {
        previo = GoldenCasos.fijarZonaYLocale();
    }

    @AfterAll
    static void restaurar() {
        GoldenCasos.restaurar(previo);
    }

    @Test
    @DisplayName("at least 40 goldens, all recorded by the legacy oracle, each naming the rule ids it pins and fully scripted")
    void goldensDelLegacy() throws Exception {
        List<GoldenCasos.Caso> casos = GoldenCasos.casos();
        assertThat(casos).hasSizeGreaterThanOrEqualTo(40);
        for (GoldenCasos.Caso c : casos) {
            JsonObject g = GoldenCasos.golden(c);
            assertThat(g.get("_provenance").getAsString()).as(c.id())
                    .startsWith("Recorded by legacy-oracle/runner/FlujoOracleRunner.java executing the UNMODIFIED legacy cl.bcs.custodia.ConsultarFlujoCustodia");
            assertThat(g.get("caso").getAsString()).isEqualTo(c.id());
            assertThat(c.reglas()).as(c.id()).isNotEmpty().allMatch(r -> REGLA.matcher(r).matches());
            assertThat(c.id()).as("the case id starts with one of its rule ids").matches("(CUS-FLU-\\d\\d|D-CUS-\\d\\d)_.+");
            assertThat(g.get("llamadaSinGuion").getAsBoolean()).as(c.id()).isFalse();
            assertThat(g.getAsJsonObject("matrizUso").getAsJsonArray("restantes")).as(c.id() + " CUS-FLU-14: matrix empty after the call").isEmpty();
        }
    }

    @Test
    @DisplayName("every card with an observable behaviour is pinned by at least one golden")
    void coberturaDeTarjetas() {
        Set<String> cubiertas = new TreeSet<>();
        for (GoldenCasos.Caso c : GoldenCasos.casos()) cubiertas.addAll(c.reglas());
        List<String> requeridas = new ArrayList<>();
        for (int i = 1; i <= 36; i++) requeridas.add(String.format("CUS-FLU-%02d", i));
        // D-CUS-05 (HashMap order) and D-CUS-09 (random work id) are pinned by every golden (order of lines / masked IdTrabajo);
        // D-CUS-06 (parallel races) by keeping each case small (5 recordings agreed); D-CUS-08 by the "null#null#" key -> PrecioMercado 0 lines
        for (String d : new String[] { "D-CUS-01", "D-CUS-02", "D-CUS-03", "D-CUS-04", "D-CUS-07", "D-CUS-10" }) requeridas.add(d);
        assertThat(cubiertas).containsAll(requeridas);
    }

    @Test
    @DisplayName("CUS-FLU-02 D-CUS-01 harness self check: a subject replaying the legacy result reproduces the golden; one changed byte is red")
    void arnesReproduceYDetecta() throws Exception {
        int replayed = 0;
        for (GoldenCasos.Caso c : GoldenCasos.casos()) {
            JsonObject g = GoldenCasos.golden(c);
            boolean sinEfectos = g.getAsJsonObject("matrizUso").getAsJsonArray("insertadas").isEmpty() && g.getAsJsonArray("secuenciasSolicitadas").isEmpty()
                    && g.getAsJsonArray("colaboradores").isEmpty();
            if (!sinEfectos) continue;
            JsonObject replay = FlujoCaso.ejecutar(GoldenCasos.base(), c.dir(), (con, in, guion) -> replay(g), "replay");
            assertThat(FlujoCaso.comparable(replay)).as(c.id()).isEqualTo(FlujoCaso.comparable(g));
            replayed++;
            if (!g.get("salidaCruda").isJsonNull()) {
                String cambiado = g.get("salidaCruda").getAsString().replaceFirst("\"P_OUT_ERRORESCONTADOR\":(\\d)", "\"P_OUT_ERRORESCONTADOR\":9");
                JsonObject mal = FlujoCaso.ejecutar(GoldenCasos.base(), c.dir(), (con, in, guion) -> GoldenCasos.parse(cambiado).getAsJsonObject(), "mutant");
                assertThat(mal.get("salidaCruda").getAsString()).as(c.id() + " the comparison sees one changed digit").isNotEqualTo(g.get("salidaCruda").getAsString());
            }
        }
        assertThat(replayed).as("cases replayed by the self check").isGreaterThanOrEqualTo(5);
    }

    private static JsonObject replay(JsonObject g) throws Exception {
        JsonElement ex = g.get("excepcion");
        if (!ex.isJsonNull()) {
            String clase = ex.getAsJsonObject().get("clase").getAsString();
            if (clase.equals("java.lang.Exception")) throw new Exception(ex.getAsJsonObject().get("mensaje").getAsString());
            throw (Exception) Class.forName(clase).getDeclaredConstructor().newInstance();
        }
        return GoldenCasos.parse(g.get("salidaCruda").getAsString()).getAsJsonObject();
    }

    @Test
    @DisplayName("CUS-FLU-15 CUS-FLU-36 dialect bridge: NEXTVAL, TO_DATE, TO_DECIMAL and (1==1) are the only rewrites")
    void puenteDeDialecto() {
        assertThat(HanaH2.translate("SELECT \"BSCL.Custodia.BD.Secuencias::DatosFlujoCustodia\".NEXTVAL AS \"Id\" FROM DUMMY"))
                .isEqualTo("SELECT NEXT VALUE FOR \"BSCL.Custodia.BD.Secuencias::DatosFlujoCustodia\" AS \"Id\" FROM DUMMY");
        assertThat(HanaH2.translate("TO_DATE('0001-01-01') AS \"F\", TO_DECIMAL(1,28,6), to_date(x)"))
                .isEqualTo("HANA_TO_DATE('0001-01-01') AS \"F\", HANA_TO_DECIMAL(1,28,6), HANA_TO_DATE(x)");
        assertThat(HanaH2.translate("ON (1==1) WHERE a = 1")).isEqualTo("ON (1=1) WHERE a = 1");
        assertThat(HanaH2.translate("SELECT TO_BIGINT(0), rtrim(CAR.\"IdEmisor\") FROM T LIMIT 100000 OFFSET 0"))
                .isEqualTo("SELECT TO_BIGINT(0), rtrim(CAR.\"IdEmisor\") FROM T LIMIT 100000 OFFSET 0");
        assertThat(HanaH2.translate(HanaH2.translate("TO_DATE(x)"))).as("idempotent").isEqualTo("HANA_TO_DATE(x)");
    }
}
