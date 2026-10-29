package cl.bcs.contabilidad.service;

import cl.bcs.contabilidad.domain.model.MensajeSalida;
import cl.bcs.contabilidad.infrastructure.persistence.HanaSequences;
import cl.bcs.contabilidad.infrastructure.persistence.SqlParams;
import cl.bcs.contabilidad.infrastructure.persistence.SqlResources;
import cl.bcs.contabilidad.infrastructure.persistence.SqlRow;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Inserts the header (Centralizacion, Estado E) of each selected centralization type before it is processed.
 *
 * <p>Legacy {@code cl.bcs.contabilidad.CentralizaGeneraCabecera}. Implements CONT-G-440..445. Each tipo yields an ERROR
 * message CENTRALIZACION_GENERADA_CON_ERROR while the counter stays 0 (legacy behaviour: the header starts in error
 * until the centralization overwrites it). FechaProceso/HoraProceso are UTC (legacy read them from the DB clock).
 */
@Service
public class CentralizaGeneraCabeceraService {

    private static final String SECUENCIA = "BSCL.Contabilidad.BD.Secuencias::IdCentralizacion";

    private final String SQL_SELECT_TIPOS = SqlResources.load("CentralizaGenerico_SelectTipoCentralizacion.sql");
    private final String SQL_INSERT = SqlResources.load("CentralizaGenerico_InsertCentralizacion.sql");

    private final NamedParameterJdbcTemplate jdbc;
    private final Clock clock;

    @Autowired
    public CentralizaGeneraCabeceraService(NamedParameterJdbcTemplate jdbc) {
        this(jdbc, Clock.systemUTC());
    }

    CentralizaGeneraCabeceraService(NamedParameterJdbcTemplate jdbc, Clock clock) {
        this.jdbc = jdbc;
        this.clock = clock;
    }

    record TipoSeleccionado(@SerializedName("IdTipoCentralizacion") Long idTipoCentralizacion) {}

    record Entrada(@SerializedName("p_IdEmpresa") Long idEmpresa,
                   @SerializedName("p_Ejercicio") Long ejercicio,
                   @SerializedName("p_FechaContable") String fechaContable,
                   @SerializedName("p_CodigoUsuario") Long codigoUsuario,
                   @SerializedName("p_TipoCentralizacion") List<TipoSeleccionado> tipos) {}

    record Salida(@SerializedName("P_OUT_ERRORESCONTADOR") Long erroresContador,
                  @SerializedName("P_OUT_MENSAJES") List<MensajeSalida> mensajes) {}

    private record TipoCentralizacion(Long idTipoCentralizacion, String nombre, String tipoEnvio) {}

    public JsonObject execute(JsonObject params) {
        Entrada entrada = JavaCallJson.read(JavaCallJson.legacy(), params, Entrada.class);
        return JavaCallJson.write(JavaCallJson.legacy(), generar(entrada));
    }

    Salida generar(Entrada entrada) {
        List<TipoSeleccionado> seleccion = entrada.tipos() == null ? List.of() : entrada.tipos();
        if (seleccion.isEmpty()) return new Salida(0L, List.of());

        List<Long> ids = seleccion.stream().map(TipoSeleccionado::idTipoCentralizacion).toList();
        Map<Long, TipoCentralizacion> catalogo = jdbc.query(SQL_SELECT_TIPOS, new MapSqlParameterSource("IdTipoCentralizacion", ids),
                        SqlRow.mapper(r -> new TipoCentralizacion(r.lng("IdTipoCentralizacion"), r.str("Nombre"), r.str("TipoEnvio"))))
                .stream().collect(Collectors.toMap(TipoCentralizacion::idTipoCentralizacion, Function.identity(), (a, b) -> b));

        LocalDateTime ahora = LocalDateTime.now(clock);
        String fechaProceso = ahora.format(DateTimeFormatter.ISO_LOCAL_DATE);
        String horaProceso = ahora.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        List<Long> secuencias = HanaSequences.next(jdbc, SECUENCIA, seleccion.size());
        String codigoUsuario = entrada.codigoUsuario() != null ? entrada.codigoUsuario().toString() : "";

        List<MapSqlParameterSource> filas = new ArrayList<>();
        List<MensajeSalida> mensajes = new ArrayList<>();
        for (int i = 0; i < seleccion.size(); i++) {
            Long idTipo = seleccion.get(i).idTipoCentralizacion();
            TipoCentralizacion tipo = catalogo.get(idTipo);
            if (tipo == null) throw new IllegalStateException("Tipo de centralizacion no existe: " + idTipo);
            Long idCentralizacion = secuencias.get(i);
            filas.add(new MapSqlParameterSource()
                    .addValue("IdCentralizacion", idCentralizacion)
                    .addValue("IdTipoCentralizacion", idTipo)
                    .addValue("IdEmpresa", entrada.idEmpresa())
                    .addValue("FechaProceso", fechaProceso)
                    .addValue("HoraProceso", horaProceso)
                    .addValue("FechaContable", entrada.fechaContable())
                    .addValue("Glosa", "")
                    .addValue("CodigoUsuario", codigoUsuario)
                    .addValue("TipoGeneracion", tipo.tipoEnvio())
                    .addValue("Estado", "E"));
            Map<String, String> valores = new HashMap<>();
            valores.put("Centralizacion", tipo.nombre());
            valores.put("IdCentralizacion", String.valueOf(idCentralizacion));
            mensajes.add(MensajeSalida.error(idTipo, "CENTRALIZACION_GENERADA_CON_ERROR", valores));
        }
        jdbc.batchUpdate(SQL_INSERT, SqlParams.batch(filas));
        return new Salida(0L, mensajes);
    }
}
