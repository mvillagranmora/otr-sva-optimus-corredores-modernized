package cl.bcs.ordenes;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.ConsultarCustodiaPortafolio.DatosCorredora;
import cl.bcs.custodia.ConsultarCustodiaPortafolio.Tabla_Salida;
import cl.bcs.custodia.evc.CrearEventoCapital.Instrumento;
import cl.bcs.ordenes.AnularOrdenMasiva.SalidaAnularOrdenMasiva;
import cl.bcs.ordenes.AprobarRechazarSolicitudAnulacionOrdenes.AnularOrden;
import cl.bcs.ordenes.ConsultarCustodiaIngresoOrden.ServiceEntrada;
import cl.bcs.ordenes.utils.OrdenUtils.AdvertenciaDetalle;
import cl.bcs.ordenes.utils.OrdenUtils.ErrorDetalle;
import cl.bcs.ordenes.utils.OrdenUtils.InformacionDetalle;
import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class InformeOrdenesConfirmadas implements JavaCall {
	private final Log log = new Log("PLA.InformeOrdenesConfirmadas");
	private final String SQL_CONSULTA_ORDEN_POR_CONFIRMAR = loadResourceToString("cl/bcs/ordenes/sql/ConsultarOrdenesPorConfirmar.sql");
	


	public static class InformeOrdenesConfirmadas_IN {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_EstadoConfirmacion;
		public String p_FechaConsulta;
		public String p_IdTipoInstrumentoSeleccionado;
		public String p_Moneda;
		public String p_CondicionDeLiquidacion;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class InformeOrdenesConfirmadas_OUT {
		public List<Tabla_Salida> P_OUT_TBL;
		public Long P_OUT_ERRORESCONTADOR = 0L;
		
		public InformeOrdenesConfirmadas_OUT(){
			this.P_OUT_TBL=new ArrayList<Tabla_Salida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Tabla_Salida{
		public String Mercado;
		public String DocumentoIdentidad;
		public String Cliente;
		public Long IdOrden;
		public String Producto;
		public String FechaIngreso;
		public String FechaConfirmacionPlazo;
		public Timestamp FechaConfirmacion;
		public String Operacion;
		public String Instrumento;
		public String IdMonedaOrden;
		public BigDecimal Cantidad;
		public BigDecimal Precio;
		public String EstadoOrden;
		public String EstadoConfirmacion;
		public String InstrumentoNegociacion;
		public String IdMonedaNegociacion;
		public String IdCondicionDeLiquidacion;



		public String toString() {
			return Utils.printJson(this, false);
		}
	
	}

	public InformeOrdenesConfirmadas_OUT logica(Connection conn, InformeOrdenesConfirmadas_IN in) throws Exception {
		SQL _sql = new SQL();
		Long Id = 0L;
		HashMap<String, String> mapMsg = new HashMap<>();
		log.write("INICIO::PLA.InformeOrdenesConfirmadas");
		InformeOrdenesConfirmadas_OUT out = new InformeOrdenesConfirmadas_OUT();
		HashMap<String, Object> params = new HashMap<>();
		List<Tabla_Salida> salida = new ArrayList<>();
		List<String> productosAFiltrarList = new ArrayList<>();
		String productosConConfirmacion = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "ORDENES", "ProductosConfirmacionOrden");
		String [] productosAFiltrar = productosConConfirmacion.split(";");

		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA","PLA.FechaSistema");
		//String fechaSistema  ="2019-06-07";
		params.clear();
		params.put("IdEmpresa",in.p_IdEmpresa);
		//params.put("FechaSistema", fechaSistema);
		params.put("FechaConsulta",in.p_FechaConsulta);
		params.put("EstadoConfirmacion","".equals(in.p_EstadoConfirmacion)?null:in.p_EstadoConfirmacion);
		params.put("IdTipoInstrumento","".equals(in.p_IdTipoInstrumentoSeleccionado)?null:in.p_IdTipoInstrumentoSeleccionado);
		params.put("CondicionDeLiquidacion","".equals(in.p_CondicionDeLiquidacion)?null:in.p_CondicionDeLiquidacion);
		params.put("IdMoneda","".equals(in.p_Moneda)?null:in.p_Moneda );
		
		for(String prod : productosAFiltrar){
			productosAFiltrarList.add(prod);
		}
		params.put("Productos",productosAFiltrarList);
		
		
		
		//Consulta Ordenes Confirmadas
		salida = _sql.query(conn,SQL_CONSULTA_ORDEN_POR_CONFIRMAR,params,Tabla_Salida.class);
	
		out.P_OUT_TBL.addAll(salida);
		log.write("FIN::PLA.InformeOrdenesConfirmadas");
		return out;

	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		Gson gson = Utils.getGsonBuilder();
		InformeOrdenesConfirmadas_IN jin = gson.fromJson(in.toString(), InformeOrdenesConfirmadas_IN.class);
		InformeOrdenesConfirmadas_OUT out = logica(conn, jin);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};

	public static void main(String[] args) throws Exception, SQLException {
		// TODO Auto-generated method stub
		Connection conn = null;
		Gson gson = new Gson();

		try {
			System.out.println("== BEGIN ==");
			conn = PlataformaUtils.getDESAConnection();
			// datos entrada

			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_FechaConsulta\":\"2019-06-12\",\"p_EstadoConfirmacion\":\"\",\"p_IdTipoInstrumentoSeleccionado\":\"\"}";
			InformeOrdenesConfirmadas_IN objIn = gson.fromJson(json, InformeOrdenesConfirmadas_IN.class);
			// mientras pruebo hare el rollback
			InformeOrdenesConfirmadas demo = new InformeOrdenesConfirmadas();

			InformeOrdenesConfirmadas_OUT ObjOut = demo.logica(conn, objIn);

			System.out.println(ObjOut.toString());

			conn.rollback();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			conn.close();
		}
		System.out.println("== END ==");

	};
}
