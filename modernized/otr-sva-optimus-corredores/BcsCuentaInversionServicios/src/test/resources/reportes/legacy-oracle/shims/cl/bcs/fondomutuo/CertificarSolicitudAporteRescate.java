package cl.bcs.fondomutuo;

import java.math.BigDecimal;
import java.sql.Connection;

import com.google.gson.JsonObject;

import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;

/**
 * ORACLE SHIM of legacy cl.bcs.fondomutuo.CertificarSolicitudAporteRescate (FondoMutuo module). Only the data class Cobro used by
 * ProcesarCobroComisionPatrimonio is kept, copied VERBATIM from CertificarSolicitudAporteRescate.java:309-313.
 */
public class CertificarSolicitudAporteRescate implements JavaCall {

    @Override
    public JsonObject call(Connection conn, JsonObject in) throws Exception {
        throw new UnsupportedOperationException("not part of the reportes oracle");
    }

    public static class Cobro extends BcsPojo{
        public String      IdCobro;
        public BigDecimal  ValorCobro;

    }
}
