
package cl.bcs.tesoreria.webservices.recaudacion;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para SvcEmp_Type complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="SvcEmp_Type">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://mfxV2_0}SvcId"/>
 *         &lt;element ref="{http://mfxV2_0}PagTp" minOccurs="0"/>
 *         &lt;element ref="{http://mfxV2_0}PagId"/>
 *         &lt;element ref="{http://mfxV2_0}NomCompleto" minOccurs="0"/>
 *         &lt;element ref="{http://mfxV2_0}DocTp" minOccurs="0"/>
 *         &lt;element ref="{http://mfxV2_0}DocNro" minOccurs="0"/>
 *         &lt;element ref="{http://mfxV2_0}MedioAbono" minOccurs="0"/>
 *         &lt;element ref="{http://mfxV2_0}SvcEmpDocLst" minOccurs="0"/>
 *         &lt;element ref="{http://mfxV2_0}SvcEmpDocAdicLst" minOccurs="0"/>
 *         &lt;element ref="{http://mfxV2_0}OpnRefAdicLst" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SvcEmp_Type", propOrder = {
    "svcId",
    "pagTp",
    "pagId",
    "nomCompleto",
    "docTp",
    "docNro",
    "medioAbono",
    "svcEmpDocLst",
    "svcEmpDocAdicLst",
    "opnRefAdicLst"
})
public class SvcEmpType {

    @XmlElement(name = "SvcId", required = true)
    protected String svcId;
    @XmlElement(name = "PagTp")
    protected String pagTp;
    @XmlElement(name = "PagId", required = true)
    protected String pagId;
    @XmlElement(name = "NomCompleto")
    protected String nomCompleto;
    @XmlElement(name = "DocTp")
    protected String docTp;
    @XmlElement(name = "DocNro")
    protected String docNro;
    @XmlElement(name = "MedioAbono")
    protected MedioAbonoType medioAbono;
    @XmlElement(name = "SvcEmpDocLst")
    protected SvcEmpDocLstType svcEmpDocLst;
    @XmlElement(name = "SvcEmpDocAdicLst")
    protected SvcEmpDocAdicLstType svcEmpDocAdicLst;
    @XmlElement(name = "OpnRefAdicLst")
    protected OpnRefAdicLstType opnRefAdicLst;

    /**
     * Obtiene el valor de la propiedad svcId.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSvcId() {
        return svcId;
    }

    /**
     * Define el valor de la propiedad svcId.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSvcId(String value) {
        this.svcId = value;
    }

    /**
     * Obtiene el valor de la propiedad pagTp.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPagTp() {
        return pagTp;
    }

    /**
     * Define el valor de la propiedad pagTp.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPagTp(String value) {
        this.pagTp = value;
    }

    /**
     * Obtiene el valor de la propiedad pagId.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPagId() {
        return pagId;
    }

    /**
     * Define el valor de la propiedad pagId.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPagId(String value) {
        this.pagId = value;
    }

    /**
     * Obtiene el valor de la propiedad nomCompleto.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNomCompleto() {
        return nomCompleto;
    }

    /**
     * Define el valor de la propiedad nomCompleto.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNomCompleto(String value) {
        this.nomCompleto = value;
    }

    /**
     * Obtiene el valor de la propiedad docTp.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDocTp() {
        return docTp;
    }

    /**
     * Define el valor de la propiedad docTp.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDocTp(String value) {
        this.docTp = value;
    }

    /**
     * Obtiene el valor de la propiedad docNro.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDocNro() {
        return docNro;
    }

    /**
     * Define el valor de la propiedad docNro.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDocNro(String value) {
        this.docNro = value;
    }

    /**
     * Obtiene el valor de la propiedad medioAbono.
     * 
     * @return
     *     possible object is
     *     {@link MedioAbonoType }
     *     
     */
    public MedioAbonoType getMedioAbono() {
        return medioAbono;
    }

    /**
     * Define el valor de la propiedad medioAbono.
     * 
     * @param value
     *     allowed object is
     *     {@link MedioAbonoType }
     *     
     */
    public void setMedioAbono(MedioAbonoType value) {
        this.medioAbono = value;
    }

    /**
     * Obtiene el valor de la propiedad svcEmpDocLst.
     * 
     * @return
     *     possible object is
     *     {@link SvcEmpDocLstType }
     *     
     */
    public SvcEmpDocLstType getSvcEmpDocLst() {
        return svcEmpDocLst;
    }

    /**
     * Define el valor de la propiedad svcEmpDocLst.
     * 
     * @param value
     *     allowed object is
     *     {@link SvcEmpDocLstType }
     *     
     */
    public void setSvcEmpDocLst(SvcEmpDocLstType value) {
        this.svcEmpDocLst = value;
    }

    /**
     * Obtiene el valor de la propiedad svcEmpDocAdicLst.
     * 
     * @return
     *     possible object is
     *     {@link SvcEmpDocAdicLstType }
     *     
     */
    public SvcEmpDocAdicLstType getSvcEmpDocAdicLst() {
        return svcEmpDocAdicLst;
    }

    /**
     * Define el valor de la propiedad svcEmpDocAdicLst.
     * 
     * @param value
     *     allowed object is
     *     {@link SvcEmpDocAdicLstType }
     *     
     */
    public void setSvcEmpDocAdicLst(SvcEmpDocAdicLstType value) {
        this.svcEmpDocAdicLst = value;
    }

    /**
     * Obtiene el valor de la propiedad opnRefAdicLst.
     * 
     * @return
     *     possible object is
     *     {@link OpnRefAdicLstType }
     *     
     */
    public OpnRefAdicLstType getOpnRefAdicLst() {
        return opnRefAdicLst;
    }

    /**
     * Define el valor de la propiedad opnRefAdicLst.
     * 
     * @param value
     *     allowed object is
     *     {@link OpnRefAdicLstType }
     *     
     */
    public void setOpnRefAdicLst(OpnRefAdicLstType value) {
        this.opnRefAdicLst = value;
    }

}
