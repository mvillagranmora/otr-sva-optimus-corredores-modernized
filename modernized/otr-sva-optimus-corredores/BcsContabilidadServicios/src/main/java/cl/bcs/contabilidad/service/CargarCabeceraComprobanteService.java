package cl.bcs.contabilidad.service;

import cl.bcs.contabilidad.domain.model.MensajeSalida;
import cl.bcs.contabilidad.infrastructure.persistence.SqlParams;
import cl.bcs.contabilidad.infrastructure.persistence.SqlResources;
import cl.bcs.contabilidad.infrastructure.persistence.SqlRow;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Bulk load of accounting voucher headers (ContabilidadTipo) with their centralization link and glosa fields.
 *
 * <p>Legacy {@code cl.bcs.contabilidad.CargarCabeceraComprobante}. Implements CONT-G-200..206. Database failures
 * propagate (legacy L202-205 logs and rethrows).
 */
@Service
public class CargarCabeceraComprobanteService {

    private final String SQL_INSERT_COMPROBANTE = SqlResources.load("CargarCabeceraComprobante_InsertCombrobante.sql");
    private final String SQL_SELECT_ID_COMPROBANTE = SqlResources.load("CargarCabeceraComprobante_SelectIdCombrobante.sql");
    private final String SQL_INSERT_CENTRALIZACIONES = SqlResources.load("CargarCabeceraComprobante_InsertCentralizaciones.sql");
    private final String SQL_INSERT_GLOSA = SqlResources.load("CargarCabeceraComprobante_InsertGlosa.sql");

    private final NamedParameterJdbcTemplate jdbc;

    public CargarCabeceraComprobanteService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** One line of the upload file, already validated by the front-end ({@code MENSAJEVALIDACION}). */
    record Linea(@SerializedName("CCCT_AGRUPA") String agrupa,
                 @SerializedName("CCCT_EJERCICIO") Long ejercicio,
                 @SerializedName("CCCT_FORMATO") String formato,
                 @SerializedName("CCCT_NEMOTECNICO") String nemotecnico,
                 @SerializedName("CCCT_NOMBRETIPOCONTABILIDAD") String nombre,
                 @SerializedName("CCCT_REVERSA") String reversa,
                 @SerializedName("CCCT_TIPOCOMPROBANTE") String tipoComprobante,
                 @SerializedName("CCCT_TIPOGENERACION") String tipoGeneracion,
                 @SerializedName("CCCT_CENTRALIZACION") Long centralizacion,
                 @SerializedName("CCCT_GLOSA") String glosa,
                 @SerializedName("MENSAJEVALIDACION") String mensajeValidacion) {
        boolean valida() { return "OK".equalsIgnoreCase(mensajeValidacion); }
    }

    record Entrada(@SerializedName("p_IdEmpresa") Long idEmpresa,
                   @SerializedName("p_IdUsuario") Long idUsuario,
                   @SerializedName("p_IdModulo") String idModulo,
                   @SerializedName("p_Comprobantes") List<Linea> comprobantes) {}

    record Salida(@SerializedName("P_OUT_MENSAJE") String mensaje,
                  @SerializedName("P_OUT_ERRORESCONTADOR") Long erroresContador,
                  @SerializedName("P_OUT_MENSAJES") List<MensajeSalida> mensajes) {}

    public JsonObject execute(JsonObject params) {
        Entrada entrada = JavaCallJson.read(JavaCallJson.plain(), params, Entrada.class);
        return JavaCallJson.write(JavaCallJson.plain(), cargar(entrada));
    }

    Salida cargar(Entrada entrada) {
        List<MapSqlParameterSource> comprobantes = new ArrayList<>();
        List<MapSqlParameterSource> centralizaciones = new ArrayList<>();
        List<MapSqlParameterSource> glosas = new ArrayList<>();
        List<MensajeSalida> mensajes = new ArrayList<>();

        for (Linea linea : entrada.comprobantes()) {
            if (!linea.valida()) {
                mensajes.add(MensajeSalida.error(0L, "ERROR_FORMATO_LINEA", Map.of()));
                continue;
            }
            Long id = siguienteIdTipoContabilidad();
            comprobantes.add(new MapSqlParameterSource()
                    .addValue("IdTipoContabilidad", id)
                    .addValue("Nombre", linea.nombre())
                    .addValue("Nemo", linea.nemotecnico())
                    .addValue("Tipo", linea.tipoComprobante())
                    .addValue("Reversa", linea.reversa())
                    .addValue("Agrupa", linea.agrupa())
                    .addValue("Condicionado", "S")
                    .addValue("TipoGeneracion", linea.tipoGeneracion())
                    .addValue("IdEmpresa", entrada.idEmpresa())
                    .addValue("Ejercicio", linea.ejercicio())
                    .addValue("Formato", linea.formato())
                    .addValue("Estado", "V"));
            if (linea.centralizacion() != null && linea.centralizacion() > 0) {
                centralizaciones.add(new MapSqlParameterSource()
                        .addValue("IdTipoCentralizacion", linea.centralizacion())
                        .addValue("IdTipoContabilidad", id)
                        .addValue("Estado", "V"));
            }
            if (linea.glosa() != null && !linea.glosa().isEmpty()) {
                long orden = 1;
                for (String campo : linea.glosa().split("\\|")) {
                    glosas.add(new MapSqlParameterSource()
                            .addValue("IdTipoContabilidad", id)
                            .addValue("IdDefinicionAsiento", 0L)
                            .addValue("Origen", "DAT")
                            .addValue("Orden", orden++)
                            .addValue("GlosaCampo", "")
                            .addValue("NombreCampoMovimiento", "")
                            .addValue("IdTipoDatoEvento", campo)
                            .addValue("Estado", "V"));
                }
            }
        }

        if (!comprobantes.isEmpty()) {
            jdbc.batchUpdate(SQL_INSERT_COMPROBANTE, SqlParams.batch(comprobantes));
            if (!centralizaciones.isEmpty()) jdbc.batchUpdate(SQL_INSERT_CENTRALIZACIONES, SqlParams.batch(centralizaciones));
            if (!glosas.isEmpty()) jdbc.batchUpdate(SQL_INSERT_GLOSA, SqlParams.batch(glosas));
        }
        // Legacy resets the counter and message after the loop: format errors are reported but the load succeeds (L190-191).
        return new Salida("UI_EXITO", 0L, mensajes);
    }

    private Long siguienteIdTipoContabilidad() {
        return jdbc.query(SQL_SELECT_ID_COMPROBANTE, new MapSqlParameterSource(), SqlRow.mapper(r -> r.lng("Id"))).get(0);
    }
}
