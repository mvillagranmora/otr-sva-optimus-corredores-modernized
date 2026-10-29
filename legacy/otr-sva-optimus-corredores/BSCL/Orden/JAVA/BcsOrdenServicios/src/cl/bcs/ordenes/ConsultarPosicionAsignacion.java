package cl.bcs.ordenes;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.dbcp2.BasicDataSource;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.ordenes.EjecutarDesasignacion.EjecutarDesasignacionEntrada;
import cl.bcs.ordenes.EjecutarDesasignacion.EjecutarDesasignacionSalida;
import cl.bcs.ordenes.utils.OrdenUtils;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.AdvertenciaDetalle;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.ErrorDetalle;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.InformacionDetalle;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class ConsultarPosicionAsignacion implements JavaCall {
	
	private final String SQL_SELECT_ORDENES = loadResourceToString("cl/bcs/ordenes/sql/ConsultarPosicionAsignacion_SelectOrdenes.sql");
	private final String SQL_SELECT_TRANSACCIONES = loadResourceToString("cl/bcs/ordenes/sql/ConsultarPosicionAsignacion_SelectTransacciones.sql");
	private final String SQL_SELECT_INSTRUMENTOS = loadResourceToString("cl/bcs/ordenes/sql/ConsultarPosicion_SelectInstrumentos.sql");
	private final String SQL_SELECT_TIPOS_INSTRUMENTO = loadResourceToString("cl/bcs/ordenes/sql/ConsultarPosicion_SelectTipoInstrumento.sql");

	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = new Gson();		
		ConsultarPosicion_Entrada in=gson.fromJson(jin.toString() ,ConsultarPosicion_Entrada.class);
		ConsultarPosicion_Salida out=logica(conn,in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		for (MensajeSalida msje : out.P_OUT_MENSAJES) {
			msje.descripcion = Diccionario.getMensaje(conn, in.p_IdEmpresa, msje.codigo, new HashMap());
		}
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public ConsultarPosicion_Salida logica(Connection conn,ConsultarPosicion_Entrada entrada) throws Exception {
		SQL _sql = new SQL();
		ConsultarPosicion_Salida salida = new ConsultarPosicion_Salida();
		String idMonedaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
		HashMap<Long,Instrumento_Proceso> instrumentos = new HashMap<>();
		{
			HashMap<String,Object> params1 = new HashMap<>();
			params1.put("IdClasificacionTipoInstrumento", entrada.p_IdClasificacionTipoInstrumento);
			List<Instrumento_Proceso> _instrumentos = _sql.query(conn, SQL_SELECT_INSTRUMENTOS, params1, Instrumento_Proceso.class);
			for (Instrumento_Proceso inst : _instrumentos) {
				instrumentos.put(inst.IdInstrumento,inst);
			}
		}
		
		HashMap<String,TipoInstrumento_Proceso> tiposInstrumentos = new HashMap<>();
		{
			List<TipoInstrumento_Proceso> _tiposInstrumentos = _sql.query(conn, SQL_SELECT_TIPOS_INSTRUMENTO, new HashMap<>(), TipoInstrumento_Proceso.class);
			for (TipoInstrumento_Proceso tip : _tiposInstrumentos) {
				tiposInstrumentos.put(tip.IdTipoInstrumento,tip);
			}
		}
		
		List<Orden_Proceso> ordenes = null;
		List<Transaccion_Proceso> transacciones = null;
		{
			HashMap<String,Object> params1 = new HashMap<>();
			params1.put("FechaConsulta", entrada.p_FechaConsulta);
			ordenes = _sql.query(conn, SQL_SELECT_ORDENES, params1, Orden_Proceso.class);
			transacciones = _sql.query(conn, SQL_SELECT_TRANSACCIONES, params1, Transaccion_Proceso.class);
				
		}
		
		for (Orden_Proceso ord : ordenes) {
			
			Posicion_Salida pos = new Posicion_Salida();
			pos.EsVentaCorta = ord.EsVentaCorta;
			if (instrumentos.get(ord.IdInstrumento) == null) continue;
			pos.IdClasificacionTipoInstrumento = instrumentos.get(ord.IdInstrumento).IdClasificacionTipoInstrumento;
			if (ord.IdTipoOperacion.equalsIgnoreCase("COMSIM") && ord.IdTipoCanalIngreso.equalsIgnoreCase("AGE")) {
				pos.IdInstrumento = ord.IdInstrumento;
			}
			else if (ord.IdTipoOperacion.equalsIgnoreCase("COMSIM") && !ord.IdTipoCanalIngreso.equalsIgnoreCase("AGE")) {
				pos.IdInstrumento = ord.IdInstrumentoSubyacente;
			}
				
			else if (ord.IdTipoOperacion.equalsIgnoreCase("VENSIM")) {
				pos.IdInstrumento = ord.IdInstrumentoSubyacente;
			}
			else {
				pos.IdInstrumento = ord.IdInstrumento;
			}
			
			pos.IdTipoInstrumento = ord.IdTipoInstrumento;
			pos.NombreTipoInstrumento = tiposInstrumentos.get(ord.IdTipoInstrumento).NombreTipoInstrumento;
			pos.Nemotecnico = instrumentos.get(pos.IdInstrumento).Nemotecnico;
			
			if (!salida.P_OUT_POSICION.contains(pos)) {
				salida.P_OUT_POSICION.add(pos);
			}
			
			pos = salida.P_OUT_POSICION.get(salida.P_OUT_POSICION.indexOf(pos));
			if (pos.OrdenadoCompra == null) {
				pos.OrdenadoCompra = BigDecimal.ZERO;
			}
			if (pos.OrdenadoVenta == null) {
				pos.OrdenadoVenta = BigDecimal.ZERO;
			}

			pos.OrdenadoCompra = pos.OrdenadoCompra.add(ord.OrdenadoCompra);
			pos.OrdenadoVenta = pos.OrdenadoVenta.add(ord.OrdenadoVenta);

			
		}
		
		for (Transaccion_Proceso tran : transacciones) {
			
			Posicion_Salida pos = new Posicion_Salida();
			pos.EsVentaCorta = tran.EsVentaCorta;
			if (instrumentos.get(tran.IdInstrumento) == null) continue;
			pos.IdClasificacionTipoInstrumento = instrumentos.get(tran.IdInstrumento).IdClasificacionTipoInstrumento;
			if (tran.IdTipoOperacion.equalsIgnoreCase("COMSIM")) {
				pos.IdInstrumento = tran.IdInstrumento;
			}
			else if (tran.IdTipoOperacion.equalsIgnoreCase("VENSIM")) {
				pos.IdInstrumento = tran.IdInstrumentoSubyacente;
			}
			else {
				pos.IdInstrumento = tran.IdInstrumento;
			}
			
			pos.IdTipoInstrumento = tran.IdTipoInstrumento;
			pos.NombreTipoInstrumento = tiposInstrumentos.get(tran.IdTipoInstrumento).NombreTipoInstrumento;
			pos.Nemotecnico = instrumentos.get(pos.IdInstrumento).Nemotecnico;
			
			if (!salida.P_OUT_POSICION.contains(pos)) {
				salida.P_OUT_POSICION.add(pos);
			}
			
			pos = salida.P_OUT_POSICION.get(salida.P_OUT_POSICION.indexOf(pos));
			
			
			if (pos.TransadoCompra == null) {
				pos.TransadoCompra = BigDecimal.ZERO;
			}
			if (pos.TransadoVenta == null) {
				pos.TransadoVenta = BigDecimal.ZERO;
			}
			

			pos.TransadoVenta = pos.TransadoVenta.add(tran.TransadoVenta);
			pos.TransadoCompra = pos.TransadoCompra.add(tran.TransadoCompra);
			
		}
		
		for (Posicion_Salida pos : salida.P_OUT_POSICION) {
			pos = salida.P_OUT_POSICION.get(salida.P_OUT_POSICION.indexOf(pos));
			
			if (pos.OrdenadoCompra == null)
				pos.OrdenadoCompra = BigDecimal.ZERO;
			if (pos.OrdenadoVenta == null)
				pos.OrdenadoVenta = BigDecimal.ZERO;
			if (pos.TransadoCompra == null)
				pos.TransadoCompra = BigDecimal.ZERO;
			if (pos.TransadoVenta == null)
				pos.TransadoVenta = BigDecimal.ZERO;

			pos.IdMoneda = idMonedaSistema;
			
			if (pos.EsVentaCorta.equalsIgnoreCase("N"))
				pos.EsVentaCortaUI = "NO";
			if (pos.EsVentaCorta.equalsIgnoreCase("S"))
				pos.EsVentaCortaUI = "SI";
			
			pos.PorComprar = (pos.OrdenadoCompra.subtract(pos.TransadoCompra)).abs();
			pos.PorVender = (pos.OrdenadoVenta.subtract(pos.TransadoVenta)).abs();
		}
		
		return salida;
	}
	
	public static class TipoInstrumento_Proceso {
		public String IdTipoInstrumento;
		public String NombreTipoInstrumento;
	}
	
	
	public static class Orden_Proceso {
		public String IdTipoInstrumento;
		public Long IdInstrumento;
		public String IdTipoCanalIngreso;
		public Long IdInstrumentoSubyacente;
		public String IdTipoOperacion;
		public BigDecimal OrdenadoCompra;
		public BigDecimal OrdenadoVenta;
		public String EsVentaCorta;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Instrumento_Proceso {
		public String IdClasificacionTipoInstrumento;
		public String IdTipoInstrumento;
		public Long IdInstrumento;
		public String Nemotecnico; 
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Transaccion_Proceso {
		public String IdTipoInstrumento;
		public String NombreTipoInstrumento;
		public Long IdInstrumento;
		public Long IdInstrumentoSubyacente;
		public String IdTipoOperacion;
		public BigDecimal TransadoCompra;
		public BigDecimal TransadoVenta;
		public String EsVentaCorta;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Posicion_Salida {
		public String IdClasificacionTipoInstrumento;
		public String IdTipoInstrumento;
		public String NombreTipoInstrumento;
		public Long IdInstrumento;
		public String Nemotecnico;
		public String EsVentaCorta;
		public String EsVentaCortaUI;
		public String IdMoneda;
		public BigDecimal OrdenadoCompra;
		public BigDecimal OrdenadoVenta; 
		public BigDecimal TransadoCompra;
		public BigDecimal TransadoVenta; 
		public BigDecimal PorComprar;
		public BigDecimal PorVender;
		
		@Override
		public boolean equals(Object o) {
			Posicion_Salida e = (Posicion_Salida)o;
			return this.EsVentaCorta.equalsIgnoreCase(e.EsVentaCorta) && this.IdInstrumento.equals(e.IdInstrumento) && this.IdTipoInstrumento.equalsIgnoreCase(e.IdTipoInstrumento);
		}
		
		@Override
		public int hashCode() {
			return (this.EsVentaCorta + this.IdInstrumento + this.IdTipoInstrumento).hashCode();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ConsultarPosicion_Entrada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		
		public String p_FechaConsulta;
		public String p_IdClasificacionTipoInstrumento;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	//SALIDAS
	public static class ConsultarPosicion_Salida {

		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		public List<Posicion_Salida> P_OUT_POSICION;
		
		public ConsultarPosicion_Salida() {
			P_OUT_MENSAJES = new ArrayList<>();
			P_OUT_POSICION = new ArrayList<>();
		}
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	
	public static void main(String[]args) throws Exception{
		try (Connection con = PlataformaUtils.getMBPEConnection()){
		//try (Connection con = PlataformaUtils.getQAPEConnection()){
			
			ConsultarPosicionAsignacion pos = new ConsultarPosicionAsignacion();
			ConsultarPosicion_Entrada ent = new ConsultarPosicion_Entrada();
			ent.p_IdEmpresa = 1L;
			ent.p_IdUsuario = 21L;
			ent.p_IdModulo  = "ORD";
			ent.p_FechaConsulta = "2018-04-23";
			ent.p_IdClasificacionTipoInstrumento = "RV";
			
			ConsultarPosicion_Salida asdf = pos.logica(con, ent);
			System.out.println(asdf.toString());
		}	
	}
}
	