package cl.bcs.ordenes;

import cl.bcs.ordenes.pojo.Validacion;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class AnularInstruccionMasivo implements JavaCall {
	static final Logger logger = LogManager.getLogger(AnularInstruccionMasivo.class);

	private final String SQL_UPDATE_ESTADO_INSTRUCCION = loadResourceToString("cl/bcs/ordenes/sql/AnularInstruccionMasivo_UpdateEstadoInstruccion.sql");

	public static class EntradaAnularInstruccionMasivo extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_IdTipoCanalIngreso;

		public String p_MotivoAnulacion;
		public String p_ComentarioAnulacion;
		public List<Long> p_IdsInstrucciones;

		public EntradaAnularInstruccionMasivo() {
			p_MotivoAnulacion = "";
			p_ComentarioAnulacion = "";
			p_IdsInstrucciones = new ArrayList<>();
		}
	}

	public static class SalidaAnularInstruccionMasivo extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<PlataformaUtils.MensajeSalida> P_OUT_MENSAJES;

		public SalidaAnularInstruccionMasivo() {
			P_OUT_ERRORESCONTADOR = 0L;
			P_OUT_MENSAJES = new ArrayList<>();
		}

		private void agregarValidacion(Validacion validacion) {
			if (validacion.EsError) {
				P_OUT_ERRORESCONTADOR++;
			}
			P_OUT_MENSAJES.add(validacion.Mensaje);
		}

		public void agregarValidaciones(List<Validacion> validaciones) {
			for (Validacion validacion : validaciones) {
				agregarValidacion(validacion);
			}
		}
	}

	public static class InstruccionProceso extends BcsPojo {
		public Long IdInstruccion;
		public Long IdCliente;

		public Long IdPortafolioCliente;
		public String IdTipoOperacion;
		public String Nemotecnico;
		public String IdMonedaInstrumento;
		public String IdMonedaLiquidacion;		
		public BigDecimal Cantidad;
		public BigDecimal TirTasa;
		public BigDecimal MontoNeto;		
		public java.sql.Date FechaModificacion;
		public String IdTipoCanalIngreso;
		public Long IdMotivoPreAnulacion;
		public Long IdMotivoPostAnulacion;
		public String IdEstadoPreAnulacion;
		public String IdEstadoPostAnulacion;
		public Date	FechaIngreso;
	}

	public static class ResultadoObtenerInstrucciones extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<PlataformaUtils.MensajeSalida> P_OUT_MENSAJES;
		public List<InstruccionProceso> p_out_Instrucciones;

		public ResultadoObtenerInstrucciones() {
			P_OUT_ERRORESCONTADOR = 0L;
			P_OUT_MENSAJES = new ArrayList<>();
			p_out_Instrucciones = new ArrayList<>();
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaAnularInstruccionMasivo entrada = gson.fromJson(in, EntradaAnularInstruccionMasivo.class);
		SalidaAnularInstruccionMasivo salida = logica(conn, entrada);
		JsonParser jsonParser = new JsonParser();

		return jsonParser.parse(gson.toJson(salida)).getAsJsonObject();
	}
	
	private void guardarInstruccionesHistoricas(Connection conn, EntradaAnularInstruccionMasivo entrada
											  , List<InstruccionProceso> instrucciones) throws Exception {
		CrearInstruccionHistorica.EntradaCrearInstruccionHistorica entradaInstruccionHistorica = new CrearInstruccionHistorica.EntradaCrearInstruccionHistorica();
		entradaInstruccionHistorica.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaInstruccionHistorica.p_IdModulo = entrada.p_IdModulo;
		entradaInstruccionHistorica.p_IdTipoCanalIngreso = entrada.p_IdTipoCanalIngreso;
		entradaInstruccionHistorica.p_IdUsuario = entrada.p_IdUsuario;
		entradaInstruccionHistorica.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
		entradaInstruccionHistorica.p_InstruccionesHistoricas = new ArrayList<>();
		
		CrearInstruccionHistorica.EntradaInstruccionHistorica registro;
		for (InstruccionProceso instruccion : instrucciones) {
			registro = new CrearInstruccionHistorica.EntradaInstruccionHistorica();
			registro.p_IdInstruccion = instruccion.IdInstruccion;
			registro.p_IdEstadoInstruccion = instruccion.IdEstadoPreAnulacion;
			registro.p_IdMotivo = instruccion.IdMotivoPreAnulacion;
			entradaInstruccionHistorica.p_InstruccionesHistoricas.add(registro);
		}
		
		CrearInstruccionHistorica servicioInstruccionHistorica = new CrearInstruccionHistorica();
		CrearInstruccionHistorica.SalidaCrearInstruccionHistorica salidaInstruccionHistorica = new CrearInstruccionHistorica.SalidaCrearInstruccionHistorica();
		
		salidaInstruccionHistorica = servicioInstruccionHistorica.logica(conn, entradaInstruccionHistorica);
		
		if (!Long.valueOf(0L).equals(salidaInstruccionHistorica.P_OUT_ERRORESCONTADOR)) {
			throw new Exception("Error al guardar historica");
		}
	}

	private SalidaAnularInstruccionMasivo logica(Connection conn, EntradaAnularInstruccionMasivo entrada) throws Exception {
		logger.info("INICIO::AnularInstruccionMasivo");
		long inicio = System.currentTimeMillis();

		SalidaAnularInstruccionMasivo salida = new SalidaAnularInstruccionMasivo();

		{
			String codigoProductoInstrucciones = "PRO.Instrucciones";
			boolean noEstaActivado = !"ACTIVO".equals(ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "PRODUCTO", codigoProductoInstrucciones));
			if (noEstaActivado) {
				HashMap<String, String> params = new HashMap<>(2);
				params.put("CodigoParametro", codigoProductoInstrucciones);

				salida.P_OUT_ERRORESCONTADOR++;
				salida.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ERROR, "UI_PRODUCTO_NO_ACTIVO", params, conn, entrada.p_IdEmpresa));

				return salida;
			}
		}

		java.sql.Timestamp fechaHoraSistema = FechaDB.ObtenerFechaHoraSistemaTs(conn, entrada.p_IdEmpresa);

		List<Long> idsInstrucciones = entrada.p_IdsInstrucciones.stream().distinct().collect(Collectors.toList());
		ResultadoObtenerInstrucciones resultadoObtenerInstrucciones = obtenerInstrucciones(conn, entrada, idsInstrucciones, fechaHoraSistema);
		salida.P_OUT_ERRORESCONTADOR += resultadoObtenerInstrucciones.P_OUT_ERRORESCONTADOR;
		salida.P_OUT_MENSAJES.addAll(resultadoObtenerInstrucciones.P_OUT_MENSAJES);
		if (salida.P_OUT_ERRORESCONTADOR > 0) {
			return salida;
		}

		List<Validacion> validacionesInstrucciones = validarInstrucciones(conn, entrada, resultadoObtenerInstrucciones.p_out_Instrucciones);
		salida.agregarValidaciones(validacionesInstrucciones);
		if (salida.P_OUT_ERRORESCONTADOR > 0) {
			return salida;
		}

		anularInstrucciones(conn, resultadoObtenerInstrucciones.p_out_Instrucciones);
		agregarBitacoras(conn, entrada, resultadoObtenerInstrucciones.p_out_Instrucciones);
		agregarLogs(conn, entrada, resultadoObtenerInstrucciones.p_out_Instrucciones, fechaHoraSistema);		
		guardarInstruccionesHistoricas(conn, entrada, resultadoObtenerInstrucciones.p_out_Instrucciones );

		long fin = System.currentTimeMillis();
		logger.info("FIN::AnularInstruccionMasivo {}", fin - inicio);

		return salida;
	}

	private ResultadoObtenerInstrucciones obtenerInstrucciones(Connection conn, EntradaAnularInstruccionMasivo entrada, List<Long> idsInstrucciones, java.sql.Timestamp fechaHoraSistema) throws Exception {
		logger.debug("INICIO::AnularInstruccionMasivo.obtenerInstrucciones");
		long inicio = System.currentTimeMillis();

		ConsultarInstruccionMasivo.EntradaConsultarInstruccionMasivo entradaConsultarInstruccionMasivo = new ConsultarInstruccionMasivo.EntradaConsultarInstruccionMasivo();
		entradaConsultarInstruccionMasivo.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaConsultarInstruccionMasivo.p_IdUsuario = entrada.p_IdUsuario;
		entradaConsultarInstruccionMasivo.p_IdModulo = entrada.p_IdModulo;
		entradaConsultarInstruccionMasivo.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
		entradaConsultarInstruccionMasivo.p_IdTipoCanalIngreso = entrada.p_IdTipoCanalIngreso;
		entradaConsultarInstruccionMasivo.p_IdsInstrucciones = idsInstrucciones;

		ConsultarInstruccionMasivo consultarInstruccionMasivo = new ConsultarInstruccionMasivo();
		ConsultarInstruccionMasivo.SalidaConsultarInstruccionMasivo salidaConsultarInstruccionMasivo = consultarInstruccionMasivo.logica(conn, entradaConsultarInstruccionMasivo);

		java.sql.Date fechaSistemaDate = Fecha.toDate(Fecha.toStringDate(fechaHoraSistema));
		ResultadoObtenerInstrucciones resultadoObtenerInstrucciones = new ResultadoObtenerInstrucciones();
		resultadoObtenerInstrucciones.P_OUT_ERRORESCONTADOR += salidaConsultarInstruccionMasivo.P_OUT_ERRORESCONTADOR;
		resultadoObtenerInstrucciones.P_OUT_MENSAJES.addAll(salidaConsultarInstruccionMasivo.P_OUT_MENSAJES);
		resultadoObtenerInstrucciones.p_out_Instrucciones = salidaConsultarInstruccionMasivo.p_out_Instrucciones.stream().map(ins -> {
			InstruccionProceso instruccionProceso = new InstruccionProceso();
			instruccionProceso.IdInstruccion = ins.IdInstruccion;
			instruccionProceso.IdEstadoPreAnulacion = ins.IdEstadoInstruccion; 
			instruccionProceso.IdEstadoPostAnulacion = "AGE".equals(entrada.p_IdTipoCanalIngreso)? "CAN" : "SCN";
			instruccionProceso.FechaModificacion = fechaSistemaDate;
			instruccionProceso.FechaIngreso = ins.FechaIngreso;
			instruccionProceso.IdTipoCanalIngreso = ins.IdTipoCanalIngreso;
			
			instruccionProceso.IdMotivoPreAnulacion = ins.IdMotivo;
			instruccionProceso.IdMotivoPostAnulacion = !(entrada.p_MotivoAnulacion == null || ("").equals(entrada.p_MotivoAnulacion)) ? 
										   Long.parseLong(entrada.p_MotivoAnulacion) : null;

			return instruccionProceso;
		}).collect(Collectors.toList());

		if (resultadoObtenerInstrucciones.p_out_Instrucciones.isEmpty()) {
			resultadoObtenerInstrucciones.P_OUT_ERRORESCONTADOR++;
			resultadoObtenerInstrucciones.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ERROR, "UI_NO_HAY_INSTRUCCIONES_POR_RECHAZAR", new HashMap<>(), conn, entrada.p_IdEmpresa));
		}

		long fin = System.currentTimeMillis();
		logger.debug("FIN::AnularInstruccionMasivo.obtenerInstrucciones {}", fin - inicio);

		return resultadoObtenerInstrucciones;
	}
	
	private List<Validacion> validarInstrucciones(Connection conn, EntradaAnularInstruccionMasivo entrada, List<InstruccionProceso> instrucciones){
		logger.trace("INICIO::AnularInstruccionMasivo.validarInstrucciones");
		long inicio = System.currentTimeMillis();
		
		List<Validacion> validaciones = new ArrayList<>();
		validaciones.addAll(validarEstados(conn, entrada, instrucciones));
		validaciones.addAll(validarFechaIngreso(conn, entrada, instrucciones));
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::AnularInstruccionMasivo.validarInstrucciones", fin - inicio);
		return validaciones;
	}

	private List<Validacion> validarEstados(Connection conn, EntradaAnularInstruccionMasivo entrada, List<InstruccionProceso> instrucciones){
		logger.debug("INICIO::AnularInstruccionMasivo.validarInstrucciones");
		long inicio = System.currentTimeMillis();
		
		List<Validacion> validaciones = instrucciones
				.stream()
				.filter(instruccion->
					!("ING".equals(instruccion.IdEstadoPreAnulacion) || "RZD".equals(instruccion.IdEstadoPreAnulacion)
					|| "MOD".equals(instruccion.IdEstadoPreAnulacion) || "NLQ".equals(instruccion.IdEstadoPreAnulacion))
				 )
				.map(instruccion->{
					Validacion validacion = new Validacion();
					HashMap<String, String> parametros = new HashMap<String, String>(2);
					parametros.put("TipoInstruccion", instruccion.IdInstruccion.toString());
					try {
						validacion.Mensaje = new MensajeSalida(instruccion.IdInstruccion, MensajeSalida.MENSAJE_ERROR, "UI_SOLO_PERMITIDO_CANCELAR_ESTADOS", parametros, conn, entrada.p_IdEmpresa);
					} catch(Exception error) {
						validacion.Mensaje = new MensajeSalida(instruccion.IdInstruccion, MensajeSalida.MENSAJE_ERROR, "UI_MENSAJE_NO_EXISTE", "UI_SOLO_PERMITIDO_CANCELAR_ESTADOS sin traduccion.");
					}
					
					validacion.EsError = true;
					return validacion;
				})
				.collect(Collectors.toList());

		long fin = System.currentTimeMillis();
		logger.debug("FIN::AnularInstruccionMasivo.validarInstrucciones {}", fin - inicio);

		return validaciones;
	}
	
	private List<Validacion> validarFechaIngreso(Connection conn, EntradaAnularInstruccionMasivo entrada, List<InstruccionProceso> instrucciones){
		logger.trace("INICIO::AnularInstruccionMasivo.validarFechaIngreso");
		long inicio = System.currentTimeMillis();
		
		List<Validacion> validaciones = instrucciones
				.stream()
				.filter(instruccion->
					"AGE".equals(entrada.p_IdTipoCanalIngreso) && "CLI".equals(instruccion.IdTipoCanalIngreso) && !diasTranscurridos(conn, instruccion.FechaIngreso, entrada.p_IdEmpresa )
				 )
				.map(instruccion->{
					Validacion validacion = new Validacion();
					HashMap<String, String> parametros = new HashMap<String, String>(2);
					parametros.put("TipoInstruccion", instruccion.IdInstruccion.toString());
					try {
						validacion.Mensaje = new MensajeSalida(instruccion.IdInstruccion, MensajeSalida.MENSAJE_ERROR, "UI_DIAS_TRANSCURRIDOS_INSUFICIENTE_CANCELAR_INSTRUCCION", parametros, conn, entrada.p_IdEmpresa);
					} catch(Exception error) {
						validacion.Mensaje = new MensajeSalida(instruccion.IdInstruccion, MensajeSalida.MENSAJE_ERROR, "UI_MENSAJE_NO_EXISTE", "UI_DIAS_TRANSCURRIDOS_INSUFICIENTE_CANCELAR_INSTRUCCION sin traduccion.");
					}
					
					validacion.EsError = true;
					return validacion;
				})
				.collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::AnularInstruccionMasivo.validarFechaIngreso", fin - inicio);
		return validaciones;
	}
	
	private boolean diasTranscurridos (Connection conn, Date FechaIngreso, Long idEmpresa){
		logger.debug("INICIO::AnularInstruccionMasivo.anularInstrucciones");
		long inicio = System.currentTimeMillis();
		boolean dias = false;
		
		java.sql.Timestamp fechaHoraSistema;
		try {
			fechaHoraSistema = FechaDB.ObtenerFechaHoraSistemaTs(conn, idEmpresa);
			 
		    long diffInMillies = Math.abs(fechaHoraSistema.getTime() - FechaIngreso.getTime());
		    long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
		    
		    if (diff > 15) {
		    	dias = true;
		    }
			
		} catch (Exception e) {
			e.printStackTrace();
		}
				
		long fin = System.currentTimeMillis();
		logger.debug("FIN::AnularInstruccionMasivo.anularInstrucciones {}", fin - inicio);
		return dias;
	}

	private void anularInstrucciones(Connection conn, List<InstruccionProceso> instrucciones) throws SQLException {
		logger.debug("INICIO::AnularInstruccionMasivo.anularInstrucciones");
		long inicio = System.currentTimeMillis();

		SQL sql = new SQL();
		sql.batch(conn, SQL_UPDATE_ESTADO_INSTRUCCION, instrucciones, InstruccionProceso.class);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::AnularInstruccionMasivo.anularInstrucciones {}", fin - inicio);
	}

	private void agregarBitacoras(Connection conn, EntradaAnularInstruccionMasivo entrada, List<InstruccionProceso> instrucciones) throws Exception {
		logger.debug("INICIO::AnularInstruccionMasivo.agregarBitacoras");
		long inicio = System.currentTimeMillis();

		AgregarEventoBitacoraMasivo.EntradaAgregarEventoBitacoraMasivo entradaAgregarEventoBitacoraMasivo = new AgregarEventoBitacoraMasivo.EntradaAgregarEventoBitacoraMasivo();
		entradaAgregarEventoBitacoraMasivo.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaAgregarEventoBitacoraMasivo.p_IdUsuario = entrada.p_IdUsuario;
		entradaAgregarEventoBitacoraMasivo.p_IdModulo = entrada.p_IdModulo;
		entradaAgregarEventoBitacoraMasivo.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
		entradaAgregarEventoBitacoraMasivo.p_IdTipoCanalIngreso = entrada.p_IdTipoCanalIngreso;
		entradaAgregarEventoBitacoraMasivo.p_eventos = instrucciones.stream().map(instruccion -> {
			AgregarEventoBitacoraMasivo.EventoBitacora eventoBitacora = new AgregarEventoBitacoraMasivo.EventoBitacora();
			eventoBitacora.IdInterno = instruccion.IdInstruccion;
			eventoBitacora.NombreEntidad = "INSTRUCCION";
			eventoBitacora.IdEntidad = instruccion.IdInstruccion;
			eventoBitacora.IdOrden = 0L;
			eventoBitacora.IdAsignacion = 0L;
			eventoBitacora.IdTransaccion = 0L;
			eventoBitacora.MensajeEventoBitacora = entrada.p_ComentarioAnulacion;
			eventoBitacora.IdTipoEventoBitacora = "INSTRUCCION_SOLICITUD_CANCELACION";

			return eventoBitacora;
		}).collect(Collectors.toList());

		AgregarEventoBitacoraMasivo agregarEventoBitacoraMasivo = new AgregarEventoBitacoraMasivo();
		agregarEventoBitacoraMasivo.logica(conn, entradaAgregarEventoBitacoraMasivo);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::AnularInstruccionMasivo.agregarBitacoras {}", fin - inicio);
	}

	private void agregarLogs(Connection conn, EntradaAnularInstruccionMasivo entrada, List<InstruccionProceso> instrucciones, java.sql.Timestamp fechaHoraSistema) throws Exception {
		logger.debug("INICIO::AnularInstruccionMasivo.agregarLogs");
		long inicio = System.currentTimeMillis();

		long idTrabajo = Double.valueOf(Math.random() * 10000000000L).longValue();
		String fechaSistema = Fecha.toStringDate(fechaHoraSistema);

		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
		entradaAgregarLog.p_IdTrabajo = idTrabajo;

		for (InstruccionProceso instruccion : instrucciones) {
			AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
			log.IdCabeceraLog = instruccion.IdInstruccion;
			log.IdTipoLog = "RECHAZAR";
			log.IdModulo = "ORD";
			log.IdUsuario = entrada.p_IdUsuario;
			log.IdEmpresa = entrada.p_IdEmpresa;
			log.FechaSistemaLog = fechaSistema;
			log.IdUsuarioAtendido = entrada.p_IdUsuario;
			log.IdCliente = instruccion.IdCliente;
			log.EstadoCliente = "";
			log.NombreCliente = "";
			log.IdTipoDocumentoIdentidad = "";
			log.IdDocumentoIdentidad = "";
			log.IdPortafolio = instruccion.IdPortafolioCliente;
			log.IdTipoRegistro = "SOLICITUD DE CANCELACION INSTRUCCION";
			log.IdTipoOperacion = instruccion.IdTipoOperacion;
			log.Folio = instruccion.IdInstruccion;
			log.Cantidad = instruccion.Cantidad;
			log.PrecioTasa = instruccion.TirTasa;
			log.Monto = instruccion.MontoNeto;
			log.Nemo = instruccion.Nemotecnico;
			log.IdProceso = 0L;
			log.EstadoProceso = "";
			log.NombreProceso = "";
			log.IdMonedaOrigen = instruccion.IdMonedaInstrumento;
			log.IdMonedaLiquidacion = instruccion.IdMonedaLiquidacion;
			log.TipoCambio = "";
			log.IdPersona = 0L;

			entradaAgregarLog.p_Log.add(log);

			AgregarLog.InAgregarLog.DetalleLog estadoInstruccionDetalleLog = new AgregarLog.InAgregarLog.DetalleLog();
			estadoInstruccionDetalleLog.IdTrabajo = idTrabajo;
			estadoInstruccionDetalleLog.IdCabeceraLog = instruccion.IdInstruccion;
			estadoInstruccionDetalleLog.SubRegistro = "Instruccion";
			estadoInstruccionDetalleLog.CampoRegistro = "IdEstadoInstruccion";
			estadoInstruccionDetalleLog.ValorAntiguo = instruccion.IdEstadoPreAnulacion;
			estadoInstruccionDetalleLog.ValorNuevo = instruccion.IdEstadoPostAnulacion;

			entradaAgregarLog.p_LogDetalle.add(estadoInstruccionDetalleLog);
		}

		AgregarLog agregarLog = new AgregarLog();
		agregarLog.logica(conn, entradaAgregarLog);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::AnularInstruccionMasivo.agregarLogs {}", fin - inicio);
	}

	public static void main(String[] args) {
		System.out.println("== INICIO TEST ==");

		String jsonRequest = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"ORD\",\"p_UsaReglasNegocio\":\"N\",\"p_IdTipoCanalIngreso\":\"AGE\",\"p_ComentarioAnulacion\":\"prueba solicitud cancelacion\",\"p_MotivoAnulacion\":\"\",\"p_IdsInstrucciones\":[188]}";
		try (Connection conn = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();
			EntradaAnularInstruccionMasivo entrada = gson.fromJson(jsonRequest, EntradaAnularInstruccionMasivo.class);
			System.out.println("ENTRADA");
			System.out.println(entrada);

			try {
				AnularInstruccionMasivo anularInstruccionMasivo = new AnularInstruccionMasivo();
				SalidaAnularInstruccionMasivo salida = anularInstruccionMasivo.logica(conn, entrada);
				System.out.println("SALIDA");
				System.out.println(salida);
				if (salida.P_OUT_ERRORESCONTADOR > 0) {
					conn.rollback();
				}

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				conn.rollback();
			}

		} catch (SQLException throwables) {
			throwables.printStackTrace();
		}

		System.out.println("== FIN TEST ==");
	}
}
