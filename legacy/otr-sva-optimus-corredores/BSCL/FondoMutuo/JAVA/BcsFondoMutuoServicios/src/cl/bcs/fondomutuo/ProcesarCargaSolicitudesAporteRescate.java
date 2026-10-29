package cl.bcs.fondomutuo;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.CertificarSolicitudAporteRescate.EntradaCertificarSolicitudAporteRescate;
import cl.bcs.fondomutuo.CertificarSolicitudAporteRescate.SalidaCertificarSolicitudAporteRescate;
import cl.bcs.fondomutuo.CrearAporteRescateFusion.EntradaCrearAporteRescateFusion;
import cl.bcs.fondomutuo.CrearAporteRescateFusion.InSolicitudesTitulo;
import cl.bcs.fondomutuo.CrearAporteRescateFusion.SalidaCrearAporteRescateFusion;
import cl.bcs.fondomutuo.CrearIngresoRetiroTituloFondoMutuo.EntradaCrearIngresoRetiroTituloFondoMutuo;
import cl.bcs.fondomutuo.CrearIngresoRetiroTituloFondoMutuo.SalidaCrearIngresoRetiroTituloFondoMutuo;
import cl.bcs.fondomutuo.CrearSolicitudAportePorDividendos.EntradaCrearSolicitudAportePorDividendos;
import cl.bcs.fondomutuo.CrearSolicitudAportePorDividendos.SalidaCrearSolicitudAportePorDividendos;
import cl.bcs.fondomutuo.CrearSolicitudAportePorDividendosCFI.EntradaCrearSolicitudAportePorDividendosCFI;
import cl.bcs.fondomutuo.CrearSolicitudAportePorDividendosCFI.SalidaCrearSolicitudAportePorDividendosCFI;
import cl.bcs.fondomutuo.CrearSolicitudAporteReinversion.EntradaCrearSolicitudAporteReinversion;
import cl.bcs.fondomutuo.CrearSolicitudAporteReinversion.InSolicitudesAporte;
import cl.bcs.fondomutuo.CrearSolicitudAporteReinversion.SalidaCrearSolicitudAporteReinversion;
import cl.bcs.fondomutuo.CrearSolicitudAporteRescateNormal.EntradaCrearSolicitudAporteRescateNormal;
import cl.bcs.fondomutuo.CrearSolicitudAporteRescateNormal.InSolicitudesAporteRescate;
import cl.bcs.fondomutuo.CrearSolicitudAporteRescateNormal.SalidaCrearSolicitudAporteRescateNormal;
import cl.bcs.fondomutuo.CrearSolicitudRescatePorComision.EntradaCrearSolicitudRescatePorComision;
import cl.bcs.fondomutuo.CrearSolicitudRescatePorComision.SalidaCrearSolicitudRescatePorComision;
import cl.bcs.fondomutuo.CrearSolicitudRescateReinversion.EntradaCrearSolicitudRescateReinversion;
import cl.bcs.fondomutuo.CrearSolicitudRescateReinversion.InSolicitudesRescate;
import cl.bcs.fondomutuo.CrearSolicitudRescateReinversion.SalidaCrearSolicitudRescateReinversion;
import cl.bcs.fondomutuo.ModificarComisionSolicitudPorPerfeccionamiento.EntradaModificarComisionSolicitudPorPerfeccionamiento;
import cl.bcs.fondomutuo.ModificarComisionSolicitudPorPerfeccionamiento.SalidaModificarComisionSolicitudPorPerfeccionamiento;
import cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo;
import cl.bcs.fondomutuo.pojo.ResultadoCertificacion;
import cl.bcs.fondomutuo.pojo.ResultadoSolicitud;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ProcesarCargaSolicitudesAporteRescate implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(CrearSolicitudAportePorDividendos.class);
	
	private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();
	
	private final String SQL_SELECT_DETALLE = loadResourceToString("cl/bcs/fondomutuo/sql/ProcesarCargaSolicitudesAporteRescate_SelectDetalleCarga.sql");
	private final String SQL_SELECT_MEDIOSDEPAGO = loadResourceToString("cl/bcs/fondomutuo/sql/ProcesarCargaSolicitudesAporteRescate_SelectMediosDePago.sql");
	private final String SQL_ACTUALIZAR_PROCESO = loadResourceToString("cl/bcs/fondomutuo/sql/ProcesarCargaSolicitudesAporteRescate_ActualizarProcesoDetalleCarga.sql");
	private final String SQL_ACTUALIZAR_DETALLE = loadResourceToString("cl/bcs/fondomutuo/sql/ProcesarCargaSolicitudesAporteRescate_ActualizarDetalle.sql");
	
	private final Integer limiteRegistrosPorProcesar = 500;
	
	public static class EntradaProcesarCargaSolicitudesAporteRescate extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public List<InCargas> p_IdCargas;
		
		public EntradaProcesarCargaSolicitudesAporteRescate(){
			this.p_IdCargas = new ArrayList<InCargas>();
		}
	}
	
	public static class InCargas extends BcsPojo{
		public Long IdInterno;
		public Long IdCarga;
	}
	
	public static class OutCargas extends BcsPojo{
		public Long IdCarga;
		public Long IdSolicitudAporteRescate;
		public String Descripcion;
		public String Codigo;
	}
	
	public static class SolicitudesProceso extends BcsPojo{
		public Long IdInterno;
		public Long IdCarga;
		public Long IdDetalleCarga;
		public Long IdSolicitudAporteRescate;
		public String TipoMovimiento;
		public Date FechaMovimiento;
		public Date FechaValorCuota;
		public String IdSerieFondo;
		public BigDecimal NumeroCuotas;
		public BigDecimal ValorCuota;
		public BigDecimal Monto;
		public BigDecimal MontoComision;
		public String IdMoneda;
		public String IdMonedaPago;
		public String IdMedioDePago;
		public String IdSerieFondoDestino;
		public Long IdCliente;
		public Long IdPortafolio;
		public String IdEmisor;
		public String IndicadorMLT;
		public String CalculoPorMontoCuota;
		public String IdRegimenTributarioOrigen;
		public String IdRegimenTributarioDestino;
		public String EstadoProceso;
		public String Codigo;
		public String Certifica;
		public String EsActualizacion;
		
		public SolicitudesProceso(){
			this.EstadoProceso = "PEN";
			this.Codigo = "";
		}
	}
	
	public static class SolicitudesMediosDePago extends BcsPojo{
		
		public Long IdPortafolio;
		public String IdTipoOperacion;
		public String IdMedioDePago;
		public String Nombre;
		public String IdMoneda;

	}
	
	public static class SalidaProcesarCargaSolicitudesAporteRescate extends BcsPojo{
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		public List <OutCargas> P_OUT_RESULTADO;
		
		public SalidaProcesarCargaSolicitudesAporteRescate(){
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_RESULTADO = new ArrayList<OutCargas>();
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaProcesarCargaSolicitudesAporteRescate jin = gson.fromJson(in.toString(), EntradaProcesarCargaSolicitudesAporteRescate.class);
		SalidaProcesarCargaSolicitudesAporteRescate out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaProcesarCargaSolicitudesAporteRescate logica(Connection con, EntradaProcesarCargaSolicitudesAporteRescate entrada) throws Exception{
		logger.info("INICIO::ProcesarCargaSolicitudesAporteRescate");
		long inicioServicio = System.currentTimeMillis();
		SalidaProcesarCargaSolicitudesAporteRescate out = new SalidaProcesarCargaSolicitudesAporteRescate();
		SQL sql=new SQL();
		List<Long> IdPortafolios = new ArrayList<Long>();
		
		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo);
		if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
			return out;
		}
		
		if(entrada.p_IdCargas.size() == 0){
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_INGRESARON_CARGAS",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
			return out;
		}
		
		ArrayList<Long> correlativos = new ArrayList<Long>();
		ArrayList<Long> ids = new ArrayList<Long>();
		
		for (InCargas mov: entrada.p_IdCargas) {
			if (correlativos.contains(mov.IdInterno)) {
				out.P_OUT_ERRORESCONTADOR = 1L;
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"CORRELATIVO_INCORRECTO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
				return out;
			} if (ids.contains(mov.IdCarga)) {
				out.P_OUT_ERRORESCONTADOR = 1L;
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"CARGA_DUPLICADA",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
				return out;
			}else {
				correlativos.add(mov.IdInterno);
				ids.add(mov.IdCarga);
			}
		}
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		String certificacionAutomaticaCargaMasiva = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "FONDOMUTUO", "CertificacionAutomaticaCargaMasiva");
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdCarga", ids);
		params.put("FechaSistema", fechaSistema);
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		List<SolicitudesProceso> solicitudes = sql.query(con, SQL_SELECT_DETALLE, params, SolicitudesProceso.class);
		params.clear();
		
		if(solicitudes.size() == 0){
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_NO_EXISTEN_SOLICITUDES_ASOCIADAS",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
			return out;
		}
		
		sql.batch(con, SQL_ACTUALIZAR_PROCESO, solicitudes, SolicitudesProceso.class);
		
		//ACTUALIZACION OPERACIONES
		{
			//SE ACTUALIZA LA COMISIONES PARA LAS SOLICITUDES ENVIADAS PARA PERFECCIONAMIENTO
			ModificarComisionSolicitudPorPerfeccionamiento modificacionComisionSolicitud = new ModificarComisionSolicitudPorPerfeccionamiento();
			EntradaModificarComisionSolicitudPorPerfeccionamiento entradaModificacion = new EntradaModificarComisionSolicitudPorPerfeccionamiento();
			for(SolicitudesProceso sol : solicitudes){
				IdPortafolios.add(sol.IdPortafolio);
				if(_c.EstadoPendiente.equals(sol.EstadoProceso) && sol.IdSolicitudAporteRescate.longValue() != 0L && _c.SI.equals(sol.EsActualizacion)
				   && (_c.AporteFondoMutuo.equals(sol.TipoMovimiento) || _c.RescateFondoMutuo.equals(sol.TipoMovimiento) || _c.AporteReinversion.equals(sol.TipoMovimiento)
						   || _c.AporteDividendo.equals(sol.TipoMovimiento) || _c.Reinversion.equals(sol.TipoMovimiento))){
					ModificarComisionSolicitudPorPerfeccionamiento.InSolicitudes s = new ModificarComisionSolicitudPorPerfeccionamiento.InSolicitudes();
					s.IdInterno = sol.IdDetalleCarga;
					s.IdSolicitudAporteRescate = sol.IdSolicitudAporteRescate;
					s.IdCliente = sol.IdCliente;
					s.IdPortafolio = sol.IdPortafolio;
					s.IdSerieFondo = sol.IdSerieFondo;
					s.CalculoPorMontoCuota = sol.CalculoPorMontoCuota;
					s.TipoSolicitud = _c.RescateFondoMutuo.equals(sol.TipoMovimiento) || _c.Reinversion.equals(sol.TipoMovimiento) ? _c.TipoSolicitudRescate : _c.TipoSolicitudAporte;
					s.IdMoneda = sol.IdMoneda;
					s.Monto = sol.Monto;
					s.NumeroCuotas = sol.NumeroCuotas;
					s.MontoComision = sol.MontoComision;
					entradaModificacion.p_Solicitudes.add(s);
				}
			}
			if(entradaModificacion.p_Solicitudes.size() > 0){
				entradaModificacion.p_IdEmpresa = entrada.p_IdEmpresa;
				entradaModificacion.p_IdUsuario = entrada.p_IdUsuario;
				entradaModificacion.p_IdModulo = entrada.p_IdModulo;
				entradaModificacion.p_SimularOperacion = _c.NO;
				entradaModificacion.p_UsaReglasNegocio = _c.SI;
				SalidaModificarComisionSolicitudPorPerfeccionamiento salidaModificacion = modificacionComisionSolicitud.logica(con, entradaModificacion);
				for(MensajeSalida mje : salidaModificacion.P_OUT_MENSAJES){
					if(MensajeSalida.MENSAJE_ERROR.equals(mje.tipo)){
						out.P_OUT_MENSAJES.add(mje);
						Long id = Math.abs(mje.id);
						for(SolicitudesProceso sol : solicitudes){
							if(sol.IdDetalleCarga.longValue() == id.longValue()){
								sol.EstadoProceso = _c.EstadoRechazado;
								sol.Codigo = mje.codigo;
								break;
							}
						}
					}
				}
				for(ModificarComisionSolicitudPorPerfeccionamiento.OutSolicitudes res : salidaModificacion.P_OUT_RESULTADO){
					for(SolicitudesProceso sol : solicitudes){
						if(sol.IdDetalleCarga.longValue() == res.IdInterno.longValue()){
							sol.Certifica = _c.SI;
							break;
						}
					}
				}
			}
		}
			
			//INGRESO DE OPERACIONES
		{	
			//1 - APORTES Y RESCATES NORMALES
			CrearSolicitudAporteRescateNormal crearSolicitudAporteRescateNormal = new CrearSolicitudAporteRescateNormal();
			EntradaCrearSolicitudAporteRescateNormal entradaCrearSolicitudAporteRescateNormal = new EntradaCrearSolicitudAporteRescateNormal();

			CalcularMontoNumeroCuota.EntradaCalcularMontoNumeroCuota  entradaCalcularMontoNumeroCuota = new CalcularMontoNumeroCuota.EntradaCalcularMontoNumeroCuota();
			entradaCalcularMontoNumeroCuota.p_IdEmpresa = entrada.p_IdEmpresa;
			
			params.put("IdPortafolios", IdPortafolios);
			params.put("IdEmpresa", entrada.p_IdEmpresa);
			List<SolicitudesMediosDePago> solicitudesMediosDePago = sql.query(con, SQL_SELECT_MEDIOSDEPAGO, params, SolicitudesMediosDePago.class);
			List<SolicitudesMediosDePago>  mediosDePago = new ArrayList<SolicitudesMediosDePago>(0);
			params.clear();
			
			for(SolicitudesProceso sol : solicitudes){
				if(_c.EstadoPendiente.equals(sol.EstadoProceso) && sol.IdSolicitudAporteRescate.longValue() == 0 && _c.NO.equals(sol.EsActualizacion)
				   && (_c.AporteFondoMutuo.equals(sol.TipoMovimiento) || _c.RescateFondoMutuo.equals(sol.TipoMovimiento))){
					CrearSolicitudAporteRescateNormal.InSolicitudesAporteRescate s = new CrearSolicitudAporteRescateNormal.InSolicitudesAporteRescate();
					if ( _c.RescateFondoMutuo.equals(sol.TipoMovimiento) &&
						 !sol.IdMedioDePago.equals("")  &&
						 !sol.IdMedioDePago.equals(_c.CuentaInversion) ) {
						mediosDePago  =  solicitudesMediosDePago.stream()
											.filter(item -> item.IdPortafolio.equals(sol.IdPortafolio) && 
													item.IdMedioDePago.equals(sol.IdMedioDePago)	   &&
													item.IdMoneda.equals(sol.IdMonedaPago.trim()))
											.collect(Collectors.toList());
					}
					s.IdInterno = sol.IdDetalleCarga;
					s.IdCliente = sol.IdCliente;
					s.IdPortafolio = sol.IdPortafolio;
					s.TipoSolicitud = _c.RescateFondoMutuo.equals(sol.TipoMovimiento) ? _c.TipoSolicitudRescate : _c.TipoSolicitudAporte;
					s.IdSerieFondo = sol.IdSerieFondo;
					s.CalculoPorMontoCuota = sol.CalculoPorMontoCuota;
					s.EsPorMontoLiquido = _c.NO;
					s.Monto = sol.Monto;
					s.NumeroCuotas = sol.NumeroCuotas;
					s.IdMoneda = sol.IdMoneda;
					s.IdMonedaPago = sol.IdMonedaPago.trim();
					s.IdMedioDePago = _c.RescateFondoMutuo.equals(sol.TipoMovimiento) && mediosDePago.size() > 0 ? sol.IdMedioDePago : _c.CuentaInversion;
					s.IdPreferenciaPagoPortafolio = 0L;
					s.Visado = _c.NO;
					s.SolicitudBatch = _c.SI;
					s.OrigenOperacion = _c.OrigenManual;
					s.FechaOperacion = "0001-01-01";
					s.IdRegimenTributario = sol.IdRegimenTributarioOrigen;
					s.Glosa = "";
					s.IndicadorMLT = _c.NO;
					s.IdSerieFondoDestino = "";
					s.IdEmisorDestino = "";
					s.IdAutorizadoOrigen = 0L;
					s.Porcentaje = BigDecimal.ZERO;
					s.FolioContrato = 0L;
					s.IngresoAutomatico = _c.SI;
					s.FechaValorCuota = (sol.FechaValorCuota.toString().equals("0001-01-01")) ? null : sol.FechaValorCuota;
					s.IngresoDesde = "CORREDORES";
					if(_c.AporteFondoMutuo.equals(sol.TipoMovimiento) && !sol.IdMonedaPago.isEmpty() && !(sol.IdMoneda.equalsIgnoreCase(sol.IdMonedaPago)) ) {
						
						logger.info("genero SPOT  " + s.IdInterno);
						
						/***
						 * INICIO SPOT
						 * 
						 */
						CrearSolicitudAporteRescateNormal.InFormaPagoSpot pagoSpot = new CrearSolicitudAporteRescateNormal.InFormaPagoSpot();
						
						long contadorInterno = 1L;
						
						pagoSpot.IdInternoPagoOperacion = contadorInterno;
						pagoSpot.IdComprobantePagoOperacion = 0L;
						pagoSpot.IdTipoMovimiento = "MOVEGR";
						pagoSpot.IdFormaDePago = "VENSPT_CIN_USD_0";
						pagoSpot.IdOperacion = s.IdInterno;
						pagoSpot.RecibePagaPortafolio = "P";
						pagoSpot.IdMoneda = s.IdMoneda;
						pagoSpot.IdMonedaLiquidacion = s.IdMoneda;
						pagoSpot.MontoFormaDePago = s.Monto;
						pagoSpot.AfectarTesoreria = "N";
						
						entradaCrearSolicitudAporteRescateNormal.p_FormaPagoSpot.add(pagoSpot);
						
						CrearSolicitudAporteRescateNormal.InFormaPagoSpot pagoSpot1 = new CrearSolicitudAporteRescateNormal.InFormaPagoSpot();
						
						pagoSpot1.IdInternoPagoOperacion = (contadorInterno + 1);
						pagoSpot1.IdComprobantePagoOperacion = 0L;
						pagoSpot1.IdTipoMovimiento = "MOVING";
						pagoSpot1.IdFormaDePago = "COMSPT_CIN_CLP_0";
						pagoSpot1.IdOperacion = s.IdInterno;
						pagoSpot1.RecibePagaPortafolio = "R";
						pagoSpot1.IdMoneda = s.IdMonedaPago;
						pagoSpot1.IdMonedaLiquidacion = s.IdMonedaPago;
						pagoSpot1.MontoFormaDePago = BigDecimal.ZERO;
						pagoSpot1.AfectarTesoreria = "N";
						
						entradaCrearSolicitudAporteRescateNormal.p_FormaPagoSpot.add(pagoSpot1);
						/**
						 * 			FIN SPOT
						 * 
						 */
						
						//serie para obtener valor de MontoFormaDePago de Spot1 
						CalcularMontoNumeroCuota.Serie serie = new CalcularMontoNumeroCuota.Serie();
						serie.IdInterno = s.IdInterno;
						serie.Fecha = fechaSistema;
						serie.IdSerieFondo = s.IdSerieFondo;
						serie.CalculoPorMontoCuota = s.CalculoPorMontoCuota;
						serie.Monto = s.Monto;
						serie.NumeroCuotas = s.NumeroCuotas;
						serie.IdMonedaFondo = s.IdMoneda;
						serie.IdMonedaInversion = s.IdMoneda;
						serie.TipoSolicitud = s.TipoSolicitud;
						serie.TipoFactorAjuste = _c.UF;
						serie.IdEmpresa = entrada.p_IdEmpresa;
						
						entradaCalcularMontoNumeroCuota.p_Series.add(serie);
					}

					entradaCrearSolicitudAporteRescateNormal.p_SolicitudesAporteRescate.add(s);
				}
				mediosDePago.clear();
			}
			
			CalcularMontoNumeroCuota calcularMontoNumeroCuota = new CalcularMontoNumeroCuota();
			CalcularMontoNumeroCuota.SalidaCalcularMontoNumeroCuota salidaCalcularMontoNumeroCuota = calcularMontoNumeroCuota.logica(con, entradaCalcularMontoNumeroCuota);
			HashMap<String,Object> hMontoNumeroCuota = Utils.List2Hash(salidaCalcularMontoNumeroCuota.P_OUT_RESULTADO, "IdInterno");
			
			if(entradaCrearSolicitudAporteRescateNormal.p_FormaPagoSpot.size() > 0) {
			
				for(CrearSolicitudAporteRescateNormal.InFormaPagoSpot spot : entradaCrearSolicitudAporteRescateNormal.p_FormaPagoSpot) {
				
					CalcularMontoNumeroCuota.Resultado montoCuota = (CalcularMontoNumeroCuota.Resultado) hMontoNumeroCuota.get(spot.IdOperacion);
					
					//se asume que es la segunda Operacion Spot "MOVEGR"
					if(spot.IdOperacion.equals(montoCuota.IdInterno) && spot.IdTipoMovimiento.equalsIgnoreCase("MOVING") ) {

						spot.MontoFormaDePago = montoCuota.MontoMonedaSistema;
					}
				}
			}
			
			
			if(entradaCrearSolicitudAporteRescateNormal.p_SolicitudesAporteRescate.size() > 0){
				entradaCrearSolicitudAporteRescateNormal.p_IdEmpresa = entrada.p_IdEmpresa;
				entradaCrearSolicitudAporteRescateNormal.p_IdUsuario = entrada.p_IdUsuario;
				entradaCrearSolicitudAporteRescateNormal.p_IdModulo = entrada.p_IdModulo;
				entradaCrearSolicitudAporteRescateNormal.p_SimularOperacion = _c.NO;
				entradaCrearSolicitudAporteRescateNormal.p_UsaReglasNegocio = _c.SI;
				entradaCrearSolicitudAporteRescateNormal.p_IdTipoCanalIngreso = _c.CanalAgente;
				entradaCrearSolicitudAporteRescateNormal.p_NumeroDividendo = 0L;
				entradaCrearSolicitudAporteRescateNormal.p_IdOrigenProceso = _c.OrigenCargaMasivaFM;
				
				int cantidadRegistros = entradaCrearSolicitudAporteRescateNormal.p_SolicitudesAporteRescate.size();
				SalidaCrearSolicitudAporteRescateNormal salidaCrearSolicitudAporteRescateNormal = new SalidaCrearSolicitudAporteRescateNormal();
				if(cantidadRegistros > limiteRegistrosPorProcesar) {
					//Segmentar carga
					int index = 0;
					int limite = 0;
					List<InSolicitudesAporteRescate> listaRespaldoSolicitudes = entradaCrearSolicitudAporteRescateNormal.p_SolicitudesAporteRescate;
					while(index < cantidadRegistros) {
						limite =  index + limiteRegistrosPorProcesar;
						if(index+limiteRegistrosPorProcesar > cantidadRegistros) {
							limite = cantidadRegistros;
						}
						entradaCrearSolicitudAporteRescateNormal.p_SolicitudesAporteRescate = listaRespaldoSolicitudes.subList(index, limite);
						SalidaCrearSolicitudAporteRescateNormal salidaCrearSolicitudAporteRescateNormalParcial = crearSolicitudAporteRescateNormal.logica(con, entradaCrearSolicitudAporteRescateNormal);
						if(salidaCrearSolicitudAporteRescateNormalParcial != null) {
							if(salidaCrearSolicitudAporteRescateNormalParcial.P_OUT_ERRORESCONTADOR != null) {
								salidaCrearSolicitudAporteRescateNormal.P_OUT_ERRORESCONTADOR += salidaCrearSolicitudAporteRescateNormalParcial.P_OUT_ERRORESCONTADOR;
							}
							if(salidaCrearSolicitudAporteRescateNormalParcial.P_OUT_MENSAJES != null) {
								salidaCrearSolicitudAporteRescateNormal.P_OUT_MENSAJES.addAll(salidaCrearSolicitudAporteRescateNormalParcial.P_OUT_MENSAJES);
							}
							if(salidaCrearSolicitudAporteRescateNormalParcial.P_OUT_RESULTADO != null) {
								salidaCrearSolicitudAporteRescateNormal.P_OUT_RESULTADO.addAll(salidaCrearSolicitudAporteRescateNormalParcial.P_OUT_RESULTADO);
							}
						}
						index += limiteRegistrosPorProcesar;
					}
				}else {
					salidaCrearSolicitudAporteRescateNormal = crearSolicitudAporteRescateNormal.logica(con, entradaCrearSolicitudAporteRescateNormal);
				}
				
				for(MensajeSalida mje : salidaCrearSolicitudAporteRescateNormal.P_OUT_MENSAJES){
					if(MensajeSalida.MENSAJE_ERROR.equals(mje.tipo)){
						out.P_OUT_MENSAJES.add(mje);
						Long id = Math.abs(mje.id);
						for(SolicitudesProceso sol : solicitudes){
							if(sol.IdDetalleCarga.longValue() == id.longValue()){
								sol.EstadoProceso = _c.EstadoRechazado;
								sol.Codigo = mje.descripcion;
								break;
							}
						}
					}
				}
				for(ResultadoSolicitud res : salidaCrearSolicitudAporteRescateNormal.P_OUT_RESULTADO){
					for(SolicitudesProceso sol : solicitudes){
						if(sol.IdDetalleCarga.longValue() == res.IdInterno.longValue()){
							sol.IdSolicitudAporteRescate = res.IdSolicitudAporteRescate;
							sol.Certifica = _c.SI;
							break;
						}
					}
				}
			}
		}
		
		{
			//2 - APORTE REINVERSIONES
			CrearSolicitudAporteReinversion crearSolicitudAporteReinversion = new CrearSolicitudAporteReinversion();
			EntradaCrearSolicitudAporteReinversion entradaCrearSolicitudAporteReinversion = new EntradaCrearSolicitudAporteReinversion(); 
			
			for(SolicitudesProceso sol : solicitudes){
				if(_c.EstadoPendiente.equals(sol.EstadoProceso) && sol.IdSolicitudAporteRescate.longValue() == 0 && _c.NO.equals(sol.EsActualizacion)
				   && _c.AporteReinversion.equals(sol.TipoMovimiento)){
					CrearSolicitudAporteReinversion.InSolicitudesAporte s = new CrearSolicitudAporteReinversion.InSolicitudesAporte();
					s.IdInterno = sol.IdDetalleCarga;
					s.IdCliente = sol.IdCliente;
					s.IdPortafolio = sol.IdPortafolio;
					s.IdSerieFondoOrigen = sol.IdSerieFondo;
					s.IdEmisorOrigen = sol.IdEmisor;
					s.CostoUF = BigDecimal.ZERO;
					s.FechaOrigenInversion = Fecha.stringToSqlDate(sol.FechaMovimiento);
					s.MontoOrigen = sol.Monto;
					s.IndicadorMLT = sol.IndicadorMLT;
					s.IdSerieFondo = sol.IdSerieFondoDestino;
					s.Monto = sol.Monto;
					s.NumeroCuotas = sol.NumeroCuotas;
					s.IdMoneda = sol.IdMoneda;
					s.IdRegimenTributario = sol.IdRegimenTributarioOrigen;
					s.IdMedioDePago = _c.CuentaInversion;
					s.Visado = _c.NO;
					s.SolicitudBatch = _c.SI;
					s.OrigenOperacion = _c.OrigenManual;
					s.Glosa = "";
					s.IdAutorizadoOrigen = 0L;
					s.FolioContrato = 0L;
					s.IngresoAutomatico = _c.SI;
					s.FechaValorCuota = (sol.FechaValorCuota.toString().equals("0001-01-01")) ? null : sol.FechaValorCuota;
					entradaCrearSolicitudAporteReinversion.p_SolicitudesAporte.add(s);
				}
			}
			
			if(entradaCrearSolicitudAporteReinversion.p_SolicitudesAporte.size() > 0){
				entradaCrearSolicitudAporteReinversion.p_IdEmpresa = entrada.p_IdEmpresa;
				entradaCrearSolicitudAporteReinversion.p_IdUsuario = entrada.p_IdUsuario;
				entradaCrearSolicitudAporteReinversion.p_IdModulo = entrada.p_IdModulo;
				entradaCrearSolicitudAporteReinversion.p_SimularOperacion = _c.NO;
				entradaCrearSolicitudAporteReinversion.p_UsaReglasNegocio = _c.SI;
				entradaCrearSolicitudAporteReinversion.p_IdTipoCanalIngreso = _c.CanalAgente;
				int cantidadRegistros = entradaCrearSolicitudAporteReinversion.p_SolicitudesAporte.size();
				SalidaCrearSolicitudAporteReinversion salidaCrearSolicitudAporteReinversion = new SalidaCrearSolicitudAporteReinversion();
				if(cantidadRegistros > limiteRegistrosPorProcesar) {
					//Segmentar carga
					int index = 0;
					int limite = 0;
					List<InSolicitudesAporte> listaRespaldoSolicitudes = entradaCrearSolicitudAporteReinversion.p_SolicitudesAporte;
					while(index < cantidadRegistros) {
						limite =  index + limiteRegistrosPorProcesar;
						if(index+limiteRegistrosPorProcesar > cantidadRegistros) {
							limite = cantidadRegistros;
						}
						entradaCrearSolicitudAporteReinversion.p_SolicitudesAporte = listaRespaldoSolicitudes.subList(index, limite);
						SalidaCrearSolicitudAporteReinversion salidaCrearSolicitudAporteReinversionParcial = crearSolicitudAporteReinversion.logica(con, entradaCrearSolicitudAporteReinversion);
						if(salidaCrearSolicitudAporteReinversionParcial != null) {
							if(salidaCrearSolicitudAporteReinversionParcial.P_OUT_ERRORESCONTADOR != null) {
								salidaCrearSolicitudAporteReinversion.P_OUT_ERRORESCONTADOR += salidaCrearSolicitudAporteReinversionParcial.P_OUT_ERRORESCONTADOR;
							}
							if(salidaCrearSolicitudAporteReinversionParcial.P_OUT_MENSAJES != null) {
								salidaCrearSolicitudAporteReinversion.P_OUT_MENSAJES.addAll(salidaCrearSolicitudAporteReinversionParcial.P_OUT_MENSAJES);
							}
							if(salidaCrearSolicitudAporteReinversionParcial.P_OUT_RESULTADO != null) {
								salidaCrearSolicitudAporteReinversion.P_OUT_RESULTADO.addAll(salidaCrearSolicitudAporteReinversionParcial.P_OUT_RESULTADO);
							}
						}
						index += limiteRegistrosPorProcesar;
					}
				}else {
					salidaCrearSolicitudAporteReinversion = crearSolicitudAporteReinversion.logica(con, entradaCrearSolicitudAporteReinversion);
				}
				for(MensajeSalida mje : salidaCrearSolicitudAporteReinversion.P_OUT_MENSAJES){
					if(MensajeSalida.MENSAJE_ERROR.equals(mje.tipo)){
						out.P_OUT_MENSAJES.add(mje);
						Long id = Math.abs(mje.id);
						for(SolicitudesProceso sol : solicitudes){
							if(sol.IdDetalleCarga.longValue() == id.longValue()){
								sol.EstadoProceso = _c.EstadoRechazado;
								sol.Codigo = mje.codigo;
								break;
							}
						}
					}
				}
				for(ResultadoSolicitud res : salidaCrearSolicitudAporteReinversion.P_OUT_RESULTADO){
					for(SolicitudesProceso sol : solicitudes){
						if(sol.IdDetalleCarga.longValue() == res.IdInterno.longValue()){
							sol.IdSolicitudAporteRescate = res.IdSolicitudAporteRescate;
							sol.Certifica = _c.SI;
							break;
						}
					}
				}
			}
		}
		
		{
			//3 - RESCATE REINVERSIONES
			CrearSolicitudRescateReinversion crearSolicitudRescateReinversion = new CrearSolicitudRescateReinversion();
			EntradaCrearSolicitudRescateReinversion entradaCrearSolicitudRescateReinversion = new EntradaCrearSolicitudRescateReinversion();
			for(SolicitudesProceso sol : solicitudes){
				if(_c.EstadoPendiente.equals(sol.EstadoProceso) && sol.IdSolicitudAporteRescate.longValue() == 0 && _c.NO.equals(sol.EsActualizacion)
				   && _c.Reinversion.equals(sol.TipoMovimiento)){
					CrearSolicitudRescateReinversion.InSolicitudesRescate r = new CrearSolicitudRescateReinversion.InSolicitudesRescate();
					r.IdInterno = sol.IdDetalleCarga;
					r.IdCliente = sol.IdCliente;
					r.IdPortafolio = sol.IdPortafolio;
					r.IdSerieFondo = sol.IdSerieFondo;
					r.CalculoPorMontoCuota = sol.CalculoPorMontoCuota;
					r.Monto = sol.Monto;
					r.NumeroCuotas = sol.NumeroCuotas;
					r.IdMoneda = sol.IdMoneda;
					r.IdMedioDePago = _c.CuentaInversion;
					r.SolicitudBatch = _c.SI;
					r.OrigenOperacion = _c.OrigenManual;
					r.Glosa = "";
					r.IdAutorizadoOrigen = 0L;
					r.FolioContrato = 0L;
					r.IdRegimenTributario = sol.IdRegimenTributarioOrigen;
					r.IdAgrupador = sol.IdDetalleCarga;
					r.IngresoAutomatico = _c.SI;
					r.FechaValorCuota = (sol.FechaValorCuota.toString().equals("0001-01-01")) ? null : sol.FechaValorCuota;
					CrearSolicitudRescateReinversion.InSolicitudesAporte a = new CrearSolicitudRescateReinversion.InSolicitudesAporte();
					a.IdInterno = sol.IdDetalleCarga;
					a.IdCliente = sol.IdCliente;
					a.IdPortafolio = sol.IdPortafolio;
					a.IdSerieFondo = sol.IdSerieFondoDestino;
					a.IdEmisor = sol.IdEmisor;
					a.Monto = sol.Monto;
					a.NumeroCuotas = sol.NumeroCuotas;
					a.IndicadorMLT = sol.IndicadorMLT;
					a.IdMoneda = sol.IdMoneda;
					a.IdMedioDePago = _c.CuentaInversion;
					a.IdRegimenTributario = "";
					a.Glosa = "";
					a.IdRegimenTributario = sol.IdRegimenTributarioDestino;
					a.IdAgrupador = sol.IdDetalleCarga;
					entradaCrearSolicitudRescateReinversion.p_SolicitudesRescate.add(r);
					entradaCrearSolicitudRescateReinversion.p_SolicitudesAporte.add(a);
				}
			}
			
			if(entradaCrearSolicitudRescateReinversion.p_SolicitudesRescate.size() > 0){
				entradaCrearSolicitudRescateReinversion.p_IdEmpresa = entrada.p_IdEmpresa;
				entradaCrearSolicitudRescateReinversion.p_IdUsuario = entrada.p_IdUsuario;
				entradaCrearSolicitudRescateReinversion.p_IdModulo = entrada.p_IdModulo;
				entradaCrearSolicitudRescateReinversion.p_SimularOperacion = _c.NO;
				entradaCrearSolicitudRescateReinversion.p_UsaReglasNegocio = _c.SI;
				entradaCrearSolicitudRescateReinversion.p_IdTipoCanalIngreso = _c.CanalAgente;
				entradaCrearSolicitudRescateReinversion.p_IdOrigenProceso = _c.OrigenCargaMasivaFM;
				int cantidadRegistros = entradaCrearSolicitudRescateReinversion.p_SolicitudesRescate.size();
				SalidaCrearSolicitudRescateReinversion salidaCrearSolicitudRescateReinversion = new SalidaCrearSolicitudRescateReinversion();
				if(cantidadRegistros > limiteRegistrosPorProcesar) {
					entradaCrearSolicitudRescateReinversion.p_SolicitudesRescate.sort(Comparator.comparingLong(sol -> sol.IdInterno));
					entradaCrearSolicitudRescateReinversion.p_SolicitudesAporte.sort(Comparator.comparingLong(sol -> sol.IdInterno));
					//Segmentar carga
					int index = 0;
					int limite = 0;
					List<InSolicitudesRescate> listaRespaldoSolicitudesRescate = entradaCrearSolicitudRescateReinversion.p_SolicitudesRescate;
					List<cl.bcs.fondomutuo.CrearSolicitudRescateReinversion.InSolicitudesAporte> listaRespaldoSolicitudesAportes = entradaCrearSolicitudRescateReinversion.p_SolicitudesAporte;
					while(index < cantidadRegistros) {
						limite =  index + limiteRegistrosPorProcesar;
						if(index+limiteRegistrosPorProcesar > cantidadRegistros) {
							limite = cantidadRegistros;
						}
						entradaCrearSolicitudRescateReinversion.p_SolicitudesRescate = listaRespaldoSolicitudesRescate.subList(index, limite);
						entradaCrearSolicitudRescateReinversion.p_SolicitudesAporte = listaRespaldoSolicitudesAportes.subList(index, limite);
						SalidaCrearSolicitudRescateReinversion salidaCrearSolicitudRescateReinversionParcial = crearSolicitudRescateReinversion.logica(con, entradaCrearSolicitudRescateReinversion);
						if(salidaCrearSolicitudRescateReinversionParcial != null) {
							if(salidaCrearSolicitudRescateReinversionParcial.P_OUT_ERRORESCONTADOR != null) {
								salidaCrearSolicitudRescateReinversion.P_OUT_ERRORESCONTADOR += salidaCrearSolicitudRescateReinversionParcial.P_OUT_ERRORESCONTADOR;
							}
							if(salidaCrearSolicitudRescateReinversionParcial.P_OUT_MENSAJES != null) {
								salidaCrearSolicitudRescateReinversion.P_OUT_MENSAJES.addAll(salidaCrearSolicitudRescateReinversionParcial.P_OUT_MENSAJES);
							}
							if(salidaCrearSolicitudRescateReinversionParcial.P_OUT_RESULTADO != null) {
								salidaCrearSolicitudRescateReinversion.P_OUT_RESULTADO.addAll(salidaCrearSolicitudRescateReinversionParcial.P_OUT_RESULTADO);
							}
						}
						index += limiteRegistrosPorProcesar;
					}
				}else {
					salidaCrearSolicitudRescateReinversion = crearSolicitudRescateReinversion.logica(con, entradaCrearSolicitudRescateReinversion);
				}
				
				for(MensajeSalida mje : salidaCrearSolicitudRescateReinversion.P_OUT_MENSAJES){
					if(MensajeSalida.MENSAJE_ERROR.equals(mje.tipo)){
						out.P_OUT_MENSAJES.add(mje);
						Long id = Math.abs(mje.id);
						for(SolicitudesProceso sol : solicitudes){
							if(sol.IdDetalleCarga.longValue() == id.longValue()){
								sol.EstadoProceso = _c.EstadoRechazado;
								sol.Codigo = mje.codigo;
								break;
							}
						}
					}
				}
				for(ResultadoSolicitud res : salidaCrearSolicitudRescateReinversion.P_OUT_RESULTADO){
					for(SolicitudesProceso sol : solicitudes){
						if(sol.IdDetalleCarga.longValue() == res.IdInterno.longValue()){
							sol.IdSolicitudAporteRescate = res.IdSolicitudAporteRescate;
							sol.Certifica = _c.SI;
							break;
						}
					}
				}
			}
		}
		
		{
			//4 - RESCATE POR COMISIONES
			CrearSolicitudRescatePorComision crearSolicitudRescatePorComision = new CrearSolicitudRescatePorComision();
			EntradaCrearSolicitudRescatePorComision entradaCrearSolicitudRescatePorComision = new EntradaCrearSolicitudRescatePorComision();
			for(SolicitudesProceso sol : solicitudes){
				if(_c.EstadoPendiente.equals(sol.EstadoProceso) && sol.IdSolicitudAporteRescate.longValue() == 0 && _c.NO.equals(sol.EsActualizacion)
				   && _c.RescateComision.equals(sol.TipoMovimiento)){
					CrearSolicitudRescatePorComision.InSolicitudesRescate s = new CrearSolicitudRescatePorComision.InSolicitudesRescate();
					s.IdInterno = sol.IdDetalleCarga;
					s.IdCliente = sol.IdCliente;
					s.IdPortafolio = sol.IdPortafolio;
					s.IdSerieFondo = sol.IdSerieFondo;
					s.Monto = sol.Monto;
					s.NumeroCuotas = sol.NumeroCuotas;
					s.IdMoneda = sol.IdMoneda;
					s.FolioContrato = 0L;
					s.IdRegimenTributario = sol.IdRegimenTributarioOrigen;
					s.IngresoAutomatico = _c.NO;
					s.FechaValorCuota = (sol.FechaValorCuota.toString().equals("0001-01-01")) ? null : sol.FechaValorCuota;
					entradaCrearSolicitudRescatePorComision.p_SolicitudesRescate.add(s);
				}
			}
			if(entradaCrearSolicitudRescatePorComision.p_SolicitudesRescate.size() > 0){
				entradaCrearSolicitudRescatePorComision.p_IdEmpresa = entrada.p_IdEmpresa;
				entradaCrearSolicitudRescatePorComision.p_IdUsuario = entrada.p_IdUsuario;
				entradaCrearSolicitudRescatePorComision.p_IdModulo = entrada.p_IdModulo;
				entradaCrearSolicitudRescatePorComision.p_SimularOperacion = _c.NO;
				entradaCrearSolicitudRescatePorComision.p_UsaReglasNegocio = _c.SI;
				entradaCrearSolicitudRescatePorComision.p_IdTipoCanalIngreso = _c.CanalAgente;
				int cantidadRegistros = entradaCrearSolicitudRescatePorComision.p_SolicitudesRescate.size();
				SalidaCrearSolicitudRescatePorComision salidaCrearSolicitudRescatePorComision = new SalidaCrearSolicitudRescatePorComision();
				if(cantidadRegistros > limiteRegistrosPorProcesar) {
					//Segmentar carga
					int index = 0;
					int limite = 0;
					List<cl.bcs.fondomutuo.CrearSolicitudRescatePorComision.InSolicitudesRescate> listaRespaldoSolicitudes = entradaCrearSolicitudRescatePorComision.p_SolicitudesRescate;
					while(index < cantidadRegistros) {
						limite =  index + limiteRegistrosPorProcesar;
						if(index+limiteRegistrosPorProcesar > cantidadRegistros) {
							limite = cantidadRegistros;
						}
						entradaCrearSolicitudRescatePorComision.p_SolicitudesRescate = listaRespaldoSolicitudes.subList(index, limite);
						SalidaCrearSolicitudRescatePorComision salidaCrearSolicitudRescatePorComisionParcial = crearSolicitudRescatePorComision.logica(con, entradaCrearSolicitudRescatePorComision);
						if(salidaCrearSolicitudRescatePorComisionParcial != null) {
							if(salidaCrearSolicitudRescatePorComisionParcial.P_OUT_ERRORESCONTADOR != null) {
								salidaCrearSolicitudRescatePorComision.P_OUT_ERRORESCONTADOR += salidaCrearSolicitudRescatePorComisionParcial.P_OUT_ERRORESCONTADOR;
							}
							if(salidaCrearSolicitudRescatePorComisionParcial.P_OUT_MENSAJES != null) {
								salidaCrearSolicitudRescatePorComision.P_OUT_MENSAJES.addAll(salidaCrearSolicitudRescatePorComisionParcial.P_OUT_MENSAJES);
							}
							if(salidaCrearSolicitudRescatePorComisionParcial.P_OUT_RESULTADO != null) {
								salidaCrearSolicitudRescatePorComision.P_OUT_RESULTADO.addAll(salidaCrearSolicitudRescatePorComisionParcial.P_OUT_RESULTADO);
							}
						}
						index += limiteRegistrosPorProcesar;
					}
				} else {
					salidaCrearSolicitudRescatePorComision = crearSolicitudRescatePorComision.logica(con, entradaCrearSolicitudRescatePorComision);
				}
				for(MensajeSalida mje : salidaCrearSolicitudRescatePorComision.P_OUT_MENSAJES){
					if(MensajeSalida.MENSAJE_ERROR.equals(mje.tipo)){
						out.P_OUT_MENSAJES.add(mje);
						Long id = Math.abs(mje.id);
						for(SolicitudesProceso sol : solicitudes){
							if(sol.IdDetalleCarga.longValue() == id.longValue()){
								sol.EstadoProceso = _c.EstadoRechazado;
								sol.Codigo = mje.codigo;
								break;
							}
						}
					}
				}
				for(ResultadoSolicitud res : salidaCrearSolicitudRescatePorComision.P_OUT_RESULTADO){
					for(SolicitudesProceso sol : solicitudes){
						if(sol.IdDetalleCarga.longValue() == res.IdInterno.longValue()){
							sol.IdSolicitudAporteRescate = res.IdSolicitudAporteRescate;
							sol.Certifica = _c.SI;
							break;
						}
					}
				}
			}
		}
		
		{
			
			//5 - APORTE DE DIVIDENDOS
			CrearSolicitudAportePorDividendos crearSolicitudAportePorDividendos = new CrearSolicitudAportePorDividendos();
			EntradaCrearSolicitudAportePorDividendos entradaCrearSolicitudAportePorDividendos = new EntradaCrearSolicitudAportePorDividendos();
			for(SolicitudesProceso sol : solicitudes){
				if(_c.EstadoPendiente.equals(sol.EstadoProceso) && sol.IdSolicitudAporteRescate.longValue() == 0 && _c.NO.equals(sol.EsActualizacion)
				   && _c.AporteDividendo.equals(sol.TipoMovimiento)){
					CrearSolicitudAportePorDividendos.InSolicitudesAporte s = new CrearSolicitudAportePorDividendos.InSolicitudesAporte();
					s.IdInterno = sol.IdDetalleCarga;
					s.IdCliente = sol.IdCliente;
					s.IdPortafolio = sol.IdPortafolio;
					s.IdSerieFondo = sol.IdSerieFondo;
					s.NumeroCuotas = sol.NumeroCuotas;
					s.IdMoneda = sol.IdMoneda;
					s.FolioContrato = 0L;
					s.IdRegimenTributario = sol.IdRegimenTributarioOrigen;
					s.IngresoAutomatico = _c.SI;
					s.FechaValorCuota = (sol.FechaValorCuota.toString().equals("0001-01-01")) ? null : sol.FechaValorCuota;
					entradaCrearSolicitudAportePorDividendos.p_SolicitudesAporte.add(s);
				}
			}
			
			if(entradaCrearSolicitudAportePorDividendos.p_SolicitudesAporte.size() > 0){
				entradaCrearSolicitudAportePorDividendos.p_IdEmpresa = entrada.p_IdEmpresa;
				entradaCrearSolicitudAportePorDividendos.p_IdUsuario = entrada.p_IdUsuario;
				entradaCrearSolicitudAportePorDividendos.p_IdModulo = entrada.p_IdModulo;
				entradaCrearSolicitudAportePorDividendos.p_SimularOperacion = _c.NO;
				entradaCrearSolicitudAportePorDividendos.p_UsaReglasNegocio = _c.SI;
				entradaCrearSolicitudAportePorDividendos.p_IdTipoCanalIngreso = _c.CanalAgente;
				int cantidadRegistros = entradaCrearSolicitudAportePorDividendos.p_SolicitudesAporte.size();
				SalidaCrearSolicitudAportePorDividendos salidaCrearSolicitudAportePorDividendos = new SalidaCrearSolicitudAportePorDividendos();
				if(cantidadRegistros > limiteRegistrosPorProcesar) {
					//Segmentar carga
					int index = 0;
					int limite = 0;
					List<cl.bcs.fondomutuo.CrearSolicitudAportePorDividendos.InSolicitudesAporte> listaRespaldoSolicitudes = entradaCrearSolicitudAportePorDividendos.p_SolicitudesAporte;
					while(index < cantidadRegistros) {
						limite =  index + limiteRegistrosPorProcesar;
						if(index+limiteRegistrosPorProcesar > cantidadRegistros) {
							limite = cantidadRegistros;
						}
						entradaCrearSolicitudAportePorDividendos.p_SolicitudesAporte = listaRespaldoSolicitudes.subList(index, limite);
						SalidaCrearSolicitudAportePorDividendos salidaCrearSolicitudAportePorDividendosParcial = crearSolicitudAportePorDividendos.logica(con, entradaCrearSolicitudAportePorDividendos);
						if(salidaCrearSolicitudAportePorDividendosParcial != null) {
							if(salidaCrearSolicitudAportePorDividendosParcial.P_OUT_ERRORESCONTADOR != null) {
								salidaCrearSolicitudAportePorDividendos.P_OUT_ERRORESCONTADOR += salidaCrearSolicitudAportePorDividendosParcial.P_OUT_ERRORESCONTADOR;
							}
							if(salidaCrearSolicitudAportePorDividendosParcial.P_OUT_MENSAJES != null) {
								salidaCrearSolicitudAportePorDividendos.P_OUT_MENSAJES.addAll(salidaCrearSolicitudAportePorDividendosParcial.P_OUT_MENSAJES);
							}
							if(salidaCrearSolicitudAportePorDividendosParcial.P_OUT_RESULTADO != null) {
								salidaCrearSolicitudAportePorDividendos.P_OUT_RESULTADO.addAll(salidaCrearSolicitudAportePorDividendosParcial.P_OUT_RESULTADO);
							}
						}
						index += limiteRegistrosPorProcesar;
					}
				} else {
					salidaCrearSolicitudAportePorDividendos = crearSolicitudAportePorDividendos.logica(con, entradaCrearSolicitudAportePorDividendos);
				}
				for(MensajeSalida mje : salidaCrearSolicitudAportePorDividendos.P_OUT_MENSAJES){
					if(MensajeSalida.MENSAJE_ERROR.equals(mje.tipo)){
						out.P_OUT_MENSAJES.add(mje);
						Long id = Math.abs(mje.id);
						for(SolicitudesProceso sol : solicitudes){
							if(sol.IdDetalleCarga.longValue() == id.longValue()){
								sol.EstadoProceso = _c.EstadoRechazado;
								sol.Codigo = mje.codigo;
								break;
							}
						}
					}
				}
				for(ResultadoSolicitud res : salidaCrearSolicitudAportePorDividendos.P_OUT_RESULTADO){
					for(SolicitudesProceso sol : solicitudes){
						if(sol.IdDetalleCarga.longValue() == res.IdInterno.longValue()){
							sol.IdSolicitudAporteRescate = res.IdSolicitudAporteRescate;
							sol.Certifica = _c.NO;
							break;
						}
					}
				}
			}
			
		}
		
		{
			//INGRESO Y RETIRO DE TITULOS
			CrearIngresoRetiroTituloFondoMutuo crearIngresoRetiroTituloFondoMutuo = new CrearIngresoRetiroTituloFondoMutuo();
			EntradaCrearIngresoRetiroTituloFondoMutuo entradaCrearIngresoRetiroTituloFondoMutuo = new EntradaCrearIngresoRetiroTituloFondoMutuo();
			for(SolicitudesProceso sol : solicitudes){
				if(_c.EstadoPendiente.equals(sol.EstadoProceso) && sol.IdSolicitudAporteRescate.longValue() == 0 && _c.NO.equals(sol.EsActualizacion)
				   && (_c.IngresoTitulo.equals(sol.TipoMovimiento) || _c.RetiroTitulo.equals(sol.TipoMovimiento))){
					CrearIngresoRetiroTituloFondoMutuo.InSolicitudesTitulo s = new CrearIngresoRetiroTituloFondoMutuo.InSolicitudesTitulo();
					s.IdInterno = sol.IdDetalleCarga;
					s.IdCliente = sol.IdCliente;
					s.IdPortafolio = sol.IdPortafolio;
					s.IdTipoMovimientoFondoMutuo = sol.TipoMovimiento;
					s.IdSerieFondo = sol.IdSerieFondo;
					s.NumeroCuotas = sol.NumeroCuotas;
					s.IdRegimenTributario = "";
					s.Glosa = "";
					s.SolicitudBatch = _c.SI;
					s.OrigenOperacion = _c.OrigenManual;
					s.IdAutorizadoOrigen = 0L;
					s.IdRegimenTributario = sol.IdRegimenTributarioOrigen;
					s.IngresoAutomatico = _c.SI;
					s.FechaValorCuota = sol.FechaValorCuota;
					entradaCrearIngresoRetiroTituloFondoMutuo.p_SolicitudesTitulo.add(s);		
				}
			}
			if(entradaCrearIngresoRetiroTituloFondoMutuo.p_SolicitudesTitulo.size() > 0){
				entradaCrearIngresoRetiroTituloFondoMutuo.p_IdEmpresa = entrada.p_IdEmpresa;
				entradaCrearIngresoRetiroTituloFondoMutuo.p_IdUsuario = entrada.p_IdUsuario;
				entradaCrearIngresoRetiroTituloFondoMutuo.p_IdModulo = entrada.p_IdModulo;
				entradaCrearIngresoRetiroTituloFondoMutuo.p_SimularOperacion = _c.NO;
				entradaCrearIngresoRetiroTituloFondoMutuo.p_UsaReglasNegocio = _c.SI;
				int cantidadRegistros = entradaCrearIngresoRetiroTituloFondoMutuo.p_SolicitudesTitulo.size();
				SalidaCrearIngresoRetiroTituloFondoMutuo salidaCrearIngresoRetiroTituloFondoMutuo = new SalidaCrearIngresoRetiroTituloFondoMutuo();
				if(cantidadRegistros > limiteRegistrosPorProcesar) {
					//Segmentar carga
					int index = 0;
					int limite = 0;
					List<cl.bcs.fondomutuo.CrearIngresoRetiroTituloFondoMutuo.InSolicitudesTitulo> listaRespaldoSolicitudes = entradaCrearIngresoRetiroTituloFondoMutuo.p_SolicitudesTitulo;
					while(index < cantidadRegistros) {
						limite =  index + limiteRegistrosPorProcesar;
						if(index+limiteRegistrosPorProcesar > cantidadRegistros) {
							limite = cantidadRegistros;
						}
						entradaCrearIngresoRetiroTituloFondoMutuo.p_SolicitudesTitulo = listaRespaldoSolicitudes.subList(index, limite);
						SalidaCrearIngresoRetiroTituloFondoMutuo salidaCrearIngresoRetiroTituloFondoMutuoParcial = crearIngresoRetiroTituloFondoMutuo.logica(con, entradaCrearIngresoRetiroTituloFondoMutuo);
						if(salidaCrearIngresoRetiroTituloFondoMutuoParcial != null) {
							if(salidaCrearIngresoRetiroTituloFondoMutuoParcial.P_OUT_ERRORESCONTADOR != null) {
								salidaCrearIngresoRetiroTituloFondoMutuo.P_OUT_ERRORESCONTADOR += salidaCrearIngresoRetiroTituloFondoMutuoParcial.P_OUT_ERRORESCONTADOR;
							}
							if(salidaCrearIngresoRetiroTituloFondoMutuoParcial.P_OUT_MENSAJES != null) {
								salidaCrearIngresoRetiroTituloFondoMutuo.P_OUT_MENSAJES.addAll(salidaCrearIngresoRetiroTituloFondoMutuoParcial.P_OUT_MENSAJES);
							}
							if(salidaCrearIngresoRetiroTituloFondoMutuoParcial.P_OUT_RESULTADO != null) {
								salidaCrearIngresoRetiroTituloFondoMutuo.P_OUT_RESULTADO.addAll(salidaCrearIngresoRetiroTituloFondoMutuoParcial.P_OUT_RESULTADO);
							}
						}
						index += limiteRegistrosPorProcesar;
					}
				}else {
					salidaCrearIngresoRetiroTituloFondoMutuo = crearIngresoRetiroTituloFondoMutuo.logica(con, entradaCrearIngresoRetiroTituloFondoMutuo);
				}
				for(MensajeSalida mje : salidaCrearIngresoRetiroTituloFondoMutuo.P_OUT_MENSAJES){
					if(MensajeSalida.MENSAJE_ERROR.equals(mje.tipo)){
						out.P_OUT_MENSAJES.add(mje);
						Long id = Math.abs(mje.id);
						for(SolicitudesProceso sol : solicitudes){
							if(sol.IdDetalleCarga.longValue() == id.longValue()){
								sol.EstadoProceso = _c.EstadoRechazado;
								sol.Codigo = mje.codigo;
								break;
							}
						}
					}
				}
				for(ResultadoSolicitud res : salidaCrearIngresoRetiroTituloFondoMutuo.P_OUT_RESULTADO){
					for(SolicitudesProceso sol : solicitudes){
						if(sol.IdDetalleCarga.longValue() == res.IdInterno.longValue()){
							sol.IdSolicitudAporteRescate = res.IdSolicitudAporteRescate;
							break;
						}
					}
				}
			}
			
		}
		
		{
			//APORTE Y RESCATE POR FUSION
			CrearAporteRescateFusion crearAporteRescateFusion = new CrearAporteRescateFusion();
			EntradaCrearAporteRescateFusion entradaCrearAporteRescateFusion = new EntradaCrearAporteRescateFusion();
			for(SolicitudesProceso sol : solicitudes){
				if(_c.EstadoPendiente.equals(sol.EstadoProceso) && sol.IdSolicitudAporteRescate.longValue() == 0 && _c.NO.equals(sol.EsActualizacion)
				   && (_c.AporteFusion.equals(sol.TipoMovimiento) || _c.RescateFusion.equals(sol.TipoMovimiento))){
					CrearAporteRescateFusion.InSolicitudesTitulo s = new CrearAporteRescateFusion.InSolicitudesTitulo();
					s.IdInterno = sol.IdDetalleCarga;
					s.IdCliente = sol.IdCliente;
					s.IdPortafolio = sol.IdPortafolio;
					s.IdTipoMovimientoFondoMutuo = sol.TipoMovimiento;
					s.IdSerieFondo = sol.IdSerieFondo;
					s.NumeroCuotas = sol.NumeroCuotas;
					s.IdRegimenTributario = "";
					s.Glosa = "";
					s.SolicitudBatch = _c.SI;
					s.OrigenOperacion = _c.OrigenManual;
					s.IdAutorizadoOrigen = 0L;
					s.IdRegimenTributario = sol.IdRegimenTributarioOrigen;
					s.IngresoAutomatico = _c.SI;
					s.FechaValorCuota = sol.FechaValorCuota;
					entradaCrearAporteRescateFusion.p_SolicitudesTitulo.add(s);		
				}
			}
			
			if(entradaCrearAporteRescateFusion.p_SolicitudesTitulo.size() > 0){
				entradaCrearAporteRescateFusion.p_IdEmpresa = entrada.p_IdEmpresa;
				entradaCrearAporteRescateFusion.p_IdUsuario = entrada.p_IdUsuario;
				entradaCrearAporteRescateFusion.p_IdModulo = entrada.p_IdModulo;
				entradaCrearAporteRescateFusion.p_SimularOperacion = _c.NO;
				entradaCrearAporteRescateFusion.p_UsaReglasNegocio = _c.SI;
				int cantidadRegistros = entradaCrearAporteRescateFusion.p_SolicitudesTitulo.size();
				SalidaCrearAporteRescateFusion salidaCrearAporteRescateFusion = new SalidaCrearAporteRescateFusion();
				if(cantidadRegistros > limiteRegistrosPorProcesar) {
					//Segmentar carga
					int index = 0;
					int limite = 0;
					List<InSolicitudesTitulo> listaRespaldoSolicitudes = entradaCrearAporteRescateFusion.p_SolicitudesTitulo;
					while(index < cantidadRegistros) {
						limite =  index + limiteRegistrosPorProcesar;
						if(index+limiteRegistrosPorProcesar > cantidadRegistros) {
							limite = cantidadRegistros;
						}
						entradaCrearAporteRescateFusion.p_SolicitudesTitulo = listaRespaldoSolicitudes.subList(index, limite);
						SalidaCrearAporteRescateFusion salidaCrearAporteRescateFusionParcial = crearAporteRescateFusion.logica(con, entradaCrearAporteRescateFusion);
						if(salidaCrearAporteRescateFusionParcial != null) {
							if(salidaCrearAporteRescateFusionParcial.P_OUT_ERRORESCONTADOR != null) {
								salidaCrearAporteRescateFusion.P_OUT_ERRORESCONTADOR += salidaCrearAporteRescateFusionParcial.P_OUT_ERRORESCONTADOR;
							}
							if(salidaCrearAporteRescateFusionParcial.P_OUT_MENSAJES != null) {
								salidaCrearAporteRescateFusion.P_OUT_MENSAJES.addAll(salidaCrearAporteRescateFusionParcial.P_OUT_MENSAJES);
							}
							if(salidaCrearAporteRescateFusionParcial.P_OUT_RESULTADO != null) {
								salidaCrearAporteRescateFusion.P_OUT_RESULTADO.addAll(salidaCrearAporteRescateFusionParcial.P_OUT_RESULTADO);
							}
						}
						index += limiteRegistrosPorProcesar;
					}
				} else {
					salidaCrearAporteRescateFusion = crearAporteRescateFusion.logica(con, entradaCrearAporteRescateFusion);
				}
				
				for(MensajeSalida mje : salidaCrearAporteRescateFusion.P_OUT_MENSAJES){
					if(MensajeSalida.MENSAJE_ERROR.equals(mje.tipo)){
						out.P_OUT_MENSAJES.add(mje);
						Long id = Math.abs(mje.id);
						for(SolicitudesProceso sol : solicitudes){
							if(sol.IdDetalleCarga.longValue() == id.longValue()){
								sol.EstadoProceso = _c.EstadoRechazado;
								sol.Codigo = mje.codigo;
								break;
							}
						}
					}
				}
				for(ResultadoSolicitud res : salidaCrearAporteRescateFusion.P_OUT_RESULTADO){
					for(SolicitudesProceso sol : solicitudes){
						if(sol.IdDetalleCarga.longValue() == res.IdInterno.longValue()){
							sol.IdSolicitudAporteRescate = res.IdSolicitudAporteRescate;
							break;
						}
					}
				}
			}
			
		}
		
		{
			//TODO:INGRESO Y RETIRO GARANTIAS
		}
		
		{
			// APORTES POR DIVIDENDO CFI
			CrearSolicitudAportePorDividendosCFI crearSolicitudAporteDividendosCFI = new CrearSolicitudAportePorDividendosCFI();
			EntradaCrearSolicitudAportePorDividendosCFI entradaCrearSolicitudAporteDividendoCFI = new EntradaCrearSolicitudAportePorDividendosCFI();
			for(SolicitudesProceso sol : solicitudes){
				if(_c.EstadoPendiente.equals(sol.EstadoProceso) && _c.AporteDividendoCFI.equals(sol.TipoMovimiento)){
					CrearSolicitudAportePorDividendosCFI.InSolicitudesAporte s = new CrearSolicitudAportePorDividendosCFI.InSolicitudesAporte();
					s.IdInterno = sol.IdDetalleCarga;
					s.IdCliente = sol.IdCliente;
					s.IdPortafolio = sol.IdPortafolio;
					s.IdSerieFondo = sol.IdSerieFondo;
					s.CalculoPorMontoCuota = sol.CalculoPorMontoCuota;
					s.Monto = sol.Monto;
					s.NumeroCuotas = sol.NumeroCuotas;
					s.IdMoneda = sol.IdMoneda;
					s.IdRegimenTributario = sol.IdRegimenTributarioOrigen;
					s.Glosa = "";
					s.FechaValorCuota = (sol.FechaValorCuota.toString().equals("0001-01-01")) ? null : sol.FechaValorCuota;
					entradaCrearSolicitudAporteDividendoCFI.p_SolicitudesAporte.add(s);
				}
			}
			
			if(entradaCrearSolicitudAporteDividendoCFI.p_SolicitudesAporte.size() > 0){
				entradaCrearSolicitudAporteDividendoCFI.p_IdEmpresa = entrada.p_IdEmpresa;
				entradaCrearSolicitudAporteDividendoCFI.p_IdUsuario = entrada.p_IdUsuario;
				entradaCrearSolicitudAporteDividendoCFI.p_IdModulo = entrada.p_IdModulo;
				entradaCrearSolicitudAporteDividendoCFI.p_SimularOperacion = _c.NO;
				entradaCrearSolicitudAporteDividendoCFI.p_UsaReglasNegocio = _c.SI;
				int cantidadRegistros = entradaCrearSolicitudAporteDividendoCFI.p_SolicitudesAporte.size();
				SalidaCrearSolicitudAportePorDividendosCFI salidaCrearSolicitudAporteDividendoCFI = new SalidaCrearSolicitudAportePorDividendosCFI();
				if(cantidadRegistros > limiteRegistrosPorProcesar) {
					//Segmentar carga
					int index = 0;
					int limite = 0;
					List<cl.bcs.fondomutuo.CrearSolicitudAportePorDividendosCFI.InSolicitudesAporte> listaRespaldoSolicitudes = entradaCrearSolicitudAporteDividendoCFI.p_SolicitudesAporte;
					while(index < cantidadRegistros) {
						limite =  index + limiteRegistrosPorProcesar;
						if(index+limiteRegistrosPorProcesar > cantidadRegistros) {
							limite = cantidadRegistros;
						}
						entradaCrearSolicitudAporteDividendoCFI.p_SolicitudesAporte = listaRespaldoSolicitudes.subList(index, limite);
						SalidaCrearSolicitudAportePorDividendosCFI salidaCrearSolicitudAporteDividendoCFIParcial = crearSolicitudAporteDividendosCFI.logica(con, entradaCrearSolicitudAporteDividendoCFI);
						if(salidaCrearSolicitudAporteDividendoCFIParcial != null) {
							if(salidaCrearSolicitudAporteDividendoCFIParcial.P_OUT_ERRORESCONTADOR != null) {
								salidaCrearSolicitudAporteDividendoCFI.P_OUT_ERRORESCONTADOR += salidaCrearSolicitudAporteDividendoCFIParcial.P_OUT_ERRORESCONTADOR;
							}
							if(salidaCrearSolicitudAporteDividendoCFIParcial.P_OUT_MENSAJES != null) {
								salidaCrearSolicitudAporteDividendoCFI.P_OUT_MENSAJES.addAll(salidaCrearSolicitudAporteDividendoCFIParcial.P_OUT_MENSAJES);
							}
							if(salidaCrearSolicitudAporteDividendoCFIParcial.P_OUT_RESULTADO != null) {
								salidaCrearSolicitudAporteDividendoCFI.P_OUT_RESULTADO.addAll(salidaCrearSolicitudAporteDividendoCFIParcial.P_OUT_RESULTADO);
							}
						}
						index += limiteRegistrosPorProcesar;
					}
				}else {
					salidaCrearSolicitudAporteDividendoCFI = crearSolicitudAporteDividendosCFI.logica(con, entradaCrearSolicitudAporteDividendoCFI);
				}
				for(MensajeSalida mje : salidaCrearSolicitudAporteDividendoCFI.P_OUT_MENSAJES){
					if(MensajeSalida.MENSAJE_ERROR.equals(mje.tipo)){
						out.P_OUT_MENSAJES.add(mje);
						Long id = Math.abs(mje.id);
						for(SolicitudesProceso sol : solicitudes){
							if(sol.IdDetalleCarga.longValue() == id.longValue()){
								sol.EstadoProceso = _c.EstadoRechazado;
								sol.Codigo = mje.codigo;
								break;
							}
						}
					}
				}
				for(ResultadoSolicitud res : salidaCrearSolicitudAporteDividendoCFI.P_OUT_RESULTADO){
					for(SolicitudesProceso sol : solicitudes){
						if(sol.IdDetalleCarga.longValue() == res.IdInterno.longValue()){
							sol.IdSolicitudAporteRescate = res.IdSolicitudAporteRescate;
							sol.Certifica = _c.SI;
							break;
						}
					}
				}
			}
		}
				
		{
			//CERTIFICACION
			if ("S".equalsIgnoreCase(certificacionAutomaticaCargaMasiva)) {
				CertificarSolicitudAporteRescate certificarSolicitudAporteRescate = new CertificarSolicitudAporteRescate();
				EntradaCertificarSolicitudAporteRescate entradaCertificarSolicitudAporteRescate = new EntradaCertificarSolicitudAporteRescate();
				for(SolicitudesProceso sol : solicitudes){
					if(_c.EstadoPendiente.equals(sol.EstadoProceso) && _c.SI.equals(sol.Certifica)){
						CertificarSolicitudAporteRescate.InSolicitudes s = new CertificarSolicitudAporteRescate.InSolicitudes();
						s.IdSolicitudAporteRescate = sol.IdSolicitudAporteRescate;
						entradaCertificarSolicitudAporteRescate.p_Solicitudes.add(s);
					}
				}
				
				if(entradaCertificarSolicitudAporteRescate.p_Solicitudes.size() > 0){
					entradaCertificarSolicitudAporteRescate.p_IdEmpresa = entrada.p_IdEmpresa;
					entradaCertificarSolicitudAporteRescate.p_IdModulo = entrada.p_IdModulo;
					entradaCertificarSolicitudAporteRescate.p_IdUsuario = entrada.p_IdUsuario;
					entradaCertificarSolicitudAporteRescate.p_SimularOperacion = _c.NO;
					entradaCertificarSolicitudAporteRescate.p_UsaReglasNegocio = _c.SI;
					entradaCertificarSolicitudAporteRescate.p_FechaCertificacion = fechaSistema;
					SalidaCertificarSolicitudAporteRescate salidaCertificarSolicitudAporteRescate = certificarSolicitudAporteRescate.logica(con, entradaCertificarSolicitudAporteRescate);
					out.P_OUT_ERRORESCONTADOR += salidaCertificarSolicitudAporteRescate.P_OUT_ERRORESCONTADOR;
					
					for(ResultadoCertificacion res : salidaCertificarSolicitudAporteRescate.P_OUT_RESULTADO){
						if(_c.EstadoRechazado.equals(res.Estado)){
							for(SolicitudesProceso sol : solicitudes){
								if(sol.IdSolicitudAporteRescate.longValue() == res.IdSolicitudAporteRescate.longValue()){
									sol.EstadoProceso = _c.EstadoRechazado;
									sol.Codigo = res.Codigo;
									break;
								}
							}
						}
					}
				}
			}
		}
		
		for(SolicitudesProceso sol : solicitudes){
			if(_c.EstadoPendiente.equals(sol.EstadoProceso)){
				sol.EstadoProceso = _c.EstadoProcesado;
				sol.Codigo = "UI_EXITO";
			}
		}
		
		sql.batch(con, SQL_ACTUALIZAR_DETALLE, solicitudes, SolicitudesProceso.class);
		
		{
			//AGREGAR LOG
			AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
			AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
			log.IdCabeceraLog = 0L;
			log.IdTipoLog = "INGRESO";
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
			log.IdTipoRegistro = "PROCESAR_CARGA_SOLICITUD_APORTE_RESCATE";
			log.EstadoRegistro = _c.EstadoProcesado;
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
		
		for(SolicitudesProceso sol : solicitudes){
			OutCargas o = new OutCargas();
			o = Utils.copyPojo(sol, o);
			o.Descripcion = new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,sol.Codigo,new HashMap<String,String>(),con,entrada.p_IdEmpresa).descripcion;
			out.P_OUT_RESULTADO.add(o);
		}
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ProcesarCargaSolicitudesAporteRescate", finServicio - inicioServicio);
		return out;
	}
	
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getQAConnection()) {
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();			
		
		String Json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":15,\"p_IdModulo\":\"FMM\", \"p_IdCargas\":[{\"IdInterno\":1, \"IdCarga\":1230}] }";		
		
		EntradaProcesarCargaSolicitudesAporteRescate Objin = gson.fromJson(Json, EntradaProcesarCargaSolicitudesAporteRescate.class);
		ProcesarCargaSolicitudesAporteRescate  demo = new ProcesarCargaSolicitudesAporteRescate();				
		try {
			SalidaProcesarCargaSolicitudesAporteRescate ObjOut = demo.logica(con, Objin);
			//con.rollback();
			con.commit();
			System.out.println(ObjOut.toString());
		}
		catch(Exception er) {
			er.printStackTrace();	
			con.rollback();
		}
						
    	}
		System.out.println("== END ==");
	}

}
