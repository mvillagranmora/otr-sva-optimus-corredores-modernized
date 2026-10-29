package cl.bcs.ordenes;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.facturacion.AnularComprobanteFinal;
import cl.bcs.facturacion.AnularComprobanteFinal.AnularComprobante_Entrada;
import cl.bcs.facturacion.AnularComprobanteFinal.Comprobante_Entrada;
import cl.bcs.facturacion.CrearCargaMasivaRefacturacion.EntradaCargaMasivaFacturacion;
import cl.bcs.facturacion.CrearComprobante;
import cl.bcs.facturacion.CrearComprobante.CrearComprobante_Entrada;
import cl.bcs.facturacion.CrearComprobante.CrearComprobante_Salida;
import cl.bcs.facturacion.CrearComprobante.MovimientoComprobante_Entrada;
import cl.bcs.facturacion.CrearComprobanteNotaDeCredito;
import cl.bcs.facturacion.CrearComprobanteNotaDeCredito.CrearComprobanteNotaDeCredito_Entrada;
import cl.bcs.facturacion.CrearComprobanteNotaDeCredito.CrearComprobanteNotaDeCredito_Salida;
import cl.bcs.ordenes.AnularAsignaciones.AnularAsignaciones_Entrada;
import cl.bcs.ordenes.AnularAsignaciones.AnularAsignaciones_Salida;
import cl.bcs.ordenes.CrearOrdenesPostreras.CrearOrdenesPostreras_Entrada;
import cl.bcs.ordenes.CrearOrdenesPostreras.CrearOrdenesPostreras_Salida;
import cl.bcs.ordenes.EnviarAsignacionesFacturar.EnviarAsignacionesFacturar_Entrada;
import cl.bcs.ordenes.EnviarAsignacionesFacturar.EnviarAsignacionesFacturar_Salida;
import cl.bcs.ordenes.EnviarAsignacionesFacturar.Resultado;
import cl.bcs.ordenes.ObtenerDetalleRefacturacion.ObtenerDetalleRefacturacion_Entrada;
import cl.bcs.ordenes.ObtenerDetalleRefacturacion.ObtenerDetalleRefacturacion_Salida;
import cl.bcs.ordenes.ObtenerDetalleRefacturacion.Transaccion_Proceso;
import cl.bcs.ordenes.pojo.ConstantesOrdenes;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class RefacturarMovimiento implements JavaCall {
	
	private static final Logger logger = LogManager.getLogger(RefacturarMovimiento.class);
	
	private final String SQL_SELECT_TRANSACCIONES = loadResourceToString("cl/bcs/ordenes/sql/RefacturarMovimiento_SelectTransacciones.sql");
	private final String SQL_UPDATE_ASIGNACIONES = loadResourceToString("cl/bcs/ordenes/sql/RefacturarMovimiento_UpdateAsignacion.sql");
	private final String SQL_UPDATE_ASIGNACIONES_ENV_FAC = loadResourceToString("cl/bcs/ordenes/sql/RefacturarMovimiento_UpdateAsignacionEnviadaFacturar.sql");
	
	
	private final ConstantesOrdenes _c = new ConstantesOrdenes();
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		RefacturarMovimiento_Entrada in=parse(jin.toString());
		
		
		RefacturarMovimiento_Salida out=logica(conn,in);
		
		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	

	
	public static String  crearArchivoLog(List<MensajeSalida> P_OUT_MENSAJE, Long empresa, Connection conn) throws Exception{	   
		String rutaCreacionArchivoLog = "";
		String rutaLecturaArchivoLog = "";
		
		String rutaBaseArchivos = ConfiguracionSistema.getParametroConfiguracionString(conn, empresa, "PLATAFORMA", "PLA.RutaBaseArchivos");
		String rutaFacturacion  = ConfiguracionSistema.getParametroConfiguracionString(conn, empresa, "FACTURACION", "RUTAARCHIVOSFAC");
		
		SimpleDateFormat fm=new SimpleDateFormat("yyyyMMddHHmmss");

		String nombreArchivo = "LOG_REFACTURACION_MASIVA_" + fm.format(new java.util.Date())+".txt";
		rutaCreacionArchivoLog = rutaBaseArchivos + rutaFacturacion + nombreArchivo;
		rutaLecturaArchivoLog  = rutaFacturacion + nombreArchivo;

		File folder = new File(rutaBaseArchivos + rutaFacturacion);
		if(!folder.exists()){
			folder.mkdirs();
		}				 
		 
		 File archivoLog = new File (rutaCreacionArchivoLog);
		 BufferedWriter bw;
		 bw = new BufferedWriter(new FileWriter(archivoLog));
		 
		 bw.write("== INICIO EJECUCION REFACTURACION MASIVA ("+ fm.format(new java.util.Date()) +") == \n");
		 for(MensajeSalida mensaje : P_OUT_MENSAJE)
		 {
			 bw.write(mensaje.tipo + " >>  " + mensaje.descripcion +"\n");
		 }
		 bw.write("== FIN EJECUCION REFACTURACION MASIVA ("+ fm.format(new java.util.Date()) +") == \n");
		 
		 bw.close();
		 
		 return rutaLecturaArchivoLog;
	}
	

	
	// ------------------------------------------------------------------------------
	// LOGICA
	// ------------------------------------------------------------------------------
	@Trace
	public RefacturarMovimiento_Salida logica(Connection conn,RefacturarMovimiento_Entrada entrada) throws Exception {

		Long init = System.currentTimeMillis();
		long startTime = System.nanoTime();		
		logger.info("== Inicio servicio RefacturarMovimiento ==");
		
		RefacturarMovimiento_Salida salida = new RefacturarMovimiento_Salida();
		
		SQL _sql = new SQL();
		
		String genCbmteTesoRefac = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "FACTURACION", "GENERACOMPROBANTETESORERIAREFACTURACION");
		
		String localizacionSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.LocalizacionSistema");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        
		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(conn, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo);
		if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
			salida.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
			
			//Si es ejecucion Batch se genera un log con los mensajes de salida
			if (entrada.p_EsEjecucionBatch.equalsIgnoreCase("S"))
			{
				salida.P_OUT_RUTAARCHIVOLOG = crearArchivoLog(salida.P_OUT_MENSAJES, entrada.p_IdEmpresa, conn);
			}
			salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
			return salida;
		} 
		
		
		if (entrada.p_Asignaciones.size() == 0) {
			String codigo = "NO_SE_ENVIARON_ASIGNACIONES";
			String descripcion = Diccionario.getMensaje(conn,entrada.p_IdEmpresa, codigo).msg();
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,codigo,descripcion));
			//Si es ejecucion Batch se genera un log con los mensajes de salida
			if (entrada.p_EsEjecucionBatch.equalsIgnoreCase("S"))
			{
				salida.P_OUT_RUTAARCHIVOLOG = crearArchivoLog(salida.P_OUT_MENSAJES, entrada.p_IdEmpresa, conn);
			}
			salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
			return salida;
		}
		

		//------------------------------------------------------------------------------------------
		// OBTIENE DATOS DE TRANSACCIONES
		HashMap<String, Object> params = new HashMap<>();
		List<Long> idsTransaccion = new ArrayList<>();
		List<Transaccion_Proceso> infoTransacciones = new ArrayList<Transaccion_Proceso>();
		for (Asignacion_Entrada asig : entrada.p_Asignaciones) {
			if (!idsTransaccion.contains(asig.IdTransaccion))
				idsTransaccion.add(asig.IdTransaccion);				
		}
		
		if (idsTransaccion.size() > 0) {
			params.put("idsTransaccion", idsTransaccion);

			logger.debug("[RefacturarMovimiento] Inicio llamada SQL_SELECT_TRANSACCIONES");
			startTime = System.nanoTime();
			infoTransacciones = _sql.query(conn, SQL_SELECT_TRANSACCIONES, params, Transaccion_Proceso.class);
			logger.debug("[RefacturarMovimiento] (" + (System.nanoTime() - startTime)/1000000 + ") Fin llamada SQL_SELECT_TRANSACCIONES");
			
		}
			
		

		//------------------------------------------------------------------------------------------
		// OBTIENE DATOS DE LA REFACTURACION (Si es que vienen entidades)
		List<ObtenerDetalleRefacturacion.Entidad_Refacturar> entidades = new ArrayList<>();
		for (Entidad_Refacturar ent : entrada.p_Entidades) {
			ObtenerDetalleRefacturacion.Entidad_Refacturar entX = new ObtenerDetalleRefacturacion.Entidad_Refacturar();
			entX.IdEntidad = ent.IdEntidad;
			if (!entidades.contains(entX))
				entidades.add(entX);
		}
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		
		List<cl.bcs.ordenes.ObtenerDetalleRefacturacion.Comprobante_Proceso> detalleComprobantesAnular = new ArrayList<>();
		List<cl.bcs.ordenes.ObtenerDetalleRefacturacion.Asignacion_Proceso> detalleAsignacionesAnular = new ArrayList<>();
		List<cl.bcs.ordenes.ObtenerDetalleRefacturacion.Transaccion_Proceso> detalleTransacciones = new ArrayList<>();
		
		List<cl.bcs.ordenes.ObtenerDetalleRefacturacion.MovimientoComprobante_Proceso> detalleMovimientos = new ArrayList<>();
		
		
		if (entidades.size() > 0) {
			ObtenerDetalleRefacturacion serviceObtenerDetalleRefacturacion = new ObtenerDetalleRefacturacion();
			ObtenerDetalleRefacturacion_Entrada entradaObtenerDetalleRefacturacion = new ObtenerDetalleRefacturacion_Entrada();
			entradaObtenerDetalleRefacturacion.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaObtenerDetalleRefacturacion.p_IdModulo = entrada.p_IdModulo;
			entradaObtenerDetalleRefacturacion.p_IdUsuario = entrada.p_IdUsuario;
			entradaObtenerDetalleRefacturacion.p_Entidades = entidades;
			entradaObtenerDetalleRefacturacion.p_Origen = entrada.p_Origen;
		
			logger.debug("[RefacturarMovimiento] Inicio llamada servicio ObtenerDetalleRefacturacion");
			startTime = System.nanoTime();
			ObtenerDetalleRefacturacion_Salida salidaObtenerDetalleRefacturacion = serviceObtenerDetalleRefacturacion.logica(conn, entradaObtenerDetalleRefacturacion);
			logger.debug("[RefacturarMovimiento] (" + (System.nanoTime() - startTime)/1000000 + ") Fin llamada servicio ObtenerDetalleRefacturacion");
			
			
			detalleComprobantesAnular = salidaObtenerDetalleRefacturacion.P_OUT_COMPROBANTESFACTURACION;
			detalleAsignacionesAnular = salidaObtenerDetalleRefacturacion.P_OUT_ASIGNACIONES;
			detalleTransacciones = salidaObtenerDetalleRefacturacion.P_OUT_TRANSACCIONES;
			
			detalleMovimientos = salidaObtenerDetalleRefacturacion.P_OUT_MOVIMIENTOSFACTURAR;
			
			
			salida.P_OUT_MENSAJES.addAll(salidaObtenerDetalleRefacturacion.P_OUT_MENSAJES);
			if (MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES) > 0) {				

				//Si es ejecucion Batch se genera un log con los mensajes de salida
				if (entrada.p_EsEjecucionBatch.equalsIgnoreCase("S"))
				{
					salida.P_OUT_RUTAARCHIVOLOG = crearArchivoLog(salida.P_OUT_MENSAJES, entrada.p_IdEmpresa, conn);
				}
				salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
				return salida;
			}
		}
		
		
		//------------------------------------------------------------------------------------------
		// VALIDACIONES
		{			
			//Valida transaccion no este reservada por otro usuario
			for (Transaccion_Proceso tran :infoTransacciones) {				
				if (tran.Reservado.equalsIgnoreCase("S") && tran.IdUsuarioReserva.compareTo(entrada.p_IdUsuario) != 0)	
				{
					HashMap<String, String> value = new HashMap<String, String>();
					value.put("FolioTransaccion", tran.FolioTransaccion);
					value.put("UsuarioReserva", tran.CodigoUsuarioReserva);
					salida.P_OUT_MENSAJES.add(new MensajeSalida(tran.IdTransaccion,MensajeSalida.MENSAJE_ERROR,"TRANSACCION_RESERVADA_POR_OTRO_USAURIO", value ,conn,entrada.p_IdEmpresa));	
				}
			}
		
		}
		
		if (MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES) > 0)
		{					
			//Si es ejecucion Batch se genera un log con los mensajes de salida
			if (entrada.p_EsEjecucionBatch.equalsIgnoreCase("S"))
			{
				salida.P_OUT_RUTAARCHIVOLOG = crearArchivoLog(salida.P_OUT_MENSAJES, entrada.p_IdEmpresa, conn);
			}
			salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
			return salida;
		}
		
		
		//------------------------------------------------------------------------------------------
		// GENERA NOTAS DE CREDITO (Para los comprobantes que fueron enviados al DTE)
		
		List<cl.bcs.facturacion.CrearComprobanteNotaDeCredito.Comprobante_Entrada> comprobantesDte = new ArrayList<>();
		for (cl.bcs.ordenes.ObtenerDetalleRefacturacion.Comprobante_Proceso comprobante : detalleComprobantesAnular) {
			cl.bcs.facturacion.CrearComprobanteNotaDeCredito.Comprobante_Entrada comprobanteDte = new cl.bcs.facturacion.CrearComprobanteNotaDeCredito.Comprobante_Entrada();
			comprobanteDte.IdCliente = comprobante.IdCliente;
			comprobanteDte.IdComprobante = comprobante.IdComprobante;
			comprobanteDte.IdPortafolio = comprobante.IdPortafolio;
			comprobanteDte.IdTipoComprobante = comprobante.IdTipoComprobante;
			
			if (comprobante.IdEstadoComprobante.equalsIgnoreCase("ENV") && !comprobantesDte.contains(comprobanteDte)) {
				
				comprobantesDte.add(comprobanteDte);
			}
		}
		
		if (comprobantesDte.size() > 0) {
			
			if (localizacionSistema.equalsIgnoreCase("CL")){
				CrearComprobanteNotaDeCredito serviceCrearComprobanteNotaDeCredito = new CrearComprobanteNotaDeCredito();
				CrearComprobanteNotaDeCredito_Entrada entradaCrearComprobanteNotaDeCredito = new CrearComprobanteNotaDeCredito_Entrada();
				entradaCrearComprobanteNotaDeCredito.P_COMPROBANTE = comprobantesDte;
				entradaCrearComprobanteNotaDeCredito.p_CrearComprobanteAutomatico = "S";
				entradaCrearComprobanteNotaDeCredito.p_EsIngresoAutomatico = "N";
				entradaCrearComprobanteNotaDeCredito.p_IdEmpresa = entrada.p_IdEmpresa;
				entradaCrearComprobanteNotaDeCredito.p_IdModulo = entrada.p_IdModulo;
				entradaCrearComprobanteNotaDeCredito.p_IdUsuario = entrada.p_IdUsuario;
				entradaCrearComprobanteNotaDeCredito.p_simularOperacion = "N";
				entradaCrearComprobanteNotaDeCredito.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
			
				logger.debug("[RefacturarMovimiento] Inicio llamada servicio CrearComprobanteNotaDeCredito");
				startTime = System.nanoTime();
				CrearComprobanteNotaDeCredito_Salida salidaCrearComprobanteNotaDeCredito = serviceCrearComprobanteNotaDeCredito.logica(conn,entradaCrearComprobanteNotaDeCredito);
				logger.debug("[RefacturarMovimiento] (" + (System.nanoTime() - startTime)/1000000 + ") Fin llamada servicio CrearComprobanteNotaDeCredito");
				
				
				salida.P_OUT_MENSAJES.addAll(salidaCrearComprobanteNotaDeCredito.P_OUT_MENSAJES);
				if (MensajeSalida.getNumeroErrores(salidaCrearComprobanteNotaDeCredito.P_OUT_MENSAJES) > 0) {
					
					//Si es ejecucion Batch se genera un log con los mensajes de salida
					if (entrada.p_EsEjecucionBatch.equalsIgnoreCase("S"))
					{
						salida.P_OUT_RUTAARCHIVOLOG = crearArchivoLog(salida.P_OUT_MENSAJES, entrada.p_IdEmpresa, conn);
					}
					salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
					return salida;
				}
			}
		}
		
		
		//------------------------------------------------------------------------------------------
		// ANULAR COMPROBANTE (Para los comprobantes que no han sido enviados al DTE)	
		List<Comprobante_Entrada> comprobantesAnulacion = new ArrayList<>();
		for (cl.bcs.ordenes.ObtenerDetalleRefacturacion.Comprobante_Proceso comprobante : detalleComprobantesAnular) {
			Comprobante_Entrada comprobanteAnular = new Comprobante_Entrada();
			comprobanteAnular.IdComprobante = comprobante.IdComprobante;
			
			if (((comprobante.IdEstadoComprobante.equalsIgnoreCase("FAC") && localizacionSistema.equalsIgnoreCase("CL")) || 
				 ((comprobante.IdEstadoComprobante.equalsIgnoreCase("ENV") || comprobante.IdEstadoComprobante.equalsIgnoreCase("FAC")) && localizacionSistema.equalsIgnoreCase("PE"))) && !comprobantesAnulacion.contains(comprobanteAnular)) {
				
				comprobantesAnulacion.add(comprobanteAnular);
			}
		}
		
		if (comprobantesAnulacion.size() > 0) {
			AnularComprobante_Entrada entradaAnularComprobante = new AnularComprobante_Entrada();
			entradaAnularComprobante.P_COMPROBANTEAANULAR = comprobantesAnulacion;
			entradaAnularComprobante.p_EjecutaAutorizacion = "N";
			entradaAnularComprobante.p_EsIngresoAutomatico = "S";
			entradaAnularComprobante.p_Glosa = "ANULACION POR REFACTURACION";
			entradaAnularComprobante.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaAnularComprobante.p_IdModulo = entrada.p_IdModulo;
			entradaAnularComprobante.p_IdUsuario = entrada.p_IdUsuario;
			entradaAnularComprobante.p_OmiteRegistroSinAutorizar = "N";
			entradaAnularComprobante.p_simularOperacion = "N";
			entradaAnularComprobante.p_UsaReglasNegocio = "S";
			
			AnularComprobanteFinal serviceAnularComprobante = new AnularComprobanteFinal();

			logger.debug("[RefacturarMovimiento] Inicio llamada servicio AnularComprobanteFinal");
			startTime = System.nanoTime();
			cl.bcs.facturacion.AnularComprobanteFinal.AnularComprobante_Salida salidaAnularComprobante = serviceAnularComprobante.logica(conn, entradaAnularComprobante);
			logger.debug("[RefacturarMovimiento] (" + (System.nanoTime() - startTime)/1000000 + ") Fin llamada servicio AnularComprobanteFinal");
			
			
			salida.P_OUT_MENSAJES.addAll(salidaAnularComprobante.P_OUT_MENSAJES);
			if (MensajeSalida.getNumeroErrores(salidaAnularComprobante.P_OUT_MENSAJES) > 0) {
				
				//Si es ejecucion Batch se genera un log con los mensajes de salida
				if (entrada.p_EsEjecucionBatch.equalsIgnoreCase("S"))
				{
					salida.P_OUT_RUTAARCHIVOLOG = crearArchivoLog(salida.P_OUT_MENSAJES, entrada.p_IdEmpresa, conn);
				}
				salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
				return salida;
			}
		}
		
		
		//------------------------------------------------------------------------------------------
		// ANULAR ASIGNACIONES
		
		List<cl.bcs.ordenes.AnularAsignaciones.Asignacion_Entrada> asignacionesAnular = new ArrayList<>();
		List<cl.bcs.ordenes.AnularAsignaciones.Asignacion_Entrada> asignacionesActualizar = new ArrayList<>();
		
		cl.bcs.ordenes.AnularAsignaciones.Asignacion_Entrada asignacionAnular;
		cl.bcs.ordenes.AnularAsignaciones.Asignacion_Entrada asignacionActualizar;
		
		for (cl.bcs.ordenes.ObtenerDetalleRefacturacion.Asignacion_Proceso asignacion : detalleAsignacionesAnular) {
			asignacionAnular = new cl.bcs.ordenes.AnularAsignaciones.Asignacion_Entrada();
			asignacionActualizar = new cl.bcs.ordenes.AnularAsignaciones.Asignacion_Entrada();
			
			asignacionAnular.IdAsignacion = asignacion.IdAsignacion;
			asignacionActualizar.IdAsignacion = asignacion.IdAsignacion;
			
				if (!asignacionesAnular.contains(asignacionAnular)) {
					if (asignacion.IdEstadoComprobante.equalsIgnoreCase("FAC")) {
						//anulo solo si no esta enviado al DTE					
						asignacionesAnular.add(asignacionAnular);
					}
					else { //actualiza estado de asignaciones a NEF
						asignacionesActualizar.add(asignacionActualizar);
					}
				}
			
		}
		
 		if (asignacionesAnular.size() > 0) {
			AnularAsignaciones_Entrada entradaAnularAsignaciones = new AnularAsignaciones_Entrada();
			entradaAnularAsignaciones.asignacionesParaAnular = asignacionesAnular ;
			entradaAnularAsignaciones.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaAnularAsignaciones.p_IdModulo = entrada.p_IdModulo;
			entradaAnularAsignaciones.p_IdUsuario = entrada.p_IdUsuario;
			
			AnularAsignaciones serviceAnularAsignaciones = new AnularAsignaciones();
			
			logger.debug("[RefacturarMovimiento] Inicio llamada servicio AnularAsignaciones");
			startTime = System.nanoTime();
			AnularAsignaciones_Salida salidaAnularAsignaciones = serviceAnularAsignaciones.logica(conn, entradaAnularAsignaciones);
			logger.debug("[RefacturarMovimiento] (" + (System.nanoTime() - startTime)/1000000 + ") Fin llamada servicio AnularAsignaciones");
			
			
			
			if (MensajeSalida.getNumeroErrores(salidaAnularAsignaciones.p_out_Mensajes) > 0) {
				salida.P_OUT_MENSAJES.addAll(salidaAnularAsignaciones.p_out_Mensajes);	
				
				//Si es ejecucion Batch se genera un log con los mensajes de salida
				if (entrada.p_EsEjecucionBatch.equalsIgnoreCase("S"))
				{
					salida.P_OUT_RUTAARCHIVOLOG = crearArchivoLog(salida.P_OUT_MENSAJES, entrada.p_IdEmpresa, conn);
				}
				salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
				return salida;
			}
		}
 		if (asignacionesActualizar.size() > 0) {
			logger.debug("[RefacturarMovimiento] Inicio llamada SQL_UPDATE_ASIGNACIONES");
			startTime = System.nanoTime();
 			_sql.batch(conn, SQL_UPDATE_ASIGNACIONES, asignacionesActualizar, cl.bcs.ordenes.AnularAsignaciones.Asignacion_Entrada.class);
			logger.debug("[RefacturarMovimiento] (" + (System.nanoTime() - startTime)/1000000 + ") Fin llamada SQL_UPDATE_ASIGNACIONES");
 		}
		

 		
		for (Transaccion_Proceso tran :infoTransacciones) {				
			if (!detalleTransacciones.contains(tran.IdTransaccion))
			{
				detalleTransacciones.add(tran);		
			}
		}
		
		
		
		//------------------------------------------------------------------------------------------
		// NUEVAS ASIGNACIONES (y crea orden postrera)
		
		CrearOrdenesPostreras_Entrada entradaCrearOrdenesPostreras = new CrearOrdenesPostreras_Entrada();
		if(entrada.p_CobrosFijos.size()>0){
			for (Cobro_Fijo cobro : entrada.p_CobrosFijos) {
				cl.bcs.ordenes.CrearOrdenesPostreras.Cobro_Fijo cobroFijo = new cl.bcs.ordenes.CrearOrdenesPostreras.Cobro_Fijo();
				
				cobroFijo.IdInterno = cobro.IdInterno; 
				cobroFijo.IdInternoCobro = cobro.IdInternoCobro;
				cobroFijo.IdPortafolio = cobro.IdPortafolio;
				cobroFijo.IdBolsa = cobro.IdBolsa;
				cobroFijo.IdTipoProducto = cobro.IdTipoProducto;
				cobroFijo.IdTipoOperacion = cobro.IdTipoOperacion;
				cobroFijo.IdTipoCanalIngreso = cobro.IdTipoCanalIngreso;
				cobroFijo.IdCobro = cobro.IdCobro;
				cobroFijo.ValorCobro = cobro.ValorCobro;
				cobroFijo.FormaCobro = cobro.FormaCobro;
				cobroFijo.ValorTopeMinimoOrden = cobro.ValorTopeMinimoOrden;
				cobroFijo.IdMoneda = cobro.IdMoneda; 
				cobroFijo.CodigoCobroClasificacion = cobro.CodigoCobroClasificacion;
				cobroFijo.PermiteCambiarValor = cobro.PermiteCambiarValor;

				entradaCrearOrdenesPostreras.p_CobrosFijos.add(cobroFijo );
			}
		}else{
			for (CobroModificado_Entrada cobro : entrada.p_CobrosModificados) {
				cl.bcs.ordenes.CrearOrdenesPostreras.CobroModificados_Entrada cobroMod = new cl.bcs.ordenes.CrearOrdenesPostreras.CobroModificados_Entrada();
				
				cobroMod.IdInterno = cobro.IdInterno;
				cobroMod.IdCobro = cobro.IdCobro;
				cobroMod.ValorCobroAplicado = cobro.ValorCobroAplicado;				
				
				entradaCrearOrdenesPostreras.p_CobrosModificados.add(cobroMod );
			}
		}
		
		
		for (Asignacion_Entrada asig : entrada.p_Asignaciones) {
			cl.bcs.ordenes.CrearOrdenesPostreras.OrdenPostrera_Entrada orden = new cl.bcs.ordenes.CrearOrdenesPostreras.OrdenPostrera_Entrada();
			
			Transaccion_Proceso tran = null;
			for (Transaccion_Proceso tranx : detalleTransacciones) {
				if (asig.IdTransaccion.equals(tranx.IdTransaccion))
					tran = tranx;
			}
			
			if (tran == null) {
				String codigo = "TRANSACCION_NO_EXISTE";
				String descripcion = Diccionario.getMensaje(conn,entrada.p_IdEmpresa, codigo).msg();
				salida.P_OUT_MENSAJES.add(new MensajeSalida(asig.IdInterno,MensajeSalida.MENSAJE_ERROR,codigo,descripcion));
				
				//Si es ejecucion Batch se genera un log con los mensajes de salida
				if (entrada.p_EsEjecucionBatch.equalsIgnoreCase("S"))
				{
					salida.P_OUT_RUTAARCHIVOLOG = crearArchivoLog(salida.P_OUT_MENSAJES, entrada.p_IdEmpresa, conn);
				}
				salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
				return salida;
			}
			
			if (tran.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_IF)) {
				orden.Cantidad = BigDecimal.ZERO;
				orden.ValorRescate = asig.Cantidad;
			}
			else {
				orden.Cantidad = asig.Cantidad;
				orden.ValorRescate = BigDecimal.ZERO;
			}
			orden.EsFacturaInmediata = "N";
			
			orden.FechaRecepcion = fechaSistema;
			
			orden.FolioCartera = 0L;
			orden.Glosa = "REFACTURACION";
			orden.IdBoveda = asig.IdBoveda;
			orden.IdCuentaCustodioPortafolio = asig.IdCuentaCustodioPortafolio;
			orden.IdFormaCustodia = asig.IdFormaCustodia;
			orden.IdInterno = asig.IdInterno;
			orden.IdMonedaLiquidacionOrden = asig.IdMonedaLiquidacionOrden;
			orden.IdPortafolio = asig.IdPortafolio;
			orden.IdPreferenciaPagoPortafolio = asig.IdPreferenciaPagoPortafolio;
			orden.IdTransaccion = asig.IdTransaccion;
			orden.PagoCobroCuentaInversion = asig.PagoCobroCuentaInversion;
			orden.PlazoVentaCorta = asig.PlazoVentaCorta;
			orden.TasaVentaCorta = asig.TasaVentaCorta;
			orden.TipoCambio = asig.TipoCambio;
			
			entradaCrearOrdenesPostreras.p_Ordenes.add(orden );
		}
		
		entradaCrearOrdenesPostreras.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCrearOrdenesPostreras.p_IdModulo = entrada.p_IdModulo;
		entradaCrearOrdenesPostreras.p_IdUsuario = entrada.p_IdUsuario;
		entradaCrearOrdenesPostreras.p_OrigenPost = entrada.p_Tipo;
		entradaCrearOrdenesPostreras.p_simularOperacion = "N";
		entradaCrearOrdenesPostreras.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
		
		CrearOrdenesPostreras serviceCrearOrdenesPostreras = new CrearOrdenesPostreras();
		
		logger.debug("[RefacturarMovimiento] Inicio llamada servicio CrearOrdenesPostreras");
		startTime = System.nanoTime();
		CrearOrdenesPostreras_Salida salidaCrearOrdenesPostreras = serviceCrearOrdenesPostreras.logica(conn, entradaCrearOrdenesPostreras);
		logger.debug("[RefacturarMovimiento] (" + (System.nanoTime() - startTime)/1000000 + ") Fin llamada  servicio CrearOrdenesPostreras");
		salida.P_OUT_MENSAJES.addAll(salidaCrearOrdenesPostreras.P_OUT_MENSAJES);
		
		if (MensajeSalida.getNumeroErrores(salidaCrearOrdenesPostreras.P_OUT_MENSAJES) > 0) {		
			//Si es ejecucion Batch se genera un log con los mensajes de salida
			if (entrada.p_EsEjecucionBatch.equalsIgnoreCase("S"))
			{
				salida.P_OUT_RUTAARCHIVOLOG = crearArchivoLog(salida.P_OUT_MENSAJES, entrada.p_IdEmpresa, conn);
			}
			salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
			return salida;
		}
		
		
		
		if (entrada.p_Origen == null || entrada.p_Origen.equalsIgnoreCase("COMP")){
			
			
			//------------------------------------------------------------------------------------------
			// ENVIAR ASIGNACIONES A FACTURAR
			EnviarAsignacionesFacturar_Entrada entradaAsignacionesFacturar = new EnviarAsignacionesFacturar_Entrada();
			
			for (cl.bcs.ordenes.CrearOrdenesPostreras.Resultado_Salida asig : salidaCrearOrdenesPostreras.P_OUT_RESULTADO) {
				cl.bcs.ordenes.EnviarAsignacionesFacturar.Asignacion_Entrada asigX = new cl.bcs.ordenes.EnviarAsignacionesFacturar.Asignacion_Entrada();
				asigX.GeneraComprobante = "N";
				asigX.IdAsignacion = asig.IdAsignacion;
				
				entradaAsignacionesFacturar.p_Asignaciones.add(asigX);
			}
			
			entradaAsignacionesFacturar.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaAsignacionesFacturar.p_IdModulo = entrada.p_IdModulo;
			entradaAsignacionesFacturar.p_IdUsuario = entrada.p_IdUsuario;
			entradaAsignacionesFacturar.p_simularOperacion = "N";
			EnviarAsignacionesFacturar serviceEnviarAsignacionesFacturar = new EnviarAsignacionesFacturar();
			
			
			logger.debug("[RefacturarMovimiento] Inicio llamada servicio EnviarAsignacionesFacturar");
			startTime = System.nanoTime();
			EnviarAsignacionesFacturar_Salida salidaAsignacionesFacturar = serviceEnviarAsignacionesFacturar.logica(conn, entradaAsignacionesFacturar);
			logger.debug("[RefacturarMovimiento] (" + (System.nanoTime() - startTime)/1000000 + ") Fin llamada  servicio EnviarAsignacionesFacturar");
			
			
			if (MensajeSalida.getNumeroErrores(salidaAsignacionesFacturar.P_OUT_MENSAJES) > 0) {
				salida.P_OUT_MENSAJES.addAll(salidaAsignacionesFacturar.P_OUT_MENSAJES);
				
				//Si es ejecucion Batch se genera un log con los mensajes de salida
				if (entrada.p_EsEjecucionBatch.equalsIgnoreCase("S"))
				{
					salida.P_OUT_RUTAARCHIVOLOG = crearArchivoLog(salida.P_OUT_MENSAJES, entrada.p_IdEmpresa, conn);
				}
				salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
				return salida;
			}
		
			
			//------------------------------------------------------------------------------------------
			// CREAR NUEVOS COMPROBANTES DE FACTURACION
			
			Integer fecAsig = 0;
			
			List<MovimientoComprobante_Entrada> movimientosFacturarCrearComprobante = new ArrayList<>();
			for (Resultado movto : salidaAsignacionesFacturar.P_OUT_RESULTADO) {
				
				
				MovimientoComprobante_Entrada movX = new MovimientoComprobante_Entrada();
				//movX.IdCliente = movto.IdCliente;
				//movX.IdPortafolio = movto.IdPortafolio;
				
				
				if (localizacionSistema.equalsIgnoreCase("PE")) {				
				  for (cl.bcs.ordenes.ObtenerDetalleRefacturacion.Transaccion_Proceso tran : detalleTransacciones)
					 {
					   if(tran.IdTransaccion.compareTo(movto.IdTransaccion)==0)
						  {
						   
						   
							movX.FechaComprobante = dateFormat.format(tran.FechaOperacionTransaccion);;
							fecAsig = 1;						
						  }
					 }
				  
				    if (fecAsig.equals(0) ){
				    	movX.FechaComprobante = fechaSistema;
				    }
				} else { 
					      movX.FechaComprobante = fechaSistema;
					   }
				
				movX.IdMovimientoComprobante = movto.IdMovimientoComprobante;
			
				movimientosFacturarCrearComprobante.add(movX);
			}
			
			
			CrearComprobante_Entrada entradaCrearComprobante = new CrearComprobante_Entrada();
			entradaCrearComprobante.p_EsIngresoAutomatico = "S";
			entradaCrearComprobante.p_Glosa = "REFACTURACION";
			entradaCrearComprobante.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaCrearComprobante.p_IdModulo = entrada.p_IdModulo;
			entradaCrearComprobante.p_IdUsuario = entrada.p_IdUsuario;	
			entradaCrearComprobante.P_MOVIMIENTOFACTURAR = movimientosFacturarCrearComprobante;
			entradaCrearComprobante.p_simularOperacion = "N";
			entradaCrearComprobante.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
			
		
			entradaCrearComprobante.p_GeneraComprobanteTesoreria = genCbmteTesoRefac;
			
			
			CrearComprobante serviceCrearComprobante = new CrearComprobante();
			
			logger.debug("[RefacturarMovimiento] Inicio llamada servicio CrearComprobante");
			startTime = System.nanoTime();
			CrearComprobante_Salida salidaCrearComprobante = serviceCrearComprobante.logica(conn, entradaCrearComprobante);
			logger.debug("[RefacturarMovimiento] (" + (System.nanoTime() - startTime)/1000000 + ") Fin llamada  servicio CrearComprobante");
			
			
			salida.P_OUT_MENSAJES.addAll(salidaCrearComprobante.P_OUT_MENSAJES);
			if (MensajeSalida.getNumeroErrores(salidaCrearComprobante.P_OUT_MENSAJES) > 0) {
				
				//Si es ejecucion Batch se genera un log con los mensajes de salida
				if (entrada.p_EsEjecucionBatch.equalsIgnoreCase("S"))
				{
					salida.P_OUT_RUTAARCHIVOLOG = crearArchivoLog(salida.P_OUT_MENSAJES, entrada.p_IdEmpresa, conn);
				}
				salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
				return salida;
			}
			else {
				List<MensajeSalida> ListMensajeSalida = new ArrayList<MensajeSalida>();
				
				for(MensajeSalida msn: salida.P_OUT_MENSAJES) {
					if(msn.codigo.equals("UI_COMPROBANTE_CREADO") || msn.tipo.equals("ADVERTENCIA")) {
						ListMensajeSalida.add(msn);
					}
				}
				
				salida.P_OUT_MENSAJES.clear();
				salida.P_OUT_MENSAJES.addAll(ListMensajeSalida);
				salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
			}
			
			//Si todo OK, volvemos a setear las asignaciones a enviadas a facturar
			if (asignacionesActualizar.size() > 0) {
				logger.debug("[RefacturarMovimiento] Inicio llamada SQL_UPDATE_ASIGNACIONES_ENV_FAC");
				startTime = System.nanoTime();
	 			_sql.batch(conn, SQL_UPDATE_ASIGNACIONES_ENV_FAC, asignacionesActualizar, cl.bcs.ordenes.AnularAsignaciones.Asignacion_Entrada.class);
				logger.debug("[RefacturarMovimiento] (" + (System.nanoTime() - startTime)/1000000 + ") Fin llamada SQL_UPDATE_ASIGNACIONES_ENV_FAC");
	 		}
		}
		salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
		
		//Si es ejecucion Batch se genera un log con los mensajes de salida
		if (entrada.p_EsEjecucionBatch.equalsIgnoreCase("S"))
		{
			salida.P_OUT_RUTAARCHIVOLOG = crearArchivoLog(salida.P_OUT_MENSAJES, entrada.p_IdEmpresa, conn);
		}
		logger.info("== Termino del servicio RefacturarMovimiento, tiempo: " + (System.currentTimeMillis() - init) + " ms - " + (System.currentTimeMillis() - init)/1000 + " seg ==");
		return salida;
	}
	
	// ------------------------------------------------------------------------------
	// CLASES DE ENTRADA
	// ------------------------------------------------------------------------------
	public static class RefacturarMovimiento_Entrada {
		public Long						p_IdEmpresa;
		public Long						p_IdUsuario;
		public String 					p_IdModulo;
		public String					p_UsaReglasNegocio = "S";
		
		public String 					p_Origen; //ORIGEN DEL PROCESO, INDICA SI LO QUE VIENE PARA PROCESAR SON TRANSACCIONES, ASIGNACIONES, MOVIMIENTOS O COMPROBANTES, POR AHORA SIEMPRE COMP
		public String					p_Tipo = "REFACTURACION";	//REFACTURACION,REASIGNACION, no se deberia usar
		
		public String					p_EsEjecucionBatch = "N";
		
		public List<Entidad_Refacturar>	p_Entidades;
		public List<Asignacion_Entrada>	p_Asignaciones;
		public List<CobroModificado_Entrada>	p_CobrosModificados;
		public List<Cobro_Fijo>  p_CobrosFijos;
		
		public RefacturarMovimiento_Entrada() {
			this.p_Entidades = new ArrayList<>();
			this.p_Asignaciones = new ArrayList<>();
			this.p_CobrosModificados = new ArrayList<>();
			this.p_CobrosFijos = new ArrayList<>();
		}
				
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public static class Entidad_Refacturar extends cl.bcs.ordenes.ObtenerDetalleRefacturacion.Entidad_Refacturar{
	}
	
	public static class Asignacion_Entrada {
		public BigDecimal 	Cantidad;
		public String 		IdBoveda;
		public Long 		IdCuentaCustodioPortafolio;
		public String 		IdFormaCustodia;
		public Long 		IdInterno;
		public String 		IdMonedaLiquidacionOrden;
		public Long 		IdPortafolio;
		public Long 		IdPreferenciaPagoPortafolio;
		public Long 		IdTransaccion;
		public String 		PagoCobroCuentaInversion;
		public Long 		PlazoVentaCorta;
		public BigDecimal 	TasaVentaCorta;
		public BigDecimal 	TipoCambio;
		public Long         IdAsignacion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class Cobro_Fijo {
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
		public String ValorCobroAplicado;
	
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class CobroModificado_Entrada {
		public Long IdInterno;
		public String	IdCobro;
		public BigDecimal ValorCobroAplicado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	// ------------------------------------------------------------------------------
	// CLASES DE SALIDA
	// ------------------------------------------------------------------------------
	public static class RefacturarMovimiento_Salida {

		public Long 				P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida>	P_OUT_MENSAJES;
		
		public String 				P_OUT_RUTAARCHIVOLOG;  // Cuando es Ejecucion Batch se genera log con mensajes de salida
		
		public RefacturarMovimiento_Salida() {
			this.P_OUT_MENSAJES = new ArrayList<>();
			this.P_OUT_RUTAARCHIVOLOG = "";
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	// ------------------------------------------------------------------------------
	// METODO PARA PRUEBAS LOCALES
	// ------------------------------------------------------------------------------
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa2", "connections.properties")) {
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdModulo\":\"FAC\",\"p_Origen\":\"COMP\",\"p_Tipo\":\"REFACTURACION\",\"p_Asignaciones\":[{\"Cantidad\":100,\"IdBoveda\":\"DCV\",\"IdCuentaCustodioPortafolio\":null,\"IdFormaCustodia\":\"CUS\",\"IdInterno\":13978,\"IdMonedaLiquidacionOrden\":\"CLP\",\"IdPortafolio\":13974,\"IdPreferenciaPagoPortafolio\":null,\"IdTransaccion\":1037112,\"PagoCobroCuentaInversion\":\"S\",\"PlazoVentaCorta\":0,\"TasaVentaCorta\":0,\"TipoCambio\":0}],\"p_Entidades\":[{\"IdEntidad\":3235121}],\"p_CobrosModificados\":[{\"IdInterno\":13978,\"IdCobro\":\"COVA\",\"ValorCobroAplicado\":\"0.4\"},{\"IdInterno\":13978,\"IdCobro\":\"DEEX\",\"ValorCobroAplicado\":\"0\"},{\"IdInterno\":13978,\"IdCobro\":\"GFOD\",\"ValorCobroAplicado\":\"5000\"},{\"IdInterno\":13978,\"IdCobro\":\"IVA\",\"ValorCobroAplicado\":\"19\"}],\"p_CobrosFijos\":[]}";
			
			RefacturarMovimiento_Entrada Objin = parse(json);
			
			RefacturarMovimiento demo=new RefacturarMovimiento();				
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			RefacturarMovimiento_Salida ObjOut=demo.logica(con, Objin);
 			System.out.println(ObjOut.toString());
 			con.rollback();
		}
		 	
		System.out.println("== END ==");
	}
	
		public static RefacturarMovimiento_Entrada parse(String targetStream) throws JSONException{
        ObjectMapper obje= new ObjectMapper();
        RefacturarMovimiento_Entrada map = new RefacturarMovimiento_Entrada();
		try {
			map = obje.readValue(targetStream, RefacturarMovimiento_Entrada.class);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       
        
	
		return map;
	
	
}
}
	