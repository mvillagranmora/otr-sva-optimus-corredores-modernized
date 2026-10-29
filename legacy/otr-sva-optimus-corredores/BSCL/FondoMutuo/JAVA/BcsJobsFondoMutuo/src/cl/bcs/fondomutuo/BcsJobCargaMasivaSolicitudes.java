package cl.bcs.fondomutuo;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;

import cl.bcs.fondomutuo.ProcesarCargaSolicitudesAporteRescate.EntradaProcesarCargaSolicitudesAporteRescate;
import cl.bcs.fondomutuo.ProcesarCargaSolicitudesAporteRescate.SalidaProcesarCargaSolicitudesAporteRescate;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.Log;

public class BcsJobCargaMasivaSolicitudes extends BcsJob {

    AtomicBoolean running = new AtomicBoolean(false);
    public String IdTipoPrograma = "CARGAMASIVASOLICITUDESFFMM";
    private final Log log = new Log("FM.BcsJobCargaMasivaSolicitudes");

    public boolean transaction(Connection conn) throws InterruptedException, Throwable {
        log.write("*CARGAMASIVASOLICITUDESFFMM* Inicio de Job");
        running.set(true);
       
        try{
            Long idEmpresa = 1L;
            Long idUsuario = 3L;
            String modulo = "FMM";
            String trans = "";

            log.write( "*CARGAMASIVASOLICITUDESFFMM* Obtencion de parametros");

            idEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
            idUsuario = Long.parseLong(this.getParam("IdUsuario"));
            trans = this.getParam("Transacciones");

            log.write( "*CARGAMASIVASOLICITUDESFFMM* idEmpresa: " + idEmpresa);
            log.write( "*CARGAMASIVASOLICITUDESFFMM* idUsuario: " + idUsuario);
            log.write( "*CARGAMASIVASOLICITUDESFFMM* Transacciones: " + trans);

            log.write( "*CARGAMASIVASOLICITUDESFFMM* Seteo de schema");

            

            String[] transacciones = trans.split(";");

            if (transacciones.length == 0) {
            	 log.write( "*CARGAMASIVASOLICITUDESFFMM* No se enviaron cargas para procesar");
                running.set(false);
                return false;
            }

            
            log.write( "*CARGAMASIVASOLICITUDESFFMM* Inicio de llenado de tabla de cargas");
            ProcesarCargaSolicitudesAporteRescate procesarCargaSolicitudesAporteRescate = new ProcesarCargaSolicitudesAporteRescate();
            EntradaProcesarCargaSolicitudesAporteRescate entradaProcesarCargaSolicitudesAporteRescate = new EntradaProcesarCargaSolicitudesAporteRescate();
            for (int i = 0; i < transacciones.length; i++) {
            	ProcesarCargaSolicitudesAporteRescate.InCargas carga = new ProcesarCargaSolicitudesAporteRescate.InCargas();
            	carga.IdInterno = (long) i;
            	carga.IdCarga =  Long.parseLong(transacciones[i]);
            	entradaProcesarCargaSolicitudesAporteRescate.p_IdCargas.add(carga);
            	
                log.write( "*CARGAMASIVASOLICITUDESFFMM* Se agrega transaccion a tabla de entrada: " + transacciones[i]);
            }
            if(entradaProcesarCargaSolicitudesAporteRescate.p_IdCargas.size() > 0){
            	log.write( "*CARGAMASIVASOLICITUDESFFMM* Inicio de llamada a procedimiento de cargas");
            	entradaProcesarCargaSolicitudesAporteRescate.p_IdEmpresa = idEmpresa;
            	entradaProcesarCargaSolicitudesAporteRescate.p_IdUsuario = idUsuario;
            	entradaProcesarCargaSolicitudesAporteRescate.p_IdModulo = modulo;
            	SalidaProcesarCargaSolicitudesAporteRescate salida = procesarCargaSolicitudesAporteRescate.logica(conn, entradaProcesarCargaSolicitudesAporteRescate);
            	log.write( "*CARGAMASIVASOLICITUDESFFMM* Fin ejecucion de servicio");
            	if(salida.P_OUT_ERRORESCONTADOR > 0){
            		String error = "";
					for(MensajeSalida s : salida.P_OUT_MENSAJES){
						if(MensajeSalida.MENSAJE_ERROR.equals(s.tipo)){
							error = s.descripcion;
						}
					}
					if(!error.isEmpty()){
						throw new Exception(error);
					}else{
						throw new Exception("Error al procesar carga");
					}
            	}
            }
        } catch (Exception e) {
            log.write("Error",e);
            throw e;
        } finally {
        	 running.set(false);
        }

        running.set(false);
        return true;
    }

    @Override
    public void onError(Throwable t) {
        System.out.println(":ProcesadorTransacciones:  ****** ERROR  *******");
        t.printStackTrace();
    }

    @Override
    public void pause() {

    }

    public static void main(String[] args) throws SQLException {
    	Connection con = PlataformaUtils.getDESAConnection();
        BcsJobCargaMasivaSolicitudes p = new BcsJobCargaMasivaSolicitudes();
        try {
            p.transaction(con);
            //con.commit();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Throwable e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
