package cl.bcs.replica;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import cl.bcs.plataforma.pla.ObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.Feriados;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.replica.EnviarTablaReplica.FechaRespaldo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ReplicaUtils implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(ReplicaUtils.class);
	
	private final String SQL_DELETE_TBCTCIRV = loadResourceToString("cl/bcs/replica/sql/EnviarTablaReplica_DeleteTBCTCIRV.sql");
	private final String SQL_DELETE_TBCTCURV = loadResourceToString("cl/bcs/replica/sql/EnviarTablaReplica_DeleteTBCTCURV.sql");
	private final String SQL_DELETE_TBCTCIRF = loadResourceToString("cl/bcs/replica/sql/EnviarTablaReplica_DeleteTBCTCIRF.sql");
	private final String SQL_DELETE_TBCTCURF = loadResourceToString("cl/bcs/replica/sql/EnviarTablaReplica_DeleteTBCTCURF.sql");
	private final String SQL_DELETE_TBCTCIIF = loadResourceToString("cl/bcs/replica/sql/EnviarTablaReplica_DeleteTBCTCIIF.sql");
	private final String SQL_DELETE_TBCTCTCL = loadResourceToString("cl/bcs/replica/sql/EnviarTablaReplica_DeleteTBCTCTCL.sql");
	private final String SQL_DELETE_TBCTTRTP = loadResourceToString("cl/bcs/replica/sql/EnviarTablaReplica_DeleteTBCTTRTP.sql");
	private final String SQL_DELETE_TBCTCIFM = loadResourceToString("cl/bcs/replica/sql/EnviarTablaReplica_DeleteTBCTCIFM.sql");
	private final String SQL_DELETE_TBCTCUFM = loadResourceToString("cl/bcs/replica/sql/EnviarTablaReplica_DeleteTBCTCUFM.sql");
	private final String SQL_DELETE_TBCPMSPC = loadResourceToString("cl/bcs/replica/sql/EnviarTablaReplica_DeleteTBCPMSPC.sql");
	private final String SQL_DELETE_TBCPDTPC = loadResourceToString("cl/bcs/replica/sql/EnviarTablaReplica_DeleteTBCPDTPC.sql");
	private final String SQL_DELETE_TBCTGTIA = loadResourceToString("cl/bcs/replica/sql/EnviarTablaReplica_DeleteTBCTGTIA.sql");
	private final String SQL_DELETE_TBCTTRPT = loadResourceToString("cl/bcs/replica/sql/EnviarTablaReplica_DeleteTBCTTRPT.sql");
	
	private final String SQL_INSERT_TBCTCIRV = loadResourceToString("cl/bcs/replica/sql/EnviarTablaReplica_InsertTBCTCIRV.sql");
	private final String SQL_INSERT_TBCTCURV = loadResourceToString("cl/bcs/replica/sql/EnviarTablaReplica_InsertTBCTCURV.sql");
	private final String SQL_INSERT_TBCTCIRF = loadResourceToString("cl/bcs/replica/sql/EnviarTablaReplica_InsertTBCTCIRF.sql");
	private final String SQL_INSERT_TBCTCURF = loadResourceToString("cl/bcs/replica/sql/EnviarTablaReplica_InsertTBCTCURF.sql");
	private final String SQL_INSERT_TBCTCIIF = loadResourceToString("cl/bcs/replica/sql/EnviarTablaReplica_InsertTBCTCIIF.sql");
	private final String SQL_INSERT_TBCTCTCL = loadResourceToString("cl/bcs/replica/sql/EnviarTablaReplica_InsertTBCTCTCL.sql");
	private final String SQL_INSERT_TBCTTRTP = loadResourceToString("cl/bcs/replica/sql/EnviarTablaReplica_InsertTBCTTRTP.sql");
	private final String SQL_INSERT_TBCTCIFM = loadResourceToString("cl/bcs/replica/sql/EnviarTablaReplica_InsertTBCTCIFM.sql");
	private final String SQL_INSERT_TBCTCUFM = loadResourceToString("cl/bcs/replica/sql/EnviarTablaReplica_InsertTBCTCUFM.sql");
	private final String SQL_INSERT_TBCPMSPC = loadResourceToString("cl/bcs/replica/sql/EnviarTablaReplica_InsertTBCPMSPC.sql");
	private final String SQL_INSERT_TBCPDTPC = loadResourceToString("cl/bcs/replica/sql/EnviarTablaReplica_InsertTBCPDTPC.sql");
	private final String SQL_INSERT_TBCTGTIA = loadResourceToString("cl/bcs/replica/sql/EnviarTablaReplica_InsertTBCTGTIA.sql");
	private final String SQL_INSERT_TBCTTRPT = loadResourceToString("cl/bcs/replica/sql/EnviarTablaReplica_InsertTBCTTRPT.sql");
	
	public void procesarRespaldoSebra(Connection conn, String fechaSistemaEjecucion, Long idEmpresa) throws Exception {
		String respaldarSebra = "N";
		try {
			respaldarSebra = ConfiguracionSistema.getParametroConfiguracionString(conn, idEmpresa, "PLATAFORMA", "PLA.RespaldoReplicaTablasSebra");
		}
		catch(Exception e) {
			logger.error("Falta parametro de respaldo sebra");
			throw e;
		}
		
		if (respaldarSebra.equalsIgnoreCase("S")) {
			
			logger.info("Ejecutando respaldo replica sebra para " + fechaSistemaEjecucion);

			try {
				logger.debug("Iniciando TBCPDTPC");
				SQL.exec(conn, SQL_DELETE_TBCPDTPC.replace(":COD_PER", fechaSistemaEjecucion.substring(0,7)));
				SQL.exec(conn, SQL_INSERT_TBCPDTPC.replace(":COD_PER", fechaSistemaEjecucion.substring(0,7)));
			
				logger.debug("Iniciando TBCPMSPC");
				SQL.exec(conn, SQL_DELETE_TBCPMSPC.replace(":COD_PER", fechaSistemaEjecucion.substring(0,7)));
				SQL.exec(conn, SQL_INSERT_TBCPMSPC.replace(":COD_PER", fechaSistemaEjecucion.substring(0,7)));
			
				logger.debug("Iniciando TBCTCIRF");
				SQL.exec(conn, SQL_DELETE_TBCTCIRF.replace(":COD_PER", fechaSistemaEjecucion.substring(0,7)));
				SQL.exec(conn, SQL_INSERT_TBCTCIRF.replace(":COD_PER", fechaSistemaEjecucion.substring(0,7)));
					
				logger.debug("Iniciando TBCTCURF");
				SQL.exec(conn, SQL_DELETE_TBCTCURF.replace(":COD_PER", fechaSistemaEjecucion.substring(0,7)));
				SQL.exec(conn, SQL_INSERT_TBCTCURF.replace(":COD_PER", fechaSistemaEjecucion.substring(0,7)));
			
				logger.debug("Iniciando TBCTCIRV");
				SQL.exec(conn, SQL_DELETE_TBCTCIRV.replace(":COD_PER", fechaSistemaEjecucion.substring(0,7)));
				SQL.exec(conn, SQL_INSERT_TBCTCIRV.replace(":COD_PER", fechaSistemaEjecucion.substring(0,7)));
			
				logger.debug("Iniciando TBCTCURV");
				SQL.exec(conn, SQL_DELETE_TBCTCURV.replace(":COD_PER", fechaSistemaEjecucion.substring(0,7)));
				SQL.exec(conn, SQL_INSERT_TBCTCURV.replace(":COD_PER", fechaSistemaEjecucion.substring(0,7)));
			
				logger.debug("Iniciando TBCTCIFM");
				SQL.exec(conn, SQL_DELETE_TBCTCIFM.replace(":COD_PER", fechaSistemaEjecucion.substring(0,7)));
				SQL.exec(conn, SQL_INSERT_TBCTCIFM.replace(":COD_PER", fechaSistemaEjecucion.substring(0,7)));
			
				logger.debug("Iniciando TBCTCUFM");
				SQL.exec(conn, SQL_DELETE_TBCTCUFM.replace(":COD_PER", fechaSistemaEjecucion.substring(0,7)));
				SQL.exec(conn, SQL_INSERT_TBCTCUFM.replace(":COD_PER", fechaSistemaEjecucion.substring(0,7)));
			
				logger.debug("Iniciando TBCTCTCL");
				SQL.exec(conn, SQL_DELETE_TBCTCTCL.replace(":COD_PER", fechaSistemaEjecucion.substring(0,7)));
				SQL.exec(conn, SQL_INSERT_TBCTCTCL.replace(":COD_PER", fechaSistemaEjecucion.substring(0,7)));
			
				logger.debug("Iniciando TBCTTRTP");
				SQL.exec(conn, SQL_DELETE_TBCTTRTP.replace(":COD_PER", fechaSistemaEjecucion.substring(0,7)));
				SQL.exec(conn, SQL_INSERT_TBCTTRTP.replace(":COD_PER", fechaSistemaEjecucion.substring(0,7)));
			
				logger.debug("Iniciando TBCTCIIF");
				SQL.exec(conn, SQL_DELETE_TBCTCIIF.replace(":COD_PER", fechaSistemaEjecucion.substring(0,7)));
				SQL.exec(conn, SQL_INSERT_TBCTCIIF.replace(":COD_PER", fechaSistemaEjecucion.substring(0,7)));
			
				logger.debug("Iniciando TBCTGTIA");
				SQL.exec(conn, SQL_DELETE_TBCTGTIA.replace(":COD_PER", fechaSistemaEjecucion.substring(0,7)));
				SQL.exec(conn, SQL_INSERT_TBCTGTIA.replace(":COD_PER", fechaSistemaEjecucion.substring(0,7)));
				
				logger.debug("Iniciando TBCTTRPT");
				SQL.exec(conn, SQL_DELETE_TBCTTRPT.replace(":COD_PER", fechaSistemaEjecucion.substring(0,7)));
				SQL.exec(conn, SQL_INSERT_TBCTTRPT.replace(":COD_PER", fechaSistemaEjecucion.substring(0,7)));
			}
			catch (Exception e) {
				logger.error(e);
				throw e;
			}
			logger.debug("Fin");
		}
	}
	
	private final String SQL_INSERT_CUENTA_INVERSION_FECHA_EFECTIVA_HISTORICA = loadResourceToString("cl/bcs/replica/sql/EnviarTablaReplica_InsertCuentaInversionFechaEfectivaHistorica.sql");
	private final String SQL_INSERT_CUENTA_INVERSION_FECHA_MOVIMIENTO_HISTORICA = loadResourceToString("cl/bcs/replica/sql/EnviarTablaReplica_InsertCuentaInversionFechaMovimientoHistorica.sql");
	private final String SQL_DELETE_CUENTA_INVERSION_FECHA_EFECTIVA_CURRENT = loadResourceToString("cl/bcs/replica/sql/EnviarTablaReplica_DeleteCuentaInversionFechaEfectivaCurrent.sql");
	private final String SQL_DELETE_CUENTA_INVERSION_FECHA_MOVIMIENTO_CURRENT = loadResourceToString("cl/bcs/replica/sql/EnviarTablaReplica_DeleteCuentaInversionFechaMovimientoCurrent.sql");

	private final String SQL_DELETE_CUSTODIA_FECHA_OPERACION_CURRENT = loadResourceToString("cl/bcs/replica/sql/EnviarTablaReplica_DeleteCustodiaFechaOperacionCurrent.sql");
	private final String SQL_DELETE_CUSTODIA_FECHA_LIQUIDACION_CURRENT = loadResourceToString("cl/bcs/replica/sql/EnviarTablaReplica_DeleteCustodiaFechaLiquidacionCurrent.sql");
	private final String SQL_INSERT_CUSTODIA_FECHA_OPERACION_HISTORICA = loadResourceToString("cl/bcs/replica/sql/EnviarTablaReplica_InsertCustodiaFechaOperacionHistorica.sql");
	private final String SQL_INSERT_CUSTODIA_FECHA_LIQUIDACION_HISTORICA = loadResourceToString("cl/bcs/replica/sql/EnviarTablaReplica_InsertCustodiaFechaLiquidacionHistorica.sql");

	private final String SQL_INSERT_CUENTA_INVERSION_FECHA_EFECTIVA_DIA_INHABIL= loadResourceToString("cl/bcs/replica/sql/EnviarTablaReplica_InsertCuentaInversionFechaEfectivaDiaInhabil.sql");
	private final String SQL_INSERT_CUENTA_INVERSION_FECHA_MOVIMIENTO_DIA_INHABIL= loadResourceToString("cl/bcs/replica/sql/EnviarTablaReplica_InsertCuentaInversionFechaMovimientoDiaInhabil.sql");
	private final String SQL_INSERT_CUSTODIA_FECHA_LIQUIDACION_DIA_INHABIL = loadResourceToString("cl/bcs/replica/sql/EnviarTablaReplica_InsertCustodiaFechaLiquidacionDiaInhabil.sql");
	private final String SQL_INSERT_CUSTODIA_FECHA_OPERACION_DIA_INHABIL = loadResourceToString("cl/bcs/replica/sql/EnviarTablaReplica_InsertCustodiaFechaOperacionDiaInhabil.sql");

	private final String SQL_UPSERT_ENVIO_POST_PAUTA = loadResourceToString("cl/bcs/replica/sql/EnviarTablaReplica_InsertEnvioPostPauta.sql");

	public static class EnvioPostPauta {
		public String FechaSistema;
		public String Tabla;
		public String Fecha;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public void guardarEnvioPostPauta(Connection conn,String tabla,String fechasistema,String fechaenvio) throws SQLException {
		List<EnvioPostPauta> datos = new ArrayList<>();
		SQL sql = new SQL();
		
		EnvioPostPauta c1 = new EnvioPostPauta();
		c1.Fecha = fechaenvio;
		c1.FechaSistema = fechasistema;
		c1.Tabla =  tabla;
		
		datos.add(c1);
		logger.debug("Guardando envio postpauta de " + c1.toString());
		sql.batch(conn,SQL_UPSERT_ENVIO_POST_PAUTA,datos,EnvioPostPauta.class);
	}
	
	public void procesarRespaldoOptimus(Connection conn, String fechaSistemaEjecucion, Long idEmpresa) throws Exception {
		
		SQL sql = new SQL();
		
		logger.info("Ejecutando respaldo replica Optimus para " + fechaSistemaEjecucion);
		
		try {
			logger.debug("Iniciando Cuenta Inversion Saldo Fecha Efectiva");
			SQL.exec(conn, SQL_DELETE_CUENTA_INVERSION_FECHA_EFECTIVA_CURRENT.replace(":FechaSaldo", fechaSistemaEjecucion));
			SQL.exec(conn, SQL_INSERT_CUENTA_INVERSION_FECHA_EFECTIVA_HISTORICA);


			logger.debug("Iniciando Cuenta Inversion Saldo Fecha Movimiento");
			SQL.exec(conn, SQL_DELETE_CUENTA_INVERSION_FECHA_MOVIMIENTO_CURRENT.replace(":FechaSaldo", fechaSistemaEjecucion));
			SQL.exec(conn, SQL_INSERT_CUENTA_INVERSION_FECHA_MOVIMIENTO_HISTORICA);
			
			logger.debug("Iniciando Custodia Fecha Operacion");
			SQL.exec(conn, SQL_DELETE_CUSTODIA_FECHA_OPERACION_CURRENT.replace(":FechaCustodia", fechaSistemaEjecucion));
			SQL.exec(conn, SQL_INSERT_CUSTODIA_FECHA_OPERACION_HISTORICA);
			
			logger.debug("Iniciando Custodia Fecha Liquidacion");
			SQL.exec(conn, SQL_DELETE_CUSTODIA_FECHA_LIQUIDACION_CURRENT.replace(":FechaCustodia", fechaSistemaEjecucion));
			SQL.exec(conn, SQL_INSERT_CUSTODIA_FECHA_LIQUIDACION_HISTORICA);			
			
			this.guardarEnvioPostPauta(conn,"Custodia_CustodiaFechaOperacionHis",fechaSistemaEjecucion,fechaSistemaEjecucion);
			this.guardarEnvioPostPauta(conn,"Custodia_CustodiaFechaLiquidacionHis",fechaSistemaEjecucion,fechaSistemaEjecucion);
			this.guardarEnvioPostPauta(conn,"CuentaInversion_SaldoFechaEfectivaHis",fechaSistemaEjecucion,fechaSistemaEjecucion);
			this.guardarEnvioPostPauta(conn,"CuentaInversion_SaldoFechaMovimientoHis",fechaSistemaEjecucion,fechaSistemaEjecucion);

			
			
			//dias inhabiles
			
			ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
			InformacionEmpresa empresa = consultarEmpresaService.logica(conn, idEmpresa);
			Long idPaisEmpresa = empresa.IdPais;
			
			String FechaDestino = fechaSistemaEjecucion;
			Boolean ContinuarRespaldoDiaInhabil = true;
			while (ContinuarRespaldoDiaInhabil) {
				List<FechasIn> lstFechas = new ArrayList<FechasIn>();
				FechasIn inFechas = new FechasIn();
				inFechas.fechaInicial 	= FechaDestino;
				inFechas.numeroDias 	= 1L;
				inFechas.pais 			= idEmpresa;
				lstFechas.add(inFechas);
				
				ObtenerDiaSiguiente obtenerDiaSiguiente = new ObtenerDiaSiguiente();
				InObtenerDiaSiguiente  inObtenerDiaSiguiente = new InObtenerDiaSiguiente();
				
				inObtenerDiaSiguiente.p_idPais 			= idPaisEmpresa;
				inObtenerDiaSiguiente.p_diaHabil 		= "N";
				inObtenerDiaSiguiente.p_soloCaeDiaHabil = "N";
				inObtenerDiaSiguiente.p_fechas 			= lstFechas;
				
				OutObtenerDiaSiguiente outObtenerDiaSiguiente = obtenerDiaSiguiente.logica(conn, inObtenerDiaSiguiente);
				
				if (outObtenerDiaSiguiente.P_OUT_ERRORESCONTADOR.compareTo(0L) != 0) {
					ContinuarRespaldoDiaInhabil = false;		
					logger.error("Error al obtener dia siguiente");
					break;
				}
				
				FechaDestino = outObtenerDiaSiguiente.p_out_fechas.get(0).fechaCalculada;
				logger.debug("FechaDestino > --- " + FechaDestino + "---");
				Boolean esDiaHabil = Feriados.esDiaHabil(conn, FechaDestino, idPaisEmpresa);
				logger.debug("EsDiaHabil > " + esDiaHabil + (esDiaHabil ? " > Entonces Termina proceso sin respaldar para "+FechaDestino : " > Entonces Copiar tablas Desde "+fechaSistemaEjecucion+ " --> Hacia "+FechaDestino));
				if (esDiaHabil) {
					ContinuarRespaldoDiaInhabil = false;		
					break;
				}
				
				
				List<FechaRespaldo> respaldos = new ArrayList<>();
				FechaRespaldo respaldoFechas = new FechaRespaldo();
				respaldoFechas.FechaOrigen = fechaSistemaEjecucion;
				respaldoFechas.FechaDestino = FechaDestino;
				respaldos.add(respaldoFechas);
				
				logger.debug("Iniciando Cuenta Inversion Saldo Fecha Efectiva");
				SQL.exec(conn, SQL_DELETE_CUENTA_INVERSION_FECHA_EFECTIVA_CURRENT.replace(":FechaSaldo", FechaDestino));
				sql.batch(conn, SQL_INSERT_CUENTA_INVERSION_FECHA_EFECTIVA_DIA_INHABIL, respaldos, FechaRespaldo.class);


				logger.debug("Iniciando Cuenta Inversion Saldo Fecha Movimiento");
				SQL.exec(conn, SQL_DELETE_CUENTA_INVERSION_FECHA_MOVIMIENTO_CURRENT.replace(":FechaSaldo", FechaDestino));
				sql.batch(conn, SQL_INSERT_CUENTA_INVERSION_FECHA_MOVIMIENTO_DIA_INHABIL, respaldos, FechaRespaldo.class);

				
				logger.debug("Iniciando Custodia Fecha Operacion");
				SQL.exec(conn, SQL_DELETE_CUSTODIA_FECHA_OPERACION_CURRENT.replace(":FechaCustodia", FechaDestino));
				sql.batch(conn, SQL_INSERT_CUSTODIA_FECHA_OPERACION_DIA_INHABIL, respaldos, FechaRespaldo.class);		
				
				logger.debug("Iniciando Custodia Fecha Liquidacion");
				SQL.exec(conn, SQL_DELETE_CUSTODIA_FECHA_LIQUIDACION_CURRENT.replace(":FechaCustodia", FechaDestino));
				sql.batch(conn, SQL_INSERT_CUSTODIA_FECHA_LIQUIDACION_DIA_INHABIL, respaldos, FechaRespaldo.class);
				
				this.guardarEnvioPostPauta(conn,"Custodia_CustodiaFechaOperacionHis",fechaSistemaEjecucion,FechaDestino);
				this.guardarEnvioPostPauta(conn,"Custodia_CustodiaFechaLiquidacionHis",fechaSistemaEjecucion,FechaDestino);
				this.guardarEnvioPostPauta(conn,"CuentaInversion_SaldoFechaEfectivaHis",fechaSistemaEjecucion,FechaDestino);
				this.guardarEnvioPostPauta(conn,"CuentaInversion_SaldoFechaMovimientoHis",fechaSistemaEjecucion,FechaDestino);

			}			
			
		}
		catch (Exception e) {
			logger.error("Error en respaldo replica",e);
			throw e;
		}
		logger.debug("Fin");

	}
	
	private final String SQL_DELETE_DIARIO_TBCTCIFM = loadResourceToString("cl/bcs/replica/sql/EnviarTablaReplica_DeleteDiarioTBCTCIFM.sql");
	private final String SQL_DELETE_DIARIO_TBCTCIRV = loadResourceToString("cl/bcs/replica/sql/EnviarTablaReplica_DeleteDiarioTBCTCIRV.sql");
	private final String SQL_DELETE_DIARIO_TBCTCIRF = loadResourceToString("cl/bcs/replica/sql/EnviarTablaReplica_DeleteDiarioTBCTCIRF.sql");
	private final String SQL_DELETE_DIARIO_TBCTCIIF = loadResourceToString("cl/bcs/replica/sql/EnviarTablaReplica_DeleteDiarioTBCTCIIF.sql");

	private final String SQL_INSERT_DIARIO_TBCTCIFM = loadResourceToString("cl/bcs/replica/sql/EnviarTablaReplica_InsertDiarioTBCTCIFM.sql");
	private final String SQL_INSERT_DIARIO_TBCTCIRV = loadResourceToString("cl/bcs/replica/sql/EnviarTablaReplica_InsertDiarioTBCTCIRV.sql");
	private final String SQL_INSERT_DIARIO_TBCTCIRF = loadResourceToString("cl/bcs/replica/sql/EnviarTablaReplica_InsertDiarioTBCTCIRF.sql");
	private final String SQL_INSERT_DIARIO_TBCTCIIF = loadResourceToString("cl/bcs/replica/sql/EnviarTablaReplica_InsertDiarioTBCTCIIF.sql");
	
	public void procesarRespaldoDiarioCarterasSebra(Connection conn, String fechaSistemaEjecucion, Long idEmpresa) throws Exception {

		String respaldoDiarioCISebra = "N";

		try {
			respaldoDiarioCISebra = ConfiguracionSistema.getParametroConfiguracionString(conn,idEmpresa,"PLATAFORMA","PLA.RespaldoDiarioReplicaCarteraSebra");
		}
		catch (Exception e) {
			logger.error("Falta parametro de respaldo diario de carteras Sebra");
		}

		if (respaldoDiarioCISebra.equalsIgnoreCase("S")) {

			logger.info("Ejecutando respaldo diario de carteras Sebra para " + fechaSistemaEjecucion);

			String codPer = fechaSistemaEjecucion.substring(0, 7);
			String codDia = "90" + fechaSistemaEjecucion.substring(8, 10) + "-01";

			try {
				logger.debug("Iniciando respaldo diario TBCTCIFM");
				SQL.exec(conn, SQL_DELETE_DIARIO_TBCTCIFM.replace(":COD_DIA", codDia));
				SQL.exec(conn, SQL_INSERT_DIARIO_TBCTCIFM
						.replace(":COD_PER", codPer)
						.replace(":COD_DIA", codDia));

				logger.debug("Iniciando respaldo diario TBCTCIRV");
				SQL.exec(conn, SQL_DELETE_DIARIO_TBCTCIRV.replace(":COD_DIA", codDia));
				SQL.exec(conn, SQL_INSERT_DIARIO_TBCTCIRV
						.replace(":COD_PER", codPer)
						.replace(":COD_DIA", codDia));

				logger.debug("Iniciando respaldo diario TBCTCIIF");
				SQL.exec(conn, SQL_DELETE_DIARIO_TBCTCIIF.replace(":COD_DIA", codDia));
				SQL.exec(conn, SQL_INSERT_DIARIO_TBCTCIIF
						.replace(":COD_PER", codPer)
						.replace(":COD_DIA", codDia));

				logger.debug("Iniciando respaldo diario TBCTCIRF");
				SQL.exec(conn, SQL_DELETE_DIARIO_TBCTCIRF.replace(":COD_DIA", codDia));
				SQL.exec(conn, SQL_INSERT_DIARIO_TBCTCIRF
						.replace(":COD_PER", codPer)
						.replace(":COD_DIA", codDia));
			}
			catch (Exception e) {
				logger.error("Error en respaldo diario de carteras Sebra", e);
				throw e;
			}

			logger.debug("Fin respaldo diario de carteras Sebra");
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	/*public static void main(String [] args) throws ConfigurationException, SQLException {
		try (Connection conn = PlataformaUtils.getPropertiesConnection("scotiapre","connections.properties")) {
			ReplicaUtils r = new ReplicaUtils();
			Long idEmpresa = 1L;
			String fechaSistema = "2026-01-10";
			r.procesarRespaldoSebra(conn,fechaSistema,idEmpresa);
			conn.commit();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
	}*/
}