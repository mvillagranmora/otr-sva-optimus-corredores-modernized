package cl.bcs.plataforma.service.pla.utils;

import cl.bcs.plataforma.service.JavaCallService;
import cl.bcs.plataforma.service.utils.ConfiguracionSistemaUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ObtenerFeriadoService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(ObtenerFeriadoService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final ConfiguracionSistemaUtil configSistema;
    private final String SQL_CONSULTAR_FERIADOS;

    public ObtenerFeriadoService(NamedParameterJdbcTemplate jdbc, ConfiguracionSistemaUtil configSistema) {
        this.jdbc = jdbc;
        this.configSistema = configSistema;
        this.SQL_CONSULTAR_FERIADOS = loadSql("cl/bcs/plataforma/pla/sql/ConsultarFeriados.sql");
    }

    @Override public String fqn() { return "cl.bcs.plataforma.pla.utils.ObtenerFeriado"; }

    @Override
    public JsonObject execute(JsonObject params) {
        Long idEmpresa = params.has("IdEmpresa") ? params.get("IdEmpresa").getAsLong() : 1L;

        String paisSistemaStr = configSistema.getParametroString(idEmpresa, "PLATAFORMA", "PLA.PaisSistema");
        Long idPais = Long.parseLong(paisSistemaStr);

        List<Map<String, Object>> feriados = jdbc.queryForList(SQL_CONSULTAR_FERIADOS,
            new MapSqlParameterSource().addValue("IdPais", idPais));

        JsonObject result = new JsonObject();
        result.addProperty("P_OUT_ERRORESCONTADOR", 0);

        // Process single fechaConsulta if provided
        if (params.has("fechaConsulta")) {
            String fechaConsulta = params.get("fechaConsulta").getAsString();
            boolean esHabil = esDiaHabil(fechaConsulta, feriados);
            result.addProperty("habil", esHabil);
        }

        return result;
    }

    private boolean esDiaHabil(String fecha, List<Map<String, Object>> feriados) {
        LocalDate d = LocalDate.parse(fecha.substring(0, 10));
        int dow = d.getDayOfWeek().getValue(); // 1=Mon..7=Sun
        if (dow == 6 || dow == 7) return false; // weekend
        Date sqlDate = Date.valueOf(d);
        for (Map<String, Object> f : feriados) {
            if ("FERIADO".equals(f.get("Tipo")) && sqlDate.equals(f.get("Fecha"))) return false;
            if ("INHABIL".equals(f.get("Tipo"))) {
                Integer dia = f.get("Dia") instanceof Number ? ((Number) f.get("Dia")).intValue() : null;
                if (dia != null && dia == (dow == 7 ? 0 : dow)) return false;
            }
        }
        return true;
    }

    private String loadSql(String path) {
        try (var is = getClass().getClassLoader().getResourceAsStream(path)) {
            return new String(is.readAllBytes());
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}
