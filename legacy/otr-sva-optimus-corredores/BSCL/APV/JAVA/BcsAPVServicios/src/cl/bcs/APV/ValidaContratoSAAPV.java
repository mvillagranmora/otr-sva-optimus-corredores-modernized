package cl.bcs.APV;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.APV.ProcesarDistribucion.Recaudacion;
//import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo;
//import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ValidaContratoSAAPV implements JavaCall{	
	
	static final Logger logger = LogManager.getLogger(ValidaContratoSAAPV.class);

	private final String SQL_UPDATE_MODIFICA_CONTRATOS = loadResourceToString("cl/bcs/APV/sql/ModificaIdsContratoSAAPV.sql");
	private final String SQL_SELECT_VALIDA_RECAUDACION= loadResourceToString("cl/bcs/APV/sql/ValidacionSAAPV_validaRecaudacion.sql");
	private final String SQL_SELECT_TRASPASO_REAPERTURA = loadResourceToString("cl/bcs/APV/sql/ValidaContrato_SelectDetallesTraspaso.sql");
	
	public String SQL_ConsultarExistenciaPersona = loadResourceToString("cl/bcs/APV/sql/ValidaContratoSAAPV_SelectDocumentoIdentidad.sql");
	public String SQL_ConsultarExistenciaCliente = "SELECT \"IdCliente\",\"IdPersona\" FROM \"BSCL.SocioNegocio.BD::CLI.Cliente\" WHERE \"IdPersona\" = :IdPersona";
	public String SQL_ConsultarExistenciaPortafolio = loadResourceToString("cl/bcs/APV/sql/ConsultarPortafolioMatrizAgrupadorDisponible.sql");
	public String SQL_ConsultarParametroAPV = loadResourceToString("cl/bcs/APV/sql/ConsultarParametrosAPV.sql");
	public String SQL_ConsultarExistenciaContrato = "SELECT \"FolioContratoSAAPV\" FROM \"BSCL.APV.BD::SCS.ContratoSAAPV\" WHERE \"FolioContratoSAAPV\" = :FolioContratoSAAPV AND \"FolioContratoSAAPV\" != ''";
	//public String SQL_ConsultarExistenciaPortafolio = loadResourceToString("cl/bcs/APV/sql/ConsultarPortafolioMatrizAgrupadorDisponible.sql");
	//public String SQL_ValidoContratoSAAPV = "SELECT \"IdPersona\",\"IdCliente\",\"IdPortafolio\",\"IdEmpleador\" FROM \"BSCL.APV.BD::SCS.ContratoSAAPV\" WHERE \"FolioContratoSAAPV\" = :FolioContratoSAAPV";
	
	public static class ContratosSAAPV {
		public Long		IdPersona;
		public Long		IdCliente;
		public Long		IdEmpleador;
		public Long 	IdPortafolio;
		public String	DocumentoIdentidad;
		public String	DocumentoIdentidadEmpleador;
		public String	FolioContratoSAAPV;
		public String	Codigo;
		public String 	NombrePlan;	
		public String   CodigoAgrupador;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};

	public static class RecaudacionCliente {
		public Long			IdRecaudacion;
		public Long			IdRecaudacionEmpleador;
		public Long			IdRecaudacionCliente;
		public BigDecimal	Monto;
		public String		IdEstadoRecaudacionCliente;
		public String		Pago;
	};
	
	public static class ValidaContratoSAAPV_Entrada {
	    public Long				p_IdEmpresa;
	    public Long				p_IdUsuario;
	    public String			p_IdModulo;
	    public String			p_Opcion;
	    public List<ContratosSAAPV>	p_ContratosSAAPV;
	    public List<RecaudacionCliente>	p_RecaudacionCliente;
	    public List<Long>		p_IdsDetalleTraspasoReapertura;
	    
	    public ValidaContratoSAAPV_Entrada(){
	    	p_ContratosSAAPV = new ArrayList<ContratosSAAPV>();
	    	p_ContratosSAAPV.add(null);
	    	p_RecaudacionCliente = new ArrayList<RecaudacionCliente>();
	    	p_IdsDetalleTraspasoReapertura = new ArrayList<Long>();
		}
	   
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class ValidaContratoSAAPV_Salida {		
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		public List<ModificaContratoSAAPV> P_IDSCONTRATO;
		public List<DetalleValidacionContratoSAAPV> P_IDSRECAUDACION;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public ValidaContratoSAAPV_Salida(){
			P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			P_IDSCONTRATO = new ArrayList<ModificaContratoSAAPV>(); 
			P_IDSRECAUDACION = new ArrayList<DetalleValidacionContratoSAAPV>(); 
		}		
	};

	public static class ConsultaExistePersona{
		public long   IdPersona;
		public String DocumentoIdentidad;
	}
	
	public static class ConsultaExisteCliente{
		public long IdCliente;
		public long IdPersona;
	}

	public static class ConsultaExistePortafolio{
		public  long		IdPersona;
	    public 	String		Codigo;
	    public  long		IdPortafolio;
	    public  long		IdCliente;
	    public  String		NombreAgrupador;
	    public  long		IdMatrizAgrupadorAPV;
	    public  String		CodigoAgrupador;
	}
	
	public static class ConsultaExisteFolioContrato{
		public String FolioContratoSAAPV;
	}
	
	public static class ModificaContratoSAAPV{
		public Long IdPersona;
		public Long IdCliente;
		public Long IdPortafolio;
		public Long IdEmpleador;
		public String FolioContratoSAAPV;
		public String Descripcion;
		public String Codigo;
	}

	public static class ConsultaValidaRecaudacion{
		public long   IdRecaudacionCliente;
	}
	
	public static class RecaudacionValidada{
		public long			FolioRecaudacion;
		public long			IdRecaudacionEmpleador;
		public long			IdRecaudacionCliente;
		public long			IdPortafolio;
		public long			IdContratoSAAPV;
		public String		Validacion_Empleador;
		public String		Validacion_FechaTermino;
		public String		Validacion_Monto;
		public String Validacion_FondoRestringido;
		public String 	    TipoRecaudacion;
		public String		RutEmpleadorEmpleador;
		public String		RutEmpleadorContrato;
		public String		Pago;
		public String		PeriodoTermino;
		public String		FormaDePago;
		public BigDecimal	RecMonto;
		public BigDecimal	MontoAhorroCV;
		public BigDecimal	RecMontoDC;
		public BigDecimal	MontoAhorroDC;
		public String		RegimenTributario;
		public String		IdEstadoPortafolio;
		public String		IdEstadoCliente;
		public String		IdEstadoContrato;
		public String		Asignado;		
	}

	public static class DetalleValidacionContratoSAAPV{
		public long			FolioRecaudacion;
		public long			IdRecaudacionEmpleador;
		public long			IdRecaudacionCliente;
		public String		IdEstadoRecaudacionCliente;
		public long			IdContrato;
		public long			IdPortafolio;
		public String		IdRegimen;
		public String		ValidacionOK;
		public BigDecimal	Monto;
		public String		DetalleValidacion;
		public String		Pago;
		
	}

	public static class ParametrosAPV{
		public long			IdParametro;
		public String		NombreParametro;
		public String		ValorParametro;
		
	}
	
	public static class TraspasoReapertura{
		public long 	IdPortafolio;
		public long 	IdContratoSAAPV;
		public String	Codigo;  
		public String	EsReapertura;	
	}

	@SuppressWarnings("unused")
	public ValidaContratoSAAPV_Salida logica(Connection conn, ValidaContratoSAAPV_Entrada in) throws Exception {
		
		logger.info("Inicio ValidaContratoSAAPV");
		ValidaContratoSAAPV_Salida out = new ValidaContratoSAAPV_Salida();
		SQL sql = new SQL();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		String periodoSistema = fechaSistema.substring(0, 7);
		/*Parametro para saber si se autoriza por el blotter de autorizacion o no.*/
		String envioBlotterAutorizacion = ConfiguracionSistema.getParametroConfiguracionString(conn,in.p_IdEmpresa, "PLATAFORMA","APV.AutorizaGestionContrato");
		
		try {
			//******************************************************************************************************************
			//													LOGICA
			//******************************************************************************************************************
			
			//recibo lista de contratos
			if(in.p_ContratosSAAPV.size() > 0){
				for (ContratosSAAPV fila :in.p_ContratosSAAPV){
					ContratosSAAPV Ids = new ContratosSAAPV();
					Ids.FolioContratoSAAPV = fila.FolioContratoSAAPV;
					HashMap<String, Object> params;
					String strDescripcion = "";
					
					if(fila.IdPersona == null){
						//valida Persona
						params = new HashMap<>();
						params.put("DocumentoIdentidad", fila.DocumentoIdentidad);
						params.put("TipoPersona", "N");
						List<ConsultaExistePersona> contadorPersona = sql.query(conn, SQL_ConsultarExistenciaPersona, params, ConsultaExistePersona.class);
						boolean existePersona = (contadorPersona.isEmpty()) ? false : true;
						if(existePersona){
							Ids.IdPersona = contadorPersona.get(0).IdPersona;	
						}												
					}else{
						Ids.IdPersona = fila.IdPersona;
					}

					if(fila.IdEmpleador == null && !("").equals(fila.DocumentoIdentidadEmpleador)){
						//valida Persona
						params = new HashMap<>();
						params.put("DocumentoIdentidad", fila.DocumentoIdentidadEmpleador);
						params.put("TipoPersona", "J");
						List<ConsultaExistePersona> contadorPersona = sql.query(conn, SQL_ConsultarExistenciaPersona, params, ConsultaExistePersona.class);
						boolean existePersonaEmp = (contadorPersona.isEmpty()) ? false : true;
						if(existePersonaEmp){
							Ids.IdEmpleador = contadorPersona.get(0).IdPersona;	
						}												
					}
					
					if(fila.IdCliente == null){
						//valida Cliente	
						params = new HashMap<>();
						params.put("IdPersona", Ids.IdPersona);
						List<ConsultaExisteCliente> contadorCliente = sql.query(conn, SQL_ConsultarExistenciaCliente, params, ConsultaExisteCliente.class);
						boolean existeCliente = (contadorCliente.isEmpty()) ? false : true;
						if(existeCliente){
							Ids.IdCliente = contadorCliente.get(0).IdCliente;					
						}						
					}
		
					if(fila.IdPortafolio == null){
						//valido Portafolio						
						params = new HashMap<>();
						params.put("IdPersona", Ids.IdPersona);
						params.put("CodigoRegimen", fila.Codigo);
						params.put("NombrePlan", fila.NombrePlan);
						params.put("CodigoAgrupador", fila.CodigoAgrupador);
						List<ConsultaExistePortafolio> contadorPortafolio = sql.query(conn, SQL_ConsultarExistenciaPortafolio, params, ConsultaExistePortafolio.class);
						boolean existePortafolio = (contadorPortafolio.isEmpty()) ? false : true;
						if(existePortafolio && contadorPortafolio.size() == 1){
							Ids.IdPortafolio = contadorPortafolio.get(0).IdPortafolio;
							Ids.Codigo = fila.Codigo;
						}
						if(contadorPortafolio.size() > 1){
							strDescripcion = "Existe mas de un Portafolio con proposito APV.";
						}
					}
					
					
					//Validacion de FolioContratoSAAPV si existe o no cuando es un Ingreso o Reapertura
					if(in.p_Opcion.equals("I") || in.p_Opcion.equals("R")) {
						if(fila.FolioContratoSAAPV != null || !fila.FolioContratoSAAPV.equals("")) {
							params = new HashMap<>();
							params.put("FolioContratoSAAPV", fila.FolioContratoSAAPV);
							
							List<ConsultaExisteFolioContrato> contadorFolioContrato = sql.query(conn, SQL_ConsultarExistenciaContrato, params, ConsultaExisteFolioContrato.class);
							boolean existeFolioContrato = (contadorFolioContrato.isEmpty()) ? false : true;
							if(existeFolioContrato){
								Ids.FolioContratoSAAPV = fila.FolioContratoSAAPV;
							}else {
								Ids.FolioContratoSAAPV = "";
							}
						}else {Ids.FolioContratoSAAPV = "";}
					}
					
					//actualizo contratos saapv segun Ids
					System.out.println("Ingreso hace actualizacion de contratos saapv.");
					
					ModificaContratoSAAPV mod = new ModificaContratoSAAPV(); 
					//List<ModificaContratoSAAPV> Parametros = new ArrayList<ModificaContratoSAAPV>();
					mod.IdPersona = Ids.IdPersona != null ? Ids.IdPersona : null;
					mod.IdEmpleador = Ids.IdEmpleador  != null ? Ids.IdEmpleador : null;
					mod.IdCliente = Ids.IdCliente != null ? Ids.IdCliente : null;
					mod.IdPortafolio = Ids.IdPortafolio != null ? Ids.IdPortafolio : null;
					mod.FolioContratoSAAPV = Ids.FolioContratoSAAPV;
					mod.Descripcion = strDescripcion;							
					mod.Codigo = Ids.Codigo;
					
					out.P_IDSCONTRATO.add(mod);
					
					if(in.p_Opcion.compareTo("M") == 0 ){
						sql.batch(conn, SQL_UPDATE_MODIFICA_CONTRATOS, out.P_IDSCONTRATO, ModificaContratoSAAPV.class);
					}
				}	
			}
			

			HashMap<String,Object> ParamsRecaudacion = new HashMap<String,Object>();
			//ParamsRecaudacion.put("FechaDeposito", FechaSistema);

			ArrayList<Long> idsRecaudacion = new ArrayList<Long>();
			HashMap<String,Object> parametrosValidaContratoRecaudacion = new HashMap<String,Object>();
			if(in.p_RecaudacionCliente != null) {
				for(RecaudacionCliente fila : in.p_RecaudacionCliente){
					//parametrosValidaContratoRecaudacion.put("IdRecaudacionCliente", fila.IdRecaudacionCliente);
					idsRecaudacion.add(fila.IdRecaudacionCliente);
					
				}
			}
			parametrosValidaContratoRecaudacion.put("IdsRecaudacionCliente", idsRecaudacion);
			parametrosValidaContratoRecaudacion.put("PeriodoSistema", periodoSistema);
			
			List<RecaudacionValidada> TotalRecaudacionValidada = sql.query(conn, SQL_SELECT_VALIDA_RECAUDACION, parametrosValidaContratoRecaudacion, RecaudacionValidada.class);
	
			
			
			//recibo lista de detallecliente de recaudacion
			if(in.p_RecaudacionCliente != null && in.p_RecaudacionCliente.size() > 0){
				if(!in.p_Opcion.equals("RECTRA")){
					List<DetalleValidacionContratoSAAPV> detalleValSAAPV = new ArrayList<DetalleValidacionContratoSAAPV>();

					String valorValEMPLEADOR = ConfiguracionSistema.getParametroConfiguracionString(conn,in.p_IdEmpresa, "APV","REC.ValidacionSAAPV_Empleador");
					String valorValPERIODO_TERMINO = ConfiguracionSistema.getParametroConfiguracionString(conn,in.p_IdEmpresa, "APV","REC.ValidacionSAAPV_PeriodoTermino");
					String valorValMONTO = ConfiguracionSistema.getParametroConfiguracionString(conn,in.p_IdEmpresa, "APV","REC.ValidacionSAAPV_Monto");
					logger.debug("COMIENZA VALIDACION RECAUDACION");
					
					List<RecaudacionCliente> contratosRepetidos = new ArrayList<RecaudacionCliente>();
					for(RecaudacionCliente fila : in.p_RecaudacionCliente){
						logger.debug("A VALIDAR FOLIO_RECAUDACION: "+fila.IdRecaudacion.toString());
						List<RecaudacionValidada> _RecaudacionValidada = new ArrayList<RecaudacionValidada>();
						for(RecaudacionValidada filaVal :TotalRecaudacionValidada){
							if(filaVal.IdRecaudacionCliente == fila.IdRecaudacionCliente )_RecaudacionValidada.add(filaVal);
						}
						
						DetalleValidacionContratoSAAPV _detalleValSAAPV = new DetalleValidacionContratoSAAPV();
						String mensajeValidacion = "";
						//Valida si la recaudacion tiene cliente asociado
						if(_RecaudacionValidada.size()==0){
							if(fila.IdRecaudacionCliente == 0){
								mensajeValidacion = mensajeValidacion+"No tiene detalle cliente asociado.\n";
							}else{
								mensajeValidacion = mensajeValidacion+"No tiene contratos vigentes.\n";
							}
						}
													
						//recorre para validar si hay un registro correcto

						for(int i = _RecaudacionValidada.size()-1; i >= 0; i --){
							if(_RecaudacionValidada.get(i).Asignado.compareTo("OK") == 0){
								RecaudacionValidada RecaudacionActual = new RecaudacionValidada();
								RecaudacionActual = _RecaudacionValidada.get(i);
								_RecaudacionValidada.clear();
								_RecaudacionValidada.add(RecaudacionActual);
								break;
							}
						}
						
						//Si se encuentra mas de 1 registro se debe rezagar para que el usuario asigne el portafolio manualmente 
						if(_RecaudacionValidada.size()>=2){
							int contadorOK = 0;
							//recorre para validar si hay un registro correcto

							String mensaje = "";
							
							for(int x = _RecaudacionValidada.size()-1; x >= 0; x --){
								if(_RecaudacionValidada.get(x).PeriodoTermino.compareTo("NOK") == 0 && valorValPERIODO_TERMINO.compareTo("S") == 0){
									_RecaudacionValidada.remove(x);
								}else if(_RecaudacionValidada.get(x).IdEstadoCliente.compareTo("NOK") == 0){
									_RecaudacionValidada.remove(x);
								}else if(_RecaudacionValidada.get(x).IdEstadoPortafolio.compareTo("NOK") == 0 ){
									_RecaudacionValidada.remove(x);
								}else if(_RecaudacionValidada.get(x).Validacion_FondoRestringido.compareTo("NOK") == 0 ){
									_RecaudacionValidada.remove(x);
								}else {
									contadorOK= contadorOK+1;
								}
							}

							if(contadorOK == 0){
								mensajeValidacion = mensajeValidacion+"Debe seleccionar un contrato\n";
							}
						}
						
						//Si solo viene un registro, se debe proceder con las demas validaciones
						if(_RecaudacionValidada.size()==1){
							if(_RecaudacionValidada.get(0).IdContratoSAAPV == 0){
								mensajeValidacion = mensajeValidacion+"No existen contratos vigentes.\n";
							}else{
								if(_RecaudacionValidada.get(0).Validacion_Empleador.compareTo("NOK") == 0 && valorValEMPLEADOR.compareTo("N") == 0){
									mensajeValidacion = mensajeValidacion+"Segun lo definido en el contrato, la recaudacion no es por empleador.\n";
								}
								if(_RecaudacionValidada.get(0).PeriodoTermino.compareTo("NOK") == 0 && valorValPERIODO_TERMINO.compareTo("S") == 0){
									mensajeValidacion = mensajeValidacion+"El contrato se encuentra vencido.\n";
								}
								if(_RecaudacionValidada.get(0).IdEstadoCliente.compareTo("ACT") != 0){
									mensajeValidacion = mensajeValidacion+"Cliente bloqueado.\n";
								}
								if(_RecaudacionValidada.get(0).IdEstadoPortafolio.compareTo("ACT") != 0){
									mensajeValidacion = mensajeValidacion+"Portafolio bloqueado.\n";
								}
								
								if(_RecaudacionValidada.get(0).Validacion_FondoRestringido.equalsIgnoreCase("NOK") ){
									mensajeValidacion = mensajeValidacion+"Fondo restringido para aportes. \n";
								}
								
							}
							_detalleValSAAPV.IdPortafolio = _RecaudacionValidada.get(0).IdPortafolio;
							_detalleValSAAPV.IdContrato = _RecaudacionValidada.get(0).IdContratoSAAPV;
							_detalleValSAAPV.IdRegimen = _RecaudacionValidada.get(0).RegimenTributario;
							_detalleValSAAPV.Pago = _RecaudacionValidada.get(0).Pago;
							
						}
						
						if (mensajeValidacion.compareTo("")==0) _detalleValSAAPV.ValidacionOK = "OK"; else _detalleValSAAPV.ValidacionOK = "NOK";
						_detalleValSAAPV.FolioRecaudacion = fila.IdRecaudacion;
						_detalleValSAAPV.IdRecaudacionEmpleador = fila.IdRecaudacionEmpleador;
						_detalleValSAAPV.IdRecaudacionCliente = fila.IdRecaudacionCliente;
						_detalleValSAAPV.IdEstadoRecaudacionCliente = fila.IdEstadoRecaudacionCliente;
						_detalleValSAAPV.Monto = fila.Monto;
						_detalleValSAAPV.DetalleValidacion = mensajeValidacion;
						
						out.P_IDSRECAUDACION.add(_detalleValSAAPV);
						
						if (mensajeValidacion.compareTo("")==0) 
							logger.debug("CLIENTE : "+fila.IdRecaudacionCliente.toString()+" OK");
						else
							logger.debug("CLIENTE : "+fila.IdRecaudacionCliente.toString()+" "+mensajeValidacion);
					}
					logger.debug("FIN VALIDACION RECAUDACION");
					
				}else if(in.p_Opcion.equals("RECTRA")){
					logger.debug("INICIA VALIDACION RECAUDACION TRASPASO.");
					Long IdPortafolioRepetido = 0L;
					
					List<TraspasoReapertura> detallesTraspasoReapertura = new ArrayList<TraspasoReapertura>();
					for(Long filaIdDT : in.p_IdsDetalleTraspasoReapertura) {
						HashMap<String,Object> parametrosTraspasoReapertura = new HashMap<String,Object>();
						parametrosTraspasoReapertura.put("IdDetalleTraspaso", filaIdDT);
						List<TraspasoReapertura> detalleTraspaso = sql.query(conn, SQL_SELECT_TRASPASO_REAPERTURA, parametrosTraspasoReapertura, TraspasoReapertura.class);
						detallesTraspasoReapertura.add(detalleTraspaso.get(0));
					}
					
					int contDetalleReapertura = 0;
					for(RecaudacionCliente fila : in.p_RecaudacionCliente){
						
						//Caso Reapertura Traspaso
						if(!TotalRecaudacionValidada.isEmpty() && TotalRecaudacionValidada.get(0).IdEstadoContrato.compareTo("COM") == 0 && !in.p_IdsDetalleTraspasoReapertura.isEmpty()) {

							IdPortafolioRepetido = detallesTraspasoReapertura.get(contDetalleReapertura).IdPortafolio;
							DetalleValidacionContratoSAAPV _detalleValSAAPV = new DetalleValidacionContratoSAAPV();
							_detalleValSAAPV.IdPortafolio = detallesTraspasoReapertura.get(contDetalleReapertura).IdPortafolio;
							_detalleValSAAPV.IdContrato = detallesTraspasoReapertura.get(contDetalleReapertura).IdContratoSAAPV;
							_detalleValSAAPV.IdRegimen = detallesTraspasoReapertura.get(contDetalleReapertura).Codigo;
							_detalleValSAAPV.FolioRecaudacion = fila.IdRecaudacion;
							_detalleValSAAPV.IdRecaudacionEmpleador = 0L;
							_detalleValSAAPV.IdRecaudacionCliente = fila.IdRecaudacionCliente;
							_detalleValSAAPV.IdEstadoRecaudacionCliente = fila.IdEstadoRecaudacionCliente;
							_detalleValSAAPV.Monto = fila.Monto;
							_detalleValSAAPV.ValidacionOK = "OK";
							_detalleValSAAPV.DetalleValidacion = "";
							
							out.P_IDSRECAUDACION.add(_detalleValSAAPV);	
							contDetalleReapertura++;
							
						}
						else {
							for(RecaudacionValidada filaVal :TotalRecaudacionValidada){
								if(filaVal.IdRecaudacionCliente == fila.IdRecaudacionCliente 
										&& filaVal.Validacion_Monto.equals("OK")
											&& filaVal.IdPortafolio != IdPortafolioRepetido) {
									IdPortafolioRepetido = filaVal.IdPortafolio;
									DetalleValidacionContratoSAAPV _detalleValSAAPV = new DetalleValidacionContratoSAAPV();
									_detalleValSAAPV.IdPortafolio = filaVal.IdPortafolio;
									_detalleValSAAPV.IdContrato = filaVal.IdContratoSAAPV;
									_detalleValSAAPV.IdRegimen = filaVal.RegimenTributario;
									_detalleValSAAPV.FolioRecaudacion = fila.IdRecaudacion;
									_detalleValSAAPV.IdRecaudacionEmpleador = 0L;
									_detalleValSAAPV.IdRecaudacionCliente = fila.IdRecaudacionCliente;
									_detalleValSAAPV.IdEstadoRecaudacionCliente = fila.IdEstadoRecaudacionCliente;
									_detalleValSAAPV.Monto = fila.Monto;
									_detalleValSAAPV.ValidacionOK = "OK";
									_detalleValSAAPV.DetalleValidacion = "";
									
									out.P_IDSRECAUDACION.add(_detalleValSAAPV);		
								}
							}
						}
						logger.debug("CLIENTE : " + fila.IdRecaudacionCliente.toString() + " OK");	
					}
					logger.debug("FIN VALIDACION RECAUDACION TRASPASO.");
				}
			}
		}
		finally {
			//algo
		}	
		logger.info("Fin ValidaContratoSAAPV");
		return out;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		ValidaContratoSAAPV_Entrada jin = gson.fromJson(in.toString() ,ValidaContratoSAAPV_Entrada.class);
		ValidaContratoSAAPV_Salida out = logica(conn,jin);		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getUATConnection()) {
			try {
				//Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();				//
			
				//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":2,\"p_IdModulo\":\"APV\",\"p_Opcion\":0,\"p_ContratosSAAPV\":[{\"IdPersona\":179580,\"IdCliente\":1759213,\"IdEmpleador\":1759213,\"CodigoTipoCargoEmpleado\":\"JEFCUS\",\"IngresoMensual\":0,\"FechaIngresoCargo\":\"0001-01-01\",\"FechaRetiroCargo\":\"0001-01-01\",\"IdEstado\":\"VIG\"}]}";
				
				ValidaContratoSAAPV_Entrada Objin = new ValidaContratoSAAPV_Entrada();
				ContratosSAAPV contratosaapv = new ContratosSAAPV();
				List<ContratosSAAPV> contrato = new ArrayList<ContratosSAAPV>();
				
				contratosaapv.IdPersona = null;
				contratosaapv.IdCliente = null;
				contratosaapv.IdEmpleador = null;
				contratosaapv.IdPortafolio = null;
				contratosaapv.DocumentoIdentidad = "10400183-1";
				contratosaapv.DocumentoIdentidadEmpleador = "17641525-8";
				contratosaapv.FolioContratoSAAPV = "1";
				
/*
				contratosaapv.IdRecaudacion = "1";
				contratosaapv.IdRecaudacionEmpleador = "1";
				contratosaapv.IdRecaudacionCliente = "1";

*/				contrato.add(contratosaapv);
				
				Objin.p_IdEmpresa = 1L;
				Objin.p_IdUsuario = 17L;
				Objin.p_IdModulo = "APV";
				Objin.p_Opcion = "M";
				Objin.p_ContratosSAAPV = contrato;
				Objin.p_RecaudacionCliente = null;
				
				ValidaContratoSAAPV demo = new ValidaContratoSAAPV();	
				
	 			System.out.println("ENTRADA");
	 			System.out.println(Objin.toString());
	 			System.out.println("SALIDA");
	 			
	 			try {
	 				ValidaContratoSAAPV_Salida ObjOut = demo.logica(con, Objin);
	 				con.commit();
	 				System.out.println(ObjOut.toString());
	 			}
	 			catch(Exception er) {
					er.printStackTrace();	
					con.rollback();
				}
	 			
		} catch (Exception e) {
			e.printStackTrace();			
		}	
		System.out.println("== END ==");
		};
	}
}