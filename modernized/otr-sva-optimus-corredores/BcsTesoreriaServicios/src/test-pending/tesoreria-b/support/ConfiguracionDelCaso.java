package cl.bcs.tesoreria.familia_b.pending;

import java.util.Map;

import cl.bcs.tesoreria.familia_b.support.Caso;
import cl.bcs.tesoreria.port.out.ConfiguracionSistema;

/** PENDING test double: seed/config.json merged with the case "config" (keys "MODULO/Parametro"); Caso#config() already does the merge. */
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
