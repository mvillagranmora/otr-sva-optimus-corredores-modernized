package cl.bcs.contabilidad.servicios;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;

import java.util.List;

import static cl.bcs.contabilidad.servicios.RecordingJdbc.row;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Proves the harness itself can fail and behaves like real Spring JDBC (CONT-G-000). */
class RecordingJdbcHarnessTest {

    public static class Bean { private Long idCentralizacion; public Long getIdCentralizacion() { return idCentralizacion; } public void setIdCentralizacion(Long v) { idCentralizacion = v; } }
    public static class PublicFields { public Long IdCentralizacion; }

    @Test
    @DisplayName("CONT-G-000a harness: legacy statement identified, rows returned, params recorded")
    void contG000a_routesByLegacyText() {
        RecordingJdbc jdbc = new RecordingJdbc().rows("AnularCentralizacion_SelectCentralizacion.sql", row("IdCentralizacion", 7L));
        List<Bean> r = jdbc.query(LegacySql.text("AnularCentralizacion_SelectCentralizacion.sql"),
                new MapSqlParameterSource("IdCentralizacion", 7L), BeanPropertyRowMapper.newInstance(Bean.class));
        assertThat(r).extracting(Bean::getIdCentralizacion).containsExactly(7L);
        assertThat(jdbc.single("AnularCentralizacion_SelectCentralizacion.sql").params()).containsEntry("IdCentralizacion", 7L);
    }

    @Test
    @DisplayName("CONT-G-000b harness: blank SQL fails like a real driver")
    void contG000b_blankSqlFails() {
        RecordingJdbc jdbc = new RecordingJdbc();
        assertThatThrownBy(() -> jdbc.queryForList("", new MapSqlParameterSource())).isInstanceOf(DataAccessException.class);
    }

    @Test
    @DisplayName("CONT-G-000c harness: batch of public-field POJOs fails with Spring's 'No value supplied'")
    void contG000c_publicFieldBatchFails() {
        RecordingJdbc jdbc = new RecordingJdbc();
        PublicFields p = new PublicFields();
        p.IdCentralizacion = 1L;
        assertThatThrownBy(() -> jdbc.batchUpdate(LegacySql.text("AnularCentralizacion_UpdateEvento.sql"),
                SqlParameterSourceUtils.createBatch(List.of(p))))
                .isInstanceOf(InvalidDataAccessApiUsageException.class).hasMessageContaining("No value supplied");
    }

    @Test
    @DisplayName("CONT-G-000d harness: sequences honour :cantidad and failOn raises a DataAccessException")
    void contG000d_sequenceAndFailure() {
        RecordingJdbc jdbc = new RecordingJdbc().sequence("BSCL.Contabilidad.BD.Secuencias::IdCentralizacion", 9001, "Id")
                .failOn("AnularCentralizacion_UpdateEvento.sql");
        List<Long> ids = jdbc.queryForList("SELECT \"BSCL.Contabilidad.BD.Secuencias::IdCentralizacion\".NEXTVAL AS \"Id\" FROM SERIES_GENERATE_BIGINT(1, 0, :cantidad)",
                new MapSqlParameterSource("cantidad", 2L), Long.class);
        assertThat(ids).containsExactly(9001L, 9002L);
        assertThatThrownBy(() -> jdbc.update(LegacySql.text("AnularCentralizacion_UpdateEvento.sql"), new MapSqlParameterSource("IdCentralizacion", 1L)))
                .isInstanceOf(DataAccessException.class);
    }
}
