package cl.bcs.ordenes;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import cl.bcs.ordenes.CrearSimulacionAsignacion.Simulacion_Salida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class CrearSimulacionAsignacionCronologica implements JavaCall{
	
	private final String SQL_SELECT_INFO_TRANSACCION = loadResourceToString("cl/bcs/ordenes/sql/CrearSimulacionAsignacionCronologica_SelectInfoTransaccion.sql");
	private final String SQL_SELECT_INFO_ORDENES = loadResourceToString("cl/bcs/ordenes/sql/CrearSimulacionAsignacionCronologica_SelectInfoOrden.sql");
	
	private Logger logger = LogManager.getLogger(CrearSimulacionAsignacionCronologica.class);
	
	public static class EntradaCrearSimulacionAsignacionCronologica{
		
		public Long p_IdEmpresa;
		public String p_IdTipo;
		public String p_Origen;
		//p_Transacciones <= BSCL.Orden.BD::ORD.CrearSimulacionAsignacion_Entrada_Transacciones
		//p_Ordenes <= BSCL.Orden.BD::ORD.CrearSimulacionAsignacion_Entrada_Ordenes
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InfoOrdenes{
		public Long IdOrden;
		public String IdClasificacionTipoInstrumento;
		public String IdBolsa;
		public String IdCondicionDeLiquidacion;
		
		public BigDecimal PrecioOrden;
		public BigDecimal MontoOrden;
		public BigDecimal CantidadAsignadaOrden;
		public Timestamp FechaIngresoRegistro;
		public Timestamp FechaOperacionOrden;
		public Long IdLibroOrdenes;
		public String IdTipoPrecio;
		public Long IdInstrumento;
		public String Instrumento;
		public String IdTipoInstrumento;
		public String IdMonedaOrden;
		public String IdMonedaInstrumento;
		public BigDecimal Tasa;
		public Long IdPortafolio;
		public String CodigoPortafolio;
		public String NombreCompleto;
		public String DocumentoIdentidad;
		public Long IdCliente;
		public BigDecimal CantidadDisponibleOrden;
		public String IdTipoCanalIngreso;
		//asignacion
		public String IdMonedaLiquidacionOrden;
		//datos instrumento
		public BigDecimal PrecioStrike;
		public String EsDivisible;
		public String LargoPlazo;
		public String ISIN;
		public String EstaInscrito;
		public Long Tamano;
		public BigDecimal CorteMinimo;
		//datos bolsa
		public String EsExtranjera;		
		//where
		public String IdTipoOperacion;
		public String EsVentaCorta;
		public Long Plazo;
		public Long IdInstrumentoSubyacente;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InfoTransacciones{
		public Long IdMovimiento;
		public Long IdTransaccion;
		public String FolioTransaccion;
		public Date FechaSistemaCreacion;
		public Long IdInstrumento;
		public String IdTipoOperacion;
		public String IdMonedaInstrumento;
		public String IdMonedaTransaccion;
		public BigDecimal PrecioTransaccion;
		public BigDecimal Tasa;
		public String IdCondicionDeLiquidacion;
		public Timestamp FechaOperacionTransaccion;
		public BigDecimal CantidadDisponibleTransaccion;
		public String IdTipoProducto;
		//asignacion
		public String EsFungible;
		public BigDecimal CantidadTransaccion;
		public BigDecimal MontoTransaccion;
		public BigDecimal ValorRescateTransaccion;
		//where
		public String IdClasificacionTipoInstrumento;
		public Long IdLibroOrdenes;
		public String IdTipoInstrumento;
		public String IdBolsa;
		public String EsVentaCorta;
		public Long Plazo;
		public Long IdInstrumentoSubyacente;
		public String InstrumentoNegociacion;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
			
	public static class SalidaCrearSimulacionAsignacionCronologica{
		public List<Simulacion_Salida> p_out_SimAsignacion;
		public SalidaCrearSimulacionAsignacionCronologica(){
			this.p_out_SimAsignacion = new ArrayList<>();
		}
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public class SimulacionAsignacionCronologica extends Simulacion_Salida{
		public SimulacionAsignacionCronologica(){
			super();
		}
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Trace
	public SalidaCrearSimulacionAsignacionCronologica logica(Connection conn, EntradaCrearSimulacionAsignacionCronologica in) {
		logger.info("INICIO");
		SQL sql = new SQL();
		SalidaCrearSimulacionAsignacionCronologica salida = new SalidaCrearSimulacionAsignacionCronologica();
		try {
			String asignacionCronologicaObligatoria = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "ORDENES", "AsignacionCronologicaObligatoria");
			String fechaCreacionSistema = FechaDB.ObtenerFechaHoraSistemaUTC(conn, in.p_IdEmpresa);
			logger.debug("Obtener transacciones");
			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("p_IdTipo", in.p_IdTipo);
			List<InfoTransacciones> listaTransacciones = sql.query(conn, SQL_SELECT_INFO_TRANSACCION, params, InfoTransacciones.class);
			logger.debug("Obtener ordenes");
			List<InfoOrdenes> listaOrdenes = sql.query(conn, SQL_SELECT_INFO_ORDENES, params, InfoOrdenes.class);
			logger.debug("Realizar asignacion de ordenes vs transacciones de forma cronologica");
			listaOrdenes.forEach(orden -> {
				listaTransacciones.forEach(transaccion -> {
					if(compararOrdenTransaccion(asignacionCronologicaObligatoria, orden, transaccion) 
							&& orden.CantidadDisponibleOrden.compareTo(BigDecimal.ZERO)>0 
							&& transaccion.CantidadDisponibleTransaccion.compareTo(BigDecimal.ZERO)>0) {
						
						BigDecimal cantidadDisponibleOrden = orden.CantidadDisponibleOrden.subtract( (orden.CantidadDisponibleOrden.compareTo(transaccion.CantidadDisponibleTransaccion)>=0) ?  transaccion.CantidadDisponibleTransaccion : orden.CantidadDisponibleOrden);
						BigDecimal cantidadAsignacion = orden.CantidadDisponibleOrden.subtract(cantidadDisponibleOrden);
						
						if(cantidadAsignacion.compareTo(BigDecimal.ZERO)>=0) {
							orden.CantidadDisponibleOrden = orden.CantidadDisponibleOrden.subtract(cantidadAsignacion);
							orden.CantidadAsignadaOrden = orden.CantidadAsignadaOrden.add(cantidadAsignacion);
							transaccion.CantidadDisponibleTransaccion = transaccion.CantidadDisponibleTransaccion.subtract(cantidadAsignacion);
							
							SimulacionAsignacionCronologica asig = new SimulacionAsignacionCronologica();
							asig.IdMovimiento = transaccion.IdMovimiento;
							asig.FechaSimulacion = Timestamp.valueOf(LocalDateTime.parse(fechaCreacionSistema, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
							asig.FechaIngresoRegistro = asig.FechaSimulacion;
							asig.IdTransaccion = transaccion.IdTransaccion;
							asig.FolioTransaccion = transaccion.FolioTransaccion;
							asig.FechaTransaccion = transaccion.FechaSistemaCreacion;
							asig.FolioCartera = 0L;
							asig.IdPortafolio = orden.IdPortafolio;
							asig.CodigoPortafolio = orden.CodigoPortafolio;
							asig.IdCliente = orden.IdCliente;
							asig.IdLibroOrdenes = transaccion.IdLibroOrdenes;
							asig.IdInstrumento = transaccion.IdInstrumento;
							asig.InstrumentoNegociacionTran = transaccion.InstrumentoNegociacion;
							asig.Instrumento = orden.Instrumento;
							asig.IdTipoInstrumento = transaccion.IdTipoInstrumento;
							asig.IdMonedaInstrumento = transaccion.IdMonedaInstrumento;
							asig.IdOrden = orden.IdOrden;
							asig.NombreCompleto = orden.NombreCompleto;
							asig.DocumentoIdentidad = orden.DocumentoIdentidad;
							asig.CantidadAsignacion = cantidadAsignacion;
							asig.ValorRescateAsignacion = BigDecimal.ZERO;
							asig.ValorRescateMontoTransaccion = (!orden.IdClasificacionTipoInstrumento.equals("IF") ? transaccion.CantidadTransaccion : transaccion.ValorRescateTransaccion);
							asig.PorcentajeAsignacion = BigDecimal.ZERO;
							asig.PrecioSimulacion = transaccion.PrecioTransaccion;
							asig.IdTipoProducto = transaccion.IdTipoProducto;
							asig.IdTipoOperacion = transaccion.IdTipoOperacion;
							asig.IdTipoCanalIngreso = orden.IdTipoCanalIngreso;
							asig.IdBolsa = transaccion.IdBolsa;
							asig.IdMonedaLiquidacionOrden = orden.IdMonedaLiquidacionOrden;
							asig.IdClasificacionTipoInstrumento = transaccion.IdClasificacionTipoInstrumento;
							asig.EsFungible = transaccion.EsFungible;
							asig.CantidadTransaccion = transaccion.CantidadTransaccion;
							asig.MontoTransaccion = transaccion.MontoTransaccion;
							asig.MontoAsignacion = transaccion.MontoTransaccion.multiply(cantidadAsignacion).divide(transaccion.CantidadTransaccion, 20, BigDecimal.ROUND_FLOOR);
							asig.EsExtranjera = orden.EsExtranjera;
							asig.CantidadAsignadaSimulacion = (!orden.IdClasificacionTipoInstrumento.equals("IF") ? cantidadAsignacion : BigDecimal.ZERO);
							asig.CantidadTransaccionSimulacion = (!orden.IdClasificacionTipoInstrumento.equals("IF") ? transaccion.CantidadTransaccion : BigDecimal.ZERO);
							asig.PrecioStrike = orden.PrecioStrike;
							asig.ModalidadLiquidacion = (orden.EsDivisible.equals("F") ? "FISICA" : orden.EsDivisible.equals("C") ? "FINANCIERA" : "");
							asig.ModalidadEjercicio = (orden.LargoPlazo.equals("A") ? "AUTOMATICA" : orden.LargoPlazo.equals("M") ? "MANUAL" : "");
							asig.TipoOpcion = (orden.ISIN.equals("C") ? "CALL" : orden.ISIN.equals("P") ? "PUT" : "");
							asig.EstiloOpcion = (orden.EstaInscrito.equals("A") ? "AMERICANA" : orden.EstaInscrito.equals("E") ? "EUROPEA" : "");
							asig.Tamano = orden.Tamano;
							asig.CorteMinimo = orden.CorteMinimo;
							salida.p_out_SimAsignacion.add(asig);
						}
					}
				});
			});
			
			logger.debug("Asignar ids asignacion");
			LinkedList<Long> idsAsignacion = new LinkedList<Long>(Utils.getSecuencia(conn, "BSCL.Orden.BD.Secuencias::DetalleSimulacionAsignacion", (long) salida.p_out_SimAsignacion.size()));
			salida.p_out_SimAsignacion = salida.p_out_SimAsignacion.stream().map(asignacion -> {
				asignacion.IdInterno = idsAsignacion.pop();
				return asignacion;
			}).collect(Collectors.toList());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		logger.info("TERMINO");
		return salida;
	}

	private boolean compararOrdenTransaccion(String asignacionCronologicaObligatoria, InfoOrdenes orden, InfoTransacciones transaccion) {
		return transaccion.IdClasificacionTipoInstrumento.equals(orden.IdClasificacionTipoInstrumento) &&
				transaccion.IdLibroOrdenes.equals(orden.IdLibroOrdenes) &&
				transaccion.IdTipoInstrumento.equals(orden.IdTipoInstrumento) &&
				transaccion.IdInstrumento.equals(orden.IdInstrumento) &&
				transaccion.IdMonedaTransaccion.equals(orden.IdMonedaOrden) &&
				(transaccion.IdBolsa.equals(orden.IdBolsa) || orden.IdBolsa.equals("0")) &&
				transaccion.IdTipoOperacion.equals(orden.IdTipoOperacion) &&
				transaccion.IdCondicionDeLiquidacion.equals(orden.IdCondicionDeLiquidacion) &&
				transaccion.EsVentaCorta.equals(orden.EsVentaCorta) &&
				transaccion.FechaOperacionTransaccion.compareTo( (asignacionCronologicaObligatoria.equals("S") ? orden.FechaOperacionOrden : transaccion.FechaOperacionTransaccion) ) >=0
				&& ( 
					(transaccion.equals("COMSIM") && transaccion.Tasa.compareTo(orden.Tasa)>=0 && transaccion.Plazo.compareTo(orden.Plazo)<=0) ||
					(transaccion.equals("VENSIM") && transaccion.CantidadDisponibleTransaccion.compareTo(orden.CantidadDisponibleOrden)==0 && transaccion.Tasa.compareTo(orden.Tasa)<=0 && transaccion.Plazo.equals(orden.Plazo) && transaccion.IdInstrumentoSubyacente.equals(orden.IdInstrumentoSubyacente)) ||
					(!transaccion.equals("COMSIM") && !transaccion.equals("VENSIM") && 
						(!orden.IdTipoPrecio.equals("L") ||
							(orden.IdTipoPrecio.equals("L") && orden.IdTipoOperacion.contains("COM") && orden.PrecioOrden.compareTo(transaccion.PrecioTransaccion)>=0) ||
							(orden.IdTipoPrecio.equals("L") && orden.IdTipoOperacion.contains("VEN") && orden.PrecioOrden.compareTo(transaccion.PrecioTransaccion)<=0)
						)
					)
				);
	}
	
	public static void main(String[] args) {
		
		try(Connection conn = PlataformaUtils.getDESAConnection()){
			
			EjecutarAsignacionPropCrono serv = new EjecutarAsignacionPropCrono();
			EjecutarAsignacionPropCrono.main(new String[0]);
			/*
			CrearSimulacionAsignacionCronologica service = new CrearSimulacionAsignacionCronologica();
			
			Gson gson = Utils.getGsonBuilder();
			
			String jsonTest = "{\"p_IdEmpresa\":1,\"p_IdTipo\":\"ASIG\",\"p_Origen\":\"\"}";
						
			EntradaCrearSimulacionAsignacionCronologica entrada = gson.fromJson(jsonTest, EntradaCrearSimulacionAsignacionCronologica.class); 
			
			SalidaCrearSimulacionAsignacionCronologica salida = service.logica(conn, entrada);
			System.out.println(salida);
			*/
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
}
