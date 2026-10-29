package cl.bcs.contabilidad.interfaz;

import cl.bcs.contabilidad.domain.port.out.InterfazSapSource;
import cl.bcs.contabilidad.domain.port.out.InterfazSapSource.BroadCuenta;
import cl.bcs.contabilidad.domain.port.out.InterfazSapSource.BroadMovimiento;
import cl.bcs.contabilidad.domain.port.out.InterfazSapSource.BroadSaldo;
import cl.bcs.contabilidad.domain.port.out.InterfazSapSource.CentroCosto;
import cl.bcs.contabilidad.domain.port.out.InterfazSapSource.CuentaBpc;
import cl.bcs.contabilidad.domain.port.out.InterfazSapSource.Entidad;
import cl.bcs.contabilidad.domain.port.out.InterfazSapSource.MovimientoBpc;
import cl.bcs.contabilidad.domain.port.out.InterfazSapSource.TipoCuenta;
import cl.bcs.contabilidad.infrastructure.interfaz.InterfazContext;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.fail;

/**
 * Shared fixtures for the WP-A "interfaz diaria" characterization tests.
 *
 * <p>The rows below are EXACTLY the rows fed to the scratch oracle harness that executed the
 * legacy String.format / Calendar statements verbatim to produce the golden files under
 * {@code src/test/resources/interfaz/golden/}. Changing a row here without regenerating the
 * goldens from the legacy code breaks the golden comparisons on purpose.
 *
 * <p>Rows are listed in SAP query order; the legacy writes them in REVERSE order (CONT-A-24).
 */
final class InterfazFixtures {

    private InterfazFixtures() {
    }

    /** Legacy JVM default zone assumed for the production host (Chile). */
    static final ZoneId SANTIAGO = ZoneId.of("America/Santiago");

    /** 2026-10-13 15:04:05 in Santiago (UTC-3, DST). Local date 2026-10-13 -> fechaEjecucion 2026-10-12. */
    static final Instant AHORA = Instant.parse("2026-10-13T18:04:05Z");

    static final Clock CLOCK = Clock.fixed(AHORA, SANTIAGO);

    static final LocalDate HOY = LocalDate.of(2026, 10, 13);
    static final LocalDate FECHA_EJECUCION = LocalDate.of(2026, 10, 12);

    static final long CONTADOR = 3L;

    static InterfazContext contexto(Path base) {
        return new InterfazContext(FECHA_EJECUCION, CONTADOR,
                base.resolve("Transmisiones/Salida/FI"), base.resolve("Transmisiones/Salida/BPC"));
    }

    static InterfazContext contexto(Path base, long contador) {
        return new InterfazContext(FECHA_EJECUCION, contador,
                base.resolve("Transmisiones/Salida/FI"), base.resolve("Transmisiones/Salida/BPC"));
    }

    // ------------------------------------------------------------------ rows (query order)

    static List<BroadCuenta> cuentasBroad() {
        List<BroadCuenta> l = new ArrayList<>();
        l.add(new BroadCuenta("20100", "1101001", "Caja moneda nacional", "1", "110100", "CLP",
                "N", "N", "Y", "Y", "N"));
        l.add(new BroadCuenta("20100", "2101005", "Proveedores Compañía Ñuñoa", "2", "210100", "USD",
                "Y", "N", "N", "N", "Y"));
        // TipoCuenta null (SQL does not IFNULL GroupMask) and a non-Latin-1 char (euro sign)
        l.add(new BroadCuenta("20100", "3101001", "Capital €uro", null, "", "",
                "N", "N", "N", "N", "N"));
        return l;
    }

    static List<BroadMovimiento> movimientosBroad() {
        List<BroadMovimiento> l = new ArrayList<>();
        // numRegistro "IGNORED": legacy writes the contador in that column, never the row value
        l.add(new BroadMovimiento("1", "B1", "20100", LocalDate.of(2026, 10, 12), 1435, "5001", "1", "IGNORED",
                "1101001", "1", "PD01", "", LocalDate.of(2026, 10, 12), "13", "F-1001", "", "D", "CLP",
                new BigDecimal("1500.0000"), "N", "", "", ""));
        l.add(new BroadMovimiento("1", "B1", "20100", LocalDate.of(2026, 10, 12), 1435, "5001", "2", "IGNORED",
                "2101005", "2", "", "CC10", LocalDate.of(2026, 10, 11), "13", "F-1001", "0001", "C", "CLP",
                new BigDecimal("1500.0000"), "N", "", "", ""));
        l.add(new BroadMovimiento("1", "B1", "20100", LocalDate.of(2026, 10, 12), 905, "5002", "1", "IGNORED",
                "1101001", "1", "PD01", "", LocalDate.of(2026, 10, 12), "", "", "", "D", "USD",
                new BigDecimal("250.5000"), "Y", "X", "", ""));
        return l;
    }

    static List<BroadSaldo> saldosBroad() {
        List<BroadSaldo> l = new ArrayList<>();
        l.add(new BroadSaldo("1", "B1", "20100", LocalDate.of(2026, 10, 12), "IGNORED", "1101001", "1", "PD01", "",
                "CLP", new BigDecimal("98765.4321"), "", ""));
        l.add(new BroadSaldo("1", "B1", "20100", LocalDate.of(2026, 10, 12), "IGNORED", "2101005", "2", "", "CC10",
                "USD", new BigDecimal("0.0000"), "", ""));
        return l;
    }

    static List<Entidad> entidades() {
        return new ArrayList<>(List.of(
                new Entidad("20100", "Corredora Ñuñoa S.A."),
                new Entidad("20200", "Agencia Valores")));
    }

    static List<TipoCuenta> tiposCuenta() {
        return new ArrayList<>(List.of(
                new TipoCuenta("1", "Activo"),
                new TipoCuenta("2", "Pasivo"),
                new TipoCuenta("3", "Patrimonio")));
    }

    /** ASCII only: legacy writes B1_DES_CC_PROD.TXT with the platform default charset (flagged). */
    static List<CentroCosto> centrosCosto() {
        return new ArrayList<>(List.of(
                new CentroCosto("PD01", "Producto renta fija", "PD"),
                new CentroCosto("CC10", "Centro costo operaciones", "CC")));
    }

    static List<CuentaBpc> cuentasBpc() {
        return new ArrayList<>(List.of(
                new CuentaBpc("2010", "1101001", "Caja moneda nacional", "1", "110100"),
                new CuentaBpc("2010", "2101005", "Proveedores Compañía", "2", "210100")));
    }

    static List<MovimientoBpc> movimientosBpc() {
        return new ArrayList<>(List.of(
                new MovimientoBpc("2010", "1101001", "K4", "2026010", "5001", "1", "SB", "0L", "", "CLP",
                        new BigDecimal("1500.0000"), new BigDecimal("0.0000"), "", ""),
                new MovimientoBpc("2010", "2101005", "K4", "2026010", "5001", "2", "SB", "0L", "0001", "CLP",
                        new BigDecimal("0.0000"), new BigDecimal("1500.0000"), "", ""),
                new MovimientoBpc("2010", "1101001", "K4", "2026010", "5002", "1", "SB", "0L", "", "USD",
                        new BigDecimal("-25.5000"), new BigDecimal("0.0000"), "", "X")));
    }

    // ------------------------------------------------------------------ fake SAP source

    enum Consulta { CUENTAS_BROAD, MOVIMIENTOS_BROAD, SALDOS_BROAD, ENTIDADES, TIPOS_CUENTA, CENTROS_COSTO,
        CUENTAS_BPC, MOVIMIENTOS_BPC }

    /** Records each query and the fecha it received; can be told to fail specific queries. */
    static final class FakeSapSource implements InterfazSapSource {
        List<BroadCuenta> cuentasBroad = InterfazFixtures.cuentasBroad();
        List<BroadMovimiento> movimientosBroad = InterfazFixtures.movimientosBroad();
        List<BroadSaldo> saldosBroad = InterfazFixtures.saldosBroad();
        List<Entidad> entidades = InterfazFixtures.entidades();
        List<TipoCuenta> tiposCuenta = InterfazFixtures.tiposCuenta();
        List<CentroCosto> centrosCosto = InterfazFixtures.centrosCosto();
        List<CuentaBpc> cuentasBpc = InterfazFixtures.cuentasBpc();
        List<MovimientoBpc> movimientosBpc = InterfazFixtures.movimientosBpc();

        final Set<Consulta> fallar = EnumSet.noneOf(Consulta.class);
        final List<String> llamadas = Collections.synchronizedList(new ArrayList<>());

        private void registrar(Consulta c, LocalDate fecha) {
            llamadas.add(fecha == null ? c.name() : c.name() + "@" + fecha);
            if (fallar.contains(c)) {
                throw new IllegalStateException("SAP B1 no disponible (fake) en " + c);
            }
        }

        @Override public List<BroadCuenta> cuentasBroad() { registrar(Consulta.CUENTAS_BROAD, null); return cuentasBroad; }
        @Override public List<BroadMovimiento> movimientosBroad(LocalDate f) { registrar(Consulta.MOVIMIENTOS_BROAD, f); return movimientosBroad; }
        @Override public List<BroadSaldo> saldosBroad(LocalDate f) { registrar(Consulta.SALDOS_BROAD, f); return saldosBroad; }
        @Override public List<Entidad> entidades() { registrar(Consulta.ENTIDADES, null); return entidades; }
        @Override public List<TipoCuenta> tiposCuenta() { registrar(Consulta.TIPOS_CUENTA, null); return tiposCuenta; }
        @Override public List<CentroCosto> centrosCosto() { registrar(Consulta.CENTROS_COSTO, null); return centrosCosto; }
        @Override public List<CuentaBpc> cuentasBpc() { registrar(Consulta.CUENTAS_BPC, null); return cuentasBpc; }
        @Override public List<MovimientoBpc> movimientosBpc(LocalDate f) { registrar(Consulta.MOVIMIENTOS_BPC, f); return movimientosBpc; }
    }

    // ------------------------------------------------------------------ goldens & IO helpers

    /**
     * Golden bytes produced by running the legacy formatting statements. A missing golden is a
     * FAILURE, never a skip.
     */
    static byte[] golden(String relative) {
        String resource = "/interfaz/golden/" + relative;
        try (InputStream in = InterfazFixtures.class.getResourceAsStream(resource)) {
            if (in == null) {
                fail("Golden fixture missing from test classpath: " + resource
                        + " (regenerate from legacy oracle; see interfaz/README.md)");
            }
            return in.readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static byte[] leer(Path file) {
        if (!Files.exists(file)) {
            fail("Expected output file was not written: " + file);
        }
        try {
            return Files.readAllBytes(file);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Decodes ISO-8859-1 (1 byte == 1 char) so column positions equal byte offsets. */
    static String texto(Path file) {
        return new String(leer(file), StandardCharsets.ISO_8859_1);
    }

    static List<String> lineas(Path file) {
        return List.of(texto(file).split("\n", -1));
    }

    static String pad(String s, int width) {
        return String.format("%-" + width + "s", s);
    }

    static byte[] concat(byte[] a, byte[] b) {
        byte[] r = new byte[a.length + b.length];
        System.arraycopy(a, 0, r, 0, a.length);
        System.arraycopy(b, 0, r, a.length, b.length);
        return r;
    }
}
