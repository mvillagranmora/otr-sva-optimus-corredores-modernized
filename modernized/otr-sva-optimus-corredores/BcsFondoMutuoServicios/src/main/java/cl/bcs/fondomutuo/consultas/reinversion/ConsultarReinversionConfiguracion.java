package cl.bcs.fondomutuo.consultas.reinversion;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring wiring of {@link ConsultarReinversionPort}. Being wired does NOT mean being served: the JavaCall entry answers 503 unless
 * {@code fondomutuo.solicitud.listas-servidas} names {@link #LISTA} (closed by default). Read only: no lock, no Tesoreria, no custody, no gate G1..G5.
 */
@Configuration
public class ConsultarReinversionConfiguracion {

    /** Name in {@code fondomutuo.solicitud.listas-servidas} that opens the entry (closed when absent). */
    public static final String LISTA = "CONSULTAR_FONDO_REINVERSION";
    /** Legacy FQN this module can answer. */
    public static final String FQN = "cl.bcs.fondomutuo.ConsultarFondoMutuoReinversion";

    @Bean
    public ConsultarReinversionPort consultarReinversionPort(ConsultarReinversionDatos datos) {
        return ConsultarReinversionFabrica.crear(datos);
    }
}
