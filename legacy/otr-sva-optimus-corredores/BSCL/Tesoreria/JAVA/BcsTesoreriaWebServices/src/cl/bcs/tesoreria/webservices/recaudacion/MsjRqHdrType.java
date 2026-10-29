
package cl.bcs.tesoreria.webservices.recaudacion;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para MsjRqHdr_Type complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="MsjRqHdr_Type">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://mfxV2_0}SegRqHdr"/>
 *         &lt;element ref="{http://mfxV2_0}CtxRqHdr"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MsjRqHdr_Type", propOrder = {
    "segRqHdr",
    "ctxRqHdr"
})
public class MsjRqHdrType {

    @XmlElement(name = "SegRqHdr", required = true)
    protected SegRqHdrType segRqHdr;
    @XmlElement(name = "CtxRqHdr", required = true)
    protected CtxRqHdrType ctxRqHdr;

    /**
     * Obtiene el valor de la propiedad segRqHdr.
     * 
     * @return
     *     possible object is
     *     {@link SegRqHdrType }
     *     
     */
    public SegRqHdrType getSegRqHdr() {
        return segRqHdr;
    }

    /**
     * Define el valor de la propiedad segRqHdr.
     * 
     * @param value
     *     allowed object is
     *     {@link SegRqHdrType }
     *     
     */
    public void setSegRqHdr(SegRqHdrType value) {
        this.segRqHdr = value;
    }

    /**
     * Obtiene el valor de la propiedad ctxRqHdr.
     * 
     * @return
     *     possible object is
     *     {@link CtxRqHdrType }
     *     
     */
    public CtxRqHdrType getCtxRqHdr() {
        return ctxRqHdr;
    }

    /**
     * Define el valor de la propiedad ctxRqHdr.
     * 
     * @param value
     *     allowed object is
     *     {@link CtxRqHdrType }
     *     
     */
    public void setCtxRqHdr(CtxRqHdrType value) {
        this.ctxRqHdr = value;
    }

}
