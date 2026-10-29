package cl.bcs.cartolas.jobs.adapter.out;

import cl.bcs.cartolas.jobs.port.out.CargaDatosPort;
import org.springframework.stereotype.Repository;

/**
 * Adaptador de salida — stub HANA para el proceso de carga de datos.
 *
 * B-CAR-05: Carga de datos — requiere HANA y SPs BSCL.Normativa.BD.Servicios::CAR.*
 *
 * @see CargaDatosPort
 */
@Repository
public class HanaCargaDatosAdapter implements CargaDatosPort {

    @Override
    public void cargar(long idEmpresa, long jobId) {
        throw new UnsupportedOperationException(
                "requires HANA: BSCL.Normativa.BD.Servicios::CAR.* (ProcesoCargaDatos)");
    }
}
