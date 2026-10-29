package cl.bcs.portalconfirmacion.service;

import cl.bcs.portalconfirmacion.domain.KeyValue;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import freemarker.core.InvalidReferenceException;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Pure-logic service for FreeMarker template evaluation and tag grouping.
 *
 * All methods are public static — they depend on no Spring beans and are
 * testable without a Spring context.
 *
 * Modernized from legacy EvalTemplate.java:
 * - Thread-safety fixed: reqObj is now a local variable (not an instance field).
 * - org.json replaced with Gson JsonObject/JsonArray.
 * - commons-lang StringEscapeUtils replaced with commons-text.
 * - Bug fixed in agrupa(): uses codigoaux (not kv.codigo()) when emitting different group.
 */
@Service
public class EvalTemplateService {

    private static final Logger LOG = LoggerFactory.getLogger(EvalTemplateService.class);

    /** Thread-safe singleton FreeMarker configuration. */
    private static final Configuration FM_CONFIG;

    static {
        @SuppressWarnings("deprecation")
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setLogTemplateExceptions(true);
        FM_CONFIG = cfg;
    }

    private static final Gson GSON = new Gson();

    // ----------------------------------------------------------
    // Public static API
    // ----------------------------------------------------------

    /**
     * Evaluates a FreeMarker template string against a JSON context.
     *
     * @param context        JSON string used as the data model.
     * @param templateString FreeMarker template (may contain HTML-escaped directives).
     * @return Rendered output, HTML-escaped; or an error message string on failure.
     */
    public static String eval(String context, String templateString) {
        if (context == null || templateString == null) {
            return "Error context o Template en NULL";
        }
        try {
            String unescaped = StringEscapeUtils.unescapeHtml4(
                    templateString.replace("<!--", "").replace("-->", ""));
            Template template = parseTemplate(unescaped, FM_CONFIG);
            StringWriter out = new StringWriter();
            @SuppressWarnings("unchecked")
            Map<String, String> model = GSON.fromJson(context, HashMap.class);
            try {
                template.process(model, out);
            } catch (InvalidReferenceException | IOException e) {
                return "\"Campo de Template no coincide(n)\"" + e.getMessage();
            }
            String output = GSON.toJson(out.toString());
            if (output.startsWith("\"") && output.endsWith("\"")) {
                output = output.substring(1, output.length() - 1);
            }
            return StringEscapeUtils.escapeHtml4(output);
        } catch (TemplateException | IOException e) {
            LOG.error("error mientras se procesa el template", e);
            return "error mientras se procesa el template";
        }
    }

    /**
     * Groups a list of KeyValue entries by codigo into a JSON structure
     * where each codigo maps to an array of key→value objects.
     *
     * <pre>
     * {"data": {"CodigoEvento": [{"key1":"val1",...}], "TagGenerales": {...}}}
     * </pre>
     *
     * @param grouped List of KeyValue entries (assumed to be sorted by codigo, then key).
     * @param tags    General tag map (added as "TagGenerales").
     * @return JSON string.
     */
    public static String agrupa(List<KeyValue> grouped, Map<String, String> tags) {
        JsonObject reqObj = new JsonObject();

        if (!grouped.isEmpty()) {
            JsonObject obj = new JsonObject();
            JsonArray req = new JsonArray();
            String codigoaux = "";
            String key = "";

            for (int i = 0; i < grouped.size(); i++) {
                KeyValue kv = grouped.get(i);
                if (i == 0) {
                    codigoaux = kv.codigo();
                    key = kv.key();
                    obj.addProperty(kv.key(), kv.value());
                } else {
                    if (codigoaux.equals(kv.codigo())) {
                        if (key.equals(kv.key())) {
                            req.add(obj);
                            reqObj.add(kv.codigo(), req);
                            obj = new JsonObject();
                            obj.addProperty(kv.key(), kv.value());
                        } else {
                            obj.addProperty(kv.key(), kv.value());
                        }
                    } else {
                        req.add(obj);
                        reqObj.add(codigoaux, req);  // emit previous group
                        req = new JsonArray();
                        obj = new JsonObject();
                        obj.addProperty(kv.key(), kv.value());
                        codigoaux = kv.codigo();
                        key = kv.key();
                    }
                }
                if (i == grouped.size() - 1) {
                    req.add(obj);
                    reqObj.add(kv.codigo(), req);
                }
            }
        }

        JsonObject tagsObj = buildTagsObject(tags);
        reqObj.add("TagGenerales", tagsObj);

        JsonObject father = new JsonObject();
        father.add("data", reqObj);
        return father.toString();
    }

    /**
     * Builds a flat (non-grouped) JSON structure from a list of KeyValue entries,
     * where each distinct codigo maps to a single object of its key→value pairs.
     *
     * <pre>
     * {"data": {"CodigoEvento": {"key1":"val1",...}, "TagGenerales": {...}}}
     * </pre>
     *
     * @param data List of KeyValue entries (assumed to be sorted by codigo).
     * @param tags General tag map (added as "TagGenerales").
     * @return JSON string.
     */
    public static String noAgrupa(List<KeyValue> data, Map<String, String> tags) {
        JsonObject reqObj = new JsonObject();

        if (!data.isEmpty()) {
            String codigoaux = "";
            JsonObject obj = new JsonObject();

            for (int i = 0; i < data.size(); i++) {
                KeyValue kv = data.get(i);
                if (i == 0) {
                    codigoaux = kv.codigo();
                    obj.addProperty(kv.key(), kv.value());
                } else {
                    if (codigoaux.equals(kv.codigo())) {
                        obj.addProperty(kv.key(), kv.value());
                    } else {
                        reqObj.add(codigoaux, obj);
                        obj = new JsonObject();
                        obj.addProperty(kv.key(), kv.value());
                        codigoaux = kv.codigo();
                    }
                }
                if (i == data.size() - 1) {
                    reqObj.add(codigoaux, obj);
                }
            }
        }

        JsonObject tagsObj = buildTagsObject(tags);
        reqObj.add("TagGenerales", tagsObj);

        JsonObject father = new JsonObject();
        father.add("data", reqObj);
        return father.toString();
    }

    // ----------------------------------------------------------
    // Private helpers
    // ----------------------------------------------------------

    private static JsonObject buildTagsObject(Map<String, String> tags) {
        JsonObject obj = new JsonObject();
        for (Map.Entry<String, String> entry : tags.entrySet()) {
            obj.addProperty(entry.getKey(), entry.getValue());
        }
        return obj;
    }

    private static Template parseTemplate(String rawTemplate, Configuration cfg) throws IOException {
        // Restore FreeMarker directives that were HTML-escaped in the stored template
        String template = rawTemplate
                .replaceAll("&lt;#else&gt;", "<#else>")
                .replaceAll("&lt;#sep&gt;", "<#sep>")
                .replaceAll("&lt;#/sep&gt;", "</#sep>")
                .replaceAll("&lt;#list (.*?)&gt;", "<#list $1>")
                .replaceAll("&lt;#/list&gt;", "</#list>");
        return new Template("bcs-notif", new StringReader(template), cfg);
    }
}
