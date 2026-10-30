package cl.bcs.canalagente.caracterizacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
 * The 7 SQL texts of the legacy module are ported byte for byte (line endings aside): the 4 SQL of the Java class (legacy .../canalagente/sql/) to the production resources
 * cl/bcs/canalagente/sql/ and the 3 SQLScript procedures of BD/Servicios (HANA only, they cannot run on H2: pinned by SHA-256) to cl/bcs/canalagente/bd/servicios/. The SHA-256 come from the manifest
 * generated from the frozen legacy by make_sql_manifest.py. The statements each case sends are compared with the goldens by the equivalence test; this test pins the files themselves.
 */
class CanalSqlRecursosTest {

    private static List<String[]> manifiesto() {
        List<String[]> l = new ArrayList<>();
        for (String linea : CasoCanal.texto("sql-manifest.txt").split("\n")) {
            if (!linea.isBlank()) l.add(linea.split(" "));
        }
        return l;
    }

    @Test
    @DisplayName("El manifiesto del legacy tiene 7 archivos SQL: 4 de la clase Java y 3 procedimientos de BD/Servicios")
    void manifiesto_de_7() {
        assertEquals(7, manifiesto().size());
        assertEquals(4, manifiesto().stream().filter(m -> m[0].startsWith("sql/")).count());
        assertEquals(3, manifiesto().stream().filter(m -> m[0].startsWith("bd/servicios/")).count());
    }

    @Test
    @DisplayName("Los 7 SQL del legacy estan en cl/bcs/canalagente/ con el mismo contenido (SHA-256, LF)")
    void los_7_sql_estan_portados_byte_a_byte() throws Exception {
        List<String> faltan = new ArrayList<>();
        List<String> distintos = new ArrayList<>();
        for (String[] m : manifiesto()) {
            try (InputStream in = CanalSqlRecursosTest.class.getClassLoader().getResourceAsStream("cl/bcs/canalagente/" + m[0])) {
                if (in == null) {
                    faltan.add(m[0]);
                    continue;
                }
                byte[] b = new String(in.readAllBytes(), StandardCharsets.UTF_8).replace("\r\n", "\n").getBytes(StandardCharsets.UTF_8);
                String sha = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(b));
                if (!sha.equals(m[2]) || b.length != Integer.parseInt(m[1])) distintos.add(m[0]);
            }
        }
        assertTrue(faltan.isEmpty(), "not implemented: SQL resources missing in src/main/resources/cl/bcs/canalagente/: " + faltan);
        assertTrue(distintos.isEmpty(), "SQL resources that differ from the legacy: " + distintos);
    }

    @Test
    @DisplayName("El puerto de procedimientos entrega el mismo texto que el recurso portado (los 3 procedimientos)")
    void el_puerto_entrega_el_texto_del_recurso() throws Throwable {
        for (String nombre : new String[] { "ConsultaAvanzadaClientes", "ConsultarHistorialMovimientosCuentaInversion", "ConsultarHistorialMovimientosCustodia" }) {
            String texto = ApiCanalAgente.textoProcedimiento(nombre);
            try (InputStream in = CanalSqlRecursosTest.class.getClassLoader().getResourceAsStream("cl/bcs/canalagente/bd/servicios/" + nombre + ".sql")) {
                assertTrue(in != null, "not implemented: resource bd/servicios/" + nombre + ".sql missing");
                assertEquals(new String(in.readAllBytes(), StandardCharsets.UTF_8).replace("\r\n", "\n"), texto.replace("\r\n", "\n"), nombre);
            }
        }
    }
}
