
package cl.bcs.custodia.utils.ClientWS.bcs.pt.cus.ws.movimientos;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para glosa complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="glosa">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="codigoError" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="corControl" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="glosaError" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="glosaExitoFracaso" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "glosa", propOrder = {
    "codigoError",
    "corControl",
    "glosaError",
    "glosaExitoFracaso"
})
public class Glosa {

    protected String codigoError;
    protected String corControl;
    protected String glosaError;
    protected String glosaExitoFracaso;

    /**
     * Obtiene el valor de la propiedad codigoError.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCodigoError() {
        return codigoError;
    }

    /**
     * Define el valor de la propiedad codigoError.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCodigoError(String value) {
        this.codigoError = value;
    }

    /**
     * Obtiene el valor de la propiedad corControl.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCorControl() {
        return corControl;
    }

    /**
     * Define el valor de la propiedad corControl.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCorControl(String value) {
        this.corControl = value;
    }

    /**
     * Obtiene el valor de la propiedad glosaError.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGlosaError() {
        return glosaError;
    }

    /**
     * Define el valor de la propiedad glosaError.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGlosaError(String value) {
        this.glosaError = value;
    }

    /**
     * Obtiene el valor de la propiedad glosaExitoFracaso.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGlosaExitoFracaso() {
        return glosaExitoFracaso;
    }

    /**
     * Define el valor de la propiedad glosaExitoFracaso.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGlosaExitoFracaso(String value) {
        this.glosaExitoFracaso = value;
    }

}
