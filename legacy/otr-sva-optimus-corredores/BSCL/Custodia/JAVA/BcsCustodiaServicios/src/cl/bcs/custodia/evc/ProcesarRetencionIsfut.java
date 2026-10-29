package cl.bcs.custodia.evc;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.cuentaInversion.CrearCargoAbono;
import cl.bcs.cuentaInversion.CrearCargoAbono.CrearCargoAbonoEntrada;
import cl.bcs.cuentaInversion.CrearCargoAbono.CrearCargoAbonoSalida;
import cl.bcs.cuentaInversion.CrearCargoAbono.ObjCargoAbonoAGenerar;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class ProcesarRetencionIsfut implements JavaCall {
	
	private static final Logger logger = LogManager.getLogger(ProcesarRetencionIsfut.class);
	
	private final String SQL_SELECT_EVENTO_CAPITAL			= loadResourceToString("cl/bcs/custodia/sql/ProcesarRetencionIsfut_SelectEventoCapital.sql");
	private final String SQL_SELECT_EVENTO_CAPITAL_CLIENTE	= loadResourceToString("cl/bcs/custodia/sql/ProcesarRetencionIsfut_SelectEventoCapitalCliente.sql");
	
	//ENTRADAS
	public static class ProcesarRetencionIsfut_Entrada extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public Long p_IdEventoCapital;	
		public String p_UsaReglasNegocio;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InfoComprobanteCargo_Proceso extends BcsPojo{
				
		public Long					IdPortafolio;
		public String				IdMoneda;
		public String				IdMonedaLiquidacion;
		public BigDecimal			MontoComprobanteFinal; 
		public java.sql.Date		FechaLiquidacion;
		public Long					IdEjecutivo; 
		public Long					IdComprobante; 
		public String				IdTipoComprobante; 
		public Long				    FolioComprobante; 
		public java.sql.Timestamp	FechaComprobante; 
		public Long					IdInstrumento; 
		public String				IdTipoInstrumento;
		public String				Nemo; 
		public String				IdTipoProducto; 
		public String				IdTipoOperacion; 
		public Long					IdSucursal; 
		public String				IdTipoMovimiento;
		public String 				IdConcepto;
		public String				Descripcion;
		public Long 				IdPersonaPagoCobro;
				
	}
	
	  public static class EventoCapitalCliente_Entrada extends BcsPojo{
			public Long					IdEventoCapitalCliente;
			public Long					IdEventoCapital;
			public String				IsfutEventoCapitalCliente;
			public String				RetencionISFUT;
			public BigDecimal 			MontoRetencionISFUT;
			public BigDecimal 			Total;
			public String				Nemo;
		};
	
	//SALIDAS
	public static class ProcesarRetencionIsfut_Salida extends BcsPojo{
		public List<MensajeSalida>					P_OUT_MENSAJES;
		public Long									P_OUT_ERRORESCONTADOR;
		public List<ResultadoProceso_Salida>		P_OUT_RESULTADOPROCESO;		
		
		public ProcesarRetencionIsfut_Salida (){
			P_OUT_MENSAJES	= new ArrayList<>();
			P_OUT_RESULTADOPROCESO = new ArrayList<>();
			P_OUT_ERRORESCONTADOR = 0L;
		}
	}
	
	public static class ResultadoProceso_Salida {
  		public String Campo;
  		public Long Id;
  		public String Servicio;
  		public String Resultado;
  		
  		public String toString() {
  			return Utils.printJson(this, false);
  		}
  	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = getGsonBuilder();
		ProcesarRetencionIsfut_Entrada in = gson.fromJson(jin.toString(), ProcesarRetencionIsfut_Entrada.class);
		ProcesarRetencionIsfut_Salida out = logica(conn, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}	
	
	 @Trace
		public ProcesarRetencionIsfut_Salida logica(Connection conn, ProcesarRetencionIsfut_Entrada in) throws Exception {
		 logger.info("Inicio Procesar Retencion ISFUT: " + in.toString());
		 ProcesarRetencionIsfut_Salida out = new ProcesarRetencionIsfut_Salida();
		 SQL _sql = new SQL();
		 String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		 
		 HashMap<String,Object> eventoCapitalClienteparams = new HashMap<String,Object>();
		 eventoCapitalClienteparams.put("IdEventoCapital", in.p_IdEventoCapital);
		 
		 List<EventoCapitalCliente_Entrada> eventosCapitalCliente = _sql.query(conn, SQL_SELECT_EVENTO_CAPITAL_CLIENTE, eventoCapitalClienteparams,EventoCapitalCliente_Entrada.class);
		 List<InfoComprobanteCargo_Proceso> eventoCapital = _sql.query(conn, SQL_SELECT_EVENTO_CAPITAL, eventoCapitalClienteparams,InfoComprobanteCargo_Proceso.class);
			
		 BigDecimal montoRetencionAcumulable = new BigDecimal(0);
		 for(EventoCapitalCliente_Entrada evCl : eventosCapitalCliente) {
			 if("S".equals(evCl.RetencionISFUT.trim().toUpperCase())  && evCl.MontoRetencionISFUT.compareTo(BigDecimal.ZERO) == 1) { 
				 
				 montoRetencionAcumulable = montoRetencionAcumulable.add(evCl.MontoRetencionISFUT, MathContext.DECIMAL128);				 
			 }			 
		 }
		 
		 //Se genera el movimiento de cargo por la acumulacion de montos de retencion isfut de evento capital cliente
		 if (montoRetencionAcumulable.compareTo(BigDecimal.ZERO) == 1) {			 
			
				CrearCargoAbonoSalida salidaCrearCargoAbono = mueveCuentaInversion(conn, eventoCapital,  in, fechaSistema,montoRetencionAcumulable);
				for(MensajeSalida info : salidaCrearCargoAbono.P_OUT_MENSAJES) {					
					out.P_OUT_MENSAJES.add(new MensajeSalida(info.id ,info.tipo,info.codigo,info.descripcion));
				}				
				
		 }
		 out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK, "UI_EXITO", new HashMap<String,String>() ,conn,in.p_IdEmpresa));
		 return out;
	 }
	 
	 private CrearCargoAbonoSalida mueveCuentaInversion(Connection conn, List<InfoComprobanteCargo_Proceso> comprobantes, ProcesarRetencionIsfut_Entrada EntradaComprobante,String fechaSistema,BigDecimal MontoRetencionIsfut) throws Exception{
			logger.trace("INICIO::ProcesarRetencionIsfut.mueveCuentaInversion");
			CrearCargoAbonoSalida salidaCrearCargoAbono = new CrearCargoAbonoSalida();
			salidaCrearCargoAbono.P_OUT_ERRORESCONTADOR = 0L;
			salidaCrearCargoAbono.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			CrearCargoAbonoEntrada entradaCrearCargoAbono = new CrearCargoAbonoEntrada();
			CrearCargoAbono crearCargoAbono = new CrearCargoAbono();
			Long i = 0L;
			HashMap<Long,Long> ids = new HashMap<Long,Long>();
		
			for(InfoComprobanteCargo_Proceso sol : comprobantes){
				
						ObjCargoAbonoAGenerar obj = new ObjCargoAbonoAGenerar();
						obj.NumeroCargoAbono = sol.IdComprobante;
						obj.IdPortafolio = sol.IdPortafolio;
						obj.IdMoneda = sol.IdMoneda;
						obj.IdMonedaOperacion = sol.IdMonedaLiquidacion;
						obj.IdTipoMovimiento = sol.IdTipoMovimiento;//cargo a tesoreria MOVCAR;
						obj.Monto = MontoRetencionIsfut;
						obj.MontoOperacion = sol.MontoComprobanteFinal;
						obj.IdConcepto =sol.IdConcepto; //RETISF
						obj.Descripcion = sol.Descripcion;
						obj.IdCondicionDeLiquidacion = "T0";
						obj.IdEjecutivo = sol.IdEjecutivo;
						obj.IdSucursal = 0L;
						obj.IdTipoProducto = sol.IdTipoProducto;//EVCA
						obj.IdTipoOperacion = sol.IdTipoOperacion;//DIVDIN
						obj.IdOperacion =sol.IdComprobante;
						obj.FechaCondicionDeLiquidacion =fechaSistema;
						obj.FechaLiquidacionOperacion = Fecha.toStringDate(sol.FechaLiquidacion);
						obj.FechaOperacion = Fecha.toStringDate(sol.FechaComprobante);					
						obj.FechaComprobante =Fecha.toStringDate(sol.FechaComprobante);
						obj.TipoCambioOperacion = new BigDecimal("0");
						obj.IdTipoComprobante = sol.IdTipoComprobante;//EGR
						obj.IdComprobante = sol.IdComprobante;
						obj.FolioComprobante = sol.FolioComprobante.toString();//secuencia
						obj.IdComprobanteDetalle = 0L;
						obj.IdTipoMovimientoAsociado = "";
						obj.IdMovimientoAsociado = 0L;
						obj.IdInstrumento = sol.IdInstrumento;
						obj.IdTipoInstrumento = sol.IdTipoInstrumento;
						obj.Nemo = sol.Nemo;
						obj.EsCargoAbonoPorTraspaso = "N";
						obj.GeneroContraparteEnTesoreria = "N";
						obj.IdPersonaPagoCobro = sol.IdPersonaPagoCobro;
						obj.CertificadoAutomatico = "N"; //Movimiento pendiente por certificar
						ids.put(i, sol.IdComprobante);
						i++;
						entradaCrearCargoAbono.p_CargoAbonoAGenerar.add(obj);
					}
				
			
			if(entradaCrearCargoAbono.p_CargoAbonoAGenerar.size() > 0){
				entradaCrearCargoAbono.p_IdEmpresa = EntradaComprobante.p_IdEmpresa;
				entradaCrearCargoAbono.p_IdUsuario = EntradaComprobante.p_IdUsuario;
				entradaCrearCargoAbono.p_IdModulo = EntradaComprobante.p_IdModulo;
				entradaCrearCargoAbono.p_simularOperacion = "N";
				entradaCrearCargoAbono.p_UsaReglasNegocio = EntradaComprobante.p_UsaReglasNegocio;
				entradaCrearCargoAbono.p_FechaSistema = fechaSistema;
				entradaCrearCargoAbono.p_EsIngresoManual	= "N";
				salidaCrearCargoAbono = crearCargoAbono.logica(conn, entradaCrearCargoAbono);
				
				//Cambiamos el id interno por el id de la solicitud
				for(MensajeSalida mje : salidaCrearCargoAbono.P_OUT_MENSAJES){
					if(MensajeSalida.MENSAJE_ERROR.equals(mje.tipo)){
						Long id = ids.get(mje.id);
						mje.id = id;
					}
				}
			}
			logger.trace("FIN::ProcesarRetencionIsfut.mueveCuentaInversion");
			return salidaCrearCargoAbono;
		}
	 
	 public static void main(String[] args) throws Exception {		
			try (Connection con = PlataformaUtils.getDESAConnection()) {
				try {
					Gson gson = Utils.getGsonBuilder();
					String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"CUS\",\"p_IdEventoCapital\":367049,\"p_UsaReglasNegocio\":\"N\"}";
					ProcesarRetencionIsfut_Entrada Objin = gson.fromJson(json, ProcesarRetencionIsfut_Entrada.class);			
					ProcesarRetencionIsfut modificarMvtoComp = new ProcesarRetencionIsfut();
					ProcesarRetencionIsfut_Salida Objout = modificarMvtoComp.logica(con, Objin);
					System.out.println(Objout.toString());
					//con.rollback();
					con.commit();
					
			    	}catch(Exception er) {
						er.printStackTrace();	
						con.rollback();
					}
			} catch (Exception e) {
				e.printStackTrace();			
			}	
			System.out.println("== END ==");
		};
	 
	 

}
