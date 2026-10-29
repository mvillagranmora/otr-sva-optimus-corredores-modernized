
package cl.bcs.tesoreria.webservices.recaudacion;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para ClientCtxIdLst_Type complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="ClientCtxIdLst_Type">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://mfxV2_0}Ctd"/>
 *         &lt;element ref="{http://mfxV2_0}ClientCtxId" maxOccurs="3"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ClientCtxIdLst_Type", propOrder = {
    "ctd",
    "clientCtxId"
})
public class ClientCtxIdLstType {

    @XmlElement(name = "Ctd")
    protected long ctd;
    @XmlElement(name = "ClientCtxId", required = true)
    protected List<ClientCtxIdType> clientCtxId;

    /**
     * Obtiene el valor de la propiedad ctd.
     * 
     */
    public long getCtd() {
        return ctd;
    }

    /**
     * Define el valor de la propiedad ctd.
     * 
     */
    public void setCtd(long value) {
        this.ctd = value;
    }

    /**
     * Gets the value of the clientCtxId property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the clientCtxId property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getClientCtxId().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ClientCtxIdType }
     * 
     * 
     */
    public List<ClientCtxIdType> getClientCtxId() {
        if (clientCtxId == null) {
            clientCtxId = new ArrayList<ClientCtxIdType>();
        }
        return this.clientCtxId;
    }

}
