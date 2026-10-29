package cl.bcs.valo2.calculadoras;

import java.util.List;

import cl.bcs.valo2.TipoInteres;
import cl.bcs.valo2.ValorizadorException;
import cl.bcs.valo2.ValorizadorBonosParams;
import cl.bcs.valo2.entidades.Bono;
import cl.bcs.valo2.entidades.Cupon;

abstract class CalculadoraUnitaria {
    private static final int MAX_ITERACIONES = 100;
    private static final double TIR_EPSILON = 1.0e-10;
    private static final double TIR_INICIAL = 0.1;

    public static double calcularConvexidad(
            final List<Cupon> cupones,
            final double tir,
            final double frecuencia) {
        final double a1 = 1 + tir / frecuencia;
        double s1 = 0;
        double s3 = 0;
        for (int i = 0; i < cupones.size(); i++) {
            final double a2 = cupones.get(i).getVencimientoEnAnos();
            final double a3 = cupones.get(i).calcularFlujoUnitario()
                    / Math.pow(a1, a2 * frecuencia);
            s1 += a3;
            s3 += a2 * (1 + a2) * a3;
        }

        return s3 / (s1 * a1 * a1);
    }

    public static double calcularDuration(
            final List<Cupon> cupones,
            final double tir,
            final double frecuencia) {
        final double a1 = 1 + tir / frecuencia;
        double s1 = 0;
        double s2 = 0;
        for (int i = 0; i < cupones.size(); i++) {
            final double a2 = cupones.get(i).getVencimientoEnAnos();
            final double a3 = cupones.get(i).calcularFlujoUnitario()
                    / Math.pow(a1, a2 * frecuencia);
            s1 += a3;
            s2 += a2 * a3;
        }

        return s2 / s1;
    }

    public static double calcularTIR(
            final List<Cupon> cupones,
            final double valorPresente,
            final double frecuencia) {
        final double epsilon = TIR_EPSILON;
        double r0 = TIR_INICIAL;
        for (int i = 0; i < MAX_ITERACIONES; i++) {
            final double vp0 = calcularValorPresente(cupones, r0, frecuencia);
            final double vp1 = calcularValorPresente(cupones, r0 + epsilon, frecuencia);
            final double r1 = r0 + epsilon * (valorPresente - vp0) / (vp1 - vp0);
            if (r1 <= -1) {
                throw new ValorizadorException("TIR no real");
            }

            if (Math.abs(r1 - r0) < epsilon) {
                return r1;
            }

            r0 = r1;
        }

        throw new ValorizadorException("Demasiadas iteraciones en calculo de TIR");
    }

    public static double calcularValorPresente(
            final List<Cupon> cupones,
            final double tir,
            final double frecuencia) {
        final double a = 1 + tir / frecuencia;
        double vp = 0;
        for (int i = 0; i < cupones.size(); i++) {
            final double b = frecuencia * cupones.get(i).getVencimientoEnAnos();
            vp += cupones.get(i).calcularFlujoUnitario() / Math.pow(a, b);
        }

        return vp;
    }

    protected double calcularProporcionIntereses(
            final ValorizadorBonosParams params,
            final Bono bono) {
        final Cupon cupon = params.getCupones().get(0);
        final double intereses = cupon.calcularInteresesUnitarios();
        final double anosPeriodo = cupon.getPeriodoEnAnos();
        final double anosTranscurridos = cupon.getPeriodoEnAnos() - cupon.getVencimientoEnAnos();

        double proporcionIntereses;
        switch (bono.getTipoCalculoInteresesDevengados()) {
        case TipoInteres.SIMPLE:
            proporcionIntereses = intereses * anosTranscurridos / anosPeriodo;
            break;
        case TipoInteres.COMPUESTO:
            proporcionIntereses = Math.pow(1 + intereses, anosTranscurridos) - 1;
            break;
        default:
            throw new ValorizadorException(
                    "Tipo de calculo de intereses devengados no implementado", bono);
        }

        return proporcionIntereses;
    }

    protected double calcularProporcionTotal(
            final ValorizadorBonosParams params,
            final Bono bono) {
        return params.getProporcionCapital() + params.getProporcionReajuste()
                + params.getProporcionIntereses();
    }

}
