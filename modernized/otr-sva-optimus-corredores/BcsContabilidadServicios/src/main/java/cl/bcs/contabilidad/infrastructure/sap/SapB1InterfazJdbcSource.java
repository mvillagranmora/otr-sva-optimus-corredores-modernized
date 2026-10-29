package cl.bcs.contabilidad.infrastructure.sap;

import cl.bcs.contabilidad.domain.port.out.InterfazSapSource;
import cl.bcs.contabilidad.infrastructure.persistence.SqlResources;
import cl.bcs.contabilidad.infrastructure.persistence.SqlRow;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * {@link InterfazSapSource} over the SAP Business One database (CONT-A-02).
 *
 * <p>Legacy: the {@code Crea*} classes ran these statements with {@code cl.bcs.utils.SQL.query} on
 * {@code PlataformaUtils.getDESAPEB1Datasource()}. The SQL files are the legacy ones, verbatim; only a trailing
 * {@code ;} is dropped before execution. {@code :FechaEjecucion} is bound as the legacy {@code yyyy-MM-dd} string.
 * Columns are read by label, a missing column yields {@code null} (legacy field copy semantics).
 */
public final class SapB1InterfazJdbcSource implements InterfazSapSource {

    static final String SQL_BROAD_CUENTA = "cl/bcs/contabilidad/sql/ConsultaBroadCuenta.sql";
    static final String SQL_BROAD_MOVIMIENTO = "cl/bcs/contabilidad/sql/ConsultaBroadMovimiento.sql";
    static final String SQL_BROAD_SALDO = "cl/bcs/contabilidad/sql/ConsultaBroadSaldo.sql";
    static final String SQL_DES_ENTIDAD = "cl/bcs/contabilidad/sql/ConsultaDesEntidad.sql";
    static final String SQL_DES_TIPO_CUENTA = "cl/bcs/contabilidad/sql/ConsultaDesTipoCuenta.sql";
    static final String SQL_DES_CENTRO_COSTO = "cl/bcs/contabilidad/sql/ConsultaDesCentroCosto.sql";
    static final String SQL_CUENTA_BPC = "cl/bcs/contabilidad/sql/ConsultaCuentaBPC.sql";
    static final String SQL_MOVIMIENTO_BPC = "cl/bcs/contabilidad/sql/ConsultaMovimientoBPC.sql";

    private static final String PARAM_FECHA = "FechaEjecucion";

    private final Supplier<NamedParameterJdbcTemplate> jdbc;

    /** @param jdbc template over the SAP B1 database, resolved on first use */
    public SapB1InterfazJdbcSource(Supplier<NamedParameterJdbcTemplate> jdbc) {
        this.jdbc = Objects.requireNonNull(jdbc, "jdbc");
    }

    @Override
    public List<BroadCuenta> cuentasBroad() {
        return query(SQL_BROAD_CUENTA, null, SqlRow.mapper(r -> new BroadCuenta(
                r.str("CodEntidad"), r.str("CodCtaContable"), r.str("Descripcion"), r.str("TipoCuenta"),
                r.str("CuentaGrupo"), r.str("CodMonedaContable"), r.str("IndCtaConfidencial"),
                r.str("IndCtaAsociada"), r.str("IndCtaMonetaria"), r.str("IndRelevanteFlujoCaja"),
                r.str("IndCtaAfiliada"))));
    }

    @Override
    public List<BroadMovimiento> movimientosBroad(LocalDate fechaEjecucion) {
        return query(SQL_BROAD_MOVIMIENTO, fechaEjecucion, SqlRow.mapper(r -> new BroadMovimiento(
                r.str("CodInstitucion"), r.str("CodAplicativo"), r.str("CodEntidad"), fecha(r, "FecDia"),
                r.integer("HraTransaccion"), r.str("NumOperacion"), r.str("NumPosOperacion"), r.str("NumRegistro"),
                r.str("CodCtaContable"), r.str("CodTipoCuenta"), r.str("CodProductoContable"),
                r.str("CodCentroCosto"), fecha(r, "FecDocumento"), r.str("NroSerieDoc"), r.str("NroDoc"),
                r.str("CodAfiliada"), r.str("TipCargoAbono"), r.str("CodMonedaContable"),
                r.dec("MontoTransaccionContable"), r.str("IndAnulado"), r.str("IndOpeAfiliada"),
                r.str("Filler01"), r.str("Filler02"))));
    }

    @Override
    public List<BroadSaldo> saldosBroad(LocalDate fechaEjecucion) {
        return query(SQL_BROAD_SALDO, fechaEjecucion, SqlRow.mapper(r -> new BroadSaldo(
                r.str("CodInstitucion"), r.str("CodAplicativo"), r.str("CodEntidad"), fecha(r, "FecDia"),
                r.str("NumRegistro"), r.str("CodCtaContable"), r.str("CodTipoCuenta"), r.str("CodProductoContable"),
                r.str("CodCentroCosto"), r.str("CodMonedaContable"), r.dec("MontoSaldoContable"),
                r.str("Filler01"), r.str("Filler02"))));
    }

    @Override
    public List<Entidad> entidades() {
        return query(SQL_DES_ENTIDAD, null, SqlRow.mapper(r -> new Entidad(r.str("CodEntidad"), r.str("Descripcion"))));
    }

    @Override
    public List<TipoCuenta> tiposCuenta() {
        return query(SQL_DES_TIPO_CUENTA, null,
                SqlRow.mapper(r -> new TipoCuenta(r.str("TipoCuenta"), r.str("Descripcion"))));
    }

    @Override
    public List<CentroCosto> centrosCosto() {
        return query(SQL_DES_CENTRO_COSTO, null,
                SqlRow.mapper(r -> new CentroCosto(r.str("CodccPro"), r.str("Descripcion"), r.str("Tipo"))));
    }

    @Override
    public List<CuentaBpc> cuentasBpc() {
        return query(SQL_CUENTA_BPC, null, SqlRow.mapper(r -> new CuentaBpc(
                r.str("CodigoSociedad"), r.str("CuentaOperativa"), r.str("Descripcion"), r.str("TipoCuenta"),
                r.str("CuentaGrupo"))));
    }

    @Override
    public List<MovimientoBpc> movimientosBpc(LocalDate fechaEjecucion) {
        return query(SQL_MOVIMIENTO_BPC, fechaEjecucion, SqlRow.mapper(r -> new MovimientoBpc(
                r.str("CodigoSociedad"), r.str("CuentaMayor"), r.str("VarianteDeEjercicio"),
                r.str("EjercicioPeriodo"), r.str("NroDocumentoContable"), r.str("NroApunteContable"),
                r.str("Segmento"), r.str("Ledger"), r.str("SocGlAsociada"), r.str("Moneda"),
                r.dec("TotalContabDebe"), r.dec("TotalContaHaber"), r.str("ClaseDeRegistro"),
                r.str("IndicadorAnulacion"))));
    }

    private <T> List<T> query(String recurso, LocalDate fecha, RowMapper<T> mapper) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        if (fecha != null) {
            params.addValue(PARAM_FECHA, fecha.format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
        return jdbc.get().query(sinPuntoYComaFinal(SqlResources.load(recurso)), params, mapper);
    }

    static String sinPuntoYComaFinal(String sql) {
        String s = sql.strip();
        return s.endsWith(";") ? s.substring(0, s.length() - 1) : s;
    }

    /** Date part of a DATE/TIMESTAMP column as the legacy {@code SimpleDateFormat("yyyyMMdd")} saw it. */
    private static LocalDate fecha(SqlRow r, String columna) {
        Object v = r.obj(columna);
        return switch (v) {
            case null -> null;
            case java.sql.Timestamp t -> t.toLocalDateTime().toLocalDate();
            case java.sql.Date d -> d.toLocalDate();
            case java.util.Date d -> new java.sql.Timestamp(d.getTime()).toLocalDateTime().toLocalDate();
            case LocalDate d -> d;
            case LocalDateTime t -> t.toLocalDate();
            case OffsetDateTime t -> t.toLocalDate();
            default -> LocalDate.parse(v.toString().substring(0, 10));
        };
    }
}
