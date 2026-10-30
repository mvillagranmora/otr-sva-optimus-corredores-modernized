package cl.bcs.replica.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cl.bcs.replica.adapter.out.jdbc.RespaldoOptimusJdbc;
import cl.bcs.replica.adapter.out.jdbc.RespaldoSebraJdbc;
import cl.bcs.replica.adapter.out.jdbc.TriggersReplicaJdbc;
import cl.bcs.replica.envio.EnviarTablaReplicaServicio;
import cl.bcs.replica.inicio.IniciarReplicaDelta;
import cl.bcs.replica.inicio.IniciarReplicaFull;
import cl.bcs.replica.kit.Compuertas;
import cl.bcs.replica.port.out.ColaboradoresNoPortados;
import cl.bcs.replica.respaldo.ReplicaUtilsServicio;

/** Cableado por defecto: compuertas cerradas y colaboradores de Plataforma no portados (lanzan {@code ServicioNoServidoException}). */
@Configuration
public class ReplicaConfig {

    @Bean
    public ColaboradoresNoPortados colaboradoresNoPortados() {
        return new ColaboradoresNoPortados();
    }

    @Bean
    public Clock reloj() {
        return Clock.systemDefaultZone();
    }

    @Bean
    public EnviarTablaReplicaServicio enviarTablaReplicaServicio(Compuertas compuertas, ColaboradoresNoPortados c, Clock reloj) {
        return new EnviarTablaReplicaServicio(compuertas, c, c, c, c, new TriggersReplicaJdbc(), reloj);
    }

    @Bean
    public ReplicaUtilsServicio replicaUtilsServicio(Compuertas compuertas, ColaboradoresNoPortados c) {
        return new ReplicaUtilsServicio(compuertas, c, c, c, c, new RespaldoSebraJdbc(), new RespaldoOptimusJdbc());
    }

    @Bean
    public IniciarReplicaFull iniciarReplicaFull(ColaboradoresNoPortados c, Clock reloj) {
        return new IniciarReplicaFull(c, c, reloj);
    }

    @Bean
    public IniciarReplicaDelta iniciarReplicaDelta(ColaboradoresNoPortados c, Clock reloj) {
        return new IniciarReplicaDelta(c, c, reloj);
    }
}
