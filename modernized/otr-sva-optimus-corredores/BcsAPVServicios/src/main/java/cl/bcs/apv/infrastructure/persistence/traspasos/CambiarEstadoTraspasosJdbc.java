package cl.bcs.apv.infrastructure.persistence.traspasos;

import java.util.List;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import cl.bcs.apv.application.comun.Sneaky;
import cl.bcs.apv.domain.port.out.traspasos.CambiarEstadoTraspasosRepositorio;

/** JDBC adapter of {@link CambiarEstadoTraspasosRepositorio}; the SQL file is the legacy one, byte for byte. */
@Repository
public class CambiarEstadoTraspasosJdbc implements CambiarEstadoTraspasosRepositorio {

    private static final String SQL_CAMBIAR_ESTADO = SqlTraspasos.recurso("CambiarEstadoTraspasoAPV");

    private final SqlTraspasos sql;

    public CambiarEstadoTraspasosJdbc(NamedParameterJdbcTemplate jdbc) {
        this.sql = new SqlTraspasos(jdbc);
    }

    @Override
    public int[] cambiarEstado(List<EstadoTraspaso> traspasos) {
        return Sneaky.sinChequear(() -> sql.loteConConteos(SQL_CAMBIAR_ESTADO, traspasos));
    }
}
