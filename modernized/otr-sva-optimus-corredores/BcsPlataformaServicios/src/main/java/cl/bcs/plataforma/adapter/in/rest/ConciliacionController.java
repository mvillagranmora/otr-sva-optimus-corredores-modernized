package cl.bcs.plataforma.adapter.in.rest;

import org.springframework.web.bind.annotation.*;

/**
 * Stub controller for conciliation (CCL) operations.
 * Legacy package: cl.bcs.plataforma.ccl (14 classes)
 *
 * Covers:
 *   - BcsConciliacion, ElConciliador, ValidacionPreventiva
 *   - ConciliacionFacturacionCuentaInversion, ConciliacionFacturacionCustodia
 *   - ConciliacionFacturacionTesoreria, ConciliacionSpotFacturacion
 *   - ConciliacionFFMMCuentaInversion, ConciliacionFFMMMovimientoSpot
 *   - ConcilicacionFFMMCustodia
 *   - ConciliacionMesaRFCuentaInversion, ConciliacionMesaRFCustodia
 *   - ConciliacionMesaRFTesoreria
 *   - ConstantesConciliacion (constants)
 *
 * TODO: wire when PLA.Servicio DDL + SELECT * export is provided.
 */
@RestController
@RequestMapping("/api/plataforma/conciliacion")
public class ConciliacionController {

    @PostMapping("/facturacion/cuenta-inversion")
    public Object conciliacionFacturacionCuentaInversion(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — CCL.FacturacionCuentaInversion not yet wired");
    }

    @PostMapping("/facturacion/custodia")
    public Object conciliacionFacturacionCustodia(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — CCL.FacturacionCustodia not yet wired");
    }

    @PostMapping("/facturacion/tesoreria")
    public Object conciliacionFacturacionTesoreria(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — CCL.FacturacionTesoreria not yet wired");
    }

    @PostMapping("/facturacion/spot")
    public Object conciliacionSpotFacturacion(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — CCL.SpotFacturacion not yet wired");
    }

    @PostMapping("/ffmm/cuenta-inversion")
    public Object conciliacionFFMMCuentaInversion(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — CCL.FFMMCuentaInversion not yet wired");
    }

    @PostMapping("/ffmm/movimiento-spot")
    public Object conciliacionFFMMMovimientoSpot(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — CCL.FFMMMovimientoSpot not yet wired");
    }

    @PostMapping("/ffmm/custodia")
    public Object conciliacionFFMMCustodia(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — CCL.FFMMCustodia not yet wired");
    }

    @PostMapping("/mesa-rf/cuenta-inversion")
    public Object conciliacionMesaRFCuentaInversion(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — CCL.MesaRFCuentaInversion not yet wired");
    }

    @PostMapping("/mesa-rf/custodia")
    public Object conciliacionMesaRFCustodia(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — CCL.MesaRFCustodia not yet wired");
    }

    @PostMapping("/mesa-rf/tesoreria")
    public Object conciliacionMesaRFTesoreria(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — CCL.MesaRFTesoreria not yet wired");
    }

    @PostMapping("/validacion-preventiva")
    public Object validacionPreventiva(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — CCL.ValidacionPreventiva not yet wired");
    }
}
