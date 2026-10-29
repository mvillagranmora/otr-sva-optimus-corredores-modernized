package cl.bcs.fondomutuo;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo;
import cl.bcs.fondomutuo.pojo.ModalidadPago;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.tesoreria.CrearIngresoEgreso;
import cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Entrada;
import cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Salida;
import cl.bcs.tesoreria.CrearIngresoEgreso.IngresoEgreso_Entrada;
import cl.bcs.tesoreria.CrearIngresoEgreso.FormaDePago_Entrada;
import cl.bcs.tesoreria.CrearIngresoEgreso.IngresoEgresoGenerado_Salida;
import cl.bcs.APV.CalculoRetencionTraspasos.Retencion;
import cl.bcs.cuentaInversion.CrearCargoAbono;
import cl.bcs.cuentaInversion.CrearCargoAbono.CrearCargoAbonoEntrada;
import cl.bcs.cuentaInversion.CrearCargoAbono.CrearCargoAbonoSalida;
import cl.bcs.cuentaInversion.CrearCargoAbono.ObjCargoAbonoAGenerar;
import cl.bcs.cuentaInversion.CrearCargoAbono.ObjCargoAbonoGenerado;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class LiquidacionSolicitudAporteRescate implements JavaCall {
	private final Log log = new Log("FM.LiquidacionSolicitudAporteRescate");
	private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();
	
	private final String SQL_SELECT_SOLICITUDES_LIQUIDAR = loadResourceToString("cl/bcs/fondomutuo/sql/LiquidacionSolicitudAporteRescate_SelectSolicitudes.sql");
	private final String SQL_SELECT_SOLICITUDES_LIQUIDAR_FILTROS = loadResourceToString("cl/bcs/fondomutuo/sql/LiquidacionSolicitudAporteRescate_SelectSolicitudesFiltros.sql");
	private final String SQL_UPDATE_SOLICITUDES = loadResourceToString("cl/bcs/fondomutuo/sql/LiquidacionSolicitudAporteRescate_UpdateSolicitudes.sql");
	private final String SQL_SELECT_MODALIDAD_PAGO = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectModalidadPago.sql");	
	public String SQL_SELECT_CONFIGURACION_RETENCION = loadResourceToString("cl/bcs/APV/sql/ConsultarRetencion.sql");
	
	
	public static class LiquidacionSolicitudAporteRescate_IN {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_SimularOperacion;
		public String p_UsaReglasNegocio;
		public List<InMovimientos> p_Movimientos;
		public List<InFiltros> p_Filtros;
		
		public LiquidacionSolicitudAporteRescate_IN() {
			p_Movimientos = new ArrayList<InMovimientos>();
			p_Filtros = new ArrayList<InFiltros>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InMovimientos {
		public Long IdMovimiento;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InFiltros {
		public Long IdInterno;
		public String IdEmisor;
		public String TipoSolicitud;
		public String IdEstado;
		public String FechaCertificacion;
		public String FechaLiquidacion;
		public String IdSerieFondo;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	public static class LiquidacionSolicitudAporteRescate_OUT {
		public List<ResultadoLiquidacion> P_OUT_RESULTADO;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		
		public LiquidacionSolicitudAporteRescate_OUT() {
			this.P_OUT_RESULTADO = new ArrayList<ResultadoLiquidacion>();
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR = 0L;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ResultadoLiquidacion{
		public Long IdInterno;
		public Long IdSolicitudAporteRescate;
		public String IdSerieFondo;
		public String Codigo;
		public String Estado;
		public String NombreEstado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SolicitudesProcesoLiquidacion{
		public Long IdInterno;
		public Long IdSolicitudAporteRescate;
		public String Codigo;
		public String Descripcion;
	    public String TipoSolicitud;
	    public String IdSerieFondo;
	    public Long IdInstrumento;
	    public String IdTipoInstrumento;
	    public Long IdPortafolio;
	    public String IdMoneda;
	    public String IdMonedaFondo;
	    public BigDecimal CostoMonedaLocal;
	    public BigDecimal Monto;
	    public Date FechaLiquidacion;
	    public Date FechaCertificacion;
	    public Long IdPortafolioAGF;
	    public String IdMedioDePago;
	    public Long IdPersonaAGF;
	    public String IdMedioDePagoAbonoAGF;
	    public String IdMedioDePagoCargoAGF;
	    public String EstadoLiquidacion;
	    public BigDecimal MontoPago;
	    public BigDecimal MontoComision;
	    public BigDecimal MontoPagoLocal;
	    public BigDecimal MontoComisionLocal;
	    public Long IdOmnibus;
	    public Long Rechazo;
	    public Long FolioCartaLiquidacion;
	    public Long IdPersona;
	    public String CodigoPortafolio;
	    public String IdTipoMovimientoFondoMutuo;
	    public Long IdMatrizAgrupadorAPV;
	    
	    public SolicitudesProcesoLiquidacion() {
	    	this.IdInterno = 0L;
	    	this.Codigo = "UI_EXITO";
	    	this.Descripcion = "";
	    	this.Rechazo = 0L;
	    	this.FolioCartaLiquidacion = 0L;
	    }
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Grupo{
		public Long IdPersonaAGF; 
		public String TipoSolicitud;
		public String IdMoneda;
		public Long IdPortafolioAGF;
		public String IdMedioDePago;
		public Date FechaLiquidacion;
		public Date FechaCertificacion;
		 
		@Override
		public int hashCode() {
			return 31 * this.IdMoneda.hashCode() + this.TipoSolicitud.hashCode() + this.IdPersonaAGF.hashCode() + this.IdPortafolioAGF.hashCode() + this.IdMedioDePago.hashCode() + this.FechaLiquidacion.hashCode() + this.FechaCertificacion.hashCode();
		}
		 
		 @Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
			    return false;
			if (getClass() != obj.getClass())
			    return false;
			Grupo grupo = (Grupo) obj;
			return this.IdMoneda.equals(grupo.IdMoneda) && this.TipoSolicitud.equals(grupo.TipoSolicitud) && this.IdPersonaAGF.equals(grupo.IdPersonaAGF) 
			  && this.IdPortafolioAGF.equals(grupo.IdPortafolioAGF) && this.IdMedioDePago.equals(grupo.IdMedioDePago) && this.FechaLiquidacion.equals(grupo.FechaLiquidacion)
			  && this.FechaCertificacion.equals(grupo.FechaCertificacion);
		}
		
		 public String toString() {
			 return Utils.printJson(this, false);
		 }
	} 

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		Gson gson = Utils.getGsonBuilder();
		LiquidacionSolicitudAporteRescate_IN jin = gson.fromJson(in.toString(), LiquidacionSolicitudAporteRescate_IN.class);
		LiquidacionSolicitudAporteRescate_OUT out = logica(conn, jin);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public LiquidacionSolicitudAporteRescate_OUT logica (Connection conn, LiquidacionSolicitudAporteRescate_IN in) throws Exception {
		log.write("INICIO::FM.LiquidacionSolicitudAporteRescate");
		SQL sql = new SQL();
		Long c = 0L;
		Long i = 1L;
		LiquidacionSolicitudAporteRescate_OUT out = new LiquidacionSolicitudAporteRescate_OUT();
		HashMap<String, Object> params = new HashMap<>();
		HashMap<String,String> mensajes = new HashMap<>();
		List<SolicitudesProcesoLiquidacion> solicitudesLiquidar = new ArrayList<>();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		String liquidacionCtaInv = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "FONDOMUTUO", "LiquidacionCuentaInversionCliente");
		String liquidarOmnibus = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "FONDOMUTUO", "VALIDAR_LIQUIDACION_OMNIBUS");
		List<ModalidadPago> listaModalidadPago = sql.query(conn, SQL_SELECT_MODALIDAD_PAGO, new HashMap<String,Object>(), ModalidadPago.class);
		Map<String,ModalidadPago> hashModalidadPago = listaModalidadPago.parallelStream().collect(Collectors.toMap(modalidad->modalidad.IdMedioDePago, modalidad->modalidad));
		
		
		//validaciones
		if(in.p_Filtros.size() == 0) {
			mensajes.clear();
			out.P_OUT_MENSAJES.add(new MensajeSalida(c,MensajeSalida.MENSAJE_ERROR,"NO_SE_INGRESARON_FILTROS",mensajes,conn,in.p_IdEmpresa));
			c++;
		}
		
		//obtenemos los movimientos
		List<InMovimientos> movimientos = new ArrayList<>();
		movimientos.addAll(in.p_Movimientos);
		//obtenemos los ids de los movimientos
		List<Long> idMovimientos = new ArrayList<>();
		idMovimientos.addAll(movimientos.stream().map(p -> p.IdMovimiento).collect(Collectors.toList()));
		
		Long IdEmpresa = 1L;
		String esAPVHabilitado = ConfiguracionSistema.getParametroConfiguracionString(conn,IdEmpresa, "PLATAFORMA","APV.Habilitado");

		
		params = new HashMap<>();
		params.put("p_IdEmpresa", in.p_IdEmpresa);
		if(movimientos.size() > 0 && idMovimientos.size() > 0) {
			params.put("p_Movimientos", idMovimientos);
			solicitudesLiquidar = sql.query(conn, SQL_SELECT_SOLICITUDES_LIQUIDAR, params, SolicitudesProcesoLiquidacion.class);
		}
		else {
			//tomamos el primer elemento de p_filtros
			InFiltros aux = in.p_Filtros.stream().findFirst().get();
			params.put("IdEmisor", aux.IdEmisor);
			params.put("TipoSolicitud", aux.TipoSolicitud);
			params.put("IdEstado", aux.IdEstado);
			params.put("FechaCertificacion", aux.FechaCertificacion.equals("") ? "0001-01-01" : aux.FechaCertificacion);
			params.put("FechaLiquidacion", aux.FechaLiquidacion.equals("") ? "0001-01-01" : aux.FechaLiquidacion);
			params.put("IdSerieFondo", aux.IdSerieFondo);
			
			solicitudesLiquidar = sql.query(conn, SQL_SELECT_SOLICITUDES_LIQUIDAR_FILTROS, params, SolicitudesProcesoLiquidacion.class);
		}
		
		//verificamos omnibus
		if(_c.SI.equals(liquidarOmnibus)) {
			Long numOmnibus = solicitudesLiquidar.stream().filter(p -> p.IdOmnibus != null).filter(p -> p.TipoSolicitud.equals("APO") && p.IdOmnibus == 0).count();
			if(numOmnibus > 0) {
				List<SolicitudesProcesoLiquidacion> sinOmnibus = solicitudesLiquidar.stream().filter(p -> p.IdOmnibus != null)
						.filter(p -> p.TipoSolicitud.equals("APO") && p.IdOmnibus == 0).collect(Collectors.toList());
				//dejamos solo las necesarias
				solicitudesLiquidar.removeAll(sinOmnibus);
				sinOmnibus.stream().map(p -> {
					p.Codigo = "UI_ERROR_APORTE_SIN_OMNIBUS";
					p.Rechazo = 1L;
					return p;
				})
				.collect(Collectors.toList());
				
				if(sinOmnibus.size() == 0) {
					mensajes.clear();
					out.P_OUT_MENSAJES.add(new MensajeSalida(c,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_APORTE_SIN_OMNIBUS",mensajes,conn,in.p_IdEmpresa));
					c++;
				}
			}
		}
		
		//genero grupo resumen con la suma
		/*Map<Object, Long> sumaAportesRescatesGrouped = solicitudesLiquidar.stream()
				.filter(p -> p.Rechazo == 0)
				.collect(Collectors.groupingBy(p -> Arrays.asList(p.IdPersonaAGF, p.TipoSolicitud, p.IdMoneda, p.IdPortafolioAGF, p.IdMedioDePagoCargoAGF,
							p.IdMedioDePagoAbonoAGF, p.FechaLiquidacion, p.FechaCertificacion, p.IdInterno)
				, Collectors.summingLong(p -> p.MontoPagoLocal.longValue())));*/
		Map<Grupo, List<SolicitudesProcesoLiquidacion>> aportesRescatesGrouped = solicitudesLiquidar.stream()
				.collect(Collectors.groupingBy(p ->{
					Grupo grupo = new Grupo();
					grupo.IdPersonaAGF = p.IdPersonaAGF;
					grupo.TipoSolicitud = p.TipoSolicitud;
					grupo.IdMoneda = p.IdMonedaFondo;
					grupo.IdPortafolioAGF = p.IdPortafolioAGF;
					grupo.IdMedioDePago = p.TipoSolicitud.equals(_c.TipoSolicitudAporte) ? p.IdMedioDePagoCargoAGF : p.IdMedioDePagoAbonoAGF;
					grupo.FechaLiquidacion = p.FechaLiquidacion;
					grupo.FechaCertificacion = p.FechaCertificacion;
					return grupo;
				}));
		
		//obtengo secuencias para las cartas
		
		int secuencias = 0;
		if(esAPVHabilitado.equals("S")) {
			secuencias = aportesRescatesGrouped.size()+solicitudesLiquidar.size();
		}else {
			secuencias = aportesRescatesGrouped.size();
		}
		
		List<Long> secuencia_carta = Utils.getSecuencia(conn, "BSCL.FondoMutuo.BD.Secuencias::FolioCartaLiquidacionAporteRescate", new Long(secuencias));
		List<Long> secuencia_comprobante = Utils.getSecuencia(conn, "BSCL.FondoMutuo.BD.Secuencias::SecuenciaComprobanteLiquidacion", new Long(secuencias));
		List<IngresoEgreso_Entrada> lista_ingreso_egreso = new ArrayList<>();
		List<FormaDePago_Entrada> lista_forma_pago = new ArrayList<>();
		List<IngresoEgreso_Entrada> lista_ingreso_egreso_inv = new ArrayList<>();
		HashMap<Long, List<SolicitudesProcesoLiquidacion>> carta_solicitud = new HashMap<>();
				
		int numReferencia = 0;

		for(Map.Entry<Grupo, List<SolicitudesProcesoLiquidacion>> entry : aportesRescatesGrouped.entrySet()) {
			
			//INICIO AGF
			//calculamos el total del grupo
			BigDecimal montoTotal = entry.getValue().stream().map(p -> p.Monto).reduce(BigDecimal.ZERO, BigDecimal::add);
			
			IngresoEgreso_Entrada ingresoEgreso = new IngresoEgreso_Entrada();
			FormaDePago_Entrada formaPago = new FormaDePago_Entrada();
			
			ingresoEgreso.NumeroReferencia = (long) numReferencia;
			ingresoEgreso.IdTipoMovimiento = entry.getKey().TipoSolicitud.equals(_c.TipoSolicitudAporte) ? "MOVEGR" : "MOVING";
			ingresoEgreso.IdTipoPersona = _c.SI.equals(liquidacionCtaInv) ? "CLI" : "CNT";
			ingresoEgreso.IdPersona = entry.getKey().IdPersonaAGF;
			ingresoEgreso.IdPortafolio = _c.SI.equals(liquidacionCtaInv) ? entry.getKey().IdPortafolioAGF : 0L;
			ingresoEgreso.IdMoneda = entry.getKey().IdMoneda;
			ingresoEgreso.Monto = montoTotal;
			ingresoEgreso.FechaLiquidacion = Fecha.toFormat(entry.getKey().FechaLiquidacion, "yyyy-MM-dd");
			ingresoEgreso.IdConcepto = entry.getKey().TipoSolicitud.equals(_c.TipoSolicitudAporte) ? "CAAEFM" : "ABREFM";
			ingresoEgreso.IdTipoPersonaTitularPago = _c.SI.equals(liquidacionCtaInv) ? "CLI" : "CNT";
			ingresoEgreso.IdPersonaTitularPago = entry.getKey().IdPersonaAGF;
			ingresoEgreso.IdPortafolioTitularPago = _c.SI.equals(liquidacionCtaInv) ? entry.getKey().IdPortafolioAGF : 0L;
			ingresoEgreso.IdCaja = 0L;
			ingresoEgreso.SecuenciaAgrupacion = "";
			ingresoEgreso.SecuenciaRelacionada = 0L;
			ingresoEgreso.Glosa = "";
			ingresoEgreso.IdEmpleadoDigitador = 0L;
			ingresoEgreso.IdSucursalEmpleadoDigitador = 0L;
			ingresoEgreso.IdTipoProducto = "INFM";
			ingresoEgreso.IdTipoOperacion = entry.getKey().TipoSolicitud.equals(_c.TipoSolicitudAporte) ? "APORFM" : "RESCFM";
			ingresoEgreso.IdTipoComprobante = _c.TipoComprobanteLiquidacionTesoreria;
			ingresoEgreso.IdComprobante = secuencia_carta.get(numReferencia);
			ingresoEgreso.FechaComprobante = "0001-01-01";
			ingresoEgreso.FechaLiquidacionComprobante = "0001-01-01";
			ingresoEgreso.IdOperacion = secuencia_comprobante.get(numReferencia);
			ingresoEgreso.FechaOperacion = Fecha.toFormat(entry.getKey().FechaLiquidacion, "yyyy-MM-dd");
			ingresoEgreso.IdCondicionLiquidacion = "T0";
			ingresoEgreso.IdBolsa = "BCS";
			ingresoEgreso.IdMonedaOperacion = entry.getKey().IdMoneda;
			ingresoEgreso.Monto = montoTotal;
			ingresoEgreso.IdTipoReajuste = "";
			ingresoEgreso.FechaParidad = "0001-01-01";
			ingresoEgreso.ValorParidad = BigDecimal.ZERO;
			ingresoEgreso.FolioCartera = 0L;
			ingresoEgreso.IdInstrumento = 0L;
			ingresoEgreso.IdTipoInstrumento = "";
			ingresoEgreso.IdClasificacionTipoInstrumento = "";
			ingresoEgreso.Bilateral = "N";
			ingresoEgreso.GeneraComprobante = "N";
			ingresoEgreso.AgruparEnComprobante = "N";
			ingresoEgreso.MueveCuentaInversion = _c.SI.equals(liquidacionCtaInv) ? "N" : "S";
			ingresoEgreso.TitularPago = "N";
			
			formaPago.NumeroReferencia = ingresoEgreso.NumeroReferencia;
			formaPago.NumeroReferenciaFormaDePago = ingresoEgreso.NumeroReferencia;
			formaPago.IdMedioDePago = entry.getKey().IdMedioDePago;
			formaPago.IdModalidadDePago = hashModalidadPago.get(entry.getKey().IdMedioDePago) != null? hashModalidadPago.get(entry.getKey().IdMedioDePago).IdModalidadDePago:_c.MODCAJ;
			formaPago.IdMoneda = ingresoEgreso.IdMoneda;
			formaPago.Monto = ingresoEgreso.Monto;
			formaPago.IdCuentaBancariaCorredora = 0L;
			formaPago.IdTarjetaBancariaCorredora = 0L;
			formaPago.IdChequera = 0L;
			formaPago.NumeroDocumentoCorredora = 0L;
			formaPago.FechaDocumentoPagoCorredora = "0001-01-01";
			formaPago.IdCuentaBancariaSocio = 0L;
			formaPago.IdBancoSocio = 0L;
			formaPago.NombreCuentaBancariaSocio = "";
			formaPago.NumeroCuentaBancariaSocio = "";
			formaPago.IdTipoCuentaBancariaSocio = "";
			formaPago.NombreTipoCuentaBancariaSocio = "";
			formaPago.IdTarjetaBancariaSocio = 0L;
			formaPago.IdTipoTarjetaBancariaSocio = "";
			formaPago.NombreTipoTarjetaBancariaSocio = "";
			formaPago.NumeroTarjetaBancariaSocio = "";
			formaPago.NumeroSerieSocio = "";
			formaPago.NumeroDocumentoSocio = 0L;
			formaPago.FechaDocumentoPagoSocio = "0001-01-01";
			lista_ingreso_egreso.add(ingresoEgreso);
			lista_forma_pago.add(formaPago);
			lista_ingreso_egreso_inv.add(ingresoEgreso);
			//asociacion previa entre numReferencia (idinterno) y solicitudes
			carta_solicitud.put((long) numReferencia, entry.getValue());
			numReferencia++;
			//FIN AGF
		}
		
		//numReferencia= 0;
		
		if(esAPVHabilitado.equals("S")) {
			HashMap<String,Object> ParamsSelectConfRetencion = new HashMap<String,Object>();
			List<Retencion> _ConfiguracionRetencion = sql.query(conn, SQL_SELECT_CONFIGURACION_RETENCION, ParamsSelectConfRetencion, Retencion.class);
			
			//INICIO CLIENTE
			for(SolicitudesProcesoLiquidacion solicitud : solicitudesLiquidar) {
				
				IngresoEgreso_Entrada ingresoEgresoCliente = new IngresoEgreso_Entrada();
				FormaDePago_Entrada formaPagoCliente = new FormaDePago_Entrada();
				//
	
				ingresoEgresoCliente.NumeroReferencia = (long) numReferencia;
				ingresoEgresoCliente.IdTipoMovimiento = solicitud.TipoSolicitud.equals(_c.TipoSolicitudAporte) ? "MOVING" : "MOVEGR";
				ingresoEgresoCliente.IdTipoPersona = _c.SI.equals(liquidacionCtaInv) ? "CLI" : "CNT";
				ingresoEgresoCliente.IdPersona = solicitud.IdPersona;
				ingresoEgresoCliente.IdPortafolio = _c.SI.equals(liquidacionCtaInv) ? solicitud.IdPortafolio : 0L;
				ingresoEgresoCliente.IdMoneda = solicitud.IdMoneda;
				ingresoEgresoCliente.Monto = solicitud.Monto.subtract(solicitud.MontoComision);
				ingresoEgresoCliente.FechaLiquidacion = Fecha.toFormat(solicitud.FechaLiquidacion, "yyyy-MM-dd");
				ingresoEgresoCliente.IdConcepto =solicitud.TipoSolicitud.equals(_c.TipoSolicitudAporte) ? "ABREFM" : "CAAEFM";
				ingresoEgresoCliente.IdTipoPersonaTitularPago = _c.SI.equals(liquidacionCtaInv) ? "CLI" : "CNT";
				ingresoEgresoCliente.IdPersonaTitularPago = solicitud.IdPersona;
				ingresoEgresoCliente.IdPortafolioTitularPago = _c.SI.equals(liquidacionCtaInv) ? solicitud.IdPortafolio : 0L;
				ingresoEgresoCliente.IdCaja = 0L;
				ingresoEgresoCliente.SecuenciaAgrupacion = "";
				ingresoEgresoCliente.SecuenciaRelacionada = 0L;
				ingresoEgresoCliente.Glosa = "";
				ingresoEgresoCliente.IdEmpleadoDigitador = 0L;
				ingresoEgresoCliente.IdSucursalEmpleadoDigitador = 0L;
				ingresoEgresoCliente.IdTipoProducto = "INFM";
				ingresoEgresoCliente.IdTipoOperacion = solicitud.TipoSolicitud.equals(_c.TipoSolicitudAporte) ? "RESCFM" : "APORFM";
				ingresoEgresoCliente.IdTipoComprobante = _c.TipoComprobanteLiquidacionTesoreria;
				ingresoEgresoCliente.IdComprobante = secuencia_carta.get(numReferencia);
				ingresoEgresoCliente.FechaComprobante = "0001-01-01";
				ingresoEgresoCliente.FechaLiquidacionComprobante = "0001-01-01";
				ingresoEgresoCliente.IdOperacion = secuencia_comprobante.get(numReferencia);
				ingresoEgresoCliente.FechaOperacion = Fecha.toFormat(solicitud.FechaLiquidacion, "yyyy-MM-dd");
				ingresoEgresoCliente.IdCondicionLiquidacion = "T0";
				ingresoEgresoCliente.IdBolsa = "BCS";
				ingresoEgresoCliente.IdMonedaOperacion = solicitud.IdMoneda;
				ingresoEgresoCliente.IdTipoReajuste = "";
				ingresoEgresoCliente.FechaParidad = "0001-01-01";
				ingresoEgresoCliente.ValorParidad = BigDecimal.ZERO;
				ingresoEgresoCliente.FolioCartera = 0L;
				ingresoEgresoCliente.IdInstrumento = 0L;
				ingresoEgresoCliente.IdTipoInstrumento = "";
				ingresoEgresoCliente.IdClasificacionTipoInstrumento = "";
				ingresoEgresoCliente.Bilateral = "N";
				ingresoEgresoCliente.GeneraComprobante = "N";
				ingresoEgresoCliente.AgruparEnComprobante = "N";
				ingresoEgresoCliente.MueveCuentaInversion = _c.SI.equals(liquidacionCtaInv) ? "N" : "S";
				ingresoEgresoCliente.TitularPago = "N";
								
				if(solicitud.IdTipoMovimientoFondoMutuo.equals("RESNORM") || solicitud.IdTipoMovimientoFondoMutuo.equals("RESTRASP")) {
					if(solicitud.CodigoPortafolio.equals("APV-B")){
						if(solicitud.IdMedioDePago.equals("CIN") == false){
							for(Retencion retRow : _ConfiguracionRetencion){
								if (retRow.CodigoRetiro.equals("RES") && retRow.PorcentajeRetencion.compareTo(BigDecimal.ZERO)!=0){ 
									if(retRow.IdRegimenTributario == null || retRow.IdRegimenTributario.compareTo(solicitud.IdMatrizAgrupadorAPV) == 0 ){
										BigDecimal MontoRetencion = solicitud.MontoPago.multiply(retRow.PorcentajeRetencion);							
										BigDecimal MontoRetencionDescontada =  solicitud.MontoPago.subtract(MontoRetencion);
										ingresoEgresoCliente.Monto = MontoRetencionDescontada.subtract(solicitud.MontoComision);
										break;
									}
								}
							}
						}
					}
				}
				
				formaPagoCliente.NumeroReferencia = ingresoEgresoCliente.NumeroReferencia;
				formaPagoCliente.NumeroReferenciaFormaDePago = ingresoEgresoCliente.NumeroReferencia;
				formaPagoCliente.IdMedioDePago = solicitud.IdMedioDePago;
				formaPagoCliente.IdModalidadDePago = hashModalidadPago.get(solicitud.IdMedioDePago) != null? hashModalidadPago.get(solicitud.IdMedioDePago).IdModalidadDePago:_c.MODCAJ;
				formaPagoCliente.IdMoneda = ingresoEgresoCliente.IdMoneda;
				formaPagoCliente.Monto = ingresoEgresoCliente.Monto;
				formaPagoCliente.IdCuentaBancariaCorredora = 0L;
				formaPagoCliente.IdTarjetaBancariaCorredora = 0L;
				formaPagoCliente.IdChequera = 0L;
				formaPagoCliente.NumeroDocumentoCorredora = 0L;
				formaPagoCliente.FechaDocumentoPagoCorredora = "0001-01-01";
				formaPagoCliente.IdCuentaBancariaSocio = 0L;
				formaPagoCliente.IdBancoSocio = 0L;
				formaPagoCliente.NombreCuentaBancariaSocio = "";
				formaPagoCliente.NumeroCuentaBancariaSocio = "";
				formaPagoCliente.IdTipoCuentaBancariaSocio = "";
				formaPagoCliente.NombreTipoCuentaBancariaSocio = "";
				formaPagoCliente.IdTarjetaBancariaSocio = 0L;
				formaPagoCliente.IdTipoTarjetaBancariaSocio = "";
				formaPagoCliente.NombreTipoTarjetaBancariaSocio = "";
				formaPagoCliente.NumeroTarjetaBancariaSocio = "";
				formaPagoCliente.NumeroSerieSocio = "";
				formaPagoCliente.NumeroDocumentoSocio = 0L;
				formaPagoCliente.FechaDocumentoPagoSocio = "0001-01-01";
				
				
				
				if (formaPagoCliente.IdMedioDePago.equals("CIN") == true) {
					
					lista_ingreso_egreso_inv.add(ingresoEgresoCliente);
					carta_solicitud.put((long) numReferencia, solicitudesLiquidar);
					
				}
				else {	
					
					if(!ingresoEgresoCliente.IdTipoMovimiento.equalsIgnoreCase("MOVING")) {		
						lista_ingreso_egreso_inv.add(ingresoEgresoCliente);
						carta_solicitud.put((long) numReferencia, solicitudesLiquidar);
						//FIN CLIENTE
						//numReferencia++;
					}else {
						lista_ingreso_egreso.add(ingresoEgresoCliente);
						lista_ingreso_egreso_inv.add(ingresoEgresoCliente);
						lista_forma_pago.add(formaPagoCliente);
						carta_solicitud.put((long) numReferencia, solicitudesLiquidar);
						//FIN CLIENTE
						//numReferencia++;
					}
						

				}
				numReferencia++;
			
			}
		}
		
		
				//crear ingreso egreso tesoreria
				CrearIngresoEgreso_Entrada entradaCrearIngresoEgreso = new CrearIngresoEgreso_Entrada();
				entradaCrearIngresoEgreso.p_IdEmpresa = in.p_IdEmpresa;
				entradaCrearIngresoEgreso.p_IdUsuario = in.p_IdUsuario;
				entradaCrearIngresoEgreso.p_IdModulo = in.p_IdModulo;
				entradaCrearIngresoEgreso.p_SimularOperacion = in.p_SimularOperacion;
				entradaCrearIngresoEgreso.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
				entradaCrearIngresoEgreso.p_EsIngresoAutomatico = "N";
				entradaCrearIngresoEgreso.p_IngresoEgresoGenerar = lista_ingreso_egreso;
				entradaCrearIngresoEgreso.p_FormaDePago = lista_forma_pago;
				
				
				CrearIngresoEgreso crearIngresoEgreso = new CrearIngresoEgreso();
				CrearIngresoEgreso_Salida salidaIngresoEgreso = crearIngresoEgreso.logica(conn, entradaCrearIngresoEgreso);
				
				//si hay errores salimos
				if(salidaIngresoEgreso.P_OUT_ERRORESCONTADOR != null) {
					if(salidaIngresoEgreso.P_OUT_ERRORESCONTADOR > 0) {
						out.P_OUT_ERRORESCONTADOR = salidaIngresoEgreso.P_OUT_ERRORESCONTADOR;
						out.P_OUT_MENSAJES = salidaIngresoEgreso.P_OUT_MENSAJES;
						return out;
					}
				}
				
				//de acuerdo a la salida de los ingreso_egreso buscamos las solicitudes respectivas para actualizar
					for(IngresoEgresoGenerado_Salida salida : salidaIngresoEgreso.P_OUT_INGRESOEGRESOGENERADO) {
						List<SolicitudesProcesoLiquidacion> solicitudesActualizar = carta_solicitud.get(salida.NumeroReferencia);
						int j = 0;
						for(SolicitudesProcesoLiquidacion sol : solicitudesActualizar) {
							sol.FolioCartaLiquidacion = secuencia_carta.get(salida.NumeroReferencia.intValue());
							j++;
						}
						if(j > 0) {
							sql.batch(conn, SQL_UPDATE_SOLICITUDES, solicitudesActualizar, SolicitudesProcesoLiquidacion.class);
							//mensaje salida
							for(SolicitudesProcesoLiquidacion sol : solicitudesActualizar) {
								ResultadoLiquidacion res = new ResultadoLiquidacion();
								res.IdInterno = sol.IdInterno;
								res.IdSolicitudAporteRescate = sol.IdSolicitudAporteRescate;
								res.IdSerieFondo = sol.IdSerieFondo;
								res.Codigo = sol.Codigo;
								res.Estado = sol.EstadoLiquidacion;
								res.NombreEstado = "";
								out.P_OUT_RESULTADO.add(res);
							}
						}
					}
				
				
		for(SolicitudesProcesoLiquidacion solicitud : solicitudesLiquidar) {
			if(_c.SI.equals(liquidacionCtaInv) && solicitud.IdMedioDePago.equals("CIN") ) {
				List<ObjCargoAbonoAGenerar> lista_cargo_abono = new ArrayList<>();
				
				for(IngresoEgreso_Entrada ie : lista_ingreso_egreso_inv) {
					ObjCargoAbonoAGenerar obj = new ObjCargoAbonoAGenerar();
					obj.NumeroCargoAbono = i;
					obj.IdPortafolio = ie.IdPortafolio;
					obj.IdMoneda = ie.IdMoneda;
					obj.IdTipoMovimiento = ie.IdTipoMovimiento.equals(_c.TipoSolicitudAporte) ? "MOVABN" : "MOVCAR";
					obj.IdConcepto =   ie.IdTipoMovimiento.equals(_c.TipoSolicitudAporte) ? "ABPEFM" : "CAPEFM";
					obj.Descripcion =  ie.IdTipoMovimiento.equals(_c.TipoSolicitudAporte) ? "APORTE_FONDO_MUTUO" : "RESCATE_FONDO_MUTUO";
					obj.IdCondicionDeLiquidacion = "T0";
					obj.FechaCondicionDeLiquidacion = ie.FechaLiquidacion;
					obj.Monto = ie.Monto;
					obj.IdEjecutivo = 0L;
					obj.IdSucursal = 0L;
					obj.IdTipoProducto = "INFM";
					obj.IdTipoOperacion =  ie.IdTipoOperacion.equals("APORFM") ? "ABPEFM" :  ie.IdTipoMovimiento.equals(_c.TipoSolicitudAporte) ? "APORFM" : "RESCFM";
					obj.IdOperacion = ie.IdComprobante;
					obj.FechaOperacion = ie.FechaLiquidacion;
					obj.FechaLiquidacionOperacion = ie.FechaLiquidacionComprobante;
					obj.FechaVencimientoOperacion = null;
					obj.IdMonedaOperacion = ie.IdMoneda;
					obj.MontoOperacion = ie.Monto;
					obj.TipoCambioOperacion = BigDecimal.ZERO;
					obj.IdContraparte = 0L;
					obj.IdTipoComprobante = "FMM";
					obj.IdComprobanteDetalle = ie.IdComprobante;
					obj.FolioComprobante = ie.IdComprobante.toString();
					obj.IdComprobanteFormaDePago = ie.IdComprobante;
					obj.IdTipoMovimientoAsociado = "";
					obj.IdMovimientoAsociado = 0L;
					obj.FechaComprobante = ie.FechaLiquidacion;
					obj.IdPersonaPagoCobro = null;
					obj.IdInstrumento = 0L;
					obj.IdTipoInstrumento = "";
					obj.Nemo = "";
					obj.EsIndicadorBilateral = null;
					obj.EsCargoAbonoPorTraspaso = "N";
					obj.IdPortafolioTraspaso = null;
					obj.GeneroContraparteEnTesoreria = "N";
					obj.CertificadoAutomatico = "S";
					i++;
					lista_cargo_abono.add(obj);
				}
				
				CrearCargoAbonoEntrada entradaCargoAbono = new CrearCargoAbonoEntrada();
				entradaCargoAbono.p_IdEmpresa	= in.p_IdEmpresa;
				entradaCargoAbono.p_IdUsuario = in.p_IdUsuario;
				entradaCargoAbono.p_IdModulo = "FMM";
				entradaCargoAbono.p_simularOperacion  = "N"; 
				entradaCargoAbono.p_UsaReglasNegocio = "S";
				entradaCargoAbono.p_FechaSistema = fechaSistema;
				entradaCargoAbono.p_EsIngresoManual	= "N";
				entradaCargoAbono.p_CargoAbonoAGenerar = lista_cargo_abono;
				
				CrearCargoAbono crearCargoAbono = new CrearCargoAbono();
				CrearCargoAbonoSalida salidaCargoAbono = crearCargoAbono.logica(conn, entradaCargoAbono);
				
				//Dado que no se estan sumando errores, vamos a capturarlos
				Long errores = salidaCargoAbono.P_OUT_MENSAJES.stream().filter(p -> p.tipo.equalsIgnoreCase("ERROR")).count();
				//si hay errores salimos
				if(salidaCargoAbono.P_OUT_ERRORESCONTADOR > 0 || errores > 0) {
					out.P_OUT_ERRORESCONTADOR = salidaCargoAbono.P_OUT_ERRORESCONTADOR;
					out.P_OUT_MENSAJES = salidaCargoAbono.P_OUT_MENSAJES;
					return out;
				}else {
					
					for(ObjCargoAbonoGenerado salida : salidaCargoAbono.P_OUT_CARGOABONOGENERADO) {
						Long K=0L;
						List<SolicitudesProcesoLiquidacion> solicitudesActualizar = carta_solicitud.get(K);
						
						for(SolicitudesProcesoLiquidacion sol : solicitudesActualizar) {
							sol.FolioCartaLiquidacion = secuencia_carta.get(K.intValue());
							K++;
						}
						if(K > 0) {
							sql.batch(conn, SQL_UPDATE_SOLICITUDES, solicitudesActualizar, SolicitudesProcesoLiquidacion.class);
							//mensaje salida
							for(SolicitudesProcesoLiquidacion sol : solicitudesActualizar) {
								ResultadoLiquidacion res = new ResultadoLiquidacion();
								res.IdInterno = sol.IdInterno;
								res.IdSolicitudAporteRescate = sol.IdSolicitudAporteRescate;
								res.IdSerieFondo = sol.IdSerieFondo;
								res.Codigo = sol.Codigo;
								res.Estado = sol.EstadoLiquidacion;
								res.NombreEstado = "";
								out.P_OUT_RESULTADO.add(res);
							}
						}
					}
								
				}
				
			}
			
		 }	
			
		
		
		//agregamos log
		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
		entradaAgregarLog.p_IdTrabajo = 1L;
		List<AgregarLog.InAgregarLog.Log> logList = new ArrayList<>();
		for(InFiltros filtro : in.p_Filtros) {
			AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
			log.IdTrabajo		= entradaAgregarLog.p_IdTrabajo; 
			log.IdCabeceraLog	= filtro.IdInterno; 
			log.IdTipoLog		= "LIQUIDACION APORTE RESCATE"; 
			log.IdModulo		= in.p_IdModulo; 
			log.IdUsuario		= in.p_IdUsuario; 
			log.IdEmpresa		= in.p_IdEmpresa; 
			log.FechaSistemaLog	= fechaSistema; 
			log.IdUsuarioAtendido	= in.p_IdUsuario; 
			log.IdCliente			= 0L; 
			log.EstadoCliente		= ""; 
			log.NombreCliente		= ""; 
			log.IdTipoDocumentoIdentidad	= ""; 
			log.IdDocumentoIdentidad	= ""; 
			log.IdPortafolio			= 0L; 
			log.EstadoPortafolio		= ""; 
			log.IdTipoRegistro			= "LIQUIDACION_APORTE_RESCATE"; 
			log.IdRegistro				= "";
			log.EstadoRegistro			= "LIQ"; 
			log.IdTipoOperacion			= ""; 
			log.Folio					= 0L; 
			log.Cantidad				= BigDecimal.ZERO; 
			log.PrecioTasa				= BigDecimal.ZERO; 
			log.Monto					= BigDecimal.ZERO; 
			log.IdMonedaOrigen			= ""; 
			log.IdMonedaLiquidacion		= ""; 
			log.TipoCambio				= ""; 
			log.Nemo					=""; 
			log.IdProceso				= 0L; 
			log.EstadoProceso			= ""; 
			log.NombreProceso			= "cl.bcs.fondomutuo.LiquidacionSolicitudAporteRescate";
			logList.add(log);
		}
		
		AgregarLog agregarLogService = new AgregarLog();
		agregarLogService.logica(conn, entradaAgregarLog);
		
		log.write("FIN::FM.LiquidacionSolicitudAporteRescate");
		return out;
	};
	
	public static void main(String[] args) throws Exception,SQLException {
		// TODO Auto-generated method stub
		Connection conn = null;
		Gson gson = new Gson();
		try {
			System.out.println("== BEGIN ==");
			//conn = PlataformaUtils.getSURAConnection();
			conn = PlataformaUtils.getPRESURAConnection();
			//datos entrada
			//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":35,\"p_IdModulo\":\"FMM\",\"p_SimularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_FechaCertificacion\":\"2019-08-05\",\"p_Movimientos\":[{\"IdMovimiento\":1671778741},{\"IdMovimiento\":1671778739}],\"p_Filtros\":[{\"IdInterno\":0,\"IdEmisor\":\"\",\"TipoSolicitud\":\"RES\",\"IdEstado\":\"CRT\",\"FechaCertificacion\":\"\",\"FechaLiquidacion\":\"\",\"IdSerieFondo\":\"\"}]}";
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":15,\"p_IdModulo\":\"FMM\",\"p_SimularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_FechaCertificacion\":\"2019-12-16\",\"p_Movimientos\":[{\"IdMovimiento\":10883883}],\"p_Filtros\":[{\"IdInterno\":0,\"IdEmisor\":\"\",\"TipoSolicitud\":\"RES\",\"IdEstado\":\"CRT\",\"FechaCertificacion\":\"\",\"FechaLiquidacion\":\"\",\"IdSerieFondo\":\"\"}]}";
			
			LiquidacionSolicitudAporteRescate_IN objIn = gson.fromJson(json,LiquidacionSolicitudAporteRescate_IN.class);
			LiquidacionSolicitudAporteRescate demo = new LiquidacionSolicitudAporteRescate();
			LiquidacionSolicitudAporteRescate_OUT ObjOut=demo.logica(conn, objIn);
			System.out.println(ObjOut.toString());
			//conn.rollback();
			conn.commit();			
		}catch (Exception e){
			e.printStackTrace();
			conn.rollback();
		}finally {
			conn.close();
		}
		System.out.println("== END ==");
	};

};
