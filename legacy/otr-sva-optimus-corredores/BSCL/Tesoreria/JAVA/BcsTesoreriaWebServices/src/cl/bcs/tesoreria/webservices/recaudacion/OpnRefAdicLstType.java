
package cl.bcs.tesoreria.webservices.recaudacion;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para OpnRefAdicLst_Type complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="OpnRefAdicLst_Type">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://mfxV2_0}Ctd"/>
 *         &lt;element ref="{http://mfxV2_0}OpnRefAdic" maxOccurs="3"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OpnRefAdicLst_Type", propOrder = {
    "ctd",
    "opnRefAdic"
})
public class OpnRefAdicLstType {

    @XmlElement(name = "Ctd")
    protected long ctd;
    @XmlElement(name = "OpnRefAdic", required = true)
    protected List<String> opnRefAdic;

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
     * Gets the value of the opnRefAdic property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the opnRefAdic property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getOpnRefAdic().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getOpnRefAdic() {
        if (opnRefAdic == null) {
            opnRefAdic = new ArrayList<String>();
        }
        return this.opnRefAdic;
    }

}
