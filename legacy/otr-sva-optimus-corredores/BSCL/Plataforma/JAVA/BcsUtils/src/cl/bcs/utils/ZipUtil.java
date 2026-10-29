package cl.bcs.utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FilenameUtils;

/**
 * M&eacute;todos utiles para el manejo de archivos comprimidos.
 *
 * @author fpino@email.com
 *
 */
public final class ZipUtil {

    /**
     * Genera archivo ZIP a partir de otro archivo.
     *
     * @param inputPath
     *            Ruta del archivo a comprimir.
     * @param outputPath
     *            Ruta del archivo comprimido.
     * @throws IOException
     *             En caso de error de I/O.
     */
    public static void generateZip(final String inputPath, final String outputPath)
            throws IOException {

        if (inputPath.equalsIgnoreCase(outputPath)) {
            throw new BadFilePathException(
                    "La ruta del archivo de salida no puede ser igual a la ruta del archivo de entrada");
        }

        try (final InputStream is = new FileInputStream(inputPath)) {
            try (final OutputStream os = new FileOutputStream(outputPath);
                    final ZipOutputStream zos = new ZipOutputStream(os)) {
                final String filename = FilenameUtils.getName(inputPath);
                final ZipEntry ze = new ZipEntry(filename);
                zos.putNextEntry(ze);

                int len;
                final byte[] buffer = new byte[1024];
                while ((len = is.read(buffer)) > 0) {
                    zos.write(buffer, 0, len);
                }

                zos.closeEntry();
            }
        }
    }
    
    /**
     * Genera un archivo ZIP a partir de otros archivos.
     * @param inputPaths 
     * 			Ruta de los archivos a comprimir.
     * @param outputPath
     *            Ruta del archivo comprimido.
     * @throws IOException
     *             En caso de error de I/O.
     */
    public static boolean generateMultipleZip(final List<String> inputPaths, final String outputPath)
            throws IOException {

        try (final OutputStream os = new FileOutputStream(outputPath);
                final ZipOutputStream zos = new ZipOutputStream(os)) {
        	
        	for(String inputPath : inputPaths) {
        		try (final InputStream is = new FileInputStream(inputPath)) {
        			
        			if (inputPath.equalsIgnoreCase(outputPath)) {
        	            throw new BadFilePathException(
        	                    "La ruta del archivo de salida no puede ser igual a la ruta del archivo de entrada");
        	        }
        			
            		final String filename = FilenameUtils.getName(inputPath);
                    final ZipEntry ze = new ZipEntry(filename);
                	zos.putNextEntry(ze);

                	int len;
                	final byte[] buffer = new byte[1024];
                    while ((len = is.read(buffer)) > 0) {
                        zos.write(buffer, 0, len);
                    }
                    zos.closeEntry();
                }
        	}
        	        	
        }catch(Exception e) {
        	e.printStackTrace();
        	return false;
        }
        return true;
    }

    /**
     * Obtiene la primera entrada correspondiente a un archivo en la ra&iacute;z
     * del archivo zip.
     * <p>
     * El prop&oacute;sito es procesar solo un archivo comprimido ignorando el
     * resto del contenido del archivo zip.
     *
     * @param zipFile
     *            Archivo zip.
     * @return La primera entrada correspondiente a un archivo en la ra&iacute;z
     *         del archivo zip o, <code>null</code> en el caso de que no haya un
     *         archivo en la ra&iacute;z del zip.
     */
    public static ZipEntry getFirstFileEntry(final ZipFile zipFile) {
        while (zipFile.entries().hasMoreElements()) {
            final ZipEntry entry = zipFile.entries().nextElement();
            if (!entry.isDirectory()) {
                return entry;
            }
        }

        return null;
    }

    /**
     * Chequea si el archivo en la ruta indicada es un RAR.
     * <p>
     * Se hace un chequeo basico en base a los primeros caracteres del archivo,
     * que deben corresponder a "Rar!". La idea es hacer un
     * discriminaci&oacute;n r&aacute;pida, para chequeos m&aacute;s completos
     * se debe usar alguna otra API.
     *
     * @param filepath
     *            Ruta del archivo a chequear.
     * @return <code>true</code> en caso de que el chequeo sea positivo,
     *         <code>false</code> en caso contrario.
     * @throws IOException
     *             En caso de error de I/O.
     */
    public static boolean isRar(final String filepath) throws IOException {
        try (final InputStream is = new FileInputStream(filepath)) {
            try (final BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                final String line = br.readLine();
                if (line != null && line.startsWith("Rar!")) {
                    return true;
                }
            }
        }

        return false;
    }

    private ZipUtil() {
    }

}
