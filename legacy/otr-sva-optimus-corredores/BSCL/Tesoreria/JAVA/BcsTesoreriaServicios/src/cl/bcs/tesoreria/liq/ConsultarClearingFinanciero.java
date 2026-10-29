package cl.bcs.tesoreria.liq;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.tesoreria.pojo.Camara;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarClearingFinanciero implements JavaCall {
	
	public String SELECT_DATOS_CORREDOR = loadResourceToString("cl/bcs/tesoreria/sql/ConsultarClearingFinanciero_ConsultarDatosCorredor.sql");
	public String SELECT_CONSULTAR_EMPRESA = loadResourceToString("cl/bcs/tesoreria/sql/ConsultarClearingFinanciero_ConsultarDatosCorredor.sql");
	public String SELECT_CONSULTAR_CONFIGURACION_TESORERIA = loadResourceToString("cl/bcs/tesoreria/sql/ConsultarClearingFinanciero_ConsultarConfiguracionTesoreria.sql");
	public String SELECT_CONSULTAR_TRANSACCIONES_MERCADO = loadResourceToString("cl/bcs/tesoreria/sql/ConsultarClearingFinanciero_ConsultarTransaccionesDeMercados.sql");
	public String SELECT_CONSULTAR_DIVIDENDO = loadResourceToString("cl/bcs/tesoreria/sql/ConsultarClearingFinanciero_ConsultarDividendos.sql");
	public String SELECT_CONSULTAR_LIQUIDACIONES = loadResourceToString("cl/bcs/tesoreria/sql/ConsultarClearingFinanciero_ConsultarLiquidaciones.sql");
	public String SELECT_CONSULTAR_ANTICIPOS = loadResourceToString("cl/bcs/tesoreria/sql/ConsultarClearingFinanciero_ConsultarAnticiposSimultaneas.sql");
	public String SELECT_CONSULTAR_ANTICIPOS_HIS = loadResourceToString("cl/bcs/tesoreria/sql/ConsultarClearingFinanciero_ConsultarAnticiposSimultaneasHistorico.sql");

	
	
	//Parametros corespondientes a derivados
	public static Long codigoGrupoCamaraDV = 4L;
	public static String nombreGrupoCamaraDV = "DV";
	
	
	//Parametros correspondientes a PH + SM
	public static String nombreGrupoCamaraPHSM = "PH + SM";
	public static Long codigoGrupoCamaraPHSM = 2L;
	
	
	//Parametros correspondientes a RV
	public static String nombreGrupoCamaraRV = "RV";
	public static Long codigoGrupoCamaraRV = 3L;

	
	//Parametros Camara RV + PM
	public static String nombreGrupoCamaraRVPM = "RV + PM";
	public static Long codigoGrupoCamaraRVPM = 1L;
	
	
	//Parametros Camara PM
	public static String nombreGrupoCamaraPM = "PM";
	public static Long codigoGrupoCamaraPM = 1L;
	
	
	//Parametros Camara HS (PH + SM + PM)
	public static String nombreGrupoCamaraHS = "HS";
	public static Long codigoGrupoCamaraHS = 5L;
	
	
	
	public static class EntradaClearingFinanciero{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_FechaLiquidacion;
		public List<Camara> p_Camaras;
		
		public EntradaClearingFinanciero() {
			super();
			this.p_Camaras = new ArrayList<Camara>();
		}
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class AnticiposSimultaneas{
		public String TipoOperacion;
		public String AccionOperacion;
		public Date FechaLiquidacion;
		public String FolioTransaccion;
		public String Nemotecnico;
		public BigDecimal Cantidad;
		public BigDecimal Monto;
		public Long CodigoGrupoCamara;
		public String NombreGrupoCamara;
		public String RutContraparte;
		public String RazonSocialContraparte;
		public Long IdCorredorContraparte;
		public String IdBolsa;
		public String IdClasificacionTipoInstrumento;
		public Long IdComprobanteTesoreria;
		public String IdMoneda;
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class ClearingFinancieroSimultaneasDetalle{
		public String TipoOperacion;
		public Date FechaLiquidacion;
		public String Nemotecnico;
		public BigDecimal Cantidad;
		public BigDecimal MontoCompra;
		public BigDecimal MontoVenta;
		public String NombreGrupoCamara;
		public String RutContraparte;
		public String IdClasificacionTipoInstrumento;
		public Long IdComprobanteTesoreria;
		public String IdBolsa;
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ClearingFinancieroTransaccionesDividendos{
		public Long IdTransaccion;
		public String IdClasificacionTipoInstrumento;  // RV, RF, IF, DV, FM
		public Date FechaSistemaCreacion;	          // fecha de sistema del ingreso del palo(esta fecha es igual a la FechaOperacion pero sin la hora) 
		public String IdTipoInstrumento;                // ACN /ACX /CFI / CFM / SIM    
		public String FolioTransaccion;		      
		public String IndicadorOrdenDirecta; 		     // S/N
		public String IdBolsa;
		public BigDecimal MontoTransaccion;				  
		public String IdTipoOperacion;        		  // COMACN  / VENACX
		public String AccionTipoOperacion;    		  // COMPRA / VENTA 
		public String IdCondicionDeLiquidacion;    	  // T0/T1/T2
		public String CodigoCondicionDeLiquidacion;     // PH/PM/CN
		public String Instrumento;
		public Long IdCorredorContraparte;
		public String CodigoOperadorContraparte;
		public String RutContraparte;
		public String RazonSocialContraparte;
		public String CodigoCorredorContraparte;
		public String CodigoOperadorCorredora;			
		public Date FechaLiquidacion;
		public Date FechaVencimiento; // Para Simultaneas
		public String EsAPlazo;
		public String EsBilateral;     // Se carga desde TBCLOPER con WS de actualizacion de transacciones
		public String IdCamara;        // Se carga desde TBCLOPER con WS de actualizacion de transacciones
		public Long IdComprobanteTesoreria;    	// Si existe este valor; ya se proceso la liquidacion
		public BigDecimal CantidadTransaccion;
		public BigDecimal PrecioTransaccion;
		public BigDecimal Tasa;
		public Long IdEventoCapitalCliente;
		public Long IdEventoCapital;
		public String IdMonedaTransaccion;
		public BigDecimal MontoRetencion;
		public Long CodigoGrupoCamara;
		public String NombreGrupoCamara;
		public BigDecimal Total;
		public BigDecimal TotalVenta;
		public BigDecimal TotalCompra;
		public BigDecimal SaldoAcreedor;
		public BigDecimal SaldoDeudor;
		public String IdMoneda;

		public ClearingFinancieroTransaccionesDividendos() {
			TotalVenta = BigDecimal.ZERO;
			TotalCompra = BigDecimal.ZERO;
			SaldoAcreedor = BigDecimal.ZERO;
			SaldoDeudor = BigDecimal.ZERO;
			MontoRetencion = BigDecimal.ZERO;
			
		}
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ClearingFinancieroConfiguracionTesoreria{
		public Long IdEmpresa;
		public String CodigoParametro;
		public String Descripcion;
		public String Valor;
	}
	
	public static class ClearingFinancieroDetalle{
		
		public Long IdTransaccion;
		public String IdBolsa;
		public Long CodigoGrupoCamara;
		public String NombreGrupoCamara;
		public String RutContraparte;
		public String AccionTipoOperacion;    		  // COMPRA / VENTA
		public BigDecimal TotalVenta;
		public BigDecimal TotalCompra;
		public String RazonSocialContraparte;
		public String IdClasificacionTipoInstrumento;  // RV, RF, IF, DV, FM
		public Date FechaSistemaCreacion;	          // fecha de sistema del ingreso del palo(esta fecha es igual a la FechaOperacion pero sin la hora) 
		public String FolioTransaccion;		  
		public String IdCondicionDeLiquidacion;    	  // T0/T1/T2
		public String CodigoCondicionDeLiquidacion;     // PH/PM/CN
		public String IdMonedaTransaccion;
		public Long IdComprobanteTesoreria;    	// Si existe este valor; ya se proceso la liquidacion
		public String Instrumento;
		public BigDecimal CantidadTransaccion;
		public BigDecimal PrecioTransaccion;
		public BigDecimal Tasa;
		public BigDecimal MontoRetencion;
		public Long IdEventoCapital;
		public Long IdEventoCapitalCliente;
		public String CompraVenta;
		public ClearingFinancieroDetalle() {
			TotalVenta = BigDecimal.ZERO;
			TotalCompra = BigDecimal.ZERO;
			MontoRetencion = BigDecimal.ZERO;
			CompraVenta = "";
		}
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ClearingFinancieroLiquidacion{
		public Long IdMovimiento;
		public String Cliente;
		public Long IdPortafolio;
		public Date FechaMovimiento;
		public BigDecimal Abono;
		public BigDecimal Cargo;
		public String TipoMovimiento;
		public String DescripcionMovimiento;
		public String AccionTipoOperacion;
		public BigDecimal Monto;
		
		//Datos Resumen
		public String IdBolsa;
		public Long CodigoGrupoCamara;
		public String NombreGrupoCamara;
		public String IdClasificacionTipoInstrumento;
		public Long IdCorredorContraparte;
		public String RutContraparte;
		public String RazonSocialContraparte;
		public String IdMonedaTransaccion;
		public Long IdComprobanteTesoreria;
		public BigDecimal TotalVenta;
		public BigDecimal TotalCompra;
		public BigDecimal SaldoAcreedor;
		public BigDecimal SaldoDeudor;
		public BigDecimal MontoRetencion;
		public ClearingFinancieroLiquidacion() {
			MontoRetencion = BigDecimal.ZERO;
		}
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ClearingFinancieroResumen{
		public String IdBolsa;
		public Long CodigoGrupoCamara;
		public String NombreGrupoCamara;
		public String IdClasificacionTipoInstrumento;
		public Long IdCorredorContraparte;
		public String RutContraparte;
		public String RazonSocialContraparte;
		public String IdMonedaTransaccion;
		public Long IdComprobanteTesoreria;
		public BigDecimal TotalVenta;
		public BigDecimal TotalCompra;
		public BigDecimal SaldoAcreedor;
		public BigDecimal SaldoDeudor;
		public BigDecimal MontoRetencion;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ValidadoresParametrosCamaras{
		public String CamaraRV;
		public String CamaraPM;
		public String CamaraPH;
		public String CamaraSM;
		public String CamaraDV;
		public String ClearingIncluyeRFPM;
		public String ClearingIncluyeIFPM;
		public String ClearingIncluyeRFPH;
		public String ClearingIncluyeIFPH;
		public String CambiosCCLV;
		
		public ValidadoresParametrosCamaras() {
			CamaraRV = "N";
			CamaraPM = "N";
			CamaraPH = "N";
			CamaraSM = "N";
			CamaraDV = "N";
			ClearingIncluyeRFPM = "N";
			ClearingIncluyeIFPM = "N";
			ClearingIncluyeRFPH = "N";
			ClearingIncluyeIFPH = "N";
			CambiosCCLV = "N";
		}
	}
	
	public static class ClearingDatosCorredor{
		public Long IdEmpresa;
		public String Codigo;
		public String Nombre;
		public String IdEstado;
		public Long IdPais;
		public Long IdSocioDeNegocio;
		public String NombreCorto;
		public Long IdContraparte;
		public Long IdPersona;
		public Date FechaIngreso;
		public Date FechaCreacion;
		public Date FechaModificacion;
		public Long IdDocumentoIdentidad;
		public String DocumentoIdentidad;
		public String TipoDocumentoIdentidad;
		public String DocumentoIdentidadAdicional;
		public String TipoDocumentoIdentidadAdicional;
		public String RazonSocial;
		public String CodigoContraparte;
		public String EsCorredora;
		public String DocumentoIdentidadRepresentante;
		public String DireccionElectronicaPrincipal;
		public String DireccionFisicaPrincipalCalle;
		public String DireccionFisicaPrincipalNumero;
		public String DireccionFisicaPrincipalDepartamento;
		public String DireccionFisicaPrincipalSector;
		public String DireccionFisicaPrincipalNombreCiudad;
		public String DireccionFisicaPrincipalNombreDivisionPais;
		public String DireccionFisicaPrincipalNombrePais;
		public String TelefonoPrincipal;
	}
	
	public static class SalidaClearingFinanciero{
		
		public List<ClearingFinancieroResumen> P_OUT_CLEARINGFINANCIERORESUMEN;
		public List<ClearingFinancieroDetalle> P_OUT_CLEARINGFINANCIERODETALLE;
		public List<ClearingFinancieroTransaccionesDividendos> P_OUT_CLEARINGFINANCIERODIVIDENDOS;
		public List<ClearingFinancieroLiquidacion>	P_OUT_CLEARINGFINANCIEROLIQUIDACIONES;
		public List<ClearingFinancieroSimultaneasDetalle> P_OUT_CLEARINGFINANCIEROSIMULTANEAS;
		
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		
		public SalidaClearingFinanciero () {
			super();
			this.P_OUT_CLEARINGFINANCIERODETALLE = new ArrayList<>();
			this.P_OUT_CLEARINGFINANCIERORESUMEN = new ArrayList<>();		
			this.P_OUT_CLEARINGFINANCIERODIVIDENDOS = new ArrayList<>();
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_CLEARINGFINANCIEROLIQUIDACIONES = new ArrayList<>();
			this.P_OUT_CLEARINGFINANCIEROSIMULTANEAS = new ArrayList<>();
		}
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		
		Gson gson = Utils.getGsonBuilder();
		EntradaClearingFinanciero in = gson.fromJson(jin.toString(), EntradaClearingFinanciero.class);
		SalidaClearingFinanciero out = logica(conn, in);
		JsonParser parser = new JsonParser();
		return parser.parse(gson.toJson(out)).getAsJsonObject();
		
	}
	
	
	public ClearingFinancieroTransaccionesDividendos LlenarTransaccionDividendo(ClearingFinancieroTransaccionesDividendos transaccion, String nombreGrupoCamara, Long codigoGrupoCamara) {
		
		ClearingFinancieroTransaccionesDividendos transaccionDetalle = new ClearingFinancieroTransaccionesDividendos();
		
		transaccionDetalle.IdTransaccion = transaccion.IdTransaccion;
		transaccionDetalle.IdClasificacionTipoInstrumento = transaccion.IdClasificacionTipoInstrumento;
		transaccionDetalle.IdBolsa = transaccion.IdBolsa;
		transaccionDetalle.CodigoGrupoCamara = codigoGrupoCamara;
		transaccionDetalle.NombreGrupoCamara = nombreGrupoCamara;
		transaccionDetalle.RutContraparte = transaccion.RutContraparte;
		transaccionDetalle.CodigoOperadorContraparte = transaccion.CodigoOperadorContraparte;
		transaccionDetalle.IdCorredorContraparte = transaccion.IdCorredorContraparte;
		transaccionDetalle.AccionTipoOperacion = transaccion.AccionTipoOperacion;
		transaccionDetalle.TotalVenta = transaccion.AccionTipoOperacion.equals("VENTA") && transaccion.AccionTipoOperacion != null ?  transaccion.MontoTransaccion : BigDecimal.ZERO;							
		transaccionDetalle.TotalCompra = transaccion.AccionTipoOperacion.equals("COMPRA") && transaccion.AccionTipoOperacion != null ?  transaccion.MontoTransaccion : BigDecimal.ZERO;							
		transaccionDetalle.RazonSocialContraparte = transaccion.RazonSocialContraparte;
		transaccionDetalle.FolioTransaccion = transaccion.FolioTransaccion;
		transaccionDetalle.IdCondicionDeLiquidacion = transaccion.IdCondicionDeLiquidacion;
		transaccionDetalle.CodigoCondicionDeLiquidacion = transaccion.CodigoCondicionDeLiquidacion;
		transaccionDetalle.FechaSistemaCreacion = transaccion.FechaSistemaCreacion;
		transaccionDetalle.IdComprobanteTesoreria = transaccion.IdComprobanteTesoreria;
		transaccionDetalle.PrecioTransaccion = transaccion.PrecioTransaccion;
		transaccionDetalle.Tasa = transaccion.Tasa;
		transaccionDetalle.CantidadTransaccion = transaccion.CantidadTransaccion;
		transaccionDetalle.IdMonedaTransaccion = transaccion.IdMonedaTransaccion != null ? transaccion.IdMonedaTransaccion : transaccion.IdMoneda;
		transaccionDetalle.MontoRetencion = transaccion.MontoRetencion;
		transaccionDetalle.Instrumento = transaccion.Instrumento;
		transaccionDetalle.IdEventoCapital = transaccion.IdEventoCapital;
		transaccionDetalle.IdEventoCapitalCliente = transaccion.IdEventoCapitalCliente;
		
		return transaccionDetalle;
	}
	
	public SalidaClearingFinanciero logica(Connection conn, EntradaClearingFinanciero in) {
		
		SalidaClearingFinanciero salida = new SalidaClearingFinanciero();
		SQL sql = new SQL();
		
		
		//Obtener Informacion de la empresa
		/*********** Inicia Nueva logica ***********/
		//Nueva logica de Clearing Financiero
		try {
			ValidadoresParametrosCamaras validadores = new ValidadoresParametrosCamaras();
			
			HashMap<String, Object> paramsDatosCorredor = new HashMap<String, Object>();
			paramsDatosCorredor.put("p_IdEmpresa", in.p_IdEmpresa);
			String fechaSistema = "0001-01-01";
			List<ClearingDatosCorredor> datosCorredor = sql.query(conn, SELECT_DATOS_CORREDOR, paramsDatosCorredor, ClearingDatosCorredor.class);
			try {
				fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
			} catch (Exception e) {
		
			}
			
			//Logica de vacio
			if(datosCorredor.isEmpty()) {
				salida.P_OUT_MENSAJES.add(new MensajeSalida(1L,MensajeSalida.MENSAJE_ERROR, "UI_CORREDOR_NO_EXISTE", "Codigo Corredor no existe"));
			}
			else {
				//Verificar si el sp finaliza el programa
				for(Camara cam : in.p_Camaras) {
					switch(cam.Camara) {
						case "RV":
							validadores.CamaraRV = "S";
							break;
						case "PM":
							validadores.CamaraPM = "S";
							break;
						case "PH":
							validadores.CamaraPH = "S";
							break;
						case "SM":
							validadores.CamaraSM = "S";
							break;
						case "DV":
							validadores.CamaraDV = "S";
							
					}
				}
				
				HashMap<String, Object> paramsParametros = new HashMap<String, Object>();
				paramsParametros.put("p_IdEmpresa", in.p_IdEmpresa);
				
				List<ClearingFinancieroConfiguracionTesoreria> datosConfiguracion = sql.query(conn, SELECT_CONSULTAR_CONFIGURACION_TESORERIA, paramsParametros, ClearingFinancieroConfiguracionTesoreria.class);
				
				for(ClearingFinancieroConfiguracionTesoreria objeto : datosConfiguracion) {
					switch(objeto.CodigoParametro) {
						case "CLEARINGINCLUYERFPM":
							validadores.ClearingIncluyeRFPM = "S";
							break;
						case "CLEARINGINCLUYEIFPM":
							validadores.ClearingIncluyeIFPM = "S";
							break;
						case "CLEARINGINCLUYERFPH":
							validadores.ClearingIncluyeRFPH = "S";
							break;
						case "CLEARINGINCLUYEIFPH":
							validadores.ClearingIncluyeIFPH = "S";
							break;
						case "CAMBIOSCCLV":
							validadores.CambiosCCLV = "S";
							break;
					}
				}
				
				//v_Transacciones
				List<ClearingFinancieroTransaccionesDividendos> transaccionesDeMercado = new ArrayList<ClearingFinancieroTransaccionesDividendos>();
				List<AnticiposSimultaneas> anticipos = new ArrayList<AnticiposSimultaneas>();
				HashMap<String, Object> paramsTransacciones = new HashMap<String, Object>();
				paramsTransacciones.put("p_IdEmpresa", in.p_IdEmpresa);
				paramsTransacciones.put("p_FechaLiquidacion", in.p_FechaLiquidacion);
				
				transaccionesDeMercado = sql.query(conn, SELECT_CONSULTAR_TRANSACCIONES_MERCADO, paramsTransacciones, ClearingFinancieroTransaccionesDividendos.class);
				
				//v_temp_transacciones
				List<ClearingFinancieroTransaccionesDividendos> transaccionesMercadoFiltroDetalles = new ArrayList<ClearingFinancieroTransaccionesDividendos>();
				
				//v_temp_dividendos
				List<ClearingFinancieroTransaccionesDividendos> clearingDividendos = new ArrayList<ClearingFinancieroTransaccionesDividendos>();
				
				//Lectura de parámetro "usaCamaraHS"
				String usaCamaraHS = "";
				try {
					usaCamaraHS = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "TESORERIA", "UsaCamaraHS");
				}catch (Exception ex){
					usaCamaraHS = "N";
		    	}		
				
				//Se lleva v_temp_transacciones
				for(ClearingFinancieroTransaccionesDividendos transaccion : transaccionesDeMercado) {
					ClearingFinancieroTransaccionesDividendos transaccionDetalle = new ClearingFinancieroTransaccionesDividendos();
					String nombreGrupoCamara = "";
					Long codigoGrupoCamara = 0L;
					
					
				    // LÓGICA CAMARA HS
					if ("S".equalsIgnoreCase(usaCamaraHS)) {
					
					//Varias condiciones
						
				    // Condición segun CN (HS)
						
					if("T2".equals(transaccion.IdCondicionDeLiquidacion) && "PH".equals(transaccion.IdCamara) && ("N".equals(validadores.CamaraRV) && "N".equals(validadores.CamaraDV))
							&& ("RF".equals(transaccion.IdClasificacionTipoInstrumento) || "IF".equals(transaccion.IdClasificacionTipoInstrumento))) {
								
						nombreGrupoCamara = nombreGrupoCamaraHS;
				        codigoGrupoCamara = codigoGrupoCamaraHS;

				        transaccionDetalle = LlenarTransaccionDividendo(transaccion, nombreGrupoCamara, codigoGrupoCamara);
				        transaccionesMercadoFiltroDetalles.add(transaccionDetalle);												
					}
					
					if("T2".equals(transaccion.IdCondicionDeLiquidacion) && "PH".equals(transaccion.IdCamara) && ("S".equals(validadores.CamaraRV) && "S".equals(validadores.CamaraDV) && "S".equals(validadores.CamaraPH) && "S".equals(validadores.CamaraPM) && "S".equals(validadores.CamaraSM))
							&& ("RF".equals(transaccion.IdClasificacionTipoInstrumento) || "IF".equals(transaccion.IdClasificacionTipoInstrumento))) {
								
						nombreGrupoCamara = nombreGrupoCamaraHS;
				        codigoGrupoCamara = codigoGrupoCamaraHS;

				        transaccionDetalle = LlenarTransaccionDividendo(transaccion, nombreGrupoCamara, codigoGrupoCamara);
				        transaccionesMercadoFiltroDetalles.add(transaccionDetalle);												
					}
					
					if("T2".equals(transaccion.IdCondicionDeLiquidacion) && "PH".equals(transaccion.IdCamara) && ("S".equals(validadores.CamaraRV) && "N".equals(validadores.CamaraDV) && "S".equals(validadores.CamaraPH) && "S".equals(validadores.CamaraPM) && "S".equals(validadores.CamaraSM))
							&& ("RF".equals(transaccion.IdClasificacionTipoInstrumento) || "IF".equals(transaccion.IdClasificacionTipoInstrumento))) {
								
						nombreGrupoCamara = nombreGrupoCamaraHS;
				        codigoGrupoCamara = codigoGrupoCamaraHS;

				        transaccionDetalle = LlenarTransaccionDividendo(transaccion, nombreGrupoCamara, codigoGrupoCamara);
				        transaccionesMercadoFiltroDetalles.add(transaccionDetalle);												
					}
					
					
														
					// Condiciones segun PH + SM (HS)
					if(("S".equals(validadores.CamaraPH) || "S".equals(validadores.CamaraSM)) 
					    && !"RV".equals(transaccion.IdClasificacionTipoInstrumento)) {

					    if( "T0".equals(transaccion.IdCondicionDeLiquidacion) && (
					            ("S".equals(validadores.ClearingIncluyeRFPH) && "RF".equals(transaccion.IdClasificacionTipoInstrumento))
					         || ("S".equals(validadores.ClearingIncluyeIFPH) && "IF".equals(transaccion.IdClasificacionTipoInstrumento))
					       )
					    ) {
					        nombreGrupoCamara = nombreGrupoCamaraHS;
					        codigoGrupoCamara = codigoGrupoCamaraHS;

					        transaccionDetalle = LlenarTransaccionDividendo(transaccion, nombreGrupoCamara, codigoGrupoCamara);
					        transaccionesMercadoFiltroDetalles.add(transaccionDetalle);
					    }
					}

					// Condiciones segun PM (HS con CambiosCCLV)
					if("S".equals(validadores.CamaraPM) 
					    && !"RV".equals(transaccion.IdClasificacionTipoInstrumento)) {

					    if( "T1".equals(transaccion.IdCondicionDeLiquidacion) && (
					            ("S".equals(validadores.ClearingIncluyeRFPM) && "RF".equals(transaccion.IdClasificacionTipoInstrumento) && "S".equals(validadores.CambiosCCLV))
					         || ("S".equals(validadores.ClearingIncluyeIFPM) && "IF".equals(transaccion.IdClasificacionTipoInstrumento) && "S".equals(validadores.CambiosCCLV))
					       )
					    ) {
					        nombreGrupoCamara = nombreGrupoCamaraHS;
					        codigoGrupoCamara = codigoGrupoCamaraHS;

					        transaccionDetalle = LlenarTransaccionDividendo(transaccion, nombreGrupoCamara, codigoGrupoCamara);
					        transaccionesMercadoFiltroDetalles.add(transaccionDetalle);
					    }
					}
					
					//Condiciones segun PM
					if(validadores.CamaraPM.equals("S") && transaccion.IdCondicionDeLiquidacion.equals("T1") && (transaccion.IdClasificacionTipoInstrumento.equals("RF") || transaccion.IdClasificacionTipoInstrumento.equals("IF")) ) {
						if(validadores.CambiosCCLV.equals("N")) {
							if(validadores.ClearingIncluyeIFPM.equals("S") || validadores.ClearingIncluyeRFPM.equals("S")) {
							
								nombreGrupoCamara = nombreGrupoCamaraRVPM;
								codigoGrupoCamara = codigoGrupoCamaraRVPM;
																						
								transaccionDetalle = LlenarTransaccionDividendo(transaccion, nombreGrupoCamara, codigoGrupoCamara);
								transaccionesMercadoFiltroDetalles.add(transaccionDetalle);
							}
						}												
					}
					
					//Condiciones Segun RV
					if(validadores.CamaraRV.equals("S") && transaccion.IdClasificacionTipoInstrumento.equals("RV") && transaccion.IdCondicionDeLiquidacion.equals("T2")) {
						if(validadores.CambiosCCLV.equals("S")) {
							nombreGrupoCamara = nombreGrupoCamaraRV;
							codigoGrupoCamara = codigoGrupoCamaraRV;
						}
						else {
							nombreGrupoCamara = nombreGrupoCamaraRVPM;
							codigoGrupoCamara = codigoGrupoCamaraRVPM;
						}
						
						transaccionDetalle = LlenarTransaccionDividendo(transaccion, nombreGrupoCamara, codigoGrupoCamara);
						transaccionesMercadoFiltroDetalles.add(transaccionDetalle);
					}
					
					//Condiciones Segun SM
					if(validadores.CamaraSM.equals("S")) {
						if(transaccion.IdClasificacionTipoInstrumento.equals("RV") && transaccion.IdCamara.equals("SM") && transaccion.IdCondicionDeLiquidacion.equals("T0") && transaccion.IdTipoInstrumento.equals("SIM")
						&& (transaccion.AccionTipoOperacion.equals("COMPRA") || transaccion.AccionTipoOperacion.equals("VENTA"))) {
							
							nombreGrupoCamara = nombreGrupoCamaraHS;
							codigoGrupoCamara = codigoGrupoCamaraHS;
							
							transaccionDetalle = LlenarTransaccionDividendo(transaccion, nombreGrupoCamara, codigoGrupoCamara);
							transaccionesMercadoFiltroDetalles.add(transaccionDetalle);
						}
					}
					
					if(validadores.CamaraDV.equals("S") & transaccion.IdClasificacionTipoInstrumento.equals("DV")) {
						nombreGrupoCamara = nombreGrupoCamaraDV; 
						codigoGrupoCamara = codigoGrupoCamaraDV;
						
						transaccionDetalle = LlenarTransaccionDividendo(transaccion, nombreGrupoCamara, codigoGrupoCamara);
						
						transaccionesMercadoFiltroDetalles.add(transaccionDetalle);
					}
				  } else {
						//Varias condiciones
												
						//Condiciones segun PH
						if(validadores.CamaraPH.equals("S") && transaccion.IdCondicionDeLiquidacion.equals("T0")) {
							if(validadores.ClearingIncluyeRFPH.equals("S") &&  transaccion.IdClasificacionTipoInstrumento.equals("RF") ) {
								nombreGrupoCamara = nombreGrupoCamaraPHSM;
								codigoGrupoCamara = codigoGrupoCamaraPHSM;
								
								transaccionDetalle = LlenarTransaccionDividendo(transaccion, nombreGrupoCamara, codigoGrupoCamara);
								transaccionesMercadoFiltroDetalles.add(transaccionDetalle);
							}
							
						}
						
						//Condiciones segun PH
						if(validadores.CamaraPH.equals("S") && transaccion.IdCondicionDeLiquidacion.equals("T0")) {
							if(validadores.ClearingIncluyeIFPH.equals("S")  && transaccion.IdClasificacionTipoInstrumento.equals("IF") ) {
								nombreGrupoCamara = nombreGrupoCamaraPHSM;
								codigoGrupoCamara = codigoGrupoCamaraPHSM;
								
								transaccionDetalle = LlenarTransaccionDividendo(transaccion, nombreGrupoCamara, codigoGrupoCamara);
								transaccionesMercadoFiltroDetalles.add(transaccionDetalle);
							}
							
						}
						
						//Condiciones segun PM
						if(validadores.CamaraPM.equals("S") && transaccion.IdCondicionDeLiquidacion.equals("T1") && (transaccion.IdClasificacionTipoInstrumento.equals("RF") || transaccion.IdClasificacionTipoInstrumento.equals("IF")) ) {
							if(validadores.CambiosCCLV.equals("S")) {
								nombreGrupoCamara = nombreGrupoCamaraPM;
								codigoGrupoCamara = codigoGrupoCamaraPM;
							}
							else {
								nombreGrupoCamara = nombreGrupoCamaraRVPM;
								codigoGrupoCamara = codigoGrupoCamaraRVPM;
							}
							
							if(validadores.ClearingIncluyeIFPM.equals("S") || validadores.ClearingIncluyeRFPM.equals("S")) {
								
								transaccionDetalle = LlenarTransaccionDividendo(transaccion, nombreGrupoCamara, codigoGrupoCamara);
								transaccionesMercadoFiltroDetalles.add(transaccionDetalle);
							}
						}
						
						//Condiciones Segun RV
						if(validadores.CamaraRV.equals("S") && transaccion.IdClasificacionTipoInstrumento.equals("RV") && transaccion.IdCondicionDeLiquidacion.equals("T2")) {
							if(validadores.CambiosCCLV.equals("S")) {
								nombreGrupoCamara = nombreGrupoCamaraRV;
								codigoGrupoCamara = codigoGrupoCamaraRV;
							}
							else {
								nombreGrupoCamara = nombreGrupoCamaraRVPM;
								codigoGrupoCamara = codigoGrupoCamaraRVPM;
							}
							
							transaccionDetalle = LlenarTransaccionDividendo(transaccion, nombreGrupoCamara, codigoGrupoCamara);
							transaccionesMercadoFiltroDetalles.add(transaccionDetalle);
						}
						
						//Condiciones Segun SM
						if(validadores.CamaraSM.equals("S")) {
							if(transaccion.IdClasificacionTipoInstrumento.equals("RV") && transaccion.IdCamara.equals("SM") && transaccion.IdCondicionDeLiquidacion.equals("T0") && transaccion.IdTipoInstrumento.equals("SIM")
							&& (transaccion.AccionTipoOperacion.equals("COMPRA") || transaccion.AccionTipoOperacion.equals("VENTA"))) {
								
								nombreGrupoCamara = nombreGrupoCamaraPHSM;
								codigoGrupoCamara = codigoGrupoCamaraPHSM;
								
								transaccionDetalle = LlenarTransaccionDividendo(transaccion, nombreGrupoCamara, codigoGrupoCamara);
								transaccionesMercadoFiltroDetalles.add(transaccionDetalle);
							}
						}
						
						if(validadores.CamaraDV.equals("S") & transaccion.IdClasificacionTipoInstrumento.equals("DV")) {
							nombreGrupoCamara = nombreGrupoCamaraDV; 
							codigoGrupoCamara = codigoGrupoCamaraDV;
							
							transaccionDetalle = LlenarTransaccionDividendo(transaccion, nombreGrupoCamara, codigoGrupoCamara);
							
							transaccionesMercadoFiltroDetalles.add(transaccionDetalle);
						}					  
				    }					
				}
				//** Crear metodo para llenar transaccionDetalle
				
		
				//RV tiene logica para dividendo
				if(validadores.CamaraRV.equals("S")) {
					
					//se obtienen los dividendos si existe camara RV
					String nombreGrupoCamara;
					Long codigoGrupoCamara;
					
					if(validadores.CambiosCCLV.equals("S")) {
						nombreGrupoCamara = nombreGrupoCamaraRV;
						codigoGrupoCamara = codigoGrupoCamaraRV;
					}
					else {
						nombreGrupoCamara = nombreGrupoCamaraRVPM;
						codigoGrupoCamara = codigoGrupoCamaraPHSM;
					}
					
					HashMap<String, Object> paramsDividendos = new HashMap<String, Object>();
					paramsDividendos.put("v_CodigoGrupoCamara", codigoGrupoCamara);
					paramsDividendos.put("v_NombreGrupoCamara", nombreGrupoCamara);
					paramsDividendos.put("p_FechaLiquidacion", in.p_FechaLiquidacion);

					clearingDividendos = sql.query(conn, SELECT_CONSULTAR_DIVIDENDO, paramsDividendos, ClearingFinancieroTransaccionesDividendos.class);
				}
				
				
				//Detalle Transacciones
				
				//SE AGREGAN LOS DETALLES
				List<ClearingFinancieroDetalle> listaDetalles = new ArrayList<ClearingFinancieroDetalle>();
				
				for(ClearingFinancieroTransaccionesDividendos transaccion : transaccionesMercadoFiltroDetalles) {
					ClearingFinancieroDetalle detalles = new ClearingFinancieroDetalle();
					
					detalles.IdTransaccion = transaccion.IdTransaccion;
					detalles.IdBolsa = transaccion.IdBolsa;
					detalles.CodigoGrupoCamara = transaccion.CodigoGrupoCamara;
					detalles.NombreGrupoCamara = transaccion.NombreGrupoCamara;
					detalles.RutContraparte = transaccion.RutContraparte;
					detalles.AccionTipoOperacion = transaccion.AccionTipoOperacion;
					if("COMPRA".equals(transaccion.AccionTipoOperacion)) {
						detalles.CompraVenta = "C";
					}else if ("VENTA".equals(transaccion.AccionTipoOperacion)){
						detalles.CompraVenta = "V";
					}
					detalles.TotalVenta = transaccion.TotalVenta;
					detalles.TotalCompra = transaccion.TotalCompra;
					detalles.RazonSocialContraparte = transaccion.RazonSocialContraparte;
					detalles.IdClasificacionTipoInstrumento = transaccion.IdClasificacionTipoInstrumento;
					detalles.FechaSistemaCreacion = transaccion.FechaSistemaCreacion;
					detalles.FolioTransaccion = transaccion.FolioTransaccion;
					detalles.IdCondicionDeLiquidacion = transaccion.IdCondicionDeLiquidacion;
					detalles.CodigoCondicionDeLiquidacion = transaccion.CodigoCondicionDeLiquidacion;
					detalles.IdMonedaTransaccion = transaccion.IdMonedaTransaccion != null ? transaccion.IdMonedaTransaccion : transaccion.IdMoneda;
					detalles.IdComprobanteTesoreria = transaccion.IdComprobanteTesoreria;
					detalles.Instrumento = transaccion.Instrumento;
					detalles.CantidadTransaccion = transaccion.CantidadTransaccion;
					detalles.PrecioTransaccion = transaccion.PrecioTransaccion;
					detalles.Tasa = transaccion.Tasa;
					detalles.MontoRetencion = transaccion.MontoRetencion;
					detalles.IdEventoCapital = transaccion.IdEventoCapital;
					detalles.IdEventoCapitalCliente = transaccion.IdEventoCapitalCliente;
					detalles.MontoRetencion = transaccion.MontoRetencion;
					
					listaDetalles.add(detalles);
				}
				
				salida.P_OUT_CLEARINGFINANCIERODETALLE.addAll(listaDetalles);
				
				
				//Resumen Transacciones
				List<ClearingFinancieroResumen> lstResumenClearingFinanciero = new ArrayList<ClearingFinancieroResumen>();
				
				
				// ***************************************************
				
					//Agregar logica de liquidaciones 
				HashMap<String, Object> paramsLiquidaciones = new HashMap<String, Object>();
				paramsLiquidaciones.put("p_FechaLiquidacion", in.p_FechaLiquidacion);
				paramsLiquidaciones.put("v_CodigoGrupoCamara", 4);
				paramsLiquidaciones.put("v_NombreGrupoCamara", nombreGrupoCamaraDV);
				
				
					//Consulta para realizar resumen de liquidaciones
				List<ClearingFinancieroLiquidacion> lstClearingFinancieroLiquidacion = new ArrayList<ClearingFinancieroLiquidacion>();
				
				//Si se contiene camara DV
				if(validadores.CamaraDV.equalsIgnoreCase("S")){
					lstClearingFinancieroLiquidacion = sql.query(conn, SELECT_CONSULTAR_LIQUIDACIONES, paramsLiquidaciones, ClearingFinancieroLiquidacion.class);
				
					for(ClearingFinancieroLiquidacion liquidacion : lstClearingFinancieroLiquidacion) {
						ClearingFinancieroResumen resumenLiquidacion = new ClearingFinancieroResumen();
						
						resumenLiquidacion.IdBolsa = liquidacion.IdBolsa;
						resumenLiquidacion.CodigoGrupoCamara = liquidacion.CodigoGrupoCamara;
						resumenLiquidacion.NombreGrupoCamara = liquidacion.NombreGrupoCamara;
						resumenLiquidacion.IdClasificacionTipoInstrumento = liquidacion.IdClasificacionTipoInstrumento;
						resumenLiquidacion.TotalCompra = liquidacion.TotalCompra;
						resumenLiquidacion.TotalVenta = liquidacion.TotalVenta;
						resumenLiquidacion.IdCorredorContraparte = liquidacion.IdCorredorContraparte;
						resumenLiquidacion.RutContraparte = liquidacion.RutContraparte;
						resumenLiquidacion.IdMonedaTransaccion = liquidacion.IdMonedaTransaccion;
						resumenLiquidacion.RazonSocialContraparte = liquidacion.RazonSocialContraparte;
						resumenLiquidacion.SaldoAcreedor = liquidacion.SaldoAcreedor;
						resumenLiquidacion.SaldoDeudor = liquidacion.SaldoDeudor;
						resumenLiquidacion.IdComprobanteTesoreria = liquidacion.IdComprobanteTesoreria;
						resumenLiquidacion.MontoRetencion = liquidacion.MontoRetencion;
						
						lstResumenClearingFinanciero.add(resumenLiquidacion);
					}
				}
				
				// ***************************************************
				
				List<ClearingFinancieroTransaccionesDividendos> listaTransaccionesDividendos = new ArrayList<ClearingFinancieroTransaccionesDividendos>();
				
				listaTransaccionesDividendos.addAll(transaccionesMercadoFiltroDetalles);
				listaTransaccionesDividendos.addAll(clearingDividendos);
				
				for(ClearingFinancieroTransaccionesDividendos transaccion : listaTransaccionesDividendos) {
					ClearingFinancieroResumen resumenClearing = new ClearingFinancieroResumen();
					
					resumenClearing.IdBolsa = transaccion.IdBolsa;
					resumenClearing.CodigoGrupoCamara = transaccion.CodigoGrupoCamara;
					resumenClearing.NombreGrupoCamara = transaccion.NombreGrupoCamara;
					resumenClearing.IdClasificacionTipoInstrumento = transaccion.IdClasificacionTipoInstrumento;
					resumenClearing.IdCorredorContraparte = transaccion.IdCorredorContraparte;
					resumenClearing.RutContraparte = transaccion.RutContraparte;
					resumenClearing.RazonSocialContraparte = transaccion.RazonSocialContraparte;
					resumenClearing.IdMonedaTransaccion = transaccion.IdMonedaTransaccion;
					resumenClearing.IdComprobanteTesoreria = transaccion.IdComprobanteTesoreria;
					resumenClearing.TotalVenta = transaccion.TotalVenta;
					resumenClearing.TotalCompra = transaccion.TotalCompra;
					resumenClearing.SaldoAcreedor = transaccion.SaldoAcreedor;
					resumenClearing.SaldoDeudor = transaccion.SaldoDeudor;
					resumenClearing.MontoRetencion = transaccion.MontoRetencion;
					
					lstResumenClearingFinanciero.add(resumenClearing);
				}
				
				if("S".equals(validadores.CamaraSM) || ("S".equalsIgnoreCase(usaCamaraHS) && "S".equals(validadores.CamaraSM))) {
					String nombreCamara = "S".equalsIgnoreCase(usaCamaraHS) ? nombreGrupoCamaraHS : nombreGrupoCamaraPHSM;
					Long codigoCamara = "S".equalsIgnoreCase(usaCamaraHS) ? codigoGrupoCamaraHS : codigoGrupoCamaraPHSM;
					HashMap<String, Object> params = new HashMap<String, Object>();
					params.put("p_IdEmpresa", in.p_IdEmpresa);
					params.put("p_FechaLiquidacion", in.p_FechaLiquidacion);
					params.put("p_CodigoGrupoCamara", codigoCamara);
					params.put("p_NombreGrupoCamara", nombreCamara);
					if (Fecha.toDate(in.p_FechaLiquidacion).equals(Fecha.toDate(fechaSistema)) || Fecha.toDate(in.p_FechaLiquidacion).after(Fecha.toDate(fechaSistema)) || "0001-01-01".equals(fechaSistema)){
						anticipos = sql.query(conn, SELECT_CONSULTAR_ANTICIPOS, params, AnticiposSimultaneas.class);
					}else {
						anticipos = sql.query(conn, SELECT_CONSULTAR_ANTICIPOS_HIS, params, AnticiposSimultaneas.class);
					}
					
					for(AnticiposSimultaneas anticipo: anticipos) {
						ClearingFinancieroSimultaneasDetalle detalle = new ClearingFinancieroSimultaneasDetalle();
						detalle.TipoOperacion= anticipo.TipoOperacion;
						detalle.FechaLiquidacion = anticipo.FechaLiquidacion;
						detalle.Nemotecnico = anticipo.Nemotecnico;
						detalle.Cantidad = anticipo.Cantidad;
						detalle.MontoCompra =  anticipo.AccionOperacion.equals("COMPRA") && anticipo.AccionOperacion != null ?  anticipo.Monto : BigDecimal.ZERO;		
						detalle.MontoVenta = anticipo.AccionOperacion.equals("VENTA") && anticipo.AccionOperacion != null ?  anticipo.Monto : BigDecimal.ZERO;
						detalle.NombreGrupoCamara = anticipo.NombreGrupoCamara;
						detalle.RutContraparte = anticipo.RutContraparte;
						detalle.IdClasificacionTipoInstrumento = anticipo.IdClasificacionTipoInstrumento;
						detalle.IdComprobanteTesoreria = anticipo.IdComprobanteTesoreria;
						detalle.IdBolsa = anticipo.IdBolsa;
						salida.P_OUT_CLEARINGFINANCIEROSIMULTANEAS.add(detalle);
						
						ClearingFinancieroResumen resumenClearing = new ClearingFinancieroResumen();
						
						resumenClearing.IdBolsa = anticipo.IdBolsa;
						resumenClearing.CodigoGrupoCamara = anticipo.CodigoGrupoCamara;
						resumenClearing.NombreGrupoCamara = anticipo.NombreGrupoCamara;
						resumenClearing.IdClasificacionTipoInstrumento = anticipo.IdClasificacionTipoInstrumento;
						resumenClearing.IdCorredorContraparte = anticipo.IdCorredorContraparte;
						resumenClearing.RutContraparte = anticipo.RutContraparte;
						resumenClearing.RazonSocialContraparte = anticipo.RazonSocialContraparte;
						resumenClearing.IdMonedaTransaccion = anticipo.IdMoneda;
						resumenClearing.IdComprobanteTesoreria = anticipo.IdComprobanteTesoreria;
						resumenClearing.TotalVenta = anticipo.AccionOperacion.equals("VENTA") && anticipo.AccionOperacion != null ?  anticipo.Monto : BigDecimal.ZERO;
						resumenClearing.TotalCompra = anticipo.AccionOperacion.equals("COMPRA") && anticipo.AccionOperacion != null ?  anticipo.Monto : BigDecimal.ZERO;
						resumenClearing.SaldoAcreedor = BigDecimal.ZERO;
						resumenClearing.SaldoDeudor = BigDecimal.ZERO;
						resumenClearing.MontoRetencion = BigDecimal.ZERO;
						
						lstResumenClearingFinanciero.add(resumenClearing);
					}
					
				}
				
				
				HashMap <String,ClearingFinancieroResumen> resumenClearingFinancieroHash = new HashMap<String,ClearingFinancieroResumen>();
				
				int indiceComparadorVentaCompraTransacciones;
				BigDecimal totalVentaResumen = BigDecimal.ZERO;
				BigDecimal totalCompraResumen = BigDecimal.ZERO;
						
				for (ClearingFinancieroResumen resumenTransaccion : lstResumenClearingFinanciero) {
					
					String key = resumenTransaccion.IdBolsa+resumenTransaccion.CodigoGrupoCamara+resumenTransaccion.NombreGrupoCamara+
							resumenTransaccion.IdClasificacionTipoInstrumento+resumenTransaccion.IdCorredorContraparte+
							resumenTransaccion.RutContraparte+resumenTransaccion.RazonSocialContraparte+resumenTransaccion.IdMonedaTransaccion+
							resumenTransaccion.IdComprobanteTesoreria;
					
					if(resumenClearingFinancieroHash.get(key) == null) {
						
						resumenClearingFinancieroHash.put(key, resumenTransaccion);
						totalVentaResumen 	= resumenClearingFinancieroHash.get(key).TotalVenta;
						totalCompraResumen 	= resumenClearingFinancieroHash.get(key).TotalCompra;
						
					} else {
						
						totalVentaResumen 	= resumenClearingFinancieroHash.get(key).TotalVenta.add(resumenTransaccion.TotalVenta);
						totalCompraResumen 	= resumenClearingFinancieroHash.get(key).TotalCompra.add(resumenTransaccion.TotalCompra);
						
						resumenClearingFinancieroHash.get(key).TotalVenta 		= resumenClearingFinancieroHash.get(key).TotalVenta.add(resumenTransaccion.TotalVenta);
						resumenClearingFinancieroHash.get(key).TotalCompra 		= resumenClearingFinancieroHash.get(key).TotalCompra.add(resumenTransaccion.TotalCompra);
						resumenClearingFinancieroHash.get(key).MontoRetencion 	= resumenClearingFinancieroHash.get(key).MontoRetencion.add(resumenTransaccion.MontoRetencion);
					}
						
					indiceComparadorVentaCompraTransacciones = resumenClearingFinancieroHash.get(key).TotalCompra.compareTo(resumenClearingFinancieroHash.get(key).TotalVenta);
		
					if(indiceComparadorVentaCompraTransacciones == 1) {
						
						resumenClearingFinancieroHash.get(key).SaldoDeudor 		= totalCompraResumen.subtract(totalVentaResumen);
						resumenClearingFinancieroHash.get(key).SaldoAcreedor 	= BigDecimal.ZERO;
					}
					else {
						
						resumenClearingFinancieroHash.get(key).SaldoAcreedor 	= totalVentaResumen.subtract(totalCompraResumen);
						resumenClearingFinancieroHash.get(key).SaldoDeudor 		= BigDecimal.ZERO;		
					}
						
				}
				
				
				//Resumen transacciones, dividendos y liquidaciones
				for(Map.Entry<String, ClearingFinancieroResumen> obj : resumenClearingFinancieroHash.entrySet()){
			          salida.P_OUT_CLEARINGFINANCIERORESUMEN.add(obj.getValue());
		        }
				
				
				//Resumen Dividendos
				salida.P_OUT_CLEARINGFINANCIERODIVIDENDOS.addAll(clearingDividendos);
				
				//Consulta para obtener detalles de liquidaciones 
				salida.P_OUT_CLEARINGFINANCIEROLIQUIDACIONES.addAll(lstClearingFinancieroLiquidacion);
				
			}
		} 
		catch (SQLException e) {
			salida.P_OUT_MENSAJES.add(new MensajeSalida(1L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_AL_CONSULTAR_CLEARING", e.getMessage()));
		}
		
		return salida;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa02", "connections.properties")) {   // PlataformaUtils.getBolsaConnection()) {
			Gson gson = Utils.getGsonBuilder();
			String jsonPE = "{\"p_ConsultarEnrolamiento\":[{\"IdEnrolamiento\":0,\"IdEmpresa\":1,\"IdUsuario\":103,\"IdTipoEntidad\":\"CLIENTE\",\"IdEntidadEnrolada\":0,\"IdTipoDocumentoIdentidad\":\"IUP\",\"IdTipoDocumentoIdentidadAsociado\":\"FIC\",\"DocumentoIdentidad\":\"22000000002620\",\"IdTipoDocumentoIdentidadAdicional\":\"RUT\",\"DocumentoIdentidadAdicional\":\"00000000262\",\"Tipo\":\"J\",\"Nombres\":\"\",\"ApellidoPaterno\":\"\",\"ApellidoMaterno\":\"\",\"RazonSocial\":\"PERSONA JURIDICA FICTICIO\",\"IdPaisNacimiento\":0,\"FechaNacimiento\":\"0001-01-01T03:00:00.000Z\",\"Sexo\":\"M\",\"EstadoCivil\":\"\",\"IdCiudad\":1660024,\"IdDivisionPais\":1661835,\"IdTipoTelefono\":\"FIJO\",\"Telefono\":\"000000000\",\"Email\":\"HOLAHOLA@CHAO.COM\",\"DireccionCalle\":\"DIRECCION\",\"DireccionNumero\":\"666\",\"DireccionDepartamento\":\"\",\"FechaCreacion\":\"2018-06-01T20:14:48.416Z\",\"FechaModificacion\":\"2018-06-01T20:14:48.416Z\",\"IdPersonaConyuge\":0,\"RegimenConyugal\":\"\",\"CLI_IdCategoriaInversionista\":\"AGRESIVO\",\"CLI_IdTipoCliente\":\"CLIENTE AGENCIA\",\"CLI_IdClaseCliente\":\"INTERMEDIARIO\",\"CLI_IdEmpleadoEjecutivo\":271,\"CLI_CodigoAdicionalHolding\":\"\",\"CLI_ClienteFinal\":\"N\",\"POR_IdPerfilComercial\":\"MODELO AGRESIVO 01\",\"POR_IdPersonaBrokerExtranjero\":0,\"EMP_CodigoEmpleado\":\"\",\"EMP_CodigoTipoCargoEmpleado\":\"\",\"EMP_IdEmpleadoSupervisor\":0,\"EMP_IdSucursal\":0,\"EMP_FechaInicioCargo\":\"0001-01-01\",\"EMP_FechaTerminoCargo\":\"0001-01-01\",\"CNT_CodigoInscripcion\":\"\",\"CNT_Emisor\":\"\",\"CNT_Depositos\":\"\",\"CNT_Bolsa\":\"\",\"CNT_Corredora\":\"\",\"CNT_CamaraDeCompensacion\":\"\",\"CNT_Banco\":\"\",\"CNT_Boveda\":\"\",\"CNT_Otros\":\"\",\"PRO_IdAreaProveedor\":0,\"EMP_CargoIndefinido\":\"\",\"EsRutInterno\":\"S\"}],\"p_IdEmpresa\":1,\"p_IdUsuario\":103,\"p_IdModulo\":\"SOC\"}";
			String jsonCL = "{\r\n"
					+ "    \"p_IdEmpresa\": 1,\r\n"
					+ "    \"p_IdUsuario\": 9863,\r\n"
					+ "    \"p_IdModulo\": \"TES\",\r\n"
					+ "    \"p_UsaReglasNegocio\": \"S\",\r\n"
					+ "    \"p_FechaLiquidacion\": \"2026-08-24\",\r\n"
					+ "    \"p_Camaras\": [\r\n"
					+ "        {\r\n"
					+ "            \"Camara\": \"RV\"\r\n"
					+ "        },\r\n"
					+ "        {\r\n"
					+ "            \"Camara\": \"PM\"\r\n"
					+ "        },\r\n"
					+ "        {\r\n"
					+ "            \"Camara\": \"PH\"\r\n"
					+ "        },\r\n"
					+ "        {\r\n"
					+ "            \"Camara\": \"SM\"\r\n"
					+ "        },\r\n"
					+ "        {\r\n"
					+ "            \"Camara\": \"DV\"\r\n"
					+ "        },\r\n"
					+ "        {\r\n"
					+ "            \"Camara\": \"HS\"\r\n"
					+ "        }\r\n"
					+ "    ]\r\n"
					+ "}";
			
			//{"p_IdEmpresa":1,"p_IdUsuario":14,"p_IdModulo":"TES","p_UsaReglasNegocio":"S","p_FechaLiquidacion":"2020-04-01","p_Camaras":[{"Camara":"RV"},{"Camara":"PM"},{"Camara":"PH"},{"Camara":"SM"},{"Camara":"DV"}]}
			
			EntradaClearingFinanciero Objin = gson.fromJson(jsonCL, EntradaClearingFinanciero.class);

			ConsultarClearingFinanciero demo = new ConsultarClearingFinanciero();
			System.out.println("ENTRADA");
			System.out.println(Objin.toString());
			
			try {
				SalidaClearingFinanciero ObjOut = demo.logica(con, Objin);
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
