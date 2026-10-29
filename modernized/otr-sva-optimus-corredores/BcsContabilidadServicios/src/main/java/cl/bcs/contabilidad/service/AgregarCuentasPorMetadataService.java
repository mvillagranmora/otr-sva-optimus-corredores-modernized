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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Creates one accounting entry definition per distinct account of a metadata, with its events and conditions.
 *
 * <p>Legacy {@code cl.bcs.contabilidad.AgregarCuentasPorMetadata}. Implements CONT-G-260..267.
 */
@Service
public class AgregarCuentasPorMetadataService {

    private static final Logger log = LoggerFactory.getLogger(AgregarCuentasPorMetadataService.class);

    private final String SQL_CUENTAS_FILTROS = SqlResources.load("AgregarCuentasPorMetadata_SelectCuentaFiltros.sql");
    private final String SQL_SELECT_ID = SqlResources.load("AgregarCuentasPorMetadata_SelectIdCuentaContable.sql");
    private final String SQL_SELECT_ORDEN = SqlResources.load("AgregarCuentasPorMetadata_SelectUltimoOrden.sql");
    private final String SQL_INSERT_ASIENTO = SqlResources.load("AgregarCuentasPorMetadata_InsertContabilidadDefinicionAsiento.sql");
    private final String SQL_INSERT_EVENTO = SqlResources.load("AgregarCuentasPorMetadata_InsertContabilidadDefinicionTipoEvento.sql");
    private final String SQL_INSERT_CONDICION = SqlResources.load("AgregarCuentasPorMetadata_InsertContabilidadTipoAsientoCondicion.sql");

    private final NamedParameterJdbcTemplate jdbc;

    public AgregarCuentasPorMetadataService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public record Evento(@SerializedName("IdTipoEvento") String idTipoEvento,
                         @SerializedName("Operador") String operador) {}

    public record CuentaFiltro(@SerializedName("CuentaOptimus") String cuentaOptimus,
                               @SerializedName("IdTipoDatoEvento") String idTipoDatoEvento,
                               @SerializedName("Condicion") String condicion,
                               @SerializedName("Valor") String valor) {}

    public record Entrada(@SerializedName("p_IdEmpresa") Long idEmpresa,
                          @SerializedName("p_IdUsuario") Long idUsuario,
                          @SerializedName("p_IdModulo") String idModulo,
                          @SerializedName("p_IdMetadataCuentaContable") Long idMetadataCuentaContable,
                          @SerializedName("p_Eventos") List<Evento> eventos,
                          @SerializedName("p_DebeHaber") String debeHaber,
                          @SerializedName("p_Ejercicio") String ejercicio,
                          @SerializedName("p_Formato") String formato,
                          @SerializedName("p_IdTipoContabilidad") Long idTipoContabilidad,
                          @SerializedName("p_CuentasFiltros") List<CuentaFiltro> cuentasFiltros) {}

    public record Salida(@SerializedName("P_OUT_MENSAJE") String mensaje,
                         @SerializedName("P_OUT_ERRORESCONTADOR") Long erroresContador) {
        public boolean ok() { return erroresContador == 0L; }
    }

    private record DefinicionAsiento(Long idDefinicionAsiento, Long idTipoContabilidad, String idCuentaContable,
                                     String ejercicio, Long orden, String debeHaber, String tipoEvento,
                                     String formato, Long idMetadataCuentaContable) {
        MapSqlParameterSource params() {
            return new MapSqlParameterSource()
                    .addValue("IdDefinicionAsiento", idDefinicionAsiento)
                    .addValue("IdTipoContabilidad", idTipoContabilidad)
                    .addValue("IdCuentaContable", idCuentaContable)
                    .addValue("Ejercicio", ejercicio)
                    .addValue("Orden", orden)
                    .addValue("DebeHaber", debeHaber)
                    .addValue("TipoEvento", tipoEvento)
                    .addValue("Condicionado", "S")
                    .addValue("OmiteCondicionCmbte", "N")
                    .addValue("Estado", "V")
                    .addValue("Formato", formato)
                    .addValue("IdMetadataCuentaContable", idMetadataCuentaContable);
        }
    }

    private record EventoAsiento(String idTipoEvento, Long idDefinicionAsiento, String operador) {
        MapSqlParameterSource params() {
            return new MapSqlParameterSource()
                    .addValue("IdTipoEvento", idTipoEvento)
                    .addValue("IdDefinicionAsiento", idDefinicionAsiento)
                    .addValue("Operador", operador)
                    .addValue("Estado", "V");
        }
    }

    private record CondicionAsiento(Long idDefinicionAsiento, String idTipoDatoEvento, String valorVarchar,
                                    BigDecimal valorDecimal, String condicion) {
        MapSqlParameterSource params() {
            return new MapSqlParameterSource()
                    .addValue("IdDefinicionAsiento", idDefinicionAsiento)
                    .addValue("IdTipoContabilidad", 0L)
                    .addValue("IdTipoDatoEvento", idTipoDatoEvento)
                    .addValue("ValorVarchar", valorVarchar)
                    .addValue("ValorDecimal", valorDecimal)
                    .addValue("Condicion", condicion)
                    .addValue("Conector", "AND")
                    .addValue("TipoCondicion", "A")
                    .addValue("Estado", "V")
                    .addValue("OmiteCondicionCmbte", "");
        }
    }

    public JsonObject execute(JsonObject params) {
        Entrada entrada = JavaCallJson.read(JavaCallJson.plain(), params, Entrada.class);
        return JavaCallJson.write(JavaCallJson.plain(), agregar(entrada));
    }

    /** Legacy {@code logica}: every failure is reported as UI_ERROR, never thrown (L221-224). */
    public Salida agregar(Entrada entrada) {
        try {
            insertar(entrada);
            return new Salida("UI_EXITO", 0L);
        } catch (RuntimeException e) {
            log.error("AgregarCuentasPorMetadata error", e);
            return new Salida("UI_ERROR", 1L);
        }
    }

    private void insertar(Entrada entrada) {
        List<CuentaFiltro> filtros = entrada.cuentasFiltros();
        if (filtros == null || filtros.isEmpty()) {
            filtros = jdbc.query(SQL_CUENTAS_FILTROS,
                    new MapSqlParameterSource("IdMetadataCuentaContable", entrada.idMetadataCuentaContable()),
                    SqlRow.mapper(r -> new CuentaFiltro(r.str("CuentaOptimus"), r.str("IdTipoDatoEvento"),
                            r.str("Condicion"), r.str("Valor"))));
        }
        long maxOrden = ultimoOrden(entrada.idTipoContabilidad());
        String tipoEvento = entrada.eventos().size() > 1 ? "C" : "S";

        Set<String> cuentas = new LinkedHashSet<>();
        List<DefinicionAsiento> asientos = new ArrayList<>();
        for (CuentaFiltro f : filtros) {
            if (cuentas.add(f.cuentaOptimus())) {
                asientos.add(new DefinicionAsiento(siguienteIdDefinicionAsiento(), entrada.idTipoContabilidad(),
                        f.cuentaOptimus(), entrada.ejercicio(), maxOrden + cuentas.size(), entrada.debeHaber(),
                        tipoEvento, entrada.formato(), entrada.idMetadataCuentaContable()));
            }
        }

        List<EventoAsiento> eventos = new ArrayList<>();
        for (Evento e : entrada.eventos())
            for (DefinicionAsiento a : asientos)
                eventos.add(new EventoAsiento(e.idTipoEvento(), a.idDefinicionAsiento(), e.operador()));

        List<CondicionAsiento> condiciones = new ArrayList<>();
        for (DefinicionAsiento a : asientos)
            for (CuentaFiltro f : filtros)
                if (f.cuentaOptimus().equalsIgnoreCase(a.idCuentaContable()))
                    condiciones.add(new CondicionAsiento(a.idDefinicionAsiento(), f.idTipoDatoEvento().trim(),
                            f.valor().trim(), decimalOrZero(f.valor()), f.condicion()));

        jdbc.batchUpdate(SQL_INSERT_ASIENTO, SqlParams.batch(asientos, DefinicionAsiento::params));
        jdbc.batchUpdate(SQL_INSERT_EVENTO, SqlParams.batch(eventos, EventoAsiento::params));
        jdbc.batchUpdate(SQL_INSERT_CONDICION, SqlParams.batch(condiciones, CondicionAsiento::params));
    }

    private long ultimoOrden(Long idTipoContabilidad) {
        List<Long> max = jdbc.query(SQL_SELECT_ORDEN, new MapSqlParameterSource("IdTipoContabilidad", idTipoContabilidad),
                SqlRow.mapper(r -> r.lng("Max")));
        return max.isEmpty() || max.get(0) == null ? 0L : max.get(0);
    }

    private Long siguienteIdDefinicionAsiento() {
        return jdbc.query(SQL_SELECT_ID, new MapSqlParameterSource(), SqlRow.mapper(r -> r.lng("Id"))).get(0);
    }

    /** Legacy parses the untrimmed value; anything that is not a number becomes 0 (L200-204). */
    private static BigDecimal decimalOrZero(String valor) {
        try {
            return new BigDecimal(valor);
        } catch (RuntimeException e) {
            return BigDecimal.ZERO;
        }
    }
}
