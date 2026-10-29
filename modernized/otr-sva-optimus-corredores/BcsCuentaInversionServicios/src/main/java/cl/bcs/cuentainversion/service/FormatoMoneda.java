package cl.bcs.cuentainversion.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Rounds and formats amounts, quantities, rates, ... with the decimals and truncate/round rule configured per currency in
 * {@code PLA.MonedaFormato} (use GEN). Replaces the legacy platform helper MonedaFormato.
 */
@Component
public class FormatoMoneda {

    private static final String SQL = CtaJavaCallInfra.loadSql("cl/bcs/cuentainversion/sql/Formato_ConsultarMonedaFormato.sql");
    private static final String USO = "GEN";

    public enum Tipo { MONTO, CANTIDAD, TASA, PUNTOS, PRECIO, TIPOCAMBIO, PARIDAD }

    /** @param valor rounded value; @param valorConFormato the same value as es-CL text without currency symbol */
    public record Resultado(BigDecimal valor, String valorConFormato, int decimales) {
    }

    private final NamedParameterJdbcTemplate jdbc;
    private volatile List<Map<String, Object>> formatos;

    public FormatoMoneda(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Resultado formatear(BigDecimal valor, Tipo tipo, String idMoneda) {
        return formatear(valor, tipo, idMoneda, "", "");
    }

    public Resultado formatear(BigDecimal valor, Tipo tipo, String idMoneda, String idTipoInstrumento, String idTipoOperacion) {
        if (idMoneda == null) {
            throw new IllegalArgumentException("debe especificar moneda");
        }
        BigDecimal v = valor == null ? BigDecimal.ZERO : valor;
        Map<String, Object> f = buscar(idMoneda, idTipoInstrumento, idTipoOperacion);
        String prefijo = switch (tipo) {
            case MONTO -> "Monto";
            case CANTIDAD -> "Cantidad";
            case PRECIO -> "Precio";
            case TASA -> "Tasa";
            case PARIDAD -> "Paridad";
            case TIPOCAMBIO -> "TipoCambio";
            case PUNTOS -> "Puntos";
        };
        String regla = String.valueOf(f.get(prefijo + "TrucaRedondea"));
        int decimales = ((Number) f.get(prefijo + "Decimales")).intValue();
        BigDecimal redondeado = switch (regla.toUpperCase(Locale.ROOT)) {
            case "R" -> v.setScale(decimales, RoundingMode.HALF_UP);
            case "T" -> v.setScale(decimales, RoundingMode.FLOOR);
            default -> throw new IllegalStateException("trunca o redondea invalido " + regla + ".");
        };
        NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.of("es", "CL"));
        nf.setMinimumFractionDigits(decimales);
        nf.setMaximumFractionDigits(decimales);
        return new Resultado(redondeado, nf.format(redondeado).replace("Ch$", ""), decimales);
    }

    /** Most specific format first: instrument type + operation type, then instrument type, then the currency alone. */
    private Map<String, Object> buscar(String idMoneda, String idTipoInstrumento, String idTipoOperacion) {
        List<Map<String, Object>> todos = formatos();
        for (Map<String, Object> f : todos) {
            if (coincide(f, idMoneda) && igual(f, "IdTipoInstrumento", idTipoInstrumento) && igual(f, "IdTipoOperacion", idTipoOperacion)) {
                return f;
            }
        }
        for (Map<String, Object> f : todos) {
            if (coincide(f, idMoneda) && igual(f, "IdTipoInstrumento", idTipoInstrumento)) {
                return f;
            }
        }
        for (Map<String, Object> f : todos) {
            if (coincide(f, idMoneda)) {
                return f;
            }
        }
        throw new IllegalStateException("formato no encontrado moneda " + idMoneda + " uso " + USO + " tipo instrumento "
                + idTipoInstrumento + " tipo operacion " + idTipoOperacion);
    }

    private static boolean coincide(Map<String, Object> f, String idMoneda) {
        return USO.equalsIgnoreCase(String.valueOf(f.get("IdUsoMonedaFormato"))) && idMoneda.equalsIgnoreCase(String.valueOf(f.get("IdMoneda")));
    }

    private static boolean igual(Map<String, Object> f, String columna, String valor) {
        return String.valueOf(f.get(columna)).equalsIgnoreCase(valor);
    }

    private List<Map<String, Object>> formatos() {
        List<Map<String, Object>> f = formatos;
        if (f == null) {
            f = jdbc.queryForList(SQL, new MapSqlParameterSource());
            formatos = f;
        }
        return f;
    }
}
