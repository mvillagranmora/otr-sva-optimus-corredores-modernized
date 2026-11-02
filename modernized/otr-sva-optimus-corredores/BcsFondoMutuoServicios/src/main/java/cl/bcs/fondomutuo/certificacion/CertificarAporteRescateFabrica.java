package cl.bcs.fondomutuo.certificacion;

import java.util.function.UnaryOperator;

import cl.bcs.fondomutuo.cierre.FormatoMoneda;
import cl.bcs.fondomutuo.cierre.SeriesFondoPort;
import cl.bcs.fondomutuo.comisiones.CalcularComisiones;
import cl.bcs.fondomutuo.cuotas.CalcularMontoNumeroCuota;
import cl.bcs.fondomutuo.cuotas.CuotasPort;
import cl.bcs.fondomutuo.kit.Compuertas;
import cl.bcs.fondomutuo.normal.ProcesosExternos;
import cl.bcs.fondomutuo.port.out.http.FlujoCajaPort;
import cl.bcs.fondomutuo.port.out.http.LogPort;
import cl.bcs.fondomutuo.port.out.http.ParidadPort;
import cl.bcs.fondomutuo.port.out.http.PreciosTasasPort;
import cl.bcs.fondomutuo.port.out.kit.CalendarioPort;
import cl.bcs.fondomutuo.port.out.kit.ConfiguracionSistemaPort;
import cl.bcs.fondomutuo.port.out.kit.DiccionarioMensajesPort;
import cl.bcs.fondomutuo.port.out.kit.RevisarRequisitosBasicosPort;
import cl.bcs.fondomutuo.requisitos.SecuenciaPort;

/**
 * Builds the certification. With a seam ({@code posteriores} given) it builds only F-A and F-B (T1..T4: the tests hand the rest over); WITHOUT it, it builds the facade: every front A..J behind one
 * port (T5). Nothing here has state.
 */
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

        // T5: what the facade needs besides F-A and F-B (unused with a seam)
        public FlujoCajaPort flujoCaja;
        public ProcesarCustodiaFondoMutuoPort procesarCustodia;
        public CalculaCustodiaRescatePort calculaCustodia;
        public CalcularComisiones comisiones;
        public FormatoMoneda formato;
        public CrearCargoAbonoPort crearCargoAbono;
        public ProcesosExternos.TesoreriaPort tesoreria;
        public ProcesosExternos.SpotPort spot;
        public FacturacionPort facturacion;
        public CrearAporteReinversionPort crearAporteReinversion;
        public CertificarAporteRescateEscrituraMonto escrituraMonto;
        public CertificarAporteRescateEscrituraCierre escrituraCierre;
        public NotificacionCertificacionPort notificacionCertificacion;
        public NotificacionOrdenesOwpPort notificacionOwp;
        public LogPort agregarLog;
        public Compuertas compuertas;
        /** Applied to the guarded port of the recursion that each level hands to the switch unit: an extension point for observing the child (null in production, no logic behind it). */
        public UnaryOperator<CertificacionHijaPort> envolverHija;
    }

    public static CertificarAporteRescatePort crear(Dependencias d) {
        CalcularMontoNumeroCuota calculo = new CalcularMontoNumeroCuota(d.configuracion, d.series, d.cuotas, d.calendario, d.precios, d.paridad);
        if (d.posteriores != null) {
            return new CertificarAporteRescateService(d.configuracion, d.diccionario, d.basicos, d.precios, calculo, d.datos, d.escritura, d.secuencias, d.posteriores);
        }
        CertificarAporteRescateSalida salida = new CertificarAporteRescateSalida(d.diccionario, d.secuencias, d.escritura);
        return new CertificarAporteRescateFachada(
                posteriores -> new CertificarAporteRescateService(d.configuracion, d.diccionario, d.basicos, d.precios, calculo, d.datos, d.escritura, d.secuencias, posteriores),
                d.compuertas,
                new CertificarAporteRescateAportes(d.flujoCaja, d.paridad, d.procesarCustodia, d.datos, d.formato),
                new CertificarAporteRescateRescates(d.calculaCustodia, d.procesarCustodia, d.comisiones, d.paridad, d.formato, d.diccionario),
                new CertificarAporteRescateCuentaInversion(d.crearCargoAbono, d.datos),
                new CertificarAporteRescateTesoreria(d.tesoreria, d.configuracion),
                new CertificarAporteRescateFacturacion(d.facturacion, d.datos),
                new CertificarAporteRescateReinversion(d.crearAporteReinversion, d.datos, d.formato),
                new CertificarAporteRescateSpot(d.spot),
                new CertificarAporteRescateCierre(d.notificacionCertificacion, d.notificacionOwp, d.escrituraCierre, d.datos, d.agregarLog, salida),
                d.datos, d.escrituraMonto, d.envolverHija);
    }
}
