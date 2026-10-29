package cl.bcs.normativa.cuadratura;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cl.bcs.normativa.carga.TipoCarga;
import cl.bcs.normativa.dal.CampoCuadraturaDAO;
import cl.bcs.normativa.dal.SecuenciadorRegistro;
import cl.bcs.normativa.data.Registro;
import cl.bcs.normativa.esquema.Campo;
import cl.bcs.normativa.esquema.CampoClasificador;
import cl.bcs.normativa.esquema.Esquema;
import cl.bcs.normativa.lectores.LectorSQL;
import cl.bcs.normativa.procesos.ProcesoException;
import cl.bcs.sql.generator.SqlFormatter;
import cl.bcs.sql.generator.SqlGenerator;
import cl.bcs.sql.generator.SqlGeneratorBuilder;
import cl.bcs.sql.metadata.SqlTable;
import cl.bcs.sql.statements.SqlWhere;
import cl.bcs.utils.DateUtil;
import cl.bcs.utils.StringUtils;

/**
 * Implementaci&oacute;n de la cuadratura agrupada de datos de un documento.
 *
 * @author fpino@email.com
 *
 */
public class ProcesoCuadraturaAgrupada extends ProcesoCuadratura {

    /**
     * Instancia del generador SQL.
     */
    private static final SqlGenerator GENERATOR = SqlGeneratorBuilder.getInstance();

    /**
     * R&oacute;tulo de la columna contador de registros.
     */
    public static final String ROTULO_COLUMNA_CONTADOR = "CNT";

    /**
     * R&oacute;tulo de la columna periodo.
     */
    public static final String ROTULO_COLUMNA_PERIODO = "MES";

    /**
     * Lista de los nombres de campo de la cuadratura.
     */
    private final List<String> nombresCamposCuadratura;

    /**
     * Constructor.
     *
     * @param contexto
     *            El contexto de cuadratura.
     * @throws ProcesoException
     *             En caso de error de proceso.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public ProcesoCuadraturaAgrupada(
            final ContextoCuadratura contexto) throws ProcesoException, SQLException {
        super(contexto);

        this.nombresCamposCuadratura = buscarNombresCamposCuadratura(contexto);
    }

    /**
     * Arma el SELECT para la lectura de datos (sirve tanto para el origen como
     * para el destino, dependiendo de los par&aaacute;metros).
     *
     * @param tabla
     *            La tabla.
     * @param tipoCarga
     *            El tipo de carga.
     * @param filtrarPorFecha
     *            Indica si se filtra por fecha.
     * @return El SELECT para la lectura de datos.
     */
    private String armarSelect(
            final SqlTable tabla,
            final TipoCarga tipoCarga,
            final boolean filtrarPorFecha) {
        final Esquema esquema = getContexto().getEsquema();
        final String nombreCampoFecha = esquema.getNombreCampoFecha();

        List<String> cols = new ArrayList<>();
        cols.add(
                String.format("TO_VARCHAR(%s, 'YYYY-MM')", GENERATOR.getSqlText(nombreCampoFecha)));
        for (final String nombreCampo : this.nombresCamposCuadratura) {
            cols.add(GENERATOR.getSqlText(nombreCampo));
        }

        final String grupo = StringUtils.join(",", cols);

        cols = new ArrayList<>();
        cols.add(String.format("TO_VARCHAR(%s, 'YYYY-MM') %s",
                GENERATOR.getSqlText(nombreCampoFecha), ROTULO_COLUMNA_PERIODO));
        for (final String nombreCampo : this.nombresCamposCuadratura) {
            cols.add(GENERATOR.getSqlText(nombreCampo));
        }

        cols.add(String.format("COUNT(*) %s", ROTULO_COLUMNA_CONTADOR));

        for (final Campo campoDato : esquema.getCamposDatos()) {
            if (campoDato.isSumable()) {
                final String nombre = GENERATOR.getSqlText(campoDato.getNombre());
                cols.add(String.format("SUM(%s) %s", nombre, nombre));
            }
        }

        final String nombreTabla = GENERATOR.getSqlText(tabla);
        final String where = armarWhere(tipoCarga, filtrarPorFecha);
        return String.format("SELECT %s FROM %s %s GROUP BY %s ORDER BY %s",
                StringUtils.join(",", cols), nombreTabla, where, grupo, grupo);
    }

    /**
     * Arma el SELECT para la lectura del destino.
     *
     * @return El SELECT para la lectura del destino.
     * @throws SQLException
     *             En caso de error SQL.
     */
    private String armarSelectDestino()
            throws SQLException {
        final ContextoCuadratura contexto = getContexto();
        final SqlTable tablaDestino = contexto.getMetadata().getTable(contexto.getSchema(),
                contexto.getEsquema().getNombreTablaDestino());

        return armarSelect(tablaDestino, TipoCarga.INICIAL, false);
    }

    /**
     * Arma el SELECT para la lectura del destino.
     *
     * @return El SELECT para la lectura del destino.
     * @throws SQLException
     *             En caso de error SQL.
     */
    private String armarSelectIdsDestino(
            final Registro registroDestino)
                    throws SQLException {
        final ContextoCuadratura contexto = getContexto();
        final SqlTable tablaDestino = contexto.getMetadata().getTable(contexto.getSchema(),
                contexto.getEsquema().getNombreTablaDestino());

        final SqlWhere where = new SqlWhere();
        where.put("IdDocumento", contexto.getDocumento().getId());
        where.put("FuenteDatos", TipoCarga.INICIAL.toString());

        if (contexto.getClasificadorCuadratura() != null) {
            for (final CampoClasificador campoClasificador : contexto.getClasificadorCuadratura()
                    .getCamposClasificador()) {
                where.put(campoClasificador.getNombreCampo(),
                        campoClasificador.getValorAsComparable());
            }
        }

        for (final String nombre : this.nombresCamposCuadratura) {
            where.put(nombre, registroDestino.get(nombre));
        }

        String whereSql = GENERATOR.getSqlText(where);

        final Esquema esquema = contexto.getEsquema();
        final String nombreCampoFecha = esquema.getNombreCampoFecha();
        final String periodo = (String) registroDestino.get(ROTULO_COLUMNA_PERIODO);
        whereSql += " AND ";
        whereSql += String.format("TO_VARCHAR(%s, 'YYYY-MM')",
                GENERATOR.getSqlText(nombreCampoFecha));
        whereSql += "=";
        whereSql += SqlFormatter.format(periodo);

        final String nombreTabla = GENERATOR.getSqlText(tablaDestino);
        return String.format("SELECT %s FROM %s %s",
                GENERATOR.getSqlText(SecuenciadorRegistro.ID_COLUMN_NAME), nombreTabla, whereSql);
    }

    /**
     * Arma el SELECT para la lectura del origen.
     *
     * @return El SELECT para la lectura del origen.
     * @throws SQLException
     *             En caso de error SQL.
     */
    private String armarSelectOrigen()
            throws SQLException {
        final ContextoCuadratura contexto = getContexto();
        final SqlTable tablaOrigen = contexto.getMetadata().getTable(contexto.getSchema(),
                contexto.getEsquema().getNombreTablaOrigen());

        return armarSelect(tablaOrigen, null, true);
    }

    /**
     * Arma la clausula WHERE para la lectura de los datos (sirve tanto para el
     * origen como para el destino, dependiendo de los par&aaacute;metros).
     *
     * @param tipoCarga
     *            El tipo de carga.
     * @param filtrarPorFecha
     *            Indica si se filtra por fecha.
     * @return La clausula WHERE para la lectura de los datos.
     */
    private String armarWhere(
            final TipoCarga tipoCarga,
            final boolean filtrarPorFecha) {
        final ContextoCuadratura contexto = getContexto();
        final Esquema esquema = contexto.getEsquema();
        final String nombreCampoFecha = esquema.getNombreCampoFecha();
        final SqlWhere where = new SqlWhere();
        if (tipoCarga != null) {
            where.put("IdDocumento", contexto.getDocumento().getId());
            where.put("FuenteDatos", tipoCarga.toString());
        }

        if (contexto.getClasificadorCuadratura() != null) {
            for (final CampoClasificador campoClasificador : contexto.getClasificadorCuadratura()
                    .getCamposClasificador()) {
                where.put(campoClasificador.getNombreCampo(),
                        campoClasificador.getValorAsComparable());
            }
        }

        if (filtrarPorFecha) {
            where.put(nombreCampoFecha, contexto.getFiltroFechas().getFechaDesde(),
                    contexto.getFiltroFechas().getFechaHasta());
        }

        return GENERATOR.getSqlText(where);
    }

    /**
     * Busca la lista de nombres de campos de la cuadratura.
     *
     * @param contexto
     *            El contexto de cuadratura.
     * @return La lista de nombres de campos de la cuadratura.
     * @throws SQLException
     *             En caso de error SQL.
     */
    private List<String> buscarNombresCamposCuadratura(
            final ContextoCuadratura contexto)
                    throws SQLException {
        final String tipoCuadratura = contexto.getTipoCuadratura();
        final List<String> list = CampoCuadraturaDAO.buscarPorDocumentoMasTipo(
                contexto.getConnection(), contexto.getSchema(), contexto.getDocumento(),
                tipoCuadratura);
        if (list.isEmpty()) {
            throw new ProcesoException("No esta especificada la cuadratura: " + tipoCuadratura);
        }

        return list;
    }

    @Override
    protected int compare(
            final Registro registro0,
            final Registro registro1) {
        final List<String> nombres = new ArrayList<>();
        nombres.add(ROTULO_COLUMNA_PERIODO);
        for (final String nombre : nombresCamposCuadratura) {
            nombres.add(nombre);
        }

        return compare(nombres, registro0, registro1);
    }

    @Override
    protected Date getAnoMes(
            final Registro registro) {
        return DateUtil.getAnoMes((String) registro.get(ROTULO_COLUMNA_PERIODO));
    }

    @Override
    protected List<Long> getIdDetalles(
            final Registro registroDestino)
                    throws SQLException {
        final List<Long> list = new ArrayList<>();
        final String sql = armarSelectIdsDestino(registroDestino);

        try (final LectorSQL lector = new LectorSQL(getContexto(), sql)) {
            Registro registro = lector.siguiente();
            while (registro != null) {
                final long id = (Long) registro.get(SecuenciadorRegistro.ID_COLUMN_NAME);
                list.add(id);
                registro = lector.siguiente();
            }
        }

        return list;
    }

    @Override
    protected LectorSQL getLectorDestino()
            throws SQLException {
        return new LectorSQL(getContexto(), armarSelectDestino());
    }

    @Override
    protected LectorSQL getLectorOrigen()
            throws SQLException {
        return new LectorSQL(getContexto(), armarSelectOrigen());
    }

}
