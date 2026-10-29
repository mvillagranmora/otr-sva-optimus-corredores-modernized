package cl.bcs.APV;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.APV.AsignarContratoARecaudacion.ParametrosAsignacionContratoRecaudacion;
import cl.bcs.APV.CrearRecaudacionCliente.Cliente;
import cl.bcs.APV.CrearRecaudacionDeposito.Recaudacion;
import cl.bcs.APV.ProcesarDistribucion.ParametrosRecaudacion;
import cl.bcs.APV.ProcesarDistribucion.PortafolioReaperturaRecaudacion;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class GrabarRecaudacionTraspaso implements JavaCall   {
	
	static final Logger logger = LogManager.getLogger(GrabarRecaudacionTraspaso.class);
	public String SQL_CAMBIAR_ESTADO_TRASPASO = loadResourceToString("cl/bcs/APV/sql/CambiarEstadoTraspasoAPV.sql");
	public String SQL_CAMBIAR_ESTADO_CONTRATO = loadResourceToString("cl/bcs/APV/sql/CambiarEstadoContratosSAAPV.sql");
	
	/*
	 *   Clases IN/OUT	*
	*/
	public static class RecaudacionConciliacion{
		/*Recaudacion*/
		public Long 		IdTraspaso;
		public Long 		IdDetalleTraspaso;
		public Long 		IdContratoSAAPV;
		public Long 		FolioCartola;
		public Long 		LineaCartola;
		public Long			IdSucursal;
		public String		FechaIngreso;
		public String		CuentaCorriente;
		public String		IdTipoDeposito;
		public Long			IdBanco;
		public Long			IdRegimenTributario;
		public String		Regimen;
		public String 		NumeroDocumento;
		public Long			MontoDeposito;
		public Long			MontoDetalle;
		/*Recaudacion Cliente*/
		public Long			IdCliente;
		public String		Origen;
		public String		TipoAfiliado;
		/*Contrato Traspaso*/
		public Long			IdPersona;
		public Long			IdEmpleador;
		public String		FolioContratoSAAPV;
	}
	
	public static class CambiarEstadoTraspaso {
		public String 		IdEstado;
		public Long			IdEmpresa; 
		public Long			IdTraspaso;
		public Long			IdContratoSAAPV;
		public String		CierreSolicitud;
	}
	
	public static class CambiarEstadoContrato {
		public String		IdEstado;
		public String		IdEstadoEnvioNotificacion;
		public String		IdEstadoSolicitud;
		public String		IdFolioContratoSAAPV;
		public Long			IdPersona;
		public Long			IdEmpleador;
	}
	
	public static class Resultado{
		public String 		CodigoResultado;
		public String 		NombreResultado;
		public Long 		IdResultado;
	}
	//  SERVICIO ENTRADA
	public static class ParametrosEntradaServicio {
		public Long								p_IdEmpresa;
		public Long								p_IdUsuario;
		public String							p_IdModulo;
		public String							p_Origen;
		public String							p_Fecha;
		public List<RecaudacionConciliacion>	p_RecaudacionConciliacion;
		public List<Long>						p_IdsDetalleTraspasoReapertura;
		
		public ParametrosEntradaServicio() {
			this.p_RecaudacionConciliacion = new ArrayList<RecaudacionConciliacion>();
			this.p_IdsDetalleTraspasoReapertura = new ArrayList<Long>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}			

	// SERVICIO SALIDA
	public static class ParametrosSalidaServicio {		
		public List<MensajeSalida>		P_OUT_MENSAJES;
		public Long						P_OUT_ERRORESCONTADOR;
		public List<Resultado>			P_OUT_IDRESULTADO;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public ParametrosSalidaServicio(){
			P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			P_OUT_IDRESULTADO = new ArrayList<Resultado>();
		}		
	};

	public static class ContratoConsultado {
		public  Long		IdContratoSAAPV;
	    public 	String		IdEstado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
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
	 *   Metodos Basicos	*	
	 ************************/
	public ParametrosSalidaServicio logica(Connection con,ParametrosEntradaServicio in)throws Exception {
		logger.info("INICIO::GrabarRecaudacionTraspaso");
		long inicioServicio = System.currentTimeMillis();
		SQL sql = new SQL();
		ParametrosSalidaServicio salida	= new ParametrosSalidaServicio ();
		MensajeSalida msg = new MensajeSalida();
		Long IdRecaudacion = 0L;
		String FolioContratoSAAPV = "";
		Long IdContratoSAAPV = 0L;
		Long IdTraspaso = 0L;
		List<Recaudacion> lstParamsRecaudacion = new ArrayList<Recaudacion>();
		List<Cliente> lstParamsRecaudacionCliente = new ArrayList<Cliente>();
		List<CambiarEstadoTraspaso> lstTraspaso = new ArrayList<CambiarEstadoTraspaso>();
		List<CambiarEstadoContrato> lstContrato = new ArrayList<CambiarEstadoContrato>();
		
		Recaudacion recaudacionEntrada = new Recaudacion();
		recaudacionEntrada.IdRecaudacion 		= 0L;
		recaudacionEntrada.IdEmpresa 			= in.p_IdEmpresa;
		recaudacionEntrada.FolioCartola 		= in.p_RecaudacionConciliacion.get(0).FolioCartola; 			
		recaudacionEntrada.LineaCartola 		= in.p_RecaudacionConciliacion.get(0).LineaCartola; 						
		recaudacionEntrada.Sucursal 			= in.p_RecaudacionConciliacion.get(0).IdSucursal == null ? "" : in.p_RecaudacionConciliacion.get(0).IdSucursal.toString(); 					
		recaudacionEntrada.FechaDeposito 		= in.p_RecaudacionConciliacion.get(0).FechaIngreso; 				
		recaudacionEntrada.CuentaCorriente 		= in.p_RecaudacionConciliacion.get(0).CuentaCorriente.toString(); 							
		recaudacionEntrada.MontoDeposito 		= BigDecimal.valueOf(in.p_RecaudacionConciliacion.get(0).MontoDeposito);					
		recaudacionEntrada.ConceptoRecaudacion 	= "";
		recaudacionEntrada.Origen 				= in.p_Origen; 							
		recaudacionEntrada.Observacion 			= "";							
		recaudacionEntrada.NumeroDocumento 		= in.p_RecaudacionConciliacion.get(0).IdContratoSAAPV; 					
		recaudacionEntrada.IdTipoDeposito 		= in.p_RecaudacionConciliacion.get(0).IdTipoDeposito;
		recaudacionEntrada.IdBanco 				= in.p_RecaudacionConciliacion.get(0).IdBanco;
		recaudacionEntrada.IdRegimenTributario 	= in.p_RecaudacionConciliacion.get(0).IdRegimenTributario;
		recaudacionEntrada.IdCarga 				= 0L;
		recaudacionEntrada.IdEstado 			= "ING";
		lstParamsRecaudacion.add(recaudacionEntrada);
		
		/*Despues de crear la relacion entre un contrato saapv de tipo traspaso y el portafolio, se debe crear en recaudacion y recaudacion cliente*/
		CrearRecaudacionDeposito.ParametrosEntradaServicio depositoRecaudacion = new CrearRecaudacionDeposito.ParametrosEntradaServicio();
		CrearRecaudacionDeposito depositoRecaudacionLogica = new CrearRecaudacionDeposito();
		depositoRecaudacion.p_IdEmpresa 	= in.p_IdEmpresa;
		depositoRecaudacion.p_IdUsuario 	= in.p_IdUsuario;
		depositoRecaudacion.p_IdModulo 		= in.p_IdModulo;
		depositoRecaudacion.p_Tipo 			= "I";
		depositoRecaudacion.p_Origen 		= in.p_Origen;
		depositoRecaudacion.p_Parametros 	= lstParamsRecaudacion;
		CrearRecaudacionDeposito.ParametrosSalidaServicio SalidaDeposito = depositoRecaudacionLogica.logica(con, depositoRecaudacion);

		for(RecaudacionConciliacion Fila : in.p_RecaudacionConciliacion){
			if(SalidaDeposito.P_OUT_IDREC > 0){
				IdRecaudacion = SalidaDeposito.P_OUT_IDREC;
				
				Resultado resultadoServicios = new Resultado();
				resultadoServicios.CodigoResultado = "REC";
				resultadoServicios.NombreResultado = "Recaudacion";
				resultadoServicios.IdResultado = IdRecaudacion;
				salida.P_OUT_IDRESULTADO.add(resultadoServicios);
				
				Cliente recaudacionCliente= new Cliente();
				recaudacionCliente.IdCliente = Fila.IdCliente;
				recaudacionCliente.IdRecaudacionCliente = 0L;
				recaudacionCliente.IdRecaudacion = IdRecaudacion;
				recaudacionCliente.IdRecaudacionEmpleador = 0L;
				recaudacionCliente.IdEmpresa = in.p_IdEmpresa;
				recaudacionCliente.IdCliente = Fila.IdCliente;
				recaudacionCliente.FechaIngreso = Fila.FechaIngreso;
				recaudacionCliente.TipoAfiliado = Fila.TipoAfiliado;
				recaudacionCliente.IdRegimenTriburario = Fila.Regimen;
				recaudacionCliente.MontoDepositoConvenido = BigDecimal.valueOf(Fila.Regimen.contains("DC") == true ? Fila.MontoDetalle : 0L);
				recaudacionCliente.MontoDepositoAPV =  BigDecimal.valueOf(Fila.Regimen.contains("APV") == true ?  Fila.MontoDetalle : Fila.Regimen.contains("BF") ? Fila.MontoDetalle: 0L);
				recaudacionCliente.Origen = Fila.Origen;
				recaudacionCliente.IdEstado = "ING";
				lstParamsRecaudacionCliente.add(recaudacionCliente);
				
			}else{
				msg.id = 2L;
				msg.tipo = "ERROR";
				msg.codigo = "ERROR_CREAR_RECAUDACION";
				msg.descripcion = "ERROR al crear registro Recaudacion.";
				salida.P_OUT_MENSAJES.add(msg);
			}
			
			/*Se llena Lista para cambiar Estado Contrato Traspaso y Traspaso*/
			if(Fila.FolioContratoSAAPV != FolioContratoSAAPV && Fila.IdTraspaso != IdTraspaso){
				FolioContratoSAAPV = Fila.FolioContratoSAAPV;
				IdTraspaso = Fila.IdTraspaso;
				IdContratoSAAPV = Fila.IdContratoSAAPV;
				
				CambiarEstadoTraspaso Traspaso = new CambiarEstadoTraspaso();
				Traspaso.IdContratoSAAPV = Fila.IdContratoSAAPV;
				Traspaso.IdEmpresa = in.p_IdEmpresa;
				Traspaso.IdTraspaso = Fila.IdTraspaso;
				Traspaso.IdEstado = "COM";
				Traspaso.CierreSolicitud = in.p_Fecha;
				lstTraspaso.add(Traspaso);	
			}			
			
			CambiarEstadoContrato Contrato = new CambiarEstadoContrato();
			Contrato.IdPersona = Fila.IdPersona;
			Contrato.IdEmpleador = Fila.IdEmpleador;
			Contrato.IdFolioContratoSAAPV = Fila.FolioContratoSAAPV;
			Contrato.IdEstado = "COM"; 
			Contrato.IdEstadoEnvioNotificacion = "";
			Contrato.IdEstadoSolicitud = "COM";
			lstContrato.add(Contrato);
		}	
		
		CrearRecaudacionCliente.ParametrosEntradaServicio recaudacionCliente = new CrearRecaudacionCliente.ParametrosEntradaServicio();
		CrearRecaudacionCliente RecaudacionClienteLogica = new CrearRecaudacionCliente();
		recaudacionCliente.p_IdEmpresa 	= in.p_IdEmpresa;
		recaudacionCliente.p_IdUsuario 	= in.p_IdUsuario;
		recaudacionCliente.p_IdModulo 	= in.p_IdModulo;
		recaudacionCliente.p_Tipo 		= "";
		recaudacionCliente.p_Origen 	= in.p_Origen;
		recaudacionCliente.p_Parametros = lstParamsRecaudacionCliente;
		CrearRecaudacionCliente.ParametrosSalidaServicio SalidaRecaudacionCliente = RecaudacionClienteLogica.logica(con, recaudacionCliente);
		
		if(SalidaRecaudacionCliente.P_OUT_ERRORESCONTADOR != null && SalidaRecaudacionCliente.P_OUT_ERRORESCONTADOR > 0){
			msg.id = 2L;
			msg.tipo = "ERROR";
			msg.codigo = "ERROR_RECAUDACION_CLIENTE";
			msg.descripcion = "Problema al crear registro Recaudacion.";
			salida.P_OUT_MENSAJES.add(msg);
		}else {
			/*Una vez que se envio a Recaudar se debe enviar a Conciliar*/
			ConciliarDeposito.Recaudacion IdRec = new ConciliarDeposito.Recaudacion();
			ConciliarDeposito.ParametrosEntradaServicio ObjinCon = new ConciliarDeposito.ParametrosEntradaServicio();
			IdRec.IdRecaudacion = IdRecaudacion;
			List<ConciliarDeposito.Recaudacion> ParamsConci = new ArrayList<ConciliarDeposito.Recaudacion>();
			ParamsConci.add(IdRec);
			ObjinCon.p_IdEmpresa = in.p_IdEmpresa;
			ObjinCon.p_IdUsuario = in.p_IdUsuario;
			ObjinCon.p_IdModulo = "APV";
			ObjinCon.p_Parametros = ParamsConci;	
			ConciliarDeposito ConcilioDeposito = new ConciliarDeposito();
			ConciliarDeposito.ParametrosSalidaServicio ConciliarDeposito = ConcilioDeposito.logica(con, ObjinCon);
			
			for (MensajeSalida fila : ConciliarDeposito.P_OUT_MENSAJES) {
				msg = new MensajeSalida();
				msg.id = IdRecaudacion;//fila.id;
				msg.tipo = "OK";
				msg.codigo = fila.codigo;
				msg.descripcion = fila.descripcion;
				salida.P_OUT_MENSAJES.add(msg);
								
				if(fila.codigo.equals("CONCILIADO_OK")){
					/*Una vez que se envio a Conciliar se debe relacionar el Contrato con la Recaudación*/
					List<ParametrosAsignacionContratoRecaudacion> lstParamsContrRecaudacion = new ArrayList<ParametrosAsignacionContratoRecaudacion>();
					AsignarContratoARecaudacion.ParametrosEntradaServicio paramAsigna = new AsignarContratoARecaudacion.ParametrosEntradaServicio();
					
					for(CrearRecaudacionCliente.Resultado Resultado   : SalidaRecaudacionCliente.P_OUT_RESULTADO) {
						ParametrosAsignacionContratoRecaudacion pConRec = new ParametrosAsignacionContratoRecaudacion();
						pConRec.IdContratoSAAPV = IdContratoSAAPV;
						pConRec.IdRecaudacionCliente = Resultado.IdRecaudacionCliente;
						lstParamsContrRecaudacion.add(pConRec);
					}
					
					
					paramAsigna.p_IdEmpresa = in.p_IdEmpresa;
					paramAsigna.p_IdModulo	= in.p_IdModulo;
					paramAsigna.p_IdUsuario = in.p_IdUsuario;
					paramAsigna.p_Origen	= in.p_Origen;
					paramAsigna.p_Parametros = lstParamsContrRecaudacion;
					
					AsignarContratoARecaudacion AsignoContratoARecaudacion = new AsignarContratoARecaudacion();
					AsignoContratoARecaudacion.logica(con, paramAsigna);

					/*Una vez que se envio a Conciliar se debe enviar a Distribuir*/	
					ParametrosRecaudacion pRec = new ParametrosRecaudacion();
					List<ParametrosRecaudacion> ListRec = new ArrayList<ParametrosRecaudacion>();
					ProcesarDistribucion.ParametrosEntradaServicio paramDis = new ProcesarDistribucion.ParametrosEntradaServicio();
					pRec.IdRecaudacion = IdRecaudacion;		
					ListRec.add(pRec);	
					paramDis.p_IdEmpresa = in.p_IdEmpresa;
					paramDis.p_IdModulo	= in.p_IdModulo;
					paramDis.p_IdUsuario = in.p_IdUsuario;
					paramDis.p_Opcion = "RECTRA";
					paramDis.p_Parametros = ListRec;
					paramDis.p_IdsDetalleTraspasoReapertura = in.p_IdsDetalleTraspasoReapertura;
					paramDis.p_DistribucionOrigen = "S";
					ProcesarDistribucion ProcesoDistribucion = new ProcesarDistribucion();
					ProcesarDistribucion.ParametrosSalidaServicio ProcesarDistibucion = ProcesoDistribucion.logica(con, paramDis);
						
					for (MensajeSalida row : ProcesarDistibucion.P_OUT_MENSAJES) {
						
						if(MensajeSalida.MENSAJE_ERROR.equalsIgnoreCase(row.tipo)){
							msg.id = 2L;
							msg.tipo = "ERROR";
							msg.codigo = "ERROR_RECAUDACION_CLIENTE";
							msg.descripcion = "Problema en el proceso de distribucion.";
							salida.P_OUT_MENSAJES.add(row);
						}else {
						
						
						msg = new MensajeSalida();
						msg.id = IdRecaudacion;
						msg.tipo = "OK";
						msg.codigo = row.codigo;
						msg.descripcion = row.descripcion;
						salida.P_OUT_MENSAJES.add(msg);	
						
						if(row.tipo == "OK"){
							/*Una vez enviado a Recaudar,Conciliar y Distribuir se debe cambiar el Estado del Traspaso y el Contrato Traspaso*/
							int[] result = sql.batch(con,SQL_CAMBIAR_ESTADO_TRASPASO,lstTraspaso, CambiarEstadoTraspaso.class);
							if(result != null && result.length > 0 && result[0] == 1){
							    msg = new MensajeSalida();
								msg.id = IdRecaudacion;
								msg.tipo = "OK";
								msg.codigo = "CAMBIAR_ESTADO_TRASPASO";
								msg.descripcion = "Cambio de Estado de Traspaso exitoso Folio: " + IdTraspaso.toString();
								salida.P_OUT_MENSAJES.add(msg);
								
								int[] resultadoContrato = sql.batch(con,SQL_CAMBIAR_ESTADO_CONTRATO,lstContrato, CambiarEstadoContrato.class);
								if(resultadoContrato != null && resultadoContrato.length > 0 && resultadoContrato[0] == 1){
									msg = new MensajeSalida();
									msg.id = IdRecaudacion;
									msg.tipo = "OK";
									msg.codigo = "CAMBIAR_ESTADO_CONTRATO_TRASPASO";
									msg.descripcion = "Cambio de Estado del Contrato Traspaso exitoso Folio : " + FolioContratoSAAPV.toString();
									salida.P_OUT_MENSAJES.add(msg);
								}else{
									msg = new MensajeSalida();
									msg.id = 2L;
									msg.tipo = "ERROR";
									msg.codigo = "ERROR_CAMBIO_ESTADO_CONTRATO_TRASPASO";
									msg.descripcion = "No es posible cambiar el Estado al Contrato Traspaso Folio : " + FolioContratoSAAPV.toString();
									salida.P_OUT_MENSAJES.add(msg);
								}
							}else{
							    msg = new MensajeSalida();
								msg.id = 2L;
								msg.tipo = "ERROR";
								msg.codigo = "ERROR_CAMBIO_ESTADO_TRASPASO";
								msg.descripcion = "No es posible cambiar el Estado al Traspaso.";
								salida.P_OUT_MENSAJES.add(msg);
							}
							break;
						}
						
						}
						
					}
				}
			}
		}
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::GrabarRecaudacionTraspaso", finServicio - inicioServicio);
		return salida;
	}
	/********************
	 * Método Principal *
	 ********************/

	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			try {
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();				//
				 String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"APV\",\"p_Origen\":\"TRASPA\",\"p_Fecha\":\"2020-06-12\",\"p_RecaudacionConciliacion\":[{\"IdTraspaso\":332,\"IdDetalleTraspaso\":220,\"IdContratoSAAPV\":2461,\"FechaIngreso\":\"2020-06-12\",\"CuentaCorriente\":\"111111\",\"IdTipoDeposito\":\"EFE\",\"IdBanco\":29,\"IdRegimenTributario\":2,\"Regimen\":\"APV-A\",\"NumeroDocumento\":1,\"MontoDeposito\":160000,\"MontoDetalle\":160000,\"IdCliente\":1424217,\"Origen\":\"TRASPA\",\"TipoAfiliado\":\"IND\",\"IdPersona\":1775516,\"IdEmpleador\":0,\"FolioContratoSAAPV\":\"119443\"}]}";
				//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":14,\"p_IdModulo\":\"APV\",\"p_Origen\":\"TRASPA\",\"p_Fecha\":\"2020-03-31\",\"p_RecaudacionConciliacion\":[{\"IdTraspaso\":2981911,\"IdDetalleTraspaso\":1918581,\"IdContratoSAAPV\":873214,\"FolioCartola\":310320202,\"LineaCartola\":310320202,\"FechaIngreso\":\"2020-03-31\",\"CuentaCorriente\":\"123\",\"IdTipoDeposito\":\"TRA\",\"IdBanco\":8,\"IdRegimenTributario\":2,\"Regimen\":\"APV-A\",\"NumeroDocumento\":1,\"MontoDeposito\":50801050,\"MontoDetalle\":11135649,\"IdCliente\":32549,\"Origen\":\"TRASPA\",\"TipoAfiliado\":\"IND\",\"IdPersona\":83921,\"IdEmpleador\":0,\"FolioContratoSAAPV\":\"31032020\"},{\"IdTraspaso\":2981911,\"IdDetalleTraspaso\":1918582,\"IdContratoSAAPV\":873214,\"FolioCartola\":310320202,\"LineaCartola\":310320202,\"FechaIngreso\":\"2020-03-31\",\"CuentaCorriente\":\"123\",\"IdTipoDeposito\":\"TRA\",\"IdBanco\":8,\"IdRegimenTributario\":7,\"Regimen\":\"BF\",\"NumeroDocumento\":1,\"MontoDeposito\":50801050,\"MontoDetalle\":104641,\"IdCliente\":32549,\"Origen\":\"TRASPA\",\"TipoAfiliado\":\"IND\",\"IdPersona\":83921,\"IdEmpleador\":0,\"FolioContratoSAAPV\":\"31032020\"},{\"IdTraspaso\":2981911,\"IdDetalleTraspaso\":1918583,\"IdContratoSAAPV\":873214,\"FolioCartola\":310320202,\"LineaCartola\":310320202,\"FechaIngreso\":\"2020-03-31\",\"CuentaCorriente\":\"123\",\"IdTipoDeposito\":\"TRA\",\"IdBanco\":8,\"IdRegimenTributario\":3,\"Regimen\":\"APV-B\",\"NumeroDocumento\":1,\"MontoDeposito\":50801050,\"MontoDetalle\":39560760,\"IdCliente\":32549,\"Origen\":\"TRASPA\",\"TipoAfiliado\":\"IND\",\"IdPersona\":83921,\"IdEmpleador\":0,\"FolioContratoSAAPV\":\"31032020\"}]}";
				
				ParametrosEntradaServicio Objin_json = gson.fromJson(json, ParametrosEntradaServicio.class);
				GrabarRecaudacionTraspaso demo = new GrabarRecaudacionTraspaso();	
				System.out.println("ENTRADA");
	 			System.out.println(Objin_json.toString());
	 			System.out.println("SALIDA");	
				
	 			try { 
	 				ParametrosSalidaServicio ObjOut = demo.logica(con, Objin_json);
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