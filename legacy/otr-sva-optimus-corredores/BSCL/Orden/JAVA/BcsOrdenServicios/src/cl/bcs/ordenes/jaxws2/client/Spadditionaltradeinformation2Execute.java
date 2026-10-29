
package cl.bcs.ordenes.jaxws2.client;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para spadditionaltradeinformation2Execute complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="spadditionaltradeinformation2Execute">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="folio" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="fecha" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="parte" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="corredor" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "spadditionaltradeinformation2Execute", propOrder = {
    "folio",
    "fecha",
    "parte",
    "corredor"
})
public class Spadditionaltradeinformation2Execute {

    protected String folio;
    protected String fecha;
    protected String parte;
    protected String corredor;

    /**
     * Obtiene el valor de la propiedad folio.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFolio() {
        return folio;
    }

    /**
     * Define el valor de la propiedad folio.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFolio(String value) {
        this.folio = value;
    }

    /**
     * Obtiene el valor de la propiedad fecha.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFecha() {
        return fecha;
    }

    /**
     * Define el valor de la propiedad fecha.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFecha(String value) {
        this.fecha = value;
    }

    /**
     * Obtiene el valor de la propiedad parte.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getParte() {
        return parte;
    }

    /**
     * Define el valor de la propiedad parte.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setParte(String value) {
        this.parte = value;
    }

    /**
     * Obtiene el valor de la propiedad corredor.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCorredor() {
        return corredor;
    }

    /**
     * Define el valor de la propiedad corredor.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCorredor(String value) {
        this.corredor = value;
    }

}
