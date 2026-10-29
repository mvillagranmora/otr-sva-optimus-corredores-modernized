
package cl.bcs.custodia.utils.ClientWS.bcomercio.ejb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para wscreatokenResult complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="wscreatokenResult">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="codError" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="descError" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="returnValue" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="token" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "wscreatokenResult", propOrder = {
    "codError",
    "descError",
    "returnValue",
    "token"
})
public class WscreatokenResult {

    protected int codError;
    protected String descError;
    protected int returnValue;
    protected String token;

    /**
     * Obtiene el valor de la propiedad codError.
     * 
     */
    public int getCodError() {
        return codError;
    }

    /**
     * Define el valor de la propiedad codError.
     * 
     */
    public void setCodError(int value) {
        this.codError = value;
    }

    /**
     * Obtiene el valor de la propiedad descError.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescError() {
        return descError;
    }

    /**
     * Define el valor de la propiedad descError.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescError(String value) {
        this.descError = value;
    }

    /**
     * Obtiene el valor de la propiedad returnValue.
     * 
     */
    public int getReturnValue() {
        return returnValue;
    }

    /**
     * Define el valor de la propiedad returnValue.
     * 
     */
    public void setReturnValue(int value) {
        this.returnValue = value;
    }

    /**
     * Obtiene el valor de la propiedad token.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getToken() {
        return token;
    }

    /**
     * Define el valor de la propiedad token.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setToken(String value) {
        this.token = value;
    }

}
