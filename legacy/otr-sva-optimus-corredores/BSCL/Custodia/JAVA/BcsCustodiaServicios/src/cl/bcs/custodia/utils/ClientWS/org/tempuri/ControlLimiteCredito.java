
package cl.bcs.custodia.utils.ClientWS.org.tempuri;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Rut_Cliente" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="TipoFacility" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Moneda" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="DiasPlazo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Monto_Operacion" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="NroSolicitudBCS" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "rutCliente",
    "tipoFacility",
    "moneda",
    "diasPlazo",
    "montoOperacion",
    "nroSolicitudBCS"
})
@XmlRootElement(name = "ControlLimiteCredito")
public class ControlLimiteCredito {

    @XmlElement(name = "Rut_Cliente")
    protected String rutCliente;
    @XmlElement(name = "TipoFacility")
    protected String tipoFacility;
    @XmlElement(name = "Moneda")
    protected String moneda;
    @XmlElement(name = "DiasPlazo")
    protected String diasPlazo;
    @XmlElement(name = "Monto_Operacion")
    protected String montoOperacion;
    @XmlElement(name = "NroSolicitudBCS")
    protected String nroSolicitudBCS;

    /**
     * Gets the value of the rutCliente property.
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
     * Sets the value of the rutCliente property.
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
     * Gets the value of the tipoFacility property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTipoFacility() {
        return tipoFacility;
    }

    /**
     * Sets the value of the tipoFacility property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTipoFacility(String value) {
        this.tipoFacility = value;
    }

    /**
     * Gets the value of the moneda property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMoneda() {
        return moneda;
    }

    /**
     * Sets the value of the moneda property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMoneda(String value) {
        this.moneda = value;
    }

    /**
     * Gets the value of the diasPlazo property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDiasPlazo() {
        return diasPlazo;
    }

    /**
     * Sets the value of the diasPlazo property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDiasPlazo(String value) {
        this.diasPlazo = value;
    }

    /**
     * Gets the value of the montoOperacion property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMontoOperacion() {
        return montoOperacion;
    }

    /**
     * Sets the value of the montoOperacion property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMontoOperacion(String value) {
        this.montoOperacion = value;
    }

    /**
     * Gets the value of the nroSolicitudBCS property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNroSolicitudBCS() {
        return nroSolicitudBCS;
    }

    /**
     * Sets the value of the nroSolicitudBCS property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNroSolicitudBCS(String value) {
        this.nroSolicitudBCS = value;
    }

}
