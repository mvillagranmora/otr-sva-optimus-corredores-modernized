package cl.bcs.normativa.circulares.ReportesBCC.Reportes3p1;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import datadog.trace.api.Trace;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.utils.BcsPojo;

/**
 * Servicio para ingresar reportes SICAM / Merrill Lynch
 */
public class IngresarReportes implements JavaCall {

    static final Logger logger = LogManager.getLogger(IngresarReportes.class);

    /**
     * Llamadas SQL
     */
    private final String SQL_InsertReporte = loadResourceToString(
            "cl/bcs/normativa/circulares/ReportesBCC/Reportes3p1/sql/IngresarReporte_Reporte_insert.sql");
    private final String SQL_SelectId = loadResourceToString(
            "cl/bcs/normativa/circulares/ReportesBCC/Reportes3p1/sql/ConsultarReportes_IDReporte_select.sql");
    private final String SQL_SelectReporte = loadResourceToString(
            "cl/bcs/normativa/circulares/ReportesBCC/Reportes3p1/sql/ConsultarReportes_Reporte_select.sql");
    private final String SQL_SelectIdDocumento = loadResourceToString(
            "cl/bcs/normativa/circulares/ReportesBCC/Reportes3p1/sql/ConsultarReportes_Documento_select.sql");
    private final String SQL_SelectDiccionario = loadResourceToString(
            "cl/bcs/normativa/circulares/ReportesBCC/Reportes3p1/sql/ConsultarReportes_Diccionario_select.sql");
    private final String SQL_SelectHora = loadResourceToString(
            "cl/bcs/normativa/circulares/ReportesBCC/Reportes3p1/sql/ConsultarReportes_HoraIngreso_select.sql");

    /**
     * Clase de entrada para ingreso de reporte 3.1
     */
    public static class EntradaReporte3p1 extends BcsPojo {
        public Long idEmpresa;
        public String idUsuario;
        public String claveUsuario;
        public String claveReporte;
        // Datos de control
        public Long id;
        public Long idDocumento;
        public String fuenteDatos;
        public String estado;
        public String estadoSICAM;
        public String sicam;
        // Datos basicos reporte
        public String tipoRegistro;
        public String rutReportante;
        public String fechaOperacion;
        public String horaOperacion;
        public String tipoOperacion;
        public String eventoReporte;
        public String identificador;
        public String rutContraparte;
        public String leiContraparte;
        public String nombreContraparte;
        public String plataformaNegociacion;
        public BigDecimal montoMonedaComprada;
        public BigDecimal montoMonedaVendida;
        public BigDecimal tipoCambio;
        // Datos complementarios
        public Long idCliente;
        public String idTipoOperacion;
        public Long idOperacion;
        public String nemo;
        public String idMonedaPrincipal;
        public String idMonedaSecundaria;
    }

    /**
     * Clase de salida para ingreso de reporte 3.1
     */
    public static class SalidaReporte3p1 extends BcsPojo {
        public List<MensajeSalida> P_OUT_MENSAJES;
        public Long P_OUT_ERRORESCONTADOR;
        public String P_OUT_DESCRIPCION;

        public SalidaReporte3p1() {
            this.P_OUT_MENSAJES = new ArrayList<>();
            this.P_OUT_ERRORESCONTADOR = 0L;
            this.P_OUT_DESCRIPCION = "";
        }
    }

    public static class NextIdResult {
        public Long idQuery;
    }
    
    public static class diccionarioResult extends BcsPojo{
        public String Codigo;
    }

    public static class IdDocumento {
        public Long IdDocumento;
    }
    
    public static class ParametroHora {
        public String Valor;
    }

    static boolean isNullOrBlank(String s){ return s == null || s.trim().isEmpty(); }

    /**
     * Carga el contenido de un recurso SQL como una cadena de texto.
     * 
     * @param con               Conexion a la base de datos.
     * @param EntradaReporte3p1 entrada con los parametros del reporte.
     * @return Contenido del recurso como una cadena de texto.
     * @throws Exception Si ocurre un error al cargar el recurso.
     */
    @Trace
    public SalidaReporte3p1 ingresarReporte(Connection con, EntradaReporte3p1 in) throws Exception {
        logger.info("Ingresando reporte");
        logger.info("Parametros de entrada: {}", in);
        SalidaReporte3p1 out = new SalidaReporte3p1();
        SQL sql = new SQL();

        // Validacion de llave nula
        if (in == null || in.rutReportante == null || in.fechaOperacion == null || in.horaOperacion == null
                || in.identificador == null) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_ENTRADA_NULA",
                    new HashMap<String, String>(), con, in.idEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "Datos del folio nulos o incompletos.";
            logger.error(
                    "Datos de folio invalidos: rutReportante={}, fechaOperacion={}, horaOperacion={}, identificador={}",
                    in.rutReportante, in.fechaOperacion, in.horaOperacion, in.identificador);
            return out;
        }

        // Validacion de tipo de registro
        if (in.tipoRegistro == null || in.tipoRegistro.isEmpty() || in.tipoRegistro.length() != 2) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_TIPO_REGISTRO_INVALIDO",
                    new HashMap<String, String>(), con, in.idEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "Tipo de registro invalido.";
            logger.error("Tipo de registro invalido: " + in.tipoRegistro);
            return out;
        }

        // Validacion de rut reportante
        if (in.rutReportante == null || in.rutReportante.isEmpty() || !ValidacionRut(in.rutReportante)) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_RUT_REPORTANTE_INVALIDO",
                    new HashMap<String, String>(), con, in.idEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "Rut reportante invalido.";
            logger.error("Rut reportante invalido: " + in.rutReportante);
            return out;
        }

        // Validacion de formato yyyy-MM-dd y que sea la fecha de hoy
        LocalDate hoy = LocalDate.now();
        try {
            LocalDate fechaOp = LocalDate.parse(
                    in.fechaOperacion,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            if (!fechaOp.equals(hoy)) {
                out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,
                        "ERROR_FECHA_OPERACION_NO_COINCIDE", new HashMap<String, String>(), con, in.idEmpresa));
                out.P_OUT_ERRORESCONTADOR++;
                out.P_OUT_DESCRIPCION = "Fecha de operacion no coincide con hoy.";
                logger.error("Fecha de operacion no coincide con hoy: {}", in.fechaOperacion);
                return out;
            }
        } catch (DateTimeParseException e) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_FECHA_OPERACION_INVALIDA",
                    new HashMap<String, String>(), con, in.idEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "Formato de fecha de operacion invalido.";
            logger.error("Formato de fecha de operacion invalido: {}", in.fechaOperacion);
            return out;
        }

        // Validacion de formato de hora (HH:mm:ss)
        final DateTimeFormatter HORA_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");
        final String horaOperacionRaw = in.horaOperacion;

        LocalTime horaOperacion;
        try {
            horaOperacion = LocalTime.parse(horaOperacionRaw, HORA_FMT);
        } catch (DateTimeParseException e) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_HORA_OPERACION_INVALIDA",
                    new HashMap<>(), con, in.idEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "Formato de hora de operacion invalido (se espera HH:mm:ss).";
            logger.error("Hora de operacion invalida: {}", horaOperacionRaw, e);
            return out;
        }

        HashMap<String, Object> HoraParams = new HashMap<>();
        List<ParametroHora> horaRows = sql.query(
                con,
                SQL_SelectHora,
                HoraParams,
                ParametroHora.class);

        if (horaRows.isEmpty()) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,"ERROR_VALIDACION_HORA_INGRESO", 
                    new HashMap<String, String>(), con, in.idEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "Error al validar hora de la operacion.";
            logger.error("Error al validar hora de la operacion.");
            return out;
        }
        
        String horaMaximaRaw = horaRows.get(0).Valor;
        LocalTime horaMaxima;
        try {
            horaMaxima = LocalTime.parse(horaMaximaRaw, HORA_FMT);
        } catch (DateTimeParseException e) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_PARAMETRO_HORA_MAXIMA_INVALIDA",
                    new HashMap<>(), con, in.idEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "Parametro de hora maxima invalido en la configuracion.";
            logger.error("Hora maxima invalida (Valor): {}", horaMaximaRaw, e);
            return out;
        }
        
        if (horaOperacion.isAfter(horaMaxima)) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_HORA_OPERACION_SUPERA_MAXIMO",
                    new HashMap<>(), con, in.idEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = String.format(
                    "La hora de la operacion (%s) no puede ser mayor que la hora maxima permitida (%s).",
                    horaOperacion.format(HORA_FMT), horaMaxima.format(HORA_FMT));
            logger.error("Hora de operacion {} supera el maximo permitido {}", horaOperacion, horaMaxima);
            return out;
        }

        logger.info("Hora de operacion {} valida (maximo permitido {}).", horaOperacion, horaMaxima);

        // Validacion de tipo de operacion
        if (in.tipoOperacion == null || in.tipoOperacion.isEmpty() ||
                (!in.tipoOperacion.equals("1") && !in.tipoOperacion.equals("2"))) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_TIPO_OPERACION_INVALIDO",
                    new HashMap<String, String>(), con, in.idEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "Tipo de operacion invalido.";
            logger.error("Tipo de operacion invalido: " + in.tipoOperacion);
            return out;
        }

        // Insercion de IdDocumento
        String version = in.fechaOperacion.substring(0, 4) + "/" + in.fechaOperacion.substring(5, 7);

        HashMap<String, Object> idDocParams = new HashMap<>();
        idDocParams.put("esquema", "REPO3P1");
        idDocParams.put("version", version);

        List<IdDocumento> idDocRows = sql.query(
                con,
                SQL_SelectIdDocumento,
                idDocParams,
                IdDocumento.class);

        if (idDocRows.isEmpty()) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_NO_ID_DOCUMENTO",
                    new HashMap<String, String>(), con, in.idEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "No se pudo obtener el IdDocumento para la version: " + version;
            logger.error("No se pudo obtener el IdDocumento para la version: {}", version);
            return out;
        }

        Long idDocumento = idDocRows.get(0).IdDocumento;
        logger.info("IdDocumento obtenido: {}", idDocumento);

        // Validacion de evento de reporte
        if (in.eventoReporte == null || in.eventoReporte.isEmpty() || in.eventoReporte.length() != 1) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_EVENTO_REPORTE_INVALIDO",
                    new HashMap<String, String>(), con, in.idEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "Evento de reporte invalido.";
            logger.error("Evento de reporte invalido: " + in.eventoReporte);
            return out;
        }

        // Validacion de identificador
        if (in.identificador == null || in.identificador.isEmpty()) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_IDENTIFICADOR_INVALIDO",
                    new HashMap<String, String>(), con, in.idEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "Identificador invalido.";
            logger.error("Identificador invalido: " + in.identificador);
            return out;
        }

        boolean rutBlank = isNullOrBlank(in.rutContraparte);
        boolean leiBlank = isNullOrBlank(in.leiContraparte);

        if (rutBlank && leiBlank) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_RUT_O_LEI_CONTRAPARTE_OBLIGATORIO",
                    new HashMap<String, String>(), con, in.idEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "Debe indicarse rutContraparte o leiContraparte (al menos uno).";
            logger.error("Faltan identificadores de contraparte: rutContraparte y leiContraparte vacios.");
            return out;
        }


        // Validacion de rut contraparte
        if (!isNullOrBlank(in.rutContraparte) && !ValidacionRut(in.rutContraparte)) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_RUT_CONTRAPARTE_INVALIDO",
                    new HashMap<String, String>(), con, in.idEmpresa
            ));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "Rut de contraparte invalido.";
            logger.error("Rut contraparte invalido: " + in.rutContraparte);
            return out;
        }

        // LEI contraparte (opcional): solo validar largo si viene con contenido
        if (!isNullOrBlank(in.leiContraparte) && in.leiContraparte.length() > 20) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_LEI_CONTRAPARTE_INVALIDO",
                    new HashMap<String, String>(), con, in.idEmpresa
            ));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "LEI de contraparte invalido.";
            logger.error("LEI de contraparte invalido: " + in.leiContraparte);
            return out;
        }

        // Validacion de nombre de contraparte
        if (in.nombreContraparte == null || in.nombreContraparte.isEmpty()) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,
                    "ERROR_NOMBRE_CONTRAPARTE_INVALIDO", new HashMap<String, String>(), con, in.idEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "Nombre de contraparte invalido.";
            logger.error("Nombre de contraparte invalido: " + in.nombreContraparte);
            return out;
        }

        // Validacion de plataforma de negociacion
        if (in.plataformaNegociacion == null || in.plataformaNegociacion.isEmpty()) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,
                    "ERROR_PLATAFORMA_NEGOCIACION_INVALIDA", new HashMap<String, String>(), con, in.idEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "Plataforma de negociacion invalida.";
            logger.error("Plataforma de negociacion invalida: " + in.plataformaNegociacion);
            return out;
        }

        HashMap<String, Object> diccParams = new HashMap<>();
        List<diccionarioResult> diccionarios = sql.query(
                con,
                SQL_SelectDiccionario,
                diccParams,
                diccionarioResult.class);
        
        if (diccionarios.isEmpty()) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,"ERROR_VALIDACION_DICCIONARIOS", 
                    new HashMap<String, String>(), con, in.idEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "Error al validar diccionarios de plataforma de negociacion.";
            logger.error("Error al validar diccionarios de plataforma de negociacion");
            return out;
        }

        if (diccionarios.stream().noneMatch(d -> d.Codigo.equals(in.plataformaNegociacion))) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,"ERROR_PLATAFORMA_NEGOCIACION_NO_EXISTE", 
                    new HashMap<String, String>(), con, in.idEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "Plataforma de negociacion no existe en diccionario.";
            logger.error("Plataforma de negociacion no existe en diccionario: " + in.plataformaNegociacion);
            return out;
        }

        logger.info("Plataforma de negociacion validada: {}", in.plataformaNegociacion);
        
        // Validacion de monto comprado
        if (in.montoMonedaComprada == null || in.montoMonedaComprada.compareTo(BigDecimal.ZERO) < 0) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_MONEDA_COMPRADA_INVALIDA",
                    new HashMap<String, String>(), con, in.idEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "Monto de moneda comprada invalido.";
            logger.error("Monto de moneda comprada invalido: " + in.montoMonedaComprada);
            return out;
        }

        // Validacion de monto vendido
        if (in.montoMonedaVendida == null || in.montoMonedaVendida.compareTo(BigDecimal.ZERO) < 0) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_MONEDA_VENDIDA_INVALIDA",
                    new HashMap<String, String>(), con, in.idEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "Monto de moneda vendida invalido.";
            logger.error("Monto de moneda vendida invalido: " + in.montoMonedaVendida);
            return out;
        }

        // Validacion de que no pueden ser iguales los montos
        if (in.montoMonedaComprada.compareTo(in.montoMonedaVendida) == 0) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_MONEDAS_IGUALES",
                    new HashMap<String, String>(), con, in.idEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "Los montos de moneda comprada y vendida no pueden ser iguales.";
            logger.error("Los montos de moneda comprada y vendida no pueden ser iguales");
            return out;
        }

        // Validacion de tipo de cambio
        if (in.tipoCambio == null || in.tipoCambio.compareTo(BigDecimal.ZERO) <= 0) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_TIPO_CAMBIO_INVALIDO",
                    new HashMap<String, String>(), con, in.idEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "Tipo de cambio invalido.";
            logger.error("Tipo de cambio invalido: " + in.tipoCambio);
            return out;
        }

        // Validacion de que el reporte no exista para el folio
        HashMap<String, Object> FolioParams = new HashMap<>();
        FolioParams.put("rutReportante", in.rutReportante);
        FolioParams.put("fechaOperacion", in.fechaOperacion);
        FolioParams.put("horaOperacion", in.horaOperacion);
        FolioParams.put("identificador", in.identificador);

        List<HashMap> folioRows = sql.query(
                con,
                SQL_SelectReporte,
                FolioParams,
                HashMap.class);

        if (!folioRows.isEmpty()) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_REPORTE_EXISTE",
                    new HashMap<String, String>(), con, in.idEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "La operacion ya existe para el folio.";
            logger.error(
                    "El reporte ya existe para el folio: rutReportante={}, fechaOperacion={}, horaOperacion={}, identificador={}",
                    in.rutReportante, in.fechaOperacion, in.horaOperacion, in.identificador);
            return out;
        }

        // Insercion de id
        HashMap<String, Object> idParams = new HashMap<>();
        List<NextIdResult> rows = sql.query(
                con,
                SQL_SelectId,
                idParams,
                NextIdResult.class);

        if (rows.isEmpty()) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_NO_ID_DISPONIBLE",
                    new HashMap<String, String>(), con, in.idEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "No se pudo obtener el siguiente ID para el reporte.";
            logger.error("No se pudo obtener el siguiente ID para el reporte");
            return out;
        }

        Long nextId = rows.get(0).idQuery;
        logger.info("Siguiente ID obtenido: {}", nextId);

        // Preparar parametros para la insercion
        EntradaReporte3p1 parametros = new EntradaReporte3p1();
        parametros.id = nextId;
        parametros.idDocumento = idDocumento;
        parametros.fuenteDatos = "WEBSERVICE";
        parametros.estado = "ACT";
        parametros.estadoSICAM = "";
        parametros.sicam = "";
        parametros.tipoRegistro = in.tipoRegistro;
        parametros.rutReportante = in.rutReportante;
        parametros.fechaOperacion = in.fechaOperacion;
        parametros.horaOperacion = in.horaOperacion;
        parametros.tipoOperacion = in.tipoOperacion;
        parametros.eventoReporte = in.eventoReporte;
        parametros.identificador = in.identificador;
        parametros.rutContraparte = isNullOrBlank(in.rutContraparte) ? "" : in.rutContraparte;
        parametros.leiContraparte = isNullOrBlank(in.leiContraparte) ? "" : in.leiContraparte;
        parametros.nombreContraparte = in.nombreContraparte;
        parametros.plataformaNegociacion = in.plataformaNegociacion;
        parametros.montoMonedaComprada = in.montoMonedaComprada;
        parametros.montoMonedaVendida = in.montoMonedaVendida;
        parametros.tipoCambio = in.tipoCambio;
        parametros.idCliente = Long.valueOf(0);
        parametros.idOperacion = Long.valueOf(0);
        parametros.idTipoOperacion = "";
        parametros.nemo = "";
        parametros.idMonedaPrincipal = "";
        parametros.idMonedaSecundaria = "";

        List<EntradaReporte3p1> batchParams = Collections.singletonList(parametros);
        int[] resultados = sql.batch(
                con,
                SQL_InsertReporte,
                batchParams,
                EntradaReporte3p1.class);

        // Verificar resultados de la insercion
        if (resultados.length > 0 && resultados[0] > 0) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, "REPORTE_INGRESADO",
                    new HashMap<String, String>(), con, in.idEmpresa));
            out.P_OUT_ERRORESCONTADOR = 0L;
            out.P_OUT_DESCRIPCION = "Reporte ingresado exitosamente.";
        } else {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_INSERTAR_REPORTE",
                    new HashMap<String, String>(), con, in.idEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "Error al insertar el reporte.";
            logger.error("Error al insertar el reporte: " + in.identificador);
            return out;
        }

        logger.info("Reporte ingresado con exito: " + resultados.length);
        return out;
    }

    public boolean ValidacionRut(String rut) {
        String soloDigitos = rut.replaceAll("\\D", "");
        if (soloDigitos.length() > 11) {
            return false;
        }
        String[] partes = rut.split("-");
        if (partes.length != 2) {
            return false;
        }
        String numero = partes[0];
        String dv = partes[1].toUpperCase();
        if (!numero.matches("\\d+") || !dv.matches("[0-9K]")) {
            return false;
        }
        return true;
    }

    /**
     * Carga el contenido de un recurso SQL como una cadena de texto.
     *
     * @param conn Conexion a la base de datos.
     * @param in   Entrada en formato JSON que contiene los parametros del reporte.
     * @return Un objeto JSON que contiene el resultado de la operacion.
     */
    @Override
    public JsonObject call(Connection conn, JsonObject in) throws Exception {
        Gson gson = new GsonBuilder().create();
        EntradaReporte3p1 entrada = gson.fromJson(in, EntradaReporte3p1.class);

        SalidaReporte3p1 out = ingresarReporte(conn, entrada);

        JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
        return dataout;
    }

    /**
     * Metodo principal para probar la insercion de un reporte.
     *
     * @param args Argumentos de linea de comandos (no se utilizan).
     */
    public static void main(String[] args) {
        try (Connection con = PlataformaUtils.getPropertiesConnection("desa01",
                "C:\\Users\\vaguilera_ext\\Documents\\GIT_OPTIMUS\\optimus-corredores\\connections.properties")) {

            try {
                Gson gson = Utils.getGsonBuilder();
                String json = "{\n" +
                        "    \"idEmpresa\": 1,\n" +
                        "    \"idUsuario\": \"testUser\",\n" +
                        "    \"claveUsuario\": \"testPassword\",\n" +
                        "    \"claveReporte\": \"testClave\",\n" +
                        "    \"tipoRegistro\": \"01\",\n" +
                        "    \"rutReportante\": \"76011193-7\",\n" +
                        "    \"fechaOperacion\": \"2025-10-17\",\n" +
                        "    \"horaOperacion\": \"13:00:00\",\n" +
                        "    \"tipoOperacion\": \"1\",\n" +
                        "    \"eventoReporte\": \"1\",\n" +
                        "    \"identificador\": \"7222212\", \n" +
                        "    \"rutContraparte\": \"20720657-1\",\n" +
                        "    \"leiContraparte\": \"\",\n" +
                        "    \"nombreContraparte\": \"JOSE GUILLERMO SCHAFF ZAMORA\",\n" +
                        "    \"plataformaNegociacion\": \"XBCL\",\n" +
                        "    \"montoMonedaComprada\": 123123,\n" +
                        "    \"montoMonedaVendida\": 0,\n" +
                        "    \"tipoCambio\": 931.83,\n" +
                        "    \"idCliente\": 77380,\n" +
                        "    \"idOperacion\": 722144,\n" +
                        "    \"idTipoOperacion\": \"COMSPT\",\n" +
                        "    \"nemo\": \"DISTRI\",\n" +
                        "    \"idMonedaPrincipal\": \"USD\",\n" +
                        "    \"idMonedaSecundaria\": \"CLP\"\n" +
                        "}";
                EntradaReporte3p1 Objin = gson.fromJson(json, EntradaReporte3p1.class);

                IngresarReportes service = new IngresarReportes();

                service.ingresarReporte(con, Objin);
                con.commit();
            } catch (Exception e) {
                logger.error("Error al ingresar reporte", e);
                con.rollback();
            }
        } catch (Exception e) {
            logger.error("Error al conectar a la base de datos", e);
        }
    }
}