package cl.bcs.custodia;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDate;

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
import cl.bcs.mesa.MRF.ConsultarCarteraPropiaValorizada.Cliente;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.OutAgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog.Log;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class InformeMayorYMenorValor implements JavaCall{

	static final Logger logger = LogManager.getLogger(InformeMayorYMenorValor.class);
	
	private final String SQL_SELECT_CLIENTE_SOCIONEGOCIO = loadResourceToString("cl/bcs/custodia/sql/InformeMayorYMenorValorCuentaPropia_SelectClienteSocioNegocio.sql");
	private final String SQL_SELECT_CLIENTE_MESA = loadResourceToString("cl/bcs/custodia/sql/InformeMayorYMenorValorCuentaPropia_SelectClienteMesa.sql");
	private final String SQL_SELECT_CARTERA_VALORIZADA = loadResourceToString("cl/bcs/custodia/sql/InformeMayorYMenorValorCuentaPropia_SelectCarteraValorizada.sql");
	
	private final String SQL_SELECT_CARTERAS_RFIF = loadResourceToString("cl/bcs/custodia/sql/InformeMayorYMenorValorCuentaPropia_SelectCarteras.sql");
	private final String SQL_SELECT_MESA_RFIF = loadResourceToString("cl/bcs/custodia/sql/InformeMayorYMenorValorCuentaPropia_SelectMesaRentaFija.sql");
	private final String SQL_SELECT_GARANTIAS_RFIF = loadResourceToString("cl/bcs/custodia/sql/InformeMayorYMenorValorCuentaPropia_SelectGarantias.sql");
	private final String SQL_SELECT_PRESTAMOS_RFIF = loadResourceToString("cl/bcs/custodia/sql/InformeMayorYMenorValorCuentaPropia_SelectPrestamos.sql");
	
	private final String SQL_SELECT_CARTERAS_HISTORICO_RFIF = loadResourceToString("cl/bcs/custodia/sql/InformeMayorYMenorValorCuentaPropia_SelectCarterasHistorico.sql");
	private final String SQL_SELECT_MESA_HISTORICO_RFIF = loadResourceToString("cl/bcs/custodia/sql/InformeMayorYMenorValorCuentaPropia_SelectMesaRentaFijaHistorico.sql");
	private final String SQL_SELECT_GARANTIAS_HISTORICO_RFIF = loadResourceToString("cl/bcs/custodia/sql/InformeMayorYMenorValorCuentaPropia_SelectGarantiasHistorico.sql");
	private final String SQL_SELECT_PRESTAMOS_HISTORICO_RFIF = loadResourceToString("cl/bcs/custodia/sql/InformeMayorYMenorValorCuentaPropia_SelectPrestamosHistorico.sql");
	
	private final String SQL_SELECT_CARTERAS_RV = loadResourceToString("cl/bcs/custodia/sql/InformeMayorYMenorValorCuentaPropia_SelectCarterasRV.sql");
	private final String SQL_SELECT_GARANTIAS_RV = loadResourceToString("cl/bcs/custodia/sql/InformeMayorYMenorValorCuentaPropia_SelectGarantiasRV.sql");
	private final String SQL_SELECT_PRESTAMOS_RV = loadResourceToString("cl/bcs/custodia/sql/InformeMayorYMenorValorCuentaPropia_SelectPrestamosRV.sql");
	
	private final String SQL_SELECT_CARTERAS_HISTORICO_RV = loadResourceToString("cl/bcs/custodia/sql/InformeMayorYMenorValorCuentaPropia_SelectCarterasRVHistorico.sql");
	private final String SQL_SELECT_GARANTIAS_HISTORICO_RV = loadResourceToString("cl/bcs/custodia/sql/InformeMayorYMenorValorCuentaPropia_SelectGarantiasRVHistorico.sql");
	private final String SQL_SELECT_PRESTAMOS_HISTORICO_RV = loadResourceToString("cl/bcs/custodia/sql/InformeMayorYMenorValorCuentaPropia_SelectPrestamosRVHistorico.sql");

	private final String SQL_SELECT_FECHA_ULTIMO_RESPALDO = loadResourceToString("cl/bcs/custodia/sql/InformeMayorYMenorValorCuentaPropia_SelectFechaUltimoRespaldo.sql");

	private static final String TIPO_INFORME = "MAYOR.MENOR.VALOR";
	
	public static class EntradaInformeMayorYMenorValor extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_FechaConsulta;
		public String p_FechaValorizacion;
		public Long p_IdCliente;
		public Long p_IdPortafolio;
		public String p_TipoValorizacion;
		public Long p_IdSocioNegocio;	
		public Boolean p_EjecutaValorizacion;
		public String p_IdTipoValorizacion;
		
		public EntradaInformeMayorYMenorValor() {
			this.p_IdTipoValorizacion = "RF";
			this.p_EjecutaValorizacion = false;
		}
	}
	
	public static class SalidaInformeMayorYMenorValor extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public List<ObjResultadoValorizacion> P_OUT_INSTRUMENTOSVALORIZADOS;
		public String IdInforme;

		public SalidaInformeMayorYMenorValor() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<>();
			this.P_OUT_INSTRUMENTOSVALORIZADOS = new ArrayList<>();
			this.IdInforme = "";
		}
	}

	public static class Operacion extends BcsPojo {
		public Long IdValorizacion = 0L;
		public Long IdCartera = 0L;
		public Long FolioCartera = 0L;
		public Long FolioOperacion = 0L;
		public Long IdOperacion = 0L;
		public String IdTipoOperacion;
		public Long IdCliente = 0L;
		public Long IdPortafolio = 0L;
		public String TablaOrigen;
		public String Diferenciador;
		public String FechaOperacion;
		public String FechaLiquidacion;
		public String FechaVencimientoOperacion;
		public String IdCondicionLiquidacion;
		public String IdTipoReajuste;
		public String CodigoIFRS;
		public String Nemotecnico;
		public String EsFungible;
		public BigDecimal Cantidad = BigDecimal.ZERO;
		public String IdEmisor;
		public String IdBoveda;
		public String NemoSVS;
		public Long IdInstrumento = 0L;
		public String IdTipoInstrumento;
		public String IdClasificacionTipoInstrumento;
		public String IdMonedaInstrumento;
		public String IdMonedaEmision;
		public String FechaEmision;
		public String FechaEmisionInstrumento;
		public String FechaVencimientoInstrumento;
		public BigDecimal MontoCompra = BigDecimal.ZERO;
		public BigDecimal MontoCompraMonedaSistema = BigDecimal.ZERO;
		public BigDecimal MontoCompraMonedaInstrumento = BigDecimal.ZERO;
		public BigDecimal ValorRescate = BigDecimal.ZERO;
		public BigDecimal PrecioCompra = BigDecimal.ZERO;	
		public BigDecimal TasaCompra = BigDecimal.ZERO;
		public BigDecimal MontoMonedaAdicionalInicial = BigDecimal.ZERO;
		public String FechaUltimoRespaldo;
	}

	@Override
	public JsonObject call(Connection con, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaInformeMayorYMenorValor jin = gson.fromJson(in.toString(), EntradaInformeMayorYMenorValor.class);
		SalidaInformeMayorYMenorValor out = logica(con,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public boolean ValidacionIds(Long id) {
		return id == null || id.equals(0L);
	}
	
	public SalidaInformeMayorYMenorValor logica(Connection con, EntradaInformeMayorYMenorValor entrada) throws Exception{
		logger.info("INICIO::InformeMayorYMenorValor ");
		SalidaInformeMayorYMenorValor salida = new SalidaInformeMayorYMenorValor();
		SQL sql=new SQL();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");		
		InformacionEmpresa empresa = new ConsultarEmpresa().logica(con, entrada.p_IdEmpresa);
		HashMap<String, Object> params = new HashMap<String, Object>(5);
		List<Cliente> clientes = new ArrayList<>();

		//VALIDACION DE VALORES FECHAS
		if(entrada.p_FechaConsulta == null || entrada.p_FechaValorizacion == null) {
			salida.P_OUT_ERRORESCONTADOR++;
			String codigoFechaInvalida = "FECHA_INVALIDA";
			MensajeSalida error = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR ,codigoFechaInvalida, Diccionario.getMensaje(con,entrada.p_IdEmpresa, codigoFechaInvalida).msg());
			salida.P_OUT_MENSAJES.add(error);
			return salida;
		}		
			
		LocalDate parsedFechaConsulta = LocalDate.parse(entrada.p_FechaConsulta);
		LocalDate parsedFechaValorizacion = LocalDate.parse(entrada.p_FechaValorizacion);
		LocalDate parsedFechaSistema = LocalDate.parse(fechaSistema);
		
		//VALIDACION DE FECHACONSULTA VS FECHAVALORIZACION
		if(parsedFechaValorizacion.isBefore(parsedFechaConsulta)) {
			salida.P_OUT_ERRORESCONTADOR++;
			String codigoFechaValorizacion = "FECHA_VALORIZACION_INVALIDA";
			MensajeSalida error = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR ,codigoFechaValorizacion, Diccionario.getMensaje(con,entrada.p_IdEmpresa, codigoFechaValorizacion).msg());
			salida.P_OUT_MENSAJES.add(error);
			return salida;
		}	
		
		//VALIDACION DE IDS NULOS O 0
		if(ValidacionIds(entrada.p_IdCliente) && ValidacionIds(entrada.p_IdSocioNegocio) && ValidacionIds(entrada.p_IdEmpresa)) {
			salida.P_OUT_ERRORESCONTADOR++;
			String codigoIdsNulos = "IDS_NULOS";
			MensajeSalida error = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR ,codigoIdsNulos, Diccionario.getMensaje(con,entrada.p_IdEmpresa, codigoIdsNulos).msg());
			salida.P_OUT_MENSAJES.add(error);
			return salida;
		}
		
		//VALIDACION DE CLIENTE
		if (entrada.p_IdCliente == 0L) {
		    if (entrada.p_IdSocioNegocio == 0L && entrada.p_IdEmpresa != 0L) {
		        params.put("IdEmpresa", entrada.p_IdEmpresa);
		        clientes = sql.query(con, SQL_SELECT_CLIENTE_MESA, params, Cliente.class);
		    } else if (entrada.p_IdSocioNegocio != 0L) {
		        params.put("IdSocioNegocio", entrada.p_IdSocioNegocio);
		        clientes = sql.query(con, SQL_SELECT_CLIENTE_SOCIONEGOCIO, params, Cliente.class);
		    }

		    entrada.p_IdCliente = (clientes != null && !clientes.isEmpty()) ? clientes.stream().findFirst().orElse(new Cliente()).IdCliente : 0L;
		}
		
		if (!entrada.p_EjecutaValorizacion) {
		    params.clear();
		    String partialKey = TIPO_INFORME + entrada.p_TipoValorizacion + parsedFechaConsulta + parsedFechaValorizacion;
		    params.put("p_Key", partialKey);
		    
		    Long existeValorizacion = sql.countQuery(con, SQL_SELECT_CARTERA_VALORIZADA, params);
		    
		    String tipo = existeValorizacion == 0L ? "ERROR" : "INFORMACION";
		    String mensaje = existeValorizacion.equals(0L) ? "No existe Valorización para Fecha Seleccionada" : "Proceso Valorización Finalizado";

		    if ("ERROR".equals(tipo)) {
		        salida.P_OUT_ERRORESCONTADOR++;
		    }
		    
		    salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, tipo, "", mensaje));
		    
		    return salida;
		}
				
		params.clear();
		params.put("IdCliente", entrada.p_IdCliente);
		params.put("IdPortafolio", entrada.p_IdPortafolio);
		params.put("FechaConsulta", entrada.p_FechaConsulta);
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		
		List<Operacion> operaciones = new ArrayList<>();

		HashMap<String, Object> paramRespaldo = new HashMap<>(1);
		paramRespaldo.put("p_Fecha", entrada.p_FechaConsulta);
		String fechaRespaldo = sql.query(con, SQL_SELECT_FECHA_ULTIMO_RESPALDO, paramRespaldo, Operacion.class).stream().findFirst().get().FechaUltimoRespaldo;
		
		if("RF".equals(entrada.p_IdTipoValorizacion)) {
			if(parsedFechaConsulta.isBefore(parsedFechaSistema)) {
				params.put("FechaConsulta", fechaRespaldo);
				operaciones.addAll(sql.query(con, SQL_SELECT_CARTERAS_HISTORICO_RFIF, params, Operacion.class));
				operaciones.addAll(sql.query(con, SQL_SELECT_GARANTIAS_HISTORICO_RFIF, params, Operacion.class));
				operaciones.addAll(sql.query(con, SQL_SELECT_PRESTAMOS_HISTORICO_RFIF, params, Operacion.class));
				params.put("IdClienteCorredor", empresa.IdCliente);
				operaciones.addAll(sql.query(con, SQL_SELECT_MESA_HISTORICO_RFIF, params, Operacion.class));
			}else {
				operaciones.addAll(sql.query(con, SQL_SELECT_CARTERAS_RFIF, params, Operacion.class));
				operaciones.addAll(sql.query(con, SQL_SELECT_GARANTIAS_RFIF, params, Operacion.class));
				operaciones.addAll(sql.query(con, SQL_SELECT_PRESTAMOS_RFIF, params, Operacion.class));
				params.put("IdClienteCorredor", empresa.IdCliente);
				operaciones.addAll(sql.query(con, SQL_SELECT_MESA_RFIF, params, Operacion.class));
			}
		} else if ("RV".equals(entrada.p_IdTipoValorizacion)) {
			if(parsedFechaConsulta.isBefore(parsedFechaSistema)) {
				params.put("FechaConsulta", fechaRespaldo);
				operaciones.addAll(sql.query(con, SQL_SELECT_CARTERAS_HISTORICO_RV, params, Operacion.class));
				operaciones.addAll(sql.query(con, SQL_SELECT_GARANTIAS_HISTORICO_RV, params, Operacion.class));
				operaciones.addAll(sql.query(con, SQL_SELECT_PRESTAMOS_HISTORICO_RV, params, Operacion.class));
			}else {
				operaciones.addAll(sql.query(con, SQL_SELECT_CARTERAS_RV, params, Operacion.class));
				operaciones.addAll(sql.query(con, SQL_SELECT_GARANTIAS_RV, params, Operacion.class));
				operaciones.addAll(sql.query(con, SQL_SELECT_PRESTAMOS_RV, params, Operacion.class));
			}
		}
		
		if (operaciones.isEmpty()) {
			logger.debug("No hay operaciones para valorizar.");				
			salida.P_OUT_ERRORESCONTADOR++;
			String codigoSinOperaciones = "SIN_OPERACIONES_PARA_VALORIZAR";
			MensajeSalida errorValorizacion = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, codigoSinOperaciones, Diccionario.getMensaje(con,entrada.p_IdEmpresa, codigoSinOperaciones).msg());
			salida.P_OUT_MENSAJES.add(errorValorizacion);
			return salida;
		}
			
		List<Long> IdsPortafolio = new ArrayList<Long>();

		if (entrada.p_IdPortafolio == null || entrada.p_IdPortafolio.equals(0L)) {
			IdsPortafolio = operaciones.stream().map(item -> item.IdPortafolio).distinct().collect(Collectors.toList());
		} else {
			IdsPortafolio.add(entrada.p_IdPortafolio);
		}

		/// SECCION VALORIZACION
		ValorizarInformeEntrada valorizarInformeEntrada = new ValorizarInformeEntrada();
		valorizarInformeEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
		valorizarInformeEntrada.p_IdUsuario = entrada.p_IdUsuario;
		valorizarInformeEntrada.p_IdModulo = entrada.p_IdModulo;
		valorizarInformeEntrada.p_IdPais = empresa.IdPais;
		valorizarInformeEntrada.p_IdMonedaAdicional = "USD";
		valorizarInformeEntrada.p_IdsPortafolio = IdsPortafolio;
		valorizarInformeEntrada.p_FechaInicial = entrada.p_FechaConsulta;
		valorizarInformeEntrada.p_FechaFinal = entrada.p_FechaValorizacion;
		valorizarInformeEntrada.p_OrigenProcesamiento = "RV".equals(entrada.p_IdTipoValorizacion) ? "MAYOR.MENOR.VALOR.RV." : "MAYOR.MENOR.VALOR.RF.";
		valorizarInformeEntrada.p_EsCuentaPropia = empresa.IdCliente.equals(entrada.p_IdCliente);
		valorizarInformeEntrada.p_IdTipoTasaValorizacion = entrada.p_TipoValorizacion;
		valorizarInformeEntrada.p_IdClienteSolicitante = entrada.p_IdCliente;
		valorizarInformeEntrada.p_IdFechaHoraSistema = FechaDB.ObtenerFechaHoraSistemaLocal(con, entrada.p_IdEmpresa);

		ValorizarInformeSalida salidaValorizar = new ValorizarInformeSalida();

		int contadorLoop = 0;

		for (Operacion detalle : operaciones) {
			ValorizarInforme.ObjEntradaInstrumentos entradaInstrumento = new ValorizarInforme.ObjEntradaInstrumentos();
			entradaInstrumento.TablaAfecta = detalle.TablaOrigen;
			entradaInstrumento.IdValorizacion = detalle.IdValorizacion;
			entradaInstrumento.TablaOrigen = detalle.TablaOrigen;
			entradaInstrumento.IdCartera = detalle.IdCartera;
			entradaInstrumento.FolioCartera = detalle.FolioCartera;
			entradaInstrumento.IdCliente = detalle.IdCliente;
			entradaInstrumento.IdPortafolio = detalle.IdPortafolio;
			entradaInstrumento.IdClasificacionTipoInstrumento = detalle.IdClasificacionTipoInstrumento;
			entradaInstrumento.IdTipoInstrumento = detalle.IdTipoInstrumento;
			entradaInstrumento.IdInstrumento = detalle.IdInstrumento;
			entradaInstrumento.FechaVencimientoInstrumento = detalle.FechaVencimientoInstrumento;
			entradaInstrumento.FechaVencimientoOperacion = detalle.FechaVencimientoOperacion;
			entradaInstrumento.IdOperacion = detalle.IdOperacion;
			entradaInstrumento.FechaEmision = detalle.FechaEmision;
			entradaInstrumento.FechaEmisionInstrumento = detalle.FechaEmisionInstrumento;
			entradaInstrumento.FechaOperacion = detalle.FechaOperacion;
			entradaInstrumento.FechaLiquidacion = detalle.FechaLiquidacion;
			entradaInstrumento.IdMonedaInstrumento = detalle.IdMonedaInstrumento;
			entradaInstrumento.IdTipoReajusteInstrumento = detalle.IdTipoReajuste;
			entradaInstrumento.IdMonedaEmisionInstrumento = detalle.IdMonedaEmision;
			entradaInstrumento.IdCondicionLiquidacion = detalle.IdCondicionLiquidacion;
			entradaInstrumento.IdTipoOperacion = detalle.IdTipoOperacion;
			entradaInstrumento.Emisor = detalle.IdEmisor;
			entradaInstrumento.EsFungible = detalle.EsFungible;
			entradaInstrumento.CodigoIFRS = detalle.CodigoIFRS;
			entradaInstrumento.Nemotecnico = detalle.Nemotecnico;
			entradaInstrumento.NemoSVS = detalle.NemoSVS;
			entradaInstrumento.Diferenciador = detalle.Diferenciador;
			entradaInstrumento.Cantidad = detalle.Cantidad;
			entradaInstrumento.MontoCompra = detalle.MontoCompra;
			entradaInstrumento.MontoCompraMonedaSistema = detalle.MontoCompraMonedaSistema;
			entradaInstrumento.MontoCompraMonedaInstrumento = detalle.MontoCompraMonedaInstrumento;
			entradaInstrumento.ValorRescate = detalle.ValorRescate;
			entradaInstrumento.PrecioCompra = detalle.PrecioCompra;
			entradaInstrumento.TasaCompra = detalle.TasaCompra;
			entradaInstrumento.TasaCompraValorizacionFechaOperacion = detalle.TasaCompra;
			entradaInstrumento.OrigenProcesamiento = "MAYOR.MENOR.VALOR";
			entradaInstrumento.IdBoveda = detalle.IdBoveda;
			entradaInstrumento.MontoCompraMonedaAdicional = detalle.MontoMonedaAdicionalInicial;
			entradaInstrumento.FolioOperacion = detalle.FolioOperacion;
			
			valorizarInformeEntrada.p_Instrumentos.add(entradaInstrumento);

			if ((contadorLoop != 0 && contadorLoop % 5000 == 0) || contadorLoop == operaciones.size() - 1) {
				logger.info("Valorizando hasta " + valorizarInformeEntrada.p_Instrumentos.size());
				ValorizarInforme ValorizarPapeles = new ValorizarInforme();
				try {
					salidaValorizar = ValorizarPapeles.logica(con, valorizarInformeEntrada);
				} catch (Exception err) {
					logger.error(err);
					err.printStackTrace();
					salida.P_OUT_ERRORESCONTADOR++;
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

		if (salida.P_OUT_ERRORESCONTADOR > 0L) {
			Long idValorizacion = operaciones.stream().findFirst().get().IdValorizacion;
			registrarLogGenerico(con, entrada, salidaValorizar, "ERROR", salida, fechaSistema, idValorizacion);
			return salida;
		}

		if (salidaValorizar.P_OUT_INSTRUMENTOSVALORIZADOS.size() > 0) {
			Long idValorizacion = salidaValorizar.P_OUT_INSTRUMENTOSVALORIZADOS.stream()
		            .max(Comparator.comparingLong(inst -> inst.IdValorizacion))
		            .orElseThrow(() -> new IllegalStateException("No se encontró IdValorizacion"))
		            .IdValorizacion;
			
			salida.P_OUT_INSTRUMENTOSVALORIZADOS = salidaValorizar.P_OUT_INSTRUMENTOSVALORIZADOS;
			registrarLogGenerico(con, entrada, salidaValorizar, "INFORMATIVO", salida, fechaSistema, idValorizacion);
			salida.IdInforme = salidaValorizar.LlaveReporte;
		}
		
		//VALIDACION LLAVE REPORTE INVALIDA
		if (salida.IdInforme == null || salida.IdInforme.isEmpty()) {
			salida.P_OUT_ERRORESCONTADOR++;
			String codigoLlaveInvalida = "LLAVE_REPORTE_INVALIDA";
			MensajeSalida errorValorizacion = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, codigoLlaveInvalida,
					Diccionario.getMensaje(con, entrada.p_IdEmpresa, codigoLlaveInvalida).msg());
			salida.P_OUT_MENSAJES.add(errorValorizacion);
		}
		
		logger.info("FIN::InformeMayorYMenorValor ");
		return salida;
	}
	
	private void registrarLogGenerico(Connection con, EntradaInformeMayorYMenorValor entrada, ValorizarInformeSalida salidaValorizar, String tipoLog, SalidaInformeMayorYMenorValor salida, String fechaSistema, Long idValorizacion) throws Exception {
		logger.info("INICIO::registrarLogGenerico ");
	    InAgregarLog agregarLogEntrada = new InAgregarLog();
		AgregarLog agregarLogService = new AgregarLog();
		
		agregarLogEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
		agregarLogEntrada.p_IdUsuario = entrada.p_IdUsuario;
		agregarLogEntrada.p_IdModulo = entrada.p_IdModulo;
		agregarLogEntrada.p_UsaReglasNegocio = "Si";
		agregarLogEntrada.p_IdEstadoAutorizacion = "S";
		agregarLogEntrada.p_IdTrabajo = 0L;
		
		String subregistroLog = "";
		String valorAntiguo = "";
		String valorNuevo = "";			
		
	    Log logValorizacion = new Log();
	    logValorizacion.IdCabeceraLog = idValorizacion;
	    logValorizacion.IdTipoLog = tipoLog;
	    logValorizacion.IdModulo = entrada.p_IdModulo;
	    logValorizacion.IdUsuario = entrada.p_IdUsuario;
	    logValorizacion.IdEmpresa = entrada.p_IdEmpresa;
	    logValorizacion.FechaSistemaLog = fechaSistema;
	    logValorizacion.IdCliente = entrada.p_IdCliente;
	    logValorizacion.NombreCliente = entrada.p_FechaConsulta;
	    logValorizacion.IdTipoRegistro = "INFORME_MAYOR_MENOR_VALOR";
	    logValorizacion.NombreProceso = "Valorizacion Informe Mayor y Menor Valor";

	    agregarLogEntrada.p_Log.add(logValorizacion);
	    
	    agregarLogEntrada.agregarDetalleLogBasico(idValorizacion, 0L, "VALORIZACION", valorAntiguo, valorNuevo, subregistroLog);
	    OutAgregarLog agregarLogSalida = agregarLogService.logica(con, agregarLogEntrada);

	    if (agregarLogSalida.P_OUT_ERRORESCONTADOR > 0) {
	        logger.debug("No se pudo agregar log");
	        String codigoError = "ERROR_AGREGAR_LOG";
	        MensajeSalida errorValorizacion = new MensajeSalida(0L, "ERROR", codigoError,
	                Diccionario.getMensaje(con, entrada.p_IdEmpresa, codigoError).msg());
	        salida.P_OUT_MENSAJES.add(errorValorizacion);
	    }

	    if ("INFORMATIVO".equals(tipoLog)) {
	        salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, "INFORMATIVO", "", "Proceso Valorización Finalizado"));
	        salida.IdInforme = salidaValorizar.LlaveReporte;
	    } else if ("ERROR".equals(tipoLog)) {
	        salida.P_OUT_ERRORESCONTADOR++;
	        salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, "ERROR", "", "Error al Ejecutar Valorización"));
	    }
		logger.info("FIN::registrarLogGenerico ");
	}

	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa02", "c:/optimus-corredores/connections.properties")) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();			

			String Json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdModulo\":\"CUS\",\"p_Fecha\":\"2025-07-28\",\"p_FechaConsulta\":\"2025-07-28\",\"p_FechaValorizacion\":\"2025-07-28\",\"p_TipoValorizacion\":\"MERC\",\"p_IdCliente\":0,\"p_IdPortafolio\":0,\"p_IdSocioNegocio\":1598,\"p_TipoReporte\":\"INF_MAME\",\"p_IdPais\":44,\"p_IdInforme\":\"\",\"p_EjecutaValorizacion\":true}";
			EntradaInformeMayorYMenorValor Objin = gson.fromJson(Json, EntradaInformeMayorYMenorValor.class);
			InformeMayorYMenorValor  demo=new InformeMayorYMenorValor();
			
			try {
				SalidaInformeMayorYMenorValor ObjOut=demo.logica(con, Objin);
				con.rollback();
				System.out.println(ObjOut.toString());
			}
			catch(Exception er) {
				er.printStackTrace();	
			} finally { 
				con.rollback();
			}			
    	}
		System.out.println("== END ==");
	}
}
