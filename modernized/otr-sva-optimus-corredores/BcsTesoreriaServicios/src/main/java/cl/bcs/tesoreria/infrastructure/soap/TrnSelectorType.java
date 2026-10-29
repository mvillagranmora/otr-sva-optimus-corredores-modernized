
package cl.bcs.tesoreria.infrastructure.soap;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para TrnSelector_Type complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="TrnSelector_Type">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://mfxV2_0}Ctd"/>
 *         &lt;element ref="{http://mfxV2_0}TrnTpLst"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TrnSelector_Type", propOrder = {
    "ctd",
    "trnTpLst"
})
public class TrnSelectorType {

    @XmlElement(name = "Ctd")
    protected long ctd;
    @XmlElement(name = "TrnTpLst", required = true)
    protected TrnTpLstType trnTpLst;

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
     * Obtiene el valor de la propiedad trnTpLst.
     * 
     * @return
     *     possible object is
     *     {@link TrnTpLstType }
     *     
     */
    public TrnTpLstType getTrnTpLst() {
        return trnTpLst;
    }

    /**
     * Define el valor de la propiedad trnTpLst.
     * 
     * @param value
     *     allowed object is
     *     {@link TrnTpLstType }
     *     
     */
    public void setTrnTpLst(TrnTpLstType value) {
        this.trnTpLst = value;
    }

}
