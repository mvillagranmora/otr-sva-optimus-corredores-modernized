package cl.bcs.tesoreria.javacall;

import static cl.bcs.tesoreria.javacall.TesoreriaJdbcHelper.loadSql;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonObject;

import cl.bcs.tesoreria.javacall.comun.Consultas;
import cl.bcs.tesoreria.javacall.comun.Filas;
import cl.bcs.tesoreria.javacall.comun.JsonLegacy;
import cl.bcs.tesoreria.javacall.comun.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;

/**
 * Legacy {@code cl.bcs.tesoreria.SaldoPendienteDeLiquidar}, behaviours TES-A-690..710: pending-to-settle balance of each requested client
 * (person, portfolio, currency) for every day of a date range: the movements and comprobantes inside the range up to that day, plus the balance
 * carried from before the range. The persons are staged in a session table and the resolved clients in a work table that the queries join.
 */
@Service
public class SaldoPendienteDeLiquidarService {

    private static final String SQL_CLIENTE_PORTAFOLIO = loadSql("sql/SaldoPendienteDeLiquidar_SelectClientePortafolio.sql");
    private static final String SQL_INSERTAR_PERSONAS = loadSql("sql/SaldoPendienteDeLiquidar_InsertPersonas.sql");
    private static final String SQL_BORRAR_PERSONAS = loadSql("sql/SaldoPendienteDeLiquidar_DeletePersonas.sql");
    private static final String SQL_SALDO_ANTERIOR_MOVIMIENTOS = loadSql("sql/SaldoPendienteDeLiquidar_SelectSaldoAnteriorMovimientos.sql");
    private static final String SQL_SALDO_ANTERIOR_COMPROBANTES = loadSql("sql/SaldoPendienteDeLiquidar_SelectSaldoAnteriorComprobantes.sql");
    private static final String SQL_MOVIMIENTOS = loadSql("sql/SaldoPendienteDeLiquidar_SelectMovimientos.sql");
    private static final String SQL_COMPROBANTES = loadSql("sql/SaldoPendienteDeLiquidar_SelectComprobantes.sql");
    private static final String SQL_INSERTAR_TABLA_PASO = loadSql("sql/SaldoPendienteDeLiquidar_InsertSaldoTablaPaso.sql");
    private static final String SQL_BORRAR_TABLA_PASO = loadSql("sql/SaldoPendienteDeLiquidar_DeleteSaldoTablaPaso.sql");

    // ------------------------------------------------------------------ JavaCall contract (legacy names)

    public static class EntradaSaldoPendienteDeLiquidar extends BcsPojo {
        public Long p_IdEmpresa;
        public String p_FechaDesde;
        public String p_FechaHasta;
        public List<SocioConsulta> p_SociosAConsultar = new ArrayList<>();
    }

    public static class SocioConsulta extends BcsPojo {
        public Long IdPersona;
        public Long IdPortafolio;
        public String IdMoneda;
    }

    public static class SalidaSaldoPendienteDeLiquidar extends BcsPojo {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
        public List<SaldoPorFechaLiquidacion> P_OUT_SALDOPORFECHALIQUIDACION = new ArrayList<>();
    }

    public static class SaldoPorFechaLiquidacion extends BcsPojo {
        public Long IdPersona;
        public Long IdCliente;
        public Long IdPortafolio;
        public String FechaSaldo;
        public String IdMoneda;
        public BigDecimal SaldoMovIng = BigDecimal.ZERO;
        public BigDecimal SaldoMovEgr = BigDecimal.ZERO;
        public BigDecimal Saldo = BigDecimal.ZERO;

        void acumular(BigDecimal ingreso, BigDecimal egreso, BigDecimal saldo) {
            SaldoMovIng = SaldoMovIng.add(ingreso, MathContext.DECIMAL128);
            SaldoMovEgr = SaldoMovEgr.add(egreso, MathContext.DECIMAL128);
            Saldo = Saldo.add(saldo, MathContext.DECIMAL128);
        }
    }

    public static class SaldoAnterior extends BcsPojo {
        public String Id;
        public Long IdPersona;
        public Long IdPortafolio;
        public String IdMoneda;
        public BigDecimal TotalMontoIngresoAnterior = BigDecimal.ZERO;
        public BigDecimal TotalMontoEgresoAnterior = BigDecimal.ZERO;
        public BigDecimal TotalMontoAnterior = BigDecimal.ZERO;
    }

    public static class SocioProceso extends BcsPojo {
        public String Id;
        public Long IdPersona;
        public Long IdCliente;
        public Long IdPortafolio;
        public String IdMoneda;

        public SocioProceso() {
        }

        public SocioProceso(long idPersona, long idCliente, long idPortafolio, String idMoneda) {
            this.IdPersona = idPersona;
            this.IdCliente = idCliente;
            this.IdPortafolio = idPortafolio;
            this.IdMoneda = idMoneda;
        }
    }

    public static class MovimientoTesoreria extends BcsPojo {
        public String Id;
        public Date FechaLiquidacion;
        public Date FechaMovimiento;
        public Long IdPersona;
        public Long IdPortafolio;
        public String IdMoneda;
        public BigDecimal MontoMOVING;
        public BigDecimal MontoMOVEGR;
        public BigDecimal Monto;
    }

    private final NamedParameterJdbcTemplate jdbc;

    public SaldoPendienteDeLiquidarService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Legacy {@code call} (plain Gson, as the legacy). */
    @Transactional(rollbackFor = Exception.class)
    public JsonObject execute(JsonObject entrada) throws Exception {
        EntradaSaldoPendienteDeLiquidar in = JsonLegacy.ENTRADA_SALIDA.fromJson(entrada, EntradaSaldoPendienteDeLiquidar.class);
        return JsonLegacy.ENTRADA_SALIDA.toJsonTree(logica(in)).getAsJsonObject();
    }

    /** Legacy {@code logica}. */
    @Transactional(rollbackFor = Exception.class)
    public SalidaSaldoPendienteDeLiquidar logica(EntradaSaldoPendienteDeLiquidar entrada) throws Exception {
        SalidaSaldoPendienteDeLiquidar out = new SalidaSaldoPendienteDeLiquidar();
        Date desde = Fecha.toDate(entrada.p_FechaDesde);
        Date hasta = Fecha.toDate(entrada.p_FechaHasta);

        // persons asked for, once each, staged in the session table
        Set<Long> personas = new HashSet<>();
        List<Map<String, Object>> filasPersona = new ArrayList<>();
        for (SocioConsulta socio : entrada.p_SociosAConsultar) {
            if (personas.add(socio.IdPersona)) {
                filasPersona.add(new HashMap<>(Collections.singletonMap("IdPersona", socio.IdPersona)));
            }
        }
        Consultas.lote(jdbc, SQL_INSERTAR_PERSONAS, filasPersona);
        Map<String, SocioProceso> clientes = clientesDePersonas();
        jdbc.getJdbcOperations().execute(SQL_BORRAR_PERSONAS);

        List<SocioProceso> sociosProceso = sociosAProcesar(entrada, clientes);
        List<SaldoFecha> saldosFecha = completarRangoDeFechas(sociosProceso, desde, hasta);

        // work table the balance queries join
        Consultas.lote(jdbc, SQL_INSERTAR_TABLA_PASO, sociosProceso.stream().map(SaldoPendienteDeLiquidarService::filaSocio).toList());

        Map<String, Object> params = new HashMap<>();
        params.put("FechaDesde", entrada.p_FechaDesde);
        Map<String, SaldoAnterior> anteriorMovimientos = primeroPorSocio(jdbc.query(SQL_SALDO_ANTERIOR_MOVIMIENTOS, params, Filas.de(SaldoAnterior.class)));
        Map<String, SaldoAnterior> anteriorComprobantes = primeroPorSocio(jdbc.query(SQL_SALDO_ANTERIOR_COMPROBANTES, params, Filas.de(SaldoAnterior.class)));
        params.put("FechaHasta", entrada.p_FechaHasta);
        Map<String, MovimientoTesoreria> movimientos = primeroPorSocio(jdbc.query(SQL_MOVIMIENTOS, params, Filas.de(MovimientoTesoreria.class)));
        Map<String, MovimientoTesoreria> comprobantes = primeroPorSocio(jdbc.query(SQL_COMPROBANTES, params, Filas.de(MovimientoTesoreria.class)));

        for (SaldoFecha saldo : saldosFecha) {
            SaldoPorFechaLiquidacion s = new SaldoPorFechaLiquidacion();
            s.IdPersona = saldo.IdPersona;
            s.IdCliente = saldo.IdCliente;
            s.IdPortafolio = saldo.IdPortafolio;
            s.IdMoneda = saldo.IdMoneda;
            s.FechaSaldo = Fecha.stringToSqlDate(saldo.Fecha);
            acumularMovimiento(s, movimientos.get(saldo.Id), desde, saldo.Fecha);
            acumularMovimiento(s, comprobantes.get(saldo.Id), desde, saldo.Fecha);
            acumularAnterior(s, anteriorMovimientos.get(saldo.Id));
            acumularAnterior(s, anteriorComprobantes.get(saldo.Id));
            out.P_OUT_SALDOPORFECHALIQUIDACION.add(s);
        }
        jdbc.getJdbcOperations().execute(SQL_BORRAR_TABLA_PASO);
        return out;
    }

    /** Every (person, portfolio, currency) of the staged persons, keyed by {@code persona_portafolio_moneda}; the first row of a key wins. */
    private Map<String, SocioProceso> clientesDePersonas() {
        Map<String, SocioProceso> clientes = new HashMap<>();
        jdbc.getJdbcOperations().query(SQL_CLIENTE_PORTAFOLIO, rs -> {
            SocioProceso soc = new SocioProceso(rs.getLong("IdPersona"), rs.getLong("IdCliente"), rs.getLong("IdPortafolio"), rs.getString("IdMoneda"));
            soc.Id = soc.IdPersona + "_" + soc.IdPortafolio + "_" + soc.IdMoneda;
            clientes.putIfAbsent(soc.Id, soc);
        });
        return clientes;
    }

    /**
     * The clients to compute: those asked with portfolio AND currency that exist in the client query.
     * LEGACY DEFECT kept (see README TES-A-709 / 710): the list of "pending" clients (a socio without portfolio or currency) is never filled in the
     * legacy, so such a socio is silently not processed.
     */
    private static List<SocioProceso> sociosAProcesar(EntradaSaldoPendienteDeLiquidar entrada, Map<String, SocioProceso> clientes) {
        List<SocioProceso> procesar = new ArrayList<>();
        Set<Long> personas = new HashSet<>();
        Set<Long> portafolios = new HashSet<>();
        Set<String> monedas = new HashSet<>();
        for (SocioConsulta socio : entrada.p_SociosAConsultar) {
            if (socio.IdPortafolio == null || socio.IdMoneda == null) {
                continue;
            }
            SocioProceso sp = clientes.get(socio.IdPersona + "_" + socio.IdPortafolio + "_" + socio.IdMoneda);
            if (sp == null) {
                continue;
            }
            // each new person, portfolio or currency adds the client once
            boolean nuevaPersona = personas.add(sp.IdPersona);
            boolean nuevoPortafolio = portafolios.add(sp.IdPortafolio);
            boolean nuevaMoneda = monedas.add(sp.IdMoneda);
            if (nuevaPersona || nuevoPortafolio || nuevaMoneda) {
                procesar.add(sp);
            }
        }
        return procesar;
    }

    private record SaldoFecha(String Id, Long IdPersona, Long IdCliente, Long IdPortafolio, String IdMoneda, Date Fecha) {
    }

    private static List<SaldoFecha> completarRangoDeFechas(List<SocioProceso> socios, Date desde, Date hasta) {
        List<SaldoFecha> saldos = new ArrayList<>();
        for (SocioProceso socio : socios) {
            for (Date dia = desde; !dia.after(hasta); dia = Fecha.addDays(dia, 1)) {
                saldos.add(new SaldoFecha(socio.Id, socio.IdPersona, socio.IdCliente, socio.IdPortafolio, socio.IdMoneda, dia));
            }
        }
        return saldos;
    }

    private static Map<String, Object> filaSocio(SocioProceso s) {
        Map<String, Object> fila = new LinkedHashMap<>();
        fila.put("IdPersona", s.IdPersona);
        fila.put("IdCliente", s.IdCliente);
        fila.put("IdPortafolio", s.IdPortafolio);
        fila.put("IdMoneda", s.IdMoneda);
        return fila;
    }

    /**
     * Keeps the first row per client. LEGACY DEFECT kept (see README TES-A-7xx): only the first row per client is used from the movement queries,
     * so with several liquidation dates the row order of the database decides.
     */
    private static <T extends BcsPojo> Map<String, T> primeroPorSocio(List<T> filas) {
        Map<String, T> porId = new HashMap<>();
        for (T f : filas) {
            String id;
            if (f instanceof SaldoAnterior a) {
                id = a.Id = a.IdPersona + "_" + a.IdPortafolio + "_" + a.IdMoneda;
            } else {
                MovimientoTesoreria m = (MovimientoTesoreria) f;
                id = m.Id = m.IdPersona + "_" + m.IdPortafolio + "_" + m.IdMoneda;
            }
            porId.putIfAbsent(id, f);
        }
        return porId;
    }

    /** Adds the movement when its liquidation date is inside [start of the range, the day being computed]. */
    private static void acumularMovimiento(SaldoPorFechaLiquidacion s, MovimientoTesoreria m, Date desde, Date dia) {
        if (m != null && !m.FechaLiquidacion.before(desde) && !m.FechaLiquidacion.after(dia)) {
            s.acumular(m.MontoMOVING, m.MontoMOVEGR, m.Monto);
        }
    }

    private static void acumularAnterior(SaldoPorFechaLiquidacion s, SaldoAnterior a) {
        if (a != null) {
            s.acumular(a.TotalMontoIngresoAnterior, a.TotalMontoEgresoAnterior, a.TotalMontoAnterior);
        }
    }
}
