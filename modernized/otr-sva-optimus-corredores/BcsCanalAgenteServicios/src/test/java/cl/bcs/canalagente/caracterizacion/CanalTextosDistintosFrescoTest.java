package cl.bcs.canalagente.caracterizacion;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;

/**
 * Verifier round 20261031a. In all the development cases {@code NombreTipoOperacion} and {@code NombreTipoMovimiento} of the cartola row are both "Abono", so crossing them in the
 * mapping changed no byte (a canary survived with the whole suite green). CAN-FRG-039 (fresh input recorded from the real legacy by the verifier: every text field of the row with a
 * DIFFERENT value: TP-producto, TO-operacion, TM-movimiento, TC-comprobante) is copied here as a test resource and run through the module's harness ({@link CasoCanal},
 * {@link ApiCanalAgente}) against its legacy golden, and the four texts are pinned one by one.
 */
class CanalTextosDistintosFrescoTest {

    private static final String ID = "CAN-FRG-039_todos-los-campos-de-texto-con-valores-distintos";

    private static JsonObject corrida() throws Exception {
        return CasoCanal.ejecutar(CanalMensajeParametersTest.recurso(ID, "case.json"), CanalMensajeParametersTest.semilla(ID), null);
    }

    @Test
    @DisplayName("CAN-FRG-039: the unit's whole dump equals the legacy golden")
    void igualAlGoldenDelLegacy() throws Exception {
        CasoCanal.comparar(ID, CanalMensajeParametersTest.recurso(ID, "expected.json"), corrida());
    }

    @Test
    @DisplayName("CAN-FRG-039: NombreTipoOperacion, NombreTipoMovimiento, NombreTipoProducto and NombreTipoComprobante keep each its own value")
    void cadaTextoConservaSuValor() throws Exception {
        JsonObject m = corrida().getAsJsonObject("salida").getAsJsonArray("P_OUT_RESULTADO").get(0).getAsJsonObject();
        assertEquals("TO-operacion", m.get("NombreTipoOperacion").getAsString());
        assertEquals("TM-movimiento", m.get("NombreTipoMovimiento").getAsString());
        assertEquals("TP-producto", m.get("NombreTipoProducto").getAsString());
        assertEquals("TC-comprobante", m.get("NombreTipoComprobante").getAsString());
    }
}
