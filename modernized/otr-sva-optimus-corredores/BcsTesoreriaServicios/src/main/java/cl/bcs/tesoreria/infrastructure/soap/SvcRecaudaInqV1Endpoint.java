package cl.bcs.tesoreria.infrastructure.soap;

import cl.bcs.tesoreria.domain.model.DepositanteResult;
import cl.bcs.tesoreria.domain.port.out.PlataformaPort;
import cl.bcs.tesoreria.domain.port.out.TesoreriaRepository;
import jakarta.jws.WebService;
import org.springframework.stereotype.Component;

/**
 * Legacy: SvcRecaudaInqV1_SoapHttpPortImpl.java:40-50. serviceName/portName follow the legacy @WebService; the
 * endpointInterface (PortType with Holder<MFX...>) is NOT implemented yet (declared SOAP wiring blocker).
 */
@Component
@WebService(
        targetNamespace = "http://mfxV2_0",
        serviceName = "SvcRecaudaInqV1",
        portName = "SvcRecaudaInqV1_SoapHttpPort"
)
public class SvcRecaudaInqV1Endpoint {

    private final PlataformaPort plataforma;
    private final TesoreriaRepository repository;

    public SvcRecaudaInqV1Endpoint(PlataformaPort plataforma, TesoreriaRepository repository) {
        this.plataforma = plataforma;
        this.repository = repository;
    }

    public SvcRecaudaInqResponse svcRecaudaInq(SvcRecaudaInqRequest request) {
        DepositanteResult result = plataforma.consultarDepositanteBCP(request);
        if (!result.existe()) {
            return new SvcRecaudaInqResponse(
                    CodigoRespuestaInqBCP.CP0010.getCodigo(),
                    CodigoRespuestaInqBCP.CP0010.getSeveridad(),
                    null, null);
        }
        return new SvcRecaudaInqResponse(
                CodigoRespuestaInqBCP.CP0000.getCodigo(),
                CodigoRespuestaInqBCP.CP0000.getSeveridad(),
                result.idDepositante(), result.estadoDeposito());
    }

    public enum CodigoRespuestaInqBCP {
        CP0000("CP0000", "INFORMACION"),
        CP0010("CP0010", "ERROR"),
        CP0141("CP0141", "ERROR"),
        CP0138("CP0138", "ERROR");

        private final String codigo;
        private final String severidad;

        CodigoRespuestaInqBCP(String codigo, String severidad) {
            this.codigo = codigo;
            this.severidad = severidad;
        }

        public String getCodigo() { return codigo; }
        public String getSeveridad() { return severidad; }
        public boolean isError() { return "ERROR".equals(severidad); }
    }
}
