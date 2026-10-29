package cl.bcs.cartolas.master.domain;

/**
 * Registro que representa un parámetro de nómina de cartola obtenido desde HANA.
 *
 * <p>Corresponde a una fila de {@code BSCL.Normativa.BD::CAR.Parametro}.</p>
 */
public record ParametroCartola(
        long idParametro,
        long idNomina,
        String nombre,
        String valor
) {
    public ParametroCartola {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("nombre de parámetro no puede ser vacío");
        }
    }
}
