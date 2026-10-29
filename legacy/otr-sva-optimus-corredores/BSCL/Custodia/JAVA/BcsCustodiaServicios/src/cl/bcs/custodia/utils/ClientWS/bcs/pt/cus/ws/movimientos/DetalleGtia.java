
package cl.bcs.custodia.utils.ClientWS.bcs.pt.cus.ws.movimientos;

import java.math.BigDecimal;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para detalleGtia complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="detalleGtia">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="cantidad" type="{http://www.w3.org/2001/XMLSchema}decimal" minOccurs="0"/>
 *         &lt;element name="codigoPrestamo" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="codigoPrestamoDestino" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="codigoSerieOpc" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="codigoSerieOpcDestino" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="corControl" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="cuentaDestino" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="dcv" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="fondoPrestamista" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="fondoPrestamistaDestino" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="fondoafp" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ingegr" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="mercado" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="nemo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="rutClienteDestino" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="rutPrestamista" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="rutPrestamistaDestino" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="rutcli" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="sistema" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="tipogtia" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "detalleGtia", propOrder = {
    "cantidad",
    "codigoPrestamo",
    "codigoPrestamoDestino",
    "codigoSerieOpc",
    "codigoSerieOpcDestino",
    "corControl",
    "cuentaDestino",
    "dcv",
    "fondoPrestamista",
    "fondoPrestamistaDestino",
    "fondoafp",
    "ingegr",
    "mercado",
    "nemo",
    "rutClienteDestino",
    "rutPrestamista",
    "rutPrestamistaDestino",
    "rutcli",
    "sistema",
    "tipogtia"
})
public class DetalleGtia {

    protected BigDecimal cantidad;
    protected int codigoPrestamo;
    protected int codigoPrestamoDestino;
    protected String codigoSerieOpc;
    protected String codigoSerieOpcDestino;
    protected String corControl;
    protected String cuentaDestino;
    protected String dcv;
    protected String fondoPrestamista;
    protected String fondoPrestamistaDestino;
    protected String fondoafp;
    protected String ingegr;
    protected String mercado;
    protected String nemo;
    protected String rutClienteDestino;
    protected String rutPrestamista;
    protected String rutPrestamistaDestino;
    protected String rutcli;
    protected String sistema;
    protected String tipogtia;

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
     * Obtiene el valor de la propiedad codigoPrestamo.
     * 
     */
    public int getCodigoPrestamo() {
        return codigoPrestamo;
    }

    /**
     * Define el valor de la propiedad codigoPrestamo.
     * 
     */
    public void setCodigoPrestamo(int value) {
        this.codigoPrestamo = value;
    }

    /**
     * Obtiene el valor de la propiedad codigoPrestamoDestino.
     * 
     */
    public int getCodigoPrestamoDestino() {
        return codigoPrestamoDestino;
    }

    /**
     * Define el valor de la propiedad codigoPrestamoDestino.
     * 
     */
    public void setCodigoPrestamoDestino(int value) {
        this.codigoPrestamoDestino = value;
    }

    /**
     * Obtiene el valor de la propiedad codigoSerieOpc.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCodigoSerieOpc() {
        return codigoSerieOpc;
    }

    /**
     * Define el valor de la propiedad codigoSerieOpc.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCodigoSerieOpc(String value) {
        this.codigoSerieOpc = value;
    }

    /**
     * Obtiene el valor de la propiedad codigoSerieOpcDestino.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCodigoSerieOpcDestino() {
        return codigoSerieOpcDestino;
    }

    /**
     * Define el valor de la propiedad codigoSerieOpcDestino.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCodigoSerieOpcDestino(String value) {
        this.codigoSerieOpcDestino = value;
    }

    /**
     * Obtiene el valor de la propiedad corControl.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCorControl() {
        return corControl;
    }

    /**
     * Define el valor de la propiedad corControl.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCorControl(String value) {
        this.corControl = value;
    }

    /**
     * Obtiene el valor de la propiedad cuentaDestino.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCuentaDestino() {
        return cuentaDestino;
    }

    /**
     * Define el valor de la propiedad cuentaDestino.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCuentaDestino(String value) {
        this.cuentaDestino = value;
    }

    /**
     * Obtiene el valor de la propiedad dcv.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDcv() {
        return dcv;
    }

    /**
     * Define el valor de la propiedad dcv.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDcv(String value) {
        this.dcv = value;
    }

    /**
     * Obtiene el valor de la propiedad fondoPrestamista.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFondoPrestamista() {
        return fondoPrestamista;
    }

    /**
     * Define el valor de la propiedad fondoPrestamista.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFondoPrestamista(String value) {
        this.fondoPrestamista = value;
    }

    /**
     * Obtiene el valor de la propiedad fondoPrestamistaDestino.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFondoPrestamistaDestino() {
        return fondoPrestamistaDestino;
    }

    /**
     * Define el valor de la propiedad fondoPrestamistaDestino.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFondoPrestamistaDestino(String value) {
        this.fondoPrestamistaDestino = value;
    }

    /**
     * Obtiene el valor de la propiedad fondoafp.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFondoafp() {
        return fondoafp;
    }

    /**
     * Define el valor de la propiedad fondoafp.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFondoafp(String value) {
        this.fondoafp = value;
    }

    /**
     * Obtiene el valor de la propiedad ingegr.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIngegr() {
        return ingegr;
    }

    /**
     * Define el valor de la propiedad ingegr.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIngegr(String value) {
        this.ingegr = value;
    }

    /**
     * Obtiene el valor de la propiedad mercado.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMercado() {
        return mercado;
    }

    /**
     * Define el valor de la propiedad mercado.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMercado(String value) {
        this.mercado = value;
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
     * Obtiene el valor de la propiedad rutClienteDestino.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRutClienteDestino() {
        return rutClienteDestino;
    }

    /**
     * Define el valor de la propiedad rutClienteDestino.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRutClienteDestino(String value) {
        this.rutClienteDestino = value;
    }

    /**
     * Obtiene el valor de la propiedad rutPrestamista.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRutPrestamista() {
        return rutPrestamista;
    }

    /**
     * Define el valor de la propiedad rutPrestamista.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRutPrestamista(String value) {
        this.rutPrestamista = value;
    }

    /**
     * Obtiene el valor de la propiedad rutPrestamistaDestino.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRutPrestamistaDestino() {
        return rutPrestamistaDestino;
    }

    /**
     * Define el valor de la propiedad rutPrestamistaDestino.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRutPrestamistaDestino(String value) {
        this.rutPrestamistaDestino = value;
    }

    /**
     * Obtiene el valor de la propiedad rutcli.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRutcli() {
        return rutcli;
    }

    /**
     * Define el valor de la propiedad rutcli.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRutcli(String value) {
        this.rutcli = value;
    }

    /**
     * Obtiene el valor de la propiedad sistema.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSistema() {
        return sistema;
    }

    /**
     * Define el valor de la propiedad sistema.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSistema(String value) {
        this.sistema = value;
    }

    /**
     * Obtiene el valor de la propiedad tipogtia.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTipogtia() {
        return tipogtia;
    }

    /**
     * Define el valor de la propiedad tipogtia.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTipogtia(String value) {
        this.tipogtia = value;
    }

}
