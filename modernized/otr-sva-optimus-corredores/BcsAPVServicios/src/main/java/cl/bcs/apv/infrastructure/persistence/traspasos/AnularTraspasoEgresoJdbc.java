package cl.bcs.apv.infrastructure.persistence.traspasos;

import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import cl.bcs.apv.application.comun.Sneaky;
import cl.bcs.apv.domain.port.out.traspasos.AnularTraspasoEgresoRepositorio;

/** JDBC adapter of {@link AnularTraspasoEgresoRepositorio}; the SQL files are the legacy ones, byte for byte. */
@Repository
public class AnularTraspasoEgresoJdbc implements AnularTraspasoEgresoRepositorio {

    private static final String SQL_ANULAR_TRASPASOS = SqlTraspasos.recurso("AnularTraspasosEgreso");
    private static final String SQL_ANULAR_DETALLE = SqlTraspasos.recurso("AnularDetalleTraspasosEgreso");
    private static final String SQL_REACTIVAR_CONTRATOS = SqlTraspasos.recurso("ReactivarContratoTraspasosEgreso");

    private final SqlTraspasos sql;

    public AnularTraspasoEgresoJdbc(NamedParameterJdbcTemplate jdbc) {
        this.sql = new SqlTraspasos(jdbc);
    }

    /** Batch parameters: the legacy {@code Traspasos} class. */
    public static class Traspasos {
        public Long IdTraspaso;
        public Long IdTrabajo;
    }

    @Override
    public void anularTraspasos(List<Long> idsTraspaso, Long idTrabajo) {
        Sneaky.sinChequear(() -> sql.lote(SQL_ANULAR_TRASPASOS, filas(idsTraspaso, idTrabajo)));
    }

    @Override
    public void anularDetalleTraspasos(List<Long> idsTraspaso, Long idTrabajo) {
        Sneaky.sinChequear(() -> sql.lote(SQL_ANULAR_DETALLE, filas(idsTraspaso, idTrabajo)));
    }

    @Override
    public void reactivarContratos(List<Long> idsTraspaso, Long idTrabajo) {
        Sneaky.sinChequear(() -> sql.lote(SQL_REACTIVAR_CONTRATOS, filas(idsTraspaso, idTrabajo)));
    }

    private static List<Traspasos> filas(List<Long> ids, Long idTrabajo) {
        List<Traspasos> filas = new ArrayList<>();
        for (Long id : ids) {
            Traspasos t = new Traspasos();
            t.IdTrabajo = idTrabajo;
            t.IdTraspaso = id;
            filas.add(t);
        }
        return filas;
    }
}
