package cl.bcs.contabilidad.infrastructure.persistence;

import cl.bcs.contabilidad.domain.model.basilea.CuentaBasilea;
import cl.bcs.contabilidad.domain.model.basilea.CuentaPropia;
import cl.bcs.contabilidad.domain.model.basilea.DatoBusquedaBasilea;
import cl.bcs.contabilidad.domain.model.basilea.RegistroInterfazBasilea;
import cl.bcs.contabilidad.domain.port.out.InterfazBasileaRepository;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * JDBC adapter of {@link InterfazBasileaRepository}: the legacy statements of ProcesarInterfazBasilea.java:33-41.
 * The own-account query is the legacy custodia ConsultarCustodiaPortafolio_ConsultarCtaPropia.sql, copied verbatim as
 * ProcesarInterfazBasilea_SelectCuentaPropia.sql. Columns are read by label; one a statement does not select stays
 * null, as with legacy {@code SQL.query}. The insert binds every {@code :Name} explicitly (legacy {@code SQL.batch}
 * bound them from the public fields of ReplicaInterfazBasilea).
 */
@Repository
public class InterfazBasileaJdbcRepository implements InterfazBasileaRepository {

    private static final String SQL_CUENTAS =
            SqlResources.load("cl/bcs/contabilidad/sql/ProcesarInterfazBasilea_SelectCuentasBasilea.sql");
    private static final String SQL_CUENTA_PROPIA =
            SqlResources.load("cl/bcs/contabilidad/sql/ProcesarInterfazBasilea_SelectCuentaPropia.sql");
    private static final String SQL_ACCIONES =
            SqlResources.load("cl/bcs/contabilidad/sql/ProcesarInterfazBasilea_SelectAcciones.sql");
    private static final String SQL_RF_IF =
            SqlResources.load("cl/bcs/contabilidad/sql/ProcesarInterfazBasilea_SelectRfIf.sql");
    private static final String SQL_VENTAS_CORTAS =
            SqlResources.load("cl/bcs/contabilidad/sql/ProcesarInterfazBasilea_SelectVentasCortas.sql");
    private static final String SQL_PACTOS_VIGENTES =
            SqlResources.load("cl/bcs/contabilidad/sql/ProcesarInterfazBasilea_SelectPactosVigentes.sql");
    private static final String SQL_FORWARD =
            SqlResources.load("cl/bcs/contabilidad/sql/ProcesarInterfazBasilea_SelectForward.sql");
    private static final String SQL_INSERT =
            SqlResources.load("cl/bcs/contabilidad/sql/ProcesarInterfazBasilea_InsertInterfazBasilea.sql");

    private static final RowMapper<CuentaBasilea> CUENTA = SqlRow.mapper(r -> new CuentaBasilea(
            r.str("ConceptoAInformar"), r.str("TipoCuentaContable"), r.str("CuentaContable"),
            r.str("NombreCuentaContable"), r.str("IdBolsa"), r.str("EsVentaCorta"), r.str("RelacionadoCorredora"),
            r.str("IdClasificacionTipoInstrumento"), r.str("IdTipoInstrumento"), r.str("IdEmisor"), r.str("Nemotecnico"),
            r.str("Grupo"), r.str("Producto"), r.str("IdEstado"), r.str("IdTipoOperacion")));

    private static final RowMapper<CuentaPropia> CUENTA_PROPIA = SqlRow.mapper(r -> new CuentaPropia(
            r.lng("IdClienteCtaPropia"), r.str("DocumentoIdentidadCtaPropia")));

    private static final RowMapper<DatoBusquedaBasilea> DATO = SqlRow.mapper(r -> new DatoBusquedaBasilea(
            r.str("IdBolsa"), r.str("EsVentaCorta"), r.str("TipoCarteraCorredor"),
            r.str("IdClasificacionTipoInstrumento"), r.str("IdTipoInstrumento"), r.str("IdEmisor"), r.str("Emisor"),
            r.str("Nemotecnico"), r.str("Grupo"), r.str("Producto"), r.str("IdMonedaInstrumento"), r.dec("Monto"),
            r.str("DocumentoIdentidadEmisor"), r.str("IdTipoReajuste"), r.str("FechaVencimiento"),
            r.str("FechaVencimientoOperacion"), r.str("FechaOperacion"), r.str("FechaLiquidacion"), r.str("Plazo"),
            r.str("Folio"), r.dec("Tasa"), r.str("RelacionadoCorredora"), r.str("IdTipoOperacion"),
            r.str("IdMonedaPrincipal"), r.str("IdMonedaSecundaria"), r.str("NemoBasilea")));

    private final NamedParameterJdbcTemplate jdbc;

    public InterfazBasileaJdbcRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<CuentaBasilea> consultarCuentasBasileaActivas() {
        return jdbc.query(SQL_CUENTAS, new MapSqlParameterSource(), CUENTA);
    }

    @Override
    public List<CuentaPropia> consultarCuentaPropia(Long idEmpresa) {
        return jdbc.query(SQL_CUENTA_PROPIA, new MapSqlParameterSource("IdEmpresa", idEmpresa), CUENTA_PROPIA);
    }

    @Override
    public List<DatoBusquedaBasilea> consultarAcciones(Long idCliente) {
        return jdbc.query(SQL_ACCIONES, new MapSqlParameterSource("IdCliente", idCliente), DATO);
    }

    @Override
    public List<DatoBusquedaBasilea> consultarRfIf(Long idCliente, String fechaSistema) {
        return jdbc.query(SQL_RF_IF, clienteYFecha(idCliente, fechaSistema), DATO);
    }

    @Override
    public List<DatoBusquedaBasilea> consultarVentasCortas(Long idCliente, String fechaSistema) {
        return jdbc.query(SQL_VENTAS_CORTAS, clienteYFecha(idCliente, fechaSistema), DATO);
    }

    @Override
    public List<DatoBusquedaBasilea> consultarPactosVigentes(String fechaSistema) {
        return jdbc.query(SQL_PACTOS_VIGENTES, new MapSqlParameterSource("FechaSistema", fechaSistema), DATO);
    }

    @Override
    public List<DatoBusquedaBasilea> consultarForward(String fechaSistema) {
        return jdbc.query(SQL_FORWARD, new MapSqlParameterSource("FechaSistema", fechaSistema), DATO);
    }

    @Override
    @Transactional
    public void insertarInterfazBasilea(List<RegistroInterfazBasilea> registros) {
        if (registros.isEmpty()) return;
        jdbc.batchUpdate(SQL_INSERT, SqlParams.batch(registros, InterfazBasileaJdbcRepository::parametros));
    }

    private static MapSqlParameterSource clienteYFecha(Long idCliente, String fechaSistema) {
        return new MapSqlParameterSource("IdCliente", idCliente).addValue("FechaSistema", fechaSistema);
    }

    /** Every :Name of ProcesarInterfazBasilea_InsertInterfazBasilea.sql (ID_INTERNO comes from the sequence). */
    private static MapSqlParameterSource parametros(RegistroInterfazBasilea r) {
        return new MapSqlParameterSource()
                .addValue("FechaEnvio", r.fechaEnvio())
                .addValue("TipoProducto", r.tipoProducto())
                .addValue("RelacionContable", r.relacionContable())
                .addValue("CuentaContable", r.cuentaContable())
                .addValue("CodigoMoneda", r.codigoMoneda())
                .addValue("NumeroOperacion", r.numeroOperacion())
                .addValue("TipoTasaInteres", r.tipoTasaInteres())
                .addValue("TasaInteresOperacion", r.tasaInteresOperacion())
                .addValue("TasaInteresCompuestoAnual", r.tasaInteresCompuestoAnual())
                .addValue("TipoTasaBase", r.tipoTasaBase())
                .addValue("Spread", r.spread())
                .addValue("CadenciaRepricing", r.cadenciaRepricing())
                .addValue("CentroGestor", r.centroGestor())
                .addValue("GestorLibro", r.gestorLibro())
                .addValue("EsTasaTecho", r.esTasaTecho())
                .addValue("TipoTasaTecho", r.tipoTasaTecho())
                .addValue("TasaTecho", r.tasaTecho())
                .addValue("EsTasaPiso", r.esTasaPiso())
                .addValue("TipoTasaPiso", r.tipoTasaPiso())
                .addValue("TasaPiso", r.tasaPiso())
                .addValue("OpcionPrepago", r.opcionPrepago())
                .addValue("FechaOpcionPrepago", r.fechaOpcionPrepago())
                .addValue("CodigoProducto", r.codigoProducto())
                .addValue("CodigoSubproducto", r.codigoSubproducto())
                .addValue("TipoCobertura", r.tipoCobertura())
                .addValue("ClaseEmision", r.claseEmision())
                .addValue("Cartera", r.cartera())
                .addValue("CodigoEstandardBono", r.codigoEstandardBono())
                .addValue("Strike", r.strike())
                .addValue("FechaVencimientoFinal", r.fechaVencimientoFinal())
                .addValue("IndicadorCompensacion", r.indicadorCompensacion())
                .addValue("PaisOrigen", r.paisOrigen())
                .addValue("DivisaCompensancion", r.divisaCompensancion())
                .addValue("FechaInicioOperacion", r.fechaInicioOperacion())
                .addValue("Carencia", r.carencia())
                .addValue("TipoAmortizacion", r.tipoAmortizacion())
                .addValue("EsAmortizacionEstandard", r.esAmortizacionEstandard())
                .addValue("FrecuenciaPago", r.frecuenciaPago())
                .addValue("FechaRevisionTasa", r.fechaRevisionTasa())
                .addValue("TipoOpcion", r.tipoOpcion())
                .addValue("TipoVentanaEjecucion", r.tipoVentanaEjecucion())
                .addValue("ValorTirCompra", r.valorTirCompra())
                .addValue("ValorTirMercado", r.valorTirMercado())
                .addValue("IndicadorTipoVencimiento", r.indicadorTipoVencimiento())
                .addValue("IndicadorFlujo", r.indicadorFlujo())
                .addValue("FechaVencimientoFlujo", r.fechaVencimientoFlujo())
                .addValue("PlazoFlujo", r.plazoFlujo())
                .addValue("FlujoMonedaOrigen", r.flujoMonedaOrigen())
                .addValue("FlujoValorContable", r.flujoValorContable())
                .addValue("FlujoValorFinal", r.flujoValorFinal())
                .addValue("MonedaFlujo", r.monedaFlujo())
                .addValue("IndicadorTipoMora", r.indicadorTipoMora())
                .addValue("SaldoCapitalMonedaOrigen", r.saldoCapitalMonedaOrigen())
                .addValue("SaldoCapitalValorContable", r.saldoCapitalValorContable())
                .addValue("SaldoCapitalValorFinal", r.saldoCapitalValorFinal())
                .addValue("ReajusteCapital", r.reajusteCapital())
                .addValue("MonedaDevengo", r.monedaDevengo())
                .addValue("InteresDevengado", r.interesDevengado())
                .addValue("InteresDevengadoValorContable", r.interesDevengadoValorContable())
                .addValue("InteresDevengadoValorFinal", r.interesDevengadoValorFinal())
                .addValue("CarteraVencidaMonedaOrigen", r.carteraVencidaMonedaOrigen())
                .addValue("CarteraVencidaValorContable", r.carteraVencidaValorContable())
                .addValue("CarteraVencidaValorFinal", r.carteraVencidaValorFinal())
                .addValue("CastigosMonedaOrigen", r.castigosMonedaOrigen())
                .addValue("CastigosValorContable", r.castigosValorContable())
                .addValue("CastigosValorFinal", r.castigosValorFinal())
                .addValue("NumeroCliente", r.numeroCliente())
                .addValue("RutCliente", r.rutCliente())
                .addValue("DigitoVerificador", r.digitoVerificador())
                .addValue("IndicadorLibro", r.indicadorLibro())
                .addValue("IndicadorTipoFlujo", r.indicadorTipoFlujo())
                .addValue("IndicadorReversa", r.indicadorReversa())
                .addValue("FechaRegistroOperacionSistema", r.fechaRegistroOperacionSistema())
                .addValue("MontoOriginalOperacion", r.montoOriginalOperacion())
                .addValue("ValorTasaCostoFondoOperacion", r.valorTasaCostoFondoOperacion())
                .addValue("FechaPrimerFlujo", r.fechaPrimerFlujo())
                .addValue("Filler", r.filler());
    }
}
