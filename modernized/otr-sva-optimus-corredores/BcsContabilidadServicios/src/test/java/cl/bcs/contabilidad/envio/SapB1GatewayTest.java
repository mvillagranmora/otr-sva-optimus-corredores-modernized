package cl.bcs.contabilidad.envio;

import cl.bcs.contabilidad.domain.model.sap.BusinessPartner;
import cl.bcs.contabilidad.domain.model.sap.JournalVoucher;
import cl.bcs.contabilidad.domain.model.sap.SapException;
import cl.bcs.contabilidad.domain.model.sap.SapRespuesta;
import cl.bcs.contabilidad.domain.model.sap.SapSesion;
import cl.bcs.contabilidad.domain.service.AsientoSapMapper;
import cl.bcs.contabilidad.envio.support.CasoEnvio;
import cl.bcs.contabilidad.envio.support.FakeSapServiceLayer;
import cl.bcs.contabilidad.envio.support.FakeSapServiceLayer.Peticion;
import cl.bcs.contabilidad.envio.support.FakeSapServiceLayer.Respuesta;
import cl.bcs.contabilidad.envio.support.JsonCanon;
import cl.bcs.contabilidad.infrastructure.sap.SapB1Config;
import cl.bcs.contabilidad.infrastructure.sap.SapB1Gateway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * HTTP contract of the SAP B1 Service Layer client, characterized from legacy
 * {@code cl.bcs.contabilidad.jobs.CallerWSJournalVoucher} (BcsContabilidadJobs). Runs against
 * {@link FakeSapServiceLayer}; nothing real is contacted. Credentials are clearly fake values of the
 * legacy shape (B1_USER / B1_PASS / B1_COMPANY in interfazSAP.properties); real ones come from the
 * environment in the implementation and are never part of a fixture.
 *
 * <p>Retry policy per endpoint (MAXIMO_INTENTOS = 10, CallerWSJournalVoucher.java:29). A status is retried
 * when it is NOT in the endpoint's accepted set; after 10 attempts a {@link SapException} carries
 * {@code {Codigo: last status, Mensaje: fixed text}}.
 */
class SapB1GatewayTest {

    private static final String USUARIO = "fake-b1-user";        // fake, same shape as B1_USER
    private static final String CLAVE = "fake-b1-pass-0000";     // fake, same shape as B1_PASS
    private static final String COMPANY = "FAKECO";              // fake, same shape as B1_COMPANY
    private static final String LOGIN = "POST /b1s/v1/Login";
    private static final Respuesta LOGIN_OK = new Respuesta(200, "{\"SessionId\":\"fake-session-0001\"}",
            List.of("B1SESSION=fake-session-0001; HttpOnly", "ROUTEID=.node1; path=/b1s"));

    private FakeSapServiceLayer sl;
    private SapB1Gateway gateway;

    @BeforeEach
    void setUp() throws Exception {
        sl = new FakeSapServiceLayer();
        gateway = new SapB1Gateway(new SapB1Config(sl.url(), USUARIO, CLAVE, COMPANY, 10, Duration.ZERO));
    }

    @AfterEach
    void tearDown() {
        sl.close();
    }

    private SapSesion sesion() {
        sl.cuando(LOGIN, LOGIN_OK);
        return gateway.login();
    }

    private static JournalVoucher voucher() {
        CasoEnvio caso = CasoEnvio.cargar("CONT-B-20_sap-voucher-detallado-cl");
        return GenerarJsonAsientoSapServiceTest.servicio(caso).generar(1L, List.of(100L)).vouchers().get(0);
    }

    @Test
    @DisplayName("CONT-B-60: login POSTs {UserName,Password,CompanyDB} as application/json to /b1s/v1/Login; later calls send the B1SESSION cookie name=value only")
    void contB60_loginYCookieDeSesion() {
        // CallerWSJournalVoucher.java:32-110 (login), :130-132/:187-189/:252-254 (Cookie = cookie.split(";")[0]).
        SapSesion s = sesion();
        sl.cuando("GET /b1s/v1/BusinessPartners('CL1-9')", Respuesta.of(200, "{\"CardCode\":\"CL1-9\"}"));
        gateway.consultarSocio(s, "CL1-9");

        Peticion login = sl.peticiones().get(0);
        assertThat(login.clave()).isEqualTo(LOGIN);
        assertThat(login.contentType()).startsWith("application/json");
        JsonCanon.assertEquivalente("login body",
                JsonCanon.parse("{\"UserName\":\"fake-b1-user\",\"Password\":\"fake-b1-pass-0000\",\"CompanyDB\":\"FAKECO\"}"),
                JsonCanon.parse(login.cuerpo()));
        Peticion get = sl.peticiones().get(1);
        assertThat(get.cookie()).contains("B1SESSION=fake-session-0001").doesNotContain("HttpOnly").doesNotContain("path=");
    }

    @Test
    @DisplayName("CONT-B-61: login answering non-200 is attempted exactly 10 times, then SapException {Codigo: status, Mensaje: 'Error de conexión'}")
    void contB61_loginFallidoReintenta10Veces() {
        // CallerWSJournalVoucher.java:53-107 (sleep 2 s between attempts; configurable here, default checked in CONT-B-65).
        sl.cuando(LOGIN, Respuesta.of(401, "{\"error\":{\"code\":100000027}}"));
        assertThatThrownBy(() -> gateway.login())
                .isInstanceOfSatisfying(SapException.class, e -> {
                    assertThat(e.respuesta()).isEqualTo(new SapRespuesta(401, "Error de conexión"));
                    assertThat(e.getMessage()).isEqualTo("{\"Codigo\":401,\"Mensaje\":\"Error de conexión\"}");
                });
        assertThat(sl.contar(LOGIN)).isEqualTo(10);
    }

    @Test
    @DisplayName("CONT-B-62: GET BusinessPartners('<CardCode>') returns 200/400/403/404 at once with the body; any other status is retried 10 times then SapException")
    void contB62_consultarSocio() {
        // CallerWSJournalVoucher.java:113-165.
        SapSesion s = sesion();
        sl.cuando("GET /b1s/v1/BusinessPartners('A')", Respuesta.of(404, "{\"error\":{\"code\":-2028}}"));
        sl.cuando("GET /b1s/v1/BusinessPartners('B')", Respuesta.of(403, "{\"error\":{\"code\":-1}}"));
        sl.cuando("GET /b1s/v1/BusinessPartners('C')", Respuesta.of(500, "{\"error\":\"proxy\"}"));

        assertThat(gateway.consultarSocio(s, "A")).isEqualTo(new SapRespuesta(404, "{\"error\":{\"code\":-2028}}"));
        assertThat(gateway.consultarSocio(s, "B").Codigo()).isEqualTo(403);
        assertThatThrownBy(() -> gateway.consultarSocio(s, "C")).isInstanceOfSatisfying(SapException.class, e ->
                assertThat(e.respuesta()).isEqualTo(new SapRespuesta(500, "Error al solicitar informacion de socio, maximo de intentos superado")));
        assertThat(sl.contar("GET /b1s/v1/BusinessPartners('A')")).isEqualTo(1);
        assertThat(sl.contar("GET /b1s/v1/BusinessPartners('C')")).isEqualTo(10);
    }

    @Test
    @DisplayName("CONT-B-63: POST BusinessPartners sends the partner JSON; 200/201/400/403 return at once; 404 IS retried (10x) then SapException")
    void contB63_crearSocio() {
        // CallerWSJournalVoucher.java:167-231 (accepted set 200,201,400,403).
        SapSesion s = sesion();
        BusinessPartner bp = BusinessPartner.of("CL1-9", "1-9", "Cliente Uno SpA", "cCustomer", BusinessPartner.CLIENTE);
        sl.cuando("POST /b1s/v1/BusinessPartners", Respuesta.of(201, "{\"CardCode\":\"CL1-9\"}"), Respuesta.of(404, "{\"error\":{}}"));

        assertThat(gateway.crearSocio(s, bp).Codigo()).isEqualTo(201);
        JsonCanon.assertEquivalente("partner body", JsonCanon.parse(
                "{\"CardCode\":\"CL1-9\",\"CardName\":\"Cliente Uno SpA\",\"CardType\":\"cCustomer\",\"FederalTaxID\":\"1-9\","
                        + "\"Address\":\"Adress\",\"City\":\"City\",\"County\":\"County\",\"GroupCode\":\"100\",\"Currency\":\"##\"}"),
                JsonCanon.parse(sl.peticiones().get(1).cuerpo()));
        assertThatThrownBy(() -> gateway.crearSocio(s, bp)).isInstanceOfSatisfying(SapException.class, e ->
                assertThat(e.respuesta()).isEqualTo(new SapRespuesta(404, "Error al crear socio, maximo de intentos superado")));
        assertThat(sl.contar("POST /b1s/v1/BusinessPartners")).isEqualTo(11);
    }

    @Test
    @DisplayName("CONT-B-64: POST JournalVouchersService_Add sends the voucher JSON; 204 -> (204, \"\"); 400 -> (400, body) once; 403 is retried 10 times then SapException")
    void contB64_enviarAsiento() {
        // CallerWSJournalVoucher.java:233-294 (accepted set 200,400,204).
        SapSesion s = sesion();
        JournalVoucher v = voucher();
        String jv = "POST /b1s/v1/JournalVouchersService_Add";
        sl.cuando(jv, new Respuesta(204, null, List.of()),
                Respuesta.of(400, "{\"error\":{\"code\":-5002,\"message\":{\"value\":\"bad\"}}}"),
                Respuesta.of(403, "{\"error\":\"forbidden\"}"));

        assertThat(gateway.enviarAsiento(s, v)).isEqualTo(new SapRespuesta(204, ""));
        JsonCanon.assertEquivalente("voucher body", JsonCanon.parse(AsientoSapMapper.toSapJson(v)), JsonCanon.parse(sl.peticiones().get(1).cuerpo()));
        assertThat(gateway.enviarAsiento(s, v)).isEqualTo(new SapRespuesta(400, "{\"error\":{\"code\":-5002,\"message\":{\"value\":\"bad\"}}}"));
        assertThatThrownBy(() -> gateway.enviarAsiento(s, v)).isInstanceOfSatisfying(SapException.class, e ->
                assertThat(e.respuesta()).isEqualTo(new SapRespuesta(403, "Error al enviar asiento, maximo de intentos superado")));
        assertThat(sl.contar(jv)).isEqualTo(12);
    }

    @Test
    @DisplayName("CONT-B-65: defaults are 10 attempts and 2 s between login attempts; the configuration never renders the password")
    void contB65_configuracionPorDefectoYSinClaveEnTexto() {
        // CallerWSJournalVoucher.java:29 (10), :74 (Thread.sleep(2000)). Legacy :63 prints the login JSON with the
        // password to stdout - the modern client must not (security deviation, deliberate).
        assertThat(SapB1Config.MAXIMO_INTENTOS_DEFAULT).isEqualTo(10);
        assertThat(SapB1Config.ESPERA_REINTENTO_LOGIN_DEFAULT).isEqualTo(Duration.ofSeconds(2));
        assertThat(new SapB1Config(sl.url(), USUARIO, CLAVE, COMPANY, 10, Duration.ZERO).toString()).doesNotContain(CLAVE);
    }
}
