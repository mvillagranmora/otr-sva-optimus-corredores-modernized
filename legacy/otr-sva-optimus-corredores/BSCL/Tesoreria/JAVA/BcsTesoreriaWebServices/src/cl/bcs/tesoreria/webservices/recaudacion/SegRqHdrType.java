
package cl.bcs.tesoreria.webservices.recaudacion;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para SegRqHdr_Type complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="SegRqHdr_Type">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://mfxV2_0}ProdEmpAutzFlg" minOccurs="0"/>
 *         &lt;element ref="{http://mfxV2_0}ProdEspAutzFlg" minOccurs="0"/>
 *         &lt;element ref="{http://mfxV2_0}TrnNivelAutz" minOccurs="0"/>
 *         &lt;element ref="{http://mfxV2_0}CredencialRqHdrLst"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SegRqHdr_Type", propOrder = {
    "prodEmpAutzFlg",
    "prodEspAutzFlg",
    "trnNivelAutz",
    "credencialRqHdrLst"
})
public class SegRqHdrType {

    @XmlElement(name = "ProdEmpAutzFlg")
    protected String prodEmpAutzFlg;
    @XmlElement(name = "ProdEspAutzFlg")
    protected String prodEspAutzFlg;
    @XmlElement(name = "TrnNivelAutz")
    @XmlSchemaType(name = "string")
    protected TrnNivelAutzType trnNivelAutz;
    @XmlElement(name = "CredencialRqHdrLst", required = true)
    protected CredencialRqHdrLstType credencialRqHdrLst;

    /**
     * Obtiene el valor de la propiedad prodEmpAutzFlg.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProdEmpAutzFlg() {
        return prodEmpAutzFlg;
    }

    /**
     * Define el valor de la propiedad prodEmpAutzFlg.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProdEmpAutzFlg(String value) {
        this.prodEmpAutzFlg = value;
    }

    /**
     * Obtiene el valor de la propiedad prodEspAutzFlg.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProdEspAutzFlg() {
        return prodEspAutzFlg;
    }

    /**
     * Define el valor de la propiedad prodEspAutzFlg.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProdEspAutzFlg(String value) {
        this.prodEspAutzFlg = value;
    }

    /**
     * Obtiene el valor de la propiedad trnNivelAutz.
     * 
     * @return
     *     possible object is
     *     {@link TrnNivelAutzType }
     *     
     */
    public TrnNivelAutzType getTrnNivelAutz() {
        return trnNivelAutz;
    }

    /**
     * Define el valor de la propiedad trnNivelAutz.
     * 
     * @param value
     *     allowed object is
     *     {@link TrnNivelAutzType }
     *     
     */
    public void setTrnNivelAutz(TrnNivelAutzType value) {
        this.trnNivelAutz = value;
    }

    /**
     * Obtiene el valor de la propiedad credencialRqHdrLst.
     * 
     * @return
     *     possible object is
     *     {@link CredencialRqHdrLstType }
     *     
     */
    public CredencialRqHdrLstType getCredencialRqHdrLst() {
        return credencialRqHdrLst;
    }

    /**
     * Define el valor de la propiedad credencialRqHdrLst.
     * 
     * @param value
     *     allowed object is
     *     {@link CredencialRqHdrLstType }
     *     
     */
    public void setCredencialRqHdrLst(CredencialRqHdrLstType value) {
        this.credencialRqHdrLst = value;
    }

}
