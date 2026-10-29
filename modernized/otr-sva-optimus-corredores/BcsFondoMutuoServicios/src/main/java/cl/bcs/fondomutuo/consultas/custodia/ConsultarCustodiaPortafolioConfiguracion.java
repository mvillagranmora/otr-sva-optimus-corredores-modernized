package cl.bcs.fondomutuo.consultas.custodia;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cl.bcs.fondomutuo.cierre.SeriesFondoPort;
import cl.bcs.fondomutuo.cuotas.CuotasPort;
import cl.bcs.fondomutuo.port.out.http.ParidadPort;
import cl.bcs.fondomutuo.port.out.http.PreciosTasasPort;
import cl.bcs.fondomutuo.port.out.kit.CalendarioPort;
import cl.bcs.fondomutuo.port.out.kit.ConfiguracionSistemaPort;
import cl.bcs.fondomutuo.port.out.kit.EmpresaPort;

/**
 * Spring wiring of {@link ConsultarCustodiaPortafolioPort}. Being wired does NOT mean being served: the JavaCall entry answers 503 unless
 * {@code fondomutuo.solicitud.listas-servidas} names {@link #LISTA} (closed by default). Read only: no lock, no Tesoreria, no gate G1..G5. The custody calculation is the port
 * {@link ConsultarCustodiaPortafolioCustodiaPort}, whose default adapter is the in-process FFMM calculation (with {@code fondomutuo.custodia.ffmm.cerrada=true} the bean FAILS CLOSED: {@link ConsultarCustodiaPortafolioCustodiaCerrada}).
 */
@Configuration
public class ConsultarCustodiaPortafolioConfiguracion {

    /** Name in {@code fondomutuo.solicitud.listas-servidas} that opens the entry (closed when absent). */
    public static final String LISTA = "CONSULTAR_CUSTODIA_PORTAFOLIO";
    /** Legacy FQN this module can answer. */
    public static final String FQN = "cl.bcs.fondomutuo.ConsultarCustodiaPortafolioFondoMutuo";

    @Bean
    public ConsultarCustodiaPortafolioPort consultarCustodiaPortafolioPort(ConfiguracionSistemaPort configuracion, CalendarioPort calendario, SeriesFondoPort series,
            CuotasPort cuotasPort, ParidadPort paridad, PreciosTasasPort precios, EmpresaPort empresas, ConsultarCustodiaPortafolioCustodiaPort custodia,
            ConsultarCustodiaPortafolioDatos datos) {
        ConsultarCustodiaPortafolioFabrica.Dependencias d = new ConsultarCustodiaPortafolioFabrica.Dependencias();
        d.configuracion = configuracion;
        d.calendario = calendario;
        d.series = series;
        d.cuotasPort = cuotasPort;
        d.paridad = paridad;
        d.precios = precios;
        d.empresas = empresas;
        d.custodia = custodia;
        d.datos = datos;
        return ConsultarCustodiaPortafolioFabrica.crear(d);
    }
}
