package cl.bcs.contabilidad.domain.port.out;

import cl.bcs.contabilidad.domain.model.sap.CuentaSap;

import java.util.List;

/**
 * SAP B1 chart of accounts (OACT of the company schema). Legacy: EnvioContabilidad_SelectOACT.sql over a SAP HANA
 * connection built from interfazSAP.properties (EnvioContabilidad.java:354-374, GenerarJsonAsientoSap.java:157-167).
 */
public interface PlanCuentasSapPort {

    List<CuentaSap> consultarPlanCuentas();

    /** The SAP B1 connection settings are missing (legacy: interfazSAP.properties not found, FileNotFoundException). */
    class ConfiguracionNoEncontradaException extends RuntimeException {
        public ConfiguracionNoEncontradaException(String message) {
            super(message);
        }
    }
}
