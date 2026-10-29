package cl.bcs.tesoreria.javacall.distribucion;

import java.math.BigDecimal;
import java.sql.Date;

/**
 * One row of the mandate / load-file queries (legacy {@code ProcesarDistribucion.InRecaudacion}). Public mutable fields with the legacy defaults:
 * the SQL row mapper only sets the columns a query returns, so the initialisers below are part of the behaviour (e.g. a file row has FolioCSC "").
 */
public class InRecaudacion {

    public String CuentaCorriente = null;
    public Long IdTraspaso;
    public String IdentServicio;
    public Long IdCliente;
    public Long IdPortafolio;
    public Long IdPersonaEmisor;
    public String FechaDeposito;
    public String IdMedioDePago;
    public Long IdBanco;
    public long NumeroCuentaBancaria;
    public Long IdCuentaBanco;
    public String IdSerieFondo;
    public String Sucursal;
    public BigDecimal MontoDeposito;
    public BigDecimal MontoCSC;
    public BigDecimal MontoSAPV;
    public BigDecimal SumaTotal;
    public String Observacion;
    public String IdRegimenTributario = "";
    public String FolioCSC = "";
    public String FolioSAAPV = "";

    public Long IdMandatoPAC;
    public String EstadoPAC = "";
    public String EstadoSAAPV = "";
    public String EstadoLargoSAAPV = "";
    public String EstadoCSC = "";
    public String EstadoLargoCSC = "";
    public String NombreCompleto = "";
    public String DocumentoIdentidad = "";
    public String EstadoCliente = "";
    public String EstadoPortafolio = "";

    public String EstadoCargo = "";
    public String DescripcionEstadoCargo = "";
    public String EstadoCargoBanco = "";
    public String MonedaCSC;
    public String MonedaSAPV;
    public String NombreBanco;
    public String CuentaBancaria;
    public String CodigoPortafolio;
    public Long DiaDescuento;
    public Date FechaSuscripcion;
    public Long IdMandatoPACUI;
}
