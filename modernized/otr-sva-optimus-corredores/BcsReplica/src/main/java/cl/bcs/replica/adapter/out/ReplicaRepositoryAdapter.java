package cl.bcs.replica.adapter.out;

import cl.bcs.replica.domain.Workstack;
import cl.bcs.replica.port.out.ReplicaDataPort;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;

/**
 * JDBC adapter for WORKSTACK_T and CONFIGURACION_T.
 *
 * ejecutarRespaldoSebra always throws UnsupportedOperationException — the
 * DELETE+INSERT across 13 BSCL tables (TBCTCIRV, TBCTCURV, etc.) require
 * a live HANA connection and are @Disabled in tests.
 */
@Repository
public class ReplicaRepositoryAdapter implements ReplicaDataPort {

    private final NamedParameterJdbcTemplate jdbc;

    public ReplicaRepositoryAdapter(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void insertarWorkstack(Workstack workstack) {
        String sql = """
                INSERT INTO WORKSTACK_T (KEY_VALUE, TABLE_NAME, ACTION, STATUS, STAMP, PARAMETERS)
                VALUES (:key, :table, :action, :status, :stamp, :parameters)
                """;
        var params = new MapSqlParameterSource()
                .addValue("key",        workstack.key())
                .addValue("table",      workstack.table())
                .addValue("action",     workstack.action())
                .addValue("status",     workstack.status())
                .addValue("stamp",      workstack.stamp() != null
                        ? Timestamp.from(workstack.stamp()) : null)
                .addValue("parameters", workstack.parameters());
        jdbc.update(sql, params);
    }

    @Override
    public String obtenerFechaSistema(Long idEmpresa) {
        String sql = """
                SELECT VALOR FROM CONFIGURACION_T
                WHERE CLAVE = 'PLA.FechaSistema'
                  AND ID_EMPRESA = :idEmpresa
                """;
        return jdbc.queryForObject(sql,
                new MapSqlParameterSource("idEmpresa", idEmpresa),
                String.class);
    }

    @Override
    public String obtenerParametroRespaldoSebra(Long idEmpresa) {
        String sql = """
                SELECT VALOR FROM CONFIGURACION_T
                WHERE CLAVE = 'PLA.RespaldoReplicaTablasSebra'
                  AND ID_EMPRESA = :idEmpresa
                """;
        return jdbc.queryForObject(sql,
                new MapSqlParameterSource("idEmpresa", idEmpresa),
                String.class);
    }

    @Override
    public void ejecutarRespaldoSebra(String codPer) {
        throw new UnsupportedOperationException(
                "requires HANA: BSCL.Replica tables DELETE+INSERT");
    }
}
