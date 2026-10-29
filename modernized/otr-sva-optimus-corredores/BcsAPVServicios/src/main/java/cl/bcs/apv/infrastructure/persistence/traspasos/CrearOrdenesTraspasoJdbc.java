package cl.bcs.apv.infrastructure.persistence.traspasos;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import cl.bcs.apv.application.comun.Sneaky;
import cl.bcs.apv.domain.port.out.traspasos.CrearOrdenesTraspasoRepositorio;

/** JDBC adapter of {@link CrearOrdenesTraspasoRepositorio}; the SQL file is the legacy one, byte for byte. */
@Repository
public class CrearOrdenesTraspasoJdbc implements CrearOrdenesTraspasoRepositorio {

    private static final String SQL_INSERT = SqlTraspasos.recurso("OrdenTraspaso_Insert");

    private final SqlTraspasos sql;

    public CrearOrdenesTraspasoJdbc(NamedParameterJdbcTemplate jdbc) {
        this.sql = new SqlTraspasos(jdbc);
    }

    @Override
    public int insertar(Long idTraspaso, Long idOrden, Long idSolicitudAporteRescate) {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("IdTraspaso", idTraspaso);
        p.put("IdOrden", idOrden);
        p.put("IdSolicitudAporteRescate", idSolicitudAporteRescate);
        return Sneaky.sinChequear(() -> sql.actualizar(SQL_INSERT, p));
    }
}
