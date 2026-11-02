package cl.bcs.fondomutuo.certificacion;

import static cl.bcs.fondomutuo.certificacion.CerConstantes.SI;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.Entrada;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.Estado;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.LogFila;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.ResultadoCertificacion;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.SolicitudProceso;
import cl.bcs.fondomutuo.port.out.kit.DiccionarioMensajesPort;
import cl.bcs.fondomutuo.requisitos.SecuenciaPort;

/**
 * Legacy {@code procesarSalida} (C:1580-1611) and {@code insertarLog} (C:1613-1643) of {@code CertificarSolicitudAporteRescate}: the output element of every solicitud (rejected, warned and
 * approved) and the rows of the log of certification. Shared by the early exit of F-A (FM-CER-14) and by the closing of front J (FM-CER-66, 67). No state.
 */
public final class CertificarAporteRescateSalida {

    private static final String SECUENCIA_LOG = "BSCL.FondoMutuo.BD.Secuencias::LogCertificacionAporteRescate";

    private final DiccionarioMensajesPort diccionario;
    private final SecuenciaPort secuencias;
    private final CertificarAporteRescateEscritura escritura;

    public CertificarAporteRescateSalida(DiccionarioMensajesPort diccionario, SecuenciaPort secuencias, CertificarAporteRescateEscritura escritura) {
        this.diccionario = diccionario;
        this.secuencias = secuencias;
        this.escritura = escritura;
    }

    public List<ResultadoCertificacion> procesarSalida(Entrada entrada, List<SolicitudProceso> solicitudes, Map<String, Estado> hestado) {
        List<ResultadoCertificacion> resultados = new ArrayList<>();
        for (SolicitudProceso solicitud : solicitudes) {
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("IdComprobante", String.valueOf(solicitud.IdComprobanteTesoreria));
            params.put("IdSolicitudAporteRescateRelacionado", String.valueOf(solicitud.IdSolicitudAporteRescateRelacionado));
            params.put("IdSolicitudAporteRescate", String.valueOf(solicitud.IdSolicitudAporteRescate));
            params.put("DocumentoIdentidad", solicitud.DocumentoIdentidad);
            params.put("CodigoPortafolio", solicitud.CodigoPortafolio);

            Estado estado = hestado.get(solicitud.EstadoProceso);
            ResultadoCertificacion r = new ResultadoCertificacion();
            r.IdSolicitudAporteRescate = solicitud.IdSolicitudAporteRescate;
            r.IdSolicitudAporteRescateRelacionado = solicitud.IdSolicitudAporteRescateRelacionado;
            r.IdSerieFondo = solicitud.IdSerieFondo;
            r.Codigo = solicitud.Codigo;
            r.Estado = solicitud.EstadoProceso;
            r.NombreEstado = estado.Nombre;   // legacy: a state that is not in PLA.Estado is a NullPointerException (FM-CER-67)
            r.DocumentoIdentidad = solicitud.DocumentoIdentidad;
            r.CodigoPortafolio = solicitud.CodigoPortafolio;
            r.DescripcionCodigo = solicitud.DescripcionCodigo.isEmpty() ? obtenerDescripcionMensajeSalida(entrada.p_IdEmpresa, solicitud.Codigo, params) : solicitud.DescripcionCodigo;

            if (SI.equals(entrada.p_CertificacionAporteSwitchSimulado)) {
                r.IdSolicitudAporteRescate = 0L;
            }
            resultados.add(r);
        }
        return resultados;
    }

    private String obtenerDescripcionMensajeSalida(Long idEmpresa, String codigo, Map<String, String> parametrosMensaje) {
        String descripcion = "";
        try {
            descripcion = diccionario.mensaje(idEmpresa, codigo, parametrosMensaje);
        } catch (Exception e) {
            // legacy: the text stays empty
        }
        return descripcion;
    }

    public void insertarLog(List<SolicitudProceso> sol, Entrada entrada, String fechaSistema) {
        List<LogFila> filas = new ArrayList<>();
        for (SolicitudProceso s : sol) {
            LogFila l = new LogFila();
            l.IdSolicitudAporteRescate = s.IdSolicitudAporteRescate;
            l.Codigo = s.Codigo;
            l.IdEstado = s.EstadoProceso;
            l.TipoSolicitud = s.TipoSolicitud;
            l.IdTipoMovimientoFondoMutuo = s.IdTipoMovimientoFondoMutuo;
            l.IdUsuario = entrada.p_IdUsuario;
            l.IdEmpresa = entrada.p_IdEmpresa;
            l.FechaSistema = fechaSistema;
            l.IdSerieFondo = s.IdSerieFondo;
            l.IdEmisor = s.IdEmisor;
            l.Monto = s.Monto;
            l.IdMoneda = s.IdMonedaFondo;
            l.MontoOriginal = s.CostoMonedaLocal;
            l.TipoCambio = s.ParidadFechaCertificacion;
            l.IdMonedaOriginal = s.IdMoneda;
            l.ValorCuota = s.ValorCuota != null ? s.ValorCuota : BigDecimal.ZERO;
            l.NumeroCuotas = s.NumeroCuotas != null ? s.NumeroCuotas : BigDecimal.ZERO;
            l.IdCliente = s.IdCliente;
            l.IdPortafolio = s.IdPortafolio;
            l.Simulacion = entrada.p_SimularOperacion;
            filas.add(l);
        }
        List<Long> idsLog = secuencias.siguientes(SECUENCIA_LOG, (long) filas.size());
        for (int i = 0; i < filas.size(); i++) {
            filas.get(i).IdLogCertificacionAporteRescate = idsLog.get(i);
        }
        escritura.insertarLog(filas);
    }
}
