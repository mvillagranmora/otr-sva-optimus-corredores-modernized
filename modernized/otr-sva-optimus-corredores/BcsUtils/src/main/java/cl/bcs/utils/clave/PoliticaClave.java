package cl.bcs.utils.clave;

import java.util.Map;

/**
 * RULE-031 / RULE-032 / RULE-033: the password policy values of ONE company (legacy
 * {@code ValidarNuevaClave.java:103-114}, which read them from PLA.Configuracion, always for IdEmpresa = 1). Which
 * company's values a caller passes is the caller's decision (RULE-033, open for an SME).
 *
 * <p>RULE-032 (approved deviation, 2026-10-16): {@code minDigitos} is a separate minimum-digit rule read from
 * {@code ACC.numDigitos}, default 1.
 */
public record PoliticaClave(long minLongitud, long minMayusculas, long minMinusculas, long minCaracteres, long minDigitos) {

    public static final String ACC_NUM_LONGITUD = "ACC.numLongitud";
    public static final String ACC_NUM_MAYUSCULAS = "ACC.numMayusculas";
    public static final String ACC_NUM_MINUSCULA = "ACC.numMinuscula";
    public static final String ACC_NUM_CARACTERES = "ACC.numCaracteres";
    public static final String ACC_NUM_DIGITOS = "ACC.numDigitos";
    public static final long MIN_DIGITOS_POR_DEFECTO = 1L;

    /**
     * From the raw PLA.Configuracion values (CodigoParametro -> Valor). Each value goes through {@link Long#parseLong},
     * so a missing, {@code null}, padded or non-numeric value throws {@link NumberFormatException}, as in the legacy.
     * An absent {@code ACC.numDigitos} means {@link #MIN_DIGITOS_POR_DEFECTO}.
     */
    public static PoliticaClave desdeConfiguracion(Map<String, String> valores) {
        return new PoliticaClave(
                Long.parseLong(valores.get(ACC_NUM_LONGITUD)),
                Long.parseLong(valores.get(ACC_NUM_MAYUSCULAS)),
                Long.parseLong(valores.get(ACC_NUM_MINUSCULA)),
                Long.parseLong(valores.get(ACC_NUM_CARACTERES)),
                valores.containsKey(ACC_NUM_DIGITOS) ? Long.parseLong(valores.get(ACC_NUM_DIGITOS)) : MIN_DIGITOS_POR_DEFECTO);
    }
}
