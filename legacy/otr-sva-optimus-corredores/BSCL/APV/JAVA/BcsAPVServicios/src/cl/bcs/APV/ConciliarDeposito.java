package cl.bcs.APV;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//import java.util.Date;
import java.sql.Date;
//import java.sql.Timestamp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.APV.CambiarEstadoContratosSAAPV.ContratosSAAPV_Salida;
import cl.bcs.APV.CargaMasivaPrevired.ExistePersona;
import cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.per.CrearPersona;
import cl.bcs.socionegocio.per.CrearPersona.EntradaServicioCrearPersona;
import cl.bcs.socionegocio.per.CrearPersona.SalidaServicioCrearPersona;
import cl.bcs.socionegocio.cli.CrearCliente;
import cl.bcs.socionegocio.cli.CrearCliente.EntradaServicioCrearCliente;
import cl.bcs.socionegocio.cli.CrearCliente.SalidaServicioCrearCliente;
import cl.bcs.socionegocio.pojo.Enrolamiento;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.APV.AsignarContratoARecaudacion;
import cl.bcs.APV.BusquedaContratoSAAPVRecaudacion.AsignarContratoRecaudacion;
import cl.bcs.APV.BusquedaContratoSAAPVRecaudacion.EntradaBusquedaContratoSAAPVRecaudacion;
import cl.bcs.APV.BusquedaContratoSAAPVRecaudacion.SalidaBusquedaContratoSAAPVRecaudacion;


public class ConciliarDeposito implements JavaCall   {
	
	private static final Logger logger = LogManager.getLogger(ConciliarDeposito.class);

	/*
	 * Queries que van a ser utilizadas por el servicio. 
	 */	
	
	public static final String APV = "APV";
	public static final String DC = "DC";
	
	public String SQL_Conciliar_Deposito = loadResourceToString("cl/bcs/APV/sql/ConciliarDeposito.sql");
	public String SQL_Conciliar_Empleador = loadResourceToString("cl/bcs/APV/sql/ConciliarRecaudacionEmpleador.sql");
	public String SQL_Conciliar_Cliente = loadResourceToString("cl/bcs/APV/sql/ConciliarRecaudacionCliente.sql");
	private String SQL_DEPOSITO = loadResourceToString("cl/bcs/APV/sql/ConsultaDeposito.sql");
	private String SQL_EMPLEADOR = loadResourceToString("cl/bcs/APV/sql/ConsultarEmpleadorPorRecaudacion.sql");
	private String SQL_CLIENTES = loadResourceToString("cl/bcs/APV/sql/ConsultarClientesPorRecaudacion.sql");
	
	private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();
	
	//private final String SQL_ConsultaExisteEnrolamiento = loadResourceToString("cl/bcs/apv/sql/ConsultaExisteEnrolamiento.sql");
	//private final String SQL_ConsultaExistePersona = loadResourceToString("cl/bcs/apv/sql/ConsultaExistePersona.sql");
	
	/********************
	 * Clases IN/OUT	*
	 ********************/
	
	public static class Recaudacion{
		
		public Long IdRecaudacion;
		public String IdEstado;
	}
	
	public static class Empleador{

		public Long 		IdRecaudacionEmpleador;
		public Long 		IdRecaudacion;
		public String		CodigoArchivo;
		public Long 		IdEmpresa;
		public Long 		IdPersona;
		public String		FechaIngreso;
		public String 		PeriodoPago;
		public Long 		NumeroHojas;
		public String 		TipoAfiliado;
		public String 		TipoPago;
		public String 		Insistencias;
		public BigDecimal	MontoDepositoConvenido;
		public BigDecimal	MontoDepositoAPV;
		public BigDecimal	Reajustes;
		public BigDecimal	Intereses;
		public String 		Origen;
		public String 		Observaciones;
		public String 		IdEstado;
		public String		DocumentoIdentidad;
	}
	
	public static class Cliente{

		public Long 		IdRecaudacionCliente;
		public Long 		IdRecaudacion;
		public Long 		IdRecaudacionEmpleador;
		public Long 		IdEmpresa;
		public Long 		IdCliente;
		public String		FechaIngreso;
		public String 		TipoAfiliado;
		public String 		IdRegimenTriburario;
		public BigDecimal 	MontoDepositoConvenido;
		public BigDecimal 	MontoDepositoAPV;
		public String 		Origen;
		public String 		IdEstado;
		public String		DocumentoIdentidad;
	}
	
	// clases agregada
		public static class ClienteDeposito{
			
			public Long 		IdRecaudacion;
			public Long 		IdRecaudacionCliente;
			public Long 		IdCliente;
			public String 		IdRegimenTriburario;
			public String	 	IdEstado;
			
	}
		
	public static class ClienteContratoAPV{
			
			public Long IdCliente;
			public Long IdContratoSAAPV;
			public String TipoContrato;	
			public String Regimen;
			public String CodigoMedioRecaudacion;
			
			public String toString() {
				
				return Utils.printJson(this, false);
			}
			
			public Long getIdCliente() {
				
				return this.IdCliente;
			}
			
			public String getTipoContrato() {
				
				return this.TipoContrato;
			}
			
			public String getRegimen() {
				
				return this.Regimen;
			}
	}
	
	//
	
	//  SERVICIO ENTRADA
	public static class ParametrosEntradaServicio {
		
		public 	Long					p_IdEmpresa;
		public	Long					p_IdUsuario;
		public 	String					p_IdModulo;
		public  String					p_AsignacionAutomaticaContrato;
		public 	String					p_EsProcesoMasivo;
		public List<Recaudacion>		p_Parametros;
		
		public ParametrosEntradaServicio(){
			p_Parametros = new ArrayList<Recaudacion>();
			p_AsignacionAutomaticaContrato = "N";
			p_EsProcesoMasivo = "N";
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}	

	// SERVICIO SALIDA
	public static class ParametrosSalidaServicio {		
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public ParametrosSalidaServicio(){
			P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}		
	};

	// SALIDA SERVICIO ASIGNACION
	public static class ParametrosSalidaAsignacion {		
		public List<MensajeSalida>									P_OUT_MENSAJES;
		public Long													P_OUT_ERRORESCONTADOR;

		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public ParametrosSalidaAsignacion(){
			P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}		
	};

	public static class ContratosRecaudacion{

		public String Nombres;
		public String RutCliente;
		public Long IdContratoSAAPV;
		public String FolioContratoSAAPV;
		public Long Portafolio;
		public String Estado;
		public String TipoContrato;
		
	}

	public static class ContratosAsignacion{

		public Long IdCliente;
		public Long IdContratoSAAPV;
		public String OrigenAhorro;	
	}
	
	
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		ParametrosEntradaServicio jin = gson.fromJson(in.toString() ,ParametrosEntradaServicio.class);
		ParametrosSalidaServicio out = logica(conn,jin);		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	/************************
	 * Metodos Basicos	*	
	 ************************/
	
	public ParametrosSalidaServicio logica(Connection con,ParametrosEntradaServicio in)throws Exception {
		logger.info("Inicio ConciliarDeposito");
		SalidaServicioCrearPersona 	ObjOut 			= new SalidaServicioCrearPersona ();
		
		ParametrosSalidaServicio 	salida 			= new ParametrosSalidaServicio ();

		EntradaServicioCrearCliente EntradaCliente  = new EntradaServicioCrearCliente();
		SalidaServicioCrearCliente  SalidaCliente   = new SalidaServicioCrearCliente();
		HashMap<String,String> valoresDiccionario = new HashMap<String,String>(3);
		
		for(ConciliarDeposito.Recaudacion fila : in.p_Parametros){
			
			SQL sql = new SQL();			
			Recaudacion RC = new Recaudacion();
			MensajeSalida msg = new MensajeSalida();
			////////////////////////////////////////////
			HashMap<String, Object> param;
			valoresDiccionario.clear();
			param = new HashMap<>();
			param.put("p_IdRecaudacion", fila.IdRecaudacion);
			valoresDiccionario.put("FolioRecaudacion", fila.IdRecaudacion.toString());
			
			List<CrearRecaudacionDeposito.Recaudacion> Deposito = sql.query(con, SQL_DEPOSITO, param, CrearRecaudacionDeposito.Recaudacion.class);
						
			if(Deposito.size() > 0){
			
				String Estado = Deposito.get(0).IdEstado;

				if(	Estado.equals("ING") == true ||
					Estado.equals("NCO") == true )
				{

					List<ConciliarDeposito.Empleador> Empleadores = sql.query(con, SQL_EMPLEADOR, param, ConciliarDeposito.Empleador.class);

					List<ConciliarDeposito.Cliente> Clientes = sql.query(con, SQL_CLIENTES, param, ConciliarDeposito.Cliente.class);
					
					if(Empleadores.size() > 0){
						valoresDiccionario.put("DocumentoIdentidadEmpleador", Empleadores.get(0).DocumentoIdentidad);
						Long totalEmpleador 	= 0L;
						Long totalEmpleadorAPV 	= 0L;
						Long totalEmpleadorDC 	= 0L;
					
						for (int i = 0; i < Empleadores.size(); i++) { 
							totalEmpleadorAPV	= totalEmpleadorAPV + Empleadores.get(i).MontoDepositoAPV.longValue();
							totalEmpleadorDC	= totalEmpleadorDC 	+ Empleadores.get(i).MontoDepositoConvenido.longValue();
							
							//totalEmpleador = totalEmpleador + Empleadores.get(i).MontoDepositoConvenido.longValue() + Empleadores.get(i).MontoDepositoAPV.longValue();
						}
						
						totalEmpleador = totalEmpleadorAPV + totalEmpleadorDC;
						
						Long MontoDeposito = Deposito.get(0).MontoDeposito.longValue();
						
						if(MontoDeposito.equals(totalEmpleador) == true){
							if(Clientes.size() > 0){
								valoresDiccionario.put("DocumentoIdentidadCliente", Clientes.get(0).DocumentoIdentidad);
								Long totalCliente = 0L;
								Long totalClienteAPV = 0L;
								Long totalClienteDC = 0L;
								
								for (int i = 0; i < Clientes.size(); i++) { 
									totalClienteAPV = totalClienteAPV + Clientes.get(i).MontoDepositoAPV.longValue();
									totalClienteDC = totalClienteDC + Clientes.get(i).MontoDepositoConvenido.longValue();
									//totalCliente = totalCliente + Clientes.get(i).MontoDepositoConvenido.longValue() + Clientes.get(i).MontoDepositoAPV.longValue();
								}
									
								totalCliente = totalClienteAPV + totalClienteDC;

								if(totalEmpleadorAPV.equals(totalClienteAPV) == true && totalEmpleadorDC.equals(totalClienteDC) == true ){
								//if(Deposito.get(0).MontoDeposito.longValue() == totalCliente){
									//Conciliar.
									
									RC.IdRecaudacion 	= fila.IdRecaudacion;
									RC.IdEstado			= "CON";
									List<Recaudacion> ParamsRecaudacion = new ArrayList<Recaudacion>();
									
									ParamsRecaudacion.add(RC);

									int[] res = sql.batch(con, SQL_Conciliar_Deposito ,ParamsRecaudacion, Recaudacion.class);
									int[] res2 = sql.batch(con, SQL_Conciliar_Cliente ,ParamsRecaudacion, Recaudacion.class);
									int[] res3 = sql.batch(con, SQL_Conciliar_Empleador ,ParamsRecaudacion, Recaudacion.class);

									ContratosSAAPV_Salida salidaEstado = new ContratosSAAPV_Salida();
									if(res!=null && res.length>0 && res[0]==1){
										msg.id = fila.IdRecaudacion;
										msg.tipo = "INFORMACION";
										msg.codigo = "CONCILIADO_OK";
										msg.descripcion = Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_DEPOSITO_CONCILIADO_CORRECTAMENTE", valoresDiccionario);
										salida.P_OUT_MENSAJES.add(msg);
										salida.P_OUT_ERRORESCONTADOR = 0L;
																				
										if("S".equals(in.p_AsignacionAutomaticaContrato)) {
											ParametrosSalidaServicio ObtieneAsignaContrato = new ParametrosSalidaServicio();
											ObtieneAsignaContrato = AsignaContrato(con , in, fila.IdRecaudacion);
											salida.P_OUT_MENSAJES.addAll(ObtieneAsignaContrato.P_OUT_MENSAJES);
										}
										
									}else{
										msg.id = fila.IdRecaudacion;
										msg.tipo = "ERROR";
										msg.codigo = "ERROR";
										msg.descripcion = "Problemas al Conciliar";
										salida.P_OUT_MENSAJES.add(msg);
										salida.P_OUT_ERRORESCONTADOR = 1L;
									}
								}else{
									//Mensaje: Los montos de los clientes asociados no cuadran con el monto del deposito.
									msg.id = fila.IdRecaudacion;
									msg.tipo = "ERROR";
									msg.codigo = "ERROR";
									msg.descripcion = (_c.NO).equals(in.p_EsProcesoMasivo) ? Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_NO_CUADRAN_MONTOS_APV_DC_DE_EMPLEADOR_CLIENTE", valoresDiccionario) : Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_NO_CUADRAN_MONTOS_APV_DC_DE_EMPLEADOR_CLIENTE_MASIVO", valoresDiccionario);
									salida.P_OUT_MENSAJES.add(msg);
									salida.P_OUT_ERRORESCONTADOR = 1L;
								}
							}else{
								//Mensaje: El deposito tiene Empleadores pero no Clientes asociados.
								msg.id = fila.IdRecaudacion;
								msg.tipo = "ERROR";
								msg.codigo = "ERROR";
								msg.descripcion =  (_c.NO).equals(in.p_EsProcesoMasivo) ? Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_DEPOSITO_CON_EMPLEADOR_SIN_CLIENTE", valoresDiccionario) :  Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_DEPOSITO_CON_EMPLEADOR_SIN_CLIENTE_MASIVO", valoresDiccionario);
								salida.P_OUT_MENSAJES.add(msg);
								salida.P_OUT_ERRORESCONTADOR = 1L;
							}
						}else{
							//Mensaje: Los montos de los empleadores asociados no cuadran con el monto del deposito.
							msg.id = fila.IdRecaudacion;
							msg.tipo = "ERROR";
							msg.codigo = "ERROR";
							msg.descripcion = (_c.NO).equals(in.p_EsProcesoMasivo) ? Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_NO_CUADRA_DEPOSITO_CON_EMPLEADOR", valoresDiccionario) : Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_NO_CUADRA_DEPOSITO_CON_EMPLEADOR_MASIVO", valoresDiccionario);
							salida.P_OUT_MENSAJES.add(msg);
							salida.P_OUT_ERRORESCONTADOR = 1L;
						}
					}else{
						if(Clientes.size() > 0){
							Long totalCliente = 0L;
						
							for (int i = 0; i < Clientes.size(); i++) { 
								totalCliente = totalCliente + Clientes.get(i).MontoDepositoConvenido.longValue() + Clientes.get(i).MontoDepositoAPV.longValue();
							}
														
							Long montoDeposito=  Deposito.get(0).MontoDeposito.longValue();

							if(montoDeposito.equals(totalCliente) == true){
								//Conciliar.
								RC.IdRecaudacion 	= fila.IdRecaudacion;
								RC.IdEstado			= "CON";

								List<Recaudacion> ParamsRecaudacion = new ArrayList<Recaudacion>();
								
								ParamsRecaudacion.add(RC);

								int[] res = sql.batch(con, SQL_Conciliar_Deposito ,ParamsRecaudacion, Recaudacion.class);
								int[] res2 = sql.batch(con, SQL_Conciliar_Cliente ,ParamsRecaudacion, Recaudacion.class);

								ContratosSAAPV_Salida salidaEstado = new ContratosSAAPV_Salida();
								if(res!=null && res.length>0 && res[0]==1){
									msg.id = fila.IdRecaudacion;
									msg.tipo = "INFORMACION";
									msg.codigo = "CONCILIADO_OK";
									msg.descripcion = Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_DEPOSITO_CONCILIADO_CORRECTAMENTE", valoresDiccionario);
									salida.P_OUT_MENSAJES.add(msg);
									salida.P_OUT_ERRORESCONTADOR = 0L;
									
									if("S".equals(in.p_AsignacionAutomaticaContrato)) {
										ParametrosSalidaServicio ObtieneAsignaContrato = new ParametrosSalidaServicio();
										ObtieneAsignaContrato = AsignaContrato(con , in, fila.IdRecaudacion);
										salida.P_OUT_MENSAJES.addAll(ObtieneAsignaContrato.P_OUT_MENSAJES);
									}
								}else{
									msg.id = fila.IdRecaudacion;
									msg.tipo = "ERROR";
									msg.codigo = "ERROR";
									msg.descripcion = "Problemas al Conciliar";
									salida.P_OUT_MENSAJES.add(msg);
									salida.P_OUT_ERRORESCONTADOR = 1L;
								}
							}else{
								//Mensaje: Los montos de los clientes asociados no cuadran con el monto del deposito.
								msg.id = fila.IdRecaudacion;
								msg.tipo = "ERROR";
								msg.codigo = "ERROR";
								msg.descripcion = (_c.NO).equals(in.p_EsProcesoMasivo) ? Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_NO_CUADRA_DEPOSITO_CON_CLIENTE", valoresDiccionario) : Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_NO_CUADRA_DEPOSITO_CON_CLIENTE_MASIVO", valoresDiccionario);
								salida.P_OUT_MENSAJES.add(msg);
								salida.P_OUT_ERRORESCONTADOR = 1L;
							}
						}else{
							//Mensaje: El deposito no tiene Empleador ni Clientes asociados.
							msg.id = fila.IdRecaudacion;
							msg.tipo = "ERROR";
							msg.codigo = "ERROR";
							msg.descripcion = (_c.NO).equals(in.p_EsProcesoMasivo) ? Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_DEPOSITO_SIN_EMPLEADOR_NI_CLIENTES", valoresDiccionario) : Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_DEPOSITO_SIN_EMPLEADOR_NI_CLIENTES_MASIVO", valoresDiccionario);
							salida.P_OUT_MENSAJES.add(msg);
							salida.P_OUT_ERRORESCONTADOR = 1L;
						}
					}
				}else{
					
					//Mensaje: Deposito no cumple con estado para conciliar.
					HashMap<String, String> parametrosMensaje = new HashMap<String, String>();
					parametrosMensaje.put("IdRecaudacion", fila.IdRecaudacion.toString());
					msg.id = fila.IdRecaudacion;
					msg.tipo = MensajeSalida.MENSAJE_ERROR;
					msg.codigo = "UI_DEPOSITO_CON_ESTADO_NO_PERMITIDO";
					msg.descripcion = Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_DEPOSITO_CON_ESTADO_NO_PERMITIDO", parametrosMensaje);
					salida.P_OUT_MENSAJES.add(msg);
					salida.P_OUT_ERRORESCONTADOR = 1L;
					
				}
			}else{
				msg.id = fila.IdRecaudacion;
				msg.tipo = "ERROR";
				msg.codigo = "ERROR";
				msg.descripcion = Diccionario.getMensaje(con,in.p_IdEmpresa, "UI_SELECCIONE_DEPOSITO", valoresDiccionario);
				salida.P_OUT_MENSAJES.add(msg);
				salida.P_OUT_ERRORESCONTADOR = 1L;
			}	
		}
		
		salida.P_OUT_ERRORESCONTADOR = (_c.NO).equals(in.p_EsProcesoMasivo) ? PlataformaUtils.MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES) : 0L;
		//Log
		if(salida.P_OUT_ERRORESCONTADOR.compareTo(0L) == 0) {
			agregarLog(con, in);
		}
		
		logger.info("Fin ConciliarDeposito");
		return salida;
	}
	
	
	public ParametrosSalidaServicio AsignaContrato(Connection con, ParametrosEntradaServicio in, Long idRecaudacion) throws Exception {
		
		SQL sql = new SQL();

		ParametrosSalidaServicio outAsignaContrato = new ParametrosSalidaServicio();
		HashMap<String,String> valoresDiccionario = new HashMap<String,String>(0);
			
		List<AsignarContratoARecaudacion.ParametrosAsignacionContratoRecaudacion> ContratosAAsignar = new ArrayList<AsignarContratoARecaudacion.ParametrosAsignacionContratoRecaudacion>();
		List<Long> idsRecaudacion = new ArrayList<Long>();
		idsRecaudacion.add(idRecaudacion);
		
		BusquedaContratoSAAPVRecaudacion busquedaContratoSAAPVRecaudacion = new BusquedaContratoSAAPVRecaudacion();
		EntradaBusquedaContratoSAAPVRecaudacion entradaBusquedaContratoSAAPVRecaudacion = new EntradaBusquedaContratoSAAPVRecaudacion();
		entradaBusquedaContratoSAAPVRecaudacion.p_IdEmpresa = in.p_IdEmpresa;
		entradaBusquedaContratoSAAPVRecaudacion.p_IdUsuario = in.p_IdUsuario;
		entradaBusquedaContratoSAAPVRecaudacion.p_IdModulo	= in.p_IdModulo;
		entradaBusquedaContratoSAAPVRecaudacion.p_IdsRecaudacion = idsRecaudacion;
		SalidaBusquedaContratoSAAPVRecaudacion salidaBusquedaContratoSAAPVRecaudacion = busquedaContratoSAAPVRecaudacion.logica(con, entradaBusquedaContratoSAAPVRecaudacion);
		
		for( AsignarContratoRecaudacion asignacionContrato : salidaBusquedaContratoSAAPVRecaudacion.P_OUT_RESULTADO ) {
			
			if( asignacionContrato.IdContratoSAAPV > 0 ) {
				
				AsignarContratoARecaudacion.ParametrosAsignacionContratoRecaudacion DatoContrato = new AsignarContratoARecaudacion.ParametrosAsignacionContratoRecaudacion();
				DatoContrato.IdRecaudacionCliente = asignacionContrato.IdRecaudacionCliente;
				DatoContrato.IdContratoSAAPV = asignacionContrato.IdContratoSAAPV;
				DatoContrato.IdRecaudacion = asignacionContrato.IdRecaudacion;
				ContratosAAsignar.add(DatoContrato);

			}
			
		}

		if(ContratosAAsignar.size() > 0) {
				
				AsignarContratoARecaudacion AsignaContrato = new AsignarContratoARecaudacion();
				
				AsignarContratoARecaudacion.ParametrosEntradaServicio IN_AsignarContrato = new AsignarContratoARecaudacion.ParametrosEntradaServicio();
				IN_AsignarContrato.p_IdEmpresa = in.p_IdEmpresa;
				IN_AsignarContrato.p_IdModulo = in.p_IdModulo;
				IN_AsignarContrato.p_IdUsuario = in.p_IdUsuario;
				IN_AsignarContrato.p_Parametros = new ArrayList<AsignarContratoARecaudacion.ParametrosAsignacionContratoRecaudacion>();
				IN_AsignarContrato.p_Parametros.addAll(ContratosAAsignar);
				
				AsignarContratoARecaudacion.ParametrosSalidaServicio salidaAsignacion = new AsignarContratoARecaudacion.ParametrosSalidaServicio(); 
				salidaAsignacion = AsignaContrato.logica(con, IN_AsignarContrato);
				
				outAsignaContrato.P_OUT_MENSAJES.addAll(salidaAsignacion.P_OUT_MENSAJES);
		}
		
		return outAsignaContrato;

	}
	
	private void agregarLog (Connection con, ParametrosEntradaServicio in) throws Exception{
		logger.trace("INICIO::ConciliarDeposito.agregarLog");
		long inicio = System.currentTimeMillis();
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		
		InAgregarLog entradaAgregarLog = new InAgregarLog();
		
		if(!in.p_Parametros.isEmpty()) {
			in.p_Parametros.sort(Comparator.comparingLong(recaudacion -> recaudacion.IdRecaudacion));
			if(_c.SI.equalsIgnoreCase(in.p_EsProcesoMasivo)) {
				AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
				Long idTrabajo = new Double(Math.random()*10000000000L).longValue();
				log.IdCabeceraLog = 1L;
				log.IdTrabajo = idTrabajo;
				log.IdTipoLog = "INFORMATIVO";
				log.IdModulo = in.p_IdModulo;
				log.IdUsuario = in.p_IdUsuario;
				log.IdEmpresa = in.p_IdEmpresa;
				log.FechaSistemaLog = fechaSistema;
				log.IdUsuarioAtendido = in.p_IdUsuario;
				log.IdTipoRegistro = "CONCILIACION";
				log.NombreProceso = "Conciliar Deposito";
				entradaAgregarLog.p_Log.add(log);
				Long folioDesde = in.p_Parametros.get(0).IdRecaudacion;
				Long folioHasta = in.p_Parametros.get(in.p_Parametros.size()-1).IdRecaudacion;
				entradaAgregarLog.agregarDetalleLogBasico(1L, idTrabajo, "Folio Recaudación", folioDesde.toString(), folioHasta.toString(), "Rango Conciliación Masiva");
			}else {
				in.p_Parametros.stream()
					.filter(fila -> fila.IdRecaudacion != null && fila.IdRecaudacion.compareTo(0L) > 0)
					.sorted(Comparator.comparingLong(fila -> fila.IdRecaudacion))
					.forEach(fila ->{
						AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
						log.IdCabeceraLog = fila.IdRecaudacion;
						log.IdTrabajo = fila.IdRecaudacion;
						log.IdTipoLog = "INFORMATIVO";
						log.IdModulo = in.p_IdModulo;
						log.IdUsuario = in.p_IdUsuario;
						log.IdEmpresa = in.p_IdEmpresa;
						log.FechaSistemaLog = fechaSistema;
						log.IdUsuarioAtendido = in.p_IdUsuario;
						log.IdTipoRegistro = "CONCILIACION";
						log.NombreProceso = "Conciliar Deposito";
						entradaAgregarLog.p_Log.add(log);
						String subRegistro = String.format("Folio Recaudación %s", fila.IdRecaudacion.toString());
						entradaAgregarLog.agregarDetalleLogBasico(fila.IdRecaudacion, fila.IdRecaudacion, "IdEstado", "", "CON", subRegistro);
					});		
			}
			AgregarLog agregarLogService = new AgregarLog();
			if(!entradaAgregarLog.p_LogDetalle.isEmpty()) {
				agregarLogService.logica(con, entradaAgregarLog);
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConciliarDeposito.agregarLog", fin - inicio);
	}
	
	
	 
	
	
	
	/********************
	 * Metodo Principal *
	 ********************/

	public static void main(String[] args) throws Exception {
		
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			try {				
				ConciliarDeposito demo = new ConciliarDeposito();
				
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();	
				
				String jsonError = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"APV\",\"p_AsignacionAutomaticaContrato\":\"S\",\"p_EsProcesoMasivo\":\"S\",\"p_Parametros\":[{\"IdRecaudacion\":1655},{\"IdRecaudacion\":1657},{\"IdRecaudacion\":1659},{\"IdRecaudacion\":1310},{\"IdRecaudacion\":1311},{\"IdRecaudacion\":1476},{\"IdRecaudacion\":1352},{\"IdRecaudacion\":1353},{\"IdRecaudacion\":1354},{\"IdRecaudacion\":1363},{\"IdRecaudacion\":1364},{\"IdRecaudacion\":1365},{\"IdRecaudacion\":642},{\"IdRecaudacion\":749},{\"IdRecaudacion\":753},{\"IdRecaudacion\":759}]}";
				
				String jsonMulti = "{\r\n"
						+ "    \"p_IdEmpresa\": 1,\r\n"
						+ "    \"p_IdUsuario\": 9,\r\n"
						+ "    \"p_IdModulo\": \"APV\",\r\n"
						+ "    \"p_AsignacionAutomaticaContrato\": \"S\",\r\n"
						+ "    \"p_EsProcesoMasivo\": \"S\",\r\n"
						+ "    \"p_Parametros\": [\r\n"
						+ "        {\r\n"
						+ "            \"IdRecaudacion\": 1655\r\n"
//						+ "        },\r\n"
//						+ "        {\r\n"
//						+ "            \"IdRecaudacion\": 749\r\n"
//						+ "        },\r\n"
//						+ "        {\r\n"
//						+ "            \"IdRecaudacion\": 1354\r\n"
						+ "        }\r\n"
						+ "    ]\r\n"
						+ "}";
				
				ParametrosEntradaServicio Objin2 = gson.fromJson(jsonError,ParametrosEntradaServicio.class);
	 			System.out.println("ENTRADA");
	 			System.out.println(Objin2.toString());
	 			System.out.println("SALIDA");
	 			
	 			
	 			try { 
	 				ParametrosSalidaServicio ObjOut = demo.logica(con, Objin2);
	 				//con.commit();
	 				con.rollback();
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