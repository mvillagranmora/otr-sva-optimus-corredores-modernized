package cl.bcs.cuentainversion.reportes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Amount formatting rules per currency ({@code PLA.MonedaFormato}, legacy {@code MonedaFormato}): number of decimals and whether the
 * value is truncated (T, towards negative infinity) or rounded (R, HALF_UP), by kind of figure (amount, quantity, price, rate, parity,
 * exchange rate, points). The most specific rule wins: currency + instrument type + operation type, then currency + instrument type,
 * then currency. Use one instance per request: the rules are read once.
 */
public final class FormatoMoneda {

    public static final String MONTO = "MONTO";
    public static final String CANTIDAD = "CANTIDAD";
    public static final String TASA = "TASA";
    public static final String PUNTOS = "PUNTOS";
    public static final String PRECIO = "PRECIO";
    public static final String TIPOCAMBIO = "TIPOCAMBIO";
    public static final String PARIDAD = "PARIDAD";

    private static final String SQL_FORMATOS = SqlReportes.recurso("plataforma/Formato_ConsultarMonedaFormato");
    private static final String USO = "GEN";

    /** A formatting rule of {@code PLA.MonedaFormato}. */
    public static class Regla {
        public Long IdMonedaFormato;
        public String IdMoneda;
        public String IdUsoMonedaFormato;
        public String IdTipoInstrumento;
        public String IdTipoOperacion;
        public Integer PrecioDecimales;
        public String PrecioTrucaRedondea;
        public Integer TasaDecimales;
        public String TasaTrucaRedondea;
        public Integer CantidadDecimales;
        public String CantidadTrucaRedondea;
        public Integer MontoDecimales;
        public String MontoTrucaRedondea;
        public Integer TipoCambioDecimales;
        public String TipoCambioTrucaRedondea;
        public Integer ParidadDecimales;
        public String ParidadTrucaRedondea;
        public Integer PuntosDecimales;
        public String PuntosTrucaRedondea;
        public String IdEstado;
    }

    /** Formatted value: {@code valor} with the rule applied, {@code valorConFormato} as text in Chilean format. */
    public record Resultado(BigDecimal valor, String uso, Integer decimales, String truncaRedondea, String idTipoInstrumento,
                            String idMoneda, String idTipoOperacion, String valorConFormato) {
    }

    private final SqlReportes sql;
    private List<Regla> reglas;

    public FormatoMoneda(SqlReportes sql) {
        this.sql = sql;
    }

    public Resultado formatear(BigDecimal valor, String tipoFormato, String idMoneda) throws Exception {
        return formatear(valor, tipoFormato, idMoneda, "", "");
    }

    public Resultado formatear(BigDecimal valor, String tipoFormato, String idMoneda, String idTipoInstrumento, String idTipoOperacion)
            throws Exception {
        if (reglas == null) {
            reglas = sql.consultar(SQL_FORMATOS, Map.of(), Regla.class);
        }
        if (idMoneda == null) {
            throw new Exception("debe especificar moneda");
        }
        BigDecimal v = valor == null ? BigDecimal.ZERO : valor;
        Regla regla = buscar(idMoneda, idTipoInstrumento, idTipoOperacion);
        if (regla == null) {
            throw new Exception("formato no encontrado moneda " + idMoneda + " uso " + USO + " tipo instrumento " + idTipoInstrumento
                    + " tipo operacion " + idTipoOperacion);
        }

        String truncaRedondea;
        int decimales;
        switch (tipoFormato.toUpperCase(Locale.ROOT)) {
            case MONTO -> {
                truncaRedondea = regla.MontoTrucaRedondea;
                decimales = regla.MontoDecimales;
            }
            case CANTIDAD -> {
                truncaRedondea = regla.CantidadTrucaRedondea;
                decimales = regla.CantidadDecimales;
            }
            case PRECIO -> {
                truncaRedondea = regla.PrecioTrucaRedondea;
                decimales = regla.PrecioDecimales;
            }
            case TASA -> {
                truncaRedondea = regla.TasaTrucaRedondea;
                decimales = regla.TasaDecimales;
            }
            case PARIDAD -> {
                truncaRedondea = regla.ParidadTrucaRedondea;
                decimales = regla.ParidadDecimales;
            }
            case TIPOCAMBIO -> {
                truncaRedondea = regla.TipoCambioTrucaRedondea;
                decimales = regla.TipoCambioDecimales;
            }
            case PUNTOS -> {
                truncaRedondea = regla.PuntosTrucaRedondea;
                decimales = regla.PuntosDecimales;
            }
            default -> throw new Exception("tipoFormato " + tipoFormato + " no valido");
        }

        BigDecimal valorFinal;
        if ("R".equalsIgnoreCase(truncaRedondea)) {
            valorFinal = v.setScale(decimales, RoundingMode.HALF_UP);
        } else if ("T".equalsIgnoreCase(truncaRedondea)) {
            valorFinal = v.setScale(decimales, RoundingMode.FLOOR);
        } else {
            throw new Exception("trunca o redondea invalido " + truncaRedondea + ".");
        }

        NumberFormat formato = NumberFormat.getCurrencyInstance(Locale.of("es", "CL"));
        formato.setMinimumFractionDigits(decimales);
        formato.setMaximumFractionDigits(decimales);
        String texto = formato.format(valorFinal).replace("Ch$", "");
        return new Resultado(valorFinal, regla.IdUsoMonedaFormato, decimales, truncaRedondea, regla.IdTipoInstrumento, regla.IdMoneda,
                regla.IdTipoOperacion, texto);
    }

    private Regla buscar(String idMoneda, String idTipoInstrumento, String idTipoOperacion) {
        Regla porInstrumento = null;
        Regla porMoneda = null;
        for (Regla f : reglas) {
            if (!USO.equalsIgnoreCase(f.IdUsoMonedaFormato) || !f.IdMoneda.equalsIgnoreCase(idMoneda)) {
                continue;
            }
            if (f.IdTipoInstrumento.equalsIgnoreCase(idTipoInstrumento)) {
                if (f.IdTipoOperacion.equalsIgnoreCase(idTipoOperacion)) {
                    return f;
                }
                if (porInstrumento == null) {
                    porInstrumento = f;
                }
            }
            if (porMoneda == null) {
                porMoneda = f;
            }
        }
        return porInstrumento != null ? porInstrumento : porMoneda;
    }
}
