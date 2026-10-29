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

@Service
public class CambiarFechaSistemaService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(CambiarFechaSistemaService.class);
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private static final String SQL_UPDATE_FECHA =
        "UPDATE \"BSCL.Plataforma.BD::PLA.Configuracion\" SET \"Valor\" = :p_FechaSistema " +
        "WHERE \"IdEmpresa\" = :p_IdEmpresa AND \"CodigoParametro\" = 'PLA.FechaSistema'";

    private static final String SQL_UPDATE_FECHA_ANTERIOR =
        "UPDATE \"BSCL.Plataforma.BD::PLA.Configuracion\" SET \"Valor\" = :p_FechaSistemaActual " +
        "WHERE \"IdEmpresa\" = :p_IdEmpresa AND \"CodigoParametro\" = 'PLA.FechaSistemaAnterior'";

    private final NamedParameterJdbcTemplate jdbc;

    public CambiarFechaSistemaService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.pla.CambiarFechaSistema"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("INICIO::CambiarFechaSistema");
        Entrada in = GSON.fromJson(params, Entrada.class);
        Salida out = new Salida();

        try {
            MapSqlParameterSource src = new MapSqlParameterSource()
                .addValue("p_IdEmpresa", in.p_IdEmpresa)
                .addValue("p_FechaSistema", in.p_FechaSistema)
                .addValue("p_FechaSistemaActual", in.p_FechaSistemaActual);

            jdbc.update(SQL_UPDATE_FECHA, src);
            jdbc.update(SQL_UPDATE_FECHA_ANTERIOR, src);
            out.P_OUT_ERRORESCONTADOR = 0L;
        } catch (Exception e) {
            log.error("Error en CambiarFechaSistema: {}", e.getMessage(), e);
            out.P_OUT_ERRORESCONTADOR = 1L;
        }

        return JsonParser.parseString(GSON.toJson(out)).getAsJsonObject();
    }

    public static class Entrada {
        public Long p_IdEmpresa;
        public String p_FechaSistema;
        public String p_FechaSistemaActual;
    }

    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 1L;
    }
}
