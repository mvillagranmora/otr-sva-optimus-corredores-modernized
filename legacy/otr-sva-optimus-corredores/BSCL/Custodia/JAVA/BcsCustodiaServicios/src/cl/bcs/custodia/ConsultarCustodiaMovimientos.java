package cl.bcs.custodia;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import cl.bcs.plataforma.pla.ObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior.InObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior.OutObtenerDiaAnterior;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaView;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class ConsultarCustodiaMovimientos implements JavaView{
	
	private final Logger logger =  LogManager.getLogger(ConsultarCustodiaMovimientos.class);
	
	private final String SQL_SELECT_CARTERAS_FILTROS = loadResourceToString("cl/bcs/custodia/sql/ConsultarCustodiaMovimientos_SelectCarterasFiltros.sql");
	private final String SQL_SELECT_CARTERAS_HISTORIA = loadResourceToString("cl/bcs/custodia/sql/ConsultarCustodiaMovimientos_SelectCarterasHistoria.sql");
	private final String SQL_INSERT_CARTERAS_FILTROS = loadResourceToString("cl/bcs/custodia/sql/ConsultarCustodiaMovimientos_InsertCarterasFiltros.sql");
	private final String SQL_DELETE_CARTERAS_FILTROS = loadResourceToString("cl/bcs/custodia/sql/ConsultarCustodiaMovimientos_DeleteCarterasFiltros.sql");
	private final String SQL_INSERT_CARTERAS_HISTORIA = loadResourceToString("cl/bcs/custodia/sql/ConsultarCustodiaMovimientos_InsertCarterasHistoria.sql");
	private final String SQL_DELETE_CARTERAS_HISTORIA = loadResourceToString("cl/bcs/custodia/sql/ConsultarCustodiaMovimientos_DeleteCarterasHistoria.sql");
	private final String SQL_SELECT_CUSTODIA_HISTORIA = loadResourceToString("cl/bcs/custodia/sql/ConsultarCustodiaMovimientos_SelectMovimientosHistoria.sql");
	private final String SQL_SELECT_MOVIMIENTOS_TRANSITO = loadResourceToString("cl/bcs/custodia/sql/ConsultarCustodiaMovimientos_SelectMovimientosTransito.sql");
	private final String SQL_SELECT_ULTIMA_GARANTIA_POR_CARTERA =  loadResourceToString("cl/bcs/custodia/sql/ConsultarCustodiaMovimientos_SelectUltimaGarantiaPorCartera.sql");
	private final String SQL_SELECT_CARTERAS_HISTORIA_MVTO = loadResourceToString("cl/bcs/custodia/sql/ConsultarCustodiaMovimientos_SelectMovimientosCarteraHistoria.sql");
	
	
	public static class EntradaConsultarCustodiaMovimientos{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_FechaInicio;
		public String p_FechaTermino;
		public List<VariableProceso> p_VariablesProceso;
		public Long p_IdProductoAgrupador;
		public Long FolioCartera;
		public String p_TipoOperacion = "";
		public String p_IdConcepto = "";
		public String p_IdBoveda = "";
		public String p_UIConsulta = "";
		
		public EntradaConsultarCustodiaMovimientos() {
			this.p_VariablesProceso = new ArrayList<VariableProceso>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public static class VariableProceso{
		public Long IdCliente;
		public Long IdPortafolio;
		public String IdClasificacionTipoInstrumento;
		public String IdTipoInstrumento;
		public Long IdInstrumento;
		public String Nemotecnico;
		public Long FolioCartera;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaConsultarCustodiaMovimientos{
		public List<SalidaMovimiento> P_OUT_TABLA;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		
		public SalidaConsultarCustodiaMovimientos(){
			this.P_OUT_TABLA = new ArrayList<SalidaMovimiento>();
			this.P_OUT_MENSAJES = new ArrayList<>();
			this.P_OUT_ERRORESCONTADOR = 0L;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Cartera{
		public Long IdCartera;
		public Long FolioCartera;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaMovimiento{
		public String UsuarioIngresoOperacion;
		public String UsuarioCertificaOperacion;
		public Timestamp FechaCertificacion;
		public Long IdCartera;
		public String INGIdTipoOperacion;
    	public Timestamp FechaRegistro;
    	public Date FechaOperacion;
    	public Date FechaLiquidacion;
    	public String IdTipoOperacion;
    	public String TipoOperacion;
    	public String Accion;
    	public Long IdCliente;
    	public Long IdPortafolio;
    	public String CodigoPortafolio;
    	public String NombreCliente;
    	public String DocumentoIdentidad;
    	public String IdConcepto;
    	public String NombreConcepto;
    	public String IdClasificacionTipoInstrumento;
    	public String IdTipoInstrumento;
    	public Long IdInstrumento;
    	public String Nemotecnico;
    	public String IdEmisor;
    	public Date FechaVencimientoInstr;
    	public String IdTipoReajuste;
    	public String NemoSVS;
    	public String IdMonedaInstr;
    	public String IdBoveda;
    	public BigDecimal Precio;
    	public BigDecimal Tasa;
    	public BigDecimal Monto;
    	public BigDecimal PrecioTir;
    	public BigDecimal PrecioMercado;
		public BigDecimal TasaMercado;
		public BigDecimal MontoMercadoMonedaLocal;
		public BigDecimal MontoMercadoMonedaInstrumento;
		public BigDecimal MontoMercadoMonedaAdicional;
    	public BigDecimal ValorRescate;
    	public BigDecimal Saldo;
    	public BigDecimal Cantidad;
    	public BigDecimal CantidadNeg;
    	public Long FolioCartera; 
    	public Long FolioComprobante;
    	public Date FechaComprobante;
    	public String IdTipoComprobante;
    	public String TipoRegistro; //1 = saldoInicial de la consulta     2=Ingreso  3=Retiro
		public Long NroFila;
		public String ISIN;
		public Long IdCuentaCustodioPortafolio;
		public String NumeroCuentaBoveda;
		public String IdTipoCustodia;
		public String NombreTipoCustodia;
		public String Producto;
		public String IdEstado;
		public String IdMonedaPrincipal;
		public String IdMonedaSecundaria;
		public BigDecimal ValorTipoCambioCierre;
		public BigDecimal MontoMonedaSecundaria;
		public Long IdOperacion;
		public Date FechaVencimiento;
		public String TipoConcepto;
		public BigDecimal SaldoIngreso;
		public BigDecimal SaldoRetiro;
		public BigDecimal Spread;
		public BigDecimal SpreadMercado;
		public Long IdCarga;
		public String EGRIdTipoOperacion = "";
		public String IdEstadoIdMovimientoCarteraEnTransito;
		public Long EGRIdMovimientoCarteraEnTransito = 0L;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class MovimientoAnulado{
		public Long IdCartera;
		public Long FolioCartera;
		public Long EGRIdOperacion;
		public String IdEstadoCartera;
		public String EstadoMovimiento;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Movimiento extends BcsPojo{
	
		public String UsuarioIngresoOperacion;
		public String UsuarioCertificaOperacion;
		public Long IdMovimientoCarteraEnTransito;
		public Timestamp FechaCertificacion;
		public Long IdCartera;
		public String INGIdTipoOperacion;
    	public Timestamp FechaRegistro;
    	public Date FechaOperacion;
    	public Date FechaLiquidacion;
    	public String IdTipoOperacion;
    	public String TipoOperacion;
    	public Long IdCliente;
    	public Long IdPortafolio;
    	public String CodigoPortafolio;
    	public String NombreCliente;
    	public String DocumentoIdentidad;
    	public String IdConcepto;
    	public String NombreConcepto;
    	public String IdClasificacionTipoInstrumento;
    	public String IdTipoInstrumento;
    	public Long IdInstrumento;
    	public String Nemotecnico;
    	public String IdEmisor;
    	public Date FechaVencimientoInstr;
    	public String IdTipoReajuste;
    	public String NemoSVS;
    	public String IdMonedaInstr;
    	public String IdBoveda;
    	public BigDecimal Precio;
    	public BigDecimal Tasa;
    	public BigDecimal Monto;
    	public BigDecimal PrecioTir;
    	public BigDecimal PrecioMercado;
		public BigDecimal TasaMercado;
		public BigDecimal MontoMercadoMonedaLocal;
		public BigDecimal MontoMercadoMonedaInstrumento;
		public BigDecimal MontoMercadoMonedaAdicional;
    	public BigDecimal ValorRescate;
    	public BigDecimal Saldo;
    	public BigDecimal Cantidad;
    	public BigDecimal CantidadNeg;
    	public Long FolioCartera; 
    	public Long FolioComprobante;
    	public Date FechaComprobante;
    	public String IdTipoComprobante;
		public String ISIN;
		public Long IdCuentaCustodioPortafolio;
		public String NumeroCuentaBoveda;
		public String IdTipoCustodia;
		public String NombreTipoCustodia;
		public String Producto;
		public String IdEstado;
		public Date EGRFechaOperacion;
		public Date EGRFechaLiquidacion;
		public String EGRIdTipoOperacion;
		public String EGRIdConcepto;
		public BigDecimal EGRPrecio;
		public BigDecimal EGRTasa;
		public BigDecimal EGRMonto;
		public BigDecimal EGRPrecioTir;
		public BigDecimal EGRPrecioMercado;
		public BigDecimal EGRTasaMercado;
		public BigDecimal EGRMontoATasaMercado;
		public BigDecimal EGRMontoMercadoMonedaInstrumento;
		public BigDecimal EGRMontoMercadoMonedaAdicional;
		public BigDecimal EGRMontoARecibirAnticipo;
		public BigDecimal EGRValorRescate;
		public BigDecimal EGRCantidad;
		public BigDecimal EGRCantidadNeg;
		public Long EGRFolioComprobante;
		public Date EGRFechaComprobante;
		public String EGRIdTipoComprobante;
		public String EGRTipoOperacion;
		public String EGRNombreConcepto;
		public String IdMonedaPrincipal;
		public String IdMonedaSecundaria;
		public BigDecimal ValorTipoCambioCierre;
		public BigDecimal MontoMonedaSecundaria;
		public Long IdOperacion;
		public Date FechaVencimiento;
		public String TipoConcepto;
		public BigDecimal SaldoIngreso;
		public BigDecimal SaldoRetiro;
		public BigDecimal Spread;
		public BigDecimal SpreadMercado;
		public Long IdCarga ;
		public String IdEstadoIdMovimientoCarteraEnTransito;
		public Long EGRIdMovimientoCarteraEnTransito =0L;
		public String UsuarioEgresoOperacion;
		public String UsuarioEgresoCertificacion;
		
		public Movimiento () {
			this.IdCliente = 0L;
		}
		
	}
	
	public static class MovimientoEnTransito extends BcsPojo{
		public Long IdCartera;
		public String IdEstado;
		public String EGRIdTipoOperacion;
		public String ExcluyeMovimientoEnCartola;
		public Long IdMovimientoCarteraEnTransito;
		public MovimientoEnTransito() {
			this.IdCartera =0L;
			this.IdEstado = "";
			this.EGRIdTipoOperacion ="";
			this.ExcluyeMovimientoEnCartola = "N";
			this.IdMovimientoCarteraEnTransito = 0L;
		}
	}
	
	public static class UltimaGarantiaCartera extends BcsPojo{
		public Long IdGarantia;
		public Long IdCarteraOrigen;
		public String IdEstado;
		
		public UltimaGarantiaCartera() {
			this.IdGarantia =0L;
			this.IdCarteraOrigen =0L;
			this.IdEstado = "";
			
		}
	}

	HashMap<Long,Object> hashUltimaGarantiaPorCartera;	
	
	@Trace
	@Override
	public JsonObject call(Connection conn, JsonObject jin, Integer skip, Integer take, String sort, String filters) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		
		EntradaConsultarCustodiaMovimientos in = gson.fromJson(jin.toString(), EntradaConsultarCustodiaMovimientos.class);
		SalidaConsultarCustodiaMovimientos out = logica(conn, in);
		
		Integer total = out.P_OUT_TABLA.size();
		logger.debug("limit");
		out.P_OUT_TABLA = this.limitResponse(out.P_OUT_TABLA, skip, take, SalidaMovimiento.class);
		logger.debug("end limit");
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
        dataout.add("total", new JsonPrimitive(total));
		return dataout;
	}
	
	@Trace
	public SalidaConsultarCustodiaMovimientos logica(Connection con, EntradaConsultarCustodiaMovimientos entrada) throws Exception {
		logger.info("INICIO::ConsultarCustodiaMovimientos");
		SalidaConsultarCustodiaMovimientos out = new SalidaConsultarCustodiaMovimientos();
		
		SQL sql=new SQL();
		
		String fechaInicio = entrada.p_FechaInicio;
		String fechaGoLive = "0001-01-01";
		// aplica solo para Consulta de Custodia		
		if ("UI_MOV_CUS".equals(entrada.p_UIConsulta) || "UI_MOV_CAR".equals(entrada.p_UIConsulta)) {
			//Si existe parametro PLA.FechaGoLive evalua si la fecha ingresada es inferior al Go Live CGO
			try {
				fechaGoLive = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaGoLive").trim();
				if (fechaGoLive.equals("") || !esFechavalida(fechaGoLive))
					fechaGoLive = "0001-01-01";
					
			} catch(Exception ex) {
				fechaGoLive = "0001-01-01";
			}
			fechaInicio = Fecha.toDate(fechaGoLive).after(Fecha.toDate(entrada.p_FechaInicio)) ? fechaGoLive : entrada.p_FechaInicio;
		}
				
		Date dateFechaInicio = Fecha.toDate(fechaInicio);
		Date dateFechaTermino = Fecha.toDate(entrada.p_FechaTermino);
		logger.debug("eliminar filtros");
		SQL.exec(con, SQL_DELETE_CARTERAS_FILTROS);
		SQL.exec(con, SQL_DELETE_CARTERAS_HISTORIA);
		logger.debug("insert filtros");
		sql.batch(con, SQL_INSERT_CARTERAS_FILTROS, entrada.p_VariablesProceso, VariableProceso.class);

		//Este servicio es utilizado en Movimientos de Consulta de Custodia y Consulta de Cartera
		//el parametro entrada.p_UIConsulta identifica si viene de Movimientos de Consulta de Cartera (UI_MOV_CAR)
		//desde la UI ConsultarCartolaMovimientos.js para identificar su origen
		List<String> notInIdTipoInstrumento = new ArrayList<String>(Arrays.asList("UI_MOV_CAR".equals(entrada.p_UIConsulta) ? new String[]{"PRT","EMP","SIM"} : "NCG380CUS".equalsIgnoreCase(entrada.p_UIConsulta) ? new String[]{"PRT","EMP","SIM","PAC"} :new String[]{"SIM","RRV","RRF","PAC"}));
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		params.put("FechaInicio", fechaInicio);
		params.put("FechaTermino", entrada.p_FechaTermino); 
		params.put("IdProductoAgrupador", entrada.p_IdProductoAgrupador);
		params.put("NotInIdTipoInstrumento", notInIdTipoInstrumento);
		params.put("TipoOperacion", entrada.p_TipoOperacion);
		params.put("IdConcepto", entrada.p_IdConcepto);
		params.put("IdBoveda", entrada.p_IdBoveda);
		
		logger.debug("select carteras");
		List<Cartera> carteras = sql.query(con, SQL_SELECT_CARTERAS_FILTROS, params, Cartera.class);
		
		HashMap<Long,Boolean> folioCarteraFiltro = new HashMap<Long,Boolean>();
		List<Cartera> carterasParaBuscarHistoria = new ArrayList<Cartera>();
		
		for(Cartera cartera : carteras) {
			if(folioCarteraFiltro.get(cartera.FolioCartera) == null) {
				carterasParaBuscarHistoria.add(cartera);
				folioCarteraFiltro.put(cartera.FolioCartera, true);
			}
		}
		logger.debug("insert carteras historica");
		sql.batch(con, SQL_INSERT_CARTERAS_HISTORIA, carterasParaBuscarHistoria, Cartera.class);
		
		params.clear();
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		params.put("FechaInicio", fechaInicio);
		params.put("FechaTermino", entrada.p_FechaTermino);
		params.put("TipoOperacion", entrada.p_TipoOperacion);
		params.put("IdConcepto", entrada.p_IdConcepto);
		params.put("IdCliente",entrada.p_VariablesProceso.get(0).IdCliente);
		params.put("IdClasificacionTipoInstrumento",entrada.p_VariablesProceso.get(0).IdClasificacionTipoInstrumento);
		
		logger.debug("select carteras historica");
		List<Movimiento> historiaCarteras = sql.query(con, SQL_SELECT_CARTERAS_HISTORIA, params, Movimiento.class);

		logger.debug("Construccion salida");
		HashMap<Long,List<Movimiento>> hashFolioHistoriaCarteras = new HashMap<Long,List<Movimiento>>();
		
		List<Long> idsCarteras = historiaCarteras.stream().map(his-> his.IdCartera).collect(Collectors.toList());
		HashMap<String,Object> paramsMvtos = new HashMap<String,Object>(1);
		
		int paramSize = 10000;
		List<MovimientoEnTransito> mvtos = new ArrayList<>();
		
		idsCarteras = idsCarteras.stream()
			    .distinct()
			    .collect(Collectors.toList());
		
		if (idsCarteras.size()> paramSize) {
			
			int indexStart = 0;
			int indexEnd = indexStart + paramSize;
			logger.debug("start:" + indexStart + " end:"+(indexEnd-1));
			while (indexStart < idsCarteras.size()) {
				ArrayList<Long> subIdsCarteras = new ArrayList<>(idsCarteras.subList(indexStart, indexEnd));
				paramsMvtos.put("idsCartera", subIdsCarteras);
				List<MovimientoEnTransito> subMovtos = sql.query(con, SQL_SELECT_MOVIMIENTOS_TRANSITO, paramsMvtos, MovimientoEnTransito.class);
				mvtos.addAll(subMovtos);
				indexStart = indexEnd;
				indexEnd = ((indexEnd + paramSize) > idsCarteras.size())?(idsCarteras.size()):indexEnd + paramSize;
				logger.debug("start:" + indexStart + " end:"+ (indexEnd-1));
			}
		}
		else {
			paramsMvtos.put("idsCartera", idsCarteras);
			mvtos = sql.query(con, SQL_SELECT_MOVIMIENTOS_TRANSITO, paramsMvtos, MovimientoEnTransito.class);
		}
		
		
		
		HashMap<Long,Object> hashMovimientosEnTransito = Utils.List2Hash(mvtos, "IdCartera");
		
		
		HashMap<String,Object> paramsGarantias = new HashMap<String,Object>(1);
		
		List<UltimaGarantiaCartera> garantias = new ArrayList<>();
		if (idsCarteras.size()> paramSize) {
			
			int indexStart = 0;
			int indexEnd = indexStart + paramSize;
			while (indexStart < idsCarteras.size()) {
				ArrayList<Long> subIdsCarteras = new ArrayList<>(idsCarteras.subList(indexStart, indexEnd));
				paramsGarantias.put("idsCartera", subIdsCarteras);
				List<UltimaGarantiaCartera> subgarantias = sql.query(con, SQL_SELECT_ULTIMA_GARANTIA_POR_CARTERA, paramsGarantias, UltimaGarantiaCartera.class);
				garantias.addAll(subgarantias);
				indexStart = indexEnd;
				indexEnd = ((indexEnd + paramSize) > idsCarteras.size())?(idsCarteras.size()):indexEnd + paramSize;
			}
			
		}
		else {
			paramsGarantias.put("idsCartera", idsCarteras);
			garantias = sql.query(con, SQL_SELECT_ULTIMA_GARANTIA_POR_CARTERA, paramsGarantias, UltimaGarantiaCartera.class);
		}
		
		hashUltimaGarantiaPorCartera = Utils.List2Hash(garantias, "IdCarteraOrigen");
		
		
		//aqui se esta agrupando por folio cartera
		for(Movimiento historiaCartera : historiaCarteras) {
			
			List<Movimiento> salidaMovimientoHistoriaCartera = hashFolioHistoriaCarteras.get(historiaCartera.FolioCartera);
			if(salidaMovimientoHistoriaCartera == null) {
				salidaMovimientoHistoriaCartera = new ArrayList<Movimiento>();
			}
			salidaMovimientoHistoriaCartera.add(historiaCartera);
			hashFolioHistoriaCarteras.put(historiaCartera.FolioCartera, salidaMovimientoHistoriaCartera);
		}
		
		List<SalidaMovimiento> salidasMovimiento = new ArrayList<SalidaMovimiento>();
		//por cada uno de los folios de cartera
		for(Map.Entry<Long,List<Movimiento>> historiasFolioCarteras : hashFolioHistoriaCarteras.entrySet()) {
			//obtener todos los movimientos asociados al folio
			List<Movimiento> historiaFolio = historiasFolioCarteras.getValue();
			
			
			if(historiaFolio.size() == 1) {
				Movimiento movimientoActual = historiaFolio.get(0);
				SalidaMovimiento movimiento = copiarMovimiento(movimientoActual, new SalidaMovimiento());			
				movimiento.Accion = "INGRESO";
				movimiento.TipoRegistro = "2";
				movimiento.Saldo = movimiento.SaldoIngreso;
				salidasMovimiento.add(movimiento);
				if(movimientoActual.EGRCantidad.compareTo(BigDecimal.ZERO) != 0) {
					salidasMovimiento.add(crearMovimientoEgreso(movimientoActual,null,null));
				} 
				
			} else {
				for(int i=0; i < historiaFolio.size(); i++) {
					Movimiento movimientoActual = historiaFolio.get(i);
					SalidaMovimiento movimiento = copiarMovimiento(movimientoActual, new SalidaMovimiento());

					movimiento.Accion = "INGRESO";
					movimiento.TipoRegistro = "2";
					movimiento.Saldo = movimiento.SaldoIngreso;
					salidasMovimiento.add(movimiento);
					if(i != historiaFolio.size()-1) {
						Movimiento movimientoProximo = historiaFolio.get(i+1);
						MovimientoEnTransito mvtoProx = (MovimientoEnTransito) hashMovimientosEnTransito.get(movimientoProximo.IdCartera);
						if("".equals(entrada.p_UIConsulta) && mvtoProx!= null && ("S".equals(mvtoProx.ExcluyeMovimientoEnCartola) || ((mvtoProx != null && ((!"INGGTA".equals(mvtoProx.EGRIdTipoOperacion) && "ANU".equals(mvtoProx.IdEstado)) )) || "ANINGA".equals(movimientoActual.IdTipoOperacion) || "ANREGA".equals(movimientoActual.IdTipoOperacion))) ){
							salidasMovimiento.remove(movimiento);
							continue;
						}
						//Se quitan carteras que tengan la ultima garantia anulada para no mostrarla en la cartola
						UltimaGarantiaCartera ultimaGarantiaCartera = (UltimaGarantiaCartera) hashUltimaGarantiaPorCartera.get(movimientoProximo.IdCartera);
						if("".equals(entrada.p_UIConsulta) && ultimaGarantiaCartera != null && "ANU".equals(ultimaGarantiaCartera.IdEstado)) {
							salidasMovimiento.remove(movimiento);
							continue;
						}
						
						if(i == 0 && !"VIG".equals(movimientoActual.IdEstado)) {
							salidasMovimiento.add(crearMovimientoEgreso(movimientoActual,null,null));
						}
						if(!movimientoActual.INGIdTipoOperacion.equals(movimientoProximo.EGRIdTipoOperacion)) {
							salidasMovimiento.add(crearMovimientoEgreso(movimientoActual,movimientoProximo,null));
						}
						if("RPA".equals(movimientoProximo.IdEstado) && movimientoActual.Cantidad.add(movimientoProximo.EGRCantidad).compareTo(movimientoProximo.Cantidad) != 0 ) {
							salidasMovimiento.add(crearMovimientoIngreso(movimientoActual, movimientoProximo));
						}
						//No modificar el movimiento nuevo de Ingreso que se genera al modificar un movimiento. Jira CELLINCE-461
						if(!"LIQ".equals(movimientoProximo.IdEstado) && !"ANU".equals(movimientoProximo.IdEstado) && !"MOD".equals(movimientoProximo.IdEstado)) {
							movimiento = crearMovimientoEgreso(movimientoActual, movimientoProximo,movimiento);
						} else if("LIQ".equals(movimientoProximo.IdEstado) && !movimientoActual.INGIdTipoOperacion.equals(movimientoProximo.EGRIdTipoOperacion)) {
							continue;
						} else if(!"MOD".equals(movimientoProximo.IdEstado)) {
							movimiento.Cantidad = movimientoActual.Cantidad.subtract(movimientoProximo.EGRCantidad, MathContext.DECIMAL128);
							movimiento.CantidadNeg = movimiento.Cantidad;
							movimiento.Monto = movimientoActual.Monto.subtract(movimientoProximo.EGRMonto, MathContext.DECIMAL128);
							movimiento.ValorRescate = movimientoActual.ValorRescate.subtract(movimientoProximo.EGRValorRescate, MathContext.DECIMAL128);
						}						
					}
					
				}
			}
		}
		
		//IMPMBIOPT-826: aplica filtro p_TipoOperacion si es INGRESO o RETIRO
		if (!"".equals(entrada.p_TipoOperacion)) {
			salidasMovimiento = salidasMovimiento.stream().filter(s -> entrada.p_TipoOperacion.equals(s.Accion)).collect(Collectors.toList());
		}
		
		salidasMovimiento = valorizarMovimientosRVFM(salidasMovimiento);
		
		logger.debug("Se eliminan las carteras anuladas de FFMM");
		
		List<SalidaMovimiento> movimientosPorAnular = new ArrayList<SalidaMovimiento>();	
		for(SalidaMovimiento movimiento : salidasMovimiento) {
			if(movimiento.INGIdTipoOperacion.equals("ANURES") && movimiento.IdTipoComprobante.contentEquals("FFMM")) {
				movimientosPorAnular.add(movimiento);
			}
		}
		 for(SalidaMovimiento movimientoAnular: movimientosPorAnular) {
			 salidasMovimiento.remove(movimientoAnular);
		 }
		 
		 
		 logger.debug("Se eliminan las carteras anuladas de Ingreso de Titulos");
			
			List<SalidaMovimiento> movimientosPorAnularIngresoTitulos = new ArrayList<SalidaMovimiento>();	
			for(SalidaMovimiento movimiento : salidasMovimiento) {
				if("INGTIT".equals(movimiento.IdTipoOperacion) && "ANU".equals(movimiento.IdEstadoIdMovimientoCarteraEnTransito)) 
				{
					movimientosPorAnularIngresoTitulos.add(movimiento);
				}
			}
			for(SalidaMovimiento movimientoAnular: movimientosPorAnularIngresoTitulos) {
				 salidasMovimiento.remove(movimientoAnular);
			}
		 
		
		ArrayList<String> keyCarteras = new ArrayList<String>();		
	
		for(SalidaMovimiento salidaMovimiento : salidasMovimiento) {
			if(salidaMovimiento.FechaOperacion.compareTo(dateFechaInicio) != -1 && salidaMovimiento.FechaOperacion.compareTo(dateFechaTermino) != 1) {
				if (!out.P_OUT_TABLA.contains(salidaMovimiento)) {
					if (!keyCarteras.contains(salidaMovimiento.IdCartera + "_" + Fecha.toStringDate(salidaMovimiento.FechaOperacion) + "_" + Fecha.toStringDate(salidaMovimiento.FechaLiquidacion) + "_" + salidaMovimiento.IdTipoOperacion + "_" + salidaMovimiento.IdCliente  + "_" + salidaMovimiento.IdPortafolio + "_" + salidaMovimiento.NemoSVS + "_" + salidaMovimiento.Precio + "_" + salidaMovimiento.Cantidad + "_" + salidaMovimiento.FolioCartera + "_" + salidaMovimiento.FolioComprobante)) {
						out.P_OUT_TABLA.add(salidaMovimiento);
						keyCarteras.add(salidaMovimiento.IdCartera + "_" + Fecha.toStringDate(salidaMovimiento.FechaOperacion) + "_" + Fecha.toStringDate(salidaMovimiento.FechaLiquidacion) + "_" + salidaMovimiento.IdTipoOperacion + "_" + salidaMovimiento.IdCliente  + "_" + salidaMovimiento.IdPortafolio + "_" + salidaMovimiento.NemoSVS + "_" + salidaMovimiento.Precio + "_" + salidaMovimiento.Cantidad + "_" + salidaMovimiento.FolioCartera + "_" + salidaMovimiento.FolioComprobante);
					}
						
				}	
				
			}
		}
		
		// aplica solo para Consulta de Custodia
		if ("UI_MOV_CUS".equals(entrada.p_UIConsulta) || "UI_MOV_CAR".equals(entrada.p_UIConsulta)) {
			salidasMovimiento.clear();
			if(!fechaGoLive.equals("") && !fechaGoLive.equals("0001-01-01") && (Fecha.toDate(fechaGoLive).after(Fecha.toDate(entrada.p_FechaInicio)))){
				// dia anterior del GoLive que representa la ultima fecha de la carga de Sebra
				fechaGoLive = obtenerDiaAnterior(con, Fecha.toDate(fechaGoLive)); 
				
				String fechaTermino = Fecha.toDate(entrada.p_FechaTermino).after(Fecha.toDate(fechaGoLive)) ? fechaGoLive : entrada.p_FechaTermino; 
				params = new HashMap<String,Object>();
				params.put("FechaInicio", entrada.p_FechaInicio);
				params.put("FechaTermino", fechaTermino);
				params.put("IdCliente", entrada.p_VariablesProceso.get(0).IdCliente);
				params.put("IdPortafolio", entrada.p_VariablesProceso.get(0).IdPortafolio);
				params.put("IdClasificacionTipoInstrumento", entrada.p_VariablesProceso.get(0).IdClasificacionTipoInstrumento);
				params.put("IdTipoInstrumento", entrada.p_VariablesProceso.get(0).IdTipoInstrumento);
				params.put("IdInstrumento", entrada.p_VariablesProceso.get(0).IdInstrumento);
				params.put("Nemotecnico", entrada.p_VariablesProceso.get(0).Nemotecnico);
				List<SalidaMovimiento> out_tbl_hist;
				if ("UI_MOV_CAR".equals(entrada.p_UIConsulta)) {
					out_tbl_hist = sql.query(con, SQL_SELECT_CARTERAS_HISTORIA_MVTO, params, SalidaMovimiento.class);
				}
				else {
					out_tbl_hist = sql.query(con, SQL_SELECT_CUSTODIA_HISTORIA, params, SalidaMovimiento.class);
				};
				
				
				
				// agrega la consulta historica del go live CGO
				out.P_OUT_TABLA.addAll(out_tbl_hist);
			}
		}
		
		logger.info("FIN::ConsultarCustodiaMovimientos");
		return out;
	}
	
	private SalidaMovimiento copiarMovimiento(Movimiento movimiento, SalidaMovimiento salidaMovimiento) {
		salidaMovimiento.UsuarioIngresoOperacion = movimiento.UsuarioIngresoOperacion;
		salidaMovimiento.UsuarioCertificaOperacion = movimiento.UsuarioCertificaOperacion;
		salidaMovimiento.FechaCertificacion = movimiento.FechaCertificacion;
		
		salidaMovimiento.IdCartera = movimiento.IdCartera;
		salidaMovimiento.INGIdTipoOperacion = movimiento.INGIdTipoOperacion;
		salidaMovimiento.EGRIdTipoOperacion = movimiento.EGRIdTipoOperacion;
		salidaMovimiento.FechaRegistro = movimiento.FechaRegistro;
		salidaMovimiento.FechaOperacion = movimiento.FechaOperacion;
		salidaMovimiento.FechaLiquidacion = movimiento.FechaLiquidacion;
		salidaMovimiento.IdTipoOperacion = movimiento.IdTipoOperacion;
		salidaMovimiento.TipoOperacion = movimiento.TipoOperacion;
		salidaMovimiento.IdCliente = movimiento.IdCliente;
		salidaMovimiento.IdPortafolio = movimiento.IdPortafolio;
		salidaMovimiento.CodigoPortafolio = movimiento.CodigoPortafolio;
		salidaMovimiento.NombreCliente = movimiento.NombreCliente;
		salidaMovimiento.DocumentoIdentidad = movimiento.DocumentoIdentidad;
		salidaMovimiento.IdConcepto = movimiento.IdConcepto;
		salidaMovimiento.NombreConcepto = movimiento.NombreConcepto;
		salidaMovimiento.IdClasificacionTipoInstrumento = movimiento.IdClasificacionTipoInstrumento;
		salidaMovimiento.IdTipoInstrumento = movimiento.IdTipoInstrumento;
		salidaMovimiento.IdInstrumento = movimiento.IdInstrumento;
		salidaMovimiento.Nemotecnico = movimiento.Nemotecnico;
		salidaMovimiento.IdEmisor = movimiento.IdEmisor;
		salidaMovimiento.FechaVencimientoInstr = movimiento.FechaVencimientoInstr;
		salidaMovimiento.IdTipoReajuste = movimiento.IdTipoReajuste;
		salidaMovimiento.NemoSVS = movimiento.NemoSVS;
		salidaMovimiento.IdMonedaInstr = movimiento.IdMonedaInstr;
		salidaMovimiento.IdBoveda = movimiento.IdBoveda;
		salidaMovimiento.Precio = movimiento.Precio;
		salidaMovimiento.Tasa = movimiento.Tasa;
		salidaMovimiento.Monto = movimiento.Monto;
		salidaMovimiento.PrecioTir = movimiento.PrecioTir;
		salidaMovimiento.PrecioMercado = movimiento.PrecioMercado;
		salidaMovimiento.TasaMercado = movimiento.TasaMercado;
		salidaMovimiento.MontoMercadoMonedaLocal = movimiento.MontoMercadoMonedaLocal;
		salidaMovimiento.MontoMercadoMonedaInstrumento = movimiento.MontoMercadoMonedaInstrumento;
		salidaMovimiento.MontoMercadoMonedaAdicional = movimiento.MontoMercadoMonedaAdicional;
		salidaMovimiento.ValorRescate = movimiento.ValorRescate;
		salidaMovimiento.Saldo = movimiento.Saldo;
		salidaMovimiento.Cantidad = movimiento.Cantidad;
		salidaMovimiento.CantidadNeg = movimiento.CantidadNeg;
		salidaMovimiento.FolioCartera = movimiento.FolioCartera; 
		salidaMovimiento.FolioComprobante = movimiento.FolioComprobante;
		salidaMovimiento.FechaComprobante = movimiento.FechaComprobante;
		salidaMovimiento.IdTipoComprobante = movimiento.IdTipoComprobante;
		salidaMovimiento.ISIN = movimiento.ISIN;
		salidaMovimiento.IdCuentaCustodioPortafolio = movimiento.IdCuentaCustodioPortafolio;
		salidaMovimiento.NumeroCuentaBoveda = movimiento.NumeroCuentaBoveda;
		salidaMovimiento.IdTipoCustodia = movimiento.IdTipoCustodia;
		salidaMovimiento.NombreTipoCustodia = movimiento.NombreTipoCustodia;
		salidaMovimiento.Producto = movimiento.Producto;
		salidaMovimiento.IdEstado = movimiento.IdEstado;
		salidaMovimiento.IdMonedaPrincipal = movimiento.IdMonedaPrincipal;
		salidaMovimiento.IdMonedaSecundaria = movimiento.IdMonedaSecundaria;
		salidaMovimiento.ValorTipoCambioCierre = movimiento.ValorTipoCambioCierre;
		salidaMovimiento.MontoMonedaSecundaria = movimiento.MontoMonedaSecundaria;
		salidaMovimiento.IdOperacion = movimiento.IdOperacion;
		salidaMovimiento.FechaVencimiento = movimiento.FechaVencimiento;
		salidaMovimiento.TipoConcepto = movimiento.TipoConcepto;
		salidaMovimiento.SaldoIngreso = movimiento.SaldoIngreso;
		salidaMovimiento.SaldoRetiro = movimiento.SaldoRetiro;
		salidaMovimiento.Spread = movimiento.Spread;
		salidaMovimiento.SpreadMercado = movimiento.SpreadMercado;
		salidaMovimiento.IdCarga= movimiento.IdCarga;
		salidaMovimiento.IdEstadoIdMovimientoCarteraEnTransito = movimiento.IdEstadoIdMovimientoCarteraEnTransito;

		return salidaMovimiento;
	}
	
	
	private SalidaMovimiento crearMovimientoEgreso(Movimiento movimientoActual,Movimiento movimientoProximo, SalidaMovimiento movimiento) throws Exception {
		SalidaMovimiento movimientoEGR = movimiento;
		if(movimiento == null) {
			movimientoEGR = new SalidaMovimiento();
		}
		movimientoEGR = copiarMovimiento(movimientoActual, movimientoEGR);
		movimientoEGR.TipoRegistro = "2";
		movimientoEGR.Accion = "RETIRO";
		if(movimientoProximo == null) {
			movimientoEGR.FechaOperacion = movimientoActual.EGRFechaOperacion;
			movimientoEGR.FechaLiquidacion = movimientoActual.EGRFechaLiquidacion;
			movimientoEGR.IdTipoOperacion = movimientoActual.EGRIdTipoOperacion; 
			movimientoEGR.IdConcepto = movimientoActual.EGRIdConcepto;
			movimientoEGR.TipoConcepto = movimientoActual.TipoConcepto;
			movimientoEGR.Precio = movimientoActual.EGRPrecio;
			movimientoEGR.Tasa = movimientoActual.EGRTasa;
			movimientoEGR.Monto = movimientoActual.EGRMonto;
			movimientoEGR.PrecioTir = movimientoActual.EGRPrecioTir;
			movimientoEGR.PrecioMercado = movimientoActual.EGRPrecioMercado;
			movimientoEGR.TasaMercado = movimientoActual.EGRTasaMercado;
			movimientoEGR.MontoMercadoMonedaLocal = movimientoActual.EGRMontoATasaMercado;
			movimientoEGR.MontoMercadoMonedaInstrumento = movimientoActual.EGRMontoMercadoMonedaInstrumento;
			movimientoEGR.MontoMercadoMonedaAdicional = movimientoActual.EGRMontoMercadoMonedaAdicional;
			movimientoEGR.ValorRescate = movimientoActual.EGRValorRescate;
			movimientoEGR.Cantidad = movimientoActual.EGRCantidad.negate();
			movimientoEGR.CantidadNeg = movimientoEGR.Cantidad.negate();
			movimientoEGR.FolioComprobante = movimientoActual.EGRFolioComprobante;
			movimientoEGR.FechaComprobante = movimientoActual.EGRFechaComprobante;
			movimientoEGR.IdTipoComprobante = movimientoActual.EGRIdTipoComprobante;
			movimientoEGR.NombreConcepto = movimientoActual.EGRNombreConcepto;
			movimientoEGR.TipoOperacion = movimientoActual.EGRTipoOperacion;
			movimientoEGR.Saldo = movimientoActual.SaldoRetiro;
			movimientoEGR.IdEstadoIdMovimientoCarteraEnTransito = movimientoActual.IdEstadoIdMovimientoCarteraEnTransito;

			movimientoEGR.UsuarioIngresoOperacion = movimientoActual.UsuarioEgresoOperacion;
			movimientoEGR.UsuarioCertificaOperacion = movimientoActual.UsuarioEgresoCertificacion;
		

		}else {
			movimientoEGR.FechaOperacion = movimientoProximo.EGRFechaOperacion;
			movimientoEGR.FechaLiquidacion = movimientoProximo.EGRFechaLiquidacion;
			movimientoEGR.IdTipoOperacion = movimientoProximo.EGRIdTipoOperacion;
			movimientoEGR.IdConcepto = movimientoProximo.EGRIdConcepto;
			movimientoEGR.TipoConcepto = movimientoProximo.TipoConcepto;
			movimientoEGR.Precio = movimientoProximo.EGRPrecio;
			movimientoEGR.Tasa = movimientoProximo.EGRTasa;
			movimientoEGR.Monto = movimientoProximo.EGRMonto;
			movimientoEGR.PrecioTir = movimientoProximo.EGRPrecioTir;
			movimientoEGR.PrecioMercado = movimientoProximo.EGRPrecioMercado;
			movimientoEGR.TasaMercado = movimientoProximo.EGRTasaMercado;
			movimientoEGR.MontoMercadoMonedaLocal = movimientoProximo.EGRMontoATasaMercado;
			movimientoEGR.MontoMercadoMonedaInstrumento = movimientoProximo.EGRMontoMercadoMonedaInstrumento;
			movimientoEGR.MontoMercadoMonedaAdicional = movimientoProximo.EGRMontoMercadoMonedaAdicional;
			movimientoEGR.ValorRescate = movimientoProximo.EGRValorRescate;
			movimientoEGR.Cantidad = movimientoProximo.EGRCantidad.negate();
			movimientoEGR.CantidadNeg = movimientoEGR.Cantidad.negate();
			movimientoEGR.FolioComprobante = movimientoProximo.EGRFolioComprobante;
			movimientoEGR.FechaComprobante = movimientoProximo.EGRFechaComprobante;
			movimientoEGR.IdTipoComprobante = movimientoProximo.EGRIdTipoComprobante;
			movimientoEGR.NombreConcepto = movimientoProximo.EGRNombreConcepto;
			movimientoEGR.TipoOperacion = movimientoProximo.EGRTipoOperacion;
			movimientoEGR.Saldo = movimientoProximo.SaldoRetiro;
			movimientoEGR.IdCartera = movimientoProximo.IdCartera;
			movimientoEGR.IdEstadoIdMovimientoCarteraEnTransito = movimientoActual.IdEstadoIdMovimientoCarteraEnTransito;
			
			movimientoEGR.UsuarioIngresoOperacion = movimientoProximo.UsuarioEgresoOperacion;
			movimientoEGR.UsuarioCertificaOperacion = movimientoProximo.UsuarioEgresoCertificacion;
			
		}

		return movimientoEGR;
	}
	
	private SalidaMovimiento crearMovimientoIngreso(Movimiento movimientoActual,Movimiento movimientoProximo) throws Exception {
		logger.trace("INICIO::ConsultarCustodiaMovimientos.crearMovimientoIngreso");
		long inicio = System.currentTimeMillis();
		
		SalidaMovimiento movimientoING = new SalidaMovimiento();
		movimientoING = copiarMovimiento(movimientoActual, movimientoING);
		movimientoING.TipoRegistro = "2";
		movimientoING.Accion = "INGRESO";
		if(movimientoProximo != null) {
			movimientoING.Cantidad = movimientoProximo.EGRCantidad.abs();
			movimientoING.CantidadNeg = movimientoING.Cantidad;
			movimientoING.Precio = movimientoProximo.EGRPrecio.abs();
			movimientoING.PrecioMercado = movimientoProximo.EGRPrecioMercado.abs();
			movimientoING.PrecioTir = movimientoProximo.EGRPrecioTir.abs();
			movimientoING.Monto = movimientoProximo.EGRMonto.abs();
			movimientoING.ValorRescate = movimientoProximo.EGRValorRescate.abs();
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConsultarCustodiaMovimientos.crearMovimientoIngreso tiempo: " + (fin - inicio));

		return movimientoING;
	}
	
	private String obtenerDiaAnterior(Connection con, Date fechaOperacion) throws Exception {
		ObtenerDiaAnterior obtenerDiaAnterior = new ObtenerDiaAnterior();
		InObtenerDiaAnterior entradaObtenerDiaAnterior = new InObtenerDiaAnterior();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");  
		entradaObtenerDiaAnterior.diaHabil = "N";

		InObtenerDiaAnterior.FechasIn fecha = new InObtenerDiaAnterior.FechasIn();
		fecha.fechaInicial = dateFormat.format(fechaOperacion);
		fecha.numeroDias = 1L;

		entradaObtenerDiaAnterior.fechas.add(fecha);
		OutObtenerDiaAnterior salidaObtenerDiaAnterior = obtenerDiaAnterior.logica(con, entradaObtenerDiaAnterior);
		return salidaObtenerDiaAnterior.fechas.get(0).fechaCalculada;
	}
	
	private List<SalidaMovimiento> valorizarMovimientosRVFM(List<SalidaMovimiento> salidasMovimiento) {
		salidasMovimiento.parallelStream()
		.filter(movimiento->"RV".equals(movimiento.IdClasificacionTipoInstrumento) || "FM".equals(movimiento.IdClasificacionTipoInstrumento))
		.forEach(movimiento->{
			movimiento.Monto = movimiento.Cantidad.multiply(movimiento.Precio, MathContext.DECIMAL128);
		});
		return salidasMovimiento;
	}
	
	private Boolean esFechavalida(String fecha) {
		try {
			SimpleDateFormat formatofecha = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
			formatofecha.setLenient(false);
			formatofecha.parse(fecha);
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			Gson gson = Utils.getGsonBuilder();
			String jsonRF = "{\"p_FechaInicio\":\"2024-12-18\",\"p_FechaTermino\":\"2024-12-18\",\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"CUS\",\"p_VariablesProceso\":[{\"IdCliente\":61625,\"IdPortafolio\":null,\"FolioCartera\":0,\"IdClasificacionTipoInstrumento\":\"\",\"IdTipoInstrumento\":\"\",\"IdInstrumento\":0,\"Nemotecnico\":\"COPEC\"}],\"p_IdProductoAgrupador\":0,\"p_UIConsulta\":\"UI_MOV_CUS\"}";
			EntradaConsultarCustodiaMovimientos Objin = gson.fromJson(jsonRF, EntradaConsultarCustodiaMovimientos.class);
			ConsultarCustodiaMovimientos  demo=new ConsultarCustodiaMovimientos();				
			try {
				SalidaConsultarCustodiaMovimientos out=demo.logica(con, Objin);
				System.out.println(out.P_OUT_TABLA);
				System.out.println(out.P_OUT_TABLA.size());
				con.rollback();
			}
			catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}
			con.rollback(); 			
    	}
		System.out.println("== END ==");
	}
	
}
