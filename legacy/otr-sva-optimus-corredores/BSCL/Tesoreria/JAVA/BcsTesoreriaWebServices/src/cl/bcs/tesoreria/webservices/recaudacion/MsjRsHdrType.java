
package cl.bcs.tesoreria.webservices.recaudacion;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para MsjRsHdr_Type complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="MsjRsHdr_Type">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://mfxV2_0}SegRsHdr" minOccurs="0"/>
 *         &lt;element ref="{http://mfxV2_0}CtxRsHdr"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MsjRsHdr_Type", propOrder = {
    "segRsHdr",
    "ctxRsHdr"
})
public class MsjRsHdrType {

    @XmlElement(name = "SegRsHdr")
    protected SegRsHdrType segRsHdr;
    @XmlElement(name = "CtxRsHdr", required = true)
    protected CtxRsHdrType ctxRsHdr;

    /**
     * Obtiene el valor de la propiedad segRsHdr.
     * 
     * @return
     *     possible object is
     *     {@link SegRsHdrType }
     *     
     */
    public SegRsHdrType getSegRsHdr() {
        return segRsHdr;
    }

    /**
     * Define el valor de la propiedad segRsHdr.
     * 
     * @param value
     *     allowed object is
     *     {@link SegRsHdrType }
     *     
     */
    public void setSegRsHdr(SegRsHdrType value) {
        this.segRsHdr = value;
    }

    /**
     * Obtiene el valor de la propiedad ctxRsHdr.
     * 
     * @return
     *     possible object is
     *     {@link CtxRsHdrType }
     *     
     */
    public CtxRsHdrType getCtxRsHdr() {
        return ctxRsHdr;
    }

    /**
     * Define el valor de la propiedad ctxRsHdr.
     * 
     * @param value
     *     allowed object is
     *     {@link CtxRsHdrType }
     *     
     */
    public void setCtxRsHdr(CtxRsHdrType value) {
        this.ctxRsHdr = value;
    }

}
