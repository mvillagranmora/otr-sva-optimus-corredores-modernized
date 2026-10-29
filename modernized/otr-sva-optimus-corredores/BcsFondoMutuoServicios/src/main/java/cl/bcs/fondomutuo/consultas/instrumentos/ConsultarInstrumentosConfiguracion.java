package cl.bcs.fondomutuo.consultas.instrumentos;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cl.bcs.fondomutuo.port.out.kit.DiccionarioMensajesPort;

/**
 * Spring wiring of {@link ConsultarInstrumentosPort}. Being wired does NOT mean being served: the JavaCall entry answers 503 unless
 * {@code fondomutuo.solicitud.listas-servidas} names {@link #LISTA} (closed by default). Read only: no lock, no Tesoreria, no custody, no gate G1..G5.
 */
@Configuration
public class ConsultarInstrumentosConfiguracion {

    /** Name in {@code fondomutuo.solicitud.listas-servidas} that opens the entry (closed when absent). */
    public static final String LISTA = "CONSULTAR_INSTRUMENTOS";
    /** Legacy FQN this module can answer. */
    public static final String FQN = "cl.bcs.fondomutuo.ConsultarInstrumentosFondosMutuos";

    @Bean
    public ConsultarInstrumentosPort consultarInstrumentosPort(DiccionarioMensajesPort diccionario, ConsultarInstrumentosDatos datos) {
        ConsultarInstrumentosFabrica.Dependencias d = new ConsultarInstrumentosFabrica.Dependencias();
        d.diccionario = diccionario;
        d.datos = datos;
        return ConsultarInstrumentosFabrica.crear(d);
    }
}
