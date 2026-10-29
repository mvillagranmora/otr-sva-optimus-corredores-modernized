package cl.bcs.contabilidad.jobs;

import java.sql.Connection;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.gson.JsonObject;

import cl.bcs.contabilidad.GenerarInformeClienteRelacionado;
import cl.bcs.contabilidad.GenerarInformeClienteRelacionado.GenerarInformeClienteRelacionado_IN;
import cl.bcs.contabilidad.GenerarInformeClienteRelacionado.GenerarInformeClienteRelacionado_OUT;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.JavaCall;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GenerarInformeClienteRelacionadoBatch extends BcsJob implements JavaCall {
	AtomicBoolean running = new AtomicBoolean(false);

	private static final Logger logger = LogManager.getLogger(GenerarInformeClienteRelacionadoBatch.class);

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		return null;
	}

	@Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
		return logica(con, false);
	}
	
	public boolean logica(Connection conn, boolean debug) throws InterruptedException, Throwable {
		logger.info("[INICIO] - GenerarInformeClienteRelacionadoBatch");
		running.set(true);
		
		GenerarInformeClienteRelacionado_IN in = new GenerarInformeClienteRelacionado_IN();
		in.p_IdEmpresa = this.getOptionalParam("IdEmpresa")
		        .map(s -> Long.valueOf((String) s))
		        .orElse(1L);
		in.p_IdUsuario = this.getOptionalParam("IdUsuario")
		        .map(s -> Long.valueOf((String) s))
		        .orElse(97L);
		in.p_IdModulo = this.getOptionalParam("IdModulo")
		        .orElse("CON");
		in.p_FechaInicio = this.getOptionalParam("FechaInicio")
		        .orElse("2025-03-20");
		in.p_FechaFin = this.getOptionalParam("FechaFin")
				 .orElse("2025-03-30");

		
		GenerarInformeClienteRelacionado generarInforme = new GenerarInformeClienteRelacionado();
		
		GenerarInformeClienteRelacionado_OUT out = generarInforme.logica(conn, in, false);
		
		if (out.P_OUT_ERRORESCONTADOR > 0) {
			logger.error("[ERROR] - ", out.toString());
			throw new Throwable();
		}
		running.set(false);
		
		updateRutaSalida(conn, out.RutaArchivo, this.getJobId());
		logger.info("[FIN] - GenerarInformeClienteRelacionadoBatch");
		return true;
	}

	@Override
	public void onError(Throwable t) {
		logger.error("[onError] - GenerarInformeClienteRelacionadoBatch");
		logger.error(t);
		t.printStackTrace();
	}

	@Override
	public void pause() { }
	
	public static void main(String[] args) throws Throwable {
		System.out.println("== INICIO PRUEBA ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa2", "C:\\Users\\lportales_ext\\OPTIMUS\\workspace\\optimus-corredores-dev-workspace\\OptimusDevJava\\application.properties")) {
			GenerarInformeClienteRelacionadoBatch demo = new GenerarInformeClienteRelacionadoBatch();
			demo.logica(con, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== FIN PRUEBA ==");
	}

}
