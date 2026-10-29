
package cl.bcs.custodia.utils.ClientWS.bcs.pt.cus.ws.movimientos;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the ejb2 package. 
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

    private final static QName _PaCargaretGtiaResponse_QNAME = new QName("http://movimientos.ws.cus.pt.bcs.cl/", "paCargaretGtiaResponse");
    private final static QName _Exception_QNAME = new QName("http://movimientos.ws.cus.pt.bcs.cl/", "Exception");
    private final static QName _PaCargaretGtia_QNAME = new QName("http://movimientos.ws.cus.pt.bcs.cl/", "paCargaretGtia");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: ejb2
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link PaCargaretGtiaResponse }
     * 
     */
    public PaCargaretGtiaResponse createPaCargaretGtiaResponse() {
        return new PaCargaretGtiaResponse();
    }

    /**
     * Create an instance of {@link Exception }
     * 
     */
    public Exception createException() {
        return new Exception();
    }

    /**
     * Create an instance of {@link PaCargaretGtia }
     * 
     */
    public PaCargaretGtia createPaCargaretGtia() {
        return new PaCargaretGtia();
    }

    /**
     * Create an instance of {@link Glosa }
     * 
     */
    public Glosa createGlosa() {
        return new Glosa();
    }

    /**
     * Create an instance of {@link CargamasivaMensaje }
     * 
     */
    public CargamasivaMensaje createCargamasivaMensaje() {
        return new CargamasivaMensaje();
    }

    /**
     * Create an instance of {@link DetalleGtia }
     * 
     */
    public DetalleGtia createDetalleGtia() {
        return new DetalleGtia();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PaCargaretGtiaResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://movimientos.ws.cus.pt.bcs.cl/", name = "paCargaretGtiaResponse")
    public JAXBElement<PaCargaretGtiaResponse> createPaCargaretGtiaResponse(PaCargaretGtiaResponse value) {
        return new JAXBElement<PaCargaretGtiaResponse>(_PaCargaretGtiaResponse_QNAME, PaCargaretGtiaResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Exception }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://movimientos.ws.cus.pt.bcs.cl/", name = "Exception")
    public JAXBElement<Exception> createException(Exception value) {
        return new JAXBElement<Exception>(_Exception_QNAME, Exception.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PaCargaretGtia }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://movimientos.ws.cus.pt.bcs.cl/", name = "paCargaretGtia")
    public JAXBElement<PaCargaretGtia> createPaCargaretGtia(PaCargaretGtia value) {
        return new JAXBElement<PaCargaretGtia>(_PaCargaretGtia_QNAME, PaCargaretGtia.class, null, value);
    }

}
