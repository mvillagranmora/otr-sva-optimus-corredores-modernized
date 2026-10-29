package cl.bcs.plataforma.pla;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.dbcp2.BasicDataSource;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarMapaRelaciones implements JavaCall{	
	//SELECT ENTIDADES
	private final Log log = new Log("ConsultarMapaRelaciones");
	private final String SQL_SELECT_ORDENES 			= loadResourceToString("cl/bcs/plataforma/pla/sql/ConsultarMapaRelaciones_SelectOrdenes.sql");
	private final String SQL_SELECT_ASIGNACIONES		= loadResourceToString("cl/bcs/plataforma/pla/sql/ConsultarMapaRelaciones_SelectAsignaciones.sql");
	private final String SQL_SELECT_TRANSACCIONES 		= loadResourceToString("cl/bcs/plataforma/pla/sql/ConsultarMapaRelaciones_SelectTransacciones.sql");
	private final String SQL_SELECT_MOVFACTURAR 		= loadResourceToString("cl/bcs/plataforma/pla/sql/ConsultarMapaRelaciones_SelectMovfacturar.sql");
	private final String SQL_SELECT_CMBTE_FACTURACION	= loadResourceToString("cl/bcs/plataforma/pla/sql/ConsultarMapaRelaciones_SelectCmbteFacturacion.sql");
	private final String SQL_SELECT_OPERACION_SPOT		= loadResourceToString("cl/bcs/plataforma/pla/sql/ConsultarMapaRelaciones_SelectOperacionSpot.sql");
	private final String SQL_SELECT_OPERACION_FFMM		= loadResourceToString("cl/bcs/plataforma/pla/sql/ConsultarMapaRelaciones_SelectOperacionFFMM.sql");
	
	//SELECT CONECTORES

	private final String SQL_SELECT_TRAN_X_ASIG 	= loadResourceToString("cl/bcs/plataforma/pla/sql/ConsultarMapaRelaciones_SelectTranXAsig.sql");
	private final String SQL_SELECT_ASIG_X_ORDEN 	= loadResourceToString("cl/bcs/plataforma/pla/sql/ConsultarMapaRelaciones_SelectAsigXOrden.sql");
	private final String SQL_SELECT_ASIG_X_MOVFACT 	= loadResourceToString("cl/bcs/plataforma/pla/sql/ConsultarMapaRelaciones_SelectAsigXMovFact.sql");
	private final String SQL_SELECT_MOV_X_CMBTE 	= loadResourceToString("cl/bcs/plataforma/pla/sql/ConsultarMapaRelaciones_SelectMovXCmbte.sql");
	private final String SQL_SELECT_SPOT_X_MOVFACT 	= loadResourceToString("cl/bcs/plataforma/pla/sql/ConsultarMapaRelaciones_SelectSpotXMovFact.sql");
	private final String SQL_SELECT_SPOT_X_OPEFFMM 	= loadResourceToString("cl/bcs/plataforma/pla/sql/ConsultarMapaRelaciones_SelectSpotXOpeFFMM.sql");
	
	//CONDICIONES
	private final String SQL_WHERE_COMPROBANTE		= "\nWHERE CMBTE.\"IdComprobante\" IN (:ids)";
	private final String SQL_WHERE_MOV_COMPROBANTE	= "\nWHERE MOV.\"IdMovimientoComprobante\" IN (:ids)";
	private final String SQL_WHERE_TRANSACCION		= "\nWHERE TRAN.\"IdTransaccion\" IN (:ids);";
	private final String SQL_WHERE_ORDEN			= "\nWHERE ORD.\"IdOrden\" IN (:ids);";
	private final String SQL_WHERE_ASIGNACION		= "\nWHERE ASIG.\"IdAsignacion\" IN (:ids);";
	private final String SQL_WHERE_SPOT				= "\nWHERE B.\"IdMovimientoComprobante\" IN (:ids);";
	private final String SQL_WHERE_FFMM				= "\nWHERE MvtoCmbte.\"IdMovimientoComprobante\" IN (:ids);";
	
	//ENTRADA Y SALIDA DE LA CLASE
	public static class ConsultarMapaRelaciones_Entrada{
		
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		
		public String p_TipoEntidad;	//ORDEN,ASIGNACION,TRANSACCION,MOVFACTURAR,CMBTEFACTURACION,MOVCTAINV,MOVTESORERIA,CMBTETESORERIA
		public Long p_IdEntidad;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class ConsultarMapaRelaciones_Salida {
			
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		
		public List<Nodo_Salida> p_out_entidades;
		public List<Conector_Salida> p_out_conectores;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public ConsultarMapaRelaciones_Salida() {
			this.p_out_entidades = new ArrayList<>();
			this.p_out_conectores = new ArrayList<>();
		}
	}
	
	public static class ConectorOrdenAsignacion {
		public Long IdOrden;
		public Long IdAsignacion;
	}
	
	public static class ConectorTransaccionAsignacion {
		public Long IdTransaccion;
		public Long IdAsignacion;
	}
	
	public static class ConectorAsignacionMovFacturar {
		public Long IdMovimientoComprobante;
		public Long IdAsignacion;
	}
	
	public static class ConectorMovFacturarCmbte {
		public Long IdMovimientoComprobante;
		public Long IdComprobante;
	}
	
	public static class ConectorOpSpotMovComprobante {
		public Long IdOperacionSpot;
		public Long IdMovimientoComprobante;
	}
	
	public static class ConectorOpSpotFFMM {
		public Long IdSolicitudAporteRescate;
		public Long IdOperacionSpot;
	}
	
	public static class Nodo_Salida {
		
		public String Titulo;
		public String Color;
		public String Linea1;
		public String Linea2;
		public String Linea3;
		public String Linea4;
		
		public String TipoEnlace; //Nombre ventana de detalle
		public String NombreParametroEnlace; //nombre campo para abrir ventana detalle
		public Long IdentificadorEnlace;	//id del elemento para abrir detalle
		public String IdentificadorNodo;	//id para conectarse en el diagrama
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Conector_Salida {
		
		public String IdentificadorDesde;
		public String IdentificadorHacia;
		public String TextoConector;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	//TODO: main
	public static void main(String[] args){
		ConsultarMapaRelaciones_Entrada entTransaccion = new ConsultarMapaRelaciones_Entrada();
		entTransaccion.p_IdEmpresa = 1L;
		entTransaccion.p_IdEntidad = 752855L;
		entTransaccion.p_IdModulo = "PLA";
		entTransaccion.p_IdUsuario = 16L;
		entTransaccion.p_TipoEntidad = "CMBTEFACTURACION";
		
//		ConsultarMapaRelaciones_Entrada entOrden = new ConsultarMapaRelaciones_Entrada();
//		entOrden.p_IdEmpresa = 1L;
//		entOrden.p_IdEntidad = 315447L;
//		entOrden.p_IdModulo = "PLA";
//		entOrden.p_IdUsuario = 41L;
//		entOrden.p_TipoEntidad = "ORDEN";
		
		//{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"PLA\",\"p_TipoEntidad\":\"ORDEN\",\"p_IdEntidad\":315447}
		//1
		//{"p_IdEmpresa":1,"p_IdUsuario":16,"p_IdModulo":"PLA","p_TipoEntidad":"CMBTEFACTURACION","p_IdEntidad":752855}
		//2
		//{"p_IdEmpresa":1,"p_IdUsuario":16,"p_IdModulo":"PLA","p_TipoEntidad":"CMBTEFACTURACION","p_IdEntidad":752732}
        //prueba de la clase
		try {
			Connection conn = PlataformaUtils.getDESAConnection();
			ConsultarMapaRelaciones o = new ConsultarMapaRelaciones();
			System.out.println( o.logica(conn, entTransaccion));
//			System.out.println( o.logica(conn, entOrden));
		}
		catch(Exception e){
			e.printStackTrace();
		}		
	}

	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = new Gson();		
		ConsultarMapaRelaciones_Entrada in=gson.fromJson(jin.toString() ,ConsultarMapaRelaciones_Entrada.class);
		ConsultarMapaRelaciones_Salida out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public static class Orden_Proceso {
		public Long IdOrden;
		public String IdEstadoOrden;
		public BigDecimal Cantidad;
		public BigDecimal PrecioOrden;
		public BigDecimal MontoOrden;
		public String Nemotecnico;
		public String DocumentoIdentidad;
		public String IdMonedaOrden;
		public String IdTipoInstrumento;
		public String IdClasificacionTipoInstrumento;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Asignacion_Proceso {
		public Long IdAsignacion;
		public String IdEstadoAsignacion;
		public BigDecimal CantidadAsignada;
		public BigDecimal MontoAsignado;
		public String IdMonedaAsignacion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class MovFacturar_Proceso {
		public Long IdMovimientoComprobante;
		public BigDecimal Monto;
		public String IdEstadoFacturacion;
		public String IdMoneda;
		public String IdTipoComprobante;
		public String NombreTipoComprobante;
		public String NombreEstadoFacturacion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Transaccion_Proceso {
		public Long IdTransaccion;
		public String FolioTransaccion;
		public java.sql.Date FechaTransaccion;
		public String IdEstado;
		public BigDecimal Cantidad;
		public BigDecimal Monto;
		public String Nemotecnico;
		public String IdBolsa;
		public String IdMoneda;
		public BigDecimal Precio;
		public String IdTipoInstrumento;
		public String IdClasificacionTipoInstrumento;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CmbteFacturar_Proceso {
		public Long IdComprobante;
		public String IdTipoComprobante;
		public Long FolioComprobante;
		public String IdEstadoComprobante;
		public String NombreEstadoComprobante;
		public String NombreTipoComprobante;
		public BigDecimal MontoLiquidacionFinal;
		public String IdMonedaLiquidacion;
		public String DocumentoIdentidad;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class OperacionSpot_Proceso {
		public Long 			IdOperacionSpot;
		public String			IdTipoOperacion;
		public String			IdMonedaPrincipal;
		public String			IdMonedaSecundaria;
		public BigDecimal		MontoMonedaPrincipal;
		public BigDecimal		MontoMonedaSecundaria;
		public java.sql.Date	FechaOperacion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class OperacionFFMM_Proceso {
		public Long 			IdSolicitudAporteRescate;
		public String			TipoSolicitud;
		public String			IdMoneda;
		public String			IdMonedaSistema;
		public BigDecimal		Monto;
		public BigDecimal		MontoPago;
		public java.sql.Date	FechaOriginalInversion;
		public java.sql.Date	FechaCertificacion;
		public java.sql.Date	FechaLiquidacion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static <T> List<T> cargarEntidad(Connection conn,String query,List<Long> ids,Class<T> outClass) throws SQLException {
		SQL _sql = new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("ids", ids);
		if (ids.size() > 0) {
			return _sql.query(conn, query, params, outClass);
		}
		return new ArrayList<T>();
	}
	
	public static <T> List<T> cargarConector(Connection conn,String query,List<Long> ids,Class<T> outClass) throws SQLException {
		SQL _sql = new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("ids", ids);
		if (ids.size() > 0) {
			return _sql.query(conn, query, params, outClass);
		}
		return new ArrayList<T>();
	}
	
	private List<Orden_Proceso> ordenes = new ArrayList<>();
	private List<Asignacion_Proceso> asignaciones = new ArrayList<>();
	private List<Transaccion_Proceso> transacciones = new ArrayList<>();
	private List<MovFacturar_Proceso> movsfacturar = new ArrayList<>();
	private List<CmbteFacturar_Proceso> cmbtesFacturacion = new ArrayList<>();
	private List<OperacionSpot_Proceso> operacionesSpot = new ArrayList<>();
	private List<OperacionFFMM_Proceso> operacionesFFMM = new ArrayList<>();
	
	
	private List<ConectorOrdenAsignacion> conectoresOrdenAsignacion = new ArrayList<>();
	private List<ConectorTransaccionAsignacion> conectoresTransaccionAsignacion = new ArrayList<>();
	private List<ConectorAsignacionMovFacturar> conectoresAsignacionMovFact = new ArrayList<>();
	private List<ConectorMovFacturarCmbte> conectoresMovFactCmbte = new ArrayList<>();
	private List<ConectorOpSpotMovComprobante> conectorOpSpotMovComprobante = new ArrayList<>();
	private List<ConectorOpSpotFFMM> conectorOpSpotFFMM = new ArrayList<>();
	
	
	public ConsultarMapaRelaciones_Salida logica(Connection conn, ConsultarMapaRelaciones_Entrada entrada) throws Exception {
		ConsultarMapaRelaciones_Salida salida = new ConsultarMapaRelaciones_Salida();
		SQL _sql = new SQL();
		
		String llaveOrden = "ORDEN";
		String llaveAsignacion = "ASIGNACION";
		String llaveMovfact = "MOVFACT";
		String llaveCmbteFact = "CMBTEFACT";
		String llaveTransaccion = "TRANSACCION";
		String llaveSpot = "SPOT";
		String llaveFFMM = "FFMM";
		
		
		//ORDEN,ASIGNACION,TRANSACCION,MOVFACTURAR,CMBTEFACTURACION,MOVCTAINV,MOVTESORERIA,CMBTETESORERIA
		if (entrada.p_TipoEntidad.equalsIgnoreCase("TRANSACCION")) {
			
			//ENTIDAD TRANSACCIONES
			{
				List<Long> idsTransaccion = new ArrayList<Long>();
				idsTransaccion.add(entrada.p_IdEntidad);
				transacciones = cargarEntidad(conn, SQL_SELECT_TRANSACCIONES, idsTransaccion,Transaccion_Proceso.class);
			}
			
			//CONECTOR TRANSACCION - ASIGNACION
			{
				List<Long> ids = new ArrayList<Long>();
				for (Transaccion_Proceso tx : transacciones) {
					ids.add(tx.IdTransaccion);	
				}
				conectoresTransaccionAsignacion = cargarConector(conn, SQL_SELECT_TRAN_X_ASIG + SQL_WHERE_TRANSACCION, ids,ConectorTransaccionAsignacion.class);
			}
			
			//ENTIDAD ASIGNACION
			{
				List<Long> idsAsignacion = new ArrayList<Long>();
				for (ConectorTransaccionAsignacion cn : conectoresTransaccionAsignacion) {
					idsAsignacion.add(cn.IdAsignacion);
				}
				asignaciones = cargarEntidad(conn, SQL_SELECT_ASIGNACIONES, idsAsignacion,Asignacion_Proceso.class);
			}
			
			//CONECTOR ORDEN-ASIGNACION
			{
				List<Long> ids = new ArrayList<Long>();
				for (Asignacion_Proceso asig : asignaciones) {
					ids.add(asig.IdAsignacion);	
				}
				conectoresOrdenAsignacion  = cargarConector(conn, SQL_SELECT_ASIG_X_ORDEN + SQL_WHERE_ASIGNACION, ids,ConectorOrdenAsignacion.class);
			}
			
			//ENTIDAD ORDEN
			{
				List<Long> ids = new ArrayList<Long>();
				for (ConectorOrdenAsignacion cn : conectoresOrdenAsignacion) {
					ids.add(cn.IdOrden);	
				}
				ordenes = cargarEntidad(conn, SQL_SELECT_ORDENES, ids,Orden_Proceso.class);
			}
			
			//CONECTOR ASIGNACION-MOVFACTURAR
			{
				List<Long> idsAsignacion = new ArrayList<Long>();
				for (Asignacion_Proceso asig : asignaciones) {
					idsAsignacion.add(asig.IdAsignacion);	
				}
				conectoresAsignacionMovFact = cargarConector(conn, SQL_SELECT_ASIG_X_MOVFACT + SQL_WHERE_ASIGNACION, idsAsignacion,ConectorAsignacionMovFacturar.class);
			}
			
			//ENTIDAD MOVS FACTURAR
			{
				List<Long> idsMovimientoComprobante = new ArrayList<Long>();
				for (ConectorAsignacionMovFacturar cn : conectoresAsignacionMovFact) {
					idsMovimientoComprobante.add(cn.IdMovimientoComprobante);
				}
				movsfacturar = cargarEntidad(conn, SQL_SELECT_MOVFACTURAR,idsMovimientoComprobante,MovFacturar_Proceso.class);
			}
			
			//CONECTOR MOV FACTURAR - CMBTE FACTURACION
			{
				List<Long> idsMovimientoComprobante = new ArrayList<Long>();
				for (MovFacturar_Proceso mov : movsfacturar) {
					idsMovimientoComprobante.add(mov.IdMovimientoComprobante);
				}
				conectoresMovFactCmbte = cargarConector(conn, SQL_SELECT_MOV_X_CMBTE + SQL_WHERE_MOV_COMPROBANTE, idsMovimientoComprobante,ConectorMovFacturarCmbte.class);	
			}
			
			//ENTIDAD CMBTE
			{
				List<Long> idsComprobante = new ArrayList<Long>();
				for (ConectorMovFacturarCmbte movx : conectoresMovFactCmbte) {
					idsComprobante.add(movx.IdComprobante);
				}
				cmbtesFacturacion = cargarEntidad(conn, SQL_SELECT_CMBTE_FACTURACION,idsComprobante,CmbteFacturar_Proceso.class);
			}
		}
		else if (entrada.p_TipoEntidad.equalsIgnoreCase("ORDEN")) {
			//ENTIDAD ORDEN
			{
				List<Long> idsOrden = new ArrayList<Long>();
				idsOrden.add(entrada.p_IdEntidad);
				ordenes = cargarEntidad(conn, SQL_SELECT_ORDENES, idsOrden,Orden_Proceso.class);
			}
			
			//CONECTOR ORDEN-ASIGNACION
			{
				List<Long> idsOrden = new ArrayList<Long>();
				for (Orden_Proceso ord : ordenes) {
					idsOrden.add(ord.IdOrden);	
				}
				conectoresOrdenAsignacion  = cargarConector(conn, SQL_SELECT_ASIG_X_ORDEN + SQL_WHERE_ORDEN, idsOrden,ConectorOrdenAsignacion.class);
			}
			
			//ENTIDAD ASIGNACIONES
			{
				List<Long> idsAsignacion = new ArrayList<Long>();
				for (ConectorOrdenAsignacion cn : conectoresOrdenAsignacion) {
					idsAsignacion.add(cn.IdAsignacion);
				}
				asignaciones = cargarEntidad(conn, SQL_SELECT_ASIGNACIONES, idsAsignacion,Asignacion_Proceso.class);
			}
			
			//CONECTOR ASIGNACION-TRANSACCION
			{
				List<Long> ids = new ArrayList<Long>();
				for (Asignacion_Proceso asig : asignaciones) {
					ids.add(asig.IdAsignacion);	
				}
				conectoresTransaccionAsignacion = cargarConector(conn, SQL_SELECT_TRAN_X_ASIG + SQL_WHERE_ASIGNACION, ids,ConectorTransaccionAsignacion.class);
			}
			
			//ENTIDAD TRANSACCIONES
			{
				List<Long> idsTransaccion = new ArrayList<Long>();
				for (ConectorTransaccionAsignacion cn : conectoresTransaccionAsignacion) {
					idsTransaccion.add(cn.IdTransaccion);
				}
				transacciones = cargarEntidad(conn, SQL_SELECT_TRANSACCIONES,idsTransaccion,Transaccion_Proceso.class);
				
			}
			
			//CONECTOR ASIGNACION-MOVFACTURAR
			{
				List<Long> idsAsignacion = new ArrayList<Long>();
				for (Asignacion_Proceso asig : asignaciones) {
					idsAsignacion.add(asig.IdAsignacion);	
				}
				conectoresAsignacionMovFact = cargarConector(conn, SQL_SELECT_ASIG_X_MOVFACT + SQL_WHERE_ASIGNACION, idsAsignacion,ConectorAsignacionMovFacturar.class);
			}
			
			//ENTIDAD MOVS FACTURAR
			{
				List<Long> idsMovimientoComprobante = new ArrayList<Long>();
				for (ConectorAsignacionMovFacturar cn : conectoresAsignacionMovFact) {
					idsMovimientoComprobante.add(cn.IdMovimientoComprobante);
				}
				movsfacturar = cargarEntidad(conn, SQL_SELECT_MOVFACTURAR,idsMovimientoComprobante,MovFacturar_Proceso.class);
			}
			
			//CONECTOR MOV FACTURAR - CMBTE FACTURACION
			{
				List<Long> idsMovimientoComprobante = new ArrayList<Long>();
				for (MovFacturar_Proceso mov : movsfacturar) {
					idsMovimientoComprobante.add(mov.IdMovimientoComprobante);
				}
				conectoresMovFactCmbte = cargarConector(conn, SQL_SELECT_MOV_X_CMBTE + SQL_WHERE_MOV_COMPROBANTE, idsMovimientoComprobante,ConectorMovFacturarCmbte.class);	
			}
			
			//ENTIDAD CMBTE
			{
				List<Long> idsComprobante = new ArrayList<Long>();
				for (ConectorMovFacturarCmbte movx : conectoresMovFactCmbte) {
					idsComprobante.add(movx.IdComprobante);
				}
				cmbtesFacturacion = cargarEntidad(conn, SQL_SELECT_CMBTE_FACTURACION,idsComprobante,CmbteFacturar_Proceso.class);
			}
			
			//SELECT MOV TESORERIA
				//SELECT CMTE TESORERIA
			//SELECT MOV CTA INV
			
		}
		//------------------------------------------------------------------------------------------------------------------------------------
		//------------------------------------------------------------------------------------------------------------------------------------
		else if (entrada.p_TipoEntidad.equalsIgnoreCase("CMBTEFACTURACION")) {
			//ENTIDAD CMBTE
			{
				List<Long> idsComprobante = new ArrayList<Long>();
				idsComprobante.add(entrada.p_IdEntidad);
				cmbtesFacturacion = cargarEntidad(conn, SQL_SELECT_CMBTE_FACTURACION,idsComprobante,CmbteFacturar_Proceso.class);
			}
			
			//CONECTOR MOV FACTURAR - CMBTE FACTURACION
			{
				List<Long> ids = new ArrayList<Long>();
				for (CmbteFacturar_Proceso cmbte : cmbtesFacturacion) {
					ids.add(cmbte.IdComprobante);
				}
				conectoresMovFactCmbte = cargarConector(conn, SQL_SELECT_MOV_X_CMBTE  + SQL_WHERE_COMPROBANTE, ids,ConectorMovFacturarCmbte.class);	
			}
			
			//ENTIDAD MOVS FACTURAR
			{
				List<Long> idsMovimientoComprobante = new ArrayList<Long>();
				for (ConectorMovFacturarCmbte cn : conectoresMovFactCmbte) {
					idsMovimientoComprobante.add(cn.IdMovimientoComprobante);
				}
				movsfacturar = cargarEntidad(conn, SQL_SELECT_MOVFACTURAR,idsMovimientoComprobante,MovFacturar_Proceso.class);
			}
			
			//CONECTOR ASIGNACION-MOVFACTURAR
			{
				List<Long> ids = new ArrayList<Long>();
				for (MovFacturar_Proceso mov : movsfacturar) {
					ids.add(mov.IdMovimientoComprobante);	
				}
				conectoresAsignacionMovFact = cargarConector(conn, SQL_SELECT_ASIG_X_MOVFACT + SQL_WHERE_MOV_COMPROBANTE, ids,ConectorAsignacionMovFacturar.class);
			}
			
			//ENTIDAD ASIGNACIONES
			{
				List<Long> idsAsignacion = new ArrayList<Long>();
				for (ConectorAsignacionMovFacturar cn : conectoresAsignacionMovFact) {
					idsAsignacion.add(cn.IdAsignacion);
				}
				asignaciones = cargarEntidad(conn, SQL_SELECT_ASIGNACIONES, idsAsignacion,Asignacion_Proceso.class);
			}
			
			//CONECTOR ASIGNACION-TRANSACCION
			{
				List<Long> ids = new ArrayList<Long>();
				for (Asignacion_Proceso asig : asignaciones) {
					ids.add(asig.IdAsignacion);	
				}
				conectoresTransaccionAsignacion = cargarConector(conn, SQL_SELECT_TRAN_X_ASIG + SQL_WHERE_ASIGNACION, ids,ConectorTransaccionAsignacion.class);
			}
			
			//ENTIDAD TRANSACCIONES
			{
				List<Long> idsTransaccion = new ArrayList<Long>();
				for (ConectorTransaccionAsignacion cn : conectoresTransaccionAsignacion) {
					idsTransaccion.add(cn.IdTransaccion);
				}
				transacciones = cargarEntidad(conn, SQL_SELECT_TRANSACCIONES,idsTransaccion,Transaccion_Proceso.class);
			}
			
			//CONECTOR ORDEN-ASIGNACION
			{
				List<Long> ids = new ArrayList<Long>();
				for (Asignacion_Proceso asig : asignaciones) {
					ids.add(asig.IdAsignacion);	
				}
				conectoresOrdenAsignacion  = cargarConector(conn, SQL_SELECT_ASIG_X_ORDEN + SQL_WHERE_ASIGNACION, ids,ConectorOrdenAsignacion.class);
			}
			
			//ENTIDAD ORDEN
			{
				List<Long> ids = new ArrayList<Long>();
				for (ConectorOrdenAsignacion cn : conectoresOrdenAsignacion) {
					ids.add(cn.IdOrden);	
				}
				ordenes = cargarEntidad(conn, SQL_SELECT_ORDENES, ids,Orden_Proceso.class);
			}
			
			//TODO: SPOT
			//CONECTOR OPERACION SPOT - MOVIMIENTO A FACTURAR
			{
				List<Long> ids = new ArrayList<Long>();
				for (MovFacturar_Proceso mov : movsfacturar) {
					ids.add(mov.IdMovimientoComprobante);	
				}
				conectorOpSpotMovComprobante = cargarConector(conn, SQL_SELECT_SPOT_X_MOVFACT + SQL_WHERE_SPOT, ids,ConectorOpSpotMovComprobante.class);
			}
			
			//ENTIDAD OPERACION SPOT
			{
				List<Long> idsAsignacion = new ArrayList<Long>();
				for (ConectorOpSpotMovComprobante cn : conectorOpSpotMovComprobante) {
					idsAsignacion.add(cn.IdOperacionSpot);
				}
				operacionesSpot = cargarEntidad(conn, SQL_SELECT_OPERACION_SPOT, idsAsignacion, OperacionSpot_Proceso.class);
			}
		}
		//------------------------------------------------------------------------------------------------------------------------------------
		//------------------------------------------------------------------------------------------------------------------------------------
		else if (entrada.p_TipoEntidad.equalsIgnoreCase("MOVFACTURAR")) {
			//ENTIDAD MOV FACTURAR
			{
				List<Long> ids = new ArrayList<Long>();
				ids.add(entrada.p_IdEntidad);
				movsfacturar = cargarEntidad(conn, SQL_SELECT_MOVFACTURAR, ids,MovFacturar_Proceso.class);
			}
			
			//CONECTOR MOV FACTURAR - CMBTE FACTURACION
			{
				List<Long> idsMovimientoComprobante = new ArrayList<Long>();
				for (MovFacturar_Proceso mov : movsfacturar) {
					idsMovimientoComprobante.add(mov.IdMovimientoComprobante);
				}
				conectoresMovFactCmbte = cargarConector(conn, SQL_SELECT_MOV_X_CMBTE + SQL_WHERE_MOV_COMPROBANTE, idsMovimientoComprobante,ConectorMovFacturarCmbte.class);	
			}
			
			//ENTIDAD CMBTE
			{
				List<Long> idsComprobante = new ArrayList<Long>();
				for (ConectorMovFacturarCmbte movx : conectoresMovFactCmbte) {
					idsComprobante.add(movx.IdComprobante);
				}
				cmbtesFacturacion = cargarEntidad(conn, SQL_SELECT_CMBTE_FACTURACION,idsComprobante,CmbteFacturar_Proceso.class);
			}
			
			//CONECTOR ASIGNACION-MOVFACTURAR
			{
				List<Long> ids = new ArrayList<Long>();
				for (MovFacturar_Proceso mov : movsfacturar) {
					ids.add(mov.IdMovimientoComprobante);	
				}
				conectoresAsignacionMovFact = cargarConector(conn, SQL_SELECT_ASIG_X_MOVFACT + SQL_WHERE_MOV_COMPROBANTE, ids,ConectorAsignacionMovFacturar.class);
			}
			
			//ENTIDAD ASIGNACIONES
			{
				List<Long> idsAsignacion = new ArrayList<Long>();
				for (ConectorAsignacionMovFacturar cn : conectoresAsignacionMovFact) {
					idsAsignacion.add(cn.IdAsignacion);
				}
				asignaciones = cargarEntidad(conn, SQL_SELECT_ASIGNACIONES, idsAsignacion,Asignacion_Proceso.class);
			}
			
			//CONECTOR ASIGNACION-TRANSACCION
			{
				List<Long> ids = new ArrayList<Long>();
				for (Asignacion_Proceso asig : asignaciones) {
					ids.add(asig.IdAsignacion);	
				}
				conectoresTransaccionAsignacion = cargarConector(conn, SQL_SELECT_TRAN_X_ASIG + SQL_WHERE_ASIGNACION, ids,ConectorTransaccionAsignacion.class);
			}
			
			//ENTIDAD TRANSACCIONES
			{
				List<Long> idsTransaccion = new ArrayList<Long>();
				for (ConectorTransaccionAsignacion cn : conectoresTransaccionAsignacion) {
					idsTransaccion.add(cn.IdTransaccion);
				}
				transacciones = cargarEntidad(conn, SQL_SELECT_TRANSACCIONES,idsTransaccion,Transaccion_Proceso.class);
			}
			
			//CONECTOR ORDEN-ASIGNACION
			{
				List<Long> ids = new ArrayList<Long>();
				for (Asignacion_Proceso asig : asignaciones) {
					ids.add(asig.IdAsignacion);	
				}
				conectoresOrdenAsignacion  = cargarConector(conn, SQL_SELECT_ASIG_X_ORDEN + SQL_WHERE_ASIGNACION, ids,ConectorOrdenAsignacion.class);
			}
			
			//ENTIDAD ORDEN
			{
				List<Long> ids = new ArrayList<Long>();
				for (ConectorOrdenAsignacion cn : conectoresOrdenAsignacion) {
					ids.add(cn.IdOrden);	
				}
				ordenes = cargarEntidad(conn, SQL_SELECT_ORDENES, ids,Orden_Proceso.class);
			}
			
			//TODO: SPOT
			//CONECTOR OPERACION SPOT - MOVIMIENTO A FACTURAR
			{
				List<Long> ids = new ArrayList<Long>();
				for (MovFacturar_Proceso mov : movsfacturar) {
					ids.add(mov.IdMovimientoComprobante);	
				}
				conectorOpSpotMovComprobante = cargarConector(conn, SQL_SELECT_SPOT_X_MOVFACT + SQL_WHERE_SPOT, ids,ConectorOpSpotMovComprobante.class);
			}
			
			//ENTIDAD OPERACION SPOT
			{
				List<Long> idsAsignacion = new ArrayList<Long>();
				for (ConectorOpSpotMovComprobante cn : conectorOpSpotMovComprobante) {
					idsAsignacion.add(cn.IdOperacionSpot);
				}
				operacionesSpot = cargarEntidad(conn, SQL_SELECT_OPERACION_SPOT, idsAsignacion, OperacionSpot_Proceso.class);
			}
						
			
			//TODO: FFMM
			//CONECTOR OPERACION SPOT - FFMM
			{
				List<Long> ids = new ArrayList<Long>();
				for (MovFacturar_Proceso mov : movsfacturar) {
					ids.add(mov.IdMovimientoComprobante);	
				}
				conectorOpSpotFFMM = cargarConector(conn, SQL_SELECT_SPOT_X_OPEFFMM + SQL_WHERE_FFMM, ids,ConectorOpSpotFFMM.class);
			}
			
			
			//ENTIDAD OPERACION SPOT
			{
				List<Long> idsSolicitudAporteRescate = new ArrayList<Long>();
				for (ConectorOpSpotFFMM cn : conectorOpSpotFFMM) {
					idsSolicitudAporteRescate.add(cn.IdSolicitudAporteRescate);
				}
				operacionesFFMM = cargarEntidad(conn, SQL_SELECT_OPERACION_FFMM, idsSolicitudAporteRescate, OperacionFFMM_Proceso.class);
			}
		}
		//------------------------------------------------------------------------------------------------------------------------------------
		//------------------------------------------------------------------------------------------------------------------------------------
		else if (entrada.p_TipoEntidad.equalsIgnoreCase("ASIGNACION")) {
			//ENTIDAD ASIGNACION
			{
				List<Long> ids = new ArrayList<Long>();
				ids.add(entrada.p_IdEntidad);
				asignaciones = cargarEntidad(conn, SQL_SELECT_ASIGNACIONES, ids,Asignacion_Proceso.class);
			}
			
			//CONECTOR ORDEN-ASIGNACION
			{
				List<Long> ids = new ArrayList<Long>();
				for (Asignacion_Proceso asig : asignaciones) {
					ids.add(asig.IdAsignacion);	
				}
				conectoresOrdenAsignacion  = cargarConector(conn, SQL_SELECT_ASIG_X_ORDEN + SQL_WHERE_ASIGNACION, ids,ConectorOrdenAsignacion.class);
			}
			
			//ENTIDAD ORDEN
			{
				List<Long> ids = new ArrayList<Long>();
				for (ConectorOrdenAsignacion cn : conectoresOrdenAsignacion) {
					ids.add(cn.IdOrden);	
				}
				ordenes = cargarEntidad(conn, SQL_SELECT_ORDENES, ids,Orden_Proceso.class);
			}
			
			//CONECTOR ASIGNACION-TRANSACCION
			{
				List<Long> ids = new ArrayList<Long>();
				for (Asignacion_Proceso asig : asignaciones) {
					ids.add(asig.IdAsignacion);	
				}
				conectoresTransaccionAsignacion = cargarConector(conn, SQL_SELECT_TRAN_X_ASIG + SQL_WHERE_ASIGNACION, ids,ConectorTransaccionAsignacion.class);
			}
			
			//ENTIDAD TRANSACCIONES
			{
				List<Long> idsTransaccion = new ArrayList<Long>();
				for (ConectorTransaccionAsignacion cn : conectoresTransaccionAsignacion) {
					idsTransaccion.add(cn.IdTransaccion);
				}
				transacciones = cargarEntidad(conn, SQL_SELECT_TRANSACCIONES,idsTransaccion,Transaccion_Proceso.class);
			}
			
			//CONECTOR ASIGNACION-MOVFACTURAR
			{
				List<Long> idsAsignacion = new ArrayList<Long>();
				for (Asignacion_Proceso asig : asignaciones) {
					idsAsignacion.add(asig.IdAsignacion);	
				}
				conectoresAsignacionMovFact = cargarConector(conn, SQL_SELECT_ASIG_X_MOVFACT + SQL_WHERE_ASIGNACION, idsAsignacion,ConectorAsignacionMovFacturar.class);
			}
			
			//ENTIDAD MOVS FACTURAR
			{
				List<Long> idsMovimientoComprobante = new ArrayList<Long>();
				for (ConectorAsignacionMovFacturar cn : conectoresAsignacionMovFact) {
					idsMovimientoComprobante.add(cn.IdMovimientoComprobante);
				}
				movsfacturar = cargarEntidad(conn, SQL_SELECT_MOVFACTURAR,idsMovimientoComprobante,MovFacturar_Proceso.class);
			}
			
			//CONECTOR MOV FACTURAR - CMBTE FACTURACION
			{
				List<Long> idsMovimientoComprobante = new ArrayList<Long>();
				for (MovFacturar_Proceso mov : movsfacturar) {
					idsMovimientoComprobante.add(mov.IdMovimientoComprobante);
				}
				conectoresMovFactCmbte = cargarConector(conn, SQL_SELECT_MOV_X_CMBTE + SQL_WHERE_MOV_COMPROBANTE, idsMovimientoComprobante,ConectorMovFacturarCmbte.class);	
			}
			
			//ENTIDAD CMBTE
			{
				List<Long> idsComprobante = new ArrayList<Long>();
				for (ConectorMovFacturarCmbte movx : conectoresMovFactCmbte) {
					idsComprobante.add(movx.IdComprobante);
				}
				cmbtesFacturacion = cargarEntidad(conn, SQL_SELECT_CMBTE_FACTURACION,idsComprobante,CmbteFacturar_Proceso.class);
			}
			
			
		}
		
		//OPERACIONESFFMM
		for (OperacionFFMM_Proceso ffmm : operacionesFFMM) {
		   Nodo_Salida n = new Nodo_Salida();
		    n.IdentificadorEnlace	= ffmm.IdSolicitudAporteRescate;
		    n.IdentificadorNodo     = llaveFFMM + ffmm.IdSolicitudAporteRescate;
		    n.TipoEnlace            = "consultaOperacionFFMM";
		    n.Titulo                = "FFMM " + ffmm.IdSolicitudAporteRescate;
		    n.NombreParametroEnlace = "IdSolicitudAporteRescate";
		    n.Linea1 				= ffmm.TipoSolicitud;
		    n.Linea2 				= ffmm.FechaCertificacion.toString();
		    n.Linea3				= MonedaFormato.formatear(conn, ffmm.Monto, MonedaFormato.CANTIDAD, ffmm.IdMoneda).valorConFormato + " " + ffmm.IdMoneda;
			n.Linea4 				= MonedaFormato.formatear(conn, ffmm.MontoPago, MonedaFormato.CANTIDAD, ffmm.IdMonedaSistema).valorConFormato + " " + ffmm.IdMonedaSistema;
			salida.p_out_entidades.add(n);
		}
		
		//OPERACIONESSPOT
		for (OperacionSpot_Proceso spt : operacionesSpot) {
			Nodo_Salida n = new Nodo_Salida();
			n.IdentificadorEnlace	= spt.IdOperacionSpot;
			n.IdentificadorNodo 	= llaveSpot + spt.IdOperacionSpot;
			n.TipoEnlace 			= "consutaOperacionSpot";
			n.Titulo 				= "Spot " + spt.IdOperacionSpot;
			n.NombreParametroEnlace = "IdOperacionSpot";
			n.Linea1 				= spt.IdTipoOperacion;
			n.Linea2 				= spt.FechaOperacion.toString();
			n.Linea3 				= MonedaFormato.formatear(conn, spt.MontoMonedaPrincipal , MonedaFormato.CANTIDAD, spt.IdMonedaPrincipal ).valorConFormato + " " + spt.IdMonedaPrincipal;
			n.Linea4 				= MonedaFormato.formatear(conn, spt.MontoMonedaSecundaria, MonedaFormato.CANTIDAD, spt.IdMonedaSecundaria).valorConFormato + " " + spt.IdMonedaSecundaria;
			salida.p_out_entidades.add(n);
		}
		
		//ORDENES
		for (Orden_Proceso ord : ordenes) {
			Nodo_Salida n = new Nodo_Salida();
			n.IdentificadorEnlace = ord.IdOrden;
			n.IdentificadorNodo = llaveOrden + ord.IdOrden;
			n.TipoEnlace = "DetalleOrden";
			n.Titulo = "Orden " + ord.IdOrden;
			n.NombreParametroEnlace = "IdOrden";
			n.Linea1 = ord.DocumentoIdentidad;
			n.Linea2 = ord.Nemotecnico;
			n.Linea3 = MonedaFormato.formatear(conn, ord.Cantidad, MonedaFormato.CANTIDAD, ord.IdMonedaOrden).valorConFormato +  " - " + MonedaFormato.formatear(conn, ord.PrecioOrden, MonedaFormato.PRECIO, ord.IdMonedaOrden).valorConFormato + " " + ord.IdMonedaOrden;
			n.Linea4 = MonedaFormato.formatear(conn, ord.MontoOrden, MonedaFormato.MONTO, ord.IdMonedaOrden).valorConFormato + " " + ord.IdMonedaOrden;
			salida.p_out_entidades.add(n);
		}
		
		//ASIGNACIONES
		for (Asignacion_Proceso asig : asignaciones) {
			Nodo_Salida n = new Nodo_Salida();
			n.IdentificadorEnlace = asig.IdAsignacion;
			n.IdentificadorNodo = llaveAsignacion + asig.IdAsignacion;
			n.TipoEnlace = "DetalleAsignacion";
			n.Titulo = "Asignacion " + asig.IdAsignacion;
			n.NombreParametroEnlace = "IdAsignacion";
			n.Linea1 = MonedaFormato.formatear(conn, asig.CantidadAsignada, MonedaFormato.CANTIDAD, asig.IdMonedaAsignacion).valorConFormato;
			n.Linea2 = MonedaFormato.formatear(conn, asig.MontoAsignado, MonedaFormato.MONTO, asig.IdMonedaAsignacion).valorConFormato + " " + asig.IdMonedaAsignacion;
			n.Linea3 = "";
			n.Linea4 = "";
			salida.p_out_entidades.add(n);
		}
		
		//TRANSACCIONES
		for (Transaccion_Proceso tran : transacciones) {
			Nodo_Salida n = new Nodo_Salida();
			n.IdentificadorEnlace = tran.IdTransaccion;
			n.IdentificadorNodo = llaveTransaccion + tran.IdTransaccion;
			n.TipoEnlace = "DetalleTransaccion";
			n.Titulo = "Transaccion " + tran.IdTransaccion;
			n.NombreParametroEnlace = "IdTransaccion";
			n.Linea1 = tran.FolioTransaccion + " " + Fecha.toStringDate(tran.FechaTransaccion) + " " + tran.IdBolsa;
			n.Linea2 = tran.Nemotecnico;
			n.Linea3 = MonedaFormato.formatear(conn, tran.Cantidad, MonedaFormato.CANTIDAD, tran.IdMoneda).valorConFormato +  " - " + MonedaFormato.formatear(conn, tran.Precio, MonedaFormato.PRECIO, tran.IdMoneda).valorConFormato + " " + tran.IdMoneda;
			n.Linea4 = MonedaFormato.formatear(conn, tran.Monto, MonedaFormato.MONTO, tran.IdMoneda).valorConFormato + " " + tran.IdMoneda;;
			salida.p_out_entidades.add(n);
		}

		//MOV FACTURAR
		for (MovFacturar_Proceso mov : movsfacturar) {
			Nodo_Salida n = new Nodo_Salida();
			n.IdentificadorEnlace = mov.IdMovimientoComprobante;
			n.IdentificadorNodo = llaveMovfact + mov.IdMovimientoComprobante;
			n.TipoEnlace = "DetalleMovimientoComprobante";
			n.Titulo = "Mov. Facturar " + mov.IdMovimientoComprobante;
			n.NombreParametroEnlace = "IdMovimientoComprobante";
			n.Linea1 = MonedaFormato.formatear(conn, mov.Monto, MonedaFormato.MONTO, mov.IdMoneda).valorConFormato + " " + mov.IdMoneda;
			n.Linea2 = mov.NombreTipoComprobante;
			n.Linea3 = mov.NombreEstadoFacturacion;
			salida.p_out_entidades.add(n);
		}
		
		//CMBTE FACTURACION
		for (CmbteFacturar_Proceso cmbte : cmbtesFacturacion) {
			Nodo_Salida n = new Nodo_Salida();
			n.IdentificadorEnlace = cmbte.IdComprobante;
			n.IdentificadorNodo = llaveCmbteFact + cmbte.IdComprobante;
			n.TipoEnlace = "DetalleComprobanteFacturacion";
			n.Titulo = "Cmbte. Fact. " + cmbte.IdComprobante;
			n.NombreParametroEnlace = "IdComprobante";
			n.Linea1 = cmbte.DocumentoIdentidad;
			n.Linea2 = MonedaFormato.formatear(conn, cmbte.MontoLiquidacionFinal.abs(), MonedaFormato.MONTO, cmbte.IdMonedaLiquidacion).valorConFormato + " " + cmbte.IdMonedaLiquidacion;
			n.Linea3 = cmbte.NombreEstadoComprobante;
			n.Linea4 = "";
			salida.p_out_entidades.add(n);
		}
		
		//SPOT - MOVIMIENTO FACTURAR
		for (ConectorOpSpotMovComprobante cn : conectorOpSpotMovComprobante) {
			Conector_Salida conector = new Conector_Salida();
			conector.IdentificadorDesde = llaveSpot + cn.IdOperacionSpot;
			conector.IdentificadorHacia = llaveMovfact + cn.IdMovimientoComprobante;
			salida.p_out_conectores.add(conector);
		}
		//ORDEN - ASIGNACION
		
		for (ConectorOrdenAsignacion cn : conectoresOrdenAsignacion) {
			Conector_Salida conector = new Conector_Salida();
			conector.IdentificadorDesde = llaveOrden + cn.IdOrden;
			conector.IdentificadorHacia = llaveAsignacion + cn.IdAsignacion;
			salida.p_out_conectores.add(conector);
		}
		
		//TRANSACCION - ASIGNACION
		for (ConectorTransaccionAsignacion cn : conectoresTransaccionAsignacion) {
			Conector_Salida conector = new Conector_Salida();
			conector.IdentificadorDesde = llaveAsignacion + cn.IdAsignacion;
			conector.IdentificadorHacia = llaveTransaccion + cn.IdTransaccion;
			salida.p_out_conectores.add(conector);
		}
		
		//ASIGNACION - MOV FACTURAR
		for (ConectorAsignacionMovFacturar cn : conectoresAsignacionMovFact) {
			Conector_Salida conector = new Conector_Salida();
			conector.IdentificadorDesde = llaveAsignacion + cn.IdAsignacion;
			conector.IdentificadorHacia = llaveMovfact + cn.IdMovimientoComprobante;
			salida.p_out_conectores.add(conector);
		}
		
		//ASIGNACION - MOV FACTURAR
		for (ConectorMovFacturarCmbte cn : conectoresMovFactCmbte) {
			Conector_Salida conector = new Conector_Salida();
			conector.IdentificadorDesde = llaveMovfact + cn.IdMovimientoComprobante;
			conector.IdentificadorHacia = llaveCmbteFact + cn.IdComprobante;
			salida.p_out_conectores.add(conector);
		}
		
		return salida ;
	}

	
}
