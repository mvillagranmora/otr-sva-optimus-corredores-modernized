
package cl.bcs.tesoreria.infrastructure.soap;

import java.math.BigDecimal;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para RangoMto_Type complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="RangoMto_Type">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://mfxV2_0}MtoRangoValMin" minOccurs="0"/>
 *         &lt;element ref="{http://mfxV2_0}MtoRangoValMax" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RangoMto_Type", propOrder = {
    "mtoRangoValMin",
    "mtoRangoValMax"
})
public class RangoMtoType {

    @XmlElement(name = "MtoRangoValMin")
    protected BigDecimal mtoRangoValMin;
    @XmlElement(name = "MtoRangoValMax")
    protected BigDecimal mtoRangoValMax;

    /**
     * Obtiene el valor de la propiedad mtoRangoValMin.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getMtoRangoValMin() {
        return mtoRangoValMin;
    }

    /**
     * Define el valor de la propiedad mtoRangoValMin.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setMtoRangoValMin(BigDecimal value) {
        this.mtoRangoValMin = value;
    }

    /**
     * Obtiene el valor de la propiedad mtoRangoValMax.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getMtoRangoValMax() {
        return mtoRangoValMax;
    }

    /**
     * Define el valor de la propiedad mtoRangoValMax.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setMtoRangoValMax(BigDecimal value) {
        this.mtoRangoValMax = value;
    }

}
