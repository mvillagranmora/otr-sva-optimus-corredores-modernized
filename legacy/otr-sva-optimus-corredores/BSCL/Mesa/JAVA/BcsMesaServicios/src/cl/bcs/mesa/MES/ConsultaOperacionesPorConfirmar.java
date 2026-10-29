package cl.bcs.mesa.MES;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultaOperacionesPorConfirmar implements JavaCall{
	private final String SQL_SELECT_OPERACIONES_FORWARD = loadResourceToString("cl/bcs/mesa/MES/sql/ConsultaOperacionesPorConfirmar_SelectOperacionesForward.sql");
	private final String SQL_SELECT_OPERACIONES_RF = loadResourceToString("cl/bcs/mesa/MES/sql/ConsultaOperacionesPorConfirmar_SelectOperacionesRF.sql");
	
	public class ConsultaOperacionesPorConfirmarEntrada {
		public Long			p_IdEmpresa;
		public Long			p_IdUsuario;
		public String		p_IdModulo;
		public String 		p_IdTipoProducto;
		public String 		p_IdTipoOperacion;
		public String 		p_IdMonedaPrincipal;
		public String 		p_IdMonedaSecundaria;
		public String 		p_EstadoConfirmacion;
		public Long 		p_IdCliente;
		public String 		p_Fecha;
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	};
	
	public class ConsultaOperacionesPorConfirmarSalida{
		public List<MensajeSalida>			P_OUT_MENSAJES;
		public Long							P_OUT_ERRORESCONTADOR;
		public List<Operaciones>			P_OUT_OPERACIONES;
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	};
	
	public static class Operaciones{
		public Long IdOperacion;
		public Long IdCartera;
		public Long FolioCartera;
		public String IdTipoInstrumento;
		public Long FolioOperacion; 
		public String EstadoConfirmacion;
		public String TipoOperacion;
		public String NombreInstrumento;
		public Long IdCliente;
		public String RutCliente;
		public String NombreCliente;
		public String Portafolio;
		public String MonedaPrincipal;
		public String MonedaSecundaria;
		public BigDecimal MontoMonedaPrincipal;
		public BigDecimal TCCierre;
		public BigDecimal TCCosto;
		public BigDecimal ParidadCierre;
		public BigDecimal MontoMonedaSecundaria;
		public Date FechaEntregamos;
		public Date FechaRecibimos;
		public String EstadoOperacion;
		public String Canal;
		public Date FechaOperacion;
		public Date FechaLiquidacion;
		public String IdTipoReajuste;
		public BigDecimal Tasa;
		public Long PlazoEnDias;
		public BigDecimal MontoOperacion;
		public BigDecimal MontoOperacionMoneda;
		public Date FechaVencimiento;
		public Long IdPortafolioCliente;
		public Long IdPortafolioCorredor;
		public Long IdClienteCorredor;
		public Long IdMesa;
		
		public String toString() {
			return Utils.printJson(this, false);
		};
		
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		Gson gson = new Gson();		
		ConsultaOperacionesPorConfirmarEntrada jin=gson.fromJson(in.toString() ,ConsultaOperacionesPorConfirmarEntrada.class);
		ConsultaOperacionesPorConfirmarSalida out=logica(conn,jin);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public ConsultaOperacionesPorConfirmarSalida logica(Connection conn, ConsultaOperacionesPorConfirmarEntrada in) throws Exception {
		SQL sql = new SQL();
		ConsultaOperacionesPorConfirmarSalida out = new ConsultaOperacionesPorConfirmarSalida();
		HashMap<String, Object> params = new HashMap<String, Object>(); 
		params.put("IdEmpresa", in.p_IdEmpresa);
		params.put("IdTipoProducto", in.p_IdTipoProducto);
		params.put("IdTipoOperacion", in.p_IdTipoOperacion);
		params.put("IdMonedaPrincipal", in.p_IdMonedaPrincipal);
		params.put("IdMonedaSecundaria", in.p_IdMonedaSecundaria);
		params.put("EstadoConfirmacion", in.p_EstadoConfirmacion);
		params.put("IdCliente", in.p_IdCliente);
		params.put("FechaOperacion", in.p_Fecha);
		
		if(in.p_IdTipoProducto.equals("MEFW")) {
			out.P_OUT_OPERACIONES = sql.query(conn, SQL_SELECT_OPERACIONES_FORWARD, params, Operaciones.class);
		}
		if(in.p_IdTipoProducto.equals("MERF")) {
			out.P_OUT_OPERACIONES = sql.query(conn, SQL_SELECT_OPERACIONES_RF, params, Operaciones.class);
		}
		return out;
	}
		
	public static void main(String[] args) {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":6,\"p_IdModulo\":\"MES\",\"p_IdTipoProducto\":\"MEFW\",\"p_IdTipoOperacion\":0,\"p_IdMonedaPrincipal\":0,\"p_IdMonedaSecundaria\":0,\"p_EstadoConfirmacion\":0,\"p_IdCliente\":0,\"p_Fecha\":\"2019-12-19\"}";
			ConsultaOperacionesPorConfirmarEntrada Objin = gson.fromJson(json, ConsultaOperacionesPorConfirmarEntrada.class);
			ConsultaOperacionesPorConfirmar demo = new ConsultaOperacionesPorConfirmar();
			System.out.println("ENTRADA");
			System.out.println(Objin.toString());
			try {
				ConsultaOperacionesPorConfirmarSalida ObjOut = demo.logica(con, Objin);
				con.rollback();
				System.out.println(ObjOut.toString());
				System.out.println("SALIDA");
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
			con.rollback();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}
}
