package cl.bcs.apv.familia_b.support;

import java.util.TimeZone;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Runs a test class with the JVM default time zone fixed to UTC and restores it afterwards. The goldens were recorded with
 * {@code -Duser.timezone=UTC} (legacy-oracle/record.sh): the legacy utility {@code Fecha.addSeconds} / {@code Timestamp} arithmetic depends
 * on the JVM zone (DST shifts), so an unpinned zone would make some timestamps differ by one hour on other machines.
 */
public final class ZonaUtc implements BeforeAllCallback, AfterAllCallback {

    private static final ExtensionContext.Namespace NS = ExtensionContext.Namespace.create(ZonaUtc.class);

    @Override
    public void beforeAll(ExtensionContext context) {
        context.getStore(NS).put("zona", TimeZone.getDefault());
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @Override
    public void afterAll(ExtensionContext context) {
        TimeZone original = context.getStore(NS).get("zona", TimeZone.class);
        if (original != null) {
            TimeZone.setDefault(original);
        }
    }
}
