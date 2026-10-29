
package cl.bcs.tesoreria.webservices.recaudacion;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para SegTokenCard_Type complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="SegTokenCard_Type">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://mfxV2_0}CardInqRef"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SegTokenCard_Type", propOrder = {
    "cardInqRef"
})
public class SegTokenCardType {

    @XmlElement(name = "CardInqRef", required = true)
    protected CardInqRefType cardInqRef;

    /**
     * Obtiene el valor de la propiedad cardInqRef.
     * 
     * @return
     *     possible object is
     *     {@link CardInqRefType }
     *     
     */
    public CardInqRefType getCardInqRef() {
        return cardInqRef;
    }

    /**
     * Define el valor de la propiedad cardInqRef.
     * 
     * @param value
     *     allowed object is
     *     {@link CardInqRefType }
     *     
     */
    public void setCardInqRef(CardInqRefType value) {
        this.cardInqRef = value;
    }

}
