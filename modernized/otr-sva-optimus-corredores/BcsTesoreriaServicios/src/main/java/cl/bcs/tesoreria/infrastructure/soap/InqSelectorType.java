
package cl.bcs.tesoreria.infrastructure.soap;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para InqSelector_Type complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="InqSelector_Type">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://mfxV2_0}Ctd"/>
 *         &lt;element ref="{http://mfxV2_0}SvcSelectorLst"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "InqSelector_Type", propOrder = {
    "ctd",
    "svcSelectorLst"
})
public class InqSelectorType {

    @XmlElement(name = "Ctd")
    protected long ctd;
    @XmlElement(name = "SvcSelectorLst", required = true)
    protected SvcSelectorLstType svcSelectorLst;

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
     * Obtiene el valor de la propiedad svcSelectorLst.
     * 
     * @return
     *     possible object is
     *     {@link SvcSelectorLstType }
     *     
     */
    public SvcSelectorLstType getSvcSelectorLst() {
        return svcSelectorLst;
    }

    /**
     * Define el valor de la propiedad svcSelectorLst.
     * 
     * @param value
     *     allowed object is
     *     {@link SvcSelectorLstType }
     *     
     */
    public void setSvcSelectorLst(SvcSelectorLstType value) {
        this.svcSelectorLst = value;
    }

}
