package cl.bcs.tesoreria.javacall.car;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.tesoreria.javacall.kit.JsonLegacy;
import cl.bcs.tesoreria.javacall.kit.MensajeSalida;
import cl.bcs.tesoreria.javacall.kit.Plataforma;
import cl.bcs.tesoreria.javacall.kit.SqlNombrado;
import cl.bcs.tesoreria.javacall.kit.colaborador.CrearComprobante;
import cl.bcs.tesoreria.javacall.kit.colaborador.CrearIngresoEgreso;
import cl.bcs.tesoreria.javacall.kit.colaborador.CrearIngresoEgreso.IngresoEgresoGenerado_Salida;
import cl.bcs.tesoreria.javacall.kit.colaborador.RequisitosBasicos;
import cl.bcs.tesoreria.port.out.Colaboradores;
import cl.bcs.tesoreria.port.out.DiccionarioMensajes;
import cl.bcs.utils.Utils;

/**
 * Legacy {@code cl.bcs.tesoreria.car.ProcesarCargaMasiva} (TES-B-160..177): processes loaded mass files (collections, generic movements, BCP
 * reconciliation): validates the loads, asks CrearIngresoEgreso for one movement per detail line (with the payment preference of the load), groups the
 * movements into entry/exit "partidas" (CrearComprobante) for the load types that generate one, and marks the loads and details as processed.
 *
 * <p>The work table {@code GBL.ProcesarCargaMasiva_Proceso} stages the requested loads and is read back by every statement of the call.
 */
@Service
public class ProcesarCargaMasivaService {

    private static final Logger log = LoggerFactory.getLogger(ProcesarCargaMasivaService.class);

    private static final String SQL_INSERT_CARGA_MASIVA_PROCESO = SqlNombrado.recurso("ProcesarCargaMasiva_InsertCargaMasivaProceso");
    private static final String SQL_SELECT_CARGA_MASIVA_PROCESO = SqlNombrado.recurso("ProcesarCargaMasiva_SelectCargaMasivaProceso");
    private static final String SQL_SELECT_PREFERENCIA_PAGO_CARGA = SqlNombrado.recurso("ProcesarCargaMasiva_SelectPreferenciaPagoCarga");
    private static final String SQL_SELECT_CARGA_MASIVA_DETALLE = SqlNombrado.recurso("ProcesarCargaMasiva_SelectCargaMasivaDetalle");
    private static final String SQL_UPDATE_CARGA_MASIVA = SqlNombrado.recurso("ProcesarCargaMasiva_UpdateCargaMasiva");
    private static final String SQL_UPDATE_CARGA_MASIVA_DETALLE = SqlNombrado.recurso("ProcesarCargaMasiva_UpdateCargaMasivaDetalle");

    // ---- JSON contract (legacy names)

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_UsaReglasNegocio;
        /** System date. */
        public String p_FechaProceso;
        public List<CargaMasivaProcesar> p_CargaMasivaProcesar = new ArrayList<>();
    }

    /** Requested load; also the parameter object of the staging INSERT. */
    public static class CargaMasivaProcesar {
        public Long IdCargaMasiva;
        public String CodigoTipoCargaMasiva;
        public String IdMoneda;
        public String IdConcepto;
        public String IdMedioDePago;
        public Long IdCuentaBanco;
    }

    public static class Salida {
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
        public Long P_OUT_ERRORESCONTADOR;
    }

    // ---- statement rows / parameter objects

    /** Load being processed; also the parameter object of the UPDATE of CAR.CargaMasiva. */
    public static class CargaMasivaProceso {
        public Long IdCargaMasiva;
        public String CodigoTipoCargaMasiva;
        public String IdMoneda;
        public String IdConcepto;
        public String IdMedioDePago;
        public Long IdCuentaBanco;
        public Long IdCargaMasiva_Real;
        public String CodigoTipoCargaMasiva_Real;
        public String IdEstado;
        public String FechaProceso;
        public long errores = 0;
    }

    public static class PreferenciaPagoCarga {
        public Long IdCargaMasiva;
        public String CodigoTipoCargaMasiva;
        public Long IdPersonaEntidadRecaudadora;
        public String NombreCompletoEntidadRecaudadora;
        public String DocumentoIdentidadEntidadRecaudadora;
        public String TipoDocumentoIdentidadEntidadRecaudadora;
        public String IdMoneda;
        public String IdConcepto;
        public String IdMedioDePago;
        public String IdModalidadDePago;
        public Long IdCuentaBanco;
        public String ProfundidadProceso;
        /** Whether an entry/exit "partida" is generated. */
        public String GeneraPartida;
    }

    /** Detail line of a load; also the parameter object of the UPDATE of CAR.CargaMasivaDetalle. */
    public static class CargaMasivaDetalle {
        public Long IdCargaMasiva;
        public Long IdCargaMasivaDetalle;
        public String CodigoTipoCargaMasiva;
        public String IdTipoMovimiento;
        public String DocumentoIdentidad;
        public BigDecimal Monto;
        public Long IdPersona;
        public Long IdPortafolio;
        public String Glosa;
        public Long IdIngresoEgreso = 0L;
        // data of the Peruvian "CONCILIACION_RECAUDACION_BCP" load
        public String BCP_CON_FechaTransaccion;
        public Long BCP_CON_NumeroOperacion;
        public String ExisteMovimiento;
        public Long IdMovimientoExistente;
    }

    public static class PartidaIngresoEgreso {
        public Long IdPartidaReferencial = 0L;
        public Long IdCargaMasiva;
        public String IdTipoMovimiento;
        public String IdMoneda;
        public Long IdIngresoEgreso;
        public String IdMedioDePago;
        public String IdModalidadDePago;
        public BigDecimal Monto;
        public Long IdCuentaBanco;
    }

    /** Grouping key of the partidas (load, movement type, currency, payment medium, account). */
    private record ClavePartida(Long idCargaMasiva, String idTipoMovimiento, String idMoneda, String idMedioDePago, Long idCuentaBanco) {
        static ClavePartida de(PartidaIngresoEgreso p) {
            return new ClavePartida(p.IdCargaMasiva, p.IdTipoMovimiento, p.IdMoneda, p.IdMedioDePago, p.IdCuentaBanco);
        }
    }

    private final SqlNombrado sql;
    private final DiccionarioMensajes diccionario;
    private final Colaboradores colaboradores;

    public ProcesarCargaMasivaService(NamedParameterJdbcTemplate jdbc, DiccionarioMensajes diccionario, Colaboradores colaboradores) {
        this.sql = new SqlNombrado(jdbc);
        this.diccionario = diccionario;
        this.colaboradores = colaboradores;
    }

    @Transactional(rollbackFor = Exception.class)
    public JsonObject execute(JsonObject jin) throws Exception {
        Gson gson = JsonLegacy.gson();
        Entrada in = gson.fromJson(jin.toString(), Entrada.class);
        Salida out = logica(in);
        out.P_OUT_ERRORESCONTADOR = MensajeSalida.contarErrores(out.P_OUT_MENSAJES);
        return JsonParser.parseString(gson.toJson(out)).getAsJsonObject();
    }

    Salida logica(Entrada in) throws Exception {
        log.debug("== INICIO PROCESAR CARGA MASIVA ==");
        Salida out = new Salida();
        boolean usaReglas = "S".equalsIgnoreCase(in.p_UsaReglasNegocio);

        if (usaReglas) {
            RequisitosBasicos.Salida basicos = RequisitosBasicos.revisar(colaboradores, in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
            if (MensajeSalida.contarErrores(basicos.p_out_Mensajes) > 0) {
                out.P_OUT_MENSAJES.addAll(basicos.p_out_Mensajes);
                return out;
            }
            if (in.p_CargaMasivaProcesar.isEmpty()) {
                out.P_OUT_MENSAJES.add(Plataforma.mensaje(diccionario, in.p_IdEmpresa, 0L, MensajeSalida.MENSAJE_ERROR, "NO_SE_INGRESARON_CARGAS", null));
                return out;
            }
        }

        sql.lote(SQL_INSERT_CARGA_MASIVA_PROCESO, in.p_CargaMasivaProcesar);

        // LEGACY DEFECT kept (see README TES-B-173): the parameter is overwritten in the loop, only the LAST requested load is validated and queried
        // (an empty request leaves it unbound = NULL)
        Map<String, Object> params = new HashMap<>();
        params.put("IdCargaMasiva", null);
        for (CargaMasivaProcesar carga : in.p_CargaMasivaProcesar) {
            params.put("IdCargaMasiva", carga.IdCargaMasiva);
        }
        List<CargaMasivaProceso> cargas = sql.consultar(SQL_SELECT_CARGA_MASIVA_PROCESO, params, CargaMasivaProceso.class);

        params.put("IdEmpresa", in.p_IdEmpresa);
        List<PreferenciaPagoCarga> preferencias = sql.consultar(SQL_SELECT_PREFERENCIA_PAGO_CARGA, params, PreferenciaPagoCarga.class);
        Map<Long, Object> preferenciaPorCarga = Utils.List2Hash(preferencias, "IdCargaMasiva");

        if (usaReglas) {
            for (CargaMasivaProceso carga : cargas) {
                PreferenciaPagoCarga pref = (PreferenciaPagoCarga) preferenciaPorCarga.get(carga.IdCargaMasiva);
                requisito(in, carga.IdCargaMasiva_Real.compareTo(0L) == 0, "CARGA_MASIVA_NO_EXISTE", carga, out);
                requisito(in, carga.CodigoTipoCargaMasiva_Real.equalsIgnoreCase(""), "TIPO_CARGA_MASIVA_NO_EXISTE", carga, out);
                requisito(in, "PRO".equalsIgnoreCase(carga.IdEstado), "CARGA_MASIVA_PROCESADA", carga, out);
                requisito(in, "INV".equalsIgnoreCase(carga.IdEstado), "CARGA_MASIVA_INVALIDA", carga, out);
                String tipoReal = carga.CodigoTipoCargaMasiva_Real;
                if (tipoReal.equalsIgnoreCase("CARGA_RECAUDACION_SERVIPAG") || tipoReal.equalsIgnoreCase("CONCILIACION_RECAUDACION_BCP")
                        || tipoReal.equalsIgnoreCase("EGRESOS_POR_ABONOS_BCP_PAC")) {
                    requisito(in, pref.CodigoTipoCargaMasiva.equalsIgnoreCase(""), "TIPO_CARGA_MASIVA_SIN_CONFIGURACION", carga, out);
                    requisito(in, tipoReal.equalsIgnoreCase("CARGA_RECAUDACION_SERVIPAG") && pref.IdPersonaEntidadRecaudadora.compareTo(0L) == 0,
                            "CONFIGURACION_TIPO_CARGA_MASIVA_INVALIDA", carga, out);
                    requisito(in, pref.IdConcepto.equalsIgnoreCase(""), "CONFIGURACION_TIPO_CARGA_MASIVA_INVALIDA", carga, out);
                    requisito(in, pref.IdCuentaBanco.compareTo(0L) == 0, "CONFIGURACION_TIPO_CARGA_MASIVA_INVALIDA", carga, out);
                    requisito(in, pref.IdMedioDePago.equalsIgnoreCase(""), "CONFIGURACION_TIPO_CARGA_MASIVA_INVALIDA", carga, out);
                }
            }
            cargas.removeIf(c -> c.errores > 0);
            if (cargas.isEmpty()) {
                out.P_OUT_MENSAJES.add(Plataforma.mensaje(diccionario, in.p_IdEmpresa, 0L, MensajeSalida.MENSAJE_ERROR, "CARGAS_MASIVAS_CON_ERRORES", null));
                return out;
            }
        }

        // loads without validation errors, without repeats
        List<Long> idsCargaOk = new ArrayList<>();
        for (CargaMasivaProceso carga : cargas) {
            if (!idsCargaOk.contains(carga.IdCargaMasiva)) {
                idsCargaOk.add(carga.IdCargaMasiva);
            }
        }

        params.clear();
        params.put("IdCargaMasiva", idsCargaOk);
        List<CargaMasivaDetalle> detalles = sql.consultar(SQL_SELECT_CARGA_MASIVA_DETALLE, params, CargaMasivaDetalle.class);

        // ---- movements (one per detail line)
        Map<Long, Object> generados = new HashMap<>();
        CrearIngresoEgreso.CrearIngresoEgreso_Entrada pedido = new CrearIngresoEgreso.CrearIngresoEgreso_Entrada();
        List<CrearIngresoEgreso.IngresoEgreso_Entrada> movimientos = new ArrayList<>();
        List<CrearIngresoEgreso.FormaDePago_Entrada> formasDePago = new ArrayList<>();
        for (CargaMasivaDetalle detalle : detalles) {
            PreferenciaPagoCarga pref = (PreferenciaPagoCarga) preferenciaPorCarga.get(detalle.IdCargaMasiva);
            if (detalle.ExisteMovimiento.equalsIgnoreCase("N") && !pref.IdMoneda.equalsIgnoreCase("")) {
                movimientos.add(movimiento(in, detalle, pref));
                if (!pref.IdMedioDePago.equalsIgnoreCase("")) {
                    formasDePago.add(formaDePago(detalle, pref));
                }
            }
        }
        if (!movimientos.isEmpty()) {
            pedido.p_IdEmpresa = in.p_IdEmpresa;
            pedido.p_IdUsuario = in.p_IdUsuario;
            pedido.p_IdModulo = in.p_IdModulo;
            pedido.p_SimularOperacion = "N";
            pedido.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
            pedido.p_EsIngresoAutomatico = "S";
            pedido.p_FechaMovimiento = in.p_FechaProceso;
            pedido.p_IngresoEgresoGenerar = movimientos;
            pedido.p_FormaDePago = formasDePago;
            CrearIngresoEgreso.CrearIngresoEgreso_Salida salida = CrearIngresoEgreso.invocar(colaboradores, pedido);
            out.P_OUT_MENSAJES.addAll(salida.P_OUT_MENSAJES);
            out.P_OUT_ERRORESCONTADOR = MensajeSalida.contarErrores(out.P_OUT_MENSAJES);
            if (out.P_OUT_ERRORESCONTADOR.compareTo(0L) > 0) {
                out.P_OUT_MENSAJES.add(Plataforma.mensaje(diccionario, in.p_IdEmpresa, 0L, MensajeSalida.MENSAJE_ERROR, "ERROR_AL_GENERAR_MOVIMIENTO_TESORERIA", null));
                return out;
            }
            generados = Utils.List2Hash(salida.P_OUT_INGRESOEGRESOGENERADO, "NumeroReferencia");
        }

        // ---- partidas (only for the load types whose configuration asks for them)
        List<PartidaIngresoEgreso> partidasDetalle = new ArrayList<>();
        for (CargaMasivaDetalle detalle : detalles) {
            PreferenciaPagoCarga pref = (PreferenciaPagoCarga) preferenciaPorCarga.get(detalle.IdCargaMasiva);
            IngresoEgresoGenerado_Salida generado = (IngresoEgresoGenerado_Salida) generados.get(detalle.IdCargaMasivaDetalle);
            if (pref.GeneraPartida.equalsIgnoreCase("S") && generado != null) {
                PartidaIngresoEgreso p = new PartidaIngresoEgreso();
                p.IdCargaMasiva = detalle.IdCargaMasiva;
                p.IdTipoMovimiento = detalle.IdTipoMovimiento;
                p.IdMoneda = pref.IdMoneda;
                p.IdIngresoEgreso = generado.IdIngresoEgreso;
                p.IdMedioDePago = pref.IdMedioDePago;
                p.IdModalidadDePago = pref.IdModalidadDePago;
                p.Monto = detalle.Monto;
                p.IdCuentaBanco = pref.IdCuentaBanco;
                partidasDetalle.add(p);
            }
        }
        if (!partidasDetalle.isEmpty()) {
            List<PartidaIngresoEgreso> partidas = new ArrayList<>();
            Map<ClavePartida, PartidaIngresoEgreso> porClave = new HashMap<>();
            for (PartidaIngresoEgreso detalle : partidasDetalle) {
                PartidaIngresoEgreso existente = porClave.get(ClavePartida.de(detalle));
                if (existente == null) {
                    partidas.add(detalle);
                    porClave.put(ClavePartida.de(detalle), detalle);
                } else {
                    existente.Monto = existente.Monto.add(detalle.Monto);
                }
            }
            long referencia = 1L;
            for (PartidaIngresoEgreso partida : partidas) {
                partida.IdPartidaReferencial = referencia;
                partida.Monto = porClave.get(ClavePartida.de(partida)).Monto;
                referencia++;
            }
            CrearComprobante.CrearComprobante_Entrada comprobante = new CrearComprobante.CrearComprobante_Entrada();
            List<CrearComprobante.IngresoEgresoGenerar_Entrada> movimientosDePartida = new ArrayList<>();
            List<CrearComprobante.FormaDePago_Entrada> formasDePartida = new ArrayList<>();
            for (PartidaIngresoEgreso detalle : partidasDetalle) {
                PartidaIngresoEgreso partida = porClave.get(ClavePartida.de(detalle));
                CrearComprobante.IngresoEgresoGenerar_Entrada m = new CrearComprobante.IngresoEgresoGenerar_Entrada();
                m.NumeroReferencia = partida.IdPartidaReferencial;
                m.TipoComprobante = detalle.IdTipoMovimiento.equalsIgnoreCase("MOVING") ? "PARING" : "PAREGR";
                m.IdIngresoEgreso = detalle.IdIngresoEgreso;
                movimientosDePartida.add(m);
            }
            for (PartidaIngresoEgreso partida : partidas) {
                formasDePartida.add(formaDePagoPartida(partida));
            }
            if (!movimientosDePartida.isEmpty()) {
                comprobante.p_IdEmpresa = in.p_IdEmpresa;
                comprobante.p_IdUsuario = in.p_IdUsuario;
                comprobante.p_IdModulo = in.p_IdModulo;
                comprobante.p_SimularOperacion = "N";
                comprobante.p_UsaReglasNegocio = "S";
                comprobante.p_FechaMovimiento = in.p_FechaProceso;
                comprobante.p_IdCaja = 0L;
                comprobante.p_GenerarComprobanteDesdeMovimientos = "S";
                comprobante.p_GenerarComprobanteDesdeComprobantes = "N";
                comprobante.p_EsIngresoAutomatico = "S";
                comprobante.p_IngresoEgresoGenerar = movimientosDePartida;
                comprobante.p_ComprobanteGenerar = new ArrayList<>();
                comprobante.p_FormaDePago = formasDePartida;
                CrearComprobante.CrearComprobante_Salida salida = CrearComprobante.invocar(colaboradores, comprobante);
                out.P_OUT_MENSAJES.addAll(salida.P_OUT_MENSAJES);
                out.P_OUT_ERRORESCONTADOR = MensajeSalida.contarErrores(out.P_OUT_MENSAJES);
                if (out.P_OUT_ERRORESCONTADOR.compareTo(0L) > 0) {
                    out.P_OUT_MENSAJES.add(Plataforma.mensaje(diccionario, in.p_IdEmpresa, 0L, MensajeSalida.MENSAJE_ERROR, "ERROR_AL_GENERAR_PARTIDA_INGRESO_EGRESO", null));
                    return out;
                }
            }
        }

        // ---- mark the loads and the details as processed
        List<CargaMasivaProceso> cargasActualizar = new ArrayList<>();
        for (Long idCarga : idsCargaOk) {
            PreferenciaPagoCarga pref = (PreferenciaPagoCarga) preferenciaPorCarga.get(idCarga);
            CargaMasivaProceso c = new CargaMasivaProceso();
            c.IdCargaMasiva = idCarga;
            c.IdMoneda = pref.IdMoneda;
            c.IdConcepto = pref.IdConcepto;
            c.FechaProceso = in.p_FechaProceso;
            cargasActualizar.add(c);
        }
        if (!cargasActualizar.isEmpty()) {
            sql.lote(SQL_UPDATE_CARGA_MASIVA, cargasActualizar);
        }
        for (CargaMasivaDetalle detalle : detalles) {
            IngresoEgresoGenerado_Salida generado = (IngresoEgresoGenerado_Salida) generados.get(detalle.IdCargaMasivaDetalle);
            detalle.IdIngresoEgreso = generado != null ? generado.IdIngresoEgreso : 0L;
        }
        if (!detalles.isEmpty()) {
            sql.lote(SQL_UPDATE_CARGA_MASIVA_DETALLE, detalles);
        }
        log.debug("== TERMINO PROCESAR CARGA MASIVA ==");
        return out;
    }

    /** Legacy {@code ProcesaRequisito} (every use is an ERROR requirement): message with the load id and one more error for the load. */
    private void requisito(Entrada in, boolean esError, String codigo, CargaMasivaProceso carga, Salida out) {
        if (esError) {
            String descripcion = diccionario.mensaje(in.p_IdEmpresa, codigo, Plataforma.valores().con("IdCargaMasiva", carga.IdCargaMasiva));
            out.P_OUT_MENSAJES.add(new MensajeSalida(carga.IdCargaMasiva, MensajeSalida.MENSAJE_ERROR, codigo, descripcion));
            carga.errores++;
        }
    }

    private static CrearIngresoEgreso.IngresoEgreso_Entrada movimiento(Entrada in, CargaMasivaDetalle detalle, PreferenciaPagoCarga pref) {
        boolean conciliacionBcp = detalle.CodigoTipoCargaMasiva.contentEquals("CONCILIACION_RECAUDACION_BCP");
        CrearIngresoEgreso.IngresoEgreso_Entrada m = new CrearIngresoEgreso.IngresoEgreso_Entrada();
        m.NumeroReferencia = detalle.IdCargaMasivaDetalle;
        m.IdTipoMovimiento = detalle.IdTipoMovimiento;
        m.IdTipoPersona = "CLI";
        m.IdPersona = detalle.IdPersona;
        m.IdPortafolio = detalle.IdPortafolio;
        m.IdMoneda = pref.IdMoneda;
        m.Monto = detalle.Monto;
        m.FechaLiquidacion = in.p_FechaProceso;
        m.IdConcepto = pref.IdConcepto;
        m.IdTipoPersonaTitularPago = "CLI";
        m.IdPersonaTitularPago = detalle.IdPersona;
        m.IdPortafolioTitularPago = detalle.IdPortafolio;
        m.IdCaja = 0L;
        m.SecuenciaAgrupacion = "";
        m.SecuenciaRelacionada = null;
        m.Glosa = detalle.Glosa;
        m.IdEmpleadoDigitador = 0L;
        m.IdSucursalEmpleadoDigitador = 0L;
        m.IdTipoProducto = "";
        m.IdTipoOperacion = "";
        m.IdTipoComprobante = conciliacionBcp ? "BAT" : "";
        m.IdComprobante = conciliacionBcp ? detalle.BCP_CON_NumeroOperacion : 0L;
        m.FechaComprobante = conciliacionBcp ? detalle.BCP_CON_FechaTransaccion : "";
        m.FechaLiquidacionComprobante = "0001-01-01";
        m.IdOperacion = 0L;
        m.FechaOperacion = "0001-01-01";
        m.IdCondicionLiquidacion = "";
        m.IdBolsa = "";
        m.IdMonedaOperacion = "";
        m.MontoOperacion = BigDecimal.ZERO;
        m.IdTipoReajuste = "";
        m.FechaParidad = "0001-01-01";
        m.ValorParidad = BigDecimal.ZERO;
        m.FolioCartera = 0L;
        m.IdInstrumento = 0L;
        m.IdTipoInstrumento = "";
        m.IdClasificacionTipoInstrumento = "";
        m.Bilateral = "N";
        m.MueveCuentaInversion = "S";
        m.GeneraComprobante = pref.ProfundidadProceso.equalsIgnoreCase("COM") || pref.ProfundidadProceso.equalsIgnoreCase("LIQ") ? "S" : "N";
        m.AgruparEnComprobante = "N";
        m.TitularPago = "N";
        return m;
    }

    private static CrearIngresoEgreso.FormaDePago_Entrada formaDePago(CargaMasivaDetalle detalle, PreferenciaPagoCarga pref) {
        CrearIngresoEgreso.FormaDePago_Entrada f = new CrearIngresoEgreso.FormaDePago_Entrada();
        f.NumeroReferencia = detalle.IdCargaMasivaDetalle;
        f.NumeroReferenciaFormaDePago = detalle.IdCargaMasivaDetalle;
        f.IdMedioDePago = pref.IdMedioDePago;
        f.IdModalidadDePago = pref.IdModalidadDePago;
        f.IdMoneda = pref.IdMoneda;
        f.Monto = detalle.Monto;
        f.IdCuentaBancariaCorredora = pref.IdCuentaBanco;
        f.IdTarjetaBancariaCorredora = 0L;
        f.IdChequera = 0L;
        f.NumeroDocumentoCorredora = 0L;
        f.FechaDocumentoPagoCorredora = "0001-01-01";
        f.IdCuentaBancariaSocio = 0L;
        f.IdBancoSocio = 0L;
        f.NombreCuentaBancariaSocio = "";
        f.NumeroCuentaBancariaSocio = "";
        f.IdTipoCuentaBancariaSocio = "";
        f.NombreTipoCuentaBancariaSocio = "";
        f.IdTarjetaBancariaSocio = 0L;
        f.IdTipoTarjetaBancariaSocio = "";
        f.NombreTipoTarjetaBancariaSocio = "";
        f.NumeroTarjetaBancariaSocio = "";
        f.NumeroSerieSocio = "";
        f.NumeroDocumentoSocio = 0L;
        f.FechaDocumentoPagoSocio = "";
        return f;
    }

    private static CrearComprobante.FormaDePago_Entrada formaDePagoPartida(PartidaIngresoEgreso partida) {
        CrearComprobante.FormaDePago_Entrada f = new CrearComprobante.FormaDePago_Entrada();
        f.NumeroReferencia = partida.IdPartidaReferencial;
        f.IdMedioDePago = partida.IdMedioDePago;
        f.IdModalidadDePago = partida.IdModalidadDePago;
        f.IdMoneda = partida.IdMoneda;
        f.Monto = partida.Monto;
        f.IdCuentaBancariaCorredora = partida.IdCuentaBanco;
        f.IdTarjetaBancariaCorredora = 0L;
        f.IdChequera = 0L;
        f.NumeroDocumentoCorredora = 0L;
        f.FechaDocumentoPagoCorredora = "0001-01-01";
        f.IdCuentaBancariaSocio = 0L;
        f.IdBancoSocio = 0L;
        f.NombreCuentaBancariaSocio = "";
        f.NumeroCuentaBancariaSocio = "";
        f.IdTipoCuentaBancariaSocio = "";
        f.NombreTipoCuentaBancariaSocio = "";
        f.IdTarjetaBancariaSocio = 0L;
        f.IdTipoTarjetaBancariaSocio = "";
        f.NombreTipoTarjetaBancariaSocio = "";
        f.NumeroTarjetaBancariaSocio = "";
        f.NumeroSerieSocio = "";
        f.NumeroDocumentoSocio = 0L;
        f.FechaDocumentoPagoSocio = "0001-01-01";
        return f;
    }
}
