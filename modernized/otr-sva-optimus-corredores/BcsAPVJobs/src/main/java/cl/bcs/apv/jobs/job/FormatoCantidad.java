package cl.bcs.apv.jobs.job;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import cl.bcs.apv.infrastructure.jdbc.SqlNombrado;

/**
 * Legacy {@code MonedaFormato.formatear(con, valor, MonedaFormato.CANTIDAD, idMoneda, idTipoInstrumento, idTipoOperacion).valor}
 * (BcsPlataformaServicios pla/utils/MonedaFormato.java:35-153), the only use the job makes of it: the quantity of a sale is cut or rounded to the decimals
 * the format table gives for the currency.
 *
 * <p>The legacy cached the table in a STATIC for the life of the JVM (RVB-002 note); here the cache lives as long as this object, i.e. the job singleton: the
 * same production behavior (read on first use, never refreshed) without leaking state between test cases.
 */
final class FormatoCantidad {

    private final SqlNombrado sql;
    private final String sqlSelect = SqlNombrado.recurso("jobs/Formato_ConsultarMonedaFormato");
    private List<ObjMonedaFormato> formatos = null;

    FormatoCantidad(SqlNombrado sql) {
        this.sql = sql;
    }

    BigDecimal formatear(BigDecimal valor, String idMoneda, String idTipoInstrumento, String idTipoOperacion) throws Exception {
        if (formatos == null) {
            formatos = sql.consultar(sqlSelect, new java.util.HashMap<>(), ObjMonedaFormato.class);
        }
        String uso = "GEN"; // always GEN
        if (idMoneda == null) {
            throw new Exception("debe especificar moneda");
        }
        if (valor == null) {
            valor = BigDecimal.ZERO;
        }
        ObjMonedaFormato formato = null;
        for (ObjMonedaFormato f : formatos) {
            if (f.IdUsoMonedaFormato.equalsIgnoreCase(uso) && f.IdMoneda.equalsIgnoreCase(idMoneda)
                    && f.IdTipoInstrumento.equalsIgnoreCase(idTipoInstrumento) && f.IdTipoOperacion.equalsIgnoreCase(idTipoOperacion)) {
                formato = f;
                break;
            }
        }
        if (formato == null) { // the specific one was not found
            for (ObjMonedaFormato f : formatos) {
                if (f.IdUsoMonedaFormato.equalsIgnoreCase(uso) && f.IdMoneda.equalsIgnoreCase(idMoneda) && f.IdTipoInstrumento.equalsIgnoreCase(idTipoInstrumento)) {
                    formato = f;
                    break;
                }
            }
        }
        if (formato == null) {
            for (ObjMonedaFormato f : formatos) {
                if (f.IdUsoMonedaFormato.equalsIgnoreCase(uso) && f.IdMoneda.equalsIgnoreCase(idMoneda)) {
                    formato = f;
                    break;
                }
            }
        }
        if (formato == null) {
            throw new Exception("formato no encontrado moneda " + idMoneda + " uso " + uso + " tipo instrumento " + idTipoInstrumento + " tipo operacion " + idTipoOperacion);
        }
        String truncaRedondea = formato.CantidadTrucaRedondea;
        Integer decimales = formato.CantidadDecimales;
        if (truncaRedondea.equalsIgnoreCase("R")) {
            return valor.setScale(decimales, RoundingMode.HALF_UP);
        } else if (truncaRedondea.equalsIgnoreCase("T")) {
            return valor.setScale(decimales, RoundingMode.FLOOR);
        }
        throw new Exception("trunca o redondea invalido " + truncaRedondea + ".");
    }

    /** Legacy {@code MonedaFormato.ObjMonedaFormato} (MonedaFormato.java:166-190), only the columns the CANTIDAD format reads. */
    public static class ObjMonedaFormato {
        public Long IdMonedaFormato;
        public String IdMoneda;
        public String IdUsoMonedaFormato;
        public String IdTipoInstrumento;
        public String IdTipoOperacion;
        public Integer CantidadDecimales;
        public String CantidadTrucaRedondea;
    }
}
