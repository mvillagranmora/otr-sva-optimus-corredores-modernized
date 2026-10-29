package cl.bcs.fondomutuo.service.comision;

import cl.bcs.fondomutuo.port.out.HanaFondoMutuoPort;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

/**
 * Manages commission (comisión) operations for mutual funds.
 *
 * Operations mapped from legacy services:
 *   - CalcularComisiones       → HANA SP (stub)
 *   - AdministrarComisiones    → HANA SP (stub)
 *   - CrearComision            → HANA SP (stub)
 *   - ModificarComision        → HANA SP (stub)
 *   - CrearTramoComision       → HANA SP (stub)
 *   - ModificarTramoComision   → HANA SP (stub)
 *   - QuitarTramoComision      → HANA SP (stub)
 *   - ProcesarComisionesNN     → HANA SP (stub)
 *   - ConsultarComisionesNNFondoMutuo → HANA SP (stub)
 *   - CrearSolicitudRescateComisionNN → HANA SP (stub)
 *   - CrearSolicitudRescatePorComision → HANA SP (stub)
 *   - ModificarComisionSolicitudPorPerfeccionamiento → HANA SP (stub)
 *   - CalcularCostoFactorAjuste → HANA SP (stub)
 *   - ActualizarCostoUF        → HANA SP (stub)
 *   - ActualizarCostoUF_MLT    → HANA SP (stub)
 *   - ActualizarCostoUFCarga   → HANA SP (stub)
 *   - CalculaCustodiaRescate   → HANA SP (stub)
 */
@Service
public class ComisionService {

    private final HanaFondoMutuoPort hanaPort;

    public ComisionService(HanaFondoMutuoPort hanaPort) {
        this.hanaPort = hanaPort;
    }

    public Map<String, Object> calcularComisiones(Map<String, Object> params) {
        Objects.requireNonNull(params, "params");
        Objects.requireNonNull(params.get("idEmpresa"), "idEmpresa");
        return hanaPort.callStoredProcedure("BCS", "BCS_FM_CALCULAR_COMISIONES", params);
    }

    public Map<String, Object> crearComision(Map<String, Object> params) {
        Objects.requireNonNull(params, "params");
        Objects.requireNonNull(params.get("idEmpresa"), "idEmpresa");
        return hanaPort.callStoredProcedure("BCS", "BCS_FM_CREAR_COMISION", params);
    }

    public Map<String, Object> modificarComision(Map<String, Object> params) {
        Objects.requireNonNull(params, "params");
        Objects.requireNonNull(params.get("idEmpresa"), "idEmpresa");
        return hanaPort.callStoredProcedure("BCS", "BCS_FM_MODIFICAR_COMISION", params);
    }
}
