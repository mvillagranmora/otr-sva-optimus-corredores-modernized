package cl.bcs.facturacion;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


import cl.bcs.plataforma.mer.CalcularParidad;

import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class AjusteSencilloComprobante implements JavaCall {

	private static final Logger logger = LogManager.getLogger(AjusteSencilloComprobante.class);
	private final String SQL_MONTOCANT_TRANSACCION = loadResourceToString("cl/bcs/facturacion/sql/AjusteSencilloComprobante_SelectTransaccionMonto.sql");
	private final String SQL_OPERACIONES_ASIG = loadResourceToString("cl/bcs/facturacion/sql/AjusteSencilloComprobante_SelectOperacionesAsignadas.sql");
	private final String SELECT_PARIDAD_COMP = loadResourceToString("cl/bcs/facturacion/sql/AjusteSencilloComprobante_SelectParidadComprobante.sql");
	private final String SELECT_FORMAS_DE_PAGO = loadResourceToString("cl/bcs/facturacion/sql/AjusteSencilloComprobante_SelectFormasdePago.sql");
	private final String UPDATE_ASIGNACION = loadResourceToString("cl/bcs/facturacion/sql/AjusteSencilloComprobante_UpdateAsignacion.sql");
	private final String UPDATE_COMPROBANTE = loadResourceToString("cl/bcs/facturacion/sql/AjusteSencilloComprobante_UpdateComprobante.sql");
	private final String UPDATE_FORMAS_DE_PAGO = loadResourceToString("cl/bcs/facturacion/sql/AjusteSencilloComprobante_UpdateFormasdePago.sql");


	// ENTRADAS
	public static class AjusteSencilloComprobante_Entrada extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_simularOperacion; // S/N se usa para simular operacion (algunos casos)
		public String p_UsaReglasNegocio; // S/N se usa en caso de servicios anidados.
		public String p_EsIngresoAutomatico = "N"; // S/N si viene de otro modulo es S para no validar requisitos
													// basicos nuevamente
		public List<AjusteSencilloComp_Entrada> P_ajusteSencillo;

		public AjusteSencilloComprobante_Entrada() {
			P_ajusteSencillo = new ArrayList<AjusteSencilloComp_Entrada>();

		}

	};

	public static class AjusteSencilloComp_Entrada extends BcsPojo {
		public Long IdMovimientoComprobante;
		public Long IdPortafolio;
		public Long IdAsignacion;
		public String IdFormaCustodia;
		public String IdMonedaMovimientoComprobante;
		public String IdMoneda;
		public String Glosa;
		public BigDecimal MontoMovimientoComprobante;
		public Long IdTransaccion;
		public BigDecimal Cantidad;
		public BigDecimal ValorRescate;
	};
	
	public static class LogDetalladoEntrada extends BcsPojo {
		public AjusteSencilloComprobante_Entrada entradaAjuste;
		public BigDecimal ValorAntiguo;
		public BigDecimal ValorNuevo;
		public String DocumentoIdentidad;
		public String NombreCompleto;
		
		
	}

	

	// SALIDAS
	public static class ResultadoProcesoAjuste_Salida {
		public Long p_out_ErroresContador;
		public Long p_out_InformacionContador;
		public Long p_out_AdvertenciasContador;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class AjusteSencilloComprobante_Salida extends BcsPojo {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		public List<ResultadoProcesoAjuste_Salida> P_OUT_RESULTADOPROCESO;

		public AjusteSencilloComprobante_Salida() {
			P_OUT_MENSAJES = new ArrayList<>();
			P_OUT_RESULTADOPROCESO = new ArrayList<>();
			P_OUT_ERRORESCONTADOR = 0L;
		}
	};

	public static class ParametrosOUT {
		public String Valor;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class MontoOUT {
		public BigDecimal Monto;
		public BigDecimal Cantidad;
		public String IdMoneda;
		public String IdClasificacionTipoInstrumento;
		public BigDecimal Precio;
		public BigDecimal ValorRescate;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class AsignacionesOUT {
		public Long IdAsignacion;
		public BigDecimal MontoAsignado;
		public BigDecimal ValorRescateAsignado;
		public BigDecimal MontoAsignadoLocal;
		public String NombreCompleto;
		public String DocumentoIdentidad;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class FormadePagoOUT {
		public BigDecimal MontoLiquidacionFormaDePago;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	

	public static class ParidadOUT {
		public Date FechaAsignacion;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class UpdateComprobante {
		public Long IdTransaccion;
		public Long IdMovimientoComprobante;
		public BigDecimal MontoAjustado;
		public BigDecimal MontoAjustadoLocal;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class UpdateAsignacion {
		public Long IdTransaccion;
		public Long IdMovimientoComprobante;
		public BigDecimal MontoAjustado;
		public BigDecimal MontoAjustadoLocal;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class UpdateFormasdePago {
		public Long IdMovimientoComprobante;
		public BigDecimal MontoLiquidacionAjustado;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();
		AjusteSencilloComprobante_Entrada jin = gson.fromJson(in.toString(), AjusteSencilloComprobante_Entrada.class);
		AjusteSencilloComprobante_Salida out = logica(conn, jin);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		List<MensajeSalida> l = new ArrayList<>();

		// PARCIALIADES AUN NO
		if (out.P_OUT_ERRORESCONTADOR != null && out.P_OUT_ERRORESCONTADOR > 0) {
			for (MensajeSalida msj : out.P_OUT_MENSAJES) {
				if (msj.tipo.equalsIgnoreCase(MensajeSalida.MENSAJE_ERROR)) {
					l.add(msj);
				}
			}
			out.P_OUT_MENSAJES = l;
		}
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};

	@Trace
	public AjusteSencilloComprobante_Salida logica(Connection conn, AjusteSencilloComprobante_Entrada in)
			throws Exception {
		logger.info("Inicio::AjusteSencilloMovimiento");
		SQL _sql = new SQL();
		String fechaParidad = "";
		String nombreCliente = "";
		String documentoIdentidad = "";
		BigDecimal montoLocalAntiguoAS = BigDecimal.ZERO;
		BigDecimal montoAsignado = BigDecimal.ZERO;
		BigDecimal parametroAjuste = null;
		LogDetalladoEntrada log = new LogDetalladoEntrada();
		log.entradaAjuste = in;
		AjusteSencilloComprobante_Salida out = new AjusteSencilloComprobante_Salida();
		
		// SE OBTIENE EL VALOR DEL PARAMETRO DEL AJUSTE SENCILLO
		try {
			parametroAjuste = ConfiguracionSistema.getParametroConfiguracionDecimal(conn, in.p_IdEmpresa, "FACTURACION", "AJUSTE_SENCILLO_FAC");
		} catch (Exception e) {
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "PARAMETRO_DE_AJUSTESENCILLO_NO_EXISTE", new HashMap<>(), conn, in.p_IdEmpresa));
			return out;
		}
		
		
		AjusteSencilloComp_Entrada AjusteSencilloData = in.P_ajusteSencillo.get(0);
		
		if(AjusteSencilloData.IdTransaccion == 0|| AjusteSencilloData.IdAsignacion == 0 || AjusteSencilloData.IdMovimientoComprobante == 0) {
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,  "AJUSTE_SENCILLO_NO_EXITOSO", new HashMap<>(), conn, in.p_IdEmpresa));
			return out;
		};
		
		
		HashMap<String, Object> cant = new HashMap<String, Object>();
		cant.put("IdTransaccion", AjusteSencilloData.IdTransaccion);
		List<AsignacionesOUT> asignaciones = _sql.query(conn, SQL_OPERACIONES_ASIG, cant, AsignacionesOUT.class);
		
		BigDecimal totalAsignacion = BigDecimal.ZERO;
		
		 for (AsignacionesOUT montoAS : asignaciones) {
			 totalAsignacion = totalAsignacion.add(montoAS.MontoAsignado);
			 if(montoAS.IdAsignacion.equals(AjusteSencilloData.IdAsignacion)) {
				 montoAsignado = montoAS.MontoAsignado;
				 montoLocalAntiguoAS = montoAS.MontoAsignadoLocal;
				 nombreCliente = montoAS.NombreCompleto;
				 documentoIdentidad = montoAS.DocumentoIdentidad;
			 }
	        }
		 
		// SE OBTIENE EL MONTO Y LA CANTIDAD TOTAL DE LA TRANSACCION
			HashMap<String, Object> montoTra = new HashMap<String, Object>();
			montoTra.put("IdTransaccion", AjusteSencilloData.IdTransaccion);
			List<MontoOUT> transaccionMC = _sql.query(conn, SQL_MONTOCANT_TRANSACCION, montoTra, MontoOUT.class);
			MontoOUT transaccionGRL = transaccionMC.get(0);
			String monedaTra = transaccionGRL.IdMoneda;
			String IdClasificacion = transaccionGRL.IdClasificacionTipoInstrumento;
			BigDecimal montoTotal = transaccionGRL.Monto;
			BigDecimal cantidadTotal = transaccionGRL.Cantidad;
			BigDecimal precioTra = transaccionGRL.Precio;
			BigDecimal montoAntiguoAs = MonedaFormato.formatear(conn, montoAsignado, MonedaFormato.MONTO, monedaTra).valor;
			BigDecimal montoAntiguoLocal = MonedaFormato.formatear(conn, montoLocalAntiguoAS, MonedaFormato.MONTO, AjusteSencilloData.IdMonedaMovimientoComprobante).valor;
		
		
		
		
		//LA CANTIDAD DE OPERACIONES DEBE SER MAYOR O IGUAL A 2
		
		if(asignaciones.size() >= 2) {
			
			//SE REALIZA EL CALCULO DEL MONTO ORIGINAL ASIGNADO EN BASE AL TIPO DE MERCADO
			BigDecimal cantidadIn = AjusteSencilloData.Cantidad;
			BigDecimal montoIn = AjusteSencilloData.MontoMovimientoComprobante;
			BigDecimal montoCalculado = BigDecimal.ZERO;
			BigDecimal montoCalculadoPrevio = BigDecimal.ZERO;
			
			if(IdClasificacion.equals("RV")) {
				montoCalculadoPrevio = precioTra.multiply(cantidadIn);
				montoCalculado = MonedaFormato.formatear(conn, montoCalculadoPrevio, MonedaFormato.MONTO, monedaTra).valor;
			}else if (IdClasificacion.equals("RF")) {
				montoCalculadoPrevio = cantidadIn.divide(cantidadTotal,20, RoundingMode.HALF_UP).multiply(montoTotal);
				montoCalculado = MonedaFormato.formatear(conn, montoCalculadoPrevio, MonedaFormato.MONTO, monedaTra).valor;
				
			}else if (IdClasificacion.equals("IF")) {
				BigDecimal valorRescateTra = transaccionGRL.ValorRescate;
				BigDecimal valorRescateMov = AjusteSencilloData.ValorRescate;
				montoCalculadoPrevio = valorRescateMov.divide(valorRescateTra,20, RoundingMode.HALF_UP).multiply(montoTotal);
				montoCalculado = MonedaFormato.formatear(conn, montoCalculadoPrevio, MonedaFormato.MONTO, monedaTra).valor;
			}
			
			
			
			// SI EL MONEDA NO ES CLP, SE CALCULA PARIDAD
			
			if(!monedaTra.equals("CLP")) {
				SimpleDateFormat formatoEntrada = new SimpleDateFormat("yyyy-MM-dd");
				HashMap<String, Object> par = new HashMap<String, Object>();
				par.put("IdTransaccion", AjusteSencilloData.IdTransaccion);
				par.put("IdMovimientoComprobante", AjusteSencilloData.IdMovimientoComprobante);
				List<ParidadOUT> paridadMov = _sql.query(conn, SELECT_PARIDAD_COMP, par, ParidadOUT.class);
				
				fechaParidad = formatoEntrada.format(paridadMov.get(0).FechaAsignacion);
				
				CalcularParidad calcularParidadGRL = new CalcularParidad();
				CalcularParidad.CalcularParidadEntrada paridadGRL = new CalcularParidad.CalcularParidadEntrada();
				paridadGRL.p_IdEmpresa = in.p_IdEmpresa;
				paridadGRL.p_IdTrabajo = 0L;
				
				CalcularParidad.CalculoEntrada datosInParidad = new CalcularParidad.CalculoEntrada();
				
				datosInParidad.Fecha = fechaParidad;
				datosInParidad.IdInterno = 0L;
				datosInParidad.IdMonedaOrigen = monedaTra;
				datosInParidad.IdMonedaDestino = AjusteSencilloData.IdMonedaMovimientoComprobante;
				datosInParidad.IdTrabajo = 0L;
				datosInParidad.IdUso = "";
				datosInParidad.MontoOrigen = montoCalculado;
				datosInParidad.ValorCambioLocalModificado = BigDecimal.ZERO;
				paridadGRL.p_CalculosEntrada.add(datosInParidad);
				
				CalcularParidadSalida respuestaParidad = calcularParidadGRL.logica(conn, paridadGRL);
				List <CalcularParidad.CalculoSalida> datos = respuestaParidad.p_out_CalculosSalida;
				BigDecimal valorParidad = datos.get(0).MontoDestino;
				montoCalculado =  MonedaFormato.formatear(conn, valorParidad, MonedaFormato.MONTO, AjusteSencilloData.IdMonedaMovimientoComprobante).valor;
				
				
			};
				
			// SE CALCULAN LOS LIMITES DEL AJUSTE SENCILLO		
			
			BigDecimal limiteInferior = montoCalculado.subtract(parametroAjuste);
			BigDecimal limiteSuperior = montoCalculado.add(parametroAjuste);
			
			if(montoIn.compareTo(limiteInferior) >= 0 && montoIn.compareTo(limiteSuperior) <= 0) {
				
				if(monedaTra.equals("CLP")) { 
					List<UpdateComprobante> updateComprobante = new ArrayList<>();
					
					UpdateComprobante ajuste = new UpdateComprobante();
					
					ajuste.IdTransaccion = AjusteSencilloData.IdTransaccion;
					ajuste.IdMovimientoComprobante = AjusteSencilloData.IdMovimientoComprobante;
					ajuste.MontoAjustado = montoIn;
					ajuste.MontoAjustadoLocal = montoIn;

					updateComprobante.add(ajuste);
					
					_sql.batch(conn, UPDATE_COMPROBANTE, updateComprobante, UpdateComprobante.class);
					
					List<UpdateAsignacion> updateAsignacion = new ArrayList<>();
					UpdateAsignacion ajusteAsig = new UpdateAsignacion();
					
					ajusteAsig.IdTransaccion = AjusteSencilloData.IdTransaccion;
					ajusteAsig.IdMovimientoComprobante = AjusteSencilloData.IdMovimientoComprobante;
					ajusteAsig.MontoAjustado = montoIn;
					ajusteAsig.MontoAjustadoLocal = montoIn;
					
					
					updateAsignacion.add(ajusteAsig);
					
					_sql.batch(conn, UPDATE_ASIGNACION, updateAsignacion, UpdateAsignacion.class);
					
					//SE VALIDA SI TIENE FORMAS DE PAGO
					
					HashMap<String, Object> formP = new HashMap<String, Object>();
					formP.put("IdMovimientoComprobante", AjusteSencilloData.IdMovimientoComprobante);
					List<FormadePagoOUT> formPOut = _sql.query(conn, SELECT_FORMAS_DE_PAGO, formP, FormadePagoOUT.class); 
					
					if(!formPOut.isEmpty()) {
						
						if(!montoAntiguoAs.equals(montoIn)) {
							BigDecimal montoFormaOUT = formPOut.get(0).MontoLiquidacionFormaDePago;
							BigDecimal montoFormaPFormat = MonedaFormato.formatear(conn, montoFormaOUT, MonedaFormato.MONTO, AjusteSencilloData.IdMonedaMovimientoComprobante).valor;
							BigDecimal diferenciaMontos = montoIn.subtract(montoAntiguoAs);
							BigDecimal resultadoFP= diferenciaMontos.add(montoFormaPFormat);
							
							List<UpdateFormasdePago> updateFormasdePago = new ArrayList<>();
							UpdateFormasdePago ajusteFP = new UpdateFormasdePago();
							
							ajusteFP.IdMovimientoComprobante = AjusteSencilloData.IdMovimientoComprobante;
							ajusteFP.MontoLiquidacionAjustado = resultadoFP;
							
							updateFormasdePago.add(ajusteFP);
							_sql.batch(conn, UPDATE_FORMAS_DE_PAGO, updateFormasdePago, UpdateFormasdePago.class);	
							
						}
	
					}
					
					log.NombreCompleto = nombreCliente;
					log.DocumentoIdentidad = documentoIdentidad;
					log.ValorAntiguo = montoAntiguoLocal;
					log.ValorNuevo = montoIn;
					
					
					AgregarLog(conn, log);
					
					
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_INFORMACION, "AJUSTE_SENCILLO_REALIZADO", new HashMap<>(), conn, in.p_IdEmpresa));
					
				}else {
					
					// SE CALCULA LA PARIDAD DEL MONTO RECALCULADO
						
					CalcularParidad calcularParidadOut = new CalcularParidad();
					CalcularParidad.CalcularParidadEntrada paridadOut = new CalcularParidad.CalcularParidadEntrada();
					paridadOut.p_IdEmpresa = in.p_IdEmpresa;
					paridadOut.p_IdTrabajo = 0L;
						
					CalcularParidad.CalculoEntrada datosOutParidad = new CalcularParidad.CalculoEntrada();
						
					datosOutParidad.Fecha = fechaParidad;
					datosOutParidad.IdInterno = 0L;
					datosOutParidad.IdMonedaOrigen =  AjusteSencilloData.IdMonedaMovimientoComprobante;
					datosOutParidad.IdMonedaDestino = monedaTra;
					datosOutParidad.IdTrabajo = 0L;
					datosOutParidad.IdUso = "";
					datosOutParidad.MontoOrigen = montoIn;
					datosOutParidad.ValorCambioLocalModificado = BigDecimal.ZERO;
					paridadOut.p_CalculosEntrada.add(datosOutParidad);
						
					CalcularParidadSalida respuestaParidadOut = calcularParidadOut.logica(conn, paridadOut);
					List <CalcularParidad.CalculoSalida> datos = respuestaParidadOut.p_out_CalculosSalida;
					BigDecimal valorParidadOut = datos.get(0).MontoDestino;
					
					BigDecimal montoCalculadoOut = MonedaFormato.formatear(conn, valorParidadOut, MonedaFormato.MONTO, monedaTra).valor;
				
					
					// SE ACTUALIZAN LOS REGISTROS
					List<UpdateComprobante> updateComprobante = new ArrayList<>();
					
					UpdateComprobante ajuste = new UpdateComprobante();
					
					ajuste.IdTransaccion = AjusteSencilloData.IdTransaccion;
					ajuste.IdMovimientoComprobante = AjusteSencilloData.IdMovimientoComprobante;
					ajuste.MontoAjustadoLocal = montoIn;
					ajuste.MontoAjustado = montoCalculadoOut;

					updateComprobante.add(ajuste);
					
					_sql.batch(conn, UPDATE_COMPROBANTE, updateComprobante, UpdateComprobante.class);
					
					List<UpdateAsignacion> updateAsignacion = new ArrayList<>();
					UpdateAsignacion ajusteAsig = new UpdateAsignacion();
					
					ajusteAsig.IdTransaccion = AjusteSencilloData.IdTransaccion;
					ajusteAsig.IdMovimientoComprobante = AjusteSencilloData.IdMovimientoComprobante;
					ajusteAsig.MontoAjustadoLocal = montoIn;
					ajusteAsig.MontoAjustado = montoCalculadoOut;
					
					updateAsignacion.add(ajusteAsig);
					
					_sql.batch(conn, UPDATE_ASIGNACION, updateAsignacion, UpdateAsignacion.class);
					
					//SE VALIDA SI TIENE FORMAS DE PAGO
					
					HashMap<String, Object> formP = new HashMap<String, Object>();
					formP.put("IdMovimientoComprobante", AjusteSencilloData.IdMovimientoComprobante);
					List<FormadePagoOUT> formPOut = _sql.query(conn, SELECT_FORMAS_DE_PAGO, formP, FormadePagoOUT.class);
					
					if(!formPOut.isEmpty()) {
						
						if(!montoAntiguoAs.equals(montoCalculadoOut)) {
							BigDecimal montoFormaOUT = formPOut.get(0).MontoLiquidacionFormaDePago;
							BigDecimal montoFormaPFormat = MonedaFormato.formatear(conn, montoFormaOUT, MonedaFormato.MONTO, monedaTra).valor;
							BigDecimal diferenciaMontos = montoCalculadoOut.subtract(montoAntiguoAs);
							BigDecimal resultadoFP= diferenciaMontos.add(montoFormaPFormat);
							
							List<UpdateFormasdePago> updateFormasdePago = new ArrayList<>();
							UpdateFormasdePago ajusteFP = new UpdateFormasdePago();
							
							ajusteFP.IdMovimientoComprobante = AjusteSencilloData.IdMovimientoComprobante;
							ajusteFP.MontoLiquidacionAjustado = resultadoFP;
							
							updateFormasdePago.add(ajusteFP);
							_sql.batch(conn, UPDATE_FORMAS_DE_PAGO, updateFormasdePago, UpdateFormasdePago.class);	
							
						}
						
	
					}
					log.NombreCompleto = nombreCliente;
					log.DocumentoIdentidad = documentoIdentidad;
					log.ValorAntiguo = montoAntiguoLocal;
					log.ValorNuevo = montoIn;
					
					AgregarLog(conn, log);
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_INFORMACION,  "AJUSTE_SENCILLO_REALIZADO", new HashMap<>(), conn, in.p_IdEmpresa));	
				}
				
				
			}else {
				HashMap<String, String> parametroAS = new HashMap<String, String>();
				parametroAS.put("rangoAS", parametroAjuste.toString());
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,  "AJUSTE_SENCILLO_FUERA_DE_RANGO", parametroAS , conn, in.p_IdEmpresa));
				return out;
			}
			
			
		}else {
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,  "AJUSTE_SENCILLO_NO_EXITOSO", new HashMap<>(), conn, in.p_IdEmpresa));
			return out;
		}
		
		logger.info("Fin::AjusteSencilloMovimiento");
		return out;

	};
	
	public void AgregarLog(Connection con, LogDetalladoEntrada in) {
		try {
			String FechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.entradaAjuste.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
			Long IdCabeceraLog =  in.entradaAjuste.p_IdUsuario;
			
			AjusteSencilloComp_Entrada AjusteSencilloDataLog = in.entradaAjuste.P_ajusteSencillo.get(0);
			//CABECERA
			AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
			AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
			InAgregarLog.DetalleLog detalle= new InAgregarLog.DetalleLog();
			
			entradaAgregarLog.p_IdEmpresa = in.entradaAjuste.p_IdEmpresa;
			entradaAgregarLog.p_IdModulo = in.entradaAjuste.p_IdModulo;
			entradaAgregarLog.p_IdUsuario = in.entradaAjuste.p_IdUsuario;
			entradaAgregarLog.p_UsaReglasNegocio = in.entradaAjuste.p_UsaReglasNegocio;
				
			log.IdCabeceraLog = IdCabeceraLog;
			log.IdModulo = in.entradaAjuste.p_IdModulo;
			log.IdUsuario = in.entradaAjuste.p_IdUsuario;
			log.IdEmpresa = in.entradaAjuste.p_IdEmpresa;
			log.FechaSistemaLog = FechaSistema;
			log.IdUsuarioAtendido = 0L;
			log.IdCliente = 0L;
			log.EstadoCliente = "";
			log.NombreCliente = in.NombreCompleto;
			log.IdTipoDocumentoIdentidad = "";
			log.IdDocumentoIdentidad = in.DocumentoIdentidad;
			log.IdPortafolio = AjusteSencilloDataLog.IdPortafolio;
			log.EstadoPortafolio = "";
			log.IdTipoRegistro = "AJUSTESENCILLO";
			log.EstadoRegistro = "";
			log.IdTipoOperacion = "";
			log.Folio = 0L;
			log.Cantidad = AjusteSencilloDataLog.Cantidad;
			log.PrecioTasa = BigDecimal.ZERO;
			log.Monto = AjusteSencilloDataLog.MontoMovimientoComprobante;
			log.IdMonedaOrigen = AjusteSencilloDataLog.IdMoneda;
			log.IdMonedaLiquidacion = AjusteSencilloDataLog.IdMonedaMovimientoComprobante;
			log.TipoCambio = "";
			log.Nemo = "";
			log.IdProceso = 0L;
			log.EstadoProceso = "";
			log.IdPersona = 0L;
			log.IdTipoLog = "MODIFICACION";
			log.NombreProceso = "Ajuste Sencillo";
			
			detalle.IdCabeceraLog = IdCabeceraLog;
			detalle.ValorAntiguo =in.ValorAntiguo.toString();
			detalle.ValorNuevo= in.ValorNuevo.toString();
			detalle.CampoRegistro = "Monto Local";
			//AÑADIR AL LOG
			entradaAgregarLog.p_Log.add(log);
			entradaAgregarLog.p_LogDetalle.add(detalle);
			
			//REGISTRO
			AgregarLog agregarLogService = new AgregarLog();
			agregarLogService.logica(con, entradaAgregarLog);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getQAConnection()) {
			try {
				Gson gson = Utils.getGsonBuilder();
				String jsonRFUS= "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdModulo\":\"FAC\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_EsIngresoAutomatico\":\"N\",\"P_ajusteSencillo\":[{\"IdMovimientoComprobante\":783045,\"IdMonedaMovimientoComprobante\":\"CLP\",\"IdMoneda\":\"USD\",\"IdPortafolio\":1676,\"IdAsignacion\":471727,\"IdFormaCustodia\":\"CUS\",\"Glosa\":\"Orden Postrera\",\"MontoMovimientoComprobante\":47983,\"IdTransaccion\":728543,\"Cantidad\":20000,\"ValorRescate\":0}]}";
				AjusteSencilloComprobante_Entrada Objin = gson.fromJson(jsonRFUS, AjusteSencilloComprobante_Entrada.class);
				AjusteSencilloComprobante ajusteSencilloComp = new AjusteSencilloComprobante();
				AjusteSencilloComprobante_Salida Objout = ajusteSencilloComp.logica(con, Objin);
				System.out.println(Objout.toString());

				con.rollback();
				//con.commit();

			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	};
};