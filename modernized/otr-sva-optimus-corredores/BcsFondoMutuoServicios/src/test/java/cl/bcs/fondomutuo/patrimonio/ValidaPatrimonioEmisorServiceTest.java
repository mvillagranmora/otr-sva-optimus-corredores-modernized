package cl.bcs.fondomutuo.patrimonio;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.cierre3b.Caso3B;
import cl.bcs.fondomutuo.kit.JsonLegacy;

/** FM-REQ-23 / D-REQ-7: ValidaPatrimonioEmisor against the goldens recorded from the legacy class (cases3b/REQ-23-nn), H2 only. */
class ValidaPatrimonioEmisorServiceTest {

    static List<String> casos() {
        return Caso3B.casos("ValidaPatrimonioEmisor");
    }

    static JsonObject sujeto(Caso3B.Contexto ctx, JsonObject entrada) throws Exception {
        ValidaPatrimonioEmisorService.Entrada in = JsonLegacy.plano().fromJson(entrada.toString(), ValidaPatrimonioEmisorService.Entrada.class);
        ValidaPatrimonioEmisorService servicio = new ValidaPatrimonioEmisorService(new PatrimonioEmisorJdbc(ctx.conexion),
                e -> ctx.guion.responder("CalcularParidad", e), ctx.diccionario);
        return JsonParser.parseString(JsonLegacy.plano().toJson(servicio.logica(in))).getAsJsonObject();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("casos")
    @DisplayName("FM-REQ-23 equivalence with the recorded legacy")
    void equivalencia(String idCaso) throws Exception {
        Caso3B.verificar(idCaso, ValidaPatrimonioEmisorServiceTest::sujeto);
    }
}
