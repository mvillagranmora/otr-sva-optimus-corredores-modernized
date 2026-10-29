
package cl.bcs.custodia.utils.ClientWS.asignacionGestion;

import java.math.BigDecimal;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import cl.bcs.utils.BcsPojo;


/**
 * <p>Clase Java para ingresoAsignacion complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="ingresoAsignacion">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="cantidad_asignada" type="{http://www.w3.org/2001/XMLSchema}decimal" minOccurs="0"/>
 *         &lt;element name="cod_corr" type="{http://www.w3.org/2001/XMLSchema}decimal" minOccurs="0"/>
 *         &lt;element name="fecha_op" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="folio" type="{http://www.w3.org/2001/XMLSchema}decimal" minOccurs="0"/>
 *         &lt;element name="id_gestion" type="{http://www.w3.org/2001/XMLSchema}decimal" minOccurs="0"/>
 *         &lt;element name="lado" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="modo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="nemo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="nombre_cli" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="rut_cliente" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="rut_cliente_nuevo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ingresoAsignacion", propOrder = {
    "cantidadAsignada",
    "codCorr",
    "fechaOp",
    "folio",
    "idGestion",
    "lado",
    "modo",
    "nemo",
    "nombreCli",
    "rutCliente",
    "rutClienteNuevo"
})
public class IngresoAsignacion extends BcsPojo {

    @XmlElement(name = "cantidad_asignada")
    protected BigDecimal cantidadAsignada;
    @XmlElement(name = "cod_corr")
    protected BigDecimal codCorr;
    @XmlElement(name = "fecha_op")
    protected String fechaOp;
    protected BigDecimal folio;
    @XmlElement(name = "id_gestion")
    protected BigDecimal idGestion;
    protected String lado;
    protected String modo;
    protected String nemo;
    @XmlElement(name = "nombre_cli")
    protected String nombreCli;
    @XmlElement(name = "rut_cliente")
    protected String rutCliente;
    @XmlElement(name = "rut_cliente_nuevo")
    protected String rutClienteNuevo;

    /**
     * Obtiene el valor de la propiedad cantidadAsignada.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getCantidadAsignada() {
        return cantidadAsignada;
    }

    /**
     * Define el valor de la propiedad cantidadAsignada.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setCantidadAsignada(BigDecimal value) {
        this.cantidadAsignada = value;
    }

    /**
     * Obtiene el valor de la propiedad codCorr.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getCodCorr() {
        return codCorr;
    }

    /**
     * Define el valor de la propiedad codCorr.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setCodCorr(BigDecimal value) {
        this.codCorr = value;
    }

    /**
     * Obtiene el valor de la propiedad fechaOp.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFechaOp() {
        return fechaOp;
    }

    /**
     * Define el valor de la propiedad fechaOp.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFechaOp(String value) {
        this.fechaOp = value;
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
     * Obtiene el valor de la propiedad idGestion.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getIdGestion() {
        return idGestion;
    }

    /**
     * Define el valor de la propiedad idGestion.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setIdGestion(BigDecimal value) {
        this.idGestion = value;
    }

    /**
     * Obtiene el valor de la propiedad lado.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLado() {
        return lado;
    }

    /**
     * Define el valor de la propiedad lado.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLado(String value) {
        this.lado = value;
    }

    /**
     * Obtiene el valor de la propiedad modo.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getModo() {
        return modo;
    }

    /**
     * Define el valor de la propiedad modo.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setModo(String value) {
        this.modo = value;
    }

    /**
     * Obtiene el valor de la propiedad nemo.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNemo() {
        return nemo;
    }

    /**
     * Define el valor de la propiedad nemo.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNemo(String value) {
        this.nemo = value;
    }

    /**
     * Obtiene el valor de la propiedad nombreCli.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNombreCli() {
        return nombreCli;
    }

    /**
     * Define el valor de la propiedad nombreCli.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNombreCli(String value) {
        this.nombreCli = value;
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
     * Obtiene el valor de la propiedad rutClienteNuevo.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRutClienteNuevo() {
        return rutClienteNuevo;
    }

    /**
     * Define el valor de la propiedad rutClienteNuevo.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRutClienteNuevo(String value) {
        this.rutClienteNuevo = value;
    }

}
