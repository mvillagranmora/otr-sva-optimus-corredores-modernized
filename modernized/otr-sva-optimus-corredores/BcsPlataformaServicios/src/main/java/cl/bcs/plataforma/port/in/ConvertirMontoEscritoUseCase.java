package cl.bcs.plataforma.port.in;

/**
 * Use case port: convert numeric values to their written Spanish representation.
 */
public interface ConvertirMontoEscritoUseCase {

    /**
     * Convert a numeric string to Spanish words (integer part).
     * @param valor numeric string, e.g. "1234.56"
     * @return Spanish word form, e.g. "Mil Doscientos Treinta y Cuatro "
     */
    String cantidadConLetra(String valor);

    /**
     * Convert a numeric string to Roman numerals.
     * @param valor numeric string, valid range 1-3999
     * @return Roman numeral string
     */
    String cantidadNumerosRomanos(String valor);
}
