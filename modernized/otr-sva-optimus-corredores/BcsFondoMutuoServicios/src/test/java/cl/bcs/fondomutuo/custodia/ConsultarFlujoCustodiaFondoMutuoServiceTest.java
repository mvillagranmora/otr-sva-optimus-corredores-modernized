package cl.bcs.fondomutuo.custodia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.cierre3b.Caso3B;
import cl.bcs.fondomutuo.cierre3b.PuertosGuionados;
import cl.bcs.fondomutuo.kit.Compuertas;
import cl.bcs.fondomutuo.kit.JsonLegacy;
import cl.bcs.fondomutuo.kit.ServicioNoServidoException;
import cl.bcs.fondomutuo.requisitos.contrato.CuotaContrato;

/** FM-REQ-24 / FM-REQ-25 / D-REQ-9 / D-REQ-10: ConsultarFlujoCustodiaFondoMutuo against the recorded legacy, plus the G-CUS gate. */
class ConsultarFlujoCustodiaFondoMutuoServiceTest {

    static final Compuertas ABIERTA = new Compuertas("", false, false, false, false, true);
    static final Compuertas CERRADA = new Compuertas("", false, false, false, false, false);

    static List<String> casos() {
        return Caso3B.casos("ConsultarFlujoCustodiaFondoMutuo");
    }

    static JsonObject sujeto(Caso3B.Contexto ctx, JsonObject entrada) throws Exception {
        ConsultarFlujoCustodiaFondoMutuoService.Entrada in = JsonLegacy.plano().fromJson(entrada.toString(),
                ConsultarFlujoCustodiaFondoMutuoService.Entrada.class);
        ConsultarFlujoCustodiaFondoMutuoService s = new ConsultarFlujoCustodiaFondoMutuoService(new FlujoCustodiaFmJdbc(ctx.conexion),
                e -> ctx.guion.responder("ConsultarFlujoCustodia", e), PuertosGuionados.cuotas(ctx.guion), ctx.calendario, ABIERTA);
        return JsonParser.parseString(JsonLegacy.plano().toJson(s.logica(in))).getAsJsonObject();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("casos")
    @DisplayName("FM-REQ-24 equivalence with the recorded legacy")
    void equivalencia(String idCaso) throws Exception {
        Caso3B.verificar(idCaso, ConsultarFlujoCustodiaFondoMutuoServiceTest::sujeto);
    }

    @Test
    @DisplayName("G-CUS closed (default): 503 exception before the custody call, nothing else is read after it")
    void compuertaCerradaNoLlamaACustodia() {
        int[] llamadas = { 0 };
        ConsultarFlujoCustodiaFondoMutuoService s = new ConsultarFlujoCustodiaFondoMutuoService(null, e -> {
            llamadas[0]++;
            return new JsonObject();
        }, (CuotaContrato.Puerto) e -> new CuotaContrato.Salida(), null, CERRADA);
        ConsultarFlujoCustodiaFondoMutuoService.Entrada in = new ConsultarFlujoCustodiaFondoMutuoService.Entrada();
        ServicioNoServidoException e = assertThrows(ServicioNoServidoException.class, () -> s.logica(in));
        assertEquals("G-CUS", e.compuerta());
        assertEquals(0, llamadas[0]);
    }
}
