package cl.bcs.normativa.jobs.domain;

import java.util.Objects;

/**
 * Período comercial used in DJ1 file generation.
 *
 * B-NJB-10: DJ1 filenames include PeriodoComercial (YYYY-MM or similar).
 * Periodicidad M = Mensual, T = Trimestral, A = Anual.
 */
public record PeriodoComercial(String valor, String periodicidad, String rectifica) {

    public PeriodoComercial {
        Objects.requireNonNull(valor, "valor must not be null");
        Objects.requireNonNull(periodicidad, "periodicidad must not be null");
        Objects.requireNonNull(rectifica, "rectifica must not be null");
    }

    /**
     * Returns the effective periodicidad for the file name.
     * B-NJB-11: when rectifica=S and periodicidad=M, effective periodicidad is "C".
     */
    public static String periodicidadEfectiva(String periodicidad, String rectifica) {
        Objects.requireNonNull(periodicidad, "periodicidad must not be null");
        Objects.requireNonNull(rectifica, "rectifica must not be null");
        if ("S".equals(rectifica) && "M".equals(periodicidad)) {
            return "C";
        }
        return periodicidad;
    }

    /**
     * Builds the DJ1 filename for a given tipo archivo (FX, IR, FI).
     *
     * B-NJB-12: Pattern:
     *   &lt;base&gt;.replace("RutEntidadLegal", rut.replace("-",""))
     *         .replace("Periodicidad", periodicidadEfectiva)
     *         .replace("PeriodoComercial", fechaPeriodo.replace("-",""))
     *         .replace("TipoArchivo", tipoArchivo)
     */
    public static String construirNombreArchivoDJ1(
            String plantilla,
            String rutDeclarante,
            String periodicidad,
            String rectifica,
            String fechaPeriodo,
            String tipoArchivo) {
        Objects.requireNonNull(plantilla, "plantilla must not be null");
        Objects.requireNonNull(rutDeclarante, "rutDeclarante must not be null");
        Objects.requireNonNull(periodicidad, "periodicidad must not be null");
        Objects.requireNonNull(rectifica, "rectifica must not be null");
        Objects.requireNonNull(fechaPeriodo, "fechaPeriodo must not be null");
        Objects.requireNonNull(tipoArchivo, "tipoArchivo must not be null");

        String nombre = plantilla
                .replace("RutEntidadLegal", rutDeclarante.replace("-", ""))
                .replace("Periodicidad", periodicidadEfectiva(periodicidad, rectifica))
                .replace("PeriodoComercial", fechaPeriodo.replace("-", ""))
                .replace("TipoArchivo", tipoArchivo);
        return nombre;
    }
}
