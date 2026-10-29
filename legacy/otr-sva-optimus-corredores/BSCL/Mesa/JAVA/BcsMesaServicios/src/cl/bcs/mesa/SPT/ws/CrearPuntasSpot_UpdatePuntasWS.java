package cl.bcs.mesa.SPT.ws;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.mesa.SPT.ConsultarPuntasMantenedor;
import cl.bcs.mesa.SPT.ConsultarPuntasPorTramo.Punta;
import cl.bcs.mesa.SPT.CrearPuntasSpot;
import cl.bcs.mesa.SPT.CrearPuntasSpot.Puntas;
import cl.bcs.mesa.SPT.CrearPuntasSpot.PuntasSalida;
import cl.bcs.mesa.SPT.CrearPuntasSpot.PuntasTramo;
import cl.bcs.plataforma.pau.CambiarEstadoModuloMesaSpot.OutParidadesTipoCambio;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.StatsD;
import cl.bcs.utils.Utils;

public class CrearPuntasSpot_UpdatePuntasWS implements JavaCall {
	static final Logger logger = LogManager.getLogger(CrearPuntasSpot_UpdatePuntasWS.class);
	private final String SQL_CONSULTAR_MONEDA_PUNTAS = "SELECT \"IdMoneda\" FROM \"BSCL.Mesa.BD.Vistas::SPT.ConsultarMonedaPuntas\" WHERE \"IdEstado\" = :IdEstado AND \"Codigo\" = :Codigo";
	private final String SQL_CONSULTAR_MONEDA_TRAMOS = "SELECT \"IdPuntasPorTramo\",\"IdEmpresa\",\"IdPuntas\",\"MontoPorTramo\",\"PorcentajeTramo\",\"MontoCentavos\",\"ValorCompra\",\"ValorVenta\",\"Estado\",\"IdUsuario\",\"Usuario\",\"Canal\",\"IdMonedaPunta\" FROM \"BSCL.Mesa.BD.Vistas::SPT.ConsultarPuntasPorTramo\" WHERE \"IdPuntas\" = :IdPunta AND \"Estado\" = :Estado";
	private final String SQL_CONSULTAR_PARIDAD_TIPO_CAMBIO = "SELECT \"TipoCambioMercado\",\"LimiteSuperiorTipoCambio\",\"LimiteInferiorTipoCambio\" FROM \"BSCL.Mesa.BD::SPT.ParidadYTipoCambio\" WHERE \"IdMoneda\" = :IdMoneda AND \"IdEstado\" = 'ACT' AND \"Fecha\" = :Fecha";

	public static final String METRIC_PREFIX = "mesa.spt.ws.updatepuntasws";
	public static final String METRIC_TIEMPO_EJECUCION = METRIC_PREFIX + "." + "tiempo_ejecucion";
	public static final String METRIC_FRECUENCIA_LLAMADOS = METRIC_PREFIX + "." + "frecuencia_llamado";


	public static class EntradaPuntasPrecioWS extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public List<EntradaPuntasPrecio> p_PuntaPrecio;

		public EntradaPuntasPrecioWS() {
			this.p_PuntaPrecio = new ArrayList<EntradaPuntasPrecio>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class EntradaPuntasPrecio extends BcsPojo {
		public String IdMoneda;
		public BigDecimal PuntaCompra;
		public BigDecimal PuntaVenta;
	}

	public static class SalidaPuntasPrecio extends BcsPojo {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		public Long P_OUT_ADVERTENCIASCONTADOR;

		public SalidaPuntasPrecio() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaPuntasPrecioWS jin = gson.fromJson(in.toString(), EntradaPuntasPrecioWS.class);
		SalidaPuntasPrecio out = logica(conn, jin);

		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public SalidaPuntasPrecio logica(Connection con, EntradaPuntasPrecioWS entrada) throws Exception {
		logger.info("INICIO::CrearPuntasSpot_UpdatePuntasWS");
		long inicioServicio = System.currentTimeMillis();
		SalidaPuntasPrecio out = actualizarPuntas(con, entrada);
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearPuntasSpot_UpdatePuntasWS", finServicio - inicioServicio);
		return out;
	}

	private SalidaPuntasPrecio actualizarPuntas(Connection con, EntradaPuntasPrecioWS entrada) throws Exception {
		logger.debug("INICIO::CrearPuntasSpot_UpdatePuntasWS.actualizarPuntas");
	    long inicio = System.currentTimeMillis();

		SQL sql = new SQL();
		SalidaPuntasPrecio out = new SalidaPuntasPrecio();

		try {
			CrearPuntasSpot crearPuntasSpot = new CrearPuntasSpot();

			// Obtener la fecha y hora actuales
			Long offset = ConfiguracionSistema.getParametroConfiguracionLong(con,entrada.p_IdEmpresa,"PLATAFORMA","PLA.SegundosZonaHoraria");

			String fechaHora = "";
			String fecha = "";

	    	ZonedDateTime.now();
	    	LocalDateTime localDate = LocalDateTime.now();
	    	localDate = localDate.plusSeconds(offset);
	    	
	    	DateTimeFormatter formatterFechaHora = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
	    	DateTimeFormatter formatterFecha = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	    	LocalDateTime ahora = LocalDateTime.now();
			Timestamp fechaTimestamp = Timestamp.valueOf(ahora);		
	    	fechaHora = localDate.format(formatterFechaHora);	    	SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");	    				
			Date parsedDate = dateFormat.parse(fechaHora);
    	    Timestamp fechaHoraTimestamp = new java.sql.Timestamp(parsedDate.getTime());
	    
    	    fecha = localDate.format(formatterFecha);	
	    		
			// Obtener la fecha del sistema
			String fechaServer = ConfiguracionSistema.getParametroConfiguracionString(con,entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");



			// Valida moneda
			HashMap<String, Object> paramsMoneda = new HashMap<String, Object>();

			paramsMoneda.put("IdEstado", "ACT");
			paramsMoneda.put("Codigo", entrada.p_PuntaPrecio.get(0).IdMoneda);

			List<Punta> puntaList = sql.query(con, SQL_CONSULTAR_MONEDA_PUNTAS, paramsMoneda, Punta.class);
			if (puntaList.size() == 0 || puntaList == null) {
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "PUNTA", "Moneda invalida."));
				out.P_OUT_ERRORESCONTADOR = 1L;
				out.P_OUT_ADVERTENCIASCONTADOR = 0L;
				return out;
			}
			

			// Obtener puntas
			ConsultarPuntasMantenedor consultarPuntas = new ConsultarPuntasMantenedor();
			cl.bcs.mesa.SPT.ConsultarPuntasMantenedor.PuntasEntrada puntasEntradaConsulta = new cl.bcs.mesa.SPT.ConsultarPuntasMantenedor.PuntasEntrada();
			puntasEntradaConsulta.IdEmpresa = entrada.p_IdEmpresa;
			puntasEntradaConsulta.IdMoneda = entrada.p_PuntaPrecio.get(0).IdMoneda;
			cl.bcs.mesa.SPT.ConsultarPuntasMantenedor.PuntasSalida puntasSalida = consultarPuntas.logica(con,
					puntasEntradaConsulta);
			if (puntasSalida != null) {
				cl.bcs.mesa.SPT.ConsultarPuntasMantenedor.Puntas puntaConsulta = puntasSalida.P_OUT_PUNTAS.get(0);

				BigDecimal nuevaPuntaCompraAux = entrada.p_PuntaPrecio.get(0).PuntaCompra;
				BigDecimal nuevaPuntaVentaAux = entrada.p_PuntaPrecio.get(0).PuntaVenta;
				BigDecimal PuntaDefaultCompra = puntaConsulta.PuntaCompra;
				BigDecimal PuntaDefaultVenta = puntaConsulta.PuntaVenta;

				if ((entrada.p_PuntaPrecio.get(0).PuntaCompra == null || entrada.p_PuntaPrecio.get(0).PuntaCompra.equals(BigDecimal.ZERO))
						|| (entrada.p_PuntaPrecio.get(0).PuntaVenta == null || entrada.p_PuntaPrecio.get(0).PuntaVenta.equals(BigDecimal.ZERO))) {
					if ((entrada.p_PuntaPrecio.get(0).PuntaCompra != null) && (!entrada.p_PuntaPrecio.get(0).PuntaCompra.equals(BigDecimal.ZERO))) {
						if (nuevaPuntaCompraAux.compareTo(PuntaDefaultVenta) == 1) {
							out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "PUNTA",
									"PuntaCompra no puede ser mayor a punta default venta actual. Ingrese PuntaVenta para actualizar."));
							out.P_OUT_ERRORESCONTADOR = 1L;
							out.P_OUT_ADVERTENCIASCONTADOR = 0L;
							return out;
						}
					} else {
						nuevaPuntaCompraAux = puntaConsulta.PuntaCompra;
					}

					if ((entrada.p_PuntaPrecio.get(0).PuntaVenta != null) && (!entrada.p_PuntaPrecio.get(0).PuntaVenta.equals(BigDecimal.ZERO))) {
						if (nuevaPuntaVentaAux.compareTo(PuntaDefaultCompra) == -1) {
							out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "PUNTA",
									"PuntaVenta no puede ser menor a punta default compra actual. Ingrese PuntaCompra para actualizar."));
							out.P_OUT_ERRORESCONTADOR = 1L;
							out.P_OUT_ADVERTENCIASCONTADOR = 0L;
							return out;
						}
					} else {
						nuevaPuntaVentaAux = puntaConsulta.PuntaVenta;
					}
				}

				BigDecimal nuevaAperturaPrecio = nuevaPuntaVentaAux.subtract(nuevaPuntaCompraAux).multiply(new BigDecimal(100));
				BigDecimal nuevaPuntaCompra = nuevaPuntaCompraAux;
				BigDecimal nuevaPuntaVenta = nuevaPuntaVentaAux;

				
				
				//Validar Tipo Cambio 
				HashMap<String,Object> parmsInTipoCambio =new HashMap<String,Object>();
				parmsInTipoCambio.put("IdMoneda", entrada.p_PuntaPrecio.get(0).IdMoneda);
				parmsInTipoCambio.put("Fecha", fechaServer);			
				
				List<OutParidadesTipoCambio> outConsultarParidadTipoCambio = sql.query(con,SQL_CONSULTAR_PARIDAD_TIPO_CAMBIO, parmsInTipoCambio, OutParidadesTipoCambio.class);
				if (outConsultarParidadTipoCambio.size() == 0 || outConsultarParidadTipoCambio == null) {
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "PUNTA", "No existe tipo de cambio para la moneda o fecha"));
					out.P_OUT_ERRORESCONTADOR = 1L;
					out.P_OUT_ADVERTENCIASCONTADOR = 0L;
					return out;
					
				} else {
					//Validar limites
					BigDecimal valorTipoCambio = outConsultarParidadTipoCambio.get(0).TipoCambioMercado;
					BigDecimal valorLimiteInferior = outConsultarParidadTipoCambio.get(0).LimiteInferiorTipoCambio;
					BigDecimal valorLimiteSuperior = outConsultarParidadTipoCambio.get(0).LimiteSuperiorTipoCambio;
					
					BigDecimal limiteInferiorPunta = valorTipoCambio.subtract(valorLimiteInferior);
					BigDecimal limiteSuperiorPunta = valorTipoCambio.add(valorLimiteSuperior);
					
					if (nuevaPuntaCompra.compareTo(limiteInferiorPunta) == -1 || nuevaPuntaVenta.compareTo(limiteSuperiorPunta) == 1) {
						out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "PUNTA",
								"Puntas fuera de rango ["+limiteInferiorPunta.setScale(4, RoundingMode.HALF_UP) +";"+ limiteSuperiorPunta.setScale(4, RoundingMode.HALF_UP)+"]"));
						out.P_OUT_ERRORESCONTADOR = 1L;
						out.P_OUT_ADVERTENCIASCONTADOR = 0L;
						return out;
					}
					
				} 
				
				
				// Crear puntas
				cl.bcs.mesa.SPT.CrearPuntasSpot.PuntasEntrada crearPuntasSpotEntrada = new cl.bcs.mesa.SPT.CrearPuntasSpot.PuntasEntrada();
				crearPuntasSpotEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
				crearPuntasSpotEntrada.p_IdMoneda = entrada.p_PuntaPrecio.get(0).IdMoneda;
				crearPuntasSpotEntrada.p_fecha =fechaHoraTimestamp;
				crearPuntasSpotEntrada.p_DatosPuntas = entrada.p_PuntaPrecio.stream().map(puntaPrecio -> {
					Puntas punta = new Puntas();
					punta.IdPuntas = puntaConsulta.IdPuntas;
					punta.IdMonedaPunta = entrada.p_PuntaPrecio.get(0).IdMoneda;
					punta.IdUsuario = entrada.p_IdUsuario.toString();
					punta.AperturaPrecio = nuevaAperturaPrecio;
					punta.PuntaCompra = nuevaPuntaCompra;
					punta.PuntaVenta = nuevaPuntaVenta;
					punta.CheckPrecioPorTramo = puntaConsulta.CheckPrecioPorTramo;
					punta.Estado = puntaConsulta.Estado;
					punta.IdEmpresa = entrada.p_IdEmpresa;
					punta.Fecha =fechaTimestamp ;
					punta.Origen = "WS";
					return punta;
				}).collect(Collectors.toList());

				// Obtener puntas por tramo
				HashMap<String, Object> paramsTramo = new HashMap<String, Object>();

				paramsTramo.put("Estado", "VIG");
				paramsTramo.put("IdPunta", puntaConsulta.IdPuntas);

				List<PuntasTramo> tramoList = sql.query(con, SQL_CONSULTAR_MONEDA_TRAMOS, paramsTramo,
						PuntasTramo.class);
				if (puntaList.size() == 0) {
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA, "TRAMO",
							"No hay tramos para la moneda."));
					out.P_OUT_ADVERTENCIASCONTADOR = 1L;
				}
				
				for (PuntasTramo elemento : tramoList) {
		            elemento.IdUsuario = entrada.p_IdUsuario.toString();
		        }
				
				crearPuntasSpotEntrada.p_DatosPuntasTramo = tramoList;

				PuntasSalida crearPuntasSpotSalida = crearPuntasSpot.logica(con, crearPuntasSpotEntrada);

				if (crearPuntasSpotSalida.P_OUT_ERRORESCONTADOR > 0) {
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "PUNTA",
							"Error al crear o actualizar las puntas"));
					out.P_OUT_ERRORESCONTADOR = 1L;
					out.P_OUT_ADVERTENCIASCONTADOR = 0L;
					return out;

				} else if (crearPuntasSpotSalida.P_OUT_ERRORESCONTADOR == 0) {
					if (entrada.p_PuntaPrecio.get(0).PuntaCompra == null || entrada.p_PuntaPrecio.get(0).PuntaCompra.equals(BigDecimal.ZERO)) {
						out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, "OK",
								"Punta default venta actualizada OK. "
										+ "Punta default compra = " + nuevaPuntaCompra.setScale(4, RoundingMode.HALF_UP) + ". "
										+ "Punta default venta = " + nuevaPuntaVenta.setScale(4, RoundingMode.HALF_UP) + ". "
										+ "Apertura de precio = "+ nuevaAperturaPrecio.setScale(0, RoundingMode.HALF_UP)));
						out.P_OUT_ADVERTENCIASCONTADOR = crearPuntasSpotSalida.P_OUT_ADVERTENCIASCONTADOR;
						out.P_OUT_ERRORESCONTADOR = crearPuntasSpotSalida.P_OUT_ERRORESCONTADOR;
					} else if (entrada.p_PuntaPrecio.get(0).PuntaVenta == null || entrada.p_PuntaPrecio.get(0).PuntaVenta.equals(BigDecimal.ZERO)) {
						out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, "OK",
								"Punta default compra actualizada OK. "
										+ "Punta default compra = " + nuevaPuntaCompra.setScale(4, RoundingMode.HALF_UP) + ". "
										+ "Punta default venta = " + nuevaPuntaVenta.setScale(4, RoundingMode.HALF_UP) + ". "
										+ "Apertura de precio = "+ nuevaAperturaPrecio.setScale(0, RoundingMode.HALF_UP)));
						out.P_OUT_ADVERTENCIASCONTADOR = crearPuntasSpotSalida.P_OUT_ADVERTENCIASCONTADOR;
						out.P_OUT_ERRORESCONTADOR = crearPuntasSpotSalida.P_OUT_ERRORESCONTADOR;
					} else {
						out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, "OK",
								"Puntas actualizadas OK. "
										+ "Punta default compra = " + nuevaPuntaCompra.setScale(4, RoundingMode.HALF_UP) + ". "
										+ "Punta default venta = " + nuevaPuntaVenta.setScale(4, RoundingMode.HALF_UP) + ". "
										+ "Apertura de precio = "+ nuevaAperturaPrecio.setScale(0, RoundingMode.HALF_UP)));
						out.P_OUT_ADVERTENCIASCONTADOR = crearPuntasSpotSalida.P_OUT_ADVERTENCIASCONTADOR;
						out.P_OUT_ERRORESCONTADOR = crearPuntasSpotSalida.P_OUT_ERRORESCONTADOR;
					}
				    long fin = System.currentTimeMillis();
				    

					StatsD.reportExecutionTimeMilis(METRIC_TIEMPO_EJECUCION, (fin - inicio), "moneda:"+entrada.p_PuntaPrecio.get(0).IdMoneda);


				}
			


			//Construir salida y agregar al log
			AgregarLog agregarLog = new AgregarLog();
			InAgregarLog entradaLog = new InAgregarLog();
			
				
				//Construir entradas log
				InAgregarLog.Log headLog = new InAgregarLog.Log();
				headLog.IdEmpresa = entrada.p_IdEmpresa;
				headLog.IdModulo = "PLA";
				headLog.IdUsuario = entrada.p_IdUsuario;
				headLog.IdTipoLog = "PUNTAS";
				headLog.IdCabeceraLog = 161L;
				headLog.FechaSistemaLog = fecha;
				headLog.IdUsuarioAtendido = entrada.p_IdUsuario;
				headLog.IdTipoRegistro = "PUNTAS";
				headLog.Folio = 0L;
				entradaLog.p_Log.add(headLog);
				
				InAgregarLog.DetalleLog detLogPuntaCompra = new InAgregarLog.DetalleLog();
				detLogPuntaCompra.IdCabeceraLog = 161L;
				detLogPuntaCompra.SubRegistro = "PUNTAS";
				detLogPuntaCompra.CampoRegistro = "Punta compra";
				detLogPuntaCompra.ValorAntiguo = PuntaDefaultCompra.setScale(4, RoundingMode.HALF_UP).toString();
				detLogPuntaCompra.ValorNuevo = nuevaPuntaCompra.setScale(4, RoundingMode.HALF_UP).toString();
				entradaLog.p_LogDetalle.add(detLogPuntaCompra);				
				
				InAgregarLog.DetalleLog detLogPuntaVenta = new InAgregarLog.DetalleLog();
				detLogPuntaVenta.IdCabeceraLog = 161L;
				detLogPuntaVenta.SubRegistro = "PUNTAS";
				detLogPuntaVenta.CampoRegistro = "Punta venta";
				detLogPuntaVenta.ValorAntiguo = PuntaDefaultVenta.setScale(4, RoundingMode.HALF_UP).toString();
				detLogPuntaVenta.ValorNuevo = nuevaPuntaVenta.setScale(4, RoundingMode.HALF_UP).toString();
				entradaLog.p_LogDetalle.add(detLogPuntaVenta);

				InAgregarLog.DetalleLog detLogAperturaPrecio = new InAgregarLog.DetalleLog();
				detLogAperturaPrecio.IdCabeceraLog = 161L;
				detLogAperturaPrecio.SubRegistro = "PUNTAS";
				detLogAperturaPrecio .CampoRegistro = "Apertura de precio";
				detLogAperturaPrecio .ValorAntiguo = puntaConsulta.AperturaPrecio.setScale(0, RoundingMode.HALF_UP).toString();
				detLogAperturaPrecio .ValorNuevo = nuevaAperturaPrecio.setScale(0, RoundingMode.HALF_UP).toString();
				entradaLog.p_LogDetalle.add(detLogAperturaPrecio );
			
			//guardar log.
				
			agregarLog.logica(con, entradaLog);
			}
		} catch (Exception e) {
			out.P_OUT_ERRORESCONTADOR = 1L;
			// logger.error(e);
			throw e;
		}

		return out;

	}

	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) { // getBOLSAConnection
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
					.serializeNulls().create();

			String jsonOptimusDesa = "{p_IdEmpresa: 1, p_IdUsuario: 1, p_PuntaPrecio: [{\"IdMoneda\":\"USD\",\"PuntaVenta\":880,\"PuntaCompra\":880}]}";

			EntradaPuntasPrecioWS Objin = gson.fromJson(jsonOptimusDesa, EntradaPuntasPrecioWS.class);
			CrearPuntasSpot_UpdatePuntasWS demo = new CrearPuntasSpot_UpdatePuntasWS();
			System.out.println("ENTRADA");
			System.out.println(Objin.toString());
			System.out.println("SALIDA");
			try {
				SalidaPuntasPrecio ObjOut = demo.logica(con, Objin);

				//con.commit();
				 con.rollback();
				System.out.println(ObjOut);

			} catch (Exception er) {
				// er.printStackTrace();
				con.rollback();

				System.out.println(er.getMessage());
			}
			// con.commit();
			finally {
				con.close();
			}

			System.out.println("== END ==");
		}

	}

}
