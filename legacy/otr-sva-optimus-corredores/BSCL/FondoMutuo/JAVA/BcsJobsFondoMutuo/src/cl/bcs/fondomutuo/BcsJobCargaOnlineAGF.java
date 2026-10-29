package cl.bcs.fondomutuo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;


import org.apache.commons.dbcp2.BasicDataSource;

import cl.bcs.fondomutuo.ConsultarSolicitudesParaAGF.EntradaConsultarSolicitudesParaAGF;
import cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.Log;

public class BcsJobCargaOnlineAGF extends BcsJob {

    AtomicBoolean running = new AtomicBoolean(false);
    public String IdTipoPrograma = "CARGAONLINEAGFFFMM";
    private final Log log = new Log("FM.BcsJobCargaOnlineAGF");
    private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();

    public boolean transaction(Connection dbConnection) throws InterruptedException, Throwable {
    	log.write( "*CARGAONLINEAGFFFMM* Inicio de Job");
        running.set(true);
        BasicDataSource dsTarget = new BasicDataSource();
        List<Long> listArraySolicitudes = new ArrayList<Long>();
        ResultSet rs = null;
        try{

            Long idEmpresa = 1L;
            Long idUsuario = 3L;
            String modulo = "FMM";

            idEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
            idUsuario = Long.parseLong(this.getParam("IdUsuario"));

            log.write( "*CARGAONLINEAGFFFMM* Parametro idEmpresa: " + idEmpresa);
            log.write( "*CARGAONLINEAGFFFMM* Parametro IdUsuario: " + idUsuario);

            /*
             * Obtener parametros de conexión a SQL
             */
            String targetURL = ConfiguracionSistema.getParametroConfiguracionString(dbConnection, idEmpresa, "FONDOMUTUO", "TARGET_URL_CARGA_ONLINE");
            String targetProp = ConfiguracionSistema.getParametroConfiguracionString(dbConnection, idEmpresa, "FONDOMUTUO", "TARGET_PROP_CARGA_ONLINE");
            String targetDriver = ConfiguracionSistema.getParametroConfiguracionString(dbConnection, idEmpresa, "FONDOMUTUO", "TARGET_DRIVER_CARGA_ONLINE");
            String targetQVal = ConfiguracionSistema.getParametroConfiguracionString(dbConnection, idEmpresa, "FONDOMUTUO", "TARGET_QVAL_CARGA_ONLINE");
            String targetUser = ConfiguracionSistema.getParametroConfiguracionString(dbConnection, idEmpresa, "FONDOMUTUO", "TARGET_USER_CARGA_ONLINE");
            String targetPass = ConfiguracionSistema.getParametroConfiguracionString(dbConnection, idEmpresa, "FONDOMUTUO", "TARGET_PASS_CARGA_ONLINE");
            String targetQuery = ConfiguracionSistema.getParametroConfiguracionString(dbConnection, idEmpresa, "FONDOMUTUO", "TARGET_QUERY_CARGA_ONLINE");

            log.write( "*CARGAONLINEAGFFFMM* Parametro targetDriver: " + targetDriver);
            log.write( "*CARGAONLINEAGFFFMM* Parametro targetUser: " + targetUser);
            log.write( "*CARGAONLINEAGFFFMM* Parametro targetPass: " + targetPass);
            log.write( "*CARGAONLINEAGFFFMM* Parametro targetURL: " + targetURL);
            log.write( "*CARGAONLINEAGFFFMM* Parametro targetQVal: " + targetQVal);
            log.write( "*CARGAONLINEAGFFFMM* Parametro targetProp: " + targetProp);

            dsTarget.setDriverClassName(targetDriver);
            dsTarget.setUsername(targetUser);
            dsTarget.setPassword(targetPass);
            dsTarget.setUrl(targetURL);
            dsTarget.setValidationQuery(targetQVal);
            dsTarget.setConnectionProperties(targetProp);
            dsTarget.setDefaultAutoCommit(false);

            /*
             * Fin parametros
             */
            log.write( "*CARGAONLINEAGFFFMM* Preparando llamada");
            
            ConsultarSolicitudesParaAGF consultarSolicitudesParaAGF = new ConsultarSolicitudesParaAGF();
            EntradaConsultarSolicitudesParaAGF entradaConsultarSolicitudesParaAGF = new EntradaConsultarSolicitudesParaAGF();
            entradaConsultarSolicitudesParaAGF.p_IdEmpresa = idEmpresa;
            entradaConsultarSolicitudesParaAGF.p_IdUsuario = idUsuario;
            entradaConsultarSolicitudesParaAGF.p_IdModulo = modulo;
            
            ConsultarSolicitudesParaAGF.SalidaConsultarSolicitudesParaAGF salidaConsultarSolicitudesParaAGF = consultarSolicitudesParaAGF.logica(dbConnection, entradaConsultarSolicitudesParaAGF);
            log.write( "*CARGAONLINEAGFFFMM* Se ejecuta llamada");
            log.write( "*CARGAONLINEAGFFFMM* Se lee resultado");
            boolean existenSolicitudes = false;
            try (Connection conMS = dsTarget.getConnection(); PreparedStatement stMS = conMS.prepareStatement(targetQuery)) {
            	log.write( "*CARGAONLINEAGFFFMM* Se lee cada fila y se procede a insertar en base de datos SigaFFMM");
            	for(ConsultarSolicitudesParaAGF.OutSolicitudes sol : salidaConsultarSolicitudesParaAGF.P_OUT_RESULTADO){
            		existenSolicitudes = true;
                    stMS.setString(1, "G");
                    stMS.setString(2, sol.TipoRegistro);
                    stMS.setString(3, sol.Registro);
                    stMS.setString(4, "");
                    stMS.setString(5, "I");
                    stMS.setInt(6, 0);
                    stMS.setString(7, "");
                    stMS.setString(8, "");
                    stMS.setInt(9, 0);
                    stMS.addBatch();
                    listArraySolicitudes.add(sol.IdInterfazAgf);
            	}
                if (existenSolicitudes) {
                    stMS.executeBatch();
                    conMS.commit();
                }
            } catch (Exception e) {
            	log.write("Error",e);
                throw new Throwable(e);
            }
            log.write( "*CARGAONLINEAGFFFMM* Fin de insercion a tabla SigaFFMM");
            log.write( "*CARGAONLINEAGFFFMM* Se acualizan registros a procesados");
            
            CambiarEstadoSolicitudAgf cambiarEstadoSolicitudAgf = new CambiarEstadoSolicitudAgf();
            CambiarEstadoSolicitudAgf.EntradaCambiarEstadoSolicitudAgf entradaCambiarEstadoSolicitudAgf = new CambiarEstadoSolicitudAgf.EntradaCambiarEstadoSolicitudAgf();
            entradaCambiarEstadoSolicitudAgf.p_IdEmpresa = idEmpresa;
            
            for(Long id : listArraySolicitudes){
            	CambiarEstadoSolicitudAgf.InSolicitudes sol = new CambiarEstadoSolicitudAgf.InSolicitudes();
            	sol.IdInterfazAgf = id;
            	sol.IdEstado = _c.EstadoProcesado;
            	sol.Observacion = "Sin observacion";
            	entradaCambiarEstadoSolicitudAgf.p_SolicitudesAGF.add(sol);
            }

            cambiarEstadoSolicitudAgf.logica(dbConnection, entradaCambiarEstadoSolicitudAgf);

            dbConnection.commit();
            log.write( "*CARGAONLINEAGFFFMM* Fin de Job");
        } catch (Exception e) {
        	log.write("Error",e);
            return false;
        } finally {
            try {
                if (rs != null)
                    rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
                log.write("Error", e);
                throw new Throwable(e);
            }
        }

        running.set(false);
        return true;
    }

    @Override
    public void onError(Throwable t) {
    	log.write(":ProcesadorTransacciones:  ****** ERROR  *******", t);
        t.printStackTrace();
    }

    @Override
    public void pause() {

    }

    public static void main(String[] args) {
    	
        BcsJobCargaOnlineAGF p = new BcsJobCargaOnlineAGF();
        try {
        	Connection con = PlataformaUtils.getDESAConnection();
            p.transaction(con);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Throwable e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
