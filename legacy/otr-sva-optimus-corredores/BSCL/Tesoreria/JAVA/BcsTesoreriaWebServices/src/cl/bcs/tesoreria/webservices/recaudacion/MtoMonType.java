
package cl.bcs.tesoreria.webservices.recaudacion;

import java.math.BigDecimal;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para MtoMon_Type complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="MtoMon_Type">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://mfxV2_0}Mto"/>
 *         &lt;element ref="{http://mfxV2_0}MonCod" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MtoMon_Type", propOrder = {
    "mto",
    "monCod"
})
public class MtoMonType {

    @XmlElement(name = "Mto", required = true)
    protected BigDecimal mto;
    @XmlElement(name = "MonCod")
    protected String monCod;

    /**
     * Obtiene el valor de la propiedad mto.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getMto() {
        return mto;
    }

    /**
     * Define el valor de la propiedad mto.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setMto(BigDecimal value) {
        this.mto = value;
    }

    /**
     * Obtiene el valor de la propiedad monCod.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMonCod() {
        return monCod;
    }

    /**
     * Define el valor de la propiedad monCod.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMonCod(String value) {
        this.monCod = value;
    }

}
