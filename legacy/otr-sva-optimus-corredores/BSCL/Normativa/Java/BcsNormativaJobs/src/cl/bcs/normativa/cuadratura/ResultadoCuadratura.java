package cl.bcs.normativa.cuadratura;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.time.DateUtils;

import cl.bcs.normativa.dal.DetalleCuadraturaInserter;
import cl.bcs.normativa.dal.ResumenCuadraturaDAO;
import cl.bcs.normativa.dal.ResumenCuadraturaInserter;
import cl.bcs.normativa.data.DataParserException;
import cl.bcs.normativa.data.Registro;
import cl.bcs.normativa.esquema.DetalleCuadratura;
import cl.bcs.normativa.esquema.RangoFechas;
import cl.bcs.normativa.esquema.ResumenCuadratura;
import cl.bcs.normativa.procesos.ProcesoException;
import cl.bcs.utils.DateUtil;

/**
 * Resultado de cuadratura.
 *
 * @author fpino@email.com
 *
 */
public class ResultadoCuadratura {

    /**
     * Contexto de cuadratura.
     */
    private final ContextoCuadratura contexto;

    /**
     * Map de los meses de cuadratura.
     */
    private final Map<Date, Map<TipoDiferencia, ResumenCuadratura>> mapMeses;

    /**
     * Constructor.
     *
     * @param contexto
     *            El contexto de cuadratura.
     */
    public ResultadoCuadratura(final ContextoCuadratura contexto) {
        this.contexto = contexto;
        this.mapMeses = new TreeMap<>();

        inicializar();
    }

    /**
     * Crea y almacena {@link ResumenCuadratura}.
     *
     * @param anoMes
     *            El a&ntilde;o/mes.
     * @param tipoDiferencia
     *            El tipo de diferencia.
     * @return Instancia de {@link ResumenCuadratura}.
     */
    private ResumenCuadratura agregar(final Date anoMes, final TipoDiferencia tipoDiferencia) {
        final ResumenCuadratura resumen = newResumenCuadratura(anoMes, tipoDiferencia);
        getMapResumen(anoMes).put(tipoDiferencia, resumen);
        return resumen;
    }

    /**
     * Agrega lista de id's de los detalles de documento al resumen de
     * cuadratura, correspondiente al a&ntilde;o/mes y tipo de diferencia.
     *
     * @param anoMes
     *            El a&ntilde;o/mes.
     * @param tipoDiferencia
     *            El tipo de diferencia.
     * @param idDetalles
     *            La lista de id's de los detalles de documento.
     */
    public void agregarIdDetalles(final Date anoMes, final TipoDiferencia tipoDiferencia,
            final List<Long> idDetalles) {
        final ResumenCuadratura resumen = getResumenCuadratura(anoMes, tipoDiferencia);
        resumen.getIdDetalles().addAll(idDetalles);
    }

    /**
     * Contabiliza diferencia en la cuadratura.
     *
     * @param anoMes
     *            El a&ntilde;o/mes.
     * @param tipoDiferencia
     *            El tipo de diferencia.
     * @param registro
     *            El registro.
     * @throws SQLException
     *             En caso de error SQL.
     * @throws DataParserException
     *             En caso de error en el parser de datos.
     */
    public void contar(final Date anoMes, final TipoDiferencia tipoDiferencia,
            final Registro registro) throws SQLException, DataParserException {

        eliminar(anoMes, TipoDiferencia.SIN_DATOS);

        final ResumenCuadratura resumen = getResumenCuadratura(anoMes, tipoDiferencia);
        resumen.contarCasos();
        resumen.getRegistros().add(registro);
    }

    /**
     * Elimina {@link ResumenCuadratura}.
     *
     * @param anoMes
     *            El ano/mes.
     * @param tipoDiferencia
     *            El tipo de diferencia.
     */
    private void eliminar(final Date anoMes, final TipoDiferencia tipoDiferencia) {
        final Map<TipoDiferencia, ResumenCuadratura> map = getMapResumen(anoMes);
        map.remove(tipoDiferencia);
    }

    /**
     * Obtiene la glosa del resumen de cuadratura.
     *
     * @param tipoDiferencia
     *            El tipo de diferencia.
     * @return La glosa del resumen de cuadratura.
     */
    private String getGlosa(final TipoDiferencia tipoDiferencia) {
        final String descripcion = contexto.getDescripcionCuadratura();

        switch (tipoDiferencia) {
        case SIN_DATOS:
            return String.format("No existen datos para '%s'", descripcion);
        case CUADRADO:
            return String.format(
                    "La declaracion no presenta diferencias en comparacion '%s' respecto al origen",
                    descripcion);
        case DIFERENTE:
            return String.format(
                    "La declaracion presenta diferencias en comparacion '%s' respecto al origen",
                    descripcion);
        case FALTANTE:
            return String.format("Existen registros no declarados en comparacion '%s'",
                    descripcion);
        case SOBRANTE:
            return String.format("Existen registros no presentes en el origen en comparacion '%s'",
                    descripcion);
        default:
            throw new ProcesoException(
                    "Tipo de diferencia no soportado: " + tipoDiferencia.toString());
        }
    }

    /**
     * Obtiene el map de los resumenes.
     *
     * @return El map de los resumenes.
     */
    public Map<Date, Map<TipoDiferencia, ResumenCuadratura>> getMapMeses() {
        return mapMeses;
    }

    /**
     * Obtiene el map de los resumenes.
     *
     * @param anoMes
     *            El a&ntilde;o/mes.
     * @return el map de los resumenes.
     */
    public Map<TipoDiferencia, ResumenCuadratura> getMapResumen(final Date anoMes) {
        Map<TipoDiferencia, ResumenCuadratura> map = mapMeses.get(anoMes);
        if (map == null) {
            map = new TreeMap<>();
            mapMeses.put(anoMes, map);
        }

        return map;
    }

    /**
     * Obtiene el resumen de cuadratura.
     *
     * @param anoMes
     *            El a&ntilde;o/mes.
     * @param tipoDiferencia
     *            El tipo de diferencia.
     * @return El resumen de cuadratura.
     */
    public ResumenCuadratura getResumenCuadratura(final Date anoMes,
            final TipoDiferencia tipoDiferencia) {
        final Map<TipoDiferencia, ResumenCuadratura> map = getMapResumen(anoMes);
        ResumenCuadratura resumen = map.get(tipoDiferencia);
        if (resumen == null) {
            resumen = agregar(anoMes, tipoDiferencia);
        }

        return resumen;
    }

    /**
     * Inicializa cada mes, indicando sin datos.
     */
    private void inicializar() {
        final RangoFechas rangoFechas = contexto.getEsquema().getRangoFechas();
        final Date mesDesde = DateUtil.getStartOfMonth(rangoFechas.getFechaDesde());
        final Date mesHasta = DateUtil.getStartOfMonth(rangoFechas.getFechaHasta());
        for (Date mes = mesDesde; mes.compareTo(mesHasta) <= 0; mes = DateUtils.addMonths(mes, 1)) {
            agregar(mes, TipoDiferencia.SIN_DATOS);
        }
    }

    /**
     * Elimina los resumenes de cuadratura generados en proceso previo.
     *
     * @throws SQLException
     *             En caso de error SQL.
     */
    public void limpiar() throws SQLException {
        contexto.getConnection().setAutoCommit(true);
        ResumenCuadraturaDAO.eliminarPorIdDocumento(contexto.getConnection(), contexto.getSchema(),
                contexto.getDocumento().getId(), contexto.getTipoCuadratura(),
                contexto.getTipoClasificador());
    }

    /**
     * Elimina los resumenes cuadrados, en la medida que haya alg&uacute;n otro
     * tipo de diferencia, para cada mes.
     */
    public void limpiarCuadrados() {
        for (final Date mes : mapMeses.keySet()) {
            final Map<TipoDiferencia, ResumenCuadratura> map = mapMeses.get(mes);
            if (map.values().size() > 1) {
                map.remove(TipoDiferencia.CUADRADO);
            }
        }
    }

    /**
     * Obtiene nueva instancia de {@link ResumenCuadratura}.
     *
     * @param anoMes
     *            El a&ntilde;o/mes.
     * @param tipoDiferencia
     *            El tipo de diferencia.
     * @return Nueva instancia de {@link ResumenCuadratura}.
     */
    private ResumenCuadratura newResumenCuadratura(final Date anoMes,
            final TipoDiferencia tipoDiferencia) {
        final ResumenCuadratura resumen = new ResumenCuadratura();
        resumen.setIdProceso(contexto.getIdProceso());
        resumen.setIdDocumento(contexto.getDocumento().getId());
        resumen.setIdUsuario(contexto.getUsuario().getId());

        resumen.setTipoCuadratura(contexto.getTipoCuadratura());
        resumen.setTipoClasificador(contexto.getTipoClasificador());

        if (tipoDiferencia != null) {
            resumen.setTipoDiferencia(tipoDiferencia.toString());
        }

        resumen.setFechaCuadratura(contexto.getFechaCuadratura());
        resumen.setCantidadCasos(0);

        resumen.setAnoMes(anoMes);
        resumen.setGlosa(getGlosa(tipoDiferencia));

        return resumen;
    }

    /**
     * Registra los resumenes y detalles resultado de la cuadratura.
     *
     * @throws SQLException
     *             En caso de error SQL.
     */
    public void registrar() throws SQLException {
        contexto.getConnection().setAutoCommit(false);

        final ResumenCuadraturaInserter resumenInserter = new ResumenCuadraturaInserter(
                contexto.getConnection(), contexto.getMetadata(), contexto.getSchema());
        for (final Map<TipoDiferencia, ResumenCuadratura> map : mapMeses.values()) {
            for (final ResumenCuadratura resumen : map.values()) {
                resumenInserter.insert(resumen);
            }
        }
        resumenInserter.endBatch();

        final DetalleCuadraturaInserter detalleInserter = new DetalleCuadraturaInserter(
                contexto.getConnection(), contexto.getMetadata(), contexto.getSchema());
        for (final Map<TipoDiferencia, ResumenCuadratura> map : mapMeses.values()) {
            for (final ResumenCuadratura resumen : map.values()) {
                for (final long idDetalle : resumen.getIdDetalles()) {
                    final DetalleCuadratura detalle = new DetalleCuadratura();
                    detalle.setIdDetalleDocumento(idDetalle);
                    detalle.setIdResumenCuadratura(resumen.getId());
                    detalleInserter.insert(detalle);
                }
            }
        }

        detalleInserter.endBatch();
    }

}
