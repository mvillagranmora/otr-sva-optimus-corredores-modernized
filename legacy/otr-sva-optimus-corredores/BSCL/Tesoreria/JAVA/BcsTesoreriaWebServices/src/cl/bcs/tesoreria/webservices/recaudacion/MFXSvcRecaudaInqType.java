
package cl.bcs.tesoreria.webservices.recaudacion;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import cl.bcs.utils.Utils;


/**
 * <p>Clase Java para MFX_SvcRecaudaInq_Type complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="MFX_SvcRecaudaInq_Type">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://mfxV2_0}SvcRecaudaInqRq" minOccurs="0"/>
 *         &lt;element ref="{http://mfxV2_0}SvcRecaudaInqRs" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MFX_SvcRecaudaInq_Type", propOrder = {
    "svcRecaudaInqRq",
    "svcRecaudaInqRs"
})
public class MFXSvcRecaudaInqType {

    @XmlElement(name = "SvcRecaudaInqRq")
    protected SvcRecaudaInqRqType svcRecaudaInqRq;
    @XmlElement(name = "SvcRecaudaInqRs")
    protected SvcRecaudaInqRsType svcRecaudaInqRs;

    /**
     * Obtiene el valor de la propiedad svcRecaudaInqRq.
     * 
     * @return
     *     possible object is
     *     {@link SvcRecaudaInqRqType }
     *     
     */
    public SvcRecaudaInqRqType getSvcRecaudaInqRq() {
        return svcRecaudaInqRq;
    }

	public String toString() {
		return Utils.printJson(this, false);
	}
	
    /**
     * Define el valor de la propiedad svcRecaudaInqRq.
     * 
     * @param value
     *     allowed object is
     *     {@link SvcRecaudaInqRqType }
     *     
     */
    public void setSvcRecaudaInqRq(SvcRecaudaInqRqType value) {
        this.svcRecaudaInqRq = value;
    }

    /**
     * Obtiene el valor de la propiedad svcRecaudaInqRs.
     * 
     * @return
     *     possible object is
     *     {@link SvcRecaudaInqRsType }
     *     
     */
    public SvcRecaudaInqRsType getSvcRecaudaInqRs() {
        return svcRecaudaInqRs;
    }

    /**
     * Define el valor de la propiedad svcRecaudaInqRs.
     * 
     * @param value
     *     allowed object is
     *     {@link SvcRecaudaInqRsType }
     *     
     */
    public void setSvcRecaudaInqRs(SvcRecaudaInqRsType value) {
        this.svcRecaudaInqRs = value;
    }

}
