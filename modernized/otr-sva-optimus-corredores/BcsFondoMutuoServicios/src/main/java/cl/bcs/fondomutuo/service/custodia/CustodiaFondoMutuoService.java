package cl.bcs.fondomutuo.service.custodia;

import cl.bcs.fondomutuo.port.out.HanaFondoMutuoPort;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

/**
 * Manages custodia (custody) operations for mutual funds.
 *
 * Operations mapped from legacy services:
 *   - ConsultarCustodiaPortafolioFFMM          → HANA SP (stub)
 *   - ConsultarCustodiaPortafolioFondoMutuo     → HANA SP (stub)
 *   - ConsultarFlujoCustodiaFondoMutuo          → HANA SP (stub)
 *   - ConsultarCustodiaDisponibleReinversion1N  → HANA SP (stub)
 *   - ProcesarCustodiaFondoMutuo               → HANA SP (stub)
 *   - CargarCustodiaFondoMutuo                 → HANA SP (stub)
 *   - CrearIngresoRetiroGarantiasFondoMutuo    → HANA SP (stub)
 *   - CrearIngresoRetiroTituloFondoMutuo       → HANA SP (stub)
 *   - ActualizarCarterasFondosMutuos           → HANA SP (stub)
 *   - ActualizaCarteraValorizadaFFMM           → HANA SP (stub)
 *   - ValorizaCarteraFFMM                      → HANA SP (stub)
 *   - ActualizarFondoIndustria                 → HANA SP (stub)
 *   - ConsultarValorCuotaHistorico             → HANA SP (stub)
 *   - ConsultarValorDolarFondoMutuo            → HANA SP (stub)
 *   - CargarValorCuota                         → HANA SP (stub)
 *   - CargaReinversionesFondoMutuo             → HANA SP (stub)
 */
@Service
public class CustodiaFondoMutuoService {

    private final HanaFondoMutuoPort hanaPort;

    public CustodiaFondoMutuoService(HanaFondoMutuoPort hanaPort) {
        this.hanaPort = hanaPort;
    }

    public Map<String, Object> consultarCustodiaPortafolio(Map<String, Object> params) {
        Objects.requireNonNull(params, "params");
        Objects.requireNonNull(params.get("idEmpresa"), "idEmpresa");
        return hanaPort.callStoredProcedure("BCS", "BCS_FM_CONSULTAR_CUSTODIA_PORTAFOLIO", params);
    }

    public Map<String, Object> consultarFlujoCustodia(Map<String, Object> params) {
        Objects.requireNonNull(params, "params");
        Objects.requireNonNull(params.get("idEmpresa"), "idEmpresa");
        return hanaPort.callStoredProcedure("BCS", "BCS_FM_CONSULTAR_FLUJO_CUSTODIA", params);
    }

    public Map<String, Object> cargarValorCuota(Map<String, Object> params) {
        Objects.requireNonNull(params, "params");
        Objects.requireNonNull(params.get("idEmpresa"), "idEmpresa");
        return hanaPort.callStoredProcedure("BCS", "BCS_FM_CARGAR_VALOR_CUOTA", params);
    }
}
