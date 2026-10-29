package cl.bcs.plataforma.adapter.in.rest;

import cl.bcs.plataforma.domain.RespuestaEstandar;
import cl.bcs.plataforma.service.plataforma.ConvertirMontoEscritoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST adapter for ConvertirMontoEscrito use case.
 * Migrated from legacy: cl.bcs.plataforma.pla.ConvertirNumeroTexto
 */
@RestController
@RequestMapping("/api/plataforma/conversor-monto")
public class ConvertirMontoEscritoController {

    // -----------------------------------------------------------------------
    // Request / Response records
    // -----------------------------------------------------------------------

    public record ConvertirLetrasRequest(String valor) {}

    public record ConvertirLetrasResponse(String valorEscrito) {}

    public record ConvertirRomanosRequest(String valor) {}

    public record ConvertirRomanosResponse(String valorRomano) {}

    // -----------------------------------------------------------------------
    // Endpoints
    // -----------------------------------------------------------------------

    /**
     * Converts a numeric string to its Spanish written form.
     * POST /api/plataforma/conversor-monto/letras
     */
    @PostMapping("/letras")
    public ResponseEntity<RespuestaEstandar<ConvertirLetrasResponse>> convertirALetras(
            @RequestBody ConvertirLetrasRequest req) {
        String resultado = ConvertirMontoEscritoService.cantidadConLetra(req.valor());
        return ResponseEntity.ok(RespuestaEstandar.ok(new ConvertirLetrasResponse(resultado)));
    }

    /**
     * Converts a numeric string (1-3999) to Roman numerals.
     * POST /api/plataforma/conversor-monto/romanos
     */
    @PostMapping("/romanos")
    public ResponseEntity<RespuestaEstandar<ConvertirRomanosResponse>> convertirARomanos(
            @RequestBody ConvertirRomanosRequest req) {
        String resultado = ConvertirMontoEscritoService.cantidadNumerosRomanos(req.valor());
        return ResponseEntity.ok(RespuestaEstandar.ok(new ConvertirRomanosResponse(resultado)));
    }

    /**
     * Batch conversion: multiple numeric values to Spanish written form.
     * POST /api/plataforma/conversor-monto/letras/batch
     */
    @PostMapping("/letras/batch")
    public ResponseEntity<RespuestaEstandar<List<ConvertirLetrasResponse>>> convertirALetrasBatch(
            @RequestBody List<ConvertirLetrasRequest> items) {
        List<ConvertirLetrasResponse> resultados = items.stream()
            .map(r -> new ConvertirLetrasResponse(ConvertirMontoEscritoService.cantidadConLetra(r.valor())))
            .toList();
        return ResponseEntity.ok(RespuestaEstandar.ok(resultados));
    }
}
