
package cl.bcs.tesoreria.infrastructure.soap;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para SvcRecaudaInqRq_Type complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="SvcRecaudaInqRq_Type">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://mfxV2_0}RqUID"/>
 *         &lt;element ref="{http://mfxV2_0}MsjRqHdr"/>
 *         &lt;element ref="{http://mfxV2_0}RegRqCtrl" minOccurs="0"/>
 *         &lt;element ref="{http://mfxV2_0}SvcRecaudaInqRef"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SvcRecaudaInqRq_Type", propOrder = {
    "rqUID",
    "msjRqHdr",
    "regRqCtrl",
    "svcRecaudaInqRef"
})
public class SvcRecaudaInqRqType {

    @XmlElement(name = "RqUID", required = true)
    protected String rqUID;
    @XmlElement(name = "MsjRqHdr", required = true)
    protected MsjRqHdrType msjRqHdr;
    @XmlElement(name = "RegRqCtrl")
    protected RegRqCtrlType regRqCtrl;
    @XmlElement(name = "SvcRecaudaInqRef", required = true)
    protected SvcRecaudaInqRefType svcRecaudaInqRef;

    /**
     * Obtiene el valor de la propiedad rqUID.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRqUID() {
        return rqUID;
    }

    /**
     * Define el valor de la propiedad rqUID.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRqUID(String value) {
        this.rqUID = value;
    }

    /**
     * Obtiene el valor de la propiedad msjRqHdr.
     * 
     * @return
     *     possible object is
     *     {@link MsjRqHdrType }
     *     
     */
    public MsjRqHdrType getMsjRqHdr() {
        return msjRqHdr;
    }

    /**
     * Define el valor de la propiedad msjRqHdr.
     * 
     * @param value
     *     allowed object is
     *     {@link MsjRqHdrType }
     *     
     */
    public void setMsjRqHdr(MsjRqHdrType value) {
        this.msjRqHdr = value;
    }

    /**
     * Obtiene el valor de la propiedad regRqCtrl.
     * 
     * @return
     *     possible object is
     *     {@link RegRqCtrlType }
     *     
     */
    public RegRqCtrlType getRegRqCtrl() {
        return regRqCtrl;
    }

    /**
     * Define el valor de la propiedad regRqCtrl.
     * 
     * @param value
     *     allowed object is
     *     {@link RegRqCtrlType }
     *     
     */
    public void setRegRqCtrl(RegRqCtrlType value) {
        this.regRqCtrl = value;
    }

    /**
     * Obtiene el valor de la propiedad svcRecaudaInqRef.
     * 
     * @return
     *     possible object is
     *     {@link SvcRecaudaInqRefType }
     *     
     */
    public SvcRecaudaInqRefType getSvcRecaudaInqRef() {
        return svcRecaudaInqRef;
    }

    /**
     * Define el valor de la propiedad svcRecaudaInqRef.
     * 
     * @param value
     *     allowed object is
     *     {@link SvcRecaudaInqRefType }
     *     
     */
    public void setSvcRecaudaInqRef(SvcRecaudaInqRefType value) {
        this.svcRecaudaInqRef = value;
    }

}
