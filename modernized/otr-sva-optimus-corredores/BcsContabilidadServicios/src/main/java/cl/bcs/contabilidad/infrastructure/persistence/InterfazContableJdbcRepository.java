package cl.bcs.contabilidad.infrastructure.persistence;

import cl.bcs.contabilidad.domain.model.sap.HomologaRespuestaSap;
import cl.bcs.contabilidad.domain.port.out.InterfazContableRepository;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.CallableStatement;
import java.sql.Types;
import java.util.List;

/**
 * {@link InterfazContableRepository} on the application's HANA datasource. The statements are the legacy Java
 * constants of jobs/InterfazContable.java (:43-44, :232), verbatim. The procedure call registers only the two
 * counter OUT parameters, as legacy did; it is not wrapped in a transaction, so with the pool's auto-commit each call
 * is committed on its own, like legacy's explicit {@code commit()} per pre-voucher.
 */
@Repository
public class InterfazContableJdbcRepository implements InterfazContableRepository {

    static final String SELECT_MENSAJES = "SELECT \"Codigo\", \"Valor\", \"Mensaje\" FROM \"BSCL.Contabilidad.BD::ITZ.HomologaRespuestaSap\"";
    static final String UPDATE_PRECOMPROBANTES = "UPDATE \"BSCL.Contabilidad.BD::ITZ.PreComprobante\" SET \"Estado\" = 'E' WHERE \"IdPreComprobante\" = :IdPreComprobante";
    static final String CALL_ACTUALIZAR_ESTADO = "CALL \"BSCL.Contabilidad.BD.Servicios::ITZ.ActualizarEstadoPreComprobante\" (?,?,?,?,?,?,?,?,?)";

    private final NamedParameterJdbcTemplate jdbc;

    public InterfazContableJdbcRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<HomologaRespuestaSap> consultarHomologaRespuestaSap() {
        return jdbc.query(SELECT_MENSAJES, new MapSqlParameterSource(), new DataClassRowMapper<>(HomologaRespuestaSap.class));
    }

    @Override
    public void actualizarEstadoContabilizado(long idEmpresa, long idUsuario, long idPreComprobante) {
        jdbc.getJdbcTemplate().execute(CALL_ACTUALIZAR_ESTADO, (CallableStatement cs) -> {
            cs.setLong(1, idEmpresa);
            cs.setLong(2, idUsuario);
            cs.setString(3, "CON");
            cs.setString(4, "N");
            cs.setLong(5, idPreComprobante);
            cs.registerOutParameter(6, Types.INTEGER); // advertencias
            cs.registerOutParameter(7, Types.INTEGER); // errores
            cs.execute();
            return null;
        });
    }

    @Override
    public void marcarConError(List<Long> idsPreComprobante) {
        jdbc.batchUpdate(UPDATE_PRECOMPROBANTES, SqlParams.batch(idsPreComprobante,
                id -> new MapSqlParameterSource("IdPreComprobante", id)));
    }
}
