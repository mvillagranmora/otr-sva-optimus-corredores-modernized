
package cl.bcs.tesoreria.infrastructure.soap;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para SegTokenKey_Type complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="SegTokenKey_Type">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://mfxV2_0}SesKey"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SegTokenKey_Type", propOrder = {
    "sesKey"
})
public class SegTokenKeyType {

    @XmlElement(name = "SesKey", required = true)
    protected String sesKey;

    /**
     * Obtiene el valor de la propiedad sesKey.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSesKey() {
        return sesKey;
    }

    /**
     * Define el valor de la propiedad sesKey.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSesKey(String value) {
        this.sesKey = value;
    }

}
