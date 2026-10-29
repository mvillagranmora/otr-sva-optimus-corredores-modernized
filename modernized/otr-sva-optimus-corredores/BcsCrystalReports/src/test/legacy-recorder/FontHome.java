import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * JRC needs ${java.home}/lib/fonts (absent on JDK 9+, its OpenTypeFontManager throws an NPE otherwise). The installed JDK
 * is not modified: java.home is pointed at a throw-away copy of the font configuration + TTFs from the OS font dirs.
 * Same logic as the test's CrystalTestEnvironment, so legacy and sidecar see the SAME fonts on this machine.
 */
public final class FontHome {
    private FontHome() {
    }

    public static void ensure(File fakeHome) throws Exception {
        File fonts = new File(System.getProperty("java.home"), "lib/fonts");
        if (fonts.isDirectory()) {
            return;
        }
        File fakeFonts = new File(fakeHome, "lib/fonts");
        Files.createDirectories(fakeFonts.toPath());
        File realLib = new File(System.getProperty("java.home"), "lib");
        File[] cfg = realLib.listFiles((d, n) -> n.startsWith("fontconfig") || n.startsWith("psfont"));
        for (File f : cfg == null ? new File[0] : cfg) {
            Files.copy(f.toPath(), new File(fakeHome, "lib/" + f.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        String sys = System.getProperty("SystemFontPath");
        String[] roots = {sys == null ? "" : sys, "C:/Windows/Fonts", "/usr/share/fonts", "/usr/local/share/fonts"};
        int copied = 0;
        for (String r : roots) {
            File root = new File(r);
            if (r.isEmpty() || !root.isDirectory() || copied > 0) {
                continue;
            }
            List<Path> ttfs;
            try (Stream<Path> st = Files.walk(root.toPath(), 3)) {
                ttfs = st.filter(x -> x.toString().toLowerCase().endsWith(".ttf")).limit(20).collect(Collectors.toList());
            }
            for (Path p : ttfs) {
                Files.copy(p, new File(fakeFonts, p.getFileName().toString()).toPath(), StandardCopyOption.REPLACE_EXISTING);
                copied++;
            }
        }
        if (copied == 0) {
            throw new IllegalStateException("No TrueType fonts found for ${java.home}/lib/fonts");
        }
        System.setProperty("java.home", fakeHome.getPath());
    }
}
