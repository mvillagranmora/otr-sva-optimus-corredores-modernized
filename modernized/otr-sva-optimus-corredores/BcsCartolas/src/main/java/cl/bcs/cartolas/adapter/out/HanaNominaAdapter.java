package cl.bcs.cartolas.adapter.out;

import cl.bcs.cartolas.domain.Nomina;
import cl.bcs.cartolas.domain.TipoProceso;
import cl.bcs.cartolas.port.out.NominaPort;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Adaptador HANA para operaciones de Nómina (tabla CAR.Nomina).
 */
@Repository
public class HanaNominaAdapter implements NominaPort {

    private final NamedParameterJdbcTemplate jdbc;

    public HanaNominaAdapter(final NamedParameterJdbcTemplate jdbc) {
        this.jdbc = Objects.requireNonNull(jdbc, "jdbc es requerido");
    }

    @Override
    public Optional<Nomina> findById(final long idNomina) {
        final String sql = """
                SELECT id_nomina, id_empresa, id_usuario, id_plantilla,
                       periodo, fecha_desde, fecha_hasta, comentario,
                       certificada, fecha_certificacion,
                       id_usuario_certificador, nombre_usuario_certificador, nota_certificacion,
                       tipo_proceso, estado_proceso,
                       fecha_programada_proceso, fecha_inicio_proceso, fecha_termino_proceso,
                       mensaje_proceso
                FROM car_nomina
                WHERE id_nomina = :idNomina
                """;
        final MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("idNomina", idNomina);

        return jdbc.query(sql, params, this::mapRow).stream().findFirst();
    }

    @Override
    public List<Nomina> findByTipoProceso(final String tipoProceso) {
        Objects.requireNonNull(tipoProceso, "tipoProceso es requerido");
        final String sql = """
                SELECT id_nomina, id_empresa, id_usuario, id_plantilla,
                       periodo, fecha_desde, fecha_hasta, comentario,
                       certificada, fecha_certificacion,
                       id_usuario_certificador, nombre_usuario_certificador, nota_certificacion,
                       tipo_proceso, estado_proceso,
                       fecha_programada_proceso, fecha_inicio_proceso, fecha_termino_proceso,
                       mensaje_proceso
                FROM car_nomina
                WHERE tipo_proceso = :tipoProceso
                """;
        final MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("tipoProceso", tipoProceso);

        return jdbc.query(sql, params, this::mapRow);
    }

    @Override
    public Optional<Nomina> findRecurrente(final long idEmpresa, final String periodo) {
        Objects.requireNonNull(periodo, "periodo es requerido");
        final String sql = """
                SELECT id_nomina, id_empresa, id_usuario, id_plantilla,
                       periodo, fecha_desde, fecha_hasta, comentario,
                       certificada, fecha_certificacion,
                       id_usuario_certificador, nombre_usuario_certificador, nota_certificacion,
                       tipo_proceso, estado_proceso,
                       fecha_programada_proceso, fecha_inicio_proceso, fecha_termino_proceso,
                       mensaje_proceso
                FROM car_nomina
                WHERE id_empresa   = :idEmpresa
                  AND tipo_proceso = :tipoProceso
                  AND periodo      = :periodo
                """;
        final MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("idEmpresa",   idEmpresa)
                .addValue("tipoProceso", TipoProceso.RECURRENTE)
                .addValue("periodo",     periodo);

        return jdbc.query(sql, params, this::mapRow).stream().findFirst();
    }

    @Override
    public long save(final Nomina nomina, final long idEmpresa, final long idUsuario) {
        Objects.requireNonNull(nomina, "nomina es requerido");
        // SP HANA: CAR.GrabarNomina — requiere HANA
        throw new UnsupportedOperationException(
                "save(Nomina) requiere SAP HANA — SP: CAR.GrabarNomina");
    }

    private Nomina mapRow(final java.sql.ResultSet rs, final int rowNum) throws java.sql.SQLException {
        final Nomina n = new Nomina();
        n.setId(rs.getLong("id_nomina"));
        n.setIdEmpresa(rs.getLong("id_empresa"));
        n.setIdUsuario(rs.getLong("id_usuario"));
        n.setIdPlantilla(rs.getLong("id_plantilla"));
        n.setPeriodo(rs.getString("periodo"));
        final java.sql.Date fd = rs.getDate("fecha_desde");
        if (fd != null) n.setFechaDesde(fd.toLocalDate());
        final java.sql.Date fh = rs.getDate("fecha_hasta");
        if (fh != null) n.setFechaHasta(fh.toLocalDate());
        n.setComentario(rs.getString("comentario"));
        n.setCertificada(rs.getString("certificada"));
        final java.sql.Date fc = rs.getDate("fecha_certificacion");
        if (fc != null) n.setFechaCertificacion(fc.toLocalDate());
        n.setIdUsuarioCertificador(rs.getLong("id_usuario_certificador"));
        n.setNombreUsuarioCertificador(rs.getString("nombre_usuario_certificador"));
        n.setNotaCertificacion(rs.getString("nota_certificacion"));
        n.setTipoProceso(rs.getString("tipo_proceso"));
        n.setEstadoProceso(rs.getString("estado_proceso"));
        final java.sql.Date fp = rs.getDate("fecha_programada_proceso");
        if (fp != null) n.setFechaProgramadaProceso(fp.toLocalDate());
        final java.sql.Date fi = rs.getDate("fecha_inicio_proceso");
        if (fi != null) n.setFechaInicioProceso(fi.toLocalDate());
        final java.sql.Date ft = rs.getDate("fecha_termino_proceso");
        if (ft != null) n.setFechaTerminoProceso(ft.toLocalDate());
        n.setMensajeProceso(rs.getString("mensaje_proceso"));
        return n;
    }
}
