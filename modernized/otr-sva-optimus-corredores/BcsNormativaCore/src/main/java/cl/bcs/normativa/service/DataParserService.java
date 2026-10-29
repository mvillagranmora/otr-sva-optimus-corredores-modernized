package cl.bcs.normativa.service;

import cl.bcs.normativa.domain.Campo;
import cl.bcs.utils.DateFormatException;
import cl.bcs.utils.DateUtil;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

/**
 * Parser de tipos de datos para registros normativos.
 *
 * Reglas de negocio (migradas desde legacy cl.bcs.normativa.data.DataParser):
 * - AMOUNT:  BigDecimal entero (sin decimales, sin separadores de miles)
 * - DATE*:   Date segun formatos soportados por DateUtil
 * - DECIMAL: BigDecimal con decimales (sin separadores de miles)
 * - ID*:     Long estricto (sin separadores de miles)
 * - QTY:     Long (sin decimales, sin separadores de miles)
 * - otros:   String sin transformacion
 *
 * Los metodos de conversion son public static para permitir uso sin inyeccion.
 */
@Service
public class DataParserService {

    private static final String COMMA = ",";
    private static final String POINT = ".";

    private final String decimalSymbol;

    /** Constructor por defecto: separador decimal es punto. */
    public DataParserService() {
        this.decimalSymbol = POINT;
    }

    public DataParserService(String decimalSymbol) throws DataParserException {
        Objects.requireNonNull(decimalSymbol, "decimalSymbol no puede ser null");
        if (decimalSymbol.isEmpty()) {
            throw new DataParserException("Simbolo decimal no puede ser vacio");
        }
        if (!COMMA.equals(decimalSymbol) && !POINT.equals(decimalSymbol)) {
            throw new DataParserException("Simbolo decimal invalido: " + decimalSymbol);
        }
        this.decimalSymbol = decimalSymbol;
    }

    /**
     * Parsea el valor de un campo segun su tipoDato.
     * Pure-logic: puede invocarse desde cualquier contexto.
     */
    public Comparable<?> parse(Campo campo, String s) throws DataParserException {
        Objects.requireNonNull(campo, "campo no puede ser null");
        if (s == null) return null;

        String str = s.trim();
        String tipoDato = campo.getTipoDato();

        if ("AMOUNT".equals(tipoDato)) {
            return parseAmount(str);
        } else if (tipoDato != null && tipoDato.startsWith("DATE")) {
            return parseDate(str);
        } else if ("DECIMAL".equals(tipoDato)) {
            return parseDecimal(str);
        } else if (tipoDato != null && tipoDato.startsWith("ID")) {
            return parseId(str);
        } else if ("QTY".equals(tipoDato)) {
            return parseQuantity(str);
        } else {
            return str;
        }
    }

    // ---- static helpers (pure logic) ----

    /**
     * Normaliza un numero: quita separadores de miles y estandariza separador decimal.
     * B-NOR-DP-01: los separadores de miles se eliminan; el decimal queda como punto.
     */
    public static String normalizeNumber(String s, String decimalSymbol) {
        String thousandsSep = COMMA.equals(decimalSymbol) ? POINT : COMMA;
        String ret = s.replaceAll("\\" + thousandsSep, "");
        if (COMMA.equals(decimalSymbol)) {
            ret = ret.replace(COMMA, POINT);
        }
        return ret;
    }

    /**
     * Descarta la parte decimal de un numero ya normalizado.
     * B-NOR-DP-02: AMOUNT y QTY descartan decimales.
     */
    public static String discardDecimals(String s, String decimalSymbol) {
        String norm = normalizeNumber(s, decimalSymbol);
        int x = norm.indexOf(POINT);
        return x >= 0 ? norm.substring(0, x) : norm;
    }

    private BigDecimal parseAmount(String s) throws DataParserException {
        try {
            return new BigDecimal(discardDecimals(s, decimalSymbol));
        } catch (NumberFormatException ex) {
            throw new DataParserException("Dato invalido (AMOUNT): " + s, ex);
        }
    }

    private Date parseDate(String s) throws DataParserException {
        try {
            return DateUtil.getDate(s);
        } catch (DateFormatException ex) {
            throw new DataParserException("Dato invalido (DATE): " + s, ex);
        }
    }

    private BigDecimal parseDecimal(String s) throws DataParserException {
        try {
            return new BigDecimal(normalizeNumber(s, decimalSymbol));
        } catch (NumberFormatException ex) {
            throw new DataParserException("Dato invalido (DECIMAL): " + s, ex);
        }
    }

    private long parseId(String s) throws DataParserException {
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException ex) {
            throw new DataParserException("Dato invalido (ID): " + s, ex);
        }
    }

    private long parseQuantity(String s) throws DataParserException {
        try {
            return Long.parseLong(discardDecimals(s, decimalSymbol));
        } catch (NumberFormatException ex) {
            throw new DataParserException("Dato invalido (QTY): " + s, ex);
        }
    }
}
