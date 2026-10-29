package cl.bcs.cuentainversion.service;

import cl.bcs.cuentainversion.port.out.ColaboradoresReportes;
import cl.bcs.cuentainversion.reportes.ConfiguracionSistema;
import cl.bcs.cuentainversion.reportes.JsonReportes;
import cl.bcs.cuentainversion.reportes.MensajeSalida;
import cl.bcs.cuentainversion.reportes.Mensajeria;
import cl.bcs.cuentainversion.reportes.SqlReportes;
import cl.bcs.utils.Fecha;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Attribution analysis report (legacy {@code InformeAtributtionAnalisys}), behaviours CTA-B-213..223.
 *
 * <p>For the requested clients / portfolios and period it reads the custody portfolio classified by market / sub-market, prices the
 * equity (RV) rows through {@code EntregarPreciosTasas}, converts every amount to the requested currency through
 * {@code CalcularParidad}, and answers (in {@code P_OUT_SALIDA}): {@code datosSocioNegocio} (as the collaborator answered),
 * {@code Atributtion} (market / sub-market / detail rows with value, profit, portfolio percentages, profitability and contribution),
 * {@code RentabilidadClasificacion} and {@code RentabilidadIndices} (index profitability per classification, "ND" when the tree has
 * no index).
 *
 * <p>The arithmetic (scales, rounding, which accumulator survives which loop) is the legacy one and the goldens pin it; the
 * accumulators that the legacy declared outside the loops live in {@link Calculo} for the same reason. Collaborators: ConsultarEmpresa,
 * DatosCartolaSocioNegocio, MovimientosTitulos, MovimientosCaja (one request each), EntregarPreciosTasas (one request) and one
 * CalcularParidad request per amount to convert, all through {@link ColaboradoresReportes}; collaborator requests are serialized
 * with the default Gson (dates as {@code "MMM d, yyyy"}), like the legacy call log.
 */
@Service
public class InformeAttributionAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(InformeAttributionAnalysisService.class);

    private static final String SQL_CLASIFICACION = SqlReportes.recurso("InformeAtributtionAnalysis_Select_RelacionClasificacion");
    private static final String SQL_ARBOL = SqlReportes.recurso("InformeAtributtionAnalysis_Select_ArbolSinPadre");
    private static final String SECUENCIA_PRECIOS = "BSCL.Plataforma.BD.Secuencias::TAS.IdRegistroEntregarPreciosTasas";
    private static final String RENTA_VARIABLE = "RV";
    private static final BigDecimal CIEN = new BigDecimal(100);

    private final SqlReportes sql;
    private final NamedParameterJdbcTemplate jdbc;
    private final ColaboradoresReportes colaboradores;
    private final Gson gsonColaboradores = new GsonBuilder().serializeNulls().create();

    public InformeAttributionAnalysisService(NamedParameterJdbcTemplate jdbc, ColaboradoresReportes colaboradores) {
        this.jdbc = jdbc;
        this.sql = new SqlReportes(jdbc);
        this.colaboradores = colaboradores;
    }

    @Transactional(rollbackFor = Exception.class)
    public JsonObject execute(JsonObject params) throws Exception {
        return JsonReportes.escribir(generar(JsonReportes.leer(params, Entrada.class)));
    }

    Salida generar(Entrada in) throws Exception {
        log.info("INICIO::InformeAttributionAnalysis");
        Salida salida = new Salida();
        // outside the try block: an invalid date or an unknown company leaves no message list (README CTA-B-217/220)
        LocalDate desde = LocalDate.parse(in.p_FechaDesde);
        Date primeroDeMes = java.sql.Date.valueOf(desde.withDayOfMonth(1));
        JsonReportes.empresa(colaboradores, in.p_IdEmpresa);

        try {
            salida.P_OUT_SALIDA.putAll(new Calculo(in, primeroDeMes).ejecutar());
        } catch (Exception e) {
            // LEGACY DEFECT kept (see README CTA-B-214/219): the error message is built into a local list that is thrown away, the exception is rethrown.
            Mensajeria mensajes = new Mensajeria(sql, 1L);
            List<MensajeSalida> descartados = new ArrayList<>();
            descartados.add(mensajes.error("UI_OPERACION_TIENE_ERRORES", Map.of()));
            salida.P_OUT_ERRORESCONTADOR = 1L;
            throw e;
        }
        return salida;
    }

    // =============================================================================================================
    // the report
    // =============================================================================================================

    /** One run of the report: the accumulators the legacy kept outside its loops are fields. */
    private final class Calculo {
        private final Entrada in;
        private final Date primeroDeMes;
        private final SimpleDateFormat formatoIso = new SimpleDateFormat("yyyy-MM-dd");
        private final SimpleDateFormat formatoMovimiento = new SimpleDateFormat("dd-MM-yyyy");

        BigDecimal montoAcumulado = BigDecimal.ZERO;
        BigDecimal totalPatrimonioDesde = BigDecimal.ZERO;
        BigDecimal totalPatrimonioHasta = BigDecimal.ZERO;
        BigDecimal utilidad = BigDecimal.ZERO;
        BigDecimal sumaUtilidad = BigDecimal.ZERO;
        BigDecimal sumaUtilidadContribucion = BigDecimal.ZERO;
        BigDecimal sumaUtilidadSub = BigDecimal.ZERO;
        BigDecimal sumaContribucionSub = BigDecimal.ZERO;
        BigDecimal montoDiaDesde = BigDecimal.ZERO;
        BigDecimal monto = BigDecimal.ZERO;
        BigDecimal montoInicial = BigDecimal.ZERO;
        BigDecimal montoVolatil = BigDecimal.ZERO;
        BigDecimal valorPortafolioSubCabecera = BigDecimal.ZERO;
        BigDecimal porcentajePortafolioDesdeSub = BigDecimal.ZERO;
        BigDecimal sumaPorcentajePortafolioHasta = BigDecimal.ZERO;
        BigDecimal sumaPorcentajePortafolioDesde = BigDecimal.ZERO;
        BigDecimal sumaRentabilidad = BigDecimal.ZERO;
        BigDecimal sumaRentabilidadSub = BigDecimal.ZERO;
        BigDecimal sumaPonderacionTitulosAbonos = BigDecimal.ZERO;
        BigDecimal sumaPonderacionTitulosCargos = BigDecimal.ZERO;
        BigDecimal sumaPonderacionCajaAbonos = BigDecimal.ZERO;
        BigDecimal sumaPonderacionCajaCargos = BigDecimal.ZERO;

        List<JsonObject> titulos;
        List<JsonObject> caja;
        Map<String, PrecioTasa> precios = new HashMap<>();

        Calculo(Entrada in, Date primeroDeMes) {
            this.in = in;
            this.primeroDeMes = primeroDeMes;
        }

        Map<String, Object> ejecutar() throws Exception {
            Map<String, Object> resultado = new HashMap<>();

            EntradaCartola cartola = new EntradaCartola();
            cartola.p_IdEmpresa = in.p_IdEmpresa;
            cartola.p_IdModulo = in.p_IdModulo;
            cartola.p_IdUsuario = in.p_IdUsuario;
            cartola.p_FechaDesde = Fecha.toDate(in.p_FechaDesde);
            cartola.p_FechaHasta = Fecha.toDate(in.p_FechaHasta);
            for (Cliente c : in.p_Datos) {
                Dato d = new Dato();
                d.IdCliente = c.IdCliente;
                d.IdPortafolio = c.IdPortafolio;
                cartola.p_Datos.add(d);
            }
            JsonObject entradaCartola = gsonColaboradores.toJsonTree(cartola).getAsJsonObject();
            resultado.put("datosSocioNegocio", colaboradores.datosSocioNegocio(entradaCartola));
            titulos = colaboradores.movimientosTitulos(entradaCartola);
            caja = colaboradores.movimientosCaja(entradaCartola);

            List<Long> portafolios = new ArrayList<>();
            List<Long> clientes = new ArrayList<>();
            for (Cliente c : in.p_Datos) {
                clientes.add(c.IdCliente);
                portafolios.add(c.IdPortafolio);
            }
            Map<String, Object> p = new HashMap<>();
            p.put("p_FechaDesde", in.p_FechaDesde);
            p.put("p_FechaHasta", in.p_FechaHasta);
            p.put("p_IdPortafolio", portafolios);
            p.put("p_IdCliente", clientes);
            p.put("p_FechaAnterior", primeroDeMes);
            List<Posicion> arbol = sql.consultar(SQL_CLASIFICACION, p, Posicion.class);

            cargarPrecios(arbol);

            List<Fila> filas = new ArrayList<>();
            List<FilaClasificacion> clasificacion = new ArrayList<>();
            recorrerMercados(arbol, filas, clasificacion);

            List<Fila> atributtion = repasarPorcentajes(filas);
            resultado.put("RentabilidadClasificacion", clasificacion);
            resultado.put("Atributtion", atributtion);
            resultado.put("RentabilidadIndices", rentabilidadIndices(clasificacion));
            return resultado;
        }

        // ------------------------------------------------------------------------------------ prices of the RV rows

        private void cargarPrecios(List<Posicion> arbol) throws Exception {
            List<Posicion> rv = arbol.stream().filter(c -> "RV".equalsIgnoreCase(c.IdClasificacionTipoInstrumento)).collect(Collectors.toList());
            List<Long> secuencias = sql.secuencia(SECUENCIA_PRECIOS, Long.valueOf(rv.size() * 2));

            EntradaPrecios entrada = new EntradaPrecios();
            entrada.p_IdEmpresa = in.p_IdEmpresa;
            entrada.p_IdUsuario = in.p_IdUsuario;
            entrada.p_IdModulo = in.p_IdModulo;
            entrada.p_IdPais = new ConfiguracionSistema(sql).entero(in.p_IdEmpresa, ConfiguracionSistema.PLATAFORMA, "PLA.PaisSistema");
            int i = 0;
            // first the rows of the end date, then those of the start date (the sequence ids follow that order)
            for (String fecha : new String[]{in.p_FechaHasta, in.p_FechaDesde}) {
                for (Posicion car : rv) {
                    InstrumentoPrecio ins = new InstrumentoPrecio();
                    ins.IdRegistro = secuencias.get(i++);
                    ins.TipoCartera = "0";
                    ins.IdUso = "CARTERA";
                    ins.IdTipoCriterioTasa = "0";
                    ins.IdClasificacionTipoInstrumento = car.IdClasificacionTipoInstrumento;
                    ins.IdTipoInstrumento = car.IdTipoInstrumento;
                    ins.IdInstrumento = car.IdInstrumento;
                    ins.Nemotecnico = car.Nemotecnico;
                    ins.IdMoneda = car.MonedaReajuste;
                    ins.FechaConsulta = Fecha.toDate(fecha);
                    ins.PlazoResidual = 0L;
                    entrada.p_DatosInstrumentos.add(ins);
                }
            }
            JsonObject respuesta = colaboradores.entregarPreciosTasas(gsonColaboradores.toJsonTree(entrada).getAsJsonObject());
            JsonArray lista = respuesta.getAsJsonArray("P_OUT_PRECIOSTASAS");
            for (JsonElement e : lista) {
                JsonObject o = e.getAsJsonObject();
                PrecioTasa dato = new PrecioTasa(texto(o, "Nemotecnico"), texto(o, "FechaFuente"), numero(o, "Precio"));
                precios.putIfAbsent(dato.nemotecnico() + "_" + dato.fechaFuente().substring(0, 10), dato);
            }
        }

        // ------------------------------------------------------------------------------------ markets / sub-markets / instruments

        private void recorrerMercados(List<Posicion> arbol, List<Fila> salidaFilas, List<FilaClasificacion> salidaClasificacion) throws Exception {
            Map<String, Map<String, List<Posicion>>> mercados = arbol.stream()
                    .collect(Collectors.groupingBy(a -> a.Mercado, Collectors.groupingBy(a -> a.SubMercado)));

            for (String mercado : mercados.keySet()) {
                if (!"".equals(mercado)) {
                    sumaPorcentajePortafolioHasta = BigDecimal.ZERO;
                    sumaRentabilidad = BigDecimal.ZERO;
                    sumaPorcentajePortafolioDesde = BigDecimal.ZERO;
                    montoAcumulado = BigDecimal.ZERO;
                    sumaUtilidad = BigDecimal.ZERO;
                    sumaUtilidadContribucion = BigDecimal.ZERO;
                }

                Fila cabecera = Fila.cabecera(mercado, "mercado");
                salidaFilas.add(cabecera);
                Fila cabeceraSub = null;
                FilaClasificacion clasificacionMercado = new FilaClasificacion(mercado);
                salidaClasificacion.add(clasificacionMercado);
                FilaClasificacion clasificacionSub = null;

                for (String subMercado : mercados.get(mercado).keySet()) {
                    // LEGACY DEFECT kept (see README CTA-B-222): reference comparison with "" (true only for an interned empty string, driver dependent).
                    if (!(subMercado == "")) {
                        clasificacionSub = new FilaClasificacion(subMercado);
                        salidaClasificacion.add(clasificacionSub);
                        cabeceraSub = Fila.cabecera(subMercado, "submercado");
                        salidaFilas.add(cabeceraSub);
                    }

                    Map<String, List<Posicion>> porNemo = mercados.get(mercado).get(subMercado).stream()
                            .collect(Collectors.groupingBy(a -> a.Nemotecnico));
                    for (Map.Entry<String, List<Posicion>> entry : porNemo.entrySet()) {
                        instrumento(entry.getKey(), entry.getValue(), cabecera, cabeceraSub, salidaFilas);
                    }
                }

                cabecera.porcentajePortafoliodesde = sumaPorcentajePortafolioDesde;
                cabecera.porcentajePortafoliohasta = sumaPorcentajePortafolioHasta;
                cabecera.rentabilidadMonedaReajuste = sumaRentabilidad;
                if (cabeceraSub != null) {
                    cabeceraSub.porcentajePortafoliodesde = porcentajePortafolioDesdeSub;
                    cabeceraSub.porcentajePortafoliohasta = BigDecimal.ZERO;
                    cabeceraSub.contribucionalPortafolio = sumaContribucionSub;
                    cabeceraSub.rentabilidadMonedaReajuste = sumaRentabilidadSub;
                    cabeceraSub.utilidadPerdida = sumaUtilidadSub;
                }
                montoVolatil = BigDecimal.ZERO;

                cabecera.ValorInstrumento = montoAcumulado;
                cabecera.utilidadPerdida = sumaUtilidad;
                cabecera.contribucionalPortafolio = sumaUtilidadContribucion;

                clasificacionMercado.MonedaReajuste = sumaRentabilidad;
                if (clasificacionSub != null) {
                    clasificacionSub.MonedaReajuste = sumaRentabilidadSub;
                }
            }
        }

        /** The rows of one instrument (nemotecnico): values, profit, profitability and contribution; appends its detail row. */
        private void instrumento(String nemotecnico, List<Posicion> lista, Fila cabecera, Fila cabeceraSub, List<Fila> salidaFilas) throws Exception {
            Utilidad utilidadPerdida = new Utilidad();
            boolean haySub = cabeceraSub != null;

            for (Posicion d : lista) {
                if (d.IdClasificacionTipoInstrumento.equals("RV")) {
                    precioRentaVariable(d);
                }
                d.ValorInstrumento = d.ValorInstrumento == null ? BigDecimal.ZERO : d.ValorInstrumento;
                d.MontoVenc = d.MontoVenc == null ? BigDecimal.ZERO : d.MontoVenc;
                if (!d.MonedaReajuste.equals(in.p_Moneda)) {
                    d.ValorInstrumento = convertir(in.p_FechaHasta, d.MonedaReajuste, in.p_Moneda, d.ValorInstrumento);
                    d.MontoVenc = convertir(d.INGFechaOperacion.toString(), d.MonedaReajuste, in.p_Moneda, d.MontoVenc);
                    d.MontoCup = convertir(d.INGFechaOperacion.toString(), d.MonedaReajuste, in.p_Moneda, d.MontoCup);
                    d.MontoDiv = convertir(d.INGFechaOperacion.toString(), d.MonedaReajuste, in.p_Moneda, d.MontoDiv);
                }
                d.ValorInstrumento = d.ValorInstrumento == null ? BigDecimal.ZERO : d.ValorInstrumento;
                d.MontoVenc = d.MontoVenc == null ? BigDecimal.ZERO : d.MontoVenc;

                boolean operacionNormal = d.EsSimultanea.equals("N") && d.EsVentaCorta.equals("N") && d.EsAnticipo.equals("N") && d.EsVencimiento.equals("N");
                if ((d.INGAccion.equals("COMPRA") || d.EGRAccion.equals("COMPRA")) && operacionNormal) {
                    utilidadPerdida.Compras = utilidadPerdida.Compras.add(d.ValorInstrumento, MathContext.DECIMAL64);
                }
                if ((d.INGAccion.equals("VENTA") || d.EGRAccion.equals("VENTA")) && operacionNormal) {
                    BigDecimal valor = d.INGAccion.equals("COMPRA") && d.EGRAccion.equals("VENTA") ? d.ValorInstrumentoEGR : d.ValorInstrumento;
                    utilidadPerdida.Ventas = utilidadPerdida.Ventas.add(valor, MathContext.DECIMAL64);
                }
                if (d.EsVencimiento.equals("S")) {
                    utilidadPerdida.Vencimientos = utilidadPerdida.Vencimientos.add(d.ValorInstrumento, MathContext.DECIMAL64);
                }
                utilidadPerdida.Cupones = d.MontoCup;
                utilidadPerdida.Dividendos = d.MontoDiv;
                d.MontoCup = d.MontoCup == null ? BigDecimal.ZERO : d.MontoCup;

                if (d.IdEstado.equalsIgnoreCase("VIG")
                        || (!d.IdEstado.equalsIgnoreCase("VIG") && d.EGRFechaLiquidacion.toString().compareTo(in.p_FechaHasta) > 0)) {
                    monto = monto.add(d.ValorInstrumentoEGR, MathContext.DECIMAL64);
                }
                if (d.FechaOperacion.toString().equals(in.p_FechaDesde)) {
                    montoInicial = montoInicial.add(d.ValorInstrumento);
                }
                if (haySub) {
                    montoVolatil = montoVolatil.add(d.ValorInstrumento, MathContext.DECIMAL64).setScale(2, RoundingMode.FLOOR);
                }
            }

            // most recent operation first (stable for equal dates)
            lista = lista.stream().sorted((m1, m2) -> m1.INGFechaOperacion.compareTo(m2.INGFechaOperacion) == 0 ? 0
                    : m2.INGFechaOperacion.compareTo(m1.INGFechaOperacion) == 1 ? 1 : -1).collect(Collectors.toList());
            Posicion primera = lista.get(0);
            Posicion ultima = lista.get(lista.size() - 1);
            Date fechaDesde = formatoIso.parse(in.p_FechaDesde);
            Date fechaHasta = formatoIso.parse(in.p_FechaHasta);

            BigDecimal montoInicialCar = BigDecimal.ZERO;
            BigDecimal precioInicial = BigDecimal.ZERO;
            BigDecimal rentabilidad = BigDecimal.ZERO;
            BigDecimal montoFinalCar = ultima.ValorInstrumentoEGR;
            BigDecimal precioFinal = ultima.EGRPrecio;
            if (!primera.IdClasificacionTipoInstrumento.equals(RENTA_VARIABLE)) {
                if (primera.FechaOperacion.compareTo(fechaDesde) == 0) {
                    montoInicialCar = primera.ValorInstrumento;
                    precioInicial = primera.INGPrecio;
                }
                if (precioInicial.compareTo(BigDecimal.ZERO) != 0) {
                    BigDecimal p1 = precioFinal.subtract(precioInicial);
                    BigDecimal p2 = p1.subtract(utilidadPerdida.Compras);
                    BigDecimal p3 = p2.add(utilidadPerdida.Ventas).add(utilidadPerdida.Cupones).add(utilidadPerdida.Vencimientos);
                    rentabilidad = p3.divide(precioInicial, RoundingMode.CEILING).multiply(CIEN).setScale(2, RoundingMode.HALF_UP);
                    if (haySub) {
                        cabeceraSub.utilidadPerdida = utilidad.setScale(2, RoundingMode.FLOOR);
                    }
                }
            } else {
                if (primera.FechaOperacion.compareTo(fechaDesde) == 0) {
                    montoInicialCar = primera.ValorInstrumento;
                    precioInicial = primera.INGPrecio;
                }
                if (fechaDesde.after(primera.FechaOperacion)) {
                    // bought before the period: the close of the previous month is the starting price
                    precioInicial = primera.MontoCierreAnterior;
                    if (precioInicial.compareTo(BigDecimal.ZERO) == 0) {
                        precioInicial = BigDecimal.ONE;
                    }
                    rentabilidad = precioFinal.subtract(precioInicial).divide(precioInicial, RoundingMode.CEILING).multiply(CIEN);
                } else if ((fechaDesde.before(primera.FechaOperacion) && fechaHasta.after(primera.FechaOperacion))
                        || (fechaDesde.equals(primera.FechaOperacion) || fechaHasta.equals(primera.FechaOperacion))) {
                    if (precioInicial.compareTo(BigDecimal.ZERO) != 0) {
                        rentabilidad = precioFinal.subtract(precioInicial).divide(precioInicial, RoundingMode.CEILING).multiply(CIEN).setScale(2, RoundingMode.FLOOR);
                    } else {
                        rentabilidad = BigDecimal.ZERO;
                    }
                }
            }

            // Both branches of the legacy "if" computed the same profit; only the second one replaced null amounts by zero first.
            if (!primera.ValorInstrumento.toString().equals(ultima.ValorInstrumento.toString()) && !haySub) {
                utilidad = utilidadDelPeriodo(montoFinalCar, montoInicialCar, utilidadPerdida);
            } else {
                utilidadPerdida.completarNulos();
                utilidad = utilidadDelPeriodo(montoFinalCar, montoInicialCar, utilidadPerdida);
                if (haySub) {
                    cabeceraSub.utilidadPerdida = utilidad.setScale(2, RoundingMode.FLOOR);
                }
            }

            BigDecimal precioDiaDesde = primera.INGPrecio;
            BigDecimal precioDiaHasta = ultima.EGRPrecio;
            montoDiaDesde = montoDiaDesde.add(precioDiaDesde).setScale(2, RoundingMode.UP);

            BigDecimal porcentajeHasta = monto.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO
                    : precioDiaHasta.divide(monto, MathContext.DECIMAL64).setScale(2, RoundingMode.FLOOR);
            if (haySub) {
                valorPortafolioSubCabecera = cabeceraSub.ValorInstrumento;
                cabeceraSub.ValorInstrumento = valorPortafolioSubCabecera.add(montoVolatil, MathContext.DECIMAL64);
                cabeceraSub.porcentajePortafoliodesde = valorPortafolioSubCabecera.add(BigDecimal.ZERO, MathContext.DECIMAL64).setScale(2, RoundingMode.FLOOR);
                cabeceraSub.porcentajePortafoliohasta = valorPortafolioSubCabecera.add(porcentajeHasta, MathContext.DECIMAL64).setScale(2, RoundingMode.FLOOR);
            }

            ponderarTitulos(primera, fechaHasta, fechaDesde);
            BigDecimal montoCaja = ponderarCaja(primera, fechaHasta, fechaDesde);

            montoAcumulado = montoAcumulado.add(monto);
            totalPatrimonioHasta = totalPatrimonioHasta.add(monto);
            if (primera.FechaOperacion.toString().compareTo(in.p_FechaDesde) <= 0) {
                totalPatrimonioDesde = totalPatrimonioDesde.add(montoInicial);
            }

            BigDecimal ponderacionAbonos = sumaPonderacionTitulosAbonos.add(sumaPonderacionCajaAbonos);
            BigDecimal ponderacionCargos = sumaPonderacionTitulosCargos.add(sumaPonderacionCajaCargos);
            BigDecimal dividendo = montoInicial.add(ponderacionAbonos).subtract(ponderacionCargos);
            BigDecimal contribucion = dividendo.compareTo(BigDecimal.ZERO) != 0
                    ? utilidad.divide(dividendo, 2, RoundingMode.HALF_UP).multiply(CIEN) : BigDecimal.ZERO;

            BigDecimal porcentajeDesde = BigDecimal.ZERO;
            if (primera.FechaOperacion.toString().compareTo(in.p_FechaDesde) <= 0) {
                porcentajeDesde = primera.ValorInstrumento;
                sumaPorcentajePortafolioDesde = sumaPorcentajePortafolioDesde.add(porcentajeDesde);
            }
            sumaRentabilidad = sumaRentabilidad.add(rentabilidad);
            if (haySub) {
                porcentajePortafolioDesdeSub = porcentajePortafolioDesdeSub.add(porcentajeDesde);
                sumaRentabilidadSub = sumaRentabilidadSub.add(rentabilidad);
                sumaUtilidadSub = sumaUtilidadSub.add(utilidad);
                sumaContribucionSub = sumaContribucionSub.add(contribucion);
            }

            salidaFilas.add(Fila.detalle(nemotecnico, lista.get(0).MonedaReajuste, monto, utilidad,
                    porcentajeDesde.setScale(2, RoundingMode.FLOOR), rentabilidad, contribucion));

            monto = BigDecimal.ZERO;
            sumaUtilidad = sumaUtilidad.add(utilidad);
            sumaUtilidadContribucion = sumaUtilidadContribucion.add(contribucion);
            utilidad = BigDecimal.ZERO;
        }

        private BigDecimal utilidadDelPeriodo(BigDecimal montoFinal, BigDecimal montoInicial, Utilidad u) {
            return montoFinal.subtract(montoInicial).subtract(u.Compras).add(u.Ventas).add(u.Cupones).add(u.Dividendos)
                    .add(u.Vencimientos).setScale(2, RoundingMode.FLOOR);
        }

        /** Price of the RV row on both dates; a missing price answer is a NullPointerException (README CTA-B-214). */
        private void precioRentaVariable(Posicion d) {
            // LEGACY DEFECT kept (see README CTA-B-214): no price answer for the instrument -> NullPointerException.
            PrecioTasa desde = precios.get(d.Nemotecnico + "_" + in.p_FechaDesde);
            int comparacion = desde.fechaFuente().substring(0, 10).compareTo(d.FechaOperacion.toString());
            if (desde.precio() != null && comparacion == 0) {
                d.INGPrecio = desde.precio();
                d.ValorInstrumento = d.INGCantidad.multiply(desde.precio());
            }
            PrecioTasa hasta = precios.get(d.Nemotecnico + "_" + in.p_FechaHasta);
            if (hasta.precio() != null) {
                d.EGRPrecio = "VIG".equalsIgnoreCase(d.IdEstado) ? hasta.precio() : BigDecimal.ZERO;
                d.ValorInstrumentoEGR = d.INGCantidad.multiply(d.EGRPrecio);
            }
        }

        // ------------------------------------------------------------------------------------ weighted movements

        private long dias(Date mayor, Date menor) {
            return (mayor.getTime() - menor.getTime()) / (1000 * 60 * 60 * 24);
        }

        private BigDecimal ponderacion(BigDecimal monto, long diasA, BigDecimal diasB) {
            return monto.multiply(new BigDecimal(diasA)).divide(diasB, RoundingMode.CEILING).setScale(2, RoundingMode.FLOOR);
        }

        private BigDecimal diferenciaDiasB(Date hasta, Date desde) {
            BigDecimal b = new BigDecimal(dias(hasta, desde));
            return b.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ONE : b;
        }

        /** Securities movements of the instrument weighted by the days they were in the portfolio (contributions / withdrawals). */
        private void ponderarTitulos(Posicion primera, Date fechaHasta, Date fechaDesde) throws Exception {
            BigDecimal diasB = diferenciaDiasB(fechaHasta, fechaDesde);
            for (JsonObject item : titulos) {
                if (texto(item, "NombreTipoOperacion") == null || texto(item, "TipoConcepto") == null || texto(item, "Nemotecnico") == null) {
                    continue;
                }
                if (!texto(item, "Nemotecnico").equals(primera.Nemotecnico)) {
                    continue;
                }
                String accion = texto(item, "Accion");
                if (accion.equals("INGRESO")) {
                    BigDecimal montoTitulos = numero(item, "MontoCompraMonedaInstrumento");
                    if (!texto(item, "IdMonedaInstrumento").equals(in.p_Moneda)) {
                        montoTitulos = convertir(texto(item, "FechaOperacion"), texto(item, "IdMonedaInstrumento"), in.p_Moneda, numero(item, "MontoMercadoMonedaLocal"));
                    }
                    Date fechaMovimiento = formatoMovimiento.parse(texto(item, "FechaOperacion"));
                    BigDecimal ponderacion = montoTitulos == null ? BigDecimal.ZERO : ponderacion(montoTitulos, dias(fechaHasta, fechaMovimiento), diasB);
                    sumaPonderacionTitulosAbonos = sumaPonderacionTitulosAbonos.add(ponderacion);
                } else if (accion.equals("RETIRO")) {
                    BigDecimal montoTitulos = numero(item, "MontoMercadoMonedaLocal");
                    if (!texto(item, "IdMonedaInstrumento").equals(in.p_Moneda)) {
                        montoTitulos = convertir(texto(item, "FechaOperacion"), texto(item, "IdMonedaInstrumento"), in.p_Moneda, numero(item, "MontoMercadoMonedaLocal"));
                    }
                    Date fechaMovimiento = formatoMovimiento.parse(texto(item, "FechaOperacion"));
                    BigDecimal ponderacion = montoTitulos == null ? BigDecimal.ZERO : ponderacion(montoTitulos, dias(fechaHasta, fechaMovimiento), diasB);
                    sumaPonderacionTitulosCargos = sumaPonderacionTitulosCargos.add(ponderacion);
                }
            }
        }

        /**
         * Cash movements of the instrument that affect profitability, weighted by days. The running cash amount is NOT reset between
         * movements (the legacy weights the accumulated amount each time); returns it.
         */
        private BigDecimal ponderarCaja(Posicion primera, Date fechaHasta, Date fechaDesde) throws Exception {
            BigDecimal diasB = diferenciaDiasB(fechaHasta, fechaDesde);
            BigDecimal montoCaja = BigDecimal.ZERO;
            for (JsonObject item : caja) {
                if (primera.Nemotecnico.equals("") || texto(item, "Moneda") == null) {
                    continue;
                }
                if (!texto(item, "Nemotecnico").equals(primera.Nemotecnico)) {
                    continue;
                }
                boolean afecta = texto(item, "AfectaRentabilidad").equals("S");
                String tipo = texto(item, "NombreTipoMovimiento");
                if (afecta && tipo.equals("ABONO")) {
                    montoCaja = montoCaja.add(montoCaja(item, "Abono"));
                    Date fechaMovimiento = formatoMovimiento.parse(texto(item, "FechaOperacion"));
                    sumaPonderacionCajaAbonos = sumaPonderacionCajaAbonos.add(ponderacion(montoCaja, dias(fechaHasta, fechaMovimiento), diasB));
                } else if (afecta && tipo.equals("CARGO")) {
                    montoCaja = montoCaja.add(montoCaja(item, "Cargo"));
                    Date fechaMovimiento = formatoMovimiento.parse(texto(item, "FechaOperacion"));
                    sumaPonderacionCajaCargos = sumaPonderacionCajaCargos.add(ponderacion(montoCaja, dias(fechaHasta, fechaMovimiento), diasB));
                }
            }
            return montoCaja;
        }

        private BigDecimal montoCaja(JsonObject item, String campo) throws Exception {
            if (texto(item, "Moneda").equals(in.p_Moneda)) {
                return numero(item, campo);
            }
            return convertir(texto(item, "FechaOperacion"), texto(item, "Moneda"), in.p_Moneda, numero(item, campo));
        }

        // ------------------------------------------------------------------------------------ percentages and indices

        private List<Fila> repasarPorcentajes(List<Fila> filas) {
            List<Fila> repaso = new ArrayList<>();
            for (Fila f : filas) {
                Fila r = new Fila();
                r.nemotecnico = f.nemotecnico;
                r.MonedaReajuste = f.MonedaReajuste;
                r.ValorInstrumento = f.ValorInstrumento;
                r.utilidadPerdida = f.utilidadPerdida;
                r.porcentajePortafoliodesde = totalPatrimonioDesde.compareTo(BigDecimal.ZERO) != 0
                        ? porcentaje(f.porcentajePortafoliodesde, totalPatrimonioDesde) : BigDecimal.ZERO;
                r.porcentajePortafoliohasta = totalPatrimonioHasta.compareTo(BigDecimal.ZERO) != 0
                        ? porcentaje(f.ValorInstrumento, totalPatrimonioHasta) : BigDecimal.ZERO;
                r.rentabilidadMonedaReajuste = f.rentabilidadMonedaReajuste;
                r.contribucionalPortafolio = f.contribucionalPortafolio;
                r.TipoFila = f.TipoFila;
                repaso.add(r);
            }
            return repaso;
        }

        private BigDecimal porcentaje(BigDecimal parte, BigDecimal total) {
            return new BigDecimal(parte.toString()).divide(total, MathContext.DECIMAL64).multiply(new BigDecimal(100L)).setScale(2, RoundingMode.FLOOR);
        }

        private List<IndiceNivel> rentabilidadIndices(List<FilaClasificacion> clasificacion) {
            List<IndiceRentabilidadAttributionAnalysisService.Clasificacion> sinPadre = sql.consultar(SQL_ARBOL, Map.of(),
                    IndiceRentabilidadAttributionAnalysisService.Clasificacion.class);
            IndiceRentabilidadAttributionAnalysisService.Entrada entrada = new IndiceRentabilidadAttributionAnalysisService.Entrada();
            entrada.p_FechaDesde = in.p_FechaDesde;
            entrada.p_FechaHasta = in.p_FechaHasta;
            entrada.p_clasificacion = sinPadre;
            List<IndiceRentabilidadAttributionAnalysisService.Indice> indices =
                    new IndiceRentabilidadAttributionAnalysisService(jdbc).procesar(entrada).P_OUT_SALIDA.get("rentabilidadindice");

            Map<String, BigDecimal> rentabilidadPorNombre = new HashMap<>();
            Map<String, String> indicePorNombre = new HashMap<>();
            for (IndiceRentabilidadAttributionAnalysisService.Indice i : indices) {
                rentabilidadPorNombre.put(i.Descripcion, i.rentabilidadIndice);
                indicePorNombre.put(i.Descripcion, i.Indice);
            }
            List<IndiceNivel> resultado = new ArrayList<>();
            for (FilaClasificacion fila : clasificacion) {
                IndiceNivel nivel = new IndiceNivel();
                nivel.Descripcion = fila.nemotecnico;
                BigDecimal rentabilidad = rentabilidadPorNombre.get(fila.nemotecnico);
                String indice = indicePorNombre.get(fila.nemotecnico);
                if (rentabilidad != null && indice != null) {
                    nivel.Indice = indice;
                    nivel.rentabilidadIndice = rentabilidad;
                } else {
                    nivel.Indice = "ND";
                    nivel.rentabilidadIndice = BigDecimal.ZERO;
                }
                resultado.add(nivel);
            }
            return resultado;
        }
    }

    // =============================================================================================================
    // currency conversion
    // =============================================================================================================

    /**
     * Converts one amount through CalcularParidad (company 1, one calculation per request; the date text is re-formatted with the
     * legacy lenient {@code yyyy-MM-dd} parser). A missing rate answers {@code null} (README CTA-B-219).
     */
    private BigDecimal convertir(String fechaMovimiento, String monedaOrigen, String monedaDestino, BigDecimal monto) throws Exception {
        SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");
        String fecha = formato.format(formato.parse(fechaMovimiento));
        JsonObject calculo = new JsonObject();
        calculo.addProperty("IdTrabajo", 0L);
        calculo.addProperty("IdInterno", 0L);
        calculo.addProperty("Fecha", fecha);
        calculo.addProperty("IdMonedaOrigen", monedaOrigen);
        calculo.addProperty("IdMonedaDestino", monedaDestino);
        calculo.addProperty("ValorCambioLocalModificado", BigDecimal.ZERO);
        calculo.addProperty("IdUso", "");
        calculo.addProperty("MontoOrigen", monto);
        JsonArray calculos = new JsonArray();
        calculos.add(calculo);
        JsonObject entrada = new JsonObject();
        entrada.addProperty("p_IdEmpresa", 1L);
        entrada.addProperty("p_IdTrabajo", 0L);
        entrada.add("p_CalculosEntrada", calculos);
        JsonElement destino = colaboradores.calcularParidad(entrada).getAsJsonArray("p_out_CalculosSalida").get(0).getAsJsonObject().get("MontoDestino");
        return destino == null || destino.isJsonNull() ? null : destino.getAsBigDecimal();
    }

    private static String texto(JsonObject o, String campo) {
        JsonElement e = o.get(campo);
        return e == null || e.isJsonNull() ? null : e.getAsString();
    }

    private static BigDecimal numero(JsonObject o, String campo) {
        JsonElement e = o.get(campo);
        return e == null || e.isJsonNull() ? null : e.getAsBigDecimal();
    }

    // =============================================================================================================
    // JSON contract and working types
    // =============================================================================================================

    public static class Entrada {
        public Long p_IdEmpresa;
        public String p_IdModulo;
        public Long p_IdUsuario;
        public String p_FechaDesde;
        public String p_FechaHasta;
        public List<Cliente> p_Datos;
        public String p_Moneda;
    }

    public static class Cliente {
        public Long IdCliente;
        public Long IdPortafolio;
    }

    public static class Salida {
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public Map<String, Object> P_OUT_SALIDA = new HashMap<>();
    }

    /** Row of the attribution table (market, sub-market or detail row); also the shape of the answer. */
    public static class Fila {
        public String nemotecnico;
        public String MonedaReajuste;
        public BigDecimal ValorInstrumento;
        public BigDecimal utilidadPerdida;
        public BigDecimal porcentajePortafoliodesde;
        public BigDecimal porcentajePortafoliohasta;
        public BigDecimal rentabilidadMonedaReajuste;
        public BigDecimal contribucionalPortafolio;
        public String TipoFila;

        static Fila cabecera(String nombre, String tipo) {
            Fila f = new Fila();
            f.nemotecnico = nombre;
            f.MonedaReajuste = "";
            f.ValorInstrumento = BigDecimal.ZERO;
            f.utilidadPerdida = BigDecimal.ZERO;
            f.porcentajePortafoliodesde = BigDecimal.ZERO;
            f.porcentajePortafoliohasta = BigDecimal.ZERO;
            f.rentabilidadMonedaReajuste = BigDecimal.ZERO;
            f.contribucionalPortafolio = BigDecimal.ZERO;
            f.TipoFila = tipo;
            return f;
        }

        static Fila detalle(String nemotecnico, String moneda, BigDecimal valor, BigDecimal utilidad, BigDecimal desde,
                            BigDecimal rentabilidad, BigDecimal contribucion) {
            Fila f = new Fila();
            f.nemotecnico = nemotecnico;
            f.MonedaReajuste = moneda;
            f.ValorInstrumento = valor;
            f.utilidadPerdida = utilidad;
            f.porcentajePortafoliodesde = desde;
            f.porcentajePortafoliohasta = BigDecimal.ZERO;
            f.rentabilidadMonedaReajuste = rentabilidad;
            f.contribucionalPortafolio = contribucion;
            f.TipoFila = "detalle";
            return f;
        }
    }

    /** Row of the profitability-by-level table: name and the sum of the profitability under it. */
    public static class FilaClasificacion {
        public String nemotecnico;
        public BigDecimal MonedaReajuste = BigDecimal.ZERO;

        FilaClasificacion(String nombre) {
            this.nemotecnico = nombre;
        }
    }

    public static class IndiceNivel {
        public String Indice;
        public BigDecimal rentabilidadIndice;
        public String Descripcion;
    }

    /** Position row of the classified custody portfolio. */
    public static class Posicion {
        public Long IdPortafolio;
        public Long IdCliente;
        public Date INGFechaOperacion;
        public Date FechaOperacion;
        public String MonedaReajuste;
        public String INGAccion;
        public String EGRAccion;
        public String Nemotecnico;
        public BigDecimal MontoDiv;
        public BigDecimal MontoCup;
        public BigDecimal MontoVenc;
        public BigDecimal MontoCompra;
        public String IdClasificacionTipoInstrumento;
        public BigDecimal ValorInstrumento;
        public String Mercado;
        public String SubMercado;
        public String EsSimultanea;
        public String EsVentaCorta;
        public String EsAnticipo;
        public String EsVencimiento;
        public String IdTipoInstrumento;
        public Long IdInstrumento;
        public Date FechaVencimientoOperacion;
        public BigDecimal MontoCierreAnterior;
        public BigDecimal INGCantidad;
        public BigDecimal ValorInstrumentoEGR;
        public BigDecimal INGPrecio = BigDecimal.ZERO;
        public BigDecimal EGRPrecio = BigDecimal.ZERO;
        public String IdEstado;
        public Date EGRFechaLiquidacion;
    }

    private static final class Utilidad {
        BigDecimal Compras = BigDecimal.ZERO;
        BigDecimal Ventas = BigDecimal.ZERO;
        BigDecimal Cupones = BigDecimal.ZERO;
        BigDecimal Dividendos = BigDecimal.ZERO;
        BigDecimal Vencimientos = BigDecimal.ZERO;

        void completarNulos() {
            Compras = Compras == null ? BigDecimal.ZERO : Compras;
            Ventas = Ventas == null ? BigDecimal.ZERO : Ventas;
            Cupones = Cupones == null ? BigDecimal.ZERO : Cupones;
            Dividendos = Dividendos == null ? BigDecimal.ZERO : Dividendos;
            Vencimientos = Vencimientos == null ? BigDecimal.ZERO : Vencimientos;
        }
    }

    private record PrecioTasa(String nemotecnico, String fechaFuente, BigDecimal precio) {
    }

    // ---- requests of the cartola collaborators and of EntregarPreciosTasas (JSON contract of the legacy POJOs)

    static class EntradaCartola {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public Date p_FechaDesde;
        public Date p_FechaHasta;
        public Date p_FechaHastaOrigen;
        public String p_AgrupaMovimientos = "N";
        public String p_FiltroSoloMonedaSistema = "S";
        public String p_FiltroUbicacionBoveda = "";
        public String p_Moneda;
        public String p_IdTipoCanal;
        public List<Dato> p_Datos = new ArrayList<>();
        public Long p_IdCliente;
        public Long p_IdTrabajo = 0L;
    }

    static class Dato {
        public Long IdCliente;
        public Long IdPortafolio;
    }

    static class EntradaPrecios {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public Long p_IdPais;
        public List<InstrumentoPrecio> p_DatosInstrumentos = new ArrayList<>();
    }

    static class InstrumentoPrecio {
        public Long IdRegistro;
        public String TipoCartera;
        public String IdUso;
        public String IdTipoCriterioTasa;
        public String IdClasificacionTipoInstrumento;
        public String IdTipoInstrumento;
        public Long IdInstrumento;
        public String Nemotecnico;
        public String IdMoneda;
        public java.sql.Date FechaConsulta;
        public Long PlazoResidual;
        public String NemoSVS;
    }
}
