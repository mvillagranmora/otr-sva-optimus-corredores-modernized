package cl.bcs.cuentainversion.domain;

public record MensajeSalida(String tipo, String codigo, String descripcion) {
    public static final String ERROR = "ERROR";
    public static final String ADVERTENCIA = "ADVERTENCIA";
    public static final String INFORMACION = "INFORMACION";
}
