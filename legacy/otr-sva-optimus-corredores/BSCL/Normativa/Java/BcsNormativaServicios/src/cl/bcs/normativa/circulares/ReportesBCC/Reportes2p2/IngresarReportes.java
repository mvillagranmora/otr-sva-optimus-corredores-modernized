package cl.bcs.normativa.circulares.ReportesBCC.Reportes2p2;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.temporal.TemporalAdjusters;


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
import static cl.bcs.normativa.circulares.ReportesBCC.Reportes2p2.utils.ReporteUtils.*;

/**
 * Servicio para ingresar reportes SICAM / Merrill Lynch
 */
public class IngresarReportes implements JavaCall {

    static final Logger logger = LogManager.getLogger(IngresarReportes.class);

    /**
     * Llamadas SQL
     */
    private final String SQL_InsertReporte = loadResourceToString(
            "cl/bcs/normativa/circulares/ReportesBCC/Reportes2p2/sql/IngresarReporte_Reporte_insert.sql");
    private final String SQL_SelectIdDocumento = loadResourceToString(
            "cl/bcs/normativa/circulares/ReportesBCC/Reportes2p2/sql/ConsultarReportes_Documento_select.sql");
    private final String SQL_SelectId = loadResourceToString(
            "cl/bcs/normativa/circulares/ReportesBCC/Reportes2p2/sql/ConsultarReportes_IDReporte_select.sql");
    private final String SQL_SelectReporte = loadResourceToString(
            "cl/bcs/normativa/circulares/ReportesBCC/Reportes2p2/sql/ConsultarReportes_Reporte_select.sql");
    private final String SQL_SelectDiaHabil = loadResourceToString(
            "cl/bcs/normativa/circulares/ReportesBCC/Reportes2p2/sql/ConsultarReportes_DiaHabil_select.sql");

    /**
     * Clase de entrada para ingreso de reporte 2.2
     */
    public static class EntradaReporte2p2 extends BcsPojo {
        public Long IdEmpresa;
        public String IdUsuario;
        public String ClaveUsuario;
        public String ClaveReporte;
        // Datos de control
        public Long Id; // SQL
        public Long IdDocumento; // SQL
        public String FuenteDatos; // WEBSERVICE
        public String Estado; //ACT
        public String EstadoSICAM; // VACIO
        public String SICAM; // VACIO
        // Datos basicos reporte
        public String TipoRegistro; 
        public String RutEntidadMCF;
        public String Fecha; 
        public BigDecimal SaldoCuentaCambioConversion; 
        public BigDecimal SaldoMonedaOriginal; 
        public String MonedaSaldo; 
        public BigDecimal Paridad;
        // Datos complementarios
        public Long IdCuentaInversion; // VACIO
        public Long IdCliente; // VACIO
        public Long IdCargoAbonoMovimiento; // VACIO
        public String IdTipoMovimiento; // VACIO
        public String FechaEfectivaMovimiento; // VACIO

        public EntradaReporte2p2() {
            this.IdEmpresa = 1L;
            this.IdUsuario = "";
            this.ClaveUsuario = "";
            this.ClaveReporte = "";
        }
    }

    /**
     * Clase de salida para ingreso de reporte 2.2
     */
    public static class SalidaReporte2p2 extends BcsPojo {
        public List<MensajeSalida> P_OUT_MENSAJES;
        public Long P_OUT_ERRORESCONTADOR;
        public String P_OUT_DESCRIPCION;

        public SalidaReporte2p2() {
            this.P_OUT_MENSAJES = new ArrayList<>();
            this.P_OUT_ERRORESCONTADOR = 0L;
            this.P_OUT_DESCRIPCION = "";
        }
    }

    public static class NextIdResult {
        public Long idQuery;
    }

    public static class IdDocumento {
        public Long IdDocumento;
    }

    public static class FechaHasta {
        public Date FechaHasta;
    }
    
    /**
     * Carga el contenido de un recurso SQL como una cadena de texto.
     * 
     * @param con               Conexion a la base de datos.
     * @param EntradaReporte2p2 entrada con los parametros del reporte.
     * @return Contenido del recurso como una cadena de texto.
     * @throws Exception Si ocurre un error al cargar el recurso.
     */
    @Trace
    public SalidaReporte2p2 ingresarReporte(Connection con, EntradaReporte2p2 in) throws Exception {
        logger.info("Ingresando reporte 2.2");
        logger.info("Parametros de entrada: {}", in);
        SalidaReporte2p2 out = new SalidaReporte2p2();
        final DateTimeFormatter DF_YMD = DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);
        SQL sql = new SQL();
        LocalDate fecha = null;
        LocalDate fechaHasta = null;
        String version = null;
        HashMap<String, Object> ParamsQuery = new HashMap<>();

        if (in == null) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_ENTRADA_NULA",
                    new HashMap<>(), con, 0L));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "Entrada nula.";
            logger.error("Entrada nula");
            return out;
        }

        if (in.RutEntidadMCF == null || in.Fecha == null || in.MonedaSaldo == null) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_ENTRADA_INCOMPLETA",
                    new HashMap<>(), con, in.IdEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "Datos del folio incompletos.";
            logger.error("Datos de folio incompletos: RutEntidadMCF={}, Fecha={}, MonedaSaldo={}",
                    in.RutEntidadMCF, in.Fecha, in.MonedaSaldo);
            return out;
        }
                
        if (!"01".equals(in.TipoRegistro)) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,"ERROR_TIPO_REGISTRO_INVALIDO", new HashMap<>(), con, in.IdEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "El TipoRegistro debe ser '01'.";
            logger.error("TipoRegistro invalido: {}", in.TipoRegistro);
            return out;
        }

        if (isNullOrBlank(in.RutEntidadMCF) || !validacionRut(in.RutEntidadMCF)) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_RUT_ENTIDAD_MCF_INVALIDO",
                    new HashMap<String, String>(), con, in.IdEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "El Rut Entidad MCF invalido.";
            logger.error("Rut Entidad MCF invalido: " + in.RutEntidadMCF);
            return out;
        }

        try {
            if (in.Fecha == null) throw new NullPointerException("null");
            fecha = LocalDate.parse(in.Fecha.trim(), DF_YMD);
        
            version = in.Fecha.substring(0, 4) + "/" + in.Fecha.substring(5, 7);
        
            ParamsQuery = new HashMap<>();
            ParamsQuery.put("esquema", "REPO2P2");
            ParamsQuery.put("version", version);
        
            List<FechaHasta> FechaHastaRows = sql.query(
                    con,
                    SQL_SelectDiaHabil,
                    ParamsQuery,
                    FechaHasta.class);
        
            if (FechaHastaRows.isEmpty() || FechaHastaRows.get(0).FechaHasta == null) {
                out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_FECHA_HABIL_NO_ENCONTRADA",
                        new HashMap<String, String>(), con, in.IdEmpresa));
                out.P_OUT_ERRORESCONTADOR++;
                out.P_OUT_DESCRIPCION = "No se pudo obtener el ultimo dia habil para la version: " + version;
                logger.error("No se pudo obtener el ultimo dia habil para la version: {}", version);
                return out;
            }
        
            fechaHasta = FechaHastaRows.get(0).FechaHasta.toLocalDate();
        
            if (!fecha.equals(fechaHasta)) {
                out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_FECHA_DIA_HABIL",
                        new HashMap<String, String>(), con, in.IdEmpresa));
                out.P_OUT_ERRORESCONTADOR++;
                out.P_OUT_DESCRIPCION = "La fecha debe ser el ultimo dia habil del mes.";
                logger.error("La fecha debe ser el ultimo dia habil del mes.");
                return out;
            }
        
        } catch (DateTimeParseException | NullPointerException e) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_FECHA_FORMATO",
                    new HashMap<>(), con, in.IdEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "Fecha invalida, el formato debe ser YYYY-MM-DD.";
            return out;
        }

        List<IdDocumento> idDocRows = sql.query(
                con,
                SQL_SelectIdDocumento,
                ParamsQuery,
                IdDocumento.class);

        if (idDocRows.isEmpty()) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_NO_ID_DOCUMENTO",
                    new HashMap<String, String>(), con, in.IdEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "No se pudo obtener el IdDocumento para la version: " + version;
            logger.error("No se pudo obtener el IdDocumento para la version: {}", version);
            return out;
        }

        Long idDocumento = idDocRows.get(0).IdDocumento;
        logger.info("IdDocumento obtenido: {}", idDocumento);
        
        if (!isNum20_2(in.SaldoCuentaCambioConversion)) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_SALDO_CTA_CAMBIO_CONVERSION_INVALIDO", 
                    new HashMap<>(), con, in.IdEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "El Saldo Cuenta Cambio Conversion obligatorio, con hasta 18 enteros y 2 decimales.";
            logger.error("SaldoCuentaCambioConversion invalido: {}", in.SaldoCuentaCambioConversion);
            return out;
        }
        
        if (!isNum20_2(in.SaldoMonedaOriginal)) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_SALDO_MONEDA_ORIGINAL_INVALIDO", 
                    new HashMap<>(), con, in.IdEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "El Saldo Moneda Original obligatorio, con hasta 18 enteros y 2 decimales.";
            logger.error("SaldoMonedaOriginal invalido: {}", in.SaldoMonedaOriginal);
            return out;
        }

         if (isNullOrBlank(in.MonedaSaldo) || !isIso4217(in.MonedaSaldo)) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_MONEDA_SALDO_INVALIDA",
            		new HashMap<>(), con, in.IdEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "La Moneda del Saldo obligatoria y debe ser codigo ISO 4217 (3 letras).";
            logger.error("MonedaSaldo invalida: {}", in.MonedaSaldo);
            return out;
        }
        in.MonedaSaldo = norm(in.MonedaSaldo);
        
        if (!isNum10_5Pos(in.Paridad)) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,
                "ERROR_PARIDAD_INVALIDA", new HashMap<>(), con, in.IdEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "Paridad obligatoria, con hasta 5 enteros y 5 decimales.";
            logger.error("Paridad invalida: {}", in.Paridad);
            return out;
        }

        HashMap<String, Object> FolioParams = new HashMap<>();
        FolioParams.put("rutEntidadMCF", in.RutEntidadMCF);
        FolioParams.put("fecha", in.Fecha);
        FolioParams.put("monedaSaldo", in.MonedaSaldo);

        List<HashMap> folioRows = sql.query(
                con,
                SQL_SelectReporte,
                FolioParams,
                HashMap.class);

        if (!folioRows.isEmpty()) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_REPORTE_EXISTE",
                    new HashMap<String, String>(), con, in.IdEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "La operacion ya existe para el folio.";
            logger.error("El reporte ya existe para el folio: RUTEntidadMCF={}, Fecha={}, MonedaSaldo={}",
                    in.RutEntidadMCF, in.Fecha, in.MonedaSaldo);
            return out;
        }

        HashMap<String, Object> idParams = new HashMap<>();
        List<NextIdResult> rows = sql.query(
                con,
                SQL_SelectId,
                idParams,
                NextIdResult.class);

        if (rows.isEmpty()) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_NO_ID_DISPONIBLE",
                    new HashMap<String, String>(), con, in.IdEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "No se pudo obtener el siguiente ID para el reporte.";
            logger.error("No se pudo obtener el siguiente ID para el reporte");
            return out;
        }

        Long nextId = rows.get(0).idQuery;
        logger.info("Siguiente ID obtenido: {}", nextId);

        // Preparar parametros para la insercion
        EntradaReporte2p2 parametros = new EntradaReporte2p2();
        parametros.Id = nextId;
        parametros.IdDocumento = idDocumento;
        parametros.FuenteDatos = "WEBSERVICE";
        parametros.Estado = "ACT";
        parametros.EstadoSICAM = "";
        parametros.SICAM = "";
        parametros.TipoRegistro = in.TipoRegistro;
        parametros.RutEntidadMCF = in.RutEntidadMCF;
        parametros.Fecha = in.Fecha;
        parametros.SaldoCuentaCambioConversion = in.SaldoCuentaCambioConversion;
        parametros.SaldoMonedaOriginal = in.SaldoMonedaOriginal;
        parametros.MonedaSaldo = in.MonedaSaldo;
        parametros.Paridad = in.Paridad;
        parametros.IdCuentaInversion = Long.valueOf(0);
        parametros.IdCliente = Long.valueOf(0);
        parametros.IdCargoAbonoMovimiento = Long.valueOf(0);
        parametros.IdTipoMovimiento = "";
        parametros.FechaEfectivaMovimiento = "";

        List<EntradaReporte2p2> batchParams = Collections.singletonList(parametros);
        int[] resultados = sql.batch(
                con,
                SQL_InsertReporte,
                batchParams,
                EntradaReporte2p2.class);

        if (resultados.length > 0 && resultados[0] > 0) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, "REPORTE_INGRESADO",
                    new HashMap<String, String>(), con, in.IdEmpresa));
            out.P_OUT_ERRORESCONTADOR = 0L;
            out.P_OUT_DESCRIPCION = "Reporte ingresado exitosamente.";
        } else {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_INSERTAR_REPORTE",
                    new HashMap<String, String>(), con, in.IdEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "Error al insertar el reporte.";
            logger.error("Error al insertar el reporte");
            return out;
        }

        logger.info("Reporte ingresado con exito: " + resultados.length);
        return out;
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
        EntradaReporte2p2 entrada = gson.fromJson(in, EntradaReporte2p2.class);

        SalidaReporte2p2 out = ingresarReporte(conn, entrada);

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
                        "    \"IdEmpresa\": 1,\n" +
                        "    \"IdUsuario\": \"testUser\",\n" +
                        "    \"ClaveUsuario\": \"testPassword\",\n" +
                        "    \"ClaveReporte\": \"testClave\",\n" +
                        "    \"TipoRegistro\": \"01\",\n" +
                        "    \"RutEntidadMCF\": \"76011193-7\",\n" +
                        "    \"Fecha\": \"2025-10-30\",\n" +
                        "    \"SaldoCuentaCambioConversion\": 1000000.00,\n" +
                        "    \"SaldoMonedaOriginal\": 800000.00,\n" +
                        "    \"MonedaSaldo\": \"USD\",\n" +
                        "    \"Paridad\": 0.80000\n" +
                        "}";
                EntradaReporte2p2 Objin = gson.fromJson(json, EntradaReporte2p2.class);

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