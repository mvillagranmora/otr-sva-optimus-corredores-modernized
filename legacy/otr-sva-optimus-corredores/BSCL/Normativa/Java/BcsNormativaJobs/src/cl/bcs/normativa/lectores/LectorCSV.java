package cl.bcs.normativa.lectores;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.opencsv.CSVReader;

import cl.bcs.normativa.carga.ContextoCarga;
import cl.bcs.normativa.data.DataParser;
import cl.bcs.normativa.data.DataParserException;
import cl.bcs.normativa.data.Registro;
import cl.bcs.normativa.data.RegistroMapper;
import cl.bcs.normativa.esquema.Esquema;
import cl.bcs.normativa.procesos.ProcesoException;
import cl.bcs.utils.ZipUtil;

/**
 * Implementa el lector de datos de archivo CSV, para la carga de datos de un
 * documento.
 *
 * @author fpino@email.com
 *
 */
public final class LectorCSV extends Lector {

    /**
     * Obtiene instancia del lector CSV.
     *
     * @param streamReader
     *            El lector.
     * @param separadorListas
     *            El separador de listas.
     * @return Instancia del lector CSV
     */
    private static CSVReader newCSVReader(final InputStreamReader streamReader,
            final String separadorListas) {
        if (separadorListas == null || separadorListas.isEmpty()) {
            return new CSVReader(streamReader);
        } else {
            return new CSVReader(streamReader, separadorListas.charAt(0));
        }
    }

    /**
     * Parser de los datos.
     */
    private final transient DataParser parser;

    /**
     * Lector CSV.
     */
    private final transient CSVReader reader;

    /**
     * Constructor.
     *
     * @param contexto
     *            El contexto de carga.
     * @throws DataParserException
     *             En caso de error en el parser de datos.
     * @throws IOException
     *             En caso de error de I/O.
     */
    public LectorCSV(final ContextoCarga contexto) throws DataParserException, IOException {
        super(contexto);

        if (ZipUtil.isRar(contexto.getRutaArchivo())) {
            throw new ProcesoException("Tipo de archivo no soportado: RAR");
        }

        final InputStream inputStream = newInputStream(contexto.getRutaArchivo());
        final InputStreamReader streamReader = new InputStreamReader(inputStream);
        reader = newCSVReader(streamReader, contexto.getOpciones().getSeparadorListas());
        parser = new DataParser(contexto.getOpciones().getSimboloDecimal());
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

    /**
     * Obtiene {@link InputStream} dada la ruta del archivo.
     * <p>
     * Detecta previamente si el archivo est&aacute; comprimido para entregar la
     * referencia a un archivo dentro del comprimido.
     *
     * @param rutaArchivo
     *            La ruta del archivo.
     * @return {@link InputStream} dada la ruta del archivo.
     * @throws FileNotFoundException
     *             En caso de que el archivo no exista.
     */
    private InputStream newInputStream(final String rutaArchivo) throws FileNotFoundException {
        try {
            final ZipFile zipFile = new ZipFile(rutaArchivo);
            final ZipEntry entry = ZipUtil.getFirstFileEntry(zipFile);
            if (entry == null) {
                throw new ProcesoException("ZIP no contiene archivo");
            }

            return zipFile.getInputStream(entry);
        } catch (FileNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            return new FileInputStream(rutaArchivo);
        }
    }

    @Override
    public Registro siguiente() throws IOException {
        String[] values;

        values = reader.readNext();

        if (values == null) {
            return null;
        }

        contarRegistrosLeidos();

        final Esquema esquema = getContexto().getEsquema();

        Registro registro;
        try {
            registro = RegistroMapper.map(esquema, values, parser);
            registro.setValido(true);
        } catch (DataParserException ex) {
            registro = new Registro(esquema);
            registro.setValido(false);
            registro.setRazonInvalidez(ex.getCauseMessage());
        }

        return registro;
    }

}
