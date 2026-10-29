package cl.bcs.normativa.generacion;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.sql.Connection;
import java.util.Date;

import cl.bcs.normativa.data.Registro;

/**
 * Base de los generadores de documentos.
 *
 * @author fpino@email.com
 *
 */
public abstract class Generador implements AutoCloseable {

    /**
     * Cantidad de casos informados.
     */
    private int cantidadCasosInformados;

    /**
     * Contexto de generaci&oacute;n.
     */
    private final ContextoGeneracion contexto;

    /**
     * Resultado de la declaraci&oacute;n.
     */
    private final PrintStream out;

    /**
     * Constructor.
     *
     * @param contexto
     *            El contexto de generaci&oacute;n.
     * @throws IOException
     *             En caso de error de I/O.
     * @throws SQLException
     *             En caso de error SQL.
     * @throws SerializadorException
     *             En caso de error al serializar.
     */
    public Generador(final ContextoGeneracion contexto)
            throws IOException, SQLException, SerializadorException {
        this.contexto = contexto;
        this.out = new PrintStream(contexto.getRutaArchivoGenerado());
        this.cantidadCasosInformados = 0;
    }

    @Override
    public void close() throws IOException, SerializadorException {
        out.close();
        reemplazarRegistroTipo0();
    }
    
    public void iniciarContadorCasosInformados(int totalCasos) {
    	this.cantidadCasosInformados=totalCasos;
    };
    
    public void reiniciarContadorCasosInformados() {
    	this.cantidadCasosInformados=0;
    };
    
    /**
     * Incrementa el contador de casos informados.
     */
    public void contarCasosInformados() {
        cantidadCasosInformados++;
    }
    
    /**
     * Generador de encabezado con registro
     *
     * @param registro
     *            El registro de datos para generar encabezado.
     * @throws SerializadorException
     *         En caso de error al serializar.
     */
     public void generarEncabezadoRegistro(final Registro registro, String tipoArchivo, String periodicidad, String rutDeclarante, Date fechaHasta) throws SerializadorException {

     };
     
     /**
      * Generador de encabezado con registro
      *
      * @param registro
      *            El registro de datos para generar encabezado.
      * @throws SerializadorException
      *         En caso de error al serializar.
      */
      public void generarEncabezadoRegistro(String fechaPeriodoComercial, String tipoArchivo, String periodicidad, String rutDeclarante, Date fechaHasta) throws SerializadorException {

      };

    /**
     * L&oacute;gica de generaci&oacute;n de la declaraci&oacute;n asociada al
     * registro de datos.
     *
     * @param registro
     *            El registro de datos a generar.
     * @throws SerializadorException
     *             En caso de error al serializar.
     */
    public abstract void generarDetalle(final Registro registro) throws SerializadorException;
    
    /**
     * L&oacute;gica de generaci&oacute;n de la declaraci&oacute;n asociada al
     * registro de datos solo para dj1.
     *
     * @param registro
     *            El registro de datos a generar.
     * @throws SerializadorException
     *             En caso de error al serializar.
     */
    public List<SeccionLinea> generarDetalleDJ1(final Registro registro, String tipoArchivo, Connection conn, String periodicidad,Date fechaHasta) throws  SerializadorException, SQLException {
    	return new ArrayList<SeccionLinea>();
    };
    
    /**
     * Imprime las lineas generadas
     *
     * @param List<String>
     *            La lista de lineas a imprimir
     */
    public void imprimirLineasDJ1(List<SeccionLinea> lstSeccionLinea) throws  SerializadorException, SQLException {
    	
    };

    /**
     * L&oacute;gica de generaci&oacute;n de los primeros registros de la
     * declaraci&oacute;n.
     *
     * @throws SerializadorException
     *             En caso de error al serializar.
     */
    public abstract void generarEncabezado() throws SerializadorException;
    
    /**
     * Genera el registro tipo 0.
     *
     * @return El registro tipo 0.
     * @throws SerializadorException
     *             En caso de error al serializar.
     */
    public abstract String generarRegistroTipo0() throws SerializadorException;

    /**
     * L&oacute;gica de generaci&oacute;n de los &uacute;ltimos registros de la
     * declaraci&oacute;n.
     *
     * @throws SerializadorException
     *             En caso de error al serializar.
     */
    public abstract void generarResumen() throws SerializadorException;

    /**
     * Obtiene la cantidad de casos informados.
     *
     * @return La cantidad de casos informados.
     */
    public int getCantidadCasosInformados() {
        return cantidadCasosInformados;
    }

    /**
     * Obtiene el contexto.
     *
     * @return El contexto.
     */
    public ContextoGeneracion getContexto() {
        return contexto;
    }

    /**
     * Imprime texto.
     *
     * @param str
     *            El texto.
     */
    public void print(final String str) {
        out.print(str);
    }

    /**
     * Imprime una l&iacute;nea de texto.
     *
     * @param str
     *            La l&iacute;nea de texto.
     */
    public void println(final String str) {
        out.println(str);
    }

    /**
     * Reemplaza el registro 0 en el archivo de resultado.
     * <p>
     * Esto se hace al final, momento en el cual se tiene el contador
     * actualizado de la cantidad de registros generados.
     *
     * @throws IOException
     *             En caso de error de I/O.
     * @throws SerializadorException
     *             En caso de error al serializar.
     */
    private void reemplazarRegistroTipo0() throws IOException, SerializadorException {
        try (RandomAccessFile f = new RandomAccessFile(new File(contexto.getRutaArchivoGenerado()),
                "rw")) {
            final String registroTipo0 = generarRegistroTipo0();
            f.seek(0);
            f.write(registroTipo0.getBytes());
        }
    }
}
