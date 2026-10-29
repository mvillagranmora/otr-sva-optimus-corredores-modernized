package cl.bcs.mesa.adapter.out;

import cl.bcs.mesa.domain.MovimientoSpot;
import cl.bcs.mesa.domain.PosicionInicial;
import cl.bcs.mesa.port.out.MesaDataPort;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Repository
public class MesaRepositoryAdapter implements MesaDataPort {

    private final NamedParameterJdbcTemplate jdbc;

    public MesaRepositoryAdapter(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<MovimientoSpot> obtenerMovimientosSpot(String fechaOperacion) {
        throw new UnsupportedOperationException(
            "requires HANA SP: SP_OBTENER_MOVIMIENTOS_SPT");
    }

    @Override
    public List<PosicionInicial> obtenerPosicionesIniciales(String fechaOperacion) {
        String sql = "SELECT ID_MONEDA, POSICION_INICIAL, TIPO_CAMBIO_INICIAL, CIERRE_INGRESADO " +
                     "FROM POSICION_INICIAL_T WHERE FECHA_OPERACION = :fecha";
        return jdbc.query(sql, Map.of("fecha", fechaOperacion), this::mapPosicion);
    }

    private PosicionInicial mapPosicion(ResultSet rs, int row) throws SQLException {
        PosicionInicial p = new PosicionInicial();
        p.IdMoneda          = rs.getString("ID_MONEDA");
        p.PosicionInicial   = rs.getBigDecimal("POSICION_INICIAL");
        p.TipoCambioInicial = rs.getBigDecimal("TIPO_CAMBIO_INICIAL");
        p.CierreIngresado   = rs.getBigDecimal("CIERRE_INGRESADO");
        return p;
    }
}
