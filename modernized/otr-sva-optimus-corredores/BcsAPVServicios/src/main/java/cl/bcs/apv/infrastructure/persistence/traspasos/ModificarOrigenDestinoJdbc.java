package cl.bcs.apv.infrastructure.persistence.traspasos;

import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import cl.bcs.apv.application.comun.Sneaky;
import cl.bcs.apv.domain.port.out.traspasos.ModificarOrigenDestinoRepositorio;

/** JDBC adapter of {@link ModificarOrigenDestinoRepositorio}; the SQL file is the legacy one, byte for byte. */
@Repository
public class ModificarOrigenDestinoJdbc implements ModificarOrigenDestinoRepositorio {

    private static final String SQL_UPDATE = SqlTraspasos.recurso("ModificarOrigenDestino");

    private final SqlTraspasos sql;

    public ModificarOrigenDestinoJdbc(NamedParameterJdbcTemplate jdbc) {
        this.sql = new SqlTraspasos(jdbc);
    }

    @Override
    public int[] modificar(OrigenDestino fila) {
        List<OrigenDestino> lote = new ArrayList<>();
        lote.add(fila);
        return Sneaky.sinChequear(() -> sql.loteConConteos(SQL_UPDATE, lote));
    }
}
