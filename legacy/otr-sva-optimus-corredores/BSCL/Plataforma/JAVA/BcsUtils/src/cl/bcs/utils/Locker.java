package cl.bcs.utils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;

import org.apache.commons.io.FilenameUtils;

/**
 * Bloqueador de archivo para controlar operaciones concurrente sobre el mismo.
 * <p>
 * Independientemente de si la operaci&oacute;n es de lectura o escritura, se
 * utiliza un archivo transitorio (con extensi&oacute;n lock), sobre el cual se
 * realiza realmente el bloqueo.
 *
 * @author fpino@email.com
 *
 */
public class Locker implements AutoCloseable {

    /**
     * El archivo transitorio que se utiliza para realizar el bloqueo.
     */
    private transient final RandomAccessFile file;

    /**
     * El bloqueador propiamente tal.
     */
    private transient final FileLock lock;

    /**
     * La ruta del archivo original.
     */
    private transient final String name;

    /**
     * Constructor que hace el bloqueo sobre el archivo de la ruta indicada.
     *
     * @param filepath
     *            Ruta del archivo.
     * @throws LockerException
     *             En caso de error.
     */
    public Locker(final String filepath) throws LockerException {
        this.name = FilenameUtils.removeExtension(filepath) + ".lock";
        try {
            this.file = new RandomAccessFile(this.name, "rw");
            this.lock = file.getChannel().lock();

            if (lock == null) {
                file.close();
                throw new LockerException("El archivo esta bloqueado por otro proceso");
            }
        } catch (IOException ex) {
            throw new LockerException("Error al tomar archivo de bloqueo", ex);
        }
    }

    /**
     * Libera el bloqueo. De paso elimina el archivo transitorio utilizado.
     *
     * @throws LockerException
     *             En caso de error.
     */
    @Override
    public void close() throws LockerException {
        try {
            lock.release();
            file.close();
            new File(name).delete();
        } catch (IOException ex) {
            throw new LockerException("Error al soltar archivo de bloqueo", ex);
        }
    }

}
