package cl.bcs.cuentainversion.jobs.adapter;

import cl.bcs.cuentainversion.jobs.job.port.ConfiguracionJobs;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Production {@link ConfiguracionJobs}: the jobs only read the PLATAFORMA parameters ({@code PLA.FechaSistema}, {@code PLA.RutaBaseArchivos}),
 * kept in {@code PLA.Configuracion} as {@code (IdEmpresa, CodigoParametro, Valor)} (the legacy {@code ConfiguracionSistema} cache was off:
 * the day-end changes the system date while the jobs run, so it is read every time). {@code null} when the company has no such parameter; the
 * job then fails with the legacy message. ASSUMPTION (not proven against HANA): {@code Utils.getConfiguracion} of the legacy used the
 * procedure {@code BSCL.Plataforma.BD.Utilitarios::ObtenerConfiguracion}; its table is read here.
 */
@Component
public class ConfiguracionJobsJdbc implements ConfiguracionJobs {

    static final String SQL = "SELECT \"Valor\" FROM \"BSCL.Plataforma.BD::PLA.Configuracion\" WHERE \"IdEmpresa\" = ? AND \"CodigoParametro\" = ?";

    private final JdbcTemplate jdbc;

    public ConfiguracionJobsJdbc(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String parametro(Long idEmpresa, String modulo, String nombre) {
        if (!"PLATAFORMA".equals(modulo)) {
            throw new IllegalArgumentException("Configuracion modulo " + modulo + " no encontrada");
        }
        List<String> valores = jdbc.queryForList(SQL, String.class, idEmpresa, nombre);
        return valores.isEmpty() ? null : valores.get(valores.size() - 1);
    }
}
