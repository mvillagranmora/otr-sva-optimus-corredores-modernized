package cl.bcs.contabilidad.infrastructure.interfaz;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Inputs shared by every interface generator in one run (CONT-A-01..05).
 *
 * <p>Legacy: {@code CreaInterfazDiaria.logica()} passed {@code _fechaEjecucion}, {@code contador},
 * {@code rutaDescargaBroad}, {@code rutaDescargaBPC} and its {@code Log} to each {@code Crea*} class.
 *
 * @param fechaEjecucion local date of the run minus one calendar day
 * @param contador       first {@code Cont} of ConsultaContadorInterfazSIGA.sql, 0 without rows
 * @param directorioFi   {@code <PLA.RutaBaseArchivos>Transmisiones/Salida/FI/}
 * @param directorioBpc  {@code <PLA.RutaBaseArchivos>Transmisiones/Salida/BPC/}
 * @param bitacora       the run log the generators write their legacy lines to
 */
public record InterfazContext(LocalDate fechaEjecucion, long contador, Path directorioFi, Path directorioBpc,
                              InterfazBitacora bitacora) {

    public InterfazContext {
        Objects.requireNonNull(fechaEjecucion, "fechaEjecucion");
        Objects.requireNonNull(directorioFi, "directorioFi");
        Objects.requireNonNull(directorioBpc, "directorioBpc");
        Objects.requireNonNull(bitacora, "bitacora");
    }

    /** A run without a run log ({@link InterfazBitacora#NINGUNA}). */
    public InterfazContext(LocalDate fechaEjecucion, long contador, Path directorioFi, Path directorioBpc) {
        this(fechaEjecucion, contador, directorioFi, directorioBpc, InterfazBitacora.NINGUNA);
    }
}
