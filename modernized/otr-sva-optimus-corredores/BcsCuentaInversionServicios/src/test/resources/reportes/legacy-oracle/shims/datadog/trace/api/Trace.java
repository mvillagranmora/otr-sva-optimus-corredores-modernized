package datadog.trace.api;

import java.lang.annotation.*;

/** ORACLE SHIM: Datadog tracing annotation used by the legacy platform classes. No behaviour. */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Trace {
}
