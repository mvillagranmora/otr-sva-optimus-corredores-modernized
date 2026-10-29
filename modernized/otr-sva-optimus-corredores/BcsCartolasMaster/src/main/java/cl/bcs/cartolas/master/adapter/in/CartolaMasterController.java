package cl.bcs.cartolas.master.adapter.in;

import cl.bcs.cartolas.master.domain.ConfiguracionCartola;
import cl.bcs.cartolas.master.domain.ParametroCartola;
import cl.bcs.cartolas.master.port.in.CartolaMasterUseCase;
import cl.bcs.cartolas.master.port.in.CartolaMasterUseCase.CartolaSummary;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Controlador REST para el módulo Cartolas Master.
 *
 * <p>Expone endpoints para:
 * <ul>
 *   <li>Consulta de configuración de cartolas por empresa.</li>
 *   <li>Consulta de parámetros de nómina.</li>
 *   <li>Generación online de cartola (requiere HANA).</li>
 *   <li>Generación por id de cartola en proceso batch (requiere HANA).</li>
 * </ul>
 * </p>
 */
@RestController
@RequestMapping("/cartolas-master")
public class CartolaMasterController {

    private final CartolaMasterUseCase useCase;

    public CartolaMasterController(final CartolaMasterUseCase useCase) {
        this.useCase = Objects.requireNonNull(useCase, "useCase es requerido");
    }

    /**
     * Retorna la configuración de cartolas de una empresa.
     *
     * <p>GET /cartolas-master/configuracion/{idEmpresa}</p>
     */
    @GetMapping("/configuracion/{idEmpresa}")
    public ResponseEntity<List<ConfiguracionCartola>> getConfiguracion(
            @PathVariable final long idEmpresa) {
        Objects.requireNonNull(idEmpresa, "idEmpresa es requerido");
        return ResponseEntity.ok(useCase.obtenerConfiguracion(idEmpresa));
    }

    /**
     * Retorna los parámetros de una nómina de cartolas.
     *
     * <p>GET /cartolas-master/parametros/{idNomina}</p>
     */
    @GetMapping("/parametros/{idNomina}")
    public ResponseEntity<List<ParametroCartola>> getParametros(
            @PathVariable final long idNomina) {
        Objects.requireNonNull(idNomina, "idNomina es requerido");
        return ResponseEntity.ok(useCase.obtenerParametros(idNomina));
    }

    /**
     * Genera una cartola online para un cliente / portafolio.
     *
     * <p>POST /cartolas-master/generar-online
     *
     * <p><b>Requiere HANA.</b> En entorno de test retorna 503.</p>
     *
     * @param idCliente    identificador del cliente
     * @param idPortafolio identificador del portafolio (opcional; null = consolidada)
     * @param fechaDesde   fecha de inicio del periodo (ISO 8601)
     * @param fechaHasta   fecha de fin del periodo (ISO 8601)
     */
    @PostMapping("/generar-online")
    public ResponseEntity<CartolaSummary> generarOnline(
            @RequestParam final long idCliente,
            @RequestParam(required = false) final Long idPortafolio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate fechaDesde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate fechaHasta) {
        Objects.requireNonNull(fechaDesde, "fechaDesde es requerido");
        Objects.requireNonNull(fechaHasta, "fechaHasta es requerido");
        final CartolaSummary result = useCase.generarOnline(idCliente, idPortafolio,
                fechaDesde, fechaHasta);
        return ResponseEntity.ok(result);
    }

    /**
     * Genera una cartola por su id de cartola en HANA.
     *
     * <p>POST /cartolas-master/generar/{idCartola}
     *
     * <p><b>Requiere HANA.</b> En entorno de test retorna 503.</p>
     */
    @PostMapping("/generar/{idCartola}")
    public ResponseEntity<CartolaSummary> generarPorIdCartola(
            @PathVariable final long idCartola) {
        final CartolaSummary result = useCase.generarPorIdCartola(idCartola);
        return ResponseEntity.ok(result);
    }
}
