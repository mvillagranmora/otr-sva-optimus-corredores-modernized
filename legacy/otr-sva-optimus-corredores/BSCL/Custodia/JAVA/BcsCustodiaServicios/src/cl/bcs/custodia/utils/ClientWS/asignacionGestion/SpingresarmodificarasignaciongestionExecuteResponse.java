
package cl.bcs.custodia.utils.ClientWS.asignacionGestion;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para spingresarmodificarasignaciongestionExecuteResponse complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="spingresarmodificarasignaciongestionExecuteResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="return" type="{http://Spingresarmodificarasignaciongestion.mantasignaciones.ws.pt.bcs.cl/}spingresarmodificarasignacionResponse" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SpingresarmodificarasignaciongestionExecuteResponse", propOrder = {
    "_return"
})
public class SpingresarmodificarasignaciongestionExecuteResponse {

    @XmlElement(name = "return")
    protected SpingresarmodificarasignacionResponse _return;

    /**
     * Obtiene el valor de la propiedad return.
     * 
     * @return
     *     possible object is
     *     {@link SpingresarmodificarasignacionResponse }
     *     
     */
    public SpingresarmodificarasignacionResponse getReturn() {
        return _return;
    }

    /**
     * Define el valor de la propiedad return.
     * 
     * @param value
     *     allowed object is
     *     {@link SpingresarmodificarasignacionResponse }
     *     
     */
    public void setReturn(SpingresarmodificarasignacionResponse value) {
        this._return = value;
    }

}
