package cl.bcs.ordenes;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.collections.map.MultiKeyMap;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.utils.LogCustodia;
import cl.bcs.custodia.utils.LogCustodia.ObjMovimiento;
import cl.bcs.ordenes.LiquidarCustodiaOrdenCompra.CarteraCompraObj_IN;
import cl.bcs.ordenes.LiquidarCustodiaOrdenCompra.LiquidarCustodiaOrdenCompra_IN;
import cl.bcs.ordenes.LiquidarCustodiaOrdenCompra.LiquidarCustodiaOrdenCompra_OUT;
import cl.bcs.ordenes.LiquidarCustodiaOrdenVenta.CarteraVentaObj_IN;
import cl.bcs.ordenes.LiquidarCustodiaOrdenVenta.LiquidarCustodiaOrdenVenta_IN;
import cl.bcs.ordenes.LiquidarCustodiaOrdenVenta.LiquidarCustodiaOrdenVenta_OUT;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class AnticiparOrden implements JavaCall{
	private final Log log = new Log("ORD.AnticiparOrden");
	
	private final String SQL_SELECT_CONCEPTOS 	= loadResourceToString("cl/bcs/ordenes/sql/AnticiparOrden_ConsultarConcepto.sql");
	private final String SQL_SELECT_CARTERAS 	= loadResourceToString("cl/bcs/ordenes/sql/AnticiparOrden_SelectCartera.sql");
	
		
	public static class AnticiparOrdenObj_IN {
		public Long 		IdCartera;
		public Long 		FolioCartera;
		public String		IdTipoOperacion;
		public Long			IdOperacion;
		public String		IdClasificacionTipoInstrumento;
		public BigDecimal	CantidadAnticipo;
		public BigDecimal	MontoAnticipo;
		public BigDecimal	TasaAnticipo;
		public String		IdCondicionLiquidacion;
		public String		FechaAnticipo;
		//public String		AccionOperacion;
		//p_PlazoOriginal no se si es necesirio usarlo
		//p_PlazoResidual no  se si es necesario usarlo
		
		
	 	public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class AnticiparOrden_IN {
		public Long								p_IdPais;
		public Long								p_IdEmpresa;
	    public Long								p_IdUsuario;
	    public Long								p_IdEmpleado;
	    public String							p_IdModulo; 
	    public String							p_UsaReglasNegocio;
	    public List<AnticiparOrdenObj_IN> 		p_Operacion;
		
		public AnticiparOrden_IN() {
			this.p_Operacion 		= new ArrayList<AnticiparOrdenObj_IN>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		};
	};
	
	public static class AnticiparOrdenObj_OUT {
		public Long 		IdCartera;		
	 	public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class AnticiparOrden_OUT {

		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;

		public List<AnticiparOrdenObj_OUT>		P_OUT_OPERACION;
		
		public AnticiparOrden_OUT() {
			this.P_OUT_OPERACION 		= new ArrayList<AnticiparOrdenObj_OUT>();
			this.P_OUT_MENSAJES 		= new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Cartera {
		public Long IdCartera;
	    public Long FolioCartera;
	    public Long FolioCarteraRelacionada;
	    public Long FolioGarantiaOrigen;
	    public Long IdEmpresa;
	    public Date FechaOperacion;
	    public Date FechaVencimientoOperacion;
	    public Long IdCliente;
	    public Long IdPortafolio;
	    public Long IdClienteRelacionado;
	    public Long IdPortafolioRelacionado;
	    public String IdEstado;
	    public String IdTipoProducto;
	    public String IdTipoCanalIngreso;
	    public String IdMonedaSistema;
	    public Long IdMovimientoCarteraEnTransito;
	    public String IdConcepto;
	    public String IdTipoOperacion;
	    public Long IdOperacion;
	    public BigDecimal INGCantidad;
	    public BigDecimal INGTasaMercadoActual;
	    public BigDecimal MontoCompra;
	    public String IdCondicionLiquidacion;
	    public String FechaAnticipo;
	    
	    public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class Concepto {
		public String IdConcepto;
		public String NombreConcepto;
		public String IdTipoOperacion;
		public String Uso;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class Garantia {
		public Long IdGarantia;
		public Long FolioGarantia;
		public Long FolioCarteraOrigen;
		public Long FolioCarteraGarantizada;
		public BigDecimal INGCantidad;
		public BigDecimal INGValorRescate;
		public BigDecimal INGMonto;
		public BigDecimal PrecioCompraOperacionOrigen;
		public BigDecimal TasaCompraOperacionOrigen;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public AnticiparOrden_OUT logica (Connection conn, AnticiparOrden_IN in) throws Exception {
		SQL _sql = new SQL();
		AnticiparOrden_OUT out = new AnticiparOrden_OUT();
		Long Id = 0L,errTot = 0L;
		//Long TicketAtencion = 0L;
		HashMap<String,String> mapMsg = new HashMap<>();
		HashMap<String,Object> params = new HashMap<>();
		log.write("INICIO::ORD.AnticiparOrden");
		try {
			ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
			InformacionEmpresa empresa = consultarEmpresaService.logica(conn, in.p_IdEmpresa);
		    Long idPaisSistema = empresa.IdPais;
		    String monedaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
		    String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		    List<ObjMovimiento>  entLogCus = new ArrayList<>();
		    //consultar conceptos
		    List<Concepto> concepto = _sql.query(conn, SQL_SELECT_CONCEPTOS, new HashMap<>(), Concepto.class);
			//HashMap<String, Object> concepHash = Utils.List2Hash(concepto, "IdTipoOperacion");
			MultiKeyMap concepHash = new MultiKeyMap();
			for (Concepto cc : concepto) {
				concepHash.put(cc.Uso, cc.IdTipoOperacion,cc);
			}
			//concepHash.put("Uso", "IdTipoOperacion", concepto);
		    //consultar Cartera
		    List<Long> carteras = new ArrayList<>();
		    List<String> clasif = new ArrayList<>();
		    for (AnticiparOrdenObj_IN car: in.p_Operacion){
		    	carteras.add(car.IdOperacion);
		    	clasif.add(car.IdClasificacionTipoInstrumento);
		    }
		    params.put("IdOperacion", carteras);
		    params.put("IdClasificacionTipoInstrumento", clasif);
		    List<Cartera> datosCartera = _sql.query(conn, SQL_SELECT_CARTERAS, params, Cartera.class);
		    List<Cartera> datosCarteraVenta = new ArrayList<>();
		    List<Cartera> datosCarteraCompra = new ArrayList<>();
		    for (Cartera car: datosCartera){
		    	if (car.IdTipoOperacion.startsWith("COM")){
		    		datosCarteraCompra.add(car);
		    	}
		    	if (car.IdTipoOperacion.startsWith("VEN")){
		    		datosCarteraVenta.add(car);
		    	}
		    }
		    HashMap<String, Object> datosCarteraVentaHash = Utils.List2Hash(datosCarteraVenta,"IdCartera");
		    HashMap<String, Object> datosCarteraCompraHash = Utils.List2Hash(datosCarteraCompra,"IdCartera");


			//TODO:: validaciones
			/*for (CrearOperacionMesaRF_Operacion_IN ope : in.p_Operacion){
				if (selectConceptoIngresoOperacionHash.get(ope.IdTipoOperacion) == null){
					//mapMsg.put("IdOperacion", op.IdOperacion.toString());
					out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"CONCEPTOS_MESA_NO_CONFIGURADO",mapMsg,conn,in.p_IdEmpresa));
					Id++;
					break;
				}				
			}*/
		    if (datosCarteraVentaHash.isEmpty() && datosCarteraCompraHash.isEmpty()){
		    	out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"CARTERAS_NO_EXISTE",mapMsg,conn,in.p_IdEmpresa));
		    	Id++;
		    	
		    }
		    errTot = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
			if (!errTot.equals(0L) ){
				return out;
			}
					    
		    /******Genero Ticket de atencion para custodia **************/
			Long TicketAtencion = LogCustodia.logCustodia(conn,entLogCus);
			/******** Liquido las operaciones de venta (REporte PEru, y podria ir Simultaneas chile)*******/
		    LiquidarCustodiaOrdenVenta liqOVenta = new LiquidarCustodiaOrdenVenta();
		    LiquidarCustodiaOrdenVenta_IN liqOVentaIn = new LiquidarCustodiaOrdenVenta_IN();
		    LiquidarCustodiaOrdenVenta_OUT liqOVentaOut = new LiquidarCustodiaOrdenVenta_OUT();
		    {
			    liqOVentaIn.p_IdPais			= in.p_IdPais;
			    liqOVentaIn.p_IdEmpresa			= in.p_IdEmpresa;
			    liqOVentaIn.p_IdUsuario			= in.p_IdUsuario;
			    liqOVentaIn.p_IdModulo			= in.p_IdModulo; 
			    liqOVentaIn.p_UsaReglasNegocio	= in.p_UsaReglasNegocio;
			    liqOVentaIn.p_TicketAtencion	= TicketAtencion;
			    liqOVentaIn.p_OrigenLiquidacion	= "ANTICIPO";
			    for (AnticiparOrdenObj_IN ant : in.p_Operacion){
			    	for(Cartera ven: datosCarteraVenta){
			    		if (ven.IdOperacion.compareTo(ant.IdOperacion) != 0){
				    		continue;
				    	}
				       	CarteraVentaObj_IN obj = new CarteraVentaObj_IN();
					    obj.AccionOperacion			= "VENTA";//ant.AccionOperacion;
					    obj.IdCartera				= ven.IdCartera;
					    obj.FolioCartera			= ven.FolioCartera;
					    obj.IdTipoOperacion			= ven.IdTipoOperacion;
					    obj.IdOperacion				= ven.IdOperacion;
					    obj.IdConcepto				= ((Concepto) concepHash.get("ANTICIPO", ven.IdTipoOperacion)).IdConcepto;
					    obj.Cantidad				= ant.CantidadAnticipo;
					    obj.Monto					= ant.MontoAnticipo;
					    obj.Tasa					= ant.TasaAnticipo;
					    obj.IdCondicionLiquidacion	= ant.IdCondicionLiquidacion;
					    obj.Fecha					= ant.FechaAnticipo;
						
					    liqOVentaIn.p_DatosCartera.add(obj);
					    for (Cartera car : datosCarteraCompra){
					    	if (car.IdOperacion.equals(ant.IdOperacion)){
					    		((Cartera) datosCarteraCompraHash.get(car.IdCartera)).IdCondicionLiquidacion = ant.IdCondicionLiquidacion;
					    		((Cartera) datosCarteraCompraHash.get(car.IdCartera)).FechaAnticipo = ant.FechaAnticipo;
					    	}
					    }
			    	}
			    }
		    }
		    liqOVentaOut = liqOVenta.logica(conn, liqOVentaIn);
		    out.P_OUT_MENSAJES.addAll(liqOVentaOut.P_OUT_MENSAJES);
		    /******** Liquido las operaciones de Compra (REporte PEru, y podria ir Simultaneas chile)*******/
		    LiquidarCustodiaOrdenCompra liqOCompra = new LiquidarCustodiaOrdenCompra();
		    LiquidarCustodiaOrdenCompra_IN liqOCompraIn = new LiquidarCustodiaOrdenCompra_IN();
		    LiquidarCustodiaOrdenCompra_OUT liqOCompraOut = new LiquidarCustodiaOrdenCompra_OUT();
		    {
			    liqOCompraIn.p_IdPais			= in.p_IdPais;
			    liqOCompraIn.p_IdEmpresa		= in.p_IdEmpresa;
			    liqOCompraIn.p_IdUsuario		= in.p_IdUsuario;
			    liqOCompraIn.p_IdModulo			= in.p_IdModulo; 
			    liqOCompraIn.p_UsaReglasNegocio	= in.p_UsaReglasNegocio;
			    liqOCompraIn.p_TicketAtencion	= TicketAtencion;
			    liqOCompraIn.p_OrigenLiquidacion= "ANTICIPO";
			    //for (AnticiparOrdenObj_IN ant : in.p_Operacion){//cambiar por la cartera compra asociada a la operacion de venta
			    for (Cartera ant : datosCarteraCompra){
			    	CarteraCompraObj_IN obj = new CarteraCompraObj_IN();
				    obj.AccionOperacion			= "COMPRA";//ant.AccionOperacion;
				    obj.IdCartera				= ant.IdCartera;
				    obj.FolioCartera			= ant.FolioCartera;
				    obj.IdTipoOperacion			= ant.IdTipoOperacion;
				    obj.IdOperacion				= ant.IdOperacion;
				    obj.IdConcepto				= ((Concepto) concepHash.get("ANTICIPO", ant.IdTipoOperacion)).IdConcepto;
				    obj.Cantidad				= ant.INGCantidad;
				    obj.Monto					= ant.MontoCompra;
				    obj.Tasa					= ant.INGTasaMercadoActual;
				    obj.IdCondicionLiquidacion	= ((Cartera) datosCarteraCompraHash.get(ant.IdCartera)).IdCondicionLiquidacion;
				    obj.Fecha					= ant.FechaAnticipo;
					
				    liqOCompraIn.p_DatosCartera.add(obj);
			    }
		    }
		    liqOCompraOut = liqOCompra.logica(conn, liqOCompraIn);
		    out.P_OUT_MENSAJES.addAll(liqOCompraOut.P_OUT_MENSAJES);
		    
		    errTot = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
			if (!errTot.equals(0L) ){
				return out;
			}
			//notificar anticipo reportes
			{
				NotificarAnticipoOrden notificar = new NotificarAnticipoOrden();
				NotificarAnticipoOrden.NotificarAnticipoOrden_Entrada notificarEntrada = new NotificarAnticipoOrden.NotificarAnticipoOrden_Entrada();
				notificarEntrada.p_IdEmpresa = in.p_IdEmpresa;
				notificarEntrada.p_IdUsuario = in.p_IdUsuario;
				notificarEntrada.p_IdModulo = in.p_IdModulo;
				List<NotificarAnticipoOrden.Cartera> notificarCarteras = new ArrayList<>();
				in.p_Operacion.forEach( cartera -> {
					NotificarAnticipoOrden.Cartera notificarCartera = new NotificarAnticipoOrden.Cartera();
					notificarCartera.IdCartera = cartera.IdCartera;
					notificarCarteras.add(notificarCartera);
				});
				notificar.logica(conn, notificarEntrada);
			}
			
		}catch (Exception e){
			e.printStackTrace();
		}
		log.write("FIN::ORD.AnticiparOrden");
		return out;
	};
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		Gson gson = Utils.getGsonBuilder();
		AnticiparOrden_IN jin = gson.fromJson(in.toString(), AnticiparOrden_IN.class);
		AnticiparOrden_OUT out = logica(conn, jin);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	public static void main(String[] args)  throws Exception,SQLException {
		// TODO Auto-generated method stub
		Connection conn = null;
		Gson gson = new Gson();
		
		try {
			System.out.println("== BEGIN ==");
			conn = PlataformaUtils.getQAPEConnection();
			//datos entrada
			//String json = "{\"p_IdPais\":166,\"p_IdEmpresa\":1,\"p_IdUsuario\":69,\"p_IdEmpleado\":234,\"p_IdModulo\":\"ORD\",\"p_UsaReglasNegocio\":\"S\",\"p_Operacion\":[{\"IdCartera\":2386,\"FolioCartera\":1121,\"IdTipoOperacion\":\"VENRRV\",\"IdOperacion\":29988,\"IdClasificacionTipoInstrumento\":\"RE\",\"CantidadAnticipo\":1000,\"MontoAnticipo\":7776.866615,\"TasaAnticipo\":1.3,\"IdCondicionLiquidacion\":\"T0\",\"FechaAnticipo\":\"2017-07-26\"}]}";
			String json = "{\"p_IdPais\":166,\"p_IdEmpresa\":1,\"p_IdUsuario\":69,\"p_IdEmpleado\":234,\"p_IdModulo\":\"ORD\",\"p_UsaReglasNegocio\":\"S\",\"p_Operacion\":[{\"IdCartera\":2954,\"FolioCartera\":13743,\"IdTipoOperacion\":\"COMRRV\",\"IdOperacion\":26676,\"IdTransaccionContado\":55552,\"IdTransaccionPlazo\":55553,\"IdClasificacionTipoInstrumento\":\"RE\",\"CantidadAnticipo\":1200,\"MontoAnticipo\":11844,\"TasaAnticipo\":5,\"IdCondicionLiquidacion\":\"T0\",\"FechaAnticipo\":\"2018-04-10\"}]}";
			AnticiparOrden_IN objIn = gson.fromJson(json,AnticiparOrden_IN.class);
			//mientras pruebo hare el rollback
			AnticiparOrden demo = new AnticiparOrden();
			
			AnticiparOrden_OUT ObjOut=demo.logica(conn, objIn);
				
			System.out.println(ObjOut.toString());
			//if (MensajeSalida.getNumeroErrores(ObjOut.P_OUT_MENSAJES) > 0)
				conn.rollback();
			//else conn.commit();
			
		}catch (Exception e){
			e.printStackTrace();
		}finally {
			conn.close();
		}
			System.out.println("== END ==");

	};
}
