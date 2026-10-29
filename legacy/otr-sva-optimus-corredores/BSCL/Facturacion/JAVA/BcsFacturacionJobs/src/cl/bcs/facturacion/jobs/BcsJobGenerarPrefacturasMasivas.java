package cl.bcs.facturacion.jobs;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.gson.JsonObject;

import cl.bcs.facturacion.GenerarInformePrefacturas;
import cl.bcs.facturacion.GenerarInformePrefacturas.EntradaGenerarInformePrefacturas;
import cl.bcs.facturacion.GenerarInformePrefacturas.SalidaGenerarInformePrefacturas;
import cl.bcs.facturacion.GenerarInformePrefacturas.ProgramaBatch;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.JavaCall;

public class BcsJobGenerarPrefacturasMasivas extends BcsJob implements JavaCall {

    private final String SQL_UPDATE_RUTA_SALIDA = loadResourceToString("cl/bcs/facturacion/jobs/sql/GenerarPrefacturasMasivas_UpdateRutaSalida.sql");

    AtomicBoolean running = new AtomicBoolean(false);
    public String IdTipoPrograma = "GenerarInformePrefacturas";
    private final Log log = new Log("FM.BcsJobGenerarInformePrefacturas");

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

    @Override
    public boolean transaction(Connection con) throws InterruptedException, Throwable {
        log.write("*BcsJobGenerarInformePrefacturas* Inicio de Job");
        running.set(true);

        try {
            EntradaGenerarInformePrefacturas entrada = new EntradaGenerarInformePrefacturas();
            entrada.p_IdEmpresa        = Long.parseLong(this.getParam("IdEmpresa"));
            entrada.p_IdUsuario        = Long.parseLong(this.getParam("IdUsuario"));
            entrada.p_FechaComprobante = this.getParam("FechaComprobante");
            entrada.p_FormatoSalida    = this.getParam("FormatoSalida");
            entrada.p_IdProceso        = this.getJobId();
            entrada.p_DataSource       = super.getDataSource();
            entrada.p_ExportarTodo     = "true".equalsIgnoreCase(this.getParam("ExportarTodo"));

            String idPortafolioParam = this.getParam("IdPortafolio");
            entrada.p_ListaIdPortafolio = new ArrayList<>();
            if (idPortafolioParam != null && !idPortafolioParam.isEmpty()) {
                for (String id : idPortafolioParam.split(",")) {
                    entrada.p_ListaIdPortafolio.add(Long.parseLong(id.trim()));
                }
            }

            log.write("*BcsJobGenerarInformePrefacturas* Parametros:");
            log.write("  IdEmpresa     : " + entrada.p_IdEmpresa);
            log.write("  IdUsuario     : " + entrada.p_IdUsuario);
            log.write("  FechaComprobante: " + entrada.p_FechaComprobante);
            log.write("  FormatoSalida : " + entrada.p_FormatoSalida);
            log.write("  IdPortafolio  : " + entrada.p_ListaIdPortafolio);

            GenerarInformePrefacturas generador = new GenerarInformePrefacturas();
            SalidaGenerarInformePrefacturas salida = generador.logica(con, entrada);

            log.write("*BcsJobGenerarInformePrefacturas* Errores: " + salida.P_OUT_ERRORESCONTADOR);

            if (salida.p_out_RutaSalida != null && !salida.p_out_RutaSalida.isEmpty()) {
                log.write("*BcsJobGenerarInformePrefacturas* Actualizando ruta: " + salida.p_out_RutaSalida);
                SQL sql = new SQL();
                List<ProgramaBatch> batch = new ArrayList<>();
                batch.add(new ProgramaBatch(this.getJobId(), salida.p_out_RutaSalida));
                sql.batch(con, SQL_UPDATE_RUTA_SALIDA, batch, ProgramaBatch.class);
            }

        } catch (Exception e) {
            log.write("*BcsJobGenerarInformePrefacturas* Error", e);
            throw e;
        } finally {
            running.set(false);
        }

        log.write("*BcsJobGenerarInformePrefacturas* Fin de Job");
        return true;
    }

    @Override
    public void onError(Throwable t) {
        log.write("*BcsJobGenerarInformePrefacturas* ERROR", t);
        t.printStackTrace();
    }

    @Override
    public void pause() {}

    public static void main(String[] args) throws SQLException {
        Connection con = PlataformaUtils.getDESAConnection();
        BcsJobGenerarPrefacturasMasivas job = new BcsJobGenerarPrefacturasMasivas();
        try {
            job.transaction(con);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}