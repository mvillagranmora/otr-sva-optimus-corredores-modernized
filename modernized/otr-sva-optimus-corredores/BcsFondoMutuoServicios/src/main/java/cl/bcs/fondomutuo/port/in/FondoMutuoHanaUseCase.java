package cl.bcs.fondomutuo.port.in;

import java.util.Map;

/**
 * Generic use case for HANA stored-procedure-backed operations.
 * Services that only orchestrate HANA SPs delegate through this port.
 */
public interface FondoMutuoHanaUseCase {
    Map<String, Object> ejecutar(String procedimiento, Map<String, Object> parametros);
}
