
package cl.bcs.tesoreria.infrastructure.soap;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para SegRsHdr_Type complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="SegRsHdr_Type">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://mfxV2_0}Ctd"/>
 *         &lt;element ref="{http://mfxV2_0}CredencialRsHdr" maxOccurs="3"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SegRsHdr_Type", propOrder = {
    "ctd",
    "credencialRsHdr"
})
public class SegRsHdrType {

    @XmlElement(name = "Ctd")
    protected long ctd;
    @XmlElement(name = "CredencialRsHdr", required = true)
    protected List<CredencialRsHdrType> credencialRsHdr;

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
     * Gets the value of the credencialRsHdr property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the credencialRsHdr property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCredencialRsHdr().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CredencialRsHdrType }
     * 
     * 
     */
    public List<CredencialRsHdrType> getCredencialRsHdr() {
        if (credencialRsHdr == null) {
            credencialRsHdr = new ArrayList<CredencialRsHdrType>();
        }
        return this.credencialRsHdr;
    }

}
