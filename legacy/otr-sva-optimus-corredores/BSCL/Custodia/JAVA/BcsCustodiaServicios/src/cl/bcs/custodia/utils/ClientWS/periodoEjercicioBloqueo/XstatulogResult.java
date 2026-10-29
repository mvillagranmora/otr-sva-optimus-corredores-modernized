
package cl.bcs.custodia.utils.ClientWS.periodoEjercicioBloqueo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para xstatulogResult complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="xstatulogResult">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="HORA_FIN" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="HORA_INI" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="XStatusLogs" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "xstatulogResult", propOrder = {
    "horafin",
    "horaini",
    "xStatusLogs"
})
public class XstatulogResult {

    @XmlElement(name = "HORA_FIN")
    protected String horafin;
    @XmlElement(name = "HORA_INI")
    protected String horaini;
    @XmlElement(name = "XStatusLogs")
    protected String xStatusLogs;

    /**
     * Obtiene el valor de la propiedad horafin.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHORAFIN() {
        return horafin;
    }

    /**
     * Define el valor de la propiedad horafin.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHORAFIN(String value) {
        this.horafin = value;
    }

    /**
     * Obtiene el valor de la propiedad horaini.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHORAINI() {
        return horaini;
    }

    /**
     * Define el valor de la propiedad horaini.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHORAINI(String value) {
        this.horaini = value;
    }

    /**
     * Obtiene el valor de la propiedad xStatusLogs.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getXStatusLogs() {
        return xStatusLogs;
    }

    /**
     * Define el valor de la propiedad xStatusLogs.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setXStatusLogs(String value) {
        this.xStatusLogs = value;
    }

}
