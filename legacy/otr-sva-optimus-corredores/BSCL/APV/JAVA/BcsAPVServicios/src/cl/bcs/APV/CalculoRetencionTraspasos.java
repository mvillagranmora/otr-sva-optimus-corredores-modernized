package cl.bcs.APV;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

//Recaudacion
import cl.bcs.APV.CambiarEstadoRecaudacion;
import cl.bcs.APV.CrearTraspasoTesoreria.OrdenTraspaso;
import cl.bcs.APV.CrearTraspasoTesoreria.Retencion;
import cl.bcs.APV.CrearTraspasoTesoreria.TraspasoTesoreria;
import cl.bcs.APV.GrabarRecaudacionTraspaso.CambiarEstadoTraspaso;
import cl.bcs.tesoreria.CrearComprobante;
//Tesoreria
import cl.bcs.tesoreria.CrearIngresoEgreso;
import cl.bcs.tesoreria.LiquidarComprobante;
import cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Entrada;
import cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Salida;
import cl.bcs.tesoreria.CrearIngresoEgreso.IngresoEgreso_Entrada;
import cl.bcs.cuentaInversion.CrearCargoAbono;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class CalculoRetencionTraspasos implements JavaCall   {
 
	/*
	 * El log
	 */	
	private final Log log = new Log("CrearTraspasoTesoreria");

	/*
	 * Queries que van a ser utilizadas por el servicio. 
	 */	
	public String SQL_SELECT_CONFIGURACION_RETENCION = loadResourceToString("cl/bcs/APV/sql/ConsultarRetencion.sql");
	private final String SQL_CONSULTA_ORDENES_RV_FFMM = loadResourceToString("cl/bcs/APV/sql/ConsultaOrdenesTraspaso.sql");

	
	/********************
	 * Clases IN/OUT	*
	 ********************/

	//  SERVICIO ENTRADA
	public static class ParametrosEntradaServicio {
		
		public Long				p_IdEmpresa;
		public Long				p_IdUsuario;
		public String			p_IdModulo;
		public List<Traspaso>	p_Parametros;

		public ParametrosEntradaServicio(){
			p_Parametros = new ArrayList<Traspaso>();	
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}			

	// SERVICIO SALIDA
	public static class ParametrosSalidaServicio {		
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		public BigDecimal			MontoTotal;
		public BigDecimal			MontoConDescuento;
		public BigDecimal			MontoRetencion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public ParametrosSalidaServicio(){
			P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}		
	};

	public static class Traspaso{
		public Long IdTraspaso;	
	}
	
	public static class Montos{
		public Long MontoTotal;
		public Long MontoConDescuento;
		public Long Retencion;
	}
	
	public static class Retencion{
		public String CodigoRetiro;
		public Long IdPersonaRetencion;
		public Long IdRegimenTributario;
		public BigDecimal PorcentajeRetencion;
		public BigDecimal MontoTopeRetencion;
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
	 * Métodos Básicos	*	
	 ************************/
	
	public ParametrosSalidaServicio logica(Connection con,ParametrosEntradaServicio in)throws Exception {
		MensajeSalida msg = new MensajeSalida();
		SQL sql = new SQL();
		ParametrosSalidaServicio 	salida 			= new ParametrosSalidaServicio ();
		
		salida.P_OUT_ERRORESCONTADOR = 0L;
		salida.MontoConDescuento = BigDecimal.ZERO;
		salida.MontoRetencion = BigDecimal.ZERO;
		salida.MontoTotal = BigDecimal.ZERO;
		
		try { 
			
			for(Traspaso fila : in.p_Parametros){
				
				/* Inicio - Obtengo ordenes ffmm certificadas o ordenes rv facturadas para la creacion de comprobantes*/
				List<OrdenTraspaso> consultaOrdenTraspaso = new ArrayList<OrdenTraspaso>();
				HashMap<String,Object> ParamsSelectOrdenTraspaso = new HashMap<String,Object>();
				ParamsSelectOrdenTraspaso.put("IdTraspaso", fila.IdTraspaso);
				consultaOrdenTraspaso = sql.query(con, SQL_CONSULTA_ORDENES_RV_FFMM, ParamsSelectOrdenTraspaso, OrdenTraspaso.class);
				/* Fin - Obtengo ordenes ffmm certificadas o ordenes rv facturadas para la creacion de comprobantes*/
				
				for(OrdenTraspaso otRow : consultaOrdenTraspaso) {
			
					HashMap<String,Object> ParamsSelectConfRetencion = new HashMap<String,Object>();
					List<Retencion> _ConfiguracionRetencion = sql.query(con, SQL_SELECT_CONFIGURACION_RETENCION, ParamsSelectConfRetencion, Retencion.class);
			
					if(otRow.MontoOrden == null && otRow.MontoOrdenFFMM == null) continue;
					if(fila.IdTraspaso.compareTo(otRow.IdTraspaso) != 0) continue;
					
					BigDecimal MontoOrden = otRow.MontoOrden == null || otRow.MontoOrden.compareTo(BigDecimal.ZERO)==0 ? otRow.MontoOrdenFFMM : otRow.MontoOrden;
					BigDecimal MontoRetencion = BigDecimal.ZERO;
					Long IdPersonaRetencion = 0L;
		
					
					/*CONSULTA RETENCION
					*    Se hace despues de los cargos y abonos ya que hasta ese punto
					*    no se aplica el descuento
					**/
					for(Retencion retRow : _ConfiguracionRetencion){
						if (otRow.RazonRetiro.equals(retRow.CodigoRetiro)
						&& retRow.PorcentajeRetencion.compareTo(BigDecimal.ZERO)!=0
						){ 
							if(retRow.IdRegimenTributario == null || 
							   retRow.IdRegimenTributario.compareTo(otRow.IdMatrizAgrupadorAPV) == 0 ){
			
								MontoRetencion = MontoOrden.multiply(retRow.PorcentajeRetencion);
								//MontoRetencion = MontoOrden.multiply(BigDecimal.valueOf(0.15));
								MontoOrden = MontoOrden.subtract(MontoRetencion);
								IdPersonaRetencion = retRow.IdPersonaRetencion;
							}
						}
					}		
					
					salida.MontoConDescuento = salida.MontoConDescuento.add(MontoOrden);
					salida.MontoRetencion = salida.MontoRetencion.add(MontoRetencion);				
				}			
			}
		} catch(Exception er) {
			er.printStackTrace();	
			con.rollback();
			
			msg.id = 1L;
			msg.codigo = "ERROR";
			msg.descripcion = "Error : " + er;
			salida.P_OUT_MENSAJES.add(msg);
			salida.P_OUT_ERRORESCONTADOR = 1L;
			
			return salida;
		}
		salida.MontoTotal = salida.MontoConDescuento.add(salida.MontoRetencion);
		
		return salida;
	}
	
	/********************
	 * Método Principal *
	 ********************/

	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			try {
				CalculoRetencionTraspasos demo = new CalculoRetencionTraspasos();	

				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();				
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":6,\"p_IdModulo\":\"APV\",\"p_Parametros\":[{\"IdTraspaso\":151},{\"IdTraspaso\":149}]}";
				//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":23,\"p_IdModulo\":\"APV\",\"p_Parametros\":[{\"IdTraspaso\":122,\"IdPersona\":1765331,\"IdPersonaCliente\":1774553,\"IdPersonaEntidad\":1765331,\"IdPortafolio\":0,\"Fecha\":\"2018-06-05\",\"IdBanco\":8,\"IdCuentaCorriente\":\"111122223\",\"IdMedioPago\":\"TRA\",\"MontoTotal\":337000000}]}";
				
				ParametrosEntradaServicio Objin = gson.fromJson(json, ParametrosEntradaServicio.class);
				
	 			try { 
	 				ParametrosSalidaServicio ObjOut = demo.logica(con, Objin);
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