
package cl.bcs.custodia.utils.ClientWS.cclv.test.ws;

import java.math.BigDecimal;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para TypeAsignaciones complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="TypeAsignaciones">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Fecha" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Folio" type="{http://www.w3.org/2001/XMLSchema}decimal"/>
 *         &lt;element name="TipoOperacion" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="RutCliente" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Fondo" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Cantidad" type="{http://www.w3.org/2001/XMLSchema}decimal"/>
 *         &lt;element name="CodCorr" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="Usuario" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="CorControl" type="{http://www.w3.org/2001/XMLSchema}decimal"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TypeAsignaciones", propOrder = {
    "fecha",
    "folio",
    "tipoOperacion",
    "rutCliente",
    "fondo",
    "cantidad",
    "codCorr",
    "usuario",
    "corControl"
})
public class TypeAsignaciones {

    @XmlElement(name = "Fecha", required = true)
    protected String fecha;
    @XmlElement(name = "Folio", required = true)
    protected BigDecimal folio;
    @XmlElement(name = "TipoOperacion", required = true)
    protected String tipoOperacion;
    @XmlElement(name = "RutCliente", required = true)
    protected String rutCliente;
    @XmlElement(name = "Fondo", required = true)
    protected String fondo;
    @XmlElement(name = "Cantidad", required = true)
    protected BigDecimal cantidad;
    @XmlElement(name = "CodCorr")
    protected int codCorr;
    @XmlElement(name = "Usuario", required = true)
    protected String usuario;
    @XmlElement(name = "CorControl", required = true)
    protected BigDecimal corControl;

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
     * Obtiene el valor de la propiedad folio.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getFolio() {
        return folio;
    }

    /**
     * Define el valor de la propiedad folio.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setFolio(BigDecimal value) {
        this.folio = value;
    }

    /**
     * Obtiene el valor de la propiedad tipoOperacion.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTipoOperacion() {
        return tipoOperacion;
    }

    /**
     * Define el valor de la propiedad tipoOperacion.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTipoOperacion(String value) {
        this.tipoOperacion = value;
    }

    /**
     * Obtiene el valor de la propiedad rutCliente.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRutCliente() {
        return rutCliente;
    }

    /**
     * Define el valor de la propiedad rutCliente.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRutCliente(String value) {
        this.rutCliente = value;
    }

    /**
     * Obtiene el valor de la propiedad fondo.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFondo() {
        return fondo;
    }

    /**
     * Define el valor de la propiedad fondo.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFondo(String value) {
        this.fondo = value;
    }

    /**
     * Obtiene el valor de la propiedad cantidad.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getCantidad() {
        return cantidad;
    }

    /**
     * Define el valor de la propiedad cantidad.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setCantidad(BigDecimal value) {
        this.cantidad = value;
    }

    /**
     * Obtiene el valor de la propiedad codCorr.
     * 
     */
    public int getCodCorr() {
        return codCorr;
    }

    /**
     * Define el valor de la propiedad codCorr.
     * 
     */
    public void setCodCorr(int value) {
        this.codCorr = value;
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

    /**
     * Obtiene el valor de la propiedad corControl.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getCorControl() {
        return corControl;
    }

    /**
     * Define el valor de la propiedad corControl.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setCorControl(BigDecimal value) {
        this.corControl = value;
    }

}
