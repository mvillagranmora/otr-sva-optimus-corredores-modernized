package cl.bcs.tesoreria.javacall.comun;

import java.util.ArrayList;
import java.util.List;

import cl.bcs.utils.BcsPojo;

/**
 * Base of the JavaCall outputs that report warnings and errors as counters plus detail lists (legacy {@code SalidaServicios}); the field names are the
 * legacy JSON names.
 */
public abstract class SalidaServicios extends BcsPojo {

    public static class AdvertenciaDetalle extends BcsPojo {
        public String Codigo;
        public String RequiereAutorizacion;
        public String Descripcion;

        public AdvertenciaDetalle() {
        }

        public AdvertenciaDetalle(String codigo, String requiereAutorizacion, String descripcion) {
            this.Codigo = codigo;
            this.RequiereAutorizacion = requiereAutorizacion;
            this.Descripcion = descripcion;
        }
    }

    public static class ErrorDetalle extends BcsPojo {
        public String Codigo;
        public String Descripcion;

        public ErrorDetalle() {
        }

        public ErrorDetalle(String codigo, String descripcion) {
            this.Codigo = codigo;
            this.Descripcion = descripcion;
        }
    }

    public Long P_OUT_ADVERTENCIASCONTADOR = 0L;
    public List<AdvertenciaDetalle> P_OUT_ADVERTENCIASDETALLE = new ArrayList<>();
    public Long P_OUT_ERRORESCONTADOR = 0L;
    public List<ErrorDetalle> P_OUT_ERRORESDETALLE = new ArrayList<>();

    public void addAdvertencia(String codigo, String descripcion) {
        P_OUT_ADVERTENCIASCONTADOR++;
        P_OUT_ADVERTENCIASDETALLE.add(new AdvertenciaDetalle(codigo, "N", descripcion));
    }

    public void addError(String codigo, String descripcion) {
        P_OUT_ERRORESCONTADOR++;
        P_OUT_ERRORESDETALLE.add(new ErrorDetalle(codigo, descripcion));
    }

    /** Errors go to the error list, every other message type to the warning list. */
    public void parseMensajeSalida(List<MensajeSalida> mensajes) {
        mensajes.forEach(m -> {
            if (MensajeSalida.MENSAJE_ERROR.equals(m.tipo())) {
                addError(m.codigo(), m.descripcion());
            } else {
                addAdvertencia(m.codigo(), m.descripcion());
            }
        });
    }
}
