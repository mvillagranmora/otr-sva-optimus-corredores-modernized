package cl.bcs.fondomutuo.certificacion;

import cl.bcs.fondomutuo.cierre.SeriesFondoPort;
import cl.bcs.fondomutuo.cuotas.CalcularMontoNumeroCuota;
import cl.bcs.fondomutuo.cuotas.CuotasPort;
import cl.bcs.fondomutuo.port.out.http.ParidadPort;
import cl.bcs.fondomutuo.port.out.http.PreciosTasasPort;
import cl.bcs.fondomutuo.port.out.kit.CalendarioPort;
import cl.bcs.fondomutuo.port.out.kit.ConfiguracionSistemaPort;
import cl.bcs.fondomutuo.port.out.kit.DiccionarioMensajesPort;
import cl.bcs.fondomutuo.port.out.kit.RevisarRequisitosBasicosPort;
import cl.bcs.fondomutuo.requisitos.SecuenciaPort;

/* RED SKELETON (F3 W3, T1 CER): the compiling shape the characterization tests expect (API_ASSUMED.md). The behaviour is NOT written yet: every entry point
 * fails closed with UnsupportedOperationException, so the CER tests are red until Step 2 replaces this body. Not registered as a bean, no handler, no list. */
public final class CertificarAporteRescateFabrica {

    private CertificarAporteRescateFabrica() {
    }

    public static final class Dependencias {
        public ConfiguracionSistemaPort configuracion;
        public DiccionarioMensajesPort diccionario;
        public RevisarRequisitosBasicosPort basicos;
        public CalendarioPort calendario;
        public SeriesFondoPort series;
        public CuotasPort cuotas;
        public PreciosTasasPort precios;
        public ParidadPort paridad;
        public CertificarAporteRescateDatos datos;
        public CertificarAporteRescateEscritura escritura;
        public SecuenciaPort secuencias;
        public CertificarAporteRescateFasesPosteriores posteriores;
    }

    public static CertificarAporteRescatePort crear(Dependencias d) {
        CalcularMontoNumeroCuota calculo = new CalcularMontoNumeroCuota(d.configuracion, d.series, d.cuotas, d.calendario, d.precios, d.paridad);
        return new CertificarAporteRescateService(d.configuracion, d.diccionario, d.basicos, d.precios, calculo, d.datos, d.escritura, d.secuencias, d.posteriores);
    }
}
