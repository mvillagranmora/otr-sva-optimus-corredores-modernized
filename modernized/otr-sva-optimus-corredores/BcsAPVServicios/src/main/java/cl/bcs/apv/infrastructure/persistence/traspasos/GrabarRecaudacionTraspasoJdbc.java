package cl.bcs.apv.infrastructure.persistence.traspasos;

import java.util.List;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import cl.bcs.apv.application.comun.Sneaky;
import cl.bcs.apv.domain.port.out.traspasos.GrabarRecaudacionTraspasoRepositorio;

/** JDBC adapter of {@link GrabarRecaudacionTraspasoRepositorio}; the SQL files are the legacy ones, byte for byte (sql/tra). */
@Repository
public class GrabarRecaudacionTraspasoJdbc implements GrabarRecaudacionTraspasoRepositorio {

    private static final String SQL_ESTADO_TRASPASO = SqlTraspasos.recurso("CambiarEstadoTraspasoAPV");
    private static final String SQL_ESTADO_CONTRATO = SqlTraspasos.recurso("CambiarEstadoContratosSAAPV");

    private final SqlTraspasos sql;

    public GrabarRecaudacionTraspasoJdbc(NamedParameterJdbcTemplate jdbc) {
        this.sql = new SqlTraspasos(jdbc);
    }

    @Override
    public int[] cambiarEstadoTraspasos(List<EstadoTraspaso> traspasos) {
        return Sneaky.sinChequear(() -> sql.loteConConteos(SQL_ESTADO_TRASPASO, traspasos));
    }

    @Override
    public int[] cambiarEstadoContratos(List<EstadoContrato> contratos) {
        return Sneaky.sinChequear(() -> sql.loteConConteos(SQL_ESTADO_CONTRATO, contratos));
    }
}
