package cl.bcs.tesoreria;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Entrada;
import cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Salida;
import cl.bcs.tesoreria.CrearIngresoEgreso.FormaDePago_Entrada;
import cl.bcs.tesoreria.CrearIngresoEgreso.IngresoEgresoGenerado_Salida;
import cl.bcs.tesoreria.CrearIngresoEgreso.IngresoEgreso_Entrada;
import cl.bcs.tesoreria.CrearIngresoEgreso.ResultadoProceso_Salida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearTraspaso implements JavaCall{
	
	// Log
	private final Log log = new Log("TES.CrearTraspaso");
	
	// Queries
	private final String SQL_UPDATE_INGRESO = loadResourceToString("cl/bcs/tesoreria/sql/CrearTraspaso_UpdateIngreso.sql");
	
	public static class CrearTraspaso_Entrada{
		public Long							p_IdEmpresa;
		public Long							p_IdUsuario;
		public String						p_IdModulo;
		public String						p_SimularOperacion = "N";
		public String						p_UsaReglasNegocio;		
		public String                       p_EsIngresoAutomatico = "S";        //S: Ingreso automático   N: Ingreso Manual
		public String						p_FechaMovimiento = "0001-01-01";   // Fecha de Sistema(si no viene le asigna fecha sistema)  //////// nuevo jv
		
		public List<IngresoEgreso_Entrada>	p_IngresoEgresoGenerar;
		public List<FormaDePago_Entrada>	p_FormaDePago;
		
	}
	
	public static class CrearTraspasos_Salida {

		public List<MensajeSalida>					P_OUT_MENSAJES;
		public Long									P_OUT_ERRORESCONTADOR;
		public List<IngresoEgresoGenerado_Salida>	P_OUT_INGRESOEGRESOGENERADO;
		public List<ResultadoProceso_Salida>        P_OUT_RESULTADOPROCESO;
		
		public CrearTraspasos_Salida()
		{	
			this.P_OUT_MENSAJES 			 = new ArrayList<MensajeSalida>();
			this.P_OUT_INGRESOEGRESOGENERADO = new ArrayList<IngresoEgresoGenerado_Salida>();
			this.P_OUT_RESULTADOPROCESO		 = new ArrayList<ResultadoProceso_Salida>();
			this.P_OUT_ERRORESCONTADOR = 0L;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class UpdateIngreso {
		public Long IdIngresoEgreso;
		public Long SecuenciaRelacionada;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		CrearTraspaso_Entrada jin = gson.fromJson(in.toString(), CrearTraspaso_Entrada.class);
		CrearTraspasos_Salida out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public CrearTraspasos_Salida logica(Connection con, CrearTraspaso_Entrada in) throws Exception {
		log.write("INICIO TRASPASO::");
		SQL _sql = new SQL();
		CrearTraspasos_Salida out = new CrearTraspasos_Salida();
		List<IngresoEgreso_Entrada> ingresos = new ArrayList<IngresoEgreso_Entrada>();
		List<FormaDePago_Entrada> fdpIngresos = new ArrayList<FormaDePago_Entrada>();
		List<IngresoEgreso_Entrada> egresos = new ArrayList<IngresoEgreso_Entrada>();
		List<FormaDePago_Entrada> fdpEgresos = new ArrayList<FormaDePago_Entrada>();
		log.write("VERIFICANDO EGRESOS ASOCIADOS");
		for(IngresoEgreso_Entrada ingreso : in.p_IngresoEgresoGenerar){
			if("MOVING".equalsIgnoreCase(ingreso.IdTipoMovimiento)){
				boolean existeEgreso = false;
				for(IngresoEgreso_Entrada egreso : in.p_IngresoEgresoGenerar){
					if("MOVEGR".equalsIgnoreCase(egreso.IdTipoMovimiento) && Math.abs(egreso.NumeroReferencia.longValue()) == Math.abs(ingreso.NumeroReferencia.longValue())){
						existeEgreso = true;
						break;
					}
				}
				if(existeEgreso){
					ingresos.add(ingreso);
					if (!in.p_FormaDePago.isEmpty()) {
						for(FormaDePago_Entrada fdpIngreso : in.p_FormaDePago){
							if(ingreso.NumeroReferencia.longValue() == fdpIngreso.NumeroReferencia.longValue()){
								fdpIngresos.add(fdpIngreso);
							}
					    }					
					}
					else
					{
						out.P_OUT_ERRORESCONTADOR += 1L;
						out.P_OUT_MENSAJES.add(new MensajeSalida(ingreso.NumeroReferencia,MensajeSalida.MENSAJE_ERROR,"REQUIERE_FORMA_PAGO_TRASPASO",new HashMap<String, String>(),con,in.p_IdEmpresa));
					}
				}else{
					out.P_OUT_ERRORESCONTADOR += 1L;
					out.P_OUT_MENSAJES.add(new MensajeSalida(ingreso.NumeroReferencia,MensajeSalida.MENSAJE_ERROR,"NO_EXISTE_EGRESO_ASOCIADO",new HashMap<String, String>(),con,in.p_IdEmpresa));
				}
			}
		}
		
		if(out.P_OUT_ERRORESCONTADOR.longValue() > 0) return out;
		
		log.write("VERIFICANDO INGRESOS ASOCIADOS");
		for(IngresoEgreso_Entrada egreso : in.p_IngresoEgresoGenerar){
			if("MOVEGR".equalsIgnoreCase(egreso.IdTipoMovimiento)){
				boolean existeIngreso = false;
				for(IngresoEgreso_Entrada ingreso : in.p_IngresoEgresoGenerar){
					if("MOVING".equalsIgnoreCase(ingreso.IdTipoMovimiento) && Math.abs(egreso.NumeroReferencia.longValue()) == Math.abs(ingreso.NumeroReferencia.longValue())){
						existeIngreso = true;
						break;
					}
				}
				if(existeIngreso){
					egresos.add(egreso);
					if (!in.p_FormaDePago.isEmpty()) {
						for(FormaDePago_Entrada fdpEgreso : in.p_FormaDePago){
							if(egreso.NumeroReferencia.longValue() == fdpEgreso.NumeroReferencia.longValue()){
								fdpEgresos.add(fdpEgreso);
							}
						}
					}					
				}else{
					out.P_OUT_ERRORESCONTADOR += 1L;
					out.P_OUT_MENSAJES.add(new MensajeSalida(egreso.NumeroReferencia,MensajeSalida.MENSAJE_ERROR,"NO_EXISTE_INGRESO_ASOCIADO",new HashMap<String, String>(),con,in.p_IdEmpresa));
				}
			}
		}
		
		//HACEMOS LOS INGRESOS
		log.write("CREAR INGRESOS");
		CrearIngresoEgreso crearIngreso = new CrearIngresoEgreso();
		CrearIngresoEgreso_Entrada inIngreso = new CrearIngresoEgreso_Entrada();
		inIngreso.p_IdEmpresa = in.p_IdEmpresa;
		inIngreso.p_IdUsuario = in.p_IdUsuario;
		inIngreso.p_IdModulo = in.p_IdModulo;
		inIngreso.p_SimularOperacion = in.p_SimularOperacion;
		inIngreso.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
		inIngreso.p_EsIngresoAutomatico = in.p_EsIngresoAutomatico;
		inIngreso.p_FechaMovimiento = in.p_FechaMovimiento;
		inIngreso.p_IngresoEgresoGenerar = ingresos;
		inIngreso.p_FormaDePago = fdpIngresos;
		
		CrearIngresoEgreso_Salida salidaIngresos = crearIngreso.logica(con, inIngreso);
		out.P_OUT_ERRORESCONTADOR = salidaIngresos.P_OUT_ERRORESCONTADOR == null ? 0L : salidaIngresos.P_OUT_ERRORESCONTADOR;
		out.P_OUT_MENSAJES.addAll(salidaIngresos.P_OUT_MENSAJES);
		out.P_OUT_INGRESOEGRESOGENERADO.addAll(salidaIngresos.P_OUT_INGRESOEGRESOGENERADO);
		out.P_OUT_RESULTADOPROCESO.addAll(salidaIngresos.P_OUT_RESULTADOPROCESO);
		
		if(out.P_OUT_ERRORESCONTADOR.longValue() > 0) return out;
		
		//Asociamos el egreso con el ingreso
		for(IngresoEgreso_Entrada egreso : egresos){
			for(IngresoEgresoGenerado_Salida salida : salidaIngresos.P_OUT_INGRESOEGRESOGENERADO){
				if(Math.abs(salida.NumeroReferencia.longValue()) == Math.abs(egreso.NumeroReferencia.longValue())){
					egreso.SecuenciaRelacionada = salida.IdIngresoEgreso;
					break;
				}
			}
		}
		
		//HACEMOS LOS EGRESOS
		log.write("CREAR EGRESOS");
		CrearIngresoEgreso crearEgreso = new CrearIngresoEgreso();
		CrearIngresoEgreso_Entrada inEgreso = new CrearIngresoEgreso_Entrada();
		inEgreso.p_IdEmpresa = in.p_IdEmpresa;
		inEgreso.p_IdUsuario = in.p_IdUsuario;
		inEgreso.p_IdModulo = in.p_IdModulo;
		inEgreso.p_SimularOperacion = in.p_SimularOperacion;
		inEgreso.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
		inEgreso.p_EsIngresoAutomatico = in.p_EsIngresoAutomatico;
		inEgreso.p_FechaMovimiento = in.p_FechaMovimiento;
		inEgreso.p_IngresoEgresoGenerar = egresos;
		inEgreso.p_FormaDePago = fdpEgresos;
		
		CrearIngresoEgreso_Salida salidaEngresos = crearEgreso.logica(con, inEgreso);
		out.P_OUT_ERRORESCONTADOR = salidaEngresos.P_OUT_ERRORESCONTADOR == null ? 0L : salidaEngresos.P_OUT_ERRORESCONTADOR;
		out.P_OUT_MENSAJES.addAll(salidaEngresos.P_OUT_MENSAJES);
		out.P_OUT_INGRESOEGRESOGENERADO.addAll(salidaEngresos.P_OUT_INGRESOEGRESOGENERADO);
		out.P_OUT_RESULTADOPROCESO.addAll(salidaEngresos.P_OUT_RESULTADOPROCESO);
		
		if(out.P_OUT_ERRORESCONTADOR.longValue() > 0) return out;
		
		List<UpdateIngreso> update = new ArrayList<UpdateIngreso>();
		//Relacionamos el ingreso con el egreso
		for(IngresoEgresoGenerado_Salida salida : salidaIngresos.P_OUT_INGRESOEGRESOGENERADO){
			for(IngresoEgresoGenerado_Salida salidaE : salidaEngresos.P_OUT_INGRESOEGRESOGENERADO){
				if(Math.abs(salidaE.NumeroReferencia.longValue()) == Math.abs(salida.NumeroReferencia.longValue())){
					UpdateIngreso ingreso = new UpdateIngreso();
					ingreso.SecuenciaRelacionada = salidaE.IdIngresoEgreso;
					ingreso.IdIngresoEgreso = salida.IdIngresoEgreso;
					update.add(ingreso);
				}
			}
		}
		
		log.write("UPDATE INGRESOS");
		_sql.batch(con, SQL_UPDATE_INGRESO, update, UpdateIngreso.class);
		
		out.P_OUT_ERRORESCONTADOR = 0L;
		for(MensajeSalida mje : out.P_OUT_MENSAJES){
			if(MensajeSalida.MENSAJE_ERROR.equalsIgnoreCase(mje.tipo)) out.P_OUT_ERRORESCONTADOR += 1L;
		}
		
		return out;
	}
	
	
	public static void main(String[] args) throws Exception 
	{
		System.out.println("== INICIO PRUEBA LOCAL ==");

		try (Connection con = PlataformaUtils.getSURAConnection()) 
		{
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();

			String test = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":150,\"p_IdModulo\":\"TES\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_FormaDePago\":[null],\"p_IngresoEgresoGenerar\":[{\"IdTipoMovimiento\":\"MOVING\",\"IdTipoPersona\":\"CLI\",\"IdPersona\":86186,\"IdPortafolio\":360032,\"IdMoneda\":\"CLP\",\"Monto\":100,\"FechaLiquidacion\":\"2019-10-01\",\"IdConcepto\":\"APORCI\",\"IdTipoPersonaTitularPago\":\"CLI\",\"IdPersonaTitularPago\":86186,\"IdPortafolioTitularPago\":360032,\"IdCaja\":0,\"SecuenciaAgrupacion\":\"\",\"SecuenciaRelacionada\":null,\"Glosa\":\"\",\"IdEmpleadoDigitador\":65,\"IdSucursalEmpleadoDigitador\":0,\"IdTipoComprobante\":\"\",\"IdComprobante\":0,\"FechaComprobante\":\"0001-01-01\",\"FechaLiquidacionComprobante\":\"0001-01-01\",\"FolioCartera\":0,\"IdInstrumento\":0,\"IdTipoInstrumento\":\"\",\"IdClasificacionTipoInstrumento\":\"\",\"IdTipoProducto\":\"\",\"IdTipoOperacion\":\"\",\"IdOperacion\":0,\"FechaOperacion\":\"2019-10-01\",\"IdCondicionLiquidacion\":\"\",\"IdBolsa\":\"\",\"IdMonedaOperacion\":\"\",\"MontoOperacion\":0,\"IdTipoReajuste\":\"\",\"FechaParidad\":\"0001-01-01\",\"ValorParidad\":0,\"Bilateral\":\"N\",\"GeneraComprobante\":\"N\",\"AgruparEnComprobante\":\"N\",\"MueveCuentaInversion\":\"S\",\"TitularPago\":\"N\",\"NumeroReferencia\":1},{\"IdTipoMovimiento\":\"MOVEGR\",\"IdTipoPersona\":\"CNT\",\"IdPersona\":79652,\"IdPortafolio\":0,\"IdMoneda\":\"CLP\",\"Monto\":100,\"FechaLiquidacion\":\"2019-10-01\",\"IdConcepto\":\"CARGCI\",\"IdTipoPersonaTitularPago\":\"CNT\",\"IdPersonaTitularPago\":79652,\"IdPortafolioTitularPago\":0,\"IdCaja\":0,\"SecuenciaAgrupacion\":\"\",\"SecuenciaRelacionada\":null,\"Glosa\":\"\",\"IdEmpleadoDigitador\":65,\"IdSucursalEmpleadoDigitador\":0,\"IdTipoComprobante\":\"\",\"IdComprobante\":0,\"FechaComprobante\":\"0001-01-01\",\"FechaLiquidacionComprobante\":\"0001-01-01\",\"FolioCartera\":0,\"IdInstrumento\":0,\"IdTipoInstrumento\":\"\",\"IdClasificacionTipoInstrumento\":\"\",\"IdTipoProducto\":\"\",\"IdTipoOperacion\":\"\",\"IdOperacion\":0,\"FechaOperacion\":\"2019-10-01\",\"IdCondicionLiquidacion\":\"\",\"IdBolsa\":\"\",\"IdMonedaOperacion\":\"\",\"MontoOperacion\":0,\"IdTipoReajuste\":\"\",\"FechaParidad\":\"0001-01-01\",\"ValorParidad\":0,\"Bilateral\":\"N\",\"GeneraComprobante\":\"N\",\"AgruparEnComprobante\":\"N\",\"MueveCuentaInversion\":\"S\",\"TitularPago\":\"N\",\"NumeroReferencia\":-1}],\"p_EsIngresoAutomatico\":\"N\"}";
			
			CrearTraspaso_Entrada Objin = gson.fromJson(test, CrearTraspaso_Entrada.class);
			CrearTraspaso demo = new CrearTraspaso();
			
 			System.out.println("ENTRADA:");
 			System.out.println(Objin.toString());
 			
 			try 
 			{	
 				CrearTraspasos_Salida ObjOut = demo.logica(con, Objin);
 				System.out.println(ObjOut.toString());
 				//con.commit();
 				con.rollback();
 			}
 			catch(Exception er) 
 			{
				//er.printStackTrace();	
				//con.commit();
				con.rollback();
			}
 			
 			
 			con.close();
 			
	    	}
		 	
		System.out.println("== FIN PRUEBA LOCAL ==");
	}
		
}
