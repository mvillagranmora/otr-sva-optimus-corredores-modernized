package cl.bcs.tesoreria;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.InformacionDetalle;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.tesoreria.CrearComprobante.ComprobanteGenerar_Entrada;
import cl.bcs.tesoreria.CrearComprobante.CrearComprobante_Salida;
import cl.bcs.tesoreria.CrearComprobante.FormaDePago_Entrada;

import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class GirarComprobanteIngreso implements JavaCall {
	private final Logger log = LogManager.getLogger(RequisitosGirarComprobanteIngreso.class);
	private static final String CREATE_TEMPORARY_TABLE = "CREATE LOCAL TEMPORARY TABLE #RequisitosGirarComprobanteIngreso_p_ComprobantesAProcesar (\"IdComprobante\" BIGINT)";
	private static final String SQL_INSERT_REGISTROS = "INSERT INTO #RequisitosGirarComprobanteIngreso_p_ComprobantesAProcesar (\"IdComprobante\") VALUES (:IdComprobante)";
	private static final String SQL_DELETE_REGISTROS = "DELETE FROM #RequisitosGirarComprobanteIngreso_p_ComprobantesAProcesar WHERE 1=1";	
	
	private static final String SQL_INSERT_INGRESOEGRESO = "INSERT INTO \"BSCL.Tesoreria.BD::GBL.CrearComprobante_Entrada_IngresoEgresoGenerar\"  SELECT 0 as \"NumeroReferencia\",0 as \"IdIngresoEgreso\", '' as \"TipoComprobante\"  FROM DUMMY WHERE 1=2";
	
	private final String SQL_UPDATE_COMPROBANTES_DE_PAGO = loadResourceToString("cl/bcs/tesoreria/sql/GirarComprobanteIngreso_UpdateComprobantesFormaDePago.sql"); 
	private final String SQL_UPDATE_ESTADO = loadResourceToString("cl/bcs/tesoreria/sql/GirarComprobanteIngreso_UpdateComprobantesEstado.sql");
	
	private final String SQL_INSERT_FORMAPAGO_INGRESO = loadResourceToString("cl/bcs/tesoreria/sql/GirarComprobanteIngreso_InsertCrearComprobantesFormaDePago.sql");
	private final String SQL_INSERT_COMPROBANTES_AGRUPAR = loadResourceToString("cl/bcs/tesoreria/sql/GirarComprobanteIngreso_InsertCrearComprobante_Entrada_ComprobanteGenerar.sql");
	private final String SQL_SELECT_CUENTAS_BANCARIAS= loadResourceToString("cl/bcs/tesoreria/sql/GirarComprobanteIngreso_SelectConsultarCuentasBancarias.sql");
	 

	private static final Logger logger = LogManager.getLogger(GirarComprobanteIngreso.class);

	//CONSTANTES 
	private final String COMPROBANTE_DESDE_MOVIMIENTOS = "N";
	private final String COMPROBANTE_DESDE_COMPROBANTES = "S";
	private final String ES_INGRESO_AUTOMATICO = "S";
	//
	
	/***********************************************************************************************
	 * CLASES
	 ***********************************************************************************************/
	
	public static class EntradaGirarComprobanteIngreso {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_simularOperacion;
		public String p_UsaReglasNegocio;
		public String p_FechaMovimiento;
		public List<ComprobanteGenerar_Entrada> p_ComprobantesAGirar;
		public String p_GeneraNomina;
		public String p_GeneraTransferencia;
		public String p_IdMedioDePago;
		public Long p_IdCuentaBancariaCorredora;
		public List<FormaDePago_Entrada> p_FormasDePago;
		public Long p_monto;
		public String p_usaTalonarioCheque;
		public String p_IdModalidadDePago;
		public String p_IdMoneda;
		public Long p_IdCaja;


		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaGirarComprobanteIngreso {

		public int p_out_advertenciascontador;
		public List<CrearComprobante_Salida> P_OUT_COMPROBANTESCREADOS;

		public List<InformacionDetalle> P_OUT_ERRORESDETALLE;
		public Long					P_OUT_ERRORESCONTADOR;
		public List<Long>			P_OUT_SECUENCIA;
		private List<MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaGirarComprobanteIngreso() {
			this.P_OUT_ERRORESDETALLE = new ArrayList<InformacionDetalle>();
			this.P_OUT_COMPROBANTESCREADOS = new ArrayList<CrearComprobante_Salida>();
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_SECUENCIA = new ArrayList<Long>();
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		public String toString() {
			return Utils.printJson(this, false);
		}

	}
	
	public static class CuentasBancarias{

		public String IdTipoCuentaBancaria;
		public String NombreTipoCuentaBancaria;
		public String NombreCuentaBancaria;
		public String NumeroCuentaBancaria;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class Comprobantes{

		public Long IdComprobante;
		public String IdEstado;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class ParametrosUpdateCuentasBancarias{
		public Long  IdComprobante;   
		public String IdTipoCuentaBancaria;
		public Long IdCuentaBancaria;
		public String NombreTipoCuentaBancaria;
		public String NombreCuentaBancaria;
		public String NumeroCuentaBancaria;
		public String IdMedioDePago;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	



	/***********************************************************************************************
	 * CALL
	 ***********************************************************************************************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaGirarComprobanteIngreso in = gson.fromJson(jin.toString(),
				EntradaGirarComprobanteIngreso.class);
		SalidaGirarComprobanteIngreso out = logica(con, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();

		return dataout;
	}

	/***********************************************************************************************
	 * LOGICA
	 ***********************************************************************************************/
	@Trace
	public SalidaGirarComprobanteIngreso logica(Connection con,	EntradaGirarComprobanteIngreso entrada) throws Exception {

		SQL sql = new SQL();
		SalidaGirarComprobanteIngreso salida = new SalidaGirarComprobanteIngreso();
		logger.info("Inicio servicio ");
		try {
			
			sql.batch(con, SQL_INSERT_COMPROBANTES_AGRUPAR, entrada.p_ComprobantesAGirar, ComprobanteGenerar_Entrada.class);
			sql.batch(con, SQL_INSERT_INGRESOEGRESO, entrada.p_ComprobantesAGirar, ComprobanteGenerar_Entrada.class);
			
			SQL.exec(con, CREATE_TEMPORARY_TABLE);
			sql.batch(con, SQL_INSERT_REGISTROS, entrada.p_ComprobantesAGirar, ComprobanteGenerar_Entrada.class);
			
			FormaDePago_Entrada formaPagoComprobanteIngresoAgrupado = new FormaDePago_Entrada();
			formaPagoComprobanteIngresoAgrupado.IdMedioDePago = entrada.p_IdMedioDePago;
			formaPagoComprobanteIngresoAgrupado.IdModalidadDePago = entrada.p_IdModalidadDePago;
			formaPagoComprobanteIngresoAgrupado.IdMoneda = entrada.p_IdMoneda;

			formaPagoComprobanteIngresoAgrupado.Monto = new BigDecimal(entrada.p_monto);

			formaPagoComprobanteIngresoAgrupado.IdCuentaBancariaCorredora = entrada.p_IdCuentaBancariaCorredora;
			entrada.p_FormasDePago.add(formaPagoComprobanteIngresoAgrupado);
			sql.batch(con, SQL_INSERT_FORMAPAGO_INGRESO,entrada.p_FormasDePago  , FormaDePago_Entrada.class);	
			HashMap<String,Object> params = new HashMap<String, Object>();
			params.put("p_IdCuentaBancariaCorredora", entrada.p_IdCuentaBancariaCorredora);
			List<CuentasBancarias> cuentasBancarias = sql.query(con, SQL_SELECT_CUENTAS_BANCARIAS, params, CuentasBancarias.class);
			if(!cuentasBancarias.isEmpty()){
				logger.debug(cuentasBancarias);
				List<ParametrosUpdateCuentasBancarias> listParans = cuentasBancarias.stream().map( temp -> {
					ParametrosUpdateCuentasBancarias obj = new ParametrosUpdateCuentasBancarias();
					obj.IdCuentaBancaria = entrada.p_IdCuentaBancariaCorredora;
					obj.IdMedioDePago = entrada.p_IdMedioDePago;
					obj.IdTipoCuentaBancaria = temp.IdTipoCuentaBancaria;
					obj.NombreCuentaBancaria = temp.NombreCuentaBancaria;
					obj.NombreTipoCuentaBancaria = temp.NombreTipoCuentaBancaria;
					obj.NumeroCuentaBancaria = temp.NumeroCuentaBancaria;
					return obj;
				}).collect(Collectors.toList());
				sql.batch(con, SQL_UPDATE_COMPROBANTES_DE_PAGO, listParans, ParametrosUpdateCuentasBancarias.class);	
			}
			
			CrearComprobante ServiceCrearComprobante = new CrearComprobante();
			CrearComprobante.CrearComprobante_Entrada EntradaCrearComprobante = new CrearComprobante.CrearComprobante_Entrada();
			EntradaCrearComprobante.p_IdEmpresa = entrada.p_IdEmpresa;
			EntradaCrearComprobante.p_IdUsuario = entrada.p_IdUsuario;
			EntradaCrearComprobante.p_IdModulo = entrada.p_IdModulo;
			EntradaCrearComprobante.p_SimularOperacion = entrada.p_simularOperacion;
			EntradaCrearComprobante.p_UsaReglasNegocio = "N";//entrada.p_UsaReglasNegocio;
			EntradaCrearComprobante.p_FechaMovimiento = entrada.p_FechaMovimiento;
			EntradaCrearComprobante.p_IdCaja = entrada.p_IdCaja;	
			EntradaCrearComprobante.p_GenerarComprobanteDesdeMovimientos = COMPROBANTE_DESDE_MOVIMIENTOS;
			EntradaCrearComprobante.p_GenerarComprobanteDesdeComprobantes = COMPROBANTE_DESDE_COMPROBANTES;
			EntradaCrearComprobante.p_EsIngresoAutomatico = ES_INGRESO_AUTOMATICO;
			EntradaCrearComprobante.p_ComprobanteGenerar = entrada.p_ComprobantesAGirar;
			EntradaCrearComprobante.p_FormaDePago = entrada.p_FormasDePago;
			CrearComprobante_Salida salidaCrearComprobante = ServiceCrearComprobante.logica(con, EntradaCrearComprobante);
			


			List<Comprobantes> update = new ArrayList<Comprobantes>();
			for(ComprobanteGenerar_Entrada Comprobantes :entrada.p_ComprobantesAGirar) {
				Comprobantes ingreso = new Comprobantes();
				ingreso.IdComprobante = Comprobantes.IdComprobante;
				update.add(ingreso);
			}
			sql.batch(con, SQL_UPDATE_ESTADO, update, Comprobantes.class);
			
			
			SQL.exec(con, SQL_DELETE_REGISTROS);
			SQL.exec(con, "DROP TABLE #RequisitosGirarComprobanteIngreso_p_ComprobantesAProcesar");
			
			if (MensajeSalida.getNumeroErrores(salidaCrearComprobante.P_OUT_MENSAJES) > 0) {
				logger.error(salidaCrearComprobante.P_OUT_MENSAJES);

				salida.P_OUT_MENSAJES = salidaCrearComprobante.P_OUT_MENSAJES;
				return salida;
			} else {
				salida.P_OUT_MENSAJES.addAll(salidaCrearComprobante.P_OUT_MENSAJES);
			}

		} catch (Exception er) { 
			er.printStackTrace();
		}		

		return salida;
	}

	/***********************************************************************************************
	 * MAIN
	 ***********************************************************************************************/
	public static void main(String[] args) throws Exception { 
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
					.serializeNulls().create();

			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":6,\"p_IdModulo\":\"TES\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_IdCaja\":1,\"p_FechaMovimiento\":\"2020-02-25\",\"p_ComprobantesAGirar\":[{\"IdComprobante\":5553}],\"p_IdMedioDePago\":\"TRA\",\"p_IdCuentaBancariaCorredora\":109,\"p_FormasDePago\":[],\"p_monto\":4450,\"p_usaTalonarioCheque\":\"N\",\"p_IdModalidadDePago\":\"MODDEP\",\"p_IdMoneda\":\"CLP\",\"p_GeneraNomina\":\"N\",\"p_GeneraTransferencia\":\"N\",\"p_GenerarComprobanteDesdeMovimientos\":\"N\",\"p_GenerarComprobanteDesdeComprobantes\":\"S\",\"p_EsIngresoAutomatico\":\"S\"}";
		
			EntradaGirarComprobanteIngreso Objin = gson.fromJson(json,
					EntradaGirarComprobanteIngreso.class);

			GirarComprobanteIngreso demo = new GirarComprobanteIngreso();

			try {
				SalidaGirarComprobanteIngreso ObjOut = demo.logica(con, Objin);
				System.out.println(ObjOut);
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
		}
		System.out.println("== END ==");
	}
	
}