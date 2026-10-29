package cl.bcs.tesoreria.generarArchivoPago;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class GenerarArchivoPagos implements JavaCall {

	private final Log log = new Log("GenerarArchivoPagoBancoEstado");

	private final String SQL_SELECT_REGISTROS_ENCABEZADO = loadResourceToString("cl/bcs/tesoreria/generarArchivoPago/sql/GenerarArchivoPago_Select_Encabezado.sql");
	private final String SQL_SELECT_REGISTROS_DETALLE = loadResourceToString("cl/bcs/tesoreria/generarArchivoPago/sql/GenerarArchivoPago_Select_Detalle.sql");

	public static class EntradaGenerarArchivoPagos {
		public Long p_IdEmpresa;
		public String p_IdModulo;
		public String p_Fecha;
		public Long p_IdNominaTransferencia;
		public Archivo p_Archivo;

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class SalidaGenerarArchivoPagos {
		public String p_out_NombreArchivoLog;
		public String p_out_RutaArchivoLog;
		public Long p_out_ErroresContador;
		public String p_out_Comentario;
		public List<String> p_out_Archivos;

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class RegistrosEncabezado {
		public Integer TipoRegistroOptimus;
		public String TipoRegistro;
		public String Relleno;
		public Long NumeroNomina;
		public String RutOrdenate;
		public String NombreOrdenate;
		public String NumeroConvenio;
		public String CuentaCargo;
		public String MonedaNomina;
		public Date FechaOperacion;
		public String NombreUsuario;
		public String NombreBanco;
	}

	public static class RegistrosDetalle {
		public Integer TipoRegistroOptimus;
		public Long IdInterno;
		public String TipoRegistro;
		public String TipoDePago;
		public String RefDetallePago;
		public String RutBeneficiario;
		public String NombreBeneficiario;
		public BigDecimal MontoRemesa;
		public String CtaCteBeneficiario;
		public String CodigoIsoBancoBeneficiario;
		public String InformacionRemesa;
		public String CodigoDepDcvComprador;
		public String CodigoDepDcvVendedor;
		public String ClaveMedioPago;
	}

	public static class RegistroControl {
		public Integer TipoRegistroOptimus;
		public Long IdInterno;
		public String TipoRegistro;
		public BigDecimal TotalDetalles;
		public String Relleno1;
		public String Relleno2;
		public String Relleno3;
		public BigDecimal MontoNomina;
	}

	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();
		EntradaGenerarArchivoPagos in = gson.fromJson(jin.toString(),
				EntradaGenerarArchivoPagos.class);
		SalidaGenerarArchivoPagos out = logica(con, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();

		return dataout;
	}

	public SalidaGenerarArchivoPagos logica(Connection conn, EntradaGenerarArchivoPagos entrada)throws Exception {
		SalidaGenerarArchivoPagos out = new SalidaGenerarArchivoPagos();
		SQL sql = new SQL();
		SimpleDateFormat sdfyyMMdd = new SimpleDateFormat("dd-MM-yyyy");
		Date date = Fecha.toDate(entrada.p_Fecha);
		String fecha = sdfyyMMdd.format(date);
		HashMap<String, Object> param = new HashMap<String, Object>();
		param.put("IdNominaTransferencia", entrada.p_IdNominaTransferencia);
		List<RegistrosEncabezado> resultadoEncabezado = sql.query(conn, SQL_SELECT_REGISTROS_ENCABEZADO, param,
				RegistrosEncabezado.class);
		List<RegistrosDetalle> resultadoDetalle = sql.query(conn, SQL_SELECT_REGISTROS_DETALLE, param,
				RegistrosDetalle.class);

		if (!resultadoEncabezado.isEmpty() && !resultadoDetalle.isEmpty()) {
			entrada.p_Archivo.crear(); // Crea archivo

			String[] lineaCvs = new String[10];
			for (RegistrosEncabezado encabezado : resultadoEncabezado) {
				// escribir encabezados
				lineaCvs[4] = "Instruccion de Cargo en Cuenta Corriente/Pago";
				lineaCvs[9] = "VB CERTIFICADO";
				entrada.p_Archivo.escribir(lineaCvs);
				lineaCvs = new String[9];
				lineaCvs[4] = "y";
				entrada.p_Archivo.escribir(lineaCvs);
				lineaCvs = new String[9];
				lineaCvs[4] = "Transferencia de Fondos a Cuenta en Otro Banco T+0";
				entrada.p_Archivo.escribir(lineaCvs);
				lineaCvs = new String[2];
				lineaCvs[0] = " ";
				entrada.p_Archivo.escribir(lineaCvs);
				lineaCvs[1] = "Los abajo firmantes en representacion de " + encabezado.NombreOrdenate + " RUT N "
						+ encabezado.RutOrdenate + " instruimos al " + encabezado.NombreBanco
						+ ", transferir con fecha";
				entrada.p_Archivo.escribir(lineaCvs);
				lineaCvs = new String[10];
				lineaCvs[1] = fecha + " los fondos.";
				entrada.p_Archivo.escribir(lineaCvs);
				lineaCvs = new String[7];
				lineaCvs[1] = " ";
				entrada.p_Archivo.escribir(lineaCvs);
				entrada.p_Archivo.escribir(lineaCvs);
				lineaCvs = new String[8];
				lineaCvs[1] = "Registro de Encabezado";
				entrada.p_Archivo.escribir(lineaCvs);
				lineaCvs = new String[2];
				lineaCvs[1] = " ";
				entrada.p_Archivo.escribir(lineaCvs);
				lineaCvs = new String[10];
				lineaCvs[2] = "Numero de Nomina";
				lineaCvs[3] = "RUT Ordenante";
				lineaCvs[4] = "Nombre del Ordenante";
				lineaCvs[5] = "Numero de Convenio";
				lineaCvs[6] = "Cuenta de Cargo";
				lineaCvs[7] = "Moneda de la Nomina";
				lineaCvs[8] = "Fecha de Operacion";
				lineaCvs[9] = "Nombre de Usuario";
				entrada.p_Archivo.escribir(lineaCvs);
				lineaCvs = new String[2];
				lineaCvs[1] = " ";
				entrada.p_Archivo.escribir(lineaCvs);
				lineaCvs = new String[10];
				lineaCvs[0] = "1";
				lineaCvs[2] = encabezado.NumeroNomina.toString();
				lineaCvs[3] = encabezado.RutOrdenate;
				lineaCvs[4] = encabezado.NombreOrdenate;
				lineaCvs[5] = encabezado.NumeroConvenio;
				lineaCvs[6] = encabezado.CuentaCargo;
				lineaCvs[7] = encabezado.MonedaNomina;
				lineaCvs[8] = encabezado.FechaOperacion.toString();
				lineaCvs[9] = encabezado.NombreUsuario;
				entrada.p_Archivo.escribir(lineaCvs);
				lineaCvs = new String[2];
				lineaCvs[1] = " ";
				entrada.p_Archivo.escribir(lineaCvs);
			}

			lineaCvs = new String[2];
			lineaCvs[1] = "Registro de Detalle";
			entrada.p_Archivo.escribir(lineaCvs);
			lineaCvs = new String[2];
			lineaCvs[1] = " ";
			entrada.p_Archivo.escribir(lineaCvs);
			lineaCvs = new String[13];
			lineaCvs[1] = "Tipo De Pago";
			lineaCvs[2] = "Ref Detalle Pago";
			lineaCvs[3] = "RUT Beneficiario";
			lineaCvs[4] = "Nombre Beneficiario";
			lineaCvs[5] = "Monto Remesa";
			lineaCvs[6] = "Cta. Cte. Beneficiario";
			lineaCvs[7] = "Banco del Beneficiario";
			lineaCvs[8] = "Informacion Remesa";
			lineaCvs[9] = "Codigo Dep. Dcv. Comprador";
			lineaCvs[10] = "Codigo Dep. Dcv. Vendedor";
			lineaCvs[11] = "Clave Medio Pago";
			entrada.p_Archivo.escribir(lineaCvs);

			int totalDetalle = 0;
			BigDecimal montoNomina = BigDecimal.ZERO;
			String[] splitDocumentoIdentidad;
			Long sumatoriaRuts = 0L;
			for (RegistrosDetalle detalle : resultadoDetalle) {
				String montoRemesaString = (detalle!=null && detalle.MontoRemesa !=null)?detalle.MontoRemesa.toString():null;
				montoRemesaString = montoRemesaString!=null ? montoRemesaString.substring(0,montoRemesaString.length()-9):""; 
				
				lineaCvs = new String[13];
				lineaCvs[0] = "2";
				lineaCvs[1] = detalle.TipoDePago;
				lineaCvs[2] = detalle.RefDetallePago;
				lineaCvs[3] = detalle.RutBeneficiario;
				lineaCvs[4] = Normalizer.normalize(detalle.NombreBeneficiario, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
				//lineaCvs[5] = detalle.MontoRemesa.toString();
				lineaCvs[5] = montoRemesaString;
				lineaCvs[6] = detalle.CtaCteBeneficiario;
				lineaCvs[7] = detalle.CodigoIsoBancoBeneficiario;
				lineaCvs[8] = detalle.InformacionRemesa;
				lineaCvs[9] = detalle.CodigoDepDcvComprador;
				lineaCvs[10] = detalle.CodigoDepDcvVendedor;
				lineaCvs[11] = detalle.ClaveMedioPago;
				entrada.p_Archivo.escribir(lineaCvs);
				totalDetalle += 1;
				montoNomina = montoNomina.add(detalle.MontoRemesa);
				splitDocumentoIdentidad = detalle.RutBeneficiario.split("-");
				sumatoriaRuts += Long.parseLong(splitDocumentoIdentidad[0]);
			}
			
			String montoNominaString = (montoNomina!=null)?montoNomina.toString():null;
			montoNominaString = montoNominaString!=null ? montoNominaString.substring(0,montoNominaString.length()-9):""; 
			
			lineaCvs = new String[2];
			lineaCvs[1] = " ";
			entrada.p_Archivo.escribir(lineaCvs);
			lineaCvs = new String[2];
			lineaCvs[1] = "Registro de Control";
			entrada.p_Archivo.escribir(lineaCvs);
			lineaCvs = new String[2];
			lineaCvs[1] = " ";
			entrada.p_Archivo.escribir(lineaCvs);
			lineaCvs = new String[6];
			lineaCvs[1] = "Total Detalles";
			lineaCvs[5] = "Monto Nomina";
			entrada.p_Archivo.escribir(lineaCvs);
			lineaCvs = new String[2];
			lineaCvs[1] = " ";
			entrada.p_Archivo.escribir(lineaCvs);
			lineaCvs = new String[6];
			lineaCvs[0] = "3";
			lineaCvs[1] = totalDetalle + "";
			lineaCvs[3] = Long.toString(sumatoriaRuts);
			lineaCvs[5] = montoNominaString;
			entrada.p_Archivo.escribir(lineaCvs);
			lineaCvs = new String[2];
			lineaCvs[1] = " ";
			entrada.p_Archivo.escribir(lineaCvs);
			for (RegistrosEncabezado encabezado : resultadoEncabezado) {
				lineaCvs = new String[12];
				lineaCvs[1] = "Ademas solicitamos cargar la Cuenta Corriente N " + encabezado.CuentaCargo + " que "
						+ encabezado.NombreOrdenate + "mantiene en " + encabezado.NombreBanco;
				entrada.p_Archivo.escribir(lineaCvs);
				lineaCvs = new String[7];
				lineaCvs[1] = "Declaramos que los datos expresados en el formulario y la instruccion de cargo son de nuestra exclusiva responsabilidad. Liberamos al Banco del Estado de Chile";
				entrada.p_Archivo.escribir(lineaCvs);
				entrada.p_Archivo.escribir(" ");
				entrada.p_Archivo.escribir(" ");
				entrada.p_Archivo.escribir(" ");
				entrada.p_Archivo.escribir(" ");
				entrada.p_Archivo.escribir(" ");
				lineaCvs = new String[8];
				lineaCvs[1] = "Firma y Timbre Apoderado";
				lineaCvs[7] = "Firma y Timbre Apoderado";
				entrada.p_Archivo.escribir(lineaCvs);
			}
			entrada.p_Archivo.cerrar();
		}

		return out;
	}

	public static void main(String[] args) {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			EntradaGenerarArchivoPagos entrada = new EntradaGenerarArchivoPagos();
			entrada.p_IdEmpresa = new Long(1);
			entrada.p_IdModulo = "NOR";
			entrada.p_Fecha = "2019-05-22";
			JsonParser parser = new JsonParser();
			JsonObject dataJsonIn = parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
			GenerarArchivoPagos demo = new GenerarArchivoPagos();
			try {
				System.out.println(demo.call(con, dataJsonIn).toString());
				con.commit();
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
