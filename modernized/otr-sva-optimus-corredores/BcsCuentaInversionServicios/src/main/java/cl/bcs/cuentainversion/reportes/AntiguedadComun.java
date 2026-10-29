package cl.bcs.cuentainversion.reportes;

import org.springframework.dao.DataAccessException;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Pieces shared by the aging / creditors consultations (CTA-B-69..108): the staging-table row, the client row of the data query,
 * the certified movement row and a few rules (amount formatting, ordering of the movements, indexing by portfolio).
 */
public final class AntiguedadComun {

    private AntiguedadComun() {
    }

    /** Account of the report as staged in the working table (and read back by the SQL of each report). */
    public static class CuentaPaso {
        public Long IdCuentaInversion;
        public Long IdCliente;
        public Long IdPortafolio;
        public String IdMoneda;
        public BigDecimal SaldoFinal = BigDecimal.ZERO;
        public String Observacion = "";
    }

    public static class Moneda {
        public String IdMoneda;
    }

    /** Client / portfolio data of the staged accounts. {@code Relacionado} is "SI" or "NO" (see CTA-B-09). */
    public static class ClientePortafolio {
        public Long IdPersona;
        public String NombreCompleto;
        public String DocumentoIdentidad;
        public Long IdCliente;
        public Long IdPortafolio;
        public String CodigoPortafolio;
        public String Relacionado;
    }

    /** Certified charge (MOVCAR) or credit (MOVABN) of a staged account. */
    public static class Movimiento {
        public Long IdCargoAbonoMovimiento;
        public Long IdCliente;
        public Long IdPortafolio;
        public String IdMoneda;
        public Long IdCuentaInversion;
        public BigDecimal Monto;
        public String TipoOperacion;
        public Date FechaMovimiento;
        public Timestamp FechaCertificacion;
        public Date FechaEfectivaMovimiento;
    }

    /** Newest first: effective date, then certification time, then id, all descending. */
    public static final Comparator<Movimiento> MAS_RECIENTE_PRIMERO =
            Comparator.comparing((Movimiento m) -> m.FechaEfectivaMovimiento)
                    .thenComparing(m -> m.FechaCertificacion)
                    .thenComparing(m -> m.IdCargoAbonoMovimiento)
                    .reversed();

    /** Balance to age: available + held, or only the available one when the client balance is consolidated. */
    public static List<CuentaPaso> prepararSaldoFinal(List<SaldosCuentaInversion.Saldo> saldos, boolean sumaRetenido) {
        return saldos.stream().map(s -> {
            CuentaPaso c = new CuentaPaso();
            c.IdCuentaInversion = s.IdCuentaInversion;
            c.IdCliente = s.IdCliente;
            c.IdPortafolio = s.IdPortafolio;
            c.IdMoneda = s.IdMoneda;
            c.SaldoFinal = sumaRetenido ? s.total() : s.SaldoDisponible;
            return c;
        }).toList();
    }

    /**
     * Inserts the staged accounts for {@code idTrabajo}. The legacy swallowed the SQL failure and reported it as "false" (so did a
     * delete failure): the caller decides what that means.
     */
    public static boolean insertarPaso(SqlReportes sql, String sqlInsert, List<CuentaPaso> cuentas, Long idTrabajo) {
        try {
            sql.lote(sqlInsert.replace(":IdTrabajo", String.valueOf(idTrabajo)), cuentas);
            return !cuentas.isEmpty();
        } catch (DataAccessException e) {
            return false;
        }
    }

    /** Legacy {@code Utils.List2Hash}: a repeated key is an error ("key field is not unique"). */
    public static Map<Long, ClientePortafolio> porPortafolio(List<ClientePortafolio> clientes) throws Exception {
        Map<Long, ClientePortafolio> m = new HashMap<>();
        for (ClientePortafolio c : clientes) {
            if (m.put(c.IdPortafolio, c) != null) {
                throw new Exception("key field is not unique");
            }
        }
        return m;
    }

    /** Whole currency units (truncated) for CLP, 2 decimals HALF_EVEN otherwise; always positive. */
    public static BigDecimal formatear(BigDecimal monto, boolean sinDecimales) {
        return (sinDecimales ? monto.setScale(0, RoundingMode.DOWN) : monto.setScale(2, RoundingMode.HALF_EVEN)).abs(MathContext.DECIMAL128);
    }

    /** a + b with the platform precision. */
    public static BigDecimal sumar(BigDecimal a, BigDecimal b) {
        return a.add(b, MathContext.DECIMAL128);
    }
}
