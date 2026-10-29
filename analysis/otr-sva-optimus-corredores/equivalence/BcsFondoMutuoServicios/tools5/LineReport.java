import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ILine;
import org.jacoco.core.tools.ExecFileLoader;

/** usage: LineReport <exec> <classesDir> <packagePrefix(/ form)>: every source line with missed branches: "File.java:line covered/total". */
public class LineReport {
    public static void main(String[] a) throws IOException {
        ExecFileLoader loader = new ExecFileLoader();
        try (FileInputStream in = new FileInputStream(a[0])) {
            loader.load(in);
        }
        CoverageBuilder cb = new CoverageBuilder();
        new Analyzer(loader.getExecutionDataStore(), cb).analyzeAll(new File(a[1]));
        for (IClassCoverage c : cb.getClasses()) {
            if (!c.getName().startsWith(a[2]) || c.getName().contains("$")) continue;
            for (int i = c.getFirstLine(); i <= c.getLastLine(); i++) {
                ILine l = c.getLine(i);
                if (l.getBranchCounter().getMissedCount() > 0) {
                    System.out.println(c.getSourceFileName() + ":" + i + " " + l.getBranchCounter().getCoveredCount() + "/" + l.getBranchCounter().getTotalCount());
                }
            }
        }
    }
}
