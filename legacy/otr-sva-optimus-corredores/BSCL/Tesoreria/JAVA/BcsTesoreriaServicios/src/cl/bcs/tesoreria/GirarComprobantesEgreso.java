package cl.bcs.tesoreria;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;

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
import cl.bcs.tesoreria.CrearComprobante.ResultadoProceso_Salida;
import cl.bcs.tesoreria.GirarComprobanteIngreso.Comprobantes;
import cl.bcs.tesoreria.GirarComprobanteIngreso.CuentasBancarias;
import cl.bcs.tesoreria.GirarComprobanteIngreso.ParametrosUpdateCuentasBancarias;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class GirarComprobantesEgreso implements JavaCall {
	private final Logger log = LogManager.getLogger(GirarComprobantesEgreso.class);
	private static final String CREATE_TEMPORARY_TABLE = "CREATE LOCAL TEMPORARY TABLE #RequisitosGirarComprobanteEgreso_p_ComprobantesAProcesar (\"IdComprobante\" BIGINT)";
	private static final String SQL_INSERT_REGISTROS = "INSERT INTO #RequisitosGirarComprobanteEgreso_p_ComprobantesAProcesar (\"IdComprobante\") VALUES (:IdComprobante)";
	private static final String SQL_DELETE_REGISTROS = "DELETE FROM #RequisitosGirarComprobanteEgreso_p_ComprobantesAProcesar WHERE 1=1";	
	
	private final String SQL_INSERT_COMPROBANTES_AGRUPAR = loadResourceToString("cl/bcs/tesoreria/sql/GirarComprobanteEgreso_InsertCrearComprobante_Entrada_ComprobanteGenerar.sql");
	private static final String SQL_INSERT_INGRESOEGRESO = "INSERT INTO \"BSCL.Tesoreria.BD::GBL.CrearComprobante_Entrada_IngresoEgresoGenerar\"  SELECT 0 as \"NumeroReferencia\",0 as \"IdIngresoEgreso\", '' as \"TipoComprobante\"  FROM DUMMY WHERE 1=2";
	private final String SQL_INSERT_FORMAPAGO_EGRESO = loadResourceToString("cl/bcs/tesoreria/sql/GirarComprobanteIngreso_InsertCrearComprobantesFormaDePago.sql");

	private final String SQL_SELECT_CUENTAS_BANCARIAS= loadResourceToString("cl/bcs/tesoreria/sql/GirarComprobanteIngreso_SelectConsultarCuentasBancarias.sql");
	private final String SQL_UPDATE_COMPROBANTES_DE_PAGO = loadResourceToString("cl/bcs/tesoreria/sql/GirarComprobanteIngreso_UpdateComprobantesFormaDePago.sql"); 
	//private final String SQL_UPDATE_COMPROBANTES_DE_PAGO_SOCIO = loadResourceToString("cl/bcs/tesoreria/sql/GirarComprobanteIngreso_UpdateComprobantesFormaDePagoSocio.sql"); 
	private final String SQL_UPDATE_FORMASPAGO_PAREGR = loadResourceToString("cl/bcs/tesoreria/sql/GirarComprobanteEgreso_UpdateFormaPagoPAREGR.sql");
	private final String SQL_SELECT_NEXTIDNOMINA = "SELECT \"BSCL.Tesoreria.BD.Secuencias::NominaTransferencia\".NEXTVAL as \"v_IdNominaTransferencia\" from dummy;";
	private final String SQL_INSERT_GENERANOMINA = loadResourceToString("cl/bcs/tesoreria/sql/GirarComprobanteEgreso_InsertGeneraNomina.sql");
	private final String SQL_INSERT_GENERANOMINADETALLE = loadResourceToString("cl/bcs/tesoreria/sql/GirarComprobanteEgreso_InsertGeneraNominaDetalle.sql");
	private final String SQL_INSERT_GENERATRANSFERENCIA = loadResourceToString("cl/bcs/tesoreria/sql/GirarComprobanteEgreso_InsertGeneraTransferencia.sql");
	private final String SQL_INSERT_GENERATRANSFERENCIADETALLE = loadResourceToString("cl/bcs/tesoreria/sql/GirarComprobanteEgreso_InsertGeneraNominaDetalle.sql");

	private final String SQL_DELETE_Comprobante = "DELETE FROM \"BSCL.Tesoreria.BD::GBL.CrearComprobante_Entrada_ComprobanteGenerar\";";

	
    //-------------------------------------------------------------------------------------------
	// CLASES UTILITARIAS QUE REPRESENTAN OBJETOS IN
	//-------------------------------------------------------------------------------------------
	public static class EntradaGirarComprobantesEgreso {
		// Parametros de validacion.
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_simularOperacion;
		public String p_UsaReglasNegocio;
		
		// Parámetros de negocio.
		public Long p_IdCaja;
		public String p_FechaMovimiento;
		public String p_IdMedioDePago;
		public Long p_IdCuentaBancariaCorredora;
		public String p_IdModalidadDePago;
		public String p_IdMoneda;
		public Long p_monto;
		public String p_UsaTalonarioCheque;
		public String p_GeneraNomina;
		public String p_GeneraTransferencia;
		public List<ComprobanteGenerar_Entrada> p_ComprobantesAGirar;
		public List<FormaDePago_Entrada> p_FormasDePago;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
   
	//-------------------------------------------------------------------------------------------
	// CLASES UTILITARIAS QUE REPRESENTAN OBJETOS OUT
	//-------------------------------------------------------------------------------------------
	public static class SalidaGirarComprobantesEgreso {

		public int p_out_advertenciascontador;
		public List<CrearComprobante_Salida> P_OUT_COMPROBANTESCREADOS;

		public List<InformacionDetalle> P_OUT_ERRORESDETALLE;
		public Long					P_OUT_ERRORESCONTADOR;
		public List<Long>			P_OUT_SECUENCIA;
		private List<MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaGirarComprobantesEgreso() {
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
	public static class Comprobantes{

		public Long IdComprobante;
		public String IdEstado;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class ParametrosUpdateCuentasBancariasSocio{
		
		public List<FormaDePago_Entrada> p_FormasDePago;  
		public String p_IdMedioDePago;
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class ParametrosUpdateFormaPago{
		
		public Long IdUsuario;
		public Long IdPAREGR;  
		public String IdMedioDePago;
		public String FechaGiro;
		public List<ComprobanteGenerar_Entrada> p_ComprobantesAGirar;

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class ParametroSelectIdNomina{
		public Long v_IdNominaTransferencia;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class ParametrosGeneraNomina{
			
			public Long v_IdNominaTransferencia;
			public Long p_IdEmpresa;  
			public Long v_IdComprobantePartidaEgreso;  

			public String p_FechaMovimiento;
			public String p_out_FechaLiquidacion;
			public String p_IdMedioDePago;
			public String p_IdModalidadDePago;
			public Long p_IdCuentaBancariaCorredora;
			public String v_IdTipoCuentaBancariaCorredora;
			public String p_out_IdMoneda;
			public Long p_Monto;
			public Long p_IdUsuario;
			public String p_UsaTalonarioCheque;

			@Override
			public String toString() {
				return Utils.printJson(this, false);
			}
		}
	public static class ParametrosGeneraNominaDetalle{
		
		public Long v_IdNominaTransferencia;
		public Long p_IdUsuario;  
		public String p_IdMedioDePago;  
		public List<ComprobanteGenerar_Entrada> p_ComprobantesAGirar;

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	//CONSTANTES 
	private final String COMPROBANTE_DESDE_MOVIMIENTOS = "N";
	private final String COMPROBANTE_DESDE_COMPROBANTES = "S";
	private final String ES_INGRESO_AUTOMATICO = "S";
	//-------------------------------------------------------------------------------------------
	// CALL
	//-------------------------------------------------------------------------------------------
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaGirarComprobantesEgreso in = gson.fromJson(jin.toString(),
				EntradaGirarComprobantesEgreso.class);
		SalidaGirarComprobantesEgreso out = logica(con, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();

		return dataout;
	
	}
	@Trace
	public SalidaGirarComprobantesEgreso logica(Connection con,	EntradaGirarComprobantesEgreso in) throws Exception {
		SQL sql = new SQL();
		SalidaGirarComprobantesEgreso salida = new SalidaGirarComprobantesEgreso();
		log.info("Inicio servicio ");
		try {
			//  LLENA TABLAS GLOBALES
			sql.batch(con, SQL_INSERT_COMPROBANTES_AGRUPAR, in.p_ComprobantesAGirar, ComprobanteGenerar_Entrada.class);
			sql.batch(con, SQL_INSERT_INGRESOEGRESO, in.p_ComprobantesAGirar, ComprobanteGenerar_Entrada.class);
			SQL.exec(con, CREATE_TEMPORARY_TABLE);
			sql.batch(con, SQL_INSERT_REGISTROS, in.p_ComprobantesAGirar, ComprobanteGenerar_Entrada.class);
			FormaDePago_Entrada formaPagoComprobanteIngresoAgrupado = new FormaDePago_Entrada();
			formaPagoComprobanteIngresoAgrupado.IdMedioDePago = in.p_IdMedioDePago;
			formaPagoComprobanteIngresoAgrupado.IdModalidadDePago = in.p_IdModalidadDePago;
			formaPagoComprobanteIngresoAgrupado.IdMoneda = in.p_IdMoneda;
			formaPagoComprobanteIngresoAgrupado.Monto = new BigDecimal(in.p_monto);
			formaPagoComprobanteIngresoAgrupado.IdCuentaBancariaCorredora = in.p_IdCuentaBancariaCorredora;
			in.p_FormasDePago.add(formaPagoComprobanteIngresoAgrupado);
			sql.batch(con, SQL_INSERT_FORMAPAGO_EGRESO,in.p_FormasDePago  , FormaDePago_Entrada.class);	
			

			// ACTUALIZA LAS FORMAS DE PAGO DEL COMPROBANTE
			// Obtiene los datos de la cuenta bancaria de la corredora con la cual se va a pagar
			HashMap<String,Object> params = new HashMap<String, Object>();
			params.put("p_IdCuentaBancariaCorredora", in.p_IdCuentaBancariaCorredora);
			List<CuentasBancarias> cuentasBancarias = sql.query(con, SQL_SELECT_CUENTAS_BANCARIAS, params, CuentasBancarias.class);
			// Modifica los datos de la corredora para las formas de pago giradas(actualiza todos los registros)		
			if(!cuentasBancarias.isEmpty()){
				//logger.debug(cuentasBancarias);
				List<ParametrosUpdateCuentasBancarias> listParans = cuentasBancarias.stream().map( temp -> {
					ParametrosUpdateCuentasBancarias obj = new ParametrosUpdateCuentasBancarias();
					obj.IdCuentaBancaria = in.p_IdCuentaBancariaCorredora;
					obj.IdMedioDePago = in.p_IdMedioDePago;
					obj.IdTipoCuentaBancaria = temp.IdTipoCuentaBancaria;
					obj.NombreCuentaBancaria = temp.NombreCuentaBancaria;
					obj.NombreTipoCuentaBancaria = temp.NombreTipoCuentaBancaria;
					obj.NumeroCuentaBancaria = temp.NumeroCuentaBancaria;
					return obj;
				}).collect(Collectors.toList());
				sql.batch(con, SQL_UPDATE_COMPROBANTES_DE_PAGO, listParans, ParametrosUpdateCuentasBancarias.class);	
			}
			// Modifica los datos del socio para las formas de pago giradas(actualiza sólo registros editados)
//			List<ParametrosUpdateCuentasBancariasSocio> updateFormaPagoSocio = new ArrayList<ParametrosUpdateCuentasBancariasSocio>();
//			ParametrosUpdateCuentasBancariasSocio obj = new ParametrosUpdateCuentasBancariasSocio();
//			obj.p_FormasDePago = in.p_FormasDePago;
//			obj.p_IdMedioDePago = in.p_IdMedioDePago;
//			updateFormaPagoSocio.add(obj);
//			sql.batch(con, SQL_UPDATE_COMPROBANTES_DE_PAGO_SOCIO, updateFormaPagoSocio, ParametrosUpdateCuentasBancariasSocio.class);
			
			// CREA PAREGR
			CrearComprobante ServiceCrearComprobante = new CrearComprobante();
			CrearComprobante.CrearComprobante_Entrada EntradaCrearComprobante = new CrearComprobante.CrearComprobante_Entrada();
			EntradaCrearComprobante.p_IdEmpresa = in.p_IdEmpresa;
			EntradaCrearComprobante.p_IdUsuario = in.p_IdUsuario;
			EntradaCrearComprobante.p_IdModulo = in.p_IdModulo;
			EntradaCrearComprobante.p_SimularOperacion = in.p_simularOperacion;
			EntradaCrearComprobante.p_UsaReglasNegocio = "N";//entrada.p_UsaReglasNegocio;
			EntradaCrearComprobante.p_FechaMovimiento = in.p_FechaMovimiento;
			EntradaCrearComprobante.p_IdCaja = in.p_IdCaja;	
			EntradaCrearComprobante.p_GenerarComprobanteDesdeMovimientos = COMPROBANTE_DESDE_MOVIMIENTOS;
			EntradaCrearComprobante.p_GenerarComprobanteDesdeComprobantes = COMPROBANTE_DESDE_COMPROBANTES;
			EntradaCrearComprobante.p_EsIngresoAutomatico = ES_INGRESO_AUTOMATICO;
			EntradaCrearComprobante.p_ComprobanteGenerar = in.p_ComprobantesAGirar;
			EntradaCrearComprobante.p_FormaDePago = in.p_FormasDePago;
			CrearComprobante_Salida salidaCrearComprobante = ServiceCrearComprobante.logica(con, EntradaCrearComprobante);
			// Obtiene idComprobante PAREGR.
			long IdPAREGR = 0L;

			List<ResultadoProceso_Salida>  salidaCrearComprobanteResultado = salidaCrearComprobante.P_OUT_RESULTADOPROCESO;
			// Encontrar el primer objeto con 'Resultado' igual a 'ING'
			Optional<ResultadoProceso_Salida> resultadoIng = salidaCrearComprobanteResultado.stream()
			        .filter(objeto -> "ING".equals(objeto.Resultado))
			        .findFirst();
			// Verificar si se encontro el objeto
			if (resultadoIng.isPresent()) {
			    // Obtener el valor de la propiedad "Id"
				ResultadoProceso_Salida objetoIng = resultadoIng.get();
				IdPAREGR = objetoIng.Id; 
			}
			sql.batch(con, SQL_INSERT_COMPROBANTES_AGRUPAR, in.p_ComprobantesAGirar, ComprobanteGenerar_Entrada.class);

			// Cambia estado Formas de Pago
			List<ParametrosUpdateFormaPago> ListFormaPago = new ArrayList<ParametrosUpdateFormaPago>();
			ParametrosUpdateFormaPago paramsFormaPago = new ParametrosUpdateFormaPago();
			paramsFormaPago.IdUsuario = in.p_IdUsuario;
			paramsFormaPago.IdPAREGR = IdPAREGR;
			paramsFormaPago.IdMedioDePago = in.p_IdMedioDePago;
			paramsFormaPago.p_ComprobantesAGirar = in.p_ComprobantesAGirar;
			paramsFormaPago.FechaGiro = in.p_FechaMovimiento;
			ListFormaPago.add(paramsFormaPago);
			sql.batch(con, SQL_UPDATE_FORMASPAGO_PAREGR, ListFormaPago, ParametrosUpdateFormaPago.class);
			
			//
			// Genera Nomina
			if(in.p_GeneraNomina.equalsIgnoreCase("S")) {
				//Genera Encabezado
				HashMap<String,Object> parmsVacio = new HashMap<String,Object>();
				List<ParametroSelectIdNomina> listIdNomina = new ArrayList<ParametroSelectIdNomina>();
				listIdNomina = sql.query(con, SQL_SELECT_NEXTIDNOMINA,parmsVacio,ParametroSelectIdNomina.class);
				ParametrosGeneraNomina ParametrosGeneraNomina = new ParametrosGeneraNomina();
				List<ParametrosGeneraNomina> listParametrosGeneraNomina = new ArrayList<ParametrosGeneraNomina>();
				ParametrosGeneraNomina.v_IdNominaTransferencia = listIdNomina.get(0).v_IdNominaTransferencia;
				ParametrosGeneraNomina.p_IdEmpresa = in.p_IdEmpresa;
				ParametrosGeneraNomina.v_IdComprobantePartidaEgreso = IdPAREGR;
				ParametrosGeneraNomina.p_FechaMovimiento = in.p_FechaMovimiento;
				ParametrosGeneraNomina.p_out_FechaLiquidacion = in.p_FechaMovimiento;
				ParametrosGeneraNomina.p_IdMedioDePago = in.p_IdMedioDePago;
				ParametrosGeneraNomina.p_IdModalidadDePago = in.p_IdModalidadDePago;
				ParametrosGeneraNomina.p_IdCuentaBancariaCorredora = in.p_IdCuentaBancariaCorredora;
				ParametrosGeneraNomina.v_IdTipoCuentaBancariaCorredora = cuentasBancarias.get(0).IdTipoCuentaBancaria;
				ParametrosGeneraNomina.p_out_IdMoneda = in.p_IdMoneda;
				ParametrosGeneraNomina.p_Monto = in.p_monto;
				ParametrosGeneraNomina.p_IdUsuario = in.p_IdUsuario;
				ParametrosGeneraNomina.p_UsaTalonarioCheque = in.p_UsaTalonarioCheque;
				listParametrosGeneraNomina.add(ParametrosGeneraNomina);
				sql.batch(con, SQL_INSERT_GENERANOMINA, listParametrosGeneraNomina, ParametrosGeneraNomina.class);
				//Genera Detalle
				ParametrosGeneraNominaDetalle ParametrosGeneraNominaDetalle = new ParametrosGeneraNominaDetalle();
				List<ParametrosGeneraNominaDetalle> listParametrosGeneraNominaDetalle = new ArrayList<ParametrosGeneraNominaDetalle>();
				ParametrosGeneraNominaDetalle.v_IdNominaTransferencia = listIdNomina.get(0).v_IdNominaTransferencia;
				ParametrosGeneraNominaDetalle.p_IdUsuario = in.p_IdUsuario;
				ParametrosGeneraNominaDetalle.p_IdMedioDePago = in.p_IdMedioDePago;
				listParametrosGeneraNominaDetalle.add(ParametrosGeneraNominaDetalle);
				sql.batch(con, SQL_INSERT_GENERANOMINADETALLE, listParametrosGeneraNominaDetalle, ParametrosGeneraNominaDetalle.class);
			// Genera Transferencia
			}
			if(in.p_GeneraTransferencia.equalsIgnoreCase("S")) {
				
				HashMap<String,Object> _parmsVacio = new HashMap<String,Object>();
				List<ParametroSelectIdNomina> listIdTransferencia = new ArrayList<ParametroSelectIdNomina>();
				listIdTransferencia = sql.query(con, SQL_SELECT_NEXTIDNOMINA,_parmsVacio,ParametroSelectIdNomina.class);
				ParametrosGeneraNomina ParametrosGeneraTransferencia = new ParametrosGeneraNomina();
				List<ParametrosGeneraNomina> listParametrosGeneraTransferencia = new ArrayList<ParametrosGeneraNomina>();
				ParametrosGeneraTransferencia.v_IdNominaTransferencia = listIdTransferencia.get(0).v_IdNominaTransferencia;
				ParametrosGeneraTransferencia.p_IdEmpresa = in.p_IdEmpresa;
				ParametrosGeneraTransferencia.v_IdComprobantePartidaEgreso = IdPAREGR;
				ParametrosGeneraTransferencia.p_FechaMovimiento = in.p_FechaMovimiento;
				ParametrosGeneraTransferencia.p_out_FechaLiquidacion = in.p_FechaMovimiento;
				ParametrosGeneraTransferencia.p_IdMedioDePago = in.p_IdMedioDePago;
				ParametrosGeneraTransferencia.p_IdModalidadDePago = in.p_IdModalidadDePago;
				ParametrosGeneraTransferencia.p_IdCuentaBancariaCorredora = in.p_IdCuentaBancariaCorredora;
				ParametrosGeneraTransferencia.v_IdTipoCuentaBancariaCorredora = cuentasBancarias.get(0).IdTipoCuentaBancaria;
				ParametrosGeneraTransferencia.p_out_IdMoneda = in.p_IdMoneda;
				ParametrosGeneraTransferencia.p_Monto = in.p_monto;
				ParametrosGeneraTransferencia.p_IdUsuario = in.p_IdUsuario;
				ParametrosGeneraTransferencia.p_UsaTalonarioCheque = in.p_UsaTalonarioCheque;
				listParametrosGeneraTransferencia.add(ParametrosGeneraTransferencia);
				sql.batch(con, SQL_INSERT_GENERATRANSFERENCIA, listParametrosGeneraTransferencia, ParametrosGeneraNomina.class);
				// Genera Transferencia Detalle
				ParametrosGeneraNominaDetalle ParametrosGeneraTransferenciaDetalle = new ParametrosGeneraNominaDetalle();
				List<ParametrosGeneraNominaDetalle> listParametrosGeneraTransferenciaDetalle = new ArrayList<ParametrosGeneraNominaDetalle>();
				ParametrosGeneraTransferenciaDetalle.v_IdNominaTransferencia = listIdTransferencia.get(0).v_IdNominaTransferencia;
				ParametrosGeneraTransferenciaDetalle.p_IdUsuario = in.p_IdUsuario;
				ParametrosGeneraTransferenciaDetalle.p_IdMedioDePago = in.p_IdMedioDePago;
				listParametrosGeneraTransferenciaDetalle.add(ParametrosGeneraTransferenciaDetalle);
				sql.batch(con, SQL_INSERT_GENERATRANSFERENCIADETALLE, listParametrosGeneraTransferenciaDetalle, ParametrosGeneraNominaDetalle.class);
			}
			
			// Limpia tablas globales restantes
			SQL.exec(con,SQL_DELETE_Comprobante);
			SQL.exec(con, SQL_DELETE_REGISTROS);
			SQL.exec(con, "DROP TABLE #RequisitosGirarComprobanteEgreso_p_ComprobantesAProcesar");

			if (MensajeSalida.getNumeroErrores(salidaCrearComprobante.P_OUT_MENSAJES) > 0) {
				log.error(salidaCrearComprobante.P_OUT_MENSAJES);

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
	

	public static void main(String[] args) throws Exception { 
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
					.serializeNulls().create();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"TES\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_IdCaja\":1,\"p_FechaMovimiento\":\"2023-10-23\",\"p_ComprobantesAGirar\":[\r\n"
					+ "    {\r\n"
					+ "        \"IdComprobante\": 1071581\r\n"
					+ "    },\r\n"
					+ "],\"p_IdMedioDePago\":\"TRA\",\"p_IdCuentaBancariaCorredora\":3049,\"p_FormasDePago\":[],\"p_monto\":85087732,\"p_IdModalidadDePago\":\"MODDEP\",\"p_IdMoneda\":\"CLP\",\"p_GeneraNomina\":\"N\",\"p_GeneraTransferencia\":\"S\",\"p_UsaTalonarioCheque\":\"N\"}";

			EntradaGirarComprobantesEgreso Objin = gson.fromJson(json,
					EntradaGirarComprobantesEgreso.class);

			GirarComprobantesEgreso demo = new GirarComprobantesEgreso();

			try {
				SalidaGirarComprobantesEgreso ObjOut = demo.logica(con, Objin);
				System.out.println(ObjOut);
				con.rollback();
				//con.commit();
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
		}
		System.out.println("== END ==");
	}
}