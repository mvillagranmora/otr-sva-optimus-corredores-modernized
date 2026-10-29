package cl.bcs.custodia.evc;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.cuentaInversion.CrearCargoAbono;
import cl.bcs.cuentaInversion.CrearCargoAbono.CrearCargoAbonoEntrada;
import cl.bcs.cuentaInversion.CrearCargoAbono.CrearCargoAbonoSalida;
import cl.bcs.cuentaInversion.CrearCargoAbono.ObjCargoAbonoAGenerar;
import cl.bcs.custodia.evc.NotificarEventoCapital.NotificarEventoCapital_Entrada;
import cl.bcs.custodia.evc.NotificarEventoCapital.NotificarEventoCapital_Salida;
import cl.bcs.custodia.evc.NotificarEventoCapital.ObjEventoCapitalCliente_Entrada;
import cl.bcs.custodia.evc.NotificarEventoCapital.ObjEventoCapital_Entrada;
import cl.bcs.custodia.pojo.EventoCapital;
import cl.bcs.custodia.pojo.EventoCapitalCliente;
import cl.bcs.custodia.pojo.EventoCapitalClienteFormaDePago;
import cl.bcs.custodia.utils.CacheValorizacion;
import cl.bcs.custodia.utils.LogCustodia;
import cl.bcs.custodia.utils.LogCustodia.ObjMovimiento;
import cl.bcs.custodia.utils.RetiroCustodia;
import cl.bcs.custodia.utils.RetiroCustodia.ObjDatosEntrada;
import cl.bcs.custodia.utils.RetiroCustodia.RetiroCustodiaEntrada;
import cl.bcs.custodia.utils.RetiroCustodia.RetiroCustodiaSalida;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.tesoreria.CrearIngresoEgreso;
import cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Entrada;
import cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Salida;
import cl.bcs.tesoreria.CrearIngresoEgreso.FormaDePago_Entrada;
import cl.bcs.tesoreria.CrearIngresoEgreso.IngresoEgresoGenerado_Salida;
import cl.bcs.tesoreria.CrearIngresoEgreso.IngresoEgreso_Entrada;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CerrarEventoCapitalSorteoLetras implements JavaCall{
	private final Log log = new Log("CUS.CerrarEventoCapitalSorteoLetras");
	private final String SQL_SELECT_INSTRUMENTO 			= loadResourceToString("cl/bcs/custodia/sql/CerrarEventoCapitalSorteoLetras_SelectInstrumento.sql");
	private final String SQL_SELECT_EVENTO_CAPITAL_CLIENTE	= loadResourceToString("cl/bcs/custodia/sql/CerrarEventoCapitalSorteoLetras_SelectEventoCapitalCliente.sql");
	private final String SQL_SELECT_EVENTO_CAPITAL_FORMA_DE_PAGO = loadResourceToString("cl/bcs/custodia/sql/CerrarEventoCapitalSorteoLetras_SelectFormaDePago.sql");
	private final String SQL_UPDATE_EVENTO_CAPITAL 		= loadResourceToString("cl/bcs/custodia/sql/CerrarEventoCapitalSorteoLetras_UpdateEventoCapital.sql");
	private final String SQL_UPDATE_EVENTO_CAPITAL_CLIENTE	= loadResourceToString("cl/bcs/custodia/sql/CerrarEventoCapitalSorteoLetras_UpdateEventoCapitalCliente.sql");
	
	public static class CerrarEventoCapitalSorteoLetrasEntrada {
		public Long							p_IdEmpresa; 
		public Long							p_IdUsuario;
		public String						p_IdModulo;
		//public String						p_Origen; //corresponde desde donde es ejecutado el evento MAN: UI, AUT: Job Pauta
		public String						p_UsaReglasNegocio;
		
		public List<EventoCapital>			p_EventoCapital; //eventos capital a procesar
				
		public CerrarEventoCapitalSorteoLetrasEntrada (){
			this.p_EventoCapital 		= new ArrayList<EventoCapital>();
		}
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class CerrarEventoCapitalSorteoLetrasSalida {
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		public List<Long>			P_OUT_SECUENCIA;
		
		public CerrarEventoCapitalSorteoLetrasSalida() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_SECUENCIA = new ArrayList<Long>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class Instrumento {
		public Long 	IdInstrumento;
		public String 	Nemotecnico;
		public String	IdClasificacionTipoInstrumento;
		public BigDecimal 	CorteMinimo;
		public String 		IdEmisor;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class EventoCapitalUpd {
		public Long 	p_IdEmpresa;
		public Long 	p_IdEventoCapital;
		public Long 	p_IdEventoCapitalCliente;
		public String 	p_IdTipoEventoCapital;
		public Long 	p_Secuencia;
		public String	p_IdEstado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
		
	public CerrarEventoCapitalSorteoLetrasSalida logica(Connection con, CerrarEventoCapitalSorteoLetrasEntrada in) throws Exception {
		log.write("INICIO::CUS.CerrarEventoCapitalSorteoLetras");
		CerrarEventoCapitalSorteoLetrasSalida out = new CerrarEventoCapitalSorteoLetrasSalida();
		HashMap<String,String> mapMsg = new HashMap<>();
		SQL sql = new SQL();
		Long Id = 0L;
		
		List<EventoCapitalUpd> updateEventoCapital = new ArrayList<>();
		
		// obtener fecha del sistema
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		// obtener valor del parámetro
		String MonedaSistema    	= ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
		String localizacionSistema	= ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.LocalizacionSistema");
		String v_Estado = "CER";//que estado deberia ser
		ConsultarEmpresa consultarEmpresa = new ConsultarEmpresa();
		InformacionEmpresa informacionEmpresa = consultarEmpresa.logica(con, in.p_IdEmpresa);
		
		List<ObjEventoCapital_Entrada> ListaIds = new ArrayList<ObjEventoCapital_Entrada>();
		List<ObjEventoCapitalCliente_Entrada> ListaCliIds = new ArrayList<ObjEventoCapitalCliente_Entrada>();
		//consulto instrumentos
		HashMap<String,Object> params = new HashMap<String,Object>();
		List<Long> idsInstrumento = new ArrayList<>();
		List<Long> idsEventoCapital = new ArrayList<>();
		List<String> tipoEventoCapital = new ArrayList<>();
		for (EventoCapital ev: in.p_EventoCapital){
			idsInstrumento.add(ev.IdInstrumento);
			idsEventoCapital.add(ev.IdEventoCapital);
			tipoEventoCapital.add(ev.IdTipoEventoCapital);
		}
		params.put("p_IdInstrumento", idsInstrumento);
		List<Instrumento> inst = sql.query(con, SQL_SELECT_INSTRUMENTO, params, Instrumento.class);
		
		//Busco los Clientes en Evento capital Cliente
		params.clear();
		params.put("p_IdEventoCapital", idsEventoCapital);
		List<EventoCapitalCliente> ClientesConDerecho = sql.query(con, SQL_SELECT_EVENTO_CAPITAL_CLIENTE, params, EventoCapitalCliente.class);
		//HashMap<Long, Object> ClientesConDerechoHash = Utils.List2Hash(ClientesConDerecho, "IdEventoCapital");
		
		//busco formas de pago
		params.clear();
		params.put("p_IdEventoCapital", idsEventoCapital);
		params.put("p_IdTipoEventoCapital", tipoEventoCapital);
		params.put("p_LocalizacionSistema",localizacionSistema);
		List<EventoCapitalClienteFormaDePago> formasDePagoCliente = sql.query(con, SQL_SELECT_EVENTO_CAPITAL_FORMA_DE_PAGO, params, EventoCapitalClienteFormaDePago.class);
		//******************************************************************************************************************
		//												VALIDACIONES INICIALES
		//******************************************************************************************************************			
		{
			RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();					
			RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
			out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
			
			for (EventoCapital ev : in.p_EventoCapital){
				Boolean existe = false;
				for (EventoCapitalCliente evCli : ClientesConDerecho){
					if (evCli.IdEventoCapital.equals(ev.IdEventoCapital)){
						existe = true;
						break;
					}
				}
				if (!existe){
					mapMsg.clear();
					mapMsg.put("NumeroDividendo", ev.NumeroDividendo.toString());
					out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"UI_EVENTO_CAPITAL_SIN_CLIENTES",mapMsg,con,in.p_IdEmpresa));
					Id++;
				}
				for (Instrumento ins : inst){
					if (ins.IdInstrumento.equals(ev.IdInstrumento)){
						break;
					}
				}
				if (!existe){
					mapMsg.clear();
					//mapMsg.put("NumeroDividendo", ev.NumeroDividendo.toString());
					out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"UI_INSTRUMENTO_NO_EXISTE",mapMsg,con,in.p_IdEmpresa));
					Id++;
				}
			}
			if (MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES) > 0){
				return out;
			}
		}
		//******************************************************************************************************************
		//													LOGICA
		//******************************************************************************************************************	
		{	
			Long TicketAtencion = 0L;
			List<ObjMovimiento>  entLogCus = new ArrayList<>();
			TicketAtencion = LogCustodia.logCustodia(con,entLogCus);
			CrearIngresoEgreso_Salida tesOut = new CrearIngresoEgreso_Salida();
			if (TicketAtencion.compareTo(0L) <= 0){
				mapMsg.clear();
				out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"SIN_TICKET_ATENCION",mapMsg,con,in.p_IdEmpresa));
				Id++;
			}		
			//Retiro de Titulos
			{
				// paso por movimiento en transito
				List<ObjDatosEntrada> retiroCustodia = new ArrayList<>();
				//List<Long> IdMovimientosCustodia = new ArrayList<Long>();
				for (EventoCapital ev: in.p_EventoCapital){
					for (EventoCapitalCliente cli: ClientesConDerecho){
						if (cli.TipoSocioNegocio.equalsIgnoreCase("CNT")){
							EventoCapitalUpd evento = new EventoCapitalUpd();
							evento.p_IdEmpresa = in.p_IdEmpresa;
							evento.p_IdEventoCapital = ev.IdEventoCapital;
							evento.p_IdEventoCapitalCliente = cli.IdEventoCapitalCliente;
							evento.p_IdTipoEventoCapital = ev.IdTipoEventoCapital;
							evento.p_Secuencia = ev.Secuencia;
							evento.p_IdEstado = v_Estado;
							updateEventoCapital.add(evento);
						}
						if (!cli.TipoSocioNegocio.equalsIgnoreCase("CLI")){
							continue;
						}
						if (cli.IdEventoCapital.equals(ev.IdEventoCapital)){
							//EventoCapitalCliente evCli = (EventoCapitalCliente) ClientesConDerechoHash.get(ev.IdEventoCapital);
							ObjDatosEntrada objIng = new ObjDatosEntrada();
							objIng.IdGenerarMovimientoCustodia 		= cli.IdEventoCapitalCliente;	//ID Interno del proceso
							objIng.TablaAfectada					= "CARTERA"; 					//CARTERA - SUBYACENTE - TRANSITO - GARANTIA
							objIng.EsRetiroNegocio					= "S";				//S o N
							objIng.EsRetiroPorFusion 				= "N";		//Cuando es retorno de cartera
							//////////////////////////	INFORMACION PARA FILTRO DE CARTERA
							objIng.FolioComprobanteBusqueda 		= cli.IdEventoCapital;
							/////////////////////////// OPERACION
							objIng.AccionOperacion	= "RETIRO";				//RETIRO - VENCIMIENTO - ANTICIPO(REVERSA ENTRA POR ANULACION) - ANULACION - MODIFICACION
							objIng.FechaRetiroRegistro 				= Fecha.toDateTime(Fecha.stringToSqlDate(ev.FechaLimite));
							objIng.IdCliente						= cli.IdCliente; 
							objIng.IdPortafolio						= cli.IdPortafolio; 
							objIng.FechaOperacion					= ev.FechaLimite;
							objIng.IdTipoOperacion					= "SORLET";
							objIng.IdTipoProducto					= "CUST";
							objIng.IdOperacion						= cli.IdEventoCapitalCliente;
							objIng.IdConcepto						= "CIECSL";
							objIng.IdCondicionLiquidacion			= "T0";
							objIng.FechaLiquidacion					= cli.FechaPago;		
							objIng.IdTipoCanalIngreso				= "SIS"; 	 
							objIng.FolioComprobante					= cli.IdEventoCapital; 
							objIng.FechaComprobante					= ev.FechaLimite;
							objIng.IdUsuario						= in.p_IdUsuario;
							objIng.IdModulo							= in.p_IdModulo;
							objIng.Glosa							= "";
							/////////////////////////// INSTRUMENTO
							objIng.IdInstrumento					= cli.IdInstrumento;
							objIng.IdBoveda							= cli.IdBoveda;
							objIng.CuentaBoveda						= cli.NumeroCuentaBoveda;
							/////////////////////////// CANTIDADES Y MONTOS		
							objIng.Cantidad							= cli.CantidadNueva; 
							objIng.PrecioCompra						= cli.Precio; 
							objIng.MontoCompraMonedaInstrumento		= cli.Total;
							objIng.ValorRescate						= BigDecimal.ZERO;
							
							retiroCustodia.add(objIng);
							EventoCapitalUpd evento = new EventoCapitalUpd();
							evento.p_IdEmpresa = in.p_IdEmpresa;
							evento.p_IdEventoCapital = ev.IdEventoCapital;
							evento.p_IdEventoCapitalCliente = cli.IdEventoCapitalCliente;
							evento.p_IdTipoEventoCapital = ev.IdTipoEventoCapital;
							evento.p_Secuencia = ev.Secuencia;
							evento.p_IdEstado = v_Estado;
							updateEventoCapital.add(evento);
							
							ObjEventoCapitalCliente_Entrada c = new ObjEventoCapitalCliente_Entrada();
							c.p_IdEventoCapitalCliente = cli.IdEventoCapitalCliente;
							ListaCliIds.add(c);
						}
					}
					//Envío de Notificación
					ObjEventoCapital_Entrada NotifIN = new ObjEventoCapital_Entrada();
					NotifIN.p_IdEventoCapital = ev.IdEventoCapital;
					ListaIds.add(NotifIN);
				}
				
				RetiroCustodiaEntrada cusIn = new RetiroCustodiaEntrada();
				cusIn.p_IdEmpresa 		= in.p_IdEmpresa;
			    cusIn.p_IdModulo 		= in.p_IdModulo;
			    cusIn.p_SimularOperacion= "N";
			    cusIn.p_TicketAtencion 	= TicketAtencion;
			    cusIn.p_ValorizacionAutomatica = "N";
				cusIn.p_DatosEntrada 	= retiroCustodia;//agregar los datos de custodia
			    						
				RetiroCustodia cus = new RetiroCustodia();
				log.write("CUS.Llamada Retiro Custodia");
				RetiroCustodiaSalida cusOut = cus.logica(con, cusIn);
				CacheValorizacion.valorizar(con);
				
				out.P_OUT_MENSAJES.addAll(cusOut.P_OUT_MENSAJES);
				
				if (MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES) > 0){
					return out;
				}
				
			}
			
			{
				//INGRESO EGRESO a Tesoreria
				CrearIngresoEgreso tes = new CrearIngresoEgreso();
				CrearIngresoEgreso_Entrada entTes = new CrearIngresoEgreso_Entrada();
				entTes.p_IdEmpresa = in.p_IdEmpresa;
				entTes.p_IdUsuario = in.p_IdUsuario;
				entTes.p_IdModulo = in.p_IdModulo;
				entTes.p_SimularOperacion = "N";
				entTes.p_UsaReglasNegocio = in.p_UsaReglasNegocio;		
				entTes.p_EsIngresoAutomatico = "S";        //S: Ingreso automático   N: Ingreso Manual
				entTes.p_FechaMovimiento = fechaSistema;   
				for (EventoCapital ev: in.p_EventoCapital){
					for (EventoCapitalCliente cli: ClientesConDerecho){
						if (cli.IdEventoCapital.equals(ev.IdEventoCapital)){
							if (cli.IdEventoCapital.equals(ev.IdEventoCapital) && cli.TipoSocioNegocio.equalsIgnoreCase("CNT")){
								IngresoEgreso_Entrada IEGen = new IngresoEgreso_Entrada();
								IEGen.NumeroReferencia 				= cli.IdEventoCapitalCliente;  				//	BIGINT 
								IEGen.IdTipoMovimiento 				= "MOVING";  		 		//	NVARCHAR(6)    -- MOVING, MOVEGR
								IEGen.IdTipoPersona 				= cli.TipoSocioNegocio;  		 		//	NVARCHAR(3)    -- Es el IdTipoSocioDeNegocio (CLI / EMP / PRO / OPE)
								IEGen.IdPersona						= cli.IdPersona;  		 			//	BIGINT
								IEGen.IdPortafolio 					= cli.IdPortafolio;  		 			//	BIGINT 
								IEGen.IdMoneda 						= MonedaSistema;//Liquidacion;  		 				//	NVARCHAR(3)
								IEGen.Monto 						= cli.Total.multiply(new BigDecimal(-1));//cli.Monto.add(fPag.Monto);//LiquidacionFormaDePago;//fPag.MontoFormaDePago;  		 				//	DECIMAL(26,8)
								IEGen.FechaLiquidacion 				= Fecha.stringToSqlDate(cli.FechaPago);  		 		//	NVARCHAR(10)                                                                      
								IEGen.IdConcepto 					= "CIECSL";  		 			//	NVARCHAR(6)
								IEGen.IdTipoPersonaTitularPago 		= cli.TipoSocioNegocio;  		//	NVARCHAR(3)    -- Es el IdTipoSocioDeNegocio (CLI / EMP / PRO / OPE)
								IEGen.IdPersonaTitularPago 			= cli.IdPersona;  		 	//	BIGINT
								IEGen.IdPortafolioTitularPago 		= cli.IdPortafolio;  		//	BIGINT
								IEGen.IdCaja 						= 0L;  		 				//	BIGINT
								IEGen.SecuenciaAgrupacion 			= ""; 		//	NVARCHAR(40)  -- Movimientos con una misma secuencia se agrupan en un mismo comprobante(también influyen parámetros de agrupación)
								IEGen.SecuenciaRelacionada 			= 0L;	 	//	BIGINT		  -- Permite relacionar distintos mov y generar una orden de liquidación entre ellos
								IEGen.Glosa 						= "";  		 				//	NVARCHAR(260)														   	
							   	//Parámetros Referenciales(Opcionales)
								//IEGen.IdEmpleadoDigitador			= operacionCerrar.p_IdUsuario;  		 	//	BIGINT
								IEGen.IdEmpleadoDigitador			= in.p_IdUsuario;  		 	//	BIGINT									
								IEGen.IdSucursalEmpleadoDigitador	= 0L;  	//	BIGINT
								IEGen.IdTipoProducto				= "";  		 		//	NVARCHAR(4)
								IEGen.IdTipoOperacion				= "SORLET";  		 		//	NVARCHAR(6)									    
								IEGen.IdTipoComprobante				= "CUS";  		 	//	NVARCHAR(3)
								IEGen.IdComprobante					= cli.IdEventoCapital;  		 		//	BIGINT
								IEGen.FechaComprobante				= Fecha.toFormat(cli.FechaPago,"YYYY-MM-dd hh:mm:ss");  		 		//	NVARCHAR(40)
								IEGen.FechaLiquidacionComprobante	= Fecha.stringToSqlDate(cli.FechaPago);  	//	NVARCHAR(40)
								
								IEGen.IdOperacion					= cli.IdEventoCapitalCliente;  		 			//	BIGINT			
								IEGen.FechaOperacion				= Fecha.toFormat(cli.FechaPago,"YYYY-MM-dd hh:mm:ss");  		 		//	NVARCHAR(40)
								IEGen.IdCondicionLiquidacion		= "";  		//	NVARCHAR(2)
								IEGen.IdBolsa						= "";  		 				//	NVARCHAR(3)
								IEGen.IdMonedaOperacion				= MonedaSistema;  		 	//	NVARCHAR(3)
								IEGen.MontoOperacion				= cli.Total.multiply(new BigDecimal(-1));//IEGen.MontoOperacion.add(fPag.Monto);//LiquidacionFormaDePago;   				//	DECIMAL(26,8)
								IEGen.IdTipoReajuste				= "";  		 		//	NVARCHAR(3)
								IEGen.FechaParidad					= "0001-01-01";  		 			//	NVARCHAR(40)
								IEGen.ValorParidad					= BigDecimal.ZERO;   				// 	DECIMAL(26,8)
								IEGen.FolioCartera					= 0L;  		 			//	BIGINT
								IEGen.IdInstrumento					= 0L;  		 		//	BIGINT
								IEGen.IdTipoInstrumento				= "";  		 	//	NVARCHAR(3)
								IEGen.IdClasificacionTipoInstrumento= ""; //	NVARCHAR(3)
							    //Indicadores
								IEGen.Bilateral 					= "N";		        //	NVARCHAR(1)  -- Liquidación bilateral entre corredores, no pasa por el CCLV
								IEGen.GeneraComprobante 			= "S"; 		//	NVARCHAR(1)  -- S/N: genera comprobante automático
								IEGen.AgruparEnComprobante 			= "N"; 	//	NVARCHAR(1)  -- S/N: permite agrupar varios mov. en un comprobante
								IEGen.MueveCuentaInversion			= "N";  			//	NVARCHAR(1)  -- S/N: mueve cuenta inversion // *** No se usa el campo, se debe borrar ? ***
								IEGen.TitularPago					= "S";  		 			//	NVARCHAR(1)  -- S/N: Socio de negocio que genera el movimiento es quién cobra o paga
								
								entTes.p_IngresoEgresoGenerar.add(IEGen);
							}
							for (EventoCapitalClienteFormaDePago fPag :formasDePagoCliente){
								if (cli.IdEventoCapital.equals(fPag.IdEventoCapital) && cli.IdEventoCapitalCliente.equals(fPag.IdEventoCapitalCliente) && !cli.IdPersona.equals(informacionEmpresa.IdPersona) &&  !fPag.IdMedioDePago.equalsIgnoreCase("CIN")){
									IngresoEgreso_Entrada IEGen = new IngresoEgreso_Entrada();
									IEGen.NumeroReferencia 				= fPag.IdEventoCapitalCliente;  				//	BIGINT 
									IEGen.IdTipoMovimiento 				= "MOVEGR";  		 		//	NVARCHAR(6)    -- MOVING, MOVEGR
									IEGen.IdTipoPersona 				= cli.TipoSocioNegocio;  		 		//	NVARCHAR(3)    -- Es el IdTipoSocioDeNegocio (CLI / EMP / PRO / OPE)
									IEGen.IdPersona						= cli.IdPersona;  		 			//	BIGINT
									IEGen.IdPortafolio 					= cli.IdPortafolio;  		 			//	BIGINT 
									IEGen.IdMoneda 						= fPag.IdMoneda;//Liquidacion;  		 				//	NVARCHAR(3)
									IEGen.Monto 						= IEGen.Monto.add(fPag.Monto);//LiquidacionFormaDePago;//fPag.MontoFormaDePago;  		 				//	DECIMAL(26,8)
									IEGen.FechaLiquidacion 				= Fecha.stringToSqlDate(cli.FechaPago);  		 		//	NVARCHAR(10)                                                                      
									IEGen.IdConcepto 					= "CIECSL";  		 			//	NVARCHAR(6)
									IEGen.IdTipoPersonaTitularPago 		= cli.TipoSocioNegocio;  		//	NVARCHAR(3)    -- Es el IdTipoSocioDeNegocio (CLI / EMP / PRO / OPE)
									IEGen.IdPersonaTitularPago 			= cli.IdPersona;  		 	//	BIGINT
									IEGen.IdPortafolioTitularPago 		= cli.IdPortafolio;  		//	BIGINT
									IEGen.IdCaja 						= 0L;  		 				//	BIGINT
									IEGen.SecuenciaAgrupacion 			= ""; 		//	NVARCHAR(40)  -- Movimientos con una misma secuencia se agrupan en un mismo comprobante(también influyen parámetros de agrupación)
									IEGen.SecuenciaRelacionada 			= 0L;	 	//	BIGINT		  -- Permite relacionar distintos mov y generar una orden de liquidación entre ellos
									IEGen.Glosa 						= "";  		 				//	NVARCHAR(260)														   	
								   	//Parámetros Referenciales(Opcionales)
									//IEGen.IdEmpleadoDigitador			= operacionCerrar.p_IdUsuario;  		 	//	BIGINT
									IEGen.IdEmpleadoDigitador			= in.p_IdUsuario;  		 	//	BIGINT									
									IEGen.IdSucursalEmpleadoDigitador	= 0L;  	//	BIGINT
									IEGen.IdTipoProducto				= "";  		 		//	NVARCHAR(4)
									IEGen.IdTipoOperacion				= "SORLET";  		 		//	NVARCHAR(6)									    
									IEGen.IdTipoComprobante				= "CUS";  		 	//	NVARCHAR(3)
									IEGen.IdComprobante					= cli.IdEventoCapital;  		 		//	BIGINT
									IEGen.FechaComprobante				= Fecha.toFormat(cli.FechaPago,"YYYY-MM-dd hh:mm:ss");  		 		//	NVARCHAR(40)
									IEGen.FechaLiquidacionComprobante	= Fecha.stringToSqlDate(cli.FechaPago);  	//	NVARCHAR(40)
									
									IEGen.IdOperacion					= fPag.IdEventoCapitalCliente;  		 			//	BIGINT			
									IEGen.FechaOperacion				= Fecha.toFormat(cli.FechaPago,"YYYY-MM-dd hh:mm:ss");  		 		//	NVARCHAR(40)
									IEGen.IdCondicionLiquidacion		= "";  		//	NVARCHAR(2)
									IEGen.IdBolsa						= "";  		 				//	NVARCHAR(3)
									IEGen.IdMonedaOperacion				= fPag.IdMoneda;  		 	//	NVARCHAR(3)
									IEGen.MontoOperacion				= IEGen.MontoOperacion.add(fPag.Monto);//LiquidacionFormaDePago;   				//	DECIMAL(26,8)
									IEGen.IdTipoReajuste				= "";  		 		//	NVARCHAR(3)
									IEGen.FechaParidad					= "0001-01-01";  		 			//	NVARCHAR(40)
									IEGen.ValorParidad					= BigDecimal.ZERO;   				// 	DECIMAL(26,8)
									IEGen.FolioCartera					= 0L;  		 			//	BIGINT
									IEGen.IdInstrumento					= 0L;  		 		//	BIGINT
									IEGen.IdTipoInstrumento				= "";  		 	//	NVARCHAR(3)
									IEGen.IdClasificacionTipoInstrumento= ""; //	NVARCHAR(3)
								    //Indicadores
									IEGen.Bilateral 					= "N";		        //	NVARCHAR(1)  -- Liquidación bilateral entre corredores, no pasa por el CCLV
									IEGen.GeneraComprobante 			= "S"; 		//	NVARCHAR(1)  -- S/N: genera comprobante automático
									IEGen.AgruparEnComprobante 			= "N"; 	//	NVARCHAR(1)  -- S/N: permite agrupar varios mov. en un comprobante
									IEGen.MueveCuentaInversion			= "N";  			//	NVARCHAR(1)  -- S/N: mueve cuenta inversion // *** No se usa el campo, se debe borrar ? ***
									IEGen.TitularPago					= "S";  		 			//	NVARCHAR(1)  -- S/N: Socio de negocio que genera el movimiento es quién cobra o paga
									
									entTes.p_IngresoEgresoGenerar.add(IEGen);
								}
							}
							
							for (EventoCapitalClienteFormaDePago fPag :formasDePagoCliente){
								if (cli.IdEventoCapital.equals(fPag.IdEventoCapital) && cli.IdEventoCapitalCliente.equals(fPag.IdEventoCapitalCliente) && !cli.IdPersona.equals(informacionEmpresa.IdPersona) && !fPag.IdMedioDePago.equalsIgnoreCase("CIN")){
									FormaDePago_Entrada FPagTes = new FormaDePago_Entrada();
									FPagTes.NumeroReferencia			= fPag.IdEventoCapitalCliente;				//  BIGINT        Id Referencial (para unir el movimiento con su forma de pago)
									FPagTes.NumeroReferenciaFormaDePago	= fPag.IdEventoCapitalCliente;	//  BIGINT        Id Referencia para FP (1,2,3,..) debe ser único por FP aunque sean de distintos movimientos
									FPagTes.IdMedioDePago				= fPag.IdMedioDePago;          		//	NVARCHAR(3)   Ej.: CHE, EFE, TRA, VVI, CIN
									FPagTes.IdModalidadDePago			= fPag.IdModalidadPago;      		//	NVARCHAR(6)   Ej.: MODCAJ: Por Caja   MODDEP: Depósito
									FPagTes.IdMoneda					= fPag.IdMoneda;//Liquidacion;               		//	NVARCHAR(3)
									FPagTes.Monto						= fPag.Monto;//LiquidacionFormaDePago; 							//	DECIMAL(26,8)
									FPagTes.IdCuentaBancariaCorredora	= 0L; 		//	BIGINT
									FPagTes.IdTarjetaBancariaCorredora	= 0L; 	//	BIGINT
									FPagTes.IdChequera					= 0L; 					//	BIGINT
									FPagTes.NumeroDocumentoCorredora	= 0L; 		//	BIGINT
									FPagTes.FechaDocumentoPagoCorredora	= "0001-01-01 00:00:00";	//	NVARCHAR(40)
									{
										FPagTes.IdCuentaBancariaSocio		= fPag.IdCuentaBancaria; 			//	BIGINT
										FPagTes.IdBancoSocio				= fPag.IdBanco; 					//	BIGINT
										FPagTes.NombreCuentaBancariaSocio	= fPag.NombreBanco;		//	NVARCHAR(100)
										FPagTes.NumeroCuentaBancariaSocio	= fPag.NumeroCuentaBancaria;		//	NVARCHAR(100)
										FPagTes.IdTipoCuentaBancariaSocio	= fPag.IdTipoCuentaBancaria;		//	NVARCHAR(4)
										FPagTes.NombreTipoCuentaBancariaSocio = fPag.NombreTipoCuentaBancaria; 	//  NVARCHAR(100)
										FPagTes.IdTarjetaBancariaSocio		= 0L;			//	BIGINT
										FPagTes.IdTipoTarjetaBancariaSocio	= fPag.IdTipoTarjetaBancaria;  	//	NVARCHAR(40)
										FPagTes.NombreTipoTarjetaBancariaSocio = ""; //	NVARCHAR(100)
										FPagTes.NumeroTarjetaBancariaSocio	= fPag.NumeroTarjetaBancaria;  	//	NVARCHAR(100)
									}
									
									FPagTes.NumeroSerieSocio			= "";  				//	NVARCHAR(40)
									FPagTes.NumeroDocumentoSocio		= 0L;			//	BIGINT
									FPagTes.FechaDocumentoPagoSocio		= Fecha.toFormat(fPag.FechaVctoDocumento,"YYYY-MM-dd hh:mm:ss").toString();//	NVARCHAR(40)
									entTes.p_FormaDePago.add(FPagTes);
								}
							}
						}
					}
				}				
			
				log.write("CUS.Llamada Ingreso Tesoreria");
				if (entTes.p_IngresoEgresoGenerar.size() > 0 && entTes.p_FormaDePago.size() > 0){
					tesOut = tes.logica(con, entTes);
					 out.P_OUT_MENSAJES.addAll(tesOut.P_OUT_MENSAJES);		
					if (MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES) > 0){
						return out;
					}
				}
			}
			{
				//genero CARGO ABONO cta inversion
				CrearCargoAbonoEntrada entCta = new CrearCargoAbonoEntrada();
				entCta.p_IdEmpresa = in.p_IdEmpresa;
				entCta.p_IdUsuario = in.p_IdUsuario;
				entCta.p_IdModulo = in.p_IdModulo;
				entCta.p_simularOperacion = "N";
				entCta.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
				entCta.p_FechaSistema = fechaSistema;
				entCta.p_EsIngresoManual	= "N";
				for (EventoCapital ev: in.p_EventoCapital){
					for (EventoCapitalCliente cli: ClientesConDerecho){
						if (cli.IdEventoCapital.equals(ev.IdEventoCapital)){
							for (EventoCapitalClienteFormaDePago fPag :formasDePagoCliente){
								if (cli.IdEventoCapital.equals(fPag.IdEventoCapital) && cli.IdEventoCapitalCliente.equals(fPag.IdEventoCapitalCliente) && cli.TipoSocioNegocio.equalsIgnoreCase("CLI")){
									Long IdMovtoTeso = 0L;
									for (IngresoEgresoGenerado_Salida tesOutGen : tesOut.P_OUT_INGRESOEGRESOGENERADO){
										if (fPag.IdEventoCapitalCliente.equals(tesOutGen.NumeroReferencia)){
											IdMovtoTeso = tesOutGen.IdIngresoEgreso;
										}
									}
									ObjCargoAbonoAGenerar objCtaInCli = new ObjCargoAbonoAGenerar();
									objCtaInCli.NumeroCargoAbono 				= fPag.IdEventoCapitalCliente; 
									objCtaInCli.IdPortafolio 					= cli.IdPortafolio; 
									objCtaInCli.IdMoneda 						= MonedaSistema;//fPag.IdMoneda;//Liquidacion; 
									objCtaInCli.IdTipoMovimiento 				= "MOVABN"; 
									objCtaInCli.IdConcepto						= "CIECSL"; //validar si hay intermesa para el concepto relacionado
									objCtaInCli.Descripcion						= ""; 
									objCtaInCli.IdCondicionDeLiquidacion		= "T0"; 
									objCtaInCli.FechaCondicionDeLiquidacion		= Fecha.stringToSqlDate(ev.FechaPago); 
									objCtaInCli.Monto							= fPag.Monto;//LiquidacionFormaDePago;//fPag.MontoFormaDePago; 
									objCtaInCli.IdEjecutivo						= in.p_IdUsuario; 
									objCtaInCli.IdSucursal						= 0L; 
									objCtaInCli.IdTipoProducto					= ""; 
									objCtaInCli.IdTipoOperacion					= "SORLET"; 
									objCtaInCli.IdOperacion						= fPag.IdEventoCapitalCliente; 
									objCtaInCli.FechaOperacion					= Fecha.toFormat(cli.FechaPago,"YYYY-MM-dd hh:mm:ss"); 
									objCtaInCli.FechaLiquidacionOperacion		= Fecha.toFormat(cli.FechaPago,"YYYY-MM-dd hh:mm:ss"); 
									objCtaInCli.FechaVencimientoOperacion		= Fecha.toFormat(cli.FechaPago,"YYYY-MM-dd hh:mm:ss"); 
									objCtaInCli.IdMonedaOperacion				= fPag.IdMoneda;//Liquidacion; 
									objCtaInCli.MontoOperacion					= fPag.Monto;//LiquidacionFormaDePago;//fPag.MontoFormaDePago; 
									objCtaInCli.TipoCambioOperacion				= BigDecimal.ZERO; 
									objCtaInCli.IdContraparte					= 0L; 
									objCtaInCli.IdTipoComprobante				= "CUS"; 
									objCtaInCli.IdComprobante					= fPag.IdEventoCapital; 
									objCtaInCli.FolioComprobante				= ""; 
									objCtaInCli.IdComprobanteDetalle			= 0L; 
									objCtaInCli.IdComprobanteFormaDePago		= 0L; 
									objCtaInCli.IdTipoMovimientoAsociado		= "MOVING"; 
									objCtaInCli.IdMovimientoAsociado			= IdMovtoTeso; 
									objCtaInCli.FechaComprobante				= Fecha.toFormat(cli.FechaPago,"YYYY-MM-dd hh:mm:ss"); 
									objCtaInCli.IdPersonaPagoCobro				= cli.IdPersona; 
									objCtaInCli.IdInstrumento					= 0L; 
									objCtaInCli.IdTipoInstrumento				= ""; 
									objCtaInCli.Nemo							= ""; 
									objCtaInCli.EsIndicadorBilateral			= "N"; 
									objCtaInCli.EsCargoAbonoPorTraspaso			= "N"; 
									objCtaInCli.IdPortafolioTraspaso			= 0L; 
									objCtaInCli.GeneroContraparteEnTesoreria	= "S"; 
									objCtaInCli.CertificadoAutomatico			= "S";
									
									entCta.p_CargoAbonoAGenerar.add(objCtaInCli);
								}
							}
						}
					}
				}
				CrearCargoAbono cta = new CrearCargoAbono();
				log.write("CUS.Llamada Ingreso Cuenta Inversión");
				CrearCargoAbonoSalida ctaOut = cta.logica(con, entCta);
				out.P_OUT_MENSAJES.addAll(ctaOut.P_OUT_MENSAJES);	
				if (MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES) > 0){
					return out;
				}
			}
			
			{
				NotificarEventoCapital_Entrada paramsNotif = new NotificarEventoCapital_Entrada();
				paramsNotif.p_IdEmpresa = in.p_IdEmpresa;
				paramsNotif.p_IdModulo = in.p_IdModulo;
				paramsNotif.p_IdUsuario = in.p_IdUsuario;
				paramsNotif.p_IdsEventoCapital = ListaIds;
				paramsNotif.p_IdsEventoCapitalCliente = ListaCliIds;
				paramsNotif.p_EstadoEventoCapital = "CER";
				
				NotificarEventoCapital CreaNotificacion = new NotificarEventoCapital();
				NotificarEventoCapital_Salida resulNotif  = CreaNotificacion.logica(con, paramsNotif);
				out.P_OUT_MENSAJES.addAll(resulNotif.P_OUT_MENSAJES);	
				if (MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES) > 0){
					return out;
				}
			}
			
			{
				//actualizo estado de los eventos
				sql.batch(con, SQL_UPDATE_EVENTO_CAPITAL, updateEventoCapital, EventoCapitalUpd.class);
				sql.batch(con, SQL_UPDATE_EVENTO_CAPITAL_CLIENTE, updateEventoCapital, EventoCapitalUpd.class);
				mapMsg.clear();
				//mapMsg.put("NumeroDividendo", ev.NumeroDividendo.toString());
				out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_OK,"UI_EVENTO_CERRADO",mapMsg,con,in.p_IdEmpresa));
				Id++;
			}
			//Notificacion
		}
			
		log.write("FIN::CUS.CerrarEventoCapitalSorteoLetras");
		return out;
		
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		Gson gson = Utils.getGsonBuilder();
		CerrarEventoCapitalSorteoLetrasEntrada jin = gson.fromJson(in.toString(), CerrarEventoCapitalSorteoLetrasEntrada.class);
		CerrarEventoCapitalSorteoLetrasSalida out = logica(conn, jin);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public static void main(String[] args)  throws Exception,SQLException {
		// TODO Auto-generated method stub
		Connection conn = null;
		Gson gson = new Gson();
		
		try {
			System.out.println("== BEGIN ==");
			conn = PlataformaUtils.getDESAConnection();
			//datos entrada364858
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":25,\"p_IdModulo\":\"CUS\",\"p_Origen\":\"UI\",\"p_UsaReglasNegocio\":\"S\",\"p_IdEventoCapital\":[365125]}";
			CerrarEventoCapitalSorteoLetrasEntrada objIn = gson.fromJson(json,CerrarEventoCapitalSorteoLetrasEntrada.class);
			//mientras pruebo hare el rollback
			CerrarEventoCapitalSorteoLetras demo = new CerrarEventoCapitalSorteoLetras();
			
			CerrarEventoCapitalSorteoLetrasSalida ObjOut=demo.logica(conn, objIn);
				
			System.out.println(ObjOut.toString());
			
			conn.rollback();
			
		}catch (Exception e){
			e.printStackTrace();
		}finally {
			conn.close();
		}
			System.out.println("== END ==");

	};
}