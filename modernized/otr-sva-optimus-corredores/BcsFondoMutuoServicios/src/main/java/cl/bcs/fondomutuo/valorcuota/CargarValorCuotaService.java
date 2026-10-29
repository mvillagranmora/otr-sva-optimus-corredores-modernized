package cl.bcs.fondomutuo.valorcuota;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.cierre.FechaLegacy;
import cl.bcs.fondomutuo.cierre.SerieFondoSolicitudes;
import cl.bcs.fondomutuo.cierre.SeriesFondoPort;
import cl.bcs.fondomutuo.fechas.ObtenerDiaAnterior;
import cl.bcs.fondomutuo.fechas.ObtenerDiaAnterior.InObtenerDiaAnterior;
import cl.bcs.fondomutuo.kit.JsonLegacy;
import cl.bcs.fondomutuo.kit.MensajeSalida;
import cl.bcs.fondomutuo.kit.RevisarRequisitosBasicosSalida;
import cl.bcs.fondomutuo.port.out.http.LogPort;
import cl.bcs.fondomutuo.port.out.kit.ConfiguracionSistemaPort;
import cl.bcs.fondomutuo.port.out.kit.DiccionarioMensajesPort;
import cl.bcs.fondomutuo.port.out.kit.RevisarRequisitosBasicosPort;
import cl.bcs.fondomutuo.requisitos.ConteoErrores;
import cl.bcs.fondomutuo.requisitos.SecuenciaPort;
import cl.bcs.fondomutuo.requisitos.contrato.LogContrato;

/**
 * Legacy {@code cl.bcs.fondomutuo.CargarValorCuota.logica}, bug for bug (cards FM-CVC-01..14, defects D-F3A-16..23). No state: everything of a request lives in the locals
 * of {@link #logica}. It validates the batch and, ONLY when {@code p_SimularOperacion} is exactly {@code "N"}, writes in this order: (1) mark the previous loads
 * as not-last, (2) insert the history (ids from the sequence port, asked once AFTER step 1), (3) delete the current values of the series, (4) insert the current value
 * (the most recent date of the batch per serie), (5) one audit line. It never commits nor rolls back: the host decides by the error counter.
 */
public class CargarValorCuotaService implements CargarValorCuotaPort {

    static final String SECUENCIA_HISTORICO = "BSCL.FondoMutuo.BD.Secuencias::ValorCuotaHistorico";
    private static final String OK = "OK";
    private static final String ERR = "ERR";
    private static final String ADV = "ADV";

    private final RevisarRequisitosBasicosPort basicos;
    private final DiccionarioMensajesPort diccionario;
    private final ConfiguracionSistemaPort configuracion;
    private final SeriesFondoPort series;
    private final CargarValorCuotaDatos datos;
    private final CargarValorCuotaEscritura escritura;
    private final ObtenerDiaAnterior diaAnterior;
    private final SecuenciaPort secuencias;
    private final LogPort log;
    private static final Logger LOGGER = LoggerFactory.getLogger(CargarValorCuotaService.class);

    public CargarValorCuotaService(RevisarRequisitosBasicosPort basicos, DiccionarioMensajesPort diccionario, ConfiguracionSistemaPort configuracion, SeriesFondoPort series,
            CargarValorCuotaDatos datos, CargarValorCuotaEscritura escritura, ObtenerDiaAnterior diaAnterior, SecuenciaPort secuencias, LogPort log) {
        this.basicos = basicos;
        this.diccionario = diccionario;
        this.configuracion = configuracion;
        this.series = series;
        this.datos = datos;
        this.escritura = escritura;
        this.diaAnterior = diaAnterior;
        this.secuencias = secuencias;
        this.log = log;
    }

    @Override
    public CargarValorCuotaContrato.Salida logica(CargarValorCuotaContrato.Entrada entrada) throws Exception {
        CargarValorCuotaContrato.Salida out = new CargarValorCuotaContrato.Salida();

        // validaRequisitosBasicos
        RevisarRequisitosBasicosSalida requisitos = basicos.revisar(entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo);
        out.P_OUT_ERRORESCONTADOR = ConteoErrores.errores(requisitos.p_out_Mensajes);
        if (out.P_OUT_ERRORESCONTADOR > 0) {
            out.P_OUT_MENSAJES.addAll(requisitos.p_out_Mensajes);
        }
        // validaCantidadValoresCuota (a null list is a NullPointerException, as the legacy)
        if (entrada.p_ValorCuota.size() == 0) {
            out.P_OUT_ERRORESCONTADOR = 1L;
            out.P_OUT_MENSAJES.add(MensajeSalida.conTexto(0L, MensajeSalida.MENSAJE_ERROR, "NO_SE_INGRESARON_VALORES_CUOTA", new HashMap<String, String>(), diccionario, entrada.p_IdEmpresa));
        }
        if (out.P_OUT_ERRORESCONTADOR > 0) {
            return out;
        }

        // Conversion ISIN -> IdSerieFondo
        if ("S".equalsIgnoreCase(entrada.p_AllFunds)) {
            List<String> seriesEntrada = entrada.p_ValorCuota.stream().map(v -> v.IDSERIEFONDO).distinct().collect(Collectors.toList());
            List<CargarValorCuotaContrato.SerieFondoIsin> fondosConIsin = datos.seriesPorIsin(seriesEntrada);
            List<CargarValorCuotaContrato.ValorCuota> nuevos = new ArrayList<>();
            Map<String, CargarValorCuotaContrato.ValorCuota> agrupados = entrada.p_ValorCuota.stream().collect(Collectors.toMap(v -> v.IDSERIEFONDO, v -> v));
            for (CargarValorCuotaContrato.SerieFondoIsin fondo : fondosConIsin) {
                CargarValorCuotaContrato.ValorCuota nuevo = new CargarValorCuotaContrato.ValorCuota();
                CargarValorCuotaContrato.ValorCuota busqueda = agrupados.get(fondo.ISIN);
                nuevo.FECHACALCULO = busqueda.FECHACALCULO;
                nuevo.FECHACIERRE = busqueda.FECHACIERRE;
                nuevo.IDMONEDA = busqueda.IDMONEDA;
                nuevo.IDSERIEFONDO = fondo.IdSerieFondo;
                nuevo.ISIN = fondo.ISIN;
                nuevo.MENSAJEVALIDACION = busqueda.MENSAJEVALIDACION;
                nuevo.OBSERVACION = busqueda.OBSERVACION;
                nuevo.REGISTRO = busqueda.REGISTRO;
                nuevo.VALORCUOTA = busqueda.VALORCUOTA;
                nuevos.add(nuevo);
            }
            entrada.p_ValorCuota = nuevos;
        }

        out.P_OUT_RESULTADO = obtenerResultado(entrada);

        boolean escribio = false;
        boolean logOk = false;
        int nSeries = 0;
        int nFilas = 0;
        String rangoIds = "none";
        if ("N".equals(entrada.p_SimularOperacion)) {
            List<CargarValorCuotaContrato.Resultado> historico = obtenerValoresCuotaHistoricosInsertar(out.P_OUT_RESULTADO);
            // H-3: the lists of steps 1, 3 and 4 go in a fixed order (IdSerieFondo) so two concurrent loads lock the rows in the same order; the historico keeps its
            // position (the sequence ids are assigned by position) and the stored rows are the same
            List<CargarValorCuotaContrato.Resultado> vigentes = porSerie(obtenerValoresCuotaInsertar(out.P_OUT_RESULTADO));
            escritura.actualizarUltimaCarga(porSerie(historico));
            List<Long> ids = secuencias.siguientes(SECUENCIA_HISTORICO, (long) historico.size());
            if (ids.size() != historico.size()) {   // L-6: a clear 500 instead of an IndexOutOfBounds in the middle of the writes
                throw new IllegalStateException("the sequence port returned " + ids.size() + " ids for " + historico.size() + " history rows");
            }
            escritura.insertarHistorico(historico, ids);
            escritura.eliminarVigentes(vigentes);
            escritura.insertarVigentes(vigentes);
            logOk = agregarLog(entrada, historico);
            escribio = true;
            nSeries = vigentes.size();
            nFilas = historico.size();
            if (!ids.isEmpty()) rangoIds = ids.get(0) + ".." + ids.get(ids.size() - 1);
        }

        out.P_OUT_MENSAJES = obtenerMensajesSalida(out.P_OUT_RESULTADO);
        out.P_OUT_ERRORESCONTADOR = ConteoErrores.errores(out.P_OUT_MENSAJES);
        if (escribio) {
            // M-3: one line per WRITE request, counts only (never values, series or messages)
            String correlacion = MDC.get("correlationId");
            LOGGER.info("CVC write: correlation={} empresa={} series={} rows={} ids={} auditLog={} errors={}", correlacion != null ? correlacion : UUID.randomUUID(),
                    entrada.p_IdEmpresa, nSeries, nFilas, rangoIds, nFilas == 0 ? "none" : logOk ? "ok" : "failed", out.P_OUT_ERRORESCONTADOR);
        }
        return out;
    }

    private static List<CargarValorCuotaContrato.Resultado> porSerie(List<CargarValorCuotaContrato.Resultado> filas) {
        List<CargarValorCuotaContrato.Resultado> copia = new ArrayList<>(filas);
        copia.sort(Comparator.comparing(r -> r.IdSerieFondo));   // stable: rows of the same serie keep their relative order
        return copia;
    }

    private List<CargarValorCuotaContrato.Resultado> obtenerResultado(CargarValorCuotaContrato.Entrada entrada) throws Exception {
        String fechaSistema = configuracion.texto(entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
        Map<CargarValorCuotaContrato.ValorCuotaVigente, Object> seriesDuplicadas = obtenerSeriesDuplicadas(entrada);
        Map<String, SerieFondoSolicitudes> seriesFondos = obtenerSeriesFondos(entrada);
        Map<String, CargarValorCuotaContrato.ValorCuotaVigente> valoresExistentes = obtenerValorCuotaVigenteFecha(entrada);
        Map<String, CargarValorCuotaContrato.ValorCuotaVigente> valoresDiaAnterior = obtenerValorCuotaVigenteFechaAnterior(entrada);

        List<CargarValorCuotaContrato.Resultado> resultados = new ArrayList<>();
        for (CargarValorCuotaContrato.ValorCuota valorCuota : entrada.p_ValorCuota) {
            String[] estado = obtenerEstado(valorCuota, seriesDuplicadas, seriesFondos, valoresExistentes, fechaSistema, entrada.p_AllFunds);
            SerieFondoSolicitudes serie = seriesFondos.get(valorCuota.IDSERIEFONDO);
            long decimales = serie != null ? serie.DecimalesValorCuota : 0L;   // unboxing: a null column is a NullPointerException, as the legacy ternary
            boolean correcto = !ERR.equals(estado[0]);
            CargarValorCuotaContrato.Resultado resultado = new CargarValorCuotaContrato.Resultado();
            resultado.IdSerieFondo = valorCuota.IDSERIEFONDO;
            resultado.ISIN = valorCuota.ISIN;
            resultado.IdMoneda = valorCuota.IDMONEDA;
            resultado.FechaVigencia = correcto ? FechaLegacy.aFechaSql(valorCuota.FECHACIERRE) : FechaLegacy.aFechaSql("0001-01-01");
            resultado.Observacion = valorCuota.OBSERVACION;
            resultado.DecimalesValorCuota = decimales;
            // D-F3A-17: the legacy computes FLOOR(ZERO, decimals) and discards it; what is written is the INPUT value with all its scale
            resultado.FechaCalculo = FechaLegacy.aFechaSql(fechaSistema);
            resultado.Estado = estado[0];
            resultado.DescEstado = estado[1];
            resultado.ValorCuota = valorCuota.VALORCUOTA;
            resultado.ValorCuotaFechaAnterior = valoresDiaAnterior.containsKey(valorCuota.IDSERIEFONDO) ? valoresDiaAnterior.get(valorCuota.IDSERIEFONDO).ValorCuotaVigente : BigDecimal.ZERO;
            resultado.IdEmpresa = entrada.p_IdEmpresa;
            resultado.EmisorValorCuota = correcto ? serie.IdEmisor : "";
            if ("S".equalsIgnoreCase(entrada.p_AllFunds)) {
                resultado.FechaCalculo = correcto ? FechaLegacy.aFechaSql(valorCuota.FECHACALCULO) : FechaLegacy.aFechaSql("0001-01-01");
            }
            if (resultado.ValorCuota.compareTo(BigDecimal.ZERO) != 0) {
                resultado.ValorCuotaCalculado = resultado.ValorCuota;
                if (resultado.ValorCuotaFechaAnterior.compareTo(BigDecimal.ZERO) != 0) {
                    resultado.VariacionValorCuota = resultado.ValorCuota.multiply(resultado.ParidadValorCuota).subtract(resultado.ValorCuotaFechaAnterior)
                        .divide(resultado.ValorCuotaFechaAnterior, 3, RoundingMode.HALF_EVEN).multiply(new BigDecimal(100)).abs();
                }
            }
            resultados.add(resultado);
        }
        return resultados;
    }

    private Map<CargarValorCuotaContrato.ValorCuotaVigente, Object> obtenerSeriesDuplicadas(CargarValorCuotaContrato.Entrada entrada) {
        Map<CargarValorCuotaContrato.ValorCuotaVigente, Object> duplicadas = new HashMap<>();
        Map<CargarValorCuotaContrato.ValorCuotaVigente, Boolean> procesadas = new HashMap<>();
        for (CargarValorCuotaContrato.ValorCuota valorCuota : entrada.p_ValorCuota) {
            CargarValorCuotaContrato.ValorCuotaVigente clave = new CargarValorCuotaContrato.ValorCuotaVigente();
            clave.IdSerieFondo = valorCuota.IDSERIEFONDO;
            clave.FechaVigencia = FechaLegacy.aFechaSql(valorCuota.FECHACIERRE);
            if (procesadas.get(clave) != null) {
                duplicadas.put(clave, new Object());
            }
            procesadas.put(clave, true);
        }
        return duplicadas;
    }

    private Map<String, SerieFondoSolicitudes> obtenerSeriesFondos(CargarValorCuotaContrato.Entrada entrada) {
        List<String> seriesEntrada = entrada.p_ValorCuota.stream().map(v -> v.IDSERIEFONDO).distinct().collect(Collectors.toList());
        List<SerieFondoSolicitudes> almacenadas = series.series(entrada.p_IdEmpresa, seriesEntrada);
        return almacenadas.stream().collect(Collectors.toMap(s -> s.IdSerieFondo, s -> s));
    }

    private Map<String, CargarValorCuotaContrato.ValorCuotaVigente> obtenerValorCuotaVigenteFecha(CargarValorCuotaContrato.Entrada entrada) {
        List<String> seriesEntrada = entrada.p_ValorCuota.stream().map(v -> v.IDSERIEFONDO).distinct().collect(Collectors.toList());
        List<String> fechas = entrada.p_ValorCuota.stream().map(v -> v.FECHACIERRE).distinct().collect(Collectors.toList());
        List<CargarValorCuotaContrato.ValorCuotaVigente> almacenadas = datos.valoresVigentesEnFechas(seriesEntrada, fechas);
        // D-F3A-16: keyed "serie_fecha" and read by serie alone (never found); two stored values of the same serie and date are an IllegalStateException
        return almacenadas.stream().collect(Collectors.toMap(v -> v.IdSerieFondo + "_" + v.FechaVigencia, v -> v));
    }

    private Map<String, CargarValorCuotaContrato.ValorCuotaVigente> obtenerValorCuotaVigenteFechaAnterior(CargarValorCuotaContrato.Entrada entrada) throws Exception {
        List<String> seriesEntrada = entrada.p_ValorCuota.stream().map(v -> v.IDSERIEFONDO).distinct().collect(Collectors.toList());
        List<InObtenerDiaAnterior.FechasIn> fechasDa = new ArrayList<>();
        InObtenerDiaAnterior.FechasIn fda = new InObtenerDiaAnterior.FechasIn();   // D-F3A-18: ONE object reused for every row
        for (CargarValorCuotaContrato.ValorCuota fecha : entrada.p_ValorCuota) {
            fda.fechaInicial = fecha.FECHACIERRE;
            fda.numeroDias = 1L;
            fechasDa.add(fda);
        }
        InObtenerDiaAnterior in = new InObtenerDiaAnterior();
        in.idPais = entrada.p_IdPais;
        in.diaHabil = "N";
        in.fechas = fechasDa;
        ObtenerDiaAnterior.OutObtenerDiaAnterior dantout = diaAnterior.logica(in);
        List<String> fechasCierre = dantout.fechas.stream().map(f -> f.fechaCalculada).distinct().collect(Collectors.toList());
        List<CargarValorCuotaContrato.ValorCuotaVigente> almacenadas = datos.historicoDiaAnterior(seriesEntrada, fechasCierre);
        return almacenadas.stream().collect(Collectors.toMap(v -> v.IdSerieFondo, v -> v));
    }

    /** {Estado, DescEstado} of a row: the first rule that applies (FM-CVC-04). */
    private String[] obtenerEstado(CargarValorCuotaContrato.ValorCuota valorCuota, Map<CargarValorCuotaContrato.ValorCuotaVigente, Object> seriesDuplicadas,
            Map<String, SerieFondoSolicitudes> seriesFondos, Map<String, CargarValorCuotaContrato.ValorCuotaVigente> existentes, String fechaSistema, String allFunds) {
        CargarValorCuotaContrato.ValorCuotaVigente clave = new CargarValorCuotaContrato.ValorCuotaVigente();
        clave.IdSerieFondo = valorCuota.IDSERIEFONDO;
        clave.FechaVigencia = FechaLegacy.aFechaSql(valorCuota.FECHACIERRE);
        Object duplicada = seriesDuplicadas.get(clave);
        SerieFondoSolicitudes serie = seriesFondos.get(valorCuota.IDSERIEFONDO);
        CargarValorCuotaContrato.ValorCuotaVigente existente = existentes.get(clave.IdSerieFondo);

        if (!OK.equals(valorCuota.MENSAJEVALIDACION)) return new String[] { ERR, "FORMATO_INVALIDO" };
        if (duplicada != null) return new String[] { ERR, "VALOR_CUOTA_DUPLICADO" };
        if (serie == null) return new String[] { ERR, "SERIE_NO_EXISTE" };
        if (valorCuota.VALORCUOTA.compareTo(BigDecimal.ZERO) == 0) return new String[] { ERR, "VALOR_CUOTA_INVALIDO" };
        if ("N".equals(allFunds) && !valorCuota.IDMONEDA.equals(serie.IdMoneda)) return new String[] { ERR, "MONEDA_INCORRECTA" };
        java.sql.Date fechaCierre = FechaLegacy.aFechaSql(valorCuota.FECHACIERRE);
        java.sql.Date fechaSistemaComparacion = FechaLegacy.aFechaSql(fechaSistema);
        if (fechaCierre.compareTo(fechaSistemaComparacion) == 1) return new String[] { ERR, "FECHA_VALOR_CUOTA_INCORRECTA" };
        if (existente != null) return new String[] { ADV, "VALOR_EXISTE_FECHA" };
        return new String[] { OK, OK };
    }

    private List<MensajeSalida> obtenerMensajesSalida(List<CargarValorCuotaContrato.Resultado> resultados) {
        List<MensajeSalida> mensajes = new ArrayList<>();
        for (CargarValorCuotaContrato.Resultado resultado : resultados) {
            if (ERR.equals(resultado.Estado)) {
                mensajes.add(MensajeSalida.conTexto(0L, MensajeSalida.MENSAJE_ERROR, resultado.DescEstado, new HashMap<String, String>(), diccionario, resultado.IdEmpresa));
            }
        }
        return mensajes;
    }

    private List<CargarValorCuotaContrato.Resultado> obtenerValoresCuotaHistoricosInsertar(List<CargarValorCuotaContrato.Resultado> valoresCuotas) {
        return valoresCuotas.stream().filter(v -> !ERR.equals(v.Estado)).collect(Collectors.toList());
    }

    private List<CargarValorCuotaContrato.Resultado> obtenerValoresCuotaInsertar(List<CargarValorCuotaContrato.Resultado> valoresCuotas) {
        List<CargarValorCuotaContrato.Resultado> correctas = valoresCuotas.stream().filter(v -> !ERR.equals(v.Estado)).collect(Collectors.toList());
        Map<String, List<CargarValorCuotaContrato.Resultado>> porSerie = correctas.stream().collect(Collectors.groupingBy(v -> v.IdSerieFondo));
        List<CargarValorCuotaContrato.Resultado> insertar = new ArrayList<>();
        for (Map.Entry<String, List<CargarValorCuotaContrato.Resultado>> entry : porSerie.entrySet()) {
            // D-F3A-19: the most recent date of the BATCH per serie, with no comparison against the stored value
            List<CargarValorCuotaContrato.Resultado> ordenados = entry.getValue().stream().sorted(comparadorFecha()).collect(Collectors.toList());
            insertar.add(ordenados.get(0));
        }
        return insertar;
    }

    private Comparator<CargarValorCuotaContrato.Resultado> comparadorFecha() {
        return (v1, v2) -> {
            if (v1.FechaVigencia.compareTo(v2.FechaVigencia) == 0) {
                return 0;
            }
            return v2.FechaVigencia.compareTo(v1.FechaVigencia) == 1 ? 1 : -1;
        };
    }

    /**
     * Legacy {@code agregarLog}: ONE call after the four writes with one Log and one DetalleLog per row with {@code Estado != ERR}; omitted when there is none (the real
     * AgregarLog returns at once). Decision 2: ANY {@code Exception} of the port is swallowed (legacy {@code catch (Exception)}, AgregarLog.java:239-241), including the
     * 503 of the router; an {@code Error} is not.
     */
    private boolean agregarLog(CargarValorCuotaContrato.Entrada entrada, List<CargarValorCuotaContrato.Resultado> filas) {
        if (filas.isEmpty()) {
            return true;
        }
        LogContrato.InAgregarLog entradaLog = new LogContrato.InAgregarLog();
        for (CargarValorCuotaContrato.Resultado fila : filas) {
            LogContrato.Log l = new LogContrato.Log();
            l.IdCabeceraLog = 159L;
            l.IdTipoLog = "MODIFICACION";
            l.IdModulo = "FMM";
            l.IdUsuario = entrada.p_IdUsuario;
            l.IdEmpresa = entrada.p_IdEmpresa;
            l.IdUsuarioAtendido = entrada.p_IdUsuario;
            l.IdTipoRegistro = "VALOR_CUOTA_FMM";
            l.NombreProceso = "Cargar Valor Cuota";
            l.FechaSistemaLog = fila.FechaVigencia != null && fila.FechaCalculo != null ? texto(fila.FechaCalculo) : "";
            entradaLog.p_Log.add(l);

            LogContrato.DetalleLog det = new LogContrato.DetalleLog();
            det.IdCabeceraLog = 159L;
            det.IdTrabajo = 0L;
            det.SubRegistro = fila.IdSerieFondo != null ? fila.IdSerieFondo : "";
            det.ValorAntiguo = "";
            det.CampoRegistro = fila.FechaVigencia != null ? "VALOR_CUOTA_FMM " + texto(fila.FechaVigencia) : "VALOR_CUOTA_FMM ";
            det.ValorNuevo = fila.ValorCuota != null ? fila.ValorCuota.toString() : "";
            entradaLog.p_LogDetalle.add(det);
        }
        try {
            JsonObject json = JsonLegacy.conNulos().toJsonTree(entradaLog).getAsJsonObject();
            log.agregar(json);
            return true;
        } catch (Exception e) {
            // legacy: printStackTrace and go on; the load is not undone by a failing audit. H-1: it is no longer silent, but only the class and the row count are logged
            LOGGER.warn("CVC audit failed: {} rows, exception={}", filas.size(), e.getClass().getName());
            return false;
        }
    }

    private static String texto(java.sql.Date fecha) {
        return FechaLegacy.aTexto(fecha.toLocalDate());
    }
}
