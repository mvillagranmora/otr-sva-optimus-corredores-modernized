
package cl.bcs.tesoreria.infrastructure.soap;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para ClientTrmId_Type complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="ClientTrmId_Type">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://mfxV2_0}ClientTrmServ" minOccurs="0"/>
 *         &lt;element ref="{http://mfxV2_0}ClientTrmApp" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ClientTrmId_Type", propOrder = {
    "clientTrmServ",
    "clientTrmApp"
})
public class ClientTrmIdType {

    @XmlElement(name = "ClientTrmServ")
    protected String clientTrmServ;
    @XmlElement(name = "ClientTrmApp")
    protected String clientTrmApp;

    /**
     * Obtiene el valor de la propiedad clientTrmServ.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getClientTrmServ() {
        return clientTrmServ;
    }

    /**
     * Define el valor de la propiedad clientTrmServ.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClientTrmServ(String value) {
        this.clientTrmServ = value;
    }

    /**
     * Obtiene el valor de la propiedad clientTrmApp.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getClientTrmApp() {
        return clientTrmApp;
    }

    /**
     * Define el valor de la propiedad clientTrmApp.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClientTrmApp(String value) {
        this.clientTrmApp = value;
    }

}
