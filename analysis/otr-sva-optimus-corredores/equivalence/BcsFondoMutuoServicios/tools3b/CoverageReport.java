import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.IMethodCoverage;
import org.jacoco.core.tools.ExecFileLoader;

/** usage: CoverageReport <exec> <classesDir> <packagePrefix(/ form)> [verbose]: branch coverage per top-level class (inner classes summed in). */
public class CoverageReport {
    public static void main(String[] a) throws IOException {
        ExecFileLoader loader = new ExecFileLoader();
        try (FileInputStream in = new FileInputStream(a[0])) {
            loader.load(in);
        }
        CoverageBuilder cb = new CoverageBuilder();
        Analyzer an = new Analyzer(loader.getExecutionDataStore(), cb);
        an.analyzeAll(new File(a[1]));
        Map<String, int[]> porClase = new TreeMap<>();
        for (IClassCoverage c : cb.getClasses()) {
            String n = c.getName();
            if (!n.startsWith(a[2])) continue;
            String top = n.contains("$") ? n.substring(0, n.indexOf('$')) : n;
            int[] t = porClase.computeIfAbsent(top, k -> new int[4]);
            t[0] += c.getBranchCounter().getCoveredCount();
            t[1] += c.getBranchCounter().getTotalCount();
            t[2] += c.getLineCounter().getCoveredCount();
            t[3] += c.getLineCounter().getTotalCount();
            if (a.length > 3) {
                for (IMethodCoverage m : c.getMethods()) {
                    if (m.getBranchCounter().getMissedCount() > 0) {
                        System.out.println("   MISSED " + n + "." + m.getName() + " branches " + m.getBranchCounter().getCoveredCount() + "/"
                                + m.getBranchCounter().getTotalCount());
                    }
                }
            }
        }
        for (Map.Entry<String, int[]> e : porClase.entrySet()) {
            int[] t = e.getValue();
            System.out.printf("%-90s branches %4d/%-4d %5.1f%%  lines %4d/%-4d%n", e.getKey(), t[0], t[1], t[1] == 0 ? 100.0 : 100.0 * t[0] / t[1], t[2], t[3]);
        }
    }
}
