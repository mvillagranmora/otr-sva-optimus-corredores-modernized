package cl.bcs.contabilidad.service;

import cl.bcs.contabilidad.infrastructure.persistence.SqlResources;
import cl.bcs.contabilidad.infrastructure.persistence.SqlRow;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lists every normative concept with its conditions grouped by data type (WP-C, CONT-C-80..88): legacy
 * {@code cl.bcs.contabilidad.ConsultarCondicionesConceptosNormativos} (ConsultarCondicionesConceptosNormativos.java:59-121).
 *
 * <p>The query has no filter and the input (p_IdEmpresa, p_IdUsuario, p_IdModulo) is not used. Conditions of a concept
 * are grouped by IdTipoDatoEvento, case-insensitively, keeping the first spelling. Any failure, including the legacy
 * NullPointerException on a null IdTipoDatoEvento (:95), is reported as P_OUT_ERRORESCONTADOR 1 plus its message with an
 * empty list. Output through a plain Gson: null fields are omitted.
 */
@Service
public class ConsultarCondicionesConceptosNormativosService {

    private static final String SQL_SELECT_CONDICIONES =
            SqlResources.load("cl/bcs/contabilidad/sql/ConsultarCondicionesConceptosNormativos_SelectConceptosConfiguraciones.sql");

    private final NamedParameterJdbcTemplate jdbc;

    public ConsultarCondicionesConceptosNormativosService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ------------------------------------------------------------------ contract

    record Valor(@SerializedName("ValorVarchar") String valorVarchar,
                 @SerializedName("OperadorFuncion") String operadorFuncion) {}

    /**
     * Legacy {@code CondicionesConceptoNormativo}. Its IdCondicionConceptoNormativo was never assigned (:100-104), so it
     * was always null and omitted from the JSON; it is not modelled.
     */
    record Condicion(@SerializedName("IdConceptoNormativo") Long idConceptoNormativo,
                     @SerializedName("IdTipoDatoEvento") String idTipoDatoEvento,
                     @SerializedName("valores") List<Valor> valores) {}

    record ConceptoNormativo(@SerializedName("IdConceptoNormativo") Long idConceptoNormativo,
                             @SerializedName("ConceptoNormativo") String conceptoNormativo,
                             @SerializedName("condiciones") List<Condicion> condiciones) {}

    record Salida(@SerializedName("P_OUT_MENSAJE") String mensaje,
                  @SerializedName("P_OUT_ERRORESCONTADOR") Long erroresContador,
                  @SerializedName("conceptosNormativos") List<ConceptoNormativo> conceptosNormativos) {}

    private record Fila(Long idConceptoNormativo, String conceptoNormativo, String idTipoDatoEvento, String valorVarchar,
                        String operadorFuncion, Long idCondicionConceptoNormativo) {}

    // ------------------------------------------------------------------ flow

    public JsonObject execute(JsonObject params) {
        return JavaCallJson.write(JavaCallJson.plain(), consultar());
    }

    Salida consultar() {
        try {
            return new Salida(null, 0L, agrupar(jdbc.query(SQL_SELECT_CONDICIONES, new MapSqlParameterSource(),
                    SqlRow.mapper(r -> new Fila(r.lng("IdConceptoNormativo"), r.str("ConceptoNormativo"),
                            r.str("IdTipoDatoEvento"), r.str("ValorVarchar"), r.str("OperadorFuncion"),
                            r.lng("IdCondicionConceptoNormativo"))))));
        } catch (RuntimeException e) {
            return new Salida(e.getMessage(), 1L, List.of());
        }
    }

    /** Legacy :79-113. The concepts come out in HashMap order, as legacy {@code mapaConceptos.values()} did. */
    private static List<ConceptoNormativo> agrupar(List<Fila> filas) {
        Map<Long, ConceptoNormativo> conceptos = new HashMap<>();
        for (Fila fila : filas) {
            ConceptoNormativo concepto = conceptos.computeIfAbsent(fila.idConceptoNormativo(),
                    id -> new ConceptoNormativo(id, fila.conceptoNormativo(), new ArrayList<>()));
            if (fila.idCondicionConceptoNormativo() == null) continue; // LEFT JOIN without conditions

            Condicion grupo = concepto.condiciones().stream()
                    .filter(c -> c.idTipoDatoEvento().equalsIgnoreCase(fila.idTipoDatoEvento()))
                    .findFirst()
                    .orElseGet(() -> {
                        Condicion nueva = new Condicion(fila.idConceptoNormativo(), fila.idTipoDatoEvento(), new ArrayList<>());
                        concepto.condiciones().add(nueva);
                        return nueva;
                    });
            grupo.valores().add(new Valor(fila.valorVarchar(), fila.operadorFuncion()));
        }
        return List.copyOf(conceptos.values());
    }
}
