package cl.bcs.contabilidad.infrastructure.persistence;

import cl.bcs.contabilidad.domain.port.out.SecuenciaPort;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/** {@link SecuenciaPort} over HANA sequences ({@link HanaSequences}, legacy Utils.getSecuencia). */
@Component
public class HanaSecuenciaAdapter implements SecuenciaPort {

    private final NamedParameterJdbcTemplate jdbc;

    public HanaSecuenciaAdapter(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<Long> siguientes(String nombreSecuencia, long cantidad) {
        return HanaSequences.next(jdbc, nombreSecuencia, cantidad);
    }
}
