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
 * Edits a metadata: description, filters added/removed, and entry definitions for accounts the metadata did not cover.
 *
 * <p>Legacy {@code cl.bcs.contabilidad.EditarMetadata}. Implements CONT-G-300..305. A filter is identified by
 * (CuentaOptimus, IdTipoDatoEvento), case-insensitively; a changed value of an existing filter is not updated (legacy).
 * For each voucher that already uses the metadata, the filters of new accounts are handed to
 * {@link AgregarCuentasPorMetadataService}. Every failure is reported as UI_ERROR (legacy L207-211).
 */
@Service
public class EditarMetadataService {

    private static final Logger log = LoggerFactory.getLogger(EditarMetadataService.class);

    private final String SQL_UPDATE_METADATA = SqlResources.load("EditarMetadata_UpdateMetadataCuentaContable.sql");
    private final String SQL_INSERT_FILTROS = SqlResources.load("EditarMetadata_InsertMetadataFiltros.sql");
    private final String SQL_SELECT_FILTROS = SqlResources.load("EditarMetadata_SelectMetadataFiltros.sql");
    private final String SQL_BORRAR_FILTROS = SqlResources.load("EditarMetadata_BorrarMetadataFiltros.sql");
    private final String SQL_SELECT_COMPROBANTE = SqlResources.load("EditarMetadata_SelectComprobante.sql");
    private final String SQL_SELECT_EVENTO = SqlResources.load("EditarMetadata_SelectEventoComprobante.sql");

    private final NamedParameterJdbcTemplate jdbc;
    private final AgregarCuentasPorMetadataService agregarCuentasPorMetadata;

    public EditarMetadataService(NamedParameterJdbcTemplate jdbc, AgregarCuentasPorMetadataService agregarCuentasPorMetadata) {
        this.jdbc = jdbc;
        this.agregarCuentasPorMetadata = agregarCuentasPorMetadata;
    }

    record Filtro(@SerializedName("CuentaOptimus") String cuentaOptimus,
                  @SerializedName("IdTipoDatoEvento") String idTipoDatoEvento,
                  @SerializedName("Condicion") String condicion,
                  @SerializedName("Valor") String valor) {
        boolean mismaClave(FiltroExistente e) {
            return e.cuentaOptimus().equalsIgnoreCase(cuentaOptimus) && e.idTipoDatoEvento().equalsIgnoreCase(idTipoDatoEvento);
        }

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
                   @SerializedName("p_IdMetadataCuentaContable") Long idMetadataCuentaContable,
                   @SerializedName("p_DescripcionMetadata") String descripcionMetadata,
                   @SerializedName("p_Filtros") List<Filtro> filtros) {}

    record Salida(@SerializedName("P_OUT_MENSAJE") String mensaje,
                  @SerializedName("P_OUT_ERRORESCONTADOR") Long erroresContador) {}

    private record FiltroExistente(Long idMetadataFiltros, String cuentaOptimus, String idTipoDatoEvento) {}

    private record Comprobante(Long idTipoContabilidad, String debeHaber, Long ejercicio, String formato) {}

    public JsonObject execute(JsonObject params) {
        Entrada entrada = JavaCallJson.read(JavaCallJson.plain(), params, Entrada.class);
        Salida salida;
        try {
            editar(entrada);
            salida = new Salida("UI_EXITO", 0L);
        } catch (RuntimeException e) {
            log.error("EditarMetadata error", e);
            salida = new Salida("UI_ERROR", 1L);
        }
        return JavaCallJson.write(JavaCallJson.plain(), salida);
    }

    private void editar(Entrada entrada) {
        Long idMetadata = entrada.idMetadataCuentaContable();
        List<FiltroExistente> existentes = jdbc.query(SQL_SELECT_FILTROS, new MapSqlParameterSource("IdMetadataCuentaContable", idMetadata),
                SqlRow.mapper(r -> new FiltroExistente(r.lng("IdMetadataFiltros"), r.str("CuentaOptimus"), r.str("IdTipoDatoEvento"))));

        List<Filtro> insertar = entrada.filtros().stream()
                .filter(f -> existentes.stream().noneMatch(f::mismaClave)).toList();
        List<Filtro> deCuentasNuevas = entrada.filtros().stream()
                .filter(f -> existentes.stream().noneMatch(e -> e.cuentaOptimus().equalsIgnoreCase(f.cuentaOptimus()))).toList();
        List<FiltroExistente> eliminar = existentes.stream()
                .filter(e -> entrada.filtros().stream().noneMatch(f -> f.mismaClave(e))).toList();

        jdbc.update(SQL_UPDATE_METADATA, new MapSqlParameterSource()
                .addValue("IdMetadataCuentaContable", idMetadata)
                .addValue("DescripcionMetadata", entrada.descripcionMetadata()));
        jdbc.batchUpdate(SQL_INSERT_FILTROS, SqlParams.batch(insertar, f -> f.params(idMetadata)));
        jdbc.batchUpdate(SQL_BORRAR_FILTROS, SqlParams.batch(eliminar,
                e -> new MapSqlParameterSource("IdMetadataFiltros", e.idMetadataFiltros())));

        if (!deCuentasNuevas.isEmpty()) agregarCuentasNuevas(entrada, deCuentasNuevas);
    }

    private void agregarCuentasNuevas(Entrada entrada, List<Filtro> filtros) {
        Long idMetadata = entrada.idMetadataCuentaContable();
        List<Comprobante> comprobantes = jdbc.query(SQL_SELECT_COMPROBANTE, new MapSqlParameterSource("IdMetadataCuentaContable", idMetadata),
                SqlRow.mapper(r -> new Comprobante(r.lng("IdTipoContabilidad"), r.str("DebeHaber"), r.lng("Ejercicio"), r.str("Formato"))));
        List<AgregarCuentasPorMetadataService.CuentaFiltro> cuentasFiltros = filtros.stream()
                .map(f -> new AgregarCuentasPorMetadataService.CuentaFiltro(f.cuentaOptimus(), f.idTipoDatoEvento(), f.condicion(), f.valor()))
                .toList();
        for (Comprobante c : comprobantes) {
            List<AgregarCuentasPorMetadataService.Evento> eventos = jdbc.query(SQL_SELECT_EVENTO, new MapSqlParameterSource()
                            .addValue("IdTipoContabilidad", c.idTipoContabilidad())
                            .addValue("IdMetadataCuentaContable", idMetadata),
                    SqlRow.mapper(r -> new AgregarCuentasPorMetadataService.Evento(r.str("IdTipoEvento"), r.str("Operador"))));
            // Legacy ignores the delegate's result (L200).
            agregarCuentasPorMetadata.agregar(new AgregarCuentasPorMetadataService.Entrada(entrada.idEmpresa(), entrada.idUsuario(),
                    entrada.idModulo(), idMetadata, eventos, c.debeHaber(), String.valueOf(c.ejercicio()), c.formato(),
                    c.idTipoContabilidad(), cuentasFiltros));
        }
    }
}
