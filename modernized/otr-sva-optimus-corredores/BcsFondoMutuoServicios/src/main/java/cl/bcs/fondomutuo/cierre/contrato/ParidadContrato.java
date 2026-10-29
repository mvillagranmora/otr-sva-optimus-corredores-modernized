package cl.bcs.fondomutuo.cierre.contrato;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.kit.JsonLegacy;
import cl.bcs.fondomutuo.port.out.http.ParidadPort;

/**
 * JSON contract of Plataforma {@code CalcularParidad} (port H {@link ParidadPort}): the legacy {@code CalcularParidadEntrada} / {@code CalculoEntrada} in full
 * (field names, defaults and null keys exactly as the legacy serialised them, so the call is the one the oracle's double records) and the part of
 * {@code CalcularParidadSalida} / {@code CalculoSalida} that the closure reads, with the legacy defaults of the constructors. One call = one POST through the router.
 */
public final class ParidadContrato {

    private ParidadContrato() {
    }

    public static class CalcularParidadEntrada {
        public Long p_IdEmpresa;
        public Long p_IdTrabajo;
        public List<CalculoEntrada> p_CalculosEntrada;

        public CalcularParidadEntrada() {
            this.p_CalculosEntrada = new ArrayList<CalculoEntrada>();
        }
    }

    public static class CalculoEntrada {
        public Long IdTrabajo;
        public Long IdInterno;
        public String Fecha;
        public String IdMonedaOrigen;
        public String IdMonedaDestino;
        public BigDecimal ValorCambioLocalModificado = BigDecimal.ZERO;
        public BigDecimal MontoOrigen;
        public String IdUso;

        public CalculoEntrada() {
            this.ValorCambioLocalModificado = BigDecimal.ZERO;
            this.IdUso = "";
        }
    }

    public static class CalculoSalida {
        public Long IdTrabajo;
        public Long IdInterno;
        public BigDecimal FactorOrigenDestino;
        public BigDecimal MontoDestino;
        public BigDecimal FactorCambioLocal;

        public CalculoSalida() {
            this.FactorOrigenDestino = BigDecimal.ZERO;
            this.FactorCambioLocal = BigDecimal.ZERO;
        }
    }

    public static class CalcularParidadSalida {
        public List<CalculoSalida> p_out_CalculosSalida;
        public Long P_OUT_ERRORESCONTADOR = 0L;

        public CalcularParidadSalida() {
            this.p_out_CalculosSalida = new ArrayList<CalculoSalida>();
        }
    }

    /** One call of the port: the legacy {@code calcularParidad.logica(con, entrada)}. */
    public static CalcularParidadSalida calcular(ParidadPort puerto, CalcularParidadEntrada entrada) {
        JsonObject solicitud = JsonLegacy.conNulos().toJsonTree(entrada).getAsJsonObject();
        return JsonLegacy.conNulos().fromJson(puerto.calcular(solicitud), CalcularParidadSalida.class);
    }
}
