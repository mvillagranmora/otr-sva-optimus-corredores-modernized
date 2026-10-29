package cl.bcs.fondomutuo.cierre.contrato;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.kit.JsonLegacy;
import cl.bcs.fondomutuo.port.out.http.PreciosTasasPort;

/**
 * JSON contract of Plataforma {@code EntregarPreciosTasas} (port H {@link PreciosTasasPort}): the legacy request in full (null keys written, query date as
 * {@code yyyy-MM-dd HH:mm:ss.SSS} like the legacy {@code java.sql.Date} under the legacy Gson) and the part of each {@code PreciosTasas} row that the
 * closure reads. {@code FechaFuente} stays text: the legacy formatted its Timestamp as {@code yyyy-MM-dd}, which is the first ten characters of the
 * wire format {@code yyyy-MM-dd HH:mm:ss.SSS} (assumption of the H contract, to confirm with the Plataforma router).
 */
public final class PreciosTasasContrato {

    private PreciosTasasContrato() {
    }

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public Long p_IdPais;
        public List<DatosInstrumentos> p_DatosInstrumentos;

        public Entrada() {
            p_DatosInstrumentos = new ArrayList<DatosInstrumentos>();
        }
    }

    public static class DatosInstrumentos {
        public Long IdRegistro;
        public String TipoCartera;
        public String IdUso;
        public String IdTipoCriterioTasa;
        public String IdClasificacionTipoInstrumento;
        public String IdTipoInstrumento;
        public Long IdInstrumento;
        public String Nemotecnico;
        public String IdMoneda;
        public String FechaConsulta;
        public Long PlazoResidual;
        public String NemoSVS;
    }

    public static class PreciosTasas {
        public Long IdRegistro;
        public Long IdInstrumento;
        public BigDecimal Precio;
        public String FechaFuente;
    }

    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR;
        public List<PreciosTasas> P_OUT_PRECIOSTASAS;

        public Salida() {
            P_OUT_PRECIOSTASAS = new ArrayList<PreciosTasas>();
            P_OUT_ERRORESCONTADOR = 0L;
        }
    }

    /** {@code yyyy-MM-dd 00:00:00.000}: the legacy {@code java.sql.Date} of a date under the legacy Gson. */
    public static String fechaConsulta(String fechaIso) {
        return fechaIso + " 00:00:00.000";
    }

    /** One call of the port: the legacy {@code EntregarPreciosTasas.logica(con, entrada)}. */
    public static Salida entregar(PreciosTasasPort puerto, Entrada entrada) {
        JsonObject solicitud = JsonLegacy.conNulos().toJsonTree(entrada).getAsJsonObject();
        return JsonLegacy.conNulos().fromJson(puerto.entregar(solicitud), Salida.class);
    }
}
