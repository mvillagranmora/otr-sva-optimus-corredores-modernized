package cl.bcs.cuentainversion.jobs.guarda;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The guard state of ONE run of a program: who it is (id, attempt, type) and what its guarded calls did so far. It lives in
 * {@link ContextoGuarda} for the thread that runs the job (the job and every collaborator call of a run share that thread; the module starts
 * no other thread).
 */
public final class CorridaGuardada {

    private final long idPrograma;
    private final int intento;
    private final String idTipoPrograma;
    private final AtomicInteger ordinal = new AtomicInteger();
    private final List<MarcaGuarda> hechas = Collections.synchronizedList(new ArrayList<>());
    private final List<MarcaGuarda> falladas = Collections.synchronizedList(new ArrayList<>());

    public CorridaGuardada(long idPrograma, int intento, String idTipoPrograma) {
        this.idPrograma = idPrograma;
        this.intento = intento;
        this.idTipoPrograma = idTipoPrograma;
    }

    public long idPrograma() {
        return idPrograma;
    }

    public int intento() {
        return intento;
    }

    public String idTipoPrograma() {
        return idTipoPrograma;
    }

    /** The INTENTO mark of the next guarded call of this run (n = 1, 2, ...). */
    MarcaGuarda siguiente(String clase) {
        return new MarcaGuarda(MarcaGuarda.Fase.INTENTO, idPrograma, intento, clase, ordinal.incrementAndGet());
    }

    void hecha(MarcaGuarda intentoMarca) {
        hechas.add(intentoMarca);
    }

    void fallada(MarcaGuarda intentoMarca) {
        falladas.add(intentoMarca);
    }

    /** Calls of this run that answered 2xx: their TomEE write is committed. */
    public List<MarcaGuarda> hechas() {
        synchronized (hechas) {
            return List.copyOf(hechas);
        }
    }

    /** Calls of this run that failed: their TomEE outcome is not certain (a timeout or a 5xx can follow a commit). */
    public List<MarcaGuarda> falladas() {
        synchronized (falladas) {
            return List.copyOf(falladas);
        }
    }
}
