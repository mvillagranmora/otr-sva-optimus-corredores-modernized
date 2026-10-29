package cl.bcs.normativa.circulares.ReportesBCC.Reportes2p2.utils;
 
import static cl.bcs.normativa.circulares.ReportesBCC.Reportes2p2.utils.ReporteUtils.MAX_NUM10_5;
import static cl.bcs.normativa.circulares.ReportesBCC.Reportes2p2.utils.ReporteUtils.MAX_NUM20_2;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.ResolverStyle;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
 
/**
* Utilidades compartidas para Reportes 2.2 (y afines).
* Centraliza validaciones de RUT, moneda, fechas y numericos.
*/
public final class ReporteUtils {
 
    private ReporteUtils() {}
 
    /**
     * Valida que el dato ingresado es nulo o vacio (luego de hacer trim).
     * @param s Cadena a validar.
     * @return true si es nulo o vacio.
     */
    public static boolean isNullOrBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
 
    /**
     * Normaliza una cadena: si es nula, retorna nulo; si no, le hace trim y la pasa a mayusculas.
     * @param s Cadena a normalizar.
     * @return Cadena normalizada o nulo.
     */
    public static String norm(String s) {
        return (s == null) ? null : s.trim().toUpperCase(Locale.ROOT);
    }
 
    /**
     * Valida si es nulo o vacio, y si no lo es, le hace trim.
     * @param s Cadena a validar y trim.
     * @return null si es nulo o vacio, o la cadena con trim si no lo es.
     */
    public static String trimToNull(String s) {
        return isNullOrBlank(s) ? null : s.trim();
    }
 
    /**
     * Valida si es nulo o vacio, y si no lo es, le hace trim y lo pasa a mayusculas.
     * @param s Cadena a validar, trim y pasar a mayusculas.
     * @return null si es nulo o vacio, o la cadena con trim y en mayusculas si no lo es.
     */
    public static String upperOrNull(String s) {
        String t = trimToNull(s);
        return (t == null) ? null : t.toUpperCase(Locale.ROOT);
    }
 
    public static final Set<String> ISO4217;
    static {
        ISO4217 = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "AED","AFN","ALL","AMD","ANG","AOA","ARS","AUD","AWG","AZN","BAM","BBD","BDT","BGN","BHD","BIF","BMD","BND","BOB","BOV",
            "BRL","BSD","BTN","BWP","BYN","BZD","CAD","CDF","CHE","CHF","CHW","CLF","CLP","CNY","COP","COU","CRC","CUP","CVE","CZK",
            "DJF","DKK","DOP","DZD","EGP","ERN","ETB","EUR","FJD","FKP","GBP","GEL","GHS","GIP","GMD","GNF","GTQ","GYD","HKD","HNL",
            "HTG","HUF","IDR","ILS","INR","IQD","IRR","ISK","JMD","JOD","JPY","KES","KGS","KHR","KMF","KPW","KRW","KWD","KYD","KZT",
            "LAK","LBP","LKR","LRD","LSL","LYD","MAD","MDL","MGA","MKD","MMK","MNT","MOP","MRU","MUR","MVR","MWK","MXN","MXV","MYR",
            "MZN","NAD","NGN","NIO","NOK","NPR","NZD","OMR","PAB","PEN","PGK","PHP","PKR","PLN","PYG","QAR","RON","RSD","RUB","RWF",
            "SAR","SBD","SCR","SDG","SEK","SGD","SHP","SLE","SOS","SRD","SSP","STN","SVC","SYP","SZL","THB","TJS","TMT","TND","TOP",
            "TRY","TTD","TWD","TZS","UAH","UGX","USD","USN","UYI","UYU","UYW","UZS","VES","VND","VUV","WST","XAF","XAG","XAU","XBA",
            "XBB","XBC","XBD","XCD","XDR","XOF","XPD","XPF","XPT","XSU","XTS","XUA","XXX","YER","ZAR","ZMW","ZWG"
        )));
    }
 
    /**
     * Valida si la cadena es una moneda ISO 4217 valida.
     * @param s Cadena a validar.
     * @return true si es una moneda ISO 4217 valida.
     */
    public static boolean isIso4217(String s) {
        String v = norm(s);
        return v != null && v.length() == 3 && ISO4217.contains(v);
    }
 
    /**
     * Valida si el RUT ingresado es valido en formato "12345678-9" o "12345678-K".
     * @param rut RUT a validar.
     * @return true si es un RUT valido en formato correcto.
     */
    public static boolean validacionRut(String rut) {
        if (isNullOrBlank(rut)) return false;
        String soloDigitos = rut.replaceAll("\\D", "");
        if (soloDigitos.length() > 11) return false;
        String[] partes = rut.split("-");
        if (partes.length != 2) return false;
        String numero = partes[0];
        String dv = partes[1].toUpperCase(Locale.ROOT);
        return numero.matches("\\d+") && dv.matches("[0-9K]");
    }
 
    public static final BigDecimal MAX_NUM20_2 = new BigDecimal("999999999999999999.99");
    public static final BigDecimal MIN_NUM20_2 = MAX_NUM20_2.negate();
    public static final BigDecimal MAX_NUM10_5 = new BigDecimal("99999.99999");
 
    /**
     * Valida si el BigDecimal es un NUM(20,2).
     * @param v Valor a validar.
     * @return true si es un NUM(20,2).
     */
    public static boolean isNum20_2(BigDecimal v) {
        if (v == null) return false;
        if (v.scale() > 2) return false;
        return v.compareTo(MIN_NUM20_2) >= 0
            && v.compareTo(MAX_NUM20_2) <= 0;
    }
 
    /**
     * Valida si el BigDecimal es un NUM(10,5) positivo.
     * @param v Valor a validar.
     * @return true si es un NUM(10,5) positivo.
     */
    public static boolean isNum10_5Pos(BigDecimal v) {
        if (v == null) return false;
        BigDecimal x = v.stripTrailingZeros();
        return x.signum() > 0 && x.scale() <= 5 && x.compareTo(MAX_NUM10_5) <= 0;
    }
    
}