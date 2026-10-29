package cl.bcs.fondomutuo.consultas.custodiaffmm;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cl.bcs.fondomutuo.consultas.custodia.ConsultarCustodiaPortafolioCustodiaCerrada;
import cl.bcs.fondomutuo.consultas.custodia.ConsultarCustodiaPortafolioCustodiaPort;
import cl.bcs.fondomutuo.port.out.kit.ConfiguracionSistemaPort;
import cl.bcs.fondomutuo.port.out.kit.EmpresaPort;

/**
 * Wiring of the in-process custody calculation ({@code ConsultarCustodiaPortafolioFFMM}). It is NOT a JavaCall entry (no list, no handler, no whitelist entry): its only caller is
 * CPF, through {@link ConsultarCustodiaPortafolioCustodiaPort}. There is ALWAYS exactly one bean of that port: the real adapter by default, or the closed one (503 G-CUS) when the
 * operator sets {@value #PROPIEDAD_CERRADA}{@code =true}. Read only.
 *
 * <p>VALUES (review of 2026-10-28, M2; user decision: documentation only, the code is unchanged): {@value #PROPIEDAD_CERRADA} accepts ONLY {@code true}, {@code false} or absent
 * (absent = false = open). Any other value (empty, {@code yes}, {@code 1}, a typo) matches neither bean, so NO {@code ConsultarCustodiaPortafolioCustodiaPort} exists and the
 * application does NOT start (0 beans): a typo cannot leave the calculation open or closed by accident. Mind the opposite polarity to
 * {@code fondomutuo.custodia.habilitada} ({@code true} OPENS the in-process custody; here {@code true} CLOSES the FFMM calculation).
 */
@Configuration
public class ConsultarCustodiaPortafolioFfmmConfiguracion {

    /** Operator switch: {@code true} closes the in-process custody calculation (the closed bean answers 503 G-CUS). */
    public static final String PROPIEDAD_CERRADA = "fondomutuo.custodia.ffmm.cerrada";

    @Bean
    public ConsultarCustodiaPortafolioFfmmPort consultarCustodiaPortafolioFfmmPort(ConfiguracionSistemaPort configuracion, EmpresaPort empresas,
            ConsultarCustodiaPortafolioFfmmDatos datos) {
        ConsultarCustodiaPortafolioFfmmFabrica.Dependencias d = new ConsultarCustodiaPortafolioFfmmFabrica.Dependencias();
        d.configuracion = configuracion;
        d.empresas = empresas;
        d.datos = datos;
        return ConsultarCustodiaPortafolioFfmmFabrica.crear(d);
    }

    @Bean
    @ConditionalOnProperty(name = PROPIEDAD_CERRADA, havingValue = "false", matchIfMissing = true)
    public ConsultarCustodiaPortafolioCustodiaPort consultarCustodiaPortafolioFfmmAdaptador(ConsultarCustodiaPortafolioFfmmPort servicio) {
        return new ConsultarCustodiaPortafolioFfmmAdaptador(servicio);
    }

    @Bean
    @ConditionalOnProperty(name = PROPIEDAD_CERRADA, havingValue = "true")
    public ConsultarCustodiaPortafolioCustodiaPort consultarCustodiaPortafolioCustodiaCerrada() {
        return new ConsultarCustodiaPortafolioCustodiaCerrada();
    }
}
