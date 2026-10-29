package cl.bcs.contabilidad.infrastructure.persistence;

import cl.bcs.contabilidad.domain.model.ConfiguracionTipoCentralizacion;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabDatoEventoRef;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabDatoMovimientoRef;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabEventoRef;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabMovimientoRef;
import cl.bcs.contabilidad.domain.port.out.ContabilidadRepository;
import cl.bcs.contabilidad.domain.service.CentralizaGenerico;
import cl.bcs.contabilidad.domain.service.devengo.CarteraValorizada;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * JDBC implementation of ContabilidadRepository backed by SAP HANA.
 *
 * SQL is loaded with {@link SqlResources}: the legacy statements under cl/bcs/contabilidad/sql/ and the
 * module's own sql/query*.sql (configuration and generic movement queries of this port).  Named parameters
 * (:param) are handled by NamedParameterJdbcTemplate; HANA's IN-clause
 * expansion is automatic when a List is supplied as the parameter value.
 *
 * SAP HANA connection parameters are read from Spring datasource configuration
 * (see application.properties: HANA_URL, HANA_USER, HANA_PASSWORD).
 * Tests mock this interface directly; the real SQL runs only against HANA.
 *
 * <p>{@link #persistirCentralizacion} is the legacy CentralizaGenerico step for one type: it delegates to
 * {@link CentralizaGenerico}, the single implementation of those rules and inserts.
 */
@Repository
public class ContabilidadJdbcRepository implements ContabilidadRepository {

    private final NamedParameterJdbcTemplate jdbc;
    private final CentralizaGenerico centralizaGenerico;

    public ContabilidadJdbcRepository(NamedParameterJdbcTemplate jdbc, CentralizaGenerico centralizaGenerico) {
        this.jdbc = jdbc;
        this.centralizaGenerico = centralizaGenerico;
    }

    // ------------------------------------------------------------------
    // Configuration
    // ------------------------------------------------------------------

    @Override
    public List<ConfiguracionTipoCentralizacion> queryConfiguraciones(List<Long> ids) {
        if (ids.isEmpty()) return List.of();
        String sql = loadSql("sql/queryConfiguraciones.sql");
        MapSqlParameterSource params = new MapSqlParameterSource("IdTipoCentralizacion", ids);
        return jdbc.query(sql, params, CONFIGURACION_MAPPER);
    }

    @Override
    public List<ConfiguracionTipoCentralizacion> queryCentralizacionesVigentes(
            List<Long> ids, String fechaContable, Long idEmpresa) {
        if (ids.isEmpty()) return List.of();
        String sql = loadSql("sql/queryCentralizacionesVigentes.sql");
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("IdTipoCentralizacion", ids)
                .addValue("FechaContable", fechaContable)
                .addValue("IdEmpresa", idEmpresa);
        return jdbc.query(sql, params, CONFIGURACION_MAPPER);
    }

    // ------------------------------------------------------------------
    // Movement queries
    // ------------------------------------------------------------------

    @Override
    public List<TabMovimientoRef> queryMovimientos(
            Long idEmpresa, String fechaContable, Long idTipoCentralizacion) {
        String sql = loadSql("sql/queryMovimientos.sql");
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("IdEmpresa", idEmpresa)
                .addValue("FechaContable", fechaContable)
                .addValue("IdTipoCentralizacion", idTipoCentralizacion);
        return jdbc.query(sql, params, MOVIMIENTO_MAPPER);
    }

    @Override
    public List<TabEventoRef> queryEventosOperacion(
            Long idEmpresa, String fechaContable, Long idTipoCentralizacion) {
        String sql = loadSql("sql/queryEventosOperacion.sql");
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("IdEmpresa", idEmpresa)
                .addValue("FechaContable", fechaContable)
                .addValue("IdTipoCentralizacion", idTipoCentralizacion);
        return jdbc.query(sql, params, EVENTO_MAPPER);
    }

    @Override
    public List<TabEventoRef> queryEventosSubyacente(
            Long idEmpresa, String fechaContable,
            Long idTipoCentralizacion, String fechaAnterior) {
        String sql = loadSql("sql/queryEventosSubyacente.sql");
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("IdEmpresa", idEmpresa)
                .addValue("FechaContable", fechaContable)
                .addValue("IdTipoCentralizacion", idTipoCentralizacion)
                .addValue("FechaAnterior", fechaAnterior);
        return jdbc.query(sql, params, EVENTO_MAPPER);
    }

    @Override
    public List<AnticipoPactosDatoMovimiento> queryDatosMovimientoAnticipo(
            Long idEmpresa, String fechaContable) {
        String sql = loadSql("sql/queryDatosMovimientoAnticipo.sql");
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("IdEmpresa", idEmpresa)
                .addValue("FechaContable", fechaContable);
        return jdbc.query(sql, params, DATO_MOVIMIENTO_MAPPER);
    }

    @Override
    public List<Map<String, Object>> queryDatosMovimientoRows(
            String sqlResourceName, MapSqlParameterSource params) {
        String sql = loadSql(sqlResourceName);
        return jdbc.queryForList(sql, params);
    }

    @Override
    public int ejecutarSql(String sqlResourceName, MapSqlParameterSource params) {
        return jdbc.update(loadSql(sqlResourceName), params);
    }

    @Override
    public void crearBloqueoCentralizacion(String idBloqueo, Long idTipoCentralizacion, String fechaCentralizacion) {
        conTablaBloqueo(idBloqueo, idTipoCentralizacion, fechaCentralizacion, "cl/bcs/contabilidad/sql/CentralizaSpotUtilidadPerdida_Call_Create.sql");
    }

    @Override
    public void eliminarBloqueoCentralizacion(String idBloqueo, Long idTipoCentralizacion, String fechaCentralizacion) {
        conTablaBloqueo(idBloqueo, idTipoCentralizacion, fechaCentralizacion, "cl/bcs/contabilidad/sql/CentralizaSpotUtilidadPerdida_Call_Delete.sql");
    }

    /**
     * The lock procedures take the local temporary table #p_BloqueoCentralizacion, which lives in one session: create,
     * fill, call and drop it on a single connection (legacy CentralizaSpotUtilidadPerdida.java:111-121, 190-195).
     */
    private void conTablaBloqueo(String idBloqueo, Long idTipo, String fecha, String llamada) {
        String crear = SqlResources.load("cl/bcs/contabilidad/sql/CentralizaSpotUtilidadPerdida_CreateTablaBloqueo.sql");
        String insertar = SqlResources.load("cl/bcs/contabilidad/sql/CentralizaSpotUtilidadPerdida_InsertTablaBloqueo.sql")
                .replace(":IdBloqueoCentralizacion", "?").replace(":IdTipoCentralizacion", "?")
                .replace(":FechaCentralizacion", "?").replace(";", "");
        String call = SqlResources.load(llamada).replace(";", "");
        String eliminar = SqlResources.load("cl/bcs/contabilidad/sql/CentralizaSpotUtilidadPerdida_DeleteTablaBloqueo.sql").replace(";", "");
        jdbc.getJdbcTemplate().execute((org.springframework.jdbc.core.ConnectionCallback<Void>) con -> {
            try (var st = con.createStatement()) {
                st.execute(crear.replace(";", ""));
            }
            try {
                try (var ps = con.prepareStatement(insertar)) {
                    ps.setString(1, idBloqueo);
                    ps.setLong(2, idTipo);
                    ps.setString(3, fecha);
                    ps.executeUpdate();
                }
                try (var cs = con.prepareCall(call)) {
                    cs.execute();
                }
            } finally {
                try (var st = con.createStatement()) {
                    st.execute(eliminar);
                }
            }
            return null;
        });
    }

    @Override
    public String queryClasificacionTipoInstrumento(Long idTipoCentralizacion) {
        String sql = loadSql(
                // Legacy CentralizaAnticipoVencimientoEmprestito/-Prestamo ship byte-identical copies of this statement.
                "cl/bcs/contabilidad/sql/CentralizaAnticipoVencimientoEmprestito_SelectClasificacionTipoInstrumento.sql");
        MapSqlParameterSource params = new MapSqlParameterSource("IdTipoCentralizacion", idTipoCentralizacion);
        List<String> results = jdbc.queryForList(sql, params, String.class);
        if (results.isEmpty()) {
            throw new IllegalStateException("Tipo de centralizacion " + idTipoCentralizacion + " sin ClasificacionTipoInstrumento");
        }
        return results.get(0);
    }

    @Override
    public List<CarteraValorizada> queryCarteraValorizada(String sqlResourceName, Long idEmpresa, String idInforme) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("p_IdEmpresa", idEmpresa)
                .addValue("p_IdInforme", idInforme);
        return jdbc.query(loadSql(sqlResourceName), params, SqlRow.mapper(r -> new CarteraValorizada(
                r.lng("IdValorizacion"), r.lng("IdCartera"), r.lng("IdEmpresa"), r.str("IdTipoOperacion"),
                r.str("IdClasificacionTipoInstrumento"), r.lng("FolioOperacion"), r.lng("FolioCartera"),
                r.str("DocumentoIdentidad"), r.lng("IdPortafolio"), fecha(r.obj("FechaOperacion")),
                r.str("IdMonedaInstrumento"), r.str("IdMonedaSistema"), r.dec("MontoCompra"), r.dec("MontoPactado"),
                r.dec("MontoActual"), r.dec("MontoProximo"), r.dec("MontoInteres"), r.dec("MontoInteresCorrido"),
                r.dec("MontoReajuste"))));
    }

    private static LocalDate fecha(Object v) {
        return switch (v) {
            case null -> null;
            case java.sql.Date d -> d.toLocalDate();
            case java.sql.Timestamp t -> t.toLocalDateTime().toLocalDate();
            case LocalDate d -> d;
            case java.time.LocalDateTime t -> t.toLocalDate();
            default -> LocalDate.parse(v.toString().substring(0, 10));
        };
    }

    // ------------------------------------------------------------------
    // Write — only called when simulaOperacion = "N"
    // ------------------------------------------------------------------

    // NESTED: a savepoint inside the JavaCall unit of work (JavaCallUnitOfWorkFilter). A type that fails here is
    // undone alone and B-03 lets the batch continue without poisoning the outer transaction; outside a request
    // (no transaction yet) it behaves like REQUIRED.
    @Transactional(propagation = Propagation.NESTED)
    @Override
    public void persistirCentralizacion(
            Long idTipoCentralizacion,
            String fechaContable,
            Long idEmpresa,
            Long ejercicio,
            String codigoUsuario,
            List<TabMovimientoRef> movimientos,
            List<TabEventoRef> eventos,
            List<TabDatoMovimientoRef> datosMovimiento,
            List<TabDatoEventoRef> datosEvento) {
        // One type per call; its name is taken from ITZ.CentralizacionTipo (the column CentralizaMultiSeleccion read).
        centralizaGenerico.logica(new CentralizaGenerico.Entrada(idEmpresa,
                List.of(new CentralizaGenerico.TipoCentralizacionSeleccionada(idTipoCentralizacion, null)),
                ejercicio, fechaContable, codigoUsuario, movimientos, eventos, datosMovimiento, datosEvento));
    }

    // ------------------------------------------------------------------
    // Row mappers
    // ------------------------------------------------------------------

    private static final RowMapper<ConfiguracionTipoCentralizacion> CONFIGURACION_MAPPER =
            (rs, rowNum) -> new ConfiguracionTipoCentralizacion(
                    rs.getLong("IdTipoCentralizacion"),
                    rs.getString("Nombre"),
                    null,                                   // vistaMovimiento — not in config query
                    null,                                   // vistaEvento
                    null,                                   // vistaMovimientoDato
                    rs.getString("Servicio"),
                    rs.getString("Tipo"),
                    null,                                   // idInforme
                    nullableLong(rs, "IdTipoCentralizacionOrigen"),
                    rs.getString("RequiereFechaConsulta"),
                    null,                                   // informeAsociado
                    null                                    // llaveInformeAsociado
            );

    private static final RowMapper<TabMovimientoRef> MOVIMIENTO_MAPPER =
            (rs, rowNum) -> new TabMovimientoRef(
                    rs.getLong("IdTipoCentralizacion"),
                    rs.getLong("IdMovimiento"),
                    rs.getString("IdTipoMovimiento"),
                    rs.getLong("IdEmpresa"),
                    rs.getLong("Numero"),
                    rs.getLong("Linea"),
                    rs.getString("Fecha"),
                    rs.getString("DocumentoIdentidad"),
                    rs.getLong("IdPortafolio"),
                    rs.getLong("Ejercicio"),
                    rs.getString("Periodo"),
                    rs.getBigDecimal("Monto"),
                    rs.getString("IdMoneda"),
                    rs.getString("Estado")
            );

    private static final RowMapper<TabEventoRef> EVENTO_MAPPER =
            (rs, rowNum) -> new TabEventoRef(
                    rs.getLong("IdTipoCentralizacion"),
                    rs.getLong("IdMovimiento"),
                    rs.getString("IdEvento"),
                    rs.getString("Nombre"),
                    nullableBigDecimal(rs, "MontoEvento"),
                    rs.getString("CodigoMoneda")
            );

    private static final RowMapper<AnticipoPactosDatoMovimiento> DATO_MOVIMIENTO_MAPPER =
            (rs, rowNum) -> new AnticipoPactosDatoMovimiento(
                    rs.getLong("IdMovimiento"),
                    rs.getString("DocumentoIdentidad"),
                    rs.getString("NombreCliente"),
                    rs.getString("Portafolio"),
                    rs.getString("Moneda"),
                    rs.getString("Producto"),
                    rs.getString("ClaseInstrumento"),
                    rs.getString("TipoInstrumento"),
                    rs.getString("Nemo"),
                    rs.getString("Boveda"),
                    rs.getString("IdCartera"),
                    rs.getString("Emisor"),
                    rs.getString("TipoReajuste"),
                    rs.getString("TipoEmisor"),
                    rs.getString("CodigoIFRS"),
                    rs.getString("TipoCartera"),
                    rs.getString("ClaseEmisor"),
                    rs.getString("Agente"),
                    rs.getString("NombreAgente"),
                    rs.getString("SucursalAgente"),
                    rs.getString("NombreSucursalAgente"),
                    rs.getString("CentroCostoAgente"),
                    rs.getString("Relacion"),
                    rs.getString("CompraVenta"),
                    rs.getString("Mesa"),
                    rs.getLong("FolioOperacion")
            );

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private static Long nullableLong(java.sql.ResultSet rs, String col)
            throws java.sql.SQLException {
        long v = rs.getLong(col);
        return rs.wasNull() ? null : v;
    }

    private static BigDecimal nullableBigDecimal(java.sql.ResultSet rs, String col)
            throws java.sql.SQLException {
        BigDecimal v = rs.getBigDecimal(col);
        return v == null ? BigDecimal.ZERO : v;
    }

    /** Every statement goes through the module loader, which fails naming the file when it is missing. */
    private static String loadSql(String classpathResource) {
        return SqlResources.load(classpathResource);
    }
}
