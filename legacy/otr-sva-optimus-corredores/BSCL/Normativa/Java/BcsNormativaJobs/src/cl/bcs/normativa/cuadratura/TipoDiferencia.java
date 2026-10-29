package cl.bcs.normativa.cuadratura;

/**
 * Tipos de diferencias resultado de la cuadratura.
 *
 * @author fpino@email.com
 *
 */
public enum TipoDiferencia {

    /**
     * No hay diferencias en la cuadratura.
     */
    CUADRADO,

    /**
     * Registros diferentes.
     */
    DIFERENTE,

    /**
     * Registro faltante.
     */
    FALTANTE,

    /**
     * No hay datos para comparar.
     */
    SIN_DATOS,

    /**
     * Registro sobrante.
     */
    SOBRANTE

}
