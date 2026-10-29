package cl.bcs.mesa.FWD;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.mesa.MES.CambiarEstadoOperacion;
import cl.bcs.mesa.MES.CambiarEstadoOperacion.CambiarEstadoOperacionEntrada;
import cl.bcs.mesa.MES.CambiarEstadoOperacion.CambiarEstadoOperacionSalida;
import cl.bcs.mesa.MES.CambiarEstadoOperacion.ObjCambiarEstadoOperacionIN;
import cl.bcs.cuentaInversion.CrearCargoAbono;
import cl.bcs.cuentaInversion.CrearCargoAbono.CrearCargoAbonoEntrada;
import cl.bcs.cuentaInversion.CrearCargoAbono.CrearCargoAbonoSalida;
import cl.bcs.cuentaInversion.CrearCargoAbono.ObjCargoAbonoAGenerar;
import cl.bcs.mesa.FWD.CrearOperacionForward.OperacionForwardObjOut;
import cl.bcs.mesa.FWD.CrearOperacionSpotOrigenForward.CrearOperacionSpotOrigenForwardEntrada;
import cl.bcs.mesa.FWD.CrearOperacionSpotOrigenForward.CrearOperacionSpotOrigenForwardSalida;
import cl.bcs.mesa.FWD.CrearOperacionSpotOrigenForward.SpotOrigenForwardObjIn;
import cl.bcs.mesa.FWD.CrearOperacionSpotOrigenForward.SpotOrigenForwardObjOut;
import cl.bcs.mesa.FWD.IngresarForwardCartera.IngresarForwardCarteraEntrada;
import cl.bcs.mesa.FWD.IngresarForwardCartera.IngresarForwardCarteraSalida;
import cl.bcs.mesa.FWD.IngresarForwardCartera.TablaForwardObjIn;
import cl.bcs.mesa.FWD.IngresarForwardCartera.TablaForwardObjOut;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.tesoreria.CrearIngresoEgreso;
import cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Entrada;
import cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Salida;
import cl.bcs.tesoreria.CrearIngresoEgreso.FormaDePago_Entrada;
import cl.bcs.tesoreria.CrearIngresoEgreso.IngresoEgreso_Entrada;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class VencimientoOperacionForward implements JavaCall{
	
	private final Log log = new Log("FWD.VencimientoOperacionForward");
	
	private final String SQL_SELECT_OPERACIONES_FORWARD = loadResourceToString("cl/bcs/mesa/FWD/sql/VencimientoOperacionForward_ConsultarOperacionesForward.sql");
	private final String SQL_UPDATE_CARTERA_REPROCESO = loadResourceToString("cl/bcs/mesa/FWD/sql/VencimientoOperacionForward_UpdateCarteraReproceso.sql");
	private final String SQL_UPDATE_CARTERA_ANTICIPADA_ID_ESTADO = loadResourceToString("cl/bcs/mesa/FWD/sql/VencimientoOperacionForward_UpdateCarteraAnticipadaIdEstado.sql");
	private final String SQL_SELECT_OPERACIONES_FORWARD_ENTREGA_FISICA = loadResourceToString("cl/bcs/mesa/FWD/sql/VencimientoOperacionForward_ConsultarOperacionesForwardEntregaFisica.sql");
	private final String SQL_UPDATE_OPERACIONES_SPOT_FOLIO_ORIGEN_FW = loadResourceToString("cl/bcs/mesa/FWD/sql/AnticiparOperacionForward_UpdateOperacionesSpotFolioOrigenFw.sql");
	private final String SQL_UPDATE_CARTERA_VENCIMIENTO_ENTREGA_FISICA = loadResourceToString("cl/bcs/mesa/FWD/sql/VencimientoOperacionForward_UpdateCarteraVencimientoEntregaFisica.sql");
	private final String SQL_SELECT_REQUISITOS_VALORIZACION_FORWARD = loadResourceToString("cl/bcs/mesa/FWD/sql/ValorizacionForward_ConsultarRequisitosValorizacionForward.sql");
	private final String SQL_SELECT_OPERACIONES_FORWARD_ENTREGA_FISICA_SIN_SPOT = loadResourceToString("cl/bcs/mesa/FWD/sql/VencimientoOperacionForward_ConsultarOperacionesForwardEntregaFisicaSinSpot.sql");
	private final String SQL_SELECT_OPERACIONES_FORWARD_ENTREGA_FISICA_FORMA_PAGO = loadResourceToString("cl/bcs/mesa/FWD/sql/VencimientoOperacionForward_ConsultarOperacionesForwardEntregaFisicaFormaDePago.sql");
	private final String SQL_UPDATE_CARTERA_VENCIMIENTO_ENTREGA_FISICA_MONEDALIQUIDACION = loadResourceToString("cl/bcs/mesa/FWD/sql/VencimientoOperacionForward_UpdateCarteraVencimientoEntregaFisicaMonedaLiquidacion.sql");
	
	public static class OperacionForwardObjIn{
		public Long IdCartera;
		public Long FolioCartera;
		public Long IdOperacion;
		public String IdEstado;
		public BigDecimal Precio;
		public String IdMonedaLiquidacion;
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class VencimientoOperacionForwardEntrada {
		public Long p_IdEmpresa;
		public Long p_IdPais;
		public Long p_IdUsuario;
		public String p_IdBolsa;
		public Long p_IdSucursal;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public List<OperacionForwardObjIn> p_tablaOperVencidas;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class VencimientoOperacionForwardSalida {
		public List<MensajeSalida>		P_OUT_MENSAJES;
		public Long						P_OUT_ERRORESCONTADOR;
		public Long						P_OUT_ADVERTENCIASCONTADOR;
		public List<OperacionForwardObjOut>	P_OUT_TBLIDFORWARD;
		
		public VencimientoOperacionForwardSalida() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_TBLIDFORWARD = new ArrayList<OperacionForwardObjOut>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class DatosCarteraVencimientoForward{
		public Long IdCartera;
		public Long FolioCartera;
		public Long IdOperacion;
		public String IdTipoOperacion;
		public String IdTipoInstrumento;
		public String IdClasificacionTipoInstrumento;
		public String IdTipoProducto;
		public Long IdInstrumento;
		public String Nemotecnico;
		public Long IdCliente;
		public Long IdPortafolio;
		public Long IdAgente;
		public String IdTipoCanalIngreso;
		public String IdMonedaPrincipal;
		public String IdMonedaSecundaria;
		public String EGRIdTipoOperacion;
		public BigDecimal INGMontoMonedaPrincipal;
		public BigDecimal EGRMontoMonedaPrincipal;
		public BigDecimal INGMontoMonedaSecundaria;
		public BigDecimal EGRMontoMonedaSecundaria;
		public Date FechaOperacion;
		public BigDecimal ValorTipoCambioCierre;
		public BigDecimal ValorParidadCierre;
		public Long Plazo;
		public Date FechaVencimientoOperacion;
		public Date FechaLiquidacion;
		public Long IdModalidadCumplimiento;
		public String ModalidadCumplimientoValor;
		public Long IdPlaza;
		public BigDecimal ValorTipoCambioCosto;
		public String INGEsPuntos;
		public BigDecimal INGTasaPuntosMonedaPrincipal;
		public BigDecimal TasaCompra;
		public Long IdFixing;
		public Date FechaFixing;
		public Time HoraFixing;
		public Long IdMesa;
		public Long EGRIdSpotAsociado;
		public String Glosa;
		public String IdEstado;
		public BigDecimal EGRMontoARecibirAnticipo;
		public BigDecimal TipoCambioMercadoDO;
		public BigDecimal TipoCambioMercadoUF;
		public String MultiplicaODivide;
		
		public String toString(){
			return Utils.printJson(this, false);
		}
	}
	
	public static class OperacionForwardVCD{
		public Long IdCartera;
		public Long FolioCartera;
		public Long IdOperacion;
		public java.sql.Timestamp FechaModificacionRegistro;
		public BigDecimal Precio;
		public BigDecimal MontoLiquidacion;
		
		public String toString(){
			return Utils.printJson(this, false);
		}
	}
		
	public static class RequisitosValorizacionForward{
		public String IdEstadoMesa;
		public String NombreMesa;
		public String IdEstadoModulo;
		public String NombreModulo;
		public Long CantidadCriterios;
		public BigDecimal TipoCambioDO;
		public BigDecimal TipoCambioUF;
		public BigDecimal TipoCambioEUR;
		public BigDecimal TipoCambioUSD;
		public BigDecimal PrecioSpotCompraUSD;
		public BigDecimal PrecioSpotVentaUSD;
		public BigDecimal ParidadMercadoEUR;
		public BigDecimal PrecioSpotPromedioUSD;
		
		public String toString(){
			return Utils.printJson(this, false);
		}
	}
	
	public static class ForwardFomaPago extends BcsPojo {
		public Long IdOperacion;
		public Long IdComprobantePagoOperacion;
		public String AfectarCuentaInversion;
		public String RecibePaga;
		public String IdMoneda;
		public BigDecimal Monto;
		public String AfectarTesoreria;
		public String IdMedioDePago;
		public String IdModalidadPago;
		public Long IdCuentaBancaria;
		public Long IdBanco;
		public String NumeroCuentaBancaria;
		public String IdTipoCuentaBancaria;
		public java.sql.Date FechaLiquidacionComprobantePagoOperacion;
		public String NombreTipoCuentaBancaria;
		public Long IdTarjetaBancaria;
		public String IdTipoTarjetaBancaria;
		public String NombreTipoTarjetaBancaria;
		public String NumeroTarjetaBancaria;
	}
	
	public static class ForwardSinSpot extends BcsPojo {
		public Long IdCartera;
		public String IdConcepto;
		public String IdCondicionLiquidacion;
		public Date FechaLiquidacion;
		public Long IdEmpleadoEjecutivo;
		public Long IdSucursalEmpleadoEjecutivo;
		public String IdTipoProducto;
		public String IdTipoOperacion;
		public Long IdOperacion;
		public Date FechaVencimientoOperacion;
		public String IdMonedaPrincipal;
		public BigDecimal INGMontoMonedaSecundaria;
		public Long IdInstrumento;
		public String IdTipoInstrumento;
		public BigDecimal EGRCantidad;
		public BigDecimal INGCantidad;
		public Long IdPortafolio;
		public String IdMonedaSecundaria;
		public Long IdPersona;
		public String IdClasificacionTipoInstrumento;
		public String InstrumentoNemotecnico;
		public ForwardSinSpot() {
			this.IdEmpleadoEjecutivo = 0L;
			this.IdSucursalEmpleadoEjecutivo = 0L;
		}
	}
	
	public static class CargoAbonoResultado extends BcsPojo {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		public CargoAbonoResultado() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR = 0L;
		}
	}
	
	public static class TesoreriaResultado extends BcsPojo {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		public TesoreriaResultado() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR = 0L;
		}
	}
	
	public static class ForwardCartera extends BcsPojo {
		public Long IdCartera;
		public Long IdOperacionSpot;
		public ForwardCartera(Long idCartera) {
			this.IdCartera = idCartera;
			this.IdOperacionSpot = 0L;
		}
	}
	
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		VencimientoOperacionForwardEntrada in = gson.fromJson(jin.toString(), VencimientoOperacionForwardEntrada.class);
		VencimientoOperacionForwardSalida out = logica(con, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		long c = 0;
		for (MensajeSalida s : out.P_OUT_MENSAJES) {
			if (s.tipo.equals("AUTORIZACION"))
				c++;
		}
		out.P_OUT_ADVERTENCIASCONTADOR = c;
		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public VencimientoOperacionForwardSalida logica(Connection conn, VencimientoOperacionForwardEntrada in) throws Exception {
		SQL _sql = new SQL();
		VencimientoOperacionForwardSalida out = new VencimientoOperacionForwardSalida();
		out.P_OUT_ERRORESCONTADOR = 0L;
		out.P_OUT_ADVERTENCIASCONTADOR = 0L;
		Long Id = 0L;
		HashMap<String,String> mapMsg = new HashMap<>();
		
		log.write("INICIO :: FWD.VencimientoOperacionForward");
		
		try {
			String fwdIntegradoConSpot = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "MESA", "FwdIntegradoConSpot");
			if (in.p_tablaOperVencidas.size() == 0) {
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_INGRESARON_OPERACIONES",mapMsg,conn,in.p_IdEmpresa));
				Id++;
				return out;
			}
			
			RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
			RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(conn, in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
			if(MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0){
				out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
				return out;
			}
			
			/***** VARIABLES *****/
			String fechaHoraSistemaSecondate = FechaDB.ObtenerFechaHoraSistemaUTC(conn,in.p_IdEmpresa);
			Long cantOpeIn = 0L;
			
			HashMap<String,Object> paramts = new HashMap<>();
			paramts.put("IdEmpresa", in.p_IdEmpresa);
			paramts.put("FechaProceso", fechaHoraSistemaSecondate.substring(0,10));
			
			List<RequisitosValorizacionForward> requisitosValorizacionForward = _sql.query(conn, SQL_SELECT_REQUISITOS_VALORIZACION_FORWARD, paramts, RequisitosValorizacionForward.class);
			
			HashMap<String,Object> params = new HashMap<>();
			List<TablaForwardObjIn> operacionesVencimientoVIG = new ArrayList<TablaForwardObjIn>();
			List<OperacionForwardVCD> operacionesVencimientoVCD = new ArrayList<OperacionForwardVCD>();
			List<OperacionForwardObjOut> operacionesAnticipadas = new ArrayList<OperacionForwardObjOut>();
			List<Long> idCartera = new ArrayList<Long>();
			
			for(OperacionForwardObjIn ope: in.p_tablaOperVencidas){
				idCartera.add(ope.IdCartera);
				cantOpeIn++;
			}
			
			params.put("IdEmpresa", in.p_IdEmpresa);
			params.put("IdCartera", idCartera);
			
			//Obtiene registros a Vencer (VIG) y registros a Reprocesar (VCD). Para ambos calcula el MontoLiquidacion
			List<DatosCarteraVencimientoForward> operacionesForward = _sql.query(conn, SQL_SELECT_OPERACIONES_FORWARD, params, DatosCarteraVencimientoForward.class);
			HashMap<Long, Object> operacionesForwardHash = Utils.List2Hash(operacionesForward, "IdCartera");
			
			for(DatosCarteraVencimientoForward opFw: operacionesForward){
				for (RequisitosValorizacionForward req: requisitosValorizacionForward){
					if(opFw.IdMonedaPrincipal.equals("USD") && req.TipoCambioDO.compareTo(BigDecimal.ZERO) == 0){
						mapMsg.put("TipoCambioDO", req.TipoCambioDO.toString());
						out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"TIPO_CAMBIO_DO_CERO_MANTENEDOR_PYTC",mapMsg,conn,in.p_IdEmpresa));
						Id++;
					}
					if(opFw.IdMonedaPrincipal.equals("UF") && req.TipoCambioUF.compareTo(BigDecimal.ZERO) == 0){
						mapMsg.put("TipoCambioUF", req.TipoCambioUF.toString());
						out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"TIPO_CAMBIO_UF_CERO_MANTENEDOR_PYTC",mapMsg,conn,in.p_IdEmpresa));
						Id++;
					}
					if(opFw.IdMonedaPrincipal.equals("EUR") && req.TipoCambioEUR.compareTo(BigDecimal.ZERO) == 0){
						mapMsg.put("TipoCambioEUR", req.TipoCambioEUR.toString());
						out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"TIPO_CAMBIO_EUR_CERO_MANTENEDOR_PYTC",mapMsg,conn,in.p_IdEmpresa));
						Id++;
					}
				}
				
				//Retorna si existen errores 
				if (MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES) > 0){
					return out;
				}
				
				for(OperacionForwardObjIn opeFwdIn: in.p_tablaOperVencidas){
					if(opFw.IdCartera.longValue() == opeFwdIn.IdCartera.longValue()){
						BigDecimal factorCompraVenta = opFw.IdTipoOperacion.equals("COMFWD") ? BigDecimal.valueOf(1) : BigDecimal.valueOf(-1);
						BigDecimal montoOperacion = opFw.EGRIdTipoOperacion.equals("ANTFWD") ? opFw.EGRMontoMonedaPrincipal : opFw.INGMontoMonedaPrincipal;
						BigDecimal montoFinal = opFw.EGRIdTipoOperacion.equals("ANTFWD") ? opFw.EGRMontoMonedaSecundaria : opFw.INGMontoMonedaSecundaria;
						
						BigDecimal MontoARecibirAnticipo = BigDecimal.ZERO;
						if(opFw.Nemotecnico.equals("SECANO") || opFw.Nemotecnico.equals("SEINFL")){
							MontoARecibirAnticipo = montoOperacion.multiply(opeFwdIn.Precio.subtract(opFw.ValorTipoCambioCierre).multiply(factorCompraVenta));
						}
						else if(opFw.Nemotecnico.equals("SECARE")){
							if (opFw.IdMonedaPrincipal.equals("EUR")) {
								MontoARecibirAnticipo = montoOperacion.multiply(opeFwdIn.Precio.subtract(opFw.ValorTipoCambioCierre).multiply(factorCompraVenta)).multiply(opFw.TipoCambioMercadoUF);
							} else if (opFw.IdMonedaPrincipal.equals("USD")) {
								if(opFw.IdMonedaSecundaria.equalsIgnoreCase("UF")) {
									MontoARecibirAnticipo = montoOperacion.multiply(opeFwdIn.Precio.subtract(opFw.ValorTipoCambioCierre).multiply(factorCompraVenta)).multiply(opFw.TipoCambioMercadoUF);
								}else {
									MontoARecibirAnticipo = montoOperacion.multiply(opeFwdIn.Precio.subtract(opFw.ValorTipoCambioCierre).multiply(factorCompraVenta)).multiply(opFw.TipoCambioMercadoDO);
								}
							}
						}
						else if(opFw.Nemotecnico.equals("ARBITR")){
							if(opFw.MultiplicaODivide.equals("D")){
								MontoARecibirAnticipo = ((montoOperacion.divide(opeFwdIn.Precio,8,RoundingMode.HALF_UP)).subtract(montoOperacion.divide(opFw.ValorParidadCierre,8,RoundingMode.HALF_UP)).multiply(factorCompraVenta)).multiply(opFw.TipoCambioMercadoDO);
							}
							else{
								MontoARecibirAnticipo = (opeFwdIn.Precio.subtract(opFw.ValorParidadCierre).multiply(factorCompraVenta)).multiply(montoOperacion).multiply(opFw.TipoCambioMercadoDO);
							}
						}
						//Se obtienen operaciones VIG para enviar a Custodia
						if(opFw.IdEstado.equals("VIG")){
							TablaForwardObjIn tfi = new TablaForwardObjIn();
							tfi.FolioInterno = opeFwdIn.FolioCartera;
							tfi.IdOperacion = opeFwdIn.IdOperacion;
							tfi.FolioCartera = opeFwdIn.FolioCartera;
							tfi.IdTipoOperacionOriginal = opFw.IdTipoOperacion;
							tfi.IdTipoOperacionNuevo = "VCDFWD";
							tfi.IdTipoInstrumento = opFw.IdTipoInstrumento;
							tfi.IdClasificacionTipoInstrumento = opFw.IdClasificacionTipoInstrumento;
							tfi.IdTipoProducto = opFw.IdTipoProducto;
							tfi.IdInstrumento = opFw.IdInstrumento;
							tfi.Nemotecnico = opFw.Nemotecnico;
							tfi.IdClienteOriginal = opFw.IdCliente;
							tfi.IdClienteNuevo = opFw.IdCliente;
							tfi.IdPortafolioOriginal = opFw.IdPortafolio;
							tfi.IdPortafolioNuevo = opFw.IdPortafolio;
							tfi.IdPersonaEjecutivoOriginal = opFw.IdAgente;
							tfi.IdPersonaEjecutivoNuevo = opFw.IdAgente;
							tfi.IdTipoCanalIngreso = opFw.IdTipoCanalIngreso;
							tfi.IdMonedaPrincipal = opFw.IdMonedaPrincipal;	
							tfi.IdMonedaSecundaria = opFw.IdMonedaSecundaria;
							tfi.PrecioVencimiento = opeFwdIn.Precio;
							tfi.MontoOperacionOriginal = montoOperacion;
							tfi.MontoOperacionNuevo = montoOperacion;
							tfi.MontoInicialOriginal = montoOperacion;
							tfi.MontoInicialNuevo = montoOperacion;
							tfi.MontoFinalOriginal = montoFinal;
							tfi.MontoFinalNuevo = montoFinal;
							tfi.FechaOperacion = opFw.FechaOperacion;
							tfi.TCCierreOriginal = opFw.ValorTipoCambioCierre;
							tfi.TCCierreNuevo = opFw.ValorTipoCambioCierre;
							tfi.ParidadCierreOriginal = opFw.ValorParidadCierre;
							tfi.ParidadCierreNuevo = opFw.ValorParidadCierre;
							tfi.PlazoOriginal = opFw.Plazo;
							tfi.PlazoNuevo = opFw.Plazo;
							tfi.FechaVencimientoOriginal = opFw.FechaVencimientoOperacion;
							tfi.FechaVencimientoNuevo = opFw.FechaVencimientoOperacion;
							tfi.FechaPagoOriginal = opFw.FechaLiquidacion;
							tfi.FechaPagoNuevo = opFw.FechaLiquidacion;
							tfi.IdModalidadLiquidacionOriginal = opFw.IdModalidadCumplimiento;
							tfi.IdModalidadLiquidacionNuevo = opFw.IdModalidadCumplimiento;
							tfi.IdReferenciaMercado = 0L;
							tfi.IdPlaza = opFw.IdPlaza;
							tfi.TCCostoOriginal = opFw.ValorTipoCambioCosto;
							tfi.TCCostoNuevo = opFw.ValorTipoCambioCosto;
							tfi.PuntosTasasForwardOriginal = opFw.INGEsPuntos;
							tfi.PuntosTasasForwardNuevo = opFw.INGEsPuntos;
							tfi.PuntosForwardOriginal = opFw.INGTasaPuntosMonedaPrincipal;
							tfi.PuntosForwardNuevo = opFw.INGTasaPuntosMonedaPrincipal;
							tfi.TCForwardOriginal = opFw.TasaCompra;
							tfi.TCForwardNuevo = opFw.TasaCompra;
							tfi.TasasMonedaPrincipalOriginal = BigDecimal.ZERO;
							tfi.TasasMonedaPrincipalNuevo = BigDecimal.ZERO;
							tfi.TasasMonedaSecundariaOriginal = BigDecimal.ZERO;
							tfi.TasasMonedaSecundariaNuevo = BigDecimal.ZERO;
							tfi.MontoDiferidoOriginal = BigDecimal.ZERO;
							tfi.MontoDiferidoNuevo = BigDecimal.ZERO;
							tfi.MontoMargenOriginal = BigDecimal.ZERO;
							tfi.MontoMargenNuevo = BigDecimal.ZERO;
							tfi.IdFixingOriginal = opFw.IdFixing;
							tfi.IdFixingNuevo = opFw.IdFixing;
							tfi.FechaFixingOriginal = opFw.FechaFixing;
							tfi.FechaFixingNuevo = opFw.FechaFixing;
							tfi.HoraFixingOriginal = Fecha.toStringTime(opFw.HoraFixing);
							tfi.HoraFixingNuevo = tfi.HoraFixingOriginal;
							tfi.IdMesa = opFw.IdMesa;
							tfi.IdSpotAsociado = opFw.EGRIdSpotAsociado;
							tfi.Observaciones = opFw.Glosa;
							tfi.IdEstado = opFw.IdEstado;
							tfi.TasaDescuento = BigDecimal.ZERO;
							tfi.TipoCambioAnticipo = BigDecimal.ZERO;
							tfi.MontoARecibirAnticipo = MontoARecibirAnticipo.setScale(0, RoundingMode.HALF_UP);
							tfi.TipoCambioObservado = BigDecimal.ZERO;
							operacionesVencimientoVIG.add(tfi);
						}
						//Se obtienen operaciones VCD para actualizar campos de Reproceso
						else if(opFw.IdEstado.equals("VCD")){
							OperacionForwardVCD opeFwdVcd = new OperacionForwardVCD();
							opeFwdVcd.IdCartera = opeFwdIn.IdCartera;
							opeFwdVcd.FolioCartera = opeFwdIn.FolioCartera;
							opeFwdVcd.IdOperacion = opeFwdIn.IdOperacion;
							opeFwdVcd.FechaModificacionRegistro = Fecha.toTimeStamp(fechaHoraSistemaSecondate);
							opeFwdVcd.Precio = opeFwdIn.Precio;
							opeFwdVcd.MontoLiquidacion = MontoARecibirAnticipo.setScale(0, RoundingMode.HALF_UP);
							operacionesVencimientoVCD.add(opeFwdVcd);
						}
					}
				}
			}
			/***** VENCIMIENTO DE FORWARD EN CARTERA *****/
			if(operacionesVencimientoVIG.size() > 0){
				log.write("MESA FORWARD :: Llamada Ingresar Forward Cartera");
				IngresarForwardCartera ingresarForwardCartera = new IngresarForwardCartera();
				IngresarForwardCarteraEntrada ingresarForwardCarteraIn = new IngresarForwardCarteraEntrada();
				ingresarForwardCarteraIn.p_IdEmpresa = in.p_IdEmpresa;
				ingresarForwardCarteraIn.p_IdPais = in.p_IdPais;
				ingresarForwardCarteraIn.p_IdUsuario = in.p_IdUsuario;
				ingresarForwardCarteraIn.p_IdBolsa = in.p_IdBolsa;
				ingresarForwardCarteraIn.p_IdSucursal = in.p_IdSucursal;
				ingresarForwardCarteraIn.p_IdModulo = in.p_IdModulo;
				ingresarForwardCarteraIn.p_UsaReglasNegocio = "N";//En Vencimiento FWD no es necesario validar Reglas
				ingresarForwardCarteraIn.p_tablaForward = operacionesVencimientoVIG;
				IngresarForwardCarteraSalida ingresarForwardCarteraOut = ingresarForwardCartera.logica(conn, ingresarForwardCarteraIn);
				
				out.P_OUT_ERRORESCONTADOR = out.P_OUT_ERRORESCONTADOR + ingresarForwardCarteraOut.P_OUT_ERRORESCONTADOR;
				out.P_OUT_MENSAJES.addAll(ingresarForwardCarteraOut.P_OUT_MENSAJES);
				if (MensajeSalida.getNumeroErrores(ingresarForwardCarteraOut.P_OUT_MENSAJES) > 0){
					return out;
				}
				
				//Salida de Servicio Custodia
				out.P_OUT_ERRORESCONTADOR = out.P_OUT_ERRORESCONTADOR + MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
				out.P_OUT_TBLIDFORWARD = new ArrayList<OperacionForwardObjOut>();
				for(TablaForwardObjIn opVcd: operacionesVencimientoVIG){
					for(TablaForwardObjOut fwdCarOut: ingresarForwardCarteraOut.P_OUT_TBLIDFORWARDRETIROS){
						if(opVcd.FolioCartera.longValue() == fwdCarOut.FolioInterno.longValue()){
							OperacionForwardObjOut opOut = new OperacionForwardObjOut();
							opOut.FolioInterno = fwdCarOut.FolioInterno;
							opOut.IdCartera = fwdCarOut.IdCartera;
							opOut.IdOperacion = opVcd.IdOperacion;
							opOut.FolioCartera = fwdCarOut.FolioCartera;
							opOut.IdEstado = opVcd.IdEstado;
							out.P_OUT_TBLIDFORWARD.add(opOut);
						}
					}
				}
			}
			
			
			
			/***** UPDATE CAMPOS DE REPROCESO (operaciones de entrada con Estado VCD) *****/
			if(operacionesVencimientoVCD.size() > 0){
				_sql.batch(conn, SQL_UPDATE_CARTERA_REPROCESO, operacionesVencimientoVCD, OperacionForwardVCD.class);
				
				for(OperacionForwardVCD opVcd: operacionesVencimientoVCD){
					OperacionForwardObjOut opOut = new OperacionForwardObjOut();
					opOut.FolioInterno = opVcd.FolioCartera;
					opOut.IdCartera = opVcd.IdCartera;
					opOut.IdOperacion = opVcd.IdOperacion;
					opOut.FolioCartera = opVcd.FolioCartera;
					opOut.IdEstado = "VCD";
					out.P_OUT_TBLIDFORWARD.add(opOut);
				}
			}
			
			
			/***** SALIDA *****/
			//Agrega a la salida obtenida de Custodia, las operaciones de entrada con Estado ANT
			for(OperacionForwardObjIn opeFwdIn: in.p_tablaOperVencidas){
				if(opeFwdIn.IdEstado.equals("ANT")){
					OperacionForwardObjOut opOut = new OperacionForwardObjOut();
					opOut.FolioInterno = opeFwdIn.FolioCartera;
					opOut.IdCartera = opeFwdIn.IdCartera;
					opOut.IdOperacion = opeFwdIn.IdOperacion;
					opOut.FolioCartera = opeFwdIn.FolioCartera;
					opOut.IdEstado = "VCD";
					out.P_OUT_TBLIDFORWARD.add(opOut);
					operacionesAnticipadas.add(opOut);
				}
			}
			
			
			/***** CAMBIAR ESTADO *****/
			if(out.P_OUT_TBLIDFORWARD.size() > 0){
				CambiarEstadoOperacion cambiarEstadoOperacion = new CambiarEstadoOperacion();
				CambiarEstadoOperacionEntrada cambiarEstadoOperacionIn = new CambiarEstadoOperacionEntrada(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo, "N", in.p_UsaReglasNegocio, in.p_IdPais);
				CambiarEstadoOperacionSalida cambiarEstadoOperacionOut = new CambiarEstadoOperacionSalida();
				for(OperacionForwardObjOut opFw: out.P_OUT_TBLIDFORWARD){
					ObjCambiarEstadoOperacionIN ceoIn = new ObjCambiarEstadoOperacionIN();
					ceoIn.IdOperacion = opFw.IdOperacion;
					ceoIn.IdTipoProducto = "MEFW";
					ceoIn.IdNuevoEstado = "ENTREGA_FISICA".equals(((DatosCarteraVencimientoForward) operacionesForwardHash.get(opFw.IdCartera)).ModalidadCumplimientoValor) ? "LIQ" : "VCD";
					ceoIn.IdReserva = 0L;
					ceoIn.FolioCarteraCustodia = opFw.FolioCartera;
					ceoIn.MontoOperacionAnticipo = BigDecimal.ZERO;
					ceoIn.MontoOperacionInicialAnticipo = BigDecimal.ZERO;
					ceoIn.MontoOperacionFinalAnticipo = BigDecimal.ZERO;
					ceoIn.TasaAnticipo = BigDecimal.ZERO;
					ceoIn.MontoOperacionMonedaAnticipo = BigDecimal.ZERO;
					ceoIn.MontoOperacionInicialMonedaAnticipo = BigDecimal.ZERO;
					ceoIn.MontoOperacionFinalMonedaAnticipo = BigDecimal.ZERO;
					ceoIn.IdMonedaLiquidacionAnticipo = "";
					ceoIn.IdCondicionLiquidacionAnticipo = "";
					ceoIn.FechaCondicionLiquidacionAnticipo = Fecha.toDate("0001-01-01");
					cambiarEstadoOperacionIn.p_DataCambiarEstado.add(ceoIn);
				}
				log.write("MESA FORWARD :: Llamada Cambiar Estado Operacion");
				cambiarEstadoOperacionOut = cambiarEstadoOperacion.logica(conn, cambiarEstadoOperacionIn);
				out.P_OUT_MENSAJES.addAll(cambiarEstadoOperacionOut.P_OUT_MENSAJES);
				if (MensajeSalida.getNumeroErrores(cambiarEstadoOperacionOut.P_OUT_MENSAJES) > 0){
					return out;
				}
				
				
				/***** UPDATE CAMPO IdEstado EN CARTERA PARA LAS OPERACIONES DE ENTRADA ANTICIPADAS *****/
				//Se acuerda con Jorge Ossandon que operaciones Anticipadas no cambian su Estado a pesar de que son Procesadas
				//_sql.batch(conn, SQL_UPDATE_CARTERA_ANTICIPADA_ID_ESTADO, operacionesAnticipadas, OperacionForwardObjOut.class);
			}
			
			
			/***** FORWARD CON ENTREGA FISICA GENERA OPER. SPOT *****/
			List<SpotOrigenForwardObjIn> operacionForwardEntregaFisica = _sql.query(conn, SQL_SELECT_OPERACIONES_FORWARD_ENTREGA_FISICA, params, SpotOrigenForwardObjIn.class);
			HashMap<Long,Object> hashIdCartera = Utils.List2Hash(in.p_tablaOperVencidas, "IdCartera");
			List<OperacionForwardObjIn> operacionesVencidas = new ArrayList();
			for(SpotOrigenForwardObjIn entregafisica  : operacionForwardEntregaFisica) {
				OperacionForwardObjIn ope = (OperacionForwardObjIn) hashIdCartera.get(entregafisica.IdCartera);
				if(ope != null) {
					operacionesVencidas.add(ope);
				}
			}
			if(operacionForwardEntregaFisica.size() > 0){
				if (fwdIntegradoConSpot.equals("S")) { // con parametro encendido sigue flujo normal
					CrearOperacionSpotOrigenForward crearSpotOrigenFwd = new CrearOperacionSpotOrigenForward();
					CrearOperacionSpotOrigenForwardEntrada crearSpotOrigenFwdEntrada = new CrearOperacionSpotOrigenForwardEntrada();
					crearSpotOrigenFwdEntrada.p_IdEmpresa = in.p_IdEmpresa;
					crearSpotOrigenFwdEntrada.p_IdUsuario = in.p_IdUsuario;
					crearSpotOrigenFwdEntrada.p_IdModulo = in.p_IdModulo;
					crearSpotOrigenFwdEntrada.p_datosForward = operacionForwardEntregaFisica;
					CrearOperacionSpotOrigenForwardSalida crearSpotOrigenFwdSalida = crearSpotOrigenFwd.logica(conn, crearSpotOrigenFwdEntrada);
					out.P_OUT_MENSAJES.addAll(crearSpotOrigenFwdSalida.P_OUT_MENSAJES);
					if (MensajeSalida.getNumeroErrores(crearSpotOrigenFwdSalida.P_OUT_MENSAJES) > 0){
						return out;
					}
					
					/***** UPDATE CAMPO FolioOrigenFW->FolioCartera PARA LOS SPOT ORIGEN FORWARD *****/
					_sql.batch(conn, SQL_UPDATE_OPERACIONES_SPOT_FOLIO_ORIGEN_FW, crearSpotOrigenFwdSalida.P_OUT_SPOTORIGENFORWARD, SpotOrigenForwardObjOut.class);
					
					/***** UPDATE CAMPO EGRIdSpotAsociado EN CARTERA PARA LOS VENCIMIENTOS MODALIDAD ENTREGA FISICA (GENERAN SPOT) *****/
					_sql.batch(conn, SQL_UPDATE_CARTERA_VENCIMIENTO_ENTREGA_FISICA, crearSpotOrigenFwdSalida.P_OUT_SPOTORIGENFORWARD, SpotOrigenForwardObjOut.class);
																			
					if(!operacionesVencidas.isEmpty()) {
						_sql.batch(conn, SQL_UPDATE_CARTERA_VENCIMIENTO_ENTREGA_FISICA_MONEDALIQUIDACION, operacionesVencidas, OperacionForwardObjIn.class);
					}
				} else {
					String fechaSistema 				= ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
					HashMap<String,Object> parametros 	= new HashMap<>();
					parametros.put("p_IdCartera", operacionForwardEntregaFisica.stream().map(forward -> forward.IdCartera).collect(Collectors.toList()));
					parametros.put("p_IdEmpresa", in.p_IdEmpresa);
					List<ForwardSinSpot> operacionesForwardEntregaFisicaSinSpot = _sql.query(conn, SQL_SELECT_OPERACIONES_FORWARD_ENTREGA_FISICA_SIN_SPOT, parametros, ForwardSinSpot.class);
					parametros.clear();
					parametros.put("p_IdOperacion", operacionForwardEntregaFisica.stream().map(forward -> forward.IdCartera).collect(Collectors.toList()));
					List<ForwardFomaPago> operacionesForwardEntregaFisicaFormaDePago = _sql.query(conn, SQL_SELECT_OPERACIONES_FORWARD_ENTREGA_FISICA_FORMA_PAGO, parametros, ForwardFomaPago.class);
					parametros.clear();
					CargoAbonoResultado cargoAbonoResultado = generaCargoAbono(conn, operacionesForwardEntregaFisicaSinSpot, operacionesForwardEntregaFisicaFormaDePago, in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo, fechaSistema);
					out.P_OUT_MENSAJES.addAll(cargoAbonoResultado.P_OUT_MENSAJES);
					out.P_OUT_ERRORESCONTADOR =+ cargoAbonoResultado.P_OUT_ERRORESCONTADOR;
					TesoreriaResultado resultadoIngresoTesoreria = generarMovimientoTesoreria(conn, operacionesForwardEntregaFisicaSinSpot, operacionesForwardEntregaFisicaFormaDePago, in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo, fechaSistema);
					out.P_OUT_MENSAJES.addAll(resultadoIngresoTesoreria.P_OUT_MENSAJES);
					out.P_OUT_ERRORESCONTADOR =+ resultadoIngresoTesoreria.P_OUT_ERRORESCONTADOR;
					if (out.P_OUT_ERRORESCONTADOR == 0) {
						List<ForwardCartera> forwardCarteraActualiza = operacionForwardEntregaFisica.stream().map(forward -> new ForwardCartera(forward.IdCartera)).collect(Collectors.toList());
						_sql.batch(conn, SQL_UPDATE_CARTERA_VENCIMIENTO_ENTREGA_FISICA, forwardCarteraActualiza, ForwardCartera.class);
						if(!operacionesVencidas.isEmpty()) {
							_sql.batch(conn, SQL_UPDATE_CARTERA_VENCIMIENTO_ENTREGA_FISICA_MONEDALIQUIDACION, operacionesVencidas, OperacionForwardObjIn.class);
						}
						System.out.println("ACTUALIZACION ESTADO CARTERA FWD");
					}
				}
			}
			
		}catch(Exception e){
			out.P_OUT_ERRORESCONTADOR = 1L;
			log.write(e);
			throw e;
		}
		
		return out;
	}
	
	private TesoreriaResultado generarMovimientoTesoreria(Connection conn, List<ForwardSinSpot> operacionesForward, List<ForwardFomaPago> operacionesForwardFormaDePago, Long idEmpresa, Long idUsuario, String idModulo, String fechaSistema) throws Exception {
		log.write("INICIO::VencimientoOperacionForward.generarMovimientoTesoreria");
		long inicio = System.currentTimeMillis();
		TesoreriaResultado salida = new TesoreriaResultado();
		if (operacionesForward.size() == 0) {
			return salida;
		}
		CrearIngresoEgreso_Entrada entradaCrearIngresoEgreso = new CrearIngresoEgreso_Entrada();
		Long indexFormaDePago = 0L;
		for (ForwardSinSpot forward: operacionesForward) {
			List<ForwardFomaPago> operacionesForwardEntregaFisicaFormaDePagoFiltradas = operacionesForwardFormaDePago.stream()
					.filter(fwd -> fwd.IdOperacion.equals(forward.IdCartera)).collect(Collectors.toList());
			for (ForwardFomaPago formaDePago : operacionesForwardEntregaFisicaFormaDePagoFiltradas) {
				if (!formaDePago.AfectarTesoreria.equalsIgnoreCase("S")) {
					continue;
				}
				IngresoEgreso_Entrada ing = new IngresoEgreso_Entrada();
				ing.NumeroReferencia = formaDePago.IdComprobantePagoOperacion;
				if (formaDePago.RecibePaga.equalsIgnoreCase("R")) {
					ing.IdTipoMovimiento = "MOVING";
				} else if (formaDePago.RecibePaga.equalsIgnoreCase("P")) {
					ing.IdTipoMovimiento = "MOVEGR";
				} else {
					ing.IdTipoMovimiento = "";
				}
				ing.IdTipoPersona = "CLI";
				ing.IdPersona = forward.IdPersona;
				ing.IdPortafolio = forward.IdPortafolio;
				ing.IdMoneda = formaDePago.IdMoneda;
				ing.Monto = MonedaFormato.formatear(conn, formaDePago.Monto, "MONTO", formaDePago.IdMoneda,"SPT","").valor;
				ing.FechaLiquidacion = fechaSistema;
				ing.IdConcepto = "VCDFWD";
				ing.IdTipoPersonaTitularPago = "CLI";
				ing.IdPersonaTitularPago = forward.IdPersona;
				ing.IdPortafolioTitularPago = forward.IdPortafolio;
				ing.IdCaja = 0L;
				ing.SecuenciaAgrupacion = "";
				ing.SecuenciaRelacionada = forward.IdOperacion;
				ing.Glosa = "VENCIMIENTO FORWARD";
				ing.IdEmpleadoDigitador = forward.IdEmpleadoEjecutivo;
				ing.IdSucursalEmpleadoDigitador = forward.IdSucursalEmpleadoEjecutivo;
				ing.IdTipoProducto = forward.IdTipoProducto;
				ing.IdTipoOperacion = forward.IdTipoOperacion;
				ing.IdTipoComprobante = "";
				ing.IdComprobante = 0L;
				ing.FechaComprobante = fechaSistema;
				ing.FechaLiquidacionComprobante = fechaSistema;
				ing.IdOperacion = forward.IdOperacion;
				ing.FechaOperacion = "0001-01-01";
				ing.IdCondicionLiquidacion = "";
				ing.IdBolsa = "";
				ing.IdMonedaOperacion = "";
				ing.MontoOperacion = ing.Monto;
				ing.IdTipoReajuste = "";
				ing.FechaParidad = "0001-01-01";
				ing.ValorParidad = BigDecimal.ZERO;
				ing.FolioCartera = 0L;
				ing.IdInstrumento = (forward.IdInstrumento == null ? 0 : forward.IdInstrumento);
				ing.IdTipoInstrumento = (forward.IdTipoInstrumento == null ? "" : forward.IdTipoInstrumento);
				ing.IdClasificacionTipoInstrumento = (forward.IdClasificacionTipoInstrumento == null ? "" : forward.IdClasificacionTipoInstrumento);
				ing.Bilateral = "N";
				ing.GeneraComprobante = "N";
				ing.AgruparEnComprobante = "S";
				ing.MueveCuentaInversion = "S";
				ing.TitularPago = "N";
				ing.IdTipoCanalIngreso = "MES";
				entradaCrearIngresoEgreso.p_IngresoEgresoGenerar.add(ing);
				
				FormaDePago_Entrada formaPago = new FormaDePago_Entrada();
				formaPago.NumeroReferencia = ing.NumeroReferencia;
				formaPago.NumeroReferenciaFormaDePago = indexFormaDePago++;
				formaPago.IdMedioDePago = formaDePago.IdMedioDePago;
				formaPago.IdModalidadDePago = formaDePago.IdModalidadPago;
				formaPago.IdMoneda = formaDePago.IdMoneda;
				formaPago.Monto = MonedaFormato.formatear(conn, formaDePago.Monto, "MONTO", formaDePago.IdMoneda,"SPT","").valor;
				formaPago.FechaDocumentoPagoCorredora = "0001-01-01";
				formaPago.IdCuentaBancariaCorredora = 0L;
				formaPago.IdTarjetaBancariaCorredora = 0L;
				formaPago.IdChequera = 0L;
				formaPago.NumeroDocumentoCorredora = 0L;
				formaPago.FechaDocumentoPagoCorredora = "0001-01-01";
				formaPago.IdCuentaBancariaSocio = formaDePago.IdCuentaBancaria == null ? 0L : formaDePago.IdCuentaBancaria;
				formaPago.IdBancoSocio = formaDePago.IdBanco;
				formaPago.NombreCuentaBancariaSocio = "";
				formaPago.NumeroCuentaBancariaSocio = formaDePago.NumeroCuentaBancaria;
				formaPago.IdTipoCuentaBancariaSocio = formaDePago.IdTipoCuentaBancaria;
				formaPago.NombreTipoCuentaBancariaSocio = formaDePago.NombreTipoCuentaBancaria;
				formaPago.IdTarjetaBancariaSocio = formaDePago.IdTarjetaBancaria == null ? 0L : formaDePago.IdTarjetaBancaria; // this
				formaPago.IdTipoTarjetaBancariaSocio = formaDePago.IdTipoTarjetaBancaria == null ? "" : formaDePago.IdTipoTarjetaBancaria;
				formaPago.NombreTipoTarjetaBancariaSocio = formaDePago.NombreTipoTarjetaBancaria == null ? "" : formaDePago.NombreTipoTarjetaBancaria;
				formaPago.NumeroTarjetaBancariaSocio = formaDePago.NumeroTarjetaBancaria == null ? "" : formaDePago.NumeroTarjetaBancaria;
				formaPago.NumeroSerieSocio = "";
				formaPago.NumeroDocumentoSocio = 0L;
				formaPago.FechaDocumentoPagoSocio = Fecha.toStringDate(formaDePago.FechaLiquidacionComprobantePagoOperacion);
				entradaCrearIngresoEgreso.p_FormaDePago.add(formaPago);
			}
		}
		
		if (entradaCrearIngresoEgreso.p_IngresoEgresoGenerar.size() > 0 && entradaCrearIngresoEgreso.p_FormaDePago.size() > 0) {
			CrearIngresoEgreso serviceCrearIngresoEgreso = new CrearIngresoEgreso();
			entradaCrearIngresoEgreso.p_EsIngresoAutomatico = "S";
			entradaCrearIngresoEgreso.p_IdEmpresa = idEmpresa;
			entradaCrearIngresoEgreso.p_IdModulo = idModulo;
			entradaCrearIngresoEgreso.p_IdUsuario = idUsuario;
			entradaCrearIngresoEgreso.p_SimularOperacion = "N";
			entradaCrearIngresoEgreso.p_FechaMovimiento = ((IngresoEgreso_Entrada) entradaCrearIngresoEgreso.p_IngresoEgresoGenerar.get(0)).FechaComprobante;
			entradaCrearIngresoEgreso.p_UsaReglasNegocio = "S";
			CrearIngresoEgreso_Salida salidaCrearIngresoEgreso = serviceCrearIngresoEgreso.logica(conn, entradaCrearIngresoEgreso);
			salida.P_OUT_MENSAJES.addAll(salidaCrearIngresoEgreso.P_OUT_MENSAJES);
			salida.P_OUT_ERRORESCONTADOR =+ salidaCrearIngresoEgreso.P_OUT_ERRORESCONTADOR;
		}
		long fin = System.currentTimeMillis();
		log.write("FIN::VencimientoOperacionForward.generarMovimientoTesoreria", fin-inicio);
		return salida;
	}
	
	private CargoAbonoResultado generaCargoAbono(Connection conn, List<ForwardSinSpot> operacionesForward, List<ForwardFomaPago> operacionesForwardFormaDePago, Long idEmpresa, Long idUsuario, String idModulo, String fechaSistema) throws Exception {
		log.write("INICIO::VencimientoOperacionForward.generaCargoAbono");
		long inicio = System.currentTimeMillis();
		CargoAbonoResultado salida = new CargoAbonoResultado();
		if (operacionesForward.size() == 0L) {
			return salida;
		}
		String localizacionSistema 	= ConfiguracionSistema.getParametroConfiguracionString(conn, idEmpresa, "PLATAFORMA", "PLA.LocalizacionSistema");
		List<ObjCargoAbonoAGenerar> CargoAbonoColeccion = new ArrayList<>();
		
		for (ForwardSinSpot forward : operacionesForward) {
			List<ForwardFomaPago> operacionesForwardEntregaFisicaFormaDePagoFiltradas = operacionesForwardFormaDePago.stream()
					.filter(fwd -> fwd.IdOperacion.equals(forward.IdCartera)).collect(Collectors.toList());
			for (ForwardFomaPago formaDePago : operacionesForwardEntregaFisicaFormaDePagoFiltradas) {
				ObjCargoAbonoAGenerar cargoAbono = new ObjCargoAbonoAGenerar();
				cargoAbono.NumeroCargoAbono = formaDePago.IdComprobantePagoOperacion;
				cargoAbono.IdPortafolio = forward.IdPortafolio;
				cargoAbono.IdMoneda = formaDePago.IdMoneda;
				if (formaDePago.RecibePaga.equalsIgnoreCase("R")) {
					cargoAbono.IdTipoMovimiento = "MOVCAR";
				} else if (formaDePago.RecibePaga.equalsIgnoreCase("P")) {
					cargoAbono.IdTipoMovimiento = "MOVABN";
				} else {
					cargoAbono.IdTipoMovimiento = "";
				}
				cargoAbono.Monto = MonedaFormato.formatear(conn, formaDePago.Monto, "MONTO", formaDePago.IdMoneda,"SPT","").valor;
				cargoAbono.IdConcepto = "VCDFWD";
				cargoAbono.Descripcion = "VENCIMIENTO FORWARD";
				cargoAbono.IdCondicionDeLiquidacion = forward.IdCondicionLiquidacion;
				cargoAbono.FechaCondicionDeLiquidacion = fechaSistema;
				cargoAbono.IdEjecutivo = forward.IdEmpleadoEjecutivo;
				cargoAbono.IdSucursal = forward.IdSucursalEmpleadoEjecutivo;
				cargoAbono.IdTipoProducto = forward.IdTipoProducto;
				cargoAbono.IdTipoOperacion = forward.IdTipoOperacion;
				cargoAbono.IdOperacion = formaDePago.IdOperacion;
				if (localizacionSistema.equalsIgnoreCase("PE")) {
					cargoAbono.FechaOperacion = fechaSistema;				
				}else{
					cargoAbono.FechaOperacion = "0001-01-01";	
				}
				cargoAbono.FechaLiquidacionOperacion = "0001-01-01";
				cargoAbono.FechaVencimientoOperacion = Fecha.toStringDate(forward.FechaVencimientoOperacion);
				cargoAbono.IdMonedaOperacion = forward.IdMonedaSecundaria;
				cargoAbono.MontoOperacion = forward.INGMontoMonedaSecundaria;
				cargoAbono.TipoCambioOperacion = BigDecimal.ZERO;
				cargoAbono.IdContraparte = 0L;
				cargoAbono.IdTipoComprobante = "";
				cargoAbono.IdComprobante = 0L;
				cargoAbono.FolioComprobante = "";
				cargoAbono.IdComprobanteDetalle = 0L;
				cargoAbono.IdComprobanteFormaDePago = 0L;
				cargoAbono.IdTipoMovimientoAsociado = cargoAbono.IdTipoComprobante;
				cargoAbono.IdMovimientoAsociado = formaDePago.IdOperacion;
				cargoAbono.FechaComprobante = fechaSistema;
				if (cargoAbono.IdTipoMovimientoAsociado == null) {
					cargoAbono.IdTipoMovimientoAsociado = "";
				}
				if (cargoAbono.IdMovimientoAsociado == null ) {
					cargoAbono.IdMovimientoAsociado = 0L;
				}
				cargoAbono.IdPersonaPagoCobro = 0L;
				cargoAbono.IdInstrumento = forward.IdInstrumento;
				cargoAbono.IdTipoInstrumento = forward.IdTipoInstrumento;
				cargoAbono.Nemo = forward.InstrumentoNemotecnico == null ? "" : forward.InstrumentoNemotecnico;
				cargoAbono.EsIndicadorBilateral = "N";
				cargoAbono.EsCargoAbonoPorTraspaso = "N";
				cargoAbono.IdPortafolioTraspaso = 0L;
				//no fue a tesoreria
				if (cargoAbono.IdTipoMovimientoAsociado.equalsIgnoreCase("")) {
					cargoAbono.GeneroContraparteEnTesoreria = "N";
				} else {
					cargoAbono.GeneroContraparteEnTesoreria = "S";
				}
				cargoAbono.CertificadoAutomatico = "S";
				cargoAbono.Cantidad = forward.EGRCantidad;
				CargoAbonoColeccion.add(cargoAbono);
			}
		}
		
		if (CargoAbonoColeccion.size() > 0) {
			CrearCargoAbono serviceCrearCargoAbono 			= new CrearCargoAbono();
			CrearCargoAbonoEntrada entradaCrearCargoAbono 	= new CrearCargoAbonoEntrada();
			entradaCrearCargoAbono.p_CargoAbonoAGenerar = CargoAbonoColeccion;
			entradaCrearCargoAbono.p_EsIngresoManual 	= "N";		
			entradaCrearCargoAbono.p_IdEmpresa 			= idEmpresa;
			entradaCrearCargoAbono.p_IdModulo 			= idModulo;
			entradaCrearCargoAbono.p_IdUsuario 			= idUsuario;
			entradaCrearCargoAbono.p_simularOperacion 	= "N";
			entradaCrearCargoAbono.p_UsaReglasNegocio 	= "S";
			if (localizacionSistema.equalsIgnoreCase("PE")) {
				entradaCrearCargoAbono.p_FechaSistema =  ((ObjCargoAbonoAGenerar) CargoAbonoColeccion.get(0)).FechaOperacion;
			} else {
				entradaCrearCargoAbono.p_FechaSistema = fechaSistema;	
			}	
			CrearCargoAbonoSalida salidaCrearCargoAbono = serviceCrearCargoAbono.logica(conn, entradaCrearCargoAbono);
			salida.P_OUT_MENSAJES.addAll(salidaCrearCargoAbono.P_OUT_MENSAJES);
			salida.P_OUT_ERRORESCONTADOR =+ salidaCrearCargoAbono.P_OUT_ERRORESCONTADOR;
		}
		long fin = System.currentTimeMillis();
		log.write("FIN::VencimientoOperacionForward.generaCargoAbono", fin-inicio);
		return salida;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa02","connections.properties")){
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String jsonDesa = "{\"p_IdEmpresa\":1,\"p_IdPais\":44,\"p_IdUsuario\":97,\"p_IdBolsa\":0,\"p_IdSucursal\":0,\"p_IdModulo\":\"MFW\",\"p_UsaReglasNegocio\":\"N\",\"p_tablaOperVencidas\":[{\"IdCartera\":542223,\"FolioCartera\":542395,\"IdOperacion\":150941,\"IdEstado\":\"VIG\",\"Precio\":0.0245,\"IdMonedaLiquidacion\":\"CLP\"}]}";
			VencimientoOperacionForwardEntrada Objin = gson.fromJson(jsonDesa, VencimientoOperacionForwardEntrada.class);
			
			VencimientoOperacionForward demo = new VencimientoOperacionForward();
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			try {
 				VencimientoOperacionForwardSalida ObjOut = demo.logica(con, Objin);
 				con.commit();
 				//con.rollback();
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				//er.printStackTrace();	
				con.rollback();
			}
 			//con.commit();
			finally {
				con.close();
			}
	    }	
		System.out.println("== END ==");
	}
}