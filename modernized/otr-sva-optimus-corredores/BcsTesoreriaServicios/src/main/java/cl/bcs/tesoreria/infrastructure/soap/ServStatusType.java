
package cl.bcs.tesoreria.infrastructure.soap;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para ServStatus_Type complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="ServStatus_Type">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://mfxV2_0}ServStatusCod"/>
 *         &lt;element ref="{http://mfxV2_0}Severidad" minOccurs="0"/>
 *         &lt;element ref="{http://mfxV2_0}StatusDes"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ServStatus_Type", propOrder = {
    "servStatusCod",
    "severidad",
    "statusDes"
})
public class ServStatusType {

    @XmlElement(name = "ServStatusCod", required = true)
    protected String servStatusCod;
    @XmlElement(name = "Severidad")
    @XmlSchemaType(name = "string")
    protected SeveridadType severidad;
    @XmlElement(name = "StatusDes", required = true)
    protected String statusDes;

    /**
     * Obtiene el valor de la propiedad servStatusCod.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getServStatusCod() {
        return servStatusCod;
    }

    /**
     * Define el valor de la propiedad servStatusCod.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setServStatusCod(String value) {
        this.servStatusCod = value;
    }

    /**
     * Obtiene el valor de la propiedad severidad.
     * 
     * @return
     *     possible object is
     *     {@link SeveridadType }
     *     
     */
    public SeveridadType getSeveridad() {
        return severidad;
    }

    /**
     * Define el valor de la propiedad severidad.
     * 
     * @param value
     *     allowed object is
     *     {@link SeveridadType }
     *     
     */
    public void setSeveridad(SeveridadType value) {
        this.severidad = value;
    }

    /**
     * Obtiene el valor de la propiedad statusDes.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStatusDes() {
        return statusDes;
    }

    /**
     * Define el valor de la propiedad statusDes.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStatusDes(String value) {
        this.statusDes = value;
    }

}
