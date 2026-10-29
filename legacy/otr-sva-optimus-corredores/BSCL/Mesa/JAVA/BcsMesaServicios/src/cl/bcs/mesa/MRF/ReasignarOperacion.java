package cl.bcs.mesa.MRF;

import java.math.BigDecimal;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.mesa.MRF.ActualizarPactoOperacionMesa.ActualizarPactoOperacionMesa_IN;
import cl.bcs.mesa.MRF.ActualizarPactoOperacionMesa.Operacion;
import cl.bcs.mesa.MRF.CrearOperacionMesa.CrearOperacionMesa_DetalleOperacion_IN;
import cl.bcs.mesa.MRF.CrearOperacionMesa.CrearOperacionMesa_DetalleOperacion_OUT;
import cl.bcs.mesa.MRF.CrearOperacionMesa.CrearOperacionMesa_FormaPagoOperacion_IN;
import cl.bcs.mesa.MRF.CrearOperacionMesa.CrearOperacionMesa_FormaPagoOperacion_OUT;
import cl.bcs.mesa.MRF.CrearOperacionMesa.CrearOperacionMesa_IN;
import cl.bcs.mesa.MRF.CrearOperacionMesa.CrearOperacionMesa_OUT;
import cl.bcs.mesa.MRF.CrearOperacionMesa.CrearOperacionMesa_Operacion_IN;
import cl.bcs.mesa.MRF.IngresarDetalleOperacionMesa.IngresarDetalleOperacionMesa_DetalleOperacion_IN;
import cl.bcs.mesa.MRF.IngresarDetalleOperacionMesa.IngresarDetalleOperacionMesa_IN;
import cl.bcs.mesa.MRF.IngresarDetalleOperacionMesa.IngresarDetalleOperacionMesa_OUT;
import cl.bcs.mesa.MRF.IngresarDetalleOperacionMesa.IngresarDetalleOperacionMesa_Operacion_IN;
import cl.bcs.mesa.MRF.ObtenerCarterasRetirar.CarteraRetiro;
import cl.bcs.mesa.MRF.ObtenerCarterasRetirar.EntradaObtenerCarterasRetirar;
import cl.bcs.mesa.MRF.ObtenerCarterasRetirar.SalidaObtenerCarterasRetirar;
import cl.bcs.mesa.MRF.ValidarReasignacionMesaRF.EntradaValidarReasignacionMesaRF;
import cl.bcs.mesa.MRF.ValidarReasignacionMesaRF.SalidaValidarReasignacionMesaRF;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.ObtenerNemoSVS;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog;
import cl.bcs.plataforma.pla.ObtenerNemoSVS.Instrumento_Entrada;
import cl.bcs.plataforma.pla.ObtenerNemoSVS.ObtenerNemoSVS_Entrada;
import cl.bcs.plataforma.pla.ObtenerNemoSVS.ObtenerNemoSVS_Salida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ReasignarOperacion implements JavaCall {

	static final Logger logger = LogManager.getLogger(ValidarReasignacionMesaRF.class);

	private final String SQL_INSERT_OPERACION_REASIGNADA = loadResourceToString("cl/bcs/mesa/MRF/sql/ReasignarOperacion_InsertOperacionReasignada.sql");
	private final String SQL_INSERT_DETALLE_OPERACION_REASIGNADA = loadResourceToString("cl/bcs/mesa/MRF/sql/ReasignarOperacion_InsertDetalleOperacionReasignada.sql");
	private final String SQL_INSERT_COMPROBANTE_PAGO_OPERACION_REASIGNADA = loadResourceToString("cl/bcs/mesa/MRF/sql/ReasignarOperacion_InsertComprobantePagoOperacionReasignada.sql");
	private final String SQL_SELECT_OPERACION_ORIGINAL = loadResourceToString("cl/bcs/mesa/MRF/sql/ReasignarOperacion_SelectOperacionOriginal.sql");
	private final String SQL_SELECT_OPERACION_CARTERA = loadResourceToString("cl/bcs/mesa/MRF/sql/ReasignarOperacion_SelectCarteraOperacionReasignada.sql");
	private final String SQL_UPDATE_IDREASIGNACION = loadResourceToString("cl/bcs/mesa/MRF/sql/ReasignarOperacion_UpdateIdReasignacion.sql");
	
	public Long IdOperacionNueva = 0L;
	
	public static class EntradaReasignarOperacion extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public Long p_IdOperacion;
		public Long p_CantidadAProcesar;
		public List<Carteras> p_Carteras;

		public EntradaReasignarOperacion(Long p_IdEmpresa, Long p_IdUsuario, String p_IdModulo, Long p_IdOperacion,	List<Carteras> p_Carteras) {
			this.p_IdEmpresa = p_IdEmpresa;
			this.p_IdUsuario = p_IdUsuario;
			this.p_IdModulo = p_IdModulo;
			this.p_IdOperacion = p_IdOperacion;
			this.p_Carteras = p_Carteras;
		}
	}

	public static class Carteras extends BcsPojo {
        public Long IdCliente;
        public Long IdPortafolioCliente;
        public Long FolioCartera;
        public Long FolioOperacion;
        public BigDecimal CantidadAsignada;
        public BigDecimal MontoAsignado;
        public Long IdDetalle;
        public String TipoOperacion;
        public String CondicionLiquidacion;
        public String TipoReajuste;
        public BigDecimal ValorInicial;
        public String Mercado;
        public Long SecuenciaCartera;
        public String Nemotecnico;
        public String Emisor;
        public String Boveda;
        public String ReajusteCartera;
        public String MonedaLiquidacion;
        public String FechaVencimiento;
        public BigDecimal Cantidad;
        public BigDecimal ValorRescate;
        public String Fungible;
        public BigDecimal Monto;
        public Long IdPortafolio;
        public BigDecimal Tasa;
        public String IdTipoInstrumento;
        public Long IdInstrumento;
        public String FechaEmisionInstrumento;
        public String IdMonedaInstrumento;
        public String IdTipoReajuste;
        public BigDecimal TasaCompra;
        public String IdMonedaEmision;
        public String IdClasificacionTipoInstrumento;
        public String IdBolsa;
        public Long IdCartera;
        public BigDecimal CorteMinimo;
        public BigDecimal MontoValorizado;
        public BigDecimal MontoLiquidacionValorizado;
        public BigDecimal MontoLiquidacionOperacionValorizado;
        public BigDecimal PrecioCompraValorizado;
        public BigDecimal TasaCompraValorizado;
        public BigDecimal PrecioMercadoValorizado;
        public BigDecimal TasaMercadoValorizado;
        public Boolean AfectaCustodia;
        public List<OperacionTerminoDetalle> Detalle;
    }
	
	public static class OperacionOriginal extends BcsPojo {
		public Long FolioCarteraCustodia;
		public Long IdClienteCorredor;
		public Long IdPortafolioCorredor;
		public Long IdMesa;
		public String AfectaCustodia;
		public String IdTipoOperacion;
		public String TipoOperacion;
		public String IdTipoReajuste;
		public String IdTipoReajusteDetalle;
		public String IdMoneda;
		public String MonedaLiquidacion;
		public Date FechaLiquidacion;
		public String IdCondicionLiquidacion;
		public Long IdInstrumento;
		public String EsFungible;
		public Long IdCartera;
		public BigDecimal Tasa;
		public BigDecimal TasaMercado;
		public BigDecimal CorteMinimo;
		public Date FechaVencimiento;
		public Date FechaVencimientoInstrumento;
		public String IdBolsa;
		public String IdBoveda;
		public String IdClasificacionTipoInstrumento;
		public String IdMonedaEmisionInstrumento;
		public String IdMonedaLiquidacionInstrumento;
		public String IdMonedaLiquidacionOperacion;
		public BigDecimal Precio;
		public BigDecimal PrecioMercado;
		public BigDecimal ValorRescate;
		public BigDecimal INGMontoAPlazo;
		public BigDecimal INGMontoAPlazoMonedaInstrumento;
	}

	public static class OperacionTerminoDetalle extends BcsPojo {
		public Long IdDetalle;
		public String TipoOperacion;
		public String CondicionLiquidacion;
		public String TipoReajuste;
		public BigDecimal ValorInicial;
		public String Mercado;
		public Long SecuenciaCartera;
		public String Nemotecnico;
		public String Emisor;
		public String Boveda;
		public String ReajusteCartera;
		public String MonedaLiquidacion;
		public String FechaVencimiento;
		public BigDecimal Cantidad;
		public BigDecimal ValorRescate;
		public String Fungible;
		public BigDecimal Monto;
		public Long IdPortafolio;
		public BigDecimal Tasa;
		public String IdTipoInstrumento;
		public Long IdInstrumento;
		public String FechaEmisionInstrumento;
		public String IdMonedaInstrumento;
		public String IdTipoReajuste;
		public BigDecimal TasaCompra;
		public String IdMonedaEmision;
		public String IdClasificacionTipoInstrumento;
		public String IdBolsa;
		public Long IdCartera;
		public BigDecimal CorteMinimo;
		public BigDecimal MontoValorizado;
		public BigDecimal MontoLiquidacionValorizado;
		public BigDecimal MontoLiquidacionOperacionValorizado;
		public BigDecimal PrecioCompraValorizado;
		public BigDecimal TasaCompraValorizado;
		public BigDecimal PrecioMercadoValorizado;
		public BigDecimal TasaMercadoValorizado;
		
		public OperacionTerminoDetalle() {
			this.IdCartera = 0L;
			this.IdBolsa = "";
			this.MontoValorizado = BigDecimal.ZERO;
			this.MontoLiquidacionValorizado = BigDecimal.ZERO;
			this.MontoLiquidacionOperacionValorizado = BigDecimal.ZERO;
			this.PrecioCompraValorizado = BigDecimal.ZERO;
			this.TasaCompraValorizado = BigDecimal.ZERO;
			this.PrecioMercadoValorizado = BigDecimal.ZERO;
			this.TasaMercadoValorizado = BigDecimal.ZERO;
		}
	}
	
	public static class OperacionReasignacion extends BcsPojo {
		public Long IdOperacionNueva;
		public Long FolioOperacionNueva;
		public Long IdOperacionOriginal;
		public Long IdReservaNueva;
		public String Estado;
		public Long IdComprobantePagoOperacion;
	}
	
	public static class OperacionCustodia extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public Long p_IdOperacion;
	}
	
	public static class IdOperacionOriginal extends BcsPojo {
		public Long IdOperacion;
	}

	public static class SalidaReasignarOperacion extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;

		public SalidaReasignarOperacion() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaReasignarOperacion jin = gson.fromJson(in.toString(), EntradaReasignarOperacion.class);
		SalidaReasignarOperacion out = logica(conn, jin);

		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public SalidaReasignarOperacion logica(Connection con, EntradaReasignarOperacion in) throws Exception {
		logger.info("INICIO::ReasignarOperacion");
		SQL _sql = new SQL();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		
		List<Long> secuenciaIdOperacion = Utils.getSecuencia(con, "BSCL.Mesa.BD.Secuencias::Operacion", 1L);	
		
		SalidaReasignarOperacion out = new SalidaReasignarOperacion();
		ValidarReasignacionMesaRF validarReasignacionMesaRF = new ValidarReasignacionMesaRF();
		for(Carteras cartera: in.p_Carteras) {
			EntradaValidarReasignacionMesaRF entradaValidarReasignacionMesaRF = new EntradaValidarReasignacionMesaRF(in.p_IdEmpresa,in.p_IdUsuario, in.p_IdModulo, cartera.FolioOperacion, cartera.FolioCartera, cartera.CantidadAsignada, true, in.p_CantidadAProcesar, cartera.IdInstrumento, cartera.IdCliente, cartera.IdPortafolio, cartera.TipoOperacion, cartera.AfectaCustodia);
			SalidaValidarReasignacionMesaRF salidaValidarReasignacionMesaRF = validarReasignacionMesaRF.logica(con, entradaValidarReasignacionMesaRF);
			if(salidaValidarReasignacionMesaRF.P_OUT_ERRORESCONTADOR != 0) {
				out.P_OUT_ERRORESCONTADOR = out.P_OUT_ERRORESCONTADOR + 1L;	
				for(MensajeSalida mensaje : salidaValidarReasignacionMesaRF.P_OUT_MENSAJES) {
					out.P_OUT_MENSAJES.add(mensaje);	
				}				
			}
		}
		
		/*
		 * Procesa Operaciones despues de validar
		 */
		if(out.P_OUT_ERRORESCONTADOR == 0) {
			//Anula Operacion original
			AnularOperacionMesa anularOperacionMesa = new AnularOperacionMesa();
			AnularOperacionMesa.AnularOperacionMesa_IN anularEntrada = new AnularOperacionMesa.AnularOperacionMesa_IN();
			AnularOperacionMesa.AnularOperacionMesa_OUT anularSalida = new AnularOperacionMesa.AnularOperacionMesa_OUT();
			anularEntrada.p_IdEmpresa = in.p_IdEmpresa;
			anularEntrada.p_IdModulo = in.p_IdModulo;
			anularEntrada.p_IdUsuario = in.p_IdUsuario;
			anularEntrada.p_UsaReglasNegocio = "S";
			
			AnularOperacionMesa.Operacion anularOperacion = new AnularOperacionMesa.Operacion();
			anularOperacion.IdOperacion = in.p_IdOperacion;
			anularOperacion.IdEstadoSiguiente = "EMI";
			List<AnularOperacionMesa.Operacion> listAnularOperacion = new ArrayList<AnularOperacionMesa.Operacion>();
			listAnularOperacion.add(anularOperacion);
			anularEntrada.p_Operaciones = listAnularOperacion;
			anularSalida = anularOperacionMesa.logica(con, anularEntrada);
			
			out.P_OUT_ERRORESCONTADOR = anularSalida.P_OUT_ERRORESCONTADOR;	
			out.P_OUT_MENSAJES = anularSalida.P_OUT_MENSAJES;
			
			if(out.P_OUT_ERRORESCONTADOR == 0) {
			
				//Crea Operacion Reversa				
				OperacionReasignacion operacionReasignacion = new OperacionReasignacion();
				List<OperacionReasignacion> operacionReasignaciones = new ArrayList<OperacionReasignacion>();
				
				operacionReasignacion.IdOperacionNueva = secuenciaIdOperacion.get(0);
				operacionReasignacion.IdOperacionOriginal = in.p_IdOperacion;
				operacionReasignacion.Estado = "RGN";
				
				operacionReasignaciones.add(operacionReasignacion);
				_sql.batch(con, SQL_INSERT_OPERACION_REASIGNADA, operacionReasignaciones, OperacionReasignacion.class);
				_sql.batch(con, SQL_INSERT_DETALLE_OPERACION_REASIGNADA, operacionReasignaciones, OperacionReasignacion.class);
				_sql.batch(con, SQL_INSERT_COMPROBANTE_PAGO_OPERACION_REASIGNADA, operacionReasignaciones, OperacionReasignacion.class);

				//Crea Operaciones Nuevas
				HashMap<String,Object> paramsOpe = new HashMap<>();
				paramsOpe.put("IdOperacion", in.p_IdOperacion);
				List<OperacionOriginal> operacionOriginal = _sql.query(con, SQL_SELECT_OPERACION_ORIGINAL, paramsOpe, OperacionOriginal.class);
				
				HashMap<Long,OperacionOriginal> operacion = new HashMap<>();
				for(OperacionOriginal ope : operacionOriginal) {
					operacion.put(ope.FolioCarteraCustodia, ope);
				}				
				//Agrega Operaciones por cada cartera
				for(Carteras cartera : in.p_Carteras) {
					
					IngresarDetalleOperacionMesa ingresarDetalleOperacionMesa = new IngresarDetalleOperacionMesa();
					
					CrearOperacionMesa_OUT salidaCrearOperacion = new CrearOperacionMesa_OUT();
					this.IdOperacionNueva = 0L;

					//Crea Operacion
					
					List<CrearOperacionMesa_FormaPagoOperacion_OUT> formaPago = new ArrayList<CrearOperacionMesa_FormaPagoOperacion_OUT>();
					List<CarteraRetiro> carteraFifo = new ArrayList<CarteraRetiro>();
					HashMap<Long,Long> reservas = new HashMap<>();
					HashMap<Long,String> fechaVencimientoInst = new HashMap<>();
					String fechaVencimiento = calculaFechaVencimiento(fechaSistema, operacion.get(cartera.FolioCartera).FechaVencimiento.toString());
					
					cartera.FechaVencimiento = fechaVencimiento;
					//Agrega Cabecera
					CrearOperacionMesa_IN entradaCrearOperacion = crearOperacionData(in, fechaVencimientoInst, cartera, carteraFifo, reservas, operacion.get(cartera.FolioCartera), formaPago, fechaSistema, "CREAR", "");	
					CrearOperacionMesa crearOperacionMesa = new CrearOperacionMesa();
					salidaCrearOperacion = crearOperacionMesa.logica(con, entradaCrearOperacion);
					
					out.P_OUT_ERRORESCONTADOR = salidaCrearOperacion.P_OUT_ERRORESCONTADOR;	
					out.P_OUT_MENSAJES = salidaCrearOperacion.P_OUT_MENSAJES;
					
					this.IdOperacionNueva = salidaCrearOperacion.P_OUT_OPERACION.get(0).IdOperacion;
				
					if (salidaCrearOperacion.P_OUT_ERRORESCONTADOR == 0) {
						
						if(esRetiroCustodia(cartera) ) {
							List<ObtenerCarterasRetirar.Resultado> carterasRetiroFifo = obtenerCarteraRetirar(con, in, cartera, operacion.get(cartera.FolioCartera));
							Map<Long, ObtenerCarterasRetirar.Resultado> resultadosPorIdInterno = carterasRetiroFifo.stream().collect(Collectors.toMap(resultado->resultado.IdInterno, resultado->resultado));
							ObtenerCarterasRetirar.Resultado resultado = resultadosPorIdInterno.get(0L);
							carteraFifo = resultado.CarterasRetiro;
							if(resultado == null || "ERR".equals(resultado.IdEstado)) {
								out.P_OUT_ERRORESCONTADOR = out.P_OUT_ERRORESCONTADOR + 1L;
								out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "OPERACION_TERMINO_SIN_CARTERAS","Cliente sin carteras disponibles"));
								return out;
							}
						}
						IngresarDetalleOperacionMesa_IN entradaIngresarDetalleOperacionMesa = crearDetalleData(con, in, cartera, carteraFifo, operacion.get(cartera.FolioCartera), fechaSistema);
						//Si es con custodia revisa carteras disponibles, si es Nominal crea el detalle
						if(!entradaIngresarDetalleOperacionMesa.p_DetalleOperacion.isEmpty()) {
							IngresarDetalleOperacionMesa_OUT salidaIngresarDetalle = ingresarDetalleOperacionMesa.logica(con, entradaIngresarDetalleOperacionMesa);
							HashMap<String,Object> params = new HashMap<>();
							for(CarteraRetiro carteras : carteraFifo) {
								params.put("IdCartera", carteras.IdCartera);
								params.put("FolioCartera", carteras.FolioCartera);
								List<OperacionOriginal> operacionCartras = _sql.query(con, SQL_SELECT_OPERACION_CARTERA, params, OperacionOriginal.class);
								
								for(OperacionOriginal cart : operacionCartras) {
									fechaVencimientoInst.put(cart.IdCartera,cart.FechaVencimientoInstrumento.toString());
								}
							}
						
							for(CrearOperacionMesa_DetalleOperacion_OUT salida: salidaIngresarDetalle.P_OUT_DETALLEOPERACION) {
								
								reservas.put(salida.FolioCartera, salida.IdReserva);
							}
							
							out.P_OUT_ERRORESCONTADOR = salidaIngresarDetalle.P_OUT_ERRORESCONTADOR;
							out.P_OUT_MENSAJES = salidaIngresarDetalle.P_OUT_MENSAJES;
							
							//Crea Operacion Completa
							CrearOperacionMesa_IN entradaCrearOperacion2 = crearOperacionData(in, fechaVencimientoInst, cartera, carteraFifo, reservas,  operacion.get(cartera.FolioCartera), salidaCrearOperacion.P_OUT_FORMASDEPAGO, fechaSistema, "CERRAR", "PEN");						
							salidaCrearOperacion = crearOperacionMesa.logica(con, entradaCrearOperacion2);
								
							out.P_OUT_ERRORESCONTADOR = salidaCrearOperacion.P_OUT_ERRORESCONTADOR;
							out.P_OUT_MENSAJES = salidaCrearOperacion.P_OUT_MENSAJES;
							
							//consultar si se crea operacion aunque las formas de pago tengan problemas
							operacionReasignacion.IdOperacionNueva = this.IdOperacionNueva;
							operacionReasignacion.IdOperacionOriginal = secuenciaIdOperacion.get(0);
							
							_sql.batch(con, SQL_UPDATE_IDREASIGNACION, operacionReasignaciones, OperacionReasignacion.class);
							
							out.P_OUT_ERRORESCONTADOR = 0L;
							
							agregarLogSistema(con,in, fechaSistema, operacionOriginal, operacionReasignacion);
							
							out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, "REASIGNACION_VALIDA_NUEVA","La reasignacion fue realizada con exito"));		
						} else {
							out.P_OUT_ERRORESCONTADOR = out.P_OUT_ERRORESCONTADOR + 1L;
							out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "OPERACION_TERMINO_SIN_CARTERAS","Cliente sin carteras disponibles"));
						}
					} 
				}		
			}
		}	
		logger.info("FIN::ReasignarOperacion");
		return out;
	}
	
	private CrearOperacionMesa_IN crearOperacionData (EntradaReasignarOperacion in, HashMap<Long,String> fechaVencimiento,Carteras cartera, List<ObtenerCarterasRetirar.CarteraRetiro> carterasFifo, HashMap<Long,Long> reservas, OperacionOriginal operacion, List<CrearOperacionMesa_FormaPagoOperacion_OUT> formaPago, String fechaSistema, String accion, String estado) {
		CrearOperacionMesa_IN entradaCrearOperacion = new CrearOperacionMesa_IN();

			// Crea Operacion
			entradaCrearOperacion.p_IdEmpresa = in.p_IdEmpresa;
			entradaCrearOperacion.p_IdModulo = in.p_IdModulo;
			entradaCrearOperacion.p_IdUsuario = in.p_IdUsuario;
			entradaCrearOperacion.p_UsaReglasNegocio = accion == "CERRAR" ? "S":"N";
			

			CrearOperacionMesa_Operacion_IN operacionCrearOperacionMesa = new CrearOperacionMesa_Operacion_IN();
			operacionCrearOperacionMesa.TasaAnualizada = BigDecimal.ZERO;
			operacionCrearOperacionMesa.CostoFondo = "0";
			operacionCrearOperacionMesa.UtilidadPerdida = BigDecimal.ZERO;
			operacionCrearOperacionMesa.UtilidadFinal = BigDecimal.ZERO;
			operacionCrearOperacionMesa.Accion = accion;
			operacionCrearOperacionMesa.IdInternoOperacion = 0L;
			operacionCrearOperacionMesa.IdOperacion = accion == "CERRAR" ? this.IdOperacionNueva:0L;
			operacionCrearOperacionMesa.FolioOperacion = 0L;
			operacionCrearOperacionMesa.FolioCartera = 0L;
			operacionCrearOperacionMesa.IdCliente = cartera.IdCliente;
			operacionCrearOperacionMesa.IdPortafolio = cartera.IdPortafolioCliente;
			operacionCrearOperacionMesa.IdClienteCorredor = operacion.IdClienteCorredor;
			operacionCrearOperacionMesa.IdPortafolioCorredor = operacion.IdPortafolioCorredor;
			operacionCrearOperacionMesa.Intermesa = "N";
			operacionCrearOperacionMesa.IdMesaOrigen = operacion.IdMesa;
			operacionCrearOperacionMesa.IdMesaDestino = operacion.IdMesa;
			operacionCrearOperacionMesa.AfectaCustodia = operacion.AfectaCustodia;//revisar deberia ser S o N
			operacionCrearOperacionMesa.IdTipoProducto = "MERF";
			operacionCrearOperacionMesa.IdTipoOperacion = operacion.IdTipoOperacion;
			operacionCrearOperacionMesa.IdTipoReajuste = operacion.IdTipoReajuste;
			operacionCrearOperacionMesa.Plazo = 0L;
			operacionCrearOperacionMesa.Monto = cartera.MontoAsignado;
			operacionCrearOperacionMesa.MontoMoneda = cartera.MontoAsignado;
			operacionCrearOperacionMesa.IdMoneda = operacion.IdMoneda;
			operacionCrearOperacionMesa.IdMonedaLiquidacion = operacion.MonedaLiquidacion;
			operacionCrearOperacionMesa.MontoLiquidacion = cartera.MontoAsignado;
			operacionCrearOperacionMesa.Tasa = BigDecimal.ZERO;
			operacionCrearOperacionMesa.ValorFinal = cartera.MontoAsignado;
			operacionCrearOperacionMesa.ValorFinalMoneda = cartera.MontoAsignado;
			operacionCrearOperacionMesa.TasaAgente = BigDecimal.ZERO;
			operacionCrearOperacionMesa.FechaOperacion = fechaSistema;
			operacionCrearOperacionMesa.FechaVencimiento = cartera.FechaVencimiento;
			operacionCrearOperacionMesa.IdCondicionLiquidacion = operacion.IdCondicionLiquidacion;
			operacionCrearOperacionMesa.FechaLiquidacion = cartera.FechaVencimiento;
			operacionCrearOperacionMesa.IdAgente = 0L;
			operacionCrearOperacionMesa.IdOperador = entradaCrearOperacion.p_IdUsuario;
			operacionCrearOperacionMesa.IdEstado = estado;
			operacionCrearOperacionMesa.Glosa = "";
			operacionCrearOperacionMesa.IdTipoCanalIngreso = "AGE";
			operacionCrearOperacionMesa.TipoDocumento = "";
			operacionCrearOperacionMesa.IdContacto = 0L;
			operacionCrearOperacionMesa.GuardandoCabecera = accion == "CERRAR" ? "N":"S";
			entradaCrearOperacion.p_Operacion = Arrays.asList(operacionCrearOperacionMesa);
		
			if (accion == "CERRAR") {
				if(esRetiroCustodia(cartera) ) {
					Long IdInternoDetalle = 0L;
					for(ObtenerCarterasRetirar.CarteraRetiro cart : carterasFifo) {
						IdInternoDetalle = IdInternoDetalle +1;
						CrearOperacionMesa_DetalleOperacion_IN operacionCrearDetalleMesa = new CrearOperacionMesa_DetalleOperacion_IN();
						operacionCrearDetalleMesa.IdInternoDetalle = IdInternoDetalle;
						operacionCrearDetalleMesa.IdInternoOperacion = 0L;
						operacionCrearDetalleMesa.IdReserva = reservas.get(cart.FolioCartera);
						operacionCrearDetalleMesa.IdCliente = cartera.IdCliente;
						operacionCrearDetalleMesa.IdPortafolio = cartera.IdPortafolioCliente;
						operacionCrearDetalleMesa.IdClienteCorredor = operacion.IdClienteCorredor;
						operacionCrearDetalleMesa.IdPortafolioCorredor = operacion.IdPortafolioCorredor;
						operacionCrearDetalleMesa.IdMesa = operacion.IdMesa;
						operacionCrearDetalleMesa.IdTipoProducto = "MERF";
						operacionCrearDetalleMesa.IdTipoOperacion = operacion.IdTipoOperacion;
						operacionCrearDetalleMesa.IdInstrumento = operacion.IdInstrumento;
						operacionCrearDetalleMesa.Nemotecnico = cartera.Nemotecnico;
						operacionCrearDetalleMesa.EsFungible = operacion.EsFungible;
						operacionCrearDetalleMesa.IdBolsa = operacion.IdBolsa;
						operacionCrearDetalleMesa.IdBoveda = operacion.IdBoveda;
						operacionCrearDetalleMesa.IdCartera = cart.IdCartera;
						operacionCrearDetalleMesa.FolioCartera = cart.FolioCartera;
						operacionCrearDetalleMesa.CodigoIFRS = "T";
						operacionCrearDetalleMesa.IdEmisor = cartera.Emisor;
						operacionCrearDetalleMesa.IdMoneda = operacion.IdMoneda;
						operacionCrearDetalleMesa.IdTipoReajuste = operacion.IdTipoReajusteDetalle;
						operacionCrearDetalleMesa.IdMonedaEmisionInstrumento = operacion.IdMonedaEmisionInstrumento;
						operacionCrearDetalleMesa.IdMonedaLiquidacionInstrumento = operacion.IdMonedaLiquidacionInstrumento;
						operacionCrearDetalleMesa.IdMonedaLiquidacionOperacion = operacion.IdMonedaLiquidacionOperacion;
						operacionCrearDetalleMesa.CorteMinimo = operacion.CorteMinimo;
						operacionCrearDetalleMesa.Cantidad = cart.SaldoRetiro;
						operacionCrearDetalleMesa.Precio = operacion.Precio;
						operacionCrearDetalleMesa.PrecioMercado = operacion.PrecioMercado;
						operacionCrearDetalleMesa.Tasa = operacion.Tasa;
						operacionCrearDetalleMesa.TasaMercado = operacion.TasaMercado;
						operacionCrearDetalleMesa.Monto = cartera.MontoAsignado;
						operacionCrearDetalleMesa.MontoLiquidacion = cartera.MontoAsignado;
						operacionCrearDetalleMesa.MontoLiquidacionOperacion = cartera.MontoAsignado;
						operacionCrearDetalleMesa.ValorRescate = operacion.ValorRescate;
						operacionCrearDetalleMesa.FechaVencimiento = fechaVencimiento.get(cart.IdCartera).toString();
						operacionCrearDetalleMesa.MontoValorizadoReserva = BigDecimal.ZERO;
						operacionCrearDetalleMesa.TipoCarteraCorredor = "";
						operacionCrearDetalleMesa.INGMontoAPlazoDetalle = operacion.INGMontoAPlazo;
						operacionCrearDetalleMesa.IdClasificacionTipoInstrumento = operacion.IdClasificacionTipoInstrumento;
						operacionCrearDetalleMesa.MontoFinalCarteraMonedaSistema = BigDecimal.ZERO;
						operacionCrearDetalleMesa.MontoFinalCarteraMonedaInstrumento= BigDecimal.ZERO;
						operacionCrearDetalleMesa.INGMontoAPlazoMonedaInstrumentoDetalle = BigDecimal.ZERO;			
						entradaCrearOperacion.p_DetalleOperacion.add(operacionCrearDetalleMesa);
						}
				}else {
					CrearOperacionMesa_DetalleOperacion_IN operacionCrearDetalleMesa = new CrearOperacionMesa_DetalleOperacion_IN();
					operacionCrearDetalleMesa.IdInternoDetalle = 0L;
					operacionCrearDetalleMesa.IdInternoOperacion = 0L;
					operacionCrearDetalleMesa.IdReserva = reservas.get(0L);
					operacionCrearDetalleMesa.IdCliente = cartera.IdCliente;
					operacionCrearDetalleMesa.IdPortafolio = cartera.IdPortafolioCliente;
					operacionCrearDetalleMesa.IdClienteCorredor = operacion.IdClienteCorredor;
					operacionCrearDetalleMesa.IdPortafolioCorredor = operacion.IdPortafolioCorredor;
					operacionCrearDetalleMesa.IdMesa = operacion.IdMesa;
					operacionCrearDetalleMesa.IdTipoProducto = "MERF";
					operacionCrearDetalleMesa.IdTipoOperacion = operacion.IdTipoOperacion;
					operacionCrearDetalleMesa.IdInstrumento = operacion.IdInstrumento;
					operacionCrearDetalleMesa.Nemotecnico = cartera.Nemotecnico;
					operacionCrearDetalleMesa.EsFungible = operacion.EsFungible;
					operacionCrearDetalleMesa.IdBolsa = operacion.IdBolsa;
					operacionCrearDetalleMesa.IdBoveda = operacion.IdBoveda;
					operacionCrearDetalleMesa.IdCartera = cartera.TipoOperacion.equals("COMTER")? 0L:operacion.IdCartera;
					operacionCrearDetalleMesa.FolioCartera = cartera.TipoOperacion.equals("COMTER")? 0L:operacion.FolioCarteraCustodia;
					operacionCrearDetalleMesa.CodigoIFRS = "T";
					operacionCrearDetalleMesa.IdEmisor = cartera.Emisor;
					operacionCrearDetalleMesa.IdMoneda = operacion.IdMoneda;
					operacionCrearDetalleMesa.IdTipoReajuste = operacion.IdTipoReajusteDetalle;
					operacionCrearDetalleMesa.IdMonedaEmisionInstrumento = operacion.IdMonedaEmisionInstrumento;
					operacionCrearDetalleMesa.IdMonedaLiquidacionInstrumento = operacion.IdMonedaLiquidacionInstrumento;
					operacionCrearDetalleMesa.IdMonedaLiquidacionOperacion = operacion.IdMonedaLiquidacionOperacion;
					operacionCrearDetalleMesa.CorteMinimo = operacion.CorteMinimo;
					operacionCrearDetalleMesa.Cantidad = cartera.CantidadAsignada;
					operacionCrearDetalleMesa.Precio = operacion.Precio;
					operacionCrearDetalleMesa.PrecioMercado = operacion.PrecioMercado;
					operacionCrearDetalleMesa.Tasa = operacion.Tasa;
					operacionCrearDetalleMesa.TasaMercado = operacion.TasaMercado;
					operacionCrearDetalleMesa.Monto = cartera.MontoAsignado;
					operacionCrearDetalleMesa.MontoLiquidacion = cartera.MontoAsignado;
					operacionCrearDetalleMesa.MontoLiquidacionOperacion = cartera.MontoAsignado;
					operacionCrearDetalleMesa.ValorRescate = operacion.ValorRescate;
					String fecha = operacion.FechaVencimientoInstrumento == null? "" : operacion.FechaVencimientoInstrumento.toString();
					operacionCrearDetalleMesa.FechaVencimiento = operacion.FechaVencimientoInstrumento == null? "" :fecha.substring(0, 10);//operacion.FechaVencimiento.toString();
					operacionCrearDetalleMesa.MontoValorizadoReserva = BigDecimal.ZERO;
					operacionCrearDetalleMesa.TipoCarteraCorredor = "";
					operacionCrearDetalleMesa.INGMontoAPlazoDetalle = operacion.INGMontoAPlazo;
					operacionCrearDetalleMesa.IdClasificacionTipoInstrumento = operacion.IdClasificacionTipoInstrumento;
					operacionCrearDetalleMesa.MontoFinalCarteraMonedaSistema = BigDecimal.ZERO;
					operacionCrearDetalleMesa.MontoFinalCarteraMonedaInstrumento= BigDecimal.ZERO;
					operacionCrearDetalleMesa.INGMontoAPlazoMonedaInstrumentoDetalle = BigDecimal.ZERO;			
					entradaCrearOperacion.p_DetalleOperacion = Arrays.asList(operacionCrearDetalleMesa);
				}
				
				List<CrearOperacionMesa_FormaPagoOperacion_IN> formaDePago = new ArrayList<>();
				for(CrearOperacionMesa_FormaPagoOperacion_OUT form : formaPago) {
					CrearOperacionMesa_FormaPagoOperacion_IN inFormaPago = new CrearOperacionMesa_FormaPagoOperacion_IN();
					inFormaPago.AfectarTesoreria = form.AfectarTesoreria;
					inFormaPago.IdInternoPagoOperacion = form.IdInternoPagoOperacion;
					inFormaPago.IdCuentaBancaria = form.IdCuentaBancaria;
					inFormaPago.IdFormaDePago = form.IdFormaDePago;
					inFormaPago.IdInternoOperacion = 0L;
					inFormaPago.IdComprobantePagoOperacion = form.IdComprobantePagoOperacion;
					inFormaPago.IdMedioDePago = form.IdMedioDePago;
					inFormaPago.IdMoneda = form.IdMoneda;
					inFormaPago.IdOperacion = form.IdOperacion;
					inFormaPago.MontoFormaDePago = form.MontoFormaDePago;
					inFormaPago.NombreFormaDePago = form.NombreFormaDePago;
					inFormaPago.CuentaBancaria = form.CuentaBancaria;
					inFormaPago.RecibePagaPortafolio = form.RecibePagaPortafolio;
					inFormaPago.FechaLiquidacion = cartera.FechaVencimiento;
					formaDePago.add(inFormaPago);
				}
				entradaCrearOperacion.p_FormasDePago = formaDePago;
			}
			return entradaCrearOperacion;
	
	}
	
	private IngresarDetalleOperacionMesa_IN crearDetalleData (Connection con, EntradaReasignarOperacion in, Carteras cartera, List<ObtenerCarterasRetirar.CarteraRetiro> carteraFifo, OperacionOriginal operacion, String fechaSistema) throws Exception {
		IngresarDetalleOperacionMesa_Operacion_IN operacionModificarOperacionMesa = new IngresarDetalleOperacionMesa_Operacion_IN();
		IngresarDetalleOperacionMesa_IN entradaIngresarDetalleOperacionMesa = new IngresarDetalleOperacionMesa_IN();
		
				entradaIngresarDetalleOperacionMesa.p_IdEmpresa = in.p_IdEmpresa;
				entradaIngresarDetalleOperacionMesa.p_IdUsuario = in.p_IdUsuario;
				entradaIngresarDetalleOperacionMesa.p_IdModulo = in.p_IdModulo;
				entradaIngresarDetalleOperacionMesa.p_UsaReglasNegocio = "S";
				
				operacionModificarOperacionMesa.Accion = "MODIFICACION";
				operacionModificarOperacionMesa.IdInternoOperacion = 0L;
				operacionModificarOperacionMesa.IdOperacion = this.IdOperacionNueva;
				operacionModificarOperacionMesa.FolioOperacion = 0L;
				operacionModificarOperacionMesa.FechaOperacion = fechaSistema;
				operacionModificarOperacionMesa.FechaLiquidacion = cartera.FechaVencimiento;
				operacionModificarOperacionMesa.IdCliente = cartera.IdCliente;
				operacionModificarOperacionMesa.IdPortafolio = cartera.IdPortafolioCliente;
				operacionModificarOperacionMesa.IdClienteCorredor = operacion.IdClienteCorredor;
				operacionModificarOperacionMesa.IdPortafolioCorredor = operacion.IdPortafolioCorredor;
				operacionModificarOperacionMesa.Intermesa = "N";
				operacionModificarOperacionMesa.IdMesaOrigen = operacion.IdMesa;
				operacionModificarOperacionMesa.IdMesaDestino = operacion.IdMesa;
				operacionModificarOperacionMesa.AfectaCustodia = operacion.AfectaCustodia;
				operacionModificarOperacionMesa.IdTipoProducto = "MERF";
				operacionModificarOperacionMesa.IdTipoOperacion = operacion.IdTipoOperacion;
				operacionModificarOperacionMesa.IdTipoReajuste = operacion.IdTipoReajusteDetalle;
				operacionModificarOperacionMesa.Plazo = 0L;
				operacionModificarOperacionMesa.Monto = cartera.MontoAsignado;
				operacionModificarOperacionMesa.MontoMoneda = cartera.MontoAsignado;
				operacionModificarOperacionMesa.IdMoneda = operacion.IdMoneda;
				operacionModificarOperacionMesa.IdMonedaLiquidacion = operacion.MonedaLiquidacion;
				operacionModificarOperacionMesa.MontoLiquidacion = BigDecimal.ZERO;
				operacionModificarOperacionMesa.Tasa = BigDecimal.ZERO;
				operacionModificarOperacionMesa.ValorFinal = cartera.MontoAsignado;
				operacionModificarOperacionMesa.ValorFinalMoneda = cartera.MontoAsignado;
				operacionModificarOperacionMesa.TasaAgente = BigDecimal.ZERO;
				operacionModificarOperacionMesa.FechaVencimiento = cartera.FechaVencimiento;
				operacionModificarOperacionMesa.IdCondicionLiquidacion = operacion.IdCondicionLiquidacion;
				operacionModificarOperacionMesa.IdAgente = 0L;
				operacionModificarOperacionMesa.IdOperador = 0L;
				operacionModificarOperacionMesa.IdEstado = "PEN";
				operacionModificarOperacionMesa.Glosa = "";
				operacionModificarOperacionMesa.IdTipoCanalIngreso = "AGE";
				operacionModificarOperacionMesa.TipoDocumento = "";
				operacionModificarOperacionMesa.IdContacto = 0L;
				operacionModificarOperacionMesa.TasaAnualizada = BigDecimal.ZERO;
				operacionModificarOperacionMesa.CostoFondo = "0";
				operacionModificarOperacionMesa.UtilidadPerdida = BigDecimal.ZERO;
				operacionModificarOperacionMesa.UtilidadFinal = BigDecimal.ZERO;
				entradaIngresarDetalleOperacionMesa.p_Operacion = Arrays.asList(operacionModificarOperacionMesa);
								
				if(esRetiroCustodia(cartera) ) {
					
					for(ObtenerCarterasRetirar.CarteraRetiro cart : carteraFifo) {
						IngresarDetalleOperacionMesa_DetalleOperacion_IN detalleOperacion = new IngresarDetalleOperacionMesa_DetalleOperacion_IN();
						detalleOperacion.IdInternoDetalle = cart.IdCartera * -1L;
						detalleOperacion.IdInternoOperacion = 0L;
						detalleOperacion.IdReserva = 0L;
						detalleOperacion.IdCliente = cartera.IdCliente;
						detalleOperacion.IdPortafolio = cartera.IdPortafolioCliente;
						detalleOperacion.IdClienteCorredor = operacion.IdClienteCorredor;
						detalleOperacion.IdPortafolioCorredor = operacion.IdPortafolioCorredor;
						detalleOperacion.IdMesa = operacion.IdMesa;
						detalleOperacion.IdTipoProducto = "MERF";
						detalleOperacion.IdTipoOperacion = operacion.IdTipoOperacion;
						detalleOperacion.IdClasificacionTipoInstrumento = operacion.IdClasificacionTipoInstrumento;
						detalleOperacion.IdInstrumento = operacion.IdInstrumento;
						detalleOperacion.Nemotecnico = cartera.Nemotecnico;
						detalleOperacion.EsFungible = "RF".equals(operacion.IdClasificacionTipoInstrumento) ? "N" : cartera.Fungible;
						detalleOperacion.IdBolsa = operacion.IdBolsa;
						detalleOperacion.IdBoveda = operacion.IdBoveda;
						detalleOperacion.IdCartera = cart.IdCartera;
						detalleOperacion.FolioCartera = cart.FolioCartera;
						detalleOperacion.CodigoIFRS = "T";
						detalleOperacion.IdEmisor = cartera.Emisor;
						detalleOperacion.IdMoneda = operacion.IdMoneda;
						detalleOperacion.IdTipoReajuste = operacion.IdTipoReajusteDetalle;
						detalleOperacion.IdMonedaEmisionInstrumento = operacion.IdMonedaEmisionInstrumento;
						detalleOperacion.IdMonedaLiquidacionInstrumento = operacion.IdMonedaLiquidacionInstrumento;
						detalleOperacion.IdMonedaLiquidacionOperacion = operacion.IdMonedaLiquidacionOperacion;
						detalleOperacion.CorteMinimo = operacion.CorteMinimo;
						detalleOperacion.Cantidad = cartera.CantidadAsignada;
						detalleOperacion.Precio = operacion.Precio;
						detalleOperacion.PrecioMercado = operacion.PrecioMercado;
						detalleOperacion.Tasa = operacion.Tasa;
						detalleOperacion.TasaMercado = operacion.TasaMercado;
						if("RF".equals(operacion.IdClasificacionTipoInstrumento)) {
							detalleOperacion.Monto = cartera.MontoAsignado;
							detalleOperacion.MontoLiquidacion = cartera.MontoAsignado;
							detalleOperacion.MontoLiquidacionOperacion = cartera.MontoAsignado;
							detalleOperacion.MontoValorizadoReserva = cartera.MontoAsignado;
							detalleOperacion.INGMontoAPlazoDetalle = cartera.MontoAsignado;
							detalleOperacion.INGMontoAPlazoMonedaInstrumentoDetalle = cartera.MontoAsignado;
						}
						
						if("IF".equals(operacion.IdClasificacionTipoInstrumento)) {
							detalleOperacion.Monto = cartera.MontoAsignado;
							detalleOperacion.MontoLiquidacion = cartera.MontoAsignado;
							detalleOperacion.MontoLiquidacionOperacion = cartera.MontoAsignado;
							detalleOperacion.MontoValorizadoReserva = cartera.MontoAsignado;
							detalleOperacion.INGMontoAPlazoDetalle = cartera.MontoAsignado;
							detalleOperacion.INGMontoAPlazoMonedaInstrumentoDetalle = cartera.MontoAsignado;
						}
						detalleOperacion.Monto = cartera.MontoAsignado;
						detalleOperacion.MontoLiquidacion = cartera.MontoAsignado;
						detalleOperacion.MontoLiquidacionOperacion = cartera.MontoAsignado;
						detalleOperacion.MontoValorizadoReserva = cartera.MontoAsignado;
						detalleOperacion.INGMontoAPlazoDetalle = cartera.MontoAsignado;
						detalleOperacion.INGMontoAPlazoMonedaInstrumentoDetalle = cartera.MontoAsignado;
						detalleOperacion.ValorRescate = operacion.ValorRescate;
						detalleOperacion.FechaVencimiento = operacion.FechaVencimiento.toString();
						detalleOperacion.TipoCarteraCorredor = "";
						entradaIngresarDetalleOperacionMesa.p_DetalleOperacion.add(detalleOperacion);
					}
				} else {
				
				IngresarDetalleOperacionMesa_DetalleOperacion_IN detalleOperacion = new IngresarDetalleOperacionMesa_DetalleOperacion_IN();
				detalleOperacion.IdInternoDetalle = 0L;
				detalleOperacion.IdInternoOperacion = 0L;
				detalleOperacion.IdReserva = 0L;
				detalleOperacion.IdCliente = cartera.IdCliente;
				detalleOperacion.IdPortafolio = cartera.IdPortafolioCliente;
				detalleOperacion.IdClienteCorredor = operacion.IdClienteCorredor;
				detalleOperacion.IdPortafolioCorredor = operacion.IdPortafolioCorredor;
				detalleOperacion.IdMesa = operacion.IdMesa;
				detalleOperacion.IdTipoProducto = "MERF";
				detalleOperacion.IdTipoOperacion = operacion.IdTipoOperacion;
				detalleOperacion.IdClasificacionTipoInstrumento = operacion.IdClasificacionTipoInstrumento;
				detalleOperacion.IdInstrumento = operacion.IdInstrumento;
				detalleOperacion.Nemotecnico = cartera.Nemotecnico;
				detalleOperacion.EsFungible = operacion.EsFungible;
				detalleOperacion.IdBolsa = operacion.IdBolsa;
				detalleOperacion.IdBoveda = operacion.IdBoveda;
				detalleOperacion.IdCartera = operacion.IdCartera;
				detalleOperacion.FolioCartera = cartera.TipoOperacion.equals("COMTER")? 0L:operacion.FolioCarteraCustodia;;
				detalleOperacion.CodigoIFRS = "T";
				detalleOperacion.IdEmisor = cartera.Emisor;
				detalleOperacion.IdMoneda = operacion.IdMoneda;
				detalleOperacion.IdTipoReajuste = operacion.IdTipoReajusteDetalle;
				detalleOperacion.IdMonedaEmisionInstrumento = operacion.IdMonedaEmisionInstrumento;
				detalleOperacion.IdMonedaLiquidacionInstrumento = operacion.IdMonedaLiquidacionInstrumento;
				detalleOperacion.IdMonedaLiquidacionOperacion = operacion.IdMonedaLiquidacionOperacion;
				detalleOperacion.CorteMinimo = operacion.CorteMinimo;
				detalleOperacion.Cantidad = cartera.CantidadAsignada;
				detalleOperacion.Precio = operacion.Precio;
				detalleOperacion.PrecioMercado = operacion.PrecioMercado;
				detalleOperacion.Tasa = operacion.Tasa;
				detalleOperacion.TasaMercado = operacion.TasaMercado;
				detalleOperacion.Monto = cartera.MontoAsignado;
				detalleOperacion.MontoLiquidacion = cartera.MontoAsignado;
				detalleOperacion.MontoLiquidacionOperacion = cartera.MontoAsignado;
				detalleOperacion.ValorRescate = operacion.ValorRescate;
				detalleOperacion.FechaVencimiento = operacion.FechaVencimiento.toString();
				detalleOperacion.MontoValorizadoReserva = BigDecimal.ZERO;
				detalleOperacion.TipoCarteraCorredor = "";
				detalleOperacion.INGMontoAPlazoDetalle = operacion.INGMontoAPlazo;
				entradaIngresarDetalleOperacionMesa.p_DetalleOperacion = Arrays.asList(detalleOperacion);
				}
		return entradaIngresarDetalleOperacionMesa;
	}
	
	private String calculaFechaVencimiento(String fechaReasignacion, String fechaVencimientoOriginal) throws Exception{
		logger.trace("INICIO::EncuentraFechaVencimiento");
		
		String fechaVencimiento = "";
		SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");
		Date fechaReasignacionOri = formato.parse(fechaReasignacion);
		Date fechaVencimientoOri = formato.parse(fechaVencimientoOriginal);
			
		if(fechaReasignacionOri.before(fechaVencimientoOri)){
			 fechaVencimiento = fechaVencimientoOriginal;
		}else {
			fechaVencimiento = fechaReasignacion;
		}
		
		logger.trace("FIN::EncuentraFechaVencimiento.EncuentraFechaVencimiento");
		return fechaVencimiento;
	}
	
	
	private List<ObtenerCarterasRetirar.Resultado> obtenerCarteraRetirar(Connection con, EntradaReasignarOperacion entrada, Carteras cartera, OperacionOriginal operacion) throws Exception {
		logger.trace("INICIO::CrearOperacionesTerminoCargaMasiva.obtenerCarteraRetirar");
		
		String nemosSVS = obtenerNemoSVS(con, cartera, operacion);
		
		EntradaObtenerCarterasRetirar entradaObtenerCarterasRetirar = new EntradaObtenerCarterasRetirar();
		entradaObtenerCarterasRetirar.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaObtenerCarterasRetirar.p_IdUsuario = entrada.p_IdUsuario;
		entradaObtenerCarterasRetirar.p_IdModulo = entrada.p_IdModulo;
			if(esRetiroCustodia(cartera)) {
				List<ObtenerCarterasRetirar.Consulta> consultas = new ArrayList<>();
						
							ObtenerCarterasRetirar.Consulta consulta = new ObtenerCarterasRetirar.Consulta();
							consulta.IdInterno = 0L;
							consulta.IdClasificacionTipoInstrumento = operacion.IdClasificacionTipoInstrumento;
							consulta.IdPortafolio = "COMTER".equals(cartera.TipoOperacion) ? cartera.IdPortafolioCliente : operacion.IdPortafolioCorredor;
							consulta.IdBoveda = operacion.IdBoveda;
							if("RF".equals(consulta.IdClasificacionTipoInstrumento)) {
								consulta.Nemotecnico = cartera.Nemotecnico;
								consulta.Cantidad = cartera.CantidadAsignada;
							}
							if("IF".equals(consulta.IdClasificacionTipoInstrumento)) {
								consulta.Nemotecnico = nemosSVS;
								consulta.Cantidad = cartera.ValorRescate;
							}
							consultas.add(consulta);
					
				entradaObtenerCarterasRetirar.p_Consultas.addAll(consultas);
			}
		
		
		SalidaObtenerCarterasRetirar salidaObtenerCarterasRetirar = new SalidaObtenerCarterasRetirar();
		ObtenerCarterasRetirar obtenerCarterasRetirar = new ObtenerCarterasRetirar();
		if(entradaObtenerCarterasRetirar.p_Consultas.size() > 0) {
			salidaObtenerCarterasRetirar =  obtenerCarterasRetirar.logica(con, entradaObtenerCarterasRetirar);
		}
		
		logger.trace("FIN::CrearOperacionesTerminoCargaMasiva.obtenerCarteraRetirar");
		return salidaObtenerCarterasRetirar.P_OUT_RESULTADO;
	}
	
	private String obtenerNemoSVS(Connection con, Carteras cartera, OperacionOriginal operacion) throws Exception {
		logger.trace("INICIO::CrearOperacionesTerminoCargaMasiva.obtenerNemoSVS");
		ObtenerNemoSVS_Entrada entradaObtenerNemoSVS = new ObtenerNemoSVS_Entrada();
		Instrumento_Entrada instrumento = new Instrumento_Entrada();
		if("IF".equals(cartera.Mercado)){
			instrumento.IdInterno = 0L;
			instrumento.Nemotecnico = cartera.Nemotecnico;
			instrumento.IdEmisor = cartera.Emisor;
			instrumento.FechaVencimiento = operacion.FechaVencimiento.toString();
			instrumento.IdTipoReajuste = cartera.IdTipoReajuste;
			instrumento.PagaderoEn = cartera.IdMonedaEmision;
		}
		
		ObtenerNemoSVS obtenerNemoSVS = new ObtenerNemoSVS();
		ObtenerNemoSVS_Salida salidaObtenerNemoSVS = new ObtenerNemoSVS_Salida();
		String NemoSVS = "";
		if(entradaObtenerNemoSVS.p_Instrumentos.size() > 0) {
			salidaObtenerNemoSVS = obtenerNemoSVS.logica(con, entradaObtenerNemoSVS);
			NemoSVS = salidaObtenerNemoSVS.p_out_Instrumento.get(0).NemoSVS;
		}
		
		logger.trace("FIN::CrearOperacionesTerminoCargaMasiva.obtenerNemoSVS");
		return NemoSVS;
	}
	
	private Boolean esRetiroCustodia(Carteras operacion) {
		logger.trace("INICIO::CrearOperacionesTerminoCargaMasiva.esRetiroFIFO");
		Boolean esRetiroFIFO = esCompraTerminoCustodia(operacion) || "VENTER".equals(operacion.TipoOperacion);
		logger.trace("FIN::CrearOperacionesTerminoCargaMasiva.esRetiroFIFO");
		return esRetiroFIFO;
	}
	
	private Boolean esCompraTerminoCustodia(Carteras operacion) {
		logger.trace("INICIO::CrearOperacionesTerminoCargaMasiva.esCompraTerminoCustodia");
		Boolean esCompraTerminoCustodia = "COMTER".equals(operacion.TipoOperacion) && operacion.AfectaCustodia;
		logger.trace("FIN::CrearOperacionesTerminoCargaMasiva.esCompraTerminoCustodia");
		return esCompraTerminoCustodia;
	}
	
	private void agregarLogSistema(Connection con, EntradaReasignarOperacion entrada, String fechaSistema, List<OperacionOriginal> operacionOriginal, OperacionReasignacion operacionReasignacion) throws Exception {
		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
		
		
		if(entrada.p_Carteras.size()>0) {
				for(Carteras OPIN : entrada.p_Carteras) {
					AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
					log.IdCabeceraLog = 0L;
					log.IdTipoLog = "MODIFICACION";
					log.IdModulo = "MRF";
					log.IdUsuario = entrada.p_IdUsuario;
					log.IdEmpresa = entrada.p_IdEmpresa;
					log.FechaSistemaLog = fechaSistema;
					log.IdUsuarioAtendido = entrada.p_IdUsuario;
					log.IdCliente =  0L;
					log.EstadoCliente = "";
					log.NombreCliente = "";
					log.IdTipoDocumentoIdentidad = "";
					log.IdDocumentoIdentidad = "";
					log.IdPortafolio =  0L;
					log.EstadoPortafolio = "";
					log.IdTipoRegistro = "";
					log.EstadoRegistro = "";
					log.IdTipoOperacion = "";
					log.Folio = OPIN.FolioCartera;
					log.Cantidad = BigDecimal.ZERO;
					log.PrecioTasa =   BigDecimal.ZERO;
					log.Monto =   BigDecimal.ZERO;
					log.IdMonedaOrigen =  "";
					log.IdMonedaLiquidacion = "";
					log.TipoCambio = "";
					log.Nemo = "";
					log.IdProceso = 0L;
					log.EstadoProceso = "";
					log.NombreProceso = "MRF.ReasignarOperacion";
					
					InAgregarLog.DetalleLog detLog = new InAgregarLog.DetalleLog();
					detLog.IdCabeceraLog = operacionReasignacion.IdOperacionNueva;
					detLog.IdTrabajo=0L;
					detLog.SubRegistro = "Operacion";
					detLog.CampoRegistro = "IdOperacion";
					detLog.ValorAntiguo =operacionReasignacion.IdOperacionOriginal.toString();
					detLog.ValorNuevo = operacionReasignacion.IdOperacionNueva.toString();
					entradaAgregarLog.p_LogDetalle.add(detLog);
					
					
					entradaAgregarLog.p_Log.add(log);
					
					
				
					
				}
				AgregarLog agregarLogService = new AgregarLog();
				agregarLogService.logica(con, entradaAgregarLog);
			}
	}

	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getQAConnection()) {
			Gson gson = Utils.getGsonBuilder();

			//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":98,\"p_IdModulo\":\"MRF\",\"p_IdOperacion\":75771,\"p_Carteras\":[{\"NombreOperacion\":\"COMPRA A TERMINO\",\"Nemotecnico\":\"BAGUA-AD\",\"Emisor\":\"AGUAS\",\"Secuencia\":75771,\"DocumentoIdentidad\":\"10632471-9\",\"NombreCompleto\":\"JP MYRGVN FLWMXNGS FZNDS\",\"IdPortafolioCliente\":1453,\"IdCliente\":65,\"FechaLiquidacion\":\"0001-01-01\",\"TasaOperacion\":1,\"MontoAsignado\":46057716,\"CantidadAsignada\":1000,\"FolioCartera\":268,\"FolioOperacion\":64},{\"NombreOperacion\":\"COMPRA A TERMINO\",\"Nemotecnico\":\"BAGUA-AC\",\"Emisor\":\"AGUAS\",\"Secuencia\":75771,\"DocumentoIdentidad\":\"10171989-8\",\"NombreCompleto\":\"WDWLMVG\",\"IdPortafolioCliente\":4118,\"IdCliente\":2829,\"FechaLiquidacion\":\"0001-01-01\",\"TasaOperacion\":1,\"MontoAsignado\":22085140,\"CantidadAsignada\":1000,\"FolioCartera\":269,\"FolioOperacion\":64}],\"p_CantidadAProcesar\":2}"; 
			//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":98,\"p_IdModulo\":\"MRF\",\"p_IdOperacion\":75773,\"p_Carteras\":[{\"NombreOperacion\":\"COMPRA A TERMINO\",\"Nemotecnico\":\"BAGUA-AC\",\"Emisor\":\"AGUAS\",\"Secuencia\":75773,\"DocumentoIdentidad\":\"10171989-8\",\"NombreCompleto\":\"WDWLMVG\",\"IdPortafolioCliente\":4118,\"IdCliente\":2829,\"FechaLiquidacion\":\"0001-01-01\",\"TasaOperacion\":1,\"MontoAsignado\":11042570,\"CantidadAsignada\":500,\"FolioCartera\":266,\"FolioOperacion\":66}],\"p_CantidadAProcesar\":1}";
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdModulo\":\"MRF\",\"p_IdOperacion\":146569,\"p_Carteras\":[{\"NombreOperacion\":\"COMPRA A TERMINO\",\"Nemotecnico\":\"BAGUA-AA\",\"Emisor\":\"AGUAS\",\"Secuencia\":146569,\"DocumentoIdentidad\":\"12969673-7\",\"NombreCompleto\":\"JORGE VILLAGRAN VASQUEZ\",\"IdPortafolioCliente\":12194,\"IdCliente\":11292,\"FechaLiquidacion\":\"0001-01-01\",\"TasaOperacion\":1,\"MontoAsignado\":189844058,\"CantidadAsignada\":4000,\"FolioCartera\":472433,\"FolioOperacion\":128509,\"AfectaCustodia\":false,\"IdInstrumento\":172596,\"TipoOperacion\":\"COMTER\"}],\"p_CantidadAProcesar\":1}";
			EntradaReasignarOperacion Objin = gson.fromJson(json, EntradaReasignarOperacion.class);
			ReasignarOperacion demo = new ReasignarOperacion();
			try {
				SalidaReasignarOperacion ObjOut = demo.logica(con, Objin);
				con.commit();
				System.out.println(ObjOut.toString());
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
		}
		System.out.println("== END ==");
	}
}