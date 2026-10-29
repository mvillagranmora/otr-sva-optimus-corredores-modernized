
package cl.bcs.tesoreria.webservices.recaudacion;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para RegRqCtrl_Type complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="RegRqCtrl_Type">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://mfxV2_0}TpCursor" minOccurs="0"/>
 *         &lt;element ref="{http://mfxV2_0}LngPgn" minOccurs="0"/>
 *         &lt;element ref="{http://mfxV2_0}CtdMax" minOccurs="0"/>
 *         &lt;element ref="{http://mfxV2_0}Cursor" minOccurs="0"/>
 *         &lt;element ref="{http://mfxV2_0}RangoFec" minOccurs="0"/>
 *         &lt;element ref="{http://mfxV2_0}RangoMto" minOccurs="0"/>
 *         &lt;element ref="{http://mfxV2_0}RangoVal" minOccurs="0"/>
 *         &lt;element ref="{http://mfxV2_0}TrnSelector" minOccurs="0"/>
 *         &lt;element ref="{http://mfxV2_0}TrnTp" minOccurs="0"/>
 *         &lt;element ref="{http://mfxV2_0}InqSelector" minOccurs="0"/>
 *         &lt;element ref="{http://mfxV2_0}SvcSelector" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RegRqCtrl_Type", propOrder = {
    "tpCursor",
    "lngPgn",
    "ctdMax",
    "cursor",
    "rangoFec",
    "rangoMto",
    "rangoVal",
    "trnSelector",
    "trnTp",
    "inqSelector",
    "svcSelector"
})
public class RegRqCtrlType {

    @XmlElement(name = "TpCursor")
    protected String tpCursor;
    @XmlElement(name = "LngPgn")
    protected Long lngPgn;
    @XmlElement(name = "CtdMax")
    protected Long ctdMax;
    @XmlElement(name = "Cursor")
    protected String cursor;
    @XmlElement(name = "RangoFec")
    protected RangoFecType rangoFec;
    @XmlElement(name = "RangoMto")
    protected RangoMtoType rangoMto;
    @XmlElement(name = "RangoVal")
    protected RangoValType rangoVal;
    @XmlElement(name = "TrnSelector")
    protected TrnSelectorType trnSelector;
    @XmlElement(name = "TrnTp")
    @XmlSchemaType(name = "string")
    protected TrnTpType trnTp;
    @XmlElement(name = "InqSelector")
    protected InqSelectorType inqSelector;
    @XmlElement(name = "SvcSelector")
    protected String svcSelector;

    /**
     * Obtiene el valor de la propiedad tpCursor.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTpCursor() {
        return tpCursor;
    }

    /**
     * Define el valor de la propiedad tpCursor.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTpCursor(String value) {
        this.tpCursor = value;
    }

    /**
     * Obtiene el valor de la propiedad lngPgn.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getLngPgn() {
        return lngPgn;
    }

    /**
     * Define el valor de la propiedad lngPgn.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setLngPgn(Long value) {
        this.lngPgn = value;
    }

    /**
     * Obtiene el valor de la propiedad ctdMax.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getCtdMax() {
        return ctdMax;
    }

    /**
     * Define el valor de la propiedad ctdMax.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setCtdMax(Long value) {
        this.ctdMax = value;
    }

    /**
     * Obtiene el valor de la propiedad cursor.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCursor() {
        return cursor;
    }

    /**
     * Define el valor de la propiedad cursor.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCursor(String value) {
        this.cursor = value;
    }

    /**
     * Obtiene el valor de la propiedad rangoFec.
     * 
     * @return
     *     possible object is
     *     {@link RangoFecType }
     *     
     */
    public RangoFecType getRangoFec() {
        return rangoFec;
    }

    /**
     * Define el valor de la propiedad rangoFec.
     * 
     * @param value
     *     allowed object is
     *     {@link RangoFecType }
     *     
     */
    public void setRangoFec(RangoFecType value) {
        this.rangoFec = value;
    }

    /**
     * Obtiene el valor de la propiedad rangoMto.
     * 
     * @return
     *     possible object is
     *     {@link RangoMtoType }
     *     
     */
    public RangoMtoType getRangoMto() {
        return rangoMto;
    }

    /**
     * Define el valor de la propiedad rangoMto.
     * 
     * @param value
     *     allowed object is
     *     {@link RangoMtoType }
     *     
     */
    public void setRangoMto(RangoMtoType value) {
        this.rangoMto = value;
    }

    /**
     * Obtiene el valor de la propiedad rangoVal.
     * 
     * @return
     *     possible object is
     *     {@link RangoValType }
     *     
     */
    public RangoValType getRangoVal() {
        return rangoVal;
    }

    /**
     * Define el valor de la propiedad rangoVal.
     * 
     * @param value
     *     allowed object is
     *     {@link RangoValType }
     *     
     */
    public void setRangoVal(RangoValType value) {
        this.rangoVal = value;
    }

    /**
     * Obtiene el valor de la propiedad trnSelector.
     * 
     * @return
     *     possible object is
     *     {@link TrnSelectorType }
     *     
     */
    public TrnSelectorType getTrnSelector() {
        return trnSelector;
    }

    /**
     * Define el valor de la propiedad trnSelector.
     * 
     * @param value
     *     allowed object is
     *     {@link TrnSelectorType }
     *     
     */
    public void setTrnSelector(TrnSelectorType value) {
        this.trnSelector = value;
    }

    /**
     * Obtiene el valor de la propiedad trnTp.
     * 
     * @return
     *     possible object is
     *     {@link TrnTpType }
     *     
     */
    public TrnTpType getTrnTp() {
        return trnTp;
    }

    /**
     * Define el valor de la propiedad trnTp.
     * 
     * @param value
     *     allowed object is
     *     {@link TrnTpType }
     *     
     */
    public void setTrnTp(TrnTpType value) {
        this.trnTp = value;
    }

    /**
     * Obtiene el valor de la propiedad inqSelector.
     * 
     * @return
     *     possible object is
     *     {@link InqSelectorType }
     *     
     */
    public InqSelectorType getInqSelector() {
        return inqSelector;
    }

    /**
     * Define el valor de la propiedad inqSelector.
     * 
     * @param value
     *     allowed object is
     *     {@link InqSelectorType }
     *     
     */
    public void setInqSelector(InqSelectorType value) {
        this.inqSelector = value;
    }

    /**
     * Obtiene el valor de la propiedad svcSelector.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSvcSelector() {
        return svcSelector;
    }

    /**
     * Define el valor de la propiedad svcSelector.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSvcSelector(String value) {
        this.svcSelector = value;
    }

}
