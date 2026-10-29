package cl.bcs.apv.jobs.job;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cl.bcs.apv.domain.port.out.Colaboradores;
import cl.bcs.apv.domain.port.out.ConfiguracionSistema;
import cl.bcs.apv.infrastructure.jdbc.SqlNombrado;
import cl.bcs.apv.jobs.job.FilasBonificacion.ParametrosPrograma;
import cl.bcs.apv.jobs.job.FilasRV.Instrumento;
import cl.bcs.apv.jobs.job.FilasRV.Orden;
import cl.bcs.apv.jobs.job.FilasRV.ResultadoInstrumento;
import cl.bcs.apv.jobs.job.contrato.CalcularMontoYCobrosOrden;
import cl.bcs.apv.jobs.job.contrato.ConsultarCustodiaPortafolio;
import cl.bcs.apv.jobs.job.contrato.ConsultarFlujoCaja;
import cl.bcs.apv.jobs.job.contrato.CrearOrdenes;
import cl.bcs.apv.jobs.job.contrato.MensajeSalida;
import cl.bcs.apv.jobs.job.contrato.SaldoFlujoCaja.ObjFLUJOCAJA;
import cl.bcs.apv.jobs.runtime.ApvJob;
import cl.bcs.apv.jobs.runtime.JobContext;

/**
 * Tax-bonus orders in equities: for each order to bonify it buys (cash flow check) or sells (custody check) {@code PorcentajeBonificacionFiscal} percent
 * (legacy IngresarOrdenRVBonificacionFiscal.java:208-546). Prices come from the platform price procedure, amounts from CalcularMontoYCobrosOrden, the orders
 * from CrearOrdenes, all through the {@link Colaboradores} port with the legacy JSON.
 *
 * <p>Reproduced bug for bug (README_CARACTERIZACION.md, RVB-*): the cash check compares the balance with the PRICE of one share, not the amount (RVB-003); the
 * balance is not reset per order (RVB-010); a {@code return true} inside the loop discards ALL orders and commits (RVB-005/019/022); only the first custody row is
 * read (RVB-020); the last price row wins (RVB-013); an unknown action keeps the defaults (RVB-023/024); every CrearOrdenes message is logged as "ADVERTENCIAS RV"
 * (RVB-033). The temporary table lives on the job connection, the DROP runs in a finally (on H2 a DDL commits earlier writes: documented limit of the proof).
 */
public final class IngresarOrdenRVBonificacionFiscal implements ApvJob {

    /** Legacy class name, the routing key of /internal/job-call. */
    public static final String CLASE = "cl.bcs.APV.jobs.IngresarOrdenRVBonificacionFiscal";

    private static final Logger log = LoggerFactory.getLogger(IngresarOrdenRVBonificacionFiscal.class);
    private static final String ID_TIPO_PROGRAMA = "INGRESAR_ORDEN_RV_BONIFICACION_FISCAL"; // :39
    private static final String PROCEDIMIENTO_PRECIOS = "BSCL.Plataforma.BD.Servicios::INS.ConsultarInstrumentoPrecioBolsa";

    private final SqlNombrado sql;
    private final ConfiguracionSistema configuracion;
    private final Colaboradores colaboradores;
    private final FormatoCantidad formato;
    private final String sqlSelectOrdenBf = SqlNombrado.recurso("jobs/IngresarOrdenRVBonificacionFiscal_SelectOrdenBF");
    private final String sqlSelectParametros = SqlNombrado.recurso("jobs/IngresarOrdenFMBonificacionFiscal_SelectParametros");
    private final String sqlCreateTabla = SqlNombrado.recurso("jobs/IngresarOrdenRVBonificacionFiscal_CreateTablePrecioInstrumentos");
    private final String sqlInsertTabla = SqlNombrado.recurso("jobs/IngresarOrdenRVBonificacionFiscal_InsertTablePrecioInstrumentos");
    private final String sqlDropTabla = SqlNombrado.recurso("jobs/IngresarOrdenRVBonificacionFiscal_DropTablePrecioInstrumentos");

    public IngresarOrdenRVBonificacionFiscal(NamedParameterJdbcTemplate jdbc, ConfiguracionSistema configuracion, Colaboradores colaboradores) {
        this.sql = LogPrograma.sql(jdbc);
        this.configuracion = configuracion;
        this.colaboradores = colaboradores;
        this.formato = new FormatoCantidad(sql);
    }

    @Override
    public String clase() {
        return CLASE;
    }

    @Override
    public boolean transaction(JobContext ctx) throws Exception {
        log.info("INICIO JOB");
        Long idPrograma = ctx.idPrograma();
        Long idEmpresa = 0L;
        Long idUsuario = 0L;
        List<Long> idsOrdenABonificar = new ArrayList<>();
        // :230-239 the parameters of the program (BAT.ParametrosPrograma)
        for (ParametrosPrograma parametro : sql.consultar(sqlSelectParametros, Map.of("IdPrograma", idPrograma), ParametrosPrograma.class)) {
            if (parametro.NombreParametro.equals("IdEmpresa")) idEmpresa = Long.parseLong(parametro.Valor);
            if (parametro.NombreParametro.equals("IdUsuario")) idUsuario = Long.parseLong(parametro.Valor);
            if (parametro.NombreParametro.equals("IdOrden")) idsOrdenABonificar.add(Long.parseLong(parametro.Valor));
        }
        String fechaSistema = ConfiguracionLegacy.parametro(configuracion, idEmpresa, "PLATAFORMA", "PLA.FechaSistema"); // :240
        ConfiguracionLegacy.parametro(configuracion, idEmpresa, "PLATAFORMA", "PLA.MonedaSistema"); // :241 read, never used (RVB-045)
        long porcentaje = ConfiguracionLegacy.parametroLong(configuracion, idEmpresa, "APV", "APV.PorcentajeBonificacionFiscal"); // :246

        Map<String, Object> params = new HashMap<>();
        params.put("IdsOrden", idsOrdenABonificar);
        List<Orden> ordenesRv = sql.consultar(sqlSelectOrdenBf, params, Orden.class);
        if (ordenesRv.size() == 0) {
            LogPrograma.agregar(sql, idPrograma, ID_TIPO_PROGRAMA, "ADVERTENCIAS RV : No cuentan con portafolio BF");
            return true;
        }
        try {
            return procesar(ctx, ordenesRv, idPrograma, idEmpresa, idUsuario, fechaSistema, porcentaje);
        } catch (Exception er) {
            log.error("ERROR", er);
            throw er;
        } finally {
            try {
                sql.ejecutar(sqlDropTabla);
            } catch (Exception er) {
                log.error("ERROR", er);
            }
        }
    }

    /** Legacy :257-519 (the body of the try). */
    private boolean procesar(JobContext ctx, List<Orden> ordenesRv, Long idPrograma, Long idEmpresa, Long idUsuario, String fechaSistema, long porcentaje)
            throws Exception {
        sql.ejecutar(sqlCreateTabla);
        List<Instrumento> instrumentos = new ArrayList<>();
        for (Orden filaOrden : ordenesRv) {
            Instrumento instru = new Instrumento();
            instru.IdEmpresa = idEmpresa;
            instru.IdInstrumento = filaOrden.IdInstrumento;
            instru.IdBolsa = "BCS";
            instru.Fecha = fechaSistema;
            instru.IdCondicionDeLiquidacion = "T0";
            instrumentos.add(instru);
        }
        sql.lote(sqlInsertTabla, instrumentos);
        List<ResultadoInstrumento> resultadoInstrumento = precios(instrumentos);

        Map<String, ObjFLUJOCAJA> flujoPorPortafolio = new HashMap<>();
        BigDecimal saldoFlujoCaja = BigDecimal.ZERO; // NOT reset per order (RVB-010)
        List<CalcularMontoYCobrosOrden.Orden_Entrada> calculos = new ArrayList<>();
        List<CrearOrdenes.ObjOrdenesEntrada> ordenes = new ArrayList<>();
        long contadorOrden = 1L;
        for (Orden filaOrden : ordenesRv) {
            BigDecimal cantidadBF = BigDecimal.ZERO;
            if (filaOrden.AccionOrden.equalsIgnoreCase("VENTA")) {
                ConsultarCustodiaPortafolio.Salida_ConsultaCustodiaPortafolio salida = custodia(filaOrden, idEmpresa, idUsuario, fechaSistema);
                if (salida.P_OUT_TBL.size() > 0) {
                    log.info(salida.P_OUT_TBL.get(0).CantidadTotal.toString());
                    log.info(ConstructorOrdenesRV.porcentaje(filaOrden.Cantidad, porcentaje).toString());
                    BigDecimal cantidadCustodia = salida.P_OUT_TBL.get(0).CantidadTotal; // only the first row (RVB-020)
                    cantidadBF = ConstructorOrdenesRV.porcentaje(filaOrden.Cantidad, porcentaje);
                    cantidadBF = formato.formatear(cantidadBF, filaOrden.IdMonedaLiquidacionOrden, filaOrden.IdTipoInstrumento, filaOrden.IdTipoOperacion);
                    if (cantidadCustodia.compareTo(cantidadBF) < 0) {
                        cantidadBF = cantidadCustodia; // not enough: everything is sold
                    }
                } else {
                    return true; // no custody: ends, discarding ALL orders (RVB-019)
                }
            } else if (filaOrden.AccionOrden.equalsIgnoreCase("COMPRA")) {
                for (ObjFLUJOCAJA flujo : flujoCaja(ordenesRv, filaOrden, idEmpresa, idUsuario, fechaSistema)) {
                    flujoPorPortafolio.putIfAbsent(flujo.IdPortafolio + "", flujo);
                }
                ObjFLUJOCAJA objFlujoCaja = flujoPorPortafolio.get(filaOrden.IdPortafolio + "");
                if (objFlujoCaja != null) {
                    saldoFlujoCaja = objFlujoCaja.Saldo;
                }
            }

            BigDecimal precio = null;
            for (ResultadoInstrumento r : resultadoInstrumento) { // a null list: NullPointerException (RVB-014); the last match wins (RVB-013)
                if (r.IdInstrumento.equals(filaOrden.IdInstrumento)) precio = r.PrecioCierre;
            }
            CalcularMontoYCobrosOrden.Orden_Entrada calculo = ConstructorOrdenesRV.calculo(filaOrden, contadorOrden, fechaSistema, precio);
            if ("COMPRA".equals(filaOrden.AccionOrden)) {
                calculo.MontoTotal = ConstructorOrdenesRV.porcentaje(filaOrden.MontoNeto, porcentaje);
                calculo.EsPorMonto = "S";
                calculo.CantidadOrden = BigDecimal.ZERO;
                if (saldoFlujoCaja.compareTo(calculo.MontoTotal) < 0) {
                    if (saldoFlujoCaja.compareTo(calculo.PrecioOrden) < 0) { // the PRICE of one share, not the amount (RVB-003)
                        String mensaje = "ADVERTENCIAS RV : Saldo Caja Insuficiente para realizar la Orden BF.";
                        LogPrograma.agregar(sql, idPrograma, ID_TIPO_PROGRAMA, mensaje);
                        colaboradores.invocar("AgregarLog", JsonLegacy.aJson(
                                ConstructorOrdenesRV.logSaldoInsuficiente(filaOrden, idPrograma, idUsuario, idEmpresa, fechaSistema, calculo.MontoTotal, mensaje)));
                        return true; // discards ALL orders (RVB-005)
                    }
                    calculo.MontoTotal = saldoFlujoCaja;
                }
            } else if ("VENTA".equals(filaOrden.AccionOrden)) {
                calculo.MontoTotal = BigDecimal.ZERO;
                calculo.EsPorMonto = "N";
                calculo.CantidadOrden = cantidadBF;
            }
            calculo.TasaReporte = BigDecimal.ZERO;
            calculo.TasaReportado = BigDecimal.ZERO;
            calculo.PlazoReporte = 0L;
            calculo.BaseTasa = 0L;
            log.info("{}", filaOrden);
            calculos.add(calculo);
            ordenes.add(ConstructorOrdenesRV.orden(filaOrden, contadorOrden, calculo.MontoTotal, cantidadBF));
            contadorOrden++;
        }
        if (ordenes.size() > 0) {
            crear(idPrograma, idEmpresa, idUsuario, calculos, ordenes);
        }
        log.info("FIN JOB");
        return true;
    }

    /** Legacy :273-295: price procedure; the rows are the legacy {@code ResultadoInstrumento}; absent output is null (the legacy map had no key). */
    private List<ResultadoInstrumento> precios(List<Instrumento> instrumentos) {
        JsonObject entrada = new JsonObject();
        entrada.add("p_Instrumentos", JsonLegacy.gson().toJsonTree(instrumentos));
        JsonObject peticion = new JsonObject();
        peticion.addProperty("procedimiento", PROCEDIMIENTO_PRECIOS);
        peticion.add("entrada", entrada);
        JsonObject salida = colaboradores.invocar("StoredProcedure", peticion);
        JsonElement filas = salida == null ? null : salida.get("p_out_Instrumento");
        if (filas == null || filas.isJsonNull()) {
            return null;
        }
        List<ResultadoInstrumento> resultado = new ArrayList<>();
        for (JsonElement fila : (JsonArray) filas) {
            resultado.add(JsonLegacy.gson().fromJson(fila, ResultadoInstrumento.class));
        }
        return resultado;
    }

    /** Legacy :299-338. */
    private ConsultarCustodiaPortafolio.Salida_ConsultaCustodiaPortafolio custodia(Orden filaOrden, Long idEmpresa, Long idUsuario, String fechaSistema) {
        ConsultarCustodiaPortafolio.Entrada_ConsultaCustodiaPortafolio entrada = new ConsultarCustodiaPortafolio.Entrada_ConsultaCustodiaPortafolio();
        entrada.p_IdEmpresa = idEmpresa;
        entrada.p_IdUsuario = idUsuario;
        entrada.p_IdModulo = "APV";
        entrada.p_IdCliente = filaOrden.IdCliente;
        entrada.p_IdPortafolio = filaOrden.IdPortafolio;
        entrada.p_IdInstrumento = filaOrden.IdInstrumento;
        entrada.p_IdTipoInstrumento = "";
        entrada.p_IdClasificacionTipoInstrumento = "RV";
        entrada.p_IdProductoAgrupador = null;
        entrada.p_AgrupaPorBoveda = "S";
        entrada.p_AgrupaPorRelacion = "S";
        entrada.p_TipoFechaConsulta = "ADQ";
        entrada.p_FechaConsulta = fechaSistema;
        return JsonLegacy.de(colaboradores.invocar("ConsultarCustodiaPortafolio", JsonLegacy.aJson(entrada)),
                ConsultarCustodiaPortafolio.Salida_ConsultaCustodiaPortafolio.class);
    }

    /** Legacy :344-373: the cash flow of the day, in the currency of the order. */
    private List<ObjFLUJOCAJA> flujoCaja(List<Orden> ordenesRv, Orden filaOrden, Long idEmpresa, Long idUsuario, String fechaSistema) {
        List<Long> idsPortafolio = ordenesRv.stream().map(p -> p.IdPortafolio).distinct().collect(Collectors.toList());
        List<ConsultarFlujoCaja.PortafolioAConsultar> portafolios = idsPortafolio.stream().map(idPortafolio -> {
            ConsultarFlujoCaja.PortafolioAConsultar portafolio = new ConsultarFlujoCaja.PortafolioAConsultar();
            portafolio.IdMoneda = filaOrden.IdMonedaLiquidacionOrden;
            portafolio.IdPortafolio = idPortafolio;
            return portafolio;
        }).collect(Collectors.toList());
        ConsultarFlujoCaja.EntradaConsultarFlujoCaja entrada = new ConsultarFlujoCaja.EntradaConsultarFlujoCaja();
        entrada.p_IdEmpresa = idEmpresa;
        entrada.p_IdUsuario = idUsuario;
        entrada.p_IdModulo = "APV";
        entrada.p_FechaDesde = fechaSistema;
        entrada.p_FechaHasta = fechaSistema;
        entrada.p_Uso = "ORDENES_DE_COMPRA";
        entrada.p_PortafolioAConsultar = portafolios;
        ConsultarFlujoCaja.SalidaConsultarFlujoCaja salida = JsonLegacy.de(
                colaboradores.invocar("ConsultarFlujoCaja", JsonLegacy.aJson(entrada)), ConsultarFlujoCaja.SalidaConsultarFlujoCaja.class);
        return salida.P_OUT_FLUJOCAJA.stream()
                .filter(s -> s.FechaSaldo.equals(fechaSistema) && filaOrden.IdMonedaLiquidacionOrden.equals(s.IdMoneda)).collect(Collectors.toList());
    }

    /** Legacy :548-589: amounts, then the orders; every message of CrearOrdenes is logged as a warning (RVB-033). */
    private void crear(Long idPrograma, Long idEmpresa, Long idUsuario, List<CalcularMontoYCobrosOrden.Orden_Entrada> calculos,
            List<CrearOrdenes.ObjOrdenesEntrada> ordenes) throws SQLException {
        CalcularMontoYCobrosOrden.CalcularMontoYCobrosOrden_Entrada paramsCalculo = new CalcularMontoYCobrosOrden.CalcularMontoYCobrosOrden_Entrada();
        paramsCalculo.p_IdEmpresa = idEmpresa;
        paramsCalculo.p_IdUsuario = idUsuario;
        paramsCalculo.p_IdModulo = "APV";
        paramsCalculo.p_Ordenes = calculos;
        CalcularMontoYCobrosOrden.CalcularMontoYCobrosOrden_Salida outCalculo = JsonLegacy.de(
                colaboradores.invocar("CalcularMontoYCobrosOrden", JsonLegacy.aJson(paramsCalculo)), CalcularMontoYCobrosOrden.CalcularMontoYCobrosOrden_Salida.class);
        for (int i = outCalculo.p_out_Ordenes.size() - 1; i >= 0; i--) {
            for (int j = ordenes.size() - 1; j >= 0; j--) {
                if (outCalculo.p_out_Ordenes.get(i).IdInterno.toString().equals(ordenes.get(j).IdInterno.toString())) {
                    ordenes.get(j).Cantidad = outCalculo.p_out_Ordenes.get(i).CantidadOperacion;
                }
            }
        }
        CrearOrdenes.CrearOrdenesEntrada paramsOrdenes = new CrearOrdenes.CrearOrdenesEntrada();
        paramsOrdenes.p_IdEmpresa = idEmpresa;
        paramsOrdenes.p_IdUsuario = idUsuario;
        paramsOrdenes.p_IdModulo = "APV";
        paramsOrdenes.p_simularOperacion = "N";
        paramsOrdenes.p_UsaReglasNegocio = "S";
        paramsOrdenes.p_Ordenes = ordenes;
        CrearOrdenes.CrearOrdenesSalida outOrdenes = JsonLegacy.de(
                colaboradores.invocar("CrearOrdenes", JsonLegacy.aJson(paramsOrdenes)), CrearOrdenes.CrearOrdenesSalida.class);
        for (MensajeSalida fila : outOrdenes.P_OUT_MENSAJES) {
            LogPrograma.agregar(sql, idPrograma, ID_TIPO_PROGRAMA, "ADVERTENCIAS RV : " + fila.descripcion);
        }
        log.info(outOrdenes.toString());
    }
}
