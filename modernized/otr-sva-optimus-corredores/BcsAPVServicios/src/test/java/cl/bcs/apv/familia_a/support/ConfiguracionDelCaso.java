package cl.bcs.apv.familia_a.support;

import java.util.Map;

import cl.bcs.apv.domain.port.out.ConfiguracionSistema;

/** Configuration of the case: seed/config.json merged with case.json "config" (key "MODULO/Parametro"); null when the parameter does not exist. */
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
