package cl.bcs.canalagente.caracterizacion;

import static cl.bcs.canalagente.caracterizacion.VolcadoAyuda.PROC;
import static cl.bcs.canalagente.caracterizacion.VolcadoAyuda.excepcion;
import static cl.bcs.canalagente.caracterizacion.VolcadoAyuda.sentencias;
import static cl.bcs.canalagente.caracterizacion.VolcadoAyuda.tipos;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;

/**
 * The gate of BcsCanalAgenteServicios (no legacy golden: the legacy has no gates, it always calls the cartola). ConsultarCartolaDeMovimiento is NOT part of this module: it sits behind the port
 * ConsultarCartolaDeMovimientoPort, whose default implementation (ColaboradoresNoPortados) is closed, and the gate G-CTA (property canalagente.compuertas.cartola, false by default) is closed.
 * A closed gate raises ServicioNoServidoException BEFORE the first SQL and before any collaborator call; with the gate open and the port not ported the failure comes at the cartola call, after
 * the client lookup and before the first write.
 */
class CanalCompuertaTest {

    private static final String SNS = "ServicioNoServidoException";
    private static final String CASO = "un-movimiento-con-comprobante-completo";

    private static JsonObject sinEfectos(JsonObject d, String id) {
        assertEquals(SNS, excepcion(d), id);
        assertEquals(d.getAsJsonObject("tablasIniciales").toString(), d.getAsJsonObject("tablas").toString(), id + ": nothing may change");
        return d;
    }

    @Test
    @DisplayName("G-CTA: con la compuerta cerrada no se envia ningun SQL ni se llama a la cartola")
    void compuerta_cerrada_no_hace_nada() throws Exception {
        String id = CasoCanal.id("CAN-LOG-", CASO);
        JsonObject d = sinEfectos(CasoCanal.ejecutar(id, c -> c.conCompuerta(false)), id);
        assertEquals(0, sentencias(d));
        assertEquals(0, d.getAsJsonArray("colaboradores").size());
    }

    @Test
    @DisplayName("G-CTA: la compuerta cerrada tambien corta el camino de cliente no encontrado y el de JSON (antes de cualquier SQL)")
    void compuerta_cerrada_corta_todos_los_caminos() throws Exception {
        for (String f : new String[] { "portafolio-inexistente-lanza-nullpointer", "json-completo-devuelve-el-contrato", "numeros-como-texto-se-aceptan", "falla-el-select-del-cliente" }) {
            String id = CasoCanal.id("CAN-", f);
            JsonObject d = sinEfectos(CasoCanal.ejecutar(id, c -> c.conCompuerta(false)), id);
            assertEquals(0, sentencias(d), id);
        }
    }

    @Test
    @DisplayName("G-CTA: por defecto (sin propiedades) la compuerta nace cerrada")
    void compuerta_por_defecto_cerrada() throws Exception {
        Object porDefecto = ApiCanalAgente.compuertasSpring(Map.of());
        String id = CasoCanal.id("CAN-LOG-", CASO);
        JsonObject d = sinEfectos(CasoCanal.ejecutar(id, c -> c.conCompuertasObjeto(porDefecto)), id);
        assertEquals(0, sentencias(d));
    }

    @Test
    @DisplayName("G-CTA: canalagente.compuertas.cartola=false la cierra y =true la abre (equivale al legacy)")
    void compuerta_por_propiedad() throws Exception {
        String id = CasoCanal.id("CAN-LOG-", CASO);
        sinEfectos(CasoCanal.ejecutar(id, c -> c.conCompuertasObjeto(ApiCanalAgente.compuertasSpring(Map.of("canalagente.compuertas.cartola", "false")))), id);
        Object abierta = ApiCanalAgente.compuertasSpring(Map.of("canalagente.compuertas.cartola", "true"));
        CasoCanal.comparar(id, CasoCanal.golden(id), CasoCanal.ejecutar(id, c -> c.conCompuertasObjeto(abierta)));
    }

    @Test
    @DisplayName("G-CTA: con la compuerta abierta se comporta como el legacy, tambien en los casos con excepcion")
    void compuerta_abierta_equivale_al_legacy() throws Exception {
        for (String f : new String[] { "portafolio-inexistente-lanza-nullpointer", "falla-el-select-de-comprobantes", "cartola-lanza-error-se-propaga-sin-tocar", "cartola-con-lista-nula", "numeros-como-texto-se-aceptan",
                "entrada-completa-se-delega" }) {
            String id = CasoCanal.id("CAN-", f.equals("entrada-completa-se-delega") ? "la-entrada-completa-se-delega" : f);
            CasoCanal.comparar(id, CasoCanal.golden(id), CasoCanal.ejecutar(id, c -> c.conCompuerta(true)));
        }
    }

    @Test
    @DisplayName("Colaborador no portado: con la compuerta abierta y el puerto por defecto falla tras buscar el cliente y antes de escribir")
    void puerto_por_defecto_cerrado() throws Exception {
        String id = CasoCanal.id("CAN-LOG-", CASO);
        JsonObject d = sinEfectos(CasoCanal.ejecutar(id, c -> c.conCompuerta(true).conColaboradoresNoPortados()), id);
        assertEquals(List.of("query"), tipos(d), "only the client lookup runs before the not ported collaborator");
        assertEquals(0, d.getAsJsonArray("colaboradores").size());
        assertEquals(0, d.getAsJsonObject("tablas").getAsJsonArray(PROC).size());
    }

    @Test
    @DisplayName("Colaborador no portado: ColaboradoresNoPortados.consultar lanza ServicioNoServidoException y no devuelve nada")
    void el_puerto_cerrado_lanza() {
        Object puerto = ApiCanalAgente.construir(ApiCanalAgente.P + "ColaboradoresNoPortados");
        Throwable t = assertThrows(Throwable.class, () -> ApiCanalAgente.invocar(puerto, "consultar", null, new JsonObject()));
        assertEquals(SNS, t.getClass().getSimpleName());
        assertTrue(t instanceof RuntimeException);
    }

    @Test
    @DisplayName("La excepcion de compuerta no se envuelve ni se traga: es una RuntimeException propia del modulo")
    void la_excepcion_es_propia() {
        Class<?> c = ApiCanalAgente.clase(ApiCanalAgente.K + "ServicioNoServidoException");
        assertTrue(RuntimeException.class.isAssignableFrom(c));
        assertEquals("cl.bcs.canalagente.kit", c.getPackageName());
    }
}
