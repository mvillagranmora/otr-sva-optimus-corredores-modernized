
package cl.bcs.custodia.utils.ClientWS.ejercicio_bloqueo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para ptde_consultaejercicios_ejercicioBloqueoExecuteResponse complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="ptde_consultaejercicios_ejercicioBloqueoExecuteResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="return" type="{http://ejercicio_bloqueo.ws.bcs.cl/}ejercicioBloqueoResponse" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ptde_consultaejercicios_ejercicioBloqueoExecuteResponse", propOrder = {
    "_return"
})
public class PtdeConsultaejerciciosEjercicioBloqueoExecuteResponse {

    @XmlElement(name = "return")
    protected EjercicioBloqueoResponse _return;

    /**
     * Obtiene el valor de la propiedad return.
     * 
     * @return
     *     possible object is
     *     {@link EjercicioBloqueoResponse }
     *     
     */
    public EjercicioBloqueoResponse getReturn() {
        return _return;
    }

    /**
     * Define el valor de la propiedad return.
     * 
     * @param value
     *     allowed object is
     *     {@link EjercicioBloqueoResponse }
     *     
     */
    public void setReturn(EjercicioBloqueoResponse value) {
        this._return = value;
    }

}
