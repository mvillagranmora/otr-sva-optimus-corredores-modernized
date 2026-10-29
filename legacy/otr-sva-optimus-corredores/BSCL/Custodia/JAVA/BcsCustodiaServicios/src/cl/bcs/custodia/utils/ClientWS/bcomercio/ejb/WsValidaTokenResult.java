
package cl.bcs.custodia.utils.ClientWS.bcomercio.ejb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para wsValidaTokenResult complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="wsValidaTokenResult">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="corredor" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="glosa" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="msgStatus" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="myReturnValue" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="usuario" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "wsValidaTokenResult", propOrder = {
    "corredor",
    "glosa",
    "msgStatus",
    "myReturnValue",
    "usuario"
})
public class WsValidaTokenResult {

    protected String corredor;
    protected String glosa;
    protected String msgStatus;
    protected int myReturnValue;
    protected String usuario;

    /**
     * Obtiene el valor de la propiedad corredor.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCorredor() {
        return corredor;
    }

    /**
     * Define el valor de la propiedad corredor.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCorredor(String value) {
        this.corredor = value;
    }

    /**
     * Obtiene el valor de la propiedad glosa.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGlosa() {
        return glosa;
    }

    /**
     * Define el valor de la propiedad glosa.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGlosa(String value) {
        this.glosa = value;
    }

    /**
     * Obtiene el valor de la propiedad msgStatus.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMsgStatus() {
        return msgStatus;
    }

    /**
     * Define el valor de la propiedad msgStatus.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMsgStatus(String value) {
        this.msgStatus = value;
    }

    /**
     * Obtiene el valor de la propiedad myReturnValue.
     * 
     */
    public int getMyReturnValue() {
        return myReturnValue;
    }

    /**
     * Define el valor de la propiedad myReturnValue.
     * 
     */
    public void setMyReturnValue(int value) {
        this.myReturnValue = value;
    }

    /**
     * Obtiene el valor de la propiedad usuario.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUsuario() {
        return usuario;
    }

    /**
     * Define el valor de la propiedad usuario.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUsuario(String value) {
        this.usuario = value;
    }

}
