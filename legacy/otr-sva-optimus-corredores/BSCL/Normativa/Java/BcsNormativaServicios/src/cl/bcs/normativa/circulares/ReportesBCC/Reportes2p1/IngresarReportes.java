package cl.bcs.normativa.circulares.ReportesBCC.Reportes2p1;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

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
            "cl/bcs/normativa/circulares/ReportesBCC/Reportes2p1/sql/IngresarReporte_Reporte_insert.sql");
    private final String SQL_SelectId = loadResourceToString(
            "cl/bcs/normativa/circulares/ReportesBCC/Reportes2p1/sql/ConsultarReportes_IDReporte_select.sql");
    private final String SQL_SelectReporte = loadResourceToString(
            "cl/bcs/normativa/circulares/ReportesBCC/Reportes2p1/sql/ConsultarReportes_Reporte_select.sql");
    private final String SQL_SelectIdDocumento = loadResourceToString(
            "cl/bcs/normativa/circulares/ReportesBCC/Reportes2p1/sql/ConsultarReportes_Documento_select.sql");
    private final String SQL_SelectDiccionario = loadResourceToString(
            "cl/bcs/normativa/circulares/ReportesBCC/Reportes2p1/sql/ConsultarReportes_Diccionario_select.sql");
    private final String SQL_SelectCodigoOperacion = loadResourceToString(
            "cl/bcs/normativa/circulares/ReportesBCC/Reportes2p1/sql/ConsultarReportes_CodigoOperacion_select.sql");

    /**
     * Clase de entrada para ingreso de reporte 2.1
     */
    public static class EntradaReporte2p1 extends BcsPojo {
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
        public String EventoReporte;
        public String TipoOperacion; 
        public String FechaNegociacion; 
        public String HoraNegociacion; 
        public String FechaValutaMonedaRecibida;
        public String FechaValutaMonedaEntregada;
        public String Identificador;
        public String RutContraparte1;
        public String LeiContraparte1;
        public String RutContraparte2;
        public String NombreContraparte1;
        public String LeiContraparte2;
        public String NombreContraparte2;
        public String PaisContraparte2;
        public String RutIntermediario;
        public String PlataformaNegociacion;
        public String CodigoOperacionCambio;
        public String PaisMonedaExtranjera; 
        public String MonedaCompradaRecibida; 
        public BigDecimal MontoMonedaCompradaRecibida;
        public String MonedaVendidaEntregada;
        public BigDecimal MontoMonedaVendidaEntregada;
        public String ConvencionTipoCambio;
        public BigDecimal TipoCambio;
        public String AfectaDerivados;
        public String IdentificadorContratoDerivados;
        public String FechaSuscripcionContrato;
        public String HoraSuscripcionContrato;
        public String OperacionEmpresaRelacionada;
        public String NumeroInscripcionObligacionExterior;
        public String NombreFinancistaAporante;
        public String LeiFinancistaAporante;
        public String NombreReceptorInversion;
        public String LeiReceptorInversion;
        public String ModalidadInversionCustodio;
        // Datos complementarios
        public Long FolioOperacion; // VACIO
        public Long IdOperacion; // VACIO
        public Long IdCartera; // VACIO
        public String FechaLiquidacion; // VACIO
        public Long IdCliente; // VACIO
        public String IdTipoOperacion; // VACIO
        public String Nemo; // VACIO
        public String IdMonedaPrincipal; // VACIO
        public String IdMonedaSecundaria; // VACIO

        public EntradaReporte2p1() {
            this.IdEmpresa = 1L;
            this.IdUsuario = "";
            this.ClaveUsuario = "";
            this.ClaveReporte = "";
        }
    }

    /**
     * Clase de salida para ingreso de reporte 2.1
     */
    public static class SalidaReporte2p1 extends BcsPojo {
        public List<MensajeSalida> P_OUT_MENSAJES;
        public Long P_OUT_ERRORESCONTADOR;
        public String P_OUT_DESCRIPCION;

        public SalidaReporte2p1() {
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

    private static final Set<String> ISO3 = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
        "ABW","AFG","AGO","AIA","ALA","ALB","AND","ARE","ARG","ARM","ASM","ATA","ATF","ATG","AUS","AUT","AZE","BDI","BEL","BEN",
        "BES","BFA","BGD","BGR","BHR","BHS","BIH","BLM","BLR","BLZ","BMU","BOL","BRA","BRB","BRN","BTN","BVT","BWA","CAF","CAN",
        "CCK","CHE","CHL","CHN","CIV","CMR","COD","COG","COK","COL","COM","CPV","CRI","CUB","CUW","CXR","CYM","CYP","CZE","DEU",
        "DJI","DMA","DNK","DOM","DZA","ECU","EGY","ERI","ESH","ESP","EST","ETH","FIN","FJI","FLK","FRA","FRO","FSM","GAB","GBR",
        "GEO","GGY","GHA","GIB","GIN","GLP","GMB","GNB","GNQ","GRC","GRD","GRL","GTM","GUF","GUM","GUY","HKG","HMD","HND","HRV",
        "HTI","HUN", "IDN","IMN","IND","IOT","IRL","IRN","IRQ","ISL","ISR","ITA","JAM","JEY","JOR","JPN","KAZ","KEN","KGZ","KHM",
        "KIR","KNA","KOR","KWT","LAO","LBN","LBR","LBY","LCA","LIE","LKA","LSO","LTU","LUX","LVA","MAC","MAF","MAR","MCO","MDA",
        "MDG","MDV","MEX","MHL","MKD","MLI","MLT","MMR","MNE","MNG","MNP","MOZ","MRT","MSR","MTQ","MUS","MWI","MYS","MYT", "NAM",
        "NCL","NER","NFK","NGA","NIC","NIU","NLD","NOR","NPL","NRU","NZL","OMN","PAK","PAN","PCN","PER","PHL","PLW","PNG","POL",
        "PRI","PRK","PRT","PRY","PSE","PYF","QAT","REU","ROU","RUS","RWA","SAU","SDN","SEN","SGP","SGS","SHN","SJM","SLB","SLE",
        "SLV","SMR","SOM","SPM","SRB","SSD","STP","SUR","SVK","SVN","SWE","SWZ","SXM","SYC","SYR","TCA","TCD","TGO","THA","TJK",
        "TKL","TKM","TLS","TON","TTO","TUN","TUR","TUV","TWN","TZA","UGA","UKR","UMI","URY","USA","UZB","VAT","VCT","VEN","VGB",
        "VIR","VNM","VUT","WLF","WSM","YEM","ZAF","ZMB","ZWE"
    )));

    private static final Set<String> ISO4217 = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
        "AED","AFN","ALL","AMD","ANG","AOA","ARS","AUD","AWG","AZN","BAM","BBD","BDT","BGN","BHD","BIF","BMD","BND","BOB","BOV",
        "BRL","BSD","BTN","BWP","BYN","BZD","CAD","CDF","CHE","CHF","CHW","CLF","CLP","CNY","COP","COU","CRC","CUP","CVE","CZK",
        "DJF","DKK","DOP","DZD","EGP","ERN","ETB","EUR","FJD","FKP","GBP","GEL","GHS","GIP","GMD","GNF","GTQ","GYD","HKD","HNL",
        "HTG","HUF","IDR","ILS","INR","IQD","IRR","ISK","JMD","JOD","JPY","KES","KGS","KHR","KMF","KPW","KRW","KWD","KYD","KZT",
        "LAK","LBP","LKR","LRD","LSL","LYD","MAD","MDL","MGA","MKD","MMK","MNT","MOP","MRU","MUR","MVR","MWK","MXN","MXV","MYR",
        "MZN","NAD","NGN","NIO","NOK","NPR","NZD","OMR","PAB","PEN","PGK","PHP","PKR","PLN","PYG","QAR","RON","RSD","RUB","RWF",
        "SAR","SBD","SCR","SDG","SEK","SGD","SHP","SLE","SOS","SRD","SSP","STN","SVC","SYP","SZL","THB","TJS","TMT","TND","TOP",
        "TRY","TTD","TWD","TZS","UAH","UGX","USD","USN","UYI","UYU","UYW","UZS","VES","VND","VUV","WST","XAF","XAG","XAU","XBA",
        "XBB","XBC","XBD","XCD","XDR","XOF","XPD","XPF","XPT","XSU","XTS","XUA","XXX","YER","ZAR","ZMW","ZWG"
    )));

    private static String norm(String s) {
        return (s == null) ? null : s.trim().toUpperCase(Locale.ROOT);
    }
    
    private static boolean isIso3(String s) {
        String v = norm(s);
        return v != null && v.length() == 3 && ISO3.contains(v);
    }

    private static boolean isIso4217(String s) {
        String v = norm(s);
        return v != null && v.length() == 3 && ISO4217.contains(v);
    }
    private static boolean isFxConv4217(String s) {
        String v = norm(s);
        if (v == null || v.length() != 7 || v.charAt(3) != '/') return false;
        return isIso4217(v.substring(0, 3)) && isIso4217(v.substring(4, 7));
    }

    static boolean isNullOrBlank(String s){ return s == null || s.trim().isEmpty(); }

    private static final BigDecimal MAX_DEC26_8 = new BigDecimal("999999999999999999.99999999");

    static boolean inRangeDecimal26_8(BigDecimal v) {
        BigDecimal vv = v.stripTrailingZeros();
        return vv.signum() >= 0
            && vv.scale() <= 8
            && vv.compareTo(MAX_DEC26_8) <= 0;
    }
    
    private static String trimToNull(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s.trim();
    }

    private static String upperOrNull(String s) {
        String t = trimToNull(s);
        return (t == null) ? null : t.toUpperCase();
    }

    /**
     * Carga el contenido de un recurso SQL como una cadena de texto.
     * 
     * @param con               Conexion a la base de datos.
     * @param EntradaReporte2p1 entrada con los parametros del reporte.
     * @return Contenido del recurso como una cadena de texto.
     * @throws Exception Si ocurre un error al cargar el recurso.
     */
    @Trace
    public SalidaReporte2p1 ingresarReporte(Connection con, EntradaReporte2p1 in) throws Exception {
        logger.info("Ingresando reporte 2.1");
        logger.info("Parametros de entrada: {}", in);
        SalidaReporte2p1 out = new SalidaReporte2p1();
        final DateTimeFormatter DF_YMD = DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);
        SQL sql = new SQL();

        if (in == null) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_ENTRADA_NULA",
                    new HashMap<>(), con, 0L));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "Entrada nula.";
            logger.error("Entrada nula");
            return out;
        }

        if (in.RutContraparte1 == null || in.FechaNegociacion == null || in.HoraNegociacion == null || in.Identificador == null) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_ENTRADA_INCOMPLETA",
                    new HashMap<>(), con, in.IdEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "Datos del folio incompletos.";
            logger.error("Datos de folio incompletos: RUTContraparte1={}, FechaNegociacion={}, HoraNegociacion={}, Identificador={}",
                    in.RutContraparte1, in.FechaNegociacion, in.HoraNegociacion, in.Identificador);
            return out;
        }
                
        if (!"01".equals(in.TipoRegistro)) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,"ERROR_TIPO_REGISTRO_INVALIDO", new HashMap<>(), con, in.IdEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "TipoRegistro debe ser '01'.";
            logger.error("TipoRegistro invalido: {}", in.TipoRegistro);
            return out;
        }

        if (!"1".equals(in.EventoReporte)) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,"ERROR_EVENTO_REPORTE_INVALIDO", new HashMap<>(), con, in.IdEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "EventoReporte debe ser '1'.";
            logger.error("EventoReporte invalido: {}", in.EventoReporte);
            return out;
        }

        if (!("01".equals(in.TipoOperacion) || "02".equals(in.TipoOperacion) || "03".equals(in.TipoOperacion) || "04".equals(in.TipoOperacion))) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,"ERROR_TIPO_OPERACION_INVALIDO", new HashMap<>(), con, in.IdEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "TipoOperacion invalido. Validos: 01, 02, 03, 04.";
            logger.error("TipoOperacion invalido: {}", in.TipoOperacion);
            return out;
        }

        try {
            if (in.FechaNegociacion == null) throw new NullPointerException("null");
            LocalDate.parse(in.FechaNegociacion, DF_YMD);
        } catch (DateTimeParseException | NullPointerException e) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_FECHA_NEGOCIACION_FORMATO",
                    new HashMap<String, String>(), con, in.IdEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "Fecha de negociacion invalida, el formato debe ser YYYY-MM-DD.";
            return out;
        }

        // Insercion de IdDocumento
        String version = in.FechaNegociacion.substring(0, 4) + "/" + in.FechaNegociacion.substring(5, 7);

        HashMap<String, Object> idDocParams = new HashMap<>();
        idDocParams.put("esquema", "REPO2P1");
        idDocParams.put("version", version);

        List<IdDocumento> idDocRows = sql.query(
                con,
                SQL_SelectIdDocumento,
                idDocParams,
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

        try {
            LocalTime.parse(in.HoraNegociacion,
                    DateTimeFormatter.ofPattern("HH:mm:ss"));
        } catch (DateTimeParseException e) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_HORA_NEGOCIACION_INVALIDA",
                    new HashMap<String, String>(), con, in.IdEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "Formato de hora de negociacion invalido, el formato debe ser HH:MM:SS.";
            logger.error("Hora de negociacion invalida: {}", in.HoraNegociacion);
            return out;
        }
        String fvRec = (in.FechaValutaMonedaRecibida != null && !in.FechaValutaMonedaRecibida.trim().isEmpty())
        ? in.FechaValutaMonedaRecibida.trim() : null;
        String fvEnt = (in.FechaValutaMonedaEntregada != null && !in.FechaValutaMonedaEntregada.trim().isEmpty())
                ? in.FechaValutaMonedaEntregada.trim() : null;

        boolean es03o04 = "03".equals(in.TipoOperacion) || "04".equals(in.TipoOperacion);

        if (es03o04) {
            if (fvRec != null) {
                out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,"ERROR_FECHA_VALUTA_RECIBIDA_NO_PERMITIDA", 
                        new HashMap<String, String>(), con, in.IdEmpresa));
                out.P_OUT_ERRORESCONTADOR++;
                out.P_OUT_DESCRIPCION = "Fecha de valuta recibida no debe venir para TipoOperacion 03/04.";
                return out;
            }
            if (fvEnt != null) {
                out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,"ERROR_FECHA_VALUTA_ENTREGADA_NO_PERMITIDA", 
                        new HashMap<String, String>(), con, in.IdEmpresa));
                out.P_OUT_ERRORESCONTADOR++;
                out.P_OUT_DESCRIPCION = "Fecha de valuta entregada no debe venir para TipoOperacion 03/04.";
                return out;
            }
            in.FechaValutaMonedaRecibida = "0001-01-01";
            in.FechaValutaMonedaEntregada = "0001-01-01";
        } else {
            if (fvRec == null) {
                out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,"ERROR_FECHA_VALUTA_RECIBIDA_REQUERIDA", 
                        new HashMap<String, String>(), con, in.IdEmpresa));
                out.P_OUT_ERRORESCONTADOR++;
                out.P_OUT_DESCRIPCION = "Fecha de valuta recibida es obligatoria para TipoOperacion 01/02.";
                return out;
            }
            if (fvEnt == null) {
                out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_FECHA_VALUTA_ENTREGADA_REQUERIDA", 
                        new HashMap<String, String>(), con, in.IdEmpresa));
                out.P_OUT_ERRORESCONTADOR++;
                out.P_OUT_DESCRIPCION = "Fecha de valuta entregada es obligatoria para TipoOperacion 01/02.";
                return out;
            }
            try {
                LocalDate.parse(fvRec, DF_YMD);
            } catch (DateTimeParseException e) {
                out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_FECHA_VALUTA_RECIBIDA_FORMATO", 
                        new HashMap<String, String>(), con, in.IdEmpresa));
                out.P_OUT_ERRORESCONTADOR++;
                out.P_OUT_DESCRIPCION = "Fecha de valuta recibida invalida, el formato debe ser YYYY-MM-DD.";
                return out;
            }
            try {
                LocalDate.parse(fvEnt, DF_YMD);
            } catch (DateTimeParseException e) {
                out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_FECHA_VALUTA_ENTREGADA_FORMATO", 
                        new HashMap<String, String>(), con, in.IdEmpresa));
                out.P_OUT_ERRORESCONTADOR++;
                out.P_OUT_DESCRIPCION = "Fecha de valuta entregada invalida, el formato debe ser YYYY-MM-DD.";
                return out;
            }

            in.FechaValutaMonedaRecibida = fvRec;
            in.FechaValutaMonedaEntregada = fvEnt;
        }
        
        if (isNullOrBlank(in.Identificador) || in.Identificador.length() > 15) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_IDENTIFICADOR_INVALIDO",
                    new HashMap<String, String>(), con, in.IdEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "Identificador invalido.";
            logger.error("Identificador invalido: " + in.Identificador);
            return out;
        }

        if (isNullOrBlank(in.RutContraparte1) || !ValidacionRut(in.RutContraparte1)) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_RUT_CONTRAPARTE1_INVALIDO",
                    new HashMap<String, String>(), con, in.IdEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "Rut contraparte 1 invalido.";
            logger.error("Rut contraparte 1  invalido: " + in.RutContraparte1);
            return out;
        }

        if (in.LeiContraparte1 != null) {
            String lei1 = in.LeiContraparte1.trim();
            if (!lei1.isEmpty() && lei1.length() > 20) {
                out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,
                    "ERROR_LEI_CONTRAPARTE1_INVALIDO", new HashMap<>(), con, in.IdEmpresa));
                out.P_OUT_ERRORESCONTADOR++;
                out.P_OUT_DESCRIPCION = "LEI de contraparte 1 invalido (max. 20).";
                logger.error("LEI contraparte 1 largo invalido: {}", in.LeiContraparte1);
                return out;
            }
            in.LeiContraparte1 = lei1.isEmpty() ? null : lei1;
        }

        if (isNullOrBlank(in.NombreContraparte1) || in.NombreContraparte1.length() > 50) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_NOMBRE_CONTRAPARTE1_INVALIDO",
                    new HashMap<String, String>(), con, in.IdEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "Nombre de contraparte 1 invalido.";
            logger.error("Nombre de contraparte 1 invalido: " + in.NombreContraparte1);
            return out;
        }

        if (!isNullOrBlank(in.RutContraparte2)) {
            in.RutContraparte2 = in.RutContraparte2.trim();
            if (!ValidacionRut(in.RutContraparte2)) {
                out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,
                    "ERROR_RUT_CONTRAPARTE2_INVALIDO", new HashMap<>(), con, in.IdEmpresa));
                out.P_OUT_ERRORESCONTADOR++;
                out.P_OUT_DESCRIPCION = "Rut contraparte 2 invalido.";
                logger.error("Rut contraparte 2 invalido: {}", in.RutContraparte2);
                return out;
            }
        } else {
            in.RutContraparte2 = null;
        }

        if (in.LeiContraparte2 != null) {
            String lei2 = in.LeiContraparte2.trim();
            if (!lei2.isEmpty() && lei2.length() > 20) {
                out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,
                    "ERROR_LEI_CONTRAPARTE2_INVALIDO", new HashMap<>(), con, in.IdEmpresa));
                out.P_OUT_ERRORESCONTADOR++;
                out.P_OUT_DESCRIPCION = "LEI de contraparte 2 invalido (max. 20).";
                logger.error("LEI contraparte 2 largo invalido: {}", in.LeiContraparte2);
                return out;
            }
            in.LeiContraparte2 = lei2.isEmpty() ? null : lei2;
        }

        if (in.RutContraparte2 == null && in.LeiContraparte2 == null) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_CONTRAPARTE2_REQUIERE_RUT_O_LEI",
                    new HashMap<>(), con, in.IdEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "Debe informar al menos uno: RUT o LEI de contraparte 2.";
            logger.error("Debe informar al menos uno: RUT o LEI de contraparte 2.");
            return out;
        }

        if (isNullOrBlank(in.NombreContraparte2) || in.NombreContraparte2.length() > 50) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_NOMBRE_CONTRAPARTE2_INVALIDO",
                    new HashMap<String, String>(), con, in.IdEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "Nombre de contraparte 2 invalido.";
            logger.error("Nombre de contraparte 2 invalido: " + in.NombreContraparte2);
            return out;
        }
        
        if (!isIso3(in.PaisContraparte2)) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,
                "ERROR_PAIS_CONTRAPARTE2_INVALIDO", new HashMap<>(), con, in.IdEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "PaisContraparte2 invalido (ISO 3166-1 alpha-3).";
            logger.error("PaisContraparte2 invalido: {}", in.PaisContraparte2);
            return out;
        }
        in.PaisContraparte2 = norm(in.PaisContraparte2);

        if (in.RutIntermediario != null) {
            String rutInt = in.RutIntermediario.trim();
            if (rutInt.isEmpty()) {
                in.RutIntermediario = null;
            } else if (!ValidacionRut(rutInt)) {
                out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,
                    "ERROR_RUT_INTERMEDIARIO_INVALIDO", new HashMap<>(), con, in.IdEmpresa));
                out.P_OUT_ERRORESCONTADOR++;
                out.P_OUT_DESCRIPCION = "RutIntermediario, si viene, debe ser valido.";
                logger.error("RutIntermediario invalido: {}", rutInt);
                return out;
            } else {
                in.RutIntermediario = rutInt;
            }
        }

        boolean es01o02 = "01".equals(in.TipoOperacion) || "02".equals(in.TipoOperacion);
        if (es01o02) {
            if (isNullOrBlank(in.PlataformaNegociacion)) {
                out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,"ERROR_PLATAFORMA_NEGOCIACION_INVALIDA", new HashMap<>(), con, in.IdEmpresa));
                out.P_OUT_ERRORESCONTADOR++;
                out.P_OUT_DESCRIPCION = "Para TipoOperacion 01/02, PlataformaNegociacion obligatorio.";
                logger.error("PlataformaNegociacion invalido: {}", in.PlataformaNegociacion);
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
                        new HashMap<String, String>(), con, in.IdEmpresa));
                out.P_OUT_ERRORESCONTADOR++;
                out.P_OUT_DESCRIPCION = "Error al validar diccionarios de plataforma de negociacion.";
                logger.error("Error al validar diccionarios de plataforma de negociacion");
                return out;
            }
            
            if (diccionarios.stream().noneMatch(d -> d.Codigo.equals(in.PlataformaNegociacion))) {
                out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,"ERROR_PLATAFORMA_NEGOCIACION_NO_EXISTE", 
                        new HashMap<String, String>(), con, in.IdEmpresa));
                out.P_OUT_ERRORESCONTADOR++;
                out.P_OUT_DESCRIPCION = "Plataforma de negociacion no existe en diccionario.";
                logger.error("Plataforma de negociacion no existe en diccionario: " + in.PlataformaNegociacion);
                return out;
            }

            logger.info("Plataforma de negociacion validada: {}", in.PlataformaNegociacion);
        } else {
            if (!isNullOrBlank(in.PlataformaNegociacion)) {
                out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,"ERROR_PLATAFORMA_NEGOCIACION_LARGO", new HashMap<>(), con, in.IdEmpresa));
                out.P_OUT_ERRORESCONTADOR++;
                out.P_OUT_DESCRIPCION = "Para TipoOperacion 03/04, PlataformaNegociacion no debe venir.";
                logger.error("PlataformaNegociacion invalido, ya que no debe venir: {}", in.PlataformaNegociacion);
                return out;
            }
            in.PlataformaNegociacion = null;
        }

        if (isNullOrBlank(in.CodigoOperacionCambio) || !in.CodigoOperacionCambio.matches("\\d{1,6}")) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,"ERROR_CODIGO_OPERACION_CAMBIO_INVALIDO", new HashMap<>(), con, in.IdEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "CodigoOperacionCambio debe ser numerico de 1 a 6 digitos.";
            logger.error("CodigoOperacionCambio invalido: {}", in.CodigoOperacionCambio);
            return out;
        }

        HashMap<String, Object> codOpParams = new HashMap<>();
        List<diccionarioResult> codigoOperaciones = sql.query(
                con,
                SQL_SelectCodigoOperacion,
                codOpParams,
                diccionarioResult.class);
        
        if (codigoOperaciones.isEmpty()) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,"ERROR_VALIDACION_DICCIONARIOS", 
                    new HashMap<String, String>(), con, in.IdEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "Error al validar diccionarios de Codigo Operacion Cambio.";
            logger.error("Error al validar diccionarios de Codigo Operacion Cambio.");
            return out;
        }
        
        if (codigoOperaciones.stream().noneMatch(d -> d.Codigo.equals(in.CodigoOperacionCambio))) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,"ERROR_CODIGO_OPERACION_CAMBIO_NO_EXISTE", 
                    new HashMap<String, String>(), con, in.IdEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "Codigo Operacion Cambio no existe en diccionario.";
            logger.error("Codigo Operacion Cambio no existe en diccionario: " + in.CodigoOperacionCambio);
            return out;
        }

        logger.info("CodigoOperacionCambio valido: {}", in.CodigoOperacionCambio);


        if (!isIso3(in.PaisMonedaExtranjera)) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,"ERROR_PAIS_MONEDA_EXTRANJERA_INVALIDO", new HashMap<>(), con, in.IdEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "PaisMonedaExtranjera invalido (ISO 3166-1 alpha-3).";
            logger.error("PaisMonedaExtranjera invalido: {}", in.PaisMonedaExtranjera);
            return out;
        }
        in.PaisMonedaExtranjera = norm(in.PaisMonedaExtranjera);

        boolean es04 = "04".equals(in.TipoOperacion);
        if (!es04) {
             if (isNullOrBlank(in.MonedaCompradaRecibida) || !isIso4217(in.MonedaCompradaRecibida)) {
                out.P_OUT_MENSAJES.add(new MensajeSalida(
                    0L, MensajeSalida.MENSAJE_ERROR,
                    "ERROR_MONEDA_COMPRADA_RECIBIDA_INVALIDA",
                    new HashMap<>(), con, in.IdEmpresa
                ));
                out.P_OUT_ERRORESCONTADOR++;
                out.P_OUT_DESCRIPCION = "MonedaCompradaRecibida obligatoria y debe ser codigo ISO 4217 (3 letras).";
                logger.error("MonedaCompradaRecibida invalida: {}", in.MonedaCompradaRecibida);
                return out;
            }
            in.MonedaCompradaRecibida = norm(in.MonedaCompradaRecibida);
            if (in.MontoMonedaCompradaRecibida == null || !inRangeDecimal26_8(in.MontoMonedaCompradaRecibida)) {
                out.P_OUT_MENSAJES.add(new MensajeSalida(
                    0L, MensajeSalida.MENSAJE_ERROR, "ERROR_MONTO_COMPRADA_RECIBIDA_INVALIDO",
                    new HashMap<>(), con, in.IdEmpresa
                ));
                out.P_OUT_ERRORESCONTADOR++;
                out.P_OUT_DESCRIPCION = "MontoMonedaCompradaRecibida obligatorio y en [0..999999].";
                logger.error("MontoMonedaCompradaRecibida invalido: {}", in.MontoMonedaCompradaRecibida);
                return out;
            }
        } else {
            if (!isNullOrBlank(in.MonedaCompradaRecibida)) {
                out.P_OUT_MENSAJES.add(new MensajeSalida(
                    0L, MensajeSalida.MENSAJE_ERROR,
                    "ERROR_MONEDA_COMPRADA_RECIBIDA_NO_PERMITIDA",
                    new HashMap<>(), con, in.IdEmpresa
                ));
                out.P_OUT_ERRORESCONTADOR++;
                out.P_OUT_DESCRIPCION = "Para TipoOperacion 04, MonedaCompradaRecibida no debe venir (dejar vacio).";
                logger.error("MonedaCompradaRecibida informada indebidamente para TipoOperacion 04: {}",
                            in.MonedaCompradaRecibida);
                return out;
            }
            if (in.MontoMonedaCompradaRecibida != null) {
                out.P_OUT_MENSAJES.add(new MensajeSalida(
                    0L, MensajeSalida.MENSAJE_ERROR, "ERROR_MONTO_COMPRADA_RECIBIDA_NO_PERMITIDO",
                    new HashMap<>(), con, in.IdEmpresa
                ));
                out.P_OUT_ERRORESCONTADOR++;
                out.P_OUT_DESCRIPCION = "Para TipoOperacion 04, MontoMonedaCompradaRecibida no debe venir (dejar vacio).";
                logger.error("MontoMonedaCompradaRecibida informado indebidamente para TipoOperacion 04: {}",
                            in.MontoMonedaCompradaRecibida);
                return out;
            }
            in.MonedaCompradaRecibida = null;
            in.MontoMonedaCompradaRecibida = null;
        }

        boolean es03 = "03".equals(in.TipoOperacion);
        if (!es03) {
            if (isNullOrBlank(in.MonedaVendidaEntregada) || !isIso4217(in.MonedaVendidaEntregada)) {
                out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,
                    "ERROR_MONEDA_VENDIDA_ENTREGADA_INVALIDA", new HashMap<>(), con, in.IdEmpresa));
                out.P_OUT_ERRORESCONTADOR++;
                out.P_OUT_DESCRIPCION = "MonedaVendidaEntregada obligatoria y debe ser ISO-4217.";
                logger.error("MonedaVendidaEntregada invalida: {}", in.MonedaVendidaEntregada);
                return out;
            }
            in.MonedaVendidaEntregada = norm(in.MonedaVendidaEntregada);
            if (in.MontoMonedaVendidaEntregada == null || !inRangeDecimal26_8(in.MontoMonedaVendidaEntregada)) {
                out.P_OUT_MENSAJES.add(new MensajeSalida(
                    0L, MensajeSalida.MENSAJE_ERROR,
                    "ERROR_MONTO_VENDIDA_ENTREGADA_INVALIDO",
                    new HashMap<>(), con, in.IdEmpresa
                ));
                out.P_OUT_ERRORESCONTADOR++;
                out.P_OUT_DESCRIPCION = "MontoMonedaVendidaEntregada obligatorio y en [0..999999].";
                logger.error("MontoMonedaVendidaEntregada invalido: {}", in.MontoMonedaVendidaEntregada);
                return out;
            }
        } else {
            if (!isNullOrBlank(in.MonedaVendidaEntregada)) {
                out.P_OUT_MENSAJES.add(new MensajeSalida(
                    0L, MensajeSalida.MENSAJE_ERROR,
                    "ERROR_MONEDA_VENDIDA_ENTREGADA_NO_PERMITIDA",
                    new HashMap<>(), con, in.IdEmpresa
                ));
                out.P_OUT_ERRORESCONTADOR++;
                out.P_OUT_DESCRIPCION = "Para TipoOperacion 03, MonedaVendidaEntregada no debe venir (dejar vacio).";
                logger.error("MonedaVendidaEntregada invalido para TipoOperacion 03: {}",in.MonedaVendidaEntregada);
                return out;
            }
            if (in.MontoMonedaVendidaEntregada != null) {
                out.P_OUT_MENSAJES.add(new MensajeSalida(
                    0L, MensajeSalida.MENSAJE_ERROR,
                    "ERROR_MONTO_VENDIDA_ENTREGADA_NO_PERMITIDO",
                    new HashMap<>(), con, in.IdEmpresa
                ));
                out.P_OUT_ERRORESCONTADOR++;
                out.P_OUT_DESCRIPCION = "Para TipoOperacion 03, MontoMonedaVendidaEntregada no debe venir (dejar vacio).";
                logger.error("MontoMonedaVendidaEntregada invalido para TipoOperacion 03: {}",in.MontoMonedaVendidaEntregada);
                return out;
            }
            in.MonedaVendidaEntregada = null;
            in.MontoMonedaVendidaEntregada = null;
        }

        if (!es03o04) {
            if (isNullOrBlank(in.ConvencionTipoCambio) || !isFxConv4217(in.ConvencionTipoCambio)) {
                out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_CONVENCION_TIPO_CAMBIO_INVALIDA",
                        new HashMap<>(), con, in.IdEmpresa));
                out.P_OUT_ERRORESCONTADOR++;
                out.P_OUT_DESCRIPCION = "ConvencionTipoCambio obligatoria, formato XXX/YYY y ambas ISO-4217.";
                logger.error("ConvencionTipoCambio invalido: {}", in.ConvencionTipoCambio);
                return out;
            }
            in.ConvencionTipoCambio = norm(in.ConvencionTipoCambio);

            if (in.TipoCambio == null || !inRangeDecimal26_8(in.TipoCambio) || in.TipoCambio.doubleValue() <= 0d) {
                out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_TIPO_CAMBIO_INVALIDO",
                        new HashMap<>(), con, in.IdEmpresa));
                out.P_OUT_ERRORESCONTADOR++;
                out.P_OUT_DESCRIPCION = "TipoCambio obligatorio (> 0) y en [0..999999].";
                logger.error("TipoCambio invalido: {}", in.TipoCambio);
                return out;
            }
        } else {
            if (!isNullOrBlank(in.ConvencionTipoCambio)) {
                out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_CONVENCION_TIPO_CAMBIO_NO_PERMITIDA",
                     new HashMap<>(), con, in.IdEmpresa));
                out.P_OUT_ERRORESCONTADOR++;
                out.P_OUT_DESCRIPCION = "Para TipoOperacion 03/04, ConvencionTipoCambio no debe venir (dejar vacio).";
                logger.error("ConvencionTipoCambio informada indebidamente para TipoOperacion 03/04: {}",
                            in.ConvencionTipoCambio);
                return out;
            }
            if (in.TipoCambio != null) {
                out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_TIPO_CAMBIO_NO_PERMITIDO",
                        new HashMap<>(), con, in.IdEmpresa));
                out.P_OUT_ERRORESCONTADOR++;
                out.P_OUT_DESCRIPCION = "Para TipoOperacion 03/04, TipoCambio no debe venir (dejar vacio).";
                logger.error("TipoCambio informado indebidamente para TipoOperacion 03/04: {}", in.TipoCambio);
                return out;
            }
            in.ConvencionTipoCambio = null;
            in.TipoCambio = null;
        }

        if (isNullOrBlank(in.AfectaDerivados) || !("S".equalsIgnoreCase(in.AfectaDerivados) || "N".equalsIgnoreCase(in.AfectaDerivados))) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,"ERROR_AFECTA_DERIVADOS_INVALIDO", 
                    new HashMap<>(), con, in.IdEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "AfectaDerivados obligatorio: 'S' o 'N'.";
            logger.error("AfectaDerivados invalido: " + in.AfectaDerivados);
            return out;
        }

        if ("S".equals(in.AfectaDerivados)) {
            if (isNullOrBlank(in.IdentificadorContratoDerivados) || in.IdentificadorContratoDerivados.length() > 52) {
                out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_IDENTIFICADOR_CONTRATO_DERIVADOS_INVALIDO",
                        new HashMap<>(), con, in.IdEmpresa));
                out.P_OUT_ERRORESCONTADOR++;
                out.P_OUT_DESCRIPCION = "IdentificadorContratoDerivados es obligatorio (Afecta=S) y largo <= 52.";
                logger.error("IdentificadorContratoDerivados invalido: " + in.IdentificadorContratoDerivados);
                return out;
            }
        } else {
            if (!isNullOrBlank(in.IdentificadorContratoDerivados)) {
                out.P_OUT_MENSAJES.add(new MensajeSalida(
                    0L, MensajeSalida.MENSAJE_ERROR, "ERROR_IDENTIFICADOR_CONTRATO_DERIVADOS_NO_PERMITIDO",
                    new HashMap<>(), con, in.IdEmpresa));
                out.P_OUT_ERRORESCONTADOR++;
                out.P_OUT_DESCRIPCION = "Para AfectaDerivados = 'N', IdentificadorContratoDerivados no debe venir (dejar vacio).";
                logger.error("IdentificadorContratoDerivados informado con Afecta='N': {}", in.IdentificadorContratoDerivados);
                return out;
            }
            in.IdentificadorContratoDerivados = null;
        }

        if ("S".equals(in.AfectaDerivados)) {
            if (isNullOrBlank(in.FechaSuscripcionContrato)) {
                out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_FECHA_SUSCRIPCION_CONTRATO_INVALIDA",
                    new HashMap<>(), con, in.IdEmpresa
                ));
                out.P_OUT_ERRORESCONTADOR++;
                out.P_OUT_DESCRIPCION = "FechaSuscripcionContrato es obligatoria (Afecta=S) y debe ser YYYY-MM-DD.";
                logger.error("FechaSuscripcionContrato vacia (Afecta=S)");
                return out;
            }
            try {
                LocalDate.parse(in.FechaSuscripcionContrato, DF_YMD);
            } catch (Exception e) {
                out.P_OUT_MENSAJES.add(new MensajeSalida(
                    0L, MensajeSalida.MENSAJE_ERROR, "ERROR_FECHA_SUSCRIPCION_CONTRATO_INVALIDA",
                    new HashMap<>(), con, in.IdEmpresa
                ));
                out.P_OUT_ERRORESCONTADOR++;
                out.P_OUT_DESCRIPCION = "FechaSuscripcionContrato debe ser YYYY-MM-DD.";
                logger.error("FechaSuscripcionContrato invalida: {}", in.FechaSuscripcionContrato);
                return out;
            }

            if (isNullOrBlank(in.HoraSuscripcionContrato)) {
                out.P_OUT_MENSAJES.add(new MensajeSalida(
                    0L, MensajeSalida.MENSAJE_ERROR, "ERROR_HORA_SUSCRIPCION_CONTRATO_INVALIDA",
                    new HashMap<>(), con, in.IdEmpresa
                ));
                out.P_OUT_ERRORESCONTADOR++;
                out.P_OUT_DESCRIPCION = "HoraSuscripcionContrato es obligatoria (Afecta=S) y debe ser HH:MM:SS.";
                logger.error("HoraSuscripcionContrato vacia (Afecta=S)");
                return out;
            }
            try {
                LocalTime.parse(in.HoraSuscripcionContrato, DateTimeFormatter.ofPattern("HH:mm:ss"));
            } catch (Exception e) {
                out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_HORA_SUSCRIPCION_CONTRATO_INVALIDA",
                        new HashMap<>(), con, in.IdEmpresa));
                out.P_OUT_ERRORESCONTADOR++;
                out.P_OUT_DESCRIPCION = "HoraSuscripcionContrato debe ser HH:MM:SS.";
                logger.error("HoraSuscripcionContrato invalida: {}", in.HoraSuscripcionContrato);
                return out;
            }
        } else {
            if (!isNullOrBlank(in.FechaSuscripcionContrato)) {
                out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_FECHA_SUSCRIPCION_CONTRATO_NO_PERMITIDA",
                        new HashMap<>(), con, in.IdEmpresa));
                out.P_OUT_ERRORESCONTADOR++;
                out.P_OUT_DESCRIPCION = "Para AfectaDerivados = 'N', FechaSuscripcionContrato no debe venir (dejar vacio).";
                logger.error("FechaSuscripcionContrato informada con Afecta='N': {}", in.FechaSuscripcionContrato);
                return out;
            }
            if (!isNullOrBlank(in.HoraSuscripcionContrato)) {
                out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_HORA_SUSCRIPCION_CONTRATO_NO_PERMITIDA",
                        new HashMap<>(), con, in.IdEmpresa));
                out.P_OUT_ERRORESCONTADOR++;
                out.P_OUT_DESCRIPCION = "Para AfectaDerivados = 'N', HoraSuscripcionContrato no debe venir (dejar vacio).";
                logger.error("HoraSuscripcionContrato informada con Afecta='N': {}", in.HoraSuscripcionContrato);
                return out;
            }
            in.FechaSuscripcionContrato = "0001-01-01";
        }
        
        if (isNullOrBlank(in.OperacionEmpresaRelacionada) ||
        !(in.OperacionEmpresaRelacionada.equals("S") || in.OperacionEmpresaRelacionada.equals("N"))) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,
            "ERROR_OPERACION_EMPRESA_RELACIONADA_INVALIDA", new HashMap<>(), con, in.IdEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "OperacionEmpresaRelacionada debe ser 'S' o 'N'.";
            logger.error("OperacionEmpresaRelacionada invalida: " + in.OperacionEmpresaRelacionada);
            return out;
        }

        String cod = in.CodigoOperacionCambio != null ? in.CodigoOperacionCambio.trim() : "";
        boolean starts2004o2005 = cod.startsWith("2004") || cod.startsWith("2005");
        boolean starts2 = cod.startsWith("2");
        boolean starts1 = cod.startsWith("1");

        if (starts2004o2005) {
            if (!isNullOrBlank(in.NumeroInscripcionObligacionExterior)
                && in.NumeroInscripcionObligacionExterior.length() > 50) {
                out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,"NUMERO_INSCRIPCION_OBLIGACION_EXTERIOR_INVALIDO",
                        new HashMap<>(), con, in.IdEmpresa));
                out.P_OUT_ERRORESCONTADOR++;
                out.P_OUT_DESCRIPCION = "NumeroInscripcionObligacionExterior <= 50.";
                logger.error("NumeroInscripcionObligacionExterior invalido: {}", in.NumeroInscripcionObligacionExterior);
                return out;
            }
        }  else {
            if (!isNullOrBlank(in.NumeroInscripcionObligacionExterior)) {
                out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "NUMERO_INSCRIPCION_OBLIGACION_EXTERIOR_INVALIDO", 
                        new HashMap<>(), con, in.IdEmpresa));
                out.P_OUT_ERRORESCONTADOR++;
                out.P_OUT_DESCRIPCION = "NumeroInscripcionObligacionExterior debe ir vacio si CodigoOperacionCambio no comienza con 2004/2005.";
                logger.error("NumeroInscripcionObligacionExterior debe ir vacio si CodigoOperacionCambio no comienza con 2004/2005.");
                return out;
            }
            in.NumeroInscripcionObligacionExterior = null;
        }

        if (starts2) {
            if (!isNullOrBlank(in.NombreFinancistaAporante) 
                    && in.NombreFinancistaAporante.length() > 50) {
                out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,"NOMBRE_FINANCISTA_APORTANTE_INVALIDO",
                        new HashMap<>(), con, in.IdEmpresa));
                out.P_OUT_ERRORESCONTADOR++;
                out.P_OUT_DESCRIPCION = "NombreFinancistaAportante <= 50.";
                logger.error("NombreFinancistaAportante invalido: {}", in.NombreFinancistaAporante);
                return out;
            }
            if (isNullOrBlank(in.NombreFinancistaAporante)) in.NombreFinancistaAporante = null;
        } else {
            if (!isNullOrBlank(in.NombreFinancistaAporante)) {
                out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "NOMBRE_FINANCISTA_APORTANTE_INVALIDO",
                        new HashMap<>(), con, in.IdEmpresa));
                out.P_OUT_ERRORESCONTADOR++;
                out.P_OUT_DESCRIPCION = "NombreFinancistaAportante debe ir vacio si CodigoOperacionCambio no inicia con '2'.";
                logger.error("NombreFinancistaAportante debe ir vacio si CodigoOperacionCambio no inicia con '2'.");
                return out;
            }
            in.NombreFinancistaAporante = null;
        }

        if (starts2) {
            if (!isNullOrBlank(in.LeiFinancistaAporante)
                    && in.LeiFinancistaAporante.length() > 20) {
                out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "LEI_FINANCISTA_APORANTE_INVALIDO",
                        new HashMap<>(), con, in.IdEmpresa));
                out.P_OUT_ERRORESCONTADOR++;
                out.P_OUT_DESCRIPCION = "LeiFinancistaAporante <= 20.";
                logger.error("LeiFinancistaAporante invalido: {}", in.LeiFinancistaAporante);
                return out;
            }
            if (isNullOrBlank(in.LeiFinancistaAporante)) in.LeiFinancistaAporante = null;
        } else {
            if (!isNullOrBlank(in.LeiFinancistaAporante)) {
                out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,"LEI_FINANCISTA_APORTANTE_INVALIDO", 
                        new HashMap<>(), con, in.IdEmpresa));
                out.P_OUT_ERRORESCONTADOR++;
                out.P_OUT_DESCRIPCION = "LeiFinancistaAportante debe ir vacio si CodigoOperacionCambio no inicia con '2'.";
                logger.error("LeiFinancistaAportante debe ir vacio si CodigoOperacionCambio no inicia con '2'.");
                return out;
            }
            in.LeiFinancistaAporante = null;
        }

        if (starts1) {
            if (!isNullOrBlank(in.NombreReceptorInversion)
                    && in.NombreReceptorInversion.length() > 50) {
                out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "NOMBRE_RECEPTOR_INVERSION_INVALIDO",
                        new HashMap<>(), con, in.IdEmpresa));
                out.P_OUT_ERRORESCONTADOR++;
                out.P_OUT_DESCRIPCION = "NombreReceptorInversion <= 50.";
                logger.error("NombreReceptorInversion invalido: {}", in.NombreReceptorInversion);
                return out;
            }
            if (isNullOrBlank(in.NombreReceptorInversion)) in.NombreReceptorInversion = null;
        } else {
            if (!isNullOrBlank(in.NombreReceptorInversion)) {
                out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "NOMBRE_RECEPTOR_INVERSION_INVALIDO",
                        new HashMap<>(), con, in.IdEmpresa));
                out.P_OUT_ERRORESCONTADOR++;
                out.P_OUT_DESCRIPCION = "NombreReceptorInversion debe ir vacio si CodigoOperacionCambio no inicia con '1'.";
                logger.error("NombreReceptorInversion debe ir vacio si CodigoOperacionCambio no inicia con '1'.");
                return out;
            }
            in.NombreReceptorInversion = null;
        }

        if (starts1) {
            if (!isNullOrBlank(in.LeiReceptorInversion) && in.LeiReceptorInversion.length() > 20) {
                out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,"LEI_RECEPTOR_INVERSION_INVALIDO",
                        new HashMap<>(), con, in.IdEmpresa));
                out.P_OUT_ERRORESCONTADOR++;
                out.P_OUT_DESCRIPCION = "LeiReceptorInversion <= 20.";
                logger.error("LeiReceptorInversion invalido: {}", in.LeiReceptorInversion);
                return out;
            }
            if (isNullOrBlank(in.LeiReceptorInversion)) in.LeiReceptorInversion = null;
        } else {
            if (!isNullOrBlank(in.LeiReceptorInversion)) {
                out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "LEI_RECEPTOR_INVERSION_INVALIDO", 
                        new HashMap<>(), con, in.IdEmpresa));
                out.P_OUT_ERRORESCONTADOR++;
                out.P_OUT_DESCRIPCION = "LeiReceptorInversion debe ir vacio si CodigoOperacionCambio no inicia con '1'.";
                logger.error("LeiReceptorInversion debe ir vacio si CodigoOperacionCambio no inicia con '1'.");
                return out;
            }
            in.LeiReceptorInversion = null;
        }

        if (!isNullOrBlank(in.ModalidadInversionCustodio)) {
            if (!( "01".equals(in.ModalidadInversionCustodio) || "02".equals(in.ModalidadInversionCustodio) )) {
                out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,"MODALIDAD_INVERSION_CUSTODIO_INVALIDA",
                        new HashMap<>(), con, in.IdEmpresa));
                out.P_OUT_ERRORESCONTADOR++;
                out.P_OUT_DESCRIPCION = "ModalidadInversionCustodio debe ser '01' o '02' si viene.";
                logger.error("ModalidadInversionCustodio invalido: " + in.ModalidadInversionCustodio);
                return out;
            }
        }

        HashMap<String, Object> FolioParams = new HashMap<>();
        FolioParams.put("rutContraparte1", in.RutContraparte1);
        FolioParams.put("fechaNegociacion", in.FechaNegociacion);
        FolioParams.put("horaNegociacion", in.HoraNegociacion);
        FolioParams.put("identificador", in.Identificador);

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
            logger.error("El reporte ya existe para el folio: RutContraparte1={}, FechaNegociacion={}, HoraNegociacion={}, Identificador={}",
                    in.RutContraparte1, in.FechaNegociacion, in.HoraNegociacion, in.Identificador);
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
        EntradaReporte2p1 parametros = new EntradaReporte2p1();
        parametros.Id = nextId;
        parametros.IdDocumento = idDocumento;
        parametros.FuenteDatos = "WEBSERVICE";
        parametros.Estado = "ACT";
        parametros.EstadoSICAM = "";
        parametros.SICAM = "";
        parametros.TipoRegistro = in.TipoRegistro;
        parametros.EventoReporte = in.EventoReporte;
        parametros.TipoOperacion = in.TipoOperacion;
        parametros.FechaNegociacion = in.FechaNegociacion;
        parametros.HoraNegociacion = in.HoraNegociacion;
        parametros.FechaValutaMonedaRecibida = in.FechaValutaMonedaRecibida;
        parametros.FechaValutaMonedaEntregada = in.FechaValutaMonedaEntregada;
        parametros.Identificador = in.Identificador;
        parametros.RutContraparte1 = in.RutContraparte1;
        parametros.LeiContraparte1 = in.LeiContraparte1;
        parametros.NombreContraparte1 = in.NombreContraparte1;
        parametros.RutContraparte2 = in.RutContraparte2;    
        parametros.LeiContraparte2 = in.LeiContraparte2;
        parametros.NombreContraparte2 = in.NombreContraparte2;
        parametros.PaisContraparte2 = upperOrNull(in.PaisContraparte2);
        parametros.RutIntermediario = in.RutIntermediario;
        parametros.PlataformaNegociacion = in.PlataformaNegociacion;
        parametros.CodigoOperacionCambio = in.CodigoOperacionCambio;
        parametros.PaisMonedaExtranjera = in.PaisMonedaExtranjera;
        parametros.MonedaCompradaRecibida = upperOrNull(in.MonedaCompradaRecibida);
        parametros.MontoMonedaCompradaRecibida = in.MontoMonedaCompradaRecibida;
        parametros.MonedaVendidaEntregada = upperOrNull(in.MonedaVendidaEntregada);
        parametros.MontoMonedaVendidaEntregada = in.MontoMonedaVendidaEntregada;
        parametros.ConvencionTipoCambio = in.ConvencionTipoCambio;
        parametros.TipoCambio = in.TipoCambio;
        parametros.AfectaDerivados = in.AfectaDerivados;
        parametros.IdentificadorContratoDerivados = in.IdentificadorContratoDerivados;
        parametros.FechaSuscripcionContrato = in.FechaSuscripcionContrato;
        parametros.HoraSuscripcionContrato = in.HoraSuscripcionContrato;
        parametros.OperacionEmpresaRelacionada = in.OperacionEmpresaRelacionada;
        parametros.NumeroInscripcionObligacionExterior = in.NumeroInscripcionObligacionExterior;
        parametros.NombreFinancistaAporante = in.NombreFinancistaAporante;
        parametros.LeiFinancistaAporante = in.LeiFinancistaAporante;
        parametros.NombreReceptorInversion = in.NombreReceptorInversion;
        parametros.LeiReceptorInversion = in.LeiReceptorInversion;
        parametros.ModalidadInversionCustodio = in.ModalidadInversionCustodio;
        parametros.FolioOperacion = Long.valueOf(0);
        parametros.IdOperacion = Long.valueOf(0);
        parametros.IdCartera = Long.valueOf(0);
        parametros.FechaLiquidacion = "";
        parametros.IdCliente = Long.valueOf(0);
        parametros.IdTipoOperacion = "";
        parametros.Nemo = "";
        parametros.IdMonedaPrincipal = "";
        parametros.IdMonedaSecundaria = "";

        List<EntradaReporte2p1> batchParams = Collections.singletonList(parametros);
        int[] resultados = sql.batch(
                con,
                SQL_InsertReporte,
                batchParams,
                EntradaReporte2p1.class);

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

    public boolean ValidacionRut(String rut) {
        if (rut == null || rut.isEmpty()) {
            return false;
        }
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
        EntradaReporte2p1 entrada = gson.fromJson(in, EntradaReporte2p1.class);

        SalidaReporte2p1 out = ingresarReporte(conn, entrada);

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
                        "    \"EventoReporte\": \"1\",\n" +
                        "    \"TipoOperacion\": \"03\",\n" +
                        "    \"FechaNegociacion\": \"2025-08-13\",\n" +
                        "    \"HoraNegociacion\": \"10:00:00\",\n" +
                        "    \"FechaValutaMonedaRecibida\": \"\",\n" +
                        "    \"FechaValutaMonedaEntregada\": \"\",\n" +
                        "    \"Identificador\": \"123467\",\n" +
                        "    \"RutContraparte1\": \"76011193-7\",\n" +
                        "    \"LeiContraparte1\": \"LEI123456789\",\n" +
                        "    \"NombreContraparte1\": \"Contraparte 1\",\n" +
                        "    \"RutContraparte2\": \"76011194-8\",\n" +
                        "    \"LeiContraparte2\": \"LEI987654321\",\n" +
                        "    \"NombreContraparte2\": \"Contraparte 2\",\n" +
                        "    \"PaisContraparte2\": \"CHL\",\n" +
                        "    \"RutIntermediario\": \"76011195-9\",\n" +
                        "    \"PlataformaNegociacion\": \"\",\n" +
                        "    \"CodigoOperacionCambio\": \"500501\",\n" +
                        "    \"PaisMonedaExtranjera\": \"CHL\",\n" +
                        "    \"MonedaCompradaRecibida\": \"USD\",\n" +
                        "    \"MontoMonedaCompradaRecibida\": 145345345000.00,\n" +
                        "    \"MonedaVendidaEntregada\": \"\",\n" +
                        "    \"MontoMonedaVendidaEntregada\": NULL,\n" +
                        "    \"ConvencionTipoCambio\": \"\",\n" +
                        "    \"TipoCambio\": null,\n" +
                        "    \"AfectaDerivados\": \"N\",\n" +
                        "    \"IdentificadorContratoDerivados\": \"\",\n" +
                        "    \"FechaSuscripcionContrato\": \"\",\n" +
                        "    \"HoraSuscripcionContrato\": \"\",\n" +
                        "    \"OperacionEmpresaRelacionada\": \"N\",\n" +
                        "    \"NumeroInscripcionObligacionExterior\": \"\",\n" +
                        "    \"NombreFinancistaAporante\": \"\",\n" +
                        "    \"LeiFinancistaAporante\": \"\",\n" +
                        "    \"NombreReceptorInversion\": \"\",\n" +
                        "    \"LeiReceptorInversion\": \"\",\n" +
                        "    \"ModalidadInversionCustodio\": \"01\"\n" +
                        "}";
                EntradaReporte2p1 Objin = gson.fromJson(json, EntradaReporte2p1.class);

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