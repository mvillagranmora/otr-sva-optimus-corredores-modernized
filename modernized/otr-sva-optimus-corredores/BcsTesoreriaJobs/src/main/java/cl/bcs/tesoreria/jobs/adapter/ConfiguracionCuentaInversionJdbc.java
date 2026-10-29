package cl.bcs.tesoreria.jobs.adapter;

import cl.bcs.cuentainversion.port.out.ConfiguracionSistemaPort;
import cl.bcs.tesoreria.port.out.ConfiguracionSistema;

import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;

/**
 * {@link ConfiguracionSistemaPort} of the CuentaInversion services that run in process inside a Tesoreria job (step 5).
 *
 * <p>The library implementation ({@code ConfiguracionRestClient}) asks a platform configuration service over HTTP by key. Inside a job that
 * would be one more HTTP hop per parameter while the job transaction is open, plus a new required URL, and the legacy never did it: it read the
 * parameter with the job's own connection ({@code ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, modulo, nombre)}).
 * This adapter does the same through the {@link ConfiguracionSistema} of this module (JDBC on the same {@code DataSource}, so inside the job
 * transaction, not cached). The CuentaInversion port gives only the key, so the module is taken from its legacy prefix
 * ({@code PLA.} = PLATAFORMA, {@code CUENTAINVERSION.} = CUENTAINVERSION); any other prefix is a wiring bug and fails closed.
 *
 * <p>ASSUMPTION (not proven against HANA): the platform configuration service reads the same {@code PLA.Configuracion} / module tables.
 */
public class ConfiguracionCuentaInversionJdbc implements ConfiguracionSistemaPort {

    private static final Map<String, String> MODULOS = Map.of("PLA.", "PLATAFORMA", "CUENTAINVERSION.", "CUENTAINVERSION");

    private final ConfiguracionSistema configuracion;

    public ConfiguracionCuentaInversionJdbc(ConfiguracionSistema configuracion) {
        this.configuracion = Objects.requireNonNull(configuracion, "configuracion");
    }

    @Override
    public String getConfiguracion(Long idEmpresa, String clave) {
        for (Map.Entry<String, String> modulo : MODULOS.entrySet()) {
            if (clave != null && clave.startsWith(modulo.getKey())) {
                return configuracion.parametro(idEmpresa, modulo.getValue(), clave);
            }
        }
        throw new IllegalArgumentException("configuration key without a known module prefix: " + clave);
    }

    @Override
    public boolean usaCargoAbonoJava(Long idEmpresa) {
        return "S".equalsIgnoreCase(getConfiguracion(idEmpresa, "PLA.UsaCargoAbonoJava"));
    }

    @Override
    public boolean permiteAnularMovimiento(Long idEmpresa) {
        return "S".equalsIgnoreCase(getConfiguracion(idEmpresa, "CUENTAINVERSION.PERMITEANULARMOVIMIENTO"));
    }

    @Override
    public boolean validaFlujoCajaEnCertificacion(Long idEmpresa) {
        return "S".equalsIgnoreCase(getConfiguracion(idEmpresa, "CUENTAINVERSION.VALIDAFLUJOCAJAENCERTIFICACION"));
    }

    @Override
    public LocalDate getFechaGoLive(Long idEmpresa) {
        String crudo = getConfiguracion(idEmpresa, "PLA.FechaGoLive");
        return crudo != null ? LocalDate.parse(crudo.substring(0, 10)) : null;
    }
}
