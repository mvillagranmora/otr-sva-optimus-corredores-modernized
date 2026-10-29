package cl.bcs.valo2.calculadoras;

import cl.bcs.valo2.TipoInteres;
import cl.bcs.valo2.ValorizadorException;
import cl.bcs.valo2.fechas.Fecha;

public final class CalculadoraPagares {
    public static double calcularCaptacion(
            final int tipoInteres,
            final double fraccionAno,
            final double rescate,
            final double tasa) {
        switch (tipoInteres) {
        case TipoInteres.SIMPLE:
            return calcularCaptacionInteresSimple(fraccionAno, rescate, tasa);
        case TipoInteres.COMPUESTO:
            return calcularCaptacionInteresCompuesto(fraccionAno, rescate, tasa);
        default:
            throw new ValorizadorException("Tipo de interes no implementado: " + tipoInteres);
        }
    }

    public static double calcularCaptacion(
            final int tipoInteres,
            final int diasAno,
            final Fecha fechaCaptacion,
            final Fecha fechaRescate,
            final double rescate,
            final double tasa) {
        final double fraccionAno = calcularFraccionAno(diasAno, fechaCaptacion, fechaRescate);
        return calcularCaptacion(tipoInteres, fraccionAno, rescate, tasa);
    }

    public static double calcularCaptacion(
            final int tipoInteres,
            final int diasAno,
            final int plazoEnDias,
            final double rescate,
            final double tasa) {
        final double fraccionAno = calcularFraccionAno(diasAno, plazoEnDias);
        return calcularCaptacion(tipoInteres, fraccionAno, rescate, tasa);
    }

    private static double calcularCaptacionInteresCompuesto(
            final double fraccionAno,
            final double rescate,
            final double tasa) {
        return rescate / Math.pow(1 + tasa, fraccionAno);
    }

    private static double calcularCaptacionInteresSimple(
            final double fraccionAno,
            final double rescate,
            final double tasa) {
        return rescate / (1 + tasa * fraccionAno);
    }

    public static double calcularFraccionAno(
            final int diasAno,
            final Fecha fechaCaptacion,
            final Fecha fechaRescate) {
        final int plazoEnDias = fechaRescate.diferenciaEnDias(fechaCaptacion);
        return calcularFraccionAno(diasAno, plazoEnDias);
    }

    public static double calcularFraccionAno(
            final int diasAno,
            final int plazoEnDias) {
        return (double) plazoEnDias / (double) diasAno;
    }

    public static double calcularRescate(
            final int tipoInteres,
            final double fraccionAno,
            final double captacion,
            final double tasa) {
        switch (tipoInteres) {
        case TipoInteres.SIMPLE:
            return calcularRescateInteresSimple(fraccionAno, captacion, tasa);
        case TipoInteres.COMPUESTO:
            return calcularRescateInteresCompuesto(fraccionAno, captacion, tasa);
        default:
            throw new ValorizadorException("Tipo de interes no implementado: " + tipoInteres);
        }
    }

    public static double calcularRescate(
            final int tipoInteres,
            final int diasAno,
            final Fecha fechaCaptacion,
            final Fecha fechaRescate,
            final double captacion,
            final double tasa) {
        final double fraccionAno = calcularFraccionAno(diasAno, fechaCaptacion, fechaRescate);
        return calcularRescate(tipoInteres, fraccionAno, captacion, tasa);
    }

    public static double calcularRescate(
            final int tipoInteres,
            final int diasAno,
            final int plazoEnDias,
            final double captacion,
            final double tasa) {
        final double fraccionAno = calcularFraccionAno(diasAno, plazoEnDias);
        return calcularRescate(tipoInteres, fraccionAno, captacion, tasa);
    }

    private static double calcularRescateInteresCompuesto(
            final double fraccionAno,
            final double captacion,
            final double tasa) {
        return captacion * Math.pow(1 + tasa, fraccionAno);
    }

    private static double calcularRescateInteresSimple(
            final double fraccionAno,
            final double captacion,
            final double tasa) {
        return captacion * (1 + tasa * fraccionAno);
    }

    public static double calcularTasa(
            final int tipoInteres,
            final double fraccionAno,
            final double captacion,
            final double rescate) {
        switch (tipoInteres) {
        case TipoInteres.SIMPLE:
            return calcularTasaInteresSimple(fraccionAno, captacion, rescate);
        case TipoInteres.COMPUESTO:
            return calcularTasaInteresCompuesto(fraccionAno, captacion, rescate);
        default:
            throw new ValorizadorException("Tipo de interes no implementado: " + tipoInteres);
        }
    }

    public static double calcularTasa(
            final int tipoInteres,
            final int diasAno,
            final Fecha fechaCaptacion,
            final Fecha fechaRescate,
            final double captacion,
            final double rescate) {
        final double fraccionAno = calcularFraccionAno(diasAno, fechaCaptacion, fechaRescate);
        return calcularTasa(tipoInteres, fraccionAno, captacion, rescate);
    }

    public static double calcularTasa(
            final int tipoInteres,
            final int diasAno,
            final int plazoEnDias,
            final double captacion,
            final double rescate) {
        final double fraccionAno = calcularFraccionAno(diasAno, plazoEnDias);
        return calcularTasa(tipoInteres, fraccionAno, captacion, rescate);
    }

    private static double calcularTasaInteresCompuesto(
            final double fraccionAno,
            final double captacion,
            final double rescate) {
        return Math.pow(rescate / captacion, 1 / fraccionAno) - 1;
    }

    private static double calcularTasaInteresSimple(
            final double fraccionAno,
            final double captacion,
            final double rescate) {
        return (rescate / captacion - 1) / fraccionAno;
    }

    private CalculadoraPagares() {
    }

}
