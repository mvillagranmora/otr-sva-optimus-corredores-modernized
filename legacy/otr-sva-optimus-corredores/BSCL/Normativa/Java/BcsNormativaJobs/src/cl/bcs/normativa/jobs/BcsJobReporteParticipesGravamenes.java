package cl.bcs.normativa.jobs;

import java.sql.Connection;
import javax.sql.DataSource;
import cl.bcs.normativa.GenerarReporteParticipesGravamenes;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.Log;
import cl.bcs.utils.Utils;

public class BcsJobReporteParticipesGravamenes extends BcsJob {
  
  private final Log log = new Log(BcsJobReporteParticipesGravamenes.class.getName());

  @Override
  public boolean transaction(Connection con) throws InterruptedException, Throwable {
    Long idEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
    Long idUsuario = Long.parseLong(this.getParam("IdUsuario"));    
    String registroDate = this.getParam("registroDate");
    String idFondo = this.getParam("idFondo");
    String idSerie = this.getParam("idSerie");
    Long idCliente = Long.parseLong(this.getParam("idCliente"));
    String reportType = this.getParam("reportType");
    Long idPrograma = this.getJobId();
    String ruta = Utils.getConfiguracion(con, Utils.getSchema(), idEmpresa, "PLA.RutaBaseArchivos");
    DataSource datasource = super.getDataSource();

    try{
      GenerarReporteParticipesGravamenes generarReporteParticipesGravamenes = new GenerarReporteParticipesGravamenes();
      GenerarReporteParticipesGravamenes.Entrada entrada = new GenerarReporteParticipesGravamenes.Entrada();
      entrada.p_IdEmpresa = idEmpresa;
      entrada.p_IdUsuario = idUsuario;
      entrada.p_registroDate = registroDate;
      entrada.p_IdFondo = idFondo;
      entrada.p_IdSerie = idSerie;
      entrada.p_IdCliente = idCliente;
      entrada.reportType = reportType;
      entrada.p_IdPrograma = idPrograma;
      entrada.p_Ruta = ruta;
      entrada.p_Datasource = datasource;
      
      GenerarReporteParticipesGravamenes.Salida salida = generarReporteParticipesGravamenes.logica(con, entrada);
      if(new Long(0).compareTo(salida.P_OUT_ERRORESCONTADOR) == 0) {
        con.commit();
      } else {
        con.rollback();
      }
      return true;
    }catch (Exception e) {
      log.write(e);
      con.rollback();
      throw new Throwable(e);
    }
  }

  @Override
  public void onError(Throwable t) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void pause() {
    // TODO Auto-generated method stub
    
  }

}
