package cl.bcs.contabilidad.envio;

import cl.bcs.contabilidad.domain.model.sap.HomologaRespuestaSap;
import cl.bcs.contabilidad.domain.model.sap.ResultadoInterfazContable;
import cl.bcs.contabilidad.domain.port.out.InterfazContableRepository;
import cl.bcs.contabilidad.envio.support.CasoEnvio;
import cl.bcs.contabilidad.envio.support.FakeSapServiceLayer;
import cl.bcs.contabilidad.envio.support.FakeSapServiceLayer.Peticion;
import cl.bcs.contabilidad.envio.support.FakeSapServiceLayer.Respuesta;
import cl.bcs.contabilidad.envio.support.Fakes;
import cl.bcs.contabilidad.envio.support.JsonCanon;
import cl.bcs.contabilidad.infrastructure.sap.SapB1Config;
import cl.bcs.contabilidad.infrastructure.sap.SapB1Gateway;
import cl.bcs.contabilidad.service.InterfazContableService;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * End-to-end characterization of the "envío a SAP" job: legacy {@code cl.bcs.contabilidad.jobs.InterfazContable}
 * (logica/procesarPreComprobante, InterfazContable.java:80-249) + {@code CallerWSJournalVoucher}.
 *
 * <p>Wiring: REAL GenerarJsonAsientoSapService on H2 (case seed), REAL SapB1Gateway against a scripted
 * {@link FakeSapServiceLayer} (same script the legacy oracle ran), and a Mockito {@link InterfazContableRepository}
 * standing for the two write-backs legacy does: {@code CALL ITZ.ActualizarEstadoPreComprobante(idEmpresa, idUsuario,
 * 'CON', 'N', id, ...)} + commit on HTTP 204 (InterfazContable.java:228-245) and
 * {@code UPDATE ITZ.PreComprobante SET Estado='E'} for every failed id (:43, :174-176).
 *
 * <p>Compared with the golden: the SAP requests (order, path, JSON bodies), the run-log lines, the state
 * procedure calls and the ids marked 'E'.
 */
class InterfazContableServiceTest {

    static final List<String> CASOS = List.of(
            "CONT-B-40_sap-envio-ok-socio-nuevo",
            "CONT-B-41_sap-socio-existente",
            "CONT-B-42_sap-error-400-cuenta-5002",
            "CONT-B-43_sap-error-400-campo-1000",
            "CONT-B-44_sap-error-400-sin-homologar",
            "CONT-B-45_sap-lote-ok-y-error",
            "CONT-B-46_sap-error-crear-socio",
            "CONT-B-47_sap-error-verificar-socio",
            "CONT-B-48_sap-reintentos-5xx-asiento",
            "CONT-B-49_sap-login-fallido",
            "CONT-B-50_sap-precomprobante-sin-vista");

    private static final AtomicInteger EJECUTADOS = new AtomicInteger();

    record Corrida(ResultadoInterfazContable resultado, List<Peticion> peticiones, InterfazContableRepository repo, CasoEnvio caso) {
    }

    static Corrida ejecutarYComparar(String nombre) throws Exception {
        CasoEnvio caso = CasoEnvio.cargar(nombre);
        JsonObject esperado = caso.esperado();
        JsonObject entrada = caso.entrada();
        List<Long> secuencias = Arrays.stream(entrada.get("secuencias").getAsString().split(";")).map(Long::parseLong).toList();

        try (FakeSapServiceLayer sl = new FakeSapServiceLayer()) {
            for (Map.Entry<String, JsonElement> e : caso.caso().getAsJsonObject("sap").entrySet()) {
                List<Respuesta> rs = new ArrayList<>();
                for (JsonElement r : e.getValue().getAsJsonArray()) {
                    JsonObject ro = r.getAsJsonObject();
                    List<String> cookies = new ArrayList<>();
                    if (ro.has("setCookies")) {
                        ro.getAsJsonArray("setCookies").forEach(c -> cookies.add(c.getAsString()));
                    }
                    rs.add(new Respuesta(ro.get("status").getAsInt(), ro.has("body") ? ro.get("body").getAsString() : null, cookies));
                }
                sl.cuando(e.getKey(), rs.toArray(new Respuesta[0]));
            }
            // fake same-shape credentials; real values are read from the environment by the implementation
            SapB1Gateway gateway = new SapB1Gateway(new SapB1Config(sl.url(), "fake-b1-user", "fake-b1-pass-0000", "FAKECO", 10, Duration.ZERO));
            InterfazContableRepository repo = Mockito.mock(InterfazContableRepository.class);
            List<HomologaRespuestaSap> homologa = caso.jdbcTemplate().query(
                    "SELECT \"Codigo\", \"Valor\", \"Mensaje\" FROM \"BSCL.Contabilidad.BD::ITZ.HomologaRespuestaSap\"",
                    (rs, i) -> new HomologaRespuestaSap(rs.getLong(1), rs.getString(2), rs.getString(3)));
            Mockito.when(repo.consultarHomologaRespuestaSap()).thenReturn(homologa);

            InterfazContableService service = new InterfazContableService(
                    GenerarJsonAsientoSapServiceTest.servicio(caso), gateway, repo, Fakes.diccionario());

            assertThat(InterfazContableService.decodificarSecuencias(esperado.get("secCmbteBase64").getAsString()))
                    .as("SecCmbte is base64 of 'id;id' (InterfazContable.java:89-90)").isEqualTo(secuencias);

            ResultadoInterfazContable r = service.procesar(entrada.get("idEmpresa").getAsLong(), entrada.get("idUsuario").getAsLong(),
                    entrada.get("destino").getAsString(), secuencias);
            List<Peticion> peticiones = sl.peticiones();

            // 1. run-log lines (legacy file ITZ/<ts>_InterfazContable.log, one line per pre-voucher)
            List<String> lineas = Arrays.stream(esperado.get("logContenido").getAsString().split("\n")).filter(l -> !l.isEmpty()).toList();
            assertThat(r.lineasLog()).as(nombre + " log lines").isEqualTo(lineas);

            // 2. SAP requests: same sequence of endpoints, same JSON bodies
            List<String> rutasEsperadas = new ArrayList<>();
            esperado.getAsJsonArray("peticiones").forEach(p -> rutasEsperadas.add(
                    p.getAsJsonObject().get("metodo").getAsString() + " " + p.getAsJsonObject().get("ruta").getAsString()));
            assertThat(peticiones).extracting(Peticion::clave).as(nombre + " SAP requests").isEqualTo(rutasEsperadas);
            for (int i = 0; i < peticiones.size(); i++) {
                JsonElement cuerpo = esperado.getAsJsonArray("peticiones").get(i).getAsJsonObject().get("cuerpo");
                if (cuerpo != null && !cuerpo.isJsonNull()) {
                    JsonCanon.assertEquivalente(nombre + " body of " + rutasEsperadas.get(i), JsonCanon.parse(cuerpo.getAsString()),
                            JsonCanon.parse(peticiones.get(i).cuerpo()), "JournalEntryLines");
                }
                if (!rutasEsperadas.get(i).equals("POST /b1s/v1/Login")) {
                    // DEVIATION (flagged): legacy sends only the LAST Set-Cookie (golden: "ROUTEID=.node1"); modern must carry B1SESSION.
                    assertThat(peticiones.get(i).cookie()).as("session cookie").contains("B1SESSION=fake-session-0001");
                }
            }

            // 3. write-backs
            List<Long> contabilizados = new ArrayList<>();
            esperado.getAsJsonArray("procedimientos").forEach(c -> {
                JsonObject p = c.getAsJsonObject().getAsJsonObject("parametros");
                assertThat(p.get("3").getAsString()).isEqualTo("CON");
                assertThat(p.get("4").getAsString()).isEqualTo("N");
                contabilizados.add(p.get("5").getAsLong());
            });
            InOrder orden = Mockito.inOrder(repo);
            for (Long id : contabilizados) {
                orden.verify(repo).actualizarEstadoContabilizado(entrada.get("idEmpresa").getAsLong(), entrada.get("idUsuario").getAsLong(), id);
            }
            if (contabilizados.isEmpty()) {
                verify(repo, never()).actualizarEstadoContabilizado(anyLong(), anyLong(), anyLong());
            }
            List<Long> conError = new ArrayList<>();
            esperado.getAsJsonObject("tablas").getAsJsonArray("PreComprobante").forEach(row -> {
                JsonObject o = row.getAsJsonObject();
                if ("E".equals(o.get("Estado").getAsString())) {
                    conError.add(o.get("IdPreComprobante").getAsLong());
                }
            });
            List<Long> conErrorEnOrden = secuencias.stream().filter(conError::contains).toList();
            if (conErrorEnOrden.isEmpty()) {
                verify(repo, never()).marcarConError(anyList());
            } else {
                verify(repo).marcarConError(conErrorEnOrden);
            }
            assertThat(r.conError()).isEqualTo(conErrorEnOrden);
            assertThat(r.enviados()).isEqualTo(contabilizados);
            EJECUTADOS.incrementAndGet();
            return new Corrida(r, peticiones, repo, caso);
        }
    }

    @Test
    @DisplayName("CONT-B-40: partner missing (GET 404) -> POST BusinessPartners (201) -> POST JournalVouchersService_Add (204) -> ActualizarEstadoPreComprobante(1,21,'CON','N',100)")
    void contB40_envioOk_conSocioNuevo() throws Exception {
        // InterfazContable.java:202-224 (partners then voucher index 0), :228-245 (204 -> SP + commit), :123-124 (log line).
        Corrida c = ejecutarYComparar("CONT-B-40_sap-envio-ok-socio-nuevo");
        assertThat(c.resultado().lineasLog()).containsExactly("PRECOMPROBANTE ENVIADO EXISTOSAMENTE: 100");
    }

    @Test
    @DisplayName("CONT-B-41: partner exists (GET 200) -> not created; voucher 204")
    void contB41_socioExistente() throws Exception {
        Corrida c = ejecutarYComparar("CONT-B-41_sap-socio-existente");
        assertThat(c.peticiones()).extracting(Peticion::clave).doesNotContain("POST /b1s/v1/BusinessPartners");
    }

    @Test
    @DisplayName("CONT-B-42: voucher 400 code -5002 -> HomologaRespuestaSap message with {cuentaContable} = first quoted text; id marked E")
    void contB42_error400_5002() throws Exception {
        // InterfazContable.java:133-136 (code/value from body only when HTTP 400), :148-156 (-5002), :158 marked E.
        Corrida c = ejecutarYComparar("CONT-B-42_sap-error-400-cuenta-5002");
        assertThat(c.resultado().lineasLog()).containsExactly("ERROR AL ENVIAR PRECOMPROBANTE A SAP: 100, Mensaje: La cuenta SAP-4101 no es valida en SAP");
    }

    @Test
    @DisplayName("CONT-B-43: voucher 400 code -1000 -> message with {nombreCampo} = first quoted text")
    void contB43_error400_1000() throws Exception {
        // InterfazContable.java:138-146.
        Corrida c = ejecutarYComparar("CONT-B-43_sap-error-400-campo-1000");
        assertThat(c.resultado().lineasLog()).containsExactly("ERROR AL ENVIAR PRECOMPROBANTE A SAP: 100, Mensaje: El campo U_BCS_Nemo excede el largo permitido");
    }

    @Test
    @DisplayName("CONT-B-44: voucher 400 with no homologation row -> the raw SAP body is the message")
    void contB44_error400_sinHomologar() throws Exception {
        // InterfazContable.java:136 orElse(respuestaExcepcion.Mensaje).
        ejecutarYComparar("CONT-B-44_sap-error-400-sin-homologar");
    }

    @Test
    @DisplayName("CONT-B-45: batch '100;200' - a failure does not stop the batch; only failed ids are marked E, in one call")
    void contB45_loteOkYError() throws Exception {
        // InterfazContable.java:119-171 (per id try/catch), :173-176 (single batch UPDATE).
        Corrida c = ejecutarYComparar("CONT-B-45_sap-lote-ok-y-error");
        assertThat(c.resultado().enviados()).containsExactly(100L);
        assertThat(c.resultado().conError()).containsExactly(200L);
    }

    @Test
    @DisplayName("CONT-B-46: partner creation fails -> error code is the GET status 404 (not 400) and is homologated through the row (404, '')")
    void contB46_errorCrearSocio() throws Exception {
        // InterfazContable.java:207-211 builds Respuesta(respuestaExisteClienteSap.Codigo, ...); "".contains("") matches Valor ''.
        Corrida c = ejecutarYComparar("CONT-B-46_sap-error-crear-socio");
        assertThat(c.resultado().lineasLog()).containsExactly("ERROR AL ENVIAR PRECOMPROBANTE A SAP: 100, Mensaje: Socio de negocio no pudo ser creado");
    }

    @Test
    @DisplayName("CONT-B-47: partner check answers 403 -> 'Error al verificar si existe socio <CardCode> - {Codigo,Mensaje}'; no voucher sent")
    void contB47_errorVerificarSocio() throws Exception {
        // InterfazContable.java:216-220.
        ejecutarYComparar("CONT-B-47_sap-error-verificar-socio");
    }

    @Test
    @DisplayName("CONT-B-48: voucher 503 forever -> 10 attempts, message 'Error al enviar asiento, maximo de intentos superado'")
    void contB48_reintentos5xx() throws Exception {
        // CallerWSJournalVoucher.java:277-291.
        Corrida c = ejecutarYComparar("CONT-B-48_sap-reintentos-5xx-asiento");
        assertThat(c.peticiones()).filteredOn(p -> p.clave().equals("POST /b1s/v1/JournalVouchersService_Add")).hasSize(10);
    }

    @Test
    @DisplayName("CONT-B-49: login fails -> one line with dictionary UI_ERROR_CONEXION_SAP, every id marked E, nothing generated or sent")
    void contB49_loginFallido() throws Exception {
        // InterfazContable.java:104-113 (login inside try; all ids to secuenciasError), :118 (loop skipped).
        Corrida c = ejecutarYComparar("CONT-B-49_sap-login-fallido");
        assertThat(c.resultado().lineasLog()).containsExactly("ERROR AL ENVIAR PRECOMPROBANTE A SAP. MENSAJE: [UI_ERROR_CONEXION_SAP]{}");
        assertThat(c.resultado().conError()).containsExactly(100L, 200L);
    }

    @Test
    @DisplayName("CONT-B-50: pre-voucher absent from the envio view -> no voucher, line 'ERROR AL ENVIAR PRECOMPROBANTE A SAP: <id>' (no Mensaje), marked E")
    void contB50_sinCabeceraEnVista() throws Exception {
        // InterfazContable.java:224 get(0) on an empty list; the non-JSON exception message falls to the inner catch :162-166.
        Corrida c = ejecutarYComparar("CONT-B-50_sap-precomprobante-sin-vista");
        assertThat(c.resultado().lineasLog()).containsExactly("ERROR AL ENVIAR PRECOMPROBANTE A SAP: 555");
    }

    @AfterAll
    static void reportarEjecutados() {
        System.out.println("equivalence cases executed (InterfazContable): " + EJECUTADOS.get() + "/" + CASOS.size());
        assertThat(EJECUTADOS.get()).as("equivalence cases executed; zero means NOT PROVEN").isPositive();
    }
}
