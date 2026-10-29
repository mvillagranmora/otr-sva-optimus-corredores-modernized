package cl.bcs.ordenes;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.ordenes.pojo.Transaccion;
import cl.bcs.ordenes.pojo.TransaccionHistorico;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearTransaccionesHistoricas implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(CrearTransaccionesHistoricas.class);
	
	private final String SQL_INSERT_TRANSACCIONES = loadResourceToString("cl/bcs/ordenes/sql/CrearTransaccionesHistoricas_InsertTransacciones.sql");
	private final String SQL_UPDATE_TRANSACCIONES = loadResourceToString("cl/bcs/ordenes/sql/CrearTransaccionesHistoricas_UpdateTransacciones.sql");
	
	public static class EntradaCrearTransaccionesHistoricas{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public List<InTransaccionHistorica> p_Transacciones;
		
		public EntradaCrearTransaccionesHistoricas() {
			this.p_Transacciones = new ArrayList<InTransaccionHistorica>();
		}
	
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InTransaccionHistorica{
		
		public String TipoRegistro;
		public String IdEstadoRegistro;
		public Transaccion Transaccion;
		public String EsModificacionMarketData;
		public Long IdTransaccionHistoricoAnulacion;
		
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaCrearTransaccionesHistoricas{
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaCrearTransaccionesHistoricas() {
			this.P_OUT_ERRORESCONTADOR=0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaCrearTransaccionesHistoricas jin = gson.fromJson(in.toString(), EntradaCrearTransaccionesHistoricas.class);
		SalidaCrearTransaccionesHistoricas out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaCrearTransaccionesHistoricas logica(Connection con, EntradaCrearTransaccionesHistoricas entrada) throws Exception {
		logger.info("INICIO::CrearTransaccionesHistoricas");
		SalidaCrearTransaccionesHistoricas out = new SalidaCrearTransaccionesHistoricas();
		
		SQL sql = new SQL();
		
		List<Long> secuencias = Utils.getSecuencia(con, "BSCL.Orden.BD.Secuencias::TransaccionHistorico", new Long(entrada.p_Transacciones.size()));
		
		AtomicInteger index = new AtomicInteger(0);
		
		List<TransaccionHistorico> transaccionesHistoricas = entrada.p_Transacciones.parallelStream().map(inTransaccion->{
			TransaccionHistorico transaccionHistorico = crearTransaccionHistoricaAPartirDeTransaccion(inTransaccion.Transaccion);
			transaccionHistorico.IdTransaccionHistorico = secuencias.get(index.getAndIncrement());
			transaccionHistorico.TipoRegistro = inTransaccion.TipoRegistro;
			transaccionHistorico.IdEstadoRegistro = inTransaccion.IdEstadoRegistro;
			transaccionHistorico.EsModificacionMarketData = inTransaccion.EsModificacionMarketData;
			transaccionHistorico.IdTransaccionHistoricoAnulacion = inTransaccion.IdTransaccionHistoricoAnulacion;
			
			return transaccionHistorico;
		}).collect(Collectors.toList());
		
		sql.batch(con, SQL_UPDATE_TRANSACCIONES, transaccionesHistoricas, TransaccionHistorico.class);
		sql.batch(con, SQL_INSERT_TRANSACCIONES, transaccionesHistoricas, TransaccionHistorico.class);
		
		logger.info("FIN::CrearTransaccionesHistoricas");
		return out;
	}
	
	private TransaccionHistorico crearTransaccionHistoricaAPartirDeTransaccion(Transaccion transaccion) {
		logger.info("INICIO::CrearTransaccionesHistoricas.crearTransaccionHistoricaAPartirDeTransaccion");
		TransaccionHistorico transaccionHistorico = new TransaccionHistorico();
		
		transaccionHistorico.IdTransaccion = transaccion.IdTransaccion;
		transaccionHistorico.Folio = transaccion.Folio;
		transaccionHistorico.FechaSistemaCreacion = transaccion.FechaSistemaCreacion;
		transaccionHistorico.HoraSistemaCreacion = transaccion.HoraSistemaCreacion;
		transaccionHistorico.FechaOperacion = transaccion.FechaOperacion;
		transaccionHistorico.FechaModificacionOperacion = transaccion.FechaModificacionOperacion;
		transaccionHistorico.FechaLiquidacion = transaccion.FechaLiquidacion;
		transaccionHistorico.FechaEmision = transaccion.FechaEmision;
		transaccionHistorico.FechaVencimiento = transaccion.FechaVencimiento;
		transaccionHistorico.FechaIngresoOferta = transaccion.FechaIngresoOferta;
		transaccionHistorico.FechaIngresoRegistro = transaccion.FechaIngresoRegistro;
		transaccionHistorico.IdSistemaBursatil = transaccion.IdSistemaBursatil;
		transaccionHistorico.EsCuentaPropia = transaccion.EsCuentaPropia;
		transaccionHistorico.EsFungible = transaccion.EsFungible;
		transaccionHistorico.EsAPlazo = transaccion.EsAPlazo;
		transaccionHistorico.EsVentaCorta = transaccion.EsVentaCorta;
		transaccionHistorico.CodigoOperadorContraparte = transaccion.CodigoOperadorContraparte;
		transaccionHistorico.IdEmisor = transaccion.IdEmisor;
		transaccionHistorico.Cantidad = transaccion.Cantidad;
		transaccionHistorico.Monto = transaccion.Monto;
		transaccionHistorico.Precio = transaccion.Precio;
		transaccionHistorico.Tasa = transaccion.Tasa;
		transaccionHistorico.Plazo = transaccion.Plazo;
		transaccionHistorico.Nemotecnico = transaccion.Nemotecnico;
		transaccionHistorico.NemotecnicoSVS = transaccion.NemotecnicoSVS;
		transaccionHistorico.ValorRescate = transaccion.ValorRescate;
		transaccionHistorico.IdEstado = transaccion.IdEstado;
		transaccionHistorico.IdTransaccionPlazo = transaccion.IdTransaccionPlazo;
		transaccionHistorico.IdTransaccionOd = transaccion.IdTransaccionOd;
		transaccionHistorico.IdTipoInstrumento = transaccion.IdTipoInstrumento;
		transaccionHistorico.IdClasificacionTipoInstrumento = transaccion.IdClasificacionTipoInstrumento;
		transaccionHistorico.IdTipoReajuste = transaccion.IdTipoReajuste;
		transaccionHistorico.IdEmpleado = transaccion.IdEmpleado;
		transaccionHistorico.IdTipoOperacion = transaccion.IdTipoOperacion;
		transaccionHistorico.IdCondicionDeLiquidacion = transaccion.IdCondicionDeLiquidacion;
		transaccionHistorico.IdMoneda = transaccion.IdMoneda;
		transaccionHistorico.Reservado = transaccion.Reservado;
		transaccionHistorico.IdUsuarioReserva = transaccion.IdUsuarioReserva;
		transaccionHistorico.MotivoReserva = transaccion.MotivoReserva;
		transaccionHistorico.Bloqueado = transaccion.Bloqueado;
		transaccionHistorico.IdUsuarioBloqueo = transaccion.IdUsuarioBloqueo;
		transaccionHistorico.MotivoBloqueo = transaccion.MotivoBloqueo;
		transaccionHistorico.IdEmpresa = transaccion.IdEmpresa;
		transaccionHistorico.IdBolsa = transaccion.IdBolsa;
		transaccionHistorico.IdLibroOrdenes = transaccion.IdLibroOrdenes;
		transaccionHistorico.IdInstrumento = transaccion.IdInstrumento;
		transaccionHistorico.NemotecnicoSubyacente = transaccion.NemotecnicoSubyacente;
		transaccionHistorico.IdInstrumentoSubyacente = transaccion.IdInstrumentoSubyacente;
		transaccionHistorico.IdMonedaSubyacente = transaccion.IdMonedaSubyacente;
		transaccionHistorico.FechaParidad = transaccion.FechaParidad;
		transaccionHistorico.ValorParidad = transaccion.ValorParidad;
		transaccionHistorico.FechaTipoCambio = transaccion.FechaTipoCambio;
		transaccionHistorico.ValorTipoCambio = transaccion.ValorTipoCambio;
		transaccionHistorico.IdCarga = transaccion.IdCarga;
		transaccionHistorico.IdComprobanteTesoreria = transaccion.IdComprobanteTesoreria;
		transaccionHistorico.MarcaNegociacion = transaccion.MarcaNegociacion;
		transaccionHistorico.IndicadorDCV = transaccion.IndicadorDCV;
		transaccionHistorico.EsBilateral = transaccion.EsBilateral;
		transaccionHistorico.IdCamara = transaccion.IdCamara;
		transaccionHistorico.TipoCambioCorresponsal = transaccion.TipoCambioCorresponsal;
		transaccionHistorico.IdCorredorCorresponsal = transaccion.IdCorredoraCorresponsal;
		transaccionHistorico.MontoLiquidacionCorresponsal = transaccion.MontoLiquidacionCorresponsal;
		transaccionHistorico.IdMonedaLiquidacionCorresponsal = transaccion.IdMonedaLiquidacionCorresponsal;
		transaccionHistorico.MejorPrecioCompra = transaccion.MejorPrecioCompra;
		transaccionHistorico.MejorPrecioVenta = transaccion.MejorPrecioVenta;
		transaccionHistorico.IdProgramaProcesaAutomatico = transaccion.IdProgramaProcesaAutomatico;
		transaccionHistorico.IdCorredorLocal = transaccion.IdCorredoraLocal;
		transaccionHistorico.IdCorredorContraparte = transaccion.IdCorredoraContraparte;
		transaccionHistorico.CodigoOperadorCorredor = transaccion.CodigoOperadorCorredor;
		transaccionHistorico.EsFueraRueda = transaccion.EsFueraRueda;
		transaccionHistorico.BaseTasa = transaccion.BaseTasa;
		transaccionHistorico.TipoInteres = transaccion.TipoInteres;
		transaccionHistorico.IndicadorOrdenDirecta = transaccion.IndicadorOrdenDirecta;
		transaccionHistorico.GastosCorresponsal = transaccion.GastosCorresponsal;
		transaccionHistorico.Intereses = transaccion.Intereses;
		transaccionHistorico.Reajustes = transaccion.Reajustes;
		transaccionHistorico.IdBoveda = transaccion.IdBoveda;
		transaccionHistorico.IdMonedaEmision = transaccion.IdMonedaEmision;
		transaccionHistorico.FlujoOrigen = transaccion.FlujoOrigen;
		transaccionHistorico.EsModificacionMarketData = transaccion.EsModificacionMarketData;
		transaccionHistorico.IdTransaccionHistoricoAnulacion = transaccion.IdTransaccionHistoricoAnulacion;
		
		logger.info("FIN::CrearTransaccionesHistoricas.crearTransaccionHistoricaAPartirDeTransaccion");
		return transaccionHistorico;
		
	} 
	
}
