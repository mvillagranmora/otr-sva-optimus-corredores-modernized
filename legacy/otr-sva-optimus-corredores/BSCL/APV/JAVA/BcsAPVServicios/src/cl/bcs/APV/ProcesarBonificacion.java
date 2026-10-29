package cl.bcs.APV;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class ProcesarBonificacion implements JavaCall   {

	/*
	 * El log
	 */	
	private final Log log = new Log("EjecutarBonificacion");

	/*
	 * Queries que van a ser utilizadas por el servicio. 
	 */	
	private String SQL_SELECT_ConsultarBonificacionAProcesar = loadResourceToString("cl/bcs/APV/sql/ConsultarBonificacionAProcesar.sql");
	private String SQL_UPDATE_ProcesarBonificacion = loadResourceToString("cl/bcs/APV/sql/ProcesarBonificacion.sql");
	private String SQL_SELECT_ConsultarCarterasRetiradas = loadResourceToString("cl/bcs/APV/sql/ConsultarCarterasRetiradas.sql");
	private String SQL_SELECT_ConsultarCarterasTraspasos = loadResourceToString("cl/bcs/APV/sql/ConsultarCarterasTraspaso.sql");
	private String SQL_SELECT_ConsultarCarterasTraspasosAnioAhorro = loadResourceToString("cl/bcs/APV/sql/ConsultarCarterasTraspasoAhorro.sql");
	/********************
	 * Clases IN/OUT	*
	 ********************/

	public static class ParametrosBonificacion{

		public Long IdBonificacion;
	}
	
	//  SERVICIO ENTRADA
	public static class ParametrosEntradaServicio {
		
		public 	Long									p_IdEmpresa;
		public	Long									p_IdUsuario;
		public 	String									p_IdModulo;
		public	Long									p_AnoAhorro;
		//public  List<ParametrosBonificacion>			p_Parametros;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}			

	// SERVICIO SALIDA
	public static class ParametrosSalidaServicio {		
		public List<MensajeSalida>									P_OUT_MENSAJES;
		public Long													P_OUT_ERRORESCONTADOR;

		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public ParametrosSalidaServicio(){
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}		
	};

	public static class BonificacionAProcesar{

		public Long 		IdCliente;
		public Long	 		AnoAhorro;
		public BigDecimal	AhorrosDJ;
		public BigDecimal	RetirosDJ;
		public Long 		IdBonificacion;
		public BigDecimal	MontoBonificacionAfiliadoNeto;
		public BigDecimal	MontoReajustadoPagadoAfiliado;
		public String 		IdEstado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class BonificacionUpdate{

		public BigDecimal	MontoCalculado;
		public BigDecimal	MontoTraspasoEgreso;
		public BigDecimal	MontoEgreso;
		public BigDecimal	PorcentajeTraspasoEgreso;
		public BigDecimal	PorcentajeEgreso;
		public BigDecimal 	MontoTraspasoDevolucion;
		public BigDecimal	MontoEgresoDevolucion;
		public Long 		IdBonificacion;
		public Long			IdEmpresa;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Cartera{
		public BigDecimal	Cantidad;
		public BigDecimal	PrecioCompra;
		public Long 		IdCliente;
		
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
	 * Métodos Básicos	*	
	 ************************/
	
	public ParametrosSalidaServicio logica(Connection con,ParametrosEntradaServicio in)throws Exception {
		log.write("INICIO::ProcesarBonificacion");
		ParametrosSalidaServicio out = new ParametrosSalidaServicio();

		//===========================================================//
		// DECLARACION VARIABLES                                     //
		//===========================================================//
		SQL sql = new SQL();
		
		//===========================================================//
		// OBTENCION DE VARIABLES INICIALES                          //
		//===========================================================//
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdEmpresa", in.p_IdEmpresa);
		params.put("AnoAhorro", in.p_AnoAhorro);
		List<BonificacionAProcesar> ListaBonificacion = sql.query(con, SQL_SELECT_ConsultarBonificacionAProcesar, params, BonificacionAProcesar.class);
		
		List<Long> idClientes = new ArrayList<Long>();
		for(BonificacionAProcesar fila :ListaBonificacion){
			if(!idClientes.contains(fila.IdCliente)) idClientes.add(fila.IdCliente);
		}
		
		params.clear();
		params.put("IdCliente", idClientes);
		params.put("FechaDesdeAhorro", in.p_AnoAhorro + "-01-01");
		params.put("FechaHastaAhorro", in.p_AnoAhorro + "-12-31");
		List<Cartera> listaCarterasRetiradasTraspasoAnioAhorro = sql.query(con, SQL_SELECT_ConsultarCarterasTraspasosAnioAhorro, params, Cartera.class);
		params.put("FechaHasta", fechaSistema);
		List<Cartera> listaCarterasRetiradasAnioProceso = sql.query(con, SQL_SELECT_ConsultarCarterasRetiradas, params, Cartera.class);
		params.put("FechaDesde", (in.p_AnoAhorro + 1) + "-01-01");
		List<Cartera> listaCarterasRetiradasTraspasoAnioProceso = sql.query(con, SQL_SELECT_ConsultarCarterasTraspasos, params, Cartera.class);
		//===========================================================//
		// CICLO DE PROCESO                                          //
		//===========================================================//
		List<BonificacionUpdate> ListBon_update = new ArrayList<BonificacionUpdate>();
		for(BonificacionAProcesar fila :ListaBonificacion){

			BigDecimal montoRetirada = BigDecimal.ZERO;
			BigDecimal montoRetiradasTraspaso = BigDecimal.ZERO;
			BigDecimal montoAhorroNeto = fila.AhorrosDJ.subtract(fila.RetirosDJ, MathContext.DECIMAL128);
			
			for(Cartera cartera : listaCarterasRetiradasAnioProceso){
				if(fila.IdCliente.equals(cartera.IdCliente)) montoRetirada = montoRetirada.add(cartera.Cantidad.multiply(cartera.PrecioCompra, MathContext.DECIMAL128), MathContext.DECIMAL128);
			}
			
			for(Cartera cartera : listaCarterasRetiradasTraspasoAnioProceso){
				if(fila.IdCliente.equals(cartera.IdCliente)) montoRetiradasTraspaso = montoRetiradasTraspaso.add(cartera.Cantidad.multiply(cartera.PrecioCompra, MathContext.DECIMAL128), MathContext.DECIMAL128);
			}
			
			for(Cartera cartera : listaCarterasRetiradasTraspasoAnioAhorro){
				if(fila.IdCliente.equals(cartera.IdCliente)) montoRetiradasTraspaso = montoRetiradasTraspaso.add(cartera.Cantidad.multiply(cartera.PrecioCompra, MathContext.DECIMAL128), MathContext.DECIMAL128);
			}
			
			BigDecimal montoRestarTraspaso = montoRetiradasTraspaso.multiply(fila.MontoReajustadoPagadoAfiliado).divide(montoAhorroNeto, MathContext.DECIMAL128);
			BigDecimal montoRestarRetiro = montoRetirada.multiply(fila.MontoReajustadoPagadoAfiliado).divide(montoAhorroNeto, MathContext.DECIMAL128);
			BigDecimal montoARestar = montoRestarTraspaso.add(montoRestarRetiro, MathContext.DECIMAL128);
			
			
			
			BonificacionUpdate Bon_update = new BonificacionUpdate();
			Bon_update.IdBonificacion   	= fila.IdBonificacion;
			Bon_update.PorcentajeEgreso		= montoRetirada.multiply(new BigDecimal("100"), MathContext.DECIMAL128).divide(montoAhorroNeto, MathContext.DECIMAL128);
			Bon_update.PorcentajeTraspasoEgreso = montoRetiradasTraspaso.multiply(new BigDecimal("100"), MathContext.DECIMAL128).divide(montoAhorroNeto, MathContext.DECIMAL128);
			Bon_update.MontoEgreso 			= montoRetirada;
			Bon_update.MontoTraspasoEgreso  = montoRetiradasTraspaso;
			Bon_update.MontoTraspasoDevolucion = montoRestarTraspaso;
			Bon_update.MontoEgresoDevolucion = montoRestarRetiro;
			Bon_update.IdEmpresa			= in.p_IdEmpresa;
			Bon_update.MontoCalculado		= fila.MontoReajustadoPagadoAfiliado.subtract(montoARestar, MathContext.DECIMAL128); 
			ListBon_update.add(Bon_update);
		}
		
		int[] resUpdate = sql.batch(con,SQL_UPDATE_ProcesarBonificacion,ListBon_update,BonificacionUpdate.class);

		if(resUpdate!=null && resUpdate.length>0 && resUpdate[0]==1){
			MensajeSalida msg = new MensajeSalida();
			msg.id = 1L;//fila.IdRecaudacion;
			msg.tipo = "OK";
			msg.codigo = "UI_PROCESO_EXITOSO";
			msg.descripcion = "UI_PROCESO_EXITOSO";
			out.P_OUT_MENSAJES.add(msg);
		}else {
			MensajeSalida msg = new MensajeSalida();
			msg.id = 1L;//fila.IdRecaudacion;
			msg.tipo = "ERROR";
			msg.codigo = "NO_SE_ENCONTRARON_REGISTROS_PARA_PROCESAR";
			msg.descripcion = "No se encontraron registros que cumplan con las condiciones para procesar";
			out.P_OUT_MENSAJES.add(msg);	
		}

		log.write("FIN::ProcesarBonificacion");
		return out;
	}
	/********************
	 * Método Principal *
	 ********************/

	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getPropertiesConnection("master01", "connections.properties")) {
			try {
				ProcesarBonificacion demo = new ProcesarBonificacion();	

				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();		
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":82508,\"p_IdModulo\":\"APV\",\"p_AnoAhorro\":2024}";
				ParametrosEntradaServicio Objin = gson.fromJson(json, ParametrosEntradaServicio.class);
				
	 			try { 
	 				ParametrosSalidaServicio ObjOut = demo.logica(con, Objin);
	 				con.rollback();
	 				//con.commit();
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