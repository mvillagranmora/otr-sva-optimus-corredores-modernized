package cl.bcs.sura.adapter.out;

import cl.bcs.sura.domain.IdsEntidadYPerfiles;
import cl.bcs.sura.port.out.SuraDataPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Runs the legacy HANA statements (the SQL resources are byte-for-byte the legacy files, B-SURA-21) through bound
 * parameters only (RULE-068 pattern, B-SURA-20). This package is the only one allowed to use JDBC.
 */
@Repository
public class LegacyHanaSuraPort implements SuraDataPort {

    private static final String BASE = "cl/bcs/sura/socionegocio/sql/";
    private static final String UTILS = "ModificarPerfilesPortafolioUtils_";
    private static final String WS = "ModificarClienteModificarPerfilesPortafolioWS_";

    private static final String SQL_IDS = load(UTILS + "ConsultarIdsPortafolioIdClientePorDocumentoIdentidad.sql");
    private static final String SQL_PERFILES = load(UTILS + "ConsultarPerfilesComercialesPorDocumentosIdentidad.sql");
    private static final String SQL_UPDATE_PERFILES = load(UTILS + "ModificarPerfilesPortafolio.sql");
    private static final String SQL_INSERT_UTILS = load(UTILS + "InsertIndicadorCobroCustodia.sql");
    private static final String SQL_UPDATE_UTILS = load(UTILS + "UpdateIndicadorCobroCustodia.sql");
    private static final String SQL_INSERT_WS = load(WS + "InsertIndicadorCobroCustodia.sql");
    private static final String SQL_DICCIONARIO = load("PlataformaDiccionario_ConsultarDiccionario.sql");
    private static final String SIN_TRADUCCION = "sin traduccion";
    private static final String SQL_UPDATE_WS = load(WS + "UpdateIndicadorCobroCustodia.sql");

    /**
     * An inline derived table replaces the HANA local temporary table (INSERT + SELECT + DELETE), safe under connection pooling.
     * HANA rejects (VALUES ...) in FROM (checked on QA), so each seed row is a typed SELECT ... FROM DUMMY joined by UNION ALL.
     */
    private static final String SQL_FILTRADOS_CON_SEMILLA =
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
        "FROM (%s) AS TEMP\n" +
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

    private final NamedParameterJdbcTemplate jdbc;
    private final Map<Long, Map<String, String>> diccionarios = new ConcurrentHashMap<>();
    private final JdbcTemplate plain;

    public LegacyHanaSuraPort(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.plain = jdbc.getJdbcTemplate();
    }

    @Override
    public List<IdsEntidadYPerfiles> consultarIdsPortafolioIdCliente(List<String> documentosIdentidad) {
        if (documentosIdentidad.isEmpty()) return List.of();
        return jdbc.query(SQL_IDS, new MapSqlParameterSource("DocumentoIdentidad", documentosIdentidad), IDS_MAPPER);
    }

    @Override
    public List<IdsEntidadYPerfiles> consultarPerfilesComerciales(List<String> documentosIdentidad) {
        if (documentosIdentidad.isEmpty()) return List.of();
        return jdbc.query(SQL_PERFILES, new MapSqlParameterSource("DocumentoIdentidad", documentosIdentidad), PERFILES_MAPPER);
    }

    @Override
    public List<IdsEntidadYPerfiles> consultarPerfilesFiltrados(List<IdsEntidadYPerfiles> semilla) {
        // Legacy would read whatever the temp table holds; an empty seed has nothing to join, so there is nothing to ask.
        if (semilla.isEmpty()) return List.of();
        StringBuilder values = new StringBuilder();
        List<Object> args = new ArrayList<>(semilla.size() * 4);
        for (int i = 0; i < semilla.size(); i++) {
            if (i > 0) values.append(" UNION ALL ");
            values.append("SELECT CAST(? AS BIGINT) AS \"IdCliente\", CAST(? AS NVARCHAR(255)) AS \"IdFormaCobro\", "
                + "CAST(? AS NVARCHAR(40)) AS \"DocumentoIdentidad\", CAST(? AS BIGINT) AS \"IdPortafolio\" FROM DUMMY");
            IdsEntidadYPerfiles p = semilla.get(i);
            args.add(p.idCliente);
            args.add(p.idFormaCobro);
            args.add(p.documentoIdentidad);
            args.add(p.idPortafolio);
        }
        return plain.query(SQL_FILTRADOS_CON_SEMILLA.formatted(values), FILTRADOS_MAPPER, args.toArray());
    }

    @Override
    public Map<String, String> resolverMensajeError(Long idCliente, Long idEmpresa, String codigo, Map<String, String> params) {
        return Map.of("codigo", codigo, "descripcion", describir(idEmpresa, codigo, params), "tipo", "ERROR");
    }

    /**
     * Legacy Diccionario.getMensaje: the dictionary of the company (PLA.Diccionario joined to the PLA.LocalizacionSistema
     * language) is read once per company and kept; a missing code, or no parameter map at all, gives "{codigo} sin traduccion";
     * otherwise every "::Key::" of the text is replaced by its value (null becomes empty).
     */
    private String describir(Long idEmpresa, String codigo, Map<String, String> params) {
        String texto = diccionario(idEmpresa).get(codigo);
        if (texto == null || params == null) return codigo + " " + SIN_TRADUCCION;
        for (Map.Entry<String, String> e : params.entrySet()) {
            texto = texto.replace("::" + e.getKey() + "::", e.getValue() == null ? "" : e.getValue());
        }
        return texto;
    }

    private Map<String, String> diccionario(Long idEmpresa) {
        return diccionarios.computeIfAbsent(idEmpresa, id -> {
            Map<String, String> textos = new HashMap<>();
            jdbc.query(SQL_DICCIONARIO,
                    new MapSqlParameterSource("IdEmpresa", id).addValue("CodigoParametro", "PLA.LocalizacionSistema"),
                    rs -> { textos.put(rs.getString("CodDiccionario"), rs.getString("Descripcion")); });
            return textos;
        });
    }

    @Override
    public void modificarPerfilesPortafolio(List<IdsEntidadYPerfiles> perfiles) {
        if (perfiles.isEmpty()) return;
        SqlParameterSource[] batch = perfiles.stream()
                .map(p -> new MapSqlParameterSource()
                        .addValue("IdPerfilComercial", p.idPerfilComercial)
                        .addValue("IdPerfilProductos", p.idPerfilProductos)
                        .addValue("IdPerfilCobros", p.idPerfilCobros)
                        .addValue("IdPortafolio", p.idPortafolio))
                .toArray(SqlParameterSource[]::new);
        jdbc.batchUpdate(SQL_UPDATE_PERFILES, batch);
    }

    @Override
    public void ingresarIndicadoresCobroCustodia(List<IdsEntidadYPerfiles> perfiles, String valorIndicador, Variante variante) {
        SqlParameterSource[] batch = perfiles.stream()
                .filter(p -> p.idIndicador == 0)
                .map(p -> new MapSqlParameterSource()
                        .addValue("IdEntidad", p.idPortafolio)
                        .addValue("ValorIndicador", valorIndicador))
                .toArray(SqlParameterSource[]::new);
        if (batch.length == 0) return;
        jdbc.batchUpdate(variante == Variante.WS ? SQL_INSERT_WS : SQL_INSERT_UTILS, batch);
    }

    @Override
    public void modificarIndicadoresCobroCustodia(List<IdsEntidadYPerfiles> perfiles, Variante variante) {
        // legacy quirk: the statement receives the IdIndicador in :IdEntidad
        SqlParameterSource[] batch = perfiles.stream()
                .filter(p -> p.idIndicador != 0 && "S".equals(p.actualizarFormaCobro))
                .map(p -> new MapSqlParameterSource()
                        .addValue("ValorIndicador", p.idFormaCobro)
                        .addValue("IdEntidad", p.idIndicador))
                .toArray(SqlParameterSource[]::new);
        if (batch.length == 0) return;
        jdbc.batchUpdate(variante == Variante.WS ? SQL_UPDATE_WS : SQL_UPDATE_UTILS, batch);
    }

    private static final RowMapper<IdsEntidadYPerfiles> IDS_MAPPER = (rs, row) -> {
        IdsEntidadYPerfiles p = new IdsEntidadYPerfiles();
        p.idPortafolio = rs.getLong("IdPortafolio");
        p.idCliente = rs.getLong("IdCliente");
        p.documentoIdentidad = rs.getString("DocumentoIdentidad");
        p.idIndicador = rs.getLong("IdIndicador");
        p.proposito = vacioSiNulo(rs.getString("Proposito"));
        return p;
    };

    /** The profile query (by document) has no "Proposito" column: as in legacy, the field keeps its default "". The filtered query has it. */
    private static final RowMapper<IdsEntidadYPerfiles> PERFILES_MAPPER = perfiles(false);
    private static final RowMapper<IdsEntidadYPerfiles> FILTRADOS_MAPPER = perfiles(true);

    private static RowMapper<IdsEntidadYPerfiles> perfiles(boolean conProposito) {
        return (rs, row) -> {
            IdsEntidadYPerfiles p = new IdsEntidadYPerfiles();
            p.idPortafolio = rs.getLong("IdPortafolio");
            p.idCliente = rs.getLong("IdCliente");
            p.documentoIdentidad = vacioSiNulo(rs.getString("DocumentoIdentidad"));
            p.idIndicador = rs.getLong("IdIndicador");
            p.idFormaCobro = vacioSiNulo(rs.getString("IdFormaCobro"));
            p.idCategoriaInversionista = vacioSiNulo(rs.getString("IdCategoriaInversionista"));
            p.idPerfilComercial = vacioSiNulo(rs.getString("IdPerfilComercial"));
            p.idPerfilCobros = vacioSiNulo(rs.getString("IdPerfilCobros"));
            p.idPerfilProductos = vacioSiNulo(rs.getString("IdPerfilProductos"));
            if (conProposito) p.proposito = vacioSiNulo(rs.getString("Proposito"));
            return p;
        };
    }

    private static String vacioSiNulo(String s) {
        return s == null ? "" : s;
    }

    private static String load(String name) {
        String path = BASE + name;
        try (InputStream is = LegacyHanaSuraPort.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL resource not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load SQL: " + path, e);
        }
    }
}
