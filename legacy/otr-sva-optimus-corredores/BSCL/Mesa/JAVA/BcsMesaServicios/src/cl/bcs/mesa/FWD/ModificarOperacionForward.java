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

import cl.bcs.mesa.MES.CrearComprobantePagoOperacion;
import cl.bcs.mesa.FWD.CrearOperacionForward.EventoAutorizacion;
import cl.bcs.mesa.FWD.CrearOperacionForward.FormaDePagoMesa;
import cl.bcs.mesa.FWD.CrearOperacionForward.FormaPagoObjIn;
import cl.bcs.mesa.FWD.CrearOperacionForward.ModalidadDePago;
import cl.bcs.mesa.FWD.CrearOperacionForward.OperacionForward;
import cl.bcs.mesa.FWD.CrearOperacionForward.OperacionForwardObjIn;
import cl.bcs.mesa.FWD.CrearOperacionForward.OperacionForwardObjOut;
import cl.bcs.mesa.FWD.IngresarForwardCartera.IngresarForwardCarteraEntrada;
import cl.bcs.mesa.FWD.IngresarForwardCartera.IngresarForwardCarteraSalida;
import cl.bcs.mesa.FWD.IngresarForwardCartera.TablaForwardObjIn;
import cl.bcs.mesa.FWD.IngresarForwardCartera.TablaForwardObjOut;
import cl.bcs.mesa.MES.CrearComprobantePagoOperacion.ComprobantePagoOperacionObj_IN;
import cl.bcs.mesa.MES.CrearComprobantePagoOperacion.CrearComprobantePagoOperacion_IN;
import cl.bcs.mesa.MES.CrearComprobantePagoOperacion.CrearComprobantePagoOperacion_OUT;
import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo;
import cl.bcs.plataforma.lim.pojo.ConstantesLimites;
import cl.bcs.plataforma.pla.AgregarMensajeWebsocket;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguienteMultiplesPaises;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente.FechasOut;
import cl.bcs.plataforma.pla.ObtenerDiaSiguienteMultiplesPaises.InObtenerDiaSiguienteMultiplesPaises;
import cl.bcs.plataforma.pla.ObtenerDiaSiguienteMultiplesPaises.OutObtenerDiaSiguienteMultiplesPaises;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.soc.SocioNegocioUtils;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarOperacionForward implements JavaCall{
	
	private final Log log = new Log("FWD.ModificarOperacionForward");
	
	private final ConstantesLimites _cl = new ConstantesLimites();
	
	private final String SQL_SELECT_OPERACIONES_FORWARD = loadResourceToString("cl/bcs/mesa/FWD/sql/ModificarOperacionForward_ConsultarOperacionesForward.sql");
	private final String SQL_SELECT_FORMAS_DE_PAGO_MESA = loadResourceToString("cl/bcs/mesa/sql/ConsultarFormasDePagoMesa.sql");
	private final String SQL_SELECT_MODALIDAD_DE_PAGO = "SELECT \"IdMedioDePagoYModalidadDePago\",\"IdMedioDePago\",\"IdModalidadDePago\",\"IdEstado\" FROM  \"BSCL.Plataforma.BD::PAG.MedioDePagoYModalidadDePago\" WHERE \"IdEstado\" = 'ACT'";
	private final String SQL_UPDATE_OPERACION_MESA = loadResourceToString("cl/bcs/mesa/FWD/sql/ModificarOperacionForward_UpdateOperacionMesa.sql");
	private final String SQL_UPDATE_OPERACION_CARTERA = loadResourceToString("cl/bcs/mesa/FWD/sql/ModificarOperacionForward_UpdateOperacionCartera.sql"); 
	private final String SQL_UPDATE_FORMA_DE_PAGO_ID_OPERACION = loadResourceToString("cl/bcs/mesa/FWD/sql/CrearOperacionForward_UpdateFormaDePagoIdOperacion.sql");
	private final String SQL_SELECT_CONSULTAR_MONEDAS_FERIADO_MONEDA_PRINCIPAL = loadResourceToString("cl/bcs/mesa/SPT/sql/ConsultarMonedasFeriadoMonedaPrincipal.sql");
	
	public static class OperacionForwardObjIn{
		public Long IdOperacion;
		public String FechaOperacion;
		public String IdTipoProducto;
		public Long FolioCartera;
		public Long IdCliente;
		public Long IdPortafolio;
		public Long IdPersonaEjecutivo;
		public BigDecimal MontoOperacion;
		public BigDecimal MontoOperacionInicial;
		public BigDecimal MontoOperacionFinal;
		public BigDecimal TCCierre;
		public BigDecimal ParidadCierre;
		public Long Plazo;
		public Date FechaVencimiento;
		public Date FechaPago;
		public Long IdModalidadLiquidacion;
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
		public Long IdContacto;
		public String TipoDocumento;
		
		public Long IdInterno;
		public String CodigoOperador = "";
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class ModificarOperacionForwardEntrada {
		public Long p_IdEmpresa;
		public Long p_IdPais;
		public Long p_IdUsuario;
		public String p_IdBolsa;
		public Long p_IdSucursal;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public List<OperacionForwardObjIn> p_tablaForward;
		public List<FormaPagoObjIn> p_DatosFormaPago;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ModificarOperacionForwardSalida {
		public List<MensajeSalida>		P_OUT_MENSAJES;
		public Long						P_OUT_ERRORESCONTADOR;
		public Long						P_OUT_ADVERTENCIASCONTADOR;
		public List<OperacionForwardObjOut>	P_OUT_TBLIDFORWARD;
		
		public ModificarOperacionForwardSalida() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_TBLIDFORWARD = new ArrayList<OperacionForwardObjOut>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
		
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		ModificarOperacionForwardEntrada in = gson.fromJson(jin.toString(), ModificarOperacionForwardEntrada.class);
		ModificarOperacionForwardSalida out = logica(con, in);
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
	
	public ModificarOperacionForwardSalida logica(Connection conn, ModificarOperacionForwardEntrada in) throws Exception {
		SQL _sql = new SQL();
		ModificarOperacionForwardSalida out = new ModificarOperacionForwardSalida();
		out.P_OUT_ERRORESCONTADOR = 0L;
		out.P_OUT_ADVERTENCIASCONTADOR = 0L;
		Long Id = 0L;
		HashMap<String,String> mapMsg = new HashMap<>();
		Set<String> codigosRechazoLimiteCtaPropia = new HashSet<>();
		
		log.write("INICIO :: FWD.ModificarOperacionForward");
		
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
			
			/***** VARIABLES *****/
			String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn,in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
			String fechaHoraSistemaSecondate = FechaDB.ObtenerFechaHoraSistemaUTC(conn,in.p_IdEmpresa);
			
			Long cantOpeIn = 0L;
			Long cantOpeVig = 0L;
			Long cantOpePen = 0L;
			
			HashMap<String,Object> params = new HashMap<>();
			
			List<Long> idOperacion = new ArrayList<Long>();
			List<Long> folioCartera = new ArrayList<Long>();
			HashMap<Long,OperacionForwardObjIn> operacionForwardHash = new HashMap<Long,OperacionForwardObjIn>();
			List<String> IdTipoProducto = new ArrayList<String>();
			
			List<EventoAutorizacion> evtList = new ArrayList<CrearOperacionForward.EventoAutorizacion>();
			
			for(OperacionForwardObjIn ope: in.p_tablaForward){
				ope.FechaOperacion = fechaHoraSistemaSecondate;
				
				idOperacion.add(ope.IdOperacion);
				folioCartera.add(ope.FolioCartera);
				IdTipoProducto.add(ope.IdTipoProducto);
				cantOpeIn++;
				
				operacionForwardHash.put(ope.IdOperacion, ope);
			}
			
			//Formas de pago Mesa
			HashMap<String, Object> selectFormaPagoMesaIN = new HashMap<>();
			selectFormaPagoMesaIN.put("IdTipoProducto" , IdTipoProducto);
			List<FormaDePagoMesa> formaDePagoMesaList = _sql.query(conn, SQL_SELECT_FORMAS_DE_PAGO_MESA, selectFormaPagoMesaIN, FormaDePagoMesa.class);
			HashMap<String, Object> formaDePagoMesaHash = Utils.List2Hash(formaDePagoMesaList, "IdFormaDePago");
			
			//Modalidad de pago
			List<ModalidadDePago> modalidadPago = _sql.query(conn, SQL_SELECT_MODALIDAD_DE_PAGO, new HashMap<>(), ModalidadDePago.class);
			HashMap<String, Object> modalidadPagoHash = Utils.List2Hash(modalidadPago, "IdMedioDePago");
			
			//Obtiene datos de las Operaciones Forward a modificar
			params.put("IdEmpresa", in.p_IdEmpresa);
			params.put("IdOperacion", idOperacion);
			params.put("FolioCartera", folioCartera);
			List<OperacionForward> operacionOriginal = _sql.query(conn, SQL_SELECT_OPERACIONES_FORWARD, params, OperacionForward.class);
			
			
			/***** MODIFICACION OPERACION MESA *****/
			_sql.batch(conn, SQL_UPDATE_OPERACION_MESA, in.p_tablaForward, OperacionForwardObjIn.class);			
			/***** MODIFICACION OPERACION CARTERA *****/
            _sql.batch(conn, SQL_UPDATE_OPERACION_CARTERA, in.p_tablaForward, OperacionForwardObjIn.class); 
			
			/***** FORMAS DE PAGO OPERACION FORWARD *****/
			
			Long aplicaFeriadoMonedaPrincipal = 0L;
			SQL sql = new SQL();	
			HashMap<String,Object> parametros = new HashMap<String,Object>();
			parametros.put("IdInstrumento",operacionOriginal.get(0).IdInstrumento);
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
			
			for(OperacionForwardObjIn opeFwd: in.p_tablaForward){
				for(FormaPagoObjIn fpIn: in.p_DatosFormaPago){
					if(opeFwd.IdOperacion.longValue() == fpIn.IdOperacion.longValue()){
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
						comPagIn.FechaIngresoComprobantePagoOperacion = fechaSistema;
						comPagIn.FechaLiquidacionComprobantePagoOperacion = fechaCondicionLiquidacion;
						comPagIn.IdEstado = "VIG";
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
			
			
			/***** MODIFICACION DE FORWARD EN CARTERA *****/
			List<TablaForwardObjIn> operacionModificada  = new ArrayList<TablaForwardObjIn>();
			for(OperacionForward datosOriginales: operacionOriginal){
				for(OperacionForwardObjIn datosNuevos: in.p_tablaForward){
					if(datosOriginales.IdOperacion.longValue() == datosNuevos.IdOperacion.longValue() && datosOriginales.FolioInterno.longValue() == datosNuevos.FolioCartera.longValue()){
						TablaForwardObjIn tfi = new TablaForwardObjIn();
						tfi.FolioInterno = datosOriginales.FolioInterno;
						tfi.IdOperacion = datosOriginales.IdOperacion;
						tfi.FolioCartera = datosOriginales.FolioCartera;
						tfi.IdTipoOperacionOriginal = datosOriginales.IdTipoOperacion;
						tfi.IdTipoOperacionNuevo = "MODFWD";
						tfi.IdTipoInstrumento = datosOriginales.IdTipoInstrumento;
						tfi.IdClasificacionTipoInstrumento = datosOriginales.IdClasificacionTipoInstrumento;
						tfi.IdTipoProducto = datosOriginales.IdTipoProducto;
						tfi.IdInstrumento = datosOriginales.IdInstrumento;
						tfi.Nemotecnico = datosOriginales.Nemotecnico;
						tfi.IdClienteOriginal = datosOriginales.IdCliente;
						tfi.IdClienteNuevo = datosNuevos.IdCliente;
						tfi.IdPortafolioOriginal = datosOriginales.IdPortafolio;
						tfi.IdPortafolioNuevo = datosNuevos.IdPortafolio;
						tfi.IdPersonaEjecutivoOriginal = datosOriginales.IdPersonaEjecutivo;
						tfi.IdPersonaEjecutivoNuevo = datosNuevos.IdPersonaEjecutivo;
						tfi.IdTipoCanalIngreso = datosOriginales.IdTipoCanalIngreso;
						tfi.IdMonedaPrincipal = datosOriginales.IdMonedaPrincipal;	
						tfi.IdMonedaSecundaria = datosOriginales.IdMonedaSecundaria;
						tfi.PrecioVencimiento = BigDecimal.ZERO;
						tfi.MontoOperacionOriginal = datosOriginales.MontoOperacion;
						tfi.MontoOperacionNuevo = datosNuevos.MontoOperacion;
						tfi.MontoInicialOriginal = datosOriginales.MontoInicial;
						tfi.MontoInicialNuevo = datosNuevos.MontoOperacionInicial;
						tfi.MontoFinalOriginal = datosOriginales.MontoFinal;
						tfi.MontoFinalNuevo = datosNuevos.MontoOperacionFinal;
						tfi.FechaOperacion = datosOriginales.FechaOperacion;
						tfi.TCCierreOriginal = datosOriginales.TCCierre;
						tfi.TCCierreNuevo = datosNuevos.TCCierre;
						tfi.ParidadCierreOriginal = datosOriginales.ParidadCierre;
						tfi.ParidadCierreNuevo = datosNuevos.ParidadCierre;
						tfi.PlazoOriginal = datosOriginales.Plazo;
						tfi.PlazoNuevo = datosNuevos.Plazo;
						tfi.FechaVencimientoOriginal = datosOriginales.FechaVencimiento;
						tfi.FechaVencimientoNuevo = datosNuevos.FechaVencimiento;
						tfi.FechaPagoOriginal = datosOriginales.FechaPago;
						tfi.FechaPagoNuevo = datosNuevos.FechaPago;
						tfi.IdModalidadLiquidacionOriginal = datosOriginales.IdModalidadLiquidacion;
						tfi.IdModalidadLiquidacionNuevo = datosNuevos.IdModalidadLiquidacion;
						tfi.IdReferenciaMercado = datosOriginales.IdReferenciaMercado;
						tfi.IdPlaza = datosOriginales.IdPlaza;
						tfi.TCCostoOriginal = datosOriginales.TCCosto;
						tfi.TCCostoNuevo = datosNuevos.TCCosto;
						tfi.PuntosTasasForwardOriginal = datosOriginales.PuntosTasasForward;
						tfi.PuntosTasasForwardNuevo = datosNuevos.PuntosTasasForward;
						tfi.PuntosForwardOriginal = datosOriginales.PuntosForward;
						tfi.PuntosForwardNuevo = datosNuevos.PuntosForward;
						tfi.TCForwardOriginal = datosOriginales.TCForward;
						tfi.TCForwardNuevo = datosNuevos.TCForward;
						tfi.TasasMonedaPrincipalOriginal = datosOriginales.TasasMonedaPrincipal;
						tfi.TasasMonedaPrincipalNuevo = datosNuevos.TasasMonedaPrincipal;
						tfi.TasasMonedaSecundariaOriginal = datosOriginales.TasasMonedaSecundaria;
						tfi.TasasMonedaSecundariaNuevo = datosNuevos.TasasMonedaSecundaria;
						tfi.MontoDiferidoOriginal = datosOriginales.MontoDiferido;
						tfi.MontoDiferidoNuevo = datosNuevos.MontoDiferido;
						tfi.MontoMargenOriginal = datosOriginales.MontoMargen;
						tfi.MontoMargenNuevo = datosNuevos.MontoMargen;
						tfi.IdFixingOriginal = datosOriginales.IdFixing;
						tfi.IdFixingNuevo = datosNuevos.IdFixing;
						tfi.FechaFixingOriginal = datosOriginales.FechaFixing;
						tfi.FechaFixingNuevo = datosNuevos.FechaFixing;
						tfi.HoraFixingOriginal = datosOriginales.HoraFixing;
						tfi.HoraFixingNuevo = datosNuevos.HoraFixing;
						tfi.IdMesa = datosOriginales.IdMesa;
						tfi.IdSpotAsociado = datosOriginales.IdSpotAsociado;
						tfi.Observaciones = datosOriginales.Observaciones;
						tfi.IdEstado = "MOD";
						tfi.TasaDescuento = BigDecimal.ZERO;
						tfi.TipoCambioAnticipo = BigDecimal.ZERO;
						tfi.MontoARecibirAnticipo = BigDecimal.ZERO;
						tfi.TipoCambioObservado = BigDecimal.ZERO;
						
						operacionModificada.add(tfi);
					}
				}
			}
						
			/***** LIMITES *****/
			Long autCont = 0L;
			Long idInternoForward = 1L;
			for(TablaForwardObjIn opeFwd : operacionModificada) {
				opeFwd.IdInterno = idInternoForward++;
				opeFwd.CodigoOperador = empleadoUsuario.CodigoEmpleado;
			}
			
			ValidarLimitesCuentaPropiaForward.SalidaValidarLimitesCuentaPropiaForward salidaValidarLimitesCuentaPropiaForward = validarLimitesCuentaPropia(conn, in, operacionModificada);
			
			if(salidaValidarLimitesCuentaPropiaForward.P_OUT_VALIDACIONES.size() > 0) {
				AtomicLong totalAutLimCtaPropia = new AtomicLong(0L);
				Map<Long, Validacion> validacionLimCtaPropiaPorIdInterno = salidaValidarLimitesCuentaPropiaForward.P_OUT_VALIDACIONES.stream().collect(Collectors.toMap(u -> u.IdInternoOperacion, u -> u));
				operacionModificada = operacionModificada.stream()
						.map(operacionFwd -> Pair.of(operacionFwd, validacionLimCtaPropiaPorIdInterno.get(operacionFwd.IdInterno)))
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
									TablaForwardObjIn operacionForward = parOpeValid.getLeft();
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
			
			if (operacionModificada.isEmpty()) {
				return out;
			}
			
			if(autCont > 0L) {
				CrearEventoAutorizacionMasivo eventoAutorizacion = new CrearEventoAutorizacionMasivo();
				CrearEventoAutorizacionMasivo.ServiceEntrada entradaEventoAutorizacion = new CrearEventoAutorizacionMasivo.ServiceEntrada();
				entradaEventoAutorizacion.P_IDEMPRESA = in.p_IdEmpresa;
				entradaEventoAutorizacion.P_IDMODULO = in.p_IdModulo;
				entradaEventoAutorizacion.P_IDUSUARIO = in.p_IdUsuario;
				entradaEventoAutorizacion.P_SIMULAROPERACION = "N";
				entradaEventoAutorizacion.P_USAREGLASNEGOCIO = "N";
				
				for(EventoAutorizacion evt: evtList) {
					for(TablaForwardObjIn opeFwd : operacionModificada) {
						if(evt.NumeroOperacion == opeFwd.FolioInterno) {
							
							
							CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS evento = new CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS();
							evento.IdTipoEvento = evt.IdTipoEvento;
							evento.IdModuloSolicita = in.p_IdModulo;
							evento.IdUsuarioSolicita = in.p_IdUsuario;
							evento.Fecha = Fecha.toStringDate(opeFwd.FechaOperacion);
							evento.IdTipoOperacion = opeFwd.IdTipoOperacionNuevo;
							evento.IdConcepto = "LIMFWD";
							evento.NumeroOperacionConcepto = opeFwd.IdOperacion.toString();
							evento.FechaOperacionConcepto = "";
							evento.IdCliente = opeFwd.IdClienteNuevo;
							evento.IdFuncionario = 0L;
							evento.IdProveedor = 0L;
							evento.IdTipoProducto = opeFwd.IdTipoProducto;
							evento.IdClasificacionTipoInstrumento = "FW";
							evento.IdTipoInstrumento = "FWD";
							evento.Nemo = opeFwd.Nemotecnico;
							evento.Cantidad = 0L;
							evento.Monto = MonedaFormato.formatear(conn, opeFwd.MontoOperacionNuevo, "MONTO", opeFwd.IdMonedaPrincipal).valor;
							evento.Precio = opeFwd.TCCierreNuevo;
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
			
			log.write("MESA FORWARD :: Llamada Ingresar Forward Cartera");
			IngresarForwardCartera ingresarForwardCartera = new IngresarForwardCartera();
			IngresarForwardCarteraEntrada ingresarForwardCarteraIn = new IngresarForwardCarteraEntrada();
			ingresarForwardCarteraIn.p_IdEmpresa = in.p_IdEmpresa;
			ingresarForwardCarteraIn.p_IdPais = in.p_IdPais;
			ingresarForwardCarteraIn.p_IdUsuario = in.p_IdUsuario;
			ingresarForwardCarteraIn.p_IdBolsa = in.p_IdBolsa;
			ingresarForwardCarteraIn.p_IdSucursal = in.p_IdSucursal;
			ingresarForwardCarteraIn.p_IdModulo = in.p_IdModulo;
			ingresarForwardCarteraIn.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
			ingresarForwardCarteraIn.p_tablaForward = operacionModificada;
			IngresarForwardCarteraSalida ingresarForwardCarteraOut = ingresarForwardCartera.logica(conn, ingresarForwardCarteraIn);
			
			out.P_OUT_ERRORESCONTADOR = out.P_OUT_ERRORESCONTADOR + ingresarForwardCarteraOut.P_OUT_ERRORESCONTADOR;
			out.P_OUT_MENSAJES.addAll(ingresarForwardCarteraOut.P_OUT_MENSAJES);
			if (MensajeSalida.getNumeroErrores(ingresarForwardCarteraOut.P_OUT_MENSAJES) > 0){
				return out;
			}
			
			
			/***** SALIDA *****/
			out.P_OUT_ERRORESCONTADOR = out.P_OUT_ERRORESCONTADOR + MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
			out.P_OUT_TBLIDFORWARD = new ArrayList<OperacionForwardObjOut>();
			for(OperacionForward opeFwd: operacionOriginal){
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
	
	private ValidarLimitesCuentaPropiaForward.SalidaValidarLimitesCuentaPropiaForward validarLimitesCuentaPropia(Connection conn, ModificarOperacionForwardEntrada entrada, List<TablaForwardObjIn> operaciones) throws Exception{
		
		log.write("INICIO::ModificarOperacionForward.validarLimitesCuentaPropia");
		long inicio = System.currentTimeMillis();
		
		ValidarLimitesCuentaPropiaForward.EntradaValidarLimitesCuentaPropiaForward entradaValidarLimitescuentaPropiaForward = new ValidarLimitesCuentaPropiaForward.EntradaValidarLimitesCuentaPropiaForward();
		entradaValidarLimitescuentaPropiaForward.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaValidarLimitescuentaPropiaForward.p_IdTipoProceso = _cl.PROCESO_MODIFICACION;
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
		
		long fin = System.currentTimeMillis();
		log.write("FIN::ModificarOperacionForward.validarLimitesCuentaPropia {}", fin-inicio);
		return salidaValidarLimitesCuentaPropiaForward;
		
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String jsonDesa = "{\"p_IdEmpresa\":1,\"p_IdPais\":44,\"p_IdUsuario\":6,\"p_IdBolsa\":\"BCS\",\"p_IdSucursal\":0,\"p_IdModulo\":\"MFW\",\"p_UsaReglasNegocio\":\"S\",\"p_tablaForward\":[{\"IdOperacion\":2783,\"IdTipoProducto\":\"MEFW\",\"FolioCartera\":20692353,\"IdCliente\":1423323,\"IdPortafolio\":1450339,\"IdPersonaEjecutivo\":621867,\"MontoOperacion\":500,\"MontoOperacionInicial\":345000,\"MontoOperacionFinal\":0,\"TCCierre\":0.3,\"ParidadCierre\":0,\"Plazo\":1,\"FechaVencimiento\":\"2019-12-19\",\"FechaPago\":\"2019-12-19\",\"IdModalidadLiquidacion\":6,\"TCCosto\":690,\"PuntosTasasForward\":\"S\",\"PuntosForward\":0,\"TCForward\":690,\"TasasMonedaPrincipal\":0,\"TasasMonedaSecundaria\":0,\"MontoDiferido\":-344850,\"MontoMargen\":344850,\"IdFixing\":37,\"FechaFixing\":\"0001-01-01\",\"HoraFixing\":\"00:00:00\",\"PagaConCtaInv\":\"S\"}],\"p_DatosFormaPago\":[{\"IdInternoPagoOperacion\":1,\"IdComprobantePagoOperacion\":0,\"IdTipoMovimiento\":\"MOVEGR\",\"IdFormaDePago\":\"COMFWD_CIN_USD_1\",\"IdOperacion\":2783,\"RecibePagaPortafolio\":\"R\",\"IdMoneda\":\"USD\",\"IdMonedaLiquidacion\":\"USD\",\"MontoFormaDePago\":250,\"AfectarTesoreria\":\"N\"},{\"IdInternoPagoOperacion\":2,\"IdComprobantePagoOperacion\":0,\"IdTipoMovimiento\":\"MOVEGR\",\"IdFormaDePago\":\"COMFWD_CHE_USD_1\",\"IdOperacion\":2783,\"RecibePagaPortafolio\":\"R\",\"IdMoneda\":\"USD\",\"IdMonedaLiquidacion\":\"USD\",\"MontoFormaDePago\":250,\"AfectarTesoreria\":\"S\"}]}";
			//String jsonMaBa = "{\"p_IdEmpresa\":1,\"p_IdPais\":44,\"p_IdUsuario\":33,\"p_IdBolsa\":\"BCS\",\"p_IdSucursal\":0,\"p_IdModulo\":\"MFW\",\"p_UsaReglasNegocio\":\"S\",\"p_tablaForward\":[{\"FolioInterno\":1,\"IdTipoOperacion\":\"COMFWD\",\"IdTipoInstrumento\":\"FWD\",\"IdTipoProducto\":\"MEFW\",\"IdInstrumento\":346370,\"Nemotecnico\":\"SECANO\",\"IdCliente\":123569,\"IdPortafolio\":27499,\"IdPersonaEjecutivo\":10003,\"IdTipoCanalIngreso\":\"MES\",\"IdMonedaPrincipal\":\"USD\",\"IdMonedaSecundaria\":\"CLP\",\"MontoOperacion\":1000,\"MontoInicial\":0,\"MontoFinal\":0,\"FechaOperacion\":\"2018-06-20\",\"TCCierre\":620,\"ParidadCierre\":0,\"Plazo\":1,\"FechaVencimiento\":\"2018-06-21\",\"FechaPago\":\"2018-06-21\",\"IdModalidadLiquidacion\":6,\"IdReferenciaMercado\":0,\"IdPlaza\":2931,\"TCCosto\":600,\"PuntosTasasForward\":\"S\",\"PuntosForward\":0,\"TCForward\":600,\"TasasMonedaPrincipal\":0,\"TasasMonedaSecundaria\":0,\"MontoDiferido\":20000,\"MontoMargen\":-20000,\"IdFixing\":37,\"FechaFixing\":\"0001-01-01\",\"HoraFixing\":\"20:50:24\",\"IdMesa\":2,\"IdSpotAsociado\":0,\"IdClasificacionCartera\":11,\"Observaciones\":\"\",\"IdEstado\":\"VIG\",\"PagaConCtaInv\":\"S\"}],\"p_DatosFormaPago\":[{\"IdInternoPagoOperacion\":1,\"IdComprobantePagoOperacion\":0,\"IdTipoMovimiento\":\"MOVEGR\",\"IdFormaDePago\":\"COMFWD_CIN_USD_0\",\"IdOperacion\":1,\"RecibePagaPortafolio\":\"R\",\"IdMoneda\":\"USD\",\"IdMonedaLiquidacion\":\"USD\",\"MontoFormaDePago\":1000,\"AfectarTesoreria\":\"N\"}],\"p_tablaCalce\":[]}";
			//String jsonDesaEF = "{\"p_IdEmpresa\":1,\"p_IdPais\":44,\"p_IdUsuario\":33,\"p_IdBolsa\":0,\"p_IdSucursal\":0,\"p_IdModulo\":\"MFW\",\"p_UsaReglasNegocio\":\"S\",\"p_tablaForward\":[{\"FolioInterno\":1,\"IdTipoOperacion\":\"COMFWD\",\"IdTipoInstrumento\":\"FWD\",\"IdTipoProducto\":\"MEFW\",\"IdInstrumento\":346370,\"Nemotecnico\":\"SECANO\",\"IdCliente\":130826,\"IdPortafolio\":34993,\"IdPersonaEjecutivo\":1335561,\"IdTipoCanalIngreso\":\"MES\",\"IdMonedaPrincipal\":\"USD\",\"IdMonedaSecundaria\":\"CLP\",\"MontoOperacion\":1000,\"MontoInicial\":0,\"MontoFinal\":0,\"FechaOperacion\":\"2018-06-20\",\"TCCierre\":625,\"ParidadCierre\":0,\"Plazo\":1,\"FechaVencimiento\":\"2018-06-21\",\"FechaPago\":\"0001-01-01\",\"IdModalidadLiquidacion\":7,\"IdReferenciaMercado\":0,\"IdPlaza\":2931,\"TCCosto\":600,\"PuntosTasasForward\":\"S\",\"PuntosForward\":0,\"TCForward\":600,\"TasasMonedaPrincipal\":0,\"TasasMonedaSecundaria\":0,\"MontoDiferido\":25000,\"MontoMargen\":-25000,\"IdFixing\":37,\"FechaFixing\":\"0001-01-01\",\"HoraFixing\":\"23:03:21\",\"IdMesa\":2,\"IdSpotAsociado\":0,\"IdClasificacionCartera\":11,\"Observaciones\":\"\",\"IdEstado\":\"VIG\",\"PagaConCtaInv\":\"N\"}],\"p_DatosFormaPago\":[{\"IdInternoPagoOperacion\":1,\"IdComprobantePagoOperacion\":0,\"IdTipoMovimiento\":\"MOVING\",\"IdFormaDePago\":\"VENFWD_EFE_CLP_0\",\"IdOperacion\":1,\"RecibePagaPortafolio\":\"P\",\"IdMoneda\":\"CLP\",\"IdMonedaLiquidacion\":\"CLP\",\"MontoFormaDePago\":625000,\"AfectarTesoreria\":\"S\"},{\"IdInternoPagoOperacion\":2,\"IdComprobantePagoOperacion\":0,\"IdTipoMovimiento\":\"MOVEGR\",\"IdFormaDePago\":\"COMFWD_EFE_USD_0\",\"IdOperacion\":1,\"RecibePagaPortafolio\":\"R\",\"IdMoneda\":\"USD\",\"IdMonedaLiquidacion\":\"USD\",\"MontoFormaDePago\":1000,\"AfectarTesoreria\":\"S\"}],\"p_tablaCalce\":[]}";
			ModificarOperacionForwardEntrada Objin = gson.fromJson(jsonDesa, ModificarOperacionForwardEntrada.class);
			
			ModificarOperacionForward demo = new ModificarOperacionForward();				
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			try {
 				ModificarOperacionForwardSalida ObjOut = demo.logica(con, Objin);
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