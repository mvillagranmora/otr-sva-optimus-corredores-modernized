
package cl.bcs.ordenes.jaxws2.client;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para spadditionaltradeinformation2ExecuteResponse complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="spadditionaltradeinformation2ExecuteResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="return" type="{http://Spadditionaltradeinformation2.ws.AdditionalInformation2.bcs.cl/}spadditionaltradeinformation2Response" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "spadditionaltradeinformation2ExecuteResponse", propOrder = {
    "_return"
})
public class Spadditionaltradeinformation2ExecuteResponse {

    @XmlElement(name = "return")
    protected Spadditionaltradeinformation2Response _return;

    /**
     * Obtiene el valor de la propiedad return.
     * 
     * @return
     *     possible object is
     *     {@link Spadditionaltradeinformation2Response }
     *     
     */
    public Spadditionaltradeinformation2Response getReturn() {
        return _return;
    }

    /**
     * Define el valor de la propiedad return.
     * 
     * @param value
     *     allowed object is
     *     {@link Spadditionaltradeinformation2Response }
     *     
     */
    public void setReturn(Spadditionaltradeinformation2Response value) {
        this._return = value;
    }

}
