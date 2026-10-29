package cl.bcs.cuentainversion.service;

import cl.bcs.cuentainversion.port.out.ColaboradoresReportes;
import cl.bcs.cuentainversion.reportes.ConfiguracionSistema;
import cl.bcs.cuentainversion.reportes.JsonReportes;
import cl.bcs.cuentainversion.reportes.MensajeSalida;
import cl.bcs.cuentainversion.reportes.Mensajeria;
import cl.bcs.cuentainversion.reportes.SqlReportes;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.math.MathContext;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * "Clients to pay / to collect" report (legacy {@code GenerarArchivoClientesPorPagarYCobrar}), behaviours CTA-B-182..200 and 224.
 *
 * <p>Takes the last certified movement of every investment account, adds the retained balance, guarantees, simultaneous operations
 * and the money-market value (custody quantity x unit value of the money-market funds, converted to the account currency through
 * {@code CalcularParidad}), drops the accounts whose amounts are all zero and the ones whose last movement is effective on the
 * query date, and either returns the rows ({@code EsBusqueda = S}) or writes {@code <RutaBase><RutaCta>ClientesPorPagarYCobrar_<ts>.xls}
 * with the detail sheet and an age summary (1-7, 8-9, 10-30, 31+ days) for related, not related and all clients.
 *
 * <p>The result is serialized with the default {@link Gson} (date format {@code "MMM d, yyyy, h:mm:ss a"}), as the legacy did.
 */
@Service
public class GenerarArchivoClientesPorPagarYCobrarService {

    private static final Logger log = LoggerFactory.getLogger(GenerarArchivoClientesPorPagarYCobrarService.class);

    private static final String SQL_CONFIGURACION = SqlReportes.recurso("GenerarArchivoClientesPorPagarYCobrar_SelectConfiguracion");
    private static final String SQL_ULTIMOS_SALDOS = SqlReportes.recurso("GenerarArchivoClientesPorPagarYCobrar_SelectUltimosSaldos");
    private static final String SQL_INSTRUMENTOS_MONEY_MARKET = SqlReportes.recurso("ConsultarSaldoCuentaInversion_Select_Instrumentos_Money_Market");
    private static final String SQL_INSERT_CUENTA_PASO = SqlReportes.recurso("ConsultarSaldoCuentaInversion_Insert_Cuenta_Inversion_Tabla_Paso");
    private static final String SQL_INSERT_PORTAFOLIO_MONEDA = SqlReportes.recurso("ConsultarSaldoCuentaInversion_Insert_Portafolio_Moneda");
    private static final String SQL_SALDO_A_PLAZO_SIMULTANEAS = SqlReportes.recurso("ConsultarSaldoCuentaInversion_Select_Saldo_A_Plazo_Simultaneas");
    private static final String SQL_SALDO_EN_GARANTIA = SqlReportes.recurso("ConsultarSaldoCuentaInversion_Select_Saldo_En_Garantia");
    private static final String SQL_SALDOS_RETENIDOS = SqlReportes.recurso("ConsultarSaldoCuentaInversion_Select_Saldos_Retenidos");
    private static final String SQL_VALOR_CUOTA = SqlReportes.recurso("ConsultarCustodiaPortafolioFondoMutuo_SelectValorCuota");
    private static final String SQL_CANTIDAD_CUSTODIA = SqlReportes.recurso("ConsultarSaldoCuentaInversion_Select_Cantidad_Custodia");
    private static final String SQL_LIMPIAR_PORTAFOLIOS_SIN_MONEDA =
            "DELETE FROM \"BSCL.CuentaInversion.BD::GBL.ConsultarSaldoCuentaInversion_Cuenta_Paso\" WHERE \"IdMoneda\" IS NULL";
    private static final String SQL_LIMPIAR_TRABAJO =
            "DELETE FROM \"BSCL.CuentaInversion.BD::GBL.ConsultarSaldoCuentaInversion_Cuenta_Delete\" WHERE \"IdTrabajo\" = ";

    /** Above this many accounts the money market is not computed (massive run). */
    private static final int MAXIMO_CUENTAS_MONEY_MARKET = 1000;
    private static final String FORMATO_MONEDA = "_ $* #,##0_ ;_ $* -#,##0_ ;_ $* \"-\"??_ ;_ @_ ";

    private final SqlReportes sql;
    private final ColaboradoresReportes colaboradores;

    public GenerarArchivoClientesPorPagarYCobrarService(NamedParameterJdbcTemplate jdbc, ColaboradoresReportes colaboradores) {
        this.sql = new SqlReportes(jdbc);
        this.colaboradores = colaboradores;
    }

    @Transactional(rollbackFor = Exception.class)
    public JsonObject execute(JsonObject params) throws Exception {
        Salida salida = generar(JsonReportes.leer(params, Entrada.class));
        salida.P_OUT_ERRORESCONTADOR = MensajeSalida.contarErrores(salida.P_OUT_MENSAJES);
        Gson gson = new Gson();
        return JsonParser.parseString(gson.toJson(salida)).getAsJsonObject();
    }

    Salida generar(Entrada in) throws Exception {
        log.info("INICIO::GenerarArchivoClientePorPagarYCobrar");
        Salida out = new Salida();
        Mensajeria mensajes = new Mensajeria(sql, in.p_IdEmpresa);

        Map<String, Object> pClientes = new HashMap<>();
        pClientes.put("IdEmpresa", in.p_IdEmpresa);
        pClientes.put("IdMoneda", in.IdMoneda);
        pClientes.put("TipoSaldo", in.TipoSaldo);
        List<Cuenta> cuentas = sql.consultar(SQL_ULTIMOS_SALDOS, pClientes, Cuenta.class);

        // work table of the balance query (portfolio / currency pairs of the accounts)
        long idTrabajo = (long) (Math.random() * 10000000000L);
        List<CuentaProceso> proceso = new ArrayList<>();
        for (Cuenta c : cuentas) {
            CuentaProceso p = new CuentaProceso();
            p.IdMoneda = c.IdMoneda;
            p.IdPortafolio = c.IdPortafolio;
            p.IdTrabajo = idTrabajo;
            proceso.add(p);
        }
        sql.lote(SQL_INSERT_CUENTA_PASO, proceso);
        sql.lote(SQL_INSERT_PORTAFOLIO_MONEDA.replace(":IdTrabajo", String.valueOf(idTrabajo)), new ArrayList<Object>());
        sql.ejecutar(SQL_LIMPIAR_PORTAFOLIOS_SIN_MONEDA);

        Map<String, Object> pSaldos = new HashMap<>();
        pSaldos.put("p_IdEmpresa", in.p_IdEmpresa);
        pSaldos.put("p_FechaDesde", in.FechaSistema);
        pSaldos.put("IdTrabajo", idTrabajo);
        // LEGACY DEFECT kept (see README CTA-B-189): the guarantee SQL filters on :p_FechaHasta but the legacy never bound it (NULL), so no guarantee is ever found.
        Map<String, Object> pGarantias = new HashMap<>(pSaldos);
        pGarantias.put("p_FechaHasta", null);

        // LEGACY DEFECT kept (see README CTA-B-200): Collectors.toMap throws IllegalStateException when two rows share portfolio-currency.
        Map<String, BigDecimal> garantias = montosPorCuenta(sql.consultar(SQL_SALDO_EN_GARANTIA, pGarantias, Saldo.class));
        Map<String, BigDecimal> retenidos = montosPorCuenta(sql.consultar(SQL_SALDOS_RETENIDOS, pSaldos, Saldo.class));
        Map<String, BigDecimal> simultaneas = montosPorCuenta(sql.consultar(SQL_SALDO_A_PLAZO_SIMULTANEAS, pSaldos, Saldo.class));

        calcularMoneyMarket(in, cuentas, proceso, idTrabajo);

        // LEGACY DEFECT kept (see README CTA-B-187..): the clean-up deletes from _Cuenta_Delete, not from _Cuenta_Paso, so the work rows stay.
        sql.ejecutar(SQL_LIMPIAR_TRABAJO + idTrabajo);

        for (Cuenta c : cuentas) {
            String clave = c.IdPortafolio + "-" + c.IdMoneda;
            c.Garantias = garantias.getOrDefault(clave, BigDecimal.ZERO);
            c.SaldoRetenido = retenidos.getOrDefault(clave, BigDecimal.ZERO);
            c.Simultanea = simultaneas.getOrDefault(clave, BigDecimal.ZERO);
        }

        Date fechaConsulta = Fecha.toDate(in.FechaSistema);
        cuentas = cuentas.stream()
                .filter(c -> c.Saldo.signum() != 0 || c.SaldoRetenido.signum() != 0 || c.Garantias.signum() != 0
                        || c.Simultanea.signum() != 0 || c.MoneyMarket.signum() != 0 || c.Pacto.signum() != 0)
                // LEGACY DEFECT kept (see README CTA-B-188): the legacy "one day before" adds 0 days, so movements effective on the query date are dropped.
                .filter(c -> fechaConsulta.after(c.FechaEfectivaMovimiento))
                .collect(Collectors.toList());

        // LEGACY DEFECT kept (see README CTA-B-199): EsBusqueda absent -> NullPointerException after all the work.
        if (in.EsBusqueda.equals("S")) {
            out.P_OUT_RESULTADO = cuentas;
            return out;
        }
        out.P_OUT_RESULTADO = null;

        if (cuentas.isEmpty()) {
            out.P_OUT_MENSAJES.add(mensajes.error("FALTA_DATA_REPORTE", null));
            return out;
        }

        String rutaArchivo = "";
        String rutaCta = "";
        for (Configuracion conf : sql.consultar(SQL_CONFIGURACION, Map.of("IdEmpresa", in.p_IdEmpresa), Configuracion.class)) {
            rutaArchivo = conf.RutaArchivo != null ? conf.RutaArchivo : "";
            rutaCta = conf.RutaCta != null ? conf.RutaCta : "";
        }
        if (rutaArchivo.isEmpty() || rutaCta.isEmpty()) {
            out.P_OUT_MENSAJES.add(mensajes.error("FALTA_CONFIGURACION_RUTA_ARCHIVO", null));
            return out;
        }

        String nombre = "ClientesPorPagarYCobrar_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".xls";
        out.P_OUT_RUTAARCHIVO = rutaCta + nombre;
        if (!escribirArchivo(in, mensajes, rutaArchivo + nombre, cuentas)) {
            out.P_OUT_MENSAJES.add(mensajes.error("ERROR_GENERAR_ARCHIVO", null));
        }
        log.info("FIN::GenerarArchivoClientePorPagarYCobrar");
        return out;
    }

    private static Map<String, BigDecimal> montosPorCuenta(List<Saldo> saldos) {
        return saldos.stream().collect(Collectors.toMap(s -> s.IdPortafolio + "-" + s.IdMoneda, s -> s.Monto));
    }

    /**
     * Money market per account: custody quantity x unit value of the funds marked money market; converted from the system currency
     * to the account currency (one collaborator call for all the foreign-currency accounts). A rate that is not available leaves the
     * unconverted amount (README CTA-B-194).
     */
    private void calcularMoneyMarket(Entrada in, List<Cuenta> cuentas, List<CuentaProceso> proceso, long idTrabajo) throws Exception {
        List<String> nemotecnicos = sql.consultar(SQL_INSTRUMENTOS_MONEY_MARKET, Map.of(), InstrumentoMoneyMarket.class).stream()
                .filter(s -> s.IdInstrumento != null).map(s -> s.Nemotecnico).collect(Collectors.toList());

        Map<String, Object> valoresCuota = new HashMap<>();
        Map<Long, List<CantidadCustodia>> cantidades = new HashMap<>();
        if (!nemotecnicos.isEmpty() && proceso.size() < MAXIMO_CUENTAS_MONEY_MARKET) {
            Map<String, Object> p = new HashMap<>();
            p.put("IdEmpresa", in.p_IdEmpresa);
            p.put("FechaConsulta", in.FechaSistema);
            p.put("IdSerieFondo", nemotecnicos);
            List<ValorCuota> cuotas = sql.consultar(SQL_VALOR_CUOTA, p, ValorCuota.class);
            cantidades = sql.consultar(SQL_CANTIDAD_CUSTODIA, Map.of("Nemotecnico", nemotecnicos), CantidadCustodia.class).stream()
                    .collect(Collectors.groupingBy(c -> c.IdPortafolio));
            valoresCuota = Utils.List2Hash(cuotas, "IdSerieFondo");
        }

        String monedaSistema = new ConfiguracionSistema(sql).cadena(in.p_IdEmpresa, ConfiguracionSistema.PLATAFORMA, "PLA.MonedaSistema");
        JsonArray aConvertir = new JsonArray();
        for (Cuenta cuenta : cuentas) {
            BigDecimal monto = BigDecimal.ZERO;
            for (CantidadCustodia reg : cantidades.getOrDefault(cuenta.IdPortafolio, List.of())) {
                ValorCuota cuota = (ValorCuota) valoresCuota.get(reg.Nemotecnico);
                if (cuota != null) {
                    monto = monto.add(reg.CantidadCustodia.multiply(cuota.ValorCuota, MathContext.DECIMAL128));
                }
            }
            cuenta.MoneyMarket = monto;
            if (!cuenta.IdMoneda.equalsIgnoreCase(monedaSistema)) {
                JsonObject c = new JsonObject();
                c.addProperty("IdTrabajo", idTrabajo);
                c.addProperty("IdInterno", cuenta.IdCuentaInversion);
                c.addProperty("Fecha", in.FechaSistema);
                c.addProperty("IdMonedaOrigen", monedaSistema);
                c.addProperty("IdMonedaDestino", cuenta.IdMoneda);
                c.addProperty("ValorCambioLocalModificado", BigDecimal.ZERO);
                c.addProperty("IdUso", "");
                c.addProperty("MontoOrigen", cuenta.MoneyMarket);
                aConvertir.add(c);
            }
        }
        if (aConvertir.isEmpty()) {
            return;
        }
        JsonObject entrada = new JsonObject();
        entrada.addProperty("p_IdEmpresa", in.p_IdEmpresa);
        entrada.addProperty("p_IdTrabajo", idTrabajo);
        entrada.add("p_CalculosEntrada", aConvertir);
        Map<Long, BigDecimal> convertido = new HashMap<>();
        for (JsonElement e : colaboradores.calcularParidad(entrada).getAsJsonArray("p_out_CalculosSalida")) {
            JsonObject s = e.getAsJsonObject();
            JsonElement destino = s.get("MontoDestino");
            convertido.put(s.get("IdInterno").getAsLong(), destino == null || destino.isJsonNull() ? null : destino.getAsBigDecimal());
        }
        for (Cuenta cuenta : cuentas) {
            BigDecimal m = convertido.get(cuenta.IdCuentaInversion);
            cuenta.MoneyMarket = m != null ? m : cuenta.MoneyMarket;
        }
    }

    // ---------------------------------------------------------------------------------------------- workbook

    /** Writes the workbook; any failure (missing directory, ...) answers false, as the legacy did. */
    private boolean escribirArchivo(Entrada in, Mensajeria dic, String ruta, List<Cuenta> cuentas) {
        try (FileOutputStream salida = new FileOutputStream(ruta); Workbook libro = new HSSFWorkbook()) {
            Estilos estilos = new Estilos(libro);
            escribirDetalle(libro, estilos, dic, cuentas);
            escribirResumen(libro, estilos, dic, in, cuentas);
            libro.write(salida);
            return true;
        } catch (Exception e) {
            log.error("Error en la creacion del reporte Clientes Por Pagar y Cobrar: {}", e.getMessage());
            return false;
        }
    }

    private record Estilos(CellStyle fecha, CellStyle moneda, CellStyle monedaNegrita, CellStyle negrita, CellStyle encabezado) {
        Estilos(Workbook libro) {
            this(estiloFecha(libro), estiloMoneda(libro, false), estiloMoneda(libro, true), estiloNegrita(libro), estiloEncabezado(libro));
        }

        private static CellStyle estiloFecha(Workbook libro) {
            CreationHelper h = libro.getCreationHelper();
            CellStyle e = libro.createCellStyle();
            e.setDataFormat(h.createDataFormat().getFormat("dd-mm-yyyy hh:mm:ss"));
            return e;
        }

        private static CellStyle estiloMoneda(Workbook libro, boolean negrita) {
            CellStyle e = libro.createCellStyle();
            e.setDataFormat(libro.createDataFormat().getFormat(FORMATO_MONEDA));
            if (negrita) {
                Font f = libro.createFont();
                f.setBold(true);
                e.setFont(f);
            }
            return e;
        }

        private static CellStyle estiloNegrita(Workbook libro) {
            CellStyle e = libro.createCellStyle();
            Font f = libro.createFont();
            f.setBold(true);
            e.setFont(f);
            e.setAlignment(HorizontalAlignment.CENTER);
            return e;
        }

        private static CellStyle estiloEncabezado(Workbook libro) {
            CellStyle e = libro.createCellStyle();
            Font f = libro.createFont();
            f.setBold(true);
            f.setColor(IndexedColors.WHITE.getIndex());
            e.setFont(f);
            e.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
            e.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            e.setAlignment(HorizontalAlignment.CENTER);
            e.setVerticalAlignment(VerticalAlignment.CENTER);
            e.setWrapText(true);
            return e;
        }
    }

    private static final String[] ENCABEZADOS = {"UI_DOCUMENTO_IDENTIDAD", "UI_PORTAFOLIO", "UI_REPORTE_PAGAR_COBRAR_RELACION", "UI_NOMBRE_CLIENTE",
            "UI_MONEDA", "UI_SALDO_DISPONIBLE", "UI_SALDO_RETENIDO", "UI_REPORTE_PAGAR_COBRAR_MONEYMARKET", "UI_REPORTE_PAGAR_COBRAR_GARANTIAS",
            "UI_REPORTE_PAGAR_COBRAR_SIMULTANEAS", "UI_PACTO", "UI_AGENTE", "UI_REPORTE_PAGAR_COBRAR_ULTIMAOPERACION"};
    private static final int[] ANCHOS = {6000, 8000, 2500, 7500, 2300, 6000, 4000, 4000, 4000, 4000, 4000, 7000, 6500, 500, 500};

    private void escribirDetalle(Workbook libro, Estilos estilos, Mensajeria dic, List<Cuenta> cuentas) {
        Sheet hoja = libro.createSheet("Datos");
        for (int c = 0; c < ANCHOS.length; c++) {
            hoja.setColumnWidth(c, ANCHOS[c]);
        }
        Row cabecera = hoja.createRow(0);
        for (int c = 0; c < ENCABEZADOS.length; c++) {
            Cell celda = cabecera.createCell(c);
            celda.setCellValue(dic.texto(ENCABEZADOS[c], Map.of()));
            celda.setCellStyle(estilos.encabezado());
        }
        SimpleDateFormat formatoFecha = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        int fila = 0;
        for (Cuenta d : cuentas) {
            Row r = hoja.createRow(++fila);
            r.createCell(0).setCellValue(d.DocumentoIdentidad);
            r.createCell(1).setCellValue(d.CodigoProposito);
            r.createCell(2).setCellValue(d.Relacion);
            r.createCell(3).setCellValue(d.NombreCliente);
            r.createCell(4).setCellValue(d.IdMoneda);
            moneda(r, 5, estilos.moneda(), d.Saldo);
            moneda(r, 6, estilos.moneda(), d.SaldoRetenido);
            moneda(r, 7, estilos.moneda(), d.MoneyMarket);
            moneda(r, 8, estilos.moneda(), d.Garantias);
            moneda(r, 9, estilos.moneda(), d.Simultanea);
            moneda(r, 10, estilos.moneda(), d.Pacto);
            r.createCell(11).setCellValue(d.NombreAgenteCliente);
            Cell ultima = r.createCell(12);
            if (formatoFecha.format(d.FechaEfectivaMovimiento).equals("01-01-0001 00:00:00")) {
                ultima.setCellValue("");
            } else {
                ultima.setCellStyle(estilos.fecha());
                ultima.setCellValue(d.FechaEfectivaMovimiento);
            }
        }
    }

    private static void moneda(Row fila, int columna, CellStyle estilo, BigDecimal valor) {
        Cell c = fila.createCell(columna);
        c.setCellValue(valor.doubleValue());
        c.setCellStyle(estilo);
    }

    /** Totals by age range (1-7, 8-9, 10-30, 31+ days) of the amounts to pay (negative balances) and to collect (positive). */
    private static final class Totales {
        final BigDecimal[] pagar = zeros();
        final BigDecimal[] cobrar = zeros();
        BigDecimal totalPagar = BigDecimal.ZERO;
        BigDecimal totalCobrar = BigDecimal.ZERO;

        private static BigDecimal[] zeros() {
            return new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO};
        }

        Totales mas(Totales o) {
            Totales t = new Totales();
            for (int i = 0; i < 4; i++) {
                t.pagar[i] = pagar[i].add(o.pagar[i]);
                t.cobrar[i] = cobrar[i].add(o.cobrar[i]);
            }
            t.totalPagar = totalPagar.add(o.totalPagar);
            t.totalCobrar = totalCobrar.add(o.totalCobrar);
            return t;
        }
    }

    private static Totales totalesPorRangos(List<Cuenta> listado, Entrada in) throws Exception {
        Totales t = new Totales();
        Date desde = new SimpleDateFormat("yyyy-MM-dd").parse(in.FechaSistema);
        for (Cuenta c : listado) {
            int signo = c.Saldo.signum();
            if (signo == 0) {
                continue;
            }
            BigDecimal[] destino = signo < 0 ? t.pagar : t.cobrar;
            if (signo < 0) {
                t.totalPagar = t.totalPagar.add(c.Saldo);
            } else {
                t.totalCobrar = t.totalCobrar.add(c.Saldo);
            }
            long dias = TimeUnit.DAYS.convert(desde.getTime() - c.FechaEfectivaMovimiento.getTime(), TimeUnit.MILLISECONDS);
            int tramo = dias >= 0 && dias <= 7 ? 0 : dias >= 8 && dias <= 9 ? 1 : dias >= 10 && dias <= 30 ? 2 : dias >= 31 ? 3 : -1;
            if (tramo >= 0) {
                destino[tramo] = destino[tramo].add(c.Saldo);
            } else {
                log.error("Saldos antes de la fecha de consulta");
            }
        }
        return t;
    }

    private static final String[] TRAMOS = {"UI_REPORTE_1_A_7_DIAS", "UI_REPORTE_8_y_9_DIAS", "UI_REPORTE_10_A_30_DIAS", "UI_REPORTE_MAS_DE_30_DIAS"};

    private void escribirResumen(Workbook libro, Estilos estilos, Mensajeria dic, Entrada in, List<Cuenta> cuentas) throws Exception {
        // LEGACY DEFECT kept (see README CTA-B-195): the age summary adds amounts of different currencies together.
        Totales noRelacionados = totalesPorRangos(cuentas.stream().filter(c -> "".equals(c.Relacion)).collect(Collectors.toList()), in);
        Totales relacionados = totalesPorRangos(cuentas.stream().filter(c -> !"".equals(c.Relacion)).collect(Collectors.toList()), in);

        Sheet hoja = libro.createSheet("Resumen");
        hoja.setColumnWidth(0, 8000);
        hoja.setColumnWidth(1, 9000);
        hoja.setColumnWidth(2, 9000);

        Row desde = hoja.createRow(0);
        Cell titulo = desde.createCell(0);
        titulo.setCellValue(dic.texto("UI_FECHA_DESDE", Map.of()));
        titulo.setCellStyle(estilos.encabezado());
        desde.createCell(1).setCellValue(in.FechaSistema);

        bloque(hoja, estilos, dic, 3, "UI_REPORTE_TOTAL_RELACIONADO", relacionados);
        bloque(hoja, estilos, dic, 12, "UI_REPORTE_TOTAL_NO_RELACIONADO", noRelacionados);
        bloque(hoja, estilos, dic, 21, "UI_REPORTE_TOTAL_GENERAL", noRelacionados.mas(relacionados));
    }

    /** Header row, the four age ranges and the total row of one summary block, starting at row {@code inicio}. */
    private void bloque(Sheet hoja, Estilos estilos, Mensajeria dic, int inicio, String codigoTitulo, Totales t) {
        Row cabecera = hoja.createRow(inicio);
        String[] titulos = {codigoTitulo, "UI_REPORTE_POR_PAGAR", "UI_REPORTE_POR_COBRAR"};
        for (int c = 0; c < titulos.length; c++) {
            Cell celda = cabecera.createCell(c);
            celda.setCellValue(dic.texto(titulos[c], Map.of()));
            celda.setCellStyle(estilos.encabezado());
        }
        for (int i = 0; i < TRAMOS.length; i++) {
            Row r = hoja.createRow(inicio + 1 + i);
            etiqueta(r, estilos, dic, TRAMOS[i]);
            moneda(r, 1, estilos.moneda(), t.pagar[i]);
            moneda(r, 2, estilos.moneda(), t.cobrar[i]);
        }
        Row total = hoja.createRow(inicio + 1 + TRAMOS.length);
        etiqueta(total, estilos, dic, "UI_TOTAL");
        moneda(total, 1, estilos.monedaNegrita(), t.totalPagar);
        moneda(total, 2, estilos.monedaNegrita(), t.totalCobrar);
    }

    private void etiqueta(Row fila, Estilos estilos, Mensajeria dic, String codigo) {
        Cell c = fila.createCell(0);
        c.setCellValue(dic.texto(codigo, Map.of()));
        c.setCellStyle(estilos.negrita());
    }

    // ---------------------------------------------------------------------------------------------- JSON contract

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String FechaSistema;
        public String EsBusqueda;
        public String IdMoneda;
        /** POS, NEG or TOD. */
        public String TipoSaldo;
    }

    public static class Salida {
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public String P_OUT_RUTAARCHIVO = "";
        public List<Cuenta> P_OUT_RESULTADO;
    }

    /** One account row of the report (legacy {@code ClientesPagarCobrar}), also the result row of the balance query. */
    public static class Cuenta {
        public Long IdCuentaInversion = 0L;
        public Long IdPersona = 0L;
        public Long IdCliente = 0L;
        public String DocumentoIdentidad = "";
        public String NombreCliente;
        public Long IdPortafolio = 0L;
        public String CodigoProposito = "";
        public String Relacion = "";
        public Date FechaIngresoRegistro = Fecha.toDateTime("0001-01-01 01:01:01");
        public Date FechaCertificacion;
        public Date FechaEfectivaMovimiento;
        public String IdMoneda = "";
        public BigDecimal Saldo;
        public String NombreAgenteCliente = "";
        public String DocumentoIdentidadAgente = "";
        public Long IdEjecutivo = 0L;
        public String NombreEjecutivoMovimiento = "";
        public String DocumentoIdentidadEjecutivo = "";
        public BigDecimal SaldoRetenido;
        public BigDecimal MoneyMarket;
        public BigDecimal Garantias;
        public BigDecimal Simultanea;
        public BigDecimal Pacto;
    }

    public static class Configuracion {
        public String RutaBase;
        public String RutaCta;
        public String RutaArchivo;
    }

    /** Row of the work table (CTA balance query). */
    public static class CuentaProceso {
        public Long IdTrabajo;
        public Long IdEjecutivo;
        public Long IdPortafolio;
        public String IdMoneda;
    }

    public static class Saldo {
        public Long IdCliente;
        public Long IdPortafolio;
        public String IdMoneda;
        public BigDecimal Monto;
        public String FechaSaldo;
    }

    public static class InstrumentoMoneyMarket {
        public String Nemotecnico;
        public Long IdInstrumento;
    }

    public static class CantidadCustodia {
        public Long IdCliente;
        public Long IdPortafolio;
        public String IdMoneda;
        public BigDecimal CantidadCustodia;
        public String Nemotecnico;
    }

    public static class ValorCuota {
        public String IdSerieFondo;
        public BigDecimal ValorCuota;
        public String FechaValorCuota;
    }
}
