
package cl.bcs.tesoreria.webservices.recaudacion;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para SegTokenRpta_Type complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="SegTokenRpta_Type">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://mfxV2_0}SegSecreto"/>
 *         &lt;element ref="{http://mfxV2_0}EncrTp" minOccurs="0"/>
 *         &lt;element ref="{http://mfxV2_0}SegRpta" minOccurs="0"/>
 *         &lt;element ref="{http://mfxV2_0}SegRptaEncr" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SegTokenRpta_Type", propOrder = {
    "segSecreto",
    "encrTp",
    "segRpta",
    "segRptaEncr"
})
public class SegTokenRptaType {

    @XmlElement(name = "SegSecreto", required = true)
    protected String segSecreto;
    @XmlElement(name = "EncrTp")
    protected String encrTp;
    @XmlElement(name = "SegRpta")
    protected String segRpta;
    @XmlElement(name = "SegRptaEncr")
    protected String segRptaEncr;

    /**
     * Obtiene el valor de la propiedad segSecreto.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSegSecreto() {
        return segSecreto;
    }

    /**
     * Define el valor de la propiedad segSecreto.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSegSecreto(String value) {
        this.segSecreto = value;
    }

    /**
     * Obtiene el valor de la propiedad encrTp.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEncrTp() {
        return encrTp;
    }

    /**
     * Define el valor de la propiedad encrTp.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEncrTp(String value) {
        this.encrTp = value;
    }

    /**
     * Obtiene el valor de la propiedad segRpta.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSegRpta() {
        return segRpta;
    }

    /**
     * Define el valor de la propiedad segRpta.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSegRpta(String value) {
        this.segRpta = value;
    }

    /**
     * Obtiene el valor de la propiedad segRptaEncr.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSegRptaEncr() {
        return segRptaEncr;
    }

    /**
     * Define el valor de la propiedad segRptaEncr.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSegRptaEncr(String value) {
        this.segRptaEncr = value;
    }

}
