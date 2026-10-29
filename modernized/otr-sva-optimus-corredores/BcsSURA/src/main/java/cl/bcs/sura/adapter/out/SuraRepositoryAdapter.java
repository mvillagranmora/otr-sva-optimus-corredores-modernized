package cl.bcs.sura.adapter.out;

import cl.bcs.sura.domain.IdsEntidadYPerfiles;
import cl.bcs.sura.port.out.SuraDataPort;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Repository
public class SuraRepositoryAdapter implements SuraDataPort {

    private final NamedParameterJdbcTemplate jdbc;

    public SuraRepositoryAdapter(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final String SELECT_COLS =
        "SELECT ID_PORTAFOLIO, ID_CLIENTE, DOCUMENTO_IDENTIDAD, ID_INDICADOR, " +
        "ID_FORMA_COBRO, ID_CATEGORIA_INVERSIONISTA, ID_PERFIL_COMERCIAL, " +
        "ID_PERFIL_COBROS, ID_PERFIL_PRODUCTOS, PROPOSITO, ACTUALIZAR_FORMA_COBRO ";

    @Override
    public List<IdsEntidadYPerfiles> consultarPerfilesComerciales(List<String> documentosIdentidad) {
        return jdbc.query(
            SELECT_COLS + "FROM PERFILES_COMERCIALES_T WHERE DOCUMENTO_IDENTIDAD IN (:docs)",
            Map.of("docs", documentosIdentidad),
            this::mapPerfil);
    }

    @Override
    public List<IdsEntidadYPerfiles> consultarIdsPortafolioIdCliente(List<String> documentosIdentidad) {
        return jdbc.query(
            SELECT_COLS + "FROM PORTAFOLIOS_T WHERE DOCUMENTO_IDENTIDAD IN (:docs)",
            Map.of("docs", documentosIdentidad),
            this::mapPerfil);
    }

    @Override
    public void modificarPerfilesPortafolio(List<IdsEntidadYPerfiles> perfiles) {
        if (perfiles.isEmpty()) return;
        String sql = "UPDATE PERFILES_COMERCIALES_T SET " +
                     "ID_PERFIL_COMERCIAL = :idPerfilComercial, " +
                     "ID_PERFIL_COBROS    = :idPerfilCobros, " +
                     "ID_PERFIL_PRODUCTOS = :idPerfilProductos " +
                     "WHERE ID_PORTAFOLIO = :idPortafolio";
        SqlParameterSource[] batch = perfiles.stream()
            .map(p -> new MapSqlParameterSource()
                .addValue("idPerfilComercial", p.idPerfilComercial)
                .addValue("idPerfilCobros",    p.idPerfilCobros)
                .addValue("idPerfilProductos", p.idPerfilProductos)
                .addValue("idPortafolio",      p.idPortafolio))
            .toArray(SqlParameterSource[]::new);
        jdbc.batchUpdate(sql, batch);
    }

    @Override
    public void ingresarIndicadoresCobroCustodia(List<IdsEntidadYPerfiles> perfiles, String valorIndicador) {
        SqlParameterSource[] batch = perfiles.stream()
            .filter(p -> p.idIndicador == 0)
            .map(p -> new MapSqlParameterSource()
                .addValue("idEntidad",      p.idPortafolio)
                .addValue("valorIndicador", valorIndicador))
            .toArray(SqlParameterSource[]::new);
        if (batch.length == 0) return;
        jdbc.batchUpdate(
            "INSERT INTO INDICADORES_PORTAFOLIO_T (ID_ENTIDAD, VALOR_INDICADOR) " +
            "VALUES (:idEntidad, :valorIndicador)",
            batch);
    }

    @Override
    public void modificarIndicadoresCobroCustodia(List<IdsEntidadYPerfiles> perfiles) {
        SqlParameterSource[] batch = perfiles.stream()
            .filter(p -> p.idIndicador != 0 && "S".equals(p.actualizarFormaCobro))
            .map(p -> new MapSqlParameterSource()
                .addValue("valorIndicador", p.idFormaCobro)
                .addValue("idIndicador",    p.idIndicador))
            .toArray(SqlParameterSource[]::new);
        if (batch.length == 0) return;
        jdbc.batchUpdate(
            "UPDATE INDICADORES_PORTAFOLIO_T SET VALOR_INDICADOR = :valorIndicador " +
            "WHERE ID_INDICADOR = :idIndicador",
            batch);
    }

    private IdsEntidadYPerfiles mapPerfil(ResultSet rs, int row) throws SQLException {
        IdsEntidadYPerfiles p = new IdsEntidadYPerfiles();
        p.idPortafolio             = rs.getLong("ID_PORTAFOLIO");
        p.idCliente                = rs.getLong("ID_CLIENTE");
        p.documentoIdentidad       = rs.getString("DOCUMENTO_IDENTIDAD");
        p.idIndicador              = rs.getLong("ID_INDICADOR");
        p.idFormaCobro             = rs.getString("ID_FORMA_COBRO");
        p.idCategoriaInversionista = rs.getString("ID_CATEGORIA_INVERSIONISTA");
        p.idPerfilComercial        = rs.getString("ID_PERFIL_COMERCIAL");
        p.idPerfilCobros           = rs.getString("ID_PERFIL_COBROS");
        p.idPerfilProductos        = rs.getString("ID_PERFIL_PRODUCTOS");
        p.proposito                = rs.getString("PROPOSITO");
        p.actualizarFormaCobro     = rs.getString("ACTUALIZAR_FORMA_COBRO");
        return p;
    }
}
