package cl.bcs.cuentainversion.port.out;

import java.util.List;

/** Database sequences (HANA {@code "name".NEXTVAL}): hands out the next values of a named sequence. */
public interface SecuenciasPort {

    /** The next {@code cantidad} values; an empty list when {@code cantidad} is null or not positive. */
    List<Long> siguientes(String nombre, Long cantidad);
}
