package cl.bcs.facturacion;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.facturacion.ConsultarDetalleAnularMovimientos.AsignacionMasiva;
import cl.bcs.facturacion.ConsultarDetalleAnularMovimientos.ConsultarDetalleAnularMovimientos_Entrada;
import cl.bcs.facturacion.ConsultarDetalleAnularMovimientos.ConsultarDetalleAnularMovimientos_Salida;
import cl.bcs.facturacion.ConsultarDetalleAnularMovimientos.DetalleMovimientoAnular;
import cl.bcs.ordenes.AnularAsignaciones;
import cl.bcs.ordenes.AnularAsignaciones.AnularAsignaciones_Entrada;
import cl.bcs.ordenes.AnularAsignaciones.AnularAsignaciones_Salida;
import cl.bcs.ordenes.AnularAsignaciones.AsignacionSalida;
import cl.bcs.ordenes.AnularAsignaciones.Asignacion_Entrada;
import cl.bcs.ordenes.AprobarRechazarSolicitudAnulacionOrdenes;
import cl.bcs.ordenes.AprobarRechazarSolicitudAnulacionOrdenes.AnularOrden;
import cl.bcs.ordenes.AprobarRechazarSolicitudAnulacionOrdenes.EntradaAprobarRechazarSolicitudAnulacionOrdenes;
import cl.bcs.ordenes.AprobarRechazarSolicitudAnulacionOrdenes.SalidaAprobarRechazarSolicitudAnulacionOrdenes;
import cl.bcs.ordenes.utils.OrdenUtils;
import cl.bcs.ordenes.utils.OrdenUtils.AdvertenciaDetalle;
import cl.bcs.ordenes.utils.OrdenUtils.ErrorDetalle;
import cl.bcs.ordenes.utils.OrdenUtils.InformacionDetalle;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.OutAgregarLog;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import java.util.HashMap;
import cl.bcs.utils.Utils;
import java.util.stream.Collectors;

import datadog.trace.api.Trace;

public class AnularMovimientosPorAsignacionMasiva implements JavaCall {
	static final Logger logger = LogManager.getLogger(AnularMovimientosPorAsignacionMasiva.class);
	
	private final String SQL_SELECT_MOVIMIENTOS = loadResourceToString("cl/bcs/facturacion/sql/AnularMovimientosPorAsignacionMasiva_SelectMovimientos.sql");
	private final String SQL_SELECT_ASIGNACIONES_MOVIMIENTOS = loadResourceToString("cl/bcs/facturacion/sql/AnularMovimientosPorAsignacionMasiva_SelectAsignacionesMovimientos.sql");
	private final String SQL_SELECT_ORDENES_OFERTADAS = loadResourceToString("cl/bcs/facturacion/sql/AnularMovimientosPorAsignacionMasiva_SelectOrdenesOfertadas.sql");

	public static class AnularMovimientosPorAsignacionMasiva_Entrada extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_SimularOperacion;
		public String p_UsaReglasNegocio;
		public String p_EsIngresoAutomatico = "N";
		public String p_Glosa = "";
		public String p_Fecha;
		public String p_IdClasificacionTipoInstrumento ;
		public String p_Operacion ;
		public Long p_IdLibroOrdenes;
		public Long p_SecuenciaOrden;
		public String p_HoraDesde;
		public String p_HoraHasta;
		public String p_IdTipoInstrumento;
		public String p_IdCondicionDeLiquidacion;
		public String p_Bolsa;
		public String p_FolioTransaccion ;
		public Long p_IdInstrumento;
		public String p_IdTipoCanalIngreso ;
		public String p_MarcaAsignacion;
		public Long p_IdPortafolio;
		public String p_FiltraPorAsignacionMasiva;
		public List<Movimiento_Anular> p_IdAsignacionMasiva;
		
		public AnularMovimientosPorAsignacionMasiva_Entrada () {
			this.p_IdEmpresa = 1L;
			this.p_IdUsuario = 0L;
			this.p_IdModulo = "FAC";
			this.p_UsaReglasNegocio = "N";
		}
	}

	public static class Movimiento_Anular extends BcsPojo {
		public Long p_IdAsignacionMasiva;
	}

	public static class AsignacionesMovimientosAnulados {
		public Long IdAsignacion;
	}
	
	public static class TransaccionDesasignada {
		public String FolioTransaccion;
		public Long IdInstrumento;
		
		public TransaccionDesasignada(String folioTransaccion, Long idInstrumento) {
			this.FolioTransaccion = folioTransaccion;
			this.IdInstrumento = idInstrumento;
		}
	}

	public static class OrdenOfertada {
		public Long	IdOrden;
	}

	public static class AnularMovimientosPorAsignacionMasiva_Salida extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public List<TransaccionDesasignada> P_OUT_TRANSACCIONES_DESASIGNADAS;
		
		public AnularMovimientosPorAsignacionMasiva_Salida () {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_TRANSACCIONES_DESASIGNADAS = new ArrayList<TransaccionDesasignada>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class MovimientosAnularMovimientosPorAsignacionMasiva extends BcsPojo {
		public Long IdMovimientoComprobante;
	}
	
	private void registrarLog(Connection conn,AnularMovimientosPorAsignacionMasiva_Entrada in, List<DetalleMovimientoAnular> detalleMovimientos) throws Exception {
		
		AgregarLog agregarLogService = new AgregarLog();
		InAgregarLog agregarLogEntrada = new InAgregarLog();
		
		agregarLogEntrada.p_IdEmpresa = in.p_IdEmpresa;
		agregarLogEntrada.p_IdUsuario = in.p_IdUsuario;
		agregarLogEntrada.p_IdModulo = in.p_IdModulo;
		agregarLogEntrada.p_UsaReglasNegocio = "S";
		agregarLogEntrada.p_IdEstadoAutorizacion = "S";
		agregarLogEntrada.p_IdTrabajo = 0L;
		
		Long idCabeceraLog =  (long)(Math.random()*10000);
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		
		agregarLogEntrada.agregarLogBasico(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo, idCabeceraLog, 0L, "ANULACION", "AnularMovimientosPorAsignacionMasiva", fechaSistema, "AnularMovimientosPorAsignacionMasiva");
		
		for (int i = 0; i < detalleMovimientos.size() ; i++) {
			DetalleMovimientoAnular detalle = detalleMovimientos.get(i);
			
			int numeroRegistro = i + 1;
			
			String subRegistro = String.format("ANU%03d", numeroRegistro);
			String campoMovimiento = String.format("MovimientoComprobante: %s", detalle.SecuenciaMovimiento);
			String campoAsignacion = String.format("Asignacion: %s", detalle.SecuenciaAsignacion);
			String campoTransaccion = String.format("Transaccion: %s", detalle.FolioTransaccion);			
			
			//MOVIMIENTO
			agregarLogEntrada.agregarDetalleLogBasico(idCabeceraLog, 0L, campoMovimiento, "Ingresado", "Anulado", subRegistro);
			
			//ASIGNACION
			agregarLogEntrada.agregarDetalleLogBasico(idCabeceraLog, 0L, campoAsignacion, "Enviado a Facturar", "Anulado", subRegistro);
			
			//TRANSACCION
			agregarLogEntrada.agregarDetalleLogBasico(idCabeceraLog, 0L, campoTransaccion, "Asignado", "Liberado", subRegistro);
			
		}
		
		OutAgregarLog agregarLogSalida = agregarLogService.logica(conn, agregarLogEntrada);
	}
	
	@Trace
	public AnularMovimientosPorAsignacionMasiva_Salida logica(Connection conn, AnularMovimientosPorAsignacionMasiva_Entrada in) throws Exception {
		logger.info("INICIO::AnularMovimientosPorAsignacionMasiva");
		long inicio = System.currentTimeMillis();
		
		SQL _sql = new SQL();

		AnularMovimientosPorAsignacionMasiva_Salida out = new AnularMovimientosPorAsignacionMasiva_Salida();

		
		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(conn, in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
		if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
			out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
			return out;
		}

		ConsultarDetalleAnularMovimientos servicioConsultarDetalleMovimientos = new ConsultarDetalleAnularMovimientos();
		
		ConsultarDetalleAnularMovimientos_Entrada entradaDetalleMovimientos = new ConsultarDetalleAnularMovimientos_Entrada();
		
		entradaDetalleMovimientos.p_IdEmpresa = in.p_IdEmpresa;
		entradaDetalleMovimientos.p_IdUsuario = in.p_IdUsuario;
		entradaDetalleMovimientos.p_IdModulo = in.p_IdModulo;
		entradaDetalleMovimientos.p_SimularOperacion = in.p_SimularOperacion;
		entradaDetalleMovimientos.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
		entradaDetalleMovimientos.p_EsIngresoAutomatico = in.p_EsIngresoAutomatico;
		entradaDetalleMovimientos.p_Glosa = in.p_Glosa;
		entradaDetalleMovimientos.p_Fecha = in.p_Fecha;
		entradaDetalleMovimientos.p_IdClasificacionTipoInstrumento = in.p_IdClasificacionTipoInstrumento;
		entradaDetalleMovimientos.p_Operacion = in.p_Operacion;
		entradaDetalleMovimientos.p_IdLibroOrdenes = in.p_IdLibroOrdenes;
		entradaDetalleMovimientos.p_SecuenciaOrden = in.p_SecuenciaOrden;
		entradaDetalleMovimientos.p_HoraDesde = in.p_HoraDesde;
		entradaDetalleMovimientos.p_HoraHasta = in.p_HoraHasta;
		entradaDetalleMovimientos.p_IdTipoInstrumento = in.p_IdTipoInstrumento;
		entradaDetalleMovimientos.p_IdCondicionDeLiquidacion = in.p_IdCondicionDeLiquidacion;
		entradaDetalleMovimientos.p_Bolsa = in.p_Bolsa;
		entradaDetalleMovimientos.p_FolioTransaccion = in.p_FolioTransaccion;
		entradaDetalleMovimientos.p_IdInstrumento = in.p_IdInstrumento;
		entradaDetalleMovimientos.p_IdTipoCanalIngreso = in.p_IdTipoCanalIngreso;
		entradaDetalleMovimientos.p_IdPortafolio = in.p_IdPortafolio;
		entradaDetalleMovimientos.p_MarcaAsignacion = in.p_MarcaAsignacion;
		entradaDetalleMovimientos.p_FiltraPorAsignacionMasiva = in.p_FiltraPorAsignacionMasiva;
		
		entradaDetalleMovimientos.p_IdAsignacionMasiva = new ArrayList<>();
		
		for(Movimiento_Anular movimiento : in.p_IdAsignacionMasiva) {
			AsignacionMasiva asignacionPorAnular = new AsignacionMasiva();
			asignacionPorAnular.p_IdAsignacionMasiva = movimiento.p_IdAsignacionMasiva;			
			entradaDetalleMovimientos.p_IdAsignacionMasiva.add(asignacionPorAnular);
		}
		
		ConsultarDetalleAnularMovimientos_Salida detalleMovimientosPorAnular = servicioConsultarDetalleMovimientos.logica(conn, entradaDetalleMovimientos);
		
		if(detalleMovimientosPorAnular.P_OUT_ERRORESCONTADOR > 0L) {
			out.P_OUT_MENSAJES.addAll(detalleMovimientosPorAnular.P_OUT_MENSAJES);
			return out;
		}

		AnularMovimientoComprobante service = new AnularMovimientoComprobante();
		AnularMovimientoComprobante.AnularMovimientoComprobante_Entrada entrada = new AnularMovimientoComprobante.AnularMovimientoComprobante_Entrada();
		entrada.p_IdEmpresa = in.p_IdEmpresa;
		entrada.p_IdUsuario = in.p_IdUsuario;
		entrada.p_IdModulo = in.p_IdModulo;
		entrada.p_simularOperacion = in.p_SimularOperacion;
		entrada.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
		entrada.p_EsIngresoAutomatico = in.p_EsIngresoAutomatico;
		entrada.p_Glosa = in.p_Glosa;
		entrada.p_MovimientoAAnular = detalleMovimientosPorAnular.P_OUT_DETALLE_MOVIMIENTOS.stream().map(detalle -> {
			AnularMovimientoComprobante.Movimiento_Entrada mv = new AnularMovimientoComprobante.Movimiento_Entrada();
		    mv.IdMovimientoComprobante = detalle.SecuenciaMovimiento;
		    return mv;
		}).collect(Collectors.toList());

		AnularMovimientoComprobante.AnularMovimientoComprobante_Salida anularMovimientoComprobante_Salida = service.logica(conn, entrada);
		
		if (anularMovimientoComprobante_Salida != null) {
			out.P_OUT_MENSAJES.addAll(anularMovimientoComprobante_Salida.P_OUT_MENSAJES);
			out.P_OUT_ERRORESCONTADOR += anularMovimientoComprobante_Salida.P_OUT_ERRORESCONTADOR;
		}
		
		if (out.P_OUT_ERRORESCONTADOR > 0L) {
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_ANULAR_MOVIMIENTOS", new HashMap<String,String>(0), conn, entrada.p_IdEmpresa));
			return out;
		}
		
		List<Long> movimientosAnulados = anularMovimientoComprobante_Salida.P_OUT_RESULTADOPROCESO.stream()
										.filter(mov -> "IdMovimientoComprobante".equals(mov.Campo) && "ANU".equals(mov.Resultado))
										.map(mov -> mov.Id)
										.collect(Collectors.toList());
		
		HashMap<String,Object> params = new HashMap<String,Object>(1);
		params.put("IdsMovimientoComprobante", movimientosAnulados);
		
		AnularAsignaciones servicioAnularAsignaciones = new AnularAsignaciones();
		
		AnularAsignaciones_Entrada entradaAnularAsignaciones = new AnularAsignaciones_Entrada();
		
		entradaAnularAsignaciones.p_IdEmpresa = in.p_IdEmpresa;
		entradaAnularAsignaciones.p_IdUsuario = in.p_IdUsuario;
		entradaAnularAsignaciones.p_IdModulo = in.p_IdModulo;
		entradaAnularAsignaciones.p_AnulaMovimientosFacturar = "N";
		entradaAnularAsignaciones.asignacionesParaAnular = _sql.query(conn, SQL_SELECT_ASIGNACIONES_MOVIMIENTOS, params, Asignacion_Entrada.class);
		
		AnularAsignaciones_Salida salidaAnularAsignaciones = servicioAnularAsignaciones.logica(conn, entradaAnularAsignaciones);
		
		if (salidaAnularAsignaciones != null) {
			out.P_OUT_MENSAJES.addAll(salidaAnularAsignaciones.p_out_Mensajes);
			out.P_OUT_ERRORESCONTADOR += salidaAnularAsignaciones.P_OUT_ERRORESCONTADOR;
		}
		
		if (out.P_OUT_ERRORESCONTADOR > 0L) {
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_ANULAR_MOVIMIENTOS", new HashMap<String,String>(0), conn, entrada.p_IdEmpresa));
			return out;
		}
		
		if(salidaAnularAsignaciones.p_out_AsignacionSalida != null && salidaAnularAsignaciones.p_out_AsignacionSalida.size() > 0) {
			for(AsignacionSalida asignacionAnulada : salidaAnularAsignaciones.p_out_AsignacionSalida) {
				TransaccionDesasignada transaccionDesasignada = new TransaccionDesasignada(asignacionAnulada.FolioTransaccion, asignacionAnulada.IdInstrumentoUI);
				out.P_OUT_TRANSACCIONES_DESASIGNADAS.add(transaccionDesasignada);
			}
		}
		
		params.clear();
		List<Long> idsOrdenes = salidaAnularAsignaciones.p_out_AsignacionSalida.stream().map(x -> x.IdOrden).collect(Collectors.toList());
		params.put("IdsOrden", idsOrdenes);
		
		List<OrdenOfertada> ordenesOfertadas = _sql.query(conn, SQL_SELECT_ORDENES_OFERTADAS, params, OrdenOfertada.class);
		
		if(!ordenesOfertadas.isEmpty()) {
			AprobarRechazarSolicitudAnulacionOrdenes servicioAnularOrdenes = new AprobarRechazarSolicitudAnulacionOrdenes();
			EntradaAprobarRechazarSolicitudAnulacionOrdenes anularOrdenesEntrada = new EntradaAprobarRechazarSolicitudAnulacionOrdenes();
			anularOrdenesEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
			anularOrdenesEntrada.p_IdUsuario = entrada.p_IdUsuario;
			anularOrdenesEntrada.p_IdModulo = entrada.p_IdModulo;
			anularOrdenesEntrada.p_Opcion = "APROBAR";
			
			for(Long idOrden: idsOrdenes) {
				AnularOrden ordenPorAnular = new AnularOrden();
				ordenPorAnular.IdOrden = idOrden;
				ordenPorAnular.MotivoAnulacion = "Anulacion Movimientos a Facturar";
				anularOrdenesEntrada.p_ordenes.add(ordenPorAnular);
			}
			
			SalidaAprobarRechazarSolicitudAnulacionOrdenes salidaAnularOrdenes = servicioAnularOrdenes.logica(conn, anularOrdenesEntrada);
			
			if (salidaAnularOrdenes != null) {
				out.P_OUT_ERRORESCONTADOR += salidaAnularOrdenes.P_OUT_ERRORESCONTADOR;
				
				if (salidaAnularOrdenes.P_OUT_INFORMACIONDETALLE != null) {
			        for (OrdenUtils.InformacionDetalle informacion : salidaAnularOrdenes.P_OUT_INFORMACIONDETALLE) {
			        	HashMap<String,String> paramsMensajeOrden = new HashMap<String,String>(1);
			        	paramsMensajeOrden.put("IdOrden", informacion.IdOperacion.toString());
			        	out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_INFORMACION, informacion.Codigo, paramsMensajeOrden, conn, entrada.p_IdEmpresa));
			        }
			    }
				
			}
			
			if (out.P_OUT_ERRORESCONTADOR > 0L) {
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_ANULAR_ORDEN", new HashMap<String,String>(0), conn, entrada.p_IdEmpresa));
				return out;
			}		
		}
		
		this.registrarLog(conn, in, detalleMovimientosPorAnular.P_OUT_DETALLE_MOVIMIENTOS);
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::AnularMovimientosPorAsignacionMasiva: {}", fin - inicio);
		return out;
	}

	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = getGsonBuilder();
		AnularMovimientosPorAsignacionMasiva_Entrada in = gson.fromJson(jin.toString(), AnularMovimientosPorAsignacionMasiva_Entrada.class);
		AnularMovimientosPorAsignacionMasiva_Salida out = logica(con, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}	
	
	public static void main(String[] args) throws Exception {
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa02", "connections.properties"))
		{
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9563,\"p_IdModulo\":\"FAC\",\"p_SimularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_EsIngresoAutomatico\":\"N\",\"p_Glosa\":\"\",\"p_IdAsignacionMasiva\":[],\"p_FiltraPorAsignacionMasiva\":\"N\",\"p_Fecha\":\"2026-07-06\",\"p_HoraHasta\":\"23:00:00\",\"p_IdPortafolio\":1124}\r\n"
					+ "";
		
			AnularMovimientosPorAsignacionMasiva_Entrada Objin = gson.fromJson(json, AnularMovimientosPorAsignacionMasiva_Entrada.class);
			AnularMovimientosPorAsignacionMasiva demo = new AnularMovimientosPorAsignacionMasiva();		
 			try {
 				AnularMovimientosPorAsignacionMasiva_Salida ObjOut = demo.logica(conn, Objin);
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
 				er.printStackTrace();
				conn.rollback();
			}
 			// conn.commit();
	    }
		 	
		System.out.println("== END ==");
	}
	
}
