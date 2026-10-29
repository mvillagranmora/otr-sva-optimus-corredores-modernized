package cl.bcs.normativa.jobs;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.fondomutuo.jobs.GenerarArchivoSolicitudAporteRescateJob.ProgramaBatch;
import cl.bcs.normativa.GenerarReporteAPV;
import cl.bcs.normativa.GenerarReporteAPV.EntradaGenerarReporteAPV;
import cl.bcs.normativa.GenerarReporteAPV.SalidaGenerarReporteAPV;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;;

public class BcsJobGenerarReporteAPV extends BcsJob implements JavaCall {

	private final static Logger logger = LogManager.getLogger(BcsJobGenerarReporteAPV.class);
	private final String SQL_UPDATE_SALIDA_PROGRAMA_BATCH = "UPDATE \"BSCL.Plataforma.BD::BAT.Programa\" SET \"RutaSalida\" = :RutaArchivo  WHERE \"IdPrograma\" = :IdPrograma ";

	@Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
		return logica(con, false);
	}

	public boolean logica(Connection con, boolean debug) throws InterruptedException, Throwable {
		logger.info("INICIO::BcsJobGenerarReporteAPV.logica");
		try {

			Long idPrograma;
			Long idEmpresa = 0L;
			Long idUsuario = 0L;
			String fechaCierre = null;
			String rutaArchivo = "";

			if (debug) {
				idPrograma = 1213222L;
				idEmpresa = 1L;
				idUsuario = 1L;
				fechaCierre = "2019-07-31";
				rutaArchivo = "C:\\\\\\\\ARCHIVO_PRUEBA\\\\\\\\\\\",\\r\\n";
			} else {
				idPrograma = this.getJobId();
				idEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
				idUsuario = Long.parseLong(this.getParam("IdUsuario"));
				fechaCierre = this.getParam("FechaCierre");
				rutaArchivo = Utils.getConfiguracion(con, Utils.getSchema(), idEmpresa, "PLA.RutaBaseArchivos");
			}

			GenerarReporteAPV generarReporteAPV = new GenerarReporteAPV();
			EntradaGenerarReporteAPV entradaReporte = new EntradaGenerarReporteAPV();
			
			entradaReporte.p_IdModulo = "NOR";
			entradaReporte.p_IdEmpresa = idEmpresa;
			entradaReporte.p_IdUsuario = idUsuario;
			entradaReporte.p_Ruta = rutaArchivo;
			entradaReporte.p_FechaCierre = fechaCierre;
			entradaReporte.p_FormatoArchivo = "TXT";

			SalidaGenerarReporteAPV salidaGenerarReporteAPV = generarReporteAPV.logica(con, entradaReporte);
			
			if(salidaGenerarReporteAPV.P_OUT_ERRORESCONTADOR.compareTo(0L) == 0) {
				SQL sql = new SQL();
				List<ProgramaBatch> programaBatch = new ArrayList<>();
				programaBatch.add(new ProgramaBatch(idPrograma, salidaGenerarReporteAPV.P_OUT_RUTA_ARCHIVO));
				sql.batch(con, SQL_UPDATE_SALIDA_PROGRAMA_BATCH, programaBatch, ProgramaBatch.class);
				con.commit();
				logger.info("FIN::BcsJobGenerarReporteAPV.logica");
				return true;
			}else {
				logger.info("FIN::BcsJobGenerarReporteAPV.logica");
				return false;
			}
		} catch (Exception err) {
			logger.error("***** ERROR BcsJobGenerarReporteAPV ******", err);
			throw err;
		}
	}

	@Override
	public void onError(Throwable t) {
		// TODO Auto-generated method stub
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		return null;
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
	}

	public static void main(String[] args) {
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			// ---------------------------------
			try {
				BcsJobGenerarReporteAPV demo = new BcsJobGenerarReporteAPV();
				System.out.println(demo.logica(con, true));
				con.commit();
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}
}
