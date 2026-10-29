
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
 *         &lt;element name="ControlLimiteCreditoResult" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
    "controlLimiteCreditoResult"
})
@XmlRootElement(name = "ControlLimiteCreditoResponse")
public class ControlLimiteCreditoResponse {

    @XmlElement(name = "ControlLimiteCreditoResult")
    protected String controlLimiteCreditoResult;

    /**
     * Gets the value of the controlLimiteCreditoResult property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getControlLimiteCreditoResult() {
        return controlLimiteCreditoResult;
    }

    /**
     * Sets the value of the controlLimiteCreditoResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setControlLimiteCreditoResult(String value) {
        this.controlLimiteCreditoResult = value;
    }

}
