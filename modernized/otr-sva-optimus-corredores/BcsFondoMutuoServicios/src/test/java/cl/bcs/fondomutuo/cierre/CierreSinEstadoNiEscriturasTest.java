package cl.bcs.fondomutuo.cierre;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.google.gson.Gson;

import cl.bcs.fondomutuo.adapter.out.jdbc.SqlRecursos;

/**
 * DEV-F2 (no static state of the request) and "reads only" for the step-3A classes. The legacy kept JVM-wide statics (the date offset of {@code Fecha},
 * the {@code MonedaFormato} table, the {@code ConfiguracionSistema} map); the new classes hold only final constants: a static field that is not a final
 * immutable constant (or the thread-safe {@code Gson}) fails this test. And every SQL they load is a SELECT: they write nothing.
 */
class CierreSinEstadoNiEscriturasTest {

    private static final String[] PAQUETES = { "cierre", "fechas", "cuotas", "comisiones", "perfil" };
    private static final Set<Class<?>> CONSTANTES_PERMITIDAS = Set.of(String.class, java.time.format.DateTimeFormatter.class, Gson.class, int.class, long.class, boolean.class);

    @Test
    @DisplayName("DEV-F2: ningun campo estatico mutable en cierre, fechas, cuotas, comisiones, perfil")
    void sinEstadoEstatico() throws IOException, ClassNotFoundException {
        List<String> culpables = new ArrayList<>();
        int clases = 0;
        for (String paquete : PAQUETES) {
            for (Resource r : new PathMatchingResourcePatternResolver().getResources("classpath*:cl/bcs/fondomutuo/" + paquete + "/**/*.class")) {
                String ruta = r.getURL().toString();
                if (ruta.contains("/test-classes/")) {
                    continue;
                }
                String nombre = ruta.substring(ruta.indexOf("cl/bcs/fondomutuo/"), ruta.length() - ".class".length()).replace('/', '.');
                Class<?> c = Class.forName(nombre, false, getClass().getClassLoader());
                clases++;
                for (Field f : c.getDeclaredFields()) {
                    if (Modifier.isStatic(f.getModifiers()) && !f.isSynthetic()) {
                        boolean constante = Modifier.isFinal(f.getModifiers()) && CONSTANTES_PERMITIDAS.contains(f.getType());
                        if (!constante) {
                            culpables.add(nombre + "." + f.getName());
                        }
                    }
                }
            }
        }
        assertThat(clases).as("classes scanned").isGreaterThan(30);
        assertThat(culpables).as("static fields that are not final immutable constants").isEmpty();
    }

    @Test
    @DisplayName("Solo lecturas: los SQL de los adaptadores del cierre 3A son SELECT")
    void soloSelect() {
        for (String sql : List.of("ComunesSolicitudes_SelectSerieFondo.sql", "CalcularFechaSolicitudAporteRescate_SelectConfiguracionFechasFFMM.sql",
                "CalcularFechaSolicitudAporteRescate_SelectDiaEsFeriado.sql", "CalcularMontoNumeroCuota_SelectValorCuotaTipoCambio.sql", "Comunes_SelectTramoComision.sql",
                "Comunes_SelectPerfilFondoMutuo.sql")) {
            assertThat(SqlRecursos.modulo(sql).stripLeading().toUpperCase()).as(sql).startsWith("SELECT");
        }
    }
}
