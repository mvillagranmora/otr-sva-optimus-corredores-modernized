package cl.bcs.apv.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import cl.bcs.apv.api.LegacyApv.Operacion;
import cl.bcs.apv.api.casouso.CasosUso;

/**
 * Pins the OPERATION COVERAGE of the typed contract against the legacy sources (read independently, see {@link LegacyApv}).
 *
 * <p>Rule ids: B-APVAPI-01 (the legacy has 66 JavaCall classes, 64 invocable), B-APVAPI-02 (every invocable operation has its use case interface
 * with the typed signature), B-APVAPI-03 ({@code CasosUso.TODOS} lists each exactly once, no extras), B-APVAPI-04 (the two non invocable classes
 * stay out), B-APVAPI-05 (names of CONTRATO.md and catalogo.py agree with the code). No RULE-nnn of BUSINESS_RULES.md describes this
 * generated contract (the only P0 rule the pack lists for this module is about HANA credentials in dead code of another module).
 */
class ApvApiCoberturaOperacionesTest {

    private static final String PKG_CASOUSO = "cl.bcs.apv.api.casouso";

    private static List<Operacion> invocables() {
        return LegacyApv.operaciones().stream().filter(o -> !o.callNulo).toList();
    }

    private static String casoDe(String legacySimple) {
        Map<String, String> cat = LegacyApv.catalogoNombres();
        assertTrue(cat.containsKey(legacySimple), "catalogo.py has no entry for " + legacySimple);
        return cat.get(legacySimple);
    }

    // B-APVAPI-01
    @Test
    void b_apvapi_01_legacy_tiene_66_javacall_de_los_cuales_64_son_invocables() {
        List<Operacion> todas = LegacyApv.operaciones();
        assertEquals(66, todas.size(), "JavaCall classes read from the legacy sources");
        Set<String> nulas = todas.stream().filter(o -> o.callNulo).map(o -> o.clase.nombre).collect(Collectors.toCollection(TreeSet::new));
        assertEquals(Set.of("ValidarBancoPrevired", "ValidarMandatosPAC"), nulas, "JavaCall classes whose call() only returns null");
        assertEquals(64, invocables().size());
        // the authorization hook is NOT a JavaCall: it must not become a use case
        assertTrue(todas.stream().noneMatch(o -> o.clase.nombre.equals("AutorizacionCambiarAPlanActivoContratoSAAPV")));
        for (Operacion o : invocables()) {
            assertNotNull(o.entrada, o.clase.nombre + ": call() does not deserialize an Entrada class");
            assertNotNull(o.salida, o.clase.nombre + ": call() does not use a Salida class returned by logica()");
        }
    }

    // B-APVAPI-02
    @Test
    void b_apvapi_02_cada_operacion_invocable_tiene_interfaz_y_firma_Resultado_ejecutar_Entrada() throws Exception {
        for (Operacion o : invocables()) {
            String caso = casoDe(o.clase.nombre);
            assertNotNull(caso, o.clase.nombre + " is invocable but catalogo.py gives it no use case");
            Class<?> iface = Class.forName(PKG_CASOUSO + "." + caso);
            assertTrue(iface.isInterface() && Modifier.isPublic(iface.getModifiers()), caso + " must be a public interface");
            assertTrue(CasoUso.class.isAssignableFrom(iface), caso + " must extend CasoUso");
            Class<?> entrada = Class.forName("cl.bcs.apv.api.dto." + caso.toLowerCase(Locale.ROOT) + "." + caso + "Entrada");
            Class<?> resultado = Class.forName("cl.bcs.apv.api.dto." + caso.toLowerCase(Locale.ROOT) + "." + caso + "Resultado");
            assertTrue(entrada.isRecord() && resultado.isRecord(), caso);
            Method ejecutar = iface.getMethod("ejecutar", entrada);
            assertEquals(resultado, ejecutar.getReturnType(), caso + ": Resultado ejecutar(Entrada)");
            // the generic supertype CasoUso<E,R> carries the same pair
            ParameterizedType pt = (ParameterizedType) iface.getGenericInterfaces()[0];
            assertEquals(CasoUso.class, pt.getRawType(), caso);
            assertEquals(entrada, pt.getActualTypeArguments()[0], caso + ": CasoUso<E, .>");
            assertEquals(resultado, pt.getActualTypeArguments()[1], caso + ": CasoUso<., R>");
            // exactly one declared method
            List<Method> declarados = new ArrayList<>(java.util.Arrays.stream(iface.getDeclaredMethods()).filter(x -> !x.isBridge() && !x.isSynthetic()).toList());
            assertEquals(1, declarados.size(), caso + ": the interface must declare only ejecutar");
        }
    }

    // B-APVAPI-03
    @Test
    void b_apvapi_03_TODOS_contiene_cada_caso_de_uso_exactamente_una_vez_sin_sobrantes() throws Exception {
        Set<String> esperados = new TreeSet<>();
        for (Operacion o : invocables()) {
            esperados.add(casoDe(o.clase.nombre));
        }
        assertEquals(64, esperados.size(), "use case names of the 64 invocable operations must be unique");
        Map<String, Integer> veces = new TreeMap<>();
        for (Class<?> c : CasosUso.TODOS) {
            veces.merge(c.getSimpleName(), 1, Integer::sum);
        }
        assertEquals(64, CasosUso.TODOS.size(), "CasosUso.TODOS size");
        veces.forEach((n, k) -> assertEquals(1, k, n + " appears " + k + " times in CasosUso.TODOS"));
        assertEquals(esperados, veces.keySet(), "CasosUso.TODOS vs the use cases derived from the legacy JavaCall classes");
        // every interface of the package is listed (nothing left out) and nothing else lives there
        Set<String> enDisco = nombresEnPaquete();
        enDisco.remove("CasosUso");
        assertEquals(esperados, enDisco, "files of package casouso vs expected use cases");
        for (Class<?> c : CasosUso.TODOS) {
            assertEquals(PKG_CASOUSO, c.getPackageName(), c.getName());
        }
    }

    // B-APVAPI-04
    @Test
    void b_apvapi_04_las_dos_clases_no_invocables_no_tienen_caso_de_uso_ni_estan_en_TODOS() {
        for (Operacion o : LegacyApv.operaciones()) {
            if (!o.callNulo) {
                continue;
            }
            assertEquals(null, LegacyApv.catalogoNombres().get(o.clase.nombre), o.clase.nombre + ": catalogo.py must register it with no use case");
            assertTrue(LegacyApv.catalogoNombres().containsKey(o.clase.nombre), o.clase.nombre + " is JavaCall: it must be registered (as non invocable)");
            Set<String> enDisco = nombresEnPaquete();
            assertFalse(enDisco.contains(o.clase.nombre), o.clase.nombre + ": no interface in casouso");
            assertTrue(CasosUso.TODOS.stream().noneMatch(c -> c.getSimpleName().startsWith(o.clase.nombre)), o.clase.nombre + " must not be in TODOS");
        }
        // the typed entry point of ValidarMandatosPAC lives in 'logica', is not a CasoUso and not in TODOS
        assertFalse(CasoUso.class.isAssignableFrom(cl.bcs.apv.api.logica.ValidarMandatosPACLogica.class));
        assertTrue(CasosUso.TODOS.stream().noneMatch(c -> c.getPackageName().endsWith(".logica")));
    }

    // B-APVAPI-05
    @Test
    void b_apvapi_05_catalogo_y_CONTRATO_md_coinciden_con_los_casos_de_uso_del_codigo() {
        Map<String, String> cat = LegacyApv.catalogoNombres();
        Set<String> legacyJavaCall = LegacyApv.operaciones().stream().map(o -> o.clase.nombre).collect(Collectors.toCollection(TreeSet::new));
        assertEquals(legacyJavaCall, new TreeSet<>(cat.keySet()), "catalogo.py entries vs legacy JavaCall classes");
        // CONTRATO.md family tables: | # | `cl.bcs.APV.X` | lines | entrada | gson | colab | `UseCase`? | route | records |
        Map<String, String> doc = new HashMap<>();
        for (String l : LegacyApv.leer(LegacyApv.modulo().resolve("CONTRATO.md")).split("\n")) {
            String[] c = l.split("\\|");
            if (c.length >= 10 && c[1].trim().matches("\\d+") && c[2].trim().matches("`cl\\.bcs\\.APV\\.\\w+`")) {
                String legacy = c[2].trim().replace("`", "").replace("cl.bcs.APV.", "");
                assertEquals(null, doc.put(legacy, c[7].trim().replace("`", "")), "legacy class repeated in CONTRATO.md: " + legacy);
            }
        }
        for (Operacion o : invocables()) {
            assertEquals(casoDe(o.clase.nombre), doc.get(o.clase.nombre), "CONTRATO.md use case of " + o.clase.nombre);
        }
        assertEquals(66, doc.size(), "rows of the family tables of CONTRATO.md (all JavaCall classes)");
        for (Operacion o : LegacyApv.operaciones()) {
            if (o.callNulo) {
                assertTrue(doc.get(o.clase.nombre).startsWith("(none"), o.clase.nombre + ": CONTRATO.md must say it is not an operation, found " + doc.get(o.clase.nombre));
            }
        }
    }

    private static Set<String> nombresEnPaquete() {
        Path dir = LegacyApv.modulo().resolve("src/main/java/cl/bcs/apv/api/casouso");
        try (Stream<Path> s = Files.list(dir)) {
            return s.map(p -> p.getFileName().toString()).filter(n -> n.endsWith(".java")).map(n -> n.substring(0, n.length() - 5))
                    .collect(Collectors.toCollection(TreeSet::new));
        } catch (IOException e) {
            throw new java.io.UncheckedIOException(e);
        }
    }
}
