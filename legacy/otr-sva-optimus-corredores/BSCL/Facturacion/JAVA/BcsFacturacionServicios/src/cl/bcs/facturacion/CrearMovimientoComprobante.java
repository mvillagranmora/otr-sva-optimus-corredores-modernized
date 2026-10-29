package cl.bcs.facturacion;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.cuentaInversion.CrearCargoAbono;
import cl.bcs.cuentaInversion.CrearCargoAbono.CrearCargoAbonoEntrada;
import cl.bcs.cuentaInversion.CrearCargoAbono.CrearCargoAbonoSalida;
import cl.bcs.cuentaInversion.CrearCargoAbono.ObjCargoAbonoAGenerar;
import cl.bcs.facturacion.CrearComprobante.CrearComprobante_Entrada;
import cl.bcs.facturacion.CrearComprobante.CrearComprobante_Salida;
import cl.bcs.facturacion.pojo.MovimientoComprobante;
import cl.bcs.facturacion.pojo.MovimientoComprobanteCobro;
import cl.bcs.facturacion.pojo.MovimientoComprobanteFormaDePago;
import cl.bcs.facturacion.pojo.ReferenciaTipoDocumento;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.plataforma.cre.ConsultarEstadoModulo;
import cl.bcs.plataforma.cre.ConsultarEstadoModulo.InConsultarEstadoModulo;
import cl.bcs.plataforma.cre.ConsultarEstadoModulo.Modulo;
import cl.bcs.plataforma.cre.ConsultarEstadoModulo.OutConsultarEstadoModulo;
import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;
import cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo;

public class CrearMovimientoComprobante implements JavaCall {

	private static final Logger log = LogManager.getLogger(CrearMovimientoComprobante.class);

	
	private final String SQL_SELECT_MESAS = loadResourceToString("cl/bcs/facturacion/sql/CrearMovimientoComprobante_SelectMesas.sql");
	private final String SQL_SELECT_MOVIMIENTO_COMPROBANTE = loadResourceToString("cl/bcs/facturacion/sql/CrearMovimientoComprobante_SelectMovimientoComprobante.sql");
	private final String SQL_SELECT_MOVIMIENTO_COMPROBANTE_FORMA_DE_PAGO = loadResourceToString("cl/bcs/facturacion/sql/CrearMovimientoComprobante_SelectMovimientoComprobanteFormaDePago.sql");
	private final String SQL_SELECT_MOVIMIENTO_COMPROBANTE_COBRO = loadResourceToString("cl/bcs/facturacion/sql/CrearMovimientoComprobante_SelectMovimientoComprobanteCobro.sql");
	private final String SQL_SELECT_SECUENCIAS = loadResourceToString("cl/bcs/facturacion/sql/CrearMovimientoComprobante_SelectSecuencias.sql");
	private final String SQL_SELECT_PERSONA_CLIENTE = loadResourceToString("cl/bcs/facturacion/sql/CrearMovimientoComprobante_SelectPersonaCliente.sql");
	private final String SQL_SELECT_PORTAFOLIO = loadResourceToString("cl/bcs/facturacion/sql/CrearMovimientoComprobante_SelectPortafolio.sql");
	private final String SQL_SELECT_MEDIODEPAGO = loadResourceToString("cl/bcs/facturacion/sql/CrearMovimientoComprobante_SelectMedioDePago.sql");
	private final String SQL_SELECT_COBRO = loadResourceToString("cl/bcs/facturacion/sql/CrearMovimientoComprobante_SelectCobro.sql");
	
	private final String SQL_INSERT_MOVIMIENTO_COMPROBANTE_PROCESO = loadResourceToString("cl/bcs/facturacion/sql/CrearMovimientoComprobante_InsertMovimientoComprobanteProceso.sql");
	private final String SQL_INSERT_MOVIMIENTO_COMPROBANTE_FORMA_DE_PAGO_PROCESO = loadResourceToString("cl/bcs/facturacion/sql/CrearMovimientoComprobante_InsertMovimientoComprobanteFormaDePagoProceso.sql");
	private final String SQL_INSERT_MOVIMIENTO_COMPROBANTE_COBRO_PROCESO = loadResourceToString("cl/bcs/facturacion/sql/CrearMovimientoComprobante_InsertMovimientoComprobanteCobroProceso.sql");
	
	private final String SQL_INSERT_MOVIMIENTO_COMPROBANTE = loadResourceToString("cl/bcs/facturacion/sql/CrearMovimientoComprobante_InsertMovimientoComprobante.sql");
	private final String SQL_INSERT_MOVIMIENTO_COMPROBANTE_COBRO = loadResourceToString("cl/bcs/facturacion/sql/CrearMovimientoComprobante_InsertMovimientoComprobanteCobro.sql");
	private final String SQL_INSERT_MOVIMIENTO_COMPROBANTE_FORMA_DE_PAGO = loadResourceToString("cl/bcs/facturacion/sql/CrearMovimientoComprobante_InsertMovimientoComprobanteFormaDePago.sql");

	private final String SQL_INSERT_MOVIMIENTO_COMPROBANTE_REFERENCIA = loadResourceToString("cl/bcs/facturacion/sql/CrearMovimientoComprobante_InsertMovimientoComprobanteReferencia.sql");
	
	private final String SQL_SELECT_CONCEPTO_INDICADOR_ABONO_CLP_CONTRAPARTIDA = loadResourceToString("cl/bcs/facturacion/sql/CrearMovimientoComprobante_SelectConceptoIndicadorAbonoClpContrapartida.sql");
	private final String SQL_SELECT_MOVIMIENTO_COMPROBANTE_ANTERIOR = loadResourceToString("cl/bcs/facturacion/sql/CrearMovimientoComprobante_SelectMovimientoComprobanteAnterior.sql");
	
	private static final String MODULO_ESTADO_CERRADO = "CER";
	private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();
	private final String SI = "S";
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		CrearMovimientoComprobante_Entrada in = gson.fromJson(jin.toString(), CrearMovimientoComprobante_Entrada.class);
		CrearMovimientoComprobante_Salida out = logica(con, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	
	private String ConsultarEstadoModulo(Long idEmpresa, Long idUsuario, Connection conn){
		String estado = "";
		
		InConsultarEstadoModulo entrada = new InConsultarEstadoModulo();
		entrada.p_IdEmpresa = idEmpresa;
		entrada.p_IdUsuario = idUsuario;
		entrada.p_IdModulo = "FAC";
		ConsultarEstadoModulo objConsulta 	= new ConsultarEstadoModulo();
		
		try{
			OutConsultarEstadoModulo salida = objConsulta.logica(conn, entrada);
			//estado = salida.p_out_Estado;
			for (Modulo modulo : salida.p_out_ListaModulos) {
				if (entrada.p_IdModulo.equals(modulo.NombreModulo)) {
					estado = modulo.IdEstadoModulo;
					break;
				}
			}
		}catch (Exception e){
			estado = "";
			e.printStackTrace();
		}
		
		return estado;
	}
	
	private void limpiarMovimientos(List<MovimientoComprobante_Proceso> movimientos, List<MovimientoComprobante_Proceso> movimientosConError,List<MensajeSalida> mensajes,List<ResultadoProceso_Salida> resultadoProceso, Connection conn) throws Exception {
		
		List<String> secuenciaAgrupacionMalas = new ArrayList<>();
		
		for (MovimientoComprobante_Proceso mov : movimientos) {
			if (mov.errores > 0) {
				movimientosConError.add(mov);
				secuenciaAgrupacionMalas.add(mov.SecuenciaAgrupacion);
				
				ResultadoProceso_Salida res = new ResultadoProceso_Salida();
				res.Campo = "NumeroMvtoCmbte";
				res.Id = mov.NumeroMvtoCmbte;
				res.Resultado = MensajeSalida.MENSAJE_ERROR;
				res.Servicio = "CrearMovimientoComprobante.java";
				resultadoProceso.add(res);
				
			}
		}
		
		//ELIMINAMOS TODOS LOS MOVIMIENTOS CON LA SECUENCIA MALA QUE YA NO HAYAMOS ELIMINADO
		for (MovimientoComprobante_Proceso mov : movimientos) {
			if (secuenciaAgrupacionMalas.contains(mov.SecuenciaAgrupacion) && !movimientosConError.contains(mov)) {
				movimientosConError.add(mov);
				String codigo = "MVTO_CMBTE_SECUENCIA_CON_ERROR";
				String desc = Diccionario.getMensaje(conn, mov.IdEmpresa, codigo).msg();
				mensajes.add(new MensajeSalida(mov.NumeroMvtoCmbte,MensajeSalida.MENSAJE_ERROR,codigo,desc));
				mov.errores++;
				
				ResultadoProceso_Salida res = new ResultadoProceso_Salida();
				res.Campo = "NumeroMvtoCmbte";
				res.Id = mov.NumeroMvtoCmbte;
				res.Resultado = MensajeSalida.MENSAJE_ERROR;
				res.Servicio = "CrearMovimientoComprobante.java";
				resultadoProceso.add(res);
			}
		}
		
		for (MovimientoComprobante_Proceso mov : movimientosConError) {
			if (movimientos.contains(mov))
				movimientos.remove(mov);
		}
	}
	
	private void ProcesaRequisito(Connection con,Long idEmpresa,boolean esError,String codMsg,String CodRequisito,MovimientoComprobante_Proceso mvto,List<MensajeSalida> P_OUT_MENSAJES) 
			throws Exception {
		if (esError) {
			String NumeroOperacion = mvto.NumeroMvtoCmbte != null ? mvto.NumeroMvtoCmbte.compareTo(0L) > 0 ? mvto.NumeroMvtoCmbte.toString() : "" : "";
			if(_c.SpotCompra.equalsIgnoreCase(mvto.IdTipoOperacion) || _c.SpotVenta.equalsIgnoreCase(mvto.IdTipoOperacion)) {
				NumeroOperacion = mvto.FolioTransaccion != null ?  mvto.FolioTransaccion :  "";
			}
			String msg= Diccionario.getMensaje(con,idEmpresa,codMsg)
						  .add("IdInterno", mvto.NumeroMvtoCmbte)
						  .add("NumeroOperacion", NumeroOperacion)
						  .msg();
			if (CodRequisito.equals(MensajeSalida.MENSAJE_ERROR)) {
				P_OUT_MENSAJES.add(new MensajeSalida(mvto.NumeroMvtoCmbte,MensajeSalida.MENSAJE_ERROR,codMsg,msg));
				mvto.errores++;
			}else if (CodRequisito.equals(MensajeSalida.MENSAJE_AUTORIZACION)) {
				P_OUT_MENSAJES.add(new MensajeSalida(mvto.NumeroMvtoCmbte,MensajeSalida.MENSAJE_AUTORIZACION,codMsg,msg));
				mvto.autorizaciones++;						
			}
			else if (CodRequisito.equals(MensajeSalida.MENSAJE_ADVERTENCIA)) {
				P_OUT_MENSAJES.add(new MensajeSalida(mvto.NumeroMvtoCmbte,MensajeSalida.MENSAJE_ADVERTENCIA,codMsg,msg));
				mvto.advertencias++;
			}
			else {
				mvto.ok++;
			}
		}else {
			mvto.ok++;
		}		
	}
	
	private void agregarLogDetalle(String Movimiento, AgregarLog.InAgregarLog.Log log, String nombreCampo, String campo, List<AgregarLog.InAgregarLog.DetalleLog> ListaDetalleLog) {
		
		AgregarLog.InAgregarLog.DetalleLog detalleLog = new AgregarLog.InAgregarLog.DetalleLog();
		detalleLog.IdTrabajo = log.IdTrabajo;
		detalleLog.IdCabeceraLog = log.IdCabeceraLog;
		detalleLog.SubRegistro = Movimiento.trim();
		
		if(campo != null && campo != "" && campo.length() > 1) {
			detalleLog.CampoRegistro = nombreCampo.trim();
			detalleLog.ValorAntiguo = "";
			detalleLog.ValorNuevo = campo.trim();
			ListaDetalleLog.add(detalleLog);
		}
	}
	
	private Long obtenerContadorDetallePorLog(HashMap<Long, Long> hashMap, Long Key) {
		
		Long cont = 1L;
		if(!hashMap.containsKey(Key)) {
			hashMap.put(Key,  cont);
		}
		else {
			cont = hashMap.get(Key);
			cont += 1L;
			hashMap.replace(Key, cont);
		}	
		return cont;
	}
	
	private void asignarNumeroFolioCabeceraLog(AgregarLog.InAgregarLog.Log log, HashMap<Long, Long> hashMap) {
		
		Long folio = log.Folio;
		if(folio.compareTo(0L) == 0) {
			if(hashMap.containsKey( log.IdCabeceraLog )) {
				folio = hashMap.get(log.IdCabeceraLog);
				if(!(folio.compareTo(0L) == 0)) {
					log.Folio = folio;
					log.EstadoProceso = "ING";
				}
			}
		}
	}
	
	@Trace
	public CrearMovimientoComprobante_Salida logica(Connection conn, CrearMovimientoComprobante_Entrada in) throws Exception {
		CrearMovimientoComprobante_Salida salida = new CrearMovimientoComprobante_Salida();
		log.info("INICIO");
		SQL _sql = new SQL();

		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(conn, in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
		if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
			salida.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
			return salida;
		}
		
		if (in.p_Movimientos.size() == 0) {
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_INGRESARON_MOVIMIENTOS",null,conn,in.p_IdEmpresa));
			return salida;
		}
		
		Map<Long, MovimientoContrapartida> movimientosContrapartida = agregarMovimientosContrapartida(conn, in.p_Movimientos, in.p_IdEmpresa);
		List<MovimientoComprobanteCobro_Entrada> movimientosCobrosContrapartida = agregarMovimientosCobrosContrapartida(conn, movimientosContrapartida, in.p_MovimientosCobros, in.p_IdEmpresa);
		List<MovimientoComprobanteFormaDePago_Entrada> movimientosFormasDePagoContrapartida = agregarMovimientosFormasDePagoContrapartida(conn, movimientosContrapartida, in.p_MovimientosFormaDePago, in.p_IdEmpresa);

		List<MovimientoComprobante_Entrada> movimientosContrapartidaAbonoUsd = in.p_Movimientos.stream().filter(mov -> "S".equals(mov.Contrapartida)).collect(Collectors.toList());
		Stream<MovimientoComprobante_Entrada> movimientoSinContrapartida = in.p_Movimientos.stream().filter(mov -> "N".equals(mov.Contrapartida));
		Stream<MovimientoComprobante_Entrada> movimientosCargoAbono = movimientosContrapartida.values().stream().flatMap(mov -> Stream.of(mov.Cargo));
		
		in.p_Movimientos = Stream.concat(movimientoSinContrapartida, movimientosCargoAbono).collect(Collectors.toList());
				
		Map<Boolean, List<MovimientoComprobanteCobro_Entrada>> movimientosPorUsaContrapartida = in.p_MovimientosCobros.stream().collect(Collectors.partitioningBy(movCobro -> movimientosContrapartida.containsKey(movCobro.NumeroMvtoCmbte)));
		Map<Long, MovimientoComprobanteCobro_Entrada> movimietoCobroContrapartida = movimientosPorUsaContrapartida.get(true).stream().collect(Collectors.toMap(mov -> mov.NumeroMvtoCmbte, mov -> mov));
		
		in.p_MovimientosCobros =  Stream.concat(movimientosCobrosContrapartida.stream(), movimientosPorUsaContrapartida.get(false).stream()).collect(Collectors.toList());
		 
		Map<Boolean, List<MovimientoComprobanteFormaDePago_Entrada>> movimientosFormasDePagoPorUsaContrapartida = in.p_MovimientosFormaDePago.stream().collect(Collectors.partitioningBy(movPago -> movimientosContrapartida.containsKey(movPago.NumeroMvtoCmbte)));
		in.p_MovimientosFormaDePago =  Stream.concat(movimientosFormasDePagoContrapartida.stream(), movimientosFormasDePagoPorUsaContrapartida.get(false).stream()).collect(Collectors.toList());
	
		ArrayList<Long> correlativos = new ArrayList<Long>();
		for (MovimientoComprobante_Entrada movto: in.p_Movimientos) {
			if (correlativos.contains(movto.NumeroMvtoCmbte)) {
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"CORRELATIVO_INCORRECTO",null,conn,in.p_IdEmpresa));
				return salida;
			} else {
				correlativos.add(movto.NumeroMvtoCmbte);
			}
		}

		// TODO VALIDACION ESTADO MODULO
		log.debug("validaciones estado modulo");
		String monedaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA",
				"PLA.MonedaSistema");
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA",
				"PLA.FechaSistema");
		String paramLiquidacionComisionFact = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PRODUCTO", "PRO.CobroComisionesMensuales");
		paramLiquidacionComisionFact = "ACTIVO".equals(paramLiquidacionComisionFact) ? "S" : "N";
		Timestamp fechaHoraSistema = FechaDB.ObtenerFechaHoraSistemaTs(conn, in.p_IdEmpresa);
		List<MovimientoComprobante_Proceso> movimientos = new ArrayList<>();
		HashMap<Long, MovimientoComprobante_Proceso> hashMovimientos = new HashMap<>();
		List<MovimientoComprobante_Proceso> movimientosConError = new ArrayList<>();
		List<MovimientoComprobanteCobro_Proceso> movimientosCobro = new ArrayList<>();
		List<MovimientoComprobanteFormaDePago_Proceso> movimientosFormaDePago = new ArrayList<>();
		String localizacionSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.LocalizacionSistema");
		
		boolean esNCM = in.p_Movimientos.stream()
				.anyMatch(data -> ("NCR".equals(data.IdTipoComprobante) || "NEO".equals(data.IdTipoComprobante))
						&& data.IdComprobanteReferencia != null);

		if (esNCM) {
			List<Long> IdsComprobante = in.p_Movimientos.stream()
						.filter(data -> data.IdComprobanteReferencia != null)
						.map(data -> data.IdComprobanteReferencia).collect(Collectors.toList());
			
			if (IdsComprobante.size() > 0) {
				HashMap<String, Object> params = new HashMap<>(1);
				params.put("IdsComprobante", IdsComprobante);
				List<MovimientoComprobanteAnterior> movimientoComprobanteAnterior = _sql.query(conn, SQL_SELECT_MOVIMIENTO_COMPROBANTE_ANTERIOR, params, MovimientoComprobanteAnterior.class);

				if (movimientoComprobanteAnterior.size() > 0) {
					for (MovimientoComprobante_Entrada movto : in.p_Movimientos) {
						BigDecimal saldo = movimientoComprobanteAnterior.stream()
								.filter(data -> data.IdComprobanteReferencia.equals(movto.IdComprobanteReferencia))
								.map(data -> data.Saldo).reduce(BigDecimal.ZERO, BigDecimal::add);

						if (saldo.compareTo(movto.Monto) < 0) {
							String codigo = "NCM_SALDO_INSUFICIENTE";
							String desc = Diccionario.getMensaje(conn, in.p_IdEmpresa, codigo).msg();
							salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,codigo,desc));
							return salida;
						}
					}
				}
			}
		}
		
		for (MovimientoComprobante_Entrada mvtoEntrada : in.p_Movimientos) {
			MovimientoComprobante_Proceso mvto = new MovimientoComprobante_Proceso();
			
			mvto.ActualizarCustodia = mvtoEntrada.ActualizarCustodia;
			mvto.ActualizarCuentaInversion = mvtoEntrada.ActualizarCuentaInversion;
			mvto.ActualizarTesoreria = mvtoEntrada.ActualizarTesoreria;
			mvto.ActualizarCustodiaRelacionado = mvtoEntrada.ActualizarCustodiaRelacionado;
			mvto.Cantidad = mvtoEntrada.Cantidad;
			mvto.CrearComprobanteAutomatico = mvtoEntrada.CrearComprobanteAutomatico;
			mvto.CodigoBroker = mvtoEntrada.CodigoBroker;
			mvto.CodigoBancoCustodio = mvtoEntrada.CodigoBancoCustodio;
			mvto.CodigoBancoLiquidador = mvtoEntrada.CodigoBancoLiquidador;
			mvto.DiasPlazo = mvtoEntrada.DiasPlazo;
			mvto.EsAPlazo = mvtoEntrada.EsAPlazo;
			mvto.EsComprobIndividual = mvtoEntrada.EsComprobIndividual;
			mvto.EsCuentaPropia = mvtoEntrada.EsCuentaPropia;
			mvto.EsEnRueda = mvtoEntrada.EsEnRueda;
			mvto.EsFungible = mvtoEntrada.EsFungible;
			mvto.EsPrecioPrima = mvtoEntrada.EsPrecioPrima;
			mvto.EsRuteo = mvtoEntrada.EsRuteo;
			mvto.EsVentaCorta = mvtoEntrada.EsVentaCorta;
			mvto.EsClienteFinal = mvtoEntrada.EsClienteFinal;
			if (mvtoEntrada.FechaEmision.length() == 10) {
				mvtoEntrada.FechaEmision+=" 00:00:00";
			}
			mvto.FechaEmision = Fecha.toTimeStamp(mvtoEntrada.FechaEmision);
			mvto.FechaLiquidacion = Fecha.toDate(mvtoEntrada.FechaLiquidacion);
			if (mvtoEntrada.FechaMovimientoComprobante.length() == 10) {
				mvtoEntrada.FechaMovimientoComprobante+=" 00:00:00";
			}
			mvto.FechaMovimientoComprobante = Fecha.toTimeStamp(mvtoEntrada.FechaMovimientoComprobante);
			if (mvtoEntrada.FechaOrden.length() == 10) {
				mvtoEntrada.FechaOrden+=" 00:00:00";
			}
			mvto.FechaOrden = Fecha.toTimeStamp(mvtoEntrada.FechaOrden);
			if (mvtoEntrada.FechaParidad.length() == 10) {
				mvtoEntrada.FechaParidad+=" 00:00:00";
			}
			mvto.FechaParidad = Fecha.toTimeStamp(mvtoEntrada.FechaParidad);
			mvto.FechaTipoCambio = Fecha.toDate(mvtoEntrada.FechaTipoCambio);
			if (mvtoEntrada.FechaTransaccion.length() == 10) {
				mvtoEntrada.FechaTransaccion+=" 00:00:00";
			}
			mvto.FechaTransaccion = Fecha.toTimeStamp(mvtoEntrada.FechaTransaccion);
			if (mvtoEntrada.FechaVencimiento.length() == 10) {
				mvtoEntrada.FechaVencimiento+=" 00:00:00";
			}
			mvto.FechaHoraMovimientoComprobante = fechaHoraSistema;
			mvto.FechaVencimiento = Fecha.toTimeStamp(mvtoEntrada.FechaVencimiento);
			mvto.FolioCartera = mvtoEntrada.FolioCartera;
			mvto.FolioTransaccion = mvtoEntrada.FolioTransaccion;
			mvto.Glosa = mvtoEntrada.Glosa;
			mvto.IdAsignacion = mvtoEntrada.IdAsignacion;
			mvto.IdAsignacionRef = mvtoEntrada.IdAsignacionRef;
			mvto.IdBolsa = mvtoEntrada.IdBolsa;
			mvto.IdBoveda = mvtoEntrada.IdBoveda;
			mvto.IdComprobanteReferencia = mvtoEntrada.IdComprobanteReferencia;
			mvto.IdConceptoMovimiento = mvtoEntrada.IdConcepto;
			mvto.IdCondicionLiquidacion = mvtoEntrada.IdCondicionLiquidacion;
			mvto.IdCorredoraContraparte = mvtoEntrada.IdCorredoraContraparte;
			mvto.IdCliente = mvtoEntrada.IdCliente;
			if (mvtoEntrada.IdCuentaCustodioPortafolio == null) {
				mvto.IdCuentaCustodioPortafolio = 0L;
			}
			else {
				mvto.IdCuentaCustodioPortafolio = mvtoEntrada.IdCuentaCustodioPortafolio;	
			}
			
			mvto.IdEmisor = mvtoEntrada.IdEmisor;
			mvto.IdEmpleadoDigitador = mvtoEntrada.IdEmpleadoDigitador;                          
			mvto.IdEmpresa = mvtoEntrada.IdEmpresa;
			mvto.IdInstrumento = mvtoEntrada.IdInstrumento;
			mvto.IdInstrumentoRelacionado = mvtoEntrada.IdInstrumentoRelacionado;
			mvto.IdInstrumentoSubyacente = mvtoEntrada.IdInstrumentoSubyacente;
			mvto.IdFormaCustodia = mvtoEntrada.IdFormaCustodia;
			mvto.IdMesa = mvtoEntrada.IdMesa;
			mvto.IdMoneda = mvtoEntrada.IdMoneda;
			mvto.IdMonedaEmision = mvtoEntrada.IdMonedaEmision;
			mvto.IdMonedaLiquidacion = mvtoEntrada.IdMonedaLiquidacion;
			mvto.IdMonedaSubyacente = mvtoEntrada.IdMonedaSubyacente;
			mvto.IdMonedaNegociacion = mvtoEntrada.IdMonedaNegociacion;
			mvto.IdMovimientoRelacionado = mvtoEntrada.IdMovimientoRelacionado;
			mvto.IndicadorOrdenDirecta = mvtoEntrada.IndicadorOrdenDirecta;
			mvto.Intereses = mvtoEntrada.Intereses;
			mvto.IdOrden = mvtoEntrada.IdOrden;
			mvto.IdPersonaBancoCustodio = mvtoEntrada.IdPersonaBancoCustodio;
			mvto.IdPersonaBancoLiquidador = mvtoEntrada.IdPersonaBancoLiquidador;
			mvto.IdPersonaBroker = mvtoEntrada.IdPersonaBroker;
			mvto.IdPortafolio = mvtoEntrada.IdPortafolio;
			mvto.IdTipoComprobante = mvtoEntrada.IdTipoComprobante;
			mvto.IdTipoOperacion = mvtoEntrada.IdTipoOperacion;
			mvto.IdTipoProducto = mvtoEntrada.IdTipoProducto;
			mvto.IdTipoReajuste = mvtoEntrada.IdTipoReajuste;
			mvto.IdTipoConvenio = mvtoEntrada.IdTipoConvenio;
			mvto.IdTipoCanalIngreso = mvtoEntrada.IdTipoCanalIngreso;
			mvto.IdTipoCliente = mvtoEntrada.IdTipoCliente;
			mvto.IdTipoLiquidacionComisionFactura = "S".equals(paramLiquidacionComisionFact) ? mvtoEntrada.IdTipoLiquidacionComisionFactura : "";
			mvto.IdTransaccion = mvtoEntrada.IdTransaccion;
			mvto.IdUsuario = mvtoEntrada.IdUsuario;
			mvto.LiquidaBancoCustodio = mvtoEntrada.LiquidaBancoCustodio;
			mvto.MercadoSMV = mvtoEntrada.MercadoSMV;
			mvto.ModalidadSMV = mvtoEntrada.ModalidadSMV;
			mvto.ModPrefFact = mvtoEntrada.ModPrefFact;
			mvto.Monto = mvtoEntrada.Monto;
			mvto.MontoRaiz = mvtoEntrada.MontoRaiz != null && mvtoEntrada.MontoRaiz.compareTo(BigDecimal.ZERO) != 0 ? mvtoEntrada.MontoRaiz : mvtoEntrada.Monto;
			mvto.MontoMovimientoComprobante = mvtoEntrada.MontoMovimientoComprobante;
			mvto.MontoLiquidacion = mvtoEntrada.MontoLiquidacion;
			mvto.MontoDescuento = mvtoEntrada.MontoDescuento;
			mvto.MontoInteres = mvtoEntrada.MontoInteres;
			mvto.MontoAPlazo = mvtoEntrada.MontoAPlazo;
			mvto.NumeroMvtoCmbte = mvtoEntrada.NumeroMvtoCmbte;	//CORRELATIVO
			mvto.NemotecnicoInstrumento = mvtoEntrada.NemotecnicoInstrumento;
			mvto.NemoSVS = mvtoEntrada.NemoSVS;
			mvto.Precio = mvtoEntrada.Precio;
			mvto.PrecioRaiz = mvtoEntrada.PrecioRaiz != null && mvtoEntrada.PrecioRaiz.compareTo(BigDecimal.ZERO) != 0 ? mvtoEntrada.PrecioRaiz : mvtoEntrada.Precio;
			mvto.PrecioAPlazo = mvtoEntrada.PrecioAPlazo;
			mvto.ReajustesVAC = mvtoEntrada.ReajustesVAC;
			mvto.SecuenciaAgrupacion = mvtoEntrada.SecuenciaAgrupacion;
			mvto.Spread = mvtoEntrada.Spread;
			mvto.Tasa = mvtoEntrada.Tasa;
			mvto.TasaAgente = mvtoEntrada.TasaAgente;
			mvto.TasaEmision = mvtoEntrada.TasaEmision;
			mvto.TasaOperacion = mvtoEntrada.TasaOperacion;
			mvto.ValorRescate = mvtoEntrada.ValorRescate;
			mvto.ValorParidad = mvtoEntrada.ValorParidad;
			mvto.ValorTipoCambio = mvtoEntrada.ValorTipoCambio;
			mvto.ValorCaptacionMonedaOrigen = mvtoEntrada.ValorCaptacionMonedaOrigen;
			mvto.CodigoIFRS =  mvtoEntrada.CodigoIFRS;
			mvto.IdOperacion = mvtoEntrada.IdOperacion != null? mvtoEntrada.IdOperacion : 0;
			mvto.IdMonedaSpot = mvtoEntrada.IdMonedaSpot != null? mvtoEntrada.IdMonedaSpot : "";
			
			movimientos.add(mvto);
			hashMovimientos.put(mvto.NumeroMvtoCmbte, mvto);
		}
		log.debug("Construir cobros");
		for (MovimientoComprobanteCobro_Entrada mvtoEntradaCobro : in.p_MovimientosCobros) {
			MovimientoComprobanteCobro_Proceso mvtoCobro = new MovimientoComprobanteCobro_Proceso();
			
			mvtoCobro.NumeroMvtoCmbte = mvtoEntradaCobro.NumeroMvtoCmbte;
			mvtoCobro.NumeroMvtoCmbteCobro = mvtoEntradaCobro.NumeroMvtoCmbteCobro;
			mvtoCobro.IdCobro = mvtoEntradaCobro.IdCobro;
			mvtoCobro.IdMonedaCobro = mvtoEntradaCobro.IdMonedaCobro;
			mvtoCobro.IdMonedaLiquidacionCobro = mvtoEntradaCobro.IdMonedaLiquidacionCobro;
			mvtoCobro.MontoCobro = mvtoEntradaCobro.MontoCobro;
			mvtoCobro.MontoMovimientoComprobanteCobro = mvtoEntradaCobro.MontoMovimientoComprobanteCobro;
			mvtoCobro.MontoLiquidacionCobro = mvtoEntradaCobro.MontoLiquidacionCobro;
			mvtoCobro.Porcentaje = mvtoEntradaCobro.Porcentaje;
			
			movimientosCobro.add(mvtoCobro);
		}
		log.debug("Construir formas de pago");
		for (MovimientoComprobanteFormaDePago_Entrada mvtoEntradaFormaDePago : in.p_MovimientosFormaDePago) {
			MovimientoComprobanteFormaDePago_Proceso mvtoFormaDePago = new MovimientoComprobanteFormaDePago_Proceso();
			
			mvtoFormaDePago.NumeroMvtoCmbte = mvtoEntradaFormaDePago.NumeroMvtoCmbte;
			mvtoFormaDePago.NumeroMvtoCmbteFormaDePago = mvtoEntradaFormaDePago.NumeroMvtoCmbteFormaDePago;
			mvtoFormaDePago.IdMedioDePago = mvtoEntradaFormaDePago.IdMedioDePago;
			mvtoFormaDePago.NumeroSerie = mvtoEntradaFormaDePago.NumeroSerie;
			mvtoFormaDePago.NumeroDocumento = mvtoEntradaFormaDePago.NumeroDocumento;
			mvtoFormaDePago.IdCuentaBancaria = mvtoEntradaFormaDePago.IdCuentaBancaria;
			mvtoFormaDePago.IdTipoCuentaBancaria = mvtoEntradaFormaDePago.IdTipoCuentaBancaria;
			mvtoFormaDePago.NumeroCuentaBancaria = mvtoEntradaFormaDePago.NumeroCuentaBancaria;
			mvtoFormaDePago.IdTarjetaBancaria = mvtoEntradaFormaDePago.IdTarjetaBancaria;
			mvtoFormaDePago.IdTipoTarjetaBancaria = mvtoEntradaFormaDePago.IdTipoTarjetaBancaria;
			mvtoFormaDePago.NumeroTarjetaBancaria = mvtoEntradaFormaDePago.NumeroTarjetaBancaria;
			mvtoFormaDePago.FechaVctoTarjetaBancaria = Fecha.toDate(mvtoEntradaFormaDePago.FechaVctoTarjetaBancaria);
			mvtoFormaDePago.IdModalidadDePago = mvtoEntradaFormaDePago.IdModalidadDePago;
			mvtoFormaDePago.IdBanco = mvtoEntradaFormaDePago.IdBanco;
			mvtoFormaDePago.IdMoneda = mvtoEntradaFormaDePago.IdMoneda;
			mvtoFormaDePago.FechaDocumentoPago = Fecha.toDate(mvtoEntradaFormaDePago.FechaDocumentoPago);
			mvtoFormaDePago.MontoLiquidacionFormaDePago = mvtoEntradaFormaDePago.MontoLiquidacionFormaDePago;
			mvtoFormaDePago.IdTipoMovimientoSpot = mvtoEntradaFormaDePago.IdTipoMovimientoSpot;
			mvtoFormaDePago.AfectarTesoreria = mvtoEntradaFormaDePago.AfectarTesoreria;
			mvtoFormaDePago.AfectarCuentaInversion = mvtoEntradaFormaDePago.AfectarCuentaInversion;
			
			movimientosFormaDePago.add(mvtoFormaDePago);
		}
		
		//VALIDACIONES
		//CAMPOS OBLIGATORIOS
		log.debug("validaciones cmpos obligatorios");
		for (MovimientoComprobante_Proceso mvto : movimientos) {
			ProcesaRequisito(conn, in.p_IdEmpresa,(mvto.IdEmpresa == null || mvto.IdEmpresa.equals(0L)), "MVTO_CMBTE_EMPRESA_INVALIDA", MensajeSalida.MENSAJE_ERROR, mvto, salida.P_OUT_MENSAJES);
			ProcesaRequisito(conn, in.p_IdEmpresa,(mvto.IdConceptoMovimiento == null || mvto.IdConceptoMovimiento.trim().length() == 0), "MVTO_CMBTE_CONCEPTO_FACTURACION_INVALIDO", MensajeSalida.MENSAJE_ERROR, mvto, salida.P_OUT_MENSAJES);
			ProcesaRequisito(conn, in.p_IdEmpresa,(mvto.IdPortafolio == null || mvto.IdPortafolio.equals(0L)), "MVTO_CMBTE_PORTAFOLIO_INVALIDO", MensajeSalida.MENSAJE_ERROR, mvto, salida.P_OUT_MENSAJES);
			ProcesaRequisito(conn, in.p_IdEmpresa,(mvto.IdTipoComprobante == null || mvto.IdTipoComprobante.trim().length() == 0), "MVTO_CMBTE_TIPO_COMPROBANTE_INVALIDO", MensajeSalida.MENSAJE_ERROR, mvto, salida.P_OUT_MENSAJES);
			ProcesaRequisito(conn, in.p_IdEmpresa,(mvto.IdCondicionLiquidacion == null || mvto.IdCondicionLiquidacion.trim().length() == 0), "MVTO_CMBTE_CONDICION_DE_LIQUIDACION_INVALIDO", MensajeSalida.MENSAJE_ERROR, mvto, salida.P_OUT_MENSAJES);
			ProcesaRequisito(conn, in.p_IdEmpresa,(mvto.IdTipoProducto == null|| mvto.IdCondicionLiquidacion.trim().length() == 0), "MVTO_CMBTE_TIPO_PRODUCTO_NO_EXISTE", MensajeSalida.MENSAJE_ERROR, mvto, salida.P_OUT_MENSAJES);
			
			//REVISAR SI FECHA DE LIQUIDACION PUEDE NO LLEGAR
			ProcesaRequisito(conn, in.p_IdEmpresa,(mvto.FechaLiquidacion == null), "MVTO_CMBTE_FECHA_LIQUIDACION_INVALIDA", MensajeSalida.MENSAJE_ERROR, mvto, salida.P_OUT_MENSAJES);
			
			ProcesaRequisito(conn, in.p_IdEmpresa,("INIF".equalsIgnoreCase(mvto.IdTipoProducto) && (mvto.IdTipoReajuste == null || mvto.IdTipoReajuste.trim().length() == 0)), "MVTO_CMBTE_TIPO_REAJUSTE_INVALIDO", MensajeSalida.MENSAJE_ERROR, mvto, salida.P_OUT_MENSAJES);
			ProcesaRequisito(conn, in.p_IdEmpresa,(mvto.IdTipoConvenio == null || mvto.IdTipoConvenio.trim().length() == 0), "MVTO_CMBTE_TIPO_CONVENIO_INVALIDO", MensajeSalida.MENSAJE_ERROR, mvto, salida.P_OUT_MENSAJES);
			ProcesaRequisito(conn, in.p_IdEmpresa,(("INSI".equalsIgnoreCase(mvto.IdTipoProducto) || mvto.EsVentaCorta.equalsIgnoreCase("S")) && mvto.FechaVencimiento == null), "MVTO_CMBTE_FECHA_VENCIMIENTO_INVALIDA", MensajeSalida.MENSAJE_ERROR, mvto, salida.P_OUT_MENSAJES);
			
			ProcesaRequisito(conn, in.p_IdEmpresa,(mvto.MontoMovimientoComprobante == null || mvto.MontoMovimientoComprobante.compareTo(BigDecimal.ZERO) == -1), "MVTO_CMBTE_MONTO_MOVIMIENTO_MENOR_IGUAL_A_CERO", MensajeSalida.MENSAJE_ERROR, mvto, salida.P_OUT_MENSAJES);
			ProcesaRequisito(conn, in.p_IdEmpresa,(mvto.FechaMovimientoComprobante == null), "MVTO_CMBTE_FECHA_MOVIMIENTO_COMPROBANTE_INVALIDO", MensajeSalida.MENSAJE_ERROR, mvto, salida.P_OUT_MENSAJES);
			ProcesaRequisito(conn, in.p_IdEmpresa,(mvto.EsComprobIndividual == null || ( !mvto.EsComprobIndividual.equalsIgnoreCase("S") && !mvto.EsComprobIndividual.equalsIgnoreCase("N")) ), "MVTO_CMBTE_IND_COMPROBANTE_INDIVIDUAL_INVALIDO", MensajeSalida.MENSAJE_ERROR, mvto, salida.P_OUT_MENSAJES);
			ProcesaRequisito(conn, in.p_IdEmpresa,(mvto.EsCuentaPropia == null || ( !mvto.EsCuentaPropia.equalsIgnoreCase("S") && !mvto.EsCuentaPropia.equalsIgnoreCase("N")) ), "MVTO_CMBTE_IND_CUENTA_PROPIA_INVALIDO", MensajeSalida.MENSAJE_ERROR, mvto, salida.P_OUT_MENSAJES);
			ProcesaRequisito(conn, in.p_IdEmpresa,(mvto.EsVentaCorta == null || ( !mvto.EsVentaCorta.equalsIgnoreCase("S") && !mvto.EsVentaCorta.equalsIgnoreCase("N")) ), "MVTO_CMBTE_IND_VENTA_CORTA_INVALIDO", MensajeSalida.MENSAJE_ERROR, mvto, salida.P_OUT_MENSAJES);
			ProcesaRequisito(conn, in.p_IdEmpresa,(mvto.EsPrecioPrima == null || ( !mvto.EsPrecioPrima.equalsIgnoreCase("S") && !mvto.EsPrecioPrima.equalsIgnoreCase("N")) ), "MVTO_CMBTE_IND_PRECIO_PRIMA_INVALIDO", MensajeSalida.MENSAJE_ERROR, mvto, salida.P_OUT_MENSAJES);
			ProcesaRequisito(conn, in.p_IdEmpresa,(mvto.EsEnRueda == null || ( !mvto.EsEnRueda.equalsIgnoreCase("S") && !mvto.EsEnRueda.equalsIgnoreCase("N")) ), "MVTO_CMBTE_IND_EN_RUEDA_INVALIDO", MensajeSalida.MENSAJE_ERROR, mvto, salida.P_OUT_MENSAJES);
			ProcesaRequisito(conn, in.p_IdEmpresa,(mvto.EsRuteo == null || ( !mvto.EsRuteo.equalsIgnoreCase("S") && !mvto.EsRuteo.equalsIgnoreCase("N")) ), "MVTO_CMBTE_IND_RUTEO_INVALIDO", MensajeSalida.MENSAJE_ERROR, mvto, salida.P_OUT_MENSAJES);
			ProcesaRequisito(conn, in.p_IdEmpresa,(mvto.EsAPlazo == null || ( !mvto.EsAPlazo.equalsIgnoreCase("S") && !mvto.EsAPlazo.equalsIgnoreCase("N")) ), "MVTO_CMBTE_IND_A_PLAZO_INVALIDO", MensajeSalida.MENSAJE_ERROR, mvto, salida.P_OUT_MENSAJES);
			ProcesaRequisito(conn, in.p_IdEmpresa,(mvto.EsFungible == null || ( !mvto.EsFungible.equalsIgnoreCase("S") && !mvto.EsFungible.equalsIgnoreCase("N")) ), "MVTO_CMBTE_IND_FUNGIBLE_INVALIDO", MensajeSalida.MENSAJE_ERROR, mvto, salida.P_OUT_MENSAJES);
			ProcesaRequisito(conn, in.p_IdEmpresa,(mvto.ActualizarCustodia == null || (!mvto.ActualizarCustodia.equalsIgnoreCase("S") && !mvto.ActualizarCustodia.equalsIgnoreCase("N")) ), "MVTO_CMBTE_IND_ACTUALIZAR_CUSTODIA_INVALIDO", MensajeSalida.MENSAJE_ERROR, mvto, salida.P_OUT_MENSAJES);
			ProcesaRequisito(conn, in.p_IdEmpresa,(mvto.ActualizarCuentaInversion == null || (!mvto.ActualizarCuentaInversion.equalsIgnoreCase("S") && !mvto.ActualizarCuentaInversion.equalsIgnoreCase("N")) ), "MVTO_CMBTE_IND_ACTUALIZAR_CUENTA_INVERSION_INVALIDO", MensajeSalida.MENSAJE_ERROR, mvto, salida.P_OUT_MENSAJES);
			ProcesaRequisito(conn, in.p_IdEmpresa,(mvto.ActualizarTesoreria == null || (!mvto.ActualizarTesoreria.equalsIgnoreCase("S") && !mvto.ActualizarTesoreria.equalsIgnoreCase("N")) ), "MVTO_CMBTE_IND_ACTUALIZAR_TESORERIA_INVALIDO", MensajeSalida.MENSAJE_ERROR, mvto, salida.P_OUT_MENSAJES);
			ProcesaRequisito(conn, in.p_IdEmpresa,(mvto.ActualizarCustodiaRelacionado == null || (!mvto.ActualizarCustodiaRelacionado.equalsIgnoreCase("S") && !mvto.ActualizarCustodiaRelacionado.equalsIgnoreCase("N")) ), "MVTO_CMBTE_IND_ACTUALIZAR_CUSTODIA_RELACIONADO_INVALIDO", MensajeSalida.MENSAJE_ERROR, mvto, salida.P_OUT_MENSAJES);
			
		}
		log.debug("limpiar movimientos");
		limpiarMovimientos(movimientos,movimientosConError,salida.P_OUT_MENSAJES,salida.p_out_ResultadoProceso,conn);
		if (movimientos.size() == 0 || (SI.equalsIgnoreCase(in.p_EsCargaMasiva) && movimientosConError.size() > 0)) {
			salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
			return salida;
		}
		log.debug("insert movimientos comprobante proceso");
		_sql.batch(conn, SQL_INSERT_MOVIMIENTO_COMPROBANTE_PROCESO, movimientos, MovimientoComprobante_Proceso.class);
		log.debug("select movimiento comprobante");
		List<InformacionMovimientoComprobante> _infoMovimientos = _sql.query(conn, SQL_SELECT_MOVIMIENTO_COMPROBANTE, new HashMap<>(), InformacionMovimientoComprobante.class);
		HashMap<Long,Object> infoMovimientos = Utils.List2Hash(_infoMovimientos, "NumeroMvtoCmbte");
		
		String estadoModulo = ConsultarEstadoModulo(in.p_IdEmpresa, in.p_IdUsuario, conn);
		log.debug("validar movimiento comprobante");
		for (MovimientoComprobante_Proceso mvto : movimientos) {
			InformacionMovimientoComprobante infoMvto = (InformacionMovimientoComprobante) infoMovimientos.get(mvto.NumeroMvtoCmbte);
			if((mvto.IdTipoOperacion.equals("VENEMP")||mvto.IdTipoOperacion.equals("VENPRT"))&& in.p_EsPauta) {
				
			}else {
				ProcesaRequisito(conn, in.p_IdEmpresa, MODULO_ESTADO_CERRADO.equals(estadoModulo), "MODULO_CERRADO", MensajeSalida.MENSAJE_ERROR, mvto, salida.P_OUT_MENSAJES);
			};
			
			ProcesaRequisito(conn, in.p_IdEmpresa, infoMvto.IdConceptoReal == null , "MVTO_CMBTE_CONCEPTO_NO_EXISTE", MensajeSalida.MENSAJE_ERROR, mvto, salida.P_OUT_MENSAJES);
			ProcesaRequisito(conn, in.p_IdEmpresa, infoMvto.IdTipoComprobanteReal == null , "MVTO_CMBTE_TIPO_COMPROBANTE_NO_EXISTE",MensajeSalida.MENSAJE_ERROR, mvto, salida.P_OUT_MENSAJES);
			ProcesaRequisito(conn, in.p_IdEmpresa, infoMvto.IdCondicionDeLiquidacionReal == null , "MVTO_CMBTE_CONDICION_DE_LIQUIDACION_NO_EXISTE", MensajeSalida.MENSAJE_ERROR, mvto, salida.P_OUT_MENSAJES);
			ProcesaRequisito(conn, in.p_IdEmpresa, mvto.IdTipoReajuste != null && mvto.IdTipoReajuste.length() > 0 && infoMvto.IdTipoReajusteReal == null , "MVTO_CMBTE_TIPO_REAJUSTE_NO_EXISTE", MensajeSalida.MENSAJE_ERROR, mvto, salida.P_OUT_MENSAJES);
			ProcesaRequisito(conn, in.p_IdEmpresa, mvto.IdTipoConvenio != null && mvto.IdTipoConvenio.length() > 0 && infoMvto.IdTipoConvenioReal == null , "MVTO_CMBTE_TIPO_CONVENIO_NO_EXISTE", MensajeSalida.MENSAJE_ERROR, mvto, salida.P_OUT_MENSAJES);
			ProcesaRequisito(conn, in.p_IdEmpresa, mvto.IdEmisor != null && mvto.IdEmisor.length() > 0 && infoMvto.IdEmisorReal == null , "MVTO_CMBTE_EMISOR_NO_EXISTE", MensajeSalida.MENSAJE_ERROR, mvto, salida.P_OUT_MENSAJES);
			ProcesaRequisito(conn, in.p_IdEmpresa, mvto.IdMonedaLiquidacion != null && mvto.IdMonedaLiquidacion.length() > 0 && infoMvto.IdMonedaLiquidacionReal == null , "MVTO_CMBTE_MONEDA_LIQUIDACION_NO_EXISTE", MensajeSalida.MENSAJE_ERROR, mvto, salida.P_OUT_MENSAJES);
			ProcesaRequisito(conn, in.p_IdEmpresa, infoMvto.IdEstadoMonedaLiquidacion != null && !infoMvto.IdEstadoMonedaLiquidacion.equalsIgnoreCase("ACT") , "MVTO_CMBTE_MONEDA_LIQUIDACION_NO_ACTIVA", MensajeSalida.MENSAJE_ERROR, mvto, salida.P_OUT_MENSAJES);
			
			ProcesaRequisito(conn, in.p_IdEmpresa, mvto.IdInstrumento != null && mvto.IdInstrumento > 0 && infoMvto.IdInstrumentoReal == null , "MVTO_CMBTE_INSTRUMENTO_NO_EXISTE", MensajeSalida.MENSAJE_ERROR, mvto, salida.P_OUT_MENSAJES);
			ProcesaRequisito(conn, in.p_IdEmpresa, mvto.IdInstrumentoRelacionado != null && mvto.IdInstrumentoRelacionado > 0 && infoMvto.IdInstrumentoRelacionadoReal == null , "MVTO_CMBTE_INSTRUMENTO_RELACIONADO_NO_EXISTE", MensajeSalida.MENSAJE_ERROR, mvto, salida.P_OUT_MENSAJES);
			ProcesaRequisito(conn, in.p_IdEmpresa, mvto.IdInstrumentoSubyacente != null && mvto.IdInstrumentoSubyacente > 0 && infoMvto.IdInstrumentoSubyacenteReal == null , "MVTO_CMBTE_INSTRUMENTO_SUBYACENTE_NO_EXISTE", MensajeSalida.MENSAJE_ERROR, mvto, salida.P_OUT_MENSAJES);
			ProcesaRequisito(conn, in.p_IdEmpresa, mvto.IdEmpleadoDigitador != null && mvto.IdEmpleadoDigitador > 0 && infoMvto.IdEmpleadoDigitadorReal == null , "MVTO_CMBTE_EMPLEADO_DIGITADOR_INVALIDO", MensajeSalida.MENSAJE_ERROR, mvto, salida.P_OUT_MENSAJES);
			ProcesaRequisito(conn, in.p_IdEmpresa, mvto.IdTipoProducto != null && mvto.IdTipoProducto.length() > 0 && infoMvto.IdTipoProductoReal == null, "MVTO_CMBTE_TIPO_PRODUCTO_NO_EXISTE", MensajeSalida.MENSAJE_ERROR, mvto, salida.P_OUT_MENSAJES);
			ProcesaRequisito(conn, in.p_IdEmpresa, mvto.IdMoneda != null && mvto.IdMoneda.length() > 0 && infoMvto.IdMonedaMovimientoComprobanteReal == null , "MVTO_CMBTE_MONEDA_NO_EXISTE", MensajeSalida.MENSAJE_ERROR, mvto, salida.P_OUT_MENSAJES);
			ProcesaRequisito(conn, in.p_IdEmpresa, infoMvto.IdEstadoMonedaMovimientoComprobante != null && !infoMvto.IdEstadoMonedaMovimientoComprobante.equals("ACT") , "MVTO_CMBTE_MONEDA_NO_ACTIVA", MensajeSalida.MENSAJE_ERROR, mvto, salida.P_OUT_MENSAJES);
			
			if( !("DIEVAC".equalsIgnoreCase(mvto.IdConceptoMovimiento)||"DIACGS".equalsIgnoreCase(mvto.IdConceptoMovimiento)) && !("EMILIB".equalsIgnoreCase(mvto.IdConceptoMovimiento)) ) {
				
				ProcesaRequisito(conn, in.p_IdEmpresa, mvto.IdMoneda != null && mvto.IdMoneda.length() > 0 && mvto.Monto.compareTo(BigDecimal.ZERO) <= 0 , "MVTO_CMBTE_MONTO_MENOR_IGUAL_A_CERO", MensajeSalida.MENSAJE_ERROR, mvto, salida.P_OUT_MENSAJES);
				ProcesaRequisito(conn, in.p_IdEmpresa, mvto.IdMoneda != null && mvto.IdMoneda.length() > 0 && mvto.MontoLiquidacion.compareTo(BigDecimal.ZERO) <= 0 , "MVTO_CMBTE_MONTO_LIQUIDACION_MENOR_IGUAL_A_CERO", MensajeSalida.MENSAJE_ERROR, mvto, salida.P_OUT_MENSAJES);
			}
			
			ProcesaRequisito(conn, in.p_IdEmpresa, mvto.IdMonedaSubyacente != null && mvto.IdMonedaSubyacente.length() > 0 && infoMvto.IdMonedaSubyacenteReal == null , "MVTO_CMBTE_MONEDA_SUBYACENTE_NO_EXISTE", MensajeSalida.MENSAJE_ERROR, mvto, salida.P_OUT_MENSAJES);
			ProcesaRequisito(conn, in.p_IdEmpresa, infoMvto.IdEstadoMonedaSubyacente != null && !infoMvto.IdEstadoMonedaSubyacente.equals("ACT") , "MVTO_CMBTE_MONEDA_SUBYACENTE_NO_ACTIVA", MensajeSalida.MENSAJE_ERROR, mvto, salida.P_OUT_MENSAJES);
			ProcesaRequisito(conn, in.p_IdEmpresa, mvto.IdTipoCanalIngreso != null && mvto.IdTipoCanalIngreso.length() > 0 && infoMvto.IdTipoCanalIngresoReal == null, "MVTO_CMBTE_TIPO_CANAL_INGRESO_NO_EXISTE", MensajeSalida.MENSAJE_ERROR, mvto, salida.P_OUT_MENSAJES);
			ProcesaRequisito(conn, in.p_IdEmpresa, mvto.IdTipoOperacion != null && mvto.IdTipoOperacion.length() > 0 && infoMvto.IdTipoOperacionReal == null , "MVTO_CMBTE_TIPO_OPERACION_NO_EXISTE", MensajeSalida.MENSAJE_ERROR, mvto, salida.P_OUT_MENSAJES);
			ProcesaRequisito(conn, in.p_IdEmpresa, mvto.IdBoveda != null && mvto.IdBoveda.length() > 0 && infoMvto.IdBovedaReal == null , "MVTO_CMBTE_BOVEDA_NO_EXISTE", MensajeSalida.MENSAJE_ERROR, mvto, salida.P_OUT_MENSAJES);
			ProcesaRequisito(conn, in.p_IdEmpresa, mvto.IdCorredoraContraparte != null && mvto.IdCorredoraContraparte > 0 && infoMvto.IdCorredoraContraparteReal == null , "MVTO_CMBTE_CORREDOR_NO_EXISTE", MensajeSalida.MENSAJE_ERROR, mvto, salida.P_OUT_MENSAJES);
			ProcesaRequisito(conn, in.p_IdEmpresa, mvto.IdBolsa != null && mvto.IdBolsa.length() > 0 && infoMvto.IdBolsaReal == null , "MVTO_CMBTE_BOLSA_NO_EXISTE", MensajeSalida.MENSAJE_ERROR, mvto, salida.P_OUT_MENSAJES);
			ProcesaRequisito(conn, in.p_IdEmpresa, mvto.IdOrden != null && mvto.IdOrden > 0 && infoMvto.IdOrdenReal == null , "MVTO_CMBTE_ORDEN_NO_EXISTE", MensajeSalida.MENSAJE_ERROR, mvto, salida.P_OUT_MENSAJES);
			
			ProcesaRequisito(conn, in.p_IdEmpresa, infoMvto.IdClasificacionTipoInstrumento != null && infoMvto.IdClasificacionTipoInstrumento.equalsIgnoreCase("RF") && infoMvto.CorteMinimo.doubleValue() != 0 && mvto.Cantidad.doubleValue() % infoMvto.CorteMinimo.doubleValue() != 0  , "MVTO_CMBTE_CORTE_MINIMO", MensajeSalida.MENSAJE_ERROR, mvto, salida.P_OUT_MENSAJES);
			
			mvto.IdMovimientoComprobante = infoMvto.IdMovimientoComprobante;
			if (mvto.ModPrefFact == null && infoMvto.ValorModoFacturacion != null) {
				mvto.ModPrefFact = infoMvto.ValorModoFacturacion == "" ? "MODPREFNORM": infoMvto.ValorModoFacturacion;
			}
			
			mvto.IdTipoInstrumento = infoMvto.IdTipoInstrumento;
			mvto.NombreTipoInstrumento = infoMvto.NombreTipoInstrumento;
			if (mvto.NemotecnicoInstrumento != null && mvto.NemotecnicoInstrumento.length() > 0) {
				//HACER NADA
			}
			else {
				mvto.NemotecnicoInstrumento = infoMvto.NemotecnicoInstrumento;
			}

			mvto.CorteMinimo = infoMvto.CorteMinimo != null ? infoMvto.CorteMinimo : BigDecimal.ZERO;
			mvto.CodigoCanalIngreso = infoMvto.CodigoTipoCanalIngreso;
			mvto.CodigoTipoProducto = infoMvto.CodigoTipoProducto;
			mvto.CodigoTipoOperacion = infoMvto.CodigoTipoOperacion;
			mvto.CodigoBolsa = infoMvto.CodigoBolsa;
			
			mvto.DireccionFacturacion = "PENDIENTE";
			mvto.DocumentoIdentidad = infoMvto.DocumentoIdentidad;
			
			mvto.EsIngresoAutomatico = in.p_EsIngresoAutomatico;
			mvto.FechaSistema = Fecha.toDateTime(fechaSistema + " 00:00:00");
			
			mvto.IdUsuarioBloqueo = 0L;
			mvto.IdCarga = 0L;
			mvto.IdEstadoFacturacion = "ING";
			mvto.IdEstadoCavali = "NEV"; //NO ENVIADO
			mvto.IdMovimientoOdRelacionado = 0L;
			mvto.IdTipoInstrumentoRelacionado = infoMvto.IdTipoInstrumentoInstrumentoRelacionado;
			mvto.IdTipoInstrumentoSubyacente = infoMvto.IdTipoInstrumentoInstrumentoSubyacente;
			mvto.IdClasificacionTipoInstrumento = infoMvto.IdClasificacionTipoInstrumento;
			mvto.IdClasificacionTipoInstrumentoRelacionado = infoMvto.IdClasificacionTipoInstrumentoInstrumentoRelacionado;
			mvto.IdMonedaMovimientoComprobante = monedaSistema;
			mvto.IdComprobante = 0L;
			mvto.IdComprobanteReferencia = infoMvto.IdComprobanteReferencia;
			mvto.IdTipoComprobanteReferencia = infoMvto.IdTipoComprobanteReferencia;
			mvto.IdSucursalEmpleadoDigitador = infoMvto.IdSucursalEmpleadoDigitador;
			mvto.IdEmpleadoEjecutivoPortafolio = infoMvto.IdEmpleadoPortafolio;
			mvto.IdSucursalEmpleadoEjecutivoPortafolio = infoMvto.IdSucursalEmpleadoPortafolio;
			if (mvto.IdTipoInstrumento == null || mvto.IdClasificacionTipoInstrumento == null) {
				mvto.ISIN = "";
			}
			else if ((mvto.IdTipoInstrumento.equalsIgnoreCase("SIM") && mvto.IdClasificacionTipoInstrumento.equalsIgnoreCase("RV"))
					|| mvto.IdClasificacionTipoInstrumento.equalsIgnoreCase("RE")) {
				mvto.ISIN = infoMvto.ISINInstrumentoSubyacente;
			}
			else {
				mvto.ISIN = infoMvto.ISIN;
			}
			
			
			mvto.FolioComprobante = 0L;
			mvto.FolioComprobanteReferencia = 0L;
			
			mvto.NemotecnicoRelacionado = infoMvto.NemotecnicoInstrumentoRelacionado;
			mvto.NemotecnicoSubyacente = infoMvto.NemotecnicoInstrumentoSubyacente;
			mvto.NombreClasificacionTipoInstrumento = infoMvto.NombreClasificacionTipoInstrumento;
			mvto.NombreClasificacionTipoInstrumentoRelacionado = infoMvto.NombreClasificacionTipoInstrumentoRelacionado;
			mvto.NombreEmpleadoDigitador = infoMvto.NombreCompletoEmpleadoDigitador;
			mvto.NombreEmpleadoEjecutivoPortafolio = infoMvto.NombreCompletoEmpleadoPortafolio;
			mvto.NombreLargoCliente = infoMvto.NombreCompleto;
			mvto.NombreTipoInstrumentoRelacionado = infoMvto.NombreTipoInstrumentoRelacionado;
			mvto.NombreTipoInstrumentoSubyacente = infoMvto.NombreTipoInstrumentoSubyacente;
			mvto.NombreSucursalEmpleadoDigitador = infoMvto.NombreSucursalEmpleadoDigitador;
			mvto.NombreSucursalEmpleadoEjecutivoPortafolio = infoMvto.NombreSucursalEmpleadoPortafolio;
			mvto.NombreTipoCliente = infoMvto.NombreTipoCliente;
			mvto.RazonSocialEmisor = infoMvto.NombreCompletoEmisor;
			
			mvto.TipoDocumentoIdentidad = infoMvto.TipoDocumentoIdentidad;
			
			mvto.IdComprobanteReferencia = infoMvto.IdComprobanteReferencia;
			mvto.FolioComprobanteReferencia = infoMvto.FolioComprobanteReferencia;
			mvto.FechaComprobanteReferencia = infoMvto.FechaComprobanteReferencia;
			mvto.IdTipoComprobanteReferencia = infoMvto.IdTipoComprobanteReferencia;
			mvto.Contrapartida = infoMvto.Contrapartida;

		}
		
		List<ReferenciaTipoDocumento> listaReferenciaTipoDocumento = new ArrayList<>();
		if (in.p_Movimientos.size() > 0) {
			for(int i=0; i<in.p_Movimientos.size(); i++) {
				String inReferencia = in.p_Movimientos.get(i).Referencia != null? in.p_Movimientos.get(i).Referencia.trim(): "";
				String inTipoReferencia = in.p_Movimientos.get(i).TipoReferencia != null? in.p_Movimientos.get(i).TipoReferencia.trim(): "";
				String inIdMovimientoComprobante = movimientos.get(i).IdMovimientoComprobante.toString();
				if (inReferencia.length() < 1 || inTipoReferencia.length() < 1) continue;
				ReferenciaTipoDocumento referenciaTipoDocumento = new ReferenciaTipoDocumento();
				referenciaTipoDocumento.Referencia = inReferencia;
				referenciaTipoDocumento.TipoReferencia = inTipoReferencia;
				referenciaTipoDocumento.IdMovimientoComprobante = inIdMovimientoComprobante;
				referenciaTipoDocumento.IdComprobante = "";
				listaReferenciaTipoDocumento.add(referenciaTipoDocumento);
			}	
		}
		
		log.debug("Preparar Informacion Log");
		List<Long> idClientes = new ArrayList<>();
		List<Long> idPortafolios = new ArrayList<>();
		
		for (MovimientoComprobante_Entrada mvtComp : in.p_Movimientos) {
			
			if(mvtComp.IdCliente != null && mvtComp.IdCliente != 0L) {
				idClientes.add(mvtComp.IdCliente);
			}
			if(mvtComp.IdPortafolio != null && mvtComp.IdPortafolio != 0L) {
				idPortafolios.add(mvtComp.IdPortafolio);
			}
		}
		
		HashMap<String,Object> paramsCliente = new HashMap<>();
		paramsCliente.put("IdCliente", idClientes);
		List<ClientePersona> selectClientePersona = _sql.query(conn, SQL_SELECT_PERSONA_CLIENTE, paramsCliente, ClientePersona.class);
		HashMap<String,Object> hCliente = Utils.List2Hash(selectClientePersona, "IdCliente");
		
		HashMap<String,Object> paramsPortafolio = new HashMap<>();
		paramsPortafolio.put("IdPortafolio", idPortafolios);
		List<Portafolio> selectPortafolio = _sql.query(conn, SQL_SELECT_PORTAFOLIO, paramsPortafolio, Portafolio.class);
		HashMap<String,Object> hPortafolio = Utils.List2Hash(selectPortafolio, "IdPortafolio");

		//AGREGAR LOG
		//CABECERA
		log.debug("Inicio PREPARAR LOG");
		AgregarLog agregarLogService = new AgregarLog();
		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
		Long IdTrabajo = (long)(Math.random()*10000);
		
		for (MovimientoComprobante_Entrada mvtoEntrada : in.p_Movimientos) {
			
			ClientePersona clientPerso = (ClientePersona) hCliente.get(mvtoEntrada.IdCliente);
			Portafolio portaf = (Portafolio) hPortafolio.get(mvtoEntrada.IdPortafolio);
			
			AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
			entradaAgregarLog.p_IdTrabajo = IdTrabajo;
			log.IdTrabajo = entradaAgregarLog.p_IdTrabajo;
			log.IdCabeceraLog = mvtoEntrada.NumeroMvtoCmbte;
			log.IdTipoLog = "INGRESO";
			log.IdModulo = "FAC";
			log.IdUsuario = in.p_IdUsuario;
			log.IdEmpresa = in.p_IdEmpresa;
			log.FechaSistemaLog = fechaSistema;
			log.IdUsuarioAtendido = in.p_IdUsuario;
			log.IdCliente = mvtoEntrada.IdCliente;
			if(clientPerso != null) {
				log.EstadoCliente = clientPerso.Estado;
				log.NombreCliente = clientPerso.Nombres + " " + clientPerso.ApellidoPaterno + " " + clientPerso.ApellidoMaterno;
				log.IdTipoDocumentoIdentidad = clientPerso.TipoDocumentoIdentidad;
				log.IdDocumentoIdentidad = clientPerso.DocumentoIdentidad;
			}
			else {
				log.EstadoCliente = "";
				log.NombreCliente = "";
				log.IdTipoDocumentoIdentidad = "";
				log.IdDocumentoIdentidad = "";
			}
			log.IdPortafolio = mvtoEntrada.IdPortafolio;
			log.EstadoPortafolio = (portaf != null) ? portaf.EstadoPortafolio : "" ;
			log.IdTipoRegistro = "FACTURACION";
			log.EstadoRegistro = "";
			log.IdTipoOperacion = mvtoEntrada.IdTipoOperacion;
			log.Folio = mvtoEntrada.FolioCartera;
			log.Cantidad = mvtoEntrada.Cantidad;
			log.PrecioTasa = mvtoEntrada.Precio;
			log.Monto = mvtoEntrada.Monto;
			log.IdMonedaOrigen = mvtoEntrada.IdMoneda;
			log.IdMonedaLiquidacion = mvtoEntrada.IdMonedaLiquidacion;
			log.TipoCambio = "";
			log.Nemo = mvtoEntrada.NemotecnicoInstrumento != null ? mvtoEntrada.NemotecnicoInstrumento : "";
			
			//caso nemo simultaneas
			if((mvtoEntrada.IdTipoOperacion != null) && ((mvtoEntrada.IdTipoOperacion).equals("COMSIM") || (mvtoEntrada.IdTipoOperacion).equals("VENSIM"))){
				MovimientoComprobante_Proceso mov = (MovimientoComprobante_Proceso) hashMovimientos.get(mvtoEntrada.NumeroMvtoCmbte);
				log.Nemo = (mov != null && mov.NemotecnicoSubyacente != null) ? mov.NemotecnicoSubyacente : "";
			}
			
			log.IdProceso = 0L;
			log.EstadoProceso = "";
			log.NombreProceso = "Ingreso a Facturacion";						
			entradaAgregarLog.p_Log.add(log);
		}
		
		HashMap<String,Object> hCabeceraLog = Utils.List2Hash(entradaAgregarLog.p_Log, "IdCabeceraLog");
		//DETALLE LOG 
		List<MedioDePago> listadoMediosDePago = _sql.query(conn, SQL_SELECT_MEDIODEPAGO, new HashMap<>(), MedioDePago.class);
		HashMap<String,Object> hMedioPago = Utils.List2Hash(listadoMediosDePago, "IdMedioDePago");
		HashMap<Long, Long> hContPorLog = new HashMap<>();
		
		if(in.p_MovimientosFormaDePago.size() > 0) {
			
			for(MovimientoComprobanteFormaDePago_Entrada movFormPago : in.p_MovimientosFormaDePago) {
				
				AgregarLog.InAgregarLog.Log log = (AgregarLog.InAgregarLog.Log) hCabeceraLog.get(movFormPago.NumeroMvtoCmbte);
				if(log != null) {
					Long cont = obtenerContadorDetallePorLog(hContPorLog, movFormPago.NumeroMvtoCmbte);
					String movimiento = "Forma de Pago " + cont;
					agregarLogDetalle(movimiento,log, "TipoMovimientoSpot" ,movFormPago.IdTipoMovimientoSpot, entradaAgregarLog.p_LogDetalle);
					if(movFormPago.FechaDocumentoPago!=null && !(movFormPago.FechaDocumentoPago.equalsIgnoreCase("0001-01-01")) ) {
						agregarLogDetalle(movimiento,log,  "Fecha Pago" ,movFormPago.FechaDocumentoPago , entradaAgregarLog.p_LogDetalle);
					}
					if(movFormPago.MontoLiquidacionFormaDePago!=null) {
					String MontoLiquidacion = movFormPago.MontoLiquidacionFormaDePago.toString();
					agregarLogDetalle(movimiento,log,  "Monto Liquidacion" , MontoLiquidacion , entradaAgregarLog.p_LogDetalle);
					}
					agregarLogDetalle(movimiento,log, "Id Moneda" ,movFormPago.IdMoneda , entradaAgregarLog.p_LogDetalle);
					if(movFormPago.IdMedioDePago != null) {
					MedioDePago medioPago = (MedioDePago) hMedioPago.get(movFormPago.IdMedioDePago);
					agregarLogDetalle(movimiento,log,  "Medio Pago" ,medioPago.Nombre, entradaAgregarLog.p_LogDetalle);	
					}
				}
			}
		}
		
		List<Cobro> listadoTiposCobro = _sql.query(conn, SQL_SELECT_COBRO, new HashMap<>(), Cobro.class);
		HashMap<String,Object> hCobro = Utils.List2Hash(listadoTiposCobro, "IdCobro");
		hContPorLog.clear();
		
		if(in.p_MovimientosCobros.size() > 0) {
			for(MovimientoComprobanteCobro_Entrada movCobro : in.p_MovimientosCobros) {
				AgregarLog.InAgregarLog.Log log = (AgregarLog.InAgregarLog.Log) hCabeceraLog.get(movCobro.NumeroMvtoCmbte);
				if(log != null) {
					Long cont = obtenerContadorDetallePorLog(hContPorLog, movCobro.NumeroMvtoCmbte);
					String movimiento = "Cobro " + cont;
					if(movCobro.IdCobro != null) {
					Cobro cobro = (Cobro) hCobro.get(movCobro.IdCobro);
						if(cobro != null) {
					agregarLogDetalle(movimiento, log, "Cobro" ,cobro.NombreCobro, entradaAgregarLog.p_LogDetalle);
						}
					}
					agregarLogDetalle(movimiento, log, "Moneda Cobro" , movCobro.IdMonedaCobro , entradaAgregarLog.p_LogDetalle);
					agregarLogDetalle(movimiento, log, "Moneda Liquidacion" , movCobro.IdMonedaLiquidacionCobro , entradaAgregarLog.p_LogDetalle);
					if(movCobro.MontoLiquidacionCobro != null) {
					String MontoLiquidacion = movCobro.MontoLiquidacionCobro.toString();
					agregarLogDetalle(movimiento, log, "Monto Liquidacion" , MontoLiquidacion , entradaAgregarLog.p_LogDetalle);
					}
					if(movCobro.Porcentaje != null) {
					String Porcentaje = movCobro.Porcentaje.toString();
					agregarLogDetalle(movimiento, log, "Porcentaje" , "%" + Porcentaje , entradaAgregarLog.p_LogDetalle);
					}
				}
			}
		}

		log.debug("Fin PREPARAR LOG");

		log.debug("limpiar movimientos");
		limpiarMovimientos(movimientos,movimientosConError,salida.P_OUT_MENSAJES,salida.p_out_ResultadoProceso,conn);
		if (movimientos.size() == 0 || (SI.equalsIgnoreCase(in.p_EsCargaMasiva) && movimientosConError.size() > 0)) {
			salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
			return salida;
		}

		// SETEO DE MESA DEL MOVIMIENTO
		log.debug("seteo de mesa del movimiento");
		{
			//IMPTANNOPT-271 se incorpora IdPortafolio al buscar la Mesa
			List<Long> idsPortafolio = movimientos.parallelStream().map(i -> i.IdPortafolio).distinct().collect(Collectors.toList());
			
			//IMPMBIOPT-617 se obtiene mesa de la corredora (por defecto o de menor id) sin verificar Cliente-Portafolio.
			//Se elimina logica MesaDefecto_Proceso pues la query siguiente SQL_SELECT_MESAS ya la obtiene
			HashMap<String, Object> params = new HashMap<>();
			params.put("idEmpresa", in.p_IdEmpresa);
			params.put("IdPortafolio", idsPortafolio);
			params.put("filtroIdPortafolio", 1);
			List<Mesa_Proceso> mesas = _sql.query(conn, SQL_SELECT_MESAS, params, Mesa_Proceso.class);
			
			if (mesas.isEmpty()) {
				//Si el Portafolio no tiene mesa, se busca la mesa por defecto o la primera
				params.put("filtroIdPortafolio", 0);
				mesas = _sql.query(conn, SQL_SELECT_MESAS, params, Mesa_Proceso.class);
			}
			
			for (MovimientoComprobante_Proceso mvto : movimientos) {
				if (("IF").equalsIgnoreCase(mvto.IdClasificacionTipoInstrumento) || ("RF").equalsIgnoreCase(mvto.IdClasificacionTipoInstrumento)) {
					mvto.IdMesa = mesas.size() > 0 ? mesas.get(0).IdMesa : 0L;
				}
			}
		}

		// VALIDACION DE SECUENCIA
		log.debug("validacion de secuencia");
		{
			List<String> secuenciasAgrupacion = new ArrayList<>();
			for (MovimientoComprobante_Proceso mvto : movimientos) {
				secuenciasAgrupacion.add(mvto.SecuenciaAgrupacion);
			}
			
			if (secuenciasAgrupacion.size() > 0) {
				HashMap<String,Object> params = new HashMap<>();
				params.put("secuenciasAgrupacion", secuenciasAgrupacion);
				List<SecuenciaAgrupacion_Proceso> secuencias = _sql.query(conn, SQL_SELECT_SECUENCIAS, params, SecuenciaAgrupacion_Proceso.class);
				
				
				for (MovimientoComprobante_Proceso mvto : movimientos) {
					for (SecuenciaAgrupacion_Proceso secuencia : secuencias) {
						if (mvto.IdCliente.equals(secuencia.IdCliente) &&
							mvto.IdPortafolio.equals(secuencia.IdPortafolio) &&
							mvto.SecuenciaAgrupacion.equalsIgnoreCase(secuencia.SecuenciaAgrupacion)) {
							String codigo = "MVTO_CMBTE_SECUENCIA_UTILIZADA";
							String desc = Diccionario.getMensaje(conn, mvto.IdEmpresa, codigo).msg();
							salida.P_OUT_MENSAJES.add(new MensajeSalida(mvto.NumeroMvtoCmbte,MensajeSalida.MENSAJE_ERROR,codigo,desc));
							mvto.errores++;
						}
					}
				}
			}
		}
		log.debug("limpiar movimientos");
		limpiarMovimientos(movimientos,movimientosConError,salida.P_OUT_MENSAJES,salida.p_out_ResultadoProceso,conn);
		if (movimientos.size() == 0 || (SI.equalsIgnoreCase(in.p_EsCargaMasiva) && movimientosConError.size() > 0)) {
			salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
			return salida;
		}
		
		//VALIDACION FORMAS DE PAGO
		log.debug("insert validacion formas de pago");
		_sql.batch(conn, SQL_INSERT_MOVIMIENTO_COMPROBANTE_FORMA_DE_PAGO_PROCESO, movimientosFormaDePago, MovimientoComprobanteFormaDePago_Proceso.class);
		log.debug("select movimiento comprobante formas de pago");
		List<InformacionMovimientoComprobanteFormaDePago> _infoFormasDePago = _sql.query(conn, SQL_SELECT_MOVIMIENTO_COMPROBANTE_FORMA_DE_PAGO, new HashMap<>(), InformacionMovimientoComprobanteFormaDePago.class);
		HashMap<Long,Object> hashFormasDePago = Utils.List2Hash(_infoFormasDePago, "NumeroMvtoCmbteFormaDePago");
		log.debug("validar comprobante formas de pago");
		for (MovimientoComprobanteFormaDePago_Proceso fpag : movimientosFormaDePago) {
			InformacionMovimientoComprobanteFormaDePago infoFpag = (InformacionMovimientoComprobanteFormaDePago) hashFormasDePago.get(fpag.NumeroMvtoCmbteFormaDePago);
			MovimientoComprobante_Proceso mvto = hashMovimientos.get(infoFpag.NumeroMvtoCmbte);
			
			ProcesaRequisito(conn, in.p_IdEmpresa, fpag.IdMedioDePago != null && fpag.IdMedioDePago.length() > 0 && infoFpag.IdMedioDePagoReal == null , "MVTO_CMBTE_MEDIO_DE_PAGO_NO_EXISTE", MensajeSalida.MENSAJE_ERROR, mvto, salida.P_OUT_MENSAJES);
			ProcesaRequisito(conn, in.p_IdEmpresa, fpag.IdModalidadDePago != null && fpag.IdModalidadDePago.length() > 0 && infoFpag.IdModalidadDePagoReal == null , "MVTO_CMBTE_MODALIDAD_DE_PAGO_NO_EXISTE", MensajeSalida.MENSAJE_ERROR, mvto, salida.P_OUT_MENSAJES);
			ProcesaRequisito(conn, in.p_IdEmpresa, fpag.IdMoneda != null && fpag.IdMoneda.length() > 0 && infoFpag.IdMonedaFormaDePagoReal == null , "MVTO_CMBTE_MONEDA_FORMA_DE_PAGO_NO_EXISTE", MensajeSalida.MENSAJE_ERROR, mvto, salida.P_OUT_MENSAJES);			
			ProcesaRequisito(conn, in.p_IdEmpresa, fpag.IdTipoCuentaBancaria != null && fpag.IdTipoCuentaBancaria.length() > 0 && infoFpag.IdTipoCuentaBancariaReal == null  , "MVTO_CMBTE_TIPO_CUENTA_BANCARIA_NO_EXISTE", MensajeSalida.MENSAJE_ERROR, mvto, salida.P_OUT_MENSAJES);
			ProcesaRequisito(conn, in.p_IdEmpresa, fpag.IdTarjetaBancaria != null && fpag.IdTarjetaBancaria > 0 && fpag.FechaVctoTarjetaBancaria == null , "MVTO_CMBTE_FECHA_VENCIMIENTO_TARJETA_INVALIDA", MensajeSalida.MENSAJE_ERROR, mvto, salida.P_OUT_MENSAJES);
			ProcesaRequisito(conn, in.p_IdEmpresa, fpag.IdTipoTarjetaBancaria != null && fpag.IdTipoTarjetaBancaria.length() > 0 && infoFpag.IdTipoTarjetaBancariaReal == null , "MVTO_CMBTE_TIPO_TARJETA_BANCARIA_NO_EXISTE", MensajeSalida.MENSAJE_ERROR, mvto, salida.P_OUT_MENSAJES);
			ProcesaRequisito(conn, in.p_IdEmpresa, fpag.NumeroDocumento != null && fpag.NumeroDocumento > 0 && fpag.FechaDocumentoPago == null , "MVTO_CMBTE_FECHA_VENCIMIENTO_DOCUMENTO_INVALIDA", MensajeSalida.MENSAJE_ERROR, mvto, salida.P_OUT_MENSAJES);
			
			fpag.IdMovimientoComprobante = mvto.IdMovimientoComprobante;
			fpag.IdMovimientoComprobanteFormaDePago = infoFpag.IdMovimientoComprobanteFormaDePago;
			fpag.IdTipoMovimiento = mvto.IdConceptoMovimiento;
			if (fpag.IdMedioDePago == null || fpag.IdMedioDePago.length() == 0) {
				fpag.NombreMedioDePago = "";
			}
			else {
				fpag.NombreMedioDePago = infoFpag.NombreMedioDePago;
			}
			fpag.NombreCuentaBancaria = "";
			
			if (fpag.IdTipoCuentaBancaria == null || fpag.IdTipoCuentaBancaria.length() == 0) {
				fpag.NombreTipoCuentaBancaria = "";
			}
			else {
				fpag.NombreTipoCuentaBancaria = infoFpag.NombreTipoCuentaBancaria;	
			}
			
			if (fpag.IdTipoTarjetaBancaria == null || fpag.IdTipoTarjetaBancaria.length() == 0) {
				fpag.NombreTipoTarjetaBancaria = "";
			}
			else {
				fpag.NombreTipoTarjetaBancaria = infoFpag.NombreTipoTarjetaBancaria;	
			}
			
			if (fpag.IdModalidadDePago == null || fpag.IdModalidadDePago.length() == 0) {
				fpag.NombreModalidadDePago = "";
			}
			else {
				fpag.NombreModalidadDePago = infoFpag.NombreModalidadDePago;	
			}
			
			if (fpag.IdBanco == null || fpag.IdBanco.equals(0L)) {
				fpag.CodigoBanco = "";
				fpag.NombreBanco = "";
			}
			else {
				fpag.CodigoBanco = infoFpag.CodigoBanco;
				fpag.NombreBanco = infoFpag.NombreBanco;
			}
			fpag.IdEstado = "VIG";
			
		}
		log.debug("limpiar movimientos");
		limpiarMovimientos(movimientos,movimientosConError,salida.P_OUT_MENSAJES,salida.p_out_ResultadoProceso,conn);
		if (movimientos.size() == 0 || (SI.equalsIgnoreCase(in.p_EsCargaMasiva) && movimientosConError.size() > 0)) {
			salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
			return salida;
		}
		
		//VALIDACION COBROS
		log.debug("insert movimiento comprobante cobro proceso");
		_sql.batch(conn, SQL_INSERT_MOVIMIENTO_COMPROBANTE_COBRO_PROCESO, movimientosCobro, MovimientoComprobanteCobro_Proceso.class);
		log.debug("select movimiento comprobante cobro proceso");
		List<InformacionMovimientoComprobanteCobro> _infoCobros = _sql.query(conn, SQL_SELECT_MOVIMIENTO_COMPROBANTE_COBRO, new HashMap<>(), InformacionMovimientoComprobanteCobro.class);
		HashMap<Long,Object> hashCobros = Utils.List2Hash(_infoCobros, "NumeroMvtoCmbteCobro");
		log.debug("validar movimiento comprobante cobro proceso");
		for (MovimientoComprobanteCobro_Proceso cobro : movimientosCobro) {
			
			InformacionMovimientoComprobanteCobro infoCobro = (InformacionMovimientoComprobanteCobro) hashCobros.get(cobro.NumeroMvtoCmbteCobro);
			MovimientoComprobante_Proceso mvto = hashMovimientos.get(infoCobro.NumeroMvtoCmbte);
			
			if (localizacionSistema.equalsIgnoreCase("CL")) {
				ProcesaRequisito(conn, in.p_IdEmpresa, cobro.IdCobro != null && cobro.IdCobro.length() > 0 && infoCobro.IdCobroReal == null , "MVTO_CMBTE_COBRO_NO_EXISTE", MensajeSalida.MENSAJE_ERROR, mvto, salida.P_OUT_MENSAJES);
					
			}
			if (localizacionSistema.equalsIgnoreCase("PE")) {
				ProcesaRequisito(conn, in.p_IdEmpresa, cobro.IdCobro != null && cobro.IdCobro.length() > 0 && infoCobro.CodigoDefinicionCobroReal == null , "MVTO_CMBTE_COBRO_NO_EXISTE", MensajeSalida.MENSAJE_ERROR, mvto, salida.P_OUT_MENSAJES);
				
			}
			ProcesaRequisito(conn, in.p_IdEmpresa, cobro.IdMonedaCobro!= null && cobro.IdMonedaCobro.length() > 0 && infoCobro.IdMonedaReal == null , "MVTO_CMBTE_MONEDA_COBRO_NO_EXISTE", MensajeSalida.MENSAJE_ERROR, mvto, salida.P_OUT_MENSAJES);
			ProcesaRequisito(conn, in.p_IdEmpresa, cobro.IdCobro != null && cobro.IdCobro.length() > 0 && cobro.MontoCobro.compareTo(BigDecimal.ZERO) < 0 , "MVTO_CMBTE_MONTO_COBRO_MENOR_A_CERO", MensajeSalida.MENSAJE_ERROR, mvto, salida.P_OUT_MENSAJES);
			ProcesaRequisito(conn, in.p_IdEmpresa, cobro.IdMonedaLiquidacionCobro != null && cobro.IdMonedaLiquidacionCobro.length() > 0 && infoCobro.IdMonedaLiquidacionReal == null , "MVTO_CMBTE_MONEDA_LIQUIDACION_COBRO_NO_EXISTE", MensajeSalida.MENSAJE_ERROR, mvto, salida.P_OUT_MENSAJES);
			ProcesaRequisito(conn, in.p_IdEmpresa, cobro.IdCobro != null && cobro.IdCobro.length() > 0 && cobro.MontoLiquidacionCobro.compareTo(BigDecimal.ZERO) < 0 , "MVTO_CMBTE_MONTO_LIQUIDACION_COBRO_MENOR_A_CERO", MensajeSalida.MENSAJE_ERROR, mvto, salida.P_OUT_MENSAJES);
			ProcesaRequisito(conn, in.p_IdEmpresa, cobro.IdCobro != null && cobro.IdCobro.length() > 0 && cobro.MontoMovimientoComprobanteCobro.compareTo(BigDecimal.ZERO) < 0 , "MVTO_CMBTE_MONTO_LOCAL_COBRO_MENOR_A_CERO", MensajeSalida.MENSAJE_ERROR, mvto, salida.P_OUT_MENSAJES);
			
			cobro.IdMovimientoComprobante = mvto.IdMovimientoComprobante;
			cobro.IdMovimientoComprobanteCobro = infoCobro.IdMovimientoComprobanteCobro;
			cobro.FechaSistema = Fecha.toDateTime(fechaSistema + " 00:00:00");
			cobro.IdEstado = "VIG";
			cobro.IdMonedaMovimientoComprobanteCobro = monedaSistema;
			cobro.Monto = mvto.Monto;
			cobro.MontoLiquidacion = mvto.MontoLiquidacion;
			cobro.MontoMovimientoComprobante = mvto.MontoMovimientoComprobante;
			
		}
		log.debug("limpiar movimientos");
		limpiarMovimientos(movimientos,movimientosConError,salida.P_OUT_MENSAJES,salida.p_out_ResultadoProceso,conn);
		if (movimientos.size() == 0 || (SI.equalsIgnoreCase(in.p_EsCargaMasiva) && movimientosConError.size() > 0)) {
			salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
			return salida;
		}
		
		//ACTUALIZACION SIMULTANEAS Y REPORTES
		log.debug("actualizacion simultaneas y reportes");
		for (MovimientoComprobante_Proceso mvto : movimientos) {
			for (MovimientoComprobante_Proceso mvtoPlazo : movimientos) {
				if (mvto.IdAsignacion != null && mvto.IdAsignacion > 0 
					&& mvtoPlazo.IdAsignacionRef != null && mvtoPlazo.IdAsignacionRef > 0
					&& mvto.IdAsignacionRef.equals(mvtoPlazo.IdAsignacion)) {
					mvto.IdMovimientoRelacionado = mvtoPlazo.IdMovimientoComprobante;
					mvtoPlazo.IdMovimientoRelacionado = mvto.IdMovimientoComprobante;
				}
			}
		}
		
		Map<Long,Object> infoMovimientosContrapartidaRelacionados = movimientos.stream().filter(mov -> "S".equals(mov.Contrapartida)).collect(Collectors.toMap(movimiento->movimiento.NumeroMvtoCmbte, movimiento->movimiento));

		for (MovimientoComprobante_Proceso mvto : movimientos) {
			if("S".equals(mvto.Contrapartida)) {
				MovimientoComprobante_Proceso mvtoContrapartida = (MovimientoComprobante_Proceso) infoMovimientosContrapartidaRelacionados.get(mvto.NumeroMvtoCmbte);
				mvto.IdMovimientoRelacionado = mvtoContrapartida.IdMovimientoComprobante;
			}
		}
				
		infoMovimientosContrapartidaRelacionados = movimientos.stream().filter(mov -> "S".equals(mov.Contrapartida) && "COMOTC".equals(mov.IdTipoOperacion)).collect(Collectors.toMap(movimiento->movimiento.IdMovimientoComprobante, movimiento->movimiento));

		
		HashMap<Long, Long> hIdTransacion = new HashMap<>();
		HashMap<Long, Long> hNumMvtoComprobante = new HashMap<>();
		
		//INSERT FINALES
		log.debug("insert finales");
		if (movimientos.size() > 0 && in.p_simularOperacion.equalsIgnoreCase("N")) {
			
			List<MovimientoComprobante> movimientosFinales = new ArrayList<>();
			String codigo = "UI_MOVIMIENTO_COMPROBANTE_CREADO";
			for (MovimientoComprobante_Proceso mvto : movimientos) {
				
				MovimientoComprobante mvtoFinal = new MovimientoComprobante();
				Utils.copyPojo(mvto, mvtoFinal);
				movimientosFinales.add(mvtoFinal);
				
				String desc = Diccionario.getMensaje(conn, in.p_IdEmpresa, codigo).add("IdMovimientoComprobante", mvto.IdMovimientoComprobante).msg();
				salida.P_OUT_MENSAJES.add(new MensajeSalida(mvto.NumeroMvtoCmbte,MensajeSalida.MENSAJE_INFORMACION,codigo,desc));
				
				ResultadoProceso_Salida res = new ResultadoProceso_Salida();
				res.Campo = "IdMovimientoComprobante";
				res.Id = mvto.IdMovimientoComprobante;
				res.Resultado = "ING";
				res.Servicio = "CrearMovimientoComprobante.java";
				salida.p_out_ResultadoProceso.add(res);
				
				MovimientoComprobante_Salida mvtoSalida = new MovimientoComprobante_Salida();
				mvtoSalida.IdMovimientoComprobante = mvto.IdMovimientoComprobante;
				mvtoSalida.NumeroMvtoCmbte = mvto.NumeroMvtoCmbte;
				mvtoSalida.IdOperacion = mvto.IdOperacion;
				salida.p_out_Movimientos.add(mvtoSalida);
				
				hIdTransacion.put(mvto.IdTransaccion , mvto.IdMovimientoComprobante);
				hNumMvtoComprobante.put(mvto.NumeroMvtoCmbte, mvto.IdMovimientoComprobante );
			}
			log.debug("insert movimiento comprobante");
			_sql.batch(conn, SQL_INSERT_MOVIMIENTO_COMPROBANTE, movimientosFinales, MovimientoComprobante.class);
			{
			log.debug("Inicio Insert log");
				//Actualizar Folio Y Estado Caberecera LOG
				for(AgregarLog.InAgregarLog.Log log : entradaAgregarLog.p_Log) {
					asignarNumeroFolioCabeceraLog(log, hIdTransacion);
					asignarNumeroFolioCabeceraLog(log, hNumMvtoComprobante);
					if(log.Folio.compareTo(0L) == 0) log.EstadoProceso = "RZD";
				}
				
				agregarLogService.logica(conn, entradaAgregarLog);
			log.debug("Fin Insert log");
			}
			
			if (listaReferenciaTipoDocumento.size()>0) {
				log.debug("insert en tabla MovimientoComprobanteReferencia");
				_sql.batch(conn, SQL_INSERT_MOVIMIENTO_COMPROBANTE_REFERENCIA, listaReferenciaTipoDocumento, ReferenciaTipoDocumento.class);	
			}
			
			if (movimientosCobro.size() > 0) {
				
				List<MovimientoComprobanteCobro> movimientosCobroFinales = new ArrayList<>();
				for (MovimientoComprobanteCobro_Proceso mvtoCobro : movimientosCobro) {
					MovimientoComprobanteCobro mvtoCobroFinal = new MovimientoComprobanteCobro();
					Utils.copyPojo(mvtoCobro, mvtoCobroFinal);
					movimientosCobroFinales.add(mvtoCobroFinal);
				}
				log.debug("insert movimiento comprobante cobro");
				_sql.batch(conn, SQL_INSERT_MOVIMIENTO_COMPROBANTE_COBRO, movimientosCobroFinales, MovimientoComprobanteCobro.class);
			}
			
			if (movimientosFormaDePago.size() > 0) {
				List<MovimientoComprobanteFormaDePago> movimientosFormaDePagoFinales = new ArrayList<>();
				for (MovimientoComprobanteFormaDePago_Proceso mvtoFormaDePago : movimientosFormaDePago) {
					MovimientoComprobanteFormaDePago mvtoFormaDePagoFinal = new MovimientoComprobanteFormaDePago();
					Utils.copyPojo(mvtoFormaDePago, mvtoFormaDePagoFinal);
					movimientosFormaDePagoFinales.add(mvtoFormaDePagoFinal);
				}
				log.debug("insert movimiento comprobante forma de pago");
				_sql.batch(conn, SQL_INSERT_MOVIMIENTO_COMPROBANTE_FORMA_DE_PAGO, movimientosFormaDePagoFinales, MovimientoComprobanteFormaDePago.class);
			}
			
			
			List<cl.bcs.facturacion.CrearComprobante.MovimientoComprobante_Entrada> movimientosCrearComprobante = new ArrayList<>();
			for (MovimientoComprobante_Proceso mvto : movimientos) {
				if (mvto.CrearComprobanteAutomatico!= null && mvto.CrearComprobanteAutomatico.equalsIgnoreCase("S")) {
					cl.bcs.facturacion.CrearComprobante.MovimientoComprobante_Entrada mvtoCmbte = new cl.bcs.facturacion.CrearComprobante.MovimientoComprobante_Entrada();
					mvtoCmbte.FechaComprobante = Fecha.toStringDatetime(mvto.FechaMovimientoComprobante);
					mvtoCmbte.IdMovimientoComprobante = mvto.IdMovimientoComprobante;
					mvtoCmbte.IdConcepto = mvto.IdConceptoMovimiento != null ? mvto.IdConceptoMovimiento : "" ; // si es null entonces deja vacio ("")
					movimientosCrearComprobante.add(mvtoCmbte);
				}
			}
			log.debug("construir objeto de salida");
			if (movimientosCrearComprobante.size() > 0) {
				CrearComprobante_Entrada entradaCrearComprobante = new CrearComprobante_Entrada();
				entradaCrearComprobante.p_EsIngresoAutomatico = "N";
				entradaCrearComprobante.p_Glosa = "";
				entradaCrearComprobante.p_IdEmpresa = in.p_IdEmpresa;
				entradaCrearComprobante.p_IdModulo = in.p_IdModulo;
				entradaCrearComprobante.p_IdUsuario = in.p_IdUsuario;
				
				entradaCrearComprobante.P_MOVIMIENTOFACTURAR = movimientosCrearComprobante;
				entradaCrearComprobante.p_simularOperacion = in.p_simularOperacion;
				entradaCrearComprobante.p_UsaReglasNegocio = "S";
				
				CrearComprobante serviceCrearComprobante = new CrearComprobante();
				CrearComprobante_Salida salidaCrearComprobante = serviceCrearComprobante.logica(conn, entradaCrearComprobante);
				
				salida.P_OUT_MENSAJES.addAll(salidaCrearComprobante.P_OUT_MENSAJES);
				
				for (cl.bcs.facturacion.CrearComprobante.Comprobante_Salida cmbte : salidaCrearComprobante.P_OUT_COMPROBANTE) {
					Comprobante_Salida cmbteSalida = new Comprobante_Salida();
					cmbteSalida.IdComprobante = cmbte.IdComprobante;
					cmbteSalida.IdMovimientoComprobante = cmbte.IdMovimientoComprobante; 
					salida.p_out_Comprobantes.add(cmbteSalida );
				}
				
				Map<String,Object> hashMovimientosContrapartidaCargoAbono = movimientosContrapartidaAbonoUsd.stream().collect(Collectors.toMap(movimiento->movimiento.SecuenciaAgrupacion, movimiento->movimiento));
				
				for (cl.bcs.facturacion.CrearComprobante.Comprobante_Salida cmbte : salidaCrearComprobante.P_OUT_COMPROBANTE) {
					MovimientoComprobante_Proceso mvtoContrapartida = (MovimientoComprobante_Proceso) infoMovimientosContrapartidaRelacionados.get(cmbte.IdMovimientoComprobante);
					if(mvtoContrapartida != null) {
						String secuenciaAgrupacion 			= String.valueOf(mvtoContrapartida.NumeroMvtoCmbte);
						MovimientoComprobante_Entrada mvto 	= (MovimientoComprobante_Entrada) hashMovimientosContrapartidaCargoAbono.get(secuenciaAgrupacion);
						mvto.IdComprobanteReferencia 		= (mvto != null) ? cmbte.IdComprobante : 0;
					}
				}
							
				salida = crearCargoAbonoUsdClpContrapartida(conn, _sql, movimientosContrapartidaAbonoUsd, movimietoCobroContrapartida, in, salida);
				
				for (cl.bcs.facturacion.CrearComprobante.ResultadoProceso_Salida res : salidaCrearComprobante.P_OUT_RESULTADOPROCESO) {
					ResultadoProceso_Salida resSalida = new ResultadoProceso_Salida();
					resSalida.Campo = res.Campo;
					resSalida.Id = res.Id;
					resSalida.Resultado = res.Resultado;
					resSalida.Servicio = res.Servicio;
					salida.p_out_ResultadoProceso.add(resSalida);
				}
			}
		}

		salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
		log.info("TERMINO");
		return salida;
	}
	
	private List<MovimientoComprobanteFormaDePago_Entrada> agregarMovimientosFormasDePagoContrapartida(Connection conn, Map<Long, MovimientoContrapartida> movimientosContrapartida, List<MovimientoComprobanteFormaDePago_Entrada> movimientosFormaDePago, Long idEmpresa) {
		log.trace("INICIO::CrearMovimientoComprobante.agregarMovimientosFormasDePagoContrapartida");
		long inicio = System.currentTimeMillis();
		
		List<MovimientoComprobanteFormaDePago_Entrada> movimientosFormasDePagoContrapartida = new ArrayList<MovimientoComprobanteFormaDePago_Entrada>();
	
		AtomicLong correlativo = new AtomicLong(movimientosFormaDePago.size());
		
		for(MovimientoComprobanteFormaDePago_Entrada formaPago : movimientosFormaDePago) {
			MovimientoContrapartida movimientoContrapartida = movimientosContrapartida.get(formaPago.NumeroMvtoCmbte);
			
			if(movimientoContrapartida != null) {
				MovimientoComprobanteFormaDePago_Entrada movimientoFormaDePagoCargo =  new MovimientoComprobanteFormaDePago_Entrada();
			
				movimientoFormaDePagoCargo.NumeroMvtoCmbte = movimientoContrapartida.Cargo.NumeroMvtoCmbte;
				movimientoFormaDePagoCargo.NumeroMvtoCmbteFormaDePago = correlativo.getAndIncrement();
				movimientoFormaDePagoCargo.IdMedioDePago = formaPago.IdMedioDePago;
				movimientoFormaDePagoCargo.NumeroSerie = formaPago.NumeroSerie;
				movimientoFormaDePagoCargo.NumeroDocumento = formaPago.NumeroDocumento;
				movimientoFormaDePagoCargo.IdCuentaBancaria = formaPago.IdCuentaBancaria;
				movimientoFormaDePagoCargo.IdTipoCuentaBancaria = formaPago.IdTipoCuentaBancaria;
				movimientoFormaDePagoCargo.NumeroCuentaBancaria = formaPago.NumeroCuentaBancaria;
				movimientoFormaDePagoCargo.IdTarjetaBancaria = formaPago.IdTarjetaBancaria;
				movimientoFormaDePagoCargo.IdTipoTarjetaBancaria = formaPago.IdTipoTarjetaBancaria;
				movimientoFormaDePagoCargo.NumeroTarjetaBancaria = formaPago.NumeroCuentaBancaria;
				movimientoFormaDePagoCargo.FechaVctoTarjetaBancaria = formaPago.FechaVctoTarjetaBancaria;
				movimientoFormaDePagoCargo.IdModalidadDePago = formaPago.IdModalidadDePago;
				movimientoFormaDePagoCargo.IdBanco = formaPago.IdBanco;
				movimientoFormaDePagoCargo.IdMoneda = movimientoContrapartida.Cargo.IdMoneda;//detalle.IdMoneda;
				movimientoFormaDePagoCargo.FechaDocumentoPago = formaPago.FechaDocumentoPago;
				movimientoFormaDePagoCargo.MontoLiquidacionFormaDePago = movimientoContrapartida.Cargo.Monto;
				movimientoFormaDePagoCargo.IdTipoMovimientoSpot = formaPago.IdTipoMovimientoSpot;

				movimientosFormasDePagoContrapartida.add(movimientoFormaDePagoCargo);
			}			
		}

		long fin = System.currentTimeMillis();
		log.info("FIN::CrearMovimientoComprobante.agregarMovimientosFormasDePagoContrapartida", fin - inicio);
		
		return movimientosFormasDePagoContrapartida;
	}


	private CrearMovimientoComprobante_Salida crearCargoAbonoUsdClpContrapartida(Connection conn, SQL sql, List<MovimientoComprobante_Entrada> movimientos, Map<Long, MovimientoComprobanteCobro_Entrada> cobros, CrearMovimientoComprobante_Entrada in, CrearMovimientoComprobante_Salida salida) throws Exception {
		log.trace("INICIO::CrearMovimientoComprobante.crearCargoAbonoUsdClpContrapartida");
		long inicio = System.currentTimeMillis();
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		List<ConceptoIndicadorAbonoClpContrapartida> conceptoIndicadorAbonoClpContrapartida = sql.query(conn, SQL_SELECT_CONCEPTO_INDICADOR_ABONO_CLP_CONTRAPARTIDA, new HashMap<>(), ConceptoIndicadorAbonoClpContrapartida.class);

		CrearCargoAbonoEntrada entradaCrearCargoAbono = new CrearCargoAbonoEntrada();
		CrearCargoAbono crearCargoAbono = new CrearCargoAbono();
		
		List<ObjCargoAbonoAGenerar> cargosAbono = new ArrayList<ObjCargoAbonoAGenerar>();
		AtomicLong correlativo = new AtomicLong();
		
		List<MovimientoComprobante_Entrada> movimentosParidad = movimientos.stream().filter(mov -> "S".equals(mov.Contrapartida)).collect(Collectors.toList());
		HashMap<Long, Object> calculoParidad = calcularParidad(conn, in.p_IdEmpresa, movimentosParidad);
		
		for(MovimientoComprobante_Entrada movimiento : movimientos) {
			
			ObjCargoAbonoAGenerar entradaCargoUsd 			= new ObjCargoAbonoAGenerar();
			MovimientoComprobanteCobro_Entrada cobro 		= cobros.getOrDefault(movimiento.NumeroMvtoCmbte, new MovimientoComprobanteCobro_Entrada());
			entradaCargoUsd.NumeroCargoAbono 				= correlativo.getAndIncrement();
			entradaCargoUsd.IdOperacion 					= correlativo.get();
			entradaCargoUsd.IdCondicionDeLiquidacion 		= "T0";
			entradaCargoUsd.IdPortafolio 					= movimiento.IdPortafolio;
			entradaCargoUsd.IdMoneda 						= movimiento.IdMoneda;
			entradaCargoUsd.IdTipoMovimiento 				= "MOVCAR";
			entradaCargoUsd.IdConcepto 						= movimiento.IdConcepto;
			entradaCargoUsd.FechaCondicionDeLiquidacion 	= movimiento.FechaLiquidacion;
			entradaCargoUsd.Monto 							= movimiento.Monto.add(cobro.MontoCobro);
			entradaCargoUsd.IdTipoOperacion 				= movimiento.IdTipoOperacion;
			entradaCargoUsd.FechaOperacion 					= movimiento.FechaLiquidacion;
			entradaCargoUsd.FechaLiquidacionOperacion 		= movimiento.FechaTransaccion;
			entradaCargoUsd.IdTipoMovimientoAsociado 		= movimiento.IdTipoComprobante;
			entradaCargoUsd.EsCargoAbonoPorTraspaso 		= "N";
			entradaCargoUsd.GeneroContraparteEnTesoreria 	= "S";
			entradaCargoUsd.CertificadoAutomatico 			= "S";
			entradaCargoUsd.EsIndicadorBilateral 			= "N";
			entradaCargoUsd.IdPortafolioTraspaso 			= 0L;
			entradaCargoUsd.IdComprobanteFormaDePago 		= 0L;
			entradaCargoUsd.FolioComprobante 				= String.valueOf(0);
			entradaCargoUsd.IdComprobanteDetalle 			= 0L;
			entradaCargoUsd.IdTipoComprobante 				= movimiento.IdTipoComprobante;
			entradaCargoUsd.IdContraparte 					= 0L;
			entradaCargoUsd.IdMonedaOperacion 				= movimiento.IdMoneda;
			entradaCargoUsd.IdComprobante 					= movimiento.IdComprobanteReferencia;
			cargosAbono.add(entradaCargoUsd);
			
			
			ObjCargoAbonoAGenerar entradaAbonoClp 			= new ObjCargoAbonoAGenerar();
			CalculoSalida paridad 							= (CalculoSalida) calculoParidad.get(movimiento.NumeroMvtoCmbte);
			entradaAbonoClp.NumeroCargoAbono 				= correlativo.getAndIncrement();
			entradaAbonoClp.IdOperacion 					= correlativo.get();
			entradaAbonoClp.IdCondicionDeLiquidacion 		= "T0";
			entradaAbonoClp.IdPortafolio 					= movimiento.IdPortafolio;
			entradaAbonoClp.IdMoneda 						= "CLP";
			entradaAbonoClp.IdTipoMovimiento 				= "MOVABN";
			entradaAbonoClp.IdConcepto 						= conceptoIndicadorAbonoClpContrapartida.size() == 1 ? 
															  conceptoIndicadorAbonoClpContrapartida.get(0).IdConcepto : 
															  movimiento.IdConcepto;
			entradaAbonoClp.FechaCondicionDeLiquidacion 	= movimiento.FechaLiquidacion;
			entradaAbonoClp.Monto 							= cobro.Porcentaje == null ? 
															  paridad.MontoDestino : 
															  paridad.MontoDestino.add(paridad.MontoDestino.multiply(cobro.Porcentaje).divide(BigDecimal.valueOf(100)));
			entradaAbonoClp.IdTipoOperacion 				= movimiento.IdTipoOperacion;
			entradaAbonoClp.FechaOperacion 					= movimiento.FechaLiquidacion;
			entradaAbonoClp.FechaLiquidacionOperacion 		= movimiento.FechaTransaccion;
			entradaAbonoClp.IdTipoMovimientoAsociado 		= movimiento.IdTipoComprobante;
			entradaAbonoClp.EsCargoAbonoPorTraspaso 		= "N";
			entradaAbonoClp.GeneroContraparteEnTesoreria 	= "S";
			entradaAbonoClp.CertificadoAutomatico 			= "S";
			entradaAbonoClp.EsIndicadorBilateral 			= "N";
			entradaAbonoClp.IdPortafolioTraspaso			= 0L;
			entradaAbonoClp.IdComprobanteFormaDePago 		= 0L;
			entradaAbonoClp.FolioComprobante 				= String.valueOf(0);
			entradaAbonoClp.IdComprobanteDetalle 			= 0L;
			entradaAbonoClp.IdTipoComprobante 				= movimiento.IdTipoComprobante;
			entradaAbonoClp.IdContraparte 					= 0L;
			entradaAbonoClp.IdMonedaOperacion 				= "CLP";
			entradaAbonoClp.IdComprobante 					= movimiento.IdComprobanteReferencia;
			cargosAbono.add(entradaAbonoClp);
		
		}
		
		entradaCrearCargoAbono.p_IdEmpresa 			= in.p_IdEmpresa;
		entradaCrearCargoAbono.p_IdUsuario 			= in.p_IdUsuario;
		entradaCrearCargoAbono.p_IdModulo 			= in.p_IdModulo;
		entradaCrearCargoAbono.p_simularOperacion 	= "N";
		entradaCrearCargoAbono.p_UsaReglasNegocio 	= "S";
		entradaCrearCargoAbono.p_FechaSistema 		= fechaSistema;
		entradaCrearCargoAbono.p_EsIngresoManual 	= "N";
		entradaCrearCargoAbono.p_CargoAbonoAGenerar = cargosAbono;
		CrearCargoAbonoSalida salidaCrearCargoAbono = crearCargoAbono.logica(conn, entradaCrearCargoAbono);
		
		salida.P_OUT_MENSAJES.addAll(salidaCrearCargoAbono.P_OUT_MENSAJES);
		salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salidaCrearCargoAbono.P_OUT_MENSAJES);

		long fin = System.currentTimeMillis();
		log.info("FIN::CrearMovimientoComprobante.crearCargoAbonoUsdClpContrapartida", fin - inicio);
		return salida;
	}

	private Map<Long, MovimientoContrapartida> agregarMovimientosContrapartida(Connection con, List<MovimientoComprobante_Entrada> movimientos, Long idEmpresa) throws Exception {
		log.trace("INICIO::CrearMovimientoComprobante.agregarMovimientosContrapartida");
		long inicio = System.currentTimeMillis();
		
		String idMonedaSistema	= ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa,"PLATAFORMA", "PLA.MonedaSistema");	
		Map<Long, MovimientoContrapartida> movimientosContrapartida = new HashMap<Long, MovimientoContrapartida>();
		
		List<MovimientoComprobante_Entrada> movimentosParidad = movimientos.stream().filter(mov -> "S".equals(mov.Contrapartida)).collect(Collectors.toList());
		HashMap<Long, Object> calculoParidad = calcularParidad(con, idEmpresa, movimentosParidad);
			
		AtomicLong numeroMovimento = new AtomicLong(movimientos.size());
		for(MovimientoComprobante_Entrada mov : movimientos) {
			if("S".equals(mov.Contrapartida)) {
				MovimientoComprobante_Entrada cargoClp = new MovimientoComprobante_Entrada();
				MovimientoContrapartida movimientoContrapartida = new MovimientoContrapartida();
					
				CalculoSalida paridad = (CalculoSalida) calculoParidad.get(mov.NumeroMvtoCmbte);

				Utils.copyPojo(mov, cargoClp);
				cargoClp.NumeroMvtoCmbte = numeroMovimento.incrementAndGet();
				cargoClp.IdMoneda = "CLP";
				cargoClp.IdMonedaLiquidacion = idMonedaSistema;
				cargoClp.IdTipoReajuste = idMonedaSistema;
				cargoClp.Monto = paridad.MontoDestino;
				cargoClp.MontoLiquidacion = cargoClp.Monto;
				cargoClp.MontoMovimientoComprobante = cargoClp.Monto;
				cargoClp.Contrapartida = "N";
				cargoClp.CrearComprobanteAutomatico = "S";
				
				movimientoContrapartida.Cargo = cargoClp;
				mov.SecuenciaAgrupacion = String.valueOf(movimientoContrapartida.Cargo.NumeroMvtoCmbte);
				movimientosContrapartida.put(mov.NumeroMvtoCmbte, movimientoContrapartida);
			}
		}
		
		long fin = System.currentTimeMillis();
		log.info("FIN::CrearMovimientoComprobante.agregarMovimientosContrapartida", fin - inicio);
		
		return movimientosContrapartida;
	}
	
	private List<MovimientoComprobanteCobro_Entrada> agregarMovimientosCobrosContrapartida(Connection con, Map<Long, MovimientoContrapartida> movimientosContrapartida, List<MovimientoComprobanteCobro_Entrada> mvtoEntradaCobro, Long idEmpresa) throws Exception {
		log.trace("INICIO::CrearMovimientoComprobante.agregarMovimientosCobrosContrapartida");
		long inicio = System.currentTimeMillis();
		
		List<MovimientoComprobanteCobro_Entrada> movimientosCobroContrapartida = new ArrayList<MovimientoComprobanteCobro_Entrada>();
	
		for(MovimientoComprobanteCobro_Entrada movimientoCobro : mvtoEntradaCobro) {
			MovimientoContrapartida movimientoContrapartida = movimientosContrapartida.get(movimientoCobro.NumeroMvtoCmbte);
			
			if(movimientoContrapartida != null) {
				MovimientoComprobanteCobro_Entrada movimientoCobroCargo =  new MovimientoComprobanteCobro_Entrada();
				
				movimientoCobroCargo.NumeroMvtoCmbte = movimientoContrapartida.Cargo.NumeroMvtoCmbte;
				movimientoCobroCargo.NumeroMvtoCmbteCobro = movimientoContrapartida.Cargo.NumeroMvtoCmbte;
				movimientoCobroCargo.IdCobro = movimientoCobro.IdCobro;
				movimientoCobroCargo.IdMonedaCobro = movimientoContrapartida.Cargo.IdMoneda;
				movimientoCobroCargo.IdMonedaLiquidacionCobro = movimientoContrapartida.Cargo.IdMoneda;
				movimientoCobroCargo.Porcentaje = movimientoCobro.Porcentaje;
				movimientoCobroCargo.MontoCobro = movimientoContrapartida.Cargo.Monto.multiply(movimientoCobroCargo.Porcentaje).divide(BigDecimal.valueOf(100));
				movimientoCobroCargo.MontoMovimientoComprobanteCobro = movimientoCobroCargo.MontoCobro;
				movimientoCobroCargo.MontoLiquidacionCobro = movimientoCobroCargo.MontoCobro;
				
				movimientosCobroContrapartida.add(movimientoCobroCargo);	
			}			
		}

		long fin = System.currentTimeMillis();
		log.info("FIN::CrearMovimientoComprobante.agregarMovimientosCobrosContrapartida", fin - inicio);
		
		return movimientosCobroContrapartida;
	}
	
	
	private HashMap<Long, Object> calcularParidad(Connection con, Long idEmpresa, List<MovimientoComprobante_Entrada> movimientos) throws Exception {
		log.trace("INICIO::CrearMovimientoComprobante.calcularParidad");
		long inicio = System.currentTimeMillis();

		String idMonedaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA", "PLA.MonedaSistema");

		CalcularParidad calcularParidad = new CalcularParidad();

		CalcularParidad.CalcularParidadEntrada calcularParidadEntrada = new CalcularParidad.CalcularParidadEntrada();
		calcularParidadEntrada.p_IdTrabajo = 0L;
		calcularParidadEntrada.p_IdEmpresa = idEmpresa;
		calcularParidadEntrada.p_CalculosEntrada = movimientos.stream().map(mov ->{
			CalcularParidad.CalculoEntrada calculoEntrada = new CalcularParidad.CalculoEntrada();
			calculoEntrada.IdTrabajo = 0L;
			calculoEntrada.IdInterno = mov.NumeroMvtoCmbte;
			calculoEntrada.Fecha = mov.FechaLiquidacion;
			calculoEntrada.IdMonedaOrigen = mov.IdMoneda;
			calculoEntrada.IdMonedaDestino = idMonedaSistema;
			calculoEntrada.ValorCambioLocalModificado = BigDecimal.ZERO;
			calculoEntrada.MontoOrigen = mov.Monto;
			calculoEntrada.IdUso = "";
			return calculoEntrada;
		}).collect(Collectors.toList());
		
		HashMap<Long, Object> calculoSalida = Utils.List2Hash(calcularParidad.logica(con, calcularParidadEntrada).p_out_CalculosSalida,"IdInterno"); 
	
		long fin = System.currentTimeMillis();
		log.info("FIN::CrearMovimientoComprobante.calcularParidad", fin - inicio);
		return calculoSalida;
		
	}

	public static class SecuenciaAgrupacion_Proceso {
		public Long IdCliente;
		public Long IdPortafolio;
		public String SecuenciaAgrupacion;
		public Integer Cantidad;
	}
	
	public static class MovimientoComprobante_Proceso extends MovimientoComprobante {

		public String CrearComprobanteAutomatico;
		public int ok;
		public int advertencias;
		public int autorizaciones;
		public int errores;
		public Long NumeroMvtoCmbte;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public static class MovimientoComprobanteCobro_Proceso extends MovimientoComprobanteCobro {
		public Long NumeroMvtoCmbte;
		public Long NumeroMvtoCmbteCobro;
	}
	
	public static class MovimientoComprobanteFormaDePago_Proceso extends MovimientoComprobanteFormaDePago {
		public Long NumeroMvtoCmbte;
		public Long NumeroMvtoCmbteFormaDePago;
	}
	
	public static class Mesa_Proceso {
		public Long Prioridad;
		public Long IdCliente;
		public Long IdPortafolio;
		public Long IdMesa;
		public String PorDefecto;
	}
	
	public static class MesaDefecto_Proceso {
		public Long IdMesa;
		public String PorDefecto;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CrearMovimientoComprobante_Entrada {
	
		public List<MovimientoComprobante_Entrada> p_Movimientos;
		public List<MovimientoComprobanteFormaDePago_Entrada> p_MovimientosFormaDePago;
		public List<MovimientoComprobanteCobro_Entrada> p_MovimientosCobros;
		
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_simularOperacion = "N";
		public String p_UsaReglasNegocio = "S";
		public String p_EsIngresoAutomatico = "N";
		public String p_EsCargaMasiva = "N";
		public boolean p_EsPauta =false;

		public String toString() {
			return Utils.printJson(this, false);
		}
		public CrearMovimientoComprobante_Entrada() {
			
			p_EsIngresoAutomatico = "N";
			p_Movimientos = new ArrayList<MovimientoComprobante_Entrada>();
			p_MovimientosFormaDePago = new ArrayList<MovimientoComprobanteFormaDePago_Entrada>();
			p_MovimientosCobros = new ArrayList<MovimientoComprobanteCobro_Entrada>();
		}
	}
	
	public static class ClientePersona{
		public Long IdCliente;
		public String Nombres;
		public String ApellidoPaterno;
		public String ApellidoMaterno;
		public String Estado;
		public String TipoDocumentoIdentidad;
		public String DocumentoIdentidad;
		
		public ClientePersona() {
			this.IdCliente = 0L;
			this.Nombres = "";
			this.ApellidoPaterno = "";
			this.ApellidoMaterno = "";
			this.Estado = "";
			this.TipoDocumentoIdentidad = "";
			this.DocumentoIdentidad = "";
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Portafolio {
		public Long IdPortafolio;
		public Long IdCliente;
		public String EstadoPortafolio;
	
		public Portafolio() {
			this.IdPortafolio = 0L;
			this.IdCliente = 0L;
			this.EstadoPortafolio = "";
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class MedioDePago {
		public String IdMedioDePago;
		public String TipoMedioDePago;
		public String Nombre;
		
		public MedioDePago() {
			this.IdMedioDePago = "";
			this.TipoMedioDePago = "";
			this.Nombre = "";
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Cobro{
		public String IdCobro;
		public String NombreCobro;
		
		public Cobro(){
			this.IdCobro = "";
			this.NombreCobro = "";
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class InformacionMovimientoComprobante {
		
		public Long NumeroMvtoCmbte;
		/*public String IdConcepto;
		public String IdTipoComprobante;
		public String IdCondicionDeLiquidacion;
		public String IdTipoReajuste;
		public String IdTipoConvenio;
		public String IdEmisor;
		public String IdMonedaLiquidacion;
		public Long IdInstrumento;
		public Long IdInstrumentoRelacionado;
		public Long IdEmpleadoDigitador;
		public Long IdInstrumentoSubyacente;
		public String IdTipoProducto;
		public String IdMoneda;
		public String IdMonedaSubyacente;
		public String IdTipoCanalIngreso;
		public String IdTipoOperacion;
		public String IdBoveda;
		public Long IdCorredoraContraparte;
		public String IdBolsa;
		public Long IdOrden;*/
		
     	public String IdTipoCanalIngreso;
		public String IdConceptoReal;
		public String IdTipoComprobanteReal;
		public String IdCondicionDeLiquidacionReal;
		public String IdTipoReajusteReal;
		public String IdTipoConvenioReal;
		public String IdEmisorReal;
		public String IdMonedaLiquidacionReal;
		public String IdEstadoMonedaLiquidacion;
		public Long IdInstrumentoReal;
		public Long IdInstrumentoRelacionadoReal;
		public Long IdInstrumentoSubyacenteReal;
		public Long IdEmpleadoDigitadorReal;
		public String IdTipoProductoReal;
		public String IdMonedaMovimientoComprobanteReal;
		public String IdEstadoMonedaMovimientoComprobante;
		public String IdMonedaSubyacenteReal;
		public String IdEstadoMonedaSubyacente;
		public String IdTipoCanalIngresoReal;
		public String IdTipoOperacionReal;
		public String IdBovedaReal;
		public Long IdCorredoraContraparteReal;
		public String IdBolsaReal;
		public Long IdOrdenReal;
		public String ISIN;
		public String ISINInstrumentoSubyacente;
		
		public BigDecimal CorteMinimo;
		
		public String IdTipoInstrumento;
		public String NombreTipoInstrumento;
		public String IdClasificacionTipoInstrumento;
		public String NombreClasificacionTipoInstrumento;
		
		public String IdTipoInstrumentoInstrumentoRelacionado;
		public String NombreTipoInstrumentoRelacionado;
		public String IdClasificacionTipoInstrumentoInstrumentoRelacionado;
		public String NombreClasificacionTipoInstrumentoRelacionado;
		
		public String IdTipoInstrumentoInstrumentoSubyacente;
		public String NombreTipoInstrumentoSubyacente;
		public String IdClasificacionTipoInstrumentoInstrumentoSubyacente;
		
		public String ValorModoFacturacion;

		public Long IdEmpleadoPortafolio;
		public Long IdSucursalEmpleadoPortafolio;
		public String NombreSucursalEmpleadoPortafolio;
		public String NombreCompletoEmpleadoPortafolio;
		
		public Long IdEmpleadoDigitador;
		public Long IdSucursalEmpleadoDigitador;
		public String NombreSucursalEmpleadoDigitador;
		public String NombreCompletoEmpleadoDigitador;
		
		public String CodigoTipoCanalIngreso;
		
		public String DocumentoIdentidad;
		public String TipoDocumentoIdentidad;
		public String NombreCompleto;
		public String NombreTipoCliente;
		
		public String CodigoTipoProducto;
		public String CodigoTipoOperacion;
		public String CodigoBolsa;
		public String NombreCompletoEmisor;
		
		public Long IdMovimientoComprobante;
		public String NemotecnicoInstrumento;
		public String NemotecnicoInstrumentoRelacionado;
		public String NemotecnicoInstrumentoSubyacente;
		
		public Long IdComprobanteReferencia;
		public Long FolioComprobanteReferencia;
		public Timestamp FechaComprobanteReferencia; ///aqui
		public String IdTipoComprobanteReferencia;
		public String Contrapartida;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InformacionMovimientoComprobanteFormaDePago {
		public Long NumeroMvtoCmbte;
		public Long NumeroMvtoCmbteFormaDePago;
		
		/*public String IdMedioDePago;
		public String IdModalidadDePago;
		public String IdMoneda;
		public String IdTipoCuentaBancaria;
		public Long IdTarjetaBancaria;
		public String IdTipoTarjetaBancaria;*/
		
		public String IdMedioDePagoReal;
		public String IdModalidadDePagoReal;
		public String IdMonedaFormaDePagoReal;
		public String IdTipoCuentaBancariaReal;
		public Long IdTarjetaBancariaReal;
		public String IdTipoTarjetaBancariaReal;
		
		public Long IdMovimientoComprobanteFormaDePago;
		public String NombreMedioDePago;
		public String NombreTipoCuentaBancaria;
		public String NombreTipoTarjetaBancaria;
		public String NombreModalidadDePago;
		public String CodigoBanco;
		public String NombreBanco;
		
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public static class InformacionMovimientoComprobanteCobro {
		public Long NumeroMvtoCmbte;
		public Long NumeroMvtoCmbteCobro;
		
		/*public String IdCobro;
		public String IdMonedaCobro;
		public String IdMonedaLiquidacionCobro;*/
		
		public String IdCobroReal;
		public String CodigoDefinicionCobroReal;
		public String IdMonedaReal;
		public String IdMonedaLiquidacionReal;
		
		public Long IdMovimientoComprobanteCobro;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class MovimientoComprobante_Entrada extends BcsPojo{

		public String ActualizarCustodia;
		public String ActualizarCuentaInversion;
		public String ActualizarTesoreria;
		public String ActualizarCustodiaRelacionado;
		public BigDecimal Cantidad;
		public String CrearComprobanteAutomatico;
		public String CodigoBroker;
		public String CodigoBancoCustodio;
		public String CodigoBancoLiquidador;
		
		public BigDecimal DiasPlazo;
		
		public String EsAPlazo;
		public String EsComprobIndividual;
		public String EsCuentaPropia;
		public String EsEnRueda;
		public String EsFungible;
		public String EsPrecioPrima;
		public String EsRuteo;
		public String EsVentaCorta = "N";
		public String EsClienteFinal = "S";
		
		public String FechaEmision = "0001-01-01";
		public String FechaLiquidacion;
		public String FechaMovimientoComprobante;
		public String FechaOrden = "0001-01-01";
		public String FechaParidad = "0001-01-01";
		public String FechaTipoCambio = "0001-01-01";
		public String FechaTransaccion = "0001-01-01";
		public String FechaVencimiento = "0001-01-01";
		
		public String FechaHoraMovimientoComprobante = "0001-01-01 00:00:00";
		public Long FolioCartera;
		public String FolioTransaccion;
		
		public String Glosa;
		public String Referencia;
		public String TipoReferencia;
		
		public Long IdAsignacion;
		public Long IdAsignacionRef;
		public String IdBolsa;
		public String IdBoveda;
		public Long IdCliente;
		public String IdConcepto;
		public Long IdComprobanteReferencia;
		public String IdTipoComprobanteReferencia;
		public String IdCondicionLiquidacion;
		public Long IdCorredoraContraparte;
		public Long IdCuentaCustodioPortafolio;
		public String IdEmisor;
		public Long IdEmpleadoDigitador;                          
		public Long IdEmpresa;
		public String IdFormaCustodia;
		public Long IdInstrumento;
		public Long IdInstrumentoRelacionado;
		public Long IdInstrumentoSubyacente;
		public Long IdMesa = 0L;
		public String IdMoneda; // IdMonedaInstrumento
		public String IdMonedaEmision = "";
		public String IdMonedaLiquidacion;
		public String IdMonedaSubyacente;
		public String IdMonedaSpot;
		public String IdMonedaNegociacion;
		public Long IdMovimientoRelacionado;
		public String IndicadorOrdenDirecta;
		public Long IdOrden;
		public Long IdPortafolio;
		public String IdTipoComprobante;
		public String IdTipoOperacion;
		public String IdTipoProducto;
		public String IdTipoReajuste;
		public String IdTipoConvenio;
		public String IdTipoCanalIngreso;
		public String IdTipoCliente = "";
		public String IdTipoLiquidacionComisionFactura;
		public Long IdTransaccion;
		public Long IdUsuario;
		public Long IdPersonaBroker;
		public Long IdPersonaBancoCustodio;
		public Long IdPersonaBancoLiquidador;
		public BigDecimal Intereses = BigDecimal.ZERO;
		
		public String LiquidaBancoCustodio  = "N";
		
		public String MercadoSMV;
		public String ModalidadSMV;
		public String ModPrefFact;
		public BigDecimal Monto;
		public BigDecimal MontoRaiz;
		public BigDecimal MontoMovimientoComprobante;
		public BigDecimal MontoLiquidacion;
		public BigDecimal MontoDescuento;
		public BigDecimal MontoInteres;
		public BigDecimal MontoAPlazo;
		
		public Long NumeroMvtoCmbte;	//CORRELATIVO
		public String NemotecnicoInstrumento;
		public String NemoSVS = "";
		
		public BigDecimal Precio;
		public BigDecimal PrecioRaiz;
		public BigDecimal PrecioAPlazo;
		
		public BigDecimal ReajustesVAC = BigDecimal.ZERO;
		public String SecuenciaAgrupacion;
		public BigDecimal Spread;
		
		public BigDecimal Tasa = BigDecimal.ZERO;
		public BigDecimal TasaAgente;
		public BigDecimal TasaEmision;
		public BigDecimal TasaOperacion;
		
		public BigDecimal ValorRescate;
		public BigDecimal ValorParidad;
		public BigDecimal ValorTipoCambio;
		public BigDecimal ValorCaptacionMonedaOrigen;
		public String CodigoIFRS;
		
		public Long IdOperacion;
		
		public String Contrapartida;

		public MovimientoComprobante_Entrada() {
			this.IdTipoLiquidacionComisionFactura = "";
			this.ModPrefFact = "";
			this.Contrapartida = "N";
		}
	}
	
	public static class MovimientoComprobanteFormaDePago_Entrada {
		public Long NumeroMvtoCmbte;
		public Long NumeroMvtoCmbteFormaDePago;
		
		public String IdMedioDePago;
		public String NumeroSerie;
		public Long NumeroDocumento;
		public Long IdCuentaBancaria;
		public String IdTipoCuentaBancaria;
		public String  NumeroCuentaBancaria;
	    public Long IdTarjetaBancaria;
	    public String IdTipoTarjetaBancaria;
	    public String NumeroTarjetaBancaria;
	    public String FechaVctoTarjetaBancaria;
	    public String IdModalidadDePago;
	    public Long IdBanco;
	    public String IdMoneda;
	    public String FechaDocumentoPago;
	    public BigDecimal MontoLiquidacionFormaDePago;
	    public String IdTipoMovimientoSpot = "";
	    public String AfectarTesoreria = "S";
	    public String AfectarCuentaInversion = "N";
	    
	    public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class MovimientoComprobanteCobro_Entrada {
		public Long NumeroMvtoCmbte;
		public Long NumeroMvtoCmbteCobro;
	    public String IdCobro;
	    public String IdMonedaCobro;
	    public String IdMonedaLiquidacionCobro;
	    public BigDecimal MontoCobro;
	    public BigDecimal MontoMovimientoComprobanteCobro;
	    public BigDecimal MontoLiquidacionCobro;
	    public BigDecimal Porcentaje;
	    
	    public MovimientoComprobanteCobro_Entrada() {
	    	this.MontoCobro = BigDecimal.ZERO;
	    }
	    
	    public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CrearMovimientoComprobante_Salida {
		
		public List<MovimientoComprobante_Salida> p_out_Movimientos;
		public List<Comprobante_Salida> p_out_Comprobantes;
		public List<PlataformaUtils.MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		public List<ResultadoProceso_Salida> p_out_ResultadoProceso;
		public List<OUT_InformacionDetalle> 	p_out_InformacionDetalle;
		
		public CrearMovimientoComprobante_Salida() {
			this.p_out_Comprobantes = new ArrayList<>();
			this.p_out_Movimientos = new ArrayList<>();
			this.P_OUT_MENSAJES = new ArrayList<>();
			this.p_out_InformacionDetalle = new ArrayList<OUT_InformacionDetalle>();
			this.p_out_ResultadoProceso = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class OUT_InformacionDetalle {
		public String		Codigo; 
		public String		Descripcion; 
	}
	
	public static class MovimientoComprobante_Salida {
		public Long IdMovimientoComprobante;
		public Long NumeroMvtoCmbte;
		public Long IdOperacion = 0L;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class Comprobante_Salida {
		public Long IdComprobante;
		public Long IdMovimientoComprobante;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class ResultadoProceso_Salida {
		public String Campo;
		public Long Id;
		public String Servicio;
		public String Resultado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class MovimientoContrapartida extends BcsPojo{
		public MovimientoComprobante_Entrada Cargo;
		
		MovimientoContrapartida(){
			this.Cargo = new MovimientoComprobante_Entrada();
		}
	}
	
	public static class ConceptoIndicadorAbonoClpContrapartida extends BcsPojo {
		public String IdConcepto;
		public String Nombre;
		public String Servicio;
		public String Resultado;
	}
	
	public static class MovimientoComprobanteAnterior extends BcsPojo {
		public Long IdComprobanteReferencia;
		public BigDecimal Saldo;
	}
	
	public static void main(String[] args) throws Exception {		
				
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			try {
				Gson gson = Utils.getGsonBuilder();
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdModulo\":\"FAC\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_EsIngresoAutomatico\":\"N\",\"P_COMPROBANTE\":[{\"IdPortafolio\":1648,\"IdCliente\":2464,\"IdComprobante\":852141,\"IdTipoComprobante\":\"FEE\"}],\"p_CrearComprobanteAutomatico\":\"S\"}";
				
				CrearMovimientoComprobante_Entrada Objin = gson.fromJson(json, CrearMovimientoComprobante_Entrada.class);
								
				CrearMovimientoComprobante  kk = new CrearMovimientoComprobante();
				CrearMovimientoComprobante_Salida Objout = kk.logica(con, Objin);				
				System.out.println(Objout.toString());
				
		    	}catch(Exception er) {
					er.printStackTrace();	
					con.rollback();
				}
		} catch (Exception e) {
			e.printStackTrace();			
		}	
		System.out.println("== END ==");
	};	
}

	