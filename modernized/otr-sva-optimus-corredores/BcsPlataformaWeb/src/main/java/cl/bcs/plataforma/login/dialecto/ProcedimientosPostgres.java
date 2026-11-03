package cl.bcs.plataforma.login.dialecto;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import cl.bcs.plataforma.config.NoDisponibleException;
import cl.bcs.plataforma.login.ProcedimientoAlmacenado;
import cl.bcs.plataforma.login.dialecto.DefinicionProcedimiento.Parametro;

/**
 * The eight stored procedures of the login in Postgres, called by their HANA name so the callers do not change. A procedure is called with the JDBC escape
 * {@code {call name(?,...)}}: IN values by position, OUT and INOUT registered, and the answer carries every OUT value under its legacy HANA name
 * ({@code P_OUT_ESADMIN}...). A cursor OUT (warnings and errors of the password recovery) is read to the end and returned as a JSON array of rows. The four
 * composite arrays of the event procedure are built from the JSON rows. The table function {@code ConsultarFormatos} is a {@code SELECT * FROM}.
 *
 * <p>Any other name is a 503 ({@link UnsupportedOperationException}) before anything reaches the connection. The connection belongs to the caller: it is never committed, rolled
 * back or closed here, and a cursor needs its open transaction. No state is kept between calls.
 */
public class ProcedimientosPostgres implements ProcedimientoAlmacenado {

    public ProcedimientosPostgres() {
    }

    @Override
    public JsonObject llamar(Connection con, String rutaServicio, JsonObject entrada) throws Exception {
        DefinicionProcedimiento definicion = DefinicionesProcedimientos.de(rutaServicio);
        if (definicion == null) {
            throw new NoDisponibleException("Stored procedure " + rutaServicio + " is not available in postgres mode (T-33)");
        }
        String nombrePostgres = definicion.nombrePostgres();
        if (con == null) {
            throw new SQLException("Null connection");
        }
        return definicion.esFuncion() ? llamarFuncion(con, definicion, nombrePostgres) : llamarProcedimiento(con, definicion, nombrePostgres, entrada);
    }

    // ------------------------------------------------------------------------------------------------ table function

    private JsonObject llamarFuncion(Connection con, DefinicionProcedimiento definicion, String nombrePostgres) throws SQLException {
        JsonArray filas = new JsonArray();
        try (PreparedStatement ps = con.prepareStatement("SELECT * FROM " + nombrePostgres + "()"); ResultSet rs = ps.executeQuery()) {
            ResultSetMetaData columnas = rs.getMetaData();
            while (rs.next()) {
                JsonObject fila = new JsonObject();
                for (int i = 1; i <= columnas.getColumnCount(); i++) {
                    String etiqueta = columnas.getColumnLabel(i);
                    fila.addProperty(definicion.etiquetaHanaPorColumna().getOrDefault(etiqueta, etiqueta), rs.getString(i));
                }
                filas.add(fila);
            }
        }
        JsonObject respuesta = new JsonObject();
        respuesta.add(definicion.claveDeLaTabla(), filas);
        return respuesta;
    }

    // ------------------------------------------------------------------------------------------------ procedure

    private JsonObject llamarProcedimiento(Connection con, DefinicionProcedimiento definicion, String nombrePostgres, JsonObject entrada) throws SQLException {
        try (CallableStatement llamada = con.prepareCall(textoDeLaLlamada(nombrePostgres, definicion.parametros().size()))) {
            for (Parametro parametro : definicion.parametros()) {
                enlazar(con, llamada, definicion, parametro, entrada);
            }
            llamada.execute();
            JsonObject respuesta = new JsonObject();
            for (Parametro parametro : definicion.parametros()) {
                if (!parametro.esEntrada()) {
                    respuesta.add(parametro.nombreHana(), salida(llamada, parametro));
                }
            }
            return respuesta;
        }
    }

    private static String textoDeLaLlamada(String nombrePostgres, int parametros) {
        return "{call " + nombrePostgres + "(" + "?,".repeat(Math.max(parametros - 1, 0)) + (parametros > 0 ? "?" : "") + ")}";
    }

    private static void enlazar(Connection con, CallableStatement llamada, DefinicionProcedimiento definicion, Parametro parametro, JsonObject entrada)
            throws SQLException {
        int posicion = parametro.posicion();
        if (parametro.esEntrada()) {
            enlazarEntrada(con, llamada, definicion, parametro, entrada);
            return;
        }
        if (parametro.esCursor()) {
            llamada.setNull(posicion, Types.OTHER);
        }
        llamada.registerOutParameter(posicion, tipoJdbcDeSalida(parametro));
    }

    private static void enlazarEntrada(Connection con, CallableStatement llamada, DefinicionProcedimiento definicion, Parametro parametro, JsonObject entrada)
            throws SQLException {
        int posicion = parametro.posicion();
        if (parametro.esArreglo()) {
            llamada.setArray(posicion, arreglo(con, definicion, parametro, entrada));
            return;
        }
        JsonElement valor = ClavesJson.buscar(entrada, parametro.nombreHana());
        boolean nulo = ClavesJson.esNulo(valor);
        if ("bigint".equals(parametro.tipoPostgres())) {
            if (nulo) {
                llamada.setNull(posicion, Types.BIGINT);
            } else {
                llamada.setLong(posicion, Long.parseLong(valor.getAsString()));
            }
        } else if (nulo) {
            // the legacy passes an empty text for the origin of the event and the Postgres default is the empty text too
            if ("p_aplica_origen_evento".equals(parametro.nombrePostgres())) {
                llamada.setString(posicion, "");
            } else {
                llamada.setNull(posicion, Types.VARCHAR);
            }
        } else {
            llamada.setString(posicion, valor.getAsString());
        }
    }

    private static java.sql.Array arreglo(Connection con, DefinicionProcedimiento definicion, Parametro parametro, JsonObject entrada) throws SQLException {
        JsonElement valor = ClavesJson.buscar(entrada, parametro.nombreHana());
        List<Object> filas = new ArrayList<>();
        if (valor != null && valor.isJsonArray()) {
            for (JsonElement fila : valor.getAsJsonArray()) {
                filas.add(LiteralCompuesto.de(fila.getAsJsonObject(), definicion.atributosPorParametro().get(parametro.nombrePostgres())));
            }
        }
        return con.createArrayOf(parametro.tipoDelElemento(), filas.toArray());
    }

    private static int tipoJdbcDeSalida(Parametro parametro) {
        if (parametro.esCursor()) {
            return Types.OTHER;
        }
        return "integer".equals(parametro.tipoPostgres()) ? Types.INTEGER : Types.VARCHAR;
    }

    private static JsonElement salida(CallableStatement llamada, Parametro parametro) throws SQLException {
        if (!parametro.esCursor()) {
            String texto = llamada.getString(parametro.posicion());
            return texto == null ? JsonNull.INSTANCE : new JsonPrimitive(texto);
        }
        JsonArray filas = new JsonArray();
        if (llamada.getObject(parametro.posicion()) instanceof ResultSet cursor) {
            try (cursor) {
                ResultSetMetaData columnas = cursor.getMetaData();
                while (cursor.next()) {
                    JsonObject fila = new JsonObject();
                    for (int i = 1; i <= columnas.getColumnCount(); i++) {
                        fila.addProperty(columnas.getColumnLabel(i), cursor.getString(i));
                    }
                    filas.add(fila);
                }
            }
        }
        return filas;
    }
}
