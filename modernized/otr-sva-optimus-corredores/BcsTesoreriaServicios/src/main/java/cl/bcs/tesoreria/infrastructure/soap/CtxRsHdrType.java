
package cl.bcs.tesoreria.infrastructure.soap;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Clase Java para CtxRsHdr_Type complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="CtxRsHdr_Type">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://mfxV2_0}ServFec"/>
 *         &lt;element ref="{http://mfxV2_0}ClientTrmId" minOccurs="0"/>
 *         &lt;element ref="{http://mfxV2_0}OpnNro" minOccurs="0"/>
 *         &lt;element ref="{http://mfxV2_0}OpnNroAdic" minOccurs="0"/>
 *         &lt;element ref="{http://mfxV2_0}ClientApp" minOccurs="0"/>
 *         &lt;element ref="{http://mfxV2_0}SPCod" minOccurs="0"/>
 *         &lt;element ref="{http://mfxV2_0}ServTrmId" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CtxRsHdr_Type", propOrder = {
    "servFec",
    "clientTrmId",
    "opnNro",
    "opnNroAdic",
    "clientApp",
    "spCod",
    "servTrmId"
})
public class CtxRsHdrType {

    @XmlElement(name = "ServFec", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar servFec;
    @XmlElement(name = "ClientTrmId")
    protected ClientTrmIdType clientTrmId;
    @XmlElement(name = "OpnNro")
    protected String opnNro;
    @XmlElement(name = "OpnNroAdic")
    protected String opnNroAdic;
    @XmlElement(name = "ClientApp")
    protected ClientAppType clientApp;
    @XmlElement(name = "SPCod")
    protected String spCod;
    @XmlElement(name = "ServTrmId")
    protected String servTrmId;

    /**
     * Obtiene el valor de la propiedad servFec.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getServFec() {
        return servFec;
    }

    /**
     * Define el valor de la propiedad servFec.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setServFec(XMLGregorianCalendar value) {
        this.servFec = value;
    }

    /**
     * Obtiene el valor de la propiedad clientTrmId.
     * 
     * @return
     *     possible object is
     *     {@link ClientTrmIdType }
     *     
     */
    public ClientTrmIdType getClientTrmId() {
        return clientTrmId;
    }

    /**
     * Define el valor de la propiedad clientTrmId.
     * 
     * @param value
     *     allowed object is
     *     {@link ClientTrmIdType }
     *     
     */
    public void setClientTrmId(ClientTrmIdType value) {
        this.clientTrmId = value;
    }

    /**
     * Obtiene el valor de la propiedad opnNro.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOpnNro() {
        return opnNro;
    }

    /**
     * Define el valor de la propiedad opnNro.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOpnNro(String value) {
        this.opnNro = value;
    }

    /**
     * Obtiene el valor de la propiedad opnNroAdic.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOpnNroAdic() {
        return opnNroAdic;
    }

    /**
     * Define el valor de la propiedad opnNroAdic.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOpnNroAdic(String value) {
        this.opnNroAdic = value;
    }

    /**
     * Obtiene el valor de la propiedad clientApp.
     * 
     * @return
     *     possible object is
     *     {@link ClientAppType }
     *     
     */
    public ClientAppType getClientApp() {
        return clientApp;
    }

    /**
     * Define el valor de la propiedad clientApp.
     * 
     * @param value
     *     allowed object is
     *     {@link ClientAppType }
     *     
     */
    public void setClientApp(ClientAppType value) {
        this.clientApp = value;
    }

    /**
     * Obtiene el valor de la propiedad spCod.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSPCod() {
        return spCod;
    }

    /**
     * Define el valor de la propiedad spCod.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSPCod(String value) {
        this.spCod = value;
    }

    /**
     * Obtiene el valor de la propiedad servTrmId.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getServTrmId() {
        return servTrmId;
    }

    /**
     * Define el valor de la propiedad servTrmId.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setServTrmId(String value) {
        this.servTrmId = value;
    }

}
