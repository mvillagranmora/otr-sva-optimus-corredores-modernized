package cl.bcs.contabilidad.service;

import cl.bcs.contabilidad.infrastructure.persistence.SqlResources;
import cl.bcs.contabilidad.infrastructure.persistence.SqlRow;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Lists the regulatory concepts.
 *
 * <p>Legacy {@code cl.bcs.contabilidad.ConsultarConceptoNormativo}. Implements CONT-G-360..362. A failure is reported in
 * {@code P_OUT_MENSAJE} (exception message) with counter 1 and an empty list; nulls are omitted ({@code new Gson()}).
 */
@Service
public class ConsultarConceptoNormativoService {

    private static final Logger log = LoggerFactory.getLogger(ConsultarConceptoNormativoService.class);

    private final String SQL_SELECT_CONCEPTOS = SqlResources.load("ConsultarConceptoNormativo_SelectConceptosNormativos.sql");

    private final NamedParameterJdbcTemplate jdbc;

    public ConsultarConceptoNormativoService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    record ConceptoNormativo(@SerializedName("IdConceptoNormativo") Long idConceptoNormativo,
                             @SerializedName("ConceptoNormativo") String conceptoNormativo,
                             @SerializedName("IdEstado") String idEstado) {}

    record Salida(@SerializedName("P_OUT_MENSAJE") String mensaje,
                  @SerializedName("P_OUT_ERRORESCONTADOR") Long erroresContador,
                  @SerializedName("conceptosNormativos") List<ConceptoNormativo> conceptosNormativos) {}

    public JsonObject execute(JsonObject params) {
        Salida salida;
        try {
            salida = new Salida(null, 0L, jdbc.query(SQL_SELECT_CONCEPTOS, new MapSqlParameterSource(),
                    SqlRow.mapper(r -> new ConceptoNormativo(r.lng("IdConceptoNormativo"), r.str("ConceptoNormativo"), r.str("IdEstado")))));
        } catch (RuntimeException ex) {
            log.error("ConsultarConceptoNormativo error", ex);
            salida = new Salida(ex.getMessage(), 1L, List.of());
        }
        return JavaCallJson.write(JavaCallJson.plain(), salida);
    }
}
