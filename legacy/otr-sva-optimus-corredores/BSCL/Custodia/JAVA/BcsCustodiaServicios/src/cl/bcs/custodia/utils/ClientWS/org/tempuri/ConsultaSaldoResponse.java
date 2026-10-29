
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
 *         &lt;element name="Consulta_SaldoResult" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
    "consultaSaldoResult"
})
@XmlRootElement(name = "Consulta_SaldoResponse")
public class ConsultaSaldoResponse {

    @XmlElement(name = "Consulta_SaldoResult")
    protected String consultaSaldoResult;

    /**
     * Gets the value of the consultaSaldoResult property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getConsultaSaldoResult() {
        return consultaSaldoResult;
    }

    /**
     * Sets the value of the consultaSaldoResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setConsultaSaldoResult(String value) {
        this.consultaSaldoResult = value;
    }

}
