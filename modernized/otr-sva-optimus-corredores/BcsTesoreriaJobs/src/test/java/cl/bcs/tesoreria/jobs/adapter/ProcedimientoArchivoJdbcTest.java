package cl.bcs.tesoreria.jobs.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import cl.bcs.tesoreria.jobs.job.port.ProcedimientoArchivo.ResultadoProcedimiento;

/**
 * {@link ProcedimientoArchivoJdbc} runs the stored procedure like the legacy: {@code prepareCall}, {@code setLong} / {@code setString} in
 * order, one {@code execute()}, every result set read with {@code getColumnName} / {@code getString}; no result set is an empty list and
 * a {@link SQLException} goes through unchanged.
 */
class ProcedimientoArchivoJdbcTest {

    private final DataSource datos = mock(DataSource.class);
    private final Connection con = mock(Connection.class);
    private final CallableStatement cs = mock(CallableStatement.class);

    private ResultSet resultado(List<String> columnas, List<List<String>> filas) throws SQLException {
        ResultSet rs = mock(ResultSet.class);
        ResultSetMetaData meta = mock(ResultSetMetaData.class);
        when(meta.getColumnCount()).thenReturn(columnas.size());
        when(meta.getColumnName(anyInt())).thenAnswer(i -> columnas.get(i.<Integer>getArgument(0) - 1));
        when(rs.getMetaData()).thenReturn(meta);
        int[] actual = {-1};
        when(rs.next()).thenAnswer(i -> ++actual[0] < filas.size());
        when(rs.getString(anyInt())).thenAnswer(i -> filas.get(actual[0]).get(i.<Integer>getArgument(0) - 1));
        return rs;
    }

    @Test
    void leeTodosLosResultSetsEnOrdenYBindeaLosParametros() throws Exception {
        when(datos.getConnection()).thenReturn(con);
        when(con.prepareCall("CALL BSCL.\"SP\"(?,?,?,?,?)")).thenReturn(cs);
        when(cs.execute()).thenReturn(true);
        ResultSet primero = resultado(List.of("TipoRegistroOptimus", "Rut"), List.of(List.of("1", "11-1")));
        ResultSet segundo = resultado(List.of("TipoRegistroOptimus", "Monto"),
                java.util.Arrays.asList(List.of("2", "1500.5"), java.util.Arrays.asList("2", null)));
        when(cs.getResultSet()).thenReturn(primero, segundo);
        when(cs.getMoreResults()).thenReturn(true, false);

        List<ResultadoProcedimiento> r = new ProcedimientoArchivoJdbc(datos)
                .ejecutar("CALL BSCL.\"SP\"(?,?,?,?,?)", List.of(1L, 8L, "CLP"));

        assertThat(r).hasSize(2);
        assertThat(r.get(0).columnas()).containsExactly("TipoRegistroOptimus", "Rut");
        assertThat(r.get(0).filas()).containsExactly(List.of("1", "11-1"));
        assertThat(r.get(1).filas()).hasSize(2);
        assertThat(r.get(1).filas().get(1)).containsExactly("2", null);
        InOrder orden = inOrder(cs);
        orden.verify(cs).setLong(1, 1L);
        orden.verify(cs).setLong(2, 8L);
        orden.verify(cs).setString(3, "CLP");
        orden.verify(cs).execute();
        verify(cs).close();
    }

    @Test
    void sinResultSetsEsUnaListaVacia() throws Exception {
        when(datos.getConnection()).thenReturn(con);
        when(con.prepareCall("CALL X()")).thenReturn(cs);
        when(cs.execute()).thenReturn(false);

        assertThat(new ProcedimientoArchivoJdbc(datos).ejecutar("CALL X()", List.of())).isEmpty();
    }

    @Test
    void laSqlExceptionPasaSinCambios() throws Exception {
        when(datos.getConnection()).thenReturn(con);
        when(con.prepareCall("CALL X()")).thenReturn(cs);
        SQLException falla = new SQLException("procedimiento caido");
        when(cs.execute()).thenThrow(falla);

        assertThatThrownBy(() -> new ProcedimientoArchivoJdbc(datos).ejecutar("CALL X()", List.of())).isSameAs(falla);
    }
}
