package cl.bcs.apv.familia_b.support;

import java.util.Map;

import cl.bcs.apv.domain.port.out.ConfiguracionSistema;

/** seed/config.json merged with the case "config" (keys "MODULO/Parametro"); {@link Caso#config()} does the merge. Null when not configured, as the port says. */
public class ConfiguracionDelCaso implements ConfiguracionSistema {
    private final Map<String, String> valores;

    public ConfiguracionDelCaso(Caso caso) {
        this.valores = caso.config();
    }

    @Override
    public String parametro(Long idEmpresa, String modulo, String nombre) {
        return valores.get(modulo + "/" + nombre);
    }
}
