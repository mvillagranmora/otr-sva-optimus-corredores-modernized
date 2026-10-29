package cl.bcs.fondomutuo.consultas.custodiaffmm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Rule cards of {@code ConsultarCustodiaPortafolioFFMM} (RULES_FONDOMUTUO_F3_C.md 8, FM-CPFF-01..20; defects D-F3C-01..15 and the new candidates D-F3C-16..18; matrix C01..C22 and C24), stated with
 * LITERAL values. The expected values are the ones the unmodified legacy produced (they equal the recorded goldens: this class and {@link FfmmEquivalenciaTest} must agree; if the legacy is
 * re-recorded and a literal changes, the legacy won: update the literal and say so). Each test runs the NEW code on the H2 of its case (slug of {@code cases-ffmm/FM-CPFF-Cnn_slug}) and names the
 * card or defect it pins. Universe: see {@code cases_ffmm_lib.py} / {@code cases_ffmm_a.py} (clients 100 / 101 / 102, the corredora client 99 = person 1; portfolios 500 .. 505; series FM001A ..
 * FM006E with instruments 900 .. 905; PLA.FechaSistema 2026-03-10; the default custody row is 100 quotas of FM001A with purchase value 980000, rate amount 990000 and price 9800).
 * C23 (through CPF) is {@link CadenaEquivalenciaTest}; C24 (the old service) is {@link #dF3c14_servicioAntiguoNoImplementaLaClase}.
 */
class FfmmReglasTest {

    private static JsonObject dump(String slug) throws Exception {
        return CasoFfmm.ejecutar(CasoFfmm.idPorSlug(slug));
    }

    private static JsonArray filas(JsonObject d) {
        assertFalse(d.has("excepcion"), "raised " + d.get("excepcion"));
        return d.getAsJsonObject("salida").getAsJsonArray("p_out_tbl");
    }

    private static JsonArray filas(String slug) throws Exception {
        return filas(dump(slug));
    }

    private static JsonObject fila(String slug) throws Exception {
        JsonArray f = filas(slug);
        assertEquals(1, f.size(), slug + " rows");
        return f.get(0).getAsJsonObject();
    }

    private static String v(JsonObject fila, String k) {
        JsonElement e = fila.get(k);
        return e == null ? "<absent>" : e.isJsonNull() ? "<null>" : e.getAsString();
    }

    private static String campos(JsonObject fila, String... ks) {
        List<String> l = new ArrayList<>();
        for (String k : ks) l.add(v(fila, k));
        return String.join("|", l);
    }

    private static String excepcion(String slug) throws Exception {
        JsonObject d = dump(slug);
        assertTrue(d.has("excepcion"), slug + " did not raise");
        assertEquals("ROLLBACK_HTTP500", d.get("decisionServlet").getAsString());
        return d.get("excepcion").getAsString();
    }

    private static List<String> colaboradores(JsonObject d) {
        List<String> l = new ArrayList<>();
        for (JsonElement c : d.getAsJsonArray("colaboradores")) l.add(c.getAsJsonObject().get("colaborador").getAsString());
        return l;
    }

    /** The groups of the output in output order: instrument/client/portfolio/regime/relation/vault. */
    private static List<String> orden(String slug) throws Exception {
        List<String> l = new ArrayList<>();
        for (JsonElement f : filas(slug)) {
            JsonObject o = f.getAsJsonObject();
            l.add(campos(o, "IdInstrumento", "IdCliente", "IdPortafolio", "IdRegimenFondoMutuo", "Relacion", "IdBoveda").replace("|", "/"));
        }
        return l;
    }

    private static List<String> instrumentos(String slug) throws Exception {
        List<String> l = new ArrayList<>();
        for (JsonElement f : filas(slug)) l.add(v(f.getAsJsonObject(), "IdInstrumento"));
        return l;
    }

    private static final String[] NUMEROS = { "CantidadCustodia", "CantidadGarantia", "CantidadPrecioPromedio", "MontoPrecioPromedio", "INGPrecioPromedioCompra", "INGMontoPrecioPromedioCompra",
            "INGMontoPrecioPromedioMonedaInstrumento", "TotalPosicion", "PPPCustodia" };

    private static String numeros(JsonObject fila) {
        return campos(fila, NUMEROS);
    }

    // ---- FM-CPFF-01 / 02 / 03 contract
    @Test
    @DisplayName("FM-CPFF-01 C01: JSON con solo p_IdEmpresa y la fecha: los defaults del pojo (cliente 0, portafolio 0, instrumento 0, N, N, LIQ) devuelven una fila con bodega y relacion vacias; los 14 campos explicitos dan el mismo texto")
    void fmCpff01_defaultsDelPojo() throws Exception {
        JsonObject d = dump("solo-empresa-y-fecha");
        JsonObject f = fila("solo-empresa-y-fecha");
        assertEquals("100.00000000", v(f, "CantidadCustodia"));
        assertEquals("<null>", v(f, "Producto"));
        assertEquals("", v(f, "IdBoveda"));
        assertEquals("", v(f, "Relacion"));
        assertEquals(List.of("ConsultarEmpresa"), colaboradores(d));
        assertEquals(d.get("salidaCruda").getAsString(), dump("todos-los-campos-explicitos").get("salidaCruda").getAsString());
        assertEquals(d.get("salidaCruda").getAsString(), dump("claves-desconocidas").get("salidaCruda").getAsString());
    }

    @Test
    @DisplayName("FM-CPFF-02: la fila trae 29 campos en el orden de la clase y la salida no tiene contador de errores (D-F3A-27): el anfitrion hace ROLLBACK en toda respuesta")
    void fmCpff02_veintinueveCamposSinContador() throws Exception {
        JsonObject d = dump("custodia-vigente-liq-una-posicion");
        assertEquals("[p_out_tbl]", d.getAsJsonObject("salida").keySet().toString());
        assertEquals("ROLLBACK", d.get("decisionServlet").getAsString());
        assertEquals("[IdClasificacionTipoInstrumento, NombreClasificacionTipoInstrumento, IdTipoInstrumento, NombreTipoInstrumento, IdInstrumento, Nemotecnico, IdEmisor, IdSocioDeNegocio, IdCliente, "
                + "IdPortafolio, DocumentoIdentidad, RazonSocial, CodigoPortafolio, IdBoveda, Relacion, IdRegimenFondoMutuo, RegimenTributario, Producto, CantidadCustodia, CantidadGarantia, "
                + "CantidadPrecioPromedio, MontoPrecioPromedio, INGPrecioPromedioCompra, INGMontoPrecioPromedioCompra, INGMontoPrecioPromedioMonedaInstrumento, TotalPosicion, IdMatrizAgrupador, EsADC, "
                + "PPPCustodia]", filas(d).get(0).getAsJsonObject().keySet().toString());
        assertEquals(29, filas(d).get(0).getAsJsonObject().size());
    }

    @Test
    @DisplayName("FM-CPFF-03: sin datos la respuesta es {\"p_out_tbl\":[]} y lee las tablas sin escribirlas (read only: el volcado de CUS.Cartera no cambia)")
    void fmCpff03_sinDatosYSoloLectura() throws Exception {
        JsonObject d = dump("sin-datos");
        assertEquals("{\"p_out_tbl\":[]}", d.get("salidaCruda").getAsString());
        assertEquals("[]", d.getAsJsonObject("tablas").get("BSCL.Custodia.BD::CUS.Cartera").toString());
        JsonObject c = dump("custodia-vigente-liq-una-posicion");
        assertEquals(1, c.getAsJsonObject("tablas").getAsJsonArray("BSCL.Custodia.BD::CUS.Cartera").size());
        assertEquals("[]", c.getAsJsonObject("sesion").getAsJsonArray("tablasLocalesSinDrop").toString());
        assertEquals("[]", c.getAsJsonObject("sesion").getAsJsonArray("commitsAutonomos").toString());
    }

    // ---- FM-CPFF-04 / D-F3C-03
    @Test
    @DisplayName("FM-CPFF-04 D-F3C-03 C13: p_IdUsuario 999999, p_IdModulo 'XXX' y p_IdBoveda 'DCV' no se leen: mismo resultado que sin ellos (la bodega COR no se filtra)")
    void dF3c03_usuarioModuloYBovedaSeIgnoran() throws Exception {
        String base = dump("claves-desconocidas").get("salidaCruda").getAsString();
        assertEquals(base, dump("usuario-y-modulo-no-se-validan").get("salidaCruda").getAsString());
        assertEquals(base, dump("boveda-como-filtro-se-ignora").get("salidaCruda").getAsString());
        assertEquals(List.of("ConsultarEmpresa"), colaboradores(dump("usuario-y-modulo-no-se-validan")));
    }

    // ---- FM-CPFF-05 / D-F3C-04 / D-F3C-12 errors
    @Test
    @DisplayName("FM-CPFF-05 C03 C04: orden de lectura y errores: ConsultarEmpresa primero ('Empresa no existe' sin otra consulta), luego PLA.FechaSistema (mensaje del parametro), luego la fecha de entrada (nula => NPE, mal formada => DateTimeParseException)")
    void fmCpff05_ordenDeErrores() throws Exception {
        assertEquals("Exception: Empresa no existe", excepcion("empresa-inexistente"));
        assertEquals(List.of("ConsultarEmpresa"), colaboradores(dump("empresa-inexistente")));
        assertEquals("Exception: Parametro PLA.FechaSistema en modulo PLATAFORMA no encontrado", excepcion("sin-fecha-de-sistema"));
        assertEquals(List.of("ConsultarEmpresa"), colaboradores(dump("sin-fecha-de-sistema")));
        assertTrue(excepcion("fecha-nula").startsWith("NullPointerException:"));
        assertEquals(List.of("ConsultarEmpresa"), colaboradores(dump("fecha-nula")));
        assertTrue(excepcion("fecha-mal-formada").startsWith("DateTimeParseException: Text '10/03/2026' could not be parsed"));
        assertTrue(excepcion("fecha-vacia").startsWith("DateTimeParseException: Text '' could not be parsed"));
        assertTrue(excepcion("fecha-sin-ceros").startsWith("DateTimeParseException: Text '2026-3-10' could not be parsed"));
        assertTrue(excepcion("fecha-de-sistema-invalida").startsWith("DateTimeParseException: Text 'hoy' could not be parsed"));
        assertTrue(excepcion("fecha-como-numero").startsWith("DateTimeParseException: Text '20260310' could not be parsed"));
    }

    @Test
    @DisplayName("FM-CPFF-05 D-NOR-9 DESVIO-KIT-1: empresa nula / ausente / sin configuracion: el legacy lanza NullPointerException; el kit lanza ParametroNoEncontradoException (se mantiene la del kit: se aceptan las dos SOLO en estos slugs)")
    void desvioKit1_empresaSinConfiguracion() throws Exception {
        for (String slug : new String[] { "empresa-nula", "empresa-ausente", "empresa-sin-configuracion" }) {
            assertTrue(CasoFfmm.golden(CasoFfmm.idPorSlug(slug)).get("excepcion").getAsString().startsWith("NullPointerException:"), slug + " golden");
            String e = excepcion(slug);
            assertTrue(e.startsWith("NullPointerException:") || (e.startsWith("Exception: Parametro ") && e.endsWith(" no encontrado")), slug + ": " + e);
            assertEquals(List.of("ConsultarEmpresa"), colaboradores(dump(slug)));
        }
        assertEquals("[null]", CasoFfmm.golden(CasoFfmm.idPorSlug("empresa-nula")).getAsJsonArray("colaboradores").get(0).getAsJsonObject().getAsJsonArray("argumentos").toString());
    }

    @Test
    @DisplayName("FM-CPFF-05 D-F3C-16: '2026-02-30' NO falla (el formateador yyyy-MM-dd lo lleva al 2026-02-28: fuente historica) y el texto crudo viaja al SQL: vacio; '2026-03-10 00:00:00' (19 caracteres) se parsea pero el texto con hora hace fallar el SQL vigente")
    void dF3c16_fechasRaras() throws Exception {
        assertEquals("{\"p_out_tbl\":[]}", dump("fecha-inexistente").get("salidaCruda").getAsString());
        assertTrue(excepcion("fecha-con-hora").startsWith("SQLException:"));
        assertEquals(1, filas("fecha-de-sistema-con-hora").size());
    }

    // ---- FM-CPFF-07 / D-F3C-04 / D-F3C-05 the date and the source
    @Test
    @DisplayName("FM-CPFF-07 C02 D-F3C-04: la fecha por defecto '0001-01-01' llega CRUDA al SQL VIGENTE (no se reemplaza por PLA.FechaSistema): vacio con custodia y solicitud presentes; explicita o con ADQ igual; SOLO sale lo vigente desde el origen (ingreso '0001-01-01'), y nunca el respaldo del dia cero")
    void dF3c04_fechaPorDefectoDaVacio() throws Exception {
        for (String slug : new String[] { "fecha-por-defecto-llamada-directa", "fecha-cero-explicita", "fecha-cero-con-adq", "fecha-cero-con-sistema-cero" }) {
            assertEquals("{\"p_out_tbl\":[]}", dump(slug).get("salidaCruda").getAsString(), slug);
        }
        assertEquals("900|100.00000000|5.00000000", campos(fila("fecha-cero-con-posicion-desde-el-origen"), "IdInstrumento", "CantidadCustodia", "CantidadGarantia"));
        assertEquals(List.of("901"), instrumentos("fecha-cero-usa-la-fuente-vigente"));
    }

    @Test
    @DisplayName("FM-CPFF-07: fecha >= PLA.FechaSistema => vigente (con 'Regimen General'); fecha anterior (1 dia, 3 dias, 2 meses, 1 anio) => historico (RegimenTributario ''); la comparacion es 'no es anterior', no 'compareTo != -1' de LocalDate")
    void fmCpff07_fuenteVigenteOHistorica() throws Exception {
        assertEquals("900|Regimen General", campos(fila("fecha-igual-al-sistema-es-vigente"), "IdInstrumento", "RegimenTributario"));
        assertEquals("901|", campos(fila("un-dia-antes-del-sistema-es-historico"), "IdInstrumento", "RegimenTributario"));
        assertEquals(List.of("901"), instrumentos("un-dia-antes-del-sistema-es-historico"));
        assertEquals(List.of("901"), instrumentos("historico-tres-dias-antes"));
        assertEquals(List.of("901"), instrumentos("historico-dos-meses-antes"));
        assertEquals(List.of("901"), instrumentos("historico-un-anio-antes"));
        assertEquals(List.of("900"), instrumentos("vigente-tres-dias-despues"));
        assertEquals(List.of("900"), instrumentos("fecha-futura-usa-vigente"));
        assertEquals("{\"p_out_tbl\":[]}", dump("fecha-historica-sin-respaldo").get("salidaCruda").getAsString());
        assertEquals(List.of("900"), instrumentos("sistema-posterior-todo-historico"));
        assertEquals("", v(filas("sistema-posterior-todo-historico").get(0).getAsJsonObject(), "RegimenTributario"));
    }

    // ---- FM-CPFF-08 current custody
    @Test
    @DisplayName("FM-CPFF-08 FM-CPFF-13 FM-CPFF-15 C05 (ejemplo A): 100 cuotas, valor 980000, monto 990000, precio 9800: los 9 numeros con su ESCALA exacta (BigDecimal.toString) y los descriptivos del cliente, portafolio y matriz")
    void fmCpff15_ejemploA() throws Exception {
        JsonObject f = fila("custodia-vigente-liq-una-posicion");
        assertEquals("100.00000000|0|100.00000000|980000.0000000000000000|9800|990000.00000000|980000.00000000|100.00000000|9800.00000000", numeros(f));
        assertEquals("1100|11111111-1|Cliente Ficticio Uno|PF500|8|N|Regimen General|<null>", campos(f, "IdSocioDeNegocio", "DocumentoIdentidad", "RazonSocial", "CodigoPortafolio", "IdMatrizAgrupador", "EsADC", "RegimenTributario", "Producto"));
        assertEquals("FM|Fondos Mutuos|FFM|Fondo Mutuo|900|FM001A|AGF1|100|500|GEN", campos(f, "IdClasificacionTipoInstrumento", "NombreClasificacionTipoInstrumento", "IdTipoInstrumento", "NombreTipoInstrumento",
                "IdInstrumento", "Nemotecnico", "IdEmisor", "IdCliente", "IdPortafolio", "IdRegimenFondoMutuo"));
    }

    @Test
    @DisplayName("FM-CPFF-13 C06 D-F3C-11: dos filas del mismo grupo (60 y 40 cuotas, precios 10000 y 9500) se suman: 100 cuotas, MontoPrecioPromedio = 60*10000 + 40*9500 = 980000, PPP 9800; las bodegas distintas se suman con N")
    void fmCpff13_acumulacion() throws Exception {
        assertEquals("100.00000000|0|100.00000000|980000.0000000000000000|9800|990000.00000000|980000.00000000|100.00000000|9800.00000000", numeros(fila("dos-filas-mismo-grupo")));
        // two rows of the default values (purchase value 980000 and rate amount 990000 EACH): the amounts double, the price amount is 60*9800 + 40*9800
        assertEquals("100.00000000|0|100.00000000|980000.0000000000000000|19600|1980000.00000000|1960000.00000000|100.00000000|9800.00000000", numeros(fila("dos-filas-bodegas-distintas-se-suman")));
        assertEquals("33.33333333|0|33.33333333|1000004.1151259995884774|30000.01500300000150030000015003000|1000001.250000000000000000000000000|1000000.500000000000000000000000000|33.33333333|30000.12345678",
                numeros(fila("decimales")));
        assertEquals("0.60000000|0|0.60000000|5880.0000000000000000|4900000|2970000.00000000|2940000.00000000|0.60000000|9800.00000000", numeros(fila("suma-de-cantidades-con-escala")));
    }

    @Test
    @DisplayName("FM-CPFF-15 (ejemplo C/D) y cantidad cero: sin custodia ni garantia (solicitud) todo 0 y PPP nulo; custodia de 0 cuotas: ING... en 0 sin dividir y PPP nulo; negativa: se divide con signo")
    void fmCpff15_cantidadesCeroYNegativas() throws Exception {
        assertEquals("0|0|0|0|0|0|0|0|<null>", numeros(fila("solicitud-sin-cartera-de-la-serie")));
        assertEquals("0E-8|0|0E-8|0E-16|0|0|0|0E-8|<null>", numeros(fila("custodia-cantidad-cero")));
        assertEquals("-10.00000000|0|-10.00000000|-98000.0000000000000000|9800|-99000.00000000|-98000.00000000|-10.00000000|9800.00000000", numeros(fila("custodia-negativa")));
    }

    @Test
    @DisplayName("FM-CPFF-08 QD-F3C-06 C16: el egreso por liquidacion es EXCLUSIVO (egreso = consulta no sale; egreso el dia siguiente si) y el ingreso es inclusivo; por LIQ/ADQ; tipo desconocido, minuscula o nulo: la custodia no sale")
    void fmCpff08_vigenciaYTipoDeFecha() throws Exception {
        assertEquals(List.of("901", "904"), instrumentos("egreso-igual-a-la-consulta-excluida"));
        assertEquals(List.of("901"), instrumentos("adq-posicion-abierta-y-cerrada"));
        assertEquals("{\"p_out_tbl\":[]}", dump("adq-usa-fecha-de-operacion").get("salidaCruda").getAsString());
        assertEquals(1, filas("liq-misma-posicion-que-adq").size());
        assertEquals(List.of("900"), instrumentos("tipo-fecha-xxx-solo-solicitudes"));
        assertEquals("0", v(fila("tipo-fecha-xxx-solo-solicitudes"), "CantidadCustodia"));
        assertEquals("0", v(fila("tipo-fecha-nulo"), "CantidadCustodia"));
        assertEquals("901", v(fila("tipo-fecha-minuscula"), "IdInstrumento"));
    }

    @Test
    @DisplayName("D-F3C-05: en modo ADQ el limite superior prueba INGFechaOperacion (no EGRFechaOperacion): la posicion ABIERTA nunca sale, solo la cerrada despues de la fecha (vigente e historico)")
    void dF3c05_adqNuncaDevuelvePosicionesAbiertas() throws Exception {
        assertEquals(List.of("901"), instrumentos("adq-posicion-abierta-y-cerrada"));
        assertEquals(List.of("901"), instrumentos("historico-adq"));
        assertEquals("{\"p_out_tbl\":[]}", dump("historico-tipo-xxx").get("salidaCruda").getAsString());
    }

    // ---- FM-CPFF-09 / 20 historical
    @Test
    @DisplayName("FM-CPFF-09 C17: respaldo del dia: del FolioCartera 77 solo la version de IdCartera MAYOR (40 cuotas, no 100); el empate de IdCartera suma las dos filas; el folio nulo es una sola particion; IdEstado <> 'ANU'")
    void fmCpff09_ultimaVersionPorFolio() throws Exception {
        List<String> g = orden("historico-ultima-version-por-folio");
        assertEquals(2, g.size());
        for (JsonElement f : filas("historico-ultima-version-por-folio")) {
            JsonObject o = f.getAsJsonObject();
            assertEquals("900".equals(v(o, "IdInstrumento")) ? "40.00000000" : "7.00000000", v(o, "CantidadCustodia"));
        }
        assertEquals("30.00000000", v(fila("historico-empate-de-idcartera"), "CantidadCustodia"));
        assertEquals("20.00000000", v(fila("historico-folio-nulo"), "CantidadCustodia"));
        assertEquals(List.of("901", "903"), instrumentos("historico-estado").stream().sorted().toList());
        assertEquals("{\"p_out_tbl\":[]}", dump("historico-estado-nulo").get("salidaCruda").getAsString());
        assertEquals("{\"p_out_tbl\":[]}", dump("historico-respaldo-con-hora").get("salidaCruda").getAsString());
        assertEquals(List.of("901"), instrumentos("historico-respaldo-de-otro-dia"));
        assertEquals(List.of("901"), instrumentos("historico-sin-socio-se-omite"));
    }

    @Test
    @DisplayName("FM-CPFF-09 FM-CPFF-20 C18 D-F3C-07: el historico NO trae Producto ni RegimenTributario (nulo y ''), la vigente si ('Producto APV tres' y 'Regimen General')")
    void dF3c07_historicoSinProductoNiRegimen() throws Exception {
        assertEquals("<null>||501", campos(fila("historico-producto-nulo-regimen-vacio"), "Producto", "RegimenTributario", "IdPortafolio"));
        assertEquals("Producto APV tres|Regimen General|S|7", campos(fila("vigente-producto-y-regimen"), "Producto", "RegimenTributario", "EsADC", "IdMatrizAgrupador"));
    }

    @Test
    @DisplayName("FM-CPFF-09 C18 D-F3C-06: la rama de solicitudes historica exige ':IdEmisor = :IdEmisor': con emisor nulo desaparece COMPLETA (solo sale la cartera); con emisor informado aparece aunque ninguna cartera sea de ese emisor; sin JOIN de regimen")
    void dF3c06_ramaDeSolicitudesHistorica() throws Exception {
        assertEquals("900|100.00000000", campos(fila("historico-solicitud-emisor-nulo-desaparece"), "IdInstrumento", "CantidadCustodia"));
        assertEquals("901|0|", campos(fila("historico-solicitud-emisor-informado-aparece"), "IdInstrumento", "CantidadCustodia", "RegimenTributario"));
        assertEquals("900|BF|", campos(fila("historico-solicitud-sin-regimen-descripcion"), "IdInstrumento", "IdRegimenFondoMutuo", "RegimenTributario"));
        assertEquals("{\"p_out_tbl\":[]}", dump("historico-solicitud-excluida-por-cartera-vigente").get("salidaCruda").getAsString());
        assertEquals(List.of("900"), instrumentos("historico-solicitud-instrumento-informado"));
    }

    @Test
    @DisplayName("FM-CPFF-13 C21 D-F3C-11 D-NOR-9: ValorCompra o MontoCompra NULOS (el respaldo los permite) => NullPointerException en la suma, tambien en la segunda fila del grupo")
    void dF3c11_valorOMontoNulosLanzanNpe() throws Exception {
        for (String slug : new String[] { "valor-compra-nulo-npe", "monto-compra-nulo-npe", "valor-compra-nulo-en-segunda-fila" }) {
            assertTrue(excepcion(slug).startsWith("NullPointerException:"), slug);
        }
        assertEquals("0E-8|0E-8", campos(fila("historico-cantidad-nula"), "CantidadCustodia", "CantidadPrecioPromedio"));
        assertEquals("<null>", v(fila("historico-cantidad-nula"), "PPPCustodia"));
        assertEquals("0E-8|0", campos(fila("historico-precio-nulo"), "MontoPrecioPromedio", "PPPCustodia"));
    }

    // ---- FM-CPFF-10 / 14 / D-F3C-01 / 02 / 08 guarantees
    @Test
    @DisplayName("FM-CPFF-14 FM-CPFF-15 C07 D-F3C-01 D-F3C-02 (ejemplo B): custodia 100 + garantia 10: CantidadPrecioPromedio 110 (mezcla unidades), MontoPrecioPromedio 0 (se reinicia: la garantia agrupada no trae monto de compra), PPPCustodia 0 (0E+8), TotalPosicion 110")
    void dF3c01_pppEnCeroConGarantia() throws Exception {
        assertEquals("100.00000000|10.00000000|110.00000000|0|9800|990000.00000000|980000.00000000|110.00000000|0E+8", numeros(fila("custodia-100-mas-garantia-10")));
        assertEquals("100.00000000|-4.00000000|96.00000000|0|9800|990000.00000000|980000.00000000|96.00000000|0E+8", numeros(fila("garantia-negativa")));
        assertEquals("100.00000000|30.00000000|130.00000000|0|9800|990000.00000000|980000.00000000|130.00000000|0E+8", numeros(fila("garantia-con-varias-filas-se-suma")));
        assertEquals("2.50000000|0.50000000|3.00000000|0|392000|990000.00000000|980000.00000000|3.00000000|0E+8", numeros(fila("precio-promedio-con-garantia-y-custodia-fraccionaria")));
        assertEquals("100.00000000|10.00000000|110.00000000|0|9800|990000.00000000|980000.00000000|110.00000000|0E+8", numeros(fila("garantia-historica-con-egrmonto")));
    }

    @Test
    @DisplayName("FM-CPFF-14 FM-CPFF-15 C08 D-F3C-08 (ejemplo C): solo garantia de 25 (la cartera origen esta cerrada): custodia 0, RegimenTributario NULO (la garantia no lo tiene) y PPP 0; garantia de 0 cuotas: PPP nulo")
    void dF3c08_soloGarantiaSinRegimenTributario() throws Exception {
        JsonObject f = fila("solo-garantia-25");
        assertEquals("0|25.00000000|25.00000000|0|0|0|0|25.00000000|0E+8", numeros(f));
        assertEquals("<null>|GEN", campos(f, "RegimenTributario", "IdRegimenFondoMutuo"));
        assertEquals("<null>", v(fila("garantia-con-cantidad-cero"), "PPPCustodia"));
        assertEquals("0E-8", v(fila("garantia-con-cantidad-cero"), "CantidadGarantia"));
        assertEquals("<null>|9.00000000", campos(fila("garantia-historica-solo-garantia"), "RegimenTributario", "CantidadGarantia"));
        assertEquals("0E-8|<null>", campos(fila("garantia-historica-cantidad-nula"), "CantidadGarantia", "PPPCustodia"));
    }

    @Test
    @DisplayName("FM-CPFF-10: la garantia se filtra por estado <> ANU, clasificacion FM, tipo FFM, empresa, vigencia por liquidacion (egreso exclusivo), INNER JOIN con la cartera origen y con el socio de negocio; bodega, regimen y emisor del filtro son los de la cartera origen")
    void fmCpff10_filtrosDeGarantia() throws Exception {
        assertEquals("3.00000000", v(fila("garantia-estado-anulado"), "CantidadGarantia"));
        assertEquals("12.00000000", v(fila("garantia-vigencia"), "CantidadGarantia"));
        assertEquals("0", v(fila("garantia-clasificacion-y-tipo"), "CantidadGarantia"));
        assertEquals("0", v(fila("garantia-sin-socio-de-negocio"), "CantidadGarantia"));
        assertEquals("<null>", v(fila("garantia-sin-socio-de-negocio"), "IdSocioDeNegocio"));
        assertEquals("0", v(fila("garantia-empresa-distinta"), "CantidadGarantia"));
        assertEquals("0", v(fila("garantia-cartera-origen-inexistente"), "CantidadGarantia"));
        assertEquals("AGF2|6.00000000", campos(fila("garantia-emisor-de-la-cartera-origen"), "IdEmisor", "CantidadGarantia"));
        assertEquals("4.00000000", v(fila("garantia-historica-estado-y-vigencia"), "CantidadGarantia"));
        assertEquals("0", v(fila("garantia-historica-respaldo-distinto"), "CantidadGarantia"));
        assertEquals(List.of("901"), instrumentos("garantia-historica-respaldo-distinto"));
    }

    @Test
    @DisplayName("FM-CPFF-14 D-F3C-08: la garantia de una cartera BF y la custodia GEN son DOS grupos (la clave lleva el regimen de la cartera); el de solo garantia sale con RegimenTributario nulo; con S la bodega DCV de la garantia separa del COR de la custodia")
    void fmCpff14_universoEsLaUnionDeClaves() throws Exception {
        List<String> g = orden("garantia-regimen-distinto-grupo-aparte");
        assertEquals(List.of("900/100/500/GEN//", "900/100/500/BF//"), g);
        JsonArray f = filas("garantia-regimen-distinto-grupo-aparte");
        assertEquals("100.00000000|0|Regimen General", campos(f.get(0).getAsJsonObject(), "CantidadCustodia", "CantidadGarantia", "RegimenTributario"));
        assertEquals("0|5.00000000|<null>", campos(f.get(1).getAsJsonObject(), "CantidadCustodia", "CantidadGarantia", "RegimenTributario"));
        assertEquals(List.of("900/100/500/BF//DCV", "900/100/500/GEN//COR"), orden("garantia-bodega-y-regimen-de-la-cartera-origen"));
        assertEquals(List.of("901", "900"), instrumentos("garantia-de-otro-instrumento"));
    }

    @Test
    @DisplayName("FM-CPFF-14 D-F3C-08: un grupo de SOLO garantia del portafolio APV 501 toma Producto, EsADC e IdMatrizAgrupador de la garantia (RegimenTributario sigue nulo); la garantia historica exige el respaldo del dia y estado <> ANU (LIQ entra)")
    void fmCpff14_soloGarantiaDelPortafolioApvYGarantiaHistorica() throws Exception {
        assertEquals("Producto APV tres|S|7|<null>|8.00000000", campos(fila("solo-garantia-de-portafolio-apv"), "Producto", "EsADC", "IdMatrizAgrupador", "RegimenTributario", "CantidadGarantia"));
        assertEquals("0", v(fila("garantia-historica-de-otro-respaldo-no-sale"), "CantidadGarantia"));
        assertEquals("3.00000000", v(fila("garantia-historica-estado-liq-entra"), "CantidadGarantia"));
    }

    // ---- FM-CPFF-11 / 12 relation, vault, key
    @Test
    @DisplayName("FM-CPFF-11 C12: con p_AgrupaPorRelacion S: cliente = IdCliente de la empresa => CPRO, persona con relacion ACTIVA => REL, resto TER (tambien la garantia y la solicitud); con N, 's' o nulo: relacion '' (y REL.ConsultarRelacionPersona se consulta igual)")
    void fmCpff11_relaciones() throws Exception {
        assertEquals(List.of("900/101/502/GEN/REL/", "900/99/504/GEN/CPRO/", "900/100/500/GEN/TER/"), orden("agrupa-por-relacion-s"));
        assertEquals("30.00000000|3.00000000", campos(filas("agrupa-por-relacion-s").get(2).getAsJsonObject(), "CantidadCustodia", "CantidadGarantia"));
        assertEquals(List.of("900/101/502/GEN//", "900/100/500/GEN//", "900/99/504/GEN//"), orden("agrupa-por-relacion-n"));
        assertEquals(orden("agrupa-por-relacion-n"), orden("agrupa-por-relacion-minuscula"));
        assertEquals(List.of("900/101/502/GEN/REL/", "900/102/505/GEN/TER/"), orden("relacion-inactiva-es-tercero"));
        assertEquals(List.of("900/99/504/GEN/CPRO/", "901/101/502/GEN/REL/"), orden("solicitud-con-relacion-cpro"));
        assertEquals(List.of("900/101/502/GEN/REL/", "900/100/500/GEN/CPRO/"), orden("empresa-con-otro-cliente"));
        assertEquals(List.of("900/101/502/GEN/TER/"), orden("empresa-con-otra-persona"));
        assertEquals(List.of("900/104/0/GEN/TER/"), orden("persona-del-cliente-nula-es-tercero"));
        assertEquals(List.of("900/101/502/GEN/REL/"), orden("personas-destino-repetida-distinct"));
    }

    @Test
    @DisplayName("FM-CPFF-11 D-NOR-9 D-F3C-12 D-F3C-18: las personas relacionadas se consultan SIEMPRE: una fila activa con IdPersonaDestino NULL NO falla (la clave nula entra al mapa; la tarjeta suponia NPE); empresa sin IdPersona: se consulta con NULL")
    void dF3c18_personaDestinoNula() throws Exception {
        assertEquals(1, filas("personas-destino-persona-nula-npe").size());
        assertEquals(1, filas("empresa-sin-persona").size());
    }

    @Test
    @DisplayName("FM-CPFF-12 C11: p_AgrupaPorBoveda S: dos grupos DCV (60) y COR (40); N, 's' minuscula, nulo o el numero 1: un grupo de 100 con IdBoveda ''; la solicitud (bodega '') es su propio grupo")
    void fmCpff12_agrupaPorBoveda() throws Exception {
        assertEquals(List.of("900/100/500/GEN//DCV", "900/100/500/GEN//COR"), orden("agrupa-por-boveda-s"));
        assertEquals("60.00000000|40.00000000", filas("agrupa-por-boveda-s").get(0).getAsJsonObject().get("CantidadCustodia").getAsString() + "|" + filas("agrupa-por-boveda-s").get(1).getAsJsonObject().get("CantidadCustodia").getAsString());
        for (String slug : new String[] { "agrupa-por-boveda-n", "agrupa-por-boveda-minuscula", "agrupa-por-boveda-nula" }) {
            assertEquals("100.00000000|", v(fila(slug), "CantidadCustodia") + "|" + v(fila(slug), "IdBoveda"), slug);
        }
        assertEquals(List.of("901/100/500/GEN//", "900/100/500/GEN//DCV"), orden("agrupa-por-boveda-con-solicitud"));
        assertEquals("100.00000000|5.00000000|", campos(fila("agrupa-por-boveda-minuscula-con-garantia"), "CantidadCustodia", "CantidadGarantia", "IdBoveda"));
        assertEquals(List.of("900/99/504/GEN/CPRO/DCV", "900/99/504/GEN/CPRO/COR"), orden("relacion-y-boveda-juntas"));
    }

    @Test
    @DisplayName("FM-CPFF-12 D-F3C-09 D-F3C-13: la clave es instrumento_nemotecnico_emisor_cliente_portafolio_regimen: el nemotecnico, el emisor y el regimen (incluso nulo) separan grupos; los descriptivos son los de la primera fila")
    void dF3c09_claveDeAgrupacion() throws Exception {
        assertEquals(2, filas("mismo-instrumento-nemotecnico-distinto-grupo").size());
        assertEquals(List.of("FM001X", "FM001A"), nemotecnicos("mismo-instrumento-nemotecnico-distinto-grupo"));
        assertEquals(List.of("AGF2", "AGF1"), emisores("mismo-instrumento-emisor-distinto-grupo"));
        assertEquals(List.of("GEN", "<null>"), regimenes("regimen-nulo-y-regimen-informado-dos-grupos"));
        assertEquals("", v(filas("regimen-nulo-y-regimen-informado-dos-grupos").get(1).getAsJsonObject(), "RegimenTributario"));
        assertEquals("ZZZ|", campos(fila("regimen-sin-descripcion"), "IdRegimenFondoMutuo", "RegimenTributario"));
    }

    private static List<String> nemotecnicos(String slug) throws Exception {
        List<String> l = new ArrayList<>();
        for (JsonElement f : filas(slug)) l.add(v(f.getAsJsonObject(), "Nemotecnico"));
        return l;
    }

    private static List<String> emisores(String slug) throws Exception {
        List<String> l = new ArrayList<>();
        for (JsonElement f : filas(slug)) l.add(v(f.getAsJsonObject(), "IdEmisor"));
        return l;
    }

    private static List<String> regimenes(String slug) throws Exception {
        List<String> l = new ArrayList<>();
        for (JsonElement f : filas(slug)) l.add(v(f.getAsJsonObject(), "IdRegimenFondoMutuo"));
        return l;
    }

    @Test
    @DisplayName("D-F3C-09 (candidato): los JOIN sin clave unica DUPLICAN la fila y por tanto la cantidad: dos series del mismo instrumento, dos matrices NOAPV con el mismo codigo, dos socios de la misma persona, dos filas de cliente en la vista, el portafolio repetido: 200 cuotas")
    void dF3c09_joinsQueDuplican() throws Exception {
        for (String slug : new String[] { "dos-series-mismo-instrumento-duplican", "dos-matrices-noapv-mismo-codigo-duplican", "dos-socios-misma-persona-duplican", "dos-filas-de-cliente-misma-vista",
                "codigo-de-portafolio-distinto-misma-clave" }) {
            assertEquals("200.00000000", v(fila(slug), "CantidadCustodia"), slug);
            // the descriptive fields are the ones of the FIRST row seen (the key does not include them: D-F3C-09)
            assertEquals("Cliente Ficticio Uno|1100|PF500|N|8", campos(fila(slug), "RazonSocial", "IdSocioDeNegocio", "CodigoPortafolio", "EsADC", "IdMatrizAgrupador"), slug);
        }
        assertEquals(List.of("901"), instrumentos("instrumento-sin-serie-no-sale"));
        assertEquals(List.of("901"), instrumentos("instrumento-fuera-de-ins-instrumento-no-sale"));
    }

    // ---- FM-CPFF-06 filters
    @Test
    @DisplayName("FM-CPFF-06 C20: producto 3 => solo el portafolio APV 501; producto 0 => los sin producto (MAP: nulo = 0); nulo => todos; 99 => ninguno; familia FAM2 => 901 y 904; '' o nula => todas; cliente / portafolio / instrumento: > 0 filtra, 0, nulo o negativo no")
    void fmCpff06_filtros() throws Exception {
        assertEquals(List.of("501"), List.of(v(fila("producto-agrupador-informado"), "IdPortafolio")));
        assertEquals(List.of("502", "500"), List.of(v(filas("producto-agrupador-cero").get(0).getAsJsonObject(), "IdPortafolio"), v(filas("producto-agrupador-cero").get(1).getAsJsonObject(), "IdPortafolio")));
        assertEquals(2, filas("producto-agrupador-nulo").size());
        assertEquals(0, filas("producto-agrupador-inexistente").size());
        assertEquals(List.of("901", "904"), instrumentos("familia-informada"));
        assertEquals(2, filas("familia-vacia-sin-filtro").size());
        assertEquals(2, filas("familia-nula-sin-filtro").size());
        assertEquals(1, filas("cliente-filtra").size());
        assertEquals(2, filas("cliente-nulo-sin-filtro").size());
        assertEquals(2, filas("cliente-negativo-sin-filtro").size());
        assertEquals("10.00000000", v(fila("portafolio-filtra"), "CantidadGarantia"));
        assertEquals("901", v(fila("instrumento-filtra"), "IdInstrumento"));
        assertEquals(2, filas("instrumento-nulo-sin-filtro").size());
        assertEquals(2, filas("instrumento-negativo-sin-filtro").size());
        assertEquals(List.of("903", "901"), instrumentos("emisor-filtra"));
        assertEquals(0, filas("emisor-vacio-no-es-sin-filtro").size());
        assertEquals(List.of("900"), instrumentos("estado-vigente-solo-vig"));
        assertEquals(List.of("900"), instrumentos("clasificacion-y-tipo-vigente"));
        assertEquals(List.of("900"), instrumentos("empresa-distinta-no-sale"));
    }

    // ---- C09 / C10 requests in transit
    @Test
    @DisplayName("FM-CPFF-08 C09 C10 D-F3C-06: la solicitud en transito sale DENTRO de la custodia (cantidad 0, bodega ''); la rama vigente NO aplica IdInstrumento, IdEmisor ni IdFamiliaFondo (si cliente, portafolio y producto); no distingue aporte de rescate")
    void dF3c06_solicitudesIgnoranInstrumentoEmisorYFamilia() throws Exception {
        assertEquals(List.of("901", "900"), instrumentos("solicitud-de-otro-instrumento-con-filtro"));
        assertEquals("100.00000000", v(filas("solicitud-de-otro-instrumento-con-filtro").get(0).getAsJsonObject(), "CantidadCustodia"));
        assertEquals("0", v(filas("solicitud-de-otro-instrumento-con-filtro").get(1).getAsJsonObject(), "CantidadCustodia"));
        assertEquals(List.of("901"), instrumentos("solicitud-ignora-emisor-y-familia"));
        assertEquals(List.of("901"), instrumentos("familia-inexistente"));
        assertEquals(List.of("901"), instrumentos("solicitud-estado-pen"));
        assertEquals(List.of("901"), instrumentos("solicitud-fecha-de-aceptacion"));
        assertEquals(List.of("901"), instrumentos("solicitud-rescate-igual-que-aporte"));
        assertEquals(List.of("900"), instrumentos("dos-solicitudes-misma-serie-se-suman"));
        assertEquals(List.of("901"), instrumentos("solicitud-filtra-cliente-y-portafolio"));
        assertEquals(List.of("901"), instrumentos("solicitud-filtra-solo-cliente"));
        assertEquals(List.of("901"), instrumentos("solicitud-filtra-solo-portafolio"));
        assertEquals(List.of("901"), instrumentos("solicitud-filtra-producto"));
        assertEquals("900|BF|Beneficio Fiscal", campos(fila("solicitud-con-regimen-bf"), "IdInstrumento", "IdRegimenFondoMutuo", "RegimenTributario"));
        assertEquals(List.of("901"), instrumentos("solicitud-tipo-calculo-total-no-se-excluye"));
    }

    @Test
    @DisplayName("FM-CPFF-08 FM-CPFF-22.1: el NOT IN de las solicitudes mira la cartera VIGENTE del mismo CLIENTE (cualquier estado, cualquier portafolio, sin importar el filtro): la excluye la cartera vigente, la anulada y la de otro portafolio; no la de otro cliente ni la cerrada")
    void fmCpff08_notInDeLasSolicitudes() throws Exception {
        assertEquals(List.of("900"), instrumentos("solicitud-cartera-vigente-de-la-serie-la-excluye"));
        assertEquals("100.00000000", v(fila("solicitud-cartera-vigente-de-la-serie-la-excluye"), "CantidadCustodia"));
        assertEquals(2, filas("solicitud-cartera-de-otro-cliente-no-la-excluye").size());
        assertEquals("0", v(fila("solicitud-cartera-cerrada-no-la-excluye"), "CantidadCustodia"));
        assertEquals("501", v(fila("solicitud-cartera-de-otro-portafolio-la-excluye"), "IdPortafolio"));
        assertEquals(0, filas("solicitud-cartera-anulada-la-excluye").size());
        assertEquals(0, filas("solicitud-de-la-empresa-2-no-sale").size());
        assertEquals("<null>|0|<null>", campos(fila("solicitud-serie-inexistente"), "IdInstrumento", "CantidadCustodia", "IdEmisor"));
        assertEquals("<null>|<null>|<null>", campos(fila("solicitud-cliente-fuera-de-la-vista"), "DocumentoIdentidad", "RazonSocial", "IdSocioDeNegocio"));
        assertEquals("<null>|0|N", campos(fila("solicitud-portafolio-fuera-de-la-vista"), "CodigoPortafolio", "IdMatrizAgrupador", "EsADC"));
        assertEquals("0|12.00000000|0E+8|Regimen General", campos(fila("solicitud-y-garantia-mismo-grupo"), "CantidadCustodia", "CantidadGarantia", "PPPCustodia", "RegimenTributario"));
    }

    // ---- numbers
    @Test
    @DisplayName("FM-CPFF-15 D-F3C-10: (X / Cantidad) * Cantidad con DECIMAL128 NO vuelve a X: 1000/3 = 333.3333333333333333333333333333333 (34 digitos) y su producto 999.9999999999999999999999999999999; el monto de tasa 2000.000000000000000000000000000000")
    void dF3c10_divisionNoExacta() throws Exception {
        assertEquals("3.00000000|0|3.00000000|999.9999999900000000|333.3333333333333333333333333333333|2000.000000000000000000000000000000|999.9999999999999999999999999999999|3.00000000|333.33333333",
                numeros(fila("division-no-exacta-decimal128")));
        assertEquals("3.00000000|7.00000000|10.00000000|0|333.3333333333333333333333333333333|2000.000000000000000000000000000000|999.9999999999999999999999999999999|10.00000000|0E+8", numeros(fila("division-no-exacta-con-garantia")));
        assertEquals("1E-8|1.00000000E-8|1|3E-8|1E-8|1.00000000", campos(fila("cantidad-minima"), "CantidadCustodia", "MontoPrecioPromedio", "INGPrecioPromedioCompra", "INGMontoPrecioPromedioCompra",
                "INGMontoPrecioPromedioMonedaInstrumento", "PPPCustodia"));
    }

    @Test
    @DisplayName("D-F3C-11: con el maximo de DECIMAL(26,8) (999999999999999999.99999999) la suma y el producto se redondean a 34 digitos (DECIMAL128): MontoPrecioPromedio sale 999999999999999999999999980000000000 (escala negativa) y PPP 999999999999999999.9999999900000000")
    void dF3c11_maximosRedondeanADecimal128() throws Exception {
        JsonObject f = fila("maximos-de-decimal-26-8");
        assertEquals("999999999999999999.99999999|0|999999999999999999.99999999|999999999999999999999999980000000000|1|999999999999999999.99999999|999999999999999999.99999999|999999999999999999.99999999|999999999999999999.9999999900000000", numeros(f));
    }

    // ---- C22 order
    @Test
    @DisplayName("FM-CPFF-16 C22 D-F3C-13: el orden de p_out_tbl es el de iteracion del HashMap<String,Instrumento> de la clave (instrumento_nemotecnico_emisor_cliente_portafolio_regimen...): 6 claves (901, 900, 902, 905, 904, 903) en cualquier orden de insercion")
    void fmCpff16_ordenSeisClaves() throws Exception {
        List<String> esperado = List.of("901/100/500/GEN//", "900/100/500/GEN//", "902/100/500/GEN//", "905/100/500/GEN//", "904/100/500/GEN//", "903/100/500/GEN//");
        assertEquals(esperado, orden("orden-seis-claves"));
        assertEquals(esperado, orden("orden-seis-claves-insertadas-al-reves"));
    }

    @Test
    @DisplayName("FM-CPFF-16 C22 D-F3C-13: 14 claves (el HashMap se redimensiona a 32 cubos al pasar de 12) y 16 con dos grupos solo de garantia al final de la insercion: orden fijo; 20 claves con regimenes; con relacion y bodega las claves cambian")
    void fmCpff16_ordenCatorceVeinteClaves() throws Exception {
        assertEquals(List.of("900/101/502/GEN//", "901/100/501/GEN//", "900/100/500/GEN//", "901/101/502/GEN//", "905/100/500/GEN//", "904/100/501/GEN//", "903/100/500/GEN//", "902/100/501/GEN//", "900/100/501/GEN//",
                "901/100/500/GEN//", "905/100/501/GEN//", "902/100/500/GEN//", "903/100/501/GEN//", "904/100/500/GEN//"), orden("orden-catorce-claves-redimensiona"));
        assertEquals(List.of("900/101/502/GEN//", "901/100/500/BF//", "901/100/501/GEN//", "900/100/500/GEN//", "901/101/502/GEN//", "905/100/500/GEN//", "904/100/501/GEN//", "903/100/500/GEN//", "902/100/501/GEN//",
                "900/100/501/GEN//", "901/100/500/GEN//", "905/100/501/GEN//", "902/100/500/GEN//", "903/100/501/GEN//", "904/100/500/GEN//", "900/100/500/BF//"), orden("orden-catorce-claves-mas-garantias"));
        assertEquals(List.of("901/100/500/BF//", "901/100/500/57BIS//", "905/100/500/GEN//", "903/100/500/57BIS//", "904/100/501/GEN//", "903/100/500/GEN//", "901/100/500/18T//", "900/100/501/BF//", "901/100/500/18Q//",
                "903/100/500/18T//", "900/100/501/18T//", "902/100/501/GEN//", "904/100/501/BF//", "905/100/500/18Q//", "902/100/501/18T//", "902/100/501/18Q//", "902/100/501/57BIS//", "904/100/501/57BIS//",
                "900/100/501/18Q//", "905/100/500/BF//"), orden("orden-veinte-claves-regimenes"));
        assertEquals(List.of("900/99/504/GEN/CPRO/DCV", "900/100/500/GEN/TER/DCV", "901/101/502/GEN/REL/DCV", "901/99/504/GEN/CPRO/COR", "901/100/500/GEN/TER/COR", "900/101/502/GEN/REL/COR"), orden("orden-con-relacion-y-boveda"));
    }

    // ---- C01 JSON inputs
    @Test
    @DisplayName("FM-CPFF-01 FM-CPFF-03: Gson convierte '100' en 100 y '1' en 1; 'abc' y 1.5 => JsonSyntaxException; el numero 1 en p_AgrupaPorBoveda es el texto '1' (no agrupa)")
    void fmCpff01_entradaJson() throws Exception {
        assertEquals(1, filas("cliente-como-texto-numerico").size());
        assertEquals(1, filas("empresa-como-texto").size());
        assertTrue(excepcion("cliente-no-numerico").startsWith("JsonSyntaxException:"));
        assertTrue(excepcion("cliente-decimal").startsWith("JsonSyntaxException:"));
        assertEquals("200.00000000|", v(fila("boveda-agrupa-numerico"), "CantidadCustodia") + "|" + v(fila("boveda-agrupa-numerico"), "IdBoveda"));
        assertEquals("0|<null>|<null>", campos(fila("salida-con-nulos-escritos"), "CantidadCustodia", "Producto", "RegimenTributario"));
        assertTrue(CasoFfmm.golden(CasoFfmm.idPorSlug("salida-con-nulos-escritos")).get("salidaCruda").getAsString().contains("\"Producto\":null"));
    }

    // ---- FM-CPFF-17..19 / RULE-076
    @Test
    @DisplayName("FM-CPFF-17 FM-CPFF-19: la unica llamada fuera del modulo es ConsultarEmpresa (con el IdEmpresa, siempre, aunque no se agrupe por relacion); sin commit ni rollback propios (read only)")
    void fmCpff17_soloHablaConConsultarEmpresa() throws Exception {
        for (String slug : new String[] { "custodia-vigente-liq-una-posicion", "agrupa-por-relacion-s", "historico-ultima-version-por-folio", "solicitud-sin-cartera-de-la-serie", "custodia-100-mas-garantia-10" }) {
            JsonObject d = dump(slug);
            assertEquals(List.of("ConsultarEmpresa"), colaboradores(d), slug);
            assertEquals("[1]", d.getAsJsonArray("colaboradores").get(0).getAsJsonObject().getAsJsonArray("argumentos").toString(), slug);
            assertEquals("ROLLBACK", d.get("decisionServlet").getAsString(), slug);
        }
    }

    @Test
    @DisplayName("FM-CPFF-18 D-F3C-15 RULE-076: ni main() ni conexion ni el id de cliente real del main (1555) en las clases nuevas ni en los fixtures; los parametros de entrada son los del caso")
    void rule076_sinMainNiIdsReales() throws Exception {
        for (Class<?> c : new Class<?>[] { ConsultarCustodiaPortafolioFfmmContrato.class, ConsultarCustodiaPortafolioFfmmPort.class, ConsultarCustodiaPortafolioFfmmService.class,
                ConsultarCustodiaPortafolioFfmmJdbc.class, ConsultarCustodiaPortafolioFfmmDatos.class, ConsultarCustodiaPortafolioFfmmFabrica.class, ConsultarCustodiaPortafolioFfmmAdaptador.class,
                ConsultarCustodiaPortafolioFfmmConfiguracion.class }) {
            for (var m : c.getDeclaredMethods()) assertFalse("main".equals(m.getName()), c.getName() + " declares main");
        }
        Path raiz = CasoFfmm.recursos();
        List<String> sospechosos = new ArrayList<>();
        try (Stream<Path> s = Files.walk(raiz)) {
            for (Path p : (Iterable<Path>) s.filter(Files::isRegularFile)::iterator) {
                String t = Files.readString(p, StandardCharsets.UTF_8);
                if (t.contains("1555") || t.contains("getPRESURAConnection") || t.contains("jdbc:sap")) sospechosos.add(p.toString());
            }
        }
        assertEquals(List.of(), sospechosos);
    }

    // ---- C24 the old service
    @Test
    @DisplayName("FM-CPFF-22.10 C24 D-F3C-14: el FQN cl.bcs.fondomutuo.ConsultarCustodiaPortafolioFFMM esta enrutado a un servicio ANTIGUO que NO implementa la clase: devuelve filas crudas {P_OUT_ERRORESCONTADOR, P_OUT_CUSTODIAS, P_OUT_GARANTIAS, P_OUT_PERSONAS_DESTINO} sin agrupar ni PPP (la libreria nueva no debe heredar ese contrato)")
    void dF3c14_servicioAntiguoNoImplementaLaClase() throws Exception {
        String id = CasoFfmm.idPorSlug("custodia-vigente-liq-una-posicion");
        JsonObject nuevo = CasoFfmm.ejecutar(id);
        JsonObject antiguo = CasoFfmm.conBase(id, (con, guion) -> {
            var servicio = new cl.bcs.fondomutuo.service.javacall.ConsultarCustodiaPortafolioFFMMService(new NamedParameterJdbcTemplate(new SingleConnectionDataSource(con, true)));
            JsonObject in = new JsonObject();
            in.addProperty("p_IdEmpresa", 1);
            in.addProperty("p_IdInstrumento", 0);
            in.addProperty("p_FechaConsulta", "2026-03-10");
            in.addProperty("p_IdFamiliaFondo", "");
            in.addProperty("p_TipoFechaConsulta", "LIQ");
            return servicio.execute(in);
        });
        assertEquals("[p_out_tbl]", nuevo.getAsJsonObject("salida").keySet().toString());
        assertEquals("[P_OUT_ERRORESCONTADOR, P_OUT_CUSTODIAS, P_OUT_GARANTIAS, P_OUT_PERSONAS_DESTINO]", antiguo.keySet().toString());
        assertNotNull(antiguo.getAsJsonArray("P_OUT_CUSTODIAS"));
        assertFalse(antiguo.has("p_out_tbl"));
    }
}
