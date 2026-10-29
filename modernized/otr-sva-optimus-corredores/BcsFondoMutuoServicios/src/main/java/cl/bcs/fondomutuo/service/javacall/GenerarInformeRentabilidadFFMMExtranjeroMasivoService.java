package cl.bcs.fondomutuo.service.javacall;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
public class GenerarInformeRentabilidadFFMMExtranjeroMasivoService {

    private static final Logger log = LoggerFactory.getLogger(GenerarInformeRentabilidadFFMMExtranjeroMasivoService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlSelectLogoCorredora;
    private final String sqlSelectRaizArchivo;
    private final String sqlInsertarTablaIntermedia;
    private final String sqlBorrarDataTablaIntermedia;

    public GenerarInformeRentabilidadFFMMExtranjeroMasivoService(NamedParameterJdbcTemplate jdbc) throws IOException {
        this.jdbc = jdbc;
        this.sqlSelectLogoCorredora = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/GenerarInformeRentabilidadFFMMExtranjeroMasivo_SelectLogoCorredora.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectRaizArchivo = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/GenerarInformeRentabilidadFFMMExtranjeroMasivo_SelectRaizArchivo.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlInsertarTablaIntermedia = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/GenerarInformeRentabilidadFFMMExtranjeroMasivo_InsertarTablaIntermediaCliente.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlBorrarDataTablaIntermedia = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/GenerarInformeRentabilidadFFMMExtranjeroMasivo_BorrarDataTablaIntermediaCliente.sql").getInputStream(), StandardCharsets.UTF_8);
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.info("GenerarInformeRentabilidadFFMMExtranjeroMasivo");
        long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : 0L;
        String periodo = params.has("p_Periodo") ? params.get("p_Periodo").getAsString() : "";

        MapSqlParameterSource ps = new MapSqlParameterSource()
            .addValue("IdEmpresa", idEmpresa)
            .addValue("Periodo", periodo);

        List<Map<String, Object>> logo = jdbc.queryForList(sqlSelectLogoCorredora, ps);
        List<Map<String, Object>> raiz = jdbc.queryForList(sqlSelectRaizArchivo, ps);

        jdbc.update(sqlBorrarDataTablaIntermedia, ps);
        jdbc.update(sqlInsertarTablaIntermedia, ps);

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        JsonArray arrLogo = new JsonArray();
        for (Map<String, Object> row : logo) arrLogo.add(GSON.toJsonTree(row));
        out.add("P_OUT_LOGO_CORREDORA", arrLogo);
        JsonArray arrRaiz = new JsonArray();
        for (Map<String, Object> row : raiz) arrRaiz.add(GSON.toJsonTree(row));
        out.add("P_OUT_RAIZ_ARCHIVO", arrRaiz);
        return out;
    }
}
