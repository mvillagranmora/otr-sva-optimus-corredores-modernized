package cl.bcs.plataforma.login.dialecto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * A3-6(a) the dev-time verification that REPLACES the run-time catalog (these are tests; they read the oracle fixtures, which is why the equivalence table is
 * kept in development): every table, view and column that each of the 45 Postgres statement texts uses EXISTS in the oracle (tables from
 * {@code loginpg/oraculo-columnas.csv}, views from {@code loginpg2/oraculo-vistas-columnas.csv}, objects from {@code loginpg/oraculo-objetos.csv}), and the
 * relations a text names are exactly the Postgres names of the relations of its HANA golden. That every column the HANA text cites reaches the Postgres text
 * under its Postgres name is pinned in LoginPgTraduccionTest (39 table statements) and LoginPg2TraduccionVistasTest (6 view statements); the last test of this
 * class proves that those two cover all 45. The word scanner is a real regular expression (the one of the round 2 test had an escaping error, a backspace
 * instead of a word boundary, and checked nothing: found by this round, see TRANSFORMATION_NOTES).
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LoginPg3OraculoSentenciasTest {

    private static final LoginPgRecursos.Oraculo XLSX = LoginPgRecursos.Oraculo.cargar();
    private static final LoginPg2Recursos.OraculoVistas VISTAS = LoginPg2Recursos.OraculoVistas.cargar();
    private static final AtomicInteger EJECUTADOS = new AtomicInteger();

    /** SQL words and functions that can appear in the Postgres statements of the login and are not names of the schema */
    static final Set<String> PALABRAS = Set.of("select", "from", "where", "and", "or", "as", "on", "left", "right", "inner", "outer", "join", "order", "by", "limit", "offset", "asc", "desc",
            "nulls", "first", "last", "is", "null", "not", "in", "like", "between", "exists", "case", "when", "then", "else", "end", "count", "cast", "integer", "bigint", "now", "at", "time",
            "zone", "interval", "second", "insert", "into", "values", "update", "set", "delete", "group", "having", "distinct", "union", "all", "coalesce", "upper", "lower", "rtrim", "ltrim",
            "trim", "to_char", "nextval", "current_timestamp", "true", "false", "min", "max", "sum", "avg", "length", "substring", "replace", "using", "varchar", "text", "date", "timestamp",
            "localtimestamp", "current_date", "to_timestamp", "to_date", "extract", "epoch", "day", "hour", "minute", "year", "month");

    private static final Pattern PALABRA = Pattern.compile("(?<![0-9A-Za-z_$:.])[A-Za-z_][A-Za-z0-9_]*");

    @AfterAll
    void informe() {
        System.out.println("LOGINPG3 oracle statement checks executed: " + EJECUTADOS.get());
    }

    static Stream<String> ids() {
        return LoginPgRecursos.goldenSentencias().keySet().stream();
    }

    private static String pg(String id) {
        return ApiLoginPg3.textoPg(id);
    }

    /** the relations (tables and views) of the HANA golden, by their HANA name */
    private static Set<String> relacionesHana(String id) {
        return LoginPgRecursos.objetosHanaDe(LoginPgRecursos.goldenSentencias().get(id));
    }

    /** the text without string literals, double-quoted names and :parameters, in lower case, with {@code alias.column} turned into two words */
    static String limpio(String sql) {
        return sql.replaceAll("'[^']*'", " ").replaceAll("\"[^\"]*\"", " ").replaceAll(":[A-Za-z_][A-Za-z0-9_]*", " ").replace('.', ' ').toLowerCase();
    }

    static List<String> palabras(String sql) {
        List<String> r = new ArrayList<>();
        Matcher m = PALABRA.matcher(limpio(sql));
        while (m.find()) r.add(m.group());
        return r;
    }

    // ------------------------------------------------------------------------------------------------ the scanner proves itself

    @Test
    @DisplayName("A3-6(a) the word scanner finds words (it is not vacuous) and tells a name of the schema from a keyword, a literal, a parameter and a quoted alias")
    void el_escaner_se_autocomprueba() {
        assertThat(palabras("SELECT u.id_usuario AS \"IdUsuario\" FROM t_pla_acc_usuario u WHERE u.codigo = 'abc def' AND x = :Param_X"))
                .containsExactly("select", "u", "id_usuario", "as", "from", "t_pla_acc_usuario", "u", "where", "u", "codigo", "and", "x");
        assertThat(palabras("SELECT a FROM t_x WHERE b = ?")).containsExactly("select", "a", "from", "t_x", "where", "b");
        assertThat(palabras("")).isEmpty();
    }

    // ------------------------------------------------------------------------------------------------ the check

    @ParameterizedTest(name = "A3-6(a) {0}: every relation named by the Postgres text is a table or view of the oracle, and they are exactly the relations of the HANA golden")
    @MethodSource("ids")
    void las_relaciones_existen_en_el_oraculo(String id) {
        Set<String> esperadas = new TreeSet<>();
        for (String h : relacionesHana(id)) {
            LoginPgRecursos.Objeto o = XLSX.objeto(h);
            assertTrue(o.tipo().equals("tabla") || o.tipo().equals("vista"), id + ": " + h + " is a " + o.tipo());
            assertThat(o.tipo().equals("tabla") ? XLSX.columnas.containsKey(h) : VISTAS.columnas.containsKey(h)).as("oracle has the columns of " + h).isTrue();
            esperadas.add(o.postgres());
        }
        Set<String> todosLosNombres = new HashSet<>();
        XLSX.objetos.values().forEach(o -> todosLosNombres.add(o.postgres()));
        Set<String> nombradas = new TreeSet<>();
        List<String> inexistentes = new ArrayList<>();
        for (String w : palabras(pg(id))) {
            boolean pareceRelacion = w.matches("(t|v|fn|sp|sq)_[a-z0-9_]+");
            if (todosLosNombres.contains(w)) nombradas.add(w);
            else if (pareceRelacion) inexistentes.add(w);
        }
        assertThat(inexistentes).as("names that look like a table or view and are not in the oracle").isEmpty();
        assertEquals(esperadas, nombradas, "relations of the Postgres text of " + id);
        EJECUTADOS.incrementAndGet();
    }

    @ParameterizedTest(name = "A3-6(a) {0}: every other word of the Postgres text is a keyword, an alias or a column of a relation it names, per the oracle")
    @MethodSource("ids")
    void las_columnas_existen_en_el_oraculo(String id) {
        Set<String> relacionesPg = new HashSet<>();
        Set<String> columnasPg = new HashSet<>();
        for (String h : relacionesHana(id)) {
            relacionesPg.add(XLSX.objeto(h).postgres());
            columnasPg.addAll(LoginPg2Recursos.columnasDe(h, XLSX, VISTAS).values());
        }
        List<String> palabras = palabras(pg(id));
        // an alias is the word after a relation name (with or without AS) and the word after "as" in "... ) as q"
        Set<String> alias = new HashSet<>();
        for (int i = 1; i < palabras.size(); i++) {
            String w = palabras.get(i);
            if (relacionesPg.contains(palabras.get(i - 1)) && !PALABRAS.contains(w)) alias.add(w);
            if (i >= 2 && palabras.get(i - 1).equals("as") && relacionesPg.contains(palabras.get(i - 2))) alias.add(w);
        }
        Matcher derivada = Pattern.compile("\\)\\s+(?:as\\s+)?([a-z_][a-z0-9_]*)").matcher(limpioConParentesis(pg(id)));
        while (derivada.find()) if (!PALABRAS.contains(derivada.group(1))) alias.add(derivada.group(1));
        List<String> desconocidas = new ArrayList<>();
        for (String w : palabras) {
            if (PALABRAS.contains(w) || relacionesPg.contains(w) || alias.contains(w) || columnasPg.contains(w)) continue;
            desconocidas.add(w);
        }
        assertThat(desconocidas).as("words of the Postgres text of " + id + " that are no keyword, relation, alias or column of its relations in the oracle").isEmpty();
        assertThat(palabras).as("the scanner saw words in " + id).hasSizeGreaterThan(3);
        EJECUTADOS.incrementAndGet();
    }

    private static String limpioConParentesis(String sql) {
        return sql.replaceAll("'[^']*'", " ").replaceAll("\"[^\"]*\"", " ").replaceAll(":[A-Za-z_][A-Za-z0-9_]*", " ").toLowerCase();
    }

    @ParameterizedTest(name = "A3-6(a) {0}: a column of a relation the text does NOT name is not used (the word is a column only of the relations of this statement)")
    @MethodSource("ids")
    void no_usa_columnas_de_otras_relaciones(String id) {
        Set<String> propias = new HashSet<>();
        Set<String> relacionesPg = new HashSet<>();
        for (String h : relacionesHana(id)) {
            relacionesPg.add(XLSX.objeto(h).postgres());
            propias.addAll(LoginPg2Recursos.columnasDe(h, XLSX, VISTAS).values());
        }
        Set<String> ajenas = new LinkedHashSet<>();
        for (Map<String, String> cols : XLSX.columnas.values()) ajenas.addAll(cols.values());
        for (Map<String, String> cols : VISTAS.columnas.values()) ajenas.addAll(cols.values());
        ajenas.removeAll(propias);
        List<String> mal = new ArrayList<>();
        for (String w : palabras(pg(id))) {
            if (ajenas.contains(w) && !PALABRAS.contains(w) && !relacionesPg.contains(w)) mal.add(w);
        }
        // a word may be an alias that happens to be named like a column of another table (u, usu, cli...): aliases are short and never in the oracle as columns
        assertThat(mal).as("columns of other relations used by " + id).isEmpty();
    }

    /** the structural keywords of a statement in order: every kind of join, UNION, GROUP BY, DISTINCT, HAVING, WHERE, ORDER BY (outside literals and quoted names) */
    static List<String> estructura(String sql) {
        String t = limpioConParentesis(sql).replaceAll("\\s+", " ");
        List<String> r = new ArrayList<>();
        Matcher m = Pattern.compile("\\b(left outer join|right outer join|full outer join|left join|right join|full join|inner join|cross join|join|union all|union|group by|distinct|having|where|order by)\\b")
                .matcher(t);
        while (m.find()) r.add(m.group(1).replace(" outer", ""));
        return r;
    }

    @ParameterizedTest(name = "A3-6(a) {0}: the Postgres text keeps the structure of the HANA golden: the same joins (LEFT stays LEFT), WHERE, ORDER BY, GROUP BY, DISTINCT, UNION")
    @MethodSource("ids")
    void la_estructura_es_la_del_golden(String id) {
        List<String> hana = estructura(LoginPgRecursos.goldenSentencias().get(id));
        List<String> pg = estructura(pg(id));
        assertEquals(hana, pg, "structure of " + id);
        EJECUTADOS.incrementAndGet();
    }

    @Test
    @DisplayName("A3-6(a) the structure scanner sees the joins and clauses (it is not vacuous): one statement with three LEFT JOINs, one without joins")
    void el_escaner_de_estructura_se_autocomprueba() {
        assertEquals(List.of("left join", "left join", "left join", "where"), estructura("select a from t1 LEFT JOIN t2 ON x = y left outer join t3 on 1 = 1 LEFT  JOIN t4 on 2 = 2 WHERE b = 'where join'"));
        assertEquals(List.of("join", "order by"), estructura("select a from t1 join t2 on 1 = 1 order by a"));
        assertEquals(List.of(), estructura("select 1"));
        assertEquals(3L, estructura(LoginPgRecursos.goldenSentencias().get("GestionClaveServicio.SQL_DATOS_RECUPERACION")).stream().filter("left join"::equals).count());
        assertEquals(3L, estructura(LoginPgRecursos.goldenSentencias().get("CrearSesionServicio.QUERY_USUARIOS")).stream().filter("left join"::equals).count());
    }

    // ------------------------------------------------------------------------------------------------ coverage of the mapping checks

    @Test
    @DisplayName("A3-6(a) the 'every HANA column reaches the Postgres text' checks of LoginPgTraduccionTest (39 table statements) and LoginPg2TraduccionVistasTest (6 view statements) together cover all 45 SQL statements, none twice")
    void la_verificacion_cubre_las_45() {
        Set<String> tablas = new TreeSet<>();
        LoginPgTraduccionTest.idsSoloTablas().forEach(tablas::add);
        Set<String> vistas = new TreeSet<>(LoginPg2TraduccionVistasTest.IDS);
        assertEquals(39, tablas.size());
        assertEquals(6, vistas.size());
        Set<String> union = new TreeSet<>(tablas);
        union.addAll(vistas);
        assertEquals(45, union.size(), "no statement is in both groups");
        assertThat(union).containsExactlyInAnyOrderElementsOf(LoginPgRecursos.goldenSentencias().keySet());
        assertEquals(45, ids().count());
    }

    @Test
    @DisplayName("A3-6(a) zero would mean not proven: all 45 statements ran through the relation, column and structure checks (90 + 45 counted cases)")
    void todos_los_casos_se_ejecutaron() {
        int antes = EJECUTADOS.get();
        int n = 0;
        for (String id : (Iterable<String>) ids()::iterator) {
            las_relaciones_existen_en_el_oraculo(id);
            las_columnas_existen_en_el_oraculo(id);
            no_usa_columnas_de_otras_relaciones(id);
            la_estructura_es_la_del_golden(id);
            n++;
        }
        assertEquals(45, n, "statements checked");
        assertEquals(135, EJECUTADOS.get() - antes, "relation, column and structure checks executed (3 per statement)");
    }
}
