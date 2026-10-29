
package cl.bcs.tesoreria.webservices.recaudacion;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para TrnTpLst_Type complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="TrnTpLst_Type">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://mfxV2_0}TrnTp" maxOccurs="3"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TrnTpLst_Type", propOrder = {
    "trnTp"
})
public class TrnTpLstType {

    @XmlElement(name = "TrnTp", required = true)
    @XmlSchemaType(name = "string")
    protected List<TrnTpType> trnTp;

    /**
     * Gets the value of the trnTp property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the trnTp property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTrnTp().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TrnTpType }
     * 
     * 
     */
    public List<TrnTpType> getTrnTp() {
        if (trnTp == null) {
            trnTp = new ArrayList<TrnTpType>();
        }
        return this.trnTp;
    }

}
