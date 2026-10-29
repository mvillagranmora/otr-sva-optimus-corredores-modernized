package cl.bcs.apv.application.traspasos;

import java.util.ArrayList;
import java.util.List;

import cl.bcs.apv.api.comun.Mensaje;

/**
 * Mutable message of the legacy logic ({@code PlataformaUtils.MensajeSalida}): the legacy shares ONE instance between several list entries and mutates it
 * afterwards, which only a mutable object reproduces. Converted to the typed {@link Mensaje} record when the outcome is built.
 */
public final class MensajeLegacy {

    public static final String OK = "OK";
    public static final String ERROR = "ERROR";
    public static final String ADVERTENCIA = "ADVERTENCIA";
    public static final String AUTORIZACION = "AUTORIZACION";
    public static final String INFORMACION = "INFORMACION";

    public Long id;
    public String tipo;
    public String codigo;
    public String descripcion;

    public MensajeLegacy() {
    }

    public MensajeLegacy(Long id, String tipo, String codigo, String descripcion) {
        this.id = id;
        this.tipo = tipo;
        this.codigo = codigo;
        this.descripcion = descripcion;
    }

    public static MensajeLegacy de(Mensaje m) {
        return new MensajeLegacy(m.id(), m.tipo(), m.codigo(), m.descripcion());
    }

    Mensaje aMensaje() {
        return new Mensaje(id, tipo, codigo, descripcion);
    }

    /**
     * Legacy {@code MensajeSalida.getNumeroErrores}: counts {@code tipo == MENSAJE_ERROR} BY REFERENCE. Messages that crossed the
     * {@code Colaboradores} port are JSON-decoded (a new String), the oracle interns them; here the comparison is by value for both.
     */
    public static long contarErrores(List<MensajeLegacy> salida) {
        long c = 0;
        for (MensajeLegacy s : salida) {
            if (ERROR.equals(s.tipo)) {
                c++;
            }
        }
        return c;
    }

    public static List<Mensaje> aMensajes(List<MensajeLegacy> l) {
        List<Mensaje> r = new ArrayList<>();
        for (MensajeLegacy m : l) {
            r.add(m == null ? null : m.aMensaje());
        }
        return r;
    }
}
