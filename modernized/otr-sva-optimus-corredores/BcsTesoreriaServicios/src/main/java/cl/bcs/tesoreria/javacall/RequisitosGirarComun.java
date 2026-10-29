package cl.bcs.tesoreria.javacall;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import cl.bcs.tesoreria.javacall.comun.Consultas;
import cl.bcs.tesoreria.javacall.comun.Filas;
import cl.bcs.tesoreria.port.out.DiccionarioMensajes;
import cl.bcs.utils.BcsPojo;

/**
 * Pieces shared by {@link RequisitosGirarComprobanteIngresoService} and {@link RequisitosGirarComprobanteEgresoService} (legacy
 * {@code RequisitosGirarComprobanteIngreso} / {@code ...Egreso}, TES-A-500..551): the contract classes, which are identical in both legacy
 * classes, and the "run a validation query, report every row that does not comply" step.
 */
final class RequisitosGirarComun {

    private static final Logger log = LoggerFactory.getLogger(RequisitosGirarComun.class);

    private RequisitosGirarComun() {
    }

    // ------------------------------------------------------------------ JavaCall contract (legacy names)

    public static class Comprobante extends BcsPojo {
        public long IdComprobante;
    }

    public static class Entrada extends BcsPojo {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_simularOperacion;
        public String p_UsaReglasNegocio;
        public String p_FechaMovimiento;
        public List<Comprobante> p_ComprobantesAGirar;
        public String p_GeneraNomina;
        public String p_GeneraTransferencia;
        public String p_IdMedioDePago;
        public String p_IdCuentaBancariaCorredora;
        public BigDecimal p_Monto;
        public List<Comprobante> p_FormasDePago;
        public String p_usaTalonarioCheque;
        public String p_IdModalidadDePago;
    }

    public static class InformacionDetalle {
        public String Codigo;
        public String Descripcion;
    }

    public static class MediosPago extends BcsPojo {
        public String IdMedioDePago;
        public BigDecimal MontoPorMedioDePago;
    }

    /** The result; it is also the row class of the queries that read the concept and the box (only the matching columns are filled). */
    public static class Salida extends BcsPojo {
        public String p_out_IdConcepto;
        public String p_out_NombreConcepto;
        public String p_out_IdMoneda;
        public String p_out_NombreMoneda;
        public String p_out_FechaLiquidacion;
        public Long p_out_IdCaja;
        public String p_out_NombreCaja;
        public int p_out_advertenciascontador;
        public List<MediosPago> p_out_MediosDePagoAGirar = new ArrayList<>();
        public List<InformacionDetalle> P_OUT_ERRORESDETALLE = new ArrayList<>();
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<Long> P_OUT_SECUENCIA = new ArrayList<>();
    }

    /** Row of the validation queries: {@code Cumple = 0} means the rule is broken. */
    public static class Validar extends BcsPojo {
        public int Prioridad;
        public Integer Cumple;
        public String CodigoError;
        public String CodigoDato;
        public String ValorDato;
        public long IdComprobante;
    }

    /** Row of the currency / liquidation-date query. */
    public static class MonedaFecha {
        public String p_out_IdMoneda;
        public String p_out_NombreMoneda;
        public Date p_out_FechaLiquidacion;
    }

    // ------------------------------------------------------------------ steps

    /**
     * Legacy {@code SQL.batch(.., p_ComprobantesAGirar, Comprobante.class)}: a missing list escapes as the SQLException of the legacy batch
     * (LEGACY DEFECT kept, see README TES-A-524 / 551).
     */
    static void insertarComprobantes(NamedParameterJdbcTemplate jdbc, String sql, List<Comprobante> comprobantes) throws SQLException {
        if (comprobantes == null) {
            throw new SQLException("Null parameters. If parameters aren't need, pass an empty");
        }
        Consultas.lote(jdbc, sql, comprobantes.stream().map(c -> Map.of("IdComprobante", (Object) c.IdComprobante)).toList());
    }

    /** Runs a validation query and reports every row with {@code Cumple = 0} as an error of the result (counter fixed at 1, never incremented). */
    static void validar(NamedParameterJdbcTemplate jdbc, DiccionarioMensajes diccionario, Entrada entrada, Salida salida, String sql,
            Map<String, ?> parametros) {
        List<Validar> filas = jdbc.query(sql, parametros, Filas.de(Validar.class));
        filas.stream().filter(req -> req.Cumple.equals(0)).forEach(req -> agregarError(diccionario, entrada, salida, req));
    }

    /** The same rows, for the queries whose errors are reported only when some row does not comply (all-match check, as the legacy). */
    static void validarSiAlguno(NamedParameterJdbcTemplate jdbc, DiccionarioMensajes diccionario, Entrada entrada, Salida salida, String sql) {
        List<Validar> filas = jdbc.query(sql, Map.of(), Filas.de(Validar.class));
        boolean todasCumplen = filas.stream().allMatch(req -> req.Cumple == 1);
        if (!todasCumplen) {
            filas.stream().filter(req -> req.Cumple.equals(0)).forEach(req -> agregarError(diccionario, entrada, salida, req));
        }
    }

    private static void agregarError(DiccionarioMensajes diccionario, Entrada entrada, Salida salida, Validar req) {
        try {
            InformacionDetalle info = new InformacionDetalle();
            info.Codigo = req.CodigoError;
            info.Descripcion = diccionario.mensaje(entrada.p_IdEmpresa, req.CodigoError, Map.of()) + " " + req.ValorDato;
            salida.P_OUT_ERRORESDETALLE.add(info);
            salida.P_OUT_ERRORESCONTADOR = 1L; // LEGACY DEFECT kept (see README TES-A-522): the counter is set to 1, never incremented
        } catch (RuntimeException e) {
            log.debug("cannot add the error {}", req.CodigoError, e);
        }
    }

    /** Fills the result with the concept, the currency and date and the box found by the three summary queries (defaults as the legacy). */
    static void resumir(NamedParameterJdbcTemplate jdbc, Salida salida, String sqlConceptos, String sqlMonedaFecha, String sqlCaja) {
        List<Salida> conceptos = jdbc.query(sqlConceptos, Map.of(), Filas.de(Salida.class));
        List<MonedaFecha> monedaFecha = jdbc.query(sqlMonedaFecha, Map.of(), Filas.de(MonedaFecha.class));
        List<Salida> caja = jdbc.query(sqlCaja, Map.of(), Filas.de(Salida.class));
        Salida concepto = conceptos.isEmpty() ? new Salida() : conceptos.get(0);
        MonedaFecha mf = monedaFecha.isEmpty() ? new MonedaFecha() : monedaFecha.get(0);
        Salida c = caja.isEmpty() ? new Salida() : caja.get(0);
        salida.p_out_IdConcepto = orDefault(concepto.p_out_IdConcepto, "");
        salida.p_out_NombreConcepto = orDefault(concepto.p_out_NombreConcepto, "");
        salida.p_out_IdMoneda = orDefault(mf.p_out_IdMoneda, "");
        salida.p_out_NombreMoneda = orDefault(mf.p_out_NombreMoneda, "");
        salida.p_out_FechaLiquidacion = mf.p_out_FechaLiquidacion != null ? mf.p_out_FechaLiquidacion.toString() : "";
        salida.p_out_IdCaja = c.p_out_IdCaja != null ? c.p_out_IdCaja : 1L;
        salida.p_out_NombreCaja = orDefault(c.p_out_NombreCaja, "");
        salida.p_out_advertenciascontador = 0;
    }

    private static <T> T orDefault(T valor, T porDefecto) {
        return valor != null ? valor : porDefecto;
    }

    /** Runs {@code accion}; legacy ignored any failure of the final DROP of the temporary table. */
    static void ignorandoErrores(Consumer<String> accion, String sql) {
        try {
            accion.accept(sql);
        } catch (RuntimeException e) {
            log.debug("ignored: {}", sql, e);
        }
    }
}
