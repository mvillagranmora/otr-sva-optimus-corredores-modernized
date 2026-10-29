package cl.bcs.normativa.lectores;

import java.io.IOException;
import java.sql.SQLException;

import cl.bcs.normativa.data.Registro;
import cl.bcs.normativa.procesos.ContextoProceso;

/**
 * Define forma de un lector de datos.
 *
 * @author fpino@email.com
 *
 */
public abstract class Lector implements AutoCloseable {

    /**
     * Cantidad de registros leidos.
     */
    private long cantidadRegistrosLeidos;

    /**
     * Contexto de proceso.
     */
    private final ContextoProceso contexto;

    /**
     * Constructor.
     *
     * @param contexto
     *            El contexto de proceso.
     */
    public Lector(final ContextoProceso contexto) {
        this.contexto = contexto;
        this.cantidadRegistrosLeidos = 0;
    }

    @Override
    public abstract void close() throws IOException, SQLException;

    /**
     * Incrementa el contador de registros leidos.
     */
    public void contarRegistrosLeidos() {
        cantidadRegistrosLeidos++;
    }

    /**
     * Obtiene la cantidad de registros leidos.
     *
     * @return La cantidad de registros leidos.
     */
    public long getCantidadRegistrosLeidos() {
        return cantidadRegistrosLeidos;
    }

    /**
     * Obtiene el contexto.
     *
     * @return El contexto.
     */
    public ContextoProceso getContexto() {
        return contexto;
    }

    /**
     * Lee el siguiente registro.
     *
     * @return El siguiente registro.
     * @throws IOException
     *             En caso de error de I/O.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public abstract Registro siguiente() throws IOException, SQLException;
}
