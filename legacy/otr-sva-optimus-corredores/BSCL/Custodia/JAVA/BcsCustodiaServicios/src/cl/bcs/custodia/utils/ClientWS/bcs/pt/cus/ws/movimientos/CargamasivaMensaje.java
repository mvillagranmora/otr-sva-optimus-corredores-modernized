
package cl.bcs.custodia.utils.ClientWS.bcs.pt.cus.ws.movimientos;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para CargamasivaMensaje complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="CargamasivaMensaje">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="cantidadSolicitudesNotok" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="cantidadSolicitudesOk" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="corredor" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="fecha" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="glosa" type="{http://movimientos.ws.cus.pt.bcs.cl/}glosa" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="usuario" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CargamasivaMensaje", propOrder = {
    "cantidadSolicitudesNotok",
    "cantidadSolicitudesOk",
    "corredor",
    "fecha",
    "glosa",
    "usuario"
})
public class CargamasivaMensaje {

    protected int cantidadSolicitudesNotok;
    protected int cantidadSolicitudesOk;
    protected String corredor;
    protected String fecha;
    @XmlElement(nillable = true)
    protected List<Glosa> glosa;
    protected String usuario;

    /**
     * Obtiene el valor de la propiedad cantidadSolicitudesNotok.
     * 
     */
    public int getCantidadSolicitudesNotok() {
        return cantidadSolicitudesNotok;
    }

    /**
     * Define el valor de la propiedad cantidadSolicitudesNotok.
     * 
     */
    public void setCantidadSolicitudesNotok(int value) {
        this.cantidadSolicitudesNotok = value;
    }

    /**
     * Obtiene el valor de la propiedad cantidadSolicitudesOk.
     * 
     */
    public int getCantidadSolicitudesOk() {
        return cantidadSolicitudesOk;
    }

    /**
     * Define el valor de la propiedad cantidadSolicitudesOk.
     * 
     */
    public void setCantidadSolicitudesOk(int value) {
        this.cantidadSolicitudesOk = value;
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
     * Gets the value of the glosa property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the glosa property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getGlosa().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Glosa }
     * 
     * 
     */
    public List<Glosa> getGlosa() {
        if (glosa == null) {
            glosa = new ArrayList<Glosa>();
        }
        return this.glosa;
    }

    /**
     * Obtiene el valor de la propiedad usuario.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUsuario() {
        return usuario;
    }

    /**
     * Define el valor de la propiedad usuario.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUsuario(String value) {
        this.usuario = value;
    }

}
