package cl.bcs.socionegocio.jobs.adapter.out;

import cl.bcs.socionegocio.jobs.domain.ClienteABloquear;
import cl.bcs.socionegocio.jobs.domain.DocumentoCliente;
import cl.bcs.socionegocio.jobs.port.out.SocioNegocioJobsDataPort;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * HANA-backed (H2 in tests) implementation of SocioNegocioJobsDataPort.
 *
 * SQL statements are inline strings; no external .sql files are used in the
 * modernized version to keep the module self-contained.
 */
@Repository
public class SocioNegocioJobsRepositoryAdapter implements SocioNegocioJobsDataPort {

    private final NamedParameterJdbcTemplate jdbc;

    SocioNegocioJobsRepositoryAdapter(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<DocumentoCliente> consultarDocumentosPorVencer() {
        var sql = """
                SELECT ID_CLIENTE, ID_ANEXO, ID_ESTADO, BLOQUEAR_AL_VENCER,
                       FECHA_VIGENCIA, FECHA_MODIFICACION
                FROM DOCUMENTOS_CLIENTE_T
                WHERE FECHA_VIGENCIA IS NOT NULL
                """;
        return jdbc.query(sql, new MapSqlParameterSource(), (rs, n) -> new DocumentoCliente(
                rs.getLong("ID_CLIENTE"),
                rs.getLong("ID_ANEXO"),
                rs.getString("ID_ESTADO"),
                rs.getString("BLOQUEAR_AL_VENCER"),
                rs.getObject("FECHA_VIGENCIA", LocalDate.class),
                rs.getObject("FECHA_MODIFICACION", LocalDate.class)
        ));
    }

    @Override
    public void actualizarAnexos(List<Long> idAnexos) {
        if (idAnexos == null || idAnexos.isEmpty()) {
            return;
        }
        var sql = "UPDATE DOCUMENTOS_CLIENTE_T SET ID_ESTADO='VEN' WHERE ID_ANEXO IN (:ids)";
        jdbc.update(sql, new MapSqlParameterSource("ids", idAnexos));
    }

    @Override
    public void bloquearClientes(List<ClienteABloquear> clientes) {
        if (clientes == null || clientes.isEmpty()) {
            return;
        }
        var sql = """
                UPDATE CLIENTES_T
                SET ID_TIPO_BLOQUEO = :idTipoBloqueo,
                    COMENTARIO_BLOQUEO = :comentario
                WHERE ID_CLIENTE = :idCliente
                """;
        SqlParameterSource[] batch = clientes.stream()
                .map(c -> new MapSqlParameterSource()
                        .addValue("idTipoBloqueo", c.idTipoBloqueo())
                        .addValue("comentario", c.comentarioBloqueo())
                        .addValue("idCliente", c.idCliente()))
                .toArray(SqlParameterSource[]::new);
        jdbc.batchUpdate(sql, batch);
    }

    @Override
    public String obtenerFechaSistema(Long idEmpresa) {
        var sql = """
                SELECT VALOR FROM CONFIGURACION_T
                WHERE ID_EMPRESA = :idEmpresa AND CLAVE = 'PLA.FechaSistema'
                """;
        return jdbc.queryForObject(sql,
                new MapSqlParameterSource("idEmpresa", idEmpresa),
                String.class);
    }

    @Override
    public int obtenerDiasVencimiento(Long idEmpresa) {
        var sql = """
                SELECT CAST(VALOR AS INTEGER) FROM CONFIGURACION_T
                WHERE ID_EMPRESA = :idEmpresa AND CLAVE = 'PLA.NumeroDiasFechaVencimiento'
                """;
        Integer result = jdbc.queryForObject(sql,
                new MapSqlParameterSource("idEmpresa", idEmpresa),
                Integer.class);
        return result != null ? result : 0;
    }

    @Override
    public void purgaProspectosClientes(Long idEmpresa, Long idUsuario) {
        throw new UnsupportedOperationException(
                "requires HANA SP: SP_PURGA_PROSPECTO_CLIENTE — not executable on H2 or without HANA");
    }
}
