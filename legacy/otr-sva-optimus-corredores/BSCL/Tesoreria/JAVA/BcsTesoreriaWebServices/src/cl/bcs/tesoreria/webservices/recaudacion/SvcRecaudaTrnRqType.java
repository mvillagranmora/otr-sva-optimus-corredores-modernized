
package cl.bcs.tesoreria.webservices.recaudacion;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para SvcRecaudaTrnRq_Type complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="SvcRecaudaTrnRq_Type">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://mfxV2_0}RqUID"/>
 *         &lt;element ref="{http://mfxV2_0}MsjRqHdr"/>
 *         &lt;element ref="{http://mfxV2_0}RegRqCtrl" minOccurs="0"/>
 *         &lt;element ref="{http://mfxV2_0}SvcRecaudaTrnRef"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SvcRecaudaTrnRq_Type", propOrder = {
    "rqUID",
    "msjRqHdr",
    "regRqCtrl",
    "svcRecaudaTrnRef"
})
public class SvcRecaudaTrnRqType {

    @XmlElement(name = "RqUID", required = true)
    protected String rqUID;
    @XmlElement(name = "MsjRqHdr", required = true)
    protected MsjRqHdrType msjRqHdr;
    @XmlElement(name = "RegRqCtrl")
    protected RegRqCtrlType regRqCtrl;
    @XmlElement(name = "SvcRecaudaTrnRef", required = true)
    protected SvcRecaudaTrnRefType svcRecaudaTrnRef;

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
     * Obtiene el valor de la propiedad svcRecaudaTrnRef.
     * 
     * @return
     *     possible object is
     *     {@link SvcRecaudaTrnRefType }
     *     
     */
    public SvcRecaudaTrnRefType getSvcRecaudaTrnRef() {
        return svcRecaudaTrnRef;
    }

    /**
     * Define el valor de la propiedad svcRecaudaTrnRef.
     * 
     * @param value
     *     allowed object is
     *     {@link SvcRecaudaTrnRefType }
     *     
     */
    public void setSvcRecaudaTrnRef(SvcRecaudaTrnRefType value) {
        this.svcRecaudaTrnRef = value;
    }

}
