package cl.bcs.fondomutuo.service.csc;

import cl.bcs.fondomutuo.port.out.HanaFondoMutuoPort;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

/**
 * Manages CSC (Cuenta de Saldo Clientela) operations for mutual funds.
 *
 * Operations mapped from legacy services:
 *   - CrearIngresoCSC           → HANA SP (stub)
 *   - CrearIngresoCSCManejador  → HANA SP (stub)
 *   - CrearAporteCSC            → HANA SP (stub)
 *   - ModificarCSC              → HANA SP (stub)
 *   - ModificarCSCManejador     → HANA SP (stub)
 *   - AnulacionContratoCSC      → HANA SP (stub)
 *   - RevocacionContratoCSC     → HANA SP (stub)
 *   - RevocacionContratoCSCManejador → HANA SP (stub)
 *   - CrearIngresoRetiroGarantiasFondoMutuo → HANA SP (stub)
 *   - CrearAporteSAAPV          → HANA SP (stub)
 *   - CrearSolicitudReinversionPorCliente → HANA SP (stub)
 *   - CrearSolicitudCambioSerie → HANA SP (stub)
 *   - CrearSolicitudRescateReinversion → HANA SP (stub)
 *   - CrearSolicitudRescateReinversion1N → HANA SP (stub)
 *   - ProcesarReinversionRescate → HANA SP (stub)
 *   - NotificarEstadoOrdenFondoMutuo → HANA SP (stub)
 *   - ProcesarIngresoEgresoTesoreria → HANA SP (stub)
 *   - ProcesarIngresoOperacionSpot → HANA SP (stub)
 */
@Service
public class CscService {

    private final HanaFondoMutuoPort hanaPort;

    public CscService(HanaFondoMutuoPort hanaPort) {
        this.hanaPort = hanaPort;
    }

    public Map<String, Object> crearIngresoCSC(Map<String, Object> params) {
        Objects.requireNonNull(params, "params");
        Objects.requireNonNull(params.get("idEmpresa"), "idEmpresa");
        return hanaPort.callStoredProcedure("BCS", "BCS_FM_CREAR_INGRESO_CSC", params);
    }

    public Map<String, Object> anulacionContratoCSC(Map<String, Object> params) {
        Objects.requireNonNull(params, "params");
        Objects.requireNonNull(params.get("idEmpresa"), "idEmpresa");
        return hanaPort.callStoredProcedure("BCS", "BCS_FM_ANULACION_CONTRATO_CSC", params);
    }

    public Map<String, Object> revocacionContratoCSC(Map<String, Object> params) {
        Objects.requireNonNull(params, "params");
        Objects.requireNonNull(params.get("idEmpresa"), "idEmpresa");
        return hanaPort.callStoredProcedure("BCS", "BCS_FM_REVOCACION_CONTRATO_CSC", params);
    }
}
