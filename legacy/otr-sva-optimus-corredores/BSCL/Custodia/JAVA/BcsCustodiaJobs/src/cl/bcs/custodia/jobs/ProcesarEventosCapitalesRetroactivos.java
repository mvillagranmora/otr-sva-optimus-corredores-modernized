package cl.bcs.custodia.jobs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.HashMap;

import cl.bcs.custodia.evc.ProcesarEventoCapital;
import cl.bcs.custodia.evc.ProcesarEventoCapital.ProcesarEventoCapitalEntrada;
import cl.bcs.custodia.evc.ProcesarEventoCapital.ProcesarEventoCapitalSalida;
import cl.bcs.plataforma.pau.AgregarControlPauta;
import cl.bcs.plataforma.pau.AgregarControlPauta.AgregarControlPautaEntrada;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;

import javax.sql.DataSource;

import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.SQL;

public class ProcesarEventosCapitalesRetroactivos extends BcsJob {
	
	private static final String EJECUCION = "PROGRAMADA";
	
	private final String SQL_SELECT_USUARIO_SISTEMA = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/Globales_SelectUsuarioSistema.sql");

	public static class Usuario {
		public Long IdUsuario;
	}

	AtomicBoolean running = new AtomicBoolean(false);
	private Long idTrabajoLog = 0L;
	
	public boolean transaction(Connection conn) throws InterruptedException, Throwable {
		
		running.set(true);
		DataSource datasource = super.getDataSource();
		
		final String IdTipoTarea = this.getParam("IdTipoTarea");

		PreparedStatement pstmt = null;
		
		try {
			
	        SQL _sql = new SQL();
	        
			final long idEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
			String confFechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, idEmpresa, "PLATAFORMA", "PLA.FechaSistema");

	        HashMap<String,Object> params = new HashMap<String,Object>();
			params.put("IdEmpresa", idEmpresa);
  			
			Long idUsuarioSistema = 0L;
			List<Usuario> usuarioSistema = _sql.query(conn, SQL_SELECT_USUARIO_SISTEMA, params, Usuario.class);        
			idUsuarioSistema = usuarioSistema.get(0).IdUsuario;

			conn = datasource.getConnection();
			final long idUsuario = idUsuarioSistema;

			logPauta(conn, getJobId(), IdTipoTarea, EJECUCION, confFechaSistema, "PEN", "ProcesarEventosCapitalesRetroactivos: Inicio del proceso");

			try {
				logPauta(conn, getJobId(), IdTipoTarea, EJECUCION, confFechaSistema, "PEN","ProcesarEventosCapitalesRetroactivos: En Ejecución");

				ProcesarEventoCapital procesarEventoCapital = new ProcesarEventoCapital();
				ProcesarEventoCapitalEntrada procesarEventoCapitalEntrada = new ProcesarEventoCapitalEntrada();
				
				procesarEventoCapitalEntrada.p_IdEmpresa = idEmpresa;
				procesarEventoCapitalEntrada.p_IdUsuario = idUsuario;
				procesarEventoCapitalEntrada.p_IdModulo = "CUS";
				procesarEventoCapitalEntrada.p_Origen = "AGENDA";
				procesarEventoCapitalEntrada.p_UsaReglasNegocio = "N";
				procesarEventoCapitalEntrada.p_IdEventoCapital = new ArrayList<Long>();
				
				procesarEventoCapitalEntrada.p_IdTipoEventoCapital.add("DA"); // DIVIDENDO EN ACCIONES
				procesarEventoCapitalEntrada.p_IdTipoEventoCapital.add("DV"); // DIVIDENDO EN DINERO
				procesarEventoCapitalEntrada.p_IdTipoEventoCapital.add("RC"); // REPARTO CAPITAL

				ProcesarEventoCapitalSalida procesarEventoCapitalSalida = procesarEventoCapital.logica(conn, procesarEventoCapitalEntrada);

				conn.commit();
				
				logPauta(conn, getJobId(), IdTipoTarea, EJECUCION, confFechaSistema, "TER", "ProcesarEventosCapitalesRetroactivos: finalizado con éxito");
			} catch (Exception ex) {
				logPauta(conn, getJobId(), IdTipoTarea, EJECUCION, confFechaSistema, "ERR", "Excepción al Ejecutar ProcesarEventosCapitalesRetroactivos IdEmpresa: " + idEmpresa + " IdTipoTarea: " + IdTipoTarea);
				ex.printStackTrace();
			}

		} catch (Exception e) {
			System.out.println("ProcesarEventosCapitalesRetroactivos(): ERROR AL OBTENER DB CONNECTION");
			System.out.println(e.getMessage());
		} finally {
			try {
				if (pstmt != null)
					pstmt.close();
				if (conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		running.set(false);
		return true;
	}

	public void logPauta(Connection con, Long idPrograma, String idTipoPrograma, String EJECUCION, String fechaSistema, String estado, String mensaje) throws Exception {
		idTrabajoLog++;
		AgregarControlPauta cnpt = new AgregarControlPauta();
		AgregarControlPauta.AgregarControlPautaEntrada cnptIn = new AgregarControlPauta.AgregarControlPautaEntrada();
		cnptIn.p_IdTrabajo = idTrabajoLog;
		List<AgregarControlPautaEntrada.Registro> registros = new ArrayList<AgregarControlPautaEntrada.Registro>();
		AgregarControlPautaEntrada.Registro item = new AgregarControlPautaEntrada.Registro();
		item.IdTrabajo = idTrabajoLog;
		item.IdPrograma = idPrograma;
		item.IdTipoPrograma = idTipoPrograma;
		item.Ejecucion = EJECUCION;
		item.FechaSistema = fechaSistema;
		item.IdEstado = estado;
		item.Mensaje = mensaje;
		registros.add(item);
		cnptIn.p_Registros = registros;
		cnpt.logica(con, cnptIn);
	}

	@Override
	public void onError(Throwable t) {
		System.out.println(":ProcesarEventosCapitalesRetroactivos:  ****** ERROR  *******");
		t.printStackTrace();
	}

	@Override
	public void pause() {

	}
}
