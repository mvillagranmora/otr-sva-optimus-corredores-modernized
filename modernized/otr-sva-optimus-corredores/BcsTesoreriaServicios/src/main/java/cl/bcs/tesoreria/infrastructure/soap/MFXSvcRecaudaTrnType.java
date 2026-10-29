
package cl.bcs.tesoreria.infrastructure.soap;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

import cl.bcs.utils.Utils;


/**
 * <p>Clase Java para MFX_SvcRecaudaTrn_Type complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="MFX_SvcRecaudaTrn_Type">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://mfxV2_0}SvcRecaudaTrnRq" minOccurs="0"/>
 *         &lt;element ref="{http://mfxV2_0}SvcRecaudaTrnRs" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MFX_SvcRecaudaTrn_Type", propOrder = {
    "svcRecaudaTrnRq",
    "svcRecaudaTrnRs"
})
public class MFXSvcRecaudaTrnType {

    @XmlElement(name = "SvcRecaudaTrnRq")
    protected SvcRecaudaTrnRqType svcRecaudaTrnRq;
    @XmlElement(name = "SvcRecaudaTrnRs")
    protected SvcRecaudaTrnRsType svcRecaudaTrnRs;

    /**
     * Obtiene el valor de la propiedad svcRecaudaTrnRq.
     * 
     * @return
     *     possible object is
     *     {@link SvcRecaudaTrnRqType }
     *     
     */
    public SvcRecaudaTrnRqType getSvcRecaudaTrnRq() {
        return svcRecaudaTrnRq;
    }

    /**
     * Define el valor de la propiedad svcRecaudaTrnRq.
     * 
     * @param value
     *     allowed object is
     *     {@link SvcRecaudaTrnRqType }
     *     
     */
    public void setSvcRecaudaTrnRq(SvcRecaudaTrnRqType value) {
        this.svcRecaudaTrnRq = value;
    }

	public String toString() {
		return Utils.printJson(this, false);
	}    
    /**
     * Obtiene el valor de la propiedad svcRecaudaTrnRs.
     * 
     * @return
     *     possible object is
     *     {@link SvcRecaudaTrnRsType }
     *     
     */
    public SvcRecaudaTrnRsType getSvcRecaudaTrnRs() {
        return svcRecaudaTrnRs;
    }

    /**
     * Define el valor de la propiedad svcRecaudaTrnRs.
     * 
     * @param value
     *     allowed object is
     *     {@link SvcRecaudaTrnRsType }
     *     
     */
    public void setSvcRecaudaTrnRs(SvcRecaudaTrnRsType value) {
        this.svcRecaudaTrnRs = value;
    }

}
