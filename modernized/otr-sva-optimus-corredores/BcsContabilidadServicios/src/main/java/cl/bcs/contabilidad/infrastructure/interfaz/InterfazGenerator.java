package cl.bcs.contabilidad.infrastructure.interfaz;

import java.io.IOException;

/**
 * One step of the daily interface: queries SAP B1 and appends its file(s) (CONT-A-06, CONT-A-07).
 *
 * <p>Legacy: each {@code Crea*.logica(...)} call inside its own try block of {@code CreaInterfazDiaria.logica()}.
 * A generator may throw; the job logs the failure and moves on to the next one.
 */
@FunctionalInterface
public interface InterfazGenerator {

    void generar(InterfazContext contexto) throws IOException;
}
