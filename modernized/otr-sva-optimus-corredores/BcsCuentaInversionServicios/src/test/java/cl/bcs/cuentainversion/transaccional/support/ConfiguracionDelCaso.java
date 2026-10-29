package cl.bcs.cuentainversion.transaccional.support;

import java.time.LocalDate;
import java.util.Map;

import cl.bcs.cuentainversion.port.out.ConfiguracionSistemaPort;

/**
 * {@link ConfiguracionSistemaPort} fed with the SAME parameters the legacy saw in the case ({@code seed/config.json} + case "config",
 * keys {@code MODULO/Parametro}). The modern code has no database access for configuration (it goes through this port), so the test
 * supplies the data; it computes nothing.
 *
 * <p>{@link #getFechaGoLive}: the legacy (ConsultarMovimientoCuentaInversion.java:204-212) falls back to 0001-01-01 when
 * PLA.FechaGoLive is missing or is not a date; this adapter does the same so the port contract is "always a date".
 */
public final class ConfiguracionDelCaso implements ConfiguracionSistemaPort {

    private final Map<String, String> config;

    public ConfiguracionDelCaso(Caso caso) {
        this.config = caso.config();
    }

    private String plataforma(String parametro) {
        return config.get("PLATAFORMA/" + parametro);
    }

    @Override
    public String getConfiguracion(Long idEmpresa, String clave) {
        for (Map.Entry<String, String> e : config.entrySet()) {
            if (e.getKey().endsWith("/" + clave)) {
                return e.getValue();
            }
        }
        return null;
    }

    @Override
    public boolean usaCargoAbonoJava(Long idEmpresa) {
        return "S".equals(plataforma("PLA.UsaCargoAbonoJava"));
    }

    @Override
    public boolean permiteAnularMovimiento(Long idEmpresa) {
        return "S".equals(config.get("CUENTAINVERSION/PERMITEANULARMOVIMIENTO"));
    }

    @Override
    public boolean validaFlujoCajaEnCertificacion(Long idEmpresa) {
        return "S".equals(config.get("CUENTAINVERSION/VALIDAFLUJOCAJAENCERTIFICACION"));
    }

    @Override
    public LocalDate getFechaGoLive(Long idEmpresa) {
        try {
            return LocalDate.parse(plataforma("PLA.FechaGoLive"));
        } catch (RuntimeException e) {
            return LocalDate.of(1, 1, 1);
        }
    }
}
