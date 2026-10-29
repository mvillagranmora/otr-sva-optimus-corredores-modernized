package cl.bcs.contabilidad;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.math.MathContext;

import cl.bcs.contabilidad.pojo.TabDatosMovimiento;
import cl.bcs.contabilidad.pojo.TabEventos;
import cl.bcs.contabilidad.pojo.TabMovimientos;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

import java.util.stream.Collectors;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;

public class CentralizaTesoreria implements BcsCentralizacion{
	
	private final Log log = new Log("CON.CentralizaTesoreria");
	
	private final String SQL_SELECT_MOVIMIENTOS = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaTesoreria_SelectMovimientos.sql");
	private final String SQL_SELECT_MOVIMIENTOS_PARTIDAS = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaTesoreria_SelectMovimientos_partidas.sql");
	private final String SQL_SELECT_EVENTOS = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaTesoreria_SelectEventos.sql");
	private final String SQL_SELECT_EVENTOS_PARTIDAS = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaTesoreria_SelectEventos_partidas.sql");
	private final String SQL_SELECT_DATOS_MOVIMIENTOS = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaTesoreria_SelectDatosMovimiento.sql");	
	private final String SQL_SELECT_DATOS_MOVIMIENTOS_SOLICITUDES = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaTesoreria_SelectDatosMovimiento_Solicitudes_Partidas.sql");	
	private final String SQL_INSERT_COMPROBANTES_ASOCIADOS = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaTesoreria_Insert_Comprobantes_Asociados_Partidas.sql");
	private final String SQL_SELECT_DATOS_MOVIMIENTOS_DOCUMENTOS = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaTesoreria_SelectDatosMovimiento_Documentos_Partidas.sql");
	private final String SQl_CALL_PARIDADYTIPOCAMBIO = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaTesoreria_Select_Parametros_Tipo_Cambio_Paridad.sql");
	
	private HashMap<Long, Object> hashResultParidad = new HashMap<Long, Object>();
	private HashMap<String, Object> hashRechazoParidad = new HashMap<String, Object>();
	private final List<String> tiposEventoCalculoParidad = Arrays.asList("SOLICITUD_MONTO_INGRESO", "SOLICITUD_MONTO_TRASPASO_INGRESO", "SOLICITUD_MONTO_EGRESO", "SOLICITUD_MONTO_TRASPASO_EGRESO"
			,"DOCUMENTO_MONTO_INGRESO", "DOCUMENTO_MONTO_TRASPASO_INGRESO", "DOCUMENTO_MONTO_EGRESO","DOCUMENTO_MONTO_TRASPASO_EGRESO");
	
	public static class TabDatosMovimientoPaso extends BcsPojo {
		public Long IdMovimiento;
		public String TipoDocumento;
		public Long Folio;
		public String FechaDocumento;
		public String NombreCuentaCorriente;
		public String NumeroCuentaCorriente;
		public String MedioDePago;
		public String NumeroDocumentoPago;
		public Date FechaLiquidacion;
		public Timestamp FechaDocumentoPago;
		public String SerieDocumento;
		public String Concepto;
		public String NombreConcepto;
		public String DocumentoIdentidad;
		public String NombreCliente;
		public String Portafolio;
		public String Moneda;
		public String CodigoCaja;
		public String EmitidoA;
		public String EsBilateral;
		public String TipoPersona;
		public String Agente;
		public String NombreAgente;
		public String SucursalAgente;
		public String NombreSucursalAgente;
		public String CentroCostoAgente;
		public String FechaProceso;
		public String Proceso;
		public String TipoComprobante;
		public String Valuta;
		public String PartidaLiquidacion;
		public Long NumeroComprobante;
		public String AliasBancoCaja;
		public BigDecimal Monto;
		public String TipoComprobanteAdj;
		public Long FolioComprobanteAdj;
		public Long FolioOperacion;
		public String CodigoProducto;
		public String Nemotecnico;
		public String Emisor;
		public String Relacion;
		public BigDecimal Valor;
		public BigDecimal TipoCambio;
		public String EsContraparte;
		public String Bolsa;
		public String ComprobanteTipo;
		public Date FechaMovimiento;
		public BigDecimal MontoMonedaSistema;
		public String DocumentoIdentidadCAVALI;
		public Long Referencia3;
		public String EsProveedor;
		public String ClienteBancario;
		public String CondicionLiquidacion;
		public Long FolioDTE;
		public String OperacionNoFacturable;

	}
	
	public static class ComprobantesAsociados extends BcsPojo {
		public Long IdComprobanteAsociado;
	}
	
	public static class ComprobantesAsociadosParams extends BcsPojo{
		public String FechaContable;
		public Long IdEmpresa;
	}
	
	public static class SelectParidadesYTipoCambio extends BcsPojo {
		public String IdMoneda;
		public Timestamp Fecha;
		public BigDecimal ParidadMercado;
		public BigDecimal TipoCambioMercado;
	}

	@Override
	public SalidaCentralizacion centralizar(Connection con, EntradaCentralizacion entrada, EntradaCentralizacionInfoAdicional infoAdicional) throws Exception {
		log.write("INICIO::CentralizaTesoreria");
		SQL sql = new SQL();
		SalidaCentralizacion out = new SalidaCentralizacion();
		
		String fechaSistema = infoAdicional.p_FechaSistema;
		String localizacionSistema = infoAdicional.p_LocalizacionSistema;
		
		String calculaParidadCLPCentralizacionTesoreria;
		try {
			calculaParidadCLPCentralizacionTesoreria = ConfiguracionSistema.getParametroConfiguracionString(con,
					entrada.p_IdEmpresa, "CONTABILIDAD", "CalculaParidadCLPCentralizacionTesoreria");
		} catch (Exception e) {
			calculaParidadCLPCentralizacionTesoreria = "N";
		}

		//MOVIMIENTOS
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("FechaContable", entrada.p_FechaContable);
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		params.put("IdTipoCentralizacion", entrada.p_IdTipoCentralizacion);		
		params.put("LocSistema",localizacionSistema);

		List<TabMovimientos> lstTabMovimientos = sql.query(con, SQL_SELECT_MOVIMIENTOS, params, TabMovimientos.class);

		//requiere calcular paridad y hay movimientos?
		if (calculaParidadCLPCentralizacionTesoreria.equalsIgnoreCase("S") && lstTabMovimientos.size()>0) {
			
			CalcularParidadEntrada 	entradaCalcularParidad 		= new CalcularParidadEntrada();		
			CalcularParidadSalida 	salidaCalcularParidad 		= new CalcularParidadSalida();
			List<CalculoEntrada> 	lstCalculoParidadEntrada 	= new ArrayList<CalculoEntrada>();

			//Filtra movimientos con moneda distinta a CLP
			List<TabMovimientos> lstTabMovimientosNoCLP = lstTabMovimientos.stream().filter(c -> !c.IdMoneda.equals("CLP")).collect(Collectors.toList());

			//Prepara entrada para calcular paridad
			lstTabMovimientosNoCLP.forEach(mov -> {
				CalculoEntrada objCalculoEntrada = new CalculoEntrada();

				objCalculoEntrada.IdTrabajo 					= 0L;
				objCalculoEntrada.IdInterno 					= mov.IdMovimiento;
				objCalculoEntrada.Fecha 						= mov.Fecha; // FALTA.. VERIFICAR QUE FECHA DEBE ENTRAR.. ??? LA DE SISTEMA
				objCalculoEntrada.IdMonedaOrigen 				= mov.IdMoneda; // MONEDA QUE VIENE EJ USD
				objCalculoEntrada.IdMonedaDestino 				= "CLP"; // MONDEA A TRANSFORMAR
				objCalculoEntrada.ValorCambioLocalModificado 	= BigDecimal.ZERO;
				objCalculoEntrada.IdUso 						= "";
				objCalculoEntrada.MontoOrigen 					= mov.Monto;

				lstCalculoParidadEntrada.add(objCalculoEntrada);
			});
			
			if (lstCalculoParidadEntrada.size() > 0) {
				// Calcular paridad CLP del monto
				entradaCalcularParidad.p_IdEmpresa = entrada.p_IdEmpresa;
				entradaCalcularParidad.p_IdTrabajo = 0L;
				entradaCalcularParidad.p_CalculosEntrada = lstCalculoParidadEntrada;
				//salidaCalcularParidad = servicioCalcularParidad.logica(con, entradaCalcularParidad);
				salidaCalcularParidad = calculoParidades(entradaCalcularParidad.p_CalculosEntrada, entrada, con);

				hashRechazoParidad = new HashMap<String, Object>();
				
				salidaCalcularParidad.p_out_CalculosSalida.forEach(calc-> {
					if(calc.Resultado.equalsIgnoreCase("RECHAZO")) {
						String clave = calc.Fecha.toString() + ";" + calc.IdMonedaOrigen;
						if(hashRechazoParidad.get(clave)==null) {
							hashRechazoParidad.put(clave, clave);
						}
					}
				});

				if (hashRechazoParidad.isEmpty()) {
					hashResultParidad = Utils.List2Hash(salidaCalcularParidad.p_out_CalculosSalida, "IdInterno");
				} else {
					out.P_OUT_ERRORESCONTADOR = 1L;
					for (Entry<String, Object> entry : hashRechazoParidad.entrySet()) {
						// System.out.println(entry.getKey() + " = " + entry.getValue());
						HashMap<String, String> valores = new HashMap<String, String>();
						valores.put("Moneda", String.valueOf(entry.getValue().toString().split(";")[1]));
						MensajeSalida objMensaje = new MensajeSalida(); // 0,MensajeSalida.MENSAJE_ERROR,"UI_PARAMETRO_FINANCIERO_FECHA_SOLICITADA",valores,con,entrada.p_IdEmpresa);
						objMensaje.id = 0L;
						objMensaje.tipo = MensajeSalida.MENSAJE_ERROR;
						objMensaje.codigo = "UI_PARAMETRO_FINANCIERO_FECHA_SOLICITADA";
						objMensaje.descripcion = Diccionario.getMensaje(con, entrada.p_IdEmpresa,
								"UI_PARAMETRO_FINANCIERO_FECHA_SOLICITADA", valores);
						out.P_OUT_MENSAJES.add(objMensaje);
					}

				}
				
				//Asigna el nuevo monto con la paridad CLP
				if (hashResultParidad.size() > 0) {
					lstTabMovimientos.forEach(mov -> {
						CalculoSalida calculoSalida = (CalculoSalida) hashResultParidad.get(mov.IdMovimiento);
						if (calculoSalida != null && !mov.IdMoneda.equalsIgnoreCase("CLP")) {
							mov.Monto = calculoSalida.MontoDestino.setScale(0, RoundingMode.HALF_UP);

						}
					});
				}
			}			
		}
		
		//no tuvo errores derivados de CalculoParidad
		if (out.P_OUT_ERRORESCONTADOR==0) {
			
			out.P_TABMOVIMIENTOS = lstTabMovimientos;
			
			//EVENTOS
			params.clear();		
			params.put("FechaContable", entrada.p_FechaContable);
			params.put("IdEmpresa", entrada.p_IdEmpresa);
			params.put("IdTipoCentralizacion", entrada.p_IdTipoCentralizacion);
			params.put("LocSistema",localizacionSistema);

			List<TabEventos> lstTabEventos = sql.query(con, SQL_SELECT_EVENTOS, params, TabEventos.class);
			
			if (calculaParidadCLPCentralizacionTesoreria.equalsIgnoreCase("S") && hashResultParidad.size() > 0) {
				// Asigna el nuevo monto con paridad CLP
				lstTabEventos.forEach(eve -> {
					if (tiposEventoCalculoParidad.contains(eve.IdEvento)) {
						CalculoSalida calculoSalida = (CalculoSalida) hashResultParidad.get(eve.IdMovimiento);
						if (calculoSalida != null && !eve.CodigoMoneda.equalsIgnoreCase("CLP")) {
							if (calculoSalida.MontoDestino == null) {
								log.write(eve.IdMovimiento);
								eve.MontoEvento = BigDecimal.valueOf(0);
							} else {
								eve.MontoEvento = calculoSalida.MontoDestino.setScale(0, RoundingMode.HALF_UP);
							}

						}
					}
				});
			}
	
			out.P_TABEVENTOS = lstTabEventos; 
			
			//MOVIMIENTO DATOS
			params.clear();
			params.put("FechaContable", entrada.p_FechaContable);
			params.put("IdEmpresa", entrada.p_IdEmpresa);
			params.put("FechaSistema", fechaSistema);
			params.put("LocSistema",localizacionSistema);
			List<TabDatosMovimientoPaso> tabDatosMovimientoPaso = sql.query(con, SQL_SELECT_DATOS_MOVIMIENTOS, params, TabDatosMovimientoPaso.class);
			
			//Buscamos comprobantes asociados para solicitudes y documentos
			List<ComprobantesAsociadosParams> ListaParamsPartidas = new ArrayList<ComprobantesAsociadosParams>(1);
			ComprobantesAsociadosParams parametrosInsert = new ComprobantesAsociadosParams();
			parametrosInsert.IdEmpresa = entrada.p_IdEmpresa;
			parametrosInsert.FechaContable = entrada.p_FechaContable;
			ListaParamsPartidas.add(parametrosInsert);
			
			sql.batch(con, SQL_INSERT_COMPROBANTES_ASOCIADOS, ListaParamsPartidas, ComprobantesAsociadosParams.class);
			
			tabDatosMovimientoPaso.addAll(sql.query(con, SQL_SELECT_DATOS_MOVIMIENTOS_DOCUMENTOS, params, TabDatosMovimientoPaso.class));
			tabDatosMovimientoPaso.addAll(sql.query(con, SQL_SELECT_DATOS_MOVIMIENTOS_SOLICITUDES, params, TabDatosMovimientoPaso.class));
			
			params.clear();
			
	
			if (calculaParidadCLPCentralizacionTesoreria.equalsIgnoreCase("S") && hashResultParidad.size() > 0) {
				// Asigna el nuevo monto con paridad CLP
				tabDatosMovimientoPaso.forEach(mov -> {
					CalculoSalida calculoSalida = (CalculoSalida) hashResultParidad.get(mov.IdMovimiento);
					if (calculoSalida != null && !mov.Moneda.equalsIgnoreCase("CLP")) {
						if (calculoSalida.MontoDestino == null) {
							log.write(mov.IdMovimiento);
							mov.Monto = BigDecimal.valueOf(0);
							mov.Valor = BigDecimal.valueOf(0);

						} else {
							mov.Monto = calculoSalida.MontoDestino.setScale(0, RoundingMode.HALF_UP);
							mov.Valor = calculoSalida.MontoDestino.setScale(0, RoundingMode.HALF_UP);
						}

					}
				});
			}

			for(TabDatosMovimientoPaso mov : tabDatosMovimientoPaso) {
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "NombreCuentaCorriente", mov.NombreCuentaCorriente, BigDecimal.ZERO));
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "TipoDocumento", mov.TipoDocumento, BigDecimal.ZERO));
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Folio", String.valueOf(mov.Folio), BigDecimal.ZERO));
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "FechaDocumento", mov.FechaDocumento, BigDecimal.ZERO));
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "NumeroCuentaCorriente", mov.NumeroCuentaCorriente, BigDecimal.ZERO));
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "MedioDePago", mov.MedioDePago, BigDecimal.ZERO));
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "NumeroDocumentoPago", mov.NumeroDocumentoPago, BigDecimal.ZERO));
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "FechaLiquidacion", Fecha.stringToSqlDate(mov.FechaLiquidacion), BigDecimal.ZERO));
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "FechaDocumentoPago", mov.FechaDocumentoPago != null ? Fecha.toStringTimeStamp(mov.FechaDocumentoPago) : "0001-01-01 00:00:00", BigDecimal.ZERO));
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "DocumentoIdentidadCAVALI", mov.DocumentoIdentidadCAVALI, BigDecimal.ZERO));
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "SerieDocumento", mov.SerieDocumento, BigDecimal.ZERO));
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Concepto", mov.Concepto, BigDecimal.ZERO));
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "NombreConcepto", mov.NombreConcepto, BigDecimal.ZERO));
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "DocumentoIdentidad", mov.DocumentoIdentidad, BigDecimal.ZERO));
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "NombreCliente", mov.NombreCliente, BigDecimal.ZERO));
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Portafolio", mov.Portafolio, BigDecimal.ZERO));
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Moneda", mov.Moneda, BigDecimal.ZERO));
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "CodigoCaja", mov.CodigoCaja, BigDecimal.ZERO));
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "EmitidoA", mov.EmitidoA, BigDecimal.ZERO));
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "EsBilateral", mov.EsBilateral, BigDecimal.ZERO));
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "TipoPersona", mov.TipoPersona, BigDecimal.ZERO));
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Agente", mov.Agente, BigDecimal.ZERO));
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "NombreAgente", mov.NombreAgente, BigDecimal.ZERO));
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "SucursalAgente", mov.SucursalAgente, BigDecimal.ZERO));
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "NombreSucursalAgente", mov.NombreSucursalAgente, BigDecimal.ZERO));
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "CentroCostoAgente", mov.CentroCostoAgente, BigDecimal.ZERO));
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "FechaProceso", mov.FechaProceso, BigDecimal.ZERO));
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Proceso", mov.Proceso, BigDecimal.ZERO));
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "TipoComprobante", mov.TipoComprobante, BigDecimal.ZERO));
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Valuta", mov.Valuta, BigDecimal.ZERO));
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "PartidaLiquidacion", mov.PartidaLiquidacion, BigDecimal.ZERO));
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "NumeroComprobante", String.valueOf(mov.NumeroComprobante), BigDecimal.ZERO));
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "AliasBancoCaja", mov.AliasBancoCaja, BigDecimal.ZERO));
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Monto", "", mov.Monto));
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "TipoComprobanteAdj", mov.TipoComprobanteAdj, BigDecimal.ZERO));
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "FolioComprobanteAdj", String.valueOf(mov.FolioComprobanteAdj), BigDecimal.ZERO));
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "FolioOperacion", String.valueOf(mov.FolioOperacion), BigDecimal.ZERO));
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "CodigoProducto", mov.CodigoProducto, BigDecimal.ZERO));
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Nemotecnico", mov.Nemotecnico, BigDecimal.ZERO));
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Emisor", mov.Emisor, BigDecimal.ZERO));
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Relacion", mov.Relacion, BigDecimal.ZERO));
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Valor", "", mov.Valor));
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "TipoCambio", "", mov.TipoCambio));
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "EsContraparte", mov.EsContraparte, BigDecimal.ZERO));
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Bolsa", mov.Bolsa, BigDecimal.ZERO));
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "ComprobanteTipo", mov.ComprobanteTipo, BigDecimal.ZERO));
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "FechaMovimiento", Fecha.stringToSqlDate(mov.FechaMovimiento), BigDecimal.ZERO));
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "MontoMonedaSistema", "", mov.MontoMonedaSistema));
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Referencia3",  String.valueOf(mov.Referencia3), BigDecimal.ZERO));
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "EsProveedor",  String.valueOf(mov.EsProveedor), BigDecimal.ZERO));
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "ClienteBancario", mov.ClienteBancario, BigDecimal.ZERO));	
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "CondicionLiquidacion", mov.CondicionLiquidacion, BigDecimal.ZERO));	
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "FolioDTE", String.valueOf(mov.FolioDTE), BigDecimal.ZERO));
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "OperacionNoFacturable", String.valueOf(mov.OperacionNoFacturable), BigDecimal.ZERO));
			}
			
			out.P_OUT_RESULTADO = llenarResultado(out.P_TABMOVIMIENTOS);
			
		}
		
		log.write("FIN::CentralizaTesoreria");
		return out;
	}
	
	public CalcularParidadSalida calculoParidades(List<CalculoEntrada> calculoparidades ,EntradaCentralizacion in ,Connection con) throws Exception{
		log.write("INICIO::CalcularParidad");
	    SQL sql = new SQL();
	    CalcularParidadSalida salida = new CalcularParidadSalida();
	    Date fechaMaxima = null;
	    Date fechaMinima = null;
	    String ID_MONEDA_CAMBIO = "USD";
	    String ID_PARAMETRO_FINANCIERO_DEFAULT = "DO";
	    Long diasDiferenciaFechaMinima = 1095L;
		
	    try {
			diasDiferenciaFechaMinima = ConfiguracionSistema.getParametroConfiguracionLong(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.DiasFechaMinimaCalculoParidad");
		} catch (Exception e) {
			log.write("ADVERTENCIA::Parametro PLA.DiasFechaMinimaCalculoParidad no existe, se toma valor por defecto de " + diasDiferenciaFechaMinima + " dias.");
		}
	    
		try {
			String idMonedaLocal = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");

			for (CalculoEntrada calc : calculoparidades) {
				CalculoSalida calculoSalida = new CalculoSalida();
				BigDecimal factor = BigDecimal.ZERO;
				Date fecha = Fecha.toDate(calc.Fecha);

				if (fechaMaxima == null || fechaMaxima.compareTo(fecha) == -1)
					fechaMaxima = fecha;
				if (fechaMinima == null || fechaMinima.compareTo(fecha) == 1)
					fechaMinima = fecha;
				if (fechaMinima != null && !fechaMinima.equals(Fecha.toDate("0001-01-01"))) {
					fechaMinima = Fecha.addDays(fechaMinima, diasDiferenciaFechaMinima.intValue() * -1);
				}
				HashMap<String, Object> paramsEntrada = new HashMap<String, Object>();
				paramsEntrada.put("IdParametroFinanciero", ID_PARAMETRO_FINANCIERO_DEFAULT);
				paramsEntrada.put("FechaMinima", fechaMinima);
				paramsEntrada.put("FechaMaxima", fechaMaxima);
				
				List<SelectParidadesYTipoCambio> ParidadPivote = sql.query(con, SQl_CALL_PARIDADYTIPOCAMBIO,paramsEntrada, SelectParidadesYTipoCambio.class);
				HashMap<String, Object> hDefinicionParametros = Utils.List2Hash(ParidadPivote, "IdMoneda");
				SelectParidadesYTipoCambio DolarObservado = (SelectParidadesYTipoCambio) hDefinicionParametros.get(ID_PARAMETRO_FINANCIERO_DEFAULT);

				paramsEntrada.clear();
				paramsEntrada.put("IdParametroFinanciero", calc.IdMonedaOrigen);
				paramsEntrada.put("FechaMinima", fechaMinima);
				paramsEntrada.put("FechaMaxima", fechaMaxima);
				
				List<SelectParidadesYTipoCambio> Paridad = sql.query(con, SQl_CALL_PARIDADYTIPOCAMBIO, paramsEntrada, SelectParidadesYTipoCambio.class);
				HashMap<String, Object> hParidadMoneda = Utils.List2Hash(Paridad, "IdMoneda");
				SelectParidadesYTipoCambio ParidadMoneda = (SelectParidadesYTipoCambio) hParidadMoneda.get(calc.IdMonedaOrigen);
				
				if (ID_MONEDA_CAMBIO.equals(calc.IdMonedaOrigen)) {
					factor = (BigDecimal) DolarObservado.TipoCambioMercado;
				} else {
					if (ParidadMoneda == null || ParidadMoneda.TipoCambioMercado == null) {
						calculoSalida.Resultado = "RECHAZO";
						calculoSalida.CodigoMensaje = "ERROR_MONEDA_TIPO_CAMBIO_NO_EXISTE";
						salida.P_OUT_ERRORESCONTADOR = 1L;
					} else {
						factor = (BigDecimal) ParidadMoneda.TipoCambioMercado;
					}
				}

				calculoSalida.IdTrabajo = calc.IdTrabajo;
				calculoSalida.IdInterno = calc.IdInterno;
				calculoSalida.IdMonedaOrigen = calc.IdMonedaOrigen;
				calculoSalida.IdMonedaDestino = calc.IdMonedaDestino;
				calculoSalida.ValorCambioLocalModificado = calc.ValorCambioLocalModificado;
				calculoSalida.MontoOrigen = calc.MontoOrigen;
				calculoSalida.IdUso = calc.IdUso;
				calculoSalida.Fecha = fecha;
				calculoSalida.FactorOrigenDestino = BigDecimal.ZERO;
				calculoSalida.FactorOrigenLocal = BigDecimal.ZERO;
				calculoSalida.FactorOrigenCambio = factor;
				calculoSalida.FechaOrigenCambio = fecha;
				calculoSalida.IdTipoRegistroOrigenCambio = calc.IdMonedaOrigen;
				calculoSalida.FactorCambioLocal = factor;
				calculoSalida.FechaCambioLocal = fecha;
				calculoSalida.IdTipoRegistroCambioLocal = "";
				calculoSalida.FactorLocalCambio = (BigDecimal) DolarObservado.TipoCambioMercado;
				calculoSalida.FechaLocalCambio = fecha;
				calculoSalida.IdTipoRegistroLocalCambio = idMonedaLocal;
				calculoSalida.FactorCambioDestino = factor;
				calculoSalida.FechaCambioDestino = fecha;
				calculoSalida.IdTipoRegistroCambioDestino = "";
				calculoSalida.ValorParametroFinanciero = factor;
				calculoSalida.FechaParametroFinanciero = fecha;
				calculoSalida.MontoLocal = calc.MontoOrigen.multiply(calculoSalida.FactorOrigenCambio, MathContext.DECIMAL128);
				calculoSalida.MontoDestino = calc.MontoOrigen.multiply(calculoSalida.FactorOrigenCambio, MathContext.DECIMAL128);
				if (factor.compareTo(BigDecimal.ZERO) <= 0) {
					calculoSalida.MontoLocal = null;
					calculoSalida.MontoDestino = null;
					calculoSalida.Resultado = "RECHAZO";
					calculoSalida.CodigoMensaje = "ERROR_CALCULAR_PARIDAD_MONTO_DESTINO";
					salida.P_OUT_ERRORESCONTADOR = 1L;
				}
				if (calculoSalida.MontoDestino == null) {
					calculoSalida.Resultado = "RECHAZO";
					calculoSalida.CodigoMensaje = "ERROR_CALCULAR_PARIDAD_MONTO_DESTINO";
				} else if (calculoSalida.MontoLocal == null) {
					calculoSalida.Resultado = "RECHAZO";
					calculoSalida.CodigoMensaje = "ERROR_CALCULAR_PARIDAD_MONTO_LOCAL";
				} else {
					calculoSalida.Resultado = "OK";
					calculoSalida.CodigoMensaje = "OK";
				}
				salida.p_out_CalculosSalida.add(calculoSalida);
			}
	        log.write("FIN::CalcularParidad");
	        return salida;
	    }catch(Exception e) {
	    	log.write("ERROR: ",e);
	    	salida.P_OUT_ERRORESCONTADOR =  1L;
	    }
	    return salida;
	}
}
