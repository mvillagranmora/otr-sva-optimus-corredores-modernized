package cl.bcs.normativa.cuadratura;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import cl.bcs.normativa.data.DataParserException;
import cl.bcs.normativa.data.Registro;
import cl.bcs.normativa.esquema.Campo;
import cl.bcs.normativa.esquema.ParComparativo;
import cl.bcs.normativa.lectores.LectorSQL;
import cl.bcs.normativa.procesos.ControladorProceso;
import cl.bcs.normativa.procesos.Proceso;
import cl.bcs.normativa.procesos.ProcesoException;
import cl.bcs.utils.PropertiesUtil;

/**
 * Implementaci&oacute;n com&uacute;n de la cuadratura de datos de un documento.
 *
 * @author fpino@email.com
 *
 */
public abstract class ProcesoCuadratura implements Proceso {

    /**
     * Instancia del Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ProcesoCuadratura.class.getName());

    /**
     * Obtiene instancia de {@link ProcesoCuadratura}.
     *
     * @param conn
     *            La conexi&oacute;n a la base de datos.
     * @param map
     *            {@link Map} de par&aacute;metros proporcionado por el Job
     *            Manager.
     * @param idProceso
     *            El id del proceso.
     * @return Instancia de {@link ProcesoCuadratura}.
     * @throws SQLException
     *             En caso de error SQL.
     * @throws DataParserException
     *             En caso de error al interpretar dato del clasificador.
     */
    public static ProcesoCuadratura newInstance(final Connection conn,
            final Map<String, String> map, final long idProceso, final DataSource ds)
                    throws SQLException, DataParserException {
        final Properties props = PropertiesUtil.newProperties(map);
        return newInstance(conn, props, idProceso, ds);
    }

    /**
     * Obtiene instancia de {@link ProcesoCuadratura}.
     *
     * @param conn
     *            La conexi&oacute;n a la base de datos.
     * @param props
     *            {@link Properties}.
     * @param idProceso
     *            El id de proceso.
     * @return Instancia de {@link ProcesoCuadratura}.
     * @throws SQLException
     *             En caso de error SQL.
     * @throws DataParserException
     *             En caso de error al interpretar dato del clasificador.
     */
    public static ProcesoCuadratura newInstance(final Connection conn, final Properties props,
            final long idProceso, final DataSource ds) throws SQLException, DataParserException {
        final ContextoCuadratura contexto = new ContextoCuadratura(conn, props, idProceso, ds);
        return newInstance(contexto);
    }

    /**
     * Obtiene nueva instancia de {@link ProcesoCuadratura}.
     *
     * @param contexto
     *            El contexto de cuadratura.
     * @return Nueva instancia de {@link ProcesoCuadratura}.
     * @throws SQLException
     *             En caso de error SQL.
     */
    private static ProcesoCuadratura newInstance(final ContextoCuadratura contexto)
            throws SQLException {
        if ("INDIVIDUAL".equals(contexto.getTipoCuadratura())) {
            return new ProcesoCuadraturaIndividual(contexto);
        } else {
            return new ProcesoCuadraturaAgrupada(contexto);
        }
    }

    /**
     * Contexto de cuadratura.
     */
    private final ContextoCuadratura contexto;

    /**
     * Resultado de la cuadratura.
     */
    private final ResultadoCuadratura resultado;

    /**
     * Constructor.
     *
     * @param contexto
     *            El contexto de cuadratura.
     * @throws SQLException
     *             En caso de error SQL.
     */
    protected ProcesoCuadratura(final ContextoCuadratura contexto) throws SQLException {
        this.contexto = contexto;
        this.resultado = new ResultadoCuadratura(contexto);
    }

    /**
     * Calcula las diferencias entre el registro origen y el registro destino.
     *
     * @param registroOrigen
     *            El registro origen.
     * @param registroDestino
     *            El registro destino.
     * @return Registro comparativo de las diferencias entre el registro origen
     *         y el registro destino o, <code>null</code> si no hay diferencias.
     */
    private Registro calcularDiferencias(final Registro registroOrigen,
            final Registro registroDestino) {

        Registro ret = null;
        for (final Campo campoDato : this.getContexto().getEsquema().getCamposDatos()) {
            final String key = campoDato.getNombre();
            final ParComparativo par = calcularParComparativo(key, registroOrigen, registroDestino);

            if (par != null) {
                if (ret == null) {
                    ret = new Registro(contexto.getEsquema());
                }
                ret.put(key, par);
            }
        }

        return ret;
    }

    /**
     * Calcula par comparativo asociado al nombre de campo.
     *
     * @param nombreCampo
     *            El nombre de campo.
     * @param registroOrigen
     *            El registro origen.
     * @param registroDestino
     *            El registro destino.
     * @return Par comparativo asociado al nombre de campo.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public ParComparativo calcularParComparativo(final String nombreCampo,
            final Registro registroOrigen, final Registro registroDestino) {

        ParComparativo par = null;

        final Comparable valorOrigen = registroOrigen.get(nombreCampo);
        final Comparable valorDestino = registroDestino.get(nombreCampo);

        if (valorOrigen == null) {
            if (valorDestino != null) {
                par = new ParComparativo(valorOrigen, valorDestino);
            }
        } else if (valorDestino == null) {
            par = new ParComparativo(valorOrigen, valorDestino);
        } else if (valorOrigen.compareTo(valorDestino) != 0) {
            par = new ParComparativo(valorOrigen, valorDestino);
        }

        return par;
    }

    /**
     * Caso registros diferentes.
     *
     * @param registroComparativo
     *            El registro comparativo.
     * @throws SQLException
     *             En caso de error SQL.
     * @throws DataParserException
     *             En caso de error en el parser de datos.
     * @throws IOException
     *             En caso de error de I/O.
     */
    private void casoDiferentes(final Registro registroComparativo)
            throws SQLException, DataParserException, IOException {
        final Date anoMes = getAnoMes(registroComparativo);
        resultado.contar(anoMes, TipoDiferencia.DIFERENTE, registroComparativo);
        resultado.agregarIdDetalles(anoMes, TipoDiferencia.DIFERENTE,
                getIdDetalles(registroComparativo));
    }

    /**
     * Caso registro faltante.
     *
     * @param registroOrigen
     *            El registro origen.
     * @throws SQLException
     *             En caso de error SQL.
     * @throws DataParserException
     *             En caso de error en el parser de datos.
     * @throws IOException
     *             En caso de error de I/O.
     */
    private void casoFaltante(final Registro registroOrigen)
            throws SQLException, DataParserException, IOException {
        final Date anoMes = getAnoMes(registroOrigen);
        resultado.contar(anoMes, TipoDiferencia.FALTANTE, registroOrigen);
    }

    /**
     * Caso registros iguales.
     *
     * @param reporte
     *            El reporte.
     * @param registro
     *            El registro.
     * @throws SQLException
     *             En caso de error SQL.
     * @throws DataParserException
     *             En caso de error en el parser de datos.
     * @throws IOException
     *             En caso de error de I/O.
     */
    private void casoIguales(final Registro registro)
            throws SQLException, DataParserException, IOException {
        final Date anoMes = getAnoMes(registro);
        resultado.contar(anoMes, TipoDiferencia.CUADRADO, registro);
        resultado.agregarIdDetalles(anoMes, TipoDiferencia.CUADRADO, getIdDetalles(registro));
    }

    /**
     * Caso registro sobrante.
     *
     * @param registroDestino
     *            El registro destino.
     * @throws SQLException
     *             En caso de error SQL.
     * @throws DataParserException
     *             En caso de error en el parser de datos.
     * @throws IOException
     *             En caso de error de I/O.
     */
    private void casoSobrante(final Registro registroDestino)
            throws SQLException, DataParserException, IOException {
        final Date anoMes = getAnoMes(registroDestino);
        resultado.contar(anoMes, TipoDiferencia.SOBRANTE, registroDestino);
        resultado.agregarIdDetalles(anoMes, TipoDiferencia.SOBRANTE,
                getIdDetalles(registroDestino));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected int compare(final Comparable cmp0, final Comparable cmp1) {
        if (cmp0 == null) {
            if (cmp1 == null) {
                return 0;
            } else {
                return -1;
            }
        } else {
            if (cmp1 == null) {
                return 1;
            } else {
                return cmp0.compareTo(cmp1);
            }
        }
    }

    @SuppressWarnings("rawtypes")
    protected int compare(final List<String> nombres, final Registro registro0,
            final Registro registro1) {
        int ret = 0;
        for (final String nombre : nombres) {
            final Comparable cmp0 = registro0.get(nombre);
            final Comparable cmp1 = registro1.get(nombre);
            ret = compare(cmp0, cmp1);
            if (ret != 0) {
                return ret;
            }
        }
        return ret;
    }

    protected abstract int compare(Registro registro0, Registro registro1);

    /**
     * Ciclo de comparaci&oacute;n del origen y el destino.
     *
     * @throws SQLException
     *             En caso de error SQL.
     * @throws DataParserException
     *             En caso de error en el parser de datos.
     * @throws IOException
     *             En caso de error de I/O.
     */
    private void cuadrar() throws SQLException, DataParserException, IOException {

        try (final LectorSQL lectorOrigen = getLectorOrigen();
                final LectorSQL lectorDestino = getLectorDestino()) {
            Registro registroOrigen = lectorOrigen.siguiente();
            Registro registroDestino = lectorDestino.siguiente();

            while (true) {
                if (registroOrigen == null) {
                    if (registroDestino == null) {
                        break;
                    } else {
                        casoSobrante(registroDestino);
                        registroDestino = lectorDestino.siguiente();
                    }
                } else if (registroDestino == null) {
                    casoFaltante(registroOrigen);
                    registroOrigen = lectorOrigen.siguiente();
                } else {
                    final int cmp = compare(registroOrigen, registroDestino);
                    if (cmp < 0) {
                        casoFaltante(registroOrigen);
                        registroOrigen = lectorOrigen.siguiente();
                    } else if (cmp > 0) {
                        casoSobrante(registroDestino);
                        registroDestino = lectorDestino.siguiente();
                    } else {
                        final Registro registroComparativo = calcularDiferencias(registroOrigen,
                                registroDestino);
                        if (registroComparativo == null) {
                            casoIguales(registroDestino);
                        } else {
                            casoDiferentes(registroComparativo);
                        }
                        registroOrigen = lectorOrigen.siguiente();
                        registroDestino = lectorDestino.siguiente();
                    }
                }
            }
        }
    }

    /**
     * Obtiene el a&ntilde;o/mes del registro.
     *
     * @param registro
     *            El registro.
     * @return El a&ntilde;o/mes del registros.
     */
    protected abstract Date getAnoMes(final Registro registro);

    /**
     * Obtiene el contexto de cuadratura.
     *
     * @return El contexto de cuadratura.
     */
    public ContextoCuadratura getContexto() {
        return contexto;
    }

    /**
     * Obtiene la lista de los id's de los detalles relacionados a un resumen de
     * cuadratura.
     *
     * @param registroDestino
     *            Registro destino en la cuadratura.
     * @return La lista de los id's de los detalles relacionados a un resumen de
     *         cuadratura.
     * @throws SQLException
     *             En caso de error SQL.
     */
    protected abstract List<Long> getIdDetalles(final Registro registroDestino) throws SQLException;

    /**
     * Obtiene el lector de los datos en el destino, seg&uacute;n el tipo de
     * cuadratura.
     *
     * @return El lector de los datos en el destino, seg&uacute;n el tipo de
     *         cuadratura.
     * @throws SQLException
     *             En caso de error SQL.
     */
    protected abstract LectorSQL getLectorDestino() throws SQLException;

    /**
     * Obtiene el lector de los datos en el origen, seg&uacute;n el tipo de
     * cuadratura.
     *
     * @return El lector de los datos en el origen, seg&uacute;n el tipo de
     *         cuadratura.
     * @throws SQLException
     *             En caso de error SQL.
     */
    protected abstract LectorSQL getLectorOrigen() throws SQLException;

    @Override
    public void procesar() throws ProcesoException {
        LOGGER.info(String.format("Se recibio solicitud de proceso de cuadratura."));

        final String rutaReporte = contexto.buscarRutaReporte();
        try (final PrintStream out = new PrintStream(rutaReporte)) {
            final ReporteCuadratura reporte = new ReporteCuadratura(out);
            reporte.printEncabezado(contexto);

            try (final ControladorProceso controlador = new ControladorProceso(contexto)) {
                try {
                    resultado.limpiar();
                    cuadrar();
                    resultado.limpiarCuadrados();
                    resultado.registrar();
                    reporte.print(resultado);
                } catch (SQLException | DataParserException | IOException ex) {
                    final String msg = "Error al procesar carga";
                    reporte.println(msg);
                    reporte.println(ex.toString());
                    LOGGER.log(Level.SEVERE, msg, ex);
                    throw new ProcesoException(msg, ex);
                }
            }
        } catch (FileNotFoundException ex) {
            final String msg = "Error al abrir archivo de reporte";
            LOGGER.log(Level.SEVERE, msg, ex);
            throw new ProcesoException(msg, ex);
        }
    }

}
