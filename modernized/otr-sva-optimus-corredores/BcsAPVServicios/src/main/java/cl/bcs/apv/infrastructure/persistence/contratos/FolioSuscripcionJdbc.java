package cl.bcs.apv.infrastructure.persistence.contratos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import cl.bcs.apv.application.comun.Sneaky;
import cl.bcs.apv.domain.port.out.contratos.FolioSuscripcionRepositorio;
import cl.bcs.apv.infrastructure.jdbc.SqlNombrado;

@Repository
public class FolioSuscripcionJdbc implements FolioSuscripcionRepositorio {

    private static final String SQL_SELECT = SqlNombrado.recurso("ObtenerFolioSuscripcion_SelectForUpdate");
    private static final String SQL_INSERT = SqlNombrado.recurso("ObtenerFolioSuscripcion_InsertFolioSuscripcion");

    /** Batch row: the parameters are read by name from the record components. */
    public record Fila(String p_Canal, Long p_Folio) {
    }

    private final SqlNombrado sql;

    public FolioSuscripcionJdbc(NamedParameterJdbcTemplate jdbc) {
        this.sql = new SqlNombrado(jdbc);
    }

    @Override
    public List<FolioFila> seleccionarPorCanal(String canal) {
        HashMap<String, Object> p = new HashMap<>();
        p.put("p_Canal", canal);
        return Sneaky.sinChequear(() -> sql.consultar(SQL_SELECT, p, FolioFila.class));
    }

    @Override
    public void insertar(String canal, Long folio) {
        List<Fila> filas = new ArrayList<>();
        filas.add(new Fila(canal, folio));
        Sneaky.sinChequear(() -> sql.lote(SQL_INSERT, filas));
    }
}
