
package cl.bcs.custodia.utils.ClientWS.periodoEjercicioBloqueo;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the cl.bcs.ws.ptde_sorteo_estadoyhorasolicitudejercicio package. 
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

    private final static QName _Exception_QNAME = new QName("http://Ptde_sorteo_estadoyhorasolicitudejercicio.ws.bcs.cl/", "Exception");
    private final static QName _PtdeSorteoEstadoyhorasolicitudejercicioExecute_QNAME = new QName("http://Ptde_sorteo_estadoyhorasolicitudejercicio.ws.bcs.cl/", "ptde_sorteo_estadoyhorasolicitudejercicioExecute");
    private final static QName _PtdeSorteoEstadoyhorasolicitudejercicioExecuteResponse_QNAME = new QName("http://Ptde_sorteo_estadoyhorasolicitudejercicio.ws.bcs.cl/", "ptde_sorteo_estadoyhorasolicitudejercicioExecuteResponse");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: cl.bcs.ws.ptde_sorteo_estadoyhorasolicitudejercicio
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link PtdeSorteoEstadoyhorasolicitudejercicioExecuteResponse }
     * 
     */
    public PtdeSorteoEstadoyhorasolicitudejercicioExecuteResponse createPtdeSorteoEstadoyhorasolicitudejercicioExecuteResponse() {
        return new PtdeSorteoEstadoyhorasolicitudejercicioExecuteResponse();
    }

    /**
     * Create an instance of {@link PtdeSorteoEstadoyhorasolicitudejercicioExecute }
     * 
     */
    public PtdeSorteoEstadoyhorasolicitudejercicioExecute createPtdeSorteoEstadoyhorasolicitudejercicioExecute() {
        return new PtdeSorteoEstadoyhorasolicitudejercicioExecute();
    }

    /**
     * Create an instance of {@link Exception }
     * 
     */
    public Exception createException() {
        return new Exception();
    }

    /**
     * Create an instance of {@link PtdeSorteoEstadoyhorasolicitudejercicioResponse }
     * 
     */
    public PtdeSorteoEstadoyhorasolicitudejercicioResponse createPtdeSorteoEstadoyhorasolicitudejercicioResponse() {
        return new PtdeSorteoEstadoyhorasolicitudejercicioResponse();
    }

    /**
     * Create an instance of {@link XstatulogResult }
     * 
     */
    public XstatulogResult createXstatulogResult() {
        return new XstatulogResult();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Exception }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://Ptde_sorteo_estadoyhorasolicitudejercicio.ws.bcs.cl/", name = "Exception")
    public JAXBElement<Exception> createException(Exception value) {
        return new JAXBElement<Exception>(_Exception_QNAME, Exception.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PtdeSorteoEstadoyhorasolicitudejercicioExecute }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://Ptde_sorteo_estadoyhorasolicitudejercicio.ws.bcs.cl/", name = "ptde_sorteo_estadoyhorasolicitudejercicioExecute")
    public JAXBElement<PtdeSorteoEstadoyhorasolicitudejercicioExecute> createPtdeSorteoEstadoyhorasolicitudejercicioExecute(PtdeSorteoEstadoyhorasolicitudejercicioExecute value) {
        return new JAXBElement<PtdeSorteoEstadoyhorasolicitudejercicioExecute>(_PtdeSorteoEstadoyhorasolicitudejercicioExecute_QNAME, PtdeSorteoEstadoyhorasolicitudejercicioExecute.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PtdeSorteoEstadoyhorasolicitudejercicioExecuteResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://Ptde_sorteo_estadoyhorasolicitudejercicio.ws.bcs.cl/", name = "ptde_sorteo_estadoyhorasolicitudejercicioExecuteResponse")
    public JAXBElement<PtdeSorteoEstadoyhorasolicitudejercicioExecuteResponse> createPtdeSorteoEstadoyhorasolicitudejercicioExecuteResponse(PtdeSorteoEstadoyhorasolicitudejercicioExecuteResponse value) {
        return new JAXBElement<PtdeSorteoEstadoyhorasolicitudejercicioExecuteResponse>(_PtdeSorteoEstadoyhorasolicitudejercicioExecuteResponse_QNAME, PtdeSorteoEstadoyhorasolicitudejercicioExecuteResponse.class, null, value);
    }

}
