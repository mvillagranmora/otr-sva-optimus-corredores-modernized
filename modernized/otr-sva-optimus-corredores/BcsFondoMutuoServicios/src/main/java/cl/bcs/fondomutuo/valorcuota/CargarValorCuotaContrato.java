package cl.bcs.fondomutuo.valorcuota;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import cl.bcs.fondomutuo.kit.MensajeSalida;

/** JSON types of legacy {@code CargarValorCuota} (names and order = legacy) and the two row types of its SELECTs. */
public final class CargarValorCuotaContrato {

    private CargarValorCuotaContrato() {
    }

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public Long p_IdPais = -1L;
        public String p_IdModulo;
        public String p_UsaReglasNegocio;
        public String p_SimularOperacion;
        public String p_AllFunds;
        public List<ValorCuota> p_ValorCuota = new ArrayList<>();
    }

    public static class ValorCuota {
        public String IDSERIEFONDO;
        public String ISIN = "";
        public String IDMONEDA;
        public String FECHACIERRE;
        public BigDecimal VALORCUOTA;
        public String OBSERVACION;
        public String MENSAJEVALIDACION;
        public String FECHACALCULO = "";
        public Long REGISTRO = 0L;
    }

    /** Legacy {@code ResultadoCargaValorCuota}. */
    public static class Resultado {
        public String IdSerieFondo;
        public String IdMoneda;
        public String ISIN;
        public java.sql.Date FechaVigencia;
        public BigDecimal ValorCuota = BigDecimal.ZERO;
        public BigDecimal ValorCuotaFechaAnterior = BigDecimal.ZERO;
        public String Observacion;
        public Long DecimalesValorCuota;
        public java.sql.Date FechaCalculo;
        public String Estado;
        public String DescEstado;
        public Long IdEmpresa;
        public String EmisorValorCuota;
        public BigDecimal ParidadValorCuota = BigDecimal.ONE;
        public BigDecimal ValorCuotaCalculado = BigDecimal.ZERO;
        public BigDecimal VariacionValorCuota = BigDecimal.ZERO;
    }

    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<Resultado> P_OUT_RESULTADO = new ArrayList<>();
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
    }

    public static class SerieFondoIsin {
        public String IdSerieFondo = "";
        public String ISIN = "";
    }

    /** Legacy {@code ValorCuotaVigente}, with its {@code hashCode} / {@code equals} (a null serie or date is a {@code NullPointerException}). */
    public static class ValorCuotaVigente {
        public String IdSerieFondo;
        public java.sql.Date FechaVigencia;
        public BigDecimal ValorCuotaVigente = BigDecimal.ZERO;

        @Override
        public int hashCode() {
            return 31 * this.IdSerieFondo.hashCode() + this.FechaVigencia.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            ValorCuotaVigente otro = (ValorCuotaVigente) obj;
            return this.IdSerieFondo.equals(otro.IdSerieFondo) && this.FechaVigencia.equals(otro.FechaVigencia);
        }
    }
}
