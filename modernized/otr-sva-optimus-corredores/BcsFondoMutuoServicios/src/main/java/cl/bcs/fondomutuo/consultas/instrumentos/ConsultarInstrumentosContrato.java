package cl.bcs.fondomutuo.consultas.instrumentos;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** JSON types of legacy {@code ConsultarInstrumentosFondosMutuos} (field names and order = legacy: the golden compares the raw text). */
public final class ConsultarInstrumentosContrato {

    private ConsultarInstrumentosContrato() {
    }

    public static class Entrada {
        public long p_IdEmpresa;
        public long p_IdUsuario;
        public String p_IdModulo;
        public InFondos p_EntradaFondosMutuos;
    }

    public static class InFondos {
        public String IdSerieFondo = "";
        public String IdEmisor = "";
        public String IdFamiliaFondo = "";
        public List<String> IdRegimenFondoMutuo = new ArrayList<>();
        public String IdSerieFondoOrigen = "";
        public String IdCategoriaPerfilInversionista = "";
        public String IdEstado = "";
        public String IdMoneda = "";
        public String FondoExtranjero = "";
        public String InstrumentoOWP = "";
        public String EsAPV = "";
        public String OcultarBloqueados = "N";
    }

    public static class Salida {
        public List<OutFondoMutuo> FondosMutuos = new ArrayList<>();
        public List<MensajesErrores> Mensajes = new ArrayList<>();
        public Long ContadorErrores = 0L;
    }

    public static class OutFondoMutuo {
        public String IdSerieFondo;
        public String CodigoFondo;
        public String NombreFondo;
        public String Nemotecnico;
        public String CodigoSerie;
        public String NombreSerie;
        public String IdEmisor;
        public String NombreEmisor;
        public String IdMoneda;
        public String IdFamiliaFondo;
        public BigDecimal ValorCuota;
        public Long DecimalesValorCuota;
        public String FechaValorCuota;
        public BigDecimal MontoMinimoInversionPesos;
        public BigDecimal MontoMinimoInversionTotal;
        public BigDecimal MontoMaximoInversionPesos;
        public BigDecimal MontoMaximoInversionTotal;
        public String IdCategoriaPerfilInversionista;
        public String NombreCategoriaPerfilInversionista;
        public String CobraComision;
        public String InstrumentoOWP;
        public String EsAPV;
        public String RestringeAporte;
        public BigDecimal MontoPatrimonioEmisor;
        public List<OutListaRegimen> ListaRegimen = new ArrayList<>();
    }

    public static class OutListaRegimen {
        public String IdRegimen;
        public String DescripcionRegimen;
        public String PermiteMLT;
    }

    /** Row of {@code ConsultarInstrumentosFondosMutuos_SelectRegimen.sql}. */
    public static class Regimen {
        public String IdSerieFondo;
        public String IdRegimenFondoMutuo;
        public String DescripcionRegimen;
        public String PermiteMLT;
    }

    public static class MensajesErrores {
        public Long IdInterno;
        public String TipoMensaje;
        public String Codigo;
        public String Mensaje;
    }
}
