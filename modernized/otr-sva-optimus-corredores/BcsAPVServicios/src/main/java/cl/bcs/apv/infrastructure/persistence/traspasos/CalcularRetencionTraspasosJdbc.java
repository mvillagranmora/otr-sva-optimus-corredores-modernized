package cl.bcs.apv.infrastructure.persistence.traspasos;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import cl.bcs.apv.application.comun.Sneaky;
import cl.bcs.apv.domain.port.out.traspasos.CalcularRetencionTraspasosRepositorio;

/** JDBC adapter of {@link CalcularRetencionTraspasosRepositorio}; the SQL files are the legacy ones, byte for byte. */
@Repository
public class CalcularRetencionTraspasosJdbc implements CalcularRetencionTraspasosRepositorio {

    private static final String SQL_ORDENES = SqlTraspasos.recurso("ConsultaOrdenesTraspaso");
    private static final String SQL_RETENCION = SqlTraspasos.recurso("ConsultarRetencion");

    private final SqlTraspasos sql;

    public CalcularRetencionTraspasosJdbc(NamedParameterJdbcTemplate jdbc) {
        this.sql = new SqlTraspasos(jdbc);
    }

    @Override
    public List<OrdenTraspaso> ordenesTraspaso(Long idTraspaso) {
        Map<String, Object> p = new HashMap<>();
        p.put("IdTraspaso", idTraspaso);
        return Sneaky.sinChequear(() -> sql.consultar(SQL_ORDENES, p, OrdenTraspaso.class));
    }

    @Override
    public List<Retencion> configuracionRetencion() {
        return Sneaky.sinChequear(() -> sql.consultar(SQL_RETENCION, new HashMap<String, Object>(), Retencion.class));
    }

    @Override
    public void revertir() {
        Sneaky.sinChequear(() -> sql.revertir());
    }
}
