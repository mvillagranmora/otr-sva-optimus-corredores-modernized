package cl.bcs.facturacion;


import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.facturacion.AnularComprobanteFinal.OperacionSpot;
import cl.bcs.facturacion.pojo.MovimientoComprobante;
import cl.bcs.ordenes.AnularMovFacturarAsignacion;
import cl.bcs.ordenes.AnularMovFacturarAsignacion.AnularMovFacturarAsignacion_Entrada;
import cl.bcs.ordenes.AnularMovFacturarAsignacion.AnularMovFacturarAsignacion_Salida;
import cl.bcs.ordenes.AnularMovFacturarAsignacion.Asignacion_Entrada;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class AnularMovimientoComprobante implements JavaCall {

	// Log
	static final Logger logger = LogManager.getLogger(AnularMovimientoComprobante.class);
	
	private final String SQL_SELECT_MOVIMIENTOS = loadResourceToString("cl/bcs/facturacion/sql/AnularMovimientoComprobante_SelectMovimientos.sql");
	private final String SQL_UPDATE_MOVIMIENTOS = loadResourceToString("cl/bcs/facturacion/sql/AnularMovimientoComprobante_UpdateMovimientos.sql");
	private final String SQL_SELECT_SPOT_ANULADO_FFMM	= loadResourceToString("cl/bcs/facturacion/sql/AnularMovimientoComprobante_SelectSpotFFMM.sql");
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = getGsonBuilder();
		AnularMovimientoComprobante_Entrada in = gson.fromJson(jin.toString(), AnularMovimientoComprobante_Entrada.class);
		AnularMovimientoComprobante_Salida out = logica(con, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}	
	
	public static class MovimientoComprobante_Proceso extends MovimientoComprobante {

		public int errores;
		public int autorizaciones;
		public int advertencias;
		public int ok;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	private void ProcesaRequisito(Connection conn,Long idEmpresa,boolean esError,String codMsg,String codRequisito,MovimientoComprobante_Proceso mov,List<MensajeSalida> P_OUT_MENSAJES) throws Exception {
		if (esError) {
			String msg = Diccionario.getMensaje(conn, idEmpresa,codMsg)
					.add("IdMovimientoComprobante", mov.IdMovimientoComprobante)
					.msg();
			
			if (codRequisito.equals(MensajeSalida.MENSAJE_ERROR)) {
				P_OUT_MENSAJES.add(new MensajeSalida(mov.IdMovimientoComprobante,MensajeSalida.MENSAJE_ERROR,codMsg,msg));
				mov.errores++;
			}else if (codRequisito.equals(MensajeSalida.MENSAJE_AUTORIZACION)) {
				P_OUT_MENSAJES.add(new MensajeSalida(mov.IdMovimientoComprobante,MensajeSalida.MENSAJE_AUTORIZACION,codMsg,msg));
				mov.autorizaciones++;						
			}
			else if (codRequisito.equals(MensajeSalida.MENSAJE_ADVERTENCIA)) {
				P_OUT_MENSAJES.add(new MensajeSalida(mov.IdMovimientoComprobante,MensajeSalida.MENSAJE_ADVERTENCIA,codMsg,msg));
				mov.advertencias++;
			}
			else {
				mov.ok++;
			}
		}
		else {
			mov.ok++;
		}
	}
	
	@Trace
	public AnularMovimientoComprobante_Salida logica(Connection conn, AnularMovimientoComprobante_Entrada in) throws Exception {
		logger.info("INICIO Anular Movimiento Comprobante");
		logger.debug(in.toString());
		AnularMovimientoComprobante_Salida salida = new AnularMovimientoComprobante_Salida();
		SQL _sql = new SQL();
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		
		if (in.p_MovimientoAAnular.size() == 0) {
			salida.P_OUT_ERRORESCONTADOR++;
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_INGRESARON_MOVIMIENTOS",null,conn,in.p_IdEmpresa));
			logger.info(salida.toString());
			logger.info("Fin");
			return salida;
		}
		
		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(conn, in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
		salida.P_OUT_ERRORESCONTADOR += MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes);
		if (salida.P_OUT_ERRORESCONTADOR > 0) {
			salida.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
			logger.info(salida.toString());
			logger.info("Fin");
			return salida;
		}
		
		List<MovimientoComprobante_Proceso> movimientos = new ArrayList<>();
		List<MovimientoComprobante_Proceso> movimientosConError = new ArrayList<>();
		
		HashMap<Long,Object> hashMovimientos = new HashMap<>();
		{
			HashMap<String,Object> params = new HashMap<>();
			List<Long> idsMovimiento = new ArrayList<>();
			for (Movimiento_Entrada mov : in.p_MovimientoAAnular) {
				if (!idsMovimiento.contains(mov.IdMovimientoComprobante)) {
					idsMovimiento.add(mov.IdMovimientoComprobante);	
				}
				
			}
			params.put("idsMovimiento", idsMovimiento);
			List<MovimientoComprobante> _movs = _sql.query(conn, SQL_SELECT_MOVIMIENTOS, params , MovimientoComprobante.class);
			hashMovimientos = Utils.List2Hash(_movs, "IdMovimientoComprobante");
		}
		
		for (Movimiento_Entrada mov : in.p_MovimientoAAnular ) {
			MovimientoComprobante_Proceso c = new MovimientoComprobante_Proceso();
			Utils.copyPojo(mov, c);
			movimientos.add(c);
		}
		
		//VALIDACIONES
		for (MovimientoComprobante_Proceso mov : movimientos) {
			MovimientoComprobante infoMov = (MovimientoComprobante)hashMovimientos.get(mov.IdMovimientoComprobante);
			ProcesaRequisito(conn, in.p_IdEmpresa, infoMov == null, "MOVIMIENTO_NO_EXISTE", MensajeSalida.MENSAJE_ERROR, mov, salida.P_OUT_MENSAJES);
		}
		
		limpiarMovimientos(movimientos, movimientosConError, salida.P_OUT_RESULTADOPROCESO);
		if (movimientos.size() == 0) {
			salida.P_OUT_ERRORESCONTADOR += MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
			logger.info(salida.toString());
			logger.info("Fin");
			return salida;
		}
		
		for (MovimientoComprobante_Proceso mov : movimientos) {
			MovimientoComprobante infoMov = (MovimientoComprobante)hashMovimientos.get(mov.IdMovimientoComprobante);
			ProcesaRequisito(conn, in.p_IdEmpresa, !infoMov.IdEstadoFacturacion.equalsIgnoreCase("ING"), "MVTO_CMBTE_DIS_ING", MensajeSalida.MENSAJE_ERROR, mov, salida.P_OUT_MENSAJES);
			
			//Cuando el canal sea FFMM verificara si se encuentra anulado el spot
			if("FMM".equalsIgnoreCase(infoMov.IdTipoCanalIngreso) && ("VENSPT".equalsIgnoreCase(infoMov.IdTipoOperacion) || "COMSPT".equalsIgnoreCase(infoMov.IdTipoOperacion))) {
				HashMap<String,Object> paramCmbte = new HashMap<>();
				paramCmbte.put("idComprobante",mov.IdMovimientoComprobante.toString());
				List<OperacionSpot> IdOperacionSpot = _sql.query(conn, SQL_SELECT_SPOT_ANULADO_FFMM,paramCmbte, OperacionSpot.class);
				if(IdOperacionSpot.size() == 0)
					ProcesaRequisito(conn, in.p_IdEmpresa, infoMov.IdTipoProducto.equalsIgnoreCase("MEME") && in.p_EsIngresoAutomatico.equalsIgnoreCase("N"), "MVTO_CMBTE_SPOT_NO_ANULA", MensajeSalida.MENSAJE_ERROR, mov, salida.P_OUT_MENSAJES);
			}
			
			Utils.copyPojo(infoMov, mov);
			
			//TODO VALIDACION DE PERIODO CERRADO
		}
		
		limpiarMovimientos(movimientos, movimientosConError, salida.P_OUT_RESULTADOPROCESO);
		if (movimientos.size() == 0) {
			salida.P_OUT_ERRORESCONTADOR += MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
			logger.info(salida.toString());
			logger.info("Fin");
			return salida;
		}
		
		//BUSCAR MOVIMIENTO DE REPORTES O SIMULTANEA ASOCIADO
		//TODO REPORTES
		List<Long> idsMovimientoRelacionado = new ArrayList<>();
		for (MovimientoComprobante_Proceso mov : movimientos) {
			MovimientoComprobante infoMov = (MovimientoComprobante)hashMovimientos.get(mov.IdMovimientoComprobante);
			
			if ("SIM".equalsIgnoreCase(infoMov.IdTipoInstrumento) && infoMov.IdMovimientoRelacionado != null && infoMov.IdMovimientoRelacionado > 0 && !hashMovimientos.containsKey(infoMov.IdMovimientoRelacionado)) {
				if (!idsMovimientoRelacionado.contains(infoMov.IdMovimientoComprobante)) {
					idsMovimientoRelacionado.add(infoMov.IdMovimientoRelacionado);
				}
			}
		}
		
		if (idsMovimientoRelacionado.size() > 0) {
			HashMap<String,Object> params = new HashMap<>();
			params.put("idsMovimiento", idsMovimientoRelacionado);
			List<MovimientoComprobante> _movs = _sql.query(conn, SQL_SELECT_MOVIMIENTOS, params , MovimientoComprobante.class);
			for (MovimientoComprobante mov : _movs) {
				MovimientoComprobante_Proceso movp = new MovimientoComprobante_Proceso();
				movp.IdMovimientoComprobante = mov.IdMovimientoComprobante;
				hashMovimientos.put(movp.IdMovimientoComprobante, movp);
				movimientos.add(movp);
			}
		}

		//actualiza estado
		if (in.p_simularOperacion.equalsIgnoreCase("N")) {
			
			//cambia estado asignacion
			{
				AnularMovFacturarAsignacion_Entrada entradaAnularMovFact = new AnularMovFacturarAsignacion_Entrada();
				for (MovimientoComprobante_Proceso mov : movimientos) {
					if (mov.IdAsignacion != null && mov.IdAsignacion > 0) {
						Asignacion_Entrada arg0 = new Asignacion_Entrada();
						arg0.IdAsignacion = mov.IdAsignacion;
						entradaAnularMovFact.p_Asignaciones.add(arg0 );
					}
				}
				if (entradaAnularMovFact.p_Asignaciones.size() > 0) {
					entradaAnularMovFact.p_IdEmpresa = in.p_IdEmpresa;
					entradaAnularMovFact.p_IdModulo = in.p_IdModulo;
					entradaAnularMovFact.p_IdUsuario = in.p_IdUsuario;
					AnularMovFacturarAsignacion service = new AnularMovFacturarAsignacion();
					AnularMovFacturarAsignacion_Salida salidaAnularMovFact = service.logica(conn, entradaAnularMovFact);
					salida.P_OUT_MENSAJES.addAll(salidaAnularMovFact.P_OUT_MENSAJES);
				}
			}
			
			if (MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES) == 0) {
				//actualiza estado
				_sql.batch(conn, SQL_UPDATE_MOVIMIENTOS, movimientos, MovimientoComprobante_Proceso.class);
				
				for (MovimientoComprobante_Proceso mov : movimientos) {
					ResultadoProceso_Salida res = new ResultadoProceso_Salida();
					res.Campo = "IdMovimientoComprobante";
					res.Id = mov.IdMovimientoComprobante;
					res.Resultado = "ANU";
					res.Servicio = "AnularMovimientoComprobante.java";
					salida.P_OUT_RESULTADOPROCESO.add(res);
					
					String codigo = "UI_MOVIMIENTO_COMPROBANTE_ANULADO";
					String descripcion = Diccionario.getMensaje(conn, in.p_IdEmpresa, "UI_MOVIMIENTO_COMPROBANTE_ANULADO").add("IdMovimientoComprobante", mov.IdMovimientoComprobante ).msg();
					MensajeSalida m = new MensajeSalida(mov.IdMovimientoComprobante,MensajeSalida.MENSAJE_INFORMACION,codigo,descripcion);
					salida.P_OUT_MENSAJES.add(m);
					
				}
				
				//agregar log
				{
					AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
					for (MovimientoComprobante_Proceso mov : movimientos) {
						MovimientoComprobante infoMov = (MovimientoComprobante)hashMovimientos.get(mov.IdMovimientoComprobante);
						
						AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
						log.IdCabeceraLog = infoMov.IdMovimientoComprobante;
						log.IdTipoLog = "ANULACION";
						log.IdModulo = "FAC";
						log.IdUsuario = in.p_IdUsuario;
						log.IdEmpresa = in.p_IdEmpresa;
						log.FechaSistemaLog = fechaSistema;
						log.IdUsuarioAtendido = in.p_IdUsuario;
						log.IdCliente = infoMov.IdCliente;
						log.EstadoCliente = "";
						log.NombreCliente = infoMov.NombreLargoCliente;
						log.IdTipoDocumentoIdentidad = infoMov.TipoDocumentoIdentidad;
						log.IdDocumentoIdentidad = infoMov.DocumentoIdentidad;
						log.IdPortafolio = infoMov.IdPortafolio;
						log.EstadoPortafolio = "";
						log.IdTipoRegistro = "MovimientoComprobanteFacturacion";
						log.EstadoRegistro = infoMov.IdEstadoFacturacion;
						log.IdTipoOperacion = infoMov.IdTipoOperacion;
						log.Folio = infoMov.IdMovimientoComprobante;
						log.Cantidad = infoMov.Cantidad;
						log.PrecioTasa = infoMov.Precio;
						log.Monto = infoMov.Monto;
						log.IdMonedaOrigen = infoMov.IdMoneda;
						log.IdMonedaLiquidacion = infoMov.IdMonedaLiquidacion;
						log.TipoCambio = infoMov.ValorTipoCambio.toString();
						log.Nemo = infoMov.NemotecnicoInstrumento;
						log.IdProceso = 0L;
						log.EstadoProceso = "";
						log.NombreProceso = "";						
						entradaAgregarLog.p_Log.add(log);
					}
					AgregarLog agregarLogService = new AgregarLog();
					agregarLogService.logica(conn, entradaAgregarLog);
				}
			}
			
			
		}

		salida.P_OUT_ERRORESCONTADOR += MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
		
		logger.info(salida.toString());
		logger.info("Fin");
		
		return salida;
	}
	
	public void limpiarMovimientos(List<MovimientoComprobante_Proceso> movs,List<MovimientoComprobante_Proceso> movsError,List<ResultadoProceso_Salida> ress) {
		
		for (MovimientoComprobante_Proceso mov : movs) {
			if (mov.errores > 0) {
				movsError.add(mov);
				
				ResultadoProceso_Salida res = new ResultadoProceso_Salida();
				res.Campo = "IdMovimientoComprobante";
				res.Id = mov.IdMovimientoComprobante;
				res.Resultado = MensajeSalida.MENSAJE_ERROR;
				res.Servicio = "AnularMovimientoComprobante.java";
				ress.add(res);
				
			}
		}
		
		for (MovimientoComprobante_Proceso movError : movsError) {
			if (movs.contains(movError)) {
				movs.remove(movError);
			}
		}		
	}
	
	public static class AnularMovimientoComprobante_Entrada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_simularOperacion;
		public String p_UsaReglasNegocio;
		public String p_EsIngresoAutomatico = "N";
		public String p_Glosa = "";
		
		public List<Movimiento_Entrada> p_MovimientoAAnular;
		
		public AnularMovimientoComprobante_Entrada() {
			p_MovimientoAAnular = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class Movimiento_Entrada {
		public Long IdMovimientoComprobante;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class AnularMovimientoComprobante_Salida {
		
		public List<MensajeSalida> 				P_OUT_MENSAJES;
		public Long 							P_OUT_ERRORESCONTADOR;
		public List<ResultadoProceso_Salida> 	P_OUT_RESULTADOPROCESO;
		
		public AnularMovimientoComprobante_Salida() {
			P_OUT_ERRORESCONTADOR = 0L;
			P_OUT_RESULTADOPROCESO = new ArrayList<>();
			P_OUT_MENSAJES = new ArrayList<>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ResultadoProceso_Salida {
		public String Campo;
		public Long Id;
		public String Servicio;
		public String Resultado;
	}
	
	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getDESAConnection())  // DESA PERU
		{
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1644,\"p_IdModulo\":\"FAC\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_EsIngresoAutomatico\":\"N\",\"p_Glosa\":\"Anulado por Sistema\",\"p_MovimientoAAnular\":[{\"IdMovimientoComprobante\":422376,\"IdPortafolio\":35409,\"IdCliente\":34848}]}";
			AnularMovimientoComprobante_Entrada Objin = gson.fromJson(json, AnularMovimientoComprobante_Entrada.class);
			AnularMovimientoComprobante demo=new AnularMovimientoComprobante();				
 			try {
 				AnularMovimientoComprobante_Salida ObjOut = demo.logica(con, Objin);
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
 				er.printStackTrace();
				con.rollback();
			}
 			//con.commit();
 			
	    }
		 	
		System.out.println("== END ==");
	}
	
}

	