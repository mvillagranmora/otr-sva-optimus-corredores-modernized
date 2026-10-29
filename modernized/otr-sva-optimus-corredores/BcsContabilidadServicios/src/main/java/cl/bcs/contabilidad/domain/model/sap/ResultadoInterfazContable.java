package cl.bcs.contabilidad.domain.model.sap;

import java.util.List;

/**
 * Outcome of one InterfazContable run (CONT-B-40..50): ids accepted by SAP (state CON), ids marked Estado 'E', and the
 * run-log lines legacy wrote to {@code ITZ/<ts>_InterfazContable.log}.
 */
public record ResultadoInterfazContable(List<Long> enviados, List<Long> conError, List<String> lineasLog) {

    public ResultadoInterfazContable {
        enviados = List.copyOf(enviados);
        conError = List.copyOf(conError);
        lineasLog = List.copyOf(lineasLog);
    }
}
