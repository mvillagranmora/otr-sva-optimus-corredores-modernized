package cl.bcs.ordenes;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.ordenes.AnularTransaccionMasiva.EntradaAnularTransaccionMasiva;
import cl.bcs.ordenes.AnularTransaccionMasiva.SalidaAnularTransaccionMasiva;
import cl.bcs.ordenes.CrearTransacciones.CrearTransacciones_Entrada;
import cl.bcs.ordenes.CrearTransacciones.CrearTransacciones_Salida;
import cl.bcs.ordenes.ModificarTransaccion.EntradaModificarTransaccion;
import cl.bcs.ordenes.ModificarTransaccion.SalidaModificarTransaccion;
import cl.bcs.ordenes.pojo.TransaccionHistorico;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ReprocesarTransaccion implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(ReprocesarTransaccion.class);
	
	private final String SQL_SELECT_TRANSACCIONES_LOG = loadResourceToString("cl/bcs/ordenes/sql/ReprocesarTransaccion_SelectTransacciones.sql");
	private final String SQL_UPDATE_TRANSACCIONES = loadResourceToString("cl/bcs/ordenes/sql/ReprocesarTransaccion_UpdateTransacciones.sql");
	
	public static class EntradaReprocesarTransaccion{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public List<Long> p_Transacciones;
		
		public EntradaReprocesarTransaccion() {
			this.p_Transacciones = new ArrayList<Long>();
		}
	}
	
	public static class SalidaReprocesarTransaccion{
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaReprocesarTransaccion() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaReprocesarTransaccion jin = gson.fromJson(in.toString(), EntradaReprocesarTransaccion.class);
		SalidaReprocesarTransaccion out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaReprocesarTransaccion logica(Connection con, EntradaReprocesarTransaccion entrada) throws Exception {
		logger.info("INICIO::SalidaReprocesarTransaccion");
		SalidaReprocesarTransaccion out = new SalidaReprocesarTransaccion();
		
		Map<Long, TransaccionHistorico> transaccionesHistoricas = buscarTransaccionesHistoricas(con,entrada);
		List<TransaccionHistorico> transaccionesValidas = obtenerTransaccionesValidas(entrada, transaccionesHistoricas);
		List<Long> transaccionesInvalidas = obtenerTransaccionesInvalidas(entrada,transaccionesHistoricas);
		
		out.P_OUT_MENSAJES.addAll(obtenerErrores(con,transaccionesInvalidas,entrada));
		out.P_OUT_ERRORESCONTADOR += new Long(transaccionesInvalidas.size());
		
		List<TransaccionHistorico> transaccionesIngresadas = buscarTransaccionSegunTipoRegistro(transaccionesValidas,"ING");
		List<TransaccionHistorico> transaccionesModificadas = buscarTransaccionSegunTipoRegistro(transaccionesValidas,"MOD");
		List<TransaccionHistorico> transaccionesAnuladas = buscarTransaccionSegunTipoRegistro(transaccionesValidas,"ANU");
		
		CrearTransacciones_Salida salidaCrearTransaccion = crearTransaccion(con,transaccionesIngresadas,entrada);
		SalidaModificarTransaccion salidaModificarTransaccion = modificarTransaccion(con,transaccionesModificadas,entrada);
		SalidaAnularTransaccionMasiva salidaAnularTransaccion = anularTransaccion(con,transaccionesAnuladas,entrada);
		
		out.P_OUT_ERRORESCONTADOR += salidaCrearTransaccion.P_OUT_ERRORESCONTADOR;
		out.P_OUT_MENSAJES.addAll(salidaCrearTransaccion.P_OUT_MENSAJES);
		out.P_OUT_ERRORESCONTADOR += salidaModificarTransaccion.P_OUT_ERRORESCONTADOR;
		out.P_OUT_MENSAJES.addAll(salidaModificarTransaccion.P_OUT_MENSAJES);
		out.P_OUT_ERRORESCONTADOR += salidaAnularTransaccion.P_OUT_ERRORESCONTADOR;
		out.P_OUT_MENSAJES.addAll(salidaAnularTransaccion.P_OUT_MENSAJES);
		
		if(out.P_OUT_ERRORESCONTADOR.equals(0L)) {
			actualizarTransaccion(con,transaccionesValidas);
		}
		
		logger.info("FIN::SalidaReprocesarTransaccion");
		return out;
	}
	
	private Map<Long, TransaccionHistorico> buscarTransaccionesHistoricas(Connection con, EntradaReprocesarTransaccion entrada) throws Exception{
		logger.info("INICIO::SalidaReprocesarTransaccion.buscarTransaccionesHistoricas");
		SQL sql=new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdTransaccionHistorico", entrada.p_Transacciones);
		Map<Long, TransaccionHistorico> transacciones = sql.query(con, SQL_SELECT_TRANSACCIONES_LOG, params, TransaccionHistorico.class).parallelStream().collect(Collectors.toMap(transaccion->transaccion.IdTransaccionHistorico, transaccion->transaccion));
		logger.info("FIN::SalidaReprocesarTransaccion.buscarTransaccionesHistoricas");
		return transacciones;
	}
	
	private List<TransaccionHistorico> obtenerTransaccionesValidas(EntradaReprocesarTransaccion entrada, Map<Long, TransaccionHistorico> transacciones){
		logger.info("INICIO::SalidaReprocesarTransaccion.obtenerTransaccionesValidas");
		List<TransaccionHistorico> transaccionesValidas = entrada.p_Transacciones.parallelStream()
		.filter(idTransaccion->transacciones.get(idTransaccion) != null && "PEN".equals(transacciones.get(idTransaccion).IdEstadoRegistro))
		.map(idTransaccion->{
			return transacciones.get(idTransaccion);
		}).collect(Collectors.toList());
		logger.info("FIN::SalidaReprocesarTransaccion.obtenerTransaccionesValidas");
		return transaccionesValidas;
	}
	
	private List<Long> obtenerTransaccionesInvalidas(EntradaReprocesarTransaccion entrada, Map<Long, TransaccionHistorico> transacciones){
		logger.info("INICIO::SalidaReprocesarTransaccion.obtenerTransaccionesInvalidas");
		List<Long> transaccionesInvalidas = entrada.p_Transacciones.parallelStream()
		.filter(idTransaccion->transacciones.get(idTransaccion) == null || !"PEN".equals(transacciones.get(idTransaccion).IdEstadoRegistro))
		.collect(Collectors.toList());
		logger.info("FIN::SalidaReprocesarTransaccion.obtenerTransaccionesInvalidas");
		return transaccionesInvalidas;
	}
	
	private List<MensajeSalida> obtenerErrores(Connection con, List<Long> transaccionesInvalidas, EntradaReprocesarTransaccion entrada) throws Exception{
		logger.info("INICIO::SalidaReprocesarTransaccion.obtenerErrores");
		List<MensajeSalida> mensajes = new ArrayList<MensajeSalida>();
		for(Long idTransaccion : transaccionesInvalidas) {
			HashMap<String,String> params = new HashMap<String,String>();
			params.put("IdTransaccion", String.valueOf(idTransaccion));
			mensajes.add(new MensajeSalida(idTransaccion,MensajeSalida.MENSAJE_ERROR,"TRANSACCION_HISTORICA_INVALIDA",params,con,entrada.p_IdEmpresa));
		}
		logger.info("FIN::SalidaReprocesarTransaccion.obtenerErrores");
		return mensajes;
	}
	
	private CrearTransacciones_Salida crearTransaccion(Connection con, List<TransaccionHistorico> transaccionesCreacion, EntradaReprocesarTransaccion entrada) throws Exception {
		logger.info("INICIO::SalidaReprocesarTransaccion.crearTransaccion");
		CrearTransacciones_Salida salida = new CrearTransacciones_Salida();
		if(transaccionesCreacion.size() > 0) {
			CrearTransacciones crearTransacciones = new CrearTransacciones();
			CrearTransacciones_Entrada crearTransaccionesEntrada = new CrearTransacciones_Entrada();
			crearTransaccionesEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
			crearTransaccionesEntrada.p_IdModulo = entrada.p_IdModulo;
			crearTransaccionesEntrada.p_IdUsuario = entrada.p_IdUsuario;
			crearTransaccionesEntrada.p_UsaReglasNegocio = "S";
			crearTransaccionesEntrada.p_simularOperacion = "N";
			crearTransaccionesEntrada.p_Reproceso = "S";
			crearTransaccionesEntrada.p_Transacciones = transaccionesCreacion.parallelStream().map(transaccion->{
				CrearTransacciones.Transaccion_Entrada transaccionCrear = new CrearTransacciones.Transaccion_Entrada();
				transaccionCrear.IdMoneda = transaccion.IdMoneda;
				transaccionCrear.IdMonedaEmision = transaccion.IdMonedaEmision;
				transaccionCrear.IdInterno = transaccion.IdTransaccionHistorico;
				transaccionCrear.FolioTransaccion = transaccion.Folio;
				transaccionCrear.FechaCreacion = Fecha.stringToSqlDate(transaccion.FechaSistemaCreacion);
				transaccionCrear.HoraCreacion = Fecha.toStringTime(transaccion.HoraSistemaCreacion);
				transaccionCrear.FechaEmision = Fecha.stringToSqlDate(transaccion.FechaEmision);
				transaccionCrear.Plazo = transaccion.Plazo;
				transaccionCrear.EsCuentaPropia = transaccion.EsCuentaPropia;
				transaccionCrear.EsFungible = transaccion.EsFungible;
				transaccionCrear.EsVentaCorta = transaccion.EsVentaCorta;
				transaccionCrear.CodigoOperadorContraparte = transaccion.CodigoOperadorContraparte;
				transaccionCrear.CodigoOperadorCorredor = transaccion.CodigoOperadorCorredor;
				transaccionCrear.IdSistemaBursatil = transaccion.IdSistemaBursatil;
				transaccionCrear.IdEmisor = transaccion.IdEmisor;
				transaccionCrear.Cantidad = transaccion.Cantidad;
				transaccionCrear.Monto = transaccion.Monto;
				transaccionCrear.Precio = transaccion.Precio;
				transaccionCrear.Tasa = transaccion.Tasa;
				transaccionCrear.MontoPlazo = transaccion.MontoPlazo;
				transaccionCrear.PrecioPlazo = transaccion.PrecioPlazo;
				transaccionCrear.ValorRescate = transaccion.ValorRescate;
				transaccionCrear.IdTipoReajuste = transaccion.IdTipoReajuste;
				transaccionCrear.IdEmpleado = transaccion.IdEmpleado;
				transaccionCrear.IdTipoOperacion = transaccion.IdTipoOperacion;
				transaccionCrear.IdCondicionDeLiquidacionContado = transaccion.IdCondicionDeLiquidacion;
				transaccionCrear.FechaLiquidacionContado = Fecha.stringToSqlDate(transaccion.FechaLiquidacion);
				transaccionCrear.FechaVencimiento = Fecha.stringToSqlDate(transaccion.FechaVencimiento);
				transaccionCrear.IdInstrumento = transaccion.IdInstrumento;
				transaccionCrear.IdCorredoraContraparte = transaccion.IdCorredorContraparte;
				transaccionCrear.IdBolsa = transaccion.IdBolsa;
				transaccionCrear.IdBoveda = transaccion.IdBoveda;
				transaccionCrear.IdLibroOrdenes = transaccion.IdLibroOrdenes;
				transaccionCrear.IdInstrumentoSubyacente = transaccion.IdInstrumentoSubyacente;
				transaccionCrear.EsFueraRueda = transaccion.EsFueraRueda;
				transaccionCrear.IndicadorDCV = transaccion.IndicadorDCV;
				transaccionCrear.EsBilateral = transaccion.EsBilateral;
				transaccionCrear.MarcaNegociacion = transaccion.MarcaNegociacion;
				transaccionCrear.TipoCambioCorresponsal = transaccion.TipoCambioCorresponsal;
				transaccionCrear.IdCorredoraCorresponsal = transaccion.IdCorredorCorresponsal;
				transaccionCrear.MontoLiquidacionCorresponsal = transaccion.MontoLiquidacionCorresponsal;
				transaccionCrear.IdMonedaLiquidacionCorresponsal = transaccion.IdMonedaLiquidacionCorresponsal;
				transaccionCrear.FolioTransaccionPlazo = transaccion.FolioTransaccionPlazo;
				transaccionCrear.MejorPrecioCompra = transaccion.MejorPrecioCompra;
				transaccionCrear.MejorPrecioVenta = transaccion.MejorPrecioVenta;
				transaccionCrear.NemotecnicoEspecificoBolson = transaccion.Nemotecnico;
				transaccionCrear.IndicadorOrdenDirecta = transaccion.IndicadorOrdenDirecta;
				transaccionCrear.NemotecnicoSVS = transaccion.NemotecnicoSVS;
				transaccionCrear.Intereses = transaccion.Intereses;
				transaccionCrear.Reajustes = transaccion.Reajustes;	
				transaccionCrear.GastosCorresponsal = transaccion.GastosCorresponsal;
				transaccionCrear.FlujoOrigen = transaccion.FlujoOrigen;
				return transaccionCrear;
			}).collect(Collectors.toList());
			
			salida = crearTransacciones.logica(con, crearTransaccionesEntrada);
			salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
		}
		
		logger.info("FIN::SalidaReprocesarTransaccion.crearTransaccion");
		return salida;
	}
	
	private SalidaModificarTransaccion modificarTransaccion(Connection con, List<TransaccionHistorico> transaccionesModificacion, EntradaReprocesarTransaccion entrada) throws Exception {
		logger.info("INICIO::SalidaReprocesarTransaccion.modificarTransaccion");
		SalidaModificarTransaccion salida = new SalidaModificarTransaccion();
		if(transaccionesModificacion.size() > 0) {
			ModificarTransaccion modificarTransaccion = new ModificarTransaccion();
			EntradaModificarTransaccion entradaModificarTransaccion = new EntradaModificarTransaccion();
			entradaModificarTransaccion.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaModificarTransaccion.p_IdModulo = entrada.p_IdModulo;
			entradaModificarTransaccion.p_IdUsuario = entrada.p_IdUsuario;
			entradaModificarTransaccion.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
			entradaModificarTransaccion.p_Reproceso = "S";
			entradaModificarTransaccion.p_Transacciones = transaccionesModificacion.parallelStream().map(transaccion->{
				ModificarTransaccion.InTransaccion transaccionModificar = new ModificarTransaccion.InTransaccion();
				transaccionModificar.IdTransaccion = transaccion.IdTransaccion;
				transaccionModificar.Monto = transaccion.Monto;
				transaccionModificar.Precio = transaccion.Precio;
				transaccionModificar.Tasa = transaccion.Tasa;
				transaccionModificar.IdLibroOrdenes = transaccion.IdLibroOrdenes;
				transaccionModificar.ValorRescate = "IF".equals(transaccion.IdClasificacionTipoInstrumento) ? transaccion.ValorRescate : null;
				transaccionModificar.Cantidad = transaccion.Cantidad;
				transaccionModificar.FechaEmision = Fecha.toStringDate(transaccion.FechaEmision);
				transaccionModificar.EsBilateral = transaccion.EsBilateral;
				transaccionModificar.FechaLiquidacion = Fecha.toStringDate(transaccion.FechaLiquidacion);
				transaccionModificar.AccionTipoOperacion = transaccion.AccionTipoOperacion;
				transaccionModificar.TipoCambioCorresponsal = transaccion.TipoCambioCorresponsal;
				transaccionModificar.IdCorredorCorresponsal = transaccion.IdCorredorCorresponsal;
				transaccionModificar.MontoLiquidacionCorresponsal = transaccion.MontoLiquidacionCorresponsal;
				transaccionModificar.GastosCorresponsal = transaccion.GastosCorresponsal;
				transaccionModificar.IdMonedaLiquidacionCorresponsal = transaccion.IdMonedaLiquidacionCorresponsal;
				transaccionModificar.IdCamara = transaccion.IdCamara;
				transaccionModificar.MarcaNegociacion = transaccion.MarcaNegociacion;
				return transaccionModificar;
			}).collect(Collectors.toList());
			
			salida = modificarTransaccion.logica(con, entradaModificarTransaccion);
		}
		logger.info("FIN::SalidaReprocesarTransaccion.modificarTransaccion");
		return salida;
	}
	
	private SalidaAnularTransaccionMasiva anularTransaccion(Connection con, List<TransaccionHistorico> transaccionesAnulacion, EntradaReprocesarTransaccion entrada) throws Exception {
		logger.info("INICIO::SalidaReprocesarTransaccion.anularTransaccion");
		SalidaAnularTransaccionMasiva salida = new SalidaAnularTransaccionMasiva();
		if(transaccionesAnulacion.size() > 0) {
			AnularTransaccionMasiva anularTransaccionMasiva = new AnularTransaccionMasiva();
			EntradaAnularTransaccionMasiva entradaAnularTransaccionMasiva = new EntradaAnularTransaccionMasiva();
			entradaAnularTransaccionMasiva.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaAnularTransaccionMasiva.p_IdModulo = entrada.p_IdModulo;
			entradaAnularTransaccionMasiva.p_IdUsuario = entrada.p_IdUsuario;
			entradaAnularTransaccionMasiva.p_Reproceso = "S";
			entradaAnularTransaccionMasiva.p_transacciones = transaccionesAnulacion.parallelStream().map(transaccion->{
				AnularTransaccionMasiva.InTransaccion transaccionAnular = new AnularTransaccionMasiva.InTransaccion();
				transaccionAnular.IdTransaccion = transaccion.IdTransaccion;
				return transaccionAnular;
			}).collect(Collectors.toList());
			salida = anularTransaccionMasiva.logica(con, entradaAnularTransaccionMasiva);
		}
		logger.info("FIN::SalidaReprocesarTransaccion.anularTransaccion");
		return salida;
	}
	
	private List<TransaccionHistorico> buscarTransaccionSegunTipoRegistro(List<TransaccionHistorico> transacciones, String tipoRegistro){
		logger.info("INICIO::SalidaReprocesarTransaccion.buscarTransaccionSegunTipoRegistro");
		List<TransaccionHistorico> transaccionesSegunTipoRegistro = transacciones.parallelStream().filter(transaccion->tipoRegistro.equals(transaccion.TipoRegistro)).collect(Collectors.toList());
		logger.info("FIN::SalidaReprocesarTransaccion.buscarTransaccionSegunTipoRegistro");
		return transaccionesSegunTipoRegistro;
	}
	
	private void actualizarTransaccion(Connection con, List<TransaccionHistorico> transacciones) throws Exception {
		logger.info("INICIO::SalidaReprocesarTransaccion.actualizarTransaccion");
		SQL sql=new SQL();
		sql.batch(con, SQL_UPDATE_TRANSACCIONES, transacciones, TransaccionHistorico.class);
		logger.info("FIN::SalidaReprocesarTransaccion.actualizarTransaccion");
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) { //-->FIJARSE EN QUE ENTORNO SE ESTA PROBANDO<--
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
		String jsonRF = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"ORD\",\"p_UsaReglasNegocio\":\"S\",\"p_Transacciones\":[\"142\"]}";
		EntradaReprocesarTransaccion Objin = gson.fromJson(jsonRF, EntradaReprocesarTransaccion.class);
		ReprocesarTransaccion demo=new ReprocesarTransaccion();				
		try {
			SalidaReprocesarTransaccion ObjOut=demo.logica(con, Objin);
			con.rollback();
			System.out.println(ObjOut.toString());
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
