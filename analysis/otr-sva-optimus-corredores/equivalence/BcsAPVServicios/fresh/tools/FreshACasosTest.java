package cl.bcs.apv.familia_a;

import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;

import cl.bcs.apv.familia_a.support.Caso;
import cl.bcs.apv.familia_a.support.Dobles;
import cl.bcs.apv.familia_a.support.Verifica;
import cl.bcs.apv.familia_a.support.ZonaUtc;

/** SCRATCH (fresh verification, not part of the module): runs every FRESH-A-* case directory like the generated test classes do. */
@ExtendWith(ZonaUtc.class)
class FreshACasosTest {
    @TestFactory
    Stream<DynamicTest> fresh() {
        return Caso.todos().stream().filter(n -> n.startsWith("FRESH-")).map(n -> DynamicTest.dynamicTest(n, () -> {
            Caso caso = Caso.cargar(n);
            Dobles d = Dobles.de(caso);
            String servicio = caso.caso().get("servicio").getAsString();
            Verifica.Ejecucion r = Verifica.Ejecucion.de(caso, () -> d.pipeline(caso).invocar("cl.bcs.APV." + servicio, caso.entrada().deepCopy()));
            Verifica.comparar(caso, r, d.colaboradores().traza(), new String[] {});
        }));
    }
}
