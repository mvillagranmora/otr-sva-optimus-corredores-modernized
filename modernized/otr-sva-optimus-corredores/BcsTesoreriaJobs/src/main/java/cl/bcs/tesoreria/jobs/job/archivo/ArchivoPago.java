package cl.bcs.tesoreria.jobs.job.archivo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.time.Clock;
import java.time.format.DateTimeFormatter;

/**
 * The bank payment file being written. Legacy: {@code cl.bcs.tesoreria.generarArchivoPago.Archivo} (171 lines). Almost every failure is
 * swallowed, as in the legacy (TES-J-DEF-09 and 10): {@link #crear()} creates only ONE directory level and ignores an
 * {@link IOException}; a null line-break type or a file that was never opened makes {@code escribir} write nothing without an error.
 * CSV is ';' separated with quotes only where needed (opencsv 3.7 rules, {@link CsvLegacyWriter}). The file is not transactional
 * (TES-J-DEF-08).
 */
public final class ArchivoPago {

    private final String nombreArchivo;
    private final String extension;
    private final String ruta;
    private FileOutputStream salida;
    private OutputStreamWriter fw;
    private CsvLegacyWriter cw;

    /** :39-44; the name is {@code <NombreArchivoSalida>_<yyyyMMdd_HHmmss>.<extension>} with the JVM-clock time (:51-72). */
    public ArchivoPago(FormatoPago formatoArchivo, Clock clock) {
        this.extension = formatoArchivo.getFormatoSalida();
        String ahora = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").withZone(clock.getZone()).format(clock.instant());
        this.nombreArchivo = String.valueOf(formatoArchivo.getNombreArchivoSalida()) + "_" + ahora + "." + this.extension;
        this.ruta = formatoArchivo.getRutaCompleta();
    }

    /** The legacy JavaCall input {@code p_Archivo}: the three fields Gson set by reflection. */
    public ArchivoPago(String nombreArchivo, String extension, String ruta) {
        this.nombreArchivo = nombreArchivo;
        this.extension = extension;
        this.ruta = ruta;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public String getRuta() {
        return ruta;
    }

    public String getExtension() {
        return extension;
    }

    /** :80-105 */
    public void crear() {
        try {
            String rutaCompleta = this.ruta + this.nombreArchivo;
            RutaSegura.bajoCarpeta(this.ruta, rutaCompleta); // review M1
            File archivo = new File(rutaCompleta);
            if (!new File(this.ruta).exists()) {
                new File(this.ruta).mkdir(); // TES-J-DEF-09: one level only, the result is ignored
            }
            this.salida = new FileOutputStream(archivo);
            this.fw = new OutputStreamWriter(salida, Charset.defaultCharset());
            if (this.extension.compareTo("CSV") == 0) {
                this.cw = new CsvLegacyWriter(this.fw, ';');
            }
        } catch (IOException ex) {
            // legacy: printStackTrace; the file stays unopened
        }
    }

    /** :110-126 */
    public void cerrar() {
        try {
            this.fw.flush();
            this.fw.close();
            if (this.extension.compareTo("CSV") == 0) {
                this.cw.close();
            }
        } catch (Exception ex) {
            // legacy: swallowed
        }
    }

    /**
     * A failed run: the legacy left the writer open with its buffer unflushed (partial or empty file, TES-J-DEF-08); the file
     * descriptor is released here without flushing, so the file on disk is the same.
     */
    public void abandonar() {
        try {
            if (salida != null) {
                salida.close();
            }
        } catch (IOException ignorada) {
            // the legacy did not close it at all
        }
    }

    /** :134-153 */
    public void escribir(String linea) {
        escribir(linea, "L");
    }

    public void escribir(String linea, String modo) {
        try {
            String separadorLinea = modo.equals("W") ? "\r\n" : "\n";
            if (modo.equals("W")) {
                linea = linea.replace("\n", "\r\n");
            }
            this.fw.write(linea);
            this.fw.write(separadorLinea);
        } catch (Exception ex) {
            // legacy: swallowed (null modo, file not open)
        }
    }

    /** :160-170 */
    public void escribir(String[] linea) {
        try {
            this.cw.escribirLinea(linea, false);
        } catch (Exception ex) {
            // legacy: swallowed
        }
    }
}
