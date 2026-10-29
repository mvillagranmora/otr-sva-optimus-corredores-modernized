package cl.bcs.fondomutuo.consultas.dolar;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** JSON types of legacy {@code ConsultarValorDolarFondoMutuo} (field names and order = legacy: the golden compares the raw text). */
public final class ConsultarDolarContrato {

    private ConsultarDolarContrato() {
    }

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public List<SerieFondoParidad> p_Series = new ArrayList<>();
    }

    public static class SerieFondoParidad {
        public String IdSerieFondo;
        public String IdTipoSolicitud;
        public String Fecha;
    }

    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<ResultadoValorDolarFondoMutuo> P_OUT_RESULTADO = new ArrayList<>();
    }

    public static class ResultadoValorDolarFondoMutuo {
        public BigDecimal ValorParidad;
        public String IdSerieFondo;
        public String Fecha;
        public String IdTipoSolicitud;
    }

    /** Row of {@code ConsultarValorDolarFondoMutuo_SelectValorCuotaTipoCambio.sql}. */
    public static class ValorCuotaTipoParidad {
        public String IdSerieFondo;
        public BigDecimal ValorDolarAporte;
        public BigDecimal ValorDolarRescate;
    }
}
