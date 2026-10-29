package cl.bcs.custodia.service.evc;

import cl.bcs.custodia.service.JavaCallService;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Migrated stub for legacy cl.bcs.custodia.evc.SuscripcionEmisionPago (JavaCall).
 * DEFERRED: cross-service dependencies: cl.bcs.ordenes.NotificarEstadoOrden, cl.bcs.ordenes.CrearOrdenes.InformacionOrden, cl.bcs.ordenes.CrearOrdenes.OrdenProceso, cl.bcs.ordenes.CrearOrdenes.RequisitoOperativoCrearOrden, cl.bcs.ordenes.NotificarEstadoOrden.NotificarEstadoOrden_Entrada, cl.bcs.ordenes.RefacturarMovimiento.RefacturarMovimiento_Entrada, cl.bcs.plataforma.mer.CalcularParidad, cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada, cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida, cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada, cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida, cl.bcs.plataforma.pla.AgregarLog, cl.bcs.plataforma.pla.SaldoFlujoCaja, cl.bcs.plataforma.pla.SaldoFlujoCaja.ObjFLUJOCAJA, cl.bcs.plataforma.pla.SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR.
 * TODO: implement after cross-service HTTP clients are available.
 */
@Service
public class SuscripcionEmisionPagoService implements JavaCallService {
    private static final Logger log = LoggerFactory.getLogger(SuscripcionEmisionPagoService.class);

    @Override
    public String getFqn() {
        return "cl.bcs.custodia.evc.SuscripcionEmisionPago";
    }

    @Override
    public JsonObject execute(JsonObject params) {
        log.warn("SuscripcionEmisionPagoService.execute called but class is deferred - returning 503");
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
            "cl.bcs.custodia.evc.SuscripcionEmisionPago is deferred: cl.bcs.ordenes.NotificarEstadoOrden, cl.bcs.ordenes.CrearOrdenes.InformacionOrden, cl.bcs.ordenes.CrearOrdenes.OrdenProceso, cl.bcs.ordenes.CrearOrdenes.RequisitoOperativoCrearOrden, cl.bcs.ordenes.NotificarEstadoOrden.NotificarEstadoOrden_Entrada, cl.bcs.ordenes.RefacturarMovimiento.RefacturarMovimiento_Entrada, cl.bcs.plataforma.mer.CalcularParidad, cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada, cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida, cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada, cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida, cl.bcs.plataforma.pla.AgregarLog, cl.bcs.plataforma.pla.SaldoFlujoCaja, cl.bcs.plataforma.pla.SaldoFlujoCaja.ObjFLUJOCAJA, cl.bcs.plataforma.pla.SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR");
    }
}
