
package cl.bcs.ordenes.jaxws2.client;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>Clase Java para additionaltradesResult complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="additionaltradesResult">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="BUYERCLIENTID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="BUYERENTERINGFIRMID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="BUYEREXECUTINGFIRMID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="COND_COD_12_TBRVTR" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="FOLIO_TBRVTR" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="HORA_TBRVTR" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="MONTO_TBRVTR" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="NEMO_TBRVTR" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ORDQTY" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="PRECIO_TBRVTR" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="PRICE" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="SELLERCLIENTID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="SELLERENTERINGFIRMID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="SELLEREXECUTINGFIRMID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="TRADEID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="TRANSACTTIME" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "additionaltradesResult", propOrder = {
    "buyerclientid",
    "buyerenteringfirmid",
    "buyerexecutingfirmid",
    "condcod12TBRVTR",
    "foliotbrvtr",
    "horatbrvtr",
    "montotbrvtr",
    "nemotbrvtr",
    "ordqty",
    "preciotbrvtr",
    "price",
    "sellerclientid",
    "sellerenteringfirmid",
    "sellerexecutingfirmid",
    "tradeid",
    "transacttime"
})
public class AdditionaltradesResult {

    @XmlElement(name = "BUYERCLIENTID")
    protected String buyerclientid;
    @XmlElement(name = "BUYERENTERINGFIRMID")
    protected String buyerenteringfirmid;
    @XmlElement(name = "BUYEREXECUTINGFIRMID")
    protected String buyerexecutingfirmid;
    @XmlElement(name = "COND_COD_12_TBRVTR")
    protected String condcod12TBRVTR;
    @XmlElement(name = "FOLIO_TBRVTR")
    protected String foliotbrvtr;
    @XmlElement(name = "HORA_TBRVTR")
    protected String horatbrvtr;
    @XmlElement(name = "MONTO_TBRVTR")
    protected String montotbrvtr;
    @XmlElement(name = "NEMO_TBRVTR")
    protected String nemotbrvtr;
    @XmlElement(name = "ORDQTY")
    protected String ordqty;
    @XmlElement(name = "PRECIO_TBRVTR")
    protected String preciotbrvtr;
    @XmlElement(name = "PRICE")
    protected String price;
    @XmlElement(name = "SELLERCLIENTID")
    protected String sellerclientid;
    @XmlElement(name = "SELLERENTERINGFIRMID")
    protected String sellerenteringfirmid;
    @XmlElement(name = "SELLEREXECUTINGFIRMID")
    protected String sellerexecutingfirmid;
    @XmlElement(name = "TRADEID")
    protected String tradeid;
    @XmlElement(name = "TRANSACTTIME")
    protected String transacttime;

    /**
     * Obtiene el valor de la propiedad buyerclientid.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBUYERCLIENTID() {
        return buyerclientid;
    }

    /**
     * Define el valor de la propiedad buyerclientid.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBUYERCLIENTID(String value) {
        this.buyerclientid = value;
    }

    /**
     * Obtiene el valor de la propiedad buyerenteringfirmid.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBUYERENTERINGFIRMID() {
        return buyerenteringfirmid;
    }

    /**
     * Define el valor de la propiedad buyerenteringfirmid.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBUYERENTERINGFIRMID(String value) {
        this.buyerenteringfirmid = value;
    }

    /**
     * Obtiene el valor de la propiedad buyerexecutingfirmid.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBUYEREXECUTINGFIRMID() {
        return buyerexecutingfirmid;
    }

    /**
     * Define el valor de la propiedad buyerexecutingfirmid.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBUYEREXECUTINGFIRMID(String value) {
        this.buyerexecutingfirmid = value;
    }

    /**
     * Obtiene el valor de la propiedad condcod12TBRVTR.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCONDCOD12TBRVTR() {
        return condcod12TBRVTR;
    }

    /**
     * Define el valor de la propiedad condcod12TBRVTR.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCONDCOD12TBRVTR(String value) {
        this.condcod12TBRVTR = value;
    }

    /**
     * Obtiene el valor de la propiedad foliotbrvtr.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFOLIOTBRVTR() {
        return foliotbrvtr;
    }

    /**
     * Define el valor de la propiedad foliotbrvtr.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFOLIOTBRVTR(String value) {
        this.foliotbrvtr = value;
    }

    /**
     * Obtiene el valor de la propiedad horatbrvtr.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHORATBRVTR() {
        return horatbrvtr;
    }

    /**
     * Define el valor de la propiedad horatbrvtr.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHORATBRVTR(String value) {
        this.horatbrvtr = value;
    }

    /**
     * Obtiene el valor de la propiedad montotbrvtr.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMONTOTBRVTR() {
        return montotbrvtr;
    }

    /**
     * Define el valor de la propiedad montotbrvtr.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMONTOTBRVTR(String value) {
        this.montotbrvtr = value;
    }

    /**
     * Obtiene el valor de la propiedad nemotbrvtr.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNEMOTBRVTR() {
        return nemotbrvtr;
    }

    /**
     * Define el valor de la propiedad nemotbrvtr.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNEMOTBRVTR(String value) {
        this.nemotbrvtr = value;
    }

    /**
     * Obtiene el valor de la propiedad ordqty.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getORDQTY() {
        return ordqty;
    }

    /**
     * Define el valor de la propiedad ordqty.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setORDQTY(String value) {
        this.ordqty = value;
    }

    /**
     * Obtiene el valor de la propiedad preciotbrvtr.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPRECIOTBRVTR() {
        return preciotbrvtr;
    }

    /**
     * Define el valor de la propiedad preciotbrvtr.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPRECIOTBRVTR(String value) {
        this.preciotbrvtr = value;
    }

    /**
     * Obtiene el valor de la propiedad price.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPRICE() {
        return price;
    }

    /**
     * Define el valor de la propiedad price.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPRICE(String value) {
        this.price = value;
    }

    /**
     * Obtiene el valor de la propiedad sellerclientid.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSELLERCLIENTID() {
        return sellerclientid;
    }

    /**
     * Define el valor de la propiedad sellerclientid.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSELLERCLIENTID(String value) {
        this.sellerclientid = value;
    }

    /**
     * Obtiene el valor de la propiedad sellerenteringfirmid.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSELLERENTERINGFIRMID() {
        return sellerenteringfirmid;
    }

    /**
     * Define el valor de la propiedad sellerenteringfirmid.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSELLERENTERINGFIRMID(String value) {
        this.sellerenteringfirmid = value;
    }

    /**
     * Obtiene el valor de la propiedad sellerexecutingfirmid.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSELLEREXECUTINGFIRMID() {
        return sellerexecutingfirmid;
    }

    /**
     * Define el valor de la propiedad sellerexecutingfirmid.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSELLEREXECUTINGFIRMID(String value) {
        this.sellerexecutingfirmid = value;
    }

    /**
     * Obtiene el valor de la propiedad tradeid.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTRADEID() {
        return tradeid;
    }

    /**
     * Define el valor de la propiedad tradeid.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTRADEID(String value) {
        this.tradeid = value;
    }

    /**
     * Obtiene el valor de la propiedad transacttime.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTRANSACTTIME() {
        return transacttime;
    }

    /**
     * Define el valor de la propiedad transacttime.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTRANSACTTIME(String value) {
        this.transacttime = value;
    }

}
