package cl.bcs.replica.caracterizacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The 58 SQL files of the legacy module (legacy/.../Replica/JAVA/BcsReplica/src/cl/bcs/replica/sql/EnviarTablaReplica_*.sql and IniciarReplica_InsertWorkstack.sql) are ported byte for byte (line
 * endings aside): the production resources cl/bcs/replica/sql/ must exist with the SHA-256 of the manifest generated from the frozen legacy by make_sql_manifest.py. The statements that each case sends
 * are compared with the goldens by the equivalence test; this test pins the files themselves, including the ones only a closed gate reaches (triggers, Sebra, Optimus).
 */
class ReplicaSqlRecursosTest {

    private static List<String[]> manifiesto() {
        List<String[]> l = new ArrayList<>();
        for (String linea : CasoReplica.texto("sql-manifest.txt").split("\n")) {
            if (!linea.isBlank()) l.add(linea.split(" "));
        }
        return l;
    }

    @Test
    @DisplayName("El manifiesto del legacy tiene 58 archivos SQL")
    void manifiesto_de_58() {
        assertEquals(58, manifiesto().size());
    }

    @Test
    @DisplayName("Los 58 SQL del legacy estan en cl/bcs/replica/sql/ con el mismo contenido (SHA-256, LF)")
    void los_58_sql_estan_portados_byte_a_byte() throws Exception {
        List<String> faltan = new ArrayList<>();
        List<String> distintos = new ArrayList<>();
        for (String[] m : manifiesto()) {
            try (InputStream in = ReplicaSqlRecursosTest.class.getClassLoader().getResourceAsStream("cl/bcs/replica/sql/" + m[0])) {
                if (in == null) {
                    faltan.add(m[0]);
                    continue;
                }
                byte[] b = new String(in.readAllBytes(), StandardCharsets.UTF_8).replace("\r\n", "\n").getBytes(StandardCharsets.UTF_8);
                String sha = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(b));
                if (!sha.equals(m[2])) distintos.add(m[0]);
            }
        }
        assertTrue(faltan.isEmpty(), "not implemented: SQL resources missing in src/main/resources/cl/bcs/replica/sql/: " + faltan);
        assertTrue(distintos.isEmpty(), "SQL resources that differ from the legacy: " + distintos);
        assertNotNull(manifiesto());
    }
}
