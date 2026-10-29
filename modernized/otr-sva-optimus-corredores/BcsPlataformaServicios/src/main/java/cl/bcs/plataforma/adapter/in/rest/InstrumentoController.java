package cl.bcs.plataforma.adapter.in.rest;

import org.springframework.web.bind.annotation.*;

/**
 * Stub controller for instrument management (INS) operations.
 * Legacy package: cl.bcs.plataforma.ins (22 classes)
 *
 * Covers:
 *   - AsignarClasificacionIndice, AsociarInstrumentoEnClasificacion
 *   - CargaArchivoPorcentajeDescuentoRF, CargaArchivoPresenciaBursatil
 *   - CargaArchivoValorRiesgoRV, CargaArchivoVectorPrecio
 *   - CargarMarketMakerDesdeBursatil
 *   - ConsultarDatosAsociadosNiveles
 *   (and additional instrument services)
 *
 * TODO: wire when PLA.Servicio DDL + SELECT * export is provided.
 */
@RestController
@RequestMapping("/api/plataforma/instrumento")
public class InstrumentoController {

    @PostMapping("/clasificacion/asignar-indice")
    public Object asignarClasificacionIndice(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — INS.ClasificacionIndice not yet wired");
    }

    @PostMapping("/clasificacion/asociar")
    public Object asociarInstrumentoEnClasificacion(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — INS.Clasificacion not yet wired");
    }

    @PostMapping("/archivo/cargar-descuento-rf")
    public Object cargaArchivoPorcentajeDescuentoRF(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires file processing — INS.DescuentoRF not yet wired");
    }

    @PostMapping("/archivo/cargar-presencia-bursatil")
    public Object cargaArchivoPresenciaBursatil(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires file processing — INS.PresenciaBursatil not yet wired");
    }

    @PostMapping("/archivo/cargar-valor-riesgo-rv")
    public Object cargaArchivoValorRiesgoRV(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires file processing — INS.ValorRiesgoRV not yet wired");
    }

    @PostMapping("/archivo/cargar-vector-precio")
    public Object cargaArchivoVectorPrecio(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires file processing — INS.VectorPrecio not yet wired");
    }

    @PostMapping("/market-maker/cargar-bursatil")
    public Object cargarMarketMakerDesdeBursatil(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — INS.MarketMaker not yet wired");
    }

    @PostMapping("/niveles/consultar-datos-asociados")
    public Object consultarDatosAsociadosNiveles(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — INS.Niveles not yet wired");
    }
}
