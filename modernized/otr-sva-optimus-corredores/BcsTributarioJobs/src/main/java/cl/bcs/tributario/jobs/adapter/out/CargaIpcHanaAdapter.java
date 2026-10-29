package cl.bcs.tributario.jobs.adapter.out;

import cl.bcs.tributario.jobs.domain.CargaIpcResultado;
import cl.bcs.tributario.jobs.port.out.CargaIpcHanaPort;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * JDBC adapter for {@link CargaIpcHanaPort}.
 *
 * <p>The IPC load operation requires a live SAP HANA environment with the stored
 * procedure {@code BSCL.Normativa.BD.Servicios::TRI.CargarValorFactorIpc} installed.
 * When running against H2 (test profile) the method throws
 * {@link UnsupportedOperationException}.</p>
 *
 * <p>Production HANA call (reference, not runnable in H2):</p>
 * <pre>{@code
 *   // set schema
 *   conn.prepareStatement("set schema BSCL").execute();
 *   conn.createStatement().executeUpdate("SET TRANSACTION AUTOCOMMIT DDL OFF");
 *
 *   // prepare SP call — 10 placeholders per legacy signature
 *   CallableStatement sp = conn.prepareCall(
 *       "CALL \"BSCL.Normativa.BD.Servicios::TRI.CargarValorFactorIpc\"" +
 *       "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
 *
 *   sp.setLong(1, idEmpresa);
 *   sp.setLong(2, idUsuario);
 *   sp.setString(3, idModulo);
 *   sp.setString(4, simularOperacion);
 *   sp.setString(5, usaReglasNegocio);
 *   sp.registerOutParameter(6, Types.INTEGER);  // advertenciasContador
 *   sp.registerOutParameter(7, Types.INTEGER);  // erroresContador
 *   // params 8–10: reserved / NULL per legacy (not set explicitly)
 *
 *   sp.execute();
 *   // consume result sets: p_out_Resultado, advertencias list, errores list
 *   sp.getMoreResults(); sp.getMoreResults();
 *
 *   int advertencias = sp.getInt(6);
 *   int errores      = sp.getInt(7);  // NOTE: legacy checked sp.getObject(4) — bug
 *
 *   if (errores == 0) conn.commit(); else conn.rollback();
 *   return new CargaIpcResultado(advertencias, errores);
 * }</pre>
 */
@Repository
public class CargaIpcHanaAdapter implements CargaIpcHanaPort {

    /** HANA stored-procedure name (canonical identifier used in @Disabled annotations). */
    public static final String SP_NOMBRE =
            "BSCL.Normativa.BD.Servicios::TRI.CargarValorFactorIpc";

    @SuppressWarnings("unused")
    private final NamedParameterJdbcTemplate jdbc;

    public CargaIpcHanaAdapter(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * {@inheritDoc}
     *
     * @throws UnsupportedOperationException always — requires a live HANA environment.
     */
    @Override
    public CargaIpcResultado ejecutarCargarValorFactorIpc(
            long idEmpresa,
            long idUsuario,
            String idModulo,
            String simularOperacion,
            String usaReglasNegocio) {
        throw new UnsupportedOperationException("requires HANA SP: " + SP_NOMBRE);
    }
}
