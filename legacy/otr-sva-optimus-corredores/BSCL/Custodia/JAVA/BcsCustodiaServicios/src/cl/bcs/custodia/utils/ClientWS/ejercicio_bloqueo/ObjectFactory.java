
package cl.bcs.custodia.utils.ClientWS.ejercicio_bloqueo;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the cl.bcs.ws.ejercicio_bloqueo package. 
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

    private final static QName _PtdeConsultaejerciciosEjercicioBloqueoExecuteResponse_QNAME = new QName("http://ejercicio_bloqueo.ws.bcs.cl/", "ptde_consultaejercicios_ejercicioBloqueoExecuteResponse");
    private final static QName _Exception_QNAME = new QName("http://ejercicio_bloqueo.ws.bcs.cl/", "Exception");
    private final static QName _PtdeConsultaejerciciosEjercicioBloqueoExecute_QNAME = new QName("http://ejercicio_bloqueo.ws.bcs.cl/", "ptde_consultaejercicios_ejercicioBloqueoExecute");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: cl.bcs.ws.ejercicio_bloqueo
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link PtdeConsultaejerciciosEjercicioBloqueoExecute }
     * 
     */
    public PtdeConsultaejerciciosEjercicioBloqueoExecute createPtdeConsultaejerciciosEjercicioBloqueoExecute() {
        return new PtdeConsultaejerciciosEjercicioBloqueoExecute();
    }

    /**
     * Create an instance of {@link Exception }
     * 
     */
    public Exception createException() {
        return new Exception();
    }

    /**
     * Create an instance of {@link PtdeConsultaejerciciosEjercicioBloqueoExecuteResponse }
     * 
     */
    public PtdeConsultaejerciciosEjercicioBloqueoExecuteResponse createPtdeConsultaejerciciosEjercicioBloqueoExecuteResponse() {
        return new PtdeConsultaejerciciosEjercicioBloqueoExecuteResponse();
    }

    /**
     * Create an instance of {@link EjercicioBloqueo }
     * 
     */
    public EjercicioBloqueo createEjercicioBloqueo() {
        return new EjercicioBloqueo();
    }

    /**
     * Create an instance of {@link EjercicioBloqueoReporte }
     * 
     */
    public EjercicioBloqueoReporte createEjercicioBloqueoReporte() {
        return new EjercicioBloqueoReporte();
    }

    /**
     * Create an instance of {@link EjercicioBloqueoResponse }
     * 
     */
    public EjercicioBloqueoResponse createEjercicioBloqueoResponse() {
        return new EjercicioBloqueoResponse();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PtdeConsultaejerciciosEjercicioBloqueoExecuteResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://ejercicio_bloqueo.ws.bcs.cl/", name = "ptde_consultaejercicios_ejercicioBloqueoExecuteResponse")
    public JAXBElement<PtdeConsultaejerciciosEjercicioBloqueoExecuteResponse> createPtdeConsultaejerciciosEjercicioBloqueoExecuteResponse(PtdeConsultaejerciciosEjercicioBloqueoExecuteResponse value) {
        return new JAXBElement<PtdeConsultaejerciciosEjercicioBloqueoExecuteResponse>(_PtdeConsultaejerciciosEjercicioBloqueoExecuteResponse_QNAME, PtdeConsultaejerciciosEjercicioBloqueoExecuteResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Exception }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://ejercicio_bloqueo.ws.bcs.cl/", name = "Exception")
    public JAXBElement<Exception> createException(Exception value) {
        return new JAXBElement<Exception>(_Exception_QNAME, Exception.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PtdeConsultaejerciciosEjercicioBloqueoExecute }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://ejercicio_bloqueo.ws.bcs.cl/", name = "ptde_consultaejercicios_ejercicioBloqueoExecute")
    public JAXBElement<PtdeConsultaejerciciosEjercicioBloqueoExecute> createPtdeConsultaejerciciosEjercicioBloqueoExecute(PtdeConsultaejerciciosEjercicioBloqueoExecute value) {
        return new JAXBElement<PtdeConsultaejerciciosEjercicioBloqueoExecute>(_PtdeConsultaejerciciosEjercicioBloqueoExecute_QNAME, PtdeConsultaejerciciosEjercicioBloqueoExecute.class, null, value);
    }

}
