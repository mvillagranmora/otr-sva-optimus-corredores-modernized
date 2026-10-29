package cl.bcs.tesoreria.javacall.pac;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Request of IngresarMandatoPAC / ModificarPAC / IngresarMandatoPACManejador (legacy {@code EntradaIngresoMandatoPAC} and {@code EntradaPAC}); the field
 * names are the JSON contract. {@code IdMandatoPACBase} and {@code EstadoMandato} are read by ModificarPAC only.
 */
public class EntradaPac {

    public Long p_IdEmpresa;
    public Long p_IdUsuario;
    public String p_IdModulo;
    public String p_SimularOperacion;
    public String p_UsaReglasNegocio;
    public List<InIngresoPAC> p_IngresoPAC = new ArrayList<>();

    public static class InIngresoPAC {
        public Long IdCliente = 0L;
        public Long IdPortafolio;
        public String FolioCSC;
        public String CuentaCorriente;
        public String FolioSAAPV;
        public Long FolioPACMandatoAgrupador;
        public String Receptor;
        public Date Fecha;
        public Long NumeroMandato;
        public String Firma;
        public String IdentServicio;
        public String Estado;
        public String EstadoCargo;
        public Long IdEmpresa;
        public Long IdCuentaBancaria;
        public String Canal;
        public String TipoPago;
        public String DocumentoIdentidad;
        public Long IdMandatoPACBase;
        public Long DiaDescuento;
        public String EstadoContrato;
        public String EstadoMandato;
    }
}
