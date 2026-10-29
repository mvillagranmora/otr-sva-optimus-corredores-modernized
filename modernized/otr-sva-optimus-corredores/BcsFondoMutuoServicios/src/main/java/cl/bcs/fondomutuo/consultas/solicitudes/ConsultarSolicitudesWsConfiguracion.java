package cl.bcs.fondomutuo.consultas.solicitudes;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cl.bcs.fondomutuo.port.out.kit.ConfiguracionSistemaPort;

/**
 * Spring wiring of {@link ConsultarSolicitudesWsPort}. Being wired does NOT mean being served: the JavaCall entry answers 503 unless
 * {@code fondomutuo.solicitud.listas-servidas} names {@link #LISTA} (closed by default). Read only: no lock, no Tesoreria, no custody, no gate G1..G5.
 */
@Configuration
public class ConsultarSolicitudesWsConfiguracion {

    /** Name in {@code fondomutuo.solicitud.listas-servidas} that opens the entry (closed when absent). */
    public static final String LISTA = "CONSULTAR_SOLICITUDES_WS";
    /** Legacy FQN this module can answer. */
    public static final String FQN = "cl.bcs.fondomutuo.ConsultarSolicitudesFFMMWS";

    @Bean
    public ConsultarSolicitudesWsPort consultarSolicitudesWsPort(ConfiguracionSistemaPort configuracion, ConsultarSolicitudesWsDatos datos) {
        ConsultarSolicitudesWsFabrica.Dependencias d = new ConsultarSolicitudesWsFabrica.Dependencias();
        d.configuracion = configuracion;
        d.datos = datos;
        return ConsultarSolicitudesWsFabrica.crear(d);
    }
}
