package cl.bcs.normativa.adapter.out;

import cl.bcs.normativa.domain.Documento;
import cl.bcs.normativa.port.out.DocumentoPort;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Adaptador JDBC para Documento normativo.
 * HANA table: "BSCL.Normativa.BD::DOC.Documento" (alias DOCUMENTO en H2 test)
 */
@Repository
public class DocumentoJdbcAdapter implements DocumentoPort {

    private final NamedParameterJdbcTemplate jdbc;

    public DocumentoJdbcAdapter(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<Documento> buscarPorId(long id) {
        var sql = """
                SELECT IdDocumento, IdTipoDocumento, Esquema, Version,
                       IdEmpresa, FechaLimite, IdEstadoDocumento, FechaCreacion,
                       FechaPresentacion, DocumentoIdentidadDeclarante,
                       DocumentoIdentidadRepresentante, DireccionElectronicaDeclaracion,
                       DireccionFisicaDeclaracion, ComunaDeclaracion, TelefonoDeclaracion,
                       FolioDeclaracion, EnProceso, FechaInicioUltimoProceso
                FROM DOCUMENTO
                WHERE IdDocumento = :id
                """;
        List<Documento> result = jdbc.query(sql, Map.of("id", id), this::mapDocumento);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public void setEnProceso(long idDocumento, boolean enProceso) {
        if (enProceso) {
            var sql = """
                    UPDATE DOCUMENTO
                    SET EnProceso = 'S', FechaInicioUltimoProceso = CURRENT_TIMESTAMP
                    WHERE IdDocumento = :id
                    """;
            jdbc.update(sql, Map.of("id", idDocumento));
        } else {
            jdbc.update("UPDATE DOCUMENTO SET EnProceso = 'N' WHERE IdDocumento = :id",
                    Map.of("id", idDocumento));
        }
    }

    private Documento mapDocumento(ResultSet rs, int row) throws SQLException {
        var d = new Documento();
        d.setId(rs.getLong("IdDocumento"));
        d.setIdTipoDocumento(rs.getString("IdTipoDocumento"));
        d.setNombreEsquema(rs.getString("Esquema"));
        d.setVersionEsquema(rs.getString("Version"));
        d.setIdEmpresa(rs.getLong("IdEmpresa"));
        d.setFechaLimite(rs.getDate("FechaLimite"));
        d.setIdEstadoDocumento(rs.getString("IdEstadoDocumento"));
        d.setFechaCreacion(rs.getDate("FechaCreacion"));
        d.setFechaPresentacion(rs.getDate("FechaPresentacion"));
        d.setRutDeclarante(rs.getString("DocumentoIdentidadDeclarante"));
        d.setRutRepresentante(rs.getString("DocumentoIdentidadRepresentante"));
        d.setEmailDeclaracion(rs.getString("DireccionElectronicaDeclaracion"));
        d.setDireccionDeclaracion(rs.getString("DireccionFisicaDeclaracion"));
        d.setComunaDeclaracion(rs.getString("ComunaDeclaracion"));
        d.setTelefonoDeclaracion(rs.getString("TelefonoDeclaracion"));
        d.setFolioDeclaracion(rs.getString("FolioDeclaracion"));
        d.setEnProceso("S".equals(rs.getString("EnProceso")));
        d.setFechaInicioUltimoProceso(rs.getTimestamp("FechaInicioUltimoProceso"));
        return d;
    }
}
