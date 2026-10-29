
package cl.bcs.custodia.utils.ClientWS.asignacionGestion;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para spingresarmodificarasignacionResponse complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="spingresarmodificarasignacionResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="insertasignacionResult" type="{http://Spingresarmodificarasignaciongestion.mantasignaciones.ws.pt.bcs.cl/}insertasignacionResult" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "spingresarmodificarasignacionResponse", propOrder = {
    "insertasignacionResult"
})
public class SpingresarmodificarasignacionResponse {

    @XmlElement(nillable = true)
    protected List<InsertasignacionResult> insertasignacionResult;

    /**
     * Gets the value of the insertasignacionResult property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the insertasignacionResult property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInsertasignacionResult().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link InsertasignacionResult }
     * 
     * 
     */
    public List<InsertasignacionResult> getInsertasignacionResult() {
        if (insertasignacionResult == null) {
            insertasignacionResult = new ArrayList<InsertasignacionResult>();
        }
        return this.insertasignacionResult;
    }

}
