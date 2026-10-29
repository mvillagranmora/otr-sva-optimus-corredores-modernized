package cl.bcs.custodia.jobs;
  

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import cl.bcs.custodia.NotificarVencimientoInstrumentos;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class VencimientoProductos extends BcsJob{
	
	private static String SQL_SELECT_CARTERAS_A_VENCER = "SELECT \"IdCartera\" FROM \"BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera\" WHERE \"FechaVencimientoOperacion\" = :fechaVencimiento;";
	
	public static class ProductosVencer{
		public Long IdCartera;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public boolean logica(Connection conn, Long idEmpresa){
		try {
			SQL sql = new SQL();
			//obtener fecha sistema, y sumar la cantidad de dias para obtener los productos a vencer
			String fechaSistema = Utils.getFechaSistema(conn, Utils.getSchema(), idEmpresa);
			String cantidadDiasVencimiento = ConfiguracionSistema.getParametroConfiguracionString(conn, idEmpresa, "CUSTODIA", "CUS.DiasVencimientoProductos");
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Instant instanteActual = sdf.parse(fechaSistema).toInstant();
			ZoneId zonaActual = ZoneId.systemDefault();
			LocalDateTime ldt = LocalDateTime.ofInstant(instanteActual, zonaActual);
			ldt = ldt.plusDays(Long.parseLong(cantidadDiasVencimiento));
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			String fechaVencimiento = ldt.format(formatter);
			
			System.out.println(fechaSistema);
			System.out.println(cantidadDiasVencimiento);
			System.out.println(fechaVencimiento);
			
			//obtener productos a vencer
			HashMap<String, Object> params = new HashMap<>();
			params.put("fechaVencimiento", fechaVencimiento);
			List<ProductosVencer> productosVencer = sql.query(conn, SQL_SELECT_CARTERAS_A_VENCER, params, ProductosVencer.class);
			
			//Notificar vencimiento
			NotificarVencimientoInstrumentos notificar = new NotificarVencimientoInstrumentos();
			NotificarVencimientoInstrumentos.EntradaNotificarVencimientoInstrumentos entradaNotificar = new NotificarVencimientoInstrumentos.EntradaNotificarVencimientoInstrumentos();
			entradaNotificar.p_IdEmpresa = idEmpresa;
			entradaNotificar.p_IdUsuario = new Long(0);
			entradaNotificar.p_IdModulo = "CUS";
			entradaNotificar.vencimientos = productosVencer.stream().map( producto -> producto.IdCartera ).distinct().collect(Collectors.toList());
			System.out.println(notificar.logica(conn, entradaNotificar));
			
			return true;
		}catch (Exception e) {
			System.out.println(":DOCUMENTOSPORVENCER:  ****** ERROR AL OBTENER DATASOURCE  *******");  
			e.printStackTrace();
		} 
		return false;
	}
	
	public static boolean DEBUG = true;
	
	@Override
	public boolean transaction(Connection dbConnection) throws Exception{
		if(DEBUG){
			final long idEmpresa = 1;
			return logica(dbConnection, idEmpresa);			
		}else{
			final long idEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
			return logica(dbConnection, idEmpresa);
		}
	}
	
	@Override
    public void onError(Throwable t) {
        System.out.println(":VENCIMIENTODOCUMENTOS:  ****** ERROR  *******");  
        t.printStackTrace();
    }

	@Override
	public void pause() {

	}
	
	//solo para test
	public static void main(String[] args) {
		// Example
		System.out.println("== START ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			con.setAutoCommit(false);
			SQL.exec(con, "set schema BSCL");
			SQL.exec(con, "SET TRANSACTION AUTOCOMMIT DDL OFF");
			// ---------------------------------
			try {
				VencimientoProductos apf = new VencimientoProductos();
				System.out.println(apf.transaction(con));
				con.rollback();
			} catch (Exception er) {
				con.rollback();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}
	
	
}
