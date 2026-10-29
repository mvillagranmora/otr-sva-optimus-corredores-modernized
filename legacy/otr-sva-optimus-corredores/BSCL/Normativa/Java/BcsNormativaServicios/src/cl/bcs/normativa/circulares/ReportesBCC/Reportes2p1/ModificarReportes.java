package cl.bcs.normativa.circulares.ReportesBCC.Reportes2p1;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Arrays;
import java.util.function.Function;
import java.util.List;
import java.util.Set;
import java.util.Locale;
import java.util.HashSet;
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
 * Servicio para modificar reportes SICAM / Merrill Lynch
 * 
 */
public class ModificarReportes implements JavaCall {

    static final Logger logger = LogManager.getLogger(ModificarReportes.class);

    /**
     * Llamadas SQL
     */
    private final String SQL_MODIFICAR_REPORTE = loadResourceToString(
            "cl/bcs/normativa/circulares/ReportesBCC/Reportes2p1/sql/ModificarReporte_Reporte_call.sql");
    private final String SQL_CONSULTA_REPORTE = loadResourceToString(
            "cl/bcs/normativa/circulares/ReportesBCC/Reportes2p1/sql/ConsultarReportes_EstadoReporte_select.sql");
    private final String SQL_SelectCodigoOperacion = loadResourceToString(
            "cl/bcs/normativa/circulares/ReportesBCC/Reportes2p1/sql/ConsultarReportes_CodigoOperacion_select.sql");

    public static class EntradaReporte2p1 extends BcsPojo {
        public Long IdEmpresa;
        public String IdUsuario;
        public String ClaveUsuario;
        public String ClaveReporte;
        public String RutContraparte1;
        public String FechaNegociacion;
        public String HoraNegociacion;
        public String Identificador;
        // Datos modificables
        public String RutContraparte2;
        public String LeiContraparte2;
        public String NombreContraparte2;
        public String PaisContraparte2;
        public String RutIntermediario;
        public String CodigoOperacionCambio;
        public String PaisMonedaExtranjera;
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

        public EntradaReporte2p1() {
            this.IdEmpresa = 1L;
            this.IdUsuario = "";
            this.ClaveUsuario = "";
            this.ClaveReporte = "";
        }
    }

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

    public static class QueryResult extends BcsPojo {
        public Long ID;
        public Long IdDocumento;
        public String FuenteDatos;
        public String Estado;
        public String EstadoSICAM;
        public String SICAM;
        public String TipoRegistro; 
        public String EventoReporte;
        public String TipoOperacion; 
        public Date FechaNegociacion; 
        public String HoraNegociacion; 
        public Date FechaValutaMonedaRecibida;
        public Date FechaValutaMonedaEntregada;
        public String Identificador;
        public String RUTContraparte1;
        public String LEIContraparte1;
        public String NombreContraparte1;
        public String RUTContraparte2;
        public String LEIContraparte2;
        public String NombreContraparte2;
        public String PaisContraparte2;
        public String RUTIntermediario;
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
        public Date FechaSuscripcionContrato;
        public String HoraSuscripcionContrato;
        public String OperacionEmpresaRelacionada;
        public String NumeroInscripcionObligacionExterior;
        public String NombreFinancistaAporante;
        public String LEIFinancistaAporante;
        public String NombreReceptorInversion;
        public String LEIReceptorInversion;
        public String ModalidadInversionCustodio;
        public Long FolioOperacion;
        public Long IdOperacion;
        public Long IdCartera;
        public Date FechaLiquidacion;
        public Long IdCliente;
        public String IdTipoOperacion;
        public String Nemo;
        public String IdMonedaPrincipal;
        public String IdMonedaSecundaria;
    }

    public static class DatosSalidaSP extends BcsPojo {
        public Long p_out_id;
        public Integer p_out_AdvertenciasContador;
        public List<Detalle> p_out_AdvertenciasDetalle;
        public Integer p_out_ErroresContador;
        public List<Detalle> p_out_ErroresDetalle;
    }

    public static class Detalle extends BcsPojo {
        public String Codigo;
        public String RequiereAutorizacion;
        public String Descripcion;

        public Detalle() {
            this.Codigo = "";
            this.RequiereAutorizacion = "";
            this.Descripcion = "";
        }
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

    private static String normIso(String s) {
        return (s == null) ? null : s.trim().toUpperCase(Locale.ROOT);
    }

    private static boolean isIso3(String s) {
        String v = normIso(s);
        return v != null && v.length() == 3 && ISO3.contains(v);
    }

    public static class diccionarioResult extends BcsPojo{
        public String Codigo;
    }

    @Trace
    public SalidaReporte2p1 modificarReporte(Connection con, EntradaReporte2p1 in) throws Exception {
        logger.info("Modificando reporte 2.1");
        logger.info("Parametros de entrada: " + in.toString());
        SalidaReporte2p1 out = new SalidaReporte2p1();
        SQL sql = new SQL();

        //Validacion Folio
        if (in.RutContraparte1 == null || !ValidacionRut(in.RutContraparte1)) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_RUT_CONTRAPARTE1_INVALIDO",
                    new HashMap<String, String>(), con, in.IdEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "Rut contraparte 1 invalido.";
            logger.error("Rut contraparte 1 invalido: " + in.RutContraparte1);
            return out;
        }

       try {
            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            if (in.FechaNegociacion == null) throw new NullPointerException("null");
            LocalDate.parse(in.FechaNegociacion, df);
        } catch (DateTimeParseException | NullPointerException e) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_FECHA_NEGOCIACION_FORMATO",
                    new HashMap<String, String>(), con, in.IdEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "Fecha de negociacion invalida, el formato debe ser YYYY-MM-DD.";
            return out;
        }

        try {
            DateTimeFormatter tf = DateTimeFormatter.ofPattern("HH:mm:ss");
            if (in.HoraNegociacion == null) throw new NullPointerException("null");
            LocalTime.parse(in.HoraNegociacion, tf);
        } catch (DateTimeParseException | NullPointerException e) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_HORA_NEGOCIACION_FORMATO",
                    new HashMap<String, String>(), con, in.IdEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "Hora de negociacion invalida, el formato debe ser HH:mm:ss.";
            return out;
        }

        if (in.Identificador == null || in.Identificador.isEmpty()|| in.Identificador.length() > 15) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_IDENTIFICADOR_VACIO",
                    new HashMap<String, String>(), con, in.IdEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "El Identificador del reporte no puede estar vacio.";
            logger.error("El Identificador del reporte no puede estar vacio");
            return out;
        }

        HashMap<String, Object> ExistParams = new HashMap<>();
        ExistParams.put("rutContraparte1", in.RutContraparte1);
        ExistParams.put("fechaNegociacion", in.FechaNegociacion);
        ExistParams.put("horaNegociacion", in.HoraNegociacion);
        ExistParams.put("identificador", in.Identificador);

        List<QueryResult> rows = sql.query(
                con,
                SQL_CONSULTA_REPORTE,
                ExistParams,
                QueryResult.class);

        if (rows.isEmpty() || rows.get(0).ID == null) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_REPORTE_NO_EXISTE",
                    new HashMap<String, String>(), con, in.IdEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "El reporte con el folio especificado no existe.";
            logger.error("El reporte con el folio especificado no existe.");
            return out;
        }

        if ("ENV".equals(rows.get(0).EstadoSICAM)) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_REPORTE_ENVIADO",
                    new HashMap<String, String>(), con, in.IdEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "El reporte ya ha sido enviado y no puede ser modificado.";
            logger.error("El reporte ya ha sido enviado y no puede ser modificado: " + in.Identificador);
            return out;
        }

        if (in.RutContraparte2 != null) {
            String rut2 = in.RutContraparte2.trim();
            if (rut2.isEmpty()) {
                in.RutContraparte2 = null;
            } else if (!ValidacionRut(rut2)) {
                out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,"ERROR_RUT_CONTRAPARTE2_INVALIDO",
                        new HashMap<>(), con, in.IdEmpresa));
                out.P_OUT_ERRORESCONTADOR++;
                out.P_OUT_DESCRIPCION = "Rut contraparte 2 invalido.";
                logger.error("Rut contraparte 2 invalido: {}", rut2);
                return out;
            } else {
                in.RutContraparte2 = rut2;
            }
        }

        if (in.LeiContraparte2 != null && in.LeiContraparte2.length() > 20) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_LEI_CONTRAPARTE2_LARGO",
                    new HashMap<String, String>(), con, in.IdEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "El LEI de la contraparte 2 no debe exceder los 20 caracteres.";
            logger.error("El LEI de la contraparte 2 no debe exceder los 20 caracteres: " + in.LeiContraparte2);
            return out;
        }

        if (in.NombreContraparte2 != null && in.NombreContraparte2.length() > 50) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_NOMBRE_CONTRAPARTE2_LARGO",
                    new HashMap<String, String>(), con, in.IdEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "El nombre de la contraparte 2 no debe exceder los 50 caracteres.";
            logger.error("El nombre de la contraparte 2 no debe exceder los 50 caracteres: " + in.NombreContraparte2);
            return out;
        }

        if (in.PaisContraparte2 != null) {
            if (!isIso3(in.PaisContraparte2)) {
                out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,
                    "ERROR_PAIS_CONTRAPARTE2_INVALIDO", new HashMap<>(), con, in.IdEmpresa));
                out.P_OUT_ERRORESCONTADOR++;
                out.P_OUT_DESCRIPCION = "PaisContraparte2 inválido (ISO 3166-1 alpha-3).";
                logger.error("PaisContraparte2 inválido: {}", in.PaisContraparte2);
                return out;
            }
            in.PaisContraparte2 = normIso(in.PaisContraparte2);
        }

        if (in.PaisMonedaExtranjera != null) {
           if (!isIso3(in.PaisMonedaExtranjera)) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,"ERROR_PAIS_MONEDA_EXTRANJERA_INVALIDO", new HashMap<>(), con, in.IdEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "PaisMonedaExtranjera invalido (ISO 3166-1 alpha-3).";
            logger.error("PaisMonedaExtranjera invalido: {}", in.PaisMonedaExtranjera);
            return out;
           }
            in.PaisMonedaExtranjera = normIso(in.PaisMonedaExtranjera);
        }

        if (in.RutIntermediario != null) {
            String rutInt = in.RutIntermediario.trim();
            if (rutInt.isEmpty()) {
                in.RutIntermediario = null;
            } else if (!ValidacionRut(rutInt)) {
                out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,"ERROR_RUT_INTERMEDIARIO_INVALIDO",
                        new HashMap<>(), con, in.IdEmpresa));
                out.P_OUT_ERRORESCONTADOR++;
                out.P_OUT_DESCRIPCION = "Rut intermediario invalido.";
                logger.error("Rut intermediario invalido: {}", rutInt);
                return out;
            } else {
                in.RutIntermediario = rutInt;
            }
        }
                
        if (in.CodigoOperacionCambio != null) {
            in.CodigoOperacionCambio = in.CodigoOperacionCambio.trim();
            if (!in.CodigoOperacionCambio.matches("\\d{1,6}")) {
                out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,
                        "ERROR_CODIGO_OPERACION_CAMBIO_FORMATO", new HashMap<>(), con, in.IdEmpresa));
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
        }

        String codOpEfectivo = null;
        if (in.CodigoOperacionCambio != null) {
            codOpEfectivo = in.CodigoOperacionCambio;
        } else if (rows.get(0).CodigoOperacionCambio != null && rows.get(0).CodigoOperacionCambio.matches("\\d{1,6}")) {
            codOpEfectivo = rows.get(0).CodigoOperacionCambio.trim();
        }

        boolean aplicarReglasCodOp = codOpEfectivo != null;
        boolean starts2004o2005 = aplicarReglasCodOp && (codOpEfectivo.startsWith("2004") || codOpEfectivo.startsWith("2005"));
        boolean starts2 = aplicarReglasCodOp && codOpEfectivo.startsWith("2");
        boolean starts1 = aplicarReglasCodOp && codOpEfectivo.startsWith("1");
        Function<String, Boolean> hasText = s -> s != null && !s.trim().isEmpty();

        if (in.AfectaDerivados != null) {
            in.AfectaDerivados = in.AfectaDerivados.trim().toUpperCase();
            if (!("S".equals(in.AfectaDerivados) || "N".equals(in.AfectaDerivados))) {
                out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,
                        "ERROR_AFECTA_DERIVADOS_VALOR", new HashMap<>(), con, in.IdEmpresa));
                out.P_OUT_ERRORESCONTADOR++;
                out.P_OUT_DESCRIPCION = "El campo AfectaDerivados debe ser 'S' o 'N'.";
                logger.error("AfectaDerivados invalido: {}", in.AfectaDerivados);
                return out;
            }
        }

        String afectaEf = (in.AfectaDerivados != null ? in.AfectaDerivados : (rows.get(0).AfectaDerivados != null ? rows.get(0).AfectaDerivados.trim().toUpperCase() : null));

        if ("S".equals(afectaEf)) {
            if (in.IdentificadorContratoDerivados != null && in.IdentificadorContratoDerivados.length() > 52) {
                out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_IDENTIFICADOR_CONTRATO_DERIVADOS_INVALIDO", 
                        new HashMap<>(), con, in.IdEmpresa));
                out.P_OUT_ERRORESCONTADOR++;
                out.P_OUT_DESCRIPCION = "IdentificadorContratoDerivados es obligatorio (Afecta=S) y largo <= 52.";
                logger.error("IdentificadorContratoDerivados es obligatorio (Afecta=S) y largo <= 52: " + in.IdentificadorContratoDerivados);
                return out;
            }

            String idDerEf = (in.IdentificadorContratoDerivados != null ? in.IdentificadorContratoDerivados : rows.get(0).IdentificadorContratoDerivados);
            if (idDerEf == null || idDerEf.trim().isEmpty() || idDerEf.trim().length() > 52) {
                out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,
                        "ERROR_IDENTIFICADOR_CONTRATO_DERIVADOS_INVALIDO", new HashMap<>(), con, in.IdEmpresa));
                out.P_OUT_ERRORESCONTADOR++;
                out.P_OUT_DESCRIPCION = "IdentificadorContratoDerivados obligatorio (Afecta=S) y largo <= 52.";
                logger.error("IdentificadorContratoDerivados invalido (efectivo): {}", idDerEf);
                return out;
            }

            String fscEf = (in.FechaSuscripcionContrato != null ? in.FechaSuscripcionContrato
                : rows.get(0).FechaSuscripcionContrato.toString());
            String hscEf = (in.HoraSuscripcionContrato != null ? in.HoraSuscripcionContrato
                : rows.get(0).HoraSuscripcionContrato);

            try {
                DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate.parse(fscEf, df);
            } catch (Exception e) {
                out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,
                    "ERROR_FECHA_SUSCRIPCION_CONTRATO_INVALIDA", new HashMap<>(), con, in.IdEmpresa));
                out.P_OUT_ERRORESCONTADOR++;
                out.P_OUT_DESCRIPCION = "FechaSuscripcionContrato debe ser YYYY-MM-DD (Afecta=S).";
                logger.error("FechaSuscripcionContrato invalida (efectiva): {}", fscEf);
                return out;
            }
            try {
                DateTimeFormatter tf = DateTimeFormatter.ofPattern("HH:mm:ss");
                LocalTime.parse(hscEf, tf);
            } catch (Exception e) {
                out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,
                    "ERROR_HORA_SUSCRIPCION_CONTRATO_INVALIDA", new HashMap<>(), con, in.IdEmpresa));
                out.P_OUT_ERRORESCONTADOR++;
                out.P_OUT_DESCRIPCION = "HoraSuscripcionContrato debe ser HH:MM:SS (Afecta=S).";
                logger.error("HoraSuscripcionContrato invalida (efectiva): {}", hscEf);
                return out;
            }

        } else {
            if (in.IdentificadorContratoDerivados != null && !in.IdentificadorContratoDerivados.trim().isEmpty()) {
                out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,
                        "ERROR_IDENTIFICADOR_CONTRATO_DERIVADOS_INVALIDO", new HashMap<>(), con, in.IdEmpresa));
                out.P_OUT_ERRORESCONTADOR++;
                out.P_OUT_DESCRIPCION = "No se debe informar IdentificadorContratoDerivados cuando AfectaDerivados='N'.";
                logger.error("IdentificadorContratoDerivados informado con Afecta=N: {}", in.IdentificadorContratoDerivados);
                return out;
            }
            if (in.FechaSuscripcionContrato != null || in.HoraSuscripcionContrato != null) {
                out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,
                        "ERROR_FECHA_SUSCRIPCION_CONTRATO_INVALIDA", new HashMap<>(), con, in.IdEmpresa));
                out.P_OUT_ERRORESCONTADOR++;
                out.P_OUT_DESCRIPCION = "No se deben informar fecha/hora de suscripcion cuando AfectaDerivados='N'.";
                logger.error("Fecha/Hora Suscripcion informadas con Afecta=N: {} / {}", in.FechaSuscripcionContrato, in.HoraSuscripcionContrato);
                return out;
            }
        }

        if (in.OperacionEmpresaRelacionada != null) {
            in.OperacionEmpresaRelacionada = in.OperacionEmpresaRelacionada.trim().toUpperCase();
            if (!("S".equals(in.OperacionEmpresaRelacionada) || "N".equals(in.OperacionEmpresaRelacionada))) {
                out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,
                    "ERROR_OPERACION_EMPRESA_RELACIONADA_INVALIDA", new HashMap<>(), con, in.IdEmpresa));
                out.P_OUT_ERRORESCONTADOR++;
                out.P_OUT_DESCRIPCION = "OperacionEmpresaRelacionada debe ser 'S' o 'N'.";
                return out;
            }
        }

        if (aplicarReglasCodOp) {
            if (starts2004o2005) {
                if (hasText.apply(in.NumeroInscripcionObligacionExterior) && in.NumeroInscripcionObligacionExterior.length() > 50) {
                    out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "NUMERO_INSCRIPCION_OBLIGACION_EXTERIOR_INVALIDO", 
                            new HashMap<>(), con, in.IdEmpresa));
                    out.P_OUT_ERRORESCONTADOR++;
                    out.P_OUT_DESCRIPCION = "NumeroInscripcionObligacionExterior <= 50.";
                    return out;
                }
            } else if (hasText.apply(in.NumeroInscripcionObligacionExterior)) {
                out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "NUMERO_INSCRIPCION_OBLIGACION_EXTERIOR_INVALIDO", 
                        new HashMap<>(), con, in.IdEmpresa));
                out.P_OUT_ERRORESCONTADOR++;
                out.P_OUT_DESCRIPCION = "NumeroInscripcionObligacionExterior Debe ir vacio si CodigoOperacionCambio no comienza con 2004/2005.";
                return out;
            }

            if (starts2) {
                if (hasText.apply(in.NombreFinancistaAporante) && in.NombreFinancistaAporante.length() > 50) {
                    out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,
                        "NOMBRE_FINANCISTA_APORTANTE_INVALIDO", new HashMap<>(), con, in.IdEmpresa));
                    out.P_OUT_ERRORESCONTADOR++;
                    out.P_OUT_DESCRIPCION = "NombreFinancistaAportante <= 50.";
                    return out;
                }

                if (hasText.apply(in.LeiFinancistaAporante) && in.LeiFinancistaAporante.length() > 20) {
                    out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,
                        "LEI_FINANCISTA_APORTANTE_INVALIDO", new HashMap<>(), con, in.IdEmpresa));
                    out.P_OUT_ERRORESCONTADOR++;
                    out.P_OUT_DESCRIPCION = "LeiFinancistaAportante <= 20.";
                    return out;
                }
            } else {
                if (hasText.apply(in.NombreFinancistaAporante)){
                    out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,
                        "NOMBRE_FINANCISTA_APORTANTE_INVALIDO", new HashMap<>(), con, in.IdEmpresa));
                    out.P_OUT_ERRORESCONTADOR++;
                    out.P_OUT_DESCRIPCION = "NombreFinancistaAportante Debe ir vacio si CodigoOperacionCambio no inicia con '2'.";
                    return out;
                }
                if (hasText.apply(in.LeiFinancistaAporante)) {
                    out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,
                        "LEI_FINANCISTA_APORTANTE_INVALIDO", new HashMap<>(), con, in.IdEmpresa));
                    out.P_OUT_ERRORESCONTADOR++;
                    out.P_OUT_DESCRIPCION = "LeiFinancistaAportante Debe ir vacio si CodigoOperacionCambio no inicia con '2'.";
                    return out;
                }
            }

            if (starts1) {
                if (hasText.apply(in.NombreReceptorInversion) && in.NombreReceptorInversion.length() > 50) {
                    out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,
                        "NOMBRE_RECEPTOR_INVERSION_INVALIDO", new HashMap<>(), con, in.IdEmpresa));
                    out.P_OUT_ERRORESCONTADOR++;
                    out.P_OUT_DESCRIPCION = "NombreReceptorInversion <= 50.";
                    return out;
                }
                if (hasText.apply(in.LeiReceptorInversion) && in.LeiReceptorInversion.length() > 20) {
                    out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,
                        "LEI_RECEPTOR_INVERSION_INVALIDO", new HashMap<>(), con, in.IdEmpresa));
                    out.P_OUT_ERRORESCONTADOR++;
                    out.P_OUT_DESCRIPCION = "LeiReceptorInversion <= 20.";
                    return out;
                }
            } else {
                if (hasText.apply(in.NombreReceptorInversion)) {
                    out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,
                        "NOMBRE_RECEPTOR_INVERSION_INVALIDO", new HashMap<>(), con, in.IdEmpresa));
                    out.P_OUT_ERRORESCONTADOR++;
                    out.P_OUT_DESCRIPCION = "NombreReceptorInversion Debe ir vacio si CodigoOperacionCambio no inicia con '1'.";
                    return out;
                }
                if (hasText.apply(in.LeiReceptorInversion)) {
                    out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,
                        "LEI_RECEPTOR_INVERSION_INVALIDO", new HashMap<>(), con, in.IdEmpresa));
                    out.P_OUT_ERRORESCONTADOR++;
                    out.P_OUT_DESCRIPCION = "LeiReceptorInversion ir vacio si CodigoOperacionCambio no inicia con '1'.";
                    return out;
                }
            }
        }

        if (in.ModalidadInversionCustodio != null) {
            if (!(in.ModalidadInversionCustodio.equals("01") || in.ModalidadInversionCustodio.equals("02"))) {
                out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,
                    "MODALIDAD_INVERSION_CUSTODIO_INVALIDA", new HashMap<>(), con, in.IdEmpresa));
                out.P_OUT_ERRORESCONTADOR++;
                out.P_OUT_DESCRIPCION = "ModalidadInversionCustodio debe ser '01' o '02' si viene.";
                return out;
            }
        }        

        HashMap<String, Object> paramsIN = new HashMap<>();
        paramsIN.put("IdEmpresa", in.IdEmpresa);
        paramsIN.put("IdUsuario", in.IdUsuario);
        paramsIN.put("IdModulo", "NOR");
        paramsIN.put("IdDocumento", rows.get(0).IdDocumento);
        paramsIN.put("Id", rows.get(0).ID);
        paramsIN.put("Estado", rows.get(0).Estado);
        paramsIN.put("EstadoSICAM", rows.get(0).EstadoSICAM);
        paramsIN.put("SICAM", rows.get(0).SICAM);
        paramsIN.put("FuenteDatos", rows.get(0).FuenteDatos);
        //Datos del reporte
        paramsIN.put("TipoRegistro", rows.get(0).TipoRegistro);
        paramsIN.put("EventoReporte", rows.get(0).EventoReporte);
        paramsIN.put("TipoOperacion", rows.get(0).TipoOperacion);
        paramsIN.put("FechaNegociacion", rows.get(0).FechaNegociacion);
        paramsIN.put("HoraNegociacion", rows.get(0).HoraNegociacion);
        paramsIN.put("FechaValutaMonedaRecibida", rows.get(0).FechaValutaMonedaRecibida);
        paramsIN.put("FechaValutaMonedaEntregada", rows.get(0).FechaValutaMonedaEntregada);
        paramsIN.put("Identificador", rows.get(0).Identificador);
        paramsIN.put("RutContraparte1", rows.get(0).RUTContraparte1);
        paramsIN.put("LeiContraparte1", rows.get(0).LEIContraparte1);
        paramsIN.put("NombreContraparte1", rows.get(0).NombreContraparte1);
        paramsIN.put("RutContraparte2", in.RutContraparte2 != null ? in.RutContraparte2 : rows.get(0).RUTContraparte2); // Editable
        paramsIN.put("LeiContraparte2", in.LeiContraparte2 != null ? in.LeiContraparte2 : rows.get(0).LEIContraparte2); // Editable
        paramsIN.put("NombreContraparte2", in.NombreContraparte2 != null ? in.NombreContraparte2 : rows.get(0).NombreContraparte2); // Editable
        paramsIN.put("PaisContraparte2", in.PaisContraparte2 != null ? in.PaisContraparte2 : rows.get(0).PaisContraparte2); // Editable
        paramsIN.put("RutIntermediario", in.RutIntermediario != null ? in.RutIntermediario : rows.get(0).RUTIntermediario); // Editable
        paramsIN.put("PlataformaNegociacion", rows.get(0).PlataformaNegociacion);
        paramsIN.put("CodigoOperacionCambio", codOpEfectivo); // Editable
        paramsIN.put("PaisMonedaExtranjera", in.PaisMonedaExtranjera != null ? in.PaisMonedaExtranjera : rows.get(0).PaisMonedaExtranjera);
        paramsIN.put("MonedaCompradaRecibida", rows.get(0).MonedaCompradaRecibida);
        paramsIN.put("MontoMonedaCompradaRecibida", rows.get(0).MontoMonedaCompradaRecibida);
        paramsIN.put("MonedaVendidaEntregada", rows.get(0).MonedaVendidaEntregada);
        paramsIN.put("MontoMonedaVendidaEntregada", rows.get(0).MontoMonedaVendidaEntregada);
        paramsIN.put("ConvencionTipoCambio", rows.get(0).ConvencionTipoCambio);
        paramsIN.put("TipoCambio", rows.get(0).TipoCambio);
        paramsIN.put("AfectaDerivados", in.AfectaDerivados != null ? in.AfectaDerivados : rows.get(0).AfectaDerivados); // Editable
        paramsIN.put("IdentificadorContratoDerivados", in.IdentificadorContratoDerivados != null ? in.IdentificadorContratoDerivados : rows.get(0).IdentificadorContratoDerivados); // Editable
        paramsIN.put("FechaSuscripcionContrato", in.FechaSuscripcionContrato != null ? in.FechaSuscripcionContrato : rows.get(0).FechaSuscripcionContrato); // Editable
        paramsIN.put("HoraSuscripcionContrato", in.HoraSuscripcionContrato != null ? in.HoraSuscripcionContrato : rows.get(0).HoraSuscripcionContrato); // Editable
        paramsIN.put("OperacionEmpresaRelacionada", in.OperacionEmpresaRelacionada != null ? in.OperacionEmpresaRelacionada : rows.get(0).OperacionEmpresaRelacionada); // Editable
        paramsIN.put("NumeroInscripcionObligacionExterior", in.NumeroInscripcionObligacionExterior != null ? in.NumeroInscripcionObligacionExterior : rows.get(0).NumeroInscripcionObligacionExterior); // Editable
        paramsIN.put("NombreFinancistaAporante", in.NombreFinancistaAporante != null ? in.NombreFinancistaAporante : rows.get(0).NombreFinancistaAporante); // Editable
        paramsIN.put("LeiFinancistaAporante", in.LeiFinancistaAporante != null ? in.LeiFinancistaAporante : rows.get(0).LEIFinancistaAporante); // Editable
        paramsIN.put("NombreReceptorInversion", in.NombreReceptorInversion != null ? in.NombreReceptorInversion : rows.get(0).NombreReceptorInversion); // Editable
        paramsIN.put("LeiReceptorInversion", in.LeiReceptorInversion != null ? in.LeiReceptorInversion : rows.get(0).LEIReceptorInversion); // Editable
        paramsIN.put("ModalidadInversionCustodio", in.ModalidadInversionCustodio != null ? in.ModalidadInversionCustodio : rows.get(0).ModalidadInversionCustodio); // Editable
        paramsIN.put("IdOperacion", rows.get(0).IdOperacion);
        paramsIN.put("IdCliente", rows.get(0).IdCliente);
        paramsIN.put("IdTipoOperacion", rows.get(0).IdTipoOperacion);
        paramsIN.put("Nemo", rows.get(0).Nemo);
        paramsIN.put("IdMonedaPrincipal", rows.get(0).IdMonedaPrincipal);
        paramsIN.put("IdMonedaSecundaria", rows.get(0).IdMonedaSecundaria);
        paramsIN.put("FolioOperacion", rows.get(0).FolioOperacion);
        paramsIN.put("FechaLiquidacion", rows.get(0).FechaLiquidacion);
        paramsIN.put("IdCartera", rows.get(0).IdCartera);
        HashMap<String, Class> paramsOUT = new HashMap<>();
        paramsOUT.put("p_out_id", Long.class);
        paramsOUT.put("p_out_AdvertenciasContador", Integer.class);
        paramsOUT.put("p_out_AdvertenciasDetalle", Detalle.class);
        paramsOUT.put("p_out_ErroresContador", Integer.class);
        paramsOUT.put("p_out_ErroresDetalle", Detalle.class);

        HashMap<String, Object> ResultSp = sql.call(con, SQL_MODIFICAR_REPORTE, paramsIN, paramsOUT);

        logger.info("Resultado de la llamada al procedimiento almacenado: " + ResultSp);

        @SuppressWarnings("unchecked")
        List<Detalle> advList = (List<Detalle>) ResultSp.get("p_out_AdvertenciasDetalle");
        @SuppressWarnings("unchecked")
        List<Detalle> errList = (List<Detalle>) ResultSp.get("p_out_ErroresDetalle");

        if (advList.stream().anyMatch(d -> "REQUIERE_AUTORIZACION".equals(d.Codigo))) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "REQUIERE_AUTORIZACION",
                    new HashMap<String, String>(), con, in.IdEmpresa));
            out.P_OUT_ERRORESCONTADOR = 0L;
            out.P_OUT_DESCRIPCION = "La operacion requiere que sea autorizada por un Supervisor.";
            logger.info("La operacion requiere que sea autorizada por un Supervisor.");
        } else if (errList.stream().anyMatch(d -> "Autorizados".equals(d.Codigo))
                || errList.stream().anyMatch(d -> "Usuario".equals(d.Codigo) || errList != null || advList != null)) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "MODIFICACION_NO_AUTORIZADA",
                    new HashMap<String, String>(), con, in.IdEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "No existen autorizados para las etapas del tipo evento: MODIFICACION REPORTE SICAM 2.1";
            logger.info("No existen autorizados para las etapas del tipo evento: MODIFICACION REPORTE SICAM 2.1");
        } else if (errList == null || errList.isEmpty() && advList == null || advList.isEmpty()) {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, "REPORTE_MODIFICADO",
                    new HashMap<String, String>(), con, in.IdEmpresa));
            out.P_OUT_ERRORESCONTADOR = 0L;
            out.P_OUT_DESCRIPCION = "El reporte ha sido modificado correctamente.";
            logger.info("Reporte modificado correctamente.");
        } else {
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_MODIFICAR_REPORTE",
                    new HashMap<String, String>(), con, in.IdEmpresa));
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_DESCRIPCION = "Se encontraron errores al modificado el reporte.";
            logger.info("Se encontraron errores al modificado el reporte.");
        }

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

    @Override
    public JsonObject call(Connection conn, JsonObject in) throws Exception {
        Gson gson = new GsonBuilder().create();
        EntradaReporte2p1 entrada = gson.fromJson(in, EntradaReporte2p1.class);

        SalidaReporte2p1 out = modificarReporte(conn, entrada);

        JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
        return dataout;
    }

    /**
     * Metodo principal para probar la modificacion de un reporte.
     *
     * @param args Argumentos de linea de comandos (no se utilizan).
     */
    public static void main(String[] args) {
        try (Connection con = PlataformaUtils.getPropertiesConnection("desa01",
                "C:\\Users\\vaguilera_ext\\Documents\\GIT_OPTIMUS\\optimus-corredores\\connections.properties")) {

            try {
                Gson gson = Utils.getGsonBuilder();
                String json = "{\n" +
                        "    \"IdUsuario\": \"1\",\n" +
                        "    \"RutContraparte1\": \"76011193-7\",\n" +
                        "    \"FechaNegociacion\": \"2025-08-13\",\n" +
                        "    \"HoraNegociacion\": \"10:00:00\",\n" +
                        "    \"Identificador\": \"123456\",\n" +
                        "    \"RutContraparte2\": \"20720657-1\",\n" +
                        "    \"LeiContraparte2\": \"TEST\",\n" +
                        "    \"NombreContraparte2\": \"LOL\",\n" +
                        "    \"PaisContraparte2\": \"USA\",\n" +
                        "    \"RutIntermediario\": \"20954536-5\",\n" +
                        "    \"CodigoOperacionCambio\": \"500401\",\n" +
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

                ModificarReportes service = new ModificarReportes();

                service.modificarReporte(con, Objin);
                con.commit();
            } catch (Exception e) {
                logger.error("Error al modificar el reporte", e);
                con.rollback();
            }
        } catch (Exception e) {
            logger.error("Error al conectar a la base de datos", e);
        }
    }
}