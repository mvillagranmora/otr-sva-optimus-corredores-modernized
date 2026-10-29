package cl.bcs.apv.infrastructure.persistence.traspasos;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import cl.bcs.apv.application.comun.Sneaky;
import cl.bcs.apv.domain.port.out.traspasos.ReaperturarTraspasosRepositorio;

/** JDBC adapter of {@link ReaperturarTraspasosRepositorio}; the SQL file is the legacy one, byte for byte (sql/tra). */
@Repository
public class ReaperturarTraspasosJdbc implements ReaperturarTraspasosRepositorio {

    private static final String SQL_DETALLE_TRASPASOS = SqlTraspasos.recurso("IngresoRecaudacionTraspaso_SelectDetalleTraspasos");

    private final SqlTraspasos sql;

    public ReaperturarTraspasosJdbc(NamedParameterJdbcTemplate jdbc) {
        this.sql = new SqlTraspasos(jdbc);
    }

    @Override
    public List<EstadoDetalleTraspaso> detalleDelTraspaso(Long idTraspaso) {
        Map<String, Object> parametros = new java.util.HashMap<>();
        parametros.put("IdTraspaso", idTraspaso);
        return Sneaky.sinChequear(() -> sql.consultar(SQL_DETALLE_TRASPASOS, parametros, EstadoDetalleTraspaso.class));
    }
}
