package cl.bcs.ordenes;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.sql.Date;
import cl.bcs.utils.Fecha;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.ordenes.pojo.ConstantesOrdenes;
import cl.bcs.ordenes.pojo.FormaPagoSpot;


import cl.bcs.mesa.SPT.ConsultarPuntasPorTramo;
import cl.bcs.mesa.SPT.ConsultarPuntasPorTramo.ConsultarPuntasPorTramoEntrada;
import cl.bcs.mesa.SPT.ConsultarPuntasPorTramo.ConsultarPuntasPorTramoSalida;
import cl.bcs.mesa.SPT.ConsultarPuntasPorTramo.PuntaPorTramoSalida;
import cl.bcs.mesa.SPT.CrearOperacionSpot;
import cl.bcs.mesa.SPT.CrearOperacionSpot.CrearOperacionSpotEntrada;
import cl.bcs.mesa.SPT.CrearOperacionSpot.CrearOperacionSpotSalida;

import cl.bcs.mesa.SPT.CrearOperacionSpot.FormaPagoObjIn;
import cl.bcs.mesa.SPT.CrearOperacionSpot.OperacionObjIn;
import cl.bcs.mesa.SPT.CrearOperacionSpot.OperacionObjOut;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida;
import cl.bcs.plataforma.ins.ConsultarDatosInstrumentos;
import cl.bcs.plataforma.ins.ConsultarDatosInstrumentos.ConsultaInstrumento;
import cl.bcs.plataforma.ins.ConsultarDatosInstrumentos.ConsultarDatosInstrumento_Entrada;
import cl.bcs.plataforma.ins.ConsultarDatosInstrumentos.ConsultarDatosInstrumento_Salida;
import cl.bcs.plataforma.ins.ConsultarDatosInstrumentos.DatosInstrumento;
import cl.bcs.plataforma.ins.ConsultarDatosInstrumentos.FiltroTipoInstrumento;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class ProcesarIngresoOperacionSpot implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(ProcesarIngresoOperacionSpot.class);
	
	private final ConstantesOrdenes _c = new ConstantesOrdenes();
	
	private final String SQL_SELECT_MESA = "SELECT TOP 1 \"IdMesa\" FROM BSCL.\"BSCL.Mesa.BD::MES.Mesa\" WHERE  \"IdTipoProducto\" = 'MEME' AND \"IdEstadoRegistro\" = 'ACT' AND \"PorDefecto\" = 'S'";
			
			
	public static class EntradaProcesarIngresoOperacionSpot{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_CalculaMargen;
		public String p_UsaReglasNegocio;
		public List<AsignacionSpot> p_DatosOperacion;
		public List<FormaPagoSpot> p_DatosFormaPago;
		public String p_PermiteOperarProspectos;
			
		public EntradaProcesarIngresoOperacionSpot(){
			this.p_DatosOperacion = new ArrayList<AsignacionSpot>();
			this.p_DatosFormaPago = new ArrayList<FormaPagoSpot>();
			this.p_PermiteOperarProspectos = "N";
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaProcesarIngresoOperacionSpot {
		public List<MensajeSalida>		P_OUT_MENSAJES;
		public Long						P_OUT_ERRORESCONTADOR;
		public Long						P_OUT_ADVERTENCIASCONTADOR;
		public List<OperacionObjOut>	P_OUT_IDOPERACIONSPOT;
		
		public SalidaProcesarIngresoOperacionSpot() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_IDOPERACIONSPOT = new ArrayList<OperacionObjOut>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class Mesa{
		public Long     IdMesa;
		public String toString(){
			return Utils.printJson(this, false);
		}
		
	}
	
	public static class PuntaOperacion{
		public BigDecimal PuntaCompra;
		public BigDecimal PuntaVenta;
		public BigDecimal TipoCambioCosto;
		
		public PuntaOperacion() {
			this.PuntaCompra = BigDecimal.ZERO;
			this.PuntaVenta = BigDecimal.ZERO;
			this.TipoCambioCosto = BigDecimal.ZERO;
		}
		
		public String toString(){
			return Utils.printJson(this, false);
		}
	}
	public static class AsignacionSpot{
		public Long 		IdAsignacion;
		public String 		IdMonedaLiquidacion;
		public Long 		IdCliente;
		public Long 		IdPortafolio;
		public Date	        FechaMovimientoAsignacion;
		public BigDecimal 	MontoAsignadoLiquidacion;
		public String 		Glosa;
		public String toString(){
			return Utils.printJson(this, false);
		}

	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaProcesarIngresoOperacionSpot jin = gson.fromJson(in.toString(), EntradaProcesarIngresoOperacionSpot.class);
		SalidaProcesarIngresoOperacionSpot out = logica(conn,jin);

		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	@Trace
	public SalidaProcesarIngresoOperacionSpot logica(Connection con, EntradaProcesarIngresoOperacionSpot entrada) throws Exception{
		logger.info("INICIO::ProcesarIngresoOperacionSpot");
		SalidaProcesarIngresoOperacionSpot out = new SalidaProcesarIngresoOperacionSpot();
		String fechaSistema = obtenerFechaSistema(con,entrada.p_IdEmpresa);
		String monedaSistema = obtenerMonedaSistema(con,entrada.p_IdEmpresa);
		
		//MESA DEFAULT
		Long idMesaDefecto=obtenerMesa(con);
				
		//VALORES ANEXOS DEL SPOT
		//Valor T/C Costo y CIerre
		BigDecimal dolarObservado = obtenerDolarObservado(con,entrada.p_IdEmpresa,fechaSistema,monedaSistema);
		
		//Instrumento SPOT
		Long instrumentoSpot=obtenerIdInstrumentoSpot(con, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo);
		
		ConsultarPuntasPorTramoSalida consultarPuntasPorTramoSalida = obtenerPuntas(con,entrada.p_IdEmpresa);
		///OperaCION spot	
		CrearOperacionSpotSalida salida = crearOperacionesSpot(con,entrada,dolarObservado,idMesaDefecto,instrumentoSpot,consultarPuntasPorTramoSalida.P_OUT_PUNTAS, monedaSistema);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
		out.P_OUT_ADVERTENCIASCONTADOR = salida.P_OUT_ADVERTENCIASCONTADOR;
		out.P_OUT_IDOPERACIONSPOT = salida.P_OUT_IDOPERACIONSPOT;
		out.P_OUT_MENSAJES = salida.P_OUT_MENSAJES;
		
		logger.info("FIN::ProcesarIngresoOperacionSpot");
		return out;
	}
	
	private String obtenerFechaSistema(Connection con, Long idEmpresa) throws Exception {
		logger.trace("INICIO::ProcesarIngresoOperacionSpot.obtenerFechaSistema");
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		logger.trace("FIN::ProcesarIngresoOperacionSpot.obtenerFechaSistema");
		return fechaSistema;
	}
	
	private String obtenerMonedaSistema(Connection con, Long idEmpresa) throws Exception {
		logger.trace("INICIO::ProcesarIngresoOperacionSpot.obtenerMonedaSistema");
		String monedaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
		logger.trace("FIN::ProcesarIngresoOperacionSpot.obtenerMonedaSistema");
		return monedaSistema;
	}
	
	private Long obtenerMesa(Connection con) throws Exception {
		logger.trace("INICIO::ProcesarIngresoOperacionSpot.obtenerMesa");
		SQL sql = new SQL();
		Long idMesaDefecto=0L;
		HashMap<String,Object> params = new HashMap<String,Object>();
		logger.trace("INICIO::SQL_SELECT_MESA");
		List<Mesa> mesaDefecto = sql.query(con, SQL_SELECT_MESA,params,Mesa.class);
		logger.trace("FIN::SQL_SELECT_MESA");
		for (Mesa  mesa: mesaDefecto){
			idMesaDefecto=mesa.IdMesa;
		};
		logger.trace("FIN::ProcesarIngresoOperacionSpot.obtenerMesa");
		return idMesaDefecto;
	}
	
	public BigDecimal obtenerDolarObservado(Connection con, Long idEmpresa, String fechaSistema, String monedaSistema) throws Exception{
		logger.trace("INICIO::ProcesarIngresoOperacionSpot.obtenerDolarObservado");
		//VALORES ANEXOS DEL SPOT
		//Valor T/C Costo y CIerre
		HashMap<Long,Object> paridades = new HashMap<Long,Object>();
		CalcularParidad calcularParidad = new CalcularParidad();
		
		CalcularParidadEntrada calcularParidadEntrada = new CalcularParidadEntrada();
		calcularParidadEntrada.p_IdEmpresa = idEmpresa;
		calcularParidadEntrada.p_IdTrabajo = 1L;

		//VALOR DOLAR OBSERVADO
		CalculoEntrada calculoEntrada = new CalculoEntrada();
		calculoEntrada.IdTrabajo = 1L;
		calculoEntrada.IdInterno = -1L;
		calculoEntrada.Fecha = fechaSistema;
		calculoEntrada.IdMonedaOrigen = _c.Dolar;
		calculoEntrada.IdMonedaDestino = monedaSistema;
		calculoEntrada.MontoOrigen = BigDecimal.ONE;
		calculoEntrada.ValorCambioLocalModificado = BigDecimal.ZERO;
		calculoEntrada.IdUso = "";
		calcularParidadEntrada.p_CalculosEntrada.add(calculoEntrada);
		
		if(calcularParidadEntrada.p_CalculosEntrada.size() > 0){
			CalcularParidadSalida calculoSalida = calcularParidad.logica(con, calcularParidadEntrada);
			paridades = Utils.List2Hash(calculoSalida.p_out_CalculosSalida, "IdInterno");
		}
		CalculoSalida paridadDolar = (CalculoSalida) paridades.get(-1L);
		BigDecimal dolarObservado = paridadDolar == null ? BigDecimal.ZERO : paridadDolar.MontoDestino;
		logger.trace("FIN::ProcesarIngresoOperacionSpot.obtenerDolarObservado");
		return dolarObservado;
	}
	
	private Long obtenerIdInstrumentoSpot(Connection con, Long idEmpresa, Long idUsuario, String idModulo) throws Exception {
		logger.trace("INICIO::ProcesarIngresoOperacionSpot.obtenerIdInstrumentoSpot");
		ConsultarDatosInstrumentos  consultarInstrumento = new ConsultarDatosInstrumentos();
		ConsultarDatosInstrumento_Entrada  instrumentoEntrada = new ConsultarDatosInstrumento_Entrada();
		
		instrumentoEntrada.p_IdEmpresa=idEmpresa;
		instrumentoEntrada.p_IdUsuario=idUsuario;
		instrumentoEntrada.p_IdModulo=idModulo;
		instrumentoEntrada.p_CreaOpcionAutomatica= "N";
		FiltroTipoInstrumento tipoInstrumento = new FiltroTipoInstrumento();
		tipoInstrumento.Correlativo= 1L;
		tipoInstrumento.IdTipoInstrumento=_c.SpotTipoInstrumento;
		instrumentoEntrada.p_IdTipoInstrumentos.add(tipoInstrumento);
		instrumentoEntrada.p_IdClasificacionTipoInstrumentos=new ArrayList<>();
		ConsultaInstrumento instrumentos = new ConsultaInstrumento();
		instrumentos.Correlativo= 1L;
		instrumentos.IdInstrumento=null;
		instrumentos.Nemotecnico=_c.SpotNemo;
		instrumentoEntrada.p_Instrumentos.add(instrumentos);
		
		ConsultarDatosInstrumento_Salida instrumentoSalida = consultarInstrumento.logica(con, instrumentoEntrada);
		
		Long instrumentoSpot=0L;
		for(DatosInstrumento rs : instrumentoSalida.P_OUT_INSTRUMENTOS){
			instrumentoSpot=rs.IdInstrumento;
		}
		logger.trace("FIN::ProcesarIngresoOperacionSpot.obtenerIdInstrumentoSpot");
		return instrumentoSpot;
	}
	
	private ConsultarPuntasPorTramoSalida obtenerPuntas(Connection con, Long idEmpresa) throws Exception {
		logger.trace("INICIO::ProcesarIngresoOperacionSpot.obtenerPuntas");
		ConsultarPuntasPorTramo consultarPuntasPorTramo = new ConsultarPuntasPorTramo();
		ConsultarPuntasPorTramoEntrada consultarPuntasPorTramoEntrada = new ConsultarPuntasPorTramoEntrada();
		consultarPuntasPorTramoEntrada.p_IdEmpresa = idEmpresa;
		consultarPuntasPorTramoEntrada.p_MontoPorTramo = 0L;
		consultarPuntasPorTramoEntrada.p_IdMoneda = _c.Dolar;
		consultarPuntasPorTramoEntrada.p_Canal = _c.CanalPunta;
		
		ConsultarPuntasPorTramoSalida consultarPuntasPorTramoSalida = consultarPuntasPorTramo.logica(con, consultarPuntasPorTramoEntrada);
		logger.trace("FIN::ProcesarIngresoOperacionSpot.obtenerPuntas");
		return consultarPuntasPorTramoSalida;
	}
	
	private PuntaOperacion obtenerPuntaOperacion(List<PuntaPorTramoSalida> puntasOrigen, BigDecimal monto, String tipoOperacion) {
		logger.trace("INICIO::ProcesarIngresoOperacionSpot.obtenerPuntaOperacion");
		List<PuntaPorTramoSalida> puntas = new ArrayList<PuntaPorTramoSalida>(puntasOrigen);
		PuntaOperacion puntaOperacion = new PuntaOperacion();
		if(puntas.size() > 0) {
			PuntaPorTramoSalida puntaTramoPrincipal = puntas.get(0);
			if(monto.compareTo(puntaTramoPrincipal.MontoPorTramo) == -1) {
				puntaOperacion.PuntaCompra = puntaTramoPrincipal.PuntaCompra;
				puntaOperacion.PuntaVenta = puntaTramoPrincipal.PuntaVenta;
				puntaOperacion.TipoCambioCosto = obtenerTipoCambioCosto(puntaTramoPrincipal,tipoOperacion);
			}else {
				Collections.reverse(puntas);
				Optional<PuntaPorTramoSalida> puntaOperacionFiltro = puntas.parallelStream().filter(punta->monto.compareTo(punta.MontoPorTramo) != -1).findFirst();
				if(puntaOperacionFiltro.isPresent()) {
					PuntaPorTramoSalida puntaOperacionEncontrada = puntaOperacionFiltro.get();
					puntaOperacion.PuntaCompra = puntaOperacionEncontrada.PuntaCompra;
					puntaOperacion.PuntaVenta = puntaOperacionEncontrada.PuntaVenta;
					puntaOperacion.TipoCambioCosto = obtenerTipoCambioCosto(puntaOperacionEncontrada,tipoOperacion);
				}
			}
		}
		logger.trace("FIN::ProcesarIngresoOperacionSpot.obtenerPuntaOperacion");
		return puntaOperacion;
	}
	
	private BigDecimal obtenerTipoCambioCosto(PuntaPorTramoSalida punta, String tipoOperacion) {
		logger.trace("INICIO::ProcesarIngresoOperacionSpot.obtenerTipoCambioCosto");
		BigDecimal tipoCambioCosto = BigDecimal.ZERO;
		if(_c.SpotCompra.equals(tipoOperacion)){
			tipoCambioCosto = punta.PuntaCompra;
		}else if (_c.SpotVenta.equals(tipoOperacion)) {
			tipoCambioCosto = punta.PuntaVenta;
		}
		logger.trace("FIN::ProcesarIngresoOperacionSpot.obtenerTipoCambioCosto");
		return tipoCambioCosto;
	}
	
	private List<OperacionObjIn> obtenerOperacionSpot(Connection con, Long IdEmpresa, List<AsignacionSpot> asignaciones, BigDecimal dolarObservado, Long idMesaDefecto, Long instrumentoSpot, List<PuntaPorTramoSalida> puntas, String monedaSistema){
		logger.trace("INICIO::ProcesarIngresoOperacionSpot.obtenerOperacionSpot");
		List<OperacionObjIn> operacionesSpot = asignaciones.stream().map(asig->{
			Timestamp fechaLiquidacion;
			try 
			{
				int segundosZonaHoraria = FechaDB.ObtenerSegundosZonaHoraria(con, IdEmpresa);
		        Fecha.setLocalOffSet(segundosZonaHoraria);	
		        fechaLiquidacion 	= Fecha.fechaSistemaHoraLocal_fechaHoraUTC(asig.FechaMovimientoAsignacion.toString(),FechaDB.ObtenerHoraSistemaLocal(con,IdEmpresa));					
				
			}
			catch(Exception e)
			{
				fechaLiquidacion 	= new  Timestamp(asig.FechaMovimientoAsignacion.getTime());	
			}
			OperacionObjIn sp = new OperacionObjIn();
			sp.IdTipoOperacion = _c.SpotVenta;
			sp.NumeroOperacion= asig.IdAsignacion+""; //validar
			sp.IdInstrumento= instrumentoSpot;//    (long) 346390;  				
			sp.IdTipoProducto =_c.SpotProducto;
			sp.IdCliente=asig.IdCliente; 
			sp.IdPortafolio=asig.IdPortafolio;
			sp.FechaOperacion= fechaLiquidacion;
			sp.IdMonedaPrincipal= _c.Dolar;
			sp.IdMonedaSecundaria= monedaSistema;
			sp.MontoMonedaPrincipal=asig.MontoAsignadoLiquidacion;
			PuntaOperacion puntaOperacion = obtenerPuntaOperacion(puntas,sp.MontoMonedaPrincipal,sp.IdTipoOperacion);
			sp.MontoMonedaSecundaria= asig.MontoAsignadoLiquidacion.multiply(dolarObservado);
			sp.TipoCambioCierre = dolarObservado;
			sp.TipoCambioCierreForward= BigDecimal.ZERO;
			sp.ParidadCierre=BigDecimal.ZERO;
			sp.ParidadCierreForward= BigDecimal.ZERO;
			sp.TipoCambioCosto= puntaOperacion.TipoCambioCosto;
			sp.ParidadCosto= BigDecimal.ZERO;
			sp.MontoEquivalente= BigDecimal.ZERO;
			sp.MontoRentabilidad= BigDecimal.ZERO;
			sp.MontoRentabilidadCanal= BigDecimal.ZERO;
			sp.Margen= BigDecimal.ZERO;
			sp.Utilidad= BigDecimal.ZERO;
			sp.PuntaCompra= puntaOperacion.PuntaCompra;
			sp.PuntaVenta= puntaOperacion.PuntaVenta;
			sp.SolicitudTipoCambio="N";
			sp.IdMesa=idMesaDefecto;//(long)6; 										//consultar mesas
			sp.IdModulo=_c.ID_MODULO_ORDENES;
			sp.IdTipoDolar=(long)4; 								//Buscar tipo dolar
			sp.TipoComprobanteFacturacion=_c.SpotTipoComprobanteFacturacion;
			sp.IdAgente=(long) 0;
			sp.BenOur=_c.SpotBenOur;
			sp.EquivalenteConGasto= BigDecimal.ZERO;
			sp.TipoCambioCierreConGasto= BigDecimal.ZERO;
			sp.Observacion=asig.Glosa;
			sp.CodigoComercio=_c.SpotCodigoComercio;				//consultar comercio
			sp.FolioPlanilla=(long)0;								
			sp.FolioInterfaz="0";
			sp.OperacionFicticia="N";
			sp.IdTipoCanalIngreso = _c.TipoCanalIngreso;
			sp.IdDatatec="0";
			sp.Estado = _c.SpotEstadoVigente;
			return sp;
		}).collect(Collectors.toList());
		logger.trace("FIN::ProcesarIngresoOperacionSpot.obtenerOperacionSpot");
		return operacionesSpot;
	}
	
	private List<FormaPagoObjIn> obtenerFormasDePago(List<FormaPagoSpot> datosFormaPago){
		logger.trace("INICIO::ProcesarIngresoOperacionSpot.obtenerFormasDePago");
		List<FormaPagoObjIn> formaPagoSpot = datosFormaPago.parallelStream().map(asig->{
			FormaPagoObjIn pa = new FormaPagoObjIn();
			pa.IdInternoPagoOperacion = asig.IdInternoPagoOperacion ;
			pa.IdComprobantePagoOperacion=asig.IdComprobantePagoOperacion;
			pa.IdTipoMovimiento=asig.IdTipoMovimiento;
			pa.IdFormaDePago=asig.IdFormaDePago;
			pa.IdMoneda=asig.IdMoneda;
			pa.IdMonedaLiquidacion=asig.IdMonedaLiquidacion;
			pa.IdOperacion=asig.IdOperacion;
			pa.RecibePagaPortafolio=asig.RecibePagaPortafolio;
			pa.MontoFormaDePago=asig.MontoFormaDePago;
			pa.AfectarTesoreria=asig.AfectarTesoreria;
			pa.FechaLiquidacion=asig.FechaLiquidacion;
			pa.IdCuentaBancaria=asig.IdCuentaBancaria;
			return pa;
		}).collect(Collectors.toList());
		logger.trace("FIN::ProcesarIngresoOperacionSpot.obtenerFormasDePago");
		return formaPagoSpot;
	}
	
	private CrearOperacionSpotSalida crearOperacionesSpot(Connection con, EntradaProcesarIngresoOperacionSpot entrada, BigDecimal dolarObservado, Long idMesaDefecto, Long instrumentoSpot, List<PuntaPorTramoSalida> puntas, String monedaSistema) throws Exception {
		logger.trace("INICIO::ProcesarIngresoOperacionSpot.crearOperacionesSpot");
		CrearOperacionSpot CrearOperacionSpot = new CrearOperacionSpot();
		CrearOperacionSpotEntrada entradaCrearOperacionSpot = new CrearOperacionSpotEntrada();
		
	
		entradaCrearOperacionSpot.p_DatosOperacion = obtenerOperacionSpot(con, entrada.p_IdEmpresa, entrada.p_DatosOperacion,dolarObservado,idMesaDefecto,instrumentoSpot,puntas,monedaSistema);
		
		entradaCrearOperacionSpot.p_DatosFormaPago = obtenerFormasDePago(entrada.p_DatosFormaPago);
		
		entradaCrearOperacionSpot.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCrearOperacionSpot.p_IdUsuario = entrada.p_IdUsuario;
		entradaCrearOperacionSpot.p_CalculaMargen = _c.SpotCalculaMargen;
		entradaCrearOperacionSpot.p_IdModulo = entrada.p_IdModulo;
		entradaCrearOperacionSpot.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
		entradaCrearOperacionSpot.p_PermiteOperarProspectos = entrada.p_PermiteOperarProspectos;
		CrearOperacionSpotSalida salida = CrearOperacionSpot.logica(con, entradaCrearOperacionSpot);
		logger.trace("FIN::ProcesarIngresoOperacionSpot.crearOperacionesSpot");
		return salida;
	}
	
}
