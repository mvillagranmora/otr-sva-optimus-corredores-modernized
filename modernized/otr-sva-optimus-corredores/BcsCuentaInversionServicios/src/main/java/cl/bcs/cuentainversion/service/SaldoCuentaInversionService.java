package cl.bcs.cuentainversion.service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonObject;

import cl.bcs.utils.Fecha;
import cl.bcs.utils.Utils;

/**
 * Balance of the investment accounts of a set of portfolios, per account and per day of the requested range, by EFFECTIVE date and
 * by MOVEMENT date (certified movements accumulated on top of the last stored balance before the range), with the pending cargos and
 * abonos, the positive/negative filters and the initial/final balance of each account (README CTA-A-77..90).
 */
@Service
public class SaldoCuentaInversionService {

    private static final Logger log = LoggerFactory.getLogger(SaldoCuentaInversionService.class);

    private static final String SQL = "cl/bcs/cuentainversion/sql/SaldoCuentaInversion_";
    private static final String SQL_INSERT_PORTAFOLIOS = CtaJavaCallInfra.loadSql(SQL + "InsertPortafolios.sql");
    private static final String SQL_CUENTAS_DE_PORTAFOLIOS = CtaJavaCallInfra.loadSql(SQL + "SelectCuentaInversion.sql");
    private static final String SQL_CUENTAS_TODAS = CtaJavaCallInfra.loadSql(SQL + "SelectCuentaInversionFull.sql");
    private static final String SQL_INSERT_CUENTAS = CtaJavaCallInfra.loadSql(SQL + "InsertCuentaInversionTablaPaso.sql");
    private static final String SQL_MOVIMIENTOS = CtaJavaCallInfra.loadSql(SQL + "SelectMovimientos.sql");
    private static final String SQL_SALDO_ANTERIOR_EFECTIVO = CtaJavaCallInfra.loadSql(SQL + "SelectSaldoAnteriorEfectiva.sql");
    private static final String SQL_SALDO_ANTERIOR_SISTEMA = CtaJavaCallInfra.loadSql(SQL + "SelectSaldoAnteriorSistema.sql");
    private static final String SQL_LIMPIAR_PASO = CtaJavaCallInfra.loadSql(SQL + "DeleteCuentaInversionTablaPaso.sql");

    // ---------------------------------------------------------------------------------------------------- contract

    public static class Entrada {
        public Long p_IdEmpresa;
        public String p_FechaDesde;
        public String p_FechaHasta;
        public String p_SoloSaldoNegativo = "N";
        public String p_SoloSaldoDisponible = "N";
        public String p_SoloSaldoDisponibleNegativo = "N";
        public String p_IncluyeCargosPendientes = "N";
        public String p_IncluyeAbonosPendientes = "N";
        public Object p_BarraProgreso;
        public List<CuentaInversionAConsultar> p_CuentaInversionAConsultar = new ArrayList<>();
    }

    public static class CuentaInversionAConsultar {
        public Long IdEjecutivo;
        public Long IdPortafolio;
        public String IdMoneda;

        public CuentaInversionAConsultar() {
        }

        public CuentaInversionAConsultar(Long idPortafolio) {
            this.IdPortafolio = idPortafolio;
        }
    }

    public static class Salida {
        public List<SaldoInicialFinal> P_OUT_SALDOINICIALFINAL = new ArrayList<>();
        public List<SaldoPorFechaEfectiva> P_OUT_SALDOPORFECHAEFECTIVA = new ArrayList<>();
        public List<SaldoPorFechaMovimiento> P_OUT_SALDOPORFECHAMOVIMIENTO = new ArrayList<>();
    }

    public static class SaldoInicialFinal {
        public Long IdCuentaInversion;
        public BigDecimal SaldoEfectivoInicial;
        public BigDecimal SaldoEfectivoFinal;
        public BigDecimal SaldoMovimientoInicial;
        public BigDecimal SaldoMovimientoFinal;
    }

    /** Daily balance by effective date; the extra columns are completed by ConsultarSaldoCuentaInversion. */
    public static class SaldoPorFechaEfectiva {
        public Long IdCuentaInversion;
        public String CodigoCuentaInversion;
        public Long IdCliente;
        public Long IdPortafolio;
        public String FechaSaldo;
        public String IdMoneda;
        public BigDecimal Saldo;
        public BigDecimal SaldoDisponible;
        public BigDecimal SaldoCargosPendientes;
        public BigDecimal SaldoAbonosPendientes;
        public Long IdEjecutivo;
        public String NombreEjecutivo;
        public String CodigoEjecutivo;
        public String CodigoSucursal;
        public String NombreSucursal;
        public String NombreCliente;
        public String DocumentoIdentidad;
        public String NombrePortafolio;
        public String PropositoPortafolio;
        public String CodigoPortafolio;
        public String CodigoMoneda;
        public String NombreMoneda;
        public BigDecimal SaldoEnGarantia;
        public BigDecimal SaldoAPlazoSimultanea;
        public BigDecimal SaldoAPlazoVentaCorta;
        public BigDecimal SaldoAPlazoCompromiso;
        public BigDecimal SaldoRetiro;
        public BigDecimal SaldoRetenido = BigDecimal.ZERO;
        public String Producto;
        public BigDecimal MontoTransSolicitudesAporte = BigDecimal.ZERO;
        public Long IdProductoAgrupador;
        public BigDecimal MontoMoneyMarket;
        public BigDecimal TotalLiquidez;
        public Long IdMatrizAgrupador;
        public String EsADC;
        public String Relacion;
        public String TipoCliente = "";
        public String FechaUltimoMovimiento = "0001-01-01";
        public BigDecimal SaldoPendienteLibDAP = BigDecimal.ZERO;
    }

    /** Daily balance by movement (system) date. */
    public static class SaldoPorFechaMovimiento {
        public Long IdCuentaInversion;
        public String CodigoCuentaInversion;
        public Long IdCliente;
        public Long IdPortafolio;
        public String FechaSaldo;
        public String IdMoneda;
        public BigDecimal Saldo;
        public BigDecimal SaldoDisponible;
        public BigDecimal SaldoCargosPendientes;
        public BigDecimal SaldoAbonosPendientes;
        public Long IdEjecutivo;
        public String NombreEjecutivo;
        public String CodigoEjecutivo;
        public String CodigoSucursal;
        public String NombreSucursal;
        public String NombreCliente;
        public String DocumentoIdentidad;
        public String NombrePortafolio;
        public String PropositoPortafolio;
        public String CodigoPortafolio;
        public String CodigoMoneda;
        public String NombreMoneda;
        public BigDecimal SaldoEnGarantia;
        public BigDecimal SaldoAPlazoSimultanea;
        public BigDecimal SaldoAPlazoVentaCorta;
        public BigDecimal SaldoAPlazoCompromiso;
        public BigDecimal SaldoRetiro;
        public BigDecimal SaldoRetenido = BigDecimal.ZERO;
        public String Producto;
        public Long IdProductoAgrupador;
        public Long IdMatrizAgrupador;
        public String EsADC;
        public BigDecimal MontoTransSolicitudesAporte = BigDecimal.ZERO;
        public BigDecimal SaldoPendienteLibDAP = BigDecimal.ZERO;
    }

    // ---------------------------------------------------------------------------------------------------- internals

    private record Cuenta(Long id, String codigo, Long idCliente, Long idPortafolio, String idMoneda, String principal) {
    }

    /** One movement of the range: amount signed for certified ones, pending amounts split by direction. */
    private record Mov(Long idCuenta, LocalDate fechaSistema, LocalDate fechaEfectiva, BigDecimal monto,
                       BigDecimal cargoPendiente, BigDecimal abonoPendiente) {
    }

    /** Last balance before the range plus the pending amounts of that period. */
    private static final class SaldoAnterior {
        BigDecimal saldo = BigDecimal.ZERO;
        BigDecimal cargoPendiente = BigDecimal.ZERO;
        BigDecimal abonoPendiente = BigDecimal.ZERO;
    }

    /** Accumulated balances of one account at one day (effective and movement series). */
    private static final class Acumulado {
        BigDecimal efectivo = BigDecimal.ZERO;
        BigDecimal cargosEfectivo = BigDecimal.ZERO;
        BigDecimal abonosEfectivo = BigDecimal.ZERO;
        BigDecimal movimiento = BigDecimal.ZERO;
        BigDecimal cargosMovimiento = BigDecimal.ZERO;
        BigDecimal abonosMovimiento = BigDecimal.ZERO;
    }

    private record Dia(Cuenta cuenta, LocalDate fecha, Acumulado acumulado) {
    }

    private final NamedParameterJdbcTemplate jdbc;

    public SaldoCuentaInversionService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        Entrada entrada = Utils.getGsonBuilder().fromJson(params, Entrada.class);
        return Utils.getGsonBuilder().toJsonTree(saldo(entrada)).getAsJsonObject();
    }

    /** Typed entry point for in-module callers (ConsultarSaldoCuentaInversion, ConsultarCartolaDeMovimiento, ConsultarMovimientos). */
    @Transactional
    public Salida saldo(Entrada entrada) {
        log.info("INICIO::SaldoCuentaInversion");
        Salida out = new Salida();

        // CTA-A-88: a null date throws NullPointerException from the date parser
        LocalDate desde = Fecha.toDate(entrada.p_FechaDesde).toLocalDate();
        LocalDate hasta = Fecha.toDate(entrada.p_FechaHasta).toLocalDate();

        Set<Long> portafolios = new LinkedHashSet<>();
        Set<String> monedas = new LinkedHashSet<>();
        for (CuentaInversionAConsultar c : entrada.p_CuentaInversionAConsultar) {
            if (c.IdMoneda != null) {
                monedas.add(c.IdMoneda);
            }
            if (c.IdPortafolio != null) {
                portafolios.add(c.IdPortafolio);
            }
        }
        long idTrabajo = ThreadLocalRandom.current().nextLong(10_000_000_000L);
        jdbc.batchUpdate(SQL_INSERT_PORTAFOLIOS, portafolios.stream()
                .map(p -> (SqlParameterSource) new MapSqlParameterSource("IdPortafolio", p).addValue("IdTrabajo", idTrabajo))
                .toArray(SqlParameterSource[]::new));

        List<Cuenta> cuentas = cuentasDeLaConsulta(portafolios, monedas, idTrabajo);

        // one entry per account (a portfolio can list the same account twice)
        Map<Long, Cuenta> unicas = new LinkedHashMap<>();
        cuentas.forEach(c -> unicas.putIfAbsent(c.id(), c));
        jdbc.batchUpdate(SQL_INSERT_CUENTAS, unicas.values().stream()
                .map(c -> (SqlParameterSource) new MapSqlParameterSource()
                        .addValue("IdCliente", c.idCliente()).addValue("IdPortafolio", c.idPortafolio())
                        .addValue("IdCuentaInversion", c.id()).addValue("IdTrabajo", idTrabajo))
                .toArray(SqlParameterSource[]::new));

        MapSqlParameterSource rango = new MapSqlParameterSource("FechaDesde", entrada.p_FechaDesde)
                .addValue("FechaHasta", entrada.p_FechaHasta).addValue("IdTrabajo", idTrabajo);
        Map<Long, List<Mov>> movimientos = jdbc.queryForList(SQL_MOVIMIENTOS, rango).stream()
                .map(SaldoCuentaInversionService::aMovimiento)
                .collect(Collectors.groupingBy(Mov::idCuenta));
        Map<Long, SaldoAnterior> anteriorEfectivo = saldosAnteriores(SQL_SALDO_ANTERIOR_EFECTIVO, rango);
        Map<Long, SaldoAnterior> anteriorSistema = saldosAnteriores(SQL_SALDO_ANTERIOR_SISTEMA, rango);

        // day by day, account by account
        List<Dia> dias = new ArrayList<>();
        for (Cuenta c : cuentas) {
            for (LocalDate f = desde; !f.isAfter(hasta); f = f.plusDays(1)) {
                dias.add(new Dia(c, f, new Acumulado()));
            }
        }
        for (Dia d : dias) {
            Acumulado a = d.acumulado();
            for (Mov m : movimientos.getOrDefault(d.cuenta().id(), List.of())) {
                if (!m.fechaEfectiva().isBefore(desde) && !m.fechaEfectiva().isAfter(d.fecha())) {
                    a.efectivo = a.efectivo.add(m.monto(), MathContext.DECIMAL128);
                    a.cargosEfectivo = a.cargosEfectivo.add(m.cargoPendiente(), MathContext.DECIMAL128);
                    a.abonosEfectivo = a.abonosEfectivo.add(m.abonoPendiente(), MathContext.DECIMAL128);
                }
                if (!m.fechaSistema().isBefore(desde) && !m.fechaSistema().isAfter(d.fecha())) {
                    a.movimiento = a.movimiento.add(m.monto(), MathContext.DECIMAL128);
                    a.cargosMovimiento = a.cargosMovimiento.add(m.cargoPendiente(), MathContext.DECIMAL128);
                    a.abonosMovimiento = a.abonosMovimiento.add(m.abonoPendiente(), MathContext.DECIMAL128);
                }
            }
            SaldoAnterior e = anteriorEfectivo.get(d.cuenta().id());
            if (e != null) {
                a.efectivo = a.efectivo.add(e.saldo, MathContext.DECIMAL128);
                a.cargosEfectivo = a.cargosEfectivo.add(e.cargoPendiente, MathContext.DECIMAL128);
                a.abonosEfectivo = a.abonosEfectivo.add(e.abonoPendiente, MathContext.DECIMAL128);
            }
            SaldoAnterior s = anteriorSistema.get(d.cuenta().id());
            if (s != null) {
                a.movimiento = a.movimiento.add(s.saldo, MathContext.DECIMAL128);
                a.cargosMovimiento = a.cargosMovimiento.add(s.cargoPendiente, MathContext.DECIMAL128);
                a.abonosMovimiento = a.abonosMovimiento.add(s.abonoPendiente, MathContext.DECIMAL128);
            }
        }

        boolean conCargos = "S".equals(entrada.p_IncluyeCargosPendientes);
        boolean conAbonos = "S".equals(entrada.p_IncluyeAbonosPendientes);
        // totals of the last day of the range, per account (only the days that passed the filter are summed)
        Map<Long, Acumulado> totalesHasta = new HashMap<>();
        for (Cuenta c : unicas.values()) {
            totalesHasta.put(c.id(), new Acumulado());
        }

        for (Dia d : dias) {
            if (!pasaFiltro(entrada, d.acumulado().efectivo)) {
                continue;
            }
            Acumulado a = d.acumulado();
            SaldoPorFechaEfectiva r = new SaldoPorFechaEfectiva();
            r.IdCuentaInversion = d.cuenta().id();
            r.CodigoCuentaInversion = d.cuenta().codigo();
            r.IdCliente = d.cuenta().idCliente();
            r.IdPortafolio = d.cuenta().idPortafolio();
            r.FechaSaldo = d.fecha().toString();
            r.IdMoneda = d.cuenta().idMoneda();
            r.SaldoDisponible = a.efectivo;
            r.SaldoCargosPendientes = a.cargosEfectivo;
            r.SaldoAbonosPendientes = a.abonosEfectivo;
            r.Saldo = neto(a.efectivo, a.cargosEfectivo, a.abonosEfectivo, conCargos, conAbonos);
            if (d.fecha().equals(hasta)) {
                Acumulado t = totalesHasta.get(d.cuenta().id());
                t.efectivo = t.efectivo.add(a.efectivo, MathContext.DECIMAL128);
                t.cargosEfectivo = t.cargosEfectivo.add(a.cargosEfectivo, MathContext.DECIMAL128);
                t.abonosEfectivo = t.abonosEfectivo.add(a.abonosEfectivo, MathContext.DECIMAL128);
            }
            out.P_OUT_SALDOPORFECHAEFECTIVA.add(r);
        }
        for (Dia d : dias) {
            // LEGACY DEFECT kept (see README CTA-A-84): the movement-date list is filtered by the EFFECTIVE-date balance
            if (!pasaFiltro(entrada, d.acumulado().efectivo)) {
                continue;
            }
            Acumulado a = d.acumulado();
            SaldoPorFechaMovimiento r = new SaldoPorFechaMovimiento();
            r.IdCuentaInversion = d.cuenta().id();
            r.CodigoCuentaInversion = d.cuenta().codigo();
            r.IdCliente = d.cuenta().idCliente();
            r.IdPortafolio = d.cuenta().idPortafolio();
            r.FechaSaldo = d.fecha().toString();
            r.IdMoneda = d.cuenta().idMoneda();
            r.SaldoDisponible = a.movimiento;
            r.SaldoCargosPendientes = a.cargosMovimiento;
            r.SaldoAbonosPendientes = a.abonosMovimiento;
            r.Saldo = neto(a.movimiento, a.cargosMovimiento, a.abonosMovimiento, conCargos, conAbonos);
            if (d.fecha().equals(hasta)) {
                Acumulado t = totalesHasta.get(d.cuenta().id());
                t.movimiento = t.movimiento.add(a.movimiento, MathContext.DECIMAL128);
                t.cargosMovimiento = t.cargosMovimiento.add(a.cargosMovimiento, MathContext.DECIMAL128);
                t.abonosMovimiento = t.abonosMovimiento.add(a.abonosMovimiento, MathContext.DECIMAL128);
            }
            out.P_OUT_SALDOPORFECHAMOVIMIENTO.add(r);
        }

        for (Cuenta c : cuentas) {
            Acumulado t = totalesHasta.get(c.id());
            SaldoAnterior e = anteriorEfectivo.getOrDefault(c.id(), new SaldoAnterior());
            SaldoAnterior s = anteriorSistema.getOrDefault(c.id(), new SaldoAnterior());
            SaldoInicialFinal r = new SaldoInicialFinal();
            r.IdCuentaInversion = c.id();
            r.SaldoEfectivoInicial = neto(e.saldo, e.cargoPendiente, e.abonoPendiente, conCargos, conAbonos);
            r.SaldoEfectivoFinal = neto(t.efectivo, t.cargosEfectivo, t.abonosEfectivo, conCargos, conAbonos);
            r.SaldoMovimientoInicial = neto(s.saldo, s.cargoPendiente, s.abonoPendiente, conCargos, conAbonos);
            r.SaldoMovimientoFinal = neto(t.movimiento, t.cargosMovimiento, t.abonosMovimiento, conCargos, conAbonos);
            out.P_OUT_SALDOINICIALFINAL.add(r);
        }

        jdbc.getJdbcTemplate().execute(SQL_LIMPIAR_PASO);
        log.info("FIN::SaldoCuentaInversion");
        return out;
    }

    private List<Cuenta> cuentasDeLaConsulta(Set<Long> portafolios, Set<String> monedas, long idTrabajo) {
        MapSqlParameterSource p = new MapSqlParameterSource()
                .addValue("FiltroMoneda", monedas.isEmpty() ? 1L : 0L)
                // an empty IN list is sent as one NULL (matches nothing), as the legacy SQL helper did
                .addValue("IdMoneda", monedas.isEmpty() ? Collections.singletonList(null) : new ArrayList<>(monedas));
        String sql = SQL_CUENTAS_TODAS;
        if (!portafolios.isEmpty()) {
            p.addValue("IdTrabajo", idTrabajo);
            sql = SQL_CUENTAS_DE_PORTAFOLIOS;
        }
        return jdbc.queryForList(sql, p).stream()
                .map(r -> new Cuenta(num(r.get("IdCuentaInversion")), (String) r.get("Codigo"), num(r.get("IdCliente")),
                        num(r.get("IdPortafolio")), (String) r.get("IdMoneda"), (String) r.get("CuentaInversionPrincipal")))
                .toList();
    }

    private Map<Long, SaldoAnterior> saldosAnteriores(String sql, MapSqlParameterSource p) {
        Map<Long, SaldoAnterior> m = new HashMap<>();
        for (Map<String, Object> r : jdbc.queryForList(sql, p)) {
            SaldoAnterior s = new SaldoAnterior();
            s.saldo = (BigDecimal) r.get("SaldoAnterior");
            s.cargoPendiente = (BigDecimal) r.get("SaldoCargoPendienteAnterior");
            s.abonoPendiente = (BigDecimal) r.get("SaldoAbonoPendienteAnterior");
            m.put(num(r.get("IdCuentaInversion")), s);
        }
        return m;
    }

    private static Mov aMovimiento(Map<String, Object> r) {
        return new Mov(num(r.get("IdCuentaInversion")), fecha(r.get("FechaSistema")), fecha(r.get("FechaEfectivaMovimiento")),
                (BigDecimal) r.get("Monto"), (BigDecimal) r.get("MontoCargoPendiente"), (BigDecimal) r.get("MontoAbonoPendiente"));
    }

    /**
     * Keeps a day when the balance matches the requested filter. LEGACY DEFECT kept (see README CTA-A-84): "solo saldo disponible
     * negativo" means {@code <> 0} (positive balances pass too).
     */
    private static boolean pasaFiltro(Entrada e, BigDecimal efectivo) {
        boolean negativo = "S".equals(e.p_SoloSaldoNegativo);
        boolean disponible = "S".equals(e.p_SoloSaldoDisponible);
        boolean dispNegativo = "S".equals(e.p_SoloSaldoDisponibleNegativo);
        int signo = efectivo.signum();
        return (signo > 0 && disponible && !negativo && !dispNegativo)
                || (signo < 0 && negativo && !disponible && !dispNegativo)
                || (signo != 0 && !negativo && !disponible && dispNegativo)
                || (!negativo && !disponible && !dispNegativo);
    }

    private static BigDecimal neto(BigDecimal saldo, BigDecimal cargos, BigDecimal abonos, boolean conCargos, boolean conAbonos) {
        BigDecimal r = saldo;
        if (conCargos) {
            r = r.subtract(cargos, MathContext.DECIMAL128);
        }
        if (conAbonos) {
            r = r.add(abonos, MathContext.DECIMAL128);
        }
        return r;
    }

    private static Long num(Object o) {
        return o == null ? null : ((Number) o).longValue();
    }

    private static LocalDate fecha(Object o) {
        if (o instanceof java.sql.Date d) {
            return d.toLocalDate();
        }
        if (o instanceof Timestamp t) {
            return t.toLocalDateTime().toLocalDate();
        }
        return LocalDate.parse(o.toString().substring(0, 10));
    }
}
