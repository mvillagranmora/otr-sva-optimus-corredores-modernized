package cl.bcs.apv.infrastructure.persistence.contratos;

import java.sql.SQLException;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import cl.bcs.apv.application.comun.Sneaky;
import cl.bcs.apv.domain.port.out.contratos.CargaMasivaRepositorio;
import cl.bcs.apv.infrastructure.jdbc.SqlNombrado;

@Repository
public class CargaMasivaJdbc implements CargaMasivaRepositorio {

    private static final String SQL_INSERT = SqlNombrado.recurso("CargaMasivaArchivo_Insert");

    private final NamedParameterJdbcTemplate jdbc;

    public CargaMasivaJdbc(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public int[] insertarArchivos(List<CargaArchivoFila> filas) {
        SqlParameterSource[] fuentes = filas.stream().map(f -> (SqlParameterSource) new MapSqlParameterSource()
                .addValue("IdEmpresa", f.IdEmpresa()).addValue("IdCargaMasiva", f.IdCargaMasiva()).addValue("NombreCargaMasiva", f.NombreCargaMasiva())
                .addValue("CantidadRegistros", f.CantidadRegistros()).addValue("IdUsuario", f.IdUsuario()).addValue("IdTipoCarga", f.IdTipoCarga())
                .addValue("Descripcion", f.Descripcion()).addValue("FechaCarga", f.FechaCarga()).addValue("IdEstado", f.IdEstado()))
                .toArray(SqlParameterSource[]::new);
        try {
            return jdbc.batchUpdate(SQL_INSERT, fuentes);
        } catch (DataAccessException e) {
            // legacy SQL.batch surfaced a SQLException (the driver one, or one carrying the message)
            Throwable causa = e.getMostSpecificCause();
            throw Sneaky.lanzar(causa instanceof SQLException sql ? new SQLException(sql.getMessage(), sql.getSQLState(), sql.getErrorCode()) : new SQLException(e.getMessage(), e));
        }
    }
}
