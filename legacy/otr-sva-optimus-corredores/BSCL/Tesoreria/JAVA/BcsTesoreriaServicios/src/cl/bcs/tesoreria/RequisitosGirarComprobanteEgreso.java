package cl.bcs.tesoreria;

import java.math.BigDecimal;
import java.sql.Connection;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.InformacionDetalle;

import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class RequisitosGirarComprobanteEgreso implements JavaCall {
	private final Logger log = LogManager.getLogger(RequisitosGirarComprobanteEgreso.class);
	private static final String CREATE_TEMPORARY_TABLE = "CREATE LOCAL TEMPORARY TABLE #RequisitosGirarComprobanteEgreso_p_ComprobantesAProcesar (\"IdComprobante\" BIGINT)";
	private static final String SQL_INSERT_REGISTROS = "INSERT INTO #RequisitosGirarComprobanteEgreso_p_ComprobantesAProcesar (\"IdComprobante\") VALUES (:IdComprobante)";
	private static final String SQL_DELETE_REGISTROS = "DELETE FROM #RequisitosGirarComprobanteEgreso_p_ComprobantesAProcesar WHERE 1=1";	
	private final String SQL_SELECT_VALIDAR_COMPROB_TESORERIA = loadResourceToString(
			"cl/bcs/tesoreria/sql/RequisitosGirarComprobanteEgreso_SelectComprobanteTes.sql");// -- Validar que vengan comprobantes de tesoreria a girar
	private final String SQL_SELECT_VALIDAR_CUENTA_CORREDORA = loadResourceToString("cl/bcs/tesoreria/sql/RequisitosGirarComprobanteIngreso_SelectCuentaCorredora.sql");// -- Validar que la cuenta bancaria de la corredora este bien configurada
	private final String SQL_SELECT_VALIDAR_FOLIOS_COMPROBANTES = loadResourceToString("cl/bcs/tesoreria/sql/RequisitosGirarComprobanteEgreso_SelectFoliosComprobantes.sql");// -- Validar los folios de  comprobantes sean validos
	private final String SQL_SELECT_VALIDAR_COMPROB_ESTADO = loadResourceToString("cl/bcs/tesoreria/sql/RequisitosGirarComprobanteEgreso_SelectComprobanteEstado.sql"); 
	private final String SQL_SELECT_VALIDAR_COMPROB_EGRESO = loadResourceToString("cl/bcs/tesoreria/sql/RequisitosGirarComprobanteEgreso_SelectComprobanteEgreso.sql");
	private final String SQL_SELECT_VALIDAR_COMPROB_PAREGR = loadResourceToString("cl/bcs/tesoreria/sql/RequisitosGirarComprobanteEgreso_SelectComprobantePAREGR.sql");
	private final String SQL_SELECT_VALIDAR_COMPROB_FECHALIQ = loadResourceToString("cl/bcs/tesoreria/sql/RequisitosGirarComprobanteEgreso_SelectComprobanteFechaLiq.sql");
	private final String SQL_SELECT_VALIDAR_COMPROB_MONEDA = loadResourceToString( "cl/bcs/tesoreria/sql/RequisitosGirarComprobanteEgreso_SelectComprobanteMoneda.sql");//// -- Valida que todos los comprobantes tengan la misma moneda
	private final String SQL_SELECT_VALIDAR_COMPROB_FORMAPAGO = loadResourceToString("cl/bcs/tesoreria/sql/RequisitosGirarComprobanteEgreso_SelectComprobanteFpago.sql");
	private final String SQL_SELECT_CONCEPTOS = loadResourceToString("cl/bcs/tesoreria/sql/RequisitosGirarComprobanteEgreso_SelectConceptos.sql");
	private final String SQL_SELECT_MONEDA_FECHA = loadResourceToString("cl/bcs/tesoreria/sql/RequisitosGirarComprobanteEgreso_SelectMonedaFecha.sql");
	private final String SQL_SELECT_VALIDAR_CAJA = loadResourceToString("cl/bcs/tesoreria/sql/RequisitosGirarComprobanteEgreso_SelectValidaCaja.sql");
	private final String SQL_SELECT_MEDIOS_PAGO = loadResourceToString("cl/bcs/tesoreria/sql/RequisitosGirarComprobanteEgreso_SelectMediosPago.sql");
	private final String SQL_SELECT_CAJA = loadResourceToString("cl/bcs/tesoreria/sql/RequisitosGirarComprobanteEgreso_SelectCaja.sql");
	private final String SQL_SELECT_VALIDAR_MUEVE_CUENTA_INVERSION = loadResourceToString("cl/bcs/tesoreria/sql/RequisitosGirarComprobanteEgreso_SelectMueveCuentaInversion.sql");
	private final String SQL_SELECT_VALIDAR_FORMAPAGO_ASIGNADA_PAREGR = loadResourceToString("cl/bcs/tesoreria/sql/RequisitosGirarComprobanteEgreso_SelectFormaPagoPAREGR.sql");
	private final String SQL_SELECT_VALIDAR_CUENTA_SOCIO = loadResourceToString("cl/bcs/tesoreria/sql/RequisitosGirarComprobanteEgreso_SelectValidaCuentaSocio.sql");

	private static final Logger logger = LogManager.getLogger(RequisitosGirarComprobanteEgreso.class);
	/***********************************************************************************************
	 * CLASES
	 ***********************************************************************************************/
	public static class EntradaRequisitoGirarComprobanteEgreso {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_simularOperacion;
		public String p_UsaReglasNegocio;
		public String p_FechaMovimiento;
		public List<Comprobante> p_ComprobantesAGirar;
		public String p_GeneraNomina;
		public String p_GeneraTransferencia;
		public String p_IdMedioDePago;
		public String p_IdCuentaBancariaCorredora;
		public BigDecimal p_Monto;
		public List<Comprobante> p_FormasDePago;
		public String p_usaTalonarioCheque;
		public String p_IdModalidadDePago;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class SalidaRequisitoGirarComprobanteEgreso {
		public String p_out_IdConcepto;
		public String p_out_NombreConcepto;
		public String p_out_IdMoneda;
		public String p_out_NombreMoneda;
		public String p_out_FechaLiquidacion;
		public Long p_out_IdCaja;
		public String p_out_NombreCaja;
		public int p_out_advertenciascontador;
		public List<MediosPago> p_out_MediosDePagoAGirar;
		public List<InformacionDetalle> P_OUT_ERRORESDETALLE;
		public Long					P_OUT_ERRORESCONTADOR;
		public List<Long>			P_OUT_SECUENCIA;
		public SalidaRequisitoGirarComprobanteEgreso() {
			this.P_OUT_ERRORESDETALLE = new ArrayList<InformacionDetalle>();
			this.p_out_MediosDePagoAGirar = new ArrayList<MediosPago>();
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_SECUENCIA = new ArrayList<Long>();
		}
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class Validar {
		public int Prioridad;
		public Integer Cumple;
		public String CodigoError;
		public String CodigoDato;
		public String ValorDato;
		public long IdComprobante;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class Comprobante {
		public long IdComprobante;
		public String toString() {
			return Utils.printJson(this, false);
		}
	} 
	public static class RequisitoGirarComprobante{
	public String p_out_IdConcepto;
	public String p_out_NombreConcepto;
	public String p_out_IdMoneda;
	public String p_out_NombreMoneda;
	public Date p_out_FechaLiquidacion;
	public Long p_out_IdCaja;
	public String p_out_NombreCaja;
	public int p_out_advertenciascontador;
	public List<MediosPago> p_out_MediosDePagoAGirar;
	public String toString() {
		return Utils.printJson(this, false);
		}
	}
	public static class MediosPago {
		public String IdMedioDePago;
		public BigDecimal MontoPorMedioDePago;
		public MediosPago() {
		}
		public MediosPago( String IdMedioDePago ,   BigDecimal MontoPorMedioDePago) {
			this.IdMedioDePago = IdMedioDePago;
			this.MontoPorMedioDePago = MontoPorMedioDePago;
		}
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
		EntradaRequisitoGirarComprobanteEgreso in = gson.fromJson(jin.toString(),
				EntradaRequisitoGirarComprobanteEgreso.class);
		SalidaRequisitoGirarComprobanteEgreso out = logica(con, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();

		return dataout;
	}

	/***********************************************************************************************
	 * LOGICA
	 ***********************************************************************************************/
	@Trace
	public SalidaRequisitoGirarComprobanteEgreso logica(Connection con,
			EntradaRequisitoGirarComprobanteEgreso entrada) throws Exception {
		SQL sql = new SQL();
		SalidaRequisitoGirarComprobanteEgreso salida = new SalidaRequisitoGirarComprobanteEgreso();
		logger.info("Inicio servicio ");
		SQL.exec(con, CREATE_TEMPORARY_TABLE);
		sql.batch(con, SQL_INSERT_REGISTROS, entrada.p_ComprobantesAGirar, Comprobante.class);
		// ---------------------------------------------------------------
		// -- VALIDACIONES BASICAS
		// ---------------------------------------------------------------
		List<Validar> comprobantesTeso = sql.query(con, SQL_SELECT_VALIDAR_COMPROB_TESORERIA, new HashMap<>(), Validar.class);
		salida.P_OUT_ERRORESCONTADOR = 0L;
		comprobantesTeso.stream().filter(req-> req.Cumple.equals(0)).forEach(req -> {
			try {
				addErrorToSalida(con, entrada, salida, req);
			} catch (Exception e) {
					log.debug(e);
				}
		});
			
		HashMap<String, Object> params = new HashMap<>();
		params.put("p_IdCuentaBancariaCorredora", entrada.p_IdCuentaBancariaCorredora);
		params.put("p_simularOperacion", 'N');
		List<Validar> cuentaCorredora = sql.query(con, SQL_SELECT_VALIDAR_CUENTA_CORREDORA, params, Validar.class);
		cuentaCorredora.stream().filter(req-> req.Cumple.equals(0)).forEach(req -> {
			try {
				addErrorToSalida(con, entrada, salida, req);
			} catch (Exception e) {
					log.debug(e);
				}
		});
			
		List<Validar> comprobanteFolios = sql.query(con, SQL_SELECT_VALIDAR_FOLIOS_COMPROBANTES, new HashMap<>(), Validar.class);
		comprobanteFolios.stream().filter(req-> req.Cumple.equals(0)).forEach(req -> {
			try {
				addErrorToSalida(con, entrada, salida, req);
			} catch (Exception e) {
					log.debug(e);
				}
		});
		// ---------------------------------------------------------------
		// -- VALIDACIONES DE NEGOCIO
		// ---------------------------------------------------------------
		List<Validar> comprobanteEstado = sql.query(con, SQL_SELECT_VALIDAR_COMPROB_ESTADO, new HashMap<>(), Validar.class);
		comprobanteEstado.stream().filter(req-> req.Cumple.equals(0)).forEach(req -> {
			try {
				addErrorToSalida(con, entrada, salida, req);
			} catch (Exception e) {
					log.debug(e);
				}
		});
		
		List<Validar> comprobanteEgreso = sql.query(con, SQL_SELECT_VALIDAR_COMPROB_EGRESO, new HashMap<>(), Validar.class);
		comprobanteEgreso.stream().filter(req-> req.Cumple.equals(0)).forEach(req -> {
			try {
				addErrorToSalida(con, entrada, salida, req);
			} catch (Exception e) {
					log.debug(e);
				}
		});
		
		List<Validar> comprobantePAREGR	 = sql.query(con, SQL_SELECT_VALIDAR_COMPROB_PAREGR, new HashMap<>(), Validar.class);
		comprobantePAREGR.stream().filter(req-> req.Cumple.equals(0)).forEach(req -> {
			try {
				addErrorToSalida(con, entrada, salida, req);
			} catch (Exception e) {
					log.debug(e);
				}
		});
		
		List<Validar> comprobanteFehaLiq = sql.query(con, SQL_SELECT_VALIDAR_COMPROB_FECHALIQ, new HashMap<>(), Validar.class);
		comprobanteFehaLiq.stream().filter(req-> req.Cumple.equals(0)).forEach(req -> {
			try {
				addErrorToSalida(con, entrada, salida, req);
			} catch (Exception e) {
					log.debug(e);
				}
		});
		
		List<Validar> comprobanteMueveCuentaInversion = sql.query(con, SQL_SELECT_VALIDAR_MUEVE_CUENTA_INVERSION, new HashMap<>(), Validar.class);
		comprobanteMueveCuentaInversion.stream().filter(req-> req.Cumple.equals(0)).forEach(req -> {
			try {
				addErrorToSalida(con, entrada, salida, req);
			} catch (Exception e) {
					log.debug(e);
				}
		});
		List<Validar> FormaPagoAsignadaPAREGR = sql.query(con, SQL_SELECT_VALIDAR_FORMAPAGO_ASIGNADA_PAREGR, new HashMap<>(), Validar.class);
        // Usar Stream para buscar el valor
        boolean encontrado = FormaPagoAsignadaPAREGR.stream()
                .allMatch((req-> req.Cumple == 1));

        // Imprimir resultado
        if (!encontrado) {
    		FormaPagoAsignadaPAREGR.stream().filter(req-> req.Cumple.equals(0)).forEach(req -> {
			try {
				addErrorToSalida(con, entrada, salida, req);
			} catch (Exception e) {
					log.debug(e);
				}
    		});
        }
        
		List<Validar> CuentaSocio = sql.query(con, SQL_SELECT_VALIDAR_CUENTA_SOCIO, new HashMap<>(), Validar.class);
        boolean encuentraCuenta = CuentaSocio.stream()
                .allMatch((req-> req.Cumple == 1));

        if (!encuentraCuenta) {
        	CuentaSocio.stream().filter(req-> req.Cumple.equals(0)).forEach(req -> {
			try {
				addErrorToSalida(con, entrada, salida, req);
			} catch (Exception e) {
					log.debug(e);
				}
    		});
        }
        
		List<Validar> comprobanteMoneda = sql.query(con, SQL_SELECT_VALIDAR_COMPROB_MONEDA, new HashMap<>(), Validar.class);
		comprobanteMoneda.stream().filter(req-> req.Cumple.equals(0)).forEach(req -> {
				try {
					addErrorToSalida(con, entrada, salida, req);
				} catch (Exception e) {
						log.debug(e);
					}
			});

		List<Validar> comprobanteForPago = sql.query(con, SQL_SELECT_VALIDAR_COMPROB_FORMAPAGO, new HashMap<>(), Validar.class);
		comprobanteForPago.stream().filter(req-> req.Cumple.equals(0)).forEach(req -> {
			try {
				addErrorToSalida(con,entrada,salida,req);
			} catch (Exception e) {
					log.debug(e);
				}
		});
		
		List<Validar> comprobanteCaja = sql.query(con, SQL_SELECT_VALIDAR_CAJA, new HashMap<>(), Validar.class);
		comprobanteCaja.stream().filter(req-> req.Cumple.equals(0)).forEach(req -> {
			try {
				addErrorToSalida(con, entrada, salida, req);
			} catch (Exception e) {
					log.debug(e);
				}
		});
		
		salida.p_out_MediosDePagoAGirar = sql.query(con, SQL_SELECT_MEDIOS_PAGO, new HashMap<>(), MediosPago.class);
		
		///CONCEPTOS
		
		List<SalidaRequisitoGirarComprobanteEgreso> conceptosComprobante = sql.query(con, SQL_SELECT_CONCEPTOS, new HashMap<>(), SalidaRequisitoGirarComprobanteEgreso.class);
		List<RequisitoGirarComprobante> MonedaFecha = sql.query(con, SQL_SELECT_MONEDA_FECHA, new HashMap<>(), RequisitoGirarComprobante.class);
		List<SalidaRequisitoGirarComprobanteEgreso> Caja = sql.query(con, SQL_SELECT_CAJA, new HashMap<>(), SalidaRequisitoGirarComprobanteEgreso.class);
		

		// -----------------------------------------------------------------------------
		// -- Valida que todos los comprobates tengan un medio de pago en comun.
		// -- No considera los camprobantes sin medio de pago asignado..
		// -----------------------------------------------------------------------------		
		
		salida.p_out_IdConcepto       =  conceptosComprobante.size() > 0 && conceptosComprobante.get(0).p_out_IdConcepto!= null   ? conceptosComprobante.get(0).p_out_IdConcepto : "";
		salida.p_out_NombreConcepto   =  conceptosComprobante.size() > 0 && conceptosComprobante.get(0).p_out_NombreConcepto!= null   ? conceptosComprobante.get(0).p_out_NombreConcepto : "";
		
		
		salida.p_out_IdMoneda         =  MonedaFecha.size() > 0	&&  MonedaFecha.get(0).p_out_IdMoneda!= null  			? MonedaFecha.get(0).p_out_IdMoneda : "";
		salida.p_out_NombreMoneda     =  MonedaFecha.size() > 0 && MonedaFecha.get(0).p_out_NombreMoneda!= null 	 		? MonedaFecha.get(0).p_out_NombreMoneda : "";
		salida.p_out_FechaLiquidacion =  MonedaFecha.size() > 0 && MonedaFecha.get(0).p_out_FechaLiquidacion!= null 	 	? MonedaFecha.get(0).p_out_FechaLiquidacion.toString() :"";
		salida.p_out_IdCaja           =   Caja.size() > 0 	 && Caja.get(0).p_out_IdCaja!= null 				    		? Caja.get(0).p_out_IdCaja : 1L;
		salida.p_out_NombreCaja       =  Caja.size() > 0 	&& Caja.get(0).p_out_NombreCaja!= null 					? Caja.get(0).p_out_NombreCaja : "";
		salida.p_out_advertenciascontador = 0;  

		SQL.exec(con, SQL_DELETE_REGISTROS);//TODO  MOVER
		try {
			SQL.exec(con, "DROP TABLE #RequisitosGirarComprobanteEgreso_p_ComprobantesAProcesar");
		} catch (Exception er) {
		}		
		return salida;
	}

	private void addErrorToSalida(Connection con, EntradaRequisitoGirarComprobanteEgreso entrada,
		SalidaRequisitoGirarComprobanteEgreso salida, Validar req) throws Exception {
		String codigo = req.CodigoError;
		String descripcion = Diccionario.getMensaje(con,entrada.p_IdEmpresa,req.CodigoError).msg()+" "+req.ValorDato;
		InformacionDetalle info = new InformacionDetalle();
		info.Codigo = codigo;
		info.Descripcion = descripcion;
		salida.P_OUT_ERRORESDETALLE.add(info);
		salida.P_OUT_ERRORESCONTADOR = 1L;
	}

	/***********************************************************************************************
	 * MAIN
	 ***********************************************************************************************/
	public static void main(String[] args) throws Exception { 
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
					.serializeNulls().create();
			//LIQUIDADO: 939867 FECH.LIQ: 2023-04-14  ----   INGRESADO:939867 FECH.LIQ: 2023-04-13
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"TES\",\"p_simularOperacion\":\"S\",\"p_UsaReglasNegocio\":\"S\",\"p_FechaMovimiento\":\"2023-04-14\",\"p_ComprobantesAGirar\":[{\"IdComprobante\":1068981}],\"p_GeneraNomina\":\"\",\"p_GeneraTransferencia\":\"\",\"p_IdMedioDePago\":\"\",\"p_IdCuentaBancariaCorredora\":0,\"p_Monto\":0,\"p_FormasDePago\":[],\"p_usaTalonarioCheque\":\"\",\"p_IdModalidadDePago\":\"\"}";

			EntradaRequisitoGirarComprobanteEgreso Objin = gson.fromJson(json,
					EntradaRequisitoGirarComprobanteEgreso.class);

			RequisitosGirarComprobanteEgreso demo = new RequisitosGirarComprobanteEgreso();

			try {
				SalidaRequisitoGirarComprobanteEgreso ObjOut = demo.logica(con, Objin);
				System.out.println(ObjOut);
				con.rollback();
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
		}
		System.out.println("== END ==");
	}
	
}