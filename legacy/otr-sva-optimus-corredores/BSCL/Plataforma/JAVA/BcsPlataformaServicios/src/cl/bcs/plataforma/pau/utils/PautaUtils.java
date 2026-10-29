package cl.bcs.plataforma.pau.utils;

import java.sql.Connection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import cl.bcs.plataforma.bat.AgregarPrograma;
import cl.bcs.plataforma.evt.NotificarAdministrador;
import cl.bcs.plataforma.pau.AdelantarPauta;
import cl.bcs.plataforma.pau.AgregarControlPauta;
import cl.bcs.plataforma.pau.AgregarControlProceso;
import cl.bcs.plataforma.pau.ConsultarPautaEjecutar;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class PautaUtils {
	
	//QUERIES
	private final String SQL_CONSULTAR_FERIADOS = "SELECT \"IdFeriado\",\"Tipo\",\"Fecha\",\"Dia\",\"DescripcionDia\",\"AgnoCarga\",\"IdPais\" FROM \"BSCL.Plataforma.BD::REG.DiaFeriado\" WHERE \"IdPais\" IN (:IdPais) AND \"Fecha\" IN (:FechaConsulta) AND \"IdEstado\" = 'ACT'";
	
	public static class DiaFeriado {
		public Long			IdFeriado; 
		public String		Tipo;  
		public java.sql.Date Fecha; 
		public Integer		Dia; 
		public String		DescripcionDia; 
		public Integer		AgnoCarga; 
		public Long			IdPais; 
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static void verificarProcesoAnterior(ConsultarPautaEjecutar.ConsultarPautaEjecutarSalida pejout, String mensajeLog,
												   Connection con, Log log, Long parIdPrograma, String idTipoPrograma, String ejecucion,
												   String confFechaSistema, String notificacionMensaje, String notificacionEsError) throws Exception{

		if(pejout.P_OUT_ERRORESCONTADOR>0){
			mensajeLog = "Error al ejecutar: ConsultarPautaEjecutar";
			AgregarControlPauta.logPauta(con,log,parIdPrograma,idTipoPrograma,ejecucion,confFechaSistema,"ERR",mensajeLog);
			notificacionMensaje=notificacionMensaje+"\n"+mensajeLog;
			notificacionEsError="S";
			throw new InterruptedException(mensajeLog);
		}
		if(pejout.P_OUT_PAUTAANTERIOR!=null && !pejout.P_OUT_PAUTAANTERIOR.IdEstadoPrograma.equals("TER")){
			mensajeLog = "No se ha ejecutado o no ha finalizado proceso de pauta anterior: "+pejout.P_OUT_IDTIPOPROGRAMAANTERIOR;
			AgregarControlPauta.logPauta(con,log,parIdPrograma,idTipoPrograma,ejecucion,confFechaSistema,"PEN",mensajeLog);
			notificacionMensaje=notificacionMensaje+"\n"+mensajeLog;
			notificacionEsError="S";
			throw new InterruptedException(mensajeLog);
		}
	}
	
	public static void verificarProcesoPosterior(ConsultarPautaEjecutar.ConsultarPautaEjecutarSalida pejout, String mensajeLog,
			   Connection con, Log log, Long parIdPrograma, String idTipoPrograma, String ejecucion,
			   String confFechaSistema, String notificacionMensaje, String notificacionEsError,
			   String codigoUsuario, Long parIdEmpresa, Long parIdUsuario) throws Exception{

		AgregarControlPauta.logPauta(con,log,parIdPrograma,idTipoPrograma,ejecucion,confFechaSistema,"PEN","Verificando proceso siguiente de pauta.");
		if(pejout.P_OUT_PAUTASIGUIENTE!=null && pejout.P_OUT_PAUTASIGUIENTE.IdPrograma>0){
			AgregarControlPauta.logPauta(con,log,parIdPrograma,idTipoPrograma,ejecucion,confFechaSistema,"PEN","Existe programa para adelantar ejecución: IdProgama="+pejout.P_OUT_PAUTASIGUIENTE.IdPrograma
					+", Tipo Programa="+pejout.P_OUT_PAUTASIGUIENTE.IdTipoPrograma);
			if(!pejout.P_OUT_PAUTASIGUIENTE.IdEstadoPrograma.equals("PEN")){
				AgregarControlPauta.logPauta(con,log,parIdPrograma,idTipoPrograma,ejecucion,confFechaSistema,"PEN","ADVERTENCIA!: Pauta a adelantar no se encuentra pendiente.");
			}
			else{//Adelantar ejecución pauta:
				AdelantarPauta adp = new AdelantarPauta();
				AdelantarPauta.AdelantarPautaEntrada adpin = new AdelantarPauta.AdelantarPautaEntrada();
			   	AdelantarPauta.AdelantarPautaEntrada.Parametro param = new AdelantarPauta.AdelantarPautaEntrada.Parametro(); 
			   	param.NombreParametro = "codigoUsuario";
			   	param.Valor = codigoUsuario;
				adpin.p_IdEmpresa = parIdEmpresa;
				adpin.p_IdUsuario = parIdUsuario;
				adpin.p_IdModulo = "PLA";
				adpin.p_IdPrograma = pejout.P_OUT_PAUTASIGUIENTE.IdPrograma;
				adpin.p_Segundos = new Long(10);
				adpin.p_Parametros.add(param);
				AdelantarPauta.AdelantarPautaSalida adpout = adp.logica(con, adpin);
				if(adpout.P_OUT_ERRORESCONTADOR>0){
					mensajeLog="ADVERTENCIA!: Error al adelantar pauta: "+pejout.P_OUT_IDTIPOPROGRAMASIGUIENTE;
					AgregarControlPauta.logPauta(con,log,parIdPrograma,idTipoPrograma,ejecucion,confFechaSistema,"PEN",mensajeLog);
					notificacionMensaje=notificacionMensaje+"\n"+mensajeLog;
				}
				else{
					mensajeLog="Pauta adelantada con éxito: "+pejout.P_OUT_IDTIPOPROGRAMASIGUIENTE;
					AgregarControlPauta.logPauta(con,log,parIdPrograma,idTipoPrograma,ejecucion,confFechaSistema,"PEN",mensajeLog);
					notificacionMensaje=notificacionMensaje+"\n"+mensajeLog;
				}
			}
		}else if(pejout.P_OUT_IDTIPOPROGRAMASIGUIENTE!=null){
			mensajeLog="No hay programa para adelantar ejecución";
			AgregarControlPauta.logPauta(con,log,parIdPrograma,idTipoPrograma,ejecucion,confFechaSistema,"PEN",mensajeLog);
			notificacionMensaje=notificacionMensaje+"\n"+mensajeLog;
			//crear nuevo programa
			mensajeLog="Creando nuevo programa";
			AgregarControlPauta.logPauta(con,log,parIdPrograma,idTipoPrograma,ejecucion,confFechaSistema,"PEN",mensajeLog);
			notificacionMensaje=notificacionMensaje+"\n"+mensajeLog;
		   	AgregarPrograma agrprg = new AgregarPrograma();
		   	AgregarPrograma.InAgregarPrograma inagrprg = new AgregarPrograma.InAgregarPrograma();
		   	AgregarPrograma.InAgregarPrograma.Parametro param = new AgregarPrograma.InAgregarPrograma.Parametro(); 
		   	param.NombreParametro = "codigoUsuario";
		   	param.Valor = codigoUsuario;
		   	inagrprg.p_IdEmpresa = parIdEmpresa;
		   	inagrprg.p_IdUsuario = parIdUsuario;
		   	inagrprg.p_IdModulo = "PLA";
		   	inagrprg.p_FechaSistema=confFechaSistema;//pese a que adelantó, se loguea programa con misma fecha ejecución del proceso(visualizar pauta)
		   	inagrprg.p_IdTipoPrograma = pejout.P_OUT_IDTIPOPROGRAMASIGUIENTE;
		   	inagrprg.p_Segundos = 30L; //delay de 30 segundos.		
		   	inagrprg.p_Parametros.add(param);
		   	AgregarPrograma.OutAgregarPrograma outagrprg = agrprg.logica(con, inagrprg);
		   	if(outagrprg.P_OUT_ERRORESCONTADOR>0){
		   		mensajeLog="ADVERTENCIA!: Error al adelantar pauta: "+pejout.P_OUT_IDTIPOPROGRAMASIGUIENTE;
				AgregarControlPauta.logPauta(con,log,parIdPrograma,idTipoPrograma,ejecucion,confFechaSistema,"PEN",mensajeLog);
				notificacionMensaje=notificacionMensaje+"\n"+mensajeLog;
		   	}else{
		   		mensajeLog="Pauta adelantada con éxito: "+pejout.P_OUT_IDTIPOPROGRAMASIGUIENTE;
				AgregarControlPauta.logPauta(con,log,parIdPrograma,idTipoPrograma,ejecucion,confFechaSistema,"PEN",mensajeLog);
				notificacionMensaje=notificacionMensaje+"\n"+mensajeLog;
		   	}
		}else {
			mensajeLog="No hay tipo programa configurado para adelantar";
			AgregarControlPauta.logPauta(con,log,parIdPrograma,idTipoPrograma,ejecucion,confFechaSistema,"PEN",mensajeLog);
			notificacionMensaje=notificacionMensaje+"\n"+mensajeLog;
		}
	}
	
	public static void notificarUsuariosTI(String tipoPrograma, Long notificacionIdEmpresa, String notificacionEsError,
										   String notificacionMensaje, Long parIdPrograma, Connection con, Log log) throws Exception{
		NotificarAdministrador notiadmin = new NotificarAdministrador();
		NotificarAdministrador.NotificarAdministradorEntrada innotiadmin = new NotificarAdministrador.NotificarAdministradorEntrada();
		innotiadmin.NombreProceso=tipoPrograma;
		innotiadmin.IdEmpresa=notificacionIdEmpresa;
		innotiadmin.EsError=notificacionEsError;
		notificacionMensaje=notificacionMensaje+"\n\n"+"Id Programa: "+parIdPrograma;
		innotiadmin.Mensaje=notificacionMensaje.replace("\n", "<br>-").replace("<br>-<br>", "<br>");
		notiadmin.logica(con, innotiadmin);	
	}
	
	public static void agregarLogProcesosPauta(String tipoPrograma, String codigoUsuario, String confFechaSistema,
											   Long parIdEmpresa, String mensajeLog, Connection con, String idTipoPrograma,
											   String ejecucion, Log log, Long parIdPrograma, String notificacionMensaje,
											   String notificacionEsError) throws Exception{
		AgregarControlProceso agregarControlProceso = new AgregarControlProceso();
		AgregarControlProceso.ControlProcesoEntrada controlProcesoEntrada = new AgregarControlProceso.ControlProcesoEntrada();
		controlProcesoEntrada.CodigoModulo = "SIS";
		controlProcesoEntrada.CodigoProceso = tipoPrograma;
		controlProcesoEntrada.CodigoUsuario = codigoUsuario;
		controlProcesoEntrada.FechaSistema = confFechaSistema;
		controlProcesoEntrada.IdEmpresa = parIdEmpresa;
		AgregarControlProceso.ControlProcesoSalida respuestaControlProceso = agregarControlProceso.logica(con, controlProcesoEntrada);
		if (respuestaControlProceso.P_OUT_ERRORESCONTADOR > 0){
			mensajeLog = "Error al agregar log control de proceso: "+(respuestaControlProceso.P_OUT_MENSAJE);
			AgregarControlPauta.logPauta(con,log,parIdPrograma,idTipoPrograma,ejecucion,confFechaSistema,"ERR",mensajeLog);
			notificacionMensaje=notificacionMensaje+"\n"+mensajeLog;
			notificacionEsError="S";
			throw new InterruptedException(mensajeLog);
		}
	}
	
	 public LocalDate obtenerUltimoDiaDelMes(LocalDate fecha) {	        
	        LocalDate ultimoDiaDelMes = fecha.withDayOfMonth(fecha.lengthOfMonth());
	        return ultimoDiaDelMes;
	    }
	 
	 public String obtenerUltimoDiaDelMesString(String fecha) {	  
		 
		    String [] FechaCalculada = fecha.split("-");	
		 	Integer Agno = Integer.parseInt(FechaCalculada[0]);
			Integer Mes = Integer.parseInt(FechaCalculada[1]);
			Integer Dia = Integer.parseInt(FechaCalculada[2]);
			
			LocalDate ultimoDia =this.obtenerUltimoDiaDelMes(LocalDate.of( Agno, Mes, Dia));
	        return ultimoDia.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")); 
	    }
	 
	 public String obtenerSiguienteDiaHabil (Connection con,Long IdPais, String fechaSistema) throws Exception {
		//calcular dia siguiente habil
	        List<InObtenerDiaSiguiente.FechasIn> FechasDiaSiguiente = new ArrayList<InObtenerDiaSiguiente.FechasIn>();
	        InObtenerDiaSiguiente.FechasIn FDS = new InObtenerDiaSiguiente.FechasIn();
	        FDS.fechaInicial = fechaSistema;
			FDS.numeroDias	= 1L;
			FechasDiaSiguiente.add(FDS);
			
			InObtenerDiaSiguiente ObtenerDiaSiguiente = new InObtenerDiaSiguiente();
			ObtenerDiaSiguiente.p_idPais = IdPais;
			ObtenerDiaSiguiente.p_diaHabil = "S";
			ObtenerDiaSiguiente.p_fechas = FechasDiaSiguiente;
			
			
			ObtenerDiaSiguiente diaSiguiente = new ObtenerDiaSiguiente();
			ObtenerDiaSiguiente.OutObtenerDiaSiguiente proximoDiaHabil = diaSiguiente.logica(con, ObtenerDiaSiguiente);			
			
			return proximoDiaHabil.p_out_fechas.get(0).fechaCalculada;
	 }
	 
	 

	public Boolean ejecutarDiaInhabil(Connection con, String fechaSistema, Long	idEmpresa) throws Exception {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa(); // se puede reemplazar por el parametro
		InformacionEmpresa empresa = consultarEmpresaService.logica(con, idEmpresa);
		SQL sql=new SQL();
		Boolean esInhabil = false;
		
		//Dia de ejecucion Pauta		
		String [] FechaSistema = fechaSistema.split("-");	
		     
        //Calcula ultimo dia del mes
		Integer Agno = Integer.parseInt(FechaSistema[0]);
		Integer Mes = Integer.parseInt(FechaSistema[1]);
		Integer Dia = Integer.parseInt(FechaSistema[2]);
		LocalDate ultimoDia =this.obtenerUltimoDiaDelMes(LocalDate.of( Agno, Mes, Dia)); 
		String ultimoDiaString = ultimoDia.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));        
        String siguienteDiaHabil = this.obtenerSiguienteDiaHabil(con, empresa.IdPais, fechaSistema);        
 
		HashMap<String,Object> parmsFeriado = new HashMap<String,Object>();
		parmsFeriado.put("IdPais", empresa.IdPais);
		parmsFeriado.put("FechaConsulta", Fecha.toDate(ultimoDiaString));
		List<DiaFeriado> listaDiaFeriado = sql.query(con,SQL_CONSULTAR_FERIADOS,parmsFeriado,DiaFeriado.class);

		if(!listaDiaFeriado.isEmpty() &&  (Fecha.toDate(ultimoDiaString)).before(Fecha.toDate(siguienteDiaHabil))) {			
			esInhabil=true;			
		}	
		
		if(("SUNDAY".equals(ultimoDia.getDayOfWeek().toString()) || "SATURDAY".equals(ultimoDia.getDayOfWeek().toString())) && (Fecha.toDate(ultimoDiaString)).before(Fecha.toDate(siguienteDiaHabil))) {
			esInhabil=true;	
		}
        
		return esInhabil;
	}
	public Boolean valorizarCuponFeriado(Connection con, String fechaSistema, Long	idEmpresa) throws Exception {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		SQL sql=new SQL();
		
		ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
		InformacionEmpresa empresa = consultarEmpresaService.logica(con, idEmpresa);
	    HashMap<String,Object> parmsFeriado = new HashMap<String,Object>();
	    parmsFeriado.put("IdPais", empresa.IdPais);
		
		Date fechaEnDate=format.parse(fechaSistema);
		Calendar hoy = Calendar.getInstance();
		hoy.setTime(fechaEnDate);
		String DiaDeSemanaHoy = hoy.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.US).toUpperCase(); 
		if("FRIDAY".equals(DiaDeSemanaHoy)) {
        	return true;
		}else {
			hoy.add(Calendar.DAY_OF_YEAR, 1);
			String siguienteDiaString = format.format(hoy.getTime());
			parmsFeriado.put("FechaConsulta", siguienteDiaString);
			List<DiaFeriado> SiguienteDiaEsFeriado = sql.query(con,SQL_CONSULTAR_FERIADOS,parmsFeriado,DiaFeriado.class);
			if(SiguienteDiaEsFeriado.isEmpty()){
				return false;
			}else {
				return true;
			}
		}	
	}
	
	public static void main(String [] args) throws Throwable {
		
		PautaUtils pautaUtil = new PautaUtils();
		String fechaSistemaPrueba = "2024-08-30";
		
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa02","c:/archivos/connections.properties")) {
			boolean esInhabil = pautaUtil.ejecutarDiaInhabil(conn,fechaSistemaPrueba,1L);
			conn.rollback();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
}

	
