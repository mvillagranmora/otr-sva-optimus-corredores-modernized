package cl.bcs.normativa.adapter.out;

import cl.bcs.normativa.domain.Direccion;
import cl.bcs.normativa.domain.Empresa;
import cl.bcs.normativa.port.out.EmpresaPort;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Adaptador JDBC para Empresa y Corredora.
 * HANA tables:
 *   "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa"          (alias EMPRESA en H2)
 *   "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredoraEmpresa" (alias CORREDORA_EMPRESA en H2)
 */
@Repository
public class EmpresaJdbcAdapter implements EmpresaPort {

    private final NamedParameterJdbcTemplate jdbc;

    public EmpresaJdbcAdapter(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<Empresa> buscarPorId(long idEmpresa) {
        var sql = """
                SELECT IdEmpresa, Nombre, RazonSocial, Rut, RutRepresentante,
                       Codigo, EsCorredora, EmailPrincipal, FonoPrincipal
                FROM EMPRESA
                WHERE IdEmpresa = :id
                """;
        List<Empresa> result = jdbc.query(sql,
                new MapSqlParameterSource("id", idEmpresa),
                this::mapEmpresa);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public Optional<String> buscarCodigoCorredora(long idEmpresa, String idBolsa) {
        var sql = """
                SELECT CodigoCorredora
                FROM CORREDORA_EMPRESA
                WHERE IdEmpresa = :idEmpresa AND IdBolsa = :idBolsa
                """;
        List<String> result = jdbc.query(sql,
                new MapSqlParameterSource("idEmpresa", idEmpresa).addValue("idBolsa", idBolsa),
                (rs, row) -> rs.getString("CodigoCorredora"));
        return result.isEmpty() ? Optional.empty() : Optional.ofNullable(result.get(0));
    }

    private Empresa mapEmpresa(ResultSet rs, int row) throws SQLException {
        var e = new Empresa();
        e.setId(rs.getLong("IdEmpresa"));
        e.setNombre(rs.getString("Nombre"));
        e.setRazonSocial(rs.getString("RazonSocial"));
        e.setRut(rs.getString("Rut"));
        e.setRutRepresentante(rs.getString("RutRepresentante"));
        e.setCodigo(rs.getString("Codigo"));
        e.setCorredora("S".equals(rs.getString("EsCorredora")));
        e.setEmailPrincipal(rs.getString("EmailPrincipal"));
        e.setFonoPrincipal(rs.getString("FonoPrincipal"));
        // Direccion no disponible en esta vista; se deja nula
        return e;
    }
}
