package cl.bcs.sura.service;

import cl.bcs.sura.domain.IdsEntidadYPerfiles;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Spring replacement for legacy ModificarPerfilesPortafolioUtils.
 * Uses NamedParameterJdbcTemplate with real HANA SQL from classpath resources.
 * The temp-table approach from the legacy code is replaced with an inline
 * VALUES constructor so no session-scoped temp tables are needed.
 */
@Component
public class PerfilesPortafolioUtilsService {

    private static final Logger log = LoggerFactory.getLogger(PerfilesPortafolioUtilsService.class);

    private static final String BASE = "cl/bcs/sura/socionegocio/sql/";

    private final NamedParameterJdbcTemplate jdbc;
    private final JdbcTemplate jdbcTemplate;

    private final String SQL_CONSULTAR_IDS;
    private final String SQL_CONSULTAR_PERFILES;
    private final String SQL_UPDATE_PERFILES;
    private final String SQL_INSERT_INDICADOR;
    private final String SQL_UPDATE_INDICADOR;

    public PerfilesPortafolioUtilsService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.jdbcTemplate = jdbc.getJdbcTemplate();
        SQL_CONSULTAR_IDS      = load("ModificarPerfilesPortafolioUtils_ConsultarIdsPortafolioIdClientePorDocumentoIdentidad.sql");
        SQL_CONSULTAR_PERFILES = load("ModificarPerfilesPortafolioUtils_ConsultarPerfilesComercialesPorDocumentosIdentidad.sql");
        SQL_UPDATE_PERFILES    = load("ModificarPerfilesPortafolioUtils_ModificarPerfilesPortafolio.sql");
        SQL_INSERT_INDICADOR   = load("ModificarPerfilesPortafolioUtils_InsertIndicadorCobroCustodia.sql");
        SQL_UPDATE_INDICADOR   = load("ModificarPerfilesPortafolioUtils_UpdateIndicadorCobroCustodia.sql");
    }

    // ─── Public API (mirrors legacy ModificarPerfilesPortafolioUtils) ─────────

    public List<IdsEntidadYPerfiles> consultarIdsPortafolioIdClientePorDocumentoIdentidad(
            List<String> documentosIdentidad) {
        if (documentosIdentidad.isEmpty()) return List.of();
        return jdbc.query(SQL_CONSULTAR_IDS,
                new MapSqlParameterSource("DocumentoIdentidad", documentosIdentidad),
                IDS_MAPPER);
    }

    /**
     * Replaces the legacy temp-table flow used by Crear/Modificar SocioNegocio.
     * Builds an inline VALUES clause instead of INSERT+SELECT+DELETE on a HANA
     * local temporary table, so this is safe under connection-pool reuse.
     *
     * @param sociosNegocio  parsed from jin.p_SocioNegocio (each has DocumentoIdentidad
     *                       and IndicadorPortafolio[{CodigoTipoIndicador,ValorIndicador}])
     * @param ids            result of consultarIdsPortafolioIdClientePorDocumentoIdentidad
     */
    public List<IdsEntidadYPerfiles> obtenerEntidadesParaInsertarYModificarEnWSSociosNegocio(
            List<JsonObject> sociosNegocio, List<IdsEntidadYPerfiles> ids) {

        List<IdsEntidadYPerfiles> listaPerfiles = new ArrayList<>();

        for (JsonObject socio : sociosNegocio) {
            String docId = strOf(socio, "DocumentoIdentidad");
            List<IdsEntidadYPerfiles> perfiles = ids.stream()
                    .filter(id -> docId.equals(id.documentoIdentidad))
                    .collect(Collectors.toList());

            Optional<String> valorIndicador = cobrocustValor(socio, "IndicadorPortafolio");
            if (valorIndicador.isPresent() && !perfiles.isEmpty()) {
                String valor = valorIndicador.get();
                perfiles.forEach(p -> p.idFormaCobro = valor);
            }
            listaPerfiles.addAll(perfiles);
        }

        if (listaPerfiles.isEmpty()) return List.of();

        // Build inline VALUES query — replaces temp table INSERT + SELECT + DELETE
        return consultarPerfilesPorListaPortafoliosValores(listaPerfiles).stream()
                .filter(p -> !p.idFormaCobro.isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * Used by ModificarClienteWS after indicators are already written.
     * Queries profiles by DocumentoIdentidad and merges IdFormaCobro from the in-memory list.
     */
    public List<IdsEntidadYPerfiles> obtenerEntidadesYPerfilesCompletas(
            List<IdsEntidadYPerfiles> ids) {
        List<String> docs = ids.stream().map(id -> id.documentoIdentidad).collect(Collectors.toList());
        if (docs.isEmpty()) return List.of();
        List<IdsEntidadYPerfiles> result = jdbc.query(SQL_CONSULTAR_PERFILES,
                new MapSqlParameterSource("DocumentoIdentidad", docs),
                PERFILES_MAPPER);
        result.forEach(entidad -> {
            ids.stream()
                    .filter(id -> id.idPortafolio.equals(entidad.idPortafolio))
                    .findFirst()
                    .ifPresent(match -> {
                        if (entidad.idFormaCobro.isEmpty()) entidad.idFormaCobro = match.idFormaCobro;
                    });
        });
        return result;
    }

    public void modificarPerfilesPortafolio(List<IdsEntidadYPerfiles> perfiles) {
        if (perfiles.isEmpty()) return;
        SqlParameterSource[] batch = perfiles.stream()
                .map(p -> new MapSqlParameterSource()
                        .addValue("IdPerfilComercial", p.idPerfilComercial)
                        .addValue("IdPerfilProductos", p.idPerfilProductos)
                        .addValue("IdPerfilCobros",    p.idPerfilCobros)
                        .addValue("IdPortafolio",      p.idPortafolio))
                .toArray(SqlParameterSource[]::new);
        jdbc.batchUpdate(SQL_UPDATE_PERFILES, batch);
    }

    public void ingresarIndicadoresCobroCustodia(List<IdsEntidadYPerfiles> ids, String valorIndicador) {
        SqlParameterSource[] batch = ids.stream()
                .filter(id -> id.idIndicador == 0)
                .map(id -> new MapSqlParameterSource()
                        .addValue("IdEntidad",      id.idPortafolio)
                        .addValue("ValorIndicador", valorIndicador))
                .toArray(SqlParameterSource[]::new);
        if (batch.length == 0) return;
        jdbc.batchUpdate(SQL_INSERT_INDICADOR, batch);
    }

    public void modificarIndicadoresCobroCustodia(List<IdsEntidadYPerfiles> ids) {
        SqlParameterSource[] batch = ids.stream()
                .filter(id -> id.idIndicador != 0 && "S".equals(id.actualizarFormaCobro))
                .map(id -> new MapSqlParameterSource()
                        .addValue("ValorIndicador", id.idFormaCobro)
                        .addValue("IdEntidad",      id.idIndicador))
                .toArray(SqlParameterSource[]::new);
        if (batch.length == 0) return;
        jdbc.batchUpdate(SQL_UPDATE_INDICADOR, batch);
    }

    public List<IdsEntidadYPerfiles> validarIndicadorFormaCobroPortafolioApv(
            List<IdsEntidadYPerfiles> ids) {
        ids.forEach(id -> {
            if ("PROPOSITOAPV".equals(id.proposito)) id.actualizarFormaCobro = "N";
        });
        return ids;
    }

    // ─── Internal: VALUES-based replacement for temp table query ─────────────

    private static final String VALUES_QUERY_BASE =
        "SELECT\n" +
        "  POR.\"IdPortafolio\"\n" +
        ", POR.\"IdCliente\"\n" +
        ", DOCI.\"DocumentoIdentidad\"\n" +
        ", COALESCE(IND.\"IdIndicador\", 0) AS \"IdIndicador\"\n" +
        ", COALESCE(PERF.\"IdFormaCobro\", TEMP.\"IdFormaCobro\", '') AS \"IdFormaCobro\"\n" +
        ", CLI.\"IdCategoriaInversionista\"\n" +
        ", COALESCE(PERF.\"IdPerfilComercialBase\", '') AS \"IdPerfilComercial\"\n" +
        ", COALESCE(PERF.\"IdPerfilCobros\", '') AS \"IdPerfilCobros\"\n" +
        ", COALESCE(PERF.\"IdPerfilProductos\", '') AS \"IdPerfilProductos\"\n" +
        ", POR.\"Proposito\"\n" +
        "FROM (%s) AS TEMP(\"IdCliente\",\"IdFormaCobro\",\"DocumentoIdentidad\",\"IdPortafolio\")\n" +
        "INNER JOIN \"BSCL.Portafolio.BD.Vistas.UI::ConsultarPortafolio\" AS POR\n" +
        "    ON POR.\"IdPortafolio\" = TEMP.\"IdPortafolio\"\n" +
        "INNER JOIN \"BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente\" CLI\n" +
        "    ON CLI.\"IdCliente\" = TEMP.\"IdCliente\"\n" +
        "INNER JOIN \"BSCL.SocioNegocio.BD.Vistas::PER.ConsultarDocumentoIdentidadPersona\" AS DOCI\n" +
        "    ON DOCI.\"IdPersona\" = POR.\"IdPersona\"\n" +
        "LEFT JOIN \"BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio\" AS IND\n" +
        "    ON IND.\"IdPortafolio\" = POR.\"IdPortafolio\"\n" +
        "   AND IND.\"CodigoTipoIndicador\" = 'COBROCUST'\n" +
        "LEFT JOIN \"BSCL.Plataforma.BD::COM.PerfilComercialAdicional\" AS PERF\n" +
        "    ON PERF.\"IdCategoriaInversionista\" = CLI.\"IdCategoriaInversionista\"\n" +
        "   AND UPPER(PERF.\"IdFormaCobro\") = UPPER(TEMP.\"IdFormaCobro\")\n" +
        "   AND PERF.\"CodigoPortafolio\" = (CASE WHEN POR.\"Proposito\" = 'PROPOSITOAPV' THEN 'APV'\n" +
        "                                         ELSE POR.\"CodigoPortafolio\" END)";

    private List<IdsEntidadYPerfiles> consultarPerfilesPorListaPortafoliosValores(
            List<IdsEntidadYPerfiles> listaPerfiles) {

        StringBuilder values = new StringBuilder("VALUES ");
        List<Object> args = new ArrayList<>();
        for (int i = 0; i < listaPerfiles.size(); i++) {
            if (i > 0) values.append(',');
            values.append("(?,?,?,?)");
            IdsEntidadYPerfiles p = listaPerfiles.get(i);
            args.add(p.idCliente);
            args.add(p.idFormaCobro);
            args.add(p.documentoIdentidad);
            args.add(p.idPortafolio);
        }
        String sql = String.format(VALUES_QUERY_BASE, values);
        return jdbcTemplate.query(sql, PERFILES_MAPPER, args.toArray());
    }

    // ─── Row mappers ──────────────────────────────────────────────────────────

    private static final RowMapper<IdsEntidadYPerfiles> IDS_MAPPER = (rs, row) -> {
        IdsEntidadYPerfiles p = new IdsEntidadYPerfiles();
        p.idPortafolio       = rs.getLong("IdPortafolio");
        p.idCliente          = rs.getLong("IdCliente");
        p.documentoIdentidad = rs.getString("DocumentoIdentidad");
        p.idIndicador        = rs.getLong("IdIndicador");
        p.proposito          = nullToEmpty(rs.getString("Proposito"));
        return p;
    };

    private static final RowMapper<IdsEntidadYPerfiles> PERFILES_MAPPER = (rs, row) -> {
        IdsEntidadYPerfiles p = new IdsEntidadYPerfiles();
        p.idPortafolio             = rs.getLong("IdPortafolio");
        p.idCliente                = rs.getLong("IdCliente");
        p.documentoIdentidad       = nullToEmpty(rs.getString("DocumentoIdentidad"));
        p.idIndicador              = rs.getLong("IdIndicador");
        p.idFormaCobro             = nullToEmpty(rs.getString("IdFormaCobro"));
        p.idCategoriaInversionista = nullToEmpty(rs.getString("IdCategoriaInversionista"));
        p.idPerfilComercial        = nullToEmpty(rs.getString("IdPerfilComercial"));
        p.idPerfilCobros           = nullToEmpty(rs.getString("IdPerfilCobros"));
        p.idPerfilProductos        = nullToEmpty(rs.getString("IdPerfilProductos"));
        try { p.proposito = nullToEmpty(rs.getString("Proposito")); } catch (SQLException ignored) {}
        return p;
    };

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private static Optional<String> cobrocustValor(JsonObject socio, String field) {
        if (!socio.has(field)) return Optional.empty();
        JsonArray arr = socio.get(field).getAsJsonArray();
        for (JsonElement el : arr) {
            JsonObject ind = el.getAsJsonObject();
            if ("COBROCUST".equals(strOf(ind, "CodigoTipoIndicador"))) {
                String v = strOf(ind, "ValorIndicador");
                if (!v.isEmpty()) return Optional.of(v);
            }
        }
        return Optional.empty();
    }

    private static String strOf(JsonObject o, String key) {
        JsonElement e = o.get(key);
        return (e == null || e.isJsonNull()) ? "" : e.getAsString();
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private static String load(String name) {
        String path = BASE + name;
        try (InputStream is = PerfilesPortafolioUtilsService.class.getClassLoader()
                .getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL resource not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load SQL: " + path, e);
        }
    }
}
