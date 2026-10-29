package cl.bcs.tesoreria.jobs.programa;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * JDBC implementation of {@link ProgramaRepositorio}. Every public method opens ITS OWN connection from the data source, works with
 * auto-commit off and commits (or rolls back) at the end, so it is independent of the job transaction (the legacy
 * {@code updateProgramm} did the same). A caller therefore needs at most one extra pooled connection at a time: the claim happens before the
 * job transaction opens, the final state after it has closed.
 */
public class ProgramaRepositorioJdbc implements ProgramaRepositorio {

    private final DataSource dataSource;

    public ProgramaRepositorioJdbc(DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource, "dataSource");
    }

    @FunctionalInterface
    private interface Trabajo<T> {
        T hacer(Connection con) throws SQLException;
    }

    private <T> T enTransaccion(String que, Trabajo<T> trabajo) {
        try (Connection con = dataSource.getConnection()) {
            con.setAutoCommit(false);
            try {
                T r = trabajo.hacer(con);
                con.commit();
                return r;
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new ProgramaJdbcException("BAT.Programa: " + que + " failed", e);
        }
    }

    @Override
    public Optional<Programa> cargarPrograma(long idPrograma) {
        return enTransaccion("read program #" + idPrograma, con -> leer(con, idPrograma));
    }

    private static Optional<Programa> leer(Connection con, long idPrograma) throws SQLException {
        try (PreparedStatement st = con.prepareStatement(ProgramaSql.CARGAR)) {
            st.setLong(1, idPrograma);
            try (ResultSet rs = st.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(new Programa(rs.getLong("IdPrograma"), rs.getString("idEstado"), rs.getString("IdTipoPrograma"),
                        rs.getInt("Intento"), rs.getString("Bloqueo"), rs.getString("Nodo"), rs.getLong("IdAgenda"),
                        rs.getString("NombreClasePrograma"), rs.getInt("MaximoIntentos")));
            }
        }
    }

    @Override
    public Map<String, String> parametros(long idPrograma) {
        return enTransaccion("read the parameters of program #" + idPrograma, con -> {
            Map<String, String> salida = new HashMap<>();
            try (PreparedStatement st = con.prepareStatement(ProgramaSql.PARAMETROS)) {
                st.setFetchSize(1000);
                st.setLong(1, idPrograma);
                try (ResultSet rs = st.executeQuery()) {
                    while (rs.next()) {
                        byte[] blob = rs.getBytes("ValorBlob");
                        String valor = blob == null ? rs.getString("Valor") : new String(blob, StandardCharsets.UTF_8);
                        if (valor != null) {
                            salida.put(rs.getString("NombreParametro"), valor);
                        } else {
                            salida.remove(rs.getString("NombreParametro")); // the last duplicate wins, even when it is null
                        }
                    }
                }
            }
            return salida;
        });
    }

    @Override
    public boolean reclamar(long idPrograma) {
        return enTransaccion("claim program #" + idPrograma, con -> {
            Optional<Programa> fila = leer(con, idPrograma);
            if (fila.isEmpty() || !fila.get().reclamable()) {
                return false;
            }
            try (PreparedStatement st = con.prepareStatement(ProgramaSql.RECLAMAR)) {
                st.setInt(1, fila.get().intento() + 1);
                st.setLong(2, idPrograma);
                st.setInt(3, fila.get().intento());
                return st.executeUpdate() == 1; // 0 rows: another instance claimed it between the read and this statement
            }
        });
    }

    @Override
    public boolean terminar(long idPrograma, String resultado) {
        return estadoFinal(idPrograma, "TER", resultado);
    }

    @Override
    public boolean error(long idPrograma, String resultado) {
        return estadoFinal(idPrograma, "ERR", resultado);
    }

    /** TransactionManager.updateProgramm for result 1 and -1: state, fechaFin, Resultado and the Agenda, in one commit (:226-253). */
    private boolean estadoFinal(long idPrograma, String idEstado, String resultado) {
        return enTransaccion("write the final state of program #" + idPrograma, con -> {
            int filas;
            try (PreparedStatement st = con.prepareStatement(ProgramaSql.ESTADO_FINAL)) {
                st.setString(1, idEstado);
                st.setString(2, TextoResultado.truncar(resultado));
                st.setLong(3, idPrograma);
                filas = st.executeUpdate();
            }
            agendaUltima(con, idPrograma);
            return filas > 0;
        });
    }

    private static void agendaUltima(Connection con, long idPrograma) throws SQLException {
        try (PreparedStatement st = con.prepareStatement(ProgramaSql.AGENDA_ULTIMA)) {
            st.setLong(1, idPrograma);
            st.executeUpdate();
        }
    }

    @Override
    public boolean requiereRevision(long idPrograma, String motivo) {
        return enTransaccion("mark program #" + idPrograma + " as requiring review", con -> {
            Optional<Programa> fila = leer(con, idPrograma);
            if (fila.isEmpty()) {
                return false;
            }
            try (PreparedStatement st = con.prepareStatement(ProgramaSql.REVISION)) {
                st.setInt(1, Math.max(fila.get().intento(), fila.get().maximoIntentos()));
                st.setString(2, TextoResultado.truncar(PREFIJO_REVISION + (motivo == null ? "" : motivo)));
                st.setLong(3, idPrograma);
                st.executeUpdate();
            }
            agendaUltima(con, idPrograma);
            return true;
        });
    }
}
