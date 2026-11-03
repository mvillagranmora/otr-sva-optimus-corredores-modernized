package cl.bcs.plataforma.login.dialecto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * LOGIN-PG round 2, part A (T-31): the Postgres text of the 6 statements over views. Same rules as round 1 (no HANA token, exact objects, every column
 * under its Postgres name, quoted names only as aliases, same parameters, TOP -> LIMIT) plus the rules of views: every table, view and column the text
 * uses EXISTS in the catalog oracle (xlsx for tables, QA catalog for views), no {@code SELECT *} (the Postgres text lists explicit columns aliased to
 * the HANA labels and, for SQL_ACCESOS, exactly the fields of the POJO {@code UsuarioAcceso}), and the ORDER BY of HANA keeps its NULL ordering
 * (HANA sorts NULL first, Postgres last: {@code NULLS FIRST}, Q-LOGINPG2-6).
 */
class LoginPg2TraduccionVistasTest {

    private static final LoginPgRecursos.Oraculo XLSX = LoginPgRecursos.Oraculo.cargar();
    private static final LoginPg2Recursos.OraculoVistas VISTAS = LoginPg2Recursos.OraculoVistas.cargar();

    static final List<String> IDS = List.of("BootstrapServicio.SQL_ACCESOS", "BootstrapServicio.SQL_EMPRESAS", "BootstrapServicio.SQL_IMAGEN",
            "BootstrapServicio.SQL_PREFERENCIAS", "BootstrapServicio.SQL_PREFERENCIAS_GRILLA", "GestionClaveServicio.SQL_DATOS_RECUPERACION");

    static Stream<String> ids() {
        return IDS.stream();
    }

    private static String pg(String id) {
        return ApiLoginPg3.textoPg(id);
    }

    private static String hana(String id) {
        return LoginPgRecursos.goldenSentencias().get(id);
    }

    private static String pgDe(String objetoHana) {
        return XLSX.objetos.get(objetoHana).postgres();
    }

    /** lower case outside quotes, whitespace collapsed, no blanks around , ( ) = and no final semicolon: layout and keyword case do not matter, aliases and literals do */
    static String canonico(String sql) {
        StringBuilder sb = new StringBuilder();
        boolean simple = false, doble = false;
        for (int i = 0; i < sql.length(); i++) {
            char c = sql.charAt(i);
            if (simple) {
                sb.append(c);
                if (c == '\'') simple = false;
            } else if (doble) {
                sb.append(c);
                if (c == '"') doble = false;
            } else if (c == '\'') {
                simple = true;
                sb.append(c);
            } else if (c == '"') {
                doble = true;
                sb.append(c);
            } else {
                sb.append(Character.toLowerCase(c));
            }
        }
        String t = sb.toString().replaceAll("\\s+", " ").trim().replaceAll(" ?([,()=]) ?", "$1");
        return t.endsWith(";") ? t.substring(0, t.length() - 1).trim() : t;
    }

    // ================================================================================================ scope (green)

    @Test
    @DisplayName("LOGIN-PG2 scope: the six statements over views are the statements of the golden that touch a view, and they are all in the catalog of ids")
    void alcance() {
        Set<String> conVistas = new TreeSet<>();
        LoginPgRecursos.goldenSentencias().forEach((id, sql) -> {
            for (String o : LoginPgRecursos.objetosHanaDe(sql)) if (XLSX.objeto(o).tipo().equals("vista")) conVistas.add(id);
        });
        assertEquals(new TreeSet<>(IDS), conVistas);
        for (String id : IDS) assertTrue(LoginPgRecursos.goldenSql().containsKey(id), id);
    }

    @ParameterizedTest(name = "LOGIN-PG2 golden {0}: every column the HANA text uses exists in a table or view it references, per the oracles")
    @MethodSource("ids")
    void columnas_del_golden_existen(String id) {
        String sql = hana(id);
        Set<String> objetos = LoginPgRecursos.objetosHanaDe(sql);
        List<String> sinDueno = new ArrayList<>();
        LoginPgRecursos.identificadoresHana(sql).forEach((n, c) -> {
            if (c[0] == 0) return;
            boolean esColumna = objetos.stream().anyMatch(o -> LoginPg2Recursos.columnasDe(o, XLSX, VISTAS).containsKey(n));
            if (!esColumna && c[1] == 0) sinDueno.add(n);
        });
        assertThat(sinDueno).isEmpty();
    }

    @Test
    @DisplayName("LOGIN-PG2 the only HANA label of the six statements that is not a column is the alias 'corredora' (an alias of DTOS.RazonSocial, not a view column)")
    void corredora_es_alias() {
        String sql = hana("GestionClaveServicio.SQL_DATOS_RECUPERACION");
        int[] c = LoginPgRecursos.identificadoresHana(sql).get("corredora");
        assertEquals(0, c[0]);
        assertEquals(1, c[1]);
        for (LoginPg2Recursos.ColumnaVista f : VISTAS.filas) assertThat(f.columnaHana()).isNotEqualTo("corredora");
    }

    // ================================================================================================ Postgres text (red)

    @Test
    @DisplayName("A3-3 every one of the 55 statements has a text file in Postgres: texto() raises for none")
    void todas_las_sentencias_tienen_texto() {
        SentenciasSql s = ApiLoginPg3.sentenciasPostgres();
        assertEquals(55, s.ids().size());
        for (String id : new TreeSet<>(s.ids())) {
            assertThat(s.texto(id)).as(id).isNotBlank();
        }
    }

    @ParameterizedTest(name = "LOGIN-PG2 postgres {0}: no HANA token is left")
    @MethodSource("ids")
    void sin_fichas_de_hana(String id) {
        String t = pg(id);
        for (Pattern p : LoginPgRecursos.FICHAS_HANA) assertThat(p.matcher(t).find()).as("HANA token " + p + " in " + t).isFalse();
    }

    @ParameterizedTest(name = "LOGIN-PG2 postgres {0}: no SELECT * and no table.* (explicit columns aliased to the HANA names)")
    @MethodSource("ids")
    void sin_select_estrella(String id) {
        String t = pg(id);
        assertThat(t).doesNotContainPattern("(?is)select\\s+(distinct\\s+)?\\*").doesNotContainPattern("\\.\\s*\\*");
    }

    @ParameterizedTest(name = "LOGIN-PG2 postgres {0}: references exactly the Postgres names of the tables and views of the HANA statement")
    @MethodSource("ids")
    void objetos_exactos(String id) {
        Set<String> esperadas = new HashSet<>();
        LoginPgRecursos.objetosHanaDe(hana(id)).forEach(h -> esperadas.add(pgDe(h)));
        Set<String> todos = new HashSet<>();
        XLSX.objetos.values().forEach(o -> todos.add(o.postgres()));
        Set<String> halladas = new HashSet<>();
        Matcher m = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*").matcher(pg(id));
        while (m.find()) if (todos.contains(m.group())) halladas.add(m.group());
        assertEquals(esperadas, halladas);
    }

    @ParameterizedTest(name = "LOGIN-PG2 postgres {0}: every table, view and column the text uses exists in the catalog oracle of the objects it references")
    @MethodSource("ids")
    void todo_lo_que_usa_existe_en_el_catalogo(String id) {
        String t = pg(id);
        Set<String> objetosPg = new HashSet<>();
        Set<String> columnasPg = new HashSet<>();
        for (String h : LoginPgRecursos.objetosHanaDe(hana(id))) {
            objetosPg.add(pgDe(h));
            columnasPg.addAll(LoginPg2Recursos.columnasDe(h, XLSX, VISTAS).values());
        }
        // strip string literals, quoted identifiers and :parameters
        String limpio = t.replaceAll("'[^']*'", " ").replaceAll("\"[^\"]*\"", " ").replaceAll(":[A-Za-z_][A-Za-z0-9_]*", " ").toLowerCase();
        Set<String> palabras = Set.of("select", "from", "where", "and", "or", "as", "on", "left", "inner", "join", "order", "by", "limit", "asc", "desc", "nulls", "first", "last", "is",
                "null", "not", "in", "like");
        List<String> desconocidos = new ArrayList<>();
        List<String> tokens = new ArrayList<>();
        Matcher m = Pattern.compile("(?<![0-9])\\b[a-z_][a-z0-9_]*\\b").matcher(limpio);
        while (m.find()) tokens.add(m.group());
        // a table alias is the word right after an object name
        Set<String> aliases = new HashSet<>();
        for (int i = 1; i < tokens.size(); i++) {
            if (objetosPg.contains(tokens.get(i - 1)) && !palabras.contains(tokens.get(i))) aliases.add(tokens.get(i));
            if (i >= 2 && tokens.get(i - 1).equals("as") && objetosPg.contains(tokens.get(i - 2))) aliases.add(tokens.get(i));
        }
        for (String w : tokens) {
            if (!palabras.contains(w) && !objetosPg.contains(w) && !aliases.contains(w) && !columnasPg.contains(w)) desconocidos.add(w);
        }
        assertThat(desconocidos).as("identifiers that are neither a keyword, an object, an alias nor a column of the referenced objects in: " + t).isEmpty();
    }

    @ParameterizedTest(name = "LOGIN-PG2 postgres {0}: every column of the HANA statement appears under its Postgres name")
    @MethodSource("ids")
    void columnas_traducidas(String id) {
        String sql = hana(id);
        Set<String> objetos = LoginPgRecursos.objetosHanaDe(sql);
        String t = pg(id);
        List<String> faltan = new ArrayList<>();
        LoginPgRecursos.identificadoresHana(sql).forEach((n, c) -> {
            if (c[0] == 0) return;
            Set<String> pgs = new HashSet<>();
            for (String o : objetos) {
                String col = LoginPg2Recursos.columnasDe(o, XLSX, VISTAS).get(n);
                if (col != null) pgs.add(col);
            }
            for (String col : pgs) if (!Pattern.compile("\\b" + col + "\\b").matcher(t).find()) faltan.add(n + " -> " + col);
        });
        assertThat(faltan).isEmpty();
    }

    @ParameterizedTest(name = "LOGIN-PG2 postgres {0}: every double-quoted name is an alias defined with AS (the label keeps the HANA name)")
    @MethodSource("ids")
    void comillas_solo_en_alias(String id) {
        String t = pg(id);
        List<String> sinAlias = new ArrayList<>();
        LoginPgRecursos.identificadoresHana(t).forEach((n, c) -> {
            if (c[1] == 0) sinAlias.add(n);
        });
        assertThat(sinAlias).isEmpty();
        assertThat(t).doesNotContain("\"BSCL");
    }

    @ParameterizedTest(name = "LOGIN-PG2 postgres {0}: the :name parameters are the same as in HANA")
    @MethodSource("ids")
    void parametros_iguales(String id) {
        assertThat(new HashSet<>(LoginPgRecursos.parametros(pg(id)))).isEqualTo(new HashSet<>(LoginPgRecursos.parametros(hana(id))));
    }

    @Test
    @DisplayName("LOGIN-PG2 postgres SQL_DATOS_RECUPERACION: TOP 1 becomes LIMIT 1 at the end, and the three LEFT JOINs of HANA stay LEFT JOINs (a user without person or contract must still come back)")
    void datos_recuperacion_limit_y_left_joins() {
        String t = canonico(pg("GestionClaveServicio.SQL_DATOS_RECUPERACION"));
        assertThat(t).endsWith(" limit 1").doesNotContainPattern("\\btop\\b");
        assertEquals(3, Pattern.compile("\\bleft join\\b").matcher(t).results().count());
        assertThat(t).doesNotContainPattern("\\binner join\\b");
    }

    @Test
    @DisplayName("LOGIN-PG2 postgres SQL_ACCESOS: the labels are exactly the ten fields of the POJO BootstrapServicio.UsuarioAcceso (HANA's SELECT * returned every column of the view; the POJO read ten)")
    void accesos_lista_los_campos_del_pojo() throws Exception {
        Set<String> campos = new TreeSet<>();
        for (Field f : Class.forName("cl.bcs.plataforma.login.bootstrap.BootstrapServicio$UsuarioAcceso").getFields()) campos.add(f.getName());
        assertEquals(10, campos.size());
        List<String> etiquetas = new ArrayList<>();
        Matcher m = Pattern.compile("(?i)\\bas\\s+\"([^\"]+)\"").matcher(pg("BootstrapServicio.SQL_ACCESOS"));
        while (m.find()) etiquetas.add(m.group(1));
        assertEquals(10, etiquetas.size(), "one label per column: " + etiquetas);
        assertEquals(campos, new TreeSet<>(etiquetas));
    }

    @Test
    @DisplayName("LOGIN-PG2 postgres SQL_ACCESOS keeps the HANA filters and the order: id_empresa, id_usuario and mostrar_acceso = 'S', ORDER BY padre, orden each NULLS FIRST (HANA sorts NULL first; Postgres would sort it last)")
    void accesos_filtros_y_orden() {
        String t = canonico(pg("BootstrapServicio.SQL_ACCESOS"));
        assertThat(t).contains(" where id_empresa=:idempresa and id_usuario=:idusuario and mostrar_acceso='S' ");
        assertThat(t).endsWith(" order by padre nulls first,orden nulls first");
    }

    @Test
    @DisplayName("LOGIN-PG2 postgres SQL_DATOS_RECUPERACION: the labels are the HANA ones in the HANA order, 'corredora' comes from the contract view's razon_social")
    void datos_recuperacion_etiquetas() {
        List<String> etiquetas = new ArrayList<>();
        Matcher m = Pattern.compile("(?i)\\bas\\s+\"([^\"]+)\"").matcher(pg("GestionClaveServicio.SQL_DATOS_RECUPERACION"));
        while (m.find()) etiquetas.add(m.group(1));
        assertEquals(List.of("IdPersona", "IdDireccionElectronicaPrincipal", "NombreCompleto", "corredora", "IdEmpresa", "IdUsuario", "TipoEntidad"), etiquetas);
        assertThat(canonico(pg("GestionClaveServicio.SQL_DATOS_RECUPERACION"))).contains("razon_social as \"corredora\"");
    }

    // ------------------------------------------------------------------------------------------------ literal texts

    private static final Map<String, String> LITERAL = new LinkedHashMap<>();

    static {
        LITERAL.put("BootstrapServicio.SQL_EMPRESAS",
                "SELECT id_empresa AS \"IdEmpresa\", nombre AS \"Nombre\" FROM v_pla_pla_consultar_empresa WHERE id_estado = 'ACT'");
        LITERAL.put("BootstrapServicio.SQL_IMAGEN",
                "SELECT nombre_imagen AS \"NombreImagen\", binario AS \"Binario\" FROM v_pla_pla_consultar_imagen WHERE nombre_imagen = 'LOGO_CORREDOR_LOGIN'");
        LITERAL.put("BootstrapServicio.SQL_PREFERENCIAS",
                "SELECT llave AS \"Llave\", valor AS \"Valor\" FROM v_pla_pla_consultar_configuracion_usuario WHERE id_empresa = :IdEmpresa AND id_usuario = :IdUsuario");
        LITERAL.put("BootstrapServicio.SQL_PREFERENCIAS_GRILLA",
                "SELECT llave AS \"Llave\", valor AS \"Valor\" FROM v_pla_pla_consultar_configuracion_grilla WHERE id_empresa = :IdEmpresa");
        LITERAL.put("BootstrapServicio.SQL_ACCESOS",
                "SELECT id_elemento_html AS \"IdElementoHtml\", es_visible_menu AS \"EsVisibleMenu\", existe AS \"Existe\", id_usuario AS \"IdUsuario\", id_acceso AS \"IdAcceso\", "
                        + "nombre_acceso AS \"NombreAcceso\", tipo_acceso AS \"TipoAcceso\", padre AS \"Padre\", orden AS \"Orden\", id_servicio AS \"IdServicio\" "
                        + "FROM v_pla_acc_consultar_usuario_acceso WHERE id_empresa = :IdEmpresa AND id_usuario = :IdUsuario AND mostrar_acceso = 'S' "
                        + "ORDER BY padre NULLS FIRST, orden NULLS FIRST");
        LITERAL.put("GestionClaveServicio.SQL_DATOS_RECUPERACION",
                "SELECT cli.id_persona AS \"IdPersona\", per.id_direccion_electronica_principal AS \"IdDireccionElectronicaPrincipal\", per.nombre_completo AS \"NombreCompleto\", "
                        + "dtos.razon_social AS \"corredora\", cli.id_empresa AS \"IdEmpresa\", usu.id_usuario AS \"IdUsuario\", usu.tipo_entidad AS \"TipoEntidad\" "
                        + "FROM t_pla_acc_usuario usu "
                        + "LEFT JOIN t_socneg_soc_socio_de_negocio cli ON (usu.tipo_entidad = 'CLI' AND usu.id_entidad = cli.id_cliente) "
                        + "OR (usu.tipo_entidad = 'EMP' AND usu.id_entidad = cli.id_empleado) OR (usu.tipo_entidad = 'PER' AND usu.id_entidad = cli.id_persona) "
                        + "LEFT JOIN v_socneg_per_consultar_persona per ON per.id_persona = cli.id_persona "
                        + "LEFT JOIN v_pla_ctt_consultar_contrato_datos_generales dtos ON dtos.id_empresa = cli.id_empresa "
                        + "WHERE usu.codigo_usuario = :CodigoUsuario LIMIT 1");
    }

    @ParameterizedTest(name = "LOGIN-PG2 literal postgres text of {0} (layout, keyword case and table aliases do not matter; labels, literals, columns and order do)")
    @MethodSource("ids")
    void texto_literal(String id) {
        assertEquals(canonico(LITERAL.get(id)), canonico(pg(id)));
    }

    @Test
    @DisplayName("LOGIN-PG2 the literal expectations of this class are canonical-equal to nothing but themselves: canonico() keeps alias case and string literals, drops layout")
    void canonico_se_autocomprueba() {
        assertEquals(canonico("SELECT a AS \"X\"  FROM t\n WHERE b = 'Ab' ;"), canonico("select a as \"X\" from t where b='Ab'"));
        assertThat(canonico("select a as \"x\" from t")).isNotEqualTo(canonico("select a as \"X\" from t"));
        assertThat(canonico("select 1 where b='ab'")).isNotEqualTo(canonico("select 1 where b='AB'"));
        assertEquals(6, LITERAL.size());
        assertEquals(new TreeSet<>(IDS), new TreeSet<>(LITERAL.keySet()));
    }
}
