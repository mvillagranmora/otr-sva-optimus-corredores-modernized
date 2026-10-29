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
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
public class GenerarCertificado31MasivoService {

    private static final Logger log = LoggerFactory.getLogger(GenerarCertificado31MasivoService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlSelectLogoCorredora;
    private final String sqlSelectRaizArchivo;
    private final String sqlSelectImagenFirma;
    private final String sqlSelectReporte;

    public GenerarCertificado31MasivoService(NamedParameterJdbcTemplate jdbc) throws IOException {
        this.jdbc = jdbc;
        this.sqlSelectLogoCorredora = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/GenerarInformeRentabilidadFFMMExtranjeroMasivo_SelectLogoCorredora.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectRaizArchivo = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/GenerarInformeRentabilidadFFMMExtranjeroMasivo_SelectRaizArchivo.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectImagenFirma = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/GenerarCertificado31Masivo_SelectImagenFirma.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectReporte = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/GenerarCertificado31Masivo_SelectReporte.sql").getInputStream(), StandardCharsets.UTF_8);
    }

    public JsonObject execute(JsonObject params) {
        log.info("GenerarCertificado31Masivo");
        long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : 0L;
        String agno = params.has("p_Agno") ? params.get("p_Agno").getAsString() : "";

        MapSqlParameterSource ps = new MapSqlParameterSource()
            .addValue("IdEmpresa", idEmpresa)
            .addValue("Agno", agno);

        List<Map<String, Object>> logo = jdbc.queryForList(sqlSelectLogoCorredora, ps);
        List<Map<String, Object>> raiz = jdbc.queryForList(sqlSelectRaizArchivo, ps);
        List<Map<String, Object>> firma = jdbc.queryForList(sqlSelectImagenFirma, ps);
        List<Map<String, Object>> reporte = jdbc.queryForList(sqlSelectReporte, ps);

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        JsonArray arrLogo = new JsonArray();
        for (Map<String, Object> row : logo) arrLogo.add(GSON.toJsonTree(row));
        out.add("P_OUT_LOGO_CORREDORA", arrLogo);
        JsonArray arrRaiz = new JsonArray();
        for (Map<String, Object> row : raiz) arrRaiz.add(GSON.toJsonTree(row));
        out.add("P_OUT_RAIZ_ARCHIVO", arrRaiz);
        JsonArray arrFirma = new JsonArray();
        for (Map<String, Object> row : firma) arrFirma.add(GSON.toJsonTree(row));
        out.add("P_OUT_IMAGEN_FIRMA", arrFirma);
        JsonArray arrRep = new JsonArray();
        for (Map<String, Object> row : reporte) arrRep.add(GSON.toJsonTree(row));
        out.add("P_OUT_REPORTE", arrRep);
        return out;
    }
}
