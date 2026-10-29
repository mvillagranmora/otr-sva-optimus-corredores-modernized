package cl.bcs.normativa.jobs;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FileLoader;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.normativa.circulares.GenerarInformeGarantiasForwardEBox;
import cl.bcs.normativa.circulares.GenerarInformeGarantiasForwardEBox.GenerarInformeGarantiasForwardEBox_Entrada;
import cl.bcs.normativa.circulares.GenerarInformeGarantiasForwardEBox.GenerarInformeGarantiasForwardEBox_Salida;
import cl.bcs.scheduler.BcsJob;

public class BcsJobGenerarInformeGarantiasForwardEBox extends BcsJob {

	private static final Logger LOGGER = LogManager.getLogger(BcsJobGenerarInformeGarantiasForwardEBox.class);

	AtomicBoolean running = new AtomicBoolean(false);

	public static class CrearGarantias {
		public Long IdEmpresa;
		public String FechaHasta;
		public Long SecuenciaSolicitud;
		
		public String toString() {
			return Utils.printJson(this, true);
		}
	}

	private static final FileLoader loader = new FileLoader();
	private static String SQL_INSERT_TABLA_TEMPORAL_GARANTIAS_MATRIZ = loader.loadResourceToString("cl/bcs/normativa/jobs/sql/BcsJobGenerarInformeGarantiasForwardEBox_InsertarTablaTemporalGarantiasMatriz.sql");
	private static String SQL_INSERT_TABLA_TEMPORAL_GARANTIAS_INDICADOR = loader.loadResourceToString("cl/bcs/normativa/jobs/sql/BcsJobGenerarInformeGarantiasForwardEBox_InsertarTablaTemporalGarantiasIndice.sql");
	private static final String MATRIZ = "MATRIZRIESGO";
	private static final String INDICADOR = "INDICADORCLIENTE"; 
	@Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
		return logica(con, false);
	}

	public boolean logica(Connection dbcon, boolean debug) throws InterruptedException, Throwable {
		running.set(true);

		final String IdTipoTarea = this.getParam("IdTipoTarea");
		try {
			SQL sql = new SQL();
			// Se obtienen parametros de entrada del JOB
			long idEmpresa = 0L;
			String fechaSistema = "";
			String fechaInicio = "";
			String fechaFin = "";
			String nombreArchivo = "";
			long secuenciaSolicitud = 0L;
			long idUsuario = 0L;
			String ruta = "";

			if (debug) {
				idEmpresa = 1L;
				fechaSistema = "2021-01-27";
				fechaInicio = "2020-12-01";
				fechaFin = "2020-12-31";
				nombreArchivo = "";
				secuenciaSolicitud = 1119L;
				idUsuario = 0L;
				ruta = "C:/mnt/optimus/des/empresa1/nor/";
			} else {
				idEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
				fechaSistema = String.valueOf(this.getParam("FechaSistema"));
				fechaInicio = String.valueOf(this.getParam("FINI"));
				fechaFin = String.valueOf(this.getParam("FTER"));
				nombreArchivo = String.valueOf(this.getParam("NOMSAL"));
				secuenciaSolicitud = Long.parseLong(this.getParam("IdSolicitudCircular"));
				idUsuario = Long.parseLong(this.getParam("idUsuario"));
				String confRutaArchivos = ConfiguracionSistema.getParametroConfiguracionString(dbcon, idEmpresa, "PLATAFORMA", "PLA.RutaBaseArchivos");
				String confSubRutaRutaArchivos = ConfiguracionSistema.getParametroConfiguracionString(dbcon, idEmpresa, "NORMATIVA", "SubRutaArchivosModulo");
				ruta = confRutaArchivos.concat(confSubRutaRutaArchivos);
			}
			
			String origenPorcentajeRiesgo = obtenerParametroOrigenPorcentajeRiesgo(dbcon, idEmpresa);
			
			List<CrearGarantias> crearGarantias = new ArrayList<>();
			CrearGarantias garantia = new CrearGarantias();
			garantia.IdEmpresa = idEmpresa;
			garantia.FechaHasta = fechaFin;
			garantia.SecuenciaSolicitud = secuenciaSolicitud;
			crearGarantias.add(garantia);

			if (MATRIZ.equals(origenPorcentajeRiesgo)) {
				LOGGER.info("GenerarInformeGarantiasForwardEBox: Llenado tabla garantias forward usando matriz de riesgo con fecha hasta " + fechaFin);
				sql.batch(dbcon, SQL_INSERT_TABLA_TEMPORAL_GARANTIAS_MATRIZ, crearGarantias, CrearGarantias.class);

			} else if (INDICADOR.equals(origenPorcentajeRiesgo)) {
				LOGGER.info("GenerarInformeGarantiasForwardEBox: Llenado tabla garantias forward usando indicador riesgo cliente con fecha hasta " + fechaFin);
				sql.batch(dbcon, SQL_INSERT_TABLA_TEMPORAL_GARANTIAS_INDICADOR, crearGarantias, CrearGarantias.class);
			}
			
			LOGGER.info("GenerarInformeGarantiasForwardEBox: Inicio del proceso");
			try {
				LOGGER.info("GenerarInformeGarantiasForwardEBox:En Ejecucion");
				GenerarInformeGarantiasForwardEBox_Entrada entrada = new GenerarInformeGarantiasForwardEBox_Entrada();
				entrada.p_IdEmpresa = idEmpresa;
				entrada.p_IdModulo = "NOR";
				entrada.p_IdUsuario = idUsuario;
				entrada.p_FechaSistema = fechaSistema;
				entrada.p_FechaInicio = fechaInicio;
				entrada.p_FechaFin = fechaFin;
				entrada.p_NombreArchivo = nombreArchivo;
				entrada.p_Ruta = ruta;
				entrada.p_SecuenciaSolicitud = secuenciaSolicitud;
				LOGGER.info("GenerarInformeGarantiasForwardEBox: Llamado java que genera archivo circular");
				GenerarInformeGarantiasForwardEBox_Salida salida = new GenerarInformeGarantiasForwardEBox_Salida();
				GenerarInformeGarantiasForwardEBox sp = new GenerarInformeGarantiasForwardEBox();
				salida = sp.logica(dbcon, entrada);

				System.out.println(salida.P_OUT_ERRORESCONTADOR);
				long numRegistros = 0L;
				nombreArchivo = "";

				if (salida.P_OUT_ERRORESCONTADOR == 0) {
					numRegistros = salida.P_OUT_NUMERO_REGISTROS;
					nombreArchivo = salida.P_OUT_NOMBRE_ARCHIVO;
				}

				ActualizarSolicitud(secuenciaSolicitud, nombreArchivo, ruta, numRegistros, dbcon, getJobId());

				LOGGER.info("GenerarInformeGarantiasForwardEBox: finalizado con exito");
			} catch (Exception ex) {
				System.out.println(
						"GenerarInformeGarantiasForwardEBox: EXCEPTION AL EJECUTAR: Informe Garantias Forward");
				LOGGER.info("Excepcion al Ejecutar GenerarInformeGarantiasForwardEBox IdEmpresa: " + idEmpresa
						+ " IdTipoTarea: " + IdTipoTarea, ex);
				ex.printStackTrace();
			}

		} catch (Exception e) {
			LOGGER.info("GenerarInformeGarantiasForwardEBox(): ERROR AL OBTENER DB CONNECTION", e);
			System.out.println(e.getMessage());
		}
		running.set(false);
		return true;
	}

	private String ActualizarSolicitud(final long p_IdSolicitud, String p_NombreArchivo, String p_RutaArchivo,
			final long p_NumRegistros, Connection conn, long jobId) {

		String sqlCall = "";

		String PROCEDURE_NAME_ACTUALIZAR = "BSCL.Normativa.BD.Servicios.NOR::ModificarEstadoSolicitudCircular";
		p_NombreArchivo = "'" + p_NombreArchivo + "'";
		p_RutaArchivo = "'" + p_RutaArchivo + "'";
		sqlCall = String.format("{CALL %s.%s(%s,%s,%s,%s,%s)}", getQuotedString("BSCL"),
				getQuotedString(PROCEDURE_NAME_ACTUALIZAR), p_IdSolicitud, p_NombreArchivo, p_RutaArchivo,
				p_NumRegistros, jobId);
		System.out.println(sqlCall);

		try (CallableStatement cstmt = conn.prepareCall(sqlCall)) {
			conn.setAutoCommit(false);

			if (cstmt.execute()) {
				conn.commit();

				try (final ResultSet rs = cstmt.getResultSet()) {
					if (rs.next()) {
						LOGGER.info("Proceso de Actualizacion de Solicitud exitoso");
						return "OK";
					} else {
						LOGGER.info("Error durante proceso de Actualizacion de Solicitud, no RS");
						return "NOK";

					}

				} catch (Exception ex) {

					LOGGER.info("Error durante proceso de Actualizacion de Solicitud", ex);
					return "NOK";
				}

			} else {

				LOGGER.info("Error no se pudo ejecutar servicio actualizacion");
				return "NOK";
			}

		} catch (Exception ex) {

			LOGGER.info("Error durante proceso de Actualizacion de Solicitud", ex);
			return "NOK";
		}

	}

	private static String getQuotedString(final String name) {
		return "\"" + name + "\"";
	}
	
	private String obtenerParametroOrigenPorcentajeRiesgo(Connection con, Long idEmpresa) throws Exception {
		LOGGER.trace("INICIO::obtenerParametroOrigenPorcentajeRiesgo");
		String origenPorcentajeRiesgo = MATRIZ;
		try {
			origenPorcentajeRiesgo = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "MESA", "OrigenPorcentajeRiesgoInformesFWD");
		}catch(Exception e) {
			LOGGER.warn("ADVERTENCIA::obtenerParametroOrigenPorcentajeRiesgo::no existe parametro OrigenPorcentajeRiesgoInformesFWD, se deja valor por defecto.");
		}
		
		LOGGER.trace("FIN::obtenerParametroOrigenPorcentajeRiesgo");
		return origenPorcentajeRiesgo;
	}

	@Override
	public void onError(Throwable t) {
		System.out.println(":GenerarInformeGarantiasForwardEBox:  ****** ERROR  *******");
		t.printStackTrace();
	}

	@Override
	public void pause() {

	}

	public static void main(String[] args) {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa2", "connections.properties")) {
			BcsJobGenerarInformeGarantiasForwardEBox sp = new BcsJobGenerarInformeGarantiasForwardEBox();
			sp.logica(con, true);
			// con.commit();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}
}