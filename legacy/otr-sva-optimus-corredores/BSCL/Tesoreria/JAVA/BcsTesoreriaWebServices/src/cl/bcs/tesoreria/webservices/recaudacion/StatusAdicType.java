
package cl.bcs.tesoreria.webservices.recaudacion;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para StatusAdic_Type complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="StatusAdic_Type">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://mfxV2_0}StatusCod"/>
 *         &lt;element ref="{http://mfxV2_0}Severidad" minOccurs="0"/>
 *         &lt;element ref="{http://mfxV2_0}StatusDes" minOccurs="0"/>
 *         &lt;element ref="{http://mfxV2_0}ServStatus" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "StatusAdic_Type", propOrder = {
    "statusCod",
    "severidad",
    "statusDes",
    "servStatus"
})
public class StatusAdicType {

    @XmlElement(name = "StatusCod", required = true)
    protected String statusCod;
    @XmlElement(name = "Severidad")
    @XmlSchemaType(name = "string")
    protected SeveridadType severidad;
    @XmlElement(name = "StatusDes")
    protected String statusDes;
    @XmlElement(name = "ServStatus")
    protected ServStatusType servStatus;

    /**
     * Obtiene el valor de la propiedad statusCod.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStatusCod() {
        return statusCod;
    }

    /**
     * Define el valor de la propiedad statusCod.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStatusCod(String value) {
        this.statusCod = value;
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

    /**
     * Obtiene el valor de la propiedad servStatus.
     * 
     * @return
     *     possible object is
     *     {@link ServStatusType }
     *     
     */
    public ServStatusType getServStatus() {
        return servStatus;
    }

    /**
     * Define el valor de la propiedad servStatus.
     * 
     * @param value
     *     allowed object is
     *     {@link ServStatusType }
     *     
     */
    public void setServStatus(ServStatusType value) {
        this.servStatus = value;
    }

}
