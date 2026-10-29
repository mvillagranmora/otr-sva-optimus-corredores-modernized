package cl.bcs.cuentaInversion;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultaComparacionSaldo implements JavaCall{
	private final Log log = new Log("CTA.ConsultaComparacionSaldo");
	
	private final String SQL_SELECT_CONFIGURACION = loadResourceToString("cl/bcs/cuentaInversion/sql/ConsultaComparacionSaldo_SelectConfiguracion.sql");	
	private final String SQL_SELECT_SALDOS_CUENTAS = loadResourceToString("cl/bcs/cuentaInversion/sql/ConsultaComparacionSaldo_SelectSaldosCuentas.sql");
	private final String SQL_SELECT_SALDOS_MOVIMIENTOS = loadResourceToString("cl/bcs/cuentaInversion/sql/ConsultaComparacionSaldo_SelectSaldosMovimientos.sql");
	private final String SQL_INSERT_SALDO_CUENTA_PROCESO = loadResourceToString("cl/bcs/cuentaInversion/sql/ConsultaComparacionSaldo_InsertSaldoCuentaProceso.sql");
	private final String SQL_DELETE_SALDO_CUENTA_PROCESO = "DELETE FROM \"BSCL.CuentaInversion.BD::CTA.Saldo_Cuenta_Proceso\" WHERE 1=1";
	
	public static class ConsultaComparacionSaldoEntrada {
		public Long			p_IdEmpresa;
		public Long			p_IdUsuario;
		public String		p_IdModulo;
		public String 		p_FechaDesde;
		public String 		p_FechaHasta;
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	};
	
	public static class ConsultaComparacionSaldoSalida{
		public List<MensajeSalida>			P_OUT_MENSAJES;
		public Long							P_OUT_ERRORESCONTADOR;
		public String                       P_OUT_RUTAARCHIVO;
		
		public ConsultaComparacionSaldoSalida() {
			this.P_OUT_ERRORESCONTADOR=0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		};		
	};

	
	public static class saldosCtaInv{
		public Long IdCuentaInversion;
		public BigDecimal SaldoAnterior;
		public String FechaAnterior;		
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class saldosCtaInvMvto{
		public Long   IdCuentaInversion;
		public String IdMoneda;
		public Date   FechaEfectivaMovimiento;
		public String CodigoCuentaInversion;
		public String NombreCuentaInversion;
		public String CodigoNombreMoneda;
		public String DocumentoIdentidad;
		public String NombreCliente;
		public String CodigoPortafolio;
		public Long   IdCargoAbonoMovimiento;
		public String Proposito;
		public BigDecimal Saldo;
		public BigDecimal SaldoMovimiento;		
		public BigDecimal SaldoCuentaInversion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Configuracion {
		public String RutaBase;
		public String RutaCta;
		public String RutaArchivo;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
			
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		Gson gson = new Gson();		
		ConsultaComparacionSaldoEntrada jin=gson.fromJson(in.toString() ,ConsultaComparacionSaldoEntrada.class);
		ConsultaComparacionSaldoSalida out=logica(conn,jin);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;	
	}
	
	
	// Genera el nombre de un arhivo , con el formato:
	// <nombreArhivoSalida>_<fechaHora>.<extension>
	private String generarNombreArchivo(String nombre, String extension) {
		String nombreArchivo = "";
		
		try {
			  Date fechaActual = new Date();
			  DateFormat formatoFechaLarga = new SimpleDateFormat("yyyyMMdd_HHmmss");
			  String fechaHora = formatoFechaLarga.format(fechaActual);
			  nombreArchivo = nombre + "_" + fechaHora + "." + extension;
			} catch (Exception ex) {
				nombreArchivo = "";
			}

	  	return nombreArchivo;
	}
			
	
	public ConsultaComparacionSaldoSalida logica(Connection conn, ConsultaComparacionSaldoEntrada in) throws Exception {
		
		//logger.info("INICIO::ConsultaComparacionSaldo");
		
		ConsultaComparacionSaldoSalida out = new ConsultaComparacionSaldoSalida();
		SQL sql = new SQL();
		
		HashMap<String, Object> params = new HashMap<String, Object>();
		HashMap<String, String> codigo = new HashMap<String, String>();
		String rutaArchivo = "";
		String nombreArchivo = "";
		String rutaCta = "";
		String rutaCompleta = ""; // ruta + nombre archivo	
		log.write("ENTRADA > " + in);
	  
		// ------------------------------------------------------------------------
		// OBTIENE CONFIGURACION (nombre y ruta para guardar archivo)
		List<Configuracion> lstConfiguracion = new ArrayList<Configuracion>();
		params.clear();
		params.put("IdEmpresa", in.p_IdEmpresa);
		lstConfiguracion = sql.query(conn, SQL_SELECT_CONFIGURACION, params, Configuracion.class);

		if (!lstConfiguracion.isEmpty()) {
			for (Configuracion conf : lstConfiguracion) {
				rutaArchivo = conf.RutaArchivo != null ? conf.RutaArchivo : "";
				rutaCta     = conf.RutaCta != null ? conf.RutaCta : "";
			}
		}

		if (rutaArchivo.equalsIgnoreCase("") || rutaCta.equalsIgnoreCase("")){
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"FALTA_CONFIGURACION_RUTA_ARCHIVO",null,conn,in.p_IdEmpresa));
			out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES); 
			return out;
		}

		nombreArchivo = generarNombreArchivo("DiferenciaSaldos", "csv");		
		rutaCompleta = rutaArchivo + nombreArchivo; // Ruta completa donde generar el archivo			
		//rutaCompleta = "C:\\Users\\nsilva\\Desktop\\" + nombreArchivo; // >> Ruta para pruebas locales
		rutaCta       = rutaCta + nombreArchivo;     // Ruta Cta para devolver y desde donde leer el archivo

		log.write("NOMBRE ARCHIVO > " + nombreArchivo);
		log.write("RUTA COMPLETA > " + rutaCompleta);
		
		// ---------------------------------------------------------------------------------
		// SE CREA ARCHIVO CSV CON LA PRIMERA LINEA DE ENCABEZADO		
		File archivo = new File(rutaCompleta); // >> PARA SERVIDOR (cuando se libera debe ser con esta ruta)
		BufferedWriter bwArchivo;
		bwArchivo = new BufferedWriter(new FileWriter(archivo));
		String linea = "";
		
		// Linea para encabezado del archivo csv
		linea = linea.concat(Diccionario.getMensaje(conn, in.p_IdEmpresa, "UI_FECHA_EFECTIVA_MOVIMIENTO", codigo) + ";");
		linea = linea.concat(Diccionario.getMensaje(conn, in.p_IdEmpresa, "UI_CUENTA_INVERSION", codigo) + ";");
		linea = linea.concat(Diccionario.getMensaje(conn, in.p_IdEmpresa, "UI_DOCUMENTO_IDENTIDAD", codigo) + ";");
		linea = linea.concat(Diccionario.getMensaje(conn, in.p_IdEmpresa, "UI_NOMBRE_CLIENTE", codigo) + ";");
		linea = linea.concat(Diccionario.getMensaje(conn, in.p_IdEmpresa, "UI_PORTAFOLIO", codigo) + ";");
		linea = linea.concat(Diccionario.getMensaje(conn, in.p_IdEmpresa, "UI_PROPOSITO_PORTAFOLIO", codigo) + ";");
		//linea = linea.concat(Diccionario.getMensaje(conn, in.p_IdEmpresa, "UI_SALDO", codigo) + ";");
		linea = linea.concat(Diccionario.getMensaje(conn, in.p_IdEmpresa, "UI_SALDO_MOVIMIENTO", codigo) + ";");
		linea = linea.concat(Diccionario.getMensaje(conn, in.p_IdEmpresa, "UI_SALDO_CUENTA_INVERSION", codigo) + ";");
		bwArchivo.write(linea);
		bwArchivo.newLine();
		
		
		// ---------------------------------------------------------------------------------
		// SE CREA ARCHIVO CSV CON DATOS
		HashMap<String, Object> param = new HashMap<String, Object>(); 
		HashMap<String, Object> paramMvto = new HashMap<String, Object>(); 
		
		List<saldosCtaInv> listCtaInvSaldos = new ArrayList<saldosCtaInv>();			
		List<saldosCtaInvMvto> listCtaSaldosMvto = new ArrayList<saldosCtaInvMvto>();
		
		param.put("FechaOperacionDesde", in.p_FechaDesde);
		param.put("FechaOperacionHasta", in.p_FechaHasta);		
		listCtaInvSaldos = sql.query(conn, SQL_SELECT_SALDOS_CUENTAS, param, saldosCtaInv.class);	
		log.write("INSERT SALDOS CUENTAS");
		sql.batch(conn, SQL_INSERT_SALDO_CUENTA_PROCESO, listCtaInvSaldos, saldosCtaInv.class);
		
		paramMvto.put("FechaOperacionDesde", in.p_FechaDesde);
		listCtaSaldosMvto = sql.query(conn, SQL_SELECT_SALDOS_MOVIMIENTOS, paramMvto, saldosCtaInvMvto.class);

		if (listCtaSaldosMvto.size() == 0) {
			log.write("===:: ERROR - NO_SE_ENCONTRARON_DIFERENCIAS ::==");			
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_ENCONTRARON_DIFERENCIAS",null,conn,in.p_IdEmpresa));
		}
		
		for (saldosCtaInvMvto saldos : listCtaSaldosMvto) {
			linea = "";
			linea = linea.concat(saldos.FechaEfectivaMovimiento + ";");		
			linea = linea.concat(saldos.CodigoNombreMoneda + ";");
			linea = linea.concat(saldos.DocumentoIdentidad + ";");
			linea = linea.concat(saldos.NombreCliente + ";");
			linea = linea.concat(saldos.CodigoPortafolio + ";");
			linea = linea.concat(saldos.Proposito + ";");
			//linea = linea.concat(MonedaFormato.formatear(conn, saldos.Saldo, MonedaFormato.MONTO, saldos.IdMoneda).valor + ";");
			linea = linea.concat(MonedaFormato.formatear(conn, saldos.SaldoMovimiento, MonedaFormato.MONTO, saldos.IdMoneda).valor + ";");
			linea = linea.concat(MonedaFormato.formatear(conn, saldos.SaldoCuentaInversion, MonedaFormato.MONTO, saldos.IdMoneda).valor + "");
			
			bwArchivo.write(linea);
			bwArchivo.newLine();
		}
				
		bwArchivo.flush();
		bwArchivo.close();
		
		SQL.exec(conn, SQL_DELETE_SALDO_CUENTA_PROCESO);
	
		out.P_OUT_RUTAARCHIVO = rutaCta;  
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		log.write("SALIDA > " + out);
		log.write("== TERMINO VALIDAR SALDOS CUENTA INVERSION ==");
		return out;
	}
		
	public static void main(String[] args) {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getBOLSAConnection()) {
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"CTA\",\"p_FechaDesde\":\"2021-06-29\",\"p_FechaHasta\":\"1900-01-01\" }";
			ConsultaComparacionSaldoEntrada Objin = gson.fromJson(json, ConsultaComparacionSaldoEntrada.class);
			ConsultaComparacionSaldo demo = new ConsultaComparacionSaldo();
			System.out.println("ENTRADA");
			System.out.println(Objin.toString());
			try {
				ConsultaComparacionSaldoSalida ObjOut = demo.logica(con, Objin);
				con.rollback();
				System.out.println(ObjOut.toString());
				System.out.println("SALIDA");
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
			con.rollback();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}
}
