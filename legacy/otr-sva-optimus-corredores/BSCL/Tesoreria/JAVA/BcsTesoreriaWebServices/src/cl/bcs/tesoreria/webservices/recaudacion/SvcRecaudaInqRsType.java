
package cl.bcs.tesoreria.webservices.recaudacion;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para SvcRecaudaInqRs_Type complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="SvcRecaudaInqRs_Type">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://mfxV2_0}Status"/>
 *         &lt;element ref="{http://mfxV2_0}RqUID"/>
 *         &lt;element ref="{http://mfxV2_0}MsjRsHdr"/>
 *         &lt;element ref="{http://mfxV2_0}SvcRecaudaInqReg" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SvcRecaudaInqRs_Type", propOrder = {
    "status",
    "rqUID",
    "msjRsHdr",
    "svcRecaudaInqReg"
})
public class SvcRecaudaInqRsType {

    @XmlElement(name = "Status", required = true)
    protected StatusType status;
    @XmlElement(name = "RqUID", required = true)
    protected String rqUID;
    @XmlElement(name = "MsjRsHdr", required = true)
    protected MsjRsHdrType msjRsHdr;
    @XmlElement(name = "SvcRecaudaInqReg")
    protected SvcRecaudaInqRegType svcRecaudaInqReg;

    /**
     * Obtiene el valor de la propiedad status.
     * 
     * @return
     *     possible object is
     *     {@link StatusType }
     *     
     */
    public StatusType getStatus() {
        return status;
    }

    /**
     * Define el valor de la propiedad status.
     * 
     * @param value
     *     allowed object is
     *     {@link StatusType }
     *     
     */
    public void setStatus(StatusType value) {
        this.status = value;
    }

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
     * Obtiene el valor de la propiedad msjRsHdr.
     * 
     * @return
     *     possible object is
     *     {@link MsjRsHdrType }
     *     
     */
    public MsjRsHdrType getMsjRsHdr() {
        return msjRsHdr;
    }

    /**
     * Define el valor de la propiedad msjRsHdr.
     * 
     * @param value
     *     allowed object is
     *     {@link MsjRsHdrType }
     *     
     */
    public void setMsjRsHdr(MsjRsHdrType value) {
        this.msjRsHdr = value;
    }

    /**
     * Obtiene el valor de la propiedad svcRecaudaInqReg.
     * 
     * @return
     *     possible object is
     *     {@link SvcRecaudaInqRegType }
     *     
     */
    public SvcRecaudaInqRegType getSvcRecaudaInqReg() {
        return svcRecaudaInqReg;
    }

    /**
     * Define el valor de la propiedad svcRecaudaInqReg.
     * 
     * @param value
     *     allowed object is
     *     {@link SvcRecaudaInqRegType }
     *     
     */
    public void setSvcRecaudaInqReg(SvcRecaudaInqRegType value) {
        this.svcRecaudaInqReg = value;
    }

}
