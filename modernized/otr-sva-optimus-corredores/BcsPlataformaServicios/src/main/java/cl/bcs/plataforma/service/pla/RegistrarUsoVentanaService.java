package cl.bcs.plataforma.service.pla;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class RegistrarUsoVentanaService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(RegistrarUsoVentanaService.class);
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_INSERT;

    private static final java.util.Set<String> VENTANAS_EXCLUIDAS = java.util.Set.of(
        "VentanaMensajeResultado", "VentanaResultado", "NO_TITLE",
        "Win_VentanaMensajeResultado", "VentanaAlerta"
    );

    public RegistrarUsoVentanaService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.SQL_INSERT = loadSql("cl/bcs/plataforma/pla/sql/RegistrarUsoVentana_InsertUsoVentana.sql");
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.pla.RegistrarUsoVentana"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("INICIO::RegistrarUsoVentana");
        EntradaRegistrarUsoVentana in = GSON.fromJson(params, EntradaRegistrarUsoVentana.class);
        SalidaRegistrarUsoVentana out = new SalidaRegistrarUsoVentana();

        try {
            if (in.p_IdVentana != null && VENTANAS_EXCLUIDAS.contains(in.p_IdVentana)) {
                log.debug("Ventana excluida, omitida: {}", in.p_IdVentana);
                return JsonParser.parseString(GSON.toJson(out)).getAsJsonObject();
            }

            if ("AGE".equals(in.p_IdTipoCanalIngreso) || "CLI".equals(in.p_IdTipoCanalIngreso)) {
                String adicional = in.p_Adicional1 != null && !in.p_Adicional1.isEmpty()
                    ? in.p_Adicional1.substring(0, 1).toUpperCase() + in.p_Adicional1.substring(1).toLowerCase()
                    : in.p_Adicional1;

                jdbc.update(SQL_INSERT, new MapSqlParameterSource()
                    .addValue("p_IdEmpresa", in.p_IdEmpresa)
                    .addValue("p_IdUsuario", in.p_IdUsuario)
                    .addValue("p_IdTipoCanalIngreso", in.p_IdTipoCanalIngreso)
                    .addValue("p_Adicional1", adicional)
                    .addValue("p_IdVentana", in.p_IdVentana)
                    .addValue("p_Ventana", in.p_Ventana));
            }
        } catch (Exception e) {
            log.error("Error en RegistrarUsoVentana: {}", e.getMessage(), e);
            out.P_OUT_MENSAJES.add(new MensajeSalidaLocal(0L, "E", "UI_ERROR", e.getMessage()));
            out.P_OUT_ERRORESCONTADOR = 1L;
        }

        return JsonParser.parseString(GSON.toJson(out)).getAsJsonObject();
    }

    public static class EntradaRegistrarUsoVentana {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdTipoCanalIngreso;
        public String p_Adicional1;
        public String p_IdVentana;
        public String p_Ventana;
    }

    public static class SalidaRegistrarUsoVentana {
        public List<MensajeSalidaLocal> P_OUT_MENSAJES = new ArrayList<>();
        public Long P_OUT_ERRORESCONTADOR = 0L;
    }

    public static class MensajeSalidaLocal {
        public Long idMensaje;
        public String tipoMensaje;
        public String codigo;
        public String descripcion;
        public MensajeSalidaLocal(Long id, String tipo, String cod, String desc) {
            this.idMensaje = id; this.tipoMensaje = tipo; this.codigo = cod; this.descripcion = desc;
        }
    }

    private static String loadSql(String path) {
        try (InputStream is = RegistrarUsoVentanaService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
