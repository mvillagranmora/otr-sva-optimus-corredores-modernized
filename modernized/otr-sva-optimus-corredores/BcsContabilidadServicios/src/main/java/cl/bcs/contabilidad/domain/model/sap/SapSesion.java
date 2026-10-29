package cl.bcs.contabilidad.domain.model.sap;

import java.util.List;

/**
 * Service Layer session: the {@code name=value} part of every {@code Set-Cookie} of the login (B1SESSION plus routing
 * cookies such as ROUTEID). Deviation from legacy CallerWSJournalVoucher.java:130-132, which kept only the LAST
 * cookie and so lost B1SESSION. {@link #toString()} never shows cookie values.
 */
public record SapSesion(List<String> cookies) {

    public static final String COOKIE_SESION = "B1SESSION";

    public SapSesion {
        cookies = List.copyOf(cookies);
    }

    public boolean tieneSesionB1() {
        return cookies.stream().anyMatch(c -> c.startsWith(COOKIE_SESION + "="));
    }

    /** Value of the {@code Cookie} request header; empty when there is no cookie. */
    public String cabeceraCookie() {
        return String.join("; ", cookies);
    }

    @Override
    public String toString() {
        return "SapSesion[cookies=" + cookies.stream()
                .map(c -> (c.contains("=") ? c.substring(0, c.indexOf('=')) : c) + "=<redacted>").toList() + "]";
    }
}
