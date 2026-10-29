package cl.bcs.normativa.jobs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.math.RoundingMode;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;

import com.google.gson.JsonObject;
import com.opencsv.CSVWriter;

import cl.bcs.normativa.ils.ConsultarSaldosCuentasContables;
import cl.bcs.normativa.ils.ConsultarSaldosCuentasContables.ConsultarSaldosCuentasContables_Entrada;
import cl.bcs.normativa.ils.ConsultarSaldosCuentasContables.ConsultarSaldosCuentasContables_Salida;
import cl.bcs.normativa.ils.pojo.CuentaContable;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;

public class GenerarReporteSaldosCuentasContables extends BcsJob implements JavaCall{

	private final Log log = new Log("MES.GenerarReporteSaldosCuentasContables");
	AtomicBoolean running = new AtomicBoolean(false);

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		return null;
	}

	@Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
		return logica(con, false);
	}

	@Override
	public void onError(Throwable t) {
		log.write(":GenerarReporteSaldosCuentasContables:  ****** ERROR  *******");
		log.write(t);
		t.printStackTrace();
	}

	@Override
	public void pause() {
		
	}

	public boolean logica(Connection con, boolean debug) throws Throwable {
		running.set(true);
		
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
		
		ConsultarSaldosCuentasContables_Entrada entrada = new ConsultarSaldosCuentasContables_Entrada();
		entrada.p_IdEmpresa = debug ? 1L    : Long.parseLong(this.getParam("IdEmpresa"));
		entrada.p_IdUsuario = debug ? 288L  : Long.parseLong(this.getParam("IdUsuario"));
		entrada.p_IdModulo  = debug ? "ILS" : String.valueOf((this.getParam("IdModulo")));
		
		String FechaContable   = debug ? "2023-02-21" : String.valueOf((this.getParam("FechaContable")));
		String NombreReporte   = debug ? String.format("Saldos Cuentas Contables %s.xls", FechaContable) : String.valueOf((this.getParam("NombreArchivo")));
		
		ConsultarSaldosCuentasContables consultarSaldosCuentasContables = new ConsultarSaldosCuentasContables();
		ConsultarSaldosCuentasContables_Salida salida = consultarSaldosCuentasContables.logica(con, entrada);
		
		String ruta = debug ? "C:\\__ARCHIVO_PRUEBA\\" : String.valueOf(this.getParam("RutaArchivo"));
		
		if (salida.P_OUT_ERRORESCONTADOR == 0) {
			log.write("Comprobar la ruta");
			if (!new File(ruta).exists()) {
				File theDir = new File(ruta);
				theDir.mkdir();
			}

			log.write("Crear file salida");
			File file = new File(ruta.concat(NombreReporte));

			log.write("Inicio Proceso");
			try (FileWriter fw = new FileWriter(file);
					CSVWriter cw = new CSVWriter(fw, ';');
					HSSFWorkbook wb = new HSSFWorkbook();
					FileOutputStream outputStream = new FileOutputStream(ruta.concat(NombreReporte))) {

				log.write("Creacion wb salida");
				HSSFSheet sheet = wb.createSheet(NombreReporte);
				
				log.write("Ordenar cuentas");
				List<CuentaContable> cuentasContables = salida.P_OUT_SALDOSCUENTASCONTABLES.stream()
						.sorted((c1, c2) ->  c1.IdCodigoContabilidad.compareTo(c2.IdCodigoContabilidad))
						.collect(Collectors.toList());
				
				log.write("Armando tabla xls");
				this.escribirReporteSaldosContables(cuentasContables, sheet, sdf);

				log.write("Escribiendo xls");
				wb.write(outputStream);
			} catch (Exception ex) {
				System.out.println("ERROR AL GENERAR ARCHIVO");
				ex.printStackTrace();
				throw new Throwable(ex);
			}
		}

		running.set(false);
		return false;
	}
	
	private void escribirReporteSaldosContables(List<CuentaContable> cuentasContables, HSSFSheet sheet, SimpleDateFormat sdf) {
		int contadorFila = 1;
		
		Row row = sheet.createRow(0);
		row.createCell(0).setCellValue("Cuenta Contable");
		row.createCell(1).setCellValue("Nombre Cuenta");
		row.createCell(2).setCellValue("Imputable");
		row.createCell(3).setCellValue("Nivel");
		row.createCell(4).setCellValue("Debe");
		row.createCell(5).setCellValue("haber");
		row.createCell(6).setCellValue("Saldo");
		
		for(CuentaContable cc : cuentasContables) {
			row = sheet.createRow(contadorFila++);
			row.createCell(0).setCellValue(cc.IdCodigoContabilidad);
			row.createCell(1).setCellValue(cc.NombreCuenta);
			row.createCell(2).setCellValue(cc.Imputable);
			row.createCell(3).setCellValue(cc.Nivel);
			row.createCell(4).setCellValue(cc.MontoDebe.setScale(0, RoundingMode.HALF_UP).longValue());
			row.createCell(5).setCellValue(cc.MontoHaber.setScale(0, RoundingMode.HALF_UP).longValue());
			row.createCell(6).setCellValue(cc.Saldo.setScale(0, RoundingMode.HALF_UP).longValue());
		}
		
		log.write("Ancho Columnas");
		sheet.setColumnWidth(0, 20 * 256);
		sheet.setColumnWidth(1, 63 * 256);
		sheet.setColumnWidth(2, 10 * 256);
		sheet.setColumnWidth(3, 10 * 256);
		sheet.setColumnWidth(4, 20 * 256);
		sheet.setColumnWidth(5, 20 * 256);
		sheet.setColumnWidth(6, 20 * 256);
	}

	public static void main(String[] args) {
		System.out.println("== INICIO PRUEBA ==");
		try {
			Connection con = PlataformaUtils.getQAConnection();
			GenerarReporteSaldosCuentasContables demo = new GenerarReporteSaldosCuentasContables();
			demo.logica(con, true);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		System.out.println("== FIN PRUEBA ==");
	}
	
}