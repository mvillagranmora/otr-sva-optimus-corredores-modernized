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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Cancels (anula) a centralization that is neither cancelled nor already accounted.
 *
 * <p>Legacy {@code cl.bcs.contabilidad.AnularCentralizacion}. Implements CONT-G-140..145. Database failures propagate
 * (legacy {@code logica} throws). The platform pre-check {@code RevisarRequisitosBasicos} (legacy L96-102) is not part
 * of this module.
 */
@Service
public class AnularCentralizacionService {

    /** Legacy {@code estadosNoAnulables} (L34). */
    private static final Set<String> ESTADOS_NO_ANULABLES = Set.of("A", "E", "M");

    private final String SQL_SELECT_CENTRALIZACION = SqlResources.load("AnularCentralizacion_SelectCentralizacion.sql");
    private final String SQL_SELECT_COMPROBANTES = SqlResources.load("AnularCentralizacion_SelectComprobantes.sql");
    private final String SQL_UPDATE_EVENTO = SqlResources.load("AnularCentralizacion_UpdateEvento.sql");
    private final String SQL_UPDATE_MOVIMIENTO = SqlResources.load("AnularCentralizacion_UpdateMovimiento.sql");
    private final String SQL_UPDATE_MOVIMIENTO_DATO = SqlResources.load("AnularCentralizacion_UpdateMovimientoDato.sql");
    private final String SQL_UPDATE_CENTRALIZACION = SqlResources.load("AnularCentralizacion_UpdateCentralizacion.sql");

    private final NamedParameterJdbcTemplate jdbc;

    public AnularCentralizacionService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    record Entrada(@SerializedName("p_IdEmpresa") Long idEmpresa,
                   @SerializedName("p_IdUsuario") Long idUsuario,
                   @SerializedName("p_IdModulo") String idModulo,
                   @SerializedName("p_IdCentralizacion") Long idCentralizacion) {}

    record Salida(@SerializedName("P_OUT_ERRORESCONTADOR") Long erroresContador,
                  @SerializedName("P_OUT_MENSAJES") List<MensajeSalida> mensajes) {}

    private record Centralizacion(Long idCentralizacion, String estado, String estadoNombre) {}

    public JsonObject execute(JsonObject params) {
        Entrada entrada = JavaCallJson.read(JavaCallJson.legacy(), params, Entrada.class);
        return JavaCallJson.write(JavaCallJson.legacy(), anular(entrada));
    }

    Salida anular(Entrada entrada) {
        Long id = entrada.idCentralizacion();
        MapSqlParameterSource porId = new MapSqlParameterSource("IdCentralizacion", id);

        List<Centralizacion> centralizaciones = jdbc.query(SQL_SELECT_CENTRALIZACION, porId,
                SqlRow.mapper(r -> new Centralizacion(r.lng("IdCentralizacion"), r.str("Estado"), r.str("EstadoNombre"))));
        int precomprobantes = jdbc.queryForList(SQL_SELECT_COMPROBANTES, porId).size();

        Map<String, String> valores = new HashMap<>();
        valores.put("IdCentralizacion", String.valueOf(id));
        if (centralizaciones.isEmpty()) {
            return error(id, "CENTRALIZACION_NO_EXISTE", valores);
        }
        Centralizacion centralizacion = centralizaciones.get(0);
        if (ESTADOS_NO_ANULABLES.contains(centralizacion.estado())) {
            valores.put("EstadoCentralizacion", String.valueOf(centralizacion.estadoNombre()));
            return error(id, "CENTRALIZACION_NO_SE_PUEDE_ANULAR", valores);
        }
        if (precomprobantes > 0) {
            return error(id, "CENTRALIZACION_ESTA_CONTABILIZADA", valores);
        }

        for (String sql : List.of(SQL_UPDATE_EVENTO, SQL_UPDATE_MOVIMIENTO_DATO, SQL_UPDATE_MOVIMIENTO, SQL_UPDATE_CENTRALIZACION)) {
            jdbc.batchUpdate(sql, SqlParams.batch(centralizaciones,
                    c -> new MapSqlParameterSource("IdCentralizacion", c.idCentralizacion())));
        }
        return new Salida(0L, new ArrayList<>());
    }

    private static Salida error(Long id, String codigo, Map<String, String> valores) {
        return new Salida(1L, List.of(MensajeSalida.error(id, codigo, valores)));
    }
}
