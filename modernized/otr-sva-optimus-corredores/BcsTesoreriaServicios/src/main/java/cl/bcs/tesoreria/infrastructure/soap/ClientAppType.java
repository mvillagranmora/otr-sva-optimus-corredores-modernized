
package cl.bcs.tesoreria.infrastructure.soap;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlSeeAlso;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para ClientApp_Type complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="ClientApp_Type">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://mfxV2_0}OrgCod"/>
 *         &lt;element ref="{http://mfxV2_0}ClientCtxIdLst" minOccurs="0"/>
 *         &lt;element ref="{http://mfxV2_0}ClientCod"/>
 *         &lt;element ref="{http://mfxV2_0}ClientCodAdic" minOccurs="0"/>
 *         &lt;element ref="{http://mfxV2_0}Ver" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ClientApp_Type", propOrder = {
    "orgCod",
    "clientCtxIdLst",
    "clientCod",
    "clientCodAdic",
    "ver"
})
@XmlSeeAlso({
    ClientProxyType.class
})
public class ClientAppType {

    @XmlElement(name = "OrgCod", required = true)
    protected String orgCod;
    @XmlElement(name = "ClientCtxIdLst")
    protected ClientCtxIdLstType clientCtxIdLst;
    @XmlElement(name = "ClientCod", required = true)
    protected String clientCod;
    @XmlElement(name = "ClientCodAdic")
    protected String clientCodAdic;
    @XmlElement(name = "Ver")
    protected String ver;

    /**
     * Obtiene el valor de la propiedad orgCod.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOrgCod() {
        return orgCod;
    }

    /**
     * Define el valor de la propiedad orgCod.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOrgCod(String value) {
        this.orgCod = value;
    }

    /**
     * Obtiene el valor de la propiedad clientCtxIdLst.
     * 
     * @return
     *     possible object is
     *     {@link ClientCtxIdLstType }
     *     
     */
    public ClientCtxIdLstType getClientCtxIdLst() {
        return clientCtxIdLst;
    }

    /**
     * Define el valor de la propiedad clientCtxIdLst.
     * 
     * @param value
     *     allowed object is
     *     {@link ClientCtxIdLstType }
     *     
     */
    public void setClientCtxIdLst(ClientCtxIdLstType value) {
        this.clientCtxIdLst = value;
    }

    /**
     * Obtiene el valor de la propiedad clientCod.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getClientCod() {
        return clientCod;
    }

    /**
     * Define el valor de la propiedad clientCod.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClientCod(String value) {
        this.clientCod = value;
    }

    /**
     * Obtiene el valor de la propiedad clientCodAdic.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getClientCodAdic() {
        return clientCodAdic;
    }

    /**
     * Define el valor de la propiedad clientCodAdic.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClientCodAdic(String value) {
        this.clientCodAdic = value;
    }

    /**
     * Obtiene el valor de la propiedad ver.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVer() {
        return ver;
    }

    /**
     * Define el valor de la propiedad ver.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVer(String value) {
        this.ver = value;
    }

}
