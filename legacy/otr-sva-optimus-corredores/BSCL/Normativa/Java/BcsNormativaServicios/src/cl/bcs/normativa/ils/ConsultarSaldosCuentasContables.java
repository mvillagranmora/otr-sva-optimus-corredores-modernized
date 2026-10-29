package cl.bcs.normativa.ils;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.normativa.ils.pojo.CuentaContable;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarSaldosCuentasContables implements JavaCall{
	private final String SQL_SELECT_SALDO_CUENTA_CONTABLE = loadResourceToString("cl/bcs/normativa/ils/sql/ConsultarSaldosCuentasContables_SelectSaldosCuentasContables.sql");
	
	//-------------------------------------------------
	// CLASES DE ENTRADA 
	//-------------------------------------------------
	public static class ConsultarSaldosCuentasContables_Entrada {
		public Long 			p_IdEmpresa;
	    public Long				p_IdUsuario;
	    public String			p_IdModulo;
	    public String			p_PresentacionMontos; // en M$ o en $
	    public List<CuentaContable>	p_SaldosCuentas;// a futuro si quiero consultar por una cuenta  en especifico
		
	    public ConsultarSaldosCuentasContables_Entrada() {
	    	this.p_PresentacionMontos = "M$";
			this.p_SaldosCuentas = new ArrayList<CuentaContable>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	} 
	 /*
	public static class SaldoCuenta {
		//public Long				Id;
		//public Long				IdPadre;
		public String			IdCodigoContabilidad;
		public String			IdCodigoContabilidadPadre;
		public BigDecimal		MontoDebe;
		public BigDecimal		MontoHaber;
		public BigDecimal		Saldo;
		public Integer			Nivel;
		public String			Imputable;
		public String			NombreCuenta;
		public String			TipoCuenta;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}*/
	//-------------------------------------------------
	// CLASES DE SALIDA 
	//-------------------------------------------------
	public static class ConsultarSaldosCuentasContables_Salida {	
		public List<MensajeSalida>			P_OUT_MENSAJES;
		public Long							P_OUT_ERRORESCONTADOR;
		public List<CuentaContable>			P_OUT_SALDOSCUENTASCONTABLES;
		
		public ConsultarSaldosCuentasContables_Salida() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES	 =  new ArrayList<MensajeSalida>();
			this.P_OUT_SALDOSCUENTASCONTABLES = new ArrayList<CuentaContable>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	//-------------------------------------------------
	// CLASES UTILITARIAS 
	//-------------------------------------------------
	
	
	//-------------------------------------------------
	// LOGICA DEL SERVICIO 
	//-------------------------------------------------	
	public ConsultarSaldosCuentasContables_Salida logica(Connection conn, ConsultarSaldosCuentasContables_Entrada entrada) throws Exception {
		SQL _sql = new SQL();
		Long Id = 0L;
		Long IdPadre = 0L;
		ConsultarSaldosCuentasContables_Salida salida = new ConsultarSaldosCuentasContables_Salida();
		HashMap<String, Object> params = new HashMap<>();
		List<CuentaContable> saldosCuentas = new ArrayList<>();
		
		HashMap<String, List<CuentaContable>> saldosCuentasPasoMap = new HashMap<String, List<CuentaContable>>();
		
		saldosCuentas = _sql.query(conn, SQL_SELECT_SALDO_CUENTA_CONTABLE, params, CuentaContable.class);
		/*
	Cuentas de Activo (*)
	Cuentas de Pasivo (-)	 
1 ACTivo *
2 pasivo -
3 patrimonio -
4 Ingresos operacionales -
5 costos OPERACIONALES *
6 gastos OPERACIONALES *
7 otros ingresos  -
8 otros egresos * 

Para el calculo de saldo
///* (debe - haber)
///- (haber - debe)
*/		BigDecimal divisor = new BigDecimal(1000);
		MathContext precision = new MathContext(4);
		for(CuentaContable sal: saldosCuentas) {
			
			CuentaContable s = new CuentaContable();
			s.IdCodigoContabilidad 		= sal.IdCodigoContabilidad;
			s.IdCodigoContabilidadPadre = sal.IdCodigoContabilidadPadre;
			s.MontoDebe					= sal.MontoDebe;
			s.MontoHaber 				= sal.MontoHaber;
			s.Nivel 					= sal.Nivel;
			s.Imputable 				= sal.Imputable;
			s.NombreCuenta				= sal.NombreCuenta;
			switch (sal.TipoCuenta) {
				case "A":
					s.Saldo				= sal.MontoDebe.subtract(sal.MontoHaber);
					break;
				case "P":
					s.Saldo				= sal.MontoHaber.subtract(sal.MontoDebe);
					break;
			}
			
			s.MontoDebe = entrada.p_PresentacionMontos.equals("M$")?s.MontoDebe.divide(divisor):s.MontoDebe;			
			s.MontoHaber = entrada.p_PresentacionMontos.equals("M$")?s.MontoHaber.divide(divisor):s.MontoHaber;			
			s.Saldo = entrada.p_PresentacionMontos.equals("M$")?s.Saldo.divide(divisor):s.Saldo;
			/*if ("".equals(sal.IdCodigoContabilidadPadre)) {
				s.Id = ++Id;
				s.IdPadre = 0L;//null;
				salida.P_OUT_SALDOSCUENTASCONTABLES.add(s);
				continue;
			}
			List<SaldoCuenta> saldosCuentasPaso = (List<SaldoCuenta>) saldosCuentasPasoMap.get(sal.IdCodigoContabilidadPadre);
			if (saldosCuentasPaso != null) {
				s.Id = ++Id;
				s.IdPadre = saldosCuentasPaso.get(0).IdPadre;
				saldosCuentasPaso.add(s);
				System.out.println(sal.IdCodigoContabilidadPadre);
			}
			else {
				List<SaldoCuenta> saldo = new ArrayList<>();
				s.Id = ++Id;
				s.IdPadre = ++IdPadre;
				saldo.add(s);
				saldosCuentasPasoMap.put(sal.IdCodigoContabilidadPadre, saldo);
			}*/
			salida.P_OUT_SALDOSCUENTASCONTABLES.add(s);
		}
		/*
		for (Map.Entry<String, List<SaldoCuenta>> l : saldosCuentasPasoMap.entrySet()) {
			salida.P_OUT_SALDOSCUENTASCONTABLES.addAll(l.getValue());
		}	*/	
		
		return salida;
	}
	
	
	//-------------------------------------------------------------------------------------------
	// OBJETO DE TRANSPORTE - CALL
	//-------------------------------------------------------------------------------------------
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();
		ConsultarSaldosCuentasContables_Entrada in = gson.fromJson(jin.toString(), ConsultarSaldosCuentasContables_Entrada.class);
		ConsultarSaldosCuentasContables_Salida out = logica(con,in);
		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	
	//-------------------------------------------------
	// METODO PARA PRUEBAS LOCALES 
	//-------------------------------------------------
 
	public static void main(String[] args) throws Exception {
		System.out.println("== INICIO PRUEBA LOCAL ==");
		
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"ILS\",\"p_UsaReglasNegocio\":\"S\",\"p_IdTipoInforme\":42,\"p_FechaContable\":\"2019-07-30\",\"p_FechaEmision\":\"\"}";
			
			ConsultarSaldosCuentasContables_Entrada Objin = gson.fromJson(json, ConsultarSaldosCuentasContables_Entrada.class);
			ConsultarSaldosCuentasContables demo = new ConsultarSaldosCuentasContables();
 			
			System.out.println("ENTRADA:" + Objin.toString());
			
 			try {
 				ConsultarSaldosCuentasContables_Salida ObjOut = demo.logica(con, Objin); 				
 				System.out.println("SALIDA:" + ObjOut);
 				con.rollback();
 				
 			} catch(Exception er) {
				er.printStackTrace();
				con.rollback();
			}

	    }

		System.out.println("== FIN PRUEBA LOCAL ==");
	}
}
