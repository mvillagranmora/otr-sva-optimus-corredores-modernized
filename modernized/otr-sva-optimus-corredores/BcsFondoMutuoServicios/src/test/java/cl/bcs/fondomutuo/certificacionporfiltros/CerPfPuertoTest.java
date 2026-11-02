package cl.bcs.fondomutuo.certificacionporfiltros;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.Entrada;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.Salida;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescatePort;
import cl.bcs.fondomutuo.kit.JsonLegacy;

/**
 * FMM-CERPF unit rules of the in-process port of {@code CertificarPorFiltrosSolicitudAporteRescate} (FM-CXF-01..05), with the two things behind it FAKED: the select (a recording fake of
 * {@code CertificarPorFiltrosDatos}) and Certificar (a recording fake of {@code CertificarAporteRescatePort}). One filter -> one query -> ONE call to Certificar with the header copied; several filters
 * -> one query each, in order, all BEFORE the one call; duplicates kept; empty filters -> Certificar still called with an empty list; null fields forwarded as null; the output copied field by field;
 * whatever Certificar or the select throw goes up UNCHANGED (same instance); the plain Gson of the legacy {@code call} reads and writes the contract types. Each test names its card.
 *
 * <p>The equivalence with the legacy is NOT here (CerPfEquivalenciaTest compares the whole run with the recorded goldens); these tests pin each rule with concrete literals and keep working when the
 * select changes engine. RED BEFORE CODE: without the production port every test fails with "T6-A not implemented: the production class ... does not exist yet" (see API_ASSUMED_T6A.md). No
 * credential, no real id: fake values only.
 */
class CerPfPuertoTest {

    /** Certificar faked: records every entry, in order, with the events log shared with the select; answers by the function. */
    private static final class CertificarFalso implements CertificarAporteRescatePort {
        final List<Entrada> entradas = new ArrayList<>();
        final List<String> eventos;
        final Function<Entrada, Salida> respuesta;

        CertificarFalso(List<String> eventos, Function<Entrada, Salida> respuesta) {
            this.eventos = eventos;
            this.respuesta = respuesta;
        }

        @Override
        public Salida logica(Entrada entrada) {
            entradas.add(entrada);
            eventos.add("certificar");
            return respuesta.apply(entrada);
        }
    }

    private final List<String> eventos = new ArrayList<>();
    private final List<ApiT6a.ConsultaVista> consultas = new ArrayList<>();

    private CertificarFalso certificarVacio() {
        return new CertificarFalso(eventos, e -> new Salida());
    }

    private ApiT6a.PorFiltros puerto(CertificarAporteRescatePort certificar, ApiT6a.Respuesta respuesta) {
        return ApiT6a.crear(certificar, ApiT6a.datosFalso(consultas, (n, c) -> {
            eventos.add("consulta" + n);
            return respuesta.responder(n, c);
        }));
    }

    private static JsonObject json(String texto) {
        return JsonParser.parseString(texto).getAsJsonObject();
    }

    private static final String CABECERA = "\"p_IdEmpresa\":1,\"p_IdUsuario\":7,\"p_IdModulo\":\"FMM\",\"p_SimularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"N\",\"p_FechaCertificacion\":\"2026-03-02\"";
    private static final String FILTRO_TODO = "{\"IdEmisor\":\"\",\"IdMoneda\":\"\",\"IdSerieFondo\":\"\",\"Nacional\":\"N\",\"TipoSolicitud\":\"\",\"IdTipoMovimientoFondoMutuo\":\"\",\"MoneyMarket\":\"N\",\"IdCarga\":0}";

    private static List<Long> ids(Entrada e) {
        return e.p_Solicitudes.stream().map(s -> s.IdSolicitudAporteRescate).toList();
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // FM-CXF-02 / 05: one filter -> one query -> one call
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("FM-CXF-02 / FM-CXF-05: ONE filter => ONE query with the 8 fields of the filter plus the date and the company of the request => ONE call to Certificar with the ids the query returned")
    void unFiltroUnaConsultaUnaLlamada() throws Exception {
        CertificarFalso certificar = certificarVacio();
        ApiT6a.PorFiltros pf = puerto(certificar, (n, c) -> List.of(11L, 22L, 33L));
        pf.llamar(json("{" + CABECERA + ",\"p_Filtros\":[{\"IdEmisor\":\"AGF1\",\"IdMoneda\":\"USD\",\"IdSerieFondo\":\"FM002A\",\"Nacional\":\"S\",\"TipoSolicitud\":\"APO\","
                + "\"IdTipoMovimientoFondoMutuo\":\"APONORM\",\"MoneyMarket\":\"S\",\"IdCarga\":900}]}"));
        assertThat(consultas).hasSize(1);
        ApiT6a.ConsultaVista q = consultas.get(0);
        assertThat(q.filtro).containsEntry("IdEmisor", "AGF1").containsEntry("IdMoneda", "USD").containsEntry("IdSerieFondo", "FM002A").containsEntry("Nacional", "S")
                .containsEntry("TipoSolicitud", "APO").containsEntry("IdTipoMovimientoFondoMutuo", "APONORM").containsEntry("MoneyMarket", "S").containsEntry("IdCarga", 900L);
        assertThat(q.filtro).as("exactly the 8 fields of the filter").hasSize(8);
        assertThat(q.fechaCertificacion).isEqualTo("2026-03-02");
        assertThat(q.idEmpresa).isEqualTo(1L);
        assertThat(certificar.entradas).hasSize(1);
        assertThat(ids(certificar.entradas.get(0))).isEqualTo(List.of(11L, 22L, 33L));
        assertThat(eventos).containsExactly("consulta1", "certificar");
    }

    @Test
    @DisplayName("FM-CXF-05: the header reaches Certificar field by field (empresa 5, usuario 9, modulo ABC, simular S, reglas S, fecha 2030-01-02, canal OWP); NotificaOWP stays S and the switch flag N even if the request says otherwise")
    void laCabeceraSeCopia() throws Exception {
        CertificarFalso certificar = certificarVacio();
        ApiT6a.PorFiltros pf = puerto(certificar, (n, c) -> List.of(1L));
        pf.llamar(json("{\"p_IdEmpresa\":5,\"p_IdUsuario\":9,\"p_IdModulo\":\"ABC\",\"p_SimularOperacion\":\"S\",\"p_UsaReglasNegocio\":\"S\",\"p_FechaCertificacion\":\"2030-01-02\","
                + "\"p_IdTipoCanalIngreso\":\"OWP\",\"p_NotificaOWP\":\"N\",\"p_CertificacionAporteSwitchSimulado\":\"S\",\"p_Filtros\":[" + FILTRO_TODO + "]}"));
        Entrada e = certificar.entradas.get(0);
        assertThat(e.p_IdEmpresa).isEqualTo(5L);
        assertThat(e.p_IdUsuario).isEqualTo(9L);
        assertThat(e.p_IdModulo).isEqualTo("ABC");
        assertThat(e.p_SimularOperacion).isEqualTo("S");
        assertThat(e.p_UsaReglasNegocio).isEqualTo("S");
        assertThat(e.p_FechaCertificacion).isEqualTo("2030-01-02");
        assertThat(e.p_IdTipoCanalIngreso).isEqualTo("OWP");
        assertThat(e.p_NotificaOWP).as("not copied: the default of Certificar").isEqualTo("S");
        assertThat(e.p_CertificacionAporteSwitchSimulado).as("not copied: the default of Certificar").isEqualTo("N");
        assertThat(consultas.get(0).fechaCertificacion).isEqualTo("2030-01-02");
        assertThat(consultas.get(0).idEmpresa).isEqualTo(5L);
    }

    @Test
    @DisplayName("FM-CXF-05: a header with absent fields is copied as null (no validation of its own): Certificar receives nulls")
    void laCabeceraAusenteSeCopiaComoNula() throws Exception {
        CertificarFalso certificar = certificarVacio();
        puerto(certificar, (n, c) -> List.of()).llamar(json("{\"p_Filtros\":[" + FILTRO_TODO + "]}"));
        Entrada e = certificar.entradas.get(0);
        assertThat(e.p_IdEmpresa).isNull();
        assertThat(e.p_IdUsuario).isNull();
        assertThat(e.p_IdModulo).isNull();
        assertThat(e.p_SimularOperacion).isNull();
        assertThat(e.p_UsaReglasNegocio).isNull();
        assertThat(e.p_FechaCertificacion).isNull();
        assertThat(e.p_IdTipoCanalIngreso).isNull();
        assertThat(consultas.get(0).fechaCertificacion).isNull();
        assertThat(consultas.get(0).idEmpresa).isNull();
    }

    @Test
    @DisplayName("FM-CXF-05: the p_Solicitudes the request itself carries (as the legacy main() sends) are IGNORED: only the rows of the filters reach Certificar")
    void laListaDelPedidoSeIgnora() throws Exception {
        CertificarFalso certificar = certificarVacio();
        puerto(certificar, (n, c) -> List.of(77L)).llamar(json("{" + CABECERA + ",\"p_Solicitudes\":[{\"IdSolicitudAporteRescate\":1},{\"IdSolicitudAporteRescate\":2}],\"p_Filtros\":[" + FILTRO_TODO + "]}"));
        assertThat(ids(certificar.entradas.get(0))).isEqualTo(List.of(77L));
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // FM-CXF-03: several filters
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("FM-CXF-03: three filters => three queries IN ORDER, ALL before the ONE call to Certificar; the ids are concatenated in the order of the filters")
    void variosFiltrosEnOrden() throws Exception {
        CertificarFalso certificar = certificarVacio();
        ApiT6a.PorFiltros pf = puerto(certificar, (n, c) -> n == 1 ? List.of(5L, 6L) : n == 2 ? List.of() : List.of(9L, 1L));
        pf.llamar(json("{" + CABECERA + ",\"p_Filtros\":[{\"IdEmisor\":\"A\",\"IdCarga\":0,\"Nacional\":\"N\",\"MoneyMarket\":\"N\"},{\"IdEmisor\":\"B\",\"IdCarga\":0,\"Nacional\":\"N\",\"MoneyMarket\":\"N\"},"
                + "{\"IdEmisor\":\"C\",\"IdCarga\":0,\"Nacional\":\"N\",\"MoneyMarket\":\"N\"}]}"));
        assertThat(consultas).extracting(c -> c.filtro.get("IdEmisor")).containsExactly("A", "B", "C");
        assertThat(consultas).allSatisfy(c -> {
            assertThat(c.fechaCertificacion).isEqualTo("2026-03-02");
            assertThat(c.idEmpresa).isEqualTo(1L);
        });
        assertThat(certificar.entradas).hasSize(1);
        assertThat(ids(certificar.entradas.get(0))).isEqualTo(List.of(5L, 6L, 9L, 1L));
        assertThat(eventos).containsExactly("consulta1", "consulta2", "consulta3", "certificar");
    }

    @Test
    @DisplayName("FM-CXF-03: the ids that two filters return are NOT de-duplicated (Certificar will reject them as SOLICITUD_DUPLICADA)")
    void losDuplicadosSeConservan() throws Exception {
        CertificarFalso certificar = certificarVacio();
        puerto(certificar, (n, c) -> List.of(4003L, 4004L)).llamar(json("{" + CABECERA + ",\"p_Filtros\":[" + FILTRO_TODO + "," + FILTRO_TODO + "]}"));
        assertThat(ids(certificar.entradas.get(0))).isEqualTo(List.of(4003L, 4004L, 4003L, 4004L));
        CertificarFalso uno = certificarVacio();
        puerto(uno, (n, c) -> List.of(8L, 8L, 8L)).llamar(json("{" + CABECERA + ",\"p_Filtros\":[" + FILTRO_TODO + "]}"));
        assertThat(ids(uno.entradas.get(0))).as("not even the repeats inside ONE answer are removed").isEqualTo(List.of(8L, 8L, 8L));
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // FM-CXF-04: no filters
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("FM-CXF-04: p_Filtros empty or ABSENT => no query, and Certificar is STILL called once with an EMPTY list and the header")
    void sinFiltrosCertificarSeLlamaIgual() throws Exception {
        for (String cuerpo : List.of("{" + CABECERA + ",\"p_Filtros\":[]}", "{" + CABECERA + "}")) {
            consultas.clear();
            eventos.clear();
            CertificarFalso certificar = certificarVacio();
            puerto(certificar, (n, c) -> List.of(1L)).llamar(json(cuerpo));
            assertThat(consultas).isEmpty();
            assertThat(certificar.entradas).hasSize(1);
            assertThat(certificar.entradas.get(0).p_Solicitudes).isNotNull().isEmpty();
            assertThat(certificar.entradas.get(0).p_IdEmpresa).isEqualTo(1L);
            assertThat(eventos).containsExactly("certificar");
        }
    }

    @Test
    @DisplayName("FM-CXF-04: p_Filtros explicitly null => NullPointerException, no query, Certificar NEVER called")
    void filtrosNuloLanza() {
        CertificarFalso certificar = certificarVacio();
        ApiT6a.PorFiltros pf = puerto(certificar, (n, c) -> List.of(1L));
        assertThatThrownBy(() -> pf.llamar(json("{" + CABECERA + ",\"p_Filtros\":null}"))).isInstanceOf(NullPointerException.class);
        assertThat(consultas).isEmpty();
        assertThat(certificar.entradas).isEmpty();
    }

    @Test
    @DisplayName("FM-CXF-04: a null request => NullPointerException before anything is queried or called")
    void entradaNulaLanza() {
        CertificarFalso certificar = certificarVacio();
        ApiT6a.PorFiltros pf = puerto(certificar, (n, c) -> List.of(1L));
        assertThatThrownBy(() -> pf.logica(null)).isInstanceOf(NullPointerException.class);
        assertThat(consultas).isEmpty();
        assertThat(certificar.entradas).isEmpty();
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // FM-CXF-02: the fields of the filter go to the select as they are (nulls included)
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("FM-CXF-02: the null / absent fields of a filter are passed to the select as NULL (the COALESCE of the SQL decides), blanks and empty strings are passed untouched, IdCarga keeps its Long value")
    void losCamposNulosSeReenvianComoNulos() throws Exception {
        puerto(certificarVacio(), (n, c) -> List.of()).llamar(json("{" + CABECERA + ",\"p_Filtros\":[{},{\"IdEmisor\":\" \",\"Nacional\":\"\",\"MoneyMarket\":null,\"IdCarga\":4000000000}]}"));
        assertThat(consultas).hasSize(2);
        assertThat(consultas.get(0).filtro).as("an empty filter: the 8 keys exist and every value is null").hasSize(8).containsEntry("IdEmisor", null).containsEntry("IdMoneda", null)
                .containsEntry("IdSerieFondo", null).containsEntry("Nacional", null).containsEntry("TipoSolicitud", null).containsEntry("IdTipoMovimientoFondoMutuo", null)
                .containsEntry("MoneyMarket", null).containsEntry("IdCarga", null);
        assertThat(consultas.get(1).filtro).containsEntry("IdEmisor", " ").containsEntry("Nacional", "").containsEntry("MoneyMarket", null).containsEntry("IdCarga", 4000000000L);
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // FM-CXF-01 / 05: the output
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("FM-CXF-01 / FM-CXF-05: counter, messages and results of Certificar are returned field by field (counter 3, two messages, two results), nothing added")
    void laSalidaSeCopia() throws Exception {
        Salida s = JsonLegacy.plano().fromJson("{\"P_OUT_ERRORESCONTADOR\":3,\"P_OUT_MENSAJES\":[{\"id\":0,\"tipo\":\"ERROR\",\"codigo\":\"X1\",\"descripcion\":\"uno\"},{\"id\":5,\"tipo\":\"ERROR\",\"codigo\":\"X2\",\"descripcion\":\"dos\"}],"
                + "\"P_OUT_RESULTADO\":[{\"IdSolicitudAporteRescate\":4001,\"IdSerieFondo\":\"FM001A\",\"Codigo\":\"UI_EXITO\",\"Estado\":\"APD\",\"NombreEstado\":\"Aprobada\"},"
                + "{\"IdSolicitudAporteRescate\":4002,\"Codigo\":\"UI_X\",\"Estado\":\"RZD\"}]}", Salida.class);
        JsonObject salida = puerto(new CertificarFalso(eventos, e -> s), (n, c) -> List.of(4001L, 4002L)).llamar(json("{" + CABECERA + ",\"p_Filtros\":[" + FILTRO_TODO + "]}"));
        assertThat(salida).isEqualTo(JsonParser.parseString(JsonLegacy.plano().toJson(s)));
        assertThat(salida.get("P_OUT_ERRORESCONTADOR").getAsLong()).isEqualTo(3L);
        assertThat(salida.getAsJsonArray("P_OUT_MENSAJES")).hasSize(2);
        assertThat(salida.getAsJsonArray("P_OUT_RESULTADO")).hasSize(2);
        assertThat(salida.keySet()).containsExactly("P_OUT_ERRORESCONTADOR", "P_OUT_MENSAJES", "P_OUT_RESULTADO");
    }

    @Test
    @DisplayName("FM-CXF-01: a default Salida (counter 0, no messages, no results) is returned as the JSON {P_OUT_ERRORESCONTADOR:0,P_OUT_MENSAJES:[],P_OUT_RESULTADO:[]}; a null counter of Certificar is copied as null (no key)")
    void laSalidaPorDefectoYElContadorNulo() throws Exception {
        JsonObject vacia = puerto(certificarVacio(), (n, c) -> List.of()).llamar(json("{" + CABECERA + ",\"p_Filtros\":[]}"));
        assertThat(vacia.toString()).isEqualTo("{\"P_OUT_ERRORESCONTADOR\":0,\"P_OUT_MENSAJES\":[],\"P_OUT_RESULTADO\":[]}");
        Salida sinContador = new Salida();
        sinContador.P_OUT_ERRORESCONTADOR = null;
        JsonObject nula = puerto(new CertificarFalso(eventos, e -> sinContador), (n, c) -> List.of()).llamar(json("{" + CABECERA + ",\"p_Filtros\":[]}"));
        assertThat(nula.has("P_OUT_ERRORESCONTADOR")).as("a plain Gson writes no null key: the host then ROLLS BACK (counter missing)").isFalse();
    }

    @Test
    @DisplayName("FM-CXF-01: Certificar answering NULL => NullPointerException (the legacy reads salida.P_OUT_ERRORESCONTADOR)")
    void certificarQueDevuelveNuloLanza() {
        ApiT6a.PorFiltros pf = puerto(new CertificarFalso(eventos, e -> null), (n, c) -> List.of());
        assertThatThrownBy(() -> pf.llamar(json("{" + CABECERA + ",\"p_Filtros\":[]}"))).isInstanceOf(NullPointerException.class);
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // FM-CXF-05: exceptions go up unchanged
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("FM-CXF-05: whatever Certificar throws goes up UNCHANGED (same instance): checked Exception, runtime exception, Error")
    void lasExcepcionesDeCertificarSuben() {
        List<Throwable> lanzadas = List.of(new Exception("FALLA_CERTIFICAR"), new IllegalStateException("estado"), new LinkageError("fake"));
        for (Throwable t : lanzadas) {
            ApiT6a.PorFiltros pf = ApiT6a.crear(e -> {
                CasoThrow.lanzar(t);
                return null;
            }, ApiT6a.datosFalso(new ArrayList<>(), (n, c) -> List.of(1L)));
            assertThatThrownBy(() -> pf.llamar(json("{" + CABECERA + ",\"p_Filtros\":[" + FILTRO_TODO + "]}"))).isSameAs(t);
        }
    }

    @Test
    @DisplayName("FM-CXF-02: an exception of the select goes up UNCHANGED, at the filter where it happened: the queries before it ran, the later ones did not, Certificar was NEVER called")
    void unaExcepcionDeLaConsultaSubeAntesDeCertificar() {
        CertificarFalso certificar = certificarVacio();
        RuntimeException falla = new IllegalArgumentException("FALLA_SELECT");
        ApiT6a.PorFiltros pf = puerto(certificar, (n, c) -> {
            if (n == 2) throw falla;
            return List.of(1L);
        });
        assertThatThrownBy(() -> pf.llamar(json("{" + CABECERA + ",\"p_Filtros\":[" + FILTRO_TODO + "," + FILTRO_TODO + "," + FILTRO_TODO + "]}"))).isSameAs(falla);
        assertThat(consultas).hasSize(2);
        assertThat(certificar.entradas).isEmpty();
        assertThat(eventos).containsExactly("consulta1", "consulta2");
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // FM-CXF-01: the contract and the plain Gson
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("FM-CXF-01: the contract has the legacy names, types and order: Entrada (7 header fields + p_Filtros), InFiltros (8 fields), Salida (counter, messages, results); defaults: p_Filtros = empty list, counter 0")
    void elContratoTieneLosNombresDelLegacy() throws Exception {
        assertThat(nombres(ApiT6a.cargar(ApiT6a.ENTRADA))).containsExactly("p_IdEmpresa", "p_IdUsuario", "p_IdModulo", "p_SimularOperacion", "p_UsaReglasNegocio", "p_FechaCertificacion",
                "p_IdTipoCanalIngreso", "p_Filtros");
        assertThat(nombres(ApiT6a.cargar(ApiT6a.FILTRO))).containsExactly("IdEmisor", "IdMoneda", "IdSerieFondo", "Nacional", "TipoSolicitud", "IdTipoMovimientoFondoMutuo", "MoneyMarket", "IdCarga");
        assertThat(nombres(ApiT6a.cargar(ApiT6a.SALIDA))).containsExactly("P_OUT_ERRORESCONTADOR", "P_OUT_MENSAJES", "P_OUT_RESULTADO");
        Field carga = ApiT6a.cargar(ApiT6a.FILTRO).getField("IdCarga");
        assertThat(carga.getType()).isEqualTo(Long.class);
        for (String f : List.of("IdEmisor", "IdMoneda", "IdSerieFondo", "Nacional", "TipoSolicitud", "IdTipoMovimientoFondoMutuo", "MoneyMarket")) {
            assertThat(ApiT6a.cargar(ApiT6a.FILTRO).getField(f).getType()).as(f).isEqualTo(String.class);
        }
        Object vacia = ApiT6a.entradaDe(json("{}"));
        assertThat(ApiT6a.cargar(ApiT6a.ENTRADA).getField("p_Filtros").get(vacia)).as("absent p_Filtros: the empty list of the constructor").isEqualTo(List.of());
        Object salida = ApiT6a.cargar(ApiT6a.SALIDA).getDeclaredConstructor().newInstance();
        assertThat(ApiT6a.cargar(ApiT6a.SALIDA).getField("P_OUT_ERRORESCONTADOR").get(salida)).isEqualTo(0L);
        assertThat(ApiT6a.cargar(ApiT6a.SALIDA).getField("P_OUT_MENSAJES").get(salida)).isEqualTo(List.of());
        assertThat(ApiT6a.cargar(ApiT6a.SALIDA).getField("P_OUT_RESULTADO").get(salida)).isEqualTo(List.of());
    }

    @Test
    @DisplayName("FM-CXF-01: the plain Gson (as the legacy call) coerces \"900\" to the Long 900 and ignores unknown keys, but fails with JsonSyntaxException on a non-number, before anything is queried or called")
    void gsonPlano() throws Exception {
        CertificarFalso certificar = certificarVacio();
        ApiT6a.PorFiltros pf = puerto(certificar, (n, c) -> List.of());
        pf.llamar(json("{\"p_IdEmpresa\":\"1\",\"p_IdUsuario\":\"7.0\",\"p_FechaCertificacion\":\"2026-03-02\",\"Desconocido\":true,\"p_Filtros\":[{\"IdCarga\":\"900\",\"Otro\":1}]}"));
        assertThat(consultas.get(0).filtro.get("IdCarga")).isEqualTo(900L);
        assertThat(certificar.entradas.get(0).p_IdEmpresa).isEqualTo(1L);
        assertThat(certificar.entradas.get(0).p_IdUsuario).isEqualTo(7L);
        consultas.clear();
        certificar.entradas.clear();
        assertThatThrownBy(() -> pf.llamar(json("{\"p_IdEmpresa\":\"abc\",\"p_Filtros\":[]}"))).isInstanceOf(JsonSyntaxException.class);
        assertThat(consultas).isEmpty();
        assertThat(certificar.entradas).isEmpty();
    }

    private static List<String> nombres(Class<?> c) {
        return java.util.Arrays.stream(c.getDeclaredFields()).filter(f -> !f.isSynthetic() && !Modifier.isStatic(f.getModifiers())).map(Field::getName).toList();
    }

    /** throws a checked exception or an Error where the lambda of the port cannot declare it */
    private static final class CasoThrow {
        @SuppressWarnings("unchecked")
        static <E extends Throwable> void lanzar(Throwable t) throws E {
            throw (E) t;
        }
    }
}
