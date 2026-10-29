package cl.bcs.cuentainversion.reportes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * JSON contract of the platform audit service ({@code cl.bcs.plataforma.pla.AgregarLog.InAgregarLog}); field names and defaults as
 * in the legacy POJO so that the REST adapter can send it unchanged.
 */
public class AgregarLogEntrada {
    public Long p_IdEmpresa;
    public Long p_IdUsuario;
    public String p_IdModulo;
    public String p_UsaReglasNegocio;
    public String p_IdEstadoAutorizacion;
    public Long p_IdTrabajo = 0L;
    public List<Log> p_Log = new ArrayList<>();
    public List<DetalleLog> p_LogDetalle = new ArrayList<>();

    /** Header row of the audit trail. */
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

        /** Header row of module CTA as the commission services write it (attended user = acting user; every other field at its default). */
        public static Log cta(String idTipoLog, String idTipoRegistro, Long idCabeceraLog, Long idTrabajo, Long idEmpresa, Long idUsuario,
                              String fechaSistema, String nombreProceso) {
            Log l = new Log();
            l.IdTrabajo = idTrabajo;
            l.IdCabeceraLog = idCabeceraLog;
            l.IdTipoLog = idTipoLog;
            l.IdModulo = "CTA";
            l.IdUsuario = idUsuario;
            l.IdEmpresa = idEmpresa;
            l.FechaSistemaLog = fechaSistema;
            l.IdUsuarioAtendido = idUsuario;
            l.IdTipoRegistro = idTipoRegistro;
            l.NombreProceso = nombreProceso;
            return l;
        }
    }

    /** Field-level change of a header row. */
    public static class DetalleLog {
        public Long IdTrabajo;
        public Long IdCabeceraLog;
        public String SubRegistro;
        public String CampoRegistro;
        public String ValorAntiguo;
        public String ValorNuevo;
    }

    /** Legacy {@code agregarLogBasico}. */
    public void agregarLogBasico(Long idEmpresa, Long idUsuario, String idModulo, Long idCabeceraLog, Long idTrabajo,
                                 String idTipoLog, String idTipoRegistro, String fechaLog, String nombreProceso) {
        Log l = new Log();
        l.IdEmpresa = idEmpresa;
        l.IdModulo = idModulo;
        l.IdUsuario = idUsuario;
        l.IdCabeceraLog = idCabeceraLog;
        l.IdTrabajo = idTrabajo;
        l.IdTipoLog = idTipoLog;
        l.IdTipoRegistro = idTipoRegistro;
        l.FechaSistemaLog = fechaLog;
        l.NombreProceso = nombreProceso;
        p_Log.add(l);
    }

    /** Legacy {@code agregarDetalleLogBasico}. */
    public void agregarDetalleLogBasico(Long idCabeceraLog, Long idTrabajo, String campoRegistro, String valorAntiguo, String valorNuevo) {
        DetalleLog d = new DetalleLog();
        d.IdCabeceraLog = idCabeceraLog;
        d.IdTrabajo = idTrabajo;
        d.CampoRegistro = campoRegistro;
        d.ValorAntiguo = valorAntiguo;
        d.ValorNuevo = valorNuevo;
        p_LogDetalle.add(d);
    }

    public void agregarDetalleLogBasico(Long idCabeceraLog, Long idTrabajo, String campoRegistro, String valorAntiguo, String valorNuevo,
                                        String subRegistro) {
        DetalleLog d = new DetalleLog();
        d.IdCabeceraLog = idCabeceraLog;
        d.IdTrabajo = idTrabajo;
        d.CampoRegistro = campoRegistro;
        d.ValorAntiguo = valorAntiguo;
        d.ValorNuevo = valorNuevo;
        d.SubRegistro = subRegistro;
        p_LogDetalle.add(d);
    }
}
