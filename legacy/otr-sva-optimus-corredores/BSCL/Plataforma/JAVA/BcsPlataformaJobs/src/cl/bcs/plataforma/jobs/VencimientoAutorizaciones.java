package cl.bcs.plataforma.jobs;

import java.sql.Connection;

import cl.bcs.plataforma.aut.VencerEventosAutorizacion;
import cl.bcs.scheduler.BcsJob;

public class VencimientoAutorizaciones extends BcsJob {

    public boolean transaction(Connection dbcon) throws InterruptedException, Throwable {
        try {
            long idEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
            VencerEventosAutorizacion vencer = new VencerEventosAutorizacion();
            VencerEventosAutorizacion.ServicioEntrada srvIN = new VencerEventosAutorizacion.ServicioEntrada();
            srvIN.p_IdEmpresa = idEmpresa;
            srvIN.p_IdModulo = "PLA";
            srvIN.p_IdUsuario = new Long(1);
            VencerEventosAutorizacion.ServicioSalida srvOUT = vencer.logica(dbcon, srvIN);
            if (srvOUT.P_OUT_ERRORESCONTADOR == 0) {
                return true;
            } else {
                System.out.println(":VENCIMIENTOAUTORIZACIONES:  ERROR: Servicio con respuesta error");
                return false;
            }
        } catch (Exception er) {
            System.out.println(":VENCIMIENTOAUTORIZACIONES:  ****** ERROR NO CONTROLADO *******");
            throw er;
        }
    }

    @Override
    public void onError(Throwable t) {
    }

    @Override
    public void pause() {
    }
}
