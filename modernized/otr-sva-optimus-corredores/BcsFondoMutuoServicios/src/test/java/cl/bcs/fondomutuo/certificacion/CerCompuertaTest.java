package cl.bcs.fondomutuo.certificacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import cl.bcs.fondomutuo.adapter.in.javacall.JavaCallDispatcher;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.Entrada;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.LogFila;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.Salida;
import cl.bcs.fondomutuo.kit.Compuertas;

/**
 * T1 does NOT serve the class: {@code CertificarSolicitudAporteRescate} cannot be served until T5 (the whole of the phases of its section 7 behind one facade), so this package has NO JavaCall
 * handler, NO allow-list entry, NO list of the Manejador and NO gate flag yet (decision 5 of the plan of W3). What these tests pin is the SHAPE of what T1 does ship, the things that make the
 * later fronts safe to add, and the rules of the project that apply to it from day one:
 * <ul>
 * <li>the class is not reachable from outside (allow-list, handler, gate list), and the proposed gates G6 (Facturacion) and G8 (notifications) are marked pending, never silently absent;</li>
 * <li>a NEW plain Gson per call (decision 6), no mutable static state in the classes of the package (the legacy {@code ConfiguracionSistema} cache is a known defect, D-NOR-10, not copied);</li>
 * <li>the 11 SQL files that F-A / F-B run are the legacy ones, byte for byte (SHA-256 prefix of {@code RULES_FONDOMUTUO_F3_B.md} section 4);</li>
 * <li>the seam is a single-method port, and the facade writes nothing before it but the log of the early exit (one batch);</li>
 * <li>RULE-076: nothing from the legacy {@code main()} (connection aliases, real-looking ids of a solicitud) and no credential-looking literal is in the fixtures.</li>
 * </ul>
 */
class CerCompuertaTest {

    private static final String FQN_LEGACY = "cl.bcs.fondomutuo.CertificarSolicitudAporteRescate";

    @AfterEach
    void limpiar() {
        SujetoCer.envolverEscritura = null;
    }

    @Test
    @DisplayName("FM-CER-01 / decision 5: the legacy class is NOT on the allow-list nor in the read classes, and there is no handler class yet (it cannot be served until T5)")
    void claseNoServida() {
        assertFalse(JavaCallDispatcher.LISTA_BLANCA.contains(FQN_LEGACY), "the class must not be on the allow-list in T1");
        assertFalse(JavaCallDispatcher.CLASES_LECTURA.contains(FQN_LEGACY));
        assertThrows(ClassNotFoundException.class, () -> Class.forName("cl.bcs.fondomutuo.certificacion.CertificarAporteRescateJavaCallHandler"));
        assertThrows(ClassNotFoundException.class, () -> Class.forName("cl.bcs.fondomutuo.certificacion.CertificarAporteRescateConfiguracion"));
        assertFalse(new Compuertas("", false, false, false, false, false).listasServidas().stream().anyMatch(l -> l.contains("CERTIFICAR")), "no list of the Manejador for Certificar");
    }

    @Test
    @Disabled("pending T5 (FM-CER-01..73 all fronts): JavaCall handler CertificarAporteRescateJavaCallHandler, allow-list entry FQN_LEGACY and the list CERTIFICAR_SOLICITUD_APORTE_RESCATE, closed by default")
    @DisplayName("T5: the handler and its list exist, closed by default (503 G0 before anything is read or written)")
    void handlerYListaPendientesT5() {
    }

    @Test
    @Disabled("pending T4 (FM-CER-48..51, gate G6): Compuertas.facturacionHabilitada() closed by default for CrearMovimientoComprobante / CrearComprobante")
    @DisplayName("T4 / G6: Facturacion (CrearMovimientoComprobante, CrearComprobante) behind a closed gate")
    void compuertaG6PendienteT4() {
    }

    @Test
    @Disabled("pending T5 (FM-CER-62, 65, gate G8): Compuertas.notificacionesHabilitadas() closed by default for CallBatchAsync (NotificarEstadoOrdenFondoMutuo) and OWP")
    @DisplayName("T5 / G8: the asynchronous notification and OWP behind a closed gate")
    void compuertaG8PendienteT5() {
    }

    @Test
    @DisplayName("decision 6: the Gson of the package is a NEW plain Gson per call (never a shared field) and has the legacy plain format: field order, no nulls, no date format")
    void gsonNuevoPorLlamada() {
        assertNotSame(CertificarAporteRescateJson.gson(), CertificarAporteRescateJson.gson());
        assertEquals("{\"P_OUT_ERRORESCONTADOR\":0,\"P_OUT_MENSAJES\":[],\"P_OUT_RESULTADO\":[]}", CertificarAporteRescateJson.gson().toJson(new Salida()));
        Entrada e = CertificarAporteRescateJson.gson().fromJson("{\"p_IdEmpresa\":1,\"p_Solicitudes\":[{\"IdSolicitudAporteRescate\":7}]}", Entrada.class);
        assertEquals("S", e.p_NotificaOWP);
        assertEquals("N", e.p_CertificacionAporteSwitchSimulado);
        assertEquals("", e.p_Solicitudes.get(0).IdTipoCanalIngreso);
        assertEquals("{\"p_IdEmpresa\":1,\"p_NotificaOWP\":\"S\",\"p_CertificacionAporteSwitchSimulado\":\"N\",\"p_Solicitudes\":[{\"IdSolicitudAporteRescate\":7,\"IdTipoCanalIngreso\":\"\"}]}",
                CertificarAporteRescateJson.gson().toJson(e));
    }

    @Test
    @DisplayName("D-NOR-10 no state shared between requests: every static field of the classes of the package is final and is not a Gson, a Map or a List that a request could fill")
    void sinEstadoEstatico() throws Exception {
        List<String> malos = new ArrayList<>();
        for (Class<?> c : new Class<?>[] { CertificarAporteRescateService.class, CertificarAporteRescateFabrica.class, CertificarAporteRescateReglas.class, CertificarAporteRescateDatosJdbc.class,
                CertificarAporteRescateEscrituraJdbc.class, CertificarAporteRescateJson.class, CertificarAporteRescateContexto.class }) {
            for (Field f : c.getDeclaredFields()) {
                if (!Modifier.isStatic(f.getModifiers()) || f.isSynthetic()) continue;
                boolean mutable = !Modifier.isFinal(f.getModifiers()) || com.google.gson.Gson.class.isAssignableFrom(f.getType()) || java.util.Collection.class.isAssignableFrom(f.getType())
                        || java.util.Map.class.isAssignableFrom(f.getType());
                if (mutable) malos.add(c.getSimpleName() + "." + f.getName());
            }
        }
        assertTrue(malos.isEmpty(), "mutable static state: " + malos);
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource({ "CertificarSolicitudAporteRescate_SelectSolicitudes.sql, e0ae667cab0ac5ec", "CertificarSolicitudAporteRescate_SelectEstados.sql, 9807520638771402",
            "CertificarSolicitudAporteRescate_SelectComprobantesNoLiquidados.sql, a424da79df80f92b", "CertificarSolicitudAporteRescate_UpdateSolicitudes.sql, 3a0837e18309caed",
            "CertificarSolicitudAporteRescate_ConsultaTraspasoAPVAsociado.sql, fc7dffb634319ddb", "CertificarSolicitudAporteRescate_ConsultaTraspasoNoAPVAsociado.sql, 1f61886f3779b728",
            "CertificarSolicitudAporteRescate_SelectPersonaPreferenciaPagoTGR.sql, 1ee1c385a0ea2dd0", "CertificarSolicitudAporteRescate_SelectCobroIva.sql, 0016baa444b5263e",
            "ComunesSolicitudes_SelectIndicadoresPortafolioFFMM.sql, b5365d78d1b221f9", "AgregarLogCertificacionAporteRescate_InsertLogCertificacion.sql, 6d983aef2e7cce78",
            "CrearSolicitudAporteRescateNormal_SelectParametroOperaConSaldoNegativo.sql, 450d0a5a042ba216" })
    @DisplayName("RULES_FONDOMUTUO_F3_B.md 4: the SQL that F-A / F-B run is the legacy file, byte for byte (SHA-256 prefix)")
    void sqlVerbatim(String archivo, String prefijo) throws Exception {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("cl/bcs/fondomutuo/sql/" + archivo.strip())) {
            assertTrue(in != null, archivo + " is not in the module resources");
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(in.readAllBytes());
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) hex.append(String.format("%02x", b));
            assertEquals(prefijo.strip(), hex.substring(0, 16), archivo);
        }
    }

    @Test
    @DisplayName("seam: FasesPosteriores is a port with ONE method (continuar(contexto)); the facade is a port with ONE method (logica(entrada)); the context has no setter-style state of its own")
    void costuraDeUnSoloMetodo() {
        assertTrue(CertificarAporteRescateFasesPosteriores.class.isInterface());
        assertEquals(1, CertificarAporteRescateFasesPosteriores.class.getDeclaredMethods().length);
        Method m = CertificarAporteRescateFasesPosteriores.class.getDeclaredMethods()[0];
        assertEquals("continuar", m.getName());
        assertEquals(CertificarAporteRescateContexto.class, m.getParameterTypes()[0]);
        assertEquals(Salida.class, m.getReturnType());
        assertTrue(CertificarAporteRescatePort.class.isInterface());
        assertEquals(1, CertificarAporteRescatePort.class.getDeclaredMethods().length);
        assertEquals("logica", CertificarAporteRescatePort.class.getDeclaredMethods()[0].getName());
    }

    @Test
    @DisplayName("FM-CER-14: the early exit writes ONE batch of the log (one row per solicitud, in the order of the output, ids from the sequence port); a case that continues writes NOTHING before the seam")
    void soloElLogDeLaSalidaTemprana() throws Exception {
        List<List<LogFila>> lotes = new ArrayList<>();
        SujetoCer.envolverEscritura = real -> filas -> {
            lotes.add(new ArrayList<>(filas));
            real.insertarLog(filas);
        };
        CasoCer.ejecutar(CasoCer.idPorSlug("salida-temprana-lote-de-cinco"));
        assertEquals(1, lotes.size());
        assertEquals(5, lotes.get(0).size());
        List<Long> ids = new ArrayList<>();
        for (LogFila f : lotes.get(0)) ids.add(f.IdSolicitudAporteRescate);
        assertEquals(List.of(1003L, 1004L, 1001L, 1002L, 1005L), ids, "the rows of the log follow the rows of the output (APO first, then RES in table order)");
        assertEquals(List.of(1L, 2L, 3L, 4L, 5L), lotes.get(0).stream().map(f -> f.IdLogCertificacionAporteRescate).toList());
        assertEquals("N", lotes.get(0).get(0).Simulacion);
        lotes.clear();
        CasoCer.ejecutar(CasoCer.idPorSlug("contrato-minimo-con-defaults-parada"));
        assertTrue(lotes.isEmpty(), "nothing is written before the seam when a solicitud passes F-A");
        CasoCer.ejecutar(CasoCer.idPorSlug("ninguna-solicitud-existe"));
        assertTrue(lotes.isEmpty(), "no log when nothing is read");
        CasoCer.ejecutar(CasoCer.idPorSlug("usuario-inexistente"));
        assertTrue(lotes.isEmpty(), "no log when the requirements fail");
    }

    @Test
    @DisplayName("RULE-076: no id, connection alias or credential of the legacy main() / of a real environment is in the fixtures of the cases")
    void sinDatosRealesEnLosFixtures() throws Exception {
        Path recursos = CasoCer.recursos();
        List<String> prohibidos = List.of("17788687", "17405614", "qa01", "desa01", "presura", "connections.properties", "getPropertiesConnection", "password", "passwd", "secret", "apikey", "bearer ",
                "jdbc:sap", "jdbc:hdb", "https://", "http://");
        List<String> hallazgos = new ArrayList<>();
        try (Stream<Path> s = Files.walk(recursos)) {
            for (Path p : s.filter(Files::isRegularFile).toList()) {
                String texto = Files.readString(p, StandardCharsets.UTF_8).toLowerCase();
                for (String x : prohibidos) {
                    if (texto.contains(x)) hallazgos.add(recursos.relativize(p) + " contains '" + x + "'");
                }
            }
        }
        assertTrue(hallazgos.isEmpty(), "real-looking data in the fixtures: " + hallazgos);
    }

    @Test
    @DisplayName("the oracle stood on the real legacy class: every case directory has its case.json, seed.sql and expected.json and the golden says which runner recorded it")
    void losGoldensSeGrabaronConElOraculo() {
        for (String id : CasoCer.casos()) {
            JsonProvenance.verificar(id, CasoCer.golden(id));
        }
    }

    /** the golden of every case carries the provenance line written by OracleRunnerCer (never by hand) */
    private static final class JsonProvenance {
        static void verificar(String id, com.google.gson.JsonObject golden) {
            assertTrue(golden.has("_provenance") && golden.get("_provenance").getAsString().contains("runner-cer/OracleRunnerCer.java executing the unmodified legacy cl.bcs.fondomutuo.CertificarSolicitudAporteRescate"),
                    id + " :: golden without the provenance of the oracle");
        }
    }
}
