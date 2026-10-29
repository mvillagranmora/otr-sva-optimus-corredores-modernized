package cl.bcs.tesoreria.jobs.job.archivo;

import cl.bcs.tesoreria.javacall.ConsultarFormatoArchivoService.SalidaFormatoArchivo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.time.Clock;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * The PAC charge file being written. Legacy: {@code cl.bcs.tesoreria.generarArchivoCargos.Archivo} (109 lines). TXT lines end with
 * CRLF; CSV is ';' separated with quotes only where needed (opencsv 3.7 rules, {@link CsvLegacyWriter}). Like the legacy it is NOT
 * transactional: a failed run leaves a partial or empty file (TES-J-DEF-08), because the buffered writer is never flushed then.
 */
public final class ArchivoCargos {

    private final String filename;
    private final String extension;
    private final String fullpath;
    private final String fieldSeparator;
    private final List<String> lines = new ArrayList<>();
    private long details = 0L;
    private FileOutputStream salida;
    private OutputStreamWriter fileWriter;
    private CsvLegacyWriter csvWriter;

    /** :46-54; the name carries the JVM-clock time {@code yyyyMMdd_HHmmss}. */
    public ArchivoCargos(SalidaFormatoArchivo formatoArchivo, Clock clock) {
        this.extension = formatoArchivo.FormatoSalida;
        String ahora = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").withZone(clock.getZone()).format(clock.instant());
        this.filename = formatoArchivo.NombreArchivoSalida + "_" + ahora + "." + this.extension;
        this.fullpath = formatoArchivo.RutaCompleta + this.filename;
        this.fieldSeparator = formatoArchivo.SeparadorCampos;
    }

    public List<String> getLines() {
        return lines;
    }

    public Long getDetails() {
        return details;
    }

    public String getFilename() {
        return filename;
    }

    public String getFullpath() {
        return fullpath;
    }

    /** :56-60 */
    public void open() throws IOException {
        RutaSegura.bajoCarpeta(this.fullpath.substring(0, this.fullpath.length() - this.filename.length()),
                this.fullpath); // review M1
        File file = new File(this.fullpath);
        this.salida = new FileOutputStream(file);
        this.fileWriter = new OutputStreamWriter(salida, Charset.defaultCharset());
        if (this.extension.equals("CSV")) {
            this.csvWriter = new CsvLegacyWriter(this.fileWriter, ';');
        }
    }

    /** :62-67 */
    public void close() throws IOException {
        this.fileWriter.flush();
        this.fileWriter.close();
        if (this.extension.equals("CSV")) {
            this.csvWriter.close();
        }
    }

    /**
     * A failed run: the legacy left the writer open and its buffer unflushed (partial or empty file, TES-J-DEF-08); the file
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

    /** :69-77 */
    public void addLine(List<String> valuesFormatted) throws IOException {
        if (this.extension.equals("TXT")) {
            this.txt(valuesFormatted);
        } else if (this.extension.equals("CSV")) {
            this.csv(valuesFormatted);
        } else {
            throw new IOException("FORMATO DE SALIDA NO CONFIGURADO/COMPATIBLE.");
        }
        this.details++;
    }

    private void txt(List<String> valuesFormatted) throws IOException {
        String line = "";
        for (String valueFormatted : valuesFormatted) {
            line = line + valueFormatted + this.fieldSeparator;
        }
        this.lines.add(line);
        this.fileWriter.write(line);
        this.fileWriter.write("\r\n");
    }

    private void csv(List<String> valuesFormatted) {
        int length = valuesFormatted.size() + 1;
        String[] campos = new String[length];
        String line = "";
        for (int i = 0; i < length - 1; i++) {
            campos[i] = valuesFormatted.get(i);
        }
        this.csvWriter.escribirLinea(campos, false);
        for (String valueFormatted : valuesFormatted) {
            line = line + valueFormatted + this.fieldSeparator;
        }
        this.lines.add(line);
    }
}
