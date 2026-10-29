package cl.bcs.ordenes;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.ordenes.ObtenerDetalleRefacturacion.ObtenerDetalleRefacturacion_Entrada;
import cl.bcs.ordenes.ObtenerDetalleRefacturacion.ObtenerDetalleRefacturacion_Salida;
import cl.bcs.ordenes.RefacturarMovimiento.RefacturarMovimiento_Entrada;
import cl.bcs.ordenes.RefacturarMovimiento.RefacturarMovimiento_Salida;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarSaldoTransaccionesRefacturar implements JavaCall {
	
	private final String SQL_SELECT_SALDO_ASIGNACIONES = loadResourceToString("cl/bcs/ordenes/sql/ConsultarSaldoTransacciones_SelectSaldoAsignaciones.sql");
	private final String SQL_SELECT_SALDO_NOTAS_CREDITO = loadResourceToString("cl/bcs/ordenes/sql/ConsultarSaldoTransacciones_SelectSaldoNotasCredito.sql");
	

	private final String SQL_SELECT_SALDO_TRANSACCIONES = loadResourceToString("cl/bcs/ordenes/sql/ConsultarSaldoTransacciones_SelectSaldoTransacciones.sql");
	
	

	// -------------------------------------------------------------------
	// LOGICA
	// -------------------------------------------------------------------
	public ConsultarSaldoTransaccionesRefacturar_Salida logica(Connection conn,ConsultarSaldoTransaccionesRefacturar_Entrada entrada) throws Exception {
		SQL _sql = new SQL();
		ConsultarSaldoTransaccionesRefacturar_Salida salida = new ConsultarSaldoTransaccionesRefacturar_Salida();
		

		


		List<Transaccion_Proceso> transacciones = new ArrayList<Transaccion_Proceso>();
		HashMap<Long,BigDecimal> hashCantidadNotaCredito = new HashMap<>();
		HashMap<Long,BigDecimal> hashCantidadNoEnviadaFacturar = new HashMap<>();
		HashMap<Long,BigDecimal> hashCantidadEnviadaFacturar = new HashMap<>();
		HashMap<String,Object> params = new HashMap<>();
		
		// TRALIB: transacciones libres(no asociadas a una factura)
		if (entrada.p_TipoConsulta.equalsIgnoreCase("TRALIB")) 
		{  
			params.put("IdEmpresa", entrada.p_IdEmpresa);
			params.put("FechaTransaccionInicio", entrada.p_FechaTransaccionInicio);
			params.put("FechaTransaccionTermino", entrada.p_FechaTransaccionTermino);	
			
			transacciones = _sql.query(conn, SQL_SELECT_SALDO_TRANSACCIONES, params, Transaccion_Proceso.class);
		}
		else
		{	//TRANDC:transacciones asociadas a notas de credito 
			List<NotaCredito_Proceso> notasCredito = _sql.query(conn, SQL_SELECT_SALDO_NOTAS_CREDITO, new HashMap<>(), NotaCredito_Proceso.class);
			
			if (notasCredito.size() > 0) {
				ArrayList<Long> idsTransaccion = new ArrayList<>();
				for (NotaCredito_Proceso nc : notasCredito) {
					if (!idsTransaccion.contains(nc.IdTransaccion)) {
						idsTransaccion.add(nc.IdTransaccion);
					}
				}
				params.put("idsTransaccion", idsTransaccion);
				transacciones = _sql.query(conn, SQL_SELECT_SALDO_ASIGNACIONES, params, Transaccion_Proceso.class);
						
			}
			
			
			for (NotaCredito_Proceso nc : notasCredito) {
				BigDecimal d = hashCantidadNotaCredito.get(nc.IdTransaccion);
				if (d == null)
					d = BigDecimal.ZERO;
				d = d.add(nc.CantidadAsignada);
				hashCantidadNotaCredito.put(nc.IdTransaccion,d);
			}
		}
		

		
		
		for (Transaccion_Proceso tran : transacciones) {
			if (!hashCantidadNoEnviadaFacturar.containsKey(tran.IdTransaccion))
					hashCantidadNoEnviadaFacturar.put(tran.IdTransaccion,BigDecimal.ZERO);
			if (!hashCantidadEnviadaFacturar.containsKey(tran.IdTransaccion))
				hashCantidadEnviadaFacturar.put(tran.IdTransaccion,BigDecimal.ZERO);
			
			if (tran.IdAsignacion != null && tran.IdEstadoAsignacion.equalsIgnoreCase("NEF")) {
				BigDecimal d = hashCantidadNoEnviadaFacturar.get(tran.IdTransaccion);
				d = d.add(tran.CantidadAsignada);
				hashCantidadNoEnviadaFacturar.put(tran.IdTransaccion,d);
			}
			
			if (tran.IdAsignacion != null && tran.IdEstadoAsignacion.equalsIgnoreCase("EAF")) {
				BigDecimal d = hashCantidadEnviadaFacturar.get(tran.IdTransaccion);
				d = d.add(tran.CantidadAsignada);
				hashCantidadEnviadaFacturar.put(tran.IdTransaccion,d);
			}
			
			if (tran.IdAsignacion != null && tran.IdEstadoAsignacion.equalsIgnoreCase("REF")) {
				BigDecimal d = hashCantidadEnviadaFacturar.get(tran.IdTransaccion);
				d = d.add(tran.CantidadAsignada);
				hashCantidadEnviadaFacturar.put(tran.IdTransaccion,d);
			}
		}
		
		ArrayList<Long> procesadas = new ArrayList<>();
		for (Transaccion_Proceso tran : transacciones) {
			if (procesadas.contains(tran.IdTransaccion)) {
				continue;
			}
			procesadas.add(tran.IdTransaccion);
			ObtenerDetalleRefacturacion.Transaccion_Proceso tx = new ObtenerDetalleRefacturacion.Transaccion_Proceso();
			Utils.copyPojo(tran,tx);
			tx.CantidadDisponibleTransaccion = tx.CantidadTransaccion.
					subtract(hashCantidadNoEnviadaFacturar.get(tx.IdTransaccion)).
					subtract(hashCantidadEnviadaFacturar.get(tx.IdTransaccion)).

					add((entrada.p_TipoConsulta.equalsIgnoreCase("TRALIB")) ? BigDecimal.ZERO : hashCantidadNotaCredito.get(tx.IdTransaccion));
			
			
			if (tx.CantidadDisponibleTransaccion.compareTo(BigDecimal.ZERO) == 1) {
				salida.P_OUT_TRANSACCIONES.add(tx);
			}
		}
		
		return salida ;
	}
	
	
	// -------------------------------------------------------------------
	// CLASES DE PROCESO
	// -------------------------------------------------------------------
	public static class NotaCredito_Proceso {
		public Long IdComprobanteNotaCredito;
		public String IdTipoComprobante;
		public Long IdComprobante;
		public Long IdMovimientoComprobante;
		public Long IdAsignacion;
		public String IdEstadoAsignacion;
		public BigDecimal CantidadAsignada;
		public String EsFungible;
		public Long IdTransaccion;
		public String IdTipoInstrumento;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Transaccion_Proceso {
		public Long IdTransaccion;
		public String EsFungible;
		public BigDecimal CantidadTransaccion;
		public Long IdAsignacion;
		public String IdEstadoAsignacion;
		public BigDecimal CantidadAsignada;
		
		public String IdTipoInstrumento;
		
		public BigDecimal CantidadDisponibleTransaccion	= BigDecimal.ZERO;
		public BigDecimal MontoTransaccion;
		public java.sql.Timestamp FechaOperacionTransaccion;
		public java.sql.Date FechaSistemaCreacion;
		public java.sql.Time HoraSistemaCreacion;
		public String FolioTransaccion;
		public BigDecimal PrecioTransaccion;
		public String InstrumentoUI;
		public String IdTipoOperacion;
		public String NombreTipoOperacion;
		public String IdCondicionDeLiquidacion;
		public String NombreCondicionDeLiquidacion;
		public String IdBolsa;
		public String IdMonedaTransaccion;
		public java.sql.Date FechaVencimiento;
		public String IdEmisor;
		public BigDecimal Tasa;
		public String IdTipoReajuste;
		public String NemotecnicoSVS;
		public String EsFungibleUI;
		public String IndicadorOrdenDirecta;
		public String EsVentaCorta;
		public String EsVentaCortaUI;
		public String NombreTipoInstrumento;
		public String NombreClasificacionTipoInstrumento;
		public String NombreCorredorContraparte;
		

		public String AccionOrden;
		
		

		public String Reservado;
		public Long IdUsuarioReserva;
		public String CodigoUsuarioReserva;
		public String IdGrupoReserva;
		

		public Long IdComprobante;
		public String IdMonedaInstrumento;
		public String InstrumentoNegociacionTran;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	
	// -------------------------------------------------------------------
	// CLASES DE ENTRADA
	// -------------------------------------------------------------------	
	public static class ConsultarSaldoTransaccionesRefacturar_Entrada {
		
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		

		public String p_TipoConsulta = "";     // TRANDC:transacciones asociadas a notas de credito     TRALIB: transacciones libres(no asociadas a una factura)
		public String p_FechaTransaccionInicio = "";
		public String p_FechaTransaccionTermino = "";
		

		
		public ConsultarSaldoTransaccionesRefacturar_Entrada() {
			
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	// -------------------------------------------------------------------
	// CLASES DE SALIDA
	// -------------------------------------------------------------------
	public static class ConsultarSaldoTransaccionesRefacturar_Salida {

		public Long 					P_OUT_ERRORESCONTADOR;
		public List<ObtenerDetalleRefacturacion.Transaccion_Proceso>	P_OUT_TRANSACCIONES;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public ConsultarSaldoTransaccionesRefacturar_Salida() {
			this.P_OUT_TRANSACCIONES = new ArrayList<>();
			this.P_OUT_MENSAJES = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	// -------------------------------------------------------------------
	// METODO PARA PRUEBAS LOCALES
	// -------------------------------------------------------------------
	public static void main(String[]args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();			
			////String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":5,\"p_IdModulo\":\"ORD\",\"p_TipoConsulta\":\"TRALIB\",\"p_FechaTransaccionInicio\":\"2020-08-21T04:00:00\",\"p_FechaTransaccionTermino\":\"2020-08-22T03:59:59\"}";
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":5,\"p_IdModulo\":\"FAC\",\"p_UsaReglasNegocio\":\"S\",\"p_IdTipoCargaMasiva\":0,\"p_CodigoArchivo\":\"CARGA_REFACTURACION_JPM_2\",\"p_TipoCarga\":\"NEMTRA\",\"p_FechaCarga\":\"2020-08-21\",\"p_DetalleCargaMasiva\":[{\"FechaRegisro\":\"2020-08-21\",\"FechaTransaccion\":\"2020-08-21\",\"Folio\":555511,\"Nemotecnico\":\"CENCOSUD\",\"Tipo\":\"C\",\"Cantidad\":120,\"DocumentoIdentidad\":\"12969673-7\",\"Portafolio\":\"1\",\"Comisiones\":0,\"Derechos\":0,\"Gastos\":0,\"Iva\":19,\"Identificador\":\"NEMTRA\"}],\"p_DetalleTransacciones\":[{\"CantidadDisponibleTransaccion\":120,\"CantidadTransaccion\":120,\"CantidadTransaccionPorAsignar\":120,\"CantidadSimulacionTransaccion\":0,\"EsFungible\":\"N\",\"EsFungibleUI\":\"NO\",\"EsVentaCorta\":\"N\",\"EsVentaCortaUI\":\"NO\",\"FolioTransaccion\":\"555511\",\"FechaSistemaCreacion\":\"2020-08-21 00:00:00.000\",\"FechaOperacionTransaccion\":\"2020-08-21 16:55:08.000\",\"FechaVencimiento\":\"0001-01-01 00:00:00.000\",\"HoraSistemaCreacion\":\"04:55:08 PM\",\"IdBolsa\":\"BCS\",\"IdCondicionDeLiquidacion\":\"T0\",\"IdClasificacionTipoInstrumento\":null,\"IdEmisor\":\"CENCOSUD\",\"IdInstrumento\":null,\"IdMonedaTransaccion\":\"CLP\",\"IdTipoInstrumento\":null,\"IdTipoOperacion\":\"COMACN\",\"IdTipoProducto\":null,\"IdTipoReajuste\":\"\",\"IdTransaccion\":70291647,\"IndicadorOrdenDirecta\":\"\",\"InstrumentoUI\":\"CENCOSUD\",\"MontoTransaccion\":156000,\"NemotecnicoSVS\":\"\",\"NombreTipoOperacion\":\"COMPRA ACCIONES NACIONALES\",\"NombreTipoInstrumento\":\"ACCIONES NACIONALES\",\"NombreClasificacionTipoInstrumento\":\"RENTA VARIABLE\",\"NombreCondicionDeLiquidacion\":\"PAGADERO HOY\",\"NombreCorredorContraparte\":\"CHILEMARK \",\"PrecioTransaccion\":1300,\"Tasa\":0,\"AccionOrden\":\"COMPRA\"}]}";

			ConsultarSaldoTransaccionesRefacturar_Entrada Objin = gson.fromJson(json, ConsultarSaldoTransaccionesRefacturar_Entrada.class);
			ConsultarSaldoTransaccionesRefacturar demo=new ConsultarSaldoTransaccionesRefacturar();	
			
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			
 			ConsultarSaldoTransaccionesRefacturar_Salida ObjOut=demo.logica(con, Objin);

			System.out.println("SALIDA");
 			System.out.println(ObjOut.toString());
 			
			//con.commit();
			con.rollback();
		}		 	
		System.out.println("== END ==");	
	}

	// -------------------------------------------------------------------
	// CALL
	// -------------------------------------------------------------------
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
		ConsultarSaldoTransaccionesRefacturar_Entrada jin=gson.fromJson(in.toString() ,ConsultarSaldoTransaccionesRefacturar_Entrada.class);
		ConsultarSaldoTransaccionesRefacturar_Salida out=logica(conn,jin);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		for (MensajeSalida msje : out.P_OUT_MENSAJES) {
			msje.descripcion = Diccionario.getMensaje(conn, jin.p_IdEmpresa, msje.codigo, new HashMap());
		}
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}	
	
}
	