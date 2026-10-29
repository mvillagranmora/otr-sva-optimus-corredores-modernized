package cl.bcs.cuentainversion.reportes;

import java.util.HashMap;
import java.util.Map;

/**
 * Builds {@link MensajeSalida}s with the text of the platform dictionary ({@code PLA.Diccionario}, language of
 * {@code PLA.LocalizacionSistema} of the company), like the legacy {@code Diccionario.getMensaje}.
 *
 * <p>Rules kept from the legacy: the text of a code that is not in the dictionary is {@code "<code> sin traduccion"}; a
 * {@code null} parameter map also gives {@code "<code> sin traduccion"} (the legacy reads {@code valores == null} that way, CTA-B-13);
 * a {@code null} parameter value is replaced by the empty string; placeholders are written {@code ::Name::}.
 *
 * <p>Create one instance per request: the dictionary of the company is read once, lazily.
 */
public final class Mensajeria {

    private static final String SQL_DICCIONARIO = SqlReportes.recurso("plataforma/ConsultarDiccionario");
    private static final String SEPARADOR = "::";
    private static final String SIN_TRADUCCION = "sin traduccion";

    public static final class Registro {
        public String CodLenguaje;
        public String CodDiccionario;
        public String Descripcion;
    }

    private final SqlReportes sql;
    private final Long idEmpresa;
    private Map<String, String> textos;

    public Mensajeria(SqlReportes sql, Long idEmpresa) {
        this.sql = sql;
        this.idEmpresa = idEmpresa;
    }

    public MensajeSalida ok(String codigo, Map<String, String> valores) {
        return crear(MensajeSalida.OK, codigo, valores);
    }

    public MensajeSalida error(String codigo, Map<String, String> valores) {
        return crear(MensajeSalida.ERROR, codigo, valores);
    }

    public MensajeSalida advertencia(String codigo, Map<String, String> valores) {
        return crear(MensajeSalida.ADVERTENCIA, codigo, valores);
    }

    public MensajeSalida informacion(String codigo, Map<String, String> valores) {
        return crear(MensajeSalida.INFORMACION, codigo, valores);
    }

    public MensajeSalida ok(String codigo) {
        return ok(codigo, Map.of());
    }

    public MensajeSalida error(String codigo) {
        return error(codigo, Map.of());
    }

    public MensajeSalida advertencia(String codigo) {
        return advertencia(codigo, Map.of());
    }

    public MensajeSalida informacion(String codigo) {
        return informacion(codigo, Map.of());
    }

    public MensajeSalida crear(String tipo, String codigo, Map<String, String> valores) {
        return new MensajeSalida(0L, tipo, codigo, texto(codigo, valores));
    }

    /** Dictionary text of {@code codigo} with the placeholders replaced. */
    public String texto(String codigo, Map<String, String> valores) {
        String plantilla = diccionario().get(codigo);
        if (plantilla == null || valores == null) {
            return codigo + " " + SIN_TRADUCCION;
        }
        String mensaje = plantilla;
        for (Map.Entry<String, String> e : valores.entrySet()) {
            mensaje = mensaje.replace(SEPARADOR + e.getKey() + SEPARADOR, e.getValue() == null ? "" : e.getValue());
        }
        return mensaje;
    }

    private Map<String, String> diccionario() {
        if (textos == null) {
            Map<String, String> m = new HashMap<>();
            Map<String, Object> p = new HashMap<>();
            p.put("IdEmpresa", idEmpresa);
            p.put("CodigoParametro", "PLA.LocalizacionSistema");
            for (Registro r : sql.consultar(SQL_DICCIONARIO, p, Registro.class)) {
                m.put(r.CodDiccionario, r.Descripcion);
            }
            textos = m;
        }
        return textos;
    }
}
