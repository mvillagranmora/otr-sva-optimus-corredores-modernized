package cl.bcs.fondomutuo.requisitos.contrato;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Contract (legacy field names and defaults) of the collaborator {@code CalcularMontoNumeroCuota}, which step 3A ports. The 3B classes depend on
 * {@link Puerto} only (typed, no JSON hop); step 4 binds the 3A service to it. Only what 3B reads of the answer is declared in {@link Resultado}.
 */
public final class CuotaContrato {

    private CuotaContrato() {
    }

    /** Port of 3B toward {@code CalcularMontoNumeroCuota.logica}. */
    @FunctionalInterface
    public interface Puerto {
        Salida calcular(Entrada entrada) throws Exception;
    }

    public static class Entrada {
        public Long p_IdEmpresa;
        public List<Serie> p_Series = new ArrayList<>();
    }

    public static class Serie {
        public Long IdInterno;
        public String Fecha;
        public String IdSerieFondo;
        public String CalculoPorMontoCuota;
        public BigDecimal Monto;
        public BigDecimal NumeroCuotas;
        public String IdMonedaFondo;
        public String IdMonedaInversion;
        public String TipoSolicitud;
        public String TipoFactorAjuste;
        public Long IdEmpresa;
        public BigDecimal Porcentaje;
        public String IdTipoMovimientoFondoMutuo = "";
    }

    public static class Salida {
        public List<Resultado> P_OUT_RESULTADO = new ArrayList<>();
    }

    public static class Resultado {
        public Long IdInterno;
        public String TipoSolicitud;
        public BigDecimal Monto;
        public BigDecimal NumeroCuotas;
        public BigDecimal ValorCuota;
        public BigDecimal MontoDestino;
        public BigDecimal ValorParidad;
        public BigDecimal ValorFactorAjuste;
        public BigDecimal CostoEnFactorAjuste;
        public String IdMonedaSistema;
        public BigDecimal MontoMonedaSistema;
        public String Resultado;
        public String CodigoMensaje;
        public BigDecimal Porcentaje;
        public Long DecimalesValorCuota;
        public Long DecimalesNumeroCuotas;
        public String FechaValorCuota;
    }
}
