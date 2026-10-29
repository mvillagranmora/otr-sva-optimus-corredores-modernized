package cl.bcs.fondomutuo.service.omnibus;

import cl.bcs.fondomutuo.port.out.HanaFondoMutuoPort;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

/**
 * Manages omnibus (aggregated order) operations for mutual funds.
 *
 * Operations mapped from legacy services:
 *   - AutorizarOmnibus              → HANA SP (stub)
 *   - CuadrarOmnibusDetalle         → HANA SP (stub)
 *   - ReniciarFolioOperacionOmnibusEmisor → HANA SP (stub)
 *   - ConsultarSolicitudesFFMMAgente → HANA SP (stub)
 *   - ConsultarSolicitudesParaAGF   → HANA SP (stub)
 *   - GeneracionArchivoAGF          → HANA SP (stub)
 *   - CrearSolicitudAGF             → HANA SP (stub)
 */
@Service
public class OmnibusService {

    private final HanaFondoMutuoPort hanaPort;

    public OmnibusService(HanaFondoMutuoPort hanaPort) {
        this.hanaPort = hanaPort;
    }

    public Map<String, Object> autorizarOmnibus(Map<String, Object> params) {
        Objects.requireNonNull(params, "params");
        Objects.requireNonNull(params.get("idEmpresa"), "idEmpresa");
        return hanaPort.callStoredProcedure("BCS", "BCS_FM_AUTORIZAR_OMNIBUS", params);
    }

    public Map<String, Object> cuadrarOmnibusDetalle(Map<String, Object> params) {
        Objects.requireNonNull(params, "params");
        Objects.requireNonNull(params.get("idEmpresa"), "idEmpresa");
        return hanaPort.callStoredProcedure("BCS", "BCS_FM_CUADRAR_OMNIBUS_DETALLE", params);
    }
}
