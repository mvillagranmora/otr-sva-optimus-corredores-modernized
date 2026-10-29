package cl.bcs.custodia.jobs.adapter.out;

import cl.bcs.custodia.jobs.domain.InstrumentoPrecio;
import cl.bcs.custodia.jobs.domain.PosicionCustodia;
import cl.bcs.custodia.jobs.port.out.CustodiaJobsDataPort;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * JDBC adapter implementing CustodiaJobsDataPort.
 *
 * H2-compatible SQL (plain queries).
 * HANA stored-procedure methods throw UnsupportedOperationException with a clear message.
 */
@Repository
public class CustodiaJobsRepositoryAdapter implements CustodiaJobsDataPort {

    private final NamedParameterJdbcTemplate jdbc;

    public CustodiaJobsRepositoryAdapter(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // -----------------------------------------------------------------------
    // Plain-SQL methods (H2-testable)
    // -----------------------------------------------------------------------

    /**
     * Legacy: ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA", "PLA.FechaSistema")
     */
    @Override
    public String obtenerFechaSistema(Long idEmpresa) {
        var params = new MapSqlParameterSource()
                .addValue("idEmpresa", idEmpresa)
                .addValue("clave", "PLA.FechaSistema");
        return jdbc.queryForObject(
                "SELECT VALOR FROM CONFIGURACION_T WHERE ID_EMPRESA = :idEmpresa AND CLAVE = :clave",
                params,
                String.class);
    }

    /**
     * Legacy: RegistraPrecioInstrumento.java — SELECT_INSTRUMENTOS query
     */
    @Override
    public List<InstrumentoPrecio> consultarPreciosInstrumentos(Long idEmpresa, String fecha) {
        var params = new MapSqlParameterSource()
                .addValue("idEmpresa", idEmpresa)
                .addValue("fecha", fecha);
        return jdbc.query(
                "SELECT ID_INSTRUMENTO, CODIGO_NEMO, PRECIO, FECHA_PRECIO" +
                " FROM INSTRUMENTOS_PRECIO_T" +
                " WHERE ID_EMPRESA = :idEmpresa AND FECHA_PRECIO = :fecha",
                params,
                (rs, rowNum) -> new InstrumentoPrecio(
                        rs.getLong("ID_INSTRUMENTO"),
                        rs.getString("CODIGO_NEMO"),
                        rs.getBigDecimal("PRECIO"),
                        rs.getString("FECHA_PRECIO")));
    }

    /**
     * Legacy: RegistraPrecioInstrumento.java — INSERT_INSTRUMENTOS / DELETE_INSTRUMENTOS cycle
     */
    @Override
    public void actualizarPrecioInstrumento(Long idInstrumento, BigDecimal precio, String fecha) {
        var params = new MapSqlParameterSource()
                .addValue("idInstrumento", idInstrumento)
                .addValue("precio", precio)
                .addValue("fecha", fecha);
        jdbc.update(
                "UPDATE INSTRUMENTOS_PRECIO_T SET PRECIO = :precio" +
                " WHERE ID_INSTRUMENTO = :idInstrumento AND FECHA_PRECIO = :fecha",
                params);
    }

    /**
     * Legacy: GenerarResumenPosiciones.java — POSICION_CUSTODIA data portion
     */
    @Override
    public List<PosicionCustodia> consultarPosicionesCliente(Long idEmpresa, String fecha) {
        var params = new MapSqlParameterSource()
                .addValue("idEmpresa", idEmpresa)
                .addValue("fecha", fecha);
        return jdbc.query(
                "SELECT ID_CLIENTE, ID_MONEDA, CANTIDAD, VALOR_MERCADO" +
                " FROM POSICION_CUSTODIA_T" +
                " WHERE ID_EMPRESA = :idEmpresa AND FECHA_POSICION = :fecha",
                params,
                (rs, rowNum) -> new PosicionCustodia(
                        rs.getLong("ID_CLIENTE"),
                        rs.getString("ID_MONEDA"),
                        rs.getBigDecimal("CANTIDAD"),
                        rs.getBigDecimal("VALOR_MERCADO")));
    }

    // -----------------------------------------------------------------------
    // HANA stored-procedure methods — not H2-testable
    // -----------------------------------------------------------------------

    /** Legacy: VencimientoProductos.java — SP_PROCESAR_VENCIMIENTO_PRODUCTOS */
    @Override
    public void procesarVencimientoProductos(Long idEmpresa, String tipoProducto, String fecha) {
        throw new UnsupportedOperationException(
                "requires HANA SP: SP_PROCESAR_VENCIMIENTO_PRODUCTOS");
    }

    /** Legacy: PautaTipificarInstrumentos.java — TipificarInstrumentos (SP_TIPIFICAR_INSTRUMENTOS) */
    @Override
    public void tipificarInstrumentos(Long idEmpresa) {
        throw new UnsupportedOperationException(
                "requires HANA SP: SP_TIPIFICAR_INSTRUMENTOS");
    }

    /** Legacy: PautaVencimientoIF.java — EjecutarVencimiento (SP_EJECUTAR_VENCIMIENTO) */
    @Override
    public void ejecutarVencimiento(Long idEmpresa, String fecha) {
        throw new UnsupportedOperationException(
                "requires HANA SP: SP_EJECUTAR_VENCIMIENTO");
    }
}
