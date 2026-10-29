package cl.bcs.custodia.service;

import cl.bcs.custodia.service.JavaCallService;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Migrated stub for legacy cl.bcs.custodia.EjecutarVencimiento (JavaCall).
 * DEFERRED: cross-service dependencies: cl.bcs.plataforma.lim.ValidarLimiteCuentaPropiaIFRF, cl.bcs.plataforma.lim.pojo.ConstantesLimites, cl.bcs.plataforma.mer.CalcularParidad, cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada, cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida, cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada, cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida, cl.bcs.cuentaInversion.CrearCargoAbono, cl.bcs.cuentaInversion.CrearCargoAbono.CrearCargoAbonoEntrada, cl.bcs.cuentaInversion.CrearCargoAbono.CrearCargoAbonoSalida, cl.bcs.mesa.MES.CambiarEstadoOperacion, cl.bcs.mesa.MRF.VencerOperacion, cl.bcs.mesa.MRF.VencerOperacion.Operacion, cl.bcs.mesa.MRF.VencerOperacion.VencerOperacionMesaEntrada, cl.bcs.mesa.MRF.VencerOperacion.VencerOperacionMesaSalida, cl.bcs.ordenes.LiquidarCustodiaOrdenCompra, cl.bcs.ordenes.LiquidarCustodiaOrdenCompra.CarteraCompraObj_IN, cl.bcs.ordenes.LiquidarCustodiaOrdenCompra.LiquidarCustodiaOrdenCompra_IN, cl.bcs.ordenes.LiquidarCustodiaOrdenCompra.LiquidarCustodiaOrdenCompra_OUT, cl.bcs.ordenes.LiquidarCustodiaOrdenVenta, cl.bcs.ordenes.LiquidarCustodiaOrdenVenta.CarteraVentaObj_IN, cl.bcs.ordenes.LiquidarCustodiaOrdenVenta.LiquidarCustodiaOrdenVenta_IN, cl.bcs.ordenes.LiquidarCustodiaOrdenVenta.LiquidarCustodiaOrdenVenta_OUT, cl.bcs.plataforma.pla.ObtenerDiaAnterior, cl.bcs.plataforma.pla.ObtenerDiaAnterior.InObtenerDiaAnterior, cl.bcs.plataforma.pla.ObtenerDiaAnterior.OutObtenerDiaAnterior, cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Entrada, cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Salida, cl.bcs.tesoreria.CrearIngresoEgreso.FormaDePago_Entrada, cl.bcs.tesoreria.CrearIngresoEgreso.IngresoEgreso_Entrada.
 * TODO: implement after cross-service HTTP clients are available.
 */
@Service
public class EjecutarVencimientoService implements JavaCallService {
    private static final Logger log = LoggerFactory.getLogger(EjecutarVencimientoService.class);

    @Override
    public String getFqn() {
        return "cl.bcs.custodia.EjecutarVencimiento";
    }

    @Override
    public JsonObject execute(JsonObject params) {
        log.warn("EjecutarVencimientoService.execute called but class is deferred - returning 503");
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
            "cl.bcs.custodia.EjecutarVencimiento is deferred: cl.bcs.plataforma.lim.ValidarLimiteCuentaPropiaIFRF, cl.bcs.plataforma.lim.pojo.ConstantesLimites, cl.bcs.plataforma.mer.CalcularParidad, cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada, cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida, cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada, cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida, cl.bcs.cuentaInversion.CrearCargoAbono, cl.bcs.cuentaInversion.CrearCargoAbono.CrearCargoAbonoEntrada, cl.bcs.cuentaInversion.CrearCargoAbono.CrearCargoAbonoSalida, cl.bcs.mesa.MES.CambiarEstadoOperacion, cl.bcs.mesa.MRF.VencerOperacion, cl.bcs.mesa.MRF.VencerOperacion.Operacion, cl.bcs.mesa.MRF.VencerOperacion.VencerOperacionMesaEntrada, cl.bcs.mesa.MRF.VencerOperacion.VencerOperacionMesaSalida, cl.bcs.ordenes.LiquidarCustodiaOrdenCompra, cl.bcs.ordenes.LiquidarCustodiaOrdenCompra.CarteraCompraObj_IN, cl.bcs.ordenes.LiquidarCustodiaOrdenCompra.LiquidarCustodiaOrdenCompra_IN, cl.bcs.ordenes.LiquidarCustodiaOrdenCompra.LiquidarCustodiaOrdenCompra_OUT, cl.bcs.ordenes.LiquidarCustodiaOrdenVenta, cl.bcs.ordenes.LiquidarCustodiaOrdenVenta.CarteraVentaObj_IN, cl.bcs.ordenes.LiquidarCustodiaOrdenVenta.LiquidarCustodiaOrdenVenta_IN, cl.bcs.ordenes.LiquidarCustodiaOrdenVenta.LiquidarCustodiaOrdenVenta_OUT, cl.bcs.plataforma.pla.ObtenerDiaAnterior, cl.bcs.plataforma.pla.ObtenerDiaAnterior.InObtenerDiaAnterior, cl.bcs.plataforma.pla.ObtenerDiaAnterior.OutObtenerDiaAnterior, cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Entrada, cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Salida, cl.bcs.tesoreria.CrearIngresoEgreso.FormaDePago_Entrada, cl.bcs.tesoreria.CrearIngresoEgreso.IngresoEgreso_Entrada");
    }
}
