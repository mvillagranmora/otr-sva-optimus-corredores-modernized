
package cl.bcs.tesoreria.infrastructure.soap;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para CardInqRef_Type complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="CardInqRef_Type">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://mfxV2_0}ProdNro"/>
 *         &lt;element ref="{http://mfxV2_0}ClsEstProdCod" minOccurs="0"/>
 *         &lt;element ref="{http://mfxV2_0}MonCod" minOccurs="0"/>
 *         &lt;element ref="{http://mfxV2_0}CardSec" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CardInqRef_Type", propOrder = {
    "prodNro",
    "clsEstProdCod",
    "monCod",
    "cardSec"
})
public class CardInqRefType {

    @XmlElement(name = "ProdNro", required = true)
    protected String prodNro;
    @XmlElement(name = "ClsEstProdCod")
    protected String clsEstProdCod;
    @XmlElement(name = "MonCod")
    protected String monCod;
    @XmlElement(name = "CardSec")
    protected String cardSec;

    /**
     * Obtiene el valor de la propiedad prodNro.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProdNro() {
        return prodNro;
    }

    /**
     * Define el valor de la propiedad prodNro.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProdNro(String value) {
        this.prodNro = value;
    }

    /**
     * Obtiene el valor de la propiedad clsEstProdCod.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getClsEstProdCod() {
        return clsEstProdCod;
    }

    /**
     * Define el valor de la propiedad clsEstProdCod.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClsEstProdCod(String value) {
        this.clsEstProdCod = value;
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
     * Obtiene el valor de la propiedad cardSec.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCardSec() {
        return cardSec;
    }

    /**
     * Define el valor de la propiedad cardSec.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCardSec(String value) {
        this.cardSec = value;
    }

}
