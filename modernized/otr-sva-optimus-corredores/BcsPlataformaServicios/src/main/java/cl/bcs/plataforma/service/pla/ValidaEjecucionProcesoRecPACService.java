package cl.bcs.plataforma.service.pla;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class ValidaEjecucionProcesoRecPACService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(ValidaEjecucionProcesoRecPACService.class);
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_SELECT;
    private final String SQL_UPDATE;

    public ValidaEjecucionProcesoRecPACService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.SQL_SELECT = loadSql("cl/bcs/plataforma/pla/sql/ValidaEjecucionProcesoRecPAC_SelectRECPAC.sql");
        this.SQL_UPDATE = loadSql("cl/bcs/plataforma/pla/sql/ValidaEjecucionProcesoRecPAC_UpdateFlag.sql");
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.pla.ValidaEjecucionProcesoRecPAC"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("INICIO::ValidaEjecucionProcesoRecPAC");
        Entrada in = GSON.fromJson(params, Entrada.class);
        Salida out = new Salida();

        try {
            ValorFlag valorFlag = new ValorFlag();
            valorFlag.Valor = in.p_ValorNuevo;

            if ("N".equalsIgnoreCase(in.p_ValorNuevo)) {
                jdbc.update(SQL_UPDATE, new BeanPropertySqlParameterSource(valorFlag));
            } else {
                List<ValorFlag> actual = jdbc.query(SQL_SELECT, new MapSqlParameterSource(),
                        BeanPropertyRowMapper.newInstance(ValorFlag.class));

                if (!actual.isEmpty() && "S".equalsIgnoreCase(actual.get(0).Valor)) {
                    Msg err = new Msg();
                    err.tipo = "E";
                    err.codigo = "INTENTE_MAS_TARDE_PROCESO_PAC_REC_EJECUTANDOSE";
                    err.descripcion = "Proceso REC/PAC ya en ejecución";
                    out.P_OUT_MENSAJES.add(err);
                    out.P_OUT_ERRORESCONTADOR = 1L;
                    return JsonParser.parseString(GSON.toJson(out)).getAsJsonObject();
                }
                jdbc.update(SQL_UPDATE, new BeanPropertySqlParameterSource(valorFlag));
            }

            Msg ok = new Msg();
            ok.id = 1L;
            ok.tipo = "I";
            ok.codigo = "OK";
            ok.descripcion = "Flag Actualizado";
            out.P_OUT_MENSAJES.add(ok);

        } catch (Exception e) {
            log.error("Error en ValidaEjecucionProcesoRecPAC: {}", e.getMessage(), e);
            out.P_OUT_ERRORESCONTADOR = 1L;
        }

        return JsonParser.parseString(GSON.toJson(out)).getAsJsonObject();
    }

    public static class Entrada {
        public Long p_IdEmpresa;
        public String p_ValorNuevo;
    }

    public static class ValorFlag {
        public String Valor;
    }

    public static class Salida {
        public List<Msg> P_OUT_MENSAJES = new ArrayList<>();
        public Long P_OUT_ERRORESCONTADOR = 0L;
    }

    public static class Msg {
        public Long id;
        public String tipo;
        public String codigo;
        public String descripcion;
    }

    private static String loadSql(String path) {
        try (InputStream is = ValidaEjecucionProcesoRecPACService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
