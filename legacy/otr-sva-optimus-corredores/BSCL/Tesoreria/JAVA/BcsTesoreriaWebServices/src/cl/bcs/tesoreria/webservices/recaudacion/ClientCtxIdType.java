
package cl.bcs.tesoreria.webservices.recaudacion;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para ClientCtxId_Type complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="ClientCtxId_Type">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://mfxV2_0}ClientCtxIdTp"/>
 *         &lt;element ref="{http://mfxV2_0}ClientCtxIdVal"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ClientCtxId_Type", propOrder = {
    "clientCtxIdTp",
    "clientCtxIdVal"
})
public class ClientCtxIdType {

    @XmlElement(name = "ClientCtxIdTp", required = true)
    protected String clientCtxIdTp;
    @XmlElement(name = "ClientCtxIdVal", required = true)
    protected String clientCtxIdVal;

    /**
     * Obtiene el valor de la propiedad clientCtxIdTp.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getClientCtxIdTp() {
        return clientCtxIdTp;
    }

    /**
     * Define el valor de la propiedad clientCtxIdTp.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClientCtxIdTp(String value) {
        this.clientCtxIdTp = value;
    }

    /**
     * Obtiene el valor de la propiedad clientCtxIdVal.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getClientCtxIdVal() {
        return clientCtxIdVal;
    }

    /**
     * Define el valor de la propiedad clientCtxIdVal.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClientCtxIdVal(String value) {
        this.clientCtxIdVal = value;
    }

}
