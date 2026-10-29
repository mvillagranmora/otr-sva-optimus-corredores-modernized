
package cl.bcs.tesoreria.webservices.recaudacion;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para RangoVal_Type complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="RangoVal_Type">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://mfxV2_0}ValIni" minOccurs="0"/>
 *         &lt;element ref="{http://mfxV2_0}ValFin" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RangoVal_Type", propOrder = {
    "valIni",
    "valFin"
})
public class RangoValType {

    @XmlElement(name = "ValIni")
    protected String valIni;
    @XmlElement(name = "ValFin")
    protected String valFin;

    /**
     * Obtiene el valor de la propiedad valIni.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getValIni() {
        return valIni;
    }

    /**
     * Define el valor de la propiedad valIni.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValIni(String value) {
        this.valIni = value;
    }

    /**
     * Obtiene el valor de la propiedad valFin.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getValFin() {
        return valFin;
    }

    /**
     * Define el valor de la propiedad valFin.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValFin(String value) {
        this.valFin = value;
    }

}
