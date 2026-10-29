package cl.bcs.tesoreria.javacall.distribucion;

import java.math.BigDecimal;
import java.sql.Date;

/**
 * Row types read by the ProcesarDistribucion queries (legacy nested classes of {@code ProcesarDistribucion}). The names of the components / fields are the
 * column labels of the SQL files {@code sql/ProcesarDistribucion_*.sql}. Public because the JDBC row mapper (another package) instantiates them.
 */
public final class Filas {

    private Filas() {
    }

    /** Own portfolio of the broker (ConsultarCorredor). */
    public record DatosCorredor(Long IdPersona, Long IdCliente, Long IdPortafolio) {
    }

    /** Distribution bank account (ConsultarCuentaDistribucion). */
    public record CtaDistribucion(Long DestinoCtaBco, Long IdPersonaBanco) {
    }

    public record Caja(Long IdCaja, String Codigo, String Nombre) {
    }

    public record DiaDescuento(Long DiaDescuento) {
    }

    public record PortafolioCliente(Long IdPortafolio, Long IdPersona) {
    }

    /** Employee of the user (ConsultarEmpleado). */
    public record Empleado(Long IdEmpleado) {
    }

    /** A detail of a CSC fund contract; Monto is converted in place when the contract is in UF. */
    public static class ContratoSuscripcionCuotaDetalle {
        public Long IdCSCDetalle;
        public String IdFolioCSC;
        public Long IdPortafolio;
        public String Serie;
        public BigDecimal Monto;
        public Double Porcentaje;
        public String Regimen;
        public String EstadoContrato;
        public String EstadoPortafolio;
        public String Moneda;
        public String IdMonedaInstrumento;
        public String Canal;
    }

    /** A line of the investment distribution of a SAAPV contract; MontoPesos is adjusted in place to absorb rounding. */
    public static class DistribucionInversion {
        public String FolioContratoSAAPV;
        public String Nemotecnico;
        public BigDecimal MontoPesos;
        public String Codigo;
        public String EstadoContrato;
        public String EstadoPortafolio;
        public String IdTipoInstrumento;
        public long IdPortafolio;
        public long IdInstrumento;
        public String IdEmisor;
        public String IdMonedaInstrumento;
        public String Canal;
        public BigDecimal MontoDistribucion = new BigDecimal(0);
    }

    /** Row of PAC.LogMandatoPAC (legacy {@code ValidarMandatosPAC.LogValidarMandatosPAC}); also the parameter bean of the log insert / update batches. */
    public static class LogValidarMandatosPAC {
        public Long IdMandatoPAC;
        public String IdentServicio;
        public Date FechaProceso;
        public Date FechaSistema;
        public String EstadoProcesamiento;
        public String PreRendicion;
        public String Usuario;
        public String Glosa;
    }
}
