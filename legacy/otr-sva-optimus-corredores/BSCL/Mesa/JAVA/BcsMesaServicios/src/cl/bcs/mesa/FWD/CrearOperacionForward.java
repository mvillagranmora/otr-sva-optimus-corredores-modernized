package cl.bcs.mesa.FWD;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import cl.bcs.plataforma.lim.pojo.Validacion;
import org.apache.commons.lang3.tuple.Pair;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.IngresoReserva;
import cl.bcs.custodia.IngresoReserva.IngresoReserva_IN;
import cl.bcs.custodia.IngresoReserva.IngresoReserva_OUT;
import cl.bcs.custodia.IngresoReserva.ReservaObj_IN;
import cl.bcs.mesa.FWD.IngresarForwardCartera.IngresarForwardCarteraEntrada;
import cl.bcs.mesa.FWD.IngresarForwardCartera.IngresarForwardCarteraSalida;
import cl.bcs.mesa.FWD.IngresarForwardCartera.TablaForwardObjIn;
import cl.bcs.mesa.FWD.IngresarForwardCartera.TablaForwardObjOut;
import cl.bcs.mesa.MES.CambiarEstadoOperacion;
import cl.bcs.mesa.MES.CambiarEstadoOperacion.CambiarEstadoOperacionEntrada;
import cl.bcs.mesa.MES.CambiarEstadoOperacion.CambiarEstadoOperacionSalida;
import cl.bcs.mesa.MES.CambiarEstadoOperacion.ObjCambiarEstadoOperacionIN;
import cl.bcs.mesa.MES.CrearComprobantePagoOperacion;
import cl.bcs.mesa.MES.CrearComprobantePagoOperacion.ComprobantePagoOperacionObj_IN;
import cl.bcs.mesa.MES.CrearComprobantePagoOperacion.CrearComprobantePagoOperacion_IN;
import cl.bcs.mesa.MES.CrearComprobantePagoOperacion.CrearComprobantePagoOperacion_OUT;
import cl.bcs.mesa.MES.IngresoOperacion;
import cl.bcs.mesa.MES.IngresoOperacion.IngresoOperacion_IN;
import cl.bcs.mesa.MES.IngresoOperacion.IngresoOperacion_OUT;
import cl.bcs.mesa.MES.IngresoOperacion.OperacionObjIn;
import cl.bcs.mesa.MES.IngresoOperacion.OperacionObjOut;
import cl.bcs.mesa.utils.ConsultarConfiguracionMesa;
import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo;
import cl.bcs.plataforma.lim.pojo.ConstantesLimites;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.AgregarMensajeWebsocket;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente.FechasOut;
import cl.bcs.plataforma.pla.ObtenerDiaSiguienteMultiplesPaises;
import cl.bcs.plataforma.pla.ObtenerDiaSiguienteMultiplesPaises.InObtenerDiaSiguienteMultiplesPaises;
import cl.bcs.plataforma.pla.ObtenerDiaSiguienteMultiplesPaises.OutObtenerDiaSiguienteMultiplesPaises;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.soc.SocioNegocioUtils;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearOperacionForward implements JavaCall{
	
	private final Log log = new Log("FWD.CrearOperacionForward");
	
	private final String SQL_INSERT_OPERACIONES_FORWARD_WRAPPER = loadResourceToString("cl/bcs/mesa/FWD/sql/CrearOperacionForward_InsertOperacionesForwardWrapper.sql");
	private final String SQL_INSERT_OPERACIONES_FORWARD_CALCE_WRAPPER = loadResourceToString("cl/bcs/mesa/FWD/sql/CrearOperacionForward_InsertOperacionesForwardCalceWrapper.sql");
	private final String SQL_SELECT_REQUISITOS_CREAR_OPERACION_FORWARD = loadResourceToString("cl/bcs/mesa/FWD/sql/CrearOperacionForward_ConsultarRequisitosCrearOperacionForward.sql");
	private final String SQL_SELECT_VENCIMIENTO_CONSULTAR_PARAMETROS = loadResourceToString("cl/bcs/mesa/FWD/sql/CrearOperacionForward_ConsultarParametrosMesaForward.sql");
	private final String SQL_SELECT_FORMAS_DE_PAGO_MESA = loadResourceToString("cl/bcs/mesa/sql/ConsultarFormasDePagoMesa.sql");
	private final String SQL_SELECT_MODALIDAD_DE_PAGO = "SELECT \"IdMedioDePagoYModalidadDePago\",\"IdMedioDePago\",\"IdModalidadDePago\",\"IdEstado\" FROM  \"BSCL.Plataforma.BD::PAG.MedioDePagoYModalidadDePago\" WHERE \"IdEstado\" = 'ACT'";
	private final String SQL_SELECT_OPERACIONES_FORWARD_CALCE = loadResourceToString("cl/bcs/mesa/FWD/sql/CrearOperacionForward_ConsultarOperacionesForwardCalce.sql");
	private final String SQL_UPDATE_FORMA_DE_PAGO_ID_OPERACION = loadResourceToString("cl/bcs/mesa/FWD/sql/CrearOperacionForward_UpdateFormaDePagoIdOperacion.sql");
	private final String SQL_SELECT_CONSULTAR_MONEDAS_FERIADO_MONEDA_PRINCIPAL = loadResourceToString("cl/bcs/mesa/SPT/sql/ConsultarMonedasFeriadoMonedaPrincipal.sql");
	private final String SQL_SELECT_PARAMETRO_MESA = loadResourceToString("cl/bcs/mesa/FWD/sql/CrearOperacionForward_ConsultarParametroMesa.sql");
	
	private final ConstantesLimites _cl = new ConstantesLimites();
	
	public static class OperacionForwardObjIn{
		public Long FolioInterno;
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
		public Long IdClasificacionCartera;
		public String Observaciones;
		public String IdEstado;
		public Long IdContacto;
		public String TipoDocumento;
		public String PagaConCtaInv;
		public String FechaSuscripcion = "";
		public String HoraSuscripcion  = "";
		public String IdMonedaCompensacion = "";
		public Long IdEmpleadoDigitador = 0L;
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class FormaPagoObjIn{
		public Long IdInternoPagoOperacion;
		public Long IdComprobantePagoOperacion;
		public String IdTipoMovimiento;
		public String IdFormaDePago;
		public String IdMoneda;
		public String IdMonedaLiquidacion;
		public Long IdOperacion;
		public String RecibePagaPortafolio;
		public BigDecimal MontoFormaDePago;
		public String AfectarTesoreria;
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class CalceObjIn{
		public Long FolioInterno;
		public Long IdOperacion;
		public Long FolioCartera;
		public String IdTipoOperacion;
		public String IdMonedaPrincipal;
		public BigDecimal MontoOperacion;
		public BigDecimal MontoDisponible;
		public BigDecimal MontoCalce;
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class CrearOperacionForwardEntrada {
		public Long p_IdEmpresa;
		public Long p_IdPais;
		public Long p_IdUsuario;
		public String p_IdBolsa;
		public Long p_IdSucursal;
		public String PagaConCtaInv;	//			NVARCHAR(1) 'N' o 'S' DEFAULT 'N'
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public List<OperacionForwardObjIn> p_tablaForward;
		public List<FormaPagoObjIn> p_DatosFormaPago;
		public List<CalceObjIn> p_tablaCalce;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class OperacionForwardObjOut {
		public Long FolioInterno;
		public Long IdCartera;
		public Long IdOperacion;
		public Long FolioCartera;
		public String IdEstado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CrearOperacionForwardSalida {
		public List<MensajeSalida>		P_OUT_MENSAJES;
		public Long						P_OUT_ERRORESCONTADOR;
		public Long						P_OUT_ADVERTENCIASCONTADOR;
		public List<OperacionForwardObjOut>	P_OUT_TBLIDFORWARD;
		
		public CrearOperacionForwardSalida() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_TBLIDFORWARD = new ArrayList<OperacionForwardObjOut>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class OperacionForward{
		public Long FolioInterno;
		public Long IdOperacion;
		public Long FolioCartera;
		public String IdTipoOperacion;
		public String IdTipoInstrumento;
		public String IdClasificacionTipoInstrumento;
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
		public BigDecimal PrecioVencimiento;
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
		public String Observaciones;
		public String IdEstado;
		public BigDecimal TasaDescuento;
		public BigDecimal TipoCambioAnticipo;
		public BigDecimal MontoARecibirAnticipo;
		public BigDecimal TipoCambioObservado;
		public Long IdContacto;
		public String TipoDocumento;
		
		public Long idInterno;
		public String CodigoOperador;
		
		public String toString(){
			return Utils.printJson(this, false);
		}
	}
		
	public static class RequisitosCrearOperacionForward{
		public String FolioInterno;
		public Long IdMesa;
		public String IdEstadoMesa;
		public BigDecimal MontoOperacion;
		public BigDecimal MontoInicial;
		public BigDecimal MontoFinal;
		public String IdTipoInstrumento;
		public String Nemotecnico;
		public Date FechaOperacion;
		public String IdTipoOperacion;
		public Date FechaVencimiento;
		public Long Plazo;
		public String IdEstadoCuentaInversionMonedaPrincipal;
		public String ValorIndicadorFirmaCCGFWD;
		public String IdEstadoModulo;
		public String TipoEstadoModulo;
		public String ResultadoPerfilProducto;
		public String MensajePerfilProducto;
		public Long IdCliente;
		public String IdEstadoCliente;
		public Long IdPortafolio;
		public String IdEstadoPortafolio;
		public BigDecimal TCCosto;
		public BigDecimal TCCierre;
		public BigDecimal ParidadCierre;
		public BigDecimal TipoCambioUF;
		public String DocumentoIdentidad;
		public String CodigoPortafolio;
		public String PlantillaEmpleadoProductoHabilitado;
		
		public String toString(){
			return Utils.printJson(this, false);
		}
	}
	
	public static class FormaDePagoMesa {
		public String IdFormaDePago;
	    public String NombreFormaDePago;
	    public Long	Dias;
	    public String PorDefecto;
	    public String RecibePagaPortafolioDesc;
	    public String IdEstado;
	    public String NombreEstado;
	    public String IdPreferenciaPago;
	    public String IdTipoOperacion;
	    public String IdMedioDePago;
	    public String IdMoneda;
	    public String UsaPersonaRelacionada;
	    public Date FechaCreacion;
	    public Date FechaModificacion;
	    public Long IdUsuarioCreador;
	    public Long IdEmpresa;
	    public String NombreTipoOperacion;
	    public String IdTipoProducto;
	    public String NombreTipoProducto;
	    public String RecibePagaPortafolio;
	    public String NombreMedioDePago;
	    public String NombreMoneda;
	    public String UsaCuentaBancaria;
	    public String UsaTarjetaBancaria;
	    public String IdEstadoPreferenciaPago;
	    public String NombreEstadoPreferenciaPago;
	    
	    public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class ModalidadDePago {
		public Long IdMedioDePagoYModalidadDePago;
		public String IdMedioDePago;
		public String IdModalidadDePago;
		public String IdEstado;
	}
	
	public static class EventoAutorizacion{
		public Long NumeroOperacion;
		public String IdTipoEvento;
		public String Glosa;
	}
	
	public static class CalceForwardCartera {
		public Long FolioInterno;
		public Long IdCartera;
		public Long IdOperacion;
		public Long FolioCartera;
		public Long IdOperacionCartera;
		public String IdTipoOperacion;
		public String IdMonedaPrincipal;
		public BigDecimal MontoOperacion;
		public BigDecimal MontoDisponible;
		public BigDecimal MontoCalce;
		public String IdClasificacionTipoInstrumento;
		public String IdTipoInstrumento;
		public Long IdInstrumento;
		public String Nemotecnico;
		public Date FechaLiquidacion;
	}
	
	public static class ParametrosForward {
		public String CodigoParametro;
		public String Valor;
		
		public String toString() {
			return Utils.printJson(this, false);
		};		
	}
	
	public static class ParametroMesa extends BcsPojo{
		public String ValorParametro;
		public Long IdParametro;
		
		public ParametroMesa() {
			this.IdParametro = 0L;
			this.ValorParametro = "";
		}
	}
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		CrearOperacionForwardEntrada in = gson.fromJson(jin.toString(), CrearOperacionForwardEntrada.class);
		CrearOperacionForwardSalida out = logica(con, in);
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
	
	public CrearOperacionForwardSalida logica(Connection conn, CrearOperacionForwardEntrada in) throws Exception {
		SQL _sql = new SQL();
		CrearOperacionForwardSalida out = new CrearOperacionForwardSalida();
		out.P_OUT_ERRORESCONTADOR = 0L;
		out.P_OUT_ADVERTENCIASCONTADOR = 0L;
		Long Id = 0L;
		Long errCont = 0L;
		HashMap<String,String> mapMsg = new HashMap<>();
		Set<String> codigosRechazoLimiteCtaPropia = new HashSet<>();
		
		log.write("INICIO :: FWD.CrearOperacionForward");
		
		try {
			if (in.p_tablaForward.size() == 0) {
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_INGRESARON_OPERACIONES",mapMsg,conn,in.p_IdEmpresa));
				Id++;
				return out;
			}
			
			RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
			RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(conn, in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
			if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
				out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
				return out;
			}
			
			SocioNegocioUtils.Empleado empleadoUsuario = SocioNegocioUtils.obtenerEmpleadoUsuario(conn, in.p_IdEmpresa, in.p_IdUsuario).orElse(null);
			if (empleadoUsuario == null) {
				out.P_OUT_ERRORESCONTADOR++;
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_USUARIO_NO_ES_EMPLEADO", new HashMap<>(), conn, in.p_IdEmpresa));
				return out;
			}
			
			// obtener dias minimo de vencimiento forward
			Long diasMinimoVencimiento = 0L;
			List<MensajeSalida> mensajeVencimientoForward = new ArrayList<PlataformaUtils.MensajeSalida>();
			
			HashMap<String,Object> paramsConsultarParametros = new HashMap<>();
			List<String> codigoParametros = new ArrayList<String>(2);
			codigoParametros.add("PLAZOVENCIMIENTOMINIMO");
			paramsConsultarParametros.put("CodigoParametros", codigoParametros);
			paramsConsultarParametros.put("IdEmpresa", in.p_IdEmpresa);
			List<ParametrosForward> valoresCodigoParametros = _sql.query(conn, SQL_SELECT_VENCIMIENTO_CONSULTAR_PARAMETROS, paramsConsultarParametros, ParametrosForward.class);
			
			HashMap<String,Object> paramsConsultarParametroModalidadCompensada = new HashMap<>(3);
			paramsConsultarParametroModalidadCompensada.put("NombreTipoParametro", "ModalidadLiquidacion");
			paramsConsultarParametroModalidadCompensada.put("ValorParametro", "COMPENSADA");
			paramsConsultarParametroModalidadCompensada.put("IdEmpresa", in.p_IdEmpresa);
			List<ParametroMesa> valorModalidadCompensada = _sql.query(conn, SQL_SELECT_PARAMETRO_MESA, paramsConsultarParametroModalidadCompensada, ParametroMesa.class);
			
			if (!valoresCodigoParametros.isEmpty()) {
				for(ParametrosForward parametro: valoresCodigoParametros) {
					if (parametro.CodigoParametro.equals("PLAZOVENCIMIENTOMINIMO") && !parametro.Valor.equals("")) {
						try {
							diasMinimoVencimiento = Long.parseLong(parametro.Valor);
							
						} catch (Exception e) {		// valida si el parametro es un numero valido
							String mensajeError= Diccionario.getMensaje(conn,in.p_IdEmpresa,"UI_PARAMETRO_NUMERICO_INVALIDO")
															.add("NombreParametro", "PLAZOVENCIMIENTOMINIMO")
									  						.msg();
							mensajeVencimientoForward.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_PARAMETRO_NUMERICO_INVALIDO", mensajeError));
							out.P_OUT_MENSAJES.addAll(mensajeVencimientoForward);
							out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
							return out;							
						}
					}
				}
			}
			
			/***** VARIABLES *****/
			String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn,in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
			String fechaHoraSistemaSecondate = FechaDB.ObtenerFechaHoraSistemaLocal(conn,in.p_IdEmpresa);
			
			Long cantOpeIn = 0L;
			Long cantOpeVig = 0L;
			Long cantOpePen = 0L;
			
			HashMap<String,Object> params = new HashMap<>();
			List<OperacionForward> operacionesForward = new ArrayList<>();
			
			List<Long> folioInterno = new ArrayList<Long>();
			HashMap<Long,OperacionForwardObjIn> operacionForwardHash = new HashMap<Long,OperacionForwardObjIn>();
			List<String> IdTipoProducto = new ArrayList<String>();
			List<OperacionObjIn> operacionMesa = new ArrayList<>();
			List<EventoAutorizacion> evtList = new ArrayList();
			
			for (OperacionForwardObjIn ope: in.p_tablaForward){
				// validar fecha minima de vencimiento segun parametro PLAZOVENCIMIENTOMINIMO
				Date fechaMinimaVencimiento = Fecha.toDate(obtenerDiaSiguiente(conn, ope.FechaOperacion.toString(), diasMinimoVencimiento, in.p_IdEmpresa));
				if (ope.FechaVencimiento.compareTo(fechaMinimaVencimiento)==-1) {
					String mensajeError= Diccionario.getMensaje(conn,in.p_IdEmpresa,"PLAZO_MINIMO_VENCIMIENTO_FORWARD")
							  						.add("CantidadDias", diasMinimoVencimiento)
							  						.msg();
					mensajeVencimientoForward.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "PLAZO_MINIMO_VENCIMIENTO_FORWARD", mensajeError));
					out.P_OUT_MENSAJES.addAll(mensajeVencimientoForward);
					out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
					return out;
				}
				
				if(!valorModalidadCompensada.isEmpty() && valorModalidadCompensada.get(0).IdParametro.equals(ope.IdModalidadLiquidacion) && ope.IdMonedaCompensacion.isEmpty()) {
					ope.IdMonedaCompensacion = "CLP";
				}
				
				folioInterno.add(ope.FolioInterno);
				cantOpeIn++;
				
				operacionForwardHash.put(ope.FolioInterno, ope);
				IdTipoProducto.add(ope.IdTipoProducto);		
				
				OperacionObjIn opeMes = new OperacionObjIn();
				Utils.copyPojo(ope, opeMes);
				opeMes.Accion = "CREAR";
				opeMes.IdInternoOperacion = ope.FolioInterno;
				opeMes.IdOperacion = 0L;
				opeMes.FolioOperacion = 0L;
				opeMes.FechaOperacion = fechaHoraSistemaSecondate;
				opeMes.IdTipoProducto = ope.IdTipoProducto;
				opeMes.IdTipoOperacion = ope.IdTipoOperacion;
				opeMes.IdOperacionRelacionada = 0L;
				opeMes.IdInstrumento = ope.IdInstrumento;
				opeMes.IdMonedaPrincipal = ope.IdMonedaPrincipal;
				opeMes.IdMonedaSecundaria = ope.IdMonedaSecundaria;
				opeMes.IdModalidadLiquidacion = ope.IdModalidadLiquidacion;
				opeMes.IdReferenciaMercado = ope.IdReferenciaMercado;
				opeMes.IdPlaza = ope.IdPlaza;
				opeMes.Intermesa = "N";
				opeMes.IdMesaOrigen = ope.IdMesa;
				opeMes.IdMesaDestino = ope.IdMesa;
				opeMes.IdCliente = ope.IdCliente;
				opeMes.IdPortafolio = ope.IdPortafolio;
				opeMes.IdClienteCorredor = 0L;
				opeMes.IdPortafolioCorredor = 0L;
				opeMes.IdTipoReajuste = "";
				opeMes.MontoOperacion = ope.MontoOperacion;
				opeMes.MontoOperacionInicial = ope.MontoInicial;
				opeMes.MontoOperacionFinal = ope.MontoFinal;
				opeMes.IdMoneda = "";
				opeMes.Tasa = BigDecimal.ZERO;
				opeMes.MontoOperacionMoneda = BigDecimal.ZERO;
				opeMes.MontoOperacionInicialMoneda = BigDecimal.ZERO;
				opeMes.MontoOperacionFinalMoneda = BigDecimal.ZERO;
				opeMes.IdMonedaLiquidacion = "";
				opeMes.TCCierre = ope.TCCierre;
				opeMes.ParidadCierre = ope.ParidadCierre;
				opeMes.TCCosto = ope.TCCosto;
				opeMes.PuntosTasasForward = ope.PuntosTasasForward;
				opeMes.PuntosForward = ope.PuntosForward;
				opeMes.TCForward = ope.TCForward;
				opeMes.TasasMonedaPrincipal = ope.TasasMonedaPrincipal;
				opeMes.TasasMonedaSecundaria = ope.TasasMonedaSecundaria;
				opeMes.MontoDiferido = ope.MontoDiferido;
				opeMes.MontoMargen = ope.MontoMargen;
				opeMes.IdFixing = ope.IdFixing;
				opeMes.FechaFixing = ope.FechaFixing;
				opeMes.HoraFixing = ope.HoraFixing;
				opeMes.IdSpotAsociado = ope.IdSpotAsociado;
				opeMes.IdClasificacionCartera = ope.IdClasificacionCartera;
				opeMes.AfectaCustodia = "";
				opeMes.PlazoEnDias = ope.Plazo;
				opeMes.FechaVencimiento = ope.FechaVencimiento;
				opeMes.FechaLiquidacion = ope.FechaPago;
				opeMes.IdCondicionLiquidacion = "";
				opeMes.FechaCondicionLiquidacion = ope.FechaPago;
				opeMes.IdAgente = ope.IdPersonaEjecutivo;
				opeMes.TasaAgente = BigDecimal.ZERO;
				opeMes.IdEstado = ope.IdEstado;
				opeMes.IdUsuarioCreacion = in.p_IdUsuario;
				opeMes.IdTipoCanalIngreso = ope.IdTipoCanalIngreso;
				opeMes.Glosa = ope.Observaciones;
				opeMes.IdEmpresa = in.p_IdEmpresa;
				opeMes.AccionEdicion = "L";
				opeMes.UsuarioEdicion = in.p_IdUsuario;
				opeMes.IdEmisor = "";
				opeMes.IdContacto = ope.IdContacto;
				opeMes.TipoDocumento = ope.TipoDocumento;
				opeMes.FechaSuscripcion = ope.FechaSuscripcion;
				opeMes.HoraSuscripcion = ope.HoraSuscripcion;
				opeMes.IdMonedaCompensacion = (!valorModalidadCompensada.isEmpty() && valorModalidadCompensada.get(0).IdParametro.equals(ope.IdModalidadLiquidacion) && ope.IdMonedaCompensacion.isEmpty()) ? "CLP" : ope.IdMonedaCompensacion;
				opeMes.IdEmpleadoDigitador = ope.IdEmpleadoDigitador != 0L ? ope.IdEmpleadoDigitador : empleadoUsuario.IdEmpleado;
				operacionMesa.add(opeMes);
			}
			
			
			//Formas de pago Mesa
			HashMap<String, Object> selectFormaPagoMesaIN = new HashMap<>();
			selectFormaPagoMesaIN.put("IdTipoProducto" , IdTipoProducto);
			//selectFormaPagoMesaIN.put("IdTipoOperacion", IdTipoOperacion);
			List<FormaDePagoMesa> formaDePagoMesaList = _sql.query(conn, SQL_SELECT_FORMAS_DE_PAGO_MESA, selectFormaPagoMesaIN, FormaDePagoMesa.class);
			HashMap<String, Object> formaDePagoMesaHash = Utils.List2Hash(formaDePagoMesaList, "IdFormaDePago");
			
			//Modalidad de pago
			List<ModalidadDePago> modalidadPago = _sql.query(conn, SQL_SELECT_MODALIDAD_DE_PAGO, new HashMap<>(), ModalidadDePago.class);
			HashMap<String, Object> modalidadPagoHash = Utils.List2Hash(modalidadPago, "IdMedioDePago");
			
			
			//Insert tablas globales
			log.write("INSERT en tabla BSCL.Mesa.BD::FWD.CrearOperacionForward_Proceso :: "+in.p_tablaForward);
			_sql.batch(conn, SQL_INSERT_OPERACIONES_FORWARD_WRAPPER, in.p_tablaForward, OperacionForwardObjIn.class);
			
			log.write("INSERT en tabla BSCL.Mesa.BD::FWD.CrearOperacionForward_ProcesoCalce :: "+in.p_tablaCalce);
			_sql.batch(conn, SQL_INSERT_OPERACIONES_FORWARD_CALCE_WRAPPER, in.p_tablaCalce, CalceObjIn.class);
			
			
			/***** VALIDACIONES *****/
			params.put("IdEmpresa", in.p_IdEmpresa);
			params.put("FolioInterno", folioInterno);
			params.put("IdUsuario", in.p_IdUsuario);
			
			if(in.p_UsaReglasNegocio.equals("S")){
				List<RequisitosCrearOperacionForward> requisitosCrearOperacionForward = _sql.query(conn, SQL_SELECT_REQUISITOS_CREAR_OPERACION_FORWARD, params, RequisitosCrearOperacionForward.class);
				String validaPlantillaEmpleado = "N";
				try{ validaPlantillaEmpleado = new ConsultarConfiguracionMesa().logica(conn, in.p_IdEmpresa, "MEFW", "VALIDA_PLANTILLA_EMPLEADO").Valor;} catch(Exception er) {};
				
				for (RequisitosCrearOperacionForward req: requisitosCrearOperacionForward){
					if(!req.IdEstadoMesa.equals("A")){
						mapMsg.put("FolioInterno", req.FolioInterno);
						out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"MESA_FORWARD_CERRADA",mapMsg,conn,in.p_IdEmpresa));
						Id++;
					}
					if(req.MontoOperacion.compareTo(BigDecimal.ZERO) <= 0){
						mapMsg.put("FolioInterno", req.FolioInterno);
						out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"MONTO_MAYOR_CERO",mapMsg,conn,in.p_IdEmpresa));
						Id++;
					}
					if(req.Nemotecnico.equals("SECARE") || req.Nemotecnico.equals("SEINFL")){
						if(req.MontoInicial.compareTo(BigDecimal.ZERO) <= 0){
							mapMsg.put("FolioInterno", req.FolioInterno);
							out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"MONTO_INICIAL_MAYOR_CERO",mapMsg,conn,in.p_IdEmpresa));
							Id++;
						}
						if(req.MontoFinal.compareTo(BigDecimal.ZERO) <= 0){
							mapMsg.put("FolioInterno", req.FolioInterno);
							out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"MONTO_FINAL_MAYOR_CERO",mapMsg,conn,in.p_IdEmpresa));
							Id++;
						}
					}
					if(!Fecha.toStringDate(req.FechaOperacion).equals(fechaSistema) && (req.IdTipoOperacion.equals("COMFWD") || req.IdTipoOperacion.equals("VENFWD"))){
						mapMsg.put("FolioInterno", req.FolioInterno);
						out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"FECHA_OPERACION_DISTINTA_FECHA_PROCESO",mapMsg,conn,in.p_IdEmpresa));
						Id++;
					}
					if(Fecha.toStringDate(req.FechaVencimiento).equals(fechaSistema) && !req.IdTipoOperacion.equals("VCDFWD")){
						mapMsg.put("FolioInterno", req.FolioInterno);
						out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"FECHA_VENCIMIENTO_DISTINTO_OPERACION",mapMsg,conn,in.p_IdEmpresa));
						Id++;
					}
					if(req.Plazo <= 0){
						mapMsg.put("FolioInterno", req.FolioInterno);
						out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"PLAZO_MAYOR_CERO",mapMsg,conn,in.p_IdEmpresa));
						Id++;
					}
					if(!req.IdEstadoCuentaInversionMonedaPrincipal.equals("ACT") && !req.Nemotecnico.equals("SEINFL")){
						mapMsg.put("FolioInterno", req.FolioInterno);
						out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"MONEDA_NO_HABILITADA_CUENTA_INVERSION",mapMsg,conn,in.p_IdEmpresa));
						Id++;
					}
					if(!req.ValorIndicadorFirmaCCGFWD.equals("S")){
						mapMsg.put("FolioInterno", req.FolioInterno);
						out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"FIRMA_CCG_FWD_CLIENTE",mapMsg,conn,in.p_IdEmpresa));
						Id++;
					}
					if(!(req.IdEstadoModulo.equals("ACT") && req.TipoEstadoModulo.equals("A"))){
						mapMsg.put("FolioInterno", req.FolioInterno);
						out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"UI_MODULO_CERRADO",mapMsg,conn,in.p_IdEmpresa));
						Id++;
					}
					if(req.ResultadoPerfilProducto.equals("NOK")){
						mapMsg.put("CodigoPortafolio", req.CodigoPortafolio);
						mapMsg.put("DocumentoIdentidad", req.DocumentoIdentidad);
						out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_PLANTILLA_PRODUCTO",mapMsg,conn,in.p_IdEmpresa));
						Id++;
					}
					if(!(req.IdCliente > 0L && req.IdEstadoCliente.equals("ACT"))){
						String msjError = "";
						switch(req.IdEstadoCliente){
							case "BLO": msjError = "CLIENTE_BLOQUEADO"; break;
							case "PEN": msjError = "CLIENTE_PENDIENTE_AUTORIZACION"; break;
							default : msjError = "CLIENTE_NO_HABILITADO"; break;
						}
						mapMsg.put("DocumentoIdentidad", req.DocumentoIdentidad);
						out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,msjError,mapMsg,conn,in.p_IdEmpresa));
						Id++;
					}
					if(!(req.IdPortafolio > 0L && req.IdEstadoPortafolio.equals("ACT"))){
						String msjError = "";
						switch(req.IdEstadoCliente){
							case "BLO": msjError = "PORTAFOLIO_BLOQUEADO"; break;
							case "PEN": msjError = "PORTAFOLIO_PENDIENTE_AUTORIZACION"; break;
							default : msjError = "PORTAFOLIO_NO_HABILITADO"; break;
						}
						mapMsg.put("CodigoPortafolio", req.CodigoPortafolio);
						mapMsg.put("DocumentoIdentidad", req.DocumentoIdentidad);
						out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,msjError,mapMsg,conn,in.p_IdEmpresa));
						Id++;
					}
					if(req.TCCosto.compareTo(BigDecimal.ZERO) <= 0 && req.IdTipoInstrumento.equals("FWD") && !req.Nemotecnico.equals("ARBITR")){
						mapMsg.put("FolioInterno", req.FolioInterno);
						out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"TIPO_COSTO_MAYOR_CERO",mapMsg,conn,in.p_IdEmpresa));
						Id++;
					}
					if(req.TCCosto.compareTo(BigDecimal.ZERO) <= 0 && req.IdTipoInstrumento.equals("FWD") && !req.Nemotecnico.equals("ARBITR")){
						mapMsg.put("FolioInterno", req.FolioInterno);
						out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"TIPO_COSTO_CIERRE_DENTRO_RANGO_DEFINIDO",mapMsg,conn,in.p_IdEmpresa));
						Id++;
					}
					if(req.TCCierre.compareTo(BigDecimal.ZERO) <= 0 && !req.Nemotecnico.equals("ARBITR")){
						mapMsg.put("FolioInterno", req.FolioInterno);
						out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"TC_CIERRE_MAYOR_CERO",mapMsg,conn,in.p_IdEmpresa));
						Id++;
					}
					if(req.ParidadCierre.compareTo(BigDecimal.ZERO) <= 0 && req.Nemotecnico.equals("ARBITR")){
						mapMsg.put("FolioInterno", req.FolioInterno);
						out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"PARIDAD_CIERRE_MAYOR_CERO",mapMsg,conn,in.p_IdEmpresa));
						Id++;
					}
					if(req.TipoCambioUF.compareTo(BigDecimal.ZERO) <= 0 && req.Nemotecnico.equals("SECARE")){
						mapMsg.put("FolioInterno", req.FolioInterno);
						out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"SIN_VALOR_TIPO_CAMBIO_UF_SECARE",mapMsg,conn,in.p_IdEmpresa));
						Id++;
					}
					if("S".equals(validaPlantillaEmpleado) && "N".equals(req.PlantillaEmpleadoProductoHabilitado)){
						out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"PRODUCTO_NO_HABILITADO_EMPLEADO",mapMsg,conn,in.p_IdEmpresa));
						Id++;
					}
				}
			}
			
			
			//Retorna si existen errores 
			errCont = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
			if (!errCont.equals(0L) ){
				return out;
			}
			
			
			/***** INGRESO OPERACION MESA *****/
			IngresoOperacion ingresoOperacion = new IngresoOperacion();
			IngresoOperacion_IN ingresoOperacionIn = new IngresoOperacion_IN();
			IngresoOperacion_OUT ingresoOperacionOut = new IngresoOperacion_OUT();
			for(FormaPagoObjIn formaPago: in.p_DatosFormaPago) {
				if(formaPago.IdFormaDePago.indexOf("_CIN_") > 0) {
					in.PagaConCtaInv = "S";
					break;
				}else {
					in.PagaConCtaInv = "N";
				} 
			}
			ingresoOperacionIn.p_IdEmpresa = in.p_IdEmpresa;
			ingresoOperacionIn.p_IdUsuario = in.p_IdUsuario;
			ingresoOperacionIn.p_IdModulo = in.p_IdModulo;
			ingresoOperacionIn.p_simularOperacion = "N"; 
			ingresoOperacionIn.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
			ingresoOperacionIn.p_IdPais = in.p_IdPais;
			ingresoOperacionIn.p_Operaciones = operacionMesa;
			ingresoOperacionIn.PagaConCtaInv = in.PagaConCtaInv;
			ingresoOperacionOut = ingresoOperacion.logica(conn, ingresoOperacionIn);
			out.P_OUT_MENSAJES.addAll(ingresoOperacionOut.P_OUT_MENSAJES);
			if (MensajeSalida.getNumeroErrores(ingresoOperacionOut.P_OUT_MENSAJES) > 0){
				return out;
			}
			
			//OperacionForward
			for(OperacionForwardObjIn opeFwdIn: in.p_tablaForward){
				for(OperacionObjOut opeMesOut: ingresoOperacionOut.P_OUT_OPERACION){
					if(opeFwdIn.FolioInterno.longValue() == opeMesOut.IdAplicacion.longValue()){
						folioInterno.add(opeFwdIn.FolioInterno);
						
						OperacionForward opeFwd = new OperacionForward();
						Utils.copyPojo(opeFwdIn, opeFwd);
						opeFwd.IdOperacion = opeMesOut.IdOperacion;
						opeFwd.FolioCartera = 0L;
						opeFwd.IdClasificacionTipoInstrumento = "DV";
						opeFwd.PrecioVencimiento = BigDecimal.ZERO;
						opeFwd.TasaDescuento = BigDecimal.ZERO;
						opeFwd.TipoCambioAnticipo = BigDecimal.ZERO;
						opeFwd.MontoARecibirAnticipo = BigDecimal.ZERO;
						opeFwd.TipoCambioObservado = BigDecimal.ZERO;
						operacionesForward.add(opeFwd);
					}
				}
			}
			
			
			/***** FORMAS DE PAGO OPERACION FORWARD *****/
						
			Long aplicaFeriadoMonedaPrincipal = 0L;
			SQL sql = new SQL();	
			HashMap<String,Object> parametros = new HashMap<String,Object>();
			parametros.put("IdInstrumento",in.p_tablaForward.get(0).IdInstrumento);
			parametros.put("IdMonedaPrincipal", "USD");
			parametros.put("IdMonedaSecundaria", "CLP");

			aplicaFeriadoMonedaPrincipal = sql.countQuery(conn, SQL_SELECT_CONSULTAR_MONEDAS_FERIADO_MONEDA_PRINCIPAL, parametros);
			OutObtenerDiaSiguienteMultiplesPaises outObtenerFechaHabil = null;
			OutObtenerDiaSiguiente salDiaSgte = null;
			if(aplicaFeriadoMonedaPrincipal==1 && in.p_tablaForward.get(0).IdModalidadLiquidacion==7) {
				ObtenerDiaSiguienteMultiplesPaises obtenerDiaSiguienteMulti = new ObtenerDiaSiguienteMultiplesPaises();
				InObtenerDiaSiguienteMultiplesPaises obtenerFechaConFeriado = new InObtenerDiaSiguienteMultiplesPaises();
				
				ArrayList<Long> listaPais= new 	ArrayList<>();
				listaPais.add(in.p_IdPais);
				listaPais.add(223L);
				for(FormaPagoObjIn fpIn : in.p_DatosFormaPago){
					cl.bcs.plataforma.pla.pojo.FechasIn fechaIn=new cl.bcs.plataforma.pla.pojo.FechasIn();
					fechaIn.idInterno = 0L;
					fechaIn.fechaInicial = Fecha.toStringDate(((OperacionForwardObjIn) operacionForwardHash.get(fpIn.IdOperacion)).FechaVencimiento);
					fechaIn.numeroDias = ((FormaDePagoMesa) formaDePagoMesaHash.get(fpIn.IdFormaDePago)).Dias;
					fechaIn.paises=listaPais;
					obtenerFechaConFeriado.p_fechas.add(fechaIn);
				}
				
				obtenerFechaConFeriado.p_diaHabil="S";
				obtenerFechaConFeriado.p_soloCaeDiaHabil ="S";

				outObtenerFechaHabil = obtenerDiaSiguienteMulti.logica(conn, obtenerFechaConFeriado);	
				
				
			}else {
				ObtenerDiaSiguiente diaSgte = new ObtenerDiaSiguiente();
				InObtenerDiaSiguiente diaSgteIn = new InObtenerDiaSiguiente();
				diaSgteIn.p_idPais = in.p_IdPais;
				diaSgteIn.p_diaHabil = "S";
				for(FormaPagoObjIn fpIn : in.p_DatosFormaPago){
					FechasIn fechas = new FechasIn();
					fechas.fechaInicial = Fecha.toStringDate(((OperacionForwardObjIn) operacionForwardHash.get(fpIn.IdOperacion)).FechaVencimiento);
					fechas.numeroDias = ((FormaDePagoMesa) formaDePagoMesaHash.get(fpIn.IdFormaDePago)).Dias;
					diaSgteIn.p_fechas.add(fechas);
				}
				salDiaSgte = diaSgte.logica(conn, diaSgteIn);
			
			}
			
			//Comprobante Pago Operacion de Mesa
			CrearComprobantePagoOperacion crearComPag = new CrearComprobantePagoOperacion();
			CrearComprobantePagoOperacion_IN crearComPagIn = new CrearComprobantePagoOperacion_IN();
			CrearComprobantePagoOperacion_OUT crearComPagOut = new CrearComprobantePagoOperacion_OUT();
			crearComPagIn.p_IdEmpresa = in.p_IdEmpresa;
			crearComPagIn.p_IdUsuario = in.p_IdUsuario;
			crearComPagIn.p_IdModulo = in.p_IdModulo;
			crearComPagIn.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
			
			for (OperacionForward opeFwd: operacionesForward){
				for(FormaPagoObjIn fpIn: in.p_DatosFormaPago){
					if(opeFwd.FolioInterno.longValue() == fpIn.IdOperacion.longValue()){
						String fechaCondicionLiquidacion = null;
						if( outObtenerFechaHabil!=null ) {
							for(cl.bcs.plataforma.pla.ObtenerDiaSiguienteMultiplesPaises.OutObtenerDiaSiguienteMultiplesPaises.FechasOut fec:  outObtenerFechaHabil.p_out_fechas){
								if(fec.fechaInicial.equals(Fecha.toStringDate(opeFwd.FechaVencimiento)) && fec.numeroDiasInicial.equals(((FormaDePagoMesa) formaDePagoMesaHash.get(fpIn.IdFormaDePago)).Dias)){
									fechaCondicionLiquidacion = fec.fechaCalculada;
								}
							}
							
						}else {
							for(FechasOut fec: salDiaSgte.p_out_fechas){
								if(fec.fechaInicial.equals(Fecha.toStringDate(opeFwd.FechaVencimiento)) && fec.numeroDiasInicial.equals(((FormaDePagoMesa) formaDePagoMesaHash.get(fpIn.IdFormaDePago)).Dias)){
									fechaCondicionLiquidacion = fec.fechaCalculada;
								}
							}
						}
						ComprobantePagoOperacionObj_IN comPagIn = new ComprobantePagoOperacionObj_IN();
						comPagIn.IdInternoPagoOperacion = fpIn.IdInternoPagoOperacion;
						comPagIn.IdComprobantePagoOperacion = fpIn.IdComprobantePagoOperacion;
						comPagIn.IdEmpresa = in.p_IdEmpresa;
						comPagIn.IdOperacion = opeFwd.IdOperacion;
						comPagIn.IdIngresoEgreso = 0L;
						comPagIn.IdAbono = 0L;
						comPagIn.IdCargo = 0L;
						comPagIn.IdIngresoEgresoAnticipo = 0L;
						comPagIn.IdAbonoAnticipo = 0L;
						comPagIn.IdCargoAnticipo = 0L;
						comPagIn.IdTipoProducto = opeFwd.IdTipoProducto;
						comPagIn.RecibePagaPortafolio = fpIn.RecibePagaPortafolio;
						comPagIn.IdFormaDePago = ((FormaDePagoMesa) formaDePagoMesaHash.get(fpIn.IdFormaDePago)).IdFormaDePago;
						comPagIn.IdModalidadPago = ((ModalidadDePago) modalidadPagoHash.get(((FormaDePagoMesa) formaDePagoMesaHash.get(fpIn.IdFormaDePago)).IdMedioDePago)).IdModalidadDePago;
						comPagIn.IdMoneda = fpIn.IdMoneda;
						//comPagIn.IdMonedaLiquidacion = fpIn.IdMonedaLiquidacion;
						comPagIn.Monto = fpIn.MontoFormaDePago;
						//comPagIn.MontoLiquidacion = fpIn.MontoFormaDePago;
						comPagIn.FechaIngresoComprobantePagoOperacion = Fecha.toStringDate(opeFwd.FechaOperacion);
						comPagIn.FechaLiquidacionComprobantePagoOperacion = fechaCondicionLiquidacion;
						comPagIn.IdEstado = opeFwd.IdEstado;
						comPagIn.AfectarTesoreria = fpIn.AfectarTesoreria;
						
						crearComPagIn.p_ComprobantePagoOperacion.add(comPagIn);
					}
				}
			}
			log.write("MESA FORWARD :: Llamada Ingreso Comprobante Pago Operación");
			crearComPagOut = crearComPag.logica(conn, crearComPagIn);
			//out.P_OUT_MENSAJES.addAll(crearComPagOut.P_OUT_MENSAJES);
			if (MensajeSalida.getNumeroErrores(crearComPagOut.P_OUT_MENSAJES) > 0){
				out.P_OUT_MENSAJES.addAll(crearComPagOut.P_OUT_MENSAJES);
				return out;
			}
			
			
			/***** INGRESO OPERACIONES CALZADAS *****/
			if(in.p_tablaCalce.size() > 0){
				IngresoReserva ingresoReserva = new IngresoReserva();
				IngresoReserva_IN ingresoReservaIn = new IngresoReserva_IN();
				IngresoReserva_OUT ingresoReservaOut = new IngresoReserva_OUT();
				ingresoReservaIn.p_IdEmpresa = in.p_IdEmpresa;
				ingresoReservaIn.p_IdUsuario = in.p_IdUsuario;
				ingresoReservaIn.p_IdModulo = in.p_IdModulo;
				ingresoReservaIn.p_simularOperacion = "N";
				ingresoReservaIn.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
				ingresoReservaIn.p_IdPais = in.p_IdPais;
				
				params = new HashMap<>();
				params.put("IdEmpresa", in.p_IdEmpresa);
				params.put("FolioInterno", folioInterno);
				List<CalceForwardCartera> calceIn = _sql.query(conn, SQL_SELECT_OPERACIONES_FORWARD_CALCE, params, CalceForwardCartera.class);
				
				for(CalceForwardCartera calce: calceIn){
					for(OperacionForward opeFwd: operacionesForward){
						if(opeFwd.FolioInterno.longValue() == calce.FolioInterno.longValue()){
							ReservaObj_IN reservaIn = new ReservaObj_IN();
							reservaIn.IdOperacion = opeFwd.IdOperacion;
							reservaIn.IdRegistro = 0L;
							reservaIn.IdReserva = 0L;
							reservaIn.IdConcepto = "INGRES";
							reservaIn.FolioOperacion = 0L;
							reservaIn.FolioCartera = calce.IdOperacionCartera;
							reservaIn.FolioCarteraCustodia = calce.IdOperacionCartera;
							reservaIn.IdCartera = calce.IdCartera; 
							reservaIn.IdCarteraCustodia = calce.IdCartera;
							reservaIn.IdTipoProducto = "MEFW";
							reservaIn.Cantidad = calce.MontoCalce;
							reservaIn.Tasa = BigDecimal.ZERO;
							reservaIn.TasaMercado = BigDecimal.ZERO;
							reservaIn.Precio = BigDecimal.ZERO;
							reservaIn.PrecioMercado = BigDecimal.ZERO;
							reservaIn.ValorRescate = BigDecimal.ZERO;
							reservaIn.MontoValorizado = calce.MontoCalce;
							reservaIn.MontoLiquidacion = BigDecimal.ZERO;
							reservaIn.MontoLiquidacionOperacion = BigDecimal.ZERO;
							reservaIn.IdClasificacionTipoInstrumento = calce.IdClasificacionTipoInstrumento;
							reservaIn.IdTipoInstrumento = opeFwd.IdTipoInstrumento;
							reservaIn.IdInstrumento = opeFwd.IdInstrumento;
							reservaIn.Nemotecnico = calce.Nemotecnico;
							reservaIn.IdMoneda = calce.IdMonedaPrincipal;
							reservaIn.IdTipoReajuste = "";
							reservaIn.IdMonedaEmisionInstrumento = "";
							reservaIn.IdMonedaLiquidacionInstrumento = "";
							reservaIn.IdMonedaLiquidacionOperacion = "";
							reservaIn.IdEmisor = "";
							reservaIn.CorteMinimo = BigDecimal.ZERO;
							reservaIn.FechaVencimiento = Fecha.toStringDate(opeFwd.FechaVencimiento);
							reservaIn.EsFungible = "N";
							reservaIn.Glosa = "";
							reservaIn.IdBolsa = "0";
							reservaIn.IdBoveda = "0";
							reservaIn.CodigoIFRS = "0";
							reservaIn.FechaOperacion = Fecha.toStringDate(opeFwd.FechaOperacion);
							ingresoReservaIn.p_TblCarteraReserva.add(reservaIn);
						}
					}
				}
				for(CalceForwardCartera calce: calceIn){
					for(OperacionObjOut opeMesOut: ingresoOperacionOut.P_OUT_OPERACION){
						if(opeMesOut.IdAplicacion.longValue() == calce.FolioInterno.longValue()){
							ReservaObj_IN reservaIn = new ReservaObj_IN();
							reservaIn.IdOperacion = calce.IdOperacionCartera;
							reservaIn.IdRegistro = 0L;
							reservaIn.IdReserva = 0L;
							reservaIn.IdConcepto = "INGRES";
							reservaIn.FolioOperacion = 0L;
							reservaIn.FolioCartera = opeMesOut.IdOperacion;
							reservaIn.FolioCarteraCustodia = opeMesOut.IdOperacion;
							reservaIn.IdCartera = calce.IdCartera; 
							reservaIn.IdCarteraCustodia = calce.IdCartera;
							reservaIn.IdTipoProducto = "MEFW";
							reservaIn.Cantidad = calce.MontoCalce;
							reservaIn.Tasa = BigDecimal.ZERO;
							reservaIn.TasaMercado = BigDecimal.ZERO;
							reservaIn.Precio = BigDecimal.ZERO;
							reservaIn.PrecioMercado = BigDecimal.ZERO;
							reservaIn.ValorRescate = BigDecimal.ZERO;
							reservaIn.MontoValorizado = calce.MontoCalce;
							reservaIn.MontoLiquidacion = BigDecimal.ZERO;
							reservaIn.MontoLiquidacionOperacion = BigDecimal.ZERO;
							reservaIn.IdClasificacionTipoInstrumento = calce.IdClasificacionTipoInstrumento;
							reservaIn.IdTipoInstrumento = calce.IdTipoInstrumento;
							reservaIn.IdInstrumento = calce.IdInstrumento;
							reservaIn.Nemotecnico = calce.Nemotecnico;
							reservaIn.IdMoneda = calce.IdMonedaPrincipal;
							reservaIn.IdTipoReajuste = "";
							reservaIn.IdMonedaEmisionInstrumento = "";
							reservaIn.IdMonedaLiquidacionInstrumento = "";
							reservaIn.IdMonedaLiquidacionOperacion = "";
							reservaIn.IdEmisor = "";
							reservaIn.CorteMinimo = BigDecimal.ZERO;
							reservaIn.FechaVencimiento = Fecha.toStringDate(calce.FechaLiquidacion);
							reservaIn.EsFungible = "N";
							reservaIn.Glosa = "";
							reservaIn.IdBolsa = "0";
							reservaIn.IdBoveda = "0";
							reservaIn.CodigoIFRS = "0";
							reservaIn.FechaOperacion = Fecha.toStringDate(((OperacionForwardObjIn) operacionForwardHash.get(opeMesOut.IdAplicacion)).FechaOperacion);
							ingresoReservaIn.p_TblCarteraReserva.add(reservaIn);
						}
					}
				}
				log.write("MESA FORWARD :: Llamada Ingresar Reserva");
				ingresoReservaOut = ingresoReserva.logica(conn, ingresoReservaIn);
				out.P_OUT_MENSAJES.addAll(ingresoReservaOut.P_OUT_MENSAJES);
				if (MensajeSalida.getNumeroErrores(ingresoReservaOut.P_OUT_MENSAJES) > 0){
					return out;
				}
			}
			
			
			/***** LIMITES *****/
			Long autCont = 0L;
			Long idInternoForward = 1L;
			for(OperacionForward opeFwd : operacionesForward) {
				opeFwd.idInterno = idInternoForward++;
				opeFwd.CodigoOperador = empleadoUsuario.CodigoEmpleado;
			}
			
			ValidarLimitesCuentaPropiaForward.SalidaValidarLimitesCuentaPropiaForward salidaValidarLimitesCuentaPropiaForward = validarLimitesCuentaPropia(conn, in, operacionesForward);
			
			if(salidaValidarLimitesCuentaPropiaForward.P_OUT_VALIDACIONES.size() > 0) {
				AtomicLong totalAutLimCtaPropia = new AtomicLong(0L);
				Map<Long, Validacion> validacionLimCtaPropiaPorIdInterno = salidaValidarLimitesCuentaPropiaForward.P_OUT_VALIDACIONES.stream().collect(Collectors.toMap(u -> u.IdInternoOperacion, u -> u));
				operacionesForward = operacionesForward.stream()
						.map(operacionFwd -> Pair.of(operacionFwd, validacionLimCtaPropiaPorIdInterno.get(operacionFwd.idInterno)))
						.peek(parOpeValid -> {
							Validacion validacion = parOpeValid.getRight();
							if (Objects.nonNull(validacion)) {
								if (_cl.TIPO_VALIDACION_OMITIR.equals(validacion.TipoComportamiento) == false) {
									out.P_OUT_MENSAJES.add(validacion.Mensaje);
								}
								if (_cl.TIPO_VALIDACION_RECHAZAR.equals(validacion.TipoComportamiento)) {
									codigosRechazoLimiteCtaPropia.add(validacion.Mensaje.codigo);
								}
								if (_cl.TIPO_VALIDACION_AUTORIZACION.equals(validacion.TipoComportamiento)) {
									totalAutLimCtaPropia.getAndIncrement();
									OperacionForward operacionForward = parOpeValid.getLeft();
									operacionForward.IdEstado = "PEN";
									
									EventoAutorizacion eventoAutorizacion = new EventoAutorizacion();
									eventoAutorizacion.IdTipoEvento = "LIFDCP";
									eventoAutorizacion.NumeroOperacion = operacionForward.FolioInterno;
									eventoAutorizacion.Glosa = "FUERA_LIM_CTA_PROPIA";
									evtList.add(eventoAutorizacion);
								}
							}
						})
						.filter(parOpeValid -> parOpeValid.getRight() == null || _cl.TIPO_VALIDACION_RECHAZAR.equals(parOpeValid.getRight().TipoComportamiento) == false)
						.map(Pair::getLeft)
						.collect(Collectors.toList());
				
				Id += totalAutLimCtaPropia.get();
				autCont += totalAutLimCtaPropia.get();
				
			}
			
			if(operacionesForward.isEmpty()) { return out;}
			
			if(autCont > 0L) {
				CrearEventoAutorizacionMasivo eventoAutorizacion = new CrearEventoAutorizacionMasivo();
				CrearEventoAutorizacionMasivo.ServiceEntrada entradaEventoAutorizacion = new CrearEventoAutorizacionMasivo.ServiceEntrada();
				entradaEventoAutorizacion.P_IDEMPRESA = in.p_IdEmpresa;
				entradaEventoAutorizacion.P_IDMODULO = in.p_IdModulo;
				entradaEventoAutorizacion.P_IDUSUARIO = in.p_IdUsuario;
				entradaEventoAutorizacion.P_SIMULAROPERACION = "N";
				entradaEventoAutorizacion.P_USAREGLASNEGOCIO = "N";
				
				for(EventoAutorizacion evt: evtList) {
					for(OperacionForward opeFwd : operacionesForward) {
						if(evt.NumeroOperacion == opeFwd.FolioInterno) {
							
							
							CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS evento = new CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS();
							evento.IdTipoEvento = evt.IdTipoEvento;
							evento.IdModuloSolicita = in.p_IdModulo;
							evento.IdUsuarioSolicita = in.p_IdUsuario;
							evento.Fecha = Fecha.toStringDate(opeFwd.FechaOperacion);
							evento.IdTipoOperacion = opeFwd.IdTipoOperacion;
							evento.IdConcepto = "LIMFWD";
							evento.NumeroOperacionConcepto = opeFwd.IdOperacion.toString();
							evento.FechaOperacionConcepto = "";
							evento.IdCliente = opeFwd.IdCliente;
							evento.IdFuncionario = 0L;
							evento.IdProveedor = 0L;
							evento.IdTipoProducto = opeFwd.IdTipoProducto;
							evento.IdClasificacionTipoInstrumento = "FW";
							evento.IdTipoInstrumento = "FWD";
							evento.Nemo = opeFwd.Nemotecnico;
							evento.Cantidad = 0L;
							evento.Monto = MonedaFormato.formatear(conn, opeFwd.MontoOperacion, "MONTO", opeFwd.IdMonedaPrincipal).valor;
							evento.Precio = opeFwd.TCCierre;
							evento.Tasa = BigDecimal.ZERO;
							evento.IdMoneda = opeFwd.IdMonedaPrincipal;
							evento.Glosa = evt.Glosa;
							
							entradaEventoAutorizacion.P_TABLAEVENTOS.add(evento);
						}
					}
				}
				CrearEventoAutorizacionMasivo.ServiceSalida salidaEventoAutorizacion = eventoAutorizacion.logica(conn, entradaEventoAutorizacion);
				for (CrearEventoAutorizacionMasivo.ObjERRORESDETALLE error: salidaEventoAutorizacion.P_OUT_ERRORESDETALLE) {
					out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,error.Codigo,mapMsg,conn,in.p_IdEmpresa));
					Id++;
				}
			}
			
			/***** CAMBIAR ESTADO *****/
			CambiarEstadoOperacion cambiarEstadoOperacion = new CambiarEstadoOperacion();
			CambiarEstadoOperacionEntrada cambiarEstadoOperacionIn = new CambiarEstadoOperacionEntrada(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo, "N", in.p_UsaReglasNegocio, in.p_IdPais);
			CambiarEstadoOperacionSalida cambiarEstadoOperacionOut = new CambiarEstadoOperacionSalida();
			for(OperacionForward opeFwd: operacionesForward){
				ObjCambiarEstadoOperacionIN ceoIn = new ObjCambiarEstadoOperacionIN();
				ceoIn.IdOperacion = opeFwd.IdOperacion;
				ceoIn.IdTipoProducto = opeFwd.IdTipoProducto;
				ceoIn.IdNuevoEstado = "VIG";
				ceoIn.IdReserva = 0L;
				ceoIn.FolioCarteraCustodia = 0L;
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
			for(MensajeSalida msje : cambiarEstadoOperacionOut.P_OUT_MENSAJES) {
				if(!"".equalsIgnoreCase(msje.codigo) && !"".equalsIgnoreCase(msje.descripcion)) {
					out.P_OUT_MENSAJES.add(msje);
				}
			}
			if (MensajeSalida.getNumeroErrores(cambiarEstadoOperacionOut.P_OUT_MENSAJES) > 0){
				return out;
			}
			
			
			/***** INGRESO DE FORWARD EN CARTERA *****/
			IngresarForwardCartera ingresarForwardCartera = new IngresarForwardCartera();
			IngresarForwardCarteraEntrada ingresarForwardCarteraIn = new IngresarForwardCarteraEntrada();
			IngresarForwardCarteraSalida ingresarForwardCarteraOut = new IngresarForwardCarteraSalida();
			ingresarForwardCarteraIn.p_IdEmpresa = in.p_IdEmpresa;
			ingresarForwardCarteraIn.p_IdPais = in.p_IdPais;
			ingresarForwardCarteraIn.p_IdUsuario = in.p_IdUsuario;
			ingresarForwardCarteraIn.p_IdBolsa = in.p_IdBolsa;
			ingresarForwardCarteraIn.p_IdSucursal = in.p_IdSucursal;
			ingresarForwardCarteraIn.p_IdModulo = in.p_IdModulo;
			ingresarForwardCarteraIn.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
			
			for(OperacionForward opeFwd: operacionesForward){
				TablaForwardObjIn tablaFwdIn = new TablaForwardObjIn();
				Utils.copyPojo(opeFwd, tablaFwdIn);
				
				tablaFwdIn.IdTipoOperacionOriginal = opeFwd.IdTipoOperacion;
				tablaFwdIn.IdTipoOperacionNuevo = opeFwd.IdTipoOperacion;
				tablaFwdIn.IdClienteOriginal = 0L;
				tablaFwdIn.IdClienteNuevo = opeFwd.IdCliente;
				tablaFwdIn.IdPortafolioOriginal = 0L;
				tablaFwdIn.IdPortafolioNuevo = opeFwd.IdPortafolio;
				tablaFwdIn.IdPersonaEjecutivoOriginal = 0L;
				tablaFwdIn.IdPersonaEjecutivoNuevo = opeFwd.IdPersonaEjecutivo;
				tablaFwdIn.MontoOperacionOriginal = BigDecimal.ZERO;
				tablaFwdIn.MontoOperacionNuevo = opeFwd.MontoOperacion;
				tablaFwdIn.MontoInicialOriginal = BigDecimal.ZERO;
				tablaFwdIn.MontoInicialNuevo = opeFwd.MontoInicial;
				tablaFwdIn.MontoFinalOriginal = BigDecimal.ZERO;
				tablaFwdIn.MontoFinalNuevo = opeFwd.MontoFinal;
				tablaFwdIn.TCCierreOriginal = BigDecimal.ZERO;
				tablaFwdIn.TCCierreNuevo = opeFwd.TCCierre;
				tablaFwdIn.ParidadCierreOriginal = BigDecimal.ZERO;
				tablaFwdIn.ParidadCierreNuevo = opeFwd.ParidadCierre;
				tablaFwdIn.PlazoOriginal = 0L;
				tablaFwdIn.PlazoNuevo = opeFwd.Plazo;
				tablaFwdIn.FechaVencimientoOriginal = Fecha.toDate("0001-01-01");
				tablaFwdIn.FechaVencimientoNuevo = opeFwd.FechaVencimiento;
				tablaFwdIn.FechaPagoOriginal = Fecha.toDate("0001-01-01");
				tablaFwdIn.FechaPagoNuevo = opeFwd.FechaPago;
				tablaFwdIn.IdModalidadLiquidacionOriginal = 0L;
				tablaFwdIn.IdModalidadLiquidacionNuevo = opeFwd.IdModalidadLiquidacion;
				tablaFwdIn.TCCostoOriginal = BigDecimal.ZERO;
				tablaFwdIn.TCCostoNuevo = opeFwd.TCCosto;
				tablaFwdIn.PuntosTasasForwardOriginal = "";
				tablaFwdIn.PuntosTasasForwardNuevo = opeFwd.PuntosTasasForward;
				tablaFwdIn.PuntosForwardOriginal = BigDecimal.ZERO;
				tablaFwdIn.PuntosForwardNuevo = opeFwd.PuntosForward;
				tablaFwdIn.TCForwardOriginal = BigDecimal.ZERO;
				tablaFwdIn.TCForwardNuevo = opeFwd.TCForward;
				tablaFwdIn.TasasMonedaPrincipalOriginal = BigDecimal.ZERO;
				tablaFwdIn.TasasMonedaPrincipalNuevo = opeFwd.TasasMonedaPrincipal;
				tablaFwdIn.TasasMonedaSecundariaOriginal = BigDecimal.ZERO;
				tablaFwdIn.TasasMonedaSecundariaNuevo = opeFwd.TasasMonedaSecundaria;
				tablaFwdIn.MontoDiferidoOriginal = BigDecimal.ZERO;
				tablaFwdIn.MontoDiferidoNuevo = opeFwd.MontoDiferido;
				tablaFwdIn.MontoMargenOriginal = BigDecimal.ZERO;
				tablaFwdIn.MontoMargenNuevo = opeFwd.MontoMargen;
				tablaFwdIn.IdFixingOriginal = 0L;
				tablaFwdIn.IdFixingNuevo = opeFwd.IdFixing;
				tablaFwdIn.FechaFixingOriginal = Fecha.toDate("0001-01-01");
				tablaFwdIn.FechaFixingNuevo = opeFwd.FechaFixing;
				tablaFwdIn.HoraFixingOriginal = "00:00:00";
				tablaFwdIn.HoraFixingNuevo = opeFwd.HoraFixing;
				tablaFwdIn.TasaDescuento = opeFwd.TasaDescuento;
				tablaFwdIn.TipoCambioAnticipo = opeFwd.TipoCambioAnticipo;
				tablaFwdIn.MontoARecibirAnticipo = opeFwd.MontoARecibirAnticipo;
				tablaFwdIn.TipoCambioObservado = opeFwd.TipoCambioObservado;
				
				ingresarForwardCarteraIn.p_tablaForward.add(tablaFwdIn);
			}
			log.write("MESA FORWARD :: Llamada Ingresar Forward Cartera");
			ingresarForwardCarteraOut = ingresarForwardCartera.logica(conn, ingresarForwardCarteraIn);
			
			out.P_OUT_ERRORESCONTADOR = out.P_OUT_ERRORESCONTADOR + ingresarForwardCarteraOut.P_OUT_ERRORESCONTADOR;
			out.P_OUT_MENSAJES.addAll(ingresarForwardCarteraOut.P_OUT_MENSAJES);
			if (MensajeSalida.getNumeroErrores(ingresarForwardCarteraOut.P_OUT_MENSAJES) > 0){
				return out;
			}
			
			
			/***** SALIDA *****/
			out.P_OUT_ERRORESCONTADOR = out.P_OUT_ERRORESCONTADOR + MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
			out.P_OUT_TBLIDFORWARD = new ArrayList<OperacionForwardObjOut>();
			for(OperacionForward opeFwd: operacionesForward){
				for(TablaForwardObjOut fwdCarOut: ingresarForwardCarteraOut.P_OUT_TBLIDFORWARDINGRESOS){
					if(opeFwd.FolioInterno.longValue() == fwdCarOut.FolioInterno.longValue() && opeFwd.IdOperacion.longValue() == fwdCarOut.IdOperacion.longValue()){
						OperacionForwardObjOut opOut = new OperacionForwardObjOut();
						opOut.FolioInterno = fwdCarOut.FolioInterno;
						opOut.IdCartera = fwdCarOut.IdCartera;
						opOut.IdOperacion = fwdCarOut.IdOperacion;
						opOut.FolioCartera = fwdCarOut.FolioCartera;
						opOut.IdEstado = opeFwd.IdEstado;
						out.P_OUT_TBLIDFORWARD.add(opOut);
					}
				}
				if(opeFwd.IdEstado.equals("VIG"))cantOpeVig++;
				else if(opeFwd.IdEstado.equals("PEN"))cantOpePen++;
			}
			
			
			/***** UPDATE EN LAS FORMAS DE PAGO: IdOperacion->IdCartera *****/
			_sql.batch(conn, SQL_UPDATE_FORMA_DE_PAGO_ID_OPERACION, out.P_OUT_TBLIDFORWARD, OperacionForwardObjOut.class);
			
			
			/***** AGREGAR LOG *****/
			AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
			for (OperacionForward opeFwd: operacionesForward) {
				AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
				log.IdCabeceraLog = opeFwd.IdOperacion;
				log.IdTipoLog = "INGRESO";
				log.IdModulo = in.p_IdModulo;
				log.IdUsuario = in.p_IdUsuario;
				log.IdEmpresa = in.p_IdEmpresa;
				log.FechaSistemaLog = fechaSistema;
				log.IdUsuarioAtendido = in.p_IdUsuario;
				log.IdCliente = opeFwd.IdCliente;
				log.EstadoCliente = "";
				log.NombreCliente = "";
				log.IdTipoDocumentoIdentidad = "";
				log.IdDocumentoIdentidad = "";
				log.IdPortafolio = opeFwd.IdPortafolio;
				log.EstadoPortafolio = "";
				log.IdTipoRegistro = "FORWARD";
				log.EstadoRegistro = opeFwd.IdEstado;
				log.IdTipoOperacion = opeFwd.IdTipoOperacion;
				log.Folio = opeFwd.FolioCartera;
				log.Cantidad = BigDecimal.ZERO;
				log.PrecioTasa = BigDecimal.ZERO;
				log.Monto = opeFwd.MontoOperacion != null ? opeFwd.MontoOperacion : BigDecimal.ZERO;
				log.IdMonedaOrigen = opeFwd.IdMonedaPrincipal;
				log.IdMonedaLiquidacion = opeFwd.IdMonedaSecundaria != null ? opeFwd.IdMonedaSecundaria : "";
				log.TipoCambio = opeFwd.TCCierre.toString();
				log.Nemo = "Forward";
				log.IdProceso = 0L;
				log.EstadoProceso = "VIG";
				log.NombreProceso = "CrearOperacionForward";						
				entradaAgregarLog.p_Log.add(log);
			}
			AgregarLog agregarLogService = new AgregarLog();
			agregarLogService.logica(conn, entradaAgregarLog);
			
			
			/***** WEBSOCKET REFRESCO AUTOMATICO GRILLA MOVIMIENTOS *****/
			AgregarMensajeWebsocket.agregar(conn, in.p_IdEmpresa, "ACTFWD", "Actualizacion Grilla FWD");
			
		}catch(Exception e){
			out.P_OUT_ERRORESCONTADOR = 1L;
			log.write(e);
			throw e;
		} finally {
			boolean noHayOperacionesAceptadas = out.P_OUT_TBLIDFORWARD.isEmpty();
			AtomicLong contadorErrores = new AtomicLong();
			AtomicLong contadorAdvertencias = new AtomicLong();
			AtomicLong contadorAutorizaciones = new AtomicLong();
			out.P_OUT_MENSAJES = out.P_OUT_MENSAJES.stream()
					.peek(mensaje -> {
						boolean noEsMensajeRechazoLimite = !codigosRechazoLimiteCtaPropia.contains(mensaje.codigo);
						if (MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo) && (noHayOperacionesAceptadas || noEsMensajeRechazoLimite)) {
							contadorErrores.getAndIncrement();
						}
						
						if (MensajeSalida.MENSAJE_ADVERTENCIA.equals(mensaje.tipo)) {
							contadorAdvertencias.getAndIncrement();
						}
						
						boolean esAutorizacionIngresada = out.P_OUT_TBLIDFORWARD.size() > 0 && MensajeSalida.MENSAJE_AUTORIZACION.equals(mensaje.tipo);
						if (esAutorizacionIngresada) {
							contadorAutorizaciones.getAndIncrement();
						}
					})
					.filter(mensaje -> out.P_OUT_TBLIDFORWARD.size() > 0 || MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo))
					.collect(Collectors.toList());

			out.P_OUT_ERRORESCONTADOR = contadorErrores.get();
			out.P_OUT_ADVERTENCIASCONTADOR = contadorAdvertencias.get() + contadorAutorizaciones.get();
		}
		
		return out;
	}
	
	private String obtenerDiaSiguiente(Connection con, String fecha, Long dias, Long IdEmpresa) throws Exception {
		//obtener configuraciones
		HashMap<String, HashMap<String, String>> configuraciones = Utils.getConfiguraciones(con, IdEmpresa);
		long idPais = Long.valueOf(configuraciones.get("PLATAFORMA").get("PLA.PaisSistema"));
		List<FechasIn> fechasInList = new ArrayList<ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn>();

		FechasIn fechas = new FechasIn();
		fechas.fechaInicial = fecha;
		fechas.numeroDias = dias;	// cantidad de dias a calcular
		fechas.pais = idPais;
		fechasInList.add(fechas);
		
		ObtenerDiaSiguiente diaSgte = new ObtenerDiaSiguiente();
		ObtenerDiaSiguiente.InObtenerDiaSiguiente diaSgteIn = new ObtenerDiaSiguiente.InObtenerDiaSiguiente();
		diaSgteIn.p_idPais = idPais;
		diaSgteIn.p_diaHabil = "N";	// dias continuos
		diaSgteIn.p_fechas = fechasInList;
		
		ObtenerDiaSiguiente.OutObtenerDiaSiguiente salDiaSgte = diaSgte.logica(con, diaSgteIn);
		return salDiaSgte.p_out_fechas.get(0).fechaCalculada;		
	}	
	
	private ValidarLimitesCuentaPropiaForward.SalidaValidarLimitesCuentaPropiaForward validarLimitesCuentaPropia(Connection conn, CrearOperacionForwardEntrada entrada, List<OperacionForward> operaciones) throws Exception{
		
		log.write("INICIO::CrearOperacionForward.validarLimitesCuentaPropia");
		long inicio = System.currentTimeMillis();
		
		ValidarLimitesCuentaPropiaForward.EntradaValidarLimitesCuentaPropiaForward entradaValidarLimitescuentaPropiaForward = new ValidarLimitesCuentaPropiaForward.EntradaValidarLimitesCuentaPropiaForward();
		entradaValidarLimitescuentaPropiaForward.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaValidarLimitescuentaPropiaForward.p_IdTipoProceso = _cl.PROCESO_INGRESAR;
		entradaValidarLimitescuentaPropiaForward.p_IdUsuario = entrada.p_IdUsuario;
		entradaValidarLimitescuentaPropiaForward.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
		entradaValidarLimitescuentaPropiaForward.p_OperacionesFwd = operaciones.stream()
				.map(u -> {
					ValidarLimitesCuentaPropiaForward.OperacionForward o = new ValidarLimitesCuentaPropiaForward.OperacionForward();
					o.IdInterno = u.idInterno;
					o.IdOperacionFwd = null;
					o.IdCliente = u.IdCliente;
					o.IdPortafolio = u.IdPortafolio;
					o.IdEstado = u.IdEstado;
					o.IdInstrumento = u.IdInstrumento;
					o.IdTipoInstrumento = u.IdTipoInstrumento;
					o.NemotecnicoInstrumento = u.Nemotecnico;
					o.CodigoOperador = u.CodigoOperador;
					o.IdTipoCanalIngreso = u.IdTipoCanalIngreso;
					o.IdTipoOperacion = u.IdTipoOperacion;
					o.IdMonedaPrincipal = u.IdMonedaPrincipal;
					o.MontoOperacion = u.MontoOperacion;
					o.FechaOperacion = new java.sql.Timestamp(u.FechaOperacion.getTime());
					o.FechaVencimiento = new java.sql.Timestamp(u.FechaVencimiento.getTime());
					o.FechaLiquidacion = new java.sql.Timestamp(u.FechaPago.getTime());
					o.PlazoDias = u.Plazo;
					o.Origen = _cl.ORIGEN_ENTRADA;
					return o;
				})
				.collect(Collectors.toList());
		
		ValidarLimitesCuentaPropiaForward.SalidaValidarLimitesCuentaPropiaForward salidaValidarLimitesCuentaPropiaForward = new ValidarLimitesCuentaPropiaForward().logica(conn, entradaValidarLimitescuentaPropiaForward);
		
		long fin = System.currentTimeMillis();
		log.write("FIN::CrearOperacionForward.validarLimitesCuentaPropia {}", fin-inicio);
		return salidaValidarLimitesCuentaPropiaForward;
		
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String jsonDesa = "{\"p_IdEmpresa\":1,\"p_IdPais\":44,\"p_IdUsuario\":33,\"p_IdBolsa\":\"BCS\",\"p_IdSucursal\":0,\"p_IdModulo\":\"MFW\",\"p_UsaReglasNegocio\":\"S\",\"p_tablaForward\":[{\"FolioInterno\":1,\"IdTipoOperacion\":\"COMFWD\",\"IdTipoInstrumento\":\"FWD\",\"IdTipoProducto\":\"MEFW\",\"IdInstrumento\":346370,\"Nemotecnico\":\"SECANO\",\"IdCliente\":123569,\"IdPortafolio\":27499,\"IdPersonaEjecutivo\":10003,\"IdTipoCanalIngreso\":\"MES\",\"IdMonedaPrincipal\":\"USD\",\"IdMonedaSecundaria\":\"CLP\",\"MontoOperacion\":1000,\"MontoInicial\":0,\"MontoFinal\":0,\"FechaOperacion\":\"2018-08-09\",\"TCCierre\":620,\"ParidadCierre\":0,\"Plazo\":6,\"FechaVencimiento\":\"2018-08-15\",\"FechaPago\":\"2018-08-15\",\"IdModalidadLiquidacion\":7,\"IdReferenciaMercado\":0,\"IdPlaza\":2931,\"TCCosto\":600,\"PuntosTasasForward\":\"S\",\"PuntosForward\":0,\"TCForward\":600,\"TasasMonedaPrincipal\":0,\"TasasMonedaSecundaria\":0,\"MontoDiferido\":20000,\"MontoMargen\":-20000,\"IdFixing\":37,\"FechaFixing\":\"0001-01-01\",\"HoraFixing\":\"00:25:59\",\"IdMesa\":2,\"IdSpotAsociado\":0,\"IdClasificacionCartera\":11,\"Observaciones\":\"\",\"IdEstado\":\"VIG\",\"PagaConCtaInv\":\"N\"}],\"p_DatosFormaPago\":[{\"IdInternoPagoOperacion\":1,\"IdComprobantePagoOperacion\":0,\"IdTipoMovimiento\":\"MOVING\",\"IdFormaDePago\":\"VENFWD_EFE_CLP_0\",\"IdOperacion\":1,\"RecibePagaPortafolio\":\"P\",\"IdMoneda\":\"CLP\",\"IdMonedaLiquidacion\":\"CLP\",\"MontoFormaDePago\":620000,\"AfectarTesoreria\":\"S\"},{\"IdInternoPagoOperacion\":2,\"IdComprobantePagoOperacion\":0,\"IdTipoMovimiento\":\"MOVEGR\",\"IdFormaDePago\":\"COMFWD_CHE_USD_1\",\"IdOperacion\":1,\"RecibePagaPortafolio\":\"R\",\"IdMoneda\":\"USD\",\"IdMonedaLiquidacion\":\"USD\",\"MontoFormaDePago\":1000,\"AfectarTesoreria\":\"S\"}],\"p_tablaCalce\":[]}";
			//String jsonMaBa = "{\"p_IdEmpresa\":1,\"p_IdPais\":44,\"p_IdUsuario\":33,\"p_IdBolsa\":\"BCS\",\"p_IdSucursal\":0,\"p_IdModulo\":\"MFW\",\"p_UsaReglasNegocio\":\"S\",\"p_tablaForward\":[{\"FolioInterno\":1,\"IdTipoOperacion\":\"COMFWD\",\"IdTipoInstrumento\":\"FWD\",\"IdTipoProducto\":\"MEFW\",\"IdInstrumento\":346370,\"Nemotecnico\":\"SECANO\",\"IdCliente\":123569,\"IdPortafolio\":27499,\"IdPersonaEjecutivo\":10003,\"IdTipoCanalIngreso\":\"MES\",\"IdMonedaPrincipal\":\"USD\",\"IdMonedaSecundaria\":\"CLP\",\"MontoOperacion\":1000,\"MontoInicial\":0,\"MontoFinal\":0,\"FechaOperacion\":\"2018-06-20\",\"TCCierre\":620,\"ParidadCierre\":0,\"Plazo\":1,\"FechaVencimiento\":\"2018-06-21\",\"FechaPago\":\"2018-06-21\",\"IdModalidadLiquidacion\":6,\"IdReferenciaMercado\":0,\"IdPlaza\":2931,\"TCCosto\":600,\"PuntosTasasForward\":\"S\",\"PuntosForward\":0,\"TCForward\":600,\"TasasMonedaPrincipal\":0,\"TasasMonedaSecundaria\":0,\"MontoDiferido\":20000,\"MontoMargen\":-20000,\"IdFixing\":37,\"FechaFixing\":\"0001-01-01\",\"HoraFixing\":\"20:50:24\",\"IdMesa\":2,\"IdSpotAsociado\":0,\"IdClasificacionCartera\":11,\"Observaciones\":\"\",\"IdEstado\":\"VIG\",\"PagaConCtaInv\":\"S\"}],\"p_DatosFormaPago\":[{\"IdInternoPagoOperacion\":1,\"IdComprobantePagoOperacion\":0,\"IdTipoMovimiento\":\"MOVEGR\",\"IdFormaDePago\":\"COMFWD_CIN_USD_0\",\"IdOperacion\":1,\"RecibePagaPortafolio\":\"R\",\"IdMoneda\":\"USD\",\"IdMonedaLiquidacion\":\"USD\",\"MontoFormaDePago\":1000,\"AfectarTesoreria\":\"N\"}],\"p_tablaCalce\":[]}";
			//String jsonDesaEF = "{\"p_IdEmpresa\":1,\"p_IdPais\":44,\"p_IdUsuario\":33,\"p_IdBolsa\":0,\"p_IdSucursal\":0,\"p_IdModulo\":\"MFW\",\"p_UsaReglasNegocio\":\"S\",\"p_tablaForward\":[{\"FolioInterno\":1,\"IdTipoOperacion\":\"COMFWD\",\"IdTipoInstrumento\":\"FWD\",\"IdTipoProducto\":\"MEFW\",\"IdInstrumento\":346370,\"Nemotecnico\":\"SECANO\",\"IdCliente\":130826,\"IdPortafolio\":34993,\"IdPersonaEjecutivo\":1335561,\"IdTipoCanalIngreso\":\"MES\",\"IdMonedaPrincipal\":\"USD\",\"IdMonedaSecundaria\":\"CLP\",\"MontoOperacion\":1000,\"MontoInicial\":0,\"MontoFinal\":0,\"FechaOperacion\":\"2018-06-26\",\"TCCierre\":625,\"ParidadCierre\":0,\"Plazo\":1,\"FechaVencimiento\":\"2018-06-27\",\"FechaPago\":\"0001-01-01\",\"IdModalidadLiquidacion\":7,\"IdReferenciaMercado\":0,\"IdPlaza\":2931,\"TCCosto\":600,\"PuntosTasasForward\":\"S\",\"PuntosForward\":0,\"TCForward\":600,\"TasasMonedaPrincipal\":0,\"TasasMonedaSecundaria\":0,\"MontoDiferido\":25000,\"MontoMargen\":-25000,\"IdFixing\":37,\"FechaFixing\":\"0001-01-01\",\"HoraFixing\":\"23:03:21\",\"IdMesa\":2,\"IdSpotAsociado\":0,\"IdClasificacionCartera\":11,\"Observaciones\":\"\",\"IdEstado\":\"VIG\",\"PagaConCtaInv\":\"N\"}],\"p_DatosFormaPago\":[{\"IdInternoPagoOperacion\":1,\"IdComprobantePagoOperacion\":0,\"IdTipoMovimiento\":\"MOVING\",\"IdFormaDePago\":\"VENFWD_EFE_CLP_0\",\"IdOperacion\":1,\"RecibePagaPortafolio\":\"P\",\"IdMoneda\":\"CLP\",\"IdMonedaLiquidacion\":\"CLP\",\"MontoFormaDePago\":625000,\"AfectarTesoreria\":\"S\"},{\"IdInternoPagoOperacion\":2,\"IdComprobantePagoOperacion\":0,\"IdTipoMovimiento\":\"MOVEGR\",\"IdFormaDePago\":\"COMFWD_EFE_USD_0\",\"IdOperacion\":1,\"RecibePagaPortafolio\":\"R\",\"IdMoneda\":\"USD\",\"IdMonedaLiquidacion\":\"USD\",\"MontoFormaDePago\":1000,\"AfectarTesoreria\":\"S\"}],\"p_tablaCalce\":[]}";
			CrearOperacionForwardEntrada Objin = gson.fromJson(jsonDesa, CrearOperacionForwardEntrada.class);
			
			CrearOperacionForward demo = new CrearOperacionForward();				
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			try {
 				CrearOperacionForwardSalida ObjOut = demo.logica(con, Objin);
 				//con.commit();
 				con.rollback();
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