package cl.bcs.jobmanager.support;

import cl.bcs.jobmanager.config.JobManagerProperties;
import cl.bcs.jobmanager.port.out.NodoPort;
import cl.bcs.jobmanager.port.out.ProgramaPort;
import cl.bcs.jobmanager.service.JobCallClient;
import cl.bcs.jobmanager.service.JobDispatcherService;
import cl.bcs.jobmanager.service.ProgramaDespachador;
import org.mockito.Mockito;

import java.util.List;

/** The dispatcher wired by hand (no Spring context) over the given port. */
public final class Stack {

    public final JobManagerProperties props = new JobManagerProperties();
    public final NodoPort nodos = Mockito.mock(NodoPort.class);
    /** Clock of the cooldown; replace before {@link #arrancar()} to control time. */
    public java.time.Clock reloj = java.time.Clock.systemUTC();
    public ProgramaDespachador despachador;
    public JobDispatcherService dispatcher;

    public Stack(ProgramaPort port, List<String> tipos) {
        props.setNode("");
        props.setTiposHabilitados(tipos);
        props.setInternalToken("secreto-de-prueba");
        props.setConnectTimeoutMs(500);
        props.setReadTimeoutMs(5_000);
        props.setShutdownWaitMs(2_000);
        this.port = port;
    }

    private final ProgramaPort port;

    /** Call after setting the properties. */
    public Stack arrancar() {
        despachador = new ProgramaDespachador(props, new JobCallClient(props), reloj);
        dispatcher = new JobDispatcherService(port, nodos, props, despachador);
        return this;
    }

    public void esperarFin() throws InterruptedException {
        long limite = System.currentTimeMillis() + 10_000;
        while (despachador.enCursoTotal() > 0 && System.currentTimeMillis() < limite) {
            Thread.sleep(10);
        }
    }
}
