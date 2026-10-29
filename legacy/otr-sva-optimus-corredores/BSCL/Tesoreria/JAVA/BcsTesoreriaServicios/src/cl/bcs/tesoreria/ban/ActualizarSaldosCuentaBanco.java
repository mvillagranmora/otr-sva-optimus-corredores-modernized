package cl.bcs.tesoreria.ban;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.tesoreria.CrearComprobante;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;


public class ActualizarSaldosCuentaBanco  implements JavaCall
{

	// Log
	static final Logger logger = LogManager.getLogger(ActualizarSaldosCuentaBanco.class);
    
    private final String SQL_SELECT_SALDO_DISPONIBLE_INICIAL 	 = loadResourceToString("cl/bcs/tesoreria/sql/ActualizarSaldosCuentaBanco_SelectSaldoDisponibleInicial.sql");
    private final String SQL_INSERT_TABLA_TEMPORAL 			 	 = loadResourceToString("cl/bcs/tesoreria/sql/ActualizarSaldosCuentaBanco_InsertTablaTemporal.sql");
    private final String SQL_SELECT_SALDO_DISPONIBLE_MOVIMIENTOS = loadResourceToString("cl/bcs/tesoreria/sql/ActualizarSaldosCuentaBanco_SelectSaldoDisponibleMovimientos.sql");
    private final String SQL_SELECT_SALDO_PENDIENTE 			 = loadResourceToString("cl/bcs/tesoreria/sql/ActualizarSaldosCuentaBanco_SelectSaldoPendiente.sql");
    private final String SQL_UPDATE_SALDOS 						 = loadResourceToString("cl/bcs/tesoreria/sql/ActualizarSaldosCuentaBanco_UpdateSaldoContable.sql");
    private final String SQL_DELETE_TABLA_TEMPORAL 			 	 = loadResourceToString("cl/bcs/tesoreria/sql/ActualizarSaldosCuentaBanco_DeleteTablaTemporal.sql");
       
    //-------------------------------------------------------------------------------------------
	// CLASES QUE REPRESENTAN OBJETOS IN
	//-------------------------------------------------------------------------------------------

	public static class ActualizarSaldosCuentaBanco_Entrada
	{	
		public String 						p_FechaMovimiento;
		public List<CuentaBanco_Entrada>	p_CuentaBanco;
		
		public ActualizarSaldosCuentaBanco_Entrada()
		{
			this.p_CuentaBanco = new ArrayList<CuentaBanco_Entrada>();
		}
		
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}


	public static class CuentaBanco_Entrada
	{	
		public Long 	IdCuentaBanco;
		
		public String toString()
		{
			return Utils.printJson(this, false);
		}		
	}
	
	
	public static class SaldoDisponible
	{
		public	Long		IdCuentaBanco;
		public	Long		IdComprobanteFormaDePago;
		public  String		FechaAfectaSaldoAnterior;
		public	BigDecimal	SaldoAnterior;
		
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	public static class Movimientos
	{
		public Long			IdComprobanteFormaDePago;
		public Long			IdCuentaBanco;
		public String		FechaHoraAfectaSaldo;
		public String		FechaHoraLiquidacion;
		public BigDecimal	Monto;		
		public BigDecimal	SaldoDisponible;
		public BigDecimal	SaldoPendiente;
		
		public String toString()
		{
			return Utils.printJson(this, false);
		}
		
		public Timestamp getFechaHoraAfectaSaldo(){
			Timestamp fecha = Timestamp.valueOf(this.FechaHoraAfectaSaldo);
			return fecha;
		}
		
		public Long getIdComprobanteFormaDePago(){
			return this.IdComprobanteFormaDePago;
		}
	}
	
	public static class MovimientoActualizar {
		public BigDecimal 	SaldoDisponible;
		public Long 		IdComprobanteFormaDePago;
	}
	
	public static class SaldoPendiente
	{
		public	Long		IdCuentaBanco;
		public	BigDecimal	SaldoPendiente;
		
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
    //-------------------------------------------------------------------------------------------
	// CLASES  QUE REPRESENTAN OBJETOS OUT	
	//-------------------------------------------------------------------------------------------	

	public static class ActualizarSaldosCuentaBanco_Salida
	{
		public List<MensajeSalida>			 P_OUT_MENSAJES;
		public Long							 P_OUT_ERRORESCONTADOR;
		
		
		public ActualizarSaldosCuentaBanco_Salida()
		{
			P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}	

	//-------------------------------------------------------------------------------------------
	// CALL - OBJETO DE TRANSPORTE
	//-------------------------------------------------------------------------------------------
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = getGsonBuilder();
		
		ActualizarSaldosCuentaBanco_Entrada in = gson.fromJson(jin.toString(), ActualizarSaldosCuentaBanco_Entrada.class);
		ActualizarSaldosCuentaBanco_Salida out = logica(con, in);		

		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	//-------------------------------------------------------------------------------------------
	// LOGICA DEL SERVICIO
	//------------------------------------------------------------------------------------------
	@Trace
	public ActualizarSaldosCuentaBanco_Salida logica(Connection con, ActualizarSaldosCuentaBanco_Entrada in) throws Exception {
		SQL sql = new SQL();
		ActualizarSaldosCuentaBanco_Salida out = new ActualizarSaldosCuentaBanco_Salida();
		
		logger.info("== INICIO ACTUALIZAR SALDOS CUENTA BANCO ==");
		
		try {
			HashMap<String,Object> parmsVacio = new HashMap<String,Object>();
			
			// Insertar Escalares de entrada
			HashMap<String, Object> parmsIN = new HashMap<String, Object>();			
			parmsIN.put("p_FechaMovimiento", in.p_FechaMovimiento);
		
			// Actualizar Saldo Disponible
			logger.debug("INSERT TABLA TEMPORAL");
			sql.batch(con, SQL_INSERT_TABLA_TEMPORAL, in.p_CuentaBanco, CuentaBanco_Entrada.class);
			logger.debug("SELECT SALDO DISPONIBLE INCIAL");
			List<SaldoDisponible> lstSaldoDisponible = sql.query(con, SQL_SELECT_SALDO_DISPONIBLE_INICIAL, parmsIN, SaldoDisponible.class);
			logger.debug("SELECT MOVIMIENTOS");
			List<Movimientos> lstMovimientos = sql.query(con, SQL_SELECT_SALDO_DISPONIBLE_MOVIMIENTOS, parmsIN, Movimientos.class);
			// Actualizar Saldo Contable (Saldo disponible + Saldo Pendiente + Tn (que no han afectado saldo))
			logger.debug("SELECT SALDO PENDIENTE");
			List<SaldoPendiente> lstSaldoPendiente = sql.query(con, SQL_SELECT_SALDO_PENDIENTE, parmsIN, SaldoPendiente.class);
			List<Movimientos> lstMovimientosPorActualizar = new ArrayList<>();
			
			if(in.p_CuentaBanco.size()>0){
				for(CuentaBanco_Entrada cuentas:in.p_CuentaBanco){
					BigDecimal saldoAnterior = lstSaldoDisponible.stream()
								.filter(saldo -> saldo.IdCuentaBanco.compareTo(cuentas.IdCuentaBanco)==0)
								.map(saldo -> saldo.SaldoAnterior)
								.findFirst()
								.orElse(BigDecimal.ZERO);
					BigDecimal saldoAnteriorPendiente = lstSaldoPendiente.stream()
							.filter(saldo -> saldo.IdCuentaBanco.compareTo(cuentas.IdCuentaBanco)==0)
							.map(saldo -> saldo.SaldoPendiente)
							.findFirst()
							.orElse(BigDecimal.ZERO);
					List<Movimientos> lstMovimientosFiltrados = lstMovimientos.stream()
								.filter(mov -> mov.IdCuentaBanco.compareTo(cuentas.IdCuentaBanco)==0)
								.collect(Collectors.toList());
					if(lstMovimientosFiltrados.size()>0){
						BigDecimal saldoAuxiliar = saldoAnterior;
						BigDecimal saldoAuxiliarPendiente = saldoAnteriorPendiente;
						lstMovimientosFiltrados.sort(Comparator.comparing(Movimientos::getFechaHoraAfectaSaldo).thenComparing(Movimientos::getIdComprobanteFormaDePago));
						for(Movimientos mov:lstMovimientosFiltrados) {
							mov.SaldoDisponible = mov.Monto.add(saldoAuxiliar);
							saldoAuxiliar = mov.SaldoDisponible;
							mov.SaldoPendiente = mov.Monto.add(saldoAuxiliarPendiente);
							saldoAuxiliarPendiente = mov.SaldoPendiente;
							lstMovimientosPorActualizar.add(mov);
						}
					}
				}
			}
			if(!lstMovimientosPorActualizar.isEmpty()){
				logger.debug("UPDATE SALDOS");
				sql.batch(con, SQL_UPDATE_SALDOS, lstMovimientosPorActualizar, Movimientos.class);
			}
			
		} 
		catch (Exception e) {
			logger.error(e.getMessage());
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_ACTUALIZAR_SALDO_CUENTA_BANCO","ERROR AL ACTUALIZAR SALDO CUENTA BANCO"));
			throw e;
		} finally {
			logger.debug("LIMPIAR TABLA TEMPORAL");
			SQL.exec(con, SQL_DELETE_TABLA_TEMPORAL);
		}
		out.P_OUT_ERRORESCONTADOR = 0L;		
		logger.info("== TERMINO ACTUALIZAR SALDOS CUENTA BANCO ==");		
		return out;
	}
	
	//-------------------------------------------------------------------------------------------
	// METODO PARA PRUEBAS LOCALES
	//-------------------------------------------------------------------------------------------
	
	
	public static void main(String[] args) throws Exception 
	{
		System.out.println("== INICIO PRUEBA LOCAL ==");
		
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa02","connections.properties")) 
		{
			Gson gson = Utils.getGsonBuilder();

			String json = "{\"p_FechaMovimiento\":\"2024-09-12\",\"p_CuentaBanco\":[{\"IdCuentaBanco\":1089},{\"IdCuentaBanco\":1094}]}";
			
			ActualizarSaldosCuentaBanco_Entrada Objin = gson.fromJson(json, ActualizarSaldosCuentaBanco_Entrada.class);
			ActualizarSaldosCuentaBanco demo = new ActualizarSaldosCuentaBanco();
			
 			System.out.println("ENTRADA:");
 			System.out.println(Objin.toString());
 			
 			try 
 			{
 				ActualizarSaldosCuentaBanco_Salida ObjOut = demo.logica(con, Objin);
 				System.out.println(ObjOut.toString());
 				con.rollback();
 				//con.commit();
 			}
 			catch(Exception er) 
 			{
				er.printStackTrace();	
				//con.commit();
			}
 			
 			
 			con.close();
 			
	    	}
		 	
		System.out.println("== FIN PRUEBA LOCAL ==");
	}
	
}
