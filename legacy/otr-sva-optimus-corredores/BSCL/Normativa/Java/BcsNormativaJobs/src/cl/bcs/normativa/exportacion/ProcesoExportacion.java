package cl.bcs.normativa.exportacion;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import com.opencsv.CSVWriter;

import cl.bcs.normativa.data.Registro;
import cl.bcs.normativa.esquema.Campo;
import cl.bcs.normativa.esquema.ResumenGeneracion;
import cl.bcs.normativa.generacion.ProcesoGeneracion;
import cl.bcs.normativa.generacion.ResumenGeneracionBuilder;
import cl.bcs.normativa.lectores.Lector;
import cl.bcs.normativa.lectores.LectorBuilder;
import cl.bcs.normativa.procesos.ControladorProceso;
import cl.bcs.normativa.procesos.Proceso;
import cl.bcs.normativa.procesos.ProcesoException;
import cl.bcs.sql.metadata.SqlMetadata;
import cl.bcs.sql.metadata.SqlTable;
import cl.bcs.utils.PropertiesUtil;


/**
 * Implementaci&oacute;n de la exportaci&oacute;n de documentos.
 *
 * @author fpino@email.com
 *
 */
public class ProcesoExportacion implements Proceso {

    /**
     * Instancia del Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ProcesoGeneracion.class.getName());
    private static Locale LOCALE;
    
    /**
     * Obtiene instancia de {@link ProcesoExportacion}.
     *
     * @param conn
     *            La conexi&oacute;n a la base de datos.
     * @param map
     *            {@link Map} de par&aacute;metros proporcionado por el Job
     *            Manager.
     * @param idProceso
     *            El id del proceso.
     * @return Instancia de {@link ProcesoExportacion}.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public static Proceso newInstance(final Connection conn, final Map<String, String> map,
            final long idProceso, final DataSource ds) throws SQLException {
        final Properties props = PropertiesUtil.newProperties(map);
        return newInstance(conn, props, idProceso, ds);
    }

    /**
     * Obtiene instancia de {@link ProcesoExportacion}.
     *
     * @param conn
     *            La conexi&oacute;n a la base de datos.
     * @param props
     *            {@link Properties}.
     * @param idProceso
     *            El id de proceso.
     * @return Instancia de {@link ProcesoExportacion}.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public static Proceso newInstance(final Connection conn, final Properties props,
            final long idProceso, final DataSource ds) throws SQLException {
        return new ProcesoExportacion(conn, props, idProceso, ds);
    }

    /**
     * Contexto de exportaci&oacute;n de datos.
     */
    private final ContextoExportacion contexto;

    /**
     * Constructor.
     *
     * @param conn
     *            La conexi&oacute;n a la base de datos.
     * @param props
     *            {@link Properties}.
     * @param idProceso
     *            El id de proceso.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public ProcesoExportacion(final Connection conn, final Properties props, final long idProceso, final DataSource ds)
            throws SQLException {
        this.contexto = new ContextoExportacion(conn, props, idProceso, ds);
    }

    /**
     * Implementa la l&oacute;gica de exportaci&oacute;n.
     *
     * @throws SQLException
     *             En caso de error SQL.
     */
    private void exportar() throws SQLException {
        final SqlMetadata metadata = contexto.getMetadata();
        final String nombreTablaDestino = contexto.getEsquema().getNombreTablaDestino();
        final SqlTable tablaDestino = metadata.getTable(contexto.getSchema(), nombreTablaDestino);
        final List<Campo> campos = contexto.getEsquema().getCamposExportables();
        final String rutaResultado = contexto.buscarRutaArchivo("exportacion");
        final ResumenGeneracion resumen = ResumenGeneracionBuilder.crearResumen(contexto, rutaResultado, 0);

        try (final Lector lector = LectorBuilder.newLectorDestino(contexto, tablaDestino, null, true, null, true);
        		FileOutputStream fos =  new FileOutputStream(rutaResultado);
        		Writer fw = new OutputStreamWriter(fos, StandardCharsets.ISO_8859_1);
                CSVWriter writer = new CSVWriter(fw,
                        contexto.getOpciones().getSeparadorListas().charAt(0))) {
        	
            Registro registro = lector.siguiente();
            boolean withHeader = true;
            while (registro != null) {
                if (withHeader) {
                    final String[] entries = new String[campos.size()];
                    for (int i = 0; i < campos.size(); i++) {
                        final Campo campo = campos.get(i);
                        entries[i] = campo.getNombre();
                    }
                    writer.writeNext(entries);
                    withHeader = false;
                }

                final String[] entries = new String[campos.size()];
                for (int i = 0; i < campos.size(); i++) {
                    final Campo campo = campos.get(i);
                    entries[i] = formatear(registro, campo);
                }
                writer.writeNext(entries, false);

                registro = lector.siguiente();
            }

            LOGGER.info(
                    String.format("%d registros exportados", lector.getCantidadRegistrosLeidos()));

            resumen.setGlosa("Finalizada");
            resumen.setTotalRegistrosGenerados(lector.getCantidadRegistrosLeidos());
        } catch (Exception ex) {
            resumen.setGlosa("Interrumpida");
            LOGGER.log(Level.SEVERE, "Error durante proceso de exportacion", ex);
        } finally {
            ResumenGeneracionBuilder.actualizarResumen(contexto, resumen);
        }
    }

    /**
     * Formatea el campo del registo, seg&uacute;n el tipo de dato y las
     * opciones de exportaci&oacute;n.
     *
     * @param registro
     *            El registro de datos.
     * @param campo
     *            El campo.
     * @return String con el valor del campo, seg&uacute;n el formato indicado
     *         en las opciones.
     */
    private String formatear(final Registro registro, final Campo campo) {
        final Comparable<?> val = registro.get(campo.getNombre());

        if (val == null) {
            return null;
        }

        if (campo.getTipoDato().startsWith("DATE")) {
            final DateFormat df = new SimpleDateFormat(contexto.getOpciones().getFormatoFechas());
            return df.format(val);
        } else {
            String ret = val.toString();
            if (campo.getTipoDato().startsWith("DECIMAL")|| campo.getTipoDato().startsWith("QTY") || campo.getTipoDato().startsWith("AMOUNT")) {
            	
            	ret = String.format(LOCALE, "%." + campo.getCantidadDecimales() + "f",((Number) val).doubleValue());
                ret = ret.replace('.', contexto.getOpciones().getSimboloDecimal().charAt(0));
            	
                
                
            }
            return ret;
        }
    }

    @Override
    public void procesar() throws ProcesoException {
        LOGGER.info(String.format("Se recibio solicitud de proceso de exportacion."));

        try (final ControladorProceso controlador = new ControladorProceso(contexto)) {
            try {
                exportar();
            } catch (SQLException ex) {
                final String msg = "Error durante proceso de exportacion";
                LOGGER.log(Level.SEVERE, msg, ex);
                throw new ProcesoException(msg, ex);
            }
        }
    }

}
