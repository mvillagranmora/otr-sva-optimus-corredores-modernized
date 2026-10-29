package cl.bcs.fondomutuo.requisitos;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.cierre3b.Caso3B;
import cl.bcs.fondomutuo.kit.JsonLegacy;

/** FM-REQ-01..21 and D-REQ-1..4: ServicioCumpleRequisitosCrearSolicitudAporteRescate against the goldens recorded from the legacy class (cases3b/REQ-nn). */
class ServicioCumpleRequisitosServiceTest {

    static List<String> casos() {
        return Caso3B.casos("ServicioCumpleRequisitosCrearSolicitudAporteRescate");
    }

    static JsonObject sujeto(Caso3B.Contexto ctx, JsonObject entrada) throws Exception {
        ServicioCumpleRequisitosService.Entrada in = JsonLegacy.plano().fromJson(entrada.toString(), ServicioCumpleRequisitosService.Entrada.class);
        ServicioCumpleRequisitosService s = new ServicioCumpleRequisitosService(new RequisitosJdbc(ctx.conexion), ctx.diccionario, ctx.configuracion,
                e -> ctx.guion.responder("ConsultarProductosPortafolio", e));
        return JsonParser.parseString(JsonLegacy.plano().toJson(s.logica(in))).getAsJsonObject();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("casos")
    @DisplayName("FM-REQ equivalence with the recorded legacy")
    void equivalencia(String idCaso) throws Exception {
        Caso3B.verificar(idCaso, ServicioCumpleRequisitosServiceTest::sujeto);
    }
}
