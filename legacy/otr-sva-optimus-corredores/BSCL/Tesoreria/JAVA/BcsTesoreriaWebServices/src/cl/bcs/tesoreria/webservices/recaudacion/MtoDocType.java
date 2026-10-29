
package cl.bcs.tesoreria.webservices.recaudacion;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para MtoDoc_Type complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="MtoDoc_Type">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://mfxV2_0}MtoTp"/>
 *         &lt;element ref="{http://mfxV2_0}MtoVal"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MtoDoc_Type", propOrder = {
    "mtoTp",
    "mtoVal"
})
public class MtoDocType {

    @XmlElement(name = "MtoTp", required = true)
    @XmlSchemaType(name = "string")
    protected MtoTpType mtoTp;
    @XmlElement(name = "MtoVal", required = true)
    protected MtoMonType mtoVal;

    /**
     * Obtiene el valor de la propiedad mtoTp.
     * 
     * @return
     *     possible object is
     *     {@link MtoTpType }
     *     
     */
    public MtoTpType getMtoTp() {
        return mtoTp;
    }

    /**
     * Define el valor de la propiedad mtoTp.
     * 
     * @param value
     *     allowed object is
     *     {@link MtoTpType }
     *     
     */
    public void setMtoTp(MtoTpType value) {
        this.mtoTp = value;
    }

    /**
     * Obtiene el valor de la propiedad mtoVal.
     * 
     * @return
     *     possible object is
     *     {@link MtoMonType }
     *     
     */
    public MtoMonType getMtoVal() {
        return mtoVal;
    }

    /**
     * Define el valor de la propiedad mtoVal.
     * 
     * @param value
     *     allowed object is
     *     {@link MtoMonType }
     *     
     */
    public void setMtoVal(MtoMonType value) {
        this.mtoVal = value;
    }

}
