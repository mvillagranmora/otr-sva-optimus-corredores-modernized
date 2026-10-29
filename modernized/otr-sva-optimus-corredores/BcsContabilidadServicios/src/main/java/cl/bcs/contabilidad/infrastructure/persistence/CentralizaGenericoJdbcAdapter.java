package cl.bcs.contabilidad.infrastructure.persistence;

import cl.bcs.contabilidad.domain.port.out.CentralizaGenericoPort;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * JDBC adapter of {@link CentralizaGenericoPort}: the legacy statements of CentralizaGenerico.java:37-51, loaded
 * verbatim from cl/bcs/contabilidad/sql/, with explicit parameter binding (legacy {@code SQL.batch} bound each
 * {@code :Name} from the POJO field of that name).
 *
 * <p>An empty IN list returns no rows without running the statement (legacy bound {@code IN (NULL)}); an empty batch
 * writes nothing. FechaProceso / HoraProceso come from the database UTC clock, as legacy FechaDB did with
 * {@code SELECT CURRENT_UTCTIMESTAMP as UTC FROM DUMMY}.
 */
@Repository
public class CentralizaGenericoJdbcAdapter implements CentralizaGenericoPort {

    private static final String SQL_SELECT_TIPO_CENTRALIZACION = SqlResources.load("cl/bcs/contabilidad/sql/CentralizaGenerico_SelectTipoCentralizacion.sql");
    private static final String SQL_SELECT_CENTRALIZACION = SqlResources.load("cl/bcs/contabilidad/sql/CentralizaGenerico_SelectCentralizacion.sql");
    private static final String SQL_SELECT_MOVIMIENTO = SqlResources.load("cl/bcs/contabilidad/sql/CentralizaGenerico_SelectMovimiento.sql");
    private static final String SQL_SELECT_EVENTO_TIPO = SqlResources.load("cl/bcs/contabilidad/sql/CentralizaGenerico_SelectEventoTipo.sql");
    private static final String SQL_SELECT_EVENTO_TIPO_DATO = SqlResources.load("cl/bcs/contabilidad/sql/CentralizaGenerico_SelectEventoTipoDato.sql");
    private static final String SQL_SELECT_EVENTO_TIPO_DATO_VALOR = SqlResources.load("cl/bcs/contabilidad/sql/CentralizaGenerico_SelectEventoTipoDatoValor.sql");
    private static final String SQL_INSERT_CENTRALIZACION = SqlResources.load("cl/bcs/contabilidad/sql/CentralizaGenerico_InsertCentralizacion.sql");
    private static final String SQL_INSERT_MOVIMIENTO = SqlResources.load("cl/bcs/contabilidad/sql/CentralizaGenerico_InsertMovimiento.sql");
    private static final String SQL_INSERT_MOVIMIENTO_DATO = SqlResources.load("cl/bcs/contabilidad/sql/CentralizaGenerico_InsertMovimientoDato.sql");
    private static final String SQL_INSERT_EVENTO = SqlResources.load("cl/bcs/contabilidad/sql/CentralizaGenerico_InsertEvento.sql");
    private static final String SQL_INSERT_LOG = SqlResources.load("cl/bcs/contabilidad/sql/CentralizaGenerico_InsertLog.sql");
    private static final String SQL_INSERT_EVENTO_TIPO = SqlResources.load("cl/bcs/contabilidad/sql/CentralizaGenerico_InsertEventoTipo.sql");
    private static final String SQL_INSERT_MOVIMIENTO_PASO = SqlResources.load("cl/bcs/contabilidad/sql/CentralizaGenerico_InsertMovimientoPaso.sql");
    private static final String SQL_INSERT_DATO_VALOR_PASO = SqlResources.load("cl/bcs/contabilidad/sql/CentralizaGenerico_InsertDatoValorPaso.sql");

    /** Legacy FechaDB.updateCache. */
    private static final String SQL_UTC = "SELECT CURRENT_UTCTIMESTAMP as UTC  FROM DUMMY";
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final NamedParameterJdbcTemplate jdbc;

    public CentralizaGenericoJdbcAdapter(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ------------------------------------------------------------------ reads

    @Override
    public List<EventoTipoDato> queryEventosTipoDato() {
        return jdbc.query(SQL_SELECT_EVENTO_TIPO_DATO, new MapSqlParameterSource(), SqlRow.mapper(r ->
                new EventoTipoDato(r.str("IdTipoDatoEvento"), r.str("TipoDato"), r.str("DatosPreDefinidos"))));
    }

    @Override
    public List<TipoCentralizacion> queryTiposCentralizacion(List<Long> ids) {
        if (ids.isEmpty()) return List.of();
        return jdbc.query(SQL_SELECT_TIPO_CENTRALIZACION, new MapSqlParameterSource("IdTipoCentralizacion", ids),
                SqlRow.mapper(r -> new TipoCentralizacion(r.lng("IdTipoCentralizacion"), r.str("Nombre"), r.str("TipoEnvio"))));
    }

    @Override
    public List<Centralizacion> queryCentralizacionesVigentes(List<Long> ids, String fechaContable) {
        if (ids.isEmpty()) return List.of();
        return jdbc.query(SQL_SELECT_CENTRALIZACION, new MapSqlParameterSource()
                        .addValue("IdTipoCentralizacion", ids)
                        .addValue("FechaContable", fechaContable),
                SqlRow.mapper(r -> new Centralizacion(r.lng("IdTipoCentralizacion"), r.lng("IdCentralizacion"))));
    }

    @Override
    public List<MovimientoVigente> queryMovimientosVigentes(List<Long> ids) {
        if (ids.isEmpty()) return List.of();
        return jdbc.query(SQL_SELECT_MOVIMIENTO, new MapSqlParameterSource("IdTipoCentralizacion", ids),
                SqlRow.mapper(r -> new MovimientoVigente(r.lng("IdEmpresa"), r.str("Fecha"), r.str("IdTipoMovimiento"),
                        r.lng("Numero"), r.str("DocumentoIdentidad"), r.lng("Cuenta"), r.lng("IdTipoCentralizacion"))));
    }

    @Override
    public List<EventoTipo> queryEventosTipo(List<String> ids) {
        if (ids.isEmpty()) return List.of();
        return jdbc.query(SQL_SELECT_EVENTO_TIPO, new MapSqlParameterSource("IdTipoEvento", ids),
                SqlRow.mapper(r -> new EventoTipo(r.str("IdTipoEvento"), r.str("Nombre"), r.str("Estado"))));
    }

    @Override
    public List<EventoTipoDatoValor> queryEventosTipoDatoValor() {
        return jdbc.query(SQL_SELECT_EVENTO_TIPO_DATO_VALOR, new MapSqlParameterSource(), SqlRow.mapper(r ->
                new EventoTipoDatoValor(r.str("IdTipoDatoEvento"), r.str("ValorVarchar"), r.dec("ValorDecimal"), r.str("Estado"))));
    }

    @Override
    public String obtenerFechaProceso(Long idEmpresa) {
        return utc().format(FECHA);
    }

    @Override
    public String obtenerHoraProceso() {
        return utc().format(HORA);
    }

    @Override
    public List<Long> obtenerSecuencia(String nombreSecuencia, long cantidad) {
        return HanaSequences.next(jdbc, nombreSecuencia, cantidad);
    }

    private LocalDateTime utc() {
        LocalDateTime ahora = jdbc.queryForObject(SQL_UTC, new MapSqlParameterSource(),
                (rs, n) -> rs.getTimestamp(1).toLocalDateTime());
        if (ahora == null) throw new IllegalStateException("No se puede obtener la hora UTC de la base de datos");
        return ahora;
    }

    // ------------------------------------------------------------------ writes

    @Override
    public void insertarMovimientosPaso(List<MovimientoPaso> movimientos) {
        batch(SQL_INSERT_MOVIMIENTO_PASO, movimientos, m -> new MapSqlParameterSource()
                .addValue("IdEmpresa", m.idEmpresa())
                .addValue("Fecha", m.fecha())
                .addValue("IdTipoMovimiento", m.idTipoMovimiento())
                .addValue("Numero", m.numero())
                .addValue("DocumentoIdentidad", m.documentoIdentidad())
                .addValue("IdPortafolio", m.idPortafolio()));
    }

    @Override
    public void insertarValoresDatoPaso(List<ValorDatoPaso> valores) {
        batch(SQL_INSERT_DATO_VALOR_PASO, valores, v -> new MapSqlParameterSource()
                .addValue("TipoDato", v.tipoDato())
                .addValue("ValorVarChar", v.valorVarChar())
                .addValue("ValorDecimal", v.valorDecimal()));
    }

    @Override
    public void insertarCentralizaciones(List<InsertCentralizacion> centralizaciones) {
        batch(SQL_INSERT_CENTRALIZACION, centralizaciones, c -> new MapSqlParameterSource()
                .addValue("IdCentralizacion", c.idCentralizacion())
                .addValue("IdTipoCentralizacion", c.idTipoCentralizacion())
                .addValue("IdEmpresa", c.idEmpresa())
                .addValue("FechaProceso", c.fechaProceso())
                .addValue("HoraProceso", c.horaProceso())
                .addValue("FechaContable", c.fechaContable())
                .addValue("Glosa", c.glosa())
                .addValue("CodigoUsuario", c.codigoUsuario())
                .addValue("TipoGeneracion", c.tipoGeneracion())
                .addValue("Estado", c.estado()));
    }

    @Override
    public void insertarMovimientos(List<InsertMovimiento> movimientos) {
        batch(SQL_INSERT_MOVIMIENTO, movimientos, m -> new MapSqlParameterSource()
                .addValue("IdMovimiento", m.idMovimiento())
                .addValue("IdTipoMovimiento", m.idTipoMovimiento())
                .addValue("IdEmpresa", m.idEmpresa())
                .addValue("Numero", m.numero())
                .addValue("Linea", m.linea())
                .addValue("Fecha", m.fecha())
                .addValue("DocumentoIdentidad", m.documentoIdentidad())
                .addValue("Cuenta", m.cuenta())
                .addValue("Ejercicio", m.ejercicio())
                .addValue("Periodo", m.periodo())
                .addValue("IdCentralizacion", m.idCentralizacion())
                .addValue("IdMoneda", m.idMoneda())
                .addValue("Monto", m.monto())
                .addValue("Estado", m.estado()));
    }

    @Override
    public void insertarMovimientosDato(List<InsertMovimientoDato> datos) {
        batch(SQL_INSERT_MOVIMIENTO_DATO, datos, d -> new MapSqlParameterSource()
                .addValue("IdMovimiento", d.idMovimiento())
                .addValue("IdTipoDatoEvento", d.idTipoDatoEvento())
                .addValue("ValorVarchar", d.valorVarchar())
                .addValue("ValorDecimal", d.valorDecimal())
                .addValue("Estado", d.estado())
                .addValue("TipoEvento", d.tipoEvento()));
    }

    @Override
    public void insertarEventosTipo(List<EventoTipo> eventosTipo) {
        batch(SQL_INSERT_EVENTO_TIPO, eventosTipo, e -> new MapSqlParameterSource()
                .addValue("IdTipoEvento", e.idTipoEvento())
                .addValue("Nombre", e.nombre())
                .addValue("Estado", e.estado()));
    }

    @Override
    public void insertarEventos(List<InsertEvento> eventos) {
        batch(SQL_INSERT_EVENTO, eventos, e -> new MapSqlParameterSource()
                .addValue("IdEmpresa", e.idEmpresa())
                .addValue("IdEvento", e.idEvento())
                .addValue("IdTipoEvento", e.idTipoEvento())
                .addValue("IdCentralizacion", e.idCentralizacion())
                .addValue("IdMovimiento", e.idMovimiento())
                .addValue("FechaContable", e.fechaContable())
                .addValue("Monto", e.monto())
                .addValue("CodigoMoneda", e.codigoMoneda())
                .addValue("CodigoUsuario", e.codigoUsuario())
                .addValue("FechaProceso", e.fechaProceso())
                .addValue("HoraProceso", e.horaProceso())
                .addValue("Estado", e.estado()));
    }

    @Override
    public void insertarLogs(List<InsertLog> logs) {
        batch(SQL_INSERT_LOG, logs, l -> new MapSqlParameterSource()
                .addValue("IdTipoLog", l.idTipoLog())
                .addValue("IdEmpresa", l.idEmpresa())
                .addValue("Ejercicio", l.ejercicio())
                .addValue("IdTipoProceso", l.idTipoProceso())
                .addValue("Fecha", l.fecha())
                .addValue("Hora", l.hora())
                .addValue("CodigoUsuario", l.codigoUsuario())
                .addValue("Glosa", l.glosa())
                .addValue("IdCentralizacion", l.idCentralizacion())
                .addValue("IdMovimiento", l.idMovimiento())
                .addValue("IdEvento", l.idEvento())
                .addValue("IdDefinicionAsiento", l.idDefinicionAsiento())
                .addValue("IdPreComprobante", l.idPreComprobante())
                .addValue("IdPreComprobanteAsiento", l.idPreComprobanteAsiento())
                .addValue("IdCuentaContable", l.idCuentaContable())
                .addValue("IdCondicion", l.idCondicion())
                .addValue("IdTipoContabilidad", l.idTipoContabilidad())
                .addValue("IdTipoEvento", l.idTipoEvento())
                .addValue("IdTipoDatoEvento", l.idTipoDatoEvento())
                .addValue("IdTipoCentralizacion", l.idTipoCentralizacion())
                .addValue("IdTipoMovimiento", l.idTipoMovimiento())
                .addValue("NumeroMovimiento", l.numeroMovimiento())
                .addValue("FechaMovimiento", l.fechaMovimiento())
                .addValue("RutMovimiento", l.rutMovimiento())
                .addValue("CuentaMovimiento", l.cuentaMovimiento())
                .addValue("IndicadorError", l.indicadorError())
                .addValue("Estado", l.estado()));
    }

    private <T> void batch(String sql, Collection<T> filas, Function<T, MapSqlParameterSource> binder) {
        if (filas.isEmpty()) return;
        jdbc.batchUpdate(sql, SqlParams.batch(filas, binder));
    }
}
