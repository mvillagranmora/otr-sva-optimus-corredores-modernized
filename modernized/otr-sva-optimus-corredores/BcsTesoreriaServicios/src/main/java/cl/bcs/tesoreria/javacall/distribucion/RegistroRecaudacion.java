package cl.bcs.tesoreria.javacall.distribucion;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * A rendition request as ProcesarDistribucion reports it (legacy {@code Traspaso.RegistroRecaudacion}); serialised as the elements of
 * P_OUT_SOLICITUDES_*, so the field names and defaults are the JSON contract.
 */
public class RegistroRecaudacion {

    public Long IdTraspaso;
    public Date FechaDeposito;
    public String IdMedioDePago;
    public Long IdBanco;
    public Long NumeroCuentaBancaria;
    public Long IdCuentaBanco;
    public String Sucursal;
    public BigDecimal MontoDeposito;
    public BigDecimal SumaTotal;
    public String Observacion;
    public String IdSerieFondo;
    public Long IdCliente;
    public Long IdPortafolio;
    public String EstadoProceso = "";
    public String Codigo;
    public Long IdRecaudacion = 0L;
    public Long IdEmpresa = 0L;
    public Long IdUsuario = 0L;
    public String IdEstado = "";
    public Date FechaIngreso;
    public Date FechaSistema;
    public Date FechaAceptacion;
    public Long IdPersonaEmisor = 0L;
    public String IdConcepto = "";
    public String DescripccionConcepto = "";
    public String FolioCSC = "";
    public String FolioAPV = "";

    public Long IdMandatoPAC = 0L;
    public String EstadoPAC = "";
    public String NombreCompleto = "";
    public String DocumentoIdentidad = "";
    public String EstadoCliente = "";
    public String IdentServicio = "";
    public String EstadoPortafolio = "";
    public String DescripcionSolicitud = "";
    public String EstadoCargo = "";
    public String DescripcionEstadoCargo = "";
    public String EstadoRendicion = "";
    public String EstadoCargoBanco = "";
    public String EstadoProcesoRendicion = "";
    public String EstadoCSC = "";
    public String EstadoLargoCSC = "";
    public String EstadoSAAPV = "";
    public String EstadoLargoSAAPV = "";
    public String Moneda = "";
    public String NombreBanco;
    public String CuentaBancaria;
    public String CodigoPortafolio;
    public Long DiaDescuento;
    public Date FechaSuscripcion;
    public Long IdMandatoPACUI;
    public List<InMovimientoGenerado> MovimientoGenerado = new ArrayList<>();
}
