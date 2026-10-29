package cl.bcs.contabilidad.domain.service.soporte;

import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabEventoRef;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;

/**
 * Fair-value events of the forward portfolio types (legacy CentralizaCarterasForward.java:131-153,
 * CentralizaCarterasForwardVencimiento.java:131-151, CentralizaCarterasForwardValorizacion.java:154-173).
 *
 * <p>Per amount row, keyed by IdCartera and in the system currency: VALOR_RAZONABLE_ACTIVO, VALOR_RAZONABLE_PASIVO,
 * then VALOR_RAZONABLE_PERDIDA when the net value is negative or VALOR_RAZONABLE_UTILIDAD when positive (both with
 * the absolute net value; a zero net adds neither). The "anterior" variant appends _ANTERIOR / " Anterior".
 */
public final class ValorRazonable {

    private ValorRazonable() {}

    /** Which amount columns a row carries. */
    public enum Columnas {
        /** RazonableActivo / RazonablePasivo / RazonableNeto. */
        ACTUALES(""),
        /** RazonableActivoAnterior / RazonablePasivoAnterior / RazonableNetoAnterior. */
        ANTERIORES("Anterior");

        private final String sufijo;

        Columnas(String sufijo) {
            this.sufijo = sufijo;
        }
    }

    public static List<TabEventoRef> eventos(Long idTipo, Fila fila, String moneda, boolean anterior, Columnas columnas) {
        String cod = anterior ? "_ANTERIOR" : "";
        String nom = anterior ? " Anterior" : "";
        Long idCartera = fila.entero("IdCartera");
        BigDecimal neto = fila.decimal("RazonableNeto" + columnas.sufijo);
        if (neto == null) {
            // legacy BigDecimal.ZERO.compareTo(null) -> NullPointerException
            throw new DatoRequeridoException("VALOR_RAZONABLE" + cod, "RazonableNeto" + columnas.sufijo);
        }
        List<TabEventoRef> out = new ArrayList<>(3);
        out.add(new TabEventoRef(idTipo, idCartera, "VALOR_RAZONABLE_ACTIVO" + cod, "Valor a Razonable Activo" + nom,
                fila.decimal("RazonableActivo" + columnas.sufijo), moneda));
        out.add(new TabEventoRef(idTipo, idCartera, "VALOR_RAZONABLE_PASIVO" + cod, "Valor a Razonable Pasivo" + nom,
                fila.decimal("RazonablePasivo" + columnas.sufijo), moneda));
        if (neto.signum() < 0) {
            out.add(new TabEventoRef(idTipo, idCartera, "VALOR_RAZONABLE_PERDIDA" + cod, "Valor a Razonable Perdida" + nom,
                    neto.abs(MathContext.DECIMAL128), moneda));
        } else if (neto.signum() > 0) {
            out.add(new TabEventoRef(idTipo, idCartera, "VALOR_RAZONABLE_UTILIDAD" + cod, "Valor a Razonable Utilidad" + nom,
                    neto.abs(MathContext.DECIMAL128), moneda));
        }
        return out;
    }
}
