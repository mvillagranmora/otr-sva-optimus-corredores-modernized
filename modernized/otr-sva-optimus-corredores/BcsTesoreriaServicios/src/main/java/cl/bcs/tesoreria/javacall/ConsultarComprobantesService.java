package cl.bcs.tesoreria.javacall;

import static cl.bcs.tesoreria.javacall.TesoreriaJdbcHelper.loadSql;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonObject;

import cl.bcs.tesoreria.javacall.comun.Consultas;
import cl.bcs.tesoreria.javacall.comun.Filas;
import cl.bcs.tesoreria.javacall.comun.JsonLegacy;
import cl.bcs.tesoreria.javacall.comun.MensajeSalida;
import cl.bcs.utils.BcsPojo;

/**
 * Legacy {@code cl.bcs.tesoreria.ConsultarComprobantes}, behaviours TES-A-600..605: reads the consultation view of the requested comprobantes.
 * Read only; a missing id list is a NullPointerException as in the legacy (TES-A-603).
 */
@Service
public class ConsultarComprobantesService {

    private static final String SQL_COMPROBANTES = loadSql("sql/ConsultarComprobantes_SelectComprobantes.sql");

    // ------------------------------------------------------------------ JavaCall contract (legacy names)

    public static class EntradaConsultarComprobantes extends BcsPojo {
        public Long p_IdEmpresa = 0L;
        public Long p_IdUsuario = 0L;
        public String p_IdModulo = "TES";
        public List<IdComprobantes> p_IdComprobantes;
    }

    public static class IdComprobantes extends BcsPojo {
        public Long IdComprobante;
    }

    public static class SalidaConsultarComprobantes extends BcsPojo {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
        public List<Comprobante_Proceso> P_OUT_COMPROBANTES = new ArrayList<>();
    }

    public static class Comprobante_Proceso extends BcsPojo {
        public Long IdComprobante;
        public String CuentaDCVOrigen;
        public String CuentaDCVDestino;
        public String IdMoneda;
        public BigDecimal Monto;
        public java.sql.Date FechaLiquidacion;
        public String NombrePersona;
        public String DocumentoIdentidad;
        public String NombreConcepto;
        public String NombreEstado;
        public String Nemotecnico;
    }

    private final NamedParameterJdbcTemplate jdbc;

    public ConsultarComprobantesService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Legacy {@code call}. */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public JsonObject execute(JsonObject entrada) throws Exception {
        EntradaConsultarComprobantes in = JsonLegacy.PLATAFORMA.fromJson(entrada, EntradaConsultarComprobantes.class);
        return JsonLegacy.PLATAFORMA.toJsonTree(logica(in)).getAsJsonObject();
    }

    /** Legacy {@code logica}. */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public SalidaConsultarComprobantes logica(EntradaConsultarComprobantes entrada) throws Exception {
        List<Long> ids = new ArrayList<>(entrada.p_IdComprobantes.size());
        for (IdComprobantes id : entrada.p_IdComprobantes) {
            ids.add(id.IdComprobante);
        }
        SalidaConsultarComprobantes salida = new SalidaConsultarComprobantes();
        salida.P_OUT_COMPROBANTES = jdbc.query(SQL_COMPROBANTES, Map.of("IdsComprobantes", Consultas.enLista(ids)), Filas.de(Comprobante_Proceso.class));
        return salida;
    }
}
