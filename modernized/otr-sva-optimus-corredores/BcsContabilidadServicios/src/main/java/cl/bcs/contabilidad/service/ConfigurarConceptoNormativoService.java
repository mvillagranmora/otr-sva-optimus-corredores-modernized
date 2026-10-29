package cl.bcs.contabilidad.service;

import cl.bcs.contabilidad.domain.model.MensajeSalida;
import cl.bcs.contabilidad.infrastructure.persistence.SqlCount;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Synchronises the conditions of a regulatory concept: new ones are inserted, known ones updated, missing ones deleted.
 *
 * <p>Legacy {@code cl.bcs.contabilidad.ConfigurarConceptoNormativo}. Implements CONT-G-340..346. Input keys are not
 * {@code p_}-prefixed ({@code IdConceptoNormativo, IdEmpresa, condiciones}). Every failure is reported as an ERROR
 * message whose descripcion is the exception text (legacy L178-181); nothing is written while any condition is invalid.
 */
@Service
public class ConfigurarConceptoNormativoService {

    private static final Logger log = LoggerFactory.getLogger(ConfigurarConceptoNormativoService.class);

    private final String SQL_SELECT_CONDICIONES = SqlResources.load("ConfigurarConceptoNormativo_Select_CondicionConceptoExistentes.sql");
    private final String SQL_SELECT_CONCEPTO = SqlResources.load("ConfigurarConceptoNormativo_SelectConceptoNormativo.sql");
    private final String SQL_SELECT_TIPO_EVENTO = SqlResources.load("ConfigurarConceptoNormativo_SelectTipoDatoEvento.sql");
    private final String SQL_SELECT_TIPO_VALOR = SqlResources.load("ConfigurarConceptoNormativo_SelectTipoDatoValorValidos.sql");
    private final String SQL_INSERT_CONDICION = SqlResources.load("ConfigurarConceptoNormativo_InsCondicionConcepto.sql");
    private final String SQL_UPDATE_CONDICION = SqlResources.load("ConfigurarConceptoNormativo_UpdateCondicionConcepto.sql");
    private final String SQL_DELETE_CONDICION = SqlResources.load("ConfigurarConceptoNormativo_DeleteCondicionConcepto.sql");

    private final NamedParameterJdbcTemplate jdbc;

    public ConfigurarConceptoNormativoService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    record Condicion(@SerializedName("IdCondicion") Long idCondicion,
                     @SerializedName("IdTipoDatoEvento") String idTipoDatoEvento,
                     @SerializedName("ValorVarchar") String valorVarchar,
                     @SerializedName("OperadorFuncion") String operadorFuncion,
                     @SerializedName("IdConceptoNormativo") Long idConceptoNormativo) {
        Condicion delConcepto(Long idConcepto) {
            return new Condicion(idCondicion, idTipoDatoEvento, valorVarchar, operadorFuncion, idConcepto);
        }

        MapSqlParameterSource params() {
            return new MapSqlParameterSource()
                    .addValue("IdCondicion", idCondicion)
                    .addValue("IdConceptoNormativo", idConceptoNormativo)
                    .addValue("IdTipoDatoEvento", idTipoDatoEvento)
                    .addValue("ValorVarchar", valorVarchar)
                    .addValue("OperadorFuncion", operadorFuncion);
        }
    }

    record Entrada(@SerializedName("IdConceptoNormativo") Long idConceptoNormativo,
                   @SerializedName("IdEmpresa") Long idEmpresa,
                   @SerializedName("condiciones") List<Condicion> condiciones) {}

    /** Mutable collector of the output (legacy {@code ConfigurarCondicionConcepto_OUT}). */
    static final class Salida {
        @SerializedName("P_OUT_MENSAJES") final List<MensajeSalida> mensajes = new ArrayList<>();
        @SerializedName("P_OUT_ERRORESCONTADOR") long erroresContador;
        @SerializedName("P_OUT_INSERTADOS") final List<Condicion> insertados = new ArrayList<>();
        @SerializedName("P_OUT_ACTUALIZADOS") final List<Condicion> actualizados = new ArrayList<>();
        @SerializedName("P_OUT_ELIMINADOS") final List<Long> eliminados = new ArrayList<>();

        void error(MensajeSalida m) {
            erroresContador++;
            mensajes.add(m);
        }
    }

    public JsonObject execute(JsonObject params) {
        Entrada entrada = JavaCallJson.read(JavaCallJson.plain(), params, Entrada.class);
        Salida salida = new Salida();
        try {
            configurar(entrada, salida);
        } catch (RuntimeException ex) {
            log.error("ConfigurarConceptoNormativo error", ex);
            salida.error(new MensajeSalida(0L, MensajeSalida.ERROR, "", ex.toString()));
        }
        return JavaCallJson.write(JavaCallJson.plain(), salida);
    }

    private void configurar(Entrada entrada, Salida salida) {
        MapSqlParameterSource porConcepto = new MapSqlParameterSource("IdConceptoNormativo", entrada.idConceptoNormativo());
        if (SqlCount.count(jdbc, SQL_SELECT_CONCEPTO, porConcepto) == 0) {
            salida.error(new MensajeSalida(0L, MensajeSalida.ERROR, "CONCEPTO_NO_EXISTE", "CONCEPTO_NO_EXISTE"));
            return;
        }
        List<Long> idsActuales = jdbc.query(SQL_SELECT_CONDICIONES, porConcepto, SqlRow.mapper(r -> r.lng("IdCondicion")));

        List<String> tipos = distintos(entrada.condiciones().stream().map(Condicion::idTipoDatoEvento).toList());
        List<String> valores = distintos(entrada.condiciones().stream().map(Condicion::valorVarchar).toList());
        Set<String> tiposValidos = new LinkedHashSet<>(jdbc.query(SQL_SELECT_TIPO_EVENTO,
                new MapSqlParameterSource("tipos", tipos), SqlRow.mapper(r -> r.str("IdTipoDatoEvento"))));
        Set<String> combinacionesValidas = new LinkedHashSet<>(jdbc.query(SQL_SELECT_TIPO_VALOR,
                new MapSqlParameterSource().addValue("tipos", tipos).addValue("valores", valores),
                SqlRow.mapper(r -> r.str("IdTipoDatoEvento") + "_" + r.str("ValorVarchar"))));

        List<Condicion> insertar = new ArrayList<>();
        List<Condicion> actualizar = new ArrayList<>();
        Set<Long> idsEntrantes = new LinkedHashSet<>();
        for (Condicion c : entrada.condiciones()) {
            if (!tiposValidos.contains(c.idTipoDatoEvento())) {
                Map<String, String> v = new HashMap<>();
                v.put("IdTipoCondicion", c.idTipoDatoEvento());
                salida.error(MensajeSalida.error(0L, "TIPO_EVENTO_NO_EXISTE", v));
                continue;
            }
            if (!combinacionesValidas.contains(c.idTipoDatoEvento() + "_" + c.valorVarchar())) {
                Map<String, String> v = new HashMap<>();
                v.put("Valor", c.valorVarchar());
                v.put("TipoEvento", c.idTipoDatoEvento());
                salida.error(MensajeSalida.error(0L, "TIPO_EVENTO_VALOR_INVALIDO", v));
                continue;
            }
            Condicion conConcepto = c.delConcepto(entrada.idConceptoNormativo());
            if (c.idCondicion() == null || !idsActuales.contains(c.idCondicion())) {
                insertar.add(conConcepto);
            } else {
                actualizar.add(conConcepto);
                idsEntrantes.add(c.idCondicion());
            }
        }
        List<Long> eliminar = idsActuales.stream().filter(id -> !idsEntrantes.contains(id)).toList();
        if (salida.erroresContador > 0) return;

        if (!insertar.isEmpty()) {
            jdbc.batchUpdate(SQL_INSERT_CONDICION, SqlParams.batch(insertar, Condicion::params));
            salida.insertados.addAll(insertar);
        }
        if (!actualizar.isEmpty()) {
            jdbc.batchUpdate(SQL_UPDATE_CONDICION, SqlParams.batch(actualizar, Condicion::params));
            salida.actualizados.addAll(actualizar);
        }
        if (!eliminar.isEmpty()) {
            jdbc.batchUpdate(SQL_DELETE_CONDICION, SqlParams.batch(eliminar, id -> new MapSqlParameterSource("IdCondicion", id)));
            salida.eliminados.addAll(eliminar);
        }
    }

    private static List<String> distintos(List<String> valores) {
        return valores.stream().filter(Objects::nonNull).collect(Collectors.toCollection(LinkedHashSet::new)).stream().toList();
    }
}
