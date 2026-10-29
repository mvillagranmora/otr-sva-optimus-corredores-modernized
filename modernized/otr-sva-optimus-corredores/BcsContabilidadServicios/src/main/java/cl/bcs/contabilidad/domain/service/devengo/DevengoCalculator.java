package cl.bcs.contabilidad.domain.service.devengo;

import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabEventoRef;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabMovimientoRef;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Movement and event derivation of the Devengo* centralizations (CONT-C-01..18) from one CarteraValorizadaIFRF row.
 *
 * <p>The legacy Java classes compute nothing: the accrued amounts are pre-computed in the valuation report. What legacy
 * derived lives in its SQL and is reproduced here:
 * <ul>
 *   <li>CentralizaDevengo*_SelectEventos.sql: {@code ROUND(x, 0)} of every amount (HANA rounds half away from zero), the
 *       sign test on the rounded amount ({@code >= 0} / {@code < 0}, false for NULL), the event codes and names, and the
 *       UNION ALL branch order;</li>
 *   <li>CentralizaDevengo*_SelectMovimientos.sql: the IdTipoMovimiento CASE, {@code ABS(MontoActual)} (not rounded),
 *       {@code YEAR} / {@code TO_CHAR(.., 'MM-YYYY')} of FechaOperacion, and the row filters.</li>
 * </ul>
 */
@Component
public class DevengoCalculator {

    private static final String COMPAC = "COMPAC";
    private static final String VENPAC = "VENPAC";

    /** One UNION ALL branch of a legacy _SelectEventos.sql. */
    private record Rama(String codigo, String nombre, Function<Redondeados, BigDecimal> monto,
                        Predicate<BigDecimal> condicion) {

        static Rama siempre(String codigo, String nombre, Function<Redondeados, BigDecimal> monto) {
            return new Rama(codigo, nombre, monto, m -> true);
        }

        static Rama positivo(String codigo, String nombre, Function<Redondeados, BigDecimal> monto) {
            return new Rama(codigo, nombre, monto, m -> m != null && m.signum() >= 0);
        }

        static Rama negativo(String codigo, String nombre, Function<Redondeados, BigDecimal> monto) {
            return new Rama(codigo, nombre, monto, m -> m != null && m.signum() < 0);
        }
    }

    /** The {@code v_TabEventosPaso} CTE: the row's amounts after {@code ROUND(x, 0)}. */
    private record Redondeados(BigDecimal compra, BigDecimal pactado, BigDecimal actual, BigDecimal proximo,
                               BigDecimal interes, BigDecimal interesCorrido, BigDecimal reajuste) {}

    /** {@code ROUND(x, 0)}: half away from zero, scale 0; {@code ROUND(NULL, 0)} is NULL. */
    public BigDecimal redondearMonto(BigDecimal monto) {
        return monto == null ? null : monto.setScale(0, RoundingMode.HALF_UP);
    }

    /** The movement of a row (legacy _SelectMovimientos.sql), empty when the type's SQL filter drops the row. */
    public Optional<TabMovimientoRef> movimiento(TipoDevengo tipo, Long idTipoCentralizacion, String fechaContable,
                                                 CarteraValorizada c) {
        return switch (tipo) {
            // TasaCompraCarteraPactada_SelectMovimientos.sql:4-8,24-25: no IdTipoOperacion filter, ELSE ten '?'
            case TASA_COMPRA_CARTERA_PACTADA -> Optional.of(movimiento(idTipoCentralizacion, fechaContable, c,
                    switch (String.valueOf(c.idTipoOperacion())) {
                        case VENPAC -> "CARTPACVEN";
                        case COMPAC -> "CARTPACCOM";
                        default -> "??????????";
                    }, c.idValorizacion(), c.folioOperacion(), 0L));
            // TasaPacto_SelectMovimientos.sql:3-7,26
            case TASA_PACTO -> esPacto(c)
                    ? Optional.of(movimiento(idTipoCentralizacion, fechaContable, c,
                            VENPAC.equals(c.idTipoOperacion()) ? "PACTOVTA" : "PACTOCOMP",
                            c.idValorizacion(), c.folioOperacion(), 0L))
                    : Optional.empty();
            // CarteraPropiaLibreYGarantia_SelectMovimientos.sql:2-7,22
            case CARTERA_PROPIA_LIBRE_Y_GARANTIA -> esRentaFija(c)
                    ? Optional.of(movimiento(idTipoCentralizacion, fechaContable, c, "CARTERARF",
                            c.idCartera(), c.folioCartera(), c.folioCartera()))
                    : Optional.empty();
        };
    }

    /** The events of a row (legacy _SelectEventos.sql), in UNION ALL branch order. */
    public List<TabEventoRef> eventos(TipoDevengo tipo, Long idTipoCentralizacion, CarteraValorizada c) {
        boolean incluida = tipo == TipoDevengo.CARTERA_PROPIA_LIBRE_Y_GARANTIA ? esRentaFija(c) : esPacto(c);
        if (!incluida) return List.of();

        Long idMovimiento = tipo == TipoDevengo.CARTERA_PROPIA_LIBRE_Y_GARANTIA ? c.idCartera() : c.idValorizacion();
        Redondeados r = new Redondeados(redondearMonto(c.montoCompra()), redondearMonto(c.montoPactado()),
                redondearMonto(c.montoActual()), redondearMonto(c.montoProximo()), redondearMonto(c.montoInteres()),
                redondearMonto(c.montoInteresCorrido()), redondearMonto(c.montoReajuste()));

        List<TabEventoRef> eventos = new ArrayList<>();
        for (Rama rama : ramas(tipo, VENPAC.equals(c.idTipoOperacion()) ? "VENTA" : "COMPRA")) {
            BigDecimal monto = rama.monto().apply(r);
            if (rama.condicion().test(monto)) {
                eventos.add(new TabEventoRef(idTipoCentralizacion, idMovimiento, rama.codigo(), rama.nombre(), monto,
                        c.idMonedaInstrumento()));
            }
        }
        return eventos;
    }

    private static List<Rama> ramas(TipoDevengo tipo, String lado) {
        return switch (tipo) {
            // TasaCompraCarteraPactada_SelectEventos.sql:21-45 (note the two spaces of 'Interes  Diario', :39)
            case TASA_COMPRA_CARTERA_PACTADA -> List.of(
                    Rama.siempre("MONTO_OPERACION", "Monto Operacion", Redondeados::compra),
                    Rama.siempre("MONTO_PACTADO", "Monto Pactado", Redondeados::pactado),
                    Rama.siempre("MONTO_ANTERIOR_TC_PACTO_" + lado, "Monto Anterior a Tasa Pacto", Redondeados::actual),
                    Rama.siempre("MONTO_PRESENTE_TC_PACTO_" + lado, "Monto Presente a Tasa Pacto", Redondeados::proximo),
                    Rama.positivo("INTERES_DIARIO_TC_PACTO_" + lado, "Interes Diario", Redondeados::interes),
                    Rama.positivo("INTERES_ACUMULADO_TC_PACTO_" + lado, "Interes Acumulado", Redondeados::interesCorrido),
                    Rama.positivo("REAJUSTE_TC_PACTO_" + lado, "Reajuste", Redondeados::reajuste),
                    Rama.negativo("INTERES_NEGATIVO_DIARIO_TC_PACTO_" + lado, "Interes  Diario", Redondeados::interes),
                    Rama.negativo("INTERES_NEG_ACUMULADO_TC_PACTO_" + lado, "Interes Acumulado", Redondeados::interesCorrido),
                    Rama.negativo("REAJUSTE_NEGATIVO_TC_PACTO_" + lado, "Reajuste", Redondeados::reajuste));
            // TasaPacto_SelectEventos.sql:21-33: no WHERE on any branch
            case TASA_PACTO -> List.of(
                    Rama.siempre("MONTO_OPERACION", "Monto Operacion", Redondeados::compra),
                    Rama.siempre("MONTO_PACTADO", "Monto Pactado", Redondeados::pactado),
                    Rama.siempre("MONTO_ANTERIOR_TP_PACTO_" + lado, "Monto Anterior a Tasa Pacto", Redondeados::actual),
                    Rama.siempre("MONTO_PRESENTE_TP_PACTO_" + lado, "Monto Presente a Tasa Pacto", Redondeados::proximo),
                    Rama.siempre("INTERES_DIARIO_TP_PACTO_" + lado, "Interes Diario", Redondeados::interes),
                    Rama.siempre("INTERES_ACUMULADO_TP_PACTO_" + lado, "Interes Acumulado", Redondeados::interesCorrido),
                    Rama.siempre("REAJUSTE_TP_PACTO_" + lado, "Reajuste", Redondeados::reajuste));
            // CarteraPropiaLibreYGarantia_SelectEventos.sql:19-35: MontoPactado / MontoInteresCorrido unused
            case CARTERA_PROPIA_LIBRE_Y_GARANTIA -> List.of(
                    Rama.siempre("MONTO_COMPRA", "Monto Compra", Redondeados::compra),
                    Rama.siempre("MONTO_ACTUAL_TASA_COMPRA", "Monto Actual a Tasa Compra", Redondeados::actual),
                    Rama.siempre("MONTO_PROXIMO_TASA_COMPRA", "Monto Proximo a Tasa Compra", Redondeados::proximo),
                    Rama.positivo("INTERES_DIARIO_TASA_COMPRA", "Interes Diario a Tasa Compra", Redondeados::interes),
                    Rama.negativo("INTERES_NEGATIVO_DIARIO_TASA_COMPRA", "Interes Diario a Tasa Compra", Redondeados::interes),
                    Rama.positivo("REAJUSTE_TASA_COMPRA", "Reajuste a Tasa Compra", Redondeados::reajuste),
                    Rama.negativo("REAJUSTE_NEGATIVO_TASA_COMPRA", "Reajuste a Tasa Compra", Redondeados::reajuste));
        };
    }

    /** Columns common to the three _SelectMovimientos.sql. */
    private static TabMovimientoRef movimiento(Long idTipoCentralizacion, String fechaContable, CarteraValorizada c,
                                               String idTipoMovimiento, Long idMovimiento, Long numero, Long linea) {
        var fecha = c.fechaOperacion();
        return new TabMovimientoRef(idTipoCentralizacion, idMovimiento, idTipoMovimiento, c.idEmpresa(), numero, linea,
                fechaContable, c.documentoIdentidad(), c.idPortafolio(),
                fecha == null ? null : (long) fecha.getYear(),
                fecha == null ? null : "%02d-%04d".formatted(fecha.getMonthValue(), fecha.getYear()),
                c.montoActual() == null ? null : c.montoActual().abs(),
                c.idMonedaSistema(), "");
    }

    /** {@code IdTipoOperacion IN ('COMPAC','VENPAC')}: case-sensitive, NULL excluded. */
    private static boolean esPacto(CarteraValorizada c) {
        return COMPAC.equals(c.idTipoOperacion()) || VENPAC.equals(c.idTipoOperacion());
    }

    /** {@code IdClasificacionTipoInstrumento IN ('RF','IF')}. */
    private static boolean esRentaFija(CarteraValorizada c) {
        return "RF".equals(c.idClasificacionTipoInstrumento()) || "IF".equals(c.idClasificacionTipoInstrumento());
    }
}
