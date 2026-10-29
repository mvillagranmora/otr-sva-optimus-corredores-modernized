package cl.bcs.fondomutuo.consultas.instrumentos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.consultas.instrumentos.CasoIfm.Perfil;

/**
 * Rule cards of {@code ConsultarInstrumentosFondosMutuos} (RULES_FONDOMUTUO_F3_A.md 8.5, FM-IFM-01..06; defects D-F3A-12/13/25/27, D-REQ-7; matrix IFM-C01..C06),
 * stated with LITERAL values. The expected values are the ones the unmodified legacy produced (they equal the recorded goldens: this class and
 * {@link IfmEquivalenciaTest} must agree; if the legacy is re-recorded and a literal changes, the legacy won: update the literal and say so). Each test runs the
 * NEW code on the H2 of its case (slug of {@code cases/FMM-IFM-nn_slug}) and names the card or defect it pins.
 * Series text in {@link #series}: {@code IdSerieFondo:Regimen=PermiteMLT,...} ({@code ~} = PermiteMLT key omitted by the plain Gson). Universe: {@code seed/base-ifm.sql}
 * (FM001A and S001..S012; see cases_fmr_lib.py: S005 fund blocked, S006 aporte restringido, S007 INA, S008 OWP/APV foreign 'X', S009 regimen 18Q INA, S010 no regime, S011 company 2).
 */
class IfmReglasTest {

    private static final String FM001A = "FM001A:GEN=N";
    private static final String S001 = "S001:18Q=S,18T=N";
    private static final String S002 = "S002:18Q=S";
    private static final String S003 = "S003:57BIS=N";
    private static final String S004 = "S004:18Q=N,18T=N";
    private static final String S005 = "S005:18Q=S";
    private static final String S006 = "S006:18Q=S";
    private static final String S007 = "S007:18Q=S";
    private static final String S008 = "S008:18T=~";
    private static final String S009 = "S009:GEN=N";
    private static final String S010 = "S010:";
    private static final String S011 = "S011:18Q=S";
    private static final String S012 = "S012:NINGUNO=N";

    private static final List<String> TODAS = List.of(FM001A, S001, S002, S003, S004, S005, S006, S007, S008, S009, S010, S012);

    private static final String S001_SALIDA =
            "{\"FondosMutuos\":[{\"IdSerieFondo\":\"S001\",\"CodigoFondo\":\"61\",\"NombreFondo\":\"Fondo Renta Uno\",\"Nemotecnico\":\"FRU01\",\"CodigoSerie\":\"A\",\"NombreSerie\":\"Serie A Renta Uno\",\"IdEmisor\":\"AGF1\",\"NombreEmisor\":\"Administradora Uno S.A.\",\"IdMoneda\":\"CLP\",\"IdFamiliaFondo\":\"FAM1\",\"ValorCuota\":1234.56780000,\"DecimalesValorCuota\":4,\"FechaValorCuota\":\"2026-03-01\",\"MontoMinimoInversionPesos\":1000.000000,\"MontoMinimoInversionTotal\":1000.000000,\"MontoMaximoInversionPesos\":5000000.000000,\"MontoMaximoInversionTotal\":5000000.000000,\"IdCategoriaPerfilInversionista\":\"CAT1\",\"NombreCategoriaPerfilInversionista\":\"Conservador\",\"CobraComision\":\"S\",\"InstrumentoOWP\":\"N\",\"EsAPV\":\"N\",\"RestringeAporte\":\"N\",\"MontoPatrimonioEmisor\":100000.00000000,\"ListaRegimen\":[{\"IdRegimen\":\"18Q\",\"DescripcionRegimen\":\"Articulo 18 Q\",\"PermiteMLT\":\"S\"},{\"IdRegimen\":\"18T\",\"DescripcionRegimen\":\"Articulo 18 T\",\"PermiteMLT\":\"N\"}]}],\"Mensajes\":[],\"ContadorErrores\":0}";
    private static final String S003_SIN_CUOTA =
            "{\"FondosMutuos\":[{\"IdSerieFondo\":\"S003\",\"CodigoFondo\":\"62\",\"NombreFondo\":\"Fondo Mixto Dos\",\"Nemotecnico\":\"FMD02\",\"CodigoSerie\":\"U\",\"NombreSerie\":\"Serie Unica Mixto Dos\",\"IdEmisor\":\"AGF2\",\"NombreEmisor\":\"Administradora Dos S.A.\",\"IdMoneda\":\"CLP\",\"IdFamiliaFondo\":\"FAM2\",\"ValorCuota\":0,\"DecimalesValorCuota\":4,\"FechaValorCuota\":\"0001-01-01\",\"MontoMinimoInversionPesos\":0.000000,\"MontoMinimoInversionTotal\":0.000000,\"MontoMaximoInversionPesos\":0.000000,\"MontoMaximoInversionTotal\":0.000000,\"IdCategoriaPerfilInversionista\":\"CAT2\",\"NombreCategoriaPerfilInversionista\":\"Moderado\",\"CobraComision\":\"S\",\"InstrumentoOWP\":\"N\",\"EsAPV\":\"N\",\"RestringeAporte\":\"N\",\"MontoPatrimonioEmisor\":0E-8,\"ListaRegimen\":[{\"IdRegimen\":\"57BIS\",\"DescripcionRegimen\":\"Articulo 57 bis\",\"PermiteMLT\":\"N\"}]}],\"Mensajes\":[],\"ContadorErrores\":0}";
    private static final String S002_REINVERSION =
            "{\"FondosMutuos\":[{\"IdSerieFondo\":\"S002\",\"CodigoFondo\":\"61\",\"NombreFondo\":\"Fondo Renta Uno\",\"Nemotecnico\":\"FRU01\",\"CodigoSerie\":\"B\",\"NombreSerie\":\"Serie B Renta Uno\",\"IdEmisor\":\"AGF1\",\"NombreEmisor\":\"Administradora Uno S.A.\",\"IdMoneda\":\"CLP\",\"IdFamiliaFondo\":\"FAM1\",\"ValorCuota\":987.65000000,\"FechaValorCuota\":\"2026-02-27\",\"MontoMinimoInversionPesos\":2500.000000,\"MontoMinimoInversionTotal\":2500.000000,\"MontoMaximoInversionPesos\":900000.000000,\"MontoMaximoInversionTotal\":900000.000000,\"IdCategoriaPerfilInversionista\":\"CAT1\",\"NombreCategoriaPerfilInversionista\":\"Conservador\",\"CobraComision\":\"N\",\"InstrumentoOWP\":\"N\",\"EsAPV\":\"N\",\"RestringeAporte\":\"N\",\"ListaRegimen\":[{\"IdRegimen\":\"18Q\",\"DescripcionRegimen\":\"Articulo 18 Q\",\"PermiteMLT\":\"S\"}]}],\"Mensajes\":[],\"ContadorErrores\":0}";
    private static final String S008_OTRO =
            "{\"FondosMutuos\":[{\"IdSerieFondo\":\"S008\",\"CodigoFondo\":\"67\",\"NombreFondo\":\"Fondo OWP APV Siete\",\"Nemotecnico\":\"FOA07\",\"CodigoSerie\":\"A\",\"NombreSerie\":\"Serie OWP APV\",\"IdEmisor\":\"AGF3\",\"NombreEmisor\":\"\",\"IdMoneda\":\"CLP\",\"IdFamiliaFondo\":\"FAM2\",\"ValorCuota\":150.00000000,\"DecimalesValorCuota\":4,\"FechaValorCuota\":\"2026-03-01\",\"MontoMinimoInversionPesos\":1000.000000,\"MontoMinimoInversionTotal\":1000.000000,\"MontoMaximoInversionPesos\":5000000.000000,\"MontoMaximoInversionTotal\":5000000.000000,\"IdCategoriaPerfilInversionista\":\"CAT1\",\"NombreCategoriaPerfilInversionista\":\"Conservador\",\"CobraComision\":\"N\",\"InstrumentoOWP\":\"S\",\"EsAPV\":\"S\",\"RestringeAporte\":\"N\",\"MontoPatrimonioEmisor\":100000.00000000,\"ListaRegimen\":[{\"IdRegimen\":\"18T\",\"DescripcionRegimen\":\"Articulo 18 T\"}]}],\"Mensajes\":[],\"ContadorErrores\":0}";
    private static final String SIN_VALORES =
            "{\"FondosMutuos\":[],\"Mensajes\":[{\"IdInterno\":0,\"TipoMensaje\":\"ERROR\",\"Codigo\":\"UI_SIN_VALORES\",\"Mensaje\":\"No se ingresaron valores de busqueda\"}],\"ContadorErrores\":1}";

    private static JsonObject dump(String slug) throws Exception {
        return CasoIfm.ejecutar(CasoIfm.idPorSlug(slug), Perfil.ABIERTA);
    }

    private static JsonObject salida(String slug) throws Exception {
        JsonObject d = dump(slug);
        assertFalse(d.has("excepcion"), slug + " raised " + d.get("excepcion"));
        return d.getAsJsonObject("salida");
    }

    private static String excepcion(String slug) throws Exception {
        JsonObject d = dump(slug);
        assertTrue(d.has("excepcion"), slug + " did not raise");
        assertEquals("ROLLBACK_HTTP500", d.get("decisionServlet").getAsString());
        return d.get("excepcion").getAsString();
    }

    private static List<String> series(String slug) throws Exception {
        List<String> r = new ArrayList<>();
        for (JsonElement e : salida(slug).getAsJsonArray("FondosMutuos")) {
            JsonObject o = e.getAsJsonObject();
            List<String> reg = new ArrayList<>();
            for (JsonElement x : o.getAsJsonArray("ListaRegimen")) {
                JsonObject g = x.getAsJsonObject();
                reg.add(g.get("IdRegimen").getAsString() + "=" + (g.has("PermiteMLT") ? g.get("PermiteMLT").getAsString() : "~"));
            }
            r.add(o.get("IdSerieFondo").getAsString() + ":" + String.join(",", reg));
        }
        return r;
    }

    private static List<String> sin(List<String> base, String... quitar) {
        List<String> r = new ArrayList<>(base);
        for (String q : quitar) r.remove(q);
        return r;
    }

    private static String cruda(String slug) throws Exception {
        return dump(slug).get("salidaCruda").getAsString();
    }

    // ---- FM-IFM-01 contract and validation (IFM-C01)
    @Test
    @DisplayName("FM-IFM-01 IFM-C01: p_EntradaFondosMutuos nulo o ausente => ContadorErrores 1, mensaje UI_SIN_VALORES (IdInterno 0, TipoMensaje ERROR, texto del diccionario), FondosMutuos []; sin P_OUT_ERRORESCONTADOR: ROLLBACK (D-F3A-12/27)")
    void fmIfm01_entradaNula() throws Exception {
        assertEquals(SIN_VALORES, cruda("entrada-nula-explicita"));
        assertEquals(SIN_VALORES, cruda("entrada-ausente"));
        assertEquals("ROLLBACK", dump("entrada-nula-explicita").get("decisionServlet").getAsString());
        JsonObject s = salida("entrada-nula-explicita");
        assertEquals(List.of("FondosMutuos", "Mensajes", "ContadorErrores"), new ArrayList<>(s.keySet()));
        assertEquals(1L, s.get("ContadorErrores").getAsLong());
        assertFalse(s.has("P_OUT_ERRORESCONTADOR"));
    }

    @Test
    @DisplayName("FM-IFM-01: sin la fila del diccionario (o con una empresa sin configuracion) el texto es '<codigo> sin traduccion' (kit)")
    void fmIfm01_mensajeSinTraduccion() throws Exception {
        for (String slug : new String[] { "entrada-nula-sin-texto-en-diccionario", "entrada-nula-empresa-sin-configuracion" }) {
            JsonObject m = salida(slug).getAsJsonArray("Mensajes").get(0).getAsJsonObject();
            assertEquals("UI_SIN_VALORES sin traduccion", m.get("Mensaje").getAsString(), slug);
            assertEquals("UI_SIN_VALORES", m.get("Codigo").getAsString(), slug);
        }
    }

    @Test
    @DisplayName("FM-IFM-01/06: {} => constructor del legacy (textos '', regimenes [], OcultarBloqueados N) => todas las series de la empresa; usuario, modulo y claves desconocidas no cuentan; sin empresa (0) ninguna")
    void fmIfm01_objetoVacioYDefectos() throws Exception {
        assertEquals(TODAS, series("objeto-vacio-todo-por-defecto"));
        assertEquals(List.of(S001), series("usuario-y-modulo-no-se-usan"));
        assertEquals(List.of(S001), series("campos-desconocidos-se-ignoran"));
        assertEquals(List.of(), series("empresa-ausente-es-cero"));
    }

    @Test
    @DisplayName("FM-IFM-01: tipos incorrectos => JsonSyntaxException; IdRegimenFondoMutuo nulo explicito => NullPointerException (size() sobre nulo, D-F3A-12)")
    void fmIfm01_tiposIncorrectosYNpe() throws Exception {
        assertTrue(excepcion("entrada-como-texto-json-syntax").startsWith("JsonSyntaxException"));
        assertTrue(excepcion("empresa-como-texto-json-syntax").startsWith("JsonSyntaxException"));
        assertTrue(excepcion("regimen-como-texto-json-syntax").startsWith("JsonSyntaxException"));
        assertTrue(excepcion("regimen-nulo-explicito-npe").startsWith("NullPointerException"));
    }

    // ---- FM-IFM-02 branches
    @Test
    @DisplayName("FM-IFM-02 IFM-C02: OcultarBloqueados S/s => exige BloqueoAporte N y RestringeAporte N (S005 y S006 no salen); N, n, '', nulo, 'SI' => los muestra")
    void fmIfm02_ocultarBloqueados() throws Exception {
        List<String> sinBloqueadas = sin(TODAS, S005, S006);
        assertEquals(sinBloqueadas, series("normal-ocultar-bloqueados-s"));
        assertEquals(sinBloqueadas, series("normal-ocultar-bloqueados-s-minuscula"));
        for (String slug : new String[] { "normal-ocultar-bloqueados-n", "normal-ocultar-bloqueados-n-minuscula", "normal-ocultar-bloqueados-vacio", "normal-ocultar-bloqueados-nulo",
                "normal-ocultar-bloqueados-basura" }) {
            assertEquals(TODAS, series(slug), slug);
        }
    }

    @Test
    @DisplayName("FM-IFM-02 D-F3A-12/13: IdSerieFondoOrigen '' exacto => rama normal; ' ' o null explicito => rama de reinversion con ese origen (nadie lo tiene: lista vacia, sin mensaje)")
    void fmIfm02_origenSeleccionaLaRama() throws Exception {
        assertEquals(List.of(S001), series("origen-vacio-es-rama-normal"));
        assertEquals(List.of(), series("origen-espacio-es-rama-reinversion"));
        assertEquals(List.of(), series("origen-nulo-explicito-rama-reinversion"));
        assertEquals(0L, salida("origen-nulo-explicito-rama-reinversion").get("ContadorErrores").getAsLong());
    }

    // ---- FM-IFM-03 filters of the normal branch
    @Test
    @DisplayName("FM-IFM-03 IFM-C02: una serie completa: campos del pojo, cuota y fecha por serie, montos Pesos = Total, categoria y emisor de las vistas (texto exacto del legacy)")
    void fmIfm03_unaSerieCompleta() throws Exception {
        assertEquals(S001_SALIDA, cruda("filtro-serie"));
    }

    @Test
    @DisplayName("FM-IFM-03 D-F3A: serie sin fila en ValorCuota => ValorCuota 0, FechaValorCuota '0001-01-01', montos 0 con escala; patrimonio 0E-8 (texto del legacy)")
    void fmIfm03_serieSinValorCuota() throws Exception {
        assertEquals(S003_SIN_CUOTA, cruda("regimen-57bis-serie-sin-valor-cuota"));
    }

    @Test
    @DisplayName("FM-IFM-03: filtros de texto: ''/nulo = sin filtro (COALESCE), comparacion exacta (minuscula no coincide); serie inexistente => lista vacia sin mensaje")
    void fmIfm03_filtrosDeTexto() throws Exception {
        assertEquals(List.of(), series("filtro-serie-inexistente"));
        assertEquals(List.of(S004), series("filtro-serie-nula"));
        assertEquals(List.of(S003, S008), series("filtro-familia"));
        assertEquals(sin(TODAS, S007), series("filtro-estado-act"));
        assertEquals(List.of(S007), series("filtro-estado-ina"));
        assertEquals(List.of(S008), series("filtro-owp-s"));
        assertEquals(sin(TODAS, S008), series("filtro-owp-n"));
        assertEquals(sin(sin(TODAS, S008), S002), series("filtro-owp-nulo-en-base"));
        assertEquals(List.of(S008), series("filtro-apv-s"));
        assertEquals(List.of(S004), series("filtro-emisor"));
        assertEquals(List.of(S008), series("filtro-emisor-fuera-de-la-vista"));
        assertEquals(List.of(FM001A, S001, S002, S005, S006, S008, S010, S012), series("filtro-categoria-cat1"));
        assertEquals(List.of(S007), series("filtro-categoria-fuera-de-la-vista"));
        assertEquals(List.of(S004, S012), series("filtro-moneda-usd"));
        assertEquals(List.of(S004), series("filtro-extranjero-s"));
        assertEquals(sin(TODAS, S004, S008), series("filtro-extranjero-n"));
        assertEquals(List.of(S008), series("filtro-extranjero-x-permitemlt-nulo"));
        assertEquals(List.of(FM001A, S001, S002, S005, S006, S010), series("filtros-combinados"));
        assertEquals(TODAS, series("filtros-con-nulos-explicitos"));
        assertEquals(List.of(), series("filtros-en-minuscula-no-coinciden"));
        assertEquals(List.of(), series("sin-validacion-de-formato"));
    }

    @Test
    @DisplayName("FM-IFM-03 IFM-C03: filtro de regimenes: EXISTS de un regimen ACT de la serie en la lista (S009 con 18Q INA no sale), sin duplicar series; [] = sin filtro; desconocido => vacio")
    void fmIfm03_filtroDeRegimenes() throws Exception {
        assertEquals(List.of(S001, S002, S004, S005, S006, S007), series("regimen-lista-18q"));
        assertEquals(List.of(S001, S002, S005, S006, S007), series("regimen-lista-dos-regimenes-sin-duplicados"));
        assertEquals(List.of(), series("regimen-lista-con-desconocido"));
        assertEquals(sin(TODAS, S007), series("regimen-lista-vacia-sin-filtro"));
        assertEquals(List.of(S001, S004, S008), series("regimen-lista-con-nulo-dentro"));
        assertEquals(List.of(FM001A, S009), series("regimen-gen-solo-con-activo"));
        assertEquals(List.of(S003), series("regimen-57bis-serie-sin-valor-cuota"));
    }

    @Test
    @DisplayName("FM-IFM-03: la empresa filtra (SER.IdEmpresa = p_IdEmpresa): 2 => S011; 99 => vacio")
    void fmIfm03_empresa() throws Exception {
        assertEquals(List.of(S011), series("empresa-2"));
        assertEquals(List.of(), series("empresa-inexistente"));
    }

    @Test
    @DisplayName("FM-IFM-03 D-REQ-7: la cuota se une por serie SIN filtro de fecha: S002 trae la cuota de 2020-01-01 (1.5) como valor de la serie")
    void fmIfm03_cuotaSinFiltroDeFecha() throws Exception {
        JsonObject o = salida("cuota-sin-filtro-de-fecha").getAsJsonArray("FondosMutuos").get(0).getAsJsonObject();
        assertEquals("1.50000000", o.get("ValorCuota").getAsString());
        assertEquals("2020-01-01", o.get("FechaValorCuota").getAsString());
        assertEquals("987.65000000", salida("valores-de-una-serie").getAsJsonArray("FondosMutuos").get(0).getAsJsonObject().get("ValorCuota").getAsString());
    }

    @Test
    @DisplayName("FM-IFM-03 SINTETICO: filas repetidas en las vistas de emisor o categoria duplican la serie (y cada una recibe su ListaRegimen)")
    void fmIfm03_vistasConFilasRepetidas() throws Exception {
        assertEquals(List.of(S001, S001), series("emisor-repetido-en-la-vista-duplica-serie"));
        assertEquals(List.of(S004, S004), series("categoria-repetida-en-la-vista-duplica-serie"));
    }

    // ---- FM-IFM-04 reinversion branch (IFM-C04)
    @Test
    @DisplayName("FM-IFM-04 IFM-C04: origen S001 (regimenes 18Q y 18T; config 18Q->18T, 18T->57BIS) => destinos que comparten 18Q/18T/57BIS; S001 (origen), S005 (fondo bloqueado) y S006 (aporte restringido) no salen; S007 INA y S009 (18Q INA) si")
    void fmIfm04_origenS001() throws Exception {
        List<String> esperado = List.of(S002, S003, S004, S007, S008, S009);
        assertEquals(esperado, series("reinversion-origen-s001"));
        assertEquals(esperado, series("reinversion-ignora-ocultar-bloqueados"));
        assertEquals(esperado, series("reinversion-config-duplicada-no-duplica-filas"));
        assertEquals(List.of(S002, S003, S004, S008, S009), series("reinversion-filtro-estado-act"));
        assertEquals(List.of(), series("reinversion-filtro-serie-igual-origen"));
    }

    @Test
    @DisplayName("FM-IFM-04: origen FM001A (GEN; config GEN->18Q) y origen S012 (NINGUNO acepta cualquier regimen de la candidata, no a las sin regimen)")
    void fmIfm04_otrosOrigenes() throws Exception {
        assertEquals(List.of(S001, S002, S004, S007, S009), series("reinversion-origen-fm001a-regimen-gen"));
        assertEquals(List.of(FM001A, S001, S002, S003, S004, S007, S008, S009), series("reinversion-origen-ninguno-acepta-cualquier-regimen"));
        assertEquals(List.of(S001, S002, S004, S007, S008, S009), series("reinversion-origen-otra-empresa"));
    }

    @Test
    @DisplayName("FM-IFM-04/06: origen sin regimenes, desconocido o en minuscula => lista vacia sin mensaje ni error")
    void fmIfm04_origenSinDestinos() throws Exception {
        for (String slug : new String[] { "reinversion-origen-sin-regimen", "reinversion-origen-desconocido", "reinversion-origen-en-minuscula" }) {
            assertEquals(List.of(), series(slug), slug);
            assertEquals(0L, salida(slug).get("ContadorErrores").getAsLong(), slug);
            assertEquals(0, salida(slug).getAsJsonArray("Mensajes").size(), slug);
        }
    }

    @Test
    @DisplayName("FM-IFM-04 D-F3A-13: filtros en reinversion (moneda, emisor, OWP/APV); el filtro de regimen NO exige estado ACT (S009 si sale); sin DecimalesValorCuota ni MontoPatrimonioEmisor (claves ausentes)")
    void fmIfm04_filtrosYCamposAusentes() throws Exception {
        assertEquals(List.of(S004), series("reinversion-filtro-moneda"));
        assertEquals(List.of(S008), series("reinversion-filtro-emisor-fuera-de-la-vista"));
        assertEquals(List.of(S008), series("reinversion-owp-y-apv"));
        assertEquals(List.of(S002, S004, S007, S009), series("reinversion-filtro-regimen-sin-exigir-activo"));
        assertEquals(S002_REINVERSION, cruda("reinversion-sin-decimales-ni-patrimonio"));
    }

    // ---- FM-IFM-05 regimes
    @Test
    @DisplayName("FM-IFM-05 IFM-C05: PermiteMLT: nacional = RescateConMandato (18Q S, 18T N); extranjero S siempre N; FondoExtranjero 'X' => ausente (CASE sin ELSE); serie sin regimenes => []")
    void fmIfm05_permiteMlt() throws Exception {
        assertEquals(List.of(S001), series("permitemlt-nacional-por-regimen"));
        assertEquals(List.of(S004), series("permitemlt-extranjero-siempre-n"));
        assertEquals(S008_OTRO, cruda("permitemlt-otro-valor-nulo"));
        assertEquals(List.of(S010), series("serie-sin-regimenes-lista-vacia"));
    }

    @Test
    @DisplayName("FM-IFM-05: ListaRegimen ordenada por IdRegimenFondoMutuo ascendente aunque se inserten al reves; solo regimenes ACT con maestro (INNER JOIN); descripcion del maestro")
    void fmIfm05_listaDeRegimenes() throws Exception {
        assertEquals(List.of(S001), series("regimenes-orden-ascendente-aunque-se-inserten-al-reves"));
        assertEquals(List.of(S009), series("regimen-inactivo-no-se-lista"));
        assertEquals(List.of(S010), series("regimen-sin-maestro-no-se-lista"));
        JsonObject r = salida("regimen-con-descripcion").getAsJsonArray("FondosMutuos").get(0).getAsJsonObject().getAsJsonArray("ListaRegimen").get(0).getAsJsonObject();
        assertEquals("Articulo 57 bis", r.get("DescripcionRegimen").getAsString());
        assertEquals("57BIS", r.get("IdRegimen").getAsString());
    }

    @Test
    @DisplayName("FM-IFM-05 SINTETICO: un regimen ACT repetido en la tabla aparece dos veces en ListaRegimen (la clave de HANA lo impediria)")
    void fmIfm05_regimenRepetido() throws Exception {
        assertEquals(List.of("S002:18Q=S,18Q=S"), series("regimen-de-la-serie-repetida-en-la-tabla"));
    }

    @Test
    @DisplayName("FM-IFM-05 IFM-C06: sin series el SELECT de regimenes corre con IN (NULL); contador 0, sin mensajes; ROLLBACK por la ausencia de P_OUT_ERRORESCONTADOR")
    void fmIfm05_sinSeries() throws Exception {
        assertEquals("{\"FondosMutuos\":[],\"Mensajes\":[],\"ContadorErrores\":0}", cruda("sin-series-in-null"));
        assertEquals("ROLLBACK", dump("sin-series-in-null").get("decisionServlet").getAsString());
    }

    @Test
    @DisplayName("FM-IFM-05/06: 32 series con regimenes (armado en parallelStream): orden de las series = el del SQL y cada una con SU lista de regimenes")
    void fmIfm05_muchasSeries() throws Exception {
        List<String> s = series("muchas-series-determinismo");
        assertEquals(32, s.size());
        assertEquals("M001:18Q=S", s.get(12));
        assertEquals("M002:18T=N", s.get(13));
        assertEquals("M003:18Q=S,57BIS=N", s.get(14));
    }

    // ---- FM-IFM-06 what IFM does not do
    @Test
    @DisplayName("FM-IFM-06: solo lectura: tablas intactas, sin tablas locales, commits autonomos ni secuencias; no hay colaboradores")
    void fmIfm06_soloLectura() throws Exception {
        JsonObject d = dump("tablas-no-cambian");
        assertEquals(0, d.getAsJsonObject("sesion").getAsJsonArray("tablasLocalesSinDrop").size());
        assertEquals(0, d.getAsJsonObject("sesion").getAsJsonArray("commitsAutonomos").size());
        assertEquals(0, d.getAsJsonArray("secuenciasSolicitadas").size());
        assertEquals(0, d.getAsJsonArray("colaboradores").size());
        assertEquals(12, d.getAsJsonObject("tablas").getAsJsonArray("BSCL.FondoMutuo.BD::FMM.ValorCuota").size());
    }

    // ---- RULE-076
    @Test
    @DisplayName("RULE-076: ni el main() del legacy ni su serie real ni su conexion se copiaron (src/main y los fixtures de fondomutuo-ifm)")
    void rule076_sinElMainNiLosIdsDelLegacy() throws Exception {
        // legacy ConsultarInstrumentosFondosMutuos.main (IFM:266-292): PlataformaUtils.getDESAConnection() and a JSON with the real series id below
        List<String> culpables = new ArrayList<>();
        for (java.nio.file.Path raiz : List.of(java.nio.file.Path.of("src", "main"), java.nio.file.Path.of("src", "test", "resources", "fondomutuo-ifm"))) {
            assertTrue(java.nio.file.Files.isDirectory(raiz), "RULE-076 needs " + raiz + " (run the tests from the module directory)");
            try (var s = java.nio.file.Files.walk(raiz)) {
                for (java.nio.file.Path p : s.filter(java.nio.file.Files::isRegularFile).toList()) {
                    String t = java.nio.file.Files.readString(p, java.nio.charset.StandardCharsets.ISO_8859_1);
                    if (t.contains("00149CUSD") || t.contains("CFICCLAT-B") || t.contains("getBOLSAConnection") || t.contains("getDESAConnection")) culpables.add(p.toString());
                }
            }
        }
        assertEquals(List.of(), culpables);
    }
}
