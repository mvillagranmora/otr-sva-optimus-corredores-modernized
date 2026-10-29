
package cl.bcs.custodia.utils.ClientWS.asignacionGestion;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the cl.bcs.pt.ws.mantasignaciones.spingresarmodificarasignaciongestion package. 
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

    private final static QName _SpingresarmodificarasignaciongestionExecuteResponse_QNAME = new QName("http://Spingresarmodificarasignaciongestion.mantasignaciones.ws.pt.bcs.cl/", "spingresarmodificarasignaciongestionExecuteResponse");
    private final static QName _SpingresarmodificarasignaciongestionExecute_QNAME = new QName("http://Spingresarmodificarasignaciongestion.mantasignaciones.ws.pt.bcs.cl/", "spingresarmodificarasignaciongestionExecute");
    private final static QName _Exception_QNAME = new QName("http://Spingresarmodificarasignaciongestion.mantasignaciones.ws.pt.bcs.cl/", "Exception");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: cl.bcs.pt.ws.mantasignaciones.spingresarmodificarasignaciongestion
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link SpingresarmodificarasignaciongestionExecute }
     * 
     */
    public SpingresarmodificarasignaciongestionExecute createSpingresarmodificarasignaciongestionExecute() {
        return new SpingresarmodificarasignaciongestionExecute();
    }

    /**
     * Create an instance of {@link SpingresarmodificarasignaciongestionExecuteResponse }
     * 
     */
    public SpingresarmodificarasignaciongestionExecuteResponse createSpingresarmodificarasignaciongestionExecuteResponse() {
        return new SpingresarmodificarasignaciongestionExecuteResponse();
    }

    /**
     * Create an instance of {@link Exception }
     * 
     */
    public Exception createException() {
        return new Exception();
    }

    /**
     * Create an instance of {@link IngresoAsignacion }
     * 
     */
    public IngresoAsignacion createIngresoAsignacion() {
        return new IngresoAsignacion();
    }

    /**
     * Create an instance of {@link InsertasignacionResult }
     * 
     */
    public InsertasignacionResult createInsertasignacionResult() {
        return new InsertasignacionResult();
    }

    /**
     * Create an instance of {@link SpingresarmodificarasignacionResponse }
     * 
     */
    public SpingresarmodificarasignacionResponse createSpingresarmodificarasignacionResponse() {
        return new SpingresarmodificarasignacionResponse();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SpingresarmodificarasignaciongestionExecuteResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://Spingresarmodificarasignaciongestion.mantasignaciones.ws.pt.bcs.cl/", name = "spingresarmodificarasignaciongestionExecuteResponse")
    public JAXBElement<SpingresarmodificarasignaciongestionExecuteResponse> createSpingresarmodificarasignaciongestionExecuteResponse(SpingresarmodificarasignaciongestionExecuteResponse value) {
        return new JAXBElement<SpingresarmodificarasignaciongestionExecuteResponse>(_SpingresarmodificarasignaciongestionExecuteResponse_QNAME, SpingresarmodificarasignaciongestionExecuteResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SpingresarmodificarasignaciongestionExecute }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://Spingresarmodificarasignaciongestion.mantasignaciones.ws.pt.bcs.cl/", name = "spingresarmodificarasignaciongestionExecute")
    public JAXBElement<SpingresarmodificarasignaciongestionExecute> createSpingresarmodificarasignaciongestionExecute(SpingresarmodificarasignaciongestionExecute value) {
        return new JAXBElement<SpingresarmodificarasignaciongestionExecute>(_SpingresarmodificarasignaciongestionExecute_QNAME, SpingresarmodificarasignaciongestionExecute.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Exception }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://Spingresarmodificarasignaciongestion.mantasignaciones.ws.pt.bcs.cl/", name = "Exception")
    public JAXBElement<Exception> createException(Exception value) {
        return new JAXBElement<Exception>(_Exception_QNAME, Exception.class, null, value);
    }

}
