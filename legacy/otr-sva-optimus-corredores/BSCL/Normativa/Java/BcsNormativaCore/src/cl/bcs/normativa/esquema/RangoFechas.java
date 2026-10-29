package cl.bcs.normativa.esquema;

import java.io.Serializable;
import java.util.Date;
import java.util.Properties;

import cl.bcs.utils.DateUtil;
import cl.bcs.utils.PropertiesUtil;

/**
 * Rango de fechas para filtro de datos.
 *
 * @author fpino@email.com
 *
 */
public class RangoFechas implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Fecha desde la cual (inclusive) se obtienen los datos del documento.
     */
    private final Date fechaDesde;

    /**
     * Fecha hasta la cual (inclusive) se obtienen los datos del documento.
     */
    private final Date fechaHasta;

    /**
     * Constructor.
     *
     * @param fechaDesde
     *            La fecha desde.
     * @param fechaHasta
     *            La fecha hasta.
     */
    public RangoFechas(final Date fechaDesde, final Date fechaHasta) {
        if (fechaDesde != null && fechaHasta != null && fechaDesde.compareTo(fechaHasta) > 0) {
            throw new RangoFechasInvalidoException("Rango invalido de fechas");
        }

        this.fechaDesde = DateUtil.getStartOfDay(fechaDesde);
        this.fechaHasta = DateUtil.getEndOfDay(fechaHasta);
    }

    /**
     * Constructor a partir de {@link Properties}.
     *
     * @param props
     *            Las {@link Properties}.
     */
    public RangoFechas(final Properties props) {
        this(PropertiesUtil.getOptionalDate(props, "fechaDesde"),
                PropertiesUtil.getOptionalDate(props, "fechaHasta"));
    }

    /**
     * Obtiene la fecha desde (inclusive) la cual se obtienen los datos.
     *
     * @return La fecha desde (inclusive) la cual se obtienen los datos.
     */
    public Date getFechaDesde() {
        return fechaDesde;
    }

    /**
     * Obtiene la fecha hasta (inclusive) la cual se obtienen los datos.
     *
     * @return La fecha hasta (inclusive) la cual se obtienen los datos.
     */
    public Date getFechaHasta() {
        return fechaHasta;
    }

    @Override
    public String toString() {
        return "RangoFechas [fechaDesde=" + fechaDesde + ", fechaHasta=" + fechaHasta + "]";
    }

}
