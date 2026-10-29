package cl.bcs.fondomutuo.custodia;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import cl.bcs.custodia.flujo.ConsultarFlujoCustodiaFlujo;
import cl.bcs.fondomutuo.adapter.out.jdbc.ConexionUnidadDeTrabajo;
import cl.bcs.fondomutuo.requisitos.SecuenciaPort;

/**
 * Registers the custody library IN PROCESS, and ONLY when {@code fondomutuo.custodia.habilitada=true} (closed by default). {@code cl.bcs.custodia} is never
 * component-scanned: exactly two beans are created here, {@link ConsultarFlujoCustodiaFlujo} (a plain class, no Spring annotation, no state) and its
 * adapter {@link FlujoCustodiaEnProceso}. The adapter is {@link Primary}: it is the effective {@code CustodiaPort} of every consumer (RescateReinversion,
 * and the closure classes that share the port), ahead of the HTTP adapter {@code ColaboradoresHttp}, which stays registered as the fall-back (with the
 * property false it is the only {@code CustodiaPort}, and G-CUS closed answers 503 as before). {@link CustodiaEnProcesoArranque} stops the start-up if the
 * property is true and the effective port is not this adapter.
 *
 * <p>Collaborators of the library, wired from FondoMutuo: {@code secuencias} = {@link SecuenciaPort#siguientes} (legacy {@code Utils.getSecuencia} on the
 * connection of the unit of work: the library asks for {@code BSCL.Custodia.BD.Secuencias::FlujoCustodia} and {@code ...::DatosFlujoCustodia});
 * {@code valorizador} = {@link FlujoCustodiaEnProceso#valorizadorCerrado()} (see the class comment of the adapter).
 *
 * <p>{@code fondomutuo.custodia.exigir-transaccion} (default true): the adapter requires an active read-write Spring transaction (M1 of the review of 2026-10-28; same
 * pattern as {@code fondomutuo.valorcuota.exigir-transaccion}). The scope of the three custody switches is in {@code RUNBOOK_LISTAS.md} 2.1.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "fondomutuo.custodia.habilitada", havingValue = "true")
public class FlujoCustodiaEnProcesoConfiguracion {

    /** Injected by Spring (default true); a plain {@code new} of this class (the existing harness) leaves it false: no guard, as before. */
    @Value("${fondomutuo.custodia.exigir-transaccion:true}")
    private boolean exigirTransaccion;

    @Bean
    public ConsultarFlujoCustodiaFlujo consultarFlujoCustodiaFlujo(SecuenciaPort secuencias) {
        return new ConsultarFlujoCustodiaFlujo(FlujoCustodiaEnProceso.valorizadorCerrado(), secuencias::siguientes);
    }

    @Bean
    @Primary
    public FlujoCustodiaEnProceso flujoCustodiaEnProceso(ConexionUnidadDeTrabajo conexion, ConsultarFlujoCustodiaFlujo flujo) {
        return new FlujoCustodiaEnProceso(conexion, flujo, exigirTransaccion);
    }
}
