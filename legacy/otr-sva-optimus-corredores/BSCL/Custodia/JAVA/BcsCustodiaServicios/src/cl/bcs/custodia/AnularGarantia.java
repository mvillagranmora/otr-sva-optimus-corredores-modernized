package cl.bcs.custodia;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.cuentaInversion.AnularCargoAbono;
import cl.bcs.cuentaInversion.AnularCargoAbono.AnularCargoAbono_Entrada;
import cl.bcs.cuentaInversion.AnularCargoAbono.AnularCargoAbono_Salida;
import cl.bcs.cuentaInversion.AnularCargoAbono.CargoAbono_Entrada;
import cl.bcs.custodia.AnularMovimientosCertificados.Cartera_Proceso;
import cl.bcs.custodia.ConsultarCarteras.ConsultarCarteraIn;
import cl.bcs.custodia.ConsultarCarteras.ConsultarCarterasOut;
import cl.bcs.custodia.ConsultarCarteras.Tabla_Salida;
import cl.bcs.custodia.IngresarGarantiaJava.IngresarGarantiaEntrada;
import cl.bcs.custodia.IngresarGarantiaJava.IngresarGarantiaSalida;
import cl.bcs.custodia.utils.IngresoCustodia;
import cl.bcs.custodia.utils.RetiroCustodia;
import cl.bcs.custodia.utils.IngresoCustodia.IngresoCustodiaEntrada;
import cl.bcs.custodia.utils.IngresoCustodia.IngresoCustodiaSalida;
import cl.bcs.custodia.utils.RetiroCustodia.RetiroCustodiaEntrada;
import cl.bcs.custodia.utils.RetiroCustodia.RetiroCustodiaSalida;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class AnularGarantia implements JavaCall{
	public static final Logger logger = LogManager.getLogger(AnularGarantia.class);
	
	private final String SQL_SELECT_MOVIMIENTOS_EN_TRANSITO = loadResourceToString("cl/bcs/custodia/sql/AnularGarantia_SelectMovimientosEnTransito.sql");
	private final String SQL_SELECT_MOVIMIENTOS_EN_TRANSITO_RETIRO_CERTIFICADOS = loadResourceToString("cl/bcs/custodia/sql/AnularGarantia_SelectMovimientosEnTransitoRetiroGarantiaCertificado.sql");
	private final String SQL_SELECT_INGRESO_GARANTIA = loadResourceToString("cl/bcs/custodia/sql/AnularGarantia_SelectGarantias.sql");
	private final String SQL_SELECT_RETIRO_GARANTIA_CUS = loadResourceToString("cl/bcs/custodia/sql/AnularGarantia_SelectRetiroGarantias.sql");
	private final String SQL_SELECT_RETIRO_GARANTIA_CTA = loadResourceToString("cl/bcs/custodia/sql/AnularGarantia_SelectRetiroGarantiasCuentaInversion.sql");
	private final String SQL_SELECT_RETIRO_GARANTIA_ESP = loadResourceToString("cl/bcs/custodia/sql/AnularGarantia_SelectRetiroGarantiasEspecie.sql");
	private final String SQL_SELECT_CARTERA_VIGENTE = loadResourceToString("cl/bcs/custodia/sql/AnularGarantia_SelectCarteraVigente.sql");

	private final String SQL_UPDATE_MOVIMIENTOS_EN_TRANSITO = loadResourceToString("cl/bcs/custodia/sql/AnularGarantia_UpdateMovimientosEnTransito.sql");
	private final String SQL_UPDATE_GARANTIA = loadResourceToString("cl/bcs/custodia/sql/AnularGarantia_UpdateGarantias.sql");
	
	public static class EntradaAnularGarantia extends BcsPojo {
		public Long 	p_IdEmpresa;
		public Long 	p_IdUsuario;
		public String 	p_IdModulo;
		public String 	p_UsaReglasNegocio;
		public String 	p_SimularOperacion;
		public List<MovimientoEnTransito> p_MovimientosEnTransito;
		
		public EntradaAnularGarantia () {
			this.p_IdEmpresa = 0L;
			this.p_IdUsuario = 0L;
			this.p_IdModulo = "";
			this.p_UsaReglasNegocio = "S";
			this.p_SimularOperacion = "S";
			this.p_MovimientosEnTransito = new ArrayList<MovimientoEnTransito>();
		}
	}
	
	public static class MovimientoEnTransito extends BcsPojo {
		public Long IdMovimientoCarteraEnTransito;
		public String IdTipoOperacion;
		public String IdEstado;
		public String IdOrigenGarantia;
		public Long IdPortafolio;
		public Timestamp FechaAnulacionRegistro;
		public Long IdUsuarioAnulacion;
		public String ExcluyeMovimientoEnCartola;
		public Long FolioGarantia;
		
		public MovimientoEnTransito () {
			this.IdMovimientoCarteraEnTransito = 0L;
			this.IdTipoOperacion = "";
			this.IdEstado = "";
			this.IdOrigenGarantia = "";
			this.IdPortafolio = 0L;
			this.FechaAnulacionRegistro = Fecha.toTimeStamp("0001-01-01 00:00:00");
			this.IdUsuarioAnulacion = 0L;
			this.ExcluyeMovimientoEnCartola = "N";
			this.FolioGarantia = 0L;
		}
		
		public MovimientoEnTransito (Long idMovimientoCarteraTransito, String idTipoOperacion) {
			this.IdMovimientoCarteraEnTransito = idMovimientoCarteraTransito;
			this.IdTipoOperacion = idTipoOperacion;
		}
	}

	public static class Garantia extends BcsPojo {
		public Long IdMovimientoCarteraEnTransito;
		public Long IdGarantia;
		public Long IdGarantiaVigente;
		public String IdMonedaInstrumento;
		public String IdEstado;
		public String IdOrigenGarantia;
		public BigDecimal MontoGarantia;
		public BigDecimal INGCantidad;
		public BigDecimal ValorRescate;
		public Long IdPortafolio;
		public Long FolioGarantia;
		public String FechaOperacion;
		public Long IdCargoAbonoMovimiento;
		public Long FolioCarteraOrigen;
		public String IdConcepto;
		public Long IdCliente;
		
		public Garantia () {
			this.IdMovimientoCarteraEnTransito = 0L;
			this.IdGarantia = 0L;
			this.IdGarantiaVigente =0L;
			this.IdMonedaInstrumento = "";
			this.IdEstado = "";
			this.IdOrigenGarantia = "";
			this.IdPortafolio = 0L;
			this.MontoGarantia = BigDecimal.ZERO;
			this.INGCantidad = BigDecimal.ZERO;
			this.ValorRescate = BigDecimal.ZERO;
			this.FolioGarantia = 0L;
			this.FechaOperacion = "0001-01-01";
			this.IdCargoAbonoMovimiento = 0L;
			this.FolioCarteraOrigen = 0L;
			this.IdConcepto = "";
			this.IdCliente = 0L;
		}
	}
	
	public static class RetiroGarantia extends BcsPojo {
		public Long IdMovimientoCarteraEnTransito;
		public Long IdMovimientoCarteraEnTransitoOrigen;
		public Long IdGarantia;
		public Long IdGarantiaOrigen;
		public Long IdGarantiaVigente;
		public String IdMonedaInstrumento;
		public String IdEstado;
		public String IdOrigenGarantia;
		public BigDecimal MontoGarantia;
		public BigDecimal INGCantidad;
		public Long IdPortafolio;
		public Long IdCliente;
		public Long FolioGarantia;
		public String FechaOperacion;
		public String FechaEmisionInstrumento;
		public String FechaVencimientoInstrumento;
		public Long IdCargoAbonoMovimiento;
		public Long FolioCarteraOrigen;
		public String NemoSVS;
		public Long IdInstrumento;
		public String IdMonedaEmision;
		public BigDecimal ValorRescate;
		public String IdClasificacionTipoInstrumento;
		public String IdTipoReajuste;
		public String IdEmisor;
		public Long PlazoEnDiasOperacion;
		public String EsFungible;
		public Long IdAgente;
		public String IdTipoInstrumento;
		public String IdBolsa;
		public Long IdInstrumentoGarantizado;
		public String IdConcepto;
		public Long IdContraparteGarantia;
		public BigDecimal Precio;
		public Long IdOperacion;
		public BigDecimal CorteMinimo;
		public BigDecimal MontoCompraMonedaInstrumento;
		public BigDecimal MovimientoCantidad;
		public BigDecimal MovimientoValorRescate;
		public BigDecimal EGRMontoMonedaInstrumento;
		public BigDecimal EGRCantidad;
		public String IdTipoOperacion;
		public BigDecimal Tasa;
		public BigDecimal CantidadVigente;
		
		public RetiroGarantia () {
			this.IdMovimientoCarteraEnTransito = 0L;
			this.IdGarantia = 0L;
			this.IdGarantiaOrigen = 0L;
			this.IdGarantiaVigente = 0L;
			this.IdMonedaInstrumento = "";
			this.IdEstado = "";
			this.IdOrigenGarantia = "";
			this.IdPortafolio = 0L;
			this.MontoGarantia = BigDecimal.ZERO;
			this.INGCantidad = BigDecimal.ZERO;
			this.FolioGarantia = 0L;
			this.FechaOperacion = "0001-01-01";
			this.IdCargoAbonoMovimiento = 0L;
			this.FolioCarteraOrigen = 0L;
			this.MontoCompraMonedaInstrumento = BigDecimal.ZERO;
			this.MovimientoCantidad = BigDecimal.ZERO;
			this.MovimientoValorRescate = BigDecimal.ZERO;
			this.EGRMontoMonedaInstrumento = BigDecimal.ZERO;
			this.EGRCantidad = BigDecimal.ZERO;
			this.IdTipoOperacion = "";
			this.Tasa = BigDecimal.ZERO;
			this.CantidadVigente = BigDecimal.ZERO;
		}
	}
	
	public static class RetiroCertificado extends BcsPojo {
		public String MovimientosRetiroEstadoCertificado;
		
		public RetiroCertificado () {
			this.MovimientosRetiroEstadoCertificado = "N";
		}
	}
	
	public static class SalidaAnularGarantia extends BcsPojo {
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		
		public SalidaAnularGarantia () {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR = 0L;
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();
		EntradaAnularGarantia jin = gson.fromJson(in.toString(), EntradaAnularGarantia.class);
		SalidaAnularGarantia out = logica(conn, jin);

		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public SalidaAnularGarantia logica(Connection con, EntradaAnularGarantia entrada) throws Exception {
		logger.info("INICIO::AnularGarantia");
		long inicio = System.currentTimeMillis();
		
		SalidaAnularGarantia salida = new SalidaAnularGarantia();
		
		if(entrada.p_MovimientosEnTransito.isEmpty()) {
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_ENTRADA_SIN_MOVIMIENTO_EN_TRANSITO",new HashMap<String,String>(0),con, entrada.p_IdEmpresa));
			salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
			return salida;
		}
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		Timestamp fechaHoraSistema = FechaDB.ObtenerFechaHoraSistemaTs(con, entrada.p_IdEmpresa);
		RetiroCustodia 	retiroCustodiaService 	= new RetiroCustodia();
		
		SQL sql = new SQL();
		
		HashMap<String,Object> paramsMovtos = new HashMap<>(2);
		paramsMovtos.put("idsMovimientoCarteraTransito", entrada.p_MovimientosEnTransito.stream().filter(movto -> ("INGGTA").equals(movto.IdTipoOperacion)).map(mvto -> mvto.IdMovimientoCarteraEnTransito).collect(Collectors.toList()) );
		List<MovimientoEnTransito> movimientosIngreso = sql.query(con, SQL_SELECT_MOVIMIENTOS_EN_TRANSITO, paramsMovtos, MovimientoEnTransito.class);
		
		if(!movimientosIngreso.isEmpty()) {
			RetiroCertificado retiroCertificado = sql.query(con, SQL_SELECT_MOVIMIENTOS_EN_TRANSITO_RETIRO_CERTIFICADOS, paramsMovtos, RetiroCertificado.class).stream().findFirst().orElse(new RetiroCertificado());
			
			if("S".equals(retiroCertificado.MovimientosRetiroEstadoCertificado)) {
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_HAY_RETIRO_GARANTIAS_NO_ANULADOS",new HashMap<String,String>(0),con, entrada.p_IdEmpresa));
				salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
				return salida;
			}
			
			if( movimientosIngreso.stream().filter(mvto-> "ANU".equals(mvto.IdEstado)).collect(Collectors.toList()).size() > 0 ) {
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_MOVIMIENTO_EN_TRANSITO_ESTADO_ANULADO",new HashMap<String,String>(0),con, entrada.p_IdEmpresa));
				salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
				return salida;
			}
			
			if(movimientosIngreso.stream().filter(mvto-> "S".equals(mvto.ExcluyeMovimientoEnCartola)).collect(Collectors.toList()).size() > 0 ) {
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_MOVIMIENTO_EN_TRANSITO_CONTRAPARTIDA_GENERADA",new HashMap<String,String>(0),con, entrada.p_IdEmpresa));
				salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
				return salida;
			}
			
			List<Garantia> garantias = new ArrayList<Garantia>();
			garantias = sql.query(con, SQL_SELECT_INGRESO_GARANTIA, paramsMovtos, Garantia.class);
			
			if(!garantias.isEmpty()) {
				
				HashMap<String, Object> params = new HashMap<>(1);
				
				List<Long> valoresCarteraGarantias = garantias.parallelStream().map(garantia->garantia.FolioCarteraOrigen).collect(Collectors.toList());
				params.put("FolioCarteraOrigen", valoresCarteraGarantias);
				List<Cartera_Proceso> carterasVigentes = sql.query(con, SQL_SELECT_CARTERA_VIGENTE, params, Cartera_Proceso.class);
				Map<Long,Cartera_Proceso> mapCarterasVigentes = carterasVigentes.parallelStream().collect(Collectors.toMap(cartera->cartera.FolioCartera, cartera->cartera));
				
				for(Garantia gta : garantias) {
					if("ANU".equals(gta.IdEstado)) {
						salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_GARANTIA_ESTADO_ANULADO",new HashMap<String,String>(0),con, entrada.p_IdEmpresa));
						salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
						return salida;
					}
					
					if(!fechaSistema.equals(gta.FechaOperacion)) {
						salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_FECHA_OPERACION_MOVIMIENTO",new HashMap<String,String>(0),con, entrada.p_IdEmpresa));
						salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
						return salida;
					}
					Cartera_Proceso carteraVigente = mapCarterasVigentes.get(gta.FolioCarteraOrigen);
					
					if("CTA".equals(gta.IdOrigenGarantia)) {
						AnularCargoAbono_Entrada entradaAnularIngresoEgreso = new AnularCargoAbono_Entrada();
						entradaAnularIngresoEgreso.p_IdEmpresa = entrada.p_IdEmpresa;
						entradaAnularIngresoEgreso.p_IdUsuario = entrada.p_IdUsuario;
						entradaAnularIngresoEgreso.p_IdModulo = entrada.p_IdModulo;
						entradaAnularIngresoEgreso.p_simularOperacion = "N";
						entradaAnularIngresoEgreso.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
						entradaAnularIngresoEgreso.p_FechaMovimiento = gta.FechaOperacion;
						entradaAnularIngresoEgreso.p_EsAnulacionManual	= "S";
						CargoAbono_Entrada cargoAbono = new CargoAbono_Entrada();
						cargoAbono.IdCargoAbonoMovimiento = gta.IdCargoAbonoMovimiento;
						cargoAbono.GlosaAnulacion = "Anulacion de Garantia";
						entradaAnularIngresoEgreso.p_CargoAbonoAAnular.add(cargoAbono);
						AnularCargoAbono servicioAnularCargoAbono = new AnularCargoAbono();
						AnularCargoAbono_Salida salidaAnularCargoAbono = servicioAnularCargoAbono.logica(con, entradaAnularIngresoEgreso);
						salida.P_OUT_MENSAJES.addAll(salidaAnularCargoAbono.P_OUT_MENSAJES);
						salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
						
					} else { 
						
						if(carterasVigentes != null && !carterasVigentes.isEmpty()) {
							IngresoCustodia ingresoCustodiaService	= new IngresoCustodia();
							IngresoCustodiaEntrada ingresoCustodiaEntrada = new IngresoCustodiaEntrada();
							RetiroCustodiaEntrada retiroCustodiaEntrada = new RetiroCustodiaEntrada();
							
							IngresoCustodia.ObjDatosEntrada reingresoC = reingresoCartera( fechaSistema, carteraVigente, gta);
							ingresoCustodiaEntrada.p_DatosEntrada.add(reingresoC);
							
							//SOLO SI LA CARTERA ES DISTINTA A LIQ
							if(!"LIQ".equals(carteraVigente.IdEstado)){
								RetiroCustodia.ObjDatosEntrada anulacionC = anulacionCartera(carteraVigente, fechaHoraSistema, fechaSistema, gta.IdConcepto);
								retiroCustodiaEntrada.p_DatosEntrada.add(anulacionC);
							}
							
							if (ingresoCustodiaEntrada.p_DatosEntrada.size() > 0) {
								ingresoCustodiaEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
								ingresoCustodiaEntrada.p_IdModulo = entrada.p_IdModulo;
								ingresoCustodiaEntrada.p_IdUsuario = entrada.p_IdUsuario;
								ingresoCustodiaEntrada.p_SimularOperacion = entrada.p_SimularOperacion;
								ingresoCustodiaEntrada.p_TicketAtencion = 0L;
								ingresoCustodiaEntrada.p_ValorizacionAutomatica = "N";
								IngresoCustodiaSalida ingresoCustodiaSalida = ingresoCustodiaService.logica(con, ingresoCustodiaEntrada);
								if(ingresoCustodiaSalida.P_OUT_ERRORESCONTADOR == 0) {
									salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"UI_REINGRESO_CUSTODIA_OK",new HashMap<String,String>(),con, entrada.p_IdEmpresa));
								}
								salida.P_OUT_MENSAJES.addAll(ingresoCustodiaSalida.P_OUT_MENSAJES);
								salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
								
							}
							
							if(retiroCustodiaEntrada.p_DatosEntrada.size() > 0) {
								retiroCustodiaEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
								retiroCustodiaEntrada.p_IdModulo = entrada.p_IdModulo;
								retiroCustodiaEntrada.p_IdUsuario = entrada.p_IdUsuario;
								retiroCustodiaEntrada.p_SimularOperacion = entrada.p_SimularOperacion;
								retiroCustodiaEntrada.p_TicketAtencion = 0L;
								retiroCustodiaEntrada.p_ValorizacionAutomatica = "N";
								RetiroCustodiaSalida retiroCustodiaSalida = retiroCustodiaService.logica(con,retiroCustodiaEntrada);
								salida.P_OUT_MENSAJES.addAll(retiroCustodiaSalida.P_OUT_MENSAJES);
							}
							
						}
					}
					gta.IdGarantia = gta.IdGarantiaVigente;
					gta.IdEstado = "ANU";
				}
				
				if(salida.P_OUT_ERRORESCONTADOR > 0) {
					return salida;
				}
				
				sql.batch(con,SQL_UPDATE_GARANTIA, garantias, Garantia.class);	
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"UI_ANULA_GARANTIAS_OK",new HashMap<String,String>(0),con, entrada.p_IdEmpresa));
				
				{
					logger.info("INFO::AnularGarantia --- Se agregan Anulaciones al Log");
					AgregarLog agregarLogService = new AgregarLog();
					AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
					entradaAgregarLog.p_IdTrabajo = 1L;
					String idTipoRegistro = "";
					for (Garantia gar: garantias) {
						switch (gar.IdOrigenGarantia) {
					        case "CUS":
					        	idTipoRegistro = "CUSTODIA";
					            break;
					        case "CTA":
					        	idTipoRegistro = "CUENTA INVERSION";
					            break;
					        case "ESP":
					        	idTipoRegistro = "ESPECIES";
					            break;
					        default:
					        	idTipoRegistro = "CUSTODIA";
					            break;
					    }
						
						AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
						log.IdTrabajo = entradaAgregarLog.p_IdTrabajo;
						log.IdCabeceraLog = gar.IdPortafolio;
						log.IdTipoLog = "ANULACION";
						log.IdModulo = "CUS";
						log.IdUsuario = entrada.p_IdUsuario;
						log.IdEmpresa = entrada.p_IdEmpresa;
						log.FechaSistemaLog = fechaSistema;
						log.IdUsuarioAtendido = entrada.p_IdUsuario;
						log.IdCliente = gar.IdCliente;
						log.EstadoCliente = "";
						log.NombreCliente = "";
						log.IdTipoDocumentoIdentidad = "";
						log.IdDocumentoIdentidad = "";
						log.IdPortafolio = gar.IdPortafolio;
						log.EstadoPortafolio = "";
						log.IdTipoRegistro = idTipoRegistro;
						log.EstadoRegistro = "";
						log.IdTipoOperacion = "INGGTA";
						log.Folio = 0L;
						log.Cantidad = gar.INGCantidad;
						log.PrecioTasa = BigDecimal.ZERO;
						log.Monto = gar.MontoGarantia;
						log.IdMonedaOrigen = gar.IdMonedaInstrumento;
						log.IdMonedaLiquidacion = "";
						log.TipoCambio = "";
						log.Nemo = "";
						log.IdProceso = 0L;
						log.EstadoProceso = "";
						log.NombreProceso = "";
						
						entradaAgregarLog.p_Log.add(log);
					}
					agregarLogService.logica(con, entradaAgregarLog);
					logger.info("INFO::AnularGarantia --- Se termina de agregar Anulaciones al Log");
				}
			}
			
			sql.batch(con,SQL_UPDATE_MOVIMIENTOS_EN_TRANSITO, movimientosIngreso, MovimientoEnTransito.class);	
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"UI_ANULA_MOVIMIENTO_EN_TRANSITO_OK",new HashMap<String,String>(0),con, entrada.p_IdEmpresa));
			
		}
		
		paramsMovtos.clear();
		paramsMovtos.put("idsMovimientoCarteraTransito", entrada.p_MovimientosEnTransito.stream().filter(movto -> ("RETGTA").equals(movto.IdTipoOperacion)).map(mvto -> mvto.IdMovimientoCarteraEnTransito).collect(Collectors.toList()) );
		List<MovimientoEnTransito> movimientosRetiro = sql.query(con, SQL_SELECT_MOVIMIENTOS_EN_TRANSITO, paramsMovtos, MovimientoEnTransito.class);
		
		if(!movimientosRetiro.isEmpty()) {
			if( movimientosRetiro.stream().filter(mvto-> !"CRT".equals(mvto.IdEstado)).collect(Collectors.toList()).size() > 0 ) {
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_MOVIMIENTO_EN_TRANSITO_ESTADO_ANULADO",new HashMap<String,String>(0),con, entrada.p_IdEmpresa));
				salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
				return salida;
			}

			if(movimientosRetiro.stream().filter(mvto-> "S".equals(mvto.ExcluyeMovimientoEnCartola)).collect(Collectors.toList()).size() > 0 ) {
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_MOVIMIENTO_EN_TRANSITO_CONTRAPARTIDA_GENERADA",new HashMap<String,String>(0),con, entrada.p_IdEmpresa));
				salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
				return salida;
			}
			
			List<Long> foliosGarantia = new ArrayList<Long>();
			List<MovimientoEnTransito> movimientosCTA = movimientosRetiro.stream().filter(mvto-> "CTA".equals(mvto.IdOrigenGarantia)).collect(Collectors.toList());
			List<MovimientoEnTransito> movimientosCUS = movimientosRetiro.stream().filter(mvto-> "CUS".equals(mvto.IdOrigenGarantia)).collect(Collectors.toList());
			List<MovimientoEnTransito> movimientosESP = movimientosRetiro.stream().filter(mvto-> "ESP".equals(mvto.IdOrigenGarantia)).collect(Collectors.toList());

			List<RetiroGarantia> retiroGarantias = new ArrayList<RetiroGarantia>();
			List<RetiroGarantia> retiroGarantiasCTA = new ArrayList<RetiroGarantia>();
			List<RetiroGarantia> retiroGarantiasCUS = new ArrayList<RetiroGarantia>();
			List<RetiroGarantia> retiroGarantiasESP = new ArrayList<RetiroGarantia>();
			
			if(!movimientosCTA.isEmpty()) {
				retiroGarantiasCTA = sql.query(con, SQL_SELECT_RETIRO_GARANTIA_CTA, paramsMovtos, RetiroGarantia.class);
			}

			if(!movimientosCUS.isEmpty()) {
				paramsMovtos.put("foliosGarantia", movimientosRetiro.stream().map(mvto -> mvto.FolioGarantia).distinct().collect(Collectors.toList()));
				retiroGarantiasCUS = sql.query(con, SQL_SELECT_RETIRO_GARANTIA_CUS, paramsMovtos, RetiroGarantia.class);
			}

			if(!movimientosESP.isEmpty()) {
				retiroGarantiasESP = sql.query(con, SQL_SELECT_RETIRO_GARANTIA_ESP, paramsMovtos, RetiroGarantia.class);
			}
			
			IngresarGarantiaEntrada entradaIngresarGarantia = new IngresarGarantiaEntrada();
			entradaIngresarGarantia.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaIngresarGarantia.p_IdUsuario = entrada.p_IdUsuario;
			entradaIngresarGarantia.p_IdModulo = entrada.p_IdModulo;
			entradaIngresarGarantia.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
			entradaIngresarGarantia.p_SimulaOperacion = "N";
			entradaIngresarGarantia.p_Certifica = "N";
			entradaIngresarGarantia.p_FechaMovimiento = fechaSistema;
			List<IngresoCustodia.ObjDatosEntrada> p_DatosReingresoGarantia = new ArrayList<IngresoCustodia.ObjDatosEntrada>();
			
			HashMap<String, Object> params = new HashMap<>(1);
			
			if(!retiroGarantiasCTA.isEmpty()) {
				for(RetiroGarantia gta : retiroGarantiasCTA) {
					
					salida.P_OUT_MENSAJES.addAll(validacionesGeneralesAnulacionGarantia(con, entrada.p_IdEmpresa, gta, fechaSistema));
					salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
					if(salida.P_OUT_ERRORESCONTADOR > 0) {
						salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_INGRESO_GARANTIAS",new HashMap<String,String>(0),con, entrada.p_IdEmpresa));
						return salida;
					}
					
					AnularCargoAbono_Entrada entradaAnularIngresoEgreso = new AnularCargoAbono_Entrada();
					entradaAnularIngresoEgreso.p_IdEmpresa = entrada.p_IdEmpresa;
					entradaAnularIngresoEgreso.p_IdUsuario = entrada.p_IdUsuario;
					entradaAnularIngresoEgreso.p_IdModulo = entrada.p_IdModulo;
					entradaAnularIngresoEgreso.p_simularOperacion = "N";
					entradaAnularIngresoEgreso.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
					entradaAnularIngresoEgreso.p_FechaMovimiento = gta.FechaOperacion;
					entradaAnularIngresoEgreso.p_EsAnulacionManual	= "S";
					CargoAbono_Entrada cargoAbono = new CargoAbono_Entrada();
					cargoAbono.IdCargoAbonoMovimiento = gta.IdCargoAbonoMovimiento;
					cargoAbono.GlosaAnulacion = "Anulacion Retiro de Garantia";
					entradaAnularIngresoEgreso.p_CargoAbonoAAnular.add(cargoAbono);
					AnularCargoAbono servicioAnularCargoAbono = new AnularCargoAbono();
					AnularCargoAbono_Salida salidaAnularCargoAbono = servicioAnularCargoAbono.logica(con, entradaAnularIngresoEgreso);
					salida.P_OUT_MENSAJES.addAll(salidaAnularCargoAbono.P_OUT_MENSAJES);
					salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
					
					gta.EGRMontoMonedaInstrumento = gta.MontoGarantia.subtract(gta.INGCantidad.add( gta.MontoCompraMonedaInstrumento));
					gta.EGRCantidad = gta.MontoGarantia.subtract(gta.INGCantidad.add( gta.MovimientoCantidad));
					
					IngresoCustodia.ObjDatosEntrada	aux = new IngresoCustodia.ObjDatosEntrada();
					aux.IdGenerarMovimientoCustodia		= 1L;
					aux.TablaAfectada					= "GARANTIA";								
					aux.AccionOperacion					= "INGRESO";
					aux.EsRegistroNuevo					= "N";
					aux.IdMovimientoCarteraEnTransitoOrigen = gta.IdMovimientoCarteraEnTransitoOrigen;
					aux.IdConcepto						= gta.IdConcepto;
					aux.IdCliente						= gta.IdCliente;
					aux.IdPortafolio					= gta.IdPortafolio;
					aux.FechaOperacion					= Fecha.toDate(gta.FechaOperacion);
					aux.IdInstrumento					= gta.IdInstrumento != null ? gta.IdInstrumento : 0L;
					aux.IdCustodia						= gta.IdGarantiaVigente > 0 ? gta.IdGarantiaVigente : gta.IdGarantiaOrigen;
					aux.FechaVencimientoInstrumento 	= Fecha.toDate(gta.FechaVencimientoInstrumento);
					aux.FolioCarteraOrigen				= gta.FolioGarantia;
					aux.TasaCompra						= gta.Tasa;
					aux.PrecioCompra					= gta.Precio;
					aux.IdMonedaInstrumento				= gta.IdMonedaInstrumento;
					aux.IdMonedaEmision					= gta.IdMonedaEmision;
					aux.IdTipoReajusteInstrumento		= gta.IdTipoReajuste;	
					aux.EsFungible						= gta.EsFungible;
					aux.MontoCompraMonedaInstrumento	= "LIQ".equals(gta.IdEstado) ? gta.MontoCompraMonedaInstrumento : gta.CantidadVigente.add( gta.MontoCompraMonedaInstrumento);	
					aux.IdOrigenGarantia				= gta.IdOrigenGarantia;
					aux.IdTipoOperacion					= "INGGTA";
					aux.IdCondicionLiquidacion			= "T0";
					aux.FechaLiquidacion				= Fecha.toDate(gta.FechaOperacion);
					aux.IdEmisor						= gta.IdEmisor;
					aux.Cantidad						= "LIQ".equals(gta.IdEstado) ? gta.MovimientoCantidad : gta.CantidadVigente.add( gta.MovimientoCantidad);
					aux.ValorRescate					= BigDecimal.ZERO;
					p_DatosReingresoGarantia.add(aux);
					gta.IdEstado = "ANU";
					gta.IdGarantia = gta.IdGarantiaVigente;
					retiroGarantias.add(gta);
				}
			}
			
			if(!retiroGarantiasCUS.isEmpty()) {
				List<Long> valoresCarteraGarantias = retiroGarantiasCUS.parallelStream().map(garantia->garantia.FolioCarteraOrigen).collect(Collectors.toList());
				params.put("FolioCarteraOrigen", valoresCarteraGarantias);
				List<Cartera_Proceso> carterasVigentes = sql.query(con, SQL_SELECT_CARTERA_VIGENTE, params, Cartera_Proceso.class);

				for(RetiroGarantia gta : retiroGarantiasCUS) {
					salida.P_OUT_MENSAJES.addAll(validacionesGeneralesAnulacionGarantia(con, entrada.p_IdEmpresa, gta, fechaSistema));
					salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
					if(salida.P_OUT_ERRORESCONTADOR > 0) {
						salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_INGRESO_GARANTIAS",new HashMap<String,String>(0),con, entrada.p_IdEmpresa));
						return salida;
					}
					
					ConsultarCarteraIn entradaConsultarCarteras = new ConsultarCarteraIn();
					entradaConsultarCarteras.p_IdEmpresa = entrada.p_IdEmpresa;
					entradaConsultarCarteras.p_IdModulo = entrada.p_IdModulo;
					entradaConsultarCarteras.p_IdUsuario = entrada.p_IdUsuario;
					entradaConsultarCarteras.p_tipoFecha = "ADQ";
					entradaConsultarCarteras.p_IdInstrumento = gta.IdInstrumento;
					entradaConsultarCarteras.p_FolioCartera = gta.FolioCarteraOrigen;
					entradaConsultarCarteras.p_FechaConsulta = gta.FechaOperacion;
					entradaConsultarCarteras.p_IdClasificacionTipoInstrumento = "";
					entradaConsultarCarteras.p_IdCliente = gta.IdCliente;
					entradaConsultarCarteras.p_IdEstado = "";
					entradaConsultarCarteras.p_IdMoneda = "";
					entradaConsultarCarteras.p_IdPortafolio = gta.IdPortafolio;
					entradaConsultarCarteras.p_IdTipoInstrumento = "";
					
					ConsultarCarteras consultarCarteras = new ConsultarCarteras();
					ConsultarCarterasOut salidaConsultarCarteras = consultarCarteras.logica(con, entradaConsultarCarteras);
					
					if(salidaConsultarCarteras.P_OUT_TABLA.isEmpty()) {
						salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_CARTERA_SIN_CUSTODIA_DISPONIBLE_ANULACION_GTA",new HashMap<String,String>(0),con, entrada.p_IdEmpresa));
						salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
						return salida;
					} else {
						Tabla_Salida cartera = salidaConsultarCarteras.P_OUT_TABLA.get(0);
						BigDecimal diferencia = "IF".equals(gta.IdClasificacionTipoInstrumento) ? cartera.ValorRescate.subtract(gta.ValorRescate) : cartera.Cantidad.subtract(gta.MovimientoCantidad);
						
						if(diferencia.compareTo(BigDecimal.ZERO) < 0) {
							salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_CARTERA_SIN_CUSTODIA_DISPONIBLE_ANULACION_GTA",new HashMap<String,String>(0),con, entrada.p_IdEmpresa));
							salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
							return salida;
						}
					}
					
					Map<Long,Cartera_Proceso> mapCarterasVigentes = carterasVigentes.parallelStream().collect(Collectors.toMap(cartera->cartera.FolioCartera, cartera->cartera));
					
					RetiroCustodiaEntrada retiroCustodiaEntrada = new RetiroCustodiaEntrada();
					RetiroCustodia.ObjDatosEntrada retiroC = retiroCartera(mapCarterasVigentes.get(gta.FolioCarteraOrigen), fechaHoraSistema, fechaSistema, gta);
					
					retiroCustodiaEntrada.p_DatosEntrada.add(retiroC);
					
					if(retiroCustodiaEntrada.p_DatosEntrada.size() > 0) {
						retiroCustodiaEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
						retiroCustodiaEntrada.p_IdModulo = entrada.p_IdModulo;
						retiroCustodiaEntrada.p_IdUsuario = entrada.p_IdUsuario;
						retiroCustodiaEntrada.p_SimularOperacion = entrada.p_SimularOperacion;
						retiroCustodiaEntrada.p_TicketAtencion = 0L;
						retiroCustodiaEntrada.p_ValorizacionAutomatica = "N";
						RetiroCustodiaSalida retiroCustodiaSalida = retiroCustodiaService.logica(con,retiroCustodiaEntrada);
						salida.P_OUT_MENSAJES.addAll(retiroCustodiaSalida.P_OUT_MENSAJES);
						
						salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
						if(salida.P_OUT_ERRORESCONTADOR > 0) {
							salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_INGRESO_GARANTIAS",new HashMap<String,String>(0),con, entrada.p_IdEmpresa));
							return salida;
						}
						
						IngresoCustodia.ObjDatosEntrada	aux = new IngresoCustodia.ObjDatosEntrada();
						aux.IdGenerarMovimientoCustodia		= 1L;
						aux.TablaAfectada					= "GARANTIA";								
						aux.AccionOperacion					= "INGRESO";
						aux.EsRegistroNuevo					= "N";
						aux.IdConcepto						= gta.IdConcepto;
						aux.IdCliente						= gta.IdCliente;
						aux.IdPortafolio					= gta.IdPortafolio;
						aux.FechaOperacion					= Fecha.toDate(gta.FechaOperacion);
						aux.IdInstrumento					= gta.IdInstrumento;
						aux.IdCustodia						= gta.IdGarantiaVigente > 0L ? gta.IdGarantiaVigente : gta.IdGarantiaOrigen;
						aux.FechaVencimientoInstrumento 	= Fecha.toDate(gta.FechaVencimientoInstrumento);
						aux.FolioCarteraOrigen				= gta.FolioGarantia;
						aux.TasaCompra						= gta.Tasa;
						aux.PrecioCompra					= gta.Precio;
						aux.IdMonedaInstrumento				= gta.IdMonedaInstrumento;
						aux.IdMonedaEmision					= gta.IdMonedaEmision;
						aux.IdTipoReajusteInstrumento		= gta.IdTipoReajuste;	
						aux.EsFungible						= gta.EsFungible;
						aux.MontoCompraMonedaInstrumento	= ("LIQ").equals(gta.IdEstado) ? gta.MontoCompraMonedaInstrumento : gta.CantidadVigente.add(gta.MontoCompraMonedaInstrumento);		
						aux.IdOrigenGarantia				= gta.IdOrigenGarantia;
						aux.IdTipoOperacion					= gta.IdTipoOperacion;
						aux.IdCondicionLiquidacion			= "T0";
						aux.FechaLiquidacion				= Fecha.toDate(gta.FechaOperacion);
						aux.IdEmisor						= gta.IdEmisor;
						if (("IF").equalsIgnoreCase(gta.IdClasificacionTipoInstrumento)){
							BigDecimal cantidadCalculada 	= ("LIQ").equals(gta.IdEstado) ? gta.MovimientoCantidad : gta.MovimientoCantidad.add(gta.INGCantidad);
							aux.Cantidad				 	= ("N").equalsIgnoreCase(aux.EsFungible) ? cantidadCalculada : BigDecimal.ONE;
							aux.ValorRescate			 	= ("LIQ").equals(gta.IdEstado) ? gta.MovimientoValorRescate : gta.ValorRescate.add(gta.MovimientoValorRescate);
						} else {
							aux.Cantidad				 	= ("LIQ").equals(gta.IdEstado) ? gta.MovimientoCantidad : gta.MovimientoCantidad.add(gta.CantidadVigente);
							aux.ValorRescate			 	= BigDecimal.ZERO;
						}
						
						p_DatosReingresoGarantia.add(aux);
						foliosGarantia.add(gta.FolioGarantia);
						gta.IdEstado = "ANU";
						gta.IdGarantia = gta.IdGarantiaVigente;
						retiroGarantias.add(gta);
					}
					
				}
			}
			
			if(!retiroGarantiasESP.isEmpty()) {
				for(RetiroGarantia gta : retiroGarantiasESP) {
					salida.P_OUT_MENSAJES.addAll(validacionesGeneralesAnulacionGarantia(con, entrada.p_IdEmpresa, gta, fechaSistema));
					salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
					if(salida.P_OUT_ERRORESCONTADOR > 0) {
						salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_INGRESO_GARANTIAS",new HashMap<String,String>(0),con, entrada.p_IdEmpresa));
						return salida;
					}
					
					IngresoCustodia.ObjDatosEntrada	aux = new IngresoCustodia.ObjDatosEntrada();
					aux.IdGenerarMovimientoCustodia		= 1L;
					aux.TablaAfectada					= "GARANTIA";								
					aux.AccionOperacion					= "INGRESO";
					aux.EsRegistroNuevo					= "N";
					aux.IdMovimientoCarteraEnTransitoOrigen = gta.IdMovimientoCarteraEnTransitoOrigen;
					aux.IdConcepto						= gta.IdConcepto;
					aux.IdCliente						= gta.IdCliente;
					aux.IdPortafolio					= gta.IdPortafolio;
					aux.FechaOperacion					= Fecha.toDate(gta.FechaOperacion);
					aux.IdInstrumento					= gta.IdInstrumento;
					aux.IdCustodia						= gta.IdGarantia > 0 ? gta.IdGarantia : gta.IdGarantiaOrigen;
					aux.FechaVencimientoInstrumento 	= Fecha.toDate(gta.FechaVencimientoInstrumento);
					aux.FolioCarteraOrigen				= gta.FolioGarantia;
					aux.TasaCompra						= gta.Tasa;
					aux.PrecioCompra					= gta.Precio;
					aux.IdMonedaInstrumento				= gta.IdMonedaInstrumento;
					aux.IdMonedaEmision					= gta.IdMonedaEmision;
					aux.IdTipoReajusteInstrumento		= gta.IdTipoReajuste;	
					aux.EsFungible						= gta.EsFungible;
					aux.MontoCompraMonedaInstrumento	= gta.MontoGarantia;	
					aux.IdOrigenGarantia				= gta.IdOrigenGarantia;
					aux.IdTipoOperacion					= "INGGTA";
					aux.IdCondicionLiquidacion			= "T0";
					aux.FechaLiquidacion				= Fecha.toDate(gta.FechaOperacion);
					aux.IdEmisor						= gta.IdEmisor;
					aux.Cantidad						= gta.MovimientoCantidad;
					aux.ValorRescate					= BigDecimal.ZERO;
					p_DatosReingresoGarantia.add(aux);
					
					retiroGarantias.add(gta);
				}
			}
			
			if(!p_DatosReingresoGarantia.isEmpty()) {
				IngresoCustodia ingresoCustodia = new IngresoCustodia();
				IngresoCustodia.IngresoCustodiaEntrada paramInSP = new IngresoCustodia.IngresoCustodiaEntrada();
				paramInSP.p_IdEmpresa			= entrada.p_IdEmpresa;
				paramInSP.p_IdModulo			= entrada.p_IdModulo;
				paramInSP.p_SimularOperacion	= entrada.p_SimularOperacion;
				paramInSP.p_TicketAtencion		= 0L;
				paramInSP.p_DatosEntrada		= p_DatosReingresoGarantia;
				
				IngresoCustodia.IngresoCustodiaSalida resultIngresoTransito = ingresoCustodia.logica(con, paramInSP);
				salida.P_OUT_MENSAJES.addAll(resultIngresoTransito.P_OUT_MENSAJES);
				salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
				if(salida.P_OUT_ERRORESCONTADOR > 0) {
					salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_REINGRESO_GARANTIA",new HashMap<String,String>(0),con, entrada.p_IdEmpresa));
					return salida;
				}else {
					salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"UI_REINGRESO_GARANTIA_OK",new HashMap<String,String>(0),con, entrada.p_IdEmpresa));
				}
			}
			
			if(!entradaIngresarGarantia.p_GarantiaCustodia.isEmpty() || !entradaIngresarGarantia.p_GarantiaCuentaInversion.isEmpty()) {
				IngresarGarantiaJava servicioIngresoGarantia = new IngresarGarantiaJava();
				IngresarGarantiaSalida salidaIngresoGarantia = servicioIngresoGarantia.logica(con, entradaIngresarGarantia);
				
				salida.P_OUT_MENSAJES.addAll(salidaIngresoGarantia.P_OUT_MENSAJES);
				salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
				if(salida.P_OUT_ERRORESCONTADOR > 0) {
					salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_INGRESO_GARANTIAS",new HashMap<String,String>(0),con, entrada.p_IdEmpresa));
					return salida;
				}
			}
			
			sql.batch(con,SQL_UPDATE_GARANTIA, retiroGarantias, RetiroGarantia.class);

			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"UI_ANULA_RETIRO_GARANTIAS_OK",new HashMap<String,String>(0),con, entrada.p_IdEmpresa));
			
			for(MovimientoEnTransito mov : movimientosRetiro){
				mov.IdUsuarioAnulacion = entrada.p_IdUsuario;
				mov.FechaAnulacionRegistro= fechaHoraSistema; 
			}
			sql.batch(con,SQL_UPDATE_MOVIMIENTOS_EN_TRANSITO, movimientosRetiro, MovimientoEnTransito.class);	
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"UI_ANULA_MOVIMIENTO_EN_TRANSITO_OK",new HashMap<String,String>(0),con, entrada.p_IdEmpresa));
			
			{
				logger.info("INFO::AnularGarantia --- Se agregan Anulaciones al Log");
				AgregarLog agregarLogService = new AgregarLog();
				AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
				entradaAgregarLog.p_IdTrabajo = 1L;
				String idTipoRegistro = "";
				for (RetiroGarantia gar: retiroGarantias) {
					AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
					switch (gar.IdOrigenGarantia) {
				        case "CUS":
				        	idTipoRegistro = "CUSTODIA";
				            break;
				        case "CTA":
				        	idTipoRegistro = "CUENTA INVERSION";
				            break;
				        case "ESP":
				        	idTipoRegistro = "ESPECIES";
				            break;
				        default:
				        	idTipoRegistro = "CUSTODIA";
				            break;
				    }
					
					log.IdTrabajo = entradaAgregarLog.p_IdTrabajo;
					log.IdCabeceraLog = gar.IdPortafolio;
					log.IdTipoLog = "ANULACION";
					log.IdModulo = "CUS";
					log.IdUsuario = entrada.p_IdUsuario;
					log.IdEmpresa = entrada.p_IdEmpresa;
					log.FechaSistemaLog = fechaSistema;
					log.IdUsuarioAtendido = entrada.p_IdUsuario;
					log.IdCliente = gar.IdCliente;
					log.EstadoCliente = "";
					log.NombreCliente = "";
					log.IdTipoDocumentoIdentidad = "";
					log.IdDocumentoIdentidad = "";
					log.IdPortafolio = gar.IdPortafolio;
					log.EstadoPortafolio = "";
					log.IdTipoRegistro = idTipoRegistro;
					log.EstadoRegistro = "";
					log.IdTipoOperacion = gar.IdTipoOperacion;
					log.Folio = 0L;
					log.Cantidad =gar.MovimientoCantidad;
					log.PrecioTasa = BigDecimal.ZERO;
					log.Monto = ("ESP").equals(gar.IdOrigenGarantia) ? gar.MontoGarantia : gar.MontoCompraMonedaInstrumento;
					log.IdMonedaOrigen = gar.IdMonedaInstrumento;
					log.IdMonedaLiquidacion = "";
					log.TipoCambio = "";
					log.Nemo = "";
					log.IdProceso = 0L;
					log.EstadoProceso = "";
					log.NombreProceso = "";
					
					entradaAgregarLog.p_Log.add(log);
				}
				agregarLogService.logica(con, entradaAgregarLog);
				logger.info("INFO::AnularGarantia --- Se termina de agregar Anulaciones al Log");
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::AnularGarantia", fin - inicio);
		return salida;
	}
	
	private List<MensajeSalida> validacionesGeneralesAnulacionGarantia(Connection con, Long p_IdEmpresa, RetiroGarantia gta, String fechaSistema) throws Exception {
		logger.info("INICIO::AnularGarantia.validacionesGeneralesAnulacionGarantia");
		long inicio = System.currentTimeMillis();
		List<MensajeSalida> salida = new ArrayList<MensajeSalida>();
		
		if("ANU".equals(gta.IdEstado)) {
			salida.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_GARANTIA_ESTADO_ANULADO",new HashMap<String,String>(0),con, p_IdEmpresa));
		}
		if(!fechaSistema.equals(gta.FechaOperacion)) {
			salida.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_FECHA_OPERACION_MOVIMIENTO",new HashMap<String,String>(0),con, p_IdEmpresa));
		}
		long fin = System.currentTimeMillis();
		logger.info("FIN::AnularGarantia.validacionesGeneralesAnulacionGarantia", fin - inicio);
		return salida;
	}

	private RetiroCustodia.ObjDatosEntrada anulacionCartera(Cartera_Proceso cartera, Timestamp fechaHoraSistema, String fechaSistema, String idConcepto) {
		logger.info("INICIO::AnularGarantia.anulacionCartera");
		long inicio = System.currentTimeMillis();
		RetiroCustodia.ObjDatosEntrada anulacionCartera = new RetiroCustodia.ObjDatosEntrada();

		anulacionCartera.IdGenerarMovimientoCustodia = cartera.IdCartera;
		anulacionCartera.TablaAfectada = cartera.TipoCartera;
		anulacionCartera.IdCustodia = cartera.IdCartera;
		anulacionCartera.FolioCustodia = cartera.FolioCartera;
		anulacionCartera.AccionOperacion = "RETIRO";
		anulacionCartera.FechaRetiroRegistro = fechaHoraSistema;
		anulacionCartera.FechaOperacion = Fecha.toDate(fechaSistema);
		anulacionCartera.IdInstrumento = cartera.IdInstrumento;
		anulacionCartera.Cantidad = cartera.INGCantidad;
		anulacionCartera.ValorRescate = cartera.INGValorRescate;
		anulacionCartera.IdCliente = cartera.IdCliente;
		anulacionCartera.IdPortafolio = cartera.IdPortafolio;
		anulacionCartera.PrecioCompra = cartera.INGPrecioCompra;
		anulacionCartera.TasaCompra = cartera.INGTasaCompra;
		anulacionCartera.IdOrigenGarantia = cartera.IdOrigenGarantia;
		anulacionCartera.IdTipoOperacion = "ANINGA";
		anulacionCartera.IdConcepto = "".equals(idConcepto) ? idConcepto : cartera.IdConcepto;
		anulacionCartera.FechaLiquidacion = cartera.FechaLiquidacion;
		anulacionCartera.IdTipoReajusteInstrumento = cartera.IdTipoReajuste;
		anulacionCartera.EsFungible= cartera.EsFungible;
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::AnularGarantia.anulacionCartera", fin - inicio);
		return anulacionCartera;
	}

	private IngresoCustodia.ObjDatosEntrada reingresoCartera(String fechaSistema, Cartera_Proceso map, Garantia garantia){
		logger.info("INICIO::AnularGarantia.reingresoCartera");
		long inicio = System.currentTimeMillis();

		IngresoCustodia.ObjDatosEntrada reingresoCartera = new IngresoCustodia.ObjDatosEntrada();
		BigDecimal cantidadVigente = BigDecimal.ZERO;
		BigDecimal cantidadRescate = BigDecimal.ZERO;
		if(map != null) {
			switch (map.IdClasificacionTipoInstrumento) {
				case "IF":
					cantidadRescate = garantia.ValorRescate;
					break;
				case "RV":
					cantidadVigente = garantia.INGCantidad;
					break;
				case "RF":
					cantidadVigente = garantia.INGCantidad;
					break;
			}
		}
		
		reingresoCartera.IdGenerarMovimientoCustodia = map.IdCartera;
		reingresoCartera.TablaAfectada = "CARTERA";
		reingresoCartera.AccionOperacion = "INGRESO";
		reingresoCartera.IdCliente = map.IdCliente;
		reingresoCartera.IdPortafolio = map.IdPortafolio;
		reingresoCartera.FolioCustodia = map.FolioCartera;
		reingresoCartera.IdCustodia = map.IdCartera;
		reingresoCartera.TasaCompra = map.INGTasaCompra;
		reingresoCartera.PrecioCompra = map.INGPrecioCompra;
		reingresoCartera.FechaLiquidacion = map.INGFechaLiquidacion;
		reingresoCartera.Cantidad = map.INGCantidad.add(cantidadVigente, MathContext.DECIMAL128);
		reingresoCartera.IdInstrumento = map.IdInstrumento;
		reingresoCartera.ValorRescate = map.INGValorRescate.add(cantidadRescate, MathContext.DECIMAL128);
		reingresoCartera.FechaOperacion = Fecha.toDate(fechaSistema);
		reingresoCartera.EsRegistroNuevo = "N";
		reingresoCartera.GenerarNuevoFolioCustodia = "N";
		reingresoCartera.MantieneFolioCustodia = "S";
		reingresoCartera.PlazoEnDiasOperacion = map.PlazoEnDiasOperacion;
		reingresoCartera.IdEmisor = map.IdEmisor;
		reingresoCartera.IdTipoReajusteInstrumento = map.IdTipoReajuste;
		reingresoCartera.IdMonedaEmision = map.IdMonedaEmision;
		reingresoCartera.IdTipoOperacion = "ANINGA";
		reingresoCartera.IdConcepto = garantia.IdConcepto;
		reingresoCartera.Nemotecnico = map.Nemotecnico;
		reingresoCartera.IdTipoComprobante = map.IdTipoComprobante;
		reingresoCartera.FolioComprobante = map.FolioComprobante;
		reingresoCartera.IdBoveda = map.IdBoveda;
		reingresoCartera.FechaVencimientoInstrumento = map.FechaVencimientoInstrumento;
		reingresoCartera.FechaVencimientoOperacion = map.FechaVencimientoOperacion;
		reingresoCartera.IdMonedaAdicional = map.IdMonedaAdicional;		
		reingresoCartera.CorteMinimo = map.CorteMinimo;
		reingresoCartera.FechaEmisionInstrumento = map.FechaEmisionInstrumento;
		reingresoCartera.IdMonedaInstrumento = map.IdMonedaInstrumento;
		reingresoCartera.IdMesa = map.IdMesa;
		reingresoCartera.IdBolsa = map.IdBolsa;
		reingresoCartera.IdCondicionLiquidacion = map.IdCondicionLiquidacion;
		reingresoCartera.IdAgente = map.IdAgente;
		reingresoCartera.IdSucursal = map.IdSucursal;
		reingresoCartera.MontoCompra = map.MontoCompra;
		reingresoCartera.IdMovimientoCarteraEnTransitoOrigen = map.IdMovimientoCarteraEnTransito;
		reingresoCartera.EsFungible = map.EsFungible;
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::AnularGarantia.reingresoCartera", fin - inicio);
		return reingresoCartera;
	}
	
	private RetiroCustodia.ObjDatosEntrada retiroCartera(Cartera_Proceso cartera, Timestamp fechaHoraSistema, String fechaSistema, RetiroGarantia gta) {
		logger.info("INICIO::AnularGarantia.retiroCartera");
		long inicio = System.currentTimeMillis();
		
		RetiroCustodia.ObjDatosEntrada anulacionCartera = new RetiroCustodia.ObjDatosEntrada();
		BigDecimal cantidadVigente = BigDecimal.ZERO;
		BigDecimal cantidadRescate = BigDecimal.ZERO;
		if(gta != null) {
			switch (gta.IdClasificacionTipoInstrumento) {
				case "IF":
					cantidadRescate = gta.MovimientoValorRescate;
					break;
				case "RV":
					cantidadVigente = gta.MovimientoCantidad;
					break;
				case "RF":
					cantidadVigente = gta.MovimientoCantidad;
					break;
			}
		}
		
		anulacionCartera.IdGenerarMovimientoCustodia = cartera.IdCartera;
		anulacionCartera.TablaAfectada = cartera.TipoCartera;
		anulacionCartera.IdCustodia = cartera.IdCartera;
		anulacionCartera.FolioCustodia = cartera.FolioCartera;
		anulacionCartera.AccionOperacion = "RETIRO";
		anulacionCartera.FechaRetiroRegistro = fechaHoraSistema;
		anulacionCartera.FechaOperacion = Fecha.toDate(fechaSistema);
		anulacionCartera.IdInstrumento = cartera.IdInstrumento;
		anulacionCartera.Cantidad = cantidadVigente;
		anulacionCartera.ValorRescate = cantidadRescate;
		anulacionCartera.IdCliente = cartera.IdCliente;
		anulacionCartera.IdPortafolio = cartera.IdPortafolio;
		anulacionCartera.PrecioCompra = cartera.INGPrecioCompra;
		anulacionCartera.TasaCompra = cartera.INGTasaCompra;
		anulacionCartera.IdOrigenGarantia = "CUS";
		anulacionCartera.IdTipoOperacion = "ANREGA";
		anulacionCartera.IdConcepto = gta.IdConcepto;
		anulacionCartera.FechaLiquidacion = cartera.FechaLiquidacion;
		anulacionCartera.IdTipoReajusteInstrumento = cartera.IdTipoReajuste;
		anulacionCartera.EsFungible= cartera.EsFungible;
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::AnularGarantia.retiroCartera", fin - inicio);
		return anulacionCartera;
	}
	
	public static void main(String[] args) {
		try (Connection con = PlataformaUtils.getPropertiesConnection("qa02")) {
			try {
				System.out.println("COMIENZA");
				Gson gson = Utils.getGsonBuilder();
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdModulo\":\"CUS\",\"p_UsaReglasNegocio\":\"S\",\"p_MovimientosEnTransito\":[{\"IdMovimientoCarteraEnTransito\":801740,\"IdTipoOperacion\":\"RETGTA\"}]}";
				EntradaAnularGarantia Objin = gson.fromJson(json, EntradaAnularGarantia.class);

				AnularGarantia kk = new AnularGarantia();
				SalidaAnularGarantia asdf = kk.logica(con, Objin);
				System.out.println(asdf);
//				con.commit();
				con.rollback();
				System.out.println("END");
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
