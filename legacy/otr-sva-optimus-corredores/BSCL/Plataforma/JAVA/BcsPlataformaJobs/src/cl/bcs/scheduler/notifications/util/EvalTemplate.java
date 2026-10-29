package cl.bcs.scheduler.notifications.util;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;

import cl.bcs.scheduler.notifications.pojo.KeyValue;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.Version;

public class EvalTemplate {

	private static Configuration newConfiguration() {
		// Create your Configuration instance, and specify if up to what
		// FreeMarker
		// version (here 2.3.24) do you want to apply the fixes that are not
		// 100%
		// backward-compatible. See the Configuration JavaDoc for details.
		Configuration cfg = new Configuration(new Version("2.3.23"));
		// Sets how errors will appear.
		// During web page *development*
		// TemplateExceptionHandler.HTML_DEBUG_HANDLER is better.
		cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		// Don't log exceptions inside FreeMarker that it will thrown at you
		// anyway:
		cfg.setLogTemplateExceptions(true);
		return cfg;
	}

	private static Configuration configuration;
	private static Gson gson;
	private static Logger logger;

	public EvalTemplate() {
		logger = Logger.getLogger(this.getClass().getName());
		gson = new Gson();
		configuration = newConfiguration();
	}

	public static String eval(String context, String templateString) {
		if (context == null || templateString == null) {
			return "Error context o Template en NULL";
		}
		try {
			Template template;
			final StringWriter out = new StringWriter();
			try {
				template = parseTemplate(
						StringEscapeUtils.unescapeJava(templateString.replace("!}", "}")),
						configuration);
				Map<String, String> model = gson.fromJson(context, HashMap.class);
				template.process(model, out);
			} catch (freemarker.core.InvalidReferenceException | IOException e) {
				// TODO Auto-generated catch block

				return ("\"Campo de Template no coincide(n)\"" + e.getMessage());
			}

			String outputtest = gson.toJson(out.toString());
			if (outputtest.startsWith("\"") && outputtest.endsWith("\"")) {

				outputtest = outputtest.substring(1, outputtest.length() - 1);

			}

			return StringEscapeUtils.unescapeJava(outputtest);
		} catch (final TemplateException e) {
			logger.log(Level.SEVERE, "error mientras se procesa el template", e);
			return "error mientras se procesa el template";
		}
	}

	private <T> T defaultIfNull(T... candidates) {
		for (T candidate : candidates) {
			return candidate;
		}
		return null;
	}

	private static Template parseTemplate(final String rawTemplate, final Configuration configuration)
			throws IOException {
		// this fixes list
		final String template = rawTemplate.replaceAll("&lt;#else&gt;", "<#else>").replaceAll("&lt;#sep&gt;", "<#sep>")
				.replaceAll("&lt;#/sep&gt;", "</#sep>").replaceAll("&lt;#list (.*?)&gt;", "<#list $1>")
				.replaceAll("&lt;#/list&gt;", "</#list>");

		return new Template("name", new StringReader(template), configuration);
	}

	JSONObject reqObj = new JSONObject();

	public JSONObject Agrupa(final List<KeyValue> grouped, Map<String, String> tags, String eventCode) throws JSONException, Exception {
		int i = 0;
		String codigoaux = "";
		String key = "";
		JSONObject obj = new JSONObject();
		JSONArray req = new JSONArray();
		for (KeyValue keyValue : grouped) {
			if (keyValue.codigo != null && keyValue.codigo.equals(eventCode)) {

				if (i == 0) {
					codigoaux = keyValue.codigo;
					key = keyValue.key;
					obj.put(keyValue.key, keyValue.value);
					obj.put("IdPersonaDestinatario", keyValue.idPersonaDestinatario);
					req.put(obj);
					
				}
				if (i > 0) {

					if (key.equals(keyValue.key)) {
						obj.put(keyValue.key, keyValue.value);
						obj.put("IdPersonaDestinatario", keyValue.idPersonaDestinatario);
						req.put(obj);
					} else {
						obj.put(keyValue.key, keyValue.value);
						obj.put("IdPersonaDestinatario", keyValue.idPersonaDestinatario);
						req.put(obj);
					}
				}
				obj = new JSONObject();
				i++;
			}
		}
		reqObj.put(codigoaux, req);
		for (Map.Entry<String, String> entry : tags.entrySet()) {
			obj.put(entry.getKey(), entry.getValue());
		}
		this.reqObj.put("TagGenerales", obj);

		JSONObject Objfather = new JSONObject();
		Objfather.put("data", reqObj);
		return Objfather;
	}

	public JSONObject NoAgrupa(final List<KeyValue> data, Map<String, String> tags, long eventId) throws JSONException, Exception {

		int i = 0;
		String codigoaux = "";
		JSONObject obj = new JSONObject();

		for (KeyValue keyValue : data) {
			if (keyValue.idEvento == eventId) {
				if (i == 0) {
					codigoaux = keyValue.codigo;
					obj.put(keyValue.key, keyValue.value);
					obj.put("IdPersonaDestinatario", keyValue.idPersonaDestinatario);
				}
				if (i > 0) {
					if (codigoaux.equals(keyValue.codigo)) {
						obj.put(keyValue.key, keyValue.value);
						obj.put("IdPersonaDestinatario", keyValue.idPersonaDestinatario);
					} else {
						this.reqObj.put(codigoaux, obj);
						obj = new JSONObject();
						obj.put(keyValue.key, keyValue.value);
						obj.put("IdPersonaDestinatario", keyValue.idPersonaDestinatario);
						codigoaux = String.valueOf(keyValue.idEvento);
					}
				}
				i++;
			}
		}

		this.reqObj.put(codigoaux, obj);
		obj = new JSONObject();
		for (Map.Entry<String, String> entry : tags.entrySet()) {
			obj.put(entry.getKey(), entry.getValue());
		}
		this.reqObj.put("TagGenerales", obj);

		JSONObject Objfather = new JSONObject();
		Objfather.put("data", reqObj);
		return Objfather;
	}
}
