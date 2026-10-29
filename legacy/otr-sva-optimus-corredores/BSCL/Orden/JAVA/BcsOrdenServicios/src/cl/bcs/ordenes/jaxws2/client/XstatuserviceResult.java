
package cl.bcs.ordenes.jaxws2.client;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para xstatuserviceResult complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="xstatuserviceResult">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="XStatuService" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "xstatuserviceResult", propOrder = {
    "xStatuService"
})
public class XstatuserviceResult {

    @XmlElement(name = "XStatuService")
    protected String xStatuService;

    /**
     * Obtiene el valor de la propiedad xStatuService.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getXStatuService() {
        return xStatuService;
    }

    /**
     * Define el valor de la propiedad xStatuService.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setXStatuService(String value) {
        this.xStatuService = value;
    }

}
