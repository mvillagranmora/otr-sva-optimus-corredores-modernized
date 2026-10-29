package cl.bcs.apv.familia_b.support;

import com.google.gson.JsonObject;

import cl.bcs.apv.adapter.javacall.ApvJavaCallPipeline;
import cl.bcs.apv.support.ApvPipelineDeCaso;

/**
 * One equivalence comparison: loads the recorded case, runs the WHOLE modern path ({@code ApvPipelineDeCaso}: legacy JSON, adapter, real use case on the
 * case's H2 database, adapter, legacy JSON) inside the production transaction model (commit on return, rollback on throw) and compares output / exception
 * type / table dumps / collaborator trace / sequences with the golden recorded from the unmodified legacy. Counts the case as executed.
 */
public final class Equivalencia {

    private Equivalencia() {
    }

    public static void verificar(Caso.Contador contador, String nombre) {
        Caso caso = Caso.cargar(nombre);
        ColaboradoresDeCaso colaboradores = new ColaboradoresDeCaso(caso);
        SecuenciasDeCaso secuencias = new SecuenciasDeCaso(caso);
        ApvJavaCallPipeline pipeline = ApvPipelineDeCaso.crear(caso.jdbc(), new ConfiguracionDelCaso(caso), new DiccionarioDeCaso(), colaboradores,
                secuencias, new RelojDeCaso(), new AlmacenArchivosDeCaso());
        Verifica.Ejecucion r;
        try {
            JsonObject salida = pipeline.invocar("cl.bcs.APV." + caso.servicio(), caso.entrada());
            r = new Verifica.Ejecucion(salida, null);
        } catch (Throwable t) {
            r = new Verifica.Ejecucion(null, t);
        }
        caso.terminar(r.excepcion() == null);
        contador.ejecutado(nombre);
        Verifica.comparar(caso, r, colaboradores.traza(), secuencias.pedidas());
    }
}
