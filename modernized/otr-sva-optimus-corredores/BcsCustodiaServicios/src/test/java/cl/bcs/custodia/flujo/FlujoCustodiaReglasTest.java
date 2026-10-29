package cl.bcs.custodia.flujo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.TreeMap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.flujo.h2.FlujoCaso;

/**
 * Pure unit tests of the deterministic pieces of ConsultarFlujoCustodia.logica, with literal inputs and outputs derived from the legacy code
 * (line numbers of ConsultarFlujoCustodia.java). They call the static methods of {@link FlujoCustodiaContrato#REGLAS} by reflection: until the
 * new code has that class every test FAILS with "implementation missing" (never skipped). Contract:
 * <pre>
 * public final class cl.bcs.custodia.flujo.FlujoCustodiaReglas {
 *   static String     fuenteDeItem(String itemCalculo)                                     // CUS-FLU-13, null = not mapped
 *   static int        similitud(JsonObject solicitado, JsonObject configurado)              // CUS-FLU-10, D-CUS-02 (NPE kept)
 *   static String     motivoDescarte(String funcionOperador, String tipoCarteraCorredor, String usoTipoOperacion,
 *                                    String busquedaEspecifica, boolean tipoInstrumentoGarantizable)   // CUS-FLU-25, "" = kept
 *   static String     considerarRegistro(String flagItemOrigen, long cantidadItem, long sItemOrigen, long sItemCalculo, long sReg) // CUS-FLU-30
 *   static BigDecimal aplicarOperador(String funcionOperador, BigDecimal valor)             // CUS-FLU-24
 *   static boolean    leerHistorico(LocalDate fechaMaxima, LocalDate fechaSistema)          // CUS-FLU-07
 * }
 * </pre>
 * {@code solicitado} carries the FlujoUsoFiltrosBusqueda fields, {@code configurado} the ConfiguracionUsoFlujoCustodia fields (same JSON names:
 * UsoTipoOperacion, IdClasificacionTipoInstrumento, IdTipoInstrumento, IdInstrumento, IdCliente, IdPortafolio, BusquedaEspecifica).
 */
@DisplayName("ConsultarFlujoCustodia deterministic rules (pure)")
class FlujoCustodiaReglasTest {

    // ------------------------------------------------------------------------------------------------ CUS-FLU-13 item -> source map (:2218-2279)

    @ParameterizedTest(name = "CUS-FLU-13 item {0} reads source {1}")
    @CsvSource({
            "INFORMACION_CARTERA, CARTERA",
            "CARTERA_TRANSITO_COMPRA, CARTERA_TRANSITO", "CARTERA_TRANSITO_VENTA, CARTERA_TRANSITO",
            "CARTERA_TRANSITO_VENTA_RPA, CARTERA_VENTA_TRANSITO_RPA",
            "INFORMACION_TRANSITO_INGRESO, TRANSITO", "INFORMACION_TRANSITO_RETIRO, TRANSITO", "MOV_TRANSITO_RETIRO_VENTA_DESCUBIERTA, TRANSITO",
            "INFORMACION_GARANTIA, GARANTIA",
            "SIMULTANEA_COMPRA, SUBYACENTE", "SIMULTANEA_VENTA, SUBYACENTE", "REPORTE_COMPRA, SUBYACENTE", "REPORTE_VENTA, SUBYACENTE",
            "PRESTAMO, SUBYACENTE", "EMPRESTITO, SUBYACENTE", "COMPRA_PACTO, SUBYACENTE", "VENTA_PACTO, SUBYACENTE",
            "RESERVA_COMPRA_PACTOS, CARTERA_RESERVADA", "RESERVA_VENTA_PACTO, CARTERA_RESERVADA", "RESERVA_COMPRA_TERMINO, CARTERA_RESERVADA",
            "RESERVA_VENTA_TERMINO, CARTERA_RESERVADA",
            "ORDENES_COMPRA_PENDIENTES, ORDENES_POR_AUTORIZAR", "ORDENES_VENTA_PENDIENTES, ORDENES_POR_AUTORIZAR",
            "ORDENES_COMPRA_INGRESADAS, ORDENES_INGRESADAS", "ORDENES_VENTA_INGRESADAS, ORDENES_INGRESADAS",
            "ORDENES_REPORTE_COMPRA_PENDIENTES, ORDENES_REPORTE_POR_AUTORIZAR", "ORDENES_REPORTE_VENTA_PENDIENTES, ORDENES_REPORTE_POR_AUTORIZAR",
            "ORDENES_REPORTE_COMPRA_INGRESADAS, ORDENES_REPORTE_INGRESADAS", "ORDENES_REPORTE_VENTA_INGRESADAS, ORDENES_REPORTE_INGRESADAS",
            "ASIGNACIONES_COMPRA_PENDIENTE, ASIGNACIONES_NO_ENVIAS_A_FACTURAR", "ASIGNACIONES_VENTA_PENDIENTE, ASIGNACIONES_NO_ENVIAS_A_FACTURAR",
            "ASIGNACIONES_REPORTE_COMPRA_PENDIENTE, ASIGNACIONES_REPORTE_NO_ENVIAS_A_FACTURAR",
            "ASIGNACIONES_REPORTE_VENTA_PENDIENTE, ASIGNACIONES_REPORTE_NO_ENVIAS_A_FACTURAR",
            "MOVIMIENTOS_COMPRA_FACTURACION, MOVIMIENTOS_FACTURACION", "MOVIMIENTOS_VENTA_FACTURACION, MOVIMIENTOS_FACTURACION",
            "MOVIMIENTOS_REPORTE_COMPRA_FACTURACION, MOVIMIENTOS_REPORTE_FACTURACION",
            "MOVIMIENTOS_REPORTE_VENTA_FACTURACION, MOVIMIENTOS_REPORTE_FACTURACION",
            "SUBSCRIPCIONES, EVENTOS_DE_CAPITAL", "PREPAGO_BONO, EVENTOS_DE_CAPITAL", "SORTEO_LETRAS, EVENTOS_DE_CAPITAL",
            "APORTE_FONDOS_MUTUOS, FONDOS_MUTUOS", "RESCATE_FONDOS_MUTUOS, FONDOS_MUTUOS" })
    void fuenteDeItem(String item, String fuente) throws Throwable {
        assertThat(fuente(item)).isEqualTo(fuente);
    }

    @ParameterizedTest(name = "CUS-FLU-13 item ''{0}'' is outside the map: nothing is read")
    @CsvSource({ "ITEM_INVENTADO", "informacion_cartera", "MOVIMIENTOS__FACTURACION", "ORDENES__PENDIENTES", "CARTERA" })
    void itemFueraDelMapa(String item) throws Throwable {
        assertThat(fuente(item)).isNull();
    }

    @Test
    @DisplayName("CUS-FLU-13 D-CUS-07 the map has 41 items and 17 source names, exactly the map recorded from the legacy (unit-goldens/item-fuente.json)")
    void mapaCompletoIgualAlLegacy() throws Throwable {
        JsonObject g = FlujoCaso.leer(GoldenCasos.base().resolve("unit-goldens/item-fuente.json"));
        assertThat(g.get("items").getAsInt()).isEqualTo(41);   // RULES_CUSTODIA_FLUJO.md CUS-FLU-13 says "42 items -> 15 sources": the code has 41 -> 17
        assertThat(g.get("fuentes").getAsInt()).isEqualTo(17);
        Map<String, String> obtenido = new TreeMap<>();
        for (String k : g.getAsJsonObject("mapa").keySet()) obtenido.put(k, fuente(k));
        Map<String, String> esperado = new TreeMap<>();
        for (String k : g.getAsJsonObject("mapa").keySet()) esperado.put(k, g.getAsJsonObject("mapa").get(k).getAsString());
        assertThat(obtenido).isEqualTo(esperado);
    }

    // ------------------------------------------------------------------------------------------------ CUS-FLU-10 similarity score (:1096-1138)

    @ParameterizedTest(name = "CUS-FLU-10 score {2}: requested {0} vs configured {1}")
    @CsvSource(delimiter = '|', value = {
            // every dimension null, requested BusquedaEspecifica "": 2 + clas 2 + tipo 0 ("" vs "null") + ins 2 + cli 2 + por 2 + busq 1 = 11
            "{'UsoTipoOperacion':'CUSTOD','BusquedaEspecifica':''} | {'UsoTipoOperacion':'CUSTOD'} | 11",
            // requested type "" -> base 1
            "{'UsoTipoOperacion':'','BusquedaEspecifica':''} | {'UsoTipoOperacion':''} | 10",
            // classification equal +2 / configured null vs requested RV +0 / requested "" +1
            "{'UsoTipoOperacion':'CUSTOD','IdClasificacionTipoInstrumento':'RV','BusquedaEspecifica':''} | {'UsoTipoOperacion':'CUSTOD','IdClasificacionTipoInstrumento':'RV'} | 11",
            "{'UsoTipoOperacion':'CUSTOD','IdClasificacionTipoInstrumento':'RV','BusquedaEspecifica':''} | {'UsoTipoOperacion':'CUSTOD'} | 9",
            "{'UsoTipoOperacion':'CUSTOD','IdClasificacionTipoInstrumento':'','BusquedaEspecifica':''} | {'UsoTipoOperacion':'CUSTOD','IdClasificacionTipoInstrumento':'RV'} | 10",
            // instrument type: configured null counts as "", so requested "" scores +2; requested null ("null") vs configured null scores 0
            "{'UsoTipoOperacion':'CUSTOD','IdTipoInstrumento':'','BusquedaEspecifica':''} | {'UsoTipoOperacion':'CUSTOD'} | 13",
            "{'UsoTipoOperacion':'CUSTOD','IdTipoInstrumento':'ACC','BusquedaEspecifica':''} | {'UsoTipoOperacion':'CUSTOD','IdTipoInstrumento':'ACC'} | 13",
            "{'UsoTipoOperacion':'CUSTOD','IdTipoInstrumento':'ACC','BusquedaEspecifica':''} | {'UsoTipoOperacion':'CUSTOD'} | 11",
            // instrument (Long): equal +2, requested null or 0 +1, different 0
            "{'UsoTipoOperacion':'CUSTOD','IdInstrumento':1001,'BusquedaEspecifica':''} | {'UsoTipoOperacion':'CUSTOD','IdInstrumento':1001} | 11",
            "{'UsoTipoOperacion':'CUSTOD','BusquedaEspecifica':''} | {'UsoTipoOperacion':'CUSTOD','IdInstrumento':0} | 10",
            "{'UsoTipoOperacion':'CUSTOD','IdInstrumento':0,'BusquedaEspecifica':''} | {'UsoTipoOperacion':'CUSTOD','IdInstrumento':7} | 10",
            "{'UsoTipoOperacion':'CUSTOD','IdInstrumento':1001,'BusquedaEspecifica':''} | {'UsoTipoOperacion':'CUSTOD','IdInstrumento':1002} | 9",
            // client and portfolio follow the instrument rule
            "{'UsoTipoOperacion':'CUSTOD','IdCliente':500,'BusquedaEspecifica':''} | {'UsoTipoOperacion':'CUSTOD','IdCliente':500} | 11",
            "{'UsoTipoOperacion':'CUSTOD','BusquedaEspecifica':''} | {'UsoTipoOperacion':'CUSTOD','IdPortafolio':5001} | 10",
            // specific search: equal +2 (also null = null), requested "" +1, different 0
            "{'UsoTipoOperacion':'CUSTOD','BusquedaEspecifica':'SOLO_SUBYACENTE'} | {'UsoTipoOperacion':'CUSTOD','BusquedaEspecifica':'SOLO_SUBYACENTE'} | 12",
            "{'UsoTipoOperacion':'CUSTOD','BusquedaEspecifica':'SOLO_SUBYACENTE'} | {'UsoTipoOperacion':'CUSTOD','BusquedaEspecifica':'OTRA'} | 10",
            "{'UsoTipoOperacion':'CUSTOD'} | {'UsoTipoOperacion':'CUSTOD'} | 12",
            // everything equal: 2 + 6 x 2 = 14 (maximum)
            "{'UsoTipoOperacion':'CUSTOD','IdClasificacionTipoInstrumento':'RV','IdTipoInstrumento':'ACC','IdInstrumento':1001,'IdCliente':500,'IdPortafolio':5001,'BusquedaEspecifica':'B'}"
                    + " | {'UsoTipoOperacion':'CUSTOD','IdClasificacionTipoInstrumento':'RV','IdTipoInstrumento':'ACC','IdInstrumento':1001,'IdCliente':500,'IdPortafolio':5001,'BusquedaEspecifica':'B'} | 14" })
    void similitud(String solicitado, String configurado, int puntaje) throws Throwable {
        assertThat(score(solicitado, configurado)).isEqualTo(puntaje);
    }

    @ParameterizedTest(name = "D-CUS-02 NullPointerException: requested {0} vs configured {1}")
    @CsvSource(delimiter = '|', value = {
            "{'UsoTipoOperacion':'CUSTOD','IdInstrumento':1001,'BusquedaEspecifica':''} | {'UsoTipoOperacion':'CUSTOD'}",
            "{'UsoTipoOperacion':'CUSTOD','IdCliente':500,'BusquedaEspecifica':''} | {'UsoTipoOperacion':'CUSTOD'}",
            "{'UsoTipoOperacion':'CUSTOD','IdPortafolio':5001,'BusquedaEspecifica':''} | {'UsoTipoOperacion':'CUSTOD'}",
            // requested BusquedaEspecifica null against a configured one (u.BusquedaEspecifica.equals(""))
            "{'UsoTipoOperacion':'CUSTOD'} | {'UsoTipoOperacion':'CUSTOD','BusquedaEspecifica':'X'}",
            // requested UsoTipoOperacion null (u.UsoTipoOperacion.equals(""), :1103)
            "{'BusquedaEspecifica':''} | {}" })
    void similitudNpeDelLegacy(String solicitado, String configurado) {
        assertThatThrownBy(() -> score(solicitado, configurado)).isInstanceOf(NullPointerException.class);
    }

    // ------------------------------------------------------------------------------------------------ CUS-FLU-25 discard reasons (:1565-1577)

    @ParameterizedTest(name = "CUS-FLU-25 operator ''{0}'' portfolio ''{1}'' use ''{2}'' search ''{3}'' guarantee {4} -> ''{5}''")
    @CsvSource(nullValues = "NULL", value = {
            "'',  CL, CUSTOD, '', false, CONFIGURACION_SIN_FUNCION_OPERADOR",
            "'',  CC, CUSTOD, '', false, CONFIGURACION_SIN_FUNCION_OPERADOR",
            "+,   CC, CUSTOD, '', false, SE_EXCLUYE_CARTERA_COMPROMETIDA",
            "-,   cc, CUSTOD, '', false, ''",
            "+,   CL, NULL,   '', false, CONFIGURACION_TIPO_OPERACION_EXCLUYE_INSTRUMENTO",
            "+,   CL, GARANT, SOLO_GARANTIZABLE, false, CONFIGURACION_GARANTIA_EXCLUYE_INSTRUMENTO",
            "+,   CL, GARANT, SOLO_GARANTIZABLE, true,  ''",
            "+,   CL, INGGPR, SOLO_GARANTIZABLE, false, ''",
            "-,   '', CUSTOD, '', false, ''",
            "+,   CL, CUSTOD, NULL, false, ''",
            "*,   CL, CUSTOD, SOLO_SUBYACENTE, false, ''" })
    void motivoDescarte(String operador, String cartera, String uso, String busqueda, boolean garantizable, String motivo) throws Throwable {
        assertThat(descarte(operador, cartera, uso, busqueda, garantizable)).isEqualTo(motivo);
    }

    @ParameterizedTest(name = "CUS-FLU-25 NullPointerException kept: portfolio {1} use {2} search {3}")
    @CsvSource(nullValues = "NULL", value = {
            "+, NULL, CUSTOD, '', false",                      // rec.TipoCarteraCorredor.equals("CC")
            "+, CL,   NULL,   NULL, false",                    // rec.BusquedaEspecifica.equals("") with a null type
            "+, CL,   NULL,   SOLO_GARANTIZABLE, false" })      // !rec.UsoTipoOperacion.equals("INGGPR")
    void motivoDescarteNpe(String operador, String cartera, String uso, String busqueda, boolean garantizable) {
        assertThatThrownBy(() -> descarte(operador, cartera, uso, busqueda, garantizable)).isInstanceOf(NullPointerException.class);
    }

    // ------------------------------------------------------------------------------------------------ CUS-FLU-30 ConsiderarRegistro (:1892-1902)

    @ParameterizedTest(name = "CUS-FLU-30 ItemOrigen flag {0}, CantidadItem {1}, SItemOrigen {2}, SItemCalculo {3}, SREG {4} -> {5}")
    @CsvSource(nullValues = "NULL", value = {
            "S, 1, 0, 0, 0, S",     // branch 1: grouped by origin, nothing in the origin
            "S, 3, 2, 0, 5, N",     // branch 2: the origin has rows, this item none
            "S, 1, 2, 1, 5, S",     // else
            "S, 0, 0, 0, 0, S",     // CantidadItem 0: neither branch 1 nor 2
            "N, 1, 0, 0, 1, N",     // branch 3: not grouped by origin, the key has rows, this item none
            "N, 1, 0, 1, 1, S",
            "N, 1, 0, 0, 0, S",     // branch 4 (else): no row at all
            "NULL, 1, 0, 0, 1, N",  // a null flag is "N" (:1890)
            "X, 1, 0, 0, 1, S" })   // any other flag: else
    void considerarRegistro(String flag, long cantidadItem, long sOrigen, long sCalculo, long sReg, String esperado) throws Throwable {
        Object r = FlujoCustodiaContrato.regla("considerarRegistro", new Class<?>[] { String.class, long.class, long.class, long.class, long.class },
                flag, cantidadItem, sOrigen, sCalculo, sReg);
        assertThat(r).isEqualTo(esperado);
    }

    // ------------------------------------------------------------------------------------------------ CUS-FLU-24 operator sign (:1554-1556)

    @ParameterizedTest(name = "CUS-FLU-24 operator ''{0}'' applied to {1} = {2} (scale kept)")
    @CsvSource({ "-, 100.00000000, -100.00000000", "+, 100.5, 100.5", "*, 3, 3", "'', 2050.50000000, 2050.50000000", "-, 0E-8, 0E-8" })
    void aplicarOperador(String operador, String valor, String esperado) throws Throwable {
        Object r = FlujoCustodiaContrato.regla("aplicarOperador", new Class<?>[] { String.class, BigDecimal.class }, operador, new BigDecimal(valor));
        assertThat(r).isInstanceOf(BigDecimal.class);
        assertThat(r.toString()).isEqualTo(esperado);
    }

    // ------------------------------------------------------------------------------------------------ CUS-FLU-07 historic switch (:1241)

    @ParameterizedTest(name = "CUS-FLU-07 max projected date {0}, system date {1}: historic = {2}")
    @CsvSource({ "2026-02-25, 2026-03-02, true", "2026-03-01, 2026-03-02, true", "2026-03-02, 2026-03-02, false", "2026-03-03, 2026-03-02, false" })
    void leerHistorico(String maxima, String sistema, boolean historico) throws Throwable {
        Object r = FlujoCustodiaContrato.regla("leerHistorico", new Class<?>[] { LocalDate.class, LocalDate.class }, LocalDate.parse(maxima),
                LocalDate.parse(sistema));
        assertThat(r).isEqualTo(historico);
    }

    // ------------------------------------------------------------------------------------------------ helpers

    private static String fuente(String item) throws Throwable {
        return (String) FlujoCustodiaContrato.regla("fuenteDeItem", new Class<?>[] { String.class }, item);
    }

    private static int score(String solicitado, String configurado) throws Throwable {
        JsonObject u = JsonParser.parseString(solicitado.replace('\'', '"')).getAsJsonObject();
        JsonObject c = JsonParser.parseString(configurado.replace('\'', '"')).getAsJsonObject();
        return (Integer) FlujoCustodiaContrato.regla("similitud", new Class<?>[] { JsonObject.class, JsonObject.class }, u, c);
    }

    private static String descarte(String operador, String cartera, String uso, String busqueda, boolean garantizable) throws Throwable {
        return (String) FlujoCustodiaContrato.regla("motivoDescarte",
                new Class<?>[] { String.class, String.class, String.class, String.class, boolean.class }, operador, cartera, uso, busqueda, garantizable);
    }
}
