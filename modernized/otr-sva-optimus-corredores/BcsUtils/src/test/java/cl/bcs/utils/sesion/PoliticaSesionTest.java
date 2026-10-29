package cl.bcs.utils.sesion;

import cl.bcs.utils.equivalencia.Golden;
import cl.bcs.utils.equivalencia.Golden.Caso;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.ArrayDeque;
import java.util.Base64;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Session policy as pure functions (enforcement moves to the Platform Service in Phase 5). Golden: golden/db/session.json,
 * recorded by running the UNMODIFIED legacy Sesion.insertarSesion, SesionUtils and ProcesaLlamadaServletCall.logica on H2.
 *
 * <ul>
 *   <li>RULE-021 token format (Sesion.java:184-185): Base64(IdEmpresa + ":" + usuario + ":" + Timestamp.toString() + UUID).</li>
 *   <li>RULE-024 idle timeout per channel (SesionUtils.java:42-81): WS/CLI/AGE read PLA.IdleMaxTime&lt;canal&gt;; any other
 *       channel 1 800 000 ms; channel match is case-SENSITIVE; expired iff (now - lastUse) &gt; idle (strict).</li>
 *   <li>RULE-025 (reviewed WRONG, 2026-10-16): COR loses its exemption (ProcesaLlamadaServletCall.java:123, :170) and uses
 *       the same mechanism: parameter PLA.IdleMaxTimeCOR, idle check applied, last use updated. APPROVED DEVIATION.</li>
 *   <li>RULE-071 UTC cache (SesionUtils.java:83-108): one DB read per hour, then cached + elapsed.</li>
 * </ul>
 */
class PoliticaSesionTest {

    static final String AREA = "session";
    /** Not pure policy: DB row lookup (017), JVM-wide parameter cache (019), RULE-023 channel match (038), RULE-027 catalog (040). Phase 5. */
    static final Set<String> NO_APLICA = Set.of("SESSION-017", "SESSION-019", "SESSION-038", "SESSION-040");
    static final Set<String> COR = Set.of("SESSION-026", "SESSION-027", "SESSION-028", "SESSION-029", "SESSION-030", "SESSION-035");

    static Map<String, String> config(JsonElement e) {
        Map<String, String> m = new HashMap<>();
        if (e != null && e.isJsonObject()) for (Map.Entry<String, JsonElement> x : e.getAsJsonObject().entrySet()) m.put(x.getKey(), x.getValue().getAsString());
        return m;
    }

    static long idle(String canal, Map<String, String> cfg) {
        Optional<String> k = PoliticaInactividad.codigoParametro(canal);
        return PoliticaInactividad.idleMaxMs(canal, k.map(cfg::get).orElse(null));
    }

    static final long AHORA = Timestamp.valueOf("2024-03-15 12:00:00").getTime();

    void verificar(Caso c) throws Exception {
        String regla = c.regla();
        JsonObject out = c.out();
        if (regla.equals("RULE-021")) {
            JsonObject v = c.valor().getAsJsonObject();
            Timestamp ts = Timestamp.valueOf(c.s("currentTimestamp"));
            ts.setNanos((int) c.l("nanos"));
            Long emp = c.input().get("idEmpresa").isJsonNull() ? null : c.l("idEmpresa");
            UUID uuid = UUID.fromString(v.get("uuid").getAsString());
            assertThat(ts.toString()).isEqualTo(v.get("tsToString").getAsString());
            assertThat(TokenSesion.raw(emp, c.s("nombreUsuario"), ts, uuid)).isEqualTo(v.get("raw").getAsString());
            assertThat(TokenSesion.generar(emp, c.s("nombreUsuario"), ts, uuid)).isEqualTo(v.get("token").getAsString());
        } else if (c.es("fn", "GetParametro")) {
            String canal = c.s("canal");
            Map<String, String> cfg = config(c.input().get("config"));
            if (canal == null) {
                assertThat(out.get("throws").getAsString()).isEqualTo("java.lang.NullPointerException");
                assertThatThrownBy(() -> idle(null, cfg)).isInstanceOf(NullPointerException.class);
            } else if (c.es("config", "PLA.IdleMaxTimeWS = NULL")) {
                // legacy returns null and the caller's Long.parseLong (ProcesaLlamadaServletCall.java:123) fails
                assertThat(c.valor().isJsonNull()).isTrue();
                assertThatThrownBy(() -> PoliticaInactividad.idleMaxMs("WS", null)).isInstanceOf(NumberFormatException.class);
            } else if (canal.equals("COR")) {
                assertThat(c.valor().getAsString()).as("legacy: COR falls to the 1800000 default").isEqualTo("1800000");
                assertThat(PoliticaInactividad.codigoParametro("COR")).contains("PLA.IdleMaxTimeCOR");
                assertThat(idle("COR", cfg)).as("RULE-025 approved deviation: COR reads its own parameter").isEqualTo(300000L);
            } else {
                assertThat(idle(canal, cfg)).isEqualTo(Long.parseLong(c.valor().getAsString()));
            }
        } else if (regla.equals("RULE-025")) {
            String canal = PoliticaInactividad.canalEfectivo(c.s("canalLlamada"));
            long idleMs = c.l("idleMs");
            boolean expirada = PoliticaInactividad.sesionExpirada(canal, AHORA, AHORA - idleMs, idle(canal, config(c.input().get("config"))));
            JsonObject v = c.valor().getAsJsonObject();
            String fila = v.get("sesionDespues(TokenValido|FechaUltimoUso|UsoToken)").getAsString();
            if (COR.contains(c.id())) {
                assertThat(v.get("p_out_Mensaje").getAsString()).as("legacy: COR exempt").isEqualTo("OK++");
                assertThat(fila).as("legacy: COR last use not updated").isEqualTo("S|unchanged|0");
                boolean esperado = switch (c.id()) {
                    case "SESSION-026" -> false;          // 299 000 ms < PLA.IdleMaxTimeCOR 300 000
                    default -> true;                       // 301 000 ms, 30 min, 10 days; "cor" -> default 1 800 000; null -> COR
                };
                assertThat(expirada).as("RULE-025 approved deviation (ProcesaLlamadaServletCall.java:123)").isEqualTo(esperado);
                assertThat(PoliticaInactividad.actualizaUltimoUso(canal)).as("RULE-025: COR last use is updated now (ProcesaLlamadaServletCall.java:170)").isTrue();
            } else {
                assertThat(expirada).isEqualTo(v.get("p_out_Mensaje").getAsString().equals("TOKEN INVALIDO"));
                assertThat(fila.startsWith(expirada ? "N|" : "S|")).isTrue();
                assertThat(PoliticaInactividad.actualizaUltimoUso(canal)).isEqualTo(!fila.contains("unchanged"));
            }
        } else if (regla.equals("RULE-071")) {
            JsonObject v = c.valor().getAsJsonObject();
            if (v.has("firstCall")) {
                AtomicLong reloj = new AtomicLong(1_700_000_000_000L);
                Deque<Timestamp> db = new ArrayDeque<>(List.of(Timestamp.valueOf("2024-03-15 12:00:00.123456789"), Timestamp.valueOf("2030-01-01 00:00:00")));
                RelojUtcCacheado r = new RelojUtcCacheado(reloj::get, db::poll);
                Timestamp t0 = r.ahoraUtc();
                assertThat(Golden.json(Golden.d(t0))).isEqualTo(v.get("firstCall"));
                assertThat(t0.getTime() - Timestamp.valueOf("2024-03-15 12:00:00.123").getTime()).isEqualTo(v.get("firstCallMinusDbMs").getAsLong());
                assertThat(Math.abs(r.ahoraUtc().getTime() - t0.getTime()) < 5000).isEqualTo(v.get("afterDbChange_usesCache").getAsBoolean());
                reloj.addAndGet(3_599_000L);
                assertThat(Math.abs(r.ahoraUtc().getTime() - (t0.getTime() + 3_599_000L)) < 5000)
                        .isEqualTo(v.getAsJsonObject("ageJustUnderOneHour_usesCacheAndAdvances").get("cached").getAsBoolean());
                reloj.addAndGet(2_000L);
                assertThat(Math.abs(r.ahoraUtc().getTime() - Timestamp.valueOf("2030-01-01 00:00:00").getTime()) < 5000)
                        .isEqualTo(v.get("ageJustOverOneHour_refreshes").getAsBoolean());
            } else {
                AtomicLong reloj = new AtomicLong(1_700_000_000_000L);
                Deque<Optional<Timestamp>> db = new ArrayDeque<>(List.of(Optional.empty(), Optional.of(Timestamp.valueOf("2024-03-15 12:00:00"))));
                RelojUtcCacheado r = new RelojUtcCacheado(reloj::get, () -> db.poll().orElse(null));
                assertThat(Golden.run(r::ahoraUtc)).isEqualTo(v.get("dbReturnsNull"));
                reloj.addAndGet(1000);
                assertThat(Golden.run(() -> Math.abs(r.ahoraUtc().getTime() - Timestamp.valueOf("2024-03-15 12:00:00").getTime()) < 5000))
                        .isEqualTo(v.get("nextCallWithinHour_nearDbValue"));
            }
        } else {
            throw new AssertionError("unmapped golden case " + c.id());
        }
        Golden.contar(AREA);
    }

    @TestFactory
    @DisplayName("RULE-021/024/025/071 equivalence (with the RULE-025 approved deviation asserted case by case)")
    Stream<DynamicTest> sesion_equivalencia() {
        List<Caso> casos = Golden.casos("db", AREA);
        assertThat(casos).extracting(Caso::id).containsAll(NO_APLICA).containsAll(COR);
        return casos.stream().filter(c -> !NO_APLICA.contains(c.id())).map(c -> DynamicTest.dynamicTest(c.nombre(), () -> Golden.enZona("UTC", () -> verificar(c)))); // DB goldens were recorded in UTC
    }

    @AfterAll
    static void reporte() {
        System.out.println("equivalence cases executed [" + AREA + "]: " + Golden.ejecutados(AREA) + " (not applicable: " + NO_APLICA + ")");
    }

    // ------------------------------------------------------------------------------------------- RULE-021

    @Test
    @DisplayName("RULE-021: token = Base64(idEmpresa:usuario:Timestamp.toString()UUID), standard alphabet with padding, UTF-8")
    void rule021_formatoToken() {
        UUID u = UUID.fromString("00000000-0000-4000-8000-000000000001");
        Timestamp ts = Timestamp.valueOf("2024-03-15 10:20:30");
        String raw = "1:usuario.prueba:2024-03-15 10:20:30.000000000-0000-4000-8000-000000000001";
        assertThat(TokenSesion.raw(1L, "usuario.prueba", ts, u)).isEqualTo(raw);
        assertThat(TokenSesion.generar(1L, "usuario.prueba", ts, u)).isEqualTo(Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8)));
        assertThat(TokenSesion.raw(null, null, ts, u)).startsWith("null:null:");
    }

    @Test
    @DisplayName("RULE-021: STANDARD Base64 alphabet ('+', '/') with '=' padding, not URL-safe (commons-codec encodeBase64, Sesion.java:185)")
    void rule021_alfabetoEstandar() {
        UUID u = UUID.fromString("00000000-0000-4000-8000-000000000001");
        Timestamp ts = Timestamp.valueOf("2024-03-15 10:20:30");
        assertThat(TokenSesion.generar(1L, "x?>>", ts, u))
                .isEqualTo("MTp4Pz4+OjIwMjQtMDMtMTUgMTA6MjA6MzAuMDAwMDAwMDAwLTAwMDAtNDAwMC04MDAwLTAwMDAwMDAwMDAwMQ==");
        assertThat(TokenSesion.generar(1L, "ñ~?", ts, u))
                .isEqualTo("MTrDsX4/OjIwMjQtMDMtMTUgMTA6MjA6MzAuMDAwMDAwMDAwLTAwMDAtNDAwMC04MDAwLTAwMDAwMDAwMDAwMQ==");
    }

    // ------------------------------------------------------------------------------------------- RULE-024 / RULE-025

    @Test
    @DisplayName("RULE-024: parameter per channel, case-sensitive; unknown channels use 1 800 000 ms; strict '>' comparison")
    void rule024_parametroPorCanal() {
        assertThat(PoliticaInactividad.codigoParametro("WS")).contains("PLA.IdleMaxTimeWS");
        assertThat(PoliticaInactividad.codigoParametro("CLI")).contains("PLA.IdleMaxTimeCLI");
        assertThat(PoliticaInactividad.codigoParametro("AGE")).contains("PLA.IdleMaxTimeAGE");
        assertThat(PoliticaInactividad.codigoParametro("ws")).isEmpty();
        assertThat(PoliticaInactividad.codigoParametro("SSO")).isEmpty();
        assertThat(PoliticaInactividad.idleMaxMs("XYZ", "5")).isEqualTo(1_800_000L);
        assertThat(PoliticaInactividad.IDLE_MAX_POR_DEFECTO_MS).isEqualTo(1_800_000L);
        assertThat(PoliticaInactividad.idleMaxMs("WS", "600000")).isEqualTo(600_000L);
        assertThat(PoliticaInactividad.sesionExpirada("WS", 1_600_000, 1_000_000, 600_000)).isFalse();
        assertThat(PoliticaInactividad.sesionExpirada("WS", 1_600_001, 1_000_000, 600_000)).isTrue();
        assertThat(PoliticaInactividad.sesionExpirada("WS", 1_000_000, 1_005_000, 600_000)).isFalse();
        assertThat(PoliticaInactividad.canalEfectivo(null)).isEqualTo("COR");
        assertThat(PoliticaInactividad.canalEfectivo("CLI")).isEqualTo("CLI");
        assertThatThrownBy(() -> PoliticaInactividad.idleMaxMs("WS", "abc")).isInstanceOf(NumberFormatException.class);
    }

    @Test
    @DisplayName("RULE-025 approved deviation: COR is no longer exempt; it uses PLA.IdleMaxTimeCOR like WS/CLI/AGE use theirs")
    void rule025_corSinExencion() {
        assertThat(PoliticaInactividad.codigoParametro("COR")).contains("PLA.IdleMaxTimeCOR");
        assertThat(PoliticaInactividad.sesionExpirada("COR", 10_000_000L, 0L, PoliticaInactividad.idleMaxMs("COR", "300000"))).isTrue();
        assertThat(PoliticaInactividad.sesionExpirada("cor", 10L * 86_400_000L, 0L, PoliticaInactividad.idleMaxMs("cor", null))).isTrue();
        assertThat(PoliticaInactividad.actualizaUltimoUso("COR")).isTrue();
        assertThat(PoliticaInactividad.actualizaUltimoUso("WS")).isTrue();
    }

    // ------------------------------------------------------------------------------------------- RULE-071

    @Test
    @DisplayName("RULE-071: one DB read per hour (refresh when age > 3 600 000 ms, strict); value = cached + elapsed; nanos cut to millis")
    void rule071_cacheUnaHora() throws Exception {
        AtomicLong reloj = new AtomicLong(5_000_000L);
        AtomicInteger lecturas = new AtomicInteger();
        Timestamp db = Timestamp.valueOf("2024-03-15 12:00:00.123456789");
        RelojUtcCacheado r = new RelojUtcCacheado(reloj::get, () -> { lecturas.incrementAndGet(); return db; });
        assertThat(r.ahoraUtc()).isEqualTo(Timestamp.valueOf("2024-03-15 12:00:00.123"));
        reloj.addAndGet(3_600_000L);
        assertThat(r.ahoraUtc()).isEqualTo(Timestamp.valueOf("2024-03-15 13:00:00.123"));
        assertThat(lecturas).hasValue(1);
        reloj.addAndGet(1L);
        r.ahoraUtc();
        assertThat(lecturas).hasValue(2);
        assertThat(RelojUtcCacheado.VIGENCIA_MS).isEqualTo(3_600_000L);
    }

    @Test
    @DisplayName("RULE-071 legacy defect pinned: a NULL read poisons the cache for an hour (java.lang.Exception on every call); a failing read is retried next call")
    void rule071_nuloEnvenenaCache() throws Exception {
        AtomicLong reloj = new AtomicLong(5_000_000L);
        AtomicInteger lecturas = new AtomicInteger();
        RelojUtcCacheado r = new RelojUtcCacheado(reloj::get, () -> { lecturas.incrementAndGet(); return null; });
        assertThatThrownBy(r::ahoraUtc).isExactlyInstanceOf(Exception.class);
        reloj.addAndGet(60_000L);
        assertThatThrownBy(r::ahoraUtc).isExactlyInstanceOf(Exception.class);
        assertThat(lecturas).hasValue(1);

        AtomicInteger intentos = new AtomicInteger();
        RelojUtcCacheado f = new RelojUtcCacheado(reloj::get, () -> {
            if (intentos.incrementAndGet() == 1) throw new java.sql.SQLException("caida simulada");
            return Timestamp.valueOf("2024-03-15 12:00:00");
        });
        assertThatThrownBy(f::ahoraUtc).isInstanceOf(java.sql.SQLException.class);
        assertThat(f.ahoraUtc()).isEqualTo(Timestamp.valueOf("2024-03-15 12:00:00"));
    }
}
