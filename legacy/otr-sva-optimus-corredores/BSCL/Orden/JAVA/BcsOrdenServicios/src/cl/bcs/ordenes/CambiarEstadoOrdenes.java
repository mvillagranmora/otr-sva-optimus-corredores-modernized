package cl.bcs.ordenes;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.JsonObject;
import cl.bcs.ordenes.pojo.ConstantesOrdenes;
import cl.bcs.ordenes.pojo.Orden;
import cl.bcs.plataforma.pla.NotificarEstadoOrdenesOWP;
import cl.bcs.plataforma.pla.NotificarEstadoOrdenesOWP.NotificarEstadoOrdenes_Entrada;
import cl.bcs.plataforma.pla.NotificarEstadoOrdenesOWP.OrdenModificada;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CambiarEstadoOrdenes implements JavaCall {
	
	private final String SQL_UPDATE_ESTADO_ORDEN = loadResourceToString("cl/bcs/ordenes/sql/CambiarEstadoOrdenes_UpdateEstadoOrdenes.sql");	
	private final String SQL_SELECT_ORDEN = loadResourceToString("cl/bcs/ordenes/sql/CambiarEstadoOrdenes_SelectOrdenes.sql");
	private final String SQL_SELECT_ORDENES_MODIFICADAS = loadResourceToString("cl/bcs/ordenes/sql/OrdenesModificadas_Select.sql");
	private final Log log = new Log("ORD.CambiarEstadoOrdenes");
	private final ConstantesOrdenes _c= new ConstantesOrdenes();
	
	public CambiarEstadoOrdenes_Salida logica(Connection conn,CambiarEstadoOrdenes_Entrada entrada) throws Exception {
		SQL _sql = new SQL();
		CambiarEstadoOrdenes_Salida salida = new CambiarEstadoOrdenes_Salida();
		
		NotificarEstadoOrden.NotificarEstadoOrden_Entrada  entradaNotificarEstadoOrden = new NotificarEstadoOrden.NotificarEstadoOrden_Entrada();
		entradaNotificarEstadoOrden.p_Ordenes = new ArrayList<NotificarEstadoOrden.Orden>();
		
		HashMap<Long,Orden> hashOrdenes = new HashMap<Long,Orden>();
		{
			HashMap<String,Object> params = new HashMap<>();
			ArrayList<Long> idsOrden = new ArrayList<>();
			for (Orden_Entrada ord : entrada.p_Ordenes) {
				idsOrden.add(ord.IdOrden);
			}
			params.put("idsOrden", idsOrden);
			List<Orden> listaOrdenes =_sql.query(conn, SQL_SELECT_ORDEN, params, Orden.class);
			for (Orden ord : listaOrdenes) {
				hashOrdenes.put(ord.IdOrden, ord);
			}
		}
		
		List<Orden> ordenes = new ArrayList<>();
		List<Long> ordenesConErrores = new ArrayList<>();
		for (Orden_Entrada ord : entrada.p_Ordenes) {
			
			Orden o1 = hashOrdenes.get(ord.IdOrden);
			if (o1 == null) {
				String codigo = "ORDEN_NO_EXISTE";
				String desc = Diccionario.getMensaje(conn, entrada.p_IdEmpresa, codigo).add("IdOrden", ord.IdOrden).msg();
				salida.P_OUT_MENSAJES.add(new MensajeSalida(ord.IdOrden,MensajeSalida.MENSAJE_ERROR,codigo,desc));
				ordenesConErrores.add(ord.IdOrden);
				continue;
			}
			boolean ok = false;
			if (o1.IdEstadoOrden.equalsIgnoreCase(_c.ESTADO_ORDEN_PENDIENTE_AUTORIZAR) && 
					(ord.IdNuevoEstado.equalsIgnoreCase(_c.ESTADO_ORDEN_ANULADA) 
					|| ord.IdNuevoEstado.equalsIgnoreCase(_c.ESTADO_ORDEN_NO_AUTORIZADA)
					|| ord.IdNuevoEstado.equalsIgnoreCase(_c.ESTADO_ORDEN_INGRESADA))) {
				
				
				ok = true;
			}
			else if (o1.IdEstadoOrden.equalsIgnoreCase(_c.ESTADO_ORDEN_INGRESADA) 
					&& ord.IdNuevoEstado.equalsIgnoreCase(_c.ESTADO_ORDEN_ANULADA)) {
				ok = true;
				
			}
			else if (o1.IdEstadoOrden.equalsIgnoreCase(_c.ESTADO_ORDEN_INGRESADA)
					&& o1.IdTipoCanalDistribucion.equalsIgnoreCase(_c.TIPO_CANAL_DISTRIBUCION_MANUAL)
					&& ord.IdNuevoEstado.equalsIgnoreCase(_c.ESTADO_ORDEN_OFERTADA)) {
				ok = true;
			}
			else if (o1.IdEstadoOrden.equalsIgnoreCase(_c.ESTADO_ORDEN_INGRESADA)
					&& !o1.IdTipoCanalDistribucion.equalsIgnoreCase(_c.TIPO_CANAL_DISTRIBUCION_MANUAL)
					&& ord.IdNuevoEstado.equalsIgnoreCase(_c.ESTADO_ORDEN_EN_RUTEO)) {
				ok = true;
			}
			else if (o1.IdEstadoOrden.equalsIgnoreCase(_c.ESTADO_ORDEN_OFERTADA)
					&& o1.IdTipoCanalDistribucion.equalsIgnoreCase(_c.TIPO_CANAL_DISTRIBUCION_MANUAL)
					&& ord.IdNuevoEstado.equalsIgnoreCase(_c.ESTADO_ORDEN_INGRESADA)) {
				ok = true;
			}
			else if (o1.IdEstadoOrden.equalsIgnoreCase(_c.ESTADO_ORDEN_OFERTADA)
					&& o1.IdTipoCanalIngreso.equalsIgnoreCase(_c.TIPO_CANAL_INGRESO_TRADER)
					&& o1.IdTipoCanalDistribucion.equalsIgnoreCase(_c.TIPO_CANAL_DISTRIBUCION_MANUAL)
					&& ord.IdNuevoEstado.equalsIgnoreCase(_c.ESTADO_ORDEN_ANULADA)) {
				ok = true;
			}
			else if (o1.IdEstadoOrden.equalsIgnoreCase(_c.ESTADO_ORDEN_OFERTADA)
					&& !o1.IdTipoCanalDistribucion.equalsIgnoreCase(_c.TIPO_CANAL_DISTRIBUCION_MANUAL)
					&& ord.IdNuevoEstado.equalsIgnoreCase(_c.ESTADO_ORDEN_ANULADA)) {
				ok = true;
			}
			else if (o1.IdEstadoOrden.equalsIgnoreCase(_c.ESTADO_ORDEN_OFERTADA)
					&& !o1.IdTipoCanalDistribucion.equalsIgnoreCase(_c.TIPO_CANAL_DISTRIBUCION_MANUAL)
					&& ord.IdNuevoEstado.equalsIgnoreCase(_c.ESTADO_ORDEN_ANULADA_PARCIAL)) {
				ok = true;
			}
			else if (o1.IdEstadoOrden.equalsIgnoreCase(_c.ESTADO_ORDEN_EN_RUTEO)
					&& ord.IdNuevoEstado.equalsIgnoreCase(_c.ESTADO_ORDEN_OFERTADA)) {
				ok = true;
			}
			else if (o1.IdEstadoOrden.equalsIgnoreCase(_c.ESTADO_ORDEN_EN_RUTEO)
					&& ord.IdNuevoEstado.equalsIgnoreCase(_c.ESTADO_ORDEN_ANULADA)) {
				ok = true;
			}
			else if (o1.IdEstadoOrden.equalsIgnoreCase(_c.ESTADO_ORDEN_EN_RUTEO)
					&& ord.IdNuevoEstado.equalsIgnoreCase(_c.ESTADO_ORDEN_RECHAZADA)) {
				ok = true;
				
			}
			else {
				ok = false;
			}
			
			
			if (ok) {
				Orden o = new Orden();
				o.IdOrden = ord.IdOrden;
				o.IdEstadoOrden = ord.IdNuevoEstado;
				if (ord.IdNuevoEstado.equalsIgnoreCase(_c.ESTADO_ORDEN_ANULADA) || ord.IdNuevoEstado.equalsIgnoreCase(_c.ESTADO_ORDEN_ANULADA_PARCIAL) || ord.IdNuevoEstado.equalsIgnoreCase(_c.ESTADO_ORDEN_RECHAZADA)) {
					o.OrdenVigente = _c.NO;
					entradaNotificarEstadoOrden.p_Ordenes.add(new NotificarEstadoOrden.Orden(ord.IdOrden));
				}
				ordenes.add(o);
			}
			else {
				String codigo = "CAMBIO_ESTADO_INVALIDO";
				String desc = Diccionario.getMensaje(conn,entrada.p_IdEmpresa, codigo).msg();
				salida.P_OUT_MENSAJES.add(new MensajeSalida(ord.IdOrden,MensajeSalida.MENSAJE_OK,codigo,desc));
			}
			
		}
		
		if (ordenes.size() > 0) {
			_sql.batch(conn, SQL_UPDATE_ESTADO_ORDEN, ordenes, Orden.class);
			for (Orden ord : ordenes) {
				String codigo = "CAMBIO_DE_ESTADO_REALIZADO";
				String desc = Diccionario.getMensaje(conn, entrada.p_IdEmpresa,codigo).msg();
				salida.P_OUT_MENSAJES.add(new MensajeSalida(ord.IdOrden,MensajeSalida.MENSAJE_OK,codigo,desc));
			}
		}
		
		try {
			//notificar anulacion de ordenes
			NotificarEstadoOrden notificarEstadoOrden = new NotificarEstadoOrden();
			if(entradaNotificarEstadoOrden.p_Ordenes.size()>0) {
				entradaNotificarEstadoOrden.p_IdEmpresa = entrada.p_IdEmpresa;
				entradaNotificarEstadoOrden.p_IdUsuario = new Long(0);
				entradaNotificarEstadoOrden.p_IdModulo = "ORD";
				notificarEstadoOrden.logica(conn, entradaNotificarEstadoOrden);
			}
		}catch(Exception e) {
			log.write("ERROR AL NOTIFICAR CAMBIO DE ESTADO ORDENES");
		}
		
		
		
		//Notificacion OpenWorkPlace
		String notificaOWP = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.NotificarEstadoOrdenOWP");
		if("S".equalsIgnoreCase(notificaOWP)){
			HashMap<String,Object> parametro = new HashMap<>();
			ArrayList<Long> idsOrden = new ArrayList<>();
			for (Orden ord : ordenes) {
				if("RV".equalsIgnoreCase(ord.IdClasificacionTipoInstrumento) && "OWP".equals(ord.IdTipoCanalIngreso)){
					idsOrden.add(ord.IdOrden);
				}
			}
			parametro.put("p_IdOrdenes", idsOrden);
			List<OrdenModificada> listaOrdenesModificadas  = _sql.query(conn, SQL_SELECT_ORDENES_MODIFICADAS, parametro, OrdenModificada.class);
			
			NotificarEstadoOrdenesOWP servicioNotOwp = new NotificarEstadoOrdenesOWP();
			List<NotificarEstadoOrdenes_Entrada> listOrdenNotiOWP = new ArrayList<NotificarEstadoOrdenes_Entrada>();
			
			for (OrdenModificada ord : listaOrdenesModificadas) {
				NotificarEstadoOrdenes_Entrada orden= new NotificarEstadoOrdenes_Entrada();
				orden.IdEmpresa = entrada.p_IdEmpresa;
				orden.IdUsuario = entrada.p_IdUsuario;
				orden.Solsec = ord.IdOrden;
				orden.NTitulos = ord.Cantidad;
				orden.Precio = ord.PrecioOrden;
				orden.Fecha = ord.FechaOperacion;
				orden.Estado = "FAC".equalsIgnoreCase(ord.IdEstadoFacturacion) ? "FAC" : ord.IdEstadoOrden;
				orden.Comision = ord.Comision;
				orden.Gastos = ord.Gasto;
				orden.Mercado = "A";
				orden.Divisa = ord.IdMonedaOrden;
				orden.TipoCambio = ord.TipoCambio;
				orden.SolsecAsoc = "";
				orden.Nemo = ord.Nemotecnico;
				listOrdenNotiOWP.add(orden);
			}
			
			if(listOrdenNotiOWP.size() > 0){
				servicioNotOwp.go(conn, listOrdenNotiOWP);
			}
		}
		
		return salida;
	}

	public static class Orden_Entrada {
		public Long IdOrden;
		public String IdNuevoEstado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CambiarEstadoOrdenes_Entrada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public List<Orden_Entrada> p_Ordenes;
		
		public CambiarEstadoOrdenes_Entrada() {
			p_Ordenes = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	//SALIDAS
	public static class CambiarEstadoOrdenes_Salida {
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public CambiarEstadoOrdenes_Salida() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
		
	public static void main(String[]args) throws Exception{
		try (Connection con = PlataformaUtils.getDESAConnection()){
			
		}	
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		throw new Exception("call no implementado");
	}
}
	