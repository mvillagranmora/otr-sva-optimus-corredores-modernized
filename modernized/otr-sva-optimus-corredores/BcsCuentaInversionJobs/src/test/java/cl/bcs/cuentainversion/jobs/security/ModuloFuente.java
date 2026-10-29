package cl.bcs.cuentainversion.jobs.security;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The production side of THIS module, as files: {@code src/main/java}, {@code src/main/resources} and the compiled
 * {@code target/classes}. Every accessor fails (never returns an empty list) when the tree is missing or does not hold
 * the job classes, so a scan can never pass vacuously.
 */
final class ModuloFuente {

    static final List<String> CLASES_OBLIGATORIAS = List.of(
            "cl/bcs/cuentainversion/jobs/job/ActualizarSaldoCargoAbonoJob",
            "cl/bcs/cuentainversion/jobs/job/ActualizarSaldosReporteAntiguedadJob",
            "cl/bcs/cuentainversion/jobs/job/CargaInicialSaldosReporteAntiguedadJob",
            "cl/bcs/cuentainversion/jobs/job/BcsJobGenerarInformePatrimonioDiario",
            "cl/bcs/cuentainversion/jobs/job/BcsJobGenerarInformeResumenCobro",
            "cl/bcs/cuentainversion/jobs/job/GenerarArchivoCargoAbonoJob",
            "cl/bcs/cuentainversion/jobs/job/GenerarArchivoClientesPorPagarYCobrarJob",
            "cl/bcs/cuentainversion/jobs/job/GenerarArchivoCuentasPorCobrarAnexo16Job",
            "cl/bcs/cuentainversion/jobs/job/GenerarArchivoDiferenciaSaldoJob",
            "cl/bcs/cuentainversion/jobs/job/GenerarArchivoSaldoCuentasJob",
            "cl/bcs/cuentainversion/jobs/job/GenerarReporteAcreedoresJob",
            "cl/bcs/cuentainversion/jobs/job/GenerarReporteAntiguedadDeudaBolsaCTAJob",
            "cl/bcs/cuentainversion/jobs/job/GenerarReporteAntiguedadDeudaCTAJob",
            "cl/bcs/cuentainversion/jobs/job/GuardarPatrimonioCajaJob",
            "cl/bcs/cuentainversion/jobs/job/LibroInventarioJob",
            "cl/bcs/cuentainversion/jobs/job/GenerarArchivo",
            "cl/bcs/cuentainversion/jobs/job/pauta/PautaActualizarSaldoCargoAbono",
            "cl/bcs/cuentainversion/jobs/job/pauta/PautaGuardarSaldosReporteAntiguedad",
            "cl/bcs/cuentainversion/jobs/job/pauta/PautaGenerarReporteAcreedores",
            "cl/bcs/cuentainversion/jobs/job/pauta/PautaGenerarReporteAntiguedadDeudaBolsaCTA",
            "cl/bcs/cuentainversion/jobs/runtime/JobRunner",
            "cl/bcs/cuentainversion/jobs/web/JobCallController",
            "cl/bcs/cuentainversion/jobs/web/TokenInternoFilter",
            "cl/bcs/cuentainversion/jobs/wiring/JobsConfiguration");

    private ModuloFuente() {
    }

    static Path base() {
        return Path.of(System.getProperty("basedir", System.getProperty("user.dir"))).toAbsolutePath().normalize();
    }

    static List<Path> fuentesJava() {
        List<Path> l = listar(base().resolve("src/main/java"), ".java");
        for (String c : CLASES_OBLIGATORIAS) {
            assertThat(base().resolve("src/main/java/" + c + ".java")).as("scan must cover the job sources").exists();
        }
        return l;
    }

    static List<Path> recursos() {
        Path r = base().resolve("src/main/resources");
        if (!Files.isDirectory(r)) return List.of();
        return listar(r, "");
    }

    static List<Path> clasesCompiladas() {
        List<Path> l = listar(base().resolve("target/classes"), ".class");
        for (String c : CLASES_OBLIGATORIAS) {
            assertThat(base().resolve("target/classes/" + c + ".class")).as("scan must cover the compiled job classes").exists();
        }
        return l;
    }

    static String nombreClase(Path clase) {
        String rel = base().resolve("target/classes").relativize(clase).toString().replace('\\', '/');
        return rel.substring(0, rel.length() - ".class".length()).replace('/', '.');
    }

    static String texto(Path p) {
        try {
            return Files.readString(p, StandardCharsets.UTF_8);
        } catch (IOException e) {
            try {
                return Files.readString(p, StandardCharsets.ISO_8859_1);
            } catch (IOException e2) {
                throw new UncheckedIOException(e2);
            }
        }
    }

    static byte[] bytes(Path p) {
        try {
            return Files.readAllBytes(p);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static List<Path> listar(Path raiz, String sufijo) {
        if (!Files.isDirectory(raiz)) {
            throw new AssertionError(raiz + " does not exist: nothing to scan is a FAILURE, not a pass");
        }
        try (Stream<Path> s = Files.walk(raiz)) {
            List<Path> l = s.filter(Files::isRegularFile).filter(p -> p.toString().endsWith(sufijo)).sorted().toList();
            if (l.isEmpty()) throw new AssertionError("no " + sufijo + " file under " + raiz);
            return l;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
