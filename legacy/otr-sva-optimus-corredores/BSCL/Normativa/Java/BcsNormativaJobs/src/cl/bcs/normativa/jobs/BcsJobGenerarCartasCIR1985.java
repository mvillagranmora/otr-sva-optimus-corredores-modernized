package cl.bcs.normativa.jobs;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;


import cl.bcs.normativa.GenerarCartasCIR1985;
import cl.bcs.normativa.GenerarCartasCIR1985.GenerarCartasCIR1985_Entrada;
import cl.bcs.normativa.GenerarCartasCIR1985.SalidaGenerarCartasCIR1985;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.Log;

public class BcsJobGenerarCartasCIR1985 extends BcsJob {

    AtomicBoolean running = new AtomicBoolean(false);
    public String IdTipoPrograma = "GenerarCartasCIR1985";
    private final Log log = new Log("NOR.BcsJobGenerarCartasCIR1985");


    @Override
    public boolean transaction(Connection con) throws InterruptedException, Throwable {
        log.write("*BcsJobGenerarCartasCIR1985* Inicio de Job");
        running.set(true);

        try {
        	GenerarCartasCIR1985_Entrada entrada = new GenerarCartasCIR1985_Entrada();
            entrada.p_IdEmpresa    = Long.parseLong(this.getParam("IdEmpresa"));
            entrada.p_IdUsuario    = Long.parseLong(this.getParam("IdUsuario"));
            entrada.p_IdModulo     = "NOR";
            entrada.p_FechaDesde = this.getParam("FechaSistema");
            entrada.p_FechaHasta = this.getParam("FechaSistema");
            entrada.p_IdCliente = null;
            entrada.p_TipoOrigen = "PAUTA";
            entrada.p_DataSource   = super.getDataSource();

            GenerarCartasCIR1985 generarCartas = new GenerarCartasCIR1985();
            SalidaGenerarCartasCIR1985 salida = generarCartas.logica(con, entrada);

            log.write("*BcsJobGenerarCartasCIR1985* Cartas: " + salida.p_out_ContadorCartas);
            log.write("*BcsJobGenerarCartasCIR1985* Errores: " + salida.P_OUT_ERRORESCONTADOR);

        } catch (Exception e) {
            log.write("*BcsJobGenerarCartasCIR1985* Error", e);
            throw e;
        } finally {
            running.set(false);
        }

        log.write("*BcsJobGenerarCartasCIR1985* Fin de Job");
        return true;
    }

    @Override
    public void onError(Throwable t) {
        log.write("*BcsJobGenerarCartasCIR1985* ERROR", t);
        t.printStackTrace();
    }

    @Override
    public void pause() {}

    public static void main(String[] args) throws SQLException {
        Connection con = PlataformaUtils.getDESAConnection();
        BcsJobGenerarCartasCIR1985 job = new BcsJobGenerarCartasCIR1985();
        try {
            job.transaction(con);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}