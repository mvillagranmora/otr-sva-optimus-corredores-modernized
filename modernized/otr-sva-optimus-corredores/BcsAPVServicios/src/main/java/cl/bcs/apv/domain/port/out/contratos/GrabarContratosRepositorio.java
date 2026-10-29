package cl.bcs.apv.domain.port.out.contratos;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * Persistence port of the use case GrabarContratos (legacy {@code GrabarContratoSAAPV}): one method per SQL statement the legacy ran, with the legacy
 * parameter names as record components (the statements bind them by name) and the legacy result columns as record components. Every method throws the
 * {@link SQLException} the legacy {@code cl.bcs.utils.SQL} threw. The batch methods answer the update counts of the batch ({@code int[]}), which the
 * legacy inspects ({@code res[0] == 1}).
 */
public interface GrabarContratosRepositorio {

    // ------------------------------------------------------------------------------------------------ rows

    record EstadoCliente(Long IdCliente, String IdEstado, String DocumentoIdentidad) {
    }

    record ConsultaDireccion(Long IdDireccionFisica) {
    }

    record CambiarCargoPersona(Long IdPersona, Long IdTipoCargoPersona) {
    }

    record TipoIndicador(Long IdTipoIndicador, String CodigoTipoIndicador) {
    }

    /** Row of the session temporary table {@code #RegistroAtributo}; also the element of the {@code RegistroAtributo} input of the procedure. */
    record IndicadorEncargadoRRHH(Long IdIndicador, Long IdTipoIndicador, String ValorIndicador, Date FechaVencimiento) {
    }

    record ConsultaRelacionPersona(Long IdRelacionPersona) {
    }

    record ContratoConsultado(Long IdContratoSAAPV, String IdEstado) {
    }

    /** Row of the session temporary table {@code #PortafoliosIngresados}. */
    record PortafolioACerrar(Long IdPortafolio, String Producto) {
    }

    /** Existing contract with the same conditions; the legacy POJO defaults ("" and 0) apply to the columns the query does not give. */
    record ContratoSAAPVID(Long IdContratoSAAPV, String Pago, String FormaDePago, String PeriodoInicio, String PeriodoTermino, Long IdEntidadRecaudadora,
            Long IdEmpleador, Long IdMedioRecaudacionFormaPago) {

        /** Legacy {@code new ContratoSAAPVID()} with only the id set (the input of the "close" statements). */
        public static ContratoSAAPVID soloId(Long idContratoSAAPV) {
            return new ContratoSAAPVID(idContratoSAAPV, null, "", "", "", 0L, 0L, 0L);
        }
    }

    record PortafoliosSAAPV(Long IdContratoSAAPV, Long IdPortafolio, String Regimen) {
    }

    record MedioRecaudacion(Long IdMedioRecaudacion, String CodigoMedioRecaudacion, String TipoMedioRecaudacion) {

        /** Legacy {@code new MedioRecaudacion()}. */
        public static MedioRecaudacion vacio() {
            return new MedioRecaudacion(0L, "", "");
        }
    }

    record DistribucionNueva(Long IdContratoSAAPV, String FolioContratoSAAPV, Long IdInstrumento, String Nemotecnico, double MontoPesos, double Porcentaje,
            String IdEstado) {
    }

    record PortafolioContratoNuevo(String OrigenAhorro, Long IdPortafolio, Long IdContratoSAAPV, double MontoPesos, double MontoUF,
            double PorcentajeRegimenImponible, String Pago, String PeriodoInicio, String PeriodoTermino, String IdEstado) {
    }

    record TraspasoNuevo(Long IdTraspaso, Long IdContratoSAAPV, Long IdEmpresa, String TipoMovimiento, String RazonRetiro, String FechaIngreso,
            String FechaEgreso, String FechaModificacion, String CodigoTipoTraspaso, String Descripcion, Long IdCliente, Long IdPersona,
            String CodigoTipoAhorro, String NombreEntidad, String DocumentoEntidad, String IdEstado, String Canal, String CierreSolicitud,
            String FechaSuscripcion) {
    }

    record TraspasoEstado(String IdEstado, Long IdTraspaso) {
    }

    record DetalleOrigenNuevo(Long IdDetalleOrigenTraspaso, Long IdTraspaso, String CodigoTipoAhorro, Long IdEntidadRecaudadora, String NombreEntidad,
            String DocumentoEntidad, String Fondo, double MontoTraspaso, double UFTraspaso, double PorcentajeTraspaso, String IdEstado, Long IdInstrumento,
            String CodigoRegimenTributario, Long IdPortafolio) {
    }

    // ------------------------------------------------------------------------------------------------ statements

    /** {@code GrabarContratoSAAPV_SelectEstadoCliente}: state of the clients whose default identity document is in the list. */
    List<EstadoCliente> estadosCliente(List<String> documentosIdentidad) throws SQLException;

    /** {@code ConsultarDireccionFisica}. */
    List<ConsultaDireccion> direccionesFisicas(Long idPersona) throws SQLException;

    /** {@code CreaDireccionPropositoAPV}: copies each address with the purpose PREV. */
    int[] copiarDireccionesConPropositoApv(List<ConsultaDireccion> direcciones) throws SQLException;

    /** {@code CrearPersonaActividaCargo_Update}. */
    int[] cambiarCargoPersona(List<CambiarCargoPersona> cargos) throws SQLException;

    /** {@code ConsultaTipoIndicadorPersona}. */
    List<TipoIndicador> tiposIndicador(String idTipoEntidad, String grupoIndicador) throws SQLException;

    /** {@code CREATE LOCAL TEMPORARY TABLE #RegistroAtributo}. */
    void crearTablaRegistroAtributo() throws SQLException;

    int[] insertarRegistroAtributo(List<IndicadorEncargadoRRHH> indicadores) throws SQLException;

    /** {@code DROP TABLE #RegistroAtributo}. */
    void eliminarTablaRegistroAtributo() throws SQLException;

    /** {@code ConsultarRelacionPersona}. */
    List<ConsultaRelacionPersona> relacionesPersona(Long idPersona, Long idEmpleador) throws SQLException;

    /** {@code ContratoSAAPV_Insert}. */
    int[] insertarContrato(ContratoNuevo contrato) throws SQLException;

    /** {@code ContratoSAAPV_Consulta}. */
    List<ContratoConsultado> consultarContrato(Long idContratoSAAPV) throws SQLException;

    /** {@code ContratoSAAPV_Delete}. */
    int[] eliminarContrato(List<ContratoConsultado> contratos) throws SQLException;

    /** {@code EliminarDistribucionInversion}. */
    int[] eliminarDistribucion(List<ContratoConsultado> contratos) throws SQLException;

    /** {@code CREATE LOCAL TEMPORARY TABLE #PortafoliosIngresados}. */
    void crearTablaPortafoliosIngresados() throws SQLException;

    int[] insertarPortafoliosIngresados(List<PortafolioACerrar> portafolios) throws SQLException;

    /** {@code DROP TABLE #PortafoliosIngresados}. */
    void eliminarTablaPortafoliosIngresados() throws SQLException;

    /** {@code ContratoSAAPV_Consulta_Condiciones_Duplicadas} (reads the temporary table of the entered portfolios). */
    List<ContratoSAAPVID> contratosConLasMismasCondiciones(Long idMedioRecaudacionFormaPago, String formaDePago) throws SQLException;

    /** {@code ContratoSAAPV_Consulta_MedioRecaudacionPAC}. */
    List<MedioRecaudacion> mediosRecaudacionPac() throws SQLException;

    /** {@code ContratoSAAPV_Consulta_PortafoliosPorIdContratoSAAPV}. */
    List<PortafoliosSAAPV> portafoliosDeContrato(Long idContratoSAAPV) throws SQLException;

    /** {@code ContratoSAAPV_Condiciones_Duplicadas_Cerrar}. */
    int[] cerrarContratos(List<ContratoSAAPVID> contratos) throws SQLException;

    /** {@code ContratoSAAPV_Distribucion_Condiciones_Duplicadas_Cerrar}. */
    int[] cerrarDistribuciones(List<ContratoSAAPVID> contratos) throws SQLException;

    /** {@code AgregarDistribucionInversion}. */
    int[] insertarDistribuciones(List<DistribucionNueva> distribuciones) throws SQLException;

    /** {@code Crear_PortafolioContratoSAAPV}. */
    int[] insertarPortafoliosContrato(List<PortafolioContratoNuevo> portafolios) throws SQLException;

    /** {@code Traspaso_Insert}. */
    int[] insertarTraspaso(List<TraspasoNuevo> traspasos) throws SQLException;

    /** {@code Traspaso_Update}. */
    int[] modificarEstadoTraspaso(List<TraspasoEstado> traspasos) throws SQLException;

    /** {@code DetalleOrigenTraspaso_Insert}. */
    int[] insertarDetallesOrigen(List<DetalleOrigenNuevo> detalles) throws SQLException;
}
