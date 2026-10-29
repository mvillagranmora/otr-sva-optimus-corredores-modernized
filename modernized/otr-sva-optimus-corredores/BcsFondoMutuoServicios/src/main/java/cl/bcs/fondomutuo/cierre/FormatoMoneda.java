package cl.bcs.fondomutuo.cierre;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cl.bcs.fondomutuo.port.out.kit.CalendarioPort;

/**
 * Legacy {@code plataforma.pla.utils.MonedaFormato.formatear} (the {@code valor}; the display text {@code valorConFormato} is never used by the closure),
 * bug for bug: the row of {@code PLA.MonedaFormato} for use GEN is chosen by (currency, instrument type, operation type), then (currency, instrument type),
 * then currency; the number of decimals and truncate/round come from the kind of value; "R" rounds HALF_UP, "T" truncates (FLOOR). The legacy cached the whole
 * table in a JVM-wide static for ever; here the table is read once per INSTANCE (one instance per call: no static state, DEV-F2). A row with a null key
 * column raises NullPointerException as the legacy did.
 */
public final class FormatoMoneda {

    public static final String MONTO = "MONTO";
    public static final String CANTIDAD = "CANTIDAD";
    public static final String TASA = "TASA";
    public static final String PUNTOS = "PUNTOS";
    public static final String PRECIO = "PRECIO";
    public static final String TIPOCAMBIO = "TIPOCAMBIO";
    public static final String PARIDAD = "PARIDAD";

    private static final class Fila {
        String idMoneda;
        String idUsoMonedaFormato;
        String idTipoInstrumento;
        String idTipoOperacion;
        Map<String, Object> datos;

        String texto(String columna) {
            return (String) datos.get(columna);
        }

        Integer entero(String columna) {
            Object v = datos.get(columna);
            return v == null ? null : Integer.valueOf(((Number) v).intValue());
        }
    }

    private final CalendarioPort calendario;
    private List<Fila> formatos;

    public FormatoMoneda(CalendarioPort calendario) {
        this.calendario = calendario;
    }

    public BigDecimal formatear(BigDecimal valor, String tipoFormato, String idMoneda) throws Exception {
        return formatear(valor, tipoFormato, idMoneda, "", "");
    }

    public BigDecimal formatear(BigDecimal valor, String tipoFormato, String idMoneda, String idTipoInstrumento, String idTipoOperacion) throws Exception {
        if (formatos == null) {
            List<Fila> filas = new ArrayList<>();
            for (Map<String, Object> d : calendario.monedaFormato()) {
                Fila f = new Fila();
                f.datos = d;
                f.idMoneda = (String) d.get("IdMoneda");
                f.idUsoMonedaFormato = (String) d.get("IdUsoMonedaFormato");
                f.idTipoInstrumento = (String) d.get("IdTipoInstrumento");
                f.idTipoOperacion = (String) d.get("IdTipoOperacion");
                filas.add(f);
            }
            formatos = filas;
        }

        String uso = "GEN";
        if (idMoneda == null)
            throw new Exception("debe especificar moneda");

        if (valor == null)
            valor = BigDecimal.ZERO;
        Fila formato = null;
        for (Fila f : formatos) {
            if (f.idUsoMonedaFormato.equalsIgnoreCase(uso)
                    && f.idMoneda.equalsIgnoreCase(idMoneda)
                    && f.idTipoInstrumento.equalsIgnoreCase(idTipoInstrumento)
                    && f.idTipoOperacion.equalsIgnoreCase(idTipoOperacion)) {
                formato = f;
                break;
            }
        }

        if (formato == null) {
            for (Fila f : formatos) {
                if (f.idUsoMonedaFormato.equalsIgnoreCase(uso)
                        && f.idMoneda.equalsIgnoreCase(idMoneda)
                        && f.idTipoInstrumento.equalsIgnoreCase(idTipoInstrumento)) {
                    formato = f;
                    break;
                }
            }
        }

        if (formato == null) {
            for (Fila f : formatos) {
                if (f.idUsoMonedaFormato.equalsIgnoreCase(uso)
                        && f.idMoneda.equalsIgnoreCase(idMoneda)) {
                    formato = f;
                    break;
                }
            }
        }

        if (formato == null) {
            throw new Exception("formato no encontrado moneda " + idMoneda + " uso " + uso + " tipo instrumento " + idTipoInstrumento + " tipo operacion " + idTipoOperacion);
        }

        String truncaRedondea = null;
        Integer decimales = null;
        if (tipoFormato.equalsIgnoreCase(MONTO)) {
            truncaRedondea = formato.texto("MontoTrucaRedondea");
            decimales = formato.entero("MontoDecimales");
        } else if (tipoFormato.equalsIgnoreCase(CANTIDAD)) {
            truncaRedondea = formato.texto("CantidadTrucaRedondea");
            decimales = formato.entero("CantidadDecimales");
        } else if (tipoFormato.equalsIgnoreCase(PRECIO)) {
            truncaRedondea = formato.texto("PrecioTrucaRedondea");
            decimales = formato.entero("PrecioDecimales");
        } else if (tipoFormato.equalsIgnoreCase(TASA)) {
            truncaRedondea = formato.texto("TasaTrucaRedondea");
            decimales = formato.entero("TasaDecimales");
        } else if (tipoFormato.equalsIgnoreCase(PARIDAD)) {
            truncaRedondea = formato.texto("ParidadTrucaRedondea");
            decimales = formato.entero("ParidadDecimales");
        } else if (tipoFormato.equalsIgnoreCase(TIPOCAMBIO)) {
            truncaRedondea = formato.texto("TipoCambioTrucaRedondea");
            decimales = formato.entero("TipoCambioDecimales");
        } else if (tipoFormato.equalsIgnoreCase(PUNTOS)) {
            truncaRedondea = formato.texto("PuntosTrucaRedondea");
            decimales = formato.entero("PuntosDecimales");
        } else {
            throw new Exception("tipoFormato " + tipoFormato + " no valido");
        }

        BigDecimal valorFinal = BigDecimal.ZERO;
        if (truncaRedondea.equalsIgnoreCase("R")) {
            valorFinal = valor.setScale(decimales, RoundingMode.HALF_UP);
        } else if (truncaRedondea.equalsIgnoreCase("T")) {
            valorFinal = valor.setScale(decimales, RoundingMode.FLOOR);
        } else {
            throw new Exception("trunca o redondea invalido " + truncaRedondea + ".");
        }
        return valorFinal;
    }
}
