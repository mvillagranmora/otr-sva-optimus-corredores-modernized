package cl.bcs.contabilidad.infrastructure.sap;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * SAP B1 Service Layer settings (legacy interfazSAP.properties B1_SERVICE_LAYER_URL / B1_USER / B1_PASS /
 * B1_COMPANY and CallerWSJournalVoucher.java:29,:74). Values come from {@code sap.b1.*} properties or the
 * {@code SAP_B1_*} environment; a missing one only fails the login, never the application start.
 * {@link #toString()} never renders the password.
 */
public record SapB1Config(String serviceLayerUrl, String usuario, String clave, String companyDb, int maximoIntentos,
                          Duration esperaReintentoLogin) {

    public static final int MAXIMO_INTENTOS_DEFAULT = 10;
    public static final Duration ESPERA_REINTENTO_LOGIN_DEFAULT = Duration.ofSeconds(2);

    public SapB1Config {
        if (maximoIntentos < 1) throw new IllegalArgumentException("maximoIntentos must be >= 1");
        esperaReintentoLogin = esperaReintentoLogin == null ? ESPERA_REINTENTO_LOGIN_DEFAULT : esperaReintentoLogin;
        if (esperaReintentoLogin.isNegative()) throw new IllegalArgumentException("esperaReintentoLogin must not be negative");
        serviceLayerUrl = serviceLayerUrl == null ? null : serviceLayerUrl.strip().replaceAll("/+$", "");
    }

    /** Names of the settings that are missing or blank (empty when the client can log in). */
    public List<String> faltantes() {
        List<String> f = new ArrayList<>();
        if (blank(serviceLayerUrl)) f.add("sap.b1.url / SAP_B1_URL");
        if (blank(usuario)) f.add("sap.b1.user / SAP_B1_USER");
        if (blank(clave)) f.add("sap.b1.password / SAP_B1_PASSWORD");
        if (blank(companyDb)) f.add("sap.b1.company / SAP_B1_COMPANY");
        return f;
    }

    private static boolean blank(String s) {
        return s == null || s.isBlank();
    }

    @Override
    public String toString() {
        return "SapB1Config[serviceLayerUrl=" + serviceLayerUrl + ", usuario=" + usuario + ", clave=<redacted>, companyDb="
                + companyDb + ", maximoIntentos=" + maximoIntentos + ", esperaReintentoLogin=" + esperaReintentoLogin + "]";
    }
}
