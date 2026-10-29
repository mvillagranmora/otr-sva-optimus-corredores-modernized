package cl.bcs.tesoreria;

// to be created in Step 2 — infrastructure SOAP endpoints
import cl.bcs.tesoreria.infrastructure.soap.SvcRecaudaInqV1Endpoint;   // to be created in Step 2
import cl.bcs.tesoreria.infrastructure.soap.SvcRecaudaTrnV1Endpoint;   // to be created in Step 2
import cl.bcs.tesoreria.domain.port.out.TesoreriaRepository;
import cl.bcs.tesoreria.domain.port.out.PlataformaPort;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Characterization tests for SOAP contracts (T-06):
 *   SvcRecaudaInqV1  — BCP inquiry service
 *   SvcRecaudaTrnV1  — BCP transaction service
 *
 * Oracle:
 *   legacy SvcRecaudaInqV1_SoapHttpPortImpl  (targetNamespace = "http://mfxV2_0")
 *   legacy SvcRecaudaTrnV1_SoapHttpPortImpl  (targetNamespace = "http://mfxV2_0")
 *
 * Rules pinned:
 *   T-06-A  Both endpoints declare targetNamespace = "http://mfxV2_0" (must be
 *           preserved verbatim — breaking this breaks the BCP contract).
 *
 *   T-06-B  InqV1 CodigoRespuestaBCP enum values (all four must exist with exact codes):
 *             CP0000 → severity INFORMACION (valid depositor, no restriction)
 *             CP0010 → severity ERROR       (depositor does not exist)
 *             CP0141 → severity ERROR       (depositor restricted for inquiry)
 *             CP0138 → severity ERROR       (generic processing error)
 *
 *   T-06-C  TrnV1 CodigoRespuestaBCP enum values (all four must exist):
 *             CP0000 → severity INFORMACION (transaction OK)
 *             CP0139 → severity ERROR       (depositor restricted for extorno)
 *             CP0140 → severity ERROR       (depositor restricted for payment)
 *             CP0138 → severity ERROR       (generic processing error)
 *
 *   T-06-D  TrnV1 ServicioBCP_Moneda mapping:
 *             ServicioBCP_2000 → PEN
 *             ServicioBCP_2001 → USD
 *
 *   T-06-E  InqV1 endpoint returns a non-null response for a valid inquiry request
 *           (CP0000 path). The response must contain the success code CP0000.
 *
 *   T-06-F  InqV1 endpoint maps an unknown depositor to error CP0010.
 *
 *   T-06-G  TrnV1 endpoint returns CP0000 for a valid payment transaction.
 */
@ExtendWith(MockitoExtension.class)
class SoapContractTest {

    @Mock
    TesoreriaRepository repository;    // to be created in Step 2

    @Mock
    PlataformaPort plataforma;         // to be created in Step 2

    SvcRecaudaInqV1Endpoint inqEndpoint;   // to be created in Step 2
    SvcRecaudaTrnV1Endpoint trnEndpoint;   // to be created in Step 2

    @BeforeEach
    void setUp() {
        inqEndpoint = new SvcRecaudaInqV1Endpoint(plataforma, repository);
        trnEndpoint = new SvcRecaudaTrnV1Endpoint(plataforma, repository);
    }

    // -----------------------------------------------------------------------
    // T-06-A: Target namespace preserved
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("T-06-A: SvcRecaudaInqV1Endpoint declares targetNamespace http://mfxV2_0")
    void rule_t06a_inqEndpointDeclaresCorrectTargetNamespace() {
        // The annotation is on the class — verified via reflection to guarantee
        // it cannot be silently removed during refactoring.
        jakarta.jws.WebService ws = SvcRecaudaInqV1Endpoint.class
                .getAnnotation(jakarta.jws.WebService.class);
        assertThat(ws)
                .as("T-06-A: @WebService annotation must be present on SvcRecaudaInqV1Endpoint")
                .isNotNull();
        assertThat(ws.targetNamespace())
                .as("T-06-A: targetNamespace must be exactly http://mfxV2_0")
                .isEqualTo("http://mfxV2_0");
    }

    @Test
    @DisplayName("T-06-A: SvcRecaudaTrnV1Endpoint declares targetNamespace http://mfxV2_0")
    void rule_t06a_trnEndpointDeclaresCorrectTargetNamespace() {
        jakarta.jws.WebService ws = SvcRecaudaTrnV1Endpoint.class
                .getAnnotation(jakarta.jws.WebService.class);
        assertThat(ws)
                .as("T-06-A: @WebService annotation must be present on SvcRecaudaTrnV1Endpoint")
                .isNotNull();
        assertThat(ws.targetNamespace())
                .as("T-06-A: targetNamespace must be exactly http://mfxV2_0")
                .isEqualTo("http://mfxV2_0");
    }

    // -----------------------------------------------------------------------
    // T-06-B: InqV1 CodigoRespuestaBCP enum
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("T-06-B: SvcRecaudaInqV1 CodigoRespuestaBCP enum values")
    class InqV1CodigoRespuestaBCP {

        @Test
        @DisplayName("T-06-B: CP0000 has severity INFORMACION — valid depositor, no restriction")
        void rule_t06b_cp0000IsInformacion() {
            var code = SvcRecaudaInqV1Endpoint.CodigoRespuestaInqBCP.CP0000;
            assertThat(code.getCodigo()).isEqualTo("CP0000");
            assertThat(code.getSeveridad()).isEqualTo("INFORMACION");
        }

        @Test
        @DisplayName("T-06-B: CP0010 has severity ERROR — depositor does not exist")
        void rule_t06b_cp0010IsError() {
            var code = SvcRecaudaInqV1Endpoint.CodigoRespuestaInqBCP.CP0010;
            assertThat(code.getCodigo()).isEqualTo("CP0010");
            assertThat(code.getSeveridad()).isEqualTo("ERROR");
        }

        @Test
        @DisplayName("T-06-B: CP0141 has severity ERROR — depositor restricted for inquiry")
        void rule_t06b_cp0141IsError() {
            var code = SvcRecaudaInqV1Endpoint.CodigoRespuestaInqBCP.CP0141;
            assertThat(code.getCodigo()).isEqualTo("CP0141");
            assertThat(code.getSeveridad()).isEqualTo("ERROR");
        }

        @Test
        @DisplayName("T-06-B: CP0138 has severity ERROR — generic processing error")
        void rule_t06b_cp0138IsError() {
            var code = SvcRecaudaInqV1Endpoint.CodigoRespuestaInqBCP.CP0138;
            assertThat(code.getCodigo()).isEqualTo("CP0138");
            assertThat(code.getSeveridad()).isEqualTo("ERROR");
        }

        @Test
        @DisplayName("T-06-B: enum has exactly 4 values — no undocumented codes added")
        void rule_t06b_enumHasExactlyFourValues() {
            assertThat(SvcRecaudaInqV1Endpoint.CodigoRespuestaInqBCP.values())
                    .as("T-06-B: InqV1 enum must have exactly 4 response codes")
                    .hasSize(4);
        }
    }

    // -----------------------------------------------------------------------
    // T-06-C: TrnV1 CodigoRespuestaBCP enum
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("T-06-C: SvcRecaudaTrnV1 CodigoRespuestaBCP enum values")
    class TrnV1CodigoRespuestaBCP {

        @Test
        @DisplayName("T-06-C: CP0000 has severity INFORMACION — transaction successful")
        void rule_t06c_cp0000IsInformacion() {
            var code = SvcRecaudaTrnV1Endpoint.CodigoRespuestaTrnBCP.CP0000;
            assertThat(code.getCodigo()).isEqualTo("CP0000");
            assertThat(code.getSeveridad()).isEqualTo("INFORMACION");
        }

        @Test
        @DisplayName("T-06-C: CP0139 has severity ERROR — depositor restricted for extorno")
        void rule_t06c_cp0139IsError() {
            var code = SvcRecaudaTrnV1Endpoint.CodigoRespuestaTrnBCP.CP0139;
            assertThat(code.getCodigo()).isEqualTo("CP0139");
            assertThat(code.getSeveridad()).isEqualTo("ERROR");
        }

        @Test
        @DisplayName("T-06-C: CP0140 has severity ERROR — depositor restricted for payment")
        void rule_t06c_cp0140IsError() {
            var code = SvcRecaudaTrnV1Endpoint.CodigoRespuestaTrnBCP.CP0140;
            assertThat(code.getCodigo()).isEqualTo("CP0140");
            assertThat(code.getSeveridad()).isEqualTo("ERROR");
        }

        @Test
        @DisplayName("T-06-C: CP0138 has severity ERROR — generic processing error")
        void rule_t06c_cp0138IsError() {
            var code = SvcRecaudaTrnV1Endpoint.CodigoRespuestaTrnBCP.CP0138;
            assertThat(code.getCodigo()).isEqualTo("CP0138");
            assertThat(code.getSeveridad()).isEqualTo("ERROR");
        }

        @Test
        @DisplayName("T-06-C: enum has exactly 4 values — no undocumented codes added")
        void rule_t06c_enumHasExactlyFourValues() {
            assertThat(SvcRecaudaTrnV1Endpoint.CodigoRespuestaTrnBCP.values())
                    .as("T-06-C: TrnV1 enum must have exactly 4 response codes")
                    .hasSize(4);
        }
    }

    // -----------------------------------------------------------------------
    // T-06-D: TrnV1 ServicioBCP_Moneda mapping
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("T-06-D: ServicioBCP_2000 maps to PEN currency")
    void rule_t06d_servicio2000MapsToPEN() {
        var moneda = SvcRecaudaTrnV1Endpoint.ServicioBCPMoneda.ServicioBCP_2000;
        assertThat(moneda.getMoneda())
                .as("T-06-D: service 2000 must map to PEN")
                .isEqualTo("PEN");
    }

    @Test
    @DisplayName("T-06-D: ServicioBCP_2001 maps to USD currency")
    void rule_t06d_servicio2001MapsToUSD() {
        var moneda = SvcRecaudaTrnV1Endpoint.ServicioBCPMoneda.ServicioBCP_2001;
        assertThat(moneda.getMoneda())
                .as("T-06-D: service 2001 must map to USD")
                .isEqualTo("USD");
    }

    // -----------------------------------------------------------------------
    // T-06-E: InqV1 valid depositor returns CP0000
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("T-06-E: SvcRecaudaInqV1 returns CP0000 for a valid depositor inquiry")
    void rule_t06e_validDepositorReturnsCP0000() {
        // Arrange: plataforma reports depositor exists with no restrictions
        when(plataforma.consultarDepositanteBCP(any()))
                .thenReturn(cl.bcs.tesoreria.domain.model.DepositanteResult.valido());

        var request = new cl.bcs.tesoreria.infrastructure.soap.SvcRecaudaInqRequest(
                "12345678",  // IdDepositante — fake same-shape value
                "TES-MOD-01"
        );

        var response = inqEndpoint.svcRecaudaInq(request);

        assertThat(response)
                .as("T-06-E: response must not be null")
                .isNotNull();
        assertThat(response.codigoRespuesta())
                .as("T-06-E: valid depositor must return CP0000")
                .isEqualTo("CP0000");
    }

    // -----------------------------------------------------------------------
    // T-06-F: InqV1 unknown depositor returns CP0010
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("T-06-F: SvcRecaudaInqV1 returns CP0010 for an unknown depositor")
    void rule_t06f_unknownDepositorReturnsCP0010() {
        when(plataforma.consultarDepositanteBCP(any()))
                .thenReturn(cl.bcs.tesoreria.domain.model.DepositanteResult.noExiste());

        var request = new cl.bcs.tesoreria.infrastructure.soap.SvcRecaudaInqRequest(
                "99999999",  // IdDepositante — fake same-shape value (unknown)
                "TES-MOD-01"
        );

        var response = inqEndpoint.svcRecaudaInq(request);

        assertThat(response.codigoRespuesta())
                .as("T-06-F: unknown depositor must return CP0010")
                .isEqualTo("CP0010");
    }

    // -----------------------------------------------------------------------
    // T-06-G: TrnV1 valid payment transaction returns CP0000
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("T-06-G: SvcRecaudaTrnV1 returns CP0000 for a successful payment transaction")
    void rule_t06g_validPaymentTransactionReturnsCP0000() {
        when(plataforma.procesarTransaccionBCP(any()))
                .thenReturn(cl.bcs.tesoreria.domain.model.TransaccionBCPResult.exitosa());

        var request = new cl.bcs.tesoreria.infrastructure.soap.SvcRecaudaTrnRequest(
                "12345678",      // IdDepositante — fake same-shape value
                "2000",          // IdServicio (ServicioBCP_2000 = PEN)
                java.math.BigDecimal.valueOf(150_000),  // Monto
                "TES-MOD-01"
        );

        var response = trnEndpoint.svcRecaudaTrn(request);

        assertThat(response)
                .as("T-06-G: response must not be null")
                .isNotNull();
        assertThat(response.codigoRespuesta())
                .as("T-06-G: successful transaction must return CP0000")
                .isEqualTo("CP0000");
    }
}
