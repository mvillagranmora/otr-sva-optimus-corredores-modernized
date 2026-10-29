package cl.bcs.plataforma.service.plataforma;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * Converts numeric amounts to their written Spanish representation.
 * All methods are pure static functions with no external dependencies.
 * Ported from legacy: cl.bcs.plataforma.pla.utils.ConvertirMontoEscrito
 */
@Service
public class ConvertirMontoEscritoService {

    private static final String[] UNIDADES = {"", "Un ", "Dos ", "Tres ", "Cuatro ", "Cinco ", "Seis ", "Siete ", "Ocho ", "Nueve "};
    private static final String[] DECENAS  = {"", "Dieci", "Veinti", "Treinta ", "Cuarenta ", "Cincuenta ", "Sesenta ", "Setenta ", "Ochenta ", "Noventa "};
    private static final String[] CENTENAS = {"", "Ciento ", "Doscientos ", "Trescientos ", "Cuatrocientos ", "Quinientos ", "Seiscientos ", "Setecientos ", "Ochocientos ", "Novecientos "};

    private static final String[] ROMANOS_UNIDADES = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX"};
    private static final String[] ROMANOS_DECENAS  = {"", "X", "XX", "XXX", "XL", "L", "LX", "LXX", "LXXX", "XC"};
    private static final String[] ROMANOS_CENTENAS = {"", "C", "CC", "CCC", "CD", "D", "DC", "DCC", "DCCC", "CM"};
    private static final String[] ROMANOS_MILES    = {"", "M", "MM", "MMM"};

    /**
     * Converts a numeric string to its Spanish written representation (integer part only).
     *
     * @param s numeric string (e.g. "1234.56")
     * @return Spanish word representation of the integer part
     */
    public static String cantidadConLetra(String s) {
        if (s == null || s.isBlank()) {
            throw new IllegalArgumentException("El valor no puede ser nulo o vacio");
        }
        StringBuilder result = new StringBuilder();
        BigDecimal totalBigDecimal = new BigDecimal(s).setScale(2, BigDecimal.ROUND_DOWN);
        long parteEntera = totalBigDecimal.toBigInteger().longValue();
        int triUnidades    = (int) (parteEntera % 1000);
        int triMiles       = (int) ((parteEntera / 1000) % 1000);
        int triMillones    = (int) ((parteEntera / 1_000_000L) % 1000);
        int triMilMillones = (int) ((parteEntera / 1_000_000_000L) % 1000);

        if (parteEntera == 0) {
            result.append("Cero ");
            return result.toString();
        }

        if (triMilMillones > 0) result.append(triTexto(triMilMillones)).append("Mil ");
        if (triMillones > 0)    result.append(triTexto(triMillones));

        if (triMilMillones == 0 && triMillones == 1) result.append("Millon ");
        else if (triMilMillones > 0 || triMillones > 0) result.append("Millones ");

        if (triMiles > 0)    result.append(triTexto(triMiles)).append("Mil ");
        if (triUnidades > 0) result.append(triTexto(triUnidades));

        return result.toString();
    }

    /**
     * Converts a numeric string to Roman numeral representation.
     * Valid range: 1–3999.
     *
     * @param s numeric string
     * @return Roman numeral string
     */
    public static String cantidadNumerosRomanos(String s) {
        if (s == null || s.isBlank()) {
            throw new IllegalArgumentException("El valor no puede ser nulo o vacio");
        }
        BigDecimal totalBigDecimal = new BigDecimal(s).setScale(2, BigDecimal.ROUND_DOWN);
        long parteEntera = totalBigDecimal.toBigInteger().longValue();

        if (parteEntera < 1 || parteEntera > 3999) {
            throw new IllegalArgumentException("El numero a convertir debe estar entre 1 y 3999.");
        }

        int m  = (int) parteEntera / 1000;
        int c  = (int) (parteEntera % 1000) / 100;
        int d  = (int) (parteEntera % 100) / 10;
        int un = (int) parteEntera % 10;

        return ROMANOS_MILES[m] + ROMANOS_CENTENAS[c] + ROMANOS_DECENAS[d] + ROMANOS_UNIDADES[un];
    }

    /**
     * Generates a list of all prime numbers less than max.
     *
     * @param max upper bound (exclusive)
     * @return list of primes
     */
    public static ArrayList<Integer> numerosPrimos(int max) {
        ArrayList<Integer> primos = new ArrayList<>();
        primos.add(2);
        for (int i = 3; i < max; i++) {
            boolean esPrimo = true;
            double limite = Math.ceil(Math.sqrt(i));
            for (int j = 0; j < primos.size(); j++) {
                if (i % primos.get(j) == 0) {
                    esPrimo = false;
                    break;
                }
                if (primos.get(j) > limite) break;
            }
            if (esPrimo) primos.add(i);
        }
        return primos;
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private static String triTexto(int n) {
        StringBuilder result = new StringBuilder();
        int centenas = n / 100;
        int decenas  = (n % 100) / 10;
        int unidades = n % 10;

        if (n == 100) {
            return "Cien ";
        }
        result.append(CENTENAS[centenas]);

        if (decenas == 1 && unidades <= 5) {
            switch (unidades) {
                case 0 -> result.append("Diez ");
                case 1 -> result.append("Once ");
                case 2 -> result.append("Doce ");
                case 3 -> result.append("Trece ");
                case 4 -> result.append("Catorce ");
                case 5 -> result.append("Quince ");
            }
            return result.toString();
        } else if (decenas == 2 && unidades == 0) {
            result.append("Veinte ");
            return result.toString();
        } else {
            result.append(DECENAS[decenas]);
        }

        if (decenas > 2 && unidades > 0) {
            result.append("y ");
        }

        result.append(UNIDADES[unidades]);
        return result.toString();
    }
}
