
package cl.bcs.ordenes.jaxws2.client;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para spadditionaltradeinformation2Response complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="spadditionaltradeinformation2Response">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="additionaltradesResult" type="{http://Spadditionaltradeinformation2.ws.AdditionalInformation2.bcs.cl/}additionaltradesResult" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="xstatuserviceResult" type="{http://Spadditionaltradeinformation2.ws.AdditionalInformation2.bcs.cl/}xstatuserviceResult" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "spadditionaltradeinformation2Response", propOrder = {
    "additionaltradesResult",
    "xstatuserviceResult"
})
public class Spadditionaltradeinformation2Response {

    @XmlElement(nillable = true)
    protected List<AdditionaltradesResult> additionaltradesResult;
    @XmlElement(nillable = true)
    protected List<XstatuserviceResult> xstatuserviceResult;

    /**
     * Gets the value of the additionaltradesResult property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the additionaltradesResult property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAdditionaltradesResult().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AdditionaltradesResult }
     * 
     * 
     */
    public List<AdditionaltradesResult> getAdditionaltradesResult() {
        if (additionaltradesResult == null) {
            additionaltradesResult = new ArrayList<AdditionaltradesResult>();
        }
        return this.additionaltradesResult;
    }

    /**
     * Gets the value of the xstatuserviceResult property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the xstatuserviceResult property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getXstatuserviceResult().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link XstatuserviceResult }
     * 
     * 
     */
    public List<XstatuserviceResult> getXstatuserviceResult() {
        if (xstatuserviceResult == null) {
            xstatuserviceResult = new ArrayList<XstatuserviceResult>();
        }
        return this.xstatuserviceResult;
    }

}
