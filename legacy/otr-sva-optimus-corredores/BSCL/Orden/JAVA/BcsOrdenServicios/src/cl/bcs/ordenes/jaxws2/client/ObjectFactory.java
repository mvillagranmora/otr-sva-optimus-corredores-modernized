
package cl.bcs.ordenes.jaxws2.client;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the cl.bcs.ordenes.jaxws2.client package. 
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

    private final static QName _Spadditionaltradeinformation2ExecuteResponse_QNAME = new QName("http://Spadditionaltradeinformation2.ws.AdditionalInformation2.bcs.cl/", "spadditionaltradeinformation2ExecuteResponse");
    private final static QName _Exception_QNAME = new QName("http://Spadditionaltradeinformation2.ws.AdditionalInformation2.bcs.cl/", "Exception");
    private final static QName _Spadditionaltradeinformation2Execute_QNAME = new QName("http://Spadditionaltradeinformation2.ws.AdditionalInformation2.bcs.cl/", "spadditionaltradeinformation2Execute");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: cl.bcs.additionalinformation2.ws.spadditionaltradeinformation2
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Spadditionaltradeinformation2Execute }
     * 
     */
    public Spadditionaltradeinformation2Execute createSpadditionaltradeinformation2Execute() {
        return new Spadditionaltradeinformation2Execute();
    }

    /**
     * Create an instance of {@link Spadditionaltradeinformation2ExecuteResponse }
     * 
     */
    public Spadditionaltradeinformation2ExecuteResponse createSpadditionaltradeinformation2ExecuteResponse() {
        return new Spadditionaltradeinformation2ExecuteResponse();
    }

    /**
     * Create an instance of {@link Exception }
     * 
     */
    public Exception createException() {
        return new Exception();
    }

    /**
     * Create an instance of {@link XstatuserviceResult }
     * 
     */
    public XstatuserviceResult createXstatuserviceResult() {
        return new XstatuserviceResult();
    }

    /**
     * Create an instance of {@link Spadditionaltradeinformation2Response }
     * 
     */
    public Spadditionaltradeinformation2Response createSpadditionaltradeinformation2Response() {
        return new Spadditionaltradeinformation2Response();
    }

    /**
     * Create an instance of {@link AdditionaltradesResult }
     * 
     */
    public AdditionaltradesResult createAdditionaltradesResult() {
        return new AdditionaltradesResult();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Spadditionaltradeinformation2ExecuteResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://Spadditionaltradeinformation2.ws.AdditionalInformation2.bcs.cl/", name = "spadditionaltradeinformation2ExecuteResponse")
    public JAXBElement<Spadditionaltradeinformation2ExecuteResponse> createSpadditionaltradeinformation2ExecuteResponse(Spadditionaltradeinformation2ExecuteResponse value) {
        return new JAXBElement<Spadditionaltradeinformation2ExecuteResponse>(_Spadditionaltradeinformation2ExecuteResponse_QNAME, Spadditionaltradeinformation2ExecuteResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Exception }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://Spadditionaltradeinformation2.ws.AdditionalInformation2.bcs.cl/", name = "Exception")
    public JAXBElement<Exception> createException(Exception value) {
        return new JAXBElement<Exception>(_Exception_QNAME, Exception.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Spadditionaltradeinformation2Execute }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://Spadditionaltradeinformation2.ws.AdditionalInformation2.bcs.cl/", name = "spadditionaltradeinformation2Execute")
    public JAXBElement<Spadditionaltradeinformation2Execute> createSpadditionaltradeinformation2Execute(Spadditionaltradeinformation2Execute value) {
        return new JAXBElement<Spadditionaltradeinformation2Execute>(_Spadditionaltradeinformation2Execute_QNAME, Spadditionaltradeinformation2Execute.class, null, value);
    }

}
