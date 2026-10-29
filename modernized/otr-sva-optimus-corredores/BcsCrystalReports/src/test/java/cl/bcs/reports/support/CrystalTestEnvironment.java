package cl.bcs.reports.support;

import java.io.File;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Preconditions of the real Crystal render tests. They FAIL (never skip) when something is missing.
 * <ul>
 *   <li>Crystal SDK jars on the classpath (installed from libs/ by install-libs.sh).</li>
 *   <li>JVM flags for com.sun.rowset (add-exports/add-opens) and headless AWT (pom: crystal.jvm.args).</li>
 *   <li>${java.home}/lib/fonts: JDK 9+ no longer ships it and JRC's OpenTypeFontManager throws an NPE when it is
 *       missing. The Docker image creates it. Locally the installed JDK must NOT be modified, so when the directory is
 *       missing we create target/test-java-home/lib/fonts and point the java.home property to it (JRC reads the
 *       property lazily; fonts themselves come from the OS font path, C:\Windows\Fonts by default).</li>
 * </ul>
 */
public final class CrystalTestEnvironment {

    private static boolean ready;

    private CrystalTestEnvironment() {
    }

    public static synchronized void ensure() {
        if (ready) {
            return;
        }
        try {
            Class.forName("com.crystaldecisions.sdk.occa.report.application.ReportClientDocument");
            Class.forName("com.businessobjects.foundation.logging.LoggerManager");
            Class.forName("org.apache.log4j.Logger");
            Class.forName("com.crystaldecisions.common.keycode.KeycodeException");
            Class.forName("org.apache.commons.configuration.CompositeConfiguration");
            Class.forName("com.ibm.icu.util.Calendar");
        } catch (ClassNotFoundException | LinkageError e) {
            fail("Crystal Reports SDK / runtime jars are not available (" + e + "). Run install-libs.sh once "
                    + "(libs/ must contain the proprietary jars) and build with 'mvn -o'.", e);
        }
        try {
            Class.forName("cl.bcs.reports.report.ListMapResultSet").getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException | LinkageError e) {
            fail("com.sun.rowset is not accessible: run with the JVM flags of pom property crystal.jvm.args "
                    + "(--add-exports/--add-opens java.sql.rowset/com.sun.rowset=ALL-UNNAMED).", e);
        }
        if (!"true".equals(System.getProperty("java.awt.headless"))) {
            fail("-Djava.awt.headless=true is required (pom property crystal.jvm.args).");
        }
        if (!"UTF-8".equalsIgnoreCase(System.getProperty("file.encoding"))) {
            fail("-Dfile.encoding=UTF-8 is required (legacy ran with it; TXT export uses the default charset).");
        }
        File fonts = new File(System.getProperty("java.home"), "lib/fonts");
        if (!fonts.isDirectory()) {
            File fakeHome = new File("target/test-java-home").getAbsoluteFile();
            File fakeFonts = new File(fakeHome, "lib/fonts");
            try {
                Files.createDirectories(fakeFonts.toPath());
                // AWT's FontConfiguration reads these from ${java.home}/lib too (else "Fontconfig head is null")
                File realLib = new File(System.getProperty("java.home"), "lib");
                File[] cfg = realLib.listFiles((d, n) -> n.startsWith("fontconfig") || n.startsWith("psfont"));
                for (File f : cfg == null ? new File[0] : cfg) {
                    Files.copy(f.toPath(), new File(fakeHome, "lib/" + f.getName()).toPath(),
                            java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                }
                copyFonts(fakeFonts);
            } catch (Exception e) {
                fail("Cannot create " + fakeFonts + " (JRC needs ${java.home}/lib/fonts on JDK 9+)", e);
            }
            System.setProperty("java.home", fakeHome.getPath());
        }
        ready = true;
    }

    /** Copies TTFs from the OS font directories into the (test-only) java.home fonts dir; fails if there are none. */
    private static void copyFonts(File target) throws java.io.IOException {
        String sys = System.getProperty("SystemFontPath");
        String[] roots = {sys == null ? "" : sys, "C:/Windows/Fonts", "/usr/share/fonts", "/usr/local/share/fonts"};
        int copied = 0;
        for (String r : roots) {
            File root = new File(r);
            if (r.isEmpty() || !root.isDirectory()) {
                continue;
            }
            java.util.List<java.nio.file.Path> ttfs;
            try (java.util.stream.Stream<java.nio.file.Path> st = Files.walk(root.toPath(), 3)) {
                ttfs = st.filter(x -> x.toString().toLowerCase().endsWith(".ttf"))
                        .limit(20).collect(java.util.stream.Collectors.toList());
            }
            for (java.nio.file.Path p : ttfs) {
                Files.copy(p, new File(target, p.getFileName().toString()).toPath(),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                copied++;
            }
            if (copied > 0) {
                return;
            }
        }
        fail("No TrueType fonts found to populate ${java.home}/lib/fonts (JRC PDF export needs them). "
                + "Install fonts or pass -DSystemFontPath=<dir with .ttf>.");
    }
}
