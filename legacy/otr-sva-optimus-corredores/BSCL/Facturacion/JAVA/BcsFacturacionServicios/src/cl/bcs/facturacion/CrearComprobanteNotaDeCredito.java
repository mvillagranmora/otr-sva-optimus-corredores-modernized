package cl.bcs.facturacion;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.cuentaInversion.AnularCargoAbono;
import cl.bcs.cuentaInversion.AnularCargoAbono.AnularCargoAbono_Entrada;
import cl.bcs.cuentaInversion.AnularCargoAbono.AnularCargoAbono_Salida;
import cl.bcs.cuentaInversion.AnularCargoAbono.CargoAbono_Entrada;
import cl.bcs.custodia.EnviarMovimientoFacturarSuscripcionOSA_Masivo.UpdateEventoCapitalCliente;
import cl.bcs.facturacion.AnularComprobanteFinal.CargoAbono_Proceso;
import cl.bcs.facturacion.AnularComprobanteFinal.Comprobante_Proceso;
import cl.bcs.facturacion.AnularComprobanteFinal.OperacionSpot;
import cl.bcs.facturacion.CrearComprobante.Comprobante_Salida;
import cl.bcs.facturacion.CrearComprobante.ResultadoProceso_Salida;
import cl.bcs.facturacion.CrearMovimientoComprobante.MovimientoComprobanteCobro_Entrada;
import cl.bcs.facturacion.CrearMovimientoComprobante.MovimientoComprobanteFormaDePago_Entrada;
import cl.bcs.facturacion.CrearMovimientoComprobante.MovimientoComprobante_Entrada;
import cl.bcs.facturacion.CrearMovimientoComprobante.MovimientoComprobante_Salida;
import cl.bcs.facturacion.pojo.Comprobante;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente.FechasOut;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.tesoreria.AnularRegistros;
import cl.bcs.tesoreria.AnularRegistros.AnularRegistros_Entrada;
import cl.bcs.tesoreria.AnularRegistros.AnularRegistros_Salida;
import cl.bcs.tesoreria.AnularRegistros.Registro_Entrada;
import cl.bcs.utils.JavaCall;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class CrearComprobanteNotaDeCredito implements JavaCall {

	private final String SQL_SELECT_COMPROBANTESNOTADECREDITO = loadResourceToString("cl/bcs/facturacion/sql/CrearComprobanteNotaDeCredito_SelectMovimientos.sql");
	private final String SQL_SELECT_COMPROBANTESNOTADECREDITOFORMASDEPAGO = loadResourceToString("cl/bcs/facturacion/sql/CrearComprobanteNotaDeCredito_SelectMovimientosFormasDePago.sql");
	private final String SQL_SELECT_COMPROBANTESNOTADECREDITOCOBROS = loadResourceToString("cl/bcs/facturacion/sql/CrearComprobanteNotaDeCredito_SelectMovimientosCobros.sql");
	private final String SQL_SELECT_CUENTA_INVERSION = loadResourceToString("cl/bcs/facturacion/sql/AnularComprobanteFinal_SelectCuentaInversion.sql");

	private final String SQL_SELECT_COMPROBANTE_SPOT = loadResourceToString("cl/bcs/facturacion/sql/CrearComprobanteNotaDeCredito_SelectComprobanteSpot.sql");
	private final String SQL_SELECT_NOTA_CREDITO_REFERENCIA = loadResourceToString("cl/bcs/facturacion/sql/CrearComprobanteNotaDeCredito_SelectMovimientosComprobanteReferencia.sql");
	private final String SQL_UPDATE_ASIGNACIONES = loadResourceToString("cl/bcs/facturacion/sql/CrearComprobanteNotaDeCredito_UpdateAsignacion.sql");
	private final String SQL_SELECT_SPOT_ANULADO_FFMM	= loadResourceToString("cl/bcs/facturacion/sql/CrearComprobanteNotaDeCredito_SelectSpotFFMM.sql");
	
	private final String SQL_SELECT_CUSTODIA_CARTERAS_COMPRAS = loadResourceToString("cl/bcs/facturacion/sql/CrearComprobanteNotaDeCredito_SelectCarterasCompra.sql");
	
	private final String SQL_SELECT_LIQUIDACION_PENDIENTE_AUTORIZACION = loadResourceToString("cl/bcs/facturacion/sql/CrearComprobanteNotaDeCredito_SelectComprobanteLiquidacionPendienteDeAutorizacion.sql");

	private final String SQL_UPDATE_ESTADO_EVENTO_CAPITAL_CLIENTE = loadResourceToString("cl/bcs/facturacion/sql/AnularComprobanteFinal_UpdateEstadoEventoCapital.sql");

	private final String SQL_SELECT_INDICADORES_PORTAFOLIO = loadResourceToString("cl/bcs/facturacion/sql/CrearComprobanteNotaDeCredito_SelectIndicadoresPortafolio.sql");
	
	List<ComprobanteNotaDeCredito_Proceso> comprobantenotadecredito = new ArrayList<ComprobanteNotaDeCredito_Proceso>();
	List<ComprobanteNotaDeCredito_Proceso> comprobantenotadecreditoError = new ArrayList<ComprobanteNotaDeCredito_Proceso>();
	
	static final Logger logger = LogManager.getLogger(CrearComprobanteNotaDeCredito.class);
	
	//ENTRADAS
	public static class CrearComprobanteNotaDeCredito_Entrada {		
	  public Long		p_IdEmpresa;		
	  public Long		p_IdUsuario;   
	  public String		p_IdModulo;   
	  public String		p_simularOperacion;				// S/N se usa para simular operacion (algunos casos)
	  public String		p_UsaReglasNegocio;				// S/N se usa en caso de servicios anidados.
	  public String		p_EsIngresoAutomatico = "N";	// S/N si viene de} otro modulo es S para no validar requisitos basicos nuevamente
	  public String		p_CrearComprobanteAutomatico = "N";
	  public String		p_EsCargaMasiva = "N";
	  public List<Comprobante_Entrada>	P_COMPROBANTE;
	  
		
	  public CrearComprobanteNotaDeCredito_Entrada (){

		  P_COMPROBANTE	= new ArrayList<Comprobante_Entrada>();
	  }
	    
	  public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	
	public static class Comprobante_Entrada {
		public Long   IdPortafolio;
		public Long   IdCliente;
		public Long   IdComprobante;
		public String IdTipoComprobante;
		public Long IdCargaMasivaDetalle;
		
		public String toString() {
			return Utils.printJson(this, false);
		}		
	};
	
	
	//Validacion
	public static class cmbteNCR_Proceso {
		public Long IdComprobante;
		
		public String toString() {
			return Utils.printJson(this, false);
		}	
	}
	
	public static class Movimiento_Salida {
		public Long IdComprobanteReferencia;
		public Long IdMovimientoComprobante;
		public Long NumeroMvtoCmbte;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	// Indicadores portafolio
	public static class IndicadoresPortafolio{
		public Long   IdPortafolio;
		public Long   IdCliente;
		public Long   IdTipoIndicador;
		public String CodigoTipoIndicador = "";
		public String ValorIndicador = "";
		
		public String toString() {
			return Utils.printJson(this, false);
		 }
	}

	//Salidas
	public static class CrearComprobanteNotaDeCredito_Salida {
		public List<MensajeSalida>					P_OUT_MENSAJES;
		public Long									P_OUT_ERRORESCONTADOR;
		public List<Comprobante_Salida>				P_OUT_COMPROBANTE;
		public List<ResultadoProceso_Salida>		P_OUT_RESULTADOPROCESO;
		public List<Movimiento_Salida>				P_OUT_MOVIMIENTO;
		
		public CrearComprobanteNotaDeCredito_Salida (){
			 P_OUT_MENSAJES	= new ArrayList<>();
			 P_OUT_COMPROBANTE = new ArrayList<>();
			 P_OUT_RESULTADOPROCESO = new ArrayList<>();
			 P_OUT_MOVIMIENTO = new ArrayList<>();
		  }
			
		public String toString() {
			return Utils.printJson(this, false);
		 }
	};
	
	//Proceso
	public static class ComprobanteNotaDeCredito_Proceso {
		public Long IdComprobante;
		public String IdTipoComprobante;
		public String IdEstadoComprobante;

		/* VALIDACIONES */
		public long 		errores = 0;
		public long 		advertencias = 0;
		public long 		autorizaciones = 0;
		public long 		ok = 0;

		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public static class Cartera_Proceso {
		public Long IdCliente;
		public Long IdPortafolio;
		public Long FolioComprobante;
		public String IdTipoComprobante;
		public String IdModulo;
		public Long FolioCartera;
		public String IdEstado;
		public String IdTipoOperacion;
		public Long IdCartera;
		public Long IdGarantia;
		public Long PlazoEnDiasOperacion;
		public String IdEmisor;
		public String IdTipoReajuste;
		public String IdMonedaEmision;
		public String IdConcepto;
		
		public String Nemotecnico;
		public BigDecimal Cantidad;
		public BigDecimal INGCantidad;
		public BigDecimal INGPrecioCompra;
		public BigDecimal EGRCantidad;
		public Long EGRFolioComprobante;
		public String TipoCartera;
		public Long IdInstrumento;
		public BigDecimal ValorRescate;
		public BigDecimal INGValorRescate;
		public BigDecimal EGRValorRescate;
		public String EsFungible;
		public Date INGFechaLiquidacion;
		
		public String IdOrigenGarantia;
		public Date FechaVencimientoInstrumento;
		

		public BigDecimal INGMontoATasaCompraActual = BigDecimal.ZERO;

		public String IdClasificacionTipoInstrumento = "";
		public String IdFormaCustodiaDetalleComprobante = "";
	    public String IdEstadoMovimientoTransito = "";
	    public BigDecimal CantidadMovimientoTransito = BigDecimal.ZERO;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = getGsonBuilder();
		CrearComprobanteNotaDeCredito_Entrada in = gson.fromJson(jin.toString(), CrearComprobanteNotaDeCredito_Entrada.class);
		CrearComprobanteNotaDeCredito_Salida salida = logica(con, in);
		salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(salida)).getAsJsonObject();
		return dataout;
	}	
	
	@Trace
	public CrearComprobanteNotaDeCredito_Salida logica(Connection conn, CrearComprobanteNotaDeCredito_Entrada in) throws Exception {
		CrearComprobanteNotaDeCredito_Salida salida = new CrearComprobanteNotaDeCredito_Salida();
		salida.P_OUT_ERRORESCONTADOR = 0L;
		SQL _sql = new SQL();

		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		String localizacionSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.LocalizacionSistema");
		
		//Validaciones
		if (in.P_COMPROBANTE.size() == 0) {
			String codigo = "NO_VIENEN_COMPROBANTES";
			String desc = Diccionario.getMensaje(conn, in.p_IdEmpresa, codigo).msg();
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,codigo,desc));
			return salida;
		}

		//Lectura de Informacion
		
		{
			List<MovimientoComprobante_Entrada> _mvtoComprobantesNC = null;
			List<MovimientoComprobanteFormaDePago_Entrada> _mvtoComprobantesNCFDePago = null;
			List<MovimientoComprobanteCobro_Entrada> _mvtoComprobantesNCCobros = null;
			
			ArrayList<Long> idsComprobante = new ArrayList<Long>();
						
			//Validacion de Previa nota de credito
			List<Long> cmbteNCR = new ArrayList<Long>();
			for (Comprobante_Entrada idCmbte : in.P_COMPROBANTE) {
				if (!cmbteNCR.contains(idCmbte.IdComprobante)) {
					cmbteNCR.add(idCmbte.IdComprobante);
				}
			}
				
			if (cmbteNCR.size() > 0 && localizacionSistema.equalsIgnoreCase("CL")) {
				HashMap<String,Object> paramsNCR = new HashMap<>();
				paramsNCR.put("idsComprobanteReferencia", cmbteNCR);
				List<cmbteNCR_Proceso> idscmbteRef = _sql.query(conn, SQL_SELECT_NOTA_CREDITO_REFERENCIA, paramsNCR, cmbteNCR_Proceso.class);
				List<Long> idscmbte = idscmbteRef.stream().map(id->id.IdComprobante).collect(Collectors.toList());
			  					
				for(Comprobante_Entrada registro: in.P_COMPROBANTE){
					if (!idscmbte.contains(registro.IdComprobante)) {
						idsComprobante.add(registro.IdComprobante);
					} else {
						String codigo = "CMBTE_TIPO_COMPROBANTE_NOTA_CREDITO";
						String descripcion = Diccionario.getMensaje(conn, in.p_IdEmpresa, codigo)
								.add("IdComprobante",registro.IdComprobante).msg();
						salida.P_OUT_MENSAJES.add(new MensajeSalida(registro.IdComprobante, MensajeSalida.MENSAJE_ERROR, codigo, descripcion));
					}
				}
			}

			if (idsComprobante.size() == 0) {
				return salida; 
		   	 };

			HashMap<String, Object> IdsComprobante = new HashMap<String, Object>();
			IdsComprobante.put("IdsComprobante",new ArrayList<>(idsComprobante));
			List<Comprobante> _comp = _sql.query(conn, SQL_SELECT_LIQUIDACION_PENDIENTE_AUTORIZACION, IdsComprobante , Comprobante.class);
			RechazarAutorizaciones(conn,_comp, in);

		   	//Obtiene pais corresponde a la empresa
		   	ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
			InformacionEmpresa empresa = consultarEmpresaService.logica(conn, in.p_IdEmpresa);
			Long idPaisEmpresa = empresa.IdPais;
			
			//Selecciona fechas a consultar
			List<FechasIn> fechas = new ArrayList<FechasIn>();
			FechasIn fechaSis = new FechasIn();
			fechaSis.fechaInicial = fechaSistema;
			fechaSis.numeroDias = 1L;
			fechas.add(fechaSis);
			
			//Obtiene dia habil siguiente a fecha de sistema,para calcular condicion de liquidacion
			InObtenerDiaSiguiente inDiaSiguiente = new InObtenerDiaSiguiente();
			inDiaSiguiente.p_diaHabil = "S";
			inDiaSiguiente.p_fechas = fechas;
			inDiaSiguiente.p_idPais = idPaisEmpresa;
			ObtenerDiaSiguiente o = new ObtenerDiaSiguiente();
			List<FechasOut> fechaVigencia = o.logica(conn,inDiaSiguiente).p_out_fechas;
			String FechaDiaHabilSig = fechaVigencia.get(0).fechaCalculada;
					
			
			HashMap<String,Object> params = new HashMap<String,Object>();
			params.put("idsComprobante", idsComprobante);
			params.put("idsfechaSistema", fechaSistema);
			params.put("idsCrearComprobanteAutomatico", in.p_CrearComprobanteAutomatico);
			params.put("idsfechaDiaHabilSiguiente", FechaDiaHabilSig);
										
			_mvtoComprobantesNC = _sql.query(conn, SQL_SELECT_COMPROBANTESNOTADECREDITO, params, MovimientoComprobante_Entrada.class);
			//verificacion de entradas contra comprobantes
			for (Long idComprobanteReferencia : idsComprobante) {
				boolean idComprobanteEncontrado = false;
				for (MovimientoComprobante_Entrada movimientoComprobanteEntrada : _mvtoComprobantesNC) {
					if (idComprobanteReferencia.equals(movimientoComprobanteEntrada.IdComprobanteReferencia)) {
						idComprobanteEncontrado = true;
					}
				}
				if (idComprobanteEncontrado == false) {
					String codigo = "CMBTE_ID_COMPROBANTE_REFERENCIA_NO_EXISTE";
					String descripcion = Diccionario.getMensaje(conn, in.p_IdEmpresa, codigo).add("IdComprobanteReferencia", idComprobanteReferencia).msg();
					salida.P_OUT_MENSAJES.add(new MensajeSalida(idComprobanteReferencia, MensajeSalida.MENSAJE_ERROR, codigo, descripcion));
				}
			}

			// Obtención de indicadores del portafolio
			List<Long> idPortafolios = in.P_COMPROBANTE.stream().map(comprobante -> comprobante.IdPortafolio).distinct().collect(Collectors.toList()); 
			HashMap<String,Object> paramsIndicadores = new HashMap<String,Object>();
			paramsIndicadores.put("idsPortafolios", idPortafolios);
			List<IndicadoresPortafolio> indicadoresPortafolio = _sql.query(conn, SQL_SELECT_INDICADORES_PORTAFOLIO, paramsIndicadores, IndicadoresPortafolio.class);
			Map<Long, List<IndicadoresPortafolio>> indicadoresPortafolioAgrupadosPorIdPortafolio = indicadoresPortafolio.stream().collect(Collectors.groupingBy(indicador -> indicador.IdPortafolio));
			
			params.clear();
			params.put("idsComprobante", new ArrayList<>(idsComprobante));
			List<Cartera_Proceso> carterasCompra = _sql.query(conn, SQL_SELECT_CUSTODIA_CARTERAS_COMPRAS, params, Cartera_Proceso.class);
			Map<Long, List<Cartera_Proceso>> carterasCompraAgrupadasPorIdComprobante = carterasCompra.stream().collect(Collectors.groupingBy(cartera->cartera.FolioComprobante));
			for (Comprobante_Entrada cmbte : in.P_COMPROBANTE) {
				List<Cartera_Proceso> carterasCompraComprobante = carterasCompraAgrupadasPorIdComprobante.get(cmbte.IdComprobante);
				if(carterasCompraComprobante != null) {
					for (Cartera_Proceso cartera : carterasCompraComprobante) {
						HashMap<String, String> parametrosMensaje = new HashMap<String, String>();
						parametrosMensaje.put("IdComprobante", String.valueOf(cartera.FolioComprobante));
						// Verificación condiciones de custodia
						Boolean requiereValidacionDeCustodia  = requiereValidacionDeCustodia(indicadoresPortafolioAgrupadosPorIdPortafolio, cartera);
						if(requiereValidacionDeCustodia && cartera.Cantidad.compareTo(cartera.INGCantidad) > 0) {
							salida.P_OUT_MENSAJES.add(new MensajeSalida(cmbte.IdComprobante, MensajeSalida.MENSAJE_ERROR, "NOTA_CREDITO_CARTERA_SALDO_INFUCIENTE", parametrosMensaje, conn, in.p_IdEmpresa));
						}
					}
				}
			}
			
			if (MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES) > 0) {
				
				salida.P_OUT_MENSAJES = salida.P_OUT_MENSAJES.stream().filter(t -> t.tipo.equalsIgnoreCase(MensajeSalida.MENSAJE_ERROR)).collect(Collectors.toList());
				salida.P_OUT_ERRORESCONTADOR += MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
				return salida;
			}
			
			
			
			//Valida CMBTE_SPOT_ANULADO
			HashMap<Long,Object> hashComprobantes = new HashMap<>();
			List<Comprobante_Proceso> comprobantes = new ArrayList<>();
			params = new HashMap<String,Object>();
			params.put("idsComprobante", new ArrayList<>(idsComprobante));
			List<Comprobante> _comprobantes = _sql.query(conn, SQL_SELECT_COMPROBANTE_SPOT, params , Comprobante.class);
			
			//Logica para no insertar idscomprobante repetidos
			Set<Long> idsComprobantesNoRepetidos = new HashSet<>();
			_comprobantes = _comprobantes.stream().filter(comp -> idsComprobantesNoRepetidos.add(comp.IdComprobante)).collect(Collectors.toList());
			
			hashComprobantes = Utils.List2Hash(_comprobantes, "IdComprobante");
			_comprobantes.forEach(_cmp -> idsComprobante.add(_cmp.IdComprobante));
			params.put("idsComprobante", new ArrayList<>(idsComprobante));
			
			for (Comprobante_Entrada cmbte : in.P_COMPROBANTE ) {
				Comprobante_Proceso c = new Comprobante_Proceso();
				Utils.copyPojo(cmbte, c);
				c.IdComprobante = cmbte.IdComprobante;
				comprobantes.add(c);
				}
			for (Comprobante_Proceso cmbte : comprobantes ) {
				Comprobante infoComprobante = (Comprobante) hashComprobantes.get(cmbte.IdComprobante);
				if(infoComprobante != null) {
					if("FMM".equalsIgnoreCase(infoComprobante.IdTipoCanalIngreso) && ("VENSPT".equalsIgnoreCase(infoComprobante.IdTipoOperacion) || "COMSPT".equalsIgnoreCase(infoComprobante.IdTipoOperacion))) {
						HashMap<String,Object> paramCmbte = new HashMap<>();
						paramCmbte.put("idComprobante",cmbte.IdComprobante.toString());
						List<OperacionSpot> IdOperacionSpot = _sql.query(conn, SQL_SELECT_SPOT_ANULADO_FFMM,paramCmbte, OperacionSpot.class);
						if(IdOperacionSpot.size() == 0)
							ProcesaRequisito(conn, in.p_IdEmpresa, infoComprobante.IdTipoProducto.equalsIgnoreCase("MEME") && in.p_EsIngresoAutomatico.equalsIgnoreCase("N"), "MVTO_CMBTE_SPOT_NO_ANULA", MensajeSalida.MENSAJE_ERROR, cmbte, salida.P_OUT_MENSAJES);
					}
				}
				
			}
						
			//Fin Valida CMBTE_SPOT_ANULADO
			
			salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
			if (salida.P_OUT_ERRORESCONTADOR > 0) {
				return salida;
			}
						
			//Forma de pago y cobro
			ArrayList<Long> idsMovimientoComprobanteOld = new ArrayList<Long>();
			
			List<MovimientoComprobante_Entrada> asignacionesActualizar = new ArrayList<>();
			MovimientoComprobante_Entrada asignacionActualizar;
		
			for (MovimientoComprobante_Entrada mvtoCompNC : _mvtoComprobantesNC) {
				
				asignacionActualizar = new MovimientoComprobante_Entrada();
				asignacionActualizar.IdAsignacion = mvtoCompNC.IdAsignacion;
				idsMovimientoComprobanteOld.add(mvtoCompNC.NumeroMvtoCmbte);
				asignacionesActualizar.add(asignacionActualizar);
			}
			
			HashMap<String,Object> paramsFormPago = new HashMap<String,Object>();
			paramsFormPago.put("idsMovimientoComprobanteOld", idsMovimientoComprobanteOld);			
			
			_mvtoComprobantesNCFDePago = _sql.query(conn, SQL_SELECT_COMPROBANTESNOTADECREDITOFORMASDEPAGO, paramsFormPago, MovimientoComprobanteFormaDePago_Entrada.class);
			
			paramsFormPago.put("idsComprobante", idsComprobante);
			_mvtoComprobantesNCCobros = _sql.query(conn, SQL_SELECT_COMPROBANTESNOTADECREDITOCOBROS, paramsFormPago, MovimientoComprobanteCobro_Entrada.class);
			
			CrearMovimientoComprobante.CrearMovimientoComprobante_Entrada  callCrearMovimientoComprobante = new CrearMovimientoComprobante.CrearMovimientoComprobante_Entrada();
			callCrearMovimientoComprobante.p_IdEmpresa = in.p_IdEmpresa;
			callCrearMovimientoComprobante.p_IdModulo = in.p_IdModulo;
			callCrearMovimientoComprobante.p_IdUsuario = in.p_IdUsuario;
			callCrearMovimientoComprobante.p_simularOperacion = "N";
			callCrearMovimientoComprobante.p_UsaReglasNegocio = "S";
			callCrearMovimientoComprobante.p_EsIngresoAutomatico = "N";
			callCrearMovimientoComprobante.p_Movimientos = _mvtoComprobantesNC;
			callCrearMovimientoComprobante.p_MovimientosFormaDePago = _mvtoComprobantesNCFDePago;			
			callCrearMovimientoComprobante.p_MovimientosCobros = _mvtoComprobantesNCCobros;
			callCrearMovimientoComprobante.p_EsCargaMasiva = in.p_EsCargaMasiva;
			
			CrearMovimientoComprobante mvtonc = new CrearMovimientoComprobante();
			CrearMovimientoComprobante.CrearMovimientoComprobante_Salida resultIngMvtoNC = mvtonc.logica(conn, callCrearMovimientoComprobante);
			salida.P_OUT_MOVIMIENTO.addAll(formatearMovimientos(resultIngMvtoNC.p_out_Movimientos, _mvtoComprobantesNC));
			salida.P_OUT_RESULTADOPROCESO.addAll(formatearResultadoProceso(resultIngMvtoNC.p_out_ResultadoProceso));
			salida.P_OUT_MENSAJES.addAll(resultIngMvtoNC.P_OUT_MENSAJES);
			
			if (asignacionesActualizar.size()>0){
				_sql.batch(conn, SQL_UPDATE_ASIGNACIONES, asignacionesActualizar, MovimientoComprobante_Entrada.class);	
			}
			
		}	
		
		if(salida.P_OUT_ERRORESCONTADOR == 0) {
			List<UpdateEventoCapitalCliente> upds = new ArrayList<UpdateEventoCapitalCliente>();
			UpdateEventoCapitalCliente upd = new UpdateEventoCapitalCliente();
			
			for(MensajeSalida msj : salida.P_OUT_MENSAJES) {			
				if(msj.codigo.equalsIgnoreCase("UI_COMPROBANTE_CREADO")) {
					upd = new UpdateEventoCapitalCliente();
					upd.IdEventoCapitalCliente = 0L;		
					upd.IdComprobanteFac = in.P_COMPROBANTE.get(0).IdComprobante;
					upds.add(upd);
					
					_sql.batch(conn, SQL_UPDATE_ESTADO_EVENTO_CAPITAL_CLIENTE, upds, UpdateEventoCapitalCliente.class);
				}
			}
		}
				
		return salida;				
	 };
	
	 private List<ResultadoProceso_Salida>  formatearResultadoProceso(List<cl.bcs.facturacion.CrearMovimientoComprobante.ResultadoProceso_Salida> p_out_ResultadoProceso) {
		 List<ResultadoProceso_Salida> salida = new ArrayList<>();
		 ResultadoProceso_Salida salidaFormateada;
		 for (cl.bcs.facturacion.CrearMovimientoComprobante.ResultadoProceso_Salida elemento : p_out_ResultadoProceso) {
			 salidaFormateada = new ResultadoProceso_Salida();
			 salidaFormateada.Campo = elemento.Campo;
			 salidaFormateada.Id = elemento.Id;
			 salidaFormateada.Servicio = elemento.Servicio;
			 salidaFormateada.Resultado = elemento.Resultado;
			 salida.add(salidaFormateada);
		 }
		 return salida;
	 }
	 
	 private List<Movimiento_Salida> formatearMovimientos(List<MovimientoComprobante_Salida> p_out_Movimientos, List<MovimientoComprobante_Entrada> _mvtoComprobantesNC) {
		 List<Movimiento_Salida> salida = new ArrayList<>();
		 Movimiento_Salida salidaFormateada;
		 for (MovimientoComprobante_Salida elemento : p_out_Movimientos) {
			 for (MovimientoComprobante_Entrada elementoEntrada : _mvtoComprobantesNC) {
				if (elementoEntrada.NumeroMvtoCmbte.equals(elemento.NumeroMvtoCmbte)) {
					salidaFormateada = new Movimiento_Salida();
					 salidaFormateada.IdComprobanteReferencia = elementoEntrada.IdComprobanteReferencia;
					 salidaFormateada.IdMovimientoComprobante = elemento.IdMovimientoComprobante;
					 salidaFormateada.NumeroMvtoCmbte = elemento.NumeroMvtoCmbte;
					 salida.add(salidaFormateada);
				}
			 }
		 }
		 return salida;
	 }
	 
	 private void ProcesaRequisito(Connection conn,Long idEmpresa,boolean esError,String codMsg,String codRequisito,Comprobante_Proceso cmbte,List<MensajeSalida> P_OUT_MENSAJES) throws Exception {
			if (esError) {
				String msg = Diccionario.getMensaje(conn, idEmpresa,codMsg)
						.add("IdComprobante", cmbte.IdComprobante)
						.msg();
				
				if (codRequisito.equals(MensajeSalida.MENSAJE_ERROR)) {
					P_OUT_MENSAJES.add(new MensajeSalida(cmbte.IdComprobante,MensajeSalida.MENSAJE_ERROR,codMsg,msg));
					cmbte.errores++;
				}else if (codRequisito.equals(MensajeSalida.MENSAJE_AUTORIZACION)) {
					P_OUT_MENSAJES.add(new MensajeSalida(cmbte.IdComprobante,MensajeSalida.MENSAJE_ERROR,codMsg,msg));
					cmbte.errores++;						
				}else if (codRequisito.equals(MensajeSalida.MENSAJE_AUTORIZACION)) {
					P_OUT_MENSAJES.add(new MensajeSalida(cmbte.IdComprobante,MensajeSalida.MENSAJE_AUTORIZACION,codMsg,msg));
					cmbte.autorizaciones++;						
				}
				else if (codRequisito.equals(MensajeSalida.MENSAJE_ADVERTENCIA)) {
					P_OUT_MENSAJES.add(new MensajeSalida(cmbte.IdComprobante,MensajeSalida.MENSAJE_ADVERTENCIA,codMsg,msg));
					cmbte.advertencias++;
				}
				else {
					cmbte.ok++;
				}
			}
			else {
				cmbte.ok++;
			}
	}

	private AnularRegistros_Salida RechazarAutorizaciones(Connection conn,List<Comprobante> ids, CrearComprobanteNotaDeCredito_Entrada in){
		AnularRegistros_Entrada entrada = new AnularRegistros_Entrada();
		entrada.p_IdEmpresa = in.p_IdEmpresa;
		entrada.p_IdModulo = "AUT";
		entrada.p_IdUsuario = in.p_IdUsuario;
		entrada.p_UsaReglasNegocio = "N";
		
		List<Registro_Entrada> registros = new ArrayList<>();
		ids.forEach(e -> {
			Registro_Entrada registro = new Registro_Entrada();
			registro.Id = e.IdComprobante;
			registro.Entidad = "TES.Comprobante";
			registros.add(registro);
		});
		entrada.p_RegistrosAProcesar = registros;
		
		AnularRegistros anular = new AnularRegistros();
		AnularRegistros_Salida salida = new AnularRegistros_Salida();
		try {
			salida = anular.logica(conn,entrada);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return salida;
	}

	private Boolean requiereValidacionDeCustodia(Map<Long, List<IndicadoresPortafolio>> indicadoresPortafolio, Cartera_Proceso cartera) {
		List<IndicadoresPortafolio> indicadoresPortafolioComprobante = indicadoresPortafolio.get(cartera.IdPortafolio);
		if(indicadoresPortafolioComprobante != null) {
			for (IndicadoresPortafolio indicadorPortafolio : indicadoresPortafolioComprobante) {
				
				if ("RV".equalsIgnoreCase(cartera.IdClasificacionTipoInstrumento) && "ORDSOBREGIRORV".equalsIgnoreCase(indicadorPortafolio.CodigoTipoIndicador) && "S".equalsIgnoreCase(indicadorPortafolio.ValorIndicador)) {
					return false;
				}
				
				if ("IF".equalsIgnoreCase(cartera.IdClasificacionTipoInstrumento) && "ORDSOBREGIRORV".equalsIgnoreCase(indicadorPortafolio.CodigoTipoIndicador) && "S".equalsIgnoreCase(indicadorPortafolio.ValorIndicador)) {
					return false;
				}
				
				if ("RF".equalsIgnoreCase(cartera.IdClasificacionTipoInstrumento) && "ORDSOBREGIRORV".equalsIgnoreCase(indicadorPortafolio.CodigoTipoIndicador) && "S".equalsIgnoreCase(indicadorPortafolio.ValorIndicador)) {
					return false;
				}
				
				// Si el detalle del comprobante es nominal solo renta variable
				if ("NOM".equalsIgnoreCase(cartera.IdFormaCustodiaDetalleComprobante) && "RV".equalsIgnoreCase(cartera.IdClasificacionTipoInstrumento)) {
					// Si el cliente opera nominal 
					if("ORDOCUSTRV".equalsIgnoreCase(indicadorPortafolio.CodigoTipoIndicador) && "N".equalsIgnoreCase(indicadorPortafolio.ValorIndicador)) {
						return false;
					}
					// Si el cliente opera custodia y el movimiento en transito esta sin ceritificar
					if("ORDOCUSTRV".equalsIgnoreCase(indicadorPortafolio.CodigoTipoIndicador) && "S".equalsIgnoreCase(indicadorPortafolio.ValorIndicador)
						&& "PEN".equalsIgnoreCase(cartera.IdEstadoMovimientoTransito) && cartera.CantidadMovimientoTransito.compareTo(cartera.Cantidad) == 0 ) {
						return false;
					}
				}
			}
		}
		return true;
	}
		
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa02","connections.properties")) {
			Gson gson = Utils.getGsonBuilder();			
		//	String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":288,\"p_IdModulo\":\"FAC\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_EsIngresoAutomatico\":\"N\",\"P_COMPROBANTE\":[{\"IdPortafolio\":6241,\"IdCliente\":4849,\"IdComprobante\":455257,\"IdTipoComprobante\":\"FAE\"}],\"p_CrearComprobanteAutomatico\":\"S\"}";
			String json="{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdModulo\":\"FAC\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_EsIngresoAutomatico\":\"N\",\"P_COMPROBANTE\":[{\"IdPortafolio\":1648,\"IdCliente\":2464,\"IdComprobante\":852141,\"IdTipoComprobante\":\"FEE\"}],\"p_CrearComprobanteAutomatico\":\"S\"}";		
			CrearComprobanteNotaDeCredito_Entrada Objin = gson.fromJson(json, CrearComprobanteNotaDeCredito_Entrada.class);
			
			CrearComprobanteNotaDeCredito demo=new CrearComprobanteNotaDeCredito();				
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			try {
 				CrearComprobanteNotaDeCredito_Salida ObjOut=demo.logica(con, Objin);
 				//con.commit();
 				con.rollback();
 				System.out.println(ObjOut.toString());
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

	