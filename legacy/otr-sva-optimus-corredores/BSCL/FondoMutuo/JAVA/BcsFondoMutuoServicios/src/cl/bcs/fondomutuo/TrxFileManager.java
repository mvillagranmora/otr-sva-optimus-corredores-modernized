package cl.bcs.fondomutuo;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import cl.bcs.plataforma.pla.utils.FileLoader;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class TrxFileManager {
	
	private final static Log log = new Log("TrxFileManager");
	
	private final FileLoader fileLoader = new FileLoader();
	private final String SQL_SELECT_DETALLE = fileLoader.loadResourceToString("cl/bcs/fondomutuo/sql/ValorCuotaFileManager_SelectDetalle.sql");
	
	private static final String FECHA_CERO = "00000000";
	
	public static class Maestro {
		public String IdCodigoTipo;
		public String Extension;
		public String Ip;
		public Integer Puerto;
		public String Carpeta;
		public String Usuario;
		public String Clave;
		public String Protocolo;
		public String ExpresionArchivo;
		public String Elimina;
		public String Java;
		public String EntradaSalida;
		public Time HoraDesde;
		public Time HoraHasta;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Cabecera {
		public Long IdCabecera;
		public String IdCodigoTipo;
		public String IdEstado;
		public java.sql.Timestamp FechaCreacion;
		public Long IdUsuario;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Detalle {
		public Long IdDetalle;
		public Long IdCabecera;
		public String Linea;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Id {
		public Long IdCabecera;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class TrxDetalle {
		public Long idTrx;//0-13
		public String rut;//14-24
		public String tipoMovimiento;//25-26
		public String idInstrumento;//27-35
		public String nombreFondoMutuo;//36-86
		public BigDecimal valor;//87-105
		public Integer tipoTransaccion;//106-107
		public String fechaTransaccion;//108-116
		public String monedaTransaccion;//117-120
		public String tipoCuenta;//121-123
		public Long idLote;//124-133
		public BigDecimal valorCuota;//134-152
		public BigDecimal numeroCuotasPesosEquivalentes;//153-171
		public String fechaLiquidez;//172-180
		public Long idAdministradora;//181-193
		public String flag;//194-195
		public String descripcion;//196-296
		public BigDecimal tipoCambio;//297-315
		public BigDecimal montoClp;//316-334
		public String fechaCuota;//335-343
		public String serieFondo;//344-354
		public Integer cantidadRegistros;
		public BigDecimal sumaCampoValor;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public List<TrxDetalle> processTrxFiles(Connection conn, Long idCabecera) {
		log.write("COMIENZA PROCESAMIENTO DETALLE DE ARCHIVO");
		SQL sql = new SQL();
		List<TrxDetalle> trxDetalleList = new ArrayList<TrxDetalle>();
		HashMap<String,Object> params = new HashMap<String,Object>();
		TrxDetalle trxDetalle;
		List<Detalle> detalleList = new ArrayList<Detalle>();
		
		try {
			params.put("IdCabecera", idCabecera);
			detalleList = sql.query(conn, SQL_SELECT_DETALLE, params, Detalle.class);
			
			for (Detalle detalle : detalleList) {
				trxDetalle = new TrxDetalle();
				List<String> lineaList = new ArrayList<String>(Arrays.asList(detalle.Linea.split(";")));
				if(null != lineaList && lineaList.size() > 20) {
					trxDetalle.idTrx = Long.valueOf(lineaList.get(0));
					trxDetalle.rut = lineaList.get(1).replaceFirst("^0*", "").trim();
					trxDetalle.tipoMovimiento = lineaList.get(2).trim();
					trxDetalle.idInstrumento = lineaList.get(3).replaceFirst("^0*", "").trim();
					trxDetalle.nombreFondoMutuo = lineaList.get(4).trim();
					trxDetalle.valor = new BigDecimal(getBigDecimalFromLine(lineaList, 5, 6));
					trxDetalle.tipoTransaccion = Integer.valueOf(lineaList.get(6));
					trxDetalle.fechaTransaccion = (!FECHA_CERO.equals(lineaList.get(7).trim())) ? lineaList.get(7).trim() : "";
					trxDetalle.monedaTransaccion = lineaList.get(8).trim();
					trxDetalle.tipoCuenta = lineaList.get(9).trim();
					trxDetalle.idLote = Long.valueOf(lineaList.get(10));
					trxDetalle.valorCuota = new BigDecimal(getBigDecimalFromLine(lineaList, 11, 9));
					trxDetalle.numeroCuotasPesosEquivalentes = new BigDecimal(getBigDecimalFromLine(lineaList, 12, 6));
					trxDetalle.fechaLiquidez = (!FECHA_CERO.equals(lineaList.get(13).trim())) ? lineaList.get(13).trim() : "";
					trxDetalle.idAdministradora = Long.valueOf(lineaList.get(14));
					trxDetalle.flag = lineaList.get(15).trim();
					trxDetalle.descripcion = lineaList.get(16).trim();
					trxDetalle.tipoCambio = new BigDecimal(getBigDecimalFromLine(lineaList, 17, 9));
					trxDetalle.montoClp = new BigDecimal(getBigDecimalFromLine(lineaList, 18, 6));
					trxDetalle.fechaCuota = (!FECHA_CERO.equals(lineaList.get(19).trim())) ? lineaList.get(19).trim() : "";
					trxDetalle.serieFondo = lineaList.get(20).trim();
					trxDetalleList.add(trxDetalle);
				} else if(null != lineaList && lineaList.size() > 2) {
					//Cabecera de los archivos TRX
					trxDetalle.cantidadRegistros = Integer.valueOf(lineaList.get(0));
					trxDetalle.sumaCampoValor = new BigDecimal(getBigDecimalFromLine(lineaList, 1, 6));
					trxDetalleList.add(trxDetalle);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}  catch (Exception e) {
			e.printStackTrace();
		}
		log.write("FINALIZA PROCESAMIENTO DETALLE DE ARCHIVO");
		return trxDetalleList;
	}
	
	private String getBigDecimalFromLine(List<String> lineaList, int position, int decimales) throws Exception {
		String value = "";
		value = lineaList.get(position);
		if(null != value && value.length() > decimales) {
			int endIntegers = value.length() - decimales;
			String integerPart = value.substring(0, endIntegers);
			String decimalPart = value.substring(endIntegers, value.length());
			integerPart = (null != integerPart) ? integerPart.replaceFirst ("^0*", "") : "";
			if(null != integerPart && !integerPart.isEmpty()) {
				value = integerPart + "." + decimalPart;
			} else {
				value = "0." + decimalPart;
			}
		} else {
			throw new Exception("Campo no tiene largo necesario!");
		}
		return value;
	}
	
	public static void main(String[] args) throws SQLException{
		log.write("INICIO TEST PROCESAR ARCHIVO TRX");
		try (Connection conn = PlataformaUtils.getDESAConnection()) {
			try {
				TrxFileManager demo = new TrxFileManager();
				List<TrxDetalle>  result = demo.processTrxFiles(conn, 0L);
				if(null != result && !result.isEmpty()) {
//					conn.commit();
					System.out.println("Exito!");
				} else {
					conn.rollback();
					System.out.println("Error...");
				}
			} catch (Exception e) {
				conn.rollback();
				System.out.println("Error...");
				e.printStackTrace();
			}
		}
		log.write("FIN TEST PROCESAR ARCHIVO TRX");
	}
}
