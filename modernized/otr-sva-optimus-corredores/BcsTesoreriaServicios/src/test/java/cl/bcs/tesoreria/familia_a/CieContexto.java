package cl.bcs.tesoreria.familia_a;

import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import cl.bcs.tesoreria.familia_a.support.Caso;
import cl.bcs.tesoreria.familia_a.support.Dobles;
import cl.bcs.tesoreria.port.out.Colaboradores;
import cl.bcs.tesoreria.port.out.ConfiguracionSistema;
import cl.bcs.tesoreria.port.out.DiccionarioMensajes;
import cl.bcs.tesoreria.port.out.Reloj;
import cl.bcs.tesoreria.port.out.Secuencias;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * Wires the services under test the way Spring does in production (constructor injection of the other services and of the ports), with the case
 * doubles as the ports. The tests of CrearIngresoEgreso, CrearTraspaso and LiquidarMovimientoAutomatico use it because those services call other
 * services in process (CrearComprobante, LiquidarComprobante, CrearIngresoEgreso) whose constructors belong to other classes.
 */
final class CieContexto {

    private CieContexto() {
    }

    /** A context holding {@code servicios} and the ports of the case; close it when the case is done. */
    static AnnotationConfigApplicationContext de(Caso caso, Dobles d, Class<?>... servicios) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.registerBean(NamedParameterJdbcTemplate.class, caso::jdbc);
        ctx.registerBean(ConfiguracionSistema.class, d::configuracion);
        ctx.registerBean(DiccionarioMensajes.class, d::diccionario);
        ctx.registerBean(Secuencias.class, d::secuencias);
        ctx.registerBean(Reloj.class, d::reloj);
        ctx.registerBean(Colaboradores.class, d::colaboradores);
        Set<Class<?>> registrados = new LinkedHashSet<>();
        for (Class<?> s : servicios) {
            registrar(registrados, s);
        }
        registrados.forEach(ctx::register);
        ctx.refresh();
        return ctx;
    }

    /** The service and, recursively, every service of this module its constructor asks for. */
    private static void registrar(Set<Class<?>> registrados, Class<?> tipo) {
        if (!registrados.add(tipo)) {
            return;
        }
        for (Class<?> parametro : tipo.getConstructors()[0].getParameterTypes()) {
            if (!parametro.isInterface() && parametro.getPackageName().startsWith("cl.bcs.tesoreria.javacall")) {
                registrar(registrados, parametro);
            }
        }
    }
}
