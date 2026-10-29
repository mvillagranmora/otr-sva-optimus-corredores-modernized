package cl.bcs.mesa.FWD;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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
import cl.bcs.mesa.FWD.NotificarAnticipoForward.EntradaNotificarAnticipoForward;
import cl.bcs.mesa.FWD.VencimientoOperacionForward.CargoAbonoResultado;
import cl.bcs.mesa.FWD.VencimientoOperacionForward.ForwardCartera;
import cl.bcs.mesa.FWD.VencimientoOperacionForward.ForwardFomaPago;
import cl.bcs.mesa.FWD.VencimientoOperacionForward.ForwardSinSpot;
import cl.bcs.mesa.FWD.VencimientoOperacionForward.TesoreriaResultado;
import cl.bcs.mesa.MES.CambiarEstadoOperacion;
import cl.bcs.mesa.MES.CambiarEstadoOperacion.CambiarEstadoOperacionEntrada;
import cl.bcs.mesa.MES.CambiarEstadoOperacion.CambiarEstadoOperacionSalida;
import cl.bcs.mesa.MES.CambiarEstadoOperacion.ObjCambiarEstadoOperacionIN;
import cl.bcs.mesa.MES.CrearComprobantePagoOperacion;
import cl.bcs.mesa.MES.CrearComprobantePagoOperacion.ComprobantePagoOperacionObj_IN;
import cl.bcs.mesa.MES.CrearComprobantePagoOperacion.CrearComprobantePagoOperacion_IN;
import cl.bcs.mesa.MES.CrearComprobantePagoOperacion.CrearComprobantePagoOperacion_OUT;
import cl.bcs.plataforma.lim.pojo.ConstantesLimites;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.soc.SocioNegocioUtils;
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

public class AnticiparOperacionForward implements JavaCall{
	
	private final Log log = new Log("FWD.AnticiparOperacionForward");
	
	private final ConstantesLimites _cl = new ConstantesLimites();

	private final String SQL_SELECT_ANTICIPO_COMPLETO_O_PARCIAL = loadResourceToString("cl/bcs/mesa/FWD/sql/AnticiparOperacionForward_ConsultarAnticipoCompletoOParcial.sql");
	private final String SQL_SELECT_ANTICIPO_CONSULTAR_PARAMETROS = loadResourceToString("cl/bcs/mesa/FWD/sql/AnticiparOperacionForward_ConsultarParametrosMesaForward.sql");
	private final String SQL_SELECT_FORMA_DE_PAGO_PARTE_ANTICIPADA = loadResourceToString("cl/bcs/mesa/FWD/sql/AnticiparOperacionForward_ConsultarFormaDePagoParteAnticipada.sql");
	private final String SQL_SELECT_FORMA_DE_PAGO_PARTE_VIGENTE = loadResourceToString("cl/bcs/mesa/FWD/sql/AnticiparOperacionForward_ConsultarFormaDePagoParteVigente.sql");
	private final String SQL_SELECT_OPERACIONES_FORWARD_ENTREGA_FISICA = loadResourceToString("cl/bcs/mesa/FWD/sql/AnticiparOperacionForward_ConsultarOperacionesForwardEntregaFisica.sql");
	private final String SQL_UPDATE_OPERACIONES_SPOT_FOLIO_ORIGEN_FW = loadResourceToString("cl/bcs/mesa/FWD/sql/AnticiparOperacionForward_UpdateOperacionesSpotFolioOrigenFw.sql");
	private final String SQL_UPDATE_CARTERA_ANTICIPO_ENTREGA_FISICA = loadResourceToString("cl/bcs/mesa/FWD/sql/AnticiparOperacionForward_UpdateCarteraAnticipoEntregaFisica.sql");
	private final String SQL_DELETE_RESERVA_ID_OPERACION = loadResourceToString("cl/bcs/mesa/FWD/sql/AnularOperacionForward_DeleteReservaIdOperacion.sql");
	private final String SQL_DELETE_RESERVA_FOLIO_CARTERA_ORIGEN = loadResourceToString("cl/bcs/mesa/FWD/sql/AnularOperacionForward_DeleteReservaFolioCarteraOrigen.sql");
	private final String SQL_SELECT_OPERACIONES_FORWARD_ENTREGA_FISICA_SIN_SPOT = loadResourceToString("cl/bcs/mesa/FWD/sql/VencimientoOperacionForward_ConsultarOperacionesForwardEntregaFisicaSinSpot.sql");
	private final String SQL_SELECT_OPERACIONES_FORWARD_ENTREGA_FISICA_FORMA_PAGO = loadResourceToString("cl/bcs/mesa/FWD/sql/VencimientoOperacionForward_ConsultarOperacionesForwardEntregaFisicaFormaDePago.sql");
	private final String SQL_SELECT_FORMA_DE_PAGO_ENTREGA_FISICA = loadResourceToString("cl/bcs/mesa/FWD/sql/AnticiparOperacionForward_ConsultarFormaDePagoEntregaFisica.sql");
	private final String SQL_UPDATE_COMPROBANTE_PAGO_OPERACION_ENTREGA_FISICA = loadResourceToString("cl/bcs/mesa/FWD/sql/AnticiparOperacionForward_UpdateComprobantePagoOperacionEntregaFisica.sql");
	
	public static class OperacionForwardObjIn{
		public Long FolioInterno;
		public Long FolioCartera;
		public String IdTipoOperacion;
		public String IdTipoInstrumento;
		public String IdTipoProducto;
		public Long IdInstrumento;
		public String Nemotecnico;
		public Long IdCliente; 
		public Long IdPortafolio;
		public Long IdPersonaEjecutivo;
		public String IdTipoCanalIngreso;
		public String IdMonedaPrincipal;	
		public String IdMonedaSecundaria;
		public BigDecimal MontoOperacion;
		public BigDecimal MontoInicial;
		public BigDecimal MontoFinal;
		public Date FechaOperacion;
		public BigDecimal TCCierre;
		public BigDecimal ParidadCierre;
		public Long Plazo;
		public Date FechaVencimiento;
		public Date FechaPago;
		public Long IdModalidadLiquidacion;
		public Long IdReferenciaMercado;
		public Long IdPlaza;
		public BigDecimal TCCosto;
		public String PuntosTasasForward;
		public BigDecimal PuntosForward;
		public BigDecimal TCForward;
		public BigDecimal TasasMonedaPrincipal;
		public BigDecimal TasasMonedaSecundaria;
		public BigDecimal MontoDiferido;
		public BigDecimal MontoMargen;
		public Long IdFixing;
		public Date FechaFixing;
		public String HoraFixing;
		public Long IdMesa;
		public Long IdSpotAsociado;
		public String IdClasificacionCartera;
		public String Observaciones;
		public String IdEstado;
		public BigDecimal TasaDescuento;
		public BigDecimal TipoCambioAnticipo;
		public BigDecimal MontoARecibirAnticipo;
		public BigDecimal TipoCambioObservado;
		public BigDecimal MontoPrincipalAnticipo;
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class AnticiparOperacionForwardEntrada {
		public Long p_IdEmpresa;
		public Long p_IdPais;
		public Long p_IdUsuario;
		public String p_IdBolsa;
		public Long p_IdSucursal;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public List<OperacionForwardObjIn> p_tablaForward;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class AnticiparOperacionForwardSalida {
		public List<MensajeSalida>		P_OUT_MENSAJES;
		public Long						P_OUT_ERRORESCONTADOR;
		public Long						P_OUT_ADVERTENCIASCONTADOR;
		public List<OperacionForwardObjOut>	P_OUT_TBLIDFORWARD;
		
		public AnticiparOperacionForwardSalida() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_TBLIDFORWARD = new ArrayList<OperacionForwardObjOut>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class DatosAnticipoCompletoOParcial{
		public Long IdCartera;
		public Long FolioCartera;
		public Long IdOperacion;
		public String IdTipoOperacion;
		public String IdTipoProducto;
		public BigDecimal MontoMonedaPrincipal;
		public BigDecimal MontoMonedaSecundaria;
		public Date FechaLiquidacion;
		public Date FechaOperacion;
		public String IdNuevoEstado;
		public String TipoAnticipo;
		public Long IdModalidadCumplimiento;
		public String ModalidadLiquidacionValor;
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class FormaDePagoAnticipoForward{
		public Long IdInternoPagoOperacion;
		public Long IdComprobantePagoOperacion;
		public Long IdEmpresa;
		public Long IdOperacion;
		public Long IdIngresoEgreso;
		public Long IdAbono;
		public Long IdCargo;
		public String IdTipoProducto;
		public String RecibePaga;
		public String IdFormaDePago;
		public String IdModalidadPago;
		public String IdMoneda;
		public String IdMonedaPrincipal;
		public String IdMonedaSecundaria;
		public String IdMonedaLiquidacion;
		public BigDecimal Monto;
		public BigDecimal MontoMonedaPrincipal;
		public BigDecimal MontoMonedaSecundaria;
		public BigDecimal MontoLiquidacion;
		public Date FechaIngresoComprobantePagoOperacion;
		public Date FechaLiquidacionComprobantePagoOperacion;
		public String IdEstado;
		public String AfectarTesoreria;
		public Long IdCartera;
		public Long IdOperacionCartera;
		public Long FolioCartera;
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class DatosSpotOrigenForward{
		public Long IdCartera;
		public Long IdInstrumento;
		public String IdTipoOperacion;
		public Long IdCliente;
		public Long IdPortafolio;
		public String IdEstado;
		public String EGRIdTipoOperacion;
		public java.sql.Timestamp EGRFechaOperacion;
		public java.sql.Timestamp FechaVencimientoOperacion;
		public String IdMonedaPrincipal;
		public String IdMonedaSecundaria;
		public BigDecimal INGMontoMonedaPrincipal;
		public BigDecimal INGMontoMonedaSecundaria;
		public BigDecimal EGRMontoMonedaPrincipal;
		public BigDecimal EGRMontoMonedaSecundaria;
		public BigDecimal ValorTipoCambioCierre;
		public BigDecimal ValorParidadCierre;
		public BigDecimal ValorTipoCambioCosto;
		public BigDecimal ValorParidadCosto;
		public Long IdMesa;
		public Long IdAgente;
		public String INGGlosa;
		public Long FolioCartera;
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class ParametrosForward {
		public String CodigoParametro;
		public String Valor;
		
		public String toString() {
			return Utils.printJson(this, false);
		};		
	}
	
	public static class UpdateComprobantePagoOperacionEntregaFisica{
			public Date FechaAnticipo;
			public Long IdComprobantePagoOperacion;
			
			public String toString() {
				return Utils.printJson(this, false);
			}	
		}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		AnticiparOperacionForwardEntrada in = gson.fromJson(jin.toString(), AnticiparOperacionForwardEntrada.class);
		AnticiparOperacionForwardSalida out = logica(con, in);
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
	
	public AnticiparOperacionForwardSalida logica(Connection conn, AnticiparOperacionForwardEntrada in) throws Exception {
		SQL _sql = new SQL();
		AnticiparOperacionForwardSalida out = new AnticiparOperacionForwardSalida();
		out.P_OUT_ERRORESCONTADOR = 0L;
		out.P_OUT_ADVERTENCIASCONTADOR = 0L;
		Long Id = 0L;
		HashMap<String,String> mapMsg = new HashMap<>();
		
		log.write("INICIO :: FWD.AnticiparOperacionForward");
		
		try {
			if (in.p_tablaForward.size() == 0) {
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
			
			SocioNegocioUtils.Empleado empleadoUsuario = SocioNegocioUtils.obtenerEmpleadoUsuario(conn, in.p_IdEmpresa, in.p_IdUsuario).orElse(null);
			if (empleadoUsuario == null) {
				out.P_OUT_ERRORESCONTADOR++;
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_USUARIO_NO_ES_EMPLEADO", new HashMap<>(), conn, in.p_IdEmpresa));
				return out;
			}
			
			// obtener dias habiles minimo para anticipar forward
			Long diasHabilesMinimoAnticipo = 0L;
			List<MensajeSalida> mensajeAnticipoForward = new ArrayList<PlataformaUtils.MensajeSalida>();
			
			HashMap<String,Object> paramsConsultarParametros = new HashMap<>();
			List<String> codigoParametros = new ArrayList<String>();
			codigoParametros.add("DIAANTICIPO");
			paramsConsultarParametros.put("CodigoParametros", codigoParametros);
			paramsConsultarParametros.put("IdEmpresa", in.p_IdEmpresa);
			List<ParametrosForward> valoresCodigoParametros = _sql.query(conn, SQL_SELECT_ANTICIPO_CONSULTAR_PARAMETROS, paramsConsultarParametros, ParametrosForward.class);
			
			if (!valoresCodigoParametros.isEmpty()) {
				for(ParametrosForward parametro: valoresCodigoParametros) {
					if (parametro.CodigoParametro.equals("DIAANTICIPO") && !parametro.Valor.equals("")) {
						try {
							diasHabilesMinimoAnticipo = Long.parseLong(parametro.Valor);
							
						} catch (Exception e) {		// valida si el parametro es un numero valido
							String mensajeError= Diccionario.getMensaje(conn,in.p_IdEmpresa,"UI_PARAMETRO_NUMERICO_INVALIDO")
															.add("NombreParametro", "DIAANTICIPO")
									  						.msg();
							mensajeAnticipoForward.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_PARAMETRO_NUMERICO_INVALIDO", mensajeError));
							out.P_OUT_MENSAJES.addAll(mensajeAnticipoForward);
							out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
							return out;							
						}
					}
				}
			}
			
			/***** VARIABLES *****/
			Long cantOpeIn = 0L;
			Date fechaSistema = Fecha.toDate(Utils.getConfiguracion(conn, Utils.getSchema(), in.p_IdEmpresa, "PLA.FechaSistema"));
			
			HashMap<String,Object> params = new HashMap<>();
			List<TablaForwardObjIn> operacionAnticipada = new ArrayList<TablaForwardObjIn>();
			List<Long> idOperacion = new ArrayList<Long>();
			List<Long> folioCartera = new ArrayList<Long>();
			List<Long> idCarteraParteAnticipada = new ArrayList<Long>();
			
			for(OperacionForwardObjIn ope: in.p_tablaForward){
				idOperacion.add(ope.FolioInterno);
				folioCartera.add(ope.FolioCartera);
				cantOpeIn++;
			}
			
			
			/***** ANTICIPO DE FORWARD EN CARTERA *****/
			params.put("IdEmpresa", in.p_IdEmpresa);
			params.put("IdOperacion", idOperacion);
			params.put("FolioCartera", folioCartera);
			List<DatosAnticipoCompletoOParcial> anticipoCompletoOParcial = _sql.query(conn, SQL_SELECT_ANTICIPO_COMPLETO_O_PARCIAL, params, DatosAnticipoCompletoOParcial.class);
			
			for(DatosAnticipoCompletoOParcial acp: anticipoCompletoOParcial){
				// validar fecha minima de anticipo segun parametro DIAANTICIPO
				Date fechaAnticipoMinimo = Fecha.toDate(obtenerDiaSiguienteHabil(conn, acp.FechaOperacion.toString(), diasHabilesMinimoAnticipo, in.p_IdEmpresa));
				if (fechaSistema.compareTo(fechaAnticipoMinimo)==-1) {
					String mensajeError= Diccionario.getMensaje(conn,in.p_IdEmpresa,"UI_PLAZO_MINIMO_ANTICIPAR_FORWARD")
							  						.add("FechaOperacion", Fecha.toFormat(acp.FechaOperacion, "dd-MM-yyyy"))
							  						.add("FolioCartera", acp.FolioCartera)
							  						.add("CantidadDias", diasHabilesMinimoAnticipo)
							  						.msg();
					mensajeAnticipoForward.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_PLAZO_MINIMO_ANTICIPAR_FORWARD", mensajeError));
					out.P_OUT_MENSAJES.addAll(mensajeAnticipoForward);
					out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
					return out;
				}
				
				for(OperacionForwardObjIn opeFwdIn: in.p_tablaForward){
					if(acp.IdOperacion.longValue() == opeFwdIn.FolioInterno.longValue() && acp.FolioCartera.longValue() == opeFwdIn.FolioCartera.longValue()){
						//Determina si es Anticipo Completo o Parcial
						if (opeFwdIn.MontoPrincipalAnticipo.compareTo(acp.MontoMonedaPrincipal) == 0) {
							acp.IdNuevoEstado = "ENTREGA_FISICA".equals(acp.ModalidadLiquidacionValor) ? "LIQ" : "ANT";
						}
						else {
							acp.IdNuevoEstado = "VIG";
						}
						acp.TipoAnticipo = acp.IdNuevoEstado.equals("ANT") ? "COMPLETO" : "PARCIAL";
						
						TablaForwardObjIn tfi = new TablaForwardObjIn();
						tfi.FolioInterno = opeFwdIn.FolioCartera;
						tfi.IdOperacion = opeFwdIn.FolioInterno;
						tfi.FolioCartera = opeFwdIn.FolioCartera;
						tfi.IdTipoOperacionOriginal = acp.IdTipoOperacion;
						tfi.IdTipoOperacionNuevo = "ANTFWD";
						tfi.IdTipoInstrumento = opeFwdIn.IdTipoInstrumento;
						tfi.IdClasificacionTipoInstrumento = opeFwdIn.IdClasificacionCartera;
						tfi.IdTipoProducto = opeFwdIn.IdTipoProducto;
						tfi.IdInstrumento = opeFwdIn.IdInstrumento;
						tfi.Nemotecnico = opeFwdIn.Nemotecnico;
						tfi.IdClienteOriginal = opeFwdIn.IdCliente;
						tfi.IdClienteNuevo = opeFwdIn.IdCliente;
						tfi.IdPortafolioOriginal = opeFwdIn.IdPortafolio;
						tfi.IdPortafolioNuevo = opeFwdIn.IdPortafolio;
						tfi.IdPersonaEjecutivoOriginal = opeFwdIn.IdPersonaEjecutivo;
						tfi.IdPersonaEjecutivoNuevo = opeFwdIn.IdPersonaEjecutivo;
						tfi.IdTipoCanalIngreso = opeFwdIn.IdTipoCanalIngreso;
						tfi.IdMonedaPrincipal = opeFwdIn.IdMonedaPrincipal;	
						tfi.IdMonedaSecundaria = opeFwdIn.IdMonedaSecundaria;
						tfi.PrecioVencimiento = BigDecimal.ZERO;
						tfi.MontoOperacionOriginal = opeFwdIn.MontoOperacion;
						if(acp.TipoAnticipo.equals("PARCIAL")){
							tfi.MontoOperacionNuevo = acp.MontoMonedaPrincipal.subtract(opeFwdIn.MontoOperacion);
						}
						tfi.MontoInicialOriginal = opeFwdIn.MontoInicial;
						tfi.MontoInicialNuevo = opeFwdIn.MontoInicial;
						tfi.MontoFinalOriginal = opeFwdIn.MontoFinal;
						tfi.MontoFinalNuevo = opeFwdIn.MontoFinal;
						tfi.FechaOperacion = opeFwdIn.FechaOperacion;
						tfi.TCCierreOriginal = opeFwdIn.TCCierre;
						tfi.TCCierreNuevo = opeFwdIn.TCCierre;
						tfi.ParidadCierreOriginal = opeFwdIn.ParidadCierre;
						tfi.ParidadCierreNuevo = opeFwdIn.ParidadCierre;
						tfi.PlazoOriginal = opeFwdIn.Plazo;
						tfi.PlazoNuevo = opeFwdIn.Plazo;
						//tfi.FechaVencimientoOriginal = opeFwdIn.FechaVencimiento;
						tfi.FechaVencimientoOriginal = acp.FechaLiquidacion;
						//tfi.FechaVencimientoNuevo = opeFwdIn.FechaVencimiento;
						tfi.FechaVencimientoNuevo = acp.FechaLiquidacion;
						tfi.FechaPagoOriginal = opeFwdIn.FechaPago;
						tfi.FechaPagoNuevo = opeFwdIn.FechaPago;
						tfi.IdModalidadLiquidacionOriginal = opeFwdIn.IdModalidadLiquidacion;
						tfi.IdModalidadLiquidacionNuevo = opeFwdIn.IdModalidadLiquidacion;
						tfi.IdReferenciaMercado = opeFwdIn.IdReferenciaMercado;
						tfi.IdPlaza = opeFwdIn.IdPlaza;
						tfi.TCCostoOriginal = opeFwdIn.TCCosto;
						tfi.TCCostoNuevo = opeFwdIn.TCCosto;
						tfi.PuntosTasasForwardOriginal = opeFwdIn.PuntosTasasForward;
						tfi.PuntosTasasForwardNuevo = opeFwdIn.PuntosTasasForward;
						tfi.PuntosForwardOriginal = opeFwdIn.PuntosForward;
						tfi.PuntosForwardNuevo = opeFwdIn.PuntosForward;
						tfi.TCForwardOriginal = opeFwdIn.TCForward;
						tfi.TCForwardNuevo = opeFwdIn.TCForward;
						tfi.TasasMonedaPrincipalOriginal = opeFwdIn.TasasMonedaPrincipal;
						tfi.TasasMonedaPrincipalNuevo = opeFwdIn.TasasMonedaPrincipal;
						tfi.TasasMonedaSecundariaOriginal = opeFwdIn.TasasMonedaSecundaria;
						tfi.TasasMonedaSecundariaNuevo = opeFwdIn.TasasMonedaSecundaria;
						tfi.MontoDiferidoOriginal = opeFwdIn.MontoDiferido;
						tfi.MontoDiferidoNuevo = opeFwdIn.MontoDiferido;
						tfi.MontoMargenOriginal = opeFwdIn.MontoMargen;
						tfi.MontoMargenNuevo = opeFwdIn.MontoMargen;
						tfi.IdFixingOriginal = opeFwdIn.IdFixing;
						tfi.IdFixingNuevo = opeFwdIn.IdFixing;
						tfi.FechaFixingOriginal = opeFwdIn.FechaFixing;
						tfi.FechaFixingNuevo = opeFwdIn.FechaFixing;
						tfi.HoraFixingOriginal = opeFwdIn.HoraFixing;
						tfi.HoraFixingNuevo = opeFwdIn.HoraFixing;
						tfi.IdMesa = opeFwdIn.IdMesa;
						tfi.IdSpotAsociado = opeFwdIn.IdSpotAsociado;
						tfi.Observaciones = opeFwdIn.Observaciones;
						tfi.IdEstado = opeFwdIn.IdEstado;
						tfi.TasaDescuento = opeFwdIn.TasaDescuento;
						tfi.TipoCambioAnticipo = opeFwdIn.TipoCambioAnticipo;
						tfi.MontoARecibirAnticipo = opeFwdIn.MontoARecibirAnticipo.setScale(0, RoundingMode.HALF_UP);
						tfi.TipoCambioObservado = opeFwdIn.TipoCambioObservado;
						tfi.TipoAnticipo = acp.TipoAnticipo;
						operacionAnticipada.add(tfi);
					}
				}
			}
			
			/***** LIMITES *****/
			Long autCont = 0L;
			Long idInternoForward = 1L;
			for(TablaForwardObjIn opeFwd : operacionAnticipada) {
				opeFwd.IdInterno = idInternoForward++;
				opeFwd.CodigoOperador = empleadoUsuario.CodigoEmpleado;
			}
			
			List<MensajeSalida> mensajesValidarLimitesCuentaPropiaForward = validarLimitesCuentaPropia(conn, in, operacionAnticipada);
			out.P_OUT_MENSAJES.addAll(mensajesValidarLimitesCuentaPropiaForward);
			
			log.write("MESA FORWARD :: Llamada Ingresar Forward Cartera");
			IngresarForwardCartera ingresarForwardCartera = new IngresarForwardCartera();
			IngresarForwardCarteraEntrada ingresarForwardCarteraIn = new IngresarForwardCarteraEntrada();
			ingresarForwardCarteraIn.p_IdEmpresa = in.p_IdEmpresa;
			ingresarForwardCarteraIn.p_IdPais = in.p_IdPais;
			ingresarForwardCarteraIn.p_IdUsuario = in.p_IdUsuario;
			ingresarForwardCarteraIn.p_IdBolsa = in.p_IdBolsa;
			ingresarForwardCarteraIn.p_IdSucursal = in.p_IdSucursal;
			ingresarForwardCarteraIn.p_IdModulo = in.p_IdModulo;
			ingresarForwardCarteraIn.p_UsaReglasNegocio = "N";//En Anticipo FWD no es necesario validar Reglas
			ingresarForwardCarteraIn.p_tablaForward = operacionAnticipada;
			IngresarForwardCarteraSalida ingresarForwardCarteraOut = ingresarForwardCartera.logica(conn, ingresarForwardCarteraIn);
			
			out.P_OUT_ERRORESCONTADOR = out.P_OUT_ERRORESCONTADOR + ingresarForwardCarteraOut.P_OUT_ERRORESCONTADOR;
			out.P_OUT_MENSAJES.addAll(ingresarForwardCarteraOut.P_OUT_MENSAJES);
			if (MensajeSalida.getNumeroErrores(ingresarForwardCarteraOut.P_OUT_MENSAJES) > 0){
				return out;
			}
			
			//Salida del Servicio
			out.P_OUT_ERRORESCONTADOR = out.P_OUT_ERRORESCONTADOR + MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
			out.P_OUT_TBLIDFORWARD = new ArrayList<OperacionForwardObjOut>();
			for(DatosAnticipoCompletoOParcial acp: anticipoCompletoOParcial){
				for(TablaForwardObjOut fwdCarOut: ingresarForwardCarteraOut.P_OUT_TBLIDFORWARDRETIROS){
					if(acp.IdCartera.longValue() == fwdCarOut.IdCartera.longValue()){
						OperacionForwardObjOut opOut = new OperacionForwardObjOut();
						opOut.FolioInterno = fwdCarOut.FolioInterno;
						opOut.IdCartera = fwdCarOut.IdCartera;
						opOut.IdOperacion = fwdCarOut.IdOperacion == null ? acp.IdOperacion : fwdCarOut.IdOperacion;
						opOut.FolioCartera = fwdCarOut.FolioCartera;
						opOut.IdEstado = acp.IdNuevoEstado;
						out.P_OUT_TBLIDFORWARD.add(opOut);
						
						/*if(acp.TipoAnticipo.equals("COMPLETO")){
							idCarteraParteAnticipada.add(acp.IdCartera);
						}
						else{
							idCarteraParteVigente.add(fwdCarOut.IdCartera);
						}*/
						idCarteraParteAnticipada.add(acp.IdCartera);
					}
				}
			}
			
			
			/***** CAMBIAR ESTADO *****/
			CambiarEstadoOperacion cambiarEstadoOperacion = new CambiarEstadoOperacion();
			CambiarEstadoOperacionEntrada cambiarEstadoOperacionIn = new CambiarEstadoOperacionEntrada(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo, "N", in.p_UsaReglasNegocio, in.p_IdPais);
			CambiarEstadoOperacionSalida cambiarEstadoOperacionOut = new CambiarEstadoOperacionSalida();
			for(DatosAnticipoCompletoOParcial acp: anticipoCompletoOParcial){
				ObjCambiarEstadoOperacionIN ceoIn = new ObjCambiarEstadoOperacionIN();
				ceoIn.IdOperacion = acp.IdOperacion;
				ceoIn.IdTipoProducto = acp.IdTipoProducto;
				ceoIn.IdNuevoEstado = acp.IdNuevoEstado;
				ceoIn.IdReserva = 0L;
				ceoIn.FolioCarteraCustodia = acp.FolioCartera;
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
			
			/***** DELETE CALCE RESERVA *****/
			_sql.batch(conn, SQL_DELETE_RESERVA_ID_OPERACION, anticipoCompletoOParcial, DatosAnticipoCompletoOParcial.class);
			_sql.batch(conn, SQL_DELETE_RESERVA_FOLIO_CARTERA_ORIGEN, anticipoCompletoOParcial, DatosAnticipoCompletoOParcial.class);
			
			
			/***** FORMAS DE PAGO OPERACION FORWARD *****/
			List<ComprobantePagoOperacionObj_IN> comprobantePagoProceso  = new ArrayList<ComprobantePagoOperacionObj_IN>();
			long i = 0;
			
			//Arma las Formas de Pago para la operacion Anticipo Parcial, parte ANTICIPADA
			List<FormaDePagoAnticipoForward> formaDePago = _sql.query(conn, SQL_SELECT_FORMA_DE_PAGO_PARTE_ANTICIPADA, params, FormaDePagoAnticipoForward.class);
			for(DatosAnticipoCompletoOParcial acp: anticipoCompletoOParcial){
				if(acp.IdNuevoEstado.equals("VIG")){
					for(FormaDePagoAnticipoForward fpParteAnticipada: formaDePago){
						if(acp.IdCartera.longValue() == fpParteAnticipada.IdCartera.longValue()){
							ComprobantePagoOperacionObj_IN cpoIn = new ComprobantePagoOperacionObj_IN();
							cpoIn.IdInternoPagoOperacion = i;
							cpoIn.IdComprobantePagoOperacion = fpParteAnticipada.IdComprobantePagoOperacion;
							cpoIn.IdEmpresa = in.p_IdEmpresa;
							cpoIn.IdOperacion = fpParteAnticipada.IdOperacion;
							cpoIn.IdIngresoEgreso = fpParteAnticipada.IdIngresoEgreso;
							cpoIn.IdAbono = fpParteAnticipada.IdAbono;
							cpoIn.IdCargo = fpParteAnticipada.IdCargo;
							cpoIn.IdIngresoEgresoAnticipo = 0L;
							cpoIn.IdAbonoAnticipo = 0L;
							cpoIn.IdCargoAnticipo = 0L;
							cpoIn.IdTipoProducto = fpParteAnticipada.IdTipoProducto;
							cpoIn.RecibePagaPortafolio = fpParteAnticipada.RecibePaga;
							cpoIn.IdFormaDePago = fpParteAnticipada.IdFormaDePago;
							cpoIn.IdModalidadPago = fpParteAnticipada.IdModalidadPago;
							cpoIn.FechaIngresoComprobantePagoOperacion = Fecha.toStringDate(fpParteAnticipada.FechaIngresoComprobantePagoOperacion);
							cpoIn.FechaLiquidacionComprobantePagoOperacion = Fecha.toStringDate(fpParteAnticipada.FechaLiquidacionComprobantePagoOperacion);
							cpoIn.IdEstado = fpParteAnticipada.IdEstado;
							cpoIn.AfectarTesoreria = fpParteAnticipada.AfectarTesoreria;
							
							if(fpParteAnticipada.IdMoneda.equals(fpParteAnticipada.IdMonedaPrincipal)){
								cpoIn.IdMoneda = fpParteAnticipada.IdMonedaPrincipal;
								cpoIn.Monto = fpParteAnticipada.MontoMonedaPrincipal;
							}
							else if(fpParteAnticipada.IdMoneda.equals(fpParteAnticipada.IdMonedaSecundaria)){
								cpoIn.IdMoneda = fpParteAnticipada.IdMonedaSecundaria;
								cpoIn.Monto = fpParteAnticipada.MontoMonedaSecundaria;
							}
							//cpoIn.IdMonedaLiquidacion = cpoIn.IdMoneda;
							//cpoIn.MontoLiquidacion = cpoIn.Monto;
							
							comprobantePagoProceso.add(cpoIn);
							i++;
						}
					}
				}
			}
			
			//Arma las Formas de Pago para la operacion Anticipo Parcial, nueva parte VIGENTE
			//params.clear();
			//params.put("IdEmpresa", in.p_IdEmpresa);
			params.put("IdCarteraParteAnticipada", idCarteraParteAnticipada);
			//params.put("IdCarteraParteVigente", idCarteraParteVigente);
			formaDePago = _sql.query(conn, SQL_SELECT_FORMA_DE_PAGO_PARTE_VIGENTE, params, FormaDePagoAnticipoForward.class);
			
			for(DatosAnticipoCompletoOParcial acp: anticipoCompletoOParcial){
				for(FormaDePagoAnticipoForward fpParteVigente: formaDePago){
					if(acp.IdCartera.longValue() == fpParteVigente.IdOperacion.longValue() && acp.FolioCartera.longValue() == fpParteVigente.FolioCartera){
						ComprobantePagoOperacionObj_IN cpoIn = new ComprobantePagoOperacionObj_IN();
						cpoIn.IdInternoPagoOperacion = i;
						cpoIn.IdComprobantePagoOperacion = 0L;
						cpoIn.IdEmpresa = in.p_IdEmpresa;
						cpoIn.IdOperacion = fpParteVigente.IdCartera;
						cpoIn.IdIngresoEgreso = fpParteVigente.IdIngresoEgreso;
						cpoIn.IdAbono = fpParteVigente.IdAbono;
						cpoIn.IdCargo = fpParteVigente.IdCargo;
						cpoIn.IdIngresoEgresoAnticipo = 0L;
						cpoIn.IdAbonoAnticipo = 0L;
						cpoIn.IdCargoAnticipo = 0L;
						cpoIn.IdTipoProducto = fpParteVigente.IdTipoProducto;
						cpoIn.RecibePagaPortafolio = fpParteVigente.RecibePaga;
						cpoIn.IdFormaDePago = fpParteVigente.IdFormaDePago;
						cpoIn.IdModalidadPago = fpParteVigente.IdModalidadPago;
						cpoIn.FechaIngresoComprobantePagoOperacion = Fecha.toStringDate(fpParteVigente.FechaIngresoComprobantePagoOperacion);
						cpoIn.FechaLiquidacionComprobantePagoOperacion = Fecha.toStringDate(fpParteVigente.FechaLiquidacionComprobantePagoOperacion);
						cpoIn.IdEstado = fpParteVigente.IdEstado;
						cpoIn.AfectarTesoreria = fpParteVigente.AfectarTesoreria;
						
						if(fpParteVigente.IdMoneda.equals(fpParteVigente.IdMonedaPrincipal)){
							cpoIn.IdMoneda = fpParteVigente.IdMonedaPrincipal;
							cpoIn.Monto = fpParteVigente.MontoMonedaPrincipal;
						}
						else if(fpParteVigente.IdMoneda.equals(fpParteVigente.IdMonedaSecundaria)){
							cpoIn.IdMoneda = fpParteVigente.IdMonedaSecundaria;
							cpoIn.Monto = fpParteVigente.MontoMonedaSecundaria;
						}
						//cpoIn.IdMonedaLiquidacion = cpoIn.IdMoneda;
						//cpoIn.MontoLiquidacion = cpoIn.Monto;
						
						comprobantePagoProceso.add(cpoIn);
						i++;
					}
				}
			}
			
			//Comprobante Pago Operacion de Mesa
			if(comprobantePagoProceso.size() > 0){
				log.write("MESA FORWARD :: Llamada Ingreso Comprobante Pago Operacion");
				CrearComprobantePagoOperacion crearComPag = new CrearComprobantePagoOperacion();
				CrearComprobantePagoOperacion_IN crearComPagIn = new CrearComprobantePagoOperacion_IN();
				crearComPagIn.p_IdEmpresa = in.p_IdEmpresa;
				crearComPagIn.p_IdUsuario = in.p_IdUsuario;
				crearComPagIn.p_IdModulo = in.p_IdModulo;
				crearComPagIn.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
				crearComPagIn.p_ComprobantePagoOperacion = comprobantePagoProceso;
				CrearComprobantePagoOperacion_OUT crearComPagOut = crearComPag.logica(conn, crearComPagIn);
				out.P_OUT_MENSAJES.addAll(crearComPagOut.P_OUT_MENSAJES);
				if (MensajeSalida.getNumeroErrores(crearComPagOut.P_OUT_MENSAJES) > 0){
					return out;
				}
			}
			
			
			/***** FORWARD CON ENTREGA FISICA GENERA OPER. SPOT *****/
			String fwdIntegradoConSpot = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "MESA", "FwdIntegradoConSpot");
			params.clear();
			params.put("IdEmpresa", in.p_IdEmpresa);
			params.put("IdCarteraParteAnticipada", idCarteraParteAnticipada);
			List<SpotOrigenForwardObjIn> operacionForwardEntregaFisica = _sql.query(conn, SQL_SELECT_OPERACIONES_FORWARD_ENTREGA_FISICA, params, SpotOrigenForwardObjIn.class);
			
			if(operacionForwardEntregaFisica.size() > 0){
				params.put("IdOperacion", idOperacion);
				params.put("FolioCartera", folioCartera);
				List<FormaDePagoAnticipoForward> formaDePagoFisica = _sql.query(conn, SQL_SELECT_FORMA_DE_PAGO_ENTREGA_FISICA, params, FormaDePagoAnticipoForward.class);
				
				if (formaDePagoFisica.size() > 0 ) {
					List<UpdateComprobantePagoOperacionEntregaFisica> updateFechaLiquidacion = new ArrayList<>();
						
					for(DatosAnticipoCompletoOParcial acp: anticipoCompletoOParcial){
						for(FormaDePagoAnticipoForward datosFormaPago: formaDePagoFisica){
							if(acp.IdCartera.longValue() == datosFormaPago.IdOperacion.longValue() && acp.FolioCartera.longValue() == datosFormaPago.FolioCartera){
								UpdateComprobantePagoOperacionEntregaFisica datosUpdateSQL = new UpdateComprobantePagoOperacionEntregaFisica();
								datosUpdateSQL.FechaAnticipo = fechaSistema;
								datosUpdateSQL.IdComprobantePagoOperacion = datosFormaPago.IdComprobantePagoOperacion;
								
								updateFechaLiquidacion.add(datosUpdateSQL);
							}
						}
					}
					//Update fecha de liquidacion a la fecha de anticipo 
					if (updateFechaLiquidacion.size() > 0) {	
						_sql.batch(conn, SQL_UPDATE_COMPROBANTE_PAGO_OPERACION_ENTREGA_FISICA, updateFechaLiquidacion, UpdateComprobantePagoOperacionEntregaFisica.class);
					}
				}
			}
			
			if(operacionForwardEntregaFisica.size() > 0){
				if (fwdIntegradoConSpot.equals("S")) {
					CrearOperacionSpotOrigenForward crearSpotOrigenFwd = new CrearOperacionSpotOrigenForward();
					CrearOperacionSpotOrigenForwardEntrada crearSpotOrigenFwdEntrada = new CrearOperacionSpotOrigenForwardEntrada();
					crearSpotOrigenFwdEntrada.p_IdEmpresa = in.p_IdEmpresa;
					crearSpotOrigenFwdEntrada.p_IdUsuario = in.p_IdUsuario;
					crearSpotOrigenFwdEntrada.p_IdModulo = in.p_IdModulo;
					crearSpotOrigenFwdEntrada.p_datosForward = operacionForwardEntregaFisica;
					CrearOperacionSpotOrigenForwardSalida crearSpotOrigenFwdSalida = crearSpotOrigenFwd.logica(conn, crearSpotOrigenFwdEntrada);
					
					out.P_OUT_ERRORESCONTADOR = out.P_OUT_ERRORESCONTADOR + crearSpotOrigenFwdSalida.P_OUT_ERRORESCONTADOR;
					out.P_OUT_ADVERTENCIASCONTADOR = out.P_OUT_ADVERTENCIASCONTADOR + crearSpotOrigenFwdSalida.P_OUT_ADVERTENCIASCONTADOR;
					out.P_OUT_MENSAJES.addAll(crearSpotOrigenFwdSalida.P_OUT_MENSAJES);
					if (MensajeSalida.getNumeroErrores(crearSpotOrigenFwdSalida.P_OUT_MENSAJES) > 0){
						return out;
					}

					/***** UPDATE CAMPO FolioOrigenFW->FolioCartera PARA LOS SPOT ORIGEN FORWARD *****/
					_sql.batch(conn, SQL_UPDATE_OPERACIONES_SPOT_FOLIO_ORIGEN_FW, crearSpotOrigenFwdSalida.P_OUT_SPOTORIGENFORWARD, SpotOrigenForwardObjOut.class);
					
					/***** UPDATE CAMPO IdEstado Y EGRIdSpotAsociado EN CARTERA PARA LOS ANTICIPOS MODALIDAD ENTREGA FISICA (GENERAN SPOT) *****/
					//IMPCREDOPT-2815, IMPCREDOPT-3466 operacion fwd EF Anticipada queda estado LIQ
					//En revision con Jorge Ossandon, se deja esto como parche, pero lo correcto es que las op fwd EF queden liquidadas cuando el spot lo este.
					_sql.batch(conn, SQL_UPDATE_CARTERA_ANTICIPO_ENTREGA_FISICA, crearSpotOrigenFwdSalida.P_OUT_SPOTORIGENFORWARD, SpotOrigenForwardObjOut.class);
				} else {
					HashMap<String,Object> parametros 	= new HashMap<>();
					parametros.put("p_IdCartera", operacionForwardEntregaFisica.stream().map(forward -> forward.IdCartera).collect(Collectors.toList()));
					parametros.put("p_IdEmpresa", in.p_IdEmpresa);
					List<ForwardSinSpot> operacionesForwardEntregaFisicaSinSpot = _sql.query(conn, SQL_SELECT_OPERACIONES_FORWARD_ENTREGA_FISICA_SIN_SPOT, parametros, ForwardSinSpot.class);
					parametros.clear();
					parametros.put("p_IdOperacion", operacionForwardEntregaFisica.stream().map(forward -> forward.IdCartera).collect(Collectors.toList()));
					List<ForwardFomaPago> operacionesForwardEntregaFisicaFormaDePago = _sql.query(conn, SQL_SELECT_OPERACIONES_FORWARD_ENTREGA_FISICA_FORMA_PAGO, parametros, ForwardFomaPago.class);
					parametros.clear();
					CargoAbonoResultado cargoAbonoResultado = generaCargoAbono(conn, operacionesForwardEntregaFisicaSinSpot, operacionesForwardEntregaFisicaFormaDePago, in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo, Fecha.toStringDate(fechaSistema));
					out.P_OUT_MENSAJES.addAll(cargoAbonoResultado.P_OUT_MENSAJES);
					out.P_OUT_ERRORESCONTADOR =+ cargoAbonoResultado.P_OUT_ERRORESCONTADOR;
					TesoreriaResultado resultadoIngresoTesoreria = generarMovimientoTesoreria(conn, operacionesForwardEntregaFisicaSinSpot, operacionesForwardEntregaFisicaFormaDePago, in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo, Fecha.toStringDate(fechaSistema));
					out.P_OUT_MENSAJES.addAll(resultadoIngresoTesoreria.P_OUT_MENSAJES);
					out.P_OUT_ERRORESCONTADOR =+ resultadoIngresoTesoreria.P_OUT_ERRORESCONTADOR;
					if (out.P_OUT_ERRORESCONTADOR == 0) {
						List<ForwardCartera> forwardCarteraActualiza = operacionesForwardEntregaFisicaSinSpot.stream()
								.filter(fwd_ef -> fwd_ef.INGCantidad.compareTo(fwd_ef.EGRCantidad) == 0)
								.map(forward -> new ForwardCartera(forward.IdCartera)).collect(Collectors.toList());
						_sql.batch(conn, SQL_UPDATE_CARTERA_ANTICIPO_ENTREGA_FISICA, forwardCarteraActualiza, ForwardCartera.class);
					}
				}
			}
			
			/***** NOTIFICACIONES *****/
			NotificarAnticipoForward notificarAnticipoForward = new NotificarAnticipoForward();
			EntradaNotificarAnticipoForward entradaNotificarAnticipoForward = new EntradaNotificarAnticipoForward();
			for(Long cartera : idCarteraParteAnticipada){
				NotificarAnticipoForward.Cartera car = new NotificarAnticipoForward.Cartera();
				car.IdCartera = cartera;
				entradaNotificarAnticipoForward.p_IdCarteras.add(car);
			}
			
			if(entradaNotificarAnticipoForward.p_IdCarteras.size() > 0){
				entradaNotificarAnticipoForward.p_IdEmpresa = in.p_IdEmpresa;
				entradaNotificarAnticipoForward.p_IdUsuario = in.p_IdUsuario;
				entradaNotificarAnticipoForward.p_IdModulo = in.p_IdModulo;
				notificarAnticipoForward.logica(conn, entradaNotificarAnticipoForward);
			}
			
		}catch(Exception e){
			out.P_OUT_ERRORESCONTADOR = 1L;
			log.write(e);
			throw e;
		}
		
		return out;
	}
	
	private String obtenerDiaSiguienteHabil(Connection con, String fecha, Long dias, Long IdEmpresa) throws Exception {
		//obtener configuraciones
		HashMap<String, HashMap<String, String>> configuraciones = Utils.getConfiguraciones(con, IdEmpresa);
		long idPais = Long.valueOf(configuraciones.get("PLATAFORMA").get("PLA.PaisSistema"));
		List<FechasIn> fechasInList = new ArrayList<ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn>();

		FechasIn fechas = new FechasIn();
		fechas.fechaInicial = fecha;
		fechas.numeroDias = dias;	// cantidad de dias habiles a calcular
		fechas.pais = idPais;
		fechasInList.add(fechas);
		
		ObtenerDiaSiguiente diaSgte = new ObtenerDiaSiguiente();
		ObtenerDiaSiguiente.InObtenerDiaSiguiente diaSgteIn = new ObtenerDiaSiguiente.InObtenerDiaSiguiente();
		diaSgteIn.p_idPais = idPais;
		diaSgteIn.p_diaHabil = "S";
		diaSgteIn.p_fechas = fechasInList;
		
		ObtenerDiaSiguiente.OutObtenerDiaSiguiente salDiaSgte = diaSgte.logica(con, diaSgteIn);
		return salDiaSgte.p_out_fechas.get(0).fechaCalculada;		
	}
	
	private List<MensajeSalida> validarLimitesCuentaPropia(Connection conn, AnticiparOperacionForwardEntrada entrada, List<TablaForwardObjIn> operaciones) throws Exception{
		
		log.write("INICIO::AnticiparOperacionForward.validarLimitesCuentaPropia");
		long inicio = System.currentTimeMillis();
		
		ValidarLimitesCuentaPropiaForward.EntradaValidarLimitesCuentaPropiaForward entradaValidarLimitescuentaPropiaForward = new ValidarLimitesCuentaPropiaForward.EntradaValidarLimitesCuentaPropiaForward();
		entradaValidarLimitescuentaPropiaForward.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaValidarLimitescuentaPropiaForward.p_IdTipoProceso = _cl.PROCESO_ANTICIPAR;
		entradaValidarLimitescuentaPropiaForward.p_IdUsuario = entrada.p_IdUsuario;
		entradaValidarLimitescuentaPropiaForward.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
		entradaValidarLimitescuentaPropiaForward.p_OperacionesFwd = operaciones.stream()
				.map(u -> {
					ValidarLimitesCuentaPropiaForward.OperacionForward o = new ValidarLimitesCuentaPropiaForward.OperacionForward();
					o.IdInterno = u.IdInterno;
					o.IdOperacionFwd = u.IdOperacion;
					o.IdCliente = u.IdClienteNuevo;
					o.IdPortafolio = u.IdPortafolioNuevo;
					o.IdEstado = u.IdEstado;
					o.IdInstrumento = u.IdInstrumento;
					o.IdTipoInstrumento = u.IdTipoInstrumento;
					o.NemotecnicoInstrumento = u.Nemotecnico;
					o.CodigoOperador = u.CodigoOperador;
					o.IdTipoCanalIngreso = u.IdTipoCanalIngreso;
					o.IdTipoOperacion = u.IdTipoOperacionNuevo;
					o.IdMonedaPrincipal = u.IdMonedaPrincipal;
					o.MontoOperacion = u.MontoOperacionNuevo;
					o.FechaVencimiento = new java.sql.Timestamp(u.FechaVencimientoNuevo.getTime());
					o.FechaLiquidacion = new java.sql.Timestamp(u.FechaPagoNuevo.getTime());
					o.PlazoDias = u.PlazoNuevo;
					o.Origen = _cl.ORIGEN_ENTRADA;
					return o;
				})
				.collect(Collectors.toList());
		
		ValidarLimitesCuentaPropiaForward.SalidaValidarLimitesCuentaPropiaForward salidaValidarLimitesCuentaPropiaForward = new ValidarLimitesCuentaPropiaForward().logica(conn, entradaValidarLimitescuentaPropiaForward);
		
		List<MensajeSalida> mensajes = salidaValidarLimitesCuentaPropiaForward.P_OUT_MENSAJES;
		for (MensajeSalida mensaje : mensajes) {
			mensaje.tipo = MensajeSalida.MENSAJE_ADVERTENCIA;
		}
		
		long fin = System.currentTimeMillis();
		log.write("FIN::AnticiparOperacionForward.validarLimitesCuentaPropia {}", fin-inicio);
		return mensajes;
		
	}
	
	private TesoreriaResultado generarMovimientoTesoreria(Connection conn, List<ForwardSinSpot> operacionesForward, List<ForwardFomaPago> operacionesForwardFormaDePago, Long idEmpresa, Long idUsuario, String idModulo, String fechaSistema) throws Exception {
		log.write("INICIO::AnticiparOperacionForward.generarMovimientoTesoreria");
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
				ing.IdConcepto = "ANTFWD";
				ing.IdTipoPersonaTitularPago = "CLI";
				ing.IdPersonaTitularPago = forward.IdPersona;
				ing.IdPortafolioTitularPago = forward.IdPortafolio;
				ing.IdCaja = 0L;
				ing.SecuenciaAgrupacion = "";
				ing.SecuenciaRelacionada = forward.IdOperacion;
				ing.Glosa = "ANTICIPO FORWARD";
				ing.IdEmpleadoDigitador = forward.IdEmpleadoEjecutivo;
				ing.IdSucursalEmpleadoDigitador = forward.IdSucursalEmpleadoEjecutivo;
				ing.IdTipoProducto = forward.IdTipoProducto;
				ing.IdTipoOperacion = forward.IdTipoOperacion;
				ing.IdTipoComprobante = "";
				ing.IdComprobante = 0L; //cte.IdComprobante;
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
		log.write("FIN::AnticiparOperacionForward.generarMovimientoTesoreria", fin-inicio);
		return salida;
	}
	
	private CargoAbonoResultado generaCargoAbono(Connection conn, List<ForwardSinSpot> operacionesForward, List<ForwardFomaPago> operacionesForwardFormaDePago, Long idEmpresa, Long idUsuario, String idModulo, String fechaSistema) throws Exception {
		log.write("INICIO::AnticiparOperacionForward.generaCargoAbono");
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
				cargoAbono.IdConcepto = "ANTFWD";
				cargoAbono.Descripcion = "ANTICIPO FORWARD";
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
		log.write("FIN::AnticiparOperacionForward.generaCargoAbono", fin-inicio);
		return salida;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String jsonDesaAntParc = "{\"p_IdEmpresa\":1,\"p_IdPais\":44,\"p_IdUsuario\":21,\"p_IdBolsa\":\"BCS\",\"p_IdSucursal\":0,\"p_IdModulo\":\"MFW\",\"p_UsaReglasNegocio\":\"N\",\"p_tablaForward\":[{\"FolioInterno\":1776,\"FolioCartera\":20685457,\"IdTipoOperacion\":\"ANTFWD\",\"IdTipoInstrumento\":\"FWD\",\"IdTipoProducto\":\"MEFW\",\"IdInstrumento\":346370,\"Nemotecnico\":\"SECANO\",\"IdCliente\":395174,\"IdPortafolio\":343710,\"IdPersonaEjecutivo\":621869,\"IdTipoCanalIngreso\":\"MES\",\"IdMonedaPrincipal\":\"USD\",\"IdMonedaSecundaria\":\"CLP\",\"MontoOperacion\":23000,\"MontoInicial\":13800000,\"MontoFinal\":0,\"FechaOperacion\":\"2018-07-31\",\"TCCierre\":610,\"ParidadCierre\":0,\"Plazo\":1,\"FechaVencimiento\":\"2018-07-31\",\"FechaPago\":\"2018-08-01\",\"IdModalidadLiquidacion\":6,\"IdReferenciaMercado\":0,\"IdPlaza\":2931,\"TCCosto\":600,\"PuntosTasasForward\":\"S\",\"PuntosForward\":0,\"TCForward\":600,\"TasasMonedaPrincipal\":0,\"TasasMonedaSecundaria\":0,\"MontoDiferido\":230000,\"MontoMargen\":13800000,\"IdFixing\":37,\"FechaFixing\":\"0001-01-01\",\"HoraFixing\":\"00:00:00\",\"IdMesa\":2,\"IdSpotAsociado\":0,\"IdClasificacionCartera\":11,\"Observaciones\":\"\",\"IdEstado\":\"ANT\",\"TasaDescuento\":0,\"TipoCambioAnticipo\":640,\"MontoARecibirAnticipo\":690000,\"TipoCambioObservado\":645,\"MontoPrincipalAnticipo\":23000}]}";
			AnticiparOperacionForwardEntrada Objin = gson.fromJson(jsonDesaAntParc, AnticiparOperacionForwardEntrada.class);
			
			AnticiparOperacionForward demo = new AnticiparOperacionForward();
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			try {
 				AnticiparOperacionForwardSalida ObjOut = demo.logica(con, Objin);
 				//con.commit();
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