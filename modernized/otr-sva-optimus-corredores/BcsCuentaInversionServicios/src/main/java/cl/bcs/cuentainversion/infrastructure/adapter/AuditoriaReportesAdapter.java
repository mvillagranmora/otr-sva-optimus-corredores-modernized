package cl.bcs.cuentainversion.infrastructure.adapter;

import cl.bcs.cuentainversion.port.out.AuditoriaReportes;
import cl.bcs.cuentainversion.port.out.Colaboradores;
import cl.bcs.cuentainversion.reportes.AgregarLogEntrada;
import cl.bcs.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Production {@link AuditoriaReportes}: sends the audit entry to the platform service {@code cl.bcs.plataforma.pla.AgregarLog}
 * (JSON of {@link AgregarLogEntrada}) through the generic {@link Colaboradores} port. An entry without {@code p_Log} rows is not sent
 * and a failure of the audit service is logged and swallowed, as the legacy service did (README CTA-B-18): the audit trail never
 * rolls back the business operation.
 */
@Component
public class AuditoriaReportesAdapter implements AuditoriaReportes {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaReportesAdapter.class);
    private static final String AGREGAR_LOG = "cl.bcs.plataforma.pla.AgregarLog";

    private final Colaboradores colaboradores;

    public AuditoriaReportesAdapter(Colaboradores colaboradores) {
        this.colaboradores = colaboradores;
    }

    @Override
    public void agregarLog(AgregarLogEntrada entrada) {
        if (entrada.p_Log == null || entrada.p_Log.isEmpty()) {
            return;
        }
        try {
            colaboradores.invocar(AGREGAR_LOG, Utils.getGsonBuilder().toJsonTree(entrada).getAsJsonObject());
        } catch (RuntimeException e) {
            log.error("audit service failed; the entry is lost", e);
        }
    }
}
