
package cl.bcs.custodia.utils.ClientWS.sorteoContratos;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para ptde_dopc_cuenta_derivados_opcExecute complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="ptde_dopc_cuenta_derivados_opcExecute">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="fecha" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="cod_corr" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="fondo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="rut_clie" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="fondo_clie" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="mercado" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="familia" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="subyacente" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="fec_vcto" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ptde_dopc_cuenta_derivados_opcExecute", propOrder = {
    "fecha",
    "codCorr",
    "fondo",
    "rutClie",
    "fondoClie",
    "mercado",
    "familia",
    "subyacente",
    "fecVcto"
})
public class PtdeDopcCuentaDerivadosOpcExecute {

    protected String fecha;
    @XmlElement(name = "cod_corr")
    protected String codCorr;
    protected String fondo;
    @XmlElement(name = "rut_clie")
    protected String rutClie;
    @XmlElement(name = "fondo_clie")
    protected String fondoClie;
    protected String mercado;
    protected String familia;
    protected String subyacente;
    @XmlElement(name = "fec_vcto")
    protected String fecVcto;

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
     * Obtiene el valor de la propiedad codCorr.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCodCorr() {
        return codCorr;
    }

    /**
     * Define el valor de la propiedad codCorr.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCodCorr(String value) {
        this.codCorr = value;
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
     * Obtiene el valor de la propiedad rutClie.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRutClie() {
        return rutClie;
    }

    /**
     * Define el valor de la propiedad rutClie.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRutClie(String value) {
        this.rutClie = value;
    }

    /**
     * Obtiene el valor de la propiedad fondoClie.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFondoClie() {
        return fondoClie;
    }

    /**
     * Define el valor de la propiedad fondoClie.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFondoClie(String value) {
        this.fondoClie = value;
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
     * Obtiene el valor de la propiedad familia.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFamilia() {
        return familia;
    }

    /**
     * Define el valor de la propiedad familia.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFamilia(String value) {
        this.familia = value;
    }

    /**
     * Obtiene el valor de la propiedad subyacente.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSubyacente() {
        return subyacente;
    }

    /**
     * Define el valor de la propiedad subyacente.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSubyacente(String value) {
        this.subyacente = value;
    }

    /**
     * Obtiene el valor de la propiedad fecVcto.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFecVcto() {
        return fecVcto;
    }

    /**
     * Define el valor de la propiedad fecVcto.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFecVcto(String value) {
        this.fecVcto = value;
    }

}
