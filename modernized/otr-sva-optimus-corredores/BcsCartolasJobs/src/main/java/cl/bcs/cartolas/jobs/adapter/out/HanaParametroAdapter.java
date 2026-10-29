package cl.bcs.cartolas.jobs.adapter.out;

import cl.bcs.cartolas.jobs.port.out.ParametroPort;
import org.springframework.stereotype.Repository;

import java.util.Map;

/**
 * Adaptador de salida — stub HANA para guardar parámetros.
 *
 * B-CAR-04: Guardar parámetros — requiere HANA
 *
 * @see ParametroPort
 */
@Repository
public class HanaParametroAdapter implements ParametroPort {

    @Override
    public void guardar(long idEmpresa, long jobId, Map<String, String> parametros) {
        throw new UnsupportedOperationException(
                "requires HANA: BSCL.Normativa.BD.Servicios::CAR.ParametroSave");
    }
}
