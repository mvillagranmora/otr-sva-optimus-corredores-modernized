package cl.bcs.fondomutuo.comisionnn;

import java.lang.reflect.Field;

/**
 * Legacy {@code Utils.copyPojo(src, target)} (the 2-argument form, {@code restrictDate = true}) for the immutable values of these pojos: every PUBLIC
 * field of the target is filled from the public field of the source with the same name AND the same type (a null stays null); fields whose type differs
 * (e.g. a {@code Date} against a {@code String}) are left as they are. Nested lists are not used by the pojos of this class.
 */
public final class Pojos {

    private Pojos() {
    }

    public static <K, T> T copiar(K origen, T destino) {
        for (Field campo : destino.getClass().getFields()) {
            try {
                Field campoOrigen = origen.getClass().getField(campo.getName());
                if (campo.getType().getName().equals(campoOrigen.getType().getName())) {
                    campo.set(destino, campoOrigen.get(origen));
                }
            } catch (NoSuchFieldException e) {
                // legacy: no field of that name in the source, nothing to copy
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }
        return destino;
    }
}
