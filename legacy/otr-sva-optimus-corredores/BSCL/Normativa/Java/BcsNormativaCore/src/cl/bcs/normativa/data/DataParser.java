package cl.bcs.normativa.data;

import java.math.BigDecimal;
import java.util.Date;

import cl.bcs.normativa.esquema.Campo;
import cl.bcs.utils.DateFormatException;
import cl.bcs.utils.DateUtil;

/**
 * Parser de los tipos de datos utilizados.
 *
 * @author fpino@email.com
 *
 */
public final class DataParser {

    /**
     * La coma utilizado como separador en los n&uacute;meros.
     */
    private static final String COMMA = ",";

    /**
     * El punto utilizado como separador en los n&uacute;meros.
     */
    private static final String POINT = ".";

    /**
     * El s&iacute;mbolo utilizado como separador decimal. TODO pasar a char.
     */
    private final String decimalSymbol;

    /**
     * Constructor sin par&aacute;metros.
     */
    public DataParser() {
        this.decimalSymbol = ".";
    }

    /**
     * Constructor indicando el separador decimal.
     *
     * @param decimalSymbol
     *            El separador de decimales.
     * @throws DataParserException
     *             En caso de error en el separador de decimales.
     */
    public DataParser(final String decimalSymbol) throws DataParserException {
        if (decimalSymbol == null) {
            throw new DataParserException("Simbolo decimal no puede ser null");
        }

        if (decimalSymbol.isEmpty()) {
            throw new DataParserException("Simbolo decimal no puede ser vacio");
        }

        if (!decimalSymbol.equals(COMMA) && !decimalSymbol.equals(POINT)) {
            throw new DataParserException("Simbolo decimal invalido");
        }

        this.decimalSymbol = decimalSymbol;
    }

    /**
     * Quita los decimales para capturar n&uacute;meros enteros y los
     * separadores de miles.
     *
     * @param s
     *            Un {@link String}.
     * @return String sin la porci&oacute;n que contiene los decimales y sin
     *         separadores de miles.
     */
    private String discardDecimals(final String s) {
        String ret = normalizeNumber(s);
        final int x = ret.indexOf(POINT);
        if (x >= 0) {
            ret = ret.substring(0, x);
        }

        return ret;
    }

    /**
     * Quita separadores de miles y estandariza separador de decimales.
     *
     * @param s
     *            Un {@link String}.
     * @return String sin separadores de miles y separador de decimales
     *         est&aacute;ndar.
     */
    private String normalizeNumber(final String s) {
        final String thousandsSeparator = decimalSymbol.equals(COMMA) ? POINT : COMMA;
        String ret = s.replaceAll("\\" + thousandsSeparator, "");
        if (decimalSymbol.equals(COMMA)) {
            ret = ret.replace(COMMA, POINT);
        }

        return ret;
    }

    /**
     * Obtiene {@link Object} interpretando el {@link String} seg&uacute;n el
     * tipo indicado en el campo.
     *
     * @param campo
     *            El campo.
     * @param s
     *            Un {@link String}.
     * @return El {@link Object} interpretado.
     * @throws DataParserException
     *             En caso de error en el parser de datos.
     */
    public Comparable<?> parse(final Campo campo, final String s) throws DataParserException {
        if (s == null) {
            return s;
        }

        final String str = s.trim();

        if (campo.getTipoDato().equals("AMOUNT")) {
            return parseAmount(str);
        } else if (campo.getTipoDato().startsWith("DATE")) {
            return parseDate(str);
        } else if (campo.getTipoDato().equals("DECIMAL")) {
            return parseDecimal(str);
        } else if (campo.getTipoDato().startsWith("ID")) {
            return parseId(str);
        } else if (campo.getTipoDato().equals("QTY")) {
            return parseQuantity(str);
        } else {
            return str;
        }
    }

    /**
     * Parsea {@link BigDecimal} a partir de {@link String}, descartando los
     * separadores de miles y la parte decimal
     *
     * @param s
     *            Un {@link String}.
     * @return {@link BigDecimal} a partir de {@link String}.
     * @throws DataParserException
     *             En caso de error al interpretar dato.
     */
    private BigDecimal parseAmount(final String s) throws DataParserException {
        try {
            return new BigDecimal(discardDecimals(s));
        } catch (NumberFormatException ex) {
            throw new DataParserException("Dato invalido: " + s, ex);
        }
    }

    /**
     * Parsea {@link Date} a partir de {@link String}, seg&uacute;n formatos
     * soportados en {@link DateUtil#getDate}
     *
     * @param s
     *            Un {@link String}.
     * @return {@link Date} a partir de {@link String}.
     * @throws DataParserException
     *             En caso de error al interpretar dato.
     */
    private Date parseDate(final String s) throws DataParserException {
        try {
            return DateUtil.getDate(s);
        } catch (DateFormatException ex) {
            throw new DataParserException("Dato invalido: " + s, ex);
        }
    }

    /**
     * Parsea {@link BigDecimal} a partir de {@link String}, descartando los
     * separadores de miles.
     *
     * @param s
     *            Un {@link String}.
     * @return {@link BigDecimal} a partir de {@link String}.
     * @throws DataParserException
     *             En caso de error al interpretar dato.
     */
    private BigDecimal parseDecimal(final String s) throws DataParserException {
        try {
            return new BigDecimal(normalizeNumber(s));
        } catch (NumberFormatException ex) {
            throw new DataParserException("Dato invalido: " + s, ex);
        }
    }

    /**
     * Parsea {@link Long} a partir de {@link String} (formato estricto).
     *
     * @param s
     *            Un {@link String}.
     * @return {@link Long} a partir de {@link String}.
     * @throws DataParserException
     *             En caso de error al interpretar dato.
     */
    private long parseId(final String s) throws DataParserException {
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException ex) {
            throw new DataParserException("Dato invalido: " + s, ex);
        }
    }

    /**
     * Parsea {@link Long} a partir de {@link String}, descartando los
     * separadores de miles.
     *
     * @param s
     *            Un {@link String}.
     * @return {@link Long} a partir de {@link String}
     * @throws DataParserException
     *             En caso de error al interpretar dato.
     */
    private long parseQuantity(final String s) throws DataParserException {
        try {
            return Long.parseLong(discardDecimals(s));
        } catch (NumberFormatException ex) {
            throw new DataParserException("Dato invalido: " + s, ex);
        }
    }

}
