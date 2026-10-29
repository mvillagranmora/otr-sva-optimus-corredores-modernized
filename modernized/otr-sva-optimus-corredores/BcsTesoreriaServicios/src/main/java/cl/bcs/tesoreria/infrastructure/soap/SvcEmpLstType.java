
package cl.bcs.tesoreria.infrastructure.soap;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para SvcEmpLst_Type complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="SvcEmpLst_Type">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://mfxV2_0}Ctd"/>
 *         &lt;element ref="{http://mfxV2_0}SvcEmp"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SvcEmpLst_Type", propOrder = {
    "ctd",
    "svcEmp"
})
public class SvcEmpLstType {

    @XmlElement(name = "Ctd")
    protected long ctd;
    @XmlElement(name = "SvcEmp", required = true)
    protected SvcEmpType svcEmp;

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
     * Obtiene el valor de la propiedad svcEmp.
     * 
     * @return
     *     possible object is
     *     {@link SvcEmpType }
     *     
     */
    public SvcEmpType getSvcEmp() {
        return svcEmp;
    }

    /**
     * Define el valor de la propiedad svcEmp.
     * 
     * @param value
     *     allowed object is
     *     {@link SvcEmpType }
     *     
     */
    public void setSvcEmp(SvcEmpType value) {
        this.svcEmp = value;
    }

}
