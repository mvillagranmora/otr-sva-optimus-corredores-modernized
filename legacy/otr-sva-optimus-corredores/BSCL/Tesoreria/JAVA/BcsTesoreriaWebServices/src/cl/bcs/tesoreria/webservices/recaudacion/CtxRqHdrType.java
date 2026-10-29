
package cl.bcs.tesoreria.webservices.recaudacion;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Clase Java para CtxRqHdr_Type complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="CtxRqHdr_Type">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://mfxV2_0}ClientFec"/>
 *         &lt;element ref="{http://mfxV2_0}ClientTrmId" minOccurs="0"/>
 *         &lt;element ref="{http://mfxV2_0}OpnNro"/>
 *         &lt;element ref="{http://mfxV2_0}TrnExtorno" minOccurs="0"/>
 *         &lt;element ref="{http://mfxV2_0}ClientApp"/>
 *         &lt;element ref="{http://mfxV2_0}ClientProxy" minOccurs="0"/>
 *         &lt;element ref="{http://mfxV2_0}StoreForwardFlg" minOccurs="0"/>
 *         &lt;element ref="{http://mfxV2_0}ReenvioFlg" minOccurs="0"/>
 *         &lt;element ref="{http://mfxV2_0}SucursalCod" minOccurs="0"/>
 *         &lt;element ref="{http://mfxV2_0}AgenciaCod" minOccurs="0"/>
 *         &lt;element ref="{http://mfxV2_0}SPCod" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CtxRqHdr_Type", propOrder = {
    "clientFec",
    "clientTrmId",
    "opnNro",
    "trnExtorno",
    "clientApp",
    "clientProxy",
    "storeForwardFlg",
    "reenvioFlg",
    "sucursalCod",
    "agenciaCod",
    "spCod"
})
public class CtxRqHdrType {

    @XmlElement(name = "ClientFec", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar clientFec;
    @XmlElement(name = "ClientTrmId")
    protected ClientTrmIdType clientTrmId;
    @XmlElement(name = "OpnNro", required = true)
    protected String opnNro;
    @XmlElement(name = "TrnExtorno")
    protected TrnExtornoType trnExtorno;
    @XmlElement(name = "ClientApp", required = true)
    protected ClientAppType clientApp;
    @XmlElement(name = "ClientProxy")
    protected ClientProxyType clientProxy;
    @XmlElement(name = "StoreForwardFlg")
    protected String storeForwardFlg;
    @XmlElement(name = "ReenvioFlg")
    protected String reenvioFlg;
    @XmlElement(name = "SucursalCod")
    protected String sucursalCod;
    @XmlElement(name = "AgenciaCod")
    protected String agenciaCod;
    @XmlElement(name = "SPCod")
    protected String spCod;

    /**
     * Obtiene el valor de la propiedad clientFec.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getClientFec() {
        return clientFec;
    }

    /**
     * Define el valor de la propiedad clientFec.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setClientFec(XMLGregorianCalendar value) {
        this.clientFec = value;
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
     * Obtiene el valor de la propiedad trnExtorno.
     * 
     * @return
     *     possible object is
     *     {@link TrnExtornoType }
     *     
     */
    public TrnExtornoType getTrnExtorno() {
        return trnExtorno;
    }

    /**
     * Define el valor de la propiedad trnExtorno.
     * 
     * @param value
     *     allowed object is
     *     {@link TrnExtornoType }
     *     
     */
    public void setTrnExtorno(TrnExtornoType value) {
        this.trnExtorno = value;
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
     * Obtiene el valor de la propiedad clientProxy.
     * 
     * @return
     *     possible object is
     *     {@link ClientProxyType }
     *     
     */
    public ClientProxyType getClientProxy() {
        return clientProxy;
    }

    /**
     * Define el valor de la propiedad clientProxy.
     * 
     * @param value
     *     allowed object is
     *     {@link ClientProxyType }
     *     
     */
    public void setClientProxy(ClientProxyType value) {
        this.clientProxy = value;
    }

    /**
     * Obtiene el valor de la propiedad storeForwardFlg.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStoreForwardFlg() {
        return storeForwardFlg;
    }

    /**
     * Define el valor de la propiedad storeForwardFlg.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStoreForwardFlg(String value) {
        this.storeForwardFlg = value;
    }

    /**
     * Obtiene el valor de la propiedad reenvioFlg.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReenvioFlg() {
        return reenvioFlg;
    }

    /**
     * Define el valor de la propiedad reenvioFlg.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReenvioFlg(String value) {
        this.reenvioFlg = value;
    }

    /**
     * Obtiene el valor de la propiedad sucursalCod.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSucursalCod() {
        return sucursalCod;
    }

    /**
     * Define el valor de la propiedad sucursalCod.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSucursalCod(String value) {
        this.sucursalCod = value;
    }

    /**
     * Obtiene el valor de la propiedad agenciaCod.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAgenciaCod() {
        return agenciaCod;
    }

    /**
     * Define el valor de la propiedad agenciaCod.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAgenciaCod(String value) {
        this.agenciaCod = value;
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

}
