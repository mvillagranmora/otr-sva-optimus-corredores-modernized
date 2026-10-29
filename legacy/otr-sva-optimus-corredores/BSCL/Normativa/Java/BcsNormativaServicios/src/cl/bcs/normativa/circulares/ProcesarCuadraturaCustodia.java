package cl.bcs.normativa.circulares;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class ProcesarCuadraturaCustodia implements JavaCall{	
	
	static final Logger logger = LogManager.getLogger(ProcesarCuadraturaCustodia.class);
	
	private final String SQL_SELECT_CUSTODIA = loadResourceToString("cl/bcs/normativa/circulares/sql/ProcesarCuadraturaCustodia_SelectCustodia.sql");
	private final String SQL_INSERT_CUSTODIA = loadResourceToString("cl/bcs/normativa/circulares/sql/ProcesarCuadraturaCustodia_InsertCuadraturaCustodia.sql");
	private final String SQL_DELETE_CUSTODIA = loadResourceToString("cl/bcs/normativa/circulares/sql/ProcesarCuadraturaCustodia_DeleteCuadraturaCustodia.sql");
	
	private final String SQL_SELECT_CUSTODIA_CIRCULAR_1720 = loadResourceToString("cl/bcs/normativa/circulares/sql/ProcesarCuadraturaCustodia_SelectCustodiaCir1720.sql");
	private final String SQL_INSERT_CUSTODIA_CIRCULAR_1720 = loadResourceToString("cl/bcs/normativa/circulares/sql/ProcesarCuadraturaCustodia_InsertCuadraturaCustodiaCir1720.sql");
	private final String SQL_DELETE_CUSTODIA_CIRCULAR_1720 = loadResourceToString("cl/bcs/normativa/circulares/sql/ProcesarCuadraturaCustodia_DeleteCuadraturaCustodiaCir1720.sql");

	private final Log log = new Log("NOR.ProcesarCuadraturaCustodia");
	
	public static class InformacionEmpresa {
		public Long IdEmpresa;
		public String DocumentoIdentidad;
		public String RazonSocial;
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}		
	}

	public static class FechaProceso{
		public String 	v_FechaOperacion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Custodia {
		public Long 		IdEmpresa;
		public java.sql.Date		FechaCuadratura; 
		public Long			IdInstrumento; 
		public String		IdTipoInstrumento; 
		public String		IdClasificacionTipoInstrumento; 
		public String		Nemotecnico; 
		public BigDecimal   CantidadCustodia;
		public BigDecimal   CantidadSimultaneaCompra;
		public BigDecimal   CantidadSimultaneaVenta;
		public BigDecimal   CantidadPactoCompra;
		public BigDecimal   CantidadPactoVenta;
		public BigDecimal   CantidadPrestamo;
		public BigDecimal   CantidadEmprestito;
		public BigDecimal   CantidadGarantia;
		public BigDecimal   CantidadTransitoIngreso;
		public BigDecimal   CantidadTransitoEgreso;
		public BigDecimal   CantidadTotal;
		public BigDecimal   MontoATasaMercadoActual;
		public BigDecimal   Diferencia;
		public String  	 	ExplicacionDescuadratura;
		public BigDecimal   CantidadRelacionado;
		public BigDecimal   CantidadTerceros;
		public BigDecimal   CantidadCuentaPropia;
		public BigDecimal   CantidadIntermediario;
		public BigDecimal   CantidadBCS;
		public BigDecimal   CantidadBEC;
		public BigDecimal   CantidadBOVALPO;
		public BigDecimal   CantidadDCV;
		public BigDecimal   CantidadEmisores;
		public BigDecimal   CantidadOtros;
		public BigDecimal   CantidadGarantiaBCS;
		public BigDecimal   CantidadGarantiaBEC;
		public BigDecimal   CantidadGarantiaBOVALPO;
		public BigDecimal   CantidadGarantiaDCV;
		public BigDecimal   CantidadGarantiaEmisores;
		public BigDecimal   CantidadGarantiaOtros;
		public BigDecimal   CantidadDCVRelacionado;
		public BigDecimal   CantidadDCVTerceros;
		public BigDecimal   CantidadDCVCuentaPropia;
		public BigDecimal 	CantidadSorteoLetras;
		public BigDecimal	CompraEnTransito;
		public BigDecimal	VentaEnTransito;
		
		public Custodia(){
			this.CantidadSorteoLetras = BigDecimal.ZERO;
			this.CompraEnTransito = BigDecimal.ZERO;
			this.VentaEnTransito =  BigDecimal.ZERO;
		}
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}		
	}

	public static class TablaParametros_Entrada {
		public String  TipoParametro;
		public String  Valor;
	
		public String toString() {
			return Utils.printJson(this, false);
		};
	};
	
	public static class ObjResultadoProceso {
		public String   NombreParametro; 
		public String  	Valor;		
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	public static class ProcesarCuadraturaCustodia_Entrada{
		public Long                          p_IdEmpresa;
		public String                        p_FechaOperacion;
				
		public String toString() {
			return Utils.printJson(this, false);
		};
	};
	
	public static class ProcesarCuadraturaCustodia_Salida{
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		
		public String toString() {
			return Utils.printJson(this, false);
		};
		public ProcesarCuadraturaCustodia_Salida (){
			P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
	};

	public List<MensajeSalida>	P_OUT_MENSAJES;
	public Long					P_OUT_ERRORESCONTADOR;
	public Long					P_OUT_TICKETATENCION;
	
	private List<Custodia> seleccionarCustodiaCircular(Connection con,String querySql,HashMap<String,Object> params) throws Exception{
		logger.debug("INICIO::ProcesarCuadraturaCustodia.seleccionarCustodiaCircular");
		long inicio = System.currentTimeMillis();
	
		SQL sql = new SQL();
		List<Custodia> listCustodia = new ArrayList<Custodia>();
		listCustodia = sql.query(con, querySql, params, Custodia.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ProcesarCuadraturaCustodia.seleccionarCustodiaCircular", fin - inicio);
		
		return 	listCustodia;
	};
	
	private void eliminarCustodiaCircular(Connection con,String querySql, List<FechaProceso> params) throws Exception {
		logger.debug("INICIO::ProcesarCuadraturaCustodia.eliminarCustodiaCircular");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		sql.batch(con, querySql, params, FechaProceso.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ProcesarCuadraturaCustodia.eliminarCustodiaCircular", fin - inicio);
	};
	
	private void actualizarCustodiaCircular(Connection con,String querySql,List<Custodia> params) throws Exception {
		logger.debug("INICIO::ProcesarCuadraturaCustodia.actualizarCustodiaCircular");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		sql.batch(con, querySql, params, Custodia.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ProcesarCuadraturaCustodia.actualizarCustodiaCircular", fin - inicio);
	};
	
	
	@Trace
	public ProcesarCuadraturaCustodia_Salida logica(Connection con, ProcesarCuadraturaCustodia_Entrada in) throws Exception {
		log.write("Inicio ProcesarCuadraturaCustodia");
		log.write(in.toString());
				
		ProcesarCuadraturaCustodia_Salida out = new ProcesarCuadraturaCustodia_Salida();
		out.P_OUT_ERRORESCONTADOR 				= 0L;
		
		try {		

			//Busqueda de datos
			HashMap<String,Object> params = new HashMap<>();
			params.put("v_FechaOperacion", in.p_FechaOperacion);
			params.put("v_IdEmpresa", in.p_IdEmpresa);
			List<Custodia> listCustodia = seleccionarCustodiaCircular(con, SQL_SELECT_CUSTODIA, params);
			List<Custodia> listCustodiaCir1720 = seleccionarCustodiaCircular(con, SQL_SELECT_CUSTODIA_CIRCULAR_1720, params);
			
			//Insert
			if(!listCustodia.isEmpty() && !listCustodiaCir1720.isEmpty()) {

				//Eliminacion de registros anteriores
				List<FechaProceso> lstDeleteCustodia = new ArrayList<FechaProceso>();
				List<FechaProceso> lstDeleteCustodiaCir1720 = new ArrayList<FechaProceso>();
				FechaProceso objDeleteCustodia= new FechaProceso();
				objDeleteCustodia.v_FechaOperacion = in.p_FechaOperacion;
				lstDeleteCustodia.add(objDeleteCustodia);
				lstDeleteCustodiaCir1720.add(objDeleteCustodia);
				
				
				eliminarCustodiaCircular(con, SQL_DELETE_CUSTODIA, lstDeleteCustodia);
				eliminarCustodiaCircular(con, SQL_DELETE_CUSTODIA_CIRCULAR_1720, lstDeleteCustodiaCir1720);
				
				actualizarCustodiaCircular(con, SQL_INSERT_CUSTODIA, listCustodia);
				actualizarCustodiaCircular(con, SQL_INSERT_CUSTODIA_CIRCULAR_1720, listCustodiaCir1720);
			}else {
				out.P_OUT_ERRORESCONTADOR = 1L;
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_INFORMACION, "UI_NO_EXISTEN_REGISTROS",new HashMap<>(), con, in.p_IdEmpresa));
			}

			log.write("Inicio OK");

		} catch (Exception er) {
			out.P_OUT_ERRORESCONTADOR = 1L;
			log.write(er);
			er.printStackTrace();
		}
		return out;
	};
			
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		ProcesarCuadraturaCustodia_Entrada jin=gson.fromJson(in.toString() ,ProcesarCuadraturaCustodia_Entrada.class);
		ProcesarCuadraturaCustodia_Salida out=logica(conn,jin);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	public static void main(String[] args) throws Exception {		
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			try {
				Gson gson = Utils.getGsonBuilder();					
			    String json ="{\"p_IdEmpresa\":1,\"p_FechaOperacion\":\"2022-06-03\"}";
								
				ProcesarCuadraturaCustodia_Entrada Objin = gson.fromJson(json, ProcesarCuadraturaCustodia_Entrada.class);			
				ProcesarCuadraturaCustodia procesaCustodia = new ProcesarCuadraturaCustodia();
				ProcesarCuadraturaCustodia_Salida Objout = procesaCustodia.logica(con, Objin);				
				System.out.println(Objout.toString());
			//	con.commit();
				con.rollback();
				
		    	}catch(Exception er) {
					er.printStackTrace();	
					con.rollback();
				}
		} catch (Exception e) {
			e.printStackTrace();			
		}	
		System.out.println("== END ==");
	};
};
