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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Legacy {@code cl.bcs.tesoreria.generarArchivoPago.GenerarArchivoPagoBancoEstado} (296 lines): the older variant of
 * {@link GenerarArchivoPagos} (no accent stripping, no RUT sum, the full {@code MontoRemesa} text, the column
 * {@code BancoDelBeneficiario}, the label "Nuamero de Nomina"). No job calls it; it is reachable only through the legacy JavaCall
 * dispatcher, so it is characterized on its own. Same {@link #call} contract as {@link GenerarArchivoPagos}. Defect kept
 * (README, TES-J-DEF-13): the SQL does not return {@code BancoDelBeneficiario}, so the bank column is always empty.
 */
public final class GenerarArchivoPagoBancoEstado {

    private static final String SQL_ENCABEZADO = SqlNombrado.recurso("jobs/GenerarArchivoPago_Select_Encabezado");
    private static final String SQL_DETALLE = SqlNombrado.recurso("jobs/GenerarArchivoPago_Select_Detalle");

    /** Legacy :30-40. */
    public static class EntradaGenerarArchivoPagoBancoEstado {
        public Long p_IdEmpresa;
        public String p_IdModulo;
        public String p_Fecha;
        public Long p_IdNominaTransferencia;
        public ArchivoPago p_Archivo;
    }

    /** Legacy :42-53. */
    public static class SalidaGenerarArchivoPagoBancoEstado {
        public String p_out_NombreArchivoLog;
        public String p_out_RutaArchivoLog;
        public Long p_out_ErroresContador;
        public String p_out_Comentario;
        public List<String> p_out_Archivos;
    }

    /** Legacy :71-85: the detail row, with the column {@code BancoDelBeneficiario}. */
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
        public String BancoDelBeneficiario;
        public String InformacionRemesa;
        public String CodigoDepDcvComprador;
        public String CodigoDepDcvVendedor;
        public String ClaveMedioPago;
    }

    private static final class ArchivoJson {
        String nombreArchivo;
        String extension;
        String ruta;
    }

    private static final class EntradaJson {
        Long p_IdEmpresa;
        String p_IdModulo;
        String p_Fecha;
        Long p_IdNominaTransferencia;
        ArchivoJson p_Archivo;
    }

    private final SqlNombrado sql;

    public GenerarArchivoPagoBancoEstado(JdbcTemplate jdbc) {
        this.sql = new SqlNombrado(new NamedParameterJdbcTemplate(Objects.requireNonNull(jdbc, "jdbc")));
    }

    /** Legacy :99-109 (see TES-J-DEF-18 in {@link GenerarArchivoPagos}). */
    public JsonObject call(JsonObject jin) throws Exception {
        Gson gson = new Gson();
        EntradaJson json = gson.fromJson(jin.toString(), EntradaJson.class);
        EntradaGenerarArchivoPagoBancoEstado in = new EntradaGenerarArchivoPagoBancoEstado();
        in.p_IdEmpresa = json.p_IdEmpresa;
        in.p_IdModulo = json.p_IdModulo;
        in.p_Fecha = json.p_Fecha;
        in.p_IdNominaTransferencia = json.p_IdNominaTransferencia;
        if (json.p_Archivo != null) {
            in.p_Archivo = new ArchivoPago(json.p_Archivo.nombreArchivo, json.p_Archivo.extension, json.p_Archivo.ruta);
        }
        return gson.toJsonTree(logica(in)).getAsJsonObject();
    }

    /** Legacy :111-272. */
    public SalidaGenerarArchivoPagoBancoEstado logica(EntradaGenerarArchivoPagoBancoEstado entrada) throws Exception {
        SalidaGenerarArchivoPagoBancoEstado out = new SalidaGenerarArchivoPagoBancoEstado();
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
            b.encabezado(encabezado, fecha, "Nuamero de Nomina"); // legacy typo kept
        }
        b.tituloDetalle();
        int totalDetalle = 0;
        BigDecimal montoNomina = BigDecimal.ZERO;
        for (RegistrosDetalle detalle : detalles) {
            String[] l = b.nueva(13);
            l[0] = "2";
            l[1] = detalle.TipoDePago;
            l[2] = detalle.RefDetallePago;
            l[3] = detalle.RutBeneficiario;
            l[4] = detalle.NombreBeneficiario;
            l[5] = detalle.MontoRemesa.toString();
            l[6] = detalle.CtaCteBeneficiario;
            l[7] = detalle.BancoDelBeneficiario;
            l[8] = detalle.InformacionRemesa;
            l[9] = detalle.CodigoDepDcvComprador;
            l[10] = detalle.CodigoDepDcvVendedor;
            l[11] = detalle.ClaveMedioPago;
            b.escribir();
            totalDetalle += 1;
            montoNomina = montoNomina.add(detalle.MontoRemesa);
        }
        b.tituloControl();
        String[] control = b.nueva(6);
        control[0] = "3";
        control[1] = totalDetalle + "";
        control[5] = montoNomina.toString();
        b.controlYPie(encabezados);
    }
}
