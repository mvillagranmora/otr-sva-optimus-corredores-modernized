package cl.bcs.apv.application.recaudacion;

import java.util.ArrayList;
import java.util.List;

import cl.bcs.apv.api.comun.Mensaje;

/**
 * Mutable message of the legacy logic ({@code PlataformaUtils.MensajeSalida}): the legacy shares ONE instance between several list entries and mutates it
 * afterwards, which only a mutable object reproduces. Converted to the typed {@link Mensaje} record when the outcome is built.
 */
final class MensajeLegacy {

    static final String OK = "OK";
    static final String ERROR = "ERROR";
    static final String ADVERTENCIA = "ADVERTENCIA";
    static final String AUTORIZACION = "AUTORIZACION";
    static final String INFORMACION = "INFORMACION";

    Long id;
    String tipo;
    String codigo;
    String descripcion;

    MensajeLegacy() {
    }

    MensajeLegacy(Long id, String tipo, String codigo, String descripcion) {
        this.id = id;
        this.tipo = tipo;
        this.codigo = codigo;
        this.descripcion = descripcion;
    }

    static MensajeLegacy de(Mensaje m) {
        return new MensajeLegacy(m.id(), m.tipo(), m.codigo(), m.descripcion());
    }

    Mensaje aMensaje() {
        return new Mensaje(id, tipo, codigo, descripcion);
    }

    /**
     * Legacy {@code MensajeSalida.getNumeroErrores}: counts {@code tipo == MENSAJE_ERROR} BY REFERENCE. Messages that crossed the
     * {@code Colaboradores} port are JSON-decoded (a new String), the oracle interns them; here the comparison is by value for both.
     */
    static long contarErrores(List<MensajeLegacy> salida) {
        long c = 0;
        for (MensajeLegacy s : salida) {
            if (ERROR.equals(s.tipo)) {
                c++;
            }
        }
        return c;
    }

    static List<Mensaje> aMensajes(List<MensajeLegacy> l) {
        List<Mensaje> r = new ArrayList<>();
        for (MensajeLegacy m : l) {
            r.add(m == null ? null : m.aMensaje());
        }
        return r;
    }
}
