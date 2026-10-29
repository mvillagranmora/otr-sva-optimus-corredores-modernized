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
