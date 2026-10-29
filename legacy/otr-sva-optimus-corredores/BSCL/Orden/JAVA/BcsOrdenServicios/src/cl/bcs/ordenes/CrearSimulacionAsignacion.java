package cl.bcs.ordenes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.ordenes.ConsultarCobrosOrdenes.ConsultarCobrosOrdenes_Salida;
import cl.bcs.ordenes.ConsultarCobrosOrdenes.InformacionOperacion;
import cl.bcs.ordenes.ConsultarCobrosOrdenes.LineaMatrizOperacional;
import cl.bcs.ordenes.ConsultarCobrosOrdenesPe.ConsultarCobrosOrdenesPe_Entrada;
import cl.bcs.ordenes.ConsultarCobrosOrdenesPe.ConsultarCobrosOrdenesPe_Salida;
import cl.bcs.ordenes.ConsultarCobrosOrdenesPe.DefinicionCobro_Salida;
import cl.bcs.ordenes.ConsultarCobrosOrdenesPe.InformacionOrden_Entrada;
import cl.bcs.ordenes.CrearSimulacionAsignacionCronologica.EntradaCrearSimulacionAsignacionCronologica;
import cl.bcs.ordenes.CrearSimulacionAsignacionCronologica.SalidaCrearSimulacionAsignacionCronologica;
import cl.bcs.ordenes.CrearSimulacionAsignacionPropCrono.EntradaCrearSimulacionAsignacionPropCrono;
import cl.bcs.ordenes.CrearSimulacionAsignacionPropCrono.SalidaCrearSimulacionAsignacionPropCrono;


import cl.bcs.ordenes.CrearSimulacionAsignacionPostrera.EntradaCrearSimulacionAsignacionPostrera;
import cl.bcs.ordenes.CrearSimulacionAsignacionPostrera.SalidaCrearSimulacionAsignacionPostrera;



import cl.bcs.ordenes.pojo.ConstantesOrdenes;
import cl.bcs.ordenes.pojo.IndicadoresPortafolio;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class CrearSimulacionAsignacion implements JavaCall {
	
	private Logger log = LogManager.getLogger(CrearSimulacionAsignacion.class);
	
	private final String SQL_INSERT_TRANSACCIONES = loadResourceToString("cl/bcs/ordenes/sql/CrearSimulacionAsignacion_InsertTransacciones.sql");	
	private final String SQL_INSERT_CARTERAS = loadResourceToString("cl/bcs/ordenes/sql/CrearSimulacionAsignacion_InsertCarteras.sql");	
	private final String SQL_INSERT_ORDENES = loadResourceToString("cl/bcs/ordenes/sql/CrearSimulacionAsignacion_InsertOrdenes.sql");	
	private final String SQL_INSERT_PORTAFOLIOS = loadResourceToString("cl/bcs/ordenes/sql/CrearSimulacionAsignacion_InsertPortafolios.sql");	

	private final String SQL_CALL_SIMULACION_ASIGNACION = loadResourceToString("cl/bcs/ordenes/sql/CrearSimulacionAsignacion_CallCrearSimulacionAsignacion.sql");
	private final String SQL_SELECT_SALIDA = loadResourceToString("cl/bcs/ordenes/sql/CrearSimulacionAsignacion_SelectSalida.sql");
	private final String SQL_SELECT_ORDENES = loadResourceToString("cl/bcs/ordenes/sql/CrearSimulacionAsignacion_SelectOrdenes.sql");
	
	private final String SQL_SELECT_INDICADORES_PORTAFOLIO = loadResourceToString("cl/bcs/ordenes/sql/Globales_SelectIndicadoresPortafolio.sql");
	private final String SQL_SELECT_PREFERENCIA_PAGO_DEFECTO = loadResourceToString("cl/bcs/ordenes/sql/CrearSimulacionAsignacion_SelectPreferenciaPagoDefecto.sql");
	private final String SQL_SELECT_CUENTA_CUSTODIO = loadResourceToString("cl/bcs/ordenes/sql/CrearSimulacionAsignacion_SelectCuentaCustodio.sql");
	
	private final String SQL_SELECT_INFORMACION_ORDENES = loadResourceToString("cl/bcs/ordenes/sql/CrearSimulacionAsignacion_SelectInformacionOrdenes.sql");
	private final String SQL_SELECT_INFORMACION_TRANSACCIONES = loadResourceToString("cl/bcs/ordenes/sql/CrearSimulacionAsignacion_SelectInformacionTransacciones.sql");
	private final String SQL_SELECT_INFORMACION_CARTERAS = loadResourceToString("cl/bcs/ordenes/sql/CrearSimulacionAsignacion_SelectInformacionCarteras.sql");
	private final String SQL_SELECT_INSTRUMENTO_BY_NEMOTECNICO = loadResourceToString("cl/bcs/ordenes/sql/CrearSimulacionAsignacion_SelectInstrumentoByNemotecnico.sql");
	
	//LIMPIEZA DE TABLAS GLOBALES
	private final String SQL_DELETE_TRANSACCIONES 	= "DELETE FROM \"BSCL.Orden.BD::ORD.CrearSimulacionAsignacion_Entrada_Transacciones\"";
	private final String SQL_DELETE_CARTERAS 		= "DELETE FROM \"BSCL.Orden.BD::ORD.CrearSimulacionAsignacion_Entrada_Carteras\"";
	private final String SQL_DELETE_ORDENES 		= "DELETE FROM \"BSCL.Orden.BD::ORD.CrearSimulacionAsignacion_Entrada_Ordenes\"";
	private final String SQL_DELETE_PORTAFOLIOS 	= "DELETE FROM \"BSCL.Orden.BD::ORD.CrearSimulacionAsignacion_Entrada_Portafolios\"";
	
	
	private ConstantesOrdenes _c = new ConstantesOrdenes();
	private static final String PRECIO_LIMITE = "L";
	private static final String ACCIONORDEN_COMPRA = "COMPRA";
	private static final String ACCIONORDEN_VENTA = "VENTA";
	private static final String PRECIO_ORDEN_MENOR_PRECIO_TRANSACCION = "PRECIO_ORDEN_MENOR_PRECIO_TRANSACCION";
	private static final String PRECIO_TRANSACCION_MENOR_PRECIO_ORDEN = "PRECIO_TRANSACCION_MENOR_PRECIO_ORDEN";
	private static final String FECHA_ORDEN_POSTERIOR_FECHA_TRANSACCION = "FECHA_ORDEN_POSTERIOR_FECHA_TRANSACCION";
	private static final String TRANSACCIONES_ORDENES_INSTRUMENTOS_DISTINTOS = "TRANSACCIONES_ORDENES_INSTRUMENTOS_DISTINTOS";
	private static final String MONEDA_NO_COINCIDE = "MONEDA_NO_COINCIDE";
	Long idAnterior;
	
	//----------------------------------------------
	// LOGICA
	@Trace
	public CrearSimulacionAsignacion_Salida logica(Connection conn,CrearSimulacionAsignacion_Entrada entrada) throws Exception {
		log.info("INICIO");
		CrearSimulacionAsignacion_Salida salida = new CrearSimulacionAsignacion_Salida();
		SQL _sql = new SQL();
		
		String localizacionSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.LocalizacionSistema");
		String idBovedaDefecto = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "ORDENES", "IdBovedaDefecto");
		
		// Validaciones
		if (entrada.p_Transacciones.size() == 0) {
			String codigo = "NO_SE_ENVIARON_TRANSACCIONES";
			String desc = Diccionario.getMensaje(conn, entrada.p_IdEmpresa,codigo).msg();
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,codigo,desc));
			return salida;
		}
		
		if (entrada.p_Carteras.size() == 0 && (entrada.p_IdTipoAsignacion.equalsIgnoreCase("ASIGNACION_MANUAL_CARTERAS") || entrada.p_IdTipoAsignacion.equalsIgnoreCase("ASIGNACION_CARTERAS"))) {
			String codigo = "NO_SE_ENVIARON_CARTERAS";
			String desc = Diccionario.getMensaje(conn, entrada.p_IdEmpresa,codigo).msg();
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,codigo,desc));
			return salida;
		}
		
		if (entrada.p_Ordenes.size() == 0 && 
				(entrada.p_IdTipoAsignacion.equalsIgnoreCase("ASIGNACION_CRONOLOGICA") || entrada.p_IdTipoAsignacion.equalsIgnoreCase("ASIGNACION_PROPORCIONAL") || entrada.p_IdTipoAsignacion.equalsIgnoreCase("ASIGNACION_MANUAL_ORDENES"))) {
			String codigo = "NO_SE_ENVIARON_ORDENES";
			String desc = Diccionario.getMensaje(conn, entrada.p_IdEmpresa,codigo).msg();
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,codigo,desc));
			return salida;
		}
		
		if (entrada.p_Portafolios.size() == 0 && 
				(entrada.p_IdTipoAsignacion.equalsIgnoreCase("ASIGNACION_POSTRERA") || entrada.p_IdTipoAsignacion.equalsIgnoreCase("ASIGNACION_MANUAL_CLIENTES") || entrada.p_IdTipoAsignacion.equalsIgnoreCase("ASIGNACION_PRECIO_PROMEDIO"))) {
			String codigo = "NO_SE_ENVIARON_PORTAFOLIOS";
			String desc = Diccionario.getMensaje(conn, entrada.p_IdEmpresa,codigo).msg();
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,codigo,desc));
			return salida;
		}

		_sql.batch(conn, SQL_INSERT_TRANSACCIONES, entrada.p_Transacciones, Transaccion_Entrada.class);
		_sql.batch(conn, SQL_INSERT_CARTERAS, entrada.p_Carteras, Cartera_Entrada.class);
		_sql.batch(conn, SQL_INSERT_ORDENES, entrada.p_Ordenes, Orden_Entrada.class);
		_sql.batch(conn, SQL_INSERT_PORTAFOLIOS, entrada.p_Portafolios, Portafolio_Entrada.class);
		
		if(entrada.p_IdTipoAsignacion!=null) {
			if(entrada.p_IdTipoAsignacion.equalsIgnoreCase("ASIGNACION_CRONOLOGICA")) {
				CrearSimulacionAsignacionCronologica servicioSimulacionAsignacionCronologica = new CrearSimulacionAsignacionCronologica();
				
				EntradaCrearSimulacionAsignacionCronologica entradaAsignacionCronologica = new EntradaCrearSimulacionAsignacionCronologica();
				entradaAsignacionCronologica.p_IdEmpresa = entrada.p_IdEmpresa;
				entradaAsignacionCronologica.p_IdTipo = entrada.p_IdTipo;
				entradaAsignacionCronologica.p_Origen = entrada.p_Origen;
				
				SalidaCrearSimulacionAsignacionCronologica salidaAsignacionCronologica = servicioSimulacionAsignacionCronologica.logica(conn, entradaAsignacionCronologica);
				salida.P_OUT_SIMASIGNACION = salidaAsignacionCronologica.p_out_SimAsignacion;
			
			}else if(entrada.p_IdTipoAsignacion.equalsIgnoreCase("ASIGNACION_PROPORCIONAL")) {
				CrearSimulacionAsignacionPropCrono servicio = new CrearSimulacionAsignacionPropCrono();
				
				EntradaCrearSimulacionAsignacionPropCrono entradaProporcional = new EntradaCrearSimulacionAsignacionPropCrono();
				entradaProporcional.p_IdEmpresa = entrada.p_IdEmpresa;
				entradaProporcional.p_IdTipo = entrada.p_IdTipo;
				entradaProporcional.p_Origen = entrada.p_Origen;
				
				SalidaCrearSimulacionAsignacionPropCrono salidaProporcional = servicio.logica(conn, entradaProporcional);
				salida.P_OUT_SIMASIGNACION = salidaProporcional.p_out_SimAsignacion;
			
				
			}else if(entrada.p_IdTipoAsignacion.equalsIgnoreCase("ASIGNACION_POSTRERA")) {
				CrearSimulacionAsignacionPostrera servicioSimulacionAsignacionPostrera = new CrearSimulacionAsignacionPostrera();
				
				EntradaCrearSimulacionAsignacionPostrera entradaAsignacionPostrera = new EntradaCrearSimulacionAsignacionPostrera();
				entradaAsignacionPostrera.p_IdEmpresa = entrada.p_IdEmpresa;
				entradaAsignacionPostrera.p_IdTipo = entrada.p_IdTipo;
				entradaAsignacionPostrera.p_Origen = entrada.p_Origen;
				
				SalidaCrearSimulacionAsignacionPostrera salidaAsignacionPostrera = servicioSimulacionAsignacionPostrera.logica(conn, entradaAsignacionPostrera);
				salida.P_OUT_SIMASIGNACION = salidaAsignacionPostrera.p_out_SimAsignacion;
				
			}else {
				//fallback
				HashMap<String,Object> params1 = new HashMap<>();
				params1.put("p_IdEmpresa", entrada.p_IdEmpresa);
				params1.put("p_IdUsuario", entrada.p_IdUsuario);
				params1.put("p_IdModulo", entrada.p_IdModulo);
				params1.put("p_IdTipoAsignacion", entrada.p_IdTipoAsignacion);
				params1.put("p_IdTipo", entrada.p_IdTipo);
				params1.put("p_Origen", entrada.p_Origen);
				HashMap<String,Class> params2 = new HashMap<>();
				HashMap<String,Object> rpt = _sql.call(conn, SQL_CALL_SIMULACION_ASIGNACION, params1, params2);
				
				List<Simulacion_Salida> simulacion = _sql.query(conn, SQL_SELECT_SALIDA, new HashMap<>(), Simulacion_Salida.class);
				salida.P_OUT_SIMASIGNACION = simulacion;
			}
		}else {
			String codigo = "NO_SE_ENVIO_TIPO_ASIGNACION";
			String desc = Diccionario.getMensaje(conn, entrada.p_IdEmpresa,codigo).msg();
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,codigo,desc));
			return salida;
		}

		
		HashMap<Long,Object> infoOrdenes = new HashMap<Long,Object>(); 
		if (entrada.p_Ordenes.size() > 0) {
			infoOrdenes = obtenerListadoOrdenes(_sql, conn, entrada.p_Ordenes);
		}
		HashMap<Long,Object> infoTransacciones = obtenerListadoTransacciones(_sql, conn, entrada.p_Transacciones);
		List<Long> foliosTransaccion = entrada.p_Transacciones.stream().map(linea -> linea.IdTransaccion).distinct().collect(Collectors.toList());
		
		if (salida.P_OUT_SIMASIGNACION.size() == 0) {
			//VALIDACIONES
			if (entrada.p_Transacciones.size() > 0 && entrada.p_Ordenes.size() > 0) {
				final String descPrecioOrdenMenorPrecioTrans = Diccionario.getMensaje(conn, entrada.p_IdEmpresa,PRECIO_ORDEN_MENOR_PRECIO_TRANSACCION).msg();
				final String descPrecioTransMenorPrecioOrden = Diccionario.getMensaje(conn, entrada.p_IdEmpresa,PRECIO_TRANSACCION_MENOR_PRECIO_ORDEN).msg();
				final String descFechaOrdenPosteriorFechaTrans = Diccionario.getMensaje(conn, entrada.p_IdEmpresa,FECHA_ORDEN_POSTERIOR_FECHA_TRANSACCION).msg();
				final String descInstrumentosDistintos = Diccionario.getMensaje(conn, entrada.p_IdEmpresa,TRANSACCIONES_ORDENES_INSTRUMENTOS_DISTINTOS).msg();
				final String descMonedasDistintas = Diccionario.getMensaje(conn, entrada.p_IdEmpresa,MONEDA_NO_COINCIDE).msg();

				if ((entrada.p_Transacciones.size() >= 1 && entrada.p_Ordenes.size() == 1) || (entrada.p_Transacciones.size() == 1 && entrada.p_Ordenes.size() >= 1)) {
					

					// se sacaron HashMap de ordenes y transacciones
					for (int indiceOrdenes = 0; indiceOrdenes < infoOrdenes.size(); indiceOrdenes++) {
						for (int indiceTransacciones = 0; indiceTransacciones < infoTransacciones.size(); indiceTransacciones++) {
							Orden orden = (Orden) infoOrdenes.get(entrada.p_Ordenes.get(indiceOrdenes).IdOrden);
							Transaccion transaccion = (Transaccion) infoTransacciones.get(entrada.p_Transacciones.get(indiceTransacciones).IdTransaccion);
							
							if (orden.IdClasificacionTipoInstrumento.equals(transaccion.IdClasificacionTipoInstrumento) && orden.IdLibroOrdenes.equals(transaccion.IdLibroOrdenes) && orden.IdTipoInstrumento.equals(transaccion.IdTipoInstrumento) &&
									orden.IdInstrumento.equals(transaccion.IdInstrumento) && (orden.IdBolsa.equals(transaccion.IdBolsa) || "0".equals(orden.IdBolsa)) && orden.IdTipoOperacion.equals(transaccion.IdTipoOperacion) &&
									orden.IdCondicionDeLiquidacion.equals(transaccion.IdCondicionDeLiquidacion) && orden.EsVentaCorta.equals(transaccion.EsVentaCorta)) {
								if (transaccion.FechaTransaccion.after(orden.FechaOrden)) {
									if (PRECIO_LIMITE.equals(orden.IdTipoPrecio)) {
										if (ACCIONORDEN_COMPRA.equals(orden.AccionOrden)) {
											if (orden.PrecioOrden.compareTo(transaccion.PrecioTransaccion) < 0) {
												salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,PRECIO_ORDEN_MENOR_PRECIO_TRANSACCION,descPrecioOrdenMenorPrecioTrans));
											}
										} else if (ACCIONORDEN_VENTA.equals(orden.AccionOrden)) {
											if (transaccion.PrecioTransaccion.compareTo(orden.PrecioOrden) < 0) {
												salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,PRECIO_TRANSACCION_MENOR_PRECIO_ORDEN,descPrecioTransMenorPrecioOrden));
											}
										}
									}
								} else {
									salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,FECHA_ORDEN_POSTERIOR_FECHA_TRANSACCION,descFechaOrdenPosteriorFechaTrans));
								}
								
							} else if (!orden.Nemotecnico.equals(transaccion.Nemotecnico)) {
								salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,TRANSACCIONES_ORDENES_INSTRUMENTOS_DISTINTOS,descInstrumentosDistintos));
							} if (!orden.IdMonedaOrden.equals(transaccion.IdMoneda)) {
								salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,MONEDA_NO_COINCIDE,descMonedasDistintas));
							}
						}
					}
				}
			}
			String codigo = "NO_SE_GENERARON_ASIGNACIONES";
			String desc = Diccionario.getMensaje(conn, entrada.p_IdEmpresa,codigo).msg();
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ADVERTENCIA,codigo,desc));
			salida.P_OUT_ADVERTENCIASCONTADOR += 1;
			return salida;
		}
		
		HashMap<Long,Long> cantCobros = new HashMap<>();		
		//CARGA DEFAULTS PARA POSTRERAS
		{
			HashMap<Long,Object> indicadoresPortafolios = new HashMap<>();
			List<PreferenciaPagoDefectoPortafolio> preferenciaPagoDefecto = new ArrayList<>();
			List<Long> idsPortafolio = new ArrayList<>(); 
			for (Simulacion_Salida sim : salida.P_OUT_SIMASIGNACION) {
				if (!idsPortafolio.contains(sim.IdPortafolio)) {
					idsPortafolio.add(sim.IdPortafolio);
				}
			}
			
			if (idsPortafolio.size() > 0) {
				HashMap<String,Object> params3 = new HashMap<>();
				params3.put("idsPortafolio", idsPortafolio);
				List<IndicadoresPortafolio> _indicadoresPortafolios = _sql.query(conn, SQL_SELECT_INDICADORES_PORTAFOLIO, params3, IndicadoresPortafolio.class);
				indicadoresPortafolios = Utils.List2Hash(_indicadoresPortafolios, "IdPortafolio");
				preferenciaPagoDefecto = _sql.query(conn, SQL_SELECT_PREFERENCIA_PAGO_DEFECTO, params3, PreferenciaPagoDefectoPortafolio.class);
				List<CuentaCustodioPortafolio> cuentasCustodio = _sql.query(conn, SQL_SELECT_CUENTA_CUSTODIO, params3, CuentaCustodioPortafolio.class);
				HashMap<Long,Long> countCuentaCustodio = new HashMap<>();
				
				for (Simulacion_Salida sim : salida.P_OUT_SIMASIGNACION) {
					if (localizacionSistema.equalsIgnoreCase("CL")) {
						sim.IdBoveda = cuentasCustodio.stream().filter(cus -> cus.IdEstado.equalsIgnoreCase("ACT") ).findFirst().map(cus -> cus.IdBoveda).orElse(idBovedaDefecto);	
					}else {
						for (CuentaCustodioPortafolio cus : cuentasCustodio) {
							
							if (cus.IdPortafolio.equals(sim.IdPortafolio)) {
								if ((cus.IdModeloCustodia.equalsIgnoreCase("LOCAL") && sim.EsExtranjera.equalsIgnoreCase("N")) 
										|| (cus.IdModeloCustodia.equalsIgnoreCase("EXTERIOR") && sim.EsExtranjera.equalsIgnoreCase("S"))) {
									
									Long i = countCuentaCustodio.get(sim.IdInterno);
									if (i == null) {
										sim.IdCuentaCustodioPortafolio = cus.IdCuentaCustodioPortafolio;
										sim.CuentaCustodioPortafolio = cus.CuentaCustodioPortafolio;
										sim.IdBoveda = cus.IdBoveda;
										i = 0L;
									}
									/*else {
										sim.IdCuentaCustodioPortafolio = null;
										sim.IdBoveda = null;
									}*/
									i++;
									countCuentaCustodio.put(sim.IdInterno, i);
									
								}	
							}
						}
					}
					
					sim.Glosa = "";
					sim.TasaVentaCorta = BigDecimal.ZERO;
					sim.PlazoVentaCorta = 0L;
					
					IndicadoresPortafolio p = (IndicadoresPortafolio) indicadoresPortafolios.get(sim.IdPortafolio);
					if (p != null) {
						
						if (p.OperaCustodiaRV.equalsIgnoreCase(_c.SI) && sim.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RV)) {
							sim.IdFormaCustodia = "CUS";
						}
						else if (p.OperaCustodiaRV.equalsIgnoreCase(_c.SI) && sim.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_DV)) {
							sim.IdFormaCustodia = "CUS";
						}
						else if (p.OperaCustodiaRFIF.equalsIgnoreCase(_c.SI) && sim.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RF)) {
							sim.IdFormaCustodia = "CUS";
						}
						else if (p.OperaCustodiaRFIF.equalsIgnoreCase(_c.SI) && sim.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_IF)) {
							sim.IdFormaCustodia = "CUS";
						}
						else {
							sim.IdFormaCustodia = "NOM";
						}
						
						if (p.OperaCuentaInversion.equalsIgnoreCase(_c.SI)) {
							sim.PagoCobroCuentaInversion = _c.SI;
						}
						else {
							sim.PagoCobroCuentaInversion = _c.NO;
							
							//SI NO PAGA CON CUENTA INVERSION, CARGAMOS PREFERENCIA PAGO POR DEFECTO
							for (PreferenciaPagoDefectoPortafolio pref : preferenciaPagoDefecto) {
								if (pref.IdPortafolio.equals(sim.IdPortafolio) && 
										pref.IdMoneda.equalsIgnoreCase(sim.IdMonedaLiquidacionOrden) &&
										pref.IdTipoOperacion.equalsIgnoreCase(sim.IdTipoOperacion)) {
									sim.IdPreferenciaPagoPortafolio = pref.IdPreferenciaPagoPortafolio;
									break;
								}		
							}
						}
						
						if(p.CodigoIfrs!=null && !p.CodigoIfrs.equals("")) {
							sim.CodigoIFRS = p.CodigoIfrs;
						}
					}
					
						
				}
			}
		}
		
		
		
		//SI SON ORDENES, TENEMOS QUE REPASAR CON LA MONEDA DE LIQUIDACION DE LA ORDEN, FORMA DE CUSTODIA DE LA ORDEN, BOVEDA, CORRESPONSAL, TASA Y PLAZO VENTA CORTA,
		//PAGO COBRO CUENTA INVERSION , FORMA DE PAGO, TIPO DE CAMBIO
		{
			List<Long> idsOrdenes = new ArrayList<>();
			for (Simulacion_Salida sim : salida.P_OUT_SIMASIGNACION) {
				if (sim.IdOrden > 0 && !idsOrdenes.contains(sim.IdOrden)) {
					idsOrdenes.add(sim.IdOrden);
				}
			}
			
			if (idsOrdenes.size() > 0) {
				HashMap<String,Object> params3 = new HashMap<>();
				params3.put("idsOrdenes", idsOrdenes);
				List<InformacionOrden> _infoOrden = _sql.query(conn, SQL_SELECT_ORDENES, params3, InformacionOrden.class);
				HashMap<Long,Object> infoOrden = Utils.List2Hash(_infoOrden, "IdOrden");
				
				for (Simulacion_Salida sim : salida.P_OUT_SIMASIGNACION) {
					if (sim.IdOrden > 0) {
						InformacionOrden info = (InformacionOrden) infoOrden.get(sim.IdOrden);
						
						sim.IdMonedaLiquidacionOrden = info.IdMonedaLiquidacionOrden;
						sim.IdFormaCustodia = info.IdFormaCustodia;
						sim.IdBoveda = info.IdBoveda;
						sim.IdCuentaCustodioPortafolio = info.IdCuentaCustodioPortafolio;
						sim.IdCorredoraCorresponsal = info.IdCorredoraCorresponsal;
						sim.TasaVentaCorta = info.TasaVentaCorta;
						sim.PlazoVentaCorta = info.PlazoVentaCorta;
						sim.TipoCambio = info.TipoCambio;
						
						if (info.IdMedioDePago != null && info.IdMedioDePago.equalsIgnoreCase(_c.MEDIO_PAGO_CUENTA_INVERSION)) {
							sim.PagoCobroCuentaInversion = "S";
							sim.IdPreferenciaPagoPortafolio = null;
						}
						else {
							sim.PagoCobroCuentaInversion = "N";
							sim.IdPreferenciaPagoPortafolio = info.IdPreferenciaPagoPortafolio;
						}
					}
				}
			}
		}

		//SI SON CARTERAS TENEMOS QUE REPASAR LA BOVEDA
		{
			HashMap<Long,Object> infoCarteras = new HashMap<Long,Object>(); 
			if (entrada.p_Carteras.size() > 0) {
				infoCarteras = obtenerListadoCarteras(_sql, conn, entrada.p_Carteras);
			}
					
			if (infoCarteras.size() > 0) {
				for (Simulacion_Salida sim : salida.P_OUT_SIMASIGNACION) {
					if (sim.FolioCartera > 0) {
						Cartera info = (Cartera) infoCarteras.get(sim.FolioCartera);
						sim.IdBoveda = info.IdBoveda;
					}
				}
			}
		}
		if (localizacionSistema.equalsIgnoreCase("CL")) {
			
			ConsultarCobrosOrdenes.ConsultarCobrosOrdenes_Entrada entradaConsultarCobrosOrdenes = new ConsultarCobrosOrdenes.ConsultarCobrosOrdenes_Entrada();
			entradaConsultarCobrosOrdenes.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaConsultarCobrosOrdenes.p_IdModulo = entrada.p_IdModulo;
			entradaConsultarCobrosOrdenes.p_IdUsuario = entrada.p_IdUsuario;
			
			//COBROS
			for (Simulacion_Salida sim : salida.P_OUT_SIMASIGNACION) {
				InformacionOperacion info = new InformacionOperacion();
				info.IdBolsa = sim.IdBolsa;
				info.IdCorredoraCorresponsal = sim.IdCorredoraCorresponsal;
				info.IdInterno = sim.IdInterno;
				info.IdPortafolio = sim.IdPortafolio;
				info.IdTipoCanalIngreso = sim.IdTipoCanalIngreso;
				info.IdTipoOperacion = sim.IdTipoOperacion;
				info.IdTipoProducto = sim.IdTipoProducto;
				entradaConsultarCobrosOrdenes.p_Operaciones.add(info);
			}
			
			ConsultarCobrosOrdenes serviceConsultarCobrosOrdenes = new ConsultarCobrosOrdenes();
			ConsultarCobrosOrdenes_Salida salidaConsultarCobrosOrdenes = serviceConsultarCobrosOrdenes.logica(conn, entradaConsultarCobrosOrdenes);
			
			long IdInternoCobro = 0L;
			//agrupar cobros
			Map<Long, List<LineaMatrizOperacional>> cobrosAgrupados = salidaConsultarCobrosOrdenes.p_out_MatrizOperacional.stream().collect(Collectors.groupingBy(cobro -> cobro.IdInterno));
			for (Simulacion_Salida sim : salida.P_OUT_SIMASIGNACION) {
				cantCobros.put(sim.IdInterno, 0L);
				List<LineaMatrizOperacional> cobros = cobrosAgrupados.getOrDefault(sim.IdInterno, new ArrayList<>());
				for (LineaMatrizOperacional linea : cobros) {
					//if (sim.IdInterno.equals(linea.IdInterno)) {
						Cobro_Salida cobro = new Cobro_Salida();
						Utils.copyPojo(linea, cobro);
						cobro.IdInternoCobro = IdInternoCobro++;
						salida.P_OUT_COBROSASIGNACION.add(cobro);
						Long cant =  cantCobros.get(sim.IdInterno);
						if (cant == null)
							cant = 0L;
						cantCobros.put(sim.IdInterno, cant +1);
					//}
				}

			}
		}
		
		
		
		//ESTO ES SOLO PARA CUANDO ES SIMULTANEA COMPRA Y ASIGNACION MANUAL 
		
		BigDecimal cantidadCalculada =BigDecimal.ZERO;
		BigDecimal sumaVariables = BigDecimal.ZERO;
		BigDecimal sumaFijos = BigDecimal.ZERO;
		BigDecimal sumaImpuestos = BigDecimal.ZERO;
		BigDecimal sumaCobros = BigDecimal.ZERO;
		BigDecimal montoAsignacionCalculado = BigDecimal.ZERO;
		
		List<Simulacion_Salida> ListAsignacion =  new ArrayList<Simulacion_Salida>();
		List<Simulacion_Salida> contenido = new ArrayList<Simulacion_Salida>();
		
		for(Simulacion_Salida contenidoAsig :salida.P_OUT_SIMASIGNACION ){
			if(foliosTransaccion.size() == 1) {
				if(foliosTransaccion.get(0).compareTo(contenidoAsig.IdTransaccion)==0L) {
					contenido.add(contenidoAsig);
					salida.P_OUT_SIMASIGNACION  = contenido;
				}
			}else {
				contenido.add(contenidoAsig);
			}
		
		}
		for(Simulacion_Salida simAsig :contenido ){
			Transaccion transaccion =  (Transaccion) infoTransacciones.get(simAsig.IdTransaccion);

			
			
			Orden orden = (Orden) infoOrdenes.get(simAsig.IdOrden);
			
			if(simAsig.IdTipoOperacion.equalsIgnoreCase(_c.TIPO_OPERACION_COMSIM) && orden != null){
				
				for (Cobro_Salida cob : salida.P_OUT_COBROSASIGNACION){
					if (cob.CodigoCobroClasificacion.equalsIgnoreCase("IMPUESTO")) {
						sumaImpuestos = sumaImpuestos.add(cob.ValorCobro.divide(new BigDecimal(100)));
					}
					else if (cob.FormaCobro.equalsIgnoreCase("V")) {
						sumaVariables = sumaVariables.add(cob.ValorCobro.divide(new BigDecimal(100)));
					}
					else if (cob.FormaCobro.equalsIgnoreCase("F")) {
						sumaFijos = sumaVariables.add(cob.ValorCobro.divide(new BigDecimal(100)));
					}
				}
				
				sumaCobros = (sumaVariables.add(sumaFijos)).multiply(BigDecimal.ONE.add(sumaImpuestos));
				
				
				if (transaccion.MontoTransaccion.compareTo(orden.MontoOrden)==1)
				{
					 cantidadCalculada = (orden.MontoOrden.subtract(sumaCobros)).divide(transaccion.PrecioTransaccion,20,RoundingMode.HALF_UP);					   
					 montoAsignacionCalculado = (transaccion.MontoTransaccion.multiply(cantidadCalculada)).divide(transaccion.CantidadTransaccion);  // MontoAsignacion = ((MontoTransaccion x CantidadAsignada)/CantidadTransaccion)  --> Regla de 3
				}
				else
				{
					 cantidadCalculada = transaccion.CantidadTransaccion;
					 montoAsignacionCalculado = transaccion.MontoTransaccion;
				}
				
				simAsig.CantidadAsignacion = cantidadCalculada;
				simAsig.MontoAsignacion    = montoAsignacionCalculado;
				//agregar mapeo de campos nuevos a la llamada por sp.
				simAsig.CantidadAsignadaSimulacion = (!simAsig.IdClasificacionTipoInstrumento.equals("IF") ? simAsig.CantidadAsignacion.setScale(0, BigDecimal.ROUND_DOWN) : BigDecimal.ZERO);//agregar round
				simAsig.CantidadTransaccionSimulacion = (!simAsig.IdClasificacionTipoInstrumento.equals("IF") ? simAsig.CantidadTransaccion.setScale(0, BigDecimal.ROUND_DOWN) : BigDecimal.ZERO);
				
				ListAsignacion.add(simAsig);
			}else{
				ListAsignacion.add(simAsig);
			}
			
			/* Derivados */
			if(transaccion.IdClasificacionTipoInstrumento.equals("DV")) {
				HashMap<String,Object> params = new HashMap<>();
				params.put("p_Nemotecnico", transaccion.Nemotecnico);
				List<Instrumento> instrumentos = _sql.query(conn, SQL_SELECT_INSTRUMENTO_BY_NEMOTECNICO, params, Instrumento.class);
				System.out.println(instrumentos);
				simAsig.TipoOpcion = instrumentos.get(0).TipoOpcion;
				simAsig.EstiloOpcion = instrumentos.get(0).EstiloOpcion;
				simAsig.ModalidadEjercicio = instrumentos.get(0).ModalidadEjercicio;
				simAsig.ModalidadLiquidacion = instrumentos.get(0).ModalidadLiquidacion;
				
				
			}
			
		}
		salida.P_OUT_SIMASIGNACION=ListAsignacion;		
		if (localizacionSistema.equalsIgnoreCase("PE")) {
			ConsultarCobrosOrdenesPe serviceConsultarCobrosOrdenes = new ConsultarCobrosOrdenesPe();
			ConsultarCobrosOrdenesPe_Entrada entradaConsultarCobrosOrdenes = new ConsultarCobrosOrdenesPe_Entrada();
			entradaConsultarCobrosOrdenes.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaConsultarCobrosOrdenes.p_IdModulo = entrada.p_IdModulo;
			entradaConsultarCobrosOrdenes.p_IdUsuario = entrada.p_IdUsuario;
			
			for (Simulacion_Salida sim : salida.P_OUT_SIMASIGNACION) {
				InformacionOrden_Entrada op = new InformacionOrden_Entrada();
				op.EsAPlazo = "N";
				op.IdBolsa = sim.IdBolsa;
				op.IdCorredoraCorresponsal = sim.IdCorredoraCorresponsal;
				op.IdEmisor = "";
				op.IdInstrumento = sim.IdInstrumento;
				op.IdInterno = sim.IdInterno;
				op.IdPortafolio = sim.IdPortafolio;
				op.IdTipoCanalIngreso = sim.IdTipoCanalIngreso;
				op.IdTipoOperacion = sim.IdTipoOperacion;
				op.IdTipoProducto = sim.IdTipoProducto;
				entradaConsultarCobrosOrdenes.p_Ordenes.add(op );
			}
			
			long IdInternoCobro = 0L;
			
			ConsultarCobrosOrdenesPe_Salida salidaConsultarCobrosOrdenes = serviceConsultarCobrosOrdenes.logica(conn, entradaConsultarCobrosOrdenes);
			for (Simulacion_Salida sim : salida.P_OUT_SIMASIGNACION) {				
				cantCobros.put(sim.IdInterno, 0L);
				for (DefinicionCobro_Salida linea : salidaConsultarCobrosOrdenes.p_out_Cobros) {
					if (sim.IdInterno.equals(linea.IdInterno)) {
						Cobro_Salida cobro = new Cobro_Salida();
						Utils.copyPojo(linea, cobro);
						cobro.IdCobro = linea.CodigoDefinicionCobro;
						cobro.IdInternoCobro = IdInternoCobro++;
						salida.P_OUT_COBROSASIGNACION.add(cobro);
						Long cant =  cantCobros.get(sim.IdInterno);
						if (cant == null)
							cant = 0L;
						cantCobros.put(sim.IdInterno, cant +1);
					}
				}			
			}
		}
		
		for (Simulacion_Salida sim : salida.P_OUT_SIMASIGNACION) {
			if (cantCobros.get(sim.IdInterno).equals(new Long(0))) {
				String codigo = "PORTAFOLIO_SIN_COBROS_CONFIGURADOS";
				String desc = Diccionario.getMensaje(conn, entrada.p_IdEmpresa,codigo).msg();
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,codigo,desc));
			}
		}

		salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);

		
		// Limpia tablas temporales
		SQL.exec(conn, SQL_DELETE_TRANSACCIONES);
		SQL.exec(conn, SQL_DELETE_CARTERAS);
		SQL.exec(conn, SQL_DELETE_ORDENES);
		SQL.exec(conn, SQL_DELETE_PORTAFOLIOS);

		
		log.info("TERMINO");
		return salida;
	}

	@Trace
	private HashMap<Long,Object> obtenerListadoOrdenes(SQL _sql, Connection conn,List<Orden_Entrada> ordenes) throws Exception {
		List<Long> idsOrdenes = new ArrayList<>();
		for (Orden_Entrada orden : ordenes) {
			idsOrdenes.add(orden.IdOrden);
		}
		HashMap<String,Object> parametrosSelectOrdenes = new HashMap<>();
		parametrosSelectOrdenes.put("idsOrdenes", idsOrdenes);
		List<Orden> infoOrdenes = _sql.query(conn, SQL_SELECT_INFORMACION_ORDENES, parametrosSelectOrdenes, Orden.class);
		return Utils.List2Hash(infoOrdenes, "IdOrden");
	}
	
	@Trace
	private HashMap<Long,Object> obtenerListadoTransacciones(SQL _sql, Connection conn,List<Transaccion_Entrada> transacciones) throws Exception {
		List<Long> idsTransacciones = new ArrayList<>();
		for(Transaccion_Entrada transaccion : transacciones) {
			idsTransacciones.add(transaccion.IdTransaccion);
		}
		HashMap<String,Object> parametrosSelectTransacciones = new HashMap<>();
		parametrosSelectTransacciones.put("idsTransacciones", idsTransacciones);
		List<Transaccion> infoTransacciones = _sql.query(conn, SQL_SELECT_INFORMACION_TRANSACCIONES, parametrosSelectTransacciones, Transaccion.class);
		return Utils.List2Hash(infoTransacciones, "IdTransaccion");
	}

	@Trace
	private HashMap<Long,Object> obtenerListadoCarteras(SQL _sql, Connection conn,List<Cartera_Entrada> carteras) throws Exception {
		List<Long> foliosCartera = new ArrayList<>();
		for(Cartera_Entrada cartera : carteras) {
			foliosCartera.add(cartera.FolioCartera);
		}
		HashMap<String,Object> parametrosSelectCarteras = new HashMap<>();
		parametrosSelectCarteras.put("foliosCartera", foliosCartera);
		List<Cartera> infoCarteras = _sql.query(conn, SQL_SELECT_INFORMACION_CARTERAS, parametrosSelectCarteras, Cartera.class);
		return Utils.List2Hash(infoCarteras, "FolioCartera");
	}

	public static class CuentaCustodioPortafolio {
		public String IdBoveda;
		public Long IdPortafolio;
		public String IdModeloCustodia;
		public Long   IdCuentaCustodioPortafolio;
		public String CuentaCustodioPortafolio;
		public String IdEstadoAutorizacion;
		public String IdEstado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public static class Instrumento{
		public Long IdInstrumento;				
		public String TipoOpcion;
		public String IdEstiloOpcion;
		public String EstiloOpcion;
		public String IdModalidadLiquidacion;
		public String ModalidadLiquidacion;
		public String IdModalidadEjercicio;
		public String ModalidadEjercicio;
		public BigDecimal Precio;
		public Date FechaVencimiento;
		public Long LotePadron;
		
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	
	public static class PreferenciaPagoDefectoPortafolio {
		public Long IdPortafolio;
		public Long IdPreferenciaPagoPortafolio;
		public String IdMoneda;
		public String IdTipoOperacion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public static class InformacionOrden {
		public Long IdOrden;
		public String IdBoveda;
		public Long IdCuentaCustodioPortafolio;
		public String IdMonedaLiquidacionOrden;
		public String IdFormaCustodia;
		public Long IdCorredoraCorresponsal;
		public BigDecimal TasaVentaCorta;
		public Long PlazoVentaCorta;
		public BigDecimal TipoCambio;
		public String IdMedioDePago;
		public Long IdPreferenciaPagoPortafolio;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Orden {
		public Long IdOrden;
		public BigDecimal MontoOrden;
		public java.sql.Timestamp FechaOrden;
		public BigDecimal PrecioOrden;
		public BigDecimal CantidadOrden;
		public String Nemotecnico;
		public String IdTipoOperacion;
		public String IdTipoPrecio;
		public String IdClasificacionTipoInstrumento;
		public Long IdLibroOrdenes;
		public String IdTipoInstrumento;
		public Long IdInstrumento;
		public String IdBolsa;
		public String IdCondicionDeLiquidacion;
		public String EsVentaCorta;
		public String AccionOrden;
		public String IdMonedaOrden;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Transaccion {
		public Long IdTransaccion;
		public BigDecimal MontoTransaccion;
		public java.sql.Timestamp FechaTransaccion;
		public BigDecimal PrecioTransaccion;
		public BigDecimal CantidadTransaccion;
		public String Nemotecnico;
		public String IdTipoOperacion;
		public String IdClasificacionTipoInstrumento;
		public Long IdLibroOrdenes;
		public String IdTipoInstrumento;
		public Long IdInstrumento;
		public String IdBolsa;
		public String IdCondicionDeLiquidacion;
		public String EsVentaCorta;
		public String AccionOrden;
		public String IdMoneda;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class Cartera {
		public Long FolioCartera;
		public String IdBoveda;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Cobro_Salida {
		public Long IdInterno;
		public Long IdInternoCobro;
		public Long IdPortafolio;
		public String IdBolsa;
		public String IdTipoProducto;
		public String IdTipoOperacion;
		public String IdTipoCanalIngreso;
		public String IdCobro;
		public BigDecimal ValorCobro;
		public String FormaCobro;
		public BigDecimal ValorTopeMinimoOrden;
		public String IdMoneda;
		public String CodigoCobroClasificacion;
		public String PermiteCambiarValor;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Simulacion_Salida {
		public BigDecimal 			CantidadAsignacion;
		public BigDecimal 			CantidadTransaccion;
		public String				CodigoIFRS;
		public String 				CodigoPortafolio;
		public BigDecimal			CorteMinimo;
		
		public String 				DocumentoIdentidad;
		
		public String     			EsFungible;
		public String				EsExtranjera;
		public String 				EstiloOpcion;
		
		public java.sql.Timestamp 	FechaIngresoRegistro;
		public java.sql.Date 		FechaTransaccion;
		public java.sql.Timestamp 	FechaSimulacion;
		public Long 				FolioCartera;
		public String 				FolioTransaccion;
		
		public String 				Glosa;
		
		public String 				IdBolsa;
		public String 				IdBoveda;
		public String 				IdClasificacionTipoInstrumento;
		public Long 				IdCliente;
		public Long 				IdCorredoraCorresponsal;
		public Long 				IdCuentaCustodioPortafolio;
		public String				CuentaCustodioPortafolio;
		public String 				IdFormaCustodia;
		public Long 				IdInterno;
		public Long 				IdInstrumento;
		public Long 				IdLibroOrdenes;
		public String 				IdMonedaInstrumento;
		public String 				IdMonedaLiquidacionOrden;
		public Long 				IdMovimiento;
		public Long 				IdOrden;
		public Long 				IdPreferenciaPagoPortafolio;
		public Long 				IdPortafolio;
		public String 				IdTipoInstrumento;
		public Long 				IdTransaccion;
		public String 				IdTipoCanalIngreso;
		public String 				IdTipoOperacion;
		public String 				IdTipoProducto;
		public String 				Instrumento;
		public String 				InstrumentoNegociacionTran;

		public BigDecimal 			MontoAsignacion;
		public BigDecimal 			MontoTransaccion;
		public String 				ModalidadEjercicio;
		public String 				ModalidadLiquidacion;
		
		public String 				NombreCompleto;
		
		public String 				PagoCobroCuentaInversion;
		public Long 				PlazoVentaCorta;
		public BigDecimal 			PorcentajeAsignacion;
		public BigDecimal 			PrecioSimulacion;
		public BigDecimal 			PrecioStrike;
		
		public BigDecimal 			TasaVentaCorta;
		public Long 				Tamano;
		public BigDecimal 			TipoCambio;
		public String 				TipoOpcion;
		
		public BigDecimal 			ValorRescateAsignacion;
		
		//Agregados		
		public BigDecimal			CantidadAsignadaSimulacion;
		public BigDecimal			ValorRescateMontoTransaccion;
		public BigDecimal 			CantidadTransaccionSimulacion;
		
		public BigDecimal           IdComprobante;
		
		public Double               DesviacionEstandar = 0.0;  //Se usa para prorrateo de puchos
		
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public BigDecimal getCantidadAsignacion() { 
	        return CantidadAsignacion; 
	    }
		
	}
	
	public static class Transaccion_Entrada {
		public Long IdMovimiento;
		public Long IdTransaccion;
		public BigDecimal Cantidad;
		public BigDecimal Monto;
		public BigDecimal ValorRescate;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Orden_Entrada {
		public Long IdOrden;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Portafolio_Entrada {
		public Long IdPortafolio;
		public BigDecimal Cantidad;
		public BigDecimal Porcentaje;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Cartera_Entrada {
		public Long IdPortafolio;
		public Long FolioCartera;
		public BigDecimal Cantidad;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CrearSimulacionAsignacion_Entrada {
		public Long 						p_IdEmpresa;
		public Long 						p_IdUsuario;
		public String						p_IdModulo;
		public String						p_UsaReglasNegocio;
		public String						p_IdTipoAsignacion;
		public String						p_IdTipo;
		public String						p_Origen;
		public List<Transaccion_Entrada> 	p_Transacciones;
		public List<Cartera_Entrada> 		p_Carteras;
		public List<Orden_Entrada> 			p_Ordenes;
		public List<Portafolio_Entrada> 	p_Portafolios;
		
		public CrearSimulacionAsignacion_Entrada() {
			this.p_Transacciones = new ArrayList<>();
			this.p_Carteras = new ArrayList<>();
			this.p_Ordenes = new ArrayList<>();
			this.p_Portafolios = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	//SALIDAS
	public static class CrearSimulacionAsignacion_Salida {
		
		public List<MensajeSalida>		P_OUT_MENSAJES;
		public Long						P_OUT_ERRORESCONTADOR;
		public List<Simulacion_Salida>	P_OUT_SIMASIGNACION;
		public List<Cobro_Salida>		P_OUT_COBROSASIGNACION;
		public Long						P_OUT_ADVERTENCIASCONTADOR = 0L;

		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public CrearSimulacionAsignacion_Salida() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_ADVERTENCIASCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<>();
			this.P_OUT_SIMASIGNACION = new ArrayList<>();
			this.P_OUT_COBROSASIGNACION = new ArrayList<>();
		}
	}
		
	public static void main(String[]args) throws Exception{

		try (Connection con = PlataformaUtils.getBOLSAConnection())
		{
			Gson gson = Utils.getGsonBuilder();
		
			String jsonTest = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1611,\"p_IdModulo\":\"ORD\",\"p_UsaReglasNegocio\":\"S\",\"p_IdTipoAsignacion\":\"ASIGNACION_POSTRERA\",\"p_IdTipo\":\"NORMAL\",\"p_Origen\":\"ASIG\",\"p_Transacciones\":[{\"IdMovimiento\":26789,\"IdTransaccion\":26789,\"Cantidad\":574.00000000,\"Monto\":7390500.00000000,\"ValorRescate\":0E-8}],\"p_Carteras\":[],\"p_Ordenes\":[],\"p_Portafolios\":[{\"IdPortafolio\":412762,\"Cantidad\":0,\"Porcentaje\":0}]}";
		   String jsonTest2 = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1611,\"p_IdModulo\":\"ORD\",\"p_UsaReglasNegocio\":\"S\",\"p_IdTipoAsignacion\":\"ASIGNACION_POSTRERA\",\"p_IdTipo\":\"NORMAL\",\"p_Origen\":\"ASIG\",\"p_Transacciones\":[{\"IdMovimiento\":26789,\"IdTransaccion\":26789,\"Cantidad\":575,\"Monto\":0,\"ValorRescate\":0}],\"p_Carteras\":[],\"p_Ordenes\":[],\"p_Portafolios\":[{\"IdPortafolio\":412762,\"Cantidad\":0,\"Porcentaje\":0}]}";
			
			CrearSimulacionAsignacion_Entrada entrada = gson.fromJson(jsonTest, CrearSimulacionAsignacion_Entrada.class);

			CrearSimulacionAsignacion simulacion = new CrearSimulacionAsignacion();
			CrearSimulacionAsignacion_Salida salida = simulacion.logica(con, entrada);
			System.out.println(salida);
			con.rollback();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
	}

	@Override
	@Trace
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = getGsonBuilder();
		CrearSimulacionAsignacion_Entrada in = gson.fromJson(jin.toString(), CrearSimulacionAsignacion_Entrada.class);
		CrearSimulacionAsignacion_Salida out = logica(conn, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
}
	