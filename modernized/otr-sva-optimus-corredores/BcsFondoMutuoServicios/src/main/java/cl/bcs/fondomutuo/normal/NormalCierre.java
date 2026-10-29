package cl.bcs.fondomutuo.normal;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import cl.bcs.fondomutuo.comisiones.CalcularComisiones.ResultadoCalcularComision;
import cl.bcs.fondomutuo.comisionnn.ComisionNnContrato;
import cl.bcs.fondomutuo.comisionnn.CrearSolicitudRescateComisionNnService;
import cl.bcs.fondomutuo.comisionnn.SolicitudAporteRescate;
import cl.bcs.fondomutuo.kit.MensajeSalida;
import cl.bcs.fondomutuo.port.out.http.NotificacionOwpPort;

/** The last part of Normal: the quarterly-commission rescues (legacy :1614-1662) and the OpenWorkPlace notification (legacy :1665-1708). */
final class NormalCierre {

    private final CrearSolicitudRescateComisionNnService comisionNn;
    private final NormalDatos datos;
    private final NotificacionOwpPort owp;

    NormalCierre(CrearSolicitudRescateComisionNnService comisionNn, NormalDatos datos, NotificacionOwpPort owp) {
        this.comisionNn = comisionNn;
        this.datos = datos;
        this.owp = owp;
    }

    /**
     * D-NOR-6: the commission rescue runs with {@code p_SimularOperacion = "N"} fixed, also while the Manejador simulates.
     * @return false when it failed (the caller releases the lock and answers with the messages)
     */
    boolean comisionNn(NormalContexto c) throws Exception {
        ComisionNnContrato.Entrada e = new ComisionNnContrato.Entrada();
        long iNN = 1L;
        e.p_IdEmpresa = c.entrada.p_IdEmpresa;
        e.p_IdModulo = c.entrada.p_IdModulo;
        e.p_IdTipoCanalIngreso = "";
        e.p_IdUsuario = c.entrada.p_IdUsuario;
        e.p_SimularOperacion = "N";
        e.p_UsaReglasNegocio = "S";
        for (SolicitudAporteRescate sal : c.solicitudesProcesar) {
            ResultadoCalcularComision comision = c.hcomisiones.get(sal.IdInterno);
            if (comision != null && comision.Comision.compareTo(BigDecimal.ZERO) > 0 && comision.TipoComision != null
                    && comision.TipoComision.equalsIgnoreCase(NormalConstantes.TIPO_COMISION_TRIMESTRAL)
                    && comision.CantidadComision.compareTo(BigDecimal.ZERO) > 0 && NormalConstantes.TIPO_SOLICITUD_RESCATE.equals(sal.TipoSolicitud)) {
                ComisionNnContrato.InSolicitudesRescate in = new ComisionNnContrato.InSolicitudesRescate();
                in.FolioContrato = 0L;
                in.IdCliente = sal.IdCliente;
                in.IdInterno = iNN++;
                in.IdMoneda = sal.IdMoneda;
                in.IdPortafolio = sal.IdPortafolio;
                in.IdRegimenTributario = sal.IdRegimenTributario;
                in.IdSerieFondo = sal.IdSerieFondo;
                in.IngresoAutomatico = "N";
                in.Monto = new BigDecimal(1);
                in.NumeroCuotas = comision.CantidadComision;
                in.FechaValorCuota = sal.FechaValorCuota;
                in.FolioCartera = 0L;
                in.IdSolicitudAporteRescateRelacionado = sal.IdSolicitudAporteRescate;
                in.IdMonedaPago = sal.IdMonedaPago;
                e.p_SolicitudesRescate.add(in);
            }
        }
        if (!e.p_SolicitudesRescate.isEmpty()) {
            ComisionNnContrato.Salida salida = comisionNn.logica(e);
            if (salida.P_OUT_ERRORESCONTADOR > 0L) {
                c.out.P_OUT_MENSAJES.addAll(salida.P_OUT_MENSAJES);
                c.out.P_OUT_ERRORESCONTADOR = c.out.P_OUT_MENSAJES.stream().filter(m -> MensajeSalida.MENSAJE_ERROR.equals(m.tipo)).count();
                return false;
            }
            if (!salida.P_OUT_RESULTADO.isEmpty()) {
                c.out.P_OUT_RESULTADO.addAll(salida.P_OUT_RESULTADO);
            }
        }
        return true;
    }

    void notificarOwp(NormalContexto c, String notifica) {
        if ("S".equalsIgnoreCase(notifica) && "OWP".equalsIgnoreCase(c.entrada.p_IdTipoCanalIngreso)) {
            List<Long> idSolicitudes = new ArrayList<>();
            for (SolicitudAporteRescate in : c.solicitudesInsertar) {
                if (!idSolicitudes.contains(in.IdSolicitudAporteRescate)) idSolicitudes.add(in.IdSolicitudAporteRescate);
            }
            List<NormalContratos.NotificarOrden> lista = new ArrayList<>();
            for (SolicitudAporteRescate s : datos.solicitudes(c.entrada.p_IdEmpresa, idSolicitudes)) {
                NormalContratos.NotificarOrden n = new NormalContratos.NotificarOrden();
                n.IdEmpresa = c.entrada.p_IdEmpresa;
                n.IdUsuario = c.entrada.p_IdUsuario;
                n.Solsec = s.IdSolicitudAporteRescate;
                n.NTitulos = s.NumeroCuotas;
                n.Precio = s.ValorCuota;
                n.Fecha = s.FechaAceptacion.toString();
                n.Estado = s.Estado;
                n.Comision = s.MontoComision;
                n.Gastos = new BigDecimal(0);
                n.Mercado = "F";
                n.Divisa = s.IdMoneda;
                n.TipoCambio = s.CostoMonedaLocal;
                n.SolsecAsoc = s.IdSolicitudAporteRescateRelacionado.toString();
                n.Nemo = s.IdSerieFondo;
                n.IdClasificacionTipoInstrumento = "FM";
                lista.add(n);
            }
            if (lista.size() > 0) {
                NormalContratos.notificar(owp, lista);
            }
        }
    }
}
