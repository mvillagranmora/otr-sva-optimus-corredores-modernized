
package cl.bcs.custodia.utils.ClientWS.sorteoContratos;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.bolsadesantiago.ws.ptde_dopc_cuenta_derivados_opc package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _PtdeDopcCuentaDerivadosOpcExecuteResponse_QNAME = new QName("http://Ptde_dopc_cuenta_derivados_opc.ws.bolsadesantiago.com/", "ptde_dopc_cuenta_derivados_opcExecuteResponse");
    private final static QName _PtdeDopcCuentaDerivadosOpcExecute_QNAME = new QName("http://Ptde_dopc_cuenta_derivados_opc.ws.bolsadesantiago.com/", "ptde_dopc_cuenta_derivados_opcExecute");
    private final static QName _Exception_QNAME = new QName("http://Ptde_dopc_cuenta_derivados_opc.ws.bolsadesantiago.com/", "Exception");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.bolsadesantiago.ws.ptde_dopc_cuenta_derivados_opc
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link PtdeDopcCuentaDerivadosOpcExecuteResponse }
     * 
     */
    public PtdeDopcCuentaDerivadosOpcExecuteResponse createPtdeDopcCuentaDerivadosOpcExecuteResponse() {
        return new PtdeDopcCuentaDerivadosOpcExecuteResponse();
    }

    /**
     * Create an instance of {@link PtdeDopcCuentaDerivadosOpcExecute }
     * 
     */
    public PtdeDopcCuentaDerivadosOpcExecute createPtdeDopcCuentaDerivadosOpcExecute() {
        return new PtdeDopcCuentaDerivadosOpcExecute();
    }

    /**
     * Create an instance of {@link Exception }
     * 
     */
    public Exception createException() {
        return new Exception();
    }

    /**
     * Create an instance of {@link PtdeDopcCuentaDerivadosOpcResponse }
     * 
     */
    public PtdeDopcCuentaDerivadosOpcResponse createPtdeDopcCuentaDerivadosOpcResponse() {
        return new PtdeDopcCuentaDerivadosOpcResponse();
    }

    /**
     * Create an instance of {@link LogResult }
     * 
     */
    public LogResult createLogResult() {
        return new LogResult();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PtdeDopcCuentaDerivadosOpcExecuteResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://Ptde_dopc_cuenta_derivados_opc.ws.bolsadesantiago.com/", name = "ptde_dopc_cuenta_derivados_opcExecuteResponse")
    public JAXBElement<PtdeDopcCuentaDerivadosOpcExecuteResponse> createPtdeDopcCuentaDerivadosOpcExecuteResponse(PtdeDopcCuentaDerivadosOpcExecuteResponse value) {
        return new JAXBElement<PtdeDopcCuentaDerivadosOpcExecuteResponse>(_PtdeDopcCuentaDerivadosOpcExecuteResponse_QNAME, PtdeDopcCuentaDerivadosOpcExecuteResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PtdeDopcCuentaDerivadosOpcExecute }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://Ptde_dopc_cuenta_derivados_opc.ws.bolsadesantiago.com/", name = "ptde_dopc_cuenta_derivados_opcExecute")
    public JAXBElement<PtdeDopcCuentaDerivadosOpcExecute> createPtdeDopcCuentaDerivadosOpcExecute(PtdeDopcCuentaDerivadosOpcExecute value) {
        return new JAXBElement<PtdeDopcCuentaDerivadosOpcExecute>(_PtdeDopcCuentaDerivadosOpcExecute_QNAME, PtdeDopcCuentaDerivadosOpcExecute.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Exception }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://Ptde_dopc_cuenta_derivados_opc.ws.bolsadesantiago.com/", name = "Exception")
    public JAXBElement<Exception> createException(Exception value) {
        return new JAXBElement<Exception>(_Exception_QNAME, Exception.class, null, value);
    }

}
