package cl.bcs.fondomutuo.agf;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.cierre3b.Caso3B;
import cl.bcs.fondomutuo.kit.JsonLegacy;

/** FM-NOR-51 / FM-NOR-52: CrearSolicitudAGF against the goldens recorded from the legacy class (cases3b/NOR-51-nn, NOR-52-nn), H2 only. */
class CrearSolicitudAgfServiceTest {

    static List<String> casos() {
        return Caso3B.casos("CrearSolicitudAGF");
    }

    static JsonObject sujeto(Caso3B.Contexto ctx, JsonObject entrada) throws Exception {
        CrearSolicitudAgfService.Entrada in = JsonLegacy.plano().fromJson(entrada.toString(), CrearSolicitudAgfService.Entrada.class);
        CrearSolicitudAgfService s = new CrearSolicitudAgfService(new AgfJdbc(ctx.conexion), ctx.configuracion, ctx.diccionario);
        return JsonParser.parseString(JsonLegacy.plano().toJson(s.logica(in))).getAsJsonObject();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("casos")
    @DisplayName("FM-NOR-51/52 equivalence with the recorded legacy")
    void equivalencia(String idCaso) throws Exception {
        Caso3B.verificar(idCaso, CrearSolicitudAgfServiceTest::sujeto);
    }
}
