package cl.bcs.apv.domain.port.out.traspasos;

import java.util.Date;
import java.util.List;

/**
 * Persistence port of {@code GrabarContratosTraspaso} (legacy {@code GrabarContratoTraspasoSAAPV}). The row classes are the legacy ones (public fields named as the
 * SQL parameters: the JDBC adapter reads the batch parameters from them, so a statement that binds a name the class does not have fails exactly as the legacy
 * {@code SQL.batch} did). Every method that the legacy answered with the {@code int[]} of a batch does the same.
 */
public interface GrabarContratosTraspasoRepositorio {

    /** Physical addresses (ids) of a person. */
    List<Long> direccionesFisicas(Long idPersona);

    /** Copies the addresses with the APV purpose; one batch element per address (the legacy ran it even when the list is empty). */
    void copiarDireccionesConPropositoApv(List<Long> idsDireccionFisica);

    /** Updates the position of a person (one batch element). */
    int[] cambiarCargoPersona(Long idPersona, Long idTipoCargoPersona);

    List<TipoIndicador> tiposIndicador(String idTipoEntidad, String grupoIndicador);

    /** Local temporary work table of the RRHH indicators (DDL as the legacy wrote it). */
    void crearTablaRegistroAtributo();

    void insertarRegistroAtributo(List<IndicadorEncargadoRRHH> indicadores);

    void eliminarTablaRegistroAtributo();

    /** Ids of the relation person/employer (empty when there is none). */
    List<Long> relacionesPersona(Long idPersona, Long idEmpleador);

    /** Legacy: the INSERT binds {@code :Canal} but {@link ContratoSAAPV} has no such field, so it always fails with SQLException. */
    int[] insertarContrato(ContratoSAAPV contrato);

    /** Legacy: the INSERT binds {@code :FechaSuscripcion} / {@code :CierreSolicitud} that {@link Traspaso} does not have: always SQLException. */
    int[] insertarTraspaso(Traspaso traspaso);

    int[] insertarDetalleOrigenTraspaso(List<DetalleOrigenTraspaso> detalles);

    int[] insertarDistribucionInversion(List<DistribucionInversion> distribucion);

    class TipoIndicador {
        public Long IdTipoIndicador;
        public String CodigoTipoIndicador;
    }

    class IndicadorEncargadoRRHH {
        public Long IdIndicador;
        public Long IdTipoIndicador;
        public String ValorIndicador;
        public Date FechaVencimiento;
    }

    class ContratoSAAPV {
        public Long IdContratoSAAPV;
        public Long IdEmpresa;
        public String FolioContratoSAAPV;
        public Long IdPlan;
        public String NombrePlan;
        public String FechaSuscripcion;
        public String FechaCreacion;
        public String FechaAprobacion;
        public String FechaNotificacion;
        public String FechaRecepcion;
        public String Folio;
        public String CodigoTipoSolicitud;
        public Long IdPersona;
        public Long IdEmpleador;
        public Long IdCliente;
        public String NombreCliente;
        public String ApellidoPaternoCliente;
        public String ApellidoMaternoCliente;
        public String DocumentoIdentidadCliente;
        public String FechaNacimientoCliente;
        public String EstadoCivil;
        public String IdTipoGenero;
        public Long IdPaisResidenciaCliente;
        public Long IdCiudadCliente;
        public Long IdComunaCliente;
        public String DireccionCliente;
        public Long IdTipoCargoPersona;
        public String TelefonoCliente;
        public String TelefonoComercialCliente;
        public String TelefonoCelularCliente;
        public String EmailCliente;
        public String RazonSocialEmpleador;
        public Long IdMedioRecaudacionTrabajador;
        public Long IdEntidadRecaudadora;
        public String DocumentoIdentidadEmpleador;
        public String DireccionComercialEmpleador;
        public Long IdComunaEmpleador;
        public String TelefonoEmpleador;
        public String NombreEncargado;
        public String EmailEncargado;
        public String TelefonoEncargado;
        public Long CodigoPrevision;
        public String Pensionado;
        public String Trabajador;
        public String FormaDePago;
        public Long IdMedioRecaudacionFormaPago;
        public Long DiaDescuento;
        public String Institucion;
        public double ComisionTransferencia;
        public String NombreEntidadOrigen;
        public String RutEntidadOrigen;
        public String TipoDeAhorro;
        public String FondoDeOrigen;
        public String TipoTraspaso;
        public double MontoTraspaso;
        public double UFTraspaso;
        public double PorcentajeTraspaso;
        public String IdEstadoEnvioNotificacion;
        public String IdEstadoSolicitud;
        public String IdEstado;
        public String IdCategoriaInversionista;
        public String IdPerfilComercial;
        public Long IdCargaMasiva;
    }

    class Traspaso {
        public Long IdTraspaso;
        public Long IdContratoSAAPV;
        public Long IdEmpresa;
        public String TipoMovimiento;
        public String RazonRetiro;
        public String FechaIngreso;
        public String FechaEgreso;
        public String FechaModificacion;
        public String CodigoTipoTraspaso;
        public String Descripcion;
        public Long IdCliente;
        public Long IdPersona;
        public String CodigoTipoAhorro;
        public String NombreEntidad;
        public String DocumentoEntidad;
        public String IdEstado;
        public String Canal;
    }

    class DetalleOrigenTraspaso {
        public Long IdDetalleOrigenTraspaso;
        public Long IdTraspaso;
        public String CodigoTipoAhorro;
        public String NombreEntidadOrigen;
        public String DocumentoEntidadOrigen;
        public String FondoOrigen;
        public double MontoTotalTraspaso;
        public double UFTotalTraspaso;
        public double PorcentajeTotalTraspaso;
        public String IdEstado;
    }

    class DistribucionInversion {
        public Long IdContratoSAAPV;
        public String FolioContratoSAAPV;
        public Long IdInstrumento;
        public String Nemotecnico;
        public double MontoPesos;
        public double Porcentaje;
        public String IdEstado;
    }
}
