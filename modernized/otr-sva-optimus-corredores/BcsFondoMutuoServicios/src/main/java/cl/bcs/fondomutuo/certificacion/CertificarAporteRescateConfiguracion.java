package cl.bcs.fondomutuo.certificacion;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.adapter.out.jdbc.ConexionUnidadDeTrabajo;
import cl.bcs.fondomutuo.cierre.FormatoMoneda;
import cl.bcs.fondomutuo.cierre.SeriesFondoPort;
import cl.bcs.fondomutuo.comisiones.CalcularComisiones;
import cl.bcs.fondomutuo.comisiones.ComisionesPort;
import cl.bcs.fondomutuo.cuotas.CuotasPort;
import cl.bcs.fondomutuo.kit.Compuertas;
import cl.bcs.fondomutuo.kit.ServicioNoServidoException;
import cl.bcs.fondomutuo.normal.ProcesosExternos;
import cl.bcs.fondomutuo.port.out.http.CustodiaPort;
import cl.bcs.fondomutuo.port.out.http.FlujoCajaPort;
import cl.bcs.fondomutuo.port.out.http.LogPort;
import cl.bcs.fondomutuo.port.out.http.ParidadPort;
import cl.bcs.fondomutuo.port.out.http.PreciosTasasPort;
import cl.bcs.fondomutuo.port.out.kit.CalendarioPort;
import cl.bcs.fondomutuo.port.out.kit.ConfiguracionSistemaPort;
import cl.bcs.fondomutuo.port.out.kit.DiccionarioMensajesPort;
import cl.bcs.fondomutuo.port.out.kit.EmpresaPort;
import cl.bcs.fondomutuo.port.out.kit.RevisarRequisitosBasicosPort;
import cl.bcs.fondomutuo.requisitos.SecuenciaPort;

/**
 * Spring wiring of {@link CertificarAporteRescatePort} (legacy {@code CertificarSolicitudAporteRescate}). Being wired does NOT mean being served: the direct JavaCall
 * FQN answers 503 unless {@code fondomutuo.solicitud.listas-servidas} names {@link #LISTA} (closed by default; the user opens it after PROVEN and sign-off). Even open,
 * every front that leaves the module sits behind its own gate (G-CUS, G1, G2, G3, G6, G8).
 *
 * <p>The ports with no production implementation are wired here as replacements that FAIL CLOSED ({@link ServicioNoServidoException} before the argument is read),
 * {@code @ConditionalOnMissingBean}: the day a real adapter exists it takes their place without touching this class.
 */
@Configuration
public class CertificarAporteRescateConfiguracion {

    /** Name of the list in {@code fondomutuo.solicitud.listas-servidas}: not a list of the Manejador, the name of the direct JavaCall entry. */
    public static final String LISTA = "CERTIFICAR_SOLICITUD_APORTE_RESCATE";

    @Bean
    public CertificarAporteRescatePort certificarAporteRescatePort(RevisarRequisitosBasicosPort basicos, ConfiguracionSistemaPort configuracion,
            DiccionarioMensajesPort diccionario, CalendarioPort calendario, SeriesFondoPort series, CuotasPort cuotas, PreciosTasasPort precios,
            ParidadPort paridad, SecuenciaPort secuencias, ConexionUnidadDeTrabajo conexion, FlujoCajaPort flujoCaja, EmpresaPort empresas,
            ComisionesPort comisionesPort, CustodiaPort custodia, ProcesosExternos.TesoreriaPort tesoreria, ProcesosExternos.SpotPort spot,
            ProcesarCustodiaFondoMutuoPort procesarCustodia, CalculaCustodiaRescatePort calculaCustodia, CrearCargoAbonoPort crearCargoAbono,
            FacturacionPort facturacion, CrearAporteReinversionPort crearAporteReinversion, NotificacionCertificacionPort notificacionCertificacion,
            NotificacionOrdenesOwpPort notificacionOwp, LogPort log, Compuertas compuertas) {
        CertificarAporteRescateFabrica.Dependencias d = new CertificarAporteRescateFabrica.Dependencias();
        d.configuracion = configuracion;
        d.diccionario = diccionario;
        d.basicos = basicos;
        d.calendario = calendario;
        d.series = series;
        d.cuotas = cuotas;
        d.precios = precios;
        d.paridad = paridad;
        d.datos = new CertificarAporteRescateDatosJdbc(conexion);
        d.escritura = new CertificarAporteRescateEscrituraJdbc(conexion);
        d.secuencias = secuencias;
        d.flujoCaja = flujoCaja;
        d.procesarCustodia = procesarCustodia;
        d.calculaCustodia = calculaCustodia;
        d.comisiones = new CalcularComisiones(configuracion, empresas, series, comisionesPort, custodia, precios, paridad);
        d.formato = new FormatoMoneda(calendario);
        d.crearCargoAbono = crearCargoAbono;
        d.tesoreria = tesoreria;
        d.spot = spot;
        d.facturacion = facturacion;
        d.crearAporteReinversion = crearAporteReinversion;
        d.escrituraMonto = new CertificarAporteRescateEscrituraMontoJdbc(conexion);
        d.escrituraCierre = new CertificarAporteRescateEscrituraCierreJdbc(conexion);
        d.notificacionCertificacion = notificacionCertificacion;
        d.notificacionOwp = notificacionOwp;
        d.agregarLog = log;
        d.compuertas = compuertas;
        return CertificarAporteRescateFabrica.crear(d);
    }

    @Bean
    @ConditionalOnMissingBean(ProcesarCustodiaFondoMutuoPort.class)
    public ProcesarCustodiaFondoMutuoPort procesarCustodiaFondoMutuoNoServido() {
        return entrada -> {
            throw noServido("ProcesarCustodiaFondoMutuo");
        };
    }

    @Bean
    @ConditionalOnMissingBean(CalculaCustodiaRescatePort.class)
    public CalculaCustodiaRescatePort calculaCustodiaRescateNoServido() {
        return entrada -> {
            throw noServido("CalculaCustodiaRescate");
        };
    }

    @Bean
    @ConditionalOnMissingBean(CrearCargoAbonoPort.class)
    public CrearCargoAbonoPort crearCargoAbonoNoServido() {
        return entrada -> {
            throw noServido("CrearCargoAbono");
        };
    }

    @Bean
    @ConditionalOnMissingBean(FacturacionPort.class)
    public FacturacionPort facturacionNoServida() {
        return new FacturacionPort() {
            @Override
            public JsonObject crearMovimientoComprobante(JsonObject entrada) {
                throw noServido("Facturacion (CrearMovimientoComprobante)");
            }

            @Override
            public JsonObject crearComprobante(JsonObject entrada) {
                throw noServido("Facturacion (CrearComprobante)");
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean(NotificacionCertificacionPort.class)
    public NotificacionCertificacionPort notificacionCertificacionNoServida() {
        return entrada -> {
            throw noServido("NotificarEstadoOrden");
        };
    }

    @Bean
    @ConditionalOnMissingBean(NotificacionOrdenesOwpPort.class)
    public NotificacionOrdenesOwpPort notificacionOrdenesOwpNoServida() {
        return ordenes -> {
            throw noServido("NotificacionOrdenesOwp");
        };
    }

    private static ServicioNoServidoException noServido(String que) {
        return new ServicioNoServidoException("G0", que + " has no production adapter in this module yet");
    }
}
