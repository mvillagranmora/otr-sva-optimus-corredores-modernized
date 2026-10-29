package cl.bcs.cuentainversion.jobs.job.archivo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Physical TXT / CSV file of a generated file job (legacy {@code generarArchivo/Archivo.java}). Bug for bug (CIJ-DEF-09): {@link #crear()}
 * creates ONE directory level and swallows the failure, so the program may name a file that does not exist; every write / close failure is
 * swallowed; the TXT line end is the platform line separator ({@code System.lineSeparator()}, LF in production). The file name is
 * {@code <NombreArchivoSalida>_<yyyyMMdd_HHmmss>.<FormatoSalida>} with the time of the {@link Clock} in the JVM zone (legacy {@code new Date()}).
 */
public final class Archivo {

    private static final Logger log = LoggerFactory.getLogger(Archivo.class);

    private final String nombreArchivo;
    private final String extension;
    private final String ruta;
    private FileOutputStream fos;
    private Writer fw;
    private CsvLegacyWriter cw;

    private final Long idPrograma;

    public Archivo(FormatoArchivo formatoArchivo, Clock reloj) {
        this(formatoArchivo, reloj, null);
    }

    /**
     * Review M-2: the name must not carry separators or '..' and the resulting path must stay under the configured folder; otherwise an
     * {@link IllegalArgumentException} (a job error, raised before anything is touched on disk). {@code idPrograma} is only for the logs.
     */
    public Archivo(FormatoArchivo formatoArchivo, Clock reloj, Long idPrograma) {
        this.extension = formatoArchivo.getFormatoSalida();
        this.nombreArchivo = generarNombreArchivo(formatoArchivo, reloj);
        this.ruta = formatoArchivo.getRutaCompleta();
        this.idPrograma = idPrograma;
        RutaSegura.nombre(this.nombreArchivo);
        RutaSegura.bajoCarpeta(this.ruta, this.ruta + this.nombreArchivo);
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    /** Legacy :51-72. */
    private String generarNombreArchivo(FormatoArchivo formatoArchivo, Clock reloj) {
        String nombre = "";
        try {
            Date fechaActual = Date.from(reloj.instant());
            String nombreArchivoSalida = String.valueOf(formatoArchivo.getNombreArchivoSalida());
            String fechaHora = new SimpleDateFormat("yyyyMMdd_HHmmss").format(fechaActual);
            nombre = nombreArchivoSalida + "_" + fechaHora + "." + this.extension;
        } catch (Exception ex) {
            log.error("Archivo.generarNombreArchivo: {}", ex.toString(), ex);
        }
        return nombre;
    }

    /** Legacy :80-105: a null path is a NullPointerException for the caller (it is inside the job's swallowing try). */
    public void crear() {
        try {
            String rutaCompleta = this.ruta + this.nombreArchivo;
            File archivo = new File(rutaCompleta);
            if (!new File(this.ruta).exists()) {
                new File(this.ruta).mkdir();
            }
            this.fos = new FileOutputStream(archivo); // what new FileWriter(archivo) does: default charset, truncate
            this.fw = new OutputStreamWriter(this.fos);
            if (this.extension.compareTo("CSV") == 0) {
                this.cw = new CsvLegacyWriter(this.fw, ';');
            }
        } catch (IOException ex) {
            // swallowed on purpose (CIJ-DEF-09: the program may name a file that does not exist); review H-2: at least say so at ERROR
            log.error("Archivo.crear failed (IdPrograma={}, ruta={}): {}", this.idPrograma, this.ruta, ex.toString(), ex);
        }
    }

    /**
     * Legacy :110-126. Review H-2: the descriptor is closed even when the flush fails (the failure is still swallowed and logged, as
     * in the legacy); the order flush, close, CSV close is unchanged.
     */
    public void cerrar() {
        try {
            try {
                this.fw.flush();
                this.fw.close();
            } finally {
                cerrarSinFallar(this.fw);
            }
            if (this.extension.compareTo("CSV") == 0) {
                this.cw.close();
            }
        } catch (Exception ex) {
            log.error("Archivo.cerrar: {}", ex.toString(), ex);
        }
    }

    /**
     * Review H-2: release the descriptor of a file that is not going to be closed normally (a write failed midway). Never throws; the
     * file keeps whatever was written (CIJ-DEF-09: partial file, no rollback).
     */
    public void liberar() {
        cerrarSinFallar(this.fos); // the stream, NOT the writer: closing the writer would flush the buffered text and change the pinned partial file (GA-28)
    }

    private static void cerrarSinFallar(java.io.Closeable escritor) {
        if (escritor == null) {
            return;
        }
        try {
            escritor.close();
        } catch (IOException | RuntimeException ex) {
            log.error("Archivo.liberar: {}", ex.toString());
        }
    }

    /** Legacy :133-144 (TXT). */
    public void escribir(String linea) {
        try {
            this.fw.write(linea);
            this.fw.write(System.lineSeparator());
        } catch (Exception ex) {
            log.error("Archivo.escribir: {}", ex.toString(), ex);
        }
    }

    /** Legacy :151-161 (CSV): {@code writeNext(linea, false)}, only the special fields are quoted. */
    public void escribir(String[] linea) {
        try {
            this.cw.escribirLinea(linea, false);
        } catch (Exception ex) {
            log.error("Archivo.escribir: {}", ex.toString(), ex);
        }
    }
}
