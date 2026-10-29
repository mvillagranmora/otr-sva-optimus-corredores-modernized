package cl.bcs.fondomutuo.seguridad;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

/**
 * C3 (PLAN_FONDOMUTUO_F2 section 12): the flat {@code sql/} directory (376 files that could clash with the {@code sql/CrearIngresoEgreso_*.sql} of Tesoreria's jar,
 * the first in the classpath winning silently) is gone; every SQL of this module lives under its own package path, {@code cl/bcs/fondomutuo/sql/} (module) and
 * {@code cl/bcs/fondomutuo/kit/sql/} (Plataforma kit), and each resource is found ONCE on the classpath. The manifest {@code fondomutuo-kit/sql-sha256.txt} holds the
 * SHA-256 of the LEGACY bytes (made by a script from legacy/); the classpath bytes must equal it, except the two files that differ in the line ending only.
 */
class RecursosSqlTest {

    /** Known, documented: the module copy of these two files has LF where the legacy has CRLF (pre-existing; RULES_FONDOMUTUO_SOLICITUD section 5; outside F1/F2). */
    private static final List<String> SOLO_FIN_DE_LINEA = List.of("cl/bcs/fondomutuo/sql/ModificarCSC_ConsultarMandatoPAC.sql",
            "cl/bcs/fondomutuo/sql/ModificarCSC_InsertarMandatoPAC.sql");

    private static Map<String, String> manifiesto() throws IOException {
        Map<String, String> m = new LinkedHashMap<>();
        try (InputStream in = new ClassPathResource("fondomutuo-kit/sql-sha256.txt").getInputStream()) {
            for (String l : new String(in.readAllBytes(), StandardCharsets.UTF_8).split("\n")) {
                if (l.isBlank() || l.startsWith("#")) continue;
                String[] p = l.split("\\s+", 2);
                m.put(p[1].trim(), p[0]);
            }
        }
        return m;
    }

    private static byte[] bytes(String ruta) throws IOException {
        try (InputStream in = RecursosSqlTest.class.getClassLoader().getResourceAsStream(ruta)) {
            assertThat(in).as("resource on the classpath: " + ruta).isNotNull();
            return in.readAllBytes();
        }
    }

    private static String sha(byte[] b) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(b));
    }

    @Test
    void noQuedaElDirectorioPlanoSql_niNingunSqlSueltoEnLaRaiz() throws Exception {
        List<URL> raiz = Collections.list(RecursosSqlTest.class.getClassLoader().getResources("sql"));
        // another jar may legitimately carry sql/ (Tesoreria); THIS module's output must not
        for (URL u : raiz) {
            assertThat(u.toString()).as("flat sql/ directory of this module").doesNotContain("bcs-fondo-mutuo-servicios").doesNotContain("BcsFondoMutuoServicios");
        }
        assertThat(java.nio.file.Files.exists(java.nio.file.Path.of("src", "main", "resources", "sql"))).isFalse();
    }

    @Test
    void los376SqlDelModuloY21DelKitEstanBajoRutasPropias_unaVezEnElClasspath() throws Exception {
        Map<String, String> m = manifiesto();
        assertThat(m).hasSize(397);
        assertThat(m.keySet().stream().filter(r -> r.startsWith("cl/bcs/fondomutuo/sql/")).count()).isEqualTo(376);
        assertThat(m.keySet().stream().filter(r -> r.startsWith("cl/bcs/fondomutuo/kit/sql/")).count()).isEqualTo(21);
        List<String> repetidos = new ArrayList<>();
        for (String ruta : m.keySet()) {
            if (Collections.list(RecursosSqlTest.class.getClassLoader().getResources(ruta)).size() != 1) repetidos.add(ruta);
        }
        assertThat(repetidos).as("resources found zero or several times on the classpath").isEmpty();
    }

    @Test
    void losBytesDelClasspathSonLosDelLegacy_salvoElFinDeLineaDeDosArchivos() throws Exception {
        List<String> distintos = new ArrayList<>();
        for (Map.Entry<String, String> e : manifiesto().entrySet()) {
            byte[] b = bytes(e.getKey());
            if (!sha(b).equals(e.getValue())) distintos.add(e.getKey());
        }
        assertThat(distintos).containsExactlyInAnyOrderElementsOf(SOLO_FIN_DE_LINEA);
        // and those two differ ONLY in CRLF -> LF: restoring the CRLF reproduces the legacy hash
        Map<String, String> m = manifiesto();
        for (String ruta : SOLO_FIN_DE_LINEA) {
            String t = new String(bytes(ruta), StandardCharsets.ISO_8859_1);
            assertThat(t).doesNotContain("\r");
            assertThat(sha(t.replace("\n", "\r\n").getBytes(StandardCharsets.ISO_8859_1))).as(ruta).isEqualTo(m.get(ruta));
        }
    }

    @Test
    void elCargadorFallaSiFaltaUnRecurso_nuncaDevuelveVacio() {
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> cl.bcs.fondomutuo.adapter.out.jdbc.SqlRecursos.modulo("NoExiste.sql"))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("cl/bcs/fondomutuo/sql/NoExiste.sql");
        assertThat(cl.bcs.fondomutuo.adapter.out.jdbc.SqlRecursos.modulo("CrearSolicitudAporteRescateNormal_CallCrearBloqueoSolicitudAporteRescate.sql"))
                .contains("CrearBloqueoSolicitudAporteRescate");
    }
}
