package cl.bcs.fondomutuo.comisionnn;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import cl.bcs.fondomutuo.kit.MensajeSalida;

/** JSON contract (legacy field names, order and defaults) of {@code CrearSolicitudRescateComisionNN}: input and output classes. */
public final class ComisionNnContrato {

    private ComisionNnContrato() {
    }

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_SimularOperacion;
        public String p_UsaReglasNegocio;
        public String p_IdTipoCanalIngreso;
        public List<InSolicitudesRescate> p_SolicitudesRescate = new ArrayList<>();
    }

    public static class InSolicitudesRescate {
        public Long IdInterno;
        public Long IdCliente;
        public Long IdPortafolio;
        public String IdSerieFondo;
        public BigDecimal Monto;
        public BigDecimal NumeroCuotas;
        public String IdMoneda;
        public Long FolioContrato;
        public String IdRegimenTributario;
        public String IngresoAutomatico = "N";
        public Date FechaValorCuota;
        public Long FolioCartera;
        public Long IdCartera = 0L;
        public Long IdSolicitudAporteRescateRelacionado = 0L;
        public String IdMonedaPago;
    }

    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public Long P_OUT_ADVERTENCIASCONTADOR = 0L;
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
        public List<ResultadoSolicitud> P_OUT_RESULTADO = new ArrayList<>();
        public List<SolicitudAporteRescate> P_OUT_COMISIONES_INSERTADAS = new ArrayList<>();
    }
}
