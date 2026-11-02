package cl.bcs.sura;

import cl.bcs.sura.adapter.out.LegacyHanaSuraPort;
import cl.bcs.sura.domain.IdsEntidadYPerfiles;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * New side of the SURA read-equivalence check (B-SURA-20/21 against HANA QA, SELECT only). Reads ORACLE_IN (id TAB doc1|doc2),
 * runs LegacyHanaSuraPort and writes ORACLE_OUT/&lt;id&gt;/{ids,perfiles,filtrados}.txt in the same canonical text as hana-jdbc/oracle/LegacyOracle.java.
 * Tagged "hana": excluded by default; FAILS (never skips) without HANA_URL or ORACLE_IN. Run: hana-jdbc/oracle/oracle.sh new ...
 */
@Tag("hana")
class SuraHanaEquivalenciaTest {

    private static final String[] FORMAS = {"Opción 2", "opción 4", "Opción 9"};

    @Test
    void escribeSalidasCanonicasDeLasLecturas() throws Exception {
        assertThat(System.getenv("HANA_URL")).as("HANA_URL").isNotBlank();
        assertThat(System.getenv("ORACLE_IN")).as("ORACLE_IN").isNotBlank();
        assertThat(System.getenv("ORACLE_OUT")).as("ORACLE_OUT").isNotBlank();
        DriverManagerDataSource ds = new DriverManagerDataSource(System.getenv("HANA_URL"), System.getenv("HANA_USER"), System.getenv("HANA_PASSWORD"));
        ds.setDriverClassName("com.sap.db.jdbc.Driver");
        LegacyHanaSuraPort port = new LegacyHanaSuraPort(new NamedParameterJdbcTemplate(ds));

        int casos = 0;
        for (String line : Files.readAllLines(Path.of(System.getenv("ORACLE_IN")), StandardCharsets.UTF_8)) {
            if (line.isBlank() || line.startsWith("#")) continue;
            String[] f = line.split("\t", 2);
            List<String> docs = f.length > 1 && !f[1].isEmpty() ? Arrays.asList(f[1].split("\\|", -1)) : List.of();
            Path d = Files.createDirectories(Path.of(System.getenv("ORACLE_OUT"), f[0]));

            List<IdsEntidadYPerfiles> ids = port.consultarIdsPortafolioIdCliente(docs);
            escribir(d.resolve("ids.txt"), ids);
            escribir(d.resolve("perfiles.txt"), port.consultarPerfilesComerciales(docs));

            List<IdsEntidadYPerfiles> semilla = new ArrayList<>(ids);
            semilla.sort(Comparator.comparing(SuraHanaEquivalenciaTest::fila));
            for (int i = 0; i < semilla.size(); i++) semilla.get(i).idFormaCobro = FORMAS[i % FORMAS.length];
            escribir(d.resolve("filtrados.txt"), port.consultarPerfilesFiltrados(semilla));
            casos++;
        }
        assertThat(casos).as("cases executed").isGreaterThan(0);
        System.out.println("equivalence cases executed: " + casos);
    }

    private static String fila(IdsEntidadYPerfiles p) {
        return String.join("\t", s(p.idPortafolio), s(p.idCliente), s(p.documentoIdentidad), s(p.idIndicador), s(p.idFormaCobro),
            s(p.idCategoriaInversionista), s(p.idPerfilComercial), s(p.idPerfilCobros), s(p.idPerfilProductos), s(p.proposito));
    }

    private static String s(Object o) { return o == null ? "<null>" : o.toString(); }

    private static void escribir(Path f, List<IdsEntidadYPerfiles> rows) throws Exception {
        List<String> l = new ArrayList<>();
        for (IdsEntidadYPerfiles r : rows) l.add(fila(r));
        Collections.sort(l);
        StringBuilder b = new StringBuilder("rows=" + l.size() + "\n");
        for (String x : l) b.append(x).append('\n');
        Files.writeString(f, b.toString(), StandardCharsets.UTF_8);
    }
}
