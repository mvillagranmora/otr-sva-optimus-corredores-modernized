package cl.bcs.cartolas.jobs.adapter.out;

import cl.bcs.cartolas.jobs.port.out.EmisionPort;
import org.springframework.stereotype.Repository;

/**
 * Adaptador de salida — stub HANA para el proceso de emisión de cartolas.
 *
 * B-CAR-06: Emisión de cartolas — requiere HANA, SPs y Crystal Reports (via BcsCrystalReports)
 *
 * @see EmisionPort
 */
@Repository
public class HanaEmisionAdapter implements EmisionPort {

    @Override
    public void emitir(long idEmpresa, long jobId) {
        throw new UnsupportedOperationException(
                "requires HANA: BSCL.Normativa.BD.Servicios::CAR.* (ProcesoEmisionMaster) "
                + "+ Crystal Reports via BcsCrystalReports service");
    }
}
