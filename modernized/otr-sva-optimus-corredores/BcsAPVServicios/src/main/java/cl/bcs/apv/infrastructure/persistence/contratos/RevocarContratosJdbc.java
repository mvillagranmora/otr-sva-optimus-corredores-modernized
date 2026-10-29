package cl.bcs.apv.infrastructure.persistence.contratos;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import cl.bcs.apv.application.comun.Sneaky;
import cl.bcs.apv.domain.port.out.contratos.RevocarContratosRepositorio;
import cl.bcs.apv.infrastructure.jdbc.SqlNombrado;

/** JDBC adapter of {@link RevocarContratosRepositorio}; the SQL file is the legacy one, byte for byte. */
@Repository
public class RevocarContratosJdbc implements RevocarContratosRepositorio {

    private static final String SQL_VALIDACIONES = SqlNombrado.recurso("ContratoSAAPV_Revocacion_SelectValidaciones");

    private final SqlNombrado sql;

    public RevocarContratosJdbc(NamedParameterJdbcTemplate jdbc) {
        this.sql = new SqlNombrado(jdbc);
    }

    @Override
    public List<ResultadoValidaciones> contratoVigente(String folioSAAPV) {
        Map<String, Object> p = new HashMap<>();
        p.put("FolioSAAPV", folioSAAPV);
        return Sneaky.sinChequear(() -> sql.consultar(SQL_VALIDACIONES, p, ResultadoValidaciones.class));
    }
}
