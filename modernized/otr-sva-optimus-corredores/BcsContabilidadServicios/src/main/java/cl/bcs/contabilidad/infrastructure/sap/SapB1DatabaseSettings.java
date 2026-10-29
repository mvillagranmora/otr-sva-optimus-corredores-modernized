package cl.bcs.contabilidad.infrastructure.sap;

import java.util.Map;

/**
 * JDBC settings of the SAP Business One database read by the daily interface.
 *
 * <p>Legacy: {@code PlataformaUtils.getDESAPEB1Datasource()} (platform-managed pool). The values now come only
 * from the environment: {@code SAP_B1_DB_URL}, {@code SAP_B1_DB_USER} and {@code SAP_B1_DB_PASSWORD}, all
 * required. {@link #toString()} never shows the password.
 */
public record SapB1DatabaseSettings(String url, String username, String password) {

    public static final String ENV_URL = "SAP_B1_DB_URL";
    public static final String ENV_USER = "SAP_B1_DB_USER";
    public static final String ENV_CLAVE = "SAP_B1_DB_PASSWORD";

    public static SapB1DatabaseSettings fromEnvironment(Map<String, String> env) {
        return new SapB1DatabaseSettings(requerida(env, ENV_URL), requerida(env, ENV_USER), requerida(env, ENV_CLAVE));
    }

    private static String requerida(Map<String, String> env, String nombre) {
        String valor = env.get(nombre);
        if (valor == null || valor.isBlank()) {
            throw new IllegalStateException("Missing required environment variable " + nombre);
        }
        return valor.trim();
    }

    @Override
    public String toString() {
        return "SapB1DatabaseSettings[url=" + url + ", username=" + username + ", password=<redacted>]";
    }
}
