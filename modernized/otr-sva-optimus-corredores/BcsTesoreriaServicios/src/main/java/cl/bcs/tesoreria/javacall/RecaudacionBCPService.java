package cl.bcs.tesoreria.javacall;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.tesoreria.javacall.bcp.DiaHabilSiguiente;
import cl.bcs.tesoreria.javacall.bcp.LogMensajeTransaccionBcp;
import cl.bcs.tesoreria.javacall.bcp.LogMensajeTransaccionBcp.Registro;
import cl.bcs.tesoreria.javacall.bcp.ProcesoBcp;
import cl.bcs.tesoreria.javacall.bcp.ProcesoBcp.Cliente;
import cl.bcs.tesoreria.javacall.kit.JsonLegacy;
import cl.bcs.tesoreria.javacall.kit.MensajeSalida;
import cl.bcs.tesoreria.javacall.kit.Plataforma;
import cl.bcs.tesoreria.javacall.kit.SqlNombrado;
import cl.bcs.tesoreria.javacall.kit.colaborador.CrearIngresoEgreso;
import cl.bcs.tesoreria.javacall.kit.colaborador.CrearIngresoEgreso.CrearIngresoEgreso_Entrada;
import cl.bcs.tesoreria.javacall.kit.colaborador.CrearIngresoEgreso.CrearIngresoEgreso_Salida;
import cl.bcs.tesoreria.javacall.kit.colaborador.CrearIngresoEgreso.FormaDePago_Entrada;
import cl.bcs.tesoreria.javacall.kit.colaborador.CrearIngresoEgreso.IngresoEgreso_Entrada;
import cl.bcs.tesoreria.javacall.kit.colaborador.LiquidarComprobante;
import cl.bcs.tesoreria.javacall.kit.colaborador.LiquidarComprobante.ComprobanteLiquidar_Entrada;
import cl.bcs.tesoreria.javacall.kit.colaborador.LiquidarComprobante.LiquidarComprobante_Entrada;
import cl.bcs.tesoreria.javacall.kit.colaborador.LiquidarComprobante.LiquidarComprobante_Salida;
import cl.bcs.tesoreria.port.out.Colaboradores;
import cl.bcs.tesoreria.port.out.ConfiguracionSistema;
import cl.bcs.tesoreria.port.out.DiccionarioMensajes;
import cl.bcs.tesoreria.port.out.Reloj;
import cl.bcs.utils.Utils;

/**
 * Legacy {@code cl.bcs.tesoreria.RecaudacionBCP} (TES-B-64..88, 96): receives a BCP payment (PAGO) or reversal (EXTORNO), logs the message,
 * validates the depositors, creates the income/expense movements and their comprobantes (collaborator CrearIngresoEgreso, paid through the
 * broker's payment preference for the BCP concept) and, inside the BCP schedule, liquidates them (collaborator LiquidarComprobante); outside the
 * schedule the settlement date is the next business day. The answer is stored in the log row.
 */
@Service
public class RecaudacionBCPService {

    private static final Long ID_EMPRESA = 1L;
    // legacy comments "FALTA .... sacar de configuracion": user and country of the company are constants
    private static final Long ID_USUARIO = 69L;
    private static final Long ID_PAIS_EMPRESA = 44L;

    private static final String SQL_SELECT_COMPROBANTE = SqlNombrado.recurso("RecaudacionBCP_SelectComprobante");
    private static final String SQL_SELECT_CONFIGURACION_TESORERIA = SqlNombrado.recurso("Globales_SelectConfiguracionTesoreria");
    private static final String SQL_SELECT_PREFERENCIA_PAGO = SqlNombrado.recurso("Globales_SelectPreferenciaPagoCorredor");

    /** MFX_SvcRecaudaTrn request. */
    public static class RecaudacionBcpEntrada {
        public String IdSolicitudBcp;             // SvcRecaudaTrnRq/RqUID
        public String NumeroOperacionBcp;         // CtxRqHdr/OpnNro
        public String NumeroOperacionExtornoBcp;  // CtxRqHdr/TrnExtorno/OpnExtornoNro: BCP operation being reversed
        public String FechaOperacionBcp;          // CtxRqHdr/ClientFec
        public String TipoOperacionBCP;           // PAGO | EXTORNO | CONSULTA
        public String MensajeEntrada;
        public List<OperacionEntrada> OperacionesBcp = new ArrayList<>();
    }

    public static class OperacionEntrada {
        public String IdClienteBcp; // PagId: BCP depositor id
        public String IdMoneda;
        public BigDecimal Monto;
    }

    /** Answer; the field order is the order of the json stored in the log. */
    public static class RecaudacionBcpSalida {
        public String P_OUT_FECHAPROCESO; // system date and local time of the processing
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
        public Long P_OUT_ERRORESCONTADOR;
        public List<ResultadoProceso> P_OUT_RESULTADOPROCESO = new ArrayList<>();

        @Override
        public String toString() {
            return Utils.printJson(this, false);
        }
    }

    public static class ResultadoProceso {
        public String Campo;
        public Long Id;
        public String Servicio;
        public String Resultado;
    }

    /** TES.ConfiguracionTesoreria parameter (code, effective value). */
    public static class ConfiguracionTesoreria {
        public String CodigoParametro;
        public String Valor;
        public String Descripcion;
    }

    /** Payment preference of the broker for a movement type (view ConsultarPreferenciaPagoCorredor). */
    public static class PreferenciaPagoCorredor {
        public Long IdPreferenciasPagoCorredor;
        public String IdTipoMovimiento;
        public String IdConcepto;
        public String IdMedioDePago;
        public String IdModalidadDePago;
        public Long IdCuentaBanco;
        public String IdEstado;
        public Long IdBanco;
        public String NombreCuentaBancaria;
        public String IdTipoCuentaBancaria;
        public String IdMonedaCuentaBanco;
        public String NumeroCuentaBancaria;
        public String IdEstadoCuentaBanco;
    }

    /** Concept, movement type and text of the two BCP operations that create movements. */
    private enum TipoOperacion {
        PAGO("RECBCP", "MOVING", "Ingreso por Operación de Pago BCP"),
        EXTORNO("RECBCP", "MOVEGR", "Egreso por Operación de Extorno BCP");

        final String concepto;
        final String tipoMovimiento;
        final String glosa;

        TipoOperacion(String concepto, String tipoMovimiento, String glosa) {
            this.concepto = concepto;
            this.tipoMovimiento = tipoMovimiento;
            this.glosa = glosa;
        }
    }

    private final SqlNombrado sql;
    private final DiccionarioMensajes diccionario;
    private final ConfiguracionSistema configuracion;
    private final Reloj reloj;
    private final LogMensajeTransaccionBcp log;
    private final DiaHabilSiguiente diaHabil;
    private final Colaboradores colaboradores;

    public RecaudacionBCPService(NamedParameterJdbcTemplate jdbc, DiccionarioMensajes diccionario, ConfiguracionSistema configuracion, Reloj reloj,
            LogMensajeTransaccionBcp log, DiaHabilSiguiente diaHabil, Colaboradores colaboradores) {
        this.sql = new SqlNombrado(jdbc);
        this.diccionario = diccionario;
        this.configuracion = configuracion;
        this.reloj = reloj;
        this.log = log;
        this.diaHabil = diaHabil;
        this.colaboradores = colaboradores;
    }

    /** Legacy {@code call}: the request is the JSON of {@link RecaudacionBcpEntrada}. */
    @Transactional(rollbackFor = Exception.class)
    public JsonObject execute(JsonObject entrada) throws Exception {
        RecaudacionBcpEntrada in = JsonLegacy.de(entrada, RecaudacionBcpEntrada.class);
        RecaudacionBcpSalida out = recaudar(in);
        out.P_OUT_ERRORESCONTADOR = MensajeSalida.contarErrores(out.P_OUT_MENSAJES);
        return JsonParser.parseString(JsonLegacy.gson().toJson(out)).getAsJsonObject();
    }

    RecaudacionBcpSalida recaudar(RecaudacionBcpEntrada in) throws Exception {
        RecaudacionBcpSalida out = new RecaudacionBcpSalida();
        String fechaSistema = Plataforma.fechaSistema(configuracion, ID_EMPRESA);
        out.P_OUT_FECHAPROCESO = fechaHoraSistemaLocal(fechaSistema);

        if (in.OperacionesBcp.isEmpty()) {
            out.P_OUT_MENSAJES.add(error("NO_SE_INGRESARON_SOLICITUDES_DE_INGRESO_EGRESO"));
            return out;
        }

        // the message is logged first and read back (to be answered at the end)
        log.registrar(in.IdSolicitudBcp, in.TipoOperacionBCP, in.MensajeEntrada, fechaSistema, ID_EMPRESA);
        Map<String, Registro> registros = log.buscar(ID_EMPRESA, in.TipoOperacionBCP, in.IdSolicitudBcp);
        if (registros.isEmpty()) {
            out.P_OUT_MENSAJES.add(error("ERROR_EN_REGISTRO_MENSAJE_ENTRADA_BCP"));
            return out;
        }

        // schedule of the BCP collection
        Map<String, ConfiguracionTesoreria> parametros = new HashMap<>();
        String apertura = "";
        String cierre = "";
        List<ConfiguracionTesoreria> filasConfiguracion = sql.consultar(SQL_SELECT_CONFIGURACION_TESORERIA,
                Map.of("IdEmpresa", ID_EMPRESA, "CodigoParametros", List.of("HORARIOAPERTURARECAUDACIONBCP", "HORARIOCIERRERECAUDACIONBCP")),
                ConfiguracionTesoreria.class);
        for (ConfiguracionTesoreria c : filasConfiguracion) {
            clavePrimaria(parametros.put(c.CodigoParametro, c));
        }
        if (parametros.size() > 0) {
            // a missing one of the two throws NullPointerException, as in the legacy
            apertura = parametros.get("HORARIOAPERTURARECAUDACIONBCP").Valor;
            cierre = parametros.get("HORARIOCIERRERECAUDACIONBCP").Valor;
        }
        if (apertura.equalsIgnoreCase("") || cierre.equalsIgnoreCase("")) {
            out.P_OUT_MENSAJES.add(error("NO_EXISTE_PARAMETRO_HORARIO_RECAUDACION_BCP"));
            return out;
        }

        for (OperacionEntrada o : in.OperacionesBcp) {
            o.IdClienteBcp = ProcesoBcp.sinCerosIzquierda(o.IdClienteBcp);
        }

        boolean estaEnHorario = dentroDeHorario(apertura, cierre);

        // settlement date: today inside the schedule, the next business day outside
        String fechaLiquidacion;
        if (estaEnHorario) {
            fechaLiquidacion = fechaSistema;
        } else {
            fechaLiquidacion = diaHabil.sumarDiasHabiles(ID_PAIS_EMPRESA, fechaSistema, 1);
        }
        if (fechaLiquidacion == null || fechaLiquidacion.equals("")) {
            out.P_OUT_MENSAJES.add(error("FECHA_LIQUIDACION_VACIA"));
            return out;
        }

        Map<String, Cliente> clientes = ProcesoBcp.consultarClientes(sql, ID_EMPRESA, in.OperacionesBcp.stream().map(o -> o.IdClienteBcp).toList());

        // payment preferences of the broker for the BCP concept (PAGO: income, EXTORNO: expense; any other type finds none)
        List<String> conceptos = new ArrayList<>();
        List<String> tiposMovimiento = new ArrayList<>();
        if (in.TipoOperacionBCP.equalsIgnoreCase("PAGO")) {
            conceptos.add(TipoOperacion.PAGO.concepto);
            tiposMovimiento.add(TipoOperacion.PAGO.tipoMovimiento);
        } else if (in.TipoOperacionBCP.equalsIgnoreCase("EXTORNO")) {
            conceptos.add(TipoOperacion.EXTORNO.concepto);
            tiposMovimiento.add(TipoOperacion.EXTORNO.tipoMovimiento);
        }
        Map<String, PreferenciaPagoCorredor> preferencias = new HashMap<>();
        for (PreferenciaPagoCorredor p : sql.consultar(SQL_SELECT_PREFERENCIA_PAGO,
                Map.of("IdEmpresa", ID_EMPRESA, "IdConceptos", conceptos, "IdTipoMovimientos", tiposMovimiento), PreferenciaPagoCorredor.class)) {
            clavePrimaria(preferencias.put(p.IdTipoMovimiento, p));
        }
        if (preferencias.isEmpty()) {
            out.P_OUT_MENSAJES.add(error("NO_EXISTE_PREFERENCIAS_PAGO_CORREDOR_RECAUDACION_BCP"));
            return out;
        }

        // the lines with their client
        Long idMensajeTransaccion = registros.get(in.IdSolicitudBcp).IdMensajeTransaccion;
        List<ProcesoBcp> operaciones = new ArrayList<>();
        for (OperacionEntrada o : in.OperacionesBcp) {
            String llave = ProcesoBcp.llaveCliente(ID_EMPRESA, o.IdClienteBcp);
            ProcesoBcp p = new ProcesoBcp();
            p.IdClienteBcp = o.IdClienteBcp;
            p.IdMoneda = o.IdMoneda;
            p.Monto = o.Monto;
            p.asignarCliente(llave, clientes.get(llave), idMensajeTransaccion);
            operaciones.add(p);
        }

        // validation of every line (CP0140 depositor restricted; CP0139 for the reversal certification case)
        List<ProcesoBcp> conErrores = new ArrayList<>();
        for (ProcesoBcp p : operaciones) {
            p.validar(diccionario, ID_EMPRESA, "CP0140", false, out.P_OUT_MENSAJES);

            // LEGACY DEFECT kept (see README TES-B-75): "only for BCP certification, delete afterwards": these four depositor ids force an error or an exception
            boolean existe = p.IdPersona != 0 && p.IdCliente != 0;
            boolean pago = in.TipoOperacionBCP.equalsIgnoreCase("PAGO");
            boolean extorno = in.TipoOperacionBCP.equalsIgnoreCase("EXTORNO");
            p.requisito(diccionario, ID_EMPRESA, pago && p.IdClienteBcp.equalsIgnoreCase("67412388") && existe, "CLIENTE_NO_ACTIVO", "CP0140", false, out.P_OUT_MENSAJES);
            if (pago && p.IdClienteBcp.equalsIgnoreCase("44607843") && existe) { // LEGACY DEFECT kept (see README TES-B-76)
                throw new Exception("Error");
            }
            p.requisito(diccionario, ID_EMPRESA, extorno && p.IdClienteBcp.equalsIgnoreCase("44225588") && existe, "CLIENTE_NO_ACTIVO", "CP0139", false, out.P_OUT_MENSAJES);
            if (extorno && p.IdClienteBcp.equalsIgnoreCase("11225588") && existe) { // LEGACY DEFECT kept (see README TES-B-78)
                throw new Exception("Error");
            }
        }
        operaciones.stream().filter(p -> p.errores > 0).forEach(conErrores::add);
        operaciones.removeAll(conErrores);

        // movements and comprobantes
        List<Long> comprobantesGenerados = new ArrayList<>();
        if (!operaciones.isEmpty()) {
            CrearIngresoEgreso_Entrada crear = new CrearIngresoEgreso_Entrada();
            crear.p_IdEmpresa = ID_EMPRESA;
            crear.p_IdUsuario = ID_USUARIO;
            crear.p_IdModulo = "TES";
            crear.p_SimularOperacion = "N";
            crear.p_UsaReglasNegocio = "S";
            crear.p_EsIngresoAutomatico = "S";
            List<IngresoEgreso_Entrada> movimientos = new ArrayList<>();
            List<FormaDePago_Entrada> formasDePago = new ArrayList<>();
            long numeroReferencia = 0;
            for (ProcesoBcp p : operaciones) {
                numeroReferencia++;
                IngresoEgreso_Entrada ie = new IngresoEgreso_Entrada();
                ie.IdMoneda = p.IdMoneda;
                ie.Monto = p.Monto;
                ie.IdPersona = p.IdPersona;
                TipoOperacion tipo = null;
                if (in.TipoOperacionBCP.equalsIgnoreCase("PAGO")) {
                    tipo = TipoOperacion.PAGO;
                    ie.Glosa = tipo.glosa + " - IdSolicitudBcp: " + in.IdSolicitudBcp;
                } else if (in.TipoOperacionBCP.equalsIgnoreCase("EXTORNO")) {
                    tipo = TipoOperacion.EXTORNO;
                    ie.Glosa = tipo.glosa + " - IdSolicitudBcp: " + in.IdSolicitudBcp + " - NumeroOperacionExtorno: " + in.NumeroOperacionExtornoBcp;
                }
                if (tipo != null) {
                    ie.IdConcepto = tipo.concepto;
                    ie.IdTipoMovimiento = tipo.tipoMovimiento;
                }
                ie.IdPortafolio = p.IdPortafolioDefecto;
                ie.IdTipoPersona = p.IdTipoSocioDeNegocio;
                ie.FechaLiquidacion = fechaLiquidacion;
                ie.NumeroReferencia = numeroReferencia;
                ie.IdCaja = 0L;
                ie.SecuenciaAgrupacion = "";
                ie.SecuenciaRelacionada = 0L;
                ie.TitularPago = "N"; // the payer is the client itself
                ie.IdTipoPersonaTitularPago = p.IdTipoSocioDeNegocio;
                ie.IdPersonaTitularPago = p.IdPersona;
                ie.IdPortafolioTitularPago = p.IdPortafolioDefecto;
                ie.IdEmpleadoDigitador = 0L;
                ie.IdSucursalEmpleadoDigitador = 0L;
                ie.IdTipoProducto = "";
                ie.IdTipoOperacion = "";
                // origin: BCP (WS online load)
                ie.IdTipoComprobante = "WS";
                // LEGACY DEFECT kept (see README TES-B-86): the legacy compared NumeroOperacionBcp == "" by reference, so an empty number reaches Long.parseLong and throws
                ie.IdComprobante = Long.parseLong(in.NumeroOperacionBcp == null ? "0" : in.NumeroOperacionBcp);
                ie.FechaComprobante = in.FechaOperacionBcp;
                ie.FechaLiquidacionComprobante = "0001-01-01";
                if (in.TipoOperacionBCP.equalsIgnoreCase("PAGO")) {
                    ie.IdOperacion = 0L;
                } else if (in.TipoOperacionBCP.equalsIgnoreCase("EXTORNO")) {
                    ie.IdOperacion = Long.parseLong(in.NumeroOperacionExtornoBcp);
                }
                ie.FechaOperacion = "0001-01-01";
                ie.IdCondicionLiquidacion = "";
                ie.IdBolsa = "";
                ie.IdMonedaOperacion = "";
                ie.MontoOperacion = BigDecimal.ZERO;
                ie.IdTipoReajuste = "";
                ie.FechaParidad = "0001-01-01";
                ie.ValorParidad = BigDecimal.ZERO;
                ie.FolioCartera = 0L;
                ie.IdInstrumento = 0L;
                ie.IdTipoInstrumento = "";
                ie.IdClasificacionTipoInstrumento = "";
                ie.Bilateral = "N";
                ie.GeneraComprobante = "S";
                ie.AgruparEnComprobante = "N";
                ie.MueveCuentaInversion = "S";
                movimientos.add(ie);

                // payment form of the broker according to its payment preference (always one: same reference for movement and payment form)
                FormaDePago_Entrada fp = new FormaDePago_Entrada();
                fp.NumeroReferencia = ie.NumeroReferencia;
                fp.NumeroReferenciaFormaDePago = ie.NumeroReferencia;
                PreferenciaPagoCorredor preferencia = preferencias.get(in.TipoOperacionBCP.equalsIgnoreCase("PAGO") ? "MOVING" : "MOVEGR");
                fp.IdMedioDePago = preferencia.IdMedioDePago;
                fp.IdModalidadDePago = preferencia.IdModalidadDePago;
                fp.IdCuentaBancariaCorredora = preferencia.IdCuentaBanco;
                fp.IdMoneda = ie.IdMoneda;
                fp.Monto = ie.Monto;
                fp.IdTarjetaBancariaCorredora = 0L;
                fp.IdChequera = 0L;
                fp.NumeroDocumentoCorredora = 0L;
                fp.FechaDocumentoPagoCorredora = "0001-01-01";
                fp.IdCuentaBancariaSocio = 0L;
                fp.IdBancoSocio = 0L;
                fp.NombreCuentaBancariaSocio = "";
                fp.NumeroCuentaBancariaSocio = "";
                fp.IdTipoCuentaBancariaSocio = "";
                fp.NombreTipoCuentaBancariaSocio = "";
                fp.IdTarjetaBancariaSocio = 0L;
                fp.IdTipoTarjetaBancariaSocio = "";
                fp.NombreTipoTarjetaBancariaSocio = "";
                fp.NumeroTarjetaBancariaSocio = "";
                fp.NumeroSerieSocio = "";
                fp.NumeroDocumentoSocio = 0L;
                fp.FechaDocumentoPagoSocio = "0001-01-01";
                formasDePago.add(fp);
            }
            crear.p_IngresoEgresoGenerar = movimientos;
            crear.p_FormaDePago = formasDePago;

            CrearIngresoEgreso_Salida creado = CrearIngresoEgreso.invocar(colaboradores, crear);
            out.P_OUT_MENSAJES.addAll(creado.P_OUT_MENSAJES);
            if (MensajeSalida.contarErrores(creado.P_OUT_MENSAJES) > 0) {
                return out;
            }
            for (CrearIngresoEgreso.ResultadoProceso_Salida r : creado.P_OUT_RESULTADOPROCESO) {
                out.P_OUT_RESULTADOPROCESO.add(copiar(r.Campo, r.Id, r.Servicio, r.Resultado));
                if (r.Campo.equalsIgnoreCase("IdComprobante") && r.Resultado.equalsIgnoreCase("ING")) {
                    comprobantesGenerados.add(r.Id);
                }
            }
        }

        // settle the comprobantes (only inside the schedule)
        if (!comprobantesGenerados.isEmpty() && estaEnHorario) {
            LiquidarComprobante_Entrada liquidar = new LiquidarComprobante_Entrada();
            liquidar.p_IdEmpresa = ID_EMPRESA;
            liquidar.p_IdUsuario = ID_USUARIO;
            liquidar.p_IdModulo = "TES";
            liquidar.p_SimularOperacion = "N";
            liquidar.p_UsaReglasNegocio = "S";
            liquidar.p_FechaMovimiento = fechaSistema;
            // whole comprobantes, not partial payment forms
            liquidar.p_ComprobanteLiquidar = sql.consultar(SQL_SELECT_COMPROBANTE, Map.of("idsComprobantes", comprobantesGenerados),
                    ComprobanteLiquidar_Entrada.class);

            LiquidarComprobante_Salida liquidado = LiquidarComprobante.invocar(colaboradores, liquidar);
            out.P_OUT_MENSAJES.addAll(liquidado.P_OUT_MENSAJES);
            if (MensajeSalida.contarErrores(liquidado.P_OUT_MENSAJES) > 0) {
                return out;
            }
            for (LiquidarComprobante.ResultadoProceso_Salida r : liquidado.P_OUT_RESULTADOPROCESO) {
                out.P_OUT_RESULTADOPROCESO.add(copiar(r.Campo, r.Id, r.Servicio, r.Resultado));
            }
        }

        // the answer sent to BCP is stored in the log row (error returns above leave it empty, as the legacy)
        Registro registro = registros.get(in.IdSolicitudBcp);
        registro.MensajeSalida = out.toString();
        log.guardarSalida(List.of(registro));
        return out;
    }

    private MensajeSalida error(String codigo) {
        return Plataforma.mensaje(diccionario, ID_EMPRESA, 0L, MensajeSalida.MENSAJE_ERROR, codigo, null);
    }

    private static ResultadoProceso copiar(String campo, Long id, String servicio, String resultado) {
        ResultadoProceso r = new ResultadoProceso();
        r.Campo = campo;
        r.Id = id;
        r.Servicio = servicio;
        r.Resultado = resultado;
        return r;
    }

    /** Two rows with the same key: legacy {@code Utils.List2Hash} threw {@code Exception("key field is not unique")}. */
    private static void clavePrimaria(Object anterior) throws Exception {
        if (anterior != null) {
            throw new Exception("key field is not unique");
        }
    }

    /**
     * System date plus local time (legacy {@code FechaDB.ObtenerFechaHoraSistemaLocal}): the UTC clock moved by PLATAFORMA/PLA.SegundosZonaHoraria.
     * The offset is applied in UTC so the text does not depend on the JVM zone.
     */
    private String fechaHoraSistemaLocal(String fechaSistema) throws Exception {
        long segundos = Plataforma.parametroLong(configuracion, ID_EMPRESA, "PLATAFORMA", "PLA.SegundosZonaHoraria");
        String hora = java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss").withZone(java.time.ZoneOffset.UTC).format(reloj.ahora().plusSeconds(segundos));
        return fechaSistema + " " + hora;
    }

    /**
     * Whether the clock time (HH:mm:ss, JVM zone: the legacy read the server clock) is inside [opening, closing]. The times are parsed with the
     * lenient SimpleDateFormat of the legacy (a malformed parameter is a ParseException).
     */
    private boolean dentroDeHorario(String apertura, String cierre) throws java.text.ParseException {
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        Instant ahora = reloj.ahora();
        Date actual = df.parse(df.format(Date.from(ahora)));
        return actual.compareTo(df.parse(apertura)) >= 0 && actual.compareTo(df.parse(cierre)) <= 0;
    }
}
