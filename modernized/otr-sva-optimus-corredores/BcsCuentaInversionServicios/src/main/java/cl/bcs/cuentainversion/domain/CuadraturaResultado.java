package cl.bcs.cuentainversion.domain;

public record CuadraturaResultado(String codigoResultado, String descripcion) {
    public static final String OK = "OK";
    public static final String DESCUADRE = "DESCUADRE";
    public static final String SIN_MOVIMIENTOS = "SIN_MOVIMIENTOS";
}
