package cl.bcs.tesoreria.javacall;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.tesoreria.javacall.kit.JsonLegacy;
import cl.bcs.tesoreria.javacall.kit.SqlNombrado;

/**
 * Legacy {@code cl.bcs.tesoreria.ConsultarFormatoArchivo} (TES-B-194..198): the definition of an output file format (header and detail rows) of a
 * bank, with the company and user of the request copied into the header.
 *
 * <p>The legacy has a typed entry {@code logica} that answers a LIST of headers (empty when the format does not exist) and a JavaCall entry
 * {@code call} that serialises that list as a JSON array and then reads it as a JSON object. {@link #consultar} is the first and
 * {@link #execute} the second. LEGACY DEFECT kept (see README TES-B-197): {@code execute} always throws {@link IllegalStateException}.
 */
@Service
public class ConsultarFormatoArchivoService {

    public static class EntradaFormatoArchivo {
        public Long IdFormatoArchivo;
        public Long IdEmpresa;
        public Long IdUsuario;
    }

    public static class SalidaFormatoArchivo {
        public Long IdEmpresa;
        public Long IdUsuario;
        public Long IdNominaTransferencia;
        public Long IdFormatoArchivo;
        public String CodigoFormatoArchivo;
        public Long IdBanco;
        public String NombreBanco;
        public String IdMoneda;
        public String IdMedioDePago;
        public String FormatoSalida;
        public String Descripcion;
        public String Servicio;
        public String RutaTesoreria;
        public String RutaCompleta;
        public Long CantidadDeTiposDeRegistro;
        public String NombreArchivoSalida;
        public String SeparadorCampos;
        public String SeparadorDecimales;
        public String RellenarCampo;
        public List<FormatoArchivoDetalle> Detalles;
    }

    public static class FormatoArchivoDetalle {
        public Long IdFormatoArchivoDetalle;
        public String CodigoFormatoArchivo;
        public Integer TipoRegistro;
        public String Campo;
        public String TipoDato;
        public Integer Largo;
        public Integer Decimales;
        public String ValorDefecto;
        public Integer Orden;
        public String DescripcionFormatoDetalle;
        public String Formato;
    }

    private static final String SQL_CABECERA = SqlNombrado.recurso("ConsultarFormatoArchivo_ConsultarCabecera");
    private static final String SQL_DETALLES = SqlNombrado.recurso("ConsultarFormatoArchivo_ConsultarDetalles");

    private final SqlNombrado sql;

    public ConsultarFormatoArchivoService(NamedParameterJdbcTemplate jdbc) {
        this.sql = new SqlNombrado(jdbc);
    }

    /** Legacy {@code call}. LEGACY DEFECT kept (see README TES-B-197): the answer is a JSON array and {@code getAsJsonObject()} rejects it. */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public JsonObject execute(JsonObject entrada) throws Exception {
        Gson gson = JsonLegacy.gson();
        return JsonParser.parseString(gson.toJson(logica(JsonLegacy.de(entrada, EntradaFormatoArchivo.class)))).getAsJsonObject();
    }

    /** Legacy typed {@code logica}, with the request and the answer as JSON (the answer is the array of headers). */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public JsonArray consultar(JsonObject entrada) throws Exception {
        return JsonLegacy.gson().toJsonTree(logica(JsonLegacy.de(entrada, EntradaFormatoArchivo.class))).getAsJsonArray();
    }

    List<SalidaFormatoArchivo> logica(EntradaFormatoArchivo in) throws SQLException {
        Map<String, Object> parametros = new java.util.HashMap<>();
        parametros.put("IdFormatoArchivo", in.IdFormatoArchivo);
        List<SalidaFormatoArchivo> formato = sql.consultar(SQL_CABECERA, parametros, SalidaFormatoArchivo.class);
        if (formato.isEmpty()) {
            return formato;
        }
        formato.get(0).Detalles = sql.consultar(SQL_DETALLES, parametros, FormatoArchivoDetalle.class);
        formato.get(0).IdEmpresa = in.IdEmpresa;
        formato.get(0).IdUsuario = in.IdUsuario;
        return formato;
    }
}
