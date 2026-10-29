package cl.bcs.cartolas.master.domain;

/**
 * Registro que representa una configuración de cartola obtenida desde HANA.
 *
 * <p>Corresponde a una fila de {@code BSCL.Normativa.BD::CAR.Config}.</p>
 */
public record ConfiguracionCartola(
        long idConfig,
        long idEmpresa,
        String descripcion,
        String tipo,
        String nombre,
        String valor
) {
    public ConfiguracionCartola {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("nombre de configuración no puede ser vacío");
        }
    }
}
