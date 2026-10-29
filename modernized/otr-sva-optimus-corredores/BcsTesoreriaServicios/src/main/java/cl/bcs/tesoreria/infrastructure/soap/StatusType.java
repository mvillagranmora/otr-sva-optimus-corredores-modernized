
package cl.bcs.tesoreria.infrastructure.soap;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para Status_Type complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="Status_Type">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://mfxV2_0}StatusCod"/>
 *         &lt;element ref="{http://mfxV2_0}Severidad"/>
 *         &lt;element ref="{http://mfxV2_0}StatusDes" minOccurs="0"/>
 *         &lt;element ref="{http://mfxV2_0}ServStatus" minOccurs="0"/>
 *         &lt;element ref="{http://mfxV2_0}StatusAdicLst" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Status_Type", propOrder = {
    "statusCod",
    "severidad",
    "statusDes",
    "servStatus",
    "statusAdicLst"
})
public class StatusType {

    @XmlElement(name = "StatusCod", required = true)
    protected String statusCod;
    @XmlElement(name = "Severidad", required = true)
    @XmlSchemaType(name = "string")
    protected SeveridadType severidad;
    @XmlElement(name = "StatusDes")
    protected String statusDes;
    @XmlElement(name = "ServStatus")
    protected ServStatusType servStatus;
    @XmlElement(name = "StatusAdicLst")
    protected StatusAdicLstType statusAdicLst;

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

    /**
     * Obtiene el valor de la propiedad statusAdicLst.
     * 
     * @return
     *     possible object is
     *     {@link StatusAdicLstType }
     *     
     */
    public StatusAdicLstType getStatusAdicLst() {
        return statusAdicLst;
    }

    /**
     * Define el valor de la propiedad statusAdicLst.
     * 
     * @param value
     *     allowed object is
     *     {@link StatusAdicLstType }
     *     
     */
    public void setStatusAdicLst(StatusAdicLstType value) {
        this.statusAdicLst = value;
    }

}
