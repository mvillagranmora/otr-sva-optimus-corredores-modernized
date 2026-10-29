
package cl.bcs.tesoreria.infrastructure.soap;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para SvcEmpDocAdic_Type complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="SvcEmpDocAdic_Type">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://mfxV2_0}DocId" minOccurs="0"/>
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
@XmlType(name = "SvcEmpDocAdic_Type", propOrder = {
    "docId",
    "docRef"
})
public class SvcEmpDocAdicType {

    @XmlElement(name = "DocId")
    protected String docId;
    @XmlElement(name = "DocRef")
    protected String docRef;

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
