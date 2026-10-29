package cl.bcs.contabilidad.infrastructure.plataforma;

import cl.bcs.contabilidad.domain.model.MensajeSalida;
import cl.bcs.contabilidad.domain.port.out.DiccionarioPort;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Default {@link DiccionarioPort}: the platform dictionary table is not part of this module, so the text is the code
 * and its values, the same rendering {@link MensajeSalida#of} uses across the module. Replace with a platform-backed
 * bean when the dictionary is migrated.
 */
@Component
public class CodigoDiccionarioAdapter implements DiccionarioPort {

    @Override
    public String mensaje(Long idEmpresa, String codigo, Map<String, String> valores) {
        return MensajeSalida.of(null, null, codigo, valores).descripcion();
    }
}
