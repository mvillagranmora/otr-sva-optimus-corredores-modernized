
package cl.bcs.custodia.utils.ClientWS.bcomercio.ejb;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the cl.bcs.custodia.utils.ClientWS.bcomercio.ejb package. 
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

    private final static QName _WsCreaTokenAgente_QNAME = new QName("http://ejecuciones.ordenes.session.ejb.bcomercio.cl/", "wsCreaTokenAgente");
    private final static QName _WsValidaTokenResponse_QNAME = new QName("http://ejecuciones.ordenes.session.ejb.bcomercio.cl/", "wsValidaTokenResponse");
    private final static QName _WsCreaTokenAgenteResponse_QNAME = new QName("http://ejecuciones.ordenes.session.ejb.bcomercio.cl/", "wsCreaTokenAgenteResponse");
    private final static QName _FaultInfo_QNAME = new QName("http://ejecuciones.ordenes.session.ejb.bcomercio.cl/", "faultInfo");
    private final static QName _Wscreatoken_QNAME = new QName("http://ejecuciones.ordenes.session.ejb.bcomercio.cl/", "wscreatoken");
    private final static QName _WsValidaToken_QNAME = new QName("http://ejecuciones.ordenes.session.ejb.bcomercio.cl/", "wsValidaToken");
    private final static QName _WscreatokenResponse_QNAME = new QName("http://ejecuciones.ordenes.session.ejb.bcomercio.cl/", "wscreatokenResponse");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: cl.bcs.custodia.utils.ClientWS.bcomercio.ejb
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link WsCreaTokenAgenteResponse }
     * 
     */
    public WsCreaTokenAgenteResponse createWsCreaTokenAgenteResponse() {
        return new WsCreaTokenAgenteResponse();
    }

    /**
     * Create an instance of {@link WsValidaTokenResponse }
     * 
     */
    public WsValidaTokenResponse createWsValidaTokenResponse() {
        return new WsValidaTokenResponse();
    }

    /**
     * Create an instance of {@link WsCreaTokenAgente }
     * 
     */
    public WsCreaTokenAgente createWsCreaTokenAgente() {
        return new WsCreaTokenAgente();
    }

    /**
     * Create an instance of {@link WsValidaToken }
     * 
     */
    public WsValidaToken createWsValidaToken() {
        return new WsValidaToken();
    }

    /**
     * Create an instance of {@link WscreatokenResponse }
     * 
     */
    public WscreatokenResponse createWscreatokenResponse() {
        return new WscreatokenResponse();
    }

    /**
     * Create an instance of {@link Wscreatoken }
     * 
     */
    public Wscreatoken createWscreatoken() {
        return new Wscreatoken();
    }

    /**
     * Create an instance of {@link WscreatokenResult }
     * 
     */
    public WscreatokenResult createWscreatokenResult() {
        return new WscreatokenResult();
    }

    /**
     * Create an instance of {@link WsValidaTokenResult }
     * 
     */
    public WsValidaTokenResult createWsValidaTokenResult() {
        return new WsValidaTokenResult();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link WsCreaTokenAgente }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://ejecuciones.ordenes.session.ejb.bcomercio.cl/", name = "wsCreaTokenAgente")
    public JAXBElement<WsCreaTokenAgente> createWsCreaTokenAgente(WsCreaTokenAgente value) {
        return new JAXBElement<WsCreaTokenAgente>(_WsCreaTokenAgente_QNAME, WsCreaTokenAgente.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link WsValidaTokenResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://ejecuciones.ordenes.session.ejb.bcomercio.cl/", name = "wsValidaTokenResponse")
    public JAXBElement<WsValidaTokenResponse> createWsValidaTokenResponse(WsValidaTokenResponse value) {
        return new JAXBElement<WsValidaTokenResponse>(_WsValidaTokenResponse_QNAME, WsValidaTokenResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link WsCreaTokenAgenteResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://ejecuciones.ordenes.session.ejb.bcomercio.cl/", name = "wsCreaTokenAgenteResponse")
    public JAXBElement<WsCreaTokenAgenteResponse> createWsCreaTokenAgenteResponse(WsCreaTokenAgenteResponse value) {
        return new JAXBElement<WsCreaTokenAgenteResponse>(_WsCreaTokenAgenteResponse_QNAME, WsCreaTokenAgenteResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://ejecuciones.ordenes.session.ejb.bcomercio.cl/", name = "faultInfo")
    public JAXBElement<String> createFaultInfo(String value) {
        return new JAXBElement<String>(_FaultInfo_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Wscreatoken }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://ejecuciones.ordenes.session.ejb.bcomercio.cl/", name = "wscreatoken")
    public JAXBElement<Wscreatoken> createWscreatoken(Wscreatoken value) {
        return new JAXBElement<Wscreatoken>(_Wscreatoken_QNAME, Wscreatoken.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link WsValidaToken }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://ejecuciones.ordenes.session.ejb.bcomercio.cl/", name = "wsValidaToken")
    public JAXBElement<WsValidaToken> createWsValidaToken(WsValidaToken value) {
        return new JAXBElement<WsValidaToken>(_WsValidaToken_QNAME, WsValidaToken.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link WscreatokenResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://ejecuciones.ordenes.session.ejb.bcomercio.cl/", name = "wscreatokenResponse")
    public JAXBElement<WscreatokenResponse> createWscreatokenResponse(WscreatokenResponse value) {
        return new JAXBElement<WscreatokenResponse>(_WscreatokenResponse_QNAME, WscreatokenResponse.class, null, value);
    }

}
