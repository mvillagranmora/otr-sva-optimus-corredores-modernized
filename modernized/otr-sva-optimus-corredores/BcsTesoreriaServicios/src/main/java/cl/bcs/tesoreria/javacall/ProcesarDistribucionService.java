package cl.bcs.tesoreria.javacall;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonObject;

import cl.bcs.tesoreria.javacall.distribucion.Corrida;
import cl.bcs.tesoreria.javacall.distribucion.EntradaRecaudacionTraspaso;
import cl.bcs.tesoreria.javacall.distribucion.Filas.Caja;
import cl.bcs.tesoreria.javacall.distribucion.Filas.ContratoSuscripcionCuotaDetalle;
import cl.bcs.tesoreria.javacall.distribucion.Filas.CtaDistribucion;
import cl.bcs.tesoreria.javacall.distribucion.Filas.DatosCorredor;
import cl.bcs.tesoreria.javacall.distribucion.Filas.DistribucionInversion;
import cl.bcs.tesoreria.javacall.distribucion.Filas.Empleado;
import cl.bcs.tesoreria.javacall.distribucion.InRecaudacion;
import cl.bcs.tesoreria.javacall.distribucion.MovimientosDistribucion;
import cl.bcs.tesoreria.javacall.distribucion.MovimientosDistribucion.DatosTesoreria;
import cl.bcs.tesoreria.javacall.distribucion.PreRendicionPac;
import cl.bcs.tesoreria.javacall.distribucion.RegistroRecaudacion;
import cl.bcs.tesoreria.javacall.distribucion.SalidaRecaudacionTraspaso;
import cl.bcs.tesoreria.javacall.kit.JsonLegacy;
import cl.bcs.tesoreria.javacall.kit.MensajeSalida;
import cl.bcs.tesoreria.javacall.kit.Plataforma;
import cl.bcs.tesoreria.javacall.kit.SqlNombrado;
import cl.bcs.tesoreria.javacall.kit.colaborador.CalcularParidad;
import cl.bcs.tesoreria.javacall.kit.colaborador.RequisitosBasicos;
import cl.bcs.tesoreria.port.out.Colaboradores;
import cl.bcs.tesoreria.port.out.ConfiguracionSistema;
import cl.bcs.utils.Utils;

/**
 * Distribution of the PAC rendition (legacy {@code cl.bcs.tesoreria.ProcesarDistribucion}, TES-B-277..321): crosses the loaded rendition file with the
 * mandates and contracts and, for every valid request, creates the Tesoreria movements, the investment-account entries and the fund contributions or stock
 * orders of its contract. {@link #execute} is {@code logica}; {@link #validaPreRendicion} is the public {@code ValidaPreRendicion} (the validation and log
 * of the pre-rendition, also called by CrearCargaMasivaPacRen).
 *
 * <p>The request carries no data ({@code p_Recaudacion} is ignored): the work list is read from CAR.CargaMasivaDetallePAC, PAC.MandatoPAC and
 * PAC.LogMandatoPAC. The response is the legacy {@code SalidaRecaudacionTraspaso} serialised with the legacy Gson (dates {@code yyyy-MM-dd HH:mm:ss.SSS});
 * the legacy production {@code call} used a plain Gson whose date format depends on the platform locale, which is not reproduced.
 *
 * <p>Steps live in {@link PreRendicionPac} (file crossing, requests, log) and {@link MovimientosDistribucion} (the four movement steps); this class
 * orchestrates one run and owns the contract-distribution queries. Collaborator services are reached through the {@link Colaboradores} port.
 */
@Service
public class ProcesarDistribucionService implements cl.bcs.tesoreria.javacall.carga.ValidadorPreRendicion {

    private static final Logger log = LoggerFactory.getLogger(ProcesarDistribucionService.class);

    /** Legacy {@code ConstantesTesoreria.EstadoProcesado}. */
    private static final String ESTADO_PROCESADO = "PRO";

    private static final String SQL_CORREDOR = SqlNombrado.recurso("ProcesarDistribucion_ConsultarCorredor");
    private static final String SQL_EMPLEADO = SqlNombrado.recurso("ProcesarDistribucion_ConsultarEmpleado");
    private static final String SQL_CUENTA_DISTRIBUCION = SqlNombrado.recurso("ProcesarDistribucion_ConsultarCuentaDistribucion");
    private static final String SQL_CAJA_TESORERIA_PAC = SqlNombrado.recurso("ProcesarDistribucion_SelectCajaTesoreriaParaPAC");
    private static final String SQL_MANDATOS_Y_ARCHIVO = SqlNombrado.recurso("ProcesarDistribucion_ConsultarMandatosPacYArchivo");
    private static final String SQL_MANDATOS_PAC = SqlNombrado.recurso("ProcesarDistribucion_ConsultarMandatosPac");
    private static final String SQL_CSC_DETALLE = SqlNombrado.recurso("ProcesarDistribucion_ConsultarContratoSuscripcionCuotasDetalle");
    private static final String SQL_DISTRIBUCION_SAAPV = SqlNombrado.recurso("ProcesarDistribucion_ConsultarSerieSAAPV");

    private final SqlNombrado sql;
    private final ConfiguracionSistema configuracion;
    private final PreRendicionPac preRendicion;
    private final MovimientosDistribucion movimientos;
    private final Colaboradores colaboradores;

    public ProcesarDistribucionService(NamedParameterJdbcTemplate jdbc, ConfiguracionSistema configuracion, PreRendicionPac preRendicion,
            MovimientosDistribucion movimientos, Colaboradores colaboradores) {
        this.sql = new SqlNombrado(jdbc);
        this.configuracion = configuracion;
        this.preRendicion = preRendicion;
        this.movimientos = movimientos;
        this.colaboradores = colaboradores;
    }

    /** Legacy {@code logica}: the distribution of the rendition. */
    @Transactional(rollbackFor = Exception.class)
    public JsonObject execute(JsonObject entradaJson) throws Exception {
        EntradaRecaudacionTraspaso entrada = JsonLegacy.de(entradaJson, EntradaRecaudacionTraspaso.class);
        return JsonLegacy.arbol(distribuir(entrada)).getAsJsonObject();
    }

    /** Legacy {@code ValidaPreRendicion}: validates the file against the mandates and writes PAC.LogMandatoPAC (TES-B-309..318). */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public JsonObject validaPreRendicion(JsonObject entradaJson) throws Exception {
        EntradaRecaudacionTraspaso entrada = JsonLegacy.de(entradaJson, EntradaRecaudacionTraspaso.class);
        return JsonLegacy.arbol(validar(entrada)).getAsJsonObject();
    }

    // ------------------------------------------------------------------------------------------------ ValidaPreRendicion

    private SalidaRecaudacionTraspaso validar(EntradaRecaudacionTraspaso entrada) throws Exception {
        String fechaSistema = Plataforma.fechaSistema(configuracion, entrada.p_IdEmpresa);
        Corrida c = new Corrida(entrada, fechaSistema, valorUf(entrada, fechaSistema));

        if (hayErroresBasicos(entrada)) {
            c.out.P_OUT_ERRORESCONTADOR = 1L;
            return c.out;
        }
        preRendicion.validar(c);
        return c.out;
    }

    // ------------------------------------------------------------------------------------------------ logica

    private SalidaRecaudacionTraspaso distribuir(EntradaRecaudacionTraspaso entrada) throws Exception {
        log.info("INICIO::ProcesarDistribucion");
        String fechaSistema = Plataforma.fechaSistema(configuracion, entrada.p_IdEmpresa);
        String monedaSistema = Plataforma.parametro(configuracion, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
        Corrida c = new Corrida(entrada, fechaSistema, valorUf(entrada, fechaSistema));
        SalidaRecaudacionTraspaso out = c.out;

        Map<String, Object> param = new HashMap<>();
        param.put("IdEmpresa", entrada.p_IdEmpresa);
        List<DatosCorredor> datosCorredor = sql.consultar(SQL_CORREDOR, param, DatosCorredor.class);
        boolean erroresBasicos = hayErroresBasicos(entrada);
        // LEGACY DEFECT kept (see README TES-B-288): the broker row is read before the basic requirements are checked; a missing row is an IndexOutOfBoundsException
        Long idPortafolioCorredor = datosCorredor.get(0).IdPortafolio();
        Long idPersonaCorredor = datosCorredor.get(0).IdPersona();

        if (erroresBasicos) {
            out.P_OUT_ERRORESCONTADOR = 1L;
            return out;
        }

        preRendicion.agregarLogsPreRendicion(c);

        // request assembly from the file load and the pending mandates
        Map<String, Object> sinParametros = new HashMap<>();
        List<InRecaudacion> registrosArchivo = sql.consultar(SQL_MANDATOS_Y_ARCHIVO, sinParametros, InRecaudacion.class);
        List<InRecaudacion> cruceArchivoMandatos = preRendicion.validacionSolicitudesArchivo(c, registrosArchivo);
        List<InRecaudacion> traspasos = sql.consultar(SQL_MANDATOS_PAC, sinParametros, InRecaudacion.class);

        if (!cruceArchivoMandatos.isEmpty() && !traspasos.isEmpty()) {
            preRendicion.validaArchivoContraMandatos(c, cruceArchivoMandatos, traspasos);
        }
        if (traspasos.isEmpty()) {
            preRendicion.archivoNoCoincide(out);
            return out;
        }

        List<RegistroRecaudacion> solicitudesProcesadas = preRendicion.solicitudesAProcesar(c, traspasos, cruceArchivoMandatos);
        out.P_OUT_SOLICITUDES_A_PROCESAR.addAll(solicitudesProcesadas);
        List<RegistroRecaudacion> solicitudProcesar = preRendicion.solicitudesValidas(c, solicitudesProcesadas);

        Long idEmpleadoDigitador = 0L;
        Long idCtaBancoCorredor = 0L;
        Long idPersonaBanco = 0L;

        Map<String, Object> paramEmpleado = new HashMap<>();
        paramEmpleado.put("IdUsuario", entrada.p_IdUsuario);
        List<Empleado> empleado = sql.consultar(SQL_EMPLEADO, paramEmpleado, Empleado.class);
        if (!empleado.isEmpty()) {
            idEmpleadoDigitador = empleado.get(0).IdEmpleado();
        }

        List<CtaDistribucion> ctaBancoCorredor = sql.consultar(SQL_CUENTA_DISTRIBUCION, param, CtaDistribucion.class);
        if (!ctaBancoCorredor.isEmpty()) {
            idCtaBancoCorredor = ctaBancoCorredor.get(0).DestinoCtaBco();
            idPersonaBanco = ctaBancoCorredor.get(0).IdPersonaBanco();
        }

        Long idCajaTesoreriaPac = 0L;
        List<Caja> cajas = sql.consultar(SQL_CAJA_TESORERIA_PAC, param, Caja.class);
        if (!cajas.isEmpty()) {
            idCajaTesoreriaPac = cajas.get(0).IdCaja();
        }

        // 1 TESORERIA
        movimientos.tesoreria(c, solicitudProcesar, new DatosTesoreria(monedaSistema, idPortafolioCorredor, idPersonaCorredor, idEmpleadoDigitador, idCtaBancoCorredor,
                idCajaTesoreriaPac, idPersonaBanco));

        // distribution of every contract, to know the state of the contract and of the portfolio
        List<String> foliosCSC = new ArrayList<>();
        List<String> foliosAPV = new ArrayList<>();
        BigDecimal montoTotalCorredora = BigDecimal.ZERO;
        for (RegistroRecaudacion solicitud : solicitudProcesar) {
            if (esCsc(solicitud)) {
                foliosCSC.add(solicitud.FolioCSC.toString());
            } else {
                foliosAPV.add(solicitud.FolioAPV.toString());
            }
            montoTotalCorredora = montoTotalCorredora.add(solicitud.MontoDeposito);
        }

        List<ContratoSuscripcionCuotaDetalle> distribucionCSC = null;
        List<DistribucionInversion> distribucionSAPV = null;
        if (!foliosCSC.isEmpty()) {
            distribucionCSC = contratosCsc(foliosCSC);
        }
        if (!foliosAPV.isEmpty()) {
            distribucionSAPV = distribucionSapv(foliosAPV);

            List<String> foliosConDistribucion = distribucionSAPV.stream().filter(d -> d.FolioContratoSAAPV != null).map(d -> d.FolioContratoSAAPV).distinct().collect(Collectors.toList());
            if (foliosConDistribucion.size() < foliosAPV.size()) {
                List<String> sinDistribucion = new ArrayList<>();
                for (String folio : foliosAPV) {
                    if (!foliosConDistribucion.contains(folio)) {
                        sinDistribucion.add(folio);
                    }
                }
                log.info("SALIDA::ProcesarDistribucion.getSalidaDistribucion - Solicitudes sin distribucion: {}", sinDistribucion);
                out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "Contratos sin distribucion", "Contratos sin distribucion FolioContratoSAAPV " + sinDistribucion));
                out.P_OUT_ERRORESCONTADOR++;
                return out;
            }
            // the last line of every contract absorbs the rounding of the others (and may become negative: no guard in this first adjustment)
            ajustarUltimaLinea(distribucionSAPV, false);
        }

        // contract and portfolio state of every request; a non-active client or portfolio takes the request out silently
        for (RegistroRecaudacion solicitud : solicitudProcesar) {
            if (esCsc(solicitud)) {
                for (ContratoSuscripcionCuotaDetalle cscd : distribucionCSC) {
                    if (solicitud.FolioCSC.equals(cscd.IdFolioCSC)) {
                        solicitud.EstadoCSC = cscd.EstadoContrato;
                        if (!cscd.EstadoContrato.equalsIgnoreCase("ING")) {
                            solicitud.EstadoRendicion = "Contrato bloqueado, Anulado o Revocado";
                            solicitud.EstadoProcesoRendicion = "N";
                        } else if (cscd.EstadoPortafolio.equals("BLO")) {
                            solicitud.EstadoRendicion = "Portafolio Bloqueado";
                            solicitud.EstadoProcesoRendicion = "N";
                        }
                    }
                }
            } else {
                for (DistribucionInversion capv : distribucionSAPV) {
                    String folioAPV = String.valueOf(solicitud.FolioAPV);
                    if (folioAPV.equals(capv.FolioContratoSAAPV)) {
                        solicitud.EstadoSAAPV = capv.EstadoContrato;
                        if (!capv.EstadoContrato.equals("VIG")) {
                            solicitud.EstadoRendicion = "Contrato no vigente";
                            solicitud.EstadoProcesoRendicion = "N";
                        } else if (!capv.EstadoPortafolio.equals("VIG")) {
                            solicitud.EstadoRendicion = "Portafolio Bloqueado";
                            solicitud.EstadoProcesoRendicion = "N";
                        }
                    }
                }
            }
        }

        List<RegistroRecaudacion> clientesActivos = new ArrayList<>();
        for (RegistroRecaudacion solicitud : solicitudProcesar) {
            if (!solicitud.EstadoPortafolio.equalsIgnoreCase("ACT")) {
                solicitud.EstadoRendicion = "Portafolio inactivo, la solicitud no se procesara";
                solicitud.EstadoProcesoRendicion = "N";
            } else if (!solicitud.EstadoCliente.equals("ACTIVO")) {
                solicitud.EstadoRendicion = "Cliente inactivo, la solicitud no se procesara";
                solicitud.EstadoProcesoRendicion = "N";
            } else {
                clientesActivos.add(preRendicion.copiar(solicitud));
            }
        }

        if (clientesActivos.isEmpty()) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "EL ARCHIVO DE CARGA NO PRESENTA CLIENTE(S) ACTIVO(S)", "EL ARCHIVO DE CARGA NO PRESENTA CLIENTE(S) ACTIVO(S)"));
            out.P_OUT_ERRORESCONTADOR += 1L;
            return out;
        }

        List<RegistroRecaudacion> solicitudesContratos = new ArrayList<>();
        for (RegistroRecaudacion solicitud : clientesActivos) {
            if (ESTADO_PROCESADO.equalsIgnoreCase(solicitud.EstadoProceso) && !solicitud.EstadoProcesoRendicion.equals("N")) {
                solicitudesContratos.add(solicitud);
            }
        }

        // 2 CUENTA DE INVERSION
        movimientos.cuentaInversion(c, solicitudesContratos, monedaSistema, idEmpleadoDigitador, idPortafolioCorredor, montoTotalCorredora);

        List<RegistroRecaudacion> recaudacionProcesadaInversion = new ArrayList<>();
        for (RegistroRecaudacion procta : solicitudesContratos) {
            if (ESTADO_PROCESADO.equalsIgnoreCase(procta.EstadoProceso)) {
                recaudacionProcesadaInversion.add(Utils.copyPojo(procta, new RegistroRecaudacion()));
            }
        }

        // distribution of the contracts that went through, to know which of their lines are funds (FFM) and which are stocks (ACN)
        distribucionCSC = null;
        distribucionSAPV = null;
        List<String> foliosCSCProcesados = new ArrayList<>();
        List<String> foliosAPVProcesados = new ArrayList<>();
        for (RegistroRecaudacion regRecaudacion : recaudacionProcesadaInversion) {
            if (esCsc(regRecaudacion)) {
                foliosCSCProcesados.add(regRecaudacion.FolioCSC.toString());
            } else {
                foliosAPVProcesados.add(regRecaudacion.FolioAPV.toString());
            }
        }

        // LEGACY DEFECT kept (see README TES-B-286): the guards test the folios collected while the requests were assembled (c.foliosCSC / c.foliosAPV), not the ones
        // of the requests that went through, so the queries run (with the folios that did go through, possibly none) whenever any request was ever assembled
        if (!c.foliosCSC.isEmpty()) {
            distribucionCSC = contratosCsc(foliosCSCProcesados);
        }
        if (!c.foliosAPV.isEmpty()) {
            distribucionSAPV = distribucionSapv(foliosAPVProcesados);
            ajustarUltimaLinea(distribucionSAPV, true);
        }

        if (distribucionCSC != null) {
            for (ContratoSuscripcionCuotaDetalle contrato : distribucionCSC) {
                if (contrato.Moneda.equalsIgnoreCase("UF")) {
                    contrato.Monto = contrato.Monto.multiply(c.valorUF);
                }
            }
        }

        List<RegistroRecaudacion> solicitudesAFFMM = new ArrayList<>();
        List<RegistroRecaudacion> solicitudesAOrdenes = new ArrayList<>();
        for (RegistroRecaudacion solicitud : recaudacionProcesadaInversion) {
            if (esCsc(solicitud)) {
                solicitudesAFFMM.add(preRendicion.copiar(solicitud));
            } else {
                for (DistribucionInversion distribucion : distribucionSAPV) {
                    RegistroRecaudacion copia = preRendicion.copiar(solicitud);
                    // LEGACY DEFECT kept (see README TES-B-286): the loop goes over ALL the lines and breaks at the first FFM line, so ACN lines listed after it never reach the orders
                    if (distribucion.IdTipoInstrumento.equals("FFM")) {
                        solicitudesAFFMM.add(copia);
                        break;
                    } else if (distribucion.IdTipoInstrumento.equals("ACN")) {
                        solicitudesAOrdenes.add(copia);
                    }
                }
            }
        }

        // 3 FONDOS MUTUOS
        if (!solicitudesAFFMM.isEmpty()) {
            movimientos.fondosMutuos(c, solicitudesAFFMM, distribucionCSC, distribucionSAPV, monedaSistema);
        }

        // 4 ORDENES
        if (!solicitudesAOrdenes.isEmpty()) {
            movimientos.ordenes(c, solicitudesAOrdenes, distribucionSAPV);
        }

        out.P_OUT_SOLICITUDES_PROCESADAS.addAll(recaudacionProcesadaInversion);
        return out;
    }

    // ------------------------------------------------------------------------------------------------ helpers

    /** UF value of the business date: CalcularParidad of 1 UF to CLP (the first calculation's FactorOrigenDestino). */
    private BigDecimal valorUf(EntradaRecaudacionTraspaso entrada, String fechaSistema) {
        CalcularParidad.CalcularParidadEntrada paridad = new CalcularParidad.CalcularParidadEntrada();
        paridad.p_IdEmpresa = entrada.p_IdEmpresa;
        paridad.p_IdTrabajo = 1L;
        CalcularParidad.CalculoEntrada uf = new CalcularParidad.CalculoEntrada();
        uf.IdTrabajo = 2L;
        uf.IdInterno = 1L;
        uf.Fecha = fechaSistema;
        uf.IdMonedaOrigen = "UF";
        uf.IdMonedaDestino = "CLP";
        uf.ValorCambioLocalModificado = BigDecimal.ZERO;
        uf.MontoOrigen = BigDecimal.ONE;
        uf.IdUso = "";
        paridad.p_CalculosEntrada.add(uf);
        return CalcularParidad.invocar(colaboradores, paridad).p_out_CalculosSalida.get(0).FactorOrigenDestino;
    }

    private boolean hayErroresBasicos(EntradaRecaudacionTraspaso entrada) {
        RequisitosBasicos.Salida basicos = RequisitosBasicos.revisar(colaboradores, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo);
        return MensajeSalida.contarErrores(basicos.p_out_Mensajes) > 0;
    }

    /** A request of a CSC fund contract (folio not "0" nor empty); otherwise it belongs to a SAAPV contract. */
    private static boolean esCsc(RegistroRecaudacion solicitud) {
        return !(solicitud.FolioCSC.equals("0") || solicitud.FolioCSC.equals(""));
    }

    private List<ContratoSuscripcionCuotaDetalle> contratosCsc(List<String> folios) throws SQLException {
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("folios", folios);
        return sql.consultar(SQL_CSC_DETALLE, parametros, ContratoSuscripcionCuotaDetalle.class);
    }

    private List<DistribucionInversion> distribucionSapv(List<String> folios) throws SQLException {
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("foliosAPV", folios);
        return sql.consultar(SQL_DISTRIBUCION_SAAPV, parametros, DistribucionInversion.class);
    }

    /**
     * Adjusts MontoPesos of the LAST line of every contract so the lines add up to the contract's distribution amount (the others are rounded to whole pesos).
     * {@code soloSiPositivo}: the second adjustment keeps the line's own amount when the difference would not be positive; the first one has no guard.
     * The running total is shared across contracts and reset on every last line, as in the legacy.
     */
    private void ajustarUltimaLinea(List<DistribucionInversion> distribucion, boolean soloSiPositivo) throws SQLException {
        BigDecimal montoPesosTotal = new BigDecimal(0);
        Map<String, List<DistribucionInversion>> lineasPorFolio = new HashMap<>();
        for (DistribucionInversion linea : distribucion) {
            List<DistribucionInversion> parcial = lineasPorFolio.get(linea.FolioContratoSAAPV);
            if (parcial == null) {
                parcial = distribucionSapv(Collections.singletonList(linea.FolioContratoSAAPV));
                lineasPorFolio.put(linea.FolioContratoSAAPV, parcial);
            }
            for (int j = 0; j < parcial.size(); j++) {
                if (parcial.get(j).FolioContratoSAAPV.equalsIgnoreCase(linea.FolioContratoSAAPV) && parcial.get(j).Nemotecnico.equalsIgnoreCase(linea.Nemotecnico)) {
                    if (j == parcial.size() - 1) {
                        BigDecimal diferencia = linea.MontoDistribucion.subtract(montoPesosTotal);
                        linea.MontoPesos = soloSiPositivo ? (diferencia.compareTo(BigDecimal.ZERO) > 0 ? diferencia : linea.MontoPesos) : diferencia;
                        montoPesosTotal = new BigDecimal(0);
                    } else {
                        montoPesosTotal = montoPesosTotal.add(parcial.get(j).MontoPesos.setScale(0, RoundingMode.HALF_UP));
                    }
                }
            }
        }
    }
}
