package cl.bcs.fondomutuo;

import java.math.BigDecimal;
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
import cl.bcs.custodia.utils.IngresoCustodia;
import cl.bcs.custodia.utils.RetiroCustodia;
import cl.bcs.fondomutuo.CrearSolicitudAGF.EntradaCrearSolicitudAGF;
import cl.bcs.fondomutuo.CrearSolicitudAGF.SalidaCrearSolicitudAGF;
import cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo;
import cl.bcs.fondomutuo.pojo.SolicitudAporteRescate;
import cl.bcs.mesa.SPT.AnularOperacionSpot;
import cl.bcs.mesa.SPT.AnularOperacionSpot.AnularOperacionSpotEntrada;
import cl.bcs.mesa.SPT.AnularOperacionSpot.AnularOperacionSpotSalida;
import cl.bcs.mesa.SPT.CrearOperacionSpot.OperacionSpot;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.NotificarEstadoOrdenesOWP;
import cl.bcs.plataforma.pla.NotificarEstadoOrdenesOWP.NotificarEstadoOrdenes_Entrada;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class AnulacionSolicitudAporteRescate implements JavaCall {
	
	private final Log log = new Log("FM.AnulacionSolicitudAporteRescate");
	
	private final String SQL_SELECT_SOLICITUDES = loadResourceToString("cl/bcs/fondomutuo/sql/AnulacionSolicitudAporteRescate_SelectSolicitudes.sql");
	private final String SQL_SELECT_CARTA_LIQUIDACION = loadResourceToString("cl/bcs/fondomutuo/sql/AnulacionSolicitudAporteRescate_SelectCartaLiquidacion.sql");
	private final String SQL_SELECT_INGRESO_EGRESO = loadResourceToString("cl/bcs/fondomutuo/sql/AnulacionSolicitudAporteRescate_SelectIngresoEgreso.sql");
	private final String SQL_SELECT_CARTERAS_APORTES = loadResourceToString("cl/bcs/fondomutuo/sql/AnulacionSolicitudAporteRescate_SelectCarteraAportes.sql");
	private final String SQL_SELECT_CARTERAS_RESCATES = loadResourceToString("cl/bcs/fondomutuo/sql/AnulacionSolicitudAporteRescate_SelectCarteraRescates.sql");
	private final String SQL_UPDATE_SOLICITUD = loadResourceToString("cl/bcs/fondomutuo/sql/AnulacionSolicitudAporteRescate_UpdateSolicitudes.sql");
	private final String SQL_SELECT_SOLICITUDES_MODIFICADAS = loadResourceToString("cl/bcs/fondomutuo/sql/CertificarSolicitudAporteRescate_SelectSolicitudes.sql");
	private final String SQL_SELECT_OPERACION_SPOT = loadResourceToString("cl/bcs/fondomutuo/sql/AnulacionSolicitudAporteRescate_SelectOperacionSpot.sql");
	
	private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();
	
	public static class EntradaAnulacionSolicitudAporteRescate {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public List<InSolicitudes> p_Solicitudes;
		
		public EntradaAnulacionSolicitudAporteRescate(){
			this.p_Solicitudes = new ArrayList<InSolicitudes>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InSolicitudes{
		
		public Long IdSolicitudAporteRescate;
		public String Estado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaAnulacionSolicitudAporteRescate {
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		public List<ResultadoAnulacion> P_SOLICITUDES;
		
		public SalidaAnulacionSolicitudAporteRescate(){
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_SOLICITUDES = new ArrayList<ResultadoAnulacion>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ResultadoAnulacion{
		
		public Long IdSolicitudAporteRescate;
		public String Codigo; 
		public String Descripcion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SolicitudProceso{
		
		public Long IdSolicitudAporteRescate;
		public Long IdSolicitudAporteRescateRelacionado;
		public String EstadoSolicitud;
		public String EstadoLiquidacion;
		public String ValorParametro;
		public String NombreParametro;
		public String IdMedioDePago;
		public Long FolioCartaLiquidacion;
		public Date FechaCertificacion;
		public Date FechaAnulacion;
		public String SolicitudBatch;
		public String TipoSolicitud;
		public String IdTipoMovimientoFondoMutuo;
		public String EstadoAnulacion;
		public String TieneCertificacion;
		public Long IdPortafolio;
		public String IdMonedaFondo;
		public BigDecimal MontoPago;
		public Long IdEmpleadoEjecutivo;
		public BigDecimal ParidadFechaCertificacion;
		public Long IdInstrumento;
		public String IdTipoInstrumento;
		public String IdSerieFondo;
		public String IdMoneda;
		public BigDecimal MontoPagoLocal;
		public String IndicadorMLT;
		public BigDecimal MontoComisionLocal;
		public BigDecimal NumeroCuotas;
		public Long IdCliente;
		public Long IdCartera;
		public String IdEstadoNuevo;
		public String EstadoProceso;
		public String Codigo;
		public String IdTipoCanalIngreso;
		public Long FolioOmnibus;
		
		public SolicitudProceso(){
			this.EstadoProceso = "PEN";
			this.Codigo = "";
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class MovSpot{
		public Long IdOperacionSpot;
		public Long IdSolicitudAporteRescate;
		public String Estado;
		public String EstadoConfirmacion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	} 
	
	public static class MovTesoreria{
		public Long IdSolicitudAporteRescate;
		public Long IdComprobanteOrigen;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class MovCuentaInversion{
		public Long IdSolicitudAporteRescate;
		public Long IdCargoAbonoMovimiento;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Carteras{
		public Long IdOperacion;
		public Long FolioCartera;
		public Long IdCartera;
		public Long IdCliente;
		public Long IdPortafolio;
		public BigDecimal INGCantidad;
		public BigDecimal Cantidad;
		public BigDecimal Monto;
		public String IdEstado;
		public Long IdInstrumento;
		public String IdEmisor;
		public String IdRegimenFondoMutuo;
		public String GeneraNuevoMovimiento;
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public Carteras() {
			this.GeneraNuevoMovimiento = "S";
		}
	}
	
	
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaAnulacionSolicitudAporteRescate jin = gson.fromJson(in.toString(), EntradaAnulacionSolicitudAporteRescate.class);
		SalidaAnulacionSolicitudAporteRescate out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaAnulacionSolicitudAporteRescate logica (Connection con, EntradaAnulacionSolicitudAporteRescate entrada) throws Exception {
		log.write("INICIO::AnularSolicitudAporteRescate");
		SalidaAnulacionSolicitudAporteRescate out = new SalidaAnulacionSolicitudAporteRescate();
		SQL sql=new SQL();
		
		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo);
		if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
			return out;
		}
		
		if(entrada.p_Solicitudes.size() == 0){
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_INGRESARON_SOLICITUDES",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
			return out;
		}
		
		List<Long> idSolicitudes = new ArrayList<Long>();
		for (InSolicitudes mov: entrada.p_Solicitudes) {
			if (idSolicitudes.contains(mov.IdSolicitudAporteRescate)) {
				out.P_OUT_ERRORESCONTADOR = 1L;
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"SOLICITUD_DUPLICADA",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
				return out;
			} else {
				idSolicitudes.add(mov.IdSolicitudAporteRescate);
			}
		}
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		HashMap<Long,Object> hsolicitudes = Utils.List2Hash(entrada.p_Solicitudes, "IdSolicitudAporteRescate"); 
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		params.put("IdSolicitudAporteRescate", idSolicitudes);
		List<SolicitudProceso> solicitudes = sql.query(con, SQL_SELECT_SOLICITUDES, params, SolicitudProceso.class);
		
		for (HashMap.Entry<Long, Object> entry : hsolicitudes.entrySet()) {
			InSolicitudes inSol = (InSolicitudes) entry.getValue();
			SolicitudProceso inSolicitud = solicitudes.stream().filter(p -> p.IdSolicitudAporteRescate.compareTo(entry.getKey()) == 0).findFirst().orElse(null);
			if (inSolicitud != null) {
				solicitudes.removeIf(p -> {
					if(p.IdSolicitudAporteRescate.compareTo(inSolicitud.IdSolicitudAporteRescate) == 0) {
						p.IdEstadoNuevo = inSol.Estado;
						return false;
					}
					else {
						if(p.TipoSolicitud.equals(_c.TipoSolicitudAporte) && inSol.Estado.equals("ING")) {
							if(inSolicitud.TipoSolicitud.equals(_c.TipoSolicitudRescate)) {
								p.IdEstadoNuevo = "ANU";
							}
							else {
								p.IdEstadoNuevo = inSol.Estado;
							}
							return false;
						}
						if(inSol.Estado.equals("ANU")) {
							p.IdEstadoNuevo = inSol.Estado;
							return false;
						}
						else {
							return true;
						}
					}
				});
			}
		}
		
		idSolicitudes = solicitudes.stream().map(p -> p.IdSolicitudAporteRescate).collect(Collectors.toList());

		params.clear();
		params.put("IdSolicitudAporteRescate", idSolicitudes);
		List<MovTesoreria> cartas = sql.query(con, SQL_SELECT_CARTA_LIQUIDACION, params, MovTesoreria.class);
		
		params.clear();
		params.put("IdSolicitudAporteRescate", idSolicitudes);
		List<MovTesoreria> ingresosEgresos = sql.query(con, SQL_SELECT_INGRESO_EGRESO, params, MovTesoreria.class);
		
		params.clear();
		
		params.put("IdSolicitudAporteRescate", idSolicitudes);
		List<MovSpot> movimientosSpot = sql.query(con, SQL_SELECT_OPERACION_SPOT, params, MovSpot.class);
		
		params.clear();
		
		HashMap<Long,Object> hCartas = Utils.List2Hash(cartas, "IdSolicitudAporteRescate");
		HashMap<Long,Object> hIngresosEgresos = Utils.List2Hash(ingresosEgresos, "IdSolicitudAporteRescate");
		HashMap<Long,Object> hOperacionSpot = Utils.List2Hash(movimientosSpot, "IdSolicitudAporteRescate");
		
		//VALIDACIONES
		Long c = 0L;
		for(SolicitudProceso sol : solicitudes){
			InSolicitudes inSol = (InSolicitudes) hsolicitudes.get(sol.IdSolicitudAporteRescate);
			if(inSol == null) {
				inSol = (InSolicitudes) hsolicitudes.get(sol.IdSolicitudAporteRescateRelacionado);
			}
			if(_c.EstadoAnulado.equals(sol.EstadoSolicitud)){
				sol.EstadoProceso = _c.EstadoRechazado;
				sol.Codigo = "UI_APORTE_RESCATE_ANULADA";
				c++;
				continue;
			}
			if(_c.EstadoIngresado.equals(inSol.Estado) && _c.NO.equals(sol.TieneCertificacion)){
				sol.EstadoProceso = _c.EstadoRechazado;
				sol.Codigo = "UI_OPERACION_NO_DESMARCAR";
				c++;
				continue;
			}
			if(_c.EstadoIngresado.equals(inSol.Estado) && _c.EstadoPendiente.equals(sol.EstadoSolicitud)  && _c.TipoSolicitudAporte.equals(sol.TipoSolicitud)){
				sol.EstadoProceso = _c.EstadoRechazado;
				sol.Codigo = "UI_APORTE_DESMARCAR_PENDIENTE";
				c++;
				continue;
			}
			if(_c.EstadoIngresado.equals(inSol.Estado) && _c.EstadoIngresado.equals(sol.EstadoSolicitud) && inSol.IdSolicitudAporteRescate.compareTo(sol.IdSolicitudAporteRescate) == 0){
				sol.EstadoProceso = _c.EstadoRechazado;
				sol.Codigo = "UI_APORTE_RESCATE_INGRESADO";
				c++;
				continue;
			}
			if(hCartas.get(sol.IdSolicitudAporteRescate) != null){
				sol.EstadoProceso = _c.EstadoRechazado;
				sol.Codigo = "FOLIO_COMPROBANTE_LIQUIDACION_ASOCIADO";
				c++;
				continue;
			}
			if((hIngresosEgresos.get(sol.IdSolicitudAporteRescate) != null) && _c.TipoSolicitudAporte.equals(sol.TipoSolicitud) && _c.EstadoAnulado.equals(inSol.Estado)){
				sol.EstadoProceso = _c.EstadoRechazado;
				sol.Codigo = "APORTE_CON_DOCUMENTO_ASOCIADO";
				c++;
				continue;
			}
			
			if((hIngresosEgresos.get(sol.IdSolicitudAporteRescate) != null) && (_c.TipoSolicitudRescate.equals(sol.TipoSolicitud) && _c.EstadoCertificado.equals(sol.EstadoSolicitud))){
				sol.EstadoProceso = _c.EstadoRechazado;
				sol.Codigo = "RESCATE_CON_DOCUMENTO_ASOCIADO";
				c++;
				continue;
			}
			if(_c.EstadoCertificado.equals(sol.EstadoSolicitud) && (_c.TipoSolicitudAporte.equals(sol.TipoSolicitud) 
					|| (_c.Transferencia.equals(sol.IdTipoMovimientoFondoMutuo) || _c.CambioSerie.equals(sol.IdTipoMovimientoFondoMutuo) || _c.TraspasoPortafolio.equals(sol.IdTipoMovimientoFondoMutuo))) 
				&& sol.IdCartera.longValue() == 0L){
				sol.EstadoProceso = _c.EstadoRechazado;
				sol.Codigo = "APORTE_SIN_CARTERA";
				c++;
				continue;
			}
			if(sol.FolioOmnibus > 0) {
				sol.EstadoProceso = _c.EstadoRechazado;
				sol.Codigo = "ANULACION_OMNIBUS_CREADO";
				c++;
				continue;
			}
			sol.IdEstadoNuevo = sol.IdEstadoNuevo == null ? inSol.Estado : sol.IdEstadoNuevo;
			sol.FechaAnulacion = Fecha.toDate(fechaSistema);
		}
		
		if(c > 0) {
			out.P_OUT_ERRORESCONTADOR = c;
			Long d = 0L;
			for(SolicitudProceso sol : solicitudes) {
				if(!sol.Codigo.equals("")) {
					HashMap<String,String> parametrosMensaje = new HashMap<String,String>();
					parametrosMensaje.put("IdSolicitudAporteRescate", String.valueOf(sol.IdSolicitudAporteRescate));
					out.P_OUT_MENSAJES.add(new MensajeSalida(sol.IdSolicitudAporteRescate,MensajeSalida.MENSAJE_ERROR,sol.Codigo,parametrosMensaje,con,entrada.p_IdEmpresa));
					d++;
				}
			}
			return out;
		}
		
		List<SolicitudProceso> solicitudesCuentaInversion = new ArrayList<SolicitudProceso>();
		
		//MOVIMIENTO CONTRAPARTIDA CUENTA INVERSION
		for(SolicitudProceso sol : solicitudes){
			InSolicitudes inSol = (InSolicitudes) hsolicitudes.get(sol.IdSolicitudAporteRescate);
			if(inSol == null) {
				inSol = (InSolicitudes) hsolicitudes.get(sol.IdSolicitudAporteRescateRelacionado);
			}
			if(_c.EstadoRechazado.equals(sol.EstadoProceso) || !_c.EstadoCertificado.equals(sol.EstadoSolicitud)) continue;
			
			solicitudesCuentaInversion.add(sol);
		}
		
		if(solicitudesCuentaInversion.size() > 0){
			CrearCargoAbonoSalida salidaCrearCargoAbono = mueveCuentaInversion(con,solicitudesCuentaInversion,entrada,fechaSistema);
			out.P_OUT_ERRORESCONTADOR += salidaCrearCargoAbono.P_OUT_ERRORESCONTADOR;
			for(MensajeSalida mje : salidaCrearCargoAbono.P_OUT_MENSAJES){
				if(MensajeSalida.MENSAJE_ERROR.equals(mje.tipo)){
					Long id = Math.abs(mje.id);
					out.P_OUT_MENSAJES.add(new MensajeSalida(id, mje.tipo, mje.codigo, mje.descripcion));
					for(SolicitudProceso sol : solicitudes){
						if(id.longValue() == sol.IdSolicitudAporteRescate.longValue()){
							sol.EstadoProceso = _c.EstadoRechazado;
							sol.Codigo = mje.codigo;
							break;
						}
					}
				}
			}
		}
		
		
		//MOVIMIENTO CUSTODIA
		
		//SEPARAMOS RESCATES CON APORTES
		List<SolicitudProceso> aportes = new ArrayList<SolicitudProceso>();
		List<Long> idAportes = new ArrayList<Long>(); 
		List<SolicitudProceso> rescates = new ArrayList<SolicitudProceso>();
		List<Long> idRescates = new ArrayList<Long>();
		
		for(SolicitudProceso sol : solicitudes){
			if(_c.EstadoRechazado.equals(sol.EstadoProceso) || !_c.EstadoCertificado.equals(sol.EstadoSolicitud)) continue;
			if(_c.TipoSolicitudAporte.equals(sol.TipoSolicitud) 
					|| (_c.Transferencia.equals(sol.IdTipoMovimientoFondoMutuo) || _c.CambioSerie.equals(sol.IdTipoMovimientoFondoMutuo) || _c.TraspasoPortafolio.equals(sol.IdTipoMovimientoFondoMutuo))){ 
				aportes.add(sol);
				idAportes.add(sol.IdCartera);
			}
			if(_c.TipoSolicitudRescate.equals(sol.TipoSolicitud)){
				rescates.add(sol);
				idRescates.add(sol.IdSolicitudAporteRescate);
			}
		}
		
		HashMap<Long,Object> hcarteras = new HashMap<Long,Object>();
		List<RetiroCustodia.ObjDatosEntrada> p_RetiroCartera = new ArrayList<RetiroCustodia.ObjDatosEntrada>();
		List<IngresoCustodia.ObjDatosEntrada> p_IngresoCartera = new ArrayList<IngresoCustodia.ObjDatosEntrada>();
		if(aportes.size() > 0){
			params.clear();
			params.put("IdCartera", idAportes);
			List<Carteras> carteras = sql.query(con, SQL_SELECT_CARTERAS_APORTES, params, Carteras.class);
			hcarteras.putAll(Utils.List2Hash(carteras, "IdCartera"));
			for(SolicitudProceso sol : aportes){
				Carteras cartera = (Carteras) hcarteras.get(sol.IdCartera);
				if(cartera != null){
					RetiroCustodia.ObjDatosEntrada	ObjEntrada = new RetiroCustodia.ObjDatosEntrada();
		    		ObjEntrada.IdGenerarMovimientoCustodia			= cartera.IdCartera;
		    		ObjEntrada.TablaAfectada 						= "CARTERA";
					ObjEntrada.AccionOperacion						= "ANULACION";		
					ObjEntrada.IdCliente							= cartera.IdCliente;
					ObjEntrada.IdPortafolio							= cartera.IdPortafolio;
					ObjEntrada.FolioCustodia						= cartera.FolioCartera;
					ObjEntrada.IdCustodia							= cartera.IdCartera;
					ObjEntrada.IdOperacion							= cartera.IdOperacion;
					ObjEntrada.IdConcepto							= _c.ConceptoAnulacionAporte;
					ObjEntrada.Cantidad								= (_c.Transferencia.equals(sol.IdTipoMovimientoFondoMutuo) 
																		|| _c.CambioSerie.equals(sol.IdTipoMovimientoFondoMutuo) 
																		|| _c.TraspasoPortafolio.equals(sol.IdTipoMovimientoFondoMutuo)) ? cartera.Cantidad : sol.NumeroCuotas;
					ObjEntrada.MontoCompraMonedaInstrumento			= cartera.Monto;
					ObjEntrada.IdInstrumento						= cartera.IdInstrumento;
					ObjEntrada.ValorRescate							= BigDecimal.ZERO;
					ObjEntrada.FechaOperacion						= Fecha.toDate(fechaSistema);
					ObjEntrada.IdEmisor								= cartera.IdEmisor;
					ObjEntrada.IdTipoOperacion						= _c.TipoOperacionAnulacionAporteFondoMutuo;
					p_RetiroCartera.add(ObjEntrada);
				}
			}
			
		}
		
		if(rescates.size() > 0){
			params.clear();
			params.put("IdSolicitudAporteRescate", idRescates);
			List<Carteras> carteras = sql.query(con, SQL_SELECT_CARTERAS_RESCATES, params, Carteras.class);
			hcarteras.putAll(Utils.List2Hash(carteras, "IdCartera"));
			for(Carteras cartera : carteras){
				if(_c.EstadoVigente.equals(cartera.IdEstado)){
					RetiroCustodia.ObjDatosEntrada	ObjEntrada = new RetiroCustodia.ObjDatosEntrada();
		    		ObjEntrada.IdGenerarMovimientoCustodia			= cartera.IdCartera * -1;
		    		ObjEntrada.TablaAfectada 						= "CARTERA";
					ObjEntrada.AccionOperacion						= "ANULACION";		
					ObjEntrada.IdCliente							= cartera.IdCliente;
					ObjEntrada.IdPortafolio							= cartera.IdPortafolio;
					ObjEntrada.FolioCustodia						= cartera.FolioCartera;
					ObjEntrada.IdCustodia							= cartera.IdCartera;
					ObjEntrada.IdOperacion							= cartera.IdOperacion;
					ObjEntrada.IdConcepto							= _c.ConceptoAnulacionAporte;
					ObjEntrada.Cantidad								= cartera.INGCantidad;
					ObjEntrada.MontoCompraMonedaInstrumento			= cartera.Monto;
					ObjEntrada.IdInstrumento						= cartera.IdInstrumento;
					ObjEntrada.ValorRescate							= BigDecimal.ZERO;
					ObjEntrada.FechaOperacion						= Fecha.toDate(fechaSistema);
					ObjEntrada.IdEmisor								= cartera.IdEmisor;
					ObjEntrada.IdTipoOperacion						= _c.TipoOperacionAnulacionRescateFondoMutuo;
					p_RetiroCartera.add(ObjEntrada);
				}
				
				if("S".equalsIgnoreCase(cartera.GeneraNuevoMovimiento)) {
				IngresoCustodia.ObjDatosEntrada	ObjEntrada = new IngresoCustodia.ObjDatosEntrada();
	    		ObjEntrada.IdGenerarMovimientoCustodia			= cartera.IdCartera;
	    		ObjEntrada.TablaAfectada 						= "CARTERA";
				ObjEntrada.AccionOperacion						= "INGRESO";		
				ObjEntrada.IdCliente							= cartera.IdCliente;
				ObjEntrada.IdPortafolio							= cartera.IdPortafolio;
				ObjEntrada.FolioCustodia						= cartera.FolioCartera;
				ObjEntrada.IdCustodia							= cartera.IdCartera;
				ObjEntrada.IdOperacion							= cartera.IdOperacion;
				ObjEntrada.IdConcepto							= _c.ConceptoAnulacionRescate;
				ObjEntrada.Cantidad								= cartera.Cantidad;
				ObjEntrada.IdInstrumento						= cartera.IdInstrumento;
				ObjEntrada.ValorRescate							= BigDecimal.ZERO;
				ObjEntrada.FechaOperacion						= Fecha.toDate(fechaSistema);
				ObjEntrada.IdEmisor								= cartera.IdEmisor;
				ObjEntrada.EsRegistroNuevo						= _c.NO;
				ObjEntrada.IdRegimenFondoMutuo					= cartera.IdRegimenFondoMutuo;
				ObjEntrada.IdTipoOperacion						= _c.TipoOperacionAnulacionRescateFondoMutuo;
				p_IngresoCartera.add(ObjEntrada);
				}
			}
			
		}
		
		if (p_RetiroCartera.size() > 0){
	    	log.write("INICIO:: Anulacion Ingreso Custodia");
	    	RetiroCustodia retiroCustodia = new RetiroCustodia();
			RetiroCustodia.RetiroCustodiaEntrada paramInSPRetiro = new RetiroCustodia.RetiroCustodiaEntrada();
			paramInSPRetiro.p_IdEmpresa			= entrada.p_IdEmpresa;
			paramInSPRetiro.p_IdModulo			= entrada.p_IdModulo;
			paramInSPRetiro.p_IdUsuario			= entrada.p_IdUsuario;
			paramInSPRetiro.p_SimularOperacion	= "N";
			paramInSPRetiro.p_TicketAtencion	= 0L;
			paramInSPRetiro.p_DatosEntrada		= p_RetiroCartera;
			log.write(paramInSPRetiro);
			RetiroCustodia.RetiroCustodiaSalida resultRetiros = retiroCustodia.logica(con, paramInSPRetiro);	
			
			out.P_OUT_ERRORESCONTADOR += resultRetiros.P_OUT_ERRORESCONTADOR;
			for(MensajeSalida mje : resultRetiros.P_OUT_MENSAJES){
				if(MensajeSalida.MENSAJE_ERROR.equalsIgnoreCase(mje.tipo)){
					Long id = Math.abs(mje.id);
					Carteras cartera = (Carteras) hcarteras.get(id);
					out.P_OUT_MENSAJES.add(new MensajeSalida(id, mje.tipo, mje.codigo, mje.descripcion));
					for(SolicitudProceso sol : solicitudes){
						if(cartera.IdOperacion.longValue() == sol.IdSolicitudAporteRescate.longValue()){
							sol.EstadoProceso = _c.EstadoRechazado;
							sol.Codigo = mje.codigo;
							break;
						}
					}
				}
			}
			log.write("FIN:: Anulacion Ingreso Custodia");
	    }
		
		if (p_IngresoCartera.size() > 0){
	    	log.write("INICIO:: Anulacion Rescate Custodia");
	    	IngresoCustodia ingresoCustodia = new IngresoCustodia();
	    	IngresoCustodia.IngresoCustodiaEntrada paramInSPIngreso = new IngresoCustodia.IngresoCustodiaEntrada();
			paramInSPIngreso.p_IdEmpresa			= entrada.p_IdEmpresa;
			paramInSPIngreso.p_IdModulo			= entrada.p_IdModulo;
			paramInSPIngreso.p_IdUsuario			= entrada.p_IdUsuario;
			paramInSPIngreso.p_SimularOperacion	= "N";
			paramInSPIngreso.p_TicketAtencion	= 0L;
			paramInSPIngreso.p_DatosEntrada		= p_IngresoCartera;				
			IngresoCustodia.IngresoCustodiaSalida resultRetiros = ingresoCustodia.logica(con, paramInSPIngreso);	
			
			out.P_OUT_ERRORESCONTADOR += resultRetiros.P_OUT_ERRORESCONTADOR;
			for(MensajeSalida mje : resultRetiros.P_OUT_MENSAJES){
				if(MensajeSalida.MENSAJE_ERROR.equalsIgnoreCase(mje.tipo)){
					Carteras cartera = (Carteras) hcarteras.get(mje.id);
					out.P_OUT_MENSAJES.add(new MensajeSalida(mje.id, mje.tipo, mje.codigo, mje.descripcion));
					for(SolicitudProceso sol : solicitudes){
						if(cartera.IdOperacion.longValue() == sol.IdSolicitudAporteRescate.longValue()){
							sol.EstadoProceso = _c.EstadoRechazado;
							sol.Codigo = mje.codigo;
							break;
						}
					}
				}
			}
			log.write("FIN:: Anulacion Rescate Custodia");
	    }
		
		//SPOT
		{
			AnularOperacionSpotEntrada anularOperacionSpotEntrada = new AnularOperacionSpotEntrada();
			anularOperacionSpotEntrada.p_DatosOperacion = solicitudes.parallelStream().filter(solicitud-> {
				MovSpot movSpot = (MovSpot) hOperacionSpot.get(solicitud.IdSolicitudAporteRescate);
				return movSpot != null && (_c.EstadoVigente.equals(movSpot.Estado) || _c.EstadoPendiente.equals(movSpot.Estado) || _c.EstadoConfirmado.equals(movSpot.Estado)) && (_c.EstadoAnulado.equals(solicitud.IdEstadoNuevo) || _c.TipoSolicitudRescate.equals(solicitud.TipoSolicitud)) && _c.EstadoPendiente.equals(solicitud.EstadoProceso);
			}).map(solicitud->{
				MovSpot movSpot = (MovSpot) hOperacionSpot.get(solicitud.IdSolicitudAporteRescate);
				OperacionSpot operacionSpot = new OperacionSpot();
				operacionSpot.NumeroOperacion =  movSpot.IdOperacionSpot.toString();
				return operacionSpot;
			}).collect(Collectors.toList());
			
			if(anularOperacionSpotEntrada.p_DatosOperacion.size() > 0) {
				anularOperacionSpotEntrada.p_IdEmpresa =  entrada.p_IdEmpresa;
				anularOperacionSpotEntrada.p_IdModulo = entrada.p_IdModulo;
				anularOperacionSpotEntrada.p_IdUsuario = entrada.p_IdUsuario;
				anularOperacionSpotEntrada.p_UsaReglasNegocio = _c.SI;
				AnularOperacionSpot anularOperacionSpot = new AnularOperacionSpot();
				AnularOperacionSpotSalida anularOperacionSpotSalida = anularOperacionSpot.logica(con, anularOperacionSpotEntrada);
				
				out.P_OUT_ERRORESCONTADOR += anularOperacionSpotSalida.P_OUT_ERRORESCONTADOR;
				solicitudes.parallelStream().forEach(solicitud->{
					MovSpot movSpot = (MovSpot) hOperacionSpot.get(solicitud.IdSolicitudAporteRescate);
					if(movSpot != null) {
						for(MensajeSalida mje : anularOperacionSpotSalida.P_OUT_MENSAJES){
							if(MensajeSalida.MENSAJE_ERROR.equalsIgnoreCase(mje.tipo))
								out.P_OUT_MENSAJES.add(mje);
							if(MensajeSalida.MENSAJE_ERROR.equalsIgnoreCase(mje.tipo) && movSpot.IdOperacionSpot.equals(mje.id)){
								solicitud.EstadoProceso = _c.EstadoRechazado;
								solicitud.Codigo = mje.codigo;
								break;
							}
						}
					}
				});
				
			}
		}
		
		List<SolicitudProceso> solUpdatear = new ArrayList<SolicitudProceso>();
		//AGF
		CrearSolicitudAGF crearSolicitudAGF = new CrearSolicitudAGF();
		EntradaCrearSolicitudAGF entradaCrearAGF = new EntradaCrearSolicitudAGF();
		for(SolicitudProceso sol : solicitudes){
			if(!_c.EstadoRechazado.equals(sol.EstadoProceso)){
				sol.EstadoProceso = _c.EstadoAprobado;
				sol.Codigo = "UI_EXITO";
				solUpdatear.add(sol);
				if(_c.NO.equals(sol.SolicitudBatch) && _c.CuentaInversion.equals(sol.IdMedioDePago)){
					CrearSolicitudAGF.Solicitudes solAGF = new CrearSolicitudAGF.Solicitudes();
					solAGF.IdSolicitudAporteRescate = sol.IdSolicitudAporteRescate;
					entradaCrearAGF.p_Solicitudes.add(solAGF);
				}
			}
		}
		
		sql.batch(con, SQL_UPDATE_SOLICITUD, solUpdatear, SolicitudProceso.class);
		
		if(entradaCrearAGF.p_Solicitudes.size() > 0){
			entradaCrearAGF.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaCrearAGF.p_IdUsuario = entrada.p_IdUsuario;
			entradaCrearAGF.p_IdModulo = entrada.p_IdModulo;
			entradaCrearAGF.p_EsManual = _c.NO;
			SalidaCrearSolicitudAGF salidaAGF = crearSolicitudAGF.logica(con, entradaCrearAGF);
			out.P_OUT_ERRORESCONTADOR += salidaAGF.P_OUT_ERRORESCONTADOR;
			out.P_OUT_MENSAJES.addAll(salidaAGF.P_OUT_MENSAJES);
		}
		
		//AGREGAR LOG
		{
			AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
			AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
			log.IdCabeceraLog = 0L;
			log.IdTipoLog = "ANULACION FONDO MUTUO";
			log.IdModulo = _c.ModuloFondoMutuo;
			log.IdUsuario = entrada.p_IdUsuario;
			log.IdEmpresa = entrada.p_IdEmpresa;
			log.FechaSistemaLog = fechaSistema;
			log.IdUsuarioAtendido = entrada.p_IdUsuario;
			log.IdCliente = 0L;
			log.EstadoCliente = "";
			log.NombreCliente = "";
			log.IdTipoDocumentoIdentidad = "";
			log.IdDocumentoIdentidad = "";
			log.IdPortafolio = 0L;
			log.EstadoPortafolio = "";
			log.IdTipoRegistro = "SOLICITUD_APORTE_RESCATE";
			log.EstadoRegistro = _c.EstadoAnulado;
			log.IdTipoOperacion = "";
			log.Folio = 0L;
			log.Cantidad = BigDecimal.ZERO;
			log.PrecioTasa = BigDecimal.ZERO;
			log.Monto = BigDecimal.ZERO;
			log.IdMonedaOrigen = "";
			log.IdMonedaLiquidacion = "";
			log.TipoCambio = "";
			log.Nemo = "";
			log.IdProceso = 0L;
			log.EstadoProceso = "";
			log.NombreProceso = "";						
			entradaAgregarLog.p_Log.add(log);
			AgregarLog agregarLogService = new AgregarLog();
			agregarLogService.logica(con, entradaAgregarLog);
		}
		
		out.P_SOLICITUDES.addAll(procesarSalida(con,solicitudes,entrada));
		
		//Notificacion OpenWorkPlace
		String notificaOWP = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.NotificarEstadoOrdenOWP");
		if("S".equalsIgnoreCase(notificaOWP)){
			List<Long> idSolicitudesOwp = new ArrayList<Long>();
					
			for(SolicitudProceso in : solUpdatear){
				if(!idSolicitudesOwp.contains(in.IdSolicitudAporteRescate) && "OWP".equals(in.IdTipoCanalIngreso)) 
					idSolicitudesOwp.add(in.IdSolicitudAporteRescate);
			}
					
			HashMap<String,Object> parametros = new HashMap<String,Object>();
			parametros.put("IdEmpresa", entrada.p_IdEmpresa);
			parametros.put("IdSolicitudAporteRescate", idSolicitudesOwp);
			List<SolicitudAporteRescate> solicitudesModificadas = sql.query(con, SQL_SELECT_SOLICITUDES_MODIFICADAS, parametros, SolicitudAporteRescate.class);
					
			NotificarEstadoOrdenesOWP servicioNotOwp = new NotificarEstadoOrdenesOWP();
			List<NotificarEstadoOrdenes_Entrada> listSolicitudesNotOWP = new ArrayList<NotificarEstadoOrdenes_Entrada>();
					
			for (SolicitudAporteRescate solicitud : solicitudesModificadas) {
				NotificarEstadoOrdenes_Entrada entradaSolicitud = new NotificarEstadoOrdenes_Entrada();
				entradaSolicitud.Solsec = solicitud.IdSolicitudAporteRescate;
				entradaSolicitud.IdEmpresa = entrada.p_IdEmpresa;
				entradaSolicitud.IdUsuario = entrada.p_IdUsuario;
				entradaSolicitud.NTitulos = solicitud.NumeroCuotas;
				entradaSolicitud.Precio = solicitud.ValorCuota;
				entradaSolicitud.Fecha = solicitud.FechaAceptacion.toString();
				entradaSolicitud.Estado = solicitud.Estado; 
				entradaSolicitud.Comision = solicitud.MontoComision;
				entradaSolicitud.Gastos = new BigDecimal(0);
				entradaSolicitud.Mercado = "F"; 
				entradaSolicitud.Divisa = solicitud.IdMoneda;
				entradaSolicitud.TipoCambio = solicitud.CostoMonedaLocal;
				entradaSolicitud.SolsecAsoc = solicitud.IdSolicitudAporteRescateRelacionado.toString();
				entradaSolicitud.Nemo = solicitud.IdSerieFondo;
				entradaSolicitud.IdClasificacionTipoInstrumento = "FM";
				listSolicitudesNotOWP.add(entradaSolicitud);
			}
			
			if(listSolicitudesNotOWP.size() > 0){
				servicioNotOwp.go(con, listSolicitudesNotOWP);
			}
		}
		
		log.write("FIN::AnularSolicitudAporteRescate");
		return out;
	}
	
	private List<ResultadoAnulacion> procesarSalida(Connection con, List<SolicitudProceso> solicitudes, EntradaAnulacionSolicitudAporteRescate entrada) throws Exception{
		List<ResultadoAnulacion> result = new ArrayList<ResultadoAnulacion>();
		for(SolicitudProceso sol : solicitudes){
			ResultadoAnulacion r = new ResultadoAnulacion();
			r.IdSolicitudAporteRescate = sol.IdSolicitudAporteRescate;
			r.Codigo = sol.Codigo;
			r.Descripcion = new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,sol.Codigo,new HashMap<String,String>(),con,entrada.p_IdEmpresa).descripcion;
			result.add(r);
		}
		return result;
	}
	
	private CrearCargoAbonoSalida mueveCuentaInversion(Connection con, List<SolicitudProceso> solicitudes, EntradaAnulacionSolicitudAporteRescate entrada, String fechaSistema) throws Exception{
		CrearCargoAbonoSalida salidaCrearCargoAbono = new CrearCargoAbonoSalida();
		salidaCrearCargoAbono.P_OUT_ERRORESCONTADOR = 0L;
		salidaCrearCargoAbono.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		CrearCargoAbonoEntrada entradaCrearCargoAbono = new CrearCargoAbonoEntrada();
		CrearCargoAbono crearCargoAbono = new CrearCargoAbono();
		Long i = 0L;
		HashMap<Long,Long> ids = new HashMap<Long,Long>();
		for(SolicitudProceso sol : solicitudes){
			if(_c.EstadoRechazado.equals(sol.EstadoProceso)) continue;
			//APORTES
			if(_c.TipoSolicitudAporte.equals(sol.TipoSolicitud)){
				if((_c.AporteFondoMutuo.equals(sol.IdTipoMovimientoFondoMutuo)
					   || _c.AporteReinversion.equals(sol.IdTipoMovimientoFondoMutuo)
					   || _c.AporteOvernight.equals(sol.IdTipoMovimientoFondoMutuo)
					   || _c.AporteRescateProgramado.equals(sol.IdTipoMovimientoFondoMutuo)
					   || _c.AporteDividendo.equals(sol.IdTipoMovimientoFondoMutuo))){
					ObjCargoAbonoAGenerar obj = new ObjCargoAbonoAGenerar();
					//ABONO EN MONEDA DEL FONDO NORMALES Y DIRERIDOS CUENTA INVERSION('APONORM','APOREINV','APOOVER','APORESPRO','APODIV')
					obj.NumeroCargoAbono = i;
					obj.IdPortafolio = sol.IdPortafolio;
					obj.IdMoneda = sol.IdMonedaFondo;
					obj.IdTipoMovimiento = _c.AbonoCuentaInversion;
					obj.Monto = sol.MontoPago;
					obj.IdMonedaOperacion = sol.IdMonedaFondo;
					obj.MontoOperacion = sol.MontoPago;
					obj.IdConcepto = _c.ConceptoAnulacionAporte;
					obj.Descripcion = "ANULACION_APORTE_FONDO_MUTUO";
					obj.IdCondicionDeLiquidacion = "";
					obj.FechaCondicionDeLiquidacion = fechaSistema;
					obj.IdEjecutivo = sol.IdEmpleadoEjecutivo;
					obj.IdSucursal = 0L;
					obj.IdTipoProducto = _c.TipoProducto;
					obj.IdTipoOperacion = _c.TipoOperacionAnulacionAporteFondoMutuo;
					obj.IdOperacion = sol.IdSolicitudAporteRescate;
					obj.FechaOperacion = fechaSistema;
					obj.FechaLiquidacionOperacion = fechaSistema;
					obj.TipoCambioOperacion = sol.ParidadFechaCertificacion;
					obj.IdTipoComprobante = _c.TipoComprobanteCuentaInversion;
					obj.IdComprobante = sol.IdSolicitudAporteRescate;
					obj.FolioComprobante = sol.IdSolicitudAporteRescate.toString();
					obj.IdComprobanteDetalle = sol.IdSolicitudAporteRescate;
					obj.IdTipoMovimientoAsociado = "";
					obj.IdMovimientoAsociado = 0L;
					obj.FechaComprobante = fechaSistema;
					obj.IdInstrumento = sol.IdInstrumento;
					obj.IdTipoInstrumento = sol.IdTipoInstrumento;
					obj.Nemo = sol.IdSerieFondo;
					obj.EsCargoAbonoPorTraspaso = _c.NO;
					obj.GeneroContraparteEnTesoreria = _c.NO;
					obj.CertificadoAutomatico = _c.SI;
					ids.put(i, sol.IdSolicitudAporteRescate);
					i++;
					entradaCrearCargoAbono.p_CargoAbonoAGenerar.add(obj);
					//CARGO EN MONEDA DE FONDO PARA DIFERIDOS CUENTA INVERSION('APONORM', 'APOREINV','APOOVER','APORESPRO','APODIV')
					if(!sol.IdMoneda.equals(sol.IdMonedaFondo)){
						ObjCargoAbonoAGenerar cardif = new ObjCargoAbonoAGenerar();
						cardif = Utils.copyPojo(obj, cardif);
						cardif.NumeroCargoAbono = i;
						cardif.IdMoneda = sol.IdMonedaFondo;
						cardif.IdTipoMovimiento = _c.CargoCuentaInversion;
						cardif.Monto = sol.MontoPago;
						cardif.IdMonedaOperacion = sol.IdMonedaFondo;
						cardif.MontoOperacion = sol.MontoPago;
						entradaCrearCargoAbono.p_CargoAbonoAGenerar.add(cardif);
						ids.put(i, sol.IdSolicitudAporteRescate);
						i++;
					}
					//ABONO EN MONEDA DE INVERSION PARA DIFERIDOS CUENTA INVERSION('APONORM', 'APOREINV','APOOVER','APORESPRO','APODIV')
					if(!sol.IdMoneda.equals(sol.IdMonedaFondo)){
						ObjCargoAbonoAGenerar cardif = new ObjCargoAbonoAGenerar();
						cardif = Utils.copyPojo(obj, cardif);
						cardif.NumeroCargoAbono = i;
						cardif.IdMoneda = sol.IdMoneda;
						cardif.IdTipoMovimiento = _c.AbonoCuentaInversion;
						cardif.Monto = sol.MontoPagoLocal;
						cardif.IdMonedaOperacion = sol.IdMoneda;
						cardif.MontoOperacion = sol.MontoPagoLocal;
						entradaCrearCargoAbono.p_CargoAbonoAGenerar.add(cardif);
						ids.put(i, sol.IdSolicitudAporteRescate);
						i++;
					}
				}
			}
			//RESCATES
			if(_c.TipoSolicitudRescate.equals(sol.TipoSolicitud)){
				if((_c.Reinversion.equals(sol.IdTipoMovimientoFondoMutuo) || _c.RescateComision.equals(sol.IdTipoMovimientoFondoMutuo))
				   || (_c.RescateFondoMutuo.equals(sol.IdTipoMovimientoFondoMutuo) && _c.NO.equals(sol.IndicadorMLT)) ){
					ObjCargoAbonoAGenerar obj = new ObjCargoAbonoAGenerar();
					//CARGO EN MONEDA FONDO NORMALES Y DIFERIDO CUENTA INVERSION('RESNORM', 'RESREINV', 'RESCOM')
					obj.NumeroCargoAbono = i;
					obj.IdPortafolio = sol.IdPortafolio;
					obj.IdMoneda = sol.IdMonedaFondo;
					obj.IdTipoMovimiento = _c.CargoCuentaInversion;
					obj.Monto = sol.MontoPago;
					obj.IdMonedaOperacion = sol.IdMonedaFondo;
					obj.MontoOperacion = sol.MontoPago;
					obj.IdConcepto = _c.ConceptoAnulacionRescate;
					obj.Descripcion = "ANULACION_RESCATE_FONDO_MUTUO";
					obj.IdCondicionDeLiquidacion = "";
					obj.FechaCondicionDeLiquidacion = fechaSistema;
					obj.IdEjecutivo = sol.IdEmpleadoEjecutivo;
					obj.IdSucursal = 0L;
					obj.IdTipoProducto = _c.TipoProducto;
					obj.IdTipoOperacion = _c.TipoOperacionAnulacionRescateFondoMutuo;
					obj.IdOperacion = sol.IdSolicitudAporteRescate;
					obj.FechaOperacion = fechaSistema;
					obj.FechaLiquidacionOperacion = fechaSistema;
					obj.TipoCambioOperacion = sol.ParidadFechaCertificacion;
					obj.IdTipoComprobante = _c.TipoComprobanteCuentaInversion;
					obj.IdComprobante = sol.IdSolicitudAporteRescate;
					obj.FolioComprobante = sol.IdSolicitudAporteRescate.toString();
					obj.IdComprobanteDetalle = sol.IdSolicitudAporteRescate;
					obj.IdTipoMovimientoAsociado = "";
					obj.IdMovimientoAsociado = 0L;
					obj.FechaComprobante = fechaSistema;
					obj.IdInstrumento = sol.IdInstrumento;
					obj.IdTipoInstrumento = sol.IdTipoInstrumento;
					obj.Nemo = sol.IdSerieFondo;
					obj.EsCargoAbonoPorTraspaso = _c.NO;
					obj.GeneroContraparteEnTesoreria = _c.NO;
					obj.CertificadoAutomatico = _c.SI;
					ids.put(i, sol.IdSolicitudAporteRescate);
					i++;
					//COMISION CON MONEDA DE OPERACION CUENTA INVERSION ('RESNORM','RESREINV')
					if((_c.Reinversion.equals(sol.IdTipoMovimientoFondoMutuo) || _c.RescateComision.equals(sol.IdTipoMovimientoFondoMutuo))
					   && sol.MontoComisionLocal.compareTo(BigDecimal.ZERO) == 1){
						ObjCargoAbonoAGenerar cardif = new ObjCargoAbonoAGenerar();
						cardif = Utils.copyPojo(obj, cardif);
						cardif.NumeroCargoAbono = i;
						cardif.IdMoneda = sol.IdMoneda;
						cardif.IdTipoMovimiento = _c.AbonoCuentaInversion;
						cardif.Monto = sol.MontoComisionLocal;
						cardif.IdMonedaOperacion = sol.IdMoneda;
						cardif.MontoOperacion = sol.MontoComisionLocal;
						entradaCrearCargoAbono.p_CargoAbonoAGenerar.add(cardif);
						ids.put(i, sol.IdSolicitudAporteRescate);
						i++;
					}
					//CARGO EN MONEDA DE INVERSION PARA DIFERIDOS CUENTA INVERSION ('RESNORM','RESREINV','RESCOM')
					if(!sol.IdMoneda.equals(sol.IdMonedaFondo)){
						ObjCargoAbonoAGenerar cardif = new ObjCargoAbonoAGenerar();
						cardif = Utils.copyPojo(obj, cardif);
						cardif.NumeroCargoAbono = i;
						cardif.IdMoneda = sol.IdMoneda;
						cardif.IdTipoMovimiento = _c.CargoCuentaInversion;
						cardif.Monto = sol.MontoPagoLocal;
						cardif.IdMonedaOperacion = sol.IdMoneda;
						cardif.MontoOperacion = sol.MontoPagoLocal;
						entradaCrearCargoAbono.p_CargoAbonoAGenerar.add(cardif);
						ids.put(i, sol.IdSolicitudAporteRescate);
						i++;
					}
					//ABONO EN MONEDA DEL FONDO PARA DIFERIDOS CUENTA INVERSION('RESNORM','RESREINV', 'RESCOM')
					if(!sol.IdMoneda.equals(sol.IdMonedaFondo)){
						ObjCargoAbonoAGenerar cardif = new ObjCargoAbonoAGenerar();
						cardif = Utils.copyPojo(obj, cardif);
						cardif.NumeroCargoAbono = i;
						cardif.IdMoneda = sol.IdMonedaFondo;
						cardif.IdTipoMovimiento = _c.AbonoCuentaInversion;
						cardif.Monto = sol.MontoPago;
						cardif.IdMonedaOperacion = sol.IdMonedaFondo;
						cardif.MontoOperacion = sol.MontoPago;
						entradaCrearCargoAbono.p_CargoAbonoAGenerar.add(cardif);
						ids.put(i, sol.IdSolicitudAporteRescate);
						i++;
					}
					entradaCrearCargoAbono.p_CargoAbonoAGenerar.add(obj);
				}
			}
			
		}
		
		if(entradaCrearCargoAbono.p_CargoAbonoAGenerar.size() > 0){
			entradaCrearCargoAbono.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaCrearCargoAbono.p_IdUsuario = entrada.p_IdUsuario;
			entradaCrearCargoAbono.p_IdModulo = entrada.p_IdModulo;
			entradaCrearCargoAbono.p_simularOperacion = _c.NO;
			entradaCrearCargoAbono.p_UsaReglasNegocio = _c.SI;
			entradaCrearCargoAbono.p_FechaSistema = fechaSistema;
			entradaCrearCargoAbono.p_EsIngresoManual	= "N";
			salidaCrearCargoAbono = crearCargoAbono.logica(con, entradaCrearCargoAbono);
			
			//Cambiamos el id interno por el id de la solicitud
			for(MensajeSalida mje : salidaCrearCargoAbono.P_OUT_MENSAJES){
				if(MensajeSalida.MENSAJE_ERROR.equals(mje.tipo)){
					Long id = ids.get(mje.id);
					mje.id = id;
				}
			}
		}
		
		return salidaCrearCargoAbono;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();			
		//String jsonRF = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"FMM\",\"p_Solicitudes\":[{\"IdSolicitudAporteRescate\":18817,\"Estado\":\"ANU\"}]}";
		String jsonRv = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"FMM\",\"p_Solicitudes\":[{\"IdSolicitudAporteRescate\":399267,\"Estado\":\"ANU\"}]}";
		EntradaAnulacionSolicitudAporteRescate Objin = gson.fromJson(jsonRv, EntradaAnulacionSolicitudAporteRescate.class);
		AnulacionSolicitudAporteRescate  demo=new AnulacionSolicitudAporteRescate();				
		try {
			SalidaAnulacionSolicitudAporteRescate ObjOut=demo.logica(con, Objin);
			//con.commit();
			con.rollback();
			System.out.println(ObjOut.toString());
		}
		catch(Exception er) {
			er.printStackTrace();	
			con.rollback();
		}
			con.rollback(); 			
    	}
		System.out.println("== END ==");
	}
}
