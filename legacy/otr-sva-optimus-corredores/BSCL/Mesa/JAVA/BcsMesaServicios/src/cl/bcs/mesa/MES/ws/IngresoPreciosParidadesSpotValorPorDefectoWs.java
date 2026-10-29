package cl.bcs.mesa.MES.ws;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class IngresoPreciosParidadesSpotValorPorDefectoWs  implements JavaCall {

	
	private final String SQL_UPDATE_PRECIOS_PARIDADES_SPOT 		= loadResourceToString("cl/bcs/mesa/MES/ws/sql/IngresoPreciosParidadesSpotPorDefectoWs_UpdatePreciosParidad.sql");
	private final String SQL_CREATE_TEMP_TABLE_PARIDADES_SPOT 	= loadResourceToString("cl/bcs/mesa/MES/ws/sql/IngresoPreciosParidadesSpotPorDefectoWs_CreateTempTableIngresoParidad.sql");
	private final String SQL_DROP_TEMP_TABLE_PARIDADES_SPOT 	= loadResourceToString("cl/bcs/mesa/MES/ws/sql/IngresoPreciosParidadesSpotPorDefectoWs_DropTempTableIngresoParidad.sql");
	private final String SQL_INSERT_TEMP_TABLE_PARIDADES_SPOT 	= loadResourceToString("cl/bcs/mesa/MES/ws/sql/IngresoPreciosParidadesSpotPorDefectoWs_InsertTempTableIngresoParidad.sql");
	

	static final Logger logger = LogManager.getLogger(IngresoPreciosParidadesSpotValorPorDefectoWs.class);
	
	
	public static class EntradaPreciosParidadesSpotValorPorDefectoWs extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public List<PreciosParidadesSpotValorPorDefecto> p_DatosParidadYTipoCambioValorPorDefecto;
	}
	
	
	
	
	public static class PreciosParidadesSpotValorPorDefecto extends BcsPojo{
	
		
		
		public Long IdParidadYTipoCambioValorPorDefecto;
		public String IdMoneda;
		public String NombreMoneda;
		public BigDecimal ParidadMercado;
		public BigDecimal ParidadBID;
		public BigDecimal ParidadASK;
		public BigDecimal LimiteSuperiorParidad;
		public BigDecimal LimiteInferiorParidad;
		public BigDecimal ParidadSBIF;
		public BigDecimal TipoCambioMercado;
		public BigDecimal LimiteSuperiorTipoCambio;
		public BigDecimal LimiteInferiorTipoCambio;
		public BigDecimal PrecioSpotCompra;
		public BigDecimal PrecioSpotVenta;
		public BigDecimal PrecioSpotPromedio;
		
		
		public PreciosParidadesSpotValorPorDefecto() {
			this.IdParidadYTipoCambioValorPorDefecto = (long) 0; // No es necesario enviar
		}
	}
	
	
	public static class AdvertenciasDetalle extends BcsPojo {
		
		public Long IdInterno;
		public String Codigo;
		public String RequiereAutorizacion;
		public String Descripcion;

	}
	
	
	public static class ErrorDetalle extends BcsPojo {
		
		public Long IdInterno;
		public String Codigo;
		public String Descripcion;

	}
	
	
	public static class ResultadoProceso_Errores extends BcsPojo {
		
		public Long IdInterno;
		public String Codigo;
		public String Descripcion;
	}
	
	public static class ResultadoProceso_Salida extends BcsPojo {
		        
		public String   IdMoneda;           
		public String	Resultado;
		public List<ResultadoProceso_Errores> Errores;
	
		
	}
	
	
	public static class SalidaPreciosParidadesSpotWs extends BcsPojo {
		public List<MensajeSalida>				P_OUT_MENSAJES;
		public Long								P_OUT_ERRORESCONTADOR;
		public Long								P_OUT_ADVERTENCIASCONTADOR;
//		public List<ResultadoProceso_Salida>	P_OUT_RESULTADOPROCESO;
		public List<AdvertenciasDetalle> 		P_OUT_ADVERTENCIASDETALLE;
		
		public SalidaPreciosParidadesSpotWs() {
			
			P_OUT_ERRORESCONTADOR		= 0L;
			P_OUT_ADVERTENCIASCONTADOR	= 0L;
			P_OUT_MENSAJES 				= new ArrayList<MensajeSalida>();
//			P_OUT_RESULTADOPROCESO 		= new ArrayList<ResultadoProceso_Salida>();
			P_OUT_ADVERTENCIASDETALLE 	= new ArrayList<AdvertenciasDetalle>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaPreciosParidadesSpotValorPorDefectoWs jin = gson.fromJson(in.toString(), EntradaPreciosParidadesSpotValorPorDefectoWs.class);
		SalidaPreciosParidadesSpotWs out = logica(conn,jin);

		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}



	private SalidaPreciosParidadesSpotWs logica(Connection conn, EntradaPreciosParidadesSpotValorPorDefectoWs entrada) throws Exception {
		// TODO Auto-generated method stub
		// acá debemos agregar toda la logica de negocio, validaciones etc.
		
		
		SalidaPreciosParidadesSpotWs out = new SalidaPreciosParidadesSpotWs();
		try {
			
			logger.info("INICIO::IngresoPreciosParidadesSpotWs.logica");
			Long init 									= System.currentTimeMillis();
			SQL sql 									= new SQL();
			String fechaSistema 						= ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
			
			
			if(entrada.p_DatosParidadYTipoCambioValorPorDefecto.size() == 0) {
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_INGRESARON_PRECIOS_PARIDADES_SPOT",null,conn,entrada.p_IdEmpresa));
				out.P_OUT_ERRORESCONTADOR++;
				return out;
			}
			
			
			
			// Validar que la moneda no sea vacio
			
			for (PreciosParidadesSpotValorPorDefecto en : entrada.p_DatosParidadYTipoCambioValorPorDefecto) {
				
				if(en.IdMoneda.isEmpty()) {
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"MONEDA_NO_PUEDE_SER_VACIO",null,conn,entrada.p_IdEmpresa));
					out.P_OUT_ERRORESCONTADOR++;
					return out;
				}
				
			}
			
			
			// Validar que la moneda dentro de la clase siempre venga , con un if se puede hacer
			
			HashMap<String, Object> SalidaPrecioparidadesSpot = this.actualizarPreciosParidadesSpot(conn, sql, entrada, fechaSistema);
			
			
			if ( (Long)SalidaPrecioparidadesSpot.get("p_out_ErroresContador") > 0L ) {
				
				logger.debug("ACTUALIZACION DE FORMAS DE PAGO CON ERRORES");
				out.P_OUT_ERRORESCONTADOR = (Long)SalidaPrecioparidadesSpot.get("p_out_ErroresContador");
				
				List<ErrorDetalle> errores = (List<ErrorDetalle>) SalidaPrecioparidadesSpot.get("p_out_ErroresDetalle");
				
				for( ErrorDetalle err: errores ) {
					
					out.P_OUT_MENSAJES.add(new MensajeSalida(err.IdInterno ,MensajeSalida.MENSAJE_ERROR,err.Codigo,err.Descripcion));
				}
				
				return out;
				
			} else {
				
				out.P_OUT_ADVERTENCIASDETALLE.addAll((Collection<? extends AdvertenciasDetalle>) SalidaPrecioparidadesSpot.get("p_out_AdvertenciasDetalle"));
				out.P_OUT_ADVERTENCIASCONTADOR = (Long)SalidaPrecioparidadesSpot.get("p_out_AdvertenciasContador");
				
				// Agregamos mensaje de que se han insertado correctamente, al parecer el servicio no entrega nada
				
				for (PreciosParidadesSpotValorPorDefecto en : entrada.p_DatosParidadYTipoCambioValorPorDefecto) {
					
					MensajeSalida mensajeCustom = new MensajeSalida();
					mensajeCustom.codigo = en.IdMoneda;
					mensajeCustom.descripcion = "REGISTROS INSERTADOS CON EXITO";
					mensajeCustom.tipo = MensajeSalida.MENSAJE_OK;
					mensajeCustom.id = en.IdParidadYTipoCambioValorPorDefecto;
					
					out.P_OUT_MENSAJES.add(mensajeCustom);
				}
				
			}
			
			logger.info("FIN::IngresoPreciosParidadesSpotWs.logica");
			return out;
			
		} catch (Exception e) {
			// TODO: handle exception
		  	throw e;
		}
		
	}
	
	
	private HashMap<String, Object> actualizarPreciosParidadesSpot(Connection con, SQL sql, EntradaPreciosParidadesSpotValorPorDefectoWs in, String fechaSistema ) throws SQLException {
		logger.info("INICIO::IngresoPreciosParidadesSpotWs.actualizarPreciosParidadesSpot");
		
		HashMap<String, Object> parametrosEntrada 	= new HashMap<String, Object>();
		
		parametrosEntrada.put("p_IdEmpresa", in.p_IdEmpresa);
		parametrosEntrada.put("p_IdUsuario", in.p_IdUsuario);
		parametrosEntrada.put("p_IdModulo", in.p_IdModulo);
		parametrosEntrada.put("p_Fecha", fechaSistema);
		parametrosEntrada.put("p_OrigenJob", 'S');
		parametrosEntrada.put("p_DatosParidadYTipoCambioValorPorDefecto", in.p_DatosParidadYTipoCambioValorPorDefecto);
		
		
		HashMap<String, Class> parametrosSalida = new HashMap<String, Class>();
		parametrosSalida.put("p_out_AdvertenciasContador", Long.class);
	    parametrosSalida.put("p_out_AdvertenciasDetalle", AdvertenciasDetalle.class); 
	    parametrosSalida.put("p_out_ErroresContador", Long.class);
	    parametrosSalida.put("p_out_ErroresDetalle", ErrorDetalle.class);
	    
	    HashMap<String, Object> respuestaIngresoPreciosParidadesSpot;
		try {
			
			SQL.exec( con, SQL_CREATE_TEMP_TABLE_PARIDADES_SPOT );
	    	sql.batch(con, SQL_INSERT_TEMP_TABLE_PARIDADES_SPOT, in.p_DatosParidadYTipoCambioValorPorDefecto, PreciosParidadesSpotValorPorDefecto.class);
	    	
	    	respuestaIngresoPreciosParidadesSpot = sql.call(con, SQL_UPDATE_PRECIOS_PARIDADES_SPOT, parametrosEntrada, parametrosSalida);
	    	
		} catch(Exception e) {
			
			SQL.exec(con, SQL_DROP_TEMP_TABLE_PARIDADES_SPOT);
	    	throw e;
	    	
	    	
	    } finally {
	    	
	    	SQL.exec(con, SQL_DROP_TEMP_TABLE_PARIDADES_SPOT);

	    }

		
		logger.info("FIN::IngresoPreciosParidadesSpotWs.actualizarPreciosParidadesSpot");
		return respuestaIngresoPreciosParidadesSpot;
	}

	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String jsonOptimusDesa = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"MSP\",\"p_OrigenJob\":\"S\",\"p_DatosParidadYTipoCambioValorPorDefecto\":[{\"IdParidadYTipoCambioValorPorDefecto\":0,\"IdMoneda\":\"USD\",\"ParidadMercado\": 0.0001345,\"TipoCambioMercado\":1000,\"LimiteSuperiorTipoCambio\":500,\"LimiteInferiorTipoCambio\":500,\"PorcentajeSuperiorTipoCambio\":0,\"PorcentajeInferiorTipoCambio\":0,\"PrecioSpotCompra\":0,\"PrecioSpotVenta\":0,\"PrecioSpotPromedio\":0},{\"IdParidadYTipoCambio\":0,\"IdMoneda\":\"EUR\",\"TipoCambioMercado\":0,\"LimiteSuperiorTipoCambio\":500,\"LimiteInferiorTipoCambio\":500,\"PorcentajeSuperiorTipoCambio\":0,\"PorcentajeInferiorTipoCambio\":0,\"PrecioSpotCompra\":0,\"PrecioSpotVenta\":0,\"PrecioSpotPromedio\":0}]}";
			EntradaPreciosParidadesSpotValorPorDefectoWs Objin = gson.fromJson(jsonOptimusDesa, EntradaPreciosParidadesSpotValorPorDefectoWs.class);
			IngresoPreciosParidadesSpotValorPorDefectoWs demo = new IngresoPreciosParidadesSpotValorPorDefectoWs();
			
			
			
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			try {
 				SalidaPreciosParidadesSpotWs ObjOut = demo.logica(con, Objin);
 				con.commit();
 				System.out.println(ObjOut);
 				//con.rollback();
 				
 			}
 			catch(Exception er) {
				//er.printStackTrace();
 				con.rollback(); 				
				System.out.println(er.getMessage());
			}
 			//con.commit();
			finally {
				con.close();
			}
			
 			System.out.println("== END ==");
		}
		
	}


}

