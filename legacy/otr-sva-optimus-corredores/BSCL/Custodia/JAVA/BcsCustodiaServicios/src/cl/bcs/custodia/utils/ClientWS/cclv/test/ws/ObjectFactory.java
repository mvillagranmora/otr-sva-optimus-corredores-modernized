
package cl.bcs.custodia.utils.ClientWS.cclv.test.ws;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the ejb package. 
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

    private final static QName _PaCargaAsignacionesResponse_QNAME = new QName("http://ws.test.cclv.cl/", "paCargaAsignacionesResponse");
    private final static QName _PaCargaAsignaciones_QNAME = new QName("http://ws.test.cclv.cl/", "paCargaAsignaciones");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: ejb
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link PaCargaAsignaciones }
     * 
     */
    public PaCargaAsignaciones createPaCargaAsignaciones() {
        return new PaCargaAsignaciones();
    }

    /**
     * Create an instance of {@link PaCargaAsignacionesResponse }
     * 
     */
    public PaCargaAsignacionesResponse createPaCargaAsignacionesResponse() {
        return new PaCargaAsignacionesResponse();
    }

    /**
     * Create an instance of {@link TypeAsignaciones }
     * 
     */
    public TypeAsignaciones createTypeAsignaciones() {
        return new TypeAsignaciones();
    }

    /**
     * Create an instance of {@link TypeRetorno }
     * 
     */
    public TypeRetorno createTypeRetorno() {
        return new TypeRetorno();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PaCargaAsignacionesResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://ws.test.cclv.cl/", name = "paCargaAsignacionesResponse")
    public JAXBElement<PaCargaAsignacionesResponse> createPaCargaAsignacionesResponse(PaCargaAsignacionesResponse value) {
        return new JAXBElement<PaCargaAsignacionesResponse>(_PaCargaAsignacionesResponse_QNAME, PaCargaAsignacionesResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PaCargaAsignaciones }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://ws.test.cclv.cl/", name = "paCargaAsignaciones")
    public JAXBElement<PaCargaAsignaciones> createPaCargaAsignaciones(PaCargaAsignaciones value) {
        return new JAXBElement<PaCargaAsignaciones>(_PaCargaAsignaciones_QNAME, PaCargaAsignaciones.class, null, value);
    }

}
