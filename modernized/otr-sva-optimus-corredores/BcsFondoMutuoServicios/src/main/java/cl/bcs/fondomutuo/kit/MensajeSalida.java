package cl.bcs.fondomutuo.kit;

import java.util.Map;

import cl.bcs.fondomutuo.port.out.kit.DiccionarioMensajesPort;

/**
 * Legacy {@code PlataformaUtils.MensajeSalida} (the message element of every {@code P_OUT_MENSAJES}): same public field names and order, same
 * constants, so its JSON is the legacy one ({@code id}, {@code tipo}, {@code codigo}, {@code descripcion}; null keys omitted by the plain Gson).
 */
public class MensajeSalida {

    public Long id;
    public String tipo;         // ERROR, AUTORIZACION, ADVERTENCIA, INFORMACION
    public String codigo;
    public String descripcion;

    public static final String MENSAJE_OK = "OK";
    public static final String MENSAJE_ERROR = "ERROR";
    public static final String MENSAJE_ADVERTENCIA = "ADVERTENCIA";
    public static final String MENSAJE_AUTORIZACION = "AUTORIZACION";
    public static final String MENSAJE_INFORMACION = "INFORMACION";
    public static final Object MENSAJE_RECHAZO = "RECHAZO";

    public MensajeSalida() {
    }

    public MensajeSalida(Long id, String tipo, String codigo, String descripcion) {
        this.id = id;
        this.tipo = tipo;
        this.codigo = codigo;
        this.descripcion = descripcion;
    }

    /** Legacy constructor {@code (id, tipo, codigo, valores, con, idEmpresa)}: the text comes from the dictionary of the company. */
    public static MensajeSalida conTexto(Long id, String tipo, String codigo, Map<String, String> valores, DiccionarioMensajesPort diccionario,
            Long idEmpresa) {
        return new MensajeSalida(id, tipo, codigo, diccionario.mensaje(idEmpresa, codigo, valores));
    }

    @Override
    public String toString() {
        return JsonLegacy.conNulos().toJson(this);
    }
}
