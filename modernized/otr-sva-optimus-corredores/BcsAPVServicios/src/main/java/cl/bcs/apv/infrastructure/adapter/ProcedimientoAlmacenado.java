package cl.bcs.apv.infrastructure.adapter;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import cl.bcs.apv.infrastructure.jdbc.SqlNombrado;

/**
 * Runs a SAP HANA stored procedure on behalf of a service (the {@code StoredProcedure} collaborator). The stored procedures themselves are
 * outside the equivalence proof, and this adapter has NOT been run against HANA: it reproduces the conventions of the legacy
 * {@code SQL.call} / {@code NamedCallableStatement} (same marker parsing, same parameter numbering, same by-name reading of the outputs) and is
 * covered only by a unit test with a fake statement.
 *
 * <p>Request: {@code {"procedimiento": "BSCL.APV.BD.Servicios::ActualizarFolioSuscripcion", "entrada": {"p_IdEmpresa": 1, ...}}}: the procedure name and its
 * SCALAR inputs. The call text is the legacy one, kept as a resource named after the procedure
 * ({@code sql/Procedimiento_<name with every non alphanumeric character replaced by _>.sql}) and written with the legacy markers:
 * {@code :x} scalar input (taken from {@code entrada}), {@code #tabla} a session temporary table that the service filled before the call (left in
 * the text), {@code @x} scalar output, {@code $x} output table. The temporary tables live in the connection of the running transaction, which is
 * the one used here.
 *
 * <p>Response: {@code {"<out name>": scalar | [rows]}}. Scalar outputs are read as text (the driver converts), so a numeric output reaches the
 * caller as a JSON string that Gson converts to the numeric field it is decoded into. Output tables are matched to the {@code $x} markers in
 * declaration order, as the driver returns them as result sets; each row is an object keyed by column label.
 */
@Component
public class ProcedimientoAlmacenado {

    private static final Pattern COMENTARIOS = Pattern.compile("(?:/\\*(?:[^*]|(?:\\*+[^*/]))*\\*+/)|(?://.*)|(?:--.*)");
    private static final String FORMATO_FECHA = "yyyy-MM-dd HH:mm:ss.SSS";

    private final JdbcTemplate jdbc;

    public ProcedimientoAlmacenado(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc.getJdbcTemplate();
    }

    public JsonObject ejecutar(JsonObject peticion) {
        String nombre = peticion.get("procedimiento").getAsString();
        JsonObject entrada = peticion.has("entrada") && peticion.get("entrada").isJsonObject() ? peticion.getAsJsonObject("entrada") : new JsonObject();
        Llamada llamada = Llamada.analizar(texto(nombre));
        return jdbc.execute((java.sql.Connection con) -> {
            try (CallableStatement cs = con.prepareCall(llamada.jdbc())) {
                for (Marcador x : llamada.entradas()) {
                    JsonElement v = entrada.get(x.nombre());
                    if (v == null || v.isJsonNull()) {
                        cs.setNull(x.indice(), Types.VARCHAR);
                    } else {
                        cs.setObject(x.indice(), valor(v));
                    }
                }
                for (Marcador s : llamada.salidas()) {
                    cs.registerOutParameter(s.indice(), Types.NVARCHAR);
                }
                cs.execute();
                JsonObject salida = new JsonObject();
                for (Marcador s : llamada.salidas()) {
                    salida.addProperty(s.nombre(), cs.getString(s.nombre()));
                }
                leerTablas(cs, llamada.tablas(), salida);
                return salida;
            } catch (SQLException e) {
                throw new DataAccessResourceFailureException("stored procedure " + nombre + " failed", e);
            }
        });
    }

    static String texto(String procedimiento) {
        return SqlNombrado.recurso("Procedimiento_" + procedimiento.replaceAll("[^A-Za-z0-9]", "_"));
    }

    private static Object valor(JsonElement v) {
        JsonPrimitive p = v.getAsJsonPrimitive();
        if (p.isNumber()) {
            return p.getAsBigDecimal();
        }
        return p.isBoolean() ? (Object) p.getAsBoolean() : p.getAsString();
    }

    private static void leerTablas(CallableStatement cs, List<String> tablas, JsonObject salida) throws SQLException {
        int n = 0;
        boolean hayResultado = cs.getResultSet() != null;
        while (hayResultado) {
            try (ResultSet rs = cs.getResultSet()) {
                String clave = n < tablas.size() ? tablas.get(n) : "ResultSet_" + n;
                salida.add(clave, filas(rs));
                n++;
            }
            hayResultado = cs.getMoreResults();
        }
    }

    private static JsonArray filas(ResultSet rs) throws SQLException {
        ResultSetMetaData md = rs.getMetaData();
        JsonArray filas = new JsonArray();
        while (rs.next()) {
            JsonObject fila = new JsonObject();
            for (int c = 1; c <= md.getColumnCount(); c++) {
                fila.add(md.getColumnLabel(c), json(rs.getObject(c)));
            }
            filas.add(fila);
        }
        return filas;
    }

    private static JsonElement json(Object o) {
        if (o == null) {
            return JsonNull.INSTANCE;
        }
        if (o instanceof Number n) {
            return new JsonPrimitive(n instanceof BigDecimal ? n : new BigDecimal(n.toString()));
        }
        if (o instanceof Timestamp t) {
            return new JsonPrimitive(new SimpleDateFormat(FORMATO_FECHA).format(t));
        }
        return new JsonPrimitive(o.toString());
    }

    /** A named marker of the call text and the JDBC position the legacy numbering gives it. */
    record Marcador(String nombre, int indice) {
    }

    /** The legacy call text turned into a JDBC statement plus its markers. */
    record Llamada(String jdbc, List<Marcador> entradas, List<Marcador> salidas, List<String> tablas) {

        /**
         * Legacy {@code NamedCallableStatement.parse}: outside quotes, {@code :x} and {@code @x} become {@code ?} and take the next position,
         * {@code $x} becomes {@code ?} WITHOUT taking a position (output table, no bind).
         */
        static Llamada analizar(String texto) {
            String sql = COMENTARIOS.matcher(texto).replaceAll("");
            StringBuilder jdbc = new StringBuilder(sql.length());
            List<Marcador> entradas = new ArrayList<>();
            List<Marcador> salidas = new ArrayList<>();
            List<String> tablas = new ArrayList<>();
            boolean comillaSimple = false;
            boolean comillaDoble = false;
            int indice = 1;
            for (int i = 0; i < sql.length(); i++) {
                char c = sql.charAt(i);
                if (comillaSimple) {
                    comillaSimple = c != '\'';
                } else if (comillaDoble) {
                    comillaDoble = c != '"';
                } else if (c == '\'') {
                    comillaSimple = true;
                } else if (c == '"') {
                    comillaDoble = true;
                } else if ((c == ':' || c == '@' || c == '$') && i + 1 < sql.length() && Character.isJavaIdentifierStart(sql.charAt(i + 1))) {
                    int j = i + 2;
                    while (j < sql.length() && Character.isJavaIdentifierPart(sql.charAt(j))) {
                        j++;
                    }
                    String nombre = sql.substring(i + 1, j);
                    switch (c) {
                        case ':' -> entradas.add(new Marcador(nombre, indice++));
                        case '@' -> salidas.add(new Marcador(nombre, indice++));
                        default -> tablas.add(nombre);
                    }
                    jdbc.append('?');
                    i = j - 1;
                    continue;
                }
                jdbc.append(c);
            }
            return new Llamada(jdbc.toString(), entradas, salidas, tablas);
        }
    }
}
