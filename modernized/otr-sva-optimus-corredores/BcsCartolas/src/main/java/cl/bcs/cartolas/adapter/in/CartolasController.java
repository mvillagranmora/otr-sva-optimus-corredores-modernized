package cl.bcs.cartolas.adapter.in;

import cl.bcs.cartolas.domain.Cartola;
import cl.bcs.cartolas.domain.Config;
import cl.bcs.cartolas.domain.Datos;
import cl.bcs.cartolas.domain.Nomina;
import cl.bcs.cartolas.domain.Parametro;
import cl.bcs.cartolas.port.in.CartolasUseCase;
import cl.bcs.cartolas.port.in.NominaUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Controlador REST principal de BcsCartolas.
 *
 * <p>Expone las operaciones de consulta del dominio de cartolas.
 * Las operaciones que requieren HANA (SPs de grabado, carga de datos, etc.)
 * devuelven 503 a través del {@link GlobalExceptionHandler}.</p>
 */
@RestController
@RequestMapping("/cartolas")
public class CartolasController {

    private final CartolasUseCase cartolasUseCase;
    private final NominaUseCase   nominaUseCase;

    public CartolasController(final CartolasUseCase cartolasUseCase,
                               final NominaUseCase nominaUseCase) {
        this.cartolasUseCase = Objects.requireNonNull(cartolasUseCase, "cartolasUseCase es requerido");
        this.nominaUseCase   = Objects.requireNonNull(nominaUseCase,   "nominaUseCase es requerido");
    }

    // =========================================================================
    // Nóminas
    // =========================================================================

    /**
     * GET /cartolas/nominas/{idNomina}
     * Retorna la nómina con el id dado.
     */
    @GetMapping("/nominas/{idNomina}")
    public ResponseEntity<Nomina> getNomina(@PathVariable final long idNomina) {
        return nominaUseCase.getNomina(idNomina)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /cartolas/nominas/programadas
     * Retorna las nóminas de tipo PROGRAMADO.
     */
    @GetMapping("/nominas/programadas")
    public List<Nomina> getNominasProgramadas() {
        return nominaUseCase.getNominasProgramadas();
    }

    /**
     * GET /cartolas/nominas/{idNomina}/parametros
     * Retorna los parámetros de una nómina.
     */
    @GetMapping("/nominas/{idNomina}/parametros")
    public List<Parametro> getParametros(@PathVariable final long idNomina) {
        return nominaUseCase.getParametros(idNomina);
    }

    // =========================================================================
    // Cartolas
    // =========================================================================

    /**
     * GET /cartolas/{idCartola}
     * Retorna la cartola con el id dado.
     */
    @GetMapping("/{idCartola}")
    public ResponseEntity<Cartola> getCartola(@PathVariable final long idCartola) {
        return cartolasUseCase.getCartola(idCartola)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /cartolas?idNomina={}&soloReproceso=false
     * Retorna las cartolas de una nómina, opcionalmente solo las marcadas para reproceso.
     */
    @GetMapping
    public List<Cartola> getCartolas(
            @RequestParam final long idNomina,
            @RequestParam(defaultValue = "false") final boolean soloReproceso) {
        return cartolasUseCase.getCartolasByNomina(idNomina, soloReproceso);
    }

    // =========================================================================
    // Configuración
    // =========================================================================

    /**
     * GET /cartolas/config?idEmpresa={}
     * Retorna la configuración de una empresa.
     */
    @GetMapping("/config")
    public List<Config> getConfig(@RequestParam final long idEmpresa) {
        return cartolasUseCase.getConfig(idEmpresa);
    }

    // =========================================================================
    // Datos
    // =========================================================================

    /**
     * GET /cartolas/{idCartola}/datos
     * Retorna los datos CSV almacenados para una cartola.
     */
    @GetMapping("/{idCartola}/datos")
    public List<Datos> getDatos(@PathVariable final long idCartola) {
        return cartolasUseCase.getDatos(idCartola);
    }

    // =========================================================================
    // Resolución de nombres de archivo — HANA-free
    // =========================================================================

    /**
     * GET /cartolas/nombre-archivo/cartola?patron={}&contexto key=value...
     * Resuelve el nombre de archivo de una cartola a partir del patrón y contexto dados.
     */
    @GetMapping("/nombre-archivo/cartola")
    public String resolverNombreArchivoCartola(
            @RequestParam final String patron,
            @RequestParam final Map<String, String> contexto) {
        Objects.requireNonNull(patron, "patron es requerido");
        // Quitamos el parámetro "patron" del mapa de contexto si está presente
        final Map<String, String> ctx = contexto.entrySet().stream()
                .filter(e -> !"patron".equals(e.getKey()))
                .collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return cartolasUseCase.resolverNombreArchivoCartola(patron, ctx);
    }

    /**
     * GET /cartolas/nombre-archivo/nomina?patron={}&contexto key=value...
     * Resuelve el nombre de archivo de una nómina.
     */
    @GetMapping("/nombre-archivo/nomina")
    public String resolverNombreArchivoNomina(
            @RequestParam final String patron,
            @RequestParam final Map<String, String> contexto) {
        Objects.requireNonNull(patron, "patron es requerido");
        final Map<String, String> ctx = contexto.entrySet().stream()
                .filter(e -> !"patron".equals(e.getKey()))
                .collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return cartolasUseCase.resolverNombreArchivoNomina(patron, ctx);
    }

    /**
     * GET /cartolas/nombre-archivo/zip?patron={}&contexto key=value...
     * Resuelve el nombre de archivo ZIP.
     */
    @GetMapping("/nombre-archivo/zip")
    public String resolverNombreArchivoZip(
            @RequestParam final String patron,
            @RequestParam final Map<String, String> contexto) {
        Objects.requireNonNull(patron, "patron es requerido");
        final Map<String, String> ctx = contexto.entrySet().stream()
                .filter(e -> !"patron".equals(e.getKey()))
                .collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return cartolasUseCase.resolverNombreArchivoZip(patron, ctx);
    }

    // =========================================================================
    // Operaciones que requieren HANA — retornan 503 vía GlobalExceptionHandler
    // =========================================================================

    /**
     * POST /cartolas/nominas/{idNomina}/poblar
     * Ejecuta el SP HANA CAR.PoblarNomina — requiere HANA.
     */
    @PostMapping("/nominas/{idNomina}/poblar")
    public ResponseEntity<Void> poblarNomina(@PathVariable final long idNomina) {
        throw new UnsupportedOperationException(
                "poblarNomina requiere SAP HANA — SP: CAR.PoblarNomina");
    }

    /**
     * POST /cartolas/carga-datos?idNomina={}
     * Ejecuta el proceso de carga de datos (ProcesoCargaDatos) — requiere HANA.
     */
    @PostMapping("/carga-datos")
    public ResponseEntity<Void> cargaDatos(@RequestParam final long idNomina) {
        throw new UnsupportedOperationException(
                "cargaDatos requiere SAP HANA — proceso: ProcesoCargaDatos");
    }
}
