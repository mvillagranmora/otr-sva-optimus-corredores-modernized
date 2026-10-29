
package cl.bcs.tesoreria.webservices.recaudacion;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para RangoFec_Type complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="RangoFec_Type">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://mfxV2_0}FecIni" minOccurs="0"/>
 *         &lt;element ref="{http://mfxV2_0}FecFin" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RangoFec_Type", propOrder = {
    "fecIni",
    "fecFin"
})
public class RangoFecType {

    @XmlElement(name = "FecIni")
    protected String fecIni;
    @XmlElement(name = "FecFin")
    protected String fecFin;

    /**
     * Obtiene el valor de la propiedad fecIni.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFecIni() {
        return fecIni;
    }

    /**
     * Define el valor de la propiedad fecIni.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFecIni(String value) {
        this.fecIni = value;
    }

    /**
     * Obtiene el valor de la propiedad fecFin.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFecFin() {
        return fecFin;
    }

    /**
     * Define el valor de la propiedad fecFin.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFecFin(String value) {
        this.fecFin = value;
    }

}
