package cl.bcs.plataforma.adapter.in.javacall;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

/**
 * Allow-list of the JavaCall classes this module serves (D-4 / DEV-A1): ONLY the {@link JavaCallHandler} beans. Everything else (the 326
 * legacy stubs included, which are no longer in the Spring context) is "not served" and the controller answers 503 so the router falls
 * back to TomEE. Today the list has one class: {@code cl.bcs.plataforma.cre.ConsultarEstadoModulo}.
 */
@Component
public class JavaCallDispatcher {

    private final Map<String, JavaCallHandler> porFqn;

    public JavaCallDispatcher(List<JavaCallHandler> handlers) {
        this.porFqn = handlers.stream().collect(Collectors.toUnmodifiableMap(JavaCallHandler::fqn, Function.identity()));
    }

    public Optional<JavaCallHandler> buscar(String fqn) {
        return Optional.ofNullable(fqn == null ? null : porFqn.get(fqn));
    }

    public java.util.Set<String> servidas() {
        return porFqn.keySet();
    }
}
