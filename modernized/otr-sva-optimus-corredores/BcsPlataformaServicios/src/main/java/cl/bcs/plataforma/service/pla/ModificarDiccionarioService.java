package cl.bcs.plataforma.service.pla;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ModificarDiccionarioService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(ModificarDiccionarioService.class);
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_SELECT;
    private final String SQL_UPDATE;

    public ModificarDiccionarioService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.SQL_SELECT = loadSql("cl/bcs/plataforma/pla/sql/ConsultaDiccionario_Select.sql");
        this.SQL_UPDATE = loadSql("cl/bcs/plataforma/pla/sql/ModificarDiccionario_Update.sql");
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.pla.ModificarDiccionario"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("INICIO::ModificarDiccionario");
        Entrada in = GSON.fromJson(params, Entrada.class);
        Salida out = new Salida();

        try {
            List<String> codigos = in.p_Codigos.stream().map(c -> c.CodDiccionario).toList();
            List<Map<String, Object>> existentes = jdbc.queryForList(SQL_SELECT,
                    new MapSqlParameterSource("ListaCodDiccionario", codigos));

            boolean existe = false;
            List<EntradaDiccionario> aActualizar = new ArrayList<>();

            for (EntradaDiccionario dicIn : in.p_Codigos) {
                boolean encontrado = existentes.stream().anyMatch(row ->
                        dicIn.CodDiccionario.equals(row.get("CodDiccionario")) &&
                        dicIn.CodLenguaje.equals(row.get("CodLenguaje")));

                if (encontrado) {
                    existe = true;
                    SalidaDiccionario msg = new SalidaDiccionario();
                    msg.CodDiccionario = dicIn.CodDiccionario;
                    msg.Mensaje = "NUEVO_MODIFICA_DICCIONARIO";
                    out.p_Respuesta.add(msg);
                    aActualizar.add(dicIn);
                }
            }

            if (!existe) {
                out.P_OUT_ERRORESCONTADOR = 1L;
                Error err = new Error();
                err.Codigo = "ERROR_MODIFICA_DICCIONARIO";
                err.Descripcion = "ERROR_MODIFICA_DICCIONARIO";
                err.RequiereAutorizacion = "N";
                out.P_OUT_ERRORESDETALLE.add(err);
            } else {
                SqlParameterSource[] batch = aActualizar.stream()
                        .map(BeanPropertySqlParameterSource::new)
                        .toArray(SqlParameterSource[]::new);
                jdbc.batchUpdate(SQL_UPDATE, batch);
            }

        } catch (Exception e) {
            log.error("Error en ModificarDiccionario: {}", e.getMessage(), e);
            out.P_OUT_ERRORESCONTADOR = 1L;
        }

        return JsonParser.parseString(GSON.toJson(out)).getAsJsonObject();
    }

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_SimularOperacion;
        public List<EntradaDiccionario> p_Codigos = new ArrayList<>();
    }

    public static class EntradaDiccionario {
        public String CodDiccionario;
        public String CodLenguaje;
        public String Descripcion;
        public String Abreviado;
    }

    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public Long P_OUT_ADVERTENCIASCONTADOR = 0L;
        public List<Object> P_OUT_ADVERTENCIADETALLE = new ArrayList<>();
        public List<Error> P_OUT_ERRORESDETALLE = new ArrayList<>();
        public List<SalidaDiccionario> p_Respuesta = new ArrayList<>();
    }

    public static class SalidaDiccionario {
        public String CodDiccionario;
        public String Mensaje;
    }

    public static class Error {
        public String Codigo;
        public String RequiereAutorizacion;
        public String Descripcion;
    }

    private static String loadSql(String path) {
        try (InputStream is = ModificarDiccionarioService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
