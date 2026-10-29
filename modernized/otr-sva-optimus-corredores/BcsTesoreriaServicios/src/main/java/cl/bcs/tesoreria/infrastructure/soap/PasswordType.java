
package cl.bcs.tesoreria.infrastructure.soap;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para Password_Type complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="Password_Type">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://mfxV2_0}EncrTp" minOccurs="0"/>
 *         &lt;element ref="{http://mfxV2_0}PasswordVal" minOccurs="0"/>
 *         &lt;element ref="{http://mfxV2_0}PasswordEncr" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Password_Type", propOrder = {
    "encrTp",
    "passwordVal",
    "passwordEncr"
})
public class PasswordType {

    @XmlElement(name = "EncrTp")
    protected String encrTp;
    @XmlElement(name = "PasswordVal")
    protected String passwordVal;
    @XmlElement(name = "PasswordEncr")
    protected String passwordEncr;

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
     * Obtiene el valor de la propiedad passwordVal.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPasswordVal() {
        return passwordVal;
    }

    /**
     * Define el valor de la propiedad passwordVal.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPasswordVal(String value) {
        this.passwordVal = value;
    }

    /**
     * Obtiene el valor de la propiedad passwordEncr.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPasswordEncr() {
        return passwordEncr;
    }

    /**
     * Define el valor de la propiedad passwordEncr.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPasswordEncr(String value) {
        this.passwordEncr = value;
    }

}
