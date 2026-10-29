package cl.bcs.apv.domain.port.out.contratos;

import java.math.BigDecimal;
import java.util.List;

/**
 * Persistence port of {@code CambiarContratosAPlanActivo} (legacy {@code CambiarAPlanActivoContratoSAAPV}): the four reads of the contracts in plan 1 and the
 * two writes that move their portfolios to the active-plan matrix. {@code SQLException} surfaces unchanged, as the legacy.
 */
public interface CambioPlanActivoRepositorio {

    /** {@code ..._SelectParametrosContrato.sql}: the VIG contracts of the empresa in the plan, with the data of the client, the employer and the traspaso. */
    List<DatosContratoSAAPV> consultarParametrosContrato(Long idEmpresa, int idPlan, List<Long> idsContrato);

    /** {@code ..._SelectDistribucionInversion.sql}. */
    List<DatosDistribucionInversion> consultarDistribucionInversion(Long idEmpresa, int idPlan, List<Long> idsContrato);

    /** {@code ..._SelectPortafoliosModificados.sql}. */
    List<DatosPortafoliosModificados> consultarPortafoliosModificados(Long idEmpresa, int idPlan, List<Long> idsContrato);

    /** {@code ..._SelectDetalleOrigenTraspaso.sql}. */
    List<DatosDetalleOrigenTraspaso> consultarDetalleOrigenTraspaso(Long idEmpresa, int idPlan, List<Long> idsContrato);

    /** {@code ..._InsertGlobalPortafolios.sql}: one insert per portfolio id (duplicates and nulls included) into the global table that is never cleaned. */
    void registrarPortafoliosCambioPlan(List<Long> idsPortafolio);

    /**
     * {@code ..._UpdatePlanPortafolios.sql} (HANA UPDATE .. FROM .. JOIN, no parameters), executed once per element of the list as the legacy batch did.
     *
     * @return the number of batch results (the legacy {@code resultadoMod.length})
     */
    int modificarPlanPortafolios(List<Long> idsPortafolio);

    record DatosContratoSAAPV(Long IdContratoSAAPV, Long IdEmpresa, String FolioContratoSAAPV, Long IdPlan, String NombrePlan, java.sql.Date FechaSuscripcion,
            java.sql.Date FechaCreacion, java.sql.Date FechaAprobacion, java.sql.Date FechaNotificacion, java.sql.Date FechaRecepcion, String Folio,
            Long IdPersona, Long IdEmpleador, Long IdCliente, String IdCategoriaInversionista, String IdPerfilComercial, String IdPerfilComercialAPV,
            String NombreCliente, String ApellidoPaternoCliente, String ApellidoMaternoCliente, String DocumentoIdentidadCliente,
            java.sql.Date FechaNacimientoCliente, String EstadoCivil, String IdTipoGenero, Long IdPaisResidenciaCliente, Long IdCiudadCliente,
            Long IdComunaCliente, String DireccionCliente, String NumeroDireccionCliente, Long IdTipoCargoPersona, String TelefonoCliente,
            String TelefonoComercialCliente, String TelefonoCelularCliente, String EmailCliente, String RazonSocialEmpleador,
            Long IdMedioRecaudacionTrabajador, Long IdEntidadRecaudadora, String DocumentoIdentidadEmpleador, Long IdPaisEmpleador,
            String DireccionComercialEmpleador, String NumeroDireccionEmpleador, Long IdCiudadEmpleador, Long IdComunaEmpleador, String TelefonoEmpleador,
            String NombreEncargado, String EmailEncargado, String TelefonoEncargado, Long CodigoPrevision, String Pensionado, String Trabajador,
            String FormaDePago, Long IdMedioRecaudacionFormaPago, Long DiaDescuento, String Institucion, BigDecimal ComisionTransferencia,
            String CodigoTipoSolicitud, String TipoMovimiento, String RazonRetiro, String NombreEntidadOrigen, String RutEntidadOrigen,
            Long IdPersonaOrigen, String Descripcion, String TipoDeAhorro, String FondoDeOrigen, String TipoTraspaso, BigDecimal MontoTraspaso,
            BigDecimal UFTraspaso, BigDecimal PorcentajeTraspaso, String IdEstadoEnvioNotificacion, String IdEstadoSolicitud, String IdEstado,
            Long IdCargaMasiva, Long IdEmpleado, Long IdInterno) {
    }

    record DatosDistribucionInversion(Long IdContratoSAAPV, String FolioContratoSAAPV, Long IdInstrumento, String Nemotecnico, BigDecimal MontoPesos,
            BigDecimal Porcentaje, String IdEstado) {
    }

    record DatosPortafoliosModificados(Long IdContratoSAAPV, String CodigoAgrupador, Long IdMatrizAgrupadorAPV, String OrigenAhorro, Long IdPortafolio,
            String IdPropositoPortafolio, String PeriodoInicio, String PeriodoTermino, BigDecimal MontoPesos, BigDecimal MontoUF, String Pago,
            BigDecimal PorcentajeRegimenImponible, String NombreProductoAgrupador, String Codigo) {
    }

    record DatosDetalleOrigenTraspaso(Long IdContratoSAAPV, Long IdDetalleOrigenTraspaso, Long IdTraspaso, String CodigoTipoAhorro,
            Long IdEntidadRecaudadora, String NombreEntidad, String DocumentoEntidad, String Fondo, BigDecimal MontoTraspaso, BigDecimal UFTraspaso,
            BigDecimal PorcentajeTraspaso, String IdEstado, Long IdInstrumento, String CodigoRegimenTributario, Long IdPortafolio, Long IdInterno) {
    }
}
