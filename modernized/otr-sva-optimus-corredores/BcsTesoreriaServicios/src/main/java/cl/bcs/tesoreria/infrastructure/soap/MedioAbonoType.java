
package cl.bcs.tesoreria.infrastructure.soap;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para MedioAbono_Type complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="MedioAbono_Type">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://mfxV2_0}MedioAbonoCod"/>
 *         &lt;element ref="{http://mfxV2_0}MtoMon"/>
 *         &lt;element ref="{http://mfxV2_0}PagCod" minOccurs="0"/>
 *         &lt;element ref="{http://mfxV2_0}ChqNro" minOccurs="0"/>
 *         &lt;element ref="{http://mfxV2_0}OrgCod" minOccurs="0"/>
 *         &lt;element ref="{http://mfxV2_0}OpnRef" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MedioAbono_Type", propOrder = {
    "medioAbonoCod",
    "mtoMon",
    "pagCod",
    "chqNro",
    "orgCod",
    "opnRef"
})
public class MedioAbonoType {

    @XmlElement(name = "MedioAbonoCod", required = true)
    protected String medioAbonoCod;
    @XmlElement(name = "MtoMon", required = true)
    protected MtoMonType mtoMon;
    @XmlElement(name = "PagCod")
    protected String pagCod;
    @XmlElement(name = "ChqNro")
    protected String chqNro;
    @XmlElement(name = "OrgCod")
    protected String orgCod;
    @XmlElement(name = "OpnRef")
    protected String opnRef;

    /**
     * Obtiene el valor de la propiedad medioAbonoCod.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMedioAbonoCod() {
        return medioAbonoCod;
    }

    /**
     * Define el valor de la propiedad medioAbonoCod.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMedioAbonoCod(String value) {
        this.medioAbonoCod = value;
    }

    /**
     * Obtiene el valor de la propiedad mtoMon.
     * 
     * @return
     *     possible object is
     *     {@link MtoMonType }
     *     
     */
    public MtoMonType getMtoMon() {
        return mtoMon;
    }

    /**
     * Define el valor de la propiedad mtoMon.
     * 
     * @param value
     *     allowed object is
     *     {@link MtoMonType }
     *     
     */
    public void setMtoMon(MtoMonType value) {
        this.mtoMon = value;
    }

    /**
     * Obtiene el valor de la propiedad pagCod.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPagCod() {
        return pagCod;
    }

    /**
     * Define el valor de la propiedad pagCod.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPagCod(String value) {
        this.pagCod = value;
    }

    /**
     * Obtiene el valor de la propiedad chqNro.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getChqNro() {
        return chqNro;
    }

    /**
     * Define el valor de la propiedad chqNro.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setChqNro(String value) {
        this.chqNro = value;
    }

    /**
     * Obtiene el valor de la propiedad orgCod.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOrgCod() {
        return orgCod;
    }

    /**
     * Define el valor de la propiedad orgCod.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOrgCod(String value) {
        this.orgCod = value;
    }

    /**
     * Obtiene el valor de la propiedad opnRef.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOpnRef() {
        return opnRef;
    }

    /**
     * Define el valor de la propiedad opnRef.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOpnRef(String value) {
        this.opnRef = value;
    }

}
