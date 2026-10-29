
package cl.bcs.custodia.utils.ClientWS.asignacionGestion;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para spingresarmodificarasignaciongestionExecute complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="spingresarmodificarasignaciongestionExecute">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="arg0" type="{http://Spingresarmodificarasignaciongestion.mantasignaciones.ws.pt.bcs.cl/}ingresoAsignacion" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SpingresarmodificarasignaciongestionExecute", propOrder = {
    "arg0"
})
public class SpingresarmodificarasignaciongestionExecute {

    @XmlElement(nillable = true)
    protected List<IngresoAsignacion> arg0;

    /**
     * Gets the value of the arg0 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the arg0 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getArg0().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link IngresoAsignacion }
     * 
     * 
     */
    public List<IngresoAsignacion> getArg0() {
        if (arg0 == null) {
            arg0 = new ArrayList<IngresoAsignacion>();
        }
        return this.arg0;
    }

}
