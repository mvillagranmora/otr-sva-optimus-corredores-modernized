
package cl.bcs.tesoreria.webservices.recaudacion;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para SvcEmpDoc_Type complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="SvcEmpDoc_Type">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://mfxV2_0}FecVct"/>
 *         &lt;element ref="{http://mfxV2_0}DocId" minOccurs="0"/>
 *         &lt;element ref="{http://mfxV2_0}FecEmision" minOccurs="0"/>
 *         &lt;element ref="{http://mfxV2_0}MonCod"/>
 *         &lt;element ref="{http://mfxV2_0}MtoDocLst"/>
 *         &lt;element ref="{http://mfxV2_0}DocRef" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SvcEmpDoc_Type", propOrder = {
    "fecVct",
    "docId",
    "fecEmision",
    "monCod",
    "mtoDocLst",
    "docRef"
})
public class SvcEmpDocType {

    @XmlElement(name = "FecVct", required = true)
    protected String fecVct;
    @XmlElement(name = "DocId")
    protected String docId;
    @XmlElement(name = "FecEmision")
    protected String fecEmision;
    @XmlElement(name = "MonCod", required = true)
    protected String monCod;
    @XmlElement(name = "MtoDocLst", required = true)
    protected MtoDocLstType mtoDocLst;
    @XmlElement(name = "DocRef")
    protected String docRef;

    /**
     * Obtiene el valor de la propiedad fecVct.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFecVct() {
        return fecVct;
    }

    /**
     * Define el valor de la propiedad fecVct.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFecVct(String value) {
        this.fecVct = value;
    }

    /**
     * Obtiene el valor de la propiedad docId.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDocId() {
        return docId;
    }

    /**
     * Define el valor de la propiedad docId.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDocId(String value) {
        this.docId = value;
    }

    /**
     * Obtiene el valor de la propiedad fecEmision.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFecEmision() {
        return fecEmision;
    }

    /**
     * Define el valor de la propiedad fecEmision.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFecEmision(String value) {
        this.fecEmision = value;
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

    /**
     * Obtiene el valor de la propiedad mtoDocLst.
     * 
     * @return
     *     possible object is
     *     {@link MtoDocLstType }
     *     
     */
    public MtoDocLstType getMtoDocLst() {
        return mtoDocLst;
    }

    /**
     * Define el valor de la propiedad mtoDocLst.
     * 
     * @param value
     *     allowed object is
     *     {@link MtoDocLstType }
     *     
     */
    public void setMtoDocLst(MtoDocLstType value) {
        this.mtoDocLst = value;
    }

    /**
     * Obtiene el valor de la propiedad docRef.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDocRef() {
        return docRef;
    }

    /**
     * Define el valor de la propiedad docRef.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDocRef(String value) {
        this.docRef = value;
    }

}
