package cl.bcs.plataforma.login.dialecto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import cl.bcs.plataforma.login.ProcedimientoAlmacenado;
import cl.bcs.plataforma.login.dialecto.JdbcGrabador.ArregloGrabado;
import cl.bcs.plataforma.login.dialecto.JdbcGrabador.Respuesta;
import cl.bcs.plataforma.login.dialecto.JdbcGrabador.Sentencia;
import cl.bcs.plataforma.login.dialecto.LoginPg2Recursos.Atributo;
import cl.bcs.plataforma.login.dialecto.LoginPg2Recursos.Parametro;
import cl.bcs.plataforma.login.dialecto.LoginPg2Recursos.Rutina;

/**
 * A3-6(b) the Java definitions of the 8 procedures equal the signatures and composite attributes of the Postgres QA evidence
 * ({@code loginpg2/rutinas-y-tipos-postgres-qa-20261103.txt}). The definitions are Java code, not a file (A3-5), and production exposes no accessor for them,
 * so the proof is BEHAVIOURAL and DERIVED from the evidence: for every routine the test builds the input and the scripted answer FROM the evidence (not from a
 * hand-written case table) and checks the JDBC call that comes out: the call text with the Postgres name of the xlsx oracle, one {@code ?} per parameter, every
 * IN at its position with its type, every OUT/INOUT registered with its JDBC type, every OUT value under its legacy name by position, the 18 labels of
 * fn_pla_consultar_formatos, and the attributes of the four composite types in the order and with the JSON keys of the type. If the Java definition and the
 * evidence disagree in one position, one type, one name or one attribute, a case here fails (the mutation run shows it).
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LoginPg3DefinicionesTest {

    private static final LoginPg2Recursos.Rutinas RUT = LoginPg2Recursos.Rutinas.cargar();
    private static final LoginPgRecursos.Oraculo XLSX = LoginPgRecursos.Oraculo.cargar();
    private static final AtomicInteger EJECUTADOS = new AtomicInteger();

    @AfterAll
    void informe() {
        System.out.println("LOGINPG3 procedure definition cases executed: " + EJECUTADOS.get());
    }

    /** the HANA names of the seven procedures (the eighth, the function, has its own tests) */
    static Stream<String> losSiete() {
        return LoginPg2Recursos.HANA.values().stream().filter(h -> !h.equals(LoginPg2Recursos.FORMATOS));
    }

    private static Rutina rutinaDe(String hana) {
        return RUT.rutina(XLSX.objeto(hana).postgres());
    }

    private static ProcedimientoAlmacenado pg() {
        return ApiLoginPg3.procedimientosPostgres();
    }

    private static int tipoJdbc(String tipoPg) {
        return LoginPg2ProcedimientosTest.tipoJdbc(tipoPg);
    }

    private static boolean esArreglo(Parametro p) {
        return p.tipo().endsWith("[]");
    }

    // ------------------------------------------------------------------------------------------------ the evidence itself

    @Test
    @DisplayName("A3-6(b) the evidence has the eight routines of the login under the xlsx Postgres names (7 procedures, 1 function), with the parameter counts 3, 3, 7, 2, 8, 9, 9 and 0")
    void la_evidencia_trae_las_ocho() {
        List<Integer> n = new ArrayList<>();
        for (String h : LoginPg2Recursos.HANA.values()) n.add(rutinaDe(h).parametros().size());
        Collections.sort(n);
        assertEquals(List.of(0, 2, 3, 3, 7, 8, 9, 9), n);
        assertTrue(RUT.rutina(XLSX.objeto(LoginPg2Recursos.FORMATOS).postgres()).esFuncion());
    }

    // ------------------------------------------------------------------------------------------------ the call, derived from the evidence

    /** the JSON input of a call: every IN parameter under its snake_case name with a value of its type; arrays empty; the origin of the event given */
    private static JsonObject entradaDe(Rutina r) {
        JsonObject in = new JsonObject();
        for (Parametro p : r.parametros()) {
            if (!p.modo().equals("IN")) continue;
            if (esArreglo(p)) in.add(p.nombre(), new JsonArray());
            else if (p.tipo().equals("bigint")) in.addProperty(p.nombre(), 100 + p.posicion());
            else if (p.nombre().equals("p_aplica_origen_evento")) in.addProperty(p.nombre(), "WEB");
            else in.addProperty(p.nombre(), "s" + p.posicion());
        }
        return in;
    }

    /** the scripted value of every OUT/INOUT parameter: a distinct value per position, of the shape the type gives */
    private static Map<Integer, Object> salidasDe(Rutina r) {
        Map<Integer, Object> m = new HashMap<>();
        for (Parametro p : r.parametros()) {
            if (!p.esSalida()) continue;
            switch (p.tipo()) {
                case "character varying" -> m.put(p.posicion(), "v" + p.posicion());
                case "integer" -> m.put(p.posicion(), 10 * p.posicion());
                case "refcursor" -> m.put(p.posicion(), List.of(JdbcGrabador.fila("k" + p.posicion(), "v" + p.posicion())));
                default -> throw new AssertionError("no scripted value for the type " + p.tipo());
            }
        }
        return m;
    }

    @ParameterizedTest(name = "A3-6(b) {0}: the call text is the JDBC escape call of the xlsx Postgres name with one ? per parameter of the evidence; every IN bound at its position with its type, every OUT/INOUT registered with its JDBC type")
    @MethodSource("losSiete")
    void la_llamada_sigue_la_evidencia(String hana) throws Exception {
        Rutina r = rutinaDe(hana);
        JdbcGrabador g = new JdbcGrabador().responder(Respuesta.salidas(salidasDe(r)));
        pg().llamar(g.conexion(), hana, entradaDe(r));
        Sentencia s = g.unica();
        assertEquals("prepareCall", s.via);
        assertEquals("{call " + r.nombre() + "(" + String.join(",", Collections.nCopies(r.parametros().size(), "?")) + ")}", LoginPg2ProcedimientosTest.norm(s.sql));
        Map<Integer, Integer> registradas = new TreeMap<>();
        List<Integer> enlazados = new ArrayList<>();
        for (Parametro p : r.parametros()) {
            switch (p.modo()) {
                case "IN" -> {
                    enlazados.add(p.posicion());
                    Object valor = s.valor(p.posicion());
                    if (esArreglo(p)) {
                        assertEquals(new ArregloGrabado(p.tipo().substring(0, p.tipo().length() - 2), List.of()), valor, p.nombre());
                    } else if (p.tipo().equals("bigint")) {
                        assertTrue(JdbcGrabador.iguales(100L + p.posicion(), valor), p.nombre() + " bound " + valor);
                        assertThat(valor).as(p.nombre() + " is a number").isInstanceOf(Number.class);
                    } else if (p.nombre().equals("p_aplica_origen_evento")) {
                        assertEquals("WEB", valor, p.nombre());
                    } else {
                        assertEquals("s" + p.posicion(), valor, p.nombre());
                    }
                }
                case "INOUT" -> {
                    enlazados.add(p.posicion());
                    assertNull(s.enlace(p.posicion()).valor(), p.nombre() + " INOUT bound to NULL");
                    assertEquals(Types.OTHER, s.enlace(p.posicion()).tipoSql(), p.nombre());
                }
                default -> {
                }
            }
            if (p.esSalida()) registradas.put(p.posicion(), tipoJdbc(p.tipo()));
        }
        assertEquals(new TreeSet<>(enlazados), new TreeSet<>(s.enlaces.keySet()), "bound positions (no OUT bound as a value)");
        assertEquals(registradas, s.salidas, "registerOutParameter: position -> JDBC type, from the evidence");
        EJECUTADOS.incrementAndGet();
    }

    @ParameterizedTest(name = "A3-6(b) {0}: each OUT/INOUT value of the evidence comes back under its legacy name (P_OUT_ + the name without underscores) from ITS position: distinct values do not get mixed")
    @MethodSource("losSiete")
    void cada_salida_vuelve_con_su_nombre_legado(String hana) throws Exception {
        Rutina r = rutinaDe(hana);
        JdbcGrabador g = new JdbcGrabador().responder(Respuesta.salidas(salidasDe(r)));
        JsonObject out = pg().llamar(g.conexion(), hana, entradaDe(r));
        Map<String, String> esperado = new LinkedHashMap<>();
        for (Parametro p : r.parametros()) {
            if (!p.esSalida()) continue;
            String clave = LoginPg2Recursos.nombreLegadoDeSalida(p.nombre());
            esperado.put(clave, switch (p.tipo()) {
                case "character varying" -> "v" + p.posicion();
                case "integer" -> String.valueOf(10 * p.posicion());
                default -> "[{\"k" + p.posicion() + "\":\"v" + p.posicion() + "\"}]";
            });
        }
        assertEquals(new TreeSet<>(esperado.keySet()), new TreeSet<>(out.keySet()), "keys of the answer of " + r.nombre());
        esperado.forEach((clave, valor) -> {
            String real = out.get(clave).isJsonArray() ? out.get(clave).toString() : out.get(clave).getAsString();
            assertEquals(valor, real, clave);
        });
        EJECUTADOS.incrementAndGet();
    }

    @Test
    @DisplayName("A3-6(b) the legacy OUT names the login reads are pinned literally against the evidence: each is the image of the Postgres OUT name that the right procedure declares")
    void nombres_legados_que_el_login_lee() {
        Map<String, String[]> leidas = new LinkedHashMap<>();
        leidas.put("P_OUT_ESADMIN", new String[] { LoginPg2Recursos.ADMIN, "p_out_es_admin" });
        leidas.put("P_OUT_VALOR", new String[] { LoginPg2Recursos.CONFIG, "p_out_valor" });
        leidas.put("P_OUT_FECHAHORASISTEMALOCAL", new String[] { LoginPg2Recursos.FECHAS, "p_out_fecha_hora_sistema_local" });
        leidas.put("P_OUT_FECHAHORASISTEMA", new String[] { LoginPg2Recursos.FECHA_HORA, "p_out_fecha_hora_sistema" });
        leidas.put("P_OUT_MAIL", new String[] { LoginPg2Recursos.RECUPERAR, "p_out_mail" });
        leidas.put("P_OUT_ERRORESCONTADOR", new String[] { LoginPg2Recursos.RECUPERAR, "p_out_errores_contador" });
        leidas.forEach((legado, donde) -> {
            Parametro p = rutinaDe(donde[0]).parametro(donde[1]);
            assertTrue(p.esSalida(), donde[1] + " is an output of " + donde[0]);
            assertEquals(legado, LoginPg2Recursos.nombreLegadoDeSalida(p.nombre()));
        });
    }

    // ------------------------------------------------------------------------------------------------ the function and its 18 labels

    @Test
    @DisplayName("A3-6(b) fn_pla_consultar_formatos: SELECT * FROM the xlsx name, and the answer rows carry the 18 columns of the evidence under their HANA labels (the PascalCase of each snake_case column), in the order of the function, with the value of its own column")
    void la_funcion_devuelve_las_18_etiquetas_de_la_evidencia() throws Exception {
        Rutina r = RUT.rutina(XLSX.objeto(LoginPg2Recursos.FORMATOS).postgres());
        List<Atributo> columnas = r.columnasRetorno();
        assertEquals(18, columnas.size());
        List<Object> pares = new ArrayList<>();
        for (int i = 0; i < columnas.size(); i++) {
            pares.add(columnas.get(i).nombre());
            pares.add("c" + (i + 1));
        }
        JdbcGrabador g = new JdbcGrabador().responder(Respuesta.filas(List.of(JdbcGrabador.fila(pares.toArray()))));
        JsonObject out = pg().llamar(g.conexion(), LoginPg2Recursos.FORMATOS, new JsonObject());
        Sentencia s = g.unica();
        assertEquals("prepareStatement", s.via);
        assertEquals("select * from " + r.nombre() + "()", LoginPg2ProcedimientosTest.norm(s.sql).toLowerCase());
        assertEquals(List.of("P_OUT_MONEDAFORMATO"), new ArrayList<>(out.keySet()));
        JsonObject fila = out.getAsJsonArray("P_OUT_MONEDAFORMATO").get(0).getAsJsonObject();
        List<String> etiquetas = new ArrayList<>(fila.keySet());
        List<String> esperadas = new ArrayList<>();
        for (Atributo a : columnas) esperadas.add(LoginPg2Recursos.pascal(a.nombre()));
        assertEquals(esperadas, etiquetas, "labels in the order of the function");
        for (int i = 0; i < esperadas.size(); i++) assertEquals("c" + (i + 1), fila.get(esperadas.get(i)).getAsString(), esperadas.get(i));
        // the labels BootstrapServicio.formatos() reads, literally
        for (String leida : List.of("IdUsoMonedaFormato", "IdMoneda", "IdTipoInstrumento", "IdTipoOperacion", "CantidadDecimales", "CantidadTrucaRedondea", "PrecioDecimales",
                "PrecioTrucaRedondea", "TasaDecimales", "TasaTrucaRedondea", "MontoDecimales", "MontoTrucaRedondea", "TipoCambioDecimales", "TipoCambioTrucaRedondea",
                "ParidadDecimales", "ParidadTrucaRedondea", "PuntosDecimales", "PuntosTrucaRedondea")) {
            assertTrue(fila.has(leida), "label read by BootstrapServicio.formatos: " + leida);
        }
        EJECUTADOS.incrementAndGet();
    }

    // ------------------------------------------------------------------------------------------------ the four composite types

    private static String campo(Atributo a, int i) {
        String t = a.tipo();
        if (t.equals("bytea")) return "\"\\\\x01ff\"";
        if (t.startsWith("bigint")) return String.valueOf(10 + i);
        if (t.startsWith("timestamp")) return "2026-11-03";
        return "t" + i;
    }

    private static JsonObject filaDe(List<Atributo> atributos) {
        JsonObject fila = new JsonObject();
        for (int i = 0; i < atributos.size(); i++) {
            Atributo a = atributos.get(i);
            String clave = LoginPg2Recursos.pascal(a.nombre());
            if (a.tipo().equals("bytea")) fila.addProperty(clave, "Af8=");
            else if (a.tipo().startsWith("bigint")) fila.addProperty(clave, 10 + i);
            else if (a.tipo().startsWith("timestamp")) fila.addProperty(clave, "2026-11-03");
            else fila.addProperty(clave, "t" + i);
        }
        return fila;
    }

    @Test
    @DisplayName("A3-6(b) the four composite types of sp_pla_evt_agregar_evento: each array element is the record literal of a row built from the ATTRIBUTES OF THE EVIDENCE (JSON key = PascalCase of the attribute): same attributes, same order, bytea as hex; the type name is the unqualified one of the evidence")
    void los_atributos_de_los_tipos_compuestos_siguen_la_evidencia() throws Exception {
        Rutina r = rutinaDe(LoginPg2Recursos.EVENTO);
        JsonObject in = new JsonObject();
        in.addProperty("p_id_empresa", 1);
        in.addProperty("p_id_usuario", 7);
        in.addProperty("p_id_modulo", "PLA");
        List<Parametro> arreglos = r.parametros().stream().filter(LoginPg3DefinicionesTest::esArreglo).toList();
        assertEquals(4, arreglos.size());
        int atributosTotal = 0;
        for (Parametro p : arreglos) {
            String tipo = p.tipo().substring(0, p.tipo().length() - 2);
            JsonArray filas = new JsonArray();
            filas.add(filaDe(RUT.tipos.get(tipo)));
            in.add(p.nombre(), filas);
            atributosTotal += RUT.tipos.get(tipo).size();
        }
        assertEquals(16, atributosTotal, "attributes of the four types in the evidence");
        JdbcGrabador g = new JdbcGrabador();
        pg().llamar(g.conexion(), LoginPg2Recursos.EVENTO, in);
        for (Parametro p : arreglos) {
            String tipo = p.tipo().substring(0, p.tipo().length() - 2);
            List<Atributo> atributos = RUT.tipos.get(tipo);
            List<String> campos = new ArrayList<>();
            for (int i = 0; i < atributos.size(); i++) campos.add(campo(atributos.get(i), i));
            String literal = "(" + String.join(",", campos) + ")";
            assertEquals(new ArregloGrabado(tipo, List.of(literal)), g.unica().valor(p.posicion()), p.nombre());
        }
        EJECUTADOS.incrementAndGet();
    }

    @Test
    @DisplayName("A3-6(b) zero would mean not proven: the seven procedures ran the call and the answer checks, the function its 18 labels and the event its 16 attributes (procedure definition cases executed: 16)")
    void todos_los_casos_se_ejecutaron() throws Exception {
        int antes = EJECUTADOS.get();
        for (String h : (Iterable<String>) losSiete()::iterator) {
            la_llamada_sigue_la_evidencia(h);
            cada_salida_vuelve_con_su_nombre_legado(h);
        }
        la_funcion_devuelve_las_18_etiquetas_de_la_evidencia();
        los_atributos_de_los_tipos_compuestos_siguen_la_evidencia();
        assertEquals(16, EJECUTADOS.get() - antes, "definition cases executed");
    }
}
