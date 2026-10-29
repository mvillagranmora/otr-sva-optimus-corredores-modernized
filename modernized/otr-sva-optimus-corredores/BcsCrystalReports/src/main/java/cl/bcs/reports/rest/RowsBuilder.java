package cl.bcs.reports.rest;

import cl.bcs.reports.report.ListMapResultSet;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Array-of-rows form of "data" (servlet lines 401-413 and 451-462): every row is typed by Gson as
 * Map&lt;String,Object&gt; (numbers become Double, nested values Maps/Lists) and the list goes to
 * ListMapResultSet.populate.
 *
 * <p>Optional extension, absent in the servlet's array form: a {@code schema} array [{field,type}] (same shape as
 * the servlet's {@code serviceData.$schema}) applies the type fixes the servlet applied only to server-resolved data
 * (getServiceData lines 755-795): "/Date(ms)/" to java.sql.Date / Timestamp text and base64 to byte[] for "blob".
 * Without {@code schema} the behaviour is exactly the servlet's. Needed because the platform now resolves the data and
 * JSON cannot carry byte[] (logos stored as BLOB).
 */
final class RowsBuilder {

    private static final Pattern FIX_DATE = Pattern.compile("(\\/Date\\()([0-9]*)(\\)\\/)");

    private RowsBuilder() {
    }

    @SuppressWarnings("unchecked")
    static List<Object> rows(JsonArray dataarr, JsonElement schemaEl) {
        Map<String, Integer> schemaTypes = schemaTypes(schemaEl);
        List<Object> nList = new ArrayList<Object>();
        for (int j = 0; j < dataarr.size(); j++) {
            Type type = new TypeToken<Map<String, Object>>() { }.getType();
            Map<String, Object> map = (Map<String, Object>) new Gson().fromJson(dataarr.get(j).getAsJsonObject(), type);
            if (schemaTypes != null) {
                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    fix(schemaTypes, entry);
                }
            }
            nList.add(map);
        }
        return nList;
    }

    static ListMapResultSet resultSet(List<Object> nList) throws Exception {
        ListMapResultSet rs = new ListMapResultSet();
        rs.populate(nList);
        return rs;
    }

    private static Map<String, Integer> schemaTypes(JsonElement el) {
        if (el == null || !el.isJsonArray()) {
            return null;
        }
        Map<String, Integer> schemaTypes = new HashMap<String, Integer>();
        for (JsonElement e : el.getAsJsonArray()) {
            JsonObject obj = e.getAsJsonObject();
            String field = obj.get("field").getAsString();
            switch (obj.get("type").getAsString()) {
                case "number": schemaTypes.put(field, Types.DECIMAL); break;
                case "date": schemaTypes.put(field, Types.DATE); break;
                case "datetime": schemaTypes.put(field, Types.TIMESTAMP); break;
                case "string": schemaTypes.put(field, Types.VARCHAR); break;
                case "blob": schemaTypes.put(field, Types.BLOB); break;
                default: schemaTypes.put(field, Types.JAVA_OBJECT); break;
            }
        }
        return schemaTypes;
    }

    private static void fix(Map<String, Integer> schemaTypes, Map.Entry<String, Object> entry) {
        if (!schemaTypes.containsKey(entry.getKey())) {
            return;
        }
        String val;
        Matcher m1;
        switch (schemaTypes.get(entry.getKey())) {
            case Types.DATE:
                val = entry.getValue().toString();
                m1 = FIX_DATE.matcher(val);
                if (m1.find()) {
                    entry.setValue(new java.sql.Date(Long.valueOf(m1.group(2)).longValue()).toString());
                }
                break;
            case Types.TIMESTAMP:
                val = entry.getValue().toString();
                m1 = FIX_DATE.matcher(val);
                if (m1.find()) {
                    entry.setValue(new java.sql.Timestamp(Long.valueOf(m1.group(2)).longValue()).toString());
                }
                break;
            case Types.BLOB:
                val = entry.getValue().toString();
                entry.setValue(Base64.getDecoder().decode(val));
                break;
            default:
                break;
        }
    }
}
