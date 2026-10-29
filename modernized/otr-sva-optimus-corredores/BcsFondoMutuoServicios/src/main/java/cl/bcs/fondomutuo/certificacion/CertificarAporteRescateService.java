package cl.bcs.fondomutuo.certificacion;

import cl.bcs.fondomutuo.cuotas.CalcularMontoNumeroCuota;
import cl.bcs.fondomutuo.port.out.http.PreciosTasasPort;
import cl.bcs.fondomutuo.port.out.kit.ConfiguracionSistemaPort;
import cl.bcs.fondomutuo.port.out.kit.DiccionarioMensajesPort;
import cl.bcs.fondomutuo.port.out.kit.RevisarRequisitosBasicosPort;
import cl.bcs.fondomutuo.requisitos.SecuenciaPort;

/* RED SKELETON (F3 W3, T1 CER): the compiling shape the characterization tests expect (API_ASSUMED.md). The behaviour is NOT written yet: every entry point
 * fails closed with UnsupportedOperationException, so the CER tests are red until Step 2 replaces this body. Not registered as a bean, no handler, no list. */
/** The facade: F-A, F-B and the seam. */
public class CertificarAporteRescateService implements CertificarAporteRescatePort {

    public CertificarAporteRescateService(ConfiguracionSistemaPort configuracion, DiccionarioMensajesPort diccionario, RevisarRequisitosBasicosPort basicos, PreciosTasasPort precios,
            CalcularMontoNumeroCuota calculoCuotas, CertificarAporteRescateDatos datos, CertificarAporteRescateEscritura escritura, SecuenciaPort secuencias,
            CertificarAporteRescateFasesPosteriores posteriores) {
    }

    @Override
    public CertificarAporteRescateContrato.Salida logica(CertificarAporteRescateContrato.Entrada entrada) throws Exception {
        throw new UnsupportedOperationException("CertificarAporteRescateService: not written yet (F3 W3 T1, Step 2)");
    }
}
