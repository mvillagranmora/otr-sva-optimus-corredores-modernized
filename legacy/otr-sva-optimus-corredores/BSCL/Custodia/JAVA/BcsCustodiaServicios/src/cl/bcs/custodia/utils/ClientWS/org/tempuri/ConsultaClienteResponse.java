
package cl.bcs.custodia.utils.ClientWS.org.tempuri;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Consulta_ClienteResult" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "consultaClienteResult"
})
@XmlRootElement(name = "Consulta_ClienteResponse")
public class ConsultaClienteResponse {

    @XmlElement(name = "Consulta_ClienteResult")
    protected String consultaClienteResult;

    /**
     * Gets the value of the consultaClienteResult property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getConsultaClienteResult() {
        return consultaClienteResult;
    }

    /**
     * Sets the value of the consultaClienteResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setConsultaClienteResult(String value) {
        this.consultaClienteResult = value;
    }

}
