package cl.bcs.apv.infrastructure.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Types;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The adapter of the {@code StoredProcedure} collaborator reproduces the legacy {@code NamedCallableStatement} conventions. HANA is not available, so
 * this pins the parsing and the JDBC calls against a fake statement; it proves nothing about the procedures themselves.
 */
class ProcedimientoAlmacenadoTest {

    private static final String LEGACY_EDICION_MULTIPLE = """
            CALL "BSCL.APV.BD.Servicios::ActualizarFolioSuscripcion" (
                p_IdEmpresa => :p_IdEmpresa,
                p_IdModulo => :p_IdModulo, -- comment :ignored
                p_FormasDePago => #EdicionMultuple_P_FORMAPAGO,
                p_out_ErroresContador => @p_out_ErroresContador,
                p_out_FormaDePagoComprobante => $p_out_FormaDePagoComprobante,
                p_out_AdvertenciasContador => @p_out_AdvertenciasContador
            )""";

    @Test
    void elTextoLegadoSeConvierteEnMarcadoresConLaNumeracionDelLegado() {
        ProcedimientoAlmacenado.Llamada l = ProcedimientoAlmacenado.Llamada.analizar(LEGACY_EDICION_MULTIPLE);

        assertThat(l.entradas()).extracting(ProcedimientoAlmacenado.Marcador::nombre).containsExactly("p_IdEmpresa", "p_IdModulo");
        assertThat(l.entradas()).extracting(ProcedimientoAlmacenado.Marcador::indice).containsExactly(1, 2);
        // an output table takes a '?' in the statement but no position: the next scalar output keeps the next free number (legacy behaviour)
        assertThat(l.salidas()).extracting(ProcedimientoAlmacenado.Marcador::nombre).containsExactly("p_out_ErroresContador", "p_out_AdvertenciasContador");
        assertThat(l.salidas()).extracting(ProcedimientoAlmacenado.Marcador::indice).containsExactly(3, 4);
        assertThat(l.tablas()).containsExactly("p_out_FormaDePagoComprobante");
        assertThat(l.jdbc()).contains("\"BSCL.APV.BD.Servicios::ActualizarFolioSuscripcion\"").contains("#EdicionMultuple_P_FORMAPAGO").doesNotContain("--");
        assertThat(l.jdbc().chars().filter(c -> c == '?').count()).isEqualTo(5);
    }

    @Test
    void ejecutaElProcedimientoYDevuelveEscalaresYTablas() throws Exception {
        CallableStatement cs = mock(CallableStatement.class);
        when(cs.getString("p_out_Contador")).thenReturn("2");
        ResultSet rs = mock(ResultSet.class);
        ResultSetMetaData md = mock(ResultSetMetaData.class);
        when(md.getColumnCount()).thenReturn(2);
        when(md.getColumnLabel(1)).thenReturn("Id");
        when(md.getColumnLabel(2)).thenReturn("Texto");
        when(rs.getMetaData()).thenReturn(md);
        when(rs.next()).thenReturn(true, false);
        when(rs.getObject(1)).thenReturn(7L);
        when(rs.getObject(2)).thenReturn("a");
        when(cs.getResultSet()).thenReturn(rs);
        when(cs.getMoreResults()).thenReturn(false);
        Connection con = mock(Connection.class);
        when(con.prepareCall(any(String.class))).thenReturn(cs);
        JdbcTemplate jt = mock(JdbcTemplate.class);
        when(jt.execute(any(ConnectionCallback.class))).thenAnswer(i -> ((ConnectionCallback<?>) i.getArgument(0)).doInConnection(con));

        JsonObject peticion = JsonParser.parseString("""
                {"procedimiento": "Prueba", "entrada": {"p_Texto": "x", "p_Monto": 12.50}}""").getAsJsonObject();
        JsonObject salida = new ProcedimientoAlmacenado(new NamedParameterJdbcTemplate(jt)).ejecutar(peticion);

        InOrder orden = inOrder(cs);
        orden.verify(cs).setObject(1, "x");
        orden.verify(cs).setObject(2, new BigDecimal("12.50"));
        orden.verify(cs).setNull(3, Types.VARCHAR);
        verify(cs).registerOutParameter(4, Types.NVARCHAR);
        assertThat(salida.get("p_out_Contador").getAsString()).isEqualTo("2");
        assertThat(salida.getAsJsonArray("p_out_Detalle")).hasSize(1);
        assertThat(salida.getAsJsonArray("p_out_Detalle").get(0).getAsJsonObject().get("Id").getAsLong()).isEqualTo(7L);
        assertThat(salida.getAsJsonArray("p_out_Detalle").get(0).getAsJsonObject().get("Texto").getAsString()).isEqualTo("a");
        verify(cs, org.mockito.Mockito.never()).registerOutParameter(anyInt(), anyInt(), anyInt());
    }
}
