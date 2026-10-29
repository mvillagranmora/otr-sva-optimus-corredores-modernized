package cl.bcs.portalconfirmacion.adapter.out;

import cl.bcs.portalconfirmacion.port.out.NotificationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * JDBC adapter for NotificationPort.
 *
 * H2-compatible methods (SQL SELECT / UPDATE) use simplified table/column names
 * that map to the test schema (schema-portal-confirmacion.sql).
 * For HANA production deployment, replace table names with their fully-qualified
 * HANA equivalents (e.g. "BSCL"."BSCL.Plataforma.BD::EVT.TagGenerales").
 *
 * HANA stored-procedure methods (validateToken, validateTokenAndId) throw
 * UnsupportedOperationException — they require SAP HANA's CallableStatement
 * extension and are @Disabled in integration tests.
 */
@Repository
public class NotificationJdbcAdapter implements NotificationPort {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationJdbcAdapter.class);

    private final NamedParameterJdbcTemplate jdbc;

    public NotificationJdbcAdapter(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ----------------------------------------------------------
    // HANA stored-procedure operations (not H2-compatible)
    // ----------------------------------------------------------

    @Override
    public long validateToken(String token) {
        throw new UnsupportedOperationException(
                "requires HANA: EVT.ValidarTokenNotificacion stored procedure");
    }

    @Override
    public String validateTokenAndId(String token, String rut) {
        throw new UnsupportedOperationException(
                "requires HANA: EVT.ValidarConfirmarNotificacion stored procedure");
    }

    // ----------------------------------------------------------
    // H2-compatible SQL operations
    // ----------------------------------------------------------

    @Override
    public String getUrlHana(long idEmpresa) {
        /*
         * HANA original:
         *   Select "Valor" FROM "BSCL"."BSCL.Plataforma.BD::PLA.Configuracion"
         *   where "CodigoParametro" = 'PLA.UrlHana' AND "IdEmpresa" = ?
         * H2-compatible equivalent:
         */
        String sql = """
                SELECT VALOR
                FROM PLA_CONFIGURACION
                WHERE CODIGO_PARAMETRO = 'PLA.UrlHana'
                  AND ID_EMPRESA = :idEmpresa
                """;
        var params = new MapSqlParameterSource("idEmpresa", idEmpresa);
        try {
            return jdbc.queryForObject(sql, params, String.class);
        } catch (EmptyResultDataAccessException e) {
            LOG.warn("No URL found for empresa {}", idEmpresa);
            return null;
        }
    }

    @Override
    public String getConfirmationMacro() {
        /*
         * HANA original:
         *   select "ValorDefault" from "BSCL.Plataforma.BD::EVT.TagGenerales"
         *   where "Llave" = 'Confirmar'
         * H2-compatible equivalent:
         */
        String sql = """
                SELECT VALOR_DEFAULT
                FROM EVT_TAG_GENERALES
                WHERE LLAVE = 'Confirmar'
                """;
        try {
            String result = jdbc.queryForObject(sql, new MapSqlParameterSource(), String.class);
            return result != null ? result : "";
        } catch (EmptyResultDataAccessException e) {
            LOG.warn("No confirmation macro found in EVT_TAG_GENERALES");
            return "";
        }
    }

    @Override
    public int acceptOrReject(String token, String observacion, String accionCode) {
        /*
         * HANA original:
         *   UPDATE "BSCL"."BSCL.Plataforma.BD::EVT.Notificacion"
         *   SET "Observacion" = ?, "Confirmado"= 'S', "AccionConfirmada" = ?,
         *       "FechaConfirmado"= CURRENT_UTCTIMESTAMP
         *   WHERE "TokenConfirmacion" = ?
         * H2-compatible equivalent (CURRENT_UTCTIMESTAMP → CURRENT_TIMESTAMP):
         */
        String sql = """
                UPDATE EVT_NOTIFICACION
                SET OBSERVACION      = :observacion,
                    CONFIRMADO       = 'S',
                    ACCION_CONFIRMADA = :accionCode,
                    FECHA_CONFIRMADO  = CURRENT_TIMESTAMP
                WHERE TOKEN_CONFIRMACION = :token
                """;
        var params = new MapSqlParameterSource()
                .addValue("observacion", observacion)
                .addValue("accionCode", accionCode)
                .addValue("token", token);
        try {
            int updated = jdbc.update(sql, params);
            return updated > 0 ? 1 : -1;
        } catch (Exception e) {
            LOG.error("Error in acceptOrReject for token {}", token, e);
            return -1;
        }
    }
}
