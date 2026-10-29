package cl.bcs.normativa.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * Rango de fechas para un esquema normativo.
 * Migrado desde legacy cl.bcs.normativa.esquema.RangoFechas.
 */
public class RangoFechas implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Date desde;
    private final Date hasta;

    public RangoFechas(Date desde, Date hasta) {
        this.desde = desde;
        this.hasta = hasta;
    }

    public Date getDesde() { return desde; }
    public Date getHasta() { return hasta; }

    @Override
    public String toString() {
        return "RangoFechas[desde=" + desde + ", hasta=" + hasta + "]";
    }
}
