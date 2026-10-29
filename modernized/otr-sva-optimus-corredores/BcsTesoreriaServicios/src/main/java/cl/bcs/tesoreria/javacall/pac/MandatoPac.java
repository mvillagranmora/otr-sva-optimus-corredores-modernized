package cl.bcs.tesoreria.javacall.pac;

import java.util.Date;

/**
 * PAC.MandatoPAC row as read by the folio / identServicio queries and written by the insert / update statements (named parameters are read from these
 * public fields, see SqlNombrado). Union of the legacy {@code IngresarMandatoPAC.MandatoPAC} and {@code ModificarPAC.MandatoPAC}.
 */
public class MandatoPac {
    public Long IdMandatoPAC;
    public Long IdCliente;
    public Long IdPortafolio;
    public String FolioCSC;
    public String CuentaCorriente;
    public String FolioSAAPV;
    public String Receptor;
    public Date Fecha;
    public Long NumeroMandato;
    public String Firma;
    public String IdentServicio;
    public String Estado;
    public String EstadoCargo;
    public Long IdEmpresa;
    public String Canal;
    public Long IdMandatoPACBase;
    public Long IdCuentaBancaria;
    public String EstadoPorAutorizar;
    public Long DiaDescuentoPorAutorizar;
    public String EstadoContratoPorAutorizar;
}
