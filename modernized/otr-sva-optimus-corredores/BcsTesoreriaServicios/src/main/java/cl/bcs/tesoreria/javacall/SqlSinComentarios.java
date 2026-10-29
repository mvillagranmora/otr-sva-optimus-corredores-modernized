package cl.bcs.tesoreria.javacall;

import java.util.regex.Pattern;

/**
 * Loads a SQL resource the way the legacy {@code cl.bcs.utils.SQL} executed it: without comments (block, {@code //} and {@code --} styles).
 * Besides readability, the HANA-to-H2 rewriting of the test connection recognises a statement by its first keyword, so a leading comment
 * would hide it. Used by the authorization and annulment services.
 */
final class SqlSinComentarios {

    private static final Pattern COMENTARIOS = Pattern.compile("(?:/\\*(?:[^*]|(?:\\*+[^*/]))*\\*+/)|(?://.*)|(?:--.*)");

    private SqlSinComentarios() {
    }

    static String cargar(String ruta) {
        return COMENTARIOS.matcher(TesoreriaJdbcHelper.loadSql(ruta)).replaceAll("").strip();
    }
}
