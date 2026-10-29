package cl.bcs.cartolas.jobs.adapter.out;

import cl.bcs.cartolas.jobs.port.out.NominaPort;
import org.springframework.stereotype.Repository;

/**
 * Adaptador de salida — stub HANA para operaciones de nómina.
 *
 * Requiere conexión SAP HANA y SPs del paquete BSCL.Normativa.BD.Servicios::CAR.*
 * Todas las operaciones lanzan {@link UnsupportedOperationException} hasta que
 * se conecte con el ambiente HANA de QA.
 *
 * B-CAR-03: Nomina abrir/cerrar/poblar — requieren HANA
 *
 * @see NominaPort
 */
@Repository
public class HanaNominaAdapter implements NominaPort {

    @Override
    public boolean esCertificada(long idEmpresa, long jobId) {
        throw new UnsupportedOperationException(
                "requires HANA: BSCL.Normativa.BD.Servicios::CAR.NominaSelectCertificada");
    }

    @Override
    public void abrir(long idEmpresa, long jobId, String tipoProceso) {
        throw new UnsupportedOperationException(
                "requires HANA: BSCL.Normativa.BD.Servicios::CAR.NominaAbrir");
    }

    @Override
    public void cerrar(long idEmpresa, long jobId) {
        throw new UnsupportedOperationException(
                "requires HANA: BSCL.Normativa.BD.Servicios::CAR.NominaCerrar");
    }

    @Override
    public void cerrarConError(long idEmpresa, long jobId, Exception ex) {
        throw new UnsupportedOperationException(
                "requires HANA: BSCL.Normativa.BD.Servicios::CAR.NominaCerrarError");
    }

    @Override
    public void poblar(long idEmpresa, long jobId, long idNomina) {
        throw new UnsupportedOperationException(
                "requires HANA: BSCL.Normativa.BD.Servicios::CAR.NominaPoblar idNomina=" + idNomina);
    }

    @Override
    public long obtenerIdNomina(long idEmpresa, long jobId) {
        throw new UnsupportedOperationException(
                "requires HANA: BSCL.Normativa.BD.Servicios::CAR.NominaSelectActiva");
    }
}
