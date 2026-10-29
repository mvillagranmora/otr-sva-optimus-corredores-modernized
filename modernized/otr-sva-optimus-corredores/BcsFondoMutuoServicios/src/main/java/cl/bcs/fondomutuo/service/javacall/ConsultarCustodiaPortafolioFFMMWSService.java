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
public class ConsultarCustodiaPortafolioFFMMWSService {

    private static final Logger log = LoggerFactory.getLogger(ConsultarCustodiaPortafolioFFMMWSService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlMatrizAgrupadora;
    private final String sqlMaestroInstrumentos;
    private final String sqlClientePortafolio;
    private final String sqlEmisor;
    private final String sqlValorCuota;

    public ConsultarCustodiaPortafolioFFMMWSService(NamedParameterJdbcTemplate jdbc) throws IOException {
        this.jdbc = jdbc;
        this.sqlMatrizAgrupadora = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/WSConsultarCustodiaPortafolioFFMM_SelectMatrizAgrupadora.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlMaestroInstrumentos = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/WSConsultarCustodiaPortafolioFFMM_SelectMaestroInstrumentos.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlClientePortafolio = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/WSConsultarCustodiaPortafolioFFMM_SelectClientePortafolio.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlEmisor = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/WSConsultarCustodiaPortafolioFFMM_SelectEmisor.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlValorCuota = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/WSConsultarCustodiaPortafolioFFMM_SelectValorCuota.sql").getInputStream(), StandardCharsets.UTF_8);
    }

    public JsonObject execute(JsonObject params) {
        log.info("ConsultarCustodiaPortafolioFFMMWS");

        long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : 0L;
        long idCliente = params.has("p_IdCliente") ? params.get("p_IdCliente").getAsLong() : 0L;
        long idPortafolio = params.has("p_IdPortafolio") ? params.get("p_IdPortafolio").getAsLong() : 0L;
        long idInstrumento = params.has("p_IdInstrumento") ? params.get("p_IdInstrumento").getAsLong() : 0L;
        String docIdentidad = params.has("p_DocumentoIdentidad") ? params.get("p_DocumentoIdentidad").getAsString() : "";
        String docIdentidadAdm = params.has("p_DocumentoIdentidadAdministradora") ? params.get("p_DocumentoIdentidadAdministradora").getAsString() : "";

        MapSqlParameterSource psBase = new MapSqlParameterSource()
            .addValue("IdEmpresa", idEmpresa);

        List<Map<String, Object>> maestro = jdbc.queryForList(sqlMaestroInstrumentos, psBase);
        List<Map<String, Object>> matriz = jdbc.queryForList(sqlMatrizAgrupadora, psBase);

        List<Map<String, Object>> clientePortafolio = List.of();
        if (!docIdentidad.isEmpty()) {
            clientePortafolio = jdbc.queryForList(sqlClientePortafolio,
                new MapSqlParameterSource().addValue("DocumentoIdentidad", docIdentidad));
        }

        List<Map<String, Object>> emisor = List.of();
        if (!docIdentidadAdm.isEmpty()) {
            emisor = jdbc.queryForList(sqlEmisor,
                new MapSqlParameterSource().addValue("DocumentoIdentidad", docIdentidadAdm));
        }

        MapSqlParameterSource psValor = new MapSqlParameterSource()
            .addValue("IdEmpresa", idEmpresa)
            .addValue("IdCliente", idCliente)
            .addValue("IdPortafolio", idPortafolio)
            .addValue("IdInstrumento", idInstrumento);
        List<Map<String, Object>> valorCuota = jdbc.queryForList(sqlValorCuota, psValor);

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        JsonArray arrMaestro = new JsonArray();
        for (Map<String, Object> row : maestro) arrMaestro.add(GSON.toJsonTree(row));
        out.add("P_OUT_MAESTRO_INSTRUMENTOS", arrMaestro);
        JsonArray arrMatriz = new JsonArray();
        for (Map<String, Object> row : matriz) arrMatriz.add(GSON.toJsonTree(row));
        out.add("P_OUT_MATRIZ_AGRUPADORA", arrMatriz);
        JsonArray arrCliente = new JsonArray();
        for (Map<String, Object> row : clientePortafolio) arrCliente.add(GSON.toJsonTree(row));
        out.add("P_OUT_CLIENTE_PORTAFOLIO", arrCliente);
        JsonArray arrEmisor = new JsonArray();
        for (Map<String, Object> row : emisor) arrEmisor.add(GSON.toJsonTree(row));
        out.add("P_OUT_EMISOR", arrEmisor);
        JsonArray arrValor = new JsonArray();
        for (Map<String, Object> row : valorCuota) arrValor.add(GSON.toJsonTree(row));
        out.add("P_OUT_VALOR_CUOTA", arrValor);
        return out;
    }
}
