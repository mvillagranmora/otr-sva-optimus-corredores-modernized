package cl.bcs.mesa.MES.ws;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
import com.ibm.icu.text.DecimalFormat;

import cl.bcs.mesa.MES.ws.ActualizarPosicionInicialDiaSiguiente;
import cl.bcs.mesa.MES.ws.ActualizarPosicionInicialDiaSiguiente.EntradaPosicionInicialDiaSiguiente;
import cl.bcs.mesa.MES.ws.ActualizarPosicionInicialDiaSiguiente.SalidaActualizarPosicionInicialDiaSiguiente;
import cl.bcs.mesa.utils.ConsultarConfiguracionMesa;
import cl.bcs.mesa.utils.ConsultarConfiguracionMesa.ConfiguracionMesa;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.tesoreria.LiquidarMovimientoAutomatico.IngresoEgreso_Entrada;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class IngresoPreciosParidadesSpotWs  implements JavaCall {


	private final String SQL_UPDATE_PRECIOS_PARIDADES_SPOT 		= loadResourceToString("cl/bcs/mesa/MES/ws/sql/IngresoPreciosParidadesSpotWs_UpdatePreciosParidad.sql");
	private final String SQL_CREATE_TEMP_TABLE_PARIDADES_SPOT 	= loadResourceToString("cl/bcs/mesa/MES/ws/sql/IngresoPreciosParidadesSpotWs_CreateTempTableIngresoParidad.sql");
	private final String SQL_DROP_TEMP_TABLE_PARIDADES_SPOT 	= loadResourceToString("cl/bcs/mesa/MES/ws/sql/IngresoPreciosParidadesSpotWs_DropTempTableIngresoParidad.sql");
	private final String SQL_INSERT_TEMP_TABLE_PARIDADES_SPOT 	= loadResourceToString("cl/bcs/mesa/MES/ws/sql/IngresoPreciosParidadesSpotWs_InsertTempTableIngresoParidad.sql");

	private final String SQL_CONSULTA_VALORES_REALES 			= loadResourceToString("cl/bcs/mesa/MES/ws/sql/IngresoPreciosParidadesSpotWs_ConsultaDatosValoresReales.sql");
	private final String SQL_CONSULTA_VALORES_POR_DEFECTO 		= loadResourceToString("cl/bcs/mesa/MES/ws/sql/IngresoPreciosParidadesSpotWs_ConsultaDatosValoresPorDefecto.sql");
	private final String SQL_VALIDA_MONEDA 						= loadResourceToString("cl/bcs/mesa/MES/ws/sql/IngresoPreciosParidadesSpotWs_ValidaMoneda.sql");


	static final Logger logger = LogManager.getLogger(IngresoPreciosParidadesSpotWs.class);


	public static class EntradaPreciosParidadesSpotWs extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public List<PreciosParidadesSpot> p_DatosParidadYTipoCambio;
	}




	public static class PreciosParidadesSpot extends BcsPojo{



		public Long IdParidadYTipoCambio;
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


		public PreciosParidadesSpot() {
			this.IdParidadYTipoCambio = (long) 0; // No es necesario enviar
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
		EntradaPreciosParidadesSpotWs jin = gson.fromJson(in.toString(), EntradaPreciosParidadesSpotWs.class);
		SalidaPreciosParidadesSpotWs out = logica(conn,jin);

		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}



	private SalidaPreciosParidadesSpotWs logica(Connection conn, EntradaPreciosParidadesSpotWs entrada) throws Exception {
		// TODO Auto-generated method stub
		// acá debemos agregar toda la logica de negocio, validaciones etc.


		SalidaPreciosParidadesSpotWs out = new SalidaPreciosParidadesSpotWs();
		try {

			logger.info("INICIO::IngresoPreciosParidadesSpotWs.logica");
			Long init 									= System.currentTimeMillis();
			SQL sql 									= new SQL();
			String fechaSistema 						= ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");


			if(entrada.p_DatosParidadYTipoCambio.size() == 0) {
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_INGRESARON_PRECIOS_PARIDADES_SPOT",null,conn,entrada.p_IdEmpresa));
				out.P_OUT_ERRORESCONTADOR++;
				return out;
			}





			for (PreciosParidadesSpot en : entrada.p_DatosParidadYTipoCambio) {
				// Validar que la moneda no sea vacio
				if(en.IdMoneda.isEmpty()) {
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"MONEDA_NO_PUEDE_SER_VACIO",null,conn,entrada.p_IdEmpresa));
					out.P_OUT_ERRORESCONTADOR++;
					return out;
				}

				//Validar que la moneda exista en el sistema
				String sql1 = SQL_VALIDA_MONEDA;
				try (PreparedStatement stmt = conn.prepareStatement(sql1)) {
				    stmt.setString(1, en.IdMoneda);
				    ResultSet rs = stmt.executeQuery();

				    if (!rs.next()) {
				        out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"MONEDA_NO_EXISTE_EN_SISTEMA",null,conn,entrada.p_IdEmpresa));
				        out.P_OUT_ERRORESCONTADOR++;
				        return out;
				    }
				}

				/**
				 * Debemos validar aqui cuál de todos los valores tomar, el que viene o el que está, para luego armar una entrada personalizada
				 */

				/**
				 * INICIO Consulta a la BD
				 */

				HashMap<String, Object> indicadores 	= new HashMap<String, Object>();
				indicadores.put("IdMoneda", en.IdMoneda);
				indicadores.put("FechaSistema", fechaSistema);

				List<PreciosParidadesSpot> paridadesReales = sql.query(conn, SQL_CONSULTA_VALORES_REALES, indicadores, PreciosParidadesSpot.class);
				List<PreciosParidadesSpot> paridadesPorDefecto = sql.query(conn, SQL_CONSULTA_VALORES_POR_DEFECTO, indicadores, PreciosParidadesSpot.class);

				/**
				 * FIN Consulta a la BD
				 */



				/**
				 * Validar si paridadesPorDefecto viene vacío
				 */
				if (paridadesPorDefecto.size() == 0) {
				    // Si está vacío, agregar 0 en todos los campos
				    PreciosParidadesSpot paridadCero = new PreciosParidadesSpot();
				    Field [] fields = PreciosParidadesSpot.class.getDeclaredFields();
				    for (Field field : fields) {
				        if (field.getType() == BigDecimal.class) {
				            field.set(paridadCero, BigDecimal.ZERO);
				        }
				    }
				    paridadesPorDefecto.add(paridadCero);
				}

				if(paridadesPorDefecto.size() != 0) {

					/**
					 * ParidadMercado
					 */


					if(paridadesPorDefecto.get(0).ParidadMercado.compareTo(BigDecimal.ZERO) != 0 && en.ParidadMercado == null) {

						en.ParidadMercado = paridadesPorDefecto.get(0).ParidadMercado;

					}else if(paridadesPorDefecto.get(0).ParidadMercado.compareTo(BigDecimal.ZERO) == 0 && en.ParidadMercado == null && paridadesReales.size() > 0) {

	                    en.ParidadMercado = paridadesReales.get(0).ParidadMercado != null ? paridadesReales.get(0).ParidadMercado : BigDecimal.ZERO;

					}


					/**
					 * ParidadBID
					 */


					if(paridadesPorDefecto.get(0).ParidadBID.compareTo(BigDecimal.ZERO) != 0 && en.ParidadBID == null) {

						en.ParidadBID = paridadesPorDefecto.get(0).ParidadBID;

					}else if(paridadesPorDefecto.get(0).ParidadBID.compareTo(BigDecimal.ZERO) == 0 && en.ParidadBID == null && paridadesReales.size() > 0) {

	                    en.ParidadBID = paridadesReales.get(0).ParidadBID != null ? paridadesReales.get(0).ParidadBID : BigDecimal.ZERO;

					}

					/**
					 * ParidadASK
					 */


					if(paridadesPorDefecto.get(0).ParidadASK.compareTo(BigDecimal.ZERO) != 0 && en.ParidadASK == null) {

						en.ParidadASK = paridadesPorDefecto.get(0).ParidadASK;

					}else if(paridadesPorDefecto.get(0).ParidadASK.compareTo(BigDecimal.ZERO) == 0 && en.ParidadASK == null && paridadesReales.size() > 0) {

	                    en.ParidadASK = paridadesReales.get(0).ParidadASK != null ? paridadesReales.get(0).ParidadASK : BigDecimal.ZERO;

					}

					/**
					 * LimiteSuperiorParidad
					 */


					if(paridadesPorDefecto.get(0).LimiteSuperiorParidad.compareTo(BigDecimal.ZERO) != 0 && en.LimiteSuperiorParidad == null) {

						en.LimiteSuperiorParidad = paridadesPorDefecto.get(0).LimiteSuperiorParidad;

					}else if(paridadesPorDefecto.get(0).LimiteSuperiorParidad.compareTo(BigDecimal.ZERO) == 0 && en.LimiteSuperiorParidad == null && paridadesReales.size() > 0) {

	                    en.LimiteSuperiorParidad = paridadesReales.get(0).LimiteSuperiorParidad != null ? paridadesReales.get(0).LimiteSuperiorParidad : BigDecimal.ZERO;

					}

					/**
					 * LimiteInferiorParidad
					 */


					if(paridadesPorDefecto.get(0).LimiteInferiorParidad.compareTo(BigDecimal.ZERO) != 0 && en.LimiteInferiorParidad == null) {

						en.LimiteInferiorParidad = paridadesPorDefecto.get(0).LimiteInferiorParidad;

					}else if(paridadesPorDefecto.get(0).LimiteInferiorParidad.compareTo(BigDecimal.ZERO) == 0 && en.LimiteInferiorParidad == null && paridadesReales.size() > 0) {

	                    en.LimiteInferiorParidad = paridadesReales.get(0).LimiteInferiorParidad != null ? paridadesReales.get(0).LimiteInferiorParidad : BigDecimal.ZERO;

					}


					/**
					 * ParidadSBIF
					 */


					if(paridadesPorDefecto.get(0).ParidadSBIF.compareTo(BigDecimal.ZERO) != 0 && en.ParidadSBIF == null) {

						en.ParidadSBIF = paridadesPorDefecto.get(0).ParidadSBIF;

					}else if(paridadesPorDefecto.get(0).ParidadSBIF.compareTo(BigDecimal.ZERO) == 0 && en.ParidadSBIF == null && paridadesReales.size() > 0) {

	                    en.ParidadSBIF = paridadesReales.get(0).ParidadSBIF != null ? paridadesReales.get(0).ParidadSBIF : BigDecimal.ZERO;

					}


					/**
					 * TipoCambioMercado
					 */


					if(paridadesPorDefecto.get(0).TipoCambioMercado.compareTo(BigDecimal.ZERO) != 0 && en.TipoCambioMercado == null) {

						en.TipoCambioMercado = paridadesPorDefecto.get(0).TipoCambioMercado;

					}else if(paridadesPorDefecto.get(0).TipoCambioMercado.compareTo(BigDecimal.ZERO) == 0 && en.TipoCambioMercado == null && paridadesReales.size() > 0) {

	                    en.TipoCambioMercado = paridadesReales.get(0).TipoCambioMercado != null ? paridadesReales.get(0).TipoCambioMercado : BigDecimal.ZERO;

					}


					/**
					 * LimiteSuperiorTipoCambio
					 */


					if(paridadesPorDefecto.get(0).LimiteSuperiorTipoCambio.compareTo(BigDecimal.ZERO) != 0 && en.LimiteSuperiorTipoCambio == null) {

						en.LimiteSuperiorTipoCambio = paridadesPorDefecto.get(0).LimiteSuperiorTipoCambio;

					}else if(paridadesPorDefecto.get(0).LimiteSuperiorTipoCambio.compareTo(BigDecimal.ZERO) == 0 && en.LimiteSuperiorTipoCambio == null && paridadesReales.size() > 0) {

	                    en.LimiteSuperiorTipoCambio = paridadesReales.get(0).LimiteSuperiorTipoCambio != null ? paridadesReales.get(0).LimiteSuperiorTipoCambio : BigDecimal.ZERO;

					}

					/**
					 * LimiteInferiorTipoCambio
					 */


					if(paridadesPorDefecto.get(0).LimiteInferiorTipoCambio.compareTo(BigDecimal.ZERO) != 0 && en.LimiteInferiorTipoCambio == null) {

						en.LimiteInferiorTipoCambio = paridadesPorDefecto.get(0).LimiteInferiorTipoCambio;

					}else if(paridadesPorDefecto.get(0).LimiteInferiorTipoCambio.compareTo(BigDecimal.ZERO) == 0 && en.LimiteInferiorTipoCambio == null && paridadesReales.size() > 0) {

	                    en.LimiteInferiorTipoCambio = paridadesReales.get(0).LimiteInferiorTipoCambio != null ? paridadesReales.get(0).LimiteInferiorTipoCambio : BigDecimal.ZERO;

					}

					/**
					 * PrecioSpotCompra
					 */


					if(paridadesPorDefecto.get(0).PrecioSpotCompra.compareTo(BigDecimal.ZERO) != 0 && en.PrecioSpotCompra == null) {

						en.PrecioSpotCompra = paridadesPorDefecto.get(0).PrecioSpotCompra;

					}else if(paridadesPorDefecto.get(0).PrecioSpotCompra.compareTo(BigDecimal.ZERO) == 0 && en.PrecioSpotCompra == null && paridadesReales.size() > 0) {

	                    en.PrecioSpotCompra = paridadesReales.get(0).PrecioSpotCompra != null ? paridadesReales.get(0).PrecioSpotCompra : BigDecimal.ZERO;

					}


					/**
					 * PrecioSpotVenta
					 */


					if(paridadesPorDefecto.get(0).PrecioSpotVenta.compareTo(BigDecimal.ZERO) != 0 && en.PrecioSpotVenta == null) {

						en.PrecioSpotVenta = paridadesPorDefecto.get(0).PrecioSpotVenta;

					}else if(paridadesPorDefecto.get(0).PrecioSpotVenta.compareTo(BigDecimal.ZERO) == 0 && en.PrecioSpotVenta == null && paridadesReales.size() > 0) {

	                    en.PrecioSpotVenta = paridadesReales.get(0).PrecioSpotVenta != null ? paridadesReales.get(0).PrecioSpotVenta : BigDecimal.ZERO;

					}

					/**
					 * PrecioSpotPromedio
					 */


					if(paridadesPorDefecto.get(0).PrecioSpotPromedio.compareTo(BigDecimal.ZERO) != 0 && en.PrecioSpotPromedio == null) {

						en.PrecioSpotPromedio = paridadesPorDefecto.get(0).PrecioSpotPromedio;

					}else if(paridadesPorDefecto.get(0).PrecioSpotPromedio.compareTo(BigDecimal.ZERO) == 0 && en.PrecioSpotPromedio == null && paridadesReales.size() > 0) {

	                    en.PrecioSpotPromedio = paridadesReales.get(0).PrecioSpotPromedio != null ? paridadesReales.get(0).PrecioSpotPromedio : BigDecimal.ZERO;

					}
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

				logger.debug("ACTUALIZACION DE FORMAS DE PAGO OK");
				out.P_OUT_ADVERTENCIASDETALLE.addAll((Collection<? extends AdvertenciasDetalle>) SalidaPrecioparidadesSpot.get("p_out_AdvertenciasDetalle"));
				out.P_OUT_ADVERTENCIASCONTADOR = (Long)SalidaPrecioparidadesSpot.get("p_out_AdvertenciasContador");

				// Agregamos mensaje de que se han insertado correctamente, al parecer el servicio no entrega nada

				for (PreciosParidadesSpot en : entrada.p_DatosParidadYTipoCambio) {

					MensajeSalida mensajeCustom = new MensajeSalida();
					mensajeCustom.codigo = en.IdMoneda;
					mensajeCustom.descripcion = "REGISTROS INSERTADOS CON EXITO";
					mensajeCustom.tipo = MensajeSalida.MENSAJE_OK;
					mensajeCustom.id = en.IdParidadYTipoCambio;

					out.P_OUT_MENSAJES.add(mensajeCustom);
				}

			}
			
			HashMap<String, Object> parametroActualziacionPosicion = new HashMap<String, Object>();
			parametroActualziacionPosicion.put("IdTipoProducto", "MEME");
			parametroActualziacionPosicion.put("CodigoParametro", "ACTUALIZARPOSICIONINICIAL");
			parametroActualziacionPosicion.put("IdEmpresa", entrada.p_IdEmpresa);

			ConsultarConfiguracionMesa consultarConfig = new ConsultarConfiguracionMesa();
			ConfiguracionMesa configuracionMesa = new ConfiguracionMesa();
			configuracionMesa = consultarConfig.logica(conn, entrada.p_IdEmpresa, "MEME", "ACTUALIZARPOSICIONINICIAL");
			
			if(configuracionMesa.Valor.toString().compareTo("S") == 0) {
				EntradaPosicionInicialDiaSiguiente entradaPI = new EntradaPosicionInicialDiaSiguiente();
				ActualizarPosicionInicialDiaSiguiente actualzarPosicionInicial = new ActualizarPosicionInicialDiaSiguiente();
				
				//entradaPI.p_DatosParidadYTipoCambio.add((PreciosParidadesSpot) entrada.p_DatosParidadYTipoCambio);
				entradaPI.p_IdMoneda = entrada.p_DatosParidadYTipoCambio.get(0).IdMoneda;
				entradaPI.p_IdEmpresa = entrada.p_IdEmpresa;
				entradaPI.p_IdUsuario = entrada.p_IdUsuario.toString();
				entradaPI.p_IdModulo = entrada.p_IdModulo;
				SalidaActualizarPosicionInicialDiaSiguiente out_actualizar_posicion = actualzarPosicionInicial.logica(conn, entradaPI);
				if(out_actualizar_posicion.P_OUT_ERRORESCONTADOR.compareTo(0L) == 0) {
					String msge = out_actualizar_posicion.DIA_ACTUALIZACION_POSICION;
					HashMap<String, String> valoresMensajePosicion = new HashMap<String, String>(2);
					valoresMensajePosicion.put("fechaModificacionPosicion", msge);
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, "POSICION_INICIAL_ACTUALIZADA_DIA", valoresMensajePosicion, conn, entrada.p_IdEmpresa));
				}
			}
			

			logger.info("FIN::IngresoPreciosParidadesSpotWs.logica");
			return out;

		} catch (Exception e) {
			// TODO: handle exception
		  	throw e;
		}

	}


	private HashMap<String, Object> actualizarPreciosParidadesSpot(Connection con, SQL sql, EntradaPreciosParidadesSpotWs in, String fechaSistema ) throws SQLException {
		logger.info("INICIO::IngresoPreciosParidadesSpotWs.actualizarPreciosParidadesSpot");

		HashMap<String, Object> parametrosEntrada 	= new HashMap<String, Object>();

		parametrosEntrada.put("p_IdEmpresa", in.p_IdEmpresa);
		parametrosEntrada.put("p_IdUsuario", in.p_IdUsuario);
		parametrosEntrada.put("p_IdModulo", in.p_IdModulo);
		parametrosEntrada.put("p_Fecha", fechaSistema);
		parametrosEntrada.put("p_OrigenJob", 'S');
		parametrosEntrada.put("p_DatosParidadYTipoCambio", in.p_DatosParidadYTipoCambio);


		HashMap<String, Class> parametrosSalida = new HashMap<String, Class>();
		parametrosSalida.put("p_out_AdvertenciasContador", Long.class);
	    parametrosSalida.put("p_out_AdvertenciasDetalle", AdvertenciasDetalle.class);
	    parametrosSalida.put("p_out_ErroresContador", Long.class);
	    parametrosSalida.put("p_out_ErroresDetalle", ErrorDetalle.class);

	    HashMap<String, Object> respuestaIngresoPreciosParidadesSpot;
		try {

			SQL.exec( con, SQL_CREATE_TEMP_TABLE_PARIDADES_SPOT );
	    	sql.batch(con, SQL_INSERT_TEMP_TABLE_PARIDADES_SPOT, in.p_DatosParidadYTipoCambio, PreciosParidadesSpot.class);

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
		try (Connection con = PlataformaUtils.getQAConnection()) {

			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String jsonOptimusDesa = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":51,\"p_IdModulo\":\"MSP\",\"p_OrigenJob\":\"S\",\"p_DatosParidadYTipoCambio\":[{\"IdParidadYTipoCambio\":0,\"IdMoneda\":\"ASD\", \"TipoCambioMercado\":1000,\"LimiteSuperiorTipoCambio\":500,\"LimiteInferiorTipoCambio\":500,\"PorcentajeSuperiorTipoCambio\":0,\"PorcentajeInferiorTipoCambio\":0,\"PrecioSpotCompra\":0,\"PrecioSpotVenta\":0, \"PrecioSpotPromedio\": 10}]}";
			EntradaPreciosParidadesSpotWs Objin = gson.fromJson(jsonOptimusDesa, EntradaPreciosParidadesSpotWs.class);
			IngresoPreciosParidadesSpotWs demo = new IngresoPreciosParidadesSpotWs();



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

