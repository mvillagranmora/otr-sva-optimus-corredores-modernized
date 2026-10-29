
package cl.bcs.custodia.utils.ClientWS.bcomercio.ejb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para wsValidaTokenResponse complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="wsValidaTokenResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="return" type="{http://ejecuciones.ordenes.session.ejb.bcomercio.cl/}wsValidaTokenResult" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "wsValidaTokenResponse", propOrder = {
    "_return"
})
public class WsValidaTokenResponse {

    @XmlElement(name = "return")
    protected WsValidaTokenResult _return;

    /**
     * Obtiene el valor de la propiedad return.
     * 
     * @return
     *     possible object is
     *     {@link WsValidaTokenResult }
     *     
     */
    public WsValidaTokenResult getReturn() {
        return _return;
    }

    /**
     * Define el valor de la propiedad return.
     * 
     * @param value
     *     allowed object is
     *     {@link WsValidaTokenResult }
     *     
     */
    public void setReturn(WsValidaTokenResult value) {
        this._return = value;
    }

}
