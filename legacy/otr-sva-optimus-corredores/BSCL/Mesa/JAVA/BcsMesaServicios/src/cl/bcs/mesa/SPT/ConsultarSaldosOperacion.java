package cl.bcs.mesa.SPT;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.cuentaInversion.ConsultarSaldoCuentaInversion;
import cl.bcs.cuentaInversion.ConsultarSaldoCuentaInversion.EntradaConsultarSaldoCuentaInversion;
import cl.bcs.cuentaInversion.ConsultarSaldoCuentaInversion.SalidaConsultarSaldoCuentaInversion;
import cl.bcs.cuentaInversion.SaldoCuentaInversion.InCuentaInversionConsultar;
import cl.bcs.cuentaInversion.SaldoCuentaInversion.SaldoPorFechaEfectiva;
import cl.bcs.plataforma.pla.ConsultarFlujoCaja;
import cl.bcs.plataforma.pla.ConsultarFlujoCaja.EntradaConsultarFlujoCaja;
import cl.bcs.plataforma.pla.ConsultarFlujoCaja.PortafolioAConsultar;
import cl.bcs.plataforma.pla.ConsultarFlujoCaja.SalidaConsultarFlujoCaja;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarSaldosOperacion implements JavaCall {
	
	private final String SQL_SELECT_SALDOS = loadResourceToString("cl/bcs/mesa/SPT/sql/ConsultarSaldosOperacion.sql");

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = getGsonBuilder();
		ConsultarSaldosOperacionEntrada in = gson.fromJson(jin.toString(), ConsultarSaldosOperacionEntrada.class);
		ConsultarSaldosOperacionSalida out = logica(conn, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public static class ConsultarSaldosOperacionEntrada{
		public Long p_IdEmpresa; 
		public Long p_IdCliente;
		public Long p_IdPortafolio;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_FechaDesde;
		public String p_FechaHasta;
		public String p_Uso;
		public String p_DesplegarEnUI;
		public String p_AgruparPorOrigen;
		public Long p_IdUsuario;
		public String p_check;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ConsultarSaldosOperacionSalida{
		public List<SaldosSalida> P_OUT_SALDOS;
		public List<SalidaConsultarFlujoCaja> P_OUT_FLUJOCAJA;
		
		public ConsultarSaldosOperacionSalida() {
			this.P_OUT_SALDOS = new ArrayList<SaldosSalida>();
			this.P_OUT_FLUJOCAJA = new ArrayList<SalidaConsultarFlujoCaja>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Saldos{
		
		public String IdTipoOperacion;
		public String IdMonedaPrincipal;
		public BigDecimal MontoMonedaPrincipal;
		public String IdMonedaSecundaria;
		public BigDecimal MontoMonedaSecundaria;
		public String IdTipoCanalIngreso;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class saldosEntrada{
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SaldosSalida{
		
		public String IdTipoOperacion;
		public String IdMonedaPrincipal;
		public BigDecimal MontoMonedaPrincipal;
		public String IdMonedaSecundaria;
		public BigDecimal MontoMonedaSecundaria;
		public String IdTipoCanalIngreso;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public ConsultarSaldosOperacionSalida logica(Connection conn, ConsultarSaldosOperacionEntrada in) throws Exception {
		
		ConsultarSaldosOperacionSalida out = new ConsultarSaldosOperacionSalida();
		EntradaConsultarFlujoCaja entrada = new EntradaConsultarFlujoCaja();
		ConsultarFlujoCaja flujoCaja = new ConsultarFlujoCaja();
		SQL sql = new SQL();
		
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("IdEmpresa", in.p_IdEmpresa);
		params.put("IdCliente", in.p_IdCliente);
		params.put("IdPortafolio", in.p_IdPortafolio);
		
		List<Saldos> saldosList = sql.query(conn, SQL_SELECT_SALDOS, params, Saldos.class);
		
		entrada.p_IdEmpresa = in.p_IdEmpresa;
		entrada.p_IdUsuario = in.p_IdUsuario;
		entrada.p_IdModulo = in.p_IdModulo;
		entrada.p_FechaDesde = in.p_FechaDesde;
		entrada.p_FechaHasta = in.p_FechaHasta;
		entrada.p_Uso = in.p_Uso;
		entrada.p_AgruparPorOrigen = "N";
		entrada.p_DesplegarEnUI = "S";
		entrada.p_UsaReglasNegocio = "N";
		
		
		ConsultarSaldoCuentaInversion saldoCuentaInversion = new ConsultarSaldoCuentaInversion();
		EntradaConsultarSaldoCuentaInversion entradaSaldoCuentaInversion = new EntradaConsultarSaldoCuentaInversion();
		SalidaConsultarSaldoCuentaInversion salidaCuentaInversion = new SalidaConsultarSaldoCuentaInversion();
		InCuentaInversionConsultar cuentaAConsultar = new InCuentaInversionConsultar();
		entradaSaldoCuentaInversion.p_IdEmpresa = in.p_IdEmpresa;
		entradaSaldoCuentaInversion.p_IdUsuario = in.p_IdUsuario;
		entradaSaldoCuentaInversion.p_IdModulo = "CTA";
		entradaSaldoCuentaInversion.p_UsaReglasNegocio = "S";
		entradaSaldoCuentaInversion.p_IncluyeOtrosSaldos = "S";
		entradaSaldoCuentaInversion.p_FechaDesde = in.p_FechaDesde;
		entradaSaldoCuentaInversion.p_FechaHasta = in.p_FechaHasta;
		entradaSaldoCuentaInversion.p_IdCliente = in.p_IdCliente;
		entradaSaldoCuentaInversion.p_IdPortafolio = in.p_IdPortafolio;
		

		cuentaAConsultar.IdEjecutivo = null;
		cuentaAConsultar.IdMoneda = null;
		cuentaAConsultar.IdPortafolio = in.p_IdPortafolio;
		entradaSaldoCuentaInversion.p_CuentaInversionAConsultar.add(cuentaAConsultar);
		salidaCuentaInversion = saldoCuentaInversion.logica(conn, entradaSaldoCuentaInversion);
		
		
		
		for (SaldoPorFechaEfectiva cuentaInversion : salidaCuentaInversion.P_OUT_SALDOPORFECHAEFECTIVA) {
			PortafolioAConsultar portafolioAConsultar = new PortafolioAConsultar();
			portafolioAConsultar.IdPortafolio = in.p_IdPortafolio;
			portafolioAConsultar.IdMoneda = cuentaInversion.IdMoneda;
			entrada.p_PortafolioAConsultar.add(portafolioAConsultar);
			
			SaldosSalida saldosSalida = new SaldosSalida();
			saldosSalida.IdTipoOperacion = "";
			saldosSalida.IdMonedaPrincipal= cuentaInversion.IdMoneda;
			saldosSalida.MontoMonedaPrincipal= BigDecimal.ZERO;
			saldosSalida.IdMonedaSecundaria= cuentaInversion.IdMoneda;
			saldosSalida.MontoMonedaSecundaria= BigDecimal.ZERO;
			saldosSalida.IdTipoCanalIngreso = "";
			
			out.P_OUT_SALDOS.add(saldosSalida);
		}
		
		for (Saldos saldos : saldosList) {
			SaldosSalida saldosSalida = new SaldosSalida();
			saldosSalida.IdTipoOperacion = saldos.IdTipoOperacion;
			saldosSalida.IdMonedaPrincipal= saldos.IdMonedaPrincipal;
			saldosSalida.MontoMonedaPrincipal= saldos.MontoMonedaPrincipal;
			saldosSalida.IdMonedaSecundaria= saldos.IdMonedaSecundaria;
			saldosSalida.MontoMonedaSecundaria= saldos.MontoMonedaSecundaria;
			saldosSalida.IdTipoCanalIngreso = saldos.IdTipoCanalIngreso;
			
			PortafolioAConsultar portafolioAConsultar = new PortafolioAConsultar();
			portafolioAConsultar = new PortafolioAConsultar();
			portafolioAConsultar.IdPortafolio = in.p_IdPortafolio;
			portafolioAConsultar.IdMoneda = saldos.IdMonedaPrincipal;
			entrada.p_PortafolioAConsultar.add(portafolioAConsultar);
			
			out.P_OUT_SALDOS.add(saldosSalida);
		}
		
		for (Saldos saldos : saldosList) {
			
			PortafolioAConsultar portafolioAConsultar = new PortafolioAConsultar();
			
			portafolioAConsultar.IdPortafolio = in.p_IdPortafolio;
			portafolioAConsultar.IdMoneda = saldos.IdMonedaSecundaria;
			entrada.p_PortafolioAConsultar.add(portafolioAConsultar);
		}
		
		SalidaConsultarFlujoCaja salidaFlujoCaja = flujoCaja.logica(conn, entrada);
		System.out.println(salidaFlujoCaja);
		out.P_OUT_FLUJOCAJA.add(salidaFlujoCaja);
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\"p_IdEmpresa\":1,\"p_IdCliente\":249193,\"p_IdUsuario\":2869,\"p_IdPortafolio\":153963,\"p_IdModulo\":\"NOR\",\"p_UsaReglasNegocio\":\"N\",\"p_FechaHasta\":\"2023-06-26\",\"p_FechaDesde\":\"2023-06-26\",\"p_Uso\":\"INGRESO_OPERACION_SPOT\",\"p_DesplegarEnUI\":\"S\",\"p_AgruparPorOrigen\":\"N\",\"p_check\":\"false\"}";
			ConsultarSaldosOperacionEntrada Objin = gson.fromJson(json, ConsultarSaldosOperacionEntrada.class);
			
			ConsultarSaldosOperacion demo = new ConsultarSaldosOperacion();				
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			try {
 				ConsultarSaldosOperacionSalida ObjOut = demo.logica(con, Objin);
 				//con.commit();
 				System.out.println("saldos:"+ObjOut.toString());
 			}
 			catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}
			finally {
				con.close();
			}
	    }	
		System.out.println("== END ==");
	}
}
