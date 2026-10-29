package cl.bcs.cuentaInversion;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.bat.InscribirProcesoBatch;
import cl.bcs.plataforma.bat.InscribirProcesoBatch.InTipoPrograma;
import cl.bcs.plataforma.bat.InscribirProcesoBatch.InscribirProcesoBatchEntrada;
import cl.bcs.plataforma.bat.InscribirProcesoBatch.InscribirProcesoBatchSalida;
import cl.bcs.plataforma.bat.InscribirProcesoBatch.Parametro;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ActualizarSaldoCargoAbono implements JavaCall{
	
	private final String SQL_CREATE_TEMP_TABLE_CUENTA_INVERSION = loadResourceToString("cl/bcs/cuentaInversion/sql/ActualizarSaldoCargoAbono_Create_Temp_Table_Cuenta_Inversion.sql");
	private final String SQL_INSERT_TEMP_TABLE_CUENTA_INVERSION = loadResourceToString("cl/bcs/cuentaInversion/sql/ActualizarSaldoCargoAbono_Insert_Temp_Table_Cuenta_Inversion.sql");
	private final String SQL_INSERT_TEMP_TABLE_CUENTA_INVERSION_PAUTA = loadResourceToString("cl/bcs/cuentaInversion/sql/ActualizarSaldoCargoAbono_Insert_Temp_Table_Cuenta_Inversion_Pauta.sql");
	private final String SQL_DROP_TEMP_TABLE_CUENTA_INVERSION = loadResourceToString("cl/bcs/cuentaInversion/sql/ActualizarSaldoCargoAbono_Drop_Temp_Table_Cuenta_Inversion.sql");
	private final String SQL_INSERT_CARGO_ABONO_MOVIMIENTO_PASO = loadResourceToString("cl/bcs/cuentaInversion/sql/ActualizarSaldoCargoAbono_Insert_Cargo_Abono_Movimiento_Paso.sql");
	private final String SQL_SELECT_CARGO_ABONO_MOVIMIENTO_PASO = loadResourceToString("cl/bcs/cuentaInversion/sql/ActualizarSaldoCargoAbono_Select_Cargo_Abono_Movimiento_Paso.sql");
	private final String SQL_DELETE_CARGO_ABONO_MOVIMIENTO_PASO = loadResourceToString("cl/bcs/cuentaInversion/sql/ActualizarSaldoCargoAbono_Delete_Cargo_Abono_Movimiento_Paso.sql");
	private final String SQL_SELECT_SALDO_FECHA_MOVIMIENTO = loadResourceToString("cl/bcs/cuentaInversion/sql/ActualizarSaldoCargoAbono_Select_Saldo_Fecha_Movimiento.sql");
	private final String SQL_SELECT_SALDO_FECHA_EFECTIVA = loadResourceToString("cl/bcs/cuentaInversion/sql/ActualizarSaldoCargoAbono_Select_Saldo_Fecha_Efectiva.sql");
	private final String SQL_UPDATE_SALDO_FECHA_MOVIMIENTO = loadResourceToString("cl/bcs/cuentaInversion/sql/ActualizarSaldoCargoAbono_Update_Saldo_Fecha_Movimiento.sql");
	private final String SQL_UPDATE_SALDO_FECHA_EFECTIVA = loadResourceToString("cl/bcs/cuentaInversion/sql/ActualizarSaldoCargoAbono_Update_Saldo_Fecha_Efectiva.sql");	
	private final static Logger logger = LogManager.getLogger(ActualizarSaldoCargoAbono.class);
	
	public static class ActualizarSaldoCargoAbonoEntrada {
		public Long p_IdEmpresa;
	    public Long p_IdUsuario;
		public String p_FechaMovimiento;
		public String p_ActualizarTodosLosSaldos;
		public List<CuentaInversion> p_CuentaInversion;
		
		public ActualizarSaldoCargoAbonoEntrada() {
			p_ActualizarTodosLosSaldos = "N";
			p_CuentaInversion = new ArrayList<CuentaInversion>();
		};
		
		public String toString() {
			return Utils.printJson(this, false);
		};
		
	};
	
	public static class CargoAbonoMovimiento{
		public Long IdTrabajo;
		public Long IdCuentaInversion;
		public Long IdCargoAbonoMovimiento;
		public String IdTipoMovimiento;
		public BigDecimal Monto;		 		
		public BigDecimal Saldo;
		public BigDecimal SaldoPorFechaMovimiento;
		public Date FechaSistema;
		public Long RankFechaMovimiento;
		public Date FechaEfectivaMovimiento;
		public Long RankFechaEfectiva;
		public Timestamp FechaCertificacion;
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class ActualizarSaldoCargoAbonoSalida {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		
		public ActualizarSaldoCargoAbonoSalida() {
			P_OUT_MENSAJES = new ArrayList<>();
			P_OUT_ERRORESCONTADOR = 0L;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SaldoEfectivo{
		public Long IdCargoAbonoMovimiento;
		public Long IdCuentaInversion;
		public Long RankFechaEfectiva;
		public Long RankEfectivoAnterior;
		public BigDecimal SaldoEfectivoAnterior;
		public BigDecimal SaldoEfectivo;
		public BigDecimal Monto;
		

		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public static class SaldoMovimiento{
		public Long IdCargoAbonoMovimiento;
		public Long IdCuentaInversion;
		public Long RankFechaMovimiento;
		public Long RankMovimientoAnterior;
		public BigDecimal SaldoMovimientoAnterior;
		public BigDecimal SaldoMovimiento;
		public BigDecimal Monto;	
		

		public String toString() {
			return Utils.printJson(this, false);
		}		
	}
	
	public static class CuentaInversion{
		public Long IdCuentaInversion;	
		
		public String toString() {
			return Utils.printJson(this, false);
		}		
	}	
	
	public static class ParamsCargoAbono_Update{
		public Long IdCargoAbonoMovimiento;	
		public BigDecimal Saldo;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Trace
	public ActualizarSaldoCargoAbonoSalida logica(Connection con, ActualizarSaldoCargoAbonoEntrada entrada) throws Exception {
		logger.info("<<INICIO::ActualizarSaldoCargoAbono>>");
		SQL sql = new SQL();
		ActualizarSaldoCargoAbonoSalida out = new ActualizarSaldoCargoAbonoSalida();
		HashMap<String,Object> params = new HashMap<>();
		SQL.exec(con, SQL_CREATE_TEMP_TABLE_CUENTA_INVERSION);
		
		if("N".equalsIgnoreCase(entrada.p_ActualizarTodosLosSaldos) && !entrada.p_CuentaInversion.isEmpty()) {
			List<Long> listaIdsCuentaInversion = new ArrayList<>();
			List<CuentaInversion> listaCuentaInversion = new ArrayList<CuentaInversion>();
			
			listaIdsCuentaInversion = entrada.p_CuentaInversion.stream()
															   .map(c -> c.IdCuentaInversion)
															   .distinct()
														       .collect(Collectors.toList());
			
			listaCuentaInversion = listaIdsCuentaInversion.stream()
														  .map(c -> {
															  CuentaInversion cuenta = new CuentaInversion();
															  cuenta.IdCuentaInversion = c;
															  return cuenta;
														    })
														  .collect(Collectors.toList());
			
			logger.debug("INICIO::SQL_INSERT_TEMP_TABLE_CUENTA_INVERSION");
			sql.batch(con, SQL_INSERT_TEMP_TABLE_CUENTA_INVERSION, listaCuentaInversion, CuentaInversion.class);
			logger.debug("FIN::SQL_INSERT_TEMP_TABLE_CUENTA_INVERSION");
		}
		
		if("S".equalsIgnoreCase(entrada.p_ActualizarTodosLosSaldos) && entrada.p_CuentaInversion.isEmpty()) {
			//INSERT TODAS LAS CUENTAS DE INVERSION ACTIVAS (PAUTA)
			logger.debug("INICIO::SQL_INSERT_TEMP_TABLE_CUENTA_INVERSION_PAUTA");
			sql.batch(con, SQL_INSERT_TEMP_TABLE_CUENTA_INVERSION_PAUTA, new ArrayList<CuentaInversion>(), CuentaInversion.class);
			logger.debug("FIN::SQL_INSERT_TEMP_TABLE_CUENTA_INVERSION_PAUTA");
		}
		
		//INSERTAR CARGO ABONO MOVIMIENTO EN TABLA DE PASO
		List<CargoAbonoMovimiento> listaCargoAbono = sql.query(con, SQL_SELECT_CARGO_ABONO_MOVIMIENTO_PASO, params, CargoAbonoMovimiento.class);
		
		if (!listaCargoAbono.isEmpty()) {
			Long idTrabajo = new Double(Math.random()*10000000000L).longValue();
			listaCargoAbono.forEach(c -> c.IdTrabajo = idTrabajo);
			logger.debug("INICIO::Insert Cargo Abono Movimiento en Tabla de Paso");
			sql.batch(con, SQL_INSERT_CARGO_ABONO_MOVIMIENTO_PASO, listaCargoAbono, CargoAbonoMovimiento.class);
			logger.debug("FIN::Insert Cargo Abono Movimiento en Tabla de Paso");
			
			//SALDOS
			HashMap<String,Object> parmSaldos = new HashMap<String,Object>();
			parmSaldos.put("p_FechaMovimiento", entrada.p_FechaMovimiento);
			
			//OBTENER SALDO FECHA MOVIMIENTO
			List<SaldoMovimiento> listaSaldoMovimiento = new ArrayList<SaldoMovimiento>();
			logger.debug("INICIO::Select Saldo Fecha Movimiento");
			listaSaldoMovimiento = sql.query(con, SQL_SELECT_SALDO_FECHA_MOVIMIENTO, parmSaldos, SaldoMovimiento.class);
			logger.debug("FIN::Select Saldo Fecha Movimiento");
			
			HashMap<String,Object> listaSaldoMovimientoHash = Utils.List2Hash(listaSaldoMovimiento, "IdCargoAbonoMovimiento");
			
			//ACTUALIZAR SALDO FECHA MOVIMIENTO
			List<ParamsCargoAbono_Update> update_SaldoMovimiento = new ArrayList<ParamsCargoAbono_Update>();
			if(listaSaldoMovimiento.size()>0){
				for (SaldoMovimiento fila : listaSaldoMovimiento){
					SaldoMovimiento saldo = (SaldoMovimiento)listaSaldoMovimientoHash.get(fila.IdCargoAbonoMovimiento);
					ParamsCargoAbono_Update parms_saldo_movimiento = new ParamsCargoAbono_Update();
					parms_saldo_movimiento.IdCargoAbonoMovimiento = saldo.IdCargoAbonoMovimiento;
					parms_saldo_movimiento.Saldo = saldo.SaldoMovimiento;
					update_SaldoMovimiento.add(parms_saldo_movimiento);
					
				}
			}
			
			logger.debug("INICIO::Update Saldo Fecha Movimiento");
			sql.batch(con,SQL_UPDATE_SALDO_FECHA_MOVIMIENTO,update_SaldoMovimiento, ParamsCargoAbono_Update.class);
			logger.debug("FIN::Update Saldo Fecha Movimiento");
			
			//OBTENER SALDO FECHA EFECTIVA
			List<SaldoEfectivo> listaSaldoEfectivo = new ArrayList<SaldoEfectivo>();
			logger.debug("INICIO::Select Saldo Fecha Efectiva");
			listaSaldoEfectivo = sql.query(con, SQL_SELECT_SALDO_FECHA_EFECTIVA, parmSaldos, SaldoEfectivo.class);
			logger.debug("FIN::Select Saldo Fecha Efectiva");
			
			HashMap<String,Object> listaSaldoEfectivoHash = Utils.List2Hash(listaSaldoEfectivo, "IdCargoAbonoMovimiento");
			
			//ACTUALIZAR SALDO FECHA EFECTIVA
			List<ParamsCargoAbono_Update> update_SaldoEfectiva = new ArrayList<ParamsCargoAbono_Update>();
			if(listaSaldoEfectivo.size()>0){
				for (SaldoEfectivo fila : listaSaldoEfectivo){
					SaldoEfectivo saldo = (SaldoEfectivo)listaSaldoEfectivoHash.get(fila.IdCargoAbonoMovimiento);
					ParamsCargoAbono_Update parms_saldo_efectivo = new ParamsCargoAbono_Update();
					parms_saldo_efectivo.IdCargoAbonoMovimiento = saldo.IdCargoAbonoMovimiento;
					parms_saldo_efectivo.Saldo = saldo.SaldoEfectivo;
					update_SaldoEfectiva.add(parms_saldo_efectivo);
					
				}
			}
			
			logger.debug("INICIO::Update Saldo Fecha Efectiva");
			sql.batch(con,SQL_UPDATE_SALDO_FECHA_EFECTIVA,update_SaldoEfectiva, ParamsCargoAbono_Update.class);
			logger.debug("FIN::Update Saldo Fecha Efectiva");
			
			SQL.exec(con, SQL_DELETE_CARGO_ABONO_MOVIMIENTO_PASO.replace(":IdTrabajo", idTrabajo+""));
		}
		
		SQL.exec(con, SQL_DROP_TEMP_TABLE_CUENTA_INVERSION);
		logger.info("<<FIN::ActualizarSaldoCargoAbono>>");
		return out;
		
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		ActualizarSaldoCargoAbonoEntrada jin=gson.fromJson(in.toString() ,ActualizarSaldoCargoAbonoEntrada.class);
		JsonParser parser = new JsonParser();
		return null;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa02", "connections.properties")) {
			Gson gson = Utils.getGsonBuilder();			
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_FechaMovimiento\":\"2024-08-20\",\"p_ActualizarTodosLosSaldos\":\"N\",\"p_CuentaInversion\":[{\"IdCuentaInversion\":7925},{\"IdCuentaInversion\":7925}]}";
			ActualizarSaldoCargoAbonoEntrada Objin = gson.fromJson(json, ActualizarSaldoCargoAbonoEntrada.class);
			ActualizarSaldoCargoAbono demo = new ActualizarSaldoCargoAbono();				
			try {
				demo.logica(con, Objin);
				con.rollback();
			}
			catch(Exception er) {
				er.printStackTrace();	
				//con.rollback();
			}
			//con.rollback(); 			
    	}
		System.out.println("== END ==");
	}
}