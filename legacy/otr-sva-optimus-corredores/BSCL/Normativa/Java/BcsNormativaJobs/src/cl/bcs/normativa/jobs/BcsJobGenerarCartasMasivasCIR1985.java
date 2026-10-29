package cl.bcs.normativa.jobs;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import cl.bcs.normativa.GenerarCartasCIR1985;
import cl.bcs.normativa.GenerarCartasCIR1985.GenerarCartasCIR1985_Entrada;
import cl.bcs.normativa.GenerarCartasCIR1985.SalidaGenerarCartasCIR1985;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.Log;

public class BcsJobGenerarCartasMasivasCIR1985 extends BcsJob {

    AtomicBoolean running = new AtomicBoolean(false);
    public String IdTipoPrograma = "GenerarCartasMasivasCIR1985";
    private final Log log = new Log("NOR.BcsJobGenerarCartasMasivasCIR1985");


    @Override
    public boolean transaction(Connection con) throws InterruptedException, Throwable {
        log.write("*BcsJobGenerarCartasMasivasCIR1985* Inicio de Job");
        running.set(true);

        try {
            GenerarCartasCIR1985_Entrada entrada = new GenerarCartasCIR1985_Entrada();
            entrada.p_IdEmpresa  = Long.parseLong(this.getParam("IdEmpresa"));
            entrada.p_IdUsuario  = Long.parseLong(this.getParam("IdUsuario"));
            entrada.p_IdModulo   = "NOR";
            entrada.p_FechaDesde = this.getParam("FechaDesde");
            entrada.p_FechaHasta = this.getParam("FechaHasta");
            entrada.p_TipoOrigen = "MANUAL";
            entrada.p_DataSource = super.getDataSource();
            entrada.p_IdPrograma = this.getJobId();

            // IdClientes es opcional (lista de ids separados por coma, resuelta desde el filtro de la grilla)
            String idClientesParam = this.getParam("IdClientes");
            entrada.p_IdClientes = new ArrayList<>();
            if (idClientesParam != null && !idClientesParam.isEmpty()) {
                for (String id : idClientesParam.split(",")) {
                    entrada.p_IdClientes.add(Long.parseLong(id.trim()));
                }
            }

            GenerarCartasCIR1985 generarCartas = new GenerarCartasCIR1985();
            SalidaGenerarCartasCIR1985 salida = generarCartas.logica(con, entrada);

            log.write("*BcsJobGenerarCartasMasivasCIR1985* Cartas: " + salida.p_out_ContadorCartas);
            log.write("*BcsJobGenerarCartasMasivasCIR1985* Errores: " + salida.P_OUT_ERRORESCONTADOR);

            // Actualizar ruta del ZIP en BD
            if (salida.p_out_RutaCartas != null && !salida.p_out_RutaCartas.isEmpty()) {
                log.write("*BcsJobGenerarCartasMasivasCIR1985* Ruta ZIP: " + salida.p_out_RutaCartas);
                BcsJob.updateRutaSalida(con, salida.p_out_RutaCartas, this.getJobId());
            }

        } catch (Exception e) {
            log.write("*BcsJobGenerarCartasMasivasCIR1985* Error", e);
            throw e;
        } finally {
            running.set(false);
        }

        log.write("*BcsJobGenerarCartasMasivasCIR1985* Fin de Job");
        return true;
    }

    @Override
    public void onError(Throwable t) {
        log.write("*BcsJobGenerarCartasMasivasCIR1985* ERROR", t);
        t.printStackTrace();
    }

    @Override
    public void pause() {}

}