package cl.bcs.ordenes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.facturacion.CrearMovimientoComprobante;
import cl.bcs.facturacion.CrearMovimientoComprobante.CrearMovimientoComprobante_Salida;
import cl.bcs.facturacion.CrearMovimientoComprobante.MovimientoComprobante_Entrada;
import cl.bcs.facturacion.CrearMovimientoComprobante.MovimientoComprobante_Salida;
import cl.bcs.ordenes.ProcesarIngresoOperacionSpot.SalidaProcesarIngresoOperacionSpot;
import cl.bcs.mesa.SPT.CrearOperacionSpot.OperacionObjOut;
import cl.bcs.ordenes.pojo.FormaPagoSpot;
import cl.bcs.ordenes.ProcesarIngresoOperacionSpot;
import cl.bcs.ordenes.ProcesarIngresoOperacionSpot.EntradaProcesarIngresoOperacionSpot;
import cl.bcs.ordenes.CambiarEstadoAsignaciones.CambiarEstadoAsignaciones_Entrada;
import cl.bcs.ordenes.CambiarEstadoAsignaciones.CambiarEstadoAsignaciones_Salida;
import cl.bcs.ordenes.ProcesarIngresoOperacionSpot.AsignacionSpot;
import cl.bcs.ordenes.pojo.ConstantesOrdenes;
import cl.bcs.ordenes.pojo.FormaDePagoOrden;
import cl.bcs.plataforma.com.ConstantesPlataformaComercial;
import cl.bcs.plataforma.pla.ObtenerPlazo;
import cl.bcs.plataforma.pla.ObtenerPlazo.InObtenerPlazo;
import cl.bcs.plataforma.pla.ObtenerPlazo.OutObtenerPlazo;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class EnviarAsignacionesFacturar implements JavaCall {

	private final Logger logger = LogManager.getLogger(EnviarAsignacionesFacturar.class);
	private final String SQL_SELECT_ASIGNACIONES = loadResourceToString("cl/bcs/ordenes/sql/EnviarAsignacionesFacturar_SelectAsignaciones.sql");
	private final String SQL_SELECT_ASIGNACIONES_GRUPO = loadResourceToString("cl/bcs/ordenes/sql/EnviarAsignacionesFacturar_SelectAsignacionesGrupo.sql");
	private final String SQL_SELECT_COBROS = loadResourceToString("cl/bcs/ordenes/sql/EnviarAsignacionesFacturar_SelectCobros.sql");
	private final String SQL_SELECT_FORMAS_DE_PAGO = loadResourceToString("cl/bcs/ordenes/sql/EnviarAsignacionesFacturar_SelectFormasDePago.sql");
	private final String SQL_SELECT_COND_LIQUIDACION = loadResourceToString("cl/bcs/ordenes/sql/EnviarAsignacionesFacturar_SelectCondLiquidacion.sql");
	private final String SQL_UPDATE_ASIGNACIONES = loadResourceToString("cl/bcs/ordenes/sql/EnviarAsignacionesFacturar_UpdateAsignaciones.sql");
	private final String SQL_SELECT_PORCENTAJE_IMPUESTO = loadResourceToString("cl/bcs/ordenes/sql/EnviarAsignacionesFacturar_SelectPorcentajeImpuesto.sql");
	
	private final ConstantesOrdenes _c = new ConstantesOrdenes();
	private final ConstantesPlataformaComercial _constantesComisiones = new ConstantesPlataformaComercial();
	
	public static class CondicionDeLiquidacion {
		public String IdCondicionDeLiquidacion;
		public Integer PlazoEnDias;
	}
	
	public static class Cobro {
		public String IdCobro;
		public BigDecimal ValorCobro;
		public String IdMonedaCobro;
	}
	
	public static class EnviarAsignacionesFacturar_Entrada {
		public Long						p_IdEmpresa;
		public Long						p_IdUsuario;
		public String					p_IdModulo;
		public String					p_simularOperacion = "N";
		public List<Asignacion_Entrada> p_Asignaciones;
				
		public EnviarAsignacionesFacturar_Entrada() {
			this.p_Asignaciones = new ArrayList<Asignacion_Entrada>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Asignacion_Entrada {
		public Long IdAsignacion;
		public String GeneraComprobante;
		public String IdTipoComprobante;

		public Asignacion_Entrada() {
			IdTipoComprobante = "FAE";
		}

		public Asignacion_Entrada(Long idAsignacion, String generaComprobante) {
			this.IdAsignacion = idAsignacion;
			this.GeneraComprobante = generaComprobante;
			this.IdTipoComprobante = "FAE";
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	

	public static class EnviarAsignacionesFacturar_Salida {

		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		public List<Resultado>		P_OUT_RESULTADO;

		public EnviarAsignacionesFacturar_Salida() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_RESULTADO = new ArrayList<Resultado>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Resultado {
		public Long IdAsignacion;
		public Long IdTransaccion;
		public Long IdComprobante;
		public Long IdMovimientoComprobante;
		public String EsAPlazo;
		public Long IdCliente;
		public Long IdPortafolio;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = getGsonBuilder();
		EnviarAsignacionesFacturar_Entrada in = gson.fromJson(jin.toString(), EnviarAsignacionesFacturar_Entrada.class);
		EnviarAsignacionesFacturar_Salida out = logica(con, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}	
		
	public static class Asignacion_Proceso extends BcsPojo{
		public Long			IdComprobante;
		public String		AccionOrden;
		public BigDecimal 	CantidadAsignada;
		
		public String 		EsAPlazo;
		public String		EsCuentaPropia;
		public String		EsFungible;
		public String 		EsVentaCorta;
		
		public String		IdClasificacionTipoInstrumento;
		public String		IdEstadoAsignacion;
		public Long			IdAsignacionGrupo;
		public String		IdTipoInstrumento;
		public String 		IndicadorOrdenDirecta;
		public Long 		IdAsignacion;
		public Long 		IdAsignacionRef;
		public String 		IdBolsa;
		public String 		IdBoveda;
		public Long 		IdCliente;
		public String		IdCondicionDeLiquidacion;
		public String 		IdConcepto;
		public String 		IdEmisor;
		public Long		 	IdCorredoraContraparte;
		public Long			IdEmpleadoDigitador;
		public String 		IdFormaCustodia;
		public Long 		IdInstrumento;
		public Long 		IdInstrumentoSubyacente;
		public String 		IdMonedaTransaccion;
		public String 		IdMonedaLiquidacionOrden;
		public String 		IdMonedaSubyacente;
		public String 		IdMonedaNegociacion;
		public Long 		IdOrden;
		public Long 		IdPortafolio;
		public String 		IdTipoCanalIngreso;
		public String 		IdTipoOperacion;
		public String 		IdTipoProducto;
		public String 		IdTipoReajuste;
		public String 		IdTipoLiquidacionComisionFactura;
		public Long 		IdTransaccion;
		
		public Date			FechaEmision;
		public Date 		FechaLiquidacion;
		public Timestamp	FechaOperacionOrden;
		public Long			FolioCartera;
		public String 		FolioTransaccion;
		
		public String 		GlosaOrden;
		public String		MercadoSMV;
		public String		ModalidadSMV;
		public String		ModPrefFact;
		public BigDecimal 	MontoAsignado;
		public BigDecimal	MontoAsignadoLiquidacion;
		public BigDecimal	MontoAsignadoLocal;
		public BigDecimal	MontoAsignadoInteres;
		public BigDecimal   ReajustesVAC = BigDecimal.ZERO;
		public String 		Nemotecnico;
		public BigDecimal 	PrecioAsignacion;
		public BigDecimal 	PrecioAsignacionRaiz;
		public BigDecimal 	Paridad;
		public BigDecimal 	ValorRescateAsignado;
		public BigDecimal 	TipoCambio;
		public BigDecimal 	MontoAsignadoAPlazo;
		public BigDecimal 	PrecioAPlazo;
		public Long 		PlazoIF;
		public Long 		PlazoVentaCorta;
		public Long 		PlazoSimultanea;
		public Long 		PlazoReporte;
		public BigDecimal 	TasaReporte;
		public BigDecimal 			TasaRFIF;
		public BigDecimal 			TasaSimultanea;
		public BigDecimal 			TasaVentaCorta;
		public List<Cobro_Proceso> 	Cobros;
		public Long 				advertencias = 0L;
		public Long					errores = 0L;
		public Long					ok = 0L;
		public Long					IdMovimientoComprobante;
		public String 				GeneraComprobante = "N";
		public Date 				FechaParidad;
		public Date 				FechaTipoCambio;
		public Date					FechaVencimiento;
		public Timestamp			FechaOperacionTransaccion;
		public FormaDePagoOrden 	formaPago;
		public Integer				DiasLiquidacion;
		public String 				NemotecnicoSVS;
		
		public String 				CodigoBancoLiquidador;
		public String 				CodigoBancoCustodio;
		public String 				CodigoBroker;
		public Long 				IdPersonaBancoLiquidador;
		public Long 				IdPersonaBancoCustodio;
		public Long 				IdPersonaBroker;
		public String 				IdTipoCliente;
		public String	 			EsBroker;
		public String				CodigoIFRS;
		public String 				EsClienteFinal;
		public Long					IdCuentaCustodioPortafolio;
		public String 				IdMonedaEmision;

		public String               IdTipoComprobante;
		
		public String				GeneraSpot;
		public Long					IdOperacionSpot;
		
		public Asignacion_Proceso() {
			this.Cobros = new ArrayList<Cobro_Proceso>();
			this.IdTipoLiquidacionComisionFactura = "";
			this.ModPrefFact = "";
			this.IdTipoComprobante = "FAE";
			this.GeneraSpot = "N";
			this.IdOperacionSpot = 0L;
		}
		public String getGeneraSpot() {
	        return GeneraSpot;
	    }
	}
	
	public static class Cobro_Proceso {
		public String IdCobro;
		public String IdMonedaCobro;
		public BigDecimal MontoCobro;
		public BigDecimal MontoMovimientoComprobanteCobro;
		public BigDecimal MontoLiquidacionCobro;
		public BigDecimal Porcentaje;
		public Long IdAsignacion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public void limpiarAsignaciones(List<Asignacion_Proceso> asignaciones, List<Asignacion_Proceso> asignacionesConErrores) {
		for (Asignacion_Proceso ord : asignaciones) {
			if (ord.errores > 0) {
				asignacionesConErrores.add(ord);
			}
		}
		
		for (Asignacion_Proceso ord : asignacionesConErrores) {
			if (asignaciones.contains(ord))
				asignaciones.remove(ord);
		}
	}
	
	public String DeterminaTipoComprobante(String parametroTipoComprobantePorDefecto,String facturaConfiguradaSistema) {
		String TipoComprobante = facturaConfiguradaSistema;
		if ("".equals(parametroTipoComprobantePorDefecto)) {
			TipoComprobante				= facturaConfiguradaSistema;
		}
		else {
			TipoComprobante				= parametroTipoComprobantePorDefecto;
		}
		return TipoComprobante;
	}
	
	private void ProcesaRequisito(Connection con,Long idEmpresa,boolean esError,String codMsg,String CodRequisito,Asignacion_Proceso asig,List<MensajeSalida> P_OUT_MENSAJES) 
			throws Exception {
		if (esError) {
			String msg=Diccionario.getMensaje(con,idEmpresa,codMsg)
				  .add("IdOrden", asig.IdOrden)
			      .add("IdCliente", asig.IdCliente)
				  .add("IdPortafolio", asig.IdPortafolio)
				  .add("IdInstrumento", asig.IdInstrumento)
				  .add("IdBolsa", asig.IdBolsa)
				  .add("IdTipoOperacion", asig.IdTipoOperacion)
				  .add("IdCondicionDeLiquidacionContado", asig.IdCondicionDeLiquidacion)
				  .add("IdBoveda", asig.IdBoveda)
				  .add("IdFormaCustodia", asig.IdFormaCustodia)
				  .add("PlazoVentaCorta", asig.PlazoVentaCorta)
				  .add("PlazoSimultanea", asig.PlazoSimultanea)
				  .add("TasaVentaCorta", asig.TasaVentaCorta)
				  .add("TasaSimultanea", asig.TasaSimultanea)
				  .add("IdEmpleadoDigitador", asig.IdEmpleadoDigitador)
				  .add("IdMonedaLiquidacionOrden", asig.IdMonedaLiquidacionOrden)
				  .msg();
			if (CodRequisito.equals(MensajeSalida.MENSAJE_ERROR)) {
				P_OUT_MENSAJES.add(new MensajeSalida(asig.IdAsignacion,MensajeSalida.MENSAJE_ERROR,codMsg,msg));
				asig.errores++;
			}
			else if (CodRequisito.equals(MensajeSalida.MENSAJE_ADVERTENCIA)) {
				P_OUT_MENSAJES.add(new MensajeSalida(asig.IdAsignacion,MensajeSalida.MENSAJE_ADVERTENCIA,codMsg,msg));
				asig.advertencias++;
			}
			else {
				throw new Exception("Error al validar operaciones");
				}
		}else {
			asig.ok++;
		}		
	}
	
	@Trace
	public EnviarAsignacionesFacturar_Salida logica(Connection conn, EnviarAsignacionesFacturar_Entrada in) throws Exception {
		logger.info("INICIO::EnviarAsignacionesFacturar");
		SQL _sql = new SQL();
		EnviarAsignacionesFacturar_Salida out = new EnviarAsignacionesFacturar_Salida();
		
		if (in.p_Asignaciones.size() == 0) {
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_INGRESARON_ASIGNACIONES",null,conn,in.p_IdEmpresa));
			out.P_OUT_ERRORESCONTADOR++;
			return out;
		}
		
		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(conn, in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
		if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
			out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
			out.P_OUT_ERRORESCONTADOR += MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
			return out;
		}
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		String paramLiquidacionComisionFact = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PRODUCTO", "PRO.CobroComisionesMensuales");
		String paramTipoFacturaPorDefecto = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "ORDENES", "TipoFacturaDefectoAsignaciones");
		paramLiquidacionComisionFact = "ACTIVO".equals(paramLiquidacionComisionFact) ? "S" : "N";
		ConsultarEmpresa serviceConsultarEmpresa = new ConsultarEmpresa();
		InformacionEmpresa empresa = serviceConsultarEmpresa.logica(conn, in.p_IdEmpresa);
		List<CondicionDeLiquidacion> condLiquidacion = _sql.query(conn, SQL_SELECT_COND_LIQUIDACION, new HashMap<>(), CondicionDeLiquidacion.class);
		String localizacionSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.LocalizacionSistema");
		String cobroMXsobrePagoMX = obtieneParametroCobroMXsobrePagoMX(conn, in.p_IdEmpresa);
		String productosSinCobroMXsobrePagoMX = obtieneParametroProductosSinCobroMXsobrePagoMX(conn, in.p_IdEmpresa);
		List<String> productosSinCobroMX = new ArrayList<>();
		try {
			String[] listado = productosSinCobroMXsobrePagoMX.split(",");
			productosSinCobroMX = Arrays.asList(listado);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		Cobro impuestoIva = null;
		{
			if (localizacionSistema.equalsIgnoreCase("CL")) {
				HashMap<String,Object> params2 = new HashMap<>();
				params2.put("IdCobro", _c.CODIGO_COBRO_IVA);
				params2.put("IdEmpresa", in.p_IdEmpresa);
				List<Cobro> impuestoChile = _sql.query(conn, SQL_SELECT_PORCENTAJE_IMPUESTO, params2, Cobro.class);
				if (impuestoChile.size() == 0) {
					String codigo = "IVA_NO_CONFIGURADO";
					String descripcion = Diccionario.getMensaje(conn, in.p_IdEmpresa,codigo)
							.msg();
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_INFORMACION,codigo,descripcion));
					return out;
				}
				impuestoIva = impuestoChile.get(0);
			}
		}
		
		HashMap<Long,Asignacion_Proceso> hashAsignaciones = new HashMap<>();
		List<Asignacion_Proceso> listaAsignaciones = new ArrayList<>();
		List<Asignacion_Proceso> listaAsignacionesConErrores = new ArrayList<>();
		{
			HashMap<String,Object> params1 = new HashMap<>();
			ArrayList<Long> idsAsignacion = new ArrayList<Long>();
			for (Asignacion_Entrada asig : in.p_Asignaciones) {
				idsAsignacion.add(asig.IdAsignacion);
			}
			params1.put("idsAsignacion", idsAsignacion);
			params1.put("localizacionSistema", localizacionSistema);
			listaAsignaciones = _sql.query(conn, SQL_SELECT_ASIGNACIONES, params1, Asignacion_Proceso.class);
			for (Asignacion_Entrada asigEnt : in.p_Asignaciones) {
				for (Asignacion_Proceso asig : listaAsignaciones) {
					if (asigEnt.IdAsignacion.equals(asig.IdAsignacion)) {
						asig.IdTipoComprobante = asigEnt.IdTipoComprobante;
						hashAsignaciones.put(asig.IdAsignacion, asig);
					}
				}
				
				if (!hashAsignaciones.containsKey(asigEnt.IdAsignacion)) {
					out.P_OUT_MENSAJES.add(new MensajeSalida(asigEnt.IdAsignacion,MensajeSalida.MENSAJE_ERROR,"ASIGNACION_NO_EXISTE",null,conn,in.p_IdEmpresa));
				}
			}
			
			//PARA CARGAR ASIGNACIONES ADICIONALES
			ArrayList<Long> idsAsignacionPlazo = new ArrayList<Long>();
			
			//ASIGNACIONES AMARRADAS POR REPORTE
			ArrayList<Long> idsAsignacionGrupo = new ArrayList<Long>();
			for (Asignacion_Proceso asig : listaAsignaciones) {
				if (asig.IdAsignacionGrupo != null && asig.IdAsignacionGrupo != 0L && !idsAsignacionGrupo.contains(asig.IdAsignacionGrupo)) {
					idsAsignacionGrupo.add(asig.IdAsignacionGrupo);
				}
			}
			
			if (idsAsignacionGrupo.size() > 0) {
				HashMap<String,Object> paramsGrupo = new HashMap<>();
				paramsGrupo.put("idsAsignacionGrupo", idsAsignacionGrupo);
				List<Asignacion_Proceso> asignacionesGrupo = _sql.query(conn, SQL_SELECT_ASIGNACIONES_GRUPO, paramsGrupo , Asignacion_Proceso.class);
				for (Asignacion_Proceso asig : asignacionesGrupo) {
					if (!idsAsignacionPlazo.contains(asig.IdAsignacion) && !idsAsignacion.contains(asig.IdAsignacion)) {
						idsAsignacionPlazo.add(asig.IdAsignacion);
					}
				}
			}
			
			//ASIGNACIONES A PLAZO
			for (Asignacion_Proceso asig : listaAsignaciones) {
				if ((asig.IdTipoInstrumento.equalsIgnoreCase(_c.TIPO_INSTRUMENTO_SIM) || asig.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RE)) && asig.EsAPlazo.equalsIgnoreCase(_c.NO)) {
					if (hashAsignaciones.get(asig.IdAsignacionRef) == null && !idsAsignacionPlazo.contains(asig.IdAsignacionRef)) {
						idsAsignacionPlazo.add(asig.IdAsignacionRef);
					}
				}
			
				if ((asig.IdTipoInstrumento.equalsIgnoreCase(_c.TIPO_INSTRUMENTO_SIM)  || asig.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RE)) && asig.EsAPlazo.equalsIgnoreCase(_c.SI)) {
					if (hashAsignaciones.get(asig.IdAsignacionRef) == null && !idsAsignacionPlazo.contains(asig.IdAsignacionRef)) {
						idsAsignacionPlazo.add(asig.IdAsignacionRef);
					}
				}
			}
			
			if (idsAsignacionPlazo.size() > 0) {
				HashMap<String,Object> params2 = new HashMap<>();
				params2.put("idsAsignacion", idsAsignacionPlazo);
				params2.put("localizacionSistema", localizacionSistema);
				List<Asignacion_Proceso> _infoAsignacionesPlazo = _sql.query(conn, SQL_SELECT_ASIGNACIONES, params2, Asignacion_Proceso.class);
				listaAsignaciones.addAll(_infoAsignacionesPlazo);
				for (Asignacion_Proceso asig : _infoAsignacionesPlazo) {
					hashAsignaciones.put(asig.IdAsignacion, asig);
				}
			}
		}
		
		limpiarAsignaciones(listaAsignaciones, listaAsignacionesConErrores);
		if (listaAsignaciones.size() == 0) {
			out.P_OUT_ERRORESCONTADOR += MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
			return out;
		}
		
		
		//actualiza monto plazo y precio plazo
		for (Asignacion_Proceso asig : listaAsignaciones) {
			
			for (Asignacion_Entrada asigEntrada : in.p_Asignaciones) {
				if (asig.IdAsignacion.equals(asigEntrada.IdAsignacion)) {
					asig.GeneraComprobante = asigEntrada.GeneraComprobante;
				}
			}
			
			if ((asig.IdTipoInstrumento.equalsIgnoreCase(_c.TIPO_INSTRUMENTO_SIM) || asig.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RE))  && asig.EsAPlazo.equalsIgnoreCase(_c.NO)) {
				Asignacion_Proceso asigPlazo = hashAsignaciones.get(asig.IdAsignacionRef);
				asig.MontoAsignadoAPlazo = asigPlazo.MontoAsignado;
				asig.PrecioAPlazo = asigPlazo.PrecioAsignacion;
			}
			if ((asig.IdTipoInstrumento.equalsIgnoreCase(_c.TIPO_INSTRUMENTO_SIM) || asig.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RE)) && asig.EsAPlazo.equalsIgnoreCase(_c.SI)) {
				asig.MontoAsignadoAPlazo = BigDecimal.ZERO;
				asig.PrecioAPlazo = BigDecimal.ZERO;
			}
		}
		
		//VALIDACIONES
		{
			for (Asignacion_Proceso asig : listaAsignaciones) {
				
				ProcesaRequisito(conn, in.p_IdEmpresa,hashAsignaciones.get(asig.IdAsignacion) == null, "ASIGNACION_NO_EXISTE", MensajeSalida.MENSAJE_ERROR, asig, out.P_OUT_MENSAJES);
				if (hashAsignaciones.get(asig.IdAsignacion) != null) {
					ProcesaRequisito(conn, in.p_IdEmpresa,asig.IdEstadoAsignacion.equalsIgnoreCase(_c.ESTADO_ASIGNACION_ENVIADA_FACTURAR), "ASIGNACION_ESTA_ENVIADA_A_FACTURAR", MensajeSalida.MENSAJE_ERROR, asig, out.P_OUT_MENSAJES);
					ProcesaRequisito(conn, in.p_IdEmpresa,asig.IdEstadoAsignacion.equalsIgnoreCase(_c.ESTADO_ASIGNACION_ANULADA), "ASIGNACION_ESTA_ANULADA", MensajeSalida.MENSAJE_ERROR, asig, out.P_OUT_MENSAJES);
					ProcesaRequisito(conn, in.p_IdEmpresa, asig.IdEstadoAsignacion.equalsIgnoreCase(_c.ESTADO_ASIGNACION_PENDIENTE_AUTORIZACION), "ASIGNACION_ESTA_PENDIENTE_AUTORIZACION", MensajeSalida.MENSAJE_ERROR, asig, out.P_OUT_MENSAJES);
				}
				
			}
			limpiarAsignaciones(listaAsignaciones, listaAsignacionesConErrores);
			if (listaAsignaciones.size() == 0) {
				out.P_OUT_ERRORESCONTADOR += MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
				return out;
			}
		}
		
		//cobros
		{
			HashMap<String,Object> params1 = new HashMap<>();
			ArrayList<Long> idsAsignacion = new ArrayList<Long>();
			for (Asignacion_Proceso asig : listaAsignaciones) {
				idsAsignacion.add(asig.IdAsignacion);
			}
			params1.put("idsAsignacion", idsAsignacion);
			List<Cobro_Proceso> _listaCobros = _sql.query(conn, SQL_SELECT_COBROS, params1, Cobro_Proceso.class);
			for (Asignacion_Proceso asig : listaAsignaciones) {
				for (Cobro_Proceso cob : _listaCobros) {
					if (cob.IdAsignacion.equals(asig.IdAsignacion)) {
						if(_constantesComisiones.cobrosFijos.contains(cob.IdCobro)) cob.Porcentaje = BigDecimal.ZERO;
						asig.Cobros.add(cob);
					}
				}
			}
		}
		
		//FORMAS DE PAGO
		{
			HashMap<String,Object> params1 = new HashMap<>();
			ArrayList<Long> idsOrden = new ArrayList<Long>();
			for (Asignacion_Proceso asig : listaAsignaciones) {
				idsOrden.add(asig.IdOrden);
			}
			params1.put("idsOrden", idsOrden);
			List<FormaDePagoOrden> _formasDePago = _sql.query(conn, SQL_SELECT_FORMAS_DE_PAGO, params1, FormaDePagoOrden.class);
			for (FormaDePagoOrden forma : _formasDePago) {
				for (Asignacion_Proceso asig : listaAsignaciones) {
					if (asig.IdOrden.equals(forma.IdOrden)) {
						asig.formaPago = forma;
					}
				}
			}
		}

		CrearMovimientoComprobante.CrearMovimientoComprobante_Entrada entradaCrearMovimientoComprobante = new CrearMovimientoComprobante.CrearMovimientoComprobante_Entrada();
		entradaCrearMovimientoComprobante.p_IdEmpresa = in.p_IdEmpresa;
		entradaCrearMovimientoComprobante.p_IdUsuario = in.p_IdUsuario;
		entradaCrearMovimientoComprobante.p_IdModulo = in.p_IdModulo;
		entradaCrearMovimientoComprobante.p_simularOperacion = in.p_simularOperacion;
		entradaCrearMovimientoComprobante.p_UsaReglasNegocio = "S";
		entradaCrearMovimientoComprobante.p_EsIngresoAutomatico = "N";
		
		Long indexCobro = 0L;
		Long indexFormaDePago = 0L;
		
		listaAsignaciones.sort((asigA, asigB) -> asigA.IdAsignacion.compareTo(asigB.IdAsignacion));		
		for (Asignacion_Proceso info : listaAsignaciones) {
			
			CrearMovimientoComprobante.MovimientoComprobante_Entrada mov = new CrearMovimientoComprobante.MovimientoComprobante_Entrada();
			if (info.EsAPlazo.equalsIgnoreCase(_c.SI)) {
				mov.ActualizarCuentaInversion = "N";
				mov.ActualizarCustodia = "N";
			}
			else {
				mov.ActualizarCuentaInversion = "S";
				mov.ActualizarCustodia = "S";
			}
			mov.ActualizarCustodiaRelacionado 	= "N";
			
			if ((info.EsAPlazo.equalsIgnoreCase(_c.SI)) || 
					(info.formaPago != null && info.formaPago.IdMedioDePago.equalsIgnoreCase(_c.MEDIO_PAGO_CUENTA_INVERSION))
					
					) {
				mov.ActualizarTesoreria 			= "N";
			} else{
				//ENVIAMOS A TESORERIA SOLO CUANDO NO SEA CUENTA PROPIA
				if (info.IdCliente.equals(empresa.IdCliente)) {
					mov.ActualizarTesoreria 			= "N";
				}
				else {
					mov.ActualizarTesoreria 			= "S";
				}
			}
			
			mov.Cantidad 						= info.CantidadAsignada;
			mov.CrearComprobanteAutomatico		= info.GeneraComprobante;
			mov.CodigoBancoCustodio				= info.CodigoBancoCustodio;
			mov.CodigoBancoLiquidador			= info.CodigoBancoLiquidador;
			mov.CodigoBroker					= info.CodigoBroker;
			mov.EsAPlazo						= info.EsAPlazo;
			mov.IdOperacion 					= info.IdAsignacionGrupo;//quedan amarradas las ordenes de reportes (reportado/reportante)
			
			if (mov.PrecioAPlazo == null) {
				mov.PrecioAPlazo = BigDecimal.ZERO; //PARCHE TEMPORAL CREARASIGNACION DEBERIA DEJARLO EN 0
				mov.MontoAPlazo = BigDecimal.ZERO;
			}
			
			mov.EsComprobIndividual				= "N";
			
			if (info.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RE)) {
				mov.EsComprobIndividual			= "S";
				mov.DiasPlazo					= new BigDecimal(info.PlazoReporte);
				mov.MontoAPlazo					= info.MontoAsignadoAPlazo;
				mov.PrecioAPlazo				= info.PrecioAPlazo;
				mov.Tasa						= info.TasaReporte;
			}
			else if (info.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_IF)) {
				mov.DiasPlazo					= new BigDecimal(info.PlazoIF);
				mov.Tasa						= info.TasaRFIF;
			}
			else if (info.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RF) && info.EsVentaCorta.equalsIgnoreCase(_c.NO)) {
				mov.DiasPlazo					= BigDecimal.ZERO;
				mov.TasaOperacion 				= BigDecimal.ZERO;
				mov.Tasa						= info.TasaRFIF;
			}
			else if (info.EsVentaCorta.equalsIgnoreCase(_c.SI)) {
				mov.TasaOperacion 				= info.TasaVentaCorta;
				mov.Tasa						= info.TasaVentaCorta;
				mov.DiasPlazo					= new BigDecimal(info.PlazoVentaCorta);
				if ( info.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RF) ) { mov.Tasa	= info.TasaRFIF;}
			}
			else if (info.IdTipoInstrumento.equalsIgnoreCase(_c.TIPO_INSTRUMENTO_SIM)) {
				mov.EsComprobIndividual			= "S";
				mov.DiasPlazo					= new BigDecimal(info.PlazoSimultanea);
				mov.Tasa						= info.TasaSimultanea;
				mov.PrecioAPlazo				= info.PrecioAPlazo;
				mov.MontoAPlazo					= info.MontoAsignadoAPlazo;
				 //TODO  HABILITAR USO DE TASAOPERACION PARA ESTE CASO? mov.TasaOperacion 				= info.TasaSimultanea;     
			} else {
				mov.MontoAPlazo					= BigDecimal.ZERO;
				mov.PrecioAPlazo				= BigDecimal.ZERO;
				mov.DiasPlazo					= BigDecimal.ZERO;
				mov.Tasa						= BigDecimal.ZERO;
				mov.TasaOperacion 				= BigDecimal.ZERO;
			}
			
			if (empresa.IdCliente.equals(info.IdCliente)) {
				mov.EsCuentaPropia 					= _c.SI;
			}
			else {
				mov.EsCuentaPropia 					= _c.NO;
			}
			
			mov.EsClienteFinal					= info.EsClienteFinal;	
			mov.EsEnRueda						= "S"; //TODO REVISAR COMO CALCULAR ESTE VALOR
			mov.EsFungible						= info.EsFungible;
			mov.EsPrecioPrima					= "N";
			mov.EsRuteo							= "N"; //TODO OBTENER A TRAVES DE UNA COLUMNA EN TABLA DE CANAL DISTRIBUCION
			mov.EsVentaCorta					= info.EsVentaCorta;
			mov.FechaEmision					= Fecha.toStringDate(info.FechaEmision);
			
			
			if (info.FechaLiquidacion.before(Fecha.toDate(fechaSistema))) {
				//SI YA LIQUIDO, LO DEJAMOS T0
				mov.FechaLiquidacion				= fechaSistema;	
			}
			else {
				mov.FechaLiquidacion				= Fecha.toStringDate(info.FechaLiquidacion);
			}
			
			
			if (localizacionSistema.equalsIgnoreCase("PE")) {
				mov.FechaMovimientoComprobante		= Fecha.toStringDate(info.FechaOperacionTransaccion);	
			}else{
				mov.FechaMovimientoComprobante		= fechaSistema;
			}
			
			
			mov.FechaOrden						= Fecha.toStringDatetime(info.FechaOperacionOrden);
			mov.FechaParidad					= Fecha.toStringDate(info.FechaParidad);
			mov.FechaTipoCambio					= Fecha.toStringDate(info.FechaTipoCambio, "0001-01-01");
			mov.FechaTransaccion				= Fecha.toStringDatetime(info.FechaOperacionTransaccion);
			mov.FechaVencimiento				= Fecha.toStringDate(info.FechaVencimiento);
			mov.FolioCartera					= info.FolioCartera;
			mov.FolioTransaccion				= info.FolioTransaccion;
			mov.Glosa							= info.GlosaOrden;
			mov.IdAsignacion					= info.IdAsignacion;
			mov.IdAsignacionRef					= info.IdAsignacionRef;
			mov.IdBolsa							= info.IdBolsa;
			mov.IdBoveda						= info.IdBoveda;
			mov.IdCliente						= info.IdCliente;
			mov.IdConcepto 						= info.IdConcepto;
			
			if("S".equalsIgnoreCase(info.EsVentaCorta)) {
				if(info.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RV)) {
					mov.IdConcepto 				= "VCACRV";
				}
				if(info.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RF)) {
					mov.IdConcepto 				= "VCRF";
				}
			}
			
			mov.IdCondicionLiquidacion			= info.IdCondicionDeLiquidacion;	//SE REVALIDA MAS ABAJO
			mov.IdCorredoraContraparte			= info.IdCorredoraContraparte;
			mov.IdCuentaCustodioPortafolio		= info.IdCuentaCustodioPortafolio;
			mov.IdEmisor						= info.IdEmisor;
			mov.IdEmpleadoDigitador				= info.IdEmpleadoDigitador;
			mov.IdEmpresa						= in.p_IdEmpresa;
			mov.IdFormaCustodia					= info.IdFormaCustodia;
			mov.IdInstrumento					= info.IdInstrumento;
			mov.IdInstrumentoRelacionado		= 0L;
			mov.IdInstrumentoSubyacente			= info.IdInstrumentoSubyacente;
			mov.IdMesa							= 0L;
			mov.IdMoneda						= info.IdMonedaTransaccion;
			mov.IdMonedaEmision					= info.IdMonedaEmision;
			mov.IdMonedaLiquidacion				= info.IdMonedaLiquidacionOrden;
			mov.IdMonedaSubyacente				= info.IdMonedaSubyacente;
			mov.IdMonedaNegociacion             = info.IdMonedaNegociacion;
			mov.IdOrden							= info.IdOrden;
			mov.IdPortafolio					= info.IdPortafolio;
			mov.IdTipoCliente					= (info.IdTipoCliente!=null) ? info.IdTipoCliente : "";
			mov.IdTipoCanalIngreso				= info.IdTipoCanalIngreso;
			mov.IdTipoComprobante 				= DeterminaTipoComprobante(paramTipoFacturaPorDefecto,info.IdTipoComprobante);
			
			mov.IdTipoConvenio					= "SCV";
			mov.IdTipoOperacion					= info.IdTipoOperacion;
			mov.IdTipoProducto					= info.IdTipoProducto;
			mov.IdTipoReajuste					= info.IdTipoReajuste;
			mov.IdTransaccion					= info.IdTransaccion;
			mov.IdUsuario						= in.p_IdUsuario;
			mov.IndicadorOrdenDirecta			= info.IndicadorOrdenDirecta;
			mov.Intereses						= info.MontoAsignadoInteres;//BigDecimal.ZERO;//info.Intereses;
			mov.IdPersonaBroker					= info.IdPersonaBroker;
			mov.IdPersonaBancoCustodio			= info.IdPersonaBancoCustodio;
			mov.IdPersonaBancoLiquidador		= info.IdPersonaBancoLiquidador;
			
			if (info.IdPersonaBancoCustodio != null && info.IdPersonaBancoCustodio > 0) {
				mov.LiquidaBancoCustodio			= "S";	
			}
			else {
				mov.LiquidaBancoCustodio			= "N";
			}
			
			mov.MercadoSMV 						= info.MercadoSMV;
			mov.ModalidadSMV 					= info.ModalidadSMV;
			mov.ModPrefFact						= null; //TODO LEER DE PREFERENCIAS DEL PORTAFOLIO (MARCA FACTURA INMEDIATA) BSCL.Facturacion.BD.Vistas::ConsultarPortafolioTipoContratoCobroPeriodico
			mov.Monto							= info.MontoAsignado;
			mov.MontoRaiz						= info.PrecioAsignacionRaiz.multiply(info.CantidadAsignada);
			mov.MontoDescuento					= BigDecimal.ZERO;
			mov.MontoInteres					= info.MontoAsignadoInteres;//BigDecimal.ZERO;
			mov.MontoLiquidacion				= info.MontoAsignadoLiquidacion;
			mov.MontoMovimientoComprobante		= info.MontoAsignadoLocal;
			mov.NemotecnicoInstrumento			= info.Nemotecnico;
			mov.NemoSVS							= info.NemotecnicoSVS;
			mov.NumeroMvtoCmbte					= info.IdAsignacion;
			mov.Precio							= info.PrecioAsignacion;
			mov.PrecioRaiz						= info.PrecioAsignacionRaiz;
			mov.ReajustesVAC					= info.ReajustesVAC;//BigDecimal.ZERO;;
			
			mov.SecuenciaAgrupacion				= "";
			mov.Spread							= BigDecimal.ZERO;
			mov.TasaAgente						= BigDecimal.ZERO;
			mov.TasaEmision						= BigDecimal.ZERO;
			mov.ValorCaptacionMonedaOrigen		= BigDecimal.ZERO;
			mov.ValorParidad					= info.Paridad;
			mov.ValorRescate					= info.ValorRescateAsignado;
			mov.ValorTipoCambio					= info.TipoCambio;
			mov.CodigoIFRS						= info.CodigoIFRS;
			
			if("S".equals(paramLiquidacionComisionFact)) {
				mov.IdTipoLiquidacionComisionFactura = info.IdTipoLiquidacionComisionFactura;
				mov.ModPrefFact = info.ModPrefFact;
			}
			else {
				mov.IdTipoLiquidacionComisionFactura = "";
				mov.ModPrefFact = "";
			}
			
			
			entradaCrearMovimientoComprobante.p_Movimientos.add(mov);
			
			//COBRO ALL INCLUSIVE (COAI) LO CAMBIAMOS POR UN COVA+IVA
			if (localizacionSistema.equalsIgnoreCase("CL")) {
				Cobro_Proceso cobroIva = null;
				for (Cobro_Proceso cobAsig : info.Cobros) {
					
					if (cobAsig.IdCobro.equalsIgnoreCase(_c.CODIGO_COBRO_ALL_INCLUSIVE)) {
						BigDecimal iva = BigDecimal.ONE.add(impuestoIva.ValorCobro.divide(new BigDecimal(100),20,RoundingMode.HALF_UP));
						BigDecimal montoIva = BigDecimal.ZERO;
						BigDecimal montoIvaLiquidacion = BigDecimal.ZERO;
						BigDecimal montoIvaLocal = BigDecimal.ZERO;
						cobAsig.MontoCobro = cobAsig.MontoCobro.divide(iva,20,RoundingMode.HALF_UP);
						cobAsig.MontoLiquidacionCobro = cobAsig.MontoLiquidacionCobro.divide(iva,20,RoundingMode.HALF_UP);//cobAsig.MontoLiquidacionCobro.subtract(montoIvaLiquidacion);
						cobAsig.MontoMovimientoComprobanteCobro = cobAsig.MontoMovimientoComprobanteCobro.divide(iva,20,RoundingMode.HALF_UP);//cobAsig.MontoMovimientoComprobanteCobro.subtract(montoIvaLocal);
						
						montoIva 			= cobAsig.MontoCobro.multiply(impuestoIva.ValorCobro.divide(new BigDecimal(100),20,RoundingMode.HALF_UP));
						montoIvaLiquidacion = cobAsig.MontoLiquidacionCobro.multiply(impuestoIva.ValorCobro.divide(new BigDecimal(100),20,RoundingMode.HALF_UP));
						montoIvaLocal 		= cobAsig.MontoMovimientoComprobanteCobro.multiply(impuestoIva.ValorCobro.divide(new BigDecimal(100),20,RoundingMode.HALF_UP));
						montoIva = MonedaFormato.formatear(conn, montoIva, MonedaFormato.MONTO, cobAsig.IdMonedaCobro).valor;
						montoIvaLiquidacion = MonedaFormato.formatear(conn, montoIvaLiquidacion, MonedaFormato.MONTO, cobAsig.IdMonedaCobro).valor;
						montoIvaLocal = MonedaFormato.formatear(conn, montoIvaLocal, MonedaFormato.MONTO, cobAsig.IdMonedaCobro).valor;
						
						
						//ACTUALIZAMOS EL IVA, SI ESTA, SUMAMOS EL VALOR, SI NO, TENEMOS QUE AGREGAR LA LINEA
						boolean found = false;
						for (Cobro_Proceso cobAsigIva : info.Cobros) {
							if (cobAsigIva.IdCobro.equalsIgnoreCase(_c.CODIGO_COBRO_IVA)) {
								cobAsigIva.MontoCobro = cobAsigIva.MontoCobro.add(montoIva);
								cobAsigIva.MontoLiquidacionCobro = cobAsigIva.MontoLiquidacionCobro.add(montoIvaLiquidacion);
								cobAsigIva.MontoMovimientoComprobanteCobro = cobAsigIva.MontoMovimientoComprobanteCobro.add(montoIvaLocal);
								found = true;
								break;
							}
						}
						
						if (found) {
							break;	
						}
						else {
							cobroIva = new Cobro_Proceso();
							cobroIva.IdAsignacion = cobAsig.IdAsignacion;
							cobroIva.IdCobro = _c.CODIGO_COBRO_IVA;
							cobroIva.IdMonedaCobro = impuestoIva.IdMonedaCobro;
							cobroIva.MontoCobro = montoIva;
							cobroIva.MontoLiquidacionCobro = montoIvaLiquidacion;
							cobroIva.MontoMovimientoComprobanteCobro = montoIvaLocal;
							cobroIva.Porcentaje = impuestoIva.ValorCobro;
						}
					}
				}
				
				if (cobroIva != null) {
					info.Cobros.add(cobroIva);
				}
				
			}
			
			
			BigDecimal totalCobros = BigDecimal.ZERO;
			BigDecimal totalCobrosMonedaLocal = BigDecimal.ZERO;
			for (Cobro_Proceso cobAsig : info.Cobros) {
				
				CrearMovimientoComprobante.MovimientoComprobanteCobro_Entrada cob = new CrearMovimientoComprobante.MovimientoComprobanteCobro_Entrada();
				cob.IdCobro = cobAsig.IdCobro;
				cob.IdMonedaCobro = cobAsig.IdMonedaCobro;
				cob.IdMonedaLiquidacionCobro = info.IdMonedaLiquidacionOrden;
				cob.MontoCobro = cobAsig.MontoCobro;
				cob.MontoLiquidacionCobro = cobAsig.MontoLiquidacionCobro;
				cob.MontoMovimientoComprobanteCobro = cobAsig.MontoMovimientoComprobanteCobro;
				cob.NumeroMvtoCmbte = cobAsig.IdAsignacion;
				cob.NumeroMvtoCmbteCobro = indexCobro++;
				cob.Porcentaje = cobAsig.Porcentaje;
				totalCobros = totalCobros.add(cobAsig.MontoLiquidacionCobro);
				totalCobrosMonedaLocal = totalCobrosMonedaLocal.add(cobAsig.MontoMovimientoComprobanteCobro);
				entradaCrearMovimientoComprobante.p_MovimientosCobros.add(cob);
			}	
			if (info.formaPago != null) {
				CrearMovimientoComprobante.MovimientoComprobanteFormaDePago_Entrada forma = new CrearMovimientoComprobante.MovimientoComprobanteFormaDePago_Entrada();
				forma.FechaDocumentoPago = "0001-01-01";
				forma.FechaVctoTarjetaBancaria = "0001-01-01";
				forma.NumeroDocumento = 0L;
				forma.NumeroMvtoCmbte = mov.NumeroMvtoCmbte;
				forma.NumeroMvtoCmbteFormaDePago = indexFormaDePago++;
				forma.NumeroSerie = "";
				forma.IdBanco = info.formaPago.IdBanco;
				forma.IdCuentaBancaria = info.formaPago.IdCuentaBancaria;
				forma.IdMedioDePago = info.formaPago.IdMedioDePago;
				forma.IdModalidadDePago = info.formaPago.IdModalidadDePago;
				forma.IdMoneda = info.IdMonedaLiquidacionOrden;
				forma.IdTarjetaBancaria = info.formaPago.IdTarjetaBancaria;
				forma.IdTipoCuentaBancaria = info.formaPago.IdTipoCuentaBancaria;
				forma.NumeroCuentaBancaria = info.formaPago.NumeroCuentaBancaria;
				forma.IdTipoTarjetaBancaria = info.formaPago.IdTipoTarjetaBancaria;
				forma.NumeroTarjetaBancaria = info.formaPago.NumeroTarjetaBancaria;
				
				if ("N".equalsIgnoreCase(cobroMXsobrePagoMX) && !mov.IdMonedaLiquidacion.equalsIgnoreCase("CLP") && productosSinCobroMX.stream().anyMatch(o -> o.equals(mov.IdTipoProducto)) && new BigDecimal("0").compareTo(totalCobrosMonedaLocal) == -1) {
					forma.MontoLiquidacionFormaDePago = info.MontoAsignadoLiquidacion;
					CrearMovimientoComprobante.MovimientoComprobanteFormaDePago_Entrada fpMonedaLocal = new CrearMovimientoComprobante.MovimientoComprobanteFormaDePago_Entrada();
					fpMonedaLocal.FechaDocumentoPago = "0001-01-01";
					fpMonedaLocal.FechaVctoTarjetaBancaria = "0001-01-01";
					fpMonedaLocal.NumeroDocumento = 0L;
					fpMonedaLocal.NumeroMvtoCmbte = mov.NumeroMvtoCmbte;
					fpMonedaLocal.NumeroMvtoCmbteFormaDePago = indexFormaDePago++;
					fpMonedaLocal.NumeroSerie = "";
					fpMonedaLocal.IdBanco = info.formaPago.IdBanco;
					fpMonedaLocal.IdCuentaBancaria = info.formaPago.IdCuentaBancaria;
					fpMonedaLocal.IdMedioDePago = info.formaPago.IdMedioDePago;
					fpMonedaLocal.IdModalidadDePago = info.formaPago.IdModalidadDePago;
					fpMonedaLocal.IdMoneda = "CLP";
					fpMonedaLocal.IdTarjetaBancaria = info.formaPago.IdTarjetaBancaria;
					fpMonedaLocal.IdTipoCuentaBancaria = info.formaPago.IdTipoCuentaBancaria;
					fpMonedaLocal.NumeroCuentaBancaria = info.formaPago.NumeroCuentaBancaria;
					fpMonedaLocal.IdTipoTarjetaBancaria = info.formaPago.IdTipoTarjetaBancaria;
					fpMonedaLocal.NumeroTarjetaBancaria = info.formaPago.NumeroTarjetaBancaria;
					fpMonedaLocal.MontoLiquidacionFormaDePago = totalCobrosMonedaLocal;
					if (!info.formaPago.IdMedioDePago.equalsIgnoreCase(_c.MEDIO_PAGO_CUENTA_INVERSION) && (!info.GeneraSpot.equalsIgnoreCase("S"))) {
						fpMonedaLocal.AfectarTesoreria = mov.ActualizarTesoreria;
						entradaCrearMovimientoComprobante.p_MovimientosFormaDePago.add(fpMonedaLocal);
					} else {
						for (MovimientoComprobante_Entrada mvto : entradaCrearMovimientoComprobante.p_Movimientos) {					
							if ( mvto.IdTipoComprobante.equalsIgnoreCase("POL")) {
								fpMonedaLocal.AfectarTesoreria = mov.ActualizarCuentaInversion;
								entradaCrearMovimientoComprobante.p_MovimientosFormaDePago.add(fpMonedaLocal);
							}
						}
						if ( !mov.IdTipoComprobante.equalsIgnoreCase("POL")) {
							fpMonedaLocal.AfectarTesoreria = mov.ActualizarCuentaInversion;
							entradaCrearMovimientoComprobante.p_MovimientosFormaDePago.add(fpMonedaLocal);
						}
					}
				} else {
					if (info.AccionOrden.equalsIgnoreCase(_c.ACCION_COMPRA)) {
						forma.MontoLiquidacionFormaDePago = info.MontoAsignadoLiquidacion.add(totalCobros);
					}
					else if (info.AccionOrden.equalsIgnoreCase(_c.ACCION_VENTA)) { 
						forma.MontoLiquidacionFormaDePago = info.MontoAsignadoLiquidacion.subtract(totalCobros);
					}
				}
				if (!info.formaPago.IdMedioDePago.equalsIgnoreCase(_c.MEDIO_PAGO_CUENTA_INVERSION) && (!info.GeneraSpot.equalsIgnoreCase("S"))) {
						forma.AfectarTesoreria = mov.ActualizarTesoreria;
						entradaCrearMovimientoComprobante.p_MovimientosFormaDePago.add(forma);
				} else {
					for (MovimientoComprobante_Entrada mvto : entradaCrearMovimientoComprobante.p_Movimientos) {					
						if ( mvto.IdTipoComprobante.equalsIgnoreCase("POL")) {
							forma.AfectarTesoreria = mov.ActualizarCuentaInversion;
							entradaCrearMovimientoComprobante.p_MovimientosFormaDePago.add(forma);
						}
					}
					if ( !mov.IdTipoComprobante.equalsIgnoreCase("POL")) {
						forma.AfectarTesoreria = mov.ActualizarCuentaInversion;
						entradaCrearMovimientoComprobante.p_MovimientosFormaDePago.add(forma);
					}					
				}
			}
			
		}		
		
		//REVALIDACION CONDICION DE LIQUIDACION
		ObtenerPlazo serviceObtenerPlazo = new ObtenerPlazo();
		InObtenerPlazo entradaObtenerPlazo = new InObtenerPlazo();
		entradaObtenerPlazo.p_IdPais = empresa.IdPais;
		for (MovimientoComprobante_Entrada mvto : entradaCrearMovimientoComprobante.p_Movimientos) {
			
			ObtenerPlazo.FechasIn f1 = new ObtenerPlazo.FechasIn();
			f1.IdInterno = mvto.NumeroMvtoCmbte;
			f1.FechaDesde = mvto.FechaMovimientoComprobante;
			f1.FechaHasta = mvto.FechaLiquidacion;
			f1.SoloDiasHabiles = "S";
			entradaObtenerPlazo.p_Fechas.add(f1);
		}
		
		if (entradaObtenerPlazo.p_Fechas.size() > 0) {
			OutObtenerPlazo salidaObtenerPlazo = serviceObtenerPlazo.logica(conn, entradaObtenerPlazo);
			
			for (MovimientoComprobante_Entrada mvto : entradaCrearMovimientoComprobante.p_Movimientos) {
				for (ObtenerPlazo.FechasOut f2 :  salidaObtenerPlazo.p_out_Fechas) {
					if (mvto.NumeroMvtoCmbte.equals(f2.IdInterno)) {
						
						//aqui tenemos el nuevo plazo de liquidacion de la operacion
						
						//buscamos la cond de liq que le corresponde
						for (CondicionDeLiquidacion liq : condLiquidacion) {
							if (new Long(liq.PlazoEnDias).equals(f2.Plazo) && !"TP".equalsIgnoreCase(mvto.IdCondicionLiquidacion)){								
								  mvto.IdCondicionLiquidacion = liq.IdCondicionDeLiquidacion;							  
							}
						}
					}
				}
			}
		}
		
		CrearMovimientoComprobante serviceCrearMovimientoComprobante = new CrearMovimientoComprobante();
		CrearMovimientoComprobante_Salida salidaCrearMovimientoComprobante = serviceCrearMovimientoComprobante.logica(conn,entradaCrearMovimientoComprobante);
		for (MensajeSalida msj : salidaCrearMovimientoComprobante.P_OUT_MENSAJES) {
			if (msj.tipo.equalsIgnoreCase(MensajeSalida.MENSAJE_INFORMACION)) 
				continue; //LOS MENSAJES DE INFORMACION VIENEN SIN CODIGO, NO SIRVEN POR AHORA
			Asignacion_Proceso asig = hashAsignaciones.get(msj.id);
			if (msj.tipo.equalsIgnoreCase(MensajeSalida.MENSAJE_ERROR))
				asig.errores++;
			out.P_OUT_MENSAJES.add(msj);
		}
		Map<Long, Asignacion_Proceso> asignacionProcesoPorIdMovimientoComprobante = new HashMap<>(hashAsignaciones.size());
		for (MovimientoComprobante_Salida movto : salidaCrearMovimientoComprobante.p_out_Movimientos) {
			Asignacion_Proceso asig = hashAsignaciones.get(movto.NumeroMvtoCmbte);
			asig.IdMovimientoComprobante = movto.IdMovimientoComprobante;
			asignacionProcesoPorIdMovimientoComprobante.put(asig.IdMovimientoComprobante, asig);
		}
		for (CrearMovimientoComprobante.Comprobante_Salida comprobanteSalida : salidaCrearMovimientoComprobante.p_out_Comprobantes) {
				Asignacion_Proceso asignacionProceso = asignacionProcesoPorIdMovimientoComprobante.get(comprobanteSalida.IdMovimientoComprobante);
				asignacionProceso.IdComprobante = comprobanteSalida.IdComprobante;
		}
		if(listaAsignaciones.stream()
	          .anyMatch(list -> list.getGeneraSpot().equals("S"))){
			ProcesarIngresoOperacionSpot procesarSpot = new  ProcesarIngresoOperacionSpot();
			EntradaProcesarIngresoOperacionSpot entradaprocesarSpot= new EntradaProcesarIngresoOperacionSpot();

			String monedaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
			BigDecimal dolarObservado = procesarSpot.obtenerDolarObservado(conn, in.p_IdEmpresa, fechaSistema, monedaSistema);
			for (Asignacion_Proceso asig : listaAsignaciones) {
				if (asig.IdMovimientoComprobante != null && asig.IdMovimientoComprobante > 0 && asig.errores == 0 && asig.GeneraSpot.equalsIgnoreCase("S")) {
					BigDecimal totalCobrosSpot = BigDecimal.ZERO;
					for (Cobro_Proceso cobAsig : asig.Cobros) {
						totalCobrosSpot = totalCobrosSpot.add(cobAsig.MontoLiquidacionCobro);
					}
					AsignacionSpot asignacionSpot = new AsignacionSpot();
					FormaPagoSpot formaPagoSpotCliente = new FormaPagoSpot();
					FormaPagoSpot formaPagoSpotCorredora = new FormaPagoSpot();
					BigDecimal totalMontoFinalLiquidacion = totalCobrosSpot.add(asig.MontoAsignadoLiquidacion);
					asignacionSpot.IdMonedaLiquidacion = asig.IdMonedaLiquidacionOrden;
					asignacionSpot.IdCliente = asig.IdCliente;
					asignacionSpot.IdPortafolio = asig.IdPortafolio;
					asignacionSpot.FechaMovimientoAsignacion = Fecha.toDate(fechaSistema); 
					asignacionSpot.MontoAsignadoLiquidacion = totalMontoFinalLiquidacion;
					asignacionSpot.Glosa = asig.GlosaOrden;
					asignacionSpot.IdAsignacion = asig.IdAsignacion;
					entradaprocesarSpot.p_DatosOperacion.add(asignacionSpot);
					formaPagoSpotCliente.IdComprobantePagoOperacion = 0L;
					formaPagoSpotCliente.IdTipoMovimiento = _c.SpotTipoMovimientoIngreso;
					
			        switch (asig.IdCondicionDeLiquidacion.toUpperCase()) {
		            case "T0":
		            	formaPagoSpotCliente.IdFormaDePago = _c.SpotformaDePagoRecibimosCompra_0;
						formaPagoSpotCorredora.IdFormaDePago = _c.SpotformaDePagoEntregamosVenta_0;
		                break;
		            case "T1":
		            	formaPagoSpotCliente.IdFormaDePago = _c.SpotformaDePagoRecibimosCompra_1;
		            	formaPagoSpotCorredora.IdFormaDePago = _c.SpotformaDePagoEntregamosVenta_1;
		                break;
		            case "T2":
		            	formaPagoSpotCliente.IdFormaDePago = _c.SpotformaDePagoRecibimosCompra_2;
		            	formaPagoSpotCorredora.IdFormaDePago = _c.SpotformaDePagoEntregamosVenta_2;
		                break;				                
			        }					
					
					formaPagoSpotCliente.IdOperacion = asig.IdAsignacion;
					formaPagoSpotCliente.IdMoneda = "CLP";
					formaPagoSpotCliente.IdMonedaLiquidacion = "CLP";
					formaPagoSpotCliente.RecibePagaPortafolio = "R";
					formaPagoSpotCliente.MontoFormaDePago = totalMontoFinalLiquidacion.multiply(dolarObservado);
					formaPagoSpotCliente.AfectarTesoreria = "N";
					if(!asig.formaPago.IdMedioDePago.equalsIgnoreCase(_c.MEDIO_PAGO_CUENTA_INVERSION)) {
						formaPagoSpotCliente.IdFormaDePago = formaPagoSpotCliente.IdFormaDePago.replace("CIN", asig.formaPago.IdMedioDePago);
						formaPagoSpotCliente.AfectarTesoreria = "S";
					}
					entradaprocesarSpot.p_DatosFormaPago.add(formaPagoSpotCliente);
					formaPagoSpotCorredora.IdComprobantePagoOperacion = 0L;
					formaPagoSpotCorredora.IdTipoMovimiento = _c.SpotTipoMovimientoEgreso;
					formaPagoSpotCorredora.IdOperacion = asig.IdAsignacion;
					formaPagoSpotCorredora.IdMoneda = _c.Dolar;
					formaPagoSpotCorredora.IdMonedaLiquidacion = _c.Dolar;
					formaPagoSpotCorredora.RecibePagaPortafolio = "P";
					formaPagoSpotCorredora.MontoFormaDePago = totalMontoFinalLiquidacion;
					formaPagoSpotCorredora.AfectarTesoreria = "N";
					entradaprocesarSpot.p_DatosFormaPago.add(formaPagoSpotCorredora);	
				}
			}
			entradaprocesarSpot.p_IdEmpresa = in.p_IdEmpresa;
			entradaprocesarSpot.p_IdUsuario = in.p_IdUsuario;
			entradaprocesarSpot.p_IdModulo = in.p_IdModulo;
			entradaprocesarSpot.p_CalculaMargen = _c.SpotCalculaMargen;
			entradaprocesarSpot.p_UsaReglasNegocio = "N";
			entradaprocesarSpot.p_PermiteOperarProspectos = "N";
			SalidaProcesarIngresoOperacionSpot salidaSpot = procesarSpot.logica(conn, entradaprocesarSpot);
			for(OperacionObjOut rs : salidaSpot.P_OUT_IDOPERACIONSPOT){
				for(Asignacion_Proceso asig : listaAsignaciones){
					if((Long.parseLong(rs.IdInterno) == asig.IdAsignacion) ){
						if(_c.SpotEstadoRechazado.equalsIgnoreCase(rs.Estado)){			
							asig.errores++;
							break;
						}else{
							asig.IdOperacionSpot = rs.IdOperacionSpot;
							break;
						}
					}
				}
			}
			for(MensajeSalida rs : salidaSpot.P_OUT_MENSAJES){
				if(_c.SpotError.equalsIgnoreCase(rs.tipo)){
					out.P_OUT_ERRORESCONTADOR += 1L;
					out.P_OUT_MENSAJES.add(new MensajeSalida(rs.id, rs.tipo, rs.codigo,  _c.ModuloSpot + " : " + rs.descripcion));			
				}
			}
		}
		CambiarEstadoAsignaciones_Entrada entradaCambiarEstadoAsignaciones = new CambiarEstadoAsignaciones_Entrada();
		for (Asignacion_Proceso asig : listaAsignaciones) {
			if (asig.IdMovimientoComprobante != null && asig.IdMovimientoComprobante > 0 && asig.errores == 0) {
				CambiarEstadoAsignaciones.Asignacion_Entrada asigCambioEstado = new CambiarEstadoAsignaciones.Asignacion_Entrada();
				asigCambioEstado.IdAsignacion = asig.IdAsignacion;
				asigCambioEstado.IdEstadoAsignacion = _c.ESTADO_ASIGNACION_ENVIADA_FACTURAR;
				entradaCambiarEstadoAsignaciones.p_IdEmpresa = in.p_IdEmpresa;
				entradaCambiarEstadoAsignaciones.p_Asignaciones.add(asigCambioEstado);
			}
		}
		
		if (entradaCambiarEstadoAsignaciones.p_Asignaciones.size() > 0) {
			List<Asignacion_Proceso> asignacionesUpdate = new ArrayList<>();
			CambiarEstadoAsignaciones serviceCambiarEstadoAsignaciones = new CambiarEstadoAsignaciones();
			CambiarEstadoAsignaciones_Salida salidaCambiarEstadoAsignaciones = serviceCambiarEstadoAsignaciones.logica(conn, entradaCambiarEstadoAsignaciones);
			for (MensajeSalida msj : salidaCambiarEstadoAsignaciones.P_OUT_MENSAJES) {
				if (!msj.tipo.equalsIgnoreCase(MensajeSalida.MENSAJE_OK)) {
					out.P_OUT_MENSAJES.add(msj);
					if (msj.tipo.equalsIgnoreCase(MensajeSalida.MENSAJE_ERROR)) {
						hashAsignaciones.get(msj.id).errores++;
					}
				} else {
					asignacionesUpdate.add(hashAsignaciones.get(msj.id));
				}
			}
			
			if (asignacionesUpdate.size() > 0) {
				_sql.batch(conn, SQL_UPDATE_ASIGNACIONES, asignacionesUpdate, Asignacion_Proceso.class);
			}
		}
		for (Asignacion_Proceso asig : listaAsignaciones) {
			if (asig.errores == 0) {
				
				//si es operacion contado, le agregamos mensaje de operacion asociado
				if ((asig.IdTipoInstrumento.equalsIgnoreCase(_c.TIPO_INSTRUMENTO_SIM) || asig.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RE)) && asig.EsAPlazo.equalsIgnoreCase(_c.NO)) {
					Asignacion_Proceso asigPlazo = hashAsignaciones.get(asig.IdAsignacionRef);
					String codigo = "ASIGNACION_MOVIMIENTO_CONTADO_GENERADO";
					String descripcion = Diccionario.getMensaje(conn, in.p_IdEmpresa,codigo)
							.add("IdAsignacion", asig.IdAsignacion)
							.add("IdMovimientoComprobante", asig.IdMovimientoComprobante)
							.msg();
					out.P_OUT_MENSAJES.add(new MensajeSalida(asig.IdAsignacion,MensajeSalida.MENSAJE_INFORMACION,codigo,descripcion));
					codigo = "ASIGNACION_MOVIMIENTO_PLAZO_GENERADO";
					descripcion = Diccionario.getMensaje(conn, in.p_IdEmpresa,codigo)
							.add("IdAsignacion", asig.IdAsignacion)
							.add("IdMovimientoComprobante", asigPlazo.IdMovimientoComprobante)
							.msg();
					out.P_OUT_MENSAJES.add(new MensajeSalida(asig.IdAsignacion,MensajeSalida.MENSAJE_INFORMACION,codigo,descripcion));
				}
				else if ((asig.IdTipoInstrumento.equalsIgnoreCase(_c.TIPO_INSTRUMENTO_SIM) || asig.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RE)) && asig.EsAPlazo.equalsIgnoreCase(_c.SI)) {
					//PARA LAS OPERACIONES PLAZO NO GENERAMOS MENSAJE
				}
				else {
					String codigo = "ASIGNACION_MOVIMIENTO_GENERADO";
					String descripcion = Diccionario.getMensaje(conn, in.p_IdEmpresa,codigo).add("IdAsignacion", asig.IdAsignacion).add("IdMovimientoComprobante", asig.IdMovimientoComprobante).msg();
					out.P_OUT_MENSAJES.add(new MensajeSalida(asig.IdAsignacion,MensajeSalida.MENSAJE_INFORMACION,codigo,descripcion));
				}
				if(asig.IdOperacionSpot>0) {
					String codigo = "ASIGNACION_SPOT_INGRESADA";
					String descripcion = Diccionario.getMensaje(conn, in.p_IdEmpresa,codigo).add("IdAsignacion", asig.IdAsignacion).add("IdOperacionSpot", asig.IdOperacionSpot).msg();
					out.P_OUT_MENSAJES.add(new MensajeSalida(asig.IdAsignacion,MensajeSalida.MENSAJE_INFORMACION,codigo,descripcion));
				}
				
			}
		}
		
		for (Asignacion_Proceso asig : listaAsignaciones) {
			Resultado res = new Resultado();
			res.IdTransaccion = asig.IdTransaccion;
			res.IdCliente = asig.IdCliente;
			res.IdPortafolio = asig.IdPortafolio;
			res.IdAsignacion = asig.IdAsignacion;
			res.IdComprobante = asig.IdComprobante;
			res.IdMovimientoComprobante = asig.IdMovimientoComprobante;
			res.EsAPlazo = asig.EsAPlazo;
			out.P_OUT_RESULTADO.add(res);
			
		}
		logger.info("FIN::EnviarAsignacionesFacturar");
		out.P_OUT_ERRORESCONTADOR += MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		return out;

	}
	
	private String obtieneParametroCobroMXsobrePagoMX(Connection conn, Long idEmpresa) {
		logger.debug("INICIO::EnviarAsignacionesFacturar.obtieneParametroCobroMXsobrePagoMX");
		long inicio = System.currentTimeMillis();
		String cobroMXsobrePagoMX = "S";
		try {
			cobroMXsobrePagoMX = ConfiguracionSistema.getParametroConfiguracionString(conn, idEmpresa, "FACTURACION", "CobroMXsobrePagoMX");
		}catch(Exception e) {
			logger.error("ADVERTENCIA::EnviarAsignacionesFacturar.obtieneParametroCobroMXsobrePagoMX::Parametro CobroMXsobrePagoMX no existe.");
		}

		long fin = System.currentTimeMillis();
		logger.debug("FIN::EnviarAsignacionesFacturar.obtieneParametroCobroMXsobrePagoMX", fin-inicio);

		return cobroMXsobrePagoMX;
	}
	
	private String obtieneParametroProductosSinCobroMXsobrePagoMX(Connection conn, Long idEmpresa) {
		logger.debug("INICIO::EnviarAsignacionesFacturar.obtieneParametroProductosSinCobroMXsobrePagoMX");
		long inicio = System.currentTimeMillis();
		String productosSinCobroMXsobrePagoMX = "";
		try {
			productosSinCobroMXsobrePagoMX = ConfiguracionSistema.getParametroConfiguracionString(conn, idEmpresa, "FACTURACION", "ProductosSinCobroMXsobrePagoMX");
		}catch(Exception e) {
			logger.error("ADVERTENCIA::EnviarAsignacionesFacturar.obtieneParametroProductosSinCobroMXsobrePagoMX::Parametro ProductosSinCobroMXsobrePagoMX no existe.");
		}

		long fin = System.currentTimeMillis();
		logger.debug("FIN::EnviarAsignacionesFacturar.obtieneParametroProductosSinCobroMXsobrePagoMX", fin-inicio);

		return productosSinCobroMXsobrePagoMX;
	}

	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdModulo\":\"ORD\",\"p_Asignaciones\":[{\"IdAsignacion\":661844,\"GeneraComprobante\":\"N\"}]}";
			EnviarAsignacionesFacturar_Entrada Objin = gson.fromJson(json, EnviarAsignacionesFacturar_Entrada.class);
			
			EnviarAsignacionesFacturar demo=new EnviarAsignacionesFacturar();				
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			
 			try {
 				EnviarAsignacionesFacturar_Salida ObjOut = demo.logica(con, Objin);
 				System.out.println(ObjOut.toString());
 				//con.commit();
 				con.rollback();
 			}
 			catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}
 			//con.commit();
 			con.rollback();
	    	}
		 	
		System.out.println("== END ==");
	}

}
