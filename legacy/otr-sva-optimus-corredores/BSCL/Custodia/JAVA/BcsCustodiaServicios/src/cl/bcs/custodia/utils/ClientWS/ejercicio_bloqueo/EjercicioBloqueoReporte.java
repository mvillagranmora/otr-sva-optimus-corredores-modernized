
package cl.bcs.custodia.utils.ClientWS.ejercicio_bloqueo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para ejercicioBloqueoReporte complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="ejercicioBloqueoReporte">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="codigoRespuesta" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="estadoIngreso" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ejercicioBloqueoReporte", propOrder = {
    "codigoRespuesta",
    "estadoIngreso"
})
public class EjercicioBloqueoReporte {

    protected Integer codigoRespuesta;
    protected String estadoIngreso;

    /**
     * Obtiene el valor de la propiedad codigoRespuesta.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getCodigoRespuesta() {
        return codigoRespuesta;
    }

    /**
     * Define el valor de la propiedad codigoRespuesta.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setCodigoRespuesta(Integer value) {
        this.codigoRespuesta = value;
    }

    /**
     * Obtiene el valor de la propiedad estadoIngreso.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEstadoIngreso() {
        return estadoIngreso;
    }

    /**
     * Define el valor de la propiedad estadoIngreso.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEstadoIngreso(String value) {
        this.estadoIngreso = value;
    }

}
