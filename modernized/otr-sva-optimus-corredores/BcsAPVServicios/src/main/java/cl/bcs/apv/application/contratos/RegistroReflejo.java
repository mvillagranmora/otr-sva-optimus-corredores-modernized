package cl.bcs.apv.application.contratos;

import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.util.Map;

/**
 * Copies a typed record into another record that has the same component names (the typed contract repeats the legacy POJOs per operation: the mass load of
 * contracts and the contract recording have their own {@code ParametrosContrato}), optionally replacing some components. The legacy mutated its input
 * objects ({@code paramcont.IdCargaMasiva = ...}); the records are immutable, so the copy carries the change.
 */
final class RegistroReflejo {

    private RegistroReflejo() {
    }

    static <R extends Record> R convertir(Record origen, Class<R> destino, Map<String, Object> cambios) {
        try {
            RecordComponent[] cd = destino.getRecordComponents();
            Class<?>[] tipos = new Class<?>[cd.length];
            Object[] valores = new Object[cd.length];
            for (int i = 0; i < cd.length; i++) {
                tipos[i] = cd[i].getType();
                if (cambios.containsKey(cd[i].getName())) {
                    valores[i] = cambios.get(cd[i].getName());
                } else {
                    RecordComponent co = null;
                    for (RecordComponent c : origen.getClass().getRecordComponents()) {
                        if (c.getName().equals(cd[i].getName())) {
                            co = c;
                        }
                    }
                    if (co == null) {
                        throw new IllegalStateException("component " + cd[i].getName() + " missing in " + origen.getClass().getName());
                    }
                    valores[i] = co.getAccessor().invoke(origen);
                }
            }
            Constructor<R> ctor = destino.getDeclaredConstructor(tipos);
            return ctor.newInstance(valores);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("cannot convert " + origen.getClass().getName() + " to " + destino.getName(), e);
        }
    }
}
