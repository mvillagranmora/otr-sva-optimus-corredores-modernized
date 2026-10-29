package cl.bcs.valo2.service;

import org.springframework.stereotype.Service;

@Service
public class PagaresService {

    public static final int SIMPLE    = 0;
    public static final int COMPUESTO = 1;

    public static double captacionSimple(double fraccionAno, double rescate, double tasa) {
        return rescate / (1 + tasa * fraccionAno);
    }

    public static double captacionCompuesto(double fraccionAno, double rescate, double tasa) {
        return rescate / Math.pow(1 + tasa, fraccionAno);
    }

    public static double captacion(int tipoInteres, double fraccionAno, double rescate, double tasa) {
        return switch (tipoInteres) {
            case SIMPLE    -> captacionSimple(fraccionAno, rescate, tasa);
            case COMPUESTO -> captacionCompuesto(fraccionAno, rescate, tasa);
            default        -> throw new IllegalArgumentException("Tipo de interes no implementado: " + tipoInteres);
        };
    }

    public static double rescateSimple(double fraccionAno, double captacion, double tasa) {
        return captacion * (1 + tasa * fraccionAno);
    }

    public static double rescateCompuesto(double fraccionAno, double captacion, double tasa) {
        return captacion * Math.pow(1 + tasa, fraccionAno);
    }

    public static double rescate(int tipoInteres, double fraccionAno, double captacion, double tasa) {
        return switch (tipoInteres) {
            case SIMPLE    -> rescateSimple(fraccionAno, captacion, tasa);
            case COMPUESTO -> rescateCompuesto(fraccionAno, captacion, tasa);
            default        -> throw new IllegalArgumentException("Tipo de interes no implementado: " + tipoInteres);
        };
    }

    public static double interesDevengadoSimple(double fraccionAno, double capitalInicial, double tasa) {
        return capitalInicial * tasa * fraccionAno;
    }

    public static double interesDevengadoCompuesto(double fraccionAno, double capitalInicial, double tasa) {
        return capitalInicial * (Math.pow(1 + tasa, fraccionAno) - 1);
    }
}
