package cl.bcs.fondomutuo.service.solicitud;

import cl.bcs.fondomutuo.port.out.HanaFondoMutuoPort;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

/**
 * Orchestrates traspaso (fund transfer) operations.
 *
 * Operations mapped from legacy services:
 *   - CrearTraspaso           → HANA SP (stub)
 *   - ConfirmarTraspaso       → HANA SP (stub)
 *   - AnulacionTraspaso       → HANA SP (stub)
 *   - ModificarTraspaso       → HANA SP (stub)
 *   - NotificarTraspasoConfirmar → HANA SP (stub)
 *   - RecaudacionTraspaso     → HANA SP (stub)
 *   - CrearSolicitudRescateTraspaso → HANA SP (stub)
 *   - CrearSolicitudRescateTraspasoPortafolio → HANA SP (stub)
 *   - CrearSolicitudRescateTransferencia → HANA SP (stub)
 *   - CrearAporteRescateFusion → HANA SP (stub)
 */
@Service
public class TraspasoService {

    private final HanaFondoMutuoPort hanaPort;

    public TraspasoService(HanaFondoMutuoPort hanaPort) {
        this.hanaPort = hanaPort;
    }

    public Map<String, Object> crearTraspaso(Map<String, Object> params) {
        Objects.requireNonNull(params, "params");
        Objects.requireNonNull(params.get("idEmpresa"), "idEmpresa");
        return hanaPort.callStoredProcedure("BCS", "BCS_FM_CREAR_TRASPASO", params);
    }

    public Map<String, Object> confirmarTraspaso(Map<String, Object> params) {
        Objects.requireNonNull(params, "params");
        Objects.requireNonNull(params.get("idEmpresa"), "idEmpresa");
        return hanaPort.callStoredProcedure("BCS", "BCS_FM_CONFIRMAR_TRASPASO", params);
    }

    public Map<String, Object> anularTraspaso(Map<String, Object> params) {
        Objects.requireNonNull(params, "params");
        Objects.requireNonNull(params.get("idEmpresa"), "idEmpresa");
        return hanaPort.callStoredProcedure("BCS", "BCS_FM_ANULAR_TRASPASO", params);
    }
}
