package cl.bcs.plataforma.login.dialecto;

import java.util.Base64;
import java.util.HexFormat;
import java.util.List;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cl.bcs.plataforma.login.dialecto.DefinicionProcedimiento.Atributo;

/**
 * The text form of a row of a composite type, {@code (f1,f2,...)}, which is what an element of a composite array is for Postgres. The attributes go in the
 * order of the type, not of the JSON keys; a missing key or a JSON null is an empty field; a field is double-quoted when it is empty text or has a comma,
 * parenthesis, double quote, backslash or whitespace (inside quotes {@code "} and {@code \} are doubled). A {@code bytea} attribute arrives as base64 and is
 * written as {@code \x} plus lower-case hex.
 */
final class LiteralCompuesto {

    private LiteralCompuesto() {
    }

    static String de(JsonObject fila, List<Atributo> atributos) {
        StringBuilder literal = new StringBuilder("(");
        for (int i = 0; i < atributos.size(); i++) {
            Atributo atributo = atributos.get(i);
            JsonElement valor = ClavesJson.buscar(fila, atributo.nombreHana());
            String texto = ClavesJson.esNulo(valor) ? null : valor.getAsString();
            if (texto != null && "bytea".equals(atributo.tipoPostgres())) {
                texto = "\\x" + HexFormat.of().formatHex(Base64.getDecoder().decode(texto));
            }
            literal.append(i == 0 ? "" : ",").append(campo(texto));
        }
        return literal.append(")").toString();
    }

    private static String campo(String texto) {
        if (texto == null) {
            return "";
        }
        boolean hayQueCitar = texto.isEmpty();
        for (int i = 0; i < texto.length() && !hayQueCitar; i++) {
            char c = texto.charAt(i);
            hayQueCitar = c == ',' || c == '(' || c == ')' || c == '"' || c == '\\' || Character.isWhitespace(c);
        }
        return hayQueCitar ? "\"" + texto.replace("\\", "\\\\").replace("\"", "\"\"") + "\"" : texto;
    }
}
