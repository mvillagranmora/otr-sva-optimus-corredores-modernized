package cl.bcs.fondomutuo.requisitos.contrato;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * JSON contract (legacy names and defaults) of the platform collaborator {@code cl.bcs.plataforma.pla.AgregarLog}, sent through {@code LogPort}.
 * Append-only audit line; by HTTP the owner commits on its own connection (gate G5), see {@code LogPort}.
 */
public final class LogContrato {

    private LogContrato() {
    }

    public static class InAgregarLog {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_UsaReglasNegocio;
        public String p_IdEstadoAutorizacion;
        public Long p_IdTrabajo = 0L;
        public List<Log> p_Log = new ArrayList<>();
        public List<DetalleLog> p_LogDetalle = new ArrayList<>();
    }

    public static class Log {
        public Long IdTrabajo = 0L;
        public Long IdCabeceraLog;
        public String IdTipoLog;
        public String IdModulo;
        public Long IdUsuario;
        public Long IdEmpresa;
        public String FechaSistemaLog;
        public Long IdUsuarioAtendido;
        public Long IdCliente = 0L;
        public String EstadoCliente = "";
        public String NombreCliente = "";
        public String IdTipoDocumentoIdentidad = "";
        public String IdDocumentoIdentidad = "";
        public Long IdPortafolio = 0L;
        public String EstadoPortafolio = "";
        public String IdTipoRegistro;
        public String IdRegistro = "";
        public String EstadoRegistro = "";
        public String IdTipoOperacion = "";
        public Long Folio = 0L;
        public BigDecimal Cantidad = BigDecimal.ZERO;
        public BigDecimal PrecioTasa = BigDecimal.ZERO;
        public BigDecimal Monto = BigDecimal.ZERO;
        public String IdMonedaOrigen = "";
        public String IdMonedaLiquidacion = "";
        public String TipoCambio = "";
        public String Nemo = "";
        public Long IdProceso = 0L;
        public String EstadoProceso = "";
        public String NombreProceso = "";
        public Long IdPersona = 0L;
    }

    public static class DetalleLog {
        public Long IdTrabajo;
        public Long IdCabeceraLog;
        public String SubRegistro;
        public String CampoRegistro;
        public String ValorAntiguo;
        public String ValorNuevo;
    }
}
