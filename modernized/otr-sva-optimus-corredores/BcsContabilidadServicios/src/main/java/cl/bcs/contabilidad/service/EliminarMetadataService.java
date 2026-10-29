package cl.bcs.contabilidad.service;

import cl.bcs.contabilidad.infrastructure.persistence.SqlParams;
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
 * Deletes metadatas and their filters, only when every requested id exists.
 *
 * <p>Legacy {@code cl.bcs.contabilidad.EliminarMetadata}. Implements CONT-G-320..323. Every failure is reported as
 * UI_ERROR (legacy L71-75).
 */
@Service
public class EliminarMetadataService {

    private static final Logger log = LoggerFactory.getLogger(EliminarMetadataService.class);

    private final String SQL_SELECT = SqlResources.load("EliminarMetadata_SelectMetadataCuentaContable.sql");
    private final String SQL_DELETE_METADATA = SqlResources.load("EliminarMetadata_DeleteMetadataCuentaContable.sql");
    private final String SQL_DELETE_FILTROS = SqlResources.load("EliminarMetadata_DeleteMetadataFiltros.sql");

    private final NamedParameterJdbcTemplate jdbc;

    public EliminarMetadataService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    record Entrada(@SerializedName("p_IdEmpresa") Long idEmpresa,
                   @SerializedName("p_IdUsuario") Long idUsuario,
                   @SerializedName("p_IdModulo") String idModulo,
                   @SerializedName("p_IdMetadataCuentaContable") List<Long> ids) {}

    record Salida(@SerializedName("P_OUT_MENSAJE") String mensaje,
                  @SerializedName("P_OUT_ERRORESCONTADOR") Long erroresContador) {}

    public JsonObject execute(JsonObject params) {
        Entrada entrada = JavaCallJson.read(JavaCallJson.plain(), params, Entrada.class);
        Salida salida;
        try {
            salida = eliminar(entrada);
        } catch (RuntimeException e) {
            log.error("EliminarMetadata error", e);
            salida = new Salida("UI_ERROR", 1L);
        }
        return JavaCallJson.write(JavaCallJson.plain(), salida);
    }

    private Salida eliminar(Entrada entrada) {
        List<Long> existentes = jdbc.query(SQL_SELECT, new MapSqlParameterSource("IdMetadataCuentaContable", entrada.ids()),
                SqlRow.mapper(r -> r.lng("IdMetadataCuentaContable")));
        if (existentes.size() != entrada.ids().size()) return new Salida("UI_METADATA_NO_EXISTE", 1L);
        var porId = SqlParams.batch(existentes, id -> new MapSqlParameterSource("IdMetadataCuentaContable", id));
        jdbc.batchUpdate(SQL_DELETE_METADATA, porId);
        jdbc.batchUpdate(SQL_DELETE_FILTROS, porId);
        return new Salida("UI_EXITO", 0L);
    }
}
