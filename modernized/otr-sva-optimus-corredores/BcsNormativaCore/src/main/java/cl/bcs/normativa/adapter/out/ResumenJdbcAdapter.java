package cl.bcs.normativa.adapter.out;

import cl.bcs.normativa.domain.ResumenCarga;
import cl.bcs.normativa.domain.ResumenGeneracion;
import cl.bcs.normativa.port.out.ResumenCargaPort;
import cl.bcs.normativa.port.out.ResumenGeneracionPort;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Date;

/**
 * Adaptador JDBC para ResumenCarga y ResumenGeneracion.
 * Implementa el patron SecuenciadorRegistro (MAX(ID)+1) de la clase legacy.
 * HANA tables:
 *   "BSCL.Normativa.BD::TRI.ResumenCarga"      (alias RESUMEN_CARGA en H2)
 *   "BSCL.Normativa.BD::TRI.ResumenGeneracion" (alias RESUMEN_GENERACION en H2)
 */
@Repository
public class ResumenJdbcAdapter implements ResumenCargaPort, ResumenGeneracionPort {

    private final NamedParameterJdbcTemplate jdbc;

    public ResumenJdbcAdapter(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // =========================================================================
    // ResumenCargaPort
    // =========================================================================

    @Override
    public void insertar(ResumenCarga resumen) {
        long id = nextId("RESUMEN_CARGA");
        resumen.setId(id);
        Timestamp ts = resumen.getFecha() != null
                ? new Timestamp(resumen.getFecha().getTime())
                : new Timestamp(System.currentTimeMillis());
        var sql = """
                INSERT INTO RESUMEN_CARGA
                  (ID, IdProceso, IdDocumento, IdUsuario, Fecha, Glosa, NombreArchivo,
                   TotalRegistrosLeidos, TotalRegistrosCargados, TotalRegistrosErroneos)
                VALUES
                  (:id, :idProceso, :idDocumento, :idUsuario, :fecha, :glosa, :archivo,
                   :leidos, :cargados, :erroneos)
                """;
        jdbc.update(sql, new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("idProceso", resumen.getIdProceso())
                .addValue("idDocumento", resumen.getIdDocumento())
                .addValue("idUsuario", resumen.getIdUsuario())
                .addValue("fecha", ts)
                .addValue("glosa", resumen.getGlosa())
                .addValue("archivo", resumen.getNombreArchivo())
                .addValue("leidos", resumen.getTotalRegistrosLeidos())
                .addValue("cargados", resumen.getTotalRegistrosCargados())
                .addValue("erroneos", resumen.getTotalRegistrosErroneos()));
    }

    @Override
    public void actualizar(ResumenCarga resumen) {
        var sql = """
                UPDATE RESUMEN_CARGA
                SET Glosa = :glosa,
                    TotalRegistrosLeidos = :leidos,
                    TotalRegistrosCargados = :cargados,
                    TotalRegistrosErroneos = :erroneos
                WHERE ID = :id
                """;
        jdbc.update(sql, new MapSqlParameterSource()
                .addValue("id", resumen.getId())
                .addValue("glosa", resumen.getGlosa())
                .addValue("leidos", resumen.getTotalRegistrosLeidos())
                .addValue("cargados", resumen.getTotalRegistrosCargados())
                .addValue("erroneos", resumen.getTotalRegistrosErroneos()));
    }

    // =========================================================================
    // ResumenGeneracionPort
    // =========================================================================

    @Override
    public void insertar(ResumenGeneracion resumen) {
        long id = nextId("RESUMEN_GENERACION");
        resumen.setId(id);
        Timestamp ts = resumen.getFecha() != null
                ? new Timestamp(resumen.getFecha().getTime())
                : new Timestamp(System.currentTimeMillis());
        var sql = """
                INSERT INTO RESUMEN_GENERACION
                  (ID, IdProceso, IdDocumento, IdUsuario, Fecha, Glosa, NombreArchivo,
                   TotalRegistrosGenerados, UltimoCorrelativo)
                VALUES
                  (:id, :idProceso, :idDocumento, :idUsuario, :fecha, :glosa, :archivo,
                   :generados, :correlativo)
                """;
        jdbc.update(sql, new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("idProceso", resumen.getIdProceso())
                .addValue("idDocumento", resumen.getIdDocumento())
                .addValue("idUsuario", resumen.getIdUsuario())
                .addValue("fecha", ts)
                .addValue("glosa", resumen.getGlosa())
                .addValue("archivo", resumen.getNombreArchivo())
                .addValue("generados", resumen.getTotalRegistrosGenerados())
                .addValue("correlativo", resumen.getUltimoCorrelativo()));
    }

    @Override
    public void actualizar(ResumenGeneracion resumen) {
        var sql = """
                UPDATE RESUMEN_GENERACION
                SET Glosa = :glosa,
                    TotalRegistrosGenerados = :generados,
                    UltimoCorrelativo = :correlativo
                WHERE ID = :id
                """;
        jdbc.update(sql, new MapSqlParameterSource()
                .addValue("id", resumen.getId())
                .addValue("glosa", resumen.getGlosa())
                .addValue("generados", resumen.getTotalRegistrosGenerados())
                .addValue("correlativo", resumen.getUltimoCorrelativo()));
    }

    // =========================================================================
    // Utilidades
    // =========================================================================

    /** Genera el siguiente ID usando MAX(ID)+1, replicando SecuenciadorRegistro del legacy. */
    private long nextId(String tabla) {
        String sql = "SELECT COALESCE(MAX(ID), 0) + 1 FROM " + tabla;
        Long id = jdbc.getJdbcTemplate().queryForObject(sql, Long.class);
        return id != null ? id : 1L;
    }
}
