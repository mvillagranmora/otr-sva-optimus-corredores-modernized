package cl.bcs.replica;

import java.sql.Connection;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

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
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class EnviarTablaReplica implements JavaCall{

	static final Logger logger = LogManager.getLogger(EnviarTablaReplica.class);
	private final String SQL_INSERT_WORKSTACK = loadResourceToString("cl/bcs/replica/sql/EnviarTablaReplica_InsertWorkstack.sql");
	private final String SQL_DROP_TRIGGER_CUSTODIA = loadResourceToString("cl/bcs/replica/sql/EnviarTablaReplica_DropTriggerCustodia.sql");
	private final String SQL_DROP_TRIGGER_CUENTA_INVERSION = loadResourceToString("cl/bcs/replica/sql/EnviarTablaReplica_DropTriggerCuentaInversion.sql");
	private final String SQL_CREATE_TRIGGER_CUSTODIA = loadResourceToString("cl/bcs/replica/sql/EnviarTablaReplica_CreateTriggerCustodia.sql");
	private final String SQL_CREATE_TRIGGER_CUENTA_INVERSION = loadResourceToString("cl/bcs/replica/sql/EnviarTablaReplica_CreateTriggerCuentaInversion.sql");
	private final String SQL_UPSERT_RESUMEN = loadResourceToString("cl/bcs/replica/sql/EnviarTablaReplica_UpsertResumen.sql");

	private final String SQL_INSERT_CUSTODIA_FECHA_OPERACION_HISTORICA = loadResourceToString("cl/bcs/replica/sql/EnviarTablaReplica_InsertCustodiaFechaOperacionHistorica.sql");
	private final String SQL_INSERT_CUSTODIA_FECHA_LIQUIDACION_HISTORICA = loadResourceToString("cl/bcs/replica/sql/EnviarTablaReplica_InsertCustodiaFechaLiquidacionHistorica.sql");
	private final String SQL_DELETE_CUSTODIA_FECHA_LIQUIDACION_HISTORICA = loadResourceToString("cl/bcs/replica/sql/EnviarTablaReplica_DeleteCustodiaFechaLiquidacionHistorica.sql");
	private final String SQL_DELETE_CUSTODIA_FECHA_LIQUIDACION_CURRENT = loadResourceToString("cl/bcs/replica/sql/EnviarTablaReplica_DeleteCustodiaFechaLiquidacionCurrent.sql");
	private final String SQL_DELETE_CUSTODIA_FECHA_OPERACION_HISTORICA = loadResourceToString("cl/bcs/replica/sql/EnviarTablaReplica_DeleteCustodiaFechaOperacionHistorica.sql");
	private final String SQL_DELETE_CUSTODIA_FECHA_OPERACION_CURRENT = loadResourceToString("cl/bcs/replica/sql/EnviarTablaReplica_DeleteCustodiaFechaOperacionCurrent.sql");
	
	private final String SQL_INSERT_CUENTA_INVERSION_FECHA_EFECTIVA_HISTORICA = loadResourceToString("cl/bcs/replica/sql/EnviarTablaReplica_InsertCuentaInversionFechaEfectivaHistorica.sql");
	private final String SQL_INSERT_CUENTA_INVERSION_FECHA_MOVIMIENTO_HISTORICA = loadResourceToString("cl/bcs/replica/sql/EnviarTablaReplica_InsertCuentaInversionFechaMovimientoHistorica.sql");
	private final String SQL_DELETE_CUENTA_INVERSION_FECHA_EFECTIVA_HISTORICA = loadResourceToString("cl/bcs/replica/sql/EnviarTablaReplica_DeleteCuentaInversionFechaEfectivaHistorica.sql");
	private final String SQL_DELETE_CUENTA_INVERSION_FECHA_MOVIMIENTO_HISTORICA = loadResourceToString("cl/bcs/replica/sql/EnviarTablaReplica_DeleteCuentaInversionFechaMovimientoHistorica.sql");
	private final String SQL_DELETE_CUENTA_INVERSION_FECHA_EFECTIVA_CURRENT = loadResourceToString("cl/bcs/replica/sql/EnviarTablaReplica_DeleteCuentaInversionFechaEfectivaCurrent.sql");
	private final String SQL_DELETE_CUENTA_INVERSION_FECHA_MOVIMIENTO_CURRENT = loadResourceToString("cl/bcs/replica/sql/EnviarTablaReplica_DeleteCuentaInversionFechaMovimientoCurrent.sql");

	private final String SQL_INSERT_CUENTA_INVERSION_FECHA_EFECTIVA_DIA_INHABIL= loadResourceToString("cl/bcs/replica/sql/EnviarTablaReplica_InsertCuentaInversionFechaEfectivaDiaInhabil.sql");
	private final String SQL_INSERT_CUENTA_INVERSION_FECHA_MOVIMIENTO_DIA_INHABIL= loadResourceToString("cl/bcs/replica/sql/EnviarTablaReplica_InsertCuentaInversionFechaMovimientoDiaInhabil.sql");
	private final String SQL_INSERT_CUSTODIA_FECHA_LIQUIDACION_DIA_INHABIL = loadResourceToString("cl/bcs/replica/sql/EnviarTablaReplica_InsertCustodiaFechaLiquidacionDiaInhabil.sql");
	private final String SQL_INSERT_CUSTODIA_FECHA_OPERACION_DIA_INHABIL = loadResourceToString("cl/bcs/replica/sql/EnviarTablaReplica_InsertCustodiaFechaOperacionDiaInhabil.sql");
	
	public static class ResumenReplica {
		public String Tipo;
		public String Nombre;
		public String Estado;
		public String Detalle;
		public Date FechaSistema;
		public Timestamp UltimaActualizacion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public static class FechaRespaldo extends BcsPojo{
		public String FechaOrigen;
		public String FechaDestino;
		
		public FechaRespaldo() {
			this.FechaOrigen = "0001-01-01";
			this.FechaDestino = "0001-01-01";
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	public Salida_EnviarTablaReplica logica(Connection conn,Entrada_EnviarTablaReplica in) throws Exception {
		Salida_EnviarTablaReplica salida = new Salida_EnviarTablaReplica();
		
		String fechaSistemaEjecucion = (String) in.p_Parametros.get("FechaSistemaEjecucion");
		List<Workstack> list = new ArrayList<>();
		logger.info("Procesando replica para proceso " + in.p_Proceso);
		List<ResumenReplica> resumen = new ArrayList<>();

		if (in.p_Proceso.equalsIgnoreCase("INICIO_PAUTA")) {
			{
				//DROP TRIGGERS
				logger.info("Desactivando replica online");
				try {
					SQL.exec(conn, SQL_DROP_TRIGGER_CUSTODIA);
					SQL.exec(conn, SQL_DROP_TRIGGER_CUENTA_INVERSION);
				}
				catch(Exception e) {
					logger.error("Error al desactivar replica online");
					logger.error(e);
				}
			}
			
			
			{
				
				Workstack w = new Workstack();
				w.Key = "";
				w.Table = "CUENTA INVERSION DIA ACTUAL";
				w.Action = "M";
				w.Parameters = "tables:CuentaInversion_SaldoFechaEfectivaHist,CuentaInversion_SaldoFechaMovimientoHist;BCS_query:QueryReplicaDelta;Fecha:" + fechaSistemaEjecucion + ";serialDelete:false;truncarDestino:false;serialLaunch:false";
				w.Status = 0;
				list.add(w);
				
				logger.info("Limpiando cuenta inversion historica");
				SQL.exec(conn, SQL_DELETE_CUENTA_INVERSION_FECHA_EFECTIVA_HISTORICA.replace(":FechaEjecucion", fechaSistemaEjecucion));
				SQL.exec(conn, SQL_DELETE_CUENTA_INVERSION_FECHA_MOVIMIENTO_HISTORICA.replace(":FechaEjecucion", fechaSistemaEjecucion));
				SQL.exec(conn, SQL_DELETE_CUENTA_INVERSION_FECHA_EFECTIVA_CURRENT.replace(":FechaSaldo", fechaSistemaEjecucion));
				SQL.exec(conn, SQL_DELETE_CUENTA_INVERSION_FECHA_MOVIMIENTO_CURRENT.replace(":FechaSaldo", fechaSistemaEjecucion));

				logger.info("Insertando cuenta inversion historica");
				
				SQL.exec(conn, SQL_INSERT_CUENTA_INVERSION_FECHA_EFECTIVA_HISTORICA);
				SQL.exec(conn, SQL_INSERT_CUENTA_INVERSION_FECHA_MOVIMIENTO_HISTORICA);
				
				logger.info("Limpiando custodia historica");
				SQL.exec(conn, SQL_DELETE_CUSTODIA_FECHA_LIQUIDACION_HISTORICA.replace(":FechaEjecucion", fechaSistemaEjecucion));
				SQL.exec(conn, SQL_DELETE_CUSTODIA_FECHA_OPERACION_HISTORICA.replace(":FechaEjecucion", fechaSistemaEjecucion));
				SQL.exec(conn, SQL_DELETE_CUSTODIA_FECHA_LIQUIDACION_CURRENT.replace(":FechaCustodia", fechaSistemaEjecucion));
				SQL.exec(conn, SQL_DELETE_CUSTODIA_FECHA_OPERACION_CURRENT.replace(":FechaCustodia", fechaSistemaEjecucion));
				
				logger.info("Insertando custodia historica");
				SQL.exec(conn, SQL_INSERT_CUSTODIA_FECHA_LIQUIDACION_HISTORICA);
				SQL.exec(conn, SQL_INSERT_CUSTODIA_FECHA_OPERACION_HISTORICA);

				List<Workstack> cuentasInversionFuturas = new ArrayList<Workstack>();
				String tableCuentaInversion = "CUENTA INVERSION DIA INHABIL";
				String parametersCuentaInversion = "tables:CuentaInversion_SaldoFechaEfectivaHist,CuentaInversion_SaldoFechaMovimientoHist;BCS_query:QueryReplicaDelta;Fecha:";
				cuentasInversionFuturas = respaldarDataDiaInhabil(conn, in.p_IdEmpresa, fechaSistemaEjecucion, tableCuentaInversion, parametersCuentaInversion);
				list.addAll(cuentasInversionFuturas);
				
			}
		}
		else if (in.p_Proceso.equalsIgnoreCase("VALORIZAR_FFMM")) {
			{
				Workstack w = new Workstack();
				w.Key = "";
				w.Table = "CUSTODIA ACTUAL";
				w.Action = "M";
				w.Parameters = "tables:Custodia_CustodiaFechaLiquidacionHist,Custodia_CustodiaFechaOperacionHist;BCS_query:QueryReplicaDelta;Fecha:" + fechaSistemaEjecucion + ";serialDelete:false;truncarDestino:false;serialLaunch:false";
				w.Status = 0;
				list.add(w);
			}
			
			{
				Workstack w1 = new Workstack();
				w1.Key = "";
				w1.Table = "CARTERAFM";
				w1.Action = "M";
				w1.Parameters = "tables:Custodia_CarteraFM;BCS_query:QueryReplica;Fecha:" + fechaSistemaEjecucion + ";serialDelete:false;truncarDestino:true;serialLaunch:false";
				w1.Status = 0;
				
				list.add(w1);
			}
			
			List<Workstack> custodiasFuturas = new ArrayList<Workstack>();
			String table = "CUSTODIA ACTUAL DIA INHABIL";
			String parameters = "tables:Custodia_CustodiaFechaOperacionHist,Custodia_CustodiaFechaLiquidacionHist;BCS_query:QueryReplicaDelta;Fecha:";
			custodiasFuturas = respaldarDataDiaInhabil(conn, in.p_IdEmpresa, fechaSistemaEjecucion, table, parameters);
			list.addAll(custodiasFuturas);
			
		}
		else if (in.p_Proceso.equalsIgnoreCase("PAUTA_PATRIMONIO")) {
			{
				Workstack w1 = new Workstack();
				w1.Key = "";
				w1.Table = "PATRIMONIO DIA ACTUAL";
				w1.Action = "M";
				w1.Parameters = "tables:CuentaInversion_PatrimonioCajaFechaLiquidacion,CuentaInversion_PatrimonioCajaFechaOperacion,Custodia_PatrimonioConsolidadoFechaLiquidacion,Custodia_PatrimonioConsolidadoFechaOperacion,Custodia_PatrimonioCustodiaFechaLiquidacion,Custodia_PatrimonioCustodiaFechaOperacion;BCS_query:QueryReplicaDelta;Fecha:" + fechaSistemaEjecucion + ";serialDelete:false;truncarDestino:false;serialLaunch:false";
				w1.Status = 0;
				
				list.add(w1);
			}
			
		}
		else if (in.p_Proceso.equalsIgnoreCase("COPIA_PATRIMONIO_PARA_INHABIL")) {
			ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
			InformacionEmpresa empresa = consultarEmpresaService.logica(conn, in.p_IdEmpresa);
			Long idPaisEmpresa = empresa.IdPais;
			
			String FechaDestino = fechaSistemaEjecucion;
			Boolean ContinuarCopiaPatrimonio = true;
			
			while (ContinuarCopiaPatrimonio)
			{
				ObtenerDiaSiguiente obtenerDiaSiguiente = new ObtenerDiaSiguiente();
				InObtenerDiaSiguiente  inObtenerDiaSiguiente = new InObtenerDiaSiguiente();

				List<FechasIn> lstFechas = new ArrayList<FechasIn>();
				FechasIn inFechas = new FechasIn();
				inFechas.fechaInicial 	= FechaDestino;
				inFechas.numeroDias 	= 1L;
				inFechas.pais 			= idPaisEmpresa;
				lstFechas.add(inFechas);
				
				inObtenerDiaSiguiente.p_idPais 			= idPaisEmpresa;
				inObtenerDiaSiguiente.p_diaHabil 		= "N";  //al sumar dias, considera solo dias habiles
				inObtenerDiaSiguiente.p_soloCaeDiaHabil = "N";  //el resultado debe ser un dia habil
				inObtenerDiaSiguiente.p_fechas 			= lstFechas;
				
				OutObtenerDiaSiguiente outObtenerDiaSiguiente = obtenerDiaSiguiente.logica(conn, inObtenerDiaSiguiente);
					
				if (outObtenerDiaSiguiente.P_OUT_ERRORESCONTADOR.compareTo(0L) != 0)
				{
					logger.error("Error al obtener dia siguiente");
					return salida;
				}

				FechaDestino = outObtenerDiaSiguiente.p_out_fechas.get(0).fechaCalculada;
				logger.debug("FechaDestino > --- " + FechaDestino + "---");
				
				
				//-------------------------------------------------------------			
				// EVALUA SI DIA SIGUIENTE ES INHABIL
				//-------------------------------------------------------------
				//Evalua si el dia es inhabil
				Boolean esDiaHabil = Feriados.esDiaHabil(conn, FechaDestino, idPaisEmpresa);
				
				logger.debug("EsDiaHabil > " + esDiaHabil + (esDiaHabil ? " > Entonces Termina proceso" : " > Entonces Copiar Patrimonio"));
				if (esDiaHabil)
				{
					ContinuarCopiaPatrimonio = false; //si dia siguiente es habil, deja de copiar			
					break;
				}
				
				Workstack w1 = new Workstack();
				w1.Key = "";
				w1.Table = "PATRIMONIO DIA " + FechaDestino;
				w1.Action = "M";
				w1.Parameters = "tables:CuentaInversion_PatrimonioCajaFechaLiquidacion,CuentaInversion_PatrimonioCajaFechaOperacion,Custodia_PatrimonioConsolidadoFechaLiquidacion,Custodia_PatrimonioConsolidadoFechaOperacion,Custodia_PatrimonioCustodiaFechaLiquidacion,Custodia_PatrimonioCustodiaFechaOperacion;BCS_query:QueryReplicaDelta;Fecha:" + 
						FechaDestino + ";serialDelete:false;truncarDestino:false;serialLaunch:false";
				w1.Status = 0;
				
				list.add(w1);
				
			}  // while (ContinuarCopiaPatrimonio)
		}
		else if (in.p_Proceso.equalsIgnoreCase("PAUTA_RESPALDAR_CUSTODIA")) {
			{
				Workstack w1 = new Workstack();
				w1.Key = "";
				w1.Table = "CARTERAFMHIS ACTUAL";
				w1.Action = "M";
				w1.Parameters = "tables:Custodia_CarteraFMHis;BCS_query:QueryReplicaDelta;Fecha:" + fechaSistemaEjecucion + ";serialDelete:false;truncarDestino:false;serialLaunch:false";
				w1.Status = 0;
				
				list.add(w1);
			}
			
			{
				Workstack w1 = new Workstack();
				w1.Key = "";
				w1.Table = "CARTERARVHIS ACTUAL";
				w1.Action = "M";
				w1.Parameters = "tables:Custodia_CarteraRVHis;BCS_query:QueryReplicaDelta;Fecha:" + fechaSistemaEjecucion + ";serialDelete:false;truncarDestino:false;serialLaunch:false";
				w1.Status = 0;
				
				list.add(w1);
			}
			
			{
				Workstack w1 = new Workstack();
				w1.Key = "";
				w1.Table = "GARANTIAHIS ACTUAL";
				w1.Action = "M";
				w1.Parameters = "tables:Custodia_GarantiaHis;BCS_query:QueryReplicaDelta;Fecha:" + fechaSistemaEjecucion + ";serialDelete:false;truncarDestino:false;serialLaunch:false";
				w1.Status = 0;
				
				list.add(w1);
			}
			
		}
		else if (in.p_Proceso.equalsIgnoreCase("CAMBIO_FECHA_SISTEMA")) {
			{
				Workstack w = new Workstack();
				w.Key = "";
				w.Table = "CUENTA INVERSION DIA SIGUIENTE";
				w.Action = "M";
				w.Parameters = "tables:CuentaInversion_SaldoFechaEfectiva,CuentaInversion_SaldoFechaMovimiento;BCS_query:QueryReplicaDelta;Fecha:" + fechaSistemaEjecucion + ";serialDelete:false;truncarDestino:false;serialLaunch:false";
				w.Status = 0;
				list.add(w);
			}
		}
		else if (in.p_Proceso.equalsIgnoreCase("VALORIZAR_CARTERA_RV_PRECIO_MERCADO")) {
			{
				Workstack w = new Workstack();
				w.Key = "";
				w.Table = "CARTERARV";
				w.Action = "M";
				w.Parameters = "tables:Custodia_CarteraRV;BCS_query:QueryReplica;Fecha:" + fechaSistemaEjecucion + " ;serialDelete:false;truncarDestino:true;serialLaunch:false";
				w.Status = 0;
				list.add(w);
			}
			
		}
		else if (in.p_Proceso.equalsIgnoreCase("FIN_PAUTA")) {
			{
				Workstack w = new Workstack();
				w.Key = "";
				w.Table = "CUSTODIA DIA SIGUIENTE";
				w.Action = "M";
				w.Parameters = "tables:Custodia_CustodiaFechaLiquidacion,Custodia_CustodiaFechaOperacion;BCS_query:QueryReplicaDelta;Fecha:" + fechaSistemaEjecucion + ";serialDelete:false;truncarDestino:false;serialLaunch:false";
				w.Status = 0;
				list.add(w);
				
				Long offset = ConfiguracionSistema.getParametroConfiguracionLong(conn,in.p_IdEmpresa,"PLATAFORMA","PLA.SegundosZonaHoraria");
				ResumenReplica r = new ResumenReplica();
				r.Tipo = "PAUTA";
				r.Estado = "OK";
				r.FechaSistema = Fecha.toDate(fechaSistemaEjecucion);
				r.Nombre = "FIN_PAUTA";
				r.Detalle = "FIN_PAUTA";
				r.UltimaActualizacion = Fecha.toDateTime(obtenerFechaHora("FechaHoraISO",offset));;
				resumen.add(r);
			}
			
			
			//CREATE TRIGGERS
			logger.info("Reactivando replica online");
			try {
				SQL.exec(conn, SQL_CREATE_TRIGGER_CUSTODIA);
				SQL.exec(conn, SQL_CREATE_TRIGGER_CUENTA_INVERSION);
				SQL sql = new SQL();
				logger.info("Insert Fin Pauta");
				sql.batch(conn, SQL_UPSERT_RESUMEN, resumen, ResumenReplica.class);
			}
			catch(Exception e) {
				logger.error("Error al reactivar replica online");
				logger.error(e);
			}
			
			
			logger.info("Limpiando cuenta inversion historica");
			SQL.exec(conn, SQL_DELETE_CUENTA_INVERSION_FECHA_EFECTIVA_HISTORICA.replace(":FechaEjecucion", fechaSistemaEjecucion));
			SQL.exec(conn, SQL_DELETE_CUENTA_INVERSION_FECHA_MOVIMIENTO_HISTORICA.replace(":FechaEjecucion", fechaSistemaEjecucion));
			SQL.exec(conn, SQL_DELETE_CUENTA_INVERSION_FECHA_EFECTIVA_CURRENT.replace(":FechaSaldo", fechaSistemaEjecucion));
			SQL.exec(conn, SQL_DELETE_CUENTA_INVERSION_FECHA_MOVIMIENTO_CURRENT.replace(":FechaSaldo", fechaSistemaEjecucion));
			
			logger.info("Insertando cuenta inversion historica");
			
			SQL.exec(conn, SQL_INSERT_CUENTA_INVERSION_FECHA_EFECTIVA_HISTORICA);
			SQL.exec(conn, SQL_INSERT_CUENTA_INVERSION_FECHA_MOVIMIENTO_HISTORICA);
			
			logger.info("Limpiando custodia historica");
			SQL.exec(conn, SQL_DELETE_CUSTODIA_FECHA_LIQUIDACION_HISTORICA.replace(":FechaEjecucion", fechaSistemaEjecucion));
			SQL.exec(conn, SQL_DELETE_CUSTODIA_FECHA_OPERACION_HISTORICA.replace(":FechaEjecucion", fechaSistemaEjecucion));
			SQL.exec(conn, SQL_DELETE_CUSTODIA_FECHA_LIQUIDACION_CURRENT.replace(":FechaCustodia", fechaSistemaEjecucion));
			SQL.exec(conn, SQL_DELETE_CUSTODIA_FECHA_OPERACION_CURRENT.replace(":FechaCustodia", fechaSistemaEjecucion));
			
			logger.info("Insertando custodia historica");
			SQL.exec(conn, SQL_INSERT_CUSTODIA_FECHA_LIQUIDACION_HISTORICA);
			SQL.exec(conn, SQL_INSERT_CUSTODIA_FECHA_OPERACION_HISTORICA);
						
		}
		else {
			logger.info("Proceso "+ in.p_Proceso+   " no realiza envio de replica");
		}
		
		if (list.size() > 0) {
			SQL sql = new SQL();
			logger.debug("Insertando workstack");
			logger.debug(list);
			sql.batch(conn,SQL_INSERT_WORKSTACK, list, Workstack.class);
		}
		
		return salida;
	}
	
	
	
	public static class Salida_EnviarTablaReplica {
		
	}
	
	public static class Entrada_EnviarTablaReplica {
		public Long p_IdEmpresa;
		public String p_Proceso;
		public HashMap<String,Object> p_Parametros = new HashMap<String, Object>();
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Workstack {
		public String Key;
		public String Table;
		public String Action;
		public int Status;
		public Timestamp Stamp;
		public String Parameters;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	private String obtenerFechaHora(String peticion,Long offset) {
    	
    	String fechaHora = "";
    	ZonedDateTime.now();
    	LocalDateTime localDate = LocalDateTime.now();
    	localDate = localDate.plusSeconds(offset);
    	DateTimeFormatter formatterFechaHora = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    	DateTimeFormatter formatterFechaHoraISO = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    	DateTimeFormatter formatterFecha = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    	DateTimeFormatter formatterHora = DateTimeFormatter.ofPattern("HH:mm:ss");
    	
    	switch(peticion) {
    	case "Fecha":    		
    		fechaHora = localDate.format(formatterFecha);
    		break;
    	case "FechaHoraISO":    		
    		fechaHora = localDate.format(formatterFechaHoraISO);
    		break;
    	case "Hora":
    		fechaHora = localDate.format(formatterHora);
    		break;    	
    	case "Ambos":
    		fechaHora = localDate.format(formatterFechaHora);
    		break;    		
    	}
    	
    	return fechaHora;    	
    }
	
	public List<Workstack> respaldarDataDiaInhabil (Connection con, Long idEmpresa, String fechaOrigen, String tablaWorkstack, String parametrosWorkstack) throws Exception {
		logger.info("INICIO::respaldarDataDiaInhabil");
		
		List<Workstack> list = new ArrayList<Workstack>();
		ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
		InformacionEmpresa empresa = consultarEmpresaService.logica(con, idEmpresa);
		Long idPaisEmpresa = empresa.IdPais;
		
		String FechaDestino = fechaOrigen;
		Boolean ContinuarRespaldoDiaInhabil = true;
		logger.debug("Respaldar Replica > --- " + tablaWorkstack + "---");
		logger.debug("FechaOrigen > --- " + fechaOrigen + "---");
		
		while (ContinuarRespaldoDiaInhabil) {
			ObtenerDiaSiguiente obtenerDiaSiguiente = new ObtenerDiaSiguiente();
			InObtenerDiaSiguiente  inObtenerDiaSiguiente = new InObtenerDiaSiguiente();

			List<FechasIn> lstFechas = new ArrayList<FechasIn>();
			FechasIn inFechas = new FechasIn();
			inFechas.fechaInicial 	= FechaDestino;
			inFechas.numeroDias 	= 1L;
			inFechas.pais 			= idPaisEmpresa;
			lstFechas.add(inFechas);
			
			inObtenerDiaSiguiente.p_idPais 			= idPaisEmpresa;
			inObtenerDiaSiguiente.p_diaHabil 		= "N";
			inObtenerDiaSiguiente.p_soloCaeDiaHabil = "N";
			inObtenerDiaSiguiente.p_fechas 			= lstFechas;
			
			OutObtenerDiaSiguiente outObtenerDiaSiguiente = obtenerDiaSiguiente.logica(con, inObtenerDiaSiguiente);
				
			if (outObtenerDiaSiguiente.P_OUT_ERRORESCONTADOR.compareTo(0L) != 0) {
				ContinuarRespaldoDiaInhabil = false;		
				logger.error("Error al obtener dia siguiente");
				break;
			}

			FechaDestino = outObtenerDiaSiguiente.p_out_fechas.get(0).fechaCalculada;
			logger.debug("FechaDestino > --- " + FechaDestino + "---");
			
			Boolean esDiaHabil = Feriados.esDiaHabil(con, FechaDestino, idPaisEmpresa);
			logger.debug("EsDiaHabil > " + esDiaHabil + (esDiaHabil ? " > Entonces Termina proceso sin respaldar "+tablaWorkstack+ " para "+FechaDestino : " > Entonces Copiar " + tablaWorkstack+ " Desde "+fechaOrigen+ " --> Hacia "+FechaDestino));
			if (esDiaHabil) {
				ContinuarRespaldoDiaInhabil = false;		
				break;
			}
			
			Workstack w = new Workstack();
			w.Key = "";
			w.Table = tablaWorkstack;
			w.Action = "M";
			w.Parameters = parametrosWorkstack + FechaDestino + ";serialDelete:false;truncarDestino:false;serialLaunch:false";
			w.Status = 0;
			
			list.add(w);
			
			SQL sql = new SQL();
			
			List<FechaRespaldo> respaldos = new ArrayList<>();
			FechaRespaldo respaldoFechas = new FechaRespaldo();
			respaldoFechas.FechaOrigen = fechaOrigen;
			respaldoFechas.FechaDestino = FechaDestino;
			respaldos.add(respaldoFechas);
			
			if("CUSTODIA ACTUAL DIA INHABIL".equals(tablaWorkstack)) {
				logger.debug("Se limpian registros para "+respaldoFechas.FechaDestino);
				SQL.exec(con, SQL_DELETE_CUSTODIA_FECHA_LIQUIDACION_CURRENT.replace(":FechaCustodia", respaldoFechas.FechaDestino));
				SQL.exec(con, SQL_DELETE_CUSTODIA_FECHA_OPERACION_CURRENT.replace(":FechaCustodia", respaldoFechas.FechaDestino));
				logger.error("Se respaldan los registros para "+respaldoFechas.FechaDestino);
				sql.batch(con, SQL_INSERT_CUSTODIA_FECHA_LIQUIDACION_DIA_INHABIL, respaldos, FechaRespaldo.class);
				sql.batch(con, SQL_INSERT_CUSTODIA_FECHA_OPERACION_DIA_INHABIL, respaldos, FechaRespaldo.class);		
			} else if("CUENTA INVERSION DIA INHABIL".equals(tablaWorkstack)){
				logger.debug("Se limpian registros para "+respaldoFechas.FechaDestino);
				SQL.exec(con, SQL_DELETE_CUENTA_INVERSION_FECHA_EFECTIVA_CURRENT.replace(":FechaSaldo", respaldoFechas.FechaDestino));
				SQL.exec(con, SQL_DELETE_CUENTA_INVERSION_FECHA_MOVIMIENTO_CURRENT.replace(":FechaSaldo", respaldoFechas.FechaDestino));
				logger.debug("Se respaldan los registros para "+respaldoFechas.FechaDestino);
				sql.batch(con, SQL_INSERT_CUENTA_INVERSION_FECHA_EFECTIVA_DIA_INHABIL, respaldos, FechaRespaldo.class);
				sql.batch(con, SQL_INSERT_CUENTA_INVERSION_FECHA_MOVIMIENTO_DIA_INHABIL, respaldos, FechaRespaldo.class);
			}
		}
		
		logger.info("FIN::respaldarDataDiaInhabil");
		return list;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection conn = PlataformaUtils.getPropertiesConnection("fynsapre","connections.properties")) {
			EnviarTablaReplica r = new EnviarTablaReplica();
			Entrada_EnviarTablaReplica in = new Entrada_EnviarTablaReplica();
			in.p_IdEmpresa = 1L;
			in.p_Proceso = "EJECUTAR_PROCESOS_DIAINHABIL";
			in.p_Parametros = new HashMap<String, Object>();
			in.p_Parametros.put("FechaSistemaEjecucion", "2024-12-16");
			Salida_EnviarTablaReplica asdf = r.logica(conn, in);
			conn.commit();
			System.out.println(asdf.toString());
		}
	}
}
