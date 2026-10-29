package cl.bcs.fondomutuo.service.cartera;

import cl.bcs.fondomutuo.port.out.HanaFondoMutuoPort;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

/**
 * Manages cartera (portfolio) and valorización (valuation) operations for mutual funds.
 *
 * Operations mapped from legacy services:
 *   - ConsultarFondo                            → HANA SP (stub)
 *   - ConsultarInstrumentosFondosMutuos         → HANA SP (stub)
 *   - ConsultarFondoMutuoReinversion            → HANA SP (stub)
 *   - ObtenerPerfilFondoMutuo                   → HANA SP (stub)
 *   - AperturaAutomaticoFondos                  → HANA SP (stub)
 *   - CalcularFechaSolicitudAporteRescate        → HANA SP (stub)
 *   - CalcularFechaSolicitudAporteRescateJava    → pure-logic (partial, date rules)
 *   - ActualizarFechaGestionOperaciones         → HANA SP (stub)
 *   - ConfirmarEjecutarFechaGestionOperaciones  → HANA SP (stub)
 *   - ConfirmarModificarFechaGestionOperaciones → HANA SP (stub)
 *   - RechazarModificarFechaGestionOperaciones  → HANA SP (stub)
 *   - AutorizacionModificacionFechaGestionOperaciones → HANA SP (stub)
 *   - ObtenerSolicitudesDeCambiosFechaGestion... → HANA SP (stub)
 *   - CuadraturaCierreDiario                    → HANA SP (stub)
 *   - AdministracionLiquidez                    → HANA SP (stub)
 *   - ConfigurarFondoAdministracionLiquidez     → HANA SP (stub)
 *   - ValidaPatrimonioEmisor                    → HANA SP (stub)
 *   - ConsultarValidacionDJ1932PorAgnoComercial → HANA SP (stub)
 *   - CalcularValores                           → HANA SP (stub)
 */
@Service
public class CarteraFondoMutuoService {

    private final HanaFondoMutuoPort hanaPort;

    public CarteraFondoMutuoService(HanaFondoMutuoPort hanaPort) {
        this.hanaPort = hanaPort;
    }

    public Map<String, Object> consultarFondo(Map<String, Object> params) {
        Objects.requireNonNull(params, "params");
        Objects.requireNonNull(params.get("idEmpresa"), "idEmpresa");
        return hanaPort.callStoredProcedure("BCS", "BCS_FM_CONSULTAR_FONDO", params);
    }

    public Map<String, Object> calcularFechaSolicitud(Map<String, Object> params) {
        Objects.requireNonNull(params, "params");
        Objects.requireNonNull(params.get("idEmpresa"), "idEmpresa");
        return hanaPort.callStoredProcedure("BCS", "BCS_FM_CALCULAR_FECHA_SOLICITUD", params);
    }

    public Map<String, Object> cuadraturaCierreDiario(Map<String, Object> params) {
        Objects.requireNonNull(params, "params");
        Objects.requireNonNull(params.get("idEmpresa"), "idEmpresa");
        return hanaPort.callStoredProcedure("BCS", "BCS_FM_CUADRATURA_CIERRE_DIARIO", params);
    }
}
