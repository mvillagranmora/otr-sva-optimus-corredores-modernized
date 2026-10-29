package cl.bcs.fondomutuo;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.fondomutuo.AdministracionLiquidez.EntradaAdministracionLiquidez;
import cl.bcs.fondomutuo.AdministracionLiquidez.SalidaAdministracionLiquidez;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;

public class BcsJobAdministracionLiquidez extends BcsJob {
	static final Logger logger = LogManager.getLogger(BcsJobAdministracionLiquidez.class);
    
    public boolean transaction(Connection conn) throws InterruptedException, Throwable {
		return go(conn,false);
	}

    public boolean go(Connection dbConnection, boolean debug) throws InterruptedException, Throwable {
    	logger.info("*ADMINISTRACIONLIQUIDEZFFMM* Inicio de demonio");
        EntradaAdministracionLiquidez entradaAdm = new EntradaAdministracionLiquidez();
        if(debug){
        	entradaAdm.p_IdEmpresa = 1L;
        	entradaAdm.p_IdUsuario = 21L;
		}else{
			entradaAdm.p_IdEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
			entradaAdm.p_IdUsuario = Long.parseLong(this.getParam("IdUsuario"));
		}
        entradaAdm.p_IdModulo = "FMM";
        entradaAdm.p_SimularOperacion = "N";
        entradaAdm.p_UsaReglasNegocio = "S";
        
        AdministracionLiquidez adm = new AdministracionLiquidez();
        SalidaAdministracionLiquidez salidaAdm = adm.logica(dbConnection, entradaAdm);
        
        logger.debug(salidaAdm);
        logger.info("*ADMINISTRACIONLIQUIDEZFFMM* Fin de demonio");
        
    	return true;
    }

    @Override
    public void onError(Throwable t) {
    	logger.info(":ADMINISTRACIONLIQUIDEZFFMM:  ****** ERROR  *******");
        t.printStackTrace();
    }

    @Override
    public void pause() {

    }

    public static void main(String[] args) throws InterruptedException, Throwable {
    	System.out.println("== START ==");
		Connection con = PlataformaUtils.getMBConnection();
		try {
			BcsJobAdministracionLiquidez adm = new BcsJobAdministracionLiquidez();		
			System.out.println(adm.go(con,true));
			con.rollback();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			con.rollback();
			e.printStackTrace();
		}
		System.out.println("== END ==");
    }
}
