package cl.bcs.apv.infrastructure.persistence.contratos;

import java.util.HashMap;
import java.util.List;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import cl.bcs.apv.application.comun.Sneaky;
import cl.bcs.apv.domain.port.out.contratos.RecaudacionRezagoRepositorio;
import cl.bcs.apv.infrastructure.jdbc.SqlNombrado;

@Repository
public class RecaudacionRezagoJdbc implements RecaudacionRezagoRepositorio {

    private static final String SQL_CREATE = SqlNombrado.recurso("ValidarYAsignarContratosARecaudaciones_CrearTablaTemporal");
    private static final String SQL_DELETE = SqlNombrado.recurso("ValidarYAsignarContratosARecaudaciones_BorrarTablaTemporal");
    private static final String SQL_DROP = SqlNombrado.recurso("ValidarYAsignarContratosARecaudaciones_DropTablaTemporal");
    private static final String SQL_INSERT = SqlNombrado.recurso("ValidarYAsignarContratosARecaudaciones_InsertarATablaTemporal");
    private static final String SQL_REZAGO = SqlNombrado.recurso("ValidarYAsignarContratosARecaudaciones_ConsultarDatosRecaudacionesEnRezago");

    private final SqlNombrado sql;

    public RecaudacionRezagoJdbc(NamedParameterJdbcTemplate jdbc) {
        this.sql = new SqlNombrado(jdbc);
    }

    @Override
    public List<DatosRecaudaciones> recaudacionesEnRezago(String fechaDesde, String fechaHasta) {
        HashMap<String, Object> p = new HashMap<>();
        p.put("p_FechaDesde", fechaDesde);
        p.put("p_FechaHasta", fechaHasta);
        return Sneaky.sinChequear(() -> sql.consultar(SQL_REZAGO, p, DatosRecaudaciones.class));
    }

    @Override
    public void crearTablaTemporal() {
        Sneaky.sinChequear(() -> sql.ejecutar(SQL_CREATE));
    }

    @Override
    public void borrarTablaTemporal(List<DatosRecaudaciones> filas) {
        lote(SQL_DELETE, filas);
    }

    @Override
    public void insertarATablaTemporal(List<DatosRecaudaciones> filas) {
        lote(SQL_INSERT, filas);
    }

    @Override
    public void dropTablaTemporal(List<DatosRecaudaciones> filas) {
        lote(SQL_DROP, filas);
    }

    /**
     * Legacy {@code SQL.batch} rethrew a PLAIN {@code SQLException} (cause message, " Query: ", sql, SQLState and error code kept; the driver exception,
     * e.g. a JdbcBatchUpdateException, as the next exception). The goldens pin the simple class name, so the batch failure is surfaced the same way.
     * (Candidate to move into SqlNombrado.lote for every adapter.)
     */
    private void lote(String texto, List<DatosRecaudaciones> filas) {
        try {
            sql.lote(texto, filas);
        } catch (java.sql.SQLException e) {
            java.sql.SQLException plano = new java.sql.SQLException(e.getMessage() + " Query: " + texto, e.getSQLState(), e.getErrorCode());
            plano.setNextException(e);
            throw Sneaky.lanzar(plano);
        }
    }
}
