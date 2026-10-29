package cl.bcs.ordenes.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Local equivalent of cl.bcs.plataforma.pla.utils.ConfiguracionSistema.
 * Reads system configuration parameters from PLA.Configuracion table.
 */
@Component
public class ConfiguracionSistemaUtil {

    private static final Logger log = LoggerFactory.getLogger(ConfiguracionSistemaUtil.class);

    private static final String SQL =
        "SELECT \"IdEmpresa\",\"CodigoParametro\",\"Valor\" " +
        "FROM \"BSCL.Plataforma.BD::PLA.Configuracion\" " +
        "WHERE \"IdEmpresa\" = :IdEmpresa AND \"CodigoParametro\" = :CodigoParametro";

    private final NamedParameterJdbcTemplate jdbc;

    public ConfiguracionSistemaUtil(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public String getParametroString(Long idEmpresa, String codigoParametro) {
        try {
            MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("IdEmpresa", idEmpresa)
                .addValue("CodigoParametro", codigoParametro);
            List<Map<String, Object>> rows = jdbc.queryForList(SQL, params);
            if (rows.isEmpty()) {
                log.warn("Config param not found: empresa={}, param={}", idEmpresa, codigoParametro);
                return "";
            }
            Object val = rows.get(0).get("Valor");
            return val != null ? val.toString() : "";
        } catch (Exception e) {
            log.error("Error reading config param empresa={}, param={}: {}", idEmpresa, codigoParametro, e.getMessage());
            return "";
        }
    }

    public long getParametroLong(Long idEmpresa, String codigoParametro) {
        String val = getParametroString(idEmpresa, codigoParametro);
        try {
            return Long.parseLong(val.trim());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    public String getParam(Long idEmpresa, String modulo, String codigoParametro) {
        return getParametroString(idEmpresa, codigoParametro);
    }

    public String getParametro(long idEmpresa, String modulo, String codigoParametro) {
        return getParametroString(idEmpresa, codigoParametro);
    }
}
