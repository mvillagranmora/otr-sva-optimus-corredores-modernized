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
public class CrearAporteCSCService {

    private static final Logger log = LoggerFactory.getLogger(CrearAporteCSCService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlSelectMonedaFondo;
    private final String sqlSelectPortafolio;
    private final String sqlSelectDocumentoIdentidad;

    public CrearAporteCSCService(NamedParameterJdbcTemplate jdbc) throws IOException {
        this.jdbc = jdbc;
        this.sqlSelectMonedaFondo = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/CrearAporteCSC_SelectMonedaFondo.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectPortafolio = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectPortafolio.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectDocumentoIdentidad = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/CrearAporteCSC_SelectDocumentoIdentidadSegunIdCliente.sql").getInputStream(), StandardCharsets.UTF_8);
    }

    public JsonObject execute(JsonObject params) {
        log.info("CrearAporteCSC");
        long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : 0L;
        String idSerieFondo = params.has("p_IdSerieFondo") ? params.get("p_IdSerieFondo").getAsString() : "";
        long idPortafolio = params.has("p_IdPortafolio") ? params.get("p_IdPortafolio").getAsLong() : 0L;
        long idCliente = params.has("p_IdCliente") ? params.get("p_IdCliente").getAsLong() : 0L;

        MapSqlParameterSource ps = new MapSqlParameterSource()
            .addValue("IdEmpresa", idEmpresa)
            .addValue("IdSerieFondo", idSerieFondo)
            .addValue("IdPortafolio", idPortafolio)
            .addValue("IdCliente", idCliente);

        List<Map<String, Object>> monedaFondo = jdbc.queryForList(sqlSelectMonedaFondo, ps);
        List<Map<String, Object>> portafolio = jdbc.queryForList(sqlSelectPortafolio, ps);
        List<Map<String, Object>> documentoIdentidad = jdbc.queryForList(sqlSelectDocumentoIdentidad, ps);

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        JsonArray arrMoneda = new JsonArray();
        for (Map<String, Object> row : monedaFondo) arrMoneda.add(GSON.toJsonTree(row));
        out.add("P_OUT_MONEDA_FONDO", arrMoneda);
        JsonArray arrPort = new JsonArray();
        for (Map<String, Object> row : portafolio) arrPort.add(GSON.toJsonTree(row));
        out.add("P_OUT_PORTAFOLIO", arrPort);
        JsonArray arrDoc = new JsonArray();
        for (Map<String, Object> row : documentoIdentidad) arrDoc.add(GSON.toJsonTree(row));
        out.add("P_OUT_DOCUMENTO_IDENTIDAD", arrDoc);
        return out;
    }
}
