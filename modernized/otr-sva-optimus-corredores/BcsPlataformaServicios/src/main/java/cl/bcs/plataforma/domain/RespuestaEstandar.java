package cl.bcs.plataforma.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Standard response envelope used across all platform REST endpoints.
 */
public class RespuestaEstandar<T> {

    public Long             erroresContador = 0L;
    public List<MensajeSalida> mensajes     = new ArrayList<>();
    public T                datos;

    public RespuestaEstandar() {}

    public RespuestaEstandar(T datos) {
        this.datos = datos;
    }

    public static <T> RespuestaEstandar<T> ok(T datos) {
        return new RespuestaEstandar<>(datos);
    }

    public static <T> RespuestaEstandar<T> error(String codigo, String descripcion) {
        RespuestaEstandar<T> r = new RespuestaEstandar<>();
        r.erroresContador = 1L;
        r.mensajes.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, codigo, descripcion));
        return r;
    }
}
