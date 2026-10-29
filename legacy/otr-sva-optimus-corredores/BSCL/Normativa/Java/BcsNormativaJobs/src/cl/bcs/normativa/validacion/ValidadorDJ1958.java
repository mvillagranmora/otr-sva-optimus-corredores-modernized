package cl.bcs.normativa.validacion;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.math.BigDecimal;
import java.time.format.DateTimeParseException;

import cl.bcs.normativa.data.Registro;

/**
 * Implementación del validador de la DJ1958.
 *
 * 
 *
 */
public class ValidadorDJ1958 extends Validador {

    private static final List<String> PAIS = Arrays.asList("AF", "AL", "AD", "AO", "AI", "AQ", "AG", "AN", "SA", "DZ",
            "AR", "AM", "AW", "AU", "AT", "AZ", "BS", "BH",
            "BD", "BB", "BY", "BE", "BZ", "BJ", "BM", "BT", "BO", "BA", "BW", "BR", "BN", "BG", "BF", "BI", "CV", "KH",
            "CM", "CA", "TD", "CL", "CN", "CY", "CO", "KM", "CG", "CI", "CR", "HR", "CU", "DK", "DJ", "DM", "EC", "EG",
            "SV", "AE", "ER", "SI", "ES", "VA", "US", "EE", "ET", "RU", "FJ", "PH", "FI", "FR", "FX", "GA", "GM", "GE",
            "GS", "GH", "GI", "GD", "GR", "GL", "GP", "GU", "GT", "GF", "GQ", "GY", "HT", "HN", "HK", "HU", "IN", "ID",
            "IR", "IQ", "IE", "BV", "CX", "NU", "NF", "PN", "IS", "KY", "CC", "CK", "FO", "HM", "FK", "MP", "MH", "UM",
            "SB", "SJ", "TC", "VG", "VI", "WF", "IL", "IT", "LY", "JM", "JP", "JO", "KZ", "KE", "KG", "KI", "KW", "LS",
            "LV", "LB", "LR", "LI", "LT", "LU", "MO", "MG", "MY", "MW", "MV", "ML", "MT", "MA", "MQ", "MU", "MR", "YT",
            "MX", "FM", "MD", "MC", "MN", "MS", "MZ", "MM", "NA", "NR", "NP", "NI", "NE", "NG", "NO", "NC", "NZ", "OM",
            "NL", "PK", "PW", "PA", "PG", "PY", "PE", "PF", "PL", "PT", "PR", "QA", "GB", "SY", "CF", "CZ", "KR", "ZR",
            "LA", "DO", "SK", "DE", "KP", "TZ", "RE", "RO", "RW", "EH", "KN", "WS", "AS", "SM", "PM", "VC", "SH", "LC",
            "ST", "SN", "SC", "SL", "SG", "SO", "LK", "ZA", "SD", "SE", "CH", "SR", "SZ", "TH", "TW", "TJ", "IO", "TF",
            "TP", "TG", "TK", "TO", "TT", "TN", "TM", "TR", "TV", "UA", "UG", "GN", "GW", "UY", "UZ", "VU", "VE", "VN",
            "YE", "YU", "ZM", "ZW");

    private static final List<String> TIPO_INSTITUCION = Arrays.asList("1", "2", "3", "4");

    private static final List<String> CUENTAS_FINANCIERAS = Arrays.asList("1", "2");

    private static final List<String> TIPO_PERSONAS_JUR = Arrays.asList("CRS101", "CRS102", "CRS103");

    private static final List<String> TIPO_DOMICILIO = Arrays.asList("CRS301", "CRS302", "CRS303", "CRS304", "CRS305");

    private static final List<String> TIPO_PAGO = Arrays.asList("CRS501", "CRS502", "CRS503", "CRS504", "CRS505");

    private int LimiteRutH3291;

    private static final List<String> MONEDA = Arrays.asList(
            "AED", "AFN", "ALL", "AMD", "ANG", "AOA", "ARS", "AUD", "AWG", "AZN",
            "BAM", "BBD", "BDT", "BGN", "BHD", "BIF", "BMD", "BND", "BOB", "BOV",
            "BRL", "BSD", "BTN", "BWP", "BYR", "BZD", "CAD", "CDF", "CHE", "CHF",
            "CHW", "CLF", "CLP", "CNY", "COP", "COU", "CRC", "CUC", "CUP", "CVE",
            "CZK", "DJF", "DKK", "DOP", "DZD", "EGP", "ERN", "ETB", "EUR", "FJD",
            "FKP", "GBP", "GEL", "GHS", "GIP", "GMD", "GNF", "GTQ", "GYD", "HKD",
            "HNL", "HRK", "HTG", "HUF", "IDR", "ILS", "INR", "IQD", "IRR", "ISK",
            "JMD", "JOD", "JPY", "KES", "KGS", "KHR", "KMF", "KPW", "KRW", "KWD",
            "KYD", "KZT", "LAK", "LBP", "LKR", "LRD", "LSL", "LYD", "MAD", "MDL",
            "MGA", "MKD", "MMK", "MNT", "MOP", "MRO", "MUR", "MVR", "MWK", "MXN",
            "MXV", "MYR", "MZN", "NAD", "NGN", "NIO", "NOK", "NPR", "NZD", "OMR",
            "PAB", "PEN", "PGK", "PHP", "PKR", "PLN", "PYG", "QAR", "RON", "RSD",
            "RUB", "RWF", "SAR", "SBD", "SCR", "SDG", "SEK", "SGD", "SHP", "SLL",
            "SOS", "SRD", "STD", "SSP", "SVC", "SYP", "SZL", "THB", "TJS", "TMT",
            "TND", "TOP", "TRY", "TTD", "TWD", "TZS", "UAH", "UGX", "USD", "USN",
            "UYI", "UYU", "UZS", "VEF", "VND", "VUV", "WST", "XAF", "XCD", "XDR",
            "XOF", "XPF", "XSU", "XUA", "YER", "ZAR", "ZMW", "ZWL");

    private final String version;

    /**
     * Constructor.
     *
     * @param contexto
     *                 El contexto de validación.
     * @throws SQLException
     *                      En caso de error SQL.
     */
    public ValidadorDJ1958(final ContextoValidacion contexto) throws SQLException {
        super(contexto);
        version = contexto.getEsquema().getVersion();
        LimiteRutH3291=(int)contexto.getParametroEsquemaAsDouble("LimiteRutH3291");
    }

    /**
     * Implementacion de la regla de validacion H3116.
     *
     * @param registro
     *                 El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH3116(final Registro registro) {
        final String c1 = String.valueOf(registro.getPorCodigoSII("C1"));

        if (c1 == null || c1.trim().isEmpty() || "RFE".equalsIgnoreCase(c1))
            return false;

        return PAIS.contains(c1.trim().toUpperCase());
    }

    /**
     * Implementacion de la regla de validacion H3117.
     *
     * @param registro
     *                 El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH3117(final Registro registro) {
        final String c2 = String.valueOf(registro.getPorCodigoSII("C2"));

        return TIPO_INSTITUCION.contains(c2.trim());
    }

    /**
     * Implementacion de la regla de validacion H3118.
     *
     * @param registro
     *                 El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH3118(final Registro registro) {
        final String c3 = String.valueOf(registro.getPorCodigoSII("C3"));

        return CUENTAS_FINANCIERAS.contains(c3.trim());
    }

    /**
     * Implementacion de la regla de validacion H3119.
     *
     * @param registro
     *                 El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH3119(final Registro registro) {
        final String c3 = String.valueOf(registro.getPorCodigoSII("C3"));

        if (!"2".equals(c3)) {
            return true;
        }

        final String c4 = String.valueOf(registro.getPorCodigoSII("C4"));
        final String c5 = String.valueOf(registro.getPorCodigoSII("C5"));
        final String c6 = String.valueOf(registro.getPorCodigoSII("C6"));
        final String c7 = String.valueOf(registro.getPorCodigoSII("C7"));
        final String c8 = String.valueOf(registro.getPorCodigoSII("C8"));
        final String c9 = String.valueOf(registro.getPorCodigoSII("C9"));
        final String c10 = String.valueOf(registro.getPorCodigoSII("C10"));
        final String c11 = String.valueOf(registro.getPorCodigoSII("C11"));
        final String c12 = String.valueOf(registro.getPorCodigoSII("C12"));
        final String c13 = String.valueOf(registro.getPorCodigoSII("C13"));
        final String c14 = String.valueOf(registro.getPorCodigoSII("C14"));
        final String c15 = String.valueOf(registro.getPorCodigoSII("C15"));
        final String c16 = String.valueOf(registro.getPorCodigoSII("C16"));
        final String c20 = String.valueOf(registro.getPorCodigoSII("C20"));
        final String c22 = String.valueOf(registro.getPorCodigoSII("C22"));
        final String c23 = String.valueOf(registro.getPorCodigoSII("C23"));
        final String c24 = String.valueOf(registro.getPorCodigoSII("C24"));
        final String c25 = String.valueOf(registro.getPorCodigoSII("C25"));
        final String c26 = String.valueOf(registro.getPorCodigoSII("C26"));
        final String c27 = String.valueOf(registro.getPorCodigoSII("C27"));
        final String c28 = String.valueOf(registro.getPorCodigoSII("C28"));
        final String c29 = String.valueOf(registro.getPorCodigoSII("C29"));
        final String c30 = String.valueOf(registro.getPorCodigoSII("C30"));
        final String c31 = String.valueOf(registro.getPorCodigoSII("C31"));
        final String c32 = String.valueOf(registro.getPorCodigoSII("C32"));

        final Number raw17 = (Number) registro.getPorCodigoSII("C17");
        final long c17 = (raw17 == null ? 0L : raw17.longValue());
        final Number raw18 = (Number) registro.getPorCodigoSII("C18");
        final long c18 = (raw18 == null ? 0L : raw18.longValue());
        final Number raw19 = (Number) registro.getPorCodigoSII("C19");
        final long c19 = (raw19 == null ? 0L : raw19.longValue());
        final Object raw21 = registro.getPorCodigoSII("C21");
        final BigDecimal c21 = (raw21 == null || raw21.toString().trim().isEmpty())
            ? BigDecimal.ZERO
            : new BigDecimal(raw21.toString().trim());
        final Object raw33 = registro.getPorCodigoSII("C33");
        final BigDecimal c33 = (raw33 == null || raw33.toString().trim().isEmpty())
            ? BigDecimal.ZERO
            : new BigDecimal(raw33.toString().trim());

        return 
                c17 == 0L &&
                c18 == 0L &&
                c19 == 0L &&
                c21.compareTo(BigDecimal.ZERO) == 0 &&
                c33.compareTo(BigDecimal.ZERO) == 0 &&
                (c4 == null || c4.trim().isEmpty()) &&
                (c5 == null || c5.trim().isEmpty()) &&
                (c6 == null || c6.trim().isEmpty()) &&
                (c7 == null || c7.trim().isEmpty()) &&
                (c8 == null || c8.trim().isEmpty()) &&
                (c9 == null || c9.trim().isEmpty()) &&
                (c10 == null || c10.trim().isEmpty()) &&
                (c11 == null || c11.trim().isEmpty()) &&
                (c12 == null || c12.trim().isEmpty()) &&
                (c13 == null || c13.trim().isEmpty()) &&
                (c14 == null || c14.trim().isEmpty()) &&
                (c15 == null || c15.trim().isEmpty()) &&
                (c16 == null || c16.trim().isEmpty()) &&
                (c20 == null || c20.trim().isEmpty()) &&
                (c22 == null || c22.trim().isEmpty()) &&
                (c23 == null || c23.trim().isEmpty()) &&
                (c24 == null || c24.trim().isEmpty()) &&
                (c25 == null || c25.trim().isEmpty()) &&
                (c26 == null || c26.trim().isEmpty()) &&
                (c27 == null || c27.trim().isEmpty()) &&
                (c28 == null || c28.trim().isEmpty()) &&
                (c29 == null || c29.trim().isEmpty()) &&
                (c30 == null || c30.trim().isEmpty()) &&
                (c31 == null || c31.trim().isEmpty()) &&
                (c32 == null || c32.trim().isEmpty());
    }

    /**
     * Implementacion de la regla de validacion H3120.
     *
     * @param registro
     *                 El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH3120(final Registro registro) {
        final String c4 = String.valueOf(registro.getPorCodigoSII("C4"));

        if (c4 == null || c4.trim().isEmpty()) {
            return false;
        }

        final String regex = "^\\d{4}-\\d{2}-\\d{2}$";
        if (!c4.matches(regex)) {
            return false;
        }
        return true;
    }

    /**
     * Implementacion de la regla de validacion H3121.
     *
     * @param registro
     *                 El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
     public boolean validarH3121(final Registro registro) {

        final Object rawC5 = registro.getPorCodigoSII("C5");
        final String c5 = (rawC5 == null)
                ? ""
                : rawC5.toString().trim().toUpperCase();

        if (c5.isEmpty()
                || "RFE".equals(c5)
                || !PAIS.contains(c5)) {
            return false;
        }

        return true;
    }

    /**
     * Implementacion de la regla de validacion H3122.
     *
     * @param registro
     *                 El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH3122(final Registro registro) {
        final String c6 = String.valueOf(registro.getPorCodigoSII("C6"));

        if (c6 == null || c6.trim().isEmpty())
            return false;

        return CUENTAS_FINANCIERAS.contains(c6.trim());
    }

    /**
     * Implementacion de la regla de validacion H3123.
     *
     * @param registro
     *                 El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH3123(final Registro registro) {
        final String c6 = String.valueOf(registro.getPorCodigoSII("C6"));
        final String c7 = String.valueOf(registro.getPorCodigoSII("C7"));

        if ("2".equals(c6)) {
            if (c7 == null || c7.trim().isEmpty())
                return false;

            return TIPO_PERSONAS_JUR.contains(c7.trim().toUpperCase());
        }

        return true;
    }

    /**
     * Implementacion de la regla de validacion H3124
     *
     * @param registro
     *                 El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH3124(final Registro registro) {
        final String c6 = String.valueOf(registro.getPorCodigoSII("C6"));
        final String c8 = String.valueOf(registro.getPorCodigoSII("C8"));

        if ("1".equals(c6)) {
            if (c8 == null || c8.trim().isEmpty())
                return false;
        }

        return true;
    }

    /**
     * Implementacion de la regla de validacion H3125
     *
     * @param registro
     *                 El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH3125(final Registro registro) {
        final String c6 = String.valueOf(registro.getPorCodigoSII("C6"));
        final String c10 = String.valueOf(registro.getPorCodigoSII("C10"));

        if ("2".equals(c6)) {
            if (c10 == null || c10.trim().isEmpty())
                return false;
        }

        return true;
    }

    /**
     * Implementacion de la regla de validacion H3126
     *
     * @param registro
     *                 El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH3126(final Registro registro) {
        final String c11 = String.valueOf(registro.getPorCodigoSII("C11"));

        if (c11 == null || c11.trim().isEmpty() || "RFE".equalsIgnoreCase(c11))
            return false;

        return PAIS.contains(c11.trim().toUpperCase());
    }

    /**
     * Implementacion de la regla de validacion H3127
     *
     * @param registro
     *                 El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH3127(final Registro registro) {
        final String c13 = String.valueOf(registro.getPorCodigoSII("C13"));

        if (c13 == null || c13.trim().isEmpty() || "RFE".equalsIgnoreCase(c13))
            return false;

        return PAIS.contains(c13.trim().toUpperCase());
    }

    /**
     * Implementacion de la regla de validacion H3128
     *
     * @param registro
     *                 El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH3128(final Registro registro) {
        final String c14 = String.valueOf(registro.getPorCodigoSII("C14"));

        return TIPO_DOMICILIO.contains(c14);
    }

    /**
     * Implementacion de la regla de validacion H3129
     *
     * @param registro
     *                 El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH3129(final Registro registro) {
        final String c15 = String.valueOf(registro.getPorCodigoSII("C15"));

        if (c15 == null || c15.trim().isEmpty())
            return false;

        return true;
    }

    /**
     * Implementacion de la regla de validacion H3130
     *
     * @param registro
     *                 El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH3130(final Registro registro) {
        final String c6 = String.valueOf(registro.getPorCodigoSII("C6"));
        final String c16 = String.valueOf(registro.getPorCodigoSII("C16"));

        if ("1".equals(c6)) {
            if (c16 == null || c16.trim().isEmpty())
                return false;
            final String regex = "^\\d{4}-\\d{2}-\\d{2}$";
            return c16.matches(regex);
        }
        return true;
    }

    /**
     * Implementacion de la regla de validacion H3131
     *
     * @param registro
     *                 El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH3131(final Registro registro) {
        final Object valor = registro.getPorCodigoSII("C17");

        if (valor == null)
            return false;

        final long c17 = ((Number) valor).longValue();
        return c17 != 0;
    }

    /**
     * Implementacion de la regla de validacion H3132
     *
     * @param registro
     *                 El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH3132(final Registro registro) {
        final String c18 = String.valueOf(registro.getPorCodigoSII("C18"));

        return CUENTAS_FINANCIERAS.contains(c18.trim());
    }

    /**
     * Implementacion de la regla de validacion H3133
     *
     * @param registro
     *                 El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH3133(final Registro registro) {
        final String c19 = String.valueOf(registro.getPorCodigoSII("C19"));

        return CUENTAS_FINANCIERAS.contains(c19.trim());
    }

    /**
     * Implementacion de la regla de validacion H3134
     *
     * @param registro
     *                 El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH3134(final Registro registro) {
        final String c20 = String.valueOf(registro.getPorCodigoSII("C20"));

        return MONEDA.contains(c20.trim().toUpperCase());
    }

    /**
     * Implementacion de la regla de validacion H3135
     *
     * @param registro
     *                 El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH3135(final Registro registro) {
        final Object valor = registro.getPorCodigoSII("C21");

        if (valor == null)
            return false;

        final long c21 = ((Number) valor).longValue();
        return c21 >= 0;
    }

    /**
     * Implementacion de la regla de validacion H3136
     *
     * @param registro
     *                 El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH3136(final Registro registro) {
        final String c7 = String.valueOf(registro.getPorCodigoSII("C7"));
        final String c22 = String.valueOf(registro.getPorCodigoSII("C22"));

        if ("CRS101".equals(c7.trim().toUpperCase())) {
            if (c22 == null || c22.trim().isEmpty() || "RFE".equalsIgnoreCase(c22))
                return false;
            return PAIS.contains(c22.trim().toUpperCase());
        }

        return true;
    }

    /**
     * Implementacion de la regla de validacion H3137
     *
     * @param registro
     *                 El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH3137(final Registro registro) {
        final String c7 = String.valueOf(registro.getPorCodigoSII("C7"));
        final String c23 = String.valueOf(registro.getPorCodigoSII("C23"));

        if ("CRS101".equals(c7.trim().toUpperCase())) {
            return c23 != null && !c23.trim().isEmpty() && !"0".equals(c23.trim());
        }

        return true;
    }

    /**
     * Implementacion de la regla de validacion H3138
     *
     * @param registro
     *                 El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH3138(final Registro registro) {
        final String c7 = String.valueOf(registro.getPorCodigoSII("C7"));
        final String c24 = String.valueOf(registro.getPorCodigoSII("C24"));

        if ("CRS101".equals(c7.trim().toUpperCase())) {
            return c24 != null && !c24.trim().isEmpty() && !"0".equals(c24.trim());
        }

        return true;
    }

    /**
     * Implementacion de la regla de validacion H3139
     *
     * @param registro
     *                 El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH3139(final Registro registro) {
        final String c7 = String.valueOf(registro.getPorCodigoSII("C7"));
        final String c25 = String.valueOf(registro.getPorCodigoSII("C25"));

        if ("CRS101".equals(c7.trim().toUpperCase())) {
            return c25 != null &&
                    !c25.trim().isEmpty() &&
                    !"RFE".equalsIgnoreCase(c25.trim()) &&
                    PAIS.contains(c25.trim().toUpperCase());
        }

        return true;
    }

    /**
     * Implementacion de la regla de validacion H3140
     *
     * @param registro
     *                 El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH3140(final Registro registro) {
        final String c7 = String.valueOf(registro.getPorCodigoSII("C7"));
        final String c27 = String.valueOf(registro.getPorCodigoSII("C27"));

        if ("CRS101".equals(c7.trim().toUpperCase())) {
            return c27 != null &&
                    !c27.trim().isEmpty() &&
                    !"RFE".equalsIgnoreCase(c27.trim()) &&
                    PAIS.contains(c27.trim().toUpperCase());
        }

        return true;
    }

    /**
     * Implementacion de la regla de validacion H3141
     *
     * @param registro
     *                 El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH3141(final Registro registro) {
        final String c7 = String.valueOf(registro.getPorCodigoSII("C7"));
        final String c28 = String.valueOf(registro.getPorCodigoSII("C28"));

        if ("CRS101".equals(c7.trim().toUpperCase())) {
            return c28 != null &&
                    !c28.trim().isEmpty() &&
                    TIPO_DOMICILIO.contains(c28.trim().toUpperCase());
        }

        return true;
    }

    /**
     * Implementacion de la regla de validacion H3142
     *
     * @param registro
     *                 El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH3142(final Registro registro) {
        final String c7 = String.valueOf(registro.getPorCodigoSII("C7"));
        final String c29 = String.valueOf(registro.getPorCodigoSII("C29"));

        if ("CRS101".equals(c7.trim().toUpperCase())) {
            return c29 != null && !c29.trim().isEmpty() && !"0".equals(c29.trim());
        }

        return true;
    }

    /**
     * Implementacion de la regla de validacion H3143
     *
     * @param registro
     *                 El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH3143(final Registro registro) {
        final String c7 = String.valueOf(registro.getPorCodigoSII("C7"));
        final String c30 = String.valueOf(registro.getPorCodigoSII("C30"));

        if ("CRS101".equalsIgnoreCase(c7.trim())) {
            if (c30 == null || c30.trim().isEmpty()){
                return false;
            }
            final String regex = "^\\d{4}-\\d{2}-\\d{2}$";
            String fecha = c30.trim();
            if (!fecha.matches(regex)) {
                return false;
            }
            try {
                java.time.LocalDate f = java.time.LocalDate.parse(fecha);
                if (f.isBefore(java.time.LocalDate.of(1920, 1, 1))) {
                    return false;
                }
            } catch (java.time.format.DateTimeParseException e) {
                return false;
            }
            return true;
        }
        return true;
    }

    /**
     * Implementacion de la regla de validacion H3144
     *
     * @param registro
     *                 El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH3144(final Registro registro) {

        final String c31 = String.valueOf(registro.getPorCodigoSII("C31"))
                .trim()
                .toUpperCase();

        final BigDecimal c33 = (BigDecimal) registro.getPorCodigoSII("C33");

        if (c33 != null
                && c33.compareTo(BigDecimal.ZERO) != 0
                && !TIPO_PAGO.contains(c31)) {
            return false;
        }

        return true;
    }

    /**
     * Implementacion de la regla de validacion H3145
     *
     * @param registro
     *                 El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH3145(final Registro registro) {
        final String c32 = String.valueOf(registro.getPorCodigoSII("C32"))
		        .trim()
		        .toUpperCase();

        final BigDecimal c33 = (BigDecimal) registro.getPorCodigoSII("C33");
        
        if (c33 != null
        		&& c33.compareTo(BigDecimal.ZERO) != 0
                && !MONEDA.contains(c32)) {
            return false;
        }
        
        
        return true;
    }

    /**
     * Implementacion de la regla de validacion H3146
     *
     * @param registro
     *                 El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH3146(final Registro registro) {
        final Object c33 = registro.getPorCodigoSII("C33");

        if (c33 == null || c33.toString().trim().isEmpty())
            return false;

        return true;
    }

    /**
     * Implementacion de la regla de validacion H3147
     *
     * @param registro
     *                 El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH3147(final Registro registro) {
        final String c6 = String.valueOf(registro.getPorCodigoSII("C6"));
        final String c9 = String.valueOf(registro.getPorCodigoSII("C9"));

        if ("1".equals(c6)) {
            return c9 != null && !c9.trim().isEmpty() && !"0".equals(c9.trim());
        }

        return true;
    }

    private boolean validarRutModulo11(String rut) {

        if (rut == null) {
            return false;
        }
        rut = rut.trim().toUpperCase();
         if (!rut.matches("^\\d{4,8}-[0-9K]$")) {
        return false;
        }
        final String cuerpo = rut.substring(0, rut.length() - 2);
        final char dv = rut.charAt(rut.length() - 1);
        int suma = 0;
        int multiplicador = 2;
        for (int i = cuerpo.length() - 1; i >= 0; i--) {
            suma += Character.getNumericValue(cuerpo.charAt(i)) * multiplicador;
            multiplicador = (multiplicador == 7) ? 2 : multiplicador + 1;
        }
        int resto = 11 - (suma % 11);
        char dvCalculado;
        if (resto == 11) {
            dvCalculado = '0';
        } else if (resto == 10) {
            dvCalculado = 'K';
        } else {
            dvCalculado = Character.forDigit(resto, 10);
        }
        return dv == dvCalculado;
    }

    public boolean validarH3291(final Registro registro) {
    final String c37 = String.valueOf(registro.getPorCodigoSII("C37"));

        // Si no viene informado, no aplica la validación
        if (c37 == null || c37.trim().isEmpty()) {
            return true;
        }

        if (!c37.matches("^\\d{7,8}-[0-9Kk]$") || !validarRutModulo11(c37)) {
            return false;
        }

        final String rutNumerico = c37.substring(0, c37.length() - 2);
        final long numeroRut = Long.parseLong(rutNumerico);

        return numeroRut > LimiteRutH3291;
    }

    public boolean validarH3292(final Registro registro) {
    final String c19 = String.valueOf(registro.getPorCodigoSII("C19"));
    final Object valor = registro.getPorCodigoSII("C21");
    final BigDecimal c21 = (BigDecimal) valor;

    if ("1".equals(c19)) {
        return c21.compareTo(BigDecimal.ZERO) == 0;
    }

        return true;

    }
}
