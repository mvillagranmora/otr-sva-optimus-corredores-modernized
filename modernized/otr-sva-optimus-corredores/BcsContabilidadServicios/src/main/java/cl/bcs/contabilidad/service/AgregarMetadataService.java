package cl.bcs.contabilidad.service;

import cl.bcs.contabilidad.domain.model.MensajeSalida;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Creates an account metadata and its filters.
 *
 * <p>Legacy {@code cl.bcs.contabilidad.AgregarMetadata}. Implements CONT-G-280..283.
 */
@Service
public class AgregarMetadataService {

    private static final Logger log = LoggerFactory.getLogger(AgregarMetadataService.class);

    /** Conditions a metadata filter may use (legacy L111). */
    static final Set<String> CONDICIONES_VALIDAS = Set.of("=", ">", "<", "<>");

    private final String SQL_SELECT_ID_METADATA = SqlResources.load("AgregarMetadata_SelectMetadataCuentaContable.sql");
    private final String SQL_INSERT_METADATA = SqlResources.load("AgregarMetadata_InsertMetadataCuentaContable.sql");
    private final String SQL_INSERT_FILTROS = SqlResources.load("AgregarMetadata_InsertMetadataFiltros.sql");

    private final NamedParameterJdbcTemplate jdbc;

    public AgregarMetadataService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    record Filtro(@SerializedName("CuentaOptimus") String cuentaOptimus,
                  @SerializedName("IdTipoDatoEvento") String idTipoDatoEvento,
                  @SerializedName("Condicion") String condicion,
                  @SerializedName("Valor") String valor) {
        MapSqlParameterSource params(Long idMetadata) {
            return new MapSqlParameterSource()
                    .addValue("IdMetadataCuentaContable", idMetadata)
                    .addValue("CuentaOptimus", cuentaOptimus)
                    .addValue("IdTipoDatoEvento", idTipoDatoEvento)
                    .addValue("Condicion", condicion)
                    .addValue("Valor", valor);
        }
    }

    record Entrada(@SerializedName("p_IdEmpresa") Long idEmpresa,
                   @SerializedName("p_IdUsuario") Long idUsuario,
                   @SerializedName("p_IdModulo") String idModulo,
                   @SerializedName("p_CodigoMetadata") String codigoMetadata,
                   @SerializedName("p_DescripcionMetadata") String descripcionMetadata,
                   @SerializedName("p_Filtros") List<Filtro> filtros) {}

    record Salida(@SerializedName("P_OUT_MENSAJE") String mensaje,
                  @SerializedName("P_OUT_ERRORESCONTADOR") Long erroresContador,
                  @SerializedName("P_OUT_MENSAJES") List<MensajeSalida> mensajes) {}

    public JsonObject execute(JsonObject params) {
        Entrada entrada = JavaCallJson.read(JavaCallJson.plain(), params, Entrada.class);
        List<MensajeSalida> mensajes = new ArrayList<>();
        Salida salida;
        try {
            insertar(entrada, mensajes);
            salida = new Salida("UI_EXITO", 0L, mensajes);
        } catch (RuntimeException e) {
            log.error("AgregarMetadata error", e);
            salida = new Salida("UI_ERROR", 1L, mensajes);
        }
        return JavaCallJson.write(JavaCallJson.plain(), salida);
    }

    private void insertar(Entrada entrada, List<MensajeSalida> mensajes) {
        Long idMetadata = jdbc.query(SQL_SELECT_ID_METADATA, new MapSqlParameterSource(),
                SqlRow.mapper(r -> r.lng("IdMetadataCuentaContable"))).get(0);

        List<Filtro> validos = new ArrayList<>();
        for (Filtro f : entrada.filtros()) {
            if (CONDICIONES_VALIDAS.contains(f.condicion())) {
                validos.add(f);
            } else {
                Map<String, String> valores = new HashMap<>();
                valores.put("Condicion", f.condicion());
                mensajes.add(MensajeSalida.advertencia(0L, "CONDICION_NO_VALIDA_ASIENTO", valores));
                log.info("{} no valido", f.condicion());
            }
        }

        jdbc.update(SQL_INSERT_METADATA, new MapSqlParameterSource()
                .addValue("IdMetadataCuentaContable", idMetadata)
                .addValue("CodigoMetadata", entrada.codigoMetadata())
                .addValue("DescripcionMetadata", entrada.descripcionMetadata()));
        jdbc.batchUpdate(SQL_INSERT_FILTROS, SqlParams.batch(validos, f -> f.params(idMetadata)));
    }
}
