package cl.bcs.reports.report;

import com.google.gson.JsonArray;

import java.util.Map;

public class JsonResultSet {
    private Map<String, String> schema;
    private JsonArray data;

    public JsonResultSet(Map<String, String> schema, JsonArray data) {
        this.schema = schema;
        this.data = data;
    }

    public Map<String, String> getSchema() {
        return this.schema;
    }

    public void setSchema(Map<String, String> schema) {
        this.schema = schema;
    }

    public JsonArray getData() {
        return this.data;
    }

    public void setData(JsonArray data) {
        this.data = data;
    }
}
