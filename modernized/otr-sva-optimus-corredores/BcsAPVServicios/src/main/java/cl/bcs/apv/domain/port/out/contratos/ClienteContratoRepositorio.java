package cl.bcs.apv.domain.port.out.contratos;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

/** Queries of the client / contract consultation (legacy ConsultarClienteContratoSAAPV SQL). */
public interface ClienteContratoRepositorio {

    /** Row of the grid query. */
    record ClienteFila(Long IdPersona, String RutCliente, String NombrePersona, Long IdPortafolio, String Producto) {
    }

    /** Row of the modal query. */
    record ModalFila(Long IdPersona, String RutCliente, String NombrePersona, Long IdPortafolio, String Producto, Long IdContratoSAAPV,
            String FolioContratoSAAPV) {
    }

    /** Row of the sub-grid query: mutable, as the legacy POJO (the service rewrites some columns); public fields = column labels. */
    class ContratoFila {
        public Long IdContratoSAAPV;
        public String FolioContratoSAAPV;
        public String Folio;
        public String NombrePlan;
        public String RutEmpleador;
        public String NombreEmpleador;
        public String NombreEncargado;
        public Date FechaSuscripcion;
        public Date FechaCreacion;
        public Date FechaAprobacion;
        public Date FechaNotificacion;
        public Date FechaRecepcion;
        public String OrigenAhorro;
        public BigDecimal MontoPesos;
        public BigDecimal MontoUF;
        public BigDecimal PorcentajeRegimenImponible;
        public String Pago;
        public String PeriodoInicio;
        public String PeriodoTermino;
        public String RegimenTributario;
        public String FormaDePago;
        public String NombreMedioRecaudacion;
        public Long DiaDescuento;
        public String Institucion;
        public BigDecimal ComisionTransferencia;
        public String CodigoTipoSolicitud;
        public String RutEntidadOrigen;
        public String NombreEntidadOrigen;
        public String TipoDeAhorro;
        public String FondoDeOrigen;
        public String TipoTraspaso;
        public BigDecimal MontoTraspaso;
        public BigDecimal UFTraspaso;
        public BigDecimal PorcentajeTraspaso;
        public String EstadoNotificacion;
        public String IdEstadoEvtAut;
        public String NombreEstadoAPV;
        public String EstadoNuevo;
        public String EstadoPAC;
    }

    List<ClienteFila> clientes(Long idEmpresa, int idPlan, int filtroIdPersona, List<Long> idPersona);

    List<ContratoFila> contratos(Long idEmpresa, int idPlan, int filtroIdPersona, List<Long> idPersona, int filtroIdPortafolio, List<Long> idPortafolio);

    List<ModalFila> modal(Long idEmpresa, int idPlan, int filtroIdPersona, List<Long> idPersona, int filtroIdPortafolio, List<Long> idPortafolio);
}
