
package cl.bcs.custodia.utils.ClientWS.ejercicio_bloqueo;

import java.math.BigDecimal;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para ejercicioBloqueo complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="ejercicioBloqueo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="agente" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="bloqueo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="cantEjercicio" type="{http://www.w3.org/2001/XMLSchema}decimal" minOccurs="0"/>
 *         &lt;element name="comitente" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="fondoAgente" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="fondoComitente" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="serie" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ejercicioBloqueo", propOrder = {
    "agente",
    "bloqueo",
    "cantEjercicio",
    "comitente",
    "fondoAgente",
    "fondoComitente",
    "serie"
})
public class EjercicioBloqueo {

    protected String agente;
    protected String bloqueo;
    protected BigDecimal cantEjercicio;
    protected String comitente;
    protected String fondoAgente;
    protected String fondoComitente;
    protected String serie;

    /**
     * Obtiene el valor de la propiedad agente.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAgente() {
        return agente;
    }

    /**
     * Define el valor de la propiedad agente.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAgente(String value) {
        this.agente = value;
    }

    /**
     * Obtiene el valor de la propiedad bloqueo.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBloqueo() {
        return bloqueo;
    }

    /**
     * Define el valor de la propiedad bloqueo.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBloqueo(String value) {
        this.bloqueo = value;
    }

    /**
     * Obtiene el valor de la propiedad cantEjercicio.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getCantEjercicio() {
        return cantEjercicio;
    }

    /**
     * Define el valor de la propiedad cantEjercicio.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setCantEjercicio(BigDecimal value) {
        this.cantEjercicio = value;
    }

    /**
     * Obtiene el valor de la propiedad comitente.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getComitente() {
        return comitente;
    }

    /**
     * Define el valor de la propiedad comitente.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setComitente(String value) {
        this.comitente = value;
    }

    /**
     * Obtiene el valor de la propiedad fondoAgente.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFondoAgente() {
        return fondoAgente;
    }

    /**
     * Define el valor de la propiedad fondoAgente.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFondoAgente(String value) {
        this.fondoAgente = value;
    }

    /**
     * Obtiene el valor de la propiedad fondoComitente.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFondoComitente() {
        return fondoComitente;
    }

    /**
     * Define el valor de la propiedad fondoComitente.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFondoComitente(String value) {
        this.fondoComitente = value;
    }

    /**
     * Obtiene el valor de la propiedad serie.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSerie() {
        return serie;
    }

    /**
     * Define el valor de la propiedad serie.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSerie(String value) {
        this.serie = value;
    }

}
