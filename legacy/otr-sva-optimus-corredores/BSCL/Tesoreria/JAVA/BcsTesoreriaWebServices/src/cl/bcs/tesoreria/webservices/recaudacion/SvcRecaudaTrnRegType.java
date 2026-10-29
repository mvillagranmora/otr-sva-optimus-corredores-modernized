
package cl.bcs.tesoreria.webservices.recaudacion;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para SvcRecaudaTrnReg_Type complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="SvcRecaudaTrnReg_Type">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://mfxV2_0}SvcEmpLst"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SvcRecaudaTrnReg_Type", propOrder = {
    "svcEmpLst"
})
public class SvcRecaudaTrnRegType {

    @XmlElement(name = "SvcEmpLst", required = true)
    protected SvcEmpLstType svcEmpLst;

    /**
     * Obtiene el valor de la propiedad svcEmpLst.
     * 
     * @return
     *     possible object is
     *     {@link SvcEmpLstType }
     *     
     */
    public SvcEmpLstType getSvcEmpLst() {
        return svcEmpLst;
    }

    /**
     * Define el valor de la propiedad svcEmpLst.
     * 
     * @param value
     *     allowed object is
     *     {@link SvcEmpLstType }
     *     
     */
    public void setSvcEmpLst(SvcEmpLstType value) {
        this.svcEmpLst = value;
    }

}
