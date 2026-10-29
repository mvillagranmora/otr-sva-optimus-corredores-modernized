package cl.bcs.fondomutuo.consultas.fecha;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import cl.bcs.fondomutuo.kit.MensajeSalida;

/** JSON types of legacy {@code ConsultarFechaSolicitudAporteRescate} (field names and order = legacy: the golden compares the raw text). */
public final class ConsultarFechaContrato {

    private ConsultarFechaContrato() {
    }

    public static class Solicitud {
        public Long IdInterno;
        public String FechaEmision;
        public String IdSerieFondo;
        public String TipoSolicitud;
        public String IdMoneda = "";
        public String IdMonedaPago = "";
    }

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_SimularOperacion;
        public String p_UsaReglasNegocio;
        public List<Solicitud> p_SolicitudAporteRescate = new ArrayList<>();
    }

    public static class Resultado {
        public Long IdInterno;
        public String IdSerieFondo;
        public Date FechaEmision;
        public Date FechaDigitacion;
        public Date FechaAceptacion;
        public Date FechaValorCuota;
        public Date FechaCertificacion;
        public Date FechaLiquidacion;
    }

    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
        public List<Resultado> P_OUT_RESULTADO = new ArrayList<>();
    }

    public static class ConfiguracionSerie {
        public String IdSerieFondo;
        public String EstadoAporteComercial;
        public String EstadoRescateComercial;
        public String EstadoAporteBackoffice;
        public String EstadoRescateBackoffice;
        public String EstadoLiquidezComercial;
        public String EstadoLiquidezBackoffice;
        public Long DiasRetencionesAporte;
        public Long DiasRetencionesRescate;
        public Long DiasValutaValorCuotaAporte;
        public Long DiasValutaValorCuotaRescate;
        public Long DiasValutaCertificacionAporte;
        public Long DiasValutaCertificacionRescate;
        public Long DiasValutaCurseAporte;
        public Long DiasValutaCurseRescate;
        public String IdEmisor;
        public String FondoExtranjero;
        public Long IdPaisFondo;
        public Long IdPaisEmisor;
        public Long IdPaisMoneda;
        public String IdMercadoFondoMutuo;
        public String FondoNacionalDolar;
    }
}
