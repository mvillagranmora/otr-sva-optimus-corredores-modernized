package cl.bcs.tesoreria.jobs.guarda;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * JDBC implementation of {@link MarcasRepositorio}: ONLY the existing columns of {@code BAT.LogPrograma} (FechaLog, IdTipoPrograma, IdPrograma,
 * Mensaje; real DDL in BAT_DDL_REAL.md). Each call opens its own connection (auto-commit off, explicit commit), so it never joins the
 * transaction of the job. SQL is qualified with the schema {@code BSCL} because that connection does not get the job's {@code SET SCHEMA}.
 */
public class MarcasRepositorioJdbc implements MarcasRepositorio {

    static final String LOG = "BSCL.\"BSCL.Plataforma.BD::BAT.LogPrograma\"";
    /** Parameters: IdTipoPrograma, IdPrograma, Mensaje. */
    static final String INSERTAR = "INSERT INTO " + LOG + " (\"FechaLog\", \"IdTipoPrograma\", \"IdPrograma\", \"Mensaje\") "
            + "VALUES (CURRENT_UTCTIMESTAMP, ?, ?, ?)";
    /** Parameters: IdPrograma. Only the marks of this program; the other log lines of the program are not read. */
    static final String LEER = "SELECT \"Mensaje\" FROM " + LOG + " WHERE \"IdPrograma\" = ? AND \"Mensaje\" LIKE 'H4G|%'";

    private final DataSource dataSource;

    public MarcasRepositorioJdbc(DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource, "dataSource");
    }

    @Override
    public void registrar(MarcaGuarda marca, String idTipoPrograma) {
        try (Connection con = dataSource.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement st = con.prepareStatement(INSERTAR)) {
                st.setString(1, idTipoPrograma == null ? "" : idTipoPrograma);
                st.setLong(2, marca.idPrograma());
                st.setString(3, marca.formatear());
                st.executeUpdate();
                con.commit();
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException | RuntimeException e) {
            throw new GuardaTomeeException("BAT.LogPrograma: mark " + marca.fase() + " of " + marca.clase() + " #" + marca.n()
                    + " of program #" + marca.idPrograma() + " could not be written", e);
        }
    }

    @Override
    public EstadoMarcas estado(long idPrograma) {
        List<MarcaGuarda> marcas = new ArrayList<>();
        int ilegibles = 0;
        try (Connection con = dataSource.getConnection(); PreparedStatement st = con.prepareStatement(LEER)) {
            st.setLong(1, idPrograma);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    var marca = MarcaGuarda.parsear(rs.getString(1));
                    if (marca.isPresent() && marca.get().idPrograma() == idPrograma) {
                        marcas.add(marca.get());
                    } else {
                        ilegibles++;
                    }
                }
            }
        } catch (SQLException | RuntimeException e) {
            throw new GuardaTomeeException("BAT.LogPrograma: the guard marks of program #" + idPrograma + " could not be read", e);
        }
        return EstadoMarcas.deMarcas(marcas, ilegibles);
    }
}
