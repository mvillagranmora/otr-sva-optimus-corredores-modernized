package cl.bcs.apv.familia_b;

import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;

import cl.bcs.apv.familia_b.support.Caso;
import cl.bcs.apv.familia_b.support.Equivalencia;
import cl.bcs.apv.familia_b.support.ZonaUtc;

/** SCRATCH (fresh verification, not part of the module): runs every FRESH-B-* case directory like the generated test classes do. */
@ExtendWith(ZonaUtc.class)
class FreshBTest {
    private static final Caso.Contador EJECUTADOS = new Caso.Contador();

    @TestFactory
    Stream<DynamicTest> fresh() {
        return Caso.todos().stream().filter(n -> n.startsWith("FRESH-")).map(n -> DynamicTest.dynamicTest(n, () -> Equivalencia.verificar(EJECUTADOS, n)));
    }
}
