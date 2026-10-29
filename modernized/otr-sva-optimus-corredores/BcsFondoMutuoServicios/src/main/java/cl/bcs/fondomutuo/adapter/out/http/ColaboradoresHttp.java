package cl.bcs.fondomutuo.adapter.out.http;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.port.out.http.BatchPort;
import cl.bcs.fondomutuo.port.out.http.CustodiaPort;
import cl.bcs.fondomutuo.port.out.http.FlujoCajaPort;
import cl.bcs.fondomutuo.port.out.http.LogPort;
import cl.bcs.fondomutuo.port.out.http.NotificacionOwpPort;
import cl.bcs.fondomutuo.port.out.http.ParidadPort;
import cl.bcs.fondomutuo.port.out.http.PreciosTasasPort;
import cl.bcs.fondomutuo.port.out.http.ProductosPortafolioPort;

/**
 * The H ports (collaborators owned by other modules) over {@link RouterClient}: each call sends the legacy JSON of the collaborator to the
 * router under its LEGACY fully qualified class name (the router maps the package prefix to the owner or falls back to TomEE). No real call is made
 * by this step: the services that use the ports arrive in steps 3-4; tests use a simulated server.
 */
@Component
public class ColaboradoresHttp implements ParidadPort, PreciosTasasPort, FlujoCajaPort, CustodiaPort, LogPort, BatchPort, ProductosPortafolioPort,
        NotificacionOwpPort {

    public static final String CALCULAR_PARIDAD = "cl.bcs.plataforma.mer.CalcularParidad";
    public static final String ENTREGAR_PRECIOS_TASAS = "cl.bcs.plataforma.tas.EntregarPreciosTasas";
    public static final String SALDO_FLUJO_CAJA = "cl.bcs.plataforma.pla.SaldoFlujoCaja";
    public static final String CONSULTAR_FLUJO_CUSTODIA = "cl.bcs.custodia.ConsultarFlujoCustodia";
    public static final String AGREGAR_LOG = "cl.bcs.plataforma.pla.AgregarLog";
    public static final String INSCRIBIR_PROCESO_BATCH = "cl.bcs.plataforma.bat.InscribirProcesoBatch";
    public static final String CONSULTAR_PRODUCTOS_PORTAFOLIO = "cl.bcs.plataforma.com.ConsultarProductosPortafolio";
    public static final String NOTIFICAR_ESTADO_ORDENES_OWP = "cl.bcs.plataforma.pla.NotificarEstadoOrdenesOWP";

    /** Port -> legacy class, in one place (a test pins it). */
    public static final Map<String, String> CLASES = Map.of(
            "ParidadPort", CALCULAR_PARIDAD, "PreciosTasasPort", ENTREGAR_PRECIOS_TASAS, "FlujoCajaPort", SALDO_FLUJO_CAJA,
            "CustodiaPort", CONSULTAR_FLUJO_CUSTODIA, "LogPort", AGREGAR_LOG, "BatchPort", INSCRIBIR_PROCESO_BATCH,
            "ProductosPortafolioPort", CONSULTAR_PRODUCTOS_PORTAFOLIO, "NotificacionOwpPort", NOTIFICAR_ESTADO_ORDENES_OWP);

    private final RouterClient router;

    public ColaboradoresHttp(RouterClient router) {
        this.router = router;
    }

    @Override
    public JsonObject calcular(JsonObject entrada) {
        return router.invocar(CALCULAR_PARIDAD, entrada);
    }

    @Override
    public JsonObject entregar(JsonObject entrada) {
        return router.invocar(ENTREGAR_PRECIOS_TASAS, entrada);
    }

    @Override
    public JsonObject saldo(JsonObject entrada) {
        return router.invocar(SALDO_FLUJO_CAJA, entrada);
    }

    @Override
    public JsonObject consultarFlujo(JsonObject entrada) {
        return router.invocar(CONSULTAR_FLUJO_CUSTODIA, entrada);
    }

    @Override
    public JsonObject agregar(JsonObject entrada) {
        return router.invocar(AGREGAR_LOG, entrada);
    }

    @Override
    public JsonObject inscribir(JsonObject entrada) {
        return router.invocar(INSCRIBIR_PROCESO_BATCH, entrada);
    }

    @Override
    public JsonObject consultar(JsonObject entrada) {
        return router.invocar(CONSULTAR_PRODUCTOS_PORTAFOLIO, entrada);
    }

    @Override
    public JsonObject notificar(JsonObject entrada) {
        return router.invocar(NOTIFICAR_ESTADO_ORDENES_OWP, entrada);
    }
}
