package cl.bcs.fondomutuo.requisitos;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.cierre3b.Caso3B;
import cl.bcs.fondomutuo.kit.JsonLegacy;

/** FM-REQ-22 / D-REQ-6: ValidaMontoInversion against the goldens recorded from the legacy class (cases3b/REQ-22-nn). */
class ValidaMontoInversionServiceTest {

    static List<String> casos() {
        return Caso3B.casos("ValidaMontoInversion");
    }

    static JsonObject sujeto(Caso3B.Contexto ctx, JsonObject entrada) {
        ValidaMontoInversionService.Entrada in = JsonLegacy.plano().fromJson(entrada.toString(), ValidaMontoInversionService.Entrada.class);
        ValidaMontoInversionService.Salida out = new ValidaMontoInversionService(ctx.configuracion, ctx.diccionario).logica(in);
        return JsonParser.parseString(JsonLegacy.plano().toJson(out)).getAsJsonObject();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("casos")
    @DisplayName("FM-REQ-22 equivalence with the recorded legacy")
    void equivalencia(String idCaso) throws Exception {
        Caso3B.verificar(idCaso, ValidaMontoInversionServiceTest::sujeto);
    }
}
