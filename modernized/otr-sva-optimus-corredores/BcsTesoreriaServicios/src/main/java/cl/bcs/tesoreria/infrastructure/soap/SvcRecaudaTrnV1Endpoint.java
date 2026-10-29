package cl.bcs.tesoreria.infrastructure.soap;

import cl.bcs.tesoreria.domain.model.TransaccionBCPResult;
import cl.bcs.tesoreria.domain.port.out.PlataformaPort;
import cl.bcs.tesoreria.domain.port.out.TesoreriaRepository;
import jakarta.jws.WebService;
import org.springframework.stereotype.Component;

/**
 * Legacy: SvcRecaudaTrnV1_SoapHttpPortImpl.java:42-52. serviceName/portName follow the legacy @WebService; the
 * endpointInterface (PortType with Holder<MFX...>) is NOT implemented yet (declared SOAP wiring blocker).
 */
@Component
@WebService(
        targetNamespace = "http://mfxV2_0",
        serviceName = "SvcRecaudaTrnV1",
        portName = "SvcRecaudaTrnV1_SoapHttpPort"
)
public class SvcRecaudaTrnV1Endpoint {

    private final PlataformaPort plataforma;
    private final TesoreriaRepository repository;

    public SvcRecaudaTrnV1Endpoint(PlataformaPort plataforma, TesoreriaRepository repository) {
        this.plataforma = plataforma;
        this.repository = repository;
    }

    public SvcRecaudaTrnResponse svcRecaudaTrn(SvcRecaudaTrnRequest request) {
        TransaccionBCPResult result = plataforma.procesarTransaccionBCP(request);
        if (!result.transaccionExitosa()) {
            return new SvcRecaudaTrnResponse(
                    CodigoRespuestaTrnBCP.CP0139.getCodigo(),
                    CodigoRespuestaTrnBCP.CP0139.getSeveridad(),
                    null);
        }
        return new SvcRecaudaTrnResponse(
                CodigoRespuestaTrnBCP.CP0000.getCodigo(),
                CodigoRespuestaTrnBCP.CP0000.getSeveridad(),
                result.idTransaccion());
    }

    public enum CodigoRespuestaTrnBCP {
        CP0000("CP0000", "INFORMACION"),
        CP0139("CP0139", "ERROR"),
        CP0140("CP0140", "ERROR"),
        CP0138("CP0138", "ERROR");

        private final String codigo;
        private final String severidad;

        CodigoRespuestaTrnBCP(String codigo, String severidad) {
            this.codigo = codigo;
            this.severidad = severidad;
        }

        public String getCodigo() { return codigo; }
        public String getSeveridad() { return severidad; }
        public boolean isError() { return "ERROR".equals(severidad); }
    }

    public enum ServicioBCPMoneda {
        ServicioBCP_2000("PEN"),
        ServicioBCP_2001("USD");

        private final String moneda;

        ServicioBCPMoneda(String moneda) {
            this.moneda = moneda;
        }

        public String getMoneda() { return moneda; }
    }
}
