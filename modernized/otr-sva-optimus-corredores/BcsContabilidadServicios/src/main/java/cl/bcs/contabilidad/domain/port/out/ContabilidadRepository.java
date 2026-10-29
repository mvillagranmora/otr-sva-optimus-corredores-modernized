package cl.bcs.contabilidad.domain.port.out;

import cl.bcs.contabilidad.domain.model.ConfiguracionTipoCentralizacion;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabDatoEventoRef;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabDatoMovimientoRef;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabEventoRef;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabMovimientoRef;
import cl.bcs.contabilidad.domain.service.devengo.CarteraValorizada;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import java.util.List;
import java.util.Map;

/**
 * Repository port for accounting data access.
 *
 * Read methods supply raw data to centralisation services.
 * Write methods (persistir*) are only called when simulaOperacion = "N";
 * in simulation mode the orchestrator must not invoke any write method.
 *
 * Derived from SQL queries used in CentralizaMultiSeleccion and
 * CentralizaGenerico legacy classes.
 */
public interface ContabilidadRepository {

    // ------------------------------------------------------------------
    // Configuration queries (CentralizaMultiSeleccion.logica)
    // ------------------------------------------------------------------

    /** Returns configuration for the given set of TipoCentralizacion ids. */
    List<ConfiguracionTipoCentralizacion> queryConfiguraciones(List<Long> ids);

    /**
     * Returns the subset of given ids that have already been accounted for
     * on the given accounting date.  Used to prevent double-posting.
     * Legacy: SQL_SELECT_CENTRALIZACIONES_VIGENTES
     */
    List<ConfiguracionTipoCentralizacion> queryCentralizacionesVigentes(
            List<Long> ids,
            String fechaContable,
            Long idEmpresa);

    // ------------------------------------------------------------------
    // Movement queries (CentralizaAnticipoPactos.centralizar)
    // ------------------------------------------------------------------

    /** Legacy: CentralizaAnticipoPactos_SelectMovimientos.sql */
    List<TabMovimientoRef> queryMovimientos(
            Long idEmpresa,
            String fechaContable,
            Long idTipoCentralizacion);

    /** Legacy: CentralizaAnticipoPactos_SelectEventosOperacion.sql */
    List<TabEventoRef> queryEventosOperacion(
            Long idEmpresa,
            String fechaContable,
            Long idTipoCentralizacion);

    /** Legacy: CentralizaAnticipoPactos_SelectEventosSubyacente.sql */
    List<TabEventoRef> queryEventosSubyacente(
            Long idEmpresa,
            String fechaContable,
            Long idTipoCentralizacion,
            String fechaAnterior);

    /** Legacy: CentralizaAnticipoPactos_SelectDatosMovimiento.sql */
    List<AnticipoPactosDatoMovimiento> queryDatosMovimientoAnticipo(
            Long idEmpresa,
            String fechaContable);

    /**
     * Runs one named legacy SQL file with the given named parameters and returns its rows as column maps.
     *
     * <p>This is the port's general "run this legacy statement" read: every Java centralization type other than
     * AnticipoPactos reads its movements, events and data rows through it, each with its own legacy file
     * (bare name such as {@code CentralizaTesoreria_SelectMovimientos.sql}, resolved under cl/bcs/contabilidad/sql/).
     * The name is historical: it is not limited to the datos-movimiento queries.
     */
    List<Map<String, Object>> queryDatosMovimientoRows(
            String sqlResourceName,
            MapSqlParameterSource params);

    /**
     * Runs one named legacy DML file (INSERT / UPDATE / DELETE) with the given named parameters; returns the row count.
     * Used where a legacy type wrote a working table while computing (e.g. CentralizaTesoreria's
     * {@code CentralizaTesoreria_Insert_Comprobantes_Asociados_Partidas.sql}).
     */
    int ejecutarSql(String sqlResourceName, MapSqlParameterSource params);

    /**
     * Takes the centralization lock {@code idBloqueo} for one type and date (legacy CentralizaSpotUtilidadPerdida
     * :111-121: temporary table #p_BloqueoCentralizacion + CALL CrearBloqueoCentralizacion).
     */
    void crearBloqueoCentralizacion(String idBloqueo, Long idTipoCentralizacion, String fechaCentralizacion);

    /** Releases that lock (legacy :190-195: CALL EliminarBloqueoCentralizacion + DROP of the temporary table). */
    void eliminarBloqueoCentralizacion(String idBloqueo, Long idTipoCentralizacion, String fechaCentralizacion);

    /**
     * Returns the ClasificacionTipoInstrumento code for the given centralization type.
     * Used by Emprestito/Prestamo services as a pre-query before the datos query.
     * Legacy: SELECT ClasificacionTipoInstrumento FROM ... WHERE IdTipoCentralizacion = :id
     *
     * <p>The four legacy copies (CentralizaAnticipoVencimientoEmprestito/-Prestamo, CentralizaEmprestitoInicio,
     * CentralizaPrestamoInicio _SelectClasificacionTipoInstrumento.sql) are byte-identical. Fails when the type has
     * no classification (legacy {@code clasificacion.get(0)} crashed).
     */
    String queryClasificacionTipoInstrumento(Long idTipoCentralizacion);

    /**
     * Raw rows of the valuation report {@code "BSCL.Custodia.BD::VAL.CarteraValorizadaIFRF"} for one empresa and informe,
     * read with the given legacy-named statement (a {@code CentralizaDevengo*_SelectCartera*.sql} of the Devengo types,
     * bound {@code :p_IdEmpresa} / {@code :p_IdInforme}).
     */
    List<CarteraValorizada> queryCarteraValorizada(String sqlResourceName, Long idEmpresa, String idInforme);

    // ------------------------------------------------------------------
    // Write methods — ONLY called when simulaOperacion = "N"
    // ------------------------------------------------------------------

    /**
     * Persists the tables one centralization type produced: ITZ.Centralizacion (new or the vigente one), Movimiento,
     * MovimientoDato, EventoTipo, Evento and Log. Must NOT be called when simulation mode is active.
     * Legacy: CentralizaMultiSeleccion.java:411-417 handed the tables of the Java types to CentralizaGenerico.logica;
     * here each type hands its own tables once and the same CentralizaGenerico rules apply to that one type.
     *
     * <p><strong>Transaction contract:</strong> implementations must execute all
     * inserts atomically.  The JDBC implementation runs it as a nested transaction (a savepoint inside the
     * JavaCall unit of work), so one failing type is undone alone; callers must not suppress that boundary.
     * A partial failure (e.g. events written, movements not) would leave the
     * accounting ledger in an inconsistent state.
     */
    void persistirCentralizacion(
            Long idTipoCentralizacion,
            String fechaContable,
            Long idEmpresa,
            Long ejercicio,
            String codigoUsuario,
            List<TabMovimientoRef> movimientos,
            List<TabEventoRef> eventos,
            List<TabDatoMovimientoRef> datosMovimiento,
            List<TabDatoEventoRef> datosEvento);

    // ------------------------------------------------------------------
    // Nested data class for CentralizaAnticipoPactos auxiliary query
    // ------------------------------------------------------------------

    /**
     * Row returned by CentralizaAnticipoPactos_SelectDatosMovimiento.sql.
     * Derived from CentralizaAnticipoPactos.MovimientoPasoAnticipoPacto.
     */
    record AnticipoPactosDatoMovimiento(
            Long   idMovimiento,
            String documentoIdentidad,
            String nombreCliente,
            String portafolio,
            String moneda,
            String producto,
            String claseInstrumento,
            String tipoInstrumento,
            String nemo,
            String boveda,
            String idCartera,
            String emisor,
            String tipoReajuste,
            String tipoEmisor,
            String codigoIFRS,
            String tipoCartera,
            String claseEmisor,
            String agente,
            String nombreAgente,
            String sucursalAgente,
            String nombreSucursalAgente,
            String centroCostoAgente,
            String relacion,
            String compraVenta,
            String mesa,
            Long   folioOperacion
    ) {}
}
