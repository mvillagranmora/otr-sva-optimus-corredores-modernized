package cl.bcs.fondomutuo.service.javacall;

import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Spring @Service migration of legacy JavaCall cl.bcs.fondomutuo.ActualizarCostoUFCarga.
 */
@Service
public class ActualizarCostoUFCargaService {

    private static final Logger log = LoggerFactory.getLogger(ActualizarCostoUFCargaService.class);

    private final JdbcTemplate jdbc;
    private final String sqlUpdateAportes;
    private final String sqlUpdateRescates;

    public ActualizarCostoUFCargaService(JdbcTemplate jdbc) throws IOException {
        this.jdbc = jdbc;
        this.sqlUpdateAportes = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/ActualizarCostoUFCarga_UpdateAportes.sql").getInputStream(),
            StandardCharsets.UTF_8);
        this.sqlUpdateRescates = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/ActualizarCostoUFCarga_UpdateRescates.sql").getInputStream(),
            StandardCharsets.UTF_8);
    }

    public JsonObject execute(JsonObject params) {
        log.info("ActualizarCostoUFCarga: executing updates");
        jdbc.execute(sqlUpdateAportes);
        jdbc.execute(sqlUpdateRescates);
        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        return out;
    }
}
