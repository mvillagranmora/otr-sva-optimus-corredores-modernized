package cl.bcs.tesoreria.infrastructure.adapter;

import cl.bcs.tesoreria.domain.model.*;
import cl.bcs.tesoreria.domain.port.out.TesoreriaRepository;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class TesoreriaJdbcRepository implements TesoreriaRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public TesoreriaJdbcRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // -----------------------------------------------------------------------
    // IngresoEgreso — HANA global temp table "GBL.IngresoEgresoGenerarComprobante_Proceso"
    // -----------------------------------------------------------------------

    @Override
    public void persistirIngresoEgreso(MovimientoRequest movimiento) {
        // HANA only — writes to global temp table BSCL.Tesoreria.BD::GBL.IngresoEgresoGenerarComprobante_Proceso.
        // Source: CrearIngresoEgreso_InsertIngresoEgresoUsuario.sql (GBL temp table).
        throw new UnsupportedOperationException(
                "persistirIngresoEgreso: requires HANA global temp table BSCL.Tesoreria.BD::GBL.IngresoEgresoGenerarComprobante_Proceso");
    }

    // -----------------------------------------------------------------------
    // LiquidarComprobante — temp table and SP-based paths
    // -----------------------------------------------------------------------

    @Override
    public ComprobanteInfo consultarComprobante(Long idComprobante) {
        // HANA only — reads from BSCL.Tesoreria.BD::TES.Comprobante with multi-table JOIN.
        // Source: LiquidarComprobante_SelectCuentaBancoAActualizar.sql uses GBL.LiquidarComprobante_TablaComprobante.
        throw new UnsupportedOperationException(
                "consultarComprobante: requires HANA global temp table BSCL.Tesoreria.BD::GBL.LiquidarComprobante_TablaComprobante");
    }

    @Override
    public List<Long> consultarFormasDePago(Long idComprobante, Long idEmpresa) {
        // HANA only — reads from BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago.
        // Source: LiquidarComprobante_SelectFormasDePago.sql (HANA quoted identifiers).
        throw new UnsupportedOperationException(
                "consultarFormasDePago: requires HANA quoted-identifier table BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago");
    }

    @Override
    public void persistirLiquidacion(ComprobanteInfo info) {
        // HANA only — UPDATE via LiquidarComprobante_UpdateComprobanteFormaDePago.sql.
        // Source: ActualizarSaldosCuentaBanco_UpdateSaldoContable.sql (multi-column batch).
        throw new UnsupportedOperationException(
                "persistirLiquidacion: requires HANA quoted-identifier UPDATE on TES.ComprobanteFormaDePago");
    }

    // -----------------------------------------------------------------------
    // GirarComprobante — SP CALL + complex INSERT chains
    // -----------------------------------------------------------------------

    @Override
    public List<String> verificarRequisitosGirarIngreso(Long idComprobante) {
        // HANA only — multiple SELECT + CALL on BSCL.Tesoreria.BD.Servicios::* SPs.
        // Source: RequisitosGirarComprobanteIngreso_SelectFoliosComprobantes.sql.
        throw new UnsupportedOperationException(
                "verificarRequisitosGirarIngreso: requires HANA stored procedures BSCL.Tesoreria.BD.Servicios::*");
    }

    @Override
    public List<String> verificarRequisitosGirarEgreso(Long idComprobante) {
        // HANA only — multiple SELECT + CALL on BSCL.Tesoreria.BD.Servicios::* SPs.
        // Source: RequisitosGirarComprobanteEgreso_SelectComprobantePAREGR.sql.
        throw new UnsupportedOperationException(
                "verificarRequisitosGirarEgreso: requires HANA stored procedures BSCL.Tesoreria.BD.Servicios::*");
    }

    @Override
    public Long crearComprobante(GirarComprobanteIngresoRequest req) {
        // HANA only — CALL to BSCL.Tesoreria.BD.Servicios::CrearComprobante, reads output via GBL temp table.
        // Source: CrearComprobante_SelectResultadoProceso.sql reads GBL.CrearComprobante_Comprobante.
        throw new UnsupportedOperationException(
                "crearComprobante: requires HANA SP CALL BSCL.Tesoreria.BD.Servicios::CrearComprobante");
    }

    @Override
    public void persistirGirarComprobanteIngreso(GirarComprobanteIngresoRequest req) {
        // HANA only — multi-step INSERT chain via GirarComprobanteIngreso_InsertCrearComprobantesFormaDePago.sql.
        throw new UnsupportedOperationException(
                "persistirGirarComprobanteIngreso: requires HANA multi-step INSERT into TES.ComprobanteFormaDePago");
    }

    @Override
    public void persistirGirarComprobantesEgreso(GirarComprobantesEgresoRequest req) {
        // HANA only — batch INSERT chain referencing GBL.ComprobanteEgresoDetalle global temp table.
        throw new UnsupportedOperationException(
                "persistirGirarComprobantesEgreso: requires HANA global temp table BSCL.Tesoreria.BD::GBL.ComprobanteEgresoDetalle");
    }

    // -----------------------------------------------------------------------
    // ConfirmarRechazarAnular — uses #RechazarAnularMovimientosTesoreria temp table
    // -----------------------------------------------------------------------

    @Override
    public List<Long> consultarMovimientosRechazados(List<Long> ids) {
        // HANA only — SELECT from #RechazarAnularMovimientosTesoreria session-local temp table.
        // Source: ConfirmarRechazarAnularMovimientos_SelectMovimientosRechazados.sql.
        throw new UnsupportedOperationException(
                "consultarMovimientosRechazados: requires HANA session-local temp table #RechazarAnularMovimientosTesoreria");
    }

    @Override
    public void rechazarAnularMovimientosIngresoEgreso(List<Long> ids) {
        // HANA only — writes to #RechazarAnularMovimientosTesoreria session-local temp table.
        throw new UnsupportedOperationException(
                "rechazarAnularMovimientosIngresoEgreso: requires HANA session-local temp table #RechazarAnularMovimientosTesoreria");
    }

    // -----------------------------------------------------------------------
    // MandatoPAC — 3 methods: 2 wired, 1 HANA stub
    // -----------------------------------------------------------------------

    @Override
    public Long insertarMandatoPAC(InIngresoPAC ingreso) {
        // HANA only — INSERT requires HANA SEQUENCE for IdMandatoPAC generation.
        // Source: IngresoPAC_InsertMandato.sql (18 columns; IdMandatoPAC from sequence).
        throw new UnsupportedOperationException(
                "insertarMandatoPAC: requires HANA SEQUENCE for IdMandatoPAC (IngresoPAC_InsertMandato.sql)");
    }

    @Override
    public List<Long> consultarContratosCSCRelacionados(Long idMandato) {
        // Source: MandatoPAC_Revocacion_SelectContratos.sql — returns IdFolioCSC for related CSC contracts.
        // LEFT JOIN; filter IS NOT NULL to exclude mandatos without CSC contracts.
        String sql = """
                SELECT CSC."IdFolioCSC"
                FROM "BSCL.Tesoreria.BD::PAC.MandatoPAC" MPAC
                LEFT JOIN "BSCL.FondoMutuo.BD::CSC.ContratoSuscripcionCuotas" CSC
                    ON MPAC."FolioCSC" = CSC."FolioCSC"
                WHERE MPAC."IdMandatoPAC" = :IdMandatoPAC
                  AND CSC."IdFolioCSC" IS NOT NULL
                """;
        return jdbc.queryForList(sql, new MapSqlParameterSource("IdMandatoPAC", idMandato), Long.class);
    }

    @Override
    public void actualizarEstadoMandato(Long idMandato, String estado) {
        // Source: Mandato_Revocacion_UpdateMandatoPAC.sql
        String sql = """
                UPDATE "BSCL.Tesoreria.BD::PAC.MandatoPAC"
                SET "Estado" = :estado
                WHERE "IdMandatoPAC" = :idMandato
                """;
        jdbc.update(sql, new MapSqlParameterSource()
                .addValue("estado", estado)
                .addValue("idMandato", idMandato));
    }

    // -----------------------------------------------------------------------
    // Orchestrator — HANA VIEW + dynamic dispatch
    // -----------------------------------------------------------------------

    @Override
    public List<ConfiguracionServicioTesoreria> consultarConfiguracionServicios(List<String> nombres) {
        // HANA only — reads from VIEW BSCL.Tesoreria.BD.Vistas::ConsultarConfiguracionTesoreria.
        // Source: ConsultarEmpresa_ObtenerConfiguracionTesoreria.sql (HANA VIEW, not a base table).
        throw new UnsupportedOperationException(
                "consultarConfiguracionServicios: requires HANA VIEW BSCL.Tesoreria.BD.Vistas::ConsultarConfiguracionTesoreria");
    }

    @Override
    public List<ResultadoServicioTesoreria> ejecutarServicioTesoreria(String beanName, ContextoServicioTesoreria ctx) {
        // HANA only — dynamic dispatch to BSCL.Tesoreria.BD.Servicios::* stored procedures by beanName.
        // Cannot be migrated to static JDBC calls without enumerating all service variants.
        throw new UnsupportedOperationException(
                "ejecutarServicioTesoreria: requires HANA SP dynamic dispatch on BSCL.Tesoreria.BD.Servicios::*");
    }

    @Override
    public void persistirResultado(SalidaOrquestadorTesoreria resultado) {
        // HANA only — INSERT into BSCL.Tesoreria.BD::TES.ResultadoOrquestador (HANA quoted identifiers).
        throw new UnsupportedOperationException(
                "persistirResultado: requires HANA quoted-identifier INSERT on BSCL.Tesoreria.BD::TES.ResultadoOrquestador");
    }
}
