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
 * Lists the conditions configured for one regulatory concept.
 *
 * <p>Legacy {@code cl.bcs.contabilidad.ConsultarConfiguracionesConceptoNormativo}. Implements CONT-G-380..382. A failure
 * is reported in {@code P_OUT_MENSAJE} (exception message) with counter 1 and an empty list.
 */
@Service
public class ConsultarConfiguracionesConceptoNormativoService {

    private static final Logger log = LoggerFactory.getLogger(ConsultarConfiguracionesConceptoNormativoService.class);

    private final String SQL_SELECT_CONFIGURACIONES =
            SqlResources.load("ConsultarConfiguracionesConceptoNormativo_SelectConceptosConfiguraciones.sql");

    private final NamedParameterJdbcTemplate jdbc;

    public ConsultarConfiguracionesConceptoNormativoService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    record Entrada(@SerializedName("p_IdEmpresa") Long idEmpresa,
                   @SerializedName("p_IdUsuario") Long idUsuario,
                   @SerializedName("p_IdConceptoNormativo") Long idConceptoNormativo,
                   @SerializedName("p_IdModulo") String idModulo) {}

    record Configuracion(@SerializedName("IdCondicionConceptoNormativo") Long idCondicionConceptoNormativo,
                         @SerializedName("IdConceptoNormativo") Long idConceptoNormativo,
                         @SerializedName("IdTipoDatoEvento") String idTipoDatoEvento,
                         @SerializedName("ValorVarchar") String valorVarchar,
                         @SerializedName("OperadorFuncion") String operadorFuncion,
                         @SerializedName("IdEstado") String idEstado,
                         @SerializedName("Descripcion") String descripcion) {}

    record Salida(@SerializedName("P_OUT_MENSAJE") String mensaje,
                  @SerializedName("P_OUT_ERRORESCONTADOR") Long erroresContador,
                  @SerializedName("configuracionConcepto") List<Configuracion> configuracionConcepto) {}

    public JsonObject execute(JsonObject params) {
        Entrada entrada = JavaCallJson.read(JavaCallJson.plain(), params, Entrada.class);
        Salida salida;
        try {
            salida = new Salida(null, 0L, jdbc.query(SQL_SELECT_CONFIGURACIONES,
                    new MapSqlParameterSource("IdConceptoNormativo", entrada.idConceptoNormativo()),
                    SqlRow.mapper(r -> new Configuracion(r.lng("IdCondicionConceptoNormativo"), r.lng("IdConceptoNormativo"),
                            r.str("IdTipoDatoEvento"), r.str("ValorVarchar"), r.str("OperadorFuncion"), r.str("IdEstado"),
                            r.str("Descripcion")))));
        } catch (RuntimeException ex) {
            log.error("ConsultarConfiguracionesConceptoNormativo error", ex);
            salida = new Salida(ex.getMessage(), 1L, List.of());
        }
        return JavaCallJson.write(JavaCallJson.plain(), salida);
    }
}
