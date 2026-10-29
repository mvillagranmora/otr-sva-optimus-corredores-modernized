package cl.bcs.facturacion.afp;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class AFPProvida implements InterfaceAFP {

	private String cosultarTransaccionesProvidaSQL;
	
	public static class TransaccionesProvida {
		public String FechaTransaccion;
		public String FolioTransaccion;
		public String CodigoCorredorVendedor;
		public String NemotecnicoCorredorVendedor;
		public String CodigoCorredorComprador;
		public String NemotecnicoCorredorComprador;
		public BigDecimal CantidadTransada;
		public BigDecimal Precio;
		public String CondicionTransaccion;
		public String HoraTransaccion;
		public String NemotecnicoInstrumento;
		public String NemotecnicoNoInscritos;
		public BigDecimal TotalTransado;
		public String OperadorInternoVenta;
		public String OperadorInternoCompra;
		public String IndicadorDCV;
		public String CodigoOperadorVendedor;
		public String CodigoOperadorComprador;
		public String Disponible;
		public String OrdenDirecta;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public AFPProvida(String consultaSQL) {
		this.cosultarTransaccionesProvidaSQL = consultaSQL;
	}

	@Override
	public String obtenerData(Long idAFP, String fecha, String esJPM, Connection con) throws SQLException {
		SQL sql = new SQL();
		HashMap<String, Object> parametrosConsulta = new HashMap<>();
		parametrosConsulta.put("IdAFP", idAFP);
		parametrosConsulta.put("Fecha", fecha);
		List<TransaccionesProvida> transaccionesAFP = sql.query(con, cosultarTransaccionesProvidaSQL,
				parametrosConsulta, TransaccionesProvida.class);
		return formatoProvida(transaccionesAFP, esJPM);
	}

	/**
	 * Metodo que retorna el texto en el formato de AFP Provida dependiendo del estado del parametro JPM
	 * @param transaccionesAFP Listado de transacciones
	 * @param esJPM Parametro JP Morgan
	 * @return Texto con formato correspondiente
	 */
	private String formatoProvida(List<TransaccionesProvida> transaccionesAFP, String esJPM) {
		StringBuilder archivo = new StringBuilder();
		String vacio = "No existen transacciones";
		if (transaccionesAFP.isEmpty()) {
			System.out.println("========================");
			System.out.println(vacio);
			System.out.println("========================");
			return vacio;
		} else {
			if (esJPM.equalsIgnoreCase("S")) {
				System.out.println("===========:: PARAMETRO ON ::===========");
				for (TransaccionesProvida transaccion : transaccionesAFP) {
					if (transaccion.OrdenDirecta.equalsIgnoreCase("N")) {
						archivo.append(String.format("%-8s", transaccion.FechaTransaccion).replace(' ', ' '));
						archivo.append(String.format("%-6s", transaccion.FolioTransaccion).replace(' ', ' '));
						archivo.append(String.format("%-3s", transaccion.CodigoCorredorVendedor).replace(' ', ' '));
						archivo.append(String.format("%-10s", transaccion.NemotecnicoCorredorVendedor).replace(' ', ' '));
						archivo.append(String.format("%-3s", transaccion.CodigoCorredorComprador).replace(' ', ' '));
						archivo.append(String.format("%-10s", transaccion.NemotecnicoCorredorComprador).replace(' ', ' '));
						archivo.append(String.format("%13s", onlyNumbers(transaccion.CantidadTransada)).replace(' ', '0'));
						archivo.append(String.format("%13s", onlyNumbers(transaccion.Precio)).replace(' ', '0'));
						archivo.append(String.format("%10s", transaccion.CondicionTransaccion).replace(' ', ' '));
						archivo.append(String.format("%-6s", transaccion.HoraTransaccion).replace(' ', ' '));
						archivo.append(String.format("%-10s", transaccion.NemotecnicoInstrumento).replace(' ', ' '));
						archivo.append(String.format("%-10s", transaccion.NemotecnicoNoInscritos).replace(' ', ' '));
						archivo.append(String.format("%15s", onlyNumbers(transaccion.TotalTransado)).replace(' ', '0'));
						archivo.append(String.format("%-3s", transaccion.OperadorInternoVenta).replace(' ', ' '));
						archivo.append(String.format("%-3s", transaccion.OperadorInternoCompra).replace(' ', ' '));
						archivo.append(String.format("%-1s", transaccion.IndicadorDCV).replace(' ', ' '));
						archivo.append(String.format("%-3s", transaccion.CodigoOperadorVendedor).replace(' ', ' '));
						archivo.append(String.format("%-3s", transaccion.CodigoOperadorComprador).replace(' ', ' '));
						archivo.append(String.format("%-8s", transaccion.Disponible).replace(' ', ' '));
						archivo.append("\n");
					}
				}
			} else {
				System.out.println("===========:: PARAMETRO OFF ::===========");
				for (TransaccionesProvida transaccion : transaccionesAFP) {
					archivo.append(String.format("%-8s", transaccion.FechaTransaccion).replace(' ', ' '));
					archivo.append(String.format("%-6s", transaccion.FolioTransaccion).replace(' ', ' '));
					archivo.append(String.format("%-3s", transaccion.CodigoCorredorVendedor).replace(' ', ' '));
					archivo.append(String.format("%-10s", transaccion.NemotecnicoCorredorVendedor).replace(' ', ' '));
					archivo.append(String.format("%-3s", transaccion.CodigoCorredorComprador).replace(' ', ' '));
					archivo.append(String.format("%-10s", transaccion.NemotecnicoCorredorComprador).replace(' ', ' '));
					archivo.append(String.format("%13s", onlyNumbers(transaccion.CantidadTransada)).replace(' ', '0'));
					archivo.append(String.format("%13s", onlyNumbers(transaccion.Precio)).replace(' ', '0'));
					archivo.append(String.format("%10s", transaccion.CondicionTransaccion).replace(' ', ' '));
					archivo.append(String.format("%-6s", transaccion.HoraTransaccion).replace(' ', ' '));
					archivo.append(String.format("%-10s", transaccion.NemotecnicoInstrumento).replace(' ', ' '));
					archivo.append(String.format("%-10s", transaccion.NemotecnicoNoInscritos).replace(' ', ' '));
					archivo.append(String.format("%15s", onlyNumbers(transaccion.TotalTransado)).replace(' ', '0'));
					archivo.append(String.format("%-3s", transaccion.OperadorInternoVenta).replace(' ', ' '));
					archivo.append(String.format("%-3s", transaccion.OperadorInternoCompra).replace(' ', ' '));
					archivo.append(String.format("%-1s", transaccion.IndicadorDCV).replace(' ', ' '));
					archivo.append(String.format("%-3s", transaccion.CodigoOperadorVendedor).replace(' ', ' '));
					archivo.append(String.format("%-3s", transaccion.CodigoOperadorComprador).replace(' ', ' '));
					archivo.append(String.format("%-8s", transaccion.Disponible).replace(' ', ' '));
					archivo.append("\n");
				}
			}
			archivo.setLength(archivo.length() - 1);
			System.out.println("==========================================================================================================================================");
			System.out.println(archivo.toString());
			System.out.println("==========================================================================================================================================");
			return archivo.toString();
		}
	}
}
