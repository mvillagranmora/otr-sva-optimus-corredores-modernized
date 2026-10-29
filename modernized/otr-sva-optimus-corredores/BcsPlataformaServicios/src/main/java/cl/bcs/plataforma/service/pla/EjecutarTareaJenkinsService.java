package cl.bcs.plataforma.service.pla;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
public class EjecutarTareaJenkinsService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(EjecutarTareaJenkinsService.class);
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();
    private static final Type PARAM_LIST = new TypeToken<List<ParametroJenkins>>(){}.getType();

    @Value("${jenkins.protocol:https}")   private String jenkinsProtocol;
    @Value("${jenkins.server:jenkins}")   private String jenkinsServer;
    @Value("${jenkins.username:}")        private String jenkinsUsername;
    @Value("${jenkins.usertoken:}")       private String jenkinsToken;

    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_TAREA;

    public EjecutarTareaJenkinsService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.SQL_TAREA = loadSql("cl/bcs/plataforma/pla/sql/EjecutarTareaJenkins_SelectTarea.sql");
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.pla.EjecutarTareaJenkins"; }

    @Override
    public JsonObject execute(JsonObject params) {
        log.debug("INICIO::EjecutarTareaJenkins");
        Salida out = new Salida();
        try {
            String tipoTarea = params.has("p_TipoTarea") ? params.get("p_TipoTarea").getAsString() : "";
            List<ParametroJenkins> parametros = params.has("p_Parametros")
                ? GSON.fromJson(params.get("p_Parametros"), PARAM_LIST) : new ArrayList<>();

            List<TareaJenkins> tareas = jdbc.query(SQL_TAREA,
                new MapSqlParameterSource("TipoTarea", tipoTarea),
                BeanPropertyRowMapper.newInstance(TareaJenkins.class));

            if (tareas.isEmpty()) {
                out.P_OUT_ERRORESCONTADOR = 1L;
                out.CodigoHttp = 404L;
                out.MensajeHttp = "Tarea no configurada";
                addMsg(out, "ERROR", "0", "No se encuentra TipoTarea");
                return toJson(out);
            }

            TareaJenkins tarea = tareas.get(0);
            StringBuilder paramStr = new StringBuilder();
            for (ParametroJenkins p : parametros) paramStr.append("&").append(p.Nombre).append("=").append(p.Valor);
            String buildPath = paramStr.length() > 0 ? "/buildWithParameters?" : "/build?";
            String strUrl = jenkinsProtocol + "://" + jenkinsServer + tarea.Ruta + buildPath + "token=" + tarea.Token + paramStr;
            log.info("Jenkins URL: {}", strUrl);

            URL url = new URL(strUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            try {
                String auth = jenkinsUsername + ":" + jenkinsToken;
                String authHeader = "Basic " + Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
                conn.setRequestProperty("Authorization", authHeader);
                conn.setConnectTimeout(10_000);
                conn.setReadTimeout(10_000);
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Content-Type", "application/json");

                out.CodigoHttp = (long) conn.getResponseCode();
                out.MensajeHttp = conn.getResponseMessage();
            } finally {
                conn.disconnect();
            }
        } catch (Exception e) {
            log.error("Error en EjecutarTareaJenkins: {}", e.getMessage(), e);
            out.P_OUT_ERRORESCONTADOR = 1L;
            out.CodigoHttp = 500L;
            out.MensajeHttp = e.getMessage();
        }
        return toJson(out);
    }

    private void addMsg(Salida out, String tipo, String codigo, String desc) {
        Mensaje m = new Mensaje(); m.tipo = tipo; m.codigo = codigo; m.descripcion = desc;
        out.P_OUT_MENSAJES.add(m);
    }

    private JsonObject toJson(Object o) {
        return JsonParser.parseString(GSON.toJson(o)).getAsJsonObject();
    }

    private String loadSql(String path) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) { throw new IllegalStateException("SQL not found: " + path, e); }
    }

    public static class ParametroJenkins { public String Nombre = ""; public String Valor = ""; }
    public static class TareaJenkins { public String Ruta; public String Token; }

    public static class Salida {
        public Long CodigoHttp = 0L; public String MensajeHttp = "";
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<Mensaje> P_OUT_MENSAJES = new ArrayList<>();
    }

    public static class Mensaje { public String tipo; public String codigo; public String descripcion; }
}
