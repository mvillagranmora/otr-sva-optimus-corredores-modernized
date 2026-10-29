package cl.bcs.contabilidad.domain.service.soporte;

import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabDatoMovimientoRef;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds {@code P_TABDATOSMOVIMIENTO}: one entry per {@link Campo} per data row, in field order, keyed by the row's
 * IdMovimiento (legacy {@code for(mov : tabDatosMovimientoPaso) out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(...))}).
 */
public final class DatosMovimiento {

    private DatosMovimiento() {}

    public static List<TabDatoMovimientoRef> de(Long idTipoCentralizacion, List<Fila> filas, List<Campo> campos) {
        List<TabDatoMovimientoRef> out = new ArrayList<>(filas.size() * campos.size());
        for (Fila fila : filas) {
            out.addAll(de(idTipoCentralizacion, fila.entero("IdMovimiento"), fila, campos));
        }
        return out;
    }

    /** The entries of one row for an explicit movement id (types that build their movements themselves). */
    public static List<TabDatoMovimientoRef> de(Long idTipoCentralizacion, Long idMovimiento, Fila fila, List<Campo> campos) {
        List<TabDatoMovimientoRef> out = new ArrayList<>(campos.size());
        for (Campo campo : campos) {
            out.add(dato(idTipoCentralizacion, idMovimiento, campo.tipoDato(),
                    campo.texto().apply(fila), campo.decimal().apply(fila)));
        }
        return out;
    }

    /** Legacy TabDatosMovimiento constructor: null ValorVarChar -> "", null ValorDecimal -> ZERO. */
    public static TabDatoMovimientoRef dato(Long idTipo, Long idMovimiento, String tipoDato, String texto, BigDecimal decimal) {
        return new TabDatoMovimientoRef(idTipo, idMovimiento, tipoDato,
                texto == null ? "" : texto, decimal == null ? BigDecimal.ZERO : decimal);
    }
}
