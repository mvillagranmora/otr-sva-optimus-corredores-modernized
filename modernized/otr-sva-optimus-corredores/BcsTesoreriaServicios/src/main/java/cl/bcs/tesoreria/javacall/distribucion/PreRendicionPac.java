package cl.bcs.tesoreria.javacall.distribucion;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import cl.bcs.tesoreria.javacall.distribucion.Filas.DiaDescuento;
import cl.bcs.tesoreria.javacall.distribucion.Filas.LogValidarMandatosPAC;
import cl.bcs.tesoreria.javacall.kit.MensajeSalida;
import cl.bcs.tesoreria.javacall.kit.Plataforma;
import cl.bcs.tesoreria.javacall.kit.SqlNombrado;
import cl.bcs.tesoreria.port.out.DiccionarioMensajes;
import cl.bcs.utils.Fecha;

/**
 * Pre-rendition of the PAC load file: crosses the file rows (CAR.CargaMasivaDetallePAC) with the mandates and contracts, assembles the requests that can be
 * distributed and writes the PAC.LogMandatoPAC trail. Legacy {@code ProcesarDistribucion.ValidaPreRendicion} and the helpers {@code ProcesarDistribucion.logica}
 * shares with it (TES-B-277..321: the messages, the 500+ request numbers and the HashMap order of the groups are pinned by those cases).
 */
@Component
public class PreRendicionPac {

    private static final Logger log = LoggerFactory.getLogger(PreRendicionPac.class);

    private static final String SQL_MANDATOS_Y_ARCHIVO_PRE_REND = SqlNombrado.recurso("ProcesarDistribucion_ConsultarMandatosPacYArchivoPreRend");
    private static final String SQL_MANDATOS_PAC_PRE_REND = SqlNombrado.recurso("ProcesarDistribucion_ConsultarMandatosPacPreRend");
    private static final String SQL_MANDATOS_PAC_PRE_REND_DETALLE_ARCHIVO = SqlNombrado.recurso("ProcesarDistribucion_ConsultarMandatosPacPreRendDetalleArchivo");
    private static final String SQL_SELECT_LOG = SqlNombrado.recurso("ProcesarDistribucion_SELECT_LogMandatosPAC");
    private static final String SQL_INSERT_LOG = SqlNombrado.recurso("ProcesarDistribucion_INSERT_LogMandatosPAC");
    private static final String SQL_UPDATE_LOG = SqlNombrado.recurso("ProcesarDistribucion_UPDATE_LogMandatosPAC");
    private static final String SQL_DELETE_LOG = SqlNombrado.recurso("ProcesarDistribucion_DELETE_LogMandatosPAC");
    private static final String SQL_DIA_DESCUENTO = SqlNombrado.recurso("ProcesarDistribucion_ConsultarDiaDescuento");
    private static final String SQL_RECHAZADOS_PRE_REND = SqlNombrado.recurso("ProcesarDistribucion_SELECT_RechazadosPreRen");

    private final SqlNombrado sql;
    private final DiccionarioMensajes diccionario;

    public PreRendicionPac(NamedParameterJdbcTemplate jdbc, DiccionarioMensajes diccionario) {
        this.sql = new SqlNombrado(jdbc);
        this.diccionario = diccionario;
    }

    // ------------------------------------------------------------------------------------------------ ValidaPreRendicion

    /** Legacy {@code ValidaPreRendicion} after the basic requirements: validates, assembles the requests and writes the log (TES-B-309..318). */
    public void validar(Corrida c) throws Exception {
        SalidaRecaudacionTraspaso out = c.out;
        Long idUsuario = c.entrada.p_IdUsuario;
        Map<String, Object> params = new HashMap<>();

        List<InRecaudacion> registrosArchivo = sql.consultar(SQL_MANDATOS_Y_ARCHIVO_PRE_REND, params, InRecaudacion.class);
        List<InRecaudacion> registrosDetalleArchivo = sql.consultar(SQL_MANDATOS_PAC_PRE_REND_DETALLE_ARCHIVO, params, InRecaudacion.class);
        List<InRecaudacion> cruceArchivoMandatos = validacionSolicitudesArchivo(c, registrosArchivo, registrosDetalleArchivo);

        List<InRecaudacion> traspasos = sql.consultar(SQL_MANDATOS_PAC_PRE_REND, params, InRecaudacion.class);
        if (!cruceArchivoMandatos.isEmpty() && !traspasos.isEmpty()) {
            validaArchivoContraMandatos(c, cruceArchivoMandatos, traspasos);
        }
        if (traspasos.isEmpty()) {
            archivoNoCoincide(out);
            return;
        }

        List<RegistroRecaudacion> solicitudesProcesadas = solicitudesAProcesar(c, traspasos, cruceArchivoMandatos);
        List<RegistroRecaudacion> solicitudProcesar = solicitudesValidas(c, solicitudesProcesadas);
        solicitudProcesar.addAll(out.P_OUT_SOLICITUDES_NO_PROCESADAS);

        // the placeholder rows (IdMandatoPAC = -1) of the previous runs are removed before the new ones are written
        sql.lote(SQL_DELETE_LOG, new ArrayList<InRecaudacion>());
        List<DiaDescuento> diasDescuento = sql.consultar(SQL_DIA_DESCUENTO, params, DiaDescuento.class);
        String diaDescuento = "5";
        if (!diasDescuento.isEmpty()) {
            diaDescuento = diasDescuento.get(0).DiaDescuento().toString();
        }

        params.put("EstadoProcesamiento", "PEN");
        params.put("fechaSistema", c.fechaSistema);
        params.put("DiaDescuento", diaDescuento);
        List<InRecaudacion> mandatosLogExistentes = sql.consultar(SQL_SELECT_LOG, params, InRecaudacion.class);
        params.clear();
        params.put("EstadoProcesamiento", "PRO");
        params.put("fechaSistema", c.fechaSistema);
        params.put("DiaDescuento", diaDescuento);
        List<InRecaudacion> mandatosLogExistentesProc = sql.consultar(SQL_SELECT_LOG, params, InRecaudacion.class);

        Map<Object, List<InRecaudacion>> conLog = mandatosLogExistentes.stream().collect(Collectors.groupingBy(t -> t.IdentServicio));
        Map<Object, List<InRecaudacion>> conLogProcesado = mandatosLogExistentesProc.stream().collect(Collectors.groupingBy(t -> t.IdentServicio));

        solicitudProcesar = marcarProcesados(solicitudProcesar, conLogProcesado);

        List<RegistroRecaudacion> logUpdate = solicitudProcesar.stream()
                .filter(r -> conLog.containsKey(r.IdentServicio))
                .filter(r -> r.IdMandatoPAC != -1)
                .collect(Collectors.toList());
        List<RegistroRecaudacion> logInsert = solicitudProcesar.stream()
                .filter(r -> !conLog.containsKey(r.IdentServicio))
                .filter(r -> !conLogProcesado.containsKey(r.IdentServicio))
                .filter(r -> r.IdMandatoPAC != -1)
                .collect(Collectors.toList());
        List<RegistroRecaudacion> logInsertErrores = solicitudProcesar.stream()
                .filter(r -> r.IdMandatoPAC == -1)
                .collect(Collectors.toList());

        if (!logInsert.isEmpty()) {
            insertarLog(logInsert, Fecha.toDate(c.fechaSistema), idUsuario);
        }
        if (!logInsertErrores.isEmpty()) {
            insertarLog(logInsertErrores, Fecha.toDate(c.fechaSistema), idUsuario);
        }
        if (!logUpdate.isEmpty()) {
            actualizarLog(logUpdate, Fecha.toDate(c.fechaSistema), idUsuario);
        }

        for (int i = solicitudProcesar.size() - 1; i >= 0; i--) {
            if (!"N".equalsIgnoreCase(solicitudProcesar.get(i).EstadoProcesoRendicion)) {
                out.P_OUT_SOLICITUDES_PROCESADAS.add(solicitudProcesar.get(i));
            }
        }
    }

    /** The log of the mandates the load rejected today, as error messages (legacy {@code agregarLogsPreRendicion}; an SQL failure is swallowed, as the legacy did). */
    public void agregarLogsPreRendicion(Corrida c) throws Exception {
        try {
            List<LogValidarMandatosPAC> rechazados = sql.consultar(SQL_RECHAZADOS_PRE_REND, new HashMap<String, Object>(), LogValidarMandatosPAC.class);
            for (LogValidarMandatosPAC rechazado : rechazados) {
                Map<String, String> valores = new HashMap<>();
                valores.put("Glosa", rechazado.Glosa);
                MensajeSalida msg = new MensajeSalida();
                msg.id = rechazado.IdMandatoPAC;
                msg.tipo = MensajeSalida.MENSAJE_ERROR;
                msg.codigo = "MANDATO_RECHAZADO_PRERENDICION";
                msg.descripcion = diccionario.mensaje(c.entrada.p_IdEmpresa, "UI_MANDATO_RECHAZADO_PRERENDICION", valores);
                c.out.P_OUT_MENSAJES.add(msg);
            }
        } catch (SQLException e) {
            // LEGACY: only the SQLException is swallowed (printStackTrace); the rejected-mandates messages are then simply missing
            log.warn("ProcesarDistribucion: rejected mandates of the pre-rendition could not be read", e);
        }
    }

    // ------------------------------------------------------------------------------------------------ file vs mandates

    /** Legacy {@code ValidacionSolicitudesArchivo(archivo, fechaSistema, ...)}: the load-time variant (one error per file row). Returns the rows without error. */
    public List<InRecaudacion> validacionSolicitudesArchivo(Corrida c, List<InRecaudacion> solicitudesArchivo) {
        List<InRecaudacion> sinError = new ArrayList<>();
        Long contador = 500L;
        for (InRecaudacion registro : solicitudesArchivo) {
            if (registro.Observacion.equalsIgnoreCase("N")) {
                rechazarPorArchivo(c, registro, contador, "Mandato informado no existe.");
                contador++;
            } else if (!registro.EstadoCargoBanco.equalsIgnoreCase("3")) {
                rechazarPorArchivo(c, registro, contador, "IdentServicio: " + registro.IdentServicio + " con codigo de estado cargo rechazado");
                contador++;
            } else if (!registro.EstadoCargo.equalsIgnoreCase("ACT")) {
                rechazarPorArchivo(c, registro, contador,
                        "IdentServicio: " + registro.IdentServicio + " con codigo de estado cargo rechazado o estado PAC distinto de ingresado. Para el cliente: " + registro.DocumentoIdentidad);
                contador++;
            } else {
                sinError.add(registro);
            }
        }
        return sinError;
    }

    private void rechazarPorArchivo(Corrida c, InRecaudacion registro, Long contador, String descripcion) {
        RegistroRecaudacion noProcesado = registroDe(c, registro, contador, BigDecimal.ZERO);
        noProcesado.EstadoRendicion = descripcion;
        noProcesado.EstadoProcesoRendicion = "N";
        noProcesado.IdentServicio = registro.IdentServicio;
        noProcesado.IdMandatoPAC = -1L;
        c.out.P_OUT_ERRORESCONTADOR++;
        c.out.P_OUT_MENSAJES.add(new MensajeSalida(noProcesado.IdMandatoPAC, MensajeSalida.MENSAJE_ERROR, noProcesado.EstadoProcesoRendicion, noProcesado.EstadoRendicion));
        c.out.P_OUT_SOLICITUDES_NO_PROCESADAS.add(noProcesado);
    }

    /**
     * Legacy {@code ValidacionSolicitudesArchivo(archivo, detalle, ...)}: the pre-rendition variant. A rejected file row is reported once per mandate detail of
     * its IdentServicio (or once when it has none); the error counter grows once per file row.
     */
    List<InRecaudacion> validacionSolicitudesArchivo(Corrida c, List<InRecaudacion> solicitudesArchivo, List<InRecaudacion> detalle) {
        List<InRecaudacion> sinError = new ArrayList<>();
        // LEGACY DEFECT kept (see README TES-B-312): an orphan file row has no mandate, its IdentServicio is null and groupingBy throws NullPointerException
        Map<String, List<InRecaudacion>> detallePorServicio = detalle.stream().collect(Collectors.groupingBy(t -> t.IdentServicio));
        Long contador = 500L;
        for (InRecaudacion registro : solicitudesArchivo) {
            if (registro.Observacion.equalsIgnoreCase("N")) {
                rechazarPorArchivo(c, registro, contador, "Mandato informado no existe.");
                contador++;
            } else if (!registro.EstadoCargoBanco.equalsIgnoreCase("3")) {
                rechazarPorDetalle(c, registro, detallePorServicio.get(registro.IdentServicio), contador, false);
                // LEGACY DEFECT kept (see README TES-B-313): the error counter is raised here too, although rows were reported by detail
                c.out.P_OUT_ERRORESCONTADOR++;
                contador++;
            } else if (!registro.EstadoCargo.equalsIgnoreCase("ACT")) {
                rechazarPorDetalle(c, registro, detallePorServicio.get(registro.IdentServicio), contador, true);
                c.out.P_OUT_ERRORESCONTADOR++;
                contador++;
            } else {
                sinError.add(registro);
            }
        }
        return sinError;
    }

    private void rechazarPorDetalle(Corrida c, InRecaudacion registro, List<InRecaudacion> detalleArchivo, Long contador, boolean conCliente) {
        if (detalleArchivo != null && !detalleArchivo.isEmpty()) {
            for (InRecaudacion d : detalleArchivo) {
                agregarRechazoDetalle(c, d, contador, conCliente);
            }
        } else {
            agregarRechazoDetalle(c, registro, contador, conCliente);
        }
    }

    private void agregarRechazoDetalle(Corrida c, InRecaudacion fuente, Long contador, boolean conCliente) {
        RegistroRecaudacion noProcesado = registroDe(c, fuente, contador, BigDecimal.ZERO);
        noProcesado.EstadoRendicion = "IdentServicio: " + fuente.IdentServicio + " con codigo de estado cargo rechazado"
                + (conCliente ? " o estado PAC distinto de ingresado. Para el cliente: " + fuente.DocumentoIdentidad : "");
        noProcesado.EstadoProcesoRendicion = "N";
        noProcesado.IdentServicio = fuente.IdentServicio;
        noProcesado.IdMandatoPAC = -1L;
        c.out.P_OUT_MENSAJES.add(new MensajeSalida(noProcesado.IdMandatoPAC, MensajeSalida.MENSAJE_ERROR, noProcesado.EstadoProcesoRendicion, noProcesado.EstadoRendicion));
        c.out.P_OUT_SOLICITUDES_NO_PROCESADAS.add(noProcesado);
    }

    /** Legacy {@code ValidaArchivoContraMandatos}: file rows whose IdentServicio has no mandate in the join are reported as badly configured. */
    public void validaArchivoContraMandatos(Corrida c, List<InRecaudacion> solicitudesArchivo, List<InRecaudacion> mandatos) {
        Map<String, List<InRecaudacion>> mandatosPorServicio = mandatos.stream().collect(Collectors.groupingBy(t -> t.IdentServicio));
        List<InRecaudacion> descartadas = solicitudesArchivo.stream().filter(a -> !mandatosPorServicio.containsKey(a.IdentServicio)).collect(Collectors.toList());
        for (InRecaudacion solicitud : descartadas) {
            RegistroRecaudacion noProcesado = new RegistroRecaudacion();
            noProcesado.EstadoRendicion = "Mandato o Contrato se encuentra mal configurado, se encuentra anulado o no se encuentra activo. Para el cliente: " + noProcesado.DocumentoIdentidad;
            noProcesado.EstadoProcesoRendicion = "N";
            noProcesado.IdentServicio = solicitud.IdentServicio;
            noProcesado.IdMandatoPAC = (solicitud.IdMandatoPAC != null) ? solicitud.IdMandatoPAC : -1L;
            c.out.P_OUT_ERRORESCONTADOR++;
            c.out.P_OUT_MENSAJES.add(new MensajeSalida(noProcesado.IdMandatoPAC, MensajeSalida.MENSAJE_ERROR, noProcesado.EstadoProcesoRendicion, noProcesado.EstadoRendicion));
            c.out.P_OUT_SOLICITUDES_NO_PROCESADAS.add(noProcesado);
        }
    }

    /** The response of a run whose mandate query is empty. */
    public void archivoNoCoincide(SalidaRecaudacionTraspaso out) {
        out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "EL ARCHIVO DE CARGA NO COINCIDE INFORMACION EXISTENTE", "EL ARCHIVO DE CARGA NO COINCIDE INFORMACION EXISTENTE"));
        out.P_OUT_ERRORESCONTADOR += 1L;
    }

    // ------------------------------------------------------------------------------------------------ requests

    /**
     * Legacy {@code getSolicitudAProcesarPreRendicion}: groups the mandates by IdentServicio, compares the file deposit with the CSC / SAAPV amounts and
     * assembles one request per contract (numbered from 500 in iteration order).
     */
    public List<RegistroRecaudacion> solicitudesAProcesar(Corrida c, List<InRecaudacion> traspasos, List<InRecaudacion> traspasosArchivo) {
        Long contador = 500L;
        List<RegistroRecaudacion> solicitudTesoreria = new ArrayList<>();

        // The groups are iterated in java.util.HashMap order (String.hashCode buckets), exactly as the legacy did: the request numbers (IdTraspaso 500, 501, ...)
        // and the order of the collaborator calls depend on it, and cases 306 and 308 pin it. Do not replace this map by an ordered one.
        Map<String, List<InRecaudacion>> grupos = traspasos.stream().collect(Collectors.groupingBy(t -> t.IdentServicio));

        for (Map.Entry<String, List<InRecaudacion>> grupo : grupos.entrySet()) {
            boolean soloSAAPV = false;
            boolean soloCSC = false;
            List<InRecaudacion> delServicio = grupo.getValue();
            List<InRecaudacion> delServicioArchivo = traspasosArchivo.stream().filter(k -> k.IdentServicio.equalsIgnoreCase(grupo.getKey())).collect(Collectors.toList());

            BigDecimal depositoSumado = delServicioArchivo.stream().map(r -> r.MontoDeposito).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal montoSumado = delServicio.stream().map(k -> k.MontoCSC.add(k.MontoSAPV, MathContext.DECIMAL128)).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal montoSumadoSAAPV = delServicio.stream().map(k -> k.MontoSAPV).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal montoSumadoCSC = delServicio.stream().map(k -> k.MontoCSC).reduce(BigDecimal.ZERO, BigDecimal::add);

            if (depositoSumado.compareTo(montoSumadoCSC) == 0) {
                soloCSC = true;
            }
            if (depositoSumado.compareTo(montoSumadoSAAPV) == 0) {
                soloSAAPV = true;
                soloCSC = false;
            }
            if (depositoSumado.compareTo(montoSumado) != 0 && !soloSAAPV && !soloCSC) {
                // LEGACY DEFECT kept (see README TES-B-293): every request of the group is numbered 500 (the inner counter is never incremented)
                Long contadorRegistro = 500L;
                for (InRecaudacion solicitud : delServicio) {
                    RegistroRecaudacion noProcesado = registroDe(c, solicitud, contadorRegistro, montoSumado);
                    noProcesado.EstadoRendicion = "Monto mandato no corresponde a SAAPV ni CSC." + noProcesado.DocumentoIdentidad;
                    noProcesado.EstadoProcesoRendicion = "N";
                    noProcesado.IdentServicio = solicitud.IdentServicio;
                    noProcesado.IdMandatoPAC = -1L;
                    c.out.P_OUT_ERRORESCONTADOR++;
                    c.out.P_OUT_MENSAJES.add(new MensajeSalida(noProcesado.IdMandatoPAC, MensajeSalida.MENSAJE_ERROR, noProcesado.EstadoProcesoRendicion, noProcesado.EstadoRendicion));
                    c.out.P_OUT_SOLICITUDES_NO_PROCESADAS.add(noProcesado);
                    contador++;
                }
            } else {
                List<InRecaudacion> traspasosSAPV = delServicio.stream().filter(k -> k.MontoSAPV.compareTo(BigDecimal.ZERO) == 1).collect(Collectors.toList());
                List<InRecaudacion> traspasosCSC = delServicio.stream().filter(k -> k.MontoCSC.compareTo(BigDecimal.ZERO) == 1).collect(Collectors.toList());
                if (soloSAAPV || depositoSumado.compareTo(montoSumado) == 0) {
                    for (InRecaudacion in : traspasosSAPV) {
                        solicitudTesoreria.add(registroDe(c, in, contador, depositoSumado));
                        contador++;
                    }
                }
                if (soloCSC || depositoSumado.compareTo(montoSumado) == 0) {
                    for (InRecaudacion in : traspasosCSC) {
                        solicitudTesoreria.add(registroDe(c, in, contador, depositoSumado));
                        contador++;
                    }
                }
            }
        }
        return solicitudTesoreria;
    }

    /** Legacy {@code obtenerRegistroRecaudacion}: the request of a mandate row (UF amounts are converted with the UF value of the run); records the contract folio. */
    RegistroRecaudacion registroDe(Corrida c, InRecaudacion in, Long contador, BigDecimal montoSumado) {
        RegistroRecaudacion reg = new RegistroRecaudacion();

        reg.FechaDeposito = Fecha.toDate(c.fechaSistema);
        reg.IdBanco = 1L;
        reg.IdMedioDePago = "EFE";
        reg.IdTraspaso = contador;
        if (in.MontoCSC.compareTo(BigDecimal.ZERO) == 0) {
            reg.MontoDeposito = in.MonedaSAPV.equalsIgnoreCase("UF") ? in.MontoSAPV.multiply(c.valorUF) : in.MontoSAPV;
            reg.Moneda = in.MonedaSAPV;
        } else {
            reg.MontoDeposito = in.MonedaCSC.equalsIgnoreCase("UF") ? in.MontoCSC.multiply(c.valorUF) : in.MontoCSC;
            reg.Moneda = in.MonedaCSC;
        }

        reg.SumaTotal = reg.Moneda.equalsIgnoreCase("UF") ? montoSumado.multiply(c.valorUF) : montoSumado;
        reg.NumeroCuentaBancaria = 0L;
        reg.IdCuentaBanco = 0L;
        reg.Observacion = "";
        reg.Sucursal = "";
        reg.EstadoProceso = Constantes.ESTADO_PROCESADO;
        reg.IdEmpresa = c.entrada.p_IdEmpresa;
        reg.IdUsuario = c.entrada.p_IdUsuario;
        reg.IdEstado = Constantes.ESTADO_ACTIVO;
        reg.FechaIngreso = Fecha.toDate(c.fechaSistema);
        reg.FechaAceptacion = Fecha.toDate(c.fechaSistema);
        reg.IdCliente = in.IdCliente;
        reg.IdPortafolio = in.IdPortafolio;
        reg.IdPersonaEmisor = 1L;

        reg.IdMandatoPAC = in.IdMandatoPAC;
        reg.EstadoPAC = in.EstadoPAC;
        reg.NombreCompleto = in.NombreCompleto;
        reg.DocumentoIdentidad = in.DocumentoIdentidad;
        reg.EstadoCliente = in.EstadoCliente;
        reg.IdentServicio = in.IdentServicio;
        reg.EstadoPortafolio = in.EstadoPortafolio;
        reg.EstadoCargo = in.EstadoCargo;
        reg.DescripcionEstadoCargo = in.DescripcionEstadoCargo;
        reg.EstadoCargoBanco = in.EstadoCargoBanco;
        reg.EstadoProcesoRendicion = "OK";
        reg.EstadoCSC = in.EstadoCSC;
        reg.EstadoLargoCSC = in.EstadoLargoCSC;
        reg.EstadoSAAPV = in.EstadoSAAPV;
        reg.EstadoLargoSAAPV = in.EstadoLargoSAAPV;
        reg.NombreBanco = in.NombreBanco;
        reg.CuentaBancaria = in.CuentaBancaria;
        reg.CodigoPortafolio = in.CodigoPortafolio;
        reg.FechaSuscripcion = in.FechaSuscripcion == null ? Fecha.toDate("0001-01-01") : in.FechaSuscripcion;
        reg.DiaDescuento = in.DiaDescuento;
        reg.IdMandatoPACUI = in.IdMandatoPACUI;

        // LEGACY DEFECT kept (see README TES-B-290): a file row (no FolioCSC column) has the default "" folio, so it is taken for a CSC contract and "" is collected
        if (!in.FolioCSC.equalsIgnoreCase("0")) {
            reg.IdConcepto = Constantes.ABNCSC;
            reg.FolioCSC = in.FolioCSC;
            c.foliosCSC.add(in.FolioCSC);
        } else if (!in.FolioSAAPV.equalsIgnoreCase("0")) {
            reg.IdConcepto = Constantes.ABNAPV;
            reg.FolioAPV = in.FolioSAAPV;
            c.foliosAPV.add(in.FolioSAAPV);
        }
        return reg;
    }

    /**
     * Legacy {@code getSolicitudesValidas}: the requests of mandates in a usable state (copied to a reduced request); the others are reported as not processed.
     */
    public List<RegistroRecaudacion> solicitudesValidas(Corrida c, List<RegistroRecaudacion> todas) {
        SalidaRecaudacionTraspaso out = c.out;
        List<RegistroRecaudacion> soloParaTesoreria = new ArrayList<>();
        for (RegistroRecaudacion registros : todas) {
            // LEGACY DEFECT kept (see README TES-B-296): the "RVC" test is unreachable through the SQL (revoked contracts do not join) and the SAAPV "REV" guard is bypassed by an RVC CSC
            if (registros.EstadoPAC.equals("ING") && registros.EstadoCargo.equals("ACT")
                    && (!"REV".equalsIgnoreCase(registros.EstadoSAAPV) || "RVC".equalsIgnoreCase(registros.EstadoCSC))) {
                RegistroRecaudacion reg = new RegistroRecaudacion();
                reg.FechaDeposito = registros.FechaDeposito;
                reg.IdBanco = registros.IdBanco;
                reg.IdMedioDePago = registros.IdMedioDePago;
                reg.IdTraspaso = registros.IdTraspaso;
                reg.MontoDeposito = registros.MontoDeposito;
                reg.SumaTotal = registros.SumaTotal;
                reg.NumeroCuentaBancaria = registros.NumeroCuentaBancaria;
                reg.IdCuentaBanco = registros.IdCuentaBanco;
                reg.Observacion = registros.Observacion;
                reg.Sucursal = registros.Sucursal;
                reg.EstadoProceso = registros.EstadoProceso;
                reg.IdEmpresa = registros.IdEmpresa;
                reg.IdUsuario = registros.IdUsuario;
                reg.IdEstado = registros.IdEstado;
                reg.FechaIngreso = registros.FechaIngreso;
                reg.FechaAceptacion = registros.FechaAceptacion;
                reg.IdCliente = registros.IdCliente;
                reg.IdPortafolio = registros.IdPortafolio;
                reg.IdPersonaEmisor = registros.IdPersonaEmisor;
                reg.IdMandatoPAC = registros.IdMandatoPAC;
                reg.EstadoPAC = registros.EstadoPAC;
                reg.NombreCompleto = registros.NombreCompleto;
                reg.DocumentoIdentidad = registros.DocumentoIdentidad;
                reg.EstadoCliente = registros.EstadoCliente;
                reg.IdentServicio = registros.IdentServicio;
                reg.EstadoPortafolio = registros.EstadoPortafolio;
                reg.EstadoCargo = registros.EstadoCargo;
                reg.DescripcionEstadoCargo = registros.DescripcionEstadoCargo;
                reg.FolioAPV = registros.FolioAPV;
                reg.FolioCSC = registros.FolioCSC;
                reg.EstadoCargoBanco = registros.EstadoCargoBanco;
                reg.IdConcepto = registros.IdConcepto;
                reg.DescripccionConcepto = registros.DescripccionConcepto;
                reg.EstadoCSC = registros.EstadoCSC;
                reg.EstadoLargoCSC = registros.EstadoLargoCSC;
                reg.EstadoSAAPV = registros.EstadoSAAPV;
                reg.EstadoLargoSAAPV = registros.EstadoLargoSAAPV;
                reg.NombreBanco = registros.NombreBanco;
                reg.CuentaBancaria = registros.CuentaBancaria;
                reg.CodigoPortafolio = registros.CodigoPortafolio;
                reg.FechaSuscripcion = registros.FechaSuscripcion == null ? Fecha.toDate("0001-01-01") : registros.FechaSuscripcion;
                reg.DiaDescuento = registros.DiaDescuento;
                reg.IdMandatoPACUI = registros.IdMandatoPACUI;
                reg.EstadoRendicion = "Mandato OK - Pendiente de procesar";
                soloParaTesoreria.add(reg);
            } else if (!registros.EstadoPAC.equals("ING")) {
                noProcesada(out, registros, "El contrato PAC: +" + registros.IdMandatoPAC + "+ no tiene un estado valido.", registros.EstadoProcesoRendicion);
            } else if (!registros.EstadoCargo.equals("ACT")) {
                noProcesada(out, registros, "Debe cargar el archivo universo, ya que el Estado Cargo del Mandato: " + registros.IdMandatoPAC + " es INA.", registros.EstadoProcesoRendicion);
            } else if ("REV".equalsIgnoreCase(registros.EstadoSAAPV)) {
                noProcesada(out, registros, "El estado del Contrato SAAPV: Folio Contrato " + registros.FolioAPV + ", se encuentra en estado: " + registros.EstadoLargoSAAPV
                        + ". Para el cliente:" + registros.DocumentoIdentidad, registros.EstadoSAAPV);
            } else if ("RVC".equalsIgnoreCase(registros.EstadoCSC)) {
                noProcesada(out, registros, "El estado del Contrato CSC: Folio CSC " + registros.FolioCSC + ", se encuentra en estado: " + registros.EstadoLargoCSC
                        + ". Para el cliente:" + registros.DocumentoIdentidad, registros.EstadoCargoBanco);
            }
        }
        return soloParaTesoreria;
    }

    private void noProcesada(SalidaRecaudacionTraspaso out, RegistroRecaudacion registros, String descripcion, String codigoMensaje) {
        RegistroRecaudacion noProcesado = copiar(registros);
        noProcesado.EstadoRendicion = descripcion;
        noProcesado.EstadoProcesoRendicion = "N";
        noProcesado.IdentServicio = registros.IdentServicio;
        noProcesado.IdMandatoPAC = registros.IdMandatoPAC;
        out.P_OUT_ERRORESCONTADOR++;
        out.P_OUT_MENSAJES.add(new MensajeSalida(noProcesado.IdMandatoPAC, MensajeSalida.MENSAJE_ERROR, codigoMensaje, noProcesado.EstadoRendicion));
        out.P_OUT_SOLICITUDES_NO_PROCESADAS.add(noProcesado);
    }

    /** Legacy {@code copiarRecaudacion}: the reduced copy of a request used by every step (it does not carry Moneda, the contract states, the folios of the movements...). */
    public RegistroRecaudacion copiar(RegistroRecaudacion r) {
        RegistroRecaudacion copia = new RegistroRecaudacion();
        copia.IdTraspaso = r.IdTraspaso;
        copia.FechaDeposito = r.FechaDeposito;
        copia.IdMedioDePago = r.IdMedioDePago;
        copia.IdBanco = r.IdBanco;
        copia.NumeroCuentaBancaria = r.NumeroCuentaBancaria;
        copia.IdCuentaBanco = r.IdCuentaBanco;
        copia.Sucursal = r.Sucursal;
        copia.MontoDeposito = r.MontoDeposito;
        copia.SumaTotal = r.SumaTotal;
        copia.Observacion = r.Observacion;

        copia.IdSerieFondo = r.IdSerieFondo;
        copia.IdCliente = r.IdCliente;
        copia.IdPortafolio = r.IdPortafolio;
        copia.EstadoProceso = r.EstadoProceso;
        copia.Codigo = r.Codigo;
        copia.IdRecaudacion = r.IdRecaudacion;
        copia.IdEmpresa = r.IdEmpresa;
        copia.IdUsuario = r.IdUsuario;
        copia.IdEstado = r.IdEstado;
        copia.FechaIngreso = r.FechaIngreso;
        copia.FechaAceptacion = r.FechaAceptacion;
        copia.IdPersonaEmisor = r.IdPersonaEmisor;
        copia.IdConcepto = r.IdConcepto;

        copia.DescripccionConcepto = r.DescripccionConcepto;
        copia.FolioCSC = r.FolioCSC;
        copia.FolioAPV = r.FolioAPV;

        copia.IdMandatoPAC = r.IdMandatoPAC;
        copia.EstadoPAC = r.EstadoPAC;
        copia.NombreCompleto = r.NombreCompleto;
        copia.DocumentoIdentidad = r.DocumentoIdentidad;
        copia.EstadoCliente = r.EstadoCliente;
        copia.IdentServicio = r.IdentServicio;
        copia.EstadoPortafolio = r.EstadoPortafolio;
        copia.DescripcionSolicitud = r.DescripcionSolicitud;
        copia.EstadoCargo = r.EstadoCargo;
        copia.DescripcionEstadoCargo = r.DescripcionEstadoCargo;
        copia.EstadoRendicion = r.EstadoRendicion;
        copia.EstadoCargoBanco = r.EstadoCargoBanco;
        copia.EstadoProcesoRendicion = r.EstadoProcesoRendicion;
        copia.NombreBanco = r.NombreBanco;
        copia.CuentaBancaria = r.CuentaBancaria;
        copia.CodigoPortafolio = r.CodigoPortafolio;
        copia.FechaSuscripcion = r.FechaSuscripcion == null ? Fecha.toDate("0001-01-01") : r.FechaSuscripcion;
        copia.DiaDescuento = r.DiaDescuento;
        copia.IdMandatoPACUI = r.IdMandatoPACUI;
        return copia;
    }

    // ------------------------------------------------------------------------------------------------ PAC.LogMandatoPAC

    /** Legacy {@code SetEstadosProcesados}: a mandate with a processed (PRO) log of the month is reported as already processed (the same objects are updated). */
    private List<RegistroRecaudacion> marcarProcesados(List<RegistroRecaudacion> todas, Map<Object, List<InRecaudacion>> procesadasOk) {
        List<RegistroRecaudacion> corregidas = new ArrayList<>();
        for (RegistroRecaudacion reg : todas) {
            if (procesadasOk.containsKey(reg.IdentServicio) && reg.IdMandatoPAC != -1) {
                reg.EstadoRendicion = "Mandato se encuentra procesado";
                reg.EstadoProcesoRendicion = "S";
            }
            corregidas.add(reg);
        }
        return corregidas;
    }

    private void insertarLog(List<RegistroRecaudacion> solicitudes, java.sql.Date fechaSistema, Long idUsuario) throws SQLException {
        List<LogValidarMandatosPAC> log = new ArrayList<>();
        for (RegistroRecaudacion reg : solicitudes) {
            LogValidarMandatosPAC np = new LogValidarMandatosPAC();
            np.IdMandatoPAC = reg.IdMandatoPAC == null ? (long) -1 : reg.IdMandatoPAC;
            np.IdentServicio = reg.IdentServicio;
            np.FechaSistema = fechaSistema;
            np.Usuario = idUsuario.toString();
            np.PreRendicion = reg.EstadoProcesoRendicion.equalsIgnoreCase("N") ? "N" : "S";
            np.EstadoProcesamiento = "PEN";
            np.Glosa = reg.EstadoRendicion;
            log.add(np);
        }
        if (!log.isEmpty()) {
            sql.lote(SQL_INSERT_LOG, log);
        }
    }

    private void actualizarLog(List<RegistroRecaudacion> solicitudes, java.sql.Date fechaSistema, Long idUsuario) throws SQLException {
        List<LogValidarMandatosPAC> log = new ArrayList<>();
        for (RegistroRecaudacion reg : solicitudes) {
            LogValidarMandatosPAC np = new LogValidarMandatosPAC();
            np.IdentServicio = reg.IdentServicio;
            np.IdMandatoPAC = reg.IdMandatoPAC;
            np.FechaSistema = fechaSistema;
            np.Usuario = idUsuario.toString();
            np.PreRendicion = reg.EstadoProcesoRendicion.equalsIgnoreCase("N") ? "N" : "S";
            np.EstadoProcesamiento = "PEN";
            np.Glosa = reg.EstadoRendicion;
            log.add(np);
        }
        if (!log.isEmpty()) {
            sql.lote(SQL_UPDATE_LOG, log);
        }
    }
}
