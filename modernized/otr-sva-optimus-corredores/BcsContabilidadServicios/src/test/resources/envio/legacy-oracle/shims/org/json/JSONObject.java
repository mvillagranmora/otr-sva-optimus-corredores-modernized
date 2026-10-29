package org.json;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/** ORACLE SHIM of org.json.JSONObject backed by Gson (only the members InterfazContable uses). Throws on non-JSON input like org.json. */
public class JSONObject {
    private final JsonObject o;

    public JSONObject(String s) {
        JsonObject parsed;
        try {
            parsed = JsonParser.parseString(s).getAsJsonObject();
        } catch (Exception e) {
            throw new RuntimeException("A JSONObject text must begin with '{'", e);
        }
        this.o = parsed;
    }

    private JSONObject(JsonObject o) { this.o = o; }

    public JSONObject getJSONObject(String k) {
        if (!o.has(k) || !o.get(k).isJsonObject()) throw new RuntimeException("JSONObject[\"" + k + "\"] not found.");
        return new JSONObject(o.getAsJsonObject(k));
    }

    public int getInt(String k) {
        if (!o.has(k)) throw new RuntimeException("JSONObject[\"" + k + "\"] not found.");
        return o.get(k).getAsInt();
    }

    public String getString(String k) {
        if (!o.has(k) || !o.get(k).isJsonPrimitive()) throw new RuntimeException("JSONObject[\"" + k + "\"] not found.");
        return o.get(k).getAsString();
    }
}
