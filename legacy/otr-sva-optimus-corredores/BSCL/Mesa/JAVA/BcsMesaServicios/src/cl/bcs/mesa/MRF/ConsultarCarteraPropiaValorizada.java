package cl.bcs.mesa.MRF;

import java.math.BigDecimal;


import java.sql.Connection;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.utils.ValorizarInforme;
import cl.bcs.custodia.utils.ValorizarInforme.ObjResultadoValorizacion;
import cl.bcs.custodia.utils.ValorizarInforme.ValorizarInformeEntrada;
import cl.bcs.custodia.utils.ValorizarInforme.ValorizarInformeSalida;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.OutAgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog.Log;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;

public class ConsultarCarteraPropiaValorizada implements JavaCall {

	static final Logger logger = LogManager.getLogger(ConsultarCarteraPropiaValorizada.class);

	private final String SQL_SELECT_CLIENTE_SOCIONEGOCIO = loadResourceToString("cl/bcs/mesa/MRF/sql/ConsultarCarteraPropiaValorizada_SelectClienteSocioNegocio.sql");
	private final String SQL_SELECT_CLIENTE_MESA = loadResourceToString("cl/bcs/mesa/MRF/sql/ConsultarCarteraPropiaValorizada_SelectClienteMesa.sql");
	
	private final String SQL_SELECT_CARTERA_LIBRE = loadResourceToString("cl/bcs/mesa/MRF/sql/ConsultarCarteraPropiaValorizada_SelectCarteraLibre.sql");
	private final String SQL_SELECT_CARTERA_GARANTIA = loadResourceToString("cl/bcs/mesa/MRF/sql/ConsultarCarteraPropiaValorizada_SelectCarteraGarantia.sql");
	
	private final String SQL_SELECT_CARTERA_LIBRE_HISTORICO = loadResourceToString("cl/bcs/mesa/MRF/sql/ConsultarCarteraPropiaValorizada_SelectCarteraLibreHistorico.sql");
	private final String SQL_SELECT_CARTERA_GARANTIA_HISTORICO = loadResourceToString("cl/bcs/mesa/MRF/sql/ConsultarCarteraPropiaValorizada_SelectCarteraGarantiaHistorico.sql");

	private final String SQL_SELECT_CARTERA_VALORIZADA = loadResourceToString("cl/bcs/mesa/MRF/sql/ConsultarCarteraPropiaValorizada_SelectCarteraValorizada.sql");
	
	private final String SQL_SELECT_PORTAFOLIOS_CUENTA_PROPIA = loadResourceToString("cl/bcs/mesa/MRF/sql/ConsultarCarteraPropiaValorizada_SelectPortafoliosCuentaPropia.sql");

	private final String SQL_SELECT_FECHA_ULTIMO_RESPALDO = loadResourceToString("cl/bcs/mesa/MRF/sql/ConsultarCarteraPropiaValorizada_SelectFechaUltimoRespaldo.sql");
	
	public static class Cliente {
		public Long IdCliente;
		public Long IdPortafolio;

		public Cliente() {
			this.IdCliente = 0L;
			this.IdPortafolio = 0L;
		}
	}

	public static class EntradaConsultarCarteraPropia extends BcsPojo {
		public String p_FechaConsulta;
		public String p_FechaValorizacion;
		public Long p_IdCliente;
		public Long p_IdEmpresa;
		public String p_IdModulo;
		public Long p_IdPortafolio;
		public Long p_IdSocioNegocio;
		public Long p_IdUsuario;
		public String p_TipoReporte;
		public String p_TipoValorizacion;
		public Boolean p_EjecutaValorizacion;
	}

	public static class SalidaCarteras extends BcsPojo {
		public String TablaAfecta;
		public Long IdValorizacion;
		public Long IdCartera;
		public Long FolioCartera;
		public String Nemotecnico;
		public String CodigoIFRS;
		public String IdEmisor;
		public String EsFungible;
		public String FechaVencimientoInstrumento;
		public String FechaVencimientoOperacion;
		public String FechaOperacion;
		public BigDecimal Cantidad;
		public BigDecimal MontoCompra;
		public String IdMonedaInstrumento;
		public BigDecimal TasaActual;
		public String Diferenciador;
		public Long IdCliente;
		public Long IdPortafolio;
		public String IdClasificacionTipoInstrumento;
		public String IdTipoInstrumento;
		public Long IdInstrumento;
		public Long IdOperacion;
		public String FechaEmisionInstrumento;
		public String IdTipoReajusteInstrumento;
		public String IdMonedaEmisionInstrumento;
		public String IdCondicionLiquidacion;
		public String IdTipoOperacion;
		public String NemoSVS;
		public BigDecimal MontoCompraMonedaSistema;
		public BigDecimal ValorRescate;
		public BigDecimal PrecioCompra;
		public String IdBolsa;
		public Long IdMesa;
		public String FechaLiquidacion;
		public String IdBoveda;
		public String IdTipoProducto;
		public String FechaUltimoRespaldo;
	}

	public static class SalidaConsultarCarteraPropia extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public List<ObjResultadoValorizacion> P_OUT_INSTRUMENTOSVALORIZADOS;
		public String IdInforme;

		public SalidaConsultarCarteraPropia() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<>();
			this.P_OUT_INSTRUMENTOSVALORIZADOS = new ArrayList<>();
			this.IdInforme = "";
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = getGsonBuilder();
		EntradaConsultarCarteraPropia jin = gson.fromJson(in.toString(), EntradaConsultarCarteraPropia.class);
		SalidaConsultarCarteraPropia out = logica(conn, jin);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public SalidaConsultarCarteraPropia logica(Connection con, EntradaConsultarCarteraPropia entrada) throws Exception {
		logger.info("INICIO::ConsultarCarteraPropia");
		SalidaConsultarCarteraPropia salida = new SalidaConsultarCarteraPropia();
		SQL sql = new SQL();
		InformacionEmpresa empresa = new ConsultarEmpresa().logica(con, entrada.p_IdEmpresa);
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		HashMap<String, Object> params = new HashMap<String, Object>(5);
		List<Cliente> cliente = new ArrayList<>();
		Long idCliente = 0L;

		if (entrada.p_FechaConsulta == null || entrada.p_FechaConsulta.isEmpty()) {
			salida.P_OUT_ERRORESCONTADOR++;
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR , "FECHA_INVALIDA", Diccionario.getMensaje(con,entrada.p_IdEmpresa, "FECHA_INVALIDA").msg()));
			return salida;
		} else {
			entrada.p_FechaConsulta = entrada.p_FechaConsulta.substring(0, 10);
		}

		if (entrada.p_FechaValorizacion == null || entrada.p_FechaValorizacion.isEmpty()) {
			salida.P_OUT_ERRORESCONTADOR++;
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR , "FECHA_INVALIDA", Diccionario.getMensaje(con,entrada.p_IdEmpresa, "FECHA_INVALIDA").msg()));
			return salida;
		} else {
			entrada.p_FechaValorizacion = entrada.p_FechaValorizacion.substring(0, 10);
		}

		if (Fecha.toDate(entrada.p_FechaValorizacion).before(Fecha.toDate(entrada.p_FechaConsulta))) {
			salida.P_OUT_ERRORESCONTADOR++;
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR ,"FECHA_VALORIZACION_INVALIDA", Diccionario.getMensaje(con,entrada.p_IdEmpresa, "FECHA_VALORIZACION_INVALIDA").msg()));
			return salida;
		}

		if (entrada.p_IdCliente.equals(0L) && entrada.p_IdSocioNegocio.equals(0L) && entrada.p_IdEmpresa.equals(0L)) {
			salida.P_OUT_ERRORESCONTADOR++;
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR ,"IDS_NULOS", Diccionario.getMensaje(con,entrada.p_IdEmpresa, "IDS_NULOS").msg()));
			return salida;
		}

		if (entrada.p_IdCliente.equals(0L) && entrada.p_IdSocioNegocio.equals(0L) && !entrada.p_IdEmpresa.equals(0L)) {
			params.put("IdEmpresa", entrada.p_IdEmpresa);
			cliente = sql.query(con, SQL_SELECT_CLIENTE_MESA, params, Cliente.class);
			idCliente = cliente.stream().findFirst().orElse(new Cliente()).IdCliente;
		} else if (entrada.p_IdCliente.equals(0L) && !entrada.p_IdSocioNegocio.equals(0L)) {
			params.put("IdSocioNegocio", entrada.p_IdSocioNegocio);
			cliente = sql.query(con, SQL_SELECT_CLIENTE_SOCIONEGOCIO, params, Cliente.class);
			idCliente = cliente.stream().findFirst().orElse(new Cliente()).IdCliente;
		} else if (!entrada.p_IdCliente.equals(0L)) {
			idCliente = entrada.p_IdCliente;
		}

		if (!idCliente.equals(empresa.IdCliente)) {
			salida.P_OUT_ERRORESCONTADOR++;
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR , "ERROR_CUENTA_PROPIA", Diccionario.getMensaje(con,entrada.p_IdEmpresa, "ERROR_CUENTA_PROPIA").msg()));
			return salida;
		}

		Long existeValorizacion = 0L;
		if (!entrada.p_EjecutaValorizacion) {
			params.clear();
			String partialKey = "CARTERA.PROPIA" + entrada.p_TipoValorizacion + entrada.p_FechaConsulta.substring(0, 10) + entrada.p_FechaValorizacion.substring(0, 10);
			params.put("p_Key", partialKey);
			existeValorizacion = sql.countQuery(con, SQL_SELECT_CARTERA_VALORIZADA, params);
			params.clear();
		}

		if (!entrada.p_EjecutaValorizacion && existeValorizacion.equals(0L)) {
			salida.P_OUT_ERRORESCONTADOR++;
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, "ERROR", "", "No existe Valorización para Fecha Seleccionada"));
			return salida;
		} else if (!entrada.p_EjecutaValorizacion && !existeValorizacion.equals(0L)) {
			salida.P_OUT_ERRORESCONTADOR++;
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, "INFORMACION", "", "Proceso Valorización Finalizado"));
			return salida;
		}

		List<SalidaCarteras> salidaCartera = new ArrayList<>();
		String fechaDB = FechaDB.ObtenerFechaHoraSistemaLocal(con, empresa.IdEmpresa);

		params.clear();
		params.put("p_Fecha", entrada.p_FechaConsulta);
		params.put("p_IdCliente", idCliente);
		params.put("p_IdPortafolio", entrada.p_IdPortafolio);

		if (Fecha.toDate(entrada.p_FechaConsulta).before(Fecha.toDate(fechaSistema))) {
			HashMap<String, Object> paramRespaldo = new HashMap<>(1);
			paramRespaldo.put("p_Fecha", entrada.p_FechaConsulta);
			String fechaRespaldo = sql.query(con, SQL_SELECT_FECHA_ULTIMO_RESPALDO, paramRespaldo, SalidaCarteras.class).stream().findFirst().get().FechaUltimoRespaldo;
			params.put("p_Fecha", fechaRespaldo);
			salidaCartera.addAll(sql.query(con, SQL_SELECT_CARTERA_LIBRE_HISTORICO, params, SalidaCarteras.class));
			salidaCartera.addAll(sql.query(con, SQL_SELECT_CARTERA_GARANTIA_HISTORICO, params, SalidaCarteras.class));
		} else {
			salidaCartera.addAll(sql.query(con, SQL_SELECT_CARTERA_LIBRE, params, SalidaCarteras.class));
			salidaCartera.addAll(sql.query(con, SQL_SELECT_CARTERA_GARANTIA, params, SalidaCarteras.class));
		}

		if (salidaCartera.isEmpty()) {
			logger.debug("No hay operaciones para valorizar.");				
			salida.P_OUT_ERRORESCONTADOR++;
			String codigoSinOperaciones = "SIN_OPERACIONES_PARA_VALORIZAR";
			MensajeSalida errorValorizacion = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, codigoSinOperaciones, Diccionario.getMensaje(con,entrada.p_IdEmpresa, codigoSinOperaciones).msg());
			salida.P_OUT_MENSAJES.add(errorValorizacion);
			return salida;
		}

		ValorizarInformeEntrada valorizarInformeEntrada = new ValorizarInformeEntrada();
		
		if(entrada.p_IdPortafolio.equals(0L)) {
			params.clear();
			params.put("IdCliente", idCliente);
			List<Cliente> portafoliosCuentaPropia =  sql.query(con, SQL_SELECT_PORTAFOLIOS_CUENTA_PROPIA, params, Cliente.class);
			valorizarInformeEntrada.p_IdsPortafolio.addAll(portafoliosCuentaPropia.stream().map(data -> data.IdPortafolio).distinct().collect(Collectors.toList()));
		} else {
			valorizarInformeEntrada.p_IdsPortafolio.add(entrada.p_IdPortafolio);
		}

		valorizarInformeEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
		valorizarInformeEntrada.p_IdUsuario = entrada.p_IdUsuario;
		valorizarInformeEntrada.p_IdModulo = entrada.p_IdModulo;
		valorizarInformeEntrada.p_IdPais = empresa.IdPais;
		valorizarInformeEntrada.p_IdMonedaAdicional = "USD";
		valorizarInformeEntrada.p_FechaInicial = entrada.p_FechaConsulta;
		valorizarInformeEntrada.p_FechaFinal = entrada.p_FechaValorizacion;
		valorizarInformeEntrada.p_OrigenProcesamiento = "CARTERA.PROPIA";
		valorizarInformeEntrada.p_IdTipoTasaValorizacion = entrada.p_TipoValorizacion;
		valorizarInformeEntrada.p_IdFechaHoraSistema = fechaDB;
		valorizarInformeEntrada.p_IdClienteSolicitante = idCliente;
		
		InAgregarLog agregarLogEntrada = new InAgregarLog();
		agregarLogEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
		agregarLogEntrada.p_IdUsuario = entrada.p_IdUsuario;
		agregarLogEntrada.p_IdModulo = entrada.p_IdModulo;
		agregarLogEntrada.p_UsaReglasNegocio = "Si";
		agregarLogEntrada.p_IdEstadoAutorizacion = "S";
		agregarLogEntrada.p_IdTrabajo = 0L;
		AgregarLog agregarLogService = new AgregarLog();
		ValorizarInformeSalida salidaValorizar = new ValorizarInformeSalida();
		int contadorLoop = 0;
		
		for (SalidaCarteras out : salidaCartera) {
			ValorizarInforme.ObjEntradaInstrumentos entradaInstrumento = new ValorizarInforme.ObjEntradaInstrumentos();
			entradaInstrumento.TablaAfecta = out.TablaAfecta;
			entradaInstrumento.IdValorizacion = out.IdValorizacion;
			entradaInstrumento.TablaOrigen = out.TablaAfecta;
			entradaInstrumento.IdCartera = out.IdCartera;
			entradaInstrumento.FolioCartera = out.FolioCartera;
			entradaInstrumento.IdCliente = out.IdCliente;
			entradaInstrumento.IdPortafolio = out.IdPortafolio;
			entradaInstrumento.IdClasificacionTipoInstrumento = out.IdClasificacionTipoInstrumento;
			entradaInstrumento.IdTipoInstrumento = out.IdTipoInstrumento;
			entradaInstrumento.IdInstrumento = out.IdInstrumento;
			entradaInstrumento.FechaVencimientoInstrumento = out.FechaVencimientoInstrumento;
			entradaInstrumento.FechaVencimientoOperacion = out.FechaVencimientoOperacion;
			entradaInstrumento.IdOperacion = out.IdOperacion;
			entradaInstrumento.FechaEmision = out.FechaEmisionInstrumento;
			entradaInstrumento.FechaOperacion = out.FechaOperacion;
			entradaInstrumento.IdMonedaInstrumento = out.IdMonedaInstrumento;
			entradaInstrumento.IdTipoReajusteInstrumento = out.IdTipoReajusteInstrumento;
			entradaInstrumento.IdMonedaEmisionInstrumento = out.IdMonedaEmisionInstrumento;
			entradaInstrumento.IdCondicionLiquidacion = out.IdCondicionLiquidacion;
			entradaInstrumento.IdTipoOperacion = out.IdTipoOperacion;
			entradaInstrumento.Emisor = out.IdEmisor;
			entradaInstrumento.EsFungible = out.EsFungible;
			entradaInstrumento.CodigoIFRS = out.CodigoIFRS;
			entradaInstrumento.Nemotecnico = out.Nemotecnico;
			entradaInstrumento.NemoSVS = out.NemoSVS;
			entradaInstrumento.Diferenciador = out.Diferenciador;
			entradaInstrumento.Cantidad = out.Cantidad;
			entradaInstrumento.MontoCompra = out.MontoCompra;
			entradaInstrumento.MontoCompraMonedaSistema = out.MontoCompraMonedaSistema;
			entradaInstrumento.ValorRescate = out.ValorRescate;
			entradaInstrumento.PrecioCompra = out.PrecioCompra;
			entradaInstrumento.TasaCompra = out.TasaActual;
			entradaInstrumento.TasaCompraValorizacionFechaOperacion = out.TasaActual;
			entradaInstrumento.IdBolsa = out.IdBolsa;
			entradaInstrumento.IdMesa = out.IdMesa;
			entradaInstrumento.OrigenProcesamiento = "CARTERA.PROPIA";
			entradaInstrumento.FechaLiquidacion = out.FechaLiquidacion;
			entradaInstrumento.IdBoveda = out.IdBoveda;
			entradaInstrumento.IdTipoProducto = out.IdTipoProducto;
			
			valorizarInformeEntrada.p_Instrumentos.add(entradaInstrumento);
			
			if ((contadorLoop != 0 && contadorLoop % 5000 == 0) || contadorLoop == salidaCartera.size() - 1) {
					logger.info("Valorizando hasta " + valorizarInformeEntrada.p_Instrumentos.size());
					ValorizarInforme ValorizarPapeles = new ValorizarInforme();
					ValorizarInformeSalida Salida = new ValorizarInformeSalida();
					try {
						salidaValorizar = ValorizarPapeles.logica(con, valorizarInformeEntrada);
					} catch(Exception err) {
						logger.error(err);
						err.printStackTrace();
						Salida.P_OUT_ERRORESCONTADOR++;
					} finally {
						salida.P_OUT_ERRORESCONTADOR += salidaValorizar.P_OUT_ERRORESCONTADOR;
						salida.P_OUT_INSTRUMENTOSVALORIZADOS.addAll(salidaValorizar.P_OUT_INSTRUMENTOSVALORIZADOS);
						salida.P_OUT_MENSAJES.addAll(salidaValorizar.P_OUT_MENSAJES);
						salida.IdInforme = salidaValorizar.LlaveReporte;
	 					valorizarInformeEntrada.p_Instrumentos.clear();
					}
			}
			contadorLoop++;			
		}
		
		if (salidaValorizar.P_OUT_ERRORESCONTADOR > 0L) {
			Long idCabeceraLog = salidaCartera.stream().findFirst().get().IdValorizacion;
			Log logValorizacion = new Log();
			logValorizacion.IdCabeceraLog = idCabeceraLog;
			logValorizacion.IdTipoLog = "ERROR";
			logValorizacion.IdModulo = entrada.p_IdModulo;
			logValorizacion.IdUsuario = entrada.p_IdUsuario;
			logValorizacion.IdEmpresa = entrada.p_IdEmpresa;
			logValorizacion.FechaSistemaLog = fechaSistema;
			logValorizacion.IdCliente = idCliente;
			logValorizacion.NombreCliente = empresa.NombreCompleto;
			logValorizacion.IdTipoRegistro = "INFORME_CARTERA_PROPIA";
			logValorizacion.NombreProceso = "Valorizacion Informe de Cartera Propia";

			agregarLogEntrada.p_Log.add(logValorizacion);
			agregarLogEntrada.agregarDetalleLogBasico(idCabeceraLog, 0L, "VALORIZACION", "", "", "MODIFICACION");

			OutAgregarLog agregarLogSalida = agregarLogService.logica(con, agregarLogEntrada);

			if (agregarLogSalida.P_OUT_ERRORESCONTADOR > 0L) {
				String codigoError = "ERROR_AGREGAR_LOG";
				MensajeSalida errorValorizacion = new MensajeSalida(0L, "ERROR", codigoError, Diccionario.getMensaje(con, entrada.p_IdEmpresa, codigoError).msg());
				salida.P_OUT_MENSAJES.add(errorValorizacion);
			}

			salida.P_OUT_ERRORESCONTADOR++;
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, "ERROR", "", "Error al Ejecutar Valorización"));
			return salida;
		}

		if (salidaValorizar.P_OUT_INSTRUMENTOSVALORIZADOS.size() > 0) {
			salida.P_OUT_INSTRUMENTOSVALORIZADOS = salidaValorizar.P_OUT_INSTRUMENTOSVALORIZADOS;
			Long ultimaValorizacion = salida.P_OUT_INSTRUMENTOSVALORIZADOS.stream()
					.max(Comparator.comparingLong(inst -> inst.IdValorizacion))
					.get().IdValorizacion;

			Log logValorizacion = new Log();
			logValorizacion.IdCabeceraLog = ultimaValorizacion;
			logValorizacion.IdTipoLog = "INFORMATIVO";
			logValorizacion.IdModulo = entrada.p_IdModulo;
			logValorizacion.IdUsuario = entrada.p_IdUsuario;
			logValorizacion.IdEmpresa = entrada.p_IdEmpresa;
			logValorizacion.FechaSistemaLog = fechaSistema;
			logValorizacion.IdCliente = idCliente;
			logValorizacion.NombreCliente = empresa.NombreCompleto;
			logValorizacion.IdTipoRegistro = "VALORIZACION_CARTERA_PROPIA";
			logValorizacion.NombreProceso = "Valorizar Informe de Cartera Propia";

			agregarLogEntrada.p_Log.add(logValorizacion);
			agregarLogEntrada.agregarDetalleLogBasico(ultimaValorizacion, 0L, "VALORIZACION", "", "", "MODIFICACION");

			OutAgregarLog agregarLogSalida = agregarLogService.logica(con, agregarLogEntrada);

			if (agregarLogSalida.P_OUT_ERRORESCONTADOR > 0L) {
				logger.debug("No se pudo agregar log");
				String codigoError = "ERROR_AGREGAR_LOG";
				MensajeSalida errorValorizacion = new MensajeSalida(0L, "ERROR", codigoError, Diccionario.getMensaje(con, entrada.p_IdEmpresa, codigoError).msg());
				salida.P_OUT_MENSAJES.add(errorValorizacion);
			}

			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, "INFORMACION", "", "Proceso Valorización Finalizado"));
			salida.IdInforme = salidaValorizar.LlaveReporte;
		}
		
		if (salida.IdInforme == null || salida.IdInforme.isEmpty()) {
			salida.P_OUT_ERRORESCONTADOR++;
			String codigoLlaveInvalida = "LLAVE_REPORTE_INVALIDA";
			MensajeSalida errorValorizacion = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, codigoLlaveInvalida, Diccionario.getMensaje(con,entrada.p_IdEmpresa, codigoLlaveInvalida).msg());
			salida.P_OUT_MENSAJES.add(errorValorizacion);
		}

		return salida;
	}

	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa02", "connections.properties")) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();	
			String Json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdModulo\":\"MRF\",\"p_Fecha\":\"2025-03-04T03:00:00.000Z\",\"p_FechaConsulta\":\"2025-03-04T03:00:00.000Z\",\"p_FechaValorizacion\":\"2025-03-05T03:00:00.000Z\",\"p_TipoValorizacion\":\"MERC\",\"p_IdCliente\":0,\"p_IdPortafolio\":0,\"p_IdSocioNegocio\":1598,\"p_TipoReporte\":\"CAR_PRO\",\"p_EjecutaValorizacion\":true}";
			EntradaConsultarCarteraPropia Objin = gson.fromJson(Json, EntradaConsultarCarteraPropia.class);
			
			ConsultarCarteraPropiaValorizada ConsultarCarteraPropia = new ConsultarCarteraPropiaValorizada();
			SalidaConsultarCarteraPropia salida = ConsultarCarteraPropia.logica(con, Objin);
			System.out.println(salida);
			con.commit();
		}

		System.out.println("== END ==");
	}

}
