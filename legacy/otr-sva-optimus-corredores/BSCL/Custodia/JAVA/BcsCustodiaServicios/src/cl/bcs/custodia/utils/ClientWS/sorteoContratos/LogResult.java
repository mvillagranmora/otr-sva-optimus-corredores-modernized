
package cl.bcs.custodia.utils.ClientWS.sorteoContratos;

import java.math.BigDecimal;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para logResult complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="logResult">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="CANT_CONTRATOS_TR" type="{http://www.w3.org/2001/XMLSchema}decimal" minOccurs="0"/>
 *         &lt;element name="COD_CORR" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="CONT_ABIERTOS_NETO" type="{http://www.w3.org/2001/XMLSchema}decimal" minOccurs="0"/>
 *         &lt;element name="CONT_ABIERTO_DA" type="{http://www.w3.org/2001/XMLSchema}decimal" minOccurs="0"/>
 *         &lt;element name="CONT_ABIERTO_DIA" type="{http://www.w3.org/2001/XMLSchema}decimal" minOccurs="0"/>
 *         &lt;element name="CONT_EJERCIDOS" type="{http://www.w3.org/2001/XMLSchema}decimal" minOccurs="0"/>
 *         &lt;element name="CONT_SORTEADOS" type="{http://www.w3.org/2001/XMLSchema}decimal" minOccurs="0"/>
 *         &lt;element name="FAMILIA" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="FECHA" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="FEC_VCTO" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="FONDO" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="FONDO_CLIE" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="LADO_DA" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="LADO_DIA" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="LADO_NETO" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="LADO_TR" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="LOTE_PADRON" type="{http://www.w3.org/2001/XMLSchema}decimal" minOccurs="0"/>
 *         &lt;element name="MERCADO" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="PRE_SUBYACENTE" type="{http://www.w3.org/2001/XMLSchema}decimal" minOccurs="0"/>
 *         &lt;element name="RUT_CLIE" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="SALDO_EJERCICIO" type="{http://www.w3.org/2001/XMLSchema}decimal" minOccurs="0"/>
 *         &lt;element name="SALDO_NETO" type="{http://www.w3.org/2001/XMLSchema}decimal" minOccurs="0"/>
 *         &lt;element name="SALDO_PRIMA" type="{http://www.w3.org/2001/XMLSchema}decimal" minOccurs="0"/>
 *         &lt;element name="SALDO_SORTEO" type="{http://www.w3.org/2001/XMLSchema}decimal" minOccurs="0"/>
 *         &lt;element name="SERIE" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="STRIKE" type="{http://www.w3.org/2001/XMLSchema}decimal" minOccurs="0"/>
 *         &lt;element name="SUBYACENTE" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="TIPO_OP" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "logResult", propOrder = {
    "cantcontratostr",
    "codcorr",
    "contabiertosneto",
    "contabiertoda",
    "contabiertodia",
    "contejercidos",
    "contsorteados",
    "familia",
    "fecha",
    "fecvcto",
    "fondo",
    "fondoclie",
    "ladoda",
    "ladodia",
    "ladoneto",
    "ladotr",
    "lotepadron",
    "mercado",
    "presubyacente",
    "rutclie",
    "saldoejercicio",
    "saldoneto",
    "saldoprima",
    "saldosorteo",
    "serie",
    "strike",
    "subyacente",
    "tipoop"
})
public class LogResult {

    @XmlElement(name = "CANT_CONTRATOS_TR")
    protected BigDecimal cantcontratostr;
    @XmlElement(name = "COD_CORR")
    protected String codcorr;
    @XmlElement(name = "CONT_ABIERTOS_NETO")
    protected BigDecimal contabiertosneto;
    @XmlElement(name = "CONT_ABIERTO_DA")
    protected BigDecimal contabiertoda;
    @XmlElement(name = "CONT_ABIERTO_DIA")
    protected BigDecimal contabiertodia;
    @XmlElement(name = "CONT_EJERCIDOS")
    protected BigDecimal contejercidos;
    @XmlElement(name = "CONT_SORTEADOS")
    protected BigDecimal contsorteados;
    @XmlElement(name = "FAMILIA")
    protected String familia;
    @XmlElement(name = "FECHA")
    protected String fecha;
    @XmlElement(name = "FEC_VCTO")
    protected String fecvcto;
    @XmlElement(name = "FONDO")
    protected String fondo;
    @XmlElement(name = "FONDO_CLIE")
    protected String fondoclie;
    @XmlElement(name = "LADO_DA")
    protected String ladoda;
    @XmlElement(name = "LADO_DIA")
    protected String ladodia;
    @XmlElement(name = "LADO_NETO")
    protected String ladoneto;
    @XmlElement(name = "LADO_TR")
    protected String ladotr;
    @XmlElement(name = "LOTE_PADRON")
    protected BigDecimal lotepadron;
    @XmlElement(name = "MERCADO")
    protected String mercado;
    @XmlElement(name = "PRE_SUBYACENTE")
    protected BigDecimal presubyacente;
    @XmlElement(name = "RUT_CLIE")
    protected String rutclie;
    @XmlElement(name = "SALDO_EJERCICIO")
    protected BigDecimal saldoejercicio;
    @XmlElement(name = "SALDO_NETO")
    protected BigDecimal saldoneto;
    @XmlElement(name = "SALDO_PRIMA")
    protected BigDecimal saldoprima;
    @XmlElement(name = "SALDO_SORTEO")
    protected BigDecimal saldosorteo;
    @XmlElement(name = "SERIE")
    protected String serie;
    @XmlElement(name = "STRIKE")
    protected BigDecimal strike;
    @XmlElement(name = "SUBYACENTE")
    protected String subyacente;
    @XmlElement(name = "TIPO_OP")
    protected String tipoop;

    /**
     * Obtiene el valor de la propiedad cantcontratostr.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getCANTCONTRATOSTR() {
        return cantcontratostr;
    }

    /**
     * Define el valor de la propiedad cantcontratostr.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setCANTCONTRATOSTR(BigDecimal value) {
        this.cantcontratostr = value;
    }

    /**
     * Obtiene el valor de la propiedad codcorr.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCODCORR() {
        return codcorr;
    }

    /**
     * Define el valor de la propiedad codcorr.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCODCORR(String value) {
        this.codcorr = value;
    }

    /**
     * Obtiene el valor de la propiedad contabiertosneto.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getCONTABIERTOSNETO() {
        return contabiertosneto;
    }

    /**
     * Define el valor de la propiedad contabiertosneto.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setCONTABIERTOSNETO(BigDecimal value) {
        this.contabiertosneto = value;
    }

    /**
     * Obtiene el valor de la propiedad contabiertoda.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getCONTABIERTODA() {
        return contabiertoda;
    }

    /**
     * Define el valor de la propiedad contabiertoda.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setCONTABIERTODA(BigDecimal value) {
        this.contabiertoda = value;
    }

    /**
     * Obtiene el valor de la propiedad contabiertodia.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getCONTABIERTODIA() {
        return contabiertodia;
    }

    /**
     * Define el valor de la propiedad contabiertodia.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setCONTABIERTODIA(BigDecimal value) {
        this.contabiertodia = value;
    }

    /**
     * Obtiene el valor de la propiedad contejercidos.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getCONTEJERCIDOS() {
        return contejercidos;
    }

    /**
     * Define el valor de la propiedad contejercidos.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setCONTEJERCIDOS(BigDecimal value) {
        this.contejercidos = value;
    }

    /**
     * Obtiene el valor de la propiedad contsorteados.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getCONTSORTEADOS() {
        return contsorteados;
    }

    /**
     * Define el valor de la propiedad contsorteados.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setCONTSORTEADOS(BigDecimal value) {
        this.contsorteados = value;
    }

    /**
     * Obtiene el valor de la propiedad familia.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFAMILIA() {
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
    public void setFAMILIA(String value) {
        this.familia = value;
    }

    /**
     * Obtiene el valor de la propiedad fecha.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFECHA() {
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
    public void setFECHA(String value) {
        this.fecha = value;
    }

    /**
     * Obtiene el valor de la propiedad fecvcto.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFECVCTO() {
        return fecvcto;
    }

    /**
     * Define el valor de la propiedad fecvcto.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFECVCTO(String value) {
        this.fecvcto = value;
    }

    /**
     * Obtiene el valor de la propiedad fondo.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFONDO() {
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
    public void setFONDO(String value) {
        this.fondo = value;
    }

    /**
     * Obtiene el valor de la propiedad fondoclie.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFONDOCLIE() {
        return fondoclie;
    }

    /**
     * Define el valor de la propiedad fondoclie.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFONDOCLIE(String value) {
        this.fondoclie = value;
    }

    /**
     * Obtiene el valor de la propiedad ladoda.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLADODA() {
        return ladoda;
    }

    /**
     * Define el valor de la propiedad ladoda.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLADODA(String value) {
        this.ladoda = value;
    }

    /**
     * Obtiene el valor de la propiedad ladodia.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLADODIA() {
        return ladodia;
    }

    /**
     * Define el valor de la propiedad ladodia.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLADODIA(String value) {
        this.ladodia = value;
    }

    /**
     * Obtiene el valor de la propiedad ladoneto.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLADONETO() {
        return ladoneto;
    }

    /**
     * Define el valor de la propiedad ladoneto.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLADONETO(String value) {
        this.ladoneto = value;
    }

    /**
     * Obtiene el valor de la propiedad ladotr.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLADOTR() {
        return ladotr;
    }

    /**
     * Define el valor de la propiedad ladotr.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLADOTR(String value) {
        this.ladotr = value;
    }

    /**
     * Obtiene el valor de la propiedad lotepadron.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getLOTEPADRON() {
        return lotepadron;
    }

    /**
     * Define el valor de la propiedad lotepadron.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setLOTEPADRON(BigDecimal value) {
        this.lotepadron = value;
    }

    /**
     * Obtiene el valor de la propiedad mercado.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMERCADO() {
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
    public void setMERCADO(String value) {
        this.mercado = value;
    }

    /**
     * Obtiene el valor de la propiedad presubyacente.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getPRESUBYACENTE() {
        return presubyacente;
    }

    /**
     * Define el valor de la propiedad presubyacente.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setPRESUBYACENTE(BigDecimal value) {
        this.presubyacente = value;
    }

    /**
     * Obtiene el valor de la propiedad rutclie.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRUTCLIE() {
        return rutclie;
    }

    /**
     * Define el valor de la propiedad rutclie.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRUTCLIE(String value) {
        this.rutclie = value;
    }

    /**
     * Obtiene el valor de la propiedad saldoejercicio.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getSALDOEJERCICIO() {
        return saldoejercicio;
    }

    /**
     * Define el valor de la propiedad saldoejercicio.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setSALDOEJERCICIO(BigDecimal value) {
        this.saldoejercicio = value;
    }

    /**
     * Obtiene el valor de la propiedad saldoneto.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getSALDONETO() {
        return saldoneto;
    }

    /**
     * Define el valor de la propiedad saldoneto.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setSALDONETO(BigDecimal value) {
        this.saldoneto = value;
    }

    /**
     * Obtiene el valor de la propiedad saldoprima.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getSALDOPRIMA() {
        return saldoprima;
    }

    /**
     * Define el valor de la propiedad saldoprima.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setSALDOPRIMA(BigDecimal value) {
        this.saldoprima = value;
    }

    /**
     * Obtiene el valor de la propiedad saldosorteo.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getSALDOSORTEO() {
        return saldosorteo;
    }

    /**
     * Define el valor de la propiedad saldosorteo.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setSALDOSORTEO(BigDecimal value) {
        this.saldosorteo = value;
    }

    /**
     * Obtiene el valor de la propiedad serie.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSERIE() {
        return serie;
    }

    /**
     * Define el valor de la propiedad serie.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSERIE(String value) {
        this.serie = value;
    }

    /**
     * Obtiene el valor de la propiedad strike.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getSTRIKE() {
        return strike;
    }

    /**
     * Define el valor de la propiedad strike.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setSTRIKE(BigDecimal value) {
        this.strike = value;
    }

    /**
     * Obtiene el valor de la propiedad subyacente.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSUBYACENTE() {
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
    public void setSUBYACENTE(String value) {
        this.subyacente = value;
    }

    /**
     * Obtiene el valor de la propiedad tipoop.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTIPOOP() {
        return tipoop;
    }

    /**
     * Define el valor de la propiedad tipoop.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTIPOOP(String value) {
        this.tipoop = value;
    }

}
