package cl.bcs.fondomutuo.consultas.detalle;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cl.bcs.fondomutuo.cierre.SeriesFondoPort;
import cl.bcs.fondomutuo.cuotas.CuotasPort;
import cl.bcs.fondomutuo.port.out.http.ParidadPort;
import cl.bcs.fondomutuo.port.out.http.PreciosTasasPort;
import cl.bcs.fondomutuo.port.out.kit.CalendarioPort;
import cl.bcs.fondomutuo.port.out.kit.ConfiguracionSistemaPort;
import cl.bcs.fondomutuo.port.out.kit.DiccionarioMensajesPort;

/**
 * Spring wiring of {@link ConsultarDetallePort}. Being wired does NOT mean being served: the JavaCall entry answers 503 unless
 * {@code fondomutuo.solicitud.listas-servidas} names {@link #LISTA} (closed by default). Read only: no lock, no Tesoreria, no custody, no gate G1..G5. The
 * only collaborators outside the module are the read-only Plataforma ports {@code CalcularParidad} and {@code EntregarPreciosTasas} (through the valuation).
 */
@Configuration
public class ConsultarDetalleConfiguracion {

    /** Name in {@code fondomutuo.solicitud.listas-servidas} that opens the entry (closed when absent). */
    public static final String LISTA = "CONSULTAR_DETALLE_SOLICITUD";
    /** Legacy FQN this module can answer. */
    public static final String FQN = "cl.bcs.fondomutuo.ConsultarDetalleSolicitud";

    @Bean
    public ConsultarDetallePort consultarDetallePort(ConfiguracionSistemaPort configuracion, DiccionarioMensajesPort diccionario, CalendarioPort calendario,
            SeriesFondoPort series, CuotasPort cuotasPort, ParidadPort paridad, PreciosTasasPort precios, ConsultarDetalleDatos datos) {
        ConsultarDetalleFabrica.Dependencias d = new ConsultarDetalleFabrica.Dependencias();
        d.configuracion = configuracion;
        d.diccionario = diccionario;
        d.calendario = calendario;
        d.series = series;
        d.cuotasPort = cuotasPort;
        d.paridad = paridad;
        d.precios = precios;
        d.datos = datos;
        return ConsultarDetalleFabrica.crear(d);
    }
}
