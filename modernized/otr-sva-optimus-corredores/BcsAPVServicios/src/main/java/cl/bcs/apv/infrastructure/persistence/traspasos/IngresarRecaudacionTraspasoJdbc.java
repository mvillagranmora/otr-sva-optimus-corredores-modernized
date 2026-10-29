package cl.bcs.apv.infrastructure.persistence.traspasos;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import cl.bcs.apv.domain.port.out.traspasos.IngresarRecaudacionTraspasoRepositorio;

/** JDBC adapter of {@link IngresarRecaudacionTraspasoRepositorio}; the SQL files are the legacy ones byte for byte. */
@Repository
public class IngresarRecaudacionTraspasoJdbc implements IngresarRecaudacionTraspasoRepositorio {

    private static final String SQL_SELECT_TRASPASOS = SqlTraspasos.recurso("IngresoRecaudacionTraspaso_SelectTraspasos");
    private static final String SQL_SELECT_DETALLE_TRASPASOS = SqlTraspasos.recurso("IngresoRecaudacionTraspaso_SelectDetalleTraspasos");
    private static final String SQL_SELECT_DETALLE_TRASPASOS_REAPERTURA = SqlTraspasos.recurso("IngresoRecaudacionTraspaso_SelectDetalleTraspasosReapertura");
    private static final String SQL_ELIMINAR_CrearOperacionSpot_Proceso = "TRUNCATE TABLE \"BSCL.Mesa.BD::SPT.CrearOperacionSpot_Proceso\"";
    private static final String SQL_ELIMINAR_SaldoFlujoCaja = "DELETE FROM \"BSCL.Plataforma.BD::GBL.SaldoFlujoCaja_Proceso_Paso\"";
    private static final String SQL_SELECT_CrearOperacionSpot_Proceso = "SELECT * FROM \"BSCL.Mesa.BD::SPT.CrearOperacionSpot_Proceso\"";

    private final SqlTraspasos sql;

    public IngresarRecaudacionTraspasoJdbc(NamedParameterJdbcTemplate jdbc) {
        this.sql = new SqlTraspasos(jdbc);
    }

    @Override
    public List<Traspaso> traspasos(List<Long> idsTraspaso) throws SQLException {
        Map<String, Object> p = new HashMap<>();
        p.put("IdTraspaso", idsTraspaso);
        return sql.consultar(SQL_SELECT_TRASPASOS, p, Traspaso.class);
    }

    @Override
    public List<DetalleTraspaso> detalleTraspasos(List<Long> idsTraspaso) throws SQLException {
        Map<String, Object> p = new HashMap<>();
        p.put("IdTraspaso", idsTraspaso);
        return sql.consultar(SQL_SELECT_DETALLE_TRASPASOS, p, DetalleTraspaso.class);
    }

    @Override
    public List<DetalleTraspaso> detalleTraspasoReapertura(Long idTraspaso, Long idDetalleTraspaso) throws SQLException {
        Map<String, Object> p = new HashMap<>();
        p.put("IdTraspaso", idTraspaso);
        p.put("IdDetalleTraspaso", idDetalleTraspaso);
        return sql.consultar(SQL_SELECT_DETALLE_TRASPASOS_REAPERTURA, p, DetalleTraspaso.class);
    }

    @Override
    public Long contarOperacionesSpotEnProceso() throws SQLException {
        return sql.contar(SQL_SELECT_CrearOperacionSpot_Proceso, new HashMap<String, Object>());
    }

    @Override
    public void eliminarSaldoFlujoCajaProceso() throws SQLException {
        sql.ejecutar(SQL_ELIMINAR_SaldoFlujoCaja);
    }

    @Override
    public void eliminarOperacionSpotProceso() throws SQLException {
        sql.ejecutar(SQL_ELIMINAR_CrearOperacionSpot_Proceso);
    }

    @Override
    public void revertir() throws SQLException {
        sql.revertir();
    }
}
