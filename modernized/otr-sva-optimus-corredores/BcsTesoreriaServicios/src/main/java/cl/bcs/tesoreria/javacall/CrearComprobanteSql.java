package cl.bcs.tesoreria.javacall;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import cl.bcs.tesoreria.javacall.comun.Consultas;

/**
 * SQL plumbing shared by {@link CrearComprobanteJavaService}, {@link CrearComprobanteService} and {@link CrearComprobantePartidaInEgService}, with the
 * conventions of the legacy {@code cl.bcs.utils.SQL}: comments stripped from the text, batch parameters read from the PUBLIC FIELDS of the row objects
 * by parameter name (a BigDecimal is written with scale 16, HALF_DOWN, a null as SQL NULL), a null list is an error.
 */
final class CrearComprobanteSql {

    private static final Pattern COMENTARIOS = Pattern.compile("(?:/\\*(?:[^*]|(?:\\*+[^*/]))*\\*+/)|(?://.*)|(?:--.*)");

    private CrearComprobanteSql() {
    }

    /** Classpath SQL resource with its comments removed (the legacy removed them before every execution). */
    static String cargar(String recurso) {
        return COMENTARIOS.matcher(TesoreriaJdbcHelper.loadSql(recurso)).replaceAll("");
    }

    /** A resource that holds one statement per non blank line (the clean-up of the global temporary tables). */
    static List<String> sentencias(String recurso) {
        List<String> sentencias = new ArrayList<>();
        for (String linea : cargar(recurso).split("\\R")) {
            if (!linea.isBlank()) {
                sentencias.add(linea.strip());
            }
        }
        return sentencias;
    }

    /** Legacy {@code SQL.exec}: runs every statement as it is. */
    static void ejecutar(NamedParameterJdbcTemplate jdbc, List<String> sentencias) {
        sentencias.forEach(jdbc.getJdbcOperations()::execute);
    }

    /** Legacy {@code SQL.batch}: one execution per row object; nothing is executed for an empty list. */
    static void lote(NamedParameterJdbcTemplate jdbc, String sql, List<?> filas) throws SQLException {
        if (filas == null) {
            // LEGACY DEFECT kept (see README TES-A-524, same SQL.batch behaviour): a null list escapes as a SQLException from SQL.batch, not as a validation message
            throw new SQLException("Null parameters. If parameters aren't need, pass an empty");
        }
        List<Map<String, Object>> parametros = new ArrayList<>();
        for (Object fila : filas) {
            parametros.add(camposPublicos(fila));
        }
        Consultas.lote(jdbc, sql, parametros);
    }

    private static Map<String, Object> camposPublicos(Object fila) throws SQLException {
        Map<String, Object> valores = new LinkedHashMap<>();
        try {
            for (Field campo : fila.getClass().getFields()) {
                Object valor = campo.get(fila);
                valores.put(campo.getName(), valor instanceof BigDecimal b ? b.setScale(Consultas.ESCALA_LOTE, RoundingMode.HALF_DOWN) : valor);
            }
        } catch (IllegalAccessException e) {
            throw new SQLException("Map Object failed", e);
        }
        return valores;
    }
}
