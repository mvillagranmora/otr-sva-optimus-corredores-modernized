package cl.bcs.apv.familia_a.support;

import java.time.Instant;

import com.google.gson.JsonObject;

import cl.bcs.apv.adapter.javacall.ApvJavaCallPipeline;
import cl.bcs.apv.domain.port.out.AlmacenArchivos;
import cl.bcs.apv.domain.port.out.ConfiguracionSistema;
import cl.bcs.apv.domain.port.out.DiccionarioMensajes;
import cl.bcs.apv.domain.port.out.Reloj;
import cl.bcs.apv.domain.port.out.Secuencias;
import cl.bcs.apv.support.ApvPipelineDeCaso;

/**
 * The injectable ports of the pipeline under test, built from one case: configuration, dictionary, collaborators (with their call trace), sequences,
 * the clock and the file store. The clock is the fixed one of the recording (2025-04-01 10:15:30 UTC) unless the case says {@code "relojReal": true}
 * (the legacy reads the REAL wall clock there: AsignarContratoARecaudacion compares the month of a stored timestamp with the current month).
 * The file store fails the test when written: no family A class opens a file (static reading, CONTRATO.md section 7).
 */
public final class Dobles {

    public static final Instant AHORA = Instant.parse("2025-04-01T10:15:30Z");

    private final ConfiguracionSistema configuracion;
    private final DiccionarioMensajes diccionario;
    private final ColaboradoresDeCaso colaboradores;
    private final SecuenciasDeCaso secuencias;
    private final Reloj reloj;
    private final AlmacenArchivos archivos = (ruta, contenido) -> {
        throw new AssertionError("family A must not write files, it wrote " + ruta);
    };

    private Dobles(Caso caso) {
        this.configuracion = new ConfiguracionDelCaso(caso);
        this.diccionario = new DiccionarioDeCaso();
        this.colaboradores = new ColaboradoresDeCaso(caso);
        this.secuencias = new SecuenciasDeCaso(caso);
        this.reloj = caso.relojReal() ? Instant::now : () -> AHORA;
    }

    public static Dobles de(Caso caso) {
        return new Dobles(caso);
    }

    public ConfiguracionSistema configuracion() {
        return configuracion;
    }

    public DiccionarioMensajes diccionario() {
        return diccionario;
    }

    public ColaboradoresDeCaso colaboradores() {
        return colaboradores;
    }

    public Secuencias secuencias() {
        return secuencias;
    }

    public SecuenciasDeCaso secuenciasDeCaso() {
        return secuencias;
    }

    public Reloj reloj() {
        return reloj;
    }

    public AlmacenArchivos archivos() {
        return archivos;
    }

    /** The whole path legacy JSON -> adapter -> REAL use case on the H2 of the case -> adapter -> legacy JSON. */
    public ApvJavaCallPipeline pipeline(Caso caso) {
        return ApvPipelineDeCaso.crear(caso.jdbc(), configuracion, diccionario, colaboradores, secuencias, reloj, archivos);
    }

    /** Runs the case through the pipeline with the platform transaction model (commit on return, rollback on throw). */
    public JsonObject invocar(Caso caso) throws Exception {
        ApvJavaCallPipeline p = pipeline(caso);
        return caso.transaccion(() -> p.invocar("cl.bcs.APV." + caso.caso().get("servicio").getAsString(), caso.entrada().deepCopy()));
    }
}
