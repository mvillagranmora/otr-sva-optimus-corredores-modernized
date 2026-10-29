package cl.bcs.apv.domain.port.out.contratos;

import java.util.List;

/**
 * Persistence port of the use case {@code ModificarContratos} (legacy {@code ModificarContratoSAAPV}): one method per legacy SQL statement, in the legacy
 * vocabulary. Every method surfaces {@code java.sql.SQLException} as the legacy did (it is thrown unchecked-through, not wrapped).
 * The {@code boolean} answers reproduce the legacy test {@code result != null && result.length > 0 && result[0] == 1} of the batch result.
 */
public interface ModificarContratosRepositorio {

    /** {@code ContratoSAAPV_Consulta.sql}: state of the contract before the update. */
    List<ContratoEstado> consultarContrato(Long idContratoSAAPV);

    /** {@code ContratoSAAPV_Update.sql} (a batch of one row). */
    void modificarContrato(ContratoModificado contrato);

    /** Inline legacy query: the {@code IdTraspaso} rows of the contract (the legacy kept the LAST one, 0 when there is none). */
    List<Long> consultarIdsTraspaso(Long idContratoSAAPV);

    /** {@code Eliminar_DetalleOrigenTraspaso.sql} (batch of one). */
    void eliminarDetalleOrigenTraspaso(Long idContratoSAAPV);

    /** {@code Eliminar_Traspaso.sql} (batch of one). */
    void eliminarTraspaso(Long idContratoSAAPV);

    /** {@code EliminarDistribucionInversion.sql} (batch of one). */
    void eliminarDistribucion(Long idContratoSAAPV);

    /** {@code Traspaso_Insert.sql} (batch of one): true when the first batch result is 1. */
    boolean crearTraspaso(TraspasoNuevo traspaso);

    /** {@code DetalleOrigenTraspaso_Insert.sql}: true when the first batch result is 1. */
    boolean crearDetallesOrigenTraspaso(List<DetalleOrigenNuevo> detalles);

    /** Inline legacy UPDATE of {@code TRA.DetalleTraspaso.IdTraspaso} (batch of one). */
    boolean modificarIdTraspasoDetalle(Long idTraspaso, Long idTraspasoNuevo);

    /** Inline legacy UPDATE of {@code SCS.PortafolioContratoSAAPV.IdContratoSAAPV} (batch of one). */
    boolean modificarContratoDePortafolios(Long idContratoSAAPV, Long idContratoSAAPVNuevo);

    /** {@code Traspaso_Update.sql} (batch of one). */
    boolean modificarEstadoTraspaso(Long idTraspaso, String idEstado);

    /** {@code AgregarDistribucionInversion.sql}: true when the first batch result is 1. */
    boolean crearDistribuciones(List<DistribucionNueva> distribuciones);

    /** {@code Eliminar_PortafolioContratoSAAPV.sql} (batch of one). */
    void eliminarPortafoliosDelContrato(Long idContratoSAAPV);

    /** {@code Crear_PortafolioContratoSAAPV.sql} (batch of one). */
    boolean crearPortafolioContrato(PortafolioContratoNuevo portafolio);

    /** {@code ConsultarTraspasosEgresos.sql}: the {@code IdDetalleOrigenTraspaso} of every detail row of the traspaso (null for a traspaso without details). */
    List<Long> consultarDetallesTraspasoEgreso(Long idTraspaso);

    /** {@code Eliminar_DetalleOrigenTraspasoEgreso.sql}: one execution per id. */
    void eliminarDetallesOrigenEgreso(List<Long> idsDetalleOrigenTraspaso);

    /** {@code ModificarTraspasosEgresos.sql} (batch of one). */
    boolean modificarTraspasoEgreso(TraspasoEgresoModificado traspaso);

    /** Result of {@code ContratoSAAPV_Consulta.sql}. */
    record ContratoEstado(Long idContratoSAAPV, String idEstado) {
    }

    /** Parameters of {@code Traspaso_Insert.sql} (legacy parameter names). */
    record TraspasoNuevo(Long IdTraspaso, Long IdContratoSAAPV, Long IdEmpresa, String TipoMovimiento, String RazonRetiro, String FechaIngreso,
            String FechaEgreso, String FechaModificacion, String CodigoTipoTraspaso, String Descripcion, Long IdCliente, Long IdPersona,
            String CodigoTipoAhorro, String NombreEntidad, String DocumentoEntidad, String IdEstado, String Canal, String CierreSolicitud,
            String FechaSuscripcion) {
    }

    /** Parameters of {@code DetalleOrigenTraspaso_Insert.sql}; the amounts are {@code double} like the legacy fields (no scale rule). */
    record DetalleOrigenNuevo(Long IdDetalleOrigenTraspaso, Long IdTraspaso, String CodigoTipoAhorro, Long IdEntidadRecaudadora, String NombreEntidad,
            String DocumentoEntidad, String Fondo, double MontoTraspaso, double UFTraspaso, double PorcentajeTraspaso, String IdEstado, Long IdInstrumento,
            Long IdPortafolio, String CodigoRegimenTributario) {
    }

    /** Parameters of {@code AgregarDistribucionInversion.sql}. */
    record DistribucionNueva(Long IdContratoSAAPV, String FolioContratoSAAPV, Long IdInstrumento, String Nemotecnico, double MontoPesos, double Porcentaje,
            String IdEstado) {
    }

    /** Parameters of {@code Crear_PortafolioContratoSAAPV.sql}. */
    record PortafolioContratoNuevo(String OrigenAhorro, Long IdPortafolio, Long IdContratoSAAPV, double MontoPesos, double MontoUF,
            double PorcentajeRegimenImponible, String Pago, String PeriodoInicio, String PeriodoTermino, String IdEstado) {
    }

    /** Parameters of {@code ModificarTraspasosEgresos.sql}. */
    record TraspasoEgresoModificado(Long IdTraspaso, String FechaModificacion, String NombreEntidad, String DocumentoEntidad, String TipoTraspaso) {
    }

    /** Parameters of {@code ContratoSAAPV_Update.sql} (legacy parameter names; amounts are {@code double}). */
    record ContratoModificado(
        Long IdContratoSAAPV,
        Long IdEmpresa,
        String FolioContratoSAAPV,
        Long IdPlan,
        String NombrePlan,
        String FechaSuscripcion,
        String FechaCreacion,
        String FechaAprobacion,
        String FechaNotificacion,
        String FechaRecepcion,
        String Folio,
        Long IdPersona,
        Long IdEmpleador,
        Long IdCliente,
        String NombreCliente,
        String ApellidoPaternoCliente,
        String ApellidoMaternoCliente,
        String DocumentoIdentidadCliente,
        String FechaNacimientoCliente,
        String EstadoCivil,
        String IdTipoGenero,
        Long IdPaisResidenciaCliente,
        Long IdCiudadCliente,
        Long IdComunaCliente,
        String DireccionCliente,
        Long IdTipoCargoPersona,
        String TelefonoCliente,
        String TelefonoComercialCliente,
        String TelefonoCelularCliente,
        String EmailCliente,
        String RazonSocialEmpleador,
        Long IdMedioRecaudacionTrabajador,
        Long IdEntidadRecaudadora,
        String DocumentoIdentidadEmpleador,
        String DireccionComercialEmpleador,
        Long IdComunaEmpleador,
        String TelefonoEmpleador,
        String NombreEncargado,
        String EmailEncargado,
        String TelefonoEncargado,
        Long CodigoPrevision,
        String Pensionado,
        String Trabajador,
        String FormaDePago,
        Long IdMedioRecaudacionFormaPago,
        Long DiaDescuento,
        String Institucion,
        double ComisionTransferencia,
        String CodigoTipoSolicitud,
        String NombreEntidadOrigen,
        String RutEntidadOrigen,
        String TipoDeAhorro,
        String FondoDeOrigen,
        String TipoTraspaso,
        double MontoTraspaso,
        double UFTraspaso,
        double PorcentajeTraspaso,
        String IdEstadoEnvioNotificacion,
        String IdEstadoSolicitud,
        String IdEstado,
        String IdCategoriaInversionista,
        String IdPerfilComercial,
        Long IdCargaMasiva,
        String Canal) {
    }
}
