package cl.bcs.contabilidad.domain.model;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Output message of a JavaCall service, legacy {@code cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida}.
 *
 * <p>JSON shape {@code {id, tipo, codigo, descripcion}}. Legacy resolved {@code descripcion} from the database
 * dictionary ({@code Diccionario.getMensaje(con, idEmpresa, codigo, valores)}); the dictionary is not part of this
 * module, so {@link #of} renders the code and its values ({@code "CODIGO {k=v, ...}"}) as a non-null description.
 */
public record MensajeSalida(Long id, String tipo, String codigo, String descripcion) {

    public static final String OK = "OK";
    public static final String ERROR = "ERROR";
    public static final String ADVERTENCIA = "ADVERTENCIA";
    public static final String AUTORIZACION = "AUTORIZACION";
    public static final String INFORMACION = "INFORMACION";

    /** A dictionary message: {@code codigo} plus the substitution values legacy passed to the dictionary. */
    public static MensajeSalida of(Long id, String tipo, String codigo, Map<String, String> valores) {
        String descripcion = valores == null || valores.isEmpty() ? codigo : codigo + " " + new TreeMap<>(valores);
        return new MensajeSalida(id, tipo, codigo, descripcion);
    }

    public static MensajeSalida of(Long id, String tipo, String codigo) {
        return of(id, tipo, codigo, Map.of());
    }

    public static MensajeSalida error(Long id, String codigo, Map<String, String> valores) {
        return of(id, ERROR, codigo, valores);
    }

    public static MensajeSalida advertencia(Long id, String codigo, Map<String, String> valores) {
        return of(id, ADVERTENCIA, codigo, valores);
    }

    public static MensajeSalida informacion(Long id, String codigo, Map<String, String> valores) {
        return of(id, INFORMACION, codigo, valores);
    }

    public boolean esError() {
        return ERROR.equals(tipo);
    }

    /** Legacy {@code MensajeSalida.getNumeroErrores}. */
    public static long contarErrores(List<MensajeSalida> mensajes) {
        return mensajes.stream().filter(MensajeSalida::esError).count();
    }
}
