import cl.bcs.sura.socionegocio.pojo.IdsEntidadYPerfiles;
import cl.bcs.utils.SQL;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.*;

/**
 * Legacy oracle for the SURA read queries, SELECT only, against HANA QA.
 * Runs the UNMODIFIED legacy classes cl.bcs.utils.SQL (+ NamedParameterStatement) and cl.bcs.sura.socionegocio.pojo.IdsEntidadYPerfiles,
 * with the legacy .sql files read from /legacy. Usage: java LegacyOracle <inputs.tsv> <outDir>
 * inputs.tsv: id TAB doc1|doc2|...   Writes <outDir>/<id>/{ids,perfiles,filtrados}.txt (canonical text, sorted rows).
 * Only deviation: the temp table of the filtered query is replaced by an inline derived table (SELECT CAST(..) FROM DUMMY UNION ALL ...; HANA rejects (VALUES ..) in FROM). A temp table is DDL, not a SELECT.
 */
public class LegacyOracle {
    static final String SQLDIR = "/legacy/BSCL/Corredor/SURA/JAVA/Servicios/src/cl/bcs/sura/socionegocio/sql/";
    static final String[] FORMAS = {"Opción 2", "opción 4", "Opción 9"};

    public static void main(String[] a) throws Exception {
        List<String> inputs = Files.readAllLines(Path.of(a[0]), StandardCharsets.UTF_8);
        Path out = Path.of(a[1]);
        String sqlIds = read("ModificarPerfilesPortafolioUtils_ConsultarIdsPortafolioIdClientePorDocumentoIdentidad.sql");
        String sqlPerf = read("ModificarPerfilesPortafolioUtils_ConsultarPerfilesComercialesPorDocumentosIdentidad.sql");
        String sqlFilt = read("ModificarPerfilesPortafolioUtils_ConsultarPerfilesComercialesFiltradosPorTablaTemporal.sql");
        String tempRef = "\"ModificarPerfilesPortafolioUtils_ClientesFormasCobro\" AS TEMP";
        if (!sqlFilt.contains(tempRef)) throw new IllegalStateException("temp table reference not found in the legacy SQL");

        Properties p = new Properties();
        p.setProperty("user", System.getenv("qa01.saphana.dbuser"));
        p.setProperty("password", System.getenv("qa01.saphana.dbpass"));
        for (String kv : System.getenv().getOrDefault("qa01.saphana.dbprop", "").split("[;&]")) {
            int i = kv.indexOf('=');
            if (i > 0) p.setProperty(kv.substring(0, i).trim(), kv.substring(i + 1).trim());
        }
        Class.forName("com.sap.db.jdbc.Driver");
        try (Connection c = DriverManager.getConnection(System.getenv("qa01.saphana.dbURL"), p)) {
            c.setReadOnly(true);
            SQL sql = new SQL();
            for (String line : inputs) {
                if (line.isBlank() || line.startsWith("#")) continue;
                String[] f = line.split("\t", 2);
                String id = f[0];
                // the legacy SQL class expands IN (:list) only for a java.util.ArrayList
                List<String> docs = new ArrayList<>();
                if (f.length > 1 && !f[1].isEmpty()) docs.addAll(Arrays.asList(f[1].split("\\|", -1)));
                Path d = Files.createDirectories(out.resolve(id));

                HashMap<String, Object> prm = new HashMap<>();
                prm.put("DocumentoIdentidad", docs);
                List<IdsEntidadYPerfiles> ids = sql.query(c, sqlIds, prm, IdsEntidadYPerfiles.class);
                write(d.resolve("ids.txt"), ids);

                prm = new HashMap<>();
                prm.put("DocumentoIdentidad", docs);
                write(d.resolve("perfiles.txt"), sql.query(c, sqlPerf, prm, IdsEntidadYPerfiles.class));

                List<IdsEntidadYPerfiles> seed = new ArrayList<>(ids);
                seed.sort(Comparator.comparing(LegacyOracle::row));
                List<IdsEntidadYPerfiles> filt = new ArrayList<>();
                if (!seed.isEmpty()) {
                    StringBuilder v = new StringBuilder("(");
                    HashMap<String, Object> fp = new HashMap<>();
                    for (int i = 0; i < seed.size(); i++) {
                        if (i > 0) v.append(" UNION ALL ");
                        v.append("SELECT CAST(:c").append(i).append(" AS BIGINT) AS \"IdCliente\", CAST(:f").append(i)
                         .append(" AS NVARCHAR(255)) AS \"IdFormaCobro\", CAST(:d").append(i)
                         .append(" AS NVARCHAR(40)) AS \"DocumentoIdentidad\", CAST(:p").append(i).append(" AS BIGINT) AS \"IdPortafolio\" FROM DUMMY");
                        fp.put("c" + i, seed.get(i).IdCliente);
                        fp.put("f" + i, FORMAS[i % FORMAS.length]);
                        fp.put("d" + i, seed.get(i).DocumentoIdentidad);
                        fp.put("p" + i, seed.get(i).IdPortafolio);
                    }
                    v.append(") AS TEMP");
                    filt = sql.query(c, sqlFilt.replace(tempRef, v.toString()), fp, IdsEntidadYPerfiles.class);
                }
                write(d.resolve("filtrados.txt"), filt);
                System.out.println(id + ": ids=" + ids.size());
            }
        }
    }

    static String read(String n) throws IOException { return Files.readString(Path.of(SQLDIR + n), StandardCharsets.UTF_8); }

    static String row(IdsEntidadYPerfiles p) {
        return String.join("\t", s(p.IdPortafolio), s(p.IdCliente), s(p.DocumentoIdentidad), s(p.IdIndicador), s(p.IdFormaCobro),
            s(p.IdCategoriaInversionista), s(p.IdPerfilComercial), s(p.IdPerfilCobros), s(p.IdPerfilProductos), s(p.Proposito));
    }

    static String s(Object o) { return o == null ? "<null>" : o.toString(); }

    static void write(Path f, List<IdsEntidadYPerfiles> rows) throws IOException {
        List<String> l = new ArrayList<>();
        for (IdsEntidadYPerfiles r : rows) l.add(row(r));
        Collections.sort(l);
        StringBuilder b = new StringBuilder("rows=" + l.size() + "\n");
        for (String x : l) b.append(x).append('\n');
        Files.writeString(f, b.toString(), StandardCharsets.UTF_8);
    }
}
