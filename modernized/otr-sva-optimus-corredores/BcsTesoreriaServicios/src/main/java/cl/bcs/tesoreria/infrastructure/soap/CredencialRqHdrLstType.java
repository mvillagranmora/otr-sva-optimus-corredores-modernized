
package cl.bcs.tesoreria.infrastructure.soap;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para CredencialRqHdrLst_Type complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="CredencialRqHdrLst_Type">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://mfxV2_0}Ctd"/>
 *         &lt;element ref="{http://mfxV2_0}CredencialRqHdr" maxOccurs="3"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CredencialRqHdrLst_Type", propOrder = {
    "ctd",
    "credencialRqHdr"
})
public class CredencialRqHdrLstType {

    @XmlElement(name = "Ctd")
    protected long ctd;
    @XmlElement(name = "CredencialRqHdr", required = true)
    protected List<CredencialRqHdrType> credencialRqHdr;

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
     * Gets the value of the credencialRqHdr property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the credencialRqHdr property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCredencialRqHdr().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CredencialRqHdrType }
     * 
     * 
     */
    public List<CredencialRqHdrType> getCredencialRqHdr() {
        if (credencialRqHdr == null) {
            credencialRqHdr = new ArrayList<CredencialRqHdrType>();
        }
        return this.credencialRqHdr;
    }

}
