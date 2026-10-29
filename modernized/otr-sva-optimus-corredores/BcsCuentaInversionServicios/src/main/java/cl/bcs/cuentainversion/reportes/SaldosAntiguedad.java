package cl.bcs.cuentainversion.reportes;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

/**
 * Shared model of the aging-balance services (ActualizarSaldos, CargaInicial, GuardarSaldos; CTA-B-47..68): the balance rows of
 * {@code CTA.SaldosReporteAntiguedad} (two per account and day: G = general, I = intermediation), the certified cash movements that
 * move them, and the netting rule.
 */
public final class SaldosAntiguedad {

    public static final String MOVIMIENTO_CARGO = "MOVCAR";

    private SaldosAntiguedad() {
    }

    /** Row of {@code CTA.SaldosReporteAntiguedad}; also the batch parameters of the INSERT / UPSERT. */
    public static class Saldo {
        public Date FechaSistema;
        public Long IdCliente;
        public Long IdPortafolio;
        public String IdMoneda;
        public String TipoSaldo;
        public BigDecimal Saldo;

        /** Same balance stamped with another day. */
        public Saldo para(LocalDate fecha) {
            Saldo s = new Saldo();
            s.FechaSistema = Date.valueOf(fecha);
            s.IdCliente = IdCliente;
            s.IdPortafolio = IdPortafolio;
            s.IdMoneda = IdMoneda;
            s.TipoSaldo = TipoSaldo;
            s.Saldo = Saldo;
            return s;
        }

        public boolean esDe(Movimiento m) {
            return m.IdCliente.equals(IdCliente) && m.IdPortafolio.equals(IdPortafolio) && m.IdMoneda.equalsIgnoreCase(IdMoneda);
        }
    }

    /** Certified cash movement (FAC module with an RV voucher is intermediation "I", anything else general "G"). */
    public static class Movimiento {
        public Long IdCargoAbonoMovimiento;
        public Long IdCliente;
        public Long IdPortafolio;
        public String IdMoneda;
        public String TipoSaldo;
        public BigDecimal Monto;
        public Date FechaMovimiento;
        public String IdTipoMovimiento;

        public LocalDate dia() {
            return FechaMovimiento.toLocalDate();
        }

        public boolean esDe(Movimiento otro) {
            return IdCliente.equals(otro.IdCliente) && IdPortafolio.equals(otro.IdPortafolio) && IdMoneda.equalsIgnoreCase(otro.IdMoneda);
        }

        public String cuenta() {
            return IdCliente + "-" + IdPortafolio + "-" + IdMoneda;
        }
    }

    /** Net movement of an account for a day: cargos subtract, abonos add. */
    public record Neto(BigDecimal general, BigDecimal intermediacion) {

        public static Neto de(List<Movimiento> movimientos) {
            BigDecimal general = BigDecimal.ZERO;
            BigDecimal intermediacion = BigDecimal.ZERO;
            for (Movimiento m : movimientos) {
                BigDecimal monto = MOVIMIENTO_CARGO.equalsIgnoreCase(m.IdTipoMovimiento) ? m.Monto.negate(MathContext.DECIMAL128) : m.Monto;
                if ("G".equalsIgnoreCase(m.TipoSaldo)) {
                    general = general.add(monto);
                } else if ("I".equalsIgnoreCase(m.TipoSaldo)) {
                    intermediacion = intermediacion.add(monto);
                }
            }
            return new Neto(general, intermediacion);
        }
    }

    /**
     * Applies the net movement to the first two balance rows of the account (whatever their order: the one typed "I" takes the
     * intermediation). The legacy assumes exactly two rows: with a single row {@code get(1)} throws IndexOutOfBoundsException.
     */
    public static void aplicar(List<Saldo> filasDeLaCuenta, Neto neto) {
        Saldo primera = filasDeLaCuenta.get(0);
        Saldo segunda = filasDeLaCuenta.get(1);
        if ("I".equalsIgnoreCase(primera.TipoSaldo)) {
            primera.Saldo = primera.Saldo.add(neto.intermediacion(), MathContext.DECIMAL128);
            segunda.Saldo = segunda.Saldo.add(neto.general(), MathContext.DECIMAL128);
        } else {
            segunda.Saldo = segunda.Saldo.add(neto.intermediacion(), MathContext.DECIMAL128);
            primera.Saldo = primera.Saldo.add(neto.general(), MathContext.DECIMAL128);
        }
    }

    /** The G and I rows of a brand-new account with its first net movement. */
    public static List<Saldo> nuevaCuenta(Movimiento cuenta, LocalDate fecha, Neto neto) {
        Saldo general = nueva(cuenta, fecha, "G", neto.general());
        Saldo intermediacion = nueva(cuenta, fecha, "I", neto.intermediacion());
        return List.of(general, intermediacion);
    }

    private static Saldo nueva(Movimiento cuenta, LocalDate fecha, String tipo, BigDecimal monto) {
        Saldo s = new Saldo();
        s.FechaSistema = Date.valueOf(fecha);
        s.IdCliente = cuenta.IdCliente;
        s.IdPortafolio = cuenta.IdPortafolio;
        s.IdMoneda = cuenta.IdMoneda;
        s.TipoSaldo = tipo;
        s.Saldo = monto;
        return s;
    }
}
