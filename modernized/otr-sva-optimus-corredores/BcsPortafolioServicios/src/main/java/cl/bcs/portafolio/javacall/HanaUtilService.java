package cl.bcs.portafolio.javacall;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Spring Boot equivalents of legacy cl.bcs.utils.Utils and cl.bcs.plataforma.pla.utils.*
 * DB utility methods that require a live HANA connection.
 */
@Service
public class HanaUtilService {

    private static final Logger log = LoggerFactory.getLogger(HanaUtilService.class);

    // RULE-068: sequence names are HANA identifiers — cannot be parameterized; enforce whitelist.
    // Allowed: letters, digits, dots, colons, hyphens, underscores (HANA catalog naming convention).
    static final Pattern SAFE_SEQUENCE_NAME = Pattern.compile("[A-Za-z0-9_.:\\-]+");

    public static void validateSequenceName(String name) {
        if (name == null || !SAFE_SEQUENCE_NAME.matcher(name).matches()) {
            throw new IllegalArgumentException("Unsafe sequence name rejected: " + name);
        }
    }

    private static final String SQL_FECHA_SISTEMA =
        "SELECT \"Valor\" FROM \"BSCL.Plataforma.BD::PLA.ConfiguracionSistema\" " +
        "WHERE \"IdEmpresa\" = :IdEmpresa AND \"IdSistema\" = 'PLATAFORMA' AND \"IdParametro\" = 'PLA.FechaSistema'";

    private static final String SQL_CONFIG_PARAM =
        "SELECT \"Valor\" FROM \"BSCL.Plataforma.BD::PLA.ConfiguracionSistema\" " +
        "WHERE \"IdEmpresa\" = :IdEmpresa AND \"IdSistema\" = :IdSistema AND \"IdParametro\" = :IdParametro";

    private static final String SQL_REQUIERE_AUT =
        "SELECT \"RequiereAutorizacion\" FROM \"BSCL.Plataforma.BD::PLA.ConceptoAutorizacion\" " +
        "WHERE \"IdConcepto\" = :IdConcepto AND \"IdEmpresa\" = :IdEmpresa";

    private static final String SQL_SOC_REQUIERE_AUT =
        "SELECT TOP 1 \"RequiereAutorizacion\" " +
        "FROM \"BSCL.SocioNegocio.BD::SOC.ReglaNegocio\" " +
        "WHERE \"IdConcepto\" = :IdConcepto AND \"IdEmpresa\" = :IdEmpresa AND \"UsaReglasNegocio\" = :UsaReglasNegocio";

    /**
     * Equivalent of ConfiguracionSistema.getParametroConfiguracionString(conn, idEmpresa, "PLATAFORMA", "PLA.FechaSistema")
     */
    public String getFechaSistema(Connection conn, Long idEmpresa) {
        var jdbc = jdbc(conn);
        var params = new MapSqlParameterSource("IdEmpresa", idEmpresa);
        List<String> rows = jdbc.queryForList(SQL_FECHA_SISTEMA, params, String.class);
        return rows.isEmpty() ? null : rows.get(0);
    }

    /**
     * Equivalent of ConfiguracionSistema.getParametroConfiguracionString(conn, idEmpresa, idSistema, idParametro)
     */
    public String getConfigParam(Connection conn, Long idEmpresa, String idSistema, String idParametro) {
        var jdbc = jdbc(conn);
        var params = new MapSqlParameterSource()
            .addValue("IdEmpresa", idEmpresa)
            .addValue("IdSistema", idSistema)
            .addValue("IdParametro", idParametro);
        List<String> rows = jdbc.queryForList(SQL_CONFIG_PARAM, params, String.class);
        return rows.isEmpty() ? null : rows.get(0);
    }

    /**
     * Equivalent of Utils.getRequiereEventoAutorizacion(conn, idEmpresa, idConcepto)
     * Returns "S" if authorization is required, "N" otherwise.
     */
    public String getRequiereAutorizacion(Connection conn, Long idEmpresa, String idConcepto) {
        var jdbc = jdbc(conn);
        var params = new MapSqlParameterSource()
            .addValue("IdConcepto", idConcepto)
            .addValue("IdEmpresa", idEmpresa);
        List<String> rows = jdbc.queryForList(SQL_REQUIERE_AUT, params, String.class);
        return rows.isEmpty() ? "N" : rows.get(0);
    }

    /**
     * Equivalent of SocioNegocioUtils.esOperacionConAutorizacion(conn, idEmpresa, idConcepto, usaReglasNegocio)
     */
    public boolean esOperacionConAutorizacion(Connection conn, Long idEmpresa, String idConcepto,
                                               String usaReglasNegocio) {
        if ("N".equalsIgnoreCase(usaReglasNegocio)) return false;
        var jdbc = jdbc(conn);
        var params = new MapSqlParameterSource()
            .addValue("IdConcepto", idConcepto)
            .addValue("IdEmpresa", idEmpresa)
            .addValue("UsaReglasNegocio", usaReglasNegocio);
        List<String> rows = jdbc.queryForList(SQL_SOC_REQUIERE_AUT, params, String.class);
        return !rows.isEmpty() && "S".equalsIgnoreCase(rows.get(0));
    }

    /**
     * Equivalent of Utils.getSecuencia(conn, sequenceName, count)
     * Returns `count` NEXTVAL values from the named HANA sequence.
     */
    public List<Long> getSequence(Connection conn, String sequenceName, int count) {
        validateSequenceName(sequenceName);  // RULE-068: reject names containing SQL metacharacters
        var jdbc = jdbc(conn);
        String seqRef = "\"" + sequenceName + "\".NEXTVAL";
        List<Long> ids = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            Long next = jdbc.queryForObject(
                "SELECT " + seqRef + " FROM DUMMY",
                new MapSqlParameterSource(), Long.class);
            if (next != null) ids.add(next);
        }
        return ids;
    }

    private static NamedParameterJdbcTemplate jdbc(Connection conn) {
        return new NamedParameterJdbcTemplate(new SingleConnectionDataSource(conn, true));
    }
}
