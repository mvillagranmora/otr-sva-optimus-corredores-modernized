package cl.bcs.apv.application.recaudacion;

import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.List;

/**
 * Copies a typed record onto another record type with the same component names (the generated DTOs of two operations repeat the shared legacy POJOs:
 * for example {@code cargarcontratospac.ParametrosContrato} and {@code cargarcontratosmasivo.ParametrosContrato}). The legacy passed the SAME objects.
 */
final class RecordsCopia {

    private RecordsCopia() {
    }

    static <R> R copiar(Object origen, Class<R> destino) {
        if (origen == null) {
            return null;
        }
        try {
            RecordComponent[] comps = destino.getRecordComponents();
            Class<?>[] tipos = new Class<?>[comps.length];
            Object[] valores = new Object[comps.length];
            for (int i = 0; i < comps.length; i++) {
                tipos[i] = comps[i].getType();
                valores[i] = origen.getClass().getMethod(comps[i].getName()).invoke(origen);
            }
            Constructor<R> c = destino.getDeclaredConstructor(tipos);
            return c.newInstance(valores);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("cannot copy " + origen.getClass().getName() + " onto " + destino.getName(), e);
        }
    }

    static <R> List<R> copiar(List<?> origen, Class<R> destino) {
        if (origen == null) {
            return null;
        }
        List<R> r = new ArrayList<>();
        for (Object o : origen) {
            r.add(copiar(o, destino));
        }
        return r;
    }
}
