
package cl.bcs.tesoreria.infrastructure.soap;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para CredencialRsHdr_Type complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="CredencialRsHdr_Type">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://mfxV2_0}Status" minOccurs="0"/>
 *         &lt;element ref="{http://mfxV2_0}NroSec" minOccurs="0"/>
 *         &lt;element ref="{http://mfxV2_0}SesKey" minOccurs="0"/>
 *         &lt;element ref="{http://mfxV2_0}FecVct" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CredencialRsHdr_Type", propOrder = {
    "status",
    "nroSec",
    "sesKey",
    "fecVct"
})
public class CredencialRsHdrType {

    @XmlElement(name = "Status")
    protected StatusType status;
    @XmlElement(name = "NroSec")
    protected Long nroSec;
    @XmlElement(name = "SesKey")
    protected String sesKey;
    @XmlElement(name = "FecVct")
    protected String fecVct;

    /**
     * Obtiene el valor de la propiedad status.
     * 
     * @return
     *     possible object is
     *     {@link StatusType }
     *     
     */
    public StatusType getStatus() {
        return status;
    }

    /**
     * Define el valor de la propiedad status.
     * 
     * @param value
     *     allowed object is
     *     {@link StatusType }
     *     
     */
    public void setStatus(StatusType value) {
        this.status = value;
    }

    /**
     * Obtiene el valor de la propiedad nroSec.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getNroSec() {
        return nroSec;
    }

    /**
     * Define el valor de la propiedad nroSec.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setNroSec(Long value) {
        this.nroSec = value;
    }

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

    /**
     * Obtiene el valor de la propiedad fecVct.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFecVct() {
        return fecVct;
    }

    /**
     * Define el valor de la propiedad fecVct.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFecVct(String value) {
        this.fecVct = value;
    }

}
