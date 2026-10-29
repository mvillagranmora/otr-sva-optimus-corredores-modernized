
package cl.bcs.tesoreria.infrastructure.soap;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para TrnExtorno_Type complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="TrnExtorno_Type">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://mfxV2_0}OpnExtornoNro"/>
 *         &lt;element ref="{http://mfxV2_0}ExtornoTp"/>
 *         &lt;element ref="{http://mfxV2_0}Des" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TrnExtorno_Type", propOrder = {
    "opnExtornoNro",
    "extornoTp",
    "des"
})
public class TrnExtornoType {

    @XmlElement(name = "OpnExtornoNro", required = true)
    protected String opnExtornoNro;
    @XmlElement(name = "ExtornoTp", required = true)
    @XmlSchemaType(name = "string")
    protected ExtornoTpType extornoTp;
    @XmlElement(name = "Des")
    protected String des;

    /**
     * Obtiene el valor de la propiedad opnExtornoNro.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOpnExtornoNro() {
        return opnExtornoNro;
    }

    /**
     * Define el valor de la propiedad opnExtornoNro.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOpnExtornoNro(String value) {
        this.opnExtornoNro = value;
    }

    /**
     * Obtiene el valor de la propiedad extornoTp.
     * 
     * @return
     *     possible object is
     *     {@link ExtornoTpType }
     *     
     */
    public ExtornoTpType getExtornoTp() {
        return extornoTp;
    }

    /**
     * Define el valor de la propiedad extornoTp.
     * 
     * @param value
     *     allowed object is
     *     {@link ExtornoTpType }
     *     
     */
    public void setExtornoTp(ExtornoTpType value) {
        this.extornoTp = value;
    }

    /**
     * Obtiene el valor de la propiedad des.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDes() {
        return des;
    }

    /**
     * Define el valor de la propiedad des.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDes(String value) {
        this.des = value;
    }

}
