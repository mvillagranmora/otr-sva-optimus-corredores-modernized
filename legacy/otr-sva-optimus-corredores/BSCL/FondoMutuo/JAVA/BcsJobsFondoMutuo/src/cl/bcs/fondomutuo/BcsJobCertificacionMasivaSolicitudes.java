package cl.bcs.fondomutuo;

import java.sql.Connection;
import java.util.concurrent.atomic.AtomicBoolean;

import cl.bcs.fondomutuo.CertificarPorFiltrosSolicitudAporteRescate.EntradaCertificarPorFiltrosSolicitudAporteRescate;
import cl.bcs.fondomutuo.CertificarPorFiltrosSolicitudAporteRescate.SalidaCertificarPorFiltrosSolicitudAporteRescate;
import cl.bcs.fondomutuo.CertificarSolicitudAporteRescate.EntradaCertificarSolicitudAporteRescate;
import cl.bcs.fondomutuo.CertificarSolicitudAporteRescate.SalidaCertificarSolicitudAporteRescate;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.Log;

public class BcsJobCertificacionMasivaSolicitudes extends BcsJob {

    AtomicBoolean running = new AtomicBoolean(false);
    public String IdTipoPrograma = "CERTIFICACIONMASIVASOLICITUDESFFMM";
    private final Log log = new Log("FM.BcsJobCertificacionMasivaSolicitudes");

    public boolean transaction(Connection conn) throws InterruptedException, Throwable {
        log.write( "*CERTIFICACIONMASIVASOLICITUDESFFMM* Inicio de Job");
        running.set(true);
        try {

            Long idEmpresa = 1L;
            Long idUsuario = 3L;
            String modulo = "FMM";
            String simularOperacion = "";
            String usaReglasNegocio = "";
            String fechaCertificacion = "";

            String idInternoFiltro = "";
            String idEmisor = "";
            String idMoneda = "";
            String idSerieFondo = "";
            String nacional = "";
            String tipoSolicitud = "";
            String idTipoMovimientoFondoMutuo = "";
            String moneyMarket = "";
            String idCarga = "";
            String esMasivo = "";
            String idSolicitudes = "";

            log.write( "*CERTIFICACIONMASIVASOLICITUDESFFMM* Lectura de parametros");
            idEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
            idUsuario = Long.parseLong(this.getParam("IdUsuario"));
            simularOperacion = this.getParam("SimularOperacion");
            usaReglasNegocio = this.getParam("UsaReglasNegocio");
            fechaCertificacion = this.getParam("FechaCertificacion");

            idInternoFiltro = this.getParam("IdInternoFiltro");
            idEmisor = this.getParam("IdEmisor");
            idMoneda = this.getParam("IdMoneda");
            idSerieFondo = this.getParam("IdSerieFondo");
            nacional = this.getParam("Nacional");
            tipoSolicitud = this.getParam("TipoSolicitud");
            idTipoMovimientoFondoMutuo = this.getParam("IdTipoMovimientoFondoMutuo");
            moneyMarket = this.getParam("MoneyMarket");
            idCarga = this.getParam("IdCarga");
            esMasivo = this.getParam("EsMasivo");
            idSolicitudes = this.getParam("IdSolicitud");
            
            String[] idInternoFiltroArray = idInternoFiltro.split(";");
            String[] idEmisorArray = idEmisor.split(";");
            String[] idMonedaArray = idMoneda.split(";");
            String[] idSerieFondoArray = idSerieFondo.split(";");
            String[] nacionalArray = nacional.split(";");
            String[] tipoSolicitudArray = tipoSolicitud.split(";");
            String[] idTipoMovimientoFondoMutuoArray = idTipoMovimientoFondoMutuo.split(";");
            String[] moneyMarketArray = moneyMarket.split(";");
            String[] idCargaArray = idCarga.split(";");
            String[] idSolicitudesArray = idSolicitudes.split(";");

            if (esMasivo.equalsIgnoreCase("S")) {
                if (idInternoFiltroArray.length == 0) {
                    running.set(false);
                    return false;
                }
                
                log.write( "*CERTIFICACIONMASIVASOLICITUDESFFMM* Se insertan filtros");
                CertificarPorFiltrosSolicitudAporteRescate certificarPorFiltrosSolicitudAporteRescate = new CertificarPorFiltrosSolicitudAporteRescate();
    			EntradaCertificarPorFiltrosSolicitudAporteRescate entradaCertificarPorFiltrosSolicitudAporteRescate = new EntradaCertificarPorFiltrosSolicitudAporteRescate();
    			
                for (int i = 0; i < idInternoFiltroArray.length; i++) {
                    CertificarPorFiltrosSolicitudAporteRescate.InFiltros f = new CertificarPorFiltrosSolicitudAporteRescate.InFiltros();
                    f.IdEmisor = idEmisorArray[i];
    				f.IdMoneda = idMonedaArray[i];
    				f.IdSerieFondo = idSerieFondoArray[i];
    				f.Nacional = nacionalArray[i];
    				f.TipoSolicitud = tipoSolicitudArray[i];
    				f.IdTipoMovimientoFondoMutuo = idTipoMovimientoFondoMutuoArray[i];
    				f.MoneyMarket = moneyMarketArray[i];
    				f.IdCarga = Long.parseLong(idCargaArray[i]);
    				entradaCertificarPorFiltrosSolicitudAporteRescate.p_Filtros.add(f);
                }

                log.write( "*CERTIFICACIONMASIVASOLICITUDESFFMM* Se prepara llamada a servicio certificacion");
                
                entradaCertificarPorFiltrosSolicitudAporteRescate.p_IdEmpresa = idEmpresa;
				entradaCertificarPorFiltrosSolicitudAporteRescate.p_IdModulo = modulo;
				entradaCertificarPorFiltrosSolicitudAporteRescate.p_IdUsuario = idUsuario;
				entradaCertificarPorFiltrosSolicitudAporteRescate.p_SimularOperacion = simularOperacion;
				entradaCertificarPorFiltrosSolicitudAporteRescate.p_UsaReglasNegocio = usaReglasNegocio;
				entradaCertificarPorFiltrosSolicitudAporteRescate.p_FechaCertificacion = fechaCertificacion;
				
				log.write( "*CERTIFICACIONMASIVASOLICITUDESFFMM* Se ejecuta llamada");
				
				if(entradaCertificarPorFiltrosSolicitudAporteRescate.p_Filtros.size() > 0){
					SalidaCertificarPorFiltrosSolicitudAporteRescate salidaCertificarPorFiltrosSolicitudAporteRescate = certificarPorFiltrosSolicitudAporteRescate.logica(conn, entradaCertificarPorFiltrosSolicitudAporteRescate);
	                
					if(salidaCertificarPorFiltrosSolicitudAporteRescate.P_OUT_ERRORESCONTADOR > 0){
						running.set(false);
						String error = "";
						for(MensajeSalida s : salidaCertificarPorFiltrosSolicitudAporteRescate.P_OUT_MENSAJES){
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
				
				
            } else {
            	if (idSolicitudesArray.length == 0) {
                    running.set(false);
                    return false;
                }
            	
            	log.write( "*CERTIFICACIONMASIVASOLICITUDESFFMM* Se prepara llamada a servicio de certificacion");
            	
            	CertificarSolicitudAporteRescate certificar = new CertificarSolicitudAporteRescate();
        		EntradaCertificarSolicitudAporteRescate entradaCertificarSolicitudAporteRescate = new EntradaCertificarSolicitudAporteRescate();
        		entradaCertificarSolicitudAporteRescate.p_IdEmpresa = idEmpresa;
        		entradaCertificarSolicitudAporteRescate.p_IdUsuario = idUsuario;
        		entradaCertificarSolicitudAporteRescate.p_IdModulo = modulo;
        		entradaCertificarSolicitudAporteRescate.p_SimularOperacion = simularOperacion;
        		entradaCertificarSolicitudAporteRescate.p_UsaReglasNegocio = usaReglasNegocio;
        		entradaCertificarSolicitudAporteRescate.p_FechaCertificacion = fechaCertificacion;
        		
        		for (int i = 0; i < idSolicitudesArray.length; i++) {
                	CertificarSolicitudAporteRescate.InSolicitudes inSolicitud = new CertificarSolicitudAporteRescate.InSolicitudes();
                	inSolicitud.IdSolicitudAporteRescate = Long.parseLong(idSolicitudesArray[i]);
                	entradaCertificarSolicitudAporteRescate.p_Solicitudes.add(inSolicitud);
                }
        		
        		log.write( "*CERTIFICACIONMASIVASOLICITUDESFFMM* Se ejecuta llamada");
        		
        		if(entradaCertificarSolicitudAporteRescate.p_Solicitudes.size() > 0){
        			SalidaCertificarSolicitudAporteRescate salida = certificar.logica(conn, entradaCertificarSolicitudAporteRescate);
            		
            		if(salida.P_OUT_ERRORESCONTADOR > 0){
            			running.set(false);
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
            } 
        } catch (Exception e) {
            log.write("Error",e);
            throw e;
        } finally {
        	running.set(false);
        }
        
        log.write( "*CERTIFICACIONMASIVASOLICITUDESFFMM* Fin del job");
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

    public static void main(String[] args) {

        BcsJobCertificacionMasivaSolicitudes p = new BcsJobCertificacionMasivaSolicitudes();
        try {
            p.transaction(null);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Throwable e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
