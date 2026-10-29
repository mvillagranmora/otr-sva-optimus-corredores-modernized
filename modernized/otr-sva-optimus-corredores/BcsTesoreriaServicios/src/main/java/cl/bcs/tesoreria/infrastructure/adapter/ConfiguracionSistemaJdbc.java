package cl.bcs.tesoreria.infrastructure.adapter;

import java.util.Map;
import java.util.Objects;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import cl.bcs.tesoreria.javacall.TesoreriaJdbcHelper;
import cl.bcs.tesoreria.port.out.ConfiguracionSistema;

/**
 * Production {@link ConfiguracionSistema}: every module keeps its parameters in its own table with rows
 * {@code (IdEmpresa, CodigoParametro, Valor)} (the SQL of each module lives in {@code sql/ConfiguracionSistema_<MODULO>.sql}).
 *
 * <p>Not cached: the legacy cache was switched off because the day-end process changes {@code PLA.FechaSistema} while the services run.
 * Returns {@code null} when the company has no such parameter (the legacy raised "Parametro X en modulo Y no encontrado"; the caller decides,
 * see {@code cl.bcs.tesoreria.javacall.comun.Parametros}). An unknown module is a programming error and raises.
 */
@Component
public class ConfiguracionSistemaJdbc implements ConfiguracionSistema {

    private static final Map<String, String> CONSULTAS = Map.ofEntries(
            consulta("PLATAFORMA"), consulta("ORDENES"), consulta("CUENTAINVERSION"), consulta("TESORERIA"), consulta("FACTURACION"),
            consulta("SOCIODENEGOCIO"), consulta("CUSTODIA"), consulta("APV"), consulta("CONTABILIDAD"), consulta("FONDOMUTUO"),
            consulta("NORMATIVA"), consulta("MESA"), consulta("PRODUCTO"), consulta("LIMITE"));

    private final NamedParameterJdbcTemplate jdbc;

    public ConfiguracionSistemaJdbc(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static Map.Entry<String, String> consulta(String modulo) {
        return Map.entry(modulo, TesoreriaJdbcHelper.loadSql("sql/ConfiguracionSistema_" + modulo + ".sql"));
    }

    @Override
    public String parametro(Long idEmpresa, String modulo, String nombre) {
        String sql = CONSULTAS.get(modulo);
        if (sql == null) {
            throw new IllegalArgumentException("Configuracion modulo " + modulo + " no encontrada");
        }
        String[] valor = new String[1];
        jdbc.getJdbcOperations().query(sql, rs -> {
            if (Objects.equals(rs.getLong("IdEmpresa"), idEmpresa) && Objects.equals(rs.getString("CodigoParametro"), nombre)) {
                valor[0] = rs.getString("Valor");
            }
        });
        return valor[0];
    }
}
