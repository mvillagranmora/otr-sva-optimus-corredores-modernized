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

import cl.bcs.ordenes.RefacturarMovimiento.RefacturarMovimiento_Entrada;
import cl.bcs.ordenes.RefacturarMovimiento.RefacturarMovimiento_Salida;
import cl.bcs.ordenes.pojo.ConstantesOrdenes;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ObtenerDetalleRefacturacion implements JavaCall {
	
	private final String SQL_SELECT_COMPROBANTES_BASE = loadResourceToString("cl/bcs/ordenes/sql/ObtenerDetalleRefacturacion_SelectComprobantesBase.sql");
	private final String SQL_SELECT_COMPROBANTES_NOTA_CREDITO = loadResourceToString("cl/bcs/ordenes/sql/ObtenerDetalleRefacturacion_SelectComprobantesNotaDeCredito.sql");
	private final String SQL_SELECT_MOVIMIENTOSXCOMPROBANTES = loadResourceToString("cl/bcs/ordenes/sql/ObtenerDetalleRefacturacion_SelectMovimientosXComprobante.sql");
	private final String SQL_SELECT_ASIGNACIONES = loadResourceToString("cl/bcs/ordenes/sql/ObtenerDetalleRefacturacion_SelectAsignaciones.sql");
	private final String SQL_SELECT_TRANSACCIONES = loadResourceToString("cl/bcs/ordenes/sql/ObtenerDetalleRefacturacion_SelectTransacciones.sql");
	private final ConstantesOrdenes _c = new ConstantesOrdenes();
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
		ObtenerDetalleRefacturacion_Entrada in=gson.fromJson(jin.toString() ,ObtenerDetalleRefacturacion_Entrada.class);
		ObtenerDetalleRefacturacion_Salida out=logica(conn,in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		for (MensajeSalida msje : out.P_OUT_MENSAJES) {
			msje.descripcion = Diccionario.getMensaje(conn, in.p_IdEmpresa, msje.codigo, new HashMap());
		}
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public ObtenerDetalleRefacturacion_Salida logica(Connection conn,ObtenerDetalleRefacturacion_Entrada entrada) throws Exception {
		SQL _sql = new SQL();
		ObtenerDetalleRefacturacion_Salida salida = new ObtenerDetalleRefacturacion_Salida();
		
		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(conn, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo);
		if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
			salida.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
			return salida;
		}
		
		if (entrada.p_Entidades.size() == 0) {
			String codigo = "NO_VIENEN_MOVIMIENTOS";
			String descripcion = Diccionario.getMensaje(conn, entrada.p_IdEmpresa,codigo).msg();
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,codigo,descripcion));
			return salida;
		}
		
		
		
		HashMap<Long,Comprobante_Proceso> hashComprobantes = new HashMap<>();
		HashMap<Long,MovimientoComprobante_Proceso> hashMovimientosComprobante = new HashMap<>();
		HashMap<Long,Asignacion_Proceso> hashAsignaciones = new HashMap<>();
		HashMap<Long,Asignacion_Proceso> hashAsignacionesColateral = new HashMap<>();
		HashMap<Long,Transaccion_Proceso> hashTransacciones = new HashMap<>();
		
		/*List<Comprobante_Proceso> comprobantes = new ArrayList<>();
		List<MovimientoComprobante_Proceso> movimientosComprobante = new ArrayList<>();
		List<Asignacion_Proceso> asignaciones = new ArrayList<>();
		List<Asignacion_Proceso> asignacionesColateral = new ArrayList<>();
		List<Transaccion_Proceso> transacciones = new ArrayList<>();*/
		
		//REFACTURANDO COMPROBANTES
		
		if (entrada.p_Origen.equalsIgnoreCase("COMP")) {
			
			//LECTURA COMPROBANTES
			{
				HashMap<String,Object> paramsComprobante = new HashMap<>();
				List<Long> idsComprobante = new ArrayList<>();
				for (Entidad_Refacturar cmbte : entrada.p_Entidades) {
					if (!idsComprobante.contains(cmbte.IdEntidad))
						idsComprobante.add(cmbte.IdEntidad);
				}
				paramsComprobante.put("idsComprobante", idsComprobante);
				paramsComprobante.put("idsMovimientoComprobante", new ArrayList<Long>());
				paramsComprobante.put("idsAsignacion", new ArrayList<Long>());
				paramsComprobante.put("idsTransaccion", new ArrayList<Long>());
				List<Comprobante_Proceso> _comprobantes = _sql.query(conn, SQL_SELECT_COMPROBANTES_BASE, paramsComprobante, Comprobante_Proceso.class);
				
				
				List<String> tiposComprobanteNotaCredito = new ArrayList<>();
				tiposComprobanteNotaCredito.addAll(Arrays.asList("NCE","NCR","NEO"));
				
				HashMap<String,Object> paramsComprobanteNotaCredito = new HashMap<>();
				paramsComprobanteNotaCredito.put("idsComprobante", idsComprobante);
				paramsComprobanteNotaCredito.put("tiposComprobanteNotaCredito", tiposComprobanteNotaCredito);
				
				List<Comprobante_Proceso> _notasCredito = _sql.query(conn, SQL_SELECT_COMPROBANTES_NOTA_CREDITO, paramsComprobanteNotaCredito, Comprobante_Proceso.class);
				HashMap<Long,Comprobante_Proceso> notasCredito = new HashMap<>();
				for (Comprobante_Proceso nce : _notasCredito) {
					if (!notasCredito.containsKey(nce.IdComprobanteReferencia))
						notasCredito.put(nce.IdComprobanteReferencia, nce);
				}
				
				for (Comprobante_Proceso cmbte : _comprobantes) {
					if (!cmbte.IdTipoComprobante.equals("FAE") && !cmbte.IdTipoComprobante.equals("POL") ) {
						String codigo = "TIPO_COMPROBANTE_NO_PERMITE_REFACTURACION";
						String descripcion = Diccionario.getMensaje(conn,entrada.p_IdEmpresa, codigo).msg();
						salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,codigo,descripcion));
					}
					if (cmbte.IdEstadoComprobante.equals("ANU")) {
						String codigo = "COMPROBANTE_ANULADO";
						String descripcion = Diccionario.getMensaje(conn,entrada.p_IdEmpresa, codigo).msg();
						salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,codigo,descripcion));
					}
					if (notasCredito.containsKey(cmbte.IdComprobante)) {
						String codigo = "COMPROBANTE_TIENE_NOTA_CREDITO";
						String descripcion = Diccionario.getMensaje(conn,entrada.p_IdEmpresa, codigo).msg();
						salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,codigo,descripcion));
					}
					hashComprobantes.put(cmbte.IdComprobante, cmbte);
				}
			}
			
			if (MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES) > 0) {
				return salida;
			}
			
			//LECTURA MOVIMIENTOS
			{
				HashMap<String,Object> paramsMovimientoComprobante = new HashMap<>();
				List<Long> idsComprobante = new ArrayList<>();
				for (Entidad_Refacturar cmbte : entrada.p_Entidades) {
					idsComprobante.add(cmbte.IdEntidad);
				}
				paramsMovimientoComprobante.put("idsComprobante", idsComprobante);
				List<MovimientoComprobante_Proceso> _movimientosComprobante = _sql.query(conn, SQL_SELECT_MOVIMIENTOSXCOMPROBANTES, paramsMovimientoComprobante, MovimientoComprobante_Proceso.class);
				for (MovimientoComprobante_Proceso mvto : _movimientosComprobante) {
					hashMovimientosComprobante.put(mvto.IdMovimientoComprobante, mvto);
				}
			}
			
			//LECTURA ASIGNACIONES
			{
				HashMap<String,Object> paramsAsignaciones = new HashMap<>();
				List<Long> idsAsignacion = new ArrayList<>();
				for (MovimientoComprobante_Proceso mvto : hashMovimientosComprobante.values()) {
					if (mvto.IdAsignacion != null && mvto.IdAsignacion > 0)
						idsAsignacion.add(mvto.IdAsignacion);
				}
				paramsAsignaciones.put("idsAsignacion", idsAsignacion);
				List<Asignacion_Proceso> _asignaciones = _sql.query(conn, SQL_SELECT_ASIGNACIONES, paramsAsignaciones, Asignacion_Proceso.class);
				for (Asignacion_Proceso asig : _asignaciones) {
					hashAsignaciones.put(asig.IdAsignacion, asig);
				}
			}
			
			//LECTURA TRANSACCIONES
			{
				HashMap<String,Object> paramsTransacciones = new HashMap<>();
				List<Long> idsTransaccion = new ArrayList<>();
				for (Asignacion_Proceso asig : hashAsignaciones.values()) {
					if (!idsTransaccion.contains(asig.IdTransaccion)) 
						idsTransaccion.add(asig.IdTransaccion);
				}
				paramsTransacciones.put("idsTransaccion", idsTransaccion);
				List<Transaccion_Proceso> _transacciones = _sql.query(conn, SQL_SELECT_TRANSACCIONES, paramsTransacciones, Transaccion_Proceso.class);
				
				//ARMAMOS SALIDA PARA REFACTURAR
				HashMap<Long,BigDecimal> sumCantidad = new HashMap<>();
				HashMap<Long,BigDecimal> sumValorRescate = new HashMap<>();
				
				for (Transaccion_Proceso tran : _transacciones) {
					for (Asignacion_Proceso asig : hashAsignaciones.values()) {
						if (asig.IdTransaccion.equals(tran.IdTransaccion)) {
							
							BigDecimal cantidad = sumCantidad.get(asig.IdTransaccion);
							if (cantidad == null) {
								cantidad = BigDecimal.ZERO;
							}
							
							BigDecimal valorRescate = sumValorRescate.get(asig.IdTransaccion);
							if (valorRescate == null) {
								valorRescate = BigDecimal.ZERO;
							}
							
							sumCantidad.put(asig.IdTransaccion,cantidad.add(asig.CantidadAsignada));
							sumValorRescate.put(asig.IdTransaccion,valorRescate.add(asig.ValorRescateAsignado));
							
						}
					}
					
					Transaccion_Proceso t = new Transaccion_Proceso();
					Utils.copyPojo(tran, t);
					if (tran.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_IF)) {
						t.CantidadDisponibleTransaccion = sumValorRescate.get(tran.IdTransaccion);;
					}
					else {
						t.CantidadDisponibleTransaccion = sumCantidad.get(tran.IdTransaccion);	
					}
					
					hashTransacciones.put(t.IdTransaccion, t);
				}

			}
			
			
			
			
			
		}else
		{
			//Lectura de Asignaciones
			if (entrada.p_Origen.equalsIgnoreCase("ASIG")) {
				
			  //Lectura Asignaciones
			  HashMap<String,Object> paramsAsignaciones = new HashMap<>();
			  List<Long> idsAsignacion = new ArrayList<>();
			  for (Entidad_Refacturar mvto : entrada.p_Entidades) {
				 if (!idsAsignacion.contains(mvto.IdEntidad))
				     idsAsignacion.add(mvto.IdEntidad);
				}
			  
			  paramsAsignaciones.put("idsAsignacion", idsAsignacion);
			  List<Asignacion_Proceso> _asignaciones = _sql.query(conn, SQL_SELECT_ASIGNACIONES, paramsAsignaciones, Asignacion_Proceso.class);
			  for (Asignacion_Proceso asig : _asignaciones) {
					hashAsignaciones.put(asig.IdAsignacion, asig);
				}
			}
			
			
			
			//Lectura Transacciones
			HashMap<String,Object> paramsTransacciones = new HashMap<>();
			List<Long> idsTransaccion = new ArrayList<>();
			for (Asignacion_Proceso asig : hashAsignaciones.values()) {
				if (!idsTransaccion.contains(asig.IdTransaccion)) 
					idsTransaccion.add(asig.IdTransaccion);
			}
			paramsTransacciones.put("idsTransaccion", idsTransaccion);
			List<Transaccion_Proceso> _transacciones = _sql.query(conn, SQL_SELECT_TRANSACCIONES, paramsTransacciones, Transaccion_Proceso.class);
			
			//ARMAMOS SALIDA PARA REFACTURAR
			HashMap<Long,BigDecimal> sumCantidad = new HashMap<>();
			HashMap<Long,BigDecimal> sumValorRescate = new HashMap<>();
			
			for (Transaccion_Proceso tran : _transacciones) {
				for (Asignacion_Proceso asig : hashAsignaciones.values()) {
					if (asig.IdTransaccion.equals(tran.IdTransaccion)) {
						
						BigDecimal cantidad = sumCantidad.get(asig.IdTransaccion);
						if (cantidad == null) {
							cantidad = BigDecimal.ZERO;
						}
						
						BigDecimal valorRescate = sumValorRescate.get(asig.IdTransaccion);
						if (valorRescate == null) {
							valorRescate = BigDecimal.ZERO;
						}
						
						sumCantidad.put(asig.IdTransaccion,cantidad.add(asig.CantidadAsignada));
						sumValorRescate.put(asig.IdTransaccion,valorRescate.add(asig.ValorRescateAsignado));
						
					}
				}
				
				Transaccion_Proceso t = new Transaccion_Proceso();
				Utils.copyPojo(tran, t);
				if (tran.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_IF)) {
					t.CantidadDisponibleTransaccion = sumValorRescate.get(tran.IdTransaccion);;
				}
				else {
					t.CantidadDisponibleTransaccion = sumCantidad.get(tran.IdTransaccion);	
				}
				
				hashTransacciones.put(t.IdTransaccion, t);
			}
			
			
			
		}
		
		salida.P_OUT_ASIGNACIONES.addAll(hashAsignaciones.values());
		salida.P_OUT_ASIGNACIONESCOLATERAL.addAll(hashAsignacionesColateral.values());
		salida.P_OUT_COMPROBANTESFACTURACION.addAll(hashComprobantes.values());
		salida.P_OUT_MOVIMIENTOSFACTURAR.addAll(hashMovimientosComprobante.values());
		salida.P_OUT_TRANSACCIONES.addAll(hashTransacciones.values());
		return salida;
	}
	
	public static class Transaccion_Proceso {
		public Long IdTransaccion;
		public BigDecimal CantidadDisponibleTransaccion	= BigDecimal.ZERO;
		public BigDecimal CantidadTransaccion;
		public BigDecimal MontoTransaccion;
		public java.sql.Timestamp FechaOperacionTransaccion;
		public java.sql.Date FechaSistemaCreacion;
		public java.sql.Time HoraSistemaCreacion;
		public String FolioTransaccion;
		public BigDecimal PrecioTransaccion;
		public String InstrumentoUI;
		public Long IdInstrumento;
		public String IdTipoProducto;
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
		public String EsFungible;
		public String EsFungibleUI;
		public String IndicadorOrdenDirecta;
		public String EsVentaCorta;
		public String EsVentaCortaUI;
		public String NombreTipoInstrumento;
		public String NombreClasificacionTipoInstrumento;
		public String NombreCorredorContraparte;
		public String IdClasificacionTipoInstrumento;
		public String IdTipoInstrumento;
		

		public String AccionOrden;    // COMPRA  VENTA
		

		public String Reservado;
		public Long IdUsuarioReserva;
		public String CodigoUsuarioReserva;
		public String IdGrupoReserva;
		

		public Long IdComprobante;
		public String IdMonedaInstrumento;
		public String InstrumentoNegociacionTran;
		
		@Override
		public boolean equals(Object o) {
			Transaccion_Proceso e = (Transaccion_Proceso)o;
			return (this.IdTransaccion.equals(e.IdTransaccion));
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Asignacion_Proceso {
		public Long IdMovimientoComprobante;
		public Long IdAsignacion;
		public BigDecimal CantidadAsignada;
		public BigDecimal MontoAsignado;
		public BigDecimal ValorRescateAsignado;
		public Long IdTransaccion;
		public java.sql.Timestamp FechaAsignacion;
		public Long IdOrden;
		public String IdEstadoAsignacion;
		public String IdMonedaOperacion;
		public String IdTipoInstrumento;
		public String InstrumentoNegociacion;
		public String IdEstadoComprobante;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Comprobante_Proceso {
		public Long IdComprobante;
		public String IdEstadoComprobante;
		public String IdTipoComprobante;
		public Long IdComprobanteReferencia;
		public Long IdCliente;
		public Long IdPortafolio;
		public String IdCondicionDeLiquidacion;
		public java.sql.Date FechaLiquidacion;
		public String NombreCompleto;
		public String DocumentoIdentidad;
		public String CodigoPortafolio;
		public String IdClasificacionTipoInstrumento;
		public String SeparaPorInstrumento;
		public BigDecimal CantidadTotal;
		public BigDecimal PPP;
		public String IdMoneda;
		public String IdTipoInstrumento;
		public String NemotecnicoInstrumentoComprobante;

		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class MovimientoComprobante_Proceso {
		public Long IdMovimientoComprobante;
		public Long IdComprobante;
		public Long IdInstrumento;
		public Long IdPortafolio;
		public Long IdCliente;
		public Long IdInstrumentoSubyacente;
		public String IdTipoInstrumento;
		public String IdClasificacionTipoInstrumento;
		public Long IdAsignacion;
		public java.sql.Timestamp FechaVencimiento;
		public String IdEmisor;
		public String IdTipoReajuste;
		public String EsFungible;
		public String IdTipoOperacion;
		public String IdBolsa;
		public String IdCondicionDeLiquidacion;
		public String IdEstadoFacturacion;
		public BigDecimal Cantidad;
		public BigDecimal ValorRescate;
		public java.sql.Timestamp FechaMovimientoComprobante;
		public String IdConceptoMovimiento;
		public java.sql.Date FechaLiquidacion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	//ENTRADAS
	public static class ObtenerDetalleRefacturacion_Entrada {
		public Long 					p_IdEmpresa;
		public Long						p_IdUsuario;
		public String 					p_IdModulo;
		public String 					p_UsaReglasNegocio = "S";
		public String					p_Origen;
		public List<Entidad_Refacturar>	p_Entidades;
		
		public ObtenerDetalleRefacturacion_Entrada() {
			this.p_Entidades = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public static class Entidad_Refacturar {
		//el corresponde a una entidad del tipo indicado en la entrada -> TRAN,ASIG,MVTO,COMP
		public Long IdEntidad;

		@Override
		public boolean equals(Object o) {
			Entidad_Refacturar e = (Entidad_Refacturar)o;
			return (this.IdEntidad != null && e.IdEntidad != null && this.IdEntidad.equals(e.IdEntidad));
		}
		
		@Override
		public int hashCode() {
			return (this.IdEntidad).hashCode();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	//SALIDAS
	public static class ObtenerDetalleRefacturacion_Salida {

		public Long 								P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> 					P_OUT_MENSAJES;
		public List<Transaccion_Proceso>			P_OUT_TRANSACCIONES;				//TRANSACCIONES POR REASIGNAR/REFACTURAR
		public List<Asignacion_Proceso>				P_OUT_ASIGNACIONES;					//ASIGNACIONES POR ANULAR
		public List<Asignacion_Proceso>				P_OUT_ASIGNACIONESCOLATERAL;		//ASIGNACIONES POR REENVIAR,SU MOVIMIENTO SE ANULAR AUTOMATICAMENTE AL ANULAR COMPROBANTE
		public List<MovimientoComprobante_Proceso>	P_OUT_MOVIMIENTOSFACTURAR;			//MOVIMIENTOS POR ANULAR
		public List<Comprobante_Proceso>			P_OUT_COMPROBANTESFACTURACION;		//COMPROBANTES POR ANULAR O GENERAR NOTA DE CREDITO
		
		public ObtenerDetalleRefacturacion_Salida() {
			this.P_OUT_ASIGNACIONES = new ArrayList<>();
			this.P_OUT_ASIGNACIONESCOLATERAL = new ArrayList<>();
			this.P_OUT_COMPROBANTESFACTURACION = new ArrayList<>();
			this.P_OUT_MENSAJES = new ArrayList<>();
			this.P_OUT_MOVIMIENTOSFACTURAR = new ArrayList<>();
			this.P_OUT_TRANSACCIONES = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public static void main(String[]args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"ORD\",\"p_Origen\":\"ASIG\",\"p_Entidades\":[{\"IdEntidad\":14002}]}";
			
			
			ObtenerDetalleRefacturacion_Entrada Objin = gson.fromJson(json, ObtenerDetalleRefacturacion_Entrada.class);
			
			ObtenerDetalleRefacturacion demo=new ObtenerDetalleRefacturacion();				
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			ObtenerDetalleRefacturacion_Salida ObjOut=demo.logica(con, Objin);
 			System.out.println(ObjOut.toString());
		}
		 	
		System.out.println("== END ==");
		
		
		
		
	}	
	
}
	