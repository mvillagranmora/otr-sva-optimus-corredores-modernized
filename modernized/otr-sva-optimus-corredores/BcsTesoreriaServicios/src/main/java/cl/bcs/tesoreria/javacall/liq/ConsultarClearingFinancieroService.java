package cl.bcs.tesoreria.javacall.liq;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonObject;

import cl.bcs.tesoreria.javacall.kit.JsonLegacy;
import cl.bcs.tesoreria.javacall.kit.MensajeSalida;
import cl.bcs.tesoreria.javacall.kit.Plataforma;
import cl.bcs.tesoreria.javacall.kit.SqlNombrado;
import cl.bcs.tesoreria.javacall.liq.ClearingFinanciero.AnticiposSimultaneas;
import cl.bcs.tesoreria.javacall.liq.ClearingFinanciero.ConfiguracionTesoreria;
import cl.bcs.tesoreria.javacall.liq.ClearingFinanciero.DatosCorredor;
import cl.bcs.tesoreria.javacall.liq.ClearingFinanciero.Detalle;
import cl.bcs.tesoreria.javacall.liq.ClearingFinanciero.Entrada;
import cl.bcs.tesoreria.javacall.liq.ClearingFinanciero.Liquidacion;
import cl.bcs.tesoreria.javacall.liq.ClearingFinanciero.Resumen;
import cl.bcs.tesoreria.javacall.liq.ClearingFinanciero.Salida;
import cl.bcs.tesoreria.javacall.liq.ClearingFinanciero.SimultaneasDetalle;
import cl.bcs.tesoreria.javacall.liq.ClearingFinanciero.TransaccionDividendo;
import cl.bcs.tesoreria.port.out.ConfiguracionSistema;
import cl.bcs.utils.Fecha;

/**
 * Legacy {@code cl.bcs.tesoreria.liq.ConsultarClearingFinanciero} (TES-B-23..46): groups the day's market transactions, dividends, derivative liquidations
 * and simultaneous-operation advances by clearing chamber (RV, PM, PH, SM, DV, HS) and counterparty, and builds the per-counterparty summary.
 *
 * <p>LEGACY DEFECT kept (see README TES-B-23): dividends with CAMBIOSCCLV=N are named 'RV + PM' but carry group code 2 (the 'PH + SM' code).
 * LEGACY DEFECT kept (see README TES-B-45): a transaction without AccionTipoOperacion (or without condition/class where the rule dereferences it) throws a
 * NullPointerException; only SQLExceptions are turned into a message.
 * The summary comes out of a {@link HashMap} (iteration order = String hash order); the order is an implementation detail of the legacy that
 * ProcesarClearingFinanciero inherits (it assigns the chamber references in that order), so the same map type and key text are kept.
 */
@Service
public class ConsultarClearingFinancieroService {

    private static final String SELECT_DATOS_CORREDOR = SqlNombrado.recurso("ConsultarClearingFinanciero_ConsultarDatosCorredor");
    private static final String SELECT_CONFIGURACION_TESORERIA = SqlNombrado.recurso("ConsultarClearingFinanciero_ConsultarConfiguracionTesoreria");
    private static final String SELECT_TRANSACCIONES_MERCADO = SqlNombrado.recurso("ConsultarClearingFinanciero_ConsultarTransaccionesDeMercados");
    private static final String SELECT_DIVIDENDOS = SqlNombrado.recurso("ConsultarClearingFinanciero_ConsultarDividendos");
    private static final String SELECT_LIQUIDACIONES = SqlNombrado.recurso("ConsultarClearingFinanciero_ConsultarLiquidaciones");
    private static final String SELECT_ANTICIPOS = SqlNombrado.recurso("ConsultarClearingFinanciero_ConsultarAnticiposSimultaneas");
    private static final String SELECT_ANTICIPOS_HISTORICO = SqlNombrado.recurso("ConsultarClearingFinanciero_ConsultarAnticiposSimultaneasHistorico");

    /** Chamber group a transaction is reported under (name and numeric code). */
    private record Grupo(String nombre, Long codigo) {
    }

    private static final Grupo GRUPO_DV = new Grupo("DV", 4L);
    private static final Grupo GRUPO_PH_SM = new Grupo("PH + SM", 2L);
    private static final Grupo GRUPO_RV = new Grupo("RV", 3L);
    private static final Grupo GRUPO_RV_PM = new Grupo("RV + PM", 1L);
    private static final Grupo GRUPO_PM = new Grupo("PM", 1L);
    private static final Grupo GRUPO_HS = new Grupo("HS", 5L);

    /** Chambers requested by the caller and the TESORERIA parameters that decide which transactions each chamber includes. */
    private static final class Validadores {
        boolean camaraRV;
        boolean camaraPM;
        boolean camaraPH;
        boolean camaraSM;
        boolean camaraDV;
        boolean clearingIncluyeRFPM;
        boolean clearingIncluyeIFPM;
        boolean clearingIncluyeRFPH;
        boolean clearingIncluyeIFPH;
        boolean cambiosCCLV;
    }

    private final SqlNombrado sql;
    private final ConfiguracionSistema configuracion;

    public ConsultarClearingFinancieroService(NamedParameterJdbcTemplate jdbc, ConfiguracionSistema configuracion) {
        this.sql = new SqlNombrado(jdbc);
        this.configuracion = configuracion;
    }

    @Transactional(rollbackFor = Exception.class)
    public JsonObject execute(JsonObject entrada) throws Exception {
        var gson = JsonLegacy.gson();
        Entrada in = gson.fromJson(entrada.toString(), Entrada.class);
        return gson.toJsonTree(consultar(in)).getAsJsonObject();
    }

    /** Typed entry point (legacy {@code logica}), also used in-process by ProcesarClearingFinanciero. */
    public Salida consultar(Entrada in) {
        Salida salida = new Salida();
        try {
            Validadores validadores = new Validadores();

            Map<String, Object> paramsCorredor = new HashMap<>();
            paramsCorredor.put("p_IdEmpresa", in.p_IdEmpresa);
            String fechaSistema = "0001-01-01";
            List<DatosCorredor> datosCorredor = sql.consultar(SELECT_DATOS_CORREDOR, paramsCorredor, DatosCorredor.class);
            try {
                fechaSistema = Plataforma.fechaSistema(configuracion, in.p_IdEmpresa);
            } catch (Exception e) {
                // not configured: the default date means "no system date", which selects the current advances query
            }

            if (datosCorredor.isEmpty()) {
                salida.P_OUT_MENSAJES.add(new MensajeSalida(1L, MensajeSalida.MENSAJE_ERROR, "UI_CORREDOR_NO_EXISTE", "Codigo Corredor no existe"));
                return salida;
            }

            for (Camara cam : in.p_Camaras) {
                switch (cam.Camara) {
                    case "RV" -> validadores.camaraRV = true;
                    case "PM" -> validadores.camaraPM = true;
                    case "PH" -> validadores.camaraPH = true;
                    case "SM" -> validadores.camaraSM = true;
                    case "DV" -> validadores.camaraDV = true;
                    default -> {
                    }
                }
            }

            Map<String, Object> paramsParametros = new HashMap<>();
            paramsParametros.put("p_IdEmpresa", in.p_IdEmpresa);
            List<ConfiguracionTesoreria> configTesoreria = sql.consultar(SELECT_CONFIGURACION_TESORERIA, paramsParametros, ConfiguracionTesoreria.class);
            for (ConfiguracionTesoreria objeto : configTesoreria) {
                switch (objeto.CodigoParametro) {
                    case "CLEARINGINCLUYERFPM" -> validadores.clearingIncluyeRFPM = true;
                    case "CLEARINGINCLUYEIFPM" -> validadores.clearingIncluyeIFPM = true;
                    case "CLEARINGINCLUYERFPH" -> validadores.clearingIncluyeRFPH = true;
                    case "CLEARINGINCLUYEIFPH" -> validadores.clearingIncluyeIFPH = true;
                    case "CAMBIOSCCLV" -> validadores.cambiosCCLV = true;
                    default -> {
                    }
                }
            }

            Map<String, Object> paramsTransacciones = new HashMap<>();
            paramsTransacciones.put("p_IdEmpresa", in.p_IdEmpresa);
            paramsTransacciones.put("p_FechaLiquidacion", in.p_FechaLiquidacion);
            List<TransaccionDividendo> transaccionesDeMercado = sql.consultar(SELECT_TRANSACCIONES_MERCADO, paramsTransacciones, TransaccionDividendo.class);

            String usaCamaraHS;
            try {
                usaCamaraHS = Plataforma.parametro(configuracion, in.p_IdEmpresa, "TESORERIA", "UsaCamaraHS");
            } catch (Exception ex) {
                usaCamaraHS = "N";
            }
            boolean usaHS = "S".equalsIgnoreCase(usaCamaraHS);

            List<TransaccionDividendo> transaccionesFiltradas = new ArrayList<>();
            for (TransaccionDividendo transaccion : transaccionesDeMercado) {
                for (Grupo grupo : gruposDe(transaccion, validadores, usaHS)) {
                    transaccionesFiltradas.add(llenar(transaccion, grupo));
                }
            }

            // RV carries the dividends
            List<TransaccionDividendo> dividendos = new ArrayList<>();
            if (validadores.camaraRV) {
                Grupo grupo;
                if (validadores.cambiosCCLV) {
                    grupo = GRUPO_RV;
                } else {
                    // LEGACY DEFECT kept (see README TES-B-23): name 'RV + PM' with the 'PH + SM' code
                    grupo = new Grupo(GRUPO_RV_PM.nombre(), GRUPO_PH_SM.codigo());
                }
                Map<String, Object> paramsDividendos = new HashMap<>();
                paramsDividendos.put("v_CodigoGrupoCamara", grupo.codigo());
                paramsDividendos.put("v_NombreGrupoCamara", grupo.nombre());
                paramsDividendos.put("p_FechaLiquidacion", in.p_FechaLiquidacion);
                dividendos = sql.consultar(SELECT_DIVIDENDOS, paramsDividendos, TransaccionDividendo.class);
            }

            // details
            for (TransaccionDividendo transaccion : transaccionesFiltradas) {
                salida.P_OUT_CLEARINGFINANCIERODETALLE.add(detalleDe(transaccion));
            }

            // summary: derivative liquidations, transactions + dividends, simultaneous advances
            List<Resumen> resumenes = new ArrayList<>();

            Map<String, Object> paramsLiquidaciones = new HashMap<>();
            paramsLiquidaciones.put("p_FechaLiquidacion", in.p_FechaLiquidacion);
            paramsLiquidaciones.put("v_CodigoGrupoCamara", 4);
            paramsLiquidaciones.put("v_NombreGrupoCamara", GRUPO_DV.nombre());
            List<Liquidacion> liquidaciones = new ArrayList<>();
            if (validadores.camaraDV) {
                liquidaciones = sql.consultar(SELECT_LIQUIDACIONES, paramsLiquidaciones, Liquidacion.class);
                for (Liquidacion liquidacion : liquidaciones) {
                    resumenes.add(resumenDe(liquidacion));
                }
            }

            List<TransaccionDividendo> transaccionesYDividendos = new ArrayList<>(transaccionesFiltradas);
            transaccionesYDividendos.addAll(dividendos);
            for (TransaccionDividendo transaccion : transaccionesYDividendos) {
                resumenes.add(resumenDe(transaccion));
            }

            if (validadores.camaraSM) {
                Grupo camara = usaHS ? GRUPO_HS : GRUPO_PH_SM;
                Map<String, Object> params = new HashMap<>();
                params.put("p_IdEmpresa", in.p_IdEmpresa);
                params.put("p_FechaLiquidacion", in.p_FechaLiquidacion);
                params.put("p_CodigoGrupoCamara", camara.codigo());
                params.put("p_NombreGrupoCamara", camara.nombre());
                java.sql.Date fechaLiquidacion = Fecha.toDate(in.p_FechaLiquidacion);
                List<AnticiposSimultaneas> anticipos;
                if (fechaLiquidacion.equals(Fecha.toDate(fechaSistema)) || fechaLiquidacion.after(Fecha.toDate(fechaSistema)) || "0001-01-01".equals(fechaSistema)) {
                    anticipos = sql.consultar(SELECT_ANTICIPOS, params, AnticiposSimultaneas.class);
                } else {
                    anticipos = sql.consultar(SELECT_ANTICIPOS_HISTORICO, params, AnticiposSimultaneas.class);
                }
                for (AnticiposSimultaneas anticipo : anticipos) {
                    salida.P_OUT_CLEARINGFINANCIEROSIMULTANEAS.add(simultaneaDe(anticipo));
                    resumenes.add(resumenDe(anticipo));
                }
            }

            // summary per counterparty
            Map<String, Resumen> resumenPorLlave = new HashMap<>();
            BigDecimal totalVentaResumen = BigDecimal.ZERO;
            BigDecimal totalCompraResumen = BigDecimal.ZERO;
            for (Resumen r : resumenes) {
                String llave = r.IdBolsa + r.CodigoGrupoCamara + r.NombreGrupoCamara + r.IdClasificacionTipoInstrumento + r.IdCorredorContraparte
                        + r.RutContraparte + r.RazonSocialContraparte + r.IdMonedaTransaccion + r.IdComprobanteTesoreria;
                Resumen acumulado = resumenPorLlave.get(llave);
                if (acumulado == null) {
                    resumenPorLlave.put(llave, r);
                    acumulado = r;
                    totalVentaResumen = acumulado.TotalVenta;
                    totalCompraResumen = acumulado.TotalCompra;
                } else {
                    totalVentaResumen = acumulado.TotalVenta.add(r.TotalVenta);
                    totalCompraResumen = acumulado.TotalCompra.add(r.TotalCompra);
                    acumulado.TotalVenta = acumulado.TotalVenta.add(r.TotalVenta);
                    acumulado.TotalCompra = acumulado.TotalCompra.add(r.TotalCompra);
                    acumulado.MontoRetencion = acumulado.MontoRetencion.add(r.MontoRetencion);
                }
                if (acumulado.TotalCompra.compareTo(acumulado.TotalVenta) == 1) {
                    acumulado.SaldoDeudor = totalCompraResumen.subtract(totalVentaResumen);
                    acumulado.SaldoAcreedor = BigDecimal.ZERO;
                } else {
                    acumulado.SaldoAcreedor = totalVentaResumen.subtract(totalCompraResumen);
                    acumulado.SaldoDeudor = BigDecimal.ZERO;
                }
            }
            salida.P_OUT_CLEARINGFINANCIERORESUMEN.addAll(resumenPorLlave.values());

            salida.P_OUT_CLEARINGFINANCIERODIVIDENDOS.addAll(dividendos);
            salida.P_OUT_CLEARINGFINANCIEROLIQUIDACIONES.addAll(liquidaciones);
        } catch (SQLException e) {
            salida.P_OUT_MENSAJES.add(new MensajeSalida(1L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_AL_CONSULTAR_CLEARING", e.getMessage()));
        }
        return salida;
    }

    /**
     * Chamber groups a transaction belongs to (a transaction may match several rules and is then reported once per rule, as the legacy did).
     * The rules and the order they are evaluated in are the legacy ones, including which operand is dereferenced without a null check.
     */
    private static List<Grupo> gruposDe(TransaccionDividendo t, Validadores v, boolean usaHS) {
        List<Grupo> grupos = new ArrayList<>();
        if (usaHS) {
            // CN conditions (HS)
            if ("T2".equals(t.IdCondicionDeLiquidacion) && "PH".equals(t.IdCamara) && (!v.camaraRV && !v.camaraDV)
                    && ("RF".equals(t.IdClasificacionTipoInstrumento) || "IF".equals(t.IdClasificacionTipoInstrumento))) {
                grupos.add(GRUPO_HS);
            }
            if ("T2".equals(t.IdCondicionDeLiquidacion) && "PH".equals(t.IdCamara) && (v.camaraRV && v.camaraDV && v.camaraPH && v.camaraPM && v.camaraSM)
                    && ("RF".equals(t.IdClasificacionTipoInstrumento) || "IF".equals(t.IdClasificacionTipoInstrumento))) {
                grupos.add(GRUPO_HS);
            }
            if ("T2".equals(t.IdCondicionDeLiquidacion) && "PH".equals(t.IdCamara) && (v.camaraRV && !v.camaraDV && v.camaraPH && v.camaraPM && v.camaraSM)
                    && ("RF".equals(t.IdClasificacionTipoInstrumento) || "IF".equals(t.IdClasificacionTipoInstrumento))) {
                grupos.add(GRUPO_HS);
            }
            // PH + SM (HS)
            if ((v.camaraPH || v.camaraSM) && !"RV".equals(t.IdClasificacionTipoInstrumento)) {
                if ("T0".equals(t.IdCondicionDeLiquidacion) && ((v.clearingIncluyeRFPH && "RF".equals(t.IdClasificacionTipoInstrumento))
                        || (v.clearingIncluyeIFPH && "IF".equals(t.IdClasificacionTipoInstrumento)))) {
                    grupos.add(GRUPO_HS);
                }
            }
            // PM (HS with CambiosCCLV)
            if (v.camaraPM && !"RV".equals(t.IdClasificacionTipoInstrumento)) {
                if ("T1".equals(t.IdCondicionDeLiquidacion)
                        && ((v.clearingIncluyeRFPM && "RF".equals(t.IdClasificacionTipoInstrumento) && v.cambiosCCLV)
                                || (v.clearingIncluyeIFPM && "IF".equals(t.IdClasificacionTipoInstrumento) && v.cambiosCCLV))) {
                    grupos.add(GRUPO_HS);
                }
            }
            // PM
            if (v.camaraPM && t.IdCondicionDeLiquidacion.equals("T1")
                    && (t.IdClasificacionTipoInstrumento.equals("RF") || t.IdClasificacionTipoInstrumento.equals("IF"))) {
                if (!v.cambiosCCLV) {
                    if (v.clearingIncluyeIFPM || v.clearingIncluyeRFPM) {
                        grupos.add(GRUPO_RV_PM);
                    }
                }
            }
            // RV
            if (v.camaraRV && t.IdClasificacionTipoInstrumento.equals("RV") && t.IdCondicionDeLiquidacion.equals("T2")) {
                grupos.add(v.cambiosCCLV ? GRUPO_RV : GRUPO_RV_PM);
            }
            // SM
            if (v.camaraSM) {
                if (t.IdClasificacionTipoInstrumento.equals("RV") && t.IdCamara.equals("SM") && t.IdCondicionDeLiquidacion.equals("T0")
                        && t.IdTipoInstrumento.equals("SIM") && (t.AccionTipoOperacion.equals("COMPRA") || t.AccionTipoOperacion.equals("VENTA"))) {
                    grupos.add(GRUPO_HS);
                }
            }
            // DV: the legacy uses the non short-circuit '&', so the classification is dereferenced even without the DV chamber
            if (v.camaraDV & t.IdClasificacionTipoInstrumento.equals("DV")) {
                grupos.add(GRUPO_DV);
            }
        } else {
            // PH
            if (v.camaraPH && t.IdCondicionDeLiquidacion.equals("T0")) {
                if (v.clearingIncluyeRFPH && t.IdClasificacionTipoInstrumento.equals("RF")) {
                    grupos.add(GRUPO_PH_SM);
                }
            }
            if (v.camaraPH && t.IdCondicionDeLiquidacion.equals("T0")) {
                if (v.clearingIncluyeIFPH && t.IdClasificacionTipoInstrumento.equals("IF")) {
                    grupos.add(GRUPO_PH_SM);
                }
            }
            // PM
            if (v.camaraPM && t.IdCondicionDeLiquidacion.equals("T1")
                    && (t.IdClasificacionTipoInstrumento.equals("RF") || t.IdClasificacionTipoInstrumento.equals("IF"))) {
                Grupo grupo = v.cambiosCCLV ? GRUPO_PM : GRUPO_RV_PM;
                if (v.clearingIncluyeIFPM || v.clearingIncluyeRFPM) {
                    grupos.add(grupo);
                }
            }
            // RV
            if (v.camaraRV && t.IdClasificacionTipoInstrumento.equals("RV") && t.IdCondicionDeLiquidacion.equals("T2")) {
                grupos.add(v.cambiosCCLV ? GRUPO_RV : GRUPO_RV_PM);
            }
            // SM
            if (v.camaraSM) {
                if (t.IdClasificacionTipoInstrumento.equals("RV") && t.IdCamara.equals("SM") && t.IdCondicionDeLiquidacion.equals("T0")
                        && t.IdTipoInstrumento.equals("SIM") && (t.AccionTipoOperacion.equals("COMPRA") || t.AccionTipoOperacion.equals("VENTA"))) {
                    grupos.add(GRUPO_PH_SM);
                }
            }
            // DV (non short-circuit '&', as above)
            if (v.camaraDV & t.IdClasificacionTipoInstrumento.equals("DV")) {
                grupos.add(GRUPO_DV);
            }
        }
        return grupos;
    }

    /** Legacy {@code LlenarTransaccionDividendo}: copy of the transaction under a chamber group, sales/purchases split by action. */
    private static TransaccionDividendo llenar(TransaccionDividendo t, Grupo grupo) {
        TransaccionDividendo d = new TransaccionDividendo();
        d.IdTransaccion = t.IdTransaccion;
        d.IdClasificacionTipoInstrumento = t.IdClasificacionTipoInstrumento;
        d.IdBolsa = t.IdBolsa;
        d.CodigoGrupoCamara = grupo.codigo();
        d.NombreGrupoCamara = grupo.nombre();
        d.RutContraparte = t.RutContraparte;
        d.CodigoOperadorContraparte = t.CodigoOperadorContraparte;
        d.IdCorredorContraparte = t.IdCorredorContraparte;
        d.AccionTipoOperacion = t.AccionTipoOperacion;
        d.TotalVenta = t.AccionTipoOperacion.equals("VENTA") ? t.MontoTransaccion : BigDecimal.ZERO;
        d.TotalCompra = t.AccionTipoOperacion.equals("COMPRA") ? t.MontoTransaccion : BigDecimal.ZERO;
        d.RazonSocialContraparte = t.RazonSocialContraparte;
        d.FolioTransaccion = t.FolioTransaccion;
        d.IdCondicionDeLiquidacion = t.IdCondicionDeLiquidacion;
        d.CodigoCondicionDeLiquidacion = t.CodigoCondicionDeLiquidacion;
        d.FechaSistemaCreacion = t.FechaSistemaCreacion;
        d.IdComprobanteTesoreria = t.IdComprobanteTesoreria;
        d.PrecioTransaccion = t.PrecioTransaccion;
        d.Tasa = t.Tasa;
        d.CantidadTransaccion = t.CantidadTransaccion;
        d.IdMonedaTransaccion = t.IdMonedaTransaccion != null ? t.IdMonedaTransaccion : t.IdMoneda;
        d.MontoRetencion = t.MontoRetencion;
        d.Instrumento = t.Instrumento;
        d.IdEventoCapital = t.IdEventoCapital;
        d.IdEventoCapitalCliente = t.IdEventoCapitalCliente;
        return d;
    }

    private static Detalle detalleDe(TransaccionDividendo t) {
        Detalle d = new Detalle();
        d.IdTransaccion = t.IdTransaccion;
        d.IdBolsa = t.IdBolsa;
        d.CodigoGrupoCamara = t.CodigoGrupoCamara;
        d.NombreGrupoCamara = t.NombreGrupoCamara;
        d.RutContraparte = t.RutContraparte;
        d.AccionTipoOperacion = t.AccionTipoOperacion;
        if ("COMPRA".equals(t.AccionTipoOperacion)) {
            d.CompraVenta = "C";
        } else if ("VENTA".equals(t.AccionTipoOperacion)) {
            d.CompraVenta = "V";
        }
        d.TotalVenta = t.TotalVenta;
        d.TotalCompra = t.TotalCompra;
        d.RazonSocialContraparte = t.RazonSocialContraparte;
        d.IdClasificacionTipoInstrumento = t.IdClasificacionTipoInstrumento;
        d.FechaSistemaCreacion = t.FechaSistemaCreacion;
        d.FolioTransaccion = t.FolioTransaccion;
        d.IdCondicionDeLiquidacion = t.IdCondicionDeLiquidacion;
        d.CodigoCondicionDeLiquidacion = t.CodigoCondicionDeLiquidacion;
        d.IdMonedaTransaccion = t.IdMonedaTransaccion != null ? t.IdMonedaTransaccion : t.IdMoneda;
        d.IdComprobanteTesoreria = t.IdComprobanteTesoreria;
        d.Instrumento = t.Instrumento;
        d.CantidadTransaccion = t.CantidadTransaccion;
        d.PrecioTransaccion = t.PrecioTransaccion;
        d.Tasa = t.Tasa;
        d.MontoRetencion = t.MontoRetencion;
        d.IdEventoCapital = t.IdEventoCapital;
        d.IdEventoCapitalCliente = t.IdEventoCapitalCliente;
        return d;
    }

    private static Resumen resumenDe(Liquidacion l) {
        Resumen r = new Resumen();
        r.IdBolsa = l.IdBolsa;
        r.CodigoGrupoCamara = l.CodigoGrupoCamara;
        r.NombreGrupoCamara = l.NombreGrupoCamara;
        r.IdClasificacionTipoInstrumento = l.IdClasificacionTipoInstrumento;
        r.TotalCompra = l.TotalCompra;
        r.TotalVenta = l.TotalVenta;
        r.IdCorredorContraparte = l.IdCorredorContraparte;
        r.RutContraparte = l.RutContraparte;
        r.IdMonedaTransaccion = l.IdMonedaTransaccion;
        r.RazonSocialContraparte = l.RazonSocialContraparte;
        r.SaldoAcreedor = l.SaldoAcreedor;
        r.SaldoDeudor = l.SaldoDeudor;
        r.IdComprobanteTesoreria = l.IdComprobanteTesoreria;
        r.MontoRetencion = l.MontoRetencion;
        return r;
    }

    private static Resumen resumenDe(TransaccionDividendo t) {
        Resumen r = new Resumen();
        r.IdBolsa = t.IdBolsa;
        r.CodigoGrupoCamara = t.CodigoGrupoCamara;
        r.NombreGrupoCamara = t.NombreGrupoCamara;
        r.IdClasificacionTipoInstrumento = t.IdClasificacionTipoInstrumento;
        r.IdCorredorContraparte = t.IdCorredorContraparte;
        r.RutContraparte = t.RutContraparte;
        r.RazonSocialContraparte = t.RazonSocialContraparte;
        r.IdMonedaTransaccion = t.IdMonedaTransaccion;
        r.IdComprobanteTesoreria = t.IdComprobanteTesoreria;
        r.TotalVenta = t.TotalVenta;
        r.TotalCompra = t.TotalCompra;
        r.SaldoAcreedor = t.SaldoAcreedor;
        r.SaldoDeudor = t.SaldoDeudor;
        r.MontoRetencion = t.MontoRetencion;
        return r;
    }

    private static Resumen resumenDe(AnticiposSimultaneas a) {
        Resumen r = new Resumen();
        r.IdBolsa = a.IdBolsa;
        r.CodigoGrupoCamara = a.CodigoGrupoCamara;
        r.NombreGrupoCamara = a.NombreGrupoCamara;
        r.IdClasificacionTipoInstrumento = a.IdClasificacionTipoInstrumento;
        r.IdCorredorContraparte = a.IdCorredorContraparte;
        r.RutContraparte = a.RutContraparte;
        r.RazonSocialContraparte = a.RazonSocialContraparte;
        r.IdMonedaTransaccion = a.IdMoneda;
        r.IdComprobanteTesoreria = a.IdComprobanteTesoreria;
        r.TotalVenta = a.AccionOperacion.equals("VENTA") ? a.Monto : BigDecimal.ZERO;
        r.TotalCompra = a.AccionOperacion.equals("COMPRA") ? a.Monto : BigDecimal.ZERO;
        r.SaldoAcreedor = BigDecimal.ZERO;
        r.SaldoDeudor = BigDecimal.ZERO;
        r.MontoRetencion = BigDecimal.ZERO;
        return r;
    }

    private static SimultaneasDetalle simultaneaDe(AnticiposSimultaneas a) {
        SimultaneasDetalle d = new SimultaneasDetalle();
        d.TipoOperacion = a.TipoOperacion;
        d.FechaLiquidacion = a.FechaLiquidacion;
        d.Nemotecnico = a.Nemotecnico;
        d.Cantidad = a.Cantidad;
        d.MontoCompra = a.AccionOperacion.equals("COMPRA") ? a.Monto : BigDecimal.ZERO;
        d.MontoVenta = a.AccionOperacion.equals("VENTA") ? a.Monto : BigDecimal.ZERO;
        d.NombreGrupoCamara = a.NombreGrupoCamara;
        d.RutContraparte = a.RutContraparte;
        d.IdClasificacionTipoInstrumento = a.IdClasificacionTipoInstrumento;
        d.IdComprobanteTesoreria = a.IdComprobanteTesoreria;
        d.IdBolsa = a.IdBolsa;
        return d;
    }
}
