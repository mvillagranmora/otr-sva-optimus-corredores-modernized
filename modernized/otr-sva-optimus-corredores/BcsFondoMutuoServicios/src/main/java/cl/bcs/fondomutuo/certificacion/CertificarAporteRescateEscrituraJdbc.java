package cl.bcs.fondomutuo.certificacion;

import java.util.List;

import cl.bcs.fondomutuo.adapter.out.jdbc.ConexionUnidadDeTrabajo;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.LogFila;

/* RED SKELETON (F3 W3, T1 CER): the compiling shape the characterization tests expect (API_ASSUMED.md). The behaviour is NOT written yet: every entry point
 * fails closed with UnsupportedOperationException, so the CER tests are red until Step 2 replaces this body. Not registered as a bean, no handler, no list. */
public class CertificarAporteRescateEscrituraJdbc implements CertificarAporteRescateEscritura {

    private final ConexionUnidadDeTrabajo conexion;

    public CertificarAporteRescateEscrituraJdbc(ConexionUnidadDeTrabajo conexion) {
        this.conexion = conexion;
    }

    @Override
    public void insertarLog(List<LogFila> filas) {
        throw new UnsupportedOperationException("CertificarAporteRescateEscrituraJdbc.insertarLog: not written yet (F3 W3 T1, Step 2)");
    }
}
