package cl.bcs.plataforma.adapter.out.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import cl.bcs.plataforma.cre.ConsultarEstadoModuloService.Modulo;
import cl.bcs.plataforma.port.out.ModuloRepository;

/**
 * JDBC adapter of {@link ModuloRepository} on the connection of the call (the unit of work opened by the
 * {@code JavaCallUnitOfWorkFilter}); it never opens, commits, rolls back or closes that connection. READ ONLY: a single SELECT.
 *
 * <p>The SQL is the legacy text of {@code ConsultarEstadoModulo.java:22-23} character for character (named parameter
 * {@code :IdEmpresa}); the legacy {@code cl.bcs.utils.SQL}/{@code NamedParameterStatement} turned it into a {@code ?} and bound
 * the value with {@code setObject}, or {@code setNull(..., Types.VARCHAR)} for a {@code null} (PLA-ESM-04: {@code "IdEmpresa" = NULL}
 * is never true). That is all the helper did for this class, so no general named-parameter helper is built (not needed here).
 *
 * <p>SQL failures propagate as the driver's {@link SQLException} (D-ESM-5). The legacy appended {@code Query:} and {@code Parameters:}
 * to the message; that text is not reproduced, and it never leaves the service (DEV-A3).
 */
public class ModuloJdbcRepository implements ModuloRepository {

    static final String SQL_SELECT_MODULOEMPRESA = "SELECT IFNULL(\"IdEstado\",'ABT') AS \"IdEstadoModulo\", IFNULL(\"CodigoModulo\",'') as \"NombreModulo\" "
            + "FROM \"BSCL.Plataforma.BD::PAU.Modulo\" where \"IdEmpresa\" = :IdEmpresa";

    private final Connection con;

    public ModuloJdbcRepository(Connection con) {
        this.con = con;
    }

    @Override
    public List<Modulo> listarPorEmpresa(Long idEmpresa) {
        List<Modulo> lista = new ArrayList<>();
        try (PreparedStatement st = con.prepareStatement(SQL_SELECT_MODULOEMPRESA.replace(":IdEmpresa", "?"))) {
            if (idEmpresa == null) {
                st.setNull(1, Types.VARCHAR);
            } else {
                st.setObject(1, idEmpresa);
            }
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Modulo(rs.getString("IdEstadoModulo"), rs.getString("NombreModulo")));
                }
            }
        } catch (SQLException e) {
            throw new ConsultaModuloFallidaException(e);
        }
        return lista;
    }

    /** Unchecked wrapper so the port stays free of {@code throws}; the cause is the original {@link SQLException}. */
    public static class ConsultaModuloFallidaException extends RuntimeException {
        public ConsultaModuloFallidaException(SQLException cause) {
            super(cause.getMessage(), cause);
        }
    }
}
