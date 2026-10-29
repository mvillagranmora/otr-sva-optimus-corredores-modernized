
package cl.bcs.custodia.utils.ClientWS.ejercicio_bloqueo;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para ptde_consultaejercicios_ejercicioBloqueoExecute complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="ptde_consultaejercicios_ejercicioBloqueoExecute">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="cod_corr_ejecutor" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ejerciciosBloqueos" type="{http://ejercicio_bloqueo.ws.bcs.cl/}ejercicioBloqueo" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ptde_consultaejercicios_ejercicioBloqueoExecute", propOrder = {
    "codCorrEjecutor",
    "ejerciciosBloqueos"
})
public class PtdeConsultaejerciciosEjercicioBloqueoExecute {

    @XmlElement(name = "cod_corr_ejecutor")
    protected String codCorrEjecutor;
    @XmlElement(nillable = true)
    protected List<EjercicioBloqueo> ejerciciosBloqueos;

    /**
     * Obtiene el valor de la propiedad codCorrEjecutor.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCodCorrEjecutor() {
        return codCorrEjecutor;
    }

    /**
     * Define el valor de la propiedad codCorrEjecutor.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCodCorrEjecutor(String value) {
        this.codCorrEjecutor = value;
    }

    /**
     * Gets the value of the ejerciciosBloqueos property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the ejerciciosBloqueos property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEjerciciosBloqueos().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link EjercicioBloqueo }
     * 
     * 
     */
    public List<EjercicioBloqueo> getEjerciciosBloqueos() {
        if (ejerciciosBloqueos == null) {
            ejerciciosBloqueos = new ArrayList<EjercicioBloqueo>();
        }
        return this.ejerciciosBloqueos;
    }

}
