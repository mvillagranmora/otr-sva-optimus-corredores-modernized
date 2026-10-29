import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.tools.ExecFileLoader;

/**
 * Branch / line coverage of the step-3A classes from a JaCoCo exec file (offline: org.jacoco.core + asm jars of the VS Code Java Test extension; the
 * module has no JaCoCo plugin configured). Nested classes are folded into their top-level class.
 *   java -cp "core.jar;asm.jar;asm-commons.jar;asm-tree.jar;." CoverageReport jacoco.exec target/classes pkg1/Class1 pkg2/Class2 ...
 * With -v the lines with missed branches are listed.
 */
public class CoverageReport {
    public static void main(String[] args) throws Exception {
        boolean verbose = args[0].equals("-v");
        int a = verbose ? 1 : 0;
        ExecFileLoader loader = new ExecFileLoader();
        loader.load(new File(args[a]));
        File classes = new File(args[a + 1]);
        CoverageBuilder cb = new CoverageBuilder();
        Analyzer an = new Analyzer(loader.getExecutionDataStore(), cb);
        Map<String, int[]> total = new TreeMap<>();
        Map<String, List<Integer>> missed = new TreeMap<>();
        for (int i = a + 2; i < args.length; i++) {
            String top = args[i];
            Path dir = classes.toPath().resolve(top).getParent();
            String base = top.substring(top.lastIndexOf('/') + 1);
            try (Stream<Path> s = Files.list(dir)) {
                for (Path p : (Iterable<Path>) s::iterator) {
                    String n = p.getFileName().toString();
                    if (n.equals(base + ".class") || n.startsWith(base + "$")) an.analyzeAll(p.toFile());
                }
            }
        }
        for (IClassCoverage cc : cb.getClasses()) {
            String top = cc.getName().contains("$") ? cc.getName().substring(0, cc.getName().indexOf('$')) : cc.getName();
            int[] t = total.computeIfAbsent(top, k -> new int[4]);
            t[0] += cc.getBranchCounter().getCoveredCount();
            t[1] += cc.getBranchCounter().getTotalCount();
            t[2] += cc.getLineCounter().getCoveredCount();
            t[3] += cc.getLineCounter().getTotalCount();
            if (verbose) {
                List<Integer> m = missed.computeIfAbsent(top, k -> new ArrayList<>());
                for (int l = cc.getFirstLine(); l <= cc.getLastLine(); l++) {
                    if (cc.getLine(l).getBranchCounter().getMissedCount() > 0) m.add(l);
                }
            }
        }
        System.out.printf("%-70s %12s %12s%n", "class", "branches", "lines");
        for (Map.Entry<String, int[]> e : total.entrySet()) {
            int[] t = e.getValue();
            System.out.printf("%-70s %5d/%-5d %3.0f%% %5d/%-5d %3.0f%%%n", e.getKey(), t[0], t[1], t[1] == 0 ? 100.0 : 100.0 * t[0] / t[1], t[2], t[3], t[3] == 0 ? 100.0 : 100.0 * t[2] / t[3]);
            if (verbose) System.out.println("   lines with missed branches: " + missed.get(e.getKey()));
        }
    }
}
