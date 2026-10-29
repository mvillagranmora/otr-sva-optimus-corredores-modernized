package cl.bcs.loaderapp.concrete.entidadmercado;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.apache.commons.dbcp.BasicDataSource;

import cl.bcs.loaderapp.App.EntradaCargaGeneral;
import cl.bcs.loaderapp.interfaces.DbLoader;
import cl.bcs.loaderapp.utils.LoaderUtils;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CuponDbLoader implements DbLoader{
	
	public String SQL_CREAR_CUPON = loadResourceToString("cl/bcs/loaderapp/sql/instrumento/Cupon_Ingresar.sql");

	public static class EntidadCupon{
		public String Nemotecnico;
		public String Numero_Cupon;
		public String Fecha_Pago_Cupon;
		public BigDecimal Amortizacion;
		public BigDecimal Saldo_amortizar;
		public BigDecimal Intereses;
		public String Indicador_Call;
		public int LINEA;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	
	@Override
	public List<String> loadIntoDb(BasicDataSource jsJB, EntradaCargaGeneral in, List<Object> lines) {
		List<String> salida = new ArrayList<String>();
		try {
			List<CSVRecord> recordsCSV = (List<CSVRecord>) (Object) lines;
			List<EntidadCupon> cupones = new ArrayList<>();
			int inlinea = 0;
			SimpleDateFormat sdf = new SimpleDateFormat("dd-mm-yyyy");
			SimpleDateFormat sdfSql = new SimpleDateFormat("yyyy-mm-dd");
			for (CSVRecord record : recordsCSV) {
				EntidadCupon cupon = new EntidadCupon();
				cupon.Nemotecnico = record.get("Nemotecnico");
				cupon.Numero_Cupon = record.get("Numero_Cupon");
				cupon.Fecha_Pago_Cupon = sdfSql.format(sdf.parse(record.get("Fecha_Pago_Cupon")));
				cupon.Amortizacion = LoaderUtils.analizarNumero( record.get("Amortizacion") );
				cupon.Saldo_amortizar = LoaderUtils.analizarNumero(record.get("Saldo_amortizar"));
				cupon.Intereses = LoaderUtils.analizarNumero(record.get("Intereses"));
				cupon.Indicador_Call = (record.get("Indicador_Call")!=null && !record.get("Indicador_Call").equalsIgnoreCase("NULL")) ? record.get("Indicador_Call") : "N";
				cupon.LINEA = inlinea++;
				cupones.add(cupon);
			}
			
			SQL sql = new SQL();
			cupones.parallelStream().forEach( cupon -> {
				int linea = cupon.LINEA;
				String mensaje = "";
				try (Connection conn = jsJB.getConnection()){
					
					try {
						sql.batch(conn, SQL_CREAR_CUPON, Arrays.asList(cupon), EntidadCupon.class);				
						mensaje = (linea + " - CUPON " + cupon.Nemotecnico + " INGRESADO CORRECTAMENTE");
						conn.commit();
					}catch(Exception ex) {
						mensaje = (linea + " - CUPON " + cupon.Nemotecnico + " "+ ex.getMessage());
						conn.rollback();
					}
					
				} catch (SQLException e) {
					mensaje = (linea + " - CUPON " + cupon.Nemotecnico +" "+ e.getMessage());
				}
				System.out.println(mensaje);
				salida.add(mensaje);
			});
		}catch(Exception e) {
			e.printStackTrace();
			salida.add("Error Grave: " + e.getMessage());
		}
		
		return salida;
	}

}
