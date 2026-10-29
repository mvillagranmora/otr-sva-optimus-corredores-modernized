package cl.bcs.tesoreria.jobs.job;

import cl.bcs.tesoreria.javacall.kit.SqlNombrado;
import cl.bcs.tesoreria.jobs.job.archivo.ArchivoPago;
import cl.bcs.tesoreria.jobs.job.archivo.BloqueBancoEstado;
import cl.bcs.tesoreria.jobs.job.archivo.BloqueBancoEstado.Encabezado;
import cl.bcs.utils.Fecha;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Legacy {@code cl.bcs.tesoreria.generarArchivoPago.GenerarArchivoPagos} (313 lines; a JavaCall class the job GenerarArchivoPago
 * calls for BANCO_ESTADO_CLP_TRADVP): writes the BancoEstado CSV (instruction text, header, detail and control records, signature
 * block) of one nomina.
 *
 * <p>{@link #logica} is the typed entry the job uses; {@link #call} is the legacy JavaCall {@code call(con, in)}: {@code in} is
 * {@code {p_IdEmpresa, p_IdModulo, p_Fecha ("yyyy-MM-dd"), p_IdNominaTransferencia, p_Archivo: {nombreArchivo, extension, ruta}}}, the
 * answer is {@link SalidaGenerarArchivoPagos} with a plain Gson (null fields omitted). TES-J-DEF-18: the legacy {@code call}
 * deserialized {@code p_Archivo} (a class with FileWriter fields) with Gson, which fails on any JDK after 8; here the three
 * fields are read directly. Defects kept (README, TES-J-DEF-12): {@code MontoRemesa} is cut with {@code substring(0, length - 9)}
 * (it assumes a DECIMAL scale of 8), RUTs with dots fail in the RUT sum, the file is not transactional.
 */
public final class GenerarArchivoPagos {

    private static final String SQL_ENCABEZADO = SqlNombrado.recurso("jobs/GenerarArchivoPago_Select_Encabezado");
    private static final String SQL_DETALLE = SqlNombrado.recurso("jobs/GenerarArchivoPago_Select_Detalle");

    /** Legacy :30-41. */
    public static class EntradaGenerarArchivoPagos {
        public Long p_IdEmpresa;
        public String p_IdModulo;
        public String p_Fecha;
        public Long p_IdNominaTransferencia;
        public ArchivoPago p_Archivo;
    }

    /** Legacy :43-54. */
    public static class SalidaGenerarArchivoPagos {
        public String p_out_NombreArchivoLog;
        public String p_out_RutaArchivoLog;
        public Long p_out_ErroresContador;
        public String p_out_Comentario;
        public List<String> p_out_Archivos;
    }

    /** Legacy :71-86 (Select_Detalle.sql). */
    public static class RegistrosDetalle {
        public Integer TipoRegistroOptimus;
        public Long IdInterno;
        public String TipoRegistro;
        public String TipoDePago;
        public String RefDetallePago;
        public String RutBeneficiario;
        public String NombreBeneficiario;
        public BigDecimal MontoRemesa;
        public String CtaCteBeneficiario;
        public String CodigoIsoBancoBeneficiario;
        public String InformacionRemesa;
        public String CodigoDepDcvComprador;
        public String CodigoDepDcvVendedor;
        public String ClaveMedioPago;
    }

    /** The JSON of {@code p_Archivo} of the legacy call. */
    private static final class ArchivoJson {
        String nombreArchivo;
        String extension;
        String ruta;
    }

    /** The JSON of the legacy call input. */
    private static final class EntradaJson {
        Long p_IdEmpresa;
        String p_IdModulo;
        String p_Fecha;
        Long p_IdNominaTransferencia;
        ArchivoJson p_Archivo;
    }

    private final SqlNombrado sql;

    public GenerarArchivoPagos(JdbcTemplate jdbc) {
        this.sql = new SqlNombrado(new NamedParameterJdbcTemplate(Objects.requireNonNull(jdbc, "jdbc")));
    }

    /** Legacy :99-109. */
    public JsonObject call(JsonObject jin) throws Exception {
        Gson gson = new Gson();
        EntradaJson json = gson.fromJson(jin.toString(), EntradaJson.class);
        EntradaGenerarArchivoPagos in = new EntradaGenerarArchivoPagos();
        in.p_IdEmpresa = json.p_IdEmpresa;
        in.p_IdModulo = json.p_IdModulo;
        in.p_Fecha = json.p_Fecha;
        in.p_IdNominaTransferencia = json.p_IdNominaTransferencia;
        if (json.p_Archivo != null) {
            in.p_Archivo = new ArchivoPago(json.p_Archivo.nombreArchivo, json.p_Archivo.extension, json.p_Archivo.ruta);
        }
        return gson.toJsonTree(logica(in)).getAsJsonObject();
    }

    /** Legacy :111-287. A failure leaves the file as the legacy did (the writer is never flushed). */
    public SalidaGenerarArchivoPagos logica(EntradaGenerarArchivoPagos entrada) throws Exception {
        SalidaGenerarArchivoPagos out = new SalidaGenerarArchivoPagos();
        SimpleDateFormat sdfyyMMdd = new SimpleDateFormat("dd-MM-yyyy");
        Date date = Fecha.toDate(entrada.p_Fecha);
        String fecha = sdfyyMMdd.format(date);
        Map<String, Object> param = new HashMap<>();
        param.put("IdNominaTransferencia", entrada.p_IdNominaTransferencia);
        List<Encabezado> resultadoEncabezado = sql.consultar(SQL_ENCABEZADO, param, Encabezado.class);
        List<RegistrosDetalle> resultadoDetalle = sql.consultar(SQL_DETALLE, param, RegistrosDetalle.class);

        if (!resultadoEncabezado.isEmpty() && !resultadoDetalle.isEmpty()) {
            entrada.p_Archivo.crear();
            try {
                escribir(entrada.p_Archivo, fecha, resultadoEncabezado, resultadoDetalle);
            } catch (Exception | Error e) {
                entrada.p_Archivo.abandonar();
                throw e;
            }
        }
        return out;
    }

    private static void escribir(ArchivoPago archivo, String fecha, List<Encabezado> encabezados,
                                 List<RegistrosDetalle> detalles) {
        BloqueBancoEstado b = new BloqueBancoEstado(archivo);
        for (Encabezado encabezado : encabezados) {
            b.encabezado(encabezado, fecha, "Numero de Nomina");
        }
        b.tituloDetalle();
        int totalDetalle = 0;
        BigDecimal montoNomina = BigDecimal.ZERO;
        String[] splitDocumentoIdentidad;
        Long sumatoriaRuts = 0L;
        for (RegistrosDetalle detalle : detalles) {
            String montoRemesaString = (detalle != null && detalle.MontoRemesa != null) ? detalle.MontoRemesa.toString() : null;
            // TES-J-DEF-12: assumes a DECIMAL scale of 8
            montoRemesaString = montoRemesaString != null ? montoRemesaString.substring(0, montoRemesaString.length() - 9) : "";
            String[] l = b.nueva(13);
            l[0] = "2";
            l[1] = detalle.TipoDePago;
            l[2] = detalle.RefDetallePago;
            l[3] = detalle.RutBeneficiario;
            l[4] = Normalizer.normalize(detalle.NombreBeneficiario, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
            l[5] = montoRemesaString;
            l[6] = detalle.CtaCteBeneficiario;
            l[7] = detalle.CodigoIsoBancoBeneficiario;
            l[8] = detalle.InformacionRemesa;
            l[9] = detalle.CodigoDepDcvComprador;
            l[10] = detalle.CodigoDepDcvVendedor;
            l[11] = detalle.ClaveMedioPago;
            b.escribir();
            totalDetalle += 1;
            montoNomina = montoNomina.add(detalle.MontoRemesa);
            splitDocumentoIdentidad = detalle.RutBeneficiario.split("-");
            sumatoriaRuts += Long.parseLong(splitDocumentoIdentidad[0]);
        }
        String montoNominaString = (montoNomina != null) ? montoNomina.toString() : null;
        montoNominaString = montoNominaString != null ? montoNominaString.substring(0, montoNominaString.length() - 9) : "";

        b.tituloControl();
        String[] control = b.nueva(6);
        control[0] = "3";
        control[1] = totalDetalle + "";
        control[3] = Long.toString(sumatoriaRuts);
        control[5] = montoNominaString;
        b.controlYPie(encabezados);
    }
}
