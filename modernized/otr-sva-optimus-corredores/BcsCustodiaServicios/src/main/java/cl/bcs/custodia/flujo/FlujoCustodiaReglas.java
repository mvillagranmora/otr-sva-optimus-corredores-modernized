package cl.bcs.custodia.flujo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonObject;

import cl.bcs.custodia.flujo.FlujoCustodiaModelo.ConfiguracionUso;
import cl.bcs.custodia.flujo.FlujoCustodiaModelo.UsoFiltro;
import cl.bcs.custodia.flujo.kit.JsonFlujo;

/**
 * The deterministic rules of ConsultarFlujoCustodia, free of I/O: the item to source map (CUS-FLU-13, D-CUS-07), the similarity score of a requested key
 * against a configured use (CUS-FLU-10, D-CUS-02), the discard reasons (CUS-FLU-25), the {@code ConsiderarRegistro} decision (CUS-FLU-30), the operator
 * sign (CUS-FLU-24) and the historic switch (CUS-FLU-07). Null handling is the legacy's on purpose: where it dereferenced a null it still does
 * (NullPointerException is part of the pinned behavior).
 */
public final class FlujoCustodiaReglas {

    private static final Map<String, String> ITEM_FUENTE = construirMapaItems();

    private FlujoCustodiaReglas() {
    }

    private static Map<String, String> construirMapaItems() {
        Map<String, String> m = new HashMap<>();
        // 1.1 CARTERA
        m.put("INFORMACION_CARTERA", "CARTERA");
        // 1.1.1 operations in transit (invoiced, settle later; not the title in/out movements)
        m.put("CARTERA_TRANSITO_COMPRA", "CARTERA_TRANSITO");
        m.put("CARTERA_TRANSITO_VENTA", "CARTERA_TRANSITO");
        m.put("CARTERA_TRANSITO_VENTA_RPA", "CARTERA_VENTA_TRANSITO_RPA");   // mapped but never read (D-CUS-07)
        // 1.2 TRANSITO
        m.put("INFORMACION_TRANSITO_INGRESO", "TRANSITO");
        m.put("INFORMACION_TRANSITO_RETIRO", "TRANSITO");
        m.put("MOV_TRANSITO_RETIRO_VENTA_DESCUBIERTA", "TRANSITO");
        // 1.3 GARANTIA
        m.put("INFORMACION_GARANTIA", "GARANTIA");
        // 1.4 SUBYACENTE
        m.put("SIMULTANEA_COMPRA", "SUBYACENTE");
        m.put("SIMULTANEA_VENTA", "SUBYACENTE");
        m.put("REPORTE_COMPRA", "SUBYACENTE");
        m.put("REPORTE_VENTA", "SUBYACENTE");
        m.put("PRESTAMO", "SUBYACENTE");
        m.put("EMPRESTITO", "SUBYACENTE");
        m.put("COMPRA_PACTO", "SUBYACENTE");
        m.put("VENTA_PACTO", "SUBYACENTE");
        // 1.5 CARTERA RESERVADA
        m.put("RESERVA_COMPRA_PACTOS", "CARTERA_RESERVADA");
        m.put("RESERVA_VENTA_PACTO", "CARTERA_RESERVADA");
        m.put("RESERVA_COMPRA_TERMINO", "CARTERA_RESERVADA");
        m.put("RESERVA_VENTA_TERMINO", "CARTERA_RESERVADA");
        // 1.6 ORDERS
        m.put("ORDENES_COMPRA_PENDIENTES", "ORDENES_POR_AUTORIZAR");
        m.put("ORDENES_VENTA_PENDIENTES", "ORDENES_POR_AUTORIZAR");
        m.put("ORDENES_COMPRA_INGRESADAS", "ORDENES_INGRESADAS");
        m.put("ORDENES_VENTA_INGRESADAS", "ORDENES_INGRESADAS");
        m.put("ORDENES_REPORTE_COMPRA_PENDIENTES", "ORDENES_REPORTE_POR_AUTORIZAR");
        m.put("ORDENES_REPORTE_VENTA_PENDIENTES", "ORDENES_REPORTE_POR_AUTORIZAR");
        m.put("ORDENES_REPORTE_COMPRA_INGRESADAS", "ORDENES_REPORTE_INGRESADAS");
        m.put("ORDENES_REPORTE_VENTA_INGRESADAS", "ORDENES_REPORTE_INGRESADAS");
        // 1.7 ASSIGNMENTS NOT SENT TO INVOICING
        m.put("ASIGNACIONES_COMPRA_PENDIENTE", "ASIGNACIONES_NO_ENVIAS_A_FACTURAR");
        m.put("ASIGNACIONES_VENTA_PENDIENTE", "ASIGNACIONES_NO_ENVIAS_A_FACTURAR");
        m.put("ASIGNACIONES_REPORTE_COMPRA_PENDIENTE", "ASIGNACIONES_REPORTE_NO_ENVIAS_A_FACTURAR");
        m.put("ASIGNACIONES_REPORTE_VENTA_PENDIENTE", "ASIGNACIONES_REPORTE_NO_ENVIAS_A_FACTURAR");
        // 1.8 INVOICING MOVEMENTS
        m.put("MOVIMIENTOS_COMPRA_FACTURACION", "MOVIMIENTOS_FACTURACION");
        m.put("MOVIMIENTOS_VENTA_FACTURACION", "MOVIMIENTOS_FACTURACION");
        m.put("MOVIMIENTOS_REPORTE_COMPRA_FACTURACION", "MOVIMIENTOS_REPORTE_FACTURACION");
        m.put("MOVIMIENTOS_REPORTE_VENTA_FACTURACION", "MOVIMIENTOS_REPORTE_FACTURACION");
        // 1.9 CAPITAL EVENTS
        m.put("SUBSCRIPCIONES", "EVENTOS_DE_CAPITAL");
        m.put("PREPAGO_BONO", "EVENTOS_DE_CAPITAL");
        m.put("SORTEO_LETRAS", "EVENTOS_DE_CAPITAL");
        // 1.10 MUTUAL FUNDS
        m.put("APORTE_FONDOS_MUTUOS", "FONDOS_MUTUOS");
        m.put("RESCATE_FONDOS_MUTUOS", "FONDOS_MUTUOS");
        return m;
    }

    /** CUS-FLU-13: the source an item is read from; {@code null} when the item is outside the map (nothing is read for it). */
    public static String fuenteDeItem(String itemCalculo) {
        return ITEM_FUENTE.get(itemCalculo);
    }

    /** CUS-FLU-10 on the JSON shapes of the two sides (requested: {@link UsoFiltro} fields; configured: {@link ConfiguracionUso} fields). */
    public static int similitud(JsonObject solicitado, JsonObject configurado) {
        UsoFiltro u = JsonFlujo.legacy().fromJson(solicitado, UsoFiltro.class);
        ConfiguracionUso c = JsonFlujo.legacy().fromJson(configurado, ConfiguracionUso.class);
        return similitud(u, c);
    }

    /**
     * CUS-FLU-10: base 2 (1 when the requested operation type is ""); for each of classification, instrument type, instrument, client, portfolio and
     * specific search: +2 when equal, else +1 when the requested value is empty (null or 0 for the numeric ones). D-CUS-02: a configured instrument,
     * client or portfolio that is null against a requested one that is not throws NullPointerException, as does a null requested operation type or
     * specific search where the legacy compared it with "".
     */
    public static int similitud(UsoFiltro u, ConfiguracionUso ufc) {
        int puntaje = 2;
        if (u.UsoTipoOperacion.equals("")) {
            puntaje = 1;
        }

        if ((ufc.IdClasificacionTipoInstrumento + "").equals(u.IdClasificacionTipoInstrumento + "")) {
            puntaje += 2;
        } else if ((u.IdClasificacionTipoInstrumento + "").equals("")) {
            puntaje += 1;
        }

        String tipoConfigurado = ufc.IdTipoInstrumento == null ? "" : ufc.IdTipoInstrumento;
        if (tipoConfigurado.equals(u.IdTipoInstrumento + "")) {
            puntaje += 2;
        } else if ((u.IdTipoInstrumento + "").equals("")) {
            puntaje += 1;
        }

        if ((ufc.IdInstrumento == null && u.IdInstrumento == null) || (ufc.IdInstrumento.equals(u.IdInstrumento))) {
            puntaje += 2;
        } else if (u.IdInstrumento == null || u.IdInstrumento == 0) {
            puntaje += 1;
        }

        if ((ufc.IdCliente == null && u.IdCliente == null) || (ufc.IdCliente.equals(u.IdCliente))) {
            puntaje += 2;
        } else if (u.IdCliente == null || u.IdCliente == 0) {
            puntaje += 1;
        }

        if ((ufc.IdPortafolio == null && u.IdPortafolio == null) || (ufc.IdPortafolio.equals(u.IdPortafolio))) {
            puntaje += 2;
        } else if (u.IdPortafolio == null || u.IdPortafolio == 0) {
            puntaje += 1;
        }

        if ((ufc.BusquedaEspecifica + "").equals(u.BusquedaEspecifica + "")) {
            puntaje += 2;
        } else if (u.BusquedaEspecifica.equals("")) {
            puntaje += 1;
        }
        return puntaje;
    }

    /**
     * CUS-FLU-25: why a real row is discarded, first match wins; "" keeps it. NullPointerException where the legacy dereferenced a null (portfolio
     * type, specific search, operation type).
     *
     * @param tipoInstrumentoGarantizable the (instrument type, specific instrument type) pair is in the guarantee table with an guarantor type
     */
    public static String motivoDescarte(String funcionOperador, String tipoCarteraCorredor, String usoTipoOperacion, String busquedaEspecifica,
            boolean tipoInstrumentoGarantizable) {
        if (funcionOperador.equals("")) {
            return "CONFIGURACION_SIN_FUNCION_OPERADOR";
        } else if (tipoCarteraCorredor.equals("CC")) {
            return "SE_EXCLUYE_CARTERA_COMPROMETIDA";
        } else if (usoTipoOperacion == null && busquedaEspecifica.equals("")) {
            return "CONFIGURACION_TIPO_OPERACION_EXCLUYE_INSTRUMENTO";
        } else if (!tipoInstrumentoGarantizable && busquedaEspecifica != null && busquedaEspecifica.equals("SOLO_GARANTIZABLE")
                && !usoTipoOperacion.equals("INGGPR")) {
            return "CONFIGURACION_GARANTIA_EXCLUYE_INSTRUMENTO";
        }
        return "";
    }

    /**
     * CUS-FLU-30: whether a grouped line counts. A null flag is "N". With the origin grouped ("S"): a line is dropped when its origin has rows but its
     * item has none. Not grouped by origin: dropped when the key has rows but the item has none.
     */
    public static String considerarRegistro(String flagItemOrigen, long cantidadItem, long sItemOrigen, long sItemCalculo, long sReg) {
        String flag = flagItemOrigen == null ? "N" : flagItemOrigen;
        if (flag.equals("S") && cantidadItem > 0 && sItemOrigen == 0) {
            return "S";
        } else if (flag.equals("S") && cantidadItem > 0 && sItemOrigen > 0 && sItemCalculo == 0) {
            return "N";
        } else if (flag.equals("N") && sReg > 0 && sItemCalculo == 0) {
            return "N";
        }
        return "S";
    }

    /** CUS-FLU-24: "-" turns the value negative (scale kept); any other operator leaves it as is. */
    public static BigDecimal aplicarOperador(String funcionOperador, BigDecimal valor) {
        BigDecimal factor = funcionOperador.equals("-") ? new BigDecimal(-1) : new BigDecimal(1);
        return valor.multiply(factor);
    }

    /** CUS-FLU-07: the historic tables are read when the last projected date is strictly before the system date. */
    public static boolean leerHistorico(LocalDate fechaMaxima, LocalDate fechaSistema) {
        return fechaMaxima.compareTo(fechaSistema) < 0;
    }
}
