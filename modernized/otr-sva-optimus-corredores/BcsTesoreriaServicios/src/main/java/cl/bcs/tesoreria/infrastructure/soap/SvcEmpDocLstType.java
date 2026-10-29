
package cl.bcs.tesoreria.infrastructure.soap;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para SvcEmpDocLst_Type complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="SvcEmpDocLst_Type">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://mfxV2_0}Ctd"/>
 *         &lt;element ref="{http://mfxV2_0}SvcEmpDoc" maxOccurs="12"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SvcEmpDocLst_Type", propOrder = {
    "ctd",
    "svcEmpDoc"
})
public class SvcEmpDocLstType {

    @XmlElement(name = "Ctd")
    protected long ctd;
    @XmlElement(name = "SvcEmpDoc", required = true)
    protected List<SvcEmpDocType> svcEmpDoc;

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
     * Gets the value of the svcEmpDoc property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the svcEmpDoc property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSvcEmpDoc().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SvcEmpDocType }
     * 
     * 
     */
    public List<SvcEmpDocType> getSvcEmpDoc() {
        if (svcEmpDoc == null) {
            svcEmpDoc = new ArrayList<SvcEmpDocType>();
        }
        return this.svcEmpDoc;
    }

}
