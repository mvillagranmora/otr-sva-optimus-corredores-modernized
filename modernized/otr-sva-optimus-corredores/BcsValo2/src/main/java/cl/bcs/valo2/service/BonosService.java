package cl.bcs.valo2.service;

import cl.bcs.valo2.domain.cupon.Flujo;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BonosService {

    // --- B-VAL-18: interés devengado bono simple ---
    // proporcionIntereses = intereses * anosTranscurridos / anosPeriodo
    public static double interesDevengadoSimple(double intereses, double anosPeriodo, double anosVencimiento) {
        double anosTranscurridos = anosPeriodo - anosVencimiento;
        return intereses * anosTranscurridos / anosPeriodo;
    }

    // --- B-VAL-19: interés devengado bono compuesto ---
    // proporcionIntereses = (1 + intereses)^anosTranscurridos - 1
    public static double interesDevengadoCompuesto(double intereses, double anosPeriodo, double anosVencimiento) {
        double anosTranscurridos = anosPeriodo - anosVencimiento;
        return Math.pow(1 + intereses, anosTranscurridos) - 1;
    }

    // --- B-VAL-20: precio bono ---
    // precio = 100 * proporcionCapital / saldoCapitalUnitario
    public static double precioBono(double proporcionCapital, double saldoCapitalUnitario) {
        return 100 * proporcionCapital / saldoCapitalUnitario;
    }

    // Overload for full discounted-cashflow price (used by controller)
    public static double precioBono(List<Flujo> flujos, double tir, double frecuencia) {
        return FinancieraService.calcularValorPresente(flujos, tir, frecuencia);
    }
}
