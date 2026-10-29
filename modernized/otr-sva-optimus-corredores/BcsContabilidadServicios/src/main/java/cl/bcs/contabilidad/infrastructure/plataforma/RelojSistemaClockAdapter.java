package cl.bcs.contabilidad.infrastructure.plataforma;

import cl.bcs.contabilidad.domain.port.out.RelojSistemaPort;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * {@link RelojSistemaPort} from the injected {@link Clock}. Legacy read the database time of the company
 * ({@code FechaDB.ObtenerFechaHoraTs / ObtenerHoraSistemaLocal}); here the clock's zone stands for it.
 */
@Component
public class RelojSistemaClockAdapter implements RelojSistemaPort {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final Clock clock;

    public RelojSistemaClockAdapter(Clock clock) {
        this.clock = clock;
    }

    @Override
    public String fechaProceso(Long idEmpresa) {
        return LocalDateTime.now(clock).format(FECHA);
    }

    @Override
    public String horaProceso(Long idEmpresa) {
        return LocalDateTime.now(clock).format(HORA);
    }
}
