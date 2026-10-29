package cl.bcs.fondomutuo.consultas.reinversion;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** JSON types of legacy {@code ConsultarFondoMutuoReinversion} (field names and order = legacy: the golden compares the raw text). */
public final class ConsultarReinversionContrato {

    private ConsultarReinversionContrato() {
    }

    public static class Entrada {
        public long p_IdEmpresa;
        public long p_IdUsuario;
        public String p_IdModulo;
        public String p_IdEstado;
        public String p_IdMoneda;
        public String p_FondoExtranjero;
        public String p_IdFamiliaFondo;
        public String p_IdEmisor;
        public String p_IdRegimenFondoMutuo;
        public String p_IdSerieFondoOrigen;
    }

    public static class Salida {
        public List<FiltroFondoMutuo> P_OUT_LISTA = new ArrayList<>();
    }

    public static class FiltroFondoMutuo {
        public String IdSerieFondo;
        public String NombreSerieFondo;
        public String IdMoneda;
        public String IdEmisor;
        public String MLT;
        public String IdRegimenFondoMutuo;
        public BigDecimal ValorCuotaInstrumento;
        public BigDecimal MontoPatrimonioEmisor;
        public BigDecimal MontoMinimo;
    }
}
