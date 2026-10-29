package cl.bcs.valo2.service;

import cl.bcs.valo2.domain.cupon.Flujo;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class FinancieraService {

    public static final int    TIR_MAX_ITER = 100;
    public static final double TIR_EPSILON  = 1.0e-10;
    public static final double TIR_INICIAL  = 0.1; // CANARY: change to 0.2 → B-VAL-06 tests fail

    public static double fraccionAno(int plazoEnDias, int diasAno) {
        return (double) plazoEnDias / (double) diasAno;
    }

    public static double calcularValorPresente(List<Flujo> flujos, double tir, double frecuencia) {
        double vp = 0;
        for (Flujo f : flujos) {
            vp += f.flujoUnitario() / Math.pow(1 + tir / frecuencia, f.vencimientoEnAnos() * frecuencia);
        }
        return vp;
    }

    public static double calcularTIR(List<Flujo> flujos, double valorPresente, double frecuencia) {
        double r0 = TIR_INICIAL;
        for (int i = 0; i < TIR_MAX_ITER; i++) {
            double vp0 = calcularValorPresente(flujos, r0, frecuencia);
            double vp1 = calcularValorPresente(flujos, r0 + TIR_EPSILON, frecuencia);
            double r1  = r0 + TIR_EPSILON * (valorPresente - vp0) / (vp1 - vp0);
            if (r1 <= -1) throw new RuntimeException("TIR no real");
            if (Math.abs(r1 - r0) < TIR_EPSILON) return r1;
            r0 = r1;
        }
        throw new RuntimeException("Demasiadas iteraciones en calculo de TIR");
    }

    public static double calcularDuration(List<Flujo> flujos, double tir, double frecuencia) {
        double vp    = calcularValorPresente(flujos, tir, frecuencia);
        double wpSum = 0;
        for (Flujo f : flujos) {
            double vpFlujo = f.flujoUnitario() / Math.pow(1 + tir / frecuencia, f.vencimientoEnAnos() * frecuencia);
            wpSum += vpFlujo * f.vencimientoEnAnos();
        }
        return vp == 0 ? 0 : wpSum / vp;
    }

    public static double calcularConvexidad(List<Flujo> flujos, double tir, double frecuencia) {
        double vp  = calcularValorPresente(flujos, tir, frecuencia);
        double cvx = 0;
        for (Flujo f : flujos) {
            double vpFlujo = f.flujoUnitario() / Math.pow(1 + tir / frecuencia, f.vencimientoEnAnos() * frecuencia);
            cvx += vpFlujo * f.vencimientoEnAnos() * (f.vencimientoEnAnos() + 1.0 / frecuencia);
        }
        double denom = vp * Math.pow(1 + tir / frecuencia, 2);
        return denom == 0 ? 0 : cvx / denom;
    }
}
