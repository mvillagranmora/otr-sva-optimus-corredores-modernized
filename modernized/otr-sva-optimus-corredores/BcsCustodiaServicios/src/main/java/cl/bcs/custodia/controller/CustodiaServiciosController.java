package cl.bcs.custodia.controller;

import cl.bcs.custodia.domain.PosicionCustodia;
import cl.bcs.custodia.port.out.CustodiaServiciosDataPort;
import cl.bcs.custodia.service.CustodiaServiciosService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * REST controller for Custodia Servicios.
 * Port: 8097
 *
 * Pure-logic endpoints delegate to CustodiaServiciosService static methods.
 * Data endpoints delegate to CustodiaServiciosDataPort.
 * HANA-dependent endpoints return 503 via GlobalExceptionHandler (UOE).
 */
@RestController
@RequestMapping("/custodia-servicios")
public class CustodiaServiciosController {

    private final CustodiaServiciosDataPort dataPort;

    public CustodiaServiciosController(CustodiaServiciosDataPort dataPort) {
        this.dataPort = dataPort;
    }

    // -----------------------------------------------------------------------
    // Pure-logic endpoints
    // -----------------------------------------------------------------------

    /**
     * POST /custodia-servicios/cuenta-dias
     * Body: {"d1": "20240101", "d2": "20240108", "tipo": "C"}
     * Returns: number of days (Long)
     */
    @PostMapping("/cuenta-dias")
    public Long cuentaDias(@RequestBody Map<String, String> body) {
        String d1 = Objects.requireNonNull(body.get("d1"), "d1 is required");
        String d2 = Objects.requireNonNull(body.get("d2"), "d2 is required");
        String tipo = Objects.requireNonNull(body.get("tipo"), "tipo is required");
        return CustodiaServiciosService.cuentaDias(d1, d2, tipo);
    }

    /**
     * POST /custodia-servicios/calculo-interes
     * Body: {"monto": 1000, "plazo": 30, "tasa": 5, "base": 360}
     * Returns: interest amount (BigDecimal)
     */
    @PostMapping("/calculo-interes")
    public BigDecimal calculoInteres(@RequestBody Map<String, BigDecimal> body) {
        BigDecimal monto = Objects.requireNonNull(body.get("monto"), "monto is required");
        BigDecimal plazo = Objects.requireNonNull(body.get("plazo"), "plazo is required");
        BigDecimal tasa = Objects.requireNonNull(body.get("tasa"), "tasa is required");
        BigDecimal base = Objects.requireNonNull(body.get("base"), "base is required");
        return CustodiaServiciosService.calculoInteres(monto, plazo, tasa, base);
    }

    /**
     * GET /custodia-servicios/hash-busqueda
     * Params: idInstrumento, idPortafolio, idCliente, nemosvs
     * Returns: hash string
     */
    @GetMapping("/hash-busqueda")
    public String getHashBusqueda(
            @RequestParam Long idInstrumento,
            @RequestParam Long idPortafolio,
            @RequestParam Long idCliente,
            @RequestParam String nemosvs) {
        Objects.requireNonNull(idInstrumento, "idInstrumento is required");
        Objects.requireNonNull(idPortafolio, "idPortafolio is required");
        Objects.requireNonNull(idCliente, "idCliente is required");
        Objects.requireNonNull(nemosvs, "nemosvs is required");
        return CustodiaServiciosService.getHashBusqueda(idInstrumento, idPortafolio, idCliente, nemosvs);
    }

    /**
     * GET /custodia-servicios/rango-fechas
     * Params: fechaInicio, fechaTermino
     * Returns: list of dates (yyyy-MM-dd)
     */
    @GetMapping("/rango-fechas")
    public List<String> obtenerRangoDeFechas(
            @RequestParam String fechaInicio,
            @RequestParam String fechaTermino) {
        Objects.requireNonNull(fechaInicio, "fechaInicio is required");
        Objects.requireNonNull(fechaTermino, "fechaTermino is required");
        return CustodiaServiciosService.obtenerRangoDeFechas(fechaInicio, fechaTermino);
    }

    /**
     * GET /custodia-servicios/tipo-busquedas
     * Returns: TIPO_BUSQUEDAS constant list
     */
    @GetMapping("/tipo-busquedas")
    public List<String> getTipoBusquedas() {
        return CustodiaServiciosService.TIPO_BUSQUEDAS;
    }

    // -----------------------------------------------------------------------
    // Data endpoints (H2-testable via plain SQL)
    // -----------------------------------------------------------------------

    /**
     * GET /custodia-servicios/posicion/{idPortafolio}
     * Returns: list of positions for the given portfolio
     */
    @GetMapping("/posicion/{idPortafolio}")
    public List<PosicionCustodia> consultarPosicion(@PathVariable Long idPortafolio) {
        Objects.requireNonNull(idPortafolio, "idPortafolio is required");
        return dataPort.consultarPosicion(idPortafolio);
    }

    /**
     * GET /custodia-servicios/fecha-sistema/{idEmpresa}
     * Returns: system date string for the given company
     */
    @GetMapping("/fecha-sistema/{idEmpresa}")
    public String obtenerFechaSistema(@PathVariable Long idEmpresa) {
        Objects.requireNonNull(idEmpresa, "idEmpresa is required");
        return dataPort.obtenerFechaSistema(idEmpresa);
    }

    // -----------------------------------------------------------------------
    // HANA SP endpoints — return 503 via GlobalExceptionHandler (UOE)
    // -----------------------------------------------------------------------

    /**
     * POST /custodia-servicios/liquidar-emprestito
     * Body: {"idEmpresa": 1, "idPortafolio": 101, "idCliente": 201}
     * Returns: 503 (UOE — requires HANA SP)
     */
    @PostMapping("/liquidar-emprestito")
    public void liquidarEmprestito(@RequestBody Map<String, Long> body) {
        Long idEmpresa = Objects.requireNonNull(body.get("idEmpresa"), "idEmpresa is required");
        Long idPortafolio = Objects.requireNonNull(body.get("idPortafolio"), "idPortafolio is required");
        Long idCliente = Objects.requireNonNull(body.get("idCliente"), "idCliente is required");
        dataPort.liquidarEmprestito(idEmpresa, idPortafolio, idCliente);
    }

    /**
     * POST /custodia-servicios/anticipar-prestamo
     * Body: {"idEmpresa": 1, "idPortafolio": 101, "tipoPrestamo": "PRTVTC"}
     * Returns: 503 (UOE — requires HANA SP)
     */
    @PostMapping("/anticipar-prestamo")
    public void anticiparPrestamo(@RequestBody Map<String, String> body) {
        Long idEmpresa = Objects.requireNonNull(
                body.get("idEmpresa") != null ? Long.valueOf(body.get("idEmpresa")) : null,
                "idEmpresa is required");
        Long idPortafolio = Objects.requireNonNull(
                body.get("idPortafolio") != null ? Long.valueOf(body.get("idPortafolio")) : null,
                "idPortafolio is required");
        String tipoPrestamo = Objects.requireNonNull(body.get("tipoPrestamo"), "tipoPrestamo is required");
        dataPort.anticiparPrestamo(idEmpresa, idPortafolio, tipoPrestamo);
    }

    /**
     * POST /custodia-servicios/guardar-patrimonio
     * Body: {"idEmpresa": 1, "fechaInicio": "2024-01-01", "fechaTermino": "2024-01-31"}
     * Returns: 503 (UOE — requires HANA SP)
     */
    @PostMapping("/guardar-patrimonio")
    public void guardarPatrimonio(@RequestBody Map<String, String> body) {
        Long idEmpresa = Objects.requireNonNull(
                body.get("idEmpresa") != null ? Long.valueOf(body.get("idEmpresa")) : null,
                "idEmpresa is required");
        String fechaInicio = Objects.requireNonNull(body.get("fechaInicio"), "fechaInicio is required");
        String fechaTermino = Objects.requireNonNull(body.get("fechaTermino"), "fechaTermino is required");
        dataPort.guardarPatrimonio(idEmpresa, fechaInicio, fechaTermino);
    }
}
