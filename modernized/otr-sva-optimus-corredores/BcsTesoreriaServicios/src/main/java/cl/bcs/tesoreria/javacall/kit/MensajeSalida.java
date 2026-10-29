package cl.bcs.tesoreria.javacall.kit;

import java.util.List;

/**
 * Output message of the JavaCall contract (legacy {@code PlataformaUtils.MensajeSalida}); the field names are the contract.
 * {@code tipo} is one of {@link #MENSAJE_ERROR}, {@link #MENSAJE_ADVERTENCIA}, {@link #MENSAJE_AUTORIZACION}, {@link #MENSAJE_INFORMACION}, {@link #MENSAJE_OK}.
 */
public class MensajeSalida {

    public static final String MENSAJE_OK = "OK";
    public static final String MENSAJE_ERROR = "ERROR";
    public static final String MENSAJE_ADVERTENCIA = "ADVERTENCIA";
    public static final String MENSAJE_AUTORIZACION = "AUTORIZACION";
    public static final String MENSAJE_INFORMACION = "INFORMACION";

    public Long id;
    public String tipo;
    public String codigo;
    public String descripcion;

    public MensajeSalida() {
    }

    public MensajeSalida(Long id, String tipo, String codigo, String descripcion) {
        this.id = id;
        this.tipo = tipo;
        this.codigo = codigo;
        this.descripcion = descripcion;
    }

    /**
     * Number of ERROR messages. The legacy compared {@code tipo == MENSAJE_ERROR} by reference (true only for the interned literal);
     * messages that crossed the {@code Colaboradores} port are JSON-decoded, so the comparison is by value (the oracle interns them for the same reason).
     */
    public static long contarErrores(List<MensajeSalida> salida) {
        return salida.stream().filter(m -> MENSAJE_ERROR.equals(m.tipo)).count();
    }
}
