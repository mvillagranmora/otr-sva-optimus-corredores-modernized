package cl.bcs.fondomutuo.consultas.dolar;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cl.bcs.fondomutuo.port.out.http.ParidadPort;
import cl.bcs.fondomutuo.port.out.kit.ConfiguracionSistemaPort;

/**
 * Spring wiring of {@link ConsultarDolarPort}. Being wired does NOT mean being served: the JavaCall entry answers 503 unless
 * {@code fondomutuo.solicitud.listas-servidas} names {@link #LISTA} (closed by default). Read only: no lock, no Tesoreria, no custody, no gate G1..G5.
 */
@Configuration
public class ConsultarDolarConfiguracion {

    /** Name in {@code fondomutuo.solicitud.listas-servidas} that opens the entry (closed when absent). */
    public static final String LISTA = "CONSULTAR_VALOR_DOLAR";
    /** Legacy FQN this module can answer. */
    public static final String FQN = "cl.bcs.fondomutuo.ConsultarValorDolarFondoMutuo";

    @Bean
    public ConsultarDolarPort consultarDolarPort(ConfiguracionSistemaPort configuracion, ParidadPort paridad, ConsultarDolarDatos datos) {
        ConsultarDolarFabrica.Dependencias d = new ConsultarDolarFabrica.Dependencias();
        d.configuracion = configuracion;
        d.paridad = paridad;
        d.datos = datos;
        return ConsultarDolarFabrica.crear(d);
    }
}
