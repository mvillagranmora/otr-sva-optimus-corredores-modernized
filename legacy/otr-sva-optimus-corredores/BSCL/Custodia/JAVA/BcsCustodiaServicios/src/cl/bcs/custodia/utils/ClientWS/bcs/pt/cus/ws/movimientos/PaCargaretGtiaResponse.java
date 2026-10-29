
package cl.bcs.custodia.utils.ClientWS.bcs.pt.cus.ws.movimientos;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para paCargaretGtiaResponse complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="paCargaretGtiaResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="return" type="{http://movimientos.ws.cus.pt.bcs.cl/}CargamasivaMensaje" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "paCargaretGtiaResponse", propOrder = {
    "_return"
})
public class PaCargaretGtiaResponse {

    @XmlElement(name = "return")
    protected CargamasivaMensaje _return;

    /**
     * Obtiene el valor de la propiedad return.
     * 
     * @return
     *     possible object is
     *     {@link CargamasivaMensaje }
     *     
     */
    public CargamasivaMensaje getReturn() {
        return _return;
    }

    /**
     * Define el valor de la propiedad return.
     * 
     * @param value
     *     allowed object is
     *     {@link CargamasivaMensaje }
     *     
     */
    public void setReturn(CargamasivaMensaje value) {
        this._return = value;
    }

}
