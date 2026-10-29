
package cl.bcs.tesoreria.infrastructure.soap;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para CredencialRqHdr_Type complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="CredencialRqHdr_Type">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://mfxV2_0}NroSec"/>
 *         &lt;element ref="{http://mfxV2_0}SegRol"/>
 *         &lt;element ref="{http://mfxV2_0}GenSesTokenFlg" minOccurs="0"/>
 *         &lt;element ref="{http://mfxV2_0}SegTokenUsr" minOccurs="0"/>
 *         &lt;element ref="{http://mfxV2_0}SegTokenCard" minOccurs="0"/>
 *         &lt;element ref="{http://mfxV2_0}SegTokenKey" minOccurs="0"/>
 *         &lt;element ref="{http://mfxV2_0}SegTokenLogin" minOccurs="0"/>
 *         &lt;element ref="{http://mfxV2_0}SegTokenRpta" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CredencialRqHdr_Type", propOrder = {
    "nroSec",
    "segRol",
    "genSesTokenFlg",
    "segTokenUsr",
    "segTokenCard",
    "segTokenKey",
    "segTokenLogin",
    "segTokenRpta"
})
public class CredencialRqHdrType {

    @XmlElement(name = "NroSec")
    protected long nroSec;
    @XmlElement(name = "SegRol", required = true)
    @XmlSchemaType(name = "string")
    protected SegRolType segRol;
    @XmlElement(name = "GenSesTokenFlg")
    protected String genSesTokenFlg;
    @XmlElement(name = "SegTokenUsr")
    protected SegTokenUsrType segTokenUsr;
    @XmlElement(name = "SegTokenCard")
    protected SegTokenCardType segTokenCard;
    @XmlElement(name = "SegTokenKey")
    protected SegTokenKeyType segTokenKey;
    @XmlElement(name = "SegTokenLogin")
    protected SegTokenLoginType segTokenLogin;
    @XmlElement(name = "SegTokenRpta")
    protected SegTokenRptaType segTokenRpta;

    /**
     * Obtiene el valor de la propiedad nroSec.
     * 
     */
    public long getNroSec() {
        return nroSec;
    }

    /**
     * Define el valor de la propiedad nroSec.
     * 
     */
    public void setNroSec(long value) {
        this.nroSec = value;
    }

    /**
     * Obtiene el valor de la propiedad segRol.
     * 
     * @return
     *     possible object is
     *     {@link SegRolType }
     *     
     */
    public SegRolType getSegRol() {
        return segRol;
    }

    /**
     * Define el valor de la propiedad segRol.
     * 
     * @param value
     *     allowed object is
     *     {@link SegRolType }
     *     
     */
    public void setSegRol(SegRolType value) {
        this.segRol = value;
    }

    /**
     * Obtiene el valor de la propiedad genSesTokenFlg.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGenSesTokenFlg() {
        return genSesTokenFlg;
    }

    /**
     * Define el valor de la propiedad genSesTokenFlg.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGenSesTokenFlg(String value) {
        this.genSesTokenFlg = value;
    }

    /**
     * Obtiene el valor de la propiedad segTokenUsr.
     * 
     * @return
     *     possible object is
     *     {@link SegTokenUsrType }
     *     
     */
    public SegTokenUsrType getSegTokenUsr() {
        return segTokenUsr;
    }

    /**
     * Define el valor de la propiedad segTokenUsr.
     * 
     * @param value
     *     allowed object is
     *     {@link SegTokenUsrType }
     *     
     */
    public void setSegTokenUsr(SegTokenUsrType value) {
        this.segTokenUsr = value;
    }

    /**
     * Obtiene el valor de la propiedad segTokenCard.
     * 
     * @return
     *     possible object is
     *     {@link SegTokenCardType }
     *     
     */
    public SegTokenCardType getSegTokenCard() {
        return segTokenCard;
    }

    /**
     * Define el valor de la propiedad segTokenCard.
     * 
     * @param value
     *     allowed object is
     *     {@link SegTokenCardType }
     *     
     */
    public void setSegTokenCard(SegTokenCardType value) {
        this.segTokenCard = value;
    }

    /**
     * Obtiene el valor de la propiedad segTokenKey.
     * 
     * @return
     *     possible object is
     *     {@link SegTokenKeyType }
     *     
     */
    public SegTokenKeyType getSegTokenKey() {
        return segTokenKey;
    }

    /**
     * Define el valor de la propiedad segTokenKey.
     * 
     * @param value
     *     allowed object is
     *     {@link SegTokenKeyType }
     *     
     */
    public void setSegTokenKey(SegTokenKeyType value) {
        this.segTokenKey = value;
    }

    /**
     * Obtiene el valor de la propiedad segTokenLogin.
     * 
     * @return
     *     possible object is
     *     {@link SegTokenLoginType }
     *     
     */
    public SegTokenLoginType getSegTokenLogin() {
        return segTokenLogin;
    }

    /**
     * Define el valor de la propiedad segTokenLogin.
     * 
     * @param value
     *     allowed object is
     *     {@link SegTokenLoginType }
     *     
     */
    public void setSegTokenLogin(SegTokenLoginType value) {
        this.segTokenLogin = value;
    }

    /**
     * Obtiene el valor de la propiedad segTokenRpta.
     * 
     * @return
     *     possible object is
     *     {@link SegTokenRptaType }
     *     
     */
    public SegTokenRptaType getSegTokenRpta() {
        return segTokenRpta;
    }

    /**
     * Define el valor de la propiedad segTokenRpta.
     * 
     * @param value
     *     allowed object is
     *     {@link SegTokenRptaType }
     *     
     */
    public void setSegTokenRpta(SegTokenRptaType value) {
        this.segTokenRpta = value;
    }

}
