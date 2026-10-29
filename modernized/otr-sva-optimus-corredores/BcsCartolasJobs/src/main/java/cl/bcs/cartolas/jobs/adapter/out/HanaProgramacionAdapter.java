package cl.bcs.cartolas.jobs.adapter.out;

import cl.bcs.cartolas.jobs.port.out.ProgramacionPort;
import org.springframework.stereotype.Repository;

/**
 * Adaptador de salida — stub HANA para el proceso programado de cartolas.
 *
 * B-CAR-07: Proceso programado — requiere HANA
 *
 * @see ProgramacionPort
 */
@Repository
public class HanaProgramacionAdapter implements ProgramacionPort {

    @Override
    public void procesarPendientes(long idEmpresa) {
        throw new UnsupportedOperationException(
                "requires HANA: BSCL.Normativa.BD.Servicios::CAR.* (ProcesoProgramacionCartolas)");
    }
}
