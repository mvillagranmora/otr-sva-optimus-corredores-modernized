package cl.bcs.tesoreria.javacall;

import java.math.BigDecimal;
import java.nio.file.NoSuchFileException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.tesoreria.javacall.dap.ArchivoFtp.Cabecera;
import cl.bcs.tesoreria.javacall.dap.ArchivoFtp.Detalle;
import cl.bcs.tesoreria.javacall.dap.CsvPlano;
import cl.bcs.tesoreria.javacall.dap.ValidacionLineaCarga;
import cl.bcs.tesoreria.javacall.kit.JsonLegacy;
import cl.bcs.tesoreria.javacall.kit.MensajeSalida;
import cl.bcs.tesoreria.javacall.kit.Plataforma;
import cl.bcs.tesoreria.javacall.kit.SqlNombrado;
import cl.bcs.tesoreria.javacall.kit.colaborador.CrearIngresoEgreso;
import cl.bcs.tesoreria.javacall.kit.colaborador.CrearIngresoEgreso.CrearIngresoEgreso_Entrada;
import cl.bcs.tesoreria.javacall.kit.colaborador.CrearIngresoEgreso.CrearIngresoEgreso_Salida;
import cl.bcs.tesoreria.javacall.kit.colaborador.CrearIngresoEgreso.FormaDePago_Entrada;
import cl.bcs.tesoreria.javacall.kit.colaborador.CrearIngresoEgreso.IngresoEgreso_Entrada;
import cl.bcs.tesoreria.javacall.kit.colaborador.CrearIngresoEgreso.ResultadoProceso_Salida;
import cl.bcs.tesoreria.port.out.AlmacenArchivos;
import cl.bcs.tesoreria.port.out.Colaboradores;
import cl.bcs.tesoreria.port.out.ConfiguracionSistema;
import cl.bcs.tesoreria.port.out.DiccionarioMensajes;
import cl.bcs.tesoreria.port.out.Secuencias;
import cl.bcs.utils.Fecha;

/**
 * Legacy {@code cl.bcs.tesoreria.CargaMasivaIngresoEgresoDAPFFMM} (TES-B-178..193): reads the day's DAP / FFMM income-expense file from the FTP
 * tables (header PEN, lines of 39 {@code ;} separated fields), validates each line, creates the movements (collaborator CrearIngresoEgreso: first the
 * incomes MOVING, then the expenses MOVEGR) and writes the result of every line as a CSV file (through {@link AlmacenArchivos}) and as a new
 * FTP file (header + lines, to be sent by a later job). The input header ends PRO, or PEN when the file could not be written or anything failed.
 */
@Service
public class CargaMasivaIngresoEgresoDAPFFMMService {

    private static final String BASE_CODIGO_TIPO = "CARGA_MASIVA_INGRESO_EGRESO_";
    private static final String BASE_NOMBRE_ARCHIVO_SALIDA = "CDB_Validacion_IngresoEgreso_";
    private static final int CAMPOS = 39;

    private static final String SQL_INSERT_CABECERA = SqlNombrado.recurso("CargaMasivaIngresoEgresoDAPFFMM_InsertCabecera");
    private static final String SQL_INSERT_DETALLES = SqlNombrado.recurso("CargaMasivaIngresoEgresoDAPFFMM_InsertDetalles");
    private static final String SQL_SELECT_CABECERA = SqlNombrado.recurso("CargaMasivaIngresoEgresoDAPFFMM_SelectCabeceras");
    private static final String SQL_SELECT_DETALLE = SqlNombrado.recurso("CargaMasivaIngresoEgresoDAPFFMM_SelectDetalles");
    private static final String SQL_INSERT_TABLA_PASO = SqlNombrado.recurso("CargaMasivaIngresoEgresoDAPFFMM_InsertTablaPaso");
    private static final String SQL_UPDATE_CABECERA = SqlNombrado.recurso("CargaMasivaIngresoEgresoDAPFFMM_UpdateCabecera");
    private static final String SQL_UPDATE_DETALLES = SqlNombrado.recurso("CargaMasivaIngresoEgresoDAPFFMM_UpdateDetalles");
    private static final String SQL_SELECT_PERSONA_PORTAFOLIO = SqlNombrado.recurso("CargaMasivaIngresoEgresoDAPFFMM_SelectIdPersonaIdPortafolioSegunTablaPaso");
    private static final String SQL_SELECT_CONCEPTOS = SqlNombrado.recurso("CargaMasivaIngresoEgresoDAPFFMM_SelectConceptosSistema");
    private static final String SQL_SELECT_TIPO_MOVIMIENTO = SqlNombrado.recurso("CargaMasivaIngresoEgresoDAPFFMM_SelectTipoMovimientoTesoreria");

    /** Request; the defaults are the legacy ones (a missing field keeps them). */
    public static class EntradaCargaMasivaIngresoEgresoDAPFFMM {
        public Long p_IdEmpresa = 1L;
        public Long p_IdUsuario = 0L;
        public String p_TipoArchivoCarga;
        public String p_EnviarArchivoACasilla;
        public String p_FechaCarga;
        public String p_IdModulo = "TES";
        public String p_SimularOperacion = "N";
        public String p_UsaReglasNegocio = "S";
    }

    public static class SalidaCargaMasivaIngresoEgresoDAPFFMM {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public String P_OUT_RUTA_ARCHIVO = "";
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
        public Long P_OUT_REGISTROSINGRESADOSCONTADOR = 0L;
    }

    /** A client / portfolio looked up by document and portfolio code. */
    public static class PersonaPortafolio {
        public String IdTipoDocumentoIdentidad = "";
        public String DocumentoIdentidad = "";
        public String CodigoPortafolio = "";
        public Long IdTrabajo = 0L;
        public Long IdPersona = 0L;
        public Long IdPortafolio = 0L;
    }

    public static class Concepto {
        public String IdConcepto = "";
    }

    public static class TipoMovimiento {
        public String IdTipoMovimiento = "";
    }

    private final SqlNombrado sql;
    private final DiccionarioMensajes diccionario;
    private final ConfiguracionSistema configuracion;
    private final Secuencias secuencias;
    private final AlmacenArchivos almacen;
    private final Colaboradores colaboradores;

    public CargaMasivaIngresoEgresoDAPFFMMService(NamedParameterJdbcTemplate jdbc, DiccionarioMensajes diccionario, ConfiguracionSistema configuracion,
            Secuencias secuencias, AlmacenArchivos almacen, Colaboradores colaboradores) {
        this.sql = new SqlNombrado(jdbc);
        this.diccionario = diccionario;
        this.configuracion = configuracion;
        this.secuencias = secuencias;
        this.almacen = almacen;
        this.colaboradores = colaboradores;
    }

    /** Legacy {@code call}: the request is the JSON of {@link EntradaCargaMasivaIngresoEgresoDAPFFMM}. */
    @Transactional(rollbackFor = Exception.class)
    public JsonObject execute(JsonObject entrada) throws Exception {
        EntradaCargaMasivaIngresoEgresoDAPFFMM in = JsonLegacy.de(entrada, EntradaCargaMasivaIngresoEgresoDAPFFMM.class);
        SalidaCargaMasivaIngresoEgresoDAPFFMM out = cargar(in);
        out.P_OUT_ERRORESCONTADOR = MensajeSalida.contarErrores(out.P_OUT_MENSAJES);
        return JsonParser.parseString(JsonLegacy.gson().toJson(out)).getAsJsonObject();
    }

    SalidaCargaMasivaIngresoEgresoDAPFFMM cargar(EntradaCargaMasivaIngresoEgresoDAPFFMM entrada) throws Exception {
        SalidaCargaMasivaIngresoEgresoDAPFFMM salida = new SalidaCargaMasivaIngresoEgresoDAPFFMM();

        List<Cabecera> cabeceras = cabecerasPendientes(entrada.p_TipoArchivoCarga, entrada.p_FechaCarga);
        if (cabeceras.isEmpty()) {
            salida.P_OUT_MENSAJES.add(mensaje(entrada, "UI_CARGA_MASIVA_DAPFFMM_SIN_ARCHIVO_PARA_CARGAR"));
            salida.P_OUT_ERRORESCONTADOR = MensajeSalida.contarErrores(salida.P_OUT_MENSAJES);
            return salida;
        }

        try {
            cabeceras.forEach(c -> c.IdEstado = "ASI"); // assigned: nobody else loads this file meanwhile
            sql.lote(SQL_UPDATE_CABECERA, cabeceras);

            List<Detalle> detalles = detalles(cabeceras);
            if (detalles.isEmpty()) {
                // LEGACY DEFECT kept (see README TES-B-180): an empty file is reported as an error and still ends PRO
                salida.P_OUT_MENSAJES.add(mensaje(entrada, "UI_CARGA_MASIVA_DAPFFMM_ARCHIVO_CARGA_VACIO"));
                salida.P_OUT_ERRORESCONTADOR = MensajeSalida.contarErrores(salida.P_OUT_MENSAJES);
            } else {
                Map<Long, String> validaciones = validarDetalles(detalles);
                List<Detalle> filtrados = filtrarDetalles(detalles, validaciones);

                CrearIngresoEgreso_Salida creadosIngresos = new CrearIngresoEgreso_Salida();
                CrearIngresoEgreso_Salida creadosEgresos = new CrearIngresoEgreso_Salida();
                if (!filtrados.isEmpty()) {
                    List<IngresoEgreso_Entrada> ingresos = movimientos(filtrados, "MOVING");
                    List<FormaDePago_Entrada> pagosIngresos = formasDePago(filtrados, "MOVING");
                    List<IngresoEgreso_Entrada> egresos = movimientos(filtrados, "MOVEGR");
                    List<FormaDePago_Entrada> pagosEgresos = formasDePago(filtrados, "MOVEGR");
                    if (!ingresos.isEmpty()) {
                        creadosIngresos = crearIngresosEgresos(entrada, ingresos, pagosIngresos);
                    }
                    if (!egresos.isEmpty()) {
                        creadosEgresos = crearIngresosEgresos(entrada, egresos, pagosEgresos);
                    }
                }

                List<String[]> lineasSalida = new ArrayList<>(lineasArchivoSalida(detalles, creadosIngresos, creadosEgresos, validaciones));
                if (!lineasSalida.isEmpty()) {
                    String nombre = BASE_NOMBRE_ARCHIVO_SALIDA + entrada.p_TipoArchivoCarga + "_" + entrada.p_FechaCarga.replace("-", "");
                    salida.P_OUT_RUTA_ARCHIVO = escribirArchivoSalida(entrada, lineasSalida, nombre);
                    if ("".equals(salida.P_OUT_RUTA_ARCHIVO)) {
                        cabeceras.forEach(c -> c.IdEstado = "PEN"); // written back by the finally block: the file is loaded again next time
                        return salida;
                    }
                    salida.P_OUT_REGISTROSINGRESADOSCONTADOR = lineasSalida.stream().filter(cols -> !"0".equals(cols[1])).count();
                    actualizarDetallesEntrada(detalles, lineasSalida);
                    registrarArchivoSalida(entrada, lineasSalida, nombre);
                }
                salida.P_OUT_MENSAJES.addAll(creadosIngresos.P_OUT_MENSAJES);
                salida.P_OUT_MENSAJES.addAll(creadosEgresos.P_OUT_MENSAJES);
                salida.P_OUT_ERRORESCONTADOR = MensajeSalida.contarErrores(salida.P_OUT_MENSAJES);
            }
            cabeceras.forEach(c -> c.IdEstado = "PRO");
        } catch (Exception ex) {
            cabeceras.forEach(c -> c.IdEstado = "PEN");
            throw ex;
        } finally {
            sql.lote(SQL_UPDATE_CABECERA, cabeceras);
        }
        return salida;
    }

    private MensajeSalida mensaje(EntradaCargaMasivaIngresoEgresoDAPFFMM entrada, String codigo) {
        return Plataforma.mensaje(diccionario, entrada.p_IdEmpresa, 0L, MensajeSalida.MENSAJE_ERROR, codigo, new HashMap<>(0));
    }

    // ------------------------------------------------------------------------------------------------ input file

    /** The first pending header of the type and date: file name CDB_IngresoEgreso_&lt;type&gt;&lt;yyyyMMdd&gt;.csv (see the SQL). */
    private List<Cabecera> cabecerasPendientes(String tipoArchivoCarga, String fechaEjecucion) throws SQLException {
        Map<String, Object> params = new HashMap<>();
        params.put("IdCodigoTipo", BASE_CODIGO_TIPO + tipoArchivoCarga);
        params.put("TipoArchivo", tipoArchivoCarga);
        params.put("FechaEjecucion", fechaEjecucion);
        params.put("IdEstado", "PEN");
        return sql.consultar(SQL_SELECT_CABECERA, params, Cabecera.class);
    }

    private List<Detalle> detalles(List<Cabecera> cabeceras) throws SQLException {
        return sql.consultar(SQL_SELECT_DETALLE, Map.of("IdCabecera", cabeceras.stream().map(c -> c.IdCabecera).collect(Collectors.toList())), Detalle.class);
    }

    // ------------------------------------------------------------------------------------------------ validation

    /** Validation text by index of the line in the file; a blank line has none. */
    private Map<Long, String> validarDetalles(List<Detalle> detalles) throws Exception {
        Map<Long, String> validaciones = new HashMap<>();
        List<String> idsConcepto = detalles.stream().map(d -> {
            String[] tokens = d.Linea.split(";", -1);
            return tokens.length > 9 ? tokens[9] : "";
        }).filter(valor -> !valor.isEmpty()).distinct().collect(Collectors.toList());

        // two rows with the same key throw IllegalStateException (Collectors.toMap), as in the legacy
        Map<String, Concepto> conceptos = sql.consultar(SQL_SELECT_CONCEPTOS, Map.of("IdsConcepto", idsConcepto), Concepto.class).stream()
                .collect(Collectors.toMap(c -> c.IdConcepto, c -> c));
        Map<String, TipoMovimiento> tiposMovimiento = sql.consultar(SQL_SELECT_TIPO_MOVIMIENTO, Map.of(), TipoMovimiento.class).stream()
                .collect(Collectors.toMap(t -> t.IdTipoMovimiento, t -> t));

        for (int indice = 0; indice < detalles.size(); indice++) {
            List<String> lineas = Arrays.asList(detalles.get(indice).Linea.split(";", -1));
            if (lineas.stream().noneMatch(tok -> !tok.isEmpty())) {
                continue; // blank line
            }
            if (lineas.size() < CAMPOS) {
                // LEGACY DEFECT kept (see README TES-B-182): the validation of a short line is stored under index 0
                validaciones.put(0L, ValidacionLineaCarga.validar(lineas, false, false));
                continue;
            }
            String validacion = ValidacionLineaCarga.validar(lineas, conceptos.containsKey(lineas.get(9)), tiposMovimiento.containsKey(lineas.get(1)));
            if (!validacion.isEmpty()) {
                validaciones.put((long) indice, validacion);
            }
        }
        return validaciones;
    }

    /** Lines that pass validation and are not blank. */
    private List<Detalle> filtrarDetalles(List<Detalle> detalles, Map<Long, String> validaciones) {
        List<Detalle> filtrados = new ArrayList<>();
        for (int indice = 0; indice < detalles.size(); indice++) {
            Detalle detalle = detalles.get(indice);
            boolean conContenido = Arrays.stream(detalle.Linea.split(";", -1)).anyMatch(tok -> !tok.isEmpty());
            if (validaciones.getOrDefault((long) indice, "").isEmpty() && conContenido) {
                filtrados.add(detalle);
            }
        }
        return filtrados;
    }

    /** Exactly 39 fields (trailing empty fields are dropped by this split, as in the legacy) and, when asked, the given movement type. */
    private static boolean esLineaValida(Detalle detalle, String tipoMovimiento) {
        List<String> lineas = Arrays.asList(detalle.Linea.split(";"));
        return lineas.size() == CAMPOS && (tipoMovimiento.equals(lineas.get(1)) || "".equals(tipoMovimiento));
    }

    // ------------------------------------------------------------------------------------------------ movements

    /** Movements of one type, sorted by reference. The client / portfolio of every line is resolved through the staging table. */
    private List<IngresoEgreso_Entrada> movimientos(List<Detalle> detalles, String tipoMovimiento) throws SQLException {
        Map<String, PersonaPortafolio> personaPortafolio = personasPortafolios(detalles);
        List<IngresoEgreso_Entrada> movimientos = new ArrayList<>();
        for (Detalle detalle : detalles) {
            if (!esLineaValida(detalle, tipoMovimiento)) {
                continue;
            }
            List<String> l = Arrays.asList(detalle.Linea.split(";"));
            PersonaPortafolio pp = personaPortafolio.get(l.get(3) + "_" + l.get(4) + "_" + l.get(5)); // a missing key is a NullPointerException, as in the legacy
            IngresoEgreso_Entrada ie = new IngresoEgreso_Entrada();
            ie.NumeroReferencia = Long.parseLong(l.get(0));
            ie.IdTipoMovimiento = l.get(1);
            ie.IdTipoPersona = l.get(2);
            ie.IdPersona = pp.IdPersona;
            ie.IdPortafolio = pp.IdPortafolio;
            ie.IdMoneda = l.get(6);
            ie.Monto = new BigDecimal(l.get(7));
            ie.FechaLiquidacion = l.get(8);
            ie.IdConcepto = l.get(9);
            ie.IdTipoPersonaTitularPago = l.get(2);
            ie.IdPersonaTitularPago = pp.IdPersona;
            ie.IdPortafolioTitularPago = pp.IdPortafolio;
            ie.IdCaja = 0L;
            ie.SecuenciaAgrupacion = "0".equals(l.get(10)) ? "" : l.get(10);
            ie.SecuenciaRelacionada = null;
            ie.Glosa = "OPERACION CARGA MASIVA - " + l.get(11);
            ie.IdEmpleadoDigitador = 0L;
            ie.IdSucursalEmpleadoDigitador = 0L;
            ie.IdTipoComprobante = "";
            ie.IdComprobante = 0L;
            ie.FechaComprobante = "0001-01-01";
            ie.FechaLiquidacionComprobante = "0001-01-01";
            ie.FolioCartera = 0L;
            ie.IdInstrumento = 0L;
            ie.IdTipoInstrumento = "";
            ie.IdClasificacionTipoInstrumento = "";
            ie.IdTipoProducto = l.get(34);
            ie.IdTipoOperacion = l.get(35);
            ie.IdOperacion = Long.parseLong(l.get(36));
            ie.FechaOperacion = l.get(37);
            ie.IdCondicionLiquidacion = l.get(38);
            ie.IdBolsa = "";
            ie.IdMonedaOperacion = "";
            ie.MontoOperacion = BigDecimal.ZERO;
            ie.IdTipoReajuste = "";
            ie.FechaParidad = "0001-01-01";
            ie.ValorParidad = BigDecimal.ZERO;
            ie.Bilateral = "N";
            ie.MueveCuentaInversion = "S";
            ie.TitularPago = "N";
            ie.GeneraComprobante = !"".equals(l.get(12)) ? l.get(12) : "S";
            ie.LiquidarComprobante = !"".equals(l.get(13)) ? l.get(13) : "S";
            ie.AgruparEnComprobante = !"".equals(l.get(14)) ? l.get(14) : "N";
            ie.IdTipoCanalIngreso = "CME";
            movimientos.add(ie);
        }
        movimientos.sort(Comparator.comparing(m -> m.NumeroReferencia));
        return movimientos;
    }

    private List<FormaDePago_Entrada> formasDePago(List<Detalle> detalles, String tipoMovimiento) {
        List<FormaDePago_Entrada> formas = new ArrayList<>();
        for (Detalle detalle : detalles) {
            if (!esLineaValida(detalle, tipoMovimiento)) {
                continue;
            }
            List<String> l = Arrays.asList(detalle.Linea.split(";"));
            FormaDePago_Entrada fp = new FormaDePago_Entrada();
            fp.NumeroReferencia = Long.parseLong(l.get(0));
            fp.NumeroReferenciaFormaDePago = Long.parseLong(l.get(0));
            fp.IdMedioDePago = l.get(16);
            fp.IdModalidadDePago = l.get(17);
            fp.IdMoneda = l.get(18);
            fp.Monto = new BigDecimal(l.get(19));
            fp.IdCuentaBancariaCorredora = Long.parseLong(l.get(26));
            fp.IdTarjetaBancariaCorredora = !"".equals(l.get(27)) ? Long.parseLong(l.get(27)) : 0L;
            fp.IdChequera = !"".equals(l.get(28)) ? Long.parseLong(l.get(28)) : 0L;
            fp.NumeroDocumentoCorredora = Long.parseLong(l.get(20));
            fp.FechaDocumentoPagoCorredora = l.get(21);
            fp.IdCuentaBancariaSocio = Long.parseLong(l.get(22));
            fp.IdBancoSocio = Long.parseLong(l.get(23));
            fp.NombreCuentaBancariaSocio = "";
            fp.NumeroCuentaBancariaSocio = l.get(24);
            fp.IdTipoCuentaBancariaSocio = "";
            fp.NombreTipoCuentaBancariaSocio = "";
            fp.IdTarjetaBancariaSocio = !"".equals(l.get(29)) ? Long.parseLong(l.get(29)) : 0L;
            fp.IdTipoTarjetaBancariaSocio = l.get(30);
            fp.NombreTipoTarjetaBancariaSocio = "";
            fp.NumeroTarjetaBancariaSocio = l.get(31);
            fp.NumeroSerieSocio = l.get(32);
            fp.NumeroDocumentoSocio = !"".equals(l.get(33)) ? Long.parseLong(l.get(33)) : 0L;
            fp.FechaDocumentoPagoSocio = l.get(25);
            formas.add(fp);
        }
        return formas;
    }

    /**
     * Resolves (document type, document, portfolio code) of every valid line to person and portfolio through the staging table (rows tagged with a
     * random job id). A client without person or portfolio comes back with null ids. Two rows for one key throw IllegalStateException.
     */
    private Map<String, PersonaPortafolio> personasPortafolios(List<Detalle> detalles) throws SQLException {
        long idTrabajo = ThreadLocalRandom.current().nextLong(10_000_000_000L);
        List<PersonaPortafolio> aBuscar = new ArrayList<>();
        for (Detalle detalle : detalles) {
            if (!esLineaValida(detalle, "")) {
                continue;
            }
            List<String> l = Arrays.asList(detalle.Linea.split(";"));
            PersonaPortafolio p = new PersonaPortafolio();
            p.IdTipoDocumentoIdentidad = l.get(3);
            p.DocumentoIdentidad = l.get(4);
            p.CodigoPortafolio = l.get(5);
            p.IdTrabajo = idTrabajo;
            aBuscar.add(p);
        }
        sql.lote(SQL_INSERT_TABLA_PASO, aBuscar);
        return sql.consultar(SQL_SELECT_PERSONA_PORTAFOLIO, Map.of("IdTrabajo", idTrabajo), PersonaPortafolio.class).stream()
                .collect(Collectors.toMap(pp -> pp.IdTipoDocumentoIdentidad + "_" + pp.DocumentoIdentidad + "_" + pp.CodigoPortafolio, pp -> pp));
    }

    private CrearIngresoEgreso_Salida crearIngresosEgresos(EntradaCargaMasivaIngresoEgresoDAPFFMM entrada, List<IngresoEgreso_Entrada> movimientos,
            List<FormaDePago_Entrada> formasDePago) {
        CrearIngresoEgreso_Entrada e = new CrearIngresoEgreso_Entrada();
        e.p_IdEmpresa = entrada.p_IdEmpresa;
        e.p_IdUsuario = entrada.p_IdUsuario;
        e.p_IdModulo = entrada.p_IdModulo;
        e.p_SimularOperacion = "N";
        e.p_UsaReglasNegocio = "S";
        e.p_EsIngresoAutomatico = "S";
        e.p_IngresoEgresoGenerar = movimientos;
        e.p_FormaDePago = formasDePago;
        return CrearIngresoEgreso.invocar(colaboradores, e);
    }

    // ------------------------------------------------------------------------------------------------ output

    /**
     * One row per line of the input file: {reference, comprobante id (0 when none), Aplicado | Rechazado, comment}. A line that is not valid for the
     * file format is Rechazado; the comment is the validation text or the errors the movement creation reported for the line.
     */
    private List<String[]> lineasArchivoSalida(List<Detalle> detalles, CrearIngresoEgreso_Salida ingresos, CrearIngresoEgreso_Salida egresos,
            Map<Long, String> validaciones) {
        List<String[]> lineasSalida = new ArrayList<>();
        for (int indice = 0; indice < detalles.size(); indice++) {
            Detalle detalle = detalles.get(indice);
            if (!esLineaValida(detalle, "")) {
                lineasSalida.add(new String[] {"0", "0", "Rechazado", "El registro no cuenta con el formato requerido."});
                continue;
            }
            List<String> l = Arrays.asList(detalle.Linea.split(";"));
            Long referencia = !"".equals(l.get(0)) ? Long.parseLong(l.get(0)) : 0L;
            String validacion = validaciones.getOrDefault((long) indice, "");

            List<ResultadoProceso_Salida> resultados = new ArrayList<>();
            for (CrearIngresoEgreso_Salida creados : List.of(ingresos, egresos)) {
                creados.P_OUT_RESULTADOPROCESO.stream()
                        .filter(r -> !"".equals(l.get(0)) && referencia.equals(r.NumeroReferencia) && "IdComprobante".equals(r.Campo))
                        .forEach(resultados::add);
            }
            List<Long> idsComprobantes = resultados.stream().map(r -> r.Id).collect(Collectors.toList());

            StringBuilder comentario = new StringBuilder();
            for (CrearIngresoEgreso_Salida creados : List.of(ingresos, egresos)) {
                for (MensajeSalida m : creados.P_OUT_MENSAJES) {
                    if (MensajeSalida.MENSAJE_ERROR.equals(m.tipo) && ((!"".equals(l.get(0)) && referencia.equals(m.id)) || idsComprobantes.contains(m.id))) {
                        comentario.append(m.descripcion).append(". ");
                    }
                }
            }
            lineasSalida.add(new String[] {referencia.toString(), !resultados.isEmpty() ? resultados.get(0).Id.toString() : "0",
                    !resultados.isEmpty() ? "Aplicado" : "Rechazado", "".equals(validacion) ? comentario.toString() : validacion});
        }
        return lineasSalida;
    }

    /**
     * Writes the CSV below the configured output folder and answers its path relative to the platform base folder, or "" when the folder does not
     * exist. PLA.RutaBaseArchivos is read as the legacy did (a missing parameter is an Exception); the base folder itself belongs to the file store.
     */
    private String escribirArchivoSalida(EntradaCargaMasivaIngresoEgresoDAPFFMM entrada, List<String[]> lineasSalida, String nombre) throws Exception {
        Plataforma.parametro(configuracion, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.RutaBaseArchivos");
        String carpeta = Plataforma.parametro(configuracion, entrada.p_IdEmpresa, "TESORERIA", "TES.RutaSalidaArchivosCargaMasiva");
        String rutaRelativa = carpeta + nombre + ".csv";
        try {
            almacen.escribir(rutaRelativa, CsvPlano.aBytes(lineasSalida));
        } catch (NoSuchFileException carpetaInexistente) {
            return ""; // the legacy checked that the folder exists and gave up with an empty path when it did not
        }
        return rutaRelativa;
    }

    /** Points every input line at the comprobante it produced (rows of the output that are not Rechazado). */
    private void actualizarDetallesEntrada(List<Detalle> detalles, List<String[]> lineasSalida) throws SQLException {
        // two output rows with the same reference throw IllegalStateException (Collectors.toMap), as in the legacy: LEGACY DEFECT kept (see README TES-B-193)
        Map<String, String[]> porReferencia = lineasSalida.stream().filter(linea -> !"Rechazado".equals(linea[2]))
                .collect(Collectors.toMap(linea -> linea[0], linea -> linea));
        for (Detalle detalle : detalles) {
            List<String> campos = new ArrayList<>(Arrays.asList(detalle.Linea.split(";")));
            if (!campos.isEmpty()) {
                String[] lineaSalida = porReferencia.get(campos.get(0));
                if (lineaSalida != null) {
                    detalle.IdTipoEntidad = "0".equals(lineaSalida[1]) ? "" : "IdComprobanteTesoreria";
                    detalle.IdEntidad = Long.parseLong(lineaSalida[1]);
                }
            }
        }
        sql.lote(SQL_UPDATE_DETALLES, detalles);
    }

    /** The output as a new FTP file (header PEN + one line per row) for the job that sends it. */
    private void registrarArchivoSalida(EntradaCargaMasivaIngresoEgresoDAPFFMM entrada, List<String[]> lineasSalida, String nombre) throws SQLException {
        long idCabecera = secuencias.siguiente("BSCL.Plataforma.BD.Secuencias::Cabecera");
        Cabecera cabecera = new Cabecera();
        cabecera.IdCabecera = idCabecera;
        cabecera.IdCodigoTipo = "SALIDA_" + BASE_CODIGO_TIPO + entrada.p_TipoArchivoCarga;
        cabecera.IdEstado = "PEN";
        cabecera.IdUsuario = entrada.p_IdUsuario;
        cabecera.NombreArchivo = nombre + ".csv";
        cabecera.FechaCreacion = Fecha.toDateTime(entrada.p_FechaCarga);
        sql.lote(SQL_INSERT_CABECERA, List.of(cabecera));

        List<Detalle> detalles = new ArrayList<>();
        for (String[] linea : lineasSalida) {
            Detalle d = new Detalle();
            d.IdCabecera = idCabecera;
            d.Linea = linea[0] + ";" + linea[1] + ";" + linea[2] + ";" + linea[3];
            d.IdTipoEntidad = "0".equals(linea[1]) ? "" : "IdComprobanteTesoreria";
            d.IdEntidad = Long.parseLong(linea[1]);
            detalles.add(d);
        }
        sql.lote(SQL_INSERT_DETALLES, detalles);
    }
}
