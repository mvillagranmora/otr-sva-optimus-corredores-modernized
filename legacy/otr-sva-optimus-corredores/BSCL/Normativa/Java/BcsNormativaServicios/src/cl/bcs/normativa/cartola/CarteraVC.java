package cl.bcs.normativa.cartola;

import java.math.BigDecimal;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CarteraVC implements BcsCartola{
	
	public final Log log = new Log("NOR.CarteraVC");
	
	private final String SQL_SELECT_PRESTAMOS = loadResourceToString("cl/bcs/normativa/cartola/sql/CarteraVC_SelectPrestamos.sql");
	private final String SQL_INSERT_TEMPORAL_CLIENTES = "INSERT INTO #TempIdClientes SELECT \"IdCliente\"  FROM BSCL.\"BSCL.SocioNegocio.BD::CLI.Cliente\" WHERE \"IdCliente\"IN (:IdTemp)";
	private final String SQL_INSERT_TEMPORAL_PORTAFOLIOS = "INSERT INTO #TempIdPortafolios SELECT \"IdPortafolio\"  FROM BSCL.\"BSCL.Portafolio.BD::POR.Portafolio\" WHERE \"IdPortafolio\"IN (:IdTemp)";
	private final String SQL_CREATE_TEMPORAL_CLIENTES = "CREATE LOCAL TEMPORARY TABLE #TempIdClientes (IdCliente BIGINT)";
	private final String SQL_CREATE_TEMPORAL_PORTAFOLIOS = "CREATE LOCAL TEMPORARY TABLE #TempIdPortafolios (IdPortafolio BIGINT)"; 
	
	public static class OperacionesSalida{
		public Long IdCliente;
		public Long IdPortafolio;
		public String Tipo_Operacion;
		public String Tipo_Prestamo;
		public String Nemo;
		public BigDecimal Cantidad;
		public Integer plazo;
		public BigDecimal Tasa;
		public String Operacion;
		public String F_Vencimiento;
		public String F_Liquidacion;
		public String F_Registro;
		public BigDecimal Precio;
		public BigDecimal monto;
		public BigDecimal Prima_Premio;
		public BigDecimal Precio_Cierre;
		public BigDecimal Monto_Cierre;
		public BigDecimal resultado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	public static class Operaciones{
		public Long IdCliente;
		public Long IdPortafolio;
		public String Tipo_Operacion;
		public String Tipo_Prestamo;
		public String Nemo;
		public BigDecimal Cantidad;
		public Long Plazo;
		public BigDecimal Tasa;
		public String Operacion;
		public Date F_Vencimiento;
		public Date F_Registro;
		public Date F_Liquidacion;
		public BigDecimal Precio;
		public BigDecimal monto;
		public BigDecimal Prima_Premio;
		public BigDecimal Precio_Cierre;
		public BigDecimal Monto_Cierre;
		public BigDecimal resultado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class InsertTemporalIds{
		public Long IdTemp;
		public String toString() {
			return Utils.printJson(this, false);
		}
	    public InsertTemporalIds(Long idTemp) {
	        this.IdTemp = idTemp;
	    }

	} 
	
	public SalidaCartola procesar(Connection con, EntradaCartola entrada) throws Exception{
		log.write("INICIO::CarteraVC");
		SQL sql = new SQL();
		SalidaCartola out = new SalidaCartola();
		
		SQL.exec(con,SQL_CREATE_TEMPORAL_CLIENTES);
		SQL.exec(con,SQL_CREATE_TEMPORAL_PORTAFOLIOS);
		
		
		List<InsertTemporalIds> paramsinsertCli = entrada.p_Datos.stream().map(dato -> new InsertTemporalIds(dato.IdCliente)).collect(Collectors.toList());
		sql.batch(con,SQL_INSERT_TEMPORAL_CLIENTES, paramsinsertCli, InsertTemporalIds.class);
		List<InsertTemporalIds> paramsinsertPor = entrada.p_Datos.stream().map(dato -> new InsertTemporalIds(dato.IdCliente)).collect(Collectors.toList());
		sql.batch(con,SQL_INSERT_TEMPORAL_PORTAFOLIOS, paramsinsertPor, InsertTemporalIds.class);
		
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		//params.put("FechaDesde", entrada.p_FechaDesde);
		params.put("FechaHasta", entrada.p_FechaHasta);
		
		List<Operaciones> operaciones = sql.query(con, SQL_SELECT_PRESTAMOS, params, Operaciones.class);
		
		SQL.exec(con, "DROP TABLE #TempIdClientes");
		SQL.exec(con, "DROP TABLE #TempIdPortafolios");
		
		List<OperacionesSalida> salidaOperaciones = new ArrayList<OperacionesSalida>();
		for(Operaciones op: operaciones) {
			OperacionesSalida salida = new OperacionesSalida();
			salida.IdCliente = op.IdCliente;
			salida.IdPortafolio = op.IdPortafolio;
			salida.Tipo_Operacion = op.Tipo_Operacion;
			salida.Tipo_Prestamo = op.Tipo_Prestamo;
			salida.Nemo = op.Nemo;
			salida.Cantidad = op.Cantidad;
			salida.plazo = Math.toIntExact(op.Plazo);
			salida.Tasa = op.Tasa;
			salida.Operacion = op.Operacion;
			salida.F_Vencimiento = new SimpleDateFormat("dd-MM-yyyy").format(op.F_Vencimiento).toString();
			salida.F_Liquidacion = new SimpleDateFormat("dd-MM-yyyy").format(op.F_Liquidacion).toString();
			salida.F_Registro = new SimpleDateFormat("dd-MM-yyyy").format(op.F_Registro).toString();
			salida.Precio = op.Precio;
			salida.monto = op.monto;
			salida.Prima_Premio = op.Prima_Premio;
			salida.Precio_Cierre = op.Precio_Cierre;
			salida.Monto_Cierre = op.Monto_Cierre;
			salida.resultado = op.Monto_Cierre.subtract(op.monto);
			
			salidaOperaciones.add(salida);
		}
		
		List<HashMap<String,Object>> resultadoCarteraVC = Utils.List2ListHashGenericObject(salidaOperaciones);
		
		out.P_OUT_SALIDA.put("CarteraVC", resultadoCarteraVC);
		
		log.write("FIN::CarteraVC");
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
		//	try (Connection con = PlataformaUtils.getMBIPREConnection()) {	 
		EntradaCartola Objin = new EntradaCartola();
		Objin.p_IdEmpresa = 1L;
		Objin.p_IdModulo = "NOR";
		Objin.p_IdUsuario = 21L;
		Objin.p_FechaDesde = Fecha.toDate("2020-12-31");
		Objin.p_FechaHasta = Fecha.toDate("2020-12-31");
		
		Dato dato = new Dato();
		dato.IdCliente = 524891L;
		dato.IdPortafolio = 1451715L;
		Objin.p_Datos.add(dato);

		
		
		CarteraVC  demo= new CarteraVC();				
		try {
			SalidaCartola ObjOut=demo.procesar(con, Objin);
			con.rollback();
			System.out.println(ObjOut.toString());
		}
		catch(Exception er) {
			er.printStackTrace();	
			con.rollback();
		}
			con.rollback(); 			
    	}
		System.out.println("== END ==");
	}
}

