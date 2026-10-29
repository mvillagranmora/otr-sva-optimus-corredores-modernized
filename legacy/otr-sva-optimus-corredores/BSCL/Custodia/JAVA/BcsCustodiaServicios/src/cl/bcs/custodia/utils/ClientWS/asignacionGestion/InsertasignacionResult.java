
package cl.bcs.custodia.utils.ClientWS.asignacionGestion;

import java.math.BigDecimal;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import cl.bcs.utils.BcsPojo;


/**
 * <p>Clase Java para insertasignacionResult complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="insertasignacionResult">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ERROR_TXT" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="FECHA_OP" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="FOLIO" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ID_GESTION" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="MSG_ID" type="{http://www.w3.org/2001/XMLSchema}decimal" minOccurs="0"/>
 *         &lt;element name="NEMO" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="RUT_CLIENTE" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "insertasignacionResult", propOrder = {
    "errortxt",
    "fechaop",
    "folio",
    "idgestion",
    "msgid",
    "nemo",
    "rutcliente"
})
public class InsertasignacionResult extends BcsPojo {

    @XmlElement(name = "ERROR_TXT")
    protected String errortxt;
    @XmlElement(name = "FECHA_OP")
    protected String fechaop;
    @XmlElement(name = "FOLIO")
    protected String folio;
    @XmlElement(name = "ID_GESTION")
    protected String idgestion;
    @XmlElement(name = "MSG_ID")
    protected BigDecimal msgid;
    @XmlElement(name = "NEMO")
    protected String nemo;
    @XmlElement(name = "RUT_CLIENTE")
    protected String rutcliente;

    /**
     * Obtiene el valor de la propiedad errortxt.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getERRORTXT() {
        return errortxt;
    }

    /**
     * Define el valor de la propiedad errortxt.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setERRORTXT(String value) {
        this.errortxt = value;
    }

    /**
     * Obtiene el valor de la propiedad fechaop.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFECHAOP() {
        return fechaop;
    }

    /**
     * Define el valor de la propiedad fechaop.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFECHAOP(String value) {
        this.fechaop = value;
    }

    /**
     * Obtiene el valor de la propiedad folio.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFOLIO() {
        return folio;
    }

    /**
     * Define el valor de la propiedad folio.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFOLIO(String value) {
        this.folio = value;
    }

    /**
     * Obtiene el valor de la propiedad idgestion.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIDGESTION() {
        return idgestion;
    }

    /**
     * Define el valor de la propiedad idgestion.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIDGESTION(String value) {
        this.idgestion = value;
    }

    /**
     * Obtiene el valor de la propiedad msgid.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getMSGID() {
        return msgid;
    }

    /**
     * Define el valor de la propiedad msgid.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setMSGID(BigDecimal value) {
        this.msgid = value;
    }

    /**
     * Obtiene el valor de la propiedad nemo.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNEMO() {
        return nemo;
    }

    /**
     * Define el valor de la propiedad nemo.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNEMO(String value) {
        this.nemo = value;
    }

    /**
     * Obtiene el valor de la propiedad rutcliente.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRUTCLIENTE() {
        return rutcliente;
    }

    /**
     * Define el valor de la propiedad rutcliente.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRUTCLIENTE(String value) {
        this.rutcliente = value;
    }

}
